 package com.mycompany;
 
 import static org.junit.Assert.*;
 
 import java.math.BigDecimal;
 
 import org.easymock.EasyMock;
 import org.junit.Ignore;
 import org.junit.Test;
 
 public class AccountUpdaterTest {
 
 	@Test
 	public void testHappyPath() {
 		// write a test to be sure that the price returned from the shipping service 
		// is recorded in the OrderResult
 	}
 
 	@Test
 	public void simulateFailure() {
 		// now adjust your stub implementation of the interfaces so that it
 		// throws one of the exceptions from either ICreditCardService or IShippingService.
 		// be sure to change it in such a way that testHappyPath still passes!
 	}
 
 	@Test
 	public void testConfusion() {
 		// now introduce a bug into your code -- pass the accountID to the
 		// productID. Does your test break? If not, change your stub to
 		// check the passed-in value.
 	}
 	
 	@Test
 	@Ignore
 	public void testConfusionWithDynamicMock() throws Exception {
 		// test the same thing as above with a dynamic mock.
 		// here's an example of creating the credit card service. You'll need
 		// another thing just like this for
 		// the account update. These mocks will replace the stub code from the
 		// happyPathTest
 		IShippingService mockShippingService = EasyMock
 				.createMock(IShippingService.class);
 
 		// some expectations
 		int productId = 424242;
 		int accountId = 575757;
 		BigDecimal amount = new BigDecimal(199.99);
 		EasyMock.expect(mockShippingService.lookupPrice(productId)).andReturn(
 				amount);
 		
 		// Even though it's not surrounded with a call to EasyMock.expect(), this
 		// next method also creates an exception!
 		mockShippingService.shipOrder(accountId, productId); 
 		EasyMock.replay(mockShippingService); // call this when you're all done
 											  // setting up expectations.
 
 		
 		//Other mocks, and calls to the code under test go here.
 		
 		
 		
 		// this should be the last line.
 		EasyMock.verify(mockShippingService);
 	}
 
 	@Test
 	@Ignore
 	public void testTwoPhaseCommitCommitTransactionCalled() throws Exception {
 		// STOP -- check with instructor before beginning this test. A+ students
 		// only!
 	}
 }
