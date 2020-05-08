 /*
  * Team : AGF AM / OSI / SI / BO
  *
  * Copyright (c) 2001 AGF Asset Management.
  */
 package net.codjo.sql.server;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Timer;
 import java.util.TimerTask;
 import org.apache.log4j.Logger;
 /**
  * Manager of SQL connection.
  *
  * <p> Spool database connection in order to speed up process. A ConnectionPool is specific for one database and one
  * user. The Pool contains 2 kinds of list: one for all connections built by this manager, and another for unused
  * connection. </p>
  *
  * <p> This class is multi-thread safe. </p>
  */
 public class ConnectionPool {
     private static final Logger LOG = Logger.getLogger(ConnectionPool.class.getName());
     private final List<Connection> allConnections = new ArrayList<Connection>(4);
     private final List<Connection> unusedConnections = new ArrayList<Connection>(4);
     private final Map<Connection, Long> lastReleasedTimes = new HashMap<Connection, Long>(4);
     private final ConnectionPoolConfiguration configuration;
     private Timer timer;
     private boolean hasBeenShutdown = false;
 
 
     /**
      * Constructor for the ConnectionPool object.
      *
      * @param classDriver Class driver name (ex: com.sybase.jdbc2.jdbc.SybDriver)
      * @param url         Url of the Database
      * @param catalog     Catalog de la base (optionnal)
      * @param props       Propriete du driver
      *
      * @throws ClassNotFoundException Si le driver n'est pas trouve.
      */
     @Deprecated
     protected ConnectionPool(String classDriver, String url, String catalog, Properties props)
           throws ClassNotFoundException {
         this(new ConnectionPoolConfiguration(classDriver, url, catalog, props, false));
     }
 
 
     protected ConnectionPool(ConnectionPoolConfiguration configuration) {
         this.configuration = configuration;
         if (this.configuration.isAutomaticClose()) {
             initTimer();
         }
     }
 
 
     /**
      * Ferme toutes les connections du pool.
      */
     public synchronized void closeAllConnections() {
         List<Connection> copy = new ArrayList<Connection>(allConnections);
         for (Connection connection : copy) {
             closeConnection(connection);
         }
     }
 
 
     /**
      * Retourne Le catalogue utilis par ce ConnectionPool.
      *
      * @return nom de catalog
      *
      * @deprecated utiliser mypool.getConfiguration().getCatalog()
      */
     @Deprecated
     public String getCatalog() {
         return configuration.getCatalog();
     }
 
 
     /**
      * Retourne Le driver JDBC utilis par ce ConnectionPool.
      *
      * @return nom de classe
      *
      * @deprecated utiliser mypool.getConfiguration().getClassDriver()
      */
     @Deprecated
     public String getClassDriver() {
         return configuration.getClassDriver();
     }
 
 
     /**
      * Gets one valid connection to the database.
      *
      * @return a connection object to the database.
      *
      * @throws SQLException Description of Exception
      */
     public synchronized Connection getConnection() throws SQLException {
         if (unusedConnections.isEmpty()) {
             addNewConnection();
         }
         Connection connection = unusedConnections.remove(0);
         lastReleasedTimes.remove(connection);
         return connection;
     }
 
 
     /**
      * Retourne Les proprits utilis par ce ConnectionPool pour crer des connections.
      *
      * @return properties
      *
      * @deprecated utiliser mypool.getConfiguration().getProperties()
      */
     @Deprecated
     public Properties getDbProps() {
         return configuration.getProperties();
     }
 
 
     /**
      * Retourne L'url de la BD utilis par ce ConnectionPool.
      *
      * @return addresse de la BD
      *
      * @deprecated utiliser mypool.getConfiguration().getUrl()
      */
     @Deprecated
     public String getDbUrl() {
         return configuration.getUrl();
     }
 
 
     /**
      * Release a connection (previously given by this manager), and close the given statement.
      *
      * @param connection a connection
      * @param statement  a statement
      *
      * @throws SQLException DB access error
      */
     public synchronized void releaseConnection(Connection connection, Statement statement)
           throws SQLException {
         try {
             if (statement != null) {
                 statement.close();
             }
         }
         finally {
             releaseConnection(connection);
         }
     }
 
 
     /**
      * Release a connection (previously given by this manager).
      *
      * @param connection a connection
      *
      * @throws SQLException             DB access error
      * @throws IllegalArgumentException La connection ne fait pas parti du pool.
      */
     public synchronized void releaseConnection(Connection connection) throws SQLException {
         ensurePoolNotShutDown();
         if (connection == null) {
             return;
         }
         if (!allConnections.contains(connection)) {
             throw new IllegalArgumentException(
                   "Cette connexion n'appartient pas  ce pool !");
         }
         if (connection.isClosed()) {
             removeConnection(connection);
             return;
         }
         if (unusedConnections.contains(connection)) {
             return;
         }
 
         if (!connection.getAutoCommit()) {
             connection.rollback();
             connection.setAutoCommit(true);
         }
         connection.clearWarnings();
 
         lastReleasedTimes.put(connection, System.currentTimeMillis());
         unusedConnections.add(connection);
     }
 
 
     @Override
     public String toString() {
         return "ConnectionPool(total=" + allConnections.size() + ", unused="
                + unusedConnections.size() + ")";
     }
 
 
     /**
      * Retourne l'attribut allConnectionsSize de ConnectionPool
      *
      * @return La valeur de allConnectionsSize
      */
     public int getAllConnectionsSize() {
         return allConnections.size();
     }
 
 
     public int getUnusedConnectionsSize() {
         return unusedConnections.size();
     }
 
 
     /**
      * Ferme toutes les connexions et arrte le timer de fermeture des connexions
      */
     public synchronized void shutdown() {
         if (hasBeenShutdown) {
             return;
         }
         hasBeenShutdown = true;
         closeAllConnections();
         if (timer != null) {
             timer.cancel();
             timer = null;
         }
     }
 
 
     public boolean hasBeenShutdown() {
         return hasBeenShutdown;
     }
 
 
     public ConnectionPoolConfiguration getConfiguration() {
         return configuration;
     }
 
 
     /**
      * Adds a feature to the NewConnection attribute of the ConnectionPool object
      *
      * @throws SQLException         Erreur SQL
      * @throws NullPointerException Connection non disponible
      */
     private void addNewConnection() throws SQLException {
         ensurePoolNotShutDown();
 
         String driver = configuration.getClassDriver().toLowerCase();
 
         // TODO : Hack pour grer le cas du format des dates Oracle => upgrade du driver JDBC oracle, les dates
         // TODO   sont condidres comme des Timestamp
         if (driver.contains("oracle")) {
             configuration.getProperties().put("oracle.jdbc.mapDateToTimestamp", "false");
         }
         // TODO fin
 
         Connection con = DriverManager.getConnection(configuration.getUrl(), configuration.getProperties());
         if (con == null) {
             throw new NullPointerException("DriverManager retourne une connection null");
         }
 
         if (configuration.getCatalog() != null) {
             con.setCatalog(configuration.getCatalog());
         }
 
         // TODO : Hack pour grer le cas du format des dates Oracle =>  supprimer lors du chantier multibases
         if (driver.contains("oracle")) {
             Statement statement = null;
             try {
                 statement = con.createStatement();
                 statement.execute("ALTER SESSION SET NLS_DATE_FORMAT = 'YYYY-MM-DD'");
                 statement.execute("ALTER SESSION SET CURRENT_SCHEMA=" + configuration.getCatalog());
             }
             finally {
                 if (statement != null) {
                     statement.close();
                 }
             }
         }
         // TODO fin
         allConnections.add(con);
         unusedConnections.add(con);
     }
 
 
     private void ensurePoolNotShutDown() throws SQLException {
         if (hasBeenShutdown()) {
             throw new SQLException("Pool has been shut down !");
         }
     }
 
 
     /**
      * Renseigne un nouveau dlai d'attente avant de fermer une connection non utilise.
      *
      * @param delay Le dlai (en millisecondes).
      */
     void setIdleConnectionTimeout(long delay) {
         configuration.setIdleConnectionTimeout(delay);
         if (timer != null) {
             timer.cancel();
             initTimer();
         }
     }
 
 
     private void closeConnection(Connection con) {
         try {
             // Attention : test sur isClosed() enlev pour viter d'attendre que le traitement SQL se termine
             //             dans la BD.
             con.close();
         }
         catch (SQLException ex) {
             LOG.warn("Unable to close a connection ! ", ex);
         }
         removeConnection(con);
     }
 
 
     private void removeConnection(Connection connection) {
         allConnections.remove(connection);
         unusedConnections.remove(connection);
         lastReleasedTimes.remove(connection);
     }
 
 
     /**
      * Ferme la connection non utilise aprs le closeDelay.
      */
     public synchronized void closeOldConnection() {
         if (unusedConnections.isEmpty()) {
             return;
         }
         long now = System.currentTimeMillis();
 
         for (Iterator<Connection> iter = unusedConnections.iterator(); iter.hasNext(); ) {
             Connection connection = iter.next();
             if (now >= lastReleasedTimes.get(connection) + configuration.getIdleConnectionTimeout()) {
                 iter.remove();
                 closeConnection(connection);
             }
         }
     }
 
 
     /**
      * Initialise le Timer permettant de fermer une connection non utilise au bout du closeDelay.
      */
     private void initTimer() {
         timer = new Timer(true);
         timer.schedule(new TimerTask() {
             @Override
             public void run() {
                 closeOldConnection();
             }
         }, 1, configuration.getIdleConnectionTimeout());
     }
 
 
     public void fillPool(int size) throws SQLException {
         for (int i = 0; i < size; i++) {
             addNewConnection();
         }
     }
 }
