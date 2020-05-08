 package com.mycompany;
 
 /*
  * In our hypothetical example, we have two pre-existing services, defined by the interfaces ICreditCardService and IShippingService.
  * One service is the credit card service that withdraws money from customer accounts.  The second service looks up prices and 
  * ships products.
  * 
  * This class should be the "glue" between those already existing services, returning the OrderResult bean.
  */
 public class OrderPlacement {
 	//remember, TDD!  Don't enter anything here unless you have a test for it.
 	
 	public OrderPlacement(IShippingService shippingService,
 			ICreditCardService creditCardService) {
 	}
 
 	public OrderResult placeOrder(int accountId, String creditCardNumber, int productId) {
 		return null; 
 	}
 }
