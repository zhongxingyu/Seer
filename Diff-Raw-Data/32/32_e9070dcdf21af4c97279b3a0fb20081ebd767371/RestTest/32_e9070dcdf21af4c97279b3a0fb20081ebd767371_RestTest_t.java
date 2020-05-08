 package org.codehaus.xfire.client.http;
 
 import junit.framework.TestCase;
 
 /**
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  * @since Oct 26, 2004
  */
 public class RestTest
     extends TestCase
 {
     public void testREST()
         throws Exception
     {
         SinkHandler handler = new SinkHandler();
         RestTestClient client = new RestTestClient( handler );
 
         client.invoke();
         
         String res = handler.getResponse();
         System.out.println(res);
         assertNotNull(res);
         assertTrue( res.indexOf("echo") != -1 );
     }
    
    public void testAmazon()
        throws Exception
    {
        SinkHandler handler = new SinkHandler();
        String url = "http://webservices.amazon.com/onca/xml?Service=AWSECommerceService"
            + "&SubscriptionId=1E5AY4ZG53H4AMC8QH82"
            + "&Operation=ItemSearch"
            + "&SearchIndex=Books"
            + "&Keywords=SOAP";
        
        RestHttpClient client = new RestHttpClient( handler, url );

        client.invoke();
        
        String res = handler.getResponse();
        System.out.println(res);
        assertNotNull(res);
        assertTrue( res.indexOf("ItemSearchResponse") != -1 );
    }
 }
