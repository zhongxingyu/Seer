 package uk.ac.ebi.fgpt.sampletab;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.fgpt.sampletab.utils.FileUtils;
 import junit.framework.TestCase;
 
 
 public class TestFileUtils extends TestCase {
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     public void testFileFilterRegex() {
 
         //first create a bunch of likely files
         
         //First
             //A1.txt
             //A2.txt
         //Second
             //A1.txt
             //A2.txt
         
         File first = new File("First");
         if (!first.exists()) first.mkdir();
         File firstA1 = new File(first, "A1.txt");
         File firstA2 = new File(first, "A2.txt");
         File second = new File("Second");
         if (!second.exists()) second.mkdir();
         File secondA1 = new File(second, "A1.txt");
         File secondA2 = new File(second, "A2.txt");
         try {
             firstA1.createNewFile();
             firstA2.createNewFile();
             secondA1.createNewFile();
             secondA2.createNewFile();
         } catch (IOException e) {
             log.error("unable to create files for testing");
             e.printStackTrace();
             fail();
         }
         
         
         //now check the results are what we expect
         List<File> FA1A2 = new ArrayList<File>();
        FA1A2.add(new File("First/A1.txt"));
        FA1A2.add(new File("First/A2.txt"));
        assertEquals(FA1A2, FileUtils.getMatchesRegex("First/.*\\.txt"));
         assertEquals(FA1A2, FileUtils.getMatchesGlob("First/*.txt"));
         
         List<File> FA1 = new ArrayList<File>();
        FA1.add(new File("First/A1.txt"));
         assertEquals(FA1, FileUtils.getMatchesRegex("First/A1.txt"));
         assertEquals(FA1, FileUtils.getMatchesGlob("First/A1.txt"));
 
         List<File> FA1SA1 = new ArrayList<File>();
        FA1SA1.add(new File("First/A1.txt"));
        FA1SA1.add(new File("Second/A1.txt"));
         assertEquals(FA1SA1, FileUtils.getMatchesGlob("*/A1.txt"));
         
         List<File> FA1A2SA1A2 = new ArrayList<File>();
        FA1A2SA1A2.add(new File("First/A1.txt"));
        FA1A2SA1A2.add(new File("First/A2.txt"));
        FA1A2SA1A2.add(new File("Second/A1.txt"));
        FA1A2SA1A2.add(new File("Second/A2.txt"));
         assertEquals(FA1A2SA1A2, FileUtils.getMatchesGlob("*/A*.txt"));
         
         
     }
 
 }
