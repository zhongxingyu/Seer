 /*
  jBilling - The Enterprise Open Source Billing System
  Copyright (C) 2003-2011 Enterprise jBilling Software Ltd. and Emiliano Conde
 
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
 import java.util.List;
 
 import com.sapienter.jbilling.server.entity.AchDTO;
 import com.sapienter.jbilling.server.invoice.InvoiceWS;
 import com.sapienter.jbilling.server.item.ItemDTOEx;
 import com.sapienter.jbilling.server.item.ItemTypeWS;
 import com.sapienter.jbilling.server.item.PlanItemWS;
 import com.sapienter.jbilling.server.item.PlanWS;
 import com.sapienter.jbilling.server.item.PricingField;
 import com.sapienter.jbilling.server.mediation.MediationConfigurationWS;
 import com.sapienter.jbilling.server.mediation.MediationProcessWS;
 import com.sapienter.jbilling.server.mediation.MediationRecordLineWS;
 import com.sapienter.jbilling.server.mediation.MediationRecordWS;
 import com.sapienter.jbilling.server.mediation.RecordCountWS;
 import com.sapienter.jbilling.server.notification.MessageDTO;
 import com.sapienter.jbilling.server.order.OrderLineWS;
 import com.sapienter.jbilling.server.order.OrderPeriodWS;
 import com.sapienter.jbilling.server.order.OrderProcessWS;
 import com.sapienter.jbilling.server.order.OrderWS;
 import com.sapienter.jbilling.server.payment.PaymentAuthorizationDTOEx;
 import com.sapienter.jbilling.server.payment.PaymentWS;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskWS;
 import com.sapienter.jbilling.server.process.AgeingWS;
 import com.sapienter.jbilling.server.process.BillingProcessConfigurationWS;
 import com.sapienter.jbilling.server.process.BillingProcessWS;
 import com.sapienter.jbilling.server.process.ProcessStatusWS;
 import com.sapienter.jbilling.server.user.CardValidationWS;
 import com.sapienter.jbilling.server.user.CompanyWS;
 import com.sapienter.jbilling.server.user.ContactTypeWS;
 import com.sapienter.jbilling.server.user.ContactWS;
 import com.sapienter.jbilling.server.user.CreateResponseWS;
 import com.sapienter.jbilling.server.user.UserTransitionResponseWS;
 import com.sapienter.jbilling.server.user.UserWS;
 import com.sapienter.jbilling.server.user.ValidatePurchaseWS;
 import com.sapienter.jbilling.server.user.contact.ContactFieldTypeWS;
 import com.sapienter.jbilling.server.user.contact.ContactFieldWS;
 import com.sapienter.jbilling.server.user.partner.PartnerWS;
 import com.sapienter.jbilling.server.util.CurrencyWS;
 import com.sapienter.jbilling.server.util.PreferenceWS;
 
 public interface JbillingAPI {
     
     /*
         Users
      */
 
     public UserWS getUserWS(Integer userId);
     public Integer createUser(UserWS newUser);
     public void updateUser(UserWS user);
     public void deleteUser(Integer userId);
 
     public boolean userExistsWithName(String userName);
     public boolean userExistsWithId(Integer userId);
 
     public ContactWS[] getUserContactsWS(Integer userId);
     public void updateUserContact(Integer userId, Integer typeId, ContactWS contact);
 
     public ContactTypeWS getContactTypeWS(Integer contactTypeId);
     public Integer createContactTypeWS(ContactTypeWS contactType);
 
     public void updateCreditCard(Integer userId, com.sapienter.jbilling.server.entity.CreditCardDTO creditCard);
     public void deleteCreditCard(Integer userId);
     public void updateAch(Integer userId, AchDTO ach);
     public void deleteAch(Integer userId);
 
     public void setAutoPaymentType(Integer userId, Integer autoPaymentType, boolean use);
     public Integer getAutoPaymentType(Integer userId);
 
     public Integer[] getUsersByStatus(Integer statusId, boolean in);
     public Integer[] getUsersInStatus(Integer statusId);
     public Integer[] getUsersNotInStatus(Integer statusId);
     public Integer[] getUsersByCustomField(Integer typeId, String value);
     public Integer[] getUsersByCustomFields(ContactFieldWS[] fields);
     public Integer[] getUsersByCreditCard(String number);
 
     public Integer getUserId(String username);
 
     public void processPartnerPayouts(Date runDate);
     public PartnerWS getPartner(Integer partnerId);
 
     public UserTransitionResponseWS[] getUserTransitions(Date from, Date to);
     public UserTransitionResponseWS[] getUserTransitionsAfterId(Integer id);
 
     public CreateResponseWS create(UserWS user, OrderWS order);
 
 
     /*
         Items
      */
 
     public ItemDTOEx getItem(Integer itemId, Integer userId, PricingField[] fields);
     public ItemDTOEx[] getAllItems();
     public Integer createItem(ItemDTOEx item);
     public void updateItem(ItemDTOEx item);
     public void deleteItem(Integer itemId);
 
     public ItemDTOEx[] getItemByCategory(Integer itemTypeId);
     public Integer[] getUserItemsByCategory(Integer userId, Integer categoryId);
 
     public ItemTypeWS[] getAllItemCategories();
     public Integer createItemCategory(ItemTypeWS itemType);
     public void updateItemCategory(ItemTypeWS itemType);
     public void deleteItemCategory(Integer itemCategoryId);
 
     public String isUserSubscribedTo(Integer userId, Integer itemId);
 
     public InvoiceWS getLatestInvoiceByItemType(Integer userId, Integer itemTypeId);
     public Integer[] getLastInvoicesByItemType(Integer userId, Integer itemTypeId, Integer number);
 
     public OrderWS getLatestOrderByItemType(Integer userId, Integer itemTypeId);
     public Integer[] getLastOrdersByItemType(Integer userId, Integer itemTypeId, Integer number);
 
     public ValidatePurchaseWS validatePurchase(Integer userId, Integer itemId, PricingField[] fields);
     public ValidatePurchaseWS validateMultiPurchase(Integer userId, Integer[] itemIds, PricingField[][] fields);
 
 
     /*
         Orders
      */
 
     public OrderWS getOrder(Integer orderId);
     public Integer createOrder(OrderWS order);
     public void updateOrder(OrderWS order);
     public Integer createUpdateOrder(OrderWS order);
     public void deleteOrder(Integer id);
 
     public Integer createOrderAndInvoice(OrderWS order);
 
     public OrderWS getCurrentOrder(Integer userId, Date date);
     public OrderWS updateCurrentOrder(Integer userId, OrderLineWS[] lines, PricingField[] fields, Date date, String eventDescription); 
 
     public OrderLineWS getOrderLine(Integer orderLineId);
     public void updateOrderLine(OrderLineWS line);
 
     public Integer[] getOrderByPeriod(Integer userId, Integer periodId);
     public OrderWS getLatestOrder(Integer userId);
     public Integer[] getLastOrders(Integer userId, Integer number);
 
     public OrderWS rateOrder(OrderWS order);
     public OrderWS[] rateOrders(OrderWS orders[]);
 
     public PaymentAuthorizationDTOEx createOrderPreAuthorize(OrderWS order);
 
 
     /*
         Invoices
      */
 
     public InvoiceWS getInvoiceWS(Integer invoiceId);
     public Integer[] createInvoice(Integer userId, boolean onlyRecurring);
    public Integer[] createInvoiceWithDate(Integer userId, Date billingDate, Integer dueDatePeriodId, Integer dueDatePeriodValue, boolean onlyRecurring);
     public Integer createInvoiceFromOrder(Integer orderId, Integer invoiceId);
     public void deleteInvoice(Integer invoiceId);
 
     public Integer[] getAllInvoices(Integer userId);
     public InvoiceWS getLatestInvoice(Integer userId);
     public Integer[] getLastInvoices(Integer userId, Integer number);
 
     public Integer[] getInvoicesByDate(String since, String until);
     public Integer[] getUserInvoicesByDate(Integer userId, String since, String until);
 
     public Integer[] getUnpaidInvoices(Integer userId);
 
     public byte[] getPaperInvoicePDF(Integer invoiceId);
     public boolean notifyInvoiceByEmail(Integer invoiceId);
     public boolean notifyPaymentByEmail(Integer paymentId);
 
     /*
         Payments
      */
 
     public PaymentWS getPayment(Integer paymentId);
     public PaymentWS getLatestPayment(Integer userId);
     public Integer[] getLastPayments(Integer userId, Integer number);
     public PaymentWS getUserPaymentInstrument(Integer userId);
 
     public Integer createPayment(PaymentWS payment);
     public void updatePayment(PaymentWS payment);
     public void deletePayment(Integer paymentId);
 
     public void removePaymentLink(Integer invoiceId, Integer paymentId);
     public void createPaymentLink(Integer invoiceId, Integer paymentId);
 
     public PaymentAuthorizationDTOEx payInvoice(Integer invoiceId);
     public Integer applyPayment(PaymentWS payment, Integer invoiceId);
     public PaymentAuthorizationDTOEx processPayment(PaymentWS payment, Integer invoiceId);
 
     public CardValidationWS validateCreditCard(com.sapienter.jbilling.server.entity.CreditCardDTO creditCard, ContactWS contact, int level);
 
 
     /*
         Billing process
      */
 
     public void triggerBillingAsync(final Date runDate);
     public boolean triggerBilling(Date runDate);
     public boolean isBillingProcessRunning();
     public ProcessStatusWS getBillingProcessStatus();
 
     public void triggerAgeing(Date runDate);
     public boolean isAgeingProcessRunning();
     public ProcessStatusWS getAgeingProcessStatus();
 
     public BillingProcessConfigurationWS getBillingProcessConfiguration();
     public Integer createUpdateBillingProcessConfiguration(BillingProcessConfigurationWS ws);
 
     public BillingProcessWS getBillingProcess(Integer processId);
     public Integer getLastBillingProcess();
 
     public List<OrderProcessWS> getOrderProcesses(Integer orderId);
     public List<OrderProcessWS> getOrderProcessesByInvoice(Integer invoiceId);
 
     public BillingProcessWS getReviewBillingProcess();
     public BillingProcessConfigurationWS setReviewApproval(Boolean flag);
 
     public List<Integer> getBillingProcessGeneratedInvoices(Integer processId);
 
     public AgeingWS[] getAgeingConfiguration(Integer languageId) ;
     public void saveAgeingConfiguration(AgeingWS[] steps, Integer gracePeriod, Integer languageId);
 
 
     /*
         Mediation process
      */
 
     public void triggerMediation();
     public Integer triggerMediationByConfiguration(Integer cfgId);
     public boolean isMediationProcessRunning();
     public ProcessStatusWS getMediationProcessStatus();
 
     public MediationProcessWS getMediationProcess(Integer mediationProcessId);
     public List<MediationProcessWS> getAllMediationProcesses();
     public List<MediationRecordLineWS> getMediationEventsForOrder(Integer orderId);
     public List<MediationRecordLineWS> getMediationEventsForInvoice(Integer invoiceId);
     public List<MediationRecordWS> getMediationRecordsByMediationProcess(Integer mediationProcessId);
     public List<RecordCountWS> getNumberOfMediationRecordsByStatuses();
 
     public List<MediationConfigurationWS> getAllMediationConfigurations();
     public void createMediationConfiguration(MediationConfigurationWS cfg);
     public List<Integer> updateAllMediationConfigurations(List<MediationConfigurationWS> configurations);
     public void deleteMediationConfiguration(Integer cfgId);
 
 
 
     /*
         Provisioning process
      */
 
     public void triggerProvisioning();
 
     public void updateOrderAndLineProvisioningStatus(Integer inOrderId, Integer inLineId, String result);
     public void updateLineProvisioningStatus(Integer orderLineId, Integer provisioningStatus);
 
 
     /*
         Utilities
      */
 
     public void generateRules(String rulesData);
 
 
     /*
         Preferences
      */
     public void updatePreferences(PreferenceWS[] prefList);
     public void updatePreference(PreferenceWS preference);
     public PreferenceWS getPreference(Integer preferenceTypeId);
 
 
     /*
         Currencies
      */
 
     public CurrencyWS[] getCurrencies();
     public void updateCurrencies(CurrencyWS[] currencies);
     public void updateCurrency(CurrencyWS currency);
     public Integer createCurrency(CurrencyWS currency);
 
 
     /*
         Plug-ins
      */
 
     public PluggableTaskWS getPluginWS(Integer pluginId);
     public Integer createPlugin(PluggableTaskWS plugin);
     public void updatePlugin(PluggableTaskWS plugin);
     public void deletePlugin(Integer plugin);
 
 
     /*
         Plans and special pricing
      */
 
     public PlanWS getPlanWS(Integer planId);
     public Integer createPlan(PlanWS plan);
     public void updatePlan(PlanWS plan);
     public void deletePlan(Integer planId);
     public void addPlanPrice(Integer planId, PlanItemWS price);
 
     public boolean isCustomerSubscribed(Integer planId, Integer userId);
     public Integer[] getSubscribedCustomers(Integer planId);
     public Integer[] getPlansBySubscriptionItem(Integer itemId);
     public Integer[] getPlansByAffectedItem(Integer itemId);
 
     public PlanItemWS createCustomerPrice(Integer userId, PlanItemWS planItem);
     public void updateCustomerPrice(Integer userId, PlanItemWS planItem);
     public void deleteCustomerPrice(Integer userId, Integer planItemId);
 
     public PlanItemWS[] getCustomerPrices(Integer userId);
     public PlanItemWS getCustomerPrice(Integer userId, Integer itemId);
 
     
     /*
      * 
      */
 
     public BigDecimal getTotalRevenueByUser (Integer userId);
     public CompanyWS getCompany();
     public Integer getCallerCompanyId();
     public Integer getCallerId();
     public Integer getCallerLanguageId();
     public InvoiceWS[] getAllInvoicesForUser(Integer userId);
     public OrderWS[] getUserSubscriptions(Integer userId);
     public boolean deleteOrderPeriod(Integer periodId);
     public boolean updateOrderPeriods(OrderPeriodWS[] orderPeriods);
     public boolean updateOrCreateOrderPeriod(OrderPeriodWS orderPeriod);
     public void createUpdateNofications(Integer messageId, MessageDTO dto);
     public void saveCustomContactField(ContactFieldTypeWS ws);
     public void saveCustomerNotes(Integer userId, String notes);
     public void updateCompany(CompanyWS companyWS);
     
 }
