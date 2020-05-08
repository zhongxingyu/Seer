 package cz.muni.fi.pv168.clockcard;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Properties;
 
 /**
  * Class that represents worker who is using the ClockCard system.
  *
  * @author Marek Osvald
  */
 
 public class Worker {
     private static final Properties properties = loadProperties();
     private static boolean testingMode = false;
 
     private Long id;
     private String name;
     private String surname;
     private String login;
     private String password;
     private Shift currentShift = null;
     private boolean suspended = false;
 
     /**
      * Returns connection to the production or testing database.
      *
      * @return connection to the database.
      */
     
 
     
     /**
      * Loads properties from file.
      *
      * @return properties from the file
      */
     public static Properties loadProperties() {
         try {
             FileInputStream inputStream = new FileInputStream("src/Worker.properties");
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
      * Turns on the testin mode.
      */
     public static void testingOn() {
         testingMode = true;
     }
     /**
      * Turns off the tesing mode.
      */
     public static void testingOff() {
         testingMode = false;
     }
 
     /**
      * Returns lists of all workers in the database.
      *
      * @return list of all workers in the database.
      */
     public static List<Worker> all() throws SQLException {
         Connection connection = ConnectionManager.getConnection();
         Statement statement = connection.createStatement();
         ResultSet resultSet = statement.executeQuery("SELECT * FROM APP.workers");
 
         ArrayList<Worker> result = new ArrayList<Worker>();
         while (resultSet.next()) {
             Worker worker = new Worker(resultSet.getLong(1),
                                        resultSet.getString(2),
                                        resultSet.getString(3),
                                        resultSet.getString(4),
                                        resultSet.getString(5),
                                        resultSet.getLong(6),
                                        resultSet.getBoolean(7));
             result.add(worker);
         }
         connection.close();
         return Collections.unmodifiableList(result);
     }
     /**
      * Returns worker with the selected ID from the database or null if the
      * worker is not found.
      * 
      * @param id ID of the worker to be found
      * @return worker with the selected ID from the database or null if the
      * worker is not found
      */
     public static Worker find(long id) throws SQLException {
         Connection connection = ConnectionManager.getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM APP.workers WHERE id=?");
         preparedStatement.setLong(1, id);
         ResultSet resultSet = preparedStatement.executeQuery();
 
         Worker worker = null;
 
         if (resultSet.getFetchSize() == 1 && resultSet.next()) {
             worker = new Worker(resultSet.getLong(1),
                                 resultSet.getString(2),
                                 resultSet.getString(3),
                                 resultSet.getString(4),
                                 resultSet.getString(5),
                                 resultSet.getLong(6),
                                 resultSet.getBoolean(7));
         }
         connection.close();
         return worker;
     }
     /**
      * Returns total number of workers in the database.
      *
      * @return total number of workers in the database
      */
     public static int count() throws SQLException {
         Connection connection = ConnectionManager.getConnection();
         Statement statement = connection.createStatement();
         ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM APP.workers");
         
         int result = 0;
         while (resultSet.next()) {
             result = resultSet.getInt(1);
         }
         connection.close();
         return result;
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
         this.currentShift = Shift.find(currentShift);
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
 
         currentShift = new Shift();
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
         return Shift.findByWorker(id);
     }
 
     /**
      * Saves worker into the database. Executes INSERT when worker is saved for
      * the first time, UPDATE otherwise.
      *
      * @return true when worker is successfuly saved, false otherwise
      * @throws SQLException
      */
     public boolean save() throws SQLException {
         Connection connection = ConnectionManager.getConnection();
         PreparedStatement preparedStatement = null;
 
         if (id == null) {
             preparedStatement = connection.prepareStatement("INSERT INTO APP.workers (name, surname, login, password, current_shift, suspended) VALUES (?, ?, ?, ?, ?, ?)");
         } else {
             preparedStatement = connection.prepareStatement("UPDATE APP.workers SET name=?, surname=?, login=?, password=?, current_shift=?, suspended=? WHERE id=" + id);
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
         int result = preparedStatement.executeUpdate();
         connection.close();
 
         return (result > 0);
     }
     /**
      * Deletes matching record from the database. Provided that the selected
      * worker has not been saved to the database yet, only returns false.
      *
      * @return true when worker is successfuly deleted, false otherwise
      */
     public boolean destroy() throws SQLException {
         if (id == null) {
             return false;
         }
 
         Connection connection = ConnectionManager.getConnection();
         Statement statement = connection.createStatement();
         int result = statement.executeUpdate("DELETE FROM APP.workers WHERE id=" + id);
         connection.close();
 
         return (result == 1);
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
