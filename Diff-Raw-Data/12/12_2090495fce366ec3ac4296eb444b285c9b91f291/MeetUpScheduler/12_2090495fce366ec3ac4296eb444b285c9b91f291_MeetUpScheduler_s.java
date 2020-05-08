 package com.cs310.ubc.meetupscheduler.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FileUpload;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
 import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class MeetUpScheduler implements EntryPoint {
 	/**
 	 * The message displayed to the user when the server cannot be reached or
 	 * returns an error.
 	 * TODO: not implemented
 	 */
 	private static final String SERVER_ERROR = "An error occurred while "
 			+ "attempting to contact the server. Please check your network "
 			+ "connection and try again.";
 	
 	  private TabPanel tabPanel;
 	  private DataObjectServiceAsync dataObjectService = GWT.create(DataObjectService.class);
 	  private GlobalView globalView = new GlobalView();
 	  private CreateEventView createEventView = new CreateEventView();
 
 
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {
 	    tabPanel = new TabPanel();
 	    initTabPanel();
 	    //set pages here
 	    AdminView admin = new AdminView();
 	    tabPanel.add(admin.createPage(), "Upload XML file");
 	    RootPanel.get().add(tabPanel);
 	  }
 
 
 	private void initTabPanel() {
 		tabPanel.add(globalView.createPage(), "Vancouver Parks and Events");
 	    tabPanel.add(createEventView.createPage(), "Create an Event");
 	    tabPanel.add(new HTML("<h1>Page 2 Content: Page 2</h1>"), " Page 2 ");
 
 	    tabPanel.addSelectionHandler(new SelectionHandler<Integer>(){
 	      public void onSelection(SelectionEvent<Integer> event) {
 	        // TODO Auto-generated method stub
 	        History.newItem("page" + event.getSelectedItem());
 	      }});
 
 	    History.addValueChangeHandler(new ValueChangeHandler<String>() {
 	      public void onValueChange(ValueChangeEvent<String> event) {
 	        String historyToken = event.getValue();
 
 	        // Parse the history token
 	        try {
 	          if (historyToken.substring(0, 4).equals("page")) {
 	            String tabIndexToken = historyToken.substring(4, 5);
 	            int tabIndex = Integer.parseInt(tabIndexToken);
 	            // Select the specified tab panel
 	            tabPanel.selectTab(tabIndex);
 	          } else {
 	            tabPanel.selectTab(0);
 	          }
 
 	        } catch (IndexOutOfBoundsException e) {
 	          tabPanel.selectTab(0);
 	        }
 	      }
 	    });
 
 	    tabPanel.selectTab(0);
 	    tabPanel.setSize("85%", "100%");
 	}
 	public void createTab(Widget w, String name) {
 	    tabPanel.add(w, name);
 	}
 }
