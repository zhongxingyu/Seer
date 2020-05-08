 package Servlets;
 
 import helpers.HTMLHelper;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.mysql.jdbc.Connection;
 import com.mysql.jdbc.Statement;
 
 import Accounts.Account;
 
 /**
  * Servlet implementation class QuizCatalogServlet
  */
 @WebServlet("/ProfileCatalogServlet")
 public class ProfileCatalogServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public ProfileCatalogServlet() {
 		super();
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		Connection con = (Connection) request.getServletContext().getAttribute("connect");
 		Statement stmt;
 		ResultSet rs;
 		try {
 			stmt = (Statement) con.createStatement();
 			String searchUsers = "Select username from user";
 			if (request.getParameter("search") != null && request.getParameter("search").length() > 0) {
 				searchUsers += " where username like \"%"+request.getParameter("search")+"%\"";
 			} else if (request.getParameter("special") != null) {
 				if (request.getParameter("special").equals("banned")) {
 					searchUsers += " where banned = true";
 				} else if (request.getParameter("special").equals("admin")) {
 					searchUsers += " where admin = true";
 				}
 			}
 			rs = stmt.executeQuery(searchUsers);
 			response.setContentType("text/html");
 	    	PrintWriter out = response.getWriter();
 	    	out.println("<head>");
 	    	out.println(HTMLHelper.printCSSLink());
 	    	out.println("</head");
 	    	out.println("<body>");
	    	out.println(HTMLHelper.printHeader((Account)request.getSession().getAttribute("account")));
     		out.println(HTMLHelper.contentStart());
 	    	out.println("<form action=\"ProfileCatalogServlet\" method=\"get\">");
 	    	out.println("Search Users: <input type=\"text\" name=\"search\"/>");
 	    	out.println("<input type=\"submit\" value=\"Search\"/>");
 			out.println("</form>");
			out.println("<ul class=boxlisting>");
 			out.println("</body>");
 			while (rs.next()) {
 				String name = rs.getString("username");
				out.println(HTMLHelper.printUserListing(name));
 			}
 			out.println("</ul>");
 			out.println(HTMLHelper.contentEnd());
 		} catch (SQLException e) {
 			e.printStackTrace();
 			System.out.println("oops!");
 		}
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 	}
 }
