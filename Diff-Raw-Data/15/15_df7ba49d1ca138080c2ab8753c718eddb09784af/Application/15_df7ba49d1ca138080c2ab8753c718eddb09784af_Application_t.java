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
 * $Id: Application.java,v 1.15 2009-05-13 21:55:29 veiming Exp $
  */
 
 package com.sun.identity.entitlement;
 
 import com.sun.identity.entitlement.interfaces.ISaveIndex;
 import com.sun.identity.entitlement.interfaces.ISearchIndex;
 import com.sun.identity.entitlement.interfaces.ResourceName;
 import com.sun.identity.policy.ResourceMatch;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Application class contains the information on how an application behaves
  * e.g. how to combine decision and how to compare resources;
  * and the supported actions.
  */
 public class Application {
     private String name;
     private ApplicationType applicationType;
     private Map<String, Boolean> actions;
     private Set<String> conditions;
     private Set<String> resources;
     private Class entitlementCombiner;
     private ISearchIndex searchIndex;
     private ISaveIndex saveIndex;
     private ResourceName resourceComparator;
     private Set<String> attributeNames;
 
     /**
      * Constructs an instance.
      *
      * @param name Name of Application.
      * @param applicationType Its application type.
      */
     public Application(String name, ApplicationType applicationType) {
         this.name = name;
         this.applicationType = applicationType;
     }
 
     /**
      * Returns a set of supported actions and its default value.
      *
      * @return set of supported actions and its default value.
      */
     public Map<String, Boolean> getActions() {
         Map<String, Boolean> results = new HashMap<String, Boolean>();
         if (applicationType.getActions() != null) {
             results.putAll(applicationType.getActions());
         }
         if (actions != null) {
             results.putAll(actions);
         }
         return results;
     }
 
     /**
      * Returns application type.
      *
      * @return application type.
      */
     public ApplicationType getApplicationType() {
         return applicationType;
     }
 
     /**
      * Returns set of supported condition class names.
      *
      * @return set of supported condition class names.
      */
     public Set<String> getConditions() {
         return conditions;
     }
 
     /**
      * Returns a new instance of entitlement combiner.
      *
      * @return an instance of entitlement combiner.
      */
     public EntitlementCombiner getEntitlementCombiner() {
         try {
             return (EntitlementCombiner)entitlementCombiner.newInstance();
         } catch (InstantiationException ex) {
             PrivilegeManager.debug.error("Application.getEntitlementCombiner",
                 ex);
         } catch (IllegalAccessException ex) {
             PrivilegeManager.debug.error("Application.getEntitlementCombiner",
                 ex);
         }
         return null;
     }
 
     /**
      * Returns application name.
      *
      * @return application name.
      */
     public String getName() {
         return name;
     }
 
     /**
      * Sets supported action names and its default values.
      *
      * @param actions Set of supported action names and its default values.
      */
     public void setActions(Map<String, Boolean> actions) {
         this.actions = actions;
     }
 
     /**
      * Sets supported condition class names.
      *
      * @param conditions Supported condition class names.
      */
     public void setConditions(Set<String> conditions) {
         this.conditions = conditions;
     }
 
     /**
      * Sets save index.
      *
      * @param saveIndex save index.
      */
     public void setSaveIndex(ISaveIndex saveIndex) {
         this.saveIndex = saveIndex;
     }
 
     /**
      * Sets search index generator.
      *
      * @param searchIndex search index generator.
      */
     public void setSearchIndex(ISearchIndex searchIndex) {
         this.searchIndex = searchIndex;
     }
 
     /**
      * Sets resource names.
      *
      * @param resources resource names
      */
     public void setResources(Set<String> resources) {
         this.resources = new HashSet<String>();
         this.resources.addAll(resources);
     }
 
     /**
      * Adds resource names.
      *
      * @param resources resource names to be added.
      */
     public void addResources(Set<String> resources) {
         if (this.resources == null) {
             this.resources = new HashSet<String>();
         }
         this.resources.addAll(resources);
 
     }
 
     /**
      * Removes resource names.
      *
      * @param resources resource names to be removed.
      */
     public void removeResources(Set<String> resources) {
         if (this.resources != null) {
             this.resources.removeAll(resources);
         }
     }
 
     /**
      * Sets entitlement combiner.
      *
      * @param entitlementCombiner entitlement combiner.
      */
     public void setEntitlementCombiner(Class entitlementCombiner){
         this.entitlementCombiner = entitlementCombiner;
     }
 
     /**
      * Sets resource comparator.
      *
      * @param resourceComparator resource comparator.
      */
     public void setResourceComparator(ResourceName resourceComparator) {
         this.resourceComparator = resourceComparator;
     }
 
     /**
      * Returns set of resource names.
      *
      * @return set of resource names.
      */
     public Set<String> getResources() {
         return resources;
     }
 
     /**
      * Returns search indexes for a given resource.
      *
      * @param resource resource to generate the indexes.
      * @return search indexes.
      */
     public ResourceSearchIndexes getResourceSearchIndex(
             String resource) {
         return (searchIndex == null) ?
             applicationType.getResourceSearchIndex(resource) :
             searchIndex.getIndexes(resource);
     }
 
     /**
      * Returns save indexes for a given resource.
      * 
      * @param resource resource to generate the indexes.
      * @return save indexes.
      */
     public ResourceSaveIndexes getResourceSaveIndex(String resource) {
         return (saveIndex == null) ?
             applicationType.getResourceSaveIndex(resource) :
             saveIndex.getIndexes(resource);
     }
 
     /**
      * Returns resource comparator.
      * 
      * @return resource comparator.
      */
     public ResourceName getResourceComparator() {
         return (resourceComparator == null) ?
             applicationType.getResourceComparator() : resourceComparator;
     }
 
     /**
      * Adds a new action with its default value.
      *
      * @param name Action name.
      * @param val Default value.
      * @throws EntitlementException if action cannot be added.
      */
     public void addAction(String name, boolean val)
         throws EntitlementException {
         //TOFIX realm
         EntitlementConfiguration ec = EntitlementConfiguration.getInstance("/");
         ec.addApplicationAction(this.name, name, val);
     }
 
     /**
      * Sets attribute names.
      *
      * @param names Attribute names.
      */
     public void setAttributeNames(Set<String> names) {
         attributeNames = new HashSet<String>();
         if (names != null) {
             attributeNames.addAll(names);
         }
     }
 
     /**
      * Returns save index
      *
      * @return save index
      */
     public ISaveIndex getSaveIndex() {
         return saveIndex;
     }
 
     /**
      * Returns search index
      *
      * @return search index
      */
     public ISearchIndex getSearchIndex() {
         return searchIndex;
     }
 
     /**
      * Return attribute names.
      *
      * @return attribute names.
      */
     public Set<String> getAttributeNames() {
         return attributeNames;
     }
 
     /**
      * Returns the results of validating resource name.
      *
      * @param resource Resource to be validated.
      * @return the results of validating resource name.
      */
     public ValidateResourceResult validateResourceName(String resource) {
         ResourceName resComp = getResourceComparator();
         boolean match = false;
 
         if ((resources != null) && !resources.isEmpty()) {
             for (String r : resources) {
                 ResourceMatch rm = resComp.compare(resource, r, true);
                 if (rm.equals(ResourceMatch.EXACT_MATCH) ||
                     rm.equals(ResourceMatch.SUB_RESOURCE_MATCH) ||
                     rm.equals(ResourceMatch.WILDCARD_MATCH)
                 ) {
                     match = true;
                     break;
                 }
             }
         }
 
         if (!match) {
             return new ValidateResourceResult(
                ValidateResourceResult.VALID_CODE_DOES_NOT_MATCH_VALID_RESOURCES,
                 "");
         }
 
         try {
             resComp.canonicalize(resource);
             return new ValidateResourceResult( 
                 ValidateResourceResult.VALID_CODE_VALID, "");
         } catch (EntitlementException ex) {
             return new ValidateResourceResult(
                 ValidateResourceResult.VALID_CODE_INVALID, ex.getMessage());
         }
     }
 }
