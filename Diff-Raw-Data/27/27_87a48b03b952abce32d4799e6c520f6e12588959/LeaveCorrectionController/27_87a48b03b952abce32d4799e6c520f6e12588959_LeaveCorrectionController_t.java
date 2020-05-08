 package com.madrone.lms.controller;
 
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
 
 import com.madrone.lms.constants.LMSConstants;
 import com.madrone.lms.entity.Department;
 import com.madrone.lms.entity.Leave;
 import com.madrone.lms.form.LeaveCorrectionForm;
 import com.madrone.lms.form.LeaveDetailsGrid;
 import com.madrone.lms.form.LeaveForm;
 import com.madrone.lms.service.DepartmentService;
 import com.madrone.lms.service.EmployeeLeaveService;
 import com.madrone.lms.service.LeaveService;
 import com.madrone.lms.utils.JSONUtils;
 import com.madrone.lms.utils.JsonResponse;
 
 
 //This is for admin users, who can able to view and edit leave records.
 @Controller
 public class LeaveCorrectionController {
 	@Autowired
 	private EmployeeLeaveService empLeaveService;
 	
 	@Autowired
 	private DepartmentService depService;
 	
 	@Autowired
 	private LeaveService leaveService;
 	
 	@Autowired
 	private MessageSource messageSource;
 	
 	private static final Logger logger = LoggerFactory
 			.getLogger(ApprovedRejectedListController.class);
 	
	@RequestMapping(value = "/leaveCorrections", method = RequestMethod.GET)
 	public String viewLeaveForm(Model model, LeaveCorrectionForm lForm) {
 		loadComboValues(model);
 		return LMSConstants.ADMIN_VIEW_LEAVE_SCR;
 	}
 	
 	private void loadComboValues(Model model) {
 		List<Department> deptList = depService.getDepartmentList();
 		model.addAttribute("deptList", deptList);
 		
 		List<Leave> ltList = leaveService.getAdminLeaveTypes();
 		model.addAttribute("leaveTypes", ltList);
 		
 		model.addAttribute("LeaveCorrectionForm", new LeaveCorrectionForm());
 		
 	}
 
 	@RequestMapping(value = "/searchLeaveCorrection", 
 			method = RequestMethod.POST)
 	public @ResponseBody
 	JsonResponse searchLeaveCorrection(Model model,
 			@ModelAttribute("LeaveCorrectionForm") LeaveCorrectionForm lForm, 
 			BindingResult result) {
 		JsonResponse res = new JsonResponse();
 		
 		logger.info("Inside submitLeaveTransaction()");
 		
 		List<LeaveDetailsGrid> leaveListOfTeam = empLeaveService.
 				getLeaveListForAdmin(lForm);
 		
 		if(leaveListOfTeam!=null) {
 			String jsonString = JSONUtils.convertListToJson(leaveListOfTeam);
 			model.addAttribute("jsonString",jsonString);
 			res.setResult(jsonString);
 			res.setStatus("SUCCESS");
 			loadComboValues(model);
 		}
 		else {
 			res.setStatus("ERROR");
 			loadComboValues(model);
 		}
 
 		return res;
 	}
 	
 
 	@RequestMapping(value = "/submitDeleteInLeaveCorrection", 
 			method = RequestMethod.POST)
	public ModelAndView submitDeleteInLeaveCorrection(@ModelAttribute("LeaveCorrectionForm") LeaveCorrectionForm leform,
			                                    BindingResult result, Map<String, Object> map, RedirectAttributes ra) { 
 		
		ModelAndView modelView = new ModelAndView(new RedirectView(LMSConstants.ADMIN_LEAVE_CORRECTIONS_URL));
 		String jsonString = leform.getSelecteddata();
 
 		if(!"".equals(jsonString)) {
 			LeaveForm leaveForm = JSONUtils
 					.convertJsonToObjectToClass(jsonString);
 			if (leaveForm != null) {
 				empLeaveService.deleteEmployeeLeave(leaveForm.getId());
				ra. addFlashAttribute("SucessMessage", messageSource.getMessage(
 					"lms.leaveCorrection.delete_message", new Object[] { "" },
 					Locale.getDefault()));
 			}
 		}
 		
		return modelView;
 	}
 
 
 }
