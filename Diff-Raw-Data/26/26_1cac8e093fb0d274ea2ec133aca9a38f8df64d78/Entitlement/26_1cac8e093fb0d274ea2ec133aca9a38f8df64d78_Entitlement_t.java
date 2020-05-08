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
 * $Id: Entitlement.java,v 1.3 2008-12-12 18:51:07 veiming Exp $
  */
 
 package com.sun.identity.entitlement;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * This class encapsulates entitlement of a subject.
  * <p>
  * Example of how to use this class
  * <pre>
  *     Entitlement e = new Entitlement();
  *     e.setResourceName("http://www.sun.com/example");
  *     e.setActionName("GET");
  *     Evaluator evaluator = new Evaluator();
  *     boolean isAllowed = evaluator.hasEntitlement(subject, e, 
  *         Collections.EMPTY_MAP);
  * </pre>
  * Or do a sub tree search like this.
  * <pre>
  *     Evaluator evaluator = new Evaluator();
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
      * @param serviceName Service Name.
      */
     public Entitlement(String serviceName) {
         this.serviceName = serviceName;       
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
     public void setActionName(Set<String> actionNames) {
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
     public void setActionValuesMap(Map<String, Object> actionValues) {
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
 }
