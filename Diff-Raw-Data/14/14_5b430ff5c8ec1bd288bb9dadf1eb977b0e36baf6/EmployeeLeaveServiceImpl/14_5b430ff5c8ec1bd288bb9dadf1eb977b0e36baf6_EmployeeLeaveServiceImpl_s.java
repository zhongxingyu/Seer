 package com.madrone.lms.service.impl;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.madrone.lms.constants.LMSConstants;
 import com.madrone.lms.dao.EmployeeDao;
 import com.madrone.lms.dao.EmployeeLeaveDao;
 import com.madrone.lms.entity.Department;
 import com.madrone.lms.entity.Employee;
 import com.madrone.lms.entity.EmployeeLeave;
 import com.madrone.lms.entity.Leave;
 import com.madrone.lms.form.LeaveCorrectionForm;
 import com.madrone.lms.form.LeaveDetailsGrid;
 import com.madrone.lms.form.LeaveForm;
 import com.madrone.lms.service.EmployeeLeaveService;
 import com.madrone.lms.utils.DateUtils;
 
 @Service("employeeLeaveService")
 @Transactional(readOnly = true)
 public class EmployeeLeaveServiceImpl implements EmployeeLeaveService {
 
 	@Autowired
 	EmployeeLeaveDao empLeaveDao;
 
 	@Autowired
 	EmployeeDao empDao;
 
 	@Override
 	public EmployeeLeave findById(long id) {
 		return empLeaveDao.findById(id);
 	}
 
 	@Override
 	@Transactional(readOnly = false)
 	public void deleteEmployeeLeave(long id) {
 		EmployeeLeave el = findById(id);
 		empLeaveDao.delete(el);
 	}
 
 	@Override
 	@Transactional(readOnly = false)
 	public void saveEmployeeLeave(EmployeeLeave el) {
 		el.setLeaveStatus(LMSConstants.LEAVE_STATUS_PENDING);
 		empLeaveDao.saveOrUpdate(el);
 	}
 
 	@Override
 	@Transactional(readOnly = false)
 	public void cancelEmployeeLeave(EmployeeLeave el) {
 		el.setLeaveStatus(LMSConstants.LEAVE_STATUS_CANCEL);
 		empLeaveDao.update(el);
 	}
 
 	@Override
 	@Transactional(readOnly = false)
 	public void approveEmployeeLeave(EmployeeLeave el) {
 		el.setLeaveStatus(LMSConstants.LEAVE_STATUS_APPROVE);
 		empLeaveDao.update(el);
 	}
 
 	@Override
 	@Transactional(readOnly = false)
 	public void rejectEmployeeLeave(EmployeeLeave el) {
 		el.setLeaveStatus(LMSConstants.LEAVE_STATUS_REJECT);
 		empLeaveDao.update(el);
 	}
 
 	@Override
 	public List<LeaveDetailsGrid> getLeaveList(String userName) {
 		Employee emp = empDao.findByEmailAddress(userName);
 		List<EmployeeLeave> leaveList = empLeaveDao.getLeaveList(emp);
 		List<LeaveDetailsGrid> returnList = new ArrayList<LeaveDetailsGrid>();
 		if (leaveList != null) {
 			returnList = setBeanValuesForGrid(leaveList);
 		}
 		return returnList;
 	}
 
 	@Override
 	public List<LeaveDetailsGrid> getLeaveListOfTeam(String userName,
 			String filter) {
 		Employee leadEmployee = empDao.findByEmailAddress(userName);
 		List<Employee> teamList = empDao.findTeamList(leadEmployee);
 		List<LeaveDetailsGrid> teamLeaveList = 
 				new ArrayList<LeaveDetailsGrid>();
 		List<EmployeeLeave> leaveList = new ArrayList<EmployeeLeave>();
 
 		for (Employee emp : teamList) {
 			if ("ALL".equals(filter)) {
 				leaveList = empLeaveDao.getPendingLeaveList(emp);
 			} else if (LMSConstants.LEAVE_STATUS_APPROVE.equals(filter)) {
 				leaveList = empLeaveDao.getApprovalLeaveList(emp);
 			} else if (LMSConstants.LEAVE_STATUS_REJECT.equals(filter)) {
 				leaveList = empLeaveDao.getRejectionLeaveList(emp);
 			} else if (LMSConstants.LEAVE_STATUS_CANCEL.equals(filter)) {
 				leaveList = empLeaveDao.getCancellationLeaveList(emp);
 			}
 
 			for (EmployeeLeave el : leaveList) {
 				LeaveDetailsGrid bean = new LeaveDetailsGrid();
 				bean.setId(String.valueOf(el.getId()));
 				bean.setEmpId(el.getEmployee().getId());
 				bean.setEmpName(el.getEmployee().getFirstName());
 				bean.setFromDateSession(el.getFromDateSession());
 				bean.setFromDate(DateUtils.convertCalendarToString(el
 						.getFromDate()));
 				bean.setToDate(DateUtils.convertCalendarToString
 						(el.getToDate()));
 				bean.setToDateSession(el.getToDateSession());
 				bean.setLeaveType(el.getLeave().getId());
 				bean.setNoOfDays(el.getNoOfDays());
 				bean.setStatus(el.getLeaveStatus());
 				if(bean.getStatus().equals(LMSConstants.LEAVE_STATUS_PENDING)) {
 					bean.setReason(el.getReasonForLeave());
 				}
 				else if(bean.getStatus().equals(LMSConstants.LEAVE_STATUS_CANCEL)) {
 					bean.setReason(el.getCancellationComments());
 				}else if(bean.getStatus().equals(LMSConstants.LEAVE_STATUS_APPROVE) || bean.getStatus().equals(LMSConstants.LEAVE_STATUS_REJECT)) {
 					bean.setReason(el.getApprovalComments());
 				}
 				bean.setEmpPrimaryEmail(el.getEmployee().getPrimaryEmail());
 				teamLeaveList.add(bean);
 			}
 		}
 
 		return teamLeaveList;
 	}
 
 	@Override
 	public List<LeaveDetailsGrid> getPendingLeaveList(String userName) {
 		Employee emp = empDao.findByEmailAddress(userName);
 		List<EmployeeLeave> leaveList = empLeaveDao.getPendingLeaveList(emp);
 		List<LeaveDetailsGrid> returnList = new ArrayList<LeaveDetailsGrid>();
 		if (leaveList != null) {
 			returnList = setBeanValuesForGrid(leaveList);
 		}
 		return returnList;
 	}
 
 	@Override
 	public EmployeeLeave setBeanValuesForSave(LeaveForm leaveForm,String operation) {
 		EmployeeLeave el = new EmployeeLeave();
 		Employee e = empDao.findById(leaveForm.getEmpId());
 		el.setEmployee(e);
 
 		Leave l = new Leave();
 		l.setId(leaveForm.getLeaveType());
 		el.setLeave(l);
 
 		el.setId(leaveForm.getId());
 
 		el.setFromDate(DateUtils.convertStringToCalendar(leaveForm
 				.getFromDate()));
 		el.setToDate(DateUtils.convertStringToCalendar(leaveForm.getToDate()));
 		el.setFromDateSession(leaveForm.getFromDateSession());
 		el.setToDateSession(leaveForm.getToDateSession());
 		el.setNoOfDays(leaveForm.getNoOfDays());
 		switch(operation) {
 		case  LMSConstants.LEAVE_APPLY : {
 			el.setReasonForLeave(leaveForm.getReason());
 			break;
 		}
 		case LMSConstants.LEAVE_CANCEL :{
 			el.setCancellationComments(leaveForm.getReason());
 			break;
 		}
 		case LMSConstants.LEAVE_APPROVE :{
 			el.setApprovalComments(leaveForm.getReason());
 			el.setApprovalDate(Calendar.getInstance());
 			break;
 		}
 			
 	}
 		el.setEmergencyPhoneNumber(leaveForm.getEmergencyPhone());
 		return el;
 
 	}
 
 	private List<LeaveDetailsGrid> setBeanValuesForGrid(
 			List<EmployeeLeave> leaveList) {
 
 		List<LeaveDetailsGrid> returnList = new ArrayList<LeaveDetailsGrid>();
 
 		for (EmployeeLeave el : leaveList) {
 			LeaveDetailsGrid bean = new LeaveDetailsGrid();
 			bean.setId(String.valueOf(el.getId()));
 			bean.setEmpId(el.getEmployee().getId());
 			bean.setEmpName(el.getEmployee().getFirstName());
 			bean.setFromDateSession(el.getFromDateSession());
 			bean.setFromDate(DateUtils.convertCalendarToString
 					(el.getFromDate()));
 			bean.setToDate(DateUtils.convertCalendarToString(el.getToDate()));
 			bean.setToDateSession(el.getToDateSession());
 			if(el.getLeaveStatus().equals(LMSConstants.LEAVE_STATUS_PENDING)) {
 				bean.setReason(el.getReasonForLeave());
 			}
 			else if(el.getLeaveStatus().equals(LMSConstants.LEAVE_STATUS_CANCEL)) {
 				bean.setReason(el.getCancellationComments());
 			}else if(el.getLeaveStatus().equals(LMSConstants.LEAVE_STATUS_APPROVE) || el.getLeaveStatus().equals(LMSConstants.LEAVE_STATUS_REJECT)) {
 				bean.setReason(el.getApprovalComments());
 			}
 			bean.setLeaveType(el.getLeave().getId());
 			bean.setNoOfDays(el.getNoOfDays());
 			bean.setStatus(el.getLeaveStatus());
 			returnList.add(bean);
 		}
 		return returnList;
 
 	}
 
 	@Override
 	public List<LeaveDetailsGrid>
 		getPendingAndApprovalLeaveList(String userName) {
 		Employee emp = empDao.findByEmailAddress(userName);
 		List<EmployeeLeave> leaveList = empLeaveDao
 				.getPendingAndApprovalList(emp);
 		List<LeaveDetailsGrid> returnList = new ArrayList<LeaveDetailsGrid>();
 		if (leaveList != null) {
 			returnList = setBeanValuesForGrid(leaveList);
 		}
 		return returnList;
 	}
 
 	@Override
 	public List<LeaveDetailsGrid> getLeaveListForAdmin(
 			LeaveCorrectionForm leaveFormFilter) {
 
 		Department dept = null;
 		Leave leave = null;
 
 		if (!"".equals(leaveFormFilter.getDeptId())) {
 			dept = new Department();
 			dept.setId(leaveFormFilter.getDeptId());
 		}
 		leaveFormFilter.setDept(dept);
 
 		if (!"".equals(leaveFormFilter.getLeaveType())) {
 				leave = new Leave();
 				leave.setId(leaveFormFilter.getLeaveType());
 		}
 		leaveFormFilter.setLeaveTypes(leave);
 
 		List<Employee> employeeList = empDao.getEmployeeList(leaveFormFilter);
 		List<LeaveDetailsGrid> returnList = null;
 
 		if (employeeList != null) {
 			returnList = new ArrayList<LeaveDetailsGrid>();
 			List<EmployeeLeave> leaveList = empLeaveDao.getLeaveListForAdmin(
 					employeeList, leaveFormFilter);
 			if (leaveList != null) {
 				returnList = setBeanValuesForGrid(leaveList);
 			}
 		}
 
 		return returnList;
 	}
 
 }
