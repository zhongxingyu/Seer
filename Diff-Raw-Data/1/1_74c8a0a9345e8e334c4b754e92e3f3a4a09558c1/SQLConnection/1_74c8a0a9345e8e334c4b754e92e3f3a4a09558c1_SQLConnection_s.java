 /*
  * This file is part of TAberystwyth, a debating competition organiser
  * Copyright (C) 2010, Roberto Sarrionandia and Cal Paterson
  * 
  * This program is free software: you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option)
  * any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package taberystwyth.db;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JOptionPane;
 
 import org.apache.log4j.Logger;
 
 /**
  * A clever wrapper for the Connection class that provides Observer/Observable
  * for notification about changes as well as singleton behaviour
  * 
  * @author Cal Paterson
  */
 final public class SQLConnection extends Observable implements Runnable {
     
     /**
      * The instance of this (singleton) object
      */
     private static final SQLConnection INSTANCE = new SQLConnection();
     
     private static final Logger LOG = Logger.getLogger(SQLConnection.class);
     
     /**
      * The frequency (in milliseconds) with which this singleton notifies
      * observers
      */
     private static final int NOTIFY_FREQUENCY = 100;
     
     /**
      * Returns the instance of this (singleton) object
      * 
      * @return
      */
     public static SQLConnection getInstance() {
         return INSTANCE;
     }
     
     /**
      * This flag indicates whether observers are notified of updates. If there
      * are a lot of updates going on very frequently, it's better to disable
      * change tracking, make the change and then call //FIXME
      */
     private boolean changeTracking = true;
     
     /**
      * The current connection
      */
     private Connection conn;
     
     /**
      * The current database file
      */
     private File file;
     
     /**
      * Constructor
      */
     private SQLConnection() {
         /*
          * Ensure that the SQL driver is pulled through the class loader
          */
         try {
             Class.forName("org.sqlite.JDBC");
         } catch (ClassNotFoundException e) {
             LOG.fatal("Unable to load database driver");
             panic(e, "Unable to load the database driver."
                     + "Perhaps your computer architecture is not supported?");
         }
         
         /*
          * No initialisation here
          */
     }
     
     /**
      * Evaluates a given SQL file against the current connection.
      */
     private void evaluateSQLFile(InputStream file) {
         synchronized (getInstance()) {
             char[] cbuf = new char[10000];
             InputStreamReader isr = null;
             BufferedReader br = null;
             try {
                 isr = new InputStreamReader(file);
                 br = new BufferedReader(new InputStreamReader(file));
                 br.read(cbuf);
                 
                 /*
                  * FIXME: This block is some disgusting magic that loads all of
                  * the SQL statements in the given file
                  */
                 String fileContents = new String(cbuf);
                 String[] statements = fileContents.split(";");
                 for (int i = 0; i < (statements.length - 1); ++i) {
                     statements[i] = statements[i].concat(";");
                     // System.out.println(statements[i]);
                     conn.createStatement().execute(statements[i]);
                 }
                 LOG.debug("Evaluated SQL file");
                 /*
                  * FIXME: Change this method's argument type to File, so that
                  * the log can hold the absolute file path
                  */
             } catch (Exception e) {
                 LOG.fatal("Unable to evaluate SQL file", e);
                 
             } finally {
                 try {
                     br.close();
                     isr.close();
                 } catch (IOException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
             }
             setChanged();
             notifyObservers();
         }
     }
     
     /**
      * An override that changes the visibility (the thread-safety) of the
      * superclasses' setChanged method
      */
     @Override
     protected synchronized void setChanged() {
         /*
          * We might not be tracking changes at the moment (ie: we are loading
          * the schema into a new tab. If so, do not track changes).
          */
         if (changeTracking) {
             super.setChanged();
         }
     }
     
     @Override
     public void addObserver(Observer observer) {
         super.addObserver(observer);
         LOG.info("Observer added: " + observer);
         /*
          * setChanged() is not designed for this kind of use, but the intention
          * here is that this ensures that in the next loop the observers are
          * told to repull
          */
         setChanged();
     }
     
     /**
      * Creates a tab in a given location
      * 
      * @param file
      *            the given location
      * @throws IOException
      * @throws SQLException
      */
     public synchronized void create(File file) throws IOException,
             SQLException {
         this.file = file;
         
         /*
          * Check if the file already exists
          */
         if (file.exists()) {
             throw new IOException("tab already exists");
         }
         
         conn = DriverManager.getConnection("jdbc:sqlite:"
                 + file.getAbsolutePath());
         conn.setAutoCommit(false);
         
         InputStream schema = this.getClass()
                 .getResourceAsStream("/schema.sql");
         evaluateSQLFile(schema);
         schema.close();
     }
     
     /**
      * This method closes the current connection and opens a new one.
      * 
      * This is complete hack but is absolutely required because if this isn't
      * done then sqlite won't notice any updates that are done to the code.
      * 
      * @throws SQLException
      */
     public void commit() throws SQLException {
         LOG.debug("committing...");
         conn.commit();
         conn.close();
         conn = DriverManager.getConnection("jdbc:sqlite:"
                 + file.getAbsolutePath());
         /*
          * Statement statement = conn.createStatement();
          * statement.execute("PRAGMA foreign_keys = ON;"); statement.close();
          */
         conn.setAutoCommit(false);
         LOG.debug("   ...done");
         setChanged();
     }
     
     /**
      * Execute an SQL query against the database
      * 
      * @param query
      *            query
      * @return resultset
      */
     @Deprecated
     public ResultSet executeQuery(String query) {
         synchronized (getInstance()) {
             ResultSet returnValue = null;
             try {
                 Statement stmt = conn.createStatement();
                 returnValue = stmt.executeQuery(query);
                 // LOG.info("Executed query: " + query);
             } catch (SQLException e) {
                 panic(e,
                         "Unable to execute this query against the database:\n"
                                 + query);
             }
             return returnValue;
         }
     }
     
     public synchronized boolean isChangeTracking() {
         return changeTracking;
     }
     
     /**
      * There are so many instances where an unfixable situation arises in this
      * file that I have defined this shorthand.
      * 
      * @param e
      *            an exception
      * @param reason
      *            a Very Informative Description of the problem
      */
     @Deprecated
     public synchronized void panic(Exception e, String reason) {
         String message = reason + "\n" + "The exact exception was:\n"
                 + e.getMessage();
         JOptionPane.showMessageDialog(null, message);
         e.printStackTrace();
         System.exit(255);
     }
     
     @Override
     public void run() {
         while (true) {
             try {
                 Thread.sleep(NOTIFY_FREQUENCY); // FIXME: finalise
                 notifyObservers();
             } catch (InterruptedException e) {
                 /*
                  * If we are interrupted, then do nothing
                  */
                 LOG.warn("Interrupted", e);
             }
         }
     }
     
     /**
      * Set the database file, and loads the schema into it if required.
      * 
      * @param file
      *            sqlite3 file
      * @throws Exception
      */
     public synchronized void set(File file) throws Exception {
         /*
          * First, load the file
          */
         this.file = file;
         conn = DriverManager.getConnection("jdbc:sqlite:"
                 + file.getAbsolutePath());
         conn.setAutoCommit(false);
         
         LOG.info("Database set to " + file.getAbsolutePath());
         
         setChanged();
         notifyObservers();
     }
     
     public void setChangeTracking(boolean changeTracking) {
         this.changeTracking = changeTracking;
         setChanged();
     }
     
     public void start() {
         Thread thread = new Thread(INSTANCE);
         thread.setName("SQL");
         thread.start();
     }
     
     public PreparedStatement prepareStatement(String string)
             throws SQLException {
         return conn.prepareStatement(string);
     }
     
     public void rollback() throws SQLException {
         conn.rollback();
     }
     
 }
