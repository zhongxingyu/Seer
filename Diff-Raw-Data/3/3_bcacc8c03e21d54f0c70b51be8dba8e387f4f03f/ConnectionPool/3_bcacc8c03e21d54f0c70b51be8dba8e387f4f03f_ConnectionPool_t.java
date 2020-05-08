 package org.narwhal.pool;
 
 import org.narwhal.core.DatabaseConnection;
 import org.narwhal.core.DatabaseInformation;
 import org.narwhal.query.QueryCreator;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 
 /**
  * The <code>ConnectionPool</code> class implements
  * basic functionality that allows end-users persist
  * variable number of database connection.
  * This class provides database connection pool and
  * takes care of lifetime and resource management.
  *
  * @author Miron Aseev
  */
 public class ConnectionPool {
 
     private static final int DEFAULT_POOL_SIZE = 5;
     private static final int DEFAULT_ACQUIRE_INCREMENT = 5;
     private int size;
     private int acquireIncrement;
     private DatabaseInformation databaseInformation;
     private Lock connectionsLock;
     private Lock variableLock;
     private List<DatabaseConnection> connections;
     private QueryCreator queryCreator;
 
 
     /**
      * Initializes a new instance of the ConnectionPool class.
      * The instance is specified by DatabaseInformation instance that
      * keeps all the information to be able to make connection to the database.
      * Default pool size is 5.
      * Acquire increment is 5.
      *
      * @param databaseInformation instance of {@code DatabaseInformation} class that includes
      *                            all the information for making connection to the database.
      * @throws SQLException If any database access problems happened.
      * @throws ClassNotFoundException If there's any problem with finding a jdbc driver class.
      * */
     public ConnectionPool(DatabaseInformation databaseInformation, QueryCreator queryCreator) throws SQLException, ClassNotFoundException {
         this(databaseInformation, DEFAULT_POOL_SIZE, DEFAULT_ACQUIRE_INCREMENT, queryCreator);
     }
 
     /**
      * Initializes a new instance of the ConnectionPool class.
      * The instance is specified by DatabaseInformation instance that
      * keeps all the information to make connection to the database.
      * Instance is also specified by the size and acquireIncrement variable.
      *
      * @param databaseInformation instance of {@code DatabaseInformation} class that includes
      *                            all the information for making connection to the database.
      * @param size the size of the pool.
      * @param acquireIncrement the acquire increment of the pool.
      * @throws SQLException If any database access problems happened.
      * @throws ClassNotFoundException If there's any problem with finding a jdbc driver class.
      * */
     public ConnectionPool(DatabaseInformation databaseInformation, int size,
                           int acquireIncrement, QueryCreator queryCreator) throws SQLException, ClassNotFoundException {
         if (size < 1) {
             throw new IllegalArgumentException("Argument value must be equal or greater than 1: " + size);
         }
 
         if (acquireIncrement < 1) {
             throw new IllegalArgumentException("Argument value must be equal or greater than 1: " + acquireIncrement);
         }
 
         this.databaseInformation = databaseInformation;
         this.size = size;
         this.acquireIncrement = acquireIncrement;
         this.queryCreator = queryCreator;
 
         connectionsLock = new ReentrantLock();
         variableLock = new ReentrantLock();
         connections = createDatabaseConnections(size, queryCreator);
     }
 
     /**
      * Returns DatabaseConnection object from the pool.
      * This method removes connection from the pool collection.
      * After working with DatabaseConnection object you should
      * return connection to the pool by invoking returnConnection() method.
      *
      * @return DatabaseConnection object.
      * @throws SQLException If any database access problems happened.
      * @throws ClassNotFoundException If there's any problem with finding a jdbc driver class.
      * */
     public DatabaseConnection getConnection() throws SQLException, ClassNotFoundException {
         connectionsLock.lock();
         try {
             if (connections.isEmpty()) {
                 connections.addAll(createDatabaseConnections(getAcquireIncrement(), queryCreator));
 
                 variableLock.lock();
                 try {
                     size += acquireIncrement;
                 } finally {
                     variableLock.unlock();
                 }
             }
 
             return connections.remove(connections.size() - 1);
         } finally {
             connectionsLock.unlock();
         }
     }
 
     /**
      * Returns connection to the pool.
      *
      * @param connection Database connection which is going to be added to the pool.
      * */
     public void returnConnection(DatabaseConnection connection) {
         connectionsLock.lock();
         try {
             if (connections.size() == getSize()) {
                 throw new IllegalArgumentException("Pool is full");
             }
 
             connections.add(connection);
         } finally {
             connectionsLock.unlock();
         }
     }
 
     /**
      * Closes all the database connections that is waiting in the pool.
      *
      * @throws SQLException If any database access problems happened.
      * */
     public void close() throws SQLException {
         connectionsLock.lock();
         try {
             for (DatabaseConnection connection : connections) {
                 if (!connection.isClosed()) {
                     connection.close();
                 }
             }
 
             connections.clear();
         } finally {
             connectionsLock.unlock();
         }
     }
 
     /**
      * Returns size of the pool.
      *
      * @return Size of the pool.
      * */
     public int getSize() {
         variableLock.lock();
         try {
             return size;
         } finally {
             variableLock.unlock();
         }
     }
 
     /**
      * Sets new size of the pool. If the new size of the pool is smaller than initial size,
      * then pool will be condensed to the new size. Unnecessary connections will be closed.
     * @param newSize The new pool's size.
      * @throws SQLException If any database access problems happened.
      * @throws ClassNotFoundException If there's any problem with finding a jdbc driver class.
      * */
     public void setSize(int newSize) throws SQLException, ClassNotFoundException {
         if (newSize < 1) {
             throw new IllegalArgumentException("Argument value must be equal or greater than 1: " + newSize);
         }
 
         connectionsLock.lock();
         try {
             variableLock.lock();
             try {
                 int numberOfConnections = Math.abs(newSize - size);
 
                 if (newSize > size) {
                     connections.addAll(createDatabaseConnections(numberOfConnections, queryCreator));
                 } else {
                     while (numberOfConnections > 0 && !connections.isEmpty()) {
                         connections.remove(connections.size() - 1).close();
                         numberOfConnections--;
                     }
                 }
 
                 this.size = newSize;
             } finally {
                 variableLock.unlock();
             }
         } finally {
             connectionsLock.unlock();
         }
     }
 
     /**
      * Returns acquire increment value.
      *
      * @return Acquire increment value.
      * */
     public int getAcquireIncrement() {
         variableLock.lock();
         try {
             return acquireIncrement;
         } finally {
             variableLock.unlock();
         }
     }
 
     /**
      * Sets new acquire increment value.
      *
      * @param newAcquireIncrement New acquire increment value.
      * */
     public void setAcquireIncrement(int newAcquireIncrement) {
         if (newAcquireIncrement < 0) {
             throw new IllegalArgumentException("Argument value must be equal or greater than 1:" + newAcquireIncrement);
         }
 
         variableLock.lock();
         try {
             this.acquireIncrement = newAcquireIncrement;
         } finally {
             variableLock.unlock();
         }
     }
 
     /**
      * Creates necessary number of database connections.
      *
      * @param requiredSize Number of the database connection.
      * @return List of the database connections.
      * @throws SQLException If any database access problems happened.
      * @throws ClassNotFoundException If there's any problem with finding a jdbc driver class.
      * */
     private List<DatabaseConnection> createDatabaseConnections(int requiredSize, QueryCreator queryCreator) throws SQLException, ClassNotFoundException {
         List<DatabaseConnection> conn = new ArrayList<>(requiredSize);
 
         for (int i = 0; i < requiredSize; ++i) {
             conn.add(new DatabaseConnection(databaseInformation, queryCreator));
         }
 
         return conn;
     }
 }
