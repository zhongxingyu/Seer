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
 package com.sapienter.jbilling.server.util.api;
 
 import java.math.BigDecimal;
 import java.util.Date;
 
 import com.sapienter.jbilling.server.invoice.InvoiceWS;
 import com.sapienter.jbilling.server.item.ItemDTOEx;
 import com.sapienter.jbilling.server.item.PricingField;
 import com.sapienter.jbilling.server.item.ItemTypeWS;
 import com.sapienter.jbilling.server.order.OrderLineWS;
 import com.sapienter.jbilling.server.order.OrderWS;
 import com.sapienter.jbilling.server.payment.PaymentAuthorizationDTOEx;
 import com.sapienter.jbilling.server.payment.PaymentWS;
 import com.sapienter.jbilling.server.user.ContactWS;
 import com.sapienter.jbilling.server.user.CreateResponseWS;
 import com.sapienter.jbilling.server.user.UserTransitionResponseWS;
 import com.sapienter.jbilling.server.user.UserWS;
 import com.sapienter.jbilling.server.user.ValidatePurchaseWS;
 import com.sapienter.jbilling.server.entity.AchDTO;
 import com.sapienter.jbilling.server.entity.CreditCardDTO;
 import com.sapienter.jbilling.server.notification.MessageDTO;
 import com.sapienter.jbilling.server.user.CompanyWS;
 import com.sapienter.jbilling.server.util.OptionDTO;
 import com.sapienter.jbilling.server.util.ReportWS;
 import com.sapienter.jbilling.server.util.audit.db.EventLogDTO;
 import java.util.Collection;
 import java.util.List;
 
 public interface JbillingAPI {
 
     /*
      * INVOICES
      */
     public Integer[] createInvoice(Integer userId, boolean onlyRecurring) throws JbillingAPIException;
 
     public InvoiceWS getInvoiceWS(Integer invoiceId) throws JbillingAPIException;
 
     public void deleteInvoice(Integer invoiceId) throws JbillingAPIException;
 
     public InvoiceWS getLatestInvoice(Integer userId) throws JbillingAPIException;
 
     public Integer[] getLastInvoices(Integer userId, Integer number) throws JbillingAPIException;
 
     public Integer[] getInvoicesByDate(String since, String until) throws JbillingAPIException;
 
     public Integer[] getUserInvoicesByDate(Integer userId, String since,
             String until) throws JbillingAPIException;
 
     /**
      * ------------------- INVOICE API EXTENSION --------------------------
      */
     /**
      * Generates a new invoice for an order, or adds the order to an existing
      * invoice.
      *
      * @param orderId order id to generate an invoice for
      * @param invoiceId optional invoice id to add the order to. If null, a new
      * invoice will be created.
      * @return id of generated invoice, null if no invoice generated.
      * @throws JbillingAPIException if order id is null.
      */
     public Integer createInvoiceFromOrder(Integer orderId, Integer invoiceId) throws JbillingAPIException;
 
     /**
      * Retrieves a list of all the {@link InvoiceWS invoices} in a given period
      * of time. The method will return an array of the InvoiceWS objects.<br>
      *
      * If no invoices where generated for the specified period, an empty array
      * is returned. If the parameters do not follow the required format
      * (yyyy-mm-dd), null is returned.
      *
      * @param since the starting date for the data extraction
      * @param until the ending date for the data extraction
      * @return an array of <code>InvoiceWS</code> objects or an empty array if
      * nothing found
      * @throws JbillingAPIException when internal error occurs
      */
     public InvoiceWS[] getInvoiceListByDate(String since, String until) throws JbillingAPIException;
 
     /**
      * Retrieves invoices belonging to a customer, starting from the last one.
      *
      * @param userId the id of the customer
      * @param number the number of invoices that are to be retrieved
      *
      * @return an array of invoices
      * @throws JbillingAPIException when internal error occurs
      */
     public InvoiceWS[] getLastInvoicesForUser(Integer userId, Integer number) throws JbillingAPIException;
 
     /**
      * Retrieves an array of Overdue invoice ids.
      *
      * @param date the current date
      * @return an array of invoice ids or null if nothing found
      * @throws JbillingAPIException when internal error occurs
      */
     public Integer[] getOverdueInvoiceIds(String date) throws JbillingAPIException;
 
     /**
      * Returns an array of all unpaid invoices for given user ID.
      *
      * @param userId user id
      * @return array of un-paid invoice IDs
      * @throws JbillingAPIException when internal error occurs
      */
     public InvoiceWS[] getUnpaidInvoices(Integer userId) throws JbillingAPIException;
 
     /**
      * Retrieves an array of the Invoice ids by given Status
      *
      * @param status the status of the invoice
      * @return an array of Invoice ids or null if nothing found
      * @throws JbillingAPIException when internal error occurs
      */
     public Integer[] getInvoiceIdsByStatus(Integer status) throws JbillingAPIException;
 
     /**
      * Search for Invoices by given string parameter.
      *
      * @param searchParam the search parameter string
      * @return an array of Invoice ids or null if nothing found
      * @throws JbillingAPIException when internal error occurs
      */
     public Integer[] searchInvoiceIds(String searchValue) throws JbillingAPIException;
 
     /**
      * Search for Invoices by given string parameter.
      *
      * @param searchParam the search parameter string
      * @return an array of Invoice objects or null if nothing found
      * @throws JbillingAPIException when internal error occurs
      */
     public InvoiceWS[] searchInvoices(String searchValue) throws JbillingAPIException;
 
     /**
      * Generates and returns the paper invoice PDF for the given invoiceId.
      * TODO: Extra check might require to make sure invoice belongs to user.
      *
      * @param userId an id of the customer
      * @param invoiceId an id of the invoice that will be downloaded.
      * @return an array of bytes for PDF invoice or null if nothing found.
      * @throws JbillingAPIException when internal error occurs
      */
     public byte[] getPaperInvoicePDF(Integer invoiceId, Integer userId) throws JbillingAPIException;
 
     /**
      * Sends an email with the invoice to a customer. This API call is used to
      * manually send an email invoice to a customer. TODO: Extra check might
      * require to make sure invoice belongs to user.
      *
      * @param userId an id of the customer to send email to
      * @param invoiceId an id of the invoice that will be send out.
      * @return <code>true</code> when email was sent * * * * * * * *
      * successfully, <code>false</code> otherwise.
      * @throws JbillingAPIException when internal error occurs
      */
     public Boolean emailInvoice(Integer invoiceId, Integer userId) throws JbillingAPIException;
 
     /*
      * USERS
      */
     public Integer createUser(UserWS newUser) throws JbillingAPIException;
 
     public UserWS getUserWS(Integer userId) throws JbillingAPIException;
 
     public ContactWS[] getUserContactsWS(Integer userId) throws JbillingAPIException;
 
     public void updateUser(UserWS user) throws JbillingAPIException;
 
     public void updateUserContact(Integer userId, Integer typeId,
             ContactWS contact) throws JbillingAPIException;
 
     public void deleteUser(Integer userId) throws JbillingAPIException;
 
     public Integer getUserId(String username) throws JbillingAPIException;
 
     public Integer[] getUsersInStatus(Integer statusId)
             throws JbillingAPIException;
 
     public Integer[] getUsersNotInStatus(Integer statusId)
             throws JbillingAPIException;
 
     public Integer[] getUsersByCreditCard(String number)
             throws JbillingAPIException;
 
     public Integer[] getUsersByCustomField(Integer typeId, String value)
             throws JbillingAPIException;
 
     public CreateResponseWS create(UserWS user, OrderWS order)
             throws JbillingAPIException;
 
     public Integer authenticate(String username, String password)
             throws JbillingAPIException;
 
     public void updateCreditCard(Integer userId, CreditCardDTO creditCard)
             throws JbillingAPIException;
 
     public void updateAch(Integer userId, AchDTO ach)
             throws JbillingAPIException;
 
     public Integer getAutoPaymentType(Integer userId) throws JbillingAPIException;
 
     public void setAutoPaymentType(Integer userId, Integer autoPaymentType, boolean use)
             throws JbillingAPIException;
 
     public UserTransitionResponseWS[] getUserTransitions(Date from, Date to)
             throws JbillingAPIException;
 
     public UserTransitionResponseWS[] getUserTransitionsAfterId(Integer id)
             throws JbillingAPIException;
 
     /**
      * ------------------- USER API EXTENSION --------------------------
      */
     /**
      * Retrieves a list of all {@link UserWS users} in a given status.
      *
      * @param statusId the status id that will be used for extraction
      * @return an array of <code>UserWS</code> objects in a given status.
      * @throws JbillingAPIException when internal error occurs
      */
     public UserWS[] getUserListInStatus(Integer statusId) throws JbillingAPIException;
 
     /**
      * Retrieves a list of all {@link UserWS customers} in a given status
      * including sub-accounts. This call excludes any other users that are not
      * Customers.
      *
      * @param statusId the status id that will be used for extraction
      * @return a Collection of <code>UserWS</code> objects in a given status.
      * @throws JbillingAPIException when internal error occurs
      */
     public Collection<UserWS> getCustomersInStatus(Integer statusId) throws JbillingAPIException;
 
     /**
      * Retrieves a list of all Customer ids in a given status including
      * sub-accounts.
      *
      * @param statusId the status id that will be used for extraction
      * @return an array of user ids in a given status.
      * @throws JbillingAPIException when internal error occurs
      */
     public Integer[] getCustomerIdsInStatus(Integer statusId) throws JbillingAPIException;
 
     /**
      * Search for {@link UserWS users}, including sub-accounts by given search
      * parameter. Only search users who are customers.
      *
      * @param searchValue the string parameter to search for
      * @return an array of <code>UserWS</code> objects if any match found, null
      * otherwise.
      * @throws JbillingAPIException when internal error occurs
      */
     public UserWS[] searchCustomers(String searchValue) throws JbillingAPIException;
 
     /**
      * Retrieves a list of customer ids by given search parameter.
      *
      * @param searchValue the search parameter string
      * @return an array of user ids or null if nothing found
      * @throws JbillingAPIException when internal error occurs
      */
     public Integer[] searchCustomerIds(String searchValue) throws JbillingAPIException;
 
     /*
      * ORDERS
      */
     public Integer createOrder(OrderWS order) throws JbillingAPIException;
 
     public PaymentAuthorizationDTOEx createOrderPreAuthorize(OrderWS order)
             throws JbillingAPIException;
 
     public Integer createOrderAndInvoice(OrderWS order) throws JbillingAPIException;
 
     public OrderWS rateOrder(OrderWS order) throws JbillingAPIException;
 
     public OrderWS[] rateOrders(OrderWS orders[]) throws JbillingAPIException;
 
     public void updateOrder(OrderWS order) throws JbillingAPIException;
 
     public OrderWS getOrder(Integer orderId) throws JbillingAPIException;
 
     public Integer[] getOrderByPeriod(Integer userId, Integer periodId)
             throws JbillingAPIException;
 
     public OrderLineWS getOrderLine(Integer orderLineId)
             throws JbillingAPIException;
 
     public void updateOrderLine(OrderLineWS line) throws JbillingAPIException;
 
     public OrderWS getLatestOrder(Integer userId) throws JbillingAPIException;
 
     public Integer[] getLastOrders(Integer userId, Integer number)
             throws JbillingAPIException;
 
     public void deleteOrder(Integer id) throws JbillingAPIException;
 
     public OrderWS getCurrentOrder(Integer userId, Date date)
             throws JbillingAPIException;
 
     public OrderWS updateCurrentOrder(Integer userId, OrderLineWS[] lines,
             PricingField[] fields, Date date, String eventDescription)
             throws JbillingAPIException;
 
     /*
      * PAYMENT
      */
     public PaymentAuthorizationDTOEx payInvoice(Integer invoiceId)
             throws JbillingAPIException;
 
     public Integer applyPayment(PaymentWS payment, Integer invoiceId)
             throws JbillingAPIException;
 
     public PaymentWS getPayment(Integer paymentId) throws JbillingAPIException;
 
     public PaymentWS getLatestPayment(Integer userId)
             throws JbillingAPIException;
 
     public Integer[] getLastPayments(Integer userId, Integer number)
             throws JbillingAPIException;
 
     public PaymentAuthorizationDTOEx processPayment(PaymentWS payment) throws JbillingAPIException;
 
     /**
      * ------------------- PAYMENT API EXTENSION --------------------------
      */
     /**
      * Updates user's payment details.
      *
      * @param payment The payment data to be updated.
      * @throws JbillingAPIException when internal error occurs
      */
     public void updatePayment(PaymentWS payment) throws JbillingAPIException;
 
     /**
      * Deletes payment for user.
      *
      * @param paymentId the id of the payment to delete
      * @throws JbillingAPIException when internal error occurs
      */
     public void deletePayment(Integer paymentId) throws JbillingAPIException;
 
     /**
      * Retrieves several payments for a customer, starting from the last one.
      *
      * @param userId id of the customer whose payment information is to be
      * retrieved
      * @param number the number of payments to retrieve
      * @return an array of PaymentWS or null if none found. If the input
      * parameters are missing or incorrect, a SessionInternalError is issued.
      * @throws JbillingAPIException when internal error occurs
      *
      */
     public PaymentWS[] getLastUserPayments(Integer userId, Integer number) throws JbillingAPIException;
 
     /**
      * Retrieve all the payments created in a given period of time for
      * organisation.
      *
      * @param since the starting date for the data extraction
      * @param until the ending date for the data extraction
      * @return an array of payment ids or null if none found. If the input
      * parameters are missing or are not in required format (yyyy-mm-dd), null
      * is returned.
      * @throws JbillingAPIException when internal error occurs
      */
     public Integer[] getPaymentIdsByDate(String since, String until) throws JbillingAPIException;
 
     /**
      * Retrieve all the payments created in a given period of time for
      * organisation.
      *
      * @param since the starting date for the data extraction
      * @param until the ending date for the data extraction
      * @return an array of PaymentWS or null if none found. If the input
      * parameters are missing or are not in required format (yyyy-mm-dd), null
      * is returned.
      * @throws JbillingAPIException when internal error occurs
      */
     public PaymentWS[] getPaymentsByDate(String since, String until) throws JbillingAPIException;
 
     /**
      * Retrieve all the payments created in a given period of time for user.
      *
      * @param since the starting date for the data extraction
      * @param until the ending date for the data extraction
      * @param userId id of the customer whose payment information is to be
      * retrieved
      * @return an array of PaymentWS or null if none found. If the input
      * parameters are missing or are not in required format (yyyy-mm-dd), null
      * is returned.
      * @throws JbillingAPIException when internal error occurs
      */
     public PaymentWS[] getUserPaymentsByDate(String since, String until, Integer userId) throws JbillingAPIException;
 
     /**
      * Search all the payments by given search parameter for organisation.
      *
      * @param searchValue the search parameter string
      *
      * @return an array of PaymentWS or null if none found. If the input
      * parameters are missing , null is returned.
      * @throws JbillingAPIException when internal error occurs
      */
     public PaymentWS[] searchPayments(String searchValue) throws JbillingAPIException;
 
     /**
      * Un-links a payment from an invoice, effectively making the invoice
      * "unpaid" by removing the payment balance.
      *
      * @param invoiceId target Invoice
      * @param paymentId payment to be unlink
      */
    public void removePaymentLink(Integer invoiceId, Integer paymentId);
 
     /**
      * Applies an existing payment to an invoice.
      *
      * @param invoiceId target invoice
      * @param paymentId payment to apply
      */
    public void createPaymentLink(Integer invoiceId, Integer paymentId);
 
     /**
      * Sends a Payment email notification to a customer for given payment
      *
      * @param paymentId
      * @return true if email was successfully sent, false otherwise
      * @throws JbillingAPIException when internal error occurs
      */
     public boolean notifyPaymentByEmail(Integer paymentId) throws JbillingAPIException;
     /*
      * ITEM
      */
 
     public Integer createItem(ItemDTOEx dto) throws JbillingAPIException;
 
     public ItemDTOEx[] getAllItems() throws JbillingAPIException;
 
     public ItemDTOEx getItem(Integer itemId, Integer userId, PricingField[] fields)
             throws JbillingAPIException;
 
     public void updateItem(ItemDTOEx item) throws JbillingAPIException;
 
     // "byItemType" routines:
     public OrderWS getLatestOrderByItemType(Integer userId, Integer itemTypeId) throws JbillingAPIException;
 
     public InvoiceWS getLatestInvoiceByItemType(Integer userId, Integer itemTypeId) throws JbillingAPIException;
 
     public Integer[] getLastInvoicesByItemType(Integer userId, Integer itemTypeId, Integer number) throws JbillingAPIException;
 
     public Integer[] getLastOrdersByItemType(Integer userId, Integer itemTypeId, Integer number) throws JbillingAPIException;
 
     public BigDecimal isUserSubscribedTo(Integer userId, Integer itemId) throws JbillingAPIException;
 
     public Integer[] getUserItemsByCategory(Integer userId, Integer categoryId) throws JbillingAPIException;
 
     public ItemDTOEx[] getItemByCategory(Integer itemTypeId) throws JbillingAPIException;
 
     public ItemTypeWS[] getAllItemCategories() throws JbillingAPIException;
 
     public ValidatePurchaseWS validatePurchase(Integer userId, Integer itemId, PricingField[] fields) throws JbillingAPIException;
 
     public ValidatePurchaseWS validateMultiPurchase(Integer userId, Integer[] itemIds, PricingField[][] fields) throws JbillingAPIException;
 
     public Integer createItemCategory(ItemTypeWS itemType) throws JbillingAPIException;
 
     void updateItemCategory(ItemTypeWS itemType) throws JbillingAPIException;
 
     public void generateRules(String rulesData) throws JbillingAPIException;
 
     /*
      * ORGANIZATION
      */
     public CompanyWS getCompany() throws JbillingAPIException;
 
     public void updateCompany(CompanyWS companyWS) throws JbillingAPIException;
     /*
      * NOTIFICATIONS
      */
 
     public MessageDTO[] getNotificationMessages() throws JbillingAPIException;
 
     public Integer createUpdateNofications(Integer messageId, MessageDTO dto) throws JbillingAPIException;
 
     /**
      *
      * ------------------- MISC API EXTENSION --------------------------
      */
     /**
      * Saves uploaded logo image file for the user's entity (company).
      *
      * @param inBytes an array of bytes for image to upload.
      * @return true if image was successfully saved, false otherwise
      * @throws JbillingAPIException when internal error occurs
      */
     public boolean uploadLogo(byte[] inBytes) throws JbillingAPIException;
 
     /**
      * Get the absolute logo image path for organisation.
      *
      * @return a string path for logo image
      * @throws JbillingAPIException when internal error occurs
      */
     public String getLogoPath() throws JbillingAPIException;
 
     /**
      * Retrieves an event logs for user account.
      *
      * @param userId the id of the user whose event log info to be retrieved
      * @return a list of <code>EventLogDTO</code>
      * @throws JbillingAPIException when internal error occurs
      */
     public List<EventLogDTO> getUserEventLog(Integer userId) throws JbillingAPIException;
 
     /**
      * Retrieves a list of {@link OptionDTO options} for used in forms fields
      * with a select box.
      *
      * @param type the type of select option to retrieve
      * @return a <code>Collection</code> of <code>OptionDTO</code> objects or
      * null if given type cannot be found
      * @throws JbillingAPIException when internal error occurs
      */
     public Collection<OptionDTO> getSelectOption(String type) throws JbillingAPIException;
 
     /**
      * Generate age receivable report for organisation. TODO: This method is not
      * secured or in a jUnit test
      *
      * @param since the starting date for the data extraction
      * @param until the ending date for the data extraction
      * @return a list of ReportWS objects or null if nothing found
      * @throws JbillingAPIException when internal error occurs
      */
     public List<ReportWS> getAgeReceivableReport(String since, String until) throws JbillingAPIException;
 
     /**
      * Utility method for importing invoices into JBilling.
      *
      * @param invoices a list of invoices to import
      * @throws JbillingAPIException when internal error occurs
      */
     public void importInvoices(Collection<InvoiceWS> invoices) throws JbillingAPIException;
 }
