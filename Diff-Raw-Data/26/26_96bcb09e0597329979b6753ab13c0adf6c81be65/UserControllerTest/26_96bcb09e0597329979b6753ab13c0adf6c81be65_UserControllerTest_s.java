 package ncu.cc.moonshine.controllers;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.support.BindingAwareModelMap;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations= { "classpath*:META-INF/spring/testing-Context.xml" })
 public class UserControllerTest {
 	private @Autowired UserController		userController;
 	
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testHome() {
 		ModelMap model = new ModelMap();
		System.out.println("Page=" + userController.home(model));
		if (! model.containsAttribute("userBean")) {
			System.out.println("No such attribute");
		} else {
			Object o = model.get("userBean");
			if (o != null) {
				System.out.println(o.getClass().getName());
			} else {
				System.out.println("null");
			}
		}
 	}
 
 	@Test
 	public void testAddUserModel() {
 		Model model = new BindingAwareModelMap();
 
 		//Model model = new ModelMap();
 		userController.addUser(model);
 		//fail("Not yet implemented");
 	}
 
 	@Test
 	public void testAddUserUserModel() {
 		//fail("Not yet implemented");
 	}
 
 	@Test
 	public void testModifyUser() {
 		//fail("Not yet implemented");
 	}
 
 	@Test
 	public void testDeleteUser() {
 		//fail("Not yet implemented");
 	}
 
 }
