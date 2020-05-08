 package at.jku.pervasive.ecg.wfdb;
 
 import java.io.File;
 
 public class RdsampTest extends PhysioToolkitTestCase {
 
   public void testRdsamp() throws Exception {
    File testFile = getFile("/test.edf");
     physioToolkit.rdsamp(testFile);
   }
 
 }
