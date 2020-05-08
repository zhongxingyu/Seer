 package com.team8.servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import com.team8.dao.UserAuthenticationDao;
 import com.team8.models.Customer;
 
 /**
  * Servlet implementation class UserAuthentication
  */
 @WebServlet("/UserAuthentication")
 public class UserAuthenticationServlet extends HttpServlet {
 	private static final long serialVersionUID = 23L;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public UserAuthenticationServlet() {
 		super();		
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */

 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 		response.setContentType("text/plain");
 		PrintWriter pw = response.getWriter();

 		try {

 			UserAuthenticationDao dao = new UserAuthenticationDao();
 			Customer user = dao.isUserAuthenticated(request.getParameterMap());
 			if(user != null)
 			{
				HttpSession session = request.getSession();
 				session.setAttribute("username", user.getCustomerName());
 				session.setAttribute("userId", user.getCustomerId());
 				pw.write("success");
 			}
 			else {
 				pw.write("fail");
 			}
 			pw.close();
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 }
