 package org.openmrs.module.amrsregistration.web.controller;
 
 import org.openmrs.api.context.Context;
 
 
 public interface AmrsRegistrationConstants {
 	
 	int START_PAGE = 0;
 	
 	int EDIT_PAGE = 1;
 	
 	int ASSIGN_ID_PAGE = 2;
 	
 	int REVIEW_PAGE = 3;
 	
 	String AMRS_TARGET_ID_KEY = "amrsregistration.idType";
 	
 	String AMRS_TARGET_ID = Context.getAdministrationService().getGlobalProperty(AMRS_TARGET_ID_KEY);
 }
