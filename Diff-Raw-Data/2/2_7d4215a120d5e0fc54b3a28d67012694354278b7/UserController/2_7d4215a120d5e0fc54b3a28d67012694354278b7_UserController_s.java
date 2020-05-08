 package com.madrone.lms.controller;
 
 import java.util.ArrayList;
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
 
 import com.madrone.lms.constants.LMSConstants;
 import com.madrone.lms.entity.Department;
 import com.madrone.lms.entity.Role;
 import com.madrone.lms.form.ReportingPerson;
 import com.madrone.lms.form.RoleTypeForm;
 import com.madrone.lms.form.UserForm;
 import com.madrone.lms.service.DepartmentService;
 import com.madrone.lms.service.EmployeeService;
 import com.madrone.lms.service.RoleService;
 import com.madrone.lms.service.UserService;
 import com.madrone.lms.utils.EnumUtils;
 import com.madrone.lms.utils.JsonResponse;
 
 @Controller
 public class UserController {
 
 	@Autowired
 	private UserService userService;
 
 	@Autowired
 	private RoleService roleService;
 
 	@Autowired
 	private DepartmentService deptService;
 
 	@Autowired
 	private EmployeeService empService;
 
 	@Autowired
 	private MessageSource messageSource;
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(UserController.class);
 
 	@RequestMapping(value = "/addUser", method = RequestMethod.GET)
 	public String addUserForm(Model model, UserForm Userform) {
 		model = loadComboValues(model);
 		return LMSConstants.ADMIN_ADD_USER_SCR;
 	}
 
 	@RequestMapping(value = "/deleteUser", method = RequestMethod.GET)
 	public String deleteUserForm(Model model, UserForm Userform) {
 		model = loadComboValues(model);
 		return LMSConstants.ADMIN_DELETE_USER_SCR;
 	}
 
 	@RequestMapping(value = "/modifyUser", method = RequestMethod.GET)
 	public String modifyUserForm(Model model, UserForm Userform) {
 		model = loadComboValues(model);
 		return LMSConstants.ADMIN_MODIFY_USER_SCR;
 	}
 
 	@RequestMapping(value = "/submitAdduser", method = RequestMethod.POST)
 	public String submitAdduser(Model model,
 			@ModelAttribute("UserForm") UserForm userForm,
 			BindingResult result, Map<String, Object> map) {
 
 		logger.info("Inside submitChangePassword method");
 		userService.saveUserAndEmployee(userForm, LMSConstants.INSERT);

 		model.addAttribute("SucessMessage", messageSource.getMessage(
 				"lms.adduser_success_message", new Object[] { "" },
 				Locale.getDefault()));
 
 		return LMSConstants.ADMIN_ADD_USER_SCR;
 
 	}
 
 	@RequestMapping(value = "/submitModifyuser", method = RequestMethod.POST)
 	public String submitModifyuser(Model model,
 			@ModelAttribute("UserForm") UserForm userForm,
 			BindingResult result, Map<String, Object> map) {
 
 		logger.info("Inside submitChangePassword method");
 		userService.saveUserAndEmployee(userForm, LMSConstants.UPDATE);
 
 		model.addAttribute("SucessMessage", messageSource.getMessage(
 				"lms.modifyuser_success_message", new Object[] { "" },
 				Locale.getDefault()));
 
 		return LMSConstants.ADMIN_MODIFY_USER_SCR;
 
 	}
 
 	@RequestMapping(value = "/submitDeleteUser", method = RequestMethod.POST)
 	public String submitDeleteUser(Model model,
 			@ModelAttribute("UserForm") UserForm userForm,
 			BindingResult result, Map<String, Object> map) {
 
 		logger.info("Inside submitDeleteUser method");
 		userService.deleteUser(userForm.getEmail());
 		empService.deleteEmployee(userForm.getNewEmpId());
 
 		model.addAttribute("SucessMessage", messageSource.getMessage(
 				"lms.deleteuser_success_message", new Object[] { "" },
 				Locale.getDefault()));
 
 		return LMSConstants.ADMIN_DELETE_USER_SCR;
 
 	}
 
 	@RequestMapping(value = "/submitSearchUser", method = RequestMethod.POST)
 	public @ResponseBody
 	JsonResponse searchUser(Model model,
 			@ModelAttribute("UserForm") UserForm userForm, BindingResult result) {
 		JsonResponse res = new JsonResponse();
 		userForm = userService.searchUser(userForm.getSearchEmail());
 		if (userForm == null) {
 			res.setStatus("ERROR");
 		} else {
 			res.setStatus("SUCCESS");
 			List<UserForm> userList = new ArrayList<UserForm>();
 			userList.add(userForm);
 			res.setResult(userList);
 			model = loadComboValues(model);
 			model.addAttribute("UserForm", userForm);
 		}
 
 		return res;
 	}
 
 	@RequestMapping(value = "/FindReportingPersonList", method = RequestMethod.POST)
 	public @ResponseBody
 	JsonResponse findReportingList(Model model,
 			@ModelAttribute("UserForm") UserForm userForm, BindingResult result) {
 
 		JsonResponse res = new JsonResponse();
 		String roleId = userForm.getRole();
 		int level = roleService.getLevel(roleId);
 		List<Role> roleListHigher = roleService.getRoleListHigher(level);
 
 		List<ReportingPerson> empList = empService
 				.FindHigherRoles(roleListHigher);
 
 		if (empList != null && empList.size() > 0) {
 			res.setStatus("SUCCESS");
 			res.setResult(empList);
 
 		}
 		return res;
 	}
 
 	private Model loadComboValues(Model model) {
 		List<RoleTypeForm> roleList = roleService.getRoleList();
 		List<Role> reportingToList = roleService.getRoleListHigher(1);
 		List<ReportingPerson> repList = empService
 				.FindHigherRoles(reportingToList);
 
 		List<Department> deptList = deptService.getDepartmentList();
 		List<Department> desigList = EnumUtils.getDesigList();
 
 		model.addAttribute("rolelist", roleList);
 		model.addAttribute("repolist", repList);
 		model.addAttribute("deptlist", deptList);
 		model.addAttribute("desiglist", desigList);
 		model.addAttribute("UserForm", new UserForm());
 
 		return model;
 	}
 
 }
