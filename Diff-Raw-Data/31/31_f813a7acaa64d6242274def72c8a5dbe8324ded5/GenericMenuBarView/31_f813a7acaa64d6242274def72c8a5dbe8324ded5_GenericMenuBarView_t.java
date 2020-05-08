 package kornell.gui.client.presentation.bar.generic;
 
 
 import kornell.gui.client.event.InstitutionEvent;
 import kornell.gui.client.event.InstitutionEventHandler;
 import kornell.gui.client.event.LogoutEvent;
 import kornell.gui.client.presentation.bar.MenuBarView;
 import kornell.gui.client.presentation.vitrine.VitrinePlace;
 import kornell.gui.client.presentation.welcome.WelcomePlace;
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
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.web.bindery.event.shared.EventBus;
 
 
 public class GenericMenuBarView extends Composite implements MenuBarView {
 	interface MyUiBinder extends UiBinder<Widget, GenericMenuBarView> {
 	}
 	private static final String IMAGES_PATH = "skins/first/icons/menuBar/";
 
 	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
 	private PlaceController placeCtrl;
 	private EventBus bus;
 	private String barLogoFileName = "logo250x45.png";
 	
 	@UiField
 	FlowPanel menuBar;
 	@UiField
 	Button btnFake;
 	@UiField
 	Button btnHome;
 	@UiField
 	Button btnNotifications;
 	@UiField
 	Button btnMessages;
 	@UiField
 	Button btnHelp;
 	@UiField
 	Button btnMenu;
 	@UiField
 	Button btnExit;
 	@UiField
 	Image imgMenuBar;
 	
 
 	public GenericMenuBarView(PlaceController placeCtrl, EventBus bus) {
 		this.placeCtrl=placeCtrl;
 		this.bus = bus;
 		initWidget(uiBinder.createAndBindUi(this));
 		display();
 		
 		try {
 			String institutionAssetsURL = ClientProperties.base64Decode(ClientProperties.get("institutionAssetsURL"));
 			imgMenuBar.setUrl(institutionAssetsURL + barLogoFileName);
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 		
 		
 		bus.addHandler(InstitutionEvent.TYPE, new InstitutionEventHandler() {
 			@Override
 			public void onEnter(InstitutionEvent event) {
 				imgMenuBar.setUrl(event.getInstitution().getAssetsURL() + barLogoFileName);
 				GWT.log("Change logo on menu bar");
 			}
 		});
 		
 		bus.addHandler(PlaceChangeEvent.TYPE,
 				new PlaceChangeEvent.Handler() {
 					@Override
 					public void onPlaceChange(PlaceChangeEvent event) {
 						Place newPlace = event.getNewPlace();
 						boolean isAtVitrine = newPlace instanceof VitrinePlace;
 						GenericMenuBarView.this.setVisible(!isAtVitrine);
 					}
 				});
 	}
 	
 	public void display(){
 		// TODO i18n
 		displayButton(btnFake, "btnFake", "", false);
 		displayButton(btnHome, "btnHome", "home", true);
 		displayButtonWithCount(btnNotifications, "btnNotifications", "notifications", "countNotifications", 19);
 		displayButtonWithCount(btnMessages, "btnMessages", "messages", "countMessages", 99);
 		displayButton(btnHelp, "btnHelp", "help", true);
 		displayButton(btnMenu, "btnMenu", "MENU", false);
 		displayButton(btnExit, "btnExit", "SAIR", false);
 
 		Timer timer = new Timer() {
 			@Override
 			public void run() {
 				if("".equals(imgMenuBar.getUrl())){
 					imgMenuBar.setUrl("skins/first/icons/logo.png");
 				}
 			}
 		};
 		// If it was unable to fetch the logo, use the default logo
 		timer.schedule(2000);
 	}
 	
 	private void displayButtonWithCount(Button btn, final String buttonType, String content, String countStyleName, Integer value) {
 		//TODO i18n
 		FlowPanel buttonPanel = new FlowPanel();
 		buttonPanel.addStyleName("btnPanel");
 		buttonPanel.addStyleName(buttonType);
 
 		Image icon = new Image(IMAGES_PATH + content + ".png");
 		icon.addStyleName("icon");
 		buttonPanel.add(icon);
 		
 		//TODO getData
 		Label count = new Label(""+value);
 		count.addStyleName("count");
 		count.addStyleName(countStyleName);
 		buttonPanel.add(count);
 		
 		btn.add(buttonPanel);
 		btn.removeStyleName("btn");
 	}
 	
 	private void displayButton(Button btn, final String buttonType, String content, boolean isImage) {
 		btn.clear();
 		//TODO i18n
 		FlowPanel buttonPanel = new FlowPanel();
 		buttonPanel.addStyleName("btnPanel");
 		buttonPanel.addStyleName(buttonType);
 
 		if(isImage){
 			Image icon = new Image(IMAGES_PATH + content + ".png");
 			icon.addStyleName("icon");
 			buttonPanel.add(icon);
 		} else {
 			Label label = new Label(content);
 			label.addStyleName("label");
 			buttonPanel.add(label);
 		}
 		
 		btn.add(buttonPanel);
 		btn.removeStyleName("btn");
 	}
 
 	@UiHandler("btnHome")
 	void handleHome(ClickEvent e) {
 		placeCtrl.goTo(new WelcomePlace());
 	}
 	
 	@UiHandler("btnExit")
 	void handleExit(ClickEvent e) {
 		bus.fireEvent(new LogoutEvent());
 	}
 
 	@Override
 	public void setPresenter(Presenter presenter) {
 	}
 
 }
