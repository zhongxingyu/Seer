 package edacc.model;
 
 import edacc.EDACCApp;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.sql.*;
 import java.util.LinkedList;
 import java.util.Observable;
 import java.util.Properties;
 import java.util.Vector;
 
 /**
  * singleton class handling the database connection.
  * It is possible to get a notification of a change of the connection state by adding an Observer to this class.
  * @author daniel
  */
 public class DatabaseConnector extends Observable {
     // time after an idling connection is closed
     public static final int CONNECTION_TIMEOUT = 60000;
     
     private static DatabaseConnector instance = null;
     private int maxconnections;
     private LinkedList<ThreadConnection> connections;
     private String hostname;
     private int port;
     private String database;
     private String username;
     private String password;
     private Properties properties;
     private final Object sync = new Object();
     private ConnectionWatchDog watchDog;
     private Boolean isCompetitionDB;
 
     private DatabaseConnector() {
         connections = new LinkedList<ThreadConnection>();
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
     public void connect(String hostname, int port, String username, String database, String password, boolean useSSL, boolean compress, int maxconnections) throws ClassNotFoundException, SQLException {
         while (connections.size() > 0) {
             ThreadConnection tconn = connections.pop();
             tconn.conn.close();
         }
         if (watchDog != null) {
             watchDog.terminate();
         }
         try {
             this.isCompetitionDB = null;
             this.hostname = hostname;
             this.port = port;
             this.username = username;
             this.password = password;
             this.database = database;
             properties = new Properties();
             properties.put("user", username);
             properties.put("password", password);
             properties.put("rewriteBatchedStatements", "true");
             //properties.put("profileSQL", "true");
             //properties.put("traceProtocol", "true");
             //properties.put("logger", "edacc.model.MysqlLogger");
             //properties.put("useUnbufferedInput", "false");
             //properties.put("useServerPrepStmts", "true");
             if (useSSL) {
                 properties.put("useSSL", "true");
                 properties.put("requireSSL", "true");
             }
             if (compress) {
                 properties.put("useCompression", "true");
             }
             /*java.io.PrintWriter w =
                     new java.io.PrintWriter(new java.io.OutputStreamWriter(System.out));
             DriverManager.setLogWriter(w);*/
 
             Class.forName("com.mysql.jdbc.Driver");
             this.maxconnections = maxconnections;
             watchDog = new ConnectionWatchDog();
             connections.add(new ThreadConnection(Thread.currentThread(), getNewConnection(), System.currentTimeMillis()));
             watchDog.start();
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
 
     private Connection getNewConnection() throws SQLException {
         return DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database, properties);
     }
 
     public int getMaxconnections() {
         return maxconnections;
     }
 
     /**
      * Closes an existing connection. If no connection exists, this method does nothing.
      * @throws SQLException if an error occurs while trying to close the connection.
      */
     public void disconnect() {
         watchDog.terminate();
         synchronized (sync) {
             if (!connections.isEmpty()) {
                 while (connections.size() > 0) {
                     ThreadConnection tconn = connections.pop();
                     try {
                         tconn.conn.rollback();
                         tconn.conn.close();
                     } catch (SQLException e) {
                     }
                 }
             }
         }
         this.setChanged();
         this.notifyObservers("disconnect");
     }
 
     public void releaseConnection() {
         synchronized (sync) {
             for (ThreadConnection tconn : connections) {
                 if (tconn.thread == Thread.currentThread()) {
                     tconn.thread = null;
                     tconn.time = System.currentTimeMillis();
                     break;
                 }
             }
         }
     }
 
     public int freeConnectionCount() {
         int res;
         synchronized (sync) {
             res = maxconnections - connections.size();
             for (ThreadConnection tconn : connections) {
                 if (tconn.thread == null || !tconn.thread.isAlive()) {
                     res++;
                 }
             }
         }
         return res;
     }
 
     public Connection getConn() throws SQLException {
         return getConn(0);
     }
 
     private Connection getConn(int retryCount) throws SQLException {
         if (retryCount > 5) {
             throw new SQLException("No connections available.");
         }
         if (!isConnected()) {
             // inform Obeservers of lost connection
             this.setChanged();
             this.notifyObservers();
             throw new NoConnectionToDBException();
         }
         try {
             synchronized (sync) {
                 // try to find the connection of this thread: every thread can only have one connection at a time
                 for (ThreadConnection tconn : connections) {
                     if (tconn.thread == Thread.currentThread()) {
                         if (tconn.conn.isValid(10)) {
                             tconn.time = System.currentTimeMillis();
                             return tconn.conn;
                         }
                     }
                 }
                 // try to take a connection from a dead thread
                 for (ThreadConnection tconn : connections) {
                     if (tconn.thread == null || !tconn.thread.isAlive()) {
                         tconn.thread = Thread.currentThread();
                         if (tconn.conn.isValid(10)) {
                             tconn.time = System.currentTimeMillis();
                             return tconn.conn;
                         }
                     }
                 }
                 // create new connection if max connection count isn't reached
                 if (connections.size() < maxconnections) {
                     Connection conn = getNewConnection();
                     connections.add(new ThreadConnection(Thread.currentThread(), conn, System.currentTimeMillis()));
                     return conn;
                 }
                 // try to steal a connection from a living thread. It is safe to use
                 // connections where autoCommit is true (no data to commit/rollback)
                 for (ThreadConnection tconn : connections) {
                     if (tconn.conn.getAutoCommit() && System.currentTimeMillis() - tconn.time > 500) {
                         tconn.thread = Thread.currentThread();
                         if (tconn.conn.isValid(10)) {
                             return tconn.conn;
                         }
                     }
                 }
             }
         } catch (SQLException e) {
             this.disconnect();
             throw new NoConnectionToDBException();
         }
         // didn't find any connection and maximum connection count is reached:
         // wait 1 sec and try again.
         try {
             Thread.sleep(1000);
         } catch (InterruptedException ex) {
             throw new SQLException("No connections available.");
         }
         return getConn(retryCount + 1);
     }
 
     /**
      *
      * @return if a valid connection exists.
      */
     public boolean isConnected() {
         synchronized (sync) {
             return connections.size() > 0;
         }
     }
 
     private class ConnectionWatchDog extends Thread {
 
         private boolean terminated = false;
 
         public void terminate() {
             this.terminated = true;
         }
 
         @Override
         public void run() {
             terminated = true;
             while (!terminated) {
                 synchronized (DatabaseConnector.this.sync) {
                     for (int i = connections.size() - 1; i >= 0; i--) {
                         ThreadConnection tconn = connections.get(i);
                         if (tconn.thread == null) {
                             if (System.currentTimeMillis() - tconn.time > CONNECTION_TIMEOUT) {
                                 try {
                                     tconn.conn.close();
                                     System.out.println("CLOSED CONNECTION!");
                                 } catch (SQLException e) {
                                 }
                                 connections.remove(i);
                             }
                         } else if (!tconn.thread.isAlive()) {
                             tconn.thread = null;
                             tconn.time = System.currentTimeMillis();
                         }
                     }
                 }
                 try {
                     Thread.sleep(1000);
                 } catch (InterruptedException e) {
                     break;
                 }
             }
         }
     }
 
     private class ThreadConnection {
 
         Thread thread;
         Connection conn;
         long time;
 
         public ThreadConnection(Thread thread, Connection conn, long time) {
             this.thread = thread;
             this.conn = conn;
             this.time = time;
         }
     }
 
     /**
      * Creates the correct DB schema for EDACC using an already established connection.
      */
     public void createDBSchema() throws NoConnectionToDBException, SQLException, IOException {
 
         InputStream in = EDACCApp.class.getClassLoader().getResourceAsStream("edacc/resources/edacc.sql");
         if (in == null) {
             throw new SQLQueryFileNotFoundException();
         }
         BufferedReader br = new BufferedReader(new InputStreamReader(in));
         String line;
         String text = "";
         String l;
         while ((line = br.readLine()) != null) {
             if (!(l = line.replaceAll("\\s", "")).isEmpty() && !l.startsWith("--")) {
                 text += line + " ";
             }
         }
         in.close();
         Vector<String> queries = new Vector<String>();
         String query = "";
         String delimiter = ";";
         int i = 0;
         while (i < text.length()) {
             if (text.toLowerCase().startsWith("delimiter", i)) {
                 i += 10;
                 delimiter = text.substring(i, text.indexOf(' ', i));
                 i = text.indexOf(' ', i);
             } else if (text.startsWith(delimiter, i)) {
                 queries.add(query);
                 i += delimiter.length();
                 query = "";
             } else {
                 query += text.charAt(i);
                 i++;
             }
         }
         if (!query.replaceAll(" ", "").equals("")) {
             queries.add(query);
         }
         boolean autoCommit = getConn().getAutoCommit();
         try {
             getConn().setAutoCommit(false);
             Statement st = getConn().createStatement();
             for (String q : queries) {
                 st.execute(q);
             }
             st.close();
             getConn().commit();
         } catch (SQLException e) {
             getConn().rollback();
             throw e;
         } finally {
             getConn().setAutoCommit(autoCommit);
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
 
     /**
      * Returns whether the database is a competition database
      * @return
      * @throws NoConnectionToDBException
      * @throws SQLException
      */
     public boolean isCompetitionDB() throws NoConnectionToDBException, SQLException {
         if (isCompetitionDB != null) {
             return isCompetitionDB;
         }
         PreparedStatement ps = getConn().prepareStatement("SELECT competition FROM DBConfiguration");
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
             isCompetitionDB = rs.getBoolean("competition");
             return isCompetitionDB;
         }
         return false;
     }
 }
