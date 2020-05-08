 package net.retakethe.policyauction.business.impl;
 
 import net.retakethe.policyauction.business.api.BusinessManager;
 import net.retakethe.policyauction.business.api.UserManager;
 import net.retakethe.policyauction.data.api.DAOManager;
import net.retakethe.policyauction.services.EmailSender;
 
 import org.apache.tapestry5.ioc.annotations.Inject;
 
 public class BusinessManagerImpl implements BusinessManager {
 	
 	private UserManager userManager;
 	
	public BusinessManagerImpl(@Inject final DAOManager daoManager, @Inject final EmailSender emailSender) {
		userManager = new UserManagerImpl(daoManager, emailSender);
 	}
 
 	@Override
 	public UserManager getUserManager() {
 		return userManager;
 	}
 }
