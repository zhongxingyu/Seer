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
            log.debug(
                    "directory " + FILE_STORE + " doesn't exist, creating it NOW!!!1!one!!");
             FileUtils.forceMkdir(dir);
            log.debug("Directory " + FILE_STORE + " created without issue");
         }
         if (dir.isDirectory()) {
            log.debug("It's a directory");
             if (dir.canRead()) {
                log.debug("And it's readable");
                 if (dir.canWrite()) {
                    log.debug("And writable!");
                     return true;
                 }
                log.debug("Can't write to directory, wtf!?!");
             }
            log.debug("Cannot read directory, crap");
         }
        log.debug("it is Not a directory, fix this");
        return false;
     }
 }
