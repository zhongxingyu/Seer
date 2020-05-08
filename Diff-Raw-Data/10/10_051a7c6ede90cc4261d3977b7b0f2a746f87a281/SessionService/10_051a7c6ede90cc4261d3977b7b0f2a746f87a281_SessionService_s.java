 package net.pdp7.tvguide.spring.web;
 
 import org.springframework.web.context.request.WebRequest;
 
 public class SessionService {
 
 	protected static final String USER_ID = "userId";
 
	public void signIn(long userId, WebRequest request) {
 		request.setAttribute(USER_ID, userId, WebRequest.SCOPE_SESSION);
 	}
 	
 	public void signOff(WebRequest request) {
 		request.removeAttribute(USER_ID, WebRequest.SCOPE_SESSION);
 	}
 	
 	public Long getUserId(WebRequest request) {
 		return (Long) request.getAttribute(USER_ID, WebRequest.SCOPE_SESSION);
 	}
 
 	
 }
