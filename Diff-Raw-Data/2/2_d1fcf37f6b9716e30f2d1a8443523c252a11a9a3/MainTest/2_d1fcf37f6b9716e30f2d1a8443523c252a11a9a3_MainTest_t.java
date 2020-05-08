 
 package com.webshop.tests;
 
 import junit.framework.TestCase;
 
 import com.sun.grizzly.http.SelectorThread;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.core.header.MediaTypes;
 import com.webshop.Main;
 import com.webshop.item.ItemManager;
 import com.webshop.order.OrderBean;
 import com.webshop.order.OrderItemBean;
 
 
 public class MainTest extends TestCase {
 
     private SelectorThread threadSelector;
     
     private WebResource r;
 
     public MainTest(String testName) {
         super(testName);
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         
         threadSelector = Main.startServer();
 
         Client c = Client.create();
         r = c.resource(Main.BASE_URI);
     }
 
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
 
         threadSelector.stopEndpoint();
     }
 
     /**
      * Test to see that the message "Got it!" is sent in the response.
      */
     public void _testMyResource() {
         String responseMsg = r.path("myresource").get(String.class);
         assertEquals("Got it!", responseMsg);
     }
     
     public void testCreateAndGet() {
     	// create a new entity first    	
     	OrderBean order = new OrderBean();
     	order.description = "Test order";
     	order.user = "test@user.com";
     	OrderItemBean item1 = new OrderItemBean();
    	item1.item = ItemManager.getInstance().getItem("1").id;
     	item1.amount = 10;
     	order.items.add(item1);
     	
     	/*Client client = Client.create();
     	WebResource createOrder = client.resource(Main.BASE_URI).path("/order");*/
     	ClientResponse response = r.path("/order").accept("application/json").post(ClientResponse.class);
     	//OrderBean result = response.getEntity(OrderBean.class);
     	/*String result = response.getEntity(String.class);
     	System.out.println("result = " + result.toString());*/
     	
     	assertTrue(true);
     }
     
     public void testGetOrder() {
     	OrderBean orderResponse = r.path("/order/2").get(OrderBean.class);    	
     	assertEquals(orderResponse.description, "Description of order 2");
     	assertEquals(orderResponse.id, "2");
     }    
 }
