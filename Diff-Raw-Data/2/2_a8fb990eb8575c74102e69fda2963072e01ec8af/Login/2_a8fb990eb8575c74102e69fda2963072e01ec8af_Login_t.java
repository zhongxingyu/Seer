 package cyberprime.servlets;
 
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.util.Iterator;
 import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import cyberprime.entities.Clients;
 import cyberprime.entities.Sessions;
 import cyberprime.entities.dao.ClientsDAO;
 import cyberprime.util.FileMethods;
 
 /**
  * Servlet implementation class Login
  */
 @WebServlet("/Login")
 public class Login extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public Login() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 	
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 	HttpSession session = request.getSession();
 		
 
 		Clients client = (Clients) session.getAttribute("client");
 		
 		if(client==null){
 			Object obj = new Object();
 			obj = "<p style='color:red'>*Your session has timed out</p>";
 			request.setAttribute("loginResult", obj);
 			request.getRequestDispatcher("templateLogin.jsp").forward(request, response);
 			return;
 		}
 		String image = (String)session.getAttribute("image");
 
 		String pattern = (String)request.getParameter("pattern");
 		
 		
 
 		Clients c = ClientsDAO.retrieveClient(client);
 		if(c.getActivation()==null){
 			Object obj = new Object();
 			obj = "<p style='color:red'>*There is no user registered with the image uploaded</p>";
 			request.setAttribute("loginResult", obj);
 			request.getRequestDispatcher("templateLogin.jsp").forward(request, response);
 			return;
 		}
 		
 		client.setUserId(c.getUserId());
 		
 		if(pattern.length() != 0){
 			try {
 				client.setPattern(pattern);
 			} catch (NoSuchAlgorithmException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		else{
 			Object obj = new Object();
 			obj = "<p style='color:red'>*You did not choose a pattern</p>";
 			request.setAttribute("loginResult", obj);
 			request.getRequestDispatcher("patternLogin.jsp").forward(request, response);
 			return;
 		}
 
 
 		if(c.getActivation().equalsIgnoreCase("Pending")){
 			Object obj = new Object();
 			obj = "<p style='color:red'>*Your account has not been activated</p>";
 			request.setAttribute("loginResult", obj);
 			request.getRequestDispatcher("patternLogin.jsp").forward(request, response);
 			return;
 		}
 		
 		else if(c.getActivation().equalsIgnoreCase("Active") || c.getActivation().equalsIgnoreCase("Reset")){
 			if(client.getImageHash().equals(c.getImageHash()) && client.getPattern().equals(c.getPattern())){
 				
 				HttpSession existingHttpSession = request.getSession();
 				Clients existingClient = (Clients)existingHttpSession.getAttribute("client");
 				if (existingClient!=null){
 					Sessions existingSessions = new Sessions(existingHttpSession.getId(), existingClient.getUserId());
 					Set sessionArray = (Set) getServletContext().getAttribute("cyberprime.sessions");
 					Iterator sessionIt = sessionArray.iterator();
 							while(sessionIt.hasNext()) {
 							Sessions sess = (Sessions)sessionIt.next();
 							System.out.println("Client id ="+sess.getClientId());
 							if(sess.getClientId().equals(existingClient.getUserId()) && sess.getSessionId().equals(existingHttpSession.getId())){
								break;
 							}
 							
 							else{
 								Object obj = new Object();
 								obj = "<p style='color:red'>*Your account is already logged in</p>";
 								request.setAttribute("loginResult", obj);
 								FileMethods.fileDelete(image);
 								request.getRequestDispatcher("templateLogin.jsp").forward(request, response);
 								return;
 								
 							}
 							
 							}
 				}
 				
 				Sessions s = new Sessions(session.getId(),c.getUserId());
 //				s = SessionsDAO.createSession(s);
 //				Set sess = Collections.synchronizedSet(new HashSet());
 //				getServletContext().setAttribute("cyberprime.sessions", sess);
 				//WeakReference sessionRef = new WeakReference(s);
 				Set sessions = (Set)getServletContext().getAttribute("cyberprime.sessions");
 				sessions.add(s);
 				session.setAttribute("c", c);
 				session.setMaxInactiveInterval(60);
 				
 				if(c.getActivation().equalsIgnoreCase("Active")){
 					session.removeAttribute("image");
 					FileMethods.fileDelete(image);
 					request.getRequestDispatcher("secured/templateNewHome.jsp").forward(request, response);
 				}
 				
 				else if(c.getActivation().equalsIgnoreCase("Reset")){
 					request.getRequestDispatcher("patternReset.jsp").forward(request, response);
 				}
 
 			}	
 			
 			else{
 				Object obj = new Object();
 				obj = "<p style='color:red'>*Login failed, wrong pattern / image</p>";
 				request.setAttribute("loginResult", obj);
 				request.getRequestDispatcher("patternLogin.jsp").forward(request, response);
 				return;
 			}
 		}
 
 
 		
 	}
 
 }
