 /*
  * **********************************************************************
  * basicworkflows
  * %%
  * Copyright (C) 2012 - 2013 e-Spirit AG
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * **********************************************************************
  */
 
 package com.espirit.moddev.basicworkflows.release;
 
 import com.espirit.moddev.basicworkflows.util.FsException;
 import com.espirit.moddev.basicworkflows.util.FsLocale;
 import com.espirit.moddev.basicworkflows.util.ReferenceResult;
 import com.espirit.moddev.basicworkflows.util.WorkflowConstants;
 import de.espirit.common.base.Logging;
 import de.espirit.firstspirit.access.ReferenceEntry;
 import de.espirit.firstspirit.access.store.IDProvider;
 import de.espirit.firstspirit.access.store.StoreElement;
 import de.espirit.firstspirit.access.store.contentstore.Content2;
 import de.espirit.firstspirit.access.store.contentstore.ContentFolder;
 import de.espirit.firstspirit.access.store.contentstore.ContentWorkflowable;
 import de.espirit.firstspirit.access.store.globalstore.GCAFolder;
 import de.espirit.firstspirit.access.store.globalstore.GCAPage;
 import de.espirit.firstspirit.access.store.globalstore.ProjectProperties;
 import de.espirit.firstspirit.access.store.mediastore.Media;
 import de.espirit.firstspirit.access.store.mediastore.MediaFolder;
 import de.espirit.firstspirit.access.store.pagestore.*;
 import de.espirit.firstspirit.access.store.sitestore.DocumentGroup;
 import de.espirit.firstspirit.access.store.sitestore.PageRef;
 import de.espirit.firstspirit.access.store.sitestore.PageRefFolder;
 import de.espirit.firstspirit.access.store.templatestore.TemplateStoreElement;
 import de.espirit.firstspirit.access.store.templatestore.WorkflowScriptContext;
 import de.espirit.or.schema.Entity;
 
 import java.util.*;
 
 /**
  * This class provides methods to get the references of the workflow object and store them in the session.
  *
  * @author stephan
  * @since 1.0
  */
 public class WorkflowObject {
     /** The storeElement to use. */
     private StoreElement storeElement;
     /** The workflowScriptContext from the workflow. */
     private WorkflowScriptContext workflowScriptContext;
     /** The content2 object from the workflow. */
     private Content2 content2;
     /** The Entity to use. */
     private Entity entity;
     /** The ResourceBundle that contains language specific labels. */
     private ResourceBundle bundle;
     /** The logging class to use. */
     public static final Class<?> LOGGER = WorkflowObject.class;
 
     /**
      * Constructor for WorkflowObject.
      *
      * @param workflowScriptContext The workflowScriptContext from the workflow.
      */
     public WorkflowObject(WorkflowScriptContext workflowScriptContext) {
         this.workflowScriptContext = workflowScriptContext;
         ResourceBundle.clearCache();
         bundle = ResourceBundle.getBundle(WorkflowConstants.MESSAGES, new FsLocale(workflowScriptContext).get());
         if(workflowScriptContext.getWorkflowable() instanceof ContentWorkflowable) {
             content2 = ((ContentWorkflowable) workflowScriptContext.getWorkflowable()).getContent();
             entity = ((ContentWorkflowable) workflowScriptContext.getWorkflowable()).getEntity();
         } else {
             storeElement = (StoreElement) workflowScriptContext.getWorkflowable();
         }
     }
 
     /**
      * This method gets the referenced objects from the workflow object (StoreElement) that prevent the release.
      *
      * @param releaseWithMedia Determines if media references should also be checked
      * @return a list of elements that reference the workflow object.
      */
     public List<Object> getRefObjectsFromStoreElement(boolean releaseWithMedia) {
         ArrayList<Object>referencedObjects = new ArrayList<Object>();
 
         if (storeElement instanceof PageRef) {
             // add outgoing references
             referencedObjects.addAll(getReferences(storeElement, releaseWithMedia));
 
             // add outgoing references of referenced page if it is not released
             final Page page = ((PageRef) storeElement).getPage();
             if (page.getReleaseStatus() != Page.RELEASED) {
                 // add results
                 for(ReferenceEntry referenceEntry : page.getOutgoingReferences()) {
                     if(!releaseWithMedia) {
                         if(!referenceEntry.isType(ReferenceEntry.MEDIA_STORE_REFERENCE)) {
                             referencedObjects.add(referenceEntry);
                         }
                     } else {
                         referencedObjects.add(referenceEntry);
                     }
                 }
                 referencedObjects.addAll(getRefObjectsFromSection(page, releaseWithMedia));
             }
 
 
         } else if (storeElement instanceof PageRefFolder) {
             // add outgoing references
             referencedObjects.addAll(getReferences(storeElement, releaseWithMedia));
 
         } else if (storeElement instanceof GCAPage) {
             // add outgoing references
             referencedObjects.addAll(getReferences(storeElement, releaseWithMedia));
 
         } else if (storeElement instanceof Page) {
             // add outgoing references
             referencedObjects.addAll(getReferences(storeElement, releaseWithMedia));
             referencedObjects.addAll(getRefObjectsFromSection(storeElement, releaseWithMedia));
 
         } else if (storeElement instanceof PageFolder) {
             // add outgoing references
             referencedObjects.addAll(getReferences(storeElement, releaseWithMedia));
 
         } else if (storeElement instanceof DocumentGroup) {
             // add outgoing references
             referencedObjects.addAll(getReferences(storeElement, releaseWithMedia));
 
         } else if (storeElement instanceof Media) {
             // add outgoing references
             referencedObjects.addAll(getReferences(storeElement, releaseWithMedia));
 
         } else if (storeElement instanceof MediaFolder) {
             // add outgoing references
             referencedObjects.addAll(getReferences(storeElement, releaseWithMedia));
 
         } else if (storeElement instanceof GCAFolder) {
             // add outgoing references
             referencedObjects.addAll(getReferences(storeElement, releaseWithMedia));
 
         } else if (storeElement instanceof ProjectProperties) {
             // add outgoing references
             referencedObjects.addAll(getReferences(storeElement, releaseWithMedia));
 
         } else if (storeElement instanceof Content2) {
             //Element is a content2 object -> aborting"
             workflowScriptContext.gotoErrorState(bundle.getString("releaseC2notPossible"), new FsException());
 
         } else if (storeElement instanceof ContentFolder) {
             //Element is a content folder object -> aborting"
             workflowScriptContext.gotoErrorState(bundle.getString("releaseCFnotPossible"), new FsException());
 
         }
         return referencedObjects;
     }
 
     /**
      * This method gets the referenced objects from sections of a page that prevent the release.
      *
      * @param page The page where to check the sections
      * @param releaseWithMedia Determines if media references should also be checked
      * @return a list of elements that reference the workflow object.
      */
     public List<Object> getRefObjectsFromSection(StoreElement page, boolean releaseWithMedia) {
             ArrayList<Object>referencedObjects = new ArrayList<Object>();
 
             // add outgoing references of page sections
             for (Section section : page.getChildren(Section.class, true)) {
 /** documentation example - begin **/
                 if (!(section instanceof Content2Section)) {
 /** documentation example - end **/
                     for(ReferenceEntry referenceEntry : section.getOutgoingReferences()) {
                         if(!releaseWithMedia) {
                             if(!referenceEntry.isType(ReferenceEntry.MEDIA_STORE_REFERENCE)) {
                                 referencedObjects.add(referenceEntry);
                             }
                         } else {
                             referencedObjects.add(referenceEntry);
                         }
                     }
                 }
             }
         return referencedObjects;
     }
 
 
     /**
      * This method gets the referenced objects from the workflow object (Entity) that prevent the release.
      *
      * @param includeMedia Determines if media references should also be checked
      * @return a list of elements that reference the workflow object.
      */
     public List<Object> getRefObjectsFromEntity(boolean includeMedia) {
         ArrayList<Object> referencedObjects= new ArrayList<Object>();
 
         for(ReferenceEntry referenceEntry : content2.getSchema().getOutgoingReferences(entity)) {
             if(!includeMedia) {
                 if(!referenceEntry.isType(ReferenceEntry.MEDIA_STORE_REFERENCE)) {
                     referencedObjects.add(referenceEntry);
                 }
             } else {
                 referencedObjects.add(referenceEntry);
             }
         }
         return referencedObjects;
     }
 
     /**
      * Convenience method to check if element can be released according to defined rules.
      *
      * @param releaseObjects The list of objects to check.
      * @param releaseWithMedia Determines if media elements should be implicitly released.
      * @return true if successfull
      */
     @SuppressWarnings("unchecked")
     public boolean checkReferences(List<Object> releaseObjects, boolean releaseWithMedia) {
         boolean result = false;
         HashMap<String, IDProvider.UidType> notReleasedElements = new HashMap<String, IDProvider.UidType>();
         // object to store if elements can be released
         ReferenceResult referenceResult = new ReferenceResult();
 
         // itereate over references and check release rules
         for(Object object : releaseObjects) {
 
             if(object instanceof Entity  || object instanceof ReferenceEntry && ((ReferenceEntry) object).getReferencedObject() instanceof Entity) {
                 Entity ent;
                 if(object instanceof Entity) {
                     ent = (Entity) object;
                 } else {
                     ent = (Entity) ((ReferenceEntry) object).getReferencedObject();
                 }
                 referenceResult.setOnlyMedia(false);
                 // check if is no media and released
                 if(!ent.isReleased()) {
                     Logging.logWarning("No media and not released:" + ent.getIdentifier() + "#" + ent.get("fs_id"), LOGGER);
                     referenceResult.setNotMediaReleased(false);
                     referenceResult.setAllObjectsReleased(false);
                     notReleasedElements.put(ent.getIdentifier().getEntityTypeName() + " (" + ent.getIdentifier().getEntityTypeName() + ", ID#" + ent.get("fs_id") + ")", IDProvider.UidType.CONTENTSTORE_DATA);
                 }
             } else {
                 IDProvider idProvider;
                 if (object instanceof IDProvider) {
                     idProvider = (IDProvider) object;
                 } else {
                     idProvider = (IDProvider) ((ReferenceEntry) object).getReferencedObject();
                 }
 
 /** documentation example - begin **/
                 if(idProvider instanceof Section) {
                     idProvider = idProvider.getParent().getParent();
                 }
 /** documentation example - end **/
 
                 // check if current PAGE within PAGEREF-Release
                 boolean isCurrentPage = false;
                 if(idProvider instanceof Page && storeElement instanceof PageRef) {
                     Page page = (Page) idProvider;
                     Page curPage = ((PageRef) storeElement).getPage();
                     if(page.getId() == curPage.getId()) {
                         isCurrentPage = true;
                     }
                 }
 
                 // if not current PAGE within PAGEREF-Release or media and release with media is not checked
                if(!isCurrentPage && idProvider != null || (idProvider instanceof Media && !releaseWithMedia )) {
                     idProvider.refresh();
                     // check if only media is referenced except templates
                     if(!(idProvider instanceof Media) && !(idProvider instanceof TemplateStoreElement) && !(idProvider instanceof Content2)) {
                         Logging.logWarning("No media:" + idProvider.getUid(), LOGGER);
                         referenceResult.setOnlyMedia(false);
                         // check if is no media and not released
                         if(idProvider.getReleaseStatus() == IDProvider.NEVER_RELEASED) {
                             Logging.logWarning("No media, but never released:" + idProvider.getUid(), LOGGER);
                             referenceResult.setNotMediaReleased(false);
                             notReleasedElements.put(idProvider.getDisplayName(new FsLocale(workflowScriptContext).getLanguage()) + " (" + idProvider.getUid() + ", " + idProvider.getId() + ")", idProvider.getUidType());
                         }
                     }
                     // check if all references are released
                     if(!(idProvider instanceof TemplateStoreElement) && !(idProvider instanceof Content2) && idProvider.getReleaseStatus() == IDProvider.NEVER_RELEASED) {
                         Logging.logWarning("Never released:" + idProvider.getUid(), LOGGER);
                         referenceResult.setAllObjectsReleased(false);
                         notReleasedElements.put(idProvider.getDisplayName(new FsLocale(workflowScriptContext).getLanguage()) + " (" + idProvider.getUid() + ", " + idProvider.getId() + ")", idProvider.getUidType());
                     }
                 }
             }
         }
 
 
         // check result if can be released
         if(referenceResult.isOnlyMedia() && releaseWithMedia) {
             Logging.logWarning("Is only media and checked", LOGGER);
             result = true;
         } else if (referenceResult.isNotMediaReleased() && releaseWithMedia) {
             Logging.logWarning("All non media released and checked", LOGGER);
             result = true;
         } else if (referenceResult.isAllObjectsReleased() && !releaseWithMedia) {
             Logging.logWarning("Everything released and not checked", LOGGER);
             result = true;
         }
         // put not released elements to session for further use
         workflowScriptContext.getSession().put("wfNotReleasedElements", notReleasedElements);
         return result;
     }
 
     /**
      * Convenience method to get referenced objects of storeElement and its parents.
      *
      * @param storeEl The StoreElement to get references from.
      * @param releaseWithMedia Determines if media references should also be checked.
      * @return the list of referenced objects.
      */
     public List<Object> getReferences(StoreElement storeEl, boolean releaseWithMedia) {
         ArrayList<Object> references = new ArrayList<Object>();
         StoreElement storeElem = storeEl;
 
         // add outgoing references
         for(ReferenceEntry referenceEntry : storeElem.getOutgoingReferences()) {
             if(!releaseWithMedia) {
                 if(!referenceEntry.isType(ReferenceEntry.MEDIA_STORE_REFERENCE)) {
                     references.add(referenceEntry);
                 }
             } else {
                 references.add(referenceEntry);
             }
         }
 
         // add outgoing references of parent objects if element was never released before
         if(((IDProvider) storeElem).getReleaseStatus() == IDProvider.NEVER_RELEASED) {
             while(storeElem.getParent() != null) {
                 for(ReferenceEntry referenceEntry : storeElem.getOutgoingReferences()) {
                     if(!releaseWithMedia) {
                         if(!referenceEntry.isType(ReferenceEntry.MEDIA_STORE_REFERENCE)) {
                             references.add(referenceEntry);
                         }
                     } else {
                         references.add(referenceEntry);
                     }
                 }
                 storeElem = storeElem.getParent();
             }
         }
 
         return references;
     }
 
     /**
      * Convenience method to get an ID from an entity/storeElement.
      *
      * @return the ID.
      */
     public String getId() {
         if(storeElement != null) {
             return storeElement.getName();
         } else {
             return entity.getKeyValue().toString();
         }
     }
 }
