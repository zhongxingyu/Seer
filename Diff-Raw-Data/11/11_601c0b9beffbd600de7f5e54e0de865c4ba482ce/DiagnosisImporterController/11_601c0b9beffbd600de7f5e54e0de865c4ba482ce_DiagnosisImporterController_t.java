 /**
  *  Copyright 2010 Society for Health Information Systems Programmes, India (HISP India)
  *
  *  This file is part of Hospital-core module.
  *
  *  Hospital-core module is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
 
  *  Hospital-core module is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Hospital-core module.  If not, see <http://www.gnu.org/licenses/>.
  *
  **/
 
 
 package org.openmrs.module.hospitalcore.web.controller.concept;
 
 import org.openmrs.api.context.Context;
 import org.openmrs.module.hospitalcore.HospitalCoreService;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller("HCSDiagnosisImporterController")
 @RequestMapping("/module/hospitalcore/conceptImport.form")
 public class DiagnosisImporterController {
 
 // TODO: replace System.out and printStackTrace with log messages
 
 	@RequestMapping(method = RequestMethod.GET)
 	public String getUploadForm(
 			Model model) {
 		model.addAttribute("uploadFile", new UploadFile());
 		return "/module/hospitalcore/concept/uploadForm";
 	}
 
 	@RequestMapping(method = RequestMethod.POST)
 	public String create(UploadFile uploadFile, BindingResult result, Model model) {
 		if (result.hasErrors()) {
			//ghanshyam 25/06/2012 tag BC_IMPOSSIBLE_CAST code Error error = (Error) obj
			
			for (ObjectError obj : result.getAllErrors()) {
				ObjectError error = (ObjectError) obj;
				System.err.println("Error: " + error.toString()) ;
 			}
 			return "/module/hospitalcore/concept/uploadForm";
 		}
 		
 		System.out.println("Begin importing");
 		Integer diagnosisNo = 0;
 		try {
 			HospitalCoreService hcs = (HospitalCoreService) Context.getService(HospitalCoreService.class);
 			diagnosisNo = hcs.importConcepts(uploadFile.getDiagnosisFile().getInputStream(), 
 					uploadFile.getMappingFile().getInputStream(), 
 					uploadFile.getSynonymFile().getInputStream());
 			model.addAttribute("diagnosisNo", diagnosisNo);
 			System.out.println("Diagnosis imported " + diagnosisNo);
 		} catch (Exception e) {
 			e.printStackTrace();
 			model.addAttribute("fail", true);
 			model.addAttribute("error", e.toString());
 		}
 
 		return "/module/hospitalcore/concept/uploadForm";
 	}
 }
