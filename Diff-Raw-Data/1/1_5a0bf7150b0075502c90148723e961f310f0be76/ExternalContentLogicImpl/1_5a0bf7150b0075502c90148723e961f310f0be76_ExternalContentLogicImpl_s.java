 /**********************************************************************************
  * $URL: https://source.sakaiproject.org/contrib/assignment2/trunk/impl/src/java/org/sakaiproject/assignment2/logic/impl/ExternalLogicImpl.java $
  * $Id: ExternalLogicImpl.java 54198 2008-10-22 17:41:27Z wagnermr@iupui.edu $
  ***********************************************************************************
  *
  * Copyright (c) 2007 The Sakai Foundation.
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
 package org.sakaiproject.assignment2.logic.impl;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.assignment2.logic.AttachmentInformation;
 import org.sakaiproject.assignment2.logic.ExternalContentLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.content.api.ContentHostingService;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.content.api.ContentTypeImageService;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.exception.IdInvalidException;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.exception.IdUsedException;
 import org.sakaiproject.exception.InconsistentException;
 import org.sakaiproject.exception.OverQuotaException;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.exception.ServerOverloadException;
 import org.sakaiproject.exception.TypeException;
 
 /**
  * This is the implementation for logic which interacts with ContentHostingService
  */
 public class ExternalContentLogicImpl implements ExternalContentLogic {
 
     private static Log log = LogFactory.getLog(ExternalContentLogicImpl.class);
 
     private static final String BASE_IMG_PATH= "/library/image/";
 
     private ContentHostingService contentHosting;
     public void setContentHostingService(ContentHostingService contentHosting) {
         this.contentHosting = contentHosting;
     }
 
     private ContentTypeImageService imageService;
     public void setContentTypeImageService(ContentTypeImageService imageService) {
         this.imageService = imageService;
     }
 
     private ExternalLogic externalLogic;
     public void setExternalLogic(ExternalLogic externalLogic) {
         this.externalLogic = externalLogic;
     }
 
     /**
      * Place any code that should run when this class is initialized by spring here
      */
     public void init() {
         if (log.isDebugEnabled()) log.debug("init");
     }
 
     public ContentResource getContentResource(String reference) {
         if (reference == null) {
             throw new IllegalArgumentException("Null reference passed to getContentResource");
         }
 
         ContentResource resource = null;
 
         try {
             resource = contentHosting.getResource(reference);
         } catch (TypeException te) {
             log.warn("TypeException thrown when attempting to retrieve ContentResource with ref: " + reference, te);
         } catch (IdUnusedException iue) {
             log.warn("IdUnusedException thrown when attempting to retrieve ContentResource with ref: " + reference, iue);
         } catch (PermissionException pe) {
             // we aren't logging the full stacktrace here because this exception might occur if
             // you are accessing an attachment via osp. we'll try again with special security override info if that's the case
             log.warn("PermissionException thrown when attempting to retrieve ContentResource with ref: " + reference);
         }
 
         return resource;
     }
 
     public String getReferenceCollectionId(String contextId) {
         if (contextId == null) {
             throw new IllegalArgumentException("Null contextId passed to getReferenceCollectionId");
         }
 
         return contentHosting.getSiteCollection(contextId);
     }
 
     public AttachmentInformation getAttachmentInformation(String attachmentReference) {
         if (attachmentReference == null) {
             throw new IllegalArgumentException("Null attachmentReference passed to getAttachmentInformation");
         }
 
         AttachmentInformation attach = null;
 
         // try to retrieve the ContentResource associated with the reference
         ContentResource resource = getContentResource(attachmentReference);
         if (resource != null) {
             attach = new AttachmentInformation();
 
             ResourceProperties properties = resource.getProperties();
 
             // Content Length Display
             String contentLengthProp = properties.getNamePropContentLength();
             String contentLength = properties.getPropertyFormatted(contentLengthProp);
             attach.setContentLength(contentLength);
 
             // Content display name
             String displayNameProp = properties.getNamePropDisplayName();
             String displayName = properties.getProperty(displayNameProp);
             attach.setDisplayName(displayName);
 
             // Content Type
             String contentTypeProp = properties.getNamePropContentType();
             String contentType = properties.getProperty(contentTypeProp);
             attach.setContentType(contentType);
 
             // Content type display icon path
             String imagePath = BASE_IMG_PATH;
             imagePath += imageService.getContentTypeImage(contentType);
             attach.setContentTypeImagePath(imagePath);
 
             // url to the resource
             attach.setUrl(resource.getUrl());
         }
 
         return attach;
     }
 
     public String copyAttachment(String attachmentReference, String contextId) {
         String newAttRef = null;
         if (attachmentReference != null) {
             try {
                 ContentResource oldAttachment = contentHosting.getResource(attachmentReference);
                 String toolTitle = externalLogic.getToolTitle();
                 String name = Validator.escapeResourceName(oldAttachment.getProperties().getProperty(
                         ResourceProperties.PROP_DISPLAY_NAME));
                 String type = oldAttachment.getContentType();
                 byte[] content = oldAttachment.getContent();
                 ResourceProperties properties = oldAttachment.getProperties();
 
                 ContentResource newResource = contentHosting.addAttachmentResource(name, 
                         contextId, toolTitle, type, content, properties);
                 newAttRef = newResource.getId();
 
             } catch (TypeException te) {
                 log.warn("TypeException thrown while attempting to retrieve resource with" +
                         " id " + attachmentReference + ". Attachment was not copied.");
             } catch (PermissionException pe) {
                 log.warn("PermissionException thrown while attempting to retrieve resource with" +
                         " id " + attachmentReference + ". Attachment was not copied.");
             } catch (IdUnusedException iue) {
                 log.warn("IdUnusedException thrown while attempting to retrieve resource with" +
                         " id " + attachmentReference + ". Attachment was not copied.");
             } catch (IdInvalidException iie) {
                 log.warn("IdInvalidException thrown while attempting to copy resource with" +
                         " id " + attachmentReference + ". Attachment was not copied.");
             } catch (ServerOverloadException soe) {
                 log.warn("ServerOverloadException thrown while attempting to copy resource with" +
                         " id " + attachmentReference + ". Attachment was not copied.");
             } catch (IdUsedException iue) {
                 log.warn("IdUsedException thrown while attempting to copy resource with" +
                         " id " + attachmentReference + ". Attachment was not copied.");
             } catch (OverQuotaException oqe) {
                 log.warn("OverQuotaException thrown while attempting to copy resource with" +
                         " id " + attachmentReference + ". Attachment was not copied.");
             } catch (InconsistentException ie) {
                 log.warn("InconsistentException thrown while attempting to copy resource with" +
                         " id " + attachmentReference + ". Attachment was not copied.");
             }
         }
 
         return newAttRef;
     }
 
     public String getMyWorkspaceCollectionId(String userId) {
         if (userId == null) {
             throw new IllegalArgumentException("Null userId passed to getMyWorkspaceCollectionId");
         }
 
         String collectionId = null;
 
         String myWorkspaceSiteId = externalLogic.getMyWorkspaceSiteId(userId);
         if (myWorkspaceSiteId != null) {
             collectionId = contentHosting.getSiteCollection(myWorkspaceSiteId);
         } else {
             log.warn("No My Workspace site id found for user: " + userId);
         }
 
         return collectionId;
 
     }
 }
