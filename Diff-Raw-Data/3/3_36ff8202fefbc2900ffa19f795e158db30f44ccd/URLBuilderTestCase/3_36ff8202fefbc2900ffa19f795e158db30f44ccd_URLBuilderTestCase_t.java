 package org.touge.restclient.test;
 
 import junit.framework.TestCase;
 
 import org.touge.restclient.RestClient;
 import org.touge.restclient.RestClient.URLBuilder;
 
 public class URLBuilderTestCase extends TestCase {
 
 	public void testURLBuilder() {
 		RestClient restClient = new RestClient();
 		
 		//This URLBuilder builds https://citibank.com/secureme/halp	
 		String url = restClient.buildURL("htTPS://citibank.com/secureme/").append("/halp").toString();
 		assertTrue(url.equals("https://citibank.com/secureme/halp"));
 		
 		// Builds http://yahoo.com/a/mystore/toodles?index=5
 		url = restClient.buildURL("yahoo.com")
 										.append("a")
 										.append("mystore/")
 										.append("toodles?index=5").toString();
 		assertTrue(url.equals("http://yahoo.com/a/mystore/toodles?index=5"));
 				
 		// Builds http://me.com/you/andi/like/each/ohter
 		url =
 				restClient.buildURL("me.com/")
 					.append("/you/")
 					.append("/andi/")
 					.append("like/each/ohter/").toString();
 		assertTrue(url.equals("http://me.com/you/andi/like/each/ohter"));
 		
 		// Builds https://myhost.com/first/second/third/forth/fith/mypage.asp?i=1&b=2&c=3
 		url = restClient.buildURL(
 						"myhost.com", 
 						"first/", 
 						"//second", 
 						"third/forth/fith", 
 						"mypage.asp?i=1&b=2&c=3").setHttps(true).toString();
 		assertTrue(url.equals("https://myhost.com/first/second/third/forth/fith/mypage.asp?i=1&b=2&c=3"));
 		
 		// Create child URLs from base URLs
 		URLBuilder origurl = restClient.buildURL("myhost.net/","/homepage");
 		URLBuilder newurl = origurl
 			.copy()
 			.append("asdf/adf/reqotwoetiywer")
 			.setHttps(true);
 						
 		// Original URL: http://myhost.net/homepage
 		assertTrue(origurl.toString().equals("http://myhost.net/homepage"));
 		// Child URL: https://myhost.net/homepage/asdf/adf/reqotwoetiywer		
 		assertTrue(newurl.toString().equals("https://myhost.net/homepage/asdf/adf/reqotwoetiywer"));
 		
 		URLBuilder purl = restClient.buildURL("myhost.net/","/homepage");
 		
 		purl.addParameter("p1", "v1");
 		purl.addParameter("p1", "v1-2");
 		purl.addParameter("p2", "v2");
 		
		System.out.println(purl.toString());
		assertTrue(purl.toString().equals("http://myhost.net/homepage?p1=v1&p1=v1-2&p2=v2"));		                                   
 	}
 }
