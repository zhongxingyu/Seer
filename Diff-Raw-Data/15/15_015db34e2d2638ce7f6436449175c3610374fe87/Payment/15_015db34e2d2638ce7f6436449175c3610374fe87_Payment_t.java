 package org.alt60m.crs.model;
 
 import com.kenburcham.framework.dbio.DBIOEntity;
 import com.kenburcham.framework.dbio.DBIOEntityException;
 import java.util.*;
 
 public class Payment extends DBIOEntity {
 	public Payment() {
 	}
 
 	public boolean isPKEmpty() {
 		return paymentID == 0;
 	}
 
 	public void localinit() throws DBIOEntityException {
 		String table = "crs_payment";
 		setMetadata("PaymentID", "paymentID", "IDENTITY");
 
		setMetadata("RegistrationID", "fk_RegistrationID", "KEY");
 
 		setMetadata("PaymentDate", "paymentDate", table);
 		setMetadata("Credit", "credit", table);
 		setMetadata("Debit", "debit", table);
 		setMetadata("Type", "type", table);
 		setMetadata("AuthCode", "authCode", table);
 		setMetadata("AccountNo", "accountNo", table);
 		setMetadata("BusinessUnit", "businessUnit", table);
 		setMetadata("Dept", "dept", table);
 //		setMetadata("Region", "region", table);
 		setMetadata("OperatingUnit", "operatingUnit", table);
 		setMetadata("Project", "project", table);
 		setMetadata("Comment", "comment", table);
 		setMetadata("PostedString", "posted", table);
 		setMetadata("PostedDate", "postedDate", table);
 
 		setAutodetectProperties(false);
 	}
 
 	private int paymentID = 0;
 
 	private int fk_RegistrationID = 0;
 
 	private Date paymentDate = null;
 
 	private float credit = 0;
 
 	private float debit = 0;
 
 	private String type = "";
 
 	private String authCode = "";
 
 	private String accountNo = "";
 
 	private String businessUnit = "";
 
 	private String dept = "";
 
 //	private String region = "";
 
 	private String operatingUnit= "";
 
 	private String project = "";
 
 	private String comment = "";
 
 	private String posted = "";
 
 	private Date postedDate = null;
 
 	public int getPaymentID() {
 		return paymentID;
 	}
 
 	public void setPaymentID(int paymentID) {
 		this.paymentID = paymentID;
 	}
 
 	public Date getPaymentDate() {
 		return paymentDate;
 	}
 
 	public void setPaymentDate(Date paymentDate) {
 		this.paymentDate = paymentDate;
 	}
 
 	public float getCredit() {
 		return credit;
 	}
 
 	public void setCredit(float credit) {
 		this.credit = credit;
 	}
 
 	public float getDebit() {
 		return debit;
 	}
 
 	public void setDebit(float debit) {
 		this.debit = debit;
 	}
 
 	public String getType() {
 		return type;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public String getAuthCode() {
 		return authCode;
 	}
 
 	public void setAuthCode(String authCode) {
 		this.authCode = authCode;
 	}
 
 	public String getAccountNo() {
 		return accountNo;
 	}
 
 	public void setAccountNo(String accountNo) {
 		this.accountNo = accountNo;
 	}
 
 	public String getBusinessUnit() {
 		return businessUnit;
 	}
 
 	public void setBusinessUnit(String businessUnit) {
 		this.businessUnit = businessUnit;
 	}
 
 	public String getDept() {
 		return dept;
 	}
 
 	public void setDept(String dept) {
 		this.dept = dept;
 	}
 /*	replaced with operating unit because that is what it is called
 	public String getRegion() {
 		return region;
 	}
 	public void setRegion(String region) {
 		this.region = region;
 	}
 */
 	public String getOperatingUnit() {
 		return operatingUnit;
 	}
 
 	public void setOperatingUnit(String operatingUnit) {
 		this.operatingUnit = operatingUnit;
 	}
 
 	public String getProject() {
 		return project;
 	}
 
 	public void setProject(String project) {
 		this.project = project;
 	}
 
 	public String getComment() {
 		return comment;
 	}
 
 	public void setComment(String comment) {
 		this.comment = comment;
 	}
 
 	public boolean getPosted() {
 		return posted != null && posted.equals("T");
 	}
 
 	public void setPosted(boolean posted) {
 		this.posted = posted ? "T" : "F";
 	}
 
 	public String getPostedString() {
 		return posted;
 	}
 
 	public void setPostedString(String posted) {
 		this.posted = posted;
 	}
 
 	public Date getPostedDate() {
 		return postedDate;
 	}
 
 	public void setPostedDate(Date postedDate) {
 		this.postedDate = postedDate;
 	}
 
 	public int getRegistrationID() {
 		return fk_RegistrationID;
 	}
 
 	public void setRegistrationID(int registrationID) {
 		this.fk_RegistrationID = registrationID;
 	}
 
 	public Registration getRegistration() {
 		Registration r = new Registration();
 		r.setRegistrationID(fk_RegistrationID);
 		r.select();
 		return r;
 	}
 
 	public void setRegistration(Registration r) {
 		fk_RegistrationID = r.getRegistrationID();
 	}
 
 }
