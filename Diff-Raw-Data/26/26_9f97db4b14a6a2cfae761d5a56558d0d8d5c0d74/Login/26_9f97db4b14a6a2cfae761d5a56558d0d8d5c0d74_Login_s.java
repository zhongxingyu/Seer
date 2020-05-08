 package com.sirra.api.login;
 
 import java.util.*;
 
 import javax.servlet.http.*;
 import javax.ws.rs.*;
 
 import com.sirra.appcore.sql.*;
 import com.sirra.appcore.users.*;
 import com.sirra.server.rest.*;
 import com.sirra.server.rest.annotations.*;
 import com.sirra.server.session.*;
 
 public class Login extends ApiBase {
 	
 	@POST
 	@Parameters({"username", "password"})
 	public Map login(String username, String password) {
 			
 		Finder finder = new Finder();
 	
		BaseUser user = finder.findByField(Finder.userClass, "email", username);
 		
 		if(user == null) {
 			return fail("Incorrect username.");
 		}
 		
 		if(!user.verifyPassword(password)) {
 			return fail("Incorrect password.");
 		}
 		
 		UserSession userSession = UserSession.newUserSession(username, user.getId(), user.getAccountId());
 		save(userSession);
 		
 		addSessionCookie("sirra-session-id", userSession.getSessionId());
 		
 		Map map = success();
 		map.put("user", user);
 		return map;
 	}
 	
 	public void addSessionCookie(String name, String value) {
 		Cookie cookie = new Cookie(name, value);
 		cookie.setPath("/");
 		SirraSession.get().getResponse().addCookie(cookie);
 	}
 }
