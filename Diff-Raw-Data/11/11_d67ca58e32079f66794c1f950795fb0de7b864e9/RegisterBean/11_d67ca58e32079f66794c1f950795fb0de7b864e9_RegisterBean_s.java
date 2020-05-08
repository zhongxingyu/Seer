 package de.fhb.autobday.beans.actions;
 
 import de.fhb.autobday.beans.SessionBean;
 import de.fhb.autobday.data.AbdUser;
 import de.fhb.autobday.exception.commons.HashFailException;
 import de.fhb.autobday.exception.user.IncompleteUserRegisterException;
 import de.fhb.autobday.exception.user.NoValidUserNameException;
 import de.fhb.autobday.manager.user.UserManagerLocal;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.enterprise.context.RequestScoped;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 /**
  * ActionBean for register-form.
  *
  * @author Michael Koppen mail: koppen@fh-brandenburg.de
  */
 @Named
 @RequestScoped
 public class RegisterBean {
 
 	private final static Logger LOGGER = Logger.getLogger(RegisterBean.class.getName());
 	@Inject
 	private UserManagerLocal userManager;
 	@Inject
 	private SessionBean sessionBean;
 	private String firstName;
 	private String name;
 	private String userName;
 	private String mail;
 	private String password;
 	private String passwordWdhl;
 
 	/**
 	 * Creates a new instance of RegisterBean
 	 */
 	public RegisterBean() {
 	}
 
 	public String register() {
 
 		try {
 			AbdUser user = userManager.register(firstName, name, userName, mail, password, passwordWdhl);
 			sessionBean.setAktUser(user);
 		} catch (IncompleteUserRegisterException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 			FacesContext.getCurrentInstance().addMessage(
 					null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
 			return "register";
 		} catch (NoValidUserNameException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 			FacesContext.getCurrentInstance().addMessage(
 					null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
 			return "register";
 		} catch (HashFailException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 			FacesContext.getCurrentInstance().addMessage(
 					null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
 			return "register";
 		}
 		FacesContext.getCurrentInstance().addMessage(
				null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Congratulations! You are registered for Auto-B-Day. Please check your mails!", ""));
 		return "index";
 	}
 
 	public String getMail() {
 		return mail;
 	}
 
 	public void setMail(String mail) {
 		this.mail = mail;
 	}
 
 	public String getPasswordWdhl() {
 		return passwordWdhl;
 	}
 
 	public void setPasswordWdhl(String passwordWdhl) {
 		this.passwordWdhl = passwordWdhl;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public String getFirstName() {
 		return firstName;
 	}
 
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getUserName() {
 		return userName;
 	}
 
 	public void setUserName(String userName) {
 		this.userName = userName;
 	}
 }
