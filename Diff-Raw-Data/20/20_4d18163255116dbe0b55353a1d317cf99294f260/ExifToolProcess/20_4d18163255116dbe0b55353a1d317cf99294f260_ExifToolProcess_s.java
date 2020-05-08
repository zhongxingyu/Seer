 package org.basex.fs.fsml.build.exiftool;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 
 import org.basex.core.Prop;
 import org.basex.io.IO;
 import org.basex.io.IOContent;
 import org.basex.util.Token;
 import org.basex.util.Util;
 
 /**
  * Invoke native process to run ExifTool.
  *
  * @author BaseX Team 2005-11, BSD License
  * @author Alexander Holupirek
  */
 public class ExifToolProcess {
 
   /** Prefix for debugging messages. */
   private final String debugName = "[ExifToolProcess] ";
   /** Flag existence of ExifTool binary in PATH. */
   private boolean systemHasExifTool;
   /** Flag if ExifTool process could be started. */
   private boolean exifToolIsStarted;
   /** Reference to native ExifTool process. */
   private Process procExifTool;
   /** Read output produced by ExifTool. */
   private BufferedReader fromExiftool;
   /** Send commands to ExifTool. */
   private BufferedWriter toExiftool;
 
   /** Constructor. */
   protected ExifToolProcess() {
     String pathToExifTool = null;
     /* search ExifTool binary. */
     String systemPath = System.getenv("PATH");
     for (String path : systemPath.split(Prop.WIN ? ";" : ":")) {
       File p = new File(path + (Prop.WIN ? "/exiftool.exe" : "/exiftool"));
       Util.debug(debugName + "Looking for ExifTool " + p.getAbsolutePath());
       if (p.exists()) {
         systemHasExifTool = true;
         pathToExifTool = p.getAbsolutePath();
         Util.debug(debugName + "Found ExifTool: " + p.getAbsolutePath());
         break;
       }
     }
     if (!systemHasExifTool) {
       String pfx = debugName;
       StringBuffer sb = new StringBuffer();
       sb.append(pfx + "ExifTool(1) could not be found in System.PATH: ");
       sb.append(systemPath + "\n");
       sb.append(pfx + "Metadata extraction for audio + images relies on it.\n");
       sb.append(pfx + "Consider installing it from: ");
       sb.append("http://www.sno.phy.queensu.ca/~phil/exiftool/");
       Util.debug(sb.toString());
       return;
     }
     /* create ExifTool process and keep it up and running. */
     try {
         procExifTool = new ProcessBuilder(new String[] {
             pathToExifTool, "−stay_open", "True", "-@", "-"
             }).start();
         fromExiftool = new BufferedReader(new InputStreamReader(
                 procExifTool.getInputStream()));
         toExiftool = new BufferedWriter(new OutputStreamWriter(
                 procExifTool.getOutputStream()));
         exifToolIsStarted = true;
     } catch (IOException e) {
         Util.debug(e);
     }
     /* shut process down when JVM exits. */
     Runtime.getRuntime().addShutdownHook(new Thread() {
       @Override
       public void run() {
         terminate();
         Util.debug(debugName + "exit value: " + procExifTool.exitValue());
       }
     });
   }
 
   /**
    * Extracts metadata from IO resource using ExifTool.
    *
    * @param path to file
    * @return extracted metadata as XML string
    */
   public IOContent extractMetadataAsXML(final IO src) {
     boolean resultReceived = false;
     int waitLimit = 1000;
     StringBuilder sb = new StringBuilder(2048);
     try {
       String path = new String(src.path().getBytes("UTF-8"));
 
       toExiftool.write("-X");
       toExiftool.newLine();
       toExiftool.write(path);
       toExiftool.newLine();
       toExiftool.write("-execute");
       toExiftool.newLine();
       toExiftool.flush();
       sb.append(fromExiftool.readLine());
       while (true) {
         if (fromExiftool.ready()) {
             final String line = fromExiftool.readLine();
             if (line.equals("{ready}")) {
               resultReceived = true;
               break;
             }
             sb.append(line);
         } else {
             try {
               if ((waitLimit -= 100) > 0) {
                 Thread.sleep(100);
                 Util.debug(debugName + "Waiting for more input: " + path);
               } else {
                 break;
               }
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
       }
       if (resultReceived)
         return new IOContent(Token.token(sb.toString()));
     } catch (IOException e) {
       Util.debug(debugName + e);
     }
     Util.debug(debugName + "Missing output from Exiftool for " + src.path());
     return new IOContent(Token.EMPTY);
   }
 
   /** Checks if ExifToolPross is alive. */
   public boolean isAlive() {
     if (!exifToolIsStarted) return false;
     try {
       /* check communication -> -execute <- {ready}*/
       toExiftool.write("-execute");
       toExiftool.newLine();
       toExiftool.flush();
       return fromExiftool.readLine().equals("{ready}");
     } catch (IOException e) {
       Util.debug(e);
       return false;
     }
   }
 
   /** Tears down running ExifTool process. */
   public void terminate() {
     try {
       if (isAlive()) {
         toExiftool.write("-stay_open\nFalse\n");
         toExiftool.flush();
         toExiftool.close();
         fromExiftool.close();
         procExifTool.waitFor();
       }
     } catch (Exception e) {
       Util.debug(e);
     }
   }
 }
