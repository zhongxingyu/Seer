 package de.evosec.printdemo.client;
 
import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
 
 public class Demo implements EntryPoint {
 
 	@Override
 	public void onModuleLoad() {
		final Button demoButton = new Button("Demo");
		demoButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				Log.info("Hello");
			}

		});
		RootPanel.get("demoButtonContainer").add(demoButton);
 	}
 
 }
