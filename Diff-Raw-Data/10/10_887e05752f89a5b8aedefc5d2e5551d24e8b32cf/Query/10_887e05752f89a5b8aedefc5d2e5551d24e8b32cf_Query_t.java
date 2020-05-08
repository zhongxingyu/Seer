 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package FP;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 /**
  *
  * @author lycantrophe
  */
 public class Query {
 
     private Connection con;
     PreparedStatement statement;
 
     public Query() {
         try {
             con = DriverManager.getConnection(
                     "jdbc:default:connection");
         } catch (SQLException e) {
             /*
              * Handle exception
              */
         }
     }
 
     public void updateAppointment(Appointment appointment, ArrayList<Person> newParticipants, ArrayList<Person> oldParticipants) {
 
         try {
             statement = con.prepareStatement("UPDATE appointment SET start=?, end=?, owner=?, description=?, setLocation=?");
             statement.setDate(1, new java.sql.Date(appointment.getStart().getTime()));
             statement.setDate(2, new java.sql.Date(appointment.getEnd().getTime()));
             statement.setString(3, appointment.getOwner().getUsername());
             statement.setString(4, appointment.getDescription());
         } catch (SQLException e) {
             /*
              * Handle exception
              */
         }
 
         addAppointment(appointment, newParticipants);
 
         try {
             statement = con.prepareStatement("DELETE FROM appointmentRel WHERE username=? AND appointmentid=? )");
 
             for (Person other : oldParticipants) {
                 statement.setString(1, other.getUsername());
                 statement.setString(2, appointment.getId());
                 statement.executeUpdate();
             }
         } catch (SQLException e) {
             /*
              * Handle exception
              */
         }
     }
 
     public void deleteAppointment(Appointment appointment) {
         try {
             statement = con.prepareStatement("DELETE FROM appointmentRel WHERE  appointmentid = ?");
             statement.setString(1, appointment.getId());
         } catch (SQLException e) {
             /*
              * handle exception
              */
         }
     }
 
     public void updateStatus(String status, Appointment appointment, Person person) {
 
         try {
             statement = con.prepareStatement("UPDATE appointmentRel SET status = ? WHERE appointmentId = ? AND username = ? ");
             statement.setString(1, status);
             statement.setString(2, appointment.getId());
             statement.setString(3, person.getUsername());
         } catch (SQLException e) {
             /*
              * Handle exception
              */
         }
     }
 
     public void addAppointment(Appointment appointment, ArrayList<Person> persons) {
 
         try {
             con.prepareStatement("INSERT INTO appointmentRel (username, appointmentid, status ) VALUES ( ?, ?, ?)");
 
             for (Person other : persons) {
                 statement.setString(1, other.getUsername());
                 statement.setString(2, appointment.getId());
                 statement.setString(3, "PENDING");
                 statement.executeUpdate();
             }
         } catch (SQLException e) {
             /*
              * handle exception
              */
         }
     }
 
    public void close() {
        try {
            con.close();
        } catch (SQLException e) {
            /*
             * Handle exception
             */
        }
     }
 }
