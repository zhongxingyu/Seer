 package kornell.gui.client.presentation.bar.generic;
 
 import kornell.api.client.Callback;
 import kornell.api.client.KornellClient;
 import kornell.core.shared.to.CourseTO;
 import kornell.core.shared.to.UserInfoTO;
 import kornell.gui.client.KornellConstants;
 import kornell.gui.client.event.NavigationForecastEvent;
 import kornell.gui.client.event.NavigationForecastEventHandler;
 import kornell.gui.client.presentation.HistoryMapper;
 import kornell.gui.client.presentation.atividade.AtividadePlace;
 import kornell.gui.client.presentation.bar.ActivityBarView;
 import kornell.gui.client.presentation.course.details.CourseDetailsPlace;
 import kornell.gui.client.presentation.course.notes.NotesPopup;
 import kornell.gui.client.sequence.NavigationRequest;
 import kornell.gui.client.util.ClientProperties;
 
 import com.github.gwtbootstrap.client.ui.Button;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.place.shared.Place;
 import com.google.gwt.place.shared.PlaceChangeEvent;
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.web.bindery.event.shared.EventBus;
 
 
 public class GenericActivityBarView extends Composite implements ActivityBarView, NavigationForecastEventHandler {
 	
 	interface MyUiBinder extends UiBinder<Widget, GenericActivityBarView> {
 	}
 
 	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
 	
 	private PlaceController placeCtrl;
 		
 	private String page;
 	
 	private NotesPopup notesPopup;
 	
 	private final HistoryMapper historyMapper = GWT.create(HistoryMapper.class);
 	
 	private static KornellConstants constants = GWT.create(KornellConstants.class);
 
 	private static final String IMAGES_PATH = constants.imagesPath() + "southBar/";
 
 	private static String BUTTON_PREVIOUS = constants.previous();
 	private static String BUTTON_NEXT = constants.next();
 	private static String BUTTON_DETAILS = constants.details();
 	private static String BUTTON_NOTES = constants.notes();
 
 	Image iconPrevious;
 	Image iconNext;
 
 	@UiField
 	Button btnPrevious;
 	@UiField
 	Button btnNext;
 	@UiField
 	Button btnDetails;
 	@UiField
 	Button btnNotes;
 
 	@UiField
 	FlowPanel activityBar;
 
 	private EventBus bus;
 	private KornellClient client;
 	private UserInfoTO user;
 	
 	public GenericActivityBarView(EventBus bus, PlaceController placeCtrl, KornellClient client) {
 		initWidget(uiBinder.createAndBindUi(this));
 		this.bus = bus;
 		this.client = client;
 		this.placeCtrl = placeCtrl;
 		bus.addHandler(NavigationForecastEvent.TYPE,this);
 		
 		bus.addHandler(PlaceChangeEvent.TYPE,
 				new PlaceChangeEvent.Handler() {
 					@Override
 					public void onPlaceChange(PlaceChangeEvent event) {
 						Place newPlace = event.getNewPlace();
 						
 						if(newPlace instanceof AtividadePlace){
 							changePreviousButton();
 							btnDetails.removeStyleName("btnSelected");
 						} else if(newPlace instanceof CourseDetailsPlace){
 							enableButton(BUTTON_PREVIOUS, false);
 							enableButton(BUTTON_NEXT, false);
 							btnDetails.addStyleName("btnSelected");
 						}
 						
 					}});
 
 		client.getCurrentUser(new Callback<UserInfoTO>() {
 			@Override
 			protected void ok(UserInfoTO userTO) {
 				user = userTO;
 				display();
 			}
 		});
 	}
 	 
 	private void display(){
 		iconPrevious = new Image(IMAGES_PATH + getItemName(BUTTON_PREVIOUS)+".png");
 		displayButton(btnPrevious, BUTTON_PREVIOUS, iconPrevious);
 		
 		iconNext = new Image(IMAGES_PATH + getItemName(BUTTON_NEXT)+".png");
 		displayButton(btnNext, BUTTON_NEXT, iconNext, true);	
 		
 		displayButton(btnDetails, BUTTON_DETAILS, new Image(IMAGES_PATH + getItemName(BUTTON_DETAILS)+".png"));	
 		
 		displayButton(btnNotes, BUTTON_NOTES, new Image(IMAGES_PATH + getItemName(BUTTON_NOTES)+".png"));	
 		
 		
 		if(placeCtrl.getWhere() instanceof CourseDetailsPlace){
 			btnDetails.addStyleName("btnSelected");
 			enableButton(BUTTON_PREVIOUS, false);
 			enableButton(BUTTON_NEXT, false);
 		} else {
 			changePreviousButton();
 		}
 	}
 
 	private void displayButton(final Button btn, final String buttonType, Image icon) {
 		displayButton(btn, buttonType, icon, false);
 	}
 	
 	private void displayButton(final Button btn, final String buttonType, Image icon, boolean invertIcon) {
 		FlowPanel buttonPanel = new FlowPanel();
 		buttonPanel.addStyleName("btnPanel");
 		buttonPanel.addStyleName(getItemName(buttonType));
 		
 		icon.addStyleName("icon");
 		
 		Label label = new Label(buttonType.toUpperCase());
 		label.addStyleName("label");
 		
 		if(invertIcon){
 			buttonPanel.add(label);
 			buttonPanel.add(icon);
 		} else {
 			buttonPanel.add(icon);
 			buttonPanel.add(label);
 		}
 		
 		btn.add(buttonPanel);
 		btn.removeStyleName("btn");
 		btn.removeStyleName("btn-link");
 	}
 	
 	private String getItemName(String constant){
 		if(constant.equals(BUTTON_NEXT)){
 			return "next";
 		} else if(constant.equals(BUTTON_PREVIOUS)) {
 			return "previous";
 		} else if(constant.equals(BUTTON_DETAILS)){
 			return "details";
 		} else {
 			return "notes";
 		}
 	}
 	
 	@UiHandler("btnNext")
 	public void btnNextClicked(ClickEvent e){
 		bus.fireEvent(NavigationRequest.next());
 	}
 
 	@UiHandler("btnPrevious")
 	public void btnPrevClicked(ClickEvent e){
 		bus.fireEvent(NavigationRequest.prev());		
 	}
 	
 	@UiHandler("btnDetails")
 	void handleClickBtnDetails(ClickEvent e) {
 		if(placeCtrl.getWhere() instanceof AtividadePlace){
 			placeCtrl.goTo(new CourseDetailsPlace(getCourseUUID()));
 			btnDetails.addStyleName("btnSelected");
 			GWT.log("btnSelected");
 		} else {
 			client.getCurrentUser(new Callback<UserInfoTO>() {
 				@Override
 				protected void ok(UserInfoTO userTO) {
 					user = userTO;
 					placeCtrl.goTo(historyMapper.getPlace(user.getLastPlaceVisited()));
 					btnDetails.removeStyleName("btnSelected");
 				}
 			});
 		}
 	}
 	
 	@UiHandler("btnNotes")
 	void handleClickBtnNotes(ClickEvent e) {
 		
 		if(notesPopup == null){
 			// TODO: remove this after changing the api
 			String courseUUID = ClientProperties.getDecoded(ClientProperties.COURSE_UUID);
 			String courseNotes = ClientProperties.getDecoded(ClientProperties.COURSE_NOTES);
 			if(courseUUID == null || courseNotes == null){
 				client.getCourseTO(getCourseUUID(),new Callback<CourseTO>(){
 					@Override
 					protected void ok(CourseTO course) {
 						notesPopup = new NotesPopup(client, course.getCourse().getUUID(), course.getEnrollment().getNotes());
 						notesPopup.show();
 					}			
 				});	
 			} else {
 				notesPopup = new NotesPopup(client, courseUUID, courseNotes);
 				notesPopup.show();
 			}
 		} else {
 			notesPopup.show();
 		}
 	}
 	
 	private String getCourseUUID() {
 		try{		
 			return Window.Location.getHash().split(":")[1].split(";")[0];
 		} catch (Exception ex){
 			GWT.log("Error trying to get the course id.");
 			placeCtrl.goTo(historyMapper.getPlace(user.getLastPlaceVisited()));
 		}
 		return null;
 	}
 	
 	private String getPage() {
 		try{	
 			return Window.Location.getHash().split(":")[1].split(";")[1];
 		} catch (Exception ex){
 			GWT.log("Error trying to get the course page number.");
 		}
 		return null;
 	}
 	
 	private void changePreviousButton() {	
 		if("0".equals(getPage())){
 			enableButton(BUTTON_PREVIOUS, false);
 		} else {
 			enableButton(BUTTON_PREVIOUS, true);
 		}	
 	}
 	
 	private void enableButton(String btn, boolean enable){
 		if(BUTTON_NEXT.equals(btn)){
 			if(enable){
 				iconNext.setUrl(IMAGES_PATH + getItemName(BUTTON_NEXT)+".png");
 				btnNext.removeStyleName("disabled");
 			} else {
 				iconNext.setUrl(IMAGES_PATH + getItemName(BUTTON_NEXT)+"Disabled.png");
 				btnNext.addStyleName("disabled");
 			}
 		}
 		else if(BUTTON_PREVIOUS.equals(btn)){
 			if(enable){
 				iconPrevious.setUrl(IMAGES_PATH + getItemName(BUTTON_PREVIOUS)+".png");
 				btnPrevious.removeStyleName("disabled");
 			} else {
 				iconPrevious.setUrl(IMAGES_PATH + getItemName(BUTTON_PREVIOUS)+"Disabled.png");
 				btnPrevious.addStyleName("disabled");
 			}
 		}
 	}
 
 	@Override
 	public void onNextActivityNotOK(NavigationForecastEvent evt) {
		//TODO: uncomment
		enableButton(BUTTON_NEXT, true);
 	}
 
 	@Override
 	public void onNextActivityOK(NavigationForecastEvent evt) {
 		enableButton(BUTTON_NEXT, true);
 	}
 	
 	@Override
 	public void setPresenter(Presenter presenter) {
 	}
 	
 }
