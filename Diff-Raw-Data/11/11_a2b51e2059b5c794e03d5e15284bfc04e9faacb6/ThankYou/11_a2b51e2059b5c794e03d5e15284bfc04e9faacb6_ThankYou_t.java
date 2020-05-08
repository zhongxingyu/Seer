 package org.bloodtorrent.functionaltests.pages;
 
 import net.sf.sahi.client.Browser;
 
 public class ThankYou extends BasePage {
 
 	public ThankYou(Browser browser){
 		super(browser);
 	}
 
 	public String getMessageForBloodRequest() {
		return browser.heading2("title").getText();
 	}
 
 	public String getMessageForRegistering() {
 		//TODO: Refactor this to remove index to identify the html element.
 		return browser.heading2("title").text();
 	}
 }
