 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2009 Sun Microsystems Inc. All Rights Reserved
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
 * $Id: PolicyEvaluatorAdaptor.java,v 1.5 2009-01-24 02:19:53 veiming Exp $
  */
 
 package com.sun.identity.policy;
 
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.iplanet.sso.SSOTokenManager;
 import com.sun.identity.entitlement.Entitlement;
 import com.sun.identity.entitlement.EntitlementException;
 import com.sun.identity.entitlement.IPolicyEvaluator;
 import com.sun.identity.entitlement.util.ResourceNameSplitter;
import java.security.Principal;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import javax.security.auth.Subject;
 
 public class PolicyEvaluatorAdaptor implements IPolicyEvaluator {
 
     private Set<Policy> search(Subject adminSubject, String resourceName)
         throws SSOException, PolicyException {
         PolicyManager pm = new PolicyManager(getSSOToken(adminSubject), "/");
         return PolicyIndexer.search(pm,
             ResourceNameSplitter.splitHost(resourceName),
             ResourceNameSplitter.splitPath(resourceName));
     }
     
     /**
      * Returns <code>true</code> if the subject is granted to an
      * entitlement.
      *
      * @param adminubject Subject for performing the evaluation.
      * @param subject Subject who is under evaluation.
      * @param serviceTypeName Application type.
      * @param entitlement Entitlement object which describes the resource name 
      *        and actions.
      * @param envParameters Map of environment parameters.
      * @return <code>true</code> if the subject is granted to an
      *         entitlement.
      * @throws EntitlementException if the result cannot be determined.
      */
     public boolean hasEntitlement(
         Subject adminSubject,
         Subject subject,
         String serviceTypeName,
         Entitlement entitlement,
         Map<String, Set<String>> envParameters
     ) throws EntitlementException {
         try {
             String resourceName = entitlement.getResourceName();
             Set<Policy> policies = search(adminSubject, resourceName);
             if ((policies == null) || policies.isEmpty()) {
                 return false;
             }
 
             Map<String, Object> actionValues = entitlement.getActionValues();
             SSOToken ssoToken = getSSOToken(subject);
             Set<PolicyDecision> policyDecisions = new HashSet<PolicyDecision>();
             for (Policy p : policies) {
                 PolicyDecision pd =  p.getPolicyDecision(
                     ssoToken, serviceTypeName, resourceName, 
                     actionValues.keySet(), envParameters);
                 if (pd != null) {
                    //TOFIX: Check can return if false
                     policyDecisions.add(pd);
                 }
             }
             
             if ((policyDecisions == null) || policyDecisions.isEmpty()) {
                 return false;
             }
             
             ServiceTypeManager stm = ServiceTypeManager.getServiceTypeManager();
             ServiceType serviceType = stm.getServiceType(serviceTypeName);
             PolicyDecision pd = mergePolicyDecisions(
                 policyDecisions, serviceType);
             return doesActionDecisionMatch(pd, actionValues);
         } catch (SSOException e) {
             throw new EntitlementException(e.getMessage(), -1);
         } catch (PolicyException e) {
             throw new EntitlementException(e.getMessage(), -1);
         }
     }
     
     private boolean doesActionDecisionMatch(
         PolicyDecision pd, 
         Map<String, Object> actionValues
     ) {
         Map<String, ActionDecision> decisionsMap = pd.getActionDecisions();
         if (decisionsMap != null) {
             for (String actionName : actionValues.keySet()) {
                 Object expected = actionValues.get(actionName);
                 ActionDecision decision = decisionsMap.get(actionName);
                 if (decision == null) {
                     return false;
                 }
                 Set values = decision.getValues();
                 if ((values == null) || !values.equals(expected)) {
                     return false;
                 }
             }
             return true;
         } else {
             return false;
         }
     }
     
     private SSOToken getSSOToken(Subject subject)
         throws SSOException {
        Set<Principal> principals = subject.getPrincipals();
         if (!principals.isEmpty()) {
             try {
                Principal p = principals.iterator().next();
                String tokenId = p.getName();
                SSOTokenManager mgr = SSOTokenManager.getInstance();
                return mgr.createSSOToken(tokenId);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         return null;
     }
     
     /**
      * Returns list of entitlements granted to a subject.
      *
      * @param adminubject Subject for performing the evaluation.
      * @param subject Subject who is under evaluation.
      * @param serviceTypeName Application type.
      * @param entitlement Entitlement object which describes the resource name 
      *        and actions.
      * @param envParameters Map of environment parameters.
      * @param recursive <code>true</code> to perform evaluation on sub resources
      *        from the given resource name.
      * @return list of entitlements granted to a subject.
      * @throws EntitlementException if the result cannot be determined.
      */
     public List<Entitlement> getEntitlements(
         Subject adminSubject,
         Subject subject,
         String serviceTypeName,
         String resourceName,
         Map<String, Set<String>> envParameters,
         boolean recursive
     ) throws EntitlementException {
         if (!recursive) {
             return getEntitlements(adminSubject, subject, serviceTypeName,
                 resourceName, envParameters);
         }
         return Collections.EMPTY_LIST;
     }
         
     /**
      * Returns list of entitlements granted to a subject.
      *
      * @param adminubject Subject for performing the evaluation.
      * @param subject Subject who is under evaluation.
      * @param serviceTypeName Application type.
      * @param entitlement Entitlement object which describes the resource name 
      *        and actions.
      * @param envParameters Map of environment parameters.
      * @return list of entitlements granted to a subject.
      * @throws EntitlementException if the result cannot be determined.
      */
     public List<Entitlement> getEntitlements(
         Subject adminSubject,
         Subject subject,
         String serviceTypeName,
         String resourceName,
         Map<String, Set<String>> envParameters
     ) throws EntitlementException {  
         try {
             Set<Policy> policies = search(adminSubject, resourceName);
             if ((policies == null) || policies.isEmpty()) {
                 return Collections.EMPTY_LIST;
             }
             
             ServiceType serviceType = 
                 ServiceTypeManager.getServiceTypeManager().getServiceType(
                     serviceTypeName);
             Set<String> actionNames = serviceType.getActionNames();
             SSOToken ssoToken = getSSOToken(subject);
             Set<PolicyDecision> policyDecisions = new HashSet<PolicyDecision>();
             for (Policy p : policies) {
                 PolicyDecision pd =  p.getPolicyDecision(
                     ssoToken, serviceTypeName, resourceName, 
                     actionNames, envParameters);
                 if (pd != null) {
                     policyDecisions.add(pd);
                 }
             }
             
             if ((policyDecisions == null) || policyDecisions.isEmpty()) {
                 return Collections.EMPTY_LIST;
             }
             
             PolicyDecision pd = mergePolicyDecisions(policyDecisions, 
                 serviceType);
             return getEntitlement(serviceTypeName, resourceName, pd);
         } catch (SSOException e) {
             throw new EntitlementException(e.getMessage(), -1);
         } catch (PolicyException e) {
             throw new EntitlementException(e.getMessage(), -1);
         }            
     }
     
     static List<Entitlement> getEntitlement(
         String serviceTypeName,
         String resourceName, 
         PolicyDecision pd
     ) {
         List<Entitlement> entitlements = new ArrayList<Entitlement>(); 
         Map<String, ActionDecision> actionDecisions = pd.getActionDecisions();
             
         for (String name : actionDecisions.keySet()) {
             ActionDecision ad = actionDecisions.get(name);
             Map<String, Object> actionValues = new HashMap<String, Object>();
             actionValues.put(ad.getActionName(), ad.getValues());
             long timeToLove = ad.getTimeToLive(); // TOFIX
             Entitlement entitlement = new Entitlement(serviceTypeName, 
                 resourceName, actionValues);
             entitlement.setAdvices(ad.getAdvices());
             entitlements.add(entitlement);
         }
         return entitlements;        
     }
     
     static PolicyDecision mergePolicyDecisions(
         Set<PolicyDecision> policyDecisions,
         ServiceType serviceType
   ) {
         PolicyDecision result = null;
         for (PolicyDecision pd : policyDecisions) {
             if (result == null) {
                 result = pd;
             } else {
                 result = PolicyEvaluator.mergePolicyDecisions(
                     serviceType, pd, result);
             }
         }
         return result;
     }
 }
