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
 
 /*
  * Created on Jan 27, 2005
  * One session bean to expose as a single web service, thus, one wsdl
  */
 package com.sapienter.jbilling.server.util;
 
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import javax.jws.WebService;
 import javax.naming.NamingException;
 
 import org.apache.commons.validator.ValidatorException;
 import org.apache.log4j.Logger;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import sun.jdbc.rowset.CachedRowSet;
 
 import com.sapienter.jbilling.common.GatewayBL;
 import com.sapienter.jbilling.common.JBCrypto;
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.server.invoice.InvoiceBL;
 import com.sapienter.jbilling.server.invoice.InvoiceWS;
 import com.sapienter.jbilling.server.invoice.db.InvoiceDAS;
 import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
 import com.sapienter.jbilling.server.item.IItemSessionBean;
 import com.sapienter.jbilling.server.item.ItemBL;
 import com.sapienter.jbilling.server.item.ItemDTOEx;
 import com.sapienter.jbilling.server.item.PricingField;
 import com.sapienter.jbilling.server.item.db.ItemDTO;
 import com.sapienter.jbilling.server.mediation.Record;
 import com.sapienter.jbilling.server.order.OrderBL;
 import com.sapienter.jbilling.server.order.OrderLineWS;
 import com.sapienter.jbilling.server.order.OrderWS;
 import com.sapienter.jbilling.server.order.db.OrderDAS;
 import com.sapienter.jbilling.server.order.db.OrderDTO;
 import com.sapienter.jbilling.server.order.db.OrderLineDTO;
 import com.sapienter.jbilling.server.payment.IPaymentSessionBean;
 import com.sapienter.jbilling.server.payment.PaymentAuthorizationDTOEx;
 import com.sapienter.jbilling.server.payment.PaymentBL;
 import com.sapienter.jbilling.server.payment.PaymentDTOEx;
 import com.sapienter.jbilling.server.payment.PaymentWS;
 import com.sapienter.jbilling.server.payment.db.PaymentMethodDAS;
 import com.sapienter.jbilling.server.pluggableTask.TaskException;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
 import com.sapienter.jbilling.server.process.BillingProcessBL;
 import com.sapienter.jbilling.server.process.db.BillingProcessConfigurationDAS;
 import com.sapienter.jbilling.server.process.db.BillingProcessConfigurationDTO;
 import com.sapienter.jbilling.server.process.db.BillingProcessDTO;
 import com.sapienter.jbilling.server.user.ContactBL;
 import com.sapienter.jbilling.server.user.ContactDTOEx;
 import com.sapienter.jbilling.server.user.ContactWS;
 import com.sapienter.jbilling.server.user.CreateResponseWS;
 import com.sapienter.jbilling.server.user.CreditCardBL;
 import com.sapienter.jbilling.server.user.IUserSessionBean;
 import com.sapienter.jbilling.server.user.UserBL;
 import com.sapienter.jbilling.server.user.UserDTOEx;
 import com.sapienter.jbilling.server.user.UserTransitionResponseWS;
 import com.sapienter.jbilling.server.user.UserWS;
 import com.sapienter.jbilling.server.user.db.CompanyDTO;
 import com.sapienter.jbilling.server.user.db.CreditCardDAS;
 import com.sapienter.jbilling.server.user.db.CreditCardDTO;
 import com.sapienter.jbilling.server.user.db.UserDAS;
 import com.sapienter.jbilling.server.user.db.UserDTO;
 import com.sapienter.jbilling.server.util.api.WebServicesConstants;
 import com.sapienter.jbilling.server.util.audit.EventLogger;
 import com.sapienter.jbilling.server.util.db.CurrencyDAS;
 import java.math.BigDecimal;
 
 @Transactional( propagation = Propagation.REQUIRED )
 @WebService( endpointInterface = "com.sapienter.jbilling.server.util.IWebServicesSessionBean" )
 public class WebServicesSessionSpringBean implements IWebServicesSessionBean {
 
     private static final Logger LOG = Logger.getLogger(
             WebServicesSessionSpringBean.class);
 
     /*
      * INVOICES
      */
     public InvoiceWS getInvoiceWS(Integer invoiceId)
             throws SessionInternalError {
         LOG.debug("Call to getInvoiceWS " + invoiceId);
         try {
             if (invoiceId == null) {
                 return null;
             }
             InvoiceDTO invoice = new InvoiceDAS().find(invoiceId);
             
             if (invoice.getDeleted() == 1 || invoice.getIsReview() == 1) {
                 return null;
             }
 
             LOG.debug("Done");
             return new InvoiceBL().getWS(invoice);
         } catch (Exception e) {
             LOG.error("WS - getInvoiceWS", e);
             throw new SessionInternalError("Error getting invoice");
         }
     }
 
     public InvoiceWS getLatestInvoice(Integer userId)
             throws SessionInternalError {
         LOG.debug("Call to getLatestInvoice " + userId);
         InvoiceWS retValue = null;
         try {
             if (userId == null) {
                 return null;
             }
             InvoiceBL bl = new InvoiceBL();
             Integer invoiceId = bl.getLastByUser(userId);
             if (invoiceId != null) {
                 retValue = bl.getWS(new InvoiceDAS().find(invoiceId));
             }
             LOG.debug("Done");
             return retValue;
         } catch (Exception e) {
             LOG.error("Exception in web service: getting latest invoice" +
                     " for user " + userId, e);
             throw new SessionInternalError("Error getting latest invoice");
         }
     }
 
     public Integer[] getLastInvoices(Integer userId, Integer number)
             throws SessionInternalError {
         LOG.debug("Call to getLastInvoices " + userId + " " + number);
         try {
             if (userId == null || number == null) {
                 return null;
             }
 
             InvoiceBL bl = new InvoiceBL();
             LOG.debug("Done");
             return bl.getManyWS(userId, number);
         } catch (Exception e) {
             LOG.error("Exception in web service: getting last invoices" +
                     " for user " + userId, e);
             throw new SessionInternalError("Error getting last invoices");
         }
     }
 
     public Integer[] getInvoicesByDate(String since, String until)
             throws SessionInternalError {
         LOG.debug("Call to getInvoicesByDate " + since + " " + until);
         try {
             Date dSince = com.sapienter.jbilling.common.Util.parseDate(since);
             Date dUntil = com.sapienter.jbilling.common.Util.parseDate(until);
             if (since == null || until == null) {
                 return null;
             }
 
             Integer entityId = getCallerCompanyId();
 
             InvoiceBL invoiceBl = new InvoiceBL();
             LOG.debug("Done");
             return invoiceBl.getInvoicesByCreateDateArray(entityId, dSince, dUntil);
         } catch (Exception e) {
             LOG.error("Exception in web service: getting invoices by date" +
                     since + until, e);
             throw new SessionInternalError("Error getting last invoices");
         }
     }
 
     /**
      * Deletes an invoice 
      * @param invoiceId
      * The id of the invoice to delete
      */
     public void deleteInvoice(Integer invoiceId) {
         LOG.debug("Call to deleteInvoice " + invoiceId);
         try {
             Integer executorId = getCallerId();
             InvoiceBL invoice = new InvoiceBL(invoiceId);
             invoice.delete(executorId);
             LOG.debug("Done");
         } catch (Exception e) {
             LOG.error("WS - deleteUser", e);
             throw new SessionInternalError("Error deleting user");
         }
     }
 
     /**
      * Generates invoices for orders not yet invoiced for this user.
      * Optionally only allow recurring orders to generate invoices. 
      * Returns the ids of the invoices generated. 
      */
     public Integer[] createInvoice(Integer userId, boolean onlyRecurring)
             throws SessionInternalError {
         LOG.debug("Call to createInvoice - userId: " + userId + 
                 " onlyRecurring: " + onlyRecurring);
         try {
             UserDTO user = new UserDAS().find(userId);
             BillingProcessBL processBL = new BillingProcessBL();
 
             BillingProcessConfigurationDTO config = 
                     new BillingProcessConfigurationDAS().findByEntity(
                     user.getCompany());
 
             // Create a mock billing process object, because the method 
             // we are calling was meant to be called by the billing process. 
             BillingProcessDTO billingProcess = new BillingProcessDTO();
             billingProcess.setId(0);
             billingProcess.setEntity(user.getCompany());
             billingProcess.setBillingDate(new Date());
             billingProcess.setPeriodUnit(config.getPeriodUnit());
             billingProcess.setPeriodValue(config.getPeriodValue());
             billingProcess.setIsReview(0);
             billingProcess.setRetriesToDo(0);
 
             InvoiceDTO[] newInvoices = processBL.generateInvoice(billingProcess,
                     user, false, onlyRecurring);
 
             if (newInvoices != null) {
                 Integer[] invoiceIds = new Integer[newInvoices.length];
                 for (int i = 0; i < newInvoices.length; i++) {
                     invoiceIds[i] = newInvoices[i].getId();
                 }
                 return invoiceIds;
             } else {
                 return new Integer[] { };
             }
 
         } catch (Exception e) {
             LOG.error("WS - createInvoice", e);
             throw new SessionInternalError("Error generating invoices.");
         }
     }
 
     /*
      * USERS
      */
     /**
      * Creates a new user. The user to be created has to be of the roles customer
      * or partner.
      * The username has to be unique, otherwise the creating won't go through. If 
      * that is the case, the return value will be null.
      * @param newUser 
      * The user object with all the information of the new user. If contact or 
      * credit card information are present, they will be included in the creation
      * although they are not mandatory.
      * @return The id of the new user, or null if non was created
      */
     public Integer createUser(UserWS newUser)
             throws SessionInternalError {
 
         LOG.debug("Call to createUser ");
         validateUser(newUser);
         newUser.setUserId(0);
 
         try {
             Integer entityId = getCallerCompanyId();
             UserBL bl = new UserBL();
             LOG.info("WS - Creating user " + newUser);
 
             if (!bl.exists(newUser.getUserName(), entityId)) {
 
                 ContactBL cBl = new ContactBL();
                 UserDTOEx dto = new UserDTOEx(newUser, entityId);
                 Integer userId = bl.create(dto);
                 if (newUser.getContact() != null) {
                     newUser.getContact().setId(0);
                     cBl.createPrimaryForUser(new ContactDTOEx(
                             newUser.getContact()), userId, entityId);
                 }
 
                 if (newUser.getCreditCard() != null) {
                     newUser.getCreditCard().setId(null);
                     CreditCardBL ccBL = new CreditCardBL();
                     ccBL.create(new CreditCardDTO(newUser.getCreditCard()));
                     
                     UserDTO userD = new UserDAS().find(userId);
                     userD.getCreditCards().add(ccBL.getEntity());
                 }
                 LOG.debug("Done");
                 return userId;
             }
             LOG.debug("Done");
             return null;
         // need to catch every single one to be able to throw inside
         } catch (NamingException e) {
             LOG.error("WS user creation error", e);
             throw new SessionInternalError("Error creating user");
         }
     }
 
     public void deleteUser(Integer userId)
             throws SessionInternalError {
         LOG.debug("Call to deleteUser " + userId);
         try {
             UserBL bl = new UserBL();
             Integer executorId = getCallerId();
             bl.set(userId);
             bl.delete(executorId);
             LOG.debug("Done");
         } catch (Exception e) {
             LOG.error("WS - deleteUser", e);
             throw new SessionInternalError("Error deleting user");
         }
     }
 
     public void updateUserContact(Integer userId, Integer typeId,
             ContactWS contact)
             throws SessionInternalError {
 
         LOG.debug("Call to updateUserContact " + userId + " " + typeId);
         try {
             LOG.info("WS - Updating contact for user " + userId);
 
             // update the contact
             ContactBL cBl = new ContactBL();
             cBl.updateForUser(new ContactDTOEx(contact), userId, typeId);
             LOG.debug("Done");
 
         } catch (Exception e) {
             LOG.error("WS - updateUserContact", e);
             throw new SessionInternalError("Error updating contact");
         }
     }
 
     /**
      * @param user 
      */
     public void updateUser(UserWS user)
             throws SessionInternalError {
 
         LOG.debug("Call to updateUser ");
         validateUser(user);
 
         try {
             UserBL bl = new UserBL(user.getUserId());
 
             // get the entity
             Integer entityId = getCallerCompanyId();
             Integer executorId = getCallerId();
             LOG.info("WS - Updating user " + user);
 
             // Check whether the password changes or not.
             if (user.getPassword() != null) {
                 JBCrypto passwordCryptoService = JBCrypto.getPasswordCrypto(bl.getMainRole());
                 String newPassword = passwordCryptoService.encrypt(user.getPassword());
                 String oldPassword = bl.getEntity().getPassword();
                 if (!newPassword.equals(oldPassword)) {
                     // If the password is changing, validate it
                     if (!bl.validatePassword(user.getPassword())) {
                         throw new SessionInternalError("Error updating user");
                     }
                 }
             }
 
             // convert to a DTO
             UserDTOEx dto = new UserDTOEx(user, entityId);
 
             // update the user info
             bl.update(executorId, dto);
 
             // now update the contact info
             if (user.getContact() != null) {
                 ContactBL cBl = new ContactBL();
                 cBl.updatePrimaryForUser(new ContactDTOEx(user.getContact()),
                         user.getUserId());
             }
 
             // and the credit card
             if (user.getCreditCard() != null) {
                 IUserSessionBean sess = (IUserSessionBean) Context.getBean(
                         Context.Name.USER_SESSION);
                 sess.updateCreditCard(executorId, user.getUserId(),
                         new CreditCardDTO(user.getCreditCard()));
             }
 
         } catch (Exception e) {
             LOG.error("WS - updateUser", e);
             throw new SessionInternalError("Error updating user");
         }
         LOG.debug("Done");
 
     }
 
     /**
      * Retrieves a user with its contact and credit card information. 
      * @param userId
      * The id of the user to be returned
      */
     public UserWS getUserWS(Integer userId)
             throws SessionInternalError {
         LOG.debug("Call to getUserWS " + userId);
         UserWS dto = null;
         // calling from dot.net seems to not have a context set. So then when calling
         // getCallerPrincipal the client gets a 'No security context set' exception
         // log.debug("principal = " + context.getCallerPrincipal().getName());
         try {
             UserBL bl = new UserBL(userId);
             dto = bl.getUserWS();
         } catch (Exception e) {
             LOG.error("WS - getUserWS", e);
             throw new SessionInternalError("Error getting user");
         }
 
         LOG.debug("Done");
         return dto;
     }
 
     /**
      * Retrieves all the contacts of a user 
      * @param userId
      * The id of the user to be returned
      */
     public ContactWS[] getUserContactsWS(Integer userId)
             throws SessionInternalError {
         LOG.debug("Call to getUserContactsWS " + userId);
         ContactWS[] dtos = null;
         try {
             ContactBL contact = new ContactBL();
             Vector result = contact.getAll(userId);
             dtos = new ContactWS[result.size()];
             for (int f = 0; f < result.size(); f++) {
                 dtos[f] = new ContactWS((ContactDTOEx) result.get(f));
             }
         } catch (Exception e) {
             LOG.error("WS - getUserWS", e);
             throw new SessionInternalError("Error getting user");
         }
 
         LOG.debug("Done");
         return dtos;
     }
 
     /**
      * Retrieves the user id for the given username 
      */
     public Integer getUserId(String username)
             throws SessionInternalError {
         LOG.debug("Call to getUserId " + username);
         try {
             UserDAS das = new UserDAS();
             Integer retValue = das.findByUserName(username, 
                     getCallerCompanyId()).getId();
             LOG.debug("Done " + retValue);
             return retValue;
 
         } catch (Exception e) {
             LOG.error("WS - getUserId", e);
             throw new SessionInternalError("Error getting user id username = " + username +
                     " root " + WebServicesCaller.getUserName());
         }
 
     }
 
     /**
      * Retrieves an array of users in the required status 
      */
     public Integer[] getUsersInStatus(Integer statusId)
             throws SessionInternalError {
         LOG.debug("Call to getUsersInStatus " + statusId);
         try {
             Integer entityId = getCallerCompanyId();
             return getUsersByStatus(statusId, entityId, true);
         } catch (Exception e) {
             LOG.error("WS - getUsersInStatus", e);
             throw new SessionInternalError("Error getting users in status");
         }
     }
 
     /**
      * Retrieves an array of users in the required status 
      */
     public Integer[] getUsersNotInStatus(Integer statusId)
             throws SessionInternalError {
         LOG.debug("Call to getUsersNotInStatus " + statusId);
         try {
             Integer entityId = getCallerCompanyId();
             return getUsersByStatus(statusId, entityId, false);
         } catch (Exception e) {
             LOG.error("WS - getUsersNotInStatus", e);
             throw new SessionInternalError("Error getting users not in status");
         }
     }
 
     /**
      * Retrieves an array of users in the required status 
      */
     public Integer[] getUsersByCustomField(Integer typeId, String value)
             throws SessionInternalError {
         LOG.debug("Call to getUsersByCustomField " + typeId + " " + value);
         try {
             UserBL bl = new UserBL();
             Integer entityId = getCallerCompanyId();
 
             CachedRowSet users = bl.getByCustomField(entityId, typeId, value);
             LOG.debug("got collection. Now converting");
             Integer[] ret = new Integer[users.size()];
             int f = 0;
             while (users.next()) {
                 ret[f] = users.getInt(1);
                 f++;
             }
             users.close();
             LOG.debug("done");
             return ret;
         } catch (Exception e) {
             LOG.error("WS - getUsersByCustomField", e);
             throw new SessionInternalError("Error getting users by custom field");
         }
     }
 
     private Integer[] getByCCNumber(Integer entityId, String number) {
     	List<Integer> usersIds = new CreditCardDAS().findByLastDigits(entityId, number);
         
         Integer[] ids = new Integer[usersIds.size()];
         return usersIds.toArray(ids);
         
     }
     
     /**
      * Retrieves an array of users in the required status 
      */
     public Integer[] getUsersByCreditCard(String number)
             throws SessionInternalError {
         LOG.debug("Call to getUsersByCreditCard " + number);
         try {
             Integer entityId = getCallerCompanyId();
 
 //            CachedRowSet users = bl.getByCCNumber(entityId, number);
 //            LOG.debug("getUsersByCreditCard - got collection. Now converting");
 //            
 //            Integer[] ret = new Integer[users.size()];
 //            int f = 0;
 //            while (users.next()) {
 //                ret[f] = users.getInt(1);
 //                f++;
 //            }
 //            users.close();
             
             Integer[] ret = getByCCNumber(entityId, number);
             LOG.debug("done");
             return ret;
         } catch (Exception e) {
             LOG.error("WS - getUsersByCustomField", e);
             throw new SessionInternalError("Error getting users by custom field");
         }
     }
 
     /**
      * Retrieves an array of users in the required status 
      */
     public Integer[] getUsersByStatus(Integer statusId, Integer entityId,
             boolean in)
             throws SessionInternalError {
         try {
             LOG.debug("getting list of users. status:" + statusId +
                     " entity:" + entityId + " in:" + in);
             UserBL bl = new UserBL();
             CachedRowSet users = bl.getByStatus(entityId, statusId, in);
             LOG.debug("got collection. Now converting");
             Integer[] ret = new Integer[users.size()];
             int f = 0;
             while (users.next()) {
                 ret[f] = users.getInt(1);
                 f++;
             }
             users.close();
             LOG.debug("done");
             return ret;
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }
 
     /**
      * Creates a user, then an order for it, an invoice out the order
      * and tries the invoice to be paid by an online payment
      * This is ... the mega call !!! 
      */
     public CreateResponseWS create(UserWS user, OrderWS order)
             throws SessionInternalError {
 
         LOG.debug("Call to create ");
         validateCaller();
 
         CreateResponseWS retValue = new CreateResponseWS();
 
         // the user first
         final Integer userId = createUser(user);
         retValue.setUserId(userId);
 
         if (userId == null) {
             return retValue;
         }
 
         // the order and (if needed) invoice
         order.setUserId(userId);
 
         Integer orderId = doCreateOrder(order, true).getId();
         InvoiceDTO invoice = doCreateInvoice(orderId);
 
         retValue.setOrderId(orderId);
 
         if (invoice != null) {
             retValue.setInvoiceId(invoice.getId());
 
             //the payment, if we have a credit card
             if (user.getCreditCard() != null) {
                 PaymentDTOEx payment = doPayInvoice(invoice, new CreditCardDTO(user.getCreditCard()));
                 PaymentAuthorizationDTOEx result = null;
                 if (payment != null) {
                     result = new PaymentAuthorizationDTOEx(payment.getAuthorization().getOldDTO());
                     result.setResult(new Integer(payment.getPaymentResult().getId()).equals(Constants.RESULT_OK));
                 }
                 retValue.setPaymentResult(result);
                 retValue.setPaymentId(payment.getId());
             }
         } else {
             throw new SessionInternalError("Invoice expected for order: " + orderId);
         }
 
         LOG.debug("Done");
         return retValue;
     }
 
     /**
      * Validates the credentials and returns if the user can login or not
      * @param username
      * @param password
      * @return
      * 0 if the user can login (success), or grater than 0 if the user can not login.
      * See the constants in WebServicesConstants (AUTH*) for details.
      * @throws SessionInternalError
      */
     public Integer authenticate(String username, String password)
             throws SessionInternalError {
         Integer retValue = null;
 
         LOG.debug("Call to authenticate " + username);
         try {
             // the caller will tell us what entity is this
             UserBL bl = new UserBL();
             Integer entityId = getCallerCompanyId();
 
             // prepare the DTO for the authentication call
             UserDTOEx user = new UserDTOEx();
             user.setEntityId(entityId);
             user.setUserName(username);
             user.setPassword(password);
 
             // do the authentication
             IUserSessionBean myRemoteSession = (IUserSessionBean) 
                     Context.getBean(Context.Name.USER_SESSION);
             retValue = myRemoteSession.authenticate(user);
             if (retValue.equals(Constants.AUTH_OK)) {
                 // see if the password is not expired
                 bl.set(user.getUserName(), entityId);
                 if (bl.isPasswordExpired()) {
                     retValue = WebServicesConstants.AUTH_EXPIRED;
                 }
             }
 
         } catch (Exception e) {
             LOG.error("WS - authenticate: ", e);
             throw new SessionInternalError("Error authenticating user");
         }
 
         LOG.debug("Done");
         return retValue;
     }
 
     /**
      * Pays given invoice, using the first credit card available for invoice'd
      * user.
      * 
      * @return <code>null</code> if invoice has not positive balance, or if
      *         user does not have credit card
      * @return resulting authorization record. The payment itself can be found by
      * calling getLatestPayment
      */
     public PaymentAuthorizationDTOEx payInvoice(Integer invoiceId) throws SessionInternalError {
         LOG.debug("Call to payInvoice " + invoiceId);
         
         if (invoiceId == null) {
             throw new SessionInternalError("Can not pay null invoice");
         }
 
         final InvoiceDTO invoice = findInvoice(invoiceId);
         CreditCardDTO creditCard = getCreditCard(invoice.getBaseUser().getUserId());
         if (creditCard == null) {
             return null;
         }
 
         PaymentDTOEx payment = doPayInvoice(invoice, creditCard);
         
         PaymentAuthorizationDTOEx result = null;
         if (payment != null) {
             result = new PaymentAuthorizationDTOEx(payment.getAuthorization().getOldDTO());
             result.setResult(new Integer(payment.getPaymentResult().getId()).equals(Constants.RESULT_OK));
         }
         
         LOG.debug("Done");
         return result;
     }
 
     /**
      * Updates a user's credit card.
      * @param userId
      * The id of the user updating credit card data.
      * @param creditCard
      * The credit card data to be updated. 
      */
     public void updateCreditCard(Integer userId, com.sapienter.jbilling.server.entity.CreditCardDTO creditCard)
             throws SessionInternalError {
         LOG.debug("Call to updateCreditCard " + userId);
         try {
             if (creditCard != null && (creditCard.getName() == null ||
                     creditCard.getExpiry() == null)) {
                 LOG.debug("WS - updateCreditCard: " + "credit card validation error.");
                 throw new SessionInternalError("Missing cc data.");
             }
 
             Integer executorId = getCallerId();
             IUserSessionBean sess = (IUserSessionBean) Context.getBean(
                     Context.Name.USER_SESSION);
             CreditCardDTO cc = creditCard != null ? new CreditCardDTO(creditCard) : null;
 
             sess.updateCreditCard(executorId, userId, cc);
         } catch (Exception e) {
             LOG.error("WS - updateCreditCard: ", e);
             throw new SessionInternalError("Error updating user's credit card");
         }
         LOG.debug("Done");
 
     }
 
     /*
      * ORDERS
      */
     /**
      * @return the information of the payment aurhotization, or NULL if the 
      * user does not have a credit card
      */
     public PaymentAuthorizationDTOEx createOrderPreAuthorize(OrderWS order)
             throws SessionInternalError {
 
         LOG.debug("Call to createOrderPreAuthorize ");
         PaymentAuthorizationDTOEx retValue = null;
         // start by creating the order. It'll do the checks as well
         Integer orderId = createOrder(order);
 
         try {
             Integer userId = order.getUserId();
             CreditCardDTO cc = getCreditCard(userId);
             UserBL user = new UserBL();
             Integer entityId = user.getEntityId(userId);
             if (cc != null) {
                 CreditCardBL ccBl = new CreditCardBL();
                 OrderDAS das = new OrderDAS();
                 OrderDTO dbOrder = das.find(orderId);
 
                 retValue = ccBl.validatePreAuthorization(entityId, userId, cc,
                         dbOrder.getTotal().floatValue(), dbOrder.getCurrencyId());
             }
         } catch (Exception e) {
             LOG.debug("Exception:", e);
             throw new SessionInternalError("error pre-validating order");
         }
         LOG.debug("Done");
         return retValue;
     }
 
     public Integer createOrder(OrderWS order)
             throws SessionInternalError {
 
         LOG.debug("Call to createOrder ");
         Integer orderId = doCreateOrder(order, true).getId();
         LOG.debug("Done");
         return orderId;
     }
 
     public OrderWS rateOrder(OrderWS order)
             throws SessionInternalError {
 
         LOG.debug("Call to rateOrder ");
         OrderWS ordr = doCreateOrder(order, false);
         LOG.debug("Done");
         return ordr;
     }
 
     public void updateItem(ItemDTOEx item) {
 
         try {
             LOG.debug("Call to updateItem ");
             UserBL bl = new UserBL(getCallerId());
             Integer executorId = bl.getEntity().getUserId();
             Integer languageId = bl.getEntity().getLanguageIdField();
 
             // do some transformation from WS to DTO :(
             ItemBL itemBL = new ItemBL();
             ItemDTO dto = itemBL.getDTO(item);
 
             IItemSessionBean itemSession = (IItemSessionBean) Context.getBean(
                     Context.Name.ITEM_SESSION);
             itemSession.update(executorId, dto, languageId);
             LOG.debug("Done updateItem ");
         } catch (SessionInternalError e) {
             LOG.error("WS - updateItem", e);
             throw new SessionInternalError("Error updating item ");
         }
     }
 
     public Integer createOrderAndInvoice(OrderWS order)
             throws SessionInternalError {
 
         LOG.debug("Call to createOrderAndInvoice ");
         Integer orderId = doCreateOrder(order, true).getId();
         InvoiceDTO invoice = doCreateInvoice(orderId);
 
         LOG.debug("Done");
         return invoice == null ? null : invoice.getId();
     }
 
     private void processItemLine(OrderLineWS[] lines, Integer languageId,
             Integer entityId, Integer userId, Integer currencyId)
             throws SessionInternalError, PluggableTaskException, TaskException {
         for (OrderLineWS line : lines) {
             // get the related item
             IItemSessionBean itemSession = (IItemSessionBean) Context.getBean(
                     Context.Name.ITEM_SESSION);
 
             ItemDTO item = itemSession.get(line.getItemId(),
                     languageId, userId, currencyId,
                     entityId);
 
             //ItemDAS itemDas = new ItemDAS();
             //line.setItem(itemDas.find(line.getItemId()));
             if (line.getUseItem().booleanValue()) {
                 if (item.getPrice() == null) {
                     line.setPrice(item.getPercentage());
                 } else {
                     line.setPrice(item.getPrice());
                 }
                 if (line.getDescription() == null ||
                         line.getDescription().length() == 0) {
                     line.setDescription(item.getDescription());
                 }
             }
         }
     }
 
     public void updateOrder(OrderWS order)
             throws SessionInternalError {
         LOG.debug("Call to updateOrder ");
         validateOrder(order);
         try {
             // get the info from the caller
             UserBL bl = new UserBL(getCallerId());
             Integer executorId = bl.getEntity().getUserId();
             Integer entityId = bl.getEntityId(bl.getEntity().getUserId());
             Integer languageId = bl.getEntity().getLanguageIdField();
 
             // see if the related items should provide info
             processItemLine(order.getOrderLines(), languageId, entityId, 
                     order.getUserId(), order.getCurrencyId());
 
             // do some transformation from WS to DTO :(
             OrderBL orderBL = new OrderBL();
             OrderDTO dto = orderBL.getDTO(order);
             // recalculate
             orderBL.set(dto);
             orderBL.recalculate(entityId);
             // update
             orderBL.set(order.getId());
             orderBL.update(executorId, dto);
         } catch (Exception e) {
             LOG.error("WS - updateOrder", e);
             throw new SessionInternalError("Error updating order");
         }
         LOG.debug("Done");
 
     }
 
     public OrderWS getOrder(Integer orderId)
             throws SessionInternalError {
         try {
             LOG.debug("order requested " + orderId);
             // get the info from the caller
             UserBL userbl = new UserBL(getCallerId());
             Integer languageId = userbl.getEntity().getLanguageIdField();
 
             // now get the order. Avoid the proxy since this is for the client
             OrderDAS das = new OrderDAS();
             OrderDTO order = das.findNow(orderId);
             if (order == null) { // not found
                 LOG.debug("Done");
                 return null;
             }
             OrderBL bl = new OrderBL(order);
             if (order.getDeleted() == 1) {
                 LOG.debug("Returning deleted order " + orderId);
             }
             LOG.debug("Done");
             return bl.getWS(languageId);
         } catch (Exception e) {
             LOG.error("WS - getOrder", e);
             throw new SessionInternalError("Error getting order");
         }
     }
 
     public Integer[] getOrderByPeriod(Integer userId, Integer periodId)
             throws SessionInternalError {
         LOG.debug("Call to getOrderByPeriod " + userId + " " + periodId);
         if (userId == null || periodId == null) {
             return null;
         }
         try {
             // now get the order
             OrderBL bl = new OrderBL();
             LOG.debug("Done");
             return bl.getByUserAndPeriod(userId, periodId);
         } catch (Exception e) {
             LOG.error("WS - getOrderByPeriod", e);
             throw new SessionInternalError("Error getting orders for a user " +
                     "by period");
         }
     }
 
     public OrderLineWS getOrderLine(Integer orderLineId)
             throws SessionInternalError {
         try {
             LOG.debug("WS - getOrderLine " + orderLineId);
             // now get the order
             OrderBL bl = new OrderBL();
             OrderLineWS retValue = null;
 
             retValue = bl.getOrderLineWS(orderLineId);
 
             LOG.debug("Done");
             return retValue;
         } catch (Exception e) {
             LOG.error("WS - getOrderLine", e);
             throw new SessionInternalError("Error getting order line");
         }
     }
 
     public void updateOrderLine(OrderLineWS line)
             throws SessionInternalError {
         LOG.debug("Call to updateOrderLine ");
         try {
             // now get the order
             OrderBL bl = new OrderBL();
             bl.updateOrderLine(line);
 //            LOG.debug("WS - updateOrderLine " + line); // cant be earlier - no session
         } catch (Exception e) {
             LOG.error("WS - updateOrderLine", e);
             throw new SessionInternalError("Error updating order line");
         }
         LOG.debug("Done");
     }
 
     public OrderWS getLatestOrder(Integer userId)
             throws SessionInternalError {
         LOG.debug("Call to getLatestOrder " + userId);
         if (userId == null) {
             throw new SessionInternalError("User id can not be null");
         }
         try {
             OrderWS retValue = null;
             // get the info from the caller
             UserBL userbl = new UserBL(getCallerId());
             Integer languageId = userbl.getEntity().getLanguageIdField();
 
             // now get the order
             OrderBL bl = new OrderBL();
             Integer orderId = bl.getLatest(userId);
             if (orderId != null) {
                 bl.set(orderId);
                 retValue = bl.getWS(languageId);
             }
             LOG.debug("Done");
             return retValue;
         } catch (Exception e) {
             LOG.error("WS - getLatestOrder", e);
             throw new SessionInternalError("Error getting latest order");
         }
     }
 
     public Integer[] getLastOrders(Integer userId, Integer number)
             throws SessionInternalError {
         LOG.debug("Call to getLastOrders " + userId + " " + number);
         if (userId == null || number == null) {
             return null;
         }
         try {
             UserBL userbl = new UserBL();
 
             OrderBL order = new OrderBL();
             LOG.debug("Done");
             return order.getListIds(userId, number, userbl.getEntityId(userId));
         } catch (Exception e) {
             LOG.error("WS - getLastOrders", e);
             throw new SessionInternalError("Error getting last orders");
         }
     }
 
     public void deleteOrder(Integer id)
             throws SessionInternalError {
         LOG.debug("Call to deleteOrder " + id);
         try {
             // now get the order
             OrderBL bl = new OrderBL(id);
             bl.delete(getCallerId());
         } catch (Exception e) {
             LOG.error("WS - deleteOrder", e);
             throw new SessionInternalError("Error deleting order");
         }
         LOG.debug("Done");
     }
 
     /**
      * Returns the current one-time order for this user for the given 
      * date. Returns null for users with no main subscription order.
      */
     public OrderWS getCurrentOrder(Integer userId, Date date) {
         LOG.debug("Call to getCurrentOrder " + userId);
         try {
             OrderWS retValue = null;
             // get the info from the caller
             UserBL userbl = new UserBL(getCallerId());
             Integer languageId = userbl.getEntity().getLanguageIdField();
 
             // now get the current order
             OrderBL bl = new OrderBL();
             if (bl.getCurrentOrder(userId, date) != null) {
                 retValue = bl.getWS(languageId);
             }
 
             LOG.debug("Done");
             return retValue;
 
         } catch (Exception e) {
             LOG.error("WS - getCurrentOrder", e);
             throw new SessionInternalError("Error getting current order");
         }
     }
 
     /**
      * Updates the uesr's current one-time order for the given date.
      * Returns the updated current order. Throws an exception for 
      * users with no main subscription order.
      */
     public OrderWS updateCurrentOrder(Integer userId, OrderLineWS[] lines, 
             String pricing, Date date) {
         LOG.debug("Call to updateCurrentOrder - userId: " + userId + 
             " lines: " + Arrays.toString(lines) + " date: " + date);
         try {
             UserBL userbl = new UserBL(userId);
             // check user has a main subscription order
             if (userbl.getEntity().getCustomer().getCurrentOrderId() == null) {
                 throw new SessionInternalError("No main subscription order " +
                         "for userId: " + userId);
             }
 
             // get currency from the user
             Integer currencyId = userbl.getCurrencyId();
 
             // get language from the caller
             userbl.set(getCallerId());
             Integer languageId = userbl.getEntity().getLanguageIdField();
 
             // convert order lines from WS to DTO
             OrderBL bl = new OrderBL();
             processItemLine(lines, languageId, getCallerCompanyId(), userId, 
                     currencyId);
             Vector<OrderLineDTO> orderLines = new Vector<OrderLineDTO>(
                     lines.length);
             for (OrderLineWS line : lines) {
                 orderLines.add(bl.getOrderLine(line));
             }
 
             // pricing fields
             Vector<Record> records = null;
             PricingField[] fieldsArray = PricingField.getPricingFieldsValue(
                     pricing);
             if (fieldsArray != null) {
                 Record record = new Record();
                 for (PricingField field : fieldsArray) {
                     record.addField(field, false); // don't care about isKey
                 }
                 records = new Vector<Record>(1);
                 records.add(record);
             }
 
             // do the update
             bl.updateCurrent(getCallerCompanyId(), getCallerId(), userId, 
                     currencyId, orderLines, records, date);
 
             // return the updated order
             return bl.getWS(languageId);
 
         } catch (Exception e) {
             LOG.error("WS - getCurrentOrder", e);
             throw new SessionInternalError("Error getting current order");
         }
     }
 
     /*
      * PAYMENT
      */
     public Integer applyPayment(PaymentWS payment, Integer invoiceId)
             throws SessionInternalError {
         LOG.debug("Call to applyPayment " + invoiceId);
         validatePayment(payment);
         try {
             //TODO Validate that the user ID of the payment is the same as the
             // owner of the invoice
             payment.setIsRefund(new Integer(0));
             IPaymentSessionBean session = (IPaymentSessionBean) Context.getBean(
                     Context.Name.PAYMENT_SESSION);
             LOG.debug("Done");
             return session.applyPayment(new PaymentDTOEx(payment), invoiceId);
         } catch (Exception e) {
             LOG.error("WS - applyPayment", e);
             throw new SessionInternalError("Error applying payment");
         }
     }
 
     public PaymentWS getPayment(Integer paymentId)
             throws SessionInternalError {
         LOG.debug("Call to getPayment " + paymentId);
         try {
             // get the info from the caller
             UserBL userbl = new UserBL(getCallerId());
             Integer languageId = userbl.getEntity().getLanguageIdField();
 
             PaymentBL bl = new PaymentBL(paymentId);
             LOG.debug("Done");
             return PaymentBL.getWS(bl.getDTOEx(languageId));
         } catch (Exception e) {
             LOG.error("WS - getPayment", e);
             throw new SessionInternalError("Error getting payment");
         }
     }
 
     public PaymentWS getLatestPayment(Integer userId)
             throws SessionInternalError {
         LOG.debug("Call to getLatestPayment " + userId);
         try {
             PaymentWS retValue = null;
             // get the info from the caller
             UserBL userbl = new UserBL(getCallerId());
             Integer languageId = userbl.getEntity().getLanguageIdField();
 
             PaymentBL bl = new PaymentBL();
             Integer paymentId = bl.getLatest(userId);
             if (paymentId != null) {
                 bl.set(paymentId);
                 retValue = PaymentBL.getWS(bl.getDTOEx(languageId));
             }
             LOG.debug("Done");
             return retValue;
         } catch (Exception e) {
             LOG.error("WS - getLatestPayment", e);
             throw new SessionInternalError("Error getting latest payment");
         }
     }
 
     public Integer[] getLastPayments(Integer userId, Integer number)
             throws SessionInternalError {
         LOG.debug("Call to getLastPayments " + userId + " " + number);
         if (userId == null || number == null) {
             return null;
         }
         LOG.debug("WS - getLastPayments " + userId + " " + number);
         try {
             UserBL userbl = new UserBL(getCallerId());
             Integer languageId = userbl.getEntity().getLanguageIdField();
 
             PaymentBL payment = new PaymentBL();
             LOG.debug("Done");
             return payment.getManyWS(userId, number, languageId);
         } catch (Exception e) {
             LOG.error("WS - getLastPayments", e);
             throw new SessionInternalError("Error getting last payments");
         }
     }
 
     /*
      * ITEM
      */
     public Integer createItem(ItemDTOEx item)
             throws SessionInternalError {
         LOG.debug("Call to createItem ");
         ItemBL itemBL = new ItemBL();
         ItemDTO dto = itemBL.getDTO(item);
         if (!ItemBL.validate(dto)) {
             throw new SessionInternalError("invalid argument");
         }
         try {
             // get the info from the caller
             UserBL bl = new UserBL(getCallerId());
             Integer languageId = bl.getEntity().getLanguageIdField();
             Integer entityId = bl.getEntityId(bl.getEntity().getUserId());
             dto.setEntity(new CompanyDTO(entityId));
 
             // call the creation
             LOG.debug("Done");
             return itemBL.create(dto, languageId);
 
         } catch (Exception e) {
             LOG.error("WS - createItem", e);
             throw new SessionInternalError("Error creating item");
         }
 
     }
 
     /**
      * Retrieves an array of items for the caller's entity. 
      * @return an array of items from the caller's entity
      */
     public ItemDTOEx[] getAllItems() throws SessionInternalError {
         LOG.debug("Call to getAllItems ");
         try {
             Integer entityId = getCallerCompanyId();
             ItemBL itemBL = new ItemBL();
             LOG.debug("Done");
             return itemBL.getAllItems(entityId);
         } catch (Exception e) {
             LOG.error("WS - getAllItems", e);
             throw new SessionInternalError("Error getting all items");
         }
     }
 
     /**
      * Implementation of the User Transitions List webservice. This accepts a
      * start and end date as arguments, and produces an array of data containing
      * the user transitions logged in the requested time range.
      * @param from Date indicating the lower limit for the extraction of transition
      * logs. It can be <code>null</code>, in such a case, the extraction will start
      * where the last extraction left off. If no extractions have been done so far and
      * this parameter is null, the function will extract from the oldest transition
      * logged.
      * @param to Date indicatin the upper limit for the extraction of transition logs.
      * It can be <code>null</code>, in which case the extraction will have no upper
      * limit. 
      * @return UserTransitionResponseWS[] an array of objects containing the result
      * of the extraction, or <code>null</code> if there is no data thas satisfies
      * the extraction parameters.
      */
     public UserTransitionResponseWS[] getUserTransitions(Date from, Date to)
             throws SessionInternalError {
 
         LOG.debug("Call to getUserTransitions " + from + " " + to);
         UserTransitionResponseWS[] result = null;
         Integer last = null;
         // Obtain the current entity and language Ids
 
         try {
             UserBL user = new UserBL();
             Integer callerId = getCallerId();
             Integer entityId = getCallerCompanyId();
             EventLogger evLog = EventLogger.getInstance();
 
             if (from == null) {
                 last = evLog.getLastTransitionEvent(entityId);
             }
 
             if (last != null) {
                 result = user.getUserTransitionsById(entityId, last, to);
             } else {
                 result = user.getUserTransitionsByDate(entityId, from, to);
             }
 
             if (result == null) {
                 LOG.info("Data retrieved but resultset is null");
             } else {
                 LOG.info("Data retrieved. Result size = " + result.length);
             }
 
             // Log the last value returned if there was any. This happens always,
             // unless the returned array is empty.
             if (result != null && result.length > 0) {
                 LOG.info("Registering transition list event");
                 evLog.audit(callerId, Constants.TABLE_EVENT_LOG, callerId, EventLogger.MODULE_WEBSERVICES,
                         EventLogger.USER_TRANSITIONS_LIST, result[result.length - 1].getId(),
                         result[0].getId().toString(), null);
             }
         } catch (Exception e) {
             throw new SessionInternalError("Error accessing database [" + e.getLocalizedMessage() + "]", this.getClass(), e);
         }
         LOG.debug("Done");
         return result;
     }
 
     /**
      * @return UserTransitionResponseWS[] an array of objects containing the result
      * of the extraction, or <code>null</code> if there is no data thas satisfies
      * the extraction parameters.
      */
     public UserTransitionResponseWS[] getUserTransitionsAfterId(Integer id)
             throws SessionInternalError {
 
         LOG.debug("Call to getUserTransitionsAfterId " + id);
         UserTransitionResponseWS[] result = null;
         // Obtain the current entity and language Ids
 
         try {
             UserBL user = new UserBL();
             Integer callerId = getCallerId();
             Integer entityId = getCallerCompanyId();
             EventLogger evLog = EventLogger.getInstance();
 
             result = user.getUserTransitionsById(entityId, id, null);
 
             if (result == null) {
                 LOG.info("Data retrieved but resultset is null");
             } else {
                 LOG.info("Data retrieved. Result size = " + result.length);
             }
 
             // Log the last value returned if there was any. This happens always,
             // unless the returned array is empty.
             if (result != null && result.length > 0) {
                 LOG.info("Registering transition list event");
                 evLog.audit(callerId, Constants.TABLE_EVENT_LOG, callerId, EventLogger.MODULE_WEBSERVICES,
                         EventLogger.USER_TRANSITIONS_LIST, result[result.length - 1].getId(),
                         result[0].getId().toString(), null);
             }
         } catch (Exception e) {
             throw new SessionInternalError("Error accessing database [" + e.getLocalizedMessage() + "]", this.getClass(), e);
         }
         LOG.debug("Done");
         return result;
     }
 
     public ItemDTOEx getItem(Integer itemId, Integer userId, String pricing) {
 
         try {
             LOG.debug("Call to getItem");
             PricingField[] fields = PricingField.getPricingFieldsValue(pricing);
 
             ItemBL helper = new ItemBL(itemId);
             Vector<PricingField> f = new Vector<PricingField>();
             f.addAll(Arrays.asList(fields));
             helper.setPricingFields(f);
             UserBL user = new UserBL(getCallerId());
             Integer callerId = user.getEntity().getUserId();
             Integer entityId = user.getEntityId(callerId);
             Integer languageId = user.getEntity().getLanguageIdField();
             Integer currencyId = user.getCurrencyId();
 
             ItemDTOEx retValue = helper.getWS(helper.getDTO(languageId, userId, entityId, currencyId));
             LOG.debug("Done");
             return retValue;
         } catch (Exception e) {
             LOG.error("WS - getItem", e);
             throw new SessionInternalError("Error getting item " + itemId);
         }
     }
 
     private Integer zero2null(Integer var) {
         if (var != null && var.intValue() == 0) {
             return null;
         } else {
             return var;
         }
     }
 
     private Date zero2null(Date var) {
         if (var != null) {
             Calendar cal = Calendar.getInstance();
             cal.setTime(var);
             if (cal.get(Calendar.YEAR) == 1) {
                 return null;
             }
         }
 
         return var;
 
     }
 
     private void validateUser(UserWS newUser)
             throws SessionInternalError {
         // do the validation
         if (newUser == null) {
             throw new SessionInternalError("Null parameter");
         }
         // C# sends a 0 when it is null ...
         newUser.setCurrencyId(zero2null(newUser.getCurrencyId()));
         newUser.setPartnerId(zero2null(newUser.getPartnerId()));
         newUser.setParentId(zero2null(newUser.getParentId()));
         newUser.setMainRoleId(zero2null(newUser.getMainRoleId()));
         newUser.setLanguageId(zero2null(newUser.getLanguageId()));
         newUser.setStatusId(zero2null(newUser.getStatusId()));
         // clean up the cc number from spaces and '-'
         if (newUser.getCreditCard() != null &&
                 newUser.getCreditCard().getNumber() != null) {
             newUser.getCreditCard().setNumber(CreditCardBL.cleanUpNumber(
                     newUser.getCreditCard().getNumber()));
         }
 
         try {
             GatewayBL valid = new GatewayBL();
             // the user
             if (!valid.validate("UserWS", newUser)) {
                 throw new SessionInternalError(valid.getText());
             }
             // the contact
             if (!valid.validate("ContactDTO", newUser.getContact())) {
                 throw new SessionInternalError(valid.getText());
             }
             // the credit card (optional)
             if (newUser.getCreditCard() != null && !valid.validate("CreditCardDTO",
                     newUser.getCreditCard())) {
                 throw new SessionInternalError(valid.getText());
             }
             // additional validation
             if (newUser.getMainRoleId().equals(Constants.TYPE_CUSTOMER) ||
                     newUser.getMainRoleId().equals(Constants.TYPE_PARTNER)) {
             } else {
                 throw new SessionInternalError("Valid user roles are customer (5) " +
                         "and partner (4)");
             }
             if (newUser.getCurrencyId() != null &&
                     newUser.getCurrencyId().intValue() <= 0) {
                 throw new SessionInternalError("Invalid currency code");
             }
             if (newUser.getStatusId().intValue() <= 0) {
                 throw new SessionInternalError("Invalid status code");
             }
         } catch (ValidatorException e) {
             LOG.error("validating ws", e);
             throw new SessionInternalError("Invalid parameter");
         }
     }
 
     private void validateOrder(OrderWS order)
             throws SessionInternalError {
         if (order == null) {
             throw new SessionInternalError("Null parameter");
         }
         order.setUserId(zero2null(order.getUserId()));
         order.setPeriod(zero2null(order.getPeriod()));
         order.setBillingTypeId(zero2null(order.getBillingTypeId()));
         order.setStatusId(zero2null(order.getStatusId()));
         order.setCurrencyId(zero2null(order.getCurrencyId()));
         order.setNotificationStep(zero2null(order.getNotificationStep()));
         order.setDueDateUnitId(zero2null(order.getDueDateUnitId()));
         order.setDueDateValue(zero2null(order.getDueDateValue()));
         order.setDfFm(zero2null(order.getDfFm()));
         order.setAnticipatePeriods(zero2null(order.getAnticipatePeriods()));
         order.setActiveSince(zero2null(order.getActiveSince()));
         order.setActiveUntil(zero2null(order.getActiveUntil()));
         order.setNextBillableDay(zero2null(order.getNextBillableDay()));
         order.setLastNotified(null);
         // CXF seems to pass empty array as null
         if (order.getOrderLines() == null) {
             order.setOrderLines(new OrderLineWS[0]);
         }
 
         try {
             GatewayBL valid = new GatewayBL();
             // the order
             if (!valid.validate("OrderWS", order)) {
                 throw new SessionInternalError(valid.getText());
             }
             // the lines
             for (int f = 0; f < order.getOrderLines().length; f++) {
                 OrderLineWS line = order.getOrderLines()[f];
                 if (!valid.validate("OrderLineWS", line)) {
                     throw new SessionInternalError(valid.getText());
                 }
                 if (line.getUseItem() == null) {
                     line.setUseItem(new Boolean(false));
                 }
                 line.setItemId(zero2null(line.getItemId()));
                 String error = "";
                 // if use the item, I need the item id
                 if (line.getUseItem().booleanValue()) {
                     if (line.getItemId() == null ||
                             line.getItemId().intValue() == 0) {
                         error += "OrderLineWS: if useItem == true the itemId " +
                                 "is required - ";
                     }
                     if (line.getQuantity() == null ||
                             line.getQuantity().doubleValue() == 0.0) {
                         error += "OrderLineWS: if useItem == true the quantity " +
                                 "is required - ";
                     }
                 } else {
                     // I need the amount and description
                     if (line.getAmount() == null) {
                         error += "OrderLineWS: if useItem == false the item amount " +
                                 "is required - ";
                     }
                     if (line.getDescription() == null ||
                             line.getDescription().length() == 0) {
                         error += "OrderLineWS: if useItem == false the description " +
                                 "is required - ";
                     }
                 }
                 if (error.length() > 0) {
                     throw new SessionInternalError(error);
                 }
             }
         } catch (ValidatorException e) {
             LOG.error("validating ws", e);
             throw new SessionInternalError("Invalid parameter");
         }
     }
 
     private void validatePayment(PaymentWS payment)
             throws SessionInternalError {
         if (payment == null) {
             throw new SessionInternalError("Null parameter");
         }
         payment.setBaseUserId(payment.getBaseUserId());
         payment.setMethodId(payment.getMethodId());
         payment.setCurrencyId(payment.getCurrencyId());
         payment.setPaymentId(payment.getPaymentId());
 
         try {
             GatewayBL valid = new GatewayBL();
             // the payment
             if (!valid.validate("PaymentWS", payment)) {
                 throw new SessionInternalError(valid.getText());
             }
             // may be there is a cc
             if (payment.getCreditCard() != null && !valid.validate(
                     "CreditCardDTO", payment.getCreditCard())) {
                 throw new SessionInternalError(valid.getText());
             }
             // may be there is a cheque
             if (payment.getCheque() != null && !valid.validate(
                     "PaymentInfoChequeDTO", payment.getCheque())) {
                 throw new SessionInternalError(valid.getText());
             }
             // may be there is a ach
             if (payment.getAch() != null && !valid.validate(
                     "AchDTO", payment.getAch())) {
                 throw new SessionInternalError(valid.getText());
             }
         } catch (ValidatorException e) {
             LOG.error("validating ws", e);
             throw new SessionInternalError("Invalid parameter");
         }
     }
 
     private InvoiceDTO doCreateInvoice(Integer orderId) {
         try {
             BillingProcessBL process = new BillingProcessBL();
             InvoiceDTO invoice = process.generateInvoice(orderId, null);
             return invoice;
         } catch (Exception e) {
             LOG.error("WS - create invoice:", e);
             throw new SessionInternalError("Error while generating a new invoice");
         }
     }
 
     private void validateCaller() {
         String root = WebServicesCaller.getUserName();
         try {
             UserBL bl = new UserBL();
             bl.setRoot(root);
             bl.getEntityId(bl.getEntity().getUserId());
         } catch (Exception e) {
             throw new SessionInternalError("Error identifiying the caller");
         }
     }
 
     private PaymentDTOEx doPayInvoice(InvoiceDTO invoice, CreditCardDTO creditCard)
             throws SessionInternalError {
 
         if (invoice.getBalance() == null || invoice.getBalance() <= 0) {
             LOG.warn("Can not pay invoice: " + invoice.getId() + ", balance: " + invoice.getBalance());
             return null;
         }
 
         try {
             IPaymentSessionBean payment = (IPaymentSessionBean) Context.getBean(
                     Context.Name.PAYMENT_SESSION);
             PaymentDTOEx paymentDto = new PaymentDTOEx();
             paymentDto.setIsRefund(0);
             paymentDto.setAmount(invoice.getBalance());
             paymentDto.setCreditCard(creditCard);
             paymentDto.setCurrency(new CurrencyDAS().find(invoice.getCurrency().getId()));
             paymentDto.setUserId(invoice.getBaseUser().getUserId());
             paymentDto.setPaymentMethod( new PaymentMethodDAS().find(
                     com.sapienter.jbilling.common.Util.getPaymentMethod(
                     creditCard.getNumber())));
             paymentDto.setPaymentDate(new Date());
 
             // make the call
             payment.processAndUpdateInvoice(paymentDto, invoice);
 
             return paymentDto;
         } catch (Exception e) {
             LOG.error("WS - make payment:", e);
             throw new SessionInternalError("Error while making payment for invoice: " + invoice.getId());
         }
     }
 
     /**
      * Conveniance method to find a credit card
      */
     private CreditCardDTO getCreditCard(Integer userId) {
         if (userId == null) {
             return null;
         }
 
         CreditCardDTO result = null;
         try {
             UserBL user = new UserBL(userId);
             Integer entityId = user.getEntityId(userId);
             if (user.hasCreditCard()) {
                 // find it
                 PaymentDTOEx paymentDto = PaymentBL.findPaymentInstrument(
                         entityId, userId);
                 // it might have a credit card, but it might not be valid or 
                 // just not found by the plug-in
                 if (paymentDto != null) {
                     result = paymentDto.getCreditCard();
                 }
             }
         } catch (Exception e) {
             LOG.error("WS - finding a credit card", e);
             throw new SessionInternalError("Error finding a credit card for user: " + userId);
         }
 
         return result;
     }
 
     private OrderWS doCreateOrder(OrderWS order, boolean create)
             throws SessionInternalError {
 
         validateOrder(order);
         try {
             // get the info from the caller
             UserBL bl = new UserBL(getCallerId());
             Integer executorId = bl.getEntity().getUserId();
             Integer entityId = bl.getEntityId(bl.getEntity().getUserId());
             Integer languageId = bl.getEntity().getLanguageIdField();
 
             // we'll need the langauge later
             bl.set(order.getUserId());
             // see if the related items should provide info
             processItemLine(order.getOrderLines(), languageId, entityId, 
                     order.getUserId(), order.getCurrencyId());
             // call the creation
             OrderBL orderBL = new OrderBL();
             OrderDTO dto = orderBL.getDTO(order);
             LOG.debug("Order has " + order.getOrderLines().length + " lines");
 
             // make sure this shows as a new order, not as an update
             dto.setId(null);
             dto.setVersionNum(null);
             for (OrderLineDTO line : dto.getLines()) {
                 line.setId(0);
                 line.setVersionNum(null);
             }
 
             LOG.info("before cycle start");
             // set a default cycle starts if needed (obtained from the main 
             // subscription order, if it exists)
             if (dto.getCycleStarts() == null && dto.getIsCurrent() == null) {
                 Integer mainOrderId = orderBL.getMainOrderId(dto.getUser().getId());
                 if (mainOrderId != null) {
                     // only set a default if preference use current order is set
                     PreferenceBL preferenceBL = new PreferenceBL();
                     try {
                         preferenceBL.set(entityId, Constants.PREFERENCE_USE_CURRENT_ORDER);
                     } catch (EmptyResultDataAccessException e) {
                     // default preference will be used
                     }
                     if (preferenceBL.getInt() != 0) {
                         OrderDAS das = new OrderDAS();
                         OrderDTO mainOrder = das.findNow(mainOrderId);
                         LOG.debug("Copying cycle starts from main order");
                         dto.setCycleStarts(mainOrder.getCycleStarts());
                     }
                 }
             }
 
             orderBL.set(dto);
             orderBL.recalculate(entityId);
             if (create) {
                 LOG.debug("creating order");
                 Integer id = orderBL.create(entityId, executorId, dto);
                 orderBL.set(id);
                 return orderBL.getWS(languageId);
             }
             return getWSFromOrder(orderBL, languageId);
         } catch (Exception e) {
             LOG.error("WS error creating order:", e);
             throw new SessionInternalError("error creating purchase order");
         }
     }
 
     private OrderWS getWSFromOrder(OrderBL bl, Integer languageId) {
         OrderDTO order = bl.getDTO();
         OrderWS retValue = new OrderWS(order.getId(), order.getBillingTypeId(),
                 order.getNotify(), order.getActiveSince(), order.getActiveUntil(),
                 order.getCreateDate(), order.getNextBillableDay(),
                 order.getCreatedBy(), order.getStatusId(), order.getDeleted(),
                 order.getCurrencyId(), order.getLastNotified(),
                 order.getNotificationStep(), order.getDueDateUnitId(),
                 order.getDueDateValue(), order.getAnticipatePeriods(),
                 order.getDfFm(), order.getIsCurrent(), order.getNotes(),
                 order.getNotesInInvoice(), order.getOwnInvoice(),
                 order.getOrderPeriod().getId(),
                 order.getBaseUserByUserId().getId(),
                 order.getVersionNum(), order.getCycleStarts());
 
         retValue.setPeriodStr(order.getOrderPeriod().getDescription(languageId));
         retValue.setBillingTypeStr(order.getOrderBillingType().getDescription(languageId));
 
         Vector<OrderLineWS> lines = new Vector<OrderLineWS>();
         for (Iterator<OrderLineDTO> it = order.getLines().iterator(); it.hasNext();) {
             OrderLineDTO line = (OrderLineDTO) it.next();
             LOG.info("copying line: " + line);
             if (line.getDeleted() == 0) {
 
             	OrderLineWS lineWS = new OrderLineWS(line.getId(), line.getItem().getId(), line.getDescription(),
                 		line.getAmount(), line.getQuantity(), line.getPrice(), line.getItemPrice(), 
                 		line.getCreateDatetime(), line.getDeleted(), line.getOrderLineType().getId(), 
                 		line.getEditable(), (line.getPurchaseOrder() != null?line.getPurchaseOrder().getId():null), 
                 		null, line.getVersionNum(),line.getProvisioningStatusId(),line.getProvisioningRequestId());
               
                 lines.add(lineWS);
             }
         }
         retValue.setOrderLines(new OrderLineWS[lines.size()]);
         lines.toArray(retValue.getOrderLines());
         return retValue;
     }
 
     private InvoiceDTO findInvoice(Integer invoiceId) {
         final InvoiceDTO invoice;
         try {
             invoice = new InvoiceBL(invoiceId).getEntity();
         } catch (Exception e) {
             LOG.error("WS: findInvoice error: ", e);
             throw new SessionInternalError("Configuration problems");
         } 
         return invoice;
     }
 
     // TODO: This method is not secured or in a jUnit test
     public InvoiceWS getLatestInvoiceByItemType(Integer userId, Integer itemTypeId)
             throws SessionInternalError {
         LOG.debug("Call to getLatestInvoiceByItemType " + userId + " " + itemTypeId);
         InvoiceWS retValue = null;
         try {
             if (userId == null) {
                 return null;
             }
             InvoiceBL bl = new InvoiceBL();
             Integer invoiceId = bl.getLastByUserAndItemType(userId, itemTypeId);
             if (invoiceId != null) {
                 retValue = bl.getWS(new InvoiceDAS().find(invoiceId));
             }
             LOG.debug("Done");
             return retValue;
         } catch (Exception e) {
             LOG.error("Exception in web service: getting latest invoice" +
                     " for user " + userId, e);
             throw new SessionInternalError("Error getting latest invoice");
         }
     }
 
     /**
      * Return 'number' most recent invoices that contain a line item with an
      * item of the given item type.
      */
     // TODO: This method is not secured or in a jUnit test
     public Integer[] getLastInvoicesByItemType(Integer userId, Integer itemTypeId, Integer number)
             throws SessionInternalError {
         LOG.debug("Call to getLastInvoicesByItemType " + userId + " " + itemTypeId + " " + number);
         try {
             if (userId == null || itemTypeId == null || number == null) {
                 return null;
             }
 
             InvoiceBL bl = new InvoiceBL();
             LOG.debug("Done");
             return bl.getManyByItemTypeWS(userId, itemTypeId, number);
         } catch (Exception e) {
             LOG.error("Exception in web service: getting last invoices by item type" +
                     " for user " + userId, e);
             throw new SessionInternalError("Error getting last invoices by item type");
         }
     }
 
     // TODO: This method is not secured or in a jUnit test
     public OrderWS getLatestOrderByItemType(Integer userId, Integer itemTypeId)
             throws SessionInternalError {
         LOG.debug("Call to getLatestOrderByItemType " + userId + " " + itemTypeId);
         if (userId == null) {
             throw new SessionInternalError("User id can not be null");
         }
         if (itemTypeId == null) {
             throw new SessionInternalError("itemTypeId can not be null");
         }
         try {
             OrderWS retValue = null;
             // get the info from the caller
             UserBL userbl = new UserBL(getCallerId());
             Integer languageId = userbl.getEntity().getLanguageIdField();
 
             // now get the order
             OrderBL bl = new OrderBL();
             Integer orderId = bl.getLatestByItemType(userId, itemTypeId);
             if (orderId != null) {
                 bl.set(orderId);
                 retValue = bl.getWS(languageId);
             }
             LOG.debug("Done");
             return retValue;
         } catch (Exception e) {
             LOG.error("WS - getLatestOrder", e);
             throw new SessionInternalError("Error getting latest order");
         }
     }
 
     // TODO: This method is not secured or in a jUnit test
     public Integer[] getLastOrdersByItemType(Integer userId, Integer itemTypeId, Integer number)
             throws SessionInternalError {
         LOG.debug("Call to getLastOrdersByItemType " + userId + " " + itemTypeId + " " + number);
         if (userId == null || number == null) {
             return null;
         }
         try {
             OrderBL order = new OrderBL();
             return order.getListIdsByItemType(userId, itemTypeId, number);
         } catch (Exception e) {
             LOG.error("WS - getLastOrdersByItemType", e);
             throw new SessionInternalError("Error getting last orders by item type");
         } finally {
             LOG.debug("Done");
         }
     }
 
     private Integer getCallerId() {
         return WebServicesCaller.getUserId();
     }
 
     private Integer getCallerCompanyId() {
         return WebServicesCaller.getCompanyId();
     }
     
     @Override
     public Double isUserSubscribedTo(Integer userId, Integer itemId) {
 	    LOG.debug("Call to isUserSubscribedTo with params: userId = "
 	    			+ userId + ", itemId = " + itemId);
     	Double result = Double.valueOf(0);
     	try {
     		OrderDAS das = new OrderDAS();
     		result = das.findIsUserSubscribedTo(userId, itemId);
     		return result;
     	} catch (Throwable e) {
     		LOG.error("Error determining if user is subscribed to item", e);
     		throw new SessionInternalError("Error determining if user is subscribed to item");
     	} finally {
   			LOG.debug("Done");
     	}
     }
     
     @Override
     public Integer[] getUserItemsByCategory(Integer userId, Integer categoryId) {
     	LOG.debug("Call to getUserItemsByCategory with params: userId = "
     			+ userId + ", categoryId = " + categoryId);
     	Integer[] result = null;
     	try {
     		OrderDAS das = new OrderDAS();
     		result = das.findUserItemsByCategory(userId, categoryId);
     		return result;
     	} catch (Throwable e) {
     		LOG.error("Error retrieving user items by category", e);
     		throw new SessionInternalError("Error retrieving user items by category");
     	} finally {
    			LOG.debug("Done");
     	}
     }
 
     public Double validatePurchase(Integer userId, Integer itemId,
             String fields) {
         LOG.debug("Call to validatePurchase " + userId + ' ' + itemId);
 
         if (userId == null || itemId == null) {
             return null;
         }
 
         PricingField[] fieldsArray = PricingField.getPricingFieldsValue(
                     fields);
         // find the price first
         ItemBL item = new ItemBL(itemId);
         item.setPricingFields(new Vector(Arrays.asList(fieldsArray)));
        Float price = item.getPrice(userId, getCallerCompanyId());
 
         Double ret = new UserBL(userId).validatePurchase(new BigDecimal(price));
         LOG.debug("Done");
         return ret;
     }
 
 
 }
