 package com.razie.pub;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import com.razie.pub.base.log.Log;
 
 /**
  * simple helper to execute windows commands
  * 
  * @author razvanc
  * 
  */
 public class WinExec {
     /**
      * use this to execute a document - windows will use the proper proogram...
      * 
      * @param file
      * @throws IOException
      */
     public static void execFile(String file) throws IOException {
         execCmd("cmd.exe /C ", file);
     }
 
     /**
      * use this to execute a document - windows will use the proper proogram...
      * 
      * @param file
      * @throws IOException
      */
     public static void execCmd(String program, String... args) throws IOException {
         String cmdline = program;
         for (String arg : args) {
             cmdline += " \"" + arg + "\"";
         }
 
         Runtime rt = Runtime.getRuntime();
         Log.logThis("EXECUTE cmd: " + cmdline);
         Process proc = rt.exec(cmdline);
         // any error message?
         StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
 
         // any output?
         StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
 
         // kick them off
         errorGobbler.start();
         outputGobbler.start();
     }
 
     /**
      * i think i found this somewhere on the net... if you don't do this something freaky hapens at
      * times...boo.hoo.hoo
      */
     static class StreamGobbler extends Thread {
         InputStream is;
         String      type;
 
         StreamGobbler(InputStream is, String type) {
             this.is = is;
             this.type = type;
         }
 
         public void run() {
             try {
                 InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader br = new BufferedReader(isr);
                 String line = null;
                 while ((line = br.readLine()) != null)
                     System.out.println(type + ">" + line);
             } catch (IOException ioe) {
                 ioe.printStackTrace();
             }
         }
     }
 }
