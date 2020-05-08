 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.io.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.sql.*;
 
 public class DBAccess extends HttpServlet{
 
 	public static String driverName = "oracle.jdbc.driver.OracleDriver";
 	public static String url = "jdbc:oracle:thin:@gwynne.cs.ualberta.ca:1521:CRS";
 
 
 	public void doGet (HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException{
 	
 		HttpSession session = req.getSession();
 		PrintWriter out = res.getWriter();
 		res.setContentType("text/html");
 		String sessionUser = (String) session.getAttribute("userName");
 		if(sessionUser == null){
 			javax.swing.JOptionPane.showMessageDialog(null, "You cannot perform that action.");
 			res.sendRedirect("../proj1/login.html");				
 		}
 		boolean ranksort = false;
 		if(req.getParameter("sortby").equals("Rank")){
 			ranksort = true;
 		}
 		String startDate = req.getParameter("startdate").trim();	
 		String endDate = req.getParameter("enddate").trim();	
 		if (startDate.equals("")){
 			startDate = "9888-01-01";
 		}
 		if (endDate.equals("")){
 			endDate = "0001-01-01";
 		}
 		ArrayList<Record> records = new ArrayList<Record>();
 		try{
 			records = getRecords(req.getParameter("keywords").trim(), ranksort, "to_date('"+startDate+"', 'yyyy-mm-dd')", "to_date('"+endDate+"', 'yyyy-mm-dd')");
 		}catch(Exception e){out.println("<hr>" + e.getMessage() + "<hr>");}
 		out.println("<HTML><HEAD><TITLE>RIS - Search Results</TITLE>");
 		out.println("<link rel='stylesheet' type='text/css' href='style.css' /><HEAD>");
 		out.println("<BODY><div id='content'><TABLE border=1><TR valign=top align=left>");
 		out.println("<td>Patient Name</td><td>Doctor Name</td><td>Radiologist Name</td><td>Test Type</td><td>Prescribing Date</td><td>Test Date</td><td>Diagnosis</td><td>Description</td><td>Image Thumbnail</td></tr>");
 		for(Record r : records){
 			try{		
 				Connection con = DriverManager.getConnection(url, "zturchan", "Pikachu1");
 				Statement stmt = con.createStatement();
 				ResultSet rset = stmt.executeQuery("select image_id from pacs_images where record_id=" + r.getRecord_id());
 				
 
 				int image_id; 
 				out.println("<TR valign=top align=left>");
 				out.println("<td>" + r.getPatient_name() + "</td>");
 				out.println("<td>" + r.getDoctor_name() + "</td>");
 				out.println("<td>" + r.getRadiologist_name() + "</td>");
 				out.println("<td>" + r.getTest_type() + "</td>");
 				out.println("<td>" + r.getPrescribing_date() + "</td>");
 				out.println("<td>" + r.getTest_date() + "</td>");
 				out.println("<td>" + r.getDiagnosis() + "</td>");
 				out.println("<td>" + r.getDescription() + "</td>");
 				out.println("<td>"); //for images
 				while(rset.next()){
 					image_id = rset.getInt("image_id");
 					out.println("<br><a href = 'oneimage?r" + image_id + "' target='_blank'> <img src = 'oneimage?t" + image_id + "'></a><br><a href = 'oneimage?f" + image_id + "' target='_blank'>View Full Size</a>");
 				}
 				stmt.close();
 				con.close();
 			}catch(Exception e){out.println("<hr>" + e.getMessage() + "<hr>");}
 		}
 		out.println("</TABLE></div><div id='footer'><a href='../proj1/home.html'>Home</a><br><a href='../proj1/logout.jsp'>Logout</a></div></BODY></HTML>");
 	}
 
 
 	/**
 	 * 
 	 * @param searchTerm
 	 * @param rankSort
 	 * @param startDate
 	 * @param endDate
 	 * @return
 	 * @throws SQLException
 	 * @throws ClassNotFoundException
 	 */
 	public static ArrayList<Record> getRecords(String searchTerm, boolean rankSort, String startDate, String endDate) throws SQLException,
 			ClassNotFoundException {
 
 		// connect to our db
 		Class drvClass = Class.forName(driverName);
		Connection con = DriverManager.getConnection(url, "zturchan", "Pikachu1");
 		Statement stmt = con.createStatement();
 
 		//big query to get back
 		ResultSet rset = stmt
 				.executeQuery("SELECT score(1), score(2), score(3), record_id, patient_name, " +
 						"doctor_name, radiologist_name, test_type, prescribing_date, test_date, diagnosis, description" +
 						" FROM radiology_record WHERE (contains(patient_name, '"+searchTerm+"', 1) > 0 or " +
 						"contains(diagnosis, '"+searchTerm+"', 2) > 0 or " +
 						"contains(description, '"+searchTerm+"', 3) > 0) or " +
 						"(test_date > "+ startDate + " and test_date < " + endDate + ") order by test_date desc");
 
 
 		//now convert to records
 		ArrayList<Record> records = new ArrayList<Record>();
 		while(rset.next()){
 			Record r = new Record(rset);
 			records.add(r);
 		}
 		//sort by rank if needed
 		if(rankSort){
 			Collections.sort(records);
 		}
 		stmt.close();
 		con.close();
 		return records;
 	}
 	
 	//this method is super ugly
 	/**
 	 * 
 	 * @param records
 	 * 			records to filter
 	 * @return
 	 * @throws SQLException
 	 */
 	public static ArrayList<Record> filterRecords(ArrayList<Record> records, String userName) throws SQLException{
 		
 		//Code for when we have a session
 		//String userName = (String) session.getAttribute("userName");
 
 		
 		//figure out permissions
 		Connection con = DriverManager.getConnection(url, "zturchan", "Pikachu1");
 		Statement stmt = con.createStatement();
 		ResultSet rset = stmt.executeQuery("Select class from users where user_name = '" + userName + "'");
 		String perms = "";
 		if(rset.next()){//to get to first result (should only be one)
 			perms = rset.getString(1);
 		}
 		else{
 			System.err.println("something has gone horribly wrong, userName not found");
 		}
 		
 		ArrayList<Record> toRemove = new ArrayList<Record>();
 		//this is not how I should do this	
 		//dont look at this code it is uuugly
 		if(perms.equals("p")){
 			for(Record r : records){
 				if(!(r.getPatient_name().equals(userName))){
 					toRemove.add(r);
 				}
 			}
 		}
 		else if(perms.equals("d")){
 			for(Record r : records){
 				if(!(r.getDoctor_name().equals(userName))){
 					toRemove.add(r);				}
 			}
 		}
 		else if(perms.equals("r")){
 			for(Record r : records){
 				if(!(r.getRadiologist_name().equals(userName))){
 					toRemove.add(r);				}
 			}
 		}		
 		
 		records.removeAll(toRemove);
 		return records;
 	}
 
 }
