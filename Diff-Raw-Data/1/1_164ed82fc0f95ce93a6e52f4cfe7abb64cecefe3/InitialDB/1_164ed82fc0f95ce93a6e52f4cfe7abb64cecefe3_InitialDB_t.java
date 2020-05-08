 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package model;
 
 import java.sql.*;
 import java.io.*;
 
 /**
  *
  * @author liufeng
  */
 public class InitialDB {
     /**
      * Just trivial initial stuff.
      * not finished yet.
      */
     private static void initialDB() {
         try {
             Class.forName("java.sqlite.JDBC");
 
             Connection connection = DriverManager.getConnection("jdbc.sqlite:/Users/liufeng/prog/ss/song.db");
             Statement statement = connection.createStatement();
             statement.executeUpdate("create table songlist (title, performer, recordingTitle, recordingType, year, length, popularity, playCount, addedTime, lastPlayed, priority);");
 
             BufferedReader in = new BufferedReader(new FileReader("library.txt"));
             String line = in.readLine();
             while (line != null) {
                 String[] token = line.split(";");
                 String title = token[0];
                 String performer = token[1];
                 String recordingTitle = token[2];
                 String recordingType = token[3];
                 String year = token[4];
                 String length = token[5] + ":" + token[6];
                 int popularity = Integer.parseInt(token[7]);
                 int playCount = Integer.parseInt(token[8]);
                 Time addedTime = new Time().getCurrentTime();
                 Time lastPlayed = new Time(0, 0, 0, 0, 0, 0);
                 double priority = 0;
 
                 //statement.executeUpdate("insert into songlist (title, performer, recordingTitle, recordingType, year, length, popularity, playCount, addedTime, lastPlayed, priority) values (\"" + title + "\"" + ", \"" + performer + "\", \"" + recordingTitle + "\"" , recordingType, year, length, popularity, playCount, addedTime, lastPlayed, priority + "\");";");
                line = in.readLine();
             }
 
             connection.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
