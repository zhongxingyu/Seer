 package org.hackystat.sensor.ant.perforce;
 
 import java.util.Enumeration;
 import java.util.Vector;
 
 import com.perforce.api.Debug;
 import com.perforce.api.Env;
 import com.perforce.api.FileEntry;
 import com.perforce.api.Utils;
 
 /**
  * Example of collecting statistics about a file.
  * 
  * @author Philip Johnson
  */
 public class StatFile {
 
   /**
    * The main program.
    * 
    * @param argv Ignored.
    */
   @SuppressWarnings("unchecked")
   public static void main(String[] argv) {
    System.out.println("Starting.");
     /*
      * Please see the Common.java file to see how the environment is being set up. This is
      * important, but it is common to all the examples.
      */
     Env env = Common.setup();
    String filename = "//guest/philip_johnson/test.txt";
     Vector fileHistory = null;
     FileEntry fent;
     Debug.setDebugLevel(Debug.ERROR);
 
     try {
       fileHistory = FileEntry.getFileLog(env, filename);
 
       // For display/testing purposes only
       System.out.println("Filelog for " + filename + ": \n");
       Enumeration logs = fileHistory.elements();
       while (logs.hasMoreElements()) {
         fent = (FileEntry) logs.nextElement();
         fent.sync(); // Uncomment for exact time.
         System.out.println("\tRev: " + fent.getHeadRev());
         System.out.println("\t\tChange: " + fent.getHeadChange());
         System.out.println("\t\tAction: " + fent.getHeadAction());
         System.out.println("\t\tType: " + fent.getHeadType());
         System.out.println("\t\tOwner: " + fent.getOwner());
         System.out.println("\t\tDate: " + fent.getHeadTimeString());
         System.out.println("\t\tDescription: " + fent.getDescription());
       }
 
     }
     catch (Exception ex) {
       ex.printStackTrace();
     }
     Utils.cleanUp();
    System.out.println("Ending");
   }
 }
