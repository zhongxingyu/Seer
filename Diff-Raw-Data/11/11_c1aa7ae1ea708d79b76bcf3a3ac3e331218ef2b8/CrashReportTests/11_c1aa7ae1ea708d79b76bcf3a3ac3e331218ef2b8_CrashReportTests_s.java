 package chum.util;
 
 import android.test.AndroidTestCase;
 
 import java.io.File;
 
 
 public class CrashReportTests extends AndroidTestCase {
 
 
     protected void setUp() throws Exception {
         super.setUp();
 
     }
 
 
 
     protected void tearDown() throws Exception {
 
         super.tearDown();
     }
 
 
 
     public void testGetCrashDir() {
         File dir = CrashReport.getCrashDir(getContext());
         assertNotNull(dir);
         assertTrue(dir.canRead());
         assertTrue(dir.canWrite());
     }
 
 
     public void testCreateCrashReport() {
         CrashReport.deleteAll(getContext());
 
         CrashReport reportOut = CrashReport.create(getContext());
         assertNotNull(reportOut);
 
         assertNull("Report initially has no file",reportOut.file);
         assertNull("Content is initially empty",reportOut.contents);
 
         // Save the report
         String contents = ("This is a test\n"+
                           "Second line");
         reportOut.save(contents);
         assertNull(reportOut.ioError);
         assertEquals("Saved contents should match",
                      contents,reportOut.contents);
         assertNotNull("Report has a file",reportOut.file);
         assertTrue("File was written",reportOut.file.exists());
 
         // Load the written report
         CrashReport[] reports = CrashReport.find(getContext());
         assertNotNull(reports);
         assertEquals("Should get 1 report", 1, reports.length);
 
         CrashReport reportIn = reports[0];
         assertNull(reportIn.ioError);
         assertNotNull("Should come from a file",reportIn.file);
         assertTrue("It's a real file",reportIn.file.exists());
         assertNotNull("Should have contents",reportIn.contents);
         assertEquals("Loaded contents match saved",
                      reportOut.contents,
                      reportIn.contents);
                      
     }
 }
 
