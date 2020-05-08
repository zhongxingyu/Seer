 package com.duggan.workflow.client.ui.component;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Anchor;
 
 public class ActionLink extends Anchor {
 
 	public ActionLink() {
 		addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
				//setStyleName("hidden");
			}	
 		});
 	}
 }
