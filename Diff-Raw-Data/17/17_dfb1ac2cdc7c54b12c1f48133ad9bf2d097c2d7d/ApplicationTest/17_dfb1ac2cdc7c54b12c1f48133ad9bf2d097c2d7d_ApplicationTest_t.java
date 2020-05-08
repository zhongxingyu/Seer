 import org.junit.*;

 import play.test.*;
 import play.mvc.*;
 import play.mvc.Http.*;
 import models.*;
 
 public class ApplicationTest extends FunctionalTest {
 
 	/*
 	 * We could test all the application functionalities this way, but it’s not 
 	 * the best way to test an HTML based web application. As our web app is 
 	 * intended to be executed in a web browser, it would be better to test it 
 	 * directly in a real web browser.
 	 * 
 	 * These kinds of JUnit based 'Functional tests' are still useful, typically 
 	 * to test Web services returning non-HTML responses such as JSON or XML over HTTP.
 	 */

	@Test
    public void testThatIndexPageWorks() {
        Response response = GET("/");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);
    }
     
 }
