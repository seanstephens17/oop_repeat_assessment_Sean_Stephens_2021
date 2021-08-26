package server;
/*
The CAOClientHandler will run as a thread. It should listen for messages from the Client and respond to them.There should be one CAOClientHandler per Client.
 */

import org.example.client.Menu;
import org.example.core.CAOService;
import org.example.core.DTOs.Course;
import org.example.core.DTOs.Student;
import org.example.core.DTOs.Student_Course;
import org.example.server.DAOs.*;
import org.example.server.Exceptions.DaoException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;


public class VaccineClientHandler implements Runnable {
    BufferedReader socketReader;
    PrintWriter socketWriter;
    Socket socket;
    VaccineCentre v = null;
    List<VaccineCentre> vaccinecentres;


    public VaccineClientHandler(Socket clientSocket, int clientNumber) {
        try {

            InputStreamReader isReader = new InputStreamReader(clientSocket.getInputStream());
            this.socketReader = new BufferedReader(isReader);

            OutputStream os = clientSocket.getOutputStream();
            this.socketWriter = new PrintWriter(os, true);

            //  this.clientNumber = clientNumber; //ID number assigned to client (not needed)
            this.socket = clientSocket; // store socket reference for closing.
        } catch (IOException ex) {
            ex.printStackTrace();

        }
    }

    //Declare DAO Objects.//
    UserDaoInterface IUserDao = new MySqlUserDao();
    VaccineCentreDaoInterface IVaccineCentreDao = new MySqlVaccineCentreDao();
    VaccineAppointmentDaoInterface IVaccineAppointmentDao = new MySqlVaccineAppointmentDao();

    @Override
    public void run() {

        try {
            String message = this.socketReader.readLine();
            String[] components = message.split(VaccineService.BREAKING_CHARACTER);
            if (components[0].equals("REGISTER"))
            {
                registerUser(components);
            }
            else if (components[0].equals("LOGIN")) {
                login(components);
                String message1 = this.socketReader.readLine();
                String[] components1 = message1.split(VaccineService.BREAKING_CHARACTER);
                if (components1[0].equals("DISPLAY VACCINE_CENTRE"))
                {
                    findVaccineCentre(components1);
                }
                if (components1[0].equals("DISPLAY ALL"))
                {
                    displayVaccineCentres();

                }
                if (components1[0].equals("DISPLAY CURRENT"))
                {
                    findVaccineCentresForUser(components1);
                }
            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }



    //display all courses//
    private void displayVaccineCentres() {
        if (IVaccineCentreDao != null) {
            try {

                socketWriter.println(IVaccineCentreDao.findAllVaccineCentres());

            } catch (DaoException E) {
                E.printStackTrace();
            }
        }
    }


    //find a course//
    private void findVaccineCentre(String[] components1) {

        if (IVaccineCentreDao != null) {
            try {
                String courseId = components1[1];
                socketWriter.println(IVaccineCentreDao.findVaccineCentre(centre_id));

            } catch (DaoException E) {
                E.printStackTrace();
            }
        }

    }

    //Register a user//
    private void registerUser(String[] components) {

        if (IUserDao != null)
            try {
                int user_id = Integer.parseInt(components[1]);
                String email = components[2];
                String password = components[3];

                if (IUserDao.registerUser(new User(user_id, email, password))) {
                    socketWriter.println(VaccineService.SUCCESSFUL_REGISTER);
                } else {
                    socketWriter.println(VaccineService.FAILED_REGISTER);
                }
            } catch (DaoException E) {
                E.printStackTrace();
            }
    }

    //Find a student.//
    private static void findUser(int user_id) {
        UserDaoInterface IUserDao = new MySqlUserDao();

        try {
            User user = IUserDao.findUser(1001);  // get student based on CAO number
            if (user != null)
                System.out.println("\nUser found:" + user);
            else
                System.out.print("User not found");
        } catch (DaoException e5) {
            e5.printStackTrace();
        }
    }


    //Check if a user is registered.//
    private static void checkIfRegistered(User u) {
        UserDaoInterface IUserDao = new MySqlUserDao();
        try {
            if (IUserDao.checkIfRegistered(new User(1005, "sean@outlook.com", "sean1234")))
                System.out.println("The user has been registered");
            else
                System.out.println("No registration found");
        } catch (
                DaoException e2) {
            e2.printStackTrace();
        }
    }


    //Let a student login.//
    public void login(String[] components) {
        if (IUserDao != null)
            try {
                int user_id = Integer.parseInt(components[1]);

                if (IUserDao.login(user_id, components[2]) ) {
                    socketWriter.println("True");
                } else {
                    socketWriter.println("LOGIN FAILED");
                }
            } catch (DaoException E) {
                E.printStackTrace();
            }
    }



    private void findVaccineCentresForUser(String[] components1){


        if (IVaccineCentreDao != null) {
            try {
                int user_id = Integer.parseInt(components1[1]);
                socketWriter.println(IVaccineCentreDao.findVaccineCentresForUser(user_id));

            } catch (DaoException E) {
                E.printStackTrace();
            }
        }
    }


    public void updateChoices(String[] components) {
        if (IVaccineAppointmentDao != null)
            try {
                String user_id = components[1];
                if (IVaccineAppointmentDao.updateChoices(user_id)) {
                    socketWriter.println(VaccineService.UPDATE_CURRENT);
                } else{
                    socketWriter.println(VaccineService.FAILED_UPDATE_CURRENT);
                }
            } catch (DaoException E) {
                E.printStackTrace();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
    }


}



