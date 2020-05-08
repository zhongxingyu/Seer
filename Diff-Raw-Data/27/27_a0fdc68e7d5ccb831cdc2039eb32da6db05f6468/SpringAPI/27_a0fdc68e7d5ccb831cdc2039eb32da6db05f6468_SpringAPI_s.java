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
 import com.sapienter.jbilling.server.entity.CreditCardDTO;
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
 import com.sapienter.jbilling.server.util.IWebServicesSessionBean;
 import com.sapienter.jbilling.server.util.PreferenceWS;
 import com.sapienter.jbilling.server.util.RemoteContext;
 
 public class SpringAPI implements JbillingAPI {
 
     private IWebServicesSessionBean session = null;
 
     public SpringAPI() {
         this(RemoteContext.Name.API_CLIENT);
     }
 
     public SpringAPI(RemoteContext.Name bean) {
         session = (IWebServicesSessionBean) RemoteContext.getBean(bean);
     }
 
     public Integer applyPayment(PaymentWS payment, Integer invoiceId) {
         return session.applyPayment(payment, invoiceId);
     }
 
     public PaymentAuthorizationDTOEx processPayment(PaymentWS payment, Integer invoiceId) {
         return session.processPayment(payment, invoiceId);
     }
 
     public CardValidationWS validateCreditCard(com.sapienter.jbilling.server.entity.CreditCardDTO creditCard, ContactWS contact, int level) {
         return session.validateCreditCard(creditCard, contact, level);
     }
 
     public void processPartnerPayouts(Date runDate) {
         session.processPartnerPayouts(runDate);
     }
 
     public PartnerWS getPartner(Integer partnerId) {
         return session.getPartner(partnerId);
     }
 
     public CreateResponseWS create(UserWS user, OrderWS order) {
         return session.create(user, order);
     }
 
     public Integer createItem(ItemDTOEx dto) {
         return session.createItem(dto);
     }
 
     public Integer createOrder(OrderWS order) {
         return session.createOrder(order);
     }
 
     public Integer createOrderAndInvoice(OrderWS order) {
         return session.createOrderAndInvoice(order);
     }
 
     public PaymentAuthorizationDTOEx createOrderPreAuthorize(OrderWS order) {
         return session.createOrderPreAuthorize(order);
     }
 
     public Integer createUser(UserWS newUser) {
         return session.createUser(newUser);
     }
 
     public void deleteOrder(Integer id) {
         session.deleteOrder(id);
     }
 
     public void deleteUser(Integer userId) {
         session.deleteUser(userId);
     }
 
     public boolean userExistsWithName(String userName) {
         return session.userExistsWithName(userName);
     }
 
     public boolean userExistsWithId(Integer userId) {
         return session.userExistsWithId(userId);
     }
 
     public void deleteInvoice(Integer invoiceId) {
         session.deleteInvoice(invoiceId);
     }
 
     public ItemDTOEx[] getAllItems() {
         return session.getAllItems();
     }
 
     public InvoiceWS getInvoiceWS(Integer invoiceId) {
         return session.getInvoiceWS(invoiceId);
     }
 
     public Integer[] getInvoicesByDate(String since, String until) {
         return session.getInvoicesByDate(since, until);
     }
 
     public byte[] getPaperInvoicePDF(Integer invoiceId) {
         return session.getPaperInvoicePDF(invoiceId);
     }
 
     public boolean notifyInvoiceByEmail(Integer invoiceId) {
         return session.notifyInvoiceByEmail(invoiceId);
     }
 
     public boolean notifyPaymentByEmail(Integer paymentId) {
         return session.notifyPaymentByEmail(paymentId);
     }
     
     public Integer[] getLastInvoices(Integer userId, Integer number) {
         return session.getLastInvoices(userId, number);
     }
 
     public Integer[] getUserInvoicesByDate(Integer userId, String since, String until) {
         return session.getUserInvoicesByDate(userId, since, until);
     }
 
     public Integer[] getUnpaidInvoices(Integer userId) {
         return session.getUnpaidInvoices(userId);
     }
 
     public Integer[] getLastInvoicesByItemType(Integer userId, Integer itemTypeId, Integer number) {
         return session.getLastInvoicesByItemType(userId, itemTypeId, number);
     }
 
     public Integer[] getLastOrders(Integer userId, Integer number) {
         return session.getLastOrders(userId, number);
     }
 
 
     public Integer[] getLastOrdersByItemType(Integer userId, Integer itemTypeId, Integer number) {
         return session.getLastOrdersByItemType(userId, itemTypeId, number);
     }
 
     public OrderWS getCurrentOrder(Integer userId, Date date) {
         return session.getCurrentOrder(userId, date);
     }
 
     public OrderWS updateCurrentOrder(Integer userId, OrderLineWS[] lines, PricingField[] fields, Date date,
                                       String eventDescription) {
 
         return session.updateCurrentOrder(userId, lines, PricingField.setPricingFieldsValue(fields), date,
                                           eventDescription);
     }
 
     public Integer[] getLastPayments(Integer userId, Integer number) {
         return session.getLastPayments(userId, number);
     }
 
     public PaymentWS getUserPaymentInstrument(Integer userId) {
         return session.getUserPaymentInstrument(userId);
     }
 
     public Integer[] getAllInvoices(Integer userId) {
         return session.getAllInvoices(userId);
     }
 
     public InvoiceWS getLatestInvoice(Integer userId) {
         return session.getLatestInvoice(userId);
     }
 
     public InvoiceWS getLatestInvoiceByItemType(Integer userId, Integer itemTypeId) {
         return session.getLatestInvoiceByItemType(userId, itemTypeId);
     }
 
     public OrderWS getLatestOrder(Integer userId) {
         return session.getLatestOrder(userId);
     }
 
     public OrderWS getLatestOrderByItemType(Integer userId, Integer itemTypeId) {
         return session.getLatestOrderByItemType(userId, itemTypeId);
     }
 
     public PaymentWS getLatestPayment(Integer userId) {
         return session.getLatestPayment(userId);
     }
 
     public OrderWS getOrder(Integer orderId) {
         return session.getOrder(orderId);
     }
 
     public Integer[] getOrderByPeriod(Integer userId, Integer periodId) {
         return session.getOrderByPeriod(userId, periodId);
     }
 
     public OrderLineWS getOrderLine(Integer orderLineId) {
         return session.getOrderLine(orderLineId);
     }
 
     public PaymentWS getPayment(Integer paymentId) {
         return session.getPayment(paymentId);
     }
 
     public ContactWS[] getUserContactsWS(Integer userId) {
         return session.getUserContactsWS(userId);
     }
 
     public Integer getUserId(String username) {
         return session.getUserId(username);
     }
 
     public UserTransitionResponseWS[] getUserTransitions(Date from, Date to) {
         return session.getUserTransitions(from, to);
     }
 
     public UserTransitionResponseWS[] getUserTransitionsAfterId(Integer id) {
         return session.getUserTransitionsAfterId(id);
     }
 
     public UserWS getUserWS(Integer userId) {
         return session.getUserWS(userId);
     }
 
     public Integer[] getUsersByCustomField(Integer typeId, String value) {
         return session.getUsersByCustomField(typeId, value);
     }
 
     public Integer[] getUsersByCustomFields(ContactFieldWS[] fields) {
         return session.getUsersByCustomFields(fields);
     }
 
     public Integer[] getUsersByStatus(Integer statusId, boolean in) {
         return session.getUsersByStatus(statusId, in);
     }
 
     public Integer[] getUsersInStatus(Integer statusId) {
         return session.getUsersInStatus(statusId);
     }
 
     public Integer[] getUsersNotInStatus(Integer statusId) {
         return session.getUsersNotInStatus(statusId);
     }
 
     public void createPaymentLink(Integer invoiceId, Integer paymentId) {
         session.createPaymentLink(invoiceId, paymentId);
     }
 
     public void removePaymentLink(Integer invoiceId, Integer paymentId) {
         session.removePaymentLink(invoiceId, paymentId);
     }
 
     public PaymentAuthorizationDTOEx payInvoice(Integer invoiceId) {
         return session.payInvoice(invoiceId);
     }
 
     public Integer createPayment(PaymentWS payment) {
         return session.createPayment(payment);
     }
 
     public void updatePayment(PaymentWS payment) {
         session.updatePayment(payment);
     }
 
     public void deletePayment(Integer paymentId) {
         session.deletePayment(paymentId);
     }
 
     public void updateCreditCard(Integer userId, CreditCardDTO creditCard) {
         session.updateCreditCard(userId, creditCard);
     }
 
     public void deleteCreditCard(Integer userId) {
         session.deleteCreditCard(userId);
     }
 
     public void updateAch(Integer userId, AchDTO ach) {
         session.updateAch(userId, ach);
     }
 
     public void deleteAch(Integer userId) {
         session.deleteAch(userId);
     }
 
     public void updateOrder(OrderWS order) {
         session.updateOrder(order);
     }
 
     public Integer createUpdateOrder(OrderWS order) {
         return session.createUpdateOrder(order);
     }
 
     public void updateOrderLine(OrderLineWS line) {
         session.updateOrderLine(line);
     }
 
     public void updateUser(UserWS user) {
         session.updateUser(user);
     }
 
     public void updateUserContact(Integer userId, Integer typeId, ContactWS contact) {
         session.updateUserContact(userId, typeId, contact);
     }
 
     public ContactTypeWS getContactTypeWS(Integer contactTypeId) {
         return session.getContactTypeWS(contactTypeId);
     }
 
     public Integer createContactTypeWS(ContactTypeWS contactType) {
         return session.createContactTypeWS(contactType);
     }
 
     public Integer[] getUsersByCreditCard(String number) {
         return session.getUsersByCreditCard(number);
     }
 
     public ItemDTOEx getItem(Integer itemId, Integer userId, PricingField[] fields) {
         return session.getItem(itemId, userId, PricingField.setPricingFieldsValue(fields));
     }
 
     public OrderWS rateOrder(OrderWS order) {
         return session.rateOrder(order);
     }
 
     public OrderWS[] rateOrders(OrderWS orders[]) {
         return session.rateOrders(orders);
     }
 
     public void updateItem(ItemDTOEx item) {
         session.updateItem(item);
     }
 
     public void deleteItem(Integer itemId) {
         session.deleteItem(itemId);
     }
 
     public Integer[] createInvoice(Integer userId, boolean onlyRecurring) {
         return session.createInvoice(userId, onlyRecurring);
     }
 
    public Integer[] createInvoice(Integer userId, Date billingDate, Integer dueDatePeriodId, Integer dueDatePeriodValue, boolean onlyRecurring) {
        return session.createInvoice(userId, billingDate, dueDatePeriodId, dueDatePeriodValue, onlyRecurring);
     }
 
     public Integer createInvoiceFromOrder(Integer orderId, Integer invoiceId) {
         return session.createInvoiceFromOrder(orderId, invoiceId);
     }
 
     public String isUserSubscribedTo(Integer userId, Integer itemId) {
         return session.isUserSubscribedTo(userId, itemId);
     }
 
     public Integer[] getUserItemsByCategory(Integer userId, Integer categoryId) {
         return session.getUserItemsByCategory(userId, categoryId);
     }
 
     public ItemDTOEx[] getItemByCategory(Integer itemTypeId) {
         return session.getItemByCategory(itemTypeId);
     }
 
     public ItemTypeWS[] getAllItemCategories() {
         return session.getAllItemCategories();
     }
 
     public ValidatePurchaseWS validatePurchase(Integer userId, Integer itemId, PricingField[] fields) {
         return session.validatePurchase(userId, itemId, PricingField.setPricingFieldsValue(fields));
     }
 
     public ValidatePurchaseWS validateMultiPurchase(Integer userId, Integer[] itemIds, PricingField[][] fields) {
         String[] pricingFields = null;
         if (fields != null) {
             pricingFields = new String[fields.length];
             for (int i = 0; i < pricingFields.length; i++) {
                 pricingFields[i] = PricingField.setPricingFieldsValue(fields[i]);
             }
         }
         return session.validateMultiPurchase(userId, itemIds, pricingFields);
     }
 
     public Integer createItemCategory(ItemTypeWS itemType) {
         return session.createItemCategory(itemType);
     }
 
     public void updateItemCategory(ItemTypeWS itemType) {
         session.updateItemCategory(itemType);
     }
 
     public void deleteItemCategory(Integer itemCategoryId) {
         session.deleteItemCategory(itemCategoryId);
     }
 
     public Integer getAutoPaymentType(Integer userId) {
         return session.getAuthPaymentType(userId);
     }
 
     public void setAutoPaymentType(Integer userId, Integer autoPaymentType, boolean use) {
         session.setAuthPaymentType(userId, autoPaymentType, use);
     }
 
     /*
         Billing process
      */
 
     public void triggerBillingAsync(Date runDate) {
         session.triggerBillingAsync(runDate);
     }
 
     public boolean triggerBilling(Date runDate) {
         return session.triggerBilling(runDate);
     }
 
     public boolean isBillingProcessRunning() {
         return session.isBillingProcessRunning();
     }
 
     public ProcessStatusWS getBillingProcessStatus() {
         return session.getBillingProcessStatus();
     }
 
     public void triggerAgeing(Date runDate) {
         session.triggerAgeing(runDate);
     }
 
     public boolean isAgeingProcessRunning() {
         return session.isAgeingProcessRunning();
     }
 
     public ProcessStatusWS getAgeingProcessStatus() {
         return session.getAgeingProcessStatus();
     }
 
     public BillingProcessConfigurationWS getBillingProcessConfiguration() {
         return session.getBillingProcessConfiguration();
     }
 
     public Integer createUpdateBillingProcessConfiguration(BillingProcessConfigurationWS ws) {
         return session.createUpdateBillingProcessConfiguration(ws);
     }
 
     public BillingProcessWS getBillingProcess(Integer processId) {
         return session.getBillingProcess(processId);
     }
 
     public Integer getLastBillingProcess() {
         return session.getLastBillingProcess();
     }
 
     public List<OrderProcessWS> getOrderProcesses(Integer orderId) {
         return session.getOrderProcesses(orderId);
     }
 
     public List<OrderProcessWS> getOrderProcessesByInvoice(Integer invoiceId) {
         return session.getOrderProcessesByInvoice(invoiceId);
     }
 
     public BillingProcessWS getReviewBillingProcess() {
         return session.getReviewBillingProcess();
     }
 
     public BillingProcessConfigurationWS setReviewApproval(Boolean flag) {
         return session.setReviewApproval(flag);
     }
 
     public List<Integer> getBillingProcessGeneratedInvoices(Integer processId) {
         return session.getBillingProcessGeneratedInvoices(processId);
     }
 
     public AgeingWS[] getAgeingConfiguration(Integer languageId) {
         return session.getAgeingConfiguration(languageId);
     }
 
     public void saveAgeingConfiguration(AgeingWS[] steps, Integer gracePeriod,
             Integer languageId) {
         session.saveAgeingConfiguration(steps, gracePeriod, languageId);
     }
 
 
     /*
        Mediation process
     */
 
     public void triggerMediation() {
         session.triggerMediation();
     }
 
     public Integer triggerMediationByConfiguration(Integer cfgId) {
         return session.triggerMediationByConfiguration(cfgId);
     }
 
     public boolean isMediationProcessRunning() {
         return session.isMediationProcessRunning();
     }
 
     public ProcessStatusWS getMediationProcessStatus() {
         return session.getMediationProcessStatus();
     }
 
     public MediationProcessWS getMediationProcess(Integer mediationProcessId) {
         return session.getMediationProcess(mediationProcessId);
     }
 
     public List<MediationProcessWS> getAllMediationProcesses() {
         return session.getAllMediationProcesses();
     }
 
     public List<MediationRecordLineWS> getMediationEventsForOrder(Integer orderId) {
         return session.getMediationEventsForOrder(orderId);
     }
 
     public List<MediationRecordLineWS> getMediationEventsForInvoice(Integer invoiceId) {
         return session.getMediationEventsForInvoice(invoiceId);
     }
 
     public List<MediationRecordWS> getMediationRecordsByMediationProcess(Integer mediationProcessId) {
         return session.getMediationRecordsByMediationProcess(mediationProcessId);
     }
 
     public List<RecordCountWS> getNumberOfMediationRecordsByStatuses() {
 
         return session.getNumberOfMediationRecordsByStatuses();
     }
 
     public List<MediationConfigurationWS> getAllMediationConfigurations() {
 
         return session.getAllMediationConfigurations();
     }
 
     public void createMediationConfiguration(MediationConfigurationWS cfg) {
         session.createMediationConfiguration(cfg);
     }
 
     public List<Integer> updateAllMediationConfigurations(List<MediationConfigurationWS> configurations) {
         return session.updateAllMediationConfigurations(configurations);
     }
 
     public void deleteMediationConfiguration(Integer cfgId) {
         session.deleteMediationConfiguration(cfgId);
     }
 
 
     /*
        Provisioning process
     */
 
     public void triggerProvisioning() {
         session.triggerProvisioning();
     }
 
     public void updateOrderAndLineProvisioningStatus(Integer inOrderId, Integer inLineId, String result) {
         session.updateOrderAndLineProvisioningStatus(inOrderId, inLineId, result);
     }
 
     public void updateLineProvisioningStatus(Integer orderLineId, Integer provisioningStatus) {
         session.updateLineProvisioningStatus(orderLineId, provisioningStatus);
     }
 
 
     /*
        Utilities
     */
 
     public void generateRules(String rulesData) {
         session.generateRules(rulesData);
     }
 
 
     /*
         Preferences
      */
 
     public void updatePreferences(PreferenceWS[] prefList) {
         session.updatePreferences(prefList);
     }
 
     public void updatePreference(PreferenceWS preference) {
         session.updatePreference(preference);
     }
 
     public PreferenceWS getPreference(Integer preferenceTypeId) {
         return session.getPreference(preferenceTypeId);
     }
 
 
     /*
         Currencies
      */
 
     public CurrencyWS[] getCurrencies() {
         return session.getCurrencies();
     }
 
     public void updateCurrencies(CurrencyWS[] currencies) {
         session.updateCurrencies(currencies);
     }
 
     public void updateCurrency(CurrencyWS currency) {
         session.updateCurrency(currency);
     }
 
     public Integer createCurrency(CurrencyWS currency) {
         return session.createCurrency(currency);
     }
 
 
     /*
        Plug-ins
     */
 
     public PluggableTaskWS getPluginWS(Integer pluginId) {
         return session.getPluginWS(pluginId);
     }
 
     public Integer createPlugin(PluggableTaskWS plugin) {
         return session.createPlugin(plugin);
     }
 
     public void updatePlugin(PluggableTaskWS plugin) {
         session.updatePlugin(plugin);
     }
 
     public void deletePlugin(Integer plugin) {
         session.deletePlugin(plugin);
     }
 
 
     /*
         Plans and special pricing
      */
 
     public PlanWS getPlanWS(Integer planId) {
         return session.getPlanWS(planId);
     }
 
     public Integer createPlan(PlanWS plan) {
         return session.createPlan(plan);
     }
 
     public void updatePlan(PlanWS plan) {
         session.updatePlan(plan);
     }
 
     public void deletePlan(Integer planId) {
         session.deletePlan(planId);
     }
 
     public void addPlanPrice(Integer planId, PlanItemWS price) {
         session.addPlanPrice(planId, price);
     }
 
     public boolean isCustomerSubscribed(Integer planId, Integer userId) {
         return session.isCustomerSubscribed(planId, userId);
     }
 
     public Integer[] getSubscribedCustomers(Integer planId) {
         return session.getSubscribedCustomers(planId);
     }
 
     public Integer[] getPlansBySubscriptionItem(Integer itemId) {
         return session.getPlansBySubscriptionItem(itemId);
     }
 
     public Integer[] getPlansByAffectedItem(Integer itemId) {
         return session.getPlansByAffectedItem(itemId);
     }
 
     public PlanItemWS createCustomerPrice(Integer userId, PlanItemWS planItem) {
         return session.createCustomerPrice(userId, planItem);
     }
 
     public void updateCustomerPrice(Integer userId, PlanItemWS planItem) {
         session.updateCustomerPrice(userId, planItem);
     }
 
     public void deleteCustomerPrice(Integer userId, Integer planItemId) {
         session.deleteCustomerPrice(userId, planItemId);
     }
 
     public PlanItemWS[] getCustomerPrices(Integer userId) {
         return session.getCustomerPrices(userId);
     }
 
     public PlanItemWS getCustomerPrice(Integer userId, Integer itemId) {
         return session.getCustomerPrice(userId, itemId);
     }
 
     public BigDecimal getTotalRevenueByUser(Integer userId) {
         return session.getTotalRevenueByUser(userId);
     }
 
     public CompanyWS getCompany() {
         return session.getCompany();
     }
 
     public Integer getCallerCompanyId() {
         return session.getCallerCompanyId();
     }
 
     public Integer getCallerId() {
         return session.getCallerId();
     }
 
     public Integer getCallerLanguageId() {
         return session.getCallerLanguageId();
     }
 
     public InvoiceWS[] getAllInvoicesForUser(Integer userId) {
         return session.getAllInvoicesForUser(userId);
     }
 
     public OrderWS[] getUserSubscriptions(Integer userId) {
         return session.getUserSubscriptions(userId);
     }
 
     public boolean deleteOrderPeriod(Integer periodId) {
         return session.deleteOrderPeriod(periodId);
     }
 
     public boolean updateOrderPeriods(OrderPeriodWS[] orderPeriods) {
         return session.updateOrderPeriods(orderPeriods);
     }
 
     public boolean updateOrCreateOrderPeriod(OrderPeriodWS orderPeriod) {
         return session.updateOrCreateOrderPeriod(orderPeriod);
     }
     
     public void createUpdateNofications(Integer messageId, MessageDTO dto) {
         session.createUpdateNofications(messageId, dto);
     }
 
     public void saveCustomContactField(ContactFieldTypeWS ws) {
         session.saveCustomContactField(ws);
     }
 
     public void saveCustomerNotes(Integer userId, String notes) {
         session.saveCustomerNotes(userId, notes);
     }
 
     public void updateCompany(CompanyWS companyWS) {
         session.updateCompany(companyWS);
     }
     
 }
