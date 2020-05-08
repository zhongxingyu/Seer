 package fm.audiobox.sync.test;
 
 import java.io.File;
 import java.io.FileFilter;
 
 import org.junit.Test;
 
 import fm.audiobox.core.exceptions.LoginException;
 import fm.audiobox.core.exceptions.ModelException;
 import fm.audiobox.core.exceptions.ServiceException;
 import fm.audiobox.core.models.AudioBoxClient;
 import fm.audiobox.core.models.User;
 import fm.audiobox.sync.task.Scan;
 import fm.audiobox.sync.test.mocks.fixtures.Fixtures;
 
 /**
  * Unit test for simple App.
  */
 public class ScanTest extends junit.framework.TestCase {
     
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
             
             final String[] allowed_formats = user.getSubscription().getAllowedFormats();
             
             Scan scan = new Scan( new File("/home/shotty/Dropbox/Public"), false, false);
             scan.setFilter(new FileFilter() {
 				public boolean accept(File file) {
 					if ( ! file.isDirectory() && ! file.isHidden() )
 						for ( String ext : allowed_formats )
 							if ( file.getName().endsWith( ext ) )
 								return true;
 					return false;
 				}
 			});
             System.out.println( "" + scan.scan().size() + " was found");
             
             
         } catch (ServiceException e) {
             e.printStackTrace();
         } catch (LoginException e) {
             e.printStackTrace();
         } catch (ModelException e) {
             e.printStackTrace();
         }
     }
     
 }
