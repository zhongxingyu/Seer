 package servlet;
 
 import java.io.*;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import model.*;
 import manager.*;
 
 @SuppressWarnings("serial")
 public class AuthenticateServlet extends HttpServlet {
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 		throws ServletException, IOException {
 		
 		processAuthenticateRequest(request, response);
 	}
 	
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 			
 			processAuthenticateRequest(request, response);
 	}
 	
 	public void processAuthenticateRequest(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
 		response.setContentType("text/plain");
 		PrintWriter writer = response.getWriter();
 		
 		String username = request.getParameter("userName");
 		String password = request.getParameter("password");
 		
 		UserDataManager udm = new UserDataManager();
 		
 		if(!username.trim().isEmpty() && !password.trim().isEmpty()) {
 			AuthenticateService authService = new AuthenticateService();
 			Sponsor authSponsor;
 			try {
 				authSponsor = authService.authenticateSponsor(username, password);
 				if(authSponsor!= null){
 					
 					String fullName = authSponsor.getFullName();
 					String sponsorUsername	= authSponsor.getUsername();
 					//String userType = authSponsor.getType();
 					HttpSession session = request.getSession();
 					if(!udm.isSuspended(sponsorUsername)){
 						
 						session.setAttribute("fullname", fullName);
 						session.setAttribute("username", sponsorUsername);
						session.setAttribute("type", "Sponsor");
 						
 						response.sendRedirect("mainPage.jsp");
 					}else{
 						session.setAttribute("message","You have  been suspended. Please contact the administrator for more details");
 						response.sendRedirect("index.jsp");
 					}
 				} else {
 					writer.print("false1");
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			
 		} else {
 			writer.print("false");
 		}
 	}
 }
