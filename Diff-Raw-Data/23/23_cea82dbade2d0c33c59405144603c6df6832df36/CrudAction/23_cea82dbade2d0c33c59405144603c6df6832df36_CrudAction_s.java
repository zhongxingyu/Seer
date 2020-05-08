 /*
     jBilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2009 Enterprise jBilling Software Ltd. and Emiliano Conde
 
     This file is part of jbilling.
 
     jbilling is free software: you can redistribute it and/or modify
     it under the terms of the GNU Affero General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     jbilling is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Affero General Public License for more details.
 
     You should have received a copy of the GNU Affero General Public License
     along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.sapienter.jbilling.client.util;
 
 import java.io.IOException;
 import java.util.Date;
 
 import javax.persistence.OptimisticLockException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.Globals;
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 import org.apache.struts.config.ModuleConfig;
 import org.apache.struts.util.RequestUtils;
 import org.apache.struts.validator.DynaValidatorForm;
 import org.apache.struts.validator.Resources;
 
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.server.user.UserDTOEx;
 import com.sapienter.jbilling.server.user.IUserSessionBean;
 import com.sapienter.jbilling.server.util.Context;
 
 /**
  * This action is a helper class that replaces GenericMaintainAction.
  * It is meant for forms that are mostly CRUD (create-retrieve-update-delete)
  * to/from the database.
  * 
  * @author Emil
  *
  */
 public abstract class CrudAction extends Action {
     protected ActionMapping mapping = null;
     protected HttpServletRequest request = null;
     protected ActionErrors errors = null;
     protected ActionMessages messages = null;
     protected Logger LOG = null;
     protected HttpSession session = null;
     protected DynaValidatorForm myForm = null;
     protected String action = null;
     protected String formName = null;
     
     // handy variables
     protected Integer selectedId = null;
     protected Integer languageId = null;
     protected Integer entityId = null;
     protected Integer executorId = null;
     
     // by default, it goes to a predefined value
     // the implementations can change this at will
     protected String forward;
     
     private FormHelper formHelper;
     private FormDateHelper dateHelper;
  
     /**
      * A constructor with no arguments is required from the implementation
      * to initialize variables before Struts calls 'execute'.
      * Implement with:
      *    - setting the formName
      *    - initializing a reference to your remote session bean
      */
     
     public ActionForward execute(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException {
         Object dtoHolder = null;
         dateHelper = null;
         forward = null;
         this.mapping = mapping;
         this.request = request;
         LOG = Logger.getLogger(CrudAction.class);
         errors = new ActionErrors();
         messages = new ActionMessages();
         action = request.getParameter("action");
         session = request.getSession(false);
         if (action == null) {
             throw new SessionInternalError("action has to be present in the request");
         }
         if (formName == null || formName.trim().length() == 0) {
             throw new SessionInternalError("formName has to be present");
         }
         
         selectedId = (Integer) session.getAttribute(
                 Constants.SESSION_LIST_ID_SELECTED);
         languageId = (Integer) session.getAttribute(
                 Constants.SESSION_LANGUAGE);
         executorId = (Integer) session.getAttribute(
                 Constants.SESSION_LOGGED_USER_ID);
         entityId = (Integer) session.getAttribute(
                 Constants.SESSION_ENTITY_ID_KEY);
         myForm = (DynaValidatorForm) form;
 
         LOG.debug("processing action : " + action);
         try {
             if (action.equals("edit")) {
             	if (isResetRequested()){
                     preReset();
                     reset();
                 }
 
             	if (forward == null) {
                     preEdit();
                     if (errors.isEmpty()) {
                         dtoHolder = editFormToDTO();
                     }         
                     
                     // some errors could be added during the form->dto copy
                     if (!errors.isEmpty()) {
                         // Save the error messages we need
                         request.setAttribute(Globals.ERROR_KEY, errors);
                         return(mapping.findForward(forward));
                     }                
                     
                     // see if this is a create, if so call it
                     boolean createCalled = false;
                     try {
                         String createFlag = (String) myForm.get("create");
                         if (createFlag != null && createFlag.length() > 0) {
                             forward = "create";
                             create(dtoHolder);
                             createCalled = true;
                         }
                     } catch (IllegalArgumentException e) {
                         // the 'create' form field is not there
                         // no problem, create is not implemented
                     }
                     
                     // you can't have a create and update in the same call
                     if (!createCalled) {
                         String messageKey = update(dtoHolder);
                         if (messageKey != null) {
                         	addGlobalMessage(messageKey);
                         }
                     }
                     
                     if (forward.equals("list")) {
                         //XXX: [MG] 
                     	//Most probably this is never called
                     	//Subclasses generally return forwards in the form of <mode>_<action>,
                     	//e.g: "type_list" instead of just "list"
                     	//
                     	//Original intention was:
                     	// remove the form from the session, otherwise it
                         // might show up in a later
                     	removeFormFromSession();
                     } 
                 }
             } else if(action.equals("setup")) {
                 preSetup();
                 setup();
                 postSetup();
             } else if(action.equals("delete")) {
                 preDelete();
                 String messageKey = delete();
                 if (messageKey != null) {
                 	addGlobalMessage(messageKey);
                 }
                 postDelete();
             } else if (!otherAction(action)){
                 LOG.error("Invalid action:" + action);
                 errors.add(ActionErrors.GLOBAL_ERROR,
                         new ActionError("all.internal"));
             }
         } catch (OptimisticLockException e) {
             LOG.debug("optimistic lock exception");
             errors.add(ActionErrors.GLOBAL_ERROR,
                    new ActionError("errors.modified"));
         } catch (Exception e) {
             LOG.error("Exception ", e);
             errors.add(ActionErrors.GLOBAL_ERROR,
                    new ActionError("all.internal"));
         }
 
         // Remove any error messages attribute if none are required
         if ((errors == null) || errors.isEmpty()) {
             request.removeAttribute(Globals.ERROR_KEY);
         } else {
             // Save the error messages we need
             request.setAttribute(Globals.ERROR_KEY, errors);
         }
         
         // Remove any messages attribute if none are required
         if ((messages == null) || messages.isEmpty()) {
             request.removeAttribute(Globals.MESSAGE_KEY);
         } else {
             // Save the messages we need
             request.setAttribute(Globals.MESSAGE_KEY, messages);
         }
         
         LOG.debug("forwarding to " + forward);
         return mapping.findForward(forward);
     }
     
 	private void preSetup() {
         ModuleConfig moduleConfig = RequestUtils.getModuleConfig(request,
                 servlet.getServletContext());
         myForm = (DynaValidatorForm) RequestUtils.createActionForm(
                 request, mapping, moduleConfig, servlet);
         forward = "edit";
     }
     
     /**
      * Setup only works when the form name has been setup
      * Implement by :
      *   - Get your dto by calling the remote session 
      *   - Set the form bean with the dto values
      *     Ex: myForm.set("name", dto.getDescription()); 
      */
     public abstract void setup();
     
     protected void postSetup() {
         LOG.debug("setting up form name=" + formName + 
                 " dyna=" + myForm);
                 
         session.setAttribute(formName, myForm);
     }
         
     protected void preEdit() {
         // do the validation, before moving any info to the dto
         errors = new ActionErrors(myForm.validate(mapping, request));
         forward = "edit";
     }
     
     /**
      * Implement this and 
      *   - create your DTO
      *   - populate it from 'myForm'
      *   - return your DTO
      */
     public abstract Object editFormToDTO();
     
     /**
      * For the create to work, you need a 'create' form variable.
      * This variable should have something in it for this method to get called
      *
      * Implement by calling your remote session (the server tier) with the DTO
      * that was initialized in the edit method. This dto is passed as a 
      * parameter to this method.
      */
     public abstract void create(Object dtoHolder);
     
     /**
      * Do not forget to remove a list object if applicable
      * 
      * Typical implementation does:
      *   - set the id for the dto (primary key)
      *   - call the remote session (server tier)
      *   - return the message key that tells the user the record was updated
      *     (or null for no message)
      * @param dtoHolder The DTO previously intialized in the edit call
      */
     public abstract String update(Object dtoHolder);
     
     public void preDelete() {
         forward = "deleted";
     }
     
     /**
      * 
      * Implement by:
      *   - getting some key fields of the row to delete from the session
      *   - call your remote session, passing the key fields as parameters
      */
     public abstract String delete();
     
     public void postDelete() {
     	removeFormFromSession();
     }
     
     protected void preReset() {
         forward = "edit";
         myForm.initialize(mapping);
     }
     
     /**
      * Implement by removing objects from the session
      */
     public abstract void reset();
     
     /**
      * Implement to handle those specific actions that are not CRUD
      * Return false if the action is not supported. 
      * If you don't need to implement this methos, simply return false.
      */
     public abstract boolean otherAction(String action);
     
     protected boolean isResetRequested(){
     	String reset;
     	try {
             // this deals with a button that resets a form
             // that is why it works on a field from the form, 
             // and not on an action from the request.
             reset = (String) myForm.get("reset");
         } catch (IllegalArgumentException e) {
         	return false;
         }
 
         return (reset != null && reset.length() > 0);
     }
 
     public String getFormName() {
         return formName;
     }
 
     public void setFormName(String formName) {
         this.formName = formName;
     }
     
     protected void setForward(String aForward){
     	this.forward = aForward;
     }
 
     protected final void removeFormFromSession() {
     	session.removeAttribute(formName);		
 	}
     
     protected final void addGlobalMessage(String messageCode){
     	if (messageCode != null){
     		messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(messageCode));
     	}
     }
     
     protected final String float2string(Float arg) {
 		return getFormHelper().float2string(arg);
 	}
     protected final String float2string(Double arg) {
         if (arg == null) return null;
         return getFormHelper().float2string(new Float(arg));
     }
 
     protected final Float string2float(String arg) {
         return getFormHelper().string2float(arg);
     }
 
     protected final Double string2double(String arg) {
         Float fl =  getFormHelper().string2float(arg);
         if (fl == null) return null;
         else return new Double(fl);
     }
 
 	protected final UserDTOEx getUser(Integer userId) {    
 		UserDTOEx retValue = null;
 		try {
 			IUserSessionBean userSession = (IUserSessionBean) Context.getBean(
                     Context.Name.USER_SESSION);
 			retValue = userSession.getUserDTOEx(userId);
 		} catch (Exception e) {
 			throw new SessionInternalError(e);
 		}
 		return retValue;
 	}
 	
 	protected FormHelper getFormHelper(){
         if (formHelper == null || !session.getId().equals(formHelper.getSessionId())) {
             formHelper = new FormHelper(session);
         }
 		return formHelper;
 	}
 	
 	protected FormDateHelper getDateHelper(){
 		if (dateHelper == null){
 			dateHelper = new FormDateHelper(myForm, request);
 		}
 		return dateHelper;
 	}
 	
     protected final Date parseDate(String prefix, String prompt) {
     	return getDateHelper().parseDate(prefix, prompt, errors);
     }
     
     protected final void setFormDate(String prefix, Date date) {
     	getDateHelper().setFormDate(prefix, date);
     }
     
     protected void required(String field, String key) {
         if (field == null || field.trim().length() == 0) {
             String name = Resources.getMessage(request, key);
             errors.add(ActionErrors.GLOBAL_ERROR,
                     new ActionError("errors.required", name));
         }
     }
     
     protected void required(Date field, String key) {
         if (field == null) {
             String name = Resources.getMessage(request, key);
             errors.add(ActionErrors.GLOBAL_ERROR,
                     new ActionError("errors.required", name));
         }
     }
 
 	
     
 }
