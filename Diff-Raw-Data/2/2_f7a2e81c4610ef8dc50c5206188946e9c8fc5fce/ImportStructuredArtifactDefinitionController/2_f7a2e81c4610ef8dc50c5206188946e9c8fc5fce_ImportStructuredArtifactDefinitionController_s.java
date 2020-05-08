 /**********************************************************************************
  * $URL: https://source.sakaiproject.org/svn/trunk/sakai/admin-tools/su/src/java/org/sakaiproject/tool/su/SuTool.java $
  * $Id: SuTool.java 6970 2006-03-23 23:25:04Z zach.thomas@txstate.edu $
  ***********************************************************************************
  *
  * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
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
 
 package org.sakaiproject.metaobj.shared.control;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.sakaiproject.component.cover.ComponentManager;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.content.api.FilePickerHelper;
 import org.sakaiproject.content.api.ContentHostingService;
 import org.sakaiproject.entity.api.Reference;
 import org.sakaiproject.entity.api.EntityManager;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.exception.ImportException;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.exception.TypeException;
 import org.sakaiproject.exception.UnsupportedFileTypeException;
 import org.sakaiproject.metaobj.shared.model.FormUploadForm;
 import org.sakaiproject.metaobj.shared.model.InvalidUploadException;
 import org.sakaiproject.metaobj.utils.mvc.intf.Controller;
 import org.sakaiproject.tool.api.ToolSession;
 import org.sakaiproject.tool.api.SessionManager;
 import org.springframework.validation.Errors;
 import org.springframework.validation.Validator;
 import org.springframework.web.servlet.ModelAndView;
 
 public class ImportStructuredArtifactDefinitionController extends AddStructuredArtifactDefinitionController
       implements Controller, Validator {
 
    private SessionManager sessionManager;
    private ContentHostingService contentHosting = null;
    private EntityManager entityManager;
 
    public Object formBackingObject(Map request, Map session, Map application) {
 
       FormUploadForm backingObject = new FormUploadForm();
       return backingObject;
    }
 
    /*
 public ModelAndView handleRequest(Object requestModel, Map request, Map session, Map application, Errors errors) {
 return prepareListView(request, null);
 }
 */
    public ModelAndView handleRequest(Object requestModel, Map request, Map session,
                                      Map application, Errors errors) {
 
       FormUploadForm templateForm = (FormUploadForm) requestModel;
       if (templateForm == null) {
          return new ModelAndView("success");
       }
       if (templateForm.getSubmitAction() != null && templateForm.getSubmitAction().equals("pickImport")) {
          if (templateForm.getUploadedForm() != null && templateForm.getUploadedForm().length() > 0) {
             Reference ref;
             List files = new ArrayList();
             String ids[] = templateForm.getUploadedForm().split(",");
             for (int i = 0; i < ids.length; i++) {
                try {
                   String id = ids[i];
                   id = getContentHosting().resolveUuid(id);
                   String rid = getContentHosting().getResource(id).getReference();
                   ref = getEntityManager().newReference(rid);
                   files.add(ref);
                }
                catch (PermissionException e) {
                   logger.error("", e);
                }
                catch (IdUnusedException e) {
                   logger.error("", e);
                }
                catch (TypeException e) {
                   logger.error("", e);
                }
             }
             session.put(FilePickerHelper.FILE_PICKER_ATTACHMENTS, files);
          }
          return new ModelAndView("pickImport");
       }
       else {
          String view = "success";
          if (templateForm.getUploadedForm().length() > 0) {
             String ids[] = templateForm.getUploadedForm().split(",");
             for (int i = 0; i < ids.length; i++) {
                try {
                   String id = ids[i];
                   if (!getStructuredArtifactDefinitionManager().importSADResource(getWorksiteManager().getCurrentWorksiteId(), id, true)) {
                      errors.rejectValue("uploadedForm", "error.format", "File format not recognized");
 
                      view = "failed";
                   }
                }
                catch (InvalidUploadException e) {
                   logger.warn("Failed uploading template", e);
                   errors.rejectValue(e.getFieldName(), e.getMessage(), e.getMessage());
                   view = "failed";
                }
                catch(UnsupportedFileTypeException ufte) {
                   logger.warn("Failed uploading template", ufte);
                   errors.rejectValue("uploadedForm", ufte.getMessage(), ufte.getMessage());
                   view = "failed";
                }
                catch(ImportException ie) {
                   logger.warn("Failed uploading template", ie);
                   errors.rejectValue("uploadedForm", ie.getMessage(), ie.getMessage());
                   view = "failed";
                }
                catch (Exception e) {
                   logger.error("Failed importing template", e);
                   view = "failed";
                }
             }
          }
          Map model = new Hashtable();
          return new ModelAndView(view, model);
       }
    }
 
    public Map referenceData(Map request, Object command, Errors errors) {
       FormUploadForm templateForm = (FormUploadForm) command;
       Map model = new HashMap();
 
       ToolSession session = getSessionManager().getCurrentToolSession();
       if (session.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null &&
             session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) {
          // here is where we setup the id
          List refs = (List) session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
          if (refs.size() >= 1) {
             String ids = "";
             String names = "";
 
             for (Iterator iter = refs.iterator(); iter.hasNext();) {
                Reference ref = (Reference) iter.next();
                String nodeId = getContentHosting().getUuid(ref.getId());
                String id = getContentHosting().resolveUuid(nodeId);
 
                ContentResource resource = null;
                try {
                   resource = getContentHosting().getResource(id);
                }
                catch (PermissionException pe) {
                   logger.warn("Failed loading content: no permission to view file", pe);
                }
                catch (TypeException pe) {
                   logger.warn("Wrong type", pe);
                }
                catch (IdUnusedException pe) {
                   logger.warn("UnusedId: ", pe);
                }
 
 
                if (ids.length() > 0) {
                   ids += ",";
                }
                ids += nodeId;
                names += resource.getProperties().getProperty(resource.getProperties().getNamePropDisplayName()) + " ";
             }
             templateForm.setUploadedForm(ids);
             model.put("name", names);
          }
          else {
             templateForm.setUploadedForm(null);
          }
       }
 
       session.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
       session.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
       session.setAttribute(FilePickerHelper.FILE_PICKER_RESOURCE_FILTER,
            ComponentManager.get("org.sakaiproject.metaobj.shared.ContentResourceFilter.formUploadStyleFile"));
       return model;
    }
 
    public boolean supports(Class clazz) {
       return (FormUploadForm.class.isAssignableFrom(clazz));
    }
 
    public void validate(Object obj, Errors errors) {
       FormUploadForm templateForm = (FormUploadForm) obj;
       if (templateForm.getUploadedForm() == null && templateForm.isValidate()) {
          errors.rejectValue("uploadedForm", "error.required", "required");
       }
    }
 
    public SessionManager getSessionManager() {
       return sessionManager;
    }
 
    public void setSessionManager(SessionManager sessionManager) {
       this.sessionManager = sessionManager;
    }
 
    public ContentHostingService getContentHosting() {
       return contentHosting;
    }
 
    public void setContentHosting(ContentHostingService contentHosting) {
       this.contentHosting = contentHosting;
    }
 
    public EntityManager getEntityManager() {
       return entityManager;
    }
 
    public void setEntityManager(EntityManager entityManager) {
       this.entityManager = entityManager;
    }
 }
 
 
