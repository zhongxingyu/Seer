 /*******************************************************************************
  * Copyright (c) 2012, 5AM Solutions, Inc.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the 
  * following conditions are met:
  *
  * Redistributions of source code must retain the above copyright notice, this list of conditions and the following 
  * disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following 
  * disclaimer in the documentation 
  * and/or other materials provided with the distribution.
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT 
  * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
  * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package com.fiveamsolutions.plc.web.struts2.interceptor;
 
 import java.lang.reflect.Method;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.Path;
 import javax.validation.Validation;
 import javax.validation.Validator;
 import javax.validation.ValidatorFactory;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.struts2.interceptor.validation.SkipValidation;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.opensymphony.xwork2.Action;
 import com.opensymphony.xwork2.ActionInvocation;
 import com.opensymphony.xwork2.TextProvider;
 import com.opensymphony.xwork2.ValidationAware;
 import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
 
 /**
  * @author Abraham J. Evans-EL <aevansel@5amsolutions.com>
  */
 public class HibernateValidatorInterceptor extends AbstractInterceptor {
 
     private static final long serialVersionUID = 1L;
     private static final Logger LOG = LoggerFactory.getLogger(HibernateValidatorInterceptor.class);
 
    private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
     private final Set<String> excludedMethodsSet = new LinkedHashSet<String>();
 
     /**
      * @return the excludeMethods
      */
     public String getExcludeMethods() {
         return StringUtils.join(excludedMethodsSet, ',');
     }
 
     /**
      * @param excludeMethods the excludeMethods to set
      */
     public void setExcludeMethods(String excludeMethods) {
         excludedMethodsSet.clear();
         if (StringUtils.isNotBlank(excludeMethods)) {
             String[] excludedMethodsList = excludeMethods.split(",");
             for (String excludedMethod : excludedMethodsList) {
                 if (StringUtils.isNotBlank(excludedMethod)) {
                     excludedMethodsSet.add(excludedMethod.trim());
                 }
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("PMD.SignatureDeclareThrowsException")
     @Override
     public String intercept(ActionInvocation invocation) throws Exception {
         Object actionObject = invocation.getAction();
         if (checkSkipValidation(actionObject, invocation.getProxy().getMethod())) {
             return invocation.invoke();
         }
         ValidationAware action = (ValidationAware) actionObject;
         validate(action);
         if (action.hasErrors()) {
             return Action.INPUT;
         } else {
             return invocation.invoke();
         }
     }
 
     private boolean checkSkipValidation(Object action, String methodName)
         throws NoSuchMethodException {
         if (!(action instanceof ValidationAware)) {
             LOG.debug("Action is not validation aware");
             return true;
         }
         if (excludedMethodsSet.contains(methodName)) {
             LOG.debug("No validation required for method " + methodName);
             return true;
         }
         Method method = action.getClass().getMethod(methodName);
         return method.isAnnotationPresent(SkipValidation.class);
     }
 
     private void validate(ValidationAware action) {
         Validator validator = validatorFactory.getValidator();
         Set<ConstraintViolation<ValidationAware>> constraintViolations = validator.validate(action);
         for (ConstraintViolation<ValidationAware> constraintViolation : constraintViolations) {
             Path fieldPath = constraintViolation.getPropertyPath();
             action.addFieldError(fieldPath.toString(), convertErrorMessage(action, constraintViolation));
         }
     }
 
     private String convertErrorMessage(Object action, ConstraintViolation<ValidationAware> constraintViolation) {
         StringBuffer errorMessage = new StringBuffer();
         if (action instanceof TextProvider) {
             TextProvider textProvider = (TextProvider) action;
             errorMessage.append(textProvider.getText(constraintViolation.getPropertyPath().toString()))
                 .append(' ')
                 .append(textProvider.getText(constraintViolation.getMessage()));
         } else {
             errorMessage.append(constraintViolation.getMessage());
         }
         return errorMessage.toString();
     }
 }
