 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package FP;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
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
             statement.executeUpdate();
         } catch (SQLException e) {
             /*
              * Handle exception
              */
         }
 
         addAppointment(appointment, newParticipants);
 
         try {
             statement = con.prepareStatement("DELETE FROM appointmentRel WHERE username = ? AND appointmentid = ? )");
 
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
         updateStatus(Attending.Status.PENDING.toString(), appointment, null);
     }
 
     public void deleteAppointment(Appointment appointment) {
         try {
             // This should remove all entries in AppointmentRel
             statement = con.prepareStatement("DELETE FROM Appointment WHERE ID = ?");
            statement.setString(1, appointment.getId());
             statement.executeUpdate();
         } catch (SQLException e) {
             /*
              * handle exception
              */
         }
         try {
             // This should remove all entries in AppointmentRel
             statement = con.prepareStatement("DELETE FROM AppointmentRel WHERE ID = ?");
             statement.setString(1, appointment.getId());
             statement.executeUpdate();
         } catch (SQLException e) {
             /*
              * handle exception
              */
         }
     }
 
     public void updateStatus(String status, Appointment appointment, Person person) {
 
         String query;
         if (person != null) {
             query = "UPDATE AppointmentRel SET Status = ? WHERE ID = ? AND Person = ? ";
         } else {
             query = "UPDATE appointmentRel SET Status = ? WHERE ID = ?";
         }
 
         try {
             statement = con.prepareStatement(query);
             statement.setString(1, status);
             statement.setString(2, appointment.getId());
             if (person != null) {
                 statement.setString(3, person.getUsername());
             }
         } catch (SQLException e) {
             /*
              * Handle exception
              */
         }
     }
 
     public void addAppointment(Appointment appointment, ArrayList<Person> persons) {
 
         try {
             con.prepareStatement("INSERT INTO AppointmentRel (Username, Appointmentid, Status ) VALUES ( ?, ?, ? )");
 
             for (Person other : persons) {
                 statement.setString(1, other.getUsername());
                 statement.setString(2, appointment.getId());
                 statement.setString(3, Attending.Status.PENDING.toString());
                 statement.executeUpdate();
             }
         } catch (SQLException e) {
             /*
              * handle exception
              */
         }
     }
 
     public boolean authorize(String username, String password) {
 
         try {
             statement = con.prepareStatement("SELECT COUNT(*) AS Valid FROM Users WHERE Username = ? AND Password = ?");
             statement.setString(1, username);
             statement.setString(2, password);
             ResultSet rs = statement.executeQuery();
             return rs.getInt("Valid") == 1 ? true : false;
         } catch (SQLException e) {
             /*
              * Handle exception
              */
         }
         return false;
     }
 
     public ArrayList<Person> getPersons() {
         ArrayList<Person> persons = new ArrayList<Person>();
         try {
             statement = con.prepareStatement("SELECT * FROM Person");
             ResultSet rs = statement.executeQuery();
             while (rs.next()) {
                 persons.add(new Person(rs.getString("Username"), rs.getString("Firstname"), rs.getString("Surname"), rs.getString("Email"), rs.getString("Phonenumber")));
             }
         } catch (SQLException e) {
             /*
              * Handle exception
              */
         }
         return persons;
     }
 
     public void createAllAppointments(Map<String, Person> persons, Map<String, Location> locations) {
 
         HashMap<String, ArrayList<Attending>> appointments = new HashMap<String, ArrayList<Attending>>();
         try {
             // Get the relations
             statement = con.prepareStatement("SELECT * FROM appointmentRel");
             ResultSet rs = statement.executeQuery();
             // Builds the map of all the appointment participants
             while (rs.next()) {
                 String key = rs.getString("ID");
                 if (!appointments.containsKey(key)) {
                     appointments.put(key, new ArrayList<Attending>());
                 }
                 // Adds the newly constructed Attending tuple to the appointments list
                 appointments.get(key).add(new Attending(persons.get(key), Attending.Status.valueOf(rs.getString("Status"))));
 
             }
         } catch (SQLException e) {
             /*
              * Handle exception
              */
         }
         // Starts the work
         try {
             statement = con.prepareStatement("SELECT * FROM Appointment");
             ResultSet rs = statement.executeQuery();
 
             while (rs.next()) {
                 // Builds the participant arraylist
                 ArrayList<String> participants = new ArrayList<String>(Arrays.asList(rs.getString("Participants").split(",")));
 
                 String id = rs.getString("ID");
                 Person owner = persons.get(rs.getString("Owner"));
 
                 // Constructs the appointment
                 Appointment appointment = new AppointmentImpl(owner, rs.getTime("StartDate"), rs.getTime("EndDate"), rs.getString("Description"), appointments.get(id), participants, locations.get(rs.getString("LocationID")));
 
                 // Adds this appointment to everyone invited
                 for (Attending other : appointments.get(id)) {
                     other.getPerson().addAppointment(appointment);
                 }
             }
 
         } catch (SQLException e) {
             /*
              * Handle exception
              */
         }
     }
 
     public Appointment createAppointment(Appointment newAppointment) {
         Appointment appointment = new AppointmentImpl();
         appointment.clone(newAppointment);
         ResultSet rs;
 
         try {
             statement = con.prepareStatement("INSERT INTO Appointment ( owner, start, end, description, location)"
                     + "VALUES( ?, ?, ?, ?, ? )");
             statement.setString(1, appointment.getOwner().getUsername());
            statement.setTime(2, new java.sql.Time(appointment.getStart().getTime()));
            statement.setTime(3, new java.sql.Time(appointment.getEnd().getTime()));
             statement.setString(4, appointment.getDescription());
             statement.setInt(5, appointment.getLocation().getId());
             rs = statement.getGeneratedKeys();

             appointment.setId(rs.getString("ID"));
         } catch (SQLException e) {
             /*
              * handle exception
              */
         }
         return appointment;
     }
 
     public ArrayList<Location> getLocations() {
 
         ArrayList<Location> locations = new ArrayList<Location>();
         try {
             statement = con.prepareStatement("SELECT * FROM Location "
                     + "INNER JOIN Room ON Location.RoomID = Room.RoomID "
                     + "INNER JOIN RoomType ON Room.RoomTypeID = RTID");
             ResultSet rs = statement.executeQuery();
             // Send in hash or arraylist to map up persons?
             while (rs.next()) {
                 if (rs.getString("Type") != null) {
                     locations.add(new Room(rs.getInt("LocID"), rs.getString("Location.Description"), rs.getInt("Size"), AbstractLocation.Roomtype.valueOf(rs.getString("RoomType.Description"))));
                 } else {
                     locations.add(new OtherLocation(rs.getString("Description")));
                 }
             }
         } catch (SQLException e) {
             /*
              * Handle exception
              */
         }
         return locations;
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
