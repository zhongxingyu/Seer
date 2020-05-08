 package cyberprime.servlets;
 
 import java.io.IOException;
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
 
 /**
  * Servlet implementation class Logout
  */
 @WebServlet("/Logout")
 public class Logout extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public Logout() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doPost(request,response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		HttpSession session = request.getSession();
 		Clients client = (Clients)session.getAttribute("c");
 		
//		new logout
 		if(client!=null){
 		
 			Sessions s = new Sessions(session.getId(), client.getUserId());
 			//s = SessionsDAO.deleteSession(s);
 			Set sessions = (Set) getServletContext().getAttribute("cyberprime.sessions");
 			Iterator sessionIt = sessions.iterator();
 					while(sessionIt.hasNext()) {
 					Sessions sess = (Sessions)sessionIt.next();
 					if(sess.getSessionId().equals(session.getId())){
 						System.out.println("Client id ="+sess.getClientId());
 						sessionIt.remove();
 						sessions.remove(sess);
 					}
 					
 
 					}
 
 		
 		session.removeAttribute("c");
 		session.invalidate();
 		response.sendRedirect("template.jsp");
 		}
 		
 		else{
 			response.sendRedirect("template.jsp");
 		}
 
 	}
 
 }
