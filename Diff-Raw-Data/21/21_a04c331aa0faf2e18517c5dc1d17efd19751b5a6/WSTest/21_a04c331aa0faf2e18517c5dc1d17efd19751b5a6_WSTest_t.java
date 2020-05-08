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
 
 /*
  * Created on Dec 18, 2003
  *
  */
 package com.sapienter.jbilling.server.order;
 
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.server.entity.InvoiceLineDTO;
 import com.sapienter.jbilling.server.invoice.InvoiceWS;
 import com.sapienter.jbilling.server.item.ItemDTOEx;
 import com.sapienter.jbilling.server.item.PricingField;
import com.sapienter.jbilling.server.order.OrderWS;
import com.sapienter.jbilling.server.order.db.OrderLineDTO;
 import com.sapienter.jbilling.server.payment.PaymentAuthorizationDTOEx;
 import com.sapienter.jbilling.server.user.UserWS;
 import com.sapienter.jbilling.server.user.ValidatePurchaseWS;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.api.JbillingAPI;
 import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;
 import junit.framework.TestCase;
 import org.joda.time.DateMidnight;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * @author Emil
  */
 public class WSTest  extends TestCase {
 
     private static final Integer GANDALF_USER_ID = 2;
 
     public void testCreateUpdateDelete() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
         int i;
 
         /*
         * Create
         */
         OrderWS newOrder = new OrderWS();
         newOrder.setUserId(GANDALF_USER_ID);
         newOrder.setBillingTypeId(Constants.ORDER_BILLING_PRE_PAID);
         newOrder.setPeriod(Constants.ORDER_PERIOD_ONCE);
         newOrder.setCurrencyId(new Integer(1));
         // notes can only be 200 long... but longer should not fail
         newOrder.setNotes("At the same time the British Crown began bestowing land grants in Nova Scotia on favored subjects to encourage settlement and trade with the mother country. In June 1764, for instance, the Boards of Trade requested the King make massive land grants to such Royal favorites as Thomas Pownall, Richard Oswald, Humphry Bradstreet, John Wentworth, Thomas Thoroton[10] and Lincoln's Inn barrister Levett Blackborne.[11] Two years later, in 1766, at a gathering at the home of Levett Blackborne, an adviser to the Duke of Rutland, Oswald and his friend James Grant were released from their Nova Scotia properties so they could concentrate on their grants in British East Florida.");
 
         Calendar cal = Calendar.getInstance();
         cal.clear();
         cal.set(2008, 9, 3);
         newOrder.setActiveSince(cal.getTime());
         newOrder.setCycleStarts(cal.getTime());
 
         // now add some lines
         OrderLineWS lines[] = new OrderLineWS[3];
         OrderLineWS line;
 
         line = new OrderLineWS();
         line.setPrice(new BigDecimal("10.00"));
         line.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
         line.setQuantity(new Integer(1));
         line.setAmount(new BigDecimal("10.00"));
         line.setDescription("Fist line");
         line.setItemId(new Integer(1));
         lines[0] = line;
 
         // this is an item line
         line = new OrderLineWS();
         line.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
         line.setQuantity(new Integer(1));
         line.setItemId(new Integer(2));
         // take the description from the item
         line.setUseItem(new Boolean(true));
         lines[1] = line;
 
         // this is an item line
         line = new OrderLineWS();
         line.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
         line.setQuantity(new Integer(1));
         line.setItemId(new Integer(3));
         line.setUseItem(new Boolean(true));
         lines[2] = line;
 
         newOrder.setOrderLines(lines);
 
         System.out.println("Creating order ... " + newOrder);
         Integer invoiceId_1 = api.createOrderAndInvoice(newOrder);
         InvoiceWS invoice_1 = api.getInvoiceWS(invoiceId_1);
         Integer orderId_1 = invoice_1.getOrders()[0];
 
         assertNotNull("The order was not created", invoiceId_1);
 
 
         // create another one so we can test get by period.
         Integer invoiceId = api.createOrderAndInvoice(newOrder);
         System.out.println("Created invoice " + invoiceId);
 
         InvoiceWS newInvoice = api.getInvoiceWS(invoiceId);
         Integer orderId = newInvoice.getOrders()[0]; // this is the order that was also created
 
         /*
         * get
         */
         //verify the created order
         // try getting one that doesn't belong to us
         try {
             api.getOrder(new Integer(5));
             fail("Order 5 belongs to entity 2");
         } catch (Exception e) {
         }
 
         System.out.println("Getting created order " + invoiceId);
         OrderWS retOrder = api.getOrder(orderId);
 
         assertEquals("created order billing type", retOrder.getBillingTypeId(), newOrder.getBillingTypeId());
         assertEquals("created order billing period", retOrder.getPeriod(), newOrder.getPeriod());
         assertEquals("created order cycle starts", retOrder.getCycleStarts().getTime(), newOrder.getCycleStarts().getTime());
 
         // cleanup
         api.deleteInvoice(invoiceId);
 
         /*
         * get order line. The new order should include a new discount
         * order line that comes from the rules.
         */
         // try getting one that doesn't belong to us
         try {
             System.out.println("Getting bad order line");
             api.getOrderLine(new Integer(6));
             fail("Order line 6 belongs to entity 6");
         } catch (Exception e) {
         }
         System.out.println("Getting created order line");
 
         // make sure that item 2 has a special price
         for (OrderLineWS item2line: retOrder.getOrderLines()) {
             if (item2line.getItemId() == 2) {
                 assertEquals("Special price for Item 2", new BigDecimal("30.00"), item2line.getPriceAsDecimal());
                 break;
             }
         }
 
         boolean found = false;
         OrderLineWS retOrderLine = null;
         OrderLineWS normalOrderLine = null;
         Integer lineId = null;
         for (i = 0; i < retOrder.getOrderLines().length; i++) {
             lineId = retOrder.getOrderLines()[i].getId();
             retOrderLine = api.getOrderLine(lineId);
             if (retOrderLine.getItemId().equals(new Integer(14))) {
                 assertEquals("created line item id", retOrderLine.getItemId(), new Integer(14));
                 assertEquals("total of discount", new BigDecimal("-5.50"), retOrderLine.getAmountAsDecimal());
                 found = true;
             } else {
                 normalOrderLine = retOrderLine;
                 if (found) break;
             }
         }
         assertTrue("Order line not found", found);
 
         /*
         * Update the order line
         */
         retOrderLine = normalOrderLine; // use a normal one, not the percentage
         retOrderLine.setQuantity(new Integer(99));
 
         try {
             System.out.println("Updating bad order line");
             retOrderLine.setOrderId(5);
             api.updateOrderLine(retOrderLine);
             fail("Order line 6 belongs to entity 301");
         } catch (Exception e) {
         }
         retOrderLine.setOrderId(orderId);
 
         System.out.println("Update order line " + lineId);
         api.updateOrderLine(retOrderLine);
         retOrderLine = api.getOrderLine(retOrderLine.getId());
         assertEquals("updated quantity", new BigDecimal("99.00"), retOrderLine.getQuantityAsDecimal());
 
         //delete a line through updating with quantity = 0
         System.out.println("Delete order line");
         retOrderLine.setQuantity(new Integer(0));
         api.updateOrderLine(retOrderLine);
         int totalLines = retOrder.getOrderLines().length;
         pause(2000); // pause while provisioning status is being updated
         retOrder = api.getOrder(orderId);
 
         // the order has to have one less line now
         assertEquals("order should have one less line", totalLines, retOrder.getOrderLines().length + 1);
 
         /*
         * Update
         */
         // now update the created order
         cal.clear();
         cal.set(2003, 9, 29, 0, 0, 0);
         retOrder.setActiveSince(cal.getTime());
         retOrder.getOrderLines()[1].setDescription("Modified description");
         retOrder.getOrderLines()[1].setQuantity(new Integer(2));
        retOrder.getOrderLines()[1].setUseItem(false);
         retOrder.setStatusId(new Integer(2));
        int orderLineid = retOrder.getOrderLines()[1].getId();
 
         System.out.println("Updating order...");
         api.updateOrder(retOrder);
 
         // try to update an order of another entity
         try {
             System.out.println("Updating bad order...");
             retOrder.setId(new Integer(5));
             api.updateOrder(retOrder);
             fail("Order 5 belongs to entity 2");
         } catch (Exception e) {
         }
 
         // and ask for it to verify the modification
         System.out.println("Getting updated order ");
         retOrder = api.getOrder(orderId);
 
         assertNotNull("Didn't get updated order", retOrder);
         assertTrue("Active since", retOrder.getActiveSince().compareTo(cal.getTime()) == 0);
         assertEquals("Status id", new Integer(2), retOrder.getStatusId());
        for (OrderLineWS updatedLine: retOrder.getOrderLines()) {
        	if (updatedLine.getId() == orderLineid) {
        		assertEquals("Modified line description", "Modified description", updatedLine.getDescription());
                assertEquals("Modified quantity", new BigDecimal("2.00"), updatedLine.getQuantityAsDecimal());
                orderLineid = 0;
                break;
        	}
        }
        
        assertEquals("Order Line updated was not found", 0, orderLineid);
        
         for (i = 0; i < retOrder.getOrderLines().length; i++) {
             retOrderLine = retOrder.getOrderLines()[i];
             if (retOrderLine.getItemId().equals(new Integer(14))) {
                 // the is one less line for 15
                 // but one extra item for 30
                 // difference is 15 and 10% of that is 1.5  thus 5.5 + 1.5 = 7
                 assertEquals("total of discount", new BigDecimal("-7.00"), retOrderLine.getAmountAsDecimal());
                 break;
             }
         }
 
         assertFalse(i == retOrder.getOrderLines().length);
 
         /*
         * Get latest
         */
         System.out.println("Getting latest");
         OrderWS lastOrder = api.getLatestOrder(new Integer(2));
         assertNotNull("Didn't get any latest order", lastOrder);
         assertEquals("Latest id", orderId, lastOrder.getId());
 
         // now one for an invalid user
         System.out.println("Getting latest invalid");
         try {
             retOrder = api.getLatestOrder(new Integer(13));
             fail("User 13 belongs to entity 2");
         } catch (Exception e) {
         }
 
         /*
         * Get last
         */
         System.out.println("Getting last 5 ... ");
         Integer[] list = api.getLastOrders(new Integer(2), new Integer(5));
         assertNotNull("Missing list", list);
         assertTrue("No more than five", list.length <= 5 && list.length > 0);
 
         // the first in the list is the last one created
         retOrder = api.getOrder(new Integer(list[0]));
         assertEquals("Latest id " + Arrays.toString(list), orderId, retOrder.getId());
 
 
         // try to get the orders of my neighbor
         try {
             System.out.println("Getting last 5 - invalid");
             api.getOrder(new Integer(5));
             fail("User 13 belongs to entity 2");
         } catch (Exception e) {
         }
 
         /*
         * Delete
         */
         System.out.println("Deleteing order " + orderId);
         api.deleteOrder(orderId);
 
         // try to delete from my neightbor
         try {
             api.deleteOrder(new Integer(5));
             fail("Order 5 belongs to entity 2");
         } catch (Exception e) {
         }
 
         // try to get the deleted order
         System.out.println("Getting deleted order ");
         retOrder = api.getOrder(orderId);
         assertEquals("Order " + orderId + " should have been deleted", 1, retOrder.getDeleted());
 
         /*
         * Get by user and period
         */
         System.out.println("Getting orders by period for invalid user " + orderId);
 
         // try to get from my neightbor
         try {
             api.getOrderByPeriod(new Integer(13), new Integer(1));
             fail("User 13 belongs to entity 2");
         } catch (Exception e) {
         }
 
         // now from a valid user
         System.out.println("Getting orders by period ");
         Integer orders[] = api.getOrderByPeriod(new Integer(2), new Integer(1));
         System.out.println("Got total orders " + orders.length + " first is " + orders[0]);
 
         /*
         * Create an order with pre-authorization
         */
         System.out.println("Create an order with pre-authorization" + orderId);
         PaymentAuthorizationDTOEx auth = (PaymentAuthorizationDTOEx) api.createOrderPreAuthorize(newOrder);
         assertNotNull("Missing list", auth);
 
         // the test processor should always approve gandalf
         assertEquals("Result is ok", new Boolean(true), auth.getResult());
         System.out.println("Order pre-authorized. Approval code = " + auth.getApprovalCode());
 
         // check the last one is a new one
         pause(2000); // pause while provisioning status is being updated
         System.out.println("Getting latest");
         retOrder = api.getLatestOrder(new Integer(2));
         System.out.println("Order created with ID = " + retOrder.getId());
         assertNotSame("New order is there", retOrder.getId(), lastOrder.getId());
 
         // delete this order
         System.out.println("Deleteing order " + retOrder.getId());
         api.deleteOrder(retOrder.getId());
 
         // cleanup
         api.deleteInvoice(invoiceId_1);
         api.deleteOrder(orderId_1);
     }
 
     public void testcreateOrderAndInvoiceAutoCreatesAnInvoice() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         final int USER_ID = GANDALF_USER_ID;
 
         InvoiceWS before = callGetLatestInvoice(USER_ID);
         assertNotNull(before);
         assertNotNull(before.getId());
 
         OrderWS order = createMockOrder(USER_ID, 3, new BigDecimal("42.00"));
         System.out.println("Creating order/invoice for order: " + order);
 
         Integer invoiceId = callcreateOrderAndInvoice(order);
         assertNotNull(invoiceId);
 
         System.out.println("Created invoice " + invoiceId);
 
         InvoiceWS afterNormalOrder = callGetLatestInvoice(USER_ID);
         assertNotNull("createOrderAndInvoice should create invoice", afterNormalOrder);
         assertNotNull("invoice without id", afterNormalOrder.getId());
 
         if (before != null){
             assertFalse("createOrderAndInvoice should create the most recent invoice", afterNormalOrder.getId().equals(before.getId()));
         }
 
         // cannot create order without lines
         OrderWS emptyOrder = createMockOrder(USER_ID, 0, new BigDecimal("123.00")); // zero lines
         try {
             callcreateOrderAndInvoice(emptyOrder);
             fail("Empty order should fail validation.");
         } catch (SessionInternalError e) {
             assertTrue("Got expected validation exception", true);
         }
 
         // cleanup
         api.deleteInvoice(invoiceId);
         api.deleteOrder(order.getId());
     }
 
     public void testCreateNotActiveOrderDoesNotCreateInvoices() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         final int USER_ID = GANDALF_USER_ID;
         InvoiceWS before = callGetLatestInvoice(USER_ID);
 
         OrderWS orderWS = createMockOrder(USER_ID, 2, new BigDecimal("234.00"));
         orderWS.setActiveSince(weeksFromToday(1));
 
         Integer orderId = api.createOrder(orderWS);
         assertNotNull(orderId);
 
         InvoiceWS after = callGetLatestInvoice(USER_ID);
 
         if (before == null){
             assertNull("Not yet active order -- no new invoices expected", after);
         } else {
             assertEquals("Not yet active order -- no new invoices expected", before.getId(), after.getId());
         }
 
         // cleanup
         api.deleteOrder(orderId);
     }
 
     public void testCreatedOrderIsCorrect() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         final int USER_ID = GANDALF_USER_ID;
         final int LINES = 2;
 
         OrderWS requestOrder = createMockOrder(USER_ID, LINES, new BigDecimal("567.00"));
         assertEquals(LINES, requestOrder.getOrderLines().length);
 
         Integer invoiceId = callcreateOrderAndInvoice(requestOrder);
         assertNotNull(invoiceId);
 
         OrderWS resultOrder = api.getOrder(requestOrder.getId());
         assertNotNull(resultOrder);
         assertEquals(requestOrder.getId(), resultOrder.getId());
         assertEquals(LINES, resultOrder.getOrderLines().length);
 
         HashMap<String, OrderLineWS> actualByDescription = new HashMap<String, OrderLineWS>();
         for (OrderLineWS next : resultOrder.getOrderLines()){
             assertNotNull(next.getId());
             assertNotNull(next.getDescription());
             actualByDescription.put(next.getDescription(), next);
         }
 
         for (int i = 0; i < LINES; i++){
             OrderLineWS nextRequested = requestOrder.getOrderLines()[i];
             OrderLineWS nextActual = actualByDescription.remove(nextRequested.getDescription());
             assertNotNull(nextActual);
 
             assertEquals(nextRequested.getDescription(), nextActual.getDescription());
             assertEquals(nextRequested.getAmountAsDecimal(), nextActual.getAmountAsDecimal());
             assertEquals(nextRequested.getQuantityAsDecimal(), nextActual.getQuantityAsDecimal());
             assertEquals(nextRequested.getQuantityAsDecimal(), nextActual.getQuantityAsDecimal());
         }
 
         // cleanup
         api.deleteInvoice(invoiceId);
         api.deleteOrder(requestOrder.getId());
     }
 
     public void testAutoCreatedInvoiceIsCorrect() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         final int USER_ID = GANDALF_USER_ID;
         final int LINES = 2;
 
         // it is critical to make sure that this invoice can not be composed by
         // previous payments
         // so, make the price unusual
         final BigDecimal PRICE = new BigDecimal("687654.29");
 
         OrderWS orderWS = createMockOrder(USER_ID, LINES, PRICE);
         Integer invoiceId = callcreateOrderAndInvoice(orderWS);
         InvoiceWS invoice = callGetLatestInvoice(USER_ID);
         assertNotNull(invoice.getOrders());
         assertTrue("Expected: " + orderWS.getId() + ", actual: " + Arrays.toString(invoice.getOrders()), Arrays.equals(new Integer[] {orderWS.getId()}, invoice.getOrders()));
 
         assertNotNull(invoice.getInvoiceLines());
         assertEquals(LINES, invoice.getInvoiceLines().length);
 
         assertEmptyArray(invoice.getPayments());
         assertEquals(Integer.valueOf(0), invoice.getPaymentAttempts());
 
         assertNotNull(invoice.getBalance());
         assertEquals(PRICE.multiply(new BigDecimal(LINES)), invoice.getBalanceAsDecimal());
 
         // cleanup
         api.deleteInvoice(invoiceId);
         api.deleteOrder(orderWS.getId());
     }
 
     public void testAutoCreatedInvoiceIsPayable() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         final int USER_ID = GANDALF_USER_ID;
 
         OrderWS order = createMockOrder(USER_ID, 1, new BigDecimal("789.00"));
         Integer invoiceId = callcreateOrderAndInvoice(order);
 
         InvoiceWS invoice = callGetLatestInvoice(USER_ID);
         assertNotNull(invoice);
         assertNotNull(invoice.getId());
         assertEquals("new invoice is not paid", 1, invoice.getToProcess().intValue());
         assertTrue("new invoice with a balance", BigDecimal.ZERO.compareTo(invoice.getBalanceAsDecimal()) < 0);
 
         PaymentAuthorizationDTOEx auth = api.payInvoice(invoice.getId());
         assertNotNull(auth);
         assertEquals("Payment result OK", true, auth.getResult().booleanValue());
         assertEquals("Processor code", "The transaction has been approved", auth.getResponseMessage());
 
         // payment date should not be null (bug fix)
         assertNotNull("Payment date not null", api.getLatestPayment(USER_ID).getPaymentDate());
 
         // now the invoice should be shown as paid
         invoice = callGetLatestInvoice(USER_ID);
         assertNotNull(invoice);
         assertNotNull(invoice.getId());
         assertEquals("new invoice is now paid", 0, invoice.getToProcess().intValue());
         assertTrue("new invoice without a balance", BigDecimal.ZERO.compareTo(invoice.getBalanceAsDecimal()) == 0);
 
         // cleanup
         api.deleteInvoice(invoiceId);
         api.deleteOrder(order.getId());
     }
 
     public void testUpdateLines() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         Integer orderId = new Integer(15);
         OrderWS order = api.getOrder(orderId);
 
         int initialCount = order.getOrderLines().length;
         System.out.println("Got order with " + initialCount + " lines");
 
         // let's add a line
         OrderLineWS line = new OrderLineWS();
         line.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
         line.setQuantity(new Integer(1));
         line.setItemId(new Integer(14));
         line.setUseItem(new Boolean(true));
 
         ArrayList<OrderLineWS> lines = new ArrayList<OrderLineWS>();
         Collections.addAll(lines, order.getOrderLines());
         lines.add(line);
         order.setOrderLines(lines.toArray(new OrderLineWS[lines.size()]));
 
         // call the update
         System.out.println("Adding one order line: " + order);
         api.updateOrder(order);
 
         // let's see if my new line is there
         order = api.getOrder(orderId);
         System.out.println("Got updated order with " + order.getOrderLines().length + " lines");
         assertEquals("One more line should be there", initialCount + 1, order.getOrderLines().length);
 
         // and again
         initialCount = order.getOrderLines().length;
         line.setItemId(1); // to add another line, you need a different item
 
         lines.clear();
         Collections.addAll(lines, order.getOrderLines());
         lines.add(line);
         order.setOrderLines(lines.toArray(new OrderLineWS[lines.size()]));
 
         System.out.println("lines now " + order.getOrderLines().length);
 
         // call the update
         System.out.println("Adding another order line");
         api.updateOrder(order);
 
         // let's see if my new line is there
         order = api.getOrder(orderId);
         System.out.println("Got updated order with " + order.getOrderLines().length + " lines");
         assertEquals("One more line should be there", initialCount + 1, order.getOrderLines().length);
     }
 
     public void testRecreate() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         // the the latest
         OrderWS order = api.getLatestOrder(GANDALF_USER_ID);
 
         // use it to create another one
         Integer newOrder = api.createOrder(order);
         assertTrue("New order newer than original", order.getId().compareTo(newOrder) < 0);
 
         // clean up
         api.deleteOrder(newOrder);
     }
 
     public void testRefundAndCancelFee() throws Exception {
         final Integer USER_ID = 1000;
 
         // create an order an order for testing
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         OrderWS newOrder = new OrderWS();
         newOrder.setUserId(USER_ID);
         newOrder.setBillingTypeId(Constants.ORDER_BILLING_PRE_PAID);
         newOrder.setPeriod(2);
         newOrder.setCurrencyId(new Integer(1));
         newOrder.setActiveSince(new Date());
 
         // now add some lines
         OrderLineWS lines[] = new OrderLineWS[2];
         OrderLineWS line;
 
         // 5 lemonades - 1 per day monthly pass
         line = new OrderLineWS();
         line.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
         line.setQuantity(new Integer(5));
         line.setItemId(new Integer(1));
         line.setUseItem(new Boolean(true));
         lines[0] = line;
 
         // 5 coffees
         line = new OrderLineWS();
         line.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
         line.setQuantity(new Integer(5));
         line.setItemId(new Integer(3));
         line.setUseItem(new Boolean(true));
         lines[1] = line;
 
         newOrder.setOrderLines(lines);
 
         // create the first order and invoice it
         System.out.println("Creating order ...");
         Integer orderId = api.createOrderAndInvoice(newOrder);
         assertNotNull("The order was not created", orderId);
 
         // update the quantities of the order (-2 lemonades, -3 coffees)
         System.out.println("Updating quantities of order ...");
         OrderWS order = api.getLatestOrder(USER_ID);
         assertEquals("No. of order lines", 2, order.getOrderLines().length);
         OrderLineWS orderLine = order.getOrderLines()[0];
         orderLine.setQuantity(3);
         orderLine = order.getOrderLines()[1];
         orderLine.setQuantity(2);
         api.updateOrder(order);
 
         // get last 3 orders and check what's on them (2 refunds and a fee)
         System.out.println("Getting last 3 orders ...");
         Integer[] list = api.getLastOrders(new Integer(USER_ID), new Integer(3));
         assertNotNull("Missing list", list);
 
         // order 1 - coffee refund
         order = api.getOrder(list[0]);
         assertEquals("No. of order lines", 1, order.getOrderLines().length);
         orderLine = order.getOrderLines()[0];
         assertEquals("Item Id", new Integer(3), orderLine.getItemId());
         assertEquals("Quantity", new BigDecimal("-3.00"), orderLine.getQuantityAsDecimal());
         assertEquals("Price", new BigDecimal("15.00"), orderLine.getPriceAsDecimal());
         assertEquals("Amount", new BigDecimal("-45.00"), orderLine.getAmountAsDecimal());
 
         // order 3 - cancel fee for lemonade (see the rule in CancelFees.drl)
         order = api.getOrder(list[1]);
         assertEquals("No. of order lines", 1, order.getOrderLines().length);
         orderLine = order.getOrderLines()[0];
         assertEquals("Item Id", new Integer(24), orderLine.getItemId());
         assertEquals("Quantity", new BigDecimal("2.00"), orderLine.getQuantityAsDecimal());
         assertEquals("Price", new BigDecimal("5.00"), orderLine.getPriceAsDecimal());
         assertEquals("Amount", new BigDecimal("10.00"), orderLine.getAmountAsDecimal());
 
         // order 2 - lemonade refund
         order = api.getOrder(list[2]);
         assertEquals("No. of order lines", 1, order.getOrderLines().length);
         orderLine = order.getOrderLines()[0];
         assertEquals("Item Id", new Integer(1), orderLine.getItemId());
         assertEquals("Quantity", new BigDecimal("-2.00"), orderLine.getQuantityAsDecimal());
         assertEquals("Price", new BigDecimal("10.00"), orderLine.getPriceAsDecimal());
         assertEquals("Amount", new BigDecimal("-20.00"), orderLine.getAmountAsDecimal());
 
         // create a new order like the first one
         System.out.println("Creating order ...");
         // to test period calculation of fees in CancellationFeeRulesTask
         newOrder.setActiveUntil(weeksFromToday(12));
         orderId = api.createOrderAndInvoice(newOrder);
         assertNotNull("The order was not created", orderId);
 
         // set active until earlier than invoice date
         order = api.getLatestOrder(USER_ID);
         order.setActiveUntil(weeksFromToday(2));
         api.updateOrder(order);
 
         // get last 2 orders and check what's on them (a full refund and a fee)
         System.out.println("Getting last 2 orders ...");
         list = api.getLastOrders(new Integer(USER_ID), new Integer(3));
         assertNotNull("Missing list", list);
 
         // order 1 - full refund
         order = api.getOrder(list[0]);
         assertEquals("No. of order lines", 2, order.getOrderLines().length);
         orderLine = order.getOrderLines()[0];
         assertEquals("Item Id", new Integer(1), orderLine.getItemId());
         assertEquals("Quantity", new BigDecimal("-5.00"), orderLine.getQuantityAsDecimal());
         assertEquals("Price", new BigDecimal("10.00"), orderLine.getPriceAsDecimal());
         assertEquals("Amount", new BigDecimal("-50.00"), orderLine.getAmountAsDecimal());
         orderLine = order.getOrderLines()[1];
         assertEquals("Item Id", new Integer(3), orderLine.getItemId());
         assertEquals("Quantity", new BigDecimal("-5.00"), orderLine.getQuantityAsDecimal());
         assertEquals("Price", new BigDecimal("15.00"), orderLine.getPriceAsDecimal());
         assertEquals("Amount", new BigDecimal("-75.00"), orderLine.getAmountAsDecimal());
 
         // order 2 - cancel fee for lemonades (see the rule in CancelFees.drl)
         order = api.getOrder(list[1]);
         assertEquals("No. of order lines", 1, order.getOrderLines().length);
         orderLine = order.getOrderLines()[0];
         assertEquals("Item Id", new Integer(24), orderLine.getItemId());
         // 2 periods cancelled (2 periods * 5 fee quantity)
         assertEquals("Quantity", new BigDecimal("10.00"), orderLine.getQuantityAsDecimal());
         assertEquals("Price", new BigDecimal("5.00"), orderLine.getPriceAsDecimal());
         assertEquals("Amount", new BigDecimal("50.00"), orderLine.getAmountAsDecimal());
 
         // remove invoices
         list = api.getLastInvoices(new Integer(USER_ID), new Integer(2));
         api.deleteInvoice(list[0]);
         api.deleteInvoice(list[1]);
         // remove orders
         list = api.getLastOrders(new Integer(USER_ID), new Integer(7));
         for (int i = 0; i < list.length; i++) {
             api.deleteOrder(list[i]);
         }
     }
 
     public void testDefaultCycleStart() throws Exception {
         final Integer USER_ID = 1000;
 
         // create an order for testing
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         // create a main subscription (current) order
         OrderWS mainOrder = createMockOrder(USER_ID, 1, new BigDecimal("10.00"));
         mainOrder.setPeriod(2);
         mainOrder.setIsCurrent(1);
         mainOrder.setCycleStarts(new Date());
         System.out.println("Creating main subscription order ...");
         Integer mainOrderId = api.createOrder(mainOrder);
         assertNotNull("The order was not created", mainOrderId);
 
         // create another order and see if cycle starts was set
         OrderWS testOrder = createMockOrder(USER_ID, 1, new BigDecimal("20.00"));
         testOrder.setPeriod(2);
         System.out.println("Creating test order ...");
         Integer testOrderId = api.createOrder(testOrder);
         assertNotNull("The order was not created", testOrderId);
 
         // check cycle starts dates are the same
         mainOrder = api.getOrder(mainOrderId);
         testOrder = api.getOrder(testOrderId);
         assertEquals("Cycle starts", mainOrder.getCycleStarts(),
                      testOrder.getCycleStarts());
 
         // create another order with cycle starts set to check it isn't
         // overwritten
         api.deleteOrder(testOrderId);
         testOrder = createMockOrder(USER_ID, 1, new BigDecimal("30.00"));
         testOrder.setPeriod(2);
         testOrder.setCycleStarts(weeksFromToday(1));
         System.out.println("Creating test order ...");
         testOrderId = api.createOrder(testOrder);
         assertNotNull("The order was not created", testOrderId);
 
         // check cycle starts dates aren't the same
         testOrder = api.getOrder(testOrderId);
         assertFalse("Cycle starts", mainOrder.getCycleStarts().equals(
                 testOrder.getCycleStarts()));
 
         // create another order with isCurrent not null
         api.deleteOrder(testOrderId);
         testOrder = createMockOrder(USER_ID, 1, new BigDecimal("40.00"));
         testOrder.setPeriod(2);
         testOrder.setIsCurrent(0);
         System.out.println("Creating test order ...");
         testOrderId = api.createOrder(testOrder);
         assertNotNull("The order was not created", testOrderId);
 
         // check that cycle starts wasn't set (is null)
         testOrder = api.getOrder(testOrderId);
         assertNull("Cycle starts", testOrder.getCycleStarts());
 
         // remove orders
         api.deleteOrder(mainOrderId);
         api.deleteOrder(testOrderId);
     }
 
     public void testPlan() throws Exception {
         final Integer USER_ID = 1000;
 
         // create an order for testing
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         // create an order with the plan item
         OrderWS mainOrder = createMockOrder(USER_ID, 1, new BigDecimal("10.00"));
         mainOrder.setPeriod(2);
         mainOrder.getOrderLines()[0].setItemId(250);
         mainOrder.getOrderLines()[0].setUseItem(true);
         System.out.println("Creating plan order ...");
         Integer mainOrderId = api.createOrder(mainOrder);
         assertNotNull("The order was not created", mainOrderId);
 
         // take the last two orders
         Integer orders[] = api.getLastOrders(USER_ID, 2);
         // setup
         OrderWS order = api.getOrder(orders[1]);
         assertEquals("Setup fee order with one item", 1, order.getOrderLines().length);
         assertEquals("Setup fee with item 251", 251, order.getOrderLines()[0].getItemId().intValue());
         assertEquals("Setup fee order one-ime", 1, order.getPeriod().intValue());
 
         // subscription
         order = api.getOrder(orders[0]);
         assertEquals("subscription order with one item", 1, order.getOrderLines().length);
         assertEquals("subscription with item 1", 1, order.getOrderLines()[0].getItemId().intValue());
         assertEquals("subscription order monthly", 2, order.getPeriod().intValue());
 
         // clean up
         api.deleteOrder(orders[0]);
         api.deleteOrder(orders[1]);
     }
 
     // Tests InternalEventsRulesTask plug-in.
     // See also InternalEventsRulesTask520.drl.
     public void testInternalEventsRulesTask() throws Exception {
         final Integer USER_ID = 1010;
 
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         // create order with 2 lines (item ids 1 & 2) and invoice
         OrderWS order = createMockOrder(USER_ID, 2, new BigDecimal("5.00"));
         order.setNotes("To be changed");
         Integer invoiceId = api.createOrderAndInvoice(order);
 
         // get back created order
         InvoiceWS invoice = api.getInvoiceWS(invoiceId);
         Integer orderId = invoice.getOrders()[0];
         System.out.println("Invoice generated " + invoice.getId() + " from order " + orderId);
         order = api.getOrder(orderId);
 
         // check order was modified
         assertEquals("Order was changed by rules", "Modified by rules.", order.getNotes());
         OrderLineWS[] orderLines = order.getOrderLines();
         assertEquals("Only 1 order line", 1, orderLines.length);
         assertEquals("Item id 1 was removed", new Integer(2), orderLines[0].getItemId());
 
         // double check the invoice lines
         InvoiceLineDTO[] invoiceLines = invoice.getInvoiceLines();
         assertEquals("Only 1 invoice line", 1, invoiceLines.length);
         assertEquals("Item id 1 was removed", new Integer(2), invoiceLines[0].getItemId());
 
         // clean up
         api.deleteInvoice(invoiceId);
         api.deleteOrder(orderId);
     }
 
     public void testCurrentOrder() throws Exception {
         final Integer USER_ID = GANDALF_USER_ID;
         final Integer NO_MAIN_SUB_USER_ID = 1010;
 
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
 
         //
         // Test update current order without pricing fields.
         //
 
         // current order before modification
         OrderWS currentOrderBefore = api.getCurrentOrder(USER_ID, new Date());
 
         // CXF returns null for empty arrays
         if (currentOrderBefore.getOrderLines() != null) {
             assertEquals("No order lines.", 0, currentOrderBefore.getOrderLines().length);
         }
 
         // add a single line
         OrderLineWS newLine = new OrderLineWS();
         newLine.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
         newLine.setItemId(new Integer(1));
         newLine.setQuantity(new BigDecimal("22.00"));
         newLine.setUseItem(new Boolean(true)); // use the price and description from the item
 
         // update the current order
         OrderWS currentOrderAfter = api.updateCurrentOrder(USER_ID,
                                                            new OrderLineWS[] { newLine }, // adding a new order line
                                                            null,
                                                            new Date(),
                                                            "Event from WS");
 
         // asserts
         assertEquals("Order ids", currentOrderBefore.getId(), currentOrderAfter.getId());
         assertEquals("1 new order line", 1, currentOrderAfter.getOrderLines().length);
 
         OrderLineWS createdLine = currentOrderAfter.getOrderLines()[0];
         assertEquals("Order line item ids", newLine.getItemId(),  createdLine.getItemId());
         assertEquals("Order line quantities", newLine.getQuantityAsDecimal(), createdLine.getQuantityAsDecimal());
         assertEquals("Order line price", new BigDecimal("10.00"), createdLine.getPriceAsDecimal());
         assertEquals("Order line total", new BigDecimal("220.00"), createdLine.getAmountAsDecimal());
 
 
         //
         // Test update current order with pricing fields.
         //
 
         // A pricing rule. See PricingRules.drl, rule 'PricingField test1'.
         PricingField pf = new PricingField("newPrice", new BigDecimal("5.0"));
         newLine.setQuantity(1);
         currentOrderAfter = api.updateCurrentOrder(USER_ID,
                                                    new OrderLineWS[] { newLine }, // adding a new order line
                                                    new PricingField[] { pf },
                                                    new Date(),
                                                    "Event from WS");
 
         // asserts
         assertEquals("1 order line", 1, currentOrderAfter.getOrderLines().length);
         createdLine = currentOrderAfter.getOrderLines()[0];
         assertEquals("Order line ids", newLine.getItemId(), createdLine.getItemId());
         assertEquals("Order line quantities", new BigDecimal("23.00"), createdLine.getQuantityAsDecimal());
         assertEquals("Order line price", new BigDecimal("10.00"), createdLine.getPriceAsDecimal());
 
         // Note that because of the rule, the result should be 225.0, not 230.0.
         assertEquals("Order line total", new BigDecimal("225.00"), createdLine.getAmountAsDecimal());
 
 
         //
         // Test update current order with pricing fields and no
         // order lines. RulesMediationTask should create them.
         //
 
         // Call info pricing fields. See Mediation.drl, rule 'line creation'
         PricingField duration = new PricingField("duration", 5); // 5 min
         PricingField dst = new PricingField("dst", "12345678");
         currentOrderAfter = api.updateCurrentOrder(USER_ID,
                                                    null,
                                                    new PricingField[] { pf, duration, dst },
                                                    new Date(),
                                                    "Event from WS");
 
         // asserts
         assertEquals("2 order line", 2, currentOrderAfter.getOrderLines().length);
 
         createdLine = currentOrderAfter.getOrderLines()[0];
         assertEquals("Order line ids", newLine.getItemId(), createdLine.getItemId());
         assertEquals("Order line quantities", new BigDecimal("23.00"), createdLine.getQuantityAsDecimal());
         assertEquals("Order line price", new BigDecimal("10.00"), createdLine.getPriceAsDecimal());
         assertEquals("Order line total", new BigDecimal("225.00"), createdLine.getAmountAsDecimal());
 
         // 'newPrice' pricing field, $5 * 5 units = 25
         createdLine = currentOrderAfter.getOrderLines()[1];
         assertEquals("Order line quantities", new BigDecimal("5.00"), createdLine.getQuantityAsDecimal());
         assertEquals("Order line price", new BigDecimal("5.00"), createdLine.getPriceAsDecimal());
         assertEquals("Order line price", new BigDecimal("25"), createdLine.getAmountAsDecimal()); // not priced
 
         //
         // Events that go into an order already invoiced, should update the
         // current order for the next cycle
         //
 
         // fool the system making the current order finished (don't do this at home)
         System.out.println("Making current order 'finished'");
         currentOrderAfter.setStatusId(2); // this means finished
         api.updateOrder(currentOrderAfter);
         assertEquals("now current order has to be finished", 2, api.getOrder(currentOrderAfter.getId()).getStatusId().intValue());
 
         // make that current order an invoice
         /*
         Integer invoiceId = api.createInvoice(USER_ID, false)[0];
         System.out.println("current order generated invoice " + invoiceId);
         */
 
         // now send again that last event
         System.out.println("Sending event again");
         OrderWS currentOrderNext = api.updateCurrentOrder(USER_ID,
                                                           null,
                                                           new PricingField[] { pf, duration, dst },
                                                           new Date(),
                                                           "Same event from WS");
 
         assertNotNull("Current order for next cycle should be provided", currentOrderNext);
 
         assertFalse("Current order for next cycle can't be the same as the previous one",
                     currentOrderNext.getId().equals(currentOrderAfter.getId()));
 
         assertEquals("Active since of new order should be one mothe later than previous one for the same event",
                      new DateMidnight(currentOrderAfter.getActiveSince().getTime()).plusMonths(1),
                      new DateMidnight(currentOrderNext.getActiveSince().getTime()));
 
         //
         // No main subscription order tests.
         //
 
         // User with no main subscription order should return
         // null when trying to get a current order.
         assertNull("User with no main subscription order should have null current order",
                    api.getCurrentOrder(NO_MAIN_SUB_USER_ID, new Date()));
 
         // An exception should be thrown
         try {
             api.updateCurrentOrder(NO_MAIN_SUB_USER_ID,
                                    new OrderLineWS[] { newLine },
                                    null,
                                    new Date(),
                                    "Event from WS");
 
             fail("User with no main subscription order should throw an exception");
         } catch(Exception e) { }
 
 
         //
         // Security tests
         //
 
         try {
             api.getCurrentOrder(13, new Date()); // returns null, not a real test
             fail("User 13 belongs to entity 2");
         } catch (Exception e) { }
 
         try {
             api.updateCurrentOrder(13,
                                    new OrderLineWS[] { newLine },
                                    new PricingField[] { pf },
                                    new Date(),
                                    "Event from WS");
 
             fail("User 13 belongs to entity 2");
         } catch (Exception e) { }
 
         // cleanup
 //        api.deleteInvoice(invoiceId);
         api.deleteOrder(currentOrderAfter.getId());
         api.deleteOrder(currentOrderNext.getId());
     }
 
     public void testIsUserSubscribedTo() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         // Test a non-existing user first, result should be 0
         String result = api.isUserSubscribedTo(999, 999);
         assertEquals(BigDecimal.ZERO, new BigDecimal(result));
 
         // Test the result given by a known existing user (in PostgreSQL test db)
         result = api.isUserSubscribedTo(2, 2);
         assertEquals(new BigDecimal("1"), new BigDecimal(result));
 
         // Test another user
         result = api.isUserSubscribedTo(73, 1);
         assertEquals(new BigDecimal("89"), new BigDecimal(result));
     }
 
     public void testGetUserItemsByCategory() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         // Test a non-existing user first, result should be 0
         Integer[] result = api.getUserItemsByCategory(Integer.valueOf(999), Integer.valueOf(999));
         assertNull(result);
 
         // Test the result given by a known existing user
         // (it has items 2 and 3 on category 1 in PostgreSQL test db)
         result = api.getUserItemsByCategory(Integer.valueOf(2), Integer.valueOf(1));
         Arrays.sort(result);
         assertEquals(2, result.length);
         assertEquals(Integer.valueOf(2), result[0]);
         assertEquals(Integer.valueOf(3), result[1]);
 
         // Test another user (has items 1 and 2 on cat. 1)
         result = api.getUserItemsByCategory(Integer.valueOf(73), Integer.valueOf(1));
         assertEquals(2, result.length);
         assertEquals(Integer.valueOf(1), result[0]);
         assertEquals(Integer.valueOf(2), result[1]);
     }
 
     public void testMainOrder() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         // note: for some reason, calling api.getUsersByCreditCard("1152") returns three users
         // but after calling updateUser, it reutrns 4 because Gandalf is included.
         // why is not picking him up before? What is updateUser doing that then the CC shows up?
         // get gandalf's orders
         Integer orders[] = api.getLastOrders(GANDALF_USER_ID, 100);
 
         // now get the user
         UserWS user = api.getUserWS(GANDALF_USER_ID);
         Integer mainOrder = user.getMainOrderId();
         System.out.println("Gandalf's main order = " + mainOrder);
 
         user.setMainOrderId(orders[orders.length - 1]);
         System.out.println("Gandalf's new main order = " + user.getMainOrderId());
 
         // update the user (so new main order)
         user.setPassword(null);
         api.updateUser(user);
 
         // validate that the user does have the new main order
         assertEquals("User does not have the correct main order",
                      orders[orders.length - 1],
                      api.getUserWS(GANDALF_USER_ID).getMainOrderId());
 
         // set to null: does not work
         /*
         user.setMainOrderId(null);
         api.updateUser(user);
         // validate that the user does have the new main order
         assertNull("User does should not have main order after setting to null",
                 api.getUserWS(GANDALF_USER_ID).getMainOrderId());
         */
 
         // update the user (restore main order)
         user.setMainOrderId(mainOrder);
         api.updateUser(user);
         assertEquals("User does not have the original main order",
                      mainOrder,
                      api.getUserWS(GANDALF_USER_ID).getMainOrderId());
     }
 
     public void testOrderLineDescriptionLanguage() throws Exception {
         final Integer USER_ID = 10750; // french speaker
 
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         // create order
         OrderWS order = new OrderWS();
         order.setUserId(USER_ID);
         order.setBillingTypeId(Constants.ORDER_BILLING_PRE_PAID);
         order.setPeriod(1); // once
         order.setCurrencyId(1);
         order.setActiveSince(new Date());
 
         OrderLineWS line = new OrderLineWS();
         line.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
         line.setItemId(1);
         line.setQuantity(1);
         line.setUseItem(true);
 
         order.setOrderLines(new OrderLineWS[] { line } );
 
         // create order and invoice
         Integer invoiceId = api.createOrderAndInvoice(order);
 
         // check invoice line
         InvoiceWS invoice = api.getInvoiceWS(invoiceId);
         assertEquals("Number of invoice lines", 1,
                      invoice.getInvoiceLines().length);
 
         InvoiceLineDTO invoiceLine = invoice.getInvoiceLines()[0];
         assertEquals("French description",
                      "French Lemonade",
                      invoiceLine.getDescription());
 
         // clean up
         api.deleteInvoice(invoiceId);
         api.deleteOrder(invoice.getOrders()[0]);
     }
 
     public void testItemSwappingRules() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         // add items to a user subscribed to 1
         System.out.println("Testing item swapping - included in plan");
         OrderWS order = createMockOrder(1070, 1, new BigDecimal("1.00"));
         order.getOrderLines()[0].setItemId(2600); // the generic lemonade
         order.getOrderLines()[0].setUseItem(true);
 
         int orderId = api.createOrder(order);
         order = api.getOrder(orderId);
         assertEquals("Order should have one line", 1, order.getOrderLines().length);
         assertEquals("Order should have the included in plan line",
                      2601,
                      order.getOrderLines()[0].getItemId().intValue());
 
         // cleanup
         api.deleteOrder(orderId);
 
         // now a guy without the plan (user 33)
         System.out.println("Testing item swapping - NOT included in plan");
         order = createMockOrder(33, 1, new BigDecimal("1.00"));
         order.getOrderLines()[0].setItemId(2600); // the generic lemonade
         order.getOrderLines()[0].setUseItem(true);
 
         orderId = api.createOrder(order);
         order = api.getOrder(orderId);
         assertEquals("Order should have one line", 1, order.getOrderLines().length);
         assertEquals("Order should have the priced item line",
                      2602,
                      order.getOrderLines()[0].getItemId().intValue());
 
         // cleanup
         api.deleteOrder(orderId);
     }
 
     public void testRateCard() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         System.out.println("Testing Rate Card");
 
         // user for tests
         UserWS user = com.sapienter.jbilling.server.user.WSTest.createUser(true, null, null);
         Integer userId = user.getUserId();
 
         // create main subscription order
         Integer mainOrderId = com.sapienter.jbilling.server.user.WSTest.createMainSubscriptionOrder(userId, 1);
 
         // update to credit limit
         user.setBalanceType(Constants.BALANCE_CREDIT_LIMIT);
         user.setCreditLimit(new BigDecimal("100.0"));
         api.updateUser(user);
 
 
         /*
             updateCurrentOrder
         */
 
         // should be priced at 0.33 (see row 548)
         PricingField[] pf = {
                 new PricingField("dst", "55999"),
                 new PricingField("duration", 1)
         };
 
         OrderWS currentOrder = api.updateCurrentOrder(userId,null, pf, new Date(), "Event from WS");
 
         assertEquals("1 order line", 1, currentOrder.getOrderLines().length);
         OrderLineWS line = currentOrder.getOrderLines()[0];
         assertEquals("order line itemId", 2800, line.getItemId().intValue());
         assertEquals("order line quantity", new BigDecimal("1.00"), line.getQuantityAsDecimal());
         assertEquals("order line total", new BigDecimal("0.33"), line.getAmountAsDecimal());
 
         // check dynamic balance
         user = api.getUserWS(userId);
         assertEquals("dynamic balance", new BigDecimal("0.33"), user.getDynamicBalanceAsDecimal());
 
         // should be priced at 0.08 (see row 1753)
         pf[0].setStrValue("55000");
         currentOrder = api.updateCurrentOrder(userId,null, pf, new Date(), "Event from WS");
 
         assertEquals("1 order line", 1, currentOrder.getOrderLines().length);
         line = currentOrder.getOrderLines()[0];
         assertEquals("order line itemId", 2800, line.getItemId().intValue());
         assertEquals("order line quantity", new BigDecimal("2.00"), line.getQuantityAsDecimal());
 
         // 0.33 + 0.08 = 0.41
         assertEquals("order line total", new BigDecimal("0.41"), line.getAmountAsDecimal());
 
         // check dynamic balance
         user = api.getUserWS(userId);
         assertEquals("dynamic balance", new BigDecimal("0.41"), user.getDynamicBalanceAsDecimal());
 
 
         /*
             getItem
         */
 
         // should be priced at 0.42 (see row 1731)
         pf[0].setStrValue("212222");
         ItemDTOEx item = api.getItem(2800, userId, pf);
         assertEquals("price", new BigDecimal("0.42"), item.getPriceAsDecimal());
 
 
         /*
             rateOrder
         */
 
         OrderWS newOrder = createMockOrder(userId, 0, new BigDecimal("10.0"));
 
         // createMockOrder(...) doesn't add the line items we need for this test - do it by hand
         OrderLineWS newLine = new OrderLineWS();
         newLine.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
         newLine.setDescription("New Order Line");
         newLine.setItemId(2800);
         newLine.setQuantity(10);
         newLine.setPrice((String) null);
         newLine.setAmount((String) null);
         newLine.setUseItem(true);
 
         List<OrderLineWS> lines = new ArrayList<OrderLineWS>();
         lines.add(newLine);
 
         newOrder.setOrderLines(lines.toArray(new OrderLineWS[lines.size()]));
         newOrder.setPricingFields(PricingField.setPricingFieldsValue(pf));
 
         OrderWS order = api.rateOrder(newOrder);
         assertEquals("1 order line", 1, currentOrder.getOrderLines().length);
         line = order.getOrderLines()[0];
         assertEquals("order line itemId", 2800, line.getItemId().intValue());
         assertEquals("order line quantity", new BigDecimal("10.00"), line.getQuantityAsDecimal());
 
         // 0.42 * 10 = 4.2
         assertEquals("order line total", new BigDecimal("4.2"), line.getAmountAsDecimal());
 
 
         /*
              validatePurchase
         */
 
         // should be priced at 0.47 (see row 498)
         pf[0].setStrValue("187630");
 
         // current balance: 100 - 0.41 = 99.59
         // quantity available expected: 99.59 / 0.47
         ValidatePurchaseWS result = api.validatePurchase(userId, null, pf);
         assertEquals("validate purchase success", Boolean.valueOf(true), result.getSuccess());
         assertEquals("validate purchase authorized", Boolean.valueOf(true), result.getAuthorized());
         assertEquals("validate purchase quantity", new BigDecimal("211.89"), result.getQuantityAsDecimal());
 
         // check current order wasn't updated
         currentOrder = api.getOrder(currentOrder.getId());
         assertEquals("1 order line", 1, currentOrder.getOrderLines().length);
         line = currentOrder.getOrderLines()[0];
         assertEquals("order line itemId", 2800, line.getItemId().intValue());
         assertEquals("order line quantity", new BigDecimal("2.00"), line.getQuantityAsDecimal());
         assertEquals("order line total", new BigDecimal("0.41"), line.getAmountAsDecimal());
 
         // clean up
         api.deleteUser(userId);
     }
 
     // Tests VelocityRulesGeneratorTask plug-in.
     // See also rules-generator-template-integration-test.vm and
     // rules-generator-config.xml. Overwrites 
     // InternalEventsRulesTask520.pkg.
     public void testVelocityRulesGeneratorTaskTest() throws Exception{
         final Integer USER_ID = 1010;
 
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         // updates rules
         String xml =
                 "<bundles> " +
                 "<bundle> " +
                 "<original-product> " +
                 "<name>DR-01</name> " +
                 "</original-product> " +
                 "<replacement-product> " +
                 "<name>DR--02</name> " +
                 "</replacement-product> " +
                 "<replacement-product> " +
                 "<name>DR-03</name> " +
                 "</replacement-product> " +
                 "</bundle> " +
                 "<bundle> " +
                 "<original-product> " +
                 "<name>CALL-LD-GEN</name> " +
                 "</original-product> " +
                 "<replacement-product> " +
                 "<name>CALL-LD</name> " +
                 "</replacement-product> " +
                 "<replacement-product> " +
                 "<name>CALL-LD-INCLUDE</name> " +
                 "</replacement-product> " +
                 "</bundle> " +
                 "</bundles>";
 
         api.generateRules(xml);
 
         // wait for packages to be rescanned
         pause(10000); // DROOLS rescans every 5 seconds, give it 10 to find the change and load it
 
         // create order with 1 line and invoice
         OrderWS order = createMockOrder(USER_ID, 1, new BigDecimal("5.00"));
         order.setNotes("Change me.");
 
         // set order line item to 'DR-01' (itemId:2600)
         order.getOrderLines()[0].setItemId(2600);
         Integer invoiceId = api.createOrderAndInvoice(order);
 
         // get back created order
         InvoiceWS invoice = api.getInvoiceWS(invoiceId);
         Integer orderId = invoice.getOrders()[0];
         order = api.getOrder(orderId);
 
         // check order was modified
         assertEquals("Order was changed by generated rules",
                      "Modified by rules created by generateRules API method.",
                      order.getNotes());
 
         // Note: Bundling rules don't seem to be working for the
         // internal event rules plug-in setup.
         /*
         OrderLineWS[] orderLines = order.getOrderLines();
         assertEquals("2 order lines", 2, orderLines.length);
 
         // check the item's internalNumbers
         Arrays.sort(orderLines, new Comparator<OrderLineWS>() {
             public int compare(OrderLineWS line1, OrderLineWS line2) {
                 return line1.getId() - line2.getId();
             }
         });
 
         assertEquals("DR--02 line created", "DR--02", orderLines[0].getItemDto().getNumber());
         assertEquals("DR-03 line created", "DR-03", orderLines[1].getItemDto().getNumber());
         */
 
         // clean up
         api.deleteInvoice(invoiceId);
         api.deleteOrder(orderId);
     }
 
     private void pause(long t) {
         System.out.println("pausing for " + t + " ms...");
         try {
             Thread.sleep(t);
         } catch (InterruptedException e) {
         }
     }
 
     private Date weeksFromToday(int weekNumber) {
         Calendar calendar = new GregorianCalendar();
         calendar.setTimeInMillis(System.currentTimeMillis());
         calendar.add(Calendar.WEEK_OF_YEAR, weekNumber);
         return calendar.getTime();
     }
 
     private InvoiceWS callGetLatestInvoice(int userId) throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
         return api.getLatestInvoice(userId);
     }
 
     /**
      * Creates an order and invoices it, returning the ID of the new invoice and populating
      * the given order with the ID of the new order.
      *
      * @param order order to create and set ID
      * @return invoice id
      * @throws Exception possible API exception
      */
     private Integer callcreateOrderAndInvoice(OrderWS order) throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         Integer invoiceId = api.createOrderAndInvoice(order);
         InvoiceWS invoice = api.getInvoiceWS(invoiceId);
         order.setId(invoice.getOrders()[0]);
 
         System.out.println("Created order " + order.getId() + " and invoice " + invoice.getId());
 
         return invoice.getId();
     }
 
     public static OrderWS createMockOrder(int userId, int orderLinesCount, BigDecimal linePrice) {
         OrderWS order = new OrderWS();
         order.setUserId(userId);
         order.setBillingTypeId(Constants.ORDER_BILLING_PRE_PAID);
         order.setPeriod(1); // once
         order.setCurrencyId(1);
         order.setActiveSince(new Date());
 
         ArrayList<OrderLineWS> lines = new ArrayList<OrderLineWS>(orderLinesCount);
         for (int i = 0; i < orderLinesCount; i++){
             OrderLineWS nextLine = new OrderLineWS();
             nextLine.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
             nextLine.setDescription("Order line: " + i);
             nextLine.setItemId(i + 1);
             nextLine.setQuantity(1);
             nextLine.setPrice(linePrice);
             nextLine.setAmount(nextLine.getQuantityAsDecimal().multiply(linePrice));
 
             lines.add(nextLine);
         }
         order.setOrderLines(lines.toArray(new OrderLineWS[lines.size()]));
         return order;
     }
 
     private void assertEmptyArray(Object[] array){
         if (array != null) {
             assertEquals("Empty array expected: " + Arrays.toString(array), 0, array.length);
         }
     }
 
     public static void assertEquals(BigDecimal expected, BigDecimal actual) {
         assertEquals(null, expected, actual);
     }
 
     public static void assertEquals(String message, BigDecimal expected, BigDecimal actual) {
         assertEquals(message,
                      (Object) (expected == null ? null : expected.setScale(2, RoundingMode.HALF_UP)),
                      (Object) (actual == null ? null : actual.setScale(2, RoundingMode.HALF_UP)));
     }
 }
