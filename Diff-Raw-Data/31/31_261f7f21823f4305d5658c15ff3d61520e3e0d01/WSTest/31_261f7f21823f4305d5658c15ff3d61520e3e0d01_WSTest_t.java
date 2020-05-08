 /*
     jBilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2008 Enterprise jBilling Software Ltd. and Emiliano Conde
 
     This file is part of jbilling.
 
     jbilling is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     jbilling is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 /*
  * Created on Dec 18, 2003
  *
  */
 package com.sapienter.jbilling.server.order;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 
 import junit.framework.TestCase;
 
 import com.sapienter.jbilling.server.invoice.InvoiceWS;
 import com.sapienter.jbilling.server.payment.PaymentAuthorizationDTOEx;
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
             line.setPrice(new Float(10));
             line.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
             line.setQuantity(new Integer(1));
             line.setAmount(new Float(10));
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
             System.out.println("Created order " + ret);
             
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
                     assertEquals("Special price for Item 2", 30F, item2line.getPrice());
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
 	                assertEquals("created line item id", retOrderLine.getItemId(), 
 	                        new Integer(14));
 	                assertEquals("total of discount", new Float(-5.5), retOrderLine.getAmount());
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
             assertEquals("updated quantity", retOrderLine.getQuantity(),
                     new Double(99.0));
             //delete a line through updating with quantity = 0
             System.out.println("Delete order line");
             retOrderLine.setQuantity(new Integer(0));
             api.updateOrderLine(retOrderLine);
             int totalLines = retOrder.getOrderLines().length;
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
             assertEquals("Modified quantity", new Double(2.0),
             		retOrder.getOrderLines()[1].getQuantity());
             assertEquals("New billable date", cal.getTimeInMillis(), 
                     retOrder.getNextBillableDay().getTime());
             for (i = 0; i < retOrder.getOrderLines().length; i++) {
             	retOrderLine = retOrder.getOrderLines()[i];
 	            if (retOrderLine.getItemId().equals(new Integer(14))) {
 	            	// the is one less line for 15
 	            	// but one extra item for 30
 	            	// difference is 15 and 10% of that is 1.5  thus 5.5 + 1.5 = 7
 	                assertEquals("total of discount", new Float(-7.0), retOrderLine.getAmount());
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
     	
     	OrderWS order = createMockOrder(USER_ID, 3, 42);
     	Integer orderId = callcreateOrderAndInvoice(order);
         assertNotNull(orderId);
         
         InvoiceWS afterNormalOrder = callGetLatestInvoice(USER_ID);
         assertNotNull("createOrderAndInvoice should create invoice", afterNormalOrder);
         assertNotNull("invoice without id", afterNormalOrder.getId());
         
         if (before != null){
         	assertFalse("createOrderAndInvoice should create the most recent invoice", afterNormalOrder.getId().equals(before.getId()));
         }
         
         //even if empty
     	OrderWS emptyOrder = createMockOrder(USER_ID, 0, 123); //empty
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
     	
     	OrderWS orderWS = createMockOrder(USER_ID, 2, 234);
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
     	
     	OrderWS requestOrder = createMockOrder(USER_ID, LINES, 567);
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
     		assertEquals(nextRequested.getAmount(), nextActual.getAmount());
     		assertEquals(nextRequested.getQuantity(), nextActual.getQuantity());
     		assertEquals(nextRequested.getPrice(), nextActual.getPrice());
     	}
     }
     
     public void testAutoCreatedInvoiceIsCorrect() throws Exception {
     	final int USER_ID = GANDALF_USER_ID;
     	final int LINES = 2;
 
     	// it is critical to make sure that this invoice can not be composed by
 		// previous payments
     	// so, make the price unusual
     	final float PRICE = 687654.29f;
     	
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
     	assertEquals(PRICE * LINES, invoice.getBalance().floatValue(), 0.00000001f);
     }
     
     public void testAutoCreatedInvoiceIsPayable() throws Exception {
     	final int USER_ID = GANDALF_USER_ID;
     	callcreateOrderAndInvoice(createMockOrder(USER_ID, 1, 789));
     	InvoiceWS invoice = callGetLatestInvoice(USER_ID);
     	assertNotNull(invoice);
     	assertNotNull(invoice.getId());
         assertEquals("new invoice is not paid", 1, invoice.getToProcess().intValue());
         assertTrue("new invoice with a balance", invoice.getBalance().floatValue() > 0);
         JbillingAPI api = JbillingAPIFactory.getAPI();
     	PaymentAuthorizationDTOEx auth = api.payInvoice(invoice.getId());
     	assertNotNull(auth);
         assertEquals("Payment result OK", true, auth.getResult().booleanValue());
         assertEquals("Processor code", "The transaction has been approved", 
                 auth.getResponseMessage());
                 
         // now the invoice should be shown as paid
         invoice = callGetLatestInvoice(USER_ID);
         assertNotNull(invoice);
         assertNotNull(invoice.getId());
         assertEquals("new invoice is now paid", 0, invoice.getToProcess().intValue());
         assertTrue("new invoice without a balance", invoice.getBalance().floatValue() == 0F);
 
     }
     
     public void testEmptyInvoiceIsNotPayable() throws Exception {
     	final int USER_ID = GANDALF_USER_ID;
     	callcreateOrderAndInvoice(createMockOrder(USER_ID, 0, 890)); 
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
     	return api.createOrderAndInvoice(order);
     	
     }
     
 	public static OrderWS createMockOrder(int userId, int orderLinesCount, float linePrice) {
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
             nextLine.setAmount(nextLine.getQuantity().floatValue() * linePrice);
             
             lines.add(nextLine);
         }
         order.setOrderLines(lines.toArray(new OrderLineWS[lines.size()]));
 		return order;
 	}
 	
 	private void assertEmptyArray(Object[] array){
 		assertNotNull(array);
 		assertEquals("Empty array expected: " + Arrays.toString(array), 0, array.length);
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
            assertEquals("Quantity", new Double(-3), orderLine.getQuantity());
            assertEquals("Price", new Float(15), orderLine.getPrice());
             assertEquals("Amount", new Float(-45), orderLine.getAmount());
 
             // order 2 - lemonade refund
             order = api.getOrder(list[1]);
             assertEquals("No. of order lines", 1, order.getOrderLines().length);
             orderLine = order.getOrderLines()[0];
             assertEquals("Item Id", new Integer(1), orderLine.getItemId());
            assertEquals("Quantity", new Double(-2), orderLine.getQuantity());
            assertEquals("Price", new Float(10), orderLine.getPrice());
             assertEquals("Amount", new Float(-20), orderLine.getAmount());
 
             // order 3 - cancel fee for lemonade (see the rule in CancelFees.drl)
             order = api.getOrder(list[2]);
             assertEquals("No. of order lines", 1, order.getOrderLines().length);
             orderLine = order.getOrderLines()[0];
             assertEquals("Item Id", new Integer(24), orderLine.getItemId());
             assertEquals("Quantity", new Double(2), orderLine.getQuantity());
             assertEquals("Price", new Float(5), orderLine.getPrice());
             assertEquals("Amount", new Float(10), orderLine.getAmount());
 
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
            assertEquals("Quantity", new Double(-5), orderLine.getQuantity());
            assertEquals("Price", new Float(10), orderLine.getPrice());
             assertEquals("Amount", new Float(-50), orderLine.getAmount());
             orderLine = order.getOrderLines()[1];
             assertEquals("Item Id", new Integer(3), orderLine.getItemId());
            assertEquals("Quantity", new Double(-5), orderLine.getQuantity());
            assertEquals("Price", new Float(15), orderLine.getPrice());
             assertEquals("Amount", new Float(-75), orderLine.getAmount());
 
             // order 2 - cancel fee for lemonades (see the rule in CancelFees.drl)
             order = api.getOrder(list[1]);
             assertEquals("No. of order lines", 1, order.getOrderLines().length);
             orderLine = order.getOrderLines()[0];
             assertEquals("Item Id", new Integer(24), orderLine.getItemId());
             // 2 periods cancelled (2 periods * 5 fee quantity)
             assertEquals("Quantity", new Double(10), orderLine.getQuantity());
             assertEquals("Price", new Float(5), orderLine.getPrice());
             assertEquals("Amount", new Float(50), orderLine.getAmount());
 
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
 
             // create an order an order for testing
             JbillingAPI api = JbillingAPIFactory.getAPI();
 
             // create a main subscription (current) order
             OrderWS mainOrder = createMockOrder(USER_ID, 1, 10);
             mainOrder.setPeriod(2);
             mainOrder.setIsCurrent(1);
             mainOrder.setCycleStarts(new Date());
             System.out.println("Creating main subscription order ...");
             Integer mainOrderId = api.createOrder(mainOrder);
             assertNotNull("The order was not created", mainOrderId);
 
             // create another order and see if cycle starts was set
             OrderWS testOrder = createMockOrder(USER_ID, 1, 20);
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
             testOrder = createMockOrder(USER_ID, 1, 30);
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
             testOrder = createMockOrder(USER_ID, 1, 40);
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
 }
