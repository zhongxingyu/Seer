 package br.com.fourlinux.videostore;
 
 import java.util.Map;
 
 import javax.ejb.EJB;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 
 import br.com.fourlinux.videostore.domain.User;
 import br.com.fourlinux.videostore.ejb.UserManagerSessionBean;
 
 @ManagedBean
 @RequestScoped
 public class LoginBean {
 	private String email;
 	private String password;
 
 	@EJB
 	private UserManagerSessionBean users;
 
 	public void login() {
 		if (email != null || !("".equals(email))) {
 			User user = users.getUserByEmail(email);
 			if (user != null) {
 				if (password.equals(user.getPassword())) {
 					ExternalContext context = FacesContext.getCurrentInstance()
 							.getExternalContext();
 					Map<String, Object> sessionMap = context.getSessionMap();
 					sessionMap.put("user", user);
 					email = null;
 					password = null;
 				} else {
 					FacesMessage msg = new FacesMessage(
 							FacesMessage.SEVERITY_ERROR,
 							"Email/Senha incorretos", null);
					FacesContext.getCurrentInstance().addMessage("login-form:login-user-email",
 							msg);
 				}
 			} else {
 				FacesMessage msg = new FacesMessage(
 						FacesMessage.SEVERITY_ERROR, "Email/Senha incorretos",
 						null);
				FacesContext.getCurrentInstance().addMessage("login-form:login-user-email", msg);
 			}
 		}
 	}
 
 	public void logout() {
 		ExternalContext context = FacesContext.getCurrentInstance()
 				.getExternalContext();
 		Map<String, Object> sessionMap = context.getSessionMap();
 		sessionMap.remove("user");
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 }
