 package net.petersson.googl;
 
 import junit.framework.TestCase;
 
import java.util.logging.Level;
import java.util.logging.Logger;

 public class GooGlResponseTest extends TestCase {
 
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Logger.getLogger("").setLevel(Level.OFF);
	}

 	public void test_GooGlResponse() throws GooGlException {
 
 		String response = "{ \"kind\": \"urlshortener#url\", \"id\": \"http://goo.gl/fbsS\", \"longUrl\": \"http://www.google.com/\" }";
 
 		GooGlResponse gooGlResponse = new GooGlResponse(response);
 
 		assertEquals("http://goo.gl/fbsS", gooGlResponse.getShortUrl());
 		assertEquals("http://www.google.com/", gooGlResponse.getLongUrl());
 	}
 
 	public void test_GooGlResponse_corruptJson() {
 
 		String invalidJsonResponse = " \"kind\": \"urlshortener#url\", \"id\": \"http://goo.gl/fbsS\", \"longUrl\": \"http://www.google.com/\" }";
 
 		try {
 			new GooGlResponse(invalidJsonResponse);
 			fail("Expected GooGlException");
 		} catch (GooGlException e) {

 		}
 	}
 
 	public void test_GooGlResponse_corruptJson_getLongUrl() {
 
 		String invalidJsonResponse = "{ \"kind\": \"urlshortener#url\", \"id\": \"http://goo.gl/fbsS\", \"looongUrl\": \"http://www.google.com/\" }";
 
 		try {
 			GooGlResponse gooGlResponse = new GooGlResponse(invalidJsonResponse);
 			gooGlResponse.getLongUrl();
 			fail("Expected GooGlException on getLongUrl()");
 		} catch (GooGlException e) {
 			//
 		}
 	}
 
 	public void test_GooGlResponse_corruptJson_getShortUrl() {
 
 		String invalidJsonResponse = "{ \"kind\": \"urlshortener#url\", \"foo\": \"http://goo.gl/fbsS\", \"longUrl\": \"http://www.google.com/\" }";
 
 		try {
 			GooGlResponse gooGlResponse = new GooGlResponse(invalidJsonResponse);
 			gooGlResponse.getShortUrl();
 			fail("Expected GooGlException on getLongUrl()");
 		} catch (GooGlException e) {

 		}
 	}
 }
