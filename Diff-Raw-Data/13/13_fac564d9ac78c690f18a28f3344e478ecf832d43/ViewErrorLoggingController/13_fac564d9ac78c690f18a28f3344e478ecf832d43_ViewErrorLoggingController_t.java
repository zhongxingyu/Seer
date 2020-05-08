 /**
  * The contents of this file are subject to the OpenMRS Public License Version
  * 1.0 (the "License"); you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing rights and limitations under the License.
  *
  * Copyright (C) OpenMRS, LLC. All Rights Reserved.
  */
 package org.openmrs.module.errorlogging.web.controller;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.errorlogging.ExceptionLog;
 import org.openmrs.module.errorlogging.api.ExceptionLogService;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  * Controller for viewing errors.
  */
 @Controller
 public class ViewErrorLoggingController {
 	
 	protected final Log log = LogFactory.getLog(getClass());
 	
 	@RequestMapping(value = "/module/errorlogging/viewErrors.form", method = RequestMethod.GET)
 	public void showForm(ModelMap model) {
 	}
 	
 	@RequestMapping(value = "/module/errorlogging/viewExceptionLogs.json", method = RequestMethod.POST)
 	public void showErrors(HttpServletRequest request, HttpServletResponse response) {
 		JSONObject json = new JSONObject();
 		JSONArray data = new JSONArray();
 		String username = processString(String.valueOf(request.getParameter("username")));
 		String excClass = processString(String.valueOf(request.getParameter("excClass")));
 		String excMessage = processString(String.valueOf(request.getParameter("excMessage")));
 		String excOpenMRSVersion = processString(String.valueOf(request.getParameter("excOpenMRSVersion")));
 		String excFileName = processString(String.valueOf(request.getParameter("excFileName")));
 		String excMethodName = processString(String.valueOf(request.getParameter("excMethodName")));
 		Integer excLineNum = processInteger(request.getParameter("excLineNum"));
 		Integer excFrequency = processInteger(request.getParameter("excFrequency"));
 		Date startDateTime = getDateTime(processString(String.valueOf(request.getParameter("startDateTimeString"))));
 		Date endDateTime = getDateTime(processString(String.valueOf(request.getParameter("endDateTimeString"))));
 		String sEcho = request.getParameter("sEcho");
 		Integer start = Integer.valueOf(request.getParameter("iDisplayStart"));
 		Integer length = Integer.valueOf(request.getParameter("iDisplayLength"));
 		
 		ExceptionLogService exceptionLogService = Context.getService(ExceptionLogService.class);
 		List<ExceptionLog> exceptionLogs = exceptionLogService.getExceptionLogs(username, excClass, excMessage,
 		    excOpenMRSVersion, excFileName, excMethodName, excLineNum, excFrequency, startDateTime, endDateTime, start,
 		    length);
 		Integer count = exceptionLogService.getCountOfExceptionLogs(username, excClass, excMessage, excOpenMRSVersion,
 		    excFileName, excMethodName, excLineNum, excFrequency, startDateTime, endDateTime);
 		response.setContentType("application/json");
 		if (excFrequency != null) {
 			for (ExceptionLog exLog : exceptionLogs) {
 				JSONArray excLog = new JSONArray();
 				List<ExceptionLog> exceptionLogsFr = exceptionLogService.getExceptionLogs(null, exLog.getExceptionClass(),
 				    exLog.getExceptionMessage(), exLog.getOpenmrsVersion(), exLog.getExceptionLogDetail().getFileName(),
 				    exLog.getExceptionLogDetail().getMethodName(), exLog.getExceptionLogDetail().getLineNumber(), null,
 				    null, null, 0, 1);
 				excLog.put(exceptionLogsFr.get(0).getExceptionLogId());
 				excLog.put(exceptionLogsFr.get(0).getExceptionClass());
				excLog.put(processNullString(exceptionLogsFr.get(0).getExceptionMessage()));
 				excLog.put(exceptionLogsFr.get(0).getOpenmrsVersion());
 				excLog.put("");
 				excLog.put("");
 				if (exceptionLogsFr.get(0).getExceptionLogDetail() != null) {
 					excLog.put("View");
 				} else {
 					excLog.put("");
 				}
 				if (exceptionLogsFr.get(0).getExceptionRootCause() != null) {
 					excLog.put("View");
 				} else {
 					excLog.put("");
 				}
 				excLog.put("Report");
 				data.put(excLog);
 			}
 		} else {
 			for (ExceptionLog exLog : exceptionLogs) {
 				JSONArray excLog = new JSONArray();
 				excLog.put(exLog.getExceptionLogId());
 				excLog.put(exLog.getExceptionClass());
				excLog.put(processNullString(exLog.getExceptionMessage()));
 				excLog.put(exLog.getOpenmrsVersion());
 				excLog.put(getFormattedExceptionDateTime(exLog.getExceptionDateTime()));
 				excLog.put(exLog.getUser().getUsername());
 				if (exLog.getExceptionLogDetail() != null) {
 					excLog.put("View");
 				} else {
 					excLog.put("");
 				}
 				if (exLog.getExceptionRootCause() != null) {
 					excLog.put("View");
 				} else {
 					excLog.put("");
 				}
 				excLog.put("Report");
 				data.put(excLog);
 			}
 		}
 		try {
 			json.put("aaData", data);
 			json.put("iTotalRecords", count);
 			json.put("iTotalDisplayRecords", count);
 			json.put("iDisplayLength", length);
 			json.put("sEcho", sEcho);
 			
 			response.getWriter().print(json);
 			
 			response.flushBuffer();
 		}
 		catch (Exception e) {
 			log.error("Error has occurred while creating json response", e);
 		}
 	}
 	
 	/**
 	 * Convert input string arguments to Date
 	 *
 	 * @param exceptionDateTime date string
 	 * @return converted date
 	 */
 	private Date getDateTime(String exceptionDateTime) {
 		Date stExceptionDateTime = null;
 		if (StringUtils.isNotBlank(exceptionDateTime)) {
 			exceptionDateTime = exceptionDateTime.trim();
 			
 			String dateTime = exceptionDateTime + ":00";
 			DateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
 			try {
 				stExceptionDateTime = (Date) format.parse(dateTime);
 			}
 			catch (ParseException ex) {
 				log.error("Cannot parse date", ex);
 			}
 		}
 		return stExceptionDateTime;
 	}
 	
 	/**
 	 * Get formatted Date/Time
 	 * 
 	 * @param exceptionDateTime the exceptionDateTime to set
 	 * @return formatted date
 	 */
 	public String getFormattedExceptionDateTime(Date exceptionDateTime) {
 		Calendar c = Calendar.getInstance();
 		c.setTime(exceptionDateTime);
 		String day = Integer.toString(c.get(Calendar.DAY_OF_MONTH));
 		day = addZero(day);
 		String month = Integer.toString(c.get(Calendar.MONTH) + 1);
 		month = addZero(month);
 		String year = Integer.toString(c.get(Calendar.YEAR));
 		year = addZero(year);
 		String hour = Integer.toString(c.get(Calendar.HOUR_OF_DAY));
 		hour = addZero(hour);
 		String minute = Integer.toString(c.get(Calendar.MINUTE));
 		minute = addZero(minute);
 		String second = Integer.toString(c.get(Calendar.SECOND));
 		second = addZero(second);
 		
 		String formattedExceptionDateTime = day + "/" + month + "/" + year + " " + hour + ":" + minute + ":" + second;
 		return formattedExceptionDateTime;
 	}
 	
 	/**
 	 * Add a zero to the beginning of the line if it contains only one digit
 	 * 
 	 * @param value
 	 * @return 
 	 */
 	private String addZero(String value) {
 		if (value.length() == 1) {
 			value = "0" + value;
 		}
 		return value;
 	}
 	
 	/**
 	 * 
 	 * @param str
 	 * @return 
 	 */
 	private String processString(String str) {
 		String resultStr = null;
 		if (StringUtils.isNotBlank(str)) {
 			resultStr = str.trim();
 		}
 		return resultStr;
 	}
 	
 	/**
 	 * 
 	 * @param strInt
 	 * @return 
 	 */
 	private Integer processInteger(String strInt) {
 		Integer resultInt = null;
 		if (StringUtils.isNotBlank(strInt)) {
 			resultInt = Integer.valueOf(strInt.trim());
 		}
 		return resultInt;
 	}
	
	private String processNullString(String str) {
		if (str == null) {
			return "";
		}
		return str;
	}
 }
