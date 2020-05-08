 package com.cs310.ubc.meetupscheduler.client;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.google.gwt.core.client.EntryPoint;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.maps.client.MapWidget;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 
 import com.google.gwt.user.client.ui.TabPanel;
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
 	  private final DataObjectServiceAsync parkService = GWT.create(DataObjectService.class);
 	  private final DataObjectServiceAsync eventService = GWT.create(DataObjectService.class);
 	  private GlobalView globalView;
 
 	  private AdminView admin;
 
 	  private CreateEventView createEventView;
 	  private EventView eventView;
 	  
 	  private LoginInfo loginInfo = null;
 	  private VerticalPanel loginPanel = new VerticalPanel();
 	  private Label loginLabel = new Label("Please sign in to your Google Account to access the Vancouver Meetup Scheduler application.");
 	  private Anchor signInLink = new Anchor("Sign In");
 	  private Anchor signOutLink = new Anchor("Sign Out");
 	  
 	  private static ArrayList<HashMap<String, String>> allEvents;
 	  private static ArrayList<HashMap<String, String>> allParks;
 
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {
 	    // Check login status using login service.
 	    LoginServiceAsync loginService = GWT.create(LoginService.class);
 	    loginService.login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo>() {
 	      public void onFailure(Throwable error) {
 	    	  //Do something?
 	      }
 	
 	      public void onSuccess(LoginInfo result) {
 	        loginInfo = result;
 	        if(loginInfo.isLoggedIn()) {
 	        	//TODO: Fix this daisy chain of async calls into something more elegant.
 	        	//Currently calls loadParks, which calls loadEvents, which calls loadMeetupScheduler
 	        	loadParks();
 	        } else {
 	        	loadLogin();	
 	        }
 	      }
 	    });
 	}
 	
 	private void loadLogin() {
 	    // Assemble login panel.
 	    signInLink.setHref(loginInfo.getLoginUrl());
 	    loginPanel.add(loginLabel);
 	    loginPanel.add(signInLink);
 	    RootPanel.get().add(loginPanel);
 	}
 
 	public void loadMeetupScheduler() {
 		
 		
 		RootPanel.get().remove(loginPanel);
 		
 		tabPanel = new TabPanel();
 	    initTabPanel();
 	    admin = new AdminView();
 	    tabPanel.add(admin.asWidget(), "Administrator");
 	    RootPanel.get().add(tabPanel);
 	    // Set up sign out hyperlink.
 	    signOutLink.setHref(loginInfo.getLogoutUrl());
 	    RootPanel.get().add(signOutLink);
 	}
 
 
 	private void initTabPanel() {
 		
 		//creates view objects. Must be done after onSuccess from the async calls
 		globalView = new GlobalView();
 		createEventView = new CreateEventView();
 		eventView = new EventView();
 		
 		tabPanel.add(globalView.asWidget(), "Vancouver Parks and Events");
 	    tabPanel.add(createEventView.asWidget(), "Create an Event");	   
 	    tabPanel.add(eventView.asWidget(), "Event Information Page");
 	    tabPanel.addSelectionHandler(new SelectionHandler<Integer>(){
 	      public void onSelection(SelectionEvent<Integer> event) {
 	    	  //TODO Load widgets on demand. Too slow right now.
 	      
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
 	
 	//loads parks and store in allParks for retrieval for views, maps, etc.
 	private void loadParks() {
 		
 		parkService.get("Park", "*", new AsyncCallback<ArrayList<HashMap<String,String>>>() {
 			@Override
 			public void onFailure(Throwable error) {
 				//TODO: replace with actual table flip
 				System.out.println("Table Flip!");
 			}
 			
 			public void onSuccess(ArrayList<HashMap<String, String>> parks) {
 				allParks = parks;
 				loadEvents();
 			}
 		});
 	}
 	
 	//loads events and stores in allEvents object for retrieval by views, etc.
 	private void loadEvents(){
 		eventService.get("Event", "*", new AsyncCallback<ArrayList<HashMap<String,String>>>(){
 			@Override
 			public void onFailure(Throwable caught) {
 				System.out.println("oh noes event data didnt werks");
 			}
 
 			@Override
 			public void onSuccess(ArrayList<HashMap<String, String>> events) {
 				allEvents = events;
 				loadMeetupScheduler();				
 			}
 		});
 	}
 	//TODO: migrate to static data class
 	//accessors for parks and events data
 	public static ArrayList<HashMap<String, String>> getParks() {
 		return allParks;
 	}
 	
 	public static ArrayList<HashMap<String, String>> getEvents() {
 		return allEvents;
 	}
 	
 	//TODO: Implement accessor for login info
 	public static void getLoginInfo() {
 		return;
 	}
 	
 	//TODO: Implement reload of page. To be called by event creation. 
 	public void reloadViews() {
 		//remove old elements? then redraw
 		//loadEvents();
 	}
 
 	
 }
