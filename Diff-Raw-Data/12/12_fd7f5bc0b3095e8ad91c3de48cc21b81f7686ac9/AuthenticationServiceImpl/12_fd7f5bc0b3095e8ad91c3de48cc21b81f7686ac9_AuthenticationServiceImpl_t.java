 
 package com.exitmusic.user.auth;
 
 import com.exitmusic.user.account.UserAccount;
 import com.exitmusic.user.account.dao.UserAccountDAO;
 
 /**
  * Description: Description goes here.
  * 
  * @author <a href="mailto:123fakestreet">Kai Chang</a>
  * @version $Revision$ $Date$
  * @since 0.1
  */
 public class AuthenticationServiceImpl
 	implements AuthenticationService
 {
 
 	private final UserAccountDAO _userAccountDAO;
 
 	/**
 	 * Default constructor - Is the method name correct?
 	 * 
 	 * @param userAccountDAO
 	 */
 	public AuthenticationServiceImpl(UserAccountDAO userAccountDAO) {
 		// TODO Need to import springframework
 		// Assert.notNull(userAccountDAO, "userAccountDAO is required");
 
 		_userAccountDAO = userAccountDAO;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see com.exitmusic.user.auth.AuthenticationService#create(com.exitmusic.user.account.UserAccount)
 	 */
 	public boolean create(UserAccount userAccount) {
 		boolean success = false;
 		
 		if (_userAccountDAO.lookupById(userAccount.getUserId()) == null) {
 			success = _userAccountDAO.saveUser(userAccount);
 		}
 		return success;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see AuthenticationService#delete(java.lang.String)
 	 */
 	public boolean delete(String userId) {
 		boolean success = false;
 		
 		if (_userAccountDAO.lookupById(userId) != null) {
 			success = _userAccountDAO.deleteUser(userId);
 		}
 		return success;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see AuthenticationService#login(java.lang.String)
 	 */
 	public boolean login(String userId) {
 		boolean success = false;
		// fake login code
		if (_userAccountDAO.lookupById(userId) != null) {
			success = true;
		}
 		return success;
 	}
 }
