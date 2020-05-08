 package my.triviagame.xmcd;
 
 import com.google.common.collect.Lists;
 import java.util.Iterator;
 import java.util.List;
 import junit.framework.Assert;
 import my.triviagame.dal.TrackRow;
 import org.junit.Ignore;
 import org.junit.Test;
 
 /**
  * Tests {@link XmcdFilters}.
  */
 public class XmcdFiltersTest {
 
     @Test
     public void testChainAllFilters() throws Throwable {
         XmcdDiscArchive discStream = new XmcdDiscArchive(TestUtilities.getUpdate_20120401_20120501());
         Iterator<XmcdDisc> filtered = new XmcdFilters.Factory(discStream)
                 .hasYear()
                 .notVarious()
                .notRemix()
                 .notBadGenre()
                 .stripTrackVariant()
                 .firstN(5000)
                 .chain();
         List<XmcdDisc> filteredList = Lists.newArrayList(filtered);
         Assert.assertEquals(5000, filteredList.size());
         for (XmcdDisc disc : filteredList) {
             Assert.assertFalse(disc.albumRow.year == XmcdDisc.INVALID_YEAR);
         }
         for (XmcdDisc disc : filteredList) {
             Assert.assertFalse(disc.albumRow.artistName.startsWith("Various"));
         }
         for (XmcdDisc disc : filteredList) {
            Assert.assertFalse(disc.albumRow.title.contains("Remix"));
        }
        for (XmcdDisc disc : filteredList) {
             Assert.assertTrue(XmcdFilters.GENRE_WHITELIST.contains(disc.albumRow.freeTextGenre));
             Assert.assertFalse(XmcdFilters.GENRE_CONVERSION.containsKey(disc.albumRow.freeTextGenre));
         }
         for (XmcdDisc disc : filteredList) {
             for (TrackRow track : disc.trackRows) {
                 Assert.assertFalse(XmcdRegEx.TRACK_VARIANT.matcher(track.title).matches());
             }
         }
     }
     
     /**
      * Tests filtering of a complete FreeDB archive.
      * References a file that exists outside the source control and should exist in the local source tree.
      * Takes a long time to complete, hence the @ignore annotation.
      */
     @Test
     @Ignore
     public void testDefaultFiltersCompleteArchive() throws Throwable {
         XmcdDiscArchive discStream = new XmcdDiscArchive(TestUtilities.getComplete_20120601());
         Iterator<XmcdDisc> filtered = new XmcdFilters.Factory(discStream).defaultFilters().chain();
         int recordsCount = 0;
         int badRecordsCount = 0;
         while (filtered.hasNext()) {
             try {
                 filtered.next();
             } catch (XmcdException e) {
                 e.printStackTrace();
                 badRecordsCount++;
             }
             recordsCount++;
             if (recordsCount % 10000 == 0) {
                 System.out.println(recordsCount + " records passed filtering so far");
                 System.out.println(badRecordsCount + " bad records so far");
             }
         }
         System.out.println(recordsCount + " filtered records total in complete archive");
         System.out.println(badRecordsCount + " bad records total in complete archive");
         discStream.close();
     }
 }
