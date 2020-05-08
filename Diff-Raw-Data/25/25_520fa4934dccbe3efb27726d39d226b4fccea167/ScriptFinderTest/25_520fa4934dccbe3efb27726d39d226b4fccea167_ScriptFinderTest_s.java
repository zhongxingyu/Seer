 package net.frontlinesms.plugins.httptrigger.httplistener;
 
 import java.io.File;
 
 import net.frontlinesms.plugins.httptrigger.httplistener.ScriptFinder;
 
 import junit.framework.TestCase;
 
 public class ScriptFinderTest extends TestCase {
 	
 	public void testMapToFile(){
 		testMapToFileFails("/");
 		testMapToFileFails("/sms");
 		testMapToFileFails("/sms/send");
 		testMapToFileFails("/sms/send/");
 		
		testMapToFileSucceeds( "sms/send.groovy", "sms/send" );
		testMapToFileSucceeds( "sms/send.groovy", "sms/send/" );
		testMapToFileSucceeds( "sms/receive.groovy", "sms/receive" );
		testMapToFileSucceeds( "sms/send.groovy", "sms/send///" );
		testMapToFileSucceeds( "quite/a/long/file/location/receive.groovy", "quite/a/long/file/location/receive" );
		testMapToFileSucceeds( "send.groovy" , "send" );
		testMapToFileSucceeds( "index.groovy", "");
		testMapToFileSucceeds( "index.groovy", null);
 	}
 
 	private void testMapToFileSucceeds(String modifiedPath, String originalPath) {
 		File file = new ScriptFinder().mapToFile(originalPath);
		assertEquals( modifiedPath, file.getPath());
 	}
 
 	private void testMapToFileFails(String path) {
 		ScriptFinder scriptFinder = new ScriptFinder();
		try{
 			scriptFinder.mapToFile(path);
 			fail("We should have thrown an exception for path: " + path);
		}catch(IllegalArgumentException ex){
 		}
 	}
 }
