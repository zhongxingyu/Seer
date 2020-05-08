 package org.ezze.utils.application;
 
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
 import java.util.HashMap;
 
 /**
  * Class containing a single static method to determine application's executable path.
  * 
  * @author Dmitriy Pushkov
 * @version 0.0.2
  */
 public class ApplicationPath {
     
     /**
      * Hash map for storing application paths.
      */
     private static HashMap<Integer, String> applicationPaths = new HashMap<Integer, String>();
     
     /**
      * Retrieves a path to JAR containing specified class.
      * 
      * @param applicationClass
      *      Class to look path to JAR with
      * @return 
      *      Path to JAR or {@code null} if specified class is not found
      */
     public static String getApplicationPath(Class applicationClass) {
         
         // Determining application class' hash code
         Integer applicationHashCode = applicationClass.hashCode();
         
         // Checking whether path to specified class has already been determined
         if (applicationPaths.containsKey(applicationHashCode))
             return applicationPaths.get(applicationHashCode);
         
         // Determining operating system's name
         String operatingSystem = System.getProperty("os.name");
         
         // Retrieving path to executable jar
         String applicationPath = null;
         try {
             
             applicationPath = applicationClass.getProtectionDomain().getCodeSource().getLocation().getPath();
         }
         catch (Exception ex) {
             
             return null;
         }
         
         // Truncating jar name from the path
         applicationPath = applicationPath.replaceFirst("^(.*)[\\\\\\/]([^\\\\\\/]+\\.jar)$", "$1");
         
         // Truncating Netbeans debug path from application's path
         applicationPath = applicationPath.replaceFirst("^(.*)([\\\\\\/]build[\\\\\\/]classes[\\\\\\/]?)$", "$1");
         
         if (operatingSystem.matches("^.*Windows.*$")) {
             
             applicationPath = applicationPath.replace('/', '\\');
             applicationPath = applicationPath.replaceFirst(("^[\\\\]{1,}"), "");
         }
        
        // Decoding URL characters
        try {
            
            applicationPath = URLDecoder.decode(applicationPath, "UTF-8");
        }
        catch (UnsupportedEncodingException ex) {
        
            return null;
        }
 
         // Storing application path for further method's calls
         applicationPaths.put(applicationHashCode, applicationPath);
         return applicationPath;
     }
 }
