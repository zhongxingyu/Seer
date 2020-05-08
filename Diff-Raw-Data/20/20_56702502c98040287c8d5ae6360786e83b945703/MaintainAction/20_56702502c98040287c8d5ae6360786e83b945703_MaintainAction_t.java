 /*
     jBilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2008 Enterprise jBilling Software Ltd. and Emiliano Conde
 
     This file is part of jbilling.
 
     jbilling is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     jbilling is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
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
 import com.sapienter.jbilling.server.order.db.OrderDTO;
 import com.sapienter.jbilling.server.user.UserDTOEx;
 import com.sapienter.jbilling.server.user.db.CompanyDTO;
 import com.sapienter.jbilling.server.user.db.CustomerDTO;
 import com.sapienter.jbilling.server.user.db.UserDTO;
 import com.sapienter.jbilling.server.user.partner.db.Partner;
 import com.sapienter.jbilling.server.util.db.CurrencyDTO;
 import com.sapienter.jbilling.server.util.db.LanguageDTO;
 import com.sapienter.jbilling.server.util.db.generated.InvoiceDeliveryMethod;
 
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
                 // remove any cached list of sub-account, so they
                 // don't mixed up among parents
                 session.removeAttribute(Constants.SESSION_LIST_KEY + 
                         Constants.LIST_TYPE_SUB_ACCOUNTS);
                 // now put the user data in the session for display
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
                 
                 //XXX
                 //Since passwords are stored encrypted, we can not do this check at client side anymore
                 //It should not be critical, because user is already authenticated
                 //and probably knows what to do.
                 //XXX: see commented block below
                 /**
 				 * <code>
 				 * // test that the old password is correct if this is a self-update
 				 * if (updatePassword && userId.equals(executorId) &&
 				 * 		!userDto.getPassword().equals((String) userForm.get(
 				 * 			"oldPassword"))) {
 				 * 
 				 * 		errors.add(ActionErrors.GLOBAL_ERROR,
 				 * 			new ActionError("user.edit.error.invalidOldPassword"));
 				 * }
 				 * </code>
 				 */
                 
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
                     dto.setCompany(new CompanyDTO((Integer) userForm.get("entity")));
                     dto.setMainRoleId((Integer) userForm.get("type"));
                     dto.setUserName((String) userForm.get("username"));
                     if (updatePassword) {
                         dto.setPassword((String) userForm.get("password"));
                     } else {
                         dto.setPassword(null);
                     }
                     dto.setLanguage(new LanguageDTO((Integer) userForm.get("language")));
                     dto.setStatusId((Integer) userForm.get("status"));
                     dto.setCurrency(new CurrencyDTO((Integer) userForm.get("currencyId")));
                     dto.setSubscriptionStatusId((Integer) 
                             userForm.get("subscriberStatus"));
 
                     
                     if (dto.getMainRoleId().equals(Constants.TYPE_CUSTOMER)) {
                         dto.setCustomer(new CustomerDTO());
                         dto.getCustomer().setInvoiceDeliveryMethod(new InvoiceDeliveryMethod(
                                 (Integer) userForm.get("deliveryMethodId")));
                         dto.getCustomer().setDueDateUnitId(
                                 (Integer) userForm.get("due_date_unit_id"));
                         String value = (String) userForm.get("due_date_value");
                         if (value != null && value.length() > 0) {
                             dto.getCustomer().setDueDateValue(
                                     Integer.valueOf(value));
                         } else {
                             dto.getCustomer().setDueDateValue(null);
                         }
                         dto.getCustomer().setDfFm(new Integer(((Boolean)
                                     userForm.get("chbx_df_fm")).booleanValue() 
                                         ? 1 : 0));
                         dto.getCustomer().setExcludeAging(new Integer(((Boolean)
                                     userForm.get("chbx_excludeAging")).booleanValue() 
                                         ? 1 : 0));
                         
                         if (partnerId != null && partnerId.length() > 0) {
                             dto.getCustomer().setPartner(new Partner(Integer.valueOf(
                                 partnerId)));
                         } else {
                             dto.getCustomer().setPartner(null);
                         }
                     }
                     
                     // I pass who am I and the info to update
                     userSession.update(executorId, dto);
                     
                     messages.add(ActionMessages.GLOBAL_MESSAGE, 
                             new ActionMessage("user.edit.done"));
                 }
             } else if (action.equals("order")) {
                 OrderDTO summary = new OrderDTO(); 
                 session.setAttribute(Constants.SESSION_ORDER_SUMMARY, 
                         summary);
                 session.setAttribute(Constants.SESSION_CUSTOMER_CONTACT_DTO,
                         userSession.getPrimaryContactDTO(userId));
                 forward = "order";
 
             // blacklist add/remove
             } else if (action.equals("blacklist_add") || action.equals("blacklist_remove")) {
                 Integer blacklistUserId = Integer.parseInt(request.getParameter("userId"));
                 if (action.equals("blacklist_add")) {
                     userSession.setUserBlacklisted(executorId, userId, true);
                     messages.add(ActionMessages.GLOBAL_MESSAGE, 
                             new ActionMessage("blacklist.user.add.done"));
                 } else {
                     userSession.setUserBlacklisted(executorId, userId, false);
                     messages.add(ActionMessages.GLOBAL_MESSAGE, 
                             new ActionMessage("blacklist.user.remove.done"));
                 }
 
                 forward="userView";
 
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
