 package jdressel.DerporiaPrime;
 
 import java.io.IOException;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import org.xml.sax.SAXException;
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 
 /**
  * Servlet implementation class ProcessLogin
  */
 public class ProcessLogin extends HttpServlet {
 	private String username;
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public ProcessLogin() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		/*
 		* Send the user back to where they belong
 		*/
 	 
 		String destination="Derporia.jsp";//TODO
 		response.sendRedirect(response.encodeRedirectURL(destination));//TODO
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		//process parameters
 		//if user logged in deal with it. 
 		//if username or password empty deal with it. 
 		
 		try {
 			Utility.load(this.getServletContext());
 		} catch (SAXException | ParserConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		username = request.getParameter("username")==null ? "" : request.getParameter("username");
 		String password = request.getParameter("password")==null ? "" : request.getParameter("password");
 		HttpSession session = request.getSession();//If a session does not exist, this will start one
 		
 		
 		if(session.getAttribute("username")!=null){
 		//user is already logged in
 			response.sendRedirect(response.encodeRedirectURL("LoggedInAlready.jsp"));//TODO
 		} else if(!userExists(new User(username))){
 			response.sendRedirect(response.encodeRedirectURL("Register.jsp"));//TODO user does not exist
 		}  else{
 			User user = new User(username);
 			if(passwordCorrect(user, password)){
 				user = new User(username, password);
 				//good to go
 			} else {
 				//TODO throw exception
 				response.sendRedirect(response.encodeRedirectURL("WrongPassword.jsp"));
 			}
 			
 			session.setAttribute("username",user);
 			this.addUserToMap(user);
 			
 			try {
 				Utility.saveUsers((Map<String, User>) this.getServletContext().getAttribute("jdresselUserMap"));
 			} catch (ParserConfigurationException | TransformerException
 					| SAXException | IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			if(session.getAttribute("loginRequester")!=null){
			String toPage = (String)session.getAttribute("loginRequester");
 			session.removeAttribute("loginRequester");
			response.sendRedirect(response.encodeRedirectURL(toPage));//TODO had problem here
 
 			} else {
 				response.sendRedirect(response.encodeRedirectURL(request.getHeader("referer")));
 			}
 		
 		}
 
 
 	}
 	
 	private boolean userExists(User user){
 		Map<String, User> userMap = getUserMap();
 		if(userMap.containsKey(user.getUN())){
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private boolean passwordCorrect(User user, String password){
 		Map<String, User> userMap = getUserMap();
 		User onServer = userMap.get(user.getUN());
 		
 		if (onServer.passwordCorrect(password)){
 			return true;
 		}
 		return false;	
 	}
 	@SuppressWarnings("unchecked")
 	public Map<String, User> getUserMap(){
 		ServletContext context = getServletContext();
 		Map<String, User> userMap = new HashMap<String, User>();
 		Object attribute = context.getAttribute("jdresselUserMap");
 		if(attribute!=null){
 			//TODO check to see if this is correct class
 			userMap = (Map<String, User>)attribute;
 		}
 		return userMap;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void addUserToMap(User user){
 		ServletContext context = getServletContext();
 		Map<String, User> userMap = new HashMap<String, User>();
 		Object attribute = context.getAttribute("jdresselUserMap");
 		if(attribute!=null){
 			//TODO check to see if this is correct class
 			userMap = (Map<String, User>)attribute;
 		}
 		userMap.put(user.getUN(), user);
 	}
 	
 	public void destroy()
 	{
 		try {
 			Utility.saveAssertions((Set<Assertion>) this.getServletContext().getAttribute("jdresselAssertionSet"));
 		} catch (ParserConfigurationException | TransformerException
 				| SAXException | IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		try {
 			Utility.saveUsers((Map<String, User>) this.getServletContext().getAttribute("jdresselUserMap"));
 		} catch (ParserConfigurationException | TransformerException
 				| SAXException | IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		super.destroy();
 	}
 }
