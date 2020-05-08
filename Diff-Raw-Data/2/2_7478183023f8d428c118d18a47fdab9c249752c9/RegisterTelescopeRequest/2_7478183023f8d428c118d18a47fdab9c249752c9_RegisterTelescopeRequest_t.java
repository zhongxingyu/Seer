 /**
  * Author: Fernando Serena (fserena@ciclope.info)
  * Organization: Ciclope Group (UPM)
  * Project: GLORIA
  */
 package eu.gloria.gs.services.api.resources;
 
 /**
  * @author Fernando Serena (fserena@ciclope.info)
  * 
  */
public class RegisterTelescopeRequest {
 
 	private String url;
 	private String owner;
 	private String user;
 	private String password;
 
 	public String getUrl() {
 		return url;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	public String getOwner() {
 		return owner;
 	}
 
 	public void setOwner(String owner) {
 		this.owner = owner;
 	}
 
 	public String getUser() {
 		return user;
 	}
 
 	public void setUser(String user) {
 		this.user = user;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 }
