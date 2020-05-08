 package border.controller;
 
 import java.util.Calendar;
 import java.util.Enumeration;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.*;
 
 import border.repository.AdminUnitRepositoryImpl;
 import border.service.AdminUnitService;
 import border.service.AdminUnitTypeService;
 import border.viewmodel.AdminUnitReportVM;
 import border.model.*;
 
 @Controller
 @RequestMapping("/AdminUnitReport")
 @SessionAttributes("formData")
 public class AdminUnitReportController {
 
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(AdminUnitRepositoryImpl.class);
 
 	@Autowired
 	AdminUnitService adminUnitService;
 	@Autowired
 	AdminUnitTypeService adminUnitTypeService;
 
 	// GET part
 
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String AdminUnitReportHome(
 			Model model,
 			@RequestParam(required = false, value = "AdminUnitTypeID") String _AdminUnitTypeID) {
 
 		LOGGER.info("Entered admin unit report with ID: " + _AdminUnitTypeID);
 
 		// extra validation when parameter entered from url
 		Long adminUnitTypeID = validateTypeID(_AdminUnitTypeID);
 		AdminUnitReportVM adminUnitReportVM = populateViewModelWithData(adminUnitTypeID);
 		model.addAttribute("formData", adminUnitReportVM);
 
 		return "AdminUnitReport";
 	}
 
 	private Long validateTypeID(String _AdminUnitTypeID) {
 		Long adminUnitTypeID;
 
 		try {
 			adminUnitTypeID = Long.decode(_AdminUnitTypeID);
 			// don't accept under 1, don't accept if not present at DB
 			if (adminUnitTypeID < 1L
 					|| adminUnitTypeService.getByID(adminUnitTypeID) == null) {
 				adminUnitTypeID = 1L;
 			}
 		} catch (Exception e) {
 			// if non-numeric stuff is entered
 			adminUnitTypeID = 1L;
 		}
 
 		return adminUnitTypeID;
 	}
 
 	private AdminUnitReportVM populateViewModelWithData(Long adminUnitTypeID) {
 		AdminUnitReportVM formData = new AdminUnitReportVM();
 		formData.setSearchDate(initializeDate());
 		formData.setAdminUnitType(adminUnitTypeService.getByID(adminUnitTypeID));
 		formData.setAdminUnitTypeList(adminUnitTypeService.findAll());
 		formData = setUnitTypeSpecifics(formData);
 		return formData;
 	}
 
 	private String initializeDate() {
 
 		Calendar today = Calendar.getInstance();
 		String dayPart = String.valueOf(today.get(Calendar.DATE));
 		dayPart = guaranteeTwoNumbers(dayPart);
 		String monthPart = String.valueOf(today.get(Calendar.MONTH) + 1);
 		monthPart = guaranteeTwoNumbers(monthPart);
 
 		String dateString = dayPart + "." + monthPart + "."
 				+ today.get(Calendar.YEAR);
 		return dateString;
 	}
 
 	// make sure date-part is two digits long
 	private String guaranteeTwoNumbers(String datePart) {
 		if (datePart.length() == 1) {
 			datePart = "0" + datePart;
 		}
 		return datePart;
 	}
 
 	// all necessary stuff to fill jsp
 	private AdminUnitReportVM setUnitTypeSpecifics(AdminUnitReportVM formData) {
 
 		Long adminUnitTypeID = formData.getAdminUnitType().getAdminUnitTypeID();
 		// String dateString = reFormat(formData.getSearchDate());
 		String dateString = "NOW()";
 
 		// get the units of the type we need
 		formData.setAdminUnitMasterList(adminUnitService.getByAdminUnitTypeID(
 				adminUnitTypeID, dateString));
 
 		// for each unit the subordinates list will be filled automatically
 		// by JPA one-to-many mapping. time limits are not considered
 		// - just like spec says this time
 
 		return formData;
 	}
 
 	// for handling language changes
 	@RequestMapping(value = "/AdminUnitReportForm", method = RequestMethod.GET)
 	public String handleLanguageChange(ModelMap model,
 			@ModelAttribute("formData") AdminUnitReportVM formData,
 			BindingResult bindingResult) {
 
		// to stop dialog re-opening by itself on get request for language
 		formData.setChosenSubordinate(null);
 		formData.setAdminUnitTypeName(null);
 		formData.setAdminUnitMasterName(null);
 		model.addAttribute("formData", formData);
 
 		return "AdminUnitReport";
 	}
 
 	// POST part
 
 	@RequestMapping(value = "/AdminUnitReportForm", method = RequestMethod.POST, params = "BackButton")
 	public String cancelChanges(ModelMap model) {
 		LOGGER.info("Saw the report, now going back.");
 		// jump back to root view
 		return "redirect:/";
 	}
 
 	@RequestMapping(value = "/AdminUnitReportForm", method = RequestMethod.POST, params = "RefreshButton")
 	public String refreshReport(
 			ModelMap model,
 			@ModelAttribute("formData") AdminUnitReportVM formData,
 			BindingResult bindingResult,
 			@RequestParam(value = "adminUnitType.adminUnitTypeID") Long adminUnitTypeID) {
 		LOGGER.info("Will refresh view for adminUnitTypeID: " + adminUnitTypeID);
 
 		// turn it to new get request
 		return "redirect:/AdminUnitReport/?AdminUnitTypeID=" + adminUnitTypeID;
 	}
 
 	// Only option: querying extra information for a subordinate
 	@RequestMapping(value = "/AdminUnitReportForm", method = RequestMethod.POST)
 	public String showExtraInfo(
 			ModelMap model,
 			@ModelAttribute("formData") AdminUnitReportVM formData,
 			BindingResult bindingResult,
 			@RequestParam(value = "adminUnitType.adminUnitTypeID") Long adminUnitTypeID,
 			HttpServletRequest request) {
 
 		Enumeration<String> paramNames = request.getParameterNames();
 
 		while (paramNames.hasMoreElements()) {
 			String paramName = paramNames.nextElement();
 			if (paramName.startsWith("LookButton_")) {
 				formData = compileSubordinateInfo(formData, paramName);
 				break;
 			}
 		}
 
 		model.addAttribute("formData", formData);
 
 		return "AdminUnitReport";
 	}
 
 	// info to be shown on dialog
 	private AdminUnitReportVM compileSubordinateInfo(
 			AdminUnitReportVM formData, String paramName) {
 
 		String adminUnitID = paramName.substring(11);
 
 		AdminUnit au = adminUnitService.getByID(Long.decode(adminUnitID));
 		if (au.getComment().startsWith("Add here extra information")) {
 			au.setComment(null);
 		}
 		formData.setChosenSubordinate(au);
 		formData.setAdminUnitTypeName(adminUnitTypeService.getByID(
 				au.getAdminUnitTypeID()).getName());
 		formData.setAdminUnitMasterName(adminUnitService.getAdminUnitMaster(
 				au.getAdminUnitID()).getName());
 
 		return formData;
 	}
 
 }
