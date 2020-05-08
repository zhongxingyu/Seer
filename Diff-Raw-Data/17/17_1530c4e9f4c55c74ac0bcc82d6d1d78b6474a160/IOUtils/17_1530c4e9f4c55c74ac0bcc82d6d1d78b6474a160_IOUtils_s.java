 package bg.uni.sofia.fmi.xml.filesystem2html.model;
 
 import java.io.File;
 import java.io.FileNotFoundException;
import java.io.FilenameFilter;
 import java.io.IOException;
import java.util.Arrays;
 import junit.framework.Assert;
 
 /**
  *
  * @author Leni Kirilov
  */
 public class IOUtils {
 
     private static String PREFIX = "FileSystem2Html";
 
     /**
      * Creates temp file in the User temp dir.
      * @param fileName
      * @return
      * @throws IOException 
      */
     public static File createTestFile(String fileName) throws IOException {
         String tempDirPath = getTempDir().getAbsolutePath();
         File testFile = new File(tempDirPath, PREFIX + fileName);
         testFile.deleteOnExit();
         Assert.assertTrue("File was not created successfully", testFile.createNewFile());
 
         return testFile;
     }
 
     public static File createTestDir(String dirName) throws IOException {
         String tempDirPath = getTempDir().getAbsolutePath();
         File testDir = new File(tempDirPath, PREFIX + dirName);
         testDir.deleteOnExit();
         Assert.assertTrue("Directory was not created successfully", testDir.mkdirs());
 
         return testDir;
     }
 
     // Private methods
     public static File getTempDir() {
         String tempDirPath = System.getProperty("java.io.tmpdir");
         return new File(tempDirPath);
     }
 
 //    public static void emptyTempDir() {
 //        String[] toDelete = getTempDir().list(new FilenameFilter() {
 //
 //            public boolean accept(File dir, String name) {
 //                return name.startsWith(PREFIX);
 //            }
 //        });
 //        
 //        System.out.println(toDelete[0]);
 //    }
 
     public static void deleteRecursively(File file) throws FileNotFoundException {
         if (file.isDirectory()) {
             for (File c : file.listFiles()) {
                 deleteRecursively(c);
             }
         }
         if (!file.delete()) {
             throw new FileNotFoundException("Failed to delete file: " + file);
         }
     }
     
 //    public static void main(String[] args) {
 //        emptyTempDir();
 //    }
 }
