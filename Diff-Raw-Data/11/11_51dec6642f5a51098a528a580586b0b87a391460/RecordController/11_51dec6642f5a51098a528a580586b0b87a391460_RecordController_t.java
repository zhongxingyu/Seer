 package ca.awesome;
 
 import java.sql.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Iterator;
 
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import javax.imageio.ImageIO;
 
 //import oracle.sql.*;
 //import oracle.jdbc.*;
 
 import org.apache.commons.fileupload.DiskFileUpload;
 import org.apache.commons.fileupload.FileItem;
 
 public class RecordController extends Controller {
 	public static final String ID_FIELD = "RECORD_ID";
 	public static final String PATIENT_FIELD = "PATIENT";
 	public static final String DOCTOR_FIELD = "DOCTOR";
 	public static final String RADIOLOGIST_FIELD = "RADIOLOGIST";
 	public static final String TESTTYPE_FIELD = "TESTTYPE";
 	public static final String DIAGNOSIS_FIELD = "DIAGNOSIS";
 	public static final String DESCRIPTION_FIELD = "DESCRIPTION";
 	public static final String PRESCRIBINGDATE_FIELD = "PRESCRIBINGDATE";
 	public static final String TESTDATE_FIELD = "TESTDATE";
 
 	// list of User
 	public Collection<User> patients = new ArrayList<User>();
 	public Collection<User> doctors = new ArrayList<User>();
	//public Collection<User> radiologists = new ArrayList<User>();
 
 	public int record_id = 0;
 	public String doctorName = "";
 	public String patientName = "";
 	public String radiologistName = "";
 	public String testType = "";
 	public String diagnosis = "";
 	public String description = "";
 	public Date prescribing = null;
 	public Date testDate = null;
 	
 
 	public RecordController(ServletContext context,
 			HttpServletRequest request, HttpServletResponse response,
 			HttpSession session) {
 		super(context, request, response, session);
 	}
 
 	// GET insertRecord.jsp
 	public void getInsertRecord() {
 		patients = User.findUsersByType(User.PATIENT_T,
 					getDatabaseConnection(context));
 		doctors = User.findUsersByType(User.DOCTOR_T,
 					getDatabaseConnection(context));
		//radiologists = User.findUsersByType(User.RADIOLOGIST_T,
		//			getDatabaseConnection(context));
 	}
 
 	// POST insertRecord.jsp
 	public boolean attemptInsertRecord() {
 		DatabaseConnection connection = getDatabaseConnection();
 		try {
 			connection.setAutoCommit(false);
 			connection.setAllowClose(false);
 
 			// to generate a unique record_id using an SQL sequence
 			ResultSet results = null;
 			Statement statement = null;
 			statement = connection.createStatement();
 			results = statement.executeQuery(
 				"SELECT record_id_sequence.nextval from dual"
 			);
 			results.next();
 			record_id = results.getInt(1);
 
 			patientName = request.getParameter(PATIENT_FIELD);
 			doctorName = request.getParameter(DOCTOR_FIELD);
			//radiologistName = request.getParameter(RADIOLOGIST_FIELD);
			radiologistName = ((User)session.getAttribute("user")).getUserName();
 			testType = request.getParameter(TESTTYPE_FIELD);
 			diagnosis = request.getParameter(DIAGNOSIS_FIELD);
 			description = request.getParameter(DESCRIPTION_FIELD);
 			prescribing = parseDate(request.getParameter(PRESCRIBINGDATE_FIELD));
 			testDate = parseDate(request.getParameter(TESTDATE_FIELD));
 	
 			Record newRecord = new Record(
 				record_id, patientName, doctorName, radiologistName ,
 				testType, prescribing, testDate, diagnosis, description
 			);
 
 			newRecord.insert(connection);
 
 			connection.commit();
 			return true;
 		} catch (Exception e) {
 			connection.rollback();
 			return false;
 		} finally {
 			connection.setAllowClose(true);
 			connection.close();
 		}
 	}	
 	
 }
