 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations (FAO).
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice,this list
  *       of conditions and the following disclaimer.
  *    2. Redistributions in binary form must reproduce the above copyright notice,this list
  *       of conditions and the following disclaimer in the documentation and/or other
  *       materials provided with the distribution.
  *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
  *       promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
  * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.common.messaging;
 
 /**
  * Contains message codes for messages raised from the services.
  * @author soladev
  */
 public class ServiceMessage {
 
     // Service message prefix
     public static final String MSG_PREFIX = "ser";
     
     // Message groups
     private static final String TEST = MSG_PREFIX + "test";
     private static final String GENERAL = MSG_PREFIX + "gnrl";
     private static final String EXCEPTION = MSG_PREFIX + "excp";
     private static final String RULE = MSG_PREFIX + "rule";
     private static final String EJB_APPLICATION = MSG_PREFIX + "ejbapplication";
     private static final String EJB_SOURCE = MSG_PREFIX + "ejbsource";
     private static final String EJB_TRANSACTION = MSG_PREFIX + "ejbtransaction";
     private static final String ADMIN_WS = MSG_PREFIX + "adminservice";
     
     // <editor-fold defaultstate="collapsed" desc="Test Messages">  
     /** sertest001 - Unit Test Message */
     public static final String TEST001 = TEST + "001";
     /** sertest002 - Unit Test Message */
     public static final String TEST002 = TEST + "002";
     /** sertest003 - Unit Test Message */
     public static final String TEST003 = TEST + "003";
     /** sertest004 - Unit Test Message */
     public static final String TEST004 = TEST + "004";
     /** sertest005 - Unit Test Message */
     public static final String TEST005 = TEST + "005";
     /** sertest006 - Unit Test Message */
     public static final String TEST006 = TEST + "006";
     /** sertest007 - Unit Test Message */
     public static final String TEST007 = TEST + "007";
     // </editor-fold>
     
     // General Messages
     /** sergnrl001 - An unexpected error has occurred. */
     public static final String GENERAL_UNEXPECTED = GENERAL + "001";
     /** sergnrl002 - Your changes cannot be saved as the record you are editing has been changed 
                      by someone else. */
     public static final String GENERAL_OPTIMISTIC_LOCK = GENERAL + "002";
     /** sergnrl003 - An unexpected error has occurred while performing {0}. Error details: {1} */
     public static final String GENERAL_UNEXPECTED_ERROR_DETAILS = GENERAL + "003";
     /** sergnrl004 - An unexpected error has occurred while performing {0}. Error details: {1} */
     public static final String GENERAL_UNEXPECTED_ERROR_DETAILS_ERR_NUM = GENERAL + "004";
 
     
     
     // Exception Messages
     /** serexcp001 - An error occurred while logging an exception. Error details: {0} */
     public static final String EXCEPTION_FAILED_LOGGING = EXCEPTION + "001";
     /** serexcp002 - An error occurred while formatting an exception. Error details: {0}. */
     public static final String EXCEPTION_FAILED_FORMATTING = EXCEPTION + "002";
     /** serexcp003 - The service url is malformed. Error details: {1} */
     public static final String EXCEPTION_MALFORMED_URL = EXCEPTION + "003";
     /** serexcp004 - The username and password are incorrect. */
     public static final String EXCEPTION_AUTHENTICATION_FAILED = EXCEPTION + "004";
     /** serexcp005 - Unable to connect to the service at {0}. Error details: {1} */
     public static final String EXCEPTION_SERVICE_CONNECTION = EXCEPTION + "005";
     /** serexcp006 - You don't have enough rights to access this function. */
     public static final String EXCEPTION_INSUFFICIENT_RIGHTS = EXCEPTION + "006";
     /** serexcp007 - Entity doesn't belong to the called EJB. */
     public static final String EXCEPTION_ENTITY_PACKAGE_VIOLATION = EXCEPTION + "007";
     /** serexcp008 - Unable to initialize web service client for service {0}. Error details: {1} */
     public static final String EXCEPTION_INITALIZE_WSCLIENT = EXCEPTION + "008";
     /* serexcp009 - The username and password are incorrect. */
     public static final String EXCEPTION_INVALID_SECURITY_HEADER = EXCEPTION + "009";
    /* serexcp010 - The file cannot be attached due to its size ({0}MB) */
    public static final String EXCEPTION_FILE_TOO_BIG = EXCEPTION + "010";
 
     
     // Business Rule Messages
     /** serrule001 - Business rule {0} does not exist.*/
     public static final String RULE_NOT_FOUND = RULE + "001";
     /** serrule002 - Business rule {0} did not execute successfully.*/
     public static final String RULE_FAILED_EXECUTION = RULE + "002";
     /** serrule003 - Validation failed.*/
     public static final String RULE_VALIDATION_FAILED = RULE + "003";
 
     // ApplicationEJB Messages
     /** serejbapplication001 - Service does not exist.*/
     public static final String EJB_APPLICATION_SERVICE_NOT_FOUND = EJB_APPLICATION + "001";
     /** serejbapplication002 - Application is not found.*/
     public static final String EJB_APPLICATION_APPLICATION_NOT_FOUND = EJB_APPLICATION + "002";
     /** serejbapplication003 - Only information service requests can be handled.*/
     public static final String EJB_APPLICATION_SERVICE_REQUEST_TYPE_INFORMATION_REQUIRED = EJB_APPLICATION + "003";
 
     //SourceEJB Messages
     /** serejbsource001 - Source does not exist. */
     public static final String EJB_SOURCE_SOURCE_NOT_FOUND = EJB_SOURCE + "001";
     /** serejbsource002 - Source is not in pending status. */
     public static final String EJB_SOURCE_SOURCE_NOT_PENDING = EJB_SOURCE + "002";
 
     //TransactionEJB Messages
     /** serejbtransaction001 - Source does not exist. */
     public static final String EJB_TRANSACTION_TRANSACTION_NOT_FOUND = EJB_TRANSACTION + "001";
     
     // Admin Web-service
     /** seradminservice001 - Username "{0}" already exists. */
     public static final String ADMIN_WS_USER_EXISTS = ADMIN_WS + "001";
 }
