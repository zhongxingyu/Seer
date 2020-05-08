 package de.fhb.autobday.manager.user;
 
import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

 import com.stvconsultants.easygloss.javaee.JavaEEGloss;

 import de.fhb.autobday.dao.AbdUserFacade;
 import de.fhb.autobday.data.AbdUser;
import de.fhb.autobday.exception.user.IncompleteLoginDataException;
import de.fhb.autobday.exception.user.IncompleteUserRegisterException;
import de.fhb.autobday.exception.user.NoValidUserNameException;
import de.fhb.autobday.exception.user.PasswordInvalidException;
import de.fhb.autobday.exception.user.UserNotFoundException;
 import de.fhb.autobday.manager.mail.MailManagerLocal;
 
 /**
  * Tests the userManager class and their methods.
  *
  * @author Michael Koppen <koppen@fh-brandenburg.de>
  * 
  * 
  */
 public class UserManagerTest {
 
 	private JavaEEGloss gloss;
 	
 	private UserManager managerUnderTest;
 	
 	private AbdUserFacade userDAOMock;
 	
 	private MailManagerLocal mailManagerMock;
 	
 	public UserManagerTest() {
 	}
 
 	@BeforeClass
 	public static void setUpClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownClass() throws Exception {
 	}
 	
 	@Before
 	public void setUp() {
 		gloss= new JavaEEGloss();
 		
 		//create Mocks
 		userDAOMock = EasyMock.createMock(AbdUserFacade.class);
 		mailManagerMock = EasyMock.createMock(MailManagerLocal.class);
 		
 		//set Objekts to inject
 		gloss.addEJB(userDAOMock);
 		gloss.addEJB(mailManagerMock);
 		
 		//create Manager with Mocks
 		managerUnderTest=gloss.make(UserManager.class);
 	}
 	
 	@After
 	public void tearDown() {
 
 	}
 
 	/**
 	 * Test of getUser method, of class UserManager.
 	 */
 	@Test
 	public void testGetUser() throws Exception {
 		System.out.println("getUser");
 		
 		//prepare test variables
 		int userid = 1;
 		AbdUser expResult = new AbdUser(1);
 		
 		// Setting up the expected value of the method call of Mockobject
 		EasyMock.expect(userDAOMock.find(userid)).andReturn(new AbdUser(1));
 		
 		// Setup is finished need to activate the mock
 		EasyMock.replay(userDAOMock);
 		
 		// verify	
 		assertEquals(expResult, managerUnderTest.getUser(1));
 		EasyMock.verify(userDAOMock);
 	}
 
 	/**
 	 * Test of login method, of class UserManager.
 	 */
 	@Test
 	public void testLoginShouldBeEquals() throws Exception {
 		System.out.println("loginShouldBeEquals");
 		
 		//prepare test variables
 		String loginName = "ott";
 		String password = "1234";
 		
 		AbdUser user = new AbdUser(1, "ott", "1234", null, "Ott", "Chris");
 		
 		// Setting up the expected value of the method call of Mockobject
		EasyMock.expect(userDAOMock.find(loginName)).andReturn(user);
 		
 		// Setup is finished need to activate the mock
 		EasyMock.replay(userDAOMock);
 		
 		// verify	
 		assertEquals(user, managerUnderTest.login(loginName, password));
 		EasyMock.verify(userDAOMock);
 	}
 	
 	/**
 	 * Test of login method, of class UserManager.
 	 */
 	@Test(expected = IncompleteLoginDataException.class)
 	public void testLoginShouldThrowIncompleteLoginDataException() throws Exception {
 		System.out.println("loginShouldThrowIncompleteLoginDataException");
 		
 		//prepare test variables
 		String loginName = "ott";
 		String password = "";
 		
 		AbdUser user = new AbdUser(1, "ott", "1234", null, "Ott", "Chris");
 		
 		// verify	
 		assertEquals(user, managerUnderTest.login(loginName, password));
 	}
 	
 	/**
 	 * Test of login method, of class UserManager.
 	 */
 	@Test(expected = UserNotFoundException.class)
 	public void testLoginShouldThrowUserNotFoundException() throws Exception {
 		System.out.println("loginShouldThrowUserNotFoundException");
 		
 		//prepare test variables
 		String loginName = "ott";
 		String password = "1234";
 		
 		AbdUser user = new AbdUser(1, "ott", "1234", null, "Ott", "Chris");
 		
 		// Setting up the expected value of the method call of Mockobject
		EasyMock.expect(userDAOMock.find(loginName)).andReturn(null);
 		
 		// Setup is finished need to activate the mock
 		EasyMock.replay(userDAOMock);
 		
 		// verify	
 		assertEquals(user, managerUnderTest.login(loginName, password));
 		EasyMock.verify(userDAOMock);
 	}
 	
 	/**
 	 * Test of login method, of class UserManager.
 	 */
 	@Test(expected = PasswordInvalidException.class)
 	public void testLoginShouldThrowPasswordInvalidException() throws Exception {
 		System.out.println("loginShouldThrowPasswordInvalidException");
 		
 		//prepare test variables
 		String loginName = "ott";
 		String password = "1234";
 		
 		AbdUser user = new AbdUser(1, "ott", "123", null, "Ott", "Chris");
 		
 		// Setting up the expected value of the method call of Mockobject
		EasyMock.expect(userDAOMock.find(loginName)).andReturn(user);
 		
 		// Setup is finished need to activate the mock
 		EasyMock.replay(userDAOMock);
 		
 		// verify	
 		assertEquals(user, managerUnderTest.login(loginName, password));
 		EasyMock.verify(userDAOMock);
 	}
 	
 	/**
 	 * Test of logout method, of class UserManager.
 	 */
 	@Test
 	public void testLogout() throws Exception {
 		System.out.println("testLogout");
 		
 		
 		// TODO review the generated test code and remove the default call to fail.
 		//fail("The test case is a prototype.");
 	}
 	
 	
 	/**
 	 * Test of register method, of class UserManager.
 	 */
 	@Test
 	public void testRegister() throws Exception {
 		System.out.println("testRegister");
 		
 		//prepare test variables
 		String firstName = "biene";
 		String name = "maja";
 		String userName = "summsesum";
 		String mail = "biene@maja.com";
 		
 		// Setting up the expected value of the method call of Mockobject
 		userDAOMock.create((AbdUser) EasyMock.anyObject());
 		//TODO funktioniert noch nicht
 //		mailManagerMock.sendBdayMail((String) EasyMock.anyObject(), mail,(String) EasyMock.anyObject(), (String) EasyMock.anyObject());
 		
 		// Setup is finished need to activate the mock
 		EasyMock.replay(userDAOMock);
 //		EasyMock.replay(mailManagerMock);
 		
 		//call method to test
 		managerUnderTest.register(firstName, name, userName, mail);
 		
 		// verify	
 		EasyMock.verify(userDAOMock);
 //		EasyMock.verify(mailManagerMock);		
 	}
 	
 	
 	/**
 	 * Test of register method, of class UserManager.
 	 * This test provokes a IncompleteUserRegisterException!
 	 */
 	@Test(expected = IncompleteUserRegisterException.class)
 	public void testRegisterThrowIncompleteUserRegisterExceptionFirst() throws Exception {
 		System.out.println("testRegister");
 		
 		//prepare test variables
 		String firstName = null;
 		String name = "maja";
 		String userName = "summsesum";
 		String mail = "biene@maja.com";
 		
 		//call method to test
 		managerUnderTest.register(firstName, name, userName, mail);
 		
 	}
 	
 	/**
 	 * Test of register method, of class UserManager.
 	 * This test provokes a IncompleteUserRegisterException!
 	 */
 	@Test(expected = IncompleteUserRegisterException.class)
 	public void testRegisterThrowIncompleteUserRegisterExceptionSecound() throws Exception {
 		System.out.println("testRegister");
 		
 		//prepare test variables
 		String firstName = "biene";
 		String name = null;
 		String userName = "summsesum";
 		String mail = "biene@maja.com";
 		
 		//call method to test
 		managerUnderTest.register(firstName, name, userName, mail);
 		
 	}
 	
 	/**
 	 * Test of register method, of class UserManager.
 	 * This test provokes a IncompleteUserRegisterException!
 	 */
 	@Test(expected = IncompleteUserRegisterException.class)
 	public void testRegisterThrowIncompleteUserRegisterExceptionThird() throws Exception {
 		System.out.println("testRegister");
 		
 		//prepare test variables
 		String firstName = "biene";
 		String name = "maja";
 		String userName = null;
 		String mail = "biene@maja.com";
 		
 		//call method to test
 		managerUnderTest.register(firstName, name, userName, mail);
 		
 	}
 	
 	/**
 	 * Test of register method, of class UserManager.
 	 * This test provokes a IncompleteUserRegisterException!
 	 */
 	@Test(expected = IncompleteUserRegisterException.class)
 	public void testRegisterThrowIncompleteUserRegisterExceptionFourth() throws Exception {
 		System.out.println("testRegister");
 		
 		//prepare test variables
 		String firstName = "biene";
 		String name = "maja";
 		String userName = "summsesum";
 		String mail = null;
 		
 		//call method to test
 		managerUnderTest.register(firstName, name, userName, mail);
 		
 	}
 	
 	/**
 	 * Test of register method, of class UserManager.
 	 * This test provokes a NoValidUserNameException!
 	 */
 	@Test(expected = NoValidUserNameException.class)
 	public void testRegisterThrowNoValidUserNameException() throws Exception {
 		System.out.println("testRegister");
 		
 		//prepare test variables
 		String firstName = "biene";
 		String name = "maja";
 		String userName = "su";
 		
 		//call method to test
 		managerUnderTest.register(firstName, name, userName, null);
 		
 	}
 
 	
 }
