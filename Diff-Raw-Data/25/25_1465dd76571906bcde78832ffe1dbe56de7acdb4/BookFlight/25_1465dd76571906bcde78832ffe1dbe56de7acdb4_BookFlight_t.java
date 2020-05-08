 package bookflight.client;
 
 import java.util.Arrays;
 import java.util.Date;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.dom.client.Style.Position;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.SuggestBox;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.datepicker.client.DatePicker;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 
 public class BookFlight implements EntryPoint {
 
 	private TabPanel tpanel = new TabPanel();
 	private VerticalPanel vpanel = new VerticalPanel();
 	private VerticalPanel vpanel2 = new VerticalPanel();
 
 	private final Label fromairport = new Label("From Airport");
 	private final Label toairport = new Label("To Airport");
 	private final Button findflight = new Button("Find Flight");
 	private final Label passengers = new Label("Passengers");
 
 	//Suggestbox Elements
 	private SuggestBox fromairporttext = new SuggestBox();
 	private SuggestBox toairporttext = new SuggestBox();
 
 	private Image cal1 = new Image("cal.gif");
 	private Image cal2 = new Image("cal.gif");
 	private PopupPanel cal1pop = new PopupPanel();
 	private PopupPanel cal2pop = new PopupPanel();
 	private DatePicker leavedatePicker = new DatePicker();
 	private DatePicker returndatePicker = new DatePicker();
	private AbsolutePanel absolutePanel = new AbsolutePanel();
 
 	public void onModuleLoad() {
 		// TEST SUGGESTBOX STUFF
 				RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
 						"/bookflight/GetAirports");
 
 				try {
 					// send the request
 					builder.sendRequest(null, new RequestCallback() {
 						public void onError(Request request, Throwable exception) {
 							// handle error
 						}
 
 						public void onResponseReceived(Request request,
 								Response response) {
 							// check the status code
 							int statusCode = response.getStatusCode();
 							if (statusCode == 200) {
 								String[] airports = response.getText().split("\n");
 
 								MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
 								oracle.addAll(Arrays.asList(airports));
								//oracle.add("Hello");
 
								fromairporttext = new SuggestBox(oracle);
								toairporttext = new SuggestBox(oracle);
								
								absolutePanel.add(fromairporttext, 165, 24);
								fromairporttext.setSize("159px", "18px");
								absolutePanel.add(toairporttext, 165, 61);
								toairporttext.setSize("159px", "18px");
 
 							}
 						}
 					});
 				} catch (RequestException e) {
 					// handle error
 				}
 		tpanel.setSize("430px", "301px");
 		tpanel.add(vpanel, "Book Flights"); // Adds Book Flights Panel
 		tpanel.add(vpanel2, "Find Flights"); // Adds Book Flights Panel
 		tpanel.selectTab(0);
 		vpanel.setSize("156px", "255px");
		
 
 		vpanel.add(absolutePanel);
 		absolutePanel.setSize("414px", "249px");
 		fromairport.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		absolutePanel.add(fromairport, 22, 24);
 		fromairport.setSize("137px", "31px");
 		toairport.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		absolutePanel.add(toairport, 22, 61);
 		toairport.setSize("137px", "30px");
 
 		Label leavetime = new Label("Departure (mm/dd/yy)");
 		leavetime.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		absolutePanel.add(leavetime, 22, 97);
 		leavetime.setSize("137px", "30px");
 
 		Label returntime = new Label("Return (mm/dd/yy)");
 		returntime.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		absolutePanel.add(returntime, 22, 133);
 		returntime.setSize("137px", "31px");
 		absolutePanel.add(passengers, 22, 170);
 		passengers.setSize("137px", "18px");
 		findflight.setText("Find Flight");
 		absolutePanel.add(findflight, 62, 204);
 
 		cal1.setSize("30px", "30px");
 		cal2.setSize("30px", "30px");
 
 		absolutePanel.add(cal1, 351, 97);
 		absolutePanel.add(cal2, 351, 133);
 
 		cal1pop.setWidget(leavedatePicker);
 		cal2pop.setWidget(returndatePicker);
 
 		final ListBox numpassengers = new ListBox();
 		numpassengers.setVisibleItemCount(1);
 		numpassengers.addItem("1");
 		numpassengers.addItem("2");
 		numpassengers.addItem("3");
 		numpassengers.addItem("4");
 		numpassengers.addItem("5");
 		numpassengers.addItem("6");
 		numpassengers.addItem("7");
 		numpassengers.addItem("8");
 		numpassengers.addItem("9");
 		numpassengers.addItem("10");
 		absolutePanel.add(numpassengers, 164, 170);
 
 		final TextBox leavedatebox = new TextBox();
 		absolutePanel.add(leavedatebox, 165, 97);
 		leavedatebox.setSize("159px", "18px");
 
 		final TextBox returndatebox = new TextBox();
 		absolutePanel.add(returndatebox, 165, 133);
 		returndatebox.setSize("159px", "18px");
 		//INITIALIZE ROOT PANEL
 		RootPanel rootPanel = RootPanel.get("book_flight");
 		rootPanel.add(tpanel, 10, 10);
 		rootPanel.getElement().getStyle().setPosition(Position.RELATIVE);
 
 		//CALENDAR ITEMS
 		cal1.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent e) {
 
 				if (!cal1pop.isShowing()) { // IF IT ISN"T SHOWING, SHOW IT!
 					cal1pop.setPopupPosition(cal1.getAbsoluteLeft() + 20,
 							cal1.getAbsoluteTop() + 20);
 					cal1pop.show(); // SHOW IT!
 					cal2pop.hide();
 				} else {
 					cal1pop.hide(); // HIDE IT!
 				}
 			}
 		});
 
 		cal2.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent e) {
 				if (!cal2pop.isShowing()) {
 					cal2pop.setPopupPosition(cal2.getAbsoluteLeft() + 20,
 							cal2.getAbsoluteTop() + 20);
 					cal2pop.show();
 					cal1pop.hide();
 				} else {
 					cal2pop.hide();
 
 				}
 			}
 		});
 
 		leavedatePicker.addValueChangeHandler(new ValueChangeHandler() {
 			public void onValueChange(ValueChangeEvent event) {
 				Date date = (Date) event.getValue();
 				String dateString = DateTimeFormat.getFormat("MM/dd/yy")
 						.format(date);
 				leavedatebox.setText(dateString);
 				cal1pop.hide();
 			}
 		});
 
 		returndatePicker.addValueChangeHandler(new ValueChangeHandler() {
 			public void onValueChange(ValueChangeEvent event) {
 				Date date = (Date) event.getValue();
 				String dateString = DateTimeFormat.getFormat("MM/dd/yy")
 						.format(date);
 				returndatebox.setText(dateString);
 				cal2pop.hide();
 			}
 		});
 
 
 		findflight.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				if (fromairporttext.getText().isEmpty()) {
 					Window.alert("Please Enter a 'From Airport'");
 				} else {
 					if (toairporttext.getText().isEmpty()) {
 						Window.alert("Please Enter a 'To Airport'");
 					} else {
 						if (leavedatebox.getText().isEmpty()) {
 							Window.alert("Please enter a 'Departure Date'");
 						} else {
 							if (returndatebox.getText().isEmpty()) {
 								Window.alert("Please enter a 'Return Date'");
 							} else {
 
 								// BEGINS EVALUATION OF THE DATE FORMAT
 								DateTimeFormat f = DateTimeFormat
 										.getFormat("MM/dd/yy");
 								Date dstart = null;
 								Date dend = null;
 								Date today = new Date();
 
 								try {
 									dstart = f.parse(leavedatebox.getValue()
 											.trim());
 								} catch (Exception error) {
 									Window.alert("Unable to format departure date!");
 
 								}
 								try {
 									dend = f.parse(returndatebox.getValue()
 											.trim());
 								} catch (Exception error) {
 									Window.alert("Unable to format return date!");
 
 								}
 
 								if (dend.before(dstart)) {
 									Window.alert("Departure date cannot be after return date");
 
 								} else {
 
 									if (dstart.before(today)) {
 										Window.alert("Departure date cannot be in the past!");
 
 									} else {
 
 										// NEEDS A PARAMETER TO JUDGE WHETHER OR
 										// NOT IT IS
 										// ACTUALLY BOOKED
 										Window.alert("Your Flight has been Booked!");
 										fromairporttext.setText("");
 										toairporttext.setText("");
 										leavedatebox.setText("");
 										returndatebox.setText("");
 										numpassengers.setSelectedIndex(0);
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		});
 	}
 }
