 package org.kuali.rice.krworkshop;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;
 import org.kuali.rice.krad.maintenance.MaintenanceDocumentBase;
 
public class TrainingApplication extends PersistableBusinessObjectBase {
 
 	private static final long serialVersionUID = 1L;
 	
	private String id;
 	private String term;
     private String college;
     private String campus;
     private String schoolColor;
 
     private String firstName;
     private String lastName;
     private String email;
     private String gender;
     private Date dob;
     private boolean emailList;
     private String ethnicity;
     private String otherEthnicity;
 
     private String address1;
     private String address2;
     private String city;
     private String state;
     private String zip;
     
     private List<TrainingApplicationReference> references = new ArrayList<TrainingApplicationReference>();
 
     private List<TrainingApplicationPreviousEducation> previousEducation = new ArrayList<TrainingApplicationPreviousEducation>();
 
     private String decisionStatus;
     private boolean accepted;
 
    public String getId() {
         return id;
     }
    public void setId(String id) {
         this.id = id;
     }
     public String getTerm() {
 		return term;
 	}
 	public void setTerm(String term) {
 		this.term = term;
 	}
 	public String getCollege() {
 		return college;
 	}
 	public void setCollege(String college) {
 		this.college = college;
 	}
 	public String getCampus() {
 		return campus;
 	}
 	public void setCampus(String campus) {
 		this.campus = campus;
 	}
 	public String getSchoolColor() {
 		return schoolColor;
 	}
 	public void setSchoolColor(String schoolColor) {
 		this.schoolColor = schoolColor;
 	}
 	public String getFirstName() {
 		return firstName;
 	}
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 	public String getLastName() {
 		return lastName;
 	}
 	public void setLastName(String lastName) {
 		this.lastName = lastName;
 	}
 	public String getEmail() {
 		return email;
 	}
 	public void setEmail(String email) {
 		this.email = email;
 	}
 	public String getGender() {
 		return gender;
 	}
 	public void setGender(String gender) {
 		this.gender = gender;
 	}
 	public Date getDob() {
 		return dob;
 	}
 	public void setDob(Date dob) {
 		this.dob = dob;
 	}
 	public boolean isEmailList() {
 		return emailList;
 	}
 	public void setEmailList(boolean emailList) {
 		this.emailList = emailList;
 	}
 	public String getEthnicity() {
 		return ethnicity;
 	}
 	public void setEthnicity(String ethnicity) {
 		this.ethnicity = ethnicity;
 	}
 	public String getOtherEthnicity() {
 		return otherEthnicity;
 	}
 	public void setOtherEthnicity(String otherEthnicity) {
 		this.otherEthnicity = otherEthnicity;
 	}
 	public String getAddress1() {
 		return address1;
 	}
 	public void setAddress1(String address1) {
 		this.address1 = address1;
 	}
 	public String getAddress2() {
 		return address2;
 	}
 	public void setAddress2(String address2) {
 		this.address2 = address2;
 	}
 	public String getCity() {
 		return city;
 	}
 	public void setCity(String city) {
 		this.city = city;
 	}
 	public String getState() {
 		return state;
 	}
 	public void setState(String state) {
 		this.state = state;
 	}
 	public String getZip() {
 		return zip;
 	}
 	public void setZip(String zip) {
 		this.zip = zip;
 	}
 	public String getDecisionStatus() {
 		return decisionStatus;
 	}
 	public void setDecisionStatus(String decisionStatus) {
 		this.decisionStatus = decisionStatus;
 	}
     public boolean isAccepted() {
         return accepted;
     }
     public void setAccepted(boolean accepted) {
         this.accepted = accepted;
     }
 }
