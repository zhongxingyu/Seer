 package net.retakethe.policyauction.services.impl;
 
 import net.retakethe.policyauction.data.api.DAOManager;
 import net.retakethe.policyauction.data.api.UserManager;
 import net.retakethe.policyauction.data.api.UserManager.NoSuchUserException;
 import net.retakethe.policyauction.data.api.dao.UsernameDAO;
 import net.retakethe.policyauction.data.impl.manager.InitializationException;
 import net.retakethe.policyauction.entities.EntityFactory;
 import net.retakethe.policyauction.entities.User;
 import net.retakethe.policyauction.services.AppModule;
 import net.retakethe.policyauction.services.AuthenticationService;
 
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.mindrot.jbcrypt.BCrypt;
 
 public class AuthenticationServiceImpl implements AuthenticationService {
 
 	private final UserManager userManager;
 	
	@Inject
	private DAOManager daoManager;
	
     /**
      * Constructor used by {@link AppModule#bind(org.apache.tapestry5.ioc.ServiceBinder)}
      *
      * @throws InitializationException
      */
     @Inject 
	public AuthenticationServiceImpl() {
 		this.userManager = daoManager.getUserManager();
 	}
 	
 	@Override
 	public User login(final String username, final String password) {
 		try {
 			UsernameDAO usernameDAO = userManager.getUsername(username);
 			if (usernameDAO != null) {
 				User unAuthenticatedUser = EntityFactory.makeUser(userManager.getUser(usernameDAO.getUserID()));
 				if (BCrypt.checkpw(password, unAuthenticatedUser.getPasswordHash())) {
 					return unAuthenticatedUser;
 				}
 			}
 		} catch (NoSuchUserException e) {
 			// TODO this should be logging.
 		}
 		return null;
 	}
 
 }
