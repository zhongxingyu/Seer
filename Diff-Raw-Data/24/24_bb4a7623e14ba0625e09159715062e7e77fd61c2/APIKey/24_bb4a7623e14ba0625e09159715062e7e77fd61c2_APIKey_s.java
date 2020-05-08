 package edu.emory.cci.bindaas.trusted_app_client.core;
 
import java.util.Date;

 import com.google.gson.annotations.Expose;
 
 public class APIKey {
 	@Expose private String value;
	@Expose private Date expires;
 	@Expose private String applicationName;
 	@Expose private String applicationID;
 	
 	
 	
 	public String getValue() {
 		return value;
 	}
 	public void setValue(String value) {
 		this.value = value;
 	}
	public Date getExpires() {
 		return expires;
 	}
	public void setExpires(Date expires) {
 		this.expires = expires;
 	}
 	public String getApplicationName() {
 		return applicationName;
 	}
 	public void setApplicationName(String applicationName) {
 		this.applicationName = applicationName;
 	}
 	public String getApplicationID() {
 		return applicationID;
 	}
 	public void setApplicationID(String applicationID) {
 		this.applicationID = applicationID;
 	}
	public void setExpires(long asLong) {
		this.expires = new Date(asLong);
		
	}
 	
 	
 	
 }
