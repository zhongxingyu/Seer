 package edu.app.web.mb;
 
 import java.io.Serializable;
 
 import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
 
 import edu.app.business.AuthenticationServiceLocal;
 import edu.app.persistence.User;
 
 @ManagedBean(name = "authbean")
 @SessionScoped
 public class AuthentificationBean implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private User user = new User();
	private boolean loggedIn = false;
 	// injection
 	@EJB
 	private AuthenticationServiceLocal authenticationServiceLocal;
 
 	// our methods
 	public String login() {
 		String navigateTo = null;
 		User userFound = authenticationServiceLocal.authenticate(
 				user.getLogin(), user.getPassword());
 		if (userFound != null) {
 			user = userFound;
			loggedIn = true;
 			navigateTo = "/pages/admin/home";
 
 		} else {
			loggedIn = false;
			FacesMessage message = new FacesMessage("bad credentials ");
			FacesContext.getCurrentInstance().addMessage(
					"login_form:login_submit", message);
			navigateTo = "";
 
 		}
 		return navigateTo;
 	}
 
	public String logout() {
		String navigateTo = null;
		loggedIn = false;
		user = new User();
		navigateTo = "/welcome";
		return navigateTo;
	}

 	public AuthentificationBean() {
 	}
 
 	public static long getSerialversionuid() {
 		return serialVersionUID;
 	}
 
 	public User getUser() {
 		return user;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 
	public boolean isLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

 }
