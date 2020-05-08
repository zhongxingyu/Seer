 package com.madrone.lms.controller;
 
 import java.util.List;
 
 import javax.servlet.http.HttpSession;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.madrone.lms.constants.LMSConstants;
 import com.madrone.lms.form.LeaveDetailsGrid;
 import com.madrone.lms.form.LeaveForm;
 import com.madrone.lms.form.ViewLeaveRequestForm;
 import com.madrone.lms.service.EmployeeLeaveService;
 import com.madrone.lms.utils.JSONUtils;
 
 @Controller
 public class ApprovedRejectedListController {
 	@Autowired
 	private EmployeeLeaveService empLeaveService;
 	
 	// Show Form for Summary : Approval List
 	@RequestMapping(value = "/approvalList", method = RequestMethod.GET)
 	public String viewApprovedleaves(Model model, LeaveForm form,
 			HttpSession session) {
 
 		System.out.println("Inside ApproveLeaveSummary()");
 		String userName = (String) session.getAttribute("sessionUser");
 		List<LeaveDetailsGrid> leaveListOfTeam = empLeaveService
 				.getLeaveListOfTeam(userName, "A");
 		String jsonString = JSONUtils.leaveListGridJSON(leaveListOfTeam);
 		System.out.println("ApprovalList-Json" + jsonString);
 		model.addAttribute("jsonString", jsonString);
 		model.addAttribute("ViewLeaveRequestForm", new ViewLeaveRequestForm());
 		return LMSConstants.MANAGER_VIEW_APPROVED_LEAVES_SCR;
 	}
 
 	// Show Form for Summary : Rejection List
 	@RequestMapping(value = "/rejetionList", method = RequestMethod.GET)
 	public String viewRejectedleaves(Model model, LeaveForm form,
 			HttpSession session) {
 		System.out.println("Inside RejectLeaveSummary()");
 		String userName = (String) session.getAttribute("sessionUser");
 		List<LeaveDetailsGrid> leaveListOfTeam = empLeaveService
 				.getLeaveListOfTeam(userName, "R");
 		String jsonString = JSONUtils.leaveListGridJSON(leaveListOfTeam);
 		System.out.println("RejectedList-Json" + jsonString);
 		model.addAttribute("jsonString", jsonString);
 		model.addAttribute("ViewLeaveRequestForm", new ViewLeaveRequestForm());
		return LMSConstants.MANAGER_VIEW_APPROVED_LEAVES_SCR;
 	}
 
 }
