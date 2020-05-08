 /*
  * Copyright 2006 wingS development team.
  *
  * This file is part of wingS (http://www.j-wings.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package org.wings.plaf.css.dwr;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.WeakHashMap;
 import org.directwebremoting.extend.AccessControl;
 import org.directwebremoting.extend.Creator;
 import org.directwebremoting.WebContextFactory;
 import javax.servlet.http.HttpSession;
import org.directwebremoting.util.Messages;
 import javax.servlet.http.HttpServletRequest;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Control who should be accessing which methods on which classes. 
  * Specialized to use in sessions.
  * 
  * @author Christian Schyma
  */
 public class SessionAccessControl implements AccessControl {
     
     private final transient static Log log = LogFactory.getLog(SessionAccessControl.class);
     
     /**
      * ban DWR classes from being created or marshalled
      */
     private static final String PACKAGE_DWR_DENY = "org.directwebremoting.";
         
     public void addRoleRestriction(String scriptName, String methodName, String role) {
         String key = scriptName + '.' + methodName;
         Set roles = (Set) getRoleRestrictMap().get(key);
         if (roles == null) {
             roles = new HashSet();
             getRoleRestrictMap().put(key, roles);
         }
         
         roles.add(role);
     }
     
     public void addIncludeRule(String scriptName, String methodName) {
         Policy policy = getPolicy(scriptName);
         
         // If the policy for the given type is defaultAllow then we need to go
         // to default disallow mode, and check that the are not rules applied
         if (policy.defaultAllow) {
             if (policy.rules.size() > 0) {
                throw new IllegalArgumentException(Messages.getString("DefaultAccessControl.MixedIncludesAndExcludes", scriptName));
             }
             
             policy.defaultAllow = false;
         }
         
         // Add the rule to this policy
         policy.rules.add(methodName);
     }
     
     public void addExcludeRule(String scriptName, String methodName) {
         Policy policy = getPolicy(scriptName);
         
         // If the policy for the given type is defaultAllow then we need to go
         // to default disallow mode, and check that the are not rules applied
         if (!policy.defaultAllow) {
             if (policy.rules.size() > 0) {
                throw new IllegalArgumentException(Messages.getString("DefaultAccessControl.MixedIncludesAndExcludes", scriptName));
             }
             
             policy.defaultAllow = true;
         }
         
         // Add the rule to this policy
         policy.rules.add(methodName);
     }
     
     /**
      * @param scriptName The name of the creator to Javascript
      * @param methodName The name of the method (without brackets)
      * @return A Set of all the roles for the given script and method
      */
     private Set getRoleRestrictions(String scriptName, String methodName) {
         String key = scriptName + '.' + methodName;
         return (Set) getRoleRestrictMap().get(key);
     }
     
     /**
      * Test to see if a method is excluded or included.
      * @param scriptName The name of the creator to Javascript
      * @param methodName The name of the method (without brackets)
      * @return true if the method is allowed by the rules in addIncludeRule()
      * @see AccessControl#addIncludeRule(String, String)
      */
     private boolean isExecutable(String scriptName, String methodName) {
         Policy policy = (Policy) getPolicyMap().get(scriptName);
         if (policy == null) {
             return true;
         }
         
         // Find a match for this method in the policy rules
         String match = null;
         for (Iterator it = policy.rules.iterator(); it.hasNext() && match == null;) {
             String test = (String) it.next();
             
             // If at some point we wish to do regex matching on rules, here is
             // the place to do it.
             if (methodName.equals(test)) {
                 match = test;
             }
         }
         
         if (policy.defaultAllow && match != null) {
             // We are in default allow mode so the rules are exclusions and we
             // have a match, so this method is excluded.
             //log.debug("method excluded for creator " + type + " due to defaultAllow=" + policy.defaultAllow + " and rule: " + match);
             return false;
         }
         
         // There may be a more optimized if statement here, but I value code
         // clarity over performance.
         //noinspection RedundantIfStatement
         
         if (!policy.defaultAllow && match == null) {
             // We are in default deny mode so the rules are inclusions and we
             // do not have a match, so this method is excluded.
             //log.debug("method excluded for creator " + type + " due to defaultAllow=" + policy.defaultAllow + " and rule: " + match);
             return false;
         }
         
         return true;
     }
     
     /**
      * Find the policy for the given type and create one if none exists.
      * @param type The name of the creator
      * @return The policy for the given Creator
      */
     private Policy getPolicy(String type) {
         Policy policy = (Policy) getPolicyMap().get(type);
         if (policy == null) {
             policy = new Policy();
             getPolicyMap().put(type, policy);
         }
         
         return policy;
     }
     
     /**
      * Get AccessMap from session.
      */
     private Map getAccessMap() {
         HttpSession session = WebContextFactory.get().getSession();
         Map map = (Map) session.getAttribute("AccessMap");
         if (map == null) {
             map = new WeakHashMap();
             session.setAttribute("AccessMap", map);
         }
         return map;
     }
     
     /**
      * Get PolicyMap from session.
      */
     private Map getPolicyMap() {
         HttpSession session = WebContextFactory.get().getSession();
         Map map = (Map) session.getAttribute("PolicyMap");
         if (map == null) {
             map = new WeakHashMap();
             session.setAttribute("PolicyMap", map);
         }
         
         return map;
     }
     
     /**
      * Get RolseRestrictMap from session.
      */
     private Map getRoleRestrictMap() {
         HttpSession session = WebContextFactory.get().getSession();
         Map map = (Map) session.getAttribute("RoleRestrictMap");
         if (map == null) {
             map = new WeakHashMap();
             session.setAttribute("RoleRestrictMap", map);
         }
         
         return map;
     }
 
     public void assertExecutionIsPossible(Creator creator, String className, Method method) throws SecurityException {
         String methodName = method.getName();
         
         // What if there is some J2EE role based restriction?
         Set roles = getRoleRestrictions(className, methodName);
         if (roles != null) {
             boolean allowed = false;
             
             HttpServletRequest req = WebContextFactory.get().getHttpServletRequest();
             if (req == null) {
                 log.warn("Missing HttpServletRequest roles can not be checked");
             } else {
                 for (Iterator it = roles.iterator(); it.hasNext() && !allowed;) {
                     String role = (String) it.next();
                     if (req.isUserInRole(role)) {
                         allowed = true;
                     }
                 }
             }
             
             if (!allowed) {
                 // This just creates a list of allowed roles for better debugging
                 StringBuffer buffer = new StringBuffer();
                 for (Iterator it = roles.iterator(); it.hasNext();) {
                     String role = (String) it.next();
                     buffer.append(role);
                     if (it.hasNext()) {
                         buffer.append(", ");
                     }
                 }
                 
                throw new SecurityException(Messages.getString("ExecuteQuery.DeniedByJ2EERoles", buffer.toString()));                
             }
         }
                 
     }
 
     public void assertIsDisplayable(Creator creator, String className, Method method) throws SecurityException {
         String methodName = method.getName();
         
         // Is it public
         if (!Modifier.isPublic(method.getModifiers())) {
            throw new SecurityException(Messages.getString("ExecuteQuery.DeniedNonPublic"));
         }
         
         // Do access controls allow it?
         if (!isExecutable(className, methodName)) {
             throw new SecurityException("Method access is denied by rules");
         }
         
         // We ban some methods from Object too
         if (method.getDeclaringClass() == Object.class) {
            throw new SecurityException(Messages.getString("ExecuteQuery.DeniedObjectMethod"));
         }
         
         if (creator.getType().getName().startsWith(PACKAGE_DWR_DENY)) {
            throw new SecurityException(Messages.getString("ExecuteQuery.DeniedCoreDWR"));
         }
         
         // Check the parameters are not DWR internal either
         for (int j = 0; j < method.getParameterTypes().length; j++) {
             Class paramType = method.getParameterTypes()[j];
             
             if (paramType.getName().startsWith(PACKAGE_DWR_DENY)) {
                throw new SecurityException(Messages.getString("ExecuteQuery.DeniedParamDWR"));
             }
         }
                 
     }
     
     /**
      * A struct that contains a method access policy for a Creator
      */
     static class Policy {
         boolean defaultAllow = false;
         List rules = new ArrayList();
     }
         
 }
