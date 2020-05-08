 package ua.krem.agent.model;
 
 import java.io.Serializable;
 
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.Size;
 
@SuppressWarnings("serial")
 public class User implements Serializable{
 	private Long id;
 	
 	@Size(min=3, max=10, message="Username should be between 3 and 10 chars")
 	@Pattern(regexp="^[\\w\\d]+$", message="Username must be without spaces")
 	private String login;
 	
 	@Size(min=4, max=10, message="Password should be at least 4 chars")
 	private String pass;
 
 	public User(){
 		login = "";
 		pass = "";
 	}
 	
 	public String getLogin() {
 		return login;
 	}
 
 	public void setLogin(String login) {
 		this.login = login;
 	}
 
 	public String getPass() {
 		return pass;
 	}
 
 	public void setPass(String pass) {
 		this.pass = pass;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 }
