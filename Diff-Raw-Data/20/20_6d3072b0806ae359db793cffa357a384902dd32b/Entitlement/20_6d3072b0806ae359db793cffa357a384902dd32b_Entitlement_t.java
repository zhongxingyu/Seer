 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2008 Sun Microsystems Inc. All Rights Reserved
  *
  * The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: Entitlement.java,v 1.10 2009-01-22 20:03:11 veiming Exp $
  */
 
 package com.sun.identity.entitlement;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import javax.security.auth.Subject;
 
 /**
  * This class encapsulates entitlement of a subject.
  * <p>
  * Example of how to use this class
  * <pre>
  *     Set set = new HashSet();
  *     set.add("GET");
  *     Evaluator evaluator = new Evaluator(adminToken);
  *     boolean isAllowed = evaluator.hasEntitlement(subject, 
  *         new Entitlement("http://www.sun.com/example", set), 
  *         Collections.EMPTY_MAP);
  * </pre>
  * Or do a sub tree search like this.
  * <pre>
  *     Evaluator evaluator = new Evaluator(adminToken);
  *     List&lt;Entitlement> entitlements = evaluator.getEntitlements(
  *         subject, "http://www.sun.com", Collections.EMPTY_MAP, true);
  *     for (Entitlement e : entitlements) {
  *         String resource = e.getResourceNames();
  *         boolean isAllowed =((Boolean)e.getActionValue("GET")).booleanValue();
  *         ...
  *     }
  * </pre>
  */ 
 public class Entitlement {
     private String serviceName;
     private String resourceName;
     private Set<String> excludedResourceNames; //TODO - add methods
     private Map<String, Object> actionValues;
     private Map<String, String> advices;
     private Map<String, Set<String>> attributes;
 
     /**
      * Creates an entitlement object with default service name.
      */
     public Entitlement() {
     }
 
     /**
      * Creates an entitlement object.
      *
      * @param resourceName Resource name.
     * @param actionNames Set of action names.
      */
    public Entitlement(String resourceName,  Set<String> actionNames) {
         setResourceName(resourceName);
        setActionNames(actionNames);
     }
 
     /**
      * Creates an entitlement object.
      *
      * @param serviceName Service name.
      * @param resourceName Resource name.
     * @param actionNames Set of action names.
      */
     public Entitlement(
         String serviceName, 
         String resourceName, 
        Set<String> actionNames
     ) {
         this.serviceName = serviceName;   
         setResourceName(resourceName);
        setActionNames(actionNames);
     }
 
     /**
      * Creates an entitlement object.
      *
      * @param resourceName Resource name.
      * @param actionValues Map of action name to set of values.
      */
     public Entitlement(
         String resourceName,  
         Map<String, Object> actionValues
     ) {
         setResourceName(resourceName);
         setActionValues(actionValues);
     }
 
     /**
      * Creates an entitlement object.
      *
      * @param serviceName Service name.
      * @param resourceName Resource name.
      * @param actionValues Map of action name to set of values.
      */
     public Entitlement(
         String serviceName, 
         String resourceName,  
         Map<String, Object> actionValues
     ) {
         this.serviceName = serviceName;   
         setResourceName(resourceName);
         setActionValues(actionValues);
     }
     
     /**
      * Sets resource name.
      *
      * @param resourceName Resource Name.
      */
     public void setResourceName(String resourceName) {
         this.resourceName = resourceName;
     }
 
     /**
      * Returns resource name.
      *
      * @return resource name.
      */
     public String getResourceName() {
         return resourceName;
     }
 
      /**
      * Sets excluded resource names.
      *
      * @param excludedResourceNames excluded resource names.
      */
     public void setExcludedResourceNames(
             Set<String> excludedResourceNames) {
         this.excludedResourceNames = excludedResourceNames;
     }
 
     /**
      * Returns excluded resource names.
      *
      * @return excluded resource names.
      */
     public Set<String> getExcludedResourceNames() {
         return excludedResourceNames;
     }
     /**
      * Returns service name.
      *
      * @return service name.
      */
     public String getServiceName() {
         return serviceName;
     }
 
     /**
      * Sets action name
      *
      * @param actionName Action name.
      */
     public void setActionName(String actionName) {
         actionValues = new HashMap<String, Object>();
         actionValues.put(actionName, Boolean.TRUE);
     }
 
     /**
      * Sets action names
      *
      * @param actionNames Set of action names.
      */
     public void setActionNames(Set<String> actionNames) {
         actionValues = new HashMap<String, Object>();
         for (String i : actionNames) {
             actionValues.put(i, Boolean.TRUE);
         }
     }
 
     /**
      * Sets action values map.
      *
      * @param actionValues Action values.
      */
     public void setActionValues(Map<String, Object> actionValues) {
         this.actionValues = new HashMap<String, Object>();
         for (String key : actionValues.keySet()) {
             Object val = actionValues.get(key);
             
             if (val instanceof Set) {
                 Set copyOf = new HashSet<Object>((Set<Object>)val);
                 this.actionValues.put(key, copyOf);
             } else {
                 this.actionValues.put(key, val);
             }
         }
         
         this.actionValues = actionValues;
     }
 
     /**
      * Returns action value.
      *
      * @param name Name of the action.
      * @return action values.
      */
     public Object getActionValue(String name) {
         return actionValues.get(name);
     }
 
     /**
      * Returns action values.
      *
      * @return action values.
      */
     public Map<String, Object> getActionValues() {
         return actionValues;
     }
     
     /**
      * Returns action values.
      *
      * @param name Name of the action.
      * @return action values.
      */
     public Set<Object> getActionValues(String name) {
         Object o = actionValues.get(name);
         if (o instanceof Set) {
             return (Set<Object>)o;
         }
 
         Set<Object> set = new HashSet<Object>();
         set.add(o);
         return set;
     }
 
     /**
      * Sets advices.
      *
      * @param advices Advices.
      */
     public void setAdvices(Map<String, String> advices) {
         this.advices = advices;
     }
 
     /**
      * Returns advices.
      *
      * @return Advices.
      */
     public Map<String, String> getAdvices() {
         return advices;
     }
 
     /**
      * Sets attributes.
      *
      * @param attributes Attributes.
      */
     public void setAttributes(Map<String, Set<String>> attributes) {
         this.attributes = attributes;
     }
 
     /**
      * Returns attributes.
      *
      * @return Attributes.
      */
     public Map<String, Set<String>> getAttributes() {
         return attributes;
     }
 
     
     /**
      * Returns <code>true</code> if the request satisfies the request
      * @param subject Subject who is under evaluation.
      * @param resourceName Resource name.
      * @param environment Environment parameters.
      * @return <code>true</code> if the request satisfies the 
      * <code>SubjectFilter</code>, otherwise <code>false</code>
      * @throws com.sun.identity.entitlement.EntitlementException
      */
     public boolean evaluate(
             Subject subject,
             String resourceName,
             Map<String, Set<String>> environment)
             throws EntitlementException {
 		return false;
 	}
 	
 }
