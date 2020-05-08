 package com.madrone.lms.form;
 
 
 public class LeaveDetailsGrid {
 	
 	private String id;
 	private String fromDate;
 	private String fromDateSession;
 	private String toDate;
 	private String toDateSession;
 	private float noOfDays;
 	private String leaveType;
 	private boolean action;
 	private String status;
 	private String reason;
 	
 	private String empId;
 	private String empName;
 	private String empPrimaryEmail;
 	
 	
 	public String getEmpId() {
 		return empId;
 	}
 	public void setEmpId(String empId) {
 		this.empId = empId;
 	}
 	public String getEmpName() {
 		return empName;
 	}
 	public void setEmpName(String empName) {
 		this.empName = empName;
 	}
 	public String getId() {
 		return id;
 	}
 	public void setId(String id) {
 		this.id = id;
 	}
 	public String getFromDateSession() {
 		return fromDateSession;
 	}
 	public void setFromDateSession(String fromDateSession) {
 		this.fromDateSession = fromDateSession;
 	}
 	public String getToDateSession() {
 		return toDateSession;
 	}
 	public void setToDateSession(String toDateSession) {
 		this.toDateSession = toDateSession;
 	}
 	public String getStatus() {
 		return status;
 	}
 	public void setStatus(String status) {
 		this.status = status;
 	}
 	public String getReason() {
 		return reason;
 	}
 	public void setReason(String reason) {
 		this.reason = reason;
 	}
 	public String getFromDate() {
 		return fromDate;
 	}
 	public void setFromDate(String fromDate) {
 		this.fromDate = fromDate;
 	}
 	public String getToDate() {
 		return toDate;
 	}
 	public void setToDate(String toDate) {
 		this.toDate = toDate;
 	}
 	public float getNoOfDays() {
 		return noOfDays;
 	}
 	public void setNoOfDays(float noOfDays) {
 		this.noOfDays = noOfDays;
 	}
 	public String getLeaveType() {
 		return leaveType;
 	}
 	public void setLeaveType(String leaveType) {
 		this.leaveType = leaveType;
 	}
 	public boolean isAction() {
 		return action;
 	}
 	public void setAction(boolean action) {
 		this.action = action;
 	}
 	public String getEmpPrimaryEmail() {
 		return empPrimaryEmail;
 	}
 	public void setEmpPrimaryEmail(String empPrimaryEmail) {
 		this.empPrimaryEmail = empPrimaryEmail;
 	}
 	
 
 }
