 package org.csovessoft.contabil.session;
 
 import org.csovessoft.contabil.user.Entity;
 import org.csovessoft.contabil.user.User;
 
 public class Session extends Entity {
 
 	private long sessionTimeout = 0;
 	private long expires;
 	private String language;
 	private String reason;
 	private User user;
 
	public Session(long sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
 	}
 
 	public void setLanguage(String language) {
 		this.language = language;
 	}
 
 	public String getLanguage() {
 		return language;
 	}
 
 	public void setReason(String reason) {
 		this.reason = reason;
 	}
 
 	public String getReason() {
 		return reason;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 	public User getUser() {
 		return user;
 	}
 
 	public void setExpires(long expires) {
 		this.expires = expires;
 	}
 
 	public long getExpires() {
 		return expires;
 	}
 
 	public boolean isExpired() {
 		return getExpires() < System.currentTimeMillis();
 	}
 
 	public void setSessiontimeout(long sessiontimeout) {
 		this.sessionTimeout = sessiontimeout;
 	}
 
 	public long getSessionTimeout() {
 		return sessionTimeout;
 	}
 
 }
