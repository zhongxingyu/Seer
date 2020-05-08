 package org.jboss.community.pv243.controller;
 
 import java.io.Serializable;
 
 import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
 import javax.enterprise.inject.Produces;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.jboss.community.pv243.model.User;
 import org.jboss.community.pv243.service.UserManager;
 
 @Named
@SessionScoped
 public class UserController implements Serializable {
 
 	private static final long serialVersionUID = 572238792634619785L;
 
 	@Inject
 	FacesContext facesContext;
 	
 	@Inject
 	UserManager userManager;
 	
 	private User newUser;
 	
 	private boolean edit = false;
 	
 	@Produces
 	@Named
 	public User getNewUser(){
 		return newUser;
 	}
 	
 	public void registerUser(){
 		userManager.registerUser(newUser);
 		facesContext.addMessage(null, new FacesMessage(
 				FacesMessage.SEVERITY_INFO, "Registration successful",
 				"User was successfuly registered"));
 		initiateUser();
 	}
 	
 	public void deleteUser(User user){
 		userManager.deleteUser(user);
 		initiateUser();
 	}
 	
 	public void editUser(User user){
 		newUser = userManager.getUser(user.getId());
 		edit = true;
 	}
 	
 	public void save() {
 		userManager.updateUser(newUser);
 		facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
 				"Update successful", "User was successfuly updated"));
 		edit = false;
 		initiateUser();
 	}
 	
 	public void clear() {
 		edit = false;
 		initiateUser();
 	}
 	
 	public boolean isEdit() {
 		return edit;
 	}
 
 	@PostConstruct
 	public void initiateUser(){
 		if (facesContext.getExternalContext().getRequestServletPath().equals("/userSpace/editInfo.jsf")){
			newUser= userManager.getUserByEmail(facesContext.getExternalContext().getUserPrincipal().getName());
 			edit=true;
 		}else{
 			newUser = new User();
 		}
 	}
 	
 }
