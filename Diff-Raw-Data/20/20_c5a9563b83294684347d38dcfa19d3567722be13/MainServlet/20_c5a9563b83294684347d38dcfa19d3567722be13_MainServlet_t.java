 package session;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Date;
 import java.util.UUID;
 import java.sql.Timestamp;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation class MainServlet
  */
 @WebServlet("/MainServlet")
 public class MainServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static String cookieName = "CS5300PROJ1SESSION";
 	
 	ConcurrentHashMap<String, SessionState> sessionData = new ConcurrentHashMap<String, SessionState>();
     
     // Determine if a session exists already based on if the session ID stored is valid
 	private boolean sessionExists(String sessionID) {
 		return ((sessionID != null) || (sessionData.get(sessionID) != null) 
 				|| (sessionData.get(sessionID).getExpirationTime().before(new Timestamp((new Date()).getTime()))));
 	}
 	
 	// Create a new session in the hashmap and return a cookie with the sessionID and version number
 	private Cookie createSession(String message) {
 		String sessionID = UUID.randomUUID().toString();
 		int version = 0;
 		SessionState st = new SessionState(sessionID, version, message);
 		sessionData.put(sessionID, st);
 		return (new Cookie(cookieName, sessionID + "^" + version));
 	}
 	
     /**
      * @see HttpServlet#HttpServlet()
      */
     public MainServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		PrintWriter out = response.getWriter();
 		
 		Cookie userCookie = null;
 		String message = "Welcome!";
 		String sessionID = null;
 		SessionState curState = null;
 		
 		// If the cookie already exists then retrieve it and store it into userCookie
 		Cookie[] cookies = request.getCookies();
 		if (cookies != null) {
 			for(Cookie cookie: cookies) {
 				if (cookieName.equals(cookie.getName())) {
 					userCookie = cookie;
 				}
 			}
 		}
 
		// Create a new session if there wasn't a cookie
 		if (userCookie == null) {
 			userCookie = createSession(message);
 			String temp[] = userCookie.getValue().split("\\^");
 			sessionID = temp[0];
 			curState = sessionData.get(sessionID);
 		} else { // Else get the session from the cookie, if it exists
 			String temp[] = userCookie.getValue().split("\\^");
 			sessionID = temp[0];
 			curState = sessionData.get(sessionID);
			// The case when a cookie exists for a session but when the session identified doesn't
			// exist in the hashmap (i.e. restarting the program when and the client still has
			// a cookie from before
			if (curState == null) {
				userCookie = createSession(message);
				String temp2[] = userCookie.getValue().split("\\^");
				sessionID = temp2[0];
				curState = sessionData.get(sessionID);	
			}
		} 
 		
 		System.out.println(curState.toString());
 		String command = request.getParameter("command");
 		if (command != null) {
 			if (command.equals("LogOut")) {
 				if (userCookie != null) {
 					sessionData.remove(sessionID);
 					userCookie.setMaxAge(0);
 				}
 			} else if (command.equals("Replace")) {
 				message = request.getParameter("replaceText");
 			} else {
 				out.println("REFRESH SESSION");
 			}
 		}
 
 		request.setAttribute("message", message);
 		if (curState != null) request.setAttribute("expires", curState.getExpirationTime());
		request.setAttribute("serverAddr", request.getServerName());
		request.setAttribute("serverPort", request.getServerPort());
		
 		if (userCookie != null) response.addCookie(userCookie);
 		
 		if (command != null && command.equals("LogOut")) {
 			request.getRequestDispatcher("/logout.jsp").forward(request,response);
 		} else {
 		request.getRequestDispatcher("/index.jsp").forward(request, response);
 		}
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doGet(request, response);
 	}
 
 }
