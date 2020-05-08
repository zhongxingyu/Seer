 package dk.statsbiblioteket.scape.tika;
 
 import org.apache.tika.detect.DefaultDetector;
 import org.apache.tika.io.TikaInputStream;
 import org.apache.tika.metadata.Metadata;
 import org.apache.tika.mime.MediaType;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: abr
  * Date: 12/16/11
  * Time: 11:46 AM
  * To change this template use File | Settings | File Templates.
  */
 public class SimpleWrapper {
 
 
     private DefaultDetector detector;
 
 
 
     public SimpleWrapper() {
         detector = new DefaultDetector();
     }
 
 
     public Identity detect(File input) throws IOException {
         long before = System.currentTimeMillis();
         Metadata metadata = new Metadata();
        metadata.set(Metadata.RESOURCE_NAME_KEY,"resource");

        FileInputStream fileInputStream = new FileInputStream(input);
        TikaInputStream stream = TikaInputStream.get(fileInputStream);
         MediaType mediaType = detector.detect(stream, metadata);
         stream.close();
         String mime = mediaType.toString().intern();
         long duration = System.currentTimeMillis() - before;
         return new Identity(input,mime,duration);
 
     }
 
 
     public static void main(String... args) throws Exception {
         String govDocsData = null;
         if (args.length == 1) {
             govDocsData = args[0];
         }
         SimpleWrapper sw = new SimpleWrapper();
         sw.identify(govDocsData);
 
     }
 
     public  List<Identity> identify(String govDocsData) throws Exception {
 
         List<File> datafiles = getFiles(new File(govDocsData));
         return identify(datafiles);
 
     }
 
 
     public List<Identity> identify(List<File> datafiles) throws Exception {
 
         List<Identity> identities = new ArrayList<Identity>();
 
         SimpleWrapper sw = new SimpleWrapper();
 
         for (File file : datafiles) {
             if (!file.isFile()){
                 continue;
             }
             Identity detection = sw.detect(file);
             identities.add(detection);
             System.out.println(file.getAbsolutePath()+":"+detection.getMime());
         }
         return identities;
 
 
     }
 
 
     private static List<File> getFiles(File dir){
         List<File> result = new ArrayList<File>();
         if (!dir.isDirectory()){
             result.add(dir);
             return result;
         }
         File[] files = dir.listFiles();
         for (File file : files) {
             result.addAll(getFiles(file));
         }
         Collections.sort(result);
         return result;
     }
 
 
 
 }
