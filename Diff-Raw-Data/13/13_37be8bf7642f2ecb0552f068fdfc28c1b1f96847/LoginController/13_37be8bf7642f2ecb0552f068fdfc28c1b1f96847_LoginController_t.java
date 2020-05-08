 package com.marthym.oikonomos.login.client;
 
 import java.util.Set;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.Validator;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.RunAsyncCallback;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.gwt.validation.client.impl.Validation;
 
 import com.marthym.oikonomos.client.components.MessageFlyer;
 import com.marthym.oikonomos.client.components.WaitingFlyer;
 import com.marthym.oikonomos.client.i18n.OikonomosErrorMessages;
 import com.marthym.oikonomos.client.presenter.Presenter;
 import com.marthym.oikonomos.client.services.AuthenticationServiceAsync;
 import com.marthym.oikonomos.login.client.event.LoginEvent;
 import com.marthym.oikonomos.login.client.event.LoginEventHandler;
 import com.marthym.oikonomos.login.client.presenter.WelcomePresenter;
 import com.marthym.oikonomos.login.client.view.WelcomeView;
 import com.marthym.oikonomos.shared.model.User;
 
 public class LoginController implements Presenter, ValueChangeHandler<String> {
 	private static final String CURRENT_MODULE_PATH = "index.html";
 	private static final String MAIN_MODULE_PATH = "oikonomos.html";
 	
 	private final HandlerManager eventBus;
 	private HasWidgets container;
 	private Presenter welcomePresenter;
 	private final AuthenticationServiceAsync rpcService;
 
 	public LoginController() {
 		this.rpcService = AuthenticationServiceAsync.Util.getInstance();
 		this.eventBus = new HandlerManager(null);
 		bind();
 	}
 
 	private void bind() {
 		History.addValueChangeHandler(this);
 
 		eventBus.addHandler(LoginEvent.TYPE,
 				new LoginEventHandler() {
 					
 					@Override
 					public String onLogin(LoginEvent event) {
 						return doOikonomosLogin(event.getUserName(), event.getUserPassword());
 					}
 				});
 	}
 
 	public void go(final HasWidgets container) {
 		this.container = container;
 
 		if (!History.getToken().isEmpty()) {
 			History.fireCurrentHistoryState();
 		} else {
 			History.newItem("login");
 		}
 	}
 
 	public void onValueChange(ValueChangeEvent<String> event) {
 
 		GWT.runAsync(new RunAsyncCallback() {
 			public void onFailure(Throwable caught) {
 			}
 
 			public void onSuccess() {
 				if (welcomePresenter == null) {
 					welcomePresenter = new WelcomePresenter(eventBus, new WelcomeView());
 				}
 				welcomePresenter.go(container);
 			}
 		});
 	}
 	
 	private String doOikonomosLogin(String login, String password) {
 		
 		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
 		Set<ConstraintViolation<User>> violations = validator.validate(new User(login, "", login.split("@")[0], password));
 		if (!violations.isEmpty() || password == null || password.length() < 8) {
 			OikonomosErrorMessages message = GWT.create(OikonomosErrorMessages.class);
 			MessageFlyer.error(message.error_message_user_badConnexionInfos());
 			return null;
 		}
 		
 		WaitingFlyer.start();
 		rpcService.authenticate(login, password, new AsyncCallback<User>() {
 			
 			@Override
 			public void onSuccess(User result) {
 				WaitingFlyer.stop();
 				String path = Window.Location.getPath();
 				String modulePath = CURRENT_MODULE_PATH;
 				int index = path.indexOf(modulePath);
 				String contextPath = path.substring(0, index);
 
 				Window.open(contextPath + MAIN_MODULE_PATH, "_self", "");
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
				WaitingFlyer.stop();
 				OikonomosErrorMessages message = GWT.create(OikonomosErrorMessages.class);
 				MessageFlyer.error(message.error_message_user_unauthorized());
 			}
 			
 		});
 		return "";
 	}
 }
