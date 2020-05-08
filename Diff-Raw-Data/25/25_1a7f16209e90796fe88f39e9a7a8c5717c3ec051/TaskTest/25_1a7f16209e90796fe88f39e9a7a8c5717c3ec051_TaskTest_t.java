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
		login("tux", "test");
 		assertTrue(webTester.isTextPresent("Create new Customer"));
 	}
 	
 	@Test
 	public void testCreateTask() throws Exception {
		login("tux", "test");
		webTester.click("link=Create new Customer");
		webTester.waitForPageToLoad("30000");
		webTester.type("title", "Test Title");
		webTester.type("description", "Description");
		webTester.click("saveButton");
		webTester.waitForPageToLoad("30000");
		assertEquals("Test Title", webTester.getText("//table[@id='customerTable']/tbody/tr[2]/td[1]"));
		assertEquals("Description", webTester.getText("//table[@id='customerTable']/tbody/tr[2]/td[2]"));
	}
	
	public void login(String username, String password){
		webTester.open("/task/login");
		webTester.type("username", username);
		webTester.type("password", password);
		webTester.click("loginButton");
		webTester.waitForPageToLoad("30000");
 	}
 	
 	
 }
