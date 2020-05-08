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
 
 package com.sapienter.jbilling.server.provisioning;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import junit.framework.TestCase;
 
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.server.order.OrderLineWS;
 import com.sapienter.jbilling.server.order.OrderWS;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.RemoteContext;
 import com.sapienter.jbilling.server.util.api.JbillingAPI;
 import com.sapienter.jbilling.server.util.api.JbillingAPIException;
 import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;
 
 public class ProvisioningTest extends TestCase {
     private static final int           ORDER_LINES_COUNT  = 6;
     private static final int           USER_ID            = 1000;
     private static Integer[]           itemIds            = {
         1, 2, 3, 24, 240, 14
     };
     private static Integer[]           provisioningStatus = new Integer[6];
     private IProvisioningProcessSessionBean remoteProvisioning = null;
     JbillingAPI                        api;
 
     /**
      *
      * @see junit.framework.TestCase#setUp()
      */
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         api = JbillingAPIFactory.getAPI();
 
         remoteProvisioning = (IProvisioningProcessSessionBean) 
                 RemoteContext.getBean(
                 RemoteContext.Name.PROVISIONING_PROCESS_REMOTE_SESSION);
     }
 
     private void pause(long t) {
         
         try {
             Thread.sleep(t);
         } catch (InterruptedException e) {
 
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     public void testNewQuantityEvent() {
         try {
             provisioningStatus[0] = Constants.PROVISIONING_STATUS_ACTIVE;
             provisioningStatus[1] = Constants.PROVISIONING_STATUS_INACTIVE;
             provisioningStatus[2] = null;
             provisioningStatus[3] = Constants.PROVISIONING_STATUS_PENDING_ACTIVE;
             provisioningStatus[4] = Constants.PROVISIONING_STATUS_PENDING_INACTIVE;
             provisioningStatus[5] = null;
 
             OrderWS newOrder = createMockOrder(USER_ID, ORDER_LINES_COUNT, new BigDecimal("77"));
 
             newOrder.setActiveSince(null);
 
             // create order through api
             Integer ret = api.createOrder(newOrder);
 
             System.out.println("Created order." + ret);
             assertNotNull("The order was not created", ret);
             System.out.println("running provisioning batch process..");
             //pause(2000);
             remoteProvisioning.trigger();
             pause(2000);
             System.out.println("Getting back order " + ret);
 
             OrderWS retOrder = api.getOrder(ret);
 
             System.out.println("got order: " + retOrder);
 
             OrderLineWS[] retLine = retOrder.getOrderLines();
 
             for (int i = 0; i < retLine.length; i++) {
                 if (i == 0) {
                     assertEquals("order line " + (i + 1) + "", retLine[i].getProvisioningStatusId(),
                                  Constants.PROVISIONING_STATUS_ACTIVE);
                 }
 
                 if (i == 1) {
                     assertEquals("order line " + (i + 1) + "", retLine[i].getProvisioningStatusId(),
                                  Constants.PROVISIONING_STATUS_ACTIVE);
                 }
 
                 if (i == 2) {
                     assertEquals("order line " + (i + 1) + "", retLine[i].getProvisioningStatusId(),
                                  Constants.PROVISIONING_STATUS_INACTIVE); // default
                 }
 
                 if (i == 3) {
                     assertEquals("order line " + (i + 1) + "", retLine[i].getProvisioningStatusId(),
                                  Constants.PROVISIONING_STATUS_PENDING_ACTIVE);
                 }
 
                 if (i == 4) {
                     assertEquals("order line " + (i + 1) + "", retLine[i].getProvisioningStatusId(),
                                  Constants.PROVISIONING_STATUS_PENDING_INACTIVE);
                 }
 
                 if (i == 5) {
                     assertEquals("order line " + (i + 1) + "", retLine[i].getProvisioningStatusId(),
                                  Constants.PROVISIONING_STATUS_INACTIVE); // default
 
                 }
             }
         } catch (JbillingAPIException e) {
             e.printStackTrace();
             fail("Exception!" + e.getMessage());
         } catch (SessionInternalError e) {
             e.printStackTrace();
             fail("Exception!" + e.getMessage());
         }
     }
 
     public void testSubscriptionActiveEvent() {
         try {
             provisioningStatus[0] = Constants.PROVISIONING_STATUS_ACTIVE;
             provisioningStatus[1] = Constants.PROVISIONING_STATUS_INACTIVE;
             provisioningStatus[2] = null;
             provisioningStatus[3] = Constants.PROVISIONING_STATUS_PENDING_ACTIVE;
             provisioningStatus[4] = Constants.PROVISIONING_STATUS_PENDING_INACTIVE;
             provisioningStatus[5] = null;
 
             OrderWS newOrder = createMockOrder(USER_ID, ORDER_LINES_COUNT, new BigDecimal("77"));
 
             // newOrder.setActiveSince(weeksFromToday(1));
             Calendar cal = Calendar.getInstance();
 
             cal.clear();
             cal.set(2008, 9, 29, 0, 0, 0);
             newOrder.setActiveSince(cal.getTime());
 
             // create order through api
             Integer ret = api.createOrder(newOrder);
 
             System.out.println("Created order." + ret);
             assertNotNull("The order was not created", ret);
             System.out.println("running provisioning batch process..");
             //pause(2000);
             remoteProvisioning.trigger();
             pause(2000);
             System.out.println("Getting back order " + ret);
 
             OrderWS retOrder = api.getOrder(ret);
 
             System.out.println("got order: " + retOrder);
 
             OrderLineWS[] retLine = retOrder.getOrderLines();
 
             for (int i = 0; i < retLine.length; i++) {
                 if (i == 0) {
                     assertEquals("order line " + (i + 1) + "", retLine[i].getProvisioningStatusId(),
                                  Constants.PROVISIONING_STATUS_ACTIVE);
                 }
 
                 if (i == 1) {
                     assertEquals("order line " + (i + 1) + "", retLine[i].getProvisioningStatusId(),
                                  Constants.PROVISIONING_STATUS_ACTIVE);
                 }
 
                 if (i == 2) {
                     assertEquals("order line " + (i + 1) + "", retLine[i].getProvisioningStatusId(),
                                  Constants.PROVISIONING_STATUS_INACTIVE); // default
                 }
 
                 if (i == 3) {
                     assertEquals("order line " + (i + 1) + "", retLine[i].getProvisioningStatusId(),
                                  Constants.PROVISIONING_STATUS_PENDING_ACTIVE);
                 }
 
                 if (i == 4) {
                     assertEquals("order line " + (i + 1) + "", retLine[i].getProvisioningStatusId(),
                                  Constants.PROVISIONING_STATUS_PENDING_INACTIVE);
                 }
 
                 if (i == 5) {
                     assertEquals("order line " + (i + 1) + "", retLine[i].getProvisioningStatusId(),
                                  Constants.PROVISIONING_STATUS_INACTIVE); // default
                 }
             }
         } catch (JbillingAPIException e) {
             e.printStackTrace();
             fail("Exception!" + e.getMessage());
         } catch (SessionInternalError e) {
             e.printStackTrace();
             fail("Exception!" + e.getMessage());
         }
     }
 
     public void testSubscriptionInActiveEvent() {
         try {
             provisioningStatus[0] = Constants.PROVISIONING_STATUS_INACTIVE;
             provisioningStatus[1] = Constants.PROVISIONING_STATUS_ACTIVE;
             provisioningStatus[2] = null;
             provisioningStatus[3] = Constants.PROVISIONING_STATUS_PENDING_ACTIVE;
             provisioningStatus[4] = Constants.PROVISIONING_STATUS_PENDING_INACTIVE;
             provisioningStatus[5] = null;
 
             OrderWS newOrder = createMockOrder(USER_ID, ORDER_LINES_COUNT, new BigDecimal("77"));
 
             // newOrder.setActiveSince(weeksFromToday(1));
             Calendar cal = Calendar.getInstance();
 
             cal.clear();
             cal.set(2008, 9, 29, 0, 0, 0);
             newOrder.setActiveUntil(cal.getTime());
 
             // create order through api
             Integer ret = api.createOrder(newOrder);
 
             System.out.println("Created order." + ret);
             assertNotNull("The order was not created", ret);
             System.out.println("running provisioning batch process..");
             //pause(2000);
             remoteProvisioning.trigger();
             pause(2000);
             System.out.println("Getting back order " + ret);
 
             OrderWS retOrder = api.getOrder(ret);
 
             System.out.println("got order: " + retOrder);
 
             OrderLineWS[] retLine = retOrder.getOrderLines();
 
             for (int i = 0; i < retLine.length; i++) {
                 if (i == 0) {
                     assertEquals("order line " + (i + 1) + "", retLine[i].getProvisioningStatusId(),
                                  Constants.PROVISIONING_STATUS_INACTIVE);
                 }
 
                 if (i == 1) {
                     assertEquals("order line " + (i + 1) + "", retLine[i].getProvisioningStatusId(),
                                  Constants.PROVISIONING_STATUS_INACTIVE);
                 }
 
                 if (i == 2) {
                     assertEquals("order line " + (i + 1) + "", retLine[i].getProvisioningStatusId(),
                                  Constants.PROVISIONING_STATUS_INACTIVE); // default
                 }
 
                 if (i == 3) {
                     assertEquals("order line " + (i + 1) + "", retLine[i].getProvisioningStatusId(),
                                  Constants.PROVISIONING_STATUS_PENDING_ACTIVE);
                 }
 
                 if (i == 4) {
                     assertEquals("order line " + (i + 1) + "", retLine[i].getProvisioningStatusId(),
                                  Constants.PROVISIONING_STATUS_PENDING_INACTIVE);
                 }
 
                 if (i == 5) {
                     assertEquals("order line " + (i + 1) + "", retLine[i].getProvisioningStatusId(),
                                  Constants.PROVISIONING_STATUS_INACTIVE); // default
                 }
             }
         } catch (JbillingAPIException e) {
             e.printStackTrace();
             fail("Exception!" + e.getMessage());
         } catch (SessionInternalError e) {
             e.printStackTrace();
             fail("Exception!" + e.getMessage());
         }
     }
 
     public static OrderWS createMockOrder(int userId, int orderLinesCount, BigDecimal linePrice) {
         OrderWS order = new OrderWS();
 
         order.setUserId(userId);
         order.setBillingTypeId(Constants.ORDER_BILLING_PRE_PAID);
         order.setPeriod(1);    // once
         order.setCurrencyId(1);
 
         ArrayList<OrderLineWS> lines = new ArrayList<OrderLineWS>(orderLinesCount);
 
         for (int i = 0; i < orderLinesCount; i++) {
             OrderLineWS nextLine = new OrderLineWS();
 
             nextLine.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
             nextLine.setDescription("Order line: " + i);
             nextLine.setItemId(itemIds[i]);
             nextLine.setQuantity(1);
             nextLine.setPrice(linePrice);
             nextLine.setAmount(nextLine.getQuantityAsDecimal().multiply(linePrice));
             nextLine.setProvisioningStatusId(provisioningStatus[i]);
             lines.add(nextLine);
         }
 
         order.setOrderLines(lines.toArray(new OrderLineWS[lines.size()]));
 
         return order;
     }
 
     /*private Date weeksFromToday(int weekNumber) {
         Calendar calendar = new GregorianCalendar();
 
         calendar.setTimeInMillis(System.currentTimeMillis());
         calendar.add(Calendar.WEEK_OF_YEAR, weekNumber);
 
         return calendar.getTime();
     }*/
 
     public void testExternalProvisioning() {
         try {
             // create the order
             OrderWS order = new OrderWS();
             order.setUserId(USER_ID);
             order.setBillingTypeId(Constants.ORDER_BILLING_PRE_PAID);
             order.setPeriod(1);
             order.setCurrencyId(1);
 
             OrderLineWS line = new OrderLineWS();
             line.setItemId(251);
             line.setQuantity(1); // trigger 'external_provisioning_test' rule
             line.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
             line.setUseItem(true);
             line.setProvisioningStatusId(Constants.PROVISIONING_STATUS_INACTIVE);
 
             order.setOrderLines(new OrderLineWS[] { line });
 
             System.out.println("Creating order ...");
             Integer ret = api.createOrder(order);
             assertNotNull("The order was not created", ret);
 
            pause(12000); // wait for MDBs to complete
             System.out.println("Getting back order " + ret);
 
             // check TestExternalProvisioningMDB was successful
             OrderWS retOrder = api.getOrder(ret); 
             OrderLineWS orderLine = retOrder.getOrderLines()[0];
             assertEquals("Order status should be active. Check log output " +
                     "from TestExternalProvisioningMDB in jbilling.log for " + 
                     "exact error.", Constants.PROVISIONING_STATUS_ACTIVE,
                     orderLine.getProvisioningStatusId());
 
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
 
     }
 
     public void testCAIProvisioning() {
         try {
             // create the order
             OrderWS order = new OrderWS();
             order.setUserId(USER_ID);
             order.setBillingTypeId(Constants.ORDER_BILLING_PRE_PAID);
             order.setPeriod(1);
             order.setCurrencyId(1);
 
             OrderLineWS line = new OrderLineWS();
             line.setItemId(251);
             line.setQuantity(2); // trigger 'cai_test' rule
             line.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
             line.setUseItem(true);
             line.setProvisioningStatusId(Constants.PROVISIONING_STATUS_INACTIVE);
 
             order.setOrderLines(new OrderLineWS[] { line });
 
             System.out.println("Creating order ...");
             Integer ret = api.createOrder(order);
             assertNotNull("The order was not created", ret);
 
             pause(2000); // wait for MDBs to complete
             System.out.println("Getting back order " + ret);
 
             // check TestExternalProvisioningMDB was successful
             OrderWS retOrder = api.getOrder(ret); 
             OrderLineWS orderLine = retOrder.getOrderLines()[0];
             assertEquals("Order status should be active. Check log output " +
                     "from TestExternalProvisioningMDB in jbilling.log for " + 
                     "exact error.", Constants.PROVISIONING_STATUS_ACTIVE,
                     orderLine.getProvisioningStatusId());
 
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
     
     public void testMMSCProvisioning() {
         try {
             // create the order
             OrderWS order = new OrderWS();
             order.setUserId(USER_ID);
             order.setBillingTypeId(Constants.ORDER_BILLING_PRE_PAID);
             order.setPeriod(1);
             order.setCurrencyId(1);
 
             OrderLineWS line = new OrderLineWS();
             line.setItemId(251);
             line.setQuantity(3); // trigger 'mmsc_test' rule
             line.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
             line.setUseItem(true);
             line.setProvisioningStatusId(Constants.PROVISIONING_STATUS_INACTIVE);
 
             order.setOrderLines(new OrderLineWS[] { line });
 
             System.out.println("Creating order ...");
             Integer ret = api.createOrder(order);
             assertNotNull("The order was not created", ret);
 
             pause(2000); // wait for MDBs to complete
             System.out.println("Getting back order " + ret);
 
             // check TestExternalProvisioningMDB was successful
             OrderWS retOrder = api.getOrder(ret); 
             OrderLineWS orderLine = retOrder.getOrderLines()[0];
             assertEquals("Order status should be active. Check log output " +
                     "from TestExternalProvisioningMDB in jbilling.log for " + 
                     "exact error.", Constants.PROVISIONING_STATUS_ACTIVE,
                     orderLine.getProvisioningStatusId());
 
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception caught:" + e);
         }
     }
 }
