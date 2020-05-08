 package org.gymadviser.controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.gymAdviser.dto.Admin;
 import org.gymadviser.service.LoginService;
 import org.gymadviser.service.TablesService;
 
 /**
  * Servlet implementation class LoginServlet
 */ 
 @WebServlet("/login")
 public class LoginServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		String database, userId, password;
 		database = request.getParameter("database");
 		userId = request.getParameter("userID");
 		password = request.getParameter("password");
 		LoginService loginService = new LoginService();
 		boolean result = loginService.authentificate(database, userId, password);
 		if (result) {
 			Admin admin = loginService.getAdminDetales(userId);
 			TablesService tablesService = new TablesService();
 			ArrayList<String> tables = tablesService.getTables(database, userId, password);
 			request.getSession().setAttribute("admin", admin);
 			request.getSession().setAttribute("tables", tables);
 			request.getSession().setAttribute("database", database);
 			request.getSession().setAttribute("userId", userId);
 			request.getSession().setAttribute("password", password);
 			response.sendRedirect("tables-list.jsp");
 			return;
 		}
 		response.sendRedirect("index.jsp");
 	}
 
 }
