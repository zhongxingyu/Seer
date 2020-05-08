 package ca.ubc.med.sample;
 
 
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.ResourceBundle;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.Cookie;
 
 import org.apache.log4j.Logger;
 
 /**
  * Simple login procedure. 
  * Creates a cookie if the user and password are the same.
  * User and other Tool Provider parameters are written to cookie.
  * 
  * <br/><br/>This program along with the showimage.jsp make up an example of how an 
  * application can be written as a Tool Provider.
  * See documentation for more details. 
  *     
  * @author Bob Walker robert.walker@ubc.ca
  *
  */
 public class Login  extends HttpServlet {	
 	
 	String user = null; // calling user.
 	String pass = null; // calling user.
 	private int cookie_lifespan = 120;
 	private String cookie_name;
 	private String cookie_domain;
 	private String cookie_path;
 	private String forward;
 	protected org.apache.log4j.Logger  log;
 
 	public void init() throws ServletException { 
 
 		log = org.apache.log4j.Logger.getLogger("InvokeLTI");
 		HashMap<String,String> props = getProperties();
 		
 		forward = props.get("app.forwardto"); 
 		if (forward == null) forward = "index.html";
 		
 		cookie_name = props.get("cookie.name");
 		if (cookie_name == null) cookie_name = "LTICookie";
 		
 		String temp = props.get("cookie.lifespan");
 		if (temp != null)
 			cookie_lifespan = Integer.parseInt(temp);
 		
 		cookie_domain = props.get("cookie.domain");
 		cookie_path = props.get("cookie.path");		
 					
 		log.info("Servlet Init: Cookie " + cookie_name + 
 				 " lifespan(seconds)=" + cookie_lifespan + " path=" + cookie_path + " domain=" + cookie_domain);
 		}
 	public void doGet (HttpServletRequest request, HttpServletResponse response) 
 	    throws ServletException, IOException  {
 	    processRequest(request, response);
 	  }
 
 	 public void doPost (HttpServletRequest request, HttpServletResponse response) 
 	    throws ServletException, IOException  {
 	  	processRequest(request,response);
 	  }
 		  
 	 private void processRequest(HttpServletRequest request, HttpServletResponse response) {
 			  log = org.apache.log4j.Logger.getLogger("Login");	
 			  user = request.getParameter("user");
 			  pass = request.getParameter("pass");
 			  String id = request.getParameter("id");
 		  
               if (user.equals(pass)) {
             	  // TODO - encrypt user name in Cookie.
             	  log.debug("User passed authentication: " + user + "/" + pass);
      		      String cookieValue="studentid=" + user + "&id=" + id;
 				  log.debug("writing cookie...");
 			      writeCookie(response,cookie_name,cookieValue,cookie_domain,cookie_path);            	  
             	  log.info("Writing cookie for " + user  + ". Lifespan: " + cookie_lifespan);
               }
 			  
 			  try {
 			   RequestDispatcher rd  = request.getRequestDispatcher("main.jsp");			   
     
 			    if (rd == null) {
 			      throw new ServletException ("RequestDispatcher is null!");
 			    }
 			    rd.forward (request, response);
 			  }
 			  catch (Exception e) { 
 				  e.printStackTrace();
 			  }
 		
 			  }
 			  		  
 		  
 		  private void writeCookie(HttpServletResponse response,String cookieName,String cookieValue, String cookieDomain, String cookiePath) {
 			  
 			  Cookie cookie = new Cookie(cookieName,cookieValue);
 		        cookie.setMaxAge(cookie_lifespan); 
 			      if (cookiePath != null)
 			    	  cookie.setPath(cookiePath);
 			      if (cookieDomain != null)
 			    	  cookie.setDomain(cookieDomain);
 		        response.addCookie(cookie); 
 
 		  }
 		  
 		  
 /**
  * 
  * @return
  */
 	public static HashMap<String,String> getProperties() { 
 		HashMap<String,String> props = new HashMap<String,String>();
 		ResourceBundle bundle = ResourceBundle.getBundle ("InvokeLTI");
 		Enumeration<String> e = bundle.getKeys();
 		while (e.hasMoreElements()) {
 			String key =  e.nextElement(); 
 			String value = bundle.getString(key);
 			props.put(key,value);
 		}
 		return props;
 	}		  
 	}
 
