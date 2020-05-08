 package org.openmrs.module.addresshierarchy.web.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.api.context.Context;
 import org.openmrs.layout.web.address.AddressSupport;
 import org.openmrs.module.addresshierarchy.AddressField;
 import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
 import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
 import org.openmrs.module.addresshierarchy.exception.AddressHierarchyModuleException;
 import org.openmrs.module.addresshierarchy.propertyeditor.AddressFieldEditor;
 import org.openmrs.module.addresshierarchy.propertyeditor.AddressHierarchyLevelEditor;
 import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
 import org.openmrs.module.addresshierarchy.util.AddressHierarchyImportUtil;
 import org.openmrs.module.addresshierarchy.validator.AddressHierarchyLevelValidator;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.support.SessionStatus;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.servlet.ModelAndView;
 
 
 @Controller
 public class AddressHierarchyAdminController {
 
 	protected static final Log log = LogFactory.getLog(AddressHierarchyAdminController.class);
 	
 	/** Validator for this controller */
 	private AddressHierarchyLevelValidator validator;
 	
 	@Autowired
 	public AddressHierarchyAdminController(AddressHierarchyLevelValidator validator) {
 		this.validator = validator;
 	}
 	
 	@InitBinder
 	public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
 		// register custom binders
 		binder.registerCustomEditor(AddressHierarchyLevel.class, new AddressHierarchyLevelEditor()); 
 		binder.registerCustomEditor(AddressField.class, new AddressFieldEditor()); 
 	}
 	
 	@ModelAttribute("addressFields")
 	public AddressField [] getAddressFields() {
 		return AddressField.values();
 	}
 	
 	@ModelAttribute("nameMappings")
 	public Map<String,String> getAddressNameMappings() {
		return (AddressSupport.getInstance() != null && AddressSupport.getInstance().getDefaultLayoutTemplate() != null) ? AddressSupport.getInstance().getDefaultLayoutTemplate().getNameMappings() : null;
 	}
 	
 	@ModelAttribute("levels")
 	public List<AddressHierarchyLevel> getOrderedAddressHierarchyLevels() {
 		// before getting the levels, we first make sure the parents are set properly (mainly to handle any migration from the 1.2 model)
 		Context.getService(AddressHierarchyService.class).setAddressHierarchyLevelParents();
 		return Context.getService(AddressHierarchyService.class).getAddressHierarchyLevels();
 	}
 	
 	@ModelAttribute("messages")
 	public List<String> showMessage(@RequestParam(value = "message", required = false) String message) {
 		List<String> messages = new ArrayList<String>();
 		if(StringUtils.isNotBlank(message)) {
 			messages.add(message);
 		}
 		return messages;
 	}
 	
 	@ModelAttribute("sampleEntries")
 	public List<List<String>> getSampleEntries(@ModelAttribute("levels") List<AddressHierarchyLevel> levels) {
 		
 		List<List<String>> sampleEntries = new ArrayList<List<String>>();
 		
 		for (AddressHierarchyLevel level : levels) {
 			List<String> sampleEntry = new ArrayList<String>();
 			List<AddressHierarchyEntry> entries = Context.getService(AddressHierarchyService.class).getAddressHierarchyEntriesByLevel(level);
 			if(entries != null && entries.size() > 0) {
 				sampleEntry.add(entries.get(0).getName());
 				sampleEntry.add(String.valueOf(entries.size()));
 			}
 			else {
 				sampleEntry.add("");
 				sampleEntry.add("0");
 			}
 			
 			sampleEntries.add(sampleEntry);
 		}
 		
 		return sampleEntries;
 	}
 	
 	@ModelAttribute("level")
 	public AddressHierarchyLevel getAddressHierarchyLevel(@RequestParam(value = "levelId", required = false) Integer levelId) {
 				
 		AddressHierarchyLevel level;
     	
     	// fetch the address hierarchy level, or if none specified, create a new one
     	if (levelId != null) {
     		level = Context.getService(AddressHierarchyService.class).getAddressHierarchyLevel(levelId);
     		
     		if (level == null) {
     			throw new AddressHierarchyModuleException("Invalid address hierarchy level id " + levelId);
     		}
     	}
     	else {
     		level = new AddressHierarchyLevel();
     		// set the new type to be the child of the bottom-most type in the hierarchy
     		level.setParent(Context.getService(AddressHierarchyService.class).getBottomAddressHierarchyLevel());
     	}
     	
     	return level;
 	}
 	
     @RequestMapping("/module/addresshierarchy/admin/manageAddressHierarchy.form")
 	public ModelAndView manageAddressHierarchy() {
 		return new ModelAndView("/module/addresshierarchy/admin/manageAddressHierarchy");
 	}
     
     @RequestMapping("/module/addresshierarchy/admin/editAddressHierarchyLevel.form")
     public ModelAndView viewAddressHierarchyLevel() {
     	return new ModelAndView("/module/addresshierarchy/admin/editAddressHierarchyLevel");
     }
     
     @SuppressWarnings("unchecked")
     @RequestMapping("/module/addresshierarchy/admin/updateAddressHierarchyLevel.form")
     public ModelAndView updateAddressHierarchyLevel(@ModelAttribute("level") AddressHierarchyLevel level, BindingResult result, SessionStatus status,  ModelMap map) {
     
     	// validate form entries
 		validator.validate(level, result);
 		
 		if (result.hasErrors()) {
 			map.put("errors", result);
 			return new ModelAndView("/module/addresshierarchy/admin/editAddressHierarchyLevel", map);
 		}
     	
 		// add/update the address hierarchy type
 		Context.getService(AddressHierarchyService.class).saveAddressHierarchyLevel(level);
 		
 		// clears the command object from the session
 		status.setComplete();
 		
 		return new ModelAndView("redirect:/module/addresshierarchy/admin/manageAddressHierarchy.form");
     	
     }
     
     @RequestMapping("/module/addresshierarchy/admin/deleteAddressHierarchyLevel.form")
     public ModelAndView deleteAddressHierarchyLevel(@ModelAttribute("level") AddressHierarchyLevel level) {
     	
     	// we are only allowing the deletion of the bottom-most type
     	if (level != Context.getService(AddressHierarchyService.class).getBottomAddressHierarchyLevel()) {
     		throw new AddressHierarchyModuleException("Cannot delete Address Hierarchy Level; not bottom type in the hierarchy");
     	}
     	
     	if (Context.getService(AddressHierarchyService.class).getAddressHierarchyEntryCountByLevel(level) > 0) {
     		throw new AddressHierarchyModuleException("Cannot delete Address Hierarchy Level; it has associated entries");
     	}
     	
     	// deletes the address hierarchy type
     	Context.getService(AddressHierarchyService.class).deleteAddressHierarchyLevel(level);
     	
     	return new ModelAndView("redirect:/module/addresshierarchy/admin/manageAddressHierarchy.form");
     }
  
     @SuppressWarnings("unchecked")
     @RequestMapping("/module/addresshierarchy/admin/uploadAddressHierarchy.form")
 	public ModelAndView processAddressHierarchyUploadForm(@RequestParam("file") MultipartFile file,
 	                                                      @RequestParam("delimiter") String delimiter, 
 	                                                      @RequestParam(value = "overwrite", required = false) Boolean overwrite,
 	                                                      ModelMap map) {	
 				
 		// handle validation
 		if (delimiter == null || delimiter.isEmpty()) {
 			((List<String>) map.get("messages")).add("addresshierarchy.admin.validation.noDelimiter");
 		}
 		if (file == null || file.isEmpty()) {
 			((List<String>) map.get("messages")).add("addresshierarchy.admin.validation.noFile");
 		}
 		if (((List<String>) map.get("messages")).size() > 0) {
 			map.addAttribute("delimiter", delimiter);
 			map.addAttribute("overwrite", overwrite);
 			return new ModelAndView("/module/addresshierarchy/admin/manageAddressHierarchy", map);
 		}
 		
 		// do the actual update
 		try {
 			// delete old records if overwrite has been selected
 			if (overwrite != null && overwrite == true) {
 				Context.getService(AddressHierarchyService.class).deleteAllAddressHierarchyEntries();
 			}
 			
 			// do the actual import
 	        AddressHierarchyImportUtil.importAddressHierarchyFile(file.getInputStream(), delimiter);
         }
         catch (Exception e) {
 	        log.error("Unable to import address hierarchy file", e);
 	        ((List<String>) map.get("messages")).add("addresshierarchy.admin.uploadFailure");
 			map.addAttribute("delimiter", delimiter);
 			map.addAttribute("overwrite", overwrite);
 			return new ModelAndView("/module/addresshierarchy/admin/manageAddressHierarchy", map);
         }
         
 		return new ModelAndView("redirect:/module/addresshierarchy/admin/manageAddressHierarchy.form?message=" +
 								"addresshierarchy.admin.uploadSuccess", map);
 	}
 }
 
