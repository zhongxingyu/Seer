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
 
 package com.sapienter.jbilling.client.util;
 
 import java.rmi.RemoteException;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Vector;
 
 import javax.ejb.EJBObject;
 import javax.ejb.FinderException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.Globals;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 import org.apache.struts.action.ActionServlet;
 import org.apache.struts.config.ModuleConfig;
 import org.apache.struts.util.RequestUtils;
 import org.apache.struts.validator.DynaValidatorForm;
 import org.apache.struts.validator.Resources;
 
 import com.sapienter.jbilling.common.JNDILookup;
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.common.Util;
 import com.sapienter.jbilling.interfaces.BillingProcessSession;
 import com.sapienter.jbilling.interfaces.ItemSession;
 import com.sapienter.jbilling.interfaces.ItemSessionHome;
 import com.sapienter.jbilling.interfaces.NewOrderSession;
 import com.sapienter.jbilling.interfaces.NotificationSession;
 import com.sapienter.jbilling.interfaces.PaymentSession;
 import com.sapienter.jbilling.interfaces.PaymentSessionHome;
 import com.sapienter.jbilling.interfaces.UserSession;
 import com.sapienter.jbilling.interfaces.UserSessionHome;
 import com.sapienter.jbilling.server.entity.AchDTO;
 import com.sapienter.jbilling.server.entity.BillingProcessConfigurationDTO;
 import com.sapienter.jbilling.server.entity.CreditCardDTO;
 import com.sapienter.jbilling.server.entity.InvoiceDTO;
 import com.sapienter.jbilling.server.entity.PartnerRangeDTO;
 import com.sapienter.jbilling.server.entity.PaymentInfoChequeDTO;
 import com.sapienter.jbilling.server.item.ItemDTOEx;
 import com.sapienter.jbilling.server.item.ItemPriceDTOEx;
 import com.sapienter.jbilling.server.item.ItemTypeDTOEx;
 import com.sapienter.jbilling.server.item.ItemUserPriceDTOEx;
 import com.sapienter.jbilling.server.item.PromotionDTOEx;
 import com.sapienter.jbilling.server.notification.MessageDTO;
 import com.sapienter.jbilling.server.notification.MessageSection;
 import com.sapienter.jbilling.server.order.NewOrderDTO;
 import com.sapienter.jbilling.server.order.OrderDTOEx;
 import com.sapienter.jbilling.server.payment.PaymentDTOEx;
 import com.sapienter.jbilling.server.pluggableTask.PluggableTaskDTOEx;
 import com.sapienter.jbilling.server.pluggableTask.PluggableTaskParameterDTOEx;
 import com.sapienter.jbilling.server.pluggableTask.PluggableTaskSession;
 import com.sapienter.jbilling.server.user.ContactDTOEx;
 import com.sapienter.jbilling.server.user.PartnerDTOEx;
 import com.sapienter.jbilling.server.user.UserDTOEx;
 import com.sapienter.jbilling.server.util.OptionDTO;
 
 public class GenericMaintainAction {
     private ActionMapping mapping = null;
     private ActionServlet servlet = null;
     private HttpServletRequest request = null;
     private ActionErrors errors = null;
     private ActionMessages messages = null;
     private Logger log = null;
     private HttpSession session = null;
     private DynaValidatorForm myForm = null;
     private String action = null;
     private String mode = null;
     private EJBObject remoteSession = null;
     private String formName = null;
     // handy variables
     private Integer selectedId = null;
     private Integer languageId = null;
     private Integer entityId = null;
     private Integer executorId = null;
     
     
     public GenericMaintainAction(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response,
             ActionServlet servlet, EJBObject remoteSession,
             String formName) 
             throws Exception {
         this.mapping = mapping;
         this.request = request;
         this.remoteSession = remoteSession;
         this.servlet = servlet;
         log = Logger.getLogger(GenericMaintainAction.class);
         errors = new ActionErrors();
         messages = new ActionMessages();
         session = request.getSession(false);
         action = request.getParameter("action");
         mode = request.getParameter("mode");
         this.formName = formName;
         
         if (action == null) {
             throw new Exception("action has to be present in the request");
         }
         if (mode == null || mode.trim().length() == 0) {
             throw new Exception("mode has to be present");
         }
         if (formName == null || formName.trim().length() == 0) {
             throw new Exception("formName has to be present");
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
     }  
     
     public ActionForward process() {
         String forward = null;
         
         log.debug("processing action : " + action);
         try {
             if (action.equals("edit")) {
                 try {
                     String reset = (String) myForm.get("reset");
                     if (reset != null && reset.length() > 0) {
                         forward = reset();
                     }
                 } catch (IllegalArgumentException e) {
                 }
                 
                 if (forward == null) {
                     forward = edit();
                 } 
             } else if(action.equals("setup")) {
                 forward = setup();
             } else if(action.equals("delete")) {
                 forward = delete();
             } else {
                 log.error("Invalid action:" + action);
                 errors.add(ActionErrors.GLOBAL_ERROR,
                         new ActionError("all.internal"));
             }
         } catch (Exception e) {
             log.error("Exception ", e);
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
         
         log.debug("forwarding to " + mode + "_" + forward);
         return mapping.findForward(mode + "_" + forward);
     }
     
     private String edit() throws SessionInternalError, RemoteException {
         String retValue = "edit";
         String messageKey = null;
 
         // create a dto with the info from the form and call
         // the remote session
         ItemTypeDTOEx typeDto = null;
         ItemDTOEx itemDto = null;
         ItemUserPriceDTOEx priceDto = null;
         PromotionDTOEx promotionDto = null;
         PaymentDTOEx paymentDto = null;
         CreditCardDTO creditCardDto = null;
         AchDTO achDto = null;
         Boolean automaticPaymentType = null;
         BillingProcessConfigurationDTO configurationDto = null;
         MessageDTO messageDto = null;
         PluggableTaskDTOEx taskDto = null;
         String[] brandingData = null; 
         PartnerDTOEx partnerDto = null;
         Object[] partnerDefaultData = null;
         Integer[] notificationPreferenceData = null;
         String[] numberingData = null;
         PartnerRangeDTO[] partnerRangesData = null;
         
         // do the validation, before moving any info to the dto
         errors = new ActionErrors(myForm.validate(mapping, request));
         
         // this is a hack for items created for promotions
         if (mode.equals("item") && ((String) myForm.get("create")).equals(
                 "promotion")) {
             retValue = "promotion";
             log.debug("Processing an item for a promotion");
         }
         if (mode.equals("payment") && ((String) myForm.get("direct")).equals(
                 "yes")) {
             retValue = "fromOrder";
         }
         if (!errors.isEmpty()) {
             return(retValue);
         }                
         
         if (mode.equals("type")) {
             typeDto = new ItemTypeDTOEx();
             typeDto.setDescription((String) myForm.get("name"));
             typeDto.setOrderLineTypeId((Integer) myForm.get("order_line_type"));
             typeDto.setEntityId((Integer) session.getAttribute(
                     Constants.SESSION_ENTITY_ID_KEY));
         } else if (mode.equals("item")) { // an item
             if (request.getParameter("reload") != null) {
                 // this is just a change of language the requires a reload
                 // of the bean
                 languageId = (Integer) myForm.get("language");
                 return setup();
             }
 
             itemDto = new ItemDTOEx();
             itemDto.setDescription((String) myForm.get("description"));
             itemDto.setEntityId((Integer) session.getAttribute(
                     Constants.SESSION_ENTITY_ID_KEY));
             itemDto.setNumber((String) myForm.get("internalNumber"));
             itemDto.setPriceManual(new Integer(((Boolean) myForm.get
                     ("chbx_priceManual")).booleanValue() ? 1 : 0));
             itemDto.setTypes((Integer[]) myForm.get("types"));
             if (((String) myForm.get("percentage")).trim().length() > 0) {
                 itemDto.setPercentage(string2float(
                         (String) myForm.get("percentage")));
             }
             // because of the bad idea of using the same bean for item/type/price,
             // the validation has to be manual
             if (itemDto.getTypes().length == 0) {
                 String field = Resources.getMessage(request, "item.prompt.types"); 
                 errors.add(ActionErrors.GLOBAL_ERROR,
                         new ActionError("errors.required", field));
             }
             
             // get the prices. At least one has to be present
             itemDto.setPrices((Vector) myForm.get("prices"));
             boolean priceFlag = false;
             for (int f = 0; f < itemDto.getPrices().size(); f++) {
                 String priceStr = ((ItemPriceDTOEx)itemDto.getPrices().get(f)).
                         getPriceForm(); 
                 log.debug("Now processing item price " + f + " data:" + 
                         (ItemPriceDTOEx)itemDto.getPrices().get(f));
                 Float price = null;
                 if (priceStr != null && priceStr.trim().length() > 0) {
                     price = string2float(priceStr.trim());
                     if (price == null) {
                         String field = Resources.getMessage(request, "item.prompt.price"); 
                         errors.add(ActionErrors.GLOBAL_ERROR,
                                 new ActionError("errors.float", field));
                         break;
                     } else {
                         priceFlag = true;
                     }
                 }
                 ((ItemPriceDTOEx)itemDto.getPrices().get(f)).setPrice(
                         price);
             }
 
             // either is a percentage or a price is required.
             if (!priceFlag && itemDto.getPercentage() == null) {
                 errors.add(ActionErrors.GLOBAL_ERROR,
                         new ActionError("item.error.price"));
             }
             
         } else if (mode.equals("price")) { // a price
             priceDto = new ItemUserPriceDTOEx();
             priceDto.setPrice(string2float((String) myForm.get("price")));
             priceDto.setCurrencyId((Integer) myForm.get("currencyId"));
         } else if (mode.equals("promotion")) {
             promotionDto = new PromotionDTOEx();
             promotionDto.setCode((String) myForm.get("code"));
             promotionDto.setNotes((String) myForm.get("notes"));
             promotionDto.setOnce(new Integer(((Boolean) myForm.get
                     ("chbx_once")).booleanValue() ? 1 : 0));
             promotionDto.setSince(parseDate("since", 
                     "promotion.prompt.since"));
             promotionDto.setUntil(parseDate("until", 
                     "promotion.prompt.until"));
         } else if (mode.equals("payment")) {
             paymentDto = new PaymentDTOEx();
             // set the amount
             paymentDto.setAmount(string2float((String) myForm.get("amount")));
             // set the date
             paymentDto.setPaymentDate(parseDate("date", "payment.date"));
             if (((String) myForm.get("method")).equals("cheque")) {
                 // create the cheque dto
                 PaymentInfoChequeDTO chequeDto = new PaymentInfoChequeDTO();
                 chequeDto.setBank((String) myForm.get("bank"));
                 chequeDto.setNumber((String) myForm.get("chequeNumber"));
                 chequeDto.setDate(parseDate("chequeDate", "payment.cheque.date"));
                 // set the cheque
                 paymentDto.setCheque(chequeDto);
                 paymentDto.setMethodId(Constants.PAYMENT_METHOD_CHEQUE);
                 // validate required fields        
                 required(chequeDto.getNumber(),"payment.cheque.number");
                 required(chequeDto.getDate(), "payment.cheque.date");
                 // cheques now are never process realtime (may be later will support
                 // electronic cheques
                 paymentDto.setResultId(Constants.RESULT_ENTERED);
                 session.setAttribute("tmp_process_now", new Boolean(false));                
                 
             } else if (((String) myForm.get("method")).equals("cc")) {
                 CreditCardDTO ccDto = new CreditCardDTO();
                 ccDto.setNumber((String) myForm.get("ccNumber"));
                 ccDto.setName((String) myForm.get("ccName"));
                 myForm.set("ccExpiry_day", "01"); // to complete the date
                 ccDto.setExpiry(parseDate("ccExpiry", "payment.cc.date"));
                 if (ccDto.getExpiry() != null) {
                     // the expiry can't be past today
                     GregorianCalendar cal = new GregorianCalendar();
                     cal.setTime(ccDto.getExpiry());
                     cal.add(GregorianCalendar.MONTH, 1); // add 1 month
                     if (Calendar.getInstance().getTime().after(cal.getTime())) {
                         errors.add(ActionErrors.GLOBAL_ERROR,
                                 new ActionError("creditcard.error.expired", 
                                     "payment.cc.date"));
                     }
                 }
                 paymentDto.setCreditCard(ccDto);
                 
                 // this will be checked when the payment is sent
                 session.setAttribute("tmp_process_now", 
                         (Boolean) myForm.get("chbx_processNow"));
                 // validate required fields        
                 required(ccDto.getNumber(), "payment.cc.number");
                 required(ccDto.getExpiry(), "payment.cc.date");
                 required(ccDto.getName(), "payment.cc.name");
 
                 // make sure that the cc is valid before trying to get
                 // the payment method from it
                 if (errors.isEmpty()) {
                     paymentDto.setMethodId(Util.getPaymentMethod(
                             ccDto.getNumber()));
                 }
 
             } else if (((String) myForm.get("method")).equals("ach")) {
             	AchDTO ach = new AchDTO();
             	ach.setAbaRouting((String) myForm.get("aba_code"));
             	ach.setBankAccount((String) myForm.get("account_number"));
             	ach.setAccountType((Integer) myForm.get("account_type"));
             	ach.setBankName((String) myForm.get("bank_name"));
             	ach.setAccountName((String) myForm.get("account_name"));
             	paymentDto.setAch(ach);
                 //this will be checked when the payment is sent
                 session.setAttribute("tmp_process_now",  new Boolean(true));
                 // since it is one big form for all methods, we need to 
                 // validate the required manually
                 required(ach.getAbaRouting(), "ach.aba.prompt");
                 required(ach.getBankAccount(), "ach.account_number.prompt");
                 required(ach.getBankName(), "ach.bank_name.prompt");
                 required(ach.getAccountName(), "ach.account_name.prompt");
                 
                 if (errors.isEmpty()) {
                     paymentDto.setMethodId(Constants.PAYMENT_METHOD_ACH);
                 }
             }
             // set the customer id selected in the list (not the logged)
             paymentDto.setUserId((Integer) session.getAttribute(
                     Constants.SESSION_USER_ID));
             // specify if this is a normal payment or a refund
             paymentDto.setIsRefund(session.getAttribute("jsp_is_refund") == 
                     null ? new Integer(0) : new Integer(1));
             // set the selected payment for refunds
             if (paymentDto.getIsRefund().intValue() == 1) {
                 PaymentDTOEx refundPayment = (PaymentDTOEx) session.getAttribute(
                         Constants.SESSION_PAYMENT_DTO); 
                 /*
                  * Right now, to process a real-time credit card refund it has to be to
                  * refund a previously done credit card payment. This could be
                  * changed, to say, refund using the customer's credit card no matter
                  * how the guy paid initially. But this might be subjet to the
                  * processor features.
                  * 
                  */
                 if (((Boolean) myForm.get("chbx_processNow")).booleanValue() &&
                         ((String) myForm.get("method")).equals("cc") &&
                         (refundPayment == null || 
                          refundPayment.getCreditCard() == null ||
                          refundPayment.getAuthorization() == null ||
                          !refundPayment.getResultId().equals(Constants.RESULT_OK))) {
 
                      errors.add(ActionErrors.GLOBAL_ERROR,
                              new ActionError("refund.error.realtimeNoPayment", 
                                  "payment.cc.processNow"));
                     
                 } else {
                     paymentDto.setPayment(refundPayment);
                 }
                 // refunds, I need to manually delete the list, because
                 // in the end only the LIST_PAYMENT will be removed
                 session.removeAttribute(Constants.SESSION_LIST_KEY + 
                         Constants.LIST_TYPE_REFUND);
             }
             
             // last, set the currency
             //If a related document is
             // set (invoice/payment) it'll take it from there. Otherwise it
             // wil inherite the one from the user
             paymentDto.setCurrencyId((Integer) myForm.get("currencyId"));
             if (paymentDto.getCurrencyId() == null) {
                 try {
                     paymentDto.setCurrencyId(getUser(paymentDto.getUserId()).
                             getCurrencyId());
                 } catch (FinderException e) {
                     throw new SessionInternalError(e);
                 }
             }
             
             if (errors.isEmpty()) {
                 // verify that this entity actually accepts this kind of 
                 //payment method
                 if (!((PaymentSession) remoteSession).isMethodAccepted((Integer)
                         session.getAttribute(Constants.SESSION_ENTITY_ID_KEY),
                         paymentDto.getMethodId())) {
                     errors.add(ActionErrors.GLOBAL_ERROR,
                             new ActionError("payment.error.notAccepted", 
                                 "payment.method"));
                 }
             }
 
             // just in case there was an error
             log.debug("direct = " + (String) myForm.get("direct"));
             if (((String) myForm.get("direct")).equals("yes")) {
                 retValue = "fromOrder";
             }
 
             log.debug("now payment methodId = " + paymentDto.getMethodId());
             log.debug("now paymentDto = " + paymentDto);
             log.debug("retValue = " + retValue);
  
         } else if (mode.equals("order")) {
             // this is kind of a wierd case. The dto in the session is all
             // it is required to edit.
             NewOrderDTO summary = (NewOrderDTO) session.getAttribute(
                     Constants.SESSION_ORDER_SUMMARY);
             summary.setPeriod((Integer) myForm.get("period"));
             summary.setActiveSince(parseDate("since", 
                     "order.prompt.activeSince"));
             summary.setActiveUntil(parseDate("until", 
                     "order.prompt.activeUntil"));
             summary.setBillingTypeId((Integer) myForm.get("billingType"));
             summary.setPromoCode((String) myForm.get("promotion_code"));
             summary.setNotify(new Integer(((Boolean) myForm.
                     get("chbx_notify")).booleanValue() ? 1 : 0));
             summary.setDfFm(new Integer(((Boolean) myForm.
                     get("chbx_df_fm")).booleanValue() ? 1 : 0));
             summary.setOwnInvoice(new Integer(((Boolean) myForm.
                     get("chbx_own_invoice")).booleanValue() ? 1 : 0));
             summary.setNotesInInvoice(new Integer(((Boolean) myForm.
                     get("chbx_notes")).booleanValue() ? 1 : 0));
             summary.setNotes((String) myForm.get("notes"));
             summary.setAnticipatePeriods(getInteger("anticipate_periods"));
             summary.setPeriodStr(getOptionDescription(summary.getPeriod(),
                     Constants.PAGE_ORDER_PERIODS, session));
             summary.setBillingTypeStr(getOptionDescription(
                     summary.getBillingTypeId(),
                     Constants.PAGE_BILLING_TYPE, session));
             summary.setDueDateUnitId((Integer) myForm.get("due_date_unit_id"));
             summary.setDueDateValue(getInteger("due_date_value"));
             
             // if she wants notification, we need a date of expiration
             if (summary.getNotify().intValue() == 1 && 
             		summary.getActiveUntil() == null) {
             	errors.add(ActionErrors.GLOBAL_ERROR,
                         new ActionError("order.error.notifyWithoutDate", 
                         "order.prompt.notify"));
                 return "edit";
             }
             
             // if there is a date of expiration, it has to be grater than
             // the starting date
             if (summary.getActiveUntil() != null) {
                 Date start = summary.getActiveSince() != null ?
                         summary.getActiveSince() : 
                         Calendar.getInstance().getTime();
                 if (!summary.getActiveUntil().after(Util.truncateDate(start))) {
                     errors.add(ActionErrors.GLOBAL_ERROR,
                             new ActionError("order.error.dates", 
                             "order.prompt.activeUntil"));
                     return "edit";
                 }
             }
                     
             // now process this promotion if specified
             if (summary.getPromoCode() != null && 
                     summary.getPromoCode().length() > 0) {
                 try {
                     JNDILookup EJBFactory = JNDILookup.getFactory(false);
                     ItemSessionHome itemHome =
                             (ItemSessionHome) EJBFactory.lookUpHome(
                             ItemSessionHome.class,
                             ItemSessionHome.JNDI_NAME);
             
                     ItemSession itemSession = itemHome.create();
                     PromotionDTOEx promotion = itemSession.getPromotion(
                             (Integer) session.getAttribute(
                             Constants.SESSION_ENTITY_ID_KEY), 
                             summary.getPromoCode());
                     
                     if (promotion == null) {
                         errors.add(ActionErrors.GLOBAL_ERROR,
                                 new ActionError("promotion.error.noExist", 
                                 "order.prompt.promotion"));
                         return "edit";
                     } 
                     // if this is an update or the promotion hasn't been 
                     // used by the user
                     if (summary.getId() != null || itemSession.
                             promotionIsAvailable(promotion.getId(),
                                 summary.getUserId(), 
                                 promotion.getCode()).booleanValue()) {
                         summary = ((NewOrderSession) remoteSession).addItem(
                                 promotion.getItemId(), new Integer(1),
                                 summary.getUserId(), entityId);
                         session.setAttribute(Constants.SESSION_ORDER_SUMMARY, 
                                 summary);
                     } else {
                         errors.add(ActionErrors.GLOBAL_ERROR,
                                 new ActionError("promotion.error.alreadyUsed", 
                                 "order.prompt.promotion"));
                         return "edit";
                     }                                
                     
                                 
                 } catch (Exception e) {
                 }
             }
             
             return "items";
         } else if (mode.equals("ach")) {
         	achDto = new AchDTO();
         	achDto.setAbaRouting((String) myForm.get("aba_code"));
         	achDto.setBankAccount((String) myForm.get("account_number"));
         	achDto.setAccountType((Integer) myForm.get("account_type"));
         	achDto.setBankName((String) myForm.get("bank_name"));
         	achDto.setAccountName((String) myForm.get("account_name"));
             // update the autimatic payment type for this customer
           	automaticPaymentType = (Boolean) myForm.get("chbx_use_this");
           	
             // verify that this entity actually accepts this kind of 
             //payment method
             try {
                 JNDILookup EJBFactory = JNDILookup.getFactory(false);
                 PaymentSessionHome paymentHome =
                         (PaymentSessionHome) EJBFactory.lookUpHome(
                         PaymentSessionHome.class,
                         PaymentSessionHome.JNDI_NAME);
     
                 PaymentSession paymentSession = paymentHome.create();
                 
                 if (!paymentSession.isMethodAccepted((Integer)
                         session.getAttribute(Constants.SESSION_ENTITY_ID_KEY),
                         Constants.PAYMENT_METHOD_ACH)) {
                     errors.add(ActionErrors.GLOBAL_ERROR,
                             new ActionError("payment.error.notAccepted", 
                                 "payment.method"));
     
                 }
             } catch (Exception e) {
                 throw new SessionInternalError(e);
             }
 
         } else if (mode.equals("creditCard")) {
             creditCardDto = new CreditCardDTO();
             creditCardDto.setName((String) myForm.get("name"));
             creditCardDto.setNumber((String) myForm.get("number"));
             myForm.set("expiry_day", "01"); // to complete the date
             creditCardDto.setExpiry(parseDate("expiry", "payment.cc.date"));
 
             // validate the expiry date
             if (creditCardDto.getExpiry() != null) {            
                 GregorianCalendar cal = new GregorianCalendar();
                 cal.setTime(creditCardDto.getExpiry());
                 cal.add(GregorianCalendar.MONTH, 1); // add 1 month
                 if (Calendar.getInstance().getTime().after(cal.getTime())) {
                     errors.add(ActionErrors.GLOBAL_ERROR,
                             new ActionError("creditcard.error.expired", 
                                 "payment.cc.date"));
                 }
             }
 
             // verify that this entity actually accepts this kind of 
             //payment method
             try {
                 JNDILookup EJBFactory = JNDILookup.getFactory(false);
                 PaymentSessionHome paymentHome =
                         (PaymentSessionHome) EJBFactory.lookUpHome(
                         PaymentSessionHome.class,
                         PaymentSessionHome.JNDI_NAME);
     
                 PaymentSession paymentSession = paymentHome.create();
                 
                 if (!paymentSession.isMethodAccepted((Integer)
                         session.getAttribute(Constants.SESSION_ENTITY_ID_KEY),
                         Util.getPaymentMethod(creditCardDto.getNumber()))) {
                     errors.add(ActionErrors.GLOBAL_ERROR,
                             new ActionError("payment.error.notAccepted", 
                                 "payment.method"));
     
                 }
             } catch (Exception e) {
                 throw new SessionInternalError(e);
             }
             
             // update the autimatic payment type for this customer
           	automaticPaymentType = (Boolean) myForm.get("chbx_use_this");
           	
         } else if (mode.equals("configuration")) {
             configurationDto = new BillingProcessConfigurationDTO();
             
             configurationDto.setRetries(getInteger("retries"));
             configurationDto.setNextRunDate(parseDate("run", 
                     "process.configuration.prompt.nextRunDate"));
             configurationDto.setDaysForRetry(getInteger("retries_days"));       
             configurationDto.setDaysForReport(getInteger("report_days"));       
             configurationDto.setGenerateReport(new Integer(((Boolean) myForm.
                     get("chbx_generateReport")).booleanValue() ? 1 : 0));
             configurationDto.setDfFm(new Integer(((Boolean) myForm.
                     get("chbx_df_fm")).booleanValue() ? 1 : 0));
             configurationDto.setOnlyRecurring(new Integer(((Boolean) myForm.
                     get("chbx_only_recurring")).booleanValue() ? 1 : 0));
             configurationDto.setInvoiceDateProcess(new Integer(((Boolean) myForm.
                     get("chbx_invoice_date")).booleanValue() ? 1 : 0));
             configurationDto.setAutoPayment(new Integer(((Boolean) myForm.
                     get("chbx_auto_payment")).booleanValue() ? 1 : 0));
             configurationDto.setAutoPaymentApplication(new Integer(((Boolean) myForm.
                     get("chbx_payment_apply")).booleanValue() ? 1 : 0));
             configurationDto.setEntityId((Integer) session.getAttribute(
                     Constants.SESSION_ENTITY_ID_KEY));
             configurationDto.setPeriodUnitId(Integer.valueOf((String) myForm.
                     get("period_unit_id")));
             configurationDto.setPeriodValue(Integer.valueOf((String) myForm.
                     get("period_unit_value")));
             configurationDto.setDueDateUnitId(Integer.valueOf((String) myForm.
                     get("due_date_unit_id")));
             configurationDto.setDueDateValue(Integer.valueOf((String) myForm.
                     get("due_date_value")));
             configurationDto.setMaximumPeriods(getInteger("maximum_periods"));
             if (configurationDto.getAutoPayment().intValue() == 0 &&
                     configurationDto.getRetries().intValue() > 0) {
                 errors.add(ActionErrors.GLOBAL_ERROR,
                         new ActionError("process.configuration.error.auto"));
             }
         } else if (mode.equals("notification")) {
             if (request.getParameter("reload") != null) {
                 // this is just a change of language the requires a reload
                 // of the bean
                 languageId = (Integer) myForm.get("language");
                 return setup();
             }
             messageDto = new MessageDTO();
             messageDto.setLanguageId((Integer) myForm.get("language"));
             messageDto.setTypeId(selectedId);
             messageDto.setUseFlag((Boolean) myForm.get("chbx_use_flag"));
             // set the sections
             String sections[] = (String[]) myForm.get("sections");
             Integer sectionNumbers[] = (Integer[]) myForm.get("sectionNumbers");
             for (int f = 0; f < sections.length; f++) {
                 messageDto.addSection(new MessageSection(sectionNumbers[f], 
                         sections[f]));
                 log.debug("adding section:" + f + " "  + sections[f]);
             }
             log.debug("message is " + messageDto);
         } else if (mode.equals("parameter")) { /// for pluggable task parameters
             taskDto = (PluggableTaskDTOEx) session.getAttribute(
                     Constants.SESSION_PLUGGABLE_TASK_DTO);
             String values[] = (String[]) myForm.get("value");
             String names[] = (String[]) myForm.get("name");
             
             for (int f = 0; f < values.length; f++) {
                 PluggableTaskParameterDTOEx parameter = 
                         (PluggableTaskParameterDTOEx) taskDto.getParameters()
                             .get(f);
                 parameter.setValue(values[f]);
                 try {
                     parameter.expandValue();
                 } catch (NumberFormatException e) {
                     errors.add(ActionErrors.GLOBAL_ERROR,
                             new ActionError("task.parameter.prompt.invalid", 
                                 names[f]));
                 }
             }       
         } else if (mode.equals("branding")) {
             brandingData = new String[2];
             brandingData[0] = (String) myForm.get("css");
             brandingData[1] = (String) myForm.get("logo");
         } else if (mode.equals("invoiceNumbering")) {
             numberingData = new String[2];
             numberingData[0] = (String) myForm.get("prefix");
             numberingData[1] = (String) myForm.get("number");
         } else if (mode.equals("notificationPreference")) {
         	notificationPreferenceData = new Integer[8];
         	notificationPreferenceData[0] = new Integer(((Boolean) 
                     myForm.get("chbx_self_delivery")).booleanValue() ? 1 : 0);
         	notificationPreferenceData[1] = new Integer(((Boolean) 
                     myForm.get("chbx_show_notes")).booleanValue() ? 1 : 0);
             String field = (String) myForm.get("order_days1");
             if (field == null || field.trim().length() == 0) {
             	notificationPreferenceData[2] = null;
             } else {
             	notificationPreferenceData[2] = Integer.valueOf(field);
             }
             field = (String) myForm.get("order_days2");
             if (field == null || field.trim().length() == 0) {
             	notificationPreferenceData[3] = null; 
             } else {
                 notificationPreferenceData[3] = Integer.valueOf(field);
             }
             field = (String) myForm.get("order_days3");
             if (field == null || field.trim().length() == 0) {
                 notificationPreferenceData[4] = null; 
             } else {
                 notificationPreferenceData[4] = Integer.valueOf(field);
             }
             
             // validate that the day values are incremental
             boolean error = false;
             if (notificationPreferenceData[2] != null) {
             	if (notificationPreferenceData[3] != null) {
             		if (notificationPreferenceData[2].intValue() <=
                             notificationPreferenceData[3].intValue()) {
             			error = true;
                     }
                 }
                 if (notificationPreferenceData[4] != null) {
                     if (notificationPreferenceData[2].intValue() <=
                             notificationPreferenceData[4].intValue()) {
                         error = true;
                     }
                 }
             }
             if (notificationPreferenceData[3] != null) {
                 if (notificationPreferenceData[4] != null) {
                     if (notificationPreferenceData[3].intValue() <=
                             notificationPreferenceData[4].intValue()) {
                         error = true;
                     }
                 }
             }
             
             if (error) {
                 errors.add(ActionErrors.GLOBAL_ERROR,
                         new ActionError("notification.orderDays.error"));
             }
             
             // now get the preferences related with the invoice reminders
             field = (String) myForm.get("first_reminder");
             if (field == null || field.trim().length() == 0) {
                 notificationPreferenceData[5] = null; 
             } else {
                 notificationPreferenceData[5] = Integer.valueOf(field);
             }
             field = (String) myForm.get("next_reminder");
             if (field == null || field.trim().length() == 0) {
                 notificationPreferenceData[6] = null; 
             } else {
                 notificationPreferenceData[6] = Integer.valueOf(field);
             }
             notificationPreferenceData[7] = new Integer(((Boolean) 
                     myForm.get("chbx_invoice_reminders")).booleanValue() 
                         ? 1 : 0);
             // validate taht if the remainders are on, the parameters are there
             if (notificationPreferenceData[7].intValue() == 1 &&
                     (notificationPreferenceData[5] == null ||
                             notificationPreferenceData[6] == null)) {
                 errors.add(ActionErrors.GLOBAL_ERROR,
                         new ActionError("notification.reminders.error"));
             }
         } else if (mode.equals("partnerDefault")) {
             partnerDefaultData = new Object[8];
             if (((String) myForm.get("rate")).trim().length() > 0) {
                 partnerDefaultData[0] = string2float((String) myForm.get(
                         "rate"));
             } else {
                 partnerDefaultData[0] = null;
             }
 
             if (((String) myForm.get("fee")).trim().length() > 0) {
                 partnerDefaultData[1] = string2float((String) myForm.get(
                         "fee"));
             } else {
                 partnerDefaultData[1] = null;
             }
             partnerDefaultData[2] = (Integer) myForm.get("fee_currency");
             partnerDefaultData[3] = new Integer(((Boolean) 
                     myForm.get("chbx_one_time")).booleanValue() ? 1 : 0);
             partnerDefaultData[4] = (Integer) myForm.get("period_unit_id");
             partnerDefaultData[5] = Integer.valueOf((String) myForm.get("period_value"));
             partnerDefaultData[6] = new Integer(((Boolean) 
                     myForm.get("chbx_process")).booleanValue() ? 1 : 0);
             partnerDefaultData[7] = Integer.valueOf((String) myForm.get("clerk"));
         } else if (mode.equals("ranges")) {
             retValue = "partner"; // goes to the partner screen
             String from[] = (String[]) myForm.get("range_from");
             String to[] = (String[]) myForm.get("range_to");
             String percentage[] = (String[]) myForm.get("percentage_rate");
             String referral[] = (String[]) myForm.get("referral_fee");
             Vector ranges = new Vector();
             
             for (int f = 0; f < from.length; f++) {
                 if (from[f] != null && from[f].trim().length() > 0) {
                     PartnerRangeDTO range = new PartnerRangeDTO();
                     try {
                         range.setRangeFrom(getInteger2(from[f]));
                         range.setRangeTo(getInteger2(to[f]));
                         range.setPercentageRate(string2float(percentage[f]));
                         range.setReferralFee(string2float(referral[f]));
                         if (range.getRangeFrom() == null || range.getRangeTo() == null ||
                                 (range.getPercentageRate() == null && range.getReferralFee() == null) ||
                                 (range.getPercentageRate() != null && range.getReferralFee() != null)) {
                             errors.add(ActionErrors.GLOBAL_ERROR,
                                     new ActionError("partner.ranges.error", 
                                             new Integer(f + 1)));
                         } else {
                             ranges.add(range);
                         }
                     } catch (NumberFormatException e) {
                         errors.add(ActionErrors.GLOBAL_ERROR,
                                 new ActionError("partner.ranges.error", 
                                 new Integer(f + 1)));
                     }
                 } 
             }
             
             partnerRangesData = new PartnerRangeDTO[ranges.size()];
             ranges.toArray(partnerRangesData);
             if (errors.isEmpty()) {
                 PartnerDTOEx p = new PartnerDTOEx();
                 p.setRanges(partnerRangesData);
                 int ret = p.validateRanges();
                 if (ret == 2) {
                     errors.add(ActionErrors.GLOBAL_ERROR,
                             new ActionError("partner.ranges.error.consec"));
                 } else if (ret == 3) {
                     errors.add(ActionErrors.GLOBAL_ERROR,
                             new ActionError("partner.ranges.error.gap"));
                 }
             }
             
             if (!errors.isEmpty()) {
                 retValue = "edit";
             }
         } else if (mode.equals("partner")) {
             partnerDto = new PartnerDTOEx();
             partnerDto.setBalance(string2float((String) myForm.get(
                     "balance")));
             String optField = (String) myForm.get("rate");
             if (optField != null && optField.trim().length() > 0) {
                 partnerDto.setPercentageRate(string2float(optField));
             }
             optField = (String) myForm.get("fee");
             if (optField != null && optField.trim().length() > 0) {
                 partnerDto.setReferralFee(string2float(optField));
                 partnerDto.setFeeCurrencyId((Integer) myForm.get(
                         "fee_currency"));
             }
             partnerDto.setPeriodUnitId((Integer) myForm.get(
                     "period_unit_id"));
             partnerDto.setPeriodValue(Integer.valueOf((String) myForm.get(
                     "period_value")));
             partnerDto.setNextPayoutDate(parseDate("payout", 
                     "partner.prompt.nextPayout"));
             partnerDto.setAutomaticProcess(new Integer(((Boolean) myForm.get(
                     "chbx_process")).booleanValue() ? 1 : 0));
             partnerDto.setOneTime(new Integer(((Boolean) myForm.get(
                     "chbx_one_time")).booleanValue() ? 1 : 0));
             try {
                 Integer clerkId = Integer.valueOf((String) myForm.get(
                         "clerk"));
                 UserDTOEx clerk = getUser(clerkId);
                 if (!entityId.equals(clerk.getEntityId()) || 
                         clerk.getDeleted().intValue() == 1 ||
                         clerk.getMainRoleId().intValue() > 
                             Constants.TYPE_CLERK.intValue()) {
                     errors.add(ActionErrors.GLOBAL_ERROR,
                             new ActionError("partner.error.clerkinvalid"));
                 } else {
                     partnerDto.setRelatedClerkUserId(clerkId);
                 }
             } catch (FinderException e) {
                 errors.add(ActionErrors.GLOBAL_ERROR,
                         new ActionError("partner.error.clerknotfound"));
             }
         } else {
             throw new SessionInternalError("mode is not supported:" + mode);
         }
 
         // some errors could be added during the form->dto copy
         if (!errors.isEmpty()) {
             return(retValue);
         }                
 
         // if here the validation was successfull, procede to modify the server
         // information
         if (((String) myForm.get("create")).length() > 0) {
                     
             retValue = "create";
 
             if (mode.equals("type")) {
                 ((ItemSession) remoteSession).createType(typeDto);
                 messageKey = "item.type.create.done";
                 retValue = "list";
             } else if (mode.equals("item")) {
                 // we pass a null language, so it'll pick up the one from
                 // the entity
                 Integer newItem = ((ItemSession) remoteSession).create(
                         itemDto, null);
                 messageKey = "item.create.done";
                 retValue = "list";
                 // an item can be created to create a promotion
                 if (((String) myForm.get("create")).equals("promotion")) {
                     retValue = "promotion";
                     // the id of the new item is needed later, when the
                     // promotion record is created
                     session.setAttribute(Constants.SESSION_ITEM_ID, 
                             newItem);
                 }
                 
             } else if (mode.equals("price")) {// a price
                 // an item has just been selected from the generic list
                 priceDto.setItemId((Integer) session.getAttribute(
                         Constants.SESSION_LIST_ID_SELECTED));
                 // the user has been also selected from a list, but it has
                 // its own key in the session
                 priceDto.setUserId((Integer) session.getAttribute(
                         Constants.SESSION_USER_ID));
                 if (((ItemSession) remoteSession).createPrice(
                         executorId, priceDto) != null) {
                     messageKey = "item.user.price.create.done";
                 } else {
                     messageKey = "item.user.price.create.duplicate";
                 }
                 retValue = "list";
             } else if (mode.equals("promotion")) {
                 // this is the item that has been created for this promotion
                 promotionDto.setItemId((Integer) session.getAttribute(
                         Constants.SESSION_ITEM_ID));    
                 ((ItemSession) remoteSession).createPromotion(executorId, 
                         (Integer) session.getAttribute(
                         Constants.SESSION_ENTITY_ID_KEY), promotionDto);
                 messageKey = "promotion.create.done";
                 retValue = "list";
             } else if (mode.equals("payment")) {
                 // this is not an update, it's the previous step of the review
                 // payments have no updates (unmodifiable transactions).
                 
                 if (paymentDto.getIsRefund().intValue() == 1) {
                     session.setAttribute(Constants.SESSION_PAYMENT_DTO_REFUND, 
                             paymentDto);
                 } else {
                     session.setAttribute(Constants.SESSION_PAYMENT_DTO, paymentDto);
                 }
                 
                 if (((String) myForm.get("create")).equals("payout")) {
                     retValue = "reviewPayout";
                 } else {
                     retValue = "review";
                 }
                 messageKey = "payment.review";              
             } else if (mode.equals("partner")) {
                 // get the user dto from the session. This is the dto with the
                 // info of the user to create
                 UserDTOEx user = (UserDTOEx) session.getAttribute(
                         Constants.SESSION_CUSTOMER_DTO);
                 ContactDTOEx contact = (ContactDTOEx) session.getAttribute(
                         Constants.SESSION_CUSTOMER_CONTACT_DTO);
                 // add the partner information just submited to the user to be
                 // created
                 user.setPartnerDto(partnerDto);
                 // make the call
                 Integer newUserID = ((UserSession) remoteSession).create(user, 
                         contact);
                 log.debug("Partner created = " + newUserID);
                 session.setAttribute(Constants.SESSION_USER_ID, 
                         newUserID);
                 messageKey = "partner.created";
                 if (request.getParameter("ranges") == null) {
                     retValue = "list";
                 } else {
                     retValue = "ranges";
                 }
             }                 
         } else { // this is then an update
             retValue = "list";
             if (mode.equals("type")) {                    
                 typeDto.setId(selectedId);
                 ((ItemSession) remoteSession).updateType(executorId, typeDto);
                 messageKey = "item.type.update.done";
             } else if (mode.equals("item")) {
                 
                 itemDto.setId(selectedId);
                 ((ItemSession) remoteSession).update(executorId, itemDto, 
                         (Integer) myForm.get("language"));
                 messageKey = "item.update.done";
             } else if (mode.equals("price")) { // a price
                 priceDto.setId((Integer) myForm.get("id"));
                 ((ItemSession) remoteSession).updatePrice(executorId, priceDto);
                 messageKey = "item.user.price.update.done";
             } else if (mode.equals("promotion")) {
                 promotionDto.setId((Integer) myForm.get("id"));
                 ((ItemSession) remoteSession).updatePromotion(executorId, 
                         promotionDto);
                 messageKey = "promotion.update.done";
             } else if (mode.equals("configuration")) {
                 ((BillingProcessSession) remoteSession).
                         createUpdateConfiguration(executorId, configurationDto);
                 messageKey = "process.configuration.updated";
                 retValue = "edit";
             } else if (mode.equals("ach")) {
             	Integer userId = (Integer) session.getAttribute(
                         Constants.SESSION_USER_ID);
                 ((UserSession) remoteSession).updateACH(userId, 
                 		executorId, achDto);
                 ((UserSession) remoteSession).setAuthPaymentType(userId,
                 		Constants.AUTO_PAYMENT_TYPE_ACH, automaticPaymentType);
                 messageKey = "ach.update.done";
                 retValue = "done";                
             } else if (mode.equals("creditCard")) {
             	Integer userId = (Integer) session.getAttribute(
                         Constants.SESSION_USER_ID);
                 ((UserSession) remoteSession).updateCreditCard(executorId,
                         userId, creditCardDto);
                 ((UserSession) remoteSession).setAuthPaymentType(userId,
                 		Constants.AUTO_PAYMENT_TYPE_CC, automaticPaymentType);
                 messageKey = "creditcard.update.done";
                 retValue = "done";
             } else if (mode.equals("notification")) {
                 ((NotificationSession) remoteSession).createUpdate(
                         messageDto, entityId);
                 messageKey = "notification.message.update.done";
                 retValue = "edit";
             } else if (mode.equals("parameter")) { /// for pluggable task parameters
                 ((PluggableTaskSession) remoteSession).updateParameters(
                         taskDto);
                 messageKey = "task.parameter.update.done";
                 retValue = "edit";
             } else if (mode.equals("invoiceNumbering")) {
                 HashMap params = new HashMap();
                 params.put(Constants.PREFERENCE_INVOICE_PREFIX, 
                         numberingData[0].trim());
                 params.put(Constants.PREFERENCE_INVOICE_NUMBER, 
                         Integer.valueOf(numberingData[1]));
                 ((UserSession) remoteSession).setEntityParameters(entityId, params);
                 messageKey = "invoice.numbering.updated";
                 retValue = "edit";
             } else if (mode.equals("branding")) {
                 HashMap params = new HashMap();
                 params.put(Constants.PREFERENCE_CSS_LOCATION, brandingData[0].trim());
                 params.put(Constants.PREFERENCE_LOGO_LOCATION, brandingData[1].trim());
                 ((UserSession) remoteSession).setEntityParameters(entityId, params);
                 messageKey = "system.branding.updated";
                 retValue = "edit";
             } else  if (mode.equals("notificationPreference")) {
                 HashMap params = new HashMap();
                 params.put(Constants.PREFERENCE_PAPER_SELF_DELIVERY, 
                 		notificationPreferenceData[0]);
                 params.put(Constants.PREFERENCE_SHOW_NOTE_IN_INVOICE, 
                 		notificationPreferenceData[1]);
                 params.put(Constants.PREFERENCE_DAYS_ORDER_NOTIFICATION_S1, 
                 		notificationPreferenceData[2]);
                 params.put(Constants.PREFERENCE_DAYS_ORDER_NOTIFICATION_S2, 
                         notificationPreferenceData[3]);
                 params.put(Constants.PREFERENCE_DAYS_ORDER_NOTIFICATION_S3, 
                         notificationPreferenceData[4]);
                 params.put(Constants.PREFERENCE_FIRST_REMINDER, 
                         notificationPreferenceData[5]);
                 params.put(Constants.PREFERENCE_NEXT_REMINDER, 
                         notificationPreferenceData[6]);
                 params.put(Constants.PREFERENCE_USE_INVOICE_REMINDERS, 
                         notificationPreferenceData[7]);
                 ((UserSession) remoteSession).setEntityParameters(entityId, params);
                 messageKey = "notification.preference.update";
                 retValue = "edit";
             } else  if (mode.equals("partnerDefault")) {
                 HashMap params = new HashMap();
                 params.put(Constants.PREFERENCE_PART_DEF_RATE, partnerDefaultData[0]);
                 params.put(Constants.PREFERENCE_PART_DEF_FEE, partnerDefaultData[1]);
                 params.put(Constants.PREFERENCE_PART_DEF_FEE_CURR, partnerDefaultData[2]);
                 params.put(Constants.PREFERENCE_PART_DEF_ONE_TIME, partnerDefaultData[3]);
                 params.put(Constants.PREFERENCE_PART_DEF_PER_UNIT, partnerDefaultData[4]);
                 params.put(Constants.PREFERENCE_PART_DEF_PER_VALUE, partnerDefaultData[5]);
                 params.put(Constants.PREFERENCE_PART_DEF_AUTOMATIC, partnerDefaultData[6]);
                 params.put(Constants.PREFERENCE_PART_DEF_CLERK, partnerDefaultData[7]);
                 ((UserSession) remoteSession).setEntityParameters(entityId, params);
                 messageKey = "partner.default.updated";
                 retValue = "edit";
             } else  if (mode.equals("ranges")) {
                 PartnerDTOEx partner = (PartnerDTOEx) session.getAttribute(
                         Constants.SESSION_PARTNER_DTO);
                 partner.setRanges(partnerRangesData); 
                 ((UserSession) remoteSession).updatePartnerRanges(executorId, 
                         partner.getId(), partnerRangesData);
                 messageKey = "partner.ranges.updated";
                 retValue = "partner";
             } else  if (mode.equals("partner")) {
                 partnerDto.setId((Integer) session.getAttribute(
                         Constants.SESSION_PARTNER_ID));
                 ((UserSession) remoteSession).updatePartner(executorId,
                         partnerDto);
                 messageKey = "partner.updated";
                 if (request.getParameter("ranges") == null) {
                     retValue = "list";
                 } else {
                     retValue = "ranges";
                 }
             }
         }
         
         messages.add(ActionMessages.GLOBAL_MESSAGE, 
                 new ActionMessage(messageKey));
 
         // remove a possible list so there's no old cached list
         session.removeAttribute(Constants.SESSION_LIST_KEY + mode);
         
 
         if (retValue.equals("list")) {
             // remove the form from the session, otherwise it might show up in a later
             session.removeAttribute(formName);
         }
         
         return retValue;
     }
     
     private String setup() throws SessionInternalError, RemoteException {
         String retValue = null;
         // let's create the form bean and initialized it with the
         // data from the database
         ModuleConfig moduleConfig = RequestUtils.getModuleConfig(request,
                 servlet.getServletContext());
         myForm = (DynaValidatorForm) RequestUtils.createActionForm(
                 request, mapping, moduleConfig, servlet);
                 
         retValue = "edit";
                 
         if (mode.equals("type")) {        
             ItemTypeDTOEx dto = ((ItemSession) remoteSession).getType(
                     selectedId);
             myForm.set("name", dto.getDescription());
             myForm.set("order_line_type", dto.getOrderLineTypeId());
         } else if (mode.equals("item")) {
             // the price is actually irrelevant in this call, since it's going
             // to be overwirtten by the user's input
             // in this case the currency doesn't matter, it
             ItemDTOEx dto = ((ItemSession) remoteSession).get(selectedId, 
                     languageId, null, null, entityId);
             // the prices have to be localized
             for (int f = 0; f < dto.getPrices().size(); f++) {
                 ItemPriceDTOEx pr = (ItemPriceDTOEx) dto.getPrices().get(f);
                 pr.setPriceForm(float2string(pr.getPrice()));
             }
             myForm.set("internalNumber", dto.getNumber());
             myForm.set("description", dto.getDescription());
             myForm.set("chbx_priceManual", new Boolean(dto.
                     getPriceManual().intValue() > 0 ? 
                             true : false));
             myForm.set("types", dto.getTypes());
             myForm.set("id", dto.getId());
             myForm.set("prices", dto.getPrices());
             myForm.set("language", languageId);
             if (dto.getPercentage() != null) {
                 myForm.set("percentage", float2string(dto.getPercentage()));
             } else {
                 // otherwise it will pickup the percentage of a 
                 // previously edited item!
                 myForm.set("percentage", null);
             }
         } else if (mode.equals("price")) { // a price
             // for prices, a setup is needed when creating one, because
             // the item information is displayed
             ItemDTOEx itemDto;
             
             // to get a price I need the user and the item
             // the item: it's just been selected from a list, so it is in selectedId
             // the user:
             Integer userId = (Integer) session.getAttribute(
                     Constants.SESSION_USER_ID);
                   
             // check if I'm being called from the list of prices or from
             // a create  
             ItemUserPriceDTOEx dto;
             
             if (session.getAttribute(Constants.SESSION_ITEM_PRICE_ID) != null) {
                 // called from the prices list
                 dto  = ((ItemSession) remoteSession). getPrice(
                         (Integer) session.getAttribute(
                             Constants.SESSION_ITEM_PRICE_ID));
                 selectedId = dto.getItemId();
             } else {                
                 // called from the items list
                 dto  = ((ItemSession) remoteSession).
                         getPrice(userId, selectedId);
             }
 
             if (dto != null) { // the combination is found
                 myForm.set("id", dto.getId());
                 myForm.set("price", float2string(dto.getPrice()));
                 myForm.set("currencyId", dto.getCurrencyId());
                 // the id of the price is left in the session, so it can
                 // be used later in the delete
                 session.setAttribute(Constants.SESSION_ITEM_PRICE_ID,
                         dto.getId());
                 // as a currency, as pass just a 1 because I don't care
                 // about the price 
                 itemDto = ((ItemSession) remoteSession).get(selectedId,
                         languageId, null, new Integer(1), entityId);
             } else { // it's a create
                 // this is a create, because there no previous price for this 
                 //user-item combination.
                 // I need the currency of the user, because the price will
                 // be defaulted to this item's price
                 UserDTOEx user;
                 try {
                     user = getUser(userId);
                 } catch (FinderException e) {
                     throw new SessionInternalError(e);
                 }
                 itemDto = ((ItemSession) remoteSession).get(selectedId,
                         languageId, null, user.getCurrencyId(), entityId);
                 // We then use this item's current price
                 myForm.set("price", float2string(itemDto.getPrice()));
                 myForm.set("currencyId", user.getCurrencyId());
                 
                 retValue = "create";
             }
             // the item dto is needed, because its data is just displayed
             // with <bean>, it is not edited with <html:text>
             session.setAttribute(Constants.SESSION_ITEM_DTO, 
                     itemDto);
         } else if (mode.equals("promotion")) {
             PromotionDTOEx dto = ((ItemSession) remoteSession).
                     getPromotion(selectedId);
             myForm.set("id", dto.getId());
             myForm.set("code", dto.getCode());
             myForm.set("notes", dto.getNotes());
             myForm.set("chbx_once", new Boolean(dto.getOnce().intValue() == 1));
             // new parse the dates
             setFormDate("since", dto.getSince());
             setFormDate("until", dto.getUntil());
                              
             // the item id will be needed if the user wants to edit the 
             // item related with this promotion.
             session.setAttribute(Constants.SESSION_LIST_ID_SELECTED, 
                     dto.getItemId());
             session.setAttribute(Constants.SESSION_PROMOTION_DTO, dto);
         } else if (mode.equals("payment")) {
             CreditCardDTO ccDto = null;
             AchDTO achDto = null;
             // if an invoice was selected, pre-populate the amount field
             InvoiceDTO invoiceDto = (InvoiceDTO) session.getAttribute(
                     Constants.SESSION_INVOICE_DTO);
             PaymentDTOEx paymentDto = (PaymentDTOEx) session.getAttribute(
                     Constants.SESSION_PAYMENT_DTO);
             if (invoiceDto != null) {
                 log.debug("setting payment with invoice:" + invoiceDto.getId());
                 
                 myForm.set("amount", float2string(invoiceDto.getBalance()));
                 //paypal can't take i18n amounts
                 session.setAttribute("jsp_paypay_amount", invoiceDto.getBalance());
                 myForm.set("currencyId", invoiceDto.getCurrencyId());
                 // this actually makes the invoice available to the jsp page
                 InvoiceDTO invoices[] = new InvoiceDTO[1];
                 invoices[0] = invoiceDto;
                 session.setAttribute("jsp_linked_invoices", invoices);
             } else if (paymentDto != null) {
                 // this works for both refunds and payouts
                 log.debug("setting refund with payment:" + paymentDto.getId());
                 myForm.set("amount", float2string(paymentDto.getAmount()));
                 myForm.set("currencyId", paymentDto.getCurrencyId());
                 ccDto = paymentDto.getCreditCard();
                 achDto = paymentDto.getAch();
             } else { // this is not an invoice selected, it's the first call
                 log.debug("setting payment without invoice");
                 // the date might come handy
                 setFormDate("date", Calendar.getInstance().getTime());
                 // make the default real-time
                 myForm.set("chbx_processNow", new Boolean(true));
                 // find out if this is a payment or a refund
             }
             boolean isRefund = session.getAttribute(
                         "jsp_is_refund") != null; 
             
             if (!isRefund && ((String) myForm.get("ccNumber")).length() == 0) {
                 // normal payment, get the selected user cc
                 // if the user has a credit card, put it (this is a waste for
                 // cheques, but it really doesn't hurt)
                 log.debug("getting this user's cc");
                 UserDTOEx user;
                 try {
                     user = getUser((Integer) session.
                         getAttribute(Constants.SESSION_USER_ID));
                 } catch (FinderException e) {
                     throw new SessionInternalError(e); 
                 }
                 ccDto = user.getCreditCard();
                 achDto = user.getAch();
                 
             } 
         
             
             if (ccDto != null) {
                 myForm.set("ccNumber", ccDto.getNumber());
                 myForm.set("ccName", ccDto.getName());
                 GregorianCalendar cal = new GregorianCalendar();
                 cal.setTime(ccDto.getExpiry());
                 myForm.set("ccExpiry_month", String.valueOf(cal.get(
                         GregorianCalendar.MONTH) + 1));
                 myForm.set("ccExpiry_year", String.valueOf(cal.get(
                         GregorianCalendar.YEAR)));
             }    
             
             if (achDto != null) {
             	myForm.set("aba_code", achDto.getAbaRouting());
                 myForm.set("account_number", achDto.getBankAccount());
                 myForm.set("bank_name", achDto.getBankName());
                 myForm.set("account_name", achDto.getAccountName());
                 myForm.set("account_type", achDto.getAccountType());
             }
             
             // if this payment is direct from an order, continue with the
             // page without invoice list
             if (request.getParameter("direct") != null) {
                 // the date won't be shown, and it has to be initialized
                 setFormDate("date", Calendar.getInstance().getTime());
                 myForm.set("method", "cc");
                 
                 // add the message 
                 messages.add(ActionMessages.GLOBAL_MESSAGE,  
                         new ActionMessage("process.invoiceGenerated"));
                 retValue = "fromOrder";
             }
             
             // if this is a payout, it has its own page
             if (request.getParameter("payout") != null) {
                 retValue = "payout";
             }
 
         } else if (mode.equals("order")) {
             OrderDTOEx dto = (OrderDTOEx) session.getAttribute(
                     Constants.SESSION_ORDER_DTO);
             myForm.set("period", dto.getPeriodId());
             myForm.set("chbx_notify", new Boolean(dto.getNotify() == null ?
                     false : dto.getNotify().intValue() == 1));
             setFormDate("since", dto.getActiveSince());
             setFormDate("until", dto.getActiveUntil());
             myForm.set("due_date_unit_id", dto.getDueDateUnitId());
             myForm.set("due_date_value", dto.getDueDateValue() == null ?
                     null : dto.getDueDateValue().toString());
             myForm.set("chbx_df_fm", new Boolean(dto.getDfFm() == null ?
                     false : dto.getDfFm().intValue() == 1));
             myForm.set("chbx_own_invoice", new Boolean(dto.getOwnInvoice() == null ?
                     false : dto.getOwnInvoice().intValue() == 1));
             myForm.set("chbx_notes", new Boolean(dto.getNotesInInvoice() == null ?
                    false : dto.getNotesInInvoice().intValue() == 1));
             myForm.set("notes", dto.getNotes());
             myForm.set("anticipate_periods", dto.getAnticipatePeriods() == null ?
                     null : dto.getAnticipatePeriods().toString());
 
             myForm.set("billingType", dto.getBillingTypeId());
             if (dto.getPromoCode() != null) {
                 myForm.set("promotion_code", dto.getPromoCode());
             }
         } else if (mode.equals("ach")) {
         	Integer userId = (Integer) session.getAttribute(
                     Constants.SESSION_USER_ID);
             // now only one credit card is supported per user
             AchDTO dto = ((UserSession) remoteSession).
                     getACH(userId);
             Integer type = ((UserSession) remoteSession).getAuthPaymentType(
             		userId);
             Boolean use;
             if (type == null || !type.equals(
             		Constants.AUTO_PAYMENT_TYPE_ACH)) {
             	use = new Boolean(false);
             } else {
             	use = new Boolean(true);
             }
             if (dto != null) { // it could be that the user has no cc yet
                 myForm.set("aba_code", dto.getAbaRouting());
                 myForm.set("account_number", dto.getBankAccount());
                 myForm.set("account_type", dto.getAccountType());
                 myForm.set("bank_name", dto.getBankName());
                 myForm.set("account_name", dto.getAccountName());
                 myForm.set("chbx_use_this", use);
             } else {
                 session.removeAttribute(formName);
                 return retValue;
             }
         } else if (mode.equals("creditCard")) {
             Integer userId = (request.getParameter("userId") == null) 
                     ? (Integer) session.getAttribute(
                             Constants.SESSION_USER_ID)
                     : Integer.valueOf(request.getParameter("userId"));
             // now only one credit card is supported per user
             CreditCardDTO dto = ((UserSession) remoteSession).
                     getCreditCard(userId);
             Integer type = ((UserSession) remoteSession).getAuthPaymentType(
             		userId);
             Boolean use;
             if (type == null || !type.equals(
             		Constants.AUTO_PAYMENT_TYPE_CC)) {
             	use = new Boolean(false);
             } else {
             	use = new Boolean(true);
             }
             if (dto != null) { // it could be that the user has no cc yet
                 myForm.set("number", dto.getNumber());
                 setFormDate("expiry", dto.getExpiry());
                 myForm.set("name", dto.getName());
                 myForm.set("chbx_use_this", use);
             } else {
                 session.removeAttribute(formName);
                 return retValue;
             }
         } else if (mode.equals("configuration")) {
             BillingProcessConfigurationDTO dto = ((BillingProcessSession) remoteSession).
                     getConfigurationDto((Integer) session.getAttribute(
                         Constants.SESSION_ENTITY_ID_KEY));
             setFormDate("run", dto.getNextRunDate());
             myForm.set("chbx_generateReport", new Boolean(
                     dto.getGenerateReport().intValue() == 1));
             myForm.set("chbx_df_fm", dto.getDfFm() == null ? null : 
                 new Boolean(dto.getDfFm().intValue() == 1));
             myForm.set("chbx_only_recurring", dto.getOnlyRecurring() == null ? null : 
                 new Boolean(dto.getOnlyRecurring().intValue() == 1));
             myForm.set("chbx_invoice_date", dto.getInvoiceDateProcess() == null ? null : 
                 new Boolean(dto.getInvoiceDateProcess().intValue() == 1));
             myForm.set("chbx_auto_payment", dto.getAutoPayment() == null ? null : 
                 new Boolean(dto.getAutoPayment().intValue() == 1));
             myForm.set("chbx_payment_apply", dto.getAutoPaymentApplication() == null ? null : 
                 new Boolean(dto.getAutoPaymentApplication().intValue() == 1));
             myForm.set("retries", dto.getRetries() == null ? null :
                     dto.getRetries().toString());
             myForm.set("retries_days", dto.getDaysForRetry() == null ? null:
                     dto.getDaysForRetry().toString());
             myForm.set("report_days", dto.getDaysForReport() == null ? null :
                     dto.getDaysForReport().toString());
             myForm.set("period_unit_id", dto.getPeriodUnitId().toString());
             myForm.set("period_unit_value", dto.getPeriodValue().toString());
             myForm.set("due_date_unit_id", dto.getDueDateUnitId().toString());
             myForm.set("due_date_value", dto.getDueDateValue().toString());
             myForm.set("maximum_periods", dto.getMaximumPeriods() == null ? null:
                 dto.getMaximumPeriods().toString());
         } else if (mode.equals("notification")) {
             MessageDTO dto = ((NotificationSession) remoteSession).getDTO(
                     selectedId, languageId, entityId);
             myForm.set("language", languageId);
             myForm.set("chbx_use_flag", dto.getUseFlag());
             // now cook the sections for the form's taste
             String sections[] = new String[dto.getContent().length];
             Integer sectionNubmers[] = new Integer[dto.getContent().length];
             for (int f = 0; f < sections.length; f++) {
                 sections[f] = dto.getContent()[f].getContent();
                 sectionNubmers[f] = dto.getContent()[f].getSection();
             }
             myForm.set("sections", sections);
             myForm.set("sectionNumbers", sectionNubmers);
         } else if (mode.equals("parameter")) { /// for pluggable task parameters
             Integer type = null;
             if (request.getParameter("type").equals("notification")) {
                 type = PluggableTaskDTOEx.TYPE_EMAIL;
             }
             PluggableTaskDTOEx dto = ((PluggableTaskSession) remoteSession).
                     getDTO(type, entityId);
             // show the values in the form
             String names[] = new String[dto.getParameters().size()];
             String values[] = new String[dto.getParameters().size()];
             for (int f = 0; f < dto.getParameters().size(); f++) {
                 PluggableTaskParameterDTOEx parameter = 
                         (PluggableTaskParameterDTOEx) dto.getParameters().
                                 get(f);
                 names[f] = parameter.getName();
                 values[f] = parameter.getValue();
             }
             myForm.set("name", names);
             myForm.set("value", values);
             // this will be needed for the update                    
             session.setAttribute(Constants.SESSION_PLUGGABLE_TASK_DTO, dto);
         } else if (mode.equals("branding")) {
             // set up which preferences do we need
             Integer[] preferenceIds = new Integer[2];
             preferenceIds[0] = Constants.PREFERENCE_CSS_LOCATION;
             preferenceIds[1] = Constants.PREFERENCE_LOGO_LOCATION;
             
             // get'em
             HashMap result = ((UserSession) remoteSession).
                     getEntityParameters(entityId, preferenceIds);
             
             String css = (String) result.get(Constants.PREFERENCE_CSS_LOCATION); 
             myForm.set("css", (css == null) ? "" : css);
             String logo = (String) result.get(Constants.PREFERENCE_LOGO_LOCATION); 
             myForm.set("logo", (logo == null) ? "" : logo);
         } else if (mode.equals("invoiceNumbering")) {
             // set up[ which preferences do we need
             Integer[] preferenceIds = new Integer[2];
             preferenceIds[0] = Constants.PREFERENCE_INVOICE_PREFIX;
             preferenceIds[1] = Constants.PREFERENCE_INVOICE_NUMBER;
             
             // get'em
             HashMap result = ((UserSession) remoteSession).
                     getEntityParameters(entityId, preferenceIds);
             
             String prefix = (String) result.get(
                     Constants.PREFERENCE_INVOICE_PREFIX); 
             myForm.set("prefix", (prefix == null) ? "" : prefix);
             String number = (String) result.get(
                     Constants.PREFERENCE_INVOICE_NUMBER); 
             myForm.set("number", (number == null) ? "" : number);
         } else if (mode.equals("notificationPreference")) {
             Integer[] preferenceIds = new Integer[8];
             preferenceIds[0] = Constants.PREFERENCE_PAPER_SELF_DELIVERY;
             preferenceIds[1] = Constants.PREFERENCE_SHOW_NOTE_IN_INVOICE;
             preferenceIds[2] = Constants.PREFERENCE_DAYS_ORDER_NOTIFICATION_S1;
             preferenceIds[3] = Constants.PREFERENCE_DAYS_ORDER_NOTIFICATION_S2;
             preferenceIds[4] = Constants.PREFERENCE_DAYS_ORDER_NOTIFICATION_S3;
             preferenceIds[5] = Constants.PREFERENCE_FIRST_REMINDER;
             preferenceIds[6] = Constants.PREFERENCE_NEXT_REMINDER;
             preferenceIds[7] = Constants.PREFERENCE_USE_INVOICE_REMINDERS;
             
             // get'em
             HashMap result = ((UserSession) remoteSession).
                     getEntityParameters(entityId, preferenceIds);
             
             String value = (String) result.get(
             		Constants.PREFERENCE_PAPER_SELF_DELIVERY); 
             myForm.set("chbx_self_delivery", new Boolean(value.equals("1") 
                     ? true : false));
             value = (String) result.get(
             		Constants.PREFERENCE_SHOW_NOTE_IN_INVOICE); 
             myForm.set("chbx_show_notes", new Boolean(value.equals("1") 
                     ? true : false));
             value = (String) result.get(
             		Constants.PREFERENCE_DAYS_ORDER_NOTIFICATION_S1);
             myForm.set("order_days1", value);
             value = (String) result.get(
                     Constants.PREFERENCE_DAYS_ORDER_NOTIFICATION_S2);
             myForm.set("order_days2", value);
             value = (String) result.get(
                     Constants.PREFERENCE_DAYS_ORDER_NOTIFICATION_S3);
             myForm.set("order_days3", value);
             value = (String) result.get(
                     Constants.PREFERENCE_FIRST_REMINDER);
             myForm.set("first_reminder", value);
             value = (String) result.get(
                     Constants.PREFERENCE_NEXT_REMINDER);
             myForm.set("next_reminder", value);
             value = (String) result.get(
                     Constants.PREFERENCE_USE_INVOICE_REMINDERS); 
             myForm.set("chbx_invoice_reminders", new Boolean(value.equals("1") 
                     ? true : false));
 
         } else if (mode.equals("partnerDefault")) {
             // set up[ which preferences do we need
             Integer[] preferenceIds = new Integer[8];
             preferenceIds[0] = Constants.PREFERENCE_PART_DEF_RATE;
             preferenceIds[1] = Constants.PREFERENCE_PART_DEF_FEE;
             preferenceIds[2] = Constants.PREFERENCE_PART_DEF_FEE_CURR;
             preferenceIds[3] = Constants.PREFERENCE_PART_DEF_ONE_TIME;
             preferenceIds[4] = Constants.PREFERENCE_PART_DEF_PER_UNIT;
             preferenceIds[5] = Constants.PREFERENCE_PART_DEF_PER_VALUE;
             preferenceIds[6] = Constants.PREFERENCE_PART_DEF_AUTOMATIC;
             preferenceIds[7] = Constants.PREFERENCE_PART_DEF_CLERK;
             
             // get'em
             HashMap result = ((UserSession) remoteSession).
                     getEntityParameters(entityId, preferenceIds);
             
             String value;
             value = (String) result.get(Constants.PREFERENCE_PART_DEF_RATE); 
             myForm.set("rate", (value == null) ? "" : value);
             value = (String) result.get(Constants.PREFERENCE_PART_DEF_FEE); 
             myForm.set("fee", (value == null) ? "" : value);
             value = (String) result.get(Constants.PREFERENCE_PART_DEF_FEE_CURR); 
             myForm.set("fee_currency", Integer.valueOf(value));
             value = (String) result.get(Constants.PREFERENCE_PART_DEF_ONE_TIME); 
             myForm.set("chbx_one_time", new Boolean(value.equals("1") 
                     ? true : false));
             value = (String) result.get(Constants.PREFERENCE_PART_DEF_PER_UNIT); 
             myForm.set("period_unit_id", Integer.valueOf(value));
             value = (String) result.get(Constants.PREFERENCE_PART_DEF_PER_VALUE); 
             myForm.set("period_value", (value == null) ? "" : value);
             value = (String) result.get(Constants.PREFERENCE_PART_DEF_AUTOMATIC); 
             myForm.set("chbx_process", new Boolean(value.equals("1") 
                     ? true : false));
             value = (String) result.get(Constants.PREFERENCE_PART_DEF_CLERK); 
             myForm.set("clerk", (value == null) ? "" : value);
         } else if (mode.equals("ranges")) {
             PartnerDTOEx partner = (PartnerDTOEx) session.getAttribute(
                     Constants.SESSION_PARTNER_DTO);
             PartnerRangeDTO ranges[] = partner.getRanges();
             String arr1[] = new String[20];
             String arr2[] = new String[20];
             String arr3[] = new String[20];
             String arr4[] = new String[20];
             // add 20 ranges to the session for edition
             for (int f = 0; f < 20; f++) {
                 if (ranges != null && ranges.length > f) {
                     arr1[f] = ranges[f].getRangeFrom().toString();
                     arr2[f] = ranges[f].getRangeTo().toString();
                     arr3[f] = float2string(ranges[f].getPercentageRate());
                     arr4[f] = float2string(ranges[f].getReferralFee());
                 } else {
                     arr1[f] = null;
                     arr2[f] = null;
                     arr3[f] = null;
                     arr4[f] = null;
                 }
             }
             
             myForm.set("range_from", arr1);
             myForm.set("range_to", arr2);
             myForm.set("percentage_rate", arr3);
             myForm.set("referral_fee", arr4);
         } else if (mode.equals("partner")) {
             Integer partnerId = (Integer) session.getAttribute(
                     Constants.SESSION_PARTNER_ID);
             
             PartnerDTOEx partner;
             if (partnerId != null) {
                 try {
                     partner = ((UserSession) remoteSession).getPartnerDTO(
                             partnerId);
                 } catch (FinderException e) {
                     throw new SessionInternalError(e);
                 }
             } else {
                 partner = new PartnerDTOEx();
                 // set the values from the preferences (defaults)
                 Integer[] preferenceIds = new Integer[8];
                 preferenceIds[0] = Constants.PREFERENCE_PART_DEF_RATE;
                 preferenceIds[1] = Constants.PREFERENCE_PART_DEF_FEE;
                 preferenceIds[2] = Constants.PREFERENCE_PART_DEF_FEE_CURR;
                 preferenceIds[3] = Constants.PREFERENCE_PART_DEF_ONE_TIME;
                 preferenceIds[4] = Constants.PREFERENCE_PART_DEF_PER_UNIT;
                 preferenceIds[5] = Constants.PREFERENCE_PART_DEF_PER_VALUE;
                 preferenceIds[6] = Constants.PREFERENCE_PART_DEF_AUTOMATIC;
                 preferenceIds[7] = Constants.PREFERENCE_PART_DEF_CLERK;
             
                  // get'em
                 HashMap result = ((UserSession) remoteSession).
                          getEntityParameters(entityId, preferenceIds);
                 String value;
                 value = (String) result.get(Constants.PREFERENCE_PART_DEF_RATE);
                 if (value != null && value.trim().length() > 0) { 
                     partner.setPercentageRate(string2float(value));
                 }
                 value = (String) result.get(Constants.PREFERENCE_PART_DEF_FEE);
                 if (value != null && value.trim().length() > 0) { 
                     partner.setReferralFee(string2float(value));
                     value = (String) result.get(
                             Constants.PREFERENCE_PART_DEF_FEE_CURR);
                     partner.setFeeCurrencyId(Integer.valueOf(value));
                 }
                 value = (String) result.get(Constants.PREFERENCE_PART_DEF_ONE_TIME);
                 partner.setOneTime(Integer.valueOf(value));
                 value = (String) result.get(Constants.PREFERENCE_PART_DEF_PER_UNIT);
                 partner.setPeriodUnitId(Integer.valueOf(value));
                 value = (String) result.get(Constants.PREFERENCE_PART_DEF_PER_VALUE);
                 partner.setPeriodValue(Integer.valueOf(value));
                 value = (String) result.get(Constants.PREFERENCE_PART_DEF_AUTOMATIC);
                 partner.setAutomaticProcess(Integer.valueOf(value));
                 value = (String) result.get(Constants.PREFERENCE_PART_DEF_CLERK);
                 partner.setRelatedClerkUserId(Integer.valueOf(value));
                 // some that are not preferences
                 partner.setBalance(new Float(0));
                 retValue = "create";
             }
             myForm.set("balance", float2string(partner.getBalance()));
             if (partner.getPercentageRate() != null) {
                 myForm.set("rate", float2string(partner.getPercentageRate()));
             }
             if (partner.getReferralFee() != null) {
                 myForm.set("fee", float2string(partner.getReferralFee()));
             }
             myForm.set("fee_currency", partner.getFeeCurrencyId());
             myForm.set("chbx_one_time", new Boolean(
                     partner.getOneTime().intValue() == 1));
             myForm.set("period_unit_id", partner.getPeriodUnitId());
             myForm.set("period_value", partner.getPeriodValue().toString());
             myForm.set("chbx_process", new Boolean(
                     partner.getAutomaticProcess().intValue() == 1));
             myForm.set("clerk", partner.getRelatedClerkUserId().toString());
             setFormDate("payout", partner.getNextPayoutDate());
         } else {
             throw new SessionInternalError("mode is not supported:" + mode);
         }
         
         log.debug("setup mode=" + mode + " form name=" + formName + 
                 " dyna=" + myForm);
                 
         session.setAttribute(formName, myForm);
         
         return retValue;
     }
     
     private String delete() throws SessionInternalError, RemoteException {
         String retValue = null;
         
         if (mode.equals("type")) {    
             ((ItemSession) remoteSession).deleteType(executorId, 
                     selectedId);
         } else if (mode.equals("item")) {
             ((ItemSession) remoteSession).delete(executorId, selectedId);
         } else if (mode.equals("price")) { // it's a price
             ((ItemSession) remoteSession).deletePrice(executorId, 
                     (Integer) session.getAttribute(
                         Constants.SESSION_ITEM_PRICE_ID));
         } else if (mode.equals("promotion")) {
             Integer promotionId = ((PromotionDTOEx) session.getAttribute(
                     Constants.SESSION_PROMOTION_DTO)).getId();
             ((ItemSession) remoteSession).deletePromotion(executorId, 
                     promotionId);
         } else if (mode.equals("creditCard")) {
             ((UserSession) remoteSession).deleteCreditCard(executorId, 
                     (Integer) session.getAttribute(Constants.SESSION_USER_ID));
             // no need to modify the auto payment type. If it is cc and
             // there's no cc the payment will be bypassed
         } else if (mode.equals("ach")) {
             ((UserSession) remoteSession).removeACH(
             		(Integer) session.getAttribute(Constants.SESSION_USER_ID),
 					executorId);
         }
                 
         session.removeAttribute(formName); 
         retValue = "deleted";
         
         // remove a possible list so there's no old cached list
         session.removeAttribute(Constants.SESSION_LIST_KEY + mode);
         
         return retValue;
     }
     
     /*
      * 
      */
     private String reset() throws SessionInternalError {
         String retValue = "edit";
         
         myForm.initialize(mapping);
         
         if (mode.equals("payment")) {
             session.removeAttribute(Constants.SESSION_INVOICE_DTO);
             session.removeAttribute(Constants.SESSION_PAYMENT_DTO);
         }
         
         return retValue;
     }
     
     private Date parseDate(String prefix, String prompt) {
         Date date = null;
         String year = (String) myForm.get(prefix + "_year");
         String month = (String) myForm.get(prefix + "_month");
         String day = (String) myForm.get(prefix + "_day");
         
         // if one of the fields have been entered, all should've been
         if ((year.length() > 0 && (month.length() <= 0 || day.length() <= 0)) ||
             (month.length() > 0 && (year.length() <= 0 || day.length() <= 0)) ||
             (day.length() > 0 && (month.length() <= 0 || year.length() <= 0)) ) {
             // get the localized name of this field
             String field = Resources.getMessage(request, prompt); 
             errors.add(ActionErrors.GLOBAL_ERROR,
                     new ActionError("errors.incomplete.date", field));
             return null;
         }
         if (year.length() > 0 && month.length() > 0 && day.length() > 0) {
             try {
                 date = Util.getDate(Integer.valueOf(year), 
                         Integer.valueOf(month), Integer.valueOf(day));
             } catch (Exception e) {
                 log.info("Exception when converting the fields to integer", e);
                 date = null;
             }
             if (date == null) {
                 // get the localized name of this field
                 String field = Resources.getMessage(request, prompt); 
                 errors.add(ActionErrors.GLOBAL_ERROR,
                         new ActionError("errors.date", field));
             } 
         }
         
         return date;
     }
     
     private void setFormDate(String prefix, Date date) {
         if (date != null) {
             GregorianCalendar cal = new GregorianCalendar();
             cal.setTime(date);
             myForm.set(prefix + "_month", String.valueOf(cal.get(
                     GregorianCalendar.MONTH) + 1));
             myForm.set(prefix + "_day", String.valueOf(cal.get(
                     GregorianCalendar.DAY_OF_MONTH)));
             myForm.set(prefix + "_year", String.valueOf(cal.get(
                     GregorianCalendar.YEAR)));
         } else {
             myForm.set(prefix + "_month", null);
             myForm.set(prefix + "_day", null);
             myForm.set(prefix + "_year", null);
         }
     }
     
     private void required(String field, String key) {
         if (field == null || field.trim().length() == 0) {
             String name = Resources.getMessage(request, key);
             errors.add(ActionErrors.GLOBAL_ERROR,
                     new ActionError("errors.required", name));
         }
     }
     
     private void required(Date field, String key) {
         if (field == null) {
             String name = Resources.getMessage(request, key);
             errors.add(ActionErrors.GLOBAL_ERROR,
                     new ActionError("errors.required", name));
         }
     }
 
     public static void cleanUpSession(HttpSession session) {
         Enumeration entries = session.getAttributeNames();
         for (String entry = (String)entries.nextElement(); 
                 entries.hasMoreElements();
                 entry = (String)entries.nextElement()) {
             if (!entry.startsWith("sys_") && !entry.startsWith("org.apache.struts")) {
                 //Logger.getLogger(GenericMaintainAction.class).debug("removing " + entry);
                 session.removeAttribute(entry);
                 // you can't modify the colleciton and keep iterating with the
                 // same reference (doahhh :p )
                 entries = session.getAttributeNames();
             }                
         }
         
     }        
 
     public static String getOptionDescription(Integer id, String optionType,
             HttpSession session) throws SessionInternalError {
         Vector options = (Vector) session.getAttribute("SESSION_" + 
                 optionType);
         if (options == null) {
             throw new SessionInternalError("can't find the vector of options" +
                     " in the session:" + optionType);
         }
         
         OptionDTO option;
         for (int f=0; f < options.size(); f++) {
             option = (OptionDTO) options.get(f);
             if (option.getCode().compareTo(id.toString()) == 0) {
                 return option.getDescription();
             }
         }
         
         throw new SessionInternalError("id " + id + " not found in options " +
                 optionType);
     }
 
     private UserDTOEx getUser(Integer userId) 
             throws SessionInternalError, FinderException {    
         UserDTOEx retValue = null;
         try {
             JNDILookup EJBFactory = JNDILookup.getFactory(false);
             UserSessionHome userHome =
                     (UserSessionHome) EJBFactory.lookUpHome(
                     UserSessionHome.class,
                     UserSessionHome.JNDI_NAME);
             UserSession userSession = userHome.create();
                     
             retValue = userSession.getUserDTOEx(userId); 
         } catch (FinderException e) {
             throw new FinderException();
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
         
         return retValue;
     }
     
     private Integer getInteger(String fieldName) {
         String field = (String) myForm.get(fieldName);
         return getInteger2(field);
     }
     
     private Integer getInteger2(String str) {
         Integer retValue;
         if (str != null && str.trim().length() > 0) {
             retValue = Integer.valueOf(str);
         } else {
             retValue = null;
         }
         
         return retValue;
     }
     
     private String float2string(Float arg) {
         return float2string(arg,session);
     }
 
     public static String float2string(Float arg, HttpSession sess) {
         if (arg == null) {
             return null;
         }
         UserDTOEx user = (UserDTOEx) sess.getAttribute(
                 Constants.SESSION_USER_DTO);
         NumberFormat nf = NumberFormat.getInstance(user.getLocale());
         if (nf instanceof DecimalFormat) {
             ((DecimalFormat) nf).applyPattern("0.00");
         }
         return nf.format(arg);
     }
 
     private Float string2float(String arg) {
         return string2float(arg, session);
     }
     
     public static Float string2float(String arg, HttpSession sess) {
         if (arg == null || arg.trim().length() == 0) {
             return null;
         }
         UserDTOEx user = (UserDTOEx) sess.getAttribute(
                 Constants.SESSION_USER_DTO);
         NumberFormat nf = NumberFormat.getInstance(user.getLocale());
         
         try {
             return new Float(nf.parse(arg).floatValue());
         } catch (ParseException e) {
             return null;
         }
     }
 }
