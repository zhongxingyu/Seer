 // Paul McCormack and Rai Feren
 // 010186829 - PO and 40152662 - HMC
 // CS 133 Final Project, Due 5/1/2012
 
 // The doctor portal allows doctors, after logging in with an id
 // to see their scheduled appointments and to update various
 // personal information like locations they work in and
 // treatments they know.
 
 // Invoke it like this: http://localhost:8080/emr/doctors
 
 import java.io.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.sql.*;
 import java.util.ArrayList;
 
 // displays page and performs various requests about electronics products
 public class DoctorServlet extends HttpServlet {
 
 	// main page and template page for doctor portal
 	public static final String DOCTOR_TEMPLATE = "../webapps/emr/res/doctor_template.html";
 	public static final String DOCTOR_MAIN = "../webapps/emr/res/doctors.html";
 
 	// respond to a GET request by just writing the page to the output
     public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         // Set up the response
         response.setContentType("text/html");
 
         // Begin composing the response
         PrintWriter out = response.getWriter();
 
 		// send the DOCTOR_MAIN page back
         out.println(readFileAsString(DOCTOR_MAIN));
     }
 
 	// respond to a post by interpreting form information and printing requested output
     public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 	    // Set up the response
         response.setContentType("text/html");
 
         // Begin composing the response
         PrintWriter out = response.getWriter();
 
 		// see which form was submitted based on state of submit button
 		if(request.getParameter("login") != null)				// log in to an account
 			doLogin(request, out);
 		else if(request.getParameter("update") != null)			// update treatments known
 			updateTreatmentsKnown(request, out);
     }
 
 
 	// log in to a doctors's page
     public void doLogin(HttpServletRequest request, PrintWriter out){
 		// get login ID
 		int doctorID = Integer.parseInt(request.getParameter("did"));
 
 		try{
 			// get records from database or else reject input
 			ArrayList<ArrayList<Object>> result = DB.executeQuery("SELECT name FROM Doctors WHERE did=" + doctorID + ";", 1);
 			if (!result.isEmpty()){
 				out.println(generate_doctor_page(doctorID));
 			}
 
 		} catch (java.lang.Exception ex2){
 			//out.println("<h2> Exception: </h2> <p>"+ ex2.getMessage() +"</p> <br>");
 			// send the DOCTOR_MAIN page back
 			String errorPage = readFileAsString(DOCTOR_MAIN);
 			errorPage = errorPage.replace("<div id=\"bad_id\" style=\"display: none;\">", "<div id=\"bad_id\" style=\"display: block;\">");
 			errorPage = errorPage.replace("%BAD_ID%", doctorID + "");
 			out.println(errorPage);
 			return;
 		}
 
 	}
 
 	// update list of treatments known and available locations
 	public void updateTreatmentsKnown(HttpServletRequest request, PrintWriter out){
 		// get doctor ID from hidden field
 		int doctorID = Integer.parseInt(request.getParameter("did"));
 		try{
 			// begin a transaction
 			DB.beginTransaction();
 
 			// get list of all fid to determine which are checked
 			ArrayList<ArrayList<Object>> fids = DB.executeQuery("SELECT F.fid FROM Facilities F;", 1);
 
 			// first remove all locations
 			DB.executeUpdate("DELETE FROM WorksIn WHERE did = " + doctorID + ";");
 
 			// consider all check boxes "loc_x", where x is fid
 			for (int i = 0; i < fids.size(); i++){
 				Integer fid = (Integer) fids.get(i).get(0);
 				if (request.getParameter("loc_" + fid) != null)
 					DB.executeUpdate("INSERT INTO WorksIn VALUES("+doctorID+", " + fid + ");");
 			}
 
 			// now consider all treatments to update knows table
 			ArrayList<ArrayList<Object>> treatments = DB.executeQuery("SELECT T.tid FROM Treatments T;", 1);
 
 			// first remove all treatments known
 			DB.executeUpdate("DELETE FROM Knows WHERE did = " + doctorID + ";");
 
 			// consider all check boxes "treat_x", where x is tid
 			for (int i = 0; i < treatments.size(); i++){
 				Integer tid = (Integer) treatments.get(i).get(0);
 				if (request.getParameter("treat_" + tid) != null)
 					DB.executeUpdate("INSERT INTO Knows VALUES("+doctorID+", " + tid + ");");
 			}
 
 			// end transaction
 			DB.endTransaction();
 
 			// make hidden divider appear in output to confirm changes made
 			out.println(generate_doctor_page(doctorID).replace("<div id=\"changes_made\" style=\"display: none;\">", "<div id=\"changes_made\" style=\"display: block;\">"));
 			return;
 
 		} catch (java.lang.Exception ex2){
 			//out.println("<h2> Exception: </h2> <p>"+ ex2.getMessage() +"</p> <br>");
 		}
 		// make hidden divider appear in output to confirm changes made
 		out.println(generate_doctor_page(doctorID));
 	}
 
 	// generate a doctor's home page
 	private String generate_doctor_page(int doctorID) {
 		String html = readFileAsString(DOCTOR_TEMPLATE);
 		String appointmentRows = "";
 		String allLocationRows = "";
 		String locationDividers = "";
 		String treatmentsKnown = "<tr>";
 
 		// begin a transaction
 		DB.beginTransaction();
 
 		// get patient name
 		String doctorName = (String) DB.executeQuery("SELECT name FROM Doctors WHERE did = " + doctorID + ";", 1).get(0).get(0);
 
 		// get all appointment information from database (except symptoms, which will get later)
 		ArrayList<ArrayList<Object>> appointmentInfo = DB.executeQuery("SELECT A.date, P.name, F.fid, F.name, C.name, T.name, A.aid FROM Patients P, Appointments A, Facilities F, ConditionsTreats C, Treatments T WHERE A.did =" + doctorID + " and A.fid = F.fid and A.pid = P.pid and C.cid=A.cid and T.tid=C.tid;", 7);
 
 		// concatenated into comma-separated lists
 		String[] symptomStrings = new String[appointmentInfo.size()];
 		for (int row = 0; row < appointmentInfo.size(); row++) {
 			// get symptom info for this appointment
 			ArrayList<ArrayList<Object>> sympList = DB.executeQuery("SELECT S.name FROM Symptoms S, SymptomList L WHERE L.aid = " + appointmentInfo.get(row).get(6) + " and L.sid = S.sid;", 1);
 			symptomStrings[row] = "";
 			for (int i = 0; i < sympList.size(); i++) {
 				if (i != 0)
 					symptomStrings[row] += ", ";
 				symptomStrings[row] += (String) sympList.get(i).get(0);
 			}
 		}
 
 		// print appointment info
 		for (int appt = 0; appt < appointmentInfo.size(); appt++) {
 			appointmentRows += String.format("<tr> <td> %s </td> <td> %s </td> <td> <a href=\"#\" onclick=\"showhide('location_%d');\">%s</a> </td> <td> %s </td> <td> %s </td> <td> %s </td> </tr>",
 			(String) appointmentInfo.get(appt).get(0), (String) appointmentInfo.get(appt).get(1), (Integer) appointmentInfo.get(appt).get(2),
 			(String) appointmentInfo.get(appt).get(3), symptomStrings[appt], (String) appointmentInfo.get(appt).get(4), (String) appointmentInfo.get(appt).get(5));
 		}
 
 		// get all location addresses for dividers
 		ArrayList<ArrayList<Object>> facilityAddrs = DB.executeQuery("SELECT F.fid, F.name, F.addr1, F.addr2 FROM Facilities F", 4);
 		for (int loc = 0; loc < facilityAddrs.size(); loc++)
 			locationDividers += String.format("<div id=\"location_%d\" style=\"display: none;\"> <br>\n <b> %s </b>\n <ul>\n %s <br>\n %s\n </ul>\n </div>\n\n",
 			(Integer) facilityAddrs.get(loc).get(0), (String) facilityAddrs.get(loc).get(1), (String) facilityAddrs.get(loc).get(2), (String) facilityAddrs.get(loc).get(3));
 
 
 		// get information about which facilities this doctor works in
 		ArrayList<ArrayList<Object>> worksAt = DB.executeQuery("SELECT F.fid FROM Facilities F, WorksIn W WHERE W.did = " + doctorID + " and W.fid = F.fid;", 1);
 		ArrayList<Integer> locationAvailable = new ArrayList<Integer>();
 
 		for (int i = 0; i < worksAt.size(); i++)
 			locationAvailable.add((Integer) worksAt.get(i).get(0));
 
 		// print check boxes for working rows
 		for (int i = 0; i < facilityAddrs.size(); i++){
 			Integer fid = (Integer) facilityAddrs.get(i).get(0);
 			allLocationRows += String.format("<tr><td> <input type=\"checkbox\" name=\"loc_%d\" %s> %s </td> </tr>\n", fid, locationAvailable.contains(fid) ? "checked" : "", facilityAddrs.get(i).get(1));
 		}
 
 
 		// list all treatments and those known
 		ArrayList<ArrayList<Object>> treatments = DB.executeQuery("SELECT T.tid, T.name FROM Treatments T", 2);
 		ArrayList<ArrayList<Object>> knows = DB.executeQuery("SELECT T.tid FROM Treatments T, Knows K WHERE K.did = " + doctorID + " and T.tid = K.tid;", 1);
 		ArrayList<Integer> treatmentKnown = new ArrayList<Integer>();
 
 		for (int i = 0; i < knows.size(); i++)
 			treatmentKnown.add((Integer) knows.get(i).get(0));
 
 
 		for (int i = 0; i < treatments.size(); i++) {
 			Integer tid = (Integer) treatments.get(i).get(0);
 			treatmentsKnown += String.format("\t<td> <input type=\"checkbox\" name=\"treat_%d\" %s> %s </td>", tid, treatmentKnown.contains(tid) ? "checked": "", treatments.get(i).get(1));
 			if (i > 0 && i < treatments.size() - 1 && i % 4 == 3)
 				treatmentsKnown += "</tr>\n<tr>";
 		}
 		treatmentsKnown += "</tr>";
 
 		// end transaction
 		DB.endTransaction();
 
 		html = html.replace("%DOCTOR_ID%", doctorID + "");
 		html = html.replace("%DOCTOR_NAME%", doctorName);
 		html = html.replace("%APPOINTMENT_ROWS%", appointmentRows);
 		html = html.replace("%LOCATION_DIVIDERS%", locationDividers);
 		html = html.replace("%AVAILABLE_LOCATIONS%", allLocationRows);
 		html = html.replace("%TREATMENTS_KNOWN%", treatmentsKnown);
 
 		return html;
     }
 
 
 
 	// read a file as a string
 	// from http://snippets.dzone.com/posts/show/1335
 	private String readFileAsString(String filePath) {
 		byte[] buffer = new byte[(int) new File(filePath).length()];
 		try{
 			BufferedInputStream f = null;
 			try {
 				f = new BufferedInputStream(new FileInputStream(filePath));
 				f.read(buffer);
 			} finally {
 				if (f != null)
 					try {
 						f.close();
 					} catch (IOException ignored) {
 					}
 			}
 		} catch (IOException e) {
 			return e.getMessage() + "\n" + System.getProperty("user.dir");
 		}
 		return new String(buffer);
 
     }
 
 }
