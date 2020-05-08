 /*
  * <License>
  * The Apache Software License
  *
  * Copyright (c) 2002 lenya. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice, this
  *    list of conditions and the following disclaimer in the documentation and/or
  *    other materials provided with the distribution.
  *
  * 3. All advertising materials mentioning features or use of this software must
  *    display the following acknowledgment: "This product includes software developed
  *    by lenya (http://www.lenya.org)"
  *
  * 4. The name "lenya" must not be used to endorse or promote products derived from
  *    this software without prior written permission. For written permission, please
  *    contact contact@lenya.org
  *
  * 5. Products derived from this software may not be called "lenya" nor may "lenya"
  *    appear in their names without prior written permission of lenya.
  *
  * 6. Redistributions of any form whatsoever must retain the following acknowledgment:
  *    "This product includes software developed by lenya (http://www.lenya.org)"
  *
  * THIS SOFTWARE IS PROVIDED BY lenya "AS IS" WITHOUT ANY WARRANTY EXPRESS OR IMPLIED,
  * INCLUDING THE WARRANTY OF NON-INFRINGEMENT AND THE IMPLIED WARRANTIES OF MERCHANTI-
  * BILITY AND FITNESS FOR A PARTICULAR PURPOSE. lenya WILL NOT BE LIABLE FOR ANY DAMAGES
  * SUFFERED BY YOU AS A RESULT OF USING THIS SOFTWARE. IN NO EVENT WILL lenya BE LIABLE
  * FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR LOST PROFITS EVEN IF lenya HAS
  * BEEN ADVISED OF THE POSSIBILITY OF THEIR OCCURRENCE. lenya WILL NOT BE LIABLE FOR ANY
  * THIRD PARTY CLAIMS AGAINST YOU.
  *
  * Lenya includes software developed by the Apache Software Foundation, W3C,
  * DOM4J Project, BitfluxEditor and Xopus.
  * </License>
  */
 package ch.unizh.lenya.cms.task;
 
 import java.io.IOException;
 import java.util.Arrays;
 
 import org.apache.avalon.framework.parameters.ParameterException;
 import org.apache.lenya.cms.publication.Document;
 import org.apache.lenya.cms.publication.DocumentException;
 import org.apache.lenya.cms.publication.Publication;
 import org.apache.lenya.cms.publication.PublicationException;
 import org.apache.lenya.cms.publication.PublicationWrapper;
 import org.apache.lenya.cms.publication.Resource;
 import org.apache.lenya.cms.publication.ResourcesManager;
 import org.apache.lenya.cms.publication.SiteTreeException;
 import org.apache.lenya.cms.publication.Version;
 import org.apache.lenya.cms.publication.util.AreaFilter;
 import org.apache.lenya.cms.publication.util.ComposedFilter;
 import org.apache.lenya.cms.publication.util.InvertingFilter;
 import org.apache.lenya.cms.publication.util.LanguageFilter;
 import org.apache.lenya.cms.site.SiteException;
 import org.apache.lenya.cms.site.SiteManager;
 import org.apache.lenya.cms.task.ExecutionException;
 import org.apache.lenya.workflow.WorkflowException;
 import org.apache.log4j.Category;
 
 import ch.unizh.lenya.util.CacheHandler;
 
 /**
  * Deactivate a document.
  * 
  * @author <a href="mailto:andreas@apache.org">Andreas Hartmann </a>
  */
 public class Deactivate extends ResourceTask {
 
     private static final Category log = Category.getInstance(Deactivate.class);
 
     public static final String PARAMETER_LANGUAGE = "document-language";
     public static final String TASK_NAME = "deactivate";
 
     /**
      * @see org.apache.lenya.cms.task.Task#execute(java.lang.String)
      */
     public void execute(String servletContextPath) throws ExecutionException {
         if (log.isDebugEnabled()) {
             log.debug("Starting deactivation");
         }
 
         try {
             Resource resource = getResource();
 
             if (!checkPreconditions(resource)) {
                 setResult(FAILURE);
             } else {
                 if (log.isDebugEnabled()) {
                     log.debug("Can execute task: last label, children are published.");
                 }
                 deactivate(resource);
                 setResult(SUCCESS);
             }
 
         } catch (ExecutionException e) {
             throw e;
         } catch (Exception e) {
             throw new ExecutionException(e);
         }
 
     }
 
     /**
      * Checks if the preconditions are complied.
      * @param liveDocument The document to deactivate.
      * @return A boolean value.
      * @throws PublicationException when something went wrong.
      * @throws ExecutionException when something went wrong.
      * @throws DocumentException when something went wrong.
      */
     protected boolean checkPreconditions(Resource resource) throws PublicationException,
             DocumentException, SiteTreeException, ParameterException, ExecutionException {
         boolean OK = true;
 
         OK = OK && canWorkflowFire(resource);
         OK = OK && canDeleteLiveVersion(resource);
 
         return OK;
     }
 
     /**
      * Checks if the workflow can fire.
      * @param resource
      * @return
      * @throws PublicationException
      */
     protected boolean canWorkflowFire(Resource resource) throws PublicationException {
         boolean OK = true;
         try {
             ComposedFilter filter = new ComposedFilter();
             filter.add(new AreaFilter(Publication.AUTHORING_AREA));
             filter.add(new LanguageFilter(getLanguage()));
             Version authoringVersion = resource.getVersions(filter)[0];
 
             if (!authoringVersion.canWorkflowFire(getEventName(), getSituation())) {
                 OK = false;
                 if (log.isDebugEnabled()) {
                     log.debug("Cannot execute task: workflow event [" + getEventName()
                             + "] not supported.");
                 }
             }
         } catch (Exception e) {
             throw new PublicationException(e);
         }
         return OK;
     }
 
     /**
      * Checks if the live version can be
      * @param resource
      * @return
      * @throws PublicationException
      * @throws SiteException
      * @throws ParameterException
      * @throws ExecutionException
      */
     protected boolean canDeleteLiveVersion(Resource resource) throws PublicationException,
             SiteException, ParameterException, ExecutionException {
         PublicationWrapper wrapper = resource.getPublicationWrapper();
         SiteManager manager = wrapper.getSiteManager();
         Resource[] descendants = manager.getRequiringResources(resource, Publication.LIVE_AREA);
 
         boolean OK = true;
 
         if (descendants.length > 0) {
             ComposedFilter filter = new ComposedFilter();
             filter.add(new AreaFilter(Publication.LIVE_AREA));
             filter.add(new InvertingFilter(new LanguageFilter(getLanguage())));
             Version[] otherLanguages = resource.getVersions(filter);
 
             if (otherLanguages.length == 0) {
                 if (log.isDebugEnabled()) {
                     log.debug("Cannot delete last language version of resource [" + resource
                             + "] because this node has children.");
                 }
                 OK = false;
             }
         }
         return OK;
     }
 
     /**
      * Deactivates a resource.
      * @param resource The resource.
      * @throws ParameterException if an error occurs.
      * @throws DocumentException if an error occurs.
      * @throws PublicationException if an error occurs.
      * @throws ExecutionException if an error occurs.
      * @throws IOException if an error occurs.
      * @throws WorkflowException if an error occurs.
      */
     protected void deactivate(Resource resource) throws ParameterException, DocumentException,
             PublicationException, ExecutionException, IOException, WorkflowException {
         deactivate(resource, getLanguage(), null, 1);
     }
 
     /**
      * Deactivates a resource.
      * @param liveDocument The resource.
      */
    protected void deactivate(Resource resource, String language, Resource rootNode, int numOfDocumentsToDeactivate) throws PublicationException,
             ExecutionException, IOException, ParameterException, WorkflowException,
             DocumentException {
 
        // delete the cache but only if the last entry will be deactivated
        try{ 
            if (rootNode != null && numOfDocumentsToDeactivate == 1) {
                Version treeVersion = getVersion(rootNode, Publication.LIVE_AREA, getLanguage());
                CacheHandler cacheHandler = new CacheHandler();
                cacheHandler.deleteCache(treeVersion, true, TASK_NAME);
            }
        } catch (SiteTreeException e) {
            throw new ExecutionException ("Unable to get sitetree.", e);
        }
        
         Version authoringVersion = getVersion(resource, Publication.AUTHORING_AREA, language);
         if (authoringVersion.canWorkflowFire(getEventName(), getSituation())) {
             Version liveVersion = getVersion(resource, Publication.LIVE_AREA, language);
             
             if (log.isDebugEnabled()) {
                 log.debug("Deleting live version [" + liveVersion + "]");
             }
             
             // Delete cache entry first because CacheHandler needs the respective node in the sitetree
             try {
                 if (rootNode == null) {
                     CacheHandler cacheHandler = new CacheHandler();
                     cacheHandler.deleteCache(liveVersion, true, TASK_NAME);
                 }
             } catch (SiteTreeException e) {
                 throw new ExecutionException ("Unable to get sitetree.", e);
             }
             
             liveVersion.delete();
 
             Document liveDocument = liveVersion.getDocument();
             if (!liveDocument.existsInAnyLanguage()) {
                 ResourcesManager resourcesManager = new ResourcesManager(liveDocument);
                 resourcesManager.deleteResources();
             }
 
             authoringVersion.triggerWorkflow(getEventName(), getSituation());
             
         }
 
     }
 
     /**
      * Returns the language.
      * @return A string.
      * @throws ParameterException when the parameter is not present.
      * @throws ExecutionException when the language is not valid.
      */
     protected String getLanguage() throws ParameterException, ExecutionException {
         String language = getParameters().getParameter(PARAMETER_LANGUAGE);
 
         String[] languages = getPublication().getLanguages();
         if (!Arrays.asList(languages).contains(language)) {
             throw new ExecutionException("The language [" + language + "] is not valid!");
         }
 
         return language;
     }
 
 }
