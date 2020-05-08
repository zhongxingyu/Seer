 package gov.usgs.cida.ncetl.utils;
 
 import java.io.File;
 import java.io.IOException;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author jwalker
  */
 public class FileHelper {
 
     public static final String FILE_STORE = System.getProperty("user.home") + IOUtils.DIR_SEPARATOR + ".ncetl";
     public static Logger log = LoggerFactory.getLogger(FileHelper.class);
 
     public static boolean setupDirectories() throws IOException {
         log.debug("setting up directories");
         File dir = new File(FILE_STORE);
         if (!dir.exists()) {
            log.debug("Directory " + FILE_STORE + " doesn't exist, creating it now.");
             FileUtils.forceMkdir(dir);
            log.debug("Directory " + FILE_STORE + " created.");
         }
         if (dir.isDirectory()) {
            log.debug("Directory " + FILE_STORE + " found.");
            log.debug("Checking read permissions.");
             if (dir.canRead()) {
                log.debug("Directory " + FILE_STORE + " is readable.");
                log.debug("Checking write permissions.");
                 if (dir.canWrite()) {
                    log.debug("Directory " + FILE_STORE + " is writable.");
                     return true;
                 }
                throw new IOException("Directory " + FILE_STORE + " is not writable. The application will not be able to continue functioning.");
             }
            throw new IOException("Directory " + FILE_STORE + " is not readable. The application will not be able to continue functioning.");
         }
        throw new IOException(FILE_STORE + "exists but is not a directory. The application will not be able to continue functioning.");
     }
 }
