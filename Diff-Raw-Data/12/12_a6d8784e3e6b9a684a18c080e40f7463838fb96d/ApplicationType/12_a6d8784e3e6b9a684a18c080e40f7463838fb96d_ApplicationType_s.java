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
 * $Id: ApplicationType.java,v 1.5 2009-04-09 13:15:01 veiming Exp $
  */
 package com.sun.identity.entitlement;
 
 import com.sun.identity.entitlement.interfaces.ISaveIndex;
 import com.sun.identity.entitlement.interfaces.ISearchIndex;
 import com.sun.identity.entitlement.interfaces.ResourceName;
 import com.sun.identity.entitlement.util.ResourceNameIndexGenerator;
 import com.sun.identity.entitlement.util.ResourceNameSplitter;
 import java.util.Map;
 
 /**
  * Application Type defines the default supported action names; search and save
  * index generators; and resource comparator.
  */
 public class ApplicationType {
     private String name;
     private Map<String, Boolean> actions;
     private ISearchIndex searchIndex;
     private ISaveIndex saveIndex;
     private ResourceName resourceComp;
 
     /**
      * Constructs an instance.
      *
      * @param name Name of application type;
      * @param actions Supported action names.
      * @param searchIndex Search index generator.
      * @param saveIndex Save index generator.
      * @param resourceComp Resource comparator.
      */
     public ApplicationType(
         String name,
         Map<String, Boolean> actions,
         ISearchIndex searchIndex,
         ISaveIndex saveIndex,
         ResourceName resourceComp
     ) {
         this.name = name;
         this.actions = actions;
 
         if (searchIndex == null) {
             this.searchIndex = new ResourceNameSplitter();
         } else {
             this.searchIndex = searchIndex;
         }
         if (saveIndex == null) {
             this.saveIndex = new ResourceNameIndexGenerator();
         } else {
             this.saveIndex = saveIndex;
         }
         if (resourceComp == null) {
             this.resourceComp = new URLResourceName();
         } else {
             this.resourceComp = resourceComp;
         }
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
      * Returns supported action names and its default values.
      *
      * @return supported action names and its default values.
      */
     public Map<String, Boolean> getActions() {
         return actions;
     }
 
     /**
      * Set save index generator.
      *
      * @param saveIndex save index generator.
      */
     public void setSaveIndex(ISaveIndex saveIndex) {
         this.saveIndex = saveIndex;
     }
 
     /**
      * Set search index generator.
      *
      * @param searchIndex search index generator.
      */
     public void setSearchIndex(ISearchIndex searchIndex) {
         this.searchIndex = searchIndex;
     }
 
     /**
      * Returns search indexes for a give resource name.
      *
      * @param resource Resource for generating the indexes.
      * @return search indexes for a give resource name.
      */
     public ResourceSearchIndexes getResourceSearchIndex(String resource) {
         return searchIndex.getIndexes(resource);
     }
 
     /**
      * Returns save indexes for a give resource name.
      * 
      * @param resource Resource for generating the indexes.
      * @return save indexes for a give resource name.
      */
     public ResourceSaveIndexes getResourceSaveIndex(String resource) {
         return saveIndex.getIndexes(resource);
     }
 
     /**
      * Returns resource comparator.
      * 
      * @return resource comparator.
      */
     public ResourceName getResourceComparator() {
         return resourceComp;
     }
 
 }
