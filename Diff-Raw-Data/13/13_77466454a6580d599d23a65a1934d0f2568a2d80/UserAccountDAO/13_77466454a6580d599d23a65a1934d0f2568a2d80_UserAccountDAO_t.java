 
 package com.exitmusic.user.account.dao;
 
 import com.exitmusic.user.account.UserAccount;
 
 /**
  * Description: Description goes here.
  * 
  * @author <a href="mailto:123fakestreet">Kai Chang</a>
  * @version $Revision$ $Date$
  * @since 0.1
  */
 public interface UserAccountDAO {
 
 	/**
 	 * Delete user account
 	 * 
 	 * @param userId
	 * @return boolean indicating success
 	 */
 	boolean deleteUser(String userId);
 
 	/**
 	 * Look up user by ID
 	 * 
 	 * @param userId
 	 * @return Retrieved UserAccount object
 	 */
 	UserAccount lookupById(String userId);
 
 	/**
 	 * Look up user by username
 	 * 
 	 * @param username
 	 * @return Retrieved UserAccount object
 	 */
 	UserAccount lookupByUsername(String username);
 
 	/**
 	 * Save user account
 	 * 
 	 * @param userAccount
	 * @return boolean indicating success
 	 */
 	boolean saveUser(UserAccount userAccount);
 }
