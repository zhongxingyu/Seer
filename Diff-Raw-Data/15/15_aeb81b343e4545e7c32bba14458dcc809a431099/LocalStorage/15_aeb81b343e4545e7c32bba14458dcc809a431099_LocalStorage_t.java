 package com.allplayers.android;
 
 import android.content.Context;
 
 import org.jasypt.util.text.BasicTextEncryptor;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.Calendar;
 import java.util.Random;
 
 //Local Storage for JSON Strings
 //
 //Example Usage:
 //LocalStorage.writeInbox(this.getBaseContext(), jsonResult);
 //LocalStorage.readInbox(this.getBaseContext()); <---Returns a String
 //
 //
 //writeInbox(Context, String)
 //readInbox(Context)
 //
 //writeUserGroups(Context, String)
 //readUserGroups(Context)
 //
 //writeUserGroupMembers(Context, String)
 //readUserGroupMembers(Context)
 //
 //Will add more as we implement this functionality into main code
 //
 
 public class LocalStorage {
     protected static Context mContext;
     private static String writeString;
 
     public static void writeSecretKey(Context c) {
         Random random = new Random();
         String alphabet = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
         String uniqueKey = "";
         for (int i = 0; i < 50; i++) {
             uniqueKey = uniqueKey + alphabet.charAt(random.nextInt(alphabet.length()));
         }
         writeFile(c, uniqueKey, "SecretKey");
     }
     public static String readSecretKey(Context c) {
         String returnValue = readFile(c, "SecretKey");
         return returnValue;
     }
 
     public static void writeUserName(Context c, String write) {
         writeFile(c, write, "UserName");
     }
     public static String readUserName(Context c) {
         String returnValue = readFile(c, "UserName");
         return returnValue;
     }
 
    public static void writePassword(Context c, String write) {    	
        writeFile(c, write, "Password");
     }
     public static String readPassword(Context c) {
         String returnValue = readFile(c, "Password");
         return returnValue;
     }
 
     //If overwrite is true, the file will be written regardless of the last update timestamp
     public static void writeInbox(Context c, String write, boolean overwrite) {
         if (overwrite) {
             writeFile(c, write, "Inbox");
         } else {
             long lastUpdate = getTimeSinceLastModification("Inbox");
 
             if (lastUpdate / 1000 / 60 > 15) { //more than 15 minutes since last update
                 writeFile(c, write, "Inbox");
             }
         }
     }
     public static String readInbox(Context c) {
         String returnValue = readFile(c, "Inbox");
         return returnValue;
     }
 
     public static void writeSentbox(Context c, String write, boolean overwrite) {
         if (overwrite) {
             writeFile(c, write, "Sentbox");
         } else {
             long lastUpdate = getTimeSinceLastModification("Sentbox");
 
             if (lastUpdate / 1000 / 60 > 15) { //more than 15 minutes since last update
                 writeFile(c, write, "Sentbox");
             }
         }
     }
     public static String readSentbox(Context c) {
         String returnValue = readFile(c, "Sentbox");
         return returnValue;
     }
 
     public static void writeUserGroups(Context c, String write, boolean overwrite) {
         if (overwrite) {
             writeFile(c, write, "UserGroups");
         } else {
             long lastUpdate = getTimeSinceLastModification("UserGroups");
 
             if (lastUpdate / 1000 / 60 > 60) { //more than 60 minutes since last update
                 writeFile(c, write, "UserGroups");
             }
         }
     }
     public static String readUserGroups(Context c) {
         String returnValue = readFile(c, "UserGroups");
         return returnValue;
     }
 
     public static void writeUserGroupMembers(Context c, String write, boolean overwrite) {
         if (overwrite) {
             writeFile(c, write, "UserGroupMembers");
         } else {
             long lastUpdate = getTimeSinceLastModification("UserGroupMembers");
 
             if (lastUpdate / 1000 / 60 > 60) { //more than 60 minutes since last update
                 writeFile(c, write, "UserGroupMembers");
             }
         }
     }
     public static String readUserGroupMembers(Context c) {
         String returnValue = readFile(c, "UserGroupMembers");
         return returnValue;
     }
 
     public static void writeUserAlbums(Context c, String write, boolean overwrite) {
         if (overwrite) {
             writeFile(c, write, "UserAlbums");
         } else {
             long lastUpdate = getTimeSinceLastModification("UserAlbums");
 
             if (lastUpdate / 1000 / 60 > 60) { //more than 60 minutes since last update
                 writeFile(c, write, "UserAlbums");
             }
         }
     }
     public static String readUserAlbums(Context c) {
         String returnValue = readFile(c, "UserAlbums");
         return returnValue;
     }
     public static void appendUserAlbums(Context c, String write) {
         String returnValue = readFile(c, "UserAlbums");
         writeUserAlbums(c, returnValue + "\n" + write, true);
     }
 
     public static void writeUserEvents(Context c, String write, boolean overwrite) {
         if (overwrite) {
             writeFile(c, write, "UserEvents");
         } else {
             long lastUpdate = getTimeSinceLastModification("UserEvents");
 
             if (lastUpdate / 1000 / 60 > 10) { //more than 10 minutes since last update
                 writeFile(c, write, "UserEvents");
             }
         }
     }
     public static String readUserEvents(Context c) {
         String returnValue = readFile(c, "UserEvents");
         return returnValue;
     }
 
     public static void writeFile(Context c, String write, String fileName) {
         mContext = c;
         writeString = write;
 
         try {
             writeString = write;
 
             FileOutputStream fOut = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
             OutputStreamWriter osw = new OutputStreamWriter(fOut);
 
             osw.write(writeString);
 
             osw.flush();
             osw.close();
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
     }
 
     public static String readFile(Context c, String fileName) {
         mContext = c;
         String data = "";
         FileInputStream fis;
 
         try {
             fis = mContext.openFileInput(fileName);
 
             InputStreamReader isr = new InputStreamReader(fis);
 
             BufferedReader br = new BufferedReader(isr);
 
             data = br.readLine();
 
             isr.close();
             br.close();
 
             return data;
         } catch (FileNotFoundException fnfe) {
             fnfe.printStackTrace();
             return "";
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
 
         return data;
     }
 
     public static long getTimeSinceLastModification(String filename) {
         File file = new File(filename);
 
         long modified = 0;
 
         if (file.exists()) {
             modified = file.lastModified();
         }
 
         long current = Calendar.getInstance().getTimeInMillis();
 
         return current - modified;
     }
 }
