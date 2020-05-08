 package bjornno.javabatch;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.Reader;
 
 
 public class BatchFileReader {
     private String directory;
 
     public BatchFileReader(String directory) {
         this.directory = directory;
         new File(directory).mkdirs();
     }
 
     public Reader getOneFile() {
         File dir = new File(directory);
 
         String[] children = dir.list();
        if (children != null) {
             String firstFileName = children[0];
             File file = new File(directory+"\\"+firstFileName);
             try {
                 file.renameTo(new File(directory+"\\temp\\"+firstFileName));
                 return new FileReader(file);
             } catch (FileNotFoundException e) {
                 throw new RuntimeException(e);
             }
         }
         return null;
     }
 }
