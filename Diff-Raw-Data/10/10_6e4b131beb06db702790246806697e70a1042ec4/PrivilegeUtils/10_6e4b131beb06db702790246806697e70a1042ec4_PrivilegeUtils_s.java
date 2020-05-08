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
  * at opensso/legal/CDDLv1.0.txt
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: PrivilegeUtils.java,v 1.14 2009-04-29 11:43:12 veiming Exp $
  */
 package com.sun.identity.entitlement.opensso;
 
 import com.iplanet.sso.SSOToken;
 import com.iplanet.sso.SSOException;
 import com.sun.identity.entitlement.Entitlement;
 import com.sun.identity.entitlement.EntitlementCondition;
 import com.sun.identity.entitlement.EntitlementSubject;
 import com.sun.identity.entitlement.OrSubject;
 import com.sun.identity.entitlement.OrCondition;
 import com.sun.identity.entitlement.AndCondition;
 import com.sun.identity.entitlement.Privilege;
 import com.sun.identity.entitlement.EntitlementException;
 import com.sun.identity.entitlement.PolicyCondition;
 import com.sun.identity.entitlement.PolicySubject;
 import com.sun.identity.entitlement.ResourceAttributes;
 import com.sun.identity.entitlement.StaticAttributes;
 import com.sun.identity.entitlement.UserAttributes;
 import com.sun.identity.policy.ActionSchema;
 import com.sun.identity.policy.InvalidNameException;
 import com.sun.identity.policy.Policy;
 import com.sun.identity.policy.PolicyConfig;
 import com.sun.identity.policy.PolicyException;
 import com.sun.identity.policy.Rule;
 import com.sun.identity.policy.ServiceType;
 import com.sun.identity.policy.ServiceTypeManager;
 import com.sun.identity.policy.interfaces.Condition;
 import com.sun.identity.policy.interfaces.ResponseProvider;
 import com.sun.identity.policy.interfaces.Subject;
 import com.sun.identity.policy.plugins.IDRepoResponseProvider;
 import com.sun.identity.policy.plugins.PrivilegeCondition;
 import com.sun.identity.policy.plugins.PrivilegeSubject;
 import com.sun.identity.security.AdminTokenAction;
 import java.security.AccessController;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 
 /**
  * Class with utility methods to map from
  * <code>com.sun.identity.entity.Privilege</code>
  * to
  * </code>com.sun.identity.policy.Policy</code>
  */
 public class PrivilegeUtils {
 
     private static Random random = new Random();
     private static ServiceTypeManager svcTypeManager;
 
     static {
         try {
             SSOToken adminToken = (SSOToken) AccessController.doPrivileged(
                 AdminTokenAction.getInstance());
             svcTypeManager = new ServiceTypeManager(adminToken);
         } catch (SSOException ex) {
             //TOFIX
         }
     }
 
     /**
      * Constructs PrivilegeUtils
      */
     private PrivilegeUtils() {
     }
 
     /**
      * Maps a OpenSSO Policy to entitlement Privilege
      * @param policy OpenSSO Policy object
      * @return entitlement Privilege object
      * @throws com.sun.identity.policy.PolicyException if the mapping fails
      */
     public static Privilege policyToPrivilege(Policy policy)
             throws PolicyException, EntitlementException {
         //TODO: split a policy to multiple prrivileges if the rules have
         // different acation values
         if (policy == null) {
             return null;
         }
 
         String policyName = policy.getName();
         Set ruleNames = policy.getRuleNames();
         Set<Rule> rules = new HashSet<Rule>();
         for (Object ruleNameObj : ruleNames) {
             String ruleName = (String) ruleNameObj;
             Rule rule = policy.getRule(ruleName);
             rules.add(rule);
         }
         Entitlement entitlement = null;
         try {
             entitlement = rulesToEntitlement(rules);
         } catch (SSOException e) {
             //TODO: record, wrap and propogate the exception
         }
 
         Set subjectNames = policy.getSubjectNames();
         Set nqSubjects = new HashSet();
         for (Object subjectNameObj : subjectNames) {
             String subjectName = (String) subjectNameObj;
             Subject subject = policy.getSubject(subjectName);
             boolean exclusive = policy.isSubjectExclusive(subjectName);
             Object[] nqSubject = new Object[3];
             nqSubject[0] = subjectName;
             nqSubject[1] = subject;
             nqSubject[2] = exclusive;
             nqSubjects.add(nqSubject);
         }
         EntitlementSubject eSubject = nqSubjectsToESubject(nqSubjects);
 
         Set conditionNames = policy.getConditionNames();
         Set nConditions = new HashSet();
         for (Object conditionNameObj : conditionNames) {
             String conditionName = (String) conditionNameObj;
             Condition condition = policy.getCondition(conditionName);
             Object[] nCondition = new Object[2];
             nCondition[0] = conditionName;
             nCondition[1] = condition;
             nConditions.add(nCondition);
         }
         EntitlementCondition eCondition = nConditionsToECondition(nConditions);
 
         Set rpNames = policy.getResponseProviderNames();
         Set nrps = new HashSet();
         for (Object rpNameObj : rpNames) {
             String rpName = (String) rpNameObj;
             ResponseProvider rp = policy.getResponseProvider(rpName);
             Object[] nrp = new Object[2];
             nrp[0] = rpName;
             nrp[1] = rp;
             nrps.add(nrp);
         }
         Set<ResourceAttributes> resourceAttributesSet =
             nrpsToResourceAttributes(nrps);
 
         Privilege privilege = new OpenSSOPrivilege(policyName, entitlement,
             eSubject, eCondition, resourceAttributesSet);
         privilege.setCreatedBy(policy.getCreatedBy());
         privilege.setLastModifiedBy(policy.getLastModifiedBy());
         privilege.setCreationDate(policy.getCreationDate());
         privilege.setLastModifiedDate(policy.getLastModifiedDate());
         
         return privilege;
     }
 
     private static Entitlement rulesToEntitlement(Set<Rule> rules)
             throws PolicyException, SSOException {
         if (rules == null || rules.isEmpty()) {
             return null;
         }
         Set<String> resourceNames = new HashSet<String>();
         Set<String> excludedResourceNames = new HashSet<String>();
         Rule lrule = null;
         //TODO: split a policy to multiple prrivileges if the rules have different
         // acation values
         for (Rule rule : rules) {
             lrule = rule;
             String resourceName = rule.getResourceName();
             Set excludedResourceNames1 = rule.getExcludedResourceNames();
             resourceNames.add(resourceName);
             if (excludedResourceNames1 != null) {
                 excludedResourceNames.addAll(excludedResourceNames1);
             }
 
         }
         String serviceName = lrule.getServiceTypeName();
         Map<String, Boolean> actionMap = pavToPrav(lrule.getActionValues(), 
                 serviceName);
         String entitlementName = lrule.getName();
         int dashi = entitlementName.indexOf("---");
         if (dashi != -1) {
             entitlementName = entitlementName.substring(0, dashi);
         }
 
         Entitlement entitlement = new Entitlement(serviceName, resourceNames,
                 actionMap);
         entitlement.setName(entitlementName);
         entitlement.setExcludedResourceNames(excludedResourceNames);
         return entitlement;
     }
 
     private static EntitlementSubject nqSubjectsToESubject(Set nqSubjects) {
         Set<EntitlementSubject> esSet = new HashSet<EntitlementSubject>();
         for (Object nqSubjectObj : nqSubjects) {
             Object[] nqSubject = (Object[]) nqSubjectObj;
             esSet.add(mapGenericSubject(nqSubject));
         }
 
         if (esSet.isEmpty()) {
             return null;
         }
         if (esSet.size() == 1) {
             return esSet.iterator().next();
         }
 
         return new OrSubject(esSet);
     }
 
     private static EntitlementCondition nConditionsToECondition(Set nConditons)
     {
         Set<EntitlementCondition> ecSet = new HashSet<EntitlementCondition>();
         for (Object nConditionObj : nConditons) {
             Object[] nCondition = (Object[]) nConditionObj;
             EntitlementCondition ec = mapGenericCondition(nCondition);
             ecSet.add(ec);
         }
 
         if (ecSet.isEmpty()) {
             return null;
         }
         if (ecSet.size() == 1) {
             return ecSet.iterator().next();
         }
 
         Map<String, Set<EntitlementCondition>> cnEntcMap =
             new HashMap<String, Set<EntitlementCondition>>();
         for (EntitlementCondition ec : ecSet) {
             String key = ec.getClass().getName();
             Set<EntitlementCondition> values = cnEntcMap.get(key);
             if (values == null) {
                 values = new HashSet<EntitlementCondition>();
                 cnEntcMap.put(key, values);
             }
             values.add(ec);
         }
         
         Set<String> keySet = cnEntcMap.keySet();
         if (keySet.size() == 1) {
             Set<EntitlementCondition> values =
                 cnEntcMap.get(keySet.iterator().next());
             return (values.size() == 1) ? values.iterator().next() :
                 new OrCondition(values);
         }
 
         Set andSet = new HashSet();
         for (String key : keySet) {
             Set values = (Set)cnEntcMap.get(key);
             if (values.size() == 1) {
                 andSet.add(values.iterator().next());
             } else {
                 andSet.add(new OrCondition(values));
             }
         }
         return new AndCondition(andSet);
     }
 
     private static EntitlementSubject mapGenericSubject(
         Object[] nSubject) {
         try {
             Object objSubject = nSubject[1];
             if (objSubject instanceof
                 com.sun.identity.policy.plugins.PrivilegeSubject) {
                 com.sun.identity.policy.plugins.PrivilegeSubject pips =
                     (com.sun.identity.policy.plugins.PrivilegeSubject)
                     objSubject;
                 Set<String> values = pips.getValues();
                 String val = values.iterator().next();
                 int idx = val.indexOf("=");
                 String className = val.substring(0, idx);
                 String state = val.substring(idx+1);
                 EntitlementSubject es =
                     (EntitlementSubject)Class.forName(className).newInstance();
                 es.setState(state);
                 return es;
             } else if (objSubject instanceof Subject) {
                 Subject sbj = (Subject)objSubject;
                 Set<String> val = sbj.getValues();
                 String className = sbj.getClass().getName();
                 return new PolicySubject((String)nSubject[0],
                     className, val, (Boolean)nSubject[2]);
             }
         } catch (ClassNotFoundException ex) {
             //TOFIX
         } catch (InstantiationException ex) {
             //TOFIX
         } catch (IllegalAccessException ex) {
             //TOFIX
         }
         return null;
     }
 
     private static EntitlementCondition mapGenericCondition(
         Object[] nCondition) {
         try {
             Object objCondition = nCondition[1];
             if (objCondition instanceof
                 com.sun.identity.policy.plugins.PrivilegeCondition) {
                 com.sun.identity.policy.plugins.PrivilegeCondition pipc =
                     (com.sun.identity.policy.plugins.PrivilegeCondition)
                     objCondition;
                 Map<String, Set<String>> props = pipc.getProperties();
                 String className = props.keySet().iterator().next();
                 EntitlementCondition ec =
                     (EntitlementCondition)Class.forName(className).newInstance();
                 Set<String> setValues = props.get(className);
                 ec.setState(setValues.iterator().next());
                 return ec;
             } else if (objCondition instanceof Condition) {
                 Condition cond = (Condition)objCondition;
                 Map<String, Set<String>> props = cond.getProperties();
                 String className = cond.getClass().getName();
                 return new PolicyCondition((String)nCondition[0], className,
                     props);
             }
         } catch (ClassNotFoundException ex) {
             //TOFIX
         } catch (InstantiationException ex) {
             //TOFIX
         } catch (IllegalAccessException ex) {
             //TOFIX
         }
         return null;
     }
 
     public static Policy privilegeToPolicy(Privilege privilege)
             throws PolicyException, SSOException {
         Policy policy = null;
         policy = new Policy(privilege.getName());
         if (privilege.getEntitlement() != null) {
             Entitlement entitlement = privilege.getEntitlement();
             Set<Rule> rules = entitlementToRules(entitlement);
             for (Rule rule : rules) {
                 policy.addRule(rule);
             }
         }
 
         EntitlementSubject es = privilege.getSubject();
         if (es != null) {
             Subject sbj = eSubjectToEPSubject(es);
             policy.addSubject(randomName(), sbj, false);
         }
 
         EntitlementCondition ec = privilege.getCondition();
         if (ec != null) {
             Condition cond = eConditionToEPCondition(ec);
             policy.addCondition(randomName(), cond);
         }
 
         if (privilege.getResourceAttributes() != null) {
             List nrps = resourceAttributesToResponseProviders(
                     privilege.getResourceAttributes());
             for (Object obj : nrps) {
                 Object[] arr = (Object[]) obj;
                 String pResponseProviderName = (String) arr[0];
                 ResponseProvider responseProvider = (ResponseProvider) arr[1];
                 policy.addResponseProvider(pResponseProviderName, responseProvider);
             }
         }
 
         policy.setCreatedBy(privilege.getCreatedBy());
         policy.setCreationDate(privilege.getCreationDate());
         policy.setLastModifiedBy(privilege.getLastModifiedBy());
         policy.setLastModifiedDate(privilege.getLastModifiedDate());
         return policy;
     }
 
     private static Set<Rule> entitlementToRules(Entitlement entitlement)
             throws PolicyException, SSOException {
         Set<Rule> rules = new HashSet<Rule>();
         String entName = entitlement.getName();
        String serviceName = entitlement.getApplicationName();
         Set<String> resourceNames = entitlement.getResourceNames();
         Map<String, Boolean> actionValues = entitlement.getActionValues();
         Map av = pravToPav(actionValues, serviceName);
         if (resourceNames != null) {
             int rc = 0;
             for (String resourceName : resourceNames) {
                 rc += 1;
                 Rule rule = new Rule(entName + "---" + rc, serviceName,
                         resourceName, av);
                 rule.setExcludedResourceNames(
                         entitlement.getExcludedResourceNames());
                 rules.add(rule);
             }
         }
         return rules;
     }
 
 
     private static Subject eSubjectToEPSubject(EntitlementSubject es) {
         PrivilegeSubject ps = new PrivilegeSubject();
         Set<String> values = new HashSet<String>();
         values.add(es.getClass().getName() + "=" + es.getState());
         ps.setValues(values);
         return ps;
     }
 
     private static Condition eConditionToEPCondition(
         EntitlementCondition ec
     ) throws PolicyException {
         PrivilegeCondition pc = new PrivilegeCondition();
         Map<String, Set<String>> map = new HashMap<String, Set<String>>();
         Set<String> set = new HashSet<String>(2);
         set.add(ec.getState());
         map.put(ec.getClass().getName(), set);
         pc.setProperties(map);
         return pc;
     }
 
     private static Set<ResourceAttributes> nrpsToResourceAttributes(
             Set nrps) throws EntitlementException {
         Set<ResourceAttributes> resourceAttributesSet = new HashSet();
         if (nrps != null && !nrps.isEmpty()) {
             for (Object nrpObj : nrps) {
                 Object[] nrpa = (Object[]) nrpObj;
                 String nrpName = (String) nrpa[0];
                 ResponseProvider rp = (ResponseProvider) nrpa[1];
                 if (rp instanceof IDRepoResponseProvider) {
                     IDRepoResponseProvider irp = (IDRepoResponseProvider) rp;
                     Map props = irp.getProperties();
                     if (props != null) {
                         Set sas = (Set) props.get(irp.STATIC_ATTRIBUTE);
                         if (sas != null && !sas.isEmpty()) {
                             StaticAttributes sa = new StaticAttributes();
                             Map saprops = new HashMap();
                             for (Object obj : sas) {
                                 String sat = (String) obj;
                                 int i = sat.indexOf("=");
                                 String name = null;
                                 String value = null;
                                 if (i > 0) {
                                     name = sat.substring(i);
                                     value = sat.substring(i, sat.length());
                                 } else {
                                     name = sat;
                                     value = null;
                                 }
                                 Set values = (Set) saprops.get(name);
                                 if (values == null) {
                                     values = new HashSet();
                                     saprops.put(name, values);
                                 }
                                 values.add(value);
                             }
                             sa.setProperties(saprops);
                             sa.setPResponseProviderName(nrpName);
                             resourceAttributesSet.add(sa);
                         }
                         Set uas = (Set) props.get(irp.DYNAMIC_ATTRIBUTE);
                         if (uas != null && !uas.isEmpty()) {
                             UserAttributes ua = new UserAttributes();
                             Map uaprops = new HashMap();
                             for (Object obj : uas) {
                                 String uat = (String) obj;
                                 int i = uat.indexOf("=");
                                 String name = null;
                                 String value = null;
                                 if (i > 0) {
                                     name = uat.substring(i);
                                     value = uat.substring(i, uat.length());
                                 } else {
                                     name = uat;
                                     value = null;
                                 }
                                 Set values = (Set) uaprops.get(name);
                                 if (values == null) {
                                     values = new HashSet();
                                     uaprops.put(name, values);
                                 }
                                 values.add(value);
                             }
                             ua.setProperties(uaprops);
                             ua.setPResponseProviderName(nrpName);
                             resourceAttributesSet.add(ua);
                         }
                     }
                 }
 
             }
         }
         return resourceAttributesSet;
     }
 
     private static List resourceAttributesToResponseProviders(
             Set<ResourceAttributes> resourceAttributes) throws PolicyException {
         List nrps = new ArrayList();
         if (resourceAttributes != null) {
             for (ResourceAttributes ra : resourceAttributes) {
                 if (ra instanceof StaticAttributes) {
                     StaticAttributes sa = (StaticAttributes) ra;
                     Object[] arr = new Object[2];
                     arr[0] = sa.getPResponseProviderName();
                     IDRepoResponseProvider rp = new IDRepoResponseProvider();
                     Map props = sa.getProperties();
                     if (props != null) {
                         Set newValues = new HashSet();
                         Set entrySet = props.entrySet();
                         for (Object entryObj : entrySet) {
                             Map.Entry entry = (Map.Entry) entryObj;
                             String name = (String) entry.getKey();
                             Set values = (Set) entry.getValue();
                             if (values != null && !values.isEmpty()) {
                                 for (Object valueObj : values) {
                                     String value = (String) valueObj;
                                     newValues.add(name + "=" + value);
                                 }
 
                             }
                             if (!newValues.isEmpty()) {
                                 Map newProps = new HashMap();
                                 newProps.put(rp.STATIC_ATTRIBUTE, newValues);
                                 rp.setProperties(newProps);
                             }
 
                         }
                         arr[1] = rp;
                         nrps.add(arr);
                     }
 
                 } else if (ra instanceof UserAttributes) {
                     UserAttributes ua = (UserAttributes) ra;
                     Object[] arr = new Object[2];
                     arr[0] = ua.getPResponseProviderName();
                     Map props = ua.getProperties();
                     IDRepoResponseProvider rp = new IDRepoResponseProvider();
                     if (props != null) {
                         Set newValues = new HashSet();
                         Set entrySet = props.entrySet();
                         for (Object entryObj : entrySet) {
                             Map.Entry entry = (Map.Entry) entryObj;
                             String name = (String) entry.getKey();
                             Set values = (Set) entry.getValue();
                             String value = null;
                             if (values != null && !values.isEmpty()) {
                                 value = (String) values.iterator().next();
                             }
 
                             String newValue = name;
                             if (value != null) {
                                 newValue = name + "=" + value;
 
                             }
 
                             newValues.add(newValue);
                             if (!newValues.isEmpty()) {
                                 Map newProps = new HashMap();
                                 newProps.put(rp.DYNAMIC_ATTRIBUTE, newValues);
                                 Map configParams = new HashMap();
                                 configParams.put(
                                         PolicyConfig.SELECTED_DYNAMIC_ATTRIBUTES,
                                         newValues);
                                 rp.initialize(configParams);
                                 rp.setProperties(newProps);
                             }
 
                         }
                     }
                     arr[1] = rp;
                     nrps.add(arr);
                 }
 
             }
         }
         return nrps;
     }
 
     private static String randomName() {
         return "" + random.nextInt(10000);
     }
 
     private static Set toSet(Object obj) {
         if (obj == null) {
             return null;
         }
 
         Set set = new HashSet();
         set.add(obj);
         return set;
     }
 
     static Map pravToPav(Map<String, Boolean> actionValues,
             String serviceName) throws PolicyException, SSOException  {
         if (actionValues == null) {
             return null;
         }
         ServiceType st = svcTypeManager.getServiceType(serviceName);
         Map av = new HashMap();
         Set<String> keySet = actionValues.keySet();
         for (String action : keySet) {
             try {
                 ActionSchema as = st.getActionSchema(action);
                 String trueValue = as.getTrueValue();
                 String falseValue = as.getFalseValue();
                 Boolean value = actionValues.get(action);
                 Set values = new HashSet();
                 if (value.equals(Boolean.TRUE)) {
                     values.add(trueValue);
                 } else {
                     values.add(falseValue);
                 }
                 av.put(action, values);
             } catch (InvalidNameException e) {
                 Boolean value = actionValues.get(action);
                 Set values = new HashSet();
                 values.add(value.toString());
                 av.put(action, values);
             }
         }
         return av;
     }
 
     static Map<String, Boolean> pavToPrav(Map actionValues,
             String serviceName) throws PolicyException, SSOException {
         if (actionValues == null) {
             return null;
         }
         ServiceType st = svcTypeManager.getServiceType(serviceName);
         Map av = new HashMap();
         Set keySet = (Set) actionValues.keySet();
         for (Object actionObj : keySet) {
             String action = (String) actionObj;
             Set values = (Set) actionValues.get(action);
             
             if ((values == null) || values.isEmpty()) {
                 av.put(action, Boolean.FALSE);
             } else {
                 try {
                     ActionSchema as = st.getActionSchema(action);
                     String trueValue = as.getTrueValue();
 
                     if (values.contains(trueValue)) {
                         av.put(action, Boolean.TRUE);
                     } else {
                         av.put(action, Boolean.FALSE);
                     }
                 } catch (InvalidNameException e) {
                     av.put(action, Boolean.parseBoolean(
                         (String)values.iterator().next()));
                 }
             }
 
         }
         return av;
     }
 }
