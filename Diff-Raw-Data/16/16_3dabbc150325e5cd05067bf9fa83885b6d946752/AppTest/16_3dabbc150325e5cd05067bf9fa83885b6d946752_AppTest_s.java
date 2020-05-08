 package fm.audiobox.sync.test;
 
 import java.io.File;
 
 import org.junit.Test;
 
 import fm.audiobox.core.exceptions.LoginException;
 import fm.audiobox.core.exceptions.ModelException;
 import fm.audiobox.core.exceptions.ServiceException;
 import fm.audiobox.core.models.AudioBoxClient;
 import fm.audiobox.core.models.Track;
 import fm.audiobox.core.models.UploadTrack;
 import fm.audiobox.core.models.User;
 import fm.audiobox.sync.task.Upload;
 import fm.audiobox.sync.test.mocks.fixtures.Fixtures;
 
 /**
  * Unit test for simple App.
  */
 public class AppTest extends junit.framework.TestCase {
     
     Fixtures fx = new Fixtures(); 
 
     @Test
     @SuppressWarnings("deprecation")
     public void testApp() {
         
         System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
         System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
         System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");
         System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "debug");
         System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.impl", "debug");
         System.setProperty("org.apache.commons.logging.simplelog.log.fm.audiobox.core", "debug");
         
         assertTrue( true );
         try {
         	AudioBoxClient.setModelClassFor( AudioBoxClient.NEW_TRACK_KEY , UploadTrack.class );
             AudioBoxClient abc = new AudioBoxClient();
             abc.setForceTrust(true);
             
             User user = abc.login( fx.get( Fixtures.LOGIN), fx.get(Fixtures.RIGHT_PASS) );
             assertNotNull( user );
             
             File f = new File( fx.get( Fixtures.TEST_FILE ) );
             assertNotNull( f );
             assertTrue( f.exists() );
             
             Track up =  user.newTrack();
             assertNotNull( up );
             
             Upload upload = new Upload( (UploadTrack)up, f);
             
             String uuid = upload.upload();
             
             assertNotNull( uuid );
             assertTrue( uuid.length() > 0 );
             
             /*Track tr = user.getTrackByUuid( uuid );
             
             assertNotNull( tr );*/
             
             
         } catch (ServiceException e) {
             e.printStackTrace();
         } catch (LoginException e) {
             e.printStackTrace();
         } catch (ModelException e) {
             e.printStackTrace();
         }
     }
 }
