 package edacc.model;
 
 import edacc.EDACCApp;
 import edacc.properties.ManagePropertyController;
 import edacc.satinstances.DefaultPropertiesManager;
 import edacc.satinstances.PropertyValueTypeAlreadyExistsException;
 import edacc.satinstances.PropertyValueTypeManager;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.sql.*;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Properties;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
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
     private Integer modelVersion;
 
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
      * @param doCheckVersion whether to perform a database version check
      * @throws ClassNotFoundException if the driver couldn't be found.
      * @throws SQLException if an error occurs while trying to establish the connection.
      */
     public void connect(String hostname, int port, String username, String database, String password, boolean useSSL, boolean compress, int maxconnections, boolean doCheckVersion, boolean rewriteBatchStatements) throws ClassNotFoundException, SQLException, DBVersionException, DBVersionUnknownException, DBEmptyException {
         while (connections.size() > 0) {
             ThreadConnection tconn = connections.pop();
             tconn.conn.close();
         }
         if (watchDog != null) {
             watchDog.terminate();
         }
         try {
             this.isCompetitionDB = null;
             this.modelVersion = null;
             this.hostname = hostname;
             this.port = port;
             this.username = username;
             this.password = password;
             this.database = database;
             properties = new Properties();
             properties.put("user", username);
             properties.put("password", password);
             if (rewriteBatchStatements) {
                 properties.put("rewriteBatchedStatements", "true");
             }
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
             if (doCheckVersion) {
                 checkVersion();
             }
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
      * overloaded connect method with implicit version check, see connect() above.
      */
     public void connect(String hostname, int port, String username, String database, String password, boolean useSSL, boolean compress, int maxconnections) throws ClassNotFoundException, SQLException, DBVersionException, DBVersionUnknownException, DBEmptyException {
         connect(hostname, port, username, database, password, useSSL, compress, maxconnections, true, true);
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
         if (connections.isEmpty()) {
             throw new NoConnectionToDBException();
         }
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
                         // System.err.println(Thread.currentThread().getId() + " Found connection");
                         if (tconn.conn.isValid(10)) {
                             tconn.time = System.currentTimeMillis();
                             return tconn.conn;
                         }
                     }
                 }
                 // try to take a connection from a dead thread
                 for (ThreadConnection tconn : connections) {
                     if (tconn.thread == null || !tconn.thread.isAlive()) {
                         // System.err.println("Taking connection of dead thread");
                         tconn.thread = Thread.currentThread();
                         if (tconn.conn.isValid(10)) {
                             tconn.time = System.currentTimeMillis();
                             tconn.rollbackOperations.clear();
                             return tconn.conn;
                         }
                     }
                 }
                 // create new connection if max connection count isn't reached
                 if (connections.size() < maxconnections) {
                     Connection conn = getNewConnection();
                     // System.err.println("Creating new connection");
                     connections.add(new ThreadConnection(Thread.currentThread(), conn, System.currentTimeMillis()));
                     return conn;
                 }
                 // try to steal a connection from a living thread. It is safe to use
                 // connections where autoCommit is true (no data to commit/rollback)
                 // TODO: 500ms is too less?
                 for (ThreadConnection tconn : connections) {
                     if (tconn.conn.getAutoCommit() && System.currentTimeMillis() - tconn.time > 500) {
                         //   System.err.println("Stealing connection");
 
                         tconn.thread = Thread.currentThread();
                         if (tconn.conn.isValid(10)) {
                             tconn.rollbackOperations.clear();
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
 
     private ThreadConnection getThreadConnection(Thread thread) {
         for (ThreadConnection tconn : connections) {
             if (tconn.thread == thread) {
                 return tconn;
             }
         }
         return null;
     }
 
     public void addRollbackOperation(Runnable runnable) {
         synchronized (sync) {
             ThreadConnection tconn = getThreadConnection(Thread.currentThread());
             if (tconn == null) {
                 throw new IllegalArgumentException("Didn't find any connection for this thread");
             }
             tconn.rollbackOperations.add(runnable);
         }
     }
 
     public void rollback() throws SQLException {
         synchronized (sync) {
             ThreadConnection tconn = getThreadConnection(Thread.currentThread());
             if (tconn == null) {
                 throw new IllegalArgumentException("Didn't find any connection for this thread");
             }
             if (tconn.conn.getAutoCommit()) {
                 throw new IllegalArgumentException("Cannot rollback a connection with AUTOCOMMIT = true");
             }
             for (Runnable r : tconn.rollbackOperations) {
                 r.run();
             }
             tconn.rollbackOperations.clear();
             tconn.conn.rollback();
         }
     }
 
     public void commit() throws SQLException {
         synchronized (sync) {
             ThreadConnection tconn = getThreadConnection(Thread.currentThread());
             if (tconn == null) {
                 throw new IllegalArgumentException("Didn't find any connection for this thread");
             }
             if (tconn.conn.getAutoCommit()) {
                 throw new IllegalArgumentException("Cannot rollback a connection with AUTOCOMMIT = true");
             }
             tconn.rollbackOperations.clear();
             tconn.conn.commit();
         }
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
         List<Runnable> rollbackOperations;
 
         public ThreadConnection(Thread thread, Connection conn, long time) {
             this.thread = thread;
             this.conn = conn;
             this.time = time;
             rollbackOperations = new LinkedList<Runnable>();
         }
     }
 
     public static int getLocalModelVersion() {
         int cur_version = 0;
         while (EDACCApp.class.getClassLoader().getResource("edacc/resources/db_version/" + (cur_version + 1) + ".sql") != null) {
             cur_version++;
         }
         return cur_version;
     }
 
     private void checkVersion() throws DBVersionException, DBVersionUnknownException, DBEmptyException {
         int localVersion = getLocalModelVersion();
         try {
             int version = getModelVersion();
             if (version != localVersion) {
                 throw new DBVersionException(version, localVersion);
             } else {
                 return;
             }
         } catch (SQLException e) {
         }
         try {
             Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery("SHOW TABLES;");
             if (rs.next()) {
                 throw new DBVersionUnknownException(localVersion);
             }
         } catch (SQLException e) {
         }
         throw new DBEmptyException();
     }
 
     public void updateDBModel(Tasks task) throws Exception {
         task.setOperationName("Updating DB Model..");
         int localVersion = getLocalModelVersion();
         int currentVersion = 0;
         try {
             currentVersion = getModelVersion();
         } catch (SQLException e) {
             if (e.getErrorCode() != 1146) {
                 throw e;
             }
             // Error 1146: Table 'Version' doesn't exist
             // -> assuming version 0
         }
         boolean autoCommit = getConn().getAutoCommit();
         try {
             getConn().setAutoCommit(false);
             for (int version = currentVersion + 1; version <= localVersion; version++) {
                 task.setStatus("Updating to version " + version);
                 // task.setTaskProgress((version - currentVersion) / (float) (localVersion - currentVersion + 1));
                 InputStream in = EDACCApp.class.getClassLoader().getResourceAsStream("edacc/resources/db_version/" + version + ".sql");
                 if (in == null) {
                     throw new SQLQueryFileNotFoundException();
                 }
                 executeSqlScript(task, in);
 
                 Statement st = getConn().createStatement();
                 st.executeUpdate("INSERT INTO `Version` VALUES (" + version + ", NOW())");
                 st.close();
             }
         } catch (Exception e) {
             getConn().rollback();
             throw e;
         } finally {
             getConn().setAutoCommit(autoCommit);
         }
     }
 
     /**
      * Creates the correct DB schema for EDACC using an already established connection.
      */
     public void createDBSchema(Tasks task) throws NoConnectionToDBException, SQLException, IOException {
         task.setOperationName("Database");
         task.setStatus("Generating tables");
 
         InputStream in = EDACCApp.class.getClassLoader().getResourceAsStream("edacc/resources/edacc.sql");
         if (in == null) {
             throw new SQLQueryFileNotFoundException();
         }
         executeSqlScript(task, in);
         Statement st = getConn().createStatement();
         st.executeUpdate("INSERT INTO `Version` VALUES (" + getLocalModelVersion() + ", NOW())");
         st.close();
 
         task.setStatus("Adding default property value types");
         try {
             PropertyValueTypeManager.getInstance().addDefaultToDB();
         } catch (PropertyValueTypeAlreadyExistsException ex) {
             Logger.getLogger(DatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         task.setStatus("Adding default satpc-properties and computation methods");
         try {
                DefaultPropertiesManager.getInstance().addDefaultToDB();

         } catch (ComputationMethodAlreadyExistsException ex) {
             Logger.getLogger(DatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
         } catch (NoComputationMethodBinarySpecifiedException ex) {
             Logger.getLogger(DatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(DatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
         } catch (ComputationMethodSameNameAlreadyExists ex) {
             Logger.getLogger(DatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
         } catch (ComputationMethodSameMD5AlreadyExists ex) {
             Logger.getLogger(DatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        }             catch (PropertyIsUsedException ex) {
                Logger.getLogger(DatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
            } catch (PropertyTypeDoesNotExistException ex) {
                Logger.getLogger(DatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
     }
 
     public void executeSqlScript(Tasks task, InputStream in) throws IOException, SQLException {
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
             int current = 0;
             for (String q : queries) {
                 task.setTaskProgress((float) ++current / (float) (queries.size()));
                 try {
                     st.execute(q);
                 } catch (SQLException ex) {
                     System.err.println("QUERY:\n" + q + "\nfailed");
                     ex.printStackTrace();
                 }
             }
             st.close();
             task.setTaskProgress(0.f);
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
             rs.close();
             ps.close();
             return isCompetitionDB;
         }
         rs.close();
         ps.close();
         return false;
     }
 
     public int getModelVersion() throws SQLException {
         if (modelVersion != null) {
             return modelVersion;
         }
         Integer version = null;
         Statement st = getConn().createStatement();
         ResultSet rs = st.executeQuery("SELECT MAX(`version`) FROM `Version`");
         if (rs.next()) {
             version = rs.getInt(1);
         }
         rs.close();
         st.close();
         modelVersion = version;
         return modelVersion;
     }
 
 }
