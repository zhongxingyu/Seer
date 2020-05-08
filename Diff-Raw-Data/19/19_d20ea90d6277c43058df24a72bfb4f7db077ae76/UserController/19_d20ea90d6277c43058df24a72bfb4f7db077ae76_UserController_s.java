 package cz.muni.fi.pv243.controller;
 
 import java.security.NoSuchAlgorithmException;
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.inject.Model;
 import javax.enterprise.inject.Produces;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.servlet.http.HttpServletRequest;
 
 import org.jboss.seam.security.BaseAuthenticator;
 import org.jboss.seam.security.Credentials;
 import org.jboss.seam.security.Identity;
 import org.picketlink.idm.impl.api.PasswordCredential;
 
 import cz.muni.fi.pv243.model.User;
 import cz.muni.fi.pv243.security.Authorization;
 import cz.muni.fi.pv243.security.Encoder;
 import cz.muni.fi.pv243.service.UserManager;
 
 @Model
 public class UserController extends BaseAuthenticator {
 
 	@Inject
 	private Logger log;
 	
 	@Inject
 	private FacesContext facesContext;
 
 	@Inject
 	private UserManager userManager;
 
 	@Inject
 	private Credentials credentials;
 
 	@Inject
 	private Identity identity;
 
 	private User newUser;
 
 	@Produces
 	@Named
 	public User getNewUser() {
 		return newUser;
 	}
 
 	public boolean isAdmin(Identity identity) {
 		return Authorization.isAdmin(identity);
 	}
 
 	public boolean isCustomer(Identity identity) {
 		return Authorization.isCustomer(identity);
 	}
 
 	@PostConstruct
 	public void initNewUser() {
 		newUser = new User();
 	}
 
 	public void register() throws Exception {
 		log.finest("Registering");
 		try {
 			userManager.create(newUser);
 			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
 					"Registered!", "Registration successful");
 			facesContext.addMessage("registerForm:registerMessages", m);
 			initNewUser();
 			log.info("Registration success");
 		} catch (Exception e) {
 			log.info("Registration failed");
 			facesContext.addMessage("registerForm:registerMessages",
 					new FacesMessage("Registration failed."));
 		}
 	}
 
	public void auth() {
 		log.finest("Calling auth method");
 		identity.login();
 		if(!identity.isLoggedIn()){
 			return;
 		}
 		HttpServletRequest request = (HttpServletRequest) FacesContext
 				.getCurrentInstance().getExternalContext().getRequest();

 		try {
 			if (!request
 					.getHeader("Referer")
 					.substring(
 							request.getHeader("Referer").lastIndexOf('/') + 1)
 					.equals("login.jsf")) {
 				log.fine("Auth method invoked from previously unaccessible page.");
 				FacesContext
 						.getCurrentInstance()
 						.getExternalContext()
 						.redirect(
 								request.getHeader("Referer").substring(
 										request.getHeader("Referer")
 												.lastIndexOf('/') + 1));
 			} else {
 				if (!isAdmin(identity)) {
 					log.fine("Auth method ended as user");
 					FacesContext.getCurrentInstance().getExternalContext()
 							.redirect("user_profile.jsf");
 				} else {
 					log.fine("Auth method ended as admin");
 					FacesContext.getCurrentInstance().getExternalContext()
 							.redirect("admin_page.jsf");
 				}
 			}
 
 		} catch (Exception e) {
 			log.warning("Auth method failure: " + e.toString());
 		}
 	}
 
 	@Override
 	public void authenticate() {
 		log.finest("Calling authenticate method for identity");
 		User userFromDb = userManager.findByEmail(credentials.getUsername());
 		if (userFromDb == null) {
 			log.info("Null user tried to log in");
 			setStatus(AuthenticationStatus.FAILURE);
 			facesContext.addMessage("loginForm:username", new FacesMessage(
 					FacesMessage.SEVERITY_ERROR, "Non existing user", "Non existing user"));
 		} else {
 			try {
 				if (userFromDb.getPasswordHash().equals(
 						Encoder.encode(((PasswordCredential) credentials
 								.getCredential()).getValue(), credentials
 								.getUsername()))) {
 					// login success
 					facesContext.addMessage("loginForm:password",
 							new FacesMessage("Good password"));
 					setStatus(AuthenticationStatus.SUCCESS);
 					setUser(userFromDb);
 
 				} else {
 					// login failed
 					log.info("Login failed");
 					facesContext.addMessage("loginForm:password",
 							new FacesMessage(FacesMessage.SEVERITY_ERROR, "Wrong password","Wrong password"));
 					setStatus(AuthenticationStatus.FAILURE);
 				}
 			} catch (NoSuchAlgorithmException e) {
 				log.warning("Exception raised while logging in: " + e.toString());
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
