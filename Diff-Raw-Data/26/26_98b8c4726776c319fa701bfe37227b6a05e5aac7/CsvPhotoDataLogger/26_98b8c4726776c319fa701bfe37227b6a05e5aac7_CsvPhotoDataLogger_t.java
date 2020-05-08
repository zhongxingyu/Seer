 package com.abudko.scheduled.csv;
 
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.csvreader.CsvReader;
 import com.csvreader.CsvWriter;
 
 public class CsvPhotoDataLogger implements PhotoDataLogger {
 
     private static final String PHOTO_ID = "photoId";
     private static final String GROUP_ID = "groupId";
     private static final char SEPARATOR = '\'';
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     private final String fileLocation;
 
     public CsvPhotoDataLogger(String fileLocation) {
         this.fileLocation = fileLocation;
     }
 
     @Override
     public void dump(Map<String, String> photoIdGroupIdMap) {
         CsvWriter csvWriter = null;
         try {
            URL resource = getURL();
 
             log.info(String.format("Dumping photoIds to a file [%s]", resource.getPath()));
 
             csvWriter = new CsvWriter(new FileWriter(resource.getPath()), SEPARATOR);
             csvWriter.writeRecord(new String[] { PHOTO_ID, GROUP_ID });
             Set<Entry<String, String>> entrySet = photoIdGroupIdMap.entrySet();
             for (Entry<String, String> e : entrySet) {
                 String photoId = e.getKey();
                 String groupId = e.getValue();
                 csvWriter.writeRecord(new String[] { photoId, groupId });
             }
         } catch (FileNotFoundException fe) {
             log.error("Error in parsing csv", fe);
         } catch (IOException ioe) {
             log.error("Error in parsing csv", ioe);
         } finally {
             if (csvWriter != null) {
                 csvWriter.close();
             }
         }
     }
 
     @Override
     public Map<String, String> read() {
         CsvReader csvReader = null;
         Map<String, String> photoIdGroupIdMap = new LinkedHashMap<String, String>();
         try {
            URL resource = getURL();
             InputStream stream = resource.openStream();
 
             log.info(String.format("Reading photoIds from a file [%s], inputstream [%s]", resource.getPath(), stream));
 
             csvReader = new CsvReader(stream, SEPARATOR, Charset.forName("UTF-8"));
             csvReader.readHeaders();
 
             while (csvReader.readRecord()) {
                 photoIdGroupIdMap.put(csvReader.get(PHOTO_ID), csvReader.get(GROUP_ID));
             }
         } catch (FileNotFoundException fe) {
             log.error("Error in parsing csv", fe);
         } catch (IOException ioe) {
             log.error("Error in parsing csv", ioe);
         } finally {
             if (csvReader != null) {
                 csvReader.close();
             }
         }
 
         return photoIdGroupIdMap;
     }
    
    private URL getURL() throws MalformedURLException {
        if (fileLocation != null && fileLocation.contains("classpath:")) {
            String path = fileLocation.substring(fileLocation.indexOf(":") + 1);
            return this.getClass().getResource(path);
        }
        
        return new URL(fileLocation);
    }
 }
