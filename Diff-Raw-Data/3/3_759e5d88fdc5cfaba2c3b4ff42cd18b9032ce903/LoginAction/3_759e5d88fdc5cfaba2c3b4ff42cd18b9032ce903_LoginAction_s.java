 package com.crs.action;
 
 import java.util.*;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.crs.dao.CrsDAO;
 import com.crs.interfaces.LoginServiceInterface;
 import com.crs.model.*;
 import com.crs.service.LoginService;
 import com.opensymphony.xwork2.ActionSupport;
 import com.opensymphony.xwork2.ModelDriven;
 import com.opensymphony.xwork2.validator.annotations.Validation;
 
 public class LoginAction extends ActionSupport implements ModelDriven<EmployeeForm> {
 
 	private EmployeeForm employee = new EmployeeForm();
 	private CarPoolMemberForm carPoolMember = new CarPoolMemberForm();
 	private CarPoolForm carPoolGroup = new CarPoolForm();
 	LoginServiceInterface loginService = new LoginService();
 	List<CarPoolMemberForm> memberList = new ArrayList<CarPoolMemberForm>();
 	CrsDAO dao;
 	
 	public LoginAction() {
 		// TODO Auto-generated constructor stub
 		this.dao = new CrsDAO();
 	}
 	
 	public CrsDAO getDao() {
 		return dao;
 	}
 
 	public void setDao(CrsDAO dao) {
 		this.dao = dao;
 	}
 
 	public CarPoolForm getCarPoolGroup() {
 		return carPoolGroup;
 	}
 
 	public void setCarPoolGroup(CarPoolForm carPoolGroup) {
 		this.carPoolGroup = carPoolGroup;
 	}
 	
 	public List<CarPoolMemberForm> getMemberList() {
 		return memberList;
 	}
 
 	public void setMemberList(List<CarPoolMemberForm> memberList) {
 		this.memberList = memberList;
 	}
 
 	public EmployeeForm getEmployee() {
 		return employee;
 	}
 
 	public void setEmployee(EmployeeForm employee) {
 		this.employee = employee;
 	}
 	
 	public CarPoolMemberForm getCarPoolMember() {
 		return carPoolMember;
 	}
 
 	public void setCarPoolMember(CarPoolMemberForm carPoolMember) {
 		this.carPoolMember = carPoolMember;
 	}
 
 	public String login() {
 		System.out.println("======In Login Action login========");
 
 		//EmployeeForm employeeDetails;
 		memberList = loginService.login(employee);
 		if(memberList.size() != 0 && memberList.get(0) != null)
 			setCarPoolMember(loginService.getCarPoolMemberDetails());
 		setCarPoolGroup(loginService.getCarPoolGroupDetails());
 		getCarPoolMember().setEmployee(loginService.getEmployeeDetails(employee));
 		//System.out.println("Member Details ===== "+getCarPoolMember().getIsDriver());
 		System.out.println("Group Details ===== "+memberList.get(0));
 			if(memberList.size() == 1 && memberList.get(0) == null)
 				return SUCCESS;
 			else if(memberList.size() == 0)
 				return ERROR;
 			else
 				return SUCCESS;
 				
 	}
 
 	/**
 	 * This method is called when the user clicks on the 
 	 * submit to register into the car pool system.
 	 * Call to this method is configured in struts.xml
 	 * @return
 	 */
 	public String registerNewUser() {
 		System.out.println("======In Login Action register========");
 	
 		employee.setDateJoined(new Date());
 		employee.setPoints(10);
 		if (employee.getNotifyTypeStr().equals("Email"))
 			employee.setNotifyType(0);
 		else
 			employee.setNotifyType(1);
 		memberList = loginService.registerNewUser(employee);
		setCarPoolMember(loginService.getCarPoolMemberDetails());
		setCarPoolGroup(loginService.getCarPoolGroupDetails());
 			return SUCCESS;
 	}
 	
 	@Override
 	public EmployeeForm getModel() {
 		// TODO Auto-generated method stub
 		return employee;
 	}
 
 }
