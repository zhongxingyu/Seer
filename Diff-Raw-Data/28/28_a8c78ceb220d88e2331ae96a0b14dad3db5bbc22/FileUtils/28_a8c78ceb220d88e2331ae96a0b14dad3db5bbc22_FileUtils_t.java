 package suncertify.constants;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 public class FileUtils {
   private static final Logger log = LoggerFactory.getLogger(FileUtils.class);
 
   public static void copyFile(String sourceFile, String destinationFile) {
     try {
       File f1 = new File(sourceFile);
       File f2 = new File(destinationFile);
       InputStream in = new FileInputStream(f1);
       OutputStream out = new FileOutputStream(f2);
       byte[] buf = new byte[1024];
       int len;
       while ((len = in.read(buf)) > 0) {
         out.write(buf, 0, len);
       }
       in.close();
       out.close();
      log.info("File copied from " + sourceFile + " to " + destinationFile);
     } catch (FileNotFoundException ex) {
       log.error(ex.getMessage(), ex);
     } catch (IOException e) {
       log.error(e.getMessage(), e);
     }
   }
 }
