 package fm.audiobox.core.test;
 
 import java.io.File;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import fm.audiobox.core.exceptions.LoginException;
 import fm.audiobox.core.exceptions.ServiceException;
 import fm.audiobox.core.models.MediaFile;
 import fm.audiobox.core.models.Playlist;
 import fm.audiobox.core.models.Playlists;
 import fm.audiobox.core.parsers.UploadHandler;
 import fm.audiobox.core.test.mocks.fixtures.Fixtures;
 
 public class MediaFileUploaderTest extends AbxTestCase {
 
   
   private static String lastTokenMedia = "";
   
   @Before
   public void setUp() {
     loginCatched();
   }
 
   @Test
   public void simpleUploadMediaFile() {
     
     Playlists pls = user.getPlaylists();
     long initialMFSize = 0;
     
     try {
       pls.load(false);
       Playlist pl = pls.getPlaylistByType( Playlists.Type.CloudPlaylist );
       initialMFSize = pl.getMediaFilesCount();
     } catch (ServiceException e) {
       fail(e.getMessage());
     } catch (LoginException e) {
       fail(e.getMessage());
     }
     
     
     MediaFile mf = user.newMediaFile();
     
     assertNull( mf.getToken() );
     
     File file = new File( Fixtures.class.getResource( Fixtures.get( Fixtures.FILE_TO_UPLOAD ) ).getFile() );
     
     try {
      mf.upload( false, new UploadHandler(file) );
       assertNotNull( mf.getToken() );
       lastTokenMedia = mf.getToken();
     } catch (ServiceException e) {
       fail(e.getMessage());
     } catch (LoginException e) {
       fail(e.getMessage());
     }
     
     try {
       pls.load(false);
       Playlist pl = pls.getPlaylistByType( Playlists.Type.CloudPlaylist );
       assertEquals( pl.getMediaFilesCount() , initialMFSize + 1);
     } catch (ServiceException e) {
       fail(e.getMessage());
     } catch (LoginException e) {
       fail(e.getMessage());
     }
     
   }
   
   
   @Test
   public void destroyMediaFiles() {
     
     MediaFile mf = user.newMediaFile();
     mf.setToken( lastTokenMedia );
     
     try {
       mf.load(false);
     } catch (ServiceException e) {
       fail(e.getMessage() );
     } catch (LoginException e) {
       fail(e.getMessage() );
     }
     
     try {
       assertTrue( mf.destroy() );
     } catch (ServiceException e) {
       fail(e.getMessage() );
     } catch (LoginException e) {
       fail(e.getMessage() );
     }
     
   }
   
 
 }
