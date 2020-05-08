 package org.alt60m.ministry.servlet;
 
 import java.util.*;
 import org.apache.log4j.*;
 import org.alt60m.servlet.*;
 import org.alt60m.ministry.*;
 import org.alt60m.ministry.model.dbio.*;
 
 /**
  * The web controller for the HR Tool.  
  * Specifically, contains methods for taking in a form for job change, address change, marital status change, leave of absense, and resignation, and then massaging the data to pass it along to the HRTool object.  
  * The two main functions of this class are to take input from a web form and convert it to a hashtable that can be used throughout the tool and then take that hashtable and pass it along to process the change request.
  * The default action is showFormList.
  * <p>
  * History: began documenting already-exisitng controller (3/27/03) MAB<br>
  * Coding Completeness (0 - 5):<br>
  * Known Issues:<br>
  * @author Mike Brinkley
  * @version
  */
 public class HRUpdateController extends Controller {
 	/**
 	* saves this change to the database and makes appropriate approvals/field changes/notifications 
 	* where necessary.&nbsp;takes the "formData" hashtable out of the request and sends it to the 
 	* addressChangeRequest in the HRTool class to process the change request &nbsp;then sends the 
 	* user to the postConfirm method.
 	* <p>
 	* gets "staffInfo" hashtable out of session<br>
 	* out of staff info, gets the account number of this staff and the staff who requested this change
 	* <p>
 	* gets "formData" out of session<br>
 	* passes the staffID, the requestedBy id, and the formData hashtable to the {@link HRTool#addressChangeRequest(String, String, Map) addressChangeRequest} method in the HRTool object
 	* <p>
 	* passes the value of hrInitiated from the session as well as the ActionContext to the {@link #postConfirm(String, Controller.ActionContext) postConfirm} method
 	*
 	* @param ctx ActionContext, gets "staffInfo" and "formData" and "hrInitiated" out of the session
 	*/
 	public void addressChangeConfirm(ActionContext ctx) {
 		try {
 			Hashtable staff =
 				(Hashtable) (ctx.getSession().getAttribute("staffInfo"));
 			String staffID = (String) staff.get("AccountNo");
 			String requestedBy = (String) staff.get("RequestedBy");
 			if (requestedBy == null || requestedBy.equals("")) {
 				log.debug("staff doing self.");
 				requestedBy = staffID;
 			} else {
 				log.debug("HR doing a staff.");
 			}
 			Hashtable newValues =
 				(Hashtable) ctx.getSession().getAttribute("formData");
 			newValues.put("AccountNo", staffID);
 			newValues.put("FirstName", staff.get("FirstName"));
 			newValues.put("LastName", staff.get("LastName"));
 			HRTool hrt = new HRTool();
 			hrt.addressChangeRequest(staffID, requestedBy, newValues);
 			postConfirm((String) ctx.getSession().getAttribute("hrInitiated"), ctx);
 		} catch (Exception e) {
 			ActionResults ar = new ActionResults();
 			ar.putValue(
 				"exceptionText",
 				"HRUpdateController:addressChangeConfirm:" + e.toString());
 			ctx.setReturnValue(ar);
 			ctx.goToView("error");
 		}
 	}
 	/**
 	* gets many input strings from the request object (via the ActionContext) and saves them to a local hashtable - newValues - and puts that hashtable in the session as "formData".&nbsp;sends the user to the formConfirm page. 
 	* <p>
 	* for the following, if the input does not equal what the person already has, then add to our hashtable of new values the name of the field and what they inputted:<br>
 	* countryStatus
 	* <p>
 	* for the following, if the input is more than 0 characters long and (the existing value is nothing or (the existing value is different than the input value)),
 	* then add to our hashtable of new values the name of the field and what they inputted:<br>
 	* primaryStreet1<br>
 	* primaryStreet2<br>
 	* primaryStreet3<br>
 	* primaryStreet4<br>
 	* primaryCity<br>
 	* primaryState<br>
 	* primaryZip<br>
 	* primaryCountry<br>
 	* <p>
 	* otherwise (if the previous check is false), then if the following input is 0 characters and the exisiting value is something, implying they are zero-ing out that field, set that value in newValues to be the empty string "" just so that it's not null when QueueChangeRequest tries to read it:
 	* primaryStreet1<br>
 	* primaryStreet2<br>
 	* primaryStreet3<br>
 	* primaryStreet4<br>
 	* <p>
 	* if the length of the beginMonth input is longer than one character, then add the beginning month/day/year as the primaryStartDate in our hashtable of new values<br>
 	* otherwise, if the length of the effectiveMonth input is longer than one character, then add the effective month/day/year as the primaryStartDate in our hashtable of new values<br>
 	* otherwise, add today's month/day/year as the primaryStartDate in our hashtable of new values
 	* <p>
 		* if the length of the endMonth input is longer than one character, then add the beginning month/day/year as the primaryEndDate in our hashtable of new values
 	* <p>
 	* for the following, if the length of the input field is longer than 0 characters and if the input does not equal what they currently already have, add this field to our hashtable of new values:<br>
 	* homePhone<br>
 	* workPhone<br>
 	* email<br>
 	* spouseEmail<br>
 	* emplState<br>
 	* emplCountry<br>
 	* emplCity<br>
 	* <p>
 	* for the following, if the input field is not null, then add it to our hashtable:<br>
 	* addressChangeType<br>
 	* addressReason<br>
 	* addressType
 	* <p>
 	* for the following, if the input field is longer than 0 characters, then add it to our hashtable:<br>
 	* schoolDictrict<br>
 	* <p>
 	* add to our hashtable that changeType is address change and action is addressChangeConfirm.
 	* <p>
 	* go to formConfirm
 	* 
 	* @param ctx ActionContext, gets "staffInfo" out of the session, puts "formData" into the session
 	*/
 	public void addressChangeRequest(ActionContext ctx) {
 		log.debug("entering addressChangeRequest");
 		try {
 			Hashtable staff = (Hashtable) (ctx.getSession().getAttribute("staffInfo"));
 			Hashtable primaryAddress = (Hashtable) (staff.get("primaryAddress"));
 			Hashtable newValues = new Hashtable();
 			if (!ctx.getInputString("countryStatus").equals((String) staff.get("CountryStatus"))) {
 				newValues.put("countryStatus", ctx.getInputString("countryStatus"));
 			}
 
 			if (ctx.getInputString("primaryStreet1").length() > 0
 				&& (primaryAddress == null
 					|| !(ctx.getInputString("primaryStreet1").equals((String) primaryAddress.get("Address1"))))) {
 				newValues.put("primaryAddress1",ctx.getInputString("primaryStreet1"));
 			} else if (ctx.getInputString("primaryStreet1").length() == 0
 					&& primaryAddress != null
 					&& !((String) primaryAddress.get("Address1") == null
 						|| ((String) primaryAddress.get("Address1")).equals(""))) {
 				newValues.put("primaryAddress1", "");
 			}
 
 			if (ctx.getInputString("primaryStreet2").length() > 0
 				&& (primaryAddress == null
 					|| !(ctx.getInputString("primaryStreet2").equals((String) primaryAddress.get("Address2"))))) {
 				newValues.put("primaryAddress2",ctx.getInputString("primaryStreet2"));
 			} else if (
 				ctx.getInputString("primaryStreet2").length() == 0
 					&& primaryAddress != null
 					&& !((String) primaryAddress.get("Address2") == null
 						|| ((String) primaryAddress.get("Address2")).equals(""))) {
 				newValues.put("primaryAddress2", "");
 			}
 
 			if (ctx.getInputString("primaryStreet3").length() > 0
 				&& (primaryAddress == null
 					|| !(ctx.getInputString("primaryStreet3").equals((String) primaryAddress.get("Address3"))))) {
 				newValues.put("primaryAddress3",ctx.getInputString("primaryStreet3"));
 			} else if (
 				ctx.getInputString("primaryStreet3").length() == 0
 					&& primaryAddress != null
 					&& !((String) primaryAddress.get("Address3") == null
 						|| ((String) primaryAddress.get("Address3")).equals(""))) {
 				newValues.put("primaryAddress3", "");
 			}
 
 			if (ctx.getInputString("primaryStreet4").length() > 0
 				&& (primaryAddress == null
 					|| !(ctx.getInputString("primaryStreet4").equals((String) primaryAddress.get("Address4"))))) {
 				newValues.put("primaryAddress4",ctx.getInputString("primaryStreet4"));
 			} else if (
 				ctx.getInputString("primaryStreet4").length() == 0
 					&& primaryAddress != null
 					&& !((String) primaryAddress.get("Address4") == null
 						|| ((String) primaryAddress.get("Address4")).equals(""))) {
 				newValues.put("primaryAddress4", "");
 			}
 
 			if (ctx.getInputString("primaryCity").length() > 0
 				&& (primaryAddress == null
 					|| !(ctx
 						.getInputString("primaryCity")
 						.equals((String) primaryAddress.get("City"))))) {
 				newValues.put("primaryCity", ctx.getInputString("primaryCity"));
 			}
 			if (ctx.getInputString("primaryState").length() > 0
 				&& (primaryAddress == null
 					|| !(ctx
 						.getInputString("primaryState")
 						.equals((String) primaryAddress.get("State"))))) {
 				newValues.put(
 					"primaryState",
 					ctx.getInputString("primaryState"));
 			} else if (
 				ctx.getInputString("primaryState").length() == 0
 					&& !ctx.getInputString("primaryCountry").equals("USA")
 					&& primaryAddress != null
 					&& !((String) primaryAddress.get("State") == null
 						|| ((String) primaryAddress.get("State")).equals(""))) {
 				newValues.put("primaryState", "");
 			}
 			if (ctx.getInputString("primaryZip").length() > 0
 				&& (primaryAddress == null
 					|| !(ctx
 						.getInputString("primaryZip")
 						.equals((String) primaryAddress.get("Zip"))))) {
 				newValues.put("primaryZip", ctx.getInputString("primaryZip"));
 			}
 			if (ctx.getInputString("primaryCountry").length() > 0
 				&& (primaryAddress == null
 					|| !(ctx
 						.getInputString("primaryCountry")
 						.equals((String) primaryAddress.get("Country"))))) {
 				newValues.put(
 					"primaryCountry",
 					ctx.getInputString("primaryCountry"));
 			}
 
 			String beginDate;
 			if (ctx.getInputString("beginMonth").length() != 0) {
 				beginDate =
 					ctx.getInputString("beginMonth")
 						+ "/"
 						+ ctx.getInputString("beginDay")
 						+ "/"
 						+ ctx.getInputString("beginYear");
 			} else if (ctx.getInputString("effectiveMonth").length() != 0) {
 				beginDate =
 					ctx.getInputString("effectiveMonth")
 						+ "/"
 						+ ctx.getInputString("effectiveDay")
 						+ "/"
 						+ ctx.getInputString("effectiveYear");
 			} else {
 				//assume immediate effective date unless specified otherwise
 				Calendar c = Calendar.getInstance();
 				beginDate =
 					""
 						+ c.get(Calendar.MONTH)
 						+ "/"
 						+ c.get(Calendar.DAY_OF_MONTH)
 						+ "/"
 						+ c.get(Calendar.YEAR);
 			}
 			newValues.put("primaryStartDate", beginDate);
 			if (ctx.getInputString("endMonth").length() > 0) {
 				String endDate =
 					ctx.getInputString("endMonth")
 						+ "/"
 						+ ctx.getInputString("endDay")
 						+ "/"
 						+ ctx.getInputString("endYear");
 				newValues.put("primaryEndDate", endDate);
 			}
 			if (ctx.getInputString("homePhone").length() > 0
 				&& !(ctx
 					.getInputString("homePhone")
 					.equals((String) staff.get("HomePhone")))) {
 				newValues.put("homePhone", ctx.getInputString("homePhone"));
 			} else if (
 				ctx.getInputString("homePhone").length() == 0
 					&& !((String) staff.get("HomePhone") == null
 						|| ((String) staff.get("HomePhone")).equals(""))) {
 				newValues.put("homePhone", "");
 			}
 			if (ctx.getInputString("workPhone").length() > 0
 				&& !(ctx
 					.getInputString("workPhone")
 					.equals((String) staff.get("WorkPhone")))) {
 				newValues.put("workPhone", ctx.getInputString("workPhone"));
 			} else if (
 				ctx.getInputString("workPhone").length() == 0
 					&& !((String) staff.get("WorkPhone") == null
 						|| ((String) staff.get("WorkPhone")).equals(""))) {
 				newValues.put("workPhone", "");
 			}
 			if (ctx.getInputString("email").length() > 0
 				&& !(ctx
 					.getInputString("email")
 					.equals((String) staff.get("Email")))) {
 				newValues.put("email", ctx.getInputString("email"));
 			}
 			if (ctx.getInputString("spouseEmail").length() > 0
 				&& !(ctx
 					.getInputString("spouseEmail")
 					.equals((String) staff.get("SpouseEmail")))) {
 				newValues.put("spouseEmail", ctx.getInputString("spouseEmail"));
 			}
 			if (ctx.getInputString("emplState").length() > 0
 				&& !(ctx
 					.getInputString("emplState")
 					.equals((String) staff.get("PrimaryEmpLocState")))) {
 				newValues.put(
 					"primaryEmpLocState",
 					ctx.getInputString("emplState"));
 			} else if (
 				ctx.getInputString("emplState").length() == 0
 					&& !ctx.getInputString("emplCountry").equals("USA")
 					&& !((String) staff.get("PrimaryEmpLocState") == null
 						|| ((String) staff.get("PrimaryEmpLocState")).equals(
 							""))) {
 				newValues.put("primaryEmpLocState", "");
 			}
 
 			if (ctx.getInputString("emplCountry").length() > 0
 				&& !(ctx
 					.getInputString("emplCountry")
 					.equals((String) staff.get("PrimaryEmpLocCountry")))) {
 				newValues.put(
 					"primaryEmpLocCountry",
 					ctx.getInputString("emplCountry"));
 			}
 			if (ctx.getInputString("emplCity").length() > 0
 				&& !(ctx
 					.getInputString("emplCity")
 					.equals((String) staff.get("PrimaryEmpLocCity")))) {
 				newValues.put(
 					"primaryEmpLocCity",
 					ctx.getInputString("emplCity"));
 			}
 			if (ctx.getInputString("addrChangeType") != null) {
 				newValues.put(
 					"addrChangeType",
 					ctx.getInputString("addrChangeType"));
 			}
 			if (ctx.getInputString("schoolDistrict").length() > 0) {
 				newValues.put(
 					"schoolDistrict",
 					ctx.getInputString("schoolDistrict"));
 			}
 			if (ctx.getInputString("addressReason") != null) {
 				newValues.put(
 					"addressReason",
 					ctx.getInputString("addressReason"));
 			}
 			if (ctx.getInputString("addressType") != null) {
 				newValues.put("addressType", ctx.getInputString("addressType"));
 			}
 
 			newValues.put("changeType", StaffChangeRequest.ADDRESS_CHANGE);
 			newValues.put("action", "addressChangeConfirm");
 			ctx.setSessionValue("formData", newValues);
 			ctx.goToView("formConfirm");
 		} catch (Exception e) {
 			ActionResults ar = new ActionResults();
 			ar.putValue(
 				"exceptionText",
 				"HRUpdateController:addressChangeRequest:" + e.toString());
 			log.error(
 				"HRUpdateController:addressChangeRequest:" + e.toString(), e);
 			ctx.setReturnValue(ar);
 			ctx.goToView("error");
 		}
 	}
 	/**
 	* saves this change to the database and makes appropriate approvals/field changes/notifications where necessary.&nbsp;takes the "formData" hashtable out of the request and sends it to the dependentStatusChangeRequest in the HRTool class to process the change request &nbsp;then sends the user to the postConfirm method.
 	* <p>
 	* gets "staffInfo" hashtable out of session<br>
 	* out of staff info, gets the account number of this staff and the staff who requested this change<br>
 	* <p>
 	* gets "formData" out of session<br>
 	* passes the staffID, the requestedBy id, and the formData hashtable to the {@link HRTool#dependentChangeRequest(String, String, Map) dependentChangeRequest} method in the HRTool object
 	* <p>
 	* passes the value of hrInitiated from the session as well as the ActionContext to the {@link #postConfirm(String, Controller.ActionContext) postConfirm} method
 	*
 	* @param ctx ActionContext, gets "staffInfo" and "formData" and "hrInitiated" out of the session
 	*/
 	public void dependentChangeConfirm(ActionContext ctx) {
 		try {
 			Hashtable staff =
 				(Hashtable) (ctx.getSession().getAttribute("staffInfo"));
 			String staffID = (String) staff.get("AccountNo");
 			String requestedBy = (String) staff.get("RequestedBy");
 			if (requestedBy == null || requestedBy.equals("")) {
 				log.debug("staff doing self.");
 				requestedBy = staffID;
 			} else {
 				log.debug("HR doing a staff.");
 			}
 			Hashtable newValues =
 				(Hashtable) ctx.getSession().getAttribute("formData");
 			newValues.put("AccountNo", staffID);
 			newValues.put("FirstName", staff.get("FirstName"));
 			newValues.put("LastName", staff.get("LastName"));
 			HRTool hrt = new HRTool();
 			hrt.dependentChangeRequest(staffID, requestedBy, newValues);
 			postConfirm((String) ctx.getSession().getAttribute("hrInitiated"), ctx);
 		} catch (Exception e) {
 			ActionResults ar = new ActionResults();
 			ar.putValue("exceptionText", e.toString());
 			ctx.setReturnValue(ar);
 			ctx.goToView("error");
 		}
 	}
 	/**
 	* gets many input strings from the request object (via the ActionContext) and saves them to a local hashtable - newValues - and puts that hashtable in the session as "formData".&nbsp;sends the user to the formConfirm page. 
 	* <p>
 	* if newChild is inputted, puts "newChild" as "true" in newValues.<br>
 	* for the following, if the input is not 0 characters long, put them in newValues:<br>
 	* childFirst
 	* childMiddle
 	* childLast
 	* if childBirthMonth length is not 0 characters long, saves the child's birthdate as childBirthMonth/childBirthDay/childBirthYear<br>
 	* if childHomeMonth length is greater than 0 characters long, saves the child's adoptdate as childHomeMonth/childHomeDay/childHomeYear<br>
 	* if the length of numChildren input is greater than 0 characters, saves numChildren to newValues<br>
 	* if childGender input is not null, saves the child's gender to newValues<br>
 	* if ineligibleDep input is not null, then ...
 	* &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;puts "ineligibleDep" as "true" in newValues<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;puts "ineligibleDependentName" as dependentName input in newValues<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;puts "ineligibleReason" as ineligibleReason input in newValues<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if tooOldMonth input length is more than 0 characters, then ...<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;saves ineligibleDate as tooOldMonth/tooOldDay/tooOldYear inputs<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;otherwise, saves ineligibleDate as notStudentMonth/notStudentDay/notStudentYear inputs<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;puts ineligibleDate in newValues<br>
 	* <p>
 	* puts changeType as dependentChange and action as dependentChangeConfirm<br>
 	* puts newValues into session object as "formData"<br>
 	* goes to formConfirm
 	*
 	* @param ctx ActionContext object, which is used to get the inputs and set the session hashtable "formData"
 	*/
 	public void dependentChangeRequest(ActionContext ctx) {
 		try {
 			Hashtable newValues = new Hashtable();
 			if (ctx.getInputString("newChild") != null) {
 				newValues.put("newChild", "true");
 				if (ctx.getInputString("childFirst").length() != 0) {
 					newValues.put(
 						"firstName",
 						ctx.getInputString("childFirst"));
 				}
 				if (ctx.getInputString("childMiddle").length() != 0) {
 					newValues.put(
 						"middleName",
 						ctx.getInputString("childMiddle"));
 				}
 				if (ctx.getInputString("childLast").length() != 0) {
 					newValues.put("lastName", ctx.getInputString("childLast"));
 				}
 
 				if (ctx.getInputString("childBirthMonth").length() != 0) {
 					String birthDate =
 						ctx.getInputString("childBirthMonth")
 							+ "/"
 							+ ctx.getInputString("childBirthDay")
 							+ "/"
 							+ ctx.getInputString("childBirthYear");
 					newValues.put("birthDate", birthDate);
 				}
 
 				if (ctx.getInputString("childHomeMonth").length() > 0) {
 					String adoptDate =
 						ctx.getInputString("childHomeMonth")
 							+ "/"
 							+ ctx.getInputString("childHomeDay")
 							+ "/"
 							+ ctx.getInputString("childHomeYear");
 					newValues.put("adoptDate", adoptDate);
 				}
 
 				if (ctx.getInputString("numChildren").length() > 0) {
 					newValues.put(
 						"numChildren",
 						ctx.getInputString("numChildren"));
 				}
 				if (ctx.getInputString("childGender") != null) {
 					newValues.put("gender", ctx.getInputString("childGender"));
 				}
 			}
 
 			if (ctx.getInputString("ineligibleDep") != null) {
 				newValues.put("ineligibleDep", "true");
 				newValues.put(
 					"ineligibleDependentName",
 					ctx.getInputString("dependentName"));
 				newValues.put(
 					"ineligibleReason",
 					ctx.getInputString("ineligibleReason"));
 				String ineligibleDate;
 				if (ctx.getInputString("tooOldMonth").length() > 0) {
 					ineligibleDate =
 						ctx.getInputString("tooOldMonth")
 							+ "/"
 							+ ctx.getInputString("tooOldDay")
 							+ "/"
 							+ ctx.getInputString("tooOldYear");
 				} else {
 					ineligibleDate =
 						ctx.getInputString("notStudentMonth")
 							+ "/"
 							+ ctx.getInputString("notStudentDay")
 							+ "/"
 							+ ctx.getInputString("notStudentYear");
 				}
 				newValues.put("ineligibleDate", ineligibleDate);
 			}
 			newValues.put("changeType", StaffChangeRequest.DEPENDENT_CHANGE);
 			newValues.put("action", "dependentChangeConfirm");
 			ctx.setSessionValue("formData", newValues);
 			ctx.goToView("formConfirm");
 		} catch (Exception e) {
 			ActionResults ar = new ActionResults();
 			ar.putValue("exceptionText", e.toString());
 			ctx.setReturnValue(ar);
 			ctx.goToView("error");
 		}
 	}
 	/**
 	* gets the list of teams in a region and jobs for a strategy and puts those two collections in the response object (via the ActionResults).
 	* <p>
 	* saves the input for region and lane locally as region and strategy.<br>
 	* if regionChange input is not null, then saves newRegion locally as region.<br>
 	* if laneChange input is not null, then saves newLane locally as strategy.<br>
 	* <p>
 	* calls {@link HRTool#getTeamList(String) getTeamList} in the HRTool object, passing it region, to get the collection of teams in this region.<br>
 	* iterates through that list and adds the teamname and teamID to a local collection of teams.<br>
 	* <p>
 	* then, hardcoded into this method, generates a list of job titles based on what was inputted for strategy.<br>
 	* adds onto the end (regardless of what strategy they are) "Seminary Staff" and "Other".
 	* <p>
 	* puts those two collections into the ActionResults<br>
 	* if spouse input is not null, then goes to spousejobchange2,<br>
 	* otherwise, goes to jobchange2.
 	*
 	* @param ctx ActionContext object, out of which get the session
 	*/
 	public void getTeamList(ActionContext ctx) throws Exception {
 		try {
 			Hashtable staff =
 				(Hashtable) (ctx.getSession().getAttribute("staffInfo"));
 			String staffID = (String) staff.get("AccountNo");
 			log.debug("top staffInfo accountno is " + staffID);
 			log.debug(
 				"top getteamlist session accountNo is "
 					+ ctx.getSessionValue("accountNo"));
 
 			String region = ctx.getInputString("region");
 			if (ctx.getInputString("regionChange") != null) {
 				region = ctx.getInputString("newRegion");
 			}
 			String strategy = ctx.getInputString("lane");
 			if (ctx.getInputString("laneChange") != null) {
 				strategy = ctx.getInputString("newLane");
 			}
 			HRTool hrt = new HRTool();
 			Collection c;
 			if (ctx.getInputString("showAllTeams") == null)
 			{
 				c = hrt.getTeamList(region);
 			}
 			else {
 				c = hrt.getTeamList();
 			}
 			List teams = new ArrayList();
 			Iterator it = c.iterator();
 			while (it.hasNext()) {
 				LocalLevel team = (LocalLevel) it.next();
 				Hashtable team1 = new Hashtable();
 				team1.put("name", team.getName());
 				team1.put("ID", team.getLocalLevelId());
 				teams.add(team1);
 			}
 
 			List jobs = new ArrayList();
 			if ("Catalytic".equals(strategy)) {
 				jobs.add("Catalytic Director");
 				jobs.add("Metro Director");
 				jobs.add("Student LINC");
 				jobs.add("Field LINC");
 				jobs.add("Catalytic staff");
 				jobs.add("Catalytic staff coach");
 				jobs.add("Catalytic staff in training");
 			}
 
 			if ("Staffed".equals(strategy)) {
 				jobs.add("SC Director");
 				jobs.add("SC Associate Director");
 				jobs.add("SC Coach");
 				jobs.add("SC in Training");
 				jobs.add("SC Staff");
 			}
 
 			if ("ESM".equals(strategy)) {
 
 				jobs.add("Destino Director");
 				jobs.add("Destino Associate Director");
 				jobs.add("Destino City Director");
 				jobs.add("Destino Coach");
 				jobs.add("Destino Staff");
 				jobs.add("Destino Staff in Training");
 
 				jobs.add("Epic Director");
 				jobs.add("Epic Associate Director");
 				jobs.add("Epic City Director");
 				jobs.add("Epic Coach");
 				jobs.add("Epic Staff");
 				jobs.add("Epic Staff in Training");
 			}
 
 			if ("HR".equals(strategy)) {
 				jobs.add("Sending Director");
 				jobs.add("Sending Associate");
 				jobs.add("Sending Coach");
 				jobs.add("Sending Coordinator");
 				jobs.add("Services Associate");
 				jobs.add("Recruiting Director");
 				jobs.add("Recruiting Associate");
 				jobs.add("Leadership Development Director");
 				jobs.add("Leadership Development Associate");
 				jobs.add("Staff Care Director");
 				jobs.add("Staff Care Associate");
 				jobs.add("HR Associate");
 				jobs.add("Intern Coordinator");
 				jobs.add("New Staff Development");
 				jobs.add("Emerging Leader Development");
 				jobs.add("Placement Coordinator");
 			}
 
 			if ("Fund Dev".equals(strategy)) {
 				jobs.add("Associate National Director");
 				jobs.add("Major Donor Representative");
 				jobs.add("Mid Level Development Representative");
 				jobs.add("Direct Marketing Director");
 				jobs.add("Direct Marketing Customer Service");
 				jobs.add("Regional/Local Development Representative");
 				jobs.add("Director of Regional/Local Development");
 			}
 
 			if ("National Director's Office".equals(strategy)) {
 				jobs.add("National Director");
 				jobs.add("Chief of Staff");
 				jobs.add("Administrative Assistant");
 			}
 			jobs.add("Seminary Staff");
 			jobs.add("Other");
 
 			ActionResults ar = new ActionResults();
 			ar.addCollection("teamList", teams);
 			ar.addCollection("jobs", jobs);
 			ctx.setReturnValue(ar);
 
 			log.debug("bottom staffInfo accountno is " + staffID);
 			log.debug(
 				"bottom getteamlist session accountNo is "
 					+ ctx.getSessionValue("accountNo"));
 
 			if (ctx.getInputString("spouse") != null) {
 				ctx.goToView("spouseJobChange2");
 			} else {
 				ctx.goToView("jobChange2");
 			}
 		} catch (Exception e) {
 			log.error(e.getMessage(), e);
 			ActionResults ar = new ActionResults();
 			ar.putValue("exceptionText", e.toString());
 			ctx.setReturnValue(ar);
 			ctx.goToView("error");
 		}
 	}
 
 	/**
 	* sets views file and default action<br>
 	*/
 	public void init() {
 		try {
 			String pathToViews = getServletContext().getRealPath("/WEB-INF/HRUpdateViews.xml");
 			setViewsFile(pathToViews);
 			setDefaultAction("showFormList");
 			log.info("init() completed.  Ready for action.");
 		} catch (Exception e) {
 			log.error("failed to init", e);
 		}
 	}
 	/**
 	* saves this change to the database and makes appropriate approvals/field changes/notifications where necessary.&nbsp;takes the "formData" hashtable out of the request and sends it to the jobChangeRequest in the HRTool class to process the change request &nbsp;then sends the user to the postConfirm method.
 	* <p>
 	* gets "staffInfo" hashtable out of session<br>
 	* out of staff info, gets the account number of this staff and the staff who requested this change.<br>
 	* if the spouse is requesting the change for their spouse, then use this spouse's account number at the staff account number of whose info is being changed.<br>
 	* <p>
 	* gets "formData" out of session<br>
 	* passes the staffID, the requestedBy id, and the formData hashtable to the {@link HRTool#dependentChangeRequest(String, String, Map) dependentChangeRequest} method in the HRTool object
 	* <p>
 	* passes the value of hrInitiated from the session as well as the ActionContext to the {@link #postConfirm(String, Controller.ActionContext) postConfirm} method
 	*
 	* @param ctx ActionContext, gets "staffInfo" and "formData" and "hrInitiated" out of the session
 	*/
 	public void jobChangeConfirm(ActionContext ctx) {
 		try {
 			Hashtable staff =
 				(Hashtable) (ctx.getSession().getAttribute("staffInfo"));
 			String staffID = (String) staff.get("AccountNo");
 			String requestedBy = (String) staff.get("RequestedBy");
 
 			log.debug("staffid is " + staffID);
 			log.debug("requestedBy is " + requestedBy);
 			log.debug("top staffInfo accountno is " + staffID);
 			log.debug(
 				"top getteamlist session accountNo is "
 					+ ctx.getSessionValue("accountNo"));
 
 			if (ctx.getInputString("spouse") != null) {
 				staffID = (String) staff.get("SpouseAccountNo");
 				requestedBy = (String) staff.get("AccountNo");
 			}
 			Hashtable newValues =
 				(Hashtable) ctx.getSession().getAttribute("formData");
 			boolean includeSpouse =
 				((Boolean) newValues.get("includeSpouse")).booleanValue();
 			if (requestedBy == null || requestedBy.equals("")) {
 				log.debug("staff doing self.");
 				requestedBy = staffID;
 			} else {
 				log.debug("HR doing a staff.");
 			}
 			HRTool hrt = new HRTool();
 			hrt.jobChangeRequest(staffID, requestedBy, newValues);
 			if (includeSpouse) {
 				requestedBy = staffID;
 				staffID = (String) staff.get("SpouseAccountNo");
 				HRTool hrt2 = new HRTool();
 				hrt2.jobChangeRequest(staffID, requestedBy, newValues);
 			}
 			log.debug("bottom staffInfo accountno is " + staffID);
 			log.debug(
 				"bottom getteamlist session accountNo is "
 					+ ctx.getSessionValue("accountNo"));
 
 			postConfirm((String) ctx.getSession().getAttribute("hrInitiated"), ctx);
 		} catch (AuthorizerNotFoundException e) {
 			log.error(e.getMessage(), e);
 			ActionResults ar = new ActionResults();
 			ar.putValue("exceptionText", "Your request has been submitted to your regional HR team for approval.  " +
 				"Unfortunately, the HR Regional Director is unable to receive an email notification of this request because there is no HRRD set up in the computer system for the " + e.getAuthRegion() + " region.  " +
 				"Contact this regional office to inform them of the problem and make sure they are aware of your pending request.");
 //			ar.putValue("exceptionText", "This change requires the approval of the " + e.getAuthRegion() + " Region " + e.getAuthRole() + ".  " +
 //				"However, our database does not know who this person is.  An email has already been sent notifying the help desk of this problem.");
 			ctx.setReturnValue(ar);
 			ctx.goToView("notice");
 		} catch (BadRegionException e) {
 			log.error(e.getMessage(), e);
 			ActionResults ar = new ActionResults();
 			ar.putValue("exceptionText", "Your current region is not recognized. This could be because you are not " +
 					"currently listed in the Campus Ministry. Please make sure to indicate your ministry as \"Campus " +
 					"Ministry\" on the first page of this form.");
 			ctx.setReturnValue(ar);
 			ctx.goToView("error");
 		} catch (Exception e) {
 			log.error(e.getMessage(), e);
 			ActionResults ar = new ActionResults();
 			ar.putValue("exceptionText", e.toString());
 			ctx.setReturnValue(ar);
 			ctx.goToView("error");
 		}
 	}
 	/**
 	* gets many input strings from the request object (via the ActionContext) and saves them to a local hashtable - newValues - and puts that hashtable in the session as "formData".&nbsp;sends the user to the formConfirm page. 
 	* <p>
 	* stores locally startDate as jobChangeMonth/jobChangeDay/jobChangeYear inputs, and puts that in newValues
 	* <p>
 	* if includeSpouse input is given, then puts "includeSpouse" as true in newValues.<br>
 	* otherwise, puts "includeSpouse" as false in newValues.
 	* <p>
 	* if ministryChange input is given, puts that in newValues.
 	* <p>
 	* if statusChange input is given, then ...<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if the length of statusOther input is longer than 0 characters, puts "jobStatus" as statusOther input in newValues<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;otherwise, puts "jobStatus" as statusMenu input in newValues
 	* <p>
 	* for the following, if the input field for the field change is given (is not null), saves the new field value ...<br>
 	* region (for example, regionChange and newRegion inputs)
 	* lane
 	* position
 	* <p>
 	* if the input for teamChange is given, saves the teamId to newValues as well as, passes that value to {@link HRTool#getTeamName(String) getTeamName} in HRTool to get/save the team name to newValues
 	* <p>
 		* if jobChange input is given, then ...<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if newJob input equals "Other", puts "title" as otherJob input in newValues<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;otherwise, puts "title" as newJob input in newValues
 	* <p>
 	* puts changeType as jobChange<br>
 	* if spouse input is not null, then sets action as "jobChangeConfirm&spouse=true",<br>
 	* otherwise, sets action as "jobChangeConfirm".<br>
 	* puts newValues into session object as "formData"<br>
 	* goes to formConfirm
 	*
 	* @param ctx ActionContext object, out of which we get the session value for "staffInfo" and set the session value for "formData"
 	*/
 	public void jobChangeRequest(ActionContext ctx) {
 		try {
 			Hashtable staff =
 				(Hashtable) (ctx.getSession().getAttribute("staffInfo"));
 			String staffID = (String) staff.get("AccountNo");
 			log.debug("top staffInfo accountno is " + staffID);
 			log.debug(
 				"top getteamlist session accountNo is "
 					+ ctx.getSessionValue("accountNo"));
 			
 			//Check to make sure it's a Campus Ministry related change
 			if ( !((String)staff.get("Ministry")).equals("Campus Ministry") &&
 					( ctx.getInputString("ministryChange") == null ||
 						((ctx.getInputString("ministryChange") != null) && !(ctx.getInputString("newMinistry").equals("Campus Ministry"))) ) ) {
 				throw new NonCMJobChangeException();
 			}
 
 			Hashtable newValues = new Hashtable();
 			String startDate =
 				ctx.getInputString("jobChangeMonth")
 					+ "/"
 					+ ctx.getInputString("jobChangeDay")
 					+ "/"
 					+ ctx.getInputString("jobChangeYear");
 			newValues.put("effectiveDate", startDate);
 			if (ctx.getInputString("includeSpouse") != null) {
 				newValues.put("includeSpouse", new Boolean(true));
 			} else {
 				newValues.put("includeSpouse", new Boolean(false));
 			}
 			if (ctx.getInputString("ministryChange") != null) {
 				newValues.put("ministry", ctx.getInputString("newMinistry"));
 			}
 			if (ctx.getInputString("statusChange") != null) {
 				if (ctx.getInputString("statusOther").length() > 0) {
 					newValues.put(
 						"jobStatus",
 						ctx.getInputString("statusOther"));
 				} else {
 					newValues.put(
 						"jobStatus",
 						ctx.getInputString("statusMenu"));
 				}
 			}
 			if (ctx.getInputString("regionChange") != null) {
 				newValues.put("region", ctx.getInputString("newRegion"));
 			}
 			if (ctx.getInputString("laneChange") != null) {
 				newValues.put("strategy", ctx.getInputString("newLane"));
 			}
 			if (ctx.getInputString("locationChange") != null) {
 				newValues.put("location", ctx.getInputString("newLocation"));
 			}
 			if (ctx.getInputString("positionChange") != null) {
 				newValues.put("position", ctx.getInputString("newPosition"));
 			}
 			if (ctx.getInputString("teamChange") != null) {
 				newValues.put("teamID", ctx.getInputString("newTeam"));
 				HRTool hrt = new HRTool();
 				newValues.put(
 					"teamName",
 					hrt.getTeamName(ctx.getInputString("newTeam")));
 			}
 			if (ctx.getInputString("jobChange") != null) {
 				if (ctx.getInputString("newJob").equals("Other")) {
 					newValues.put("title", ctx.getInputString("otherJob"));
 				} else {
 					newValues.put("title", ctx.getInputString("newJob"));
 				}
 			}
 			newValues.put("changeType", StaffChangeRequest.JOB_CHANGE);
 			if (ctx.getInputString("spouse") != null) {
 				newValues.put("action", "jobChangeConfirm&spouse=true");
 			} else {
 				newValues.put("action", "jobChangeConfirm");
 			}
 			log.debug("bottom staffInfo accountno is " + staffID);
 			log.debug(
 				"bottom getteamlist session accountNo is "
 					+ ctx.getSessionValue("accountNo"));
 
 			ctx.setSessionValue("formData", newValues);
 			ctx.goToView("formConfirm");
 		} catch (NonCMJobChangeException e) {
			log.error(e.getMessage(), e);
 			ActionResults ar = new ActionResults();
 			ar.putValue("exceptionText", "This form is only for job information changes involving the Campus Ministry.  " +
 					"If you currently are in the Campus Ministry, please indicate that on the first page of this form.  " +
 					"If you are not in the Campus Ministry, please contact Staff Services or fill out and mail the printable " +
 					"form located here: <A HREF=\"https://staff2.ccci.org/ssfiles/pdf/ACOS.pdf\" target='_blank'>Administrative Change of Status Form.</A>");
 			ctx.setReturnValue(ar);
 			ctx.goToView("error");
 		} catch (Exception e) {
 			log.error(e.getMessage(), e);
 			ActionResults ar = new ActionResults();
 			ar.putValue("exceptionText", e.toString());
 			ctx.setReturnValue(ar);
 			ctx.goToView("error");
 		}
 	}
 	/**
 	* gets many input strings from the request object (via the ActionContext) and saves them to a local hashtable - newValues - and puts that hashtable in the session as "formData".&nbsp;sends the user to the formConfirm page. 
 	* <p>
 	* if the input value for "leaveBeginMonth" is longer than 0 characters, puts inputs "leaveBeginMonth"/"leaveBeginDay"/"leaveBeginYear" in newValues
 	* <p>
 	* <p>
 	* if the input value for "leaveEndMonth" is longer than 0 characters, puts inputs "leaveEndMonth"/"leaveEndDay"/"leaveEndYear" in newValues
 	* <p>
 	* if "leaveReason" input is not given (is null), then sets the local variable reason as "No Reason given"<br>
 	* otherwise, if "leaveReason" input equals "thirtydays", then ...<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;if "leave1Paid" input is "true", then sets the local variable reason as "30 days paid"<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;otherwise, sets the local variable reason as "30 days unpaid"<br>
 	* otherwise, if "leaveReason" input equals "admin", then ...<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;if "leave2Paid" input is "true", then sets the local variable reason as "Paid admin"<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;otherwise, sets the local variable reason as "Unpaid admin"<br>
 	* otherwise, sets the local variable reason equal to the "leaveReason" input
 	* <p>
 	* puts reason in newValues
 	* <p>
 	* if "letter" input does not equal nothing (empty string), puts "letter" input in newValues
 	* <p>
 	* if "includeSpouse" input is given, puts "includeSpouse" as "yes" in newValues
 	* <p>
 	* puts changeType as leaveofabsense and action as "leaveOfAbsenceConfirm"<br>
 	* puts newValues into session object as "formData"<br>
 	* goes to formConfirm
 	*
 	* @param ctx ActionContext object, out of which we get the session value for "staffInfo" and set the session value for "formData"
 	*/
 	public void leaveOfAbsence(ActionContext ctx) {
 		try {
 			Hashtable newValues = new Hashtable();
 			if (ctx.getInputString("leaveBeginMonth").length() > 0) {
 				String beginDate =
 					ctx.getInputString("leaveBeginMonth")
 						+ "/"
 						+ ctx.getInputString("leaveBeginDay")
 						+ "/"
 						+ ctx.getInputString("leaveBeginYear");
 				newValues.put("beginDate", beginDate);
 			}
 			if (ctx.getInputString("leaveEndMonth").length() > 0) {
 				String endDate =
 					ctx.getInputString("leaveEndMonth")
 						+ "/"
 						+ ctx.getInputString("leaveEndDay")
 						+ "/"
 						+ ctx.getInputString("leaveEndYear");
 				newValues.put("endDate", endDate);
 			}
 			String reason;
 			if (ctx.getInputString("leaveReason") == null) {
 				reason = "No Reason given";
 			} else if (
 				ctx.getInputString("leaveReason").equals("thirtydays")) {
 				if (ctx.getInputString("leave1Paid").equals("true")) {
 					reason = "30 day paid";
 				} else {
 					reason = "30 day unpaid";
 				}
 			} else if (ctx.getInputString("leaveReason").equals("admin")) {
 				if (ctx.getInputString("leave2Paid").equals("true")) {
 					reason = "Paid admin";
 				} else {
 					reason = "Unpaid admin";
 				}
 			} else {
 				reason = ctx.getInputString("leaveReason");
 			}
 
 			if (!ctx.getInputString("letter").equals("")) {
 				newValues.put("loaNote", ctx.getInputString("letter"));
 			}
 			if (ctx.getInputString("includeSpouse") != null) {
 				newValues.put("includeSpouse", "yes");
 			}
 			newValues.put("leaveReason", reason);
 			newValues.put("changeType", StaffChangeRequest.LEAVE_OF_ABSENCE);
 			newValues.put("action", "leaveOfAbsenceConfirm");
 			ctx.setSessionValue("formData", newValues);
 			ctx.goToView("formConfirm");
 		} catch (Exception e) {
 			ActionResults ar = new ActionResults();
 			ar.putValue("exceptionText", e.toString());
 			ctx.setReturnValue(ar);
 			ctx.goToView("error");
 		}
 	}
 	/**
 	* saves this change to the database and makes appropriate approvals/field changes/notifications where necessary.&nbsp;takes the "formData" hashtable out of the request and sends it to the leaveOfAbsence in the HRTool class to process the change request &nbsp;then sends the user to the postConfirm method.
 	* <p>
 	* gets "staffInfo" hashtable out of session<br>
 	* out of staff info, gets the account number of this staff and the staff who requested this change<br>
 	* <p>
 	* gets "formData" out of session<br>
 	* passes the staffID, the requestedBy id, and the formData hashtable to the {@link HRTool#leaveOfAbsence(String, String, Map) leaveOfAbsence} method in the HRTool object
 	* <p>
 	* passes the value of hrInitiated from the session as well as the ActionContext to the {@link #postConfirm(String, Controller.ActionContext) postConfirm} method
 	*
 	* @param ctx ActionContext, gets "staffInfo" and "formData" and "hrInitiated" out of the session
 	*/
 	public void leaveOfAbsenceConfirm(ActionContext ctx) {
 		try {
 			Hashtable staff =
 				(Hashtable) (ctx.getSession().getAttribute("staffInfo"));
 			String staffID = (String) staff.get("AccountNo");
 			String requestedBy = (String) staff.get("RequestedBy");
 			if (requestedBy == null || requestedBy.equals("")) {
 				log.debug("staff doing self.");
 				requestedBy = staffID;
 			} else {
 				log.debug("HR doing a staff.");
 			}
 			Hashtable newValues =
 				(Hashtable) ctx.getSession().getAttribute("formData");
 			HRTool hrt = new HRTool();
 			hrt.leaveOfAbsence(staffID, requestedBy, newValues);
 			postConfirm((String) ctx.getSession().getAttribute("hrInitiated"), ctx);
 		} catch (AuthorizerNotFoundException e) {
 			log.error(e.getMessage(), e);
 			ActionResults ar = new ActionResults();
 			ar.putValue("exceptionText", "Your request has been submitted to your regional HR team for approval.  " +
 				"Unfortunately, the HR Regional Director is unable to receive an email notification of this request because there is no HRRD set up in the computer system for the " + e.getAuthRegion() + " region.  " +
 				"Contact this regional office to inform them of the problem and make sure they are aware of your pending request.");
 //			ar.putValue("exceptionText", "This change requires the approval of the " + e.getAuthRegion() + " Region " + e.getAuthRole() + ".  " +
 //				"However, our database does not know who this person is.  An email has already been sent notifying the help desk of this problem.");
 			ctx.setReturnValue(ar);
 			ctx.goToView("notice");
 		} catch (BadRegionException e) {
 			log.error(e.getMessage(), e);
 			ActionResults ar = new ActionResults();
 			ar.putValue("exceptionText", "Your current region is not recognized.");
 			ctx.setReturnValue(ar);
 			ctx.goToView("error");
 		} catch (Exception e) {
 			ActionResults ar = new ActionResults();
 			ar.putValue("exceptionText", e.toString());
 			ctx.setReturnValue(ar);
 			ctx.goToView("error");
 		}
 	}
 	/**
 	* saves this change to the database and makes appropriate approvals/field changes/notifications where necessary.&nbsp;takes the "formData" hashtable out of the request and sends it to the maritalStatusChangeRequest in the HRTool class to process the change request &nbsp;then sends the user to the postConfirm method.
 	* <p>
 	* gets "staffInfo" hashtable out of session<br>
 	* out of staff info, gets the account number of this staff and the staff who requested this change<br>
 	* <p>
 	* gets "formData" out of session<br>
 	* passes the staffID, the requestedBy id, and the formData hashtable to the {@link HRTool#maritalStatusChangeRequest(String, String, Map) maritalStatusChangeRequest} method in the HRTool object
 	* <p>
 	* passes the value of hrInitiated from the session as well as the ActionContext to the {@link #postConfirm(String, Controller.ActionContext) postConfirm} method
 	*
 	* @param ctx ActionContext, gets "staffInfo" and "formData" and "hrInitiated" out of the session
 	*/
 	public void maritalStatusChangeConfirm(ActionContext ctx) {
 		try {
 			Hashtable staff =
 				(Hashtable) (ctx.getSession().getAttribute("staffInfo"));
 			String staffID = (String) staff.get("AccountNo");
 			String requestedBy = (String) staff.get("RequestedBy");
 			if (staffID == null) {
 				// not staff
 				return;
 			}
 			if (requestedBy == null || requestedBy.equals("")) {
 				log.debug("staff doing self.");
 				requestedBy = staffID;
 			} else {
 				log.debug("HR doing a staff.");
 			}
 			Hashtable newValues =
 				(Hashtable) ctx.getSession().getAttribute("formData");
 			newValues.put("AccountNo", staffID);
 			newValues.put("FirstName", staff.get("FirstName"));
 			newValues.put("LastName", staff.get("LastName"));
 			HRTool hrt = new HRTool();
 			hrt.maritalStatusChangeRequest(staffID, requestedBy, newValues);
 			postConfirm((String) ctx.getSession().getAttribute("hrInitiated"), ctx);
 		} catch (Exception e) {
 			ActionResults ar = new ActionResults();
 			ar.putValue("exceptionText", e.toString());
 			ctx.setReturnValue(ar);
 			ctx.goToView("error");
 		}
 	}
 	/**
 	* gets many input strings from the request object (via the ActionContext) and saves them to a local hashtable - newValues - and puts that hashtable in the session as "formData".&nbsp;sends the user to the formConfirm page. 
 	* <p>
 	* gets the following inputs through the ActionContext:<br>
 	* fianceeFirst<br>
 	* wifeFirst<br>
 	* fianceeMiddle<br>
 	* wifeMiddle<br>
 	* wifeLast<br>
 	* fianceeLast<br>
 	* weddingMonth<br>
 	* weddingDay<br>
 	* weddingYear<br>
 	* fianceeIsUs<br>
 	* fianceeAccountNo<br>
 	* fianceeStaffMonth<br>
 	* fianceeStaffDay<br>
 	* fianceeStaffYear<br>
 	* fianceeIsNat<br>
 	* joinNat<br>
 	* <p>
 	* gets "staffInfo" hashtable out of session through <code>ctx</code> to compare the input with what this person's staff record already has.
 	* <p>
 	* if this person is male, then ...
 	* &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if the length of the "fianceeFirst" input is not 0 characters, gets that as the spouseFirstName, otherwise use the "wifeFirst" input<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;puts that name in our newValues hashtable as the spouseFirstName and fianceeFirstName<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;repeats with middle name<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;puts the input for "wifeLast" and "fianceeLast" in our newValues hashtable accordingly<br>
 	* if this person is female, then ...<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;puts the input for "fianceeFirst", "fianceeMiddle", and"fianceeLast" in our newValues hashtable as spouse and fiancee names.<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;puts the input for "wifeFirst", "wifeMiddle", and "wifeLast" in our newValues hashtable as the person's own name<br>
 	* <p>
 	* if the length of the input for "weddingMonth" is more than 0 characters, then it puts into our newValues hashtable the inputs "weddingMonth"/"weddingDay"/"weddingYear"
 	* <p>
 	* if they provided input for "fianceeIsUs", then ...<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;puts that value in our newValues hashtable<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if "fianceeIsUs" is true, then ...<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;puts the "fianceeAccountNo" in as the spouseAccountNo in newValues<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;otherwise, puts the stores the fianceeJoinStaffDate with the input "fianceeStaffMonth"/"fianceeStaffDay"/"fianceeStaffYear"
 	* <p>
 	* if they provided input for "fianceeIsNat", then stores that in newValues
 	* <p>
 	* if they provided input for "joinNat", then stores that in newValues
 	* <p>
 	* stores in newValues that the changetype is a marital status change and that the action is maritalStatusChangeConfirm
 	* <p>
 	* stores newValues in the session as "formData"
 	* <p>
 	* goes to formConfirm
 	* 
 	* @param ctx ActionContext, gets "staffInfo" out of the session, puts "formData" into the session
 	*/
 	public void maritalStatusChangeRequest(ActionContext ctx) {
 		try {
 			Hashtable staff =
 				(Hashtable) (ctx.getSession().getAttribute("staffInfo"));
 			Hashtable newValues = new Hashtable();
 			newValues.put("maritalStatus", "M");
 			newValues.put("IsMale", staff.get("IsMale"));
 			newValues.put("fianceeLastName",ctx.getInputString("fianceeLast"));
 // if user is male, we needn't worry about name changes
 			if (((Boolean) staff.get("IsMale")).booleanValue()) {
 				String spouseFirstName = (ctx.getInputString("fianceeFirst").length() != 0) ? ctx.getInputString("fianceeFirst") : ctx.getInputString("wifeFirst");
 				String spouseMiddleName = (ctx.getInputString("fianceeMiddle").length() != 0) ? ctx.getInputString("fianceeMiddle") : ctx.getInputString("wifeMiddle");
 				String spouseLastName = ctx.getInputString("wifeLast");
 				newValues.put("spouseFirstName", spouseFirstName);
 				newValues.put("fianceeFirstName", spouseFirstName);
 				newValues.put("spouseMiddleName", spouseMiddleName);
 				newValues.put("fianceeMiddleName", spouseMiddleName);
 				newValues.put("spouseLastName", spouseLastName);
 			} else {
 				newValues.put("spouseFirstName", ctx.getInputString("fianceeFirst"));
 				newValues.put("spouseMiddleName", ctx.getInputString("fianceeMiddle"));
 				newValues.put("spouseLastName", ctx.getInputString("fianceeLast"));
 				newValues.put("fianceeFirstName", ctx.getInputString("fianceeFirst"));
 				newValues.put("fianceeMiddleName", ctx.getInputString("fianceeMiddle"));
 				newValues.put("lastName", ctx.getInputString("wifeLast"));
 				newValues.put("middleName", ctx.getInputString("wifeMiddle"));
 				newValues.put("firstName", ctx.getInputString("wifeFirst"));
 			}
 			if (ctx.getInputString("weddingMonth").length() != 0) {
 				String weddingDate = ctx.getInputString("weddingMonth") + "/" + ctx.getInputString("weddingDay") + "/" + ctx.getInputString("weddingYear");
 				newValues.put("marriageDate", weddingDate);
 			}
 			if (ctx.getInputString("fianceeIsUs") != null) {
 				newValues.put("isFianceeStaff", ctx.getInputString("fianceeIsUs"));
 				// Form suggests that if fiancee is not currently staff, fiancee WILL become staff.
 				if (ctx.getInputString("fianceeIsUs").equals("true")) {
 					newValues.put("fianceeAccountNo", ctx.getInputString("fianceeAccountNo"));
 					if (((Boolean) staff.get("IsMale")).booleanValue()) {
 						newValues.put("spouseAccountNo", staff.get("AccountNo")+"S");
 					} else {
 						newValues.put("spouseAccountNo", ctx.getInputString("fianceeAccountNo"));
 						newValues.put("accountNo", ctx.getInputString("fianceeAccountNo")+"S");					
 					}
 				} else {
 					String fianceeJoinStaffDate = ctx.getInputString("fianceeStaffMonth") + "/" + ctx.getInputString("fianceeStaffDay") + "/" + ctx.getInputString("fianceeStaffYear");
 					newValues.put("fianceeJoinStaffDate", fianceeJoinStaffDate);
 				}
 			}
 			if (ctx.getInputString("fianceeIsNat") != null) {
 				newValues.put("isFianceeJoiningNS", ctx.getInputString("fianceeIsNat"));
 			}
 			if (ctx.getInputString("joinNat") != null) {
 				newValues.put("joiningNS", ctx.getInputString("joinNat"));
 			}
 			newValues.put("changeType", StaffChangeRequest.MARITAL_STATUS_CHANGE);
 			newValues.put("action", "maritalStatusChangeConfirm");
 			ctx.setSessionValue("formData", newValues);
 			ctx.goToView("formConfirm");
 		} catch (Exception e) {
 			ActionResults ar = new ActionResults();
 			ar.putValue("exceptionText", e.toString());
 			ctx.setReturnValue(ar);
 			ctx.goToView("error");
 		}
 	}
 	/**
 	* to be called after a change request confirmation has been made.&nbsp;Sends the user to whichever page they should go back to (tools page, or index page)
 	* <p>
 	* if hrInitiated is not null and it equals "true", then set the session attribute "accountNo", to be the account number in the profile, and redirect the user to the {@link StaffController#showTools(Controller.ActionContext) showTools} method in StaffController
 	* <p>
 	* otherwise, set the session attribute of hrInitiated to be "false" and send them to the {@link showFormList(Controller.ActionContext) showFormList} method
 	* 
 	* @param hrInitiated a string indication whether this changerequest was initiated by hr (ACOS) or by the person
 	* @param ctx the ActionContext object out of which to get the profile, and session, and redirect the response
 	*/
 	private void postConfirm(String hrInitiated, ActionContext ctx)
 		throws Exception {
 		if (hrInitiated != null && hrInitiated.equals("true")) {
 			log.debug(
 				"setting session accountNo to "
 					+ ctx.getProfile().get("AccountNo"));
 			ctx.setSessionValue("accountNo", ctx.getProfile().get("AccountNo"));
 //			ctx.goToView("success");
 			ctx.getResponse().sendRedirect(
 				"/servlet/StaffController?action=showTools");
 		} else {
 			ctx.setSessionValue("hrInitiated", "false");
 			ctx.goToView("success");
 //			ctx.getResponse().sendRedirect(
 //				"/servlet/HRUpdateController?action=showFormList");
 		}
 		
 	}
 	/**
 	* iterates through the newValues hashtable and for every key it prints out form data to System.out in the form of <code>"newValues.put(\"" + key + "\", \"" + newValues.get(key) + "\");"</code>
 	* 
 	* @param newValues the hashtable that is to be printed out
 	*/
 	private void printFormData(Hashtable newValues) {
 		for (Enumeration e = newValues.keys(); e.hasMoreElements();) {
 			String key = (String) e.nextElement();
 			log.debug(
 				"newValues.put(\""
 					+ key
 					+ "\", \""
 					+ newValues.get(key)
 					+ "\");");
 		}
 	}
 	/**
 	* gets many input strings from the request object (via the ActionContext) and saves them to a local hashtable - newValues - and puts that hashtable in the session as "formData".&nbsp;sends the user to the formConfirm page. 
 	* <p>
 	* if the input value for "resignMonth" is longer than 0 characters, stores strLastDay as inputs "resignMonth"/"resignDay"/"resignYear"<br>
 	* puts strLastDay in newValues as resignationDate
 	* <p>
 	* if the input value for "resignReasion" doesn't exist (is null), then sets the local variable reason to equal "None given".<br>
 	* otherwise, if the input value for "resignReason" has "otherReason" anywhere in it, then sets the local variable reason to equal the input for resignReason plus the input for otherReason<br>
 	* otherwise, sets the local variable reason to be equal to the resignReason input
 	* <p>
 	* puts that reason in newValues
 	* <p>
 	* puts the "serverance" input in newValues 
 	* <p>
 	* if "keepSupport" input is given, puts that in newValues
 	* <p>
 	* if "contribute" input equals "Yes", puts input for "receiveName" and "receiveNumber" in newValues
 	* <p>
 	* if "letter" input does not equal nothing (empty string), puts "letter" input in newValues
 	* <p>
 	* puts changeType as resignation and action as "resignationConfirm"<br>
 	* puts newValues into session object as "formData"<br>
 	* goes to formConfirm
 	*
 	* @param ctx ActionContext object, out of which we get the session value for "staffInfo" and set the session value for "formData"
 	*/
 	public void resignation(ActionContext ctx) {
 		try {
 			Hashtable newValues = new Hashtable();
 			if (ctx.getInputString("resignMonth").length() > 0) {
 				String strLastDay =
 					ctx.getInputString("resignMonth")
 						+ "/"
 						+ ctx.getInputString("resignDay")
 						+ "/"
 						+ ctx.getInputString("resignYear");
 				newValues.put("resignationDate", strLastDay);
 			}
 			String reason;
 			if (ctx.getInputString("resignReason") == null) {
 				reason = "None given";
 			} else if (ctx.getInputString("resignReason").equals("OtherReason")) {
 				reason =
 					ctx.getInputString("resignReason") + ": "
 						+ ctx.getInputString("otherReason");
 			} else {
 				reason = ctx.getInputString("resignReason");
 			}
 			newValues.put("resignReason", reason);
 			newValues.put("severanceRequest", ctx.getInputString("severance"));
 			if (ctx.getInputString("keepSupport") != null) {
 				newValues.put("keepSupport", ctx.getInputString("keepSupport"));
 			}
 			if (ctx.getInputString("contribute").equals("Yes")) {
 				newValues.put(
 					"anotherAccountName",
 					ctx.getInputString("receiveName"));
 				newValues.put(
 					"anotherAccountNumber",
 					ctx.getInputString("receiveNumber"));
 			}
 			if (!ctx.getInputString("letter").equals("")) {
 				newValues.put(
 					"resignationLetter",
 					ctx.getInputString("letter"));
 			}
 			
 			if(ctx.getInputString("newRegion")!=null && !ctx.getInputString("newRegion").equals(""))
 			{
 				newValues.put("newRegion",ctx.getInputString("newRegion"));
 			}
 			newValues.put("changeType", StaffChangeRequest.RESIGNATION);
 			newValues.put("action", "resignationConfirm");
 			ctx.setSessionValue("formData", newValues);
 			ctx.goToView("formConfirm");
 		} catch (Exception e) {
 			ActionResults ar = new ActionResults();
 			ar.putValue("exceptionText", e.toString());
 			ctx.setReturnValue(ar);
 			ctx.goToView("error");
 		}
 	}
 	/**
 	* saves this change to the database and makes appropriate approvals/field changes/notifications where necessary.&nbsp;takes the "formData" hashtable out of the request and sends it to resignation in the HRTool class to process the change request &nbsp;then sends the user to the postConfirm method.
 	* <p>
 	* gets "staffInfo" hashtable out of session<br>
 	* out of staff info, gets the account number of this staff and the staff who requested this change<br>
 	* <p>
 	* gets "formData" out of session<br>
 	* passes the staffID, the requestedBy id, and the formData hashtable to the {@link HRTool#resignation(String, String, Map) resignation} method in the HRTool object
 	* <p>
 	* passes the value of hrInitiated from the session as well as the ActionContext to the {@link #postConfirm(String, Controller.ActionContext) postConfirm} method
 	*
 	* @param ctx ActionContext, gets "staffInfo" and "formData" and "hrInitiated" out of the session
 	*/
 	public void resignationConfirm(ActionContext ctx) {
 		try {						
 			Hashtable staff =
 				(Hashtable) (ctx.getSession().getAttribute("staffInfo"));
 			String staffID = (String) staff.get("AccountNo");
 			String requestedBy = (String) staff.get("RequestedBy");
 			if (requestedBy == null || requestedBy.equals("")) {
 				log.debug("staff doing self.");
 				requestedBy = staffID;
 			} else {
 				log.debug("HR doing a staff.");
 			}
 			Hashtable newValues =
 				(Hashtable) ctx.getSession().getAttribute("formData");
 			newValues.put("accountNo", staffID);
 			
 			/*
 			if(newValues.get("newRegion")==null || newValues.get("newRegion").equals(""))
 			{
 				// set newRegion in  newValues to region from staff record
 				newValues.put("newRegion",(String) staff.get("Region"));
 			}
 			*/
 			
 			HRTool hrt = new HRTool();
 			hrt.resignation(staffID, requestedBy, newValues);
 			postConfirm((String) ctx.getSession().getAttribute("hrInitiated"), ctx);
 		} catch (AuthorizerNotFoundException e) {
 			log.error(e.getMessage(), e);
 			ActionResults ar = new ActionResults();
 			ar.putValue("exceptionText", "Your request has been submitted to your regional HR team for approval.  " +
 				"Unfortunately, the HR Regional Director is unable to receive an email notification of this request because there is no HRRD set up in the computer system for the " + e.getAuthRegion() + " region.  " +
 				"Contact this regional office to inform them of the problem and make sure they are aware of your pending request.");
 //			ar.putValue("exceptionText", "This change requires the approval of the " + e.getAuthRegion() + " Region " + e.getAuthRole() + ".  " +
 //				"However, our database does not know who this person is.  An email has already been sent notifying the help desk of this problem.");
 			ctx.setReturnValue(ar);
 			ctx.goToView("notice");
 		} catch (BadRegionException e) {
 			log.error(e.getMessage(), e);
 			ActionResults ar = new ActionResults();
 			ar.putValue("exceptionText", "Your current region is not recognized.");
 			ctx.setReturnValue(ar);
 			ctx.goToView("error");
 		} catch (Exception e) {
 			log.error(e.getMessage(), e);
 			ActionResults ar = new ActionResults();
 			ar.putValue("exceptionText", e.toString());
 			ctx.setReturnValue(ar);
 			ctx.goToView("error");
 		}
 	}
 
 	/**
 	* Out of the session, gets the profile, whose info is to be changed, and who's requesting the change.&nbsp;Uses that info to get a hashtable representation of the staff to be changed and puts it in the session.&nbsp;Sends the user to the index page.
 	* <p>
 	* Checks to see if user's profile exists in the ActionContext.<br>
 	* if so, get the account number out of the profile
 	* <p>
 	* Checks to see if user's account number is in the session.<br>
 	* if so, get it,<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;remove the account number from the session.<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;and is someone requesting this?<br>
 	* &nbsp;&nbsp;&nbsp;&nbsp;if so, get that account number.
 	* <p>
 	* By this point, the account number of who is to be changed hasn't been set, throws an exception,<br>
 	* otherwise, calls {@link HRTool#getUserData(Hashtable, String, String) getUserData}
 	* in the {@link HRTool} object with the profile, the account number, and the requester's account number.
 	* <p>
 	* Sets the session value "staffInfo" to be the hashtable that is returned.
 	* <p>
 	* Goes to index
 	* 
 	* @param ctx ActionContext.  out of the ActionContext, we {@link Controller.ActionContext#getProfile() getProfile()} and we get "accountNo" and "requestedBy" out of the session as well as remove "accountNo" from the session and set "staffInfo"
 	*/
 	public void showFormList(ActionContext ctx) {
 		String acctNo = null;
 		String requestedBy = null;
 		HRTool hrt = new HRTool();
 		if (ctx.getProfile() != null) {
 			acctNo = (String) ctx.getProfile().get("AccountNo");
 		}
 		if (ctx.getSession().getAttribute("accountNo") != null) {
 			acctNo = (String) ctx.getSession().getAttribute("accountNo");
 			ctx.getSession().removeAttribute("accountNo");
 			if (ctx.getSession().getAttribute("requestedBy") != null) {
 				requestedBy = (String) ctx.getSession().getAttribute("requestedBy");
 			} else {
 				log.info("requestedBy is not in the session");
 			}
 		}
 		try {
 			if (acctNo == null || acctNo.equals("") ) {
 				log.info("accountno is null");
 				throw new AccountNumberNullException("HRUpdateController:showFormList:Account number is null");
 			} else {
 				log.debug("before getting user data");
 				ctx.setSessionValue("staffInfo", hrt.getUserData(ctx.getProfile(), acctNo, requestedBy));
 				log.debug("after getting user data");
 			}
 			ctx.goToView("index");
 		} catch (AccountNumberNullException e) {
 			log.error("AccountNumberNullException thrown.", e);
 			ActionResults ar = new ActionResults();
 			ar.putValue("exceptionText","Your Account Number is not available in your Staff Site profile.  " +
 					"Please email <a href=\"mailto:help@campuscrusadeforchrist.com\">help@campuscrusadeforchrist.com</a> " +
 					"with this error message and your account number.");
 			ctx.setReturnValue(ar);
 			ctx.goToView("error");
 		} catch (Exception e) {
 			log.error(e.getMessage(), e);
 			ActionResults ar = new ActionResults();
 			ar.putValue("exceptionText","HRUpdateController:showFormList:" + e.toString());
 			ctx.setReturnValue(ar);
 			ctx.goToView("error");
 		}
 	}
 }
