 package edu.ycp.cs320.chronos.client;
 
 
 import java.sql.Date;
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.LayoutPanel;
 import com.google.gwt.user.client.ui.Button;
 //import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 
import com.google.gwt.i18n.client.TimeZone;		//Used to get the current date and time 
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.client.DateTimeFormat;
 
 import edu.ycp.cs320.chronos.shared.Event;
 import com.google.gwt.user.client.ui.DateLabel;
 
 public class mainView extends Composite{
 	
 	public mainView(){
		Date date; 
 		final LayoutPanel mainPanel = new LayoutPanel();
 		//Sign out button: signs the user out upon click
 		Button signOut = new Button("Sign out");
 		signOut.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				ChronosUI.setCurrentView(new LoginView());
 			}
 		});
 		mainPanel.add(signOut);
 		mainPanel.setWidgetLeftWidth(signOut, 0.0, Unit.PX, 81.0, Unit.PX);
 		mainPanel.setWidgetTopHeight(signOut, 50.0, Unit.PX, 30.0, Unit.PX);
 		
 		//Create Event button: Upon click, the user will be able to enter information needed in order to make a new event
 		Button createEvent = new Button("Create Event");
 		mainPanel.add(createEvent);
 		mainPanel.setWidgetLeftWidth(createEvent, 0.0, Unit.PX, 81.0, Unit.PX);
 		mainPanel.setWidgetTopHeight(createEvent, 87.0, Unit.PX, 30.0, Unit.PX);
 		
 		//Calendar button: Displays an actual calendar for the month
 		Button btnCalendar = new Button("Calendar");
 		mainPanel.add(btnCalendar);
 		mainPanel.setWidgetLeftWidth(btnCalendar, 0.0, Unit.PX, 81.0, Unit.PX);
 		mainPanel.setWidgetTopHeight(btnCalendar, 123.0, Unit.PX, 30.0, Unit.PX);
 		
 		DateLabel dateLabel = new DateLabel();
 		mainPanel.add(dateLabel);
 		mainPanel.setWidgetLeftWidth(dateLabel, 580.0, Unit.PX, 64.0, Unit.PX);
 		mainPanel.setWidgetTopHeight(dateLabel, 0.0, Unit.PX, 18.0, Unit.PX);
 		
 		//Displays the user's next event if retrieval was a success
		RPC.eventManagementService.nextEventString(ChronosUI.user, date.getMonth(), date.getDate(), date.getYear(), new AsyncCallback<String>(){
 
 			@Override
 			public void onFailure(Throwable caught) {
 				GWT.log("Account's next event retrieval failed: " + caught.getMessage());				
 			}
 
 			@Override
 			public void onSuccess(String result) {
 				Label lblNextevent = new Label(result);
 				mainPanel.add(lblNextevent);
 				mainPanel.setWidgetLeftWidth(lblNextevent, 144.0, Unit.PX, 320.0, Unit.PX);
 				mainPanel.setWidgetTopHeight(lblNextevent, 50.0, Unit.PX, 41.0, Unit.PX);				
			}

			
 			
 		});		
 		
 		
 	}
 }
