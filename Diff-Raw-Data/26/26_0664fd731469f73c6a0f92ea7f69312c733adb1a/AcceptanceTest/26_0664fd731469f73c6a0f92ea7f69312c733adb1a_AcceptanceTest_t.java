 /****************************************************************************
  * Copyright (c) 2010 Giorgio Sironi. All rights reserved.
  * This program and the accompanying materials are made available under 
  * the terms of the Eclipse Public License v1.0 which accompanies this 
  * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  ****************************************************************************/
 package it.polimi.chansonnier.test;
 
 
 import com.meterware.httpunit.PostMethodWebRequest;
 import com.meterware.httpunit.WebConversation;
 import com.meterware.httpunit.WebRequest;
 import com.meterware.httpunit.WebResponse;
 import com.thoughtworks.selenium.Selenium;
 
 public abstract class AcceptanceTest extends FunctionalTest {	
 	protected WrappableSeleneseTestCase wrapped;
 	protected Selenium selenium;
 	
 	public void setUp() throws Exception {
 		super.setUp();
 		wrapped = new WrappableSeleneseTestCase();
 		wrapped.setUp("http://localhost:8080/", "*firefox");
 		selenium = wrapped.getSelenium();
 	}
 	
 	public void tearDown() throws Exception {
 		wrapped.tearDown();
 		super.tearDown();
 	}
 	
 	protected String getPipelineName() {
 		return "AddPipeline";
 	}
 	
 	protected WebResponse addVideoLink(String link) throws Exception {
 		WebConversation wc = new WebConversation();
 		PostMethodWebRequest	req = new PostMethodWebRequest( "http://localhost:8080/chansonnier/add" );
 		req.setParameter("link", link);
 		WebResponse resp = wc.getResponse(req);
 		return resp;
 	}
 
 	protected WebResponse assertWebPageContains(WebRequest req, String text, int timeout) throws Exception {
         int tries = timeout / 10000;
         for (int i = 0; i < tries; i++) {
             System.out.println("Try " + i + " for text ''" + text + "'");
             WebConversation wc = new WebConversation();
             WebResponse   resp = wc.getResponse( req );
             System.out.println(resp.getText());
             if (resp.getText().contains(text)) {
                 assertTrue(true);
                 return resp;
             }
             Thread.sleep(10000);
         }
         fail("After " + timeout + "milliseconds of waiting, the web page does not contain the prescribed text (" + text + ").");
         return null;
     }
	
	public void waitForPageToLoad() throws InterruptedException {
		Thread.sleep(5000);
	}
 }
