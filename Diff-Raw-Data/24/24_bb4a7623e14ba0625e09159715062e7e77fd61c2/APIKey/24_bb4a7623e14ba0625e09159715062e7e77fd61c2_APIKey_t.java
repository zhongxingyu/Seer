 package edu.emory.cci.bindaas.trusted_app_client.core;
 
 import com.google.gson.annotations.Expose;
 
 public class APIKey {
 	@Expose private String value;
	@Expose private String expires;
 	@Expose private String applicationName;
 	@Expose private String applicationID;
 	
 	
 	
 	public String getValue() {
 		return value;
 	}
 	public void setValue(String value) {
 		this.value = value;
 	}
	public String getExpires() {
 		return expires;
 	}
	public void setExpires(String expires) {
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
	
 	
 	
 	
 }
