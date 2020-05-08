 package org.alt60m.hr.si.servlet.dbio;
 
 import org.alt60m.servlet.*;
 //import org.alt60m.wsn.sp.model.dbio.WsnApplication;
 //import org.alt60m.wsn.sp.model.dbio.WsnProject;
 import org.alt60m.hr.si.bean.dbio.SIInfoBean;
 import org.alt60m.hr.si.model.dbio.*;
 import org.alt60m.html.FormHelper;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.io.FileWriter;
 import java.io.IOException;
 
 //This class handles all of the STINT/Internship Administration related actions
 public class SIAdminHandler {
 
 	private static Log log = LogFactory.getLog(SIAdminHandler.class);
 	boolean debug = true;
 	ActionResults ar = new ActionResults();
 	SIInfoBean siBean = new SIInfoBean();
 
     public static final String PROJECTCLASS = "org.alt60m.hr.si.model.dbio.SIProject";
 
 	/*
       All actionhandler methods should return an ActionResults object after
       having set the view using setView("viewname")
      
       for example:
     
       protected ActionResults processSomething(Action action)
       {
         ActionResults ar = new ActionResults();
          //do something ...
         ar.setView("home");
         return ar;
       }
       
     */
 /*	protected ActionResults showSIAdminTool(Action action){
 		boolean showAddProjectLink = false;
 		boolean showAddAdministratorLink = false;
 		boolean showListProjectsLink = false;
 		boolean showAdministrateProjectsLink = false;
 		try {
 			int role = getRole(action);
 			/* ONLY VALID ROLES AT THIS POINT ARE 0, 1, 2, 3, 4, 5, 6 !!! */
 /*			if (role < 2 || role > 6) { // not authorized to administrate SI Projects
 				ar.putValue("ErrorMessage","This system could not recognize you as having a current STINT/Internship administrative role. You must be entered in this system in that capacity in order to use this tool.");
 			} else {
 				if (role == 3 || role == 4) { // functionally equivalent to summer project APD(3) and PD(4)
 					showListProjectsLink = true;
 					showAdministrateProjectsLink = true;
 				} else if (role == 5) { // Not sure of the name for this role
 					/* This role can add/edit projects, administrate projects,
 					and add administrators/evaluators with roles up to "4" */
 					/* I think this is a regional-coordinator-type person. */
 /*					showAddProjectLink = true;
 					showAddAdministratorLink = true;
 					showListProjectsLink = true;
 					showAdministrateProjectsLink = true;
 				} else if (role == 6) { // Uber-user
 					/* can add administrators/evaluators with roles up to "5", add/edit projects,
 					and administrate projects. */
 /*					showAddProjectLink = true;
 					showAddAdministratorLink = true;
 					showListProjectsLink = true;
 					showAdministrateProjectsLink = true;
 				}
 				ar.putValue("showAddProjectLink", (new Boolean(showAddProjectLink)).toString());
 				ar.putValue("showAddAdministratorLink", (new Boolean(showAddAdministratorLink)).toString());
 				ar.putValue("showListProjectsLink", (new Boolean(showListProjectsLink)).toString());
 				ar.putValue("showAdministrateProjectsLink", (new Boolean(showAdministrateProjectsLink)).toString());
 				ar.putValue("role", Integer.toString(role));
 			}
 			ar.setView("adminhome");
 		} catch (Exception e) {
 			log.error("Exception encountered in SIAdminHandler.showSIAdminTool(): "+e);
 			ar.setView("tools");
 		}
 		return ar;
 	}
 */
 	// Just takes the user to the location editing screen if they have the correct role.
 	protected ActionResults adminEditProject(Action action) {
 		String SIProjectID = (String)action.getValue("SIProjectID");
 		if ((SIProjectID != null) && !(SIProjectID.trim().equals(""))) {
 			ar.putValue("SIProjectID", SIProjectID);
 		}
 		ar.setView("adminEditProject");
 		return ar;
 	}
 
 	// Saves the project that has been edited.
 	public ActionResults adminSaveProject(Action action) {
 		try {
 			String SIProjectID = (String)action.getValue("SIProjectID");
 			if (SIProjectID == null) {
 				SIProjectID = "new";
 			}
 			action.putValue("lastChangedDate", org.alt60m.html.Util.formatDate(new Date()));
 			SIUtil.saveObjectHash(action.getValues(), SIProjectID, "SIProjectID", SIProjectHandler.PROJECTCLASS);
 			ar.putValue("ErrorMessage", "Your STINT / Internship location was successfully added to the database.");
 			ar.setView("addProjectConfirm");
 		} catch (Exception e) {
 			ar.putValue("ErrorMessage", "Internal Error saving STINT/Internship Location Info: " + e.getMessage());
 		}
 		return ar;
 	}
 
 	public ActionResults adminListProjects(Action action) {
 		Hashtable h = new Hashtable();
 		FormHelper helper = new FormHelper();
 		try {
 			String whereClause = "";
 			String Region = (String)action.getValue("Region");
 			String AOA = (String)action.getValue("AOA");
 
 			if (Region!=null){
 				if (Region.equals(" ")){
 					Region = null;
 				}
 			}
 			if (AOA!=null){
 				if (AOA.equals(" ")){
 					AOA = null;
 				}
 			}
 
 			whereClause=whereClause + " NOT(name like \'Unassi%\')";		//don't show the 'Unassigned' Project
 
 			if ((action.getValue("City")!=null)&&(!((String)action.getValue("City")).equals(""))){
 				whereClause=whereClause + " AND UPPER(city) like \'" + ((String)action.getValue("City")).toUpperCase() + "%\'";
 			}
 			if ((action.getValue("Name")!=null)&&(!((String)action.getValue("Name")).equals(""))){
 				whereClause=whereClause + " AND UPPER(name) like \'" + ((String)action.getValue("Name")).toUpperCase() + "%\'";
 			}
 			if ((action.getValue("Country")!=null)&&(!((String)action.getValue("Country")).equals(""))){
 				whereClause=whereClause + " AND country like \'" + (String)action.getValue("Country") + "%\'";
 			}
 			if (Region!=null){
 				whereClause=whereClause + " AND partnershipRegion = \'" + Region + "\'";
 			}
 			if (AOA!=null){
 				whereClause=whereClause + " AND AOA = \'" + AOA + "\'";
 			}
 			if ((action.getValue("StartDate")!=null)&&(!((String)action.getValue("StartDate")).equals(""))){
 				whereClause=whereClause + " AND studentStartDate like \'" + (String)action.getValue("StartDate") + "%\'";
 			}
 			if ((action.getValue("EndDate")!=null)&&(!((String)action.getValue("EndDate")).equals(""))){
 				whereClause=whereClause + " AND studentEndDate like \'" + (String)action.getValue("EndDate") + "%\'";
 			}
 			if ((action.getValue("Year")!=null)&&(!((String)action.getValue("Year")).equals(""))){
 				whereClause=whereClause + " AND siYear = \'" + (String)action.getValue("Year") + "\'";
 			}
 
 			whereClause=whereClause + " order by name, city";
 //					Iterator staffiter = staff.selectList("isSecure = 'F' AND firstname like '" + firstName + "%' AND lastname like '" + lastName + "%' AND preferredname like '" + preferredName + "%' ORDER BY lastname").iterator();
 			Iterator projects = (new SIProject()).selectList(whereClause).iterator();
 
 			for (int i = 0; projects.hasNext(); i++) {
 				SIProject project = (SIProject) projects.next();
 				Hashtable temp = new Hashtable();
 				temp.put("SIProjectID",new Integer(project.getSIProjectID()).toString());
 				temp.put("Name",helper.value((String)project.getName()));
 				temp.put("City",helper.value((String)project.getCity()));
 				temp.put("Country",helper.value((String)project.getCountry()));
 				temp.put("PartnershipRegion",helper.value((String)project.getPartnershipRegion()));
 				if(project.getStudentStartDate() != null) {
 					temp.put("StartDate",helper.value((new SimpleDateFormat("MM/dd/yyyy")).format( project.getStudentStartDate())));
 				} else {
 					temp.put("StartDate","-");
 				}
 				if(project.getStudentEndDate() != null) {
 					temp.put("StopDate",helper.value((new SimpleDateFormat("MM/dd/yyyy")).format( project.getStudentEndDate())));
 				} else {
 					temp.put("StopDate","-");
 				}
				
				temp.put("AOA",helper.value((String)project.getAOA()));
				
				temp.put("SiYear",helper.value((String)project.getSiYear()));
				
 				String getIsCoordId = project.getFk_isCoord();
 				if (getIsCoordId ==null || getIsCoordId.equals("")) {
 					temp.put("Coord","-");
 				} else {
 					SIPerson coord = new SIPerson(project.getFk_isCoord());
 					temp.put("Coord",helper.value((String)coord.getFirstName())+" "+helper.value((String)coord.getLastName()));
 				}
 				h.put(String.valueOf(i),temp);
 			}
 
 			ar.putObject("tub", h);
 			
 			ar.setView("listProjects");
 		} catch (Exception e) {
 			ar.putValue("ErrorMessage", "Internal Error saving STINT/Internship Location Info: " + e.getMessage());
 			e.printStackTrace();
 		}
 		return ar;
 	}
 
 /*	private int getRole(Action action) {
 		String staffSiteProfileID = (String) action.getValue("loggedIn");
 		int role = 0;
 		if(debug) log.debug("******** SIAdminHandler.getRole(): loggedIn staffSiteProfileID has value: "+staffSiteProfileID);
 		try {
 			SIPerson siAdministrator = SIUtil.getSIAdministrator(staffSiteProfileID);
 			role = Integer.parseInt(siAdministrator.getRole());
 			if(debug) log.debug("******** SIAdminHandler.getRole(): role is: "+role);
 		} catch (Exception e) {
 			log.error("Exception encountered in SIAdminHandler.getRole(): "+e);
 			e.printStackTrace();
 		}
 		return role;
 	}
 */
 	// Just takes the user to the location editing screen if they have the correct role.
 	protected ActionResults postMakeDownload(Action action, String theFile, String httpFile) {
 		log.debug("In postMakeDownload()");
 		try {
 			Hashtable h = new Hashtable();
 
 			// get the parameters from the page
 			String regionID = (String)action.getValue("regionID");
 			String yearID = (String)action.getValue("yearID");
 			String appType = (String)action.getValue("appType");
 
 			// project was found
 			h.put("regionID", regionID);
 			h.put("yearID", yearID);
 			h.put("appType", appType);
 
 			// create the entire output file in memory in a String
 			String s = "";
 		    Collection c = null;
 
 			// setup titles of each fields
 			s += "Title,FirstName,MiddleName,LastName,PreferredName,Gender,Birthdate,SSN,";
 			s += "SpouseFirstName,SpouseMiddleName,SpouseBirthdate,SpouseSSN,";
 			s += "RecentSchools,PreferenceA,PreferenceB,PreferenceC,Year,";
 			s += "CurrentAddress1,CurrentAddress2,CurrentCity,CurrentState,CurrentZip,CurrentWorkPhone,CurrentHomePhone,CurrentCellPhone,CurrentEmail,";
 			s += "PermAddress1,PermAddress2,PermCity,PermState,PermZip,PermWorkPhone,PermHomePhone,PermCellPhone,PermEmail,";
 			s += "EmerContactName,EmerContactRelationship,EmerAddress1,EmerAddress2,EmerCity,EmerState,EmerZip,EmerWorkPhone,EmerHomePhone,EmerCellPhone,EmerEmail,";
 			s += "Emer2ContactName,Emer2ContactRelationship,Emer2Address1,Emer2Address2,Emer2City,Emer2State,Emer2Zip,Emer2WorkPhone,Emer2HomePhone,Emer2CellPhone,Emer2Email,";
 			s += "\n";
 	
 			// 3-28-03 kl: first get the 3 collections to process
 			Collection cCollections = siBean.getAppsCollections(regionID, yearID);
 			Iterator tubIterator = cCollections.iterator();
 			Hashtable tub = (Hashtable)tubIterator.next();
 			log.debug("tub="+ tub.toString()); // trace
 
 
 			// Get the list of applicants based on the appType and RegionID.
 			if (appType.equals("R"))
 				// 3-27-03 kl: change to new method
 				//c = siBean.getAppsReady(regionID);
 				c = (Collection) tub.get("ReadyID");
 			else if (appType.equals("I"))
 				//c = siBean.getAppsInProcess(regionID);
 				c = (Collection) tub.get("InProcessID");
 			else if (appType.equals("S"))
 				//c = siBean.getAppsStarted(regionID);
 				c = (Collection) tub.get("StartedID");
 			Iterator listIterator = c.iterator();
 
 // We need the following fields: Title, First Name, Middle Name, Last Name, Preferred Name, Gender, Birthdate, SSN, Spouse First Name, Spouse Middle Name, Spouse Birthdate, Spouse SSN, Campus, Preference 1, 2, & 3, Year Number, Current Address (street, city, state, zip, phone, cell phone, email), Permanent Address (street, city, state, zip, phone), Emergency Ccontact 1&2 (name, relationship, street, city, state, zip, phone, work phone, email). 
 
 			// Loop thru the applicants and add each one to the output file.
 			while (listIterator.hasNext()) {
 				Hashtable ha = (Hashtable)listIterator.next();
 				String appID = (String) ha.get("ApplicationID");
 
 				// get the Application object
 				SIApplication a = new SIApplication();
 				try {
 					a = siBean.getSIApplication(appID);
 				} catch (Exception e) { /* should never happen, but might in test data */ }
 
 				// get the corresponding person object of the applicant
 				SIPerson p = new SIPerson();
 				try {
 					p = siBean.getSIPerson(a.getFk_PersonIDString());
 				} catch (Exception e) { /* should never happen, but might in test data */ }
 
 				s += "\""+p.getTitle()+"\",";
 				s += "\""+p.getFirstName()+"\",";
 				s += "\""+p.getMiddleName()+"\",";
 				s += "\""+p.getLastName()+"\",";
 				s += "\""+p.getPreferredName()+"\",";
 				s += "\""+p.getGender()+"\",";
 				s += "\"\",";	// birthdate - NOT AVAILABLE
 				s += "\""+a.getSsn()+"\",";
 
 				// spouse info:
 				s += "\"\",";	// spouse first name - NOT AVAILABLE
 				s += "\"\",";	// spouse first name - NOT AVAILABLE
 				s += "\"\",";	// spouse birthdate - NOT AVAILABLE
 				s += "\"\",";	// spouse SSN - NOT AVAILABLE
 
 				// 4-11-03 kl: added code from projectview.jsp to get the names of projects application is applying to:
 				SIProject projA = new SIProject();
 				SIProject projB = new SIProject();
 				SIProject projC = new SIProject();
 				if (a.getLocationA() != null  &&  !a.getLocationA().equals(""))
 					try {
 						projA = siBean.getSIProject(Integer.valueOf( a.getLocationA() ).intValue());
 					} catch (Exception e) { /* should never happen, but might in test data */ }
 				if (a.getLocationB() != null  &&  !a.getLocationB().equals(""))
 					try {
 						projB = siBean.getSIProject(Integer.valueOf( a.getLocationB() ).intValue());
 					} catch (Exception e) { /* should never happen, but might in test data */ }
 				if (a.getLocationC() != null  &&  !a.getLocationC().equals(""))
 					try {
 						projC = siBean.getSIProject(Integer.valueOf( a.getLocationC() ).intValue());
 					} catch (Exception e) { /* should never happen, but might in test data */ }
 
 				// school and location preferences
 				s += "\""+p.getRecentSchools()+"\",";
 				s += "\""+projA.getName()+"\",";
 				s += "\""+projB.getName()+"\",";
 				s += "\""+projC.getName()+"\",";
 				//s += "\""+a.getLocationA()+"\","; //4-11-03 kl: replaced with location names instead of location numbers
 				//s += "\""+a.getLocationB()+"\",";
 				//s += "\""+a.getLocationC()+"\",";
 				s += "\""+a.getSiYear()+"\",";
 
 				// current address:
 				s += "\""+p.getCurrentAddress1()+"\",";
 				s += "\""+p.getCurrentAddress2()+"\",";
 				s += "\""+p.getCurrentCity()+"\",";
 				s += "\""+p.getCurrentState()+"\",";
 				s += "\""+p.getCurrentZip()+"\",";
 				s += "\""+p.getCurrentWorkPhone()+"\",";
 				s += "\""+p.getCurrentHomePhone()+"\",";
 				s += "\""+p.getCurrentCellPhone()+"\",";
 				s += "\""+p.getCurrentEmail()+"\",";
 				
 				s += "\""+p.getPermAddress1()+"\",";
 				s += "\""+p.getPermAddress2()+"\",";
 				s += "\""+p.getPermCity()+"\",";
 				s += "\""+p.getPermState()+"\",";
 				s += "\""+p.getPermZip()+"\",";
 				s += "\""+p.getPermWorkPhone()+"\",";
 				s += "\""+p.getPermHomePhone()+"\",";
 				s += "\""+p.getPermCellPhone()+"\",";
 				s += "\""+p.getPermEmail()+"\",";
 
 				s += "\""+p.getEmerContactName()+"\",";
 				s += "\""+p.getEmerContactRelationship()+"\",";
 				s += "\""+p.getEmerAddress1()+"\",";
 				s += "\""+p.getEmerAddress2()+"\",";
 				s += "\""+p.getEmerCity()+"\",";
 				s += "\""+p.getEmerState()+"\",";
 				s += "\""+p.getEmerZip()+"\",";
 				s += "\""+p.getEmerWorkPhone()+"\",";
 				s += "\""+p.getEmerHomePhone()+"\",";
 				s += "\""+p.getEmerCellPhone()+"\",";
 				s += "\""+p.getEmerEmail()+"\",";
 
 				s += "\""+p.getEmer2ContactName()+"\",";
 				s += "\""+p.getEmer2ContactRelationship()+"\",";
 				s += "\""+p.getEmer2Address1()+"\",";
 				s += "\""+p.getEmer2Address2()+"\",";
 				s += "\""+p.getEmer2City()+"\",";
 				s += "\""+p.getEmer2State()+"\",";
 				s += "\""+p.getEmer2Zip()+"\",";
 				s += "\""+p.getEmer2WorkPhone()+"\",";
 				s += "\""+p.getEmer2CellPhone()+"\",";
 				s += "\""+p.getEmer2HomePhone()+"\",";
 				s += "\""+p.getEmer2Email()+"\",";
 
 				s += "\n";
 			}
 			log.debug("s="+s);
 
 			// create the output file:
 			try	{
 				FileWriter file = new FileWriter(theFile);
 				file.write(s);
 				file.close();
 //  3-21-03 kl: comment out h.put("filename", theFile);
 //  3-21-03 kl: un-comment  h.put("filename", httpFile);
 				log.info("Writing download file to "+httpFile);
 //				h.put("filename", theFile);
 				h.put("filename", httpFile);
 			} catch (IOException e) {
 				h.put("filename", "Sorry, an error occurred while writing the file.");
 				log.error("Error writing download file.");
 			}
 			ar.addHashtable("tub", h);
 			// forward on to specified page
 			ar.setView((String)action.getValue("view"));
 		} catch (Exception e) {
 			log.error("Exception encountered in SIAdminHandler.getRole(): ", e);
 		}
 		return ar;
 	}
 
 
 }
