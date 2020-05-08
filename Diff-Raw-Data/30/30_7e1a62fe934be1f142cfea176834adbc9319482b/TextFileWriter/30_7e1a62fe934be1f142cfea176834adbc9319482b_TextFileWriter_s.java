 package filemanagmentsystem;

 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;

 /**
 *
  * @author bspor
  */
 public class TextFileWriter <T> implements FileWriterStrategy {
     private static final String CRLF = "\n";
     @Override
    public void writeFile(String filePath, List type, boolean append) 
            throws FileNotFoundException, IOException {
         File data = new File(filePath);
         PrintWriter out = new PrintWriter(
                 new BufferedWriter(new FileWriter(data, append)));
         try {
             List<T> myList = new ArrayList<>(type);
             // if file doesnt exists, then create it
             if (!data.exists()) {
                 data.createNewFile();
             }
             
             for (T t: myList) {
                //System.out.println(s + "What");
                //Put a break for each line
                 out.write(t + CRLF);
             }
             out.close();
 
         } catch (IOException e) {
             System.out.println(e.getMessage()); //Get rid of this
             throw new IllegalArgumentException(e.getMessage());
         }
     }
     
     //Get rid
     public static void main(String[] args) throws FileNotFoundException, IOException {
         //C:\Users\Brandon\Documents\NetBeansProjects\FileManagementSystem2\FileManagmentSystem\src\filemanagmentsystem\test
         String filePath = "src" + File.separatorChar + "filemanagmentsystem"
             + File.separatorChar + "test2";
         String filePath2 = "src" + File.separatorChar + "filemanagmentsystem"
             + File.separatorChar + "test";
         String test = "src" + File.separatorChar + "filemanagmentsystem"
             + File.separatorChar + "test_comma_only.csv";
         FileReaderStrategy frs = new TextFileReader();
         //System.out.println(frs.readFile(filePath));
         FileWriterStrategy fws = new TextFileWriter();
         FileFormatStrategy ffs = new CustomCSVFormat();
         
         //fws.writeFile(filePath + "3", frs.readFile(filePath2), false);
         
       // ffs.readFormatedFile(frs.readFile(filePath));
        
        //fws.writeFile(filePath + "3", ffs.encode(frs.readFile(test)), true);
 //        
         //fws.writeFile(filePath, frs.readFile(filePath2), false);
         //System.out.println("Done Did");
     }
 }
