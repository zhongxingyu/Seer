 /*
     jBilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2009 Enterprise jBilling Software Ltd. and Emiliano Conde
 
     This file is part of jbilling.
 
     jbilling is free software: you can redistribute it and/or modify
     it under the terms of the GNU Affero General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     jbilling is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Affero General Public License for more details.
 
     You should have received a copy of the GNU Affero General Public License
     along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package com.sapienter.jbilling.server.util.api.validation;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Set;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.Validator;
 
 import org.apache.log4j.Logger;
 import org.springframework.aop.MethodBeforeAdvice;
 
 import com.sapienter.jbilling.common.SessionInternalError;
 
 /**
  *
  * @author emilc
  */
 public class APIValidator implements MethodBeforeAdvice {
 
     private static final Logger LOG = Logger.getLogger(APIValidator.class);
     
     private Validator validator;
     private Set<String> objectsToTest = null;
 
     public Validator getValidator() {
 		return validator;
 	}
 
 	public void setValidator(Validator validator) {
 		this.validator = validator;
 	}
 
 	public static Logger getLog() {
 		return LOG;
 	}
 
 	public void before(Method method, Object[] args, Object target) throws Throwable {
 		ArrayList<String> errors = new ArrayList<String>();
 		
         for (Object arg: args) {
             if (arg != null) {
                 String objectname = arg.getClass().getName();
                 boolean testThisObject = false;
                 for (String test: objectsToTest) {
                     if (objectname.endsWith(test)) {
                         testThisObject = true;
                         break;
                     }
                 }
                 if (testThisObject) {
                     // it always does the default
                     Set<ConstraintViolation<Object>> constraintViolations =
                             validator.validate(arg);
 
                     if (method.getName().startsWith("create")) {
                         constraintViolations.addAll(validator.validate(arg, CreateValidationGroup.class));
                     } else if (method.getName().startsWith("update")) {
                        constraintViolations.addAll(validator.validate(arg, UpdateValidationGroup.class));
                     }
 
                     if (constraintViolations.size() > 0) {
                         for (ConstraintViolation<Object> violation: constraintViolations) {
                             // compose the error message
                             String shortObjectName = objectname.substring(objectname.lastIndexOf('.') + 1);
                             errors.add(shortObjectName + "," + violation.getPropertyPath().toString() + "," +
                                        violation.getMessage());
                         }
                         LOG.debug("Calling " + method.getName() + " found an error in " + objectname);
                     }
                 }
             }
         }
         
         if (errors.size() > 0) {
         	SessionInternalError exception = new SessionInternalError();
         	exception.setErrorMessages(new String[errors.size()]);
         	errors.toArray(exception.getErrorMessages());
         	throw exception;
         }
     }
 
 	public void setObjectsToTest(Set<String> objectsToTest) {
 		this.objectsToTest = objectsToTest;
 	}
 
 	public Set<String> getObjectsToTest() {
 		return objectsToTest;
 	}
 }
