 package net.retakethe.policyauction.business.impl;
 
 import net.retakethe.policyauction.business.api.BusinessManager;
 import net.retakethe.policyauction.business.api.UserManager;
 import net.retakethe.policyauction.data.api.DAOManager;
 
 import org.apache.tapestry5.ioc.annotations.Inject;
 
 public class BusinessManagerImpl implements BusinessManager {
 	
 	private UserManager userManager;
 	
	@Inject
	private DAOManager daoManager;
	
	public BusinessManagerImpl() {
		userManager = new UserManagerImpl(daoManager);
 	}
 
 	@Override
 	public UserManager getUserManager() {
 		return userManager;
 	}

 }
