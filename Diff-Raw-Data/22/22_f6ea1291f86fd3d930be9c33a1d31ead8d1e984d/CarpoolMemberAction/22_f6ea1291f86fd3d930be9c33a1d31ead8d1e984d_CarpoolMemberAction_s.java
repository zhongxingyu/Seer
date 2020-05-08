 package com.crs.action;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 
 import com.crs.dao.CrsDAO;
 import java.util.List;
 
 import com.crs.model.CarPoolForm;
 import com.crs.model.CarPoolMemberForm;
 import com.crs.model.EmployeeForm;
 import com.crs.service.EmailService;
 import com.crs.service.LoginService;
 import com.opensymphony.xwork2.ActionSupport;
 
 public class CarpoolMemberAction extends ActionSupport {
 	
 	CrsDAO dao;
 	private CarPoolMemberForm carPoolMember = new CarPoolMemberForm();
 	private CarPoolForm carPoolGroup = new CarPoolForm();
 	private int carpoolGroupID;
 	List<CarPoolMemberForm> memberList = new ArrayList<CarPoolMemberForm>();
 	LoginService svc = new LoginService();
 	private int employeeID;
 	
 	public int getEmployeeID() {
 		return employeeID;
 	}
 
 	public void setEmployeeID(int employeeID) {
 		this.employeeID = employeeID;
 	}
 
 	public List<CarPoolMemberForm> getMemberList() {
 		return memberList;
 	}
 
 	public void setMemberList(List<CarPoolMemberForm> memberList) {
 		this.memberList = memberList;
 	}
 	
 	public int getCarpoolGroupID() {
 		return carpoolGroupID;
 	}
 
 	public void setCarpoolGroupID(int carpoolGroupID) {
 		System.out.println("Setting value : "+carpoolGroupID);
 		this.carpoolGroupID = carpoolGroupID;
 	}
 
 	public CarPoolMemberForm getCarPoolMember() {
 		return carPoolMember;
 	}
 
 	public void setCarPoolMember(CarPoolMemberForm carPoolMember) {
 		this.carPoolMember = carPoolMember;
 	}
 
 	public CarPoolForm getCarPoolGroup() {
 		return carPoolGroup;
 	}
 
 	public void setCarPoolGroup(CarPoolForm carPoolGroup) {
 		this.carPoolGroup = carPoolGroup;
 	}
 
 	public CrsDAO getDao() {
 		return dao;
 	}
 
 	public void setDao(CrsDAO dao) {
 		this.dao = dao;
 	}
 
 	public CarpoolMemberAction() {
 		// TODO Auto-generated constructor stub
 		this.dao = new CrsDAO();
 	}
 	
 	public String checkOut(){
 		System.out.println("Checking out Member Action :"+carpoolGroupID+"======"+getEmployeeID());
 		dao.checkOut(carpoolGroupID,getEmployeeID());
 		EmployeeForm employeeDetails = dao.getLoginRecordWithEmpID(getEmployeeID());
 		System.out.println("Employee Name :"+employeeDetails.getFirstName());
 		carPoolMember = dao.getMemberInfo(getEmployeeID());
 		carPoolMember.setEmployee(employeeDetails);
 		carPoolGroup.setCarpoolID(carpoolGroupID);
 		carPoolGroup.setAtWork(0);
 		setMemberList(dao.retrieveMembers(carPoolMember));
 		return SUCCESS;
 	}
 	
 	public String checkIn(){
 		System.out.println("Checking In "+carpoolGroupID+"========"+getEmployeeID());
 		dao.checkIn(carpoolGroupID);
 		EmployeeForm employeeDetails = dao.getLoginRecordWithEmpID(getEmployeeID());
 		carPoolMember = dao.getMemberInfo(getEmployeeID());
 		carPoolMember.setEmployee(employeeDetails);
 		carPoolGroup.setCarpoolID(carpoolGroupID);
 		carPoolGroup.setAtWork(1);
 		setMemberList(dao.retrieveMembers(carPoolMember));
 		return SUCCESS;
 	}
 	
 	public String optOutCarpool(){
 		System.out.println("Opting out "+carpoolGroupID+"========"+getEmployeeID());
 		dao.optOutCarpool(getEmployeeID());
 		EmployeeForm employeeDetails = dao.getLoginRecordWithEmpID(getEmployeeID());
 		carPoolMember.setEmployee(employeeDetails);
 		return SUCCESS;
 	}
 	
 	public String cancelPickupConfirm() {
 			System.out.println("Cancelling car pool request");
 			EmployeeForm employeeDetails = dao.getLoginRecordWithEmpID(getEmployeeID());
 			carPoolMember = dao.getMemberInfo(getEmployeeID());
 			carPoolMember.setEmployee(employeeDetails);
 			carPoolMember.setEmployeeID(getEmployeeID());
 			carPoolGroup.setCarpoolID(carpoolGroupID);
 			dao.cancelCarpoolPickUp(carPoolMember);
			notifyMembersOfGroup(carpoolGroupID, "User cancelled car pool pick up", employeeDetails.getFirstName());
 			return SUCCESS;
 	}
 	
	public void notifyMembersOfGroup(int carpoolID, String subject, String userName){
 		EmailService emailSvc = new EmailService();
 		List<Object> emailIDList = dao.fetchMembersEmailID(carpoolID);
 		Iterator<Object> iter = emailIDList.iterator();
		StringBuffer messageBuffer = new StringBuffer();
		messageBuffer.append("This email is to notify you that member of your group "+userName+" has cancelled pick up.");
		String emailMessage = messageBuffer.toString();
 		while(iter.hasNext()){
 			String emailID = (String)iter.next();
 			emailSvc.sendSimpleMail(subject, emailMessage, emailID);
 		}
 	}
 
 	public String optOutCrp(){
 		dao.optOutCrp(getEmployeeID());
 		return SUCCESS;
 	}
 	
 	public String confirmEmergency() {
 			EmployeeForm employeeDetails = dao.getLoginRecordWithEmpID(getEmployeeID());
 			carPoolMember = dao.getMemberInfo(getEmployeeID());
 			carPoolMember.setEmployee(employeeDetails);
 			carPoolMember.setEmployeeID(getEmployeeID());
 			carPoolMember.setDateJoined(new Date());
 			carPoolMember.setIsDriver(0);
 			carPoolMember.setIsPickUp(1);
 			carPoolMember.setIsTemporary(1);
 			carPoolGroup.setCarpoolID(carpoolGroupID);
 			dao.processEmergencyRequest(carPoolMember);
 			return SUCCESS;
 	}
 	
 	public String cancelDrivingConfirm() {
 		System.out.println("Cancelling car pool drive");
 		EmployeeForm employeeDetails = dao.getLoginRecordWithEmpID(getEmployeeID());
 		carPoolMember = dao.getMemberInfo(getEmployeeID());
 		carPoolMember.setEmployee(employeeDetails);
 		carPoolMember.setEmployeeID(getEmployeeID());
 		carPoolGroup.setCarpoolID(carpoolGroupID);
 		if((carPoolMember.getEmployee().getPoints()-3) <= 0)
 			return ERROR;
 		else{
 			dao.cancelCarpoolDrive(carPoolMember);
 			CarPoolMemberForm nextDriver = dao.getNextDriver(getEmployeeID(), 0);
 			if(nextDriver != null){
 				dao.updateTemporaryDriver(nextDriver.getEmployeeID());
 			}
 			else{
 				nextDriver = dao.getNextDriver(getEmployeeID(), 1); 
 				dao.updateTemporaryDriver(nextDriver.getEmployeeID());
 			}
 			String message = "Car Pool Driver has cancelled his drive for the day";
			//notifyUsersByEmail(message, carPoolMember);
 			return SUCCESS;
 		}
 	}
 	
 	
 }
