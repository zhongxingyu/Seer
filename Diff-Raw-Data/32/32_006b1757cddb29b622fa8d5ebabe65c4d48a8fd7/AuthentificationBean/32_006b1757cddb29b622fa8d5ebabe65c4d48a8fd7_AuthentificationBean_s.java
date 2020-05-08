 package edu.app.web.mb;
 
 import java.io.Serializable;
 
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 
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
 			navigateTo = "/pages/admin/home";
 
 		} else {
			navigateTo = "/pages/error";
 
 		}
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
 
 }
