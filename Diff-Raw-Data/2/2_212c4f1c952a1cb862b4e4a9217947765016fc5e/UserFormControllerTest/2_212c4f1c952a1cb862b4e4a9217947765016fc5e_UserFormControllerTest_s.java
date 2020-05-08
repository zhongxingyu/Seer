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
 
 import org.openmrs.contrib.metadatarepository.Constants;
 import org.openmrs.contrib.metadatarepository.model.User;
 import org.openmrs.contrib.metadatarepository.service.UserManager;
 import org.openmrs.contrib.metadatarepository.webapp.controller.UserFormController;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.security.access.AccessDeniedException;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.DataBinder;
 import org.openmrs.contrib.metadatarepository.webapp.controller.BaseControllerTestCase;
 
 import static org.junit.Assert.*;
 
 public class UserFormControllerTest extends BaseControllerTestCase {
     @Autowired
     private UserFormController c = null;
     private MockHttpServletRequest request;
     private User user;
 
     @Test
     public void testAdd() throws Exception {
         log.debug("testing add new user...");
         request = newGet("/userform.html");
         request.addParameter("method", "Add");
         request.addUserRole(Constants.ADMIN_ROLE);
 
         user = c.showForm(request, new MockHttpServletResponse());
         assertNull(user.getUsername());
     }
 
     @Test
     public void testAddWithoutPermission() throws Exception {
         log.debug("testing add new user...");
         request = newGet("/userform.html");
         request.addParameter("method", "Add");
 
         try {
             c.showForm(request, new MockHttpServletResponse());
             fail("AccessDeniedException not thrown...");
         } catch (AccessDeniedException ade) {
             assertNotNull(ade.getMessage());
         }     
     }
 
     @Test
     public void testCancel() throws Exception {
         log.debug("testing cancel...");
         request = newPost("/userform.html");
         request.addParameter("cancel", "");
 
         BindingResult errors = new DataBinder(user).getBindingResult();
         String view = c.onSubmit(user, errors, request, new MockHttpServletResponse());
 
         assertEquals("redirect:/mainMenu", view);
     }
 
     @Test
     public void testEdit() throws Exception {
         log.debug("testing edit...");
         request = newGet("/userform.html");
         request.addParameter("id", "1"); // regular user
         request.addUserRole(Constants.ADMIN_ROLE);
 
         User user = c.showForm(request, new MockHttpServletResponse());
         assertEquals("Tomcat User", user.getFullName());
     }
 
     @Test
     public void testEditWithoutPermission() throws Exception {
         log.debug("testing edit...");
         request = newGet("/userform.html");
         request.addParameter("id", "1"); // regular user
 
         try {
             c.showForm(request, new MockHttpServletResponse());
             fail("AccessDeniedException not thrown...");
         } catch (AccessDeniedException ade) {
             assertNotNull(ade.getMessage());
         }
     }
 
     @Test
     public void testEditProfile() throws Exception {
         log.debug("testing edit profile...");
         request = newGet("/userform.html");
         request.setRemoteUser("user");
 
         user = c.showForm(request, new MockHttpServletResponse());
         assertEquals("Tomcat User", user.getFullName());
     }
 
     @Test
     public void testSave() throws Exception {
         request = newPost("/userform.html");
         // set updated properties first since adding them later will
         // result in multiple parameters with the same name getting sent
         User user = ((UserManager) applicationContext.getBean("userManager")).getUser("1");
         user.setConfirmPassword(user.getPassword());
         user.setLastName("Updated Last Name");
 
         BindingResult errors = new DataBinder(user).getBindingResult();
         c.onSubmit(user, errors, request, new MockHttpServletResponse());
 
         assertFalse(errors.hasErrors());
         assertNotNull(request.getSession().getAttribute("successMessages"));
     }
 
     @Test
     public void testAddWithMissingFields() throws Exception {
         request = newPost("/userform.html");
         user = new User();
         user.setFirstName("Jack");
         request.setRemoteUser("user");
 
         BindingResult errors = new DataBinder(user).getBindingResult();
         c.onSubmit(user, errors, request, new MockHttpServletResponse());
         
        assertTrue(errors.getAllErrors().size() == 10);
     }
 
     @Test
     public void testRemove() throws Exception {
         request = newPost("/userform.html");
         request.addParameter("delete", "");
         user = new User();
         user.setId(2L);
 
         BindingResult errors = new DataBinder(user).getBindingResult();
         c.onSubmit(user, errors, request, new MockHttpServletResponse());
         
         assertNotNull(request.getSession().getAttribute("successMessages"));
     }
 }
