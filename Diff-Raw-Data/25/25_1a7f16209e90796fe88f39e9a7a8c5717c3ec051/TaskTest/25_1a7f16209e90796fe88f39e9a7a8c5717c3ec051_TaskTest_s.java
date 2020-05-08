 package task.uat;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.thoughtworks.selenium.DefaultSelenium;
 import com.thoughtworks.selenium.Selenium;
 
 public class TaskTest {
 	
 	Selenium webTester;
 	
 	@Before
 	public void setup(){
 		webTester = new DefaultSelenium("localhost", 4444, "*chrome", "http://192.168.178.41:8080/");
 		webTester.start();
 	}
 	
 	@After
 	public void tearDown(){
 		webTester.stop();
 	}
 	
 	@Test
 	public void testLogin() throws Exception {
		webTester.open("/task/login");
		webTester.type("username", "tux");
		webTester.type("password", "test");
		webTester.click("loginButton");
		webTester.waitForPageToLoad("30000");
 		assertTrue(webTester.isTextPresent("Create new Customer"));
 	}
 	
 	@Test
 	public void testCreateTask() throws Exception {
		fail("fail");
 	}
 	
 	
 }
