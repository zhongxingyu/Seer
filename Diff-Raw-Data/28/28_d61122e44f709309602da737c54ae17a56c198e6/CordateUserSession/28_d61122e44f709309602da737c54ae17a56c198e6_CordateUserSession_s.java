 package se.ryttargardskyrkan.cordate.model;
 
import javax.annotation.PreDestroy;
 
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 import org.springframework.web.context.WebApplicationContext;
 
 @Component
 @Scope(WebApplicationContext.SCOPE_SESSION)
public class CordateUserSession implements UserSession {
 	private String username;
 	private String password;
 	
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 }
