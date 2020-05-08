 package ca.ubc.med.lti;
 
 
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 /**
  * LTIForward. 
  * This program does the heavy lifting of LTI integration. 
  * The values provided in custom parameters from the LTI consumer call are written into a cookie. 
  * 
  * The URL of an application that is forwarded-to is expected to be a LTI Tool Provider
  * that accepts the existence of the cookie as proof of authentication. 
  * The Tool Provider is expected to extract runtime parameters from the cookie.
  * The Tool Consumer must provide runtime parameters for the tool provider as part of the 
  * custom parameters of the LTI call.  
  *  
  * @author Bob Walker robert.walker@ubc.ca
  *
  */
 
 public class LTIForward  extends HttpServlet {
 	
 	private String mysecret = "Manuj"; 
 	private int cookie_lifespan = 120;
 	private String cookie_name;
 	private String cookie_domain = null;
 	private String cookie_path = null;
 	private String forward;
 	private String private_key;
 
 	private Logger log = org.apache.log4j.Logger.getLogger("LTIForward");
 	
 	/**
 	 * Get properties from properties file. 
 	 */
 	public void init() throws ServletException { 
 
 		HashMap<String,String> props = getProperties();
 
 		forward = props.get("app.forwardto"); 
 		if (forward == null) forward = "index.html";
 		
 		cookie_name = props.get("cookie.name");
 		if (cookie_name == null) cookie_name = "LTICookie";
 		
 		String temp = props.get("cookie.lifespan");
 		if (temp != null)
 			cookie_lifespan = Integer.parseInt(temp);
 		
 		cookie_domain = props.get("cookie.domain");
 		cookie_path = props.get("cookie.domain");				
 		
 		mysecret = props.get("lti.sharedsecret");
 		if (mysecret == null) mysecret = "Manuj";
 		
 		private_key = props.get("hash.privatekey");
 			
 		log.info("Servlet Init:\n forward=" + forward + " cookie " + cookie_name + 
				
 				 " lifespan(seconds)=" + cookie_lifespan);
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
 			  		  
 		  blackboard.blti.message.BLTIMessage msg = blackboard.blti.provider.BLTIProvider.getMessage( request );
 		  String key = msg.getKey();
 		  log.debug("Key from provider = " + key);
 			  
 		  if ( blackboard.blti.provider.BLTIProvider.isValid( msg, mysecret ) ) { // authenticated.
 
 			  log.debug("Successfully authenticated using LTI...");
 			  try {				    
 				  // Get custom parameters and put them in the cookie.
 				  // TODO - get studentid from General Params.
 			    Map<String,String> customParams = msg.getCustomParameters();
 			  
 			    String values="";
 			    String delimiter="";
 				    for (Map.Entry<String,String> entry : customParams.entrySet()) {
 				    	values += delimiter + entry.getKey() + "=" + entry.getValue();
 				    	log.debug("Custom Param: " + entry.getKey() + "=" + entry.getValue());
 				    	delimiter="&";
 				    	if (entry.getKey().toUpperCase().equals("forward"))
 				    		forward = entry.getValue();
 				    }
				values = values + "&name=" + request.getParameter("user_id");
				log.debug("writing cookie with value:" + values);
 			    writeCookie(response,cookie_name,values,cookie_domain);
 				    
 			    log.debug("System Params:");
 				Enumeration<String> E = request.getParameterNames();
 				while (E.hasMoreElements()) { 
 				      String param = E.nextElement();
 				      log.debug(param + "=" + request.getParameter(param));
 				      }
 					   			   
 				log.debug("Redirecting to " + forward);
 			    request.setAttribute("forward",forward);
 				    					  
 			  try {
 				   RequestDispatcher rd  = request.getRequestDispatcher("forward.jsp");    
 				    if (rd == null) {
 				      throw new ServletException ("RequestDispatcher is null!");
 				    }
 				    rd.forward (request, response);
 				  }
 				  catch (Exception e) {
 					  log.error(e.getMessage());
 					  e.printStackTrace();
 				  }					 			  
 				    
 			  }
 			  catch (Exception e) { 
 				  log.error(e.getMessage());				  
 				  e.printStackTrace();
 			  }
 		  }
 			  
 		  else {   // Not authenticated.				  
 			  log.error("Authentication failed.");
 		  }	 
 		  
 		  try {
 			  response.sendError( HttpServletResponse.SC_FORBIDDEN, "Bad Basic LTI Launch" );
 		  	}
 	  	  catch (Exception e) { 
 			  log.error(e.getMessage());
 			  e.printStackTrace();
 			}
 				  
 		}
 
 /**
  * 	Write the cookie. 
  *  TODO - must encrypt cookie.... 		  
  *  
  */
 	  private void writeCookie(HttpServletResponse response,String cookieName,String value,String domain) {
 			  
 			  Cookie cookie = new Cookie(cookieName, value);
 		      cookie.setMaxAge(cookie_lifespan);
 		      if (cookie_path != null)
 		    	  cookie.setPath(cookie_path);
 		      if (cookie_domain != null)
 		    	  cookie.setDomain(domain);
 		      response.addCookie(cookie);  
 		  }
 		  
 /**
  * Get properties from properties file. 
  * @return
  */
 	public static HashMap<String,String> getProperties() { 
 		HashMap<String,String> props = new HashMap<String,String>();
 		ResourceBundle bundle = ResourceBundle.getBundle ("LTIForward");
 		Enumeration<String> e = bundle.getKeys();
 		while (e.hasMoreElements()) {
 			String key =  e.nextElement(); 
 			String value = bundle.getString(key);
 			props.put(key,value);
 		}
 		return props;
 	}
 }
 
