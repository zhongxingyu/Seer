 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2005, 2006 The Sakai Foundation.
  *
  * Licensed under the Educational Community License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.opensource.org/licenses/ecl1.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.metaobj.shared.mgt;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.entity.api.Entity;
 import org.sakaiproject.entity.api.EntityManager;
 import org.sakaiproject.entity.api.EntityProducer;
 import org.sakaiproject.entity.api.HttpAccess;
 import org.sakaiproject.entity.api.Reference;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * Created by IntelliJ IDEA.
  * User: John Ellis
  * Date: Nov 7, 2005
  * Time: 1:40:10 PM
  * To change this template use File | Settings | File Templates.
  */
 public abstract class EntityProducerBase implements EntityProducer {
 
    private EntityManager entityManager;
    private HttpAccess httpAccess;
 
    public boolean willArchiveMerge() {
       return false;
    }
 
    public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments) {
       return null;
    }
 
    public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans, Set userListAllowImport) {
       return null;
    }
 
    public boolean parseEntityReference(String reference, Reference ref) {
       if (reference.startsWith(getContext())) {
          ref.set(getLabel(), null, reference, null, "");
          return true;
       }
       return false;
    }
 
    protected String getContext() {
       return Entity.SEPARATOR + getLabel() + Entity.SEPARATOR;
    }
 
    public String getEntityDescription(Reference ref) {
       return ref.getId();
    }
 
    public ResourceProperties getEntityResourceProperties(Reference ref) {
       ContentEntityWrapper entity = getContentEntityWrapper(ref);
 
       return entity.getBase().getProperties();
    }
 
    protected ContentEntityWrapper getContentEntityWrapper(Reference ref) {
       String wholeRef = ref.getReference();
       ReferenceParser parser = parseReference(wholeRef);
       ContentResource base =
             (ContentResource) entityManager.newReference(parser.getRef()).getEntity();
       return new ContentEntityWrapper(base, wholeRef);
    }
 
    protected ReferenceParser parseReference(String wholeRef) {
       return new ReferenceParser(wholeRef, this);
    }
 
    public Entity getEntity(Reference ref) {
       return getContentEntityWrapper(ref);
    }
 
    public String getEntityUrl(Reference ref) {
       return ServerConfigurationService.getAccessUrl() + ref.getReference();
    }
 
   public Collection getEntityAuthzGroups(Reference ref) {
       return null;
    }
 
    public EntityManager getEntityManager() {
       return entityManager;
    }
 
    public void setEntityManager(EntityManager entityManager) {
       this.entityManager = entityManager;
    }
 
    public HttpAccess getHttpAccess() {
       return httpAccess;
    }
 
    public void setHttpAccess(HttpAccess httpAccess) {
       this.httpAccess = httpAccess;
    }
 
    public void destroy() {
 
    }
 
 }
