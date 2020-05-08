 package org.alt60m.wsn.sp.model.dbio;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Vector;
 
 import org.alt60m.hr.ms.servlet.dbio.MSInfo;
 import org.alt60m.ministry.model.dbio.LocalLevel;
 import org.alt60m.util.SendMessage;
 
 import com.kenburcham.framework.dbio.DBIOEntity;
 import com.kenburcham.framework.dbio.DBIOEntityException;
 
 public class WsnApplication extends DBIOEntity{
 	private static final String APPLICATION_TABLE = "wsn_sp_WsnApplication";
 	private static final String VIEWAPP_TABLE = "wsn_sp_viewApplication";
 
 	private WsnPerson person = new WsnPerson();
 	
 	public boolean isPKEmpty() {
 		return WsnApplicationID == 0;
 	}
 	
 	public boolean insert() {
 		person.persist();
 		
 		clearMetadata();
 		applicationinit(APPLICATION_TABLE);
 		setAutodetectProperties(false);
 		
 		setFk_personID(person.getWsnPersonIdInt());
 		setWsnYear(currentWsnYear);	
 		setCreateDate(new Date());
 		setLastChangedDate(new Date());
 		boolean result = super.insert();
 		
 		clearMetadata();
 		try {
 			localinit();
 		} catch (DBIOEntityException e) {
 			e.printStackTrace();
 		}
 		
 		return result;
 	}
 
 	public boolean update() {
 		person.persist();
 		
 		clearMetadata();
 		applicationinit(APPLICATION_TABLE);
 		setAutodetectProperties(false);
 		
 		setFk_personID(person.getWsnPersonIdInt());
 		setLastChangedDate(new Date());
 		boolean result = super.update();
 		
 		clearMetadata();
 		try {
 			localinit();
 		} catch (DBIOEntityException e) {
 			e.printStackTrace();
 		}
 		
 		return result;
 	}
 	
 	public boolean delete() {
 		clearMetadata();
 		applicationinit(APPLICATION_TABLE);
 		setAutodetectProperties(false);
 		
 		boolean result = super.delete();
 		
 		clearMetadata();
 		try {
 			localinit();
 		} catch (DBIOEntityException e) {
 			e.printStackTrace();
 		}
 		
 		return result;
 	}
 	
 	public boolean select() {
 		boolean result = super.select();
 		return result;
 	}
 	
 	public void localinit() throws DBIOEntityException {
 		String table = VIEWAPP_TABLE;
 		
 		applicationinit(table);
 		personinit(table);
 		currentAddressinit(table);
 		emergAddressinit(table);
 		projectinit(table);
 		
 		setAutodetectProperties(false);
 	}
 
 	public void personinit(String table) {
 		setMetadata("WsnPersonID", "personID", "IDENTITY");
 		
 		setMetadata("SurferID", "fk_StaffSiteProfileID", table);
 		setMetadata("Region", "region", table);
 		setMetadata("LegalLastName", "legalLastName", table);
 		setMetadata("LegalFirstName", "legalFirstName", table);
 		setMetadata("Birthdate", "birthdate", table);
 		setMetadata("DateBecameChristian", "dateBecameChristian", table);
 		setMetadata("MaritalStatus", "maritalStatus", table);
 		setMetadata("UniversityFullName", "universityFullName", table);
 		setMetadata("Major", "major", table);
 		setMetadata("YearInSchool", "yearInSchool", table);
 		setMetadata("GraduationDate", "graduationDate", table);
 		setMetadata("UsCitizen", "usCitizen", table);
 		setMetadata("Citizenship", "citizenship", table);
 		setMetadata("AccountNo", "accountNo", table);
 		setMetadata("Gender", "gender", table);
 		setMetadata("IsStaff", "isStaff", table);
 		setMetadata("Child", "child", table);
 		setMetadata("WsnSpouseId", "fk_wsnSpouse", table);
 		setMetadata("ChildOfId", "fk_childOf", table);
 		setMetadata("SsmUserID", "fk_ssmUserID", table);
 		setMetadata("LegalMiddleName", "legalMiddleName", table);
 		setMetadata("Title", "title", table);
 		setMetadata("UniversityState", "universityState", table);
 		setMetadata("CreatedBy", "createdBy", table);
 		setMetadata("DateCreated", "dateCreated", table);
 		setMetadata("DateChanged", "dateChanged", table);
 	}
 	
     public void currentAddressinit(String table) {
     	setMetadata("CurrentAddressID","currentAddressID","IDENTITY");
         setMetadata("CurrentAddress","currentAddress",table);
         setMetadata("CurrentAddress2","currentAddress2",table);
         setMetadata("CurrentCity","currentCity",table);
         setMetadata("CurrentState","currentState",table);
         setMetadata("CurrentZip","currentZip",table);
         setMetadata("CurrentPhone","currentPhone",table);
         setMetadata("CurrentCellPhone","currentCellPhone",table);
         setMetadata("CurrentEmail","currentEmail",table);
         setMetadata("CurrentAddressType","currentAddressType",table);
         setMetadata("CurrentAddressFk_PersonID","currentFk_PersonID",table);
  
 		setAutodetectProperties(false);
 	}
 
     public void emergAddressinit(String table) {
     	setMetadata("EmergAddressID","emergAddressID","IDENTITY");
             
         setMetadata("EmergAddress","emergAddress",table);
         setMetadata("EmergAddress2","emergAddress2",table);
         setMetadata("EmergCity","emergCity",table);
         setMetadata("EmergState","emergState",table);
         setMetadata("EmergZip","emergZip",table);
         setMetadata("EmergPhone","emergPhone",table);
         setMetadata("EmergWorkPhone","emergWorkPhone",table);
         setMetadata("EmergEmail","emergEmail",table);
         setMetadata("EmergName","emergName",table);
         setMetadata("EmergAddressType","emergAddressType",table);
         setMetadata("EmergAddressFk_PersonID","emergFk_PersonID",table);
  
 		setAutodetectProperties(false);
 	}
     public void projectinit(String table) {
 		setMetadata("ProjectName", "projectName", table);
 		setMetadata("ScholarshipBusinessUnit", "scholarshipBusinessUnit", table);
 		setMetadata("ScholarshipOperatingUnit", "scholarshipOperatingUnit", table);
 		setMetadata("ScholarshipDeptID", "scholarshipDeptID", table);
 		setMetadata("ScholarshipProjectID", "scholarshipProjectID", table);
 		setMetadata("ScholarshipDesignation", "scholarshipDesignation", table);	
 		setAutodetectProperties(false);
     }
 	
 	public void applicationinit(String table) {
 		setMetadata("WsnApplicationID", "WsnApplicationID", "IDENTITY");
 		
 		setMetadata("Fk_personID","fk_personID", table);
 		setMetadata("Role", "role", table);
 		setMetadata("EarliestAvailableDate", "earliestAvailableDate", table);
 		setMetadata("DateMustReturn", "dateMustReturn", table);
 		setMetadata("WillingForDifferentProject", "willingForDifferentProject", table);
 		setMetadata("IsApplicationComplete", "isApplicationComplete", table);
 		setMetadata("ProjectPref1Int", "projectPref1", table);
 		setMetadata("ProjectPref2Int", "projectPref2", table);
 		setMetadata("ProjectPref3Int", "projectPref3", table);
 		setMetadata("ProjectPref4Int", "projectPref4", table);
 		setMetadata("ProjectPref5Int", "projectPref5", table);
 		setMetadata("ApplAccountNo", "applAccountNo", table);
 		setMetadata("SupportBalance", "supportBalance", table);
 		setMetadata("InsuranceReceived", "insuranceReceived", table);
 		setMetadata("WaiverReceived", "waiverReceived", table);
 		setMetadata("DidGo", "didGo", table);
 		setMetadata("ParticipantEvaluation", "participantEvaluation", table);
 		setMetadata("ArrivalGatewayCityToLocation", "arrivalGatewayCityToLocation", table);
 		setMetadata("LocationToGatewayCityFlightNo", "locationToGatewayCityFlightNo", table);
 		setMetadata("DepartLocationToGatewayCity", "departLocationToGatewayCity", table);
 		setMetadata("PassportNo", "passportNo", table);
 		setMetadata("PassportCountry", "passportCountry", table);
 		setMetadata("PassportIssueDate", "passportIssueDate", table);
 		setMetadata("PassportExpirationDate", "passportExpirationDate", table);
 		setMetadata("VisaCountry", "visaCountry", table);
 		setMetadata("VisaNo", "visaNo", table);
 		setMetadata("VisaType", "visaType", table);
 		setMetadata("VisaIsMultipleEntry", "visaIsMultipleEntry", table);
 		setMetadata("VisaIssueDate", "visaIssueDate", table);
 		setMetadata("VisaExpirationDate", "visaExpirationDate", table);
 		setMetadata("DateUpdated", "dateUpdated", table);
 		setMetadata("PrevIsp", "prevIsp", table);
 		setMetadata("Status", "status", table);
 		setMetadata("WsnYear", "wsnYear", table);
 		setMetadata("IsMemberIntId", "fk_isMember", table);
 		setMetadata("ParticipateEpic", "participateEpic", table);
 		setMetadata("ParticipateImpact", "participateImpact", table);
 		setMetadata("ParticipateDestino", "participateDestino", table);
 		setMetadata("SpringBreakStart", "springBreakStart", table);
 		setMetadata("SpringBreakEnd", "springBreakEnd", table);
 		setMetadata("IsIntern", "isIntern", table);
 		setMetadata("_1a", "_1a", table);
 		setMetadata("_1b", "_1b", table);
 		setMetadata("_1c", "_1c", table);
 		setMetadata("_1d", "_1d", table);
 		setMetadata("_1e", "_1e", table);
 		setMetadata("_1f", "_1f", table);
 		setMetadata("_2a", "_2a", table);
 		setMetadata("_2b", "_2b", table);
 		setMetadata("_2c", "_2c", table);
 		setMetadata("_3a", "_3a", table);
 		setMetadata("_3b", "_3b", table);
 		setMetadata("_3c", "_3c", table);
 		setMetadata("_3d", "_3d", table);
 		setMetadata("_3e", "_3e", table);
 		setMetadata("_3f", "_3f", table);
 		setMetadata("_3g", "_3g", table);
 		setMetadata("_3h", "_3h", table);
 		setMetadata("_4a", "_4a", table);
 		setMetadata("_4b", "_4b", table);
 		setMetadata("_4c", "_4c", table);
 		setMetadata("_4d", "_4d", table);
 		setMetadata("_4e", "_4e", table);
 		setMetadata("_4f", "_4f", table);
 		setMetadata("_4g", "_4g", table);
 		setMetadata("_4h", "_4h", table);
 		setMetadata("_4i", "_4i", table);
 		setMetadata("_5a", "_5a", table);
 		setMetadata("_5b", "_5b", table);
 		setMetadata("_5c", "_5c", table);
 		setMetadata("_5d", "_5d", table);
 		setMetadata("_5e", "_5e", table);
 		setMetadata("_5f", "_5f", table);
 		setMetadata("_5g", "_5g", table);
 		setMetadata("_5h", "_5h", table);
 		setMetadata("_6", "_6", table);
 		setMetadata("_7", "_7", table);
 		setMetadata("_8a", "_8a", table);
 		setMetadata("_8b", "_8b", table);
 		setMetadata("_9", "_9", table);
 		setMetadata("_10", "_10", table);
 		setMetadata("_11a", "_11a", table);
 		setMetadata("_11b", "_11b", table);
 		setMetadata("_12a", "_12a", table);
 		setMetadata("_12b", "_12b", table);
 		setMetadata("_13a", "_13a", table);
 		setMetadata("_13b", "_13b", table);
 		setMetadata("_13c", "_13c", table);
 		setMetadata("_14", "_14", table);
 		setMetadata("_15", "_15", table);
 		setMetadata("_16", "_16", table);
 		setMetadata("_17", "_17", table);
 		setMetadata("_18", "_18", table);
 		setMetadata("_19a", "_19a", table);
 		setMetadata("_19b", "_19b", table);
 		setMetadata("_19c", "_19c", table);
 		setMetadata("_19d", "_19d", table);
 		setMetadata("_19e", "_19e", table);
 		setMetadata("_19f", "_19f", table);
 		setMetadata("_20a", "_20a", table);
 		setMetadata("_20b", "_20b", table);
 		setMetadata("_20c", "_20c", table);
 		setMetadata("_21a", "_21a", table);
 		setMetadata("_21b", "_21b", table);
 		setMetadata("_21c", "_21c", table);
 		setMetadata("_21d", "_21d", table);
 		setMetadata("_21e", "_21e", table);
 		setMetadata("_21f", "_21f", table);
 		setMetadata("_21g", "_21g", table);
 		setMetadata("_21h", "_21h", table);
 		setMetadata("_21i", "_21i", table);
 		setMetadata("_21j", "_21j", table);
 		setMetadata("_22a", "_22a", table);
 		setMetadata("_22b", "_22b", table);
 		setMetadata("_23a", "_23a", table);
 		setMetadata("_23b", "_23b", table);
 		setMetadata("_24a", "_24a", table);
 		setMetadata("_24b", "_24b", table);
 		setMetadata("_25", "_25", table);
 		setMetadata("_26a", "_26a", table);
 		setMetadata("_26b", "_26b", table);
 		setMetadata("_27a", "_27a", table);
 		setMetadata("_27b", "_27b", table);
 		setMetadata("_28a", "_28a", table);
 		setMetadata("_28b", "_28b", table);
 		setMetadata("_29a", "_29a", table);
 		setMetadata("_29b", "_29b", table);
 		setMetadata("_29c", "_29c", table);
 		setMetadata("_29d", "_29d", table);
 		setMetadata("_29e", "_29e", table);
 		setMetadata("_29f", "_29f", table);
 		setMetadata("_30", "_30", table);
 		setMetadata("_31", "_31", table);
 		setMetadata("_32", "_32", table);
 		setMetadata("_33", "_33", table);
 		setMetadata("_34", "_34", table);
 		setMetadata("_35", "_35", table);
 		setMetadata("IsPaid", "isPaid", table);
 		setMetadata("IsApplyingForStaffInternship", "isApplyingForStaffInternship", table);
 		setMetadata("CreateDate", "createDate", table);
 		setMetadata("LastChangedDate", "lastChangedDate", table);
 		setMetadata("LastChangedByInt", "lastChangedBy", table);
 		setMetadata("IsRecruited", "isRecruited", table);
 		setMetadata("AssignedToProjectInt", "assignedToProject", table);
 		setMetadata("ElectronicSignature", "electronicSignature", table);
 		setMetadata("SubmittedDate", "submittedDate", table);
 		setMetadata("AcceptedDate", "acceptedDate", table);
 		setMetadata("PreferredContactMethod", "preferredContactMethod", table);
 		setMetadata("HowOftenCheckEmail", "howOftenCheckEmail", table);
 		setMetadata("OtherClassDetails", "otherClassDetails", table);
 		setMetadata("ParticipateOtherProjects", "participateOtherProjects", table);
 		setMetadata("CampusHasStaffTeam", "campusHasStaffTeam", table);
 		setMetadata("CampusHasStaffCoach", "campusHasStaffCoach", table);
 		setMetadata("CampusHasMetroTeam", "campusHasMetroTeam", table);
 		setMetadata("CampusHasOther", "campusHasOther", table);
 		setMetadata("CampusHasOtherDetails", "campusHasOtherDetails", table);
 		setMetadata("InSchoolNextFall", "inSchoolNextFall", table);
 		setMetadata("ParticipateCCC", "participateCCC", table);
 		setMetadata("ParticipateNone", "participateNone", table);
 		setMetadata("CiPhoneCallRequested", "ciPhoneCallRequested", table);
 		setMetadata("CiPhoneNumber", "ciPhoneNumber", table);
 		setMetadata("CiBestTimeToCall", "ciBestTimeToCall", table);
 		setMetadata("CiTimeZone", "ciTimeZone", table);
 		setMetadata("_26date", "_26date", table);
 	}
 	private final MSInfo info = new MSInfo();
 
 	private final String currentWsnYear = MSInfo.CURRENT_WSN_YEAR;
 
 	public WsnApplication() {
 	}
 
 	public WsnApplication(String id) {
 		setWsnApplicationIDStr(id);
 		select();
 	}
 	public WsnApplication(int id) {
 		setWsnApplicationIdInt(id);
 		select();
 	}
 	
 	public boolean persist() { return isPKEmpty() ? insert() : update(); }
     private int WsnApplicationID = 0;
 
     private int fk_personID = 0;
 	private String role = "";
 	private String ssn = "";
 	private String earliestAvailableDate = "";
 	private String dateMustReturn = "";
 	private boolean willingForDifferentProject;
 	private boolean isApplicationComplete;
 	private int projectPref1;
 	private int projectPref2;
 	private int projectPref3;
 	private int projectPref4;
 	private int projectPref5;
 	private String accountNo = "";
 	private int supportGoal = 0;
 	private int supportReceived = 0;
 	private double supportBalance = 0;
 	private boolean insuranceReceived;
 	private boolean waiverReceived;
 	private boolean didGo;
 	private boolean participantEvaluation;
 	private String destinationGatewayCity = "";
 	private String gatewayCityToLocationFlightNo = "";
 	private String departGatewayCityToLocation = "";
 	private String arrivalGatewayCityToLocation = "";
 	private String locationToGatewayCityFlightNo = "";
 	private String departLocationToGatewayCity = "";
 	private String arrrivalLocationToGatewayCity = "";
 	private String domesticOrigin = "";
 	private String domesticOriginToGCFlightNo = "";
 	private String departDomesticToGatewayCity = "";
 	private String arrivalDomesticToGatewayCity = "";
 	private String arrivalAtDomesticOrigin = "";
 	private boolean travelPlans;
 	private boolean travelDeviation;
 	private String passportNo = "";
 	private String passportCountry = "";
 	private String passportIssueDate = "";
 	private String passportExpirationDate = "";
 	private String visaCountry = "";
 	private String visaNo = "";
 	private String visaType = "";
 	private boolean visaIsMultipleEntry;
 	private String visaIssueDate = "";
 	private String visaExpirationDate = "";
 	private String dateUpdated = "";
 	private boolean prevIsp;
 	private String status = "";
 	private String appType = "";
 	private String wsnYear = "";
 	private int fk_isMember;
 	private boolean isPaid;
 
 	private int weight, heightFeet, heightInches, ethnicMinistry, _16, _17, _18;
 	private Date springBreakStart, springBreakEnd, createDate, lastChangedDate, datePaymentRecieved;
 	private boolean inSchool, isIntern, _1a, _1b, _1c, _1d, _1e,_2a, _2c, _3a, _3b, _3c, _3d, _3e, _3f, _3g, _4a, _4b, _4c, _4d, _4e,
 		_4f, _4g, _4h,_5a, _5b, _5c, _5d, _5f, _5h, _11a, _12a, _13a, _13b, _13c, _15, _19a, _19b, _19c, _19d, _19e,
 		_21a, _21b, _21c, _21d, _21e, _21f, _21g, _21h, _21j, _22a, _23a, _24a, _26a, _27a, _28a, _29a,  _29c, _29d,
 		participateEpic, participateDestino, participateImpact, isApplyingForStaffInternship, isRecruited;
 	private String _1f, _2b, _3h, _4i, _5e, _5g, _6, _7, _8a, _8b, _9, _10, _11b, _12b, _14, _19f, _20a, _20b, _20c,
 	_21i, _22b, _23b, _24b, _25, _26b, _27b, _28b, _29b, _29e, _29f, _30, _31, _32, _33, _34, _35;
 	private int lastChangedBy;
 	private int	assignedToProject;
 	private String electronicSignature = "";
 	private Date submittedDate;
 	private Date assignedDate;
 	private Date dateReferencesDone;
 	private Date acceptedDate;
 	private Date notAcceptedDate;
 	private Date withdrawnDate;
 	private String finalWsnProjectID;
 	private String preferredContactMethod;
 	private String howOftenCheckEmail;
 	private String otherClassDetails;
 	private boolean participateOtherProjects;
 	private boolean campusHasStaffTeam;
 	private boolean campusHasStaffCoach;
 	private boolean campusHasMetroTeam;
 	private boolean campusHasOther;
     private String campusHasOtherDetails;
 	private boolean inSchoolNextFall;
 	private boolean participateCCC;
 	private boolean participateNone;
 	private boolean ciPhoneCallRequested;
     private String ciPhoneNumber;
     private String ciBestTimeToCall;
     private String ciTimeZone;
     private String _26date;
     private String projectName;
     private String scholarshipBusinessUnit;
     private String scholarshipOperatingUnit;
     private String scholarshipDeptID;
     private String scholarshipProjectID;
     private String scholarshipDesignation;
     
 
 	/* end MS additions */
 
 	public String getWsnApplicationID() {
 		return String.valueOf(WsnApplicationID);
 	}
 	public void setWsnApplicationIDStr(String WsnApplicationID) {
 		if (WsnApplicationID != null && !WsnApplicationID.equals("") && !WsnApplicationID.equals(" ")) {
 			this.WsnApplicationID = Integer.parseInt(WsnApplicationID);
 		} 
 	}
 	public void setWsnApplicationID(int WsnApplicationID) {
 		this.WsnApplicationID = WsnApplicationID;
 	}
 	public int getWsnApplicationIdInt() {
 		return WsnApplicationID;
 	}
 	public void setWsnApplicationIdInt(int WsnApplicationID) {
 		this.WsnApplicationID = WsnApplicationID; 
 	}
 	public String getSurferID() {
 		return String.valueOf(person.getFk_StaffSiteProfileID()); 
 	}
 	public void setSurferID(String surferID) {
 		person.setFk_StaffSiteProfileID(surferID);
 	}
 	public String getRole() {
 		return role; 
 	}
 	public void setRole(String role) {
 		this.role = role; 
 	}
 	public String getRegion() {
 		return person.getRegion(); 
 	}
 	public void setRegion(String region) {
 		person.setRegion(region); 
 	}
 	public String getLegalLastName() {
 		return person.getLastName(); 
 	}
 	public void setLegalLastName(String legalLastName) {
 		person.setLastName(legalLastName); 
 	}
 	public String getLegalFirstName() {
 		return person.getFirstName(); 
 	}
 	public void setLegalFirstName(String legalFirstName) {
 		person.setFirstName(legalFirstName); 
 	}
 	public String getSsn() {
 		return ssn; 
 	}
 	public void setSsn(String ssn) {
 		this.ssn = ssn; 
 	}
 	public int getCurrentAddressID() {
 		return person.getCurrentAddressID();
 	}
 	public void setCurrentAddressID(int ID) {
 		person.setCurrentAddressID(ID);
 	}
 	public String getCurrentAddressType() {
 		return person.getCurrentAddressType();
 	}
 	public void setCurrentAddressType(String type) {
 		person.setCurrentAddressType(type);
 	}
 	public int getCurrentAddressFk_PersonID() {
 		return person.getCurrentAddressFk_PersonID();
 	}
 	public void setCurrentAddressFk_PersonID(int ID) {
 		person.setCurrentAddressFk_PersonID(ID);
 	}
 	public String getCurrentAddress() {
 		return person.getCurrentAddress(); 
 	}
 	public void setCurrentAddress(String currentAddress) {
 		person.setCurrentAddress(currentAddress); 
 	}
 	public String getCurrentCity() {
 		return person.getCurrentCity(); 
 	}
 	public void setCurrentCity(String currentCity) {
 		person.setCurrentCity(currentCity); 
 	}
 	public String getCurrentAddress2() {
 		return person.getCurrentAddress2(); 
 	}
 	public void setCurrentAddress2(String currentAddress2) {
 		person.setCurrentAddress2(currentAddress2); 
 	}
 	public String getCurrentState() {
 		return person.getCurrentState(); 
 	}
 	public void setCurrentState(String currentState) {
 		person.setCurrentState(currentState); 
 	}
 	public String getCurrentZip() {
 		return person.getCurrentZip(); 
 	}
 	public void setCurrentZip(String currentZip) {
 		person.setCurrentZip(currentZip); 
 	}
 	public String getCurrentPhone() {
 		return person.getCurrentPhone(); 
 	}
 	public void setCurrentPhone(String currentPhone) {
 		person.setCurrentPhone(currentPhone); 
 	}
 	public String getCurrentEmail() {
 		return person.getCurrentEmail(); 
 	}
 	public void setCurrentEmail(String currentEmail) {
 		person.setCurrentEmail(currentEmail); 
 	}
 	public String getBirthdate() {
 		return person.getBirthdate(); 
 	}
 	public void setBirthdate(String birthdate) {
 		person.setBirthdate(birthdate); 
 	}
 	public String getDateBecameChristian() {
 		return person.getDateBecameChristian(); 
 	}
 	public void setDateBecameChristian(String dateBecameChristian) {
 		person.setDateBecameChristian(dateBecameChristian); 
 	}
 	public String getMaritalStatus() {
 		return person.getMaritalStatus(); 
 	}
 	public void setMaritalStatus(String maritalStatus) {
 		person.setMaritalStatus(maritalStatus); 
 	}
 	public String getUniversityFullName() {
 		return person.getCampus(); 
 	}
 	public void setUniversityFullName(String universityFullName) {
 		person.setCampus(universityFullName); 
 	}
 	public String getMajor() {
 		return person.getMajor(); 
 	}
 	public void setMajor(String major) {
 		person.setMajor(major); 
 	}
 	public String getYearInSchool() {
 		return person.getYearInSchool(); 
 	}
 	public void setYearInSchool(String yearInSchool) {
 		person.setYearInSchool(yearInSchool); 
 	}
 	public String getGraduationDate() {
 		return person.getGraduationDate(); 
 	}
 	public void setGraduationDate(String graduationDate) {
 		person.setGraduationDate(graduationDate); 
 	}
 	public String getEarliestAvailableDate() {
 		return earliestAvailableDate; 
 	}
 	public void setEarliestAvailableDate(String earliestAvailableDate) {
 		this.earliestAvailableDate = earliestAvailableDate; 
 	}
 	public String getDateMustReturn() {
 		return dateMustReturn; 
 	}
 	public void setDateMustReturn(String dateMustReturn) {
 		this.dateMustReturn = dateMustReturn; 
 	}
 	public boolean getWillingForDifferentProject() {
 		return willingForDifferentProject; 
 	}
 	public void setWillingForDifferentProject(boolean willingForDifferentProject) {
 		this.willingForDifferentProject = willingForDifferentProject; 
 	}
 	public boolean getUsCitizen() {
 		return person.getUsCitizen(); 
 	}
 	public void setUsCitizen(boolean usCitizen) {
 		person.setUsCitizen(usCitizen); 
 	}
 	public String getCitizenship() {
 		return person.getCitizenship(); 
 	}
 	public void setCitizenship(String citizenship) {
 		person.setCitizenship(citizenship); 
 	}
 	public boolean getIsApplicationComplete() {
 		return isApplicationComplete; 
 	}
 	public void setIsApplicationComplete(boolean isApplicationComplete) {
 		this.isApplicationComplete = isApplicationComplete; 
 	}
 	public String getProjectPref(int projectPrefIndex) {
 		switch(projectPrefIndex){
 			case 1:return String.valueOf(projectPref1);
 			case 2:return String.valueOf(projectPref2);
 			case 3:return String.valueOf(projectPref3);
 			case 4:return String.valueOf(projectPref4);
 			case 5:return String.valueOf(projectPref5);
 			default: return "";
 		}
 	}
 	public String getProjectPref1() {
 		return String.valueOf(projectPref1);
 	}
 	public int getProjectPref1Int() {
 		return projectPref1; 
 	}
 	public void setProjectPref1(String projectPref1) {
 		this.projectPref1 = Integer.parseInt(projectPref1);
 	}
 	public void setProjectPref1Int(int projectPref1) {
 		this.projectPref1 = projectPref1; 
 	}
 	public String getProjectPref2() {
 		return String.valueOf(projectPref2);
 	}
 	public int getProjectPref2Int() {
 		return projectPref2; 
 	}
 	public void setProjectPref2(String projectPref2) {
 		this.projectPref2 = Integer.parseInt(projectPref2);
 	}
 	public void setProjectPref2Int(int projectPref2) {
 		this.projectPref2 = projectPref2; 
 	}
 	public String getProjectPref3() {
 		return String.valueOf(projectPref3);
 	}
 	public int getProjectPref3Int() {
 		return projectPref3; 
 	}
 	public void setProjectPref3(String projectPref3) {
 		this.projectPref3 = Integer.parseInt(projectPref3);
 	}
 	public void setProjectPref3Int(int projectPref3) {
 		this.projectPref3 = projectPref3; 
 	}
 	public String getProjectPref4() {
 		return String.valueOf(projectPref4);
 	}
 	public int getProjectPref4Int() {
 		return projectPref4; 
 	}
 	public void setProjectPref4(String projectPref4) {
 		this.projectPref4 = Integer.parseInt(projectPref4);
 	}
 	public void setProjectPref4Int(int projectPref4) {
 		this.projectPref4 = projectPref4; 
 	}
 	public String getProjectPref5() {
 		return String.valueOf(projectPref5);
 	}
 	public int getProjectPref5Int() {
 		return projectPref5; 
 	}
 	public void setProjectPref5(String projectPref5) {
 		this.projectPref5 = Integer.parseInt(projectPref5);
 	}
 	public void setProjectPref5Int(int projectPref5) {
 		this.projectPref5 = projectPref5; 
 	}
 	public String getAccountNo() {
 		return person.getAccountNo(); 
 	}
 	public void setAccountNo(String accountNo) {
 		person.setAccountNo(accountNo); 
 	}
 	public String getApplAccountNo() {
 		return accountNo; 
 	}
 	public void setApplAccountNo(String accountNo) {
 		this.accountNo = accountNo;
 		setAccountNo(accountNo);
 	}
 	public int getSupportGoal() {
 		return supportGoal; 
 	}
 	public void setSupportGoal(int supportGoal) {
 		this.supportGoal = supportGoal; 
 	}
 	public int getSupportReceived() {
 		return supportReceived; 
 	}
 	public void setSupportReceived(int supportReceived) {
 		this.supportReceived = supportReceived; 
 	}
 	public double getSupportBalance() {
 		return supportBalance; 
 	}
 	public void setSupportBalance(double supportBalance) {
 		this.supportBalance = supportBalance; 
 	}
 	public boolean getInsuranceReceived() {
 		return insuranceReceived; 
 	}
 	public void setInsuranceReceived(boolean insuranceReceived) {
 		this.insuranceReceived = insuranceReceived; 
 	}
 	public boolean getWaiverReceived() {
 		return waiverReceived; 
 	}
 	public void setWaiverReceived(boolean waiverReceived) {
 		this.waiverReceived = waiverReceived; 
 	}
 	public boolean getDidGo() {
 		return didGo; 
 	}
 	public void setDidGo(boolean didGo) {
 		this.didGo = didGo; 
 	}
 	public boolean getParticipantEvaluation() {
 		return participantEvaluation; 
 	}
 	public void setParticipantEvaluation(boolean participantEvaluation) {
 		this.participantEvaluation = participantEvaluation; 
 	}
 	public String getDestinationGatewayCity() {
 		return destinationGatewayCity; 
 	}
 	public void setDestinationGatewayCity(String destinationGatewayCity) {
 		this.destinationGatewayCity = destinationGatewayCity; 
 	}
 	public String getGatewayCityToLocationFlightNo() {
 		return gatewayCityToLocationFlightNo; 
 	}
 	public void setGatewayCityToLocationFlightNo(String gatewayCityToLocationFlightNo) {
 		this.gatewayCityToLocationFlightNo = gatewayCityToLocationFlightNo; 
 	}
 	public String getDepartGatewayCityToLocation() {
 		return departGatewayCityToLocation; 
 	}
 	public void setDepartGatewayCityToLocation(String departGatewayCityToLocation) {
 		this.departGatewayCityToLocation = departGatewayCityToLocation; 
 	}
 	public String getArrivalGatewayCityToLocation() {
 		return arrivalGatewayCityToLocation; 
 	}
 	public void setArrivalGatewayCityToLocation(String arrivalGatewayCityToLocation) {
 		this.arrivalGatewayCityToLocation = arrivalGatewayCityToLocation; 
 	}
 	public String getLocationToGatewayCityFlightNo() {
 		return locationToGatewayCityFlightNo; 
 	}
 	public void setLocationToGatewayCityFlightNo(String locationToGatewayCityFlightNo) {
 		this.locationToGatewayCityFlightNo = locationToGatewayCityFlightNo; 
 	}
 	public String getDepartLocationToGatewayCity() {
 		return departLocationToGatewayCity; 
 	}
 	public void setDepartLocationToGatewayCity(String departLocationToGatewayCity) {
 		this.departLocationToGatewayCity = departLocationToGatewayCity; 
 	}
 	public String getArrrivalLocationToGatewayCity() {
 		return arrrivalLocationToGatewayCity; 
 	}
 	public void setArrrivalLocationToGatewayCity(String arrrivalLocationToGatewayCity) {
 		this.arrrivalLocationToGatewayCity = arrrivalLocationToGatewayCity; 
 	}
 	public String getDomesticOrigin() {
 		return domesticOrigin; 
 	}
 	public void setDomesticOrigin(String domesticOrigin) {
 		this.domesticOrigin = domesticOrigin; 
 	}
 	public String getDomesticOriginToGCFlightNo() {
 		return domesticOriginToGCFlightNo; 
 	}
 	public void setDomesticOriginToGCFlightNo(String domesticOriginToGCFlightNo) {
 		this.domesticOriginToGCFlightNo = domesticOriginToGCFlightNo; 
 	}
 	public String getDepartDomesticToGatewayCity() {
 		return departDomesticToGatewayCity; 
 	}
 	public void setDepartDomesticToGatewayCity(String departDomesticToGatewayCity) {
 		this.departDomesticToGatewayCity = departDomesticToGatewayCity; 
 	}
 	public String getArrivalDomesticToGatewayCity() {
 		return arrivalDomesticToGatewayCity; 
 	}
 	public void setArrivalDomesticToGatewayCity(String arrivalDomesticToGatewayCity) {
 		this.arrivalDomesticToGatewayCity = arrivalDomesticToGatewayCity; 
 	}
 	public String getArrivalAtDomesticOrigin() {
 		return arrivalAtDomesticOrigin; 
 	}
 	public void setArrivalAtDomesticOrigin(String arrivalAtDomesticOrigin) {
 		this.arrivalAtDomesticOrigin = arrivalAtDomesticOrigin; 
 	}
 	public boolean getTravelPlans() {
 		return travelPlans; 
 	}
 	public void setTravelPlans(boolean travelPlans) {
 		this.travelPlans = travelPlans; 
 	}
 	public boolean getTravelDeviation() {
 		return travelDeviation; 
 	}
 	public void setTravelDeviation(boolean travelDeviation) {
 		this.travelDeviation = travelDeviation; 
 	}
 	public String getPassportNo() {
 		return passportNo; 
 	}
 	public void setPassportNo(String passportNo) {
 		this.passportNo = passportNo; 
 	}
 	public String getPassportCountry() {
 		return passportCountry; 
 	}
 	public void setPassportCountry(String passportCountry) {
 		this.passportCountry = passportCountry; 
 	}
 	public String getPassportIssueDate() {
 		return passportIssueDate; 
 	}
 	public void setPassportIssueDate(String passportIssueDate) {
 		this.passportIssueDate = passportIssueDate; 
 	}
 	public String getPassportExpirationDate() {
 		return passportExpirationDate; 
 	}
 	public void setPassportExpirationDate(String passportExpirationDate) {
 		this.passportExpirationDate = passportExpirationDate; 
 	}
 	public String getVisaCountry() {
 		return visaCountry; 
 	}
 	public void setVisaCountry(String visaCountry) {
 		this.visaCountry = visaCountry; 
 	}
 	public String getVisaNo() {
 		return visaNo; 
 	}
 	public void setVisaNo(String visaNo) {
 		this.visaNo = visaNo; 
 	}
 	public String getVisaType() {
 		return visaType; 
 	}
 	public void setVisaType(String visaType) {
 		this.visaType = visaType; 
 	}
 	public boolean getVisaIsMultipleEntry() {
 		return visaIsMultipleEntry; 
 	}
 	public void setVisaIsMultipleEntry(boolean visaIsMultipleEntry) {
 		this.visaIsMultipleEntry = visaIsMultipleEntry; 
 	}
 	public String getVisaIssueDate() {
 		return visaIssueDate; 
 	}
 	public void setVisaIssueDate(String visaIssueDate) {
 		this.visaIssueDate = visaIssueDate; 
 	}
 	public String getVisaExpirationDate() {
 		return visaExpirationDate; 
 	}
 	public void setVisaExpirationDate(String visaExpirationDate) {
 		this.visaExpirationDate = visaExpirationDate; 
 	}
 	public int getEmergAddressID() {
 		return person.getEmergAddressID();
 	}
 	public void setEmergAddressID(int ID) {
 		person.setEmergAddressID(ID);
 	}
 	public String getEmergAddressType() {
 		return person.getEmergAddressType();
 	}
 	public void setEmergAddressType(String type) {
 		person.setEmergAddressType(type);
 	}
 	public int getEmergAddressFk_PersonID() {
 		return person.getEmergAddressFk_PersonID();
 	}
 	public void setEmergAddressFk_PersonID(int ID) {
 		person.setEmergAddressFk_PersonID(ID);
 	}
 	public String getEmergName() {
 		return person.getEmergName(); 
 	}
 	public void setEmergName(String emergName) {
 		person.setEmergName(emergName); 
 	}
 	public String getEmergAddress() {
 		return person.getEmergAddress(); 
 	}
 	public void setEmergAddress(String emergAddress) {
 		person.setEmergAddress(emergAddress); 
 	}
 	public String getEmergCity() {
 		return person.getEmergCity(); 
 	}
 	public void setEmergCity(String emergCity) {
 		person.setEmergCity(emergCity); 
 	}
 	public String getEmergState() {
 		return person.getEmergState(); 
 	}
 	public void setEmergState(String emergState) {
 		person.setEmergState(emergState); 
 	}
 	public String getEmergZip() {
 		return person.getEmergZip(); 
 	}
 	public void setEmergZip(String emergZip) {
 		person.setEmergZip(emergZip); 
 	}
 	public String getEmergPhone() {
 		return person.getEmergPhone(); 
 	}
 	public void setEmergPhone(String emergPhone) {
 		person.setEmergPhone(emergPhone); 
 	}
 	public String getEmergWorkPhone() {
 		return person.getEmergWorkPhone(); 
 	}
 	public void setEmergWorkPhone(String emergWorkPhone) {
 		person.setEmergWorkPhone(emergWorkPhone); 
 	}
 	public String getEmergEmail() {
 		return person.getEmergEmail(); 
 	}
 	public void setEmergEmail(String emergEmail) {
 		person.setEmergEmail(emergEmail); 
 	}
 	public String getGender() {
 		return person.getGender(); 
 	}
 	public void setGender(String gender) {
 		person.setGender(gender); 
 	}
 	public String getDateUpdated() {
 		return dateUpdated; 
 	}
 	public void setDateUpdated(String dateUpdated) {
 		this.dateUpdated = dateUpdated; 
 	}
 	public boolean getIsStaff() {
 		return person.getIsStaff(); 
 	}
 	public void setIsStaff(boolean isStaff) {
 		person.setIsStaff(isStaff); 
 	}
 	public boolean getPrevIsp() {
 		return prevIsp; 
 	}
 	public void setPrevIsp(boolean prevIsp) {
 		this.prevIsp = prevIsp; 
 	}
 	public boolean getChild() {
 		return person.getIsChild(); 
 	}
 	public void setChild(boolean child) {
 		person.setIsChild(child); 
 	}
 	public String getAppType() {
 		return appType; 
 	}
 	public void setAppType(String appType) {
 		this.appType = appType; 
 	}
 	public String getStatus() {
 		return status; 
 	}
 	public void setStatus(String status) {
 		this.status = status; 
 	}
 	public String getWsnYear() {
 		return wsnYear; 
 	}
 	public void setWsnYear(String wsnYear) {
 		this.wsnYear = wsnYear; 
 	}
 	public String getIsMemberId() {
		return String.valueOf(this.fk_isMember); 
 	}
 	public int getIsMemberIntId() {
		return this.fk_isMember; 
 	}
 	public void setIsMemberId(String isMember) {
 		this.fk_isMember =Integer.parseInt(isMember); 
 	}
 	public void setIsMemberIntId(int isMember) {
 		this.fk_isMember = isMember; 
 	}
 	public WsnProject getIsMember() {
 		WsnProject result = null;
 		if (fk_isMember != 0) {
 			result = new WsnProject(fk_isMember);
 		}
 		return result;
 	}
 	public void setIsMember(WsnProject isMember) {
 		if (isMember != null) {
 			setIsMemberId(isMember.getWsnProjectID());
 		}
 		else {
 			setIsMemberId("0"); 
 		}
 	}
 	public String getWsnSpouseId() {
 		return String.valueOf(person.getSpouseID()); 
 	}
 	public void setWsnSpouseId(int wsnSpouse) {
 		person.setSpouseID(wsnSpouse); 
 	}
 	public WsnApplication getWsnSpouse() {
 		WsnApplication result = null;
 		if (person.getSpouseID() != 0) {
 			result = new WsnApplication();
 			result.setFk_personID(person.getSpouseID());
 			result.setWsnYear(wsnYear);
 			result.select();
 		}
 		return result; 
 	}
 	public void setWsnSpouse(WsnApplication wsnSpouse) {
 		if (wsnSpouse != null) {
 			person.setSpouseID(String.valueOf(wsnSpouse.getFk_personID()));
 		}
 		else {
 			person.setSpouseID(""); 
 		}
 	}
 	public Vector getIsCoord() {
 		WsnProject project = new WsnProject();
 		project.setIsCoordId(WsnApplicationID);
 		return project.selectList();
 	}
 	public Vector getIsCoord(String orderField, boolean DESC) {
 		WsnProject project = new WsnProject();
 		return project.selectList("fk_IsCoord = '" + this.WsnApplicationID + "' ORDER BY " + orderField + " " + (DESC ? "DESC" : "ASC"));
 	}
 	public void setIsCoord(Collection isCoord) {
 		Iterator iter = isCoord.iterator();
 		while (iter.hasNext()) {
 			WsnProject project = (WsnProject)iter.next();
 			project.setIsCoordId(WsnApplicationID);
 			project.persist();
 		}
 	}
 	public void assocIsCoord(WsnProject project) {
 		project.setIsCoordId(WsnApplicationID);
 		project.persist();
 	}
 	public void dissocIsCoord(WsnProject project) {
 		project.setIsCoordId(0);
 		project.persist();
 	}
 
 	public String getIsAPDId() {
 		WsnProject apd = getIsAPD();
 		String result = null;
 		if (apd != null) {
 			result = apd.getIsAPDId();
 		}
 		return result; 
 	}
 	public void setIsAPDId(String isAPD) {
 		setIsAPD(new WsnProject(isAPD));
 	}
 	public WsnProject getIsAPD() {
 		WsnProject project = null;
 		if (WsnApplicationID != 0) {
 			project = new WsnProject();
 			project.setIsAPDId(WsnApplicationID);
 			project.select();
 			if (project.getWsnProjectIdInt() == 0) {
 				project = null;
 			}
 		}
 		return project; 
 	}
 	public void setIsAPD(WsnProject isAPD) {
 		isAPD.setIsAPDId(WsnApplicationID); 
 	}
 	public String getIsPDId() {
 		WsnProject pd = getIsPD();
 		String result = null;
 		if (pd != null) {
 			result = pd.getIsPDId();
 		}
 		return result; 
 	}
 	public void setIsPDId(String isPD) {
 		setIsPD(new WsnProject(isPD));
 	}
 	public WsnProject getIsPD() {
 		WsnProject project = null;
 		if (WsnApplicationID != 0) {
 			project = new WsnProject();
 			project.setIsPDId(WsnApplicationID);
 			project.select();
 			if (project.getWsnProjectIdInt() == 0) {
 				project = null;
 			}
 		}
 		return project; 
 	}
 	public void setIsPD(WsnProject isPD) {
 		isPD.setIsPDId(WsnApplicationID); 
 	}
 
 	public String getChildOfId() {
 		return String.valueOf(person.getChildOfId()); 
 	}
 	public void setChildOfId(int childOf) {
 		person.setChildOfId(childOf); 
 	}
 	public WsnApplication getChildOf() {
 		WsnApplication result = null;
 		if (person.getChildOfId() != 0) {
 			result = new WsnApplication();
 			result.setFk_personID(person.getChildOfId());
 			result.setWsnYear(this.wsnYear);
 			result.select();
 		}
 		return result; 
 	}
 	public void setChildOf(WsnApplication childOf) {
 		if (childOf!=null){
 			person.setChildOfId(childOf.getFk_personID());
 		} else {
 			person.setChildOfId(null); 
 		}
 	}
 	
 	public Vector getWsnChild() {
 		WsnApplication person = new WsnApplication();
 		person.setChildOfId(this.fk_personID);
 		person.setWsnYear(this.wsnYear);
 		return person.selectList();
 	}
 	public Vector getWsnChild(String orderField, boolean DESC) {
 		WsnApplication person = new WsnApplication();
 		return person.selectList("fk_childOf = '" + this.WsnApplicationID + "' AND wsnYear = '" + this.wsnYear + "' ORDER BY " + orderField + " " + (DESC ? "DESC" : "ASC"));
 	}
 	public void setWsnChild(Collection wsnChild) {
 		Iterator iter = wsnChild.iterator();
 		while (iter.hasNext()) {
 			WsnApplication person = (WsnApplication)iter.next();
 			person.setChildOfId(this.fk_personID);
 			person.persist();
 		}
 	}
 	public void assocWsnChild(WsnApplication person) {
 		person.setChildOfId(this.fk_personID);
 		person.persist();
 	}
 	public void dissocWsnChild(WsnApplication person) {
 		person.setChildOfId(0);
 		person.persist();
 	}
 
 	public int getSsmUserID() { return person.getSsmUserID(); }
 	public void setSsmUserID(int ssmUserID) { person.setSsmUserID(ssmUserID); }
 	/* integers */
 	public int getWeight() { return weight; }
 	public void setWeight(int weight) { this.weight = weight; }
 	public int getHeightFeet() { return heightFeet; }
 	public void setHeightFeet(int heightFeet) { this.heightFeet = heightFeet; }
 	public int getHeightInches() { return heightInches; }
 	public void setHeightInches(int heightInches) { this.heightInches = heightInches; }
 	public int getEthnicMinistry() { return ethnicMinistry; }
 	public void setEthnicMinistry(int ethnicMinistry) { this.ethnicMinistry = ethnicMinistry; }
 	public int get_16() { return _16; }
 	public void set_16(int _16) { this._16 = _16; }
 	public int get_17() { return _17; }
 	public void set_17(int _17) { this._17 = _17; }
 	public int get_18() { return _18; }
 	public void set_18(int _18) { this._18 = _18; }
 	/* Dates */
 	public Date getSpringBreakStart() { return springBreakStart; }
 	public void setSpringBreakStart(Date springBreakStart) { this.springBreakStart = org.alt60m.util.DateUtils.clearTimeFromDate(springBreakStart); }
 	public Date getSpringBreakEnd() { return springBreakEnd; }
 	public void setSpringBreakEnd(Date springBreakEnd) { this.springBreakEnd = org.alt60m.util.DateUtils.clearTimeFromDate(springBreakEnd); }
 	/* booleans */
 	public boolean getIsIntern() { return isIntern; }
 	public void setIsIntern(boolean isIntern) { this.isIntern = isIntern; }
 	public boolean getInSchool() { return inSchool; }
 	public void setInSchool(boolean inSchool) { this.inSchool = inSchool; }
 	public boolean get_1a() { return _1a; }
 	public void set_1a(boolean _1a) { this._1a = _1a; }
 	public boolean get_1b() { return _1b; }
 	public void set_1b(boolean _1b) { this._1b = _1b; }
 	public boolean get_1c() { return _1c; }
 	public void set_1c(boolean _1c) { this._1c = _1c; }
 	public boolean get_1d() { return _1d; }
 	public void set_1d(boolean _1d) { this._1d = _1d; }
 	public boolean get_1e() { return _1e; }
 	public void set_1e(boolean _1e) { this._1e = _1e; }
 	public boolean get_2a() { return _2a; }
 	public void set_2a(boolean _2a) { this._2a = _2a; }
 	public boolean get_2c() { return _2c; }
 	public void set_2c(boolean _2c) { this._2c = _2c; }
 	public boolean get_3a() { return _3a; }
 	public void set_3a(boolean _3a) { this._3a = _3a; }
 	public boolean get_3b() { return _3b; }
 	public void set_3b(boolean _3b) { this._3b = _3b; }
 	public boolean get_3c() { return _3c; }
 	public void set_3c(boolean _3c) { this._3c = _3c; }
 	public boolean get_3d() { return _3d; }
 	public void set_3d(boolean _3d) { this._3d = _3d; }
 	public boolean get_3e() { return _3e; }
 	public void set_3e(boolean _3e) { this._3e = _3e; }
 	public boolean get_3f() { return _3f; }
 	public void set_3f(boolean _3f) { this._3f = _3f; }
 	public boolean get_3g() { return _3g; }
 	public void set_3g(boolean _3g) { this._3g = _3g; }
 	public boolean get_4a() { return _4a; }
 	public void set_4a(boolean _4a) { this._4a = _4a; }
 	public boolean get_4b() { return _4b; }
 	public void set_4b(boolean _4b) { this._4b = _4b; }
 	public boolean get_4c() { return _4c; }
 	public void set_4c(boolean _4c) { this._4c = _4c; }
 	public boolean get_4d() { return _4d; }
 	public void set_4d(boolean _4d) { this._4d = _4d; }
 	public boolean get_4e() { return _4e; }
 	public void set_4e(boolean _4e) { this._4e = _4e; }
 	public boolean get_4f() { return _4f; }
 	public void set_4f(boolean _4f) { this._4f = _4f; }
 	public boolean get_4g() { return _4g; }
 	public void set_4g(boolean _4g) { this._4g = _4g; }
 	public boolean get_4h() { return _4h; }
 	public void set_4h(boolean _4h) { this._4h = _4h; }
 	public boolean get_5a() { return _5a; }
 	public void set_5a(boolean _5a) { this._5a = _5a; }
 	public boolean get_5b() { return _5b; }
 	public void set_5b(boolean _5b) { this._5b = _5b; }
 	public boolean get_5c() { return _5c; }
 	public void set_5c(boolean _5c) { this._5c = _5c; }
 	public boolean get_5d() { return _5d; }
 	public void set_5d(boolean _5d) { this._5d = _5d; }
 	public boolean get_5f() { return _5f; }
 	public void set_5f(boolean _5f) { this._5f = _5f; }
 	public boolean get_5h() { return _5h; }
 	public void set_5h(boolean _5h) { this._5h = _5h; }
 	public boolean get_11a() { return _11a; }
 	public void set_11a(boolean _11a) { this._11a = _11a; }
 	public boolean get_12a() { return _12a; }
 	public void set_12a(boolean _12a) { this._12a = _12a; }
 	public boolean get_13a() { return _13a; }
 	public void set_13a(boolean _13a) { this._13a = _13a; }
 	public boolean get_13b() { return _13b; }
 	public void set_13b(boolean _13b) { this._13b = _13b; }
 	public boolean get_13c() { return _13c; }
 	public void set_13c(boolean _13c) { this._13c = _13c; }
 	public boolean get_15() { return _15; }
 	public void set_15(boolean _15) { this._15 = _15; }
 	public boolean get_19a() { return _19a; }
 	public void set_19a(boolean _19a) { this._19a = _19a; }
 	public boolean get_19b() { return _19b; }
 	public void set_19b(boolean _19b) { this._19b = _19b; }
 	public boolean get_19c() { return _19c; }
 	public void set_19c(boolean _19c) { this._19c = _19c; }
 	public boolean get_19d() { return _19d; }
 	public void set_19d(boolean _19d) { this._19d = _19d; }
 	public boolean get_19e() { return _19e; }
 	public void set_19e(boolean _19e) { this._19e = _19e; }
 	public boolean get_21a() { return _21a; }
 	public void set_21a(boolean _21a) { this._21a = _21a; }
 	public boolean get_21b() { return _21b; }
 	public void set_21b(boolean _21b) { this._21b = _21b; }
 	public boolean get_21c() { return _21c; }
 	public void set_21c(boolean _21c) { this._21c = _21c; }
 	public boolean get_21d() { return _21d; }
 	public void set_21d(boolean _21d) { this._21d = _21d; }
 	public boolean get_21e() { return _21e; }
 	public void set_21e(boolean _21e) { this._21e = _21e; }
 	public boolean get_21f() { return _21f; }
 	public void set_21f(boolean _21f) { this._21f = _21f; }
 	public boolean get_21g() { return _21g; }
 	public void set_21g(boolean _21g) { this._21g = _21g; }
 	public boolean get_21h() { return _21h; }
 	public void set_21h(boolean _21h) { this._21h = _21h; }
 	public boolean get_21j() { return _21j; }
 	public void set_21j(boolean _21j) { this._21j = _21j; }
 	public boolean get_22a() { return _22a; }
 	public void set_22a(boolean _22a) { this._22a = _22a; }
 	public boolean get_23a() { return _23a; }
 	public void set_23a(boolean _23a) { this._23a = _23a; }
 	public boolean get_24a() { return _24a; }
 	public void set_24a(boolean _24a) { this._24a = _24a; }
 	public boolean get_26a() { return _26a; }
 	public void set_26a(boolean _26a) { this._26a = _26a; }
 	public boolean get_27a() { return _27a; }
 	public void set_27a(boolean _27a) { this._27a = _27a; }
 	public boolean get_28a() { return _28a; }
 	public void set_28a(boolean _28a) { this._28a = _28a; }
 	public boolean get_29a() { return _29a; }
 	public void set_29a(boolean _29a) { this._29a = _29a; }
 	public boolean get_29c() { return _29c; }
 	public void set_29c(boolean _29c) { this._29c = _29c; }
 	public boolean get_29d() { return _29d; }
 	public void set_29d(boolean _29d) { this._29d = _29d; }
 	public boolean getParticipateEpic() { return participateEpic; }
 	public void setParticipateEpic(boolean participateEpic) { this.participateEpic = participateEpic; }
 	public boolean getParticipateDestino() { return participateDestino; }
 	public void setParticipateDestino(boolean participateDestino) { this.participateDestino = participateDestino; }
 	public boolean getParticipateImpact() { return participateImpact; }
 	public void setParticipateImpact(boolean participateImpact) { this.participateImpact = participateImpact; }
 
 	public boolean getIsApplyingForStaffInternship() { return isApplyingForStaffInternship; }
 	public void setIsApplyingForStaffInternship(boolean isApplyingForStaffInternship) { this.isApplyingForStaffInternship = isApplyingForStaffInternship; }
 	
 	public String getCreatedBy() { return person.getCreatedBy(); }
 	public void setCreatedBy(String createBy) { person.setCreatedBy(createBy); }
 	
 	public Date getCreateDate() { return createDate; }
 	public void setCreateDate(Date createDate) { this.createDate = org.alt60m.util.DateUtils.clearTimeFromDate(createDate); }
 	
 	public Date getLastChangedDate() { return lastChangedDate; }
 	public void setLastChangedDate(Date lastChangedDate) { this.lastChangedDate = org.alt60m.util.DateUtils.clearTimeFromDate(lastChangedDate); }
 	
 	public String getLastChangedBy() { return String.valueOf(lastChangedBy); }
 	public int getLastChangedByInt() { return lastChangedBy; }
 	public void setLastChangedBy(String lastChangedBy) { this.lastChangedBy = Integer.parseInt(lastChangedBy); }
 	public void setLastChangedByInt(int lastChangedBy) { this.lastChangedBy = lastChangedBy; }
 	
 	public String getCurrentCellPhone() { return person.getCurrentCellPhone(); }
 	public void setCurrentCellPhone(String currentCellPhone) { person.setCurrentCellPhone(currentCellPhone); }
 	
 	public String getEmergAddress2() { return person.getEmergAddress2(); }
 	public void setEmergAddress2(String emergAddress2) { person.setEmergAddress2(emergAddress2); }
 	
 	public String getLegalMiddleName() { return person.getMiddleName(); }
 	public void setLegalMiddleName(String legalMiddleName) { person.setMiddleName(legalMiddleName); }
 	
 	public String getTitle() { return person.getTitle(); }
 	public void setTitle(String title) { person.setTitle(title); }
 	
 //	 This method returns the people soft code corresponding to a person's title
 	public String getPeoplesoftTitle() {
 		String title = person.getTitle();
 		if (title == null || title.equals("") ) {
 			// if title wasn't specified, do a gender check
 			String gender = this.getGender(); 
 			if (person.getGender().equals("0")) {
 				return "11";
 			} else {
 				return "01";
 			}
 		} else if(title.equals("Mr.")) {
 			return "01";
 		} else if (title.equals("Ms.")) {
 			return "11";
 		} else if (title.equals("Mrs.")) {
 			return "03";
 		} 
 		// If all else fails, call them Mr
 		return "01";	
 	}
 	
 	public boolean getIsRecruited() { return isRecruited; }
 	public void setIsRecruited(boolean isRecruited) { this.isRecruited = isRecruited; }
 	
 	public String getAssignedToProject() { return String.valueOf(assignedToProject); }
 	public int getAssignedToProjectInt() { return assignedToProject; }
 	public void setAssignedToProject(String assignedToProject) { this.assignedToProject = Integer.parseInt(assignedToProject); }
 	public void setAssignedToProjectInt(int assignedToProject) { this.assignedToProject = assignedToProject; }
 	
 	public Date getDatePaymentRecieved() { return datePaymentRecieved; }
 	public void setDatePaymentRecieved(Date datePaymentRecieved) { this.datePaymentRecieved = org.alt60m.util.DateUtils.clearTimeFromDate(datePaymentRecieved); }
 
 	/* Strings */
 	public String get_1f() { return _1f; }
 	public void set_1f(String _1f) { this._1f = _1f; }
 	public String get_2b() { return _2b; }
 	public void set_2b(String _2b) { this._2b = _2b; }
 	public String get_3h() { return _3h; }
 	public void set_3h(String _3h) { this._3h = _3h; }
 	public String get_4i() { return _4i; }
 	public void set_4i(String _4i) { this._4i = _4i; }
 	public String get_5e() { return _5e; }
 	public void set_5e(String _5e) { this._5e = _5e; }
 	public String get_5g() { return _5g; }
 	public void set_5g(String _5g) { this._5g = _5g; }
 	public String get_6() { return _6; }
 	public void set_6(String _6) { this._6 = _6; }
 	public String get_7() { return _7; }
 	public void set_7(String _7) { this._7 = _7; }
 	public String get_8a() { return _8a; }
 	public void set_8a(String _8a) { this._8a = _8a; }
 	public String get_8b() { return _8b; }
 	public void set_8b(String _8b) { this._8b = _8b; }
 	public String get_9() { return _9; }
 	public void set_9(String _9) { this._9 = _9; }
 	public String get_10() { return _10; }
 	public void set_10(String _10) { this._10 = _10; }
 	public String get_11b() { return _11b; }
 	public void set_11b(String _11b) { this._11b = _11b; }
 	public String get_12b() { return _12b; }
 	public void set_12b(String _12b) { this._12b = _12b; }
 	public String get_14() { return _14; }
 	public void set_14(String _14) { this._14 = _14; }
 	public String get_19f() { return _19f; }
 	public void set_19f(String _19f) { this._19f = _19f; }
 	public String get_20a() { return _20a; }
 	public void set_20a(String _20a) { this._20a = _20a; }
 	public String get_20b() { return _20b; }
 	public void set_20b(String _20b) { this._20b = _20b; }
 	public String get_20c() { return _20c; }
 	public void set_20c(String _20c) { this._20c = _20c; }
 	public String get_21i() { return _21i; }
 	public void set_21i(String _21i) { this._21i = _21i; }
 	public String get_22b() { return _22b; }
 	public void set_22b(String _22b) { this._22b = _22b; }
 	public String get_23b() { return _23b; }
 	public void set_23b(String _23b) { this._23b = _23b; }
 	public String get_24b() { return _24b; }
 	public void set_24b(String _24b) { this._24b = _24b; }
 	public String get_25() { return _25; }
 	public void set_25(String _25) { this._25 = _25; }
 	public String get_26b() { return _26b; }
 	public void set_26b(String _26b) { this._26b = _26b; }
 	public String get_27b() { return _27b; }
 	public void set_27b(String _27b) { this._27b = _27b; }
 	public String get_28b() { return _28b; }
 	public void set_28b(String _28b) { this._28b = _28b; }
 	public String get_29b() { return _29b; }
 	public void set_29b(String _29b) { this._29b = _29b; }
 	public String get_29e() { return _29e; }
 	public void set_29e(String _29e) { this._29e = _29e; }
 	public String get_29f() { return _29f; }
 	public void set_29f(String _29f) { this._29f = _29f; }
 	public String get_30() { return _30; }
 	public void set_30(String _30) { this._30 = _30; }
 	public String get_31() { return _31; }
 	public void set_31(String _31) { this._31 = _31; }
 	public String get_32() { return _32; }
 	public void set_32(String _32) { this._32 = _32; }
 	public String get_33() { return _33; }
 	public void set_33(String _33) { this._33 = _33; }
 	public String get_34() { return _34; }
 	public void set_34(String _34) { this._34 = _34; }
 	public String get_35() { return _35; }
 	public void set_35(String _35) { this._35 = _35; }
 	public boolean getIsPaid() { return isPaid; }
 	public void setIsPaid(boolean value) { isPaid = value; }
 
 	public String getUniversityState() {
 		return person.getUniversityState(); 
 	}
 	public void setUniversityState(String universityState) {
 		person.setUniversityState(universityState);
 	}
 
 	public String getElectronicSignature() {
 		return electronicSignature; 
 	}
 	public void setElectronicSignature(String electronicSignature) {
 		this.electronicSignature = electronicSignature; 
 	}
 
 	public Date getSubmittedDate() { return submittedDate; }
 	public void setSubmittedDate(Date submittedDate) { this.submittedDate = org.alt60m.util.DateUtils.clearTimeFromDate(submittedDate); }
 
 	public Date getAssignedDate() { return assignedDate; }
 	public void setAssignedDate(Date assignedDate) { this.assignedDate = org.alt60m.util.DateUtils.clearTimeFromDate(assignedDate); }
 
 	public Date getDateReferencesDone() { return dateReferencesDone; }
 	public void setDateReferencesDone(Date dateReferencesDone) { this.dateReferencesDone = org.alt60m.util.DateUtils.clearTimeFromDate(dateReferencesDone); }
 
 	public Date getAcceptedDate() { return acceptedDate; }
 	public void setAcceptedDate(Date acceptedDate) { this.acceptedDate = org.alt60m.util.DateUtils.clearTimeFromDate(acceptedDate); }
 
 	public Date getNotAcceptedDate() { return notAcceptedDate; }
 	public void setNotAcceptedDate(Date notAcceptedDate) { this.notAcceptedDate = org.alt60m.util.DateUtils.clearTimeFromDate(notAcceptedDate); }
 
 	public Date getWithdrawnDate() { return withdrawnDate; }
 	public void setWithdrawnDate(Date withdrawnDate) { this.withdrawnDate = org.alt60m.util.DateUtils.clearTimeFromDate(withdrawnDate); }
 
 	public String getFinalWsnProjectID() { return finalWsnProjectID; }
 	public void setFinalWsnProjectID(String finalWsnProjectID) { this.finalWsnProjectID = finalWsnProjectID; }
 
 	public String getPreferredContactMethod() { return preferredContactMethod; }
 	public void setPreferredContactMethod(String preferredContactMethod) { this.preferredContactMethod = preferredContactMethod; }
 
 	public String getHowOftenCheckEmail() { return howOftenCheckEmail; }
 	public void setHowOftenCheckEmail(String howOftenCheckEmail) { this.howOftenCheckEmail = howOftenCheckEmail; }
 
 	public String getOtherClassDetails() { return otherClassDetails; }
 	public void setOtherClassDetails(String otherClassDetails) { this.otherClassDetails = otherClassDetails; }
 
 	public boolean getParticipateOtherProjects() { return participateOtherProjects; }
 	public void setParticipateOtherProjects(boolean participateOtherProjects) { this.participateOtherProjects = participateOtherProjects; }
 
 	public boolean getCampusHasStaffTeam() { return campusHasStaffTeam; }
 	public void setCampusHasStaffTeam(boolean campusHasStaffTeam) { this.campusHasStaffTeam = campusHasStaffTeam; }
 
 	public boolean getCampusHasStaffCoach() { return campusHasStaffCoach; }
 	public void setCampusHasStaffCoach(boolean campusHasStaffCoach) { this.campusHasStaffCoach = campusHasStaffCoach; }
 
 	public boolean getCampusHasMetroTeam() { return campusHasMetroTeam; }
 	public void setCampusHasMetroTeam(boolean campusHasMetroTeam) { this.campusHasMetroTeam = campusHasMetroTeam; }
 
 	public boolean getCampusHasOther() { return campusHasOther; }
 	public void setCampusHasOther(boolean campusHasOther) { this.campusHasOther = campusHasOther; }
 
 	public String getCampusHasOtherDetails() { return campusHasOtherDetails; }
 	public void setCampusHasOtherDetails(String campusHasOtherDetails) { this.campusHasOtherDetails = campusHasOtherDetails; }
 
 	public boolean getInSchoolNextFall() { return inSchoolNextFall; }
 	public void setInSchoolNextFall(boolean inSchoolNextFall) { this.inSchoolNextFall = inSchoolNextFall; }
 
 	public boolean getParticipateCCC() { return participateCCC; }
 	public void setParticipateCCC(boolean participateCCC) { this.participateCCC = participateCCC; }
 
 	public boolean getParticipateNone() { return participateNone; }
 	public void setParticipateNone(boolean participateNone) { this.participateNone = participateNone; }
 
 	public boolean getCiPhoneCallRequested() { return ciPhoneCallRequested; }
 	public void setCiPhoneCallRequested(boolean ciPhoneCallRequested) { this.ciPhoneCallRequested = ciPhoneCallRequested; }
 
 	public String getCiPhoneNumber() { return ciPhoneNumber; }
 	public void setCiPhoneNumber(String ciPhoneNumber) { this.ciPhoneNumber = ciPhoneNumber; }
 
 	public String getCiBestTimeToCall() { return ciBestTimeToCall; }
 	public void setCiBestTimeToCall(String ciBestTimeToCall) { this.ciBestTimeToCall = ciBestTimeToCall; }
 
 	public String getCiTimeZone() { return ciTimeZone; }
 	public void setCiTimeZone(String ciTimeZone) { this.ciTimeZone = ciTimeZone; }
 
 	public String get_26date() { return _26date; }
 	public void set_26date(String _26date) { this._26date = _26date; }
 
 
 	/**
 	 * @return Returns the projectName.
 	 */
 	public String getProjectName() {
 		return projectName;
 	}
 
 	/**
 	 * @param projectName The projectName to set.
 	 */
 	public void setProjectName(String projectName) {
 		this.projectName = projectName;
 	}
 
 	/**
 	 * @return Returns the scholarshipBusinessUnit.
 	 */
 	public String getScholarshipBusinessUnit() {
 		return scholarshipBusinessUnit;
 	}
 
 	/**
 	 * @param scholarshipBusinessUnit The scholarshipBusinessUnit to set.
 	 */
 	public void setScholarshipBusinessUnit(String scholarshipBusinessUnit) {
 		this.scholarshipBusinessUnit = scholarshipBusinessUnit;
 	}
 
 	/**
 	 * @return Returns the scholarshipDeptID.
 	 */
 	public String getScholarshipDeptID() {
 		return scholarshipDeptID;
 	}
 
 	/**
 	 * @param scholarshipDeptID The scholarshipDeptID to set.
 	 */
 	public void setScholarshipDeptID(String scholarshipDeptID) {
 		this.scholarshipDeptID = scholarshipDeptID;
 	}
 
 	/**
 	 * @return Returns the scholarshipDesignation.
 	 */
 	public String getScholarshipDesignation() {
 		return scholarshipDesignation;
 	}
 
 	/**
 	 * @param scholarshipDesignation The scholarshipDesignation to set.
 	 */
 	public void setScholarshipDesignation(String scholarshipDesignation) {
 		this.scholarshipDesignation = scholarshipDesignation;
 	}
 
 	/**
 	 * @return Returns the scholarshipOperatingUnit.
 	 */
 	public String getScholarshipOperatingUnit() {
 		return scholarshipOperatingUnit;
 	}
 
 	/**
 	 * @param scholarshipOperatingUnit The scholarshipOperatingUnit to set.
 	 */
 	public void setScholarshipOperatingUnit(String scholarshipOperatingUnit) {
 		this.scholarshipOperatingUnit = scholarshipOperatingUnit;
 	}
 
 	/**
 	 * @return Returns the scholarshipProjectID.
 	 */
 	public String getScholarshipProjectID() {
 		return scholarshipProjectID;
 	}
 
 	/**
 	 * @param scholarshipProjectID The scholarshipProjectID to set.
 	 */
 	public void setScholarshipProjectID(String scholarshipProjectID) {
 		this.scholarshipProjectID = scholarshipProjectID;
 	}
 
 	public boolean checkRequiredFields() throws Exception{
 		try {
 			// perform checks for required fields.
 			return true;
 		} catch(Exception e) { throw e; }
 	}
 
 	// encodes the object's referenceId and returns it.
 	// used for emailing the referenceId as a hyperlink.
 	public static String encodeWsnApplicationID(String id) {
 	    Long result = new Long(0L);
 	    System.out.println("EncodeWsnApplicationID=" + id);
 	    try
 	    {
 	      long wsnid = Long.valueOf(id).longValue();
 		  long coefficient = 2125551213L;  // largest integer is: 214 748 3647
 		  result = new Long (coefficient * wsnid);
 		}
 		catch(Exception e)
 		{
             System.out.println("Error: Problem encoding WsnApplicationID: " +id + "... returning '0' (probably not what you wanted)"); 		    
 		}
 		System.out.println("EncodeWsnApplicationID returning: " + result.toString());
 		
 		return result.toString();
 	}
 
 	// Given an encoded referenceId, decodes it back to a form that can be used for query on referenceId field
 	public static String decodeWsnApplicationID(String id) {
 	    Long result = new Long(0L);
 	    System.out.println("DecodeWsnApplicationID=" + id);
 	    try
 	    {
 	      long wsnid = Long.valueOf(id).longValue();
 		  long coefficient = 2125551213L;  // largest integer is: 214 748 3647
 		  result = new Long (wsnid / coefficient);
 		}
 		catch(Exception e)
 		{
             System.out.println("Error: Problem decoding WsnApplicationID: " +id + "... returning '0' (probably not what you wanted)"); 		    
 		}
 		System.out.println("DecodeWsnApplicationID returning: " + result.toString());
 		
 		return result.toString();
 	}
 	
 	/* end MS additions */
 
 
 	// added dc 11/07/02
 	// used to submit an application form for processing.
 	// - If can't submit:
 	//		the details of the errors are returned in HTML format.
 	// - If can submit:
 	//		an empty string is returned
 	//		the submission fields are updated
 	//		the caller must Persist the object!
 	public String submitApplication() {
 		String returnVal = "";
 		String mcVal = "";
 		String phoneVal = "";
 		int ec = 0; // error counter
 
 		// ---- CHECK APPLICATION FORM ----
 		// check some specific fields to give specific error messages
 		if (electronicSignature == null  ||  electronicSignature.length() == 0) {
 			ec++;
 			returnVal = "You must enter your electronic signature below<BR>";
 		}
 
 		// rest of fields do not give specific error message, rather just count the number of missing fields and have 1 error message
 		// NOTE! Fields that are required based on answer in another field are not yet included.  Because we don't have a way yet
 		// to show it on the screen.  Can't make them CLASS="REQUIRED" all the time, beause this isn't true.
 		int mc = 0;	// missing counter
 
 		// 11/05/03 kl: cleaner process for checking submission failures when submit application form
 		// add missing counters 
 		int mc1 = 0;
 		int mc2 = 0;
 		int mc4 = 0;
 		int mc5 = 0;
 		int mc7 = 0;
 		int mc9 = 0;
 		int ps = 0; // project specific required questions
 		int rc = 0; //reference counter
 
 		// on page app1:
 		// NOTE: IF YOU CHANGE THIS LIST OF REQUIRED FIELDS ON PAGE app1, be sure to update checkPersonalInfoRequiredFields in MSInfoBean.java
 		if (person.getTitle() == null  ||  person.getTitle().length() == 0) {
 			mc1++;
 			mcVal = "* Title" + "<br>";
 		}
 		// do not check gender so auto-loaded, not on the screen
 		if (person.getFirstName() == null  ||  person.getFirstName().length() == 0) {
 			mc1++;
 			mcVal += "* First Name" + "<br>";
 		}
 		//4-16-03 kl: remove requirement to check MiddleName
 		//if (legalMiddleName == null  ||  legalMiddleName.length() == 0) {
 		//	mc1++;
 		//	mcVal += ", Middle Name" + "<br>";
 		//}
 		if (person.getLastName() == null  ||  person.getLastName().length() == 0) {
 			mc1++;
 			mcVal += "* Last Name" + "<br>";
 		}
 		if (person.getBirthdate() == null  ||  person.getBirthdate().toString().length() == 0) {
 			mc1++;
 			mcVal += "* Birth Date" + "<br>";
 		}
 		if (person.getMaritalStatus() == null  ||  person.getMaritalStatus().length() == 0) {
 			mc1++;
 			mcVal += "* Marital Status" + "<br>";
 		}
 		if (person.getDateBecameChristian() == null  ||  person.getDateBecameChristian().length() == 0) {
 			mc1++;
 			mcVal += "* Date Received Christ" + "<br>";
 		}
 		if (person.getCurrentAddress() == null  ||  person.getCurrentAddress().length() == 0) {
 			mc1++;
 			mcVal += "* Address 1" + "<br>";
 		}
 		// skip currentAddress2; not required!
 		if (person.getCurrentCity() == null  ||  person.getCurrentCity().length() == 0) {
 			mc1++;
 			mcVal += "* City" + "<br>";
 		}
 		if (person.getCurrentState() == null  ||  person.getCurrentState().length() == 0) {
 			mc1++;
 			mcVal += "* State" + "<br>";
 		}
 		if (person.getCurrentZip() == null  ||  person.getCurrentZip().length() == 0) {
 			mc1++;
 			mcVal += "* Zip" + "<br>";
 		}
 		// if (person.getDateAddressGoodUntil() == null  ||  person.getDateAddressGoodUntil().toString().length() == 0) {
 		//	mc1++;
 		//	mcVal += "* 'I will be at address until'" + "<br>";
 		// }
 		phoneVal += person.getCurrentPhone();
 		phoneVal += person.getCurrentCellPhone();
 		if (phoneVal == null  ||  phoneVal.length() == 0) {
 			mc1++;
 			mcVal += "* Telephone" + "<br>";
 		}
 
 		// added mc1 11/05/03 KL
 		if (mc1 > 0) {
 			// missing fields
 			ec++;
 			returnVal += "Personal Information page is not complete" + "<br>";
 			returnVal += mcVal;
 		}
 
 		// on page app2 (School Information):
 		// first do clear mcVal
 		mcVal = "";
 		if (person.getUniversityState() == null  ||  person.getUniversityState().length() == 0) {
 			mc2++;
 			mcVal =  "* University State" + "<br>";
 		}
 		if (person.getCampus() == null  ||  person.getCampus().length() == 0) {
 			mc2++;
 			mcVal =  "* University Full Name" + "<br>";
 		}
 		if (person.getYearInSchool() == null  ||  person.getYearInSchool().length() == 0) {
 			mc2++;
 			mcVal =  "* Class" + "<br>";
 		}
 // TODO: add back in when the fields are working!		if (springBreakStart == null) mc++;
 // TODO: add back in when the fields are working!		if (springBreakEnd == null) mc++;
 		if (earliestAvailableDate == null  ||  earliestAvailableDate.length() == 0) {
 			mc2++;
 			mcVal +=  "* Earliest Date Available" + "<br>";
 		}
 		if (dateMustReturn == null  ||  dateMustReturn.length() == 0) {
 			mc2++;
 			mcVal +=  "* Date must return" + "<br>";
 		}
 		if (!participateImpact && !participateDestino && !participateEpic && !participateCCC && !participateNone) {
 			mc2++;
 			mcVal += "* You must select at least one type of participation" + "<br>";
 		}
 		if (!campusHasStaffTeam && !campusHasStaffCoach && !campusHasMetroTeam && !campusHasOther) {
 			mc2++;
 			mcVal += "* You must select one answer to the Campus Ministry Information question" + "<br>";
 		}
 		// added mc2 11/05/03 KL
 		if (mc2 > 0) {
 			// missing fields
 			ec++;
 			returnVal += "School Information" + "<br>";
 			returnVal += mcVal;
 		}
 		// on page app3:
 		// NONE
 
 
 		// ---- CHECK PROJECTS ----
 		// ensure at least one of their preferred projects is still eligible to receive additional applicants.
 		// Get list of all valid projects that would be displayed on the app3 Project Preferences screen right now.
 		// Do NOT force it to include preferred projects, that's what were are checking!
 		Collection projects = info.getValidProjects(person.getRegion(), true, person.getGender(), "", "", "", "", "", false);
 		String foundProject = "";
 		if (projects != null) {
 			if (checkProject(this.getProjectPref1(), projects))
 				foundProject = this.getProjectPref1();
 			else if (checkProject(this.getProjectPref2(), projects))
 				foundProject = this.getProjectPref2();
 			else if (checkProject(this.getProjectPref3(), projects))
 				foundProject = this.getProjectPref3();
 			else if (checkProject(this.getProjectPref4(), projects))
 				foundProject = this.getProjectPref4();
 			else if (checkProject(this.getProjectPref5(), projects))
 				foundProject = this.getProjectPref5();
 		}
 		if (foundProject.equals("")) {
 			ec++;
 			returnVal += "Project Preferences" + "<br>";
 			returnVal += "* None of the projects you selected are still available.  Please choose different projects." + "<br>";
 
 			//send errpr email here
 			if (!sendMessage(projects))
 			{
 				return "System error; please try again later";
 			}
 			
 		}
 
 
 		// on page app4 (Experience Information page):
 		// first do clear mcVal
 		mcVal = "";
 		if (_6 == null  ||  _6.length() == 0) {
 			mc4++;
 			mcVal =  "* Question #6" + "<br>";
 		}
 		// added mc4 11/05/03 KL
 		if (mc4 > 0) {
 			// missing fields
 			ec++;
 			returnVal += "Experience Information page is not complete" + "<br>";
 			returnVal += mcVal;
 		}
 
 		// on page app5 (Spiritual Information page):
 		// first do clear mcVal
 		mcVal = "";
 		if (_8a == null  ||  _8a.length() == 0) {
 			mc5++;
 			mcVal =  "* Question #8 (Home Church)" + "<br>";
 		}
 		if (_8b == null  ||  _8b.length() == 0) {
 			mc5++;
 			mcVal +=  "* Question #8 (School Church)" + "<br>";
 		}
 		if (_9 == null  ||  _9.length() == 0) {
 			mc5++;
 			mcVal +=  "* Question #9" + "<br>";
 		}
 		if (_10 == null  ||  _10.length() == 0) {
 			mc5++;
 			mcVal +=  "* Question #10" + "<br>";
 		}
 		if (_11b == null  ||  _11b.length() == 0) {
 			mc5++;
 			mcVal +=  "* Question #11 (Why or Why Not)" + "<br>";
 		}
 		if (_14 == null  ||  _14.length() == 0) {
 			mc5++;
 			mcVal +=  "* Question #14" + "<br>";
 		}
 		// added mc5 11/05/03 KL
 		if (mc5 > 0) {
 			// missing fields
 			ec++;
 			returnVal += "Spiritual Information page is not complete" + "<br>";
 			returnVal += mcVal;
 		}
 
 		// on page app6:
 		// NONE
 
 		// on page app7 (Confidential Information page):
 		// first do clear mcVal
 		mcVal = "";
 		if (_25 == null  ||  _25.length() == 0) {
 			mc7++;
 			mcVal = "* Question #25" + "<br>";
 		}
 		// added mc7 11/05/03 KL
 		if (mc7 > 0) {
 			// missing fields
 			ec++;
 			returnVal += "Confidential Information" + "<br>";
 			returnVal += mcVal;
 		}
 
 		// on page app8:
 		// NONE
 
 		// on page app9 (Short Answer Information page):
 		// first do clear mcVal
 		mcVal = "";
 		if (_30 == null  ||  _30.length() == 0) {
 			mc9++;
 			mcVal =  "* Question #30" + "<br>";
 		}
 		if (_31 == null  ||  _31.length() == 0) {
 			mc9++;
 			mcVal +=  "* Question #31" + "<br>";
 		}
 		if (_32 == null  ||  _32.length() == 0) {
 			mc9++;
 			mcVal +=  "* Question #32" + "<br>";
 		}
 		if (_33 == null  ||  _33.length() == 0) {
 			mc9++;
 			mcVal +=  "* Question #33" + "<br>";
 		}
 		if (_34 == null  ||  _34.length() == 0) {
 			mc9++;
 			mcVal +=  "* Question #34" + "<br>";
 		}
 		// added mc9 11/05/03 KL
 		if (mc9 > 0) {
 			// missing fields
 			ec++;
 			returnVal += "Short Answer Information" + "<br>";
 			returnVal += mcVal;
 		}
 		
 //		// ADDED by S Paulis 11/24/2004 with project specific questions module
 		// TODO: check if the required project specific questions are answered
 		//	first clear mcVal
 		mcVal = "";
 		
 		// loop through all project preferences and see if the applicant answered the required questions
 		Collection personAnswers = info.listWsnApplicationAnswers(WsnApplicationID);
 		Vector usedProjects = new Vector();
 		
 		for(int projIndex=0;projIndex<5;projIndex++)
 		{
 			int missing=0;
 			String projectVal="";
 			try{
 				String projectID= getProjectPref(projIndex+1);
 				if(!projectID.equals("")&&!usedProjects.contains(projectID))//(if they haven't already been asked - if they have duplicate project prefs)
 				{
 					usedProjects.add(projectID);	// add this project to the list of projects whose questions have been asked already on this page
 					
 				
 					Collection projectQuestions = info.listProjectQuestions(projectID, "displayOrder", "ASC");
 					Iterator questions = projectQuestions.iterator();
 					Iterator answers = personAnswers.iterator();
 					
 					Hashtable hashAnswers = new Hashtable();
 					while(answers.hasNext()){
 						Answer nextAnswer = (Answer)answers.next();
 						hashAnswers.put(String.valueOf(nextAnswer.getQuestionID()), nextAnswer.getBody());
 					}
 	
 					for(int i = 1; questions.hasNext(); i++)
 					{
 						Question question = (Question)questions.next();
 						if(!question.getAnswerType().equals("divider") && !question.getAnswerType().equals("info") && !question.getAnswerType().equals("hide") && !question.getAnswerType().equals("checkbox")){
 							String answer = hashAnswers.get(String.valueOf(question.getQuestionID())) == null ? "" : (String)hashAnswers.get(String.valueOf(question.getQuestionID()));
 							
 							if(question.getRequired()& answer.equals("")){
 								ps++;
 								missing++;
 								projectVal+= "* ";
 								projectVal+= (question.getAnswerType().equals("customD")?"Select from ":"");
 								projectVal+= question.getQuestionText().getBody() + "<br>";
 							}
 						}
 					}
 					
 					if(missing>0)
 					{
 						String projectName= info.getProject(projectID).get("Name").toString();
 						mcVal+= "for " + projectName + "<br>";	
 						mcVal+= projectVal; 
 					}
 				}
 			}
 			catch (Exception e)
 			{
 				String sErr = "Exception encountered in WsnApplication.submitApplication(): "+e;
 				System.err.println(sErr);
 			}
 		}
 		
 		if (ps > 0) {
 			// missing fields
 			ec++;
 			returnVal += "Project Specific Questions" + "<br>";
 			returnVal += mcVal;
 		}
 		
 
 		// 11/05/03 kl: replaced with above code
 		/**
 		if (mc > 0) {
 			// missing fields
 			ec++;
 			// strip leading "," out
 			mcVal = mcVal.substring(1, mcVal.length());
 			// submission failed, return error message
 			if (mc==1)
 				returnVal += ec + ". Personal Information page is not complete: " + mcVal + "<br>"; // There is 1 field that has not been entered:
 			else
 				returnVal += ec + ". There are " + mc + " fields that have not been entered: " + mcVal + "<br>";
 		}
 		*/
 
 		// ---- CHECK REFERENCES ----
 		WsnReference ref = new WsnReference();
 
 		// first, everyone requires 1 Peer Reference
 		ref = info.getWsnReferenceWithWsnApplication(getWsnApplicationID(), "P");
 		if (ref == null) {
 			ec++;
 			rc++;
 			returnVal += "References" + "<br>";
 			//returnVal += "* You must specify a Peer Reference.<BR>";
 		} else {
 			if (!ref.getIsFormSubmitted()) {
 				// 03-03-03: only do this check if form is not yet submitted.  This was preventing people from submitting applications because they could not edit the reference anymore.
 				// ensure required fields are not empty
 				String temp = ref.checkAppRequiredFields();
 				if (temp != "") {
 					ec++;
 					rc++;
 					if (rc == 1)	{
 						returnVal += "References" + "<br>";
 					}
 					returnVal += "* Peer Reference: " + temp + "<br>";
 				}
 			}
 		}
 
 		// second, everyone requires 1st Spiritual Leader Reference
 		ref = info.getWsnReferenceWithWsnApplication(getWsnApplicationID(), "S1");
 		if (ref == null) {
 			ec++;
 			rc++;
 			if (rc == 1)	{
 				returnVal += "References" + "<br>";
 			}
 			//returnVal += ec + "* You must specify the first Spritiual Leader Reference.<BR>";
 		} else {
 			if (!ref.getIsFormSubmitted()) {
 				// 03-03-03: only do this check if form is not yet submitted.  This was preventing people from submitting applications because they could not edit the reference anymore.
 				// ensure required fields are not empty
 				String temp = ref.checkAppRequiredFields();
 				if (temp != "") {
 					ec++;
 					rc++;
 					if (rc == 1)	{
 						returnVal += "References" + "<br>";
 					}
 					returnVal += "* For the first Spiritual Leader Reference: " + temp + "<br>";
 				}
 			}
 		}
 
 		if (isApplyingForStaffInternship) {
 			// third, these people require a second Spiritual Leader Reference
 			ref = info.getWsnReferenceWithWsnApplication(getWsnApplicationID(), "S2");
 			if (ref == null) {
 				ec++;
 				rc++;
 				if (rc == 1)	{
 					returnVal += "References" + "<br>";
 				}
 				//returnVal += ec + "* Because you are interested in a Summer Project Staff Internship, you must specify a second Spiritual Leader Reference.<BR>";
 			} else {
 				if (!ref.getIsFormSubmitted()) {
 					// 03-03-03: only do this check if form is not yet submitted.  This was preventing people from submitting applications because they could not edit the reference anymore.
 					// ensure required fields are not empty
 					String temp = ref.checkAppRequiredFields();
 					if (temp != "") {
 						ec++;
 						rc++;
 						if (rc == 1)	{
 							returnVal += "References" + "<br>";
 						}
 						returnVal += "* Leader Reference: " + temp + "<br>";
 					}
 				}
 			}
 		}
 
 
 		// ---- CHECK PAYMENT STATUS ----
 		// Must have at least one payment record for this person. (Which would be either a processed payment, or an intent to pay.)
 		Hashtable payments = info.getPayments(getWsnApplicationID());
 		if (payments == null  ||  payments.size() == 0) {
 			// no payment records yet for this person
 			ec++;
 			returnVal += "Payment" + "<br>";
 			returnVal += "* You have not indicated how you will pay the application fee." + "<br>";
 		}
 
 
 		// ANY ERRORS?
 		if (ec > 0) {
 			return returnVal;
 		}
 
 		// SUBMISSION ALLOWED!
 		// I will update the fields, but the caller must actually persist me!
 
 		// 2003-01-13: these fields are required for the WSN tool so that students show up in project lists:
 		this.setStatus("Pending");
 		this.setAssignedToProject(foundProject);
 		this.setIsApplicationComplete(true);
 		this.setRole("1");
 
 		// set fields about submission and app status
 		this.setSubmittedDate(new Date());	// record date of submission
 
 		return returnVal;
 	}
 
 	
 	public boolean sendMessage(Collection projects)
 	{
 		String errText = "";
 		Date errLastTime = new Date();
 		errText += "Foreign key PersonID: " + this.fk_personID + "\n\n";
 		errText += "WsnApplicationID: " + this.WsnApplicationID + "\n\n";
 		errText += "Timestamp: " + errLastTime.toString() + "\n\n";
 		errText += "Project preference 1: " + this.projectPref1 + "\n\n";
 		errText += "Project preference 2: " + this.projectPref2 + "\n\n";
 		errText += "Project preference 3: " + this.projectPref3 + "\n\n";
 		errText += "Project preference 4: " + this.projectPref4 + "\n\n";
 		errText += "Project preference 5: " + this.projectPref5 + "\n\n";
 		errText += "List of Collection projects: " +"\n\n";
 		
 		
 		for(Iterator i = projects.iterator(); i.hasNext();) {
 		errText += i.next() + "\n";
 		}
 		try {
 			SendMessage msg = new SendMessage();
 			msg.setTo("matt.drees@uscm.org");
 			msg.setFrom("SPerrors@uscm.org");
 			msg.setSubject("Error report: Summer Project Application " + this.WsnApplicationID);
 			msg.setBody(errText, "text/plain");
 			msg.send();
 		
 		} catch(Exception e) {
 			System.err.println("Summer Project error error; we're screwed: Exception=" + e);
 			if (!e.getMessage().equals("No recipient addresses")) {
 				e.printStackTrace();
 				return false;
 			}
 		}
 		return true;
 	}
 
 	// added dc 11/15/02
 	// Use this method to see if a particular ProjectID is in the collection projects
 	public boolean checkProject(String pid, Collection projects) {
 		// Go thru all available projects to see if this one project is in the list
 		Iterator itr = projects.iterator();
 		while (itr.hasNext()) {
 			Hashtable project = (Hashtable)itr.next();
 			String projectID = (String)project.get("WsnProjectID");
 			if (!"".equals(projectID) && projectID != null  &&  projectID.equals(pid)) {
 System.out.println("   projectid=" + projectID + " name=" + (String)project.get("Name"));
 System.out.println("   FOUND THIS ONE!");
 				return true;
 			}
 		}
 		return false;
 	}
 
 /* 2003-02-12 DC deprecated
 	// added dc 11/11/02
 	// Use this method to update the ApplicationStatus field.
 	public void updateApplicationStatus() {
 		try	{
 			// 2003-01-15: Simplified.  Need to review with Scott.  Not all of the fields we included will actually be used, nor is the workflow necessarily correct.
 			if (submittedDate != null)
 				applicationStatus = "Submitted for Evaluation";
 			else
 				applicationStatus = "New";
 
 		} catch(Exception e) { 
 			String sErr = "Exception encountered in WsnApplication.updateApplicationStatus(): "+e;
 			System.err.println(sErr);
 		}
 	}
 **/
 
 
 	// added dc 02/13/03
 	// Use this method to see if this Person is ready to evaluate.
 	// TRUE if application is submitted, all references are submitted, and application is paid
 	// FALSE otherwise
 	public boolean isReadyToEvaluate() {
 		try {
 			
 			//modified 2/27/03 - kb
 			//always ready to evaluate if isStaff()=true
 			if(getIsStaff()) return true;
 
 			//4-15-03 kl: always ready to evaluate if getStatus()=Accepted
 			String wsnGetStatus = getStatus();
 			if(wsnGetStatus.equals("Accepted")) return true;
 			
 			WsnReference ref;
 
 			WsnProject PrefProject= new WsnProject(getProjectPref1());
 			String PrefProjectName= PrefProject.getName();
 			
 			// if not paid, then not ready to evaluate (unless it's an Impact project, because they don't have to pay the app fee)
 			if (!PrefProjectName.startsWith("Impact")&&!isPaid)			
 				return false;
 			// if a reference exists, and it is not submitted, then not ready to evaluate
 			ref = (WsnReference) info.getWsnReferenceWithWsnApplication(getWsnApplicationID(), "P");
 			if (ref != null  && ref.getFormSubmittedDate() == null)
 				return false;
 			ref = (WsnReference) info.getWsnReferenceWithWsnApplication(getWsnApplicationID(), "S1");
 			if (ref != null  && ref.getFormSubmittedDate() == null)
 				return false;
 			ref = (WsnReference) info.getWsnReferenceWithWsnApplication(getWsnApplicationID(), "S2");
 			if (ref != null  && ref.getFormSubmittedDate() == null)
 				return false;
 		}
 		catch (Exception e) {
 			System.err.println(e.toString());
 			e.printStackTrace();
 		}
 		return true;
 	}
 
 
 	/**
 	 * @return Returns the fk_personID.
 	 */
 	public int getFk_personID() {
 		return fk_personID;
 	}
 	/**
 	 * @param fk_personID The fk_personID to set.
 	 */
 	public void setFk_personID(int fk_personID) {
 		this.fk_personID = fk_personID;
 		person.setWsnPersonID(fk_personID);
 	}
 	
 	public void setPerson(WsnPerson pers) {
 		person = pers;
 		this.fk_personID = pers.getWsnPersonID();
 	}
 	
 	public int getWsnPersonID() {
 		return person.getWsnPersonID();
 	}
 	
 	public void setWsnPersonID(int personID) {
 		this.fk_personID = personID;
 		person.setWsnPersonID(personID);
 	}
 	public Date getDateCreated() {
 		return person.getDateCreated();
 	}
 	public void setDateCreated(Date create) {
 		person.setDateCreated(create);
 	}
 	public Date getDateChanged() {
 		return person.getDateChanged();
 	}
 	public void setDateChanged(Date change) {
 		person.setDateChanged(change);
 	}
 	
 	
 }
