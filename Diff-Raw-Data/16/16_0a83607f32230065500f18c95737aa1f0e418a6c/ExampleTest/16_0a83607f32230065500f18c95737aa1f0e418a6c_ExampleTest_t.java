 package test;
 
 import org.fest.assertions.AssertExtension;
 import org.junit.*;
 import play.mvc.*;
 import play.test.*;
import views.html.*;
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
	
	@Test
	public void testTemplate(){
		
//	    Index is the name of our scala template.
//		The scala source file takes one string argument thet the template will render
		Content htmlContent = index.render("Test-string");
		
//		Test the content Type
		assertThat(contentType(htmlContent)).isEqualTo("text/html");
//		Test that the html Content contains a string
		assertThat(contentAsString(htmlContent)).contains("Test-string");
	}
 }
