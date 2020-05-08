 package xite;
 
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import com.github.enr.xite.util.Directories;
 import com.google.common.io.Files;
 
 @Test(suiteName = "Util package")
 public class DirectoriesTest {
 
     private static FileFilter javaFileFilter = new FileFilter() {
         public boolean accept(File file) {
             // if (file.isDirectory()) return true;
             return ((file.isFile()) && (file.getAbsolutePath().endsWith(".java")));
         }
     };
 
     private String thisFile;
 
     @BeforeClass
     public void initData() {
         thisFile = "." + File.separator + "src" + File.separator + "test" + File.separator + "groovy" + File.separator
                 + this.getClass().getCanonicalName().replace(".", File.separator) + ".java";
     }
 
     @Test
     public void testFilterdListing() {
         File startingDirectory = new File(".");
         List<File> files = Directories.list(startingDirectory, javaFileFilter, true);
 
         List<String> filePaths = new ArrayList<String>();
         // assertTrue(files.contains(new File("pom.xml")));
         // print out all file names, in the the order of File.compareTo()
         for (File file : files) {
             filePaths.add(file.getPath());
         }
         assertFalse(filePaths.contains("./core.gradle"));
         assertTrue(filePaths.contains(thisFile));
 
     }
 
     @Test
     public void testListing() {
         File startingDirectory = new File(".");
         List<File> files = Directories.list(startingDirectory);
 
         List<String> fileNames = new ArrayList<String>();
         for (File file : files) {
             // System.out.println(file.getPath());
             fileNames.add(file.getName());
         }
         assertTrue(fileNames.contains("core.gradle"));
 
     }
 
     @Test
     public void testIsEmpty() {
         File pwd = new File(".");
         assertFalse(Directories.isEmpty(pwd));
         File newDir = Files.createTempDir();
         assertTrue(Directories.isEmpty(newDir));
     }
 
 }
