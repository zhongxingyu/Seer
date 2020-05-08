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
 
 /*
  * Created on Feb 8, 2005
  *
  */
 package com.sapienter.jbilling.client.signup;
 
 import java.io.IOException;
 import java.util.Locale;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.Globals;
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 import org.apache.struts.validator.DynaValidatorForm;
 
 import com.sapienter.jbilling.common.Constants;
 import com.sapienter.jbilling.common.JNDILookup;
 import com.sapienter.jbilling.interfaces.UserSession;
 import com.sapienter.jbilling.interfaces.UserSessionHome;
 import com.sapienter.jbilling.server.entity.ContactDTO;
 import com.sapienter.jbilling.server.user.CustomerDTOEx;
 import com.sapienter.jbilling.server.user.UserDTOEx;
 
 /**
  * @author Emil
  */
 public class NewEntityAction extends Action {
     public ActionForward execute(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException {
         
         Logger log = Logger.getLogger(NewEntityAction.class);
         String action = request.getParameter("action");
         if (action == null || action.length() == 0) {
             return null;
         }
         
         String retValue = null;
         HttpSession session;
         
         if (action.equals("setup")) {
             session = request.getSession(false);
             if (session != null) {
                 session.invalidate();
             }
             session = request.getSession();
             // locale
             String language = request.getParameter("language");
             String country = request.getParameter("country");
             Locale locale = null;
             if (language == null || language.length() == 0) {
                 language = "en";
             }
             if (country == null || country.length() == 0) {
                 locale = new Locale(language);
                 country = "US";
             } else {
                 locale = new Locale(language, country);
             }
             
             
             session.setAttribute(Globals.LOCALE_KEY, locale);
             session.setAttribute("signup_language", language);
             log.debug("Setup language to " + 
                     session.getAttribute("signup_language"));
             session.setAttribute("signup_country", country);
 
             retValue = "edit";
         } else if (action.equals("edit")) {
             session = request.getSession(false);
             DynaValidatorForm myForm = (DynaValidatorForm) form;
             ActionErrors errors = new ActionErrors(myForm.validate(
                     mapping, request));
             if (!errors.isEmpty()) {
                 saveErrors(request, errors);
                 return mapping.findForward("edit");
             }
             log.debug("now doing the entity creation");
             // check this constructor out ... :p
             ContactDTO contact = new ContactDTO(
                     null, 
                     (String) myForm.get("company_name"), 
                     (String) myForm.get("address1"), 
                     (String) myForm.get("address2"), 
                     (String) myForm.get("city"), 
                     (String) myForm.get("state"), 
                     (String) myForm.get("postal_code"), 
                     (String) myForm.get("country"), 
                     (String) myForm.get("last_name"), 
                     (String) myForm.get("first_name"), 
                     null, null, null, 
                    ((String)myForm.get("phone_area")).length() == 0 ? 
                        null : Integer.valueOf((String) myForm.get("phone_area")), 
                     (String) myForm.get("phone_number"), 
                     null, null, null, 
                     (String) myForm.get("email"), 
                     null, 
                     new Integer(0), new Integer(1),null);
             
             UserDTOEx user = new UserDTOEx();
             user.setUserName((String) myForm.get("user_name"));
             user.setPassword((String) myForm.get("password"));
             user.setMainRoleId(Constants.TYPE_CUSTOMER);
             CustomerDTOEx cust = new CustomerDTOEx();
             cust.setInvoiceDeliveryMethodId(Constants.D_METHOD_EMAIL);
             user.setCustomerDto(cust);
             
             try {
                 JNDILookup EJBFactory = JNDILookup.getFactory(false);            
                 UserSessionHome UserHome =
                         (UserSessionHome) EJBFactory.lookUpHome(
                         UserSessionHome.class,
                         UserSessionHome.JNDI_NAME);
 
                 UserSession userSession = UserHome.create();
                 log.debug("Using language " + 
                         session.getAttribute("signup_language"));
                 
                 Integer entityId = userSession.createEntity(contact, user, 
                         null, null, 
                         (String) session.getAttribute("signup_language"), null);
 
                 // refresh the jbilling table 
                 RefreshBettyTable toCall = new RefreshBettyTable(null);
                 toCall.refresh();
                 
                 // store message
                 ActionMessages messages = new ActionMessages();
                 messages.add(ActionMessages.GLOBAL_MESSAGE, 
                         new ActionMessage("newEntity.welcome", entityId.toString(),
                                 user.getUserName(), user.getPassword()));
                 saveMessages(request, messages);
 
                 retValue = "done";
             } catch (Exception e) {
                 log.debug("Exception ", e);
             }
         }
 
         return mapping.findForward(retValue);
     }
 }
