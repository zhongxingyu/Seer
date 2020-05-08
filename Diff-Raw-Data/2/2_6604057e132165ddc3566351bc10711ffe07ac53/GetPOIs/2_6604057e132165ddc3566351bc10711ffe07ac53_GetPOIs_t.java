 package ch.hsr.OSMPOIs4Layar;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.Servlet;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import java.sql.*;
 import java.util.Properties;
 
 
 /**
  * Servlet implementation class GetPOIs
  */
 public class GetPOIs extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
     /**
      * Default constructor. 
      */
     public GetPOIs() {
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see Servlet#init(ServletConfig)
 	 */
 	public void init(ServletConfig config) throws ServletException {
 		try {
 			Class.forName("org.postgresql.Driver");
 		} catch(ClassNotFoundException e) {
 			System.err.println("Unable to load PostgreSQL database driver.");
 			System.exit(1);
 		}
 
		String url = "jdbc:postgresql://" + Database.host + "/" + Database.dbname;
 		Properties props = new Properties();
 		props.setProperty("user", Database.user);
 		props.setProperty("password", Database.password);
 //		props.setProperty("ssl","true");
 		Connection conn = DriverManager.getConnection(url, props);
 	}
 
 	/**
 	 * @see Servlet#destroy()
 	 */
 	public void destroy() {
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		System.out.println("processing request ...");
 		PrintWriter writer = response.getWriter();
 		writer.println("<html>");
 		writer.println("<head><title>Hello World Servlet</title></head>");
 		writer.println("<body>");
 		writer.println("	<h1>Hello World from a Sevlet!</h1>");
 		writer.println("<p>" + Database.host + "</p>");
 		writer.println("<body>");
 		writer.println("</html>");
 		writer.close();
 	}
 }
