 package org.me.webapps.bookstore;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 public class SignupServlet extends HttpServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private String strUsername;
 	private String strPassword;
 	private String strEmail;
 	private String strFirstname;
 	private String strLastname;
 	private String strStreet;
 	private String strCity;
 	private String strState;
 	private String strPhone;
 	private String strCreditcard;
 	private int intExpiremm;
 	private int intExpireyy;
 	private Connection connection;
 	private PreparedStatement customrecord;
 	private boolean boolprocess = true;
 	private ResultSet rsChecking;
 		
 	public void doPost(HttpServletRequest request, HttpServletResponse response) 
 			throws ServletException, IOException {

 		try
 		{	 
 			response.setContentType("text/html");
 			PrintWriter out = response.getWriter();
 			
 			Class.forName("org.hsqldb.jdbcDriver");
 			connection = DriverManager.getConnection( "jdbc:hsqldb:hsql://localhost/bookdb", "sa", "" );
 			
 			if (request.getParameter("txtusername").isEmpty()) {
 				out.println("username cannot be empty<br>");
 			} else {
 				strUsername = (String) request.getParameter("txtusername").toLowerCase();
 				customrecord = connection
 						.prepareStatement("SELECT * FROM customers WHERE username = ?");
 				customrecord.setString(1, strUsername);				
 				rsChecking = customrecord.executeQuery();				
 				if (rsChecking.next()) {
 					boolprocess = false;
 					out.println("username already in use<br>");
 				}
 				rsChecking.close();
 				customrecord.close();			
 			}
 			
 			if (request.getParameter("txtpassword").isEmpty()) {
 				out.println("password cannot be empty<br>");
 			} else {
 				strPassword = (String) request.getParameter("txtpassword");				
 			}
 			
 			if (request.getParameter("txtemail").isEmpty()) {
 				out.println("e-mail address cannot be empty");
 			} else {
 				strEmail = (String) request.getParameter("txtemail");
 				String EMAIL_PATTERN = 
 						"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
 						+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
 				
 				Pattern pattern = Pattern.compile(EMAIL_PATTERN);
 				Matcher matcher = pattern.matcher(strEmail);
 				if (!matcher.matches()) {
 					out.println("e-mail address is invalid");
 					boolprocess = false;
 				}
 			}
 			
 			if (request.getParameter("txtfirstname").isEmpty()) {
 				out.println("First name address cannot be empty");
 			} else {
 				strFirstname = (String) request.getParameter("txtfirstname");
 			}
 			
 			if (request.getParameter("txtlastname").isEmpty()) {
 				out.println("Last name address cannot be empty");
 			} else {
 				strLastname = (String) request.getParameter("txtlastname");
 			}
 			
 			strStreet = (String) request.getParameter("txtstreet");
 			strCity = (String) request.getParameter("txtcity");
 			strState = (String) request.getParameter("txtstate");
 			
 			if (!request.getParameter("txtphone").isEmpty()) {
 				strPhone = request.getParameter("txtphone");
 				if (!strPhone.matches("[0-9]+")) {
 					out.println("invalid phone number<br>");
 					boolprocess = false;
 				}
 			}					
 						
 			if (!request.getParameter("txtcreditcard").isEmpty()) {
 				strCreditcard = request.getParameter("txtcreditcard");
 				if (!strCreditcard.matches("[0-9]+")) {
 					out.println("invalid credit card number<br>");
 					boolprocess = false;
 				}
 			}
 
 			if (!request.getParameter("txtexpiresmm").equals("month")) {
 				intExpiremm = Integer.parseInt(request.getParameter("txtexpiresmm"));
 			}
 
 			if (!request.getParameter("txtexpiresyy").equals("year")) {
 				intExpireyy = Integer.parseInt(request.getParameter("txtexpiresyy"));
 			}
 			
 			
 			
 			if (boolprocess) {
 				connection.setAutoCommit(true);
 				customrecord = connection
 						.prepareStatement("INSERT INTO customers (username, firstname, lastname, password, email, street, city," +
 								" state, phone, creditcard, expiremm, expireyy) " +
 								"VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
 				customrecord.setString(1, strUsername);
 				customrecord.setString(2, strFirstname);
 				customrecord.setString(3, strLastname);
 				customrecord.setString(4, strPassword);
 				customrecord.setString(5, strEmail);
 				customrecord.setString(6, strStreet);
 				customrecord.setString(7, strCity);
 				customrecord.setString(8, strState);
 				customrecord.setString(9, strPhone);
 				customrecord.setString(10, strCreditcard);
 				customrecord.setInt(11, intExpiremm);
 				customrecord.setInt(12, intExpireyy);
 				customrecord.executeUpdate();
 				
 				customrecord.close();
 				
 				HttpSession session = request.getSession(true);
 				session.setAttribute("currentSessionUser", strUsername);
 				session.setAttribute("currentSessionUserEmail", strEmail);
 				
 				Object objReturnurl = session.getAttribute( "returnurl" );
 				if( objReturnurl != null && objReturnurl.toString().equals("process.jsp")) 
 				{
 					response.sendRedirect(objReturnurl.toString());
 				} else {
 					response.sendRedirect("books.jsp"); //logged-in page
 				}
 			} else {
				out.println("<a href='signup.jsp'>Back to previous page</a>");
 			}
 		} catch (Throwable theException) 	    
 		{
 			System.out.println("Exception:" + theException); 
 			try {
 				connection.close();
 				System.out.println("Connection closed");
 			}
 
 			// process SQLException on close operation
 			catch (SQLException sqlException) {
 				sqlException.printStackTrace();
 			}
 		} 
 	} 
 
 	// close statements and terminate database connection
 		protected void finalize() {
 			// attempt to close database connection
 			try {
 				connection.close();
 				System.out.println("Connection closed");
 			}
 
 			// process SQLException on close operation
 			catch (SQLException sqlException) {
 				sqlException.printStackTrace();
 			}
 		}
 }
