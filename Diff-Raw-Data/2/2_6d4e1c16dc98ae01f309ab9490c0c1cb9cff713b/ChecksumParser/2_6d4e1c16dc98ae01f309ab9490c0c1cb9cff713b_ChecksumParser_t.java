 package dk.statsbiblioteket.doms.transformers.common.checksums;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.zip.ZipInputStream;
 
 /**
  * Created by IntelliJ IDEA.
  * User: abr
  * Date: 7/18/12
  * Time: 3:24 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ChecksumParser {
 
     private Map<String,String> nameChecksumsMap = new HashMap<String, String>();
 
     public ChecksumParser(InputStream checksumsZipStream) throws IOException {
         ZipInputStream zipInputStream = new ZipInputStream(checksumsZipStream);
        // This probably only works when the zip-file contains exacly one (1) file
         zipInputStream.getNextEntry();
 
         BufferedReader reader = new BufferedReader(new InputStreamReader(zipInputStream));
         String line;
         while ((line = reader.readLine()) != null){
             String[] splits = line.split(" ");
             String name = splits[1].trim();
             String checksum = splits[0].trim();
             if (name.endsWith(".log")){
                 continue;
             }
             nameChecksumsMap.put(name, checksum);
         }
         System.out.println(nameChecksumsMap.size());
     }
 
     public Map<String, String> getNameChecksumsMap() {
         return Collections.unmodifiableMap(nameChecksumsMap);
     }
 }
