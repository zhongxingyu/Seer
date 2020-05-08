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
 package org.openmrs.contrib.metadatarepository.webapp.controller;
 
 import org.openmrs.contrib.metadatarepository.model.MetadataPackage;
 import org.openmrs.contrib.metadatarepository.webapp.controller.PackageFormController;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.DataBinder;
 
 import static org.junit.Assert.*;
 
 public class PackageFormControllerTest extends BaseControllerTestCase {
 
 	@Autowired
 	private PackageFormController form;
 
 	private MockHttpServletRequest request;
 
 	private MetadataPackage pkg;
 
 	protected transient final Log log = LogFactory.getLog(getClass());
 
 	@Test
 	public void testEdit() throws Exception {
 		log.debug("testing edit...");
 		request = newGet("/packageform");
 		request.addParameter("id", "1");
 
 		pkg = form.showForm(1L);
 		assertNotNull(pkg);
 	}
 
 	@Test
 	public void testSave() throws Exception {
 		request = newGet("/packageform");
 		request.addParameter("id", "1");
 		pkg = form.showForm(1L);
 		assertNotNull(pkg);
 
 		request = newPost("/packageform");
 
 		request.setRemoteUser("admin");
 		pkg = form.showForm(1L);
 		// update required fields
 
 		BindingResult errors = new DataBinder(pkg).getBindingResult();
		form.onSubmit(pkg, errors, request, new MockHttpServletResponse());
 		assertFalse(errors.hasErrors());
 		assertNotNull(request.getSession().getAttribute("successMessages"));
 	}
 
 	@Test
 	public void testRemove() throws Exception {
 		request = newPost("/packageform");
 		request.addParameter("delete", "");
 		pkg = new MetadataPackage();
 		pkg.setId(1L);
 
 		BindingResult errors = new DataBinder(pkg).getBindingResult();
		form.onSubmit(pkg, errors, request, new MockHttpServletResponse());
 
 		assertNotNull(request.getSession().getAttribute("successMessages"));
 	}
 
 	public MockHttpServletRequest newPost(String url) {
 		return new MockHttpServletRequest("POST", url);
 	}
 
 	public MockHttpServletRequest newGet(String url) {
 		return new MockHttpServletRequest("GET", url);
 	}
 
 }
