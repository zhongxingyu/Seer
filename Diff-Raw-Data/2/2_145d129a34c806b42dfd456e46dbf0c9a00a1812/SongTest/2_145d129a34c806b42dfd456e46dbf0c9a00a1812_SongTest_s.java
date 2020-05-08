 package playlist.model;
 
 import com.datastax.driver.core.Session;
 import junit.framework.TestCase;
 import java.util.List;
 
 /**
  * DataStax Academy Sample Application
  *
  * Copyright 2013 DataStax
  *
  */
 
 public class SongTest extends TestCase {
 
   public void testFindTracksByArtist() throws Exception {
 
     List<TracksDAO> songs = TracksDAO.listSongsByArtist("The Pioneers");
 
     assertEquals(44, songs.size());
 
     // Check the first track
     TracksDAO firstTrack = songs.get(0);
 
     assertEquals("Ali Button", firstTrack.getTrack());
 
   }
 
   public void testFindTracksByGenre() throws Exception {
 
     List<TracksDAO> songs = TracksDAO.listSongsByGenre("classical", 10000);
 
    assertEquals(200, songs.size());
 
     // Check the first track
     TracksDAO firstTrack = songs.get(0);
 
     assertEquals("Concerto grosso No. 10 en Ré Mineur_ Op. 6: Air lento", firstTrack.getTrack());
 
   }
 
   public void testFindTrackById() throws Exception {
 
     TracksDAO track = TracksDAO.getTrackById("TRXQAEJ128F426C456");
 
     assertEquals("TRXQAEJ128F426C456", track.getTrack_id());
     assertEquals("Don't Fear The Reaper", track.getTrack());
 
   }
 
   private void cleanTestTrack() {
     Session session = CassandraData.getSession();
     session.execute("DELETE FROM artists_by_first_letter WHERE first_letter = '-' ");
     session.execute("DELETE FROM track_by_artist WHERE artist = '-The Riptanos' ");
     session.execute("DELETE FROM track_by_genre WHERE genre = 'geek music' ");
     session.execute("DELETE FROM track_by_id WHERE track_id = '123' ");
   }
 
   public void testAddSongAndArtist() throws Exception {
 
     cleanTestTrack();
 
     // Validate data is clean
 
     assertEquals(0,TracksDAO.listSongsByArtist("-The Riptanos").size());
     assertEquals(0,TracksDAO.listSongsByGenre("geek music", 10000).size());
     assertNull("Track 123 exists",TracksDAO.getTrackById("123"));
 
     TracksDAO tracksDAO = new TracksDAO("123", "-The Riptanos", "Share a Mind", "geek music",100 );
     tracksDAO.add();
 
     assertEquals(1,TracksDAO.listSongsByArtist("-The Riptanos").size());
     assertEquals(1,TracksDAO.listSongsByGenre("geek music", 10000).size());
     assertEquals(100,TracksDAO.listSongsByGenre("geek music", 10000).get(0).getTrack_length_in_seconds());
     assertNotNull("Track 123 exists",TracksDAO.getTrackById("123"));
 
     cleanTestTrack();
   }
 
 }
