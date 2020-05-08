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
 
 package org.openmrs.module.feedback.extension.html;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.openmrs.module.Extension;
 import org.openmrs.module.web.extension.AdministrationSectionExt;
 
 public class AdminList extends AdministrationSectionExt {
 	
     @Override
 	public Extension.MEDIA_TYPE getMediaType() {
 		return Extension.MEDIA_TYPE.html;
 	}
 	
 	public String getTitle() {
 		return "feedback.module";
 	}
 	
 	public Map<String, String> getLinks() {
 		
 		Map<String, String> map = new LinkedHashMap<String, String>();
 		
                 map.put("module/feedback/addPredefinedSubject.form", "feedback.predefinedsubjects");
 		map.put("module/feedback/addSeverity.form", "feedback.severities");
 		map.put("module/feedback/addStatus.form", "feedback.statuses");
                 map.put("module/feedback/addFeedback.form", "feedback.submit");
                map.put("module/feedback/feedback.list", "feedback.manageFeedback");
 		map.put("module/feedback/preference.form", "feedback.user.preference");
 		map.put("module/feedback/feedbackProperties.form", "feedback.admin.properties");
 		map.put("module/feedback/feedbackUser.list", "feedback.user.manageFeedback");
                 return map;
 	}
 	
 }
