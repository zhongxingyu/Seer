 package com.jsar.client.unit;
 
 import java.util.Date;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.user.client.Random;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.datepicker.client.CalendarUtil;
 import com.jsar.client.YapoolGWT;
 import com.jsar.client.handler.VisibilityClickHandler;
 import com.jsar.client.http.AbstractRequestCallback;
 import com.jsar.client.http.HttpInterface;
 import com.jsar.client.json.YapoolJson;
 import com.jsar.client.util.CheckLoggedIn;
 import com.jsar.client.util.MessageDisplayer;
 
 /**
  * this unit display a Pop Up that allow user to register
  * 
  * @author rem, hyahn
  * 
  */
 
 public class CreateYapoolUnit {
 	public static CreateYapoolUnit createYapoolUnit;
 
 	// public static final String containerIopUpPaneld = "CreateYaPoolUDiv";
 
 	private Label messageLabel;
 	private Button createButton;
 	private PopupPanel popUpPanel;
 	private String id;
 
 	private String currentRestaurantId;
 
 	private ListBox expectedOrderTimeList;
 
 	public CreateYapoolUnit() {
 		CreateYapoolUnit.createYapoolUnit = this;
 
 		final Label nameLabel = new Label("name");
 		final TextBox nameField = new TextBox();
 
 		final Label descriptionLabel = new Label("description");
 		final TextBox descriptionField = new TextBox();
 
 		final Label expectedOrderTimeLabel = new Label("Expected Order Time");
 		final Label textIn = new Label("in ");
 		final Label hours = new Label(" hours");
 		final HorizontalPanel orderTimePanel = new HorizontalPanel();
 		expectedOrderTimeList = new ListBox();
 		for (int i = 1; i < 12; i++) {
 			expectedOrderTimeList.addItem(String.valueOf(i));
 		}
 		orderTimePanel.add(textIn);
 		orderTimePanel.add(expectedOrderTimeList);
 		orderTimePanel.add(hours);
 
 		final Label pickUpPlaceLabel = new Label("Pick Up Place");
 		final TextBox pickUpPlaceField = new TextBox();
 
 		messageLabel = new Label();
 		messageLabel.setVisible(false);
 		popUpPanel = new PopupPanel();
 		popUpPanel.setAutoHideEnabled(true);
 
 		VerticalPanel verticalPannel = new VerticalPanel();
 		Label registerLabel = new Label("Create YaPooL", false);
 		registerLabel.setHorizontalAlignment(null);
 		verticalPannel.add(registerLabel);
 		registerLabel.addClickHandler(new VisibilityClickHandler(
 				"YaPooLCreationTable"));
 
 		createButton = new Button("Create");
 
 		verticalPannel.add(nameLabel);
 		verticalPannel.add(nameField);
 		verticalPannel.add(descriptionLabel);
 		verticalPannel.add(descriptionField);
 		verticalPannel.add(expectedOrderTimeLabel);
 		verticalPannel.add(orderTimePanel);
 		verticalPannel.add(pickUpPlaceLabel);
 		verticalPannel.add(pickUpPlaceField);
 		verticalPannel.add(createButton);
 		verticalPannel.add(messageLabel);
 
 		verticalPannel.add(messageLabel);
 		popUpPanel.add(verticalPannel);
 		popUpPanel.setPopupPosition(450, 200);
 
 		createButton.addClickHandler(new ClickHandler() {
 			@SuppressWarnings("deprecation")
 			public void onClick(ClickEvent event) {
 				messageLabel.setVisible(true);
 				if (!CheckLoggedIn.userIsloggedIn()) {
 					return;
 				}
 				YapoolJson yapoolJson = new YapoolJson();
 				id = String.valueOf(Random.nextInt());
 				yapoolJson.setId(id);
 				yapoolJson.setDescription(descriptionField.getText());
 				yapoolJson.setRestaurant(currentRestaurantId);
 				String yapoolName = nameField.getText();
 				if (yapoolName.equals("")) {
 					displayErrorMessage("you must enter a YaPooL! name");
 					return;
 				}
 				yapoolJson.setName(yapoolName);
 				String pickUpPlace = pickUpPlaceField.getText();
 				if (pickUpPlace.equals("")) {
 					displayErrorMessage("you must enter a pick up place");
 					return;
 				}
 				yapoolJson.setPickUpPlace(pickUpPlaceField.getText());
 
 				String hoursString = expectedOrderTimeList
 						.getValue(expectedOrderTimeList.getSelectedIndex());
 
 				Date expectedOrderTime = new Date();
 				int hours = expectedOrderTime.getHours();
 				int delta = Integer.valueOf(hoursString);
 				if (hours + delta > 23) {
 					CalendarUtil.addDaysToDate(expectedOrderTime, 1);
 					expectedOrderTime.setHours(expectedOrderTime.getHours()
 							- 23 + delta);
 				} else {
 					expectedOrderTime.setHours(hours + delta);
 				}
 				yapoolJson.setExpectedOrderDate(expectedOrderTime.toString());
 
 				yapoolJson.setOwner(YapoolGWT.currentSession.getName());
 				yapoolJson.setState("open");
 				createButton.setEnabled(false);
 				createButton.setText("Sending...");
 				HttpInterface.doPostJson("/yapooldb/", yapoolJson,
 						new CreateYapoolCallbackCallback());
 				ListYapoolUnit.listYapoolUnit.listYapools();
 			}
 
 			private void displayErrorMessage(String string) {
 				messageLabel.setText(string);
 				messageLabel.setVisible(true);
 
 			}
 		});
 
 	}
 
 	class CreateYapoolCallbackCallback extends AbstractRequestCallback {
 
 		@Override
 		public void onResponseReceived(Request request, Response response) {
 			if (responseIsOk(response)) {

 				YapoolGWT.currentProfile.setCurrentYapool(id);
 				HttpInterface.doPostJson("/yapooldb/", YapoolGWT.currentProfile, new AbstractRequestCallback() {
 	    			@Override
 	    			public void onResponseReceived(Request request, Response response) {
 	    			  System.out.println(response.toString());
 	    			  System.out.println("Created Successfully");
	  					popUpPanel.hide();
	  					createButton.setEnabled(true);
	  					createButton.setText("Create");
	    			  MessageDisplayer.DisplayMessage("Yapool Successfully Created, you can access it from your YaPooL! page");
 	    			}
 	    		}); // http doPostJson Ends
						
 			} else {
 
 			}
 
 		}
 	}
 
 	public void setVisible(boolean visibility) {
 		RootPanel.get("displayCreateYaPoolContainer").setVisible(visibility);
 	}
 
 	public void createYapool(String currentRestaurantId) {
 		this.currentRestaurantId = currentRestaurantId;
 		popUpPanel.show();
 
 	}
 
 }
