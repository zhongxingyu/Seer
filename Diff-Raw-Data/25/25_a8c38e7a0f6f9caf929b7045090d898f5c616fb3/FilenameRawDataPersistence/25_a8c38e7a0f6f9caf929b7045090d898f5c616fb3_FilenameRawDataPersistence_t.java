 package uk.org.sappho.code.change.management.data.persistence;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
import com.google.inject.Inject;

 import uk.org.sappho.code.change.management.data.RawData;
 
 public class FilenameRawDataPersistence extends RawDataPersistence {
 
     private final String filename;
     private String zipFilename;
 
    @Inject
     public FilenameRawDataPersistence(String filename) {
 
         super();
         this.filename = filename;
         zipFilename = null;
         if (filename.endsWith(".zip")) {
             zipFilename = new File(filename.replaceFirst(".zip$", ".xml")).getName();
         }
     }
 
     public RawData load() throws IOException {
 
         return load(new FileInputStream(filename), zipFilename);
     }
 
     public void save(RawData rawData) throws IOException {
 
         save(rawData, new FileOutputStream(filename), zipFilename);
     }
 
     @Override
     public String getDescription() {
 
         return filename;
     }
 }
