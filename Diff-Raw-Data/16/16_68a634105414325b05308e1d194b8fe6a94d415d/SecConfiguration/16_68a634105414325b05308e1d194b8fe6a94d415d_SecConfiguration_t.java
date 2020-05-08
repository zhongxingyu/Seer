 package org.mdissjava.mdisscore.view.configuration;
 
 import java.io.IOException;
 import java.io.Serializable;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 import javax.servlet.ServletException;
 
 import org.mdissjava.mdisscore.controller.bll.UserManager;
 import org.mdissjava.mdisscore.controller.bll.impl.UserManagerImpl;
 import org.mdissjava.mdisscore.model.pojo.User;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 @ManagedBean
 @RequestScoped
 public class SecConfiguration{
 	
 	private User user;
 	private String userNick;
 	private UserManager userManager;
 	
 	public SecConfiguration()
 	{
 		this.userNick = retrieveSessionUserNick();	
 		userManager = new UserManagerImpl();		
 		this.setUser(userManager.getUserByNick(this.userNick));	
 	}
 
 	
 	private String retrieveSessionUserNick() {
 		  Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		  return auth.getName();	   
 		 }
 
 
 	public User getUser() {
 		return user;
 	}
 
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 	
 	public String doSecuritySave() throws ServletException, IOException
 	{
 		userManager.saveUser(this.user);
 		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Security settings updated"));
 		return null;
 	}
 
 }
