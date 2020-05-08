 package example.springmvc.data;
 
 /**
  * This class stores all information which is needed to register new user.
  * 
  * @author Daniel
  * 
  */
 public class UserRegistrationData {
 
 	private String id = "";
 
 	private String password = "";
 	
 	private String confirmedPassword = "";
 
 	public UserRegistrationData() {
 
 	}
 
 	public UserRegistrationData(String id, String password, String confirmedPassword) {
 		this.id = id;
 		this.password = password;
 		this.confirmedPassword = confirmedPassword;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public String getConfirmedPassword() {
 		return confirmedPassword;
 	}
 
 	public void setConfirmedPassword(String confirmedPassword) {
 		this.confirmedPassword = confirmedPassword;
 	}

 }
