 
 package com.exitmusic.user.auth;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import com.exitmusic.user.account.UserAccount;
 import com.exitmusic.user.account.UserAccountDAOTestImpl;
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
 		//_userAccountDAO = new UserAccountDAOTestImpl(); The mock eliminates the need for a separate test implementation
 		_authenticationService = new AuthenticationServiceImpl(_userAccountDAO);
 
 		//Mockito.when(_userAccountDAO.lookupByUsername("testUsername")).thenReturn();
 		//Mockito.when(_userAccountDAO.saveUser(testUserAccount));
 	}
 
 	/**
 	 * Verify user account creation
 	 */
 	@Test
 	public void testUserAccountCreate() {
 		UserAccount testUserAccount;
 		boolean createSuccess;
 		
 		testUserAccount = new UserAccount("testUserId", "testUsername");
 		Mockito.when(_userAccountDAO.lookupById("testUserId")).thenReturn(testUserAccount);
 		createSuccess = _authenticationService.create(testUserAccount);
 		assertEquals("UserAccount was not created", true, createSuccess);
 	}
 
 	/**
 	 * Verify user account deletion
 	 */
 	@Test
 	public void testUserAccountDelete() {
 		UserAccount testUserAccount;
 		
		Mockito.when(_userAccountDAO.lookupById("testUserId")).thenReturn("user exists");
		Mockito.when(_userAccountDAO.deleteUser("testUserId")).thenReturn(true);
		
		_authenticationService.delete(testUserAccount.getUserId());
		assertNull("UserAccount was not deleted", _userAccountDAO.lookupById("testUserId"));
 	}
 
 	/**
 	 * Verify user account login
 	 */
 	@Test
 	public void testUserAccountLogin() {
 		UserAccount testUserAccount;
 		
		_authenticationService.login(testUserAccount.getUserId());
 	}
 }
