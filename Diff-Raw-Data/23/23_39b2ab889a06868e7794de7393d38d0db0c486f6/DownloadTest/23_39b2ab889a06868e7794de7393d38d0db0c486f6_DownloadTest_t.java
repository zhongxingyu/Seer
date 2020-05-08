 package fm.audiobox.sync.test;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import org.junit.Test;
 
 import fm.audiobox.core.exceptions.LoginException;
 import fm.audiobox.core.exceptions.ModelException;
 import fm.audiobox.core.exceptions.ServiceException;
 import fm.audiobox.core.models.AudioBoxClient;
 import fm.audiobox.core.models.Track;
 import fm.audiobox.core.models.User;
 import fm.audiobox.sync.test.mocks.fixtures.Fixtures;
 
 /**
  * Unit test for simple App.
  */
 public class DownloadTest extends junit.framework.TestCase {
     
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
         System.setProperty("org.apache.commons.logging.simplelog.log.fm.audiobox.sync", "debug");
         
         assertTrue( true );
         try {
             AudioBoxClient abc = new AudioBoxClient();
             abc.setForceTrust(true);
             
             User user = abc.login( fx.get( Fixtures.LOGIN), fx.get(Fixtures.RIGHT_PASS) );
             assertNotNull( user );
             
             File f = new File ( fx.get(Fixtures.DOWNLOAD_TEST_FILE) );
             
             assertNotNull( f );
             
             /* delete file if exists */
             if ( f.exists() ) f.delete();
             
             try {
 				f.createNewFile();
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
             
 			
 			assertTrue( f.exists() );
 			
             
             // get track and its information by a given UUID
             Track t = user.getTrackByUuid( fx.get( Fixtures.TRACK_TO_DOWNLOAD ) );
             
             assertNotNull( t );
             
             long total = t.getAudioFileSize();
             
             try {
             	// download track
 				t.download( this.new testFileOutputStream( f, total ) );
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
             
 			assertTrue( f.length() == total );
			
			System.out.println( "New file: " + f.getPath() );
             
         } catch (ServiceException e) {
             e.printStackTrace();
         } catch (LoginException e) {
             e.printStackTrace();
         } catch (ModelException e) {
             e.printStackTrace();
         }
     }
     
     private class testFileOutputStream extends FileOutputStream {
 
     	private long total_bytes = -1;
     	private long current_bytes = 0;
     	
 		public testFileOutputStream(File file, long total) throws FileNotFoundException {
 			super(file);
 			this.total_bytes = total;
 		}
 		
 		public void write(byte[] bytes, int offset, int len) throws IOException{
 			super.write(bytes,offset,len);
 			this.current_bytes += len;
 			
 			System.out.println("Downloaded: " + ( (current_bytes * 100 )/ total_bytes ) + " %" );
 		}
     	
     }
     
 }
