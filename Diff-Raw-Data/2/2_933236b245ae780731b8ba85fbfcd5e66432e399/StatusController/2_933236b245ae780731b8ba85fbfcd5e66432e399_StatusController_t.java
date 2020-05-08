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
 
 package org.openmrs.module.iqchartimport.web.controller;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.module.iqchartimport.EntityBuilder;
 import org.openmrs.module.iqchartimport.Utils;
 import org.openmrs.module.iqchartimport.task.ImportIssue;
 import org.openmrs.module.iqchartimport.task.TaskEngine;
 import org.openmrs.module.iqchartimport.task.ImportTask;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  * Import status AJAX controller
  */
 @Controller("iqChartImportStatusController")
 @RequestMapping("/module/iqchartimport/status")
 public class StatusController {
 
 	protected static final Log log = LogFactory.getLog(StatusController.class);
 	
 	@RequestMapping(method = RequestMethod.GET)
 	public void getProgress(HttpServletRequest request, HttpServletResponse response) throws IOException {
 		Utils.checkSuperUser();
 		
 		ImportTask task = TaskEngine.getCurrentTask();
 		StringBuilder json = new StringBuilder();
 		
 		if (task != null) {
 			EntityBuilder builder = task.getEntityBuilder();
 			
 			String completed = task.isCompleted() ? "true" : "false";
 			String exception;
 			if (task.getException() != null) {
 				String clazz = "'" + task.getException().getClass().getName() + "'";
				String message = (task.getException().getMessage() != null) ? ("'" + task.getException().getMessage().replace("'", "\\'") + "'") : "null";
 				exception = "{ clazz: " + clazz + ", message: " + message + " }";		
 			}
 			else
 				exception = "null";
 			
 			json.append("{\n");
 			json.append("  task: {\n");
 			json.append("    completed: " + completed + ",\n");
 			json.append("    exception: " + exception + ",\n");
 			json.append("    progress: " + task.getProgress() + ",\n");
 			json.append("    timeTaken: " + task.getTimeTaken() + ",\n");
 			json.append("    importedPatients: " + task.getImportedPatients() + ",\n");
 			json.append("    importedEncounters: " + task.getImportedEncounters() + ",\n");
 			json.append("    importedObservations: " + task.getImportedObservations() + ",\n");
 			json.append("    importedOrders: " + task.getImportedOrders() + ",\n");
 			json.append("    cache: { hitCount: " + builder.getCache().getHitCount() + ", missCount: " + builder.getCache().getMissCount() + " },\n");
 			json.append("    issues: [\n");
 			
 			for (ImportIssue issue : task.getIssues()) {
 				json.append("      { patientId: " + issue.getPatient().getPatientId() + ", message: \"" + issue.getMessage() + "\" },\n");
 			}
 			
 			json.append("    ]\n");
 			json.append("  }\n");
 			json.append("}");
 		}
 		else
 			json.append("{ task: null, issues: null }");
 		
 		response.setContentType("application/json");			
 		response.getWriter().write(json.toString());
 	}
 }
