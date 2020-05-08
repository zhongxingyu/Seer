 package com.ptzlabs.wc.servlet;
 
 import static com.googlecode.objectify.ObjectifyService.ofy;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.gson.Gson;
 import com.ptzlabs.wc.User;
 import com.ptzlabs.wc.UserData;
 
 @SuppressWarnings("serial")
 public class LoginServlet extends HttpServlet {
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		String accessToken = req.getParameter("access_token");
 		String userId_str = req.getParameter("userid");
 		long userId = Long.parseLong(userId_str);
 		
 		if (accessToken == null) {
 			return;
 		}
 		
 		// check datastore if such fbid exists already.
 		// if not, ask facebook graph for data and write a new entry
 		if(User.getUsers(userId).size() == 0) {
             URL url = new URL("https://graph.facebook.com/" + userId + 
             		"?fields=name,email&access_token=" + accessToken);
             BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
 
             // there is only one json line to read
             String line = reader.readLine();
             Gson gson = new Gson();
             UserData data = gson.fromJson(line, UserData.class);
             
             if(data.error == null) {
             	User user = new User(userId, data.name, data.email);
             	ofy().save().entity(user).now();
             	resp.setContentType("text/plain");
             	resp.getWriter().println("OK");
             } else {
             	resp.setContentType("text/plain");
             	resp.getWriter().println("FB Error " + data.error.code + ": " + data.error.message);
             }
             
             reader.close();
 		}
 		
 		
 		/*
 		resp.sendRedirect("https://www.facebook.com/dialog/oauth?client_id=457817960938308" +
 			"&redirect_uri=" + URLEncoder.encode("http://wc.ptzlabs.com/login", "ISO-8859-1") +
 		    "&state=SOME_ARBITRARY_BUT_UNIQUE_STRING");
 		*/
 	}
 }
