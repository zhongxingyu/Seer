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
 
 public class ClientMessage {
 
     public static final String MSG_PREFIX = "cli";
     
     // Message groups
     private static final String TEST = MSG_PREFIX + "test";
     private static final String GENERAL = MSG_PREFIX + "gnrl";
     private static final String SEARCH = MSG_PREFIX + "srch";
     private static final String SERVICE = MSG_PREFIX + "serv";
     private static final String CHECK = MSG_PREFIX + "chck";
     private static final String ARCHIVE = MSG_PREFIX + "arch";
     private static final String GENERAL_ERRORS = MSG_PREFIX + "errs";
     private static final String APPLICATION = MSG_PREFIX + "app";
     private static final String REPORT = MSG_PREFIX + "rpt";
     private static final String GENERAL_CONFIRM = MSG_PREFIX + "confirm";
     private static final String HELP = MSG_PREFIX + "help";
     private static final String BAUNIT = MSG_PREFIX + "baunit";
     private static final String PARTY = MSG_PREFIX + "party";
     private static final String VALIDATION = MSG_PREFIX + "vldtn";
     private static final String SOURCE = MSG_PREFIX + "source";
     private static final String ADMIN = MSG_PREFIX + "admin";
     private static final String BR = MSG_PREFIX + "br";
     
     // <editor-fold defaultstate="collapsed" desc="Test Messages">  
     /** clitest001 - Unit Test Message */
     public static final String TEST001 = TEST + "001";
     // </editor-fold>
     
     // General Messages
     /** clignrl001 - An unexpected error has occurred. Error details: {0} */
     public static final String GENERAL_UNEXPECTED = GENERAL + "001";
     /** clignrl002 - Language update will take effect on restart */
     public static final String GENERAL_UPDATE_LANG = GENERAL + "002";
     /** clignrl003 - There are {0} running tasks currently. Please wait until finishing. */
     public static final String GENERAL_ACTIVE_TASKS_EXIST = GENERAL + "003";
     /** clignrl004 - Select document. */
     public static final String GENERAL_SELECT_DOCUMENT = GENERAL + "004";
     /** clignrl005 - Some fields are not filled or have incorrect values:\n\n{0}"  */
     public static final String GENERAL_BEAN_VALIDATION = GENERAL + "005";
     /** clignrl006 - Loading application, please wait..."  */
     public static final String GENERAL_LOADING_APPLICATION = GENERAL + "006";
     /** clignrl007 - Save  */
     public static final String GENERAL_LABELS_SAVE = GENERAL + "007";
     /** clignrl008 - Create  */
     public static final String GENERAL_LABELS_CREATE = GENERAL + "008";
     /** clignrl009 - Create and Close  */
     public static final String GENERAL_LABELS_CREATE_AND_CLOSE = GENERAL + "009";
     /** clignrl010 - Save and Close  */
     public static final String GENERAL_LABELS_SAVE_AND_CLOSE = GENERAL + "010";
     /** clignrl011 - Close  */
     public static final String GENERAL_LABELS_CLOSE = GENERAL + "011";
     /** clignrl012 - Cancel  */
     public static final String GENERAL_LABELS_CANCEL = GENERAL + "012";
     /** clignrl013 - Record has been updated successfully. */
     public static final String GENERAL_RECORD_SAVED = GENERAL + "013";
     
     public static final String GENERAL_LABELS_INDIVIDUAL = GENERAL + "014";
     
     public static final String GENERAL_LABELS_ENTITY = GENERAL + "015";
         
     // Service Messages
     /** cliserv001 - Unable to initialize the {0} service. The reason is: {1} */
     public static final String SERVICE_INITIALIZE = SERVICE + "001";
      
     // Search Messages
     /** clisrch001 - There were no records matching your search criteria. */
     public static final String SEARCH_NO_RESULTS = SEARCH + "001";
     /** clisrch002 - Your search matched more than {0} records */
     public static final String SEARCH_TOO_MANY_RESULTS = SEARCH + "002";
     /** clisrch003 - Select application */
     public static final String SEARCH_SELECT_APPLICATION = SEARCH + "003";
     
     // Check / Validation Messages
     /** clichck001 - The selected application cannot be assigned or unassigned until
                      its fees are paid. */
     public static final String CHECK_FEES_NOT_PAID = CHECK + "001";
     /** clichck002 - The selected application cannot have a receipt printed
                      until its fees are paid. */
     public static final String CHECK_FEES_NOT_PAID_RECEIPT = CHECK + "002";
     /** clichck003 - The selected application cannot have a receipt printed
                      until it is lodged and its fees are paid. */
     public static final String CHECK_NOT_LODGED_RECEIPT = CHECK + "003";
     /** clichck004 - A user name and password must be provided. */
     public static final String CHECK_INVALID_USERNAME_PASSWORD = CHECK + "004";
      /** clichck005 - Select document type"*/
     public static final String CHECK_SELECT_DOCTYPE = CHECK + "005";
     /** clichck006 - Enter first and last part of identifier */
     public static final String CHECK_FIRST_LAST_PROPERTY = CHECK + "006";
     /** clichck007 - cannot be null */
     public static final String CHECK_NOTNULL_FIELDS = CHECK + "007";
    
     // validation hibernate messages 
     /** clichck008 - name cannot be null */
     public static final String CHECK_NOTNULL_NAME = CHECK + "008";
     public static final String CHECK_NOTNULL_CODE = CHECK + "009";
     public static final String CHECK_NOTNULL_STATUS = CHECK + "010";
     public static final String CHECK_SIZE_STATUS = CHECK + "011";
     public static final String CHECK_NOTNULL_DISPLAYVALUE = CHECK + "012";
     public static final String CHECK_NOTNULL_ADDRESS = CHECK + "013";
     public static final String CHECK_NOTNULL_FIRSTPART = CHECK + "014";
     public static final String CHECK_NOTNULL_LASTPART = CHECK + "015";
     public static final String CHECK_NOTNULL_NOTATION = CHECK + "016";
     public static final String CHECK_NOTNULL_EXPIRATION = CHECK + "017";
     public static final String CHECK_FUTURE_EXPIRATION = CHECK + "018";
     public static final String CHECK_NOTNULL_MORTGAGEAMOUNT = CHECK + "019";
     public static final String CHECK_NOTNULL_MORTAGAETYPE = CHECK + "020";
     public static final String CHECK_SIZE_SOURCELIST = CHECK + "021";
     public static final String CHECK_NODUPLICATED_SOURCELIST = CHECK + "022";
     public static final String CHECK_SIZE_RRRSHARELIST = CHECK + "023";
     public static final String CHECK_TOTALSHARE_RRRSHARELIST = CHECK + "024";
     public static final String CHECK_SIZE_RIGHTHOLDERLIST = CHECK + "025";
     public static final String CHECK_NOTNULL_NOMINATOR = CHECK + "026";
     public static final String CHECK_MIN_NOMINATOR = CHECK + "027";
     public static final String CHECK_NOTNULL_DENOMINATOR = CHECK + "028";
     public static final String CHECK_MIN_DENOMINATOR = CHECK + "029";
     public static final String CHECK_NODUPLI_RIGHTHOLDERLIST = CHECK + "030";
     public static final String CHECK_SIZE_FILTERRIGHTHOLDER = CHECK + "031";
     public static final String CHECK_NOTNULL_CADFIRSTPART = CHECK + "032";
     public static final String CHECK_NOTNULL_CADLASTPART = CHECK + "033";
     public static final String CHECK_NOTNULL_CADOBJTYPE = CHECK + "034";
     public static final String CHECK_INVALID_EMAIL = CHECK + "035";
     public static final String CHECK_NOTNULL_LASTNAME = CHECK + "036";
     public static final String CHECK_MIN_GROUPROLES = CHECK + "037";
     public static final String CHECK_NOTNULL_GROUPNAME = CHECK + "038";
     public static final String CHECK_NOTNULL_PASSWORD = CHECK + "039";
     public static final String CHECK_MIN_PASSWORD = CHECK + "040";
     public static final String CHECK_NOTNULL_CONFPASSWORD = CHECK + "041";
     public static final String CHECK_NOTNULL_USERNAME = CHECK + "042";
     public static final String CHECK_MIN_USERGROUP = CHECK + "043";
     public static final String CHECK_NOTNULL_FIRSTNAME = CHECK + "044";
     public static final String CHECK_NOTEQUAL_PASSWORD = CHECK + "045";
     public static final String CHECK_NOTNULL_RECORDATION = CHECK + "046";
     public static final String CHECK_NOTNULL_REFERENCENR = CHECK + "047";
     public static final String CHECK_NOTNULL_SOURCETYPE = CHECK + "048";
     public static final String CHECK_NOTNULL_DISPLAYNAME = CHECK + "049";
     public static final String CHECK_BEANNOTEMPTY_BRTECHTYPE = CHECK + "050";
     public static final String CHECK_SIZE_BRDEFLIST = CHECK + "051";
     public static final String CHECK_SIZE_BRVALLIST = CHECK + "052";
     public static final String CHECK_NOTNULL_BRID = CHECK + "053";
     public static final String CHECK_NOTNULL_ACTIVEFROM = CHECK + "054";
     public static final String CHECK_NOTNULL_ACTIVEUNTIL = CHECK + "055";
     public static final String CHECK_NOTNULL_BODY = CHECK + "056";
     public static final String CHECK_NOTNULL_BRVALID = CHECK + "057";
     public static final String CHECK_BEANNOTNULL_BRSEVTYPE = CHECK + "058";
     public static final String CHECK_BEANNOTNULL_BRVALTARGETTYPE = CHECK + "059";
     public static final String CHECK_GENERICNULL_OBJECT = CHECK + "060";
     public static final String CHECK_GENERICDUPL_OBJECT = CHECK + "061";
     
     // Application messages
     /** cliapp001 - Select document to attach digital copy. */
     public static final String APPLICATION_SELECT_DOCUMENT_TO_ATTACH_TO = APPLICATION + "001";
     /** cliapp002 - Are you sure you want to remove attachment? */
     public static final String APPLICATION_CONFIRM_DOCUMENT_ATTACHMENT_REMOVAL = APPLICATION + "002";
     /** cliapp003 - Property with ID = {0} has the following incomplete applications:
                     {1} */
     public static final String APPLICATION_PROPERTY_HAS_INCOMPLETE_APPLICATIONS = APPLICATION + "003";
    
     /** cliapp004 - You are about to save application.
                      Do you want to proceed? */
     public static final String APPLICATION_SAVE_CONFIRM = APPLICATION + "004";
     
      /** cliapp005 - You haven't supplied all required documents. Do you want to proceed anyway?*/
     public static final String APPLICATION_NOTALL_DOCUMENT_REQUIRED = APPLICATION + "005";
     
      /** cliapp006 - You should supply at least {0} property object(s).  
      * Do you want to proceed anyway?*/
      public static final String APPLICATION_ATLEAST_PROPERTY_REQUIRED = APPLICATION + "006";
     
     /** cliapp007 - Application saved successfully */
      public static final String APPLICATION_SUCCESSFULLY_SAVED = APPLICATION + "007";
      
     /** cliapp008 - Select property to verify */
     public static final String APPLICATION_SELECT_PROPERTY_TOVERIFY = APPLICATION + "008";
     
     /** cliapp009 - Property has been verified successfully  */
     public static final String APPLICATION_PROPERTY_VERIFIED = APPLICATION + "009";
     
     /** cliapp010 - There is no selected user  */
     public static final String APPLICATION_NOSEL_USER = APPLICATION + "010";
     /** cliapp011 - Application will be assigned to user "{0}"  */
     public static final String APPLICATION_ASSIGN = APPLICATION + "011";
     /** cliapp012 - Application has been assigned  */
     public static final String APPLICATION_ASSIGNED = APPLICATION + "012";
     /** cliapp013 - Application will be unassigned from user "{0}"  */
     public static final String APPLICATION_UNASSIGN = APPLICATION + "013";
     /** cliapp014 - Application has been unassigned  */
     public static final String APPLICATION_UNASSIGNED = APPLICATION + "014";
     /** cliapp015 - Loading unassigned applications... */
     public static final String APPLICATION_LOADING_UNASSIGNED = APPLICATION + "015";
     /** cliapp016 - Loading assigned applications... */
     public static final String APPLICATION_LOADING_ASSIGNED = APPLICATION + "016";
     /** cliapp017 - Select a Service */
     public static final String APPLICATION_SELECT_SERVICE = APPLICATION + "017";
     /** cliapp018 - There are no property objects on the list */
     public static final String APPLICATION_PROPERTY_LIST_EMPTY = APPLICATION + "018";
     
     /** cliapp019 - You are about to take action \"{0}\" against application, no further changes will be possible. Are you sure? */
     public static final String APPLICATION_ACTION_WARNING_STRONG = APPLICATION + "019";
 
     /** cliapp020 - Application #{0}, was failed to approve. */
     public static final String APPLICATION_APPROVE_FAILED = APPLICATION + "020";
     /** cliapp021 - Application #{0}, has been successfully approved. */
     public static final String APPLICATION_APPROVE_SUCCESS = APPLICATION + "021";
     /** cliapp022 - You are about to reject application, no further changes will be possible. Are you sure? */
     public static final String APPLICATION_REJECT_WARNING = APPLICATION + "022";
     /** cliapp023 - Application #{0}, was failed to reject. */
     public static final String APPLICATION_REJECT_FAILED = APPLICATION + "023";
     /** cliapp024 - Application #{0}, has been successfully rejected. */
     public static final String APPLICATION_REJECT_SUCCESS = APPLICATION + "024";
     
     /** cliapp025 - You are about to complete \"{0}\" service. Are you sure? */
     public static final String APPLICATION_SERVICE_COMPLETE_WARNING = APPLICATION + "025";
     /** cliapp026 - Application service \"{0}\", was failed to complete. */
     public static final String APPLICATION_SERVICE_COMPLETE_FAILED = APPLICATION + "026";
     /** cliapp027 - Application service \"{0}\", has been successfully completed. */
     public static final String APPLICATION_SERVICE_COMPLETE_SUCCESS = APPLICATION + "027";
     /** cliapp028 - You are about to cancel application service \"{0}\", no further changes will be possible. Are you sure? */
     public static final String APPLICATION_SERVICE_CANCEL_WARNING = APPLICATION + "028";
     /** cliapp029 - Application service \"{0}\", was failed to cancel. */
     public static final String APPLICATION_SERVICE_CANCEL_FAILED = APPLICATION + "029";
     /** cliapp030 - Application service \"{0}\", has been successfully canceled. */
     public static final String APPLICATION_SERVICE_CANCEL_SUCCESS = APPLICATION + "030";
     /** cliapp031 - You are about to revert \"{0}\" service to the pending state. Are you sure? */
     public static final String APPLICATION_SERVICE_REVERT_WARNING = APPLICATION + "031";
     /** cliapp032 - Application service \"{0}\", was failed to revert. */
     public static final String APPLICATION_SERVICE_REVERT_FAILED = APPLICATION + "032";
     /** cliapp033 - Application service \"{0}\", has been successfully reverted. */
     public static final String APPLICATION_SERVICE_REVERT_SUCCESS = APPLICATION + "033";
     /** cliapp034 - Application has critical violations. Fix them first, to continue. */
     public static final String APPLICATION_HAS_CRITICAL_VIOLATIONS = APPLICATION + "034";
      /** cliapp035 - Service already added. */
     public static final String  APPLICATION_ALREADYSELECTED_SERVICE  = APPLICATION + "035";
     /** cliapp036 - You are about to take action \"{0}\" against application. Are you sure? */
     public static final String APPLICATION_ACTION_WARNING_SOFT = APPLICATION + "036";
     /** cliapp037 - The action against the application #{0}, has been successful. */
     public static final String APPLICATION_ACTION_SUCCESS = APPLICATION + "037";
      /** cliapp038 - Opening application form... */
     public static final String APPLICATION_OPENING_FORM = APPLICATION + "038";
    /** cliapp039 - Do you want to pick up rights and parcels from existing Title? */
    public static final String APPLICATION_SELECT_EXISTING_TITLE = APPLICATION + "039";
    
    
     // Reports messages.
     /** clirpt001 - Report generation failed. Error details: {0} */
     public static final String REPORT_GENERATION_FAILED = REPORT + "001";
             
     // Digital archive messages
     /** cliarch001 - Select file to open. */
     public static final String ARCHIVE_SELECT_FILE_TO_OPEN = ARCHIVE + "001";
     
     /** cliarch002 - Select file to attach. */
     public static final String ARCHIVE_SELECT_FILE_TO_ATTACH = ARCHIVE + "002";
     
     /**cliarch003 - Failed to attach file "{0}". */
     public static final String ARCHIVE_FAILED_TO_ATTACH_FILE = ARCHIVE + "003";
     
     /** cliarch004 - File has been deleted successfully. */
     public static final String ARCHIVE_FILE_DELETED = ARCHIVE + "004";
     
     /** cliarch005 - You are going to delete file from the shared server folder. 
                       Do you want to proceed? */
     public static final String ARCHIVE_CONFIRM_FILE_DELETION = ARCHIVE + "005";
     
     /** cliarch006 - File has been added into digital archive. */
     public static final String ARCHIVE_FILE_ADDED = ARCHIVE + "006";
     
     /** cliarch007 - Failed to delete file {0}. */
     public static final String ARCHIVE_FAILED_DELETE_FILE = ARCHIVE + "007";
     
     // BA unit messages
     /** clibaunit001 - Select parcel  */
     public static final String BAUNIT_SELECT_PARCEL = BAUNIT + "001";
     /** clibaunit002 - Selected right doesn't have pending changes and can't be edited.  */
     public static final String BAUNIT_RRR_NO_PEDING_CHANGES = BAUNIT + "002";
     /** clibaunit003 - Property not found.  */
     public static final String BAUNIT_NOT_FOUND = BAUNIT + "003";
    /** clibaunit004 - Nothing is selected. Do you want to continue?  */
     public static final String BAUNIT_NOTHING_SELECTED = BAUNIT + "004";
     /** clibaunit005 - You have selected parcels, related to existing property. Make sure to make changes in the current property object.  */
     public static final String BAUNIT_EXISTING_PARCELS_SELECTED = BAUNIT + "005";
     
     // General error messages
     /** clierrs001 - Can't cerate a new file. {0} */
     public static final String ERR_FAILED_CREATE_NEW_FILE = GENERAL_ERRORS + "001";
     
     /** clierrs002 - Can't open the file. {0} */
     public static final String ERR_FAILED_OPEN_FILE = GENERAL_ERRORS + "002";
     
     /** clierrs003 - Method not found - {0}.  Error details: {1} */
     public static final String ERR_NO_SUCH_METHOD = GENERAL_ERRORS + "003";
     
     /** clierrs004 - Violation of business rules occurred. */
     public static final String ERR_BR_VIOLATION = GENERAL_ERRORS + "004";
     
     // General confirms
     /** cliconfirm001 - Are you sure you want to delete record? All data will be lost */
     public static final String CONFIRM_DELETE_RECORD = GENERAL_CONFIRM + "001";
     
      // Help messages
     /** clihelp001 - HelpSet - Exception:{0}  Path: {1} not found */
     public static final String EXCEPTION_HELPSET = HELP + "001";
     
     // Party messages
     /** cliparty001 - Select party  */
     public static final String PARTY_SELECT_PARTY = PARTY + "001";
     
     /** cliparty002 - You are about to save party.\nDo you want to proceed?  */
     public static final String PARTY_SAVE_CONFIRM = PARTY + "002";
     
     /** cliparty003 - Person has been saved. */
     public static final String PARTY_SAVED = PARTY + "003";
     
     /** cliparty004 - Fill party fields */
     public static final String PARTY_FILL_PARTY = PARTY + "004";
     
     /** cliparty005 - party prepared successfully */
     public static final String PARTY_PREPARED_PARTY = PARTY + "005";
     
     /** cliparty006 - Select role  */
     public static final String PARTY_SELECT_ROLE = PARTY + "006";
     
    /** cliparty007 - Role already added  */
      public static final String PARTY_ALREADYSELECTED_ROLE = PARTY + "007";
      
      /** cliparty008 - Person has been created. */
     public static final String PARTY_CREATED = PARTY + "003";
     
    // Source messages
      
      /** clisource001 -  This action will detach document from the transaction. Are you sure?  */
      public static final String  SOURCE_DETACH_TRANSACTION_WARNING = SOURCE +  "001";
      
      /** clisource002 -  This action will attach document to the transaction. Are you sure?  */
      public static final String  SOURCE_ATTACH_TRANSACTION_WARNING = SOURCE +  "002";
      
     
    // Validation messages 
     
      /** clivldtn001 -  You have duplicated objects in the list */
      public static final String  VALIDATION_NODUPLICATES = VALIDATION +  "001";
     
       /** clivldtn002 -  You have duplicated parties in one share. */
      public static final String  VALIDATION_SHARE_NODUPLICATES = VALIDATION +  "002";
      
     /** clivldtn003 -  Fill in at least one owner. */
      public static final String  VALIDATION_OWNER_FILL = VALIDATION +  "003";
      
      /** clivldtn004 -  Fill in numerator. */
      public static final String  VALIDATION_NUM_FILL = VALIDATION +  "004";
      
      /** clivldtn005 -  Minimum value for numerator is 1. */
      public static final String  VALIDATION_MIN_NUM = VALIDATION +  "005";
      
      /** clivldtn006 -  Fill in denominator. */
      public static final String  VALIDATION_DEN_FILL = VALIDATION +  "006";
      
      /** clivldtn007 -  Minimum value for denominator is 1. */
      public static final String  VALIDATION_MIN_DEN = VALIDATION +  "007";
 
      // BR messages
      
      /** clibr001 -  For selected target type \"Service moment\" and \"Application moment\" should be empty. */
      public static final String  BR_REGISTRATION_MOMENT_VALIDATION = BR +  "001";
      /** clibr002 -  For selected target type \"Service moment\" and \"Registration moment\" should be empty. */
      public static final String  BR_APPLICATION_MOMENT_VALIDATION = BR +  "002";
      /** clibr003 -  Select Right and Restriction target type. */
      public static final String  BR_RRR_TYPE_VALIDATION = BR +  "003";
     /** clibr004 -  For selected target type \"Application moment\" and \"Registration moment\" should be empty. */
      public static final String  BR_SERVICE_MOMENT_VALIDATION = BR +  "004";
      /** clibr005 -  Select target type, different from application. */
      public static final String  BR_REQUEST_TYPE_VALIDATION = BR +  "005";
      
      // Admin messages
     
      /** cliadmin001 - Are you sure you want to delete group? All users included in the group will lose access rights, assigned to this group. */
     public static final String ADMIN_CONFIRM_DELETE_GROUP = ADMIN + "001";
     /** cliadmin002 - Group has been saved successfully. */
     public static final String ADMIN_GROUP_SAVED = ADMIN + "002";
     /** cliadmin003 - Role has been saved successfully. */
     public static final String ADMIN_ROLE_SAVED = ADMIN + "003";
      /** cliadmin004 - Are you sure you want to delete role? All groups having this role will lose related access rights. */
     public static final String ADMIN_CONFIRM_DELETE_ROLE = ADMIN + "004";
     /** cliadmin005 - Group has been created successfully. */
     public static final String ADMIN_GROUP_CREATED = ADMIN + "005";
     /** cliadmin006 - Role has been created successfully. */
     public static final String ADMIN_ROLE_CREATED = ADMIN + "006";
     /** cliadmin007 - Are you sure you want to delete user? */
     public static final String ADMIN_CONFIRM_DELETE_USER = ADMIN + "007";
     /** cliadmin008 - User has been created successfully. */
     public static final String ADMIN_USER_CREATED = ADMIN + "008";
     /** cliadmin009 - User has been saved successfully. */
     public static final String ADMIN_USER_SAVED = ADMIN + "009";
     /** cliadmin010 - No users were found. */
     public static final String ADMIN_USERS_NO_FOUND = ADMIN + "010";
     /** cliadmin011 - Password has been changed successfully. */
     public static final String ADMIN_PASSWORD_CHANGED = ADMIN + "011";
     /** cliadmin012 - Current user can't be deleted. */
     public static final String ADMIN_CURRENT_USER_DELETE_ERROR = ADMIN + "011";
     /** cliadmin013 - You don't have administrator's rights. */
     public static final String ADMIN_NO_ADMIN_RIGHTS = ADMIN + "013";
     /** cliadmin014 - Current user can't be disabled. */
     public static final String ADMIN_CURRENT_USER_DISABLE_ERROR = ADMIN + "014";
     /** cliadmin015 - New \"{0}\" object has been created. */
     public static final String ADMIN_REFDATA_CREATED = ADMIN + "015";
     /** cliadmin016 - Are you sure you want to delete \"{0}\"?. It might affect data in other tables.*/
     public static final String ADMIN_CONFIRM_DELETE_REFDATA = ADMIN + "016";
     /** cliadmin017 - Object has been saved successfully.*/
     public static final String ADMIN_OBJECT_SAVED = ADMIN + "017";
     /** cliadmin018 - No business rules where found.*/
     public static final String ADMIN_BR_NO_FOUND = ADMIN + "018";
     /** cliadmin019 - New business rule has been created. */
     public static final String ADMIN_BR_CREATED = ADMIN + "019";
     /** cliadmin020 - Business rule has been saved successfully.*/
     public static final String ADMIN_BR_SAVED = ADMIN + "020";
     /** cliadmin021 - Are you sure you want to delete \"{0}\"?.*/
     public static final String ADMIN_CONFIRM_DELETE_BR = ADMIN + "021";
     /** cliadmin022 - There are business rule validations dependent on the current object. Remove them first before deleting object. */
     public static final String ADMIN_BR_REMOVE_VALIDATIONS = ADMIN + "022";
 }
