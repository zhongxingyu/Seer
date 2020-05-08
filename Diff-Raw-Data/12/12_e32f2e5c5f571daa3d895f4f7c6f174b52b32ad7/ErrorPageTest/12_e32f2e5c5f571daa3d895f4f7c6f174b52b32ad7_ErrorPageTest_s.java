 package suite;
 
import static org.junit.Assert.*;
 import static play.test.Helpers.fakeApplication;
 import static play.test.Helpers.running;

 import org.junit.Test;
 
import com.sun.jna.platform.win32.W32Service;

import play.mvc.Result;
import play.test.FakeRequest;

import controllers.routes;

 public class ErrorPageTest {
 
 	@Test
 	public void test() {
 		   running(fakeApplication(), new Runnable(){
 	            public void run(){
 	                // TODO write tests for pagination
 	            }
 	        });
 	}
 
 }
