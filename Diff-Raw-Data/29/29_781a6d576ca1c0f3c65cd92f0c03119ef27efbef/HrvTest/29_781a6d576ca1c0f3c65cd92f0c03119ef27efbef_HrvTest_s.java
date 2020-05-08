 package at.jku.pervasive.ecg.wfdb;
 
 import java.io.File;
 
 import org.joda.time.LocalTime;
 
 public class HrvTest extends PhysioToolkitTestCase {
 
   public void testBaseDirectoryForRecord() throws Exception {
     File chf03 = getWFDBFile("/chf03.dat");
     HRVOptions options = new HRVOptions("chf03", "ecg");
     options.setBaseDirectory(chf03.getParentFile());
     HRV hrv = physioToolkit.hrv(options);
 
     assertNotNull(hrv);
     assertEquals(0.892769, hrv.getAVNN(), 0.000001D);
     assertEquals(0.0612485, hrv.getSDNN(), 0.000001D);
   }
 
  public void testHrv() throws Exception {
    File testFile = getWFDBFile("/test.dat");
    HRV hrv = physioToolkit.hrv(testFile, "qrs");

    assertNotNull(hrv);
    assertEquals(1.31832D, hrv.getAVNN(), 0.001D);
    assertEquals(0.689728D, hrv.getSDNN(), 0.001D);
  }

  public void testHrvWithTimes() throws Exception {
    File testFile = getWFDBFile("/test.edf");
    HRV hrv = physioToolkit.hrv(testFile, "qrs", new LocalTime(0, 0),
        new LocalTime(7, 0));

    assertNotNull(hrv);
    assertEquals(1.31832D, hrv.getAVNN(), 0.001D);
    assertEquals(0.689728D, hrv.getSDNN(), 0.001D);
  }

  public void testHrvWithTimes2() throws Exception {
    File testFile = getWFDBFile("/test.edf");
    HRV hrv = physioToolkit.hrv(testFile, "qrs", new LocalTime(1, 0),
        new LocalTime(2, 0));

    assertNotNull(hrv);
    assertEquals(1.41793D, hrv.getAVNN(), 0.001D);
    assertEquals(0.810737D, hrv.getSDNN(), 0.001D);
  }

   public void testLargeFile() throws Exception {
     File chf03 = getWFDBFile("/chf03.dat");
     HRV hrv = physioToolkit.hrv(chf03, "ecg");
 
     assertNotNull(hrv);
     assertEquals(0.892769, hrv.getAVNN(), 0.000001D);
     assertEquals(0.0612485, hrv.getSDNN(), 0.000001D);
   }
 
   public void testLargeFileHRVOptions() throws Exception {
     File chf03 = getWFDBFile("/chf03.dat");
     HRVOptions options = new HRVOptions(chf03, "ecg");
     HRV hrv = physioToolkit.hrv(options);
 
     assertNotNull(hrv);
     assertEquals(0.892769, hrv.getAVNN(), 0.000001D);
     assertEquals(0.0612485, hrv.getSDNN(), 0.000001D);
   }
 
   public void testLargeFileWithOutlierDetection() throws Exception {
     File chf03 = getWFDBFile("/chf03.dat");
     HRV hrv = physioToolkit.hrv(chf03, "ecg", "-f 0.2 20 -x 0.4 2.0",
         "-p 20 50");
 
     assertNotNull(hrv);
     assertEquals(0.892206, hrv.getAVNN(), 0.000001D);
     assertEquals(0.054712, hrv.getSDNN(), 0.000001D);
   }
 
   public void testLargeFileWithOutlierDetectionAndStartAndEndTime()
       throws Exception {
     File chf03 = getWFDBFile("/chf03.dat");
     HRV hrv = physioToolkit.hrv(chf03, "ecg", new LocalTime(0, 0),
         new LocalTime(1, 0), "-s", "-M", "-f 0.2 20 -x 0.4 2.0", "-p 20 50");
 
     assertNotNull(hrv);
     assertEquals(867.958D, hrv.getAVNN(), 0.000001D);
     assertEquals(39.7191D, hrv.getSDNN(), 0.000001D);
   }
 
 }
