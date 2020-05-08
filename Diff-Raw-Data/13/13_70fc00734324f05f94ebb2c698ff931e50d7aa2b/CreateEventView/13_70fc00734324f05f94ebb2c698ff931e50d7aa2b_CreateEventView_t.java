 package com.cs310.ubc.meetupscheduler.client;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.datepicker.client.DatePicker;
 
 public class CreateEventView extends Composite implements View {
 
 	private final DataObjectServiceAsync objectService = GWT.create(DataObjectService.class);
 	private ArrayList<HashMap<String, String>> allParks;
 	//event fields
 	private String eventName;
 	private String userName;
 	private String userEmail;
 	private String park_name;
 	private String park_id;
 	private String eventType;
 	private String date;
 	private String startTime;
 	private String endTime;
 	
 	//Panel to hold Create Event Components
 	private VerticalPanel rightPanel = new VerticalPanel();
 	private VerticalPanel leftPanel = new VerticalPanel();
 	private HorizontalPanel createEventPanel = new HorizontalPanel();
 	//Name boxes	
 	private TextBox eventNameBox = new TextBox();
 	private Label eventNameLabel = new Label("Event Name");
 	//Date Selector for Event
 	private DatePicker eventDatePicker = new DatePicker();
 	private Label eventDatePickerLabel = new Label();
 	//Time selectors
 	private ListBox startTimeList = new ListBox();
 	private ListBox endTimeList = new ListBox();
 	//Category selector
 	private ListBox categoriesListBox = new ListBox();
 	//Park Selector
 	private ListBox parksListBox = new ListBox();
 	private SimplePanel viewPanel = new SimplePanel();
 	private Element nameSpan = DOM.createSpan();
 	
 	//TODO Have to figure this out
 	public CreateEventView() {
 	     viewPanel.getElement().appendChild(nameSpan);
 	      initWidget(viewPanel);
 	}
 	
 	public CreateEventView(String parkID) {
 	    viewPanel.getElement().appendChild(nameSpan);
 	    initWidget(viewPanel);
 		park_id = parkID;
 	}
 	
 	@Override
 	public Widget asWidget() {					
 		
 		//Sets the label when a date is selected		
 		eventDatePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
 			public void onValueChange(ValueChangeEvent<Date> event) {
 				Date eventDate = event.getValue();
 				String eventDateString = DateTimeFormat.getMediumDateFormat().format(eventDate);
 				eventDatePickerLabel.setText(eventDateString);
 			}
 		}); 
 		//Default Value for date
 		eventDatePicker.setValue(new Date(), true);
 		
 		//populate time lists 			
 		setHoursList(startTimeList);
 		setHoursList(endTimeList);		
 		
 		//categories set-up
 		categoriesListBox.addItem("Default Category");
 		populateCategories(categoriesListBox);
 		categoriesListBox.setVisibleItemCount(1);
 		
 		//get parks for parks list
 		getParks(parksListBox);		
 		parksListBox.setVisibleItemCount(1);		
 		
 		Button submitButton = new Button("Submit", new ClickHandler() {
 			public void onClick(ClickEvent event) {
 							
 				//TODO: Implement verifier call, submit call, and route to event view tab
 				setEventFields();				
 				
 				if (verifyFields()) {
 					createEvent();					
 				}
 				else {
 				Window.alert("Please ensure all event fields are filled out");
 				}
 			}
 		});
 		
 		//Add items to panel
 		//TODO: fix appearance through sub-panels
 		leftPanel.add(eventNameLabel);
 		leftPanel.add(eventNameBox);
 		leftPanel.add(parksListBox);
 		leftPanel.add(categoriesListBox);
 		leftPanel.add(submitButton);
 		
 		rightPanel.add(eventDatePickerLabel);
 		rightPanel.add(eventDatePicker);
 		rightPanel.add(startTimeList);
 		rightPanel.add(endTimeList);
 		
 		
 		createEventPanel.add(leftPanel);
 		createEventPanel.add(rightPanel);		
 		
 		return createEventPanel;
 	}
 	
 	private void getParks(final ListBox parksList) {
 		
 		allParks = MeetUpScheduler.getParks();
 		addParksToParksListBox(allParks, parksList);		
 		
 	}
 	
 	private void addParksToParksListBox(ArrayList<HashMap<String, String>> parks, ListBox parksList){
 		ArrayList<String> parkNames = new ArrayList<String>();
 
 		for(int i = 0; i<parks.size(); i++){
 			parkNames.add(parks.get(i).get("name"));
 		}
 		
 		Collections.sort(parkNames);
 		
 		for(int i = 0; i<parks.size(); i++){
 			parksList.addItem(parkNames.get(i));
 		}
 	}
 	
 	private String getParkID(String parkName) {
 		String id = "";		
		for (int i=0;i<allParks.size(); i++) {
			if (allParks.get(i).get("name").equals(parkName)) {
				id = allParks.get(i).get("id");
 			}
 		}
 		
 		return id;
 	}
 	
 	private void setEventFields() {
 		
 		eventName = eventNameBox.getValue();
 		LoginInfo loginInfo = MeetUpScheduler.SharedData.getLoginInfo();
 		userName = loginInfo.getNickname();
 		userEmail = loginInfo.getEmailAddress();		
 		park_name = parksListBox.getValue(parksListBox.getSelectedIndex());
 		park_id = getParkID(park_name);
 		eventType = categoriesListBox.getValue(categoriesListBox.getSelectedIndex());
 		date = DateTimeFormat.getMediumDateFormat().format(eventDatePicker.getValue());
 		startTime = startTimeList.getValue(startTimeList.getSelectedIndex());
 		endTime = endTimeList.getValue(endTimeList.getSelectedIndex());		
 	}
 	
 	private boolean verifyFields() {
 		//TODO: specify conditions for field verification, add field specific errors
 		if (eventName == null || userName == null || park_name == null || date == null || startTime == null || endTime == null) return false;
 		return true;
 	}
 	
 	//TODO: Use the ENUM from event for the field names. Note: breaks compilation at this point
 	private void createEvent() {
 		HashMap<String, String> event = new HashMap<String, String>();
 		event.put("name", eventName);
 		event.put("park_name", park_name);
 		event.put("park_id", park_id);
 		event.put("creator_name", userName);
 		event.put("creator_email", userEmail);
 		event.put("category", eventType);
 		event.put("date", date);
 		event.put("start_time", startTime);
 		event.put("end_time", endTime);
 		event.put("num_attending", "1");
 		event.put("attending_names", userName);
 		event.put("attending_emails", userEmail);
 
 		//TODO: move to static data object? Make call to reload method to re get events and load views
 		objectService.add("Event", event, new AsyncCallback<HashMap<String, String>>() {
 			public void onFailure(Throwable error) {
 				System.out.println("Flip a table!  (>o.o)> _|__|_)");
 			}
 			
 			public void onSuccess(HashMap<String, String> newEvent) {
 				//TODO: Add call to helper to scheduler to get event view based on event id
 				System.out.println(newEvent);
 				Window.alert("Event Created with ID " + newEvent.get("id"));
 			}
 		});
 		
 	}
 	
 	private void setHoursList(ListBox list) {
 		for (int i=0; i<=23; i++ ) {
 			list.addItem(i+":00");
 		}
 		list.setVisibleItemCount(1);
 	}
 	
 	private void populateCategories(ListBox categoriesList) {
 		categoriesList.addItem("Sack-race");
 		categoriesList.addItem("Larping");
 		categoriesList.addItem("Ultimate");
 		categoriesList.addItem("Spelling-B");
 		categoriesList.addItem("Judo");
 		categoriesList.addItem("Arctic Char Fishing");
 		categoriesList.addItem("Treasure Hunt");
 		categoriesList.addItem("Night Soccer");
 		categoriesList.addItem("Dog Show");
 		categoriesList.addItem("Chili Cook-off");
 	}
 
 	@Override
 	public void setName(String name) {
 		nameSpan.setInnerText("Create Event " + name);
 		
 	}
 }
