 package org.alt60m.crs.model;
 
 import com.kenburcham.framework.dbio.DBIOEntity;
 import com.kenburcham.framework.dbio.DBIOEntityException;
 import java.util.*;
 
 public class Registration extends DBIOEntity {
 	//IDENTITY
 	private int registrationID = 0;
 
 	//KEY
 	private int fk_ConferenceID = 0;
 
 	private int fk_RegistrationTypeID = 0;
 
 	private int fk_PersonID = 0;
 	private int newPersonID = 0;
 
 	private Date registrationDate = null;
 
 	private String preRegistered = "";
 
 	private String isOnsite = "";
 
 	private String registrationType = "";	/* deprecated 20041026 greg.wilson */
 
 	private Date arriveDate = null;
 
 	private Date leaveDate = null;
 
 	private int additionalRooms = 0;
 
 	private int spouseComing = 0; // 0=not asked 1=no 2=yes
 	
 	private String registeredFirst= "";
 	
 	private int spouseRegistrationID = 0;
 
 	protected Person person = new Person();
 
 	public static final int SPOUSE_COMING = 2;
 	public static final int SPOUSE_NOT_COMING = 1;
 	public static final int SPOUSE_NO_RESPONSE = 0;
 
 	public Registration() {
 	}
 
 	public Registration(int id) {
 		setRegistrationID(id);
 		select();
 	}
 
 	public void localinit() throws DBIOEntityException {
 		String table = "crs_viewregistration";
 		setMetadata("RegistrationID", "registrationID", "IDENTITY");
 
 		setMetadata("ConferenceID", "fk_conferenceID", "KEY");
 		setMetadata("RegistrationTypeID", "fk_RegistrationTypeID", "KEY");
 		setMetadata("PersonID", "fk_personID", "KEY");
 
 		setMetadata("RegistrationDate", "registrationDate", table);
 		setMetadata("PreRegisteredString", "preRegistered", table);
 //		setMetadata("RegistrationType", "registrationType", table);			/* to be deprecated */
 		setMetadata("ArriveDate", "arriveDate", table);
 		setMetadata("LeaveDate", "leaveDate", table);
 		setMetadata("AdditionalRooms", "additionalRooms", table);
 		setMetadata("SpouseComing", "spouseComing", table);
 		setMetadata("RegisteredFirstString", "registeredFirst", table);
 		setMetadata("SpouseRegistrationID", "spouseRegistrationID", table);
 		setMetadata("IsOnsite", "isOnsite", table);
 		
 
 		//Person loading
 //		setMetadata("Username", "username", table);
 		setMetadata("Email", "email", table);
 		setMetadata("CreatedDate", "createdDate", table);
 		setMetadata("FirstName", "firstName", table);
 		setMetadata("LastName", "lastName", table);
 		setMetadata("MiddleInitial", "middleInitial", table);
 		setMetadata("RawBirthDate", "birth_date", table);
 		setMetadata("Campus", "campus", table);
 		setMetadata("YearInSchool", "yearInSchool", table);
 		setMetadata("RawGraduationDate", "graduation_date", table);
 		setMetadata("GreekAffiliation", "greekAffiliation", table);
 		setMetadata("Gender", "gender", table);
 		setMetadata("Address1", "address1", table);
 		setMetadata("Address2", "address2", table);
 		setMetadata("City", "city", table);
 		setMetadata("State", "state", table);
 		setMetadata("Zip", "zip", table);
 		setMetadata("Country", "country", table);
 		setMetadata("PermanentAddress1", "permanentAddress1", table);
 		setMetadata("PermanentAddress2", "permanentAddress2", table);
 		setMetadata("PermanentCity", "permanentCity", table);
 		setMetadata("PermanentState", "permanentState", table);
 		setMetadata("PermanentZip", "permanentZip", table);
 		setMetadata("PermanentCountry", "permanentCountry", table);
 		setMetadata("PermanentPhone", "permanentPhone", table);
 		setMetadata("HomePhone", "homePhone", table);
 //		setMetadata("Ssn", "ssn", table);
 		setMetadata("MaritalStatus", "maritalStatus", table);
 		setMetadata("NumberOfKids", "numberOfKids", table);
 		setMetadata("AccountNo","accountNo",table);
 		setMetadata("SpouseID", "spouseID", table);
 		setMetadata("NewPersonID", "newPersonID", table);
 		
 		setAutodetectProperties(false);
 	}
 
 	public boolean isPKEmpty() {
 		return getRegistrationID() == 0;
 	}
 
 	public int getRegistrationID() {
 		return registrationID;
 	}
 
 	public void setRegistrationID(int registrationID) {
 		this.registrationID = registrationID;
 	}
 
 	public Date getRegistrationDate() {
 		return registrationDate;
 	}
 
 	public void setRegistrationDate(Date registrationDate) {
 		this.registrationDate = registrationDate;
 	}
 
 	public int getSpouseComing() {
 		return spouseComing;
 	}
 
 	public boolean getPreRegistered() {
 		return isPreRegistered();
 	}
 
 	public boolean isOnsite() {
 		return "T".equals(isOnsite);
 	}
 
 	public String getIsOnsite() {
 		return isOnsite;
 	}
 
 	public void setIsOnsite(boolean _onSite) {
 		this.isOnsite = _onSite ? "T" : "F";
 	}
 
 	public void setIsOnsiteString(String _onSite) {
 		this.isOnsite = _onSite;
 	}
 
 	/**
 	 * @return Returns the registeredFirst.
 	 */
 	public boolean isRegisteredFirst() {
 		return "T".equals(registeredFirst);
 	}
 
 	public String getRegisteredFirstString() {
 		return registeredFirst;
 	}
 
 	/**
 	 * @param registeredFirst
 	 *            The registeredFirst to set.
 	 */
 	public void setRegisteredFirst(boolean _registeredFirst) {
 		this.registeredFirst = _registeredFirst ? "T" : "F";
 	}
 
 	public void setRegisteredFirstString(String _registeredFirst) {
 		this.registeredFirst = _registeredFirst;
 	}
 
 	
 	/**
 	 * @return Returns the spouseRegistrationID.
 	 */
 	public int getSpouseRegistrationID() {
 		return spouseRegistrationID;
 	}
 	/**
 	 * @param spouseRegistrationID The spouseRegistrationID to set.
 	 */
 	public void setSpouseRegistrationID(int spouseRegistrationID) {
 		this.spouseRegistrationID = spouseRegistrationID;
 	}
 	/**
 	 * @param spouseComing The spouseComing to set.
 	 */
 	public void setSpouseComing(int spouseComing) {
 		this.spouseComing = spouseComing;
 	}
 	public void setPreRegistered(boolean preRegistered) {
 		if (!getPreRegistered() && preRegistered) {
 			if (fk_ConferenceID != 0 && person != null) {
 				org.alt60m.crs.util.EmailConfirm email = new org.alt60m.crs.util.EmailConfirm();
 				email.createConfirmation(getConference(),
 						person.getFirstName(), person.getLastName(), person
 								.getEmail());
 				email.send();
 			}
 		}
 		this.preRegistered = preRegistered ? "T" : "F";
 	}
 
 	public String getPreRegisteredString() {
 		return preRegistered;
 	}
 	public boolean isPreRegistered() {
 		return "T".equals(preRegistered);
 	}
 
 	public void setPreRegisteredString(String preRegistered) {
 		this.preRegistered = preRegistered;
 	}
 
 	public String getRegistrationTypeOld() {	/* to be deprecated */
 		return registrationType;
 	}
 
 	public void setRegistrationTypeOld(String registrationType) {	/* to be deprecated */
 		this.registrationType = registrationType;
 	}
 
 	public Date getArriveDate() {
 		return arriveDate;
 	}
 
 	public void setArriveDate(Date arrive) {
 		this.arriveDate = org.alt60m.util.DateUtils.clearTimeFromDate(arrive);
 	}
 
 	public Date getLeaveDate() {
 		return leaveDate;
 	}
 
 	public void setLeaveDate(Date leave) {
 		this.leaveDate = org.alt60m.util.DateUtils.clearTimeFromDate(leave);
 	}
 
 	// Associations
 	public int getConferenceID() {
 		return fk_ConferenceID;
 	}
 
 	public void setConferenceID(int conferenceID) {
 		this.fk_ConferenceID = conferenceID;
 	}
 
 	public Conference getConference() {
 		Conference c = new Conference();
 		c.setConferenceID(fk_ConferenceID);
 		c.select();
 		return c;
 	}
 
 	public void setConference(Conference c) {
 		fk_ConferenceID = c.getConferenceID();
 	}
 
 	//RegistrationType
 	public int getRegistrationTypeID() {
 		return fk_RegistrationTypeID;
 	}
 
 	public void setRegistrationTypeID(int registrationTypeID) {
 		this.fk_RegistrationTypeID = registrationTypeID;
 	}
 
 	public RegistrationType getRegistrationType() {
 		RegistrationType rt = new RegistrationType();
 		rt.setRegistrationTypeID(fk_RegistrationTypeID);
 		rt.select();
 		return rt;
 	}
 
 	public void setRegistrationType(RegistrationType rt) {
 		fk_RegistrationTypeID = rt.getRegistrationTypeID();
 	}
 	
 	public void setRegistrationType(int registrationTypeID) {
 		fk_RegistrationTypeID = registrationTypeID;
 	}
 	
 	//Person
 
 	public int getPersonID() {
 		return person.getPersonID();
 	}
 
 	public void setPersonID(int personID) {
 		person.setPersonID(personID);
 	}
 
 	public int getSpouseID() {
 		return person.getSpouseID();
 	}
 
 	public void setSpouseID(int SpouseID) {
 		person.setSpouseID(SpouseID);
 	}
 
 	
 	public String getAccountNo() {
 		return person.getAccountNo();
 	}
 
 	public void setAccountNo(String accountNo) {
 		person.setAccountNo(accountNo);
 	}
 	
 	
 /*	public String getUsername() {
 		return person.getUsername();
 	}
 
 	public void setUsername(String username) {
 		person.setUsername(username);
 	}
 */
 	public String getEmail() {
 		return person.getEmail();
 	}
 
 	public void setEmail(String email) {
 		person.setEmail(email);
 	}
 
 	public Date getCreatedDate() {
 		return person.getDateCreated();
 	}
 
 	public void setCreatedDate(Date createdDate) {
 		person.setDateCreated(createdDate);
 	}
 
 	public String getFirstName() {
 		return person.getFirstName();
 	}
 
 	public void setFirstName(String firstName) {
 		person.setFirstName(firstName);
 	}
 
 	public String getLastName() {
 		return person.getLastName();
 	}
 
 	public void setLastName(String lastName) {
 		person.setLastName(lastName);
 	}
 
 	public String getMiddleInitial() {
 		return person.getMiddleInitial();
 	}
 
 	public void setMiddleInitial(String middleInitial) {
 		person.setMiddleInitial(middleInitial);
 	}
 
 	public String getBirthDate() {
 		return person.getBirthDate();
 	}
 
 	public void setBirthDate(String birthDate) {
 		person.setBirthDate(birthDate);
 	}
 
 	
 	
 	public Date getRawBirthDate() {
 		return person.getRawBirthDate();
 	}
 
 	public void setRawBirthDate(Date birthDate) {
 		person.setRawBirthDate(birthDate);
 	}
 
 
 	public String getCampus() {
 		return person.getCampus();
 	}
 
 	public void setCampus(String campus) {
 		person.setCampus(campus);
 	}
 
 	public String getYearInSchool() {
 		return person.getYearInSchool();
 	}
 
 	public void setYearInSchool(String yearInSchool) {
 		person.setYearInSchool(yearInSchool);
 	}
 
 	public String getGreekAffiliation() {
 		return person.getGreekAffiliation();
 	}
 
 	public void setGreekAffiliation(String greekAffiliation) {
 		person.setGreekAffiliation(greekAffiliation);
 	}
 
 	public String getGraduationDate() {
 		return person.getGraduationDate();
 	}
 
 	public void setGraduationDate(String graduation_date) {
 		person.setGraduationDate(graduation_date);
 	}
 
 	public Date getRawGraduationDate() {
 		return person.getRawGraduationDate();
 	}
 
 	public void setRawGraduationDate(Date graduation_date) {
 		person.setRawGraduationDate(graduation_date);
 	}
 
 	public String getGender() {
 		return person.getGender();
 	}
 
 	public void setGender(String gender) {
 		person.setGender(gender);
 	}
 
 	public String getAddress1() {
 		return person.getAddress1();
 	}
 
 	public void setAddress1(String address1) {
 		person.setAddress1(address1);
 	}
 
 	public String getAddress2() {
 		return person.getAddress2();
 	}
 
 	public void setAddress2(String address2) {
 		person.setAddress2(address2);
 	}
 
 	public String getCity() {
 		return person.getCity();
 	}
 
 	public void setCity(String city) {
 		person.setCity(city);
 	}
 
 	public String getState() {
 		return person.getState();
 	}
 
 	public void setState(String state) {
 		person.setState(state);
 	}
 
 	public String getZip() {
 		return person.getZip();
 	}
 
 	public void setZip(String zip) {
 		person.setZip(zip);
 	}
 
 	public String getCountry() {
 		return person.getCountry();
 	}
 
 	public void setCountry(String country) {
 		person.setCountry(country);
 	}
 
 	public String getHomePhone() {
 		return person.getHomePhone();
 	}
 
 	public void setHomePhone(String homePhone) {
 		person.setHomePhone(homePhone);
 	}
 
 	public String getPermanentAddress1() {
 		return person.getPermanentAddress1();
 	}
 
 	public void setPermanentAddress1(String permanentAddress1) {
 		person.setPermanentAddress1(permanentAddress1);
 	}
 
 	public String getPermanentAddress2() {
 		return person.getPermanentAddress2();
 	}
 
 	public void setPermanentAddress2(String permanentAddress2) {
 		person.setPermanentAddress2(permanentAddress2);
 	}
 
 	public String getPermanentCity() {
 		return person.getPermanentCity();
 	}
 
 	public void setPermanentCity(String permanentCity) {
 		person.setPermanentCity(permanentCity);
 	}
 
 	public String getPermanentState() {
 		return person.getPermanentState();
 	}
 
 	public void setPermanentState(String permanentState) {
 		person.setPermanentState(permanentState);
 	}
 
 	public String getPermanentZip() {
 		return person.getPermanentZip();
 	}
 
 	public void setPermanentZip(String permanentZip) {
 		person.setPermanentZip(permanentZip);
 	}
 
 	public String getPermanentCountry() {
 		return person.getPermanentCountry();
 	}
 
 	public void setPermanentCountry(String permanentCountry) {
 		person.setPermanentCountry(permanentCountry);
 	}
 
 	public String getPermanentPhone() {
 		return person.getPermanentPhone();
 	}
 
 	public void setPermanentPhone(String permanentPhone) {
 		person.setPermanentPhone(permanentPhone);
 	}
 
 /*	public String getSsn() {
 		return person.getSsn();
 	}
 
 	public void setSsn(String ssn) {
 		person.setSsn(ssn);
 	}
 */
 	public String getMaritalStatus() {
 		return person.getMaritalStatus();
 	}
 
 	public void setMaritalStatus(String maritalStatus) {
 		person.setMaritalStatus(maritalStatus);
 	}
 
 /*	public String getSpouseName() {
 		return person.getSpouseName();
 	}
 
 	public void setSpouseName(String spouseName) {
 		person.setSpouseName(spouseName);
 	}
 */
 	public String getNumberOfKids() {
 		return person.getNumberOfKids();
 	}
 
 	public void setNumberOfKids(String numberOfKids) {
 		person.setNumberOfKids(numberOfKids);
 	}
 
 	public int getNumberOfKidsInt() {
 		return person.getNumberOfKidsInt();
 	}
 
 	public void setNumberOfKidsInt(int numberOfKids) {
 		person.setNumberOfKidsInt(numberOfKids);
 	}
 
 	public int getAdditionalRooms() {
 		return additionalRooms;
 	}
 
 	public void setAdditionalRooms(int rooms) {
 		this.additionalRooms = rooms;
 	}
 
 	public Person getPerson() {
 		return person;
 	}
 
 	public void setPerson(Person p) {
 		person = p;
 	}
 
 	public Vector getAnswers() {
 		Answer a = new Answer();
 		a.setRegistrationID(registrationID);
 		return a.selectList();
 	}
 
 	public Vector getAnswers(String orderField, boolean DESC) {
 		Answer a = new Answer();
 		return a.selectList("fk_RegistrationID = '" + registrationID
 				+ "' ORDER BY " + orderField + " " + (DESC ? "DESC" : "ASC"));
 	}
 
 	public void assocAnswer(Answer a) {
 		a.setRegistrationID(registrationID);
 		a.update();
 	}
 
 	public void dissocAnswer(Answer a) {
 		a.setRegistrationID(0);
 		a.update();
 	}
 
 	public Vector getMerchandise() {
 		Merchandise m = new Merchandise();
 		m.clearMetadata();
 
 		String table = "crs_viewmerchandisechoice";
 		m.setMetadata("MerchandiseID", "merchandiseID", "IDENTITY");
 		m.setMetadata("ConferenceID", "fk_ConferenceID", "KEY");
 		m.setMetadata("Name", "name", table);
 		m.setMetadata("Note", "note", table);
 		m.setMetadata("Amount", "amount", table);
 		m.setMetadata("RequiredString", "required", table);
 //		m.setMetadata("RegistrationType", "registrationType", table);  /* to be deprecated */
 		m.setMetadata("DisplayOrder", "displayOrder", table);
 
 		return m.selectList("registrationID = '" + registrationID + "'");
 	}
 
 	public Vector getMerchandise(String orderField, boolean DESC) {
 		Merchandise m = new Merchandise();
 		m.clearMetadata();
 
 		String table = "crs_viewmerchandisechoice";
 		m.setMetadata("MerchandiseID", "merchandiseID", "IDENTITY");
 		m.setMetadata("ConferenceID", "fk_ConferenceID", "KEY");
 		m.setMetadata("Name", "name", table);
 		m.setMetadata("Note", "note", table);
 		m.setMetadata("Amount", "amount", table);
 		m.setMetadata("RequiredString", "required", table);
 //		m.setMetadata("RegistrationType", "registrationType", table);  /* to be deprecated */
 		m.setMetadata("DisplayOrder", "displayOrder", table);
 
 		return m.selectList("registrationID = '" + registrationID
 				+ "' ORDER BY " + orderField + " " + (DESC ? "DESC" : "ASC"));
 	}
 
 	public void assocMerchandise(Merchandise rel) {
 		MerchandiseChoice mc = new MerchandiseChoice();
 		mc.setMerchandiseID(rel.getMerchandiseID());
 		mc.setRegistrationID(registrationID);
 		mc.insert();
 	}
 
 	public void dissocMerchandise(Merchandise rel) {
 		MerchandiseChoice mc = new MerchandiseChoice();
 		mc.setRegistrationID(rel.getMerchandiseID());
 		mc.setMerchandiseID(registrationID);
 		mc.delete();
 	}
 
 	public Vector getPayments() {
 		Payment p = new Payment();
 		p.setRegistrationID(registrationID);
 		return p.selectList();
 	}
 
 	public Vector getPayments(String orderField, boolean DESC) {
 		Payment p = new Payment();
 		return p.selectList("fk_RegistrationID = '" + registrationID
 				+ "' ORDER BY " + orderField + " " + (DESC ? "DESC" : "ASC"));
 	}
 
 	public void assocPayment(Payment p) {
 		p.setRegistrationID(registrationID);
 		p.persist();
 	}
 
 	public void dissocPayment(Payment p) {
 		p.setRegistrationID(0);
 		p.persist();
 	}
 
 	public Vector getChildRegistrations() {
 		ChildRegistration cr = new ChildRegistration();
 		cr.setRegistrationID(registrationID);
 		return cr.selectList();
 	}
 
 	public Vector getChildRegistrations(String orderField, boolean DESC) {
 		ChildRegistration cr = new ChildRegistration();
 		return cr.selectList("fk_RegistrationID = '" + registrationID
 				+ "' ORDER BY " + orderField + " " + (DESC ? "DESC" : "ASC"));
 	}
 
 	public void assocChildRegistration(ChildRegistration cr) {
 		cr.setRegistrationID(registrationID);
 		cr.persist();
 	}
 
 	public void dissocChildRegistration(ChildRegistration cr) {
 		cr.setRegistrationID(0);
 		cr.persist();
 	}
 	
 	public Registration getSpouse(){
 		Registration result = null;
 		if (getSpouseRegistrationID() != 0) {
 			result = new Registration(getSpouseRegistrationID());
 		}
 		return result;
 	}
 
 	public Person getSpousePerson(){
 		Person result = null;
 		if (getSpouseID() != 0) {
 			result = new Person(getSpouseID());
 		}
 		return result;
 	}
 
 	public boolean insert() {
 		clearMetadata();
 
 		String table = "crs_registration";
 
 		setMetadata("RegistrationID", "registrationID", "IDENTITY");
 		setMetadata("ConferenceID", "fk_ConferenceID", table);
 		setMetadata("RegistrationTypeID", "fk_RegistrationTypeID", table);
 		setMetadata("PersonID", "fk_PersonID", table);
 		setMetadata("RegistrationDate", "registrationDate", table);
 		setMetadata("PreRegisteredString", "preRegistered", table);
 //		setMetadata("RegistrationType", "registrationType", table);		/* to be deprecated gdw - 2004-09-17 */
 		setMetadata("ArriveDate", "arriveDate", table);
 		setMetadata("LeaveDate", "leaveDate", table);
 		setMetadata("AdditionalRooms", "additionalRooms", table);
 		setMetadata("SpouseComing", "spouseComing", table);
 		setMetadata("RegisteredFirstString", "registeredFirst", table);
 		setMetadata("SpouseRegistrationID", "spouseRegistrationID", table);
 		setMetadata("IsOnsite", "isOnsite", table);
 		setMetadata("NewPersonID", "newPersonID", table);
 		return super.insert();
 	}
 
 	public boolean delete() {
 		clearMetadata();
 
 		String table = "crs_registration";
 
 		setMetadata("RegistrationID", "registrationID", "IDENTITY");
		setMetadata("ConferenceID", "fk_ConferenceID", table);
 		setMetadata("PersonID", "fk_PersonID", table);
 		setMetadata("RegistrationDate", "registrationDate", table);
 		setMetadata("PreRegisteredString", "preRegistered", table);
 //		setMetadata("RegistrationType", "registrationType", table);		/* to be deprecated gdw - 2004-09-17 */
 		setMetadata("ArriveDate", "arriveDate", table);
 		setMetadata("LeaveDate", "leaveDate", table);
 		setMetadata("AdditionalRooms", "additionalRooms", table);
 		setMetadata("SpouseComing", "spouseComing", table);
 		setMetadata("RegisteredFirstString", "registeredFirst", table);
 		setMetadata("SpouseRegistrationID", "spouseRegistrationID", table);
 		setMetadata("NewPersonID", "newPersonID", table);
 		return super.delete();
 	}
 
 	public boolean passInsert() {
 		return super.insert();
 	}
 
 	public boolean update() {
 		clearMetadata();
 
 		String table = "crs_registration";
 
 		setMetadata("RegistrationID", "registrationID", "IDENTITY");
 		setMetadata("ConferenceID", "fk_ConferenceID", table);
 		setMetadata("RegistrationTypeID", "fk_RegistrationTypeID", table);
 		setMetadata("PersonID", "fk_PersonID", table);
 		setMetadata("RegistrationDate", "registrationDate", table);
 		setMetadata("PreRegisteredString", "preRegistered", table);
 //		setMetadata("RegistrationType", "registrationType", table);		/* to be deprecated gdw - 2004-09-17 */
 		setMetadata("ArriveDate", "arriveDate", table);
 		setMetadata("LeaveDate", "leaveDate", table);
 		setMetadata("AdditionalRooms", "additionalRooms", table);
 		setMetadata("SpouseComing", "spouseComing", table);
 		setMetadata("RegisteredFirstString", "registeredFirst", table);
 		setMetadata("SpouseRegistrationID", "spouseRegistrationID", table);
 		setMetadata("IsOnsite", "isOnsite", table);
 		setMetadata("NewPersonID", "newPersonID", table);
 		return super.update();
 	}
 
 	public boolean passUpdate() {
 		return super.update();
 	}
 	/**
 	 * @return Returns the newPersonID.
 	 */
 	public int getNewPersonID() {
 		return newPersonID;
 	}
 	/**
 	 * @param newPersonID The newPersonID to set.
 	 */
 	public void setNewPersonID(int newPersonID) {
 		this.newPersonID = newPersonID;
 	}
 }
