 /*
     jbilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2007 Sapienter Billing Software Corp. and Emiliano Conde
 
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
  * Created on 17-Apr-2003
  *
  * Copyright Sapienter Enterprise Software
  */
 package com.sapienter.jbilling.server.process;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Vector;
 
 import junit.framework.TestCase;
 
 import com.sapienter.jbilling.common.JNDILookup;
 import com.sapienter.jbilling.common.Util;
 import com.sapienter.jbilling.interfaces.BillingProcessSession;
 import com.sapienter.jbilling.interfaces.BillingProcessSessionHome;
 import com.sapienter.jbilling.interfaces.InvoiceSession;
 import com.sapienter.jbilling.interfaces.InvoiceSessionHome;
 import com.sapienter.jbilling.interfaces.OrderSession;
 import com.sapienter.jbilling.interfaces.OrderSessionHome;
 import com.sapienter.jbilling.interfaces.PaymentSession;
 import com.sapienter.jbilling.interfaces.PaymentSessionHome;
 import com.sapienter.jbilling.interfaces.UserSession;
 import com.sapienter.jbilling.interfaces.UserSessionHome;
 import com.sapienter.jbilling.server.entity.BillingProcessConfigurationDTO;
 import com.sapienter.jbilling.server.entity.BillingProcessDTO;
 import com.sapienter.jbilling.server.entity.InvoiceDTO;
 import com.sapienter.jbilling.server.invoice.InvoiceDTOEx;
 import com.sapienter.jbilling.server.order.db.OrderDTO;
 import com.sapienter.jbilling.server.order.db.OrderProcessDTO;
 import com.sapienter.jbilling.server.user.UserDTOEx;
 import com.sapienter.jbilling.server.util.Constants;
 
 /**
  * Points to testOrders:
  * Orders :
  * - next billable day
  * - to_process 
  * - start/end of billing period
  * - invoice has (not) generated
  * - billing process relationship
  * - some amounts of the generated invoice
  * Invoices :
  * - if the invoice has been processed or no
  * - to_process
  * - delegated_invoice_id is updated
  * @author Emil
  */
 public class BillingProcessTest extends TestCase {
 
     OrderSession remoteOrder = null;
     InvoiceSession remoteInvoice = null;
     BillingProcessSession remoteBillingProcess = null;
     UserSession remoteUser = null;
     PaymentSession remotePayment = null;
     GregorianCalendar cal;
     Date processDate = null;
     Integer entityId = null;
     Integer languageId = null;
     Date runDate = null;
     
     private static final Integer NEW_INVOICE = new Integer(87);
 
     public BillingProcessTest(String arg0) {
         super(arg0);
     }
     
     protected void setUp() throws Exception {
         // once it run well ;) let's get the order interface
         OrderSessionHome orderHome =
             (OrderSessionHome) JNDILookup.getFactory(true).lookUpHome(
                 OrderSessionHome.class,
                 OrderSessionHome.JNDI_NAME);
         remoteOrder = orderHome.create();
             
         InvoiceSessionHome invoiceHome =
             (InvoiceSessionHome) JNDILookup.getFactory(true).lookUpHome(
                 InvoiceSessionHome.class,
                 InvoiceSessionHome.JNDI_NAME);
         remoteInvoice = invoiceHome.create();
 
         BillingProcessSessionHome billingProcessHome =
                 (BillingProcessSessionHome) JNDILookup.getFactory(true).lookUpHome(
                 BillingProcessSessionHome.class,
                 BillingProcessSessionHome.JNDI_NAME);
         remoteBillingProcess = billingProcessHome.create();
 
         UserSessionHome userHome = (UserSessionHome) JNDILookup.getFactory(
                 true).lookUpHome(UserSessionHome.class, 
                     UserSessionHome.JNDI_NAME);
         remoteUser = userHome.create();
 
         PaymentSessionHome paymentHome = (PaymentSessionHome) JNDILookup.getFactory(
                 true).lookUpHome(PaymentSessionHome.class, 
                     PaymentSessionHome.JNDI_NAME);
         remotePayment= paymentHome.create();
 
         entityId = new Integer(1);
         languageId = new Integer(1);
         cal = new GregorianCalendar();
         cal.clear();
         cal.set(2006, GregorianCalendar.OCTOBER, 26, 0, 0, 0); 
         runDate = cal.getTime();
 
     }
 
     public void testRetry() {
         try {
             // set the configuration to something we are sure about
             BillingProcessConfigurationDTO configDto = remoteBillingProcess.
                     getConfigurationDto(entityId);
             configDto.setNextRunDate(runDate);
             configDto.setRetries(new Integer(1));
             configDto.setDaysForRetry(new Integer(5));
             configDto.setGenerateReport(new Integer(0));
             configDto.setAutoPayment(new Integer(1));
             configDto.setAutoPaymentApplication(new Integer(1));
             configDto.setDfFm(new Integer(0));
             configDto.setDueDateUnitId(Constants.PERIOD_UNIT_MONTH);
             configDto.setDueDateValue(new Integer(1));
             configDto.setInvoiceDateProcess(new Integer(1));
             configDto.setMaximumPeriods(new Integer(10));
             configDto.setOnlyRecurring(new Integer(1));
             configDto.setPeriodUnitId(Constants.PERIOD_UNIT_MONTH);
             configDto.setPeriodValue(new Integer(1));
             
             remoteBillingProcess.createUpdateConfiguration(new Integer(1),
                  configDto);
             
             // retries calculate dates using the real date of the run
             // when know of one from the pre-cooked DB
             cal.set(2000, GregorianCalendar.DECEMBER, 19, 0, 0, 0); 
             Date retryDate = Util.truncateDate(cal.getTime());
             
             // let's monitor invoice 45, which is the one to be retried
             InvoiceDTOEx invoice = remoteInvoice.getInvoiceEx(45, 1);
             assertEquals("Invoice without payments before retry", new Integer(0), 
                     invoice.getPaymentAttempts());
             assertEquals("Invoice without payments before retry - 2", 0, 
                     invoice.getPaymentMap().size());
             
             // get the involved process
             BillingProcessDTOEx lastDto = remoteBillingProcess.getDto(
                     2, languageId);
 
             // run trigger
             remoteBillingProcess.trigger(retryDate);
             
             // get the process again
             BillingProcessDTOEx lastDtoB = remoteBillingProcess.getDto(
                     2, languageId);
             
             assertEquals("18 - No retries", 1, lastDtoB.getRuns().size());
             
             // run trigger 5 days later
             cal.add(GregorianCalendar.DAY_OF_YEAR, 5);
             
             remoteBillingProcess.trigger(cal.getTime());
                       
             // get the process again
             BillingProcessDTOEx lastDtoC = remoteBillingProcess.getDto(
                     2, languageId);
 
             // now a retry should be there          
             assertEquals("19 - First retry", 2, lastDtoC.getRuns().size());
             
             // run trigger 10 days later
             cal.setTime(retryDate);
             cal.add(GregorianCalendar.DAY_OF_YEAR, 10);
             remoteBillingProcess.trigger(cal.getTime());
 
             // get the process again
             lastDtoC = remoteBillingProcess.getDto(
                     2, languageId);
 
             assertEquals("21 - No new retry", 2, lastDtoC.getRuns().size());
             
             // let's monitor invoice 45, which is the one to be retried
             invoice = remoteInvoice.getInvoiceEx(45, 1);
             assertEquals("Invoice without payments before retry", new Integer(1), 
                     invoice.getPaymentAttempts());
             assertEquals("Invoice without payments before retry - 2", 1, 
                     invoice.getPaymentMap().size());
             
             // the billing process has to have a total paid equal to the invoice
             BillingProcessDTOEx process = remoteBillingProcess.getDto(2, 1);
             BillingProcessRunDTOEx run = process.getRuns().lastElement();
             BillingProcessRunTotalDTOEx total = run.getTotals().firstElement();
             assertEquals("Retry total paid equals to invoice total", 
                     invoice.getTotal(), total.getTotalPaid());
            
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception:" + e);
         }
     }
     
     public void testRun() {
         try {
             // get the latest process
             BillingProcessDTOEx lastDto = remoteBillingProcess.getDto(
                     remoteBillingProcess.getLast(entityId),
                     languageId);
         
             // run trigger but too early     
             cal.set(2005, GregorianCalendar.JANUARY, 26); 
             remoteBillingProcess.trigger(cal.getTime());
 
             // get latest run (b)            
             BillingProcessDTOEx lastDtoB = remoteBillingProcess.getDto(
                     remoteBillingProcess.getLast(entityId),
                     languageId);
             
             // no new process should have run
             assertTrue("1 - No new process", lastDto.getId().intValue() == 
                     lastDtoB.getId().intValue());
             // no retry should have run
             assertTrue("2 - No run", lastDto.getRuns().size() == 
                     lastDtoB.getRuns().size());
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception:" + e);
         }
     }
 
     public void testReview() {
         try {
             // get the latest process
             BillingProcessDTOEx lastDto = remoteBillingProcess.getDto(
                     remoteBillingProcess.getLast(entityId),
                     languageId);
             
             // get the review
             BillingProcessDTOEx reviewDto = remoteBillingProcess.getReviewDto(
                     entityId, languageId);
             
             // not review should be there
             assertNull("3 - The test DB should not have any review", reviewDto);
             
             // set the configuration to something we are sure about
             BillingProcessConfigurationDTO configDto = remoteBillingProcess.
                     getConfigurationDto(entityId);
             configDto.setDaysForReport(new Integer(5));
             configDto.setGenerateReport(new Integer(1));
             remoteBillingProcess.createUpdateConfiguration(new Integer(1),
                  configDto);
 
             // run trigger, this time it should run and generate a report     
             remoteBillingProcess.trigger(runDate);
             
            // get the latest process
             BillingProcessDTOEx lastDtoB = remoteBillingProcess.getDto(
                     remoteBillingProcess.getLast(entityId),
                     languageId);
                     
             // no new process should have run
             assertTrue("4 - No new process", lastDto.getId().intValue() == 
                     lastDtoB.getId().intValue());
                     
             // get the review
             reviewDto = remoteBillingProcess.getReviewDto(
                     entityId, languageId);
 
             // now review should be there
             assertNotNull("5 - Review should be there", reviewDto);
             
             // the review should have invoices 
             assertTrue("6 - Invoices in review", reviewDto.getGrandTotal().
                     getInvoiceGenerated().intValue() > 0);
             
             
             // disapprove the review
             remoteBillingProcess.setReviewApproval(new Integer(1), entityId,
                     new Boolean(false));
                     
             // run trigger, but too early (six days, instead of 5)    
             cal.set(2006, GregorianCalendar.OCTOBER, 20); 
             remoteBillingProcess.trigger(cal.getTime());
             
             // get the latest process
             lastDtoB = remoteBillingProcess.getDto(
                     remoteBillingProcess.getLast(entityId),
                     languageId);
             
             // no new process should have run
             assertTrue("7 - No new process, too early", lastDto.getId().intValue() == 
                     lastDtoB.getId().intValue());
  
             // get the review
             BillingProcessDTOEx reviewDto2 = remoteBillingProcess.getReviewDto(
                     entityId, languageId);
            
             assertEquals("8 - No new review run", reviewDto.getId(), 
                     reviewDto2.getId());
                     
             // status of the review should still be disapproved
             configDto = remoteBillingProcess.
                     getConfigurationDto(entityId);
             assertEquals("9 - Review still disapproved", configDto.getReviewStatus(),
                     Constants.REVIEW_STATUS_DISAPPROVED);
 
             // run trigger this time has to generate a review report
             cal.set(2006, GregorianCalendar.OCTOBER, 22); 
             remoteBillingProcess.trigger(cal.getTime());
 
             // get the latest process
             lastDtoB = remoteBillingProcess.getDto(
                     remoteBillingProcess.getLast(entityId),
                     languageId);
             
             // no new process should have run
             assertEquals("10 - No new process, review disapproved", lastDto.getId(),  
                     lastDtoB.getId());
 
             // get the review
             reviewDto2 = remoteBillingProcess.getReviewDto(
                     entityId, languageId);
             
             // since the last one was disapproved, a new one has to be created
             assertNotSame("11 - New review run", reviewDto.getId(), 
                     reviewDto2.getId());
 
             // status of the review should now be generated
             configDto = remoteBillingProcess.
                     getConfigurationDto(entityId);
             assertEquals("12 - Review generated", configDto.getReviewStatus(),
                     Constants.REVIEW_STATUS_GENERATED);
             
             // run trigger, date is good, but the review is not approved
             cal.set(2006, GregorianCalendar.OCTOBER, 22); 
             remoteBillingProcess.trigger(cal.getTime());
             
             // get the review
             reviewDto = remoteBillingProcess.getReviewDto(
                     entityId, languageId);
             // the status is generated, so it should not be a new review                    
             assertEquals("13 - No new review run", reviewDto.getId(), 
                     reviewDto2.getId());
 
             // run trigger report still not approved, no process then
             cal.set(2006, GregorianCalendar.OCTOBER, 22); 
             remoteBillingProcess.trigger(cal.getTime());
                     
             // get the latest process
             lastDtoB = remoteBillingProcess.getDto(
                     remoteBillingProcess.getLast(entityId),
                     languageId);
                     
             // no new process should have run
             assertEquals("14 - No new process, review not yet approved", lastDto.getId(),  
                     lastDtoB.getId());
 
             // disapprove the review so it should run again 
             remoteBillingProcess.setReviewApproval(new Integer(1), entityId,
                     new Boolean(false));
 
             //
             //  Run the review and approve it to allow the process to run
             //              
             cal.clear();
             cal.set(2006, GregorianCalendar.OCTOBER, 26); 
             cal.add(GregorianCalendar.DATE, -4); 
             remoteBillingProcess.trigger(cal.getTime());
 
             // get the review
             reviewDto2 = remoteBillingProcess.getReviewDto(
                     entityId, languageId);
             // since the last one was disapproved, a new one has to be created
             assertFalse("14.2 - New review run", reviewDto.getId().intValue() == 
                     reviewDto2.getId().intValue());
             
             // finally, approve the review. The billing process is next
             remoteBillingProcess.setReviewApproval(new Integer(1), entityId,
                     new Boolean(true));
 
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception:" + e);
         }
     }
    
     
     public void testProcess() throws Exception {
         try {
             // get the latest process
             BillingProcessDTOEx lastDto = remoteBillingProcess.getDto(
                     remoteBillingProcess.getLast(entityId),
                     languageId);
             
             // get the review, so we can later check that what id had
             // is the same that is generated in the real process
             BillingProcessDTOEx reviewDto = remoteBillingProcess.getReviewDto(
                     entityId, languageId);
             
             // run trigger on the run date     
             remoteBillingProcess.trigger(runDate);
 
             // get the latest process
             BillingProcessDTOEx lastDtoB = remoteBillingProcess.getDto(
                     remoteBillingProcess.getLast(entityId),
                     languageId);
 
             // this is the one and only new process run
             assertFalse("15 - New Process", lastDto.getId().equals(lastDtoB.getId()));
             // initially, runs should be 1
             assertEquals("16 - Only one run", lastDtoB.getRuns().size(), 1);
 
             // check that the next billing date is updated
             BillingProcessConfigurationDTO configDto = remoteBillingProcess.
                     getConfigurationDto(entityId);
             cal.add(GregorianCalendar.MONTH, 1);
             assertTrue("17 - Next billing date for a month later", 
                     configDto.getNextRunDate().equals(Util.truncateDate(
                         cal.getTime())));
             
             // verify that what just have run, is the same that was displayed
             // in the review
             assertEquals("17.1 - Review invoices = Process invoices",
                     reviewDto.getGrandTotal().getInvoiceGenerated().intValue(),
                     lastDtoB.getGrandTotal().getInvoiceGenerated().intValue());
             
             BillingProcessRunTotalDTOEx aTotal = (BillingProcessRunTotalDTOEx)
                     reviewDto.getGrandTotal().getTotals().get(0);
             BillingProcessRunTotalDTOEx bTotal = (BillingProcessRunTotalDTOEx)
                     lastDtoB.getGrandTotal().getTotals().get(0);
             assertEquals("17.2 - Review invoiced = Process invoiced",
                     aTotal.getTotalInvoiced().floatValue(),
                     bTotal.getTotalInvoiced().floatValue(),
                     0.01F);
 
             // verify that the transition from pending unsubscription to unsubscribed worked
             assertEquals("User should stay on pending unsubscription",
                     UserDTOEx.SUBSCRIBER_PENDING_UNSUBSCRIPTION,
                     remoteUser.getUserDTOEx("pendunsus1", new Integer(1)).getSubscriptionStatusId());
             assertEquals("User should have changed to unsubscribed",
                     UserDTOEx.SUBSCRIBER_UNSUBSCRIBED,
                     remoteUser.getUserDTOEx("pendunsus2", new Integer(1)).getSubscriptionStatusId());
            
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception:" + e);
         }
     }
 
     // This should work when data of the order lines makes sense (quantity *
     // price = total).
     // Yet, the periods have to be added in this function
     public void testGeneratedInvoices() {
         try {
             Collection<InvoiceDTOEx> invoices = remoteBillingProcess.getGeneratedInvoices(
                     new Integer(35));
             // we know that only one invoice should be generated
             assertEquals("Invoices generated", 998, invoices.size());
             
             for (InvoiceDTOEx invoice : invoices) {
                 float orderTotal = 0F;
                 Vector<OrderDTO> orders = invoice.getOrders();
                 boolean isProRated = false;
                 for (OrderDTO order: orders) {
                     OrderDTO orderDto = remoteOrder.getOrderEx(order.getId(),
                             languageId);
                     orderTotal += orderDto.getTotal().floatValue();
                    if (order.getId() >= 103 && order.getId() <= 108 || order.getId() == 113) {
                     	isProRated = true;
                     }
                 }
 
                 if (!isProRated) {
 	                assertEquals("Orders total = Invoice " + invoice.getId()
 	                        + " total", orderTotal, invoice.getTotal().floatValue()
 	                        - invoice.getCarriedBalance().floatValue(), 0.005F);
                 } else {
                 	// TODO: add exact calculations for pro-rated invoices
                 }
             }
             
             // take the invoice and examine
             InvoiceDTO invoice = remoteInvoice.getInvoice(NEW_INVOICE);
             assertNotNull("Invoice should've been generated", invoice);
             
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception:" + e);
         }
     }
     
     public void testPayments() {
         try {
             BillingProcessDTOEx process = remoteBillingProcess.getDto(35, 1);
             assertNotNull("The process should be there", process);
             assertNotNull("The run should be there", process.getRuns());
             assertEquals("Only one run should be present", 1, process.getRuns().size());
             BillingProcessRunDTOEx run = (BillingProcessRunDTOEx) process.getRuns().get(0);
 
             for(int myTry = 0; myTry < 10 && run.getPaymentFinished() == null; myTry++) {
                 System.out.println("Waiting for payment processing ... " + myTry);
                 Thread.sleep(1000);
                 process = remoteBillingProcess.getDto(35, 1);
                 run = (BillingProcessRunDTOEx) process.getRuns().get(0);
             }
             
             assertNotNull("The payment processing did not run", run.getPaymentFinished());
             // we know that the only one invoice will be payed in full
             assertEquals("Invoices in the grand total", new Integer(998), process.getGrandTotal().getInvoiceGenerated());
             assertEquals("Total invoiced is consitent", ((BillingProcessRunTotalDTOEx) process.getGrandTotal().getTotals().get(0)).getTotalInvoiced(),
                     ((BillingProcessRunTotalDTOEx) process.getGrandTotal().getTotals().get(0)).getTotalPaid() + 
                     ((BillingProcessRunTotalDTOEx) process.getGrandTotal().getTotals().get(0)).getTotalNotPaid());
             InvoiceDTO invoice = remoteInvoice.getInvoice(NEW_INVOICE);
             assertEquals("Invoice is paid", new Integer(0), invoice.getToProcess());
             
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception:" + e);
         }
     }
     
     /*
      * VALIDATE ORDERS
      */
 
     public void testOrdersProcessedDate() {
         String dates[] = { 
             "2006-11-26", null, null,   // 100 - 102 
             "2006-11-01", null, null,   // 103 - 105 
             "2006-10-01", null, null,   // 106 - 108 
             null, "2006-11-25", null,   // 109 - 111 
             "2006-11-15", null,    // 112 - 113 
         }; 
         
         try {
             for (int f = 100; f < dates.length; f++) {
                 OrderDTO order = remoteOrder.getOrder(f);
                 
                 if (order.getNextBillableDay() != null) {
                            
                     if (dates[f] == null ){
                         assertNull("Order " + order.getId(),order.getNextBillableDay());
                     } else {
                         assertEquals("Order " + order.getId(), parseDate(dates[f]),
                                 order.getNextBillableDay());
                     } 
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception:" + e);
         }
     }
     
     public void testOrdersFlaggedOut() {
         int orders[] = { 102, 104, 105, 107, 108, 109, 113 }; 
 
         try {
             for (int f = 0; f < orders.length; f++) {
                 OrderDTO order = remoteOrder.getOrder(new Integer(orders[f]));
                 assertEquals("Order " + order.getId(), order.getStatusId(), 
                         Constants.ORDER_STATUS_FINISHED); 
             }
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception:" + e);
         }
             
     }
     
     public void testOrdersStillIn() {
         int orders[] = { 100, 101, 103, 106, 110, 111, 112}; 
 
         try {
             for (int f = 0; f < orders.length; f++) {
                 OrderDTO order = remoteOrder.getOrder(new Integer(orders[f]));
                 assertEquals("Order " + order.getId(), order.getStatusId(), 
                         Constants.ORDER_STATUS_ACTIVE); 
             }
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception:" + e);
         }
            
     }
     
     public void testPeriodsBilled() {
         String dateRanges[][] = {
             { "2006-10-26", "2006-11-26", "1"  }, // 100
             { "2006-10-01", "2006-11-01", "1"  }, // 102
             { "2006-10-16", "2006-12-01", "2"  }, // 103
             { "2006-10-15", "2006-11-30", "2"  }, // 104
             { "2006-09-05", "2006-11-25", "3"  }, // 105
             { "2006-09-03", "2006-11-01", "2"  }, // 106
             { "2006-09-30", "2006-10-29", "2"  }, // 107
             { "2006-08-10", "2006-10-20", "3"  }, // 108
             { "2006-10-25", "2006-11-25", "1"  }, // 110
             { "2006-10-15", "2006-11-15", "1"  }, // 112
             { "2006-10-15", "2006-11-05", "1"  }, // 113
         };
         
         int orders[] = { 100, 102, 103, 104, 105, 106, 107, 108, 110, 112, 113 };
        
         try {
             // get the latest process
             BillingProcessDTOEx lastDto = remoteBillingProcess.getDto(
                     remoteBillingProcess.getLast(entityId),
                     languageId);
             
             for (int f = 0; f < orders.length; f++) {
                 OrderDTO order = remoteOrder.getOrderEx(
                         new Integer(orders[f]), languageId);
                 Date from = parseDate(dateRanges[f][0]);
                 Date to = parseDate(dateRanges[f][1]);
                 Integer number = Integer.valueOf(dateRanges[f][2]);
                 
                 OrderProcessDTO period = (OrderProcessDTO)
                         order.getPeriods().toArray()[0];
                 assertTrue("(from) Order " + order.getId(),period.getPeriodStart().compareTo(
                         from) == 0);
                 assertTrue("(to) Order " + order.getId(),period.getPeriodEnd().compareTo(to) == 0);
                 assertEquals("(number) Order " + order.getId(), number, 
                         period.getPeriodsIncluded());
  
                 OrderProcessDTO process = (OrderProcessDTO)
                         order.getOrderProcesses().toArray()[0];
                 assertEquals("(process) Order " + order.getId(),lastDto.getId().intValue(), 
                         process.getBillingProcess().getId()); 
             }
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception:" + e);
         }
         
     }
     
     public void testExcluded() {
         int orders[] = {101, 109, 111};
         try {
             for (int f = 0; f < orders.length; f++) {
                 OrderDTO order = remoteOrder.getOrderEx(
                         new Integer(orders[f]), languageId);
                 
                 assertTrue("1 - Order " + order.getId(),order.getPeriods().isEmpty());
                 assertTrue("2 - Order " + order.getId(),order.getOrderProcesses().isEmpty());
             }
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception:" + e);
         }
 
     }
     
     /*
     public void testInvoicesFlaggedOut() {
         int invoices[] = { 3, 4, 6, 7, 8, 9, 10, 11, 13, 14, 17, 18, 19, 
                 20, 21, 22, 23 };
 
         try {
             for (int f = 0; f < invoices.length; f++) {
                 InvoiceDTO invoice = remoteInvoice.getInvoice(
                         new Integer(invoices[f]));
                 assertEquals("Invoice " + invoice.getId(), 
                         new Integer(0), invoice.getToProcess()); 
             }
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception:" + e);
         }
             
     }
 
     public void testInvoicesStillIn() {
         int invoices[] = { 1, 2, 5, 12, 15, 16 };
 
         try {
             for (int f = 0; f < invoices.length; f++) {
                 InvoiceDTO invoice = remoteInvoice.getInvoice(
                         new Integer(invoices[f]));
                 assertEquals("Invoice " + invoice.getId(), 
                         invoice.getToProcess(), new Integer(1)); 
             }
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception:" + e);
         }
             
     }
     
     public void testInvoicesDelegated() {
         int invoices[] = { 4, 6, 8, 9, 10, 11, 17, 18, 19, 20, 21, 22 };
 
         try {
             for (int f = 0; f < invoices.length; f++) {
                 InvoiceDTOEx invoice = remoteInvoice.getInvoiceEx(
                         new Integer(invoices[f]), new Integer(1));
                 assertNotNull("Invoice " + invoice.getId(), 
                         invoice.getDelegatedInvoiceId()); 
             }
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception:" + e);
         }
          
     }
 
     public void testInvoicesNotDelegated() {
         int invoices[] = { 1,2,5,12,13,15,16 };
 
         try {
             for (int f = 0; f < invoices.length; f++) {
                 InvoiceDTOEx invoice = remoteInvoice.getInvoiceEx(
                         new Integer(invoices[f]), new Integer(1));
                 assertNull("Invoice " + invoice.getId(), 
                         invoice.getDelegatedInvoiceId()); 
             }
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception:" + e);
         }
          
     }
 
 
     public void testPayments() {
         try {
             
             Collection invoices = remoteBillingProcess.getGeneratedInvoices(
                 remoteBillingProcess.getLast(entityId));
             
             for (Iterator it = invoices.iterator(); it.hasNext();) {
                 InvoiceDTOEx invoice = (InvoiceDTOEx) it.next();
                 
                 if (invoice.getUserId().intValue() == 9) { // Chisky Peters has a cc to pay with
                     assertFalse("Invoice " + invoice.getId(), invoice.getPaymentMap()
                             .isEmpty());
                     
                     PaymentDTO payment = remotePayment.getPayment(
                             (Integer) invoice.getPaymentMap().get(0), languageId); 
 
                     if (payment.getResultId().equals(Constants.RESULT_OK)) {
                         assertEquals("(to_process) Invoice " + invoice.getId(),
                             new Integer(0), invoice.getToProcess()); 
                     } else {
                         assertEquals("(to_process) Invoice " + invoice.getId(),
                             new Integer(1), invoice.getToProcess()); 
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception:" + e);
         }
     }
 
 
    
     public void testTotals() {
         try {
             // get the latest process
             BillingProcessDTOEx lastDto = remoteBillingProcess.getDto(
                     remoteBillingProcess.getLast(entityId),
                     languageId);
 
             // get the grand total for this process
             BillingProcessRunDTOEx grandTotal = lastDto.getGrandTotal();
             
             // add up all the totals and compare to the grand total
             // go over all the runs
             float totalInvoicedSum = 0;
             for (int f = 0; f < lastDto.getRuns().size(); f++) {
                 BillingProcessRunDTOEx runDto = (BillingProcessRunDTOEx) lastDto.
                         getRuns().get(f);
                 // go over the totals
                 for (int ff = 0; ff < runDto.getTotals().size(); ff++) {
                     BillingProcessRunTotalDTOEx totalDto = 
                             (BillingProcessRunTotalDTOEx) runDto.getTotals()
                                 .get(ff);
                     if (totalDto.getCurrencyId().intValue() == 1) {
                         totalInvoicedSum += totalDto.getTotalInvoiced().
                             floatValue();
                     }
                 }
             }
             
             // verify that my invoiced total is the same as the grandtotal's
             for (int f = 0; f < grandTotal.getTotals().size(); f++) {
                 BillingProcessRunTotalDTOEx totalDto = 
                         (BillingProcessRunTotalDTOEx) grandTotal.getTotals()
                             .get(f);
                 if (totalDto.getCurrencyId().intValue() == 1) {
                     assertEquals("Total invoiced sum", 
                             totalDto.getTotalInvoiced().floatValue(),
                             totalInvoicedSum, 0.005F);        
                     break;
                 }
             }
             
             // now verify that the numbers of total/pm are consitent
             // get the first run of this process
             BillingProcessRunDTOEx runDto = (BillingProcessRunDTOEx) lastDto.
                     getRuns().get(0);
             // get the total for the currency 1
             BillingProcessRunTotalDTOEx totalDto = null;
             for (int f = 0; f < runDto.getTotals().size(); f++) {
                 totalDto = (BillingProcessRunTotalDTOEx) runDto.getTotals()
                         .get(f);
                 if (totalDto.getCurrencyId().intValue() == 1) {
                     break;
                 }
             }
             
             assertEquals("Total numbers", totalDto.getTotalInvoiced().floatValue(),
                     totalDto.getTotalNotPaid().floatValue() +
                     totalDto.getTotalPaid().floatValue(), 0.01F);
             
             // get the pms for this total
             float totalpms = 0;
             for (Enumeration en = totalDto.getPmTotals().elements(); 
                     en.hasMoreElements();) {
                 totalpms += ((Float) en.nextElement()).floatValue();
             }
             assertEquals("Pms total", totalDto.getTotalPaid().floatValue(),
                     totalpms, 0.01F);
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception:" + e);
         }
     }
     
     public void testAgeing() {
         try {
             Integer userId = new Integer(17);
             
             // the grace period should keep this user active
             cal.clear();
             cal.set(2003, GregorianCalendar.MAY, 6);
             remoteBillingProcess.reviewUsersStatus(cal.getTime());
             UserDTOEx user = remoteUser.getUserDTOEx(userId);
             assertEquals("Grace period", UserDTOEx.STATUS_ACTIVE,
                     user.getStatusId());
                     
             // when the grace over, she should be warned
             cal.set(2003, GregorianCalendar.MAY, 7);
             remoteBillingProcess.reviewUsersStatus(cal.getTime());
             user = remoteUser.getUserDTOEx(userId);
             assertEquals("to overdue", UserDTOEx.STATUS_ACTIVE.intValue() + 1,
                     user.getStatusId().intValue());
 
             // two day after, the status should be the same
             cal.set(2003, GregorianCalendar.MAY, 9);
             remoteBillingProcess.reviewUsersStatus(cal.getTime());
             user = remoteUser.getUserDTOEx(userId);
             assertEquals("still overdue", UserDTOEx.STATUS_ACTIVE.intValue() + 1,
                     user.getStatusId().intValue());
 
             // after three days of the warning, fire the next one
             cal.set(2003, GregorianCalendar.MAY, 10);
             remoteBillingProcess.reviewUsersStatus(cal.getTime());
             user = remoteUser.getUserDTOEx(userId);
             assertEquals("to overdue 2", UserDTOEx.STATUS_ACTIVE.intValue() + 2,
                     user.getStatusId().intValue());
 
             // the next day it goes to suspended
             cal.set(2003, GregorianCalendar.MAY, 11);
             remoteBillingProcess.reviewUsersStatus(cal.getTime());
             user = remoteUser.getUserDTOEx(userId);
             assertEquals("to suspended", UserDTOEx.STATUS_ACTIVE.intValue() + 4,
                     user.getStatusId().intValue());
 
             // two days for suspended 3
             cal.set(2003, GregorianCalendar.MAY, 13);
             remoteBillingProcess.reviewUsersStatus(cal.getTime());
             user = remoteUser.getUserDTOEx(userId);
             assertEquals("to suspended 3", UserDTOEx.STATUS_ACTIVE.intValue() + 6,
                     user.getStatusId().intValue());
 
             // two days for suspended 3
             cal.add(GregorianCalendar.DATE, 30);
             remoteBillingProcess.reviewUsersStatus(cal.getTime());
             user = remoteUser.getUserDTOEx(userId);
             assertEquals("deleted", new Integer(1), user.getDeleted());
             
         } catch (Exception e) {
             e.printStackTrace();
             fail("Exception:" + e);
         }
     }
 
 
     public void testQuicky() {
         try {
             cal.clear();
             cal.set(2006, GregorianCalendar.AUGUST, 3); 
             remoteBillingProcess.trigger(cal.getTime());
             //remoteBillingProcess.reviewUsersStatus(cal.getTime());
         } catch (Exception e) {
             fail(e.getMessage());
         }
     }
 */
     public static Date parseDate(String str) throws Exception{
         if (str == null ) {
             return null;
         }
         
         if ( str.length() != 10 || str.charAt(4) != '-' || str.charAt(7) != '-') {
             throw new Exception("Can't parse " + str);
            
         }
         
         try {
             int year = Integer.valueOf(str.substring(0,4)).intValue();
             int month = Integer.valueOf(str.substring(5,7)).intValue();
             int day = Integer.valueOf(str.substring(8,10)).intValue();
         
             GregorianCalendar cal = new GregorianCalendar(year, month - 1, day);
         
             return cal.getTime();
         } catch (Exception e) {
             throw new Exception("Can't parse " + str);
         }
     }
 
 }
