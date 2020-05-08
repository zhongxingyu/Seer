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
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashSet;
 import java.util.Observable;
 
 import javax.swing.JOptionPane;
 
 import taberystwyth.view.OverviewFrame;
 
 /**
  * A clever wrapper for the Connection class that provides Observer/Observable
  * for notification about changes as well as singleton behavior
  * 
  * @author Cal Paterson
  */
 public class SQLConnection extends Observable {
     
     /**
      * The instance of this (singleton) object
      */
     private static SQLConnection instance = new SQLConnection();
     
     /**
      * Returns the instance of this (singleton) object
      * 
      * @return
      */
     public static SQLConnection getInstance() {
         return instance;
     }
     
     private boolean changeTracking = true;
     
     /**
      * The current connection
      */
     private Connection conn;
     
     /**
      * The current database file
      */
     private File dbfile;
     
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
             panic(e, "Unable to load the database driver."
                     + "Perhaps your computer architecture is not supported?");
         }
         
         /*
          * No initialisation here - that is done by WelcomeDialog
          */
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
         if (file.exists()) {
             throw new IOException("tab already exists");
         }
         
         dbfile = file;
         
        /*
         * This lump of code is required in order to get the last modification
         * time of a jar resource
         */
        URL url = this.getClass().getResource("/schema.sql");
        URLConnection urlConn = url.openConnection();
        long schemaUnixTime = urlConn.getLastModified();
         
         conn = DriverManager.getConnection("jdbc:sqlite:"
                 + file.getAbsolutePath());
         InputStream schema = this.getClass().getResourceAsStream("/schema.sql");
         evaluateSQLFile(schema);
         
         /*
          * Set tab version
          */
         execute("insert into version values(" + schemaUnixTime + ");");
     }
     
     /**
      * Evaluates a given SQL file against the current connection.
      * 
      * @param conn
      *            connection to SQL database
      * @param filePath
      *            path of SQL file
      * @throws IOException
      *             if the file is not found
      * @throws SQLException
      *             if there is some problem with the SQL server
      */
     private synchronized void evaluateSQLFile(InputStream file) {
         char[] cbuf = new char[4000];
         try {
             new BufferedReader(new InputStreamReader(file)).read(cbuf);
             
             /*
              * FIXME: This block is some disgusting magic that loads all of
              * the SQL statements in the given file
              */
             String fileContents = new String(cbuf);
             String[] statements = fileContents.split(";");
             for (int i = 0; i < (statements.length - 1); ++i){
                 statements[i] = statements[i].concat(";");
                 //System.out.println(statements[i]);
                 conn.createStatement().execute(statements[i]);
             }
         } catch (Exception e) {
             panic(e,
                     "Unable to evaluate SQL file");
         }
         System.out.println("SQLConnection.evaluateSQLFile()");
         setChanged();
         notifyObservers();
     }
     
     /**
      * Execute an SQL statement against the database
      * 
      * @param statement
      *            statement
      * @return success or failure
      */
     public synchronized boolean execute(String statement) {
         boolean returnValue = false;
         try {
             /*
              * Order seems to be important here because the semantics of sqlite
              * are quite odd. Closing the connection is required in order to
              * flush updates to the database file
              */
             Statement stmt = conn.createStatement();
             returnValue = stmt.execute(statement);
             stmt.close();
             conn.close();
             conn = DriverManager.getConnection("jdbc:sqlite:"
                     + dbfile.getAbsolutePath());
         } catch (SQLException e) {
             panic(e,
                     "Unable to execute this statement against the database:\n"
                             + statement);
         }
         System.out.println("SQLConnection.execute()");
         setChanged();
         notifyObservers();
         return returnValue;
     }
     
     /**
      * Execute an SQL query against the database
      * 
      * @param query
      *            query
      * @return resultset
      */
     public synchronized ResultSet executeQuery(String query) {
         ResultSet returnValue = null;
         try {
             Statement stmt = conn.createStatement();
             returnValue = stmt.executeQuery(query);
         } catch (SQLException e) {
             panic(e, "Unable to execute this query against the database:\n"
                     + query);
         }
         return returnValue;
     }
     
     public synchronized void initialise() {
         // TODO Auto-generated method stub
         
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
     
     /**
      * An override that changes the visibility (the thread-safety) of the
      * superclasses' setChanged method
      */
     public synchronized void setChanged() {
         /*
          * We might not be tracking changes at the moment (ie: we are loading
          * the schema into a new tab. If so, do not track changes).
          */
         if (changeTracking) {
             super.setChanged();
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
         dbfile = file;
         conn = DriverManager.getConnection("jdbc:sqlite:"
                 + file.getAbsolutePath());
         
         /*
          * This line is required in order to unfuck sqlites' default behaviour,
          * which is to ignore fk constraints
          */
         execute("PRAGMA foreign_keys = ON;");
         
         /*
          * Get the version of the tab file
          */
         long tabVersion = 0;
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("select version from version;");
         rs.next();
         tabVersion = rs.getLong(1);
         
         /*
          * Get the unixtime of the last modification of the schema file
          */
         long schemaVersion = new File("data/schema.sql").lastModified();
         
         /*
          * If they don't match, bomb.
          */
         if (tabVersion != schemaVersion) {
            System.out.println("tabVersion=" + tabVersion);
            System.out.println("schemaVersion=" + schemaVersion);
             throw new Exception("tab version not as expected");
         }
         
         System.out.println("SQLConnection.setDatabase()");
         setChanged();
         notifyObservers();
     }
     
 }
