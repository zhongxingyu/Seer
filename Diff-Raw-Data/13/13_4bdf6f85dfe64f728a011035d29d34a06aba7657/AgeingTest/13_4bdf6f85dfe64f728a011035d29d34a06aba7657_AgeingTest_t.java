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
 
 package com.sapienter.jbilling.server.process;
 
 import com.sapienter.jbilling.server.invoice.InvoiceWS;
 import com.sapienter.jbilling.server.order.OrderLineWS;
 import com.sapienter.jbilling.server.order.OrderWS;
 import com.sapienter.jbilling.server.user.UserDTOEx;
 import com.sapienter.jbilling.server.user.UserWS;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.api.JbillingAPI;
 import com.sapienter.jbilling.server.util.api.JbillingAPIException;
 import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;
 import com.sapienter.jbilling.server.util.api.SpringAPI;
 import junit.framework.TestCase;
 import org.joda.time.DateMidnight;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  * AgeingTest
  *
  * @author Brian Cowdery
  * @since 31/05/11
  */
 public class AgeingTest extends TestCase {
 
     private static final Integer AGEING_TEST_USER_ID = 10790; // ageing-test-01
 
     Calendar calendar = GregorianCalendar.getInstance();
 
     public AgeingTest() {
     }
 
     public AgeingTest(String name) {
         super(name);
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         calendar.clear();
     }
 
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     /**
      * Test Ageing.
      *
      * Create an invoice and trigger the ageing process for various dates to simulate natural ageing of an invoice.
      *
      * Note that this test runs quickly when run standalone, but very slowly if ran after the billing process
      * when the system contains thousands of invoices to be aged.
      *
      * @throws Exception unhandled exception from API
      */
     public void testAgeing() throws Exception {
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
 
         // configure billing so that the generated invoices due date will be 1 month after the invoice
         // is created. This should make it easy to determine ageing dates.
         BillingProcessConfigurationWS config = api.getBillingProcessConfiguration();
         config.setNextRunDate(new DateMidnight(2006, 10, 26).toDate());
         config.setDueDateUnitId(Constants.PERIOD_UNIT_MONTH);
         config.setDueDateValue(1);
         config.setPeriodUnitId(Constants.PERIOD_UNIT_MONTH);
         config.setPeriodValue(1);
 
 
         // create a new order and a invoice to be aged
         OrderWS order = new OrderWS();
         order.setUserId(AGEING_TEST_USER_ID);
         order.setBillingTypeId(Constants.ORDER_BILLING_PRE_PAID);
         order.setPeriod(Constants.ORDER_PERIOD_ONCE);
         order.setCurrencyId(1);
         order.setActiveSince(new DateMidnight(2006, 10, 1).toDate());
         order.setCycleStarts(new DateMidnight(2006, 10, 1).toDate());
 
         OrderLineWS line = new OrderLineWS();
         line.setPrice(new BigDecimal("10.00"));
         line.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
         line.setQuantity(1);
         line.setAmount(new BigDecimal("10.00"));
         line.setDescription("Generic order line");
         line.setItemId(1);
 
         order.setOrderLines(new OrderLineWS[] { line });
 
         Integer orderId = api.createOrder(order);
         Integer invoiceId = api.createInvoiceFromOrder(orderId, null);
 
 
         /*
            Ageing days:
 
                grace period = 5               n/a
                overdue1     = 3               > 5 days overdue              overdue on the 8th day
                overdue2     = 1               > 8 days overdue              overdue 2 on the 9th day
                suspended 1  = 2               > 9 days overdue              suspended on the 10th day
                suspended 3  = 30              > 11 days overdue             suspended 3 on the 12th day
                deleted (end)                  > 41 days overdue             deleted on the 41st day
         */
 
         UserWS user = api.getUserWS(AGEING_TEST_USER_ID);
         InvoiceWS invoice = api.getInvoiceWS(invoiceId);
 
         calendar.clear();
         calendar.setTime(invoice.getDueDate());
         System.out.println("Due date: " + calendar.getTime());
 
 
         // the grace period should keep this user active
         calendar.add(Calendar.DATE, 5); // + 5 days, equal to grace period
         System.out.println("Last day of grace period: " + calendar.getTime());
 
        AgeingStatusChecker statusChecker = new AgeingStatusChecker(api);
         Thread statusCheckingThread = new Thread(statusChecker);
         statusCheckingThread.start();
 
         Long start = new Date().getTime();
         api.triggerAgeing(calendar.getTime());
         Long end = new Date().getTime();
 
         statusChecker.stopChecking();
         System.out.println("Ageing process occupy " + (end - start)+ "ms");
         if (start + 1500 < end) { // we have not time to check status if ageing has been done very quickly
             assertTrue("Ageing has to been active", statusChecker.wasRunning);
         }
 
         user = api.getUserWS(AGEING_TEST_USER_ID);
         assertEquals("grace period", UserDTOEx.STATUS_ACTIVE, user.getStatusId());
 
 
         // when the grace over, she should be warned
         calendar.add(Calendar.DATE, 1); // + 6 days
         System.out.println("Overdue on: " + calendar.getTime());
 
         api.triggerAgeing(calendar.getTime());
         user = api.getUserWS(AGEING_TEST_USER_ID);
         assertEquals("to overdue", UserDTOEx.STATUS_ACTIVE + 1, user.getStatusId().intValue());
 
 
         // two day after, the status should be the same
         calendar.add(Calendar.DATE, 2); // + 8 days
         System.out.println("Still overdue on: " + calendar.getTime());
 
         api.triggerAgeing(calendar.getTime());
         user = api.getUserWS(AGEING_TEST_USER_ID);
         assertEquals("still overdue", UserDTOEx.STATUS_ACTIVE + 1, user.getStatusId().intValue());
 
 
         // after three days of the warning, fire the next one
         calendar.add(Calendar.DATE, 1); // + 9 days
         System.out.println("Overdue 2 on: " + calendar.getTime());
 
         api.triggerAgeing(calendar.getTime());
         user = api.getUserWS(AGEING_TEST_USER_ID);
         assertEquals("to overdue 2", UserDTOEx.STATUS_ACTIVE + 2, user.getStatusId().intValue());
 
 
         // the next day it goes to suspended
         calendar.add(Calendar.DATE, 1); // + 10 days
         System.out.println("Suspended on: " + calendar.getTime());
 
         api.triggerAgeing(calendar.getTime());
         user = api.getUserWS(AGEING_TEST_USER_ID);
         assertEquals("to suspended", UserDTOEx.STATUS_ACTIVE + 4, user.getStatusId().intValue());
 
 
         // two days for suspended 3
         calendar.add(Calendar.DATE, 2); // + 12 days
         System.out.println("Suspended 3 on: " + calendar.getTime());
 
         api.triggerAgeing(calendar.getTime());
         user = api.getUserWS(AGEING_TEST_USER_ID);
         assertEquals("to suspended 3", UserDTOEx.STATUS_ACTIVE + 6, user.getStatusId().intValue());
 
 
         // 30 days for deleted
         calendar.add(Calendar.DATE, 30); // + 41 days
         System.out.println("Deleted on: " + calendar.getTime());
 
         api.triggerAgeing(calendar.getTime());
         user = api.getUserWS(AGEING_TEST_USER_ID);
         assertEquals("deleted", 1, user.getDeleted());
     }
 
     public void testAgeingProcessStatus() throws Exception {
 
         JbillingAPI api = JbillingAPIFactory.getAPI();
 
         assertFalse("No active ageing processes yet!", api.isAgeingProcessRunning());
 
         ProcessStatusWS status = api.getAgeingProcessStatus();
         assertNotNull("Status should be retrieved", status);
         assertEquals("Process status should be FINISHED", ProcessStatusWS.State.FINISHED, status.getState());
     }
 
     private class AgeingStatusChecker implements Runnable {
         protected Boolean wasRunning = false;
         protected AtomicBoolean active = new AtomicBoolean(true);
         private JbillingAPI api = null;
 
         public void stopChecking() {
             active.set(false);
         }
        public AgeingStatusChecker(JbillingAPI api) {
            this.api = api;
         }
         @Override
         public void run() {
             while (active.get() && !wasRunning) {
                 wasRunning = api.isAgeingProcessRunning();
                 try {
                     Thread.sleep(50);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 }
