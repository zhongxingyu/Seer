 
 package com.exitmusic.user.auth;
 
 import com.exitmusic.user.account.UserAccount;
 
 /**
  * Description: Description goes here.
  * 
  * @author <a href="mailto:123fakestreet">Kai Chang</a>
  * @version $Revision$ $Date$
  * @since 0.1
  */
 public interface AuthenticationService {
 
 	/**
 	 * Create user account
 	 * 
 	 * @param userAccount
	 * @return boolean indicating success
 	 */
 	boolean create(UserAccount userAccount);
 
 	/**
 	 * Delete user account
 	 * 
 	 * @param userId
	 * @return boolean indicating success
 	 */
 	boolean delete(String userId);
 
 	/**
 	 * Login user
 	 * 
 	 * @param userId
	 * @return boolean indicating success
 	 */
 	boolean login(String userId);
 }
