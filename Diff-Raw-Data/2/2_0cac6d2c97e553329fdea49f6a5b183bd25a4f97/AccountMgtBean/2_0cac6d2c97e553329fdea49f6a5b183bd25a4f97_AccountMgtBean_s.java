 package beans;
 
 import java.io.Serializable;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 
 import org.jasypt.util.password.BasicPasswordEncryptor;
 
 import utilities.JdbcUtil;
 
 /**
  * @author Arisa C. Ochavez
  *
  * Backing bean for the user account management functionalities of the application
  */
 @ManagedBean
 @SessionScoped
 public class AccountMgtBean implements Serializable {
 
 	
 	/**
 	 * Global variables 
 	 */
 	private static final long serialVersionUID = 1L;
 	private static int userId;
 	private String username, password;
 	private boolean loggedIn = false, expert = false, admin = false;
 	private BasicPasswordEncryptor pass = new BasicPasswordEncryptor();
 	
 	/**
 	 * Constructor
 	 */
 	public AccountMgtBean () {
 		
 	}
 	
 	/**
 	 * Getter for the user id assigned to the user by the database
 	 * @return userId - session identification for the logged in user
 	 */
 	public static int getUserId() {
 		return userId;
 	}
 
 	/**
 	 * Setter for the user id assigned to the user by the database
 	 * @param userId - session identification for the logged in user
 	 */
 	public void setUserId(int userId) {
 		AccountMgtBean.userId = userId;
 	}
 
 
 	/**
 	 * Getter for the user's username
 	 * @return username - string used to log in the system
 	 */
 	public String getUsername() {
 		return username;
 	}
 
 	
 	/**
 	 * Setter for the user's username
 	 * @param username - string used to log in the system
 	 */
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	/**
 	 * Getter for the user's password
 	 * @return password - string that authenticates user log in
 	 */
 	public String getPassword() {
 		return password;
 	}
 	
 	
 	/**
 	 * Setter for the user's password
 	 * @param password - string that authenticates user log in
 	 */
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	/**
 	 * Getter for the marker if a user is currently logged in
 	 * @return loggedIn - true if user is logged in, otherwise, false
 	 */
 	public boolean isLoggedIn() {
 		return loggedIn;
 	}
 
 	/**
 	 * Setter for the marker if a user is currently logged in
 	 * @param loggedIn - true if user is logged in, otherwise, false
 	 */
 	public void setLoggedIn(boolean loggedIn) {
 		this.loggedIn = loggedIn;
 	}
 
 	/**
 	 * Getter for marker of type of currently logged in user
 	 * @return expert - true if logged in user is an expert, otherwise, false
 	 */
 	public boolean isExpert() {
 		return expert;
 	}
 
 	/**
 	 * Setter for marker of type of currently logged in user
 	 * @param expert - true if logged in user is an expert, otherwise, false
 	 */
 	public void setExpert(boolean expert) {
 		this.expert = expert;
 	}
 
 	/**
 	 * Getter for marker if user logged in as an administrator
 	 * @return admin - true if logged in user is admin, otherwise, false
 	 */
 	public boolean isAdmin() {
 		return admin;
 	}
 
 	/**
 	 * Setter for marker if user logged in as an administrator
 	 * @param admin - true if logged in user is admin, otherwise, false
 	 */
 	public void setAdmin(boolean admin) {
 		this.admin = admin;
 	}
 
 	/**
 	 * Method for checking if the user entered valid credentials and is therefore allowed to open the application 
 	 */
 	public void logIn () {
 		FacesContext context = FacesContext.getCurrentInstance();  
 		Connection conn = JdbcUtil.startConnection();
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		
 		try {
 			//Check if the username and password are in the database
 			ps = conn.prepareStatement("SELECT user_id, password, type, user_type FROM user WHERE username = ?");
 				ps.setString(1, username);	
 			rs = ps.executeQuery();
 			
 			if (rs.next()) {
 				if (pass.checkPassword(password, rs.getString(2))) {
 					loggedIn = true;		//Allow log in
 					setUserId(rs.getInt(1));
 					
 					//Check if logged in user is an admin
 					if (rs.getString(3).equals("ADMIN")) {
 						admin = true;
 						expert = true;
 					} else {
 						//Check if the logged in user is an expert
 						if (rs.getString(4).equals("EX"))
 							expert = true;
 					}
 					
 					//Initialize contents	
 					InferenceBean.start();
 					SPTBean.start();
 					SKDBean.start();
 					PatientInfoBean.start();
 					
 					context.addMessage(null, new FacesMessage("Log In Successful", "Hello " + username + "!"));
 				} else {
 					username = "";
 					password = "";
 					loggedIn = false;		//Reject log in
 					context.addMessage(null, new FacesMessage("Log In Failed", "Please re-enter credentials."));
 				}
 			} else {
 				username = "";
 				password = "";
 				loggedIn = false;		//Reject log in
 				context.addMessage(null, new FacesMessage("Log In Failed", "Please re-enter credentials."));
 			}	
 		} catch (SQLException e) {
 			e.printStackTrace();
 			context.addMessage(null, new FacesMessage("Internal Error:", "Error Occured, Please Refresh Page."));
 		} finally {
 			try {
 				if (conn != null)
 					conn.close();
 				if (ps != null)
 					ps.close();
 				if (rs != null)
 					rs.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * Method for setting user credentials to null and log him/her out of the application
 	 * @param e - variable for the click of the log out button
 	 */
 	public void logOut (ActionEvent e) {
 		userId = 0;
 		username = "";
 		password = "";
 		loggedIn = false;
 		expert = false;
 		SPTBean.resetSPT();
 	}
 }
