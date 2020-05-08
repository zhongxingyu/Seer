 /**
  * 
  */
 package com.chinarewards.qqgbvpn.mgmtui.struts;
 
 import com.chinarewards.qqgbvpn.mgmtui.struts.formbean.UserForm;
 
 /**
  * Simple implementation of the {@code UserSession} interface.
  * 
  * @author Cream
  * @since 1.0.0 2011-09-02
  */
 public class SimpleUserSession implements UserSession {
 
 	private UserForm user;
 
 	private boolean isLoggedIn = false;
 
	public SimpleUserSession(UserForm user, boolean isLoggedIn) {
 		this.user = user;
		this.isLoggedIn = isLoggedIn;
 	}
 
 	@Override
 	public UserForm getUser() {
 		return user;
 	}
 
 	@Override
 	public boolean isUserLoggedIn() {
 		return isLoggedIn;
 	}
 
 }
