 package test;
 
 import org.fest.assertions.AssertExtension;
 import org.junit.*;
 import play.mvc.*;
 import play.test.*;
import play.libs.*;
 import static play.test.Helpers.*;
 import static org.fest.assertions.Assertions.*;
 
 
 //An example of some basic JUnit tests and integration tests.
 
 public class ExampleTest {
 
 	@Test
 	public void ExampleTest(){
 		int a = 1+1;
 		Assert.assertTrue(a==2);
 	}
 	
 	@Test
 	public void setupFakeServer(){
 		//Create anon runnable and pass it to running()
 		running(fakeApplication(), new Runnable() {
 			
 			@Override
 			public void run() {
 				// Do some tests on the fake server
 				int a = 1+1;
 				Assert.assertTrue(a==2);
 				
 			}
 		});
 	}
 }
