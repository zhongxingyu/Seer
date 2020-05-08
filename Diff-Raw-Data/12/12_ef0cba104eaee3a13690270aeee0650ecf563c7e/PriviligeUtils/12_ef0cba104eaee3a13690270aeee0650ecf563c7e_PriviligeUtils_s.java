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
 * $Id: PriviligeUtils.java,v 1.23 2009-03-20 01:10:15 dillidorai Exp $
  */
 package com.sun.identity.policy;
 
 import com.iplanet.sso.SSOToken;
 import com.iplanet.sso.SSOException;
 import com.sun.identity.entitlement.Entitlement;
 import com.sun.identity.entitlement.EntitlementCondition;
 import com.sun.identity.entitlement.EntitlementSubject;
 import com.sun.identity.entitlement.UserSubject;
 import com.sun.identity.entitlement.GroupSubject;
 import com.sun.identity.entitlement.IPCondition;
 import com.sun.identity.entitlement.RoleSubject;
 import com.sun.identity.entitlement.OrSubject;
 import com.sun.identity.entitlement.NotSubject;
 import com.sun.identity.entitlement.OrCondition;
 import com.sun.identity.entitlement.AndCondition;
 import com.sun.identity.entitlement.Privilige;
 import com.sun.identity.entitlement.ResourceAttributes;
import com.sun.identity.entitlement.StaticAttributes;
 import com.sun.identity.entitlement.TimeCondition;
import com.sun.identity.entitlement.UserAttributes;
 import com.sun.identity.idm.AMIdentity;
 import com.sun.identity.idm.IdType;
 import com.sun.identity.idm.IdRepoException;
 import com.sun.identity.idm.IdUtils;
 import com.sun.identity.policy.interfaces.Condition;
 import com.sun.identity.policy.interfaces.ResponseProvider;
 import com.sun.identity.policy.interfaces.Subject;
 import com.sun.identity.policy.plugins.IDRepoResponseProvider;
 import com.sun.identity.shared.debug.Debug;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 
 /**
  * Class with utility methods to map from
  * <code>com.sun.identity.entity.Privilige</code>
  * to
  * </code>com.sun.identity.policy.Policy</code>
  */
 public class PriviligeUtils {
 
     private static Random random = new Random();
 
     /**
      * Constructs PriviligeUtils
      */
     private PriviligeUtils() {
     }
 
     /**
      * Maps a OpenSSO Policy to entitlement Privilige
      * @param policy OpenSSO Policy object
      * @return entitlement Privilige object
      * @throws com.sun.identity.policy.PolicyException if the mapping fails
      */
     public static Privilige policyToPrivilige(Policy policy)
             throws PolicyException {
 
         if (policy == null) {
             return null;
         }
 
         String policyName = policy.getName();
 
         Set ruleNames = policy.getRuleNames();
         Set<Entitlement> entitlements = new HashSet<Entitlement>();
         for (Object ruleNameObj : ruleNames) {
             String ruleName = (String) ruleNameObj;
             Rule rule = policy.getRule(ruleName);
             Entitlement entitlement = ruleToEntitlement(rule);
             entitlements.add(entitlement);
         }
 
         Set subjectNames = policy.getSubjectNames();
         Set nqSubjects = new HashSet();
         for (Object subjectNameObj : subjectNames) {
             String subjectName = (String) subjectNameObj;
             Subject subject = policy.getSubject(subjectName);
             Set subjectValues = subject.getValues();
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
         for (Object rpNameObj : nrps) {
             String rpName = (String) rpNameObj;
             ResponseProvider rp = policy.getResponseProvider(rpName);
             Object[] nrp = new Object[2];
             nrp[0] = rpName;
             nrp[1] = rp;
             nrps.add(nrp);
         }
         Set<ResourceAttributes> resourceAttributesSet = nrpsToEResourceAttributesSet(nrps);
 
         Privilige privilige = new Privilige(policyName, entitlements, eSubject,
                 eCondition, resourceAttributesSet);
         return privilige;
     }
 
     private static Entitlement ruleToEntitlement(Rule rule)
             throws PolicyException {
         String entitlementName = rule.getName();
         String serviceName = rule.getServiceTypeName();
         String resourceName = rule.getResourceName();
         Set excludedResourceNames = rule.getExcludedResourceNames();
         Map<String, Object> actionMap = new HashMap<String, Object>();
         Set actionNames = rule.getActionNames();
         for (Object actionNameObj : actionNames) {
             String actionName = (String) actionNameObj;
             Set actionValues = rule.getActionValues(actionName);
             actionMap.put(actionName, actionValues);
         }
         Entitlement entitlement = new Entitlement(serviceName, resourceName,
                 actionMap);
         entitlement.setName(entitlementName);
         entitlement.setExcludedResourceNames(excludedResourceNames);
         return entitlement;
     }
 
     private static EntitlementSubject nqSubjectsToESubject(Set nqSubjects) {
         Set esSet = new HashSet();
         EntitlementSubject es = null;
         for (Object nqSubjectObj : nqSubjects) {
             Object[] nqSubject = (Object[]) nqSubjectObj;
             Set orSubjects = new HashSet();
             String subjectName = (String) nqSubject[0];
             Subject subject = (Subject) nqSubject[1];
             if (subject instanceof com.sun.identity.policy.plugins.AMIdentitySubject) {
                 es = mapAMIdentitySubjectToESubject(nqSubject);
             } else { // mapt to PolicyESubject
             }
             esSet.add(es);
         }
         if (esSet.size() == 1) {
             es = (EntitlementSubject) esSet.iterator().next();
         } else if (esSet.size() > 1) {
             es = new OrSubject(esSet);
         }
         return es;
     }
 
     private static EntitlementSubject mapAMIdentitySubjectToESubject(Object[] nqSubject) {
         Set esSet = new HashSet();
         EntitlementSubject es = null;
         String subjectName = (String) nqSubject[0];
         Subject subject = (Subject) nqSubject[1];
         Set values = subject.getValues();
         if (values == null || values.isEmpty()) {
             es = new UserSubject(null, subjectName);
             Boolean exclusive = (Boolean) nqSubject[2];
             if (exclusive) {
                 es = new NotSubject(es, subjectName);
             }
             return es;
         }
         for (Object valueObj : values) {
             String value = (String) valueObj;
             AMIdentity amIdentity = null;
             SSOToken ssoToken = null;
             amIdentity = null;
             IdType idType = null;
             try {
                 ssoToken = ServiceTypeManager.getSSOToken();
                 amIdentity = IdUtils.getIdentity(ssoToken, value);
                 if (amIdentity != null) {
                     idType = amIdentity.getType();
                 }
             } catch (SSOException ssoe) {
             } catch (IdRepoException idre) {
             }
             es = null;
             if (IdType.USER.equals(idType)) {
                 es = new UserSubject(value, subjectName);
             } else if (IdType.GROUP.equals(idType)) {
                 es = new GroupSubject(value, subjectName);
             } else if (IdType.ROLE.equals(idType)) {
                 es = new RoleSubject(value, subjectName);
             } else {
                 Debug debug = Debug.getInstance("Entitlement");
                 debug.error("PriviligeUtils.MapAMIdentitySubjectToESubject(); " + " unsupported IDType=" + idType);
             }
             if (es != null) {
                 esSet.add(es);
             }
         }
         es = null;
         if (esSet.size() == 1) {
             es = (EntitlementSubject) esSet.iterator().next();
         } else if (esSet.size() > 1) {
             es = new OrSubject(esSet, subjectName);
         }
         Boolean exclusive = (Boolean) nqSubject[2];
         if (exclusive) {
             es = new NotSubject(es, subjectName);
         }
         return es;
     }
 
     private static EntitlementCondition nConditionsToECondition(
             Set nConditons) {
         Set ecSet = new HashSet();
         EntitlementCondition ec = null;
         for (Object nConditionObj : nConditons) {
             Object[] nCondition = (Object[]) nConditionObj;
             String conditionName = (String) nCondition[0];
             Condition condition = (Condition) nCondition[1];
             if (condition instanceof com.sun.identity.policy.plugins.IPCondition) {
                 ec = mapIPPConditionToIPECondition(nCondition);
             } else if (condition instanceof com.sun.identity.policy.plugins.SimpleTimeCondition) {
                 ec = mapSimpleTimeConditionToTimeCondition(nCondition);
             } else { //TODO: map to generic eCondition
             }
             ecSet.add(ec);
         }
         if (ecSet.size() == 1) {
             ec = (EntitlementCondition) ecSet.iterator().next();
         } else if (ecSet.size() > 1) {
             ec = new OrCondition(ecSet);
         }
         return ec;
     }
 
     private static IPCondition mapIPPConditionToIPECondition(
             Object[] nCondition) {
         String pConditionName = (String) nCondition[0];
         com.sun.identity.policy.plugins.IPCondition pipc =
                 (com.sun.identity.policy.plugins.IPCondition) nCondition[1];
         Map props = pipc.getProperties();
         IPCondition ipc = new IPCondition(
                 getCpValue(props, pipc.DNS_NAME),
                 getCpValue(props, pipc.START_IP),
                 getCpValue(props, pipc.END_IP));
         ipc.setPConditionName(pConditionName);
         return ipc;
     }
 
     private static TimeCondition mapSimpleTimeConditionToTimeCondition(
             Object[] nCondition) {
         String pConditionName = (String) nCondition[0];
         com.sun.identity.policy.plugins.SimpleTimeCondition stc =
                 (com.sun.identity.policy.plugins.SimpleTimeCondition) nCondition[1];
         Map props = stc.getProperties();
         TimeCondition tc = new TimeCondition(
                 getCpValue(props, stc.START_TIME),
                 getCpValue(props, stc.END_TIME),
                 getCpValue(props, stc.START_DAY),
                 getCpValue(props, stc.END_DAY));
         tc.setStartDate(getCpValue(props, stc.START_DATE));
         tc.setEndDate(getCpValue(props, stc.END_DATE));
         tc.setEnforcementTimeZone(getCpValue(props, stc.ENFORCEMENT_TIME_ZONE));
         tc.setPConditionName(pConditionName);
         return tc;
     }
 
     private static String getCpValue(Map props, String name) {
         if (props == null || name == null) {
             return null;
         }
         Object valueObj = props.get(name);
         if (valueObj == null) {
             return null;
         }
         return valueObj.toString();
     }
 
     private static Set<ResourceAttributes> nrpsToEResourceAttributesSet(Set nprs) {
         //ResourceAttributes era = nrpsToEResourceAttributes(nrps);
         return null;
     }
 
     public static Policy priviligeToPolicy(Privilige privilige)
             throws PolicyException, SSOException {
         Policy policy = null;
         policy = new Policy(privilige.getName());
         if (privilige.getEntitlements() != null) {
             Set<Entitlement> entitlements = privilige.getEntitlements();
             for (Entitlement entitlement : entitlements) {
                 Rule rule = entitlementToRule(entitlement);
                 policy.addRule(rule);
             }
         }
         if (privilige.getSubject() != null) {
             List pSubjects = eSubjectToPSubjects(privilige.getSubject());
             for (Object obj : pSubjects) {
                 Object[] arr = (Object[]) obj;
                 String pSubjectName = (String) arr[0];
                 Subject subject = (Subject) arr[1];
                 Subject s = null;
                 try {
                     s = policy.getSubject(pSubjectName);
                 } catch (NameNotFoundException nnfe) {
                 }
                 if (s != null) {
                     Set values = s.getValues();
                     if (values == null) {
                         values = new HashSet();
                     }
                     values.addAll(subject.getValues());
                     s.setValues(values);
                 } else {
                     Boolean exclusive = (Boolean) arr[2];
                     policy.addSubject(pSubjectName, subject, exclusive);
                 }
             }
         }
         if (privilige.getCondition() != null) {
             List pConditions = eConditionToPConditions(privilige.getCondition());
             for (Object obj : pConditions) {
                 Object[] arr = (Object[]) obj;
                 String pConditionName = (String) arr[0];
                 Condition condition = (Condition) arr[1];
                 policy.addCondition(pConditionName, condition);
             }
         }
         if (privilige.getResourceAttributes() != null) {
             List nrps = resourceAttributesToResponseProviders(
                     privilige.getResourceAttributes());
             for (Object obj : nrps) {
                 Object[] arr = (Object[]) obj;
                 String pResponseProviderName = (String) arr[0];
                 ResponseProvider responseProvider = (ResponseProvider) arr[1];
                 policy.addResponseProvider(pResponseProviderName, responseProvider);
             }
         }
         return policy;
     }
 
     private static Rule entitlementToRule(Entitlement entitlement)
             throws PolicyException {
         String ruleName = entitlement.getName();
         String serviceName = entitlement.getServiceName();
         String resourceName = entitlement.getResourceName();
         Map actionValues = entitlement.getActionValues();
         Rule rule = new Rule(ruleName, serviceName, resourceName, actionValues);
         rule.setExcludedResourceNames(entitlement.getExcludedResourceNames());
         return rule;
     }
 
     private static List eSubjectToPSubjects(EntitlementSubject es)
             throws PolicyException, SSOException {
         List subjects = new ArrayList();
         if (es instanceof UserSubject) {
             subjects.add(userESubjectToPSubject((UserSubject) es));
         } else if (es instanceof GroupSubject) {
             subjects.add(groupESubjectToPSubject((GroupSubject) es));
         } else if (es instanceof RoleSubject) {
             subjects.add(roleESubjectToPSubject((RoleSubject) es));
         } else if (es instanceof PolicyESubject) {
             subjects.add(policyESubjectToPSubject((PolicyESubject) es));
         } else if (es instanceof NotSubject) {
             List list = notESubjectToPSubject((NotSubject) es);
             for (Object obj : list) {
                 subjects.add(obj);
             }
         } else if (es instanceof OrSubject) {
             List list = orESubjectToPSubject((OrSubject) es);
             for (Object obj : list) {
                 subjects.add(obj);
             }
         } else { // map to EntitlementSubject
 
             subjects.add(eSubjectToEntitlementSubject(es));
         }
         return subjects;
     }
 
     private static Object[] userESubjectToPSubject(UserSubject us)
             throws PolicyException, SSOException {
         Subject subject = new com.sun.identity.policy.plugins.AMIdentitySubject();
         Set<String> values = new HashSet<String>();
         values.add(us.getUser());
         subject.setValues(values);
         String pSubjectName = us.getPSubjectName();
         if (pSubjectName == null) {
             pSubjectName = "UserSubject" + randomName();
         }
         Object[] arr = new Object[3];
         arr[0] = pSubjectName;
         arr[1] = subject;
         arr[2] = false;
         return arr;
     }
 
     private static Object[] groupESubjectToPSubject(GroupSubject gs)
             throws PolicyException, SSOException {
         Subject subject = new com.sun.identity.policy.plugins.AMIdentitySubject();
         Set<String> values = new HashSet<String>();
         values.add(gs.getGroup());
         subject.setValues(values);
         String pSubjectName = gs.getPSubjectName();
         if (pSubjectName == null) {
             pSubjectName = "GroupSubject" + randomName();
         }
         Object[] arr = new Object[3];
         arr[0] = pSubjectName;
         arr[1] = subject;
         arr[2] = false;
         return arr;
     }
 
     private static Object[] roleESubjectToPSubject(RoleSubject rs)
             throws PolicyException, SSOException {
         Subject subject = new com.sun.identity.policy.plugins.AMIdentitySubject();
         Set<String> values = new HashSet<String>();
         values.add(rs.getRole());
         subject.setValues(values);
         String pSubjectName = rs.getPSubjectName();
         if (pSubjectName == null) {
             pSubjectName = "RoleSubject" + randomName();
         }
         Object[] arr = new Object[3];
         arr[0] = pSubjectName;
         arr[1] = subject;
         arr[2] = false;
         return arr;
     }
 
     private static Object[] policyESubjectToPSubject(PolicyESubject ps)
             throws PolicyException, SSOException {
         Subject subject = null; //stm.getSubject("AMIdentitySubject");
         Set<String> values = new HashSet<String>();
         /*
         values.add(rs.getRole());
         subject.setValues(values);
         String pSubjectName = rs.getPSubjectName();
         if (pSubjectName == null) {
         pSubjectName = randomName();
         }
          * */
         Object[] arr = new Object[3];
         //arr[0] = pSubjectName;
         arr[1] = subject;
         arr[2] = false;
         return arr;
     }
 
     private static List notESubjectToPSubject(NotSubject nos)
             throws PolicyException, SSOException {
         List list = new ArrayList();
         EntitlementSubject ns = nos.getESubject();
         if (ns instanceof OrSubject) {
             OrSubject ores = (OrSubject) ns;
             Set<EntitlementSubject> nested2Subjects = ores.getESubjects();
             if (nested2Subjects != null) {
                 for (EntitlementSubject es : nested2Subjects) {
                     if (es instanceof UserSubject) {
                         Object[] arr = userESubjectToPSubject(
                                 (UserSubject) es);
                         arr[2] = Boolean.TRUE;
                         list.add(arr);
                     } else if (es instanceof GroupSubject) {
                         Object[] arr = groupESubjectToPSubject(
                                 (GroupSubject) es);
                         arr[2] = Boolean.TRUE;
                         list.add(arr);
                     } else if (es instanceof RoleSubject) {
                         Object[] arr = roleESubjectToPSubject(
                                 (RoleSubject) es);
                         arr[2] = Boolean.TRUE;
                         list.add(arr);
                     } else { // map to EntitlementSubject
                         Object[] arr = eSubjectToEntitlementSubject(es);
                         arr[2] = Boolean.TRUE;
                         list.add(arr);
                     }
                 }
             }
         } else if (ns instanceof UserSubject) {
 
             Object[] arr = userESubjectToPSubject((UserSubject) ns);
             arr[2] = Boolean.TRUE;
             list.add(arr);
         } else if (ns instanceof GroupSubject) {
             Object[] arr = groupESubjectToPSubject((GroupSubject) ns);
             arr[2] = Boolean.TRUE;
             list.add(arr);
         } else if (ns instanceof RoleSubject) {
             Object[] arr = roleESubjectToPSubject((RoleSubject) ns);
             arr[2] = Boolean.TRUE;
             list.add(arr);
         } else { // map to EntitlementSubejct
             Object[] arr = eSubjectToEntitlementSubject(ns);
             arr[2] = Boolean.TRUE;
             list.add(arr);
         }
         return list;
     }
 
     private static List orESubjectToPSubject(
             OrSubject os) throws PolicyException, SSOException {
         List list = new ArrayList();
         Set nestedSubjects = os.getESubjects();
         if (nestedSubjects != null) {
             for (Object ns : nestedSubjects) {
                 if (ns instanceof UserSubject) {
                     list.add(userESubjectToPSubject((UserSubject) ns));
                 } else if (ns instanceof GroupSubject) {
                     list.add(groupESubjectToPSubject((GroupSubject) ns));
                 } else if (ns instanceof RoleSubject) {
                     list.add(roleESubjectToPSubject((RoleSubject) ns));
                 } else if (ns instanceof OrSubject) {
                     List list1 = orESubjectToPSubject((OrSubject) ns);
                     for (Object obj : list1) {
                         list.add(obj);
                     }
                 } else if (ns instanceof NotSubject) {
                     List list1 = notESubjectToPSubject((NotSubject) ns);
                     for (Object obj : list1) {
                         Object[] arr = (Object[]) obj;
                         arr[2] = Boolean.TRUE;
                         list.add(arr);
                     }
                 } else { // map to EntitlementSubejct
                     list.add(eSubjectToEntitlementSubject((EntitlementSubject) ns));
                 }
             }
         }
         return list;
     }
 
     private static Object[] eSubjectToEntitlementSubject(EntitlementSubject es)
             throws PolicyException, SSOException {
         return null;
     }
 
     private static List eConditionToPConditions(EntitlementCondition ec)
             throws PolicyException, SSOException {
         List conditions = new ArrayList();
         if (ec instanceof IPCondition) {
             Object[] ncondition = new Object[2];
             ncondition[0] = ((IPCondition) ec).getPConditionName();
             ncondition[1] = ipConditionToPCondition((IPCondition) ec);
             conditions.add(ncondition);
         } else if (ec instanceof TimeCondition) {
             Object[] ncondition = new Object[2];
             ncondition[0] = ((TimeCondition) ec).getPConditionName();
             ncondition[1] = timeConditionToPCondition((TimeCondition) ec);
             conditions.add(ncondition);
         } else if (ec instanceof OrCondition) {
             List list = orConditionToPCondition((OrCondition) ec);
             for (Object obj : list) {
                 conditions.add(obj);
             }
         } else if (ec instanceof AndCondition) {
             List list = andConditionToPCondition((AndCondition) ec);
             for (Object obj : list) {
                 conditions.add(obj);
             }
         } else { // map to EPCondition
 
             conditions.add(eConditionToEPCondition(ec));
         }
         return conditions;
     }
 
     private static Condition ipConditionToPCondition(IPCondition ipc)
             throws PolicyException, SSOException {
         com.sun.identity.policy.plugins.IPCondition ipCondition = new com.sun.identity.policy.plugins.IPCondition();
         Map props = new HashMap();
         if (ipc.getDomainNameMask() != null) {
             props.put(ipCondition.DNS_NAME, toSet(ipc.getDomainNameMask()));
         }
         if (ipc.getStartIp() != null) {
             props.put(ipCondition.START_IP, toSet(ipc.getStartIp()));
         }
         if (ipc.getEndIp() != null) {
             props.put(ipCondition.END_IP, toSet(ipc.getEndIp()));
         }
         ipCondition.setProperties(props);
         return ipCondition;
     }
 
     private static Condition timeConditionToPCondition(TimeCondition tc)
             throws PolicyException, SSOException {
         com.sun.identity.policy.plugins.SimpleTimeCondition stc = new com.sun.identity.policy.plugins.SimpleTimeCondition();
         Map props = new HashMap();
         if (tc.getStartTime() != null) {
             props.put(stc.START_TIME, toSet(tc.getStartTime()));
         }
         if (tc.getEndTime() != null) {
             props.put(stc.END_TIME, toSet(tc.getEndTime()));
         }
         if (tc.getStartDay() != null) {
             props.put(stc.START_DAY, toSet(tc.getStartDay()));
         }
         if (tc.getEndDay() != null) {
             props.put(stc.END_DAY, toSet(tc.getEndDay()));
         }
         if (tc.getStartDate() != null) {
             props.put(stc.START_DATE, toSet(tc.getStartDate()));
         }
         if (tc.getEndDate() != null) {
             props.put(stc.START_DATE, toSet(tc.getEndDate()));
         }
         if (tc.getEnforcementTimeZone() != null) {
             props.put(stc.ENFORCEMENT_TIME_ZONE, toSet(tc.getEnforcementTimeZone()));
         }
         stc.setProperties(props);
         return stc;
     }
 
     private static List orConditionToPCondition(OrCondition oc)
             throws PolicyException, SSOException {
         List list = new ArrayList();
         Set nestedConditions = oc.getEConditions();
         if (nestedConditions != null) {
             for (Object nc : nestedConditions) {
                 if (nc instanceof IPCondition) {
                     list.add(ipConditionToPCondition((IPCondition) nc));
                 } else if (nc instanceof TimeCondition) {
                     list.add(timeConditionToPCondition((TimeCondition) nc));
                 } else if (nc instanceof OrCondition) {
                     List list1 = orConditionToPCondition((OrCondition) nc);
                     for (Object obj : list1) {
                         list.add(obj);
                     }
                 } else if (nc instanceof TimeCondition) { //NotCondition) {
                     List list1 = orConditionToPCondition((OrCondition) nc);
                     for (Object obj : list1) {
                         Object[] arr = (Object[]) obj;
                         arr[2] = Boolean.TRUE;
                         list.add(arr);
                     }
                 } else { // map to EPCondition
                     //list.add(eConditionToEPCondition((EntitlementCondiiton) nc));
                 }
             }
         }
         return list;
     }
 
     private static List andConditionToPCondition(AndCondition ac)
             throws PolicyException, SSOException {
         List list = new ArrayList();
         Set nestedConditions = ac.getEConditions();
         if (nestedConditions != null) {
             for (Object nc : nestedConditions) {
                 if (nc instanceof IPCondition) {
                     list.add(ipConditionToPCondition((IPCondition) nc));
                 } else if (nc instanceof TimeCondition) {
                     list.add(timeConditionToPCondition((TimeCondition) nc));
                 } else if (nc instanceof OrCondition) {
                     List list1 = orConditionToPCondition((OrCondition) nc);
                     for (Object obj : list1) {
                         list.add(obj);
                     }
                 } else if (nc instanceof TimeCondition) { //NotCondition) {
                     List list1 = orConditionToPCondition((OrCondition) nc);
                     for (Object obj : list1) {
                         Object[] arr = (Object[]) obj;
                         arr[2] = Boolean.TRUE;
                         list.add(arr);
                     }
                 } else { // map to EPCondiiton
                     //list.add(eConditionToEPCondition((EntitlementCondiiton) nc));
                 }
             }
         }
         return list;
     }
 
     private static Condition eConditionToEPCondition(EntitlementCondition ec)
             throws PolicyException, SSOException {
         return null;
     }
 
     private static Condition eConditionToEPCondition(Condition tc)
             throws PolicyException, SSOException {
         return null;
     }
 
     private static Set<ResourceAttributes> responseProvidersToResourceAttributes() {
         return null;
     }
 
     private static List resourceAttributesToResponseProviders(
             Set<ResourceAttributes> resourceAttributes) throws PolicyException {
         List nrps = new ArrayList();
         if (resourceAttributes != null) {
             for (ResourceAttributes ra : resourceAttributes) {
                 if (ra instanceof StaticAttributes) {
                     StaticAttributes sa = (StaticAttributes)ra;
                     Object[] arr = new Object[2];
                     arr[0] = sa.getPResponseProviderName();
                     IDRepoResponseProvider rp = new IDRepoResponseProvider();
                     Map props = sa.getProperties();
                     if (props != null) {
                         Set newValues = new HashSet();
                         Set entrySet = props.entrySet();
                         for (Object entryObj : entrySet) {
                             Map.Entry entry = (Map.Entry)entryObj;
                             String name = (String)entry.getKey();
                             Set values = (Set)entry.getValue();
                             if (values != null && !values.isEmpty()) {
                                 for (Object valueObj : values) {
                                    String value = (String)valueObj;
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
                     UserAttributes ua = (UserAttributes)ra;
                     Object[] arr = new Object[2];
                     arr[0] = ua.getPResponseProviderName();
                     Map props = ua.getProperties();
                     IDRepoResponseProvider rp = new IDRepoResponseProvider();
                                   if (props != null) {
                         Set newValues = new HashSet();
                         Set entrySet = props.entrySet();
                         for (Object entryObj : entrySet) {
                             Map.Entry entry = (Map.Entry)entryObj;
                             String name = (String)entry.getKey();
                             Set values = (Set)entry.getValue();
                             String value = null;
                             if (values != null && !values.isEmpty()) {
                                 value = (String)values.iterator().next();
                             }
                             String newValue = name;
                             if (value != null) {
                                 newValue = name + "=" + value;
 
                             }
                             newValues.add(newValue);
                             if (!newValues.isEmpty()) {
                                 Map newProps = new HashMap();
                                 newProps.put(rp.DYNAMIC_ATTRIBUTE, newValues);
                                 rp.setProperties(newProps);
                                 Map configParams = new HashMap();
                                 configParams.put(
                                         PolicyConfig.SELECTED_DYNAMIC_ATTRIBUTES,
                                         newValues);
                                 rp.initialize(configParams);
                             }
                         }
                     }
                     arr[1] = rp;
                     Map configParams = new HashMap();
 
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
 }
