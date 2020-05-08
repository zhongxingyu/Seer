 package web;
 
 import java.io.Serializable;
 
 import javax.enterprise.context.SessionScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import service.*;
 import domain.Role;
 
 @SessionScoped
 @Named
 public class LoginBean implements Serializable{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	@Inject 
 	LoginInterface loginManager;
 	
 	@Inject
 	UserInterface userManager;
 	
 	private static boolean loggedIn = false;
 	private static long id;
 	private String login;
 	private String password;
 	private Role role;
 	
 	public static long getCurrentUID() {
 		if(loggedIn == true)
 			return id;
 		else
 			return (Long) null;
 	}
 	
 	public boolean isLoggedIn() {
 		return loggedIn;
 	}
 	public void setLoggedIn(boolean loggedIn) {
 		LoginBean.loggedIn = loggedIn;
 	}
 	public long getId() {
 		return id;
 	}
 	public void setId(long id) {
 		LoginBean.id = id;
 	}
 	public String getLogin() {
 		return login;
 	}
 	public void setLogin(String login) {
 		this.login = login;
 	}
 	public String getPassword() {
 		return password;
 	}
 	public void setPassword(String password) {
 		this.password = password;
 	}
 	public void setRole(Role role) {
 		this.role = role;
 	}
 	public Role getRole() {
 		return role;
 	}
 	
 	//ACTIONS.
 	public String login() {
 		if(password.isEmpty()) {
 			return "/panel/error";
 		}
 		else if(loginManager.login(id, password)) {
 			loggedIn = true;
 			login = loginManager.convertIdToLogin(id);
 			role = loginManager.convertIdToRole(id);
			return "/panel/success";
 		}
 		else {
 			return "/panel/error";
 		}
 	}
 	
 	public String logout() {
 		if(!loggedIn) {
 			return "/panel/error";
 		}
 		id = 0;
 		login = null;
 		password = null;
 		loggedIn = false;
		return "/panel/success";
 	}
 	
 	public String logAsUser() {
 		if(loggedIn) {
 			return "/panel/user";
 		}
 		else {
 			return "/panel/login";
 		}
 	}
 	public String logAsTeacher() {
 		if(loggedIn && (role.equals(Role.TEACHER) || role.equals(Role.ADMIN))) {
 			return "/panel/teacher";
 		}
 		else {
 			return "/panel/login";
 		}
 	}
 	public String logAsAdmin() {
 		if(loggedIn && role.equals(Role.ADMIN)) {
 			return "/panel/admin";
 		}
 		else {
 			return "/panel/login";
 		}
 	}
	
		
 }
