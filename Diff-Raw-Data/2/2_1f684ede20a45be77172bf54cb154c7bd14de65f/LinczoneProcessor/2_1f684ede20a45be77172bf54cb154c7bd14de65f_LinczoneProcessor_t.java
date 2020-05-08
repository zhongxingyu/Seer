 package org.alt60m.linczone;
 
 import java.sql.*;
 import java.util.*;
 
 import org.alt60m.util.DBConnectionFactory;
 import org.alt60m.util.SendMessage;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class LinczoneProcessor {
 	private static Log log = LogFactory.getLog(LinczoneProcessor.class);
 	
 	private final String[] MINISTRIES = new String[] {"ccc", "nav", "iv", "fca", "bsu", "cacm", "efca", "gcm", "wesley"};
 
 	private java.util.Properties _props;
 	private Connection _conn;
 	private boolean _debug;
 	private String _baseDir;
 	
 	
 	public LinczoneProcessor(String propertyFilePath, String baseDir) throws Exception {
 		this(propertyFilePath, baseDir, false);
 	}
 
 	public LinczoneProcessor(String propertyFilePath, String baseDir, boolean debug) throws Exception {
 
 		// Load properties file
 		// Assume that this is present
 		_props = new Properties();
 		_props.load(new java.io.FileInputStream(propertyFilePath));
 		_debug = debug;
 		_baseDir = baseDir;
 		//debug("Class loaded with properties file: '"+propertyFilePath+"'");
 	}
 
 	
 	public void enterNewContact(Map values) throws Exception {
 		
 		try {
 			//log.debug("Getting database driver '"+_props.getProperty("driver")+"'.");
 			// Connect to requested data store
 			//Class.forName(_props.getProperty("driver"));
 
 			//log.debug("Connecting to URL '"+_props.getProperty("url")+"'.");
 
 			_conn = DBConnectionFactory.getDatabaseConn(); //DriverManager.getConnection(_props.getProperty("url"));
 			
 			createRecord(values);
 			createEmails(values);
 		} catch (Exception e) {
 			sendErrorEmail(e);						// send out an error email
 			throw e;
 		}
 
 
 	}
 	
 	private void createRecord(Map values) throws Exception {
 	
 		PreparedStatement ps = _conn.prepareStatement(
 			"INSERT INTO linczone_contacts (" +
 			 "FirstName, LastName, HomeAddress, City, State, Zip, Email, HighSchool, CampusName, CampusID, " +
 			 "ReferrerFirstName, ReferrerLastName, ReferrerRelationship, ReferrerEmail, " +
 			 "InfoCCC, InfoNav, InfoIV, InfoFCA, InfoBSU, InfoCACM, InfoEFCA, InfoGCM, InfoWesley" +
 			") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
 		);
 		
 		ps.setString(1, getValue(values, "firstname"));
 		ps.setString(2, getValue(values, "lastname"));
 		ps.setString(3, getValue(values, "address"));
 		ps.setString(4, getValue(values, "city"));
 		ps.setString(5, getValue(values, "state"));
 		ps.setString(6, getValue(values, "zip"));
 		ps.setString(7, getValue(values, "email"));
 		ps.setString(8, getValue(values, "highschool"));
 		ps.setString(9, getValue(values, "campusname"));
 		ps.setString(10, getValue(values, "campusid"));
 		ps.setString(11, getValue(values, "referrerfirst"));
 		ps.setString(12, getValue(values, "referrerlast"));
 		ps.setString(13, getValue(values, "relationship"));
 		ps.setString(14, getValue(values, "refemail"));
 		
 		for(int i = 0;i<MINISTRIES.length;i++) {
 			boolean selected = isMinistrySelected((String[])values.get("ministryInfo"), MINISTRIES[i]);
 			ps.setString(15+i, (selected ? "T" : "F"));
 		}
 
 		log.debug("Creating record.");
 		
 		// Insert new record
 		ps.executeUpdate();
 	}
 
 	private void createEmails(Map values) throws Exception {
 
 		sendStudentEmail(values);
 		sendMinistryEmail(values);
 	
 	}
 
 	private void sendStudentEmail(Map values) throws Exception {
 
 		String template;
 		boolean isReferral = (getValue(values, "referrer")!=null) && (getValue(values, "referrer").length() > 0);
 
 		// is a referral
 		if(isReferral) {
 			template = _baseDir + "/" + _props.getProperty("email-template-referral");
 		} else {
 			template = _baseDir + "/" +_props.getProperty("email-template-student");
 		}
 
 		java.io.FileReader file = new java.io.FileReader(template);
 		char[] buf = new char[20000];
 		int bytesRead = file.read(buf,0,20000);
 		String text = new String(buf,0,bytesRead);
 
 		
 		String ministryTags ="";
 		for(int i = 0;i<MINISTRIES.length;i++) {
 			if(isMinistrySelected((String[])values.get("ministryInfo"), MINISTRIES[i]))
 				ministryTags += "* " +_props.getProperty("tag-"+MINISTRIES[i])+"\n";
 		}
 
 		
 		// With Jakarta regex package
 		text = (new org.apache.regexp.RE("<%MINISTRY_TAGS%>")).subst(text, ministryTags);
 		text = (new org.apache.regexp.RE("<%FIRST_NAME%>")).subst(text, getValue(values, "firstname"));
 
 		//With JDK 1.4
 		//text = text.replaceAll("<%MINISTRY_TAGS%>", ministryTags);
 
 		if(isReferral) {
 			text = (new org.apache.regexp.RE("<%REFERRER%>")).subst(text, getValue(values, "referrer"));
 			//With JDK1.4
 			//text = text.replaceAll("<%REFERRER%>", getValue(values, "referrer"));
 		}
 		
 		sendEmail(getValue(values, "email"),_props.getProperty("email-is-from"),"Thank you for visiting CollegeWalk", text);
 		
 	}
 
 	private void sendMinistryEmail(Map values) throws Exception {
 		StringBuffer body = new StringBuffer();
 		String firstName = getValue(values, "firstname");
 		String fullName =  firstName + " " + getValue(values, "lastname");
 
 		body.append("Dear Ministry Contact,\n\n");
 		body.append(fullName + " will be attending college this fall and has requested information about your ministry at his/her school from CollegeWalk.com (www.collegewalk.com).\n\n");
 
 		body.append(fullName +"\n");
 		body.append(getValue(values, "address")+"\n");
 		body.append(getValue(values, "city")+ ", " + getValue(values, "state") + " " + getValue(values, "zip") + "\n");
 		body.append(getValue(values, "campusname")+"\n");
 		body.append(getValue(values, "email")+"\n\n");
 		
 		if(getValue(values, "referrerfirst")!=null && getValue(values, "referrerfirst").length() > 0) {
 			String referrerName = getValue(values, "referrerfirst") + " " + getValue(values, "referrerlast");
 			body.append("This request was made on behalf of " + firstName + " by " + referrerName +" (" + getValue(values, "refemail") + ").  His/her relationship to " + firstName+" is " + getValue(values, "relationship")+".\n\n");
 		}
 		
 
 		body.append("Thank you for taking the time to follow-up this contact in a timely manner.\n\n");
 		body.append("Your partner in ministry,\nGodSquad Help Desk\nCampus Crusade for Christ\ngodsquad@uscm.org\n");
 		body.append("\n\n(This information was entered into www.collegewalk.com on " + new java.util.Date()+")\n");
 		
 
 		// *****************************
 		// Handle non-ccc email
 		Vector nonCCCEmails = getNonCCCMinistryEmailAddresses(values);
 		if(nonCCCEmails.size()>0)
 			sendEmail(nonCCCEmails, _props.getProperty("email-is-from"), "Request for Information from CollegeWalk", body.toString());
 
 			
 		// *****************************
 		// Handle CCC Emails
 		
 		String targetAreaID = getValue(values, "campusid");
 		boolean validTargetAreaID = (targetAreaID != null && targetAreaID.length() > 0);
 		boolean requestedCCC = isMinistrySelected((String[])values.get("ministryInfo"), "ccc");
 		Vector CCCEmails = new Vector();
 
 		// if ccc selected AND there is a valid campus id
 		if (requestedCCC && validTargetAreaID) {
 			CCCEmails = getCCCMinistryEmailAddresses(targetAreaID);
 			
 			// Found some contacts
 			if(CCCEmails.size()>0)
 				sendEmail(CCCEmails, _props.getProperty("email-is-from"), "Request for Information from CollegeWalk", body.toString());
 		} 
 		
 		// Handle two special CCC cases
 		// 1. no target area id (they entered it manually) -- send a notice to the default-ccc
 		// 2. no contacts for the campus -- send a notice to default-ccc
 		if(!validTargetAreaID) {
 			// Entered manually, send to CCC default with the following note
 			String msg = "***********************************************************\n" +
 						 "Please note that this campus is not listed in the InfoBase.\n" +
 						 "It was entered manually by the user.\n" + 
 						 "***********************************************************\n\n\n";
 			body.insert(0, msg);
 			sendEmail(_props.getProperty("email-ccc"), _props.getProperty("email-is-from"), "Request for Information from CollegeWalk", body.toString());
 		} else if (requestedCCC && CCCEmails.size()==0) {
 			// No CCC contacts, use CCC default
 			sendEmail(_props.getProperty("email-ccc"), _props.getProperty("email-is-from"), "Request for Information from CollegeWalk", body.toString());
 		}
 
 
 	
 	}
 
 
 	private Vector getCCCMinistryEmailAddresses(String targetAreaID) throws Exception {
 
 		Vector recipients = new Vector();
 
		String cccEmails = "SELECT ministry_targetarea.TargetAreaID, ministry_staff.email FROM ministry_targetarea INNER JOIN ministry_activity ON ministry_targetarea.TargetAreaID = ministry_activity.fk_targetAreaID INNER JOIN ministry_assoc_activitycontact INNER JOIN ministry_staff ON ministry_assoc_activitycontact.accountNo = ministry_staff.accountNo AND ministry_activity.ActivityID = ministry_assoc_activitycontact.ActivityID WHERE ministry_targetarea.TargetAreaID like ?";
 
 		PreparedStatement ps = _conn.prepareStatement(cccEmails);
 		ps.setString(1, targetAreaID);
 		ResultSet rs = ps.executeQuery();
 		while(rs.next()) {
 			recipients.add(rs.getString("email"));
 			log.debug("Adding Crusade email: " + rs.getString("email"));
 		}
 			
 		return recipients;
 	
 	}
 
 	private Vector getNonCCCMinistryEmailAddresses(Map values) throws Exception {
 		
 		Vector recipients = new Vector();
 
 		//List ministryList = java.util.Arrays.asList((String[])values.get("ministryInfo"));
 
 		
 
 
 		// loop over ministries (ignore ccc, in this case, so start at 1)
 		for(int i=1;i<MINISTRIES.length;i++) {
 			// Get other ministry emails
 			boolean selected = isMinistrySelected((String[])values.get("ministryInfo"), MINISTRIES[i]);
 
 			if(selected) { 
 				recipients.add(_props.getProperty("email-"+MINISTRIES[i]));
 				log.debug("Adding email: " + _props.getProperty("email-"+MINISTRIES[i]) );
 			}
 		}
 
 		return recipients;
 	}
 
 	private void sendErrorEmail(Exception e) {
 	
 		// Ok, there was an error. Lets try to send it to someone
 		try { 
 
 			String body = "There was an error processing a linczone request.\n\nTime:"+new java.util.Date()+"\nError: "+e;			
 			sendEmail(_props.getProperty("email-error"), _props.getProperty("email-error"), "CollegeWalk error", body);
 		} catch (Exception ignore) {}
 
 	}
 
 	protected void sendEmail(String recipients, String from, String subject, String body) throws Exception{
 		Vector recips = new Vector();
 		recips.add(recipients);
 		sendEmail(recips, from,subject,body);
 	}
 	protected void sendEmail(Vector recipients, String from, String subject, String body) throws Exception{
 	
 		SendMessage msg = new SendMessage();
 		msg.setFrom(from);
 
 		String debugEmail = _props.getProperty("debug-override-email");
 		if(debugEmail!=null) {
 			msg.setTo(debugEmail);
 			body = "***************************\n" +
 				   "DEBUG: Original recipients: \n" + 
 				   recipients + "\n" +
 				   "***************************\n\n\n" + 
 				   body;	
 		} else {
 			for(Iterator i = recipients.iterator();i.hasNext();) {
 				msg.addTo((String)i.next());
 			}
 		}
 
 		msg.setSubject(subject);
 		msg.setBody(body);
 		msg.send();
 	}
 
 	private String getValue(Map map, String key) {
 		String[] val = (String[]) map.get(key);
 		if(val!=null) return val[0]; 
 		else return null;
 	}
 
 	public static boolean isMinistrySelected(String[] ministriesSelected, String ministry) {
 		if(ministriesSelected==null) return false;
 
 		for(int cnt=0;cnt<ministriesSelected.length;cnt++) {
 			if(ministry.equalsIgnoreCase(ministriesSelected[cnt])) return true;
 			if("all".equalsIgnoreCase(ministriesSelected[cnt])) return true;
 		}
 		return false;
 	}
 
 }
 
