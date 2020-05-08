 package com.jreddit.casinobots;
 
 import java.io.*;
 import java.util.*;
 
 import com.almworks.sqlite4java.*;
 import com.jreddit.botkernel.*;
 
 /**
  *
  * Persistence Utilities
  *
  */
 public class PersistenceUtils {
 
     /**
      *
      *  Location of the db file. This will be relative to the
      *  working directory of the botkernel we are running in.
      */
     private static final String DB_FILE = "../casinobots/scratch/bots.db";
 
     //
     // NOTE How to check for sqlite tables defined in the schema
     //
     // $ sqlite3 scratch/bots.db
     // sqlite> SELECT * FROM sqlite_master WHERE type='table';
     //
 
     private static Object DB_LOCK = new Object();
 
     /**
      *
      * Load a file into a List.
      *
      * Load the specified file line by line into a list the given
      * number of lines.
      *
      * @param filename  The name of the file
      * @param list      The list on which to add items
      * @param numLines  The max number of lines to read from the file.
      *                  Specify -1 to read all lines
      *
      */
     public static void loadList(    String filename, 
                                     List<String> list, 
                                     int numLines) {
         try {
             FileInputStream fis = new FileInputStream(filename);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr);
 
             int i = 0;
             String line = null;
 
             while((line = br.readLine()) != null) {
                 list.add(line.trim());
                 i++;
                 if(i == numLines) {
                     break;
                 }
             }
             
             br.close();
             isr.close();
             fis.close();
 
         } catch (IOException e) {
             e.printStackTrace();
             BotKernel.getBotKernel().log("Error loading file " + filename);
         }
     }
 
     /**
      *  Save a List to a file.
      *
      * @param filename  The name of the file
      * @param list      The list to write to the file
      *
      */
     public static synchronized void saveList(   String filename, 
                                                 List<String> list ) {
         try {
 
             FileOutputStream fos = new FileOutputStream(filename);
 
             for(int i = 0; i < list.size(); i++) {
                 String sub = list.get(i);
                 fos.write((sub + "\n").getBytes());
             }
 
             fos.close();
 
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /**
      *
      * Return true if the bot has replied to the specified thing.
      * False otherwise.
      *
      * @param botName   The name of the bot.
      * @param thingName The name of the Thing.
      *
      */
     public static boolean isBotReplied(String botName, String thingName) {
         synchronized(DB_LOCK) {
 
             //
             // This might be a bit counter intuitive, but we will default
             // to true here so that the bot doesn't go spam replying
             // if the db connection somehow fails.
             //
             boolean ret = true;
             
             try {
 
                 //
                 // TODO Should this connection be 
                 // cached rather than instantiated each time?
                 //
                 SQLiteConnection db = new SQLiteConnection(new File(DB_FILE));
                 db.open(true);
 
                 SQLiteStatement st = db.prepare(
                     "SELECT bot_name, thing_name " +
                     " FROM bot_replies " +
                    " WHERE bot_name = ? AND thing_name = ?");
 
                 try {
                     st.bind(1, botName);
                     st.bind(2, thingName);
                     if(st.step()) {
                         ret = true;
                     } else {
                         ret = false;
                     }
                 } finally {
                     st.dispose();
                 }
                 db.dispose();
 
             } catch(SQLiteException se) {
                 se.printStackTrace();
                 BotKernel.getBotKernel().log("SEVERE error with database.");
             }
 
             return ret;
         }
     }
    
     /**
      * 
      * Set a thing as having been replied to by the specified bot.
      *
      * @param botName   The name of the bot.
      * @param thingName The name of the Thing.
      *
      */
     public static void setBotReplied(String botName, String thingName) {
         synchronized(DB_LOCK) {
 
             try {
                 //
                 // TODO Should this connection be cached rather 
                 // than instantiated each time?
                 //
                 SQLiteConnection db = new SQLiteConnection(new File(DB_FILE));
                 db.open(true);
 
                 SQLiteStatement st = db.prepare(
                     "INSERT INTO bot_replies (bot_name, thing_name) " +
                     " VALUES (?, ?)" );
 
                 try {
                     st.bind(1, botName);
                     st.bind(2, thingName);
                     st.step();
                 } finally {
                     st.dispose();
                 }
                 db.dispose();
 
             } catch(SQLiteException se) {
                 se.printStackTrace();
                 BotKernel.getBotKernel().log("SEVERE error with database.");
             }
         }
     }
 
 }
