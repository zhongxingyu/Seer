 /* Paul McCormack
  * 010186829 - PO
  * CS 133 PS 6
  * 2/29/2012
  */
 
 // Invoke it like this: http://localhost:8080/emr/patients
 
 import java.io.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.sql.*;
 import java.util.ArrayList;
 
 // displays page and performs various requests about electronics products
 public class PatientServlet extends HttpServlet {
 
 	// main page (not template page) for patient portal
 	public static final String PATIENT_TEMPLATE = "../webapps/emr/res/patient_template.html";
 	public static final String PATIENT_MAIN = "../webapps/emr/res/patients.html";
 
 
 	// respond to a GET request by just writing the page to the output
     public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         // Set up the response
         response.setContentType("text/html");
 
         // Begin composing the response
         PrintWriter out = response.getWriter();
 
 		// send the PATIENT_MAIN page back
         out.println(readFileAsString(PATIENT_MAIN));
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
 		else if(request.getParameter("register") != null)		// create a new account
 			registerPatient(request, out);
 		else if(request.getParameter("create") != null)			// create an appointment
 			createAppointment(request, out);
 		else 													// canceling any appointment or remove prescription
 			cancelAppointmentOrPrescription(request, out);
     }
 
 
 	// log in to a patient's page
     public void doLogin(HttpServletRequest request, PrintWriter out){
 		try{
 			// get login ID
 			int patientID = Integer.parseInt(request.getParameter("pid"));
 
 			// get records from database or else reject input
 			ArrayList<ArrayList<Object>> result = DB.executeQuery("SELECT name FROM Patients WHERE pid=" + patientID + ";", 1);
 			if (result.isEmpty()){
 				// send the PATIENT_MAIN page back
 				String errorPage = readFileAsString(PATIENT_MAIN);
 				errorPage = errorPage.replace("<div id=\"bad_id\" style=\"display: none;\">", "<div id=\"bad_id\" style=\"display: block;\">");
 				errorPage = errorPage.replace("%BAD_ID%", patientID + "");
         		out.println(errorPage);
         		return;
 			}
 			else{
 				out.println(generate_patient_page(patientID));
 			}
 
 		} catch (java.lang.Exception ex2){
 			out.println("<h2> Exception: </h2> <p>"+ ex2.getMessage() +"</p> <br>");
 		}
 	}
 
 	// register a new patient
     public void registerPatient(HttpServletRequest request, PrintWriter out){
 		try{
 			// get name, dob, budget
 			String patientName = request.getParameter("pname2");
 			String dob = request.getParameter("dob");
 			String insurance = request.getParameter("insurance");
 			double weight = Double.parseDouble(request.getParameter("weight"));
 			int patientID = 0;
 
 			// create a new pid for this patient by finding maximum of all current IDs
 			DB.beginTransaction();
 			Object result = DB.executeQuery("SELECT MAX(pid) FROM Patients", 1).get(0).get(0);
 			if (result == null)
 				patientID = 1;
 			else
 				patientID = Integer.parseInt(result.toString()) + 1;
 
 			// insert patient into database
 			DB.executeUpdate("INSERT INTO Patients VALUES(" + patientID + ", \"" + patientName + "\", \"" + dob  + "\", " + weight + ");");
 
 			// insert information into insurance and uses table
 			// will not insert duplicate row by primary key constraint
 			DB.executeUpdate("INSERT INTO Insurance VALUES(\"" + insurance + "\", 0);");
 			DB.executeUpdate("UPDATE Insurance SET num_users = num_users + 1 WHERE name LIKE \"" + insurance + "\";");
 			DB.executeUpdate("INSERT INTO Uses VALUES(" + patientID + ", \"" + insurance + "\");");
 			DB.endTransaction();
 
 			out.println(generate_patient_page(patientID));
 
 		} catch (java.lang.Exception ex2){
 			out.println("<h2> Exception: </h2> <p>"+ ex2.getMessage() +"</p> <br>");
 		}
 	}
 
 	// create an appointment by id (note that patient id is in hidden html field)
 	public void createAppointment(HttpServletRequest request, PrintWriter out){
 		try{
 			// get patient ID
 			int patientID = Integer.parseInt(request.getParameter("pid"));
 
 			// get symptoms and decide on condition
 			DB.beginTransaction();
 			ArrayList<ArrayList<Object>> possibleSymptoms = DB.executeQuery("SELECT sid FROM Symptoms", 1);
 			ArrayList<Object> symptoms = new ArrayList<Object>();
 			for (int i = 0; i < possibleSymptoms.size(); i++)
 				if (request.getParameter("sym_" + possibleSymptoms.get(i).get(0)) != null)
 					symptoms.add(possibleSymptoms.get(i).get(0));
 
 			// P(cond | s1, s2, s3,...) = P(cond) * (P(s1 | cond) * P(s2 | cond) * P(s3 | cond) * ...) / P(s1) * P(s2) ...
 			// find probability of each conditions given the symptoms, denominator is always the same for any given set of symptoms, so normalize out
 
 			// unconditional probabilities of condition (frequency in population)
 			ArrayList<ArrayList<Object>> uncondConditionProbabilities = DB.executeQuery("SELECT cid, probability FROM ConditionsTreats;", 2);
 
 			if (uncondConditionProbabilities.size() == 0){
 				// there are no conditions in the database, maybe print an error?
 				DB.endTransaction();
 				out.println("The database has no conditions in it!");
 				out.println(generate_patient_page(patientID));
 				return;
 			}
 
 			// iterate through all conditions
 			double maxProbability = 0;
 			int maxCondition = -1;
 
 			for (int c = 0; c < uncondConditionProbabilities.size(); c++){
 				int cid = (Integer) uncondConditionProbabilities.get(c).get(0);
 				double prob = (Double) uncondConditionProbabilities.get(c).get(1);
 
 				// get symptom probability
 				ArrayList<ArrayList<Object>> symProbs = DB.executeQuery("SELECT sid, probability FROM Implies WHERE cid = " + cid + ";", 2);
 
 				for (int s = 0; s < symProbs.size(); s++){
 					if (symptoms.contains(symProbs.get(s).get(0))){
 						double mult = (Double) symProbs.get(s).get(1);
 						// a probability of 0 will not be factored in
 						// for example, if you have a cough, that does not mean that you absolutely do not have giardiasis
 						// it is just not related
 						if (mult != 0)
 							prob *= mult;
 					}
 				}
 
 				if (prob > maxProbability){
 					maxCondition = cid;
 					maxProbability = prob;
 				}
 			}
 
 			// now that we know the most likely condition, find the treatment for it and its name
 			ArrayList<ArrayList<Object>> treatment = DB.executeQuery("SELECT C.tid, T.name FROM ConditionsTreats C, Treatments T WHERE C.cid = " + maxCondition + " and T.tid = C.tid;", 2);
 			int tid = (Integer) treatment.get(0).get(0);
 			String treatName = (String) treatment.get(0).get(1);
 
 			// find a doctor that knows how to treat it
 			ArrayList<ArrayList<Object>> doctorToTreat = DB.executeQuery("SELECT K.did FROM Knows K WHERE K.tid = " + tid + ";", 1);
 			int did = (Integer) doctorToTreat.get((int)(doctorToTreat.size()*Math.random())).get(0);
 
 			// find where this doctor works
 			ArrayList<ArrayList<Object>> doctorWorks = DB.executeQuery("SELECT W.fid FROM WorksIn W WHERE W.did = " + did + ";", 1);
 			int fid = (Integer) doctorWorks.get((int)(doctorWorks.size()*Math.random())).get(0);
 
 			// create a new appointment
 			// get a new appointment id
 			Integer aid = 1;
 			Object result = DB.executeQuery("SELECT MAX(aid) FROM Appointments", 1).get(0).get(0);
 			if (result != null)
 				aid = Integer.parseInt(result.toString()) + 1;
 
			String date = "Drop In";

 			// add appointment
			DB.executeUpdate("INSERT INTO Appointments VALUES("+aid+", "+patientID+", "+did+", "+fid+", " + maxCondition + ", \""+date+"\");");
 
 			// add prescripton based on treatment
 			DB.executeUpdate("INSERT INTO TakesPrescriptions VALUES("+patientID+", "+tid+", "+did+", \"indefinitely\");");
 
 			// get symptoms and decide on condition
 			for (int i = 0; i < symptoms.size(); i++)
 				DB.executeUpdate("INSERT INTO SymptomList VALUES("+aid+", "+symptoms.get(i)+");");
 
 			DB.endTransaction();
 			out.println(generate_patient_page(patientID));
 
 		} catch (java.lang.Exception ex2){
 			out.println("<h2> Exception: </h2> <p>"+ ex2.getMessage() +"</p> <br>");
 		}
 	}
 
 	// cancel an appointment by id (note that patient id is in hidden html field)
 	public void cancelAppointmentOrPrescription(HttpServletRequest request, PrintWriter out){
 		try{
 			// get patient ID
 			int patientID = Integer.parseInt(request.getParameter("pid"));
 			int apptID = -1;
 
 			// get a list of all appointment ids for this person
 			DB.beginTransaction();
 			ArrayList<ArrayList<Object>> appts = DB.executeQuery("SELECT A.aid FROM Appointments A WHERE A.pid = " + patientID + ";", 1);
 
 			// check all buttons with ids "cancel_i", where i is aid
 			for (int i = 0; i < appts.size(); i++){
 				apptID = (Integer) appts.get(i).get(0);
 				if (request.getParameter("cancel_" + apptID) != null)
 					break;
 				apptID = -1;
 			}
 			// make sure an appointment was selected
 			if (apptID != -1)
 				DB.executeUpdate("DELETE FROM Appointments WHERE aid = " + apptID + ";");
 
 
 			// not an appointment - must be a prescription
 			int tid = -1, did = -1;
 
 			// get all prescriptions
 			ArrayList<ArrayList<Object>> prescriptions = DB.executeQuery("SELECT P.tid, P.did FROM TakesPrescriptions P WHERE P.pid = " + patientID + ";", 2);
 
 			// check all buttons with ids "remove_i", where i is tid + "|" + did
 			for (int i = 0; i < prescriptions.size(); i++){
 				tid = (Integer) prescriptions.get(i).get(0);
 				did = (Integer) prescriptions.get(i).get(1);
 				if (request.getParameter("remove_" + tid + "|" + did) != null)
 					break;
 				tid = -1;
 			}
 
 			// remove at most one prescription if one was selected (some may be identical, in which case it doesn't matter which is removed)
 			if (tid != -1)
 				DB.executeUpdate("DELETE FROM TakesPrescriptions WHERE pid = " + patientID + " and tid = " + tid + " and did = " + did + " LIMIT 1;");
 
 			DB.endTransaction();
 			out.println(generate_patient_page(patientID));
 
 		} catch (java.lang.Exception ex2){
 			out.println("<h2> Exception: </h2> <p>"+ ex2.getMessage() +"</p> <br>");
 		}
 	}
 
 	// generate a page with a patient's info, where the patient is identified by his/her id
 	private String generate_patient_page(int patientID) throws IOException {
 
 	        String html = readFileAsString(PATIENT_TEMPLATE);
 	        String appointmentRows = "";
 	        String prescriptionRows = "";
 	        String allowedSymptomRows = "<tr>";
 	        String locationDividers = "";
 
 			// begin a transaction
 			DB.beginTransaction();
 
 	        // get patient name
 	        String patientName = (String) DB.executeQuery("SELECT name FROM Patients WHERE pid = " + patientID + ";", 1).get(0).get(0);
 
 			// get all appointment information from database (except symptoms, which will get later)
 			ArrayList<ArrayList<Object>> appointmentInfo = DB.executeQuery("SELECT A.date, D.name, F.fid, F.name, A.aid, C.name FROM Appointments A, Doctors D, Facilities F, ConditionsTreats C WHERE A.pid=" + patientID + " and A.did = D.did and A.fid = F.fid and C.cid = A.cid;", 6);
 
 			// concatenated into comma-separated lists
 	        String[] symptomStrings = new String[appointmentInfo.size()];
 	        for (int row = 0; row < appointmentInfo.size(); row++) {
 				// get symptom info for this appointment
 				ArrayList<ArrayList<Object>> sympList = DB.executeQuery("SELECT S.name FROM Symptoms S, SymptomList L WHERE L.aid = " + appointmentInfo.get(row).get(4) + " and L.sid = S.sid;", 1);
 	            symptomStrings[row] = "";
 	            for (int i = 0; i < sympList.size(); i++) {
 	                if (i != 0)
 	                    symptomStrings[row] += ", ";
 	                symptomStrings[row] += (String) sympList.get(i).get(0);
 	            }
 	        }
 
 			// print appointment info
 	        for (int appt = 0; appt < appointmentInfo.size(); appt++) {
 	            appointmentRows += String.format("<tr> <td> %s </td> <td> %s </td> <td> <a href=\"#\" onclick=\"showhide('location_%d');\">%s</a> </td> <td> %s </td> <td> %s </td> <td> <input type=\"submit\" name=\"cancel_%d\" value=\"Cancel\"> </td> </tr>\n",
 	            (String) appointmentInfo.get(appt).get(0), (String) appointmentInfo.get(appt).get(1), (Integer) appointmentInfo.get(appt).get(2),
 	            (String) appointmentInfo.get(appt).get(3), symptomStrings[appt], (String) appointmentInfo.get(appt).get(5), (Integer) appointmentInfo.get(appt).get(4));
 	            //date[appt], doctor[appt], loc_id[appt], loc_name[appt], symptomStrings[appt], appt_id[appt]);
 	        }
 
 	        // get all prescriptions
	        ArrayList<ArrayList<Object>> prescriptions = DB.executeQuery("SELECT T.name, D.name, T.cost, R.howlong, T.tid, D.did FROM Treatments T, TakesPrescriptions R, Patients P, Doctors D WHERE P.pid = " + patientID + " and R.pid = P.pid and T.tid = R.tid and D.did = R.did;", 6);
 			for (int i = 0; i < prescriptions.size(); i++) {
 				prescriptionRows += String.format("<tr> <td> %s </td> <td> %s </td> <td> %s </td> <td> %s </td>  <td> <input type=\"submit\" name=\"remove_%d|%d\" value=\"Remove\"> </td> </tr>\n",
 				(String) prescriptions.get(i).get(0), (String) prescriptions.get(i).get(1), (Double) prescriptions.get(i).get(2), (String) prescriptions.get(i).get(3), (Integer) prescriptions.get(i).get(4), (Integer) prescriptions.get(i).get(5));
 	        }
 
 			// get all location addresses for dividers
 			ArrayList<ArrayList<Object>> facilityAddrs = DB.executeQuery("SELECT F.fid, F.name, F.addr1, F.addr2 FROM Facilities F", 4);
 	        for (int loc = 0; loc < facilityAddrs.size(); loc++)
 	            locationDividers += String.format("<div id=\"location_%d\" style=\"display: none;\"> <br>\n <b> %s </b>\n <ul>\n %s <br>\n %s\n </ul>\n </div>\n\n",
 	            (Integer) facilityAddrs.get(loc).get(0), (String) facilityAddrs.get(loc).get(1), (String) facilityAddrs.get(loc).get(2), (String) facilityAddrs.get(loc).get(3));
 	            //loc_id[loc], loc_name[loc], loc_addr_1[loc], loc_addr_2[loc]);
 
 			// get all allowed symptoms from database
 			ArrayList<ArrayList<Object>> symptomList = DB.executeQuery("SELECT sid, name FROM Symptoms", 2);
 	        for (int i = 0; i < symptomList.size(); i++) {
 	            allowedSymptomRows += String.format("\t<td> <input type=\"checkbox\" name=\"sym_%d\"> %s </td>", (Integer) symptomList.get(i).get(0), (String) symptomList.get(i).get(1));
 	            if (i > 0 && i < symptomList.size() - 1 && i % 4 == 3)
 	                allowedSymptomRows += "</tr>\n<tr>";
 	        }
 	        allowedSymptomRows += "</tr>";
 	        DB.endTransaction();
 
 	        html = html.replace("%PATIENT_ID%", patientID + "");
 	        html = html.replace("%PATIENT_NAME%", patientName);
 	        html = html.replace("%APPOINTMENT_ROWS%", appointmentRows);
 	        html = html.replace("%LOCATION_DIVIDERS%", locationDividers);
 	        html = html.replace("%POSSIBLE_SYMPTOMS%", allowedSymptomRows);
 	        html = html.replace("%PRESCRIPTION_ROWS%", prescriptionRows);
 
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
