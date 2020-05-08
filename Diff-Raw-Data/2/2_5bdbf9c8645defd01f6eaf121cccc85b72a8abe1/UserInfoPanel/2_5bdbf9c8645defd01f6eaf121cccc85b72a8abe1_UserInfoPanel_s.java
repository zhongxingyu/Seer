 package cpsc310.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.BlurEvent;
 import com.google.gwt.event.dom.client.BlurHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.InlineHTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 
 /**
  * My Account panel that has user information.
  */
 public class UserInfoPanel extends FlowPanel {
 	private LoginInfo loginInfo = null;
 	private Label errorMsg;
 	protected Label userPhoneNumber;		
 	protected Label userWebsite;
 	protected Label userDescription;
 	private LoginServiceAsync loginService = GWT.create(LoginService.class);
 	private HouseDataServiceAsync houseDataSvc = GWT.create(HouseDataService.class);
 	private HouseTable table;
 	
 	/**
 	 * Constructor
 	 * @param loginInfo - current user's information instance
 	 */
 	public UserInfoPanel(LoginInfo loginInfo, HouseTable table) {
 		if (loginInfo != null) {
 
 			this.loginInfo = loginInfo;
 			this.table = table;
 			
 			// Set style
 			this.setStyleName("userInfoPanel");
 			
 			// Add user's current information to the panel
 			addUserInfo();
 
 			// Add buttons
 			addChangeUserInfoBtn();
 			addSeeUserHousesBtn();
 		}
 	}
 
 	/**
 	 * Add user info to the panel
 	 */
 	private void addUserInfo() {
 		Label userName = new Label("");
 		Label userEmail = new Label("");
 		userPhoneNumber = new Label("");		
 		userWebsite = new Label("");
 		userDescription = new Label("");
 		
 		// Get info from login info
 		userName.setText("Hello, " + loginInfo.getNickname());
 		userEmail.setText("Email: " + loginInfo.getEmailAddress());
 		userPhoneNumber.setText("Phone #: " + loginInfo.getphoneNumber());
 		userWebsite.setText("Website: " + loginInfo.getWebsite());
 		userDescription.setText("Description: " + loginInfo.getDescription());
 		
 		// Add to panel
 		this.add(userName);
 		this.add(new HTML("<br>"));
 		this.add(userEmail);
 		this.add(userPhoneNumber);
 		this.add(userWebsite);
 		this.add(userDescription);
 	}
 
 	/**
 	 * Builds and adds Change User Info button to the panel
 	 */
 	private void addChangeUserInfoBtn() {
 		final Button changeUserInfoBtn = new Button("Change My Info");
 		changeUserInfoBtn.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				EditUserInfoDialog2 editUserInfoDialog = 
 						new EditUserInfoDialog2(loginInfo);
 				editUserInfoDialog.center();
 				editUserInfoDialog.show();
 
 			}
 			
 		});
 		
 		this.add(changeUserInfoBtn);
 	}
 
 	/**
 	 * Builds and adds button that allows user to
 	 * see all his/her houses to the panel
 	 */
 	private void addSeeUserHousesBtn() {
 		final Button seeUserHousesBtn = new Button("See My Houses");
 		
 		seeUserHousesBtn.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				getUserHouse();				
 			}
 		});
 		
 		this.add(new HTML("<hr>"));
 		this.add(seeUserHousesBtn);
 	}
 
 	
 	/**
 	 * Async call to the server to grab user's houses
 	 */
 	private void getUserHouse() {
 		// TODO Make Async call once async call is implemented
 
 		// Initialize the service proxy
 		if (houseDataSvc == null) {
 			houseDataSvc = GWT.create(HouseDataService.class);
 		}
 
 		// Set up the callback object
 		AsyncCallback<Void> callback = new AsyncCallback<Void>() {
 			public void onFailure(Throwable caught) {
 				Window.alert(caught.getMessage());
 			}
 
 			public void onSuccess(Void result) {
 				table.refreshTableFromBeginning();
 			}
 		};
 		houseDataSvc.getHomesByUser(loginInfo.getEmailAddress(), callback);
 		
 		
 	}
 	
 	public void refreshUserInfoPanel()
 	{
 		AsyncCallback<LoginInfo> userCallback = new AsyncCallback<LoginInfo>() {
 			public void onFailure(Throwable caught) {
 				Window.alert(caught.getMessage());
 				Window.alert("exception in refreshUserInfoPanel - call to getUser");
 			}
 			public void onSuccess(LoginInfo user) {
 				userPhoneNumber.setText("Phone #: " + user.getphoneNumber());
 				userWebsite.setText("Website: " + user.getWebsite());
 				userDescription.setText("Description: " + user.getDescription());
 			}
 		};
 		loginService.getUser(loginInfo.getEmailAddress(), userCallback);
 		
 	}
 	
 	public class EditUserInfoDialog2 extends DialogBox{
 		private int MAXKEYCOUNT = 200;
 		private Label errorMsg = new Label("");
 		private LoginServiceAsync loginService = GWT.create(LoginService.class);
 		TextBox phoneNumberBox; 
 		TextBox websiteBox;
 		TextArea descArea;
 		LoginInfo loginInfo;
 		UserInfoPanel infoPanel;
 		
 		
 		/**
 		 * Constructor
 		 * @param loginInfo - LoginInfo class instance that has user's information
 		 */
 		public EditUserInfoDialog2(LoginInfo loginInfo) {
 			FlowPanel contentWrap = new FlowPanel();
 			this.loginInfo = loginInfo;
 			this.setStyleName("editDialog");
 			
 			
 			// Build dialog content
 			buildContent(contentWrap);
 			
 			// Assemble dialog
 			this.setText("Change my information");
 			this.setGlassEnabled(true);
 			this.setAnimationEnabled(true);		
 			this.setWidget(contentWrap);		
 		}
 		
 		/**
 		 * Build dialog content
 		 * @param contentWrap - panel that wraps the contents of dialog
 		 */
 		private void buildContent(FlowPanel contentWrap) {
 			phoneNumberBox = new TextBox(); 
 			websiteBox = new TextBox();
 			descArea = new TextArea();
 			Button okBtn = new Button("OK");
 			Button cancelBtn = new Button("Cancel");
 			
 			// Set style of components
 			contentWrap.setStyleName("editPanel");
 			phoneNumberBox.addStyleDependentName("longer");
 			websiteBox.addStyleDependentName("longer");
 			errorMsg.addStyleDependentName("error");
 			
 			// Build components
 			buildDescArea(descArea);
 			buildPhoneNumberBox(phoneNumberBox);
 			buildWebsiteBox(websiteBox);
 			buildOKBtn(okBtn);
 			buildCancelBtn(cancelBtn);
 			
 			// Assemble content
 			contentWrap.add(new Label("Phone # (without - or space): "));
 			contentWrap.add(phoneNumberBox);
 			contentWrap.add(new Label("Website: "));
 			contentWrap.add(websiteBox);
 			contentWrap.add(new Label("Description about yourself (max 200 char): "));
 			contentWrap.add(new InlineHTML("<br>"));
 			contentWrap.add(descArea);
 			contentWrap.add(new HTML("<br>"));
 			contentWrap.add(errorMsg);
 			contentWrap.add(cancelBtn);
 			contentWrap.add(new InlineHTML("&nbsp;&nbsp;"));
 			contentWrap.add(okBtn);
 		}
 		
 		/**
 		 * Add blur handler to the phone number box for type checking
 		 * @param phoneNumberBox - phone number box to add this behavior
 		 */
 		private void buildPhoneNumberBox(final TextBox phoneNumberBox) {
 			phoneNumberBox.addBlurHandler(new BlurHandler() {
 
 				@Override
 				public void onBlur(BlurEvent event) {
 					String phoneNum = phoneNumberBox.getText().trim();
 					if (!phoneNum.matches("^$|^\\d{10}$")) {
 						errorMsg.setText("Phone number must be a 10-digit number only.");
 						phoneNumberBox.selectAll();
 					}
 					else {
 						if (errorMsg.getText().length() > 0)
 							errorMsg.setText("");
 					}
 				}
 				
 			});
 			
 		}
 		
 		/**
 		 * Add blur handler to the phone number box for type checking
 		 * @param phoneNumberBox - phone number box to add this behavior
 		 */
 		private void buildWebsiteBox(final TextBox websiteBox) {
 			websiteBox.addBlurHandler(new BlurHandler() {
 
 				@Override
 				public void onBlur(BlurEvent event) {
 					String website = websiteBox.getText().trim();
 					if (!website.matches("^([a-z][a-z0-9\\-]+(\\.|\\-*\\.))+[a-z]{2,6}$")) {
 						errorMsg.setText("Website format must be www.example.com");
 						websiteBox.selectAll();
 					}
 					else {
 						if (errorMsg.getText().length() > 0)
 							errorMsg.setText("");
 					}
 				}
 				
 			});
 			
 		}
 
 		/**
 		 * Build details of description text area.
 		 * Limit user's input into 200 characters
 		 * @param descArea - text area to apply constraints
 		 */
 		private void buildDescArea(final TextArea descArea) {
 			descArea.addBlurHandler(new BlurHandler() {
 				@Override
 				public void onBlur(BlurEvent event) {
 					if (descArea.getText().length() > MAXKEYCOUNT)
 						errorMsg.setText("Description can't be more than 200 characters.");
 					else {
 						if (errorMsg.getText().length() > 0)
 							errorMsg.setText("");
 					}
 				}
 			});
 		}
 		
 
 		/**
 		 * Attach OK Button behavior.
 		 * When OK button is clicked, button calls editUserInfo(), which invokes
 		 * async call to the server.
 		 * 
 		 * @param okBtn - button to attach ok button behavior
 		 */
 		private void buildOKBtn(Button okBtn) {
 			okBtn.addClickHandler(new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent event) {
 					editUserInfo();
 				}
 			});
 		}
 		
 		
 		/**
 		 * Async call to the server to edit user's information
 		 */
 		private void editUserInfo() {
 			// TODO add asycn call when implemented. make sure dialog is closed.
 			// add the user to db
 			
 			AsyncCallback<LoginInfo> userCallback = new AsyncCallback<LoginInfo>() {
 				public void onFailure(Throwable caught) {
 					Window.alert(caught.getMessage());
 					Window.alert("exception in edit user method - call to getUser");
 				}
 				public void onSuccess(LoginInfo user) {
 					AsyncCallback<Void> editUserCallback = new AsyncCallback<Void>() {
 						public void onFailure(Throwable caught) {
 							Window.alert(caught.getMessage());
 						}
 
 						public void onSuccess(Void result) {
 							//Window.alert("edited user");
 							clear();
 							hide();
 							refreshUserInfoPanel();
 							//TODO:UPDATE THE UI
 						}
 					};
 
 					long phone;
 					String phoneString = phoneNumberBox.getValue();
 					if (!phoneString.isEmpty())
 						phone = Long.parseLong(phoneNumberBox.getValue());
 					else phone = user.getphoneNumber();
 					
 					String website = websiteBox.getValue();
 					if(website.isEmpty()) website = user.getWebsite();
 					
 					String description = descArea.getValue();
 					if(description.isEmpty()) description = user.getDescription();
					loginService.editUser(user.getEmailAddress(), loginInfo.getNickname(), phone, websiteBox.getText(), description, editUserCallback);
 				}
 			};
 			loginService.getUser(loginInfo.getEmailAddress(), userCallback);
 			
 		}
 		
 		
 
 		/**
 		 * Attaches Cancel button behavior.
 		 * A click on cancel button clears the dialog and closes.
 		 * 
 		 * @param cancelBtn - button to attach cancel button behavior
 		 */
 		private void buildCancelBtn(Button cancelBtn) {
 			cancelBtn.addClickHandler(new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent event) {
 					clear();
 					hide();
 				}
 			});	
 		}
 		
 
 	}
 
 	
 }
