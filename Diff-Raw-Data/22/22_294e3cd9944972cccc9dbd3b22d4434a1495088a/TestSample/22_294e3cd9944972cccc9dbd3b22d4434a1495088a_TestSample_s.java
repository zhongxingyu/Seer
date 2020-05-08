 
 import net.sourceforge.jwebunit.junit.WebTester;
 import net.sourceforge.jwebunit.util.TestingEngineRegistry;
 
 import org.junit.*;
 import static net.sourceforge.jwebunit.junit.JWebUnit.*;
 
 public class TestSample {
 
 	  private WebTester tester;
 
   @Before
   public void prepare() {
   	setTestingEngineKey(TestingEngineRegistry.TESTING_ENGINE_HTMLUNIT);    // use HtmlUnit
   	
   	System.setProperty("javax.net.ssl.trustStore", System.getenv("TOMCAT_HOME")+"/conf/keystore.jks");
 	System.setProperty("javax.net.ssl.trustStorePassword", "asdasd");
   	
       tester = new WebTester();
       tester.setBaseUrl("https://localhost:8443/CDStore");
   }
   
   @Test
   public void testRegistrationValid() {
       tester.beginAt("index.jsp");
       tester.clickLinkWithExactText("Sign Up!");
       tester.assertFormPresent();
      tester.setTextField("userName", "test");
       tester.setTextField("userPassword", "test");
       tester.setTextField("verifyPassword", "test");
       tester.setTextField("firstName", "Albert");
       tester.setTextField("lastName", "Smith");
       tester.setTextField("street", "123 Test Street");
       tester.setTextField("city", "Junit");
       tester.setTextField("province", "Website");
       tester.setTextField("postalcode", "N7D5F7");
       tester.setTextField("country", "CDStore");
       tester.setTextField("phonenumber", "6131234567");    	
       tester.submit();
      tester.assertFormNotPresent();
      tester.assertLinkPresentWithExactText("Add to Cart");
   }
   
   @Test
   public void testLoginValid() {
       tester.beginAt("index.jsp");
       tester.assertFormPresent();
       tester.setTextField("userName", "test");
       tester.setTextField("userPassword", "test");
       tester.submit();
       tester.assertFormNotPresent();
       tester.assertLinkPresentWithExactText("Add to Cart");
   }
   
   
   @Test
   public void testLoginMissing1() {
       tester.beginAt("index.jsp");
       tester.assertFormPresent();
       tester.setTextField("userName", "test");
       tester.submit();
       tester.assertFormPresent();
       tester.assertLinkPresentWithExactText("Sign Up!");
   }
   
   @Test
   public void testLoginMissing2() {
       tester.beginAt("index.jsp"); 
       tester.assertFormPresent();
       tester.setTextField("userPassword", "test");
       tester.submit();
       tester.assertFormPresent();
       tester.assertLinkPresentWithExactText("Sign Up!");
   }
   
   @Test
   public void testLoginMissing3() {
       tester.beginAt("index.jsp"); 
       tester.assertFormPresent();
       tester.submit();
       tester.assertFormPresent();
       tester.assertLinkPresentWithExactText("Sign Up!");
   }
   
  
  
   @Test
   public void testRegistrationMissing() {
   	  tester.beginAt("index.jsp"); 
   	  tester.clickLinkWithExactText("Sign Up!");
       tester.assertFormPresent();
      tester.setTextField("userName", "test2");
       tester.setTextField("userPassword", "test456");
       tester.setTextField("verifyPassword", "test456");
       tester.setTextField("firstName", "Albert");
       tester.setTextField("lastName", "Smith");
       tester.setTextField("street", "456 Test Street");
       tester.setTextField("province", "Website");
       tester.setTextField("postalcode", "N7D5F7");
       tester.setTextField("country", "CDStore");
       tester.setTextField("phonenumber", "6131234567");    	
       tester.submit();
      tester.assertLinkPresentWithExactText("Add to Cart");
   }
   
   @Test
   public void testCDStoreCategories() {
   	  tester.beginAt("index.jsp"); 
       tester.setTextField("userName", "test");
       tester.setTextField("userPassword", "test");
       tester.submit();
       tester.clickLinkWithExactText("COUNTRY");
       tester.assertLinkPresentWithExactText("Add to Cart");
   }
   
   
   @Test
   public void testCartAdd() {
   	  tester.beginAt("index.jsp");
       tester.setTextField("userName", "test");
       tester.setTextField("userPassword", "test");
       tester.submit();
       tester.clickLinkWithExactText("Add to Cart");
       tester.assertLinkPresentWithExactText("Checkout");
   }
   
   @Test
   public void testCartUpdate() {
   	  tester.beginAt("index.jsp"); 
       tester.setTextField("userName", "test");
       tester.setTextField("userPassword", "test");
       tester.submit();
       tester.clickLinkWithExactText("Add to Cart");
       tester.setTextField("newQuantity", "0");
       tester.submit("update");
       tester.assertTextPresent("Your shopping cart is empty.");
   }
   
   @Test
   public void testSubmitOrderValid() {
   	  tester.beginAt("index.jsp"); 
   	  tester.setTextField("userName", "test");
       tester.setTextField("userPassword", "test");
       tester.submit();
       tester.clickLinkWithExactText("Add to Cart");
       tester.clickLinkWithExactText("Checkout");
       tester.assertTextPresent("Grand Total");
       tester.submit();
       tester.assertTextPresent("Order Successfully Completed ");
   }
 
   
   @Test
   public void testRejectOrder() {
   
   	  tester.beginAt("index.jsp");
       tester.setTextField("userName", "test");
       tester.setTextField("userPassword", "test");
       tester.submit();
       tester.clickLinkWithExactText("Add to Cart");
       tester.clickLinkWithExactText("Checkout");
       tester.assertTextPresent("Grand Total");
       tester.setTextField("ccNumber", "12345678910111213");
       tester.setTextField("secCode", "123");
       tester.submit();
       
       tester.assertTextPresent("Order Successfully Completed ");
       
       //tester.beginAt("index.jsp");
       tester.clickLinkWithExactText("Return to CD Store");
       tester.clickLinkWithExactText("Add to Cart");
       tester.clickLinkWithExactText("Checkout");
       tester.assertTextPresent("Grand Total");
       tester.setTextField("ccNumber", "12345678910111213");
       tester.setTextField("secCode", "123");
       tester.submit();
       
       tester.assertTextPresent("Order Successfully Completed ");
       
       //tester.beginAt("index.jsp");
       tester.clickLinkWithExactText("Return to CD Store");
       tester.clickLinkWithExactText("Add to Cart");
       tester.clickLinkWithExactText("Checkout");
       tester.assertTextPresent("Grand Total");
       tester.setTextField("ccNumber", "12345678910111213");
       tester.setTextField("secCode", "123");
       tester.submit();
       
       tester.assertTextPresent("Order Successfully Completed ");
       		
       //tester.beginAt("index.jsp");
       tester.clickLinkWithExactText("Return to CD Store");
       tester.clickLinkWithExactText("Add to Cart");
       tester.clickLinkWithExactText("Checkout");
       tester.assertTextPresent("Grand Total");
       tester.setTextField("ccNumber", "12345678910111213");
       tester.setTextField("secCode", "123");
       tester.submit();
       
       tester.assertTextPresent("Order Successfully Completed ");
       
       //tester.beginAt("index.jsp");
       tester.clickLinkWithExactText("Return to CD Store");
       tester.clickLinkWithExactText("Add to Cart");
       tester.clickLinkWithExactText("Checkout");
       tester.assertTextPresent("Grand Total");
       tester.setTextField("ccNumber", "12345678910111213");
       tester.setTextField("secCode", "123");
       tester.submit();
    
       tester.assertTextPresent("Every fifth request is refused!!!");
   }
   
   @Test
   public void testCancelOrder() {
   	
   	tester.beginAt("index.jsp"); 
     tester.setTextField("userName", "test");
     tester.setTextField("userPassword", "test");
     tester.submit();
     tester.clickLinkWithExactText("Add to Cart");
     tester.clickLinkWithExactText("Checkout");
     tester.assertTextPresent("Grand Total");
     tester.setTextField("ccNumber", "12345678910111213");
     tester.setTextField("secCode", "123");
     tester.clickLinkWithExactText("Cancel Order");
 	tester.assertTextNotPresent("Grand Total");
 	tester.assertTextNotPresent("There was a problem processing your order! Please contact a CD Store representative.");
 	tester.assertTextNotPresent("Your order was processed successfully!");
 
   }
 
 }
