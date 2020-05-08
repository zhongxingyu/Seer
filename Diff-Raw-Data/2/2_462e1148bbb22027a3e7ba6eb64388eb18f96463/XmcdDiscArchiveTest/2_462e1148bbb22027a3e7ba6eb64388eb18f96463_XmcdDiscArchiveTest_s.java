 package my.triviagame.xmcd;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 /**
  * Tests {@link XmcdDiscArchive}.
  */
 public class XmcdDiscArchiveTest {
     
     @Test
     public void testFullExtractionFromSampleArchive() throws Throwable {
         XmcdDiscArchive xmcdArchive = new XmcdDiscArchive(TestUtilities.getUpdate_20120401_20120501(), false);
         int recordsCount = 0;
         while (xmcdArchive.hasNext()) {
             xmcdArchive.next();
             recordsCount++;
         }
         xmcdArchive.close();
         System.out.println(recordsCount + " records total in update");
     }
     
     @Test
     public void testFullExtractionFromSampleArchiveOnlyAscii() throws Throwable {
         XmcdDiscArchive xmcdArchive = new XmcdDiscArchive(TestUtilities.getUpdate_20120401_20120501(), true);
         int recordsCount = 0;
         while (xmcdArchive.hasNext()) {
             xmcdArchive.next();
             recordsCount++;
         }
         xmcdArchive.close();
         System.out.println(recordsCount + " ASCII records in update");
     }
     
     /**
      * Tests extraction & parsing of a complete FreeDB archive.
      * References a file that exists outside the source control and should exist in the local source tree.
      * Takes a long time to complete, hence the @ignore annotation.
      */
     @Ignore
     @Test
     @SuppressWarnings("CallToThreadDumpStack")  // For printing parsing failures
     public void testCompleteArchiveExtraction() throws Throwable {
         XmcdDiscArchive xmcdArchive = new XmcdDiscArchive(TestUtilities.getComplete_20120601(), false);
         int discsCount = 0;
         int badDiscsCount = 0;
         int tracksCount = 0;
         while (xmcdArchive.hasNext()) {
             try {
                 XmcdDisc disc = xmcdArchive.next();
                 tracksCount += disc.trackRows.size();
             } catch (XmcdException e) {
                 e.printStackTrace();
                 badDiscsCount++;
             }
             discsCount++;
             if (discsCount % 10000 == 0) {
                 System.out.println(discsCount + " discs parsed so far");
                System.out.println(badDiscsCount + " discs records so far");
             }
         }
         System.out.println(discsCount + " discs total in complete archive");
         System.out.println(badDiscsCount + " bad discs total in complete archive");
         System.out.println(tracksCount + " tracks total in complete archive");
     }
 }
