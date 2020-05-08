 /*
  * PatientView
  *
  * Copyright (c) Worth Solutions Limited 2004-2013
  *
  * This file is part of PatientView.
  *
  * PatientView is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  * PatientView is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License along with PatientView in a file
  * titled COPYING. If not, see <http://www.gnu.org/licenses/>.
  *
  * @package PatientView
  * @link http://www.patientview.org
  * @author PatientView <info@patientview.org>
  * @copyright Copyright (c) 2004-2013, Worth Solutions Limited
  * @license http://www.gnu.org/licenses/gpl-3.0.html The GNU General Public License V3.0
  */
 package org.patientview.security.impl;
 
 import org.patientview.security.SecurityConfig;
 import org.patientview.security.UnitSecured;
 import org.patientview.security.model.SecurityUser;
 import org.patientview.service.SecurityUserManager;
 import org.patientview.service.UnitManager;
 import org.patientview.service.UserManager;
 import org.springframework.security.access.AccessDecisionVoter;
 import org.springframework.security.access.ConfigAttribute;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.web.FilterInvocation;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
import java.util.Collection;
 import javax.inject.Inject;
 
 /**
  * Implement finer grained authorisation per unit
  */
 @Transactional(propagation = Propagation.REQUIRED)
 public class UnitVoter implements AccessDecisionVoter {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(UnitVoter.class);
 
     @Inject
     private SecurityUserManager securityUserManager;
 
     @Override
     public boolean supports(ConfigAttribute configAttribute) {
 
         // not sure how this should really work - use the voter for everything
         return true;
     }
 
     @Override
     /**
      *  authentication: encapsulates the principal and their granted authorities
      *
      *  o: the actual secure object invocation,
      *
      *  e.g. for method based security: ReflectiveMethodInvocation:
      *  org.patientview.service.get(String unitCode);
      *  target is of class [org.patientview.service.impl.UnitManagerImpl]
      *
      *  collection: A list of ConfigAttribute, typically, SecurityConfig, which hold the attributes supplied by the
      *  @Secured("ROLE_ANY_USER") annotation
      */
     public int vote(Authentication authentication, Object o, Collection collection) {
 
         int result = ACCESS_DENIED;    // default deny access
 
         if (o instanceof MethodInvocation) {
             // securing a method
             MethodInvocation methodInvocation = (MethodInvocation) o;
             result = voteOnMethodInvocation(authentication, methodInvocation);
 
         } else if (o instanceof FilterInvocation) {
             // without securing a url
             result = ACCESS_GRANTED;
         }
 
         return result;
     }
 
     private int voteOnMethodInvocation(Authentication authentication, MethodInvocation methodInvocation) {
 
         // Check for the @UnitSecured annotation that provides custom method security
         int result = ACCESS_DENIED;
         if (authentication.getPrincipal() instanceof SecurityUser) {
             SecurityUser user = (SecurityUser) authentication.getPrincipal();
 
             UnitSecured securedAnnotation = methodInvocation.getMethod().getAnnotation(UnitSecured.class);
 
             // super admins bypass method security
             if (securityUserManager.isRolePresent("superadmin")) {
                result = ACCESS_GRANTED;
             }
 
             if (securedAnnotation != null && securedAnnotation.value() != null) {
                 if (securedAnnotation.value().equals(SecurityConfig.UNIT_ACCESS)) {
                     result = voteOnUnitAccessMethodInvocation(methodInvocation);
                 }
 
             } else {
                 // There is no UnitSecured annotation - access granted
                 result = ACCESS_GRANTED;
             }
         } else {
             result = ACCESS_GRANTED;
         }
 
         if (result == ACCESS_DENIED) {
             // report authorisation fail
             LOGGER.info("Vote failed on method: {}, result: {}", methodInvocation.getMethod(), result);
         } else {
             // show add debug
             LOGGER.debug("Vote on method: {}, result: {}", methodInvocation.getMethod(), result);
         }
 
         return result;
     }
 
     /**
      * Authenticate the login user request valid user or unit.
      *
      * @param methodInvocation
      * @return 1: valid request, -1: invalid request
      */
     private int voteOnUnitAccessMethodInvocation(MethodInvocation methodInvocation) {
 
         String argument = getMethodArgument(methodInvocation);
 
         if (methodInvocation.getMethod().getDeclaringClass().getSimpleName()
                 .equals(UnitManager.class.getSimpleName())) {
             if (securityUserManager.userHasReadAccessToUnit(argument)) {
                 return ACCESS_GRANTED;
             }
 
         } else if (methodInvocation.getMethod().getDeclaringClass().getSimpleName()
                 .equals(UserManager.class.getSimpleName())) {
             if (securityUserManager.userHasReadAccessToUnitUser(argument)) {
                 return ACCESS_GRANTED;
             }
         }
 
         return ACCESS_DENIED;
     }
 
     /**
      * Get the username associated with the method invocation
      *
      * @param methodInvocation method
      * @return username
      */
     private String getMethodArgument(MethodInvocation methodInvocation) {
         String result = null;
 
         try {
             Object[] arguments = methodInvocation.getArguments();
             if (arguments.length > 0) {
                 Object arg0 = arguments[0];
                 if (arg0 instanceof String) {
                     result = String.valueOf(arg0);
                 }
             }
         } catch (Exception e) {
             LOGGER.error("Error parsing username id from methodInvocation: " + e.getMessage());
         }
 
         return result;
     }
 
     @Override
     public boolean supports(Class aClass) {
         // not sure how this should really work - use the voter for everything
         return true;
     }
 
     public void setSecurityUserManager(SecurityUserManager securityUserManager) {
         this.securityUserManager = securityUserManager;
     }
 }
