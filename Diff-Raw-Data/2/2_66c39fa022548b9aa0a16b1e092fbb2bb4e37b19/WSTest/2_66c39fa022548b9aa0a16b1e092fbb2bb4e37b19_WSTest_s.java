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
  * Created on Dec 18, 2003
  *
  */
 package com.sapienter.jbilling.server.order;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import com.sapienter.jbilling.server.entity.InvoiceLineDTO;
 import com.sapienter.jbilling.server.invoice.InvoiceWS;
 import com.sapienter.jbilling.server.item.ItemDTOEx;
 import com.sapienter.jbilling.server.item.PricingField;
 import com.sapienter.jbilling.server.payment.PaymentAuthorizationDTOEx;
 import com.sapienter.jbilling.server.user.UserWS;
 import com.sapienter.jbilling.server.user.ValidatePurchaseWS;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.api.JbillingAPI;
 import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;
 
 /**
  * @author Emil
  */
 public class WSTest  extends TestCase {
 
     private static final Integer GANDALF_USER_ID = 2;
       
     public void testCreateUpdateDelete() {
         try {
         	
             JbillingAPI api = JbillingAPIFactory.getAPI();
         	int i;
 
             /*
              * Create
              */
             OrderWS newOrder = new OrderWS();
             newOrder.setUserId(GANDALF_USER_ID); 
             newOrder.setBillingTypeId(Constants.ORDER_BILLING_PRE_PAID);
             newOrder.setPeriod(new Integer(1)); // once
             newOrder.setCurrencyId(new Integer(1));
             Calendar cal = Calendar.getInstance();
             cal.clear();
             cal.set(2008, 9, 3);
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
             
             System.out.println("Creating order ...");
             Integer ret = api.createOrderAndInvoice(newOrder);
             assertNotNull("The order was not created", ret);
             // create another one so we can test get by period.
             ret = api.createOrderAndInvoice(newOrder);
             System.out.println("Created invoice " + ret);
             InvoiceWS newInvoice = api.getInvoiceWS(ret);
             ret = newInvoice.getOrders()[0]; // this is the order that was also created
             
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
             System.out.println("Getting created order " + ret);
             OrderWS retOrder = api.getOrder(ret);
             //System.out.println("Got:" + retOrder);
             assertEquals("created order billing type", retOrder.getBillingTypeId(),
                     newOrder.getBillingTypeId());
             assertEquals("created order billing period", retOrder.getPeriod(),
                     newOrder.getPeriod());
             assertEquals("created order cycle starts", retOrder.getCycleStarts().getTime(),
                     newOrder.getCycleStarts().getTime());
             
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
                     assertEquals("Special price for Item 2", "30", item2line.getPrice());
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
 	                assertEquals("total of discount", "-5.5", retOrderLine.getAmount());
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
             lineId = retOrderLine.getId();
             try {
                 System.out.println("Updating bad order line");
                 retOrderLine.setId(new Integer(6));
                 api.updateOrderLine(retOrderLine);
                 fail("Order line 6 belongs to entity 301");
             } catch (Exception e) {
             }
             retOrderLine.setId(lineId);
             System.out.println("Update order line " + lineId);
             api.updateOrderLine(retOrderLine);
             retOrderLine = api.getOrderLine(retOrderLine.getId());
             assertEquals("updated quantity", "99", retOrderLine.getQuantity());
             //delete a line through updating with quantity = 0
             System.out.println("Delete order line");
             retOrderLine.setQuantity(new Integer(0));
             api.updateOrderLine(retOrderLine);
             int totalLines = retOrder.getOrderLines().length;
             pause(2000); // pause while provisioning status is being updated
             retOrder = api.getOrder(retOrder.getId());
             // the order has to have one less line now
             assertEquals("order should have one less line", totalLines, 
                     retOrder.getOrderLines().length + 1);
              
             /*
              * Update
              */
             // now update the created order
             cal.clear();
             cal.set(2003, 9, 29, 0, 0, 0);
             retOrder.setActiveSince(cal.getTime());
             retOrder.getOrderLines()[1].setDescription("Modified description");
             retOrder.getOrderLines()[1].setQuantity(new Integer(2));
             retOrder.setStatusId(new Integer(2));
             // also update the next billable day
             retOrder.setNextBillableDay(cal.getTime());
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
             retOrder = api.getOrder(ret);
             assertNotNull("Didn't get updated order", retOrder);
             assertTrue("Active since", retOrder.getActiveSince().compareTo(cal.getTime()) == 0);
             assertEquals("Status id", new Integer(2), retOrder.getStatusId());
             assertEquals("Modified line description", "Modified description",
             		retOrder.getOrderLines()[1].getDescription());
             assertEquals("Modified quantity", "2", retOrder.getOrderLines()[1].getQuantity());
             assertEquals("New billable date", cal.getTimeInMillis(), 
                     retOrder.getNextBillableDay().getTime());
             for (i = 0; i < retOrder.getOrderLines().length; i++) {
             	retOrderLine = retOrder.getOrderLines()[i];
 	            if (retOrderLine.getItemId().equals(new Integer(14))) {
 	            	// the is one less line for 15
 	            	// but one extra item for 30
 	            	// difference is 15 and 10% of that is 1.5  thus 5.5 + 1.5 = 7
 	                assertEquals("total of discount", "-7", retOrderLine.getAmount());
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
             assertEquals("Latest id", ret, lastOrder.getId());
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
             assertEquals("Latest id " + Arrays.toString(list), ret, retOrder.getId());
 
             
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
             System.out.println("Deleteing order " + ret);
             api.deleteOrder(ret);
             // try to delete from my neightbor
             try {
             	api.deleteOrder(new Integer(5));
                 fail("Order 5 belongs to entity 2");
             } catch (Exception e) {
             }
             // try to get the deleted order
             System.out.println("Getting deleted order ");
             retOrder = api.getOrder(ret);
             assertEquals("Order " + ret + " should have been deleted", 
                     1, retOrder.getDeleted());
            
             /*
              * Get by user and period
              */
             System.out.println("Getting orders by period for invalid user " + ret);
             // try to get from my neightbor
             try {
             	api.getOrderByPeriod(new Integer(13), new Integer(1));
                 fail("User 13 belongs to entity 2");
             } catch (Exception e) {
             }
             // now from a valid user
             System.out.println("Getting orders by period ");
             Integer orders[] = api.getOrderByPeriod(new Integer(2), new Integer(1));
             System.out.println("Got total orders " + orders.length +
                     " first is " + orders[0]);
             
             /*
              * Create an order with pre-authorization
              */
             System.out.println("Create an order with pre-authorization" + ret);
             PaymentAuthorizationDTOEx auth = (PaymentAuthorizationDTOEx) 
         	        api.createOrderPreAuthorize(newOrder);
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
 
 
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
     
     public void testcreateOrderAndInvoiceAutoCreatesAnInvoice() throws Exception {
     	final int USER_ID = GANDALF_USER_ID;
     	InvoiceWS before = callGetLatestInvoice(USER_ID);
     	assertTrue(before == null || before.getId() != null);
     	
     	OrderWS order = createMockOrder(USER_ID, 3, new BigDecimal("42.00"));
     	Integer invoiceId = callcreateOrderAndInvoice(order);
         assertNotNull(invoiceId);
         
         InvoiceWS afterNormalOrder = callGetLatestInvoice(USER_ID);
         assertNotNull("createOrderAndInvoice should create invoice", afterNormalOrder);
         assertNotNull("invoice without id", afterNormalOrder.getId());
         
         if (before != null){
         	assertFalse("createOrderAndInvoice should create the most recent invoice", afterNormalOrder.getId().equals(before.getId()));
         }
         
         //even if empty
     	OrderWS emptyOrder = createMockOrder(USER_ID, 0, new BigDecimal("123.00")); //empty
     	Integer emptyOrderId = callcreateOrderAndInvoice(emptyOrder);
         assertNotNull(emptyOrderId);
         
         InvoiceWS afterEmptyOrder = callGetLatestInvoice(USER_ID);
         assertNotNull("invoice without id", afterEmptyOrder.getId());
         assertNotNull("createOrderAndInvoice should create invoice even for empty order", afterEmptyOrder);
         assertFalse("createOrderAndInvoice should create the most recent invoice", afterNormalOrder.getId().equals(afterEmptyOrder.getId()));
     }
 
     public void testCreateNotActiveOrderDoesNotCreateInvoices() throws Exception {
     	final int USER_ID = GANDALF_USER_ID;
     	InvoiceWS before = callGetLatestInvoice(USER_ID);
     	
     	OrderWS orderWS = createMockOrder(USER_ID, 2, new BigDecimal("234.00"));
     	orderWS.setActiveSince(weeksFromToday(1));
         JbillingAPI api = JbillingAPIFactory.getAPI();
     	Integer orderId = api.createOrder(orderWS);
     	assertNotNull(orderId);
     	
     	InvoiceWS after = callGetLatestInvoice(USER_ID);
     	
     	if (before == null){
     		assertNull("Not yet active order -- no new invoices expected", after);
     	} else {
     		assertEquals("Not yet active order -- no new invoices expected", before.getId(), after.getId());
     	}
     }
     
     public void testCreatedOrderIsCorrect() throws Exception {
     	final int USER_ID = GANDALF_USER_ID;
     	final int LINES = 2;
     	
     	OrderWS requestOrder = createMockOrder(USER_ID, LINES, new BigDecimal("567.00"));
     	assertEquals(LINES, requestOrder.getOrderLines().length);
     	Integer orderId = callcreateOrderAndInvoice(requestOrder);
     	assertNotNull(orderId);
     	
         JbillingAPI api = JbillingAPIFactory.getAPI();
     	OrderWS resultOrder = api.getOrder(orderId);
     	assertNotNull(resultOrder);
     	assertEquals(orderId, resultOrder.getId());
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
     }
     
     public void testAutoCreatedInvoiceIsCorrect() throws Exception {
     	final int USER_ID = GANDALF_USER_ID;
     	final int LINES = 2;
 
     	// it is critical to make sure that this invoice can not be composed by
 		// previous payments
     	// so, make the price unusual
     	final BigDecimal PRICE = new BigDecimal("687654.29");  
     	
     	OrderWS orderWS = createMockOrder(USER_ID, LINES, PRICE);
     	Integer orderId = callcreateOrderAndInvoice(orderWS);
     	InvoiceWS invoice = callGetLatestInvoice(USER_ID);
     	assertNotNull(invoice.getOrders());
     	assertTrue("Expected: " + orderId + ", actual: " + Arrays.toString(invoice.getOrders()), Arrays.equals(new Integer[] {orderId}, invoice.getOrders()));
     	
     	assertNotNull(invoice.getInvoiceLines());
     	assertEquals(LINES, invoice.getInvoiceLines().length);
     	
     	assertEmptyArray(invoice.getPayments());
     	assertEquals(Integer.valueOf(0), invoice.getPaymentAttempts());
     	
     	assertNotNull(invoice.getBalance());
     	assertEquals(PRICE.multiply(new BigDecimal(LINES)), invoice.getBalanceAsDecimal());              
     }
     
     public void testAutoCreatedInvoiceIsPayable() throws Exception {
     	final int USER_ID = GANDALF_USER_ID;
     	callcreateOrderAndInvoice(createMockOrder(USER_ID, 1, new BigDecimal("789.00")));
     	InvoiceWS invoice = callGetLatestInvoice(USER_ID);
     	assertNotNull(invoice);
     	assertNotNull(invoice.getId());
         assertEquals("new invoice is not paid", 1, invoice.getToProcess().intValue());
         assertTrue("new invoice with a balance", BigDecimal.ZERO.compareTo(invoice.getBalanceAsDecimal()) < 0);
         JbillingAPI api = JbillingAPIFactory.getAPI();
     	PaymentAuthorizationDTOEx auth = api.payInvoice(invoice.getId());
     	assertNotNull(auth);
         assertEquals("Payment result OK", true, auth.getResult().booleanValue());
         assertEquals("Processor code", "The transaction has been approved", 
                 auth.getResponseMessage());
         
         // payment date should not be null (bug fix)
         assertNotNull("Payment date not null", api.getLatestPayment(USER_ID).getPaymentDate());
                 
         // now the invoice should be shown as paid
         invoice = callGetLatestInvoice(USER_ID);
         assertNotNull(invoice);
         assertNotNull(invoice.getId());
         assertEquals("new invoice is now paid", 0, invoice.getToProcess().intValue());
         assertTrue("new invoice without a balance", BigDecimal.ZERO.compareTo(invoice.getBalanceAsDecimal()) == 0);
 
     }
     
     public void testEmptyInvoiceIsNotPayable() throws Exception {
     	final int USER_ID = GANDALF_USER_ID;
     	callcreateOrderAndInvoice(createMockOrder(USER_ID, 0, new BigDecimal("890.00")));
     	InvoiceWS invoice = callGetLatestInvoice(USER_ID);
     	assertNotNull(invoice);
     	assertNotNull(invoice.getId());
         JbillingAPI api = JbillingAPIFactory.getAPI();
         PaymentAuthorizationDTOEx auth = api.payInvoice(invoice.getId());
     	assertNull(auth);
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
     
     private Integer callcreateOrderAndInvoice(OrderWS order) throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
     	InvoiceWS invoice = api.getInvoiceWS(api.createOrderAndInvoice(order));
     	return invoice.getOrders()[0];
     }
     
 	public static OrderWS createMockOrder(int userId, int orderLinesCount, BigDecimal linePrice) {
 		OrderWS order = new OrderWS();
     	order.setUserId(userId); 
         order.setBillingTypeId(Constants.ORDER_BILLING_PRE_PAID);
         order.setPeriod(1); // once
         order.setCurrencyId(1);
         
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
         // CXF returns null for empty array
 		//assertNotNull(array);
         if (array != null) {
             assertEquals("Empty array expected: " + Arrays.toString(array), 0, array.length);
         }
 	}
 	
     public void testUpdateLines() {
         try {
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
             OrderLineWS[] aLines = new OrderLineWS[lines.size()];
             lines.toArray(aLines);
             order.setOrderLines(aLines);
             
             // call the update
             System.out.println("Adding one order line");
             api.updateOrder(order);
             
             // let's see if my new line is there
             order = api.getOrder(orderId);
             System.out.println("Got updated order with " + order.getOrderLines().length + " lines");
             assertEquals("One more line should be there", initialCount + 1, 
                     order.getOrderLines().length);
             
             // and again
             initialCount = order.getOrderLines().length;
             lines = new ArrayList<OrderLineWS>();
             Collections.addAll(lines, order.getOrderLines());
             line.setItemId(1); // to add another line, you need a different item
             lines.add(line);
             aLines = new OrderLineWS[lines.size()];
             System.out.println("lines now " + aLines.length);
             lines.toArray(aLines);
             order.setOrderLines(aLines);
             
             // call the update
             System.out.println("Adding another order line");
             api.updateOrder(order);
             
             // let's see if my new line is there
             order = api.getOrder(orderId);
             System.out.println("Got updated order with " + order.getOrderLines().length + " lines");
             assertEquals("One more line should be there", initialCount + 1, 
                     order.getOrderLines().length);
             
 
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception: " + e);
         }
     }
     
     public void testRecreate() {
         try {
             JbillingAPI api = JbillingAPIFactory.getAPI();
             // the the latest
             OrderWS order = api.getLatestOrder(GANDALF_USER_ID);
             // use it to create another one
             Integer newOrder = api.createOrder(order);
             assertTrue("New order newer than original", order.getId().compareTo(newOrder) < 0);
             // clean up
             api.deleteOrder(newOrder);
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception: " + e);
         }
     }
 
     public void testRefundAndCancelFee() {
         try {
             final Integer USER_ID = 1000;
 
             // create an order an order for testing
             JbillingAPI api = JbillingAPIFactory.getAPI();
 
             OrderWS newOrder = new OrderWS();
             newOrder.setUserId(USER_ID); 
             newOrder.setBillingTypeId(Constants.ORDER_BILLING_PRE_PAID);
             newOrder.setPeriod(2);
             newOrder.setCurrencyId(new Integer(1));
 
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
             assertEquals("Quantity", "-3", orderLine.getQuantity());
             assertEquals("Price", "15", orderLine.getPrice());
             assertEquals("Amount", "-45", orderLine.getAmount());
 
             // order 3 - cancel fee for lemonade (see the rule in CancelFees.drl)
             order = api.getOrder(list[1]);
             assertEquals("No. of order lines", 1, order.getOrderLines().length);
             orderLine = order.getOrderLines()[0];
             assertEquals("Item Id", new Integer(24), orderLine.getItemId());
             assertEquals("Quantity", "2", orderLine.getQuantity());
             assertEquals("Price", "5", orderLine.getPrice());
             assertEquals("Amount", "10", orderLine.getAmount());
 
             // order 2 - lemonade refund
             order = api.getOrder(list[2]);
             assertEquals("No. of order lines", 1, order.getOrderLines().length);
             orderLine = order.getOrderLines()[0];
             assertEquals("Item Id", new Integer(1), orderLine.getItemId());
             assertEquals("Quantity", "-2", orderLine.getQuantity());
             assertEquals("Price", "10", orderLine.getPrice());
             assertEquals("Amount", "-20", orderLine.getAmount());
 
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
             assertEquals("Quantity", "-5", orderLine.getQuantity());
             assertEquals("Price", "10", orderLine.getPrice());
             assertEquals("Amount", "-50", orderLine.getAmount());
             orderLine = order.getOrderLines()[1];
             assertEquals("Item Id", new Integer(3), orderLine.getItemId());
             assertEquals("Quantity", "-5", orderLine.getQuantity());
             assertEquals("Price", "15", orderLine.getPrice());
             assertEquals("Amount", "-75", orderLine.getAmount());
 
             // order 2 - cancel fee for lemonades (see the rule in CancelFees.drl)
             order = api.getOrder(list[1]);
             assertEquals("No. of order lines", 1, order.getOrderLines().length);
             orderLine = order.getOrderLines()[0];
             assertEquals("Item Id", new Integer(24), orderLine.getItemId());
             // 2 periods cancelled (2 periods * 5 fee quantity)
             assertEquals("Quantity", "10", orderLine.getQuantity());
             assertEquals("Price", "5", orderLine.getPrice());
             assertEquals("Amount", "50", orderLine.getAmount());
 
             // remove invoices
             list = api.getLastInvoices(new Integer(USER_ID), new Integer(2));
             api.deleteInvoice(list[0]);
             api.deleteInvoice(list[1]);
             // remove orders
             list = api.getLastOrders(new Integer(USER_ID), new Integer(7));
             for (int i = 0; i < list.length; i++) {
                 api.deleteOrder(list[i]);
             }
 
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
 
     public void testDefaultCycleStart() {
         try {
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
             
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
     
     public void testPlan() {
         try {
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
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
 
     // Tests InternalEventsRulesTask plug-in.
     // See also InternalEventsRulesTask520.drl.
     public void testInternalEventsRulesTask() {
         try {
             final Integer USER_ID = 1010;
 
             JbillingAPI api = JbillingAPIFactory.getAPI();
 
             // create order with 2 lines (item ids 1 & 2) and invoice
             OrderWS order = createMockOrder(USER_ID, 2, new BigDecimal("5.00"));
             order.setNotes("Change me.");
             Integer invoiceId = api.createOrderAndInvoice(order);
 
             // get back created order
             InvoiceWS invoice = api.getInvoiceWS(invoiceId);
             Integer orderId = invoice.getOrders()[0];
             order = api.getOrder(orderId);
 
             // check order was modified
             assertEquals("Order was changed by rules", "Modified by rules.", 
                     order.getNotes());
             OrderLineWS[] orderLines = order.getOrderLines();
             assertEquals("Only 1 order line", 1, orderLines.length);
             assertEquals("Item id 1 was removed", new Integer(2), 
                     orderLines[0].getItemId());
 
             // double check the invoice lines
             InvoiceLineDTO[] invoiceLines = invoice.getInvoiceLines();
             assertEquals("Only 1 invoice line", 1, invoiceLines.length);
             assertEquals("Item id 1 was removed", new Integer(2), 
                     invoiceLines[0].getItemId());
 
             // clean up
             api.deleteInvoice(invoiceId);
             api.deleteOrder(orderId);
 
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
 
     public void testCurrentOrder() {
         try {
             final Integer USER_ID = GANDALF_USER_ID;
             final Integer NO_MAIN_SUB_USER_ID = 1010;
 
             JbillingAPI api = JbillingAPIFactory.getAPI();
 
             /*
              * Test update current order without pricing fields.
              */
 
             // current order before modification
             OrderWS currentOrderBefore = api.getCurrentOrder(USER_ID, 
                     new Date());
             // CXF returns null for empty arrays
             if (currentOrderBefore.getOrderLines() != null) {
                 assertEquals("No order lines.", 0, 
                         currentOrderBefore.getOrderLines().length);
             }
 
             // add a single line
             OrderLineWS newLine = new OrderLineWS();
             newLine.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
             newLine.setItemId(new Integer(1));
             newLine.setQuantity(new BigDecimal("22.00"));
             // take the price and description from the item
             newLine.setUseItem(new Boolean(true));
 
             // update the current order
             OrderWS currentOrderAfter = api.updateCurrentOrder(USER_ID, 
                     new OrderLineWS[] { newLine }, null, new Date(), 
                     "Event from WS");
 
             // asserts
             assertEquals("Order ids", currentOrderBefore.getId(), currentOrderAfter.getId());
             assertEquals("1 new order line", 1, currentOrderAfter.getOrderLines().length);
 
             OrderLineWS createdLine = currentOrderAfter.getOrderLines()[0];
             assertEquals("Order line item ids", newLine.getItemId(),  createdLine.getItemId());
             assertEquals("Order line quantities", newLine.getQuantity(), createdLine.getQuantity());
             assertEquals("Order line price", "10", createdLine.getPrice());
             assertEquals("Order line total", "220.00", createdLine.getAmount());
 
 
             /*
              * Test update current order with pricing fields.
              */
 
             // A pricing rule. See PricingRules.drl, rule 'PricingField test1'.
             PricingField pf = new PricingField("newPrice", new BigDecimal("5.0"));
             newLine.setQuantity(1);
             currentOrderAfter = api.updateCurrentOrder(USER_ID, 
                     new OrderLineWS[] { newLine }, new PricingField[] { pf }, 
                     new Date(), "Event from WS");
 
             // asserts
             assertEquals("1 order line", 1, currentOrderAfter.getOrderLines().length);
             createdLine = currentOrderAfter.getOrderLines()[0];
             assertEquals("Order line ids", newLine.getItemId(), createdLine.getItemId());
             assertEquals("Order line quantities", "23", createdLine.getQuantity());
             assertEquals("Order line price", "10", createdLine.getPrice());
 
             // Note that because of the rule, the result should be 
             // 225.0, not 230.0.
             assertEquals("Order line total", "225", createdLine.getAmount());
 
 
             /*
              * Test update current order with pricing fields and no 
              * order lines. RulesMediationTask should create them.
              */
 
             // Call info pricing fields. See Mediation.drl, rule 'line creation'
             PricingField duration = new PricingField("duration", 5); // 5 min
             PricingField dst = new PricingField("dst", "12345678");
             currentOrderAfter = api.updateCurrentOrder(USER_ID, null, 
                     new PricingField[] { pf, duration, dst }, new Date(),
                     "Event from WS");
 
             // asserts
             assertEquals("2 order line", 2, currentOrderAfter.getOrderLines().length);
 
             createdLine = currentOrderAfter.getOrderLines()[0];
             assertEquals("Order line ids", newLine.getItemId(), createdLine.getItemId());
             assertEquals("Order line quantities", "23", createdLine.getQuantity());
             assertEquals("Order line price", "10", createdLine.getPrice());
             assertEquals("Order line total", "225", createdLine.getAmount());
 
             // 'newPrice' pricing field, $5 * 5 units = 25
             createdLine = currentOrderAfter.getOrderLines()[1];
             assertEquals("Order line quantities", "5", createdLine.getQuantity());
             assertEquals("Order line price", "5", createdLine.getPrice());
             assertEquals("Order line price", "25", createdLine.getAmount()); // not priced
 
             /*
              * No main subscription order tests.
              */
 
             // User with no main subscription order should return
             // null when trying to get a current order.
             assertNull("User with no main subscription order should have " +
                     "null current order", api.getCurrentOrder(
                     NO_MAIN_SUB_USER_ID, new Date()));
 
             // An exception should be thrown
             try {
                 api.updateCurrentOrder(NO_MAIN_SUB_USER_ID, 
                         new OrderLineWS[] { newLine }, null, new Date(),
                         "Event from WS");
                 fail("User with no main subscription order should throw an " +
                         "exception");
             } catch(Exception e) {
             }
 
 
             /*
              * Security tests
              */
             try {
                 api.getCurrentOrder(13, new Date());
                 fail("User 13 belongs to entity 2");
             } catch (Exception e) {
             }
 
             try {
                 api.updateCurrentOrder(13, new OrderLineWS[] { newLine }, 
                         new PricingField[] { pf }, new Date(), "Event from WS");
                 fail("User 13 belongs to entity 2");
             } catch (Exception e) {
             }                
 
 
             // cleanup
             api.deleteOrder(currentOrderAfter.getId());
 
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
     
     public void testIsUserSubscribedTo() throws Exception {
     	JbillingAPI api = JbillingAPIFactory.getAPI();
     	
     	// Test a non-existing user first, result should be 0
     	BigDecimal result = api.isUserSubscribedTo(Integer.valueOf(999), Integer.valueOf(999));
     	assertEquals(BigDecimal.ZERO, result);
     	
     	// Test the result given by a known existing user (
     	// in PostgreSQL test db)
     	result = api.isUserSubscribedTo(Integer.valueOf(2), Integer.valueOf(2));
     	assertEquals(new BigDecimal("1"), result);
     	
     	// Test another user
     	result = api.isUserSubscribedTo(Integer.valueOf(73), Integer.valueOf(1));
     	assertEquals(new BigDecimal("89"), result);
     }
     
     public void testGetUserItemsByCategory() throws Exception {
     	JbillingAPI api = JbillingAPIFactory.getAPI();
     	
     	// Test a non-existing user first, result should be 0
     	Integer[] result = api.getUserItemsByCategory(
     			Integer.valueOf(999), 
     			Integer.valueOf(999));
     	assertNull(result);
     	
     	// Test the result given by a known existing user 
     	// (it has items 2 and 3 on category 1
     	// in PostgreSQL test db)
     	result = api.getUserItemsByCategory(Integer.valueOf(2), 
     			Integer.valueOf(1));
     	assertEquals(2, result.length);
     	assertEquals(Integer.valueOf(2), result[0]);
     	assertEquals(Integer.valueOf(3), result[1]);
     	
     	// Test another user (has items 1 and 2 on cat. 1)
     	result = api.getUserItemsByCategory(Integer.valueOf(73), 
     			Integer.valueOf(1));
     	assertEquals(2, result.length);
     	assertEquals(Integer.valueOf(1), result[0]);
     	assertEquals(Integer.valueOf(2), result[1]);
     }
 
     private void pause(long t) {
         System.out.println("pausing for " + t + " ms...");
         try {
             Thread.sleep(t);
         } catch (InterruptedException e) {
         }
     }
 
     public void testMainOrder() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         // note: for some reason, calling api.getUsersByCreditCard("1152") returns three users
         // but after calling updateUser, it reutrns 4 because Gandalf is included.
         // why is not picking him up before? What is updateUser doing that then the CC shows up?
         // get gandalf's orders
        Integer orders[] = api.getLastInvoices(GANDALF_USER_ID, 100);
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
         assertEquals("User does not have the correct main order", orders[orders.length - 1],
                 api.getUserWS(GANDALF_USER_ID).getMainOrderId());
         // update the user (restore main order)
         user.setMainOrderId(mainOrder);
         api.updateUser(user);
         assertEquals("User does not have the original main order", mainOrder,
                 api.getUserWS(GANDALF_USER_ID).getMainOrderId());
     }
 
     public void testOrderLineDescriptionLanguage() {
         try {
             final Integer USER_ID = 10750; // french speaker
 
             JbillingAPI api = JbillingAPIFactory.getAPI();
 
             // create order
             OrderWS order = new OrderWS();
             order.setUserId(USER_ID); 
             order.setBillingTypeId(Constants.ORDER_BILLING_PRE_PAID);
             order.setPeriod(1); // once
             order.setCurrencyId(1);
 
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
             assertEquals("French description", "French Lemonade", 
                     invoiceLine.getDescription());
 
             // clean up
             api.deleteInvoice(invoiceId);
             api.deleteOrder(invoice.getOrders()[0]);
 
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
 
     public void testItemSwappingRules() {
         try {
             JbillingAPI api = JbillingAPIFactory.getAPI();
             // add items to a user subscribed to 1
             System.out.println("Testing item swapping - included in plan");
             OrderWS order = createMockOrder(1070, 1, new BigDecimal("1.00"));
             order.getOrderLines()[0].setItemId(2600); // the generic lemonade
             order.getOrderLines()[0].setUseItem(true);
 
             int orderId = api.createOrder(order);
             order = api.getOrder(orderId);
             assertEquals("Order should have one line", 1, order.getOrderLines().length);
             assertEquals("Order should have the included in plan line", 2601, 
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
             assertEquals("Order should have the priced item line", 2602,
                     order.getOrderLines()[0].getItemId().intValue());
 
             // cleanup
             api.deleteOrder(orderId);
 
 
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
 
     public void testRateCard() {
         try {
             JbillingAPI api = JbillingAPIFactory.getAPI();
 
             System.out.println("Testing Rate Card");
 
             // user for tests
             UserWS user = com.sapienter.jbilling.server.user.WSTest.createUser(
                     true, null, null);
             Integer userId = user.getUserId();
             // create main subscription order
             Integer mainOrderId = com.sapienter.jbilling.server.user.WSTest
                     .createMainSubscriptionOrder(userId, 1);
             // update to credit limit
             user.setBalanceType(Constants.BALANCE_CREDIT_LIMIT);
             user.setCreditLimit(new BigDecimal("100.0"));
             api.updateUser(user);
 
 
             /* updateCurrentOrder */
             // should be priced at 0.33 (see row 548)
             PricingField[] pf = {
                     new PricingField("dst", "55999"), 
                     new PricingField("duration", 1) };
 
             OrderWS currentOrder = api.updateCurrentOrder(userId,
                     null, pf, new Date(), "Event from WS");
 
             assertEquals("1 order line", 1, currentOrder.getOrderLines().length);
             OrderLineWS line = currentOrder.getOrderLines()[0];
             assertEquals("order line itemId", 2800, line.getItemId().intValue());
             assertEquals("order line quantity", "1", line.getQuantity());
             assertEquals("order line total", new BigDecimal("0.33"), 
                     line.getAmountAsDecimal());
 
             // check dynamic balance
             user = api.getUserWS(userId);
             assertEquals("dynamic balance", new BigDecimal("0.33"), 
                     user.getDynamicBalanceAsDecimal());
 
             // should be priced at 0.08 (see row 1753)
             pf[0].setStrValue("55000");
             currentOrder = api.updateCurrentOrder(userId,
                     null, pf, new Date(), "Event from WS");
 
             assertEquals("1 order line", 1, currentOrder.getOrderLines().length);
             line = currentOrder.getOrderLines()[0];
             assertEquals("order line itemId", 2800, line.getItemId().intValue());
             assertEquals("order line quantity", "2", line.getQuantity());
             // 0.33 + 0.08 = 0.41
             assertEquals("order line total", new BigDecimal("0.41"), 
                     line.getAmountAsDecimal());
 
             // check dynamic balance
             user = api.getUserWS(userId);
             assertEquals("dynamic balance", new BigDecimal("0.41"), 
                     user.getDynamicBalanceAsDecimal());
 
 
             /* getItem */
             // should be priced at 0.42 (see row 1731)
             pf[0].setStrValue("212222");
             ItemDTOEx item = api.getItem(2800, userId, pf);
             assertEquals("price", new BigDecimal("0.42"), item.getPrice());
 
 
             /* rateOrder */
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
             assertEquals("order line quantity", "10", line.getQuantity());
             // 0.42 * 10 = 4.2
             assertEquals("order line total", new BigDecimal("4.2"), line.getAmountAsDecimal());
 
             /* validatePurchase */
             // should be priced at 0.47 (see row 498)
             pf[0].setStrValue("187630");
             // current balance: 100 - 0.41 = 99.59
             // quantity available expected: 99.59 / 0.47
             ValidatePurchaseWS result = api.validatePurchase(userId,
                     null, pf);
             assertEquals("validate purchase success", Boolean.valueOf(true), 
                     result.getSuccess());
             assertEquals("validate purchase authorized", Boolean.valueOf(true),
                     result.getAuthorized()); 
             assertEquals("validate purchase quantity", new BigDecimal("211.89"),
                     result.getQuantityAsDecimal());
 
             // check current order wasn't updated
             currentOrder = api.getOrder(currentOrder.getId());
             assertEquals("1 order line", 1, currentOrder.getOrderLines().length);
             line = currentOrder.getOrderLines()[0];
             assertEquals("order line itemId", 2800, line.getItemId().intValue());
             assertEquals("order line quantity", "2", line.getQuantity());
             assertEquals("order line total", new BigDecimal("0.41"), 
                     line.getAmountAsDecimal());
 
 
             // clean up
             api.deleteUser(userId);
 
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
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
