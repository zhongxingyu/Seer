 package com.operativus.senacrs.audit.model.config;
 
 

 public class Configuration {
 
	private static final String SEP_PIPE = "|";
 	private String baseUrl = null;
 	private String version = null;
 	private String username = null;
 	private String password = null;
 	
 	public Configuration() {
 
 		super();
 	}
 
 	
 	public String getBaseUrl() {
 	
 		return baseUrl;
 	}
 
 	
 	public void setBaseUrl(String baseUrl) {
 	
 		this.baseUrl = baseUrl;
 	}
 
 	
 	public String getVersion() {
 	
 		return version;
 	}
 
 	
 	public void setVersion(String version) {
 	
 		this.version = version;
 	}
 
 	
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
 
 
 	@Override
 	public int hashCode() {
 
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((baseUrl == null) ? 0 : baseUrl.hashCode());
 		result = prime * result + ((password == null) ? 0 : password.hashCode());
 		result = prime * result + ((username == null) ? 0 : username.hashCode());
 		result = prime * result + ((version == null) ? 0 : version.hashCode());
 		return result;
 	}
 
 
 	@Override
 	public boolean equals(Object obj) {
 
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Configuration other = (Configuration) obj;
 		if (baseUrl == null) {
 			if (other.baseUrl != null)
 				return false;
 		} else if (!baseUrl.equals(other.baseUrl))
 			return false;
 		if (password == null) {
 			if (other.password != null)
 				return false;
 		} else if (!password.equals(other.password))
 			return false;
 		if (username == null) {
 			if (other.username != null)
 				return false;
 		} else if (!username.equals(other.username))
 			return false;
 		if (version == null) {
 			if (other.version != null)
 				return false;
 		} else if (!version.equals(other.version))
 			return false;
 		return true;
 	}
	
	@Override
	public String toString() {
 		
		StringBuilder result = null;
		
		result = new StringBuilder();
		result.append(this.getClass().getSimpleName());
		result.append(SEP_PIPE);
		result.append(this.baseUrl);
		result.append(SEP_PIPE);
		result.append(this.version);
		result.append(SEP_PIPE);
		result.append(this.username);
		result.append(SEP_PIPE);
		result.append(this.password);
	
		return result.toString();
	}
 }
