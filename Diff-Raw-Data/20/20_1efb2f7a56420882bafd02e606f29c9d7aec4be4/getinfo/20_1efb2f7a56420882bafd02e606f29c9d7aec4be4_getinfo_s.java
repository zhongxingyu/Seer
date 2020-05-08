 /*
 This class provides the class, first name, last name, address, email, phone and list of doctors (if patient) to the modification form. 
 These are used to pre-populate an html form for user editing. 
 */
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Date;
 import java.sql.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 public class getinfo extends HttpServlet {
 	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
 		res.setContentType("text/html");
 		//establish the connection to the underlying database
 		Connection conn = null;
 		String driverName = "oracle.jdbc.driver.OracleDriver";
 		String dbstring = "jdbc:oracle:thin:@gwynne.cs.ualberta.ca:1521:CRS";
 		String requestedUser = req.getParameter("USERNAME");
 		PrintWriter out = res.getWriter();
 		try{
 			//load and register the driver	
 			Class drvClass = Class.forName(driverName);
 			DriverManager.registerDriver((Driver) drvClass.newInstance());
 		}
 		catch(Exception ex){
 			out.println("<hr>" + ex.getMessage() + "<hr>");
 		}
 		try{
 			//establish the connection
 			conn = DriverManager.getConnection(dbstring,"zturchan","Pikachu1");
 		    conn.setAutoCommit(false);
 		}
 		catch(Exception ex){
 			out.println("<hr>" + ex.getMessage() + "<hr>");
 		}
 		Statement stmt = null;
 		ResultSet rset = null;
 		String first = "";
 		String last = "";	
 		String address = "";
 		String email = "";
 		String phone = "";
 		String classid = "";
 		String docs = "";	
 		String sqlinfo = "select first_name,last_name,address,email,phone from persons where USER_NAME = '"+requestedUser+"'";
 		String sqluser = "select class from users where USER_NAME = '"+requestedUser+"'";
 		String sqldocs = "select doctor_name from family_doctor where patient_name = '"+requestedUser+"'";
 		try{
 			stmt = conn.createStatement();
 			rset = stmt.executeQuery(sqlinfo);
 			while(rset.next()){
 				first = rset.getString(1);
 				last = rset.getString(2);
 				address = rset.getString(3);
 				email = rset.getString(4);
 				phone = rset.getString(5);
 			}
 			//Now we want to get the person's class, if they are a patient then get their list of doctors
 			stmt = conn.createStatement();
 			rset = stmt.executeQuery(sqluser);
 			while(rset.next()){
 				classid = rset.getString(1);
 			}
 			if(classid.equals("p")){
 				stmt = conn.createStatement();
 				rset = stmt.executeQuery(sqldocs);
 				while(rset.next()){
 					docs = docs + rset.getString(1)+ ",";
 				}
 			}	
 			stmt.close();
 			conn.close();
 		}
 		catch(Exception ex){
 			javax.swing.JOptionPane.showMessageDialog(null, "There was an error connecting to the database.  Please ensure you have entered a valid username");
 			out.println("<hr>" + ex.getMessage() + "<hr>");
 		}
 		res.sendRedirect(		"newmodify.jsp?user="+requestedUser+
 								"&classid="+classid+
 								"&first="+first+
 								"&last="+last+
 								"&address="+address+
 								"&email="+email+
 								"&phone="+phone+
 								"&docs="+docs);
 	}
 }
