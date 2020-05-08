 package border.controller;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.Valid;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 import border.model.AdminUnit;
 import border.model.AdminUnitType;
 import border.repository.AdminUnitRepositoryImpl;
 import border.service.*;
 import border.viewmodel.AdminUnitVM;
 
 @Controller
 @RequestMapping("/AdminUnit")
 @SessionAttributes("formData")
 public class AdminUnitController {
 
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(AdminUnitRepositoryImpl.class);
 
 	@Autowired
 	AdminUnitService adminUnitService;
 	@Autowired
 	AdminUnitTypeService adminUnitTypeService;
 
 	// GET part
 
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String adminUnitHome(
 			Model model,
 			@RequestParam(required = false, value = "AdminUnitID") String _AdminUnitID) {
 		LOGGER.info("adminUnit home for " + _AdminUnitID);
 
 		// set up the amdminUnitID
 		Long adminUnitID;
 		try {
 			adminUnitID = Long.decode(_AdminUnitID);
 		} catch (Exception e) {
 			adminUnitID = 0L;
 		}
 
 		AdminUnitVM adminUnitVM = populateViewModelWithData(adminUnitID);
 		model.addAttribute("formData", adminUnitVM);
 
 		return "AdminUnit";
 	}
 
 	private AdminUnitVM populateViewModelWithData(Long adminUnitID) {
 
 		AdminUnitVM formData = new AdminUnitVM();
 		formData.setAdminUnitTypeList(adminUnitTypeService.findAll());
 
 		if (adminUnitID == 0) {
 			formData.setAdminUnit(new AdminUnit());
 			formData.getAdminUnit().setAdminUnitTypeID(0L);
 		} else {
 			// unit itself
 			formData.setAdminUnit(adminUnitService.getByID(adminUnitID));
 
 			// current unit type
 			formData.setAdminUnitType(adminUnitTypeService.getByID(formData
 					.getAdminUnit().getAdminUnitTypeID()));
 		}
 
 		// its current master
 		formData.setAdminUnitMaster(adminUnitService
 				.getAdminUnitMasterWithZero(formData.getAdminUnit()
 						.getAdminUnitID()));
 		LOGGER.info("Found a master: "
 				+ formData.getAdminUnitMaster().getName());
 
 		formData.setAdminUnitMasterID(formData.getAdminUnitMaster()
 				.getAdminUnitID());
 
 		// possible masters
 		formData.setAdminUnitMasterListWithZero(
 				adminUnitService.getAllowedMasters(formData.getAdminUnit()
 						.getAdminUnitTypeID()), formData.getAdminUnitMaster());
 
 		// its current slaves
 		formData.setAdminUnitsSubordinateList(adminUnitService
 				.getAdminUnitSubordinates(adminUnitID));
 
 		for (AdminUnit sub : formData.getAdminUnitsSubordinateList()) {
 			LOGGER.info("Found a slave: " + sub.getName());
 		}
 
 		// those that might still be enslaved
 		formData.setAdminUnitsSubordinateListPossible(adminUnitService
 				.getAdminUnitSubordinatesPossible(formData.getAdminUnit()
 						.getAdminUnitTypeID()));
 
 		for (AdminUnit sub : formData.getAdminUnitsSubordinateListPossible()) {
 			LOGGER.info("A possible slave: " + sub.getName());
 		}
 
 		// initiate list of slaves that might be freed
 		formData.setAdminUnitsSubordinateListRemoved(new ArrayList<AdminUnit>());
 
 		return formData;
 	}
 
 	// POST part
 
 	@RequestMapping(value = "/AdminUnitForm", method = RequestMethod.POST, params = "SubmitButton")
 	public String saveChanges(ModelMap model,
 			@Valid @ModelAttribute("formData") AdminUnitVM formData,
 			BindingResult bindingResult) {
 
 		model.addAttribute("formData", formData);
 		
 		if (bindingResult.hasErrors()) {
 			LOGGER.info("Some errors, no saving: " + bindingResult);			
 			return "AdminUnit";
 		}
 		LOGGER.info("Will go and save things.");
 
 		// save basic changes; if this was new entry then id has now value
 		formData.setAdminUnit(adminUnitService.save(formData.getAdminUnit()));
 
 		// save relationship between chosen unit and its master
 		adminUnitService.saveSubordination(formData.getAdminUnit().getAdminUnitID(),
 				formData.getAdminUnitMasterID(), "NOW()");
 
 		// update the master for all subordinates
 		for (AdminUnit sub : formData.getAdminUnitsSubordinateList()) {
 			adminUnitService.saveSubordination(sub.getAdminUnitID(), formData
 					.getAdminUnit().getAdminUnitID(), "NOW()");
 		}
 		// remove subordination entries for abandoned subordinates
 		for (AdminUnit subEx : formData
 				.getAdminUnitsSubordinateListRemoved()) {
 			adminUnitService.saveSubordination(subEx.getAdminUnitID(), 0L, "NOW()");
 		}
 
 		return "redirect:/";
 	}
 
 	@RequestMapping(value = "/AdminUnitForm", method = RequestMethod.POST, params = "CancelButton")
 	public String cancelChanges(ModelMap model) {
 		LOGGER.info("/cancelChanges - no save, return to root view ");
 		// jump back to root view
 		return "redirect:/";
 	}
 
 	@RequestMapping(value = "/AdminUnitForm", method = RequestMethod.POST, params = "AddSubordinateButton")
 	public String addSubordinate(
 			ModelMap model,
 			@Valid @ModelAttribute("formData") AdminUnitVM formData,
 			BindingResult bindingResult,
 			@RequestParam(value = "AdminUnit_NewSubordinateNo") Integer adminUnit_NewSubordinateNo) {
 
 		formData = makeChangesToAdminUnitType(formData);
 
 		AdminUnit addedSubordinate = formData
 				.getAdminUnitsSubordinateListPossible().get(
 						adminUnit_NewSubordinateNo);
 
 		LOGGER.info("Adding a new subordinate: " + addedSubordinate.getName());
 
 		// add the desired subordinate into our slaves list
 		if (formData.getAdminUnitsSubordinateList() == null) {
 			formData.setAdminUnitsSubordinateList(new ArrayList<AdminUnit>());
 		}
 		formData.getAdminUnitsSubordinateList().add(addedSubordinate);
 
 		// check if unit was removed during session, get it out of this list
 		if (formData.getAdminUnitsSubordinateListRemoved().contains(
 				addedSubordinate)) {
 			formData.getAdminUnitsSubordinateListRemoved().remove(
 					addedSubordinate);
 		}
 
 		// finally, get it out of the candidates list where it belongs no more
 		formData.getAdminUnitsSubordinateListPossible()
 				.remove(addedSubordinate);
 
 		return "AdminUnit";
 	}
 
 	// posts for changing adminunittype or removing subordinates
 	@RequestMapping(value = "/AdminUnitForm", method = RequestMethod.POST)
 	public String removeSubOrChangeType(ModelMap model,
 			@Valid @ModelAttribute("formData") AdminUnitVM formData,
 			BindingResult bindingResult, HttpServletRequest request) {
 
 		formData = makeChangesToAdminUnitType(formData);
 
 		Enumeration<String> paramNames = request.getParameterNames();
 
 		while (paramNames.hasMoreElements()) {
 			String paramName = paramNames.nextElement();
 
 			if (paramName.startsWith("RemoveButton")) {
 				Integer removeSubLineNo = Integer.parseInt(paramName
 						.substring(13));
 
 				AdminUnit removedSubordinate = formData
 						.getAdminUnitsSubordinateList().get(removeSubLineNo);
 
 				LOGGER.info("Removing subordinate: "
 						+ removedSubordinate.getName());
 
 				// add the unit to candidates list
 				if (formData.getAdminUnitsSubordinateListPossible() == null)
 					formData.setAdminUnitMastersListPossible(new ArrayList<AdminUnit>());
 
 				formData.getAdminUnitsSubordinateListPossible().add(
 						removedSubordinate);
 
 				// add the unit to freed slaves list
 				formData.getAdminUnitsSubordinateListRemoved().add(
 						removedSubordinate);
 
 				// finally, remove the unit from active slaves list
 				formData.getAdminUnitsSubordinateList().remove(
 						removedSubordinate);
 
 				break;
 			}
 		}
 
 		return "AdminUnit";
 	}
 
 	// if user changed type of the unit, all subordinations are removed,
 	// new possible subordinations evoked
 	private AdminUnitVM makeChangesToAdminUnitType(AdminUnitVM formData) {
 		Long adminUnitTypeID = formData.getAdminUnit().getAdminUnitTypeID();
 
 		// if type changed or new unit had no type until now
 		if (formData.getAdminUnitType() == null 
 				|| adminUnitTypeID != formData.getAdminUnitType().getAdminUnitTypeID()) {
 			formData.setAdminUnitType(adminUnitTypeService
 					.getByID(adminUnitTypeID));
 
 			// remove all current subordinates from session
 			formData.getAdminUnitsSubordinateListRemoved().addAll(
 					formData.getAdminUnitsSubordinateList());
 			formData.getAdminUnitsSubordinateList().clear();
 
 			// find new possible subordinates for new unit type
 			formData.setAdminUnitsSubordinateListPossible(adminUnitService
 					.getAdminUnitSubordinatesPossible(formData.getAdminUnit()
 							.getAdminUnitTypeID()));
 
 			// remove current master, make unit masterless
 			formData.setAdminUnitMaster(adminUnitService.getEmptyAdminUnit());
 			formData.setAdminUnitMasterID(0L);
 
 			// find new possible masters for new unit type
 			formData.setAdminUnitMasterListWithZero(adminUnitService
 					.getAllowedMasters(formData.getAdminUnit()
 							.getAdminUnitTypeID()), formData
 					.getAdminUnitMaster());
 		}
 
 		return formData;
 	}
 }
