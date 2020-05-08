 package my.triviagame.xmcd;
 
 import junit.framework.Assert;
 import my.triviagame.dal.AlbumRow;
 import my.triviagame.dal.TrackRow;
 import org.junit.Test;
 
 /**
  * Tests {@link XmcdDisc}.
  */
 public class XmcdDiscTest {
 
     /**
      * Tests that generating a disc from an empty xmcd file fails.
      */
     @Test
     public void testConstructionFromEmptyFileFails() throws Throwable {
         try {
             XmcdDisc.fromXmcdFile("", FreedbGenre.BLUES);
             // Expecting an exception
             Assert.fail();
         } catch (XmcdException e) {
         }
     }
 
     /**
      * Tests that generating a disc from a badly formatted xmcd file fails.
      */
     @Test
     public void testConstructionFromBadFileFails() throws Throwable {
         try {
             XmcdDisc.fromXmcdFile("blah!\nSome more text\n\n\nmore blah!", FreedbGenre.CLASSICAL);
             // Expecting an exception
             Assert.fail();
         } catch (XmcdException e) {
         }
     }
 
     /**
      * Tests that generating a disc from a sample good xmcd file works.
      */
     @Test
     public void testConstructionFromSampleFile1() throws Throwable {
         String xmcd0a0d7d14 = TestUtilities.get_0a0d7d14();
         XmcdDisc disc = XmcdDisc.fromXmcdFile(xmcd0a0d7d14, FreedbGenre.REGGAE);
         
         // Set expectations for the album part
         AlbumRow expectedAlbumRow = new AlbumRow();
         expectedAlbumRow.freedbGenre = (byte)FreedbGenre.REGGAE.ordinal();
         expectedAlbumRow.revision = 0;
         expectedAlbumRow.freedbId = 0x0a0d7d14;
         expectedAlbumRow.artistName = "Kinks, The";
         expectedAlbumRow.title = "Face To Face: Deluxe Edition";
         expectedAlbumRow.year = 1966;
         expectedAlbumRow.freeTextGenre = "Pop";
         Assert.assertEquals(expectedAlbumRow, disc.albumRow);
 
         // Set expectations for some of the tracks
         TrackRow expectedTrackRow0 = new TrackRow();
         expectedTrackRow0.trackNum = 0;
         expectedTrackRow0.title = "Party Line (Mono)";
         expectedTrackRow0.artistName = "Kinks, The";
         expectedTrackRow0.lenInSec = 2 * 60 + 38;
        expectedTrackRow0.albumRow = expectedAlbumRow;
         Assert.assertEquals(expectedTrackRow0, disc.trackRows.get(0));
         TrackRow expectedTrackRow19 = new TrackRow();
         expectedTrackRow19.trackNum = 19;
         expectedTrackRow19.title = "Dead End Street (Alternative Version)";
         expectedTrackRow19.artistName = "Kinks, The";
         expectedTrackRow19.lenInSec = 2 * 60 + 56;
        expectedTrackRow19.albumRow = expectedAlbumRow;
         Assert.assertEquals(expectedTrackRow19, disc.trackRows.get(19));
     }
     
     /**
      * Tests another sample xmcd file.
      * This test doesn't bother verifying fields, it just checks that nothing blows up.
      */
     @Test
     public void testConstructionFromSampleFile2() throws Throwable {
         String xmcda10be40d = TestUtilities.get_a10be40d();
         XmcdDisc disc = XmcdDisc.fromXmcdFile(xmcda10be40d, FreedbGenre.NEWAGE);
     }
     
     /**
      * Tests another sample xmcd file.
      * This test doesn't bother verifying fields, it just checks that nothing blows up.
      */
     @Test
     public void testConstructionFromSampleFile3() throws Throwable {
         String xmcd9209840d = TestUtilities.get_9209840d();
         XmcdDisc disc = XmcdDisc.fromXmcdFile(xmcd9209840d, FreedbGenre.ROCK);
     }
     
     /**
      * Parse a fabricated xmcd file which tests some edge cases.
      */
     @Test
     public void testParsingUnderStress() throws Throwable {
         String xmcdStress = TestUtilities.getStressFile();
         XmcdDisc disc = XmcdDisc.fromXmcdFile(xmcdStress, FreedbGenre.ROCK);
         System.out.println(disc);
         
         // Tests handling of discs titled after the artist
         Assert.assertEquals("The Artist", disc.albumRow.artistName);
         Assert.assertEquals("The Artist", disc.albumRow.title);
         // Tests handling of missing year
         Assert.assertEquals(XmcdDisc.INVALID_YEAR, disc.albumRow.year);
         // Tests handling of missing genre (guessing from FreeDB genre)
         Assert.assertEquals("Rock", disc.albumRow.freeTextGenre);
         // Tests splitting of track title
         Assert.assertEquals("The Artist", disc.trackRows.get(9).artistName);
         Assert.assertEquals("Fancy Shmancy", disc.trackRows.get(9).title);
         Assert.assertEquals(9, disc.trackRows.get(9).trackNum);
         // Tests one track with a different artist
         Assert.assertEquals("The Guys", disc.trackRows.get(13).artistName);
         Assert.assertEquals("I'll Remember (Mono)", disc.trackRows.get(13).title);
     }
 }
