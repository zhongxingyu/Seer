 /*
  * JBILLING CONFIDENTIAL
  * _____________________
  *
  * [2003] - [2012] Enterprise jBilling Software Ltd.
  * All Rights Reserved.
  *
  * NOTICE:  All information contained herein is, and remains
  * the property of Enterprise jBilling Software.
  * The intellectual and technical concepts contained
  * herein are proprietary to Enterprise jBilling Software
  * and are protected by trade secret or copyright law.
  * Dissemination of this information or reproduction of this material
  * is strictly forbidden.
  */
 package com.sapienter.jbilling.server.item;
 
 import com.sapienter.jbilling.server.order.OrderLineWS;
 import com.sapienter.jbilling.server.order.OrderWS;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.CurrencyWS;
 import com.sapienter.jbilling.server.util.api.JbillingAPI;
 import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;
 import junit.framework.TestCase;
 
 import java.math.BigDecimal;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * @author kkulagin
  * @since 10.01.12
  */
 public class CurrencyExchangeTest extends TestCase {
 
     private static final Integer SYSTEM_CURRENCY_ID = 1;
     private static final Integer CALLS_ITEM_TYPE_ID = 2200;
     private static final Integer AUD_CURRENCY_ID = 11;
     private static final Integer USER_WITH_SYSTEM_CURRENCY_ID = 2;
 
 
     /**
      * should remove record for specified date with null amount
      */
     public void testRecordRemoving() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         final CurrencyWS[] currencies = api.getCurrencies();
         final CurrencyWS audCurrency = getCurrencyById(AUD_CURRENCY_ID, currencies);
         final BigDecimal rate = new BigDecimal("999999.0");
         audCurrency.setRate(rate);
         audCurrency.setFromDate(new Date());
         // this should add new currency exchange record which current date
         api.updateCurrency(audCurrency);
 
         final Integer currency1Id = audCurrency.getId();
 
         final CurrencyWS[] currenciesAfterRateUpdate = api.getCurrencies();
         final CurrencyWS currency1AfterRateUpdate = getCurrencyById(currency1Id, currenciesAfterRateUpdate);
         // check that current rate has a correct value
         assertEquals(rate.compareTo(currency1AfterRateUpdate.getRateAsDecimal()), 0);
 
         currency1AfterRateUpdate.setFromDate(new Date());
         currency1AfterRateUpdate.setRate((String) null);
         // this should remove currencyexchange record
         api.updateCurrency(currency1AfterRateUpdate);
 
         final CurrencyWS[] currenciesAfterRemove = api.getCurrencies();
         final CurrencyWS currency1AfterRemove = getCurrencyById(currency1Id, currenciesAfterRemove);
         assertNotSame(currency1AfterRemove.getRateAsDecimal(), rate);
     }
 
 
     /**
      * This will create 2 different exchange rates for AUD for 2 different dates.
      * After that create an Item and an Order with this Item.
      * Depending on order's ActiveSince property price should vary correspondingly
      */
     public void testRecordSomething() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         final CurrencyWS[] currencies = api.getCurrencies();
         final CurrencyWS audCurrency = getCurrencyById(AUD_CURRENCY_ID, currencies);
 
         Date date1 = getDate1();
         Date date2 = getDate2();
 
         BigDecimal firstDateExchangeRate = new BigDecimal("10.0");
         BigDecimal secondDateExchangeRate = new BigDecimal("100.0");
         BigDecimal itemPrice = new BigDecimal("1.0");
 
         try {
             audCurrency.setRate(firstDateExchangeRate);
             audCurrency.setFromDate(date1);
             api.updateCurrency(audCurrency);
 
             audCurrency.setRate(secondDateExchangeRate);
             audCurrency.setFromDate(date2);
             api.updateCurrency(audCurrency);
 
             Integer itemId = null;
 
             try {
                 ItemDTOEx item = new ItemDTOEx();
                 item.setCurrencyId(SYSTEM_CURRENCY_ID);
                 item.setPrice(itemPrice);
                 item.setDescription("Test Item for Currency Exchange");
                 item.setEntityId(1);
                 item.setNumber("Number");
                 item.setTypes(new Integer[]{CALLS_ITEM_TYPE_ID});
                 itemId = api.createItem(item);
                 item.setId(itemId);
 
 
                 assertNotNull("item created", item.getId());
 
                 Integer orderId = null;
                 try {
                     OrderWS order = new OrderWS();
                     order.setUserId(USER_WITH_SYSTEM_CURRENCY_ID);
                     order.setBillingTypeId(Constants.ORDER_BILLING_POST_PAID);
                     order.setPeriod(1);
                     order.setCurrencyId(AUD_CURRENCY_ID);
                     order.setActiveSince(date1);
 
                     OrderLineWS line = new OrderLineWS();
                     line.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
                     line.setItemId(itemId);
                     line.setUseItem(true);
                     line.setQuantity(1);
                     order.setOrderLines(new OrderLineWS[]{line});
 
                     orderId = api.createOrder(order);
                     order.setId(orderId); // create order
 
                     final OrderWS result = api.rateOrder(order);
 
                     final OrderLineWS[] orderLines = result.getOrderLines();
                     final BigDecimal amount = orderLines[0].getAmountAsDecimal();
                     assertEquals(amount.compareTo(firstDateExchangeRate.multiply(itemPrice)), 0);
 
                 } finally {
                     if (orderId != null) {
                         try {
                             api.deleteOrder(orderId);
                         } catch (Throwable e) {
                         }
                     }
                 }
 
                 orderId = null;
                 try {
                     OrderWS order = new OrderWS();
                     order.setUserId(USER_WITH_SYSTEM_CURRENCY_ID);
                     order.setBillingTypeId(Constants.ORDER_BILLING_POST_PAID);
                     order.setPeriod(1);
                     order.setCurrencyId(AUD_CURRENCY_ID);
                     order.setActiveSince(date2);
 
                     OrderLineWS line = new OrderLineWS();
                     line.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
                     line.setItemId(itemId);
                     line.setUseItem(true);
                     line.setQuantity(1);
                     order.setOrderLines(new OrderLineWS[]{line});
 
                     orderId = api.createOrder(order);
                     order.setId(orderId); // create order
 
                     final OrderWS result = api.rateOrder(order);
 
                     final OrderLineWS[] orderLines = result.getOrderLines();
                     final BigDecimal amount = orderLines[0].getAmountAsDecimal();
                     assertEquals(amount.compareTo(secondDateExchangeRate.multiply(itemPrice)), 0);
                 } finally {
                     if (orderId != null) {
                         try {
                             api.deleteOrder(orderId);
                         } catch (Throwable e) {
                         }
                     }
                 }
 
 
             } finally {
                 if (itemId != null) {
                     try {
                         api.deleteItem(itemId);
                     } catch (Throwable e) {
                     }
                 }
             }
         } finally {
             // clear currency exchange records
             audCurrency.setRate((String) null);
             audCurrency.setFromDate(date1);
             api.updateCurrency(audCurrency);
 
             audCurrency.setRate((String) null);
             audCurrency.setFromDate(date2);
             api.updateCurrency(audCurrency);
 
         }
 
     }
 
     private static CurrencyWS getCurrencyById(Integer currencyId, CurrencyWS[] currencies) {
         for (CurrencyWS currency : currencies) {
             if (currencyId.equals(currency.getId())) {
                 return currency;
             }
         }
         throw new IllegalStateException("Currency with id = " + currencyId + " not found.");
     }
 
     private static Date getDate1() {
         final Calendar calendar = Calendar.getInstance();
         calendar.set(Calendar.YEAR, 2100);
         calendar.set(Calendar.MONTH, Calendar.MARCH);
         calendar.set(Calendar.DAY_OF_MONTH, 1);
         return calendar.getTime();
     }
 
     private static Date getDate2() {
         final Calendar calendar = Calendar.getInstance();
         calendar.set(Calendar.YEAR, 2100);
         calendar.set(Calendar.MONTH, Calendar.APRIL);
         calendar.set(Calendar.DAY_OF_MONTH, 15);
         return calendar.getTime();
     }
 }
