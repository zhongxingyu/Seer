 package org.gemsjax.client.experiment.presenter;
 
 import java.io.IOException;
 
 import org.gemsjax.client.admin.presenter.Presenter;
 import org.gemsjax.client.admin.presenter.event.CriticalErrorEvent;
 import org.gemsjax.client.admin.presenter.event.LoadingAnimationEvent;
 import org.gemsjax.client.admin.presenter.event.LoginSuccessfulEvent;
 import org.gemsjax.client.admin.presenter.event.LogoutRequiredEvent;
 import org.gemsjax.client.admin.presenter.event.CriticalErrorEvent.CriticalErrorType;
 import org.gemsjax.client.admin.presenter.event.LoadingAnimationEvent.LoadingAnimationEventType;
 import org.gemsjax.client.admin.presenter.handler.LogoutRequiredHandler;
 import org.gemsjax.client.admin.view.LogoutView;
 import org.gemsjax.client.experiment.ExperimentUserImpl;
 import org.gemsjax.client.experiment.view.ExperimentLoginView;
 import org.gemsjax.client.module.AuthenticationModule;
 import org.gemsjax.client.module.handler.AuthenticationModuleHandler;
 import org.gemsjax.shared.communication.message.system.LogoutMessage.LogoutReason;
 import org.gemsjax.shared.user.RegisteredUser;
 
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.user.client.Window;
 import com.smartgwt.client.widgets.events.ClickEvent;
 import com.smartgwt.client.widgets.events.ClickHandler;
 
 public class ExperimentAuthenticationPresenter extends Presenter implements LogoutRequiredHandler, AuthenticationModuleHandler, ClickHandler{
 	
 	private ExperimentLoginView loginView;
 	private LogoutView logoutView;
 	private AuthenticationModule authenticationModule;
 	private String verificationCode;
 	
 	public ExperimentAuthenticationPresenter(EventBus eventBus, ExperimentLoginView loginView, LogoutView logoutView, AuthenticationModule module, String verificationCode) {
 		super(eventBus);
 		this.verificationCode = verificationCode;
 		this.authenticationModule = module;
 		authenticationModule.addAuthenticationModuleHandler(this);
 		this.loginView = loginView;
 		this.logoutView = logoutView;
 		bind();
 	}
 	
 
 	private void bind(){
 		
 		loginView.getLoginButton().addClickHandler(this);
 	}
 
 
 
 	@Override
 	public void onLogoutRequired(LogoutRequiredEvent event) {
 		loginView.resetView();
 	
 		try {
 			authenticationModule.doLogout();
 		} catch (IOException e) {
 			
 		}
 		finally{
 			Window.Location.reload();
 		}
 	}
 	
 
 
 
 
 	@Override
 	public void onLogoutReceived(LogoutReason reason) {
 		eventBus.fireEvent(new CriticalErrorEvent(CriticalErrorType.AUTHENTICATION));
 		logoutView.show(reason);
 	}
 
 
 
 
 	@Override
 	public void onParseError(Exception e) {
 		eventBus.fireEvent(new LoadingAnimationEvent(LoadingAnimationEventType.HIDE, this));
 		loginView.showError(null);
 	}
 
 
 	@Override
 	public void onLoginSuccessful(RegisteredUser authenticatedUser, long unreadNotificationRequest) {
 		eventBus.fireEvent(new LoadingAnimationEvent(LoadingAnimationEventType.HIDE, this));
 		loginView.hide();
 		eventBus.fireEvent(new LoginSuccessfulEvent(authenticatedUser, unreadNotificationRequest));
 	}
 
 
 	@Override
 	public void onLoginFailed() {
 		eventBus.fireEvent(new LoadingAnimationEvent(LoadingAnimationEventType.HIDE, this));
 		loginView.showPasswordIncorrectMessage();
 	}
 
 
 	@Override
 	public void onClick(ClickEvent event) {
 		if (event.getSource()== loginView.getLoginButton()){
 			
 			try {
				eventBus.fireEvent(new LoadingAnimationEvent(LoadingAnimationEventType.SHOW, this));
 				authenticationModule.doExperimentLogin(verificationCode, loginView.getPassword());
 			} catch (IOException e) {
 				e.printStackTrace();
 				loginView.showError(e);
 			}
 			
 		}
 		
 	}
 
 
 	@Override
 	public void onExperimentLoginSuccessful(ExperimentUserImpl user) {
 		loginView.hide();
 		loginView.resetView();
 		eventBus.fireEvent(new LoginSuccessfulEvent(user, 0));
 		eventBus.fireEvent(new LoadingAnimationEvent(LoadingAnimationEventType.HIDE, this));
 		
 	}
 	
 	
 	
 
 
 }
