 package test;
 
 import org.fest.assertions.AssertExtension;
 import org.junit.*;
 import play.mvc.*;
 import play.test.*;
 import views.html.*;
 import static play.test.Helpers.*;
 import static org.fest.assertions.Assertions.*;
 
import models.data.Link;

import java.util.List;
import java.util.ArrayList;
 
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
        List links = new ArrayList<Link>();
        links.add(new Link("Bebras", "http://www.bebras.be"));
		Content htmlContent = index.render("Test-string", links);
 		
 //		Test the content Type
 		assertThat(contentType(htmlContent)).isEqualTo("text/html");
 //		Test that the html Content contains a string
 		assertThat(contentAsString(htmlContent)).contains("Test-string");
 	}
 }
