 
 package com.exitmusic.user.account;
 
 import com.exitmusic.user.account.UserAccount;
 import com.exitmusic.user.account.dao.UserAccountDAO;
 
 /**
  * Description: Description goes here.
  * 
  * @author <a href="mailto:123fakestreet">Kai Chang</a>
  * @version $Revision$ $Date$
  * @since 0.1
  */
 public class UserAccountDAOTestImpl
 	implements UserAccountDAO
 {
 
 	/**
 	 * Constructor
 	 */
 	public UserAccountDAOTestImpl() {
 		// super(); ?
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see com.exitmusic.user.account.dao.UserAccountDAO#deleteUser(java.lang.String)
 	 */
	public boolean deleteUser(String userId) {
 		// TODO Auto-generated method stub
		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see com.exitmusic.user.account.dao.UserAccountDAO#lookupById(java.lang.String)
 	 */
 	public UserAccount lookupById(String userId) {
 		// Simulate the user does not exist
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see com.exitmusic.user.account.dao.UserAccountDAO#lookupByUsername(java.lang.String)
 	 */
 	public UserAccount lookupByUsername(String username) {
 		// Simulate the user does not exist
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see com.exitmusic.user.account.dao.UserAccountDAO#saveUser(com.exitmusic.user.account.UserAccount)
 	 */
	public boolean saveUser(UserAccount userAccount) {
 		// TODO Auto-generated method stub
		return false;
 	}
 
 }
