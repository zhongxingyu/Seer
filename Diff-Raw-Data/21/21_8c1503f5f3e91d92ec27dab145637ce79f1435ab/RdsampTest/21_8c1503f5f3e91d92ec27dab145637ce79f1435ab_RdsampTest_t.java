 package at.jku.pervasive.ecg.wfdb;
 
 import java.io.File;
 
 public class RdsampTest extends PhysioToolkitTestCase {
 
   public void testRdsamp() throws Exception {
    File testFile = getFile("/chf03.dat");
     physioToolkit.rdsamp(testFile);
   }
 
 }
