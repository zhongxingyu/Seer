 /*
  JBILLING CONFIDENTIAL
  _____________________
 
  [2003] - [2012] Enterprise jBilling Software Ltd.
  All Rights Reserved.
 
  NOTICE:  All information contained herein is, and remains
  the property of Enterprise jBilling Software.
  The intellectual and technical concepts contained
  herein are proprietary to Enterprise jBilling Software
  and are protected by trade secret or copyright law.
  Dissemination of this information or reproduction of this material
  is strictly forbidden.
  */
 
 package com.sapienter.jbilling.server.task;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Hashtable;
 
 import junit.framework.TestCase;
 
 import com.sapienter.jbilling.server.entity.AchDTO;
 import com.sapienter.jbilling.server.entity.CreditCardDTO;
 import com.sapienter.jbilling.server.entity.InvoiceLineDTO;
 import com.sapienter.jbilling.server.invoice.InvoiceWS;
 import com.sapienter.jbilling.server.item.ItemDTOEx;
 import com.sapienter.jbilling.server.item.PricingField;
 import com.sapienter.jbilling.server.order.OrderLineWS;
 import com.sapienter.jbilling.server.order.OrderWS;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskWS;
 import com.sapienter.jbilling.server.user.ContactWS;
 import com.sapienter.jbilling.server.user.UserDTOEx;
 import com.sapienter.jbilling.server.user.UserWS;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.api.JbillingAPI;
 import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;
 
 //
 
 
 /**
  * @author Alexander Aksenov, Vikas Bodani
  * @since 30.04.11
  */
 public class SimpleTaxCompositionTaskTest extends TestCase {
 
     private static final Integer SIMPLE_TAX_PLUGIN_TYPE_ID = 86;
 
    private final static String PLUGIN_PARAM_TAX_ITEM_ID= "tax_item_id";
     private final static String PLUGIN_PARAM_EXEMPT_ITEM_CATEGORY_ID= "item_exempt_category_id";
 
     private static final Integer TAX_EXEMPT_ITEM_TYPE_ID = 2200;
     private static final Integer FEE_ITEM_TYPE_ID = 22;
     
     private static final Integer LEMONADE_ITEM_ID = 2602;            // taxable item
     private static final Integer LONG_DISTANCE_PLAN_ITEM_ID = 2700;  // tax-exempt item
 
 
     public void testInvoiceWithTaxableItems() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         // add a new tax item & enable the tax plug-in
         ItemDTOEx item = new ItemDTOEx();
         item.setCurrencyId(1);
         item.setPercentage((BigDecimal) null);     // not a percentage
         item.setPrice(new BigDecimal("10.00"));    // $10 flat fee
         item.setHasDecimals(1);
         item.setDescription("Tax");
         item.setEntityId(1);
         item.setNumber("TAX");
         item.setTypes(new Integer[] { FEE_ITEM_TYPE_ID });
 
         item.setId(api.createItem(item));
         assertNotNull("tax item created", item.getId());
 
         Integer pluginId = enableTaxPlugin(api, item.getId());
 
 
         // create a user for testing
         UserWS user = new UserWS();
         user.setUserName("simple-tax-01-" + new Date().getTime());
         user.setPassword("password");
         user.setLanguageId(1);
         user.setCurrencyId(1);
         user.setMainRoleId(5);
         user.setStatusId(UserDTOEx.STATUS_ACTIVE);
         user.setBalanceType(Constants.BALANCE_NO_DYNAMIC);
 
         ContactWS contact = new ContactWS();
         contact.setEmail("test@test.com");
         contact.setFirstName("Simple Tax Test");
         contact.setLastName("Flat Rate");
         user.setContact(contact);
 
         user.setUserId(api.createUser(user)); // create user
         assertNotNull("customer created", user.getUserId());
 
 
         // purchase order with taxable items
         OrderWS order = new OrderWS();
         order.setUserId(user.getUserId());
         order.setBillingTypeId(Constants.ORDER_BILLING_POST_PAID);
         order.setPeriod(1);
         order.setCurrencyId(1);
         order.setActiveSince(new Date());
 
         OrderLineWS line = new OrderLineWS();
         line.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
         line.setItemId(LEMONADE_ITEM_ID);
         line.setUseItem(true);
         line.setQuantity(10);
         order.setOrderLines(new OrderLineWS[] { line });
 
         order.setId(api.createOrder(order)); // create order
         order = api.getOrder(order.getId());
         assertNotNull("order created", order.getId());
 
 
         // generate an invoice and verify the taxes
         Integer invoiceId = api.createInvoiceFromOrder(order.getId(), null);
         InvoiceWS invoice = api.getInvoiceWS(invoiceId);
 
         assertNotNull("invoice generated", invoice);
         assertEquals("two lines in invoice, lemonade and the tax line", 2, invoice.getInvoiceLines().length);
 
         boolean foundTaxItem = false;
         boolean foundLemonadeItem = false;
 
         for (InvoiceLineDTO invoiceLine : invoice.getInvoiceLines()) {
 
             // purchased lemonade
             if (invoiceLine.getItemId().equals(LEMONADE_ITEM_ID)) {
                 assertEquals("lemonade item", "Lemonade ", invoiceLine.getDescription());
                 assertEquals("lemonade $35", new BigDecimal("35"), invoiceLine.getAmountAsDecimal());
                 foundLemonadeItem = true;
             }
 
             // tax is a flat fee, not affected by the price of the invoice
             if (invoiceLine.getItemId().equals(item.getId())) {
                 assertEquals("tax item", "Tax line with flat price for tax item " + item.getId(), invoiceLine.getDescription());
                 assertEquals("tax $10", new BigDecimal("10"), invoiceLine.getAmountAsDecimal());
                 foundTaxItem = true;
             }
         }
 
         assertTrue("found and validated tax", foundTaxItem);
         assertTrue("found and validated lemonade", foundLemonadeItem);
 
 
         // cleanup
         disableTaxPlugin(api, pluginId);
         api.deleteItem(item.getId());
         api.deleteOrder(order.getId());
         api.deleteInvoice(invoice.getId());
         api.deleteUser(user.getUserId());
     }
 
     public void testInvoiceWithExemptItems() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         // add a new tax item & enable the tax plug-in
         ItemDTOEx item = new ItemDTOEx();
         item.setCurrencyId(1);
         item.setPercentage(new BigDecimal("10.00"));     // tax is %10
         item.setPrice(item.getPercentage());             //
         item.setHasDecimals(1);
         item.setDescription("Tax");
         item.setEntityId(1);
         item.setNumber("TAX");
         item.setTypes(new Integer[] { FEE_ITEM_TYPE_ID });
 
         item.setId(api.createItem(item));
         assertNotNull("tax item created", item.getId());
 
         Integer pluginId = enableTaxPlugin(api, item.getId());
 
 
         // create a user for testing
         UserWS user = new UserWS();
         user.setUserName("simple-tax-02-" + new Date().getTime());
         user.setPassword("password");
         user.setLanguageId(1);
         user.setCurrencyId(1);
         user.setMainRoleId(5);
         user.setStatusId(UserDTOEx.STATUS_ACTIVE);
         user.setBalanceType(Constants.BALANCE_NO_DYNAMIC);
 
         ContactWS contact = new ContactWS();
         contact.setEmail("test@test.com");
         contact.setFirstName("Simple Tax Test");
         contact.setLastName("% Tax with Exemptions");
         user.setContact(contact);
 
         user.setUserId(api.createUser(user)); // create user
         assertNotNull("customer created", user.getUserId());
 
 
         // purchase order with taxable items
         OrderWS order = new OrderWS();
         order.setUserId(user.getUserId());
         order.setBillingTypeId(Constants.ORDER_BILLING_POST_PAID);
         order.setPeriod(1);
         order.setCurrencyId(1);
         order.setActiveSince(new Date());
 
         OrderLineWS line1 = new OrderLineWS();
         line1.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
         line1.setItemId(LEMONADE_ITEM_ID);
         line1.setUseItem(true);
         line1.setQuantity(10); // $3.5 x 10 = $35 line
 
         OrderLineWS line2 = new OrderLineWS();
         line2.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
         line2.setItemId(LONG_DISTANCE_PLAN_ITEM_ID);
         line2.setUseItem(true);
         line2.setQuantity(2);  // $25 x 2 = $50 line
 
         order.setOrderLines(new OrderLineWS[] { line1, line2 });
 
         order.setId(api.createOrder(order)); // create order
         order = api.getOrder(order.getId());
         assertNotNull("order created", order.getId());
 
 
         // generate an invoice and verify the taxes
         Integer invoiceId = api.createInvoiceFromOrder(order.getId(), null);
         InvoiceWS invoice = api.getInvoiceWS(invoiceId);
 
         assertNotNull("invoice generated", invoice);
         assertEquals("three lines in invoice including tax line", 3, invoice.getInvoiceLines().length);
 
         boolean foundTaxItem = false;
         boolean foundLemonadeItem = false;
         boolean foundExemptItem = false;
 
         for (InvoiceLineDTO invoiceLine : invoice.getInvoiceLines()) {
 
             // purchased lemonade
             if (invoiceLine.getItemId().equals(LEMONADE_ITEM_ID)) {
                 assertEquals("lemonade item", "Lemonade ", invoiceLine.getDescription());
                 assertEquals("lemonade $35", new BigDecimal("35"), invoiceLine.getAmountAsDecimal());
                 foundLemonadeItem = true;
             }
 
             // purchased tax exempt long distance plan
             if (invoiceLine.getItemId().equals(LONG_DISTANCE_PLAN_ITEM_ID)) {
                 assertEquals("long distance item", "Long Distance Plan A - fixed rate", invoiceLine.getDescription());
                 assertEquals("long distance $50", new BigDecimal("50"), invoiceLine.getAmountAsDecimal());
                 foundExemptItem = true;
             }
 
             // tax, %10 of taxable item total ($35 x 0.10 = $3.5)
             // excludes $50 from the tax exempt line
             if (invoiceLine.getItemId().equals(item.getId())) {
                 assertEquals("tax item", "Tax line for percentage tax item " + item.getId(), invoiceLine.getDescription());
                 assertEquals("tax $3.5", new BigDecimal("3.5"), invoiceLine.getAmountAsDecimal());
                 foundTaxItem = true;
             }
         }
 
         assertTrue("found and validated tax", foundTaxItem);
         assertTrue("found and validated lemonade", foundLemonadeItem);
         assertTrue("found and validated exempt item", foundExemptItem);
 
 
         // cleanup
         disableTaxPlugin(api, pluginId);
         api.deleteItem(item.getId());
         api.deleteOrder(order.getId());
         api.deleteInvoice(invoice.getId());
         api.deleteUser(user.getUserId());
     }
 
     public void testInvoiceWithNoTaxableItems() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         // add a new tax item & enable the tax plug-in
         ItemDTOEx item = new ItemDTOEx();
         item.setCurrencyId(1);
         item.setPercentage(new BigDecimal("10.00"));     // tax is %10
         item.setPrice(item.getPercentage());             //
         item.setHasDecimals(1);
         item.setDescription("Tax");
         item.setEntityId(1);
         item.setNumber("TAX");
         item.setTypes(new Integer[] { FEE_ITEM_TYPE_ID });
 
         item.setId(api.createItem(item));
         assertNotNull("tax item created", item.getId());
 
         Integer pluginId = enableTaxPlugin(api, item.getId());
 
 
         // create a user for testing
         UserWS user = new UserWS();
         user.setUserName("simple-tax-03-" + new Date().getTime());
         user.setPassword("password");
         user.setLanguageId(1);
         user.setCurrencyId(1);
         user.setMainRoleId(5);
         user.setStatusId(UserDTOEx.STATUS_ACTIVE);
         user.setBalanceType(Constants.BALANCE_NO_DYNAMIC);
 
         ContactWS contact = new ContactWS();
         contact.setEmail("test@test.com");
         contact.setFirstName("Simple Tax Test");
         contact.setLastName("No Taxable Items");
         user.setContact(contact);
 
         user.setUserId(api.createUser(user)); // create user
         assertNotNull("customer created", user.getUserId());
 
 
         // purchase order without any taxable items
         OrderWS order = new OrderWS();
         order.setUserId(user.getUserId());
         order.setBillingTypeId(Constants.ORDER_BILLING_POST_PAID);
         order.setPeriod(1);
         order.setCurrencyId(1);
         order.setActiveSince(new Date());
 
         OrderLineWS line = new OrderLineWS();
         line.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
         line.setItemId(LONG_DISTANCE_PLAN_ITEM_ID);
         line.setUseItem(true);
         line.setQuantity(2);  // $25 x 2 = $50 line
 
         order.setOrderLines(new OrderLineWS[] { line });
 
         order.setId(api.createOrder(order)); // create order
         order = api.getOrder(order.getId());
         assertNotNull("order created", order.getId());
 
 
         // generate an invoice and verify the taxes
         Integer invoiceId = api.createInvoiceFromOrder(order.getId(), null);
         InvoiceWS invoice = api.getInvoiceWS(invoiceId);
 
         assertNotNull("invoice generated", invoice);
         assertEquals("tow lines in invoice including tax line, tax should be $0", 2, invoice.getInvoiceLines().length);
 
         boolean foundTaxItem = false;
         boolean foundExemptItem = false;
 
         for (InvoiceLineDTO invoiceLine : invoice.getInvoiceLines()) {
 
             // purchased tax exempt long distance plan
             if (invoiceLine.getItemId().equals(LONG_DISTANCE_PLAN_ITEM_ID)) {
                 assertEquals("long distance item", "Long Distance Plan A - fixed rate", invoiceLine.getDescription());
                 assertEquals("long distance $50", new BigDecimal("50"), invoiceLine.getAmountAsDecimal());
                 foundExemptItem = true;
             }
 
             // tax, but no taxable items on order
             // value of tax should be $0
             if (invoiceLine.getItemId().equals(item.getId())) {
                 assertEquals("tax item", "Tax line for percentage tax item " + item.getId(), invoiceLine.getDescription());
                 assertEquals("tax $0", new BigDecimal("0"), invoiceLine.getAmountAsDecimal());
                 foundTaxItem = true;
             }
         }
 
         assertTrue("found and validated tax", foundTaxItem);
         assertTrue("found and validated exempt item", foundExemptItem);
 
 
         // cleanup
         disableTaxPlugin(api, pluginId);
         api.deleteItem(item.getId());
         api.deleteOrder(order.getId());
         api.deleteInvoice(invoice.getId());
         api.deleteUser(user.getUserId());
     }
 
 
 
     /*
        Enable/disable the SimpleTaxCompositionTask plug-in.
     */
 
     public Integer enableTaxPlugin(JbillingAPI api, Integer itemId) {
         PluggableTaskWS plugin = new PluggableTaskWS();
         plugin.setTypeId(SIMPLE_TAX_PLUGIN_TYPE_ID);
         plugin.setProcessingOrder(4);
 
         // plug-in adds the given tax item to the invoice
         // when the customer purchase an item outside of the exempt category
         Hashtable<String, String> parameters = new Hashtable<String, String>();
         parameters.put(PLUGIN_PARAM_TAX_ITEM_ID, itemId.toString());
         parameters.put(PLUGIN_PARAM_EXEMPT_ITEM_CATEGORY_ID, TAX_EXEMPT_ITEM_TYPE_ID.toString());
         plugin.setParameters(parameters);
 
         return api.createPlugin(plugin);
     }
 
     public void disableTaxPlugin(JbillingAPI api, Integer pluginId) {
         api.deletePlugin(pluginId);
     }
 
 
 
     /*
        Convenience assertions for BigDecimal comparisons.
     */
 
     public static void assertEquals(BigDecimal expected, BigDecimal actual) {
         assertEquals(null, expected, actual);
     }
 
     public static void assertEquals(String message, BigDecimal expected, BigDecimal actual) {
         assertEquals(message,
                 (Object) (expected == null ? null : expected.setScale(2, RoundingMode.HALF_UP)),
                 (Object) (actual == null ? null : actual.setScale(2, RoundingMode.HALF_UP)));
     }    
 }
