 
 package com.exitmusic.user.auth;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import com.exitmusic.user.account.UserAccount;
 import com.exitmusic.user.account.dao.UserAccountDAO;
 
 /**
  * Description: Description goes here.
  * 
  * @author <a href="mailto:123fakestreet">Kai Chang</a>
  * @version $Revision$ $Date$
  * @since 0.1
  */
 @RunWith(MockitoJUnitRunner.class)
 public class AuthenticationServiceTestCase {
 
 	private AuthenticationService _authenticationService;
 
 	@Mock
 	private UserAccountDAO _userAccountDAO;
 
 	/**
 	 * Setup
 	 */
 	@Before
 	public void setUp() {
 		// _userAccountDAO = new UserAccountDAOTestImpl(); The mock eliminates
 		// the need for a separate test implementation
 		_authenticationService = new AuthenticationServiceImpl(_userAccountDAO);
 
 		// Mockito.when(_userAccountDAO.lookupByUsername("testUsername")).thenReturn();
 		// Mockito.when(_userAccountDAO.saveUser(testUserAccount));
 	}
 
 	/**
 	 * Verify user account creation
 	 */
 	@Test
 	public void testUserAccountCreate() {
 		UserAccount testUserAccount;
 		boolean createSuccess;
 
 		// Setup
 		testUserAccount = new UserAccount("testUserId", "testUsername");
 		Mockito.when(_userAccountDAO.lookupById("testUserId")).thenReturn(null);
		Mockito.when(_userAccountDAO.saveUser(testUserAccount)).thenReturn(true);
 
 		// Run
 		createSuccess = _authenticationService.create(testUserAccount);
		assertEquals("UserAccount was not created", true, createSuccess);
 		Mockito.verify(_userAccountDAO, Mockito.times(1)).lookupById(Matchers.anyString());
 		Mockito.verify(_userAccountDAO, Mockito.times(1)).saveUser(testUserAccount);
 	}
 
 	/**
 	 * Verify user account deletion
 	 */
 	@Test
 	public void testUserAccountDelete() {
 		UserAccount testUserAccount;
 		boolean deleteSuccess;
 
 		// Setup
 		testUserAccount = new UserAccount("testUserId", "testUsername");
 		Mockito.when(_userAccountDAO.lookupById("testUserId")).thenReturn(testUserAccount);
		Mockito.when(_userAccountDAO.deleteUser("testUserId")).thenReturn(true);
 
 		// Run
 		deleteSuccess = _authenticationService.delete(testUserAccount.getUserId());
		assertEquals("UserAccount was not deleted", true, deleteSuccess);
 		Mockito.verify(_userAccountDAO, Mockito.times(1)).lookupById(Matchers.anyString());
 		Mockito.verify(_userAccountDAO, Mockito.times(1)).deleteUser(Matchers.anyString());
 	}
 
 	/**
 	 * Verify user account login
 	 */
 	@Test
 	public void testUserAccountLogin() {
 		UserAccount testUserAccount;
 		boolean loginSuccess;
 
 		// Setup
 		testUserAccount = new UserAccount("testUserId", "testUsername");
 		Mockito.when(_userAccountDAO.lookupById("testUserId")).thenReturn(testUserAccount);
 
 		// Run
 		loginSuccess = _authenticationService.login(testUserAccount.getUserId());
		assertEquals("Login failed", true, loginSuccess);
 		Mockito.verify(_userAccountDAO, Mockito.times(1)).lookupById(Matchers.anyString());
 	}
 }
