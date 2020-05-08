 package cz.muni.fi.pv168.clockcard;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 /**
  * Class that represents worker who is using the ClockCard system.
  *
  * @author Marek Osvald
  * @version 2011.0518
  */
 
 public class Worker extends ADatabaseStoreable {
     private static final String PROPERTY_FILE = "src/Worker.properties";
 
     private static final Properties properties = loadProperties();
     
     private Long id;
     private String name;
     private String surname;
     private String login;
     private String password;
     private Shift currentShift = null;
     private boolean suspended = false;
 
     /**
      * Loads properties from file.
      *
      * @return properties from the file
      */
     public static Properties loadProperties() {
         try {
             FileInputStream inputStream = new FileInputStream(PROPERTY_FILE);
             Properties prop = new Properties();
             prop.load(inputStream);
             inputStream.close();
             return prop;
         } catch (IOException e) {
             return new Properties();
             //TODO: LOG fatal error, Property file not found.
         }
     }
         
     /**
      * Resets the password of the selected worker to the default value.
      *
      * @param worker worker whose password is being reset
      */
     public static void resetForgottenPassword(Worker worker) {
         worker.password = properties.getProperty("defaultPassword");
     }
 
     /**
      * TODO: Javadoc
      *
      * @param id
      * @param name
      * @param surname
      * @param login
      * @param password
      * @param currentShift
      * @param suspended
      * @return
      */
     public static Worker loadWorker(long id, String name, String surname, String login, String password, long currentShift, boolean suspended) {
         return new Worker(id, name, surname, login, password, currentShift, suspended);
     }
 
     /**
      * Parametric constructor. This constructor is used to recreate objects that
      * have been previuosly stored in the database.
      *
      * @param id id of the worker
      * @param name name of the worker
      * @param surname surname of the worker
      * @param login login of the worker
      * @param password password of the worker
      * @param currentShift id of the current pending shift
      * @param suspended true when worker is suspended, false otherwise
      */
     private Worker(long id, String name, String surname, String login, String password, long currentShift, boolean suspended) {
         this.id = id;
         this.name = name;
         this.surname = surname;
         this.password = password;
         this.login = login;
         this.currentShift = (Shift) ShiftManager.getInstance().find(currentShift);
         this.suspended = suspended;
     }
     
     /**
      * Parametric constructor. This constructor is used for objects that have
      * not been serialized into the database yet.
      * 
      * @param name name of the worker
      * @param surname surname of the worker
      * @param login login of the worker
      */
     public Worker(String name, String surname, String login) {
         this.name = name;
         this.surname = surname;
         this.login = login;
         this.password = properties.getProperty("defaultPassword");
     }
 
     /**
      * Returns ID of the worker.
      *
      * @return id of the worker or null when the worker has not been saved to the database yet.
      */
     public Long getID() {
         return id;
     }
     /**
      * Returns name of the worker.
      *
      * @return name of the worker
      */
     public String getName() {
         return name;
     }
     /**
      * Sets name of the worker.
      *
      * @param name name of the worker to be set
      */
     public void setName(String name) {
         this.name = name;
     }
     /**
      * Returns surname.
      *
      * @return surname of the worker
      */
     public String getSurname() {
         return surname;
     }
     /**
      * Sets surname of the worker.
      *
      * @param surname surname of the worker.
      */
     public void setSurname(String surname) {
         this.surname = surname;
     }
     /**
      * Returns login of the worker.
      *
      * @return login of the worker
      */
     public String getLogin() {
         return login;
     }
     /**
      * Sets login of the worker.
      *
      * @param login login of the worker.
      */
     public void setLogin(String login) {
         this.login = login;
     }
     /**
      * Sets password of the worker.
      *
      * @param password password of the worker.
      */
     public void setPassword(String password) {
         this.password = password;
     }
     /**
      * Returns current pending shift.
      *
      * @return current pending shift
      */
     public Shift getCurrentShift() {
         return currentShift;
     }
     /**
      * Tries to log user into the system.
      *
      * @param password password to authenticate with
      * @return true when password is correct and worker is not suspended, false otherwise
      */
     public boolean authenticate(String password) {
         if (suspended) {
             return false;
         }
         
         return this.password.equals(password);
     }
     /**
      * Starts a new shift. Provided that worker is suspended or already has
      * a pending shift throws a WorkerException.
      *
      * @throws WorkerException
      */
     public void startShift() throws WorkerException {
         if (isSuspended()) {
             throw new WorkerException("Suspended workers cannot start a new shift.");
         }
 
         if (getCurrentShift() != null) {
             throw new WorkerException("Worker already has a pending shift.");
         }
 
         //TODO use the right constructor currentShift = new Shift();
     }
     /**
      * Ends pending shift. Provided that worker is suspended or has not
      * a pending shift throws a WorkerException.
      *
      * @throws WorkerException
      */
     public void endShift() throws WorkerException {
         if (getCurrentShift() == null) {
             throw new WorkerException("Worker has not a pending shift.");
         }
 
        //TODO: Create instance of Calendar (current date and time)
         currentShift = null;
     }
     
     public void startBreak() {
        //TODO implement
     }
 
     public void endBreak() {
         //TODO implement
     }
     /**
      * Returns whether the worker is suspended or not.
      *
      * @return true when worker is suspended, false otherwise
      */
     public boolean isSuspended() {
         return suspended;
     }
     /**
      * Suspends worker.
      *
      * @throws WorkerException
      */
     public void suspend() throws WorkerException {
         if (isSuspended()) {
             throw new WorkerException("Worker is already suspended");
         }
 
         suspended = true;
     }
     /**
      * Cancels previous suspension.
      *
      * @throws WorkerException
      */
     public void unsuspend() throws WorkerException {
         if (!isSuspended()) {
             throw new WorkerException("Worker is not suspended.");
         }
 
         suspended = false;
     }
     /**
      * Returns all shifts worked by worker.
      *
      * @return list of all shifts worked by worker
      */
     public List<Shift> getShifts() {
         return new ArrayList<Shift>();
         //TODO implement return Shift.findByWorker(id);
     }
     /**
      * Saves worker into the database. Executes INSERT when worker is saved for
      * the first time, UPDATE otherwise.
      *
      * @return true when worker is successfuly saved, false otherwise
      * @throws SQLException
      */
     @Override
     public boolean save() {
         Connection connection = null;
         PreparedStatement preparedStatement = null;
         boolean result = false;
 
         try {
             connection = ConnectionManager.getConnection();
 
             if (id == null) {
                 preparedStatement = connection.prepareStatement(properties.getProperty("saveQuery"));
             } else {
                 preparedStatement = connection.prepareStatement(properties.getProperty("updateQuery"));
             }
 
             preparedStatement.setString(1, name);
             preparedStatement.setString(2, surname);
             preparedStatement.setString(3, login);
             preparedStatement.setString(4, password);
             if (currentShift != null) {
                 preparedStatement.setLong(5, currentShift.getID());
             } else {
                 preparedStatement.setNull(5, Types.INTEGER);
             }
             preparedStatement.setBoolean(6, suspended);
 
             result = (preparedStatement.executeUpdate() == 1);
         } catch (SQLException ex) {
             //TODO: log error
         } finally {
             if (connection != null) {
                 try {
                     connection.close();
                 } catch (SQLException ex) {
                     //Log error
                 }
             }
         }
 
         return result;
     }
 
     /**
      * Deletes matching record from the database. Provided that the selected
      * worker has not been saved to the database yet, only returns false.
      *
      * @return true when worker is successfuly deleted, false otherwise
      */
     @Override
     public boolean destroy() {
         if (id == null) {
             return false;
         }
 
         Connection connection = null;
         PreparedStatement preparedStatement;
         boolean result = false;
 
         try {
             connection = ConnectionManager.getConnection();
             preparedStatement = connection.prepareStatement(properties.getProperty("deleteQuery"));
             preparedStatement.setLong(1, id);
             result = (preparedStatement.executeUpdate() == 1);
         } catch (SQLException ex) {
             //TODO: log
         } finally {
             if (connection != null) {
                 try {
                     connection.close();
                 } catch (SQLException ex) {
                     //log exception
                 }
             }
         }
 
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null || getClass() != obj.getClass()) {
             return false;
         }
 
         final Worker other = (Worker) obj;
 
         if (login == null || other.login == null) {
             return false;
         }
 
         return this.login.equals(other.login);
     }
 
     @Override
     public int hashCode() {
         return (this.login != null ? this.login.hashCode() : 0);
     }
 }
