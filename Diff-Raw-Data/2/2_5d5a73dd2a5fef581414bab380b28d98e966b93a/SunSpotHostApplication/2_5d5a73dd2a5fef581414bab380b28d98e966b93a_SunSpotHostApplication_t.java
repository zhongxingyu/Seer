 /*
  * SunSpotHostApplication.java
  *
  * Created on Sep 26, 2013 12:47:16 PM;
  */
 
 package org.sunspotworld;
 
 import com.sun.spot.io.j2me.radiogram.*;
 import javax.microedition.midlet.MIDletStateChangeException;
 import com.sun.spot.peripheral.ota.OTACommandServer;
 import com.sun.spot.util.Utils;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.text.DateFormat;
 import java.util.Date;
 import javax.microedition.io.*;
 
 
 /**
  * Host application
  */
 /**
  * Host application
  */
 public class SunSpotHostApplication {
 
     // Broadcast port on which we listen for sensor samples
     private static final int HOST_PORT = 67;
         
     private void run() throws Exception {
         RadiogramConnection rCon;
         Datagram dg;
         
          
         try {
             // Open up a server-side broadcast radiogram connection
             // to listen for sensor readings being sent by different SPOTs
             rCon = (RadiogramConnection) Connector.open("radiogram://broadcast:" + HOST_PORT);
             dg = rCon.newDatagram(50);
         } catch (Exception e) {
              System.err.println("setUp caught " + e.getMessage());
              throw e;
         }
 
         //Create initial table and insert data into that table
         createTable();
         insertInitialDataTable();
         
         // Loop to query database and send info to spots
         while (true) {
             try {
                 int data = queryTable(1);
                 System.out.println("value in table is " + data);
                 dg.reset();
                 dg.writeInt(data);
                 rCon.send(dg);
                 Utils.sleep(100);
                 
             } catch (Exception e) {
                 System.err.println("Caught " + e +  " while reading sensor samples.");
                 throw e;
             }
         }
     }
     
     /**
      * Start up the host application.
      *
      * @param args any command line arguments
      */
     public static void main(String[] args) throws Exception {
         // register the application's name with the OTA Command server & start OTA running
         OTACommandServer.start("SendDataDemo");
 
         SunSpotHostApplication app = new SunSpotHostApplication();
         
         app.run();
     }
 
     /* Method to create table with three rows*/
     /*Create table code based on code at: http://www.tutorialspoint.com/sqlite/sqlite_java.htm*/
     public static void createTable() {
         java.sql.Connection createConnect = null;
         Statement sqlStatement = null;
         
         try {
             
             /*Create connection with database*/
             Class.forName("org.sqlite.JDBC");
             createConnect = DriverManager.getConnection("jdbc:sqlite:spotData.db");
             System.out.println("Opened database successfully");
 
             /*Create table sql statement*/
             sqlStatement = createConnect.createStatement();
             String sql = "CREATE TABLE OURDATA"
                     + "(ID             VARCHAR   NOT NULL,"
                     + "ONOFF           CHAR    NOT NULL, "
                     + "CONSTRAINT pri_id_time PRIMARY KEY(ID))";
             
             /*Execute sql statement and close connection*/
             try{
             sqlStatement.executeUpdate(sql);
             }catch(Exception e){
                 System.err.println("Table already exists...continuing");
             }
             sqlStatement.close();
             createConnect.close();
             
         } catch (Exception e) {
             System.err.println(e.getClass().getName() + ": " + e.getMessage());
             System.exit(0);
         }
         System.out.println("Table created successfully");
     }
     
         /*method to insert table*/
     public static void insertInitialDataTable()
     {
         java.sql.Connection insertConnection = null;
         Statement insertStatement = null;
         
         try 
         {
             /*Create connection with database*/
             Class.forName("org.sqlite.JDBC");
             insertConnection = DriverManager.getConnection("jdbc:sqlite:spotData.db");
             System.out.println("Opened database successfully");
             
             /*Create sql statement*/
             insertStatement = insertConnection.createStatement();
             
             try{
             String sql = "INSERT INTO OURDATA(ID,ONOFF)" + 
                     "VALUES('0014.4F01.0000.7FEE',0);";
             insertStatement.executeUpdate(sql);
             
             String sql2 = "INSERT INTO OURDATA(ID,ONOFF)" + 
             "VALUES('0014.4F01.0000.765E',0);";
             insertStatement.executeUpdate(sql2);
             
             String sql3 = "INSERT INTO OURDATA(ID,ONOFF)" + 
             "VALUES('0014.4F01.0000.4120',0);";
             insertStatement.executeUpdate(sql3);
             }catch(Exception e)
             {
                 System.err.println("Table already contains default data...continuing");
             }
             insertStatement.close();
             insertConnection.close();
             
         }catch (Exception e) {
             System.err.println(e.getClass().getName() + ": " + e.getMessage());
             System.exit(0);
         }
     }
     
     public static int queryTable(int spotNumber) {
         java.sql.Connection queryConnection = null;
         Statement queryStatement = null;
 
         try {
             /*Create connection with database*/
             Class.forName("org.sqlite.JDBC");
             queryConnection = DriverManager.getConnection("jdbc:sqlite:spotData.db");
             System.out.println("Opened database successfully");
 
             /*Create sql statement*/
             queryStatement = queryConnection.createStatement();
 
             if (spotNumber == 1) {
                 String sql = "SELECT ONOFF "
                         + "FROM OURDATA "
                         + "WHERE ID = '0014.4F01.0000.7FEE';";
                 ResultSet myResult = queryStatement.executeQuery(sql);
                 int data = myResult.getInt("ONOFF");
                 
                 myResult.close();
                 queryStatement.close();
                 queryConnection.close();
 
                 return data;
                 
             } else if (spotNumber == 2) {
                 String sql = "SELECT ONOFF "
                         + "FROM OURDATA "
                         + "WHERE ID = '0014.4F01.0000.4120';";
                 
                 ResultSet myResult = queryStatement.executeQuery(sql);
                 int data = myResult.getInt("ONOFF");
                 
                 myResult.close();
                 queryStatement.close();
                 queryConnection.close();
 
                 return data;
                 
 
             } else {
                 String sql = "SELECT ONOFF "
                         + "FROM OURDATA "
                        + "WHERE ID = '0014.4F01.0000.765E';";
 
                 ResultSet myResult = queryStatement.executeQuery(sql);
                 int data = myResult.getInt("ONOFF");
                 
                 myResult.close();
                 queryStatement.close();
                 queryConnection.close();
 
                 return data;
             }
 
         } catch (Exception e) {
             System.err.println(e.getClass().getName() + ": " + e.getMessage());
             return 0;
         }
     }
 }
