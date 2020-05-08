 package com.desklampstudios.edab;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.core.UriBuilder;
 
 import com.desklampstudios.edab.User.Gender;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 public class GoogleOAuthClient {
 	private static final Logger log = Logger.getLogger(GoogleOAuthClient.class.getName());
 
 	private static final String LOGIN_PATH = "/logincallback";
 	private static final String SCOPES = "openid profile email";
 
 	/*
 	protected static String getRedirectURL() {
     	if (LoginCallbackServlet.isProduction) {
     		return getRedirectURL(SERVER_HOST);
     	} else {
     		return getRedirectURL(DEBUG_HOST);
     	}
 	}
 	 */
 	protected static String getRedirectURL(String host) {
 		return host + LOGIN_PATH;
 	}
 	protected static String getRequestHost(HttpServletRequest req) {
 		return req.getScheme() + "://" +
 				req.getServerName() + ":" + 
 				req.getServerPort();
 	}
 
 	protected static String getEndpointURL(String host, String csrfToken) {
 		UriBuilder uri = UriBuilder.fromUri("https://accounts.google.com/o/oauth2/auth");
 		uri.queryParam("response_type", "code");
		uri.queryParam("client_id", Config.CLIENT_ID);
 		uri.queryParam("redirect_uri", getRedirectURL(host));
 		uri.queryParam("scope", SCOPES);
 		uri.queryParam("hd", "fcpsschools.net");
 		uri.queryParam("state", csrfToken);
 
 		return uri.build().toString();
 	}
 
 	protected static String getAccessToken(String authCode, String host) throws Exception {
 		// Open the connection to the access token thing
 		UriBuilder uri = UriBuilder.fromPath("");
 		uri.queryParam("code", authCode);
		uri.queryParam("client_id", Config.CLIENT_ID);
		uri.queryParam("client_secret", Config.CLIENT_SECRET);
 		uri.queryParam("redirect_uri", getRedirectURL(host));
 		uri.queryParam("grant_type", "authorization_code");
 		
 		// substring(1) to get rid of the ? that's found in urls
 		String params = uri.build().toString().substring(1);
 		
 		String output = null;
 		try {
 			output = Utils.fetchURL(
 					"POST",
 					"https://accounts.google.com/o/oauth2/token",
 					params,
 					"application/x-www-form-urlencoded;charset=UTF-8");
 		} catch (IOException e) {
 			throw e;
 		}
 		
 		log.log(Level.FINER, "Google OAuth2 Token endpoint returned:\n" + output);
 
 		// Parse the JSON.
 		String access_token = null;
 		try {
 			ObjectMapper m = new ObjectMapper();
 			JsonNode rootNode = m.readTree(output);
 			access_token = rootNode.path("access_token").textValue();
 		} catch (Exception e) {
 			log.log(Level.INFO, "Error parsing JSON:\n" + output);
 			throw e;
 		}
 
 		if (access_token == null) {
 			log.log(Level.WARNING, "No access token found in JSON:\n" + output);
 			throw new Exception("No access token found in JSON!");
 		}
 
 		return access_token;
 	}
 
 	protected static User getUserData(String access_token) throws Exception {
 		String output = null;
 
 		try {
 			output = Utils.fetchURL("GET", "https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + access_token);
 		} catch (IOException e) {
 			throw e;
 		}
 		
 		log.log(Level.FINER, "Google UserInfo endpoint returned:\n" + output);
 
 		// Parse the JSON.
 		User user = new User();
 		try {
 			ObjectMapper m = new ObjectMapper();
 			JsonNode rootNode = m.readTree(output);
 			
 			String id = rootNode.path("sub").textValue();
 			String name = rootNode.path("name").textValue();
 			String email = rootNode.path("email").textValue();
 			boolean verifiedEmail = rootNode.get("email_verified").booleanValue();
 			String gender = rootNode.path("gender").textValue();
 
 			if (name == null || email == null) {
 				throw new Exception("missing fields");
 			}
 			// must be fcpsschools.net
 			if (email.length() < 16 || !email.substring(email.length() - 16).equalsIgnoreCase("@fcpsschools.net") || 
 					verifiedEmail != true) {
 				throw new Exception("Invalid email address: " + email);
 			}
 			
 			user.id = id;
 			user.name = name;
 			user.real_name = name;
 			user.fcps_id = email.substring(0, email.length() - 16);
 
 			if (gender != null) {
 				user.gender = Gender.valueOf(gender.toUpperCase());
 			}
 		} catch (Exception e) {
 			log.log(Level.INFO, "JSON that failed: " + output);
 			throw e;
 		}
 
 		return user;
 	}
 }
