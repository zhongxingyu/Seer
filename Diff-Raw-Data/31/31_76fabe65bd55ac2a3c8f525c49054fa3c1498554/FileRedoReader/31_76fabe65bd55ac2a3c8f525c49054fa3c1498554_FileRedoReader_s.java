 package org.sakaiproject.nakamura.lite.storage.jdbc.migrate;
 
 import java.io.DataInputStream;
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Ordering;
 
 public class FileRedoReader {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(FileRedoLogger.class);
     private File location;
 
     public FileRedoReader(File location) {
         this.location = location;
     }
 
     public void analyse() throws IOException {
         List<File> sortedFileList = Ordering.from(new Comparator<File>() {
                     public int compare(File arg0, File arg1) {
                         return arg0.getAbsolutePath().compareTo(arg1.getAbsolutePath());
                     }
                }).sortedCopy(ImmutableList.of(location.listFiles()));
         for (File f : sortedFileList) {
             try {
                 DataInputStream din = new DataInputStream(new FileInputStream(f));
                 Map<String, Map<String, Object>> logMap = Maps.newLinkedHashMap();
                 for (;;) {
 
                     if (LogFileRecord.read(din, logMap)) {
                         LOGGER.info("Committed {} ", logMap);
                     } else {
                         LOGGER.info("Not Committed {} ", logMap);
                     }
                 }
             } catch (FileNotFoundException e1) {
                 LOGGER.info("Log File Missing Reading {} ", f);
             } catch (EOFException e) {
                 LOGGER.info("Finished Reading {} ", f);
             } catch (IOException e) {
                 LOGGER.info("Error Reading {} {} ", f, e.getMessage());
                 throw e;
             }
         }
     }
 
 }
