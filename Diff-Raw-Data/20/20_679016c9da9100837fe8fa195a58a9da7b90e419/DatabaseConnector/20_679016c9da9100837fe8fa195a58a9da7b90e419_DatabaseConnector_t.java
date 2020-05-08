 package edacc.model;
 
 import edacc.EDACCApp;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.sql.*;
 import java.util.Observable;
 import java.util.Vector;
 
 /**
  * singleton class handling the database connection.
  * It is possible to get a notification of a change of the connection state by adding an Observer to this class.
  * @author daniel
  */
 public class DatabaseConnector extends Observable {
 
     private static DatabaseConnector instance = null;
     private Connection conn;
     private String hostname;
     private int port;
     private String database;
     private String username;
     private String password;
 
     private DatabaseConnector() {
     }
 
     public static DatabaseConnector getInstance() {
         if (instance == null) {
             instance = new DatabaseConnector();
         }
         return instance;
     }
 
     /**
      * Creates a connection to a specified DB.
      * @param hostname the hostname of the DB server.
      * @param port the port of the DB server.
      * @param username the username of the DB user.
      * @param database the name of the database containing the EDACC tables.
      * @param password the password of the DB user.
      * @throws ClassNotFoundException if the driver couldn't be found.
      * @throws SQLException if an error occurs while trying to establish the connection.
      */
     public void connect(String hostname, int port, String username, String database, String password) throws ClassNotFoundException, SQLException {
         if (conn != null) {
             conn.close();
         }
         try {
             this.hostname = hostname;
             this.port = port;
             this.username = username;
             this.password = password;
             this.database = database;
             Class.forName("com.mysql.jdbc.Driver");
             conn = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database + "?user=" + username + "&password=" + password);
         } catch (ClassNotFoundException e) {
             throw e;
         } catch (SQLException e) {
             throw e;
         } finally {
             // inform Observers of changed connection state
             this.setChanged();
             this.notifyObservers();
         }
     }
 
     /**
      * Closes an existing connection. If no connection exists, this method does nothing.
      * @throws SQLException if an error occurs while trying to close the connection.
      */
     public void disconnect() throws SQLException {
         if (conn != null) {
             conn.close();
             this.setChanged();
            this.notifyObservers(new String("disconnect"));
         }
     }
 
     public Connection getConn() throws NoConnectionToDBException {
         try {
             if (!isConnected()) {
                 // inform Obeservers of lost connection
                 this.setChanged();
                 this.notifyObservers();
                 throw new NoConnectionToDBException();
             }
             return conn;
         } catch (SQLException e) {
             conn = null;
             throw new NoConnectionToDBException();
         }
     }
 
     /**
      *
      * @return if a valid connection exists.
      */
     public boolean isConnected() {
         try {
             return conn != null && conn.isValid(10);
         } catch (SQLException ex) {
             return false;
         }
     }
 
     /**
      * Creates the correct DB schema for EDACC using an already established connection.
      */
     public void createDBSchema() throws NoConnectionToDBException, SQLException, IOException {
         InputStream in = EDACCApp.class.getClassLoader().getResourceAsStream("edacc/resources/edacc.sql");
         if (in == null)
             throw new SQLQueryFileNotFoundException();
         BufferedReader br = new BufferedReader(new InputStreamReader(in));
         String line;
         String text = "";
         String l;
         while ((line = br.readLine()) != null) 
             if (!(l = line.replaceAll("\\s", "")).isEmpty() && !l.startsWith("--"))
                 text += line + " ";
         in.close();
         Vector<String> queries = new Vector<String>();
         String query = "";
         String delimiter = ";";
         int i = 0;
         while (i < text.length()) {
             if (text.startsWith(delimiter,i)) {
                 queries.add(query);
                 i += delimiter.length();
                 query = "";
             } else if (text.startsWith("delimiter",i)) {
                 i += 10;
                 delimiter = text.substring(i, text.indexOf(' ', i));
                 i = text.indexOf(' ', i);
             } else {
                 query += text.charAt(i);
                 i++;
             }
         }
         if (!query.equals("")) {
             queries.add(query);
         }
 
         Statement st = getConn().createStatement();
         for (String q : queries)
             if (!q.replaceAll("\\s", "").isEmpty())
                 st.addBatch(q);
         st.executeBatch();
         st.close();
         try {
             getConn().setAutoCommit(false);
             getConn().commit();
         } finally {
             getConn().setAutoCommit(true);
         }
     }
 
     public String getDatabase() {
         return database;
     }
 
     public String getHostname() {
         return hostname;
     }
 
     public String getPassword() {
         return password;
     }
 
     public int getPort() {
         return port;
     }
 
     public String getUsername() {
         return username;
     }
 
 }
