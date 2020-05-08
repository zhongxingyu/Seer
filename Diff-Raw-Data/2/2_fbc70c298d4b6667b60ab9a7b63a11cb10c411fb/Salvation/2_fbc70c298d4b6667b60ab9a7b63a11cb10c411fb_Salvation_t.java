 package org.elsys.salvation.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 
 
 public class Salvation implements EntryPoint {
 
 	private Button newData = new Button("New");
 	private Button existingData = new Button("Existing");
 	private HorizontalPanel mainHorizontalPanel = new HorizontalPanel();
 	private Label lastUpdatedLabel = new Label();
 	
 	public void onModuleLoad() {
 		mainHorizontalPanel.add(newData);
 		mainHorizontalPanel.add(existingData);
 		mainHorizontalPanel.add(lastUpdatedLabel);
 		
		RootPanel.get("mainDiv").add(mainHorizontalPanel);
 	}
 
 }
