 package net.latroquette.web.beans.profile;
 
 import java.io.Serializable;
 import java.sql.Timestamp;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIInput;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ComponentSystemEvent;
 import javax.servlet.http.HttpServletRequest;
 
 import net.latroquette.common.database.IDatabaseConstants;
 import net.latroquette.common.database.data.model.users.User;
 import net.latroquette.common.database.data.model.users.Users;
 import net.latroquette.common.database.session.DatabaseSession;
 import net.latroquette.common.security.Security;
 
 
 @ManagedBean
 @SessionScoped
 public class UserBean extends User implements Serializable{
 	
 	public static final int ANONYMOUS = -1;
 	public static final int NOT_LOGGED_IN = 0;
 	public static final int NEW_USER = 1;
 	public static final int LOGGED_IN = 2;	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 173422903879328102L;
 	private transient String passwordConfirm;
 	private transient String mailConfirm;
 	private int loginState;
 
 	/**
 	 * @return the passwordConfirm
 	 */
 	public String getPasswordConfirm() {
 		return passwordConfirm;
 	}
 	/**
 	 * @param passwordConfirm the passwordConfirm to set
 	 */
 	public void setPasswordConfirm(String passwordConfirm) {
 		this.passwordConfirm = passwordConfirm;
 	}
 	/**
 	 * @return the mailConfirm
 	 */
 	public String getMailConfirm() {
 		return mailConfirm;
 	}
 	/**
 	 * @param mailConfirm the mailConfirm to set
 	 */
 	public void setMailConfirm(String mailConfirm) {
 		this.mailConfirm = mailConfirm;
 	}
 
 	/**
 	 * @return the newUser
 	 */
 	public int getLoginState() {
 		return loginState;
 	}
 	/**
 	 * @param newUser the newUser to set
 	 */
 	public void getLoginState(int loginState) {
 		this.loginState = loginState;
 	}
 	public void validateAddressMail(ComponentSystemEvent event){
 
 		FacesContext fc = FacesContext.getCurrentInstance();
 
 		UIComponent components = event.getComponent();
 
 		//get textbox1 value
 		UIInput uiText1 = (UIInput)components.findComponent("addressMailField");
 		
 
 		//get textbox2 value
 		UIInput uiText2 = (UIInput)components.findComponent("addressMailFieldConfirmation");
 		if(uiText1 != null && uiText2 != null && uiText2.getLocalValue() != null && uiText1.getLocalValue() != null){
 			String text1 = uiText1.getLocalValue().toString();
 			String text2 = uiText2.getLocalValue().toString();
 	
 			if(!text1.equals(text2)){
 	
 				FacesMessage msg = new FacesMessage("Email check failed", 
 						"Address mail do not match with previous given");
 	
 				msg.setSeverity(FacesMessage.SEVERITY_ERROR);
 	
 				fc.addMessage(components.getClientId(), msg);
 	
 				//passed to the Render Response phase
 				fc.renderResponse();
 			}
 		}
 	}
 	public void validatePassword(ComponentSystemEvent event){
 
 		FacesContext fc = FacesContext.getCurrentInstance();
 
 		UIComponent components = event.getComponent();
 
 		//get textbox1 value
 		UIInput uiText1 = (UIInput)components.findComponent("passwordField");
 		
 
 		//get textbox2 value
 		UIInput uiText2 = (UIInput)components.findComponent("passwordFieldConfirmation");
 		if(uiText1 != null && uiText2 != null && uiText2.getLocalValue() != null && uiText1.getLocalValue() != null){
 			String text2 = uiText2.getLocalValue().toString();
 			String text1 = uiText1.getLocalValue().toString();
 			if(!text1.equals(text2)){
 	
 				FacesMessage msg = new FacesMessage("Password check failed", 
 						"Password do not match with previous given");
 	
 				msg.setSeverity(FacesMessage.SEVERITY_ERROR);
 	
 				fc.addMessage(components.getClientId(), msg);
 	
 				//passed to the Render Response phase
 				fc.renderResponse();
 			}
 		}
 	}
 	public String registerUser()
 	{
 		loginState = NOT_LOGGED_IN;
 		User newUser = new User();
 		this.setLogginUserInfo(newUser);
 		DatabaseSession db =  new DatabaseSession();
 		newUser.setDatabaseOperation(IDatabaseConstants.INSERT);
 		db.persist(newUser);
 		db.commit();
 		this.loginState = NEW_USER;
 		return "test";
 
 	}
 	public String loginUser()
 	{
 		loginState = NOT_LOGGED_IN;
 		Users userSearch = new Users();
 		User user = userSearch.getUserByLogin(this.getLogin());
 		if(user.getPassword() != null && user.getPassword().equals(this.getPassword())){
 			this.setLogginUserInfo(user);
 			this.loginState = LOGGED_IN;
 		}
 		user.setDatabaseOperation(IDatabaseConstants.UPDATE);
 		userSearch.persist(user);
 		userSearch.commit();
 		switch (loginState) {
 		case LOGGED_IN:
 			return "index";
 		default:
 			return "login?logInFail="+loginState; 
 		}
 	}
 	public String logoutUser()
 	{
 		
 		Users userSearch = new Users();
 		User user = userSearch.getUserByLogin(this.getLogin());
 		user.setToken(null);
 		user.setDatabaseOperation(IDatabaseConstants.UPDATE);
 		userSearch.persist(user);
 		userSearch.commit();
 		//Unset properties of this user
 		this.copyProperties(new User());
 		loginState = NOT_LOGGED_IN;
 		switch (loginState) {
 		case NOT_LOGGED_IN:
 			return "index";
 		default:
 			return "login#?logInFail="+loginState; 
 		}
 	}
 	private void setLogginUserInfo(User user){
 		Timestamp now = new Timestamp(new java.util.Date().getTime());
 		ExternalContext externe = FacesContext.getCurrentInstance().getExternalContext();
 		String ip = ((HttpServletRequest)externe.getRequest()).getRemoteAddr();
 		String host = ((HttpServletRequest)externe.getRequest()).getRemoteHost();
 		user.setLastDateLogin(now);
 		user.setLastIpLogin(ip);
 		user.setLastHostNameLogin(host);
 		user.setToken(Security.generateSessionID(this.hashCode(), user) );
 		this.copyProperties(user);
 	}
 	
 	private void copyProperties(User user){
 		this.setId(user.getId());
 		this.setMail(user.getMail());
 		this.setLogin(user.getLogin());
 		this.setLastDateLogin(user.getLastDateLogin());
 		this.setLastIpLogin(user.getLastIpLogin());
 		this.setLastHostNameLogin(user.getLastHostNameLogin());
 		this.setToken(user.getToken());
 	}
 	
 	public Integer getLoggedIn()
 	{
 		return (loginState == LOGGED_IN || loginState == NEW_USER) ? new Integer(1) : null ;
 	}
 	
 }
