 package org.alt60m.wsn.sp.servlet;
  
 import org.alt60m.crs.model.RegistrationType;
 import org.alt60m.html.FormHelper;
 import org.alt60m.ministry.model.dbio.OldAddress;
 import org.alt60m.ministry.model.dbio.Staff;
 import org.alt60m.servlet.ActionResults;
 import org.alt60m.servlet.Controller;
 //import org.alt60m.servlet.Controller.ActionContext;
 import org.alt60m.staffSite.model.dbio.StaffSiteProfile;
 import org.alt60m.util.SendMessage;
 import org.alt60m.wsn.sp.model.dbio.*;
 import org.alt60m.hr.ms.servlet.dbio.*;
 import org.apache.log4j.Priority;
 import org.alt60m.util.ObjectHashUtil;
 import org.alt60m.ministry.StaffSelectException;
 
 import com.kenburcham.framework.dbio.DBIOEntityException;
 import com.oreilly.servlet.MultipartRequest;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.text.*;
 import java.util.*;
 
 /**
  * Web controller for InfoBase
  *
  * History:
  *		1/24/02	MDP	Initial Coding
  *
  * Completeness (0 - 5):
  *		3
  *
  * Known Issues:
  *
  * @author  Mike Brinkley
  * @version 1.0
  */
 public class WsnSpController extends Controller
 {
 
 	// Comparator used to sort lists
 	private class comp implements Comparator
 	{
 		protected int _column = 0;
 
 		public comp()				{ _column = 0; }
 		public comp(int column) { _column = column; }
 
 		public int compare(Object o1, Object o2) {
 
 			String s1 = (String) ((Vector) o1).get(_column);
 			String s2 = (String) ((Vector) o2).get(_column);
 
 			return (s1.compareTo(s2));
 		}
 
 		public boolean equals(Object obj)	{ return (obj == this); }
 	}
 
 	private final String currentWsnYear = MSInfo.CURRENT_WSN_YEAR;
 
 	public WsnSpController()  {	}
 
 	public void accountNo (ActionContext ctx) {
 		Hashtable h = new Hashtable();
 		ctx.setSessionValue("tub", h);
 		MultipartRequest multi = null;
 		String contentPath = getServletContext().getRealPath("/wsnsp");
 		try {
 			// Get the multi-part form object
 			multi = new MultipartRequest(ctx.getRequest(), contentPath);
 			Enumeration files = multi.getFileNames();
 			// Get the uploaded file
 			String name = (String) files.nextElement();
 			java.io.File f = multi.getFile(name);
 			if (f == null) {
 				ctx.goToView("showaccountno");
 				return;
 			} 
 			FileReader fr = new FileReader(f);
 			
 			BufferedReader all_accounts = new BufferedReader(fr);
 			String[] columns;
 			String line;
 			// Loop over the account numbers to insert
 			while (all_accounts.ready()) {
 				line = all_accounts.readLine();
 				// If we hit a blank row, we're done with this file
 				if (line.trim().length() == 0) {
 					break;
 				}
 				columns = line.split(","); 
 				// If we don't have at least 47 columns, the file is bogus
 				if (columns.length < 2) {
 					throw new Exception("Invalid input file");
 				}
 				//ctx.getResponse().getWriter().println(line); //debug dump
 				// Get the application ID from this row
 				String id = columns[1];
 				// If the id for some reason is empty, we have to skip that row
 				if (id.equals("")) {
 					continue;
 				}
 				// Get the new account number
 				String accountNo = columns[0];
 				// If the accountNo for some reason is empty, we have to skip that row
 				if ((accountNo!=null) && (!accountNo.equals("")) && (!accountNo.equals(" "))) {
 					// If we get here we have a valid id and account number
 					WsnApplication person = new WsnApplication(id);
 					person.setApplAccountNo(accountNo);
 					if (person.getWsnSpouse() != null) {
 						WsnApplication spouse = person.getWsnSpouse();
 						spouse.setApplAccountNo(accountNo + "S");
 					}
 					person.persist();
 					String emailTo = "none";
 
 					WsnProject project = (WsnProject) person.getIsMember();
 					if (project.getIsPD() != null) { 
 						WsnApplication pd = (WsnApplication) project.getIsPD();
 						if (emailTo.equals("none")) {
 							emailTo = pd.getCurrentEmail();
 						} else {
 							emailTo = emailTo + ", " + pd.getCurrentEmail();
 						}
 					} 
 					if (project.getIsAPD() != null) {
 						WsnApplication apd = (WsnApplication) project.getIsAPD();
 						if (emailTo.equals("none")) {
 							emailTo = apd.getCurrentEmail();
 						} else {
 							emailTo = emailTo + ", " + apd.getCurrentEmail();
 						}
 					}
 					if (project.getIsCoord() != null) {
 						WsnApplication coord = (WsnApplication) project.getIsCoord();
 						if (emailTo.equals("none")) {
 							emailTo = coord.getCurrentEmail();
 						} else {
 							emailTo = emailTo + ", " + coord.getCurrentEmail();
 						}
 					}
 
 					if (!emailTo.equals("none")) {
 							try {
 								System.out.println("emailTo= " + emailTo); // trace
 								SendMessage email = new SendMessage();
 								email.setTo(emailTo);
 								email.setFrom("wsn@uscm.org");
 								email.setSubject("WSN Summer Project account number just assigned");
 								email.setBody("The account number for a student on the " + project.getName() + " project has just been assigned. \n\n Name: " + person.getLegalFirstName() + " " + person.getLegalLastName() + "\n\n Email: " + person.getCurrentEmail() + "\n\n Account number: " + accountNo);
 								email.send();
 							}
 							catch (Exception e) {
 								System.err.println(e.toString()); // 4-10-03 kl: Log the message and move on if invalid email
 								e.printStackTrace();
 							}
 					}
 				} // fi
 			} // while
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		} catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
 	}
 
 	public void addKids (ActionContext ctx) {
 		try {
 			Hashtable h = new Hashtable();
 
 			WsnApplication person = new WsnApplication();
 			WsnApplication spouse = new WsnApplication();
 			WsnApplication child = new WsnApplication();
 
 			String id = ctx.getInputString("id");
 
 			person = new WsnApplication(id);
 			spouse = person.getWsnSpouse();
 			
 	//deleting kids
 			if (ctx.getInputString("del").equals("yes")) {
 				child = new WsnApplication(ctx.getInputString("KidWsnApplicationID"));
 				child.delete();
 			}
 	//not deleting
 			else {
 	//existing kid to associate
 				if (ctx.getInputString("KidWsnApplicationID") != null) {
 					if (!ctx.getInputString("KidWsnApplicationID").equals("none")) {
 						child = new WsnApplication(ctx.getInputString("KidWsnApplicationID"));
 						WsnApplication oldParent = child.getChildOf();
 						if (oldParent!=null){
 							WsnApplication oldSpouse = oldParent.getWsnSpouse();
 							oldParent.dissocWsnChild(child);
 							oldParent.persist();
 								if (oldSpouse!=null){
 									oldSpouse.dissocWsnChild(child);
 									oldSpouse.persist();
 								}
 						}
 						//put them on the same project
 						WsnProject project = person.getIsMember();	//need to rename in all files this "M" to something more descriptive
 						if (project !=null){
 							child.setIsMember(project);	//for some reason new people are not getting assigned properly! this fixes it.
 						}
 
 						child.setChildOf(person);
 						person.assocWsnChild(child);
 						spouse.setChild(true);  //make sure their spouse knows about the kids!
 					} else if (ctx.getInputString("KidsFirstName")==null) {
 							//do nothing
 					} else if (ctx.getInputString("KidsFirstName").equals(" ")) {
 							//do nothing
 					} else {
 		//add a new kid
 						child.setLegalFirstName(ctx.getInputString("KidsFirstName"));
 						child.setLegalLastName(person.getLegalLastName());
 						child.setApplAccountNo("child");
 						child.setRole("1");
 						child.setRegion(person.getRegion());
 						child.setCurrentAddress(person.getCurrentAddress());
 						child.setCurrentCity(person.getCurrentCity());
 						child.setCurrentState(person.getCurrentState());
 						child.setCurrentZip(person.getCurrentZip());
 						child.setCurrentPhone(person.getCurrentPhone());
 						child.setCurrentEmail(person.getCurrentEmail());
 						// child.setDateAddressGoodUntil(person.getDateAddressGoodUntil());
 						child.setEmergName(person.getLegalFirstName()+ " " + person.getLegalLastName());
 						child.setEmergAddress(person.getCurrentAddress());
 						child.setEmergCity(person.getCurrentCity());
 						child.setEmergState(person.getCurrentState());
 						child.setEmergZip(person.getCurrentZip());
 						child.setEmergPhone(person.getCurrentPhone());
 						child.setMaritalStatus("S");
 						child.setStatus(person.getStatus());
 						child.setUsCitizen(person.getUsCitizen());
 						child.setWsnYear(person.getWsnYear());
 
 						//put them on the same project
 						WsnProject project = person.getIsMember();	//need to rename in all files this "M" to something more descriptive
 						if (project !=null){
 							child.setIsMember(project);	//for some reason new people are not getting assigned properly! this fixes it.
 						}
 
 						child.setChildOf(person);
 						person.assocWsnChild(child);
 						spouse.setChild(true);  //make sure their spouse knows about the kids!
 					}
 				} else {
 	//add a new kid
 					child.setLegalFirstName(ctx.getInputString("KidsFirstName"));
 					child.setLegalLastName(person.getLegalLastName());
 					child.setApplAccountNo("child");
 					child.setRole("1");
 					child.setRegion(person.getRegion());
 					child.setCurrentAddress(person.getCurrentAddress());
 					child.setCurrentCity(person.getCurrentCity());
 					child.setCurrentState(person.getCurrentState());
 					child.setCurrentZip(person.getCurrentZip());
 					child.setCurrentPhone(person.getCurrentPhone());
 					child.setCurrentEmail(person.getCurrentEmail());
 					// child.setDateAddressGoodUntil(person.getDateAddressGoodUntil());
 					child.setEmergName(person.getLegalFirstName()+person.getLegalLastName());
 					child.setEmergAddress(person.getCurrentAddress());
 					child.setEmergCity(person.getCurrentCity());
 					child.setEmergState(person.getCurrentState());
 					child.setEmergZip(person.getCurrentZip());
 					child.setEmergPhone(person.getCurrentPhone());
 					child.setMaritalStatus("S");
 					child.setStatus(person.getStatus());
 					child.setUsCitizen(person.getUsCitizen());
 
 					//put them on the same project
 					WsnProject project = person.getIsMember();	//need to rename in all files this "M" to something more descriptive
 					if (project !=null){
 						child.setIsMember(project);	//for some reason new people are not getting assigned properly! this fixes it.
 					}
 
 					child.setChildOf(person);
 					person.assocWsnChild(child);
 					spouse.setChild(true);  //make sure their spouse knows about the kids!
 				}
 			}
 
 			child.persist();
 			person.persist();
 			spouse.persist();
 			
 			showAddKids(ctx);
 		} catch (Exception e) {
 			ctx.goToView("error");
 			System.err.println(e.toString());
 		}
 	}
 
 	public void adminEditApp (ActionContext ctx) {
 		Hashtable h = new Hashtable();
 		FormHelper helper = new FormHelper();
 		
 		try {
 			Hashtable old = (Hashtable) ctx.getSessionValue("tub");
 			String unassignedID = "none";
 
 			WsnApplication person = null;
 			if (ctx.getInputString("id").equals("new")){ //new person so the ID was put in the session
 				person = new WsnApplication((String)ctx.getSessionValue("newID"));
 			}
 			else{	//hope they exist!
 				person = new WsnApplication(ctx.getInputString("id"));
 			}
 
 			h = ObjectHashUtil.obj2hash(person);
 
 //				if (m !=null){  // no longer works with dbio
 			String strM = "true";
 			if (person.getIsMemberId() != null) {
 				WsnProject m = person.getIsMember();
 				h.put("M",m.getWsnProjectID());
 				ctx.setSessionValue("teamID",(m.getWsnProjectID()));  //for the back to team roster links on evaluator pages.
 			} else {
 				ctx.setSessionValue("teamID","notAssigned");  //for the back to team roster links on evaluator pages.
 				strM = null;
 			}
 
 			WsnProject unassignedProject = new WsnProject();
 			unassignedProject.setName("Unassigned");
 			unassignedProject.setWsnYear((String)ctx.getSessionValue("wsnYear"));
 			unassignedProject.select();
 
 			unassignedID = unassignedProject.getWsnProjectID();
 
 			if (ctx.getInputString("M") != null) {
 				h.put("M", helper.value(ctx.getInputString("M")));
 			} else if (person.getRole().equals("4") && (strM == null) && (ctx.getInputString("M") == null)) {
 				person.setIsMember(unassignedProject);
 				h.put("M",unassignedID);
 			}
 
 			String unassignedCoord = "no";
 
 			if (person.getRole().equals("4") && (person.getIsMemberId().equals(unassignedID))) {
 				unassignedCoord = "yes";
 			}
 			if (person.getRole().equals("4") && (strM == null) && (ctx.getInputString("M") == null)) {
 				unassignedCoord = "yes";
 			}
 
 			h.put("unassignedCoord", unassignedCoord);
 
 			if ((old!=null)&&(old.get("frompage")!=null)) {
 				h.put("frompage",helper.value((String)old.get("frompage")));
 			}
 
 			if(person.getSubmittedDate() != null)
 			{
 				h.put("SubmittedDate",helper.value((new SimpleDateFormat("MM/dd/yyyy")).format( person.getSubmittedDate())));
 			}
 
 			if (person.getMaritalStatus().equals("M")) {	//get the spouse to display!
 				if (person.getWsnSpouseId() != null && !person.getWsnSpouseId().equals("0")) {
 					WsnApplication spouse = person.getWsnSpouse();
 					h.put("Spouse", spouse.getLegalFirstName() + " " + spouse.getLegalLastName());
 					h.put("SpouseID", spouse.getWsnApplicationID());
 				}
 				else {
 					h.put("Spouse", "Unassigned");
 					h.put("SpouseID", person.getWsnApplicationID());
 				}
 			}
 			else {
 				h.put("Spouse","none");
 			}
 
 			boolean femaleWithChildrenButNoSpouse = false;
 
 			if (person.getChild()) {		//children are going also!
 				java.util.Iterator children = null;
 				if (person.getGender().equals("1")) {
 					h.put("ShowKidsID", person.getWsnApplicationID());
 					children = person.getWsnChild().iterator();
 				}
 				else {		//the wife will reference the father's children because of model limitations
 					if (person.getWsnSpouse()!=null){
 						WsnApplication spouse = person.getWsnSpouse();
 						h.put("ShowKidsID", h.get("SpouseID"));
 						children = spouse.getWsnChild().iterator();
 					}
 					else{	//supposedly married, but no spouse, but their children are coming??
 						femaleWithChildrenButNoSpouse = true;
 					}
 				}
 				if (children!=null&&!femaleWithChildrenButNoSpouse){
 					for (int i = 0; children.hasNext(); i++) {
 						WsnApplication child = (WsnApplication) children.next();
 						if (child!=null){
 							Hashtable temp = new Hashtable();
 							temp.put("Name",child.getLegalFirstName());
 							temp.put("WsnApplicationID",child.getWsnApplicationID());
 							h.put(String.valueOf(i), temp);
 						}
 					}
 					h.put("Children", "true");
 				}
 				else{
 					h.put("Children","false");
 				}
 			}
 			else{
 				h.put("Children", "false");
 			}
 
 			if (person.getChildOfId()!=null) {		//they are a child
 				h.put("aChild", "true");
 			}
 			else {									//they are not a child
 				h.put("aChild", "false");
 			}
 
             //Load up viewing permisions
 			Hashtable profile = (Hashtable)ctx.getProfile();
             String userName = null;
             if(profile != null) {
             	userName = (String)profile.get("UserName");
             }
             
             //We've decided to let anyone who has access to the 
             //tool (which is based on their leadership role) to
             //be able to evaluate applicants.
             
             //h.put("ToolUser", (isUserAuthorized(userName,"ToolUser")?"Granted":"Denied"));
             //h.put("Evaluator", (isUserAuthorized(userName,"Evaluator")?"Granted":"Denied"));
 
             h.put("Evaluator", "Granted");
             
 			ctx.setSessionValue("tub", h);
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		} catch (Exception e) {
 			h.put("LegalFirstName","<font color='#ff0000'>An Error Occurred.");
 			h.put("LegalLastName","<br>This record cannot be displayed because it contains an error. <br>Please press the 'MyWSNHome' and notify wsn@uscm.org of the problem.</font>");
 			ctx.goToView("error");
 			ctx.setSessionValue("tub", h);
 			e.printStackTrace();
 		}
 	}
 
 	public void adminEditProj (ActionContext ctx) {
         try {
 			WsnProject project = new WsnProject(ctx.getInputString("id"));
             ObjectHashUtil.hash2obj(ctx.getHashedRequest(), project);
             project.persist();
 
 			showProj(ctx);
 		} catch (Exception e) {
 			ctx.goToView("error");
 			System.err.println(e.toString());
 		}
     }
 
 	public void adminEditUser (ActionContext ctx) {
 		try {
 	        Hashtable h = new Hashtable();
 			FormHelper helper = new FormHelper();
 
 			h.put("Fk_StaffSiteProfileID",ctx.getInputString("id"));
 
 			if (!ctx.getInputString("id").equals("null"))
 			{
 				try {
 					StaffSiteProfile surfer = new StaffSiteProfile(ctx.getInputString("id"));
 
 					h.put("LegalLastName",surfer.getLastName());
 					h.put("LegalFirstName",surfer.getFirstName());
 					h.put("CurrentEmail",helper.value((String)surfer.getUserName()));
 
 	      			if(surfer.getAccountNo()==null||surfer.getAccountNo().equals(""))
 	      				throw new StaffSelectException();
       				
   					Staff staff = new Staff(surfer.getAccountNo());
   				
   					h.put("Region",helper.value((String)staff.getRegion()));
 
   					OldAddress add = staff.getPrimaryAddress();
 					if (add != null) {
 						h.put("CurrentAddress",add.getAddress1()+ " "+add.getAddress2());
 						h.put("CurrentCity",helper.value((String)add.getCity()));
 						h.put("CurrentState",helper.value((String)add.getState()));
 						h.put("CurrentZip",helper.value((String)add.getZip()));
 					} else {
 						h.put("CurrentAddress","");
 						h.put("CurrentCity","");
 						h.put("CurrentState","");
 						h.put("CurrentZip","");
 					}
 
 					h.put("CurrentPhone",helper.value((String)staff.getHomePhone()));
 					h.put("Ssn",helper.value((String)staff.getSsn()));
 					String acctNo = surfer.getAccountNo();
 					if (acctNo.length() == 10) {
 						acctNo = acctNo.substring(0,9);
 					}
 					h.put("AccountNo",helper.value((String)acctNo.substring(2,9)));
 					h.put("ApplAccountNo",helper.value((String)acctNo.substring(2,9)));
 
 					String g = "";
 					if (staff.getIsMale()) {
 						g = "1";
 					} else {
 						g = "0";
 					}
 					h.put("Gender",g);
 					if (staff.getMaritalStatus().equals("M")) {
 						h.put("MaritalStatus","M");
 					} else {
 						h.put("MaritalStatus","S");
 					}
       				
 				} catch (StaffSelectException sse){
 					//sse.printStackTrace();
 					System.err.println(sse.toString());
 				} catch (Exception e) {
 					e.printStackTrace();
 					throw e;
 				}
 			}
 				ctx.setSessionValue("tub", h);
 				String view = ctx.getInputString("view");
 				ctx.goToView(view);
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			ctx.goToView("error");
 		}
 	}
 
 	public void adminListUser (ActionContext ctx) {
 		try {
 			Hashtable h = new Hashtable();
 			FormHelper helper = new FormHelper();
 
 			java.util.Iterator persons = null;
 
 			StaffSiteProfile ssp = new StaffSiteProfile();
 			
 			String lastName = ctx.getInputString("LegalLastName").toUpperCase();
 			lastName = lastName.replaceAll("'","''");
 			String whereClause="UPPER(lastName) like '" + lastName + "%' order by lastName, firstName";  //default
 
 			Iterator staffSiteProfileList = ssp.selectList(whereClause).iterator();
 			for (int i = 0; staffSiteProfileList.hasNext(); i++) {
 				StaffSiteProfile profile = (StaffSiteProfile) staffSiteProfileList.next();
 
 				Hashtable temp = new Hashtable();
 
 				temp.put("StaffProfileID",profile.getStaffSiteProfileID());
 				if ((profile.getFirstName() != null) && (profile.getLastName() != null))
 					{temp.put("Name",profile.getFirstName() + "&nbsp;" + profile.getLastName());}
 				if (profile.getUserName() != null)
 					{temp.put("CurrentEmail",helper.value((String)profile.getUserName()));}
 
 				Staff person = new Staff();
 				if ((profile.getAccountNo() != null) && (!profile.getAccountNo().equals("")))	{
 						person = new Staff(profile.getAccountNo());
 
 						if (person.getAccountNo() != null) {
 							temp.put("AccountNo",helper.value((String)person.getAccountNo()));
 							temp.put("ApplAccountNo",helper.value((String)person.getAccountNo()));
 						}
 						if (person.getPrimaryAddress() != null)	{
 							if (person.getPrimaryAddress().getAddress1() != null)
 								{temp.put("CurrentAddress",person.getPrimaryAddress().getAddress1());}
 							if (person.getPrimaryAddress().getCity() != null)
 								{temp.put("CurrentCity",person.getPrimaryAddress().getCity());}
 							if (person.getPrimaryAddress().getState() != null)
 								{temp.put("CurrentState",person.getPrimaryAddress().getState());}
 						}
 						if (person.getHomePhone() != null)
 							{temp.put("CurrentPhone",person.getHomePhone());}
 				}
 				h.put(String.valueOf(i), temp);
 			}						//end standard search
 			ctx.setSessionValue("tub", h);
 
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			ctx.goToView("error");
 		}
 	}
 
 	public void adminSave (ActionContext ctx) {
 		FormHelper helper = new FormHelper();
 
         try {
 			Hashtable h = new Hashtable();
 
 			WsnApplication person = new WsnApplication();
 			String surfer = ctx.getInputString("Fk_StaffSiteProfileID");
 
 			if (surfer != null && !surfer.equals("new")) {
 				person.setSurferID(ctx.getInputString("Fk_StaffSiteProfileID"));
 				person.setWsnYear((String)ctx.getSessionValue("wsnYear"));
 				boolean found = person.select();
         		if (!found)	{
 					person = new WsnApplication();
 					WsnPerson pers = new WsnPerson();
 					pers.setFk_StaffSiteProfileID(surfer);
 					if (pers.select()) {
 						person.setPerson(pers);
 					}
 //					if (surfer != null) {person.setSurferID(ctx.getInputString("SurferID"));}
         		}
 			}
 
 			person.setSurferID(surfer);
 			if (ctx.getInputString("Ssn") != null){person.setSsn(ctx.getInputString("Ssn"));}
 			if (ctx.getInputString("CurrentAddress") != null){person.setCurrentAddress(ctx.getInputString("CurrentAddress"));}
 			if (ctx.getInputString("CurrentCity") != null){person.setCurrentCity(ctx.getInputString("CurrentCity"));}
 			if (ctx.getInputString("CurrentState") != null){person.setCurrentState(ctx.getInputString("CurrentState"));}
 			if (ctx.getInputString("CurrentZip") != null){person.setCurrentZip(ctx.getInputString("CurrentZip"));}
 			if (ctx.getInputString("CurrentPhone") != null){person.setCurrentPhone(ctx.getInputString("CurrentPhone"));}
 			if (ctx.getInputString("LegalFirstName") != null){person.setLegalFirstName(ctx.getInputString("LegalFirstName"));}
 			if (ctx.getInputString("LegalLastName") != null){person.setLegalLastName(ctx.getInputString("LegalLastName"));}
 			if (ctx.getInputString("Role") != null){person.setRole(ctx.getInputString("Role"));}
 			if (ctx.getInputString("Region") != null){person.setRegion(ctx.getInputString("Region"));}
 			if (ctx.getInputString("CurrentEmail") != null){person.setCurrentEmail(ctx.getInputString("CurrentEmail"));}
 			// if (ctx.getInputString("DateAddressGoodUntil") != null){person.setDateAddressGoodUntil(ctx.getInputString("DateAddressGoodUntil"));}
 			if (ctx.getInputString("IsStaff") != null){
 				if (ctx.getInputString("IsStaff").equals("1"))
 				{
 					person.setIsStaff(true);
 					person.setYearInSchool("Other");
 					person.setPrevIsp(true);
 					person.setIsApplicationComplete(true);
 					person.setUsCitizen(true);
 
 				} else
 				{
 					person.setIsStaff(false);
 				}
 			}
 			if (ctx.getInputString("MaritalStatus") != null){person.setMaritalStatus(ctx.getInputString("MaritalStatus"));}
 			if (ctx.getInputString("Child") != null){
 				if (ctx.getInputString("Child").equals("1"))
 				{
 					person.setChild(true);
 				} else
 				{
 					person.setChild(false);
 				}
 			}
 			if (ctx.getInputString("Gender") != null){person.setGender(ctx.getInputString("Gender"));}
 			if (ctx.getInputString("ApplAccountNo") != null){person.setApplAccountNo(ctx.getInputString("ApplAccountNo"));}
 			if (ctx.getSessionValue("wsnYear") != null){person.setWsnYear((String)ctx.getSessionValue("wsnYear"));}
 			
 			person.persist();
 			
 			h.put("id",person.getWsnApplicationID());
 			h.put("LegalFirstName",person.getLegalFirstName());
 			h.put("LegalLastName",person.getLegalLastName());
 			// h.put("DateAddressGoodUntil",person.getDateAddressGoodUntil());
 			h.put("frompage","adduser");
 
 			java.util.Iterator projects = person.getIsCoord().iterator();
 			if (projects.hasNext()){
 				int i=0;
 				while (projects.hasNext()) {
 					WsnProject project = (WsnProject) projects.next();
 					Hashtable temp = new Hashtable();
 					temp.put("id",project.getWsnProjectID());
 					temp.put("name",project.getName());
 					h.put(String.valueOf(i),temp);
 					i++;
 				}
 			}
 
 			if (person.getIsMember() != null) {
 				h.put("IsMemberOf",person.getIsMember().getName());
 				h.put("IsMemberOfID",person.getIsMember().getWsnProjectID());
 			}
 
 			ctx.setSessionValue("tub", h);
 
 			if (ctx.getInputString("Role").equals("5"))
 			{
 				ctx.goToView("adduser"); //change later
 			}
 			else if (ctx.getInputString("Role").equals("4"))
 			{
 				ctx.goToView("assigncoord"); //change later
 			}
 			else if (ctx.getInputString("Role").equals("3"))
 			{
 				//assign as pd to proj
 				ctx.goToView("assignpdapd"); //change later
 			}
 			else if (ctx.getInputString("Role").equals("2"))
 			{
 				ctx.goToView("assignpdapd"); //change later
 			}
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 	public void adminSaveApp (ActionContext ctx) {
 		FormHelper helper = new FormHelper();
 		Hashtable h = new Hashtable();
 
 		try {
 			WsnApplication person = new WsnApplication();
 			String view = ctx.getInputString("view");
 
 			String whereClause = "";
 
 			if (ctx.getInputString("id").equals("new"))	{
 				if (ctx.getInputString("Ssn") == null || ctx.getInputString("Ssn").equals("")) { // 3-13-03 kl added to check for ssn null
 					whereClause=whereClause + " wsnYear ='"+ctx.getSessionValue("wsnYear")+"'";  // 3-14-03 kl added direct SQL
 					whereClause=whereClause + " AND applAccountNo ='"+ctx.getInputString("ApplAccountNo")+"'";
 					whereClause=whereClause + " AND legalLastName ='"+ctx.getInputString("LegalLastName")+"'";
 					whereClause=whereClause + " AND legalFirstName ='"+ctx.getInputString("LegalFirstName")+"'";
 					whereClause=whereClause + " ORDER BY legalLastName, legalFirstName";  //default
 				} else {
 					whereClause=whereClause + " wsnYear ='"+ctx.getSessionValue("wsnYear")+"'";  // 3-14-03 kl added direct SQL
 					whereClause=whereClause + " AND ssn ='"+ctx.getInputString("Ssn")+"'"; // 3-14-03 kl try to match OQL below
 					whereClause=whereClause + " AND ssn != ''";
 					whereClause=whereClause + " OR wsnYear ='"+ctx.getInputString("wsnYear")+"'";
 					whereClause=whereClause + " AND applAccountNo ='"+ctx.getInputString("ApplAccountNo")+"'";
 					whereClause=whereClause + " AND legalLastName ='"+ctx.getInputString("LegalLastName")+"'";
 					whereClause=whereClause + " AND legalFirstName ='"+ctx.getInputString("LegalFirstName")+"'";
 					whereClause=whereClause + " ORDER BY legalLastName, legalFirstName";  //default
 				}
 				Iterator sameperson = person.selectList(whereClause).iterator();
 
 				if (ctx.getInputString("saveAnyway") == null) {
 					if (sameperson.hasNext())	{
 						person = (WsnApplication) sameperson.next();
 						String sameString = "";
 						if (person.getSsn() != null) {
 							if (person.getSsn().equals(ctx.getInputString("Ssn"))) {
 								sameString = sameString + "social security number";
 							}
 						}
 						String AccountNoCheck = "False";
 						if (person.getApplAccountNo() != null) {
 							if ((person.getSsn().equals(ctx.getInputString("Ssn"))) && (person.getApplAccountNo().equals(ctx.getInputString("ApplAccountNo")))) {
 								sameString = sameString + " or ";
 							}
 							if (person.getApplAccountNo().equals(ctx.getInputString("ApplAccountNo"))) {
 								sameString = sameString + "account number";
 								AccountNoCheck = "True";
 							}
 						}
 						h.put("accountNoCheck",helper.value((String)AccountNoCheck));
 						h.put("sameString",helper.value((String)sameString));
 						h.put("sameid",person.getWsnApplicationID());
 						h.put("sameLegalFirstName",helper.value((String)person.getLegalFirstName()));
 						h.put("sameLegalLastName",helper.value((String)person.getLegalLastName()));
 						h.put("sameCurrentAddress",helper.value((String)person.getCurrentAddress()));
 						h.put("sameCurrentCity",helper.value((String)person.getCurrentCity()));
 						h.put("sameCurrentState",helper.value((String)person.getCurrentState()));
 						h.put("sameCurrentZip",helper.value((String)person.getCurrentZip()));
 						h.put("sameCurrentPhone",helper.value((String)person.getCurrentPhone()));
 						h.put("sameCurrentEmail",helper.value((String)person.getCurrentEmail()));
 						h.put("sameSsn",helper.value((String)person.getSsn()));
 						h.put("sameAccountNo",helper.value((String)person.getApplAccountNo()));
 						h.put("sameUniversityFullName",helper.value((String)person.getUniversityFullName()));
 						if (person.getIsStaff()){
 							h.put("sameIsStaff","staff");
 						} else {h.put("sameIsStaff","student");}
 
 						ctx.setSessionValue("tub", h);
 						ctx.goToView("duplicateapp");
 						return;
 					}
 				}
 				person = new WsnApplication();
 			} else{
 				person = new WsnApplication(ctx.getInputString("id"));
 			}
 			ObjectHashUtil.hash2obj(ctx.getHashedRequest(), person);
 			
 			if (ctx.getInputString("M") != null) {
 				WsnProject project = new WsnProject(ctx.getInputString("M"));
 				if (ctx.getInputString("id").equals("new"))	{
 					person.setIsMember(project);	//for some reason new people are not getting assigned properly! this fixes it.
 				} else {
 					person.setIsMember(project);
 				}
 			}
 
 			if (ctx.getInputString("StaffProfileID") != null)	{
 				String surferid = ctx.getInputString("StaffProfileID");
 				person.setSurferID(surferid);
 			}
 			//check form status, set acceptedDate accordingly
 			if (ctx.getInputString("Status") != null) {
 				if (ctx.getInputString("Status").equals("Accepted")) {
 					if (person.getAcceptedDate() != null) {
 						// do nothing, do not alter existing acceptedDate
 					}
 					else {
 						SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yyyy");
 						Date today = sdfDate.parse(sdfDate.format(new Date()));
 						person.setAcceptedDate(today);
 					}
 				}
 				else {
 					//status not accepted, make sure and reset acceptedDate accordingly
 					person.setAcceptedDate(null);
 				}
 			}
 			person.persist();
 			ctx.setSessionValue("newID",person.getWsnApplicationID());
 			adminEditApp(ctx);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 
 	public void adminSaveProj (ActionContext ctx) {
 		Hashtable h = new Hashtable();
 		FormHelper helper = new FormHelper();
         try {
 			WsnProject project = new WsnProject();
 			if (ctx.getInputString("id").equals("new"))	{
 				project.persist();
 				ctx.setSessionValue("newID",project.getWsnProjectID());
 			} else{
 				project = new WsnProject(ctx.getInputString("id"));
 			}
 
 			ObjectHashUtil.hash2obj(ctx.getHashedRequest(), project);
 			project.persist();
 			
 			String view = ctx.getInputString("view");
 			if (view.equals("showprojectinfo")) {
 				showProj(ctx);
 			}
 			else {
 				ctx.goToView(view);
 			}
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 	public void adminSaveUserCoord (ActionContext ctx) {
 		try {
 			Hashtable h = new Hashtable();
 
 			String projectname = "none";
 			String projectid = "none";
 
 			WsnApplication person = new WsnApplication(ctx.getInputString("id"));
 			WsnProject project = new WsnProject(ctx.getInputString("Project"));
 
 			if (person.getIsMember() != null) {
 				projectname = person.getIsMember().getName();
 				projectid = person.getIsMemberId();
 			}
 
 			if (ctx.getInputString("mode").equals("remove")) {
 				project.setIsCoord(null);
 			} else if (ctx.getInputString("mode").equals("removeproject")) {
 					person.setIsMember(null);
 					projectname = "none";
 					projectid = "none";
 			} else if (ctx.getInputString("mode").equals("add")) {
 				if (project.getIsCoord() != null && !project.getIsCoord().equals("")) {
 					WsnApplication oldPerson = project.getIsCoord();
 					String whereClause = "fk_IsCoord="+oldPerson.getWsnApplicationID()+" and wsnYear='"+oldPerson.getWsnYear()+"' order by name";
 					Collection proj = new WsnProject().selectList(whereClause);
 					if (proj.size() == 1) {
 						oldPerson.setRole("1");
 					}
 					oldPerson.persist();
 					project.setIsCoord(null);
 				}
 				if (ctx.getInputString("going").equals("true")) {
 					if (person.getIsMember() != null) {
 						person.setIsMember(null);
 					}
 					person.setIsMember(project);
 					projectname = project.getName();
 					projectid = project.getWsnProjectID();
 				}
 				project.setIsCoord(person);
 				person.setStatus("Accepted");
 				person.setRole("4");
 			}
 			
 			person.persist();
 			project.persist();
 
 			java.util.Iterator projects = person.getIsCoord().iterator();
 			if (projects.hasNext()){
 				int i=0;
 				while (projects.hasNext()) {
 					WsnProject thisProject = (WsnProject) projects.next();
 					Hashtable temp = new Hashtable();
 					temp.put("id",thisProject.getWsnProjectID());
 					temp.put("name",thisProject.getName());
 					h.put(String.valueOf(i),temp);
 					i++;
 				}
 			}
 
 			h.put("id",person.getWsnApplicationID());
 			h.put("LegalFirstName",person.getLegalFirstName());
 			h.put("LegalLastName",person.getLegalLastName());
 
 			if (!projectname.equals("none") && !projectid.equals("none")) {
 				h.put("IsMemberOf",projectname);
 				h.put("IsMemberOfID",projectid);
 			}
 
 			Hashtable clearTub = new Hashtable();
 			ctx.setSessionValue("tub", clearTub);
 			ctx.setSessionValue("tub", h);
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 	public void adminSaveUserPdApd (ActionContext ctx) {
 		try {
 			Hashtable h = new Hashtable();
 
 			WsnApplication person = new WsnApplication(ctx.getInputString("id"));
 			if (ctx.getInputString("Project") != null) {
 				WsnProject project = new WsnProject(ctx.getInputString("Project"));
 
 				if (ctx.getInputString("leadrole").equals("pd")) {
 					if (project.getIsPDId() != null && !project.getIsPDId().equals("")) {
 						WsnApplication oldPerson = project.getIsPD();
 						project.setIsPD(null);
 						if (oldPerson != null) {
 							if (oldPerson.getIsMemberId() != null) {
 								oldPerson.setIsMember(null);
 								oldPerson.setRole("1");
 							}
 							oldPerson.persist();
 						}
 					}
 					if (person.getIsPDId() != null && !person.getIsPDId().equals("")) {
 						WsnProject oldProject = person.getIsPD();
 						oldProject.setIsPD(null);
 						oldProject.persist();
 					}
 					if (person.getIsMemberId() != null && !person.getIsMemberId().equals("")) {
 						person.setIsMember(null);
 					}
 					project.setIsPD(person);
 					person.setIsMember(project);
 					person.setStatus("Accepted");
 					person.setRole("3");
 				}
 				if (ctx.getInputString("leadrole").equals("apd")) {
 					if (project.getIsAPDId() != null && !project.getIsAPDId().equals("")) {
 						WsnApplication oldPerson = project.getIsAPD();
 						project.setIsAPD(null);
 						if (oldPerson != null) {
 							if (oldPerson.getIsMemberId() != null) {
 								oldPerson.setIsMember(null);
 								oldPerson.setRole("1");
 							}
 							oldPerson.persist();
 						}
 					}
 					if (person.getIsAPDId() != null && !person.getIsAPDId().equals("")) {
 						WsnProject oldProject = person.getIsAPD();
 						oldProject.setIsAPD(null);
 						oldProject.persist();
 					}
 					if (person.getIsMemberId() != null && !person.getIsMemberId().equals("")) {
 						person.setIsMember(null);
 					}
 					project.setIsAPD(person);
 					person.setIsMember(project);
 					person.setStatus("Accepted");
 					person.setRole("2");
 				}
 				project.persist();
 			}
 			person.persist();
 			if (ctx.getInputString("frompage")!=null) {
 				h.put("frompage",ctx.getInputString("frompage"));
 			}
 			ctx.setSessionValue("tub", h);
 
 			adminEditApp(ctx);
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 	public void clearTub (ActionContext ctx) {
 		try
 		{
 			Hashtable h = new Hashtable();
 			ctx.setSessionValue("tub", h);
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
 	}
 
 	public void defaultShowProj (ActionContext ctx) {
 		Hashtable h = new Hashtable();
 		try	{
 			WsnProject project = new WsnProject(ctx.getInputString("id"));
 			h = ObjectHashUtil.obj2hash(project);
 			project.persist();
 			ctx.setSessionValue("tub", h);
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 	public void deleteProj (ActionContext ctx) {
 		try {
 			WsnProject project = new WsnProject(ctx.getInputString("id"));
 
 			//dissociate members
 			java.util.Iterator persons = project.getIsMember().iterator();
 			int i=0;
 			while (persons.hasNext()) {
 				WsnApplication person = (WsnApplication) persons.next();
 				person.setIsMember(null);
 				person.persist();
 				//SET ASSIGNED PROJECT TO UNASSIGNED!!!!!!!!!!!!!!!
 			}
 
 			WsnApplication person = null;
 
 			//dissociate PD
 			String getIsPDId = project.getIsPDId();
 			if (getIsPDId !=null && !getIsPDId.equals("0") && !getIsPDId.equals("")){
 				person = project.getIsPD();
 				person.setRole("1");
 				person.persist();
 			}
 			project.setIsPD(null);
 
 			//dissociate APD
 			if (project.getIsAPDId() !=null && !project.getIsAPDId().equals("0") && !project.getIsAPDId().equals("")){
 				person = project.getIsAPD();
 				person.setRole("1");
 				person.persist();
 			}
 			project.setIsAPD(null);
 
 			//dissociate Coord
 			if (project.getIsCoordId() !=null && !project.getIsCoordId().equals("0") && !project.getIsCoordId().equals("")){
 				person = project.getIsCoord();
 				String whereClause = "fk_IsCoord="+person.getWsnApplicationID()+" and wsnYear='"+person.getWsnYear()+"' order by name";
 				Collection proj = new WsnProject().selectList(whereClause);
 				if (proj.size() == 1) {
 					person.setRole("1");
 				}
 				person.persist();
 			}
 			project.setIsCoord(null);
 			
 			project.delete();
 			showMainProj(ctx);
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 	protected String element2hash(String s) {
 		if (s == null)	{
 			String blank = " ";
 			return blank;
 		} else {
 			return s;
 		}
     }
 
 	public String fetchPersonID(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpSession session) {
 
 		String surferID = (String) session.getAttribute("loggedIn");
 		String personID = " ";
 
 		try {
 			if (surferID != null) {
 				WsnApplication person = new WsnApplication();
 				person.setWsnYear((String) session.getAttribute("wsnYear"));
 				person.setSurferID(surferID);
 				boolean found = person.select();
 				if (found) {
 					personID = person.getWsnApplicationID();
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		session.setAttribute("WsnApplicationID", personID);
 		return personID;
 	}
 
 	public void getStaffInfo (ActionContext ctx) {
 		FormHelper helper = new FormHelper();
 		try {
 			Hashtable temp = new Hashtable();
 			if (ctx.getInputString("id").equals(""))	{
 				StaffSiteProfile person = new StaffSiteProfile(ctx.getInputString("StaffProfileID"));
 				temp.put("LegalFirstName",helper.value((String)person.getFirstName()));
 				temp.put("LegalLastName",helper.value((String)person.getLastName()));
 				temp.put("CurrentEmail",helper.value((String)person.getUserName()));
 			} else {
 				Staff person = new Staff(ctx.getInputString("id"));  // javax.rmi.PortableRemoteObject.narrow(staff.next(),StaffObject.class);
 				temp.put("LegalFirstName",helper.value((String)person.getFirstName()));
 				temp.put("LegalLastName",helper.value((String)person.getLastName()));
 				temp.put("Ssn",helper.value((String)person.getSsn()));
 				if (person.getIsMale()){
 					temp.put("Gender","1");
 				} else {
 					temp.put("Gender","0");
 				}
 				temp.put("Region",helper.value((String)person.getRegion()));
 				String acctNo = person.getAccountNo();
 				if (acctNo.length() == 10) {
 					acctNo = acctNo.substring(0,9);
 				}
 				temp.put("AccountNo",helper.value((String)acctNo.substring(2,9)));
 				temp.put("ApplAccountNo",helper.value((String)acctNo.substring(2,9)));
 
 				String address = new String();
 				if (person.getPrimaryAddress() != null)	{
 					address = person.getPrimaryAddress().getAddress1();
 					if (person.getPrimaryAddress().getAddress2() != null) {
 						address = address + ", " + person.getPrimaryAddress().getAddress2();
 					}
 					temp.put("CurrentAddress",helper.value((String)address));
 					temp.put("CurrentCity",helper.value((String)person.getPrimaryAddress().getCity()));
 					temp.put("CurrentState",helper.value((String)person.getPrimaryAddress().getState()));
 					temp.put("CurrentZip",helper.value((String)person.getPrimaryAddress().getZip()));
 				}
 				temp.put("CurrentPhone",helper.value((String)person.getHomePhone()));
 				temp.put("CurrentEmail",helper.value((String)person.getEmail()));
 				if (person.getMaritalStatus()!=null){
 					if (person.getMaritalStatus().equals("S"))	{
 						temp.put("MaritalStatus","S");
 					} else if (person.getMaritalStatus().equals("M")) {
 						temp.put("MaritalStatus","M");
 					} else if (person.getMaritalStatus().equals("W")) {
 						temp.put("MaritalStatus","W");
 					} else if (person.getMaritalStatus().equals("D")) {
 						temp.put("MaritalStatus","D");
 					}
 				}
 			}
 
 			temp.put("IsStaff","true");
 			temp.put("YearInSchool","Other");
 			temp.put("frompage","ProjectStaff");
 
 			ctx.setSessionValue("tub", temp);
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
         } catch (Exception e) {
                System.err.println(e);
                System.err.println("Failed to perform GetStaffInfoAction");
 		}
     }
 
 	// ************************************************************
 	// Test entry point
 
 	public void init() {
 		log(Priority.DEBUG, "WsnSpController.init()");
 		setViewsFile(getServletContext().getRealPath("/WEB-INF/WsnSpViewsDbio.xml"));
 		setDefaultAction("showIndex");
 		log(Priority.DEBUG, "WsnSpController constructor");
 	}
 
 	public void insurance (ActionContext ctx) {
 		Hashtable h = new Hashtable();
 
 		try {
 			Hashtable old = (Hashtable) ctx.getSessionValue("tub");
 
 			String id = "";
 			String InsuranceGood = "";
 			int i = 0;
 
 			Integer counter= new Integer((String)ctx.getInputString("counter"));
 			while (i<(counter.intValue())){
 
 				id = ctx.getInputString(String.valueOf(i));
 				if (id!=null){
 					InsuranceGood = (String)ctx.getInputString(id);
 				}
 				if ((InsuranceGood!=null)&&(id!=null)) {
 					try	{
 						WsnApplication person = new WsnApplication(id);
 						person.setInsuranceReceived(InsuranceGood.equalsIgnoreCase("true"));
 						person.persist();
 						h.put(String.valueOf(i),String.valueOf(InsuranceGood.equalsIgnoreCase("true")));
 					}
 					catch (Exception e) {
 					}
 				}
 				i++;
 			}
 			ctx.setSessionValue("tub", h);
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 	public void insuranceSubmitForm (ActionContext ctx) {
         Hashtable h = new Hashtable();
 		FormHelper helper = new FormHelper();
 		ctx.setSessionValue("tub", h);  // blank out the tub's previous info
 		try {
 			WsnProject project = new WsnProject(ctx.getInputString("id"));
 			h.put("ProjectName",helper.value((String)project.getName()));
 			h.put("id",helper.value((String)project.getWsnProjectID()));
 			h.put("PartnershipRegion",helper.value((String)project.getPartnershipRegion()));
 			h.put("StartDate",project.getStudentStartDate()==null?"":(new SimpleDateFormat("MM/dd/yyyy")).format( project.getStudentStartDate() ) );
 			h.put("StopDate",project.getStudentEndDate()==null?"":(new SimpleDateFormat("MM/dd/yyyy")).format( project.getStudentEndDate() ) );
 			h.put("City",helper.value((String)project.getCity()));
 			h.put("Country",helper.value((String)project.getCountry()));
 			h.put("CountryLetter",helper.value((String)project.getCountry()).substring(0,1));
 			h.put("OperatingAccountNo",helper.value((String)project.getOperatingAccountNo()));
 
 			String getIsPDId = project.getIsPDId();
 			if (getIsPDId !=null && !getIsPDId.equals("0") && !getIsPDId.equals("")){
 				WsnApplication leader = project.getIsPD();
 				h.put("ProjectPD", leader.getLegalFirstName() + " " + leader.getLegalLastName());
 				h.put("ProjectPDID", leader.getWsnApplicationID());
 			}
 			else{
 				h.put("ProjectPD", "none");
 				h.put("ProjectPDID", "xxx");
 			}
 
 			String getIsAPDId = project.getIsAPDId();
 			if (getIsAPDId !=null && !getIsAPDId.equals("0") && !getIsAPDId.equals("")){
 				WsnApplication leader = project.getIsAPD();
 				h.put("ProjectAPD", leader.getLegalFirstName() + " " + leader.getLegalLastName());
 				h.put("ProjectAPDID", leader.getWsnApplicationID());
 			}
 			else{
 				h.put("ProjectAPD", "none");
 				h.put("ProjectAPDID", "xxx");
 			}
 
 			String getIsCoordId = project.getIsCoordId();
 			if (getIsCoordId !=null && !getIsCoordId.equals("0") && !getIsCoordId.equals("")){
 				WsnApplication leader = project.getIsCoord();
 				h.put("ProjectCoord", leader.getLegalFirstName() + " " + leader.getLegalLastName());
 				h.put("ProjectCoordID", leader.getWsnApplicationID());
 			}
 			else{
 				h.put("ProjectCoord", "none");
 				h.put("ProjectCoordID", "xxx");
 			}
 
 
 			java.util.Iterator persons = project.getIsMember().iterator();
 			int i=0;
 			while (persons.hasNext()) {
 				WsnApplication person = (WsnApplication) persons.next();
 				if (person.getStatus()!=null){
 					if (person.getStatus().equalsIgnoreCase("Accepted"))	{
 						//only display people who are accepted
 						Hashtable temp = new Hashtable();
 						temp.put("FirstName",helper.value((String)person.getLegalFirstName()));
 						temp.put("LastName",helper.value((String)person.getLegalLastName()));
 						temp.put("WsnApplicationID",helper.value((String)person.getWsnApplicationID()));
 						temp.put("AccountNo",helper.value((String)person.getAccountNo()));
 						temp.put("ApplAccountNo",helper.value((String)person.getApplAccountNo()));
 						temp.put("PassportNo",helper.value((String)person.getPassportNo()));
 						temp.put("VisaNo",helper.value((String)person.getVisaNo()));
 						if (person.getIsStaff()){
 							temp.put("OnStaff","Yes");
 						}
 						else {
 							temp.put("OnStaff","No");
 						}
 						h.put(String.valueOf(i),temp);
 						i++;
 					}
 				} // done with accepted person, ignore pending/withdrawn/rejected
 			}
 
 			ctx.setSessionValue("tub", h);
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
     //Created DMB 1/30/2003
     private boolean isUserAuthorized(String ssmUserID, String role) {
         try {
             boolean authorized = false;
             
             SimpleDateFormat redoDate = new SimpleDateFormat("yyyy-MM-dd");
             Date today = redoDate.parse(redoDate.format(new Date()));
 
             WsnUser user = new WsnUser();
             user.setSsmUserName(ssmUserID);
             
             Collection users = new Vector();
             if (ssmUserID != null) {
             	users = user.selectList();
             }
 
             for(Iterator i = users.iterator(); i.hasNext();) {
                 user = (WsnUser) i.next();
                 Date expire = redoDate.parse(redoDate.format(user.getExpirationDate()));
                 if(role.equals(user.getRole()) && user.getExpirationDate()!=null && 1>today.compareTo(expire))
                     authorized = true;
             }
             return authorized;
         } catch (Exception e) {
             e.printStackTrace();
             return false;
         }
     }
 
 	public void linkSpouse (ActionContext ctx) {
 		try {
 			WsnApplication person = new WsnApplication(ctx.getInputString("id"));
 			WsnApplication spouse = new WsnApplication();
 			Hashtable h = new Hashtable();
 
 			if (ctx.getInputString("spouseID").equals("x")) {
 				//do nothing
 			}
 			else if (ctx.getInputString("spouseID").equals("Disassociate")) {
 				spouse = person.getWsnSpouse();
 				person.setWsnSpouse(null);
 				spouse.setWsnSpouse(null);
 				spouse = null;
 			} else {
 				spouse = new WsnApplication(ctx.getInputString("spouseID"));
 //					person.setWsnSpouse(null);
 				person.setWsnSpouse(spouse);
 //					spouse.setWsnSpouse(null);
 				spouse.setWsnSpouse(person);
 				spouse.setMaritalStatus("M");  //make sure their status is now married
 				if (spouse.getChild()&&(!person.getChild())){
 					spouse.setChild(true);
 					person.setChild(true);
 				}
 			}
 
 			person.persist();
 			if (!spouse.isPKEmpty()) spouse.persist();
 			adminEditApp(ctx);
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 	public void listApps (ActionContext ctx) {
 		Hashtable h = new Hashtable();
 		FormHelper helper = new FormHelper();
 
 		try {
 			String whereClause = "";
 			if (ctx.getInputString("Admin")!=null){
 				whereClause=whereClause + " role like '" + ctx.getInputString("Admin") + "%'";
 			}
 			if ((ctx.getInputString("LegalFirstName")!=null)&&(!ctx.getInputString("LegalFirstName").equals(""))){
 				whereClause=whereClause + " AND UPPER(legalFirstName) like '" + ctx.getInputString("LegalFirstName").toUpperCase().replaceAll("'", "''") + "%'";
 			}
 			if ((ctx.getInputString("LegalLastName")!=null)&&(!ctx.getInputString("LegalLastName").equals(""))){
 				whereClause=whereClause + " AND UPPER(legalLastName) like '" + ctx.getInputString("LegalLastName").toUpperCase().replaceAll("'","''") + "%'";
 			}
 			if ((ctx.getInputString("Gender")!=null)&&(!ctx.getInputString("Gender").equals(""))){
 				whereClause=whereClause + " AND gender = '" + ctx.getInputString("Gender") + "'";
 			}
 			if ((ctx.getInputString("IsStaff")!=null)&&(!ctx.getInputString("IsStaff").equals(""))){
 				if (ctx.getInputString("IsStaff").equals("1"))	{
 					whereClause=whereClause + " AND isStaff = '1' ";
 				}
 				if (ctx.getInputString("IsStaff").equals("0"))	{
 					whereClause=whereClause + " AND isStaff = '0' ";
 				}
 			}
 			if ((ctx.getInputString("University")!=null)&&(!ctx.getInputString("University").equals(""))){
 				whereClause=whereClause + " AND UPPER(universityFullName) like '" + ctx.getInputString("University").toUpperCase().replaceAll("'", "''") + "%'";
 			}
 			if ((ctx.getInputString("Region")!=null)&&(!ctx.getInputString("Region").equals(""))){
 				if (!ctx.getInputString("Region").equals(" ")){
 					whereClause=whereClause + " AND region = '" + ctx.getInputString("Region") + "'";
 				}
 			}
 			if ((ctx.getInputString("City")!=null)&&(!ctx.getInputString("City").equals(""))){
 				if (!ctx.getInputString("City").equals(" ")){
 					whereClause=whereClause + " AND UPPER(currentCity) like '" + ctx.getInputString("City").toUpperCase().replaceAll("'", "''") + "%'";
 				}
 			}
 			if ((ctx.getInputString("State")!=null)&&(!ctx.getInputString("State").equals(""))){
 				if (!ctx.getInputString("State").equals(" ")){
 					whereClause=whereClause + " AND currentState = '" + ctx.getInputString("State") + "'";
 				}
 			}
 			if ((ctx.getInputString("ApplAccountNo")!=null)&&(!ctx.getInputString("ApplAccountNo").equals(""))){
 				if (!ctx.getInputString("ApplAccountNo").equals(" ")){
 					whereClause=whereClause + " AND applAccountNo = '" + ctx.getInputString("ApplAccountNo") + "'";
 				}
 			}
 			if ((ctx.getInputString("Project")!=null)&&(!ctx.getInputString("Project").equals(""))){
 				whereClause=whereClause + " AND (projectPref1 = '" + ctx.getInputString("Project") + "' OR projectPref2 = '" + ctx.getInputString("Project") + "' OR projectPref3 = '" + ctx.getInputString("Project") + "' OR projectPref4 = '" + ctx.getInputString("Project") + "' OR projectPref5 = '" + ctx.getInputString("Project") + "')";
 			}
 			if ((ctx.getInputString("ProjectAssign")!=null)&&(!ctx.getInputString("ProjectAssign").equals(""))){
 				whereClause=whereClause + " AND fk_isMember = '" + ctx.getInputString("ProjectAssign") + "'";
 			}
 			if ((ctx.getInputString("Year")!=null)&&(!ctx.getInputString("Year").equals(""))){
 				whereClause=whereClause + " AND wsnYear = '" + ctx.getInputString("Year") + "'";
 			}
 			if ((ctx.getInputString("IsApplicationComplete")!=null)&&(!ctx.getInputString("IsApplicationComplete").equals(""))){
 				if (ctx.getInputString("IsApplicationComplete").equals("true")){
 					whereClause=whereClause + " AND isApplicationComplete='true'";
 				}
 				else {
 					whereClause=whereClause + " AND isApplicationComplete='false'";
 				}
 			}
 			if ((ctx.getInputString("Status")!=null)&&(!ctx.getInputString("Status").equals(""))){
 				whereClause=whereClause + "AND status = '" + ctx.getInputString("Status") + "'";
 			}
 			whereClause=whereClause + " order by status, legalLastName";
 
 			if ((ctx.getInputString("InMyRegion")!=null)&&(!ctx.getInputString("InMyRegion").equals(""))){		//for listing applicants on my region's projects
 				whereClause=whereClause + " where role like '1%'";
 			}					//end listing people on my region's projects
 			else {
 				Iterator persons = (new WsnApplication()).selectList(whereClause).iterator();
 
 				for (int i = 0; persons.hasNext(); i++) {
 					WsnApplication person = (WsnApplication) persons.next();
 					Hashtable temp = new Hashtable();
 					temp.put("Name",helper.value((String)person.getLegalFirstName()) + "&nbsp;" + helper.value((String)person.getLegalLastName()));
 					temp.put("Role",helper.value((String)person.getRole()));
 					temp.put("WsnApplicationID",helper.value((String)person.getWsnApplicationID()));
 					temp.put("CurrentEmail",helper.value((String)person.getCurrentEmail()));
 					temp.put("AccountNo",helper.value((String)person.getAccountNo()));
 					temp.put("ApplAccountNo",helper.value((String)person.getApplAccountNo()));
 					if (person.getIsStaff()) {
 						temp.put("IsStaff","T");
 					} else {
 						temp.put("IsStaff","F");
 					}
 					if(person.getSubmittedDate() != null) {
 						temp.put("SubmittedDate",helper.value((new SimpleDateFormat("MM/dd/yyyy")).format( person.getSubmittedDate())));
 						temp.put("Status",helper.value((String)person.getStatus()));
 					} else if (person.getIsStaff()) {
 						temp.put("SubmittedDate","Staff");
 						temp.put("Status",helper.value((String)person.getStatus()));
 					} else {
 						temp.put("SubmittedDate","Not Submitted");
 						temp.put("Status","N/A");
 					}
 					temp.put("UniversityFullName",helper.value((String)person.getUniversityFullName()));
 					temp.put("CurrentCity",helper.value((String)person.getCurrentCity()));
 					temp.put("CurrentState",helper.value((String)person.getCurrentState()));
 					temp.put("CurrentPhone",helper.value((String)person.getCurrentPhone()));
 //						WsnProject m = person.getIsMember();  // no longer works with dbio
 					if (person.getIsMemberId() != null) {
 						WsnProject m = person.getIsMember();
 						temp.put("Project",helper.value((String)m.getName()));
 					} else {
 						temp.put("Project","-");
 					}
 					h.put(String.valueOf(i), temp);
 				}
 			}						//end standard search
 			ctx.setSessionValue("tub", h);
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 	public void listInsurance (ActionContext ctx) {
 		Hashtable h = new Hashtable();
 		FormHelper helper = new FormHelper();
 		try {
 			String whereClause = "";
 			whereClause=whereClause + " insuranceReceived='0'";
 			whereClause=whereClause + " AND isStaff='0' AND wsnYear='"+ctx.getSessionValue("wsnYear")+"'";
 			whereClause=whereClause + " AND fk_isMember='"+ctx.getInputString("Project")+"'";
 			whereClause=whereClause + " ORDER BY legalLastName";
 			Iterator persons = (new WsnApplication()).selectList(whereClause).iterator();
 
 			int i = 0;
 			while(persons.hasNext()) {
 				WsnApplication person = (WsnApplication) persons.next();
 				Hashtable temp = new Hashtable();
 				String Name = person.getLegalFirstName() + " " + person.getLegalLastName();
 				temp.put("Name",helper.value((String)Name));
 				temp.put("WsnApplicationID",person.getWsnApplicationID());
 				WsnProject m = person.getIsMember();
 				if (m !=null){
 					temp.put("Assignment",helper.value((String)m.getWsnProjectID()));
 					temp.put("City",helper.value((String)m.getCity()));
 					temp.put("StartDate",m.getStudentStartDate()==null?"":(new SimpleDateFormat("MM/dd/yyyy")).format( m.getStudentStartDate() ));
 					temp.put("StopDate",m.getStudentEndDate()==null?"":(new SimpleDateFormat("MM/dd/yyyy")).format( m.getStudentEndDate() ));
 					temp.put("OperatingAccountNo",helper.value((String)m.getOperatingAccountNo()));
 				}
 				h.put(String.valueOf(i), temp);
 				i++;
 			}
 
 			h.put("number",String.valueOf(i));
 			ctx.setSessionValue("tub", h);
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 //	 Added 18 Nov 2004 by Scott Paulis
 	// returns a collection of the project specific questions for a specific project
 	private Collection listProjectQuestions(String projectID, String orderField, String orderDirection)
 			throws Exception {
 		boolean DESC = orderDirection.equals("DESC");
 		Question q = new Question();
 		Collection qs = q.selectList("fk_WsnProjectID = '" + projectID
 				+ "' ORDER BY "
 				+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 		return qs;
 	}
 	
 	private String fixOrderBy(String orderBy, String direction) {
 		return orderBy.replaceFirst("([\\w|\\.]*)", "$1 " + direction);
 	}
 	
 	public void listQuestions (ActionContext ctx) {
 		Hashtable h = new Hashtable();
 
 		try {
 			
 			String projectID = (String) ctx.getInputString("id",true);
 			String viewing = "";
 			int orderCol = 0;
 			String order = "";
 			String[] orderFields = { "displayOrder", "wsn_sp_QuestionText.body", "QuestionText.answerType",
 					"required", "wsn_sp_QuestionText.status" };
 
 			if (ctx.getInputString("orderCol") != null)
 				orderCol = Integer.parseInt(ctx.getInputString("orderCol"));
 			else
 				orderCol = 0;
 
 			if (ctx.getInputString("order") != null && ctx.getInputString("order").equals("DESC"))
 				order = "DESC";
 			else
 				order = "ASC";
 
 			h.put("orderCol", String.valueOf(orderCol));
 			h.put("order", order);
 			h.put("id",projectID);
 			//h.put("project", getProject(projectID));
 			
 			h.put("Questions",ObjectHashUtil.list(listProjectQuestions(projectID, orderFields[orderCol],order)));
 			
 			ctx.setSessionValue("tub", h);
 
 			ctx.goToView("listquestions");
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
 	}
 	
 	public void editQuestionDetails(ActionContext ctx) {
 		Hashtable h = new Hashtable();
 
 		try {
 			if (ctx.getInputString("questionID") != null) {
 				String questionID = ctx.getInputString("questionID");
 				String order = "";
 				int orderCol = 0;
 
 				if (ctx.getInputString("orderCol") != null)
 					orderCol = Integer.parseInt(ctx.getInputString("orderCol"));
 				else
 					orderCol = 0;
 
 				if (ctx.getInputString("order") != null && ctx.getInputString("order").equals("DESC"))
 					order = "DESC";
 				else
 					order = "ASC";
 
 				String projectID = (String) ctx.getInputString("id",true);
 				h.put("id", projectID);
 				
 				h.put("questionID", questionID);
 				h.put("orderCol", String.valueOf(orderCol));
 				h.put("order", order);
 				ctx.setSessionValue("tub", h);
 				ctx.goToView("editquestiondetails");
 			} else {
 				throw new Exception();
 			}
 		} catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
 	}
 	
 	public void updateQuestionOrder(ActionContext ctx) {
 		try {
 			int questionSize = Integer.parseInt(ctx.getInputString("questionSize"));
 			System.out.println("------> " + questionSize);
 			for (int i = 0; i < questionSize; i++) {
 				Hashtable q = new Hashtable();
 				q.put("QuestionID", ctx.getInputString(i + "QuestionID"));
 				q.put("DisplayOrder", ctx.getInputString(i + "DisplayOrder"));
 				updateQuestion(q);
 			}
 			listQuestions(ctx);
 		
 		} catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
 	}
 	public void newQuestion(ActionContext ctx) {
 		Hashtable h = new Hashtable();
 		
 		try {
 			
 			Question q = new Question();
 			QuestionText qt = new QuestionText();
 			qt.setStatus("custom");
 			q.setQuestionText(qt);
 			h.put("questionID", "");
 			h.put("order", ctx.getInputString("order"));
 			h.put("orderCol", ctx.getInputString("orderCol"));
 			
 			String projectID = (String) ctx.getInputString("id",true);
 			h.put("id", projectID);
 			
 			ctx.setSessionValue("tub", h);
 
 			ctx.goToView("editquestiondetails");
 		
 		} catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
 	}
 
 	public void saveQuestionDetails(ActionContext ctx) {
 		try {
 			if ("0".equals(ctx.getInputString("QuestionID"))) {
 				/* new question */
 				Hashtable tempQuestion = ctx.getHashedRequest();
 				tempQuestion.remove("order");
 				tempQuestion.remove("orderCol");
 				tempQuestion.remove("action");
 				tempQuestion.remove("id");
 				
 				saveQuestion(tempQuestion);
 			} else {  /* edit existing */
 				saveQuestion(ctx.getHashedRequest());
 			}
 			listQuestions(ctx);
 			
 		} catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
 	}
 	
 	public void deleteQuestion(ActionContext ctx) {
 		try {
 				if (ctx.getInputString("questionID") != null) {
 					String questionID = ctx.getInputString("questionID");
 					try {
 						deleteQuestion(questionID);
 					} catch (Exception e) {
 						System.err.println(e.toString());
 						e.printStackTrace();
 						ctx.goToView("error");
 					}
 					listQuestions(ctx);
 				} else {
 					ctx.goToView("error");
 				}
 		} catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
 	}
 
 	
 	public void saveQuestions(ActionContext ctx) {
 		
 		try {
 			int questionSize = ctx.getInputString("questionSize") == null ? 0
 					: Integer.parseInt(ctx.getInputString("questionSize"));
 			for (int i = 0; i < questionSize; i++) {
 				Hashtable q = new Hashtable();
 				q.put("QuestionID", ctx.getInputString(i + "QuestionID"));
 				q.put("DisplayOrder", ctx.getInputString(i + "DisplayOrder"));
 				updateQuestion(q);
 			}
 			
 			showProj(ctx);
 
 		} catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
 	}
 	
 	private Question newQuestion(Hashtable values) {
 		try {
 			if (values.containsKey("QuestionID")
 					&& values.containsKey("QuestionTextID")
 					&& values.containsKey("WsnProjectID")) {
 				QuestionText qt = new QuestionText();
 				if (!((String) values.get("QuestionTextID")).equals("0"))
 					qt = getQuestionText((String) values.get("QuestionTextID"));
 				values.remove("QuestionTextID");
 				Question q = new Question();
 				if (!((String) values.get("QuestionID")).equals("0"))
 					q = getQuestion((String) values.get("QuestionID"));
 				values.remove("QuestionID");
 				WsnProject p = getProject((String) values.get("WsnProjectID"));
 				values.remove("ConferenceID");
 				qt.setMappedValues(values);
 				boolean qtgood = qt.persist();
 				q.setMappedValues(values);
 				q.setWsnProject(p);
 				q.setQuestionText(qt);
 				q.insert();
 				return q;
 			} else
 				return null;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	private boolean saveQuestion(Hashtable values) {
 		try {
 			if (values.containsKey("QuestionID")
 					&& values.containsKey("QuestionTextID")
 					&& values.containsKey("ProjectID")) {
 				QuestionText qt = new QuestionText();
 				if (!((String) values.get("QuestionTextID")).equals("0"))
 					qt = getQuestionText((String) values.get("QuestionTextID"));
 				values.remove("QuestionTextID");
 				Question q = new Question();
 				if (!((String) values.get("QuestionID")).equals("0"))
 					q = getQuestion((String) values.get("QuestionID"));
 				values.remove("QuestionID");
 				WsnProject p = getProject((String) values.get("ProjectID"));
 				values.remove("ProjectID");
 				qt.setMappedValues(values);
 				boolean qtgood = qt.persist();
 				q.setMappedValues(values);
 				q.setWsnProject(p);
 				q.setQuestionText(qt);
 				return qtgood && q.persist();
 			} else
 				return false;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	private boolean deleteQuestion(String questionID) {
 		try {
 			Question q = getQuestion(questionID);
 			Answer a = new Answer();
 			a.setQuestion(q);
 			if ("common".equals(q.getQuestionText().getStatus())) {
 				return q.delete() && a.delete();
 			} else {
 				if (q.getQuestionText().delete())
 					return a.delete() && q.delete();
 				else
 					return false;
 			}
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	private boolean updateQuestion(Hashtable values) {
 		try {
 			Question q = getQuestion((String) values.get("QuestionID"));
 			q.setMappedValues(values);
 			return q.update();
 		} catch (Exception e) {
 			return false;
 		}
 	}
 	
 	private Answer getAnswer(int ID) {
 		try {
 			Answer a = new Answer();
 			a.setAnswerID(ID);
 			a.select();
 			return a;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	private Answer getAnswer(String ID) {
 		return getAnswer(Integer.parseInt(ID));
 	}
 
 	private Question getQuestion(int ID) {
 		try {
 			Question m = new Question();
 			m.setQuestionID(ID);
 			m.select();
 			return m;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	private Question getQuestion(String ID) {
 		return getQuestion(Integer.parseInt(ID));
 	}
 
 	private QuestionText getQuestionText(int ID) {
 		try {
 			QuestionText m = new QuestionText();
 			m.setQuestionTextID(ID);
 			m.select();
 			return m;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	private QuestionText getQuestionText(String ID) {
 		return getQuestionText(Integer.parseInt(ID));
 	}
 	
 	private WsnProject getProject(int ID) {
 		try {
 			WsnProject p = new WsnProject();
 			p.setWsnProjectIdInt(ID);
 			p.select();
 			return p;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	private WsnProject getProject(String projectIDString)
 			throws DBIOEntityException {
 		return getProject(Integer.parseInt(projectIDString.trim()));
 	}
 	
 	
 	
 	public void listProjects (ActionContext ctx) {
 		Hashtable h = new Hashtable();
 		FormHelper helper = new FormHelper();
 		try {
 			String whereClause = "";
 			String Region = ctx.getInputString("Region");
 			String AOA = ctx.getInputString("AOA");
 
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
 
 			if ((ctx.getInputString("City")!=null)&&(!ctx.getInputString("City").equals(""))){
 				whereClause=whereClause + " AND UPPER(city) like \'" + ctx.getInputString("City").toUpperCase() + "%\'";
 			}
 			if ((ctx.getInputString("Name")!=null)&&(!ctx.getInputString("Name").equals(""))){
 				whereClause=whereClause + " AND UPPER(name) like \'" + ctx.getInputString("Name").toUpperCase() + "%\'";
 			}
 			if ((ctx.getInputString("Country")!=null)&&(!ctx.getInputString("Country").equals(""))){
 				whereClause=whereClause + " AND country like \'" + ctx.getInputString("Country") + "%\'";
 			}
 			if (Region!=null){
 				whereClause=whereClause + " AND partnershipRegion = \'" + Region + "\'";
 			}
 			if (AOA!=null){
 				whereClause=whereClause + " AND AOA = \'" + AOA + "\'";
 			}
 			if ((ctx.getInputString("StartDate")!=null)&&(!ctx.getInputString("StartDate").equals(""))){
 				whereClause=whereClause + " AND studentStartDate like \'" + ctx.getInputString("StartDate") + "%\'";
 			}
 			if ((ctx.getInputString("EndDate")!=null)&&(!ctx.getInputString("EndDate").equals(""))){
 				whereClause=whereClause + " AND studentEndDate like \'" + ctx.getInputString("EndDate") + "%\'";
 			}
 			if ((ctx.getInputString("Year")!=null)&&(!ctx.getInputString("Year").equals(""))){
 				whereClause=whereClause + " AND wsnYear = \'" + ctx.getInputString("Year") + "\'";
 			}
 
 			whereClause=whereClause + " order by name, city";
 //				Iterator staffiter = staff.selectList("isSecure = 'F' AND firstname like '" + firstName + "%' AND lastname like '" + lastName + "%' AND preferredname like '" + preferredName + "%' ORDER BY lastname").iterator();
 			Iterator projects = (new WsnProject()).selectList(whereClause).iterator();
 
 			for (int i = 0; projects.hasNext(); i++) {
 				WsnProject project = (WsnProject) projects.next();
 				Hashtable temp = new Hashtable();
 				temp.put("WsnProjectID",project.getWsnProjectID());
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
 				String getIsPDId = project.getIsPDId();
 				if (getIsPDId ==null || getIsPDId.equals("0") || getIsPDId.equals("")) {
 					temp.put("PD","-");
 				} else {
 					WsnApplication pd = project.getIsPD();
 					temp.put("PD",helper.value((String)pd.getLegalFirstName())+" "+helper.value((String)pd.getLegalLastName()));
 				}
 				String getIsAPDId = project.getIsAPDId();
 				if (getIsAPDId ==null || getIsAPDId.equals("0") || getIsAPDId.equals("")) {
 					temp.put("APD","-");
 				} else {
 					WsnApplication apd = project.getIsAPD();
 					temp.put("APD",helper.value((String)apd.getLegalFirstName())+" "+helper.value((String)apd.getLegalLastName()));
 				}
 				String getIsCoordId = project.getIsCoordId();
 				if (getIsCoordId ==null || getIsCoordId.equals("0") || getIsCoordId.equals("")) {
 					temp.put("Coord","-");
 				} else {
 					WsnApplication coord = project.getIsCoord();
 					temp.put("Coord",helper.value((String)coord.getLegalFirstName())+" "+helper.value((String)coord.getLegalLastName()));
 				}
 				h.put(String.valueOf(i),temp);
 			}
 
 			ctx.setSessionValue("tub", h);
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 	public void listSpouse (ActionContext ctx) {
 		Hashtable h = new Hashtable();
 		FormHelper helper = new FormHelper();
 		try {
 			String whereClause = new String();
 			String firstName = ctx.getInputString("FirstName");
 			String lastName = ctx.getInputString("LastName");
 			String wsnYear = (String)ctx.getSessionValue("wsnYear");
 
 			whereClause = "UPPER(legalLastName) like '" + lastName.toUpperCase() + "%' AND NOT(UPPER(legalFirstName) like '" + firstName.toUpperCase() + "%') AND wsnYear='"+wsnYear+"'";
 
 			Iterator persons = (new WsnApplication()).selectList(whereClause).iterator();
 
 			for (int i = 0; persons.hasNext(); i++) {
 				WsnApplication person = (WsnApplication) persons.next();
 				Hashtable temp = new Hashtable();
 				temp.put("Name",helper.value((String)person.getLegalFirstName()) + "&nbsp;" + helper.value((String)person.getLegalLastName()));
 				temp.put("WsnApplicationID",person.getWsnApplicationID());
 				h.put(String.valueOf(i), temp);
 			}
 			h.put("WsnApplicationID", helper.value((String)ctx.getInputString("id")));
 			h.put("LegalFirstName", helper.value((String)ctx.getInputString("FirstName")));
 			h.put("LegalLastName", helper.value((String)ctx.getInputString("LastName")));
 
 			ctx.setSessionValue("tub", h);
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 	public void listStaffApps (ActionContext ctx) {
 		Hashtable h = new Hashtable();
 		try {
 			Staff staffList = new Staff();
 			staffList.setLastName(ctx.getInputString("LegalLastName"));
 			Iterator profiles = staffList.selectList().iterator();
 			
 			for (int i = 0; profiles.hasNext(); i++) {
 				Staff staff = (Staff) profiles.next();
 				Hashtable temp = new Hashtable();
 
 				temp.put("StaffProfileID","STAFFPROFILEID-NOTUSED"); //profile.getStaffSiteProfileID()); //kb 3/10
                 temp.put("StaffID",staff.getAccountNo()); //kb 3/10
                 
 				if ((staff.getFirstName() != null) && (staff.getLastName() != null)) {
 					temp.put("Name",staff.getFirstName() + "&nbsp;" + staff.getLastName());
 				}
 				if (staff.getEmail() != null) {
 					temp.put("CurrentEmail",staff.getEmail());
 				}
 				
 				temp.put("AccountNo",staff.getAccountNo());
 				temp.put("ApplAccountNo",staff.getAccountNo());
 				
 				OldAddress addr = staff.getPrimaryAddress();
 				if (addr != null) {											
 					if (addr.getAddress1() != null) {
 						temp.put("CurrentAddress",addr.getAddress1());
 					}
 					if (addr.getCity() != null) {
 						temp.put("CurrentCity",addr.getCity());
 					}
 					if (addr.getState() != null) {
 						temp.put("CurrentState",addr.getState());
 					}
 				}
 				
 				if (staff.getHomePhone() != null) {
 					temp.put("CurrentPhone",staff.getHomePhone());
 				}
 
 				//add the person to the hash
 				h.put(String.valueOf(i), temp);
 
 			}						//end standard search
 			ctx.setSessionValue("tub", h);
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		} catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 	public void listWaiver (ActionContext ctx) {
 		Hashtable h = new Hashtable();
 		FormHelper helper = new FormHelper();
 		try {
 			String whereClause = new String();
 			String wsnYear = (String)ctx.getSessionValue("wsnYear");
 			String projectInput = ctx.getInputString("Project");
 			whereClause=whereClause + " waiverReceived='0'";
 			whereClause=whereClause + " AND isStaff='0' AND wsnYear='"+wsnYear+"'";
 			whereClause=whereClause + " AND fk_isMember='"+projectInput+"'";
 			whereClause=whereClause + " ORDER BY legalLastName";
 
 			Iterator persons = (new WsnApplication()).selectList(whereClause).iterator();
 
 			int i = 0;
 			while(persons.hasNext()) {
 				WsnApplication person = (WsnApplication) persons.next();
 				Hashtable temp = new Hashtable();
 				String Name = person.getLegalFirstName() + " " + person.getLegalLastName();
 				temp.put("Name",helper.value((String)Name));
 				temp.put("WsnApplicationID",helper.value((String)person.getWsnApplicationID()));
 				WsnProject m = person.getIsMember();
 				if (m !=null){
 					temp.put("Assignment",m.getWsnProjectID());
 				}
 				h.put(String.valueOf(i), temp);
 				i++;
 			}
 
 			h.put("number",String.valueOf(i));
 			ctx.setSessionValue("tub", h);
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 	public void makeDownload (ActionContext ctx) {
         Hashtable h = new Hashtable();
 		FormHelper helper = new FormHelper();
 		try {
 			String personID = fetchPersonID(ctx.getRequest(),ctx.getSession());
 			String emailList = new String();
 
 			if (ctx.getInputString("id")!=null){
 				//get project database info
 				WsnProject project = new WsnProject(ctx.getInputString("id"));
 				h.put("DBName", helper.value((String)project.getName()));
 				String stringBuffer = helper.value((String)project.getName()) + "\n";
 
 				stringBuffer = stringBuffer + "City:\t" + helper.value((String)project.getCity()) + "\n";
 				stringBuffer = stringBuffer + "Country:\t" + helper.value((String)project.getCountry()) + "\n";
 				stringBuffer = stringBuffer + "PartnershipRegion:\t" + helper.value((String)project.getPartnershipRegion()) + "\n\n";
 				stringBuffer = stringBuffer + "Start Date\t" + helper.value((String)project.getStartDate()) +"\t\n";
 				stringBuffer = stringBuffer + "Stop Date\t" + helper.value((String)project.getStopDate()) +"\t\n\n";
 				
 				String getIsPDId = project.getIsPDId();
 				if (getIsPDId ==null || getIsPDId.equals("") || getIsPDId.equals("0")){
 					stringBuffer = stringBuffer + "PD\t-\t-\t\n";
 				} else {
 					WsnApplication pd = project.getIsPD();
 					stringBuffer = stringBuffer + "PD\t" + helper.value((String)pd.getLegalFirstName())+" "+helper.value((String)pd.getLegalLastName()) +"\t";
 					stringBuffer = stringBuffer + pd.getCurrentEmail() +"\t";
 				}
 				String getIsAPDId = project.getIsAPDId();
 				if (getIsAPDId ==null || getIsAPDId.equals("") || getIsAPDId.equals("0")){
 					stringBuffer = stringBuffer + "APD\t-\t-\t\n";
 				} else {
 					WsnApplication apd = project.getIsAPD();
 					stringBuffer = stringBuffer + "APD\t" + helper.value((String)apd.getLegalFirstName())+" "+helper.value((String)apd.getLegalLastName()) +"\t";
 					stringBuffer = stringBuffer + apd.getCurrentEmail() +"\t\n";
 				}
 				String getIsCoordId = project.getIsCoordId();
 				if (getIsCoordId ==null || getIsCoordId.equals("") || getIsCoordId.equals("0")){
 					stringBuffer = stringBuffer + "Coord\t-\t-\t\n";
 				} else {
 					WsnApplication coord = project.getIsCoord();
 					stringBuffer = stringBuffer + "Coord\t" + helper.value((String)coord.getLegalFirstName())+" "+helper.value((String)coord.getLegalLastName()) +"\t";
 					stringBuffer = stringBuffer + coord.getCurrentEmail() +"\t\n\n";
 				}
 
 				stringBuffer = stringBuffer + "number\t First Name\tLast Name\tGender\tBirthday\tStaff?\tEmail\tAddress\tAddress2\tCity\tState\tZip\tPhone\tCampus\tStatus\tAccountNo\tWaiverReceived\t InsuranceReceived\tMarried?\tChildren?\tEmergencyContact\tEmergencyAddress\tEmergencyCity\tEmergencyState\tEmergencyZip\tEmergencyPhone\tEmergencyWorkPhone\tEmergencyEmail\tPassport\tPassportCountry\tPassportIssueDate\tPassportExpDate\tVisa\tVisaCountry\tVisaIssueDate\tVisaExpDate\tVisaType\tVisaIsMultipleEntry\tDid The Applicant Go?\tParticipant Evaluation\tParticipant's Campus Region\tAccount Number\tAt This Address Until\tWilling For Different Project\tPreviously Participated In A Crusade Project?\tDate Became A Christian\tMajor\tClass\tGraduationDate\tEarliestAvailableDate\tDateMustReturn\tUsCitizen\tCitizenship\tIsApplicationComplete\t\n";
 
 				java.util.Iterator persons = project.getIsMember().iterator();
 				int i=0;
 				while (persons.hasNext()) {
 					WsnApplication person = (WsnApplication) persons.next();
 					if (person.getStatus()!=null){
 						if ((person.getStatus().equalsIgnoreCase("Pending"))||(person.getStatus().equalsIgnoreCase("Accepted")))	{
 							//only display people who are accepted or pending
 							stringBuffer = stringBuffer + String.valueOf(i) + "\t";
 							stringBuffer = stringBuffer + person.getLegalFirstName() + "\t" + person.getLegalLastName() + "\t";
 							stringBuffer = stringBuffer + person.getGender() + "\t";
 							stringBuffer = stringBuffer + person.getBirthdate() + "\t";
 							stringBuffer = stringBuffer + person.getIsStaff() + "\t";
 
 							stringBuffer = stringBuffer + person.getCurrentEmail() + "\t";
 							stringBuffer = stringBuffer + person.getCurrentAddress() + "\t";
 							stringBuffer = stringBuffer + person.getCurrentAddress2() + "\t";
 							stringBuffer = stringBuffer + person.getCurrentCity() + "\t";
 							stringBuffer = stringBuffer + person.getCurrentState() + "\t";
 							stringBuffer = stringBuffer + person.getCurrentZip() + "\t";
 							stringBuffer = stringBuffer + person.getCurrentPhone() + "\t";
 							stringBuffer = stringBuffer + person.getUniversityFullName() + "\t";
 
 							stringBuffer = stringBuffer + person.getStatus() + "\t";
 							if (!person.getIsStaff()) {
 								stringBuffer = stringBuffer + person.getApplAccountNo() + "\t";
 								stringBuffer = stringBuffer + person.getWaiverReceived() + "\t";
 								stringBuffer = stringBuffer + person.getInsuranceReceived() + "\t";
 							}
 							else {
 								stringBuffer = stringBuffer + "-\t";
 								stringBuffer = stringBuffer + "-\t";
 								stringBuffer = stringBuffer + "-\t";
 
 							}
 							stringBuffer = stringBuffer + WsnPerson.translateMaritalStatus(person.getMaritalStatus()) + "\t";
 							stringBuffer = stringBuffer + person.getChild() + "\t";
 
 							stringBuffer = stringBuffer + person.getEmergName() + "\t";
 							stringBuffer = stringBuffer + person.getEmergAddress() + "\t";
 							stringBuffer = stringBuffer + person.getEmergCity() + "\t";
 							stringBuffer = stringBuffer + person.getEmergState() + "\t";
 							stringBuffer = stringBuffer + person.getEmergZip() + "\t";
 							stringBuffer = stringBuffer + person.getEmergPhone() + "\t";
 							stringBuffer = stringBuffer + person.getEmergWorkPhone() + "\t";
 							stringBuffer = stringBuffer + person.getEmergEmail() + "\t";
 
 							// 4-3-03 kl: added optional items
 							stringBuffer = stringBuffer + person.getPassportNo() + "\t";
 							stringBuffer = stringBuffer + person.getPassportCountry() + "\t";
 							stringBuffer = stringBuffer + person.getPassportIssueDate() + "\t";
 							stringBuffer = stringBuffer + person.getPassportExpirationDate() + "\t";
 							stringBuffer = stringBuffer + person.getVisaNo() + "\t";
 							stringBuffer = stringBuffer + person.getVisaCountry() + "\t";
 							stringBuffer = stringBuffer + person.getVisaIssueDate() + "\t";
 							stringBuffer = stringBuffer + person.getVisaExpirationDate() + "\t";
 							stringBuffer = stringBuffer + person.getVisaType() + "\t";
 							stringBuffer = stringBuffer + person.getVisaIsMultipleEntry() + "\t";
 							stringBuffer = stringBuffer + person.getDidGo() + "\t";
 							stringBuffer = stringBuffer + person.getParticipantEvaluation() + "\t";
 
 							// 4-3-03 kl: remaining page items
 							stringBuffer = stringBuffer + person.getRegion() + "\t";
 							stringBuffer = stringBuffer + person.getApplAccountNo() + "\t";
 							// stringBuffer = stringBuffer + person.getDateAddressGoodUntil() + "\t";
 							stringBuffer = stringBuffer + person.getWillingForDifferentProject() + "\t";
 							stringBuffer = stringBuffer + person.getPrevIsp() + "\t";
 							stringBuffer = stringBuffer + person.getDateBecameChristian() + "\t";
 							stringBuffer = stringBuffer + person.getMajor() + "\t";
 							stringBuffer = stringBuffer + person.getYearInSchool() + "\t";
 							stringBuffer = stringBuffer + person.getGraduationDate() + "\t";
 							stringBuffer = stringBuffer + person.getEarliestAvailableDate() + "\t";
 							stringBuffer = stringBuffer + person.getDateMustReturn() + "\t";
 							stringBuffer = stringBuffer + person.getUsCitizen() + "\t";
 							stringBuffer = stringBuffer + person.getCitizenship() + "\t";
 							stringBuffer = stringBuffer + person.getIsApplicationComplete() + "\t";
 
 							stringBuffer = stringBuffer + "\t\n";
 							i++;
 						}
 					} // done with accepted/pending person, ignore withdrawn/rejected
 				}
 				String theFile = "/wsnsp/" + (String)ctx.getInputString("id")+ ".txt";
 					h.put("test",theFile);
 				try {
 					FileWriter file = new FileWriter (getServletContext().getRealPath(theFile));
 					file.write(stringBuffer);
 					file.close();
 					h.put("filename",theFile);
 				}
 				catch (IOException e){
 					h.put("filename","ERROR");
 				}
 			} else if (ctx.getInputString("region")!=null){
 				//get regional database info
 				String region = ctx.getInputString("region");
 				String wsnYear = (String)ctx.getSessionValue("wsnYear");
 
 				h.put("DBName", helper.value((String)ctx.getInputString("region")));
 
 				Iterator projects = (new WsnProject()).selectList("partnershipRegion=\""+region+"\" and wsnYear=\""+wsnYear+"\" order by name").iterator();
 
 				int i=0;
 				String stringBuffer = (String)ctx.getInputString("region") + "\n";
 				stringBuffer = stringBuffer + "number\tprojectname\tcountry\tcity\tPD\tPDemail\tAPD\tAPDemail\tCoord\tCoordEmail\tstudentStartDate\tstudentEndDate\tscholarshipaccountno\toperatingaccountno\taoa\tstaffcost\tstudentcost\tsecure\t\n\n";
 
 				while (projects.hasNext()) {
 					WsnProject project = (WsnProject) projects.next();
 
 					stringBuffer = stringBuffer + String.valueOf(i) + "\t";
 					stringBuffer = stringBuffer + project.getName() + "\t";
 					stringBuffer = stringBuffer + project.getCountry() + "\t";
 					stringBuffer = stringBuffer + project.getCity() + "\t";
 
 					String getIsPDId = project.getIsPDId();
 					if (getIsPDId ==null || getIsPDId.equals("") || getIsPDId.equals("0")){
 						stringBuffer = stringBuffer + "-\t-\t";
 					} else {
 						WsnApplication pd = project.getIsPD();
 						stringBuffer = stringBuffer + helper.value((String)pd.getLegalFirstName())+" "+helper.value((String)pd.getLegalLastName()) +"\t";
 						stringBuffer = stringBuffer + pd.getCurrentEmail() +"\t";
 					}
 					String getIsAPDId = project.getIsAPDId();
 					if (getIsAPDId ==null || getIsAPDId.equals("") || getIsAPDId.equals("0")){
 						stringBuffer = stringBuffer + "-\t-\t";
 					} else {
 						WsnApplication apd = project.getIsAPD();
 						stringBuffer = stringBuffer + helper.value((String)apd.getLegalFirstName())+" "+helper.value((String)apd.getLegalLastName()) +"\t";
 						stringBuffer = stringBuffer + apd.getCurrentEmail() +"\t";
 					}
 					String getIsCoordId = project.getIsCoordId();
 					if (getIsCoordId ==null || getIsCoordId.equals("") || getIsCoordId.equals("0")){
 						stringBuffer = stringBuffer + "-\t-\t";
 					} else {
 						WsnApplication coord = project.getIsCoord();
 						stringBuffer = stringBuffer + helper.value((String)coord.getLegalFirstName())+" "+helper.value((String)coord.getLegalLastName()) +"\t";
 						stringBuffer = stringBuffer + coord.getCurrentEmail() +"\t";
 					}
 					stringBuffer = stringBuffer + project.getStartDate() + "\t";
 					stringBuffer = stringBuffer + project.getStopDate() + "\t";
 					stringBuffer = stringBuffer + project.getScholarshipAccountNo() + "\t";
 					stringBuffer = stringBuffer + project.getOperatingAccountNo() + "\t";
 					stringBuffer = stringBuffer + project.getAOA() + "\t";
 					stringBuffer = stringBuffer + project.getStaffCost() + "\t";
 					stringBuffer = stringBuffer + project.getStudentCost() + "\t";
 					stringBuffer = stringBuffer + project.getSecure() + "\t";
 					stringBuffer = stringBuffer + "\t\n";
 
 					i++;
 				}
 				String theFile = "/wsnsp/" + (String)ctx.getInputString("region")+ ".txt";
 					h.put("test",theFile);
 				try {
 					FileWriter file = new FileWriter (getServletContext().getRealPath(theFile));
 					file.write(stringBuffer);
 					file.close();
 					h.put("filename",theFile);
 				}
 				catch (IOException e){
 					System.err.println(e.toString());
 					h.put("filename","ERROR");
 				}
 
 			}
 			else{
 				//invalid!!!
 			}
 
 			ctx.setSessionValue("tub", h);
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		} catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 /*	protected Hashtable obj2hash(Object o) {
         Hashtable h = new Hashtable();
         Object[] arguments = new Object[] {};
         Class c = o.getClass();
         Method[] publicMethods = c.getMethods();
         for (int i = 0; i < publicMethods.length; i++) {
             String methodName = publicMethods[i].getName();
 			if (methodName.startsWith("get") && (!methodName.equals("getEJBHome")) && (!methodName.equals("getHandle")) && (!methodName.equals("getPrimaryKey") && (!methodName.equals("getClass")) && (!methodName.equals("getIsMember")) && (!methodName.equals("getWsnChild")) && (!methodName.equals("getIsCoord") && (!methodName.equals("getIsPD")) && (!methodName.equals("getIsAPD"))))  ) {
                 String key = methodName.substring(3);
                 try {
                     Method m = c.getMethod(methodName, null);
                     h.put(key, m.invoke(o, arguments));
                 } catch (IllegalAccessException e) {
                     System.out.println(e);
                 } catch (InvocationTargetException e) {
                     System.out.println(e);
                 } catch (NoSuchMethodException e) {
                     System.out.println(e);
                 } catch (IllegalArgumentException e) {
                     System.out.println(methodName + " " + e);
                 } catch (NullPointerException e) {
                     //e.printStackTrace();
                 }
             }
         }
         return h;
     }
 */
     protected void req2ejb(javax.servlet.http.HttpServletRequest request, Object o) {
         Class c = o.getClass();
         Object[] arguments = null;
         Method[] methods = c.getMethods();
         Hashtable parameterTypes = new Hashtable();
 
         for (int i = 0; i < methods.length; i++) {
             if (methods[i].getName().startsWith("set")) {
                 parameterTypes.put(methods[i].getName(), methods[i].getParameterTypes());
             }
         }
 
         for (Enumeration enumer = request.getParameterNames(); enumer.hasMoreElements();) {
             String attr = (String) enumer.nextElement();
             try {
 		if (request.getParameter(attr).equals("true") || request.getParameter(attr).equals("false")) {
 		    arguments = new Object[] {Boolean.class};
 		    arguments[0] = new Boolean(request.getParameter(attr));
 		} else {
 		    arguments = new Object[] {request.getParameter(attr).getClass()};
 		    arguments[0] = request.getParameter(attr);
 		}
                 Method m = c.getMethod("set" + attr, (Class[]) parameterTypes.get("set" + attr));
                 m.invoke(o, arguments);
             } catch (IllegalAccessException e) {
                 System.err.println(e);
             } catch (InvocationTargetException e) {
                 System.err.println(e);
             } catch (IllegalArgumentException e) {
                 System.err.println(e);
             } catch (NoSuchMethodException e) {
                 //Often this is okay
                 System.err.println(e);
             } catch (NullPointerException e) {
                 e.printStackTrace();
             }
         }
     }
 
 	public void saveEditProjLeaders (ActionContext ctx) {
 		try {
 			if (ctx.getInputString("id") != null) {
 				WsnProject project = new WsnProject(ctx.getInputString("id"));
 
 				// clear old pd, apd and coord
 				if (project.getIsPDId() != null && !project.getIsPDId().equals("") && !project.getIsPDId().equals("0")) {
 					WsnApplication oldPerson = project.getIsPD();
 					project.setIsPD(null);
 					oldPerson.setIsMember(null);
 					oldPerson.setRole("1");
 					oldPerson.persist();
 				}
 				if (project.getIsAPDId() != null && !project.getIsAPDId().equals("") && !project.getIsAPDId().equals("0")) {
 					WsnApplication oldPerson = project.getIsAPD();
 					project.setIsAPD(null);
 					oldPerson.setIsMember(null);
 					oldPerson.setRole("1");
 					oldPerson.persist();
 				}
 				if (project.getIsCoordId() != null && !project.getIsCoordId().equals("") && !project.getIsCoordId().equals("0")) {
 					WsnApplication oldPerson = project.getIsCoord();
 					project.setIsCoord(null);
 					String whereClause = "fk_IsCoord="+oldPerson.getWsnApplicationID()+" and wsnYear='"+oldPerson.getWsnYear()+"' order by name";
 					Collection proj = new WsnProject().selectList(whereClause);
 					if (proj.size() == 1) {
 						oldPerson.setRole("1");
 					}
 					oldPerson.persist();
 				}
 
 				project.persist();
 
 				//THEN associate the new pd, apd and coord
 				if (!ctx.getInputString("pd").equals("")) {        //dissoc new leader from their old projects
 					WsnApplication person = new WsnApplication(ctx.getInputString("pd"));
 					if (person.getIsPDId() != null && !person.getIsPDId().equals("0")) {
 						WsnProject oldProject = person.getIsPD();
 						oldProject.setIsPD(null);
 						oldProject.persist();
 					}
 					if (person.getIsMemberId() != null) {
 						person.setIsMember(null);
 					}
 
 					//assoc new leader with this project
 					project.setIsPD(person);
 					person.setIsMember(project);
 					person.setStatus("Accepted");
 					person.setRole("3");
 					
 					person.persist();
 				}
 
 				if (!ctx.getInputString("apd").equals("")) {
 					//dissoc new leader from their old projects
 					WsnApplication person = new WsnApplication(ctx.getInputString("apd"));
 					if (person.getIsAPD() != null && !person.getIsAPD().equals("0")) {
 						WsnProject oldProject = person.getIsAPD();
 						oldProject.setIsAPD(null);
 						oldProject.persist();
 					}
 					if (person.getIsMember() != null) {
 						person.setIsMember(null);
 					}
 
 					//assoc new leader with this project
 					project.setIsAPD(person);
 					person.setIsMember(project);
 					person.setStatus("Accepted");
 					person.setRole("2");
 					
 					person.persist();
 				}
 				if (!ctx.getInputString("coord").equals("")) {
 					WsnApplication person = new WsnApplication(ctx.getInputString("coord"));
 					project.setIsCoord(person);
 					person.setRole("4");
 					
 					person.persist();
 				}
 				project.persist();
 			}
 			showProj(ctx);
 		} catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 	public void sendEmail (ActionContext ctx) {
 		try {
 			SendMessage email = new SendMessage();
 			System.out.println(ctx.getInputString("to"));
 			email.setBcc(ctx.getInputString("to") + ", " + ctx.getInputString("from"));
 			email.setFrom(ctx.getInputString("from"));
 			email.setSubject(ctx.getInputString("subject"));
 			email.setBody(ctx.getInputString("bodytext"));
 			email.send();
 
 			System.err.println("sendEmail to: " + ctx.getInputString("to"));
 
 			String next = ctx.getInputString("frompage");
 			if (next.equals("showMainProject"))	{
 				showMainProj(ctx);
 			} else if (next.equals("showTeam")) {
 				showTeam(ctx);
 			}
 		} catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 	
 	public void showAcctNo (ActionContext ctx) {
 		Hashtable h = new Hashtable();
 		FormHelper helper = new FormHelper();
 		DateFormat date_format = new SimpleDateFormat("yyyyMMdd"); 
 		Date now = new Date(); 
 		String today = date_format.format(now);
 		int	count = 0;
 		try {
 			WsnApplication person = new WsnApplication();
 			person.localinit();
			Iterator peopleNeedingAcctNo = person.selectSQLList("SELECT person.WsnApplicationID, person.LegalFirstName, person.gender, person.title, person.fk_wsnspouse, person.LegalLastName, person.AccountNo, person.applAccountNo, person.CurrentAddress, person.CurrentCity, person.CurrentState, person.CurrentZip, person.Ssn, person.dateCreated, person.fk_ismember, project.WsnProjectID, project.name, project.ScholarshipAccountNo, project.scholarshipBusinessUnit,  project.scholarshipOperatingUnit, project.scholarshipDeptID, project.scholarshipProjectID, project.scholarshipDesignation, person.wsnyear from wsn_sp_viewApplication as person, wsn_sp_WsnProject as project where person.fk_ismember = project.wsnprojectid and person.wsnYear=\'"+currentWsnYear+"\' and person.status='Accepted' and person.isStaff='0' and (len(rtrim(person.applAccountNo)) = 0 or person.applAccountNo is null) and (person.fk_childof = 0 or len(rtrim(person.fk_childof)) = 0) and project.scholarshipDesignation > '1000000' and project.scholarshipDesignation < '3000000' and (person.fk_wsnspouse = 0 or (person.fk_wsnspouse <> 0 and person.gender = '1')) order by person.acceptedDate, person.LegalLastName, person.LegalFirstName").iterator();
 
 			//String tmpString = "";
 			String buf = "OPER_NAME\tKEYED_DATE\tPEOPLE_ID\tSTATUS\tORG_ID\t" +
 			"PERSON_TYPE\tTITLE\tFIRST_NAME\tMIDDLE_NAME\tLAST_NAME_ORG\tSUFFIX\t" +
 			"SPOUSE_TITLE\tSPOUSE_FIRST\tSPOUSE_MIDDLE\tSPOUSE_LAST\tSPOUSE_SUFFIX\t" +
 			"ADDRESS1\tADDRESS2\tADDRESS3\tCITY\tSTATE\tZIP\tTELEPHONE\tTELE_TYPE\t" +
 			"COUNTRY\tINTL_ZIP\tADDR_TYPE\tADDR_START\tADDR_STOP\tADDR_NAME\tSALUTATION\t" +
 			"EMAIL\tEMAIL_TYPE\tPRIM_EMAIL\tSVC_IND_MAIL\tSVC_IND_TELE\tSOURCE_MOTIV\t" +
 			"SOURCE_DATE\tMIN_MAIL_IND\tMIN_TELE_IND\tMIN_SVC_CODE\tMIN_SVC_TYPE\t" +
 			"MIN_SVC_DATE\tMIN_SVC_DESC\tAMT_PAID\tLIST_ID\tWSN_APPLICATION_ID\tASSIGNMENT_NAME\t" +
 			"SCHOLARSHIP_BUSINESS_UNIT\tSCHOLARSHIP_OPERATING_UNIT\t" + 
 			"SCHOLARSHIP_DEPT_ID\tSCHOLARSHIP_PROJECT_ID\tSCHOLARSHIP_DESIGNATION\n";
 			while(peopleNeedingAcctNo.hasNext()) {
 				count++;
 				WsnApplication personNeedingAcctNo = (WsnApplication) peopleNeedingAcctNo.next();
 				String personID = helper.value(personNeedingAcctNo.getWsnApplicationID());
 				Hashtable<String,String> temp = new Hashtable<String,String>();
 				
 				String Name = helper.value(personNeedingAcctNo.getLegalFirstName());
 				String wifeID = helper.value(personNeedingAcctNo.getWsnSpouseId());
 				String spouse_title = "";
 				String spouse_first = "";
 				String spouse_last = "";
 				if (!wifeID.equals("0")){  //married
 					WsnApplication wife = new WsnApplication(wifeID);
 					if (!wife.getLegalFirstName().equals("")) {
 						Name = Name + " and " + wife.getLegalFirstName();
 						spouse_title = wife.getPeoplesoftTitle(); // SP0USE_TITLE
 						spouse_first = helper.value(wife.getLegalFirstName());	// SP0USE_FIRST
 						spouse_last = helper.value(wife.getLegalLastName());	// SP0USE_LAST
 					}
 				}
 				Name = Name + " " + helper.value(personNeedingAcctNo.getLegalLastName());
 				//WsnProject assignedProject = personNeedingAcctNo.getIsMember(); 
 				// Set up a row in our csv file
 				Hashtable profile = (Hashtable)ctx.getSessionValue("profile");
 				buf += profile.get("FirstName").toString().replace('\t',' ')+" "+profile.get("LastName").toString().replace('\t',' ')+"\t";// OPER_NAME
 				buf += today + "\t";// KEYED_DATE
 				buf += helper.value(personNeedingAcctNo.getAccountNo()) + "\t"; // PEOPLE_ID
 				buf += "\t";	// STATUS
 				buf += "CAMPUS\t";	// ORG_ID
 				buf += "P\t"; // PERSON_TYPE
 				buf += personNeedingAcctNo.getPeoplesoftTitle()+"\t"; // TITLE
 				buf += helper.value(personNeedingAcctNo.getLegalFirstName()).replace('\t',' ') + "\t";	// FIRST_NAME
 				buf += "\t"; // MIDDLE_NAME
 				buf += helper.value(personNeedingAcctNo.getLegalLastName()).replace('\t',' ') + "\t";	// LAST_NAME_ORG
 				buf += "\t"; // SUFFIX
 				buf += spouse_title+"\t"; // SPOUSE_TITLE
 				buf += spouse_first.replace('\t',' ')+"\t"; // SPOUSE_FIRST
 				buf += "\t";	// SPOUSE_MIDDLE
 				buf += spouse_last.replace('\t',' ')+"\t"; // SPOUSE_LAST
 				buf += "\t"; // SPOUSE_SUFFIX
 				buf += "\""+helper.value(personNeedingAcctNo.getCurrentAddress()).replace('\t',' ').replace('\t',' ') + "\"\t"; // ADDRESS1
 				buf += "\t"; // ADDRESS2
 				buf += "\t"; // ADDRESS3
 				buf += helper.value(personNeedingAcctNo.getCurrentCity()).replace('\t',' ') + "\t"; // CITY
 				buf += helper.value(personNeedingAcctNo.getCurrentState()).replace('\t',' ') + "\t"; // STATE
 				buf += helper.value(personNeedingAcctNo.getCurrentZip()).replace('\t',' ') + "\t"; // ZIP
 				buf += helper.value(personNeedingAcctNo.getCurrentPhone()).replace('\t',' ') + "\t"; // TELEPHONE
 				buf += "\t"; // TELE_TYPE
 				buf += "USA\t"; // COUNTRY
 				buf += "\t"; // INTL_ZIP
 				buf += "PRIM\t"; // ADDR_TYPE
 				buf += today+"\t"; // ADDR_START
 				buf += "\t"; // ADDR_STOP
 				buf += "\t"; // ADDR_NAME
 				buf += "\t"; // SALUTATION
 				buf += helper.value(personNeedingAcctNo.getCurrentEmail()).replace('\t',' ') + "\t"; // EMAIL
 				buf += "HM\t"; // EMAIL_TYPE 
 				buf += "Y\t"; // PRIM_EMAIL 
 				buf += "0\t"; // SVC_IND_MAIL 
 				buf += "0\t"; // SVC_IND_TELE 
 				buf += "USLOAD\t"; // SOURCE_MOTIV
 				buf += today+"\t"; // SOURCE_DATE
 				buf += "\t"; // MIN_MAIL_IND
 				buf += "\t"; // MIN_TELE_IND
 				buf += "\t"; // MIN_SVC_CODE
 				buf += "\t"; // MIN_SVC_TYPE
 				buf += "\t"; // MIN_SVC_DATE
 				buf += "\t"; // MIN_SVC_DESC
 				buf += "\t"; // AMT_PAID
 				buf += "\t"; // LIST_ID
 				buf += helper.value((String)personID) + "\t"; // WSN_APPLICATION_ID
 				buf += helper.value(personNeedingAcctNo.getProjectName()).replace('\t',' ') + "\t"; // ASSIGNMENT_NAME
 				buf += helper.value(personNeedingAcctNo.getScholarshipBusinessUnit()).replace('\t',' ').toUpperCase() + "\t"; // SCHOLARSHIP_BUSINESS_UNIT
 				buf += helper.value(personNeedingAcctNo.getScholarshipOperatingUnit()).replace('\t',' ').toUpperCase() + "\t"; // SCHOLARSHIP_OPERATING_UNIT
 				buf += helper.value(personNeedingAcctNo.getScholarshipDeptID()).replace('\t',' ').toUpperCase() + "\t"; // SCHOLARSHIP_DEPT_ID
 				if(personNeedingAcctNo.getScholarshipProjectID().toUpperCase().equals("NONE")) {
 					buf += "\t";
 				} else {
 					buf += helper.value(personNeedingAcctNo.getScholarshipProjectID()).replace('\t',' ').toUpperCase() + "\t"; // SCHOLARSHIP_PROJECT_ID
 				}
 				buf += helper.value(personNeedingAcctNo.getScholarshipDesignation()).replace('\t',' ') + "\n"; // SCHOLARSHIP_DESIGNATION
 			}
 			String theFile = "/wsnsp/tmp/sp_need_account_no.txt";
 			try {
 				FileWriter file = new FileWriter (getServletContext().getRealPath(theFile));
 				file.write(buf);
 				file.close();
 			}
 			catch (IOException e){
 				System.err.println(e.toString());
 				e.printStackTrace();
 				ctx.goToView("error");
 				return;
 			}
 			//ctx.getResponse().getWriter().println(buf);
 			ctx.setSessionValue("tub", h);
 			ctx.setSessionValue("count", count);
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 	public void showAddKids (ActionContext ctx) {
 		Hashtable h = new Hashtable();
 		FormHelper helper = new FormHelper();
 		try {
 			WsnApplication person = new WsnApplication(ctx.getInputString("id"));
 			h = ObjectHashUtil.obj2hash(person);
 			WsnApplication spouse = person.getWsnSpouse();
 			String SpouseFirstName = "";
 			if (spouse!=null){  //this should not occur!
 				SpouseFirstName=spouse.getLegalFirstName();
 			}
 
 			h.put("SpouseFirstName", SpouseFirstName);
 
 			if (person.getChild()) {		//children are going also!
 				java.util.Iterator children = person.getWsnChild().iterator();
 				for (int i = 0; children.hasNext(); i++) {
 					WsnApplication child = (WsnApplication) children.next();
 					Hashtable temp = new Hashtable();
 					temp.put("Name",helper.value((String)child.getLegalFirstName()));
 					temp.put("WsnApplicationID",helper.value((String)child.getWsnApplicationID()));
 					h.put(String.valueOf(i), temp);
 				}
 				h.put("Children", "true");
 			}
 			else{
 				h.put("Children", "false");
 			}
 
 			//drop down box information
 			String lastName = ctx.getInputString("LastName");
 			String firstName = ctx.getInputString("FirstName");
 			String wsnYear = (String)ctx.getSessionValue("wsnYear");
 
 
 			// Because Castor's forward-only cursor can't handle text(blob) fields when getting multiple objects
 			String whereClause = "UPPER(legalLastName) like '" + lastName.toUpperCase() + "%' AND NOT(UPPER(legalFirstName) like '" + firstName.toUpperCase() + "%') AND NOT(UPPER(legalFirstName) like '" + SpouseFirstName.toUpperCase() + "%') AND role like '1%' AND wsnYear='"+wsnYear+"'";
 			Iterator persons = (new WsnApplication()).selectList(whereClause).iterator();
 
 			for (int i = 0; persons.hasNext(); i++) {
 				person = (WsnApplication) persons.next();
 				Hashtable temp = new Hashtable();
 				temp.put("Name",person.getLegalFirstName() + "&nbsp;" + person.getLegalLastName());
 				temp.put("WsnApplicationID",person.getWsnApplicationID());
 				h.put("select" + String.valueOf(i), temp);
 			}
 
 			ctx.setSessionValue("tub", h);
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 	public void showIndex (ActionContext ctx) {
 		try {
 			Hashtable h = new Hashtable();
 			FormHelper helper = new FormHelper();
 
 			if (ctx.getSessionValue("wsnYear") == null)	{
 				ctx.setSessionValue("wsnYear", currentWsnYear);
 			}
 
 			String personID = fetchPersonID(ctx.getRequest(),ctx.getSession());
 			String wsnYear = (String)ctx.getSessionValue("wsnYear");
 				try {
 					WsnApplication person = null;
 					if (!personID.equals(" ")) {
 						person = new WsnApplication(personID);
 					}
 					if (person!=null) {
 						java.util.Iterator projects = null;
 						if (person.getRole().equals("3")) {
 							try
 							{
 								WsnProject project = new WsnProject();
 								project.setWsnYear((String)ctx.getSessionValue("wsnYear"));
 								project.setIsPDIdStr(personID);
 								project.select();
 								h.put("WsnProjectID",project.getWsnProjectID());
 								h.put("Access","pdapd");
 								ctx.setSessionValue("MyHomeAction","showTeam&view=team&id=" + (String)h.get("WsnProjectID"));
 							} catch (Exception e) {
 								h.put("WsnProjectID","notassigned");
 								h.put("Access","pdapd");
 							}
 						} else if (person.getRole().equals("2")) {
 							try
 							{
 								WsnProject project = new WsnProject();
 								project.setWsnYear((String)ctx.getSessionValue("wsnYear"));
 								project.setIsAPDIdStr(personID);
 								project.select();
 								h.put("WsnProjectID",project.getWsnProjectID());
 								h.put("Access","pdapd");
 								ctx.setSessionValue("MyHomeAction","showTeam&view=team&id=" + (String)h.get("WsnProjectID"));
 							} catch (Exception e) {
 								System.err.println("wsnsp - director but has no project assignment");
 								h.put("WsnProjectID","notassigned");
 								h.put("Access","pdapd");
 							}
 						} else if (person.getRole().equals("4")) {
 							h.put("Access","coord");
 							ctx.setSessionValue("MyHomeAction","showMainProj&view=adminprojects");
 						} else {
 							h.put("Access","rdcoord");
 							ctx.setSessionValue("MyHomeAction","showMainProj&view=adminprojects");
 						}
 					} else {
 						h.put("Access", "Denied");
 					}
 
 					ctx.setSessionValue("tub", h);
 					ctx.goToView("index");
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 		} catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
 	}
 
 	public void showInsuranceProjects (ActionContext ctx) {
 		Hashtable h = new Hashtable();
 		FormHelper helper = new FormHelper();
 
 		try {
 			WsnProject project = new WsnProject();
 			Iterator projects = project.selectList("wsnYear='"+currentWsnYear+"' order by departDateFromGateCity").iterator();
 			for (int i = 0; projects.hasNext(); i++) {
 				project = (WsnProject) projects.next();
 				Hashtable temp = new Hashtable();
 				temp.put("WsnProjectID", helper.value(project.getWsnProjectID()));
 				temp.put("Name", helper.value(project.getName()));
 				temp.put("PartnershipRegion", helper.value(project.getPartnershipRegion()));
 				if ((project.getStartDate() == null)  || (project.getStartDate().equals(""))) {
 					temp.put("StartDate", "");
 				} else {
 					temp.put("StartDate", (new SimpleDateFormat("MM/dd/yyyy")).format(project.getStartDate()));
 				}
 				if ((project.getStopDate() == null) || (project.getStopDate().equals(""))) {
 					temp.put("StopDate", "");
 				} else {
 					temp.put("StopDate", (new SimpleDateFormat("MM/dd/yyyy")).format(project.getStopDate()));
 				}
 				temp.put("ReturnDate", helper.value(project.getDepartureDateFromLocation()));  //needed for world travel
 				h.put(String.valueOf(i),temp);
 			}
 			ctx.setSessionValue("tub", h);
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		} catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 	public void showMainProj (ActionContext ctx) {
         Hashtable h = new Hashtable();
 		FormHelper helper = new FormHelper();
 		Hashtable emails = new Hashtable();
 		try {
 			String personID = fetchPersonID(ctx.getRequest(),ctx.getSession());
 			String emailList = new String();
 			String wsnYear = (String)ctx.getSessionValue("wsnYear");
 
 			emailList = "wsn@uscm.org";
 
 			// check the surfer's role in the wsnsp system
 			if (!personID.equals(" ")) {
 				WsnApplication person = new WsnApplication(personID);
 				String whereClause = "";
 				if (person.getRole().equals("4")&&(ctx.getInputString("region")==null)) {
 					// coordinator: return all projects where person is the coordinator
 					whereClause = "fk_IsCoord='"+personID+"' and wsnYear='"+wsnYear+"' order by name";
 				} else if (person.getRole().equals("6")) {
 					// Uber user
 					whereClause = "wsnYear='"+wsnYear+"' order by name";
 				} else {
 					// regional director: return all projects in person's region
 					whereClause = "partnershipRegion='"+person.getRegion()+"' and wsnYear='"+wsnYear+"' order by name";
 				}
 				
 				Iterator projects = (new WsnProjectLeader()).selectList(whereClause).iterator();
 	
 				// load up my hashtable with the project information
 				int i=0;
 	
 				while (projects.hasNext()) {
 					WsnProjectLeader project = (WsnProjectLeader) projects.next();
 	
 					Hashtable temp = new Hashtable();
 					temp.put("WsnProjectID",project.getWsnProjectID());
 					temp.put("Name",project.getName());
 					temp.put("Country",helper.value((String)project.getCountry()));
 
 					if (project.getPDWsnApplicationID() == null || project.getPDWsnApplicationID().equals("")) {
 						temp.put("PD","");
 					} else {
 						temp.put("PD",helper.value(project.getPDFirstName())+" "+helper.value(project.getPDLastName()));
 						temp.put("PDid",project.getPDWsnApplicationID());
 						//add email address to hash checking to see if it is already in there
 						if (project.getPDCurrentEmail()!=null){
 							if (emailList.indexOf(project.getPDCurrentEmail()) == -1) {
 								emailList = emailList + ", " + project.getPDCurrentEmail() + "";
 							}
 						}
 					}
 					if (project.getAPDWsnApplicationID() == null || project.getAPDWsnApplicationID().equals("")) {
 						temp.put("APD","");
 					} else {
 						temp.put("APD",helper.value(project.getAPDFirstName())+" "+helper.value(project.getAPDLastName()));
 						temp.put("APDid",project.getAPDWsnApplicationID());
 						//add email address to hash checking to see if it is already in there
 						if (project.getAPDCurrentEmail()!=null){
 							if (emailList.indexOf(project.getAPDCurrentEmail()) == -1) {
 								emailList = emailList + ", " + project.getAPDCurrentEmail() + "";
 							}
 						}
 					}
 					if (project.getCoordWsnApplicationID() == null || project.getCoordWsnApplicationID().equals("")) {
 						temp.put("Coord","");
 					} else {
 						temp.put("Coord",helper.value(project.getCoordFirstName())+" "+helper.value(project.getCoordLastName()));
 						temp.put("Coordid",project.getCoordWsnApplicationID());
 						//add email address to hash checking to see if it is already in there
 						if (project.getCoordCurrentEmail()!=null){
 							if (emailList.indexOf(project.getCoordCurrentEmail()) == -1) {
 								emailList = emailList + ", " + project.getCoordCurrentEmail() + "";
 							}
 						}
 					}
 					
 					h.put(String.valueOf(i),temp);
 					i++;
 				}
 	
 				h.put("region",helper.value((String)person.getRegion()));
 				h.put("emailList", emailList);
 				ctx.setSessionValue("tub", h);
 			}
 
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		} catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
 	}
 
 	public void showProj (ActionContext ctx) {
         Hashtable h = new Hashtable();
 		FormHelper helper = new FormHelper();
 		try {
 			WsnProject project = new WsnProject(ctx.getInputString("id"));
 			h = ObjectHashUtil.obj2hash(project);
 			String getIsPDId = project.getIsPDId();
 			if (getIsPDId ==null || getIsPDId.equals("") || getIsPDId.equals("0")){
 				h.put("PD","-");
 			}
 			else {
 				WsnApplication pd = project.getIsPD();
 				h.put("PD",helper.value((String)pd.getLegalFirstName())+" "+helper.value((String)pd.getLegalLastName()));
 				h.put("PDid",helper.value((String)pd.getWsnApplicationID()));
 				h.put("PDEmail",helper.value((String)pd.getCurrentEmail()));
 			}
 			String getIsAPDId = project.getIsAPDId();
 			if (getIsAPDId ==null || getIsAPDId.equals("") || getIsAPDId.equals("0")){
 				h.put("APD","-");
 			}
 			else {
 				WsnApplication apd = project.getIsAPD();
 				h.put("APD",helper.value((String)apd.getLegalFirstName())+" "+helper.value((String)apd.getLegalLastName()));
 				h.put("APDid",helper.value((String)apd.getWsnApplicationID()));
 				h.put("APDEmail",helper.value((String)apd.getCurrentEmail()));
 			}
 			String getIsCoordId = project.getIsCoordId();
 			if (getIsCoordId ==null || getIsCoordId.equals("") || getIsCoordId.equals("0")){
 				h.put("Coord","-");
 			}
 			else {
 				WsnApplication coord = project.getIsCoord();
 				h.put("Coord",helper.value((String)coord.getLegalFirstName())+" "+helper.value((String)coord.getLegalLastName()));
 				h.put("Coordid",helper.value((String)coord.getWsnApplicationID()));
 				h.put("CoordEmail",helper.value((String)coord.getCurrentEmail()));
 			}
 			ctx.setSessionValue("tub", h);
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
 	}
 
 	public void showSOS (ActionContext ctx) {
 		Hashtable h = new Hashtable();
 		FormHelper helper = new FormHelper();
 		try {
 			Iterator projects = (new WsnProject()).selectList("wsnYear='"+currentWsnYear+"' order by departDateFromGateCity").iterator();
 
 			for (int i = 0; projects.hasNext(); i++) {
 				WsnProject project = (WsnProject) projects.next();
 				Hashtable temp = new Hashtable();
 				temp.put("WsnProjectID", helper.value(project.getWsnProjectID()));
 				temp.put("Name", helper.value(project.getName()));
 				temp.put("PartnershipRegion", helper.value(project.getPartnershipRegion()));
 				temp.put("DepartureDate", helper.value(project.getDepartDateFromGateCity()));
 				temp.put("ReturnDate", helper.value(project.getArrivalDateAtGatewayCity()));  //needed for world travel
 				h.put(String.valueOf(i),temp);
 			}
 
 			ctx.setSessionValue("tub", h);
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		} catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 	public void showTeam (ActionContext ctx) {
         Hashtable h = new Hashtable();
 		double constant95 = 0.95;
 		Locale currentLocale = new Locale("en", "US");
 		String currencyFullBal;
 		String currencyUsableBal;
 		FormHelper helper = new FormHelper();
 		try {
 			WsnProject project = new WsnProject(ctx.getInputString("id"));
 			h.put("ProjectName",helper.value(project.getName()));
 			h.put("id",helper.value(project.getWsnProjectID()));
 			h.put("PartnershipRegion",helper.value(project.getPartnershipRegion()));
 
 			// 2-17-03 kl: new sql added to populate hashtable for counting Applicants and Participants
 			String mAppsCount = "0";
 			String fAppsCount = "0";
 			String mPartCount = "0";
 			String fPartCount = "0";
 			String mStaffCount = "0";
 			String fStaffCount = "0";
 			String tAppsCount = "0";
 			String tPartCount = "0";
 			String tStaffCount = "0";
 			String sql = "";
 			Collection c;
 			Iterator ic;
 			int mAppsNum1;
 			int fAppsNum2;
 			int mPartNum1;
 			int fPartNum2;
 			int mStaffNum;
 			int fStaffNum;
 			int TotalApplicants;
 			int TotalParticipants;
 			int TotalStaff;
 
 			// get # of men that have applied to the project:
 			sql = "SELECT COUNT(fk_isMember) "+
 				" FROM wsn_sp_viewApplication "+
 				" WHERE (gender = '1') AND (fk_isMember = '" + project.getWsnProjectID() + "') AND (status='Accepted' OR status='Pending') AND (isStaff = 0) AND (isApplyingForStaffInternship = 0)";
 			mAppsNum1 = ObjectHashUtil.countIt((new WsnApplication()), sql);
 
 			// get # of females that have applied to the project:
 			sql = "SELECT COUNT(fk_isMember) "+
 				" FROM wsn_sp_viewApplication "+
 				" WHERE (gender = '0') AND (fk_isMember = '" + project.getWsnProjectID() + "') AND (status='Accepted' OR status='Pending') AND (isStaff = 0) AND (isApplyingForStaffInternship = 0)";
 			fAppsNum2 = ObjectHashUtil.countIt((new WsnApplication()), sql);
 
 			// get # of males that are participants of the project:
 			sql = "SELECT COUNT(fk_isMember) "+
 				" FROM wsn_sp_viewApplication "+
 				" WHERE (gender = '1') AND (fk_isMember = '" + project.getWsnProjectID() + "') AND (role = '1') AND (status = 'Accepted') AND (isStaff = 0) AND (isApplyingForStaffInternship = 0)";
 			mPartNum1 = ObjectHashUtil.countIt((new WsnApplication()), sql);
 
 			// get # of females that are participants of the project:
 			sql = "SELECT COUNT(fk_isMember) "+
 				" FROM wsn_sp_viewApplication "+
 				" WHERE (gender = '0') AND (fk_isMember = '" + project.getWsnProjectID() + "') AND (role = '1') AND (status = 'Accepted') AND (isStaff = 0) AND (isApplyingForStaffInternship = 0)";
 			fPartNum2 = ObjectHashUtil.countIt((new WsnApplication()), sql);
 
 			// get # of males that are staff/interns of the project:
 			sql = "SELECT COUNT(fk_isMember) "+
 				" FROM wsn_sp_viewApplication "+
 				" WHERE (gender = '1') AND (fk_isMember = '" + project.getWsnProjectID() + "') AND (isStaff = 1 OR isApplyingForStaffInternship = 1) AND (fk_childOf = 0)";
 			mStaffNum = ObjectHashUtil.countIt((new WsnApplication()), sql);
 
 			// get # of females that are staff/interns of the project:
 			sql = "SELECT COUNT(fk_isMember) "+
 				" FROM wsn_sp_viewApplication "+
 				" WHERE (gender = '0') AND (fk_isMember = '" + project.getWsnProjectID() + "') AND (isStaff = 1 OR isApplyingForStaffInternship = 1) AND (fk_childOf = 0)";
 			fStaffNum = ObjectHashUtil.countIt((new WsnApplication()), sql);
 
 			// add the numbers
 			TotalApplicants = mAppsNum1 + fAppsNum2;
 			TotalParticipants = mPartNum1 + fPartNum2;
 			TotalStaff = mStaffNum + fStaffNum;
 			// 2-17-03 kl: convert Integer back to String for JSP
 			mAppsCount = Integer.toString(mAppsNum1);
 			mPartCount = Integer.toString(mPartNum1);
 			mStaffCount = Integer.toString(mStaffNum);
 			fAppsCount = Integer.toString(fAppsNum2);
 			fPartCount = Integer.toString(fPartNum2);
 			fStaffCount = Integer.toString(fStaffNum);
 			tAppsCount = Integer.toString(TotalApplicants);
 			tPartCount = Integer.toString(TotalParticipants);
 			tStaffCount = Integer.toString(TotalStaff);
 
 			// 2-13-03 kl: populate hashtable for counting Applicants and Participants
 			h.put("MaleApplicants",mAppsCount);
 			h.put("MaleParticipants",mPartCount);
 			h.put("MaleStaff",mStaffCount);
 			h.put("FemaleApplicants",fAppsCount);
 			h.put("FemaleParticipants",fPartCount);
 			h.put("FemaleStaff",fStaffCount);
 			h.put("TotalApplicants",tAppsCount);
 			h.put("TotalParticipants",tPartCount);
 			h.put("TotalStaff",tStaffCount);
 
             String emailList = "";
             for(int i=0; i<3; i++) {
                 WsnApplication person=null;
                 String personCode="";
                 switch (i) {
                     case 0: personCode="PD"; person = project.getIsPD(); break;
                     case 1: personCode="APD"; person = project.getIsAPD(); break;
                     case 2: personCode="Coord"; person = project.getIsCoord(); break;
                 }
                 if (person !=null){
                     Hashtable temp = new Hashtable();
                     temp.put("Name",person.getLegalFirstName() + "&nbsp;" + person.getLegalLastName());
                     temp.put("WsnApplicationID",(person.getWsnApplicationID()!=null ? person.getWsnApplicationID() : ""));
                     temp.put("CurrentEmail",(person.getCurrentEmail()!=null ? person.getCurrentEmail() : ""));
                     h.put(personCode,temp);
                     if (person.getCurrentEmail()!=null) {
                     	// Already added down below...
 						//emailList += person.getCurrentEmail() + ", ";
                     }
                 }
             }
 
 			String whereClause = "(status='Accepted' OR status='Pending') AND fk_isMember='"+project.getWsnProjectID()+"' ORDER BY status, legalLastName, legalFirstName";
 			Iterator persons = (new WsnApplication()).selectList(whereClause).iterator();
 
             Collection ready = new Vector(), notReady = new Vector();
 			while (persons.hasNext()) {
 				WsnApplication person = (WsnApplication) persons.next();
 				if (person.getStatus()!=null){
 					if ((person.getStatus().equalsIgnoreCase("Pending"))||(person.getStatus().equalsIgnoreCase("Accepted")))	{
 						//only display people who are accepted or pending
 						Hashtable temp = new Hashtable();
 						temp.put("Name",(person.getLegalFirstName()!=null?person.getLegalFirstName() : "") + "&nbsp;" + (person.getLegalLastName()!=null ? person.getLegalLastName() : ""));
 						temp.put("WsnApplicationID",     (person.getWsnApplicationID()!=null ?        person.getWsnApplicationID() :""));
 						temp.put("CurrentEmail",    (person.getCurrentEmail()!=null ?       person.getCurrentEmail():""));
 						temp.put("Status",          (person.getStatus()!=null ?             person.getStatus()      :""));
 						temp.put("AccountNo",       (person.getAccountNo()!=null ?          person.getAccountNo()   :""));
 						temp.put("SummerIntern",	(person.getIsApplyingForStaffInternship() ?	"Yes" : ""));
 						temp.put("ApplAccountNo",       (person.getApplAccountNo()!=null ?          person.getApplAccountNo()   :""));
 						temp.put("UniversityFullName",(person.getUniversityFullName()!=null?person.getUniversityFullName() :""));
 						temp.put("PassportNo",      (person.getPassportNo()!=null ?         person.getPassportNo()  :""));
 						temp.put("VisaNo",          (person.getVisaNo()!=null ?             person.getVisaNo()      :""));
 
 						if (!person.getIsStaff()) {
 							System.out.print("Person = " + person.getLegalFirstName() + " " + person.getLegalLastName());
 							String tmpValue = helper.value(String.valueOf(person.getSupportBalance()));
 							System.out.print(" tmpValue = " + tmpValue);
 							Double db1 = Double.valueOf(tmpValue);
 							System.out.println(" db1 = " + db1);
 							double usableAcctBal = db1.doubleValue();
 							double fullAcctBal = usableAcctBal / constant95;
 							NumberFormat currencyFormatter;
 							currencyFormatter = NumberFormat.getCurrencyInstance(currentLocale);
 							currencyUsableBal = currencyFormatter.format(db1);
 							Double db2 = new Double(fullAcctBal);
 							currencyFullBal = currencyFormatter.format(db2);
 
 							temp.put("WaiverReceived",new Boolean(person.getWaiverReceived()));
 							temp.put("InsuranceReceived",new Boolean(person.getInsuranceReceived()));
 							temp.put("UsableSupportBalance",helper.value(String.valueOf(currencyUsableBal)));
 							temp.put("FullSupportBalance",helper.value(String.valueOf(currencyFullBal)));
 						} else {
 							temp.put("WaiverReceived","(-)");
 							temp.put("InsuranceReceived","(-)");
                             temp.put("UsableSupportBalance","(staff)");
                             temp.put("FullSupportBalance","N/A");
 						}
 						//only accepted people get team emails
                         if (person.getStatus().equalsIgnoreCase("Accepted") && person.getCurrentEmail()!=null)
                             emailList += person.getCurrentEmail() + ", ";
 
 						// determine which list to display this person in.
 						if (person.isReadyToEvaluate())
                             ready.add(temp);
                         else
                             notReady.add(temp);
 
 					}
 				} // done with accepted/pending person, ignore withdrawn/rejected
 			}
             h.put("Ready",ready);
             h.put("NotReady",notReady);
             if("".equals(emailList))    h.put("emailList","");
             else                        h.put("emailList",emailList.substring(0,emailList.length()-2));
 
 			ctx.setSessionValue("tub", h);
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		} catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 	public void showTeamTravel (ActionContext ctx) {
         Hashtable h = new Hashtable();
 		FormHelper helper = new FormHelper();
 		try {
 			WsnProject project = new WsnProject(ctx.getInputString("id"));
 			h.put("ProjectName",helper.value((String)project.getName()));
 			h.put("id",helper.value((String)project.getWsnProjectID()));
 			h.put("PartnershipRegion",helper.value((String)project.getPartnershipRegion()));
 			String emailList = "test";
 
 			h.put("DestinationGatewayCity",helper.value((String)project.getDestinationGatewayCity()));
 			h.put("DepartDateFromGateCity",helper.value((String)project.getDepartDateFromGateCity()));
 			h.put("ArrivalDateAtLocation",helper.value((String)project.getArrivalDateAtLocation()));
 			h.put("LocationGatewayCity",helper.value((String)project.getLocationGatewayCity()));
 			h.put("DepartureDateFromLocation",helper.value((String)project.getDepartureDateFromLocation()));
 			h.put("ArrivalDateAtGatewayCity",helper.value((String)project.getArrivalDateAtGatewayCity()));
 			h.put("FlightBudget",helper.value((String)project.getFlightBudget()));
 			h.put("GatewayCitytoLocationFlightNo",helper.value((String)project.getGatewayCitytoLocationFlightNo()));
 			h.put("LocationToGatewayCityFlightNo",helper.value((String)project.getLocationToGatewayCityFlightNo()));
 
 			String getIsPDId = project.getIsPDId();
 			if (getIsPDId !=null || getIsPDId.equals("") || getIsPDId.equals("0")){
 				WsnApplication pd = project.getIsPD();
 				Hashtable temp = new Hashtable();
 				temp.put("Name",pd.getLegalFirstName() + "&nbsp;" + pd.getLegalLastName());
 				temp.put("WsnApplicationID",pd.getWsnApplicationID());
 				temp.put("CurrentEmail",helper.value((String)pd.getCurrentEmail()));
 				h.put("PD",temp);
 				if (emailList.equals("test"))	{
 					emailList = pd.getCurrentEmail();
 				} else {
 					emailList = emailList + ", " + pd.getCurrentEmail();
 				}
 			}
 			String getIsAPDId = project.getIsAPDId();
 			if (getIsAPDId !=null || getIsAPDId.equals("") || getIsAPDId.equals("0")){
 				WsnApplication apd = project.getIsAPD();
 				Hashtable temp = new Hashtable();
 				temp.put("Name",apd.getLegalFirstName() + "&nbsp;" + apd.getLegalLastName());
 				temp.put("WsnApplicationID",apd.getWsnApplicationID());
 				temp.put("CurrentEmail",helper.value((String)apd.getCurrentEmail()));
 				h.put("APD",temp);
 				if (emailList.equals("test"))	{
 					emailList = apd.getCurrentEmail();
 				} else {
 					emailList = emailList + ", " + apd.getCurrentEmail();
 				}
 			}
 			String getIsCoordId = project.getIsCoordId();
 			if (getIsCoordId !=null || getIsCoordId.equals("") || getIsCoordId.equals("0")){
 				WsnApplication coord = project.getIsCoord();
 				Hashtable temp = new Hashtable();
 				temp.put("Name",coord.getLegalFirstName() + "&nbsp;" + coord.getLegalLastName());
 				temp.put("WsnApplicationID",coord.getWsnApplicationID());
 				temp.put("CurrentEmail",helper.value((String)coord.getCurrentEmail()));
 				h.put("Coord",temp);
 				if (emailList.equals("test"))	{
 					emailList = coord.getCurrentEmail();
 				} else {
 					emailList = emailList + ", " + coord.getCurrentEmail();
 				}
 			}
 
 
 			String whereClause="(status='Accepted' OR status='Pending') AND fk_isMember='"+project.getWsnProjectID()+"' ORDER BY status, legalLastName, legalFirstName";
 			Iterator persons = (new WsnApplication()).selectList(whereClause).iterator();
 
 			int i=0;
 			while (persons.hasNext()) {
 				WsnApplication person = (WsnApplication) persons.next();
 				if (person.getStatus()!=null){
 					if ((person.getStatus().equalsIgnoreCase("Pending"))||(person.getStatus().equalsIgnoreCase("Accepted")))	{
 						//only display people who are accepted or pending
 						Hashtable temp = new Hashtable();
 						temp.put("LegalFirstName",helper.value((String)person.getLegalFirstName()));
 						temp.put("LegalLastName",helper.value((String)person.getLegalLastName()));
 						temp.put("WsnApplicationID",person.getWsnApplicationID());
 						temp.put("PassportNo",helper.value((String)person.getPassportNo()));
 						temp.put("Birthdate",helper.value(person.getBirthdate().toString()));
 						temp.put("AccountNo",helper.value((String)person.getAccountNo()));
 						temp.put("ApplAccountNo",helper.value((String)person.getApplAccountNo()));
 
 						if (!person.getIsStaff()) {
 							temp.put("IsStaff","No");
 						}
 						else {
 							temp.put("IsStaff","Yes");
 						}
 
 						if (!person.getApplAccountNo().equals("child")) {
 							temp.put("IsChild","No");
 						}
 						else {
 							temp.put("IsChild","Yes");
 						}
 
 
 						h.put(String.valueOf(i),temp);
 						if (person.getStatus().equalsIgnoreCase("Accepted")) {		//only accepted people get team emails
 							if (person.getCurrentEmail()!=null){
 								if (emailList.equals("test"))	{
 									emailList = person.getCurrentEmail();
 								} else {
 									emailList = emailList + ", " + person.getCurrentEmail();
 								}
 							}
 						}
 						i++;
 					}
 				} // done with accepted/pending person, ignore withdrawn/rejected
 			}
 			h.put("emailList",emailList);
 
 			ctx.setSessionValue("tub", h);
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 	public void showWorldTravel (ActionContext ctx) {
 		Hashtable h = new Hashtable();
 		FormHelper helper = new FormHelper();
 		try {
 			String whereClause = "NOT (name like 'Unassi%') and NOT (name like 'US%') and wsnYear='"+ctx.getSessionValue("wsnYear")+"' order by studentStartDate";
 			Iterator projects = (new WsnProject()).selectList(whereClause).iterator();
 
 			for (int i = 0; projects.hasNext(); i++) {
 				WsnProject project = (WsnProject) projects.next();
 				Hashtable temp = new Hashtable();
 				temp.put("WsnProjectID",(project.getWsnProjectID()!=null ? project.getWsnProjectID() : ""));
 				temp.put("Name",(project.getName()!=null ? project.getName() : ""));
 				temp.put("PartnershipRegion",(project.getPartnershipRegion()!=null ? project.getPartnershipRegion() : ""));
 				temp.put("StartDate",(project.getStudentStartDate()!=null ? (new SimpleDateFormat("MM/dd/yyyy")).format( project.getStudentStartDate()) : ""));
 				temp.put("StopDate",(project.getStudentEndDate()!=null ? (new SimpleDateFormat("MM/dd/yyyy")).format( project.getStudentEndDate()) : ""));
 				temp.put("ReturnDate",(project.getDepartureDateFromLocation()!=null ? project.getDepartureDateFromLocation() : ""));  //needed for world travel
 				h.put(String.valueOf(i),temp);
 			}
 
 			String qry = "select proj from org.alt60m.wsn.sp.model.WsnProject as proj where NOT (proj.name like 'Unassi%') and NOT (proj.name like 'US%') and proj.wsnYear='"+ctx.getSessionValue("wsnYear")+"' order by proj.studentStartDate";
 			h.put("Q",qry);
 
 			ctx.setSessionValue("tub", h);
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		} catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 	public void sosForm (ActionContext ctx) {
         Hashtable h = new Hashtable();
 		FormHelper helper = new FormHelper();
 		ctx.setSessionValue("tub", h);  // blank out the tub's previous info
 		try {
 			WsnProject project = new WsnProject(ctx.getInputString("id"));
 			h = ObjectHashUtil.obj2hash(project); // 4-24-03 kl: added for chartfield combination data for sosform.jsp
 			h.put("ProjectName",helper.value((String)project.getName()));
 			h.put("id",helper.value((String)project.getWsnProjectID()));
 			h.put("PartnershipRegion",helper.value((String)project.getPartnershipRegion()));
 			h.put("DepartureDate",helper.value((String)project.getDepartDateFromGateCity()));
 			h.put("ReturnDate",helper.value((String)project.getArrivalDateAtGatewayCity()));
 			h.put("City",helper.value((String)project.getCity()));
 			h.put("Country",helper.value((String)project.getCountry()));
 
 			//5-7-03 kl: need to check if sosForm Country equals blank to avoid substring calculation before h.put CountryLetter
 			String CountryLetter = "";
 			if (helper.value((String)project.getCountry()) == null || helper.value((String)project.getCountry()).equals(""))
 			{
 			h.put("CountryLetter",CountryLetter);
 			}
 			else
 			{
 			CountryLetter = helper.value((String)project.getCountry()).substring(0,1);
 			h.put("CountryLetter",CountryLetter);
 			}
 			// end of inserted error handling
 			
 			h.put("OperatingAccountNo",helper.value((String)project.getOperatingAccountNo()));
 			
 			String getIsPDId = project.getIsPDId();
 			if (getIsPDId !=null && !getIsPDId.equals("") && !getIsPDId.equals("0")){
 				WsnApplication leader = project.getIsPD();
 				h.put("ProjectPD", leader.getLegalFirstName() + " " + leader.getLegalLastName());
 				h.put("ProjectPDID", leader.getWsnApplicationID());
 			}
 			else{
 				h.put("ProjectPD", "none");
 				h.put("ProjectPDID", "xxx");
 			}
 			
 			String getIsAPDId = project.getIsAPDId();
 			if (getIsAPDId !=null && !getIsAPDId.equals("") && !getIsAPDId.equals("0")){
 				WsnApplication leader = project.getIsAPD();
 				h.put("ProjectAPD", leader.getLegalFirstName() + " " + leader.getLegalLastName());
 				h.put("ProjectAPDID", leader.getWsnApplicationID());
 			}
 			else{
 				h.put("ProjectAPD", "none");
 				h.put("ProjectAPDID", "xxx");
 			}
 
 			String getIsCoordId = project.getIsCoordId();
 			if (getIsCoordId!=null && !getIsCoordId.equals("") && !getIsCoordId.equals("0")){
 				WsnApplication leader = project.getIsCoord();
 				h.put("ProjectCoord", leader.getLegalFirstName() + " " + leader.getLegalLastName());
 				h.put("ProjectCoordID", leader.getWsnApplicationID());
 			}
 			else{
 				h.put("ProjectCoord", "none");
 				h.put("ProjectCoordID", "xxx");
 			}
 
 			java.util.Iterator persons = project.getIsMember().iterator();
 			int i=0;
 			while (persons.hasNext()) {
 				WsnApplication person = (WsnApplication) persons.next();
 				if (person.getStatus()!=null){
 					if (person.getStatus().equalsIgnoreCase("Accepted"))	{
 						//only display people who are accepted
 						Hashtable temp = new Hashtable();
 						temp.put("FirstName",helper.value((String)person.getLegalFirstName()));
 						temp.put("LastName",helper.value((String)person.getLegalLastName()));
 						temp.put("WsnApplicationID",person.getWsnApplicationID());
 						temp.put("AccountNo",helper.value((String)person.getAccountNo()));
 						temp.put("ApplAccountNo",helper.value((String)person.getApplAccountNo()));
 						temp.put("PassportNo",helper.value((String)person.getPassportNo()));
 						temp.put("VisaNo",helper.value((String)person.getVisaNo()));
 						if (person.getIsStaff()){
 							temp.put("OnStaff","Yes");
 						}
 						else {
 							temp.put("OnStaff","No");
 						}
 						h.put(String.valueOf(i),temp);
 						i++;
 					}
 				} // done with accepted person, ignore pending/withdrawn/rejected
 			}
 
 			ctx.setSessionValue("tub", h);
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 
 	public void waiver (ActionContext ctx) {
 		Hashtable h = new Hashtable();
 		try {
 			Hashtable old = (Hashtable) ctx.getSessionValue("tub");
 
 			Integer counter= new Integer((String)ctx.getInputString("counter"));
 			int i = 0;
 			while (i<(counter.intValue())){
 
 				String id = ctx.getInputString(String.valueOf(i));
 				String waiverGood = ctx.getInputString(id);
 
 				if (waiverGood!=null) {
 					try {
 						WsnApplication person = new WsnApplication(id);
 						person.setWaiverReceived(waiverGood.equalsIgnoreCase("true"));
 						person.persist();
 					}
 					catch (Exception e) {
 						System.err.println("WSNSP-WAIVERACTION---------------------:" + e);
 						System.err.println("Attempted to find with key: " + (ctx.getInputString("id")));
 					}
 				}
 				i++;
 			}
 			ctx.setSessionValue("tub", h);
 			String view = ctx.getInputString("view");
 			ctx.goToView(view);
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 			ctx.goToView("error");
 		}
     }
 }
