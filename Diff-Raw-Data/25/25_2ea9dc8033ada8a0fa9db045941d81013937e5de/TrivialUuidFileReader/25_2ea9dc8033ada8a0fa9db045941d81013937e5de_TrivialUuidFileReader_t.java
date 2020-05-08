 package dk.statsbiblioteket.doms.transformers.common;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Read UUIDs from file using simple readline.
  */
 public class TrivialUuidFileReader implements UuidFileReader {
     @Override
     public List<String> readUuids(File file) throws IOException {
         List<String> uuids = new ArrayList<String>();
         BufferedReader reader = new BufferedReader(new FileReader(file));
         String line;
         while ((line = reader.readLine()) != null) {
            if( !line.isEmpty()) {
                uuids.add(line);                
            }
         }
         return uuids;
     }
 }
