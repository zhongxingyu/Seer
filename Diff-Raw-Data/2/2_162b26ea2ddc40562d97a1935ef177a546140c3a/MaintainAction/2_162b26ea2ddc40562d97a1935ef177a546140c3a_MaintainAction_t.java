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
 import org.apache.struts.validator.DynaValidatorForm;
 
 import com.sapienter.jbilling.client.util.Constants;
 import com.sapienter.jbilling.common.JNDILookup;
 import com.sapienter.jbilling.interfaces.InvoiceSession;
 import com.sapienter.jbilling.interfaces.InvoiceSessionHome;
 import com.sapienter.jbilling.interfaces.UserSession;
 import com.sapienter.jbilling.interfaces.UserSessionHome;
 import com.sapienter.jbilling.server.order.NewOrderDTO;
 import com.sapienter.jbilling.server.user.CustomerDTOEx;
 import com.sapienter.jbilling.server.user.UserDTOEx;
 import com.sapienter.jbilling.server.entity.UserDTO;
 
 public class MaintainAction extends Action {
 
     public ActionForward execute(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException {
         ActionErrors errors = new ActionErrors();
         ActionMessages messages = new ActionMessages();
         Logger log = Logger.getLogger(MaintainAction.class);
         HttpSession session = request.getSession(false);
 
         String action = request.getParameter("action");
         if (action == null) {
             log.error("action is required in maintain action");
             throw new ServletException("action is required");
         }
         
         // this page requires a forward from, but not a forward to, as it
         // always reders itself back with the result of the sumbision
         String forward = (String) session.getAttribute(
                 Constants.SESSION_FORWARD_FROM);
         
         Integer userId = (Integer) session.getAttribute(
                 Constants.SESSION_USER_ID);
         Integer executorId = (Integer) session.getAttribute(
                 Constants.SESSION_LOGGED_USER_ID);
         UserDTOEx userDto = (UserDTOEx) session.getAttribute(
                 Constants.SESSION_USER_DTO);
 
         try {
             JNDILookup EJBFactory = JNDILookup.getFactory(false);
             UserSessionHome userHome =
                     (UserSessionHome) EJBFactory.lookUpHome(
                     UserSessionHome.class,
                     UserSessionHome.JNDI_NAME);
         
             UserSession userSession = userHome.create();
         
             if (action.equals("setup")) {
                 String id = request.getParameter("id");
                 if (id != null) {
                     // called from anywhere to see a customer
                     userId = Integer.valueOf(id);
                 } else {
                     // called from the list when selectin a customer
                     userId = (Integer) session.getAttribute(
                             Constants.SESSION_LIST_ID_SELECTED);
                 }
                 userDto = userSession.getUserDTOEx(userId); 
                 session.setAttribute(Constants.SESSION_CUSTOMER_DTO, 
                         userDto);
                 session.setAttribute(Constants.SESSION_USER_ID,
                         userId);
                 session.setAttribute(Constants.SESSION_CUSTOMER_CONTACT_DTO,
                         userSession.getPrimaryContactDTO(userId));
 
                 // add the last invoice dto 
                 InvoiceSessionHome invoiceHome =
                         (InvoiceSessionHome) EJBFactory.lookUpHome(
                         InvoiceSessionHome.class,
                         InvoiceSessionHome.JNDI_NAME);
         
                 InvoiceSession invoiceSession = invoiceHome.create();
                 if (userDto.getLastInvoiceId() != null) {
                     log.debug("adding the latest inovoice: " + 
                             userDto.getLastInvoiceId());
                     Integer languageId = (Integer) session.getAttribute(
                             Constants.SESSION_LANGUAGE);
                     session.setAttribute(Constants.SESSION_INVOICE_DTO,
                             invoiceSession.getInvoiceEx(userDto.getLastInvoiceId(),
                                 languageId));
                 } else {
                     log.debug("there is no invoices.");
                     session.removeAttribute(Constants.SESSION_INVOICE_DTO);
                 }
 
                 return mapping.findForward("view");
             } 
 
             if (forward == null) {
                 log.error("forward is required in the session");
                 throw new ServletException("forward is required in the session");
             }
             if (userId == null) {
                 log.error("userId is required in the session");
                 throw new ServletException("userId is required in the session");
             }
             
             if (action.equals("delete")) {
                 userSession.delete(executorId, userId);
                 // after deleting, it goes to the maintain page, showing the
                 // list of users
                 forward = Constants.FORWARD_USER_MAINTAIN;
                 messages.add(ActionMessages.GLOBAL_MESSAGE, 
                         new ActionMessage("user.delete.done", userId));
                 // get rid of the cached list of users
                 session.removeAttribute(Constants.SESSION_LIST_KEY + 
                 		Constants.LIST_TYPE_CUSTOMER);
                 session.removeAttribute(Constants.SESSION_LIST_KEY + 
                 		Constants.LIST_TYPE_CUSTOMER_SIMPLE);
             } else if (action.equals("update")) {
                 DynaValidatorForm userForm = (DynaValidatorForm) form;
 
 				// get the info in its current status
                 UserDTOEx orgUser = (UserDTOEx) session.getAttribute(
                         Constants.SESSION_CUSTOMER_DTO);
 				log.debug("Updating user: ");
                 // general validation first
                 errors = userForm.validate(mapping, request);
                 // verify that the password and the verification password 
                 // are the same, but only if the verify password has been
                 // entered, otherwise will consider that the password is not
                 // being changed
                 String vPassword = (String) userForm.get("verifyPassword");
                 String password = (String) userForm.get("password");
                 boolean updatePassword = false;
                 if ((vPassword != null && vPassword.trim().length() > 0) ||
                         (password != null && password.trim().length() > 0)) {
                     updatePassword = true;
                 }
                 if (updatePassword && (password == null || 
                         password.trim().length() == 0 || vPassword == null ||
                         vPassword.trim().length() == 0 ||
                         !password.equals(vPassword))) {
                     errors.add(ActionErrors.GLOBAL_ERROR,
                             new ActionError("user.create.error.password_match"));
                 }
                 
                 // test that the old password is correct if this is a self-update
                 if (updatePassword && userId.equals(executorId) && 
                         !userDto.getPassword().equals((String) userForm.get(
                         "oldPassword"))) {
                     errors.add(ActionErrors.GLOBAL_ERROR,
                             new ActionError("user.edit.error.invalidOldPassword"));
                 } 
                 
                 String partnerId = (String) userForm.get("partnerId");
                 // validate the partnerId if present
                if (errors.isEmpty() && partnerId != null && partnerId.length() > 0) {
                     try {
                         userSession.getPartnerDTO(Integer.valueOf(partnerId));
                     } catch (FinderException e) {
                         errors.add(ActionErrors.GLOBAL_ERROR,
                                 new ActionError("user.create.error.badPartner"));
                     }
                 }
 
 				// the login name has to be unique across entities
                 // test only if it has changed
                 if (orgUser != null && !orgUser.getUserName().equals((String) 
                         userForm.get("username"))) {
                     UserDTO testUser = userSession.getUserDTO(
                             (String) userForm.get("username"), 
                             (Integer) userForm.get("entity"));
 
                     if (testUser != null) {
                         errors.add(ActionErrors.GLOBAL_ERROR,
                                 new ActionError("user.create.error.taken", 
                                     (String) userForm.get("username")));
                     }
                 }
 				
                 
                 if (errors.isEmpty()) {              
                     // create a dto with the info from the form
                     UserDTOEx dto = new UserDTOEx();
                     
                     dto.setUserId(userId);
                     dto.setEntityId((Integer) userForm.get("entity"));
                     dto.setMainRoleId((Integer) userForm.get("type"));
                     dto.setUserName((String) userForm.get("username"));
                     if (updatePassword) {
                         dto.setPassword((String) userForm.get("password"));
                     } else {
                         dto.setPassword(null);
                     }
                     dto.setLanguageId((Integer) userForm.get("language"));
                     dto.setStatusId((Integer) userForm.get("status"));
                     dto.setCurrencyId((Integer) userForm.get("currencyId"));
                     
                     
                     if (dto.getMainRoleId().equals(Constants.TYPE_CUSTOMER)) {
                         dto.setCustomerDto(new CustomerDTOEx());
                         dto.getCustomerDto().setInvoiceDeliveryMethodId(
                                 (Integer) userForm.get("deliveryMethodId"));
                         dto.getCustomerDto().setDueDateUnitId(
                                 (Integer) userForm.get("due_date_unit_id"));
                         String value = (String) userForm.get("due_date_value");
                         if (value != null && value.length() > 0) {
                             dto.getCustomerDto().setDueDateValue(
                                     Integer.valueOf(value));
                         } else {
                             dto.getCustomerDto().setDueDateValue(null);
                         }
                         dto.getCustomerDto().setDfFm(new Integer(((Boolean)
                                     userForm.get("chbx_df_fm")).booleanValue() 
                                         ? 1 : 0));
                         dto.getCustomerDto().setExcludeAging(new Integer(((Boolean)
                                     userForm.get("chbx_excludeAging")).booleanValue() 
                                         ? 1 : 0));
                         
                         if (partnerId != null && partnerId.length() > 0) {
                             dto.getCustomerDto().setPartnerId(Integer.valueOf(
                                 partnerId));
                         } else {
                             dto.getCustomerDto().setPartnerId(null);
                         }
                     }
                     
                     // I pass who am I and the info to update
                     userSession.update(executorId, dto);
                     
                     messages.add(ActionMessages.GLOBAL_MESSAGE, 
                             new ActionMessage("user.edit.done"));
                 }
             } else if (action.equals("order")) {
                 NewOrderDTO summary = new NewOrderDTO(); 
                 session.setAttribute(Constants.SESSION_ORDER_SUMMARY, 
                         summary);
                 session.setAttribute(Constants.SESSION_CUSTOMER_CONTACT_DTO,
                         userSession.getPrimaryContactDTO(userId));
                 forward = "order";
             } else {
                 log.error("action not supported" + action);
                 throw new ServletException("action is not supported :" + action);
             }
             saveMessages(request, messages);
         } catch (Exception e) {
             errors.add(ActionErrors.GLOBAL_ERROR,
                     new ActionError("all.internal"));
             log.debug("Exception:", e);
         }
         
         if (!errors.isEmpty()) {
             saveErrors(request, errors);
         }
         return mapping.findForward(forward);        
     }
 }
