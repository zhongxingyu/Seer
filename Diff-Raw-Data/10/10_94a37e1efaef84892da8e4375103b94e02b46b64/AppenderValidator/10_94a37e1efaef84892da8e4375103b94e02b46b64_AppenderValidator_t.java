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
 package org.openmrs.module.logmanager.validator;
 
 import java.util.Collection;
 
 import org.openmrs.api.context.Context;
 import org.openmrs.module.logmanager.AppenderProxy;
 import org.openmrs.module.logmanager.Constants;
 import org.openmrs.module.logmanager.LayoutType;
 import org.openmrs.module.logmanager.LogManagerService;
 import org.springframework.validation.Errors;
 import org.springframework.validation.Validator;
 
 /**
  * Validator for appender objects
  */
 public class AppenderValidator implements Validator {
 
 	/**
 	 * @see org.springframework.validation.Validator#supports(Class)
 	 */
 	@SuppressWarnings("unchecked")
 	public boolean supports(Class clazz) {
 		return AppenderProxy.class.isAssignableFrom(clazz);
 	}
 
 	/**
 	 * @see org.springframework.validation.Validator#validate(Object, Errors)
 	 */
 	public void validate(Object obj, Errors errors) {
 		AppenderProxy appender = (AppenderProxy)obj;
 		
 		// General validation
 		if (!appender.getName().matches("[\\w\\.\\- ]+"))
 			errors.rejectValue("name", Constants.MODULE_ID + ".error.name");
		else if (!appender.isExisting() && isAppenderNameInUse(appender.getName()))
 			errors.rejectValue("name", Constants.MODULE_ID + ".error.nameAlreadyInUse");
 		
 		if (appender.getLayoutType() == LayoutType.PATTERN && appender.getLayoutPattern().isEmpty())
 			errors.rejectValue("layout", Constants.MODULE_ID + ".error.layout");
 		
 		// Subclass validation
 		switch (appender.getType()) {
 		case MEMORY:
 			validateMemoryAppender(appender, errors);
 			break;
 		case SOCKET:
 			validateSocketAppender(appender, errors);
 			break;
 		case NT_EVENT_LOG:
 			validateNTEventLogAppender(appender, errors);
 			break;
 		}		
 	}
 	
 	/**
 	 * Validation specific to memory appenders
 	 * @param appender the appender to validate
 	 * @param errors the errors
 	 */
 	private void validateMemoryAppender(AppenderProxy appender, Errors errors) {
 		if (appender.getBufferSize() < 1 || appender.getBufferSize() > 100000)
 			errors.rejectValue("bufferSize", Constants.MODULE_ID + ".error.bufferSize");
 	}
 	
 	/**
 	 * Validation specific to socket appenders
 	 * @param appender the appender to validate
 	 * @param errors the errors
 	 */
 	private void validateSocketAppender(AppenderProxy appender, Errors errors) {
 		if (appender.getRemoteHost().isEmpty())
 			errors.rejectValue("host", Constants.MODULE_ID + ".error.host");
 		if (appender.getPort() < 0 || appender.getPort() > 65535)
 			errors.rejectValue("port", Constants.MODULE_ID + ".error.port");
 	}
 	
 	/**
 	 * Validation specific to NT event log appenders
 	 * @param appender the appender to validate
 	 * @param errors the errors
 	 */
 	private void validateNTEventLogAppender(AppenderProxy appender, Errors errors) {
 		if (appender.getSource().isEmpty())
 			errors.rejectValue("source", Constants.MODULE_ID + ".error.source");
 	}
 	
 	/**
 	 * Checks to see if the given appender name is already being used
 	 * @param name the name to check
 	 * @return true if name is in use
 	 */
 	private boolean isAppenderNameInUse(String name) {
 		LogManagerService svc = Context.getService(LogManagerService.class);
 		Collection<AppenderProxy> appenders = svc.getAppenders(false);
 		for (AppenderProxy appender : appenders)
 			if (name.equals(appender.getName()))
 				return true;
 		return false;
 	}
 }
