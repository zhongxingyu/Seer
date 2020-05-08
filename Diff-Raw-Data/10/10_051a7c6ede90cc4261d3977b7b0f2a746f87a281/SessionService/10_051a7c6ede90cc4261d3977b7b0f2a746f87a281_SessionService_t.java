 package net.pdp7.tvguide.spring.web;
 
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.NativeWebRequest;
 import org.springframework.web.context.request.WebRequest;
 
 public class SessionService {
 
 	protected static final String USER_ID = "userId";
	protected static final int SIGNED_USER_SESSION_DURATION_SECONDS = 60*60*24*7;
 
	public void signIn(long userId, NativeWebRequest request) {
 		request.setAttribute(USER_ID, userId, WebRequest.SCOPE_SESSION);
		request.getNativeRequest(HttpServletRequest.class).getSession().setMaxInactiveInterval(SIGNED_USER_SESSION_DURATION_SECONDS);
 	}
 	
 	public void signOff(WebRequest request) {
 		request.removeAttribute(USER_ID, WebRequest.SCOPE_SESSION);
 	}
 	
 	public Long getUserId(WebRequest request) {
 		return (Long) request.getAttribute(USER_ID, WebRequest.SCOPE_SESSION);
 	}
 
 	
 }
