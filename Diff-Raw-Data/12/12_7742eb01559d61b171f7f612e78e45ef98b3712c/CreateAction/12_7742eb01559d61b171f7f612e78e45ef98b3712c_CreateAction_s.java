 /*
 The contents of this file are subject to the Jbilling Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.jbilling.com/JPL/
 
 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.
 
 The Original Code is jbilling.
 
 The Initial Developer of the Original Code is Emiliano Conde.
 Portions created by Sapienter Billing Software Corp. are Copyright 
 (C) Sapienter Billing Software Corp. All Rights Reserved.
 
 Contributor(s): ______________________________________.
 */
 
 package com.sapienter.jbilling.client.user;
 
 import java.io.IOException;
 import java.util.Vector;
 
 import javax.ejb.FinderException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 import org.apache.struts.action.DynaActionForm;
 
 import com.sapienter.jbilling.client.util.Constants;
 import com.sapienter.jbilling.common.JNDILookup;
 import com.sapienter.jbilling.interfaces.UserSession;
 import com.sapienter.jbilling.interfaces.UserSessionHome;
 import com.sapienter.jbilling.server.entity.UserDTO;
 import com.sapienter.jbilling.server.user.ContactDTOEx;
 import com.sapienter.jbilling.server.user.CustomerDTOEx;
 import com.sapienter.jbilling.server.user.UserDTOEx;
 
 public class CreateAction extends Action {
 
     public ActionForward execute(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException {
     
         ActionErrors errors = new ActionErrors();
         ActionMessages messages = new ActionMessages();
         String retValue = "done";
         Integer newUserId = null;
         // some attributes of the new user will be inherited from the root user
         // that is creating this account
         HttpSession session = request.getSession();
         Integer entityId = (Integer) session.getAttribute(
                 Constants.SESSION_ENTITY_ID_KEY);
         
         Integer languageId = (Integer) session.getAttribute(
                 Constants.SESSION_LANGUAGE);
         // get the dynamic form
         DynaActionForm userForm = (DynaActionForm) form;
         
         Logger.getLogger(CreateAction.class).debug("Create user action");
         
         // verify that the password and the verification password are the same
         if (!((String) userForm.get("password")).equals((String) 
                 userForm.get("verifyPassword"))) {
             errors.add(ActionErrors.GLOBAL_ERROR,
                     new ActionError("user.create.error.password_match"));
         } else {
          
             try {
                 JNDILookup EJBFactory = JNDILookup.getFactory(false);
                 UserSessionHome userHome =
                         (UserSessionHome) EJBFactory.lookUpHome(
                         UserSessionHome.class,
                         UserSessionHome.JNDI_NAME);
         
                 UserSession myRemoteSession = userHome.create();
                 
                 // create the dto from the dynamic form
                 Integer typeId = (Integer) userForm.get("type");
                 if (typeId == null) {
                     // some users won't have the permission to pick the
                     // type, so it is assumend they are creating customers
                     typeId = Constants.TYPE_CUSTOMER;
                 }
                 UserDTOEx dto = new UserDTOEx(null, entityId, 
                         (String)userForm.get("username"),
                         (String)userForm.get("password"),
                         null, languageId, 
                         typeId, (Integer) userForm.get("currencyId"),
                         null, null,null, null);
                 
                 // add the roles 
                 // now, it will be just one and directly mapped to the user type
                 // this doesn't have to be like this, it is now because it is 
                 // enough for the BAT requirements
                 Vector roles = new Vector();
                 roles.add(typeId);
                 dto.setRoles(roles);
                 
                 // finally, create the contact dto to send the email
                 ContactDTOEx contact = new ContactDTOEx();
                 contact.setEmail((String) userForm.get("email"));
                 // and default the country to the entity's
                 contact.setCountryCode(myRemoteSession.getEntityContact(
                         entityId).getCountryCode());
                 // by default the contact is included 
                 contact.setInclude(new Integer(1));
                 
                 
                 // if it is a customer, it'll need the customer dto
                 if (typeId.equals(Constants.TYPE_CUSTOMER)) {
                     CustomerDTOEx customerDto = new CustomerDTOEx();
                     
                     // set the partner 
                     Integer partnerId = null;
                     try {
                         partnerId = Integer.valueOf((String) userForm.get(
                                 "partnerId"));
                     } catch (Exception e) {
                         try {
                             partnerId = Integer.valueOf((String)
                                     session.getAttribute("jsp_partnerId"));
                         } catch (Exception e1) {
                         }
                     }
                     if (partnerId != null) {
                         customerDto.setPartnerId(partnerId);
                     }
                     customerDto.setReferralFeePaid(new Integer(0));
                     dto.setCustomerDto(customerDto);
 
                     // set the sub-account fields
                     String parentStr = (String) userForm.get("parentId");
                     Integer parentId = null;
                     if (parentStr != null && parentStr.length() > 0) {
                         parentId = Integer.valueOf(parentStr);
                     }
                     if (parentId != null) {
                         customerDto.setParentId(parentId);
                     }
                     
                     customerDto.setIsParent(((Boolean) userForm.get(
                             "chbx_is_parent")).booleanValue() 
                             ? new Integer(1) : new Integer(0));
                     
                     if (customerDto.getIsParent().intValue() == 1 &&
                             customerDto.getParentId() != null) {
                         // can't be parent and child at the same time
                         errors.add(ActionErrors.GLOBAL_ERROR,
                                 new ActionError(
                                     "user.create.error.parent"));
                     }
                     
                     // verify that the parent is valid
                     if (customerDto.getParentId() != null) {
                         try {
                             UserDTOEx parent = myRemoteSession.getUserDTOEx(
                                     customerDto.getParentId());
                             if (!parent.getEntityId().equals(entityId) ||
                                     parent.getDeleted().intValue() == 1) {
                                 errors.add(ActionErrors.GLOBAL_ERROR,
                                         new ActionError(
                                             "user.create.error.noParent"));                               
                             } else {
                                 if (parent.getCustomerDto() == null ||
                                         parent.getCustomerDto().getIsParent() == null ||
                                         parent.getCustomerDto().getIsParent().intValue() == 0) {
                                     errors.add(ActionErrors.GLOBAL_ERROR,
                                             new ActionError(
                                                 "user.create.error.badParent"));        
                                 }
                             }
                         } catch (FinderException e) {
                             errors.add(ActionErrors.GLOBAL_ERROR,
                                     new ActionError(
                                         "user.create.error.noParent"));
                         }
                     }
                 } 
                 
                 if (typeId.equals(Constants.TYPE_PARTNER) && errors.isEmpty()) {
                     // partners require more data
                     
                     // verify that the user is available
                     UserDTO user = myRemoteSession.getUserDTO(
                             dto.getUserName(), entityId);
                     if (user != null) {
                         errors.add(ActionErrors.GLOBAL_ERROR,
                                 new ActionError("user.create.error.taken", 
                                 (String) userForm.get("username")));
                     } else {
                         // leave the data available to the next step
                         session.setAttribute(Constants.SESSION_CUSTOMER_DTO, dto);
                         session.setAttribute(
                                 Constants.SESSION_CUSTOMER_CONTACT_DTO, contact);
                         // but to allow the defaults to be loaded, make sure no
                         // id is in the session
                         session.removeAttribute(Constants.SESSION_PARTNER_ID);
                         retValue = "partner";
                     }
                 } else if (errors.isEmpty()) {
                     newUserId = myRemoteSession.create(dto, contact);
                     if (newUserId == null) {
                         errors.add(ActionErrors.GLOBAL_ERROR,
                                 new ActionError("user.create.error.taken", 
                                 (String) userForm.get("username")));
                     } else if (newUserId.intValue() == -1) {
                         errors.add(ActionErrors.GLOBAL_ERROR,
                                 new ActionError("user.create.error.badPartner"));
                     }
                 }
             } catch (Exception e) {
                 errors.add(ActionErrors.GLOBAL_ERROR,
                         new ActionError("all.internal"));
             }
         }
         
         if (errors.isEmpty()) {
             // the creation was successfull, let's now see if that's all or
             // the contact information will be entered
             messages.add(ActionMessages.GLOBAL_MESSAGE, 
                     new ActionMessage("user.create.done"));
             // later, when a customer is self creating, the forward might
             // have to be to the contact form.
             // leave the user id in the session so the page knows that
             // is time to show up the contact/cc options
             session.setAttribute(Constants.SESSION_USER_ID, 
                     newUserId);
             Logger.getLogger(CreateAction.class).debug("new user id = " + 
                     newUserId);
             session.setAttribute(Constants.SESSION_CONTACT_USER_ID, 
                     newUserId);
             saveMessages(request, messages);
             // remove any list with users
             session.removeAttribute(Constants.SESSION_LIST_KEY + 
                     Constants.LIST_TYPE_CUSTOMER);
             session.removeAttribute(Constants.SESSION_LIST_KEY + 
                     Constants.LIST_TYPE_CUSTOMER_SIMPLE);
         } else {
             // something failed
             saveErrors(request, errors);
             Logger.getLogger(CreateAction.class).debug("errors: " + errors.size());
             retValue = "error";
         }
         
         return mapping.findForward(retValue);
         
     }
 }
