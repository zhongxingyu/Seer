 package suite;
 

 import static play.test.Helpers.fakeApplication;
 import static play.test.Helpers.running;
 import org.junit.Test;
 
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
