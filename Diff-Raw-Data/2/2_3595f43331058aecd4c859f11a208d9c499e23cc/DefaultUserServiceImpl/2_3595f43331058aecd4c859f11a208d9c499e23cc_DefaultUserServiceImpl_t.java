 package org.jasig.portlet.blackboardvcportlet.service.impl;
 
 import org.jasig.portlet.blackboardvcportlet.data.User;
 import org.jasig.portlet.blackboardvcportlet.service.UserService;
 
 public class DefaultUserServiceImpl implements UserService {
 
 	@Override
 	public User getUserDetails(String searchTerm) {
 		User myFakeUser = new User();
 		myFakeUser.setDisplayName("Bruce Wayne");
 		myFakeUser.setEmail("bruce@batcave.com");
		myFakeUser.setUid(searchTerm);
 		return myFakeUser;
 	}
 
 }
