 /*******************************************************************************
  * Copyright (c) Feb 20, 2012 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdkcli.internal.commands;
 
 import java.util.List;
 
 import org.zend.sdkcli.internal.options.Option;
 import org.zend.sdklib.monitor.IZendIssue;
 import org.zend.webapi.core.WebApiException;
 import org.zend.webapi.core.connection.data.Event;
 import org.zend.webapi.core.connection.data.EventsGroupDetails;
 import org.zend.webapi.core.connection.data.GeneralDetails;
 import org.zend.webapi.core.connection.data.Issue;
 import org.zend.webapi.core.connection.data.Parameter;
 import org.zend.webapi.core.connection.data.ParameterList;
 import org.zend.webapi.core.connection.data.RouteDetail;
 import org.zend.webapi.core.connection.data.RouteDetails;
 import org.zend.webapi.core.connection.data.SuperGlobals;
 
 /**
  * Command to list issues on specified target.
  * 
  * @author Wojciech Galanciak, 2012
  * 
  */
 public class GetIssueCommand extends AbstractMonitorCommand {
 
 	private static final String ID = "i";
 
 	@Option(opt = ID, required = true, description = "Issue id", argName = "Issue id")
 	public String getId() {
 		return getValue(ID);
 	}
 
 	@Override
 	public boolean doExecute() {
 		IZendIssue zendIssue = getMonitor().get(Integer.valueOf(getId()));
 		List<EventsGroupDetails> eventsGroupsDetails = null;
		try {
			eventsGroupsDetails = zendIssue.getGroupDetails();
		} catch (WebApiException e) {
			// ignore and just do not display group details
		}
 		if (zendIssue != null) {
 			Issue issue = zendIssue.getIssue();
 			getLogger().info("last occurance: " + issue.getLastOccurance());
 			getLogger().info("rule:           " + issue.getRule());
 			getLogger().info("severity:       " + issue.getSeverity());
 			getLogger().info("status:         " + issue.getStatus().getName());
 			GeneralDetails generalDetails = issue.getGeneralDetails();
 			if (generalDetails != null) {
 				getLogger().info("general details:");
 				getLogger().info("\turl:        " + generalDetails.getUrl());
 				String errorType = generalDetails.getErrorType();
 				if (errorType != null && errorType.length() > 0) {
 					getLogger().info(
 							"\terror:      " + generalDetails.getErrorType()
 									+ ": " + generalDetails.getErrorString());
 				}
 				String function = generalDetails.getFunction();
 				if (function != null && function.length() > 0) {
 					getLogger().info(
 							"\tfunction:   " + generalDetails.getFunction()
 									+ "(" + generalDetails.getSourceFile()
 									+ ":" + generalDetails.getSourceLine()
 									+ ")");
 				}
 			}
 			RouteDetails routeDetails = issue.getRouteDetails();
 			if (routeDetails != null) {
 				List<RouteDetail> details = routeDetails.getDetails();
 				if (details != null) {
 					getLogger().info("route details:");
 					for (RouteDetail routeDetail : details) {
 						getLogger().info(
 								"\t" + routeDetail.getKey() + " = "
 										+ routeDetail.getValue());
 					}
 				}
 			}
 			if (eventsGroupsDetails != null) {
 				getLogger().info("events:");
 				for (EventsGroupDetails groupDetails : eventsGroupsDetails) {
 					Event event = groupDetails.getEvent();
 					getLogger().info("\ttype:        " + event.getEventType());
 					getLogger()
 							.info("\tdescription: " + event.getDescription());
 					getLogger().info("\tseverity:    " + event.getSeverity());
 					SuperGlobals superGlobals = event.getSuperGlobals();
 					if (superGlobals != null) {
 						getLogger().info("\tSuper Globals:");
 						getLogger().info("\t\tget:");
 						ParameterList globals = superGlobals.getGet();
 						printParameters(globals);
 						getLogger().info("\t\tpost:");
 						globals = superGlobals.getPost();
 						printParameters(globals);
 						getLogger().info("\t\tsession:");
 						globals = superGlobals.getSession();
 						printParameters(globals);
 						getLogger().info("\t\tcookie:");
 						globals = superGlobals.getCookie();
 						printParameters(globals);
 						getLogger().info("\t\tserver:");
 						globals = superGlobals.getServer();
 						printParameters(globals);
 					}
 				}
 			}
 			return true;
 		} else {
 			getLogger().info("There is no issue with id " + getId());
 			return true;
 		}
 	}
 
 	private void printParameters(ParameterList globals) {
 		if (globals != null) {
 			List<Parameter> params = globals.getParameters();
 			if (params != null) {
 				for (Parameter parameter : params) {
 					getLogger().info(
 							"\t\t\t" + parameter.getName() + " = "
 									+ parameter.getValue());
 				}
 			} else {
 				getLogger().info("\t\t\t<EMPTY>");
 			}
 		}
 	}
 
 }
