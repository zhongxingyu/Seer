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
 
 package org.openmrs.module.keaddonexample.page.controller;
 
 import org.openmrs.Patient;
 import org.openmrs.module.appframework.AppUiUtil;
 import org.openmrs.module.keaddonexample.ExampleConstants;
 import org.openmrs.module.kenyaemr.KenyaEmr;
 import org.openmrs.module.kenyaemr.KenyaEmrUiUtils;
 import org.openmrs.module.kenyaemr.form.FormDescriptor;
 import org.openmrs.ui.framework.UiUtils;
 import org.openmrs.ui.framework.annotation.SpringBean;
 import org.openmrs.ui.framework.page.PageModel;
 import org.openmrs.ui.framework.session.Session;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import java.util.Collections;
 
 /**
  * Home page controller
  */
 public class HomePageController {
 	
 	public void controller(@RequestParam(required=false, value="patientId") Patient patient,
 						   Session session,
 						   PageModel model,
 						   UiUtils ui,
 						   @SpringBean KenyaEmr emr,
 						   @SpringBean KenyaEmrUiUtils kenyaUi) {
 
 		AppUiUtil.startApp("keaddonexample.example", session);
 
 		model.addAttribute("patient", patient);
 
		FormDescriptor exampleForm = emr.getFormManager().getFormConfig(ExampleConstants.EXAMPLE_ADDON_FORM_UUID);
 
 		model.addAttribute("forms", Collections.singletonList(kenyaUi.simpleForm(exampleForm, ui)));
 	}
 }
