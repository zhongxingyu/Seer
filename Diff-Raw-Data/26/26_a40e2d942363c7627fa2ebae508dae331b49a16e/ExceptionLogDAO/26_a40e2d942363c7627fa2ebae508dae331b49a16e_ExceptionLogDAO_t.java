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
 package org.openmrs.module.errorlogging.api.db;
 
 import java.util.Date;
 import java.util.List;
 import org.openmrs.module.errorlogging.ExceptionLog;
 
 /**
  * Database methods for {@link ExceptionLogService}.
  */
 public interface ExceptionLogDAO {
 	
 	/**
 	 * @see {@link ExceptionLogService#saveExceptionLog(ExceptionLog)}
 	 */
 	public ExceptionLog saveExceptionLog(ExceptionLog exceptionLog);
 	
 	/**
 	 * @see {@link ExceptionLogService#purgeExceptionLog(ExceptionLog)}
 	 */
 	public void deleteExceptionLog(ExceptionLog exceptionLog);
 	
 	/**
 	 * @see {@link ExceptionLogService#getExceptionLog(Integer)}
 	 */
 	public ExceptionLog getExceptionLog(Integer exceptionLogId);
 	
 	/**
 	 * @see {@link ExceptionLogService#getExceptionLogs(String, Date, Integer, Integer)}
 	 */
 	public List<ExceptionLog> getExceptionLogs(String exceptionClass, Date minExceptionDateTime, Integer start,
 	                                           Integer length);
 	
 	/**
 	 * @see {@link ExceptionLogService#getCountOfExceptionLogs(String, Date)}
 	 */
	public Integer getCountOfExceptionLogs(String exceptionClass, Date minExceptionDateTime);
 }
