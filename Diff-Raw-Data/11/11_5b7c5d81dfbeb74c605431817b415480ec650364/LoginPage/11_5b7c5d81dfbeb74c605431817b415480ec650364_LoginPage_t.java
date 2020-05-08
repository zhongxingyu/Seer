 package se.timelog.pages;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import se.kyh.ad10.timeloggers.server.entities.User;
 import se.timelog.rmi.MockupRMI;
 
 /**
  * Servlet implementation class LoginPage
  */
 public class LoginPage extends JspPage {
 	
 	@Override
 	public void doStuff(List<String> remainingPath, HttpServletRequest request, 
 			HttpServletResponse response) throws ServletException, IOException {
 		
 		if ("GET".equals(request.getMethod())) {
 			request.setAttribute("content", "login");
 			request.getRequestDispatcher("/WEB-INF/views/page_tpl.jsp").forward(request, response);
 		} else {
			String email, password;
 			
			email = request.getParameter("email");
 			password = request.getParameter("password");
 			
 			User user = new User();
			user.setEmail(email);
 			user.setPassword(password);
 			
 			MockupRMI mockupRMI = new MockupRMI();
 			boolean status  = mockupRMI.login(user);
 			if (status) {
 				request.setAttribute("content", "success");
 				request.getRequestDispatcher("/WEB-INF/views/page_tpl.jsp").forward(request, response);	
 			} else {
 				request.setAttribute("content", "login");
				request.setAttribute("failed", "Email or password does not match");
 				request.getRequestDispatcher("/WEB-INF/views/page_tpl.jsp").forward(request, response);	
 			}
 		}
 		
 	}
 
 }
