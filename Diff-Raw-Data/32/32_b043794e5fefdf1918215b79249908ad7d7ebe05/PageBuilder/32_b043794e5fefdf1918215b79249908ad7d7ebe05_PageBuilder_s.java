 package com.reimDev.reimWeb.client.ui.pages.util;
 
 import com.google.gwt.user.client.ui.RootPanel;
 import com.reimDev.reimWeb.client.ui.pages.AbstractPage;
 
 public class PageBuilder {
 	
 	public static void setPage(AbstractPage page) {
 		//TODO History management
 		
 		//clear existing content
 		RootPanel.get("gwt-content").clear();
 		//add new content
 		RootPanel.get("gwt-content").add(page);
		
		//set URL
 	}
 }
