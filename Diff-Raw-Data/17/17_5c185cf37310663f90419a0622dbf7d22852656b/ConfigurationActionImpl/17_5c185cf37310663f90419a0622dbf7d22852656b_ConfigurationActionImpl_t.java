 /**
  * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  *
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  */
 
 package com.liferay.ci.jenkins.action;
 
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.PortletConfig;
 import javax.portlet.PortletPreferences;
 
 import com.liferay.ci.http.JenkinsConnectUtil;
 import com.liferay.portal.kernel.portlet.DefaultConfigurationAction;
 import com.liferay.portal.kernel.servlet.SessionErrors;
 import com.liferay.portal.kernel.util.Validator;
 
 /**
  * @author Manuel de la Peña
  */
 public class ConfigurationActionImpl extends DefaultConfigurationAction {
 
 	@Override
 	public void processAction(
 			PortletConfig portletConfig, ActionRequest actionRequest,
 			ActionResponse actionResponse)
 		throws Exception {
 
 		String baseAPIURL = getParameter(actionRequest, "baseapiurl");
 
 		if (Validator.isNull(baseAPIURL)) {
 			SessionErrors.add(actionRequest, "baseApiURLError");
 		}
 		else {
 			// change base API URL
 
 			JenkinsConnectUtil.setJenkinsBaseApiURL(baseAPIURL);
 		}
 
 		String user = getParameter(actionRequest, "username");
 		String password = getParameter(actionRequest, "password");
 
 		super.processAction(portletConfig, actionRequest, actionResponse);
 
		PortletPreferences preferences = actionRequest.getPreferences();

 		if (Validator.isNotNull(user) && Validator.isNotNull(password)) {
 			// reset http connect configuration
 
 			JenkinsConnectUtil.setAuthConfiguration(user, password);
 		}
 		else {
			preferences.reset("username");
		}
 
		// this is needed to reset unused preferences
 
		preferences.reset("password");
 
		preferences.store();
 	}
 
 }
