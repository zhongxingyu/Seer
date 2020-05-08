 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.sdmxhdintegration.web.controller;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jembi.sdmxhd.dsd.DSD;
 import org.jembi.sdmxhd.dsd.KeyFamily;
 import org.openmrs.api.AdministrationService;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
 import org.openmrs.module.sdmxhdintegration.KeyFamilyMapping;
 import org.openmrs.module.sdmxhdintegration.SDMXHDMessage;
 import org.openmrs.module.sdmxhdintegration.SDMXHDMessageValidator;
 import org.openmrs.module.sdmxhdintegration.SDMXHDService;
 import org.openmrs.web.WebConstants;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.bind.support.SessionStatus;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
 
 
 @Controller
 @SessionAttributes("sdmxhdMessage")
 @RequestMapping("/module/sdmxhdintegration/messageUpload")
 public class MessageUploadFormController {
 	
 	private static Log log = LogFactory.getLog(MessageUploadFormController.class);
 	
 	@RequestMapping(method=RequestMethod.GET)
     public void showForm(@RequestParam(value = "sdmxhdmessageid", required = false) Integer sdmxMessageId, ModelMap model) {
 		if (sdmxMessageId != null) {
 	    	SDMXHDService sdmxhdService = (SDMXHDService) Context.getService(SDMXHDService.class);
 	    	SDMXHDMessage sdmxhdMessage = sdmxhdService.getSDMXHDMessage(sdmxMessageId);
 	    	
 	    	model.addAttribute("sdmxhdMessage", sdmxhdMessage);
     	} else {
     		model.addAttribute("sdmxhdMessage", new SDMXHDMessage());
     	}
     }
 	
 	@RequestMapping(method=RequestMethod.POST)
     public String handleSubmission(HttpServletRequest request,
                                    @ModelAttribute("sdmxhdMessage") SDMXHDMessage sdmxhdMessage,
                                    BindingResult result,
                                    SessionStatus status) throws IllegalStateException {
 		
 		DefaultMultipartHttpServletRequest req = (DefaultMultipartHttpServletRequest) request;
 		MultipartFile file = req.getFile("sdmxhdMessage");
 		File destFile = null;
 		
 		if (!(file.getSize() <= 0)) {
 			AdministrationService as = Context.getAdministrationService();
 			String dir = as.getGlobalProperty("sdmxhdintegration.messageUploadDir");
 			String filename = file.getOriginalFilename();
			filename = "[" + (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")).format(new Date()) + "]" + filename;
 			destFile = new File(dir + File.separator + filename);
 			destFile.mkdirs();
 			
 			try {
 	            file.transferTo(destFile);
             }
             catch (IOException e) {
             	HttpSession session = request.getSession();
             	session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Could not save file. Make sure you have setup the upload directory using the configuration page and that the directory is readable by the tomcat user.");
             	return "/module/sdmxhdintegration/messageUpload";
             }
 			
 			sdmxhdMessage.setSdmxhdZipFileName(filename);
 		}
 		
 		new SDMXHDMessageValidator().validate(sdmxhdMessage, result);
 		
 		if (result.hasErrors()) {
 			log.error("SDMXHDMessage object failed validation");
 			if (destFile != null) {
 				destFile.delete();
 			}
 			return "/module/sdmxhdintegration/messageUpload";
 		}
 		
 		SDMXHDService sdmxhdService = Context.getService(SDMXHDService.class);
 		ReportDefinitionService rds = Context.getService(ReportDefinitionService.class);
 		sdmxhdService.saveSDMXHDMessage(sdmxhdMessage);
 		
 		// delete all existing mappings and reports
 		List<KeyFamilyMapping> allKeyFamilyMappingsForMsg = sdmxhdService.getKeyFamilyMappingBySDMXHDMessage(sdmxhdMessage);
 		for (Iterator<KeyFamilyMapping> iterator = allKeyFamilyMappingsForMsg.iterator(); iterator.hasNext();) {
 	        KeyFamilyMapping kfm = iterator.next();
 	        Integer reportDefinitionId = kfm.getReportDefinitionId();
 	        sdmxhdService.purgeKeyFamilyMapping(kfm);
 	        if (reportDefinitionId != null) {
 	        	rds.purgeDefinition(rds.getDefinition(reportDefinitionId));
 	        }
         }
 		
 		// create initial keyFamilyMappings
 		try {
 	        DSD dsd = sdmxhdService.getSDMXHDDataSetDefinition(sdmxhdMessage);
 	        List<KeyFamily> keyFamilies = dsd.getKeyFamilies();
 	        for (Iterator<KeyFamily> iterator = keyFamilies.iterator(); iterator.hasNext();) {
 	            KeyFamily keyFamily = iterator.next();
 	            
 	            KeyFamilyMapping kfm = new KeyFamilyMapping();
             	kfm.setKeyFamilyId(keyFamily.getId());
 	            kfm.setSdmxhdMessage(sdmxhdMessage);
 	            sdmxhdService.saveKeyFamilyMapping(kfm);
             }
         }
         catch (Exception e) {
         	log.error("Error parsing SDMX-HD Message: " + e, e);
         	if (destFile != null) {
         		destFile.delete();
         	}
         	
         	sdmxhdService.purgeSDMXHDMessage(sdmxhdMessage);
         	result.rejectValue("sdmxhdZipFileName", "upload.file.rejected", "This file is not a valid zip file or it does not contain a valid SDMX-HD DataSetDefinition");
         	return "/module/sdmxhdintegration/messageUpload";
         }
         
 		return "redirect:viewSDMXHDMessages.list";
 	}
 
 }
