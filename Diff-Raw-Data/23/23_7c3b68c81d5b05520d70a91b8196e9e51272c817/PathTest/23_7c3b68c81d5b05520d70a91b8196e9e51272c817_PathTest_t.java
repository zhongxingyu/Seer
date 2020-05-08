 
 package edu.msoe.se2800.h4;
 
 import edu.msoe.se2800.h4.jplot.JPoint;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.awt.Point;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.testng.Assert.*;
 
 public class PathTest {
 
     private Path mPath = Path.INSTANCE;
 
     @BeforeClass
     public void setupPath() {
 
         //Ensure we have a fresh start
         mPath.reset();
 
         // add 10 points along a straight line
         for (int i = 0; i < 10; i++) {
             mPath.add(new JPoint(i * 10, i * 10));
         }
     }
 
     @BeforeMethod
     public void beforeMethod() {
         mPath.reset();
     }
 
     @Test(expectedExceptions = {
             NullPointerException.class
     }, description = "Tests passing a null parameter. Expects a NullPointerException")
     public void writeToFileNullParameter() {
         mPath.writeToFile(null);
     }
 
     @Test(description = "This is a test writing under 'normal' conditions")
     public void writeAsExpected() throws IOException {
         File file = new File("TestFile.scrumbot");
         file.deleteOnExit();
         assertTrue(mPath.writeToFile(file));
 
         // Make sure we have the number of lines we expect
         int count = lineCount(file);
         int expectedCount = mPath.size() + 1;
         assertEquals(count, expectedCount, "Test the line counts");
     }
 
     @Test(enabled = false)
     public void writeToReadOnlyFile() {
         // TODO Marius: test writing to read only files
     }
 
     @Test(description = "Tries to write to a non-existent file")
     public void writeToNonExistentFile() {
         assertFalse(mPath.writeToFile(new File("")));
     }
 
 
     @Test(expectedExceptions = {
             NullPointerException.class
     })
     public void testReadNull() throws Path.BadFormatException, FileNotFoundException {
         mPath.readFromFile(null);
     }
 
     @Test(expectedExceptions = {
             FileNotFoundException.class
     })
     public void testNonExistentFile() throws Path.BadFormatException, FileNotFoundException {
         File f = new File("aaa");
         mPath.readFromFile(f);
     }
 
     //
 //    @Test(expectedExceptions = {
 //            UnsupportedEncodingException.class
 //    })
 //    public void testBadFormatHeaderLine(){
 //
 //    }
 //
     @Test(expectedExceptions = {
             Path.BadFormatException.class
     })
     public void testBadFormatCoordinates() throws Path.BadFormatException, FileNotFoundException {
         File f = new File("./resourcesTest/BadCoordinates.scrumbot");
         try {
             mPath.readFromFile(f);
         } catch (Path.BadFormatException e) {
             assertEquals(mPath.size(), 0);
             throw e;
         }
     }
 
     @Test
     public void testEmptyPath() throws Path.BadFormatException, FileNotFoundException {
         File f = new File("./resourcesTest/ExamplePathFileNoCoordinates.scrumbot");
         mPath.readFromFile(f);
     }
 
     @Test
     public void testMultipleCoordinates() throws Path.BadFormatException, FileNotFoundException {
         File f = new File("./resourcesTest/ReadOnlyFile.scrumbot");
 
         mPath.readFromFile(f);
         FileReader f2 = new FileReader(f);
         BufferedReader br = new BufferedReader(f2);
         int lines = 0;
         try {
             while (br.readLine() != null) {
                 lines++;
             }
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         assertEquals(mPath.size(), lines - 1);
     }
 
     /*
      * This was taken from StackTrace & slightly modified
      * http://stackoverflow.com
      * /questions/453018/number-of-lines-in-a-file-in-java
      */
     private int lineCount(File file) throws IOException {
         InputStream is = new BufferedInputStream(new FileInputStream(file));
         try {
             byte[] c = new byte[1024];
             int count = 0;
            int readChars;
             boolean empty = true;
             while ((readChars = is.read(c)) != -1) {
                 empty = false;
                 for (int i = 0; i < readChars; ++i) {
                     if (c[i] == '\n')
                         ++count;
                 }
             }
             return (count == 0 && !empty) ? 1 : count;
         } finally {
             is.close();
         }
     }

     @Test
     public void testReset() {
     	List<JPoint> points = new ArrayList<JPoint>();
     	JPoint point1 = new JPoint();
     	JPoint point2 = new JPoint();
     	points.add(point1);
     	points.add(point2);
     	mPath.reset();
     }
     
     @Test
     public void testAdd(){
     	JPoint point1 = new JPoint();
     	mPath.add(point1);
     }
     
     @Test
     public void testGetNull(){
     	mPath.get(0);
     }
     
     @Test
     public void testGet(){
     	mPath.add(new JPoint());
     	List<JPoint> points = mPath.getPoints();
     	points.get(0);
     }
     
     @Test
     public void testSize(){
     	mPath.add(new JPoint());
     	mPath.size();
     	
     }
 
 
     @AfterClass
     public void resetPath() {
         mPath.reset();
     }
 
 }
