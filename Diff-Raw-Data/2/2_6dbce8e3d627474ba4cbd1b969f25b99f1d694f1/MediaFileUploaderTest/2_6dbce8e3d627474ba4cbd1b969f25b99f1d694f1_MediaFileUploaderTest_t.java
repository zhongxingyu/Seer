 package fm.audiobox.core.test;
 
 import java.io.File;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import fm.audiobox.core.exceptions.LoginException;
 import fm.audiobox.core.exceptions.ServiceException;
 import fm.audiobox.core.models.MediaFile;
 import fm.audiobox.core.models.MediaFiles;
 import fm.audiobox.core.parsers.UploadHandler;
 import fm.audiobox.core.test.mocks.fixtures.Fixtures;
 import fm.audiobox.interfaces.IConnector.IConnectionMethod;
 
 public class MediaFileUploaderTest extends AudioBoxTestCase {
 
   @Before
   public void setUp() {
     loginCatched();
   }
 
   @Test
   public void testMediaFileUpload() {
     
     try {
       MediaFile media = new MediaFile(this.abc);
       
       assertNull(media.getToken());
       assertNull(media.getMediaFileName());
       
       File fileToUpload = new File( Fixtures.get( Fixtures.FILE_TO_UPLOAD ) );
       IConnectionMethod request = media.upload( false, new UploadHandler(fileToUpload) );
       assertTrue( request.getResponse().isOK() );
       
       assertNotNull(media.getToken());
       assertNotNull(media.getMediaFileName());
       
     } catch (ServiceException e) {
       e.printStackTrace();
     } catch (LoginException e) {
       e.printStackTrace();
     }
 
   }
   
   @Test
   public void testMediaFileUploadFailed() {
     
     MediaFile media = new MediaFile(this.abc);
     Exception ex = null;
     try {
       
       assertNull(media.getToken());
       assertNull(media.getMediaFileName());
       
       File fileToUpload = new File( Fixtures.get( Fixtures.FILE_TO_UPLOAD ) );
       IConnectionMethod request = media.upload( false, new UploadHandler(fileToUpload) );
       assertTrue( request.getResponse().isOK() );
       
     } catch (ServiceException e) {
       ex = e;
     } catch (LoginException e) {
       ex = e;
     }
     
     assertNotNull(ex);
     assertTrue( ex instanceof ServiceException );
     
     ServiceException se = (ServiceException)ex;
     
     assertEquals( se.getErrorCode(), 422 );
     assertNotNull( se.getMessage() );
     
     assertNull(media.getToken());
     assertNull(media.getMediaFileName());
 
   }
   
   
   @Test
   public void testMediaAsLocal() {
     
     
     Thread t1 = new UploadAsLocal( "67820b3d14728669b27fb6653512cf18" );
     Thread t2 = new UploadAsLocal( "41e1efd31428c322f02ad01533d42c47" );
     
     
     t1.start();
     t2.start();
     
     try {
       t1.join();
       t2.join();
     } catch (InterruptedException e) {
       assertNull( e );
     }
     
   }
   
   
   private class UploadAsLocal extends Thread {
     
     private String md5;
     
     public UploadAsLocal(String md5) {
       this.md5 = md5;
     }
     
     public void run(){
       MediaFile media = new MediaFile( abc );
       
       media.setTitle(   "title"    );
       media.setArtist (  "artist"     );
       media.setAlbum (  "album"     );
       media.setGenre (   "genre"    );
       media.setLenStr (  "1:24"     );
       media.setMediaFileName (   "media_file_name.mp3"    );
       media.setMime (   "audio/mp3"    );
       media.setYear (   2012    );
       media.setLenInt (   203    );
       media.setPosition (  1     );
       media.setPlays (   4    );
       media.setDisc (   9    );
       media.setSize(  1234     );
       media.setType(    MediaFiles.Type.AudioFile   );
      media.setSource(  MediaFile.Source.local.toString()     );
       media.setAudioSampleRate (   "192"    );
       media.setAudioBitRate (  "44100"   );
       media.setOriginalFileName (  "/User/path/to/media_file_name.mp3"     );
       media.setMd5(   this.md5    );
       
       try {
         media.notifyAsLocal();
       } catch (ServiceException e) {
         assertNull(e);
         e.printStackTrace();
       } catch (LoginException e) {
         assertNull(e);
         e.printStackTrace();
       }
       
       System.out.println( "===========" );
       System.out.println( "[" + media.toString() + "] >> " + (media.getToken() != null ) );
       System.out.println( "===========" );
       
     }
   }
 
 }
