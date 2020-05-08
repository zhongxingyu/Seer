 /**
  * Copyright (c) 2009 - 2013 By: CWS, Inc.
  * 
  * All rights reserved. These materials are confidential and
  * proprietary to CaspersBox Web Services N.A and no part of
  * these materials should be reproduced, published in any form
  * by any means, electronic or mechanical, including photocopy
  * or any information storage or retrieval system not should
  * the materials be disclosed to third parties without the
  * express written authorization of CaspersBox Web Services, N.A.
  */
 package com.cws.us.esolutions.validators;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.validation.Errors;
 import org.springframework.validation.Validator;
 import org.springframework.validation.ValidationUtils;
 
 import com.cws.us.esolutions.Constants;
 import com.cws.esolutions.core.processors.dto.SearchRequest;
 /**
  * eSolutions_java_source
  * com.cws.us.esolutions.validators
  * EmailMessageValidator.java
  *
  * TODO: Add class description
  *
  * $Id: $
  * $Author: $
  * $Date: $
  * $Revision: $
  * @author 35033355
  * @version 1.0
  *
  * History
  * ----------------------------------------------------------------------------
  * 35033355 @ Nov 18, 2013 2:45:18 PM
  *     Created.
  */
 public class SearchRequestValidator implements Validator
 {
     private static final String CNAME = EmailMessageValidator.class.getName();
 
     private static final Logger DEBUGGER = LoggerFactory.getLogger(Constants.DEBUGGER);
     private static final boolean DEBUG = DEBUGGER.isDebugEnabled();
 
     @Override
     public final boolean supports(final Class<?> target)
     {
         final String methodName = SearchRequestValidator.CNAME + "#supports(final Class<?> target)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Class: ", target);
         }
 
         final boolean isSupported = SearchRequest.class.isAssignableFrom(target);
 
         if (DEBUG)
         {
             DEBUGGER.debug("isSupported: {}", isSupported);
         }
 
         return isSupported;
     }
 
     @Override
     public final void validate(final Object target, final Errors errors)
     {
         final String methodName = SearchRequestValidator.CNAME + "#validate(final Object target, final Errors errors)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Object: {}", target);
             DEBUGGER.debug("Errors: {}", errors);
         }
 
         final SearchRequest value = (SearchRequest) target;
		final Pattern pattern = Pattern.compile("[^\w+([\s-_]\w+)*$]");
 
         if (DEBUG)
         {
             DEBUGGER.debug("Pattern: {}", pattern);
         }
 
         ValidationUtils.rejectIfEmptyOrWhitespace(errors, "searchTerms");
 
         if (!(pattern.matcher(value.getSearchTerms()).matches()))
         {
             errors.reject("searchTerms");
         }
     }
 }
