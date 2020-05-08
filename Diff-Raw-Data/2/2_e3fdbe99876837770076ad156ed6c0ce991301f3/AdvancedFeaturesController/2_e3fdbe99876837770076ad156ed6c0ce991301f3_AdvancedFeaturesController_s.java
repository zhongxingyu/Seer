 package org.openmrs.module.addresshierarchy.web.controller;
 
 import org.apache.commons.lang.StringUtils;
 import org.openmrs.GlobalProperty;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.addresshierarchy.AddressHierarchyConstants;
 import org.openmrs.scheduler.TaskDefinition;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 public class AdvancedFeaturesController {
 
 	
 	
 	@RequestMapping("/module/addresshierarchy/admin/advancedFeatures.form")
 	public ModelAndView advancedFeatures(ModelMap map) {
 		
 		// load in the value of any existing task
 		TaskDefinition updaterTask = Context.getSchedulerService().getTaskByName(AddressHierarchyConstants.TASK_NAME_ADDRESS_TO_ENTRY_MAP_UPDATER);
 		
 		if (updaterTask != null) {
 			map.addAttribute("repeatInterval", updaterTask.getRepeatInterval().intValue() / 60);   // convert the repeat interval (in seconds) to minutes
 			map.addAttribute("updaterStarted", updaterTask.getStarted());	
 			
 			// if there is no last start time for the updater, this means the next execution is going to recalculate all the mappings, so set this attribute to true
 			map.addAttribute("recalculateMappings", StringUtils.isBlank(Context.getAdministrationService().getGlobalProperty(AddressHierarchyConstants.GLOBAL_PROP_ADDRESS_TO_ENTRY_MAP_UPDATER_LAST_START_TIME)) ? true : false);
 		}
 				
 		return new ModelAndView("/module/addresshierarchy/admin/advancedFeatures", map);
 	}
 	
 	@RequestMapping("/module/addresshierarchy/admin/scheduleAddressToEntryMapping.form")
 	public ModelAndView processAddressHierarchyUploadForm(@RequestParam(value = "repeatInterval", required = false) Integer repeatInterval,
 	                                                      @RequestParam(value = "updaterStarted", required = false) Boolean updaterStarted,
 	                                                      @RequestParam(value = "recalculateMappings", required = false) Boolean recalculateMappings,
 	                                                      ModelMap map) {	
 		 
 		// load any existing task
 		TaskDefinition updaterTask = Context.getSchedulerService().getTaskByName(AddressHierarchyConstants.TASK_NAME_ADDRESS_TO_ENTRY_MAP_UPDATER);
 		
 		// if there is no task, and the updater has been set to start, nothing to do
 		if (updaterTask == null && (updaterStarted == null || !updaterStarted)) {
 			return new ModelAndView("redirect:/module/addresshierarchy/admin/advancedFeatures.form");
 		}
 		
 		// otherwise, create a new task if need be
 		if (updaterTask == null) {
 			updaterTask = new TaskDefinition();
 			updaterTask.setName(AddressHierarchyConstants.TASK_NAME_ADDRESS_TO_ENTRY_MAP_UPDATER);
 			updaterTask.setTaskClass(AddressHierarchyConstants.TASK_CLASS_ADDRESS_TO_ENTRY_MAP_UPDATER);
 		}
 		
 		// start or stop the task as needed
		if (updaterStarted) {
 			updaterTask.setStarted(true);
 			updaterTask.setStartOnStartup(true);
 			
 		}
 		else {
 			updaterTask.setStarted(false);
 			updaterTask.setStartOnStartup(false);
 		}
 		
 		// set the repeat interval
 		if (repeatInterval != null) {
 			updaterTask.setRepeatInterval(new Long(repeatInterval * 60));
 		}
 		// if the task has been started, make sure we have at least a default repeat interval
 		else if (updaterStarted != null && updaterStarted){
 			updaterTask.setRepeatInterval(AddressHierarchyConstants.TASK_PARAMETER_ADDRESS_ENTRY_MAP_DEFAULT_REPEAT_INTERVAL);
 		}
 		
 		// if the recalculate mappings property has been set, reset the last start time to null (which will result in all address mappings being recalculated)
 		if (recalculateMappings != null && recalculateMappings) {
 			GlobalProperty lastStartTimeGlobalProp = Context.getAdministrationService().getGlobalPropertyObject(AddressHierarchyConstants.GLOBAL_PROP_ADDRESS_TO_ENTRY_MAP_UPDATER_LAST_START_TIME);
 			lastStartTimeGlobalProp.setPropertyValue(null);
 			Context.getAdministrationService().saveGlobalProperty(lastStartTimeGlobalProp);
 		}
 		
 		// save the task
 		if (updaterTask != null) {
 			Context.getSchedulerService().saveTask(updaterTask);
 		}
 		
 		// redirect back to the same page
 		return new ModelAndView("redirect:/module/addresshierarchy/admin/advancedFeatures.form");
 	 }
 }
