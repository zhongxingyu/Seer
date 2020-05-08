 package at.jku.pervasive.ecg.wfdb;
 
 import java.io.File;
 
 public class SqrsTest extends PhysioToolkitTestCase {
 
   public void testSqrs() throws Exception {
    File testFile = getFile("/chf03.dat");
     physioToolkit.sqrs(testFile);
   }
 
 }
