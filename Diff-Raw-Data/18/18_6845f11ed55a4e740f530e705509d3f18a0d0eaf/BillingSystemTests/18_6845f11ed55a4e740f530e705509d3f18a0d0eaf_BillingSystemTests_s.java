 package com.acmetelecom;
 
import org.mockito.Mockito;
 import org.testng.annotations.BeforeTest;
 import org.testng.annotations.Test;
 
 /**
  * Set of tests for the Billing System. Uses Mocking to mock the billing data.
  */
 public class BillingSystemTests {
 
     private BillingSystem billingSystem;
 
     @BeforeTest
     public void setupBillingSystem() {
         billingSystem = new BillingSystem();
     }
 
 
     @Test
     public void startingCallsTest() {
         String caller = "00000000";
         String callee = "00000001";
 
         billingSystem.callInitiated(caller, callee);
     }
 
     @Test
     public void endCallsTest() {
         String caller = "00000000";
         String callee = "00000001";
 
         billingSystem.callInitiated(caller, callee);
 
         billingSystem.callCompleted(caller, callee);
 
     }
 
     @Test
     public void produceBill() {
 
         String caller = "00000000";
         String callee = "00000001";
 
         billingSystem.callInitiated(caller, callee);
 
         billingSystem.callCompleted(caller, callee);
 
         billingSystem.createCustomerBills();
         // TODO: to get the output, we need to get HtmlPrinter.getInstance() to make a mock or refactor everything
     }
 
 }
