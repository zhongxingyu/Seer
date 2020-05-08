 package nl.coralic.beta.sms.test;
 
 public class S102_Beta_SMSActivityTest extends Beta_SMSActivityTest
 {
     public void testSaldoShown() throws InterruptedException
     {
	solo.waitForText("Saldo:");
     }
 }
