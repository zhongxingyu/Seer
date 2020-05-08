 package kornell.gui.client.presentation.vitrine.generic;
 
 import kornell.api.client.Callback;
 import kornell.api.client.KornellClient;
 import kornell.core.shared.to.UserInfoTO;
 import kornell.gui.client.presentation.profile.ProfilePlace;
 import kornell.gui.client.presentation.terms.TermsPlace;
 import kornell.gui.client.presentation.vitrine.VitrinePlace;
 import kornell.gui.client.presentation.vitrine.VitrineView;
 import kornell.gui.client.util.ClientProperties;
 
 import com.github.gwtbootstrap.client.ui.Alert;
 import com.github.gwtbootstrap.client.ui.Form;
 import com.github.gwtbootstrap.client.ui.Image;
 import com.github.gwtbootstrap.client.ui.PasswordTextBox;
 import com.github.gwtbootstrap.client.ui.TextBox;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.place.shared.Place;
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.place.shared.PlaceHistoryMapper;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 //HTTP
 
 public class GenericVitrineView extends Composite implements VitrineView {
 	interface MyUiBinder extends UiBinder<Widget, GenericVitrineView> {
 	}
 
 	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
 	private PlaceController placeCtrl;
 	
 	
 	@UiField
 	TextBox txtUsername;
 	@UiField
 	PasswordTextBox pwdPassword;
 	@UiField
 	Form frmLogin;
 	@UiField
 	Button btnLogin;
 	@UiField
 	Button btnRegister;
 	@UiField
 	Alert altUnauthorized;
 	@UiField
 	Image imgLogo;
 
 	@UiField
 	FlowPanel contentPanel;
 
 
 	private KornellClient client;
 	private Place defaultPlace;
 	private PlaceHistoryMapper mapper;
 	//TODO i18n xml
 	public GenericVitrineView(
 			PlaceHistoryMapper mapper, 
 			PlaceController placeCtrl,
 			Place defaultPlace,
 			KornellClient client) {
 		this.placeCtrl = placeCtrl;
 		this.client = client;
 		this.defaultPlace = defaultPlace;
 		this.mapper = mapper;
 		
 	
 		initWidget(uiBinder.createAndBindUi(this));
 		pwdPassword.addKeyPressHandler(new KeyPressHandler() {			
 			@Override
 			public void onKeyPress(KeyPressEvent event) {
 				if(KeyCodes.KEY_ENTER == event.getCharCode())
 					doLogin();				
 			}
 		});
 		txtUsername.getElement().setAttribute("autocorrect", "off");
 		txtUsername.getElement().setAttribute("autocapitalize", "off");
 		btnLogin.removeStyleName("btn");
 		
 		String imgLogoURL = ClientProperties.getDecoded("institutionAssetsURL");
 		if(imgLogoURL != null){
 			imgLogo.setUrl(imgLogoURL + "logo300x80.png");
 		} else {
 			imgLogo.setUrl("/skins/first/icons/logo.png");
 		}
 	}
 
 
 	@Override
 	public void setPresenter(Presenter presenter) {
 	}
 
 	@UiHandler("btnLogin")
 	void doLogin(ClickEvent e) {
 		altUnauthorized.setVisible(true);
 		doLogin();
 	}
 
 	@UiHandler("btnRegister")
 	void register(ClickEvent e) {
 		placeCtrl.goTo(new ProfilePlace(""));
 	}
 
 	private void doLogin() {
 		altUnauthorized.setVisible(false);
 		Callback<UserInfoTO> callback = new Callback<UserInfoTO>() {
 			@Override
 			protected void ok(UserInfoTO user) {
 				if("".equals(user.getPerson().getConfirmation())){
 					if(user.isSigningNeeded()){
 						placeCtrl.goTo(new TermsPlace());
 					} else {
 						String token = user.getLastPlaceVisited();
 						Place place;
 						if(token == null || token.contains("vitrine")){
 							place = defaultPlace;
 						}else {
 							place = mapper.getPlace(token);
 							
 						}
 						placeCtrl.goTo(place);
 					}
 				} else {
 					altUnauthorized.setText("Usuário não verificado. Confira seu email.");
 					altUnauthorized.setVisible(true);
 				}
 			}
 
 			@Override
 			protected void unauthorized() {
 				altUnauthorized.setText("Usuário ou senha incorretos, por favor tente novamente.");
 				altUnauthorized.setVisible(true);
 			}
 		};
 		String confirmation = ((VitrinePlace)placeCtrl.getWhere()).getConfirmation();
 		GWT.log("Confirmation: " + confirmation);
 		// TODO: Should be client.auth().checkPassword()?
 		// TODO: Should the api accept HasValue<String> too?
 		client.login(txtUsername.getValue(),
 				pwdPassword.getValue(),
 				confirmation,
 				callback);
 	}
 
 }
