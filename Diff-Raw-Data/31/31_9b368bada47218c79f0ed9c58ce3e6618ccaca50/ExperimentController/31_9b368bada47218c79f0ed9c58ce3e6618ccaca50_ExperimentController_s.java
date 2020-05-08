 package org.gemsjax.client.experiment;
 
 import java.io.IOException;
 
 import org.gemsjax.client.admin.AdminApplicationController;
 import org.gemsjax.client.admin.UserLanguage;
 import org.gemsjax.client.admin.adminui.TabEnviroment;
 import org.gemsjax.client.admin.presenter.CriticalErrorPresenter;
 import org.gemsjax.client.admin.presenter.LoadingPresenter;
 import org.gemsjax.client.admin.presenter.MetaModelPresenter;
 import org.gemsjax.client.admin.presenter.event.CriticalErrorEvent;
 import org.gemsjax.client.admin.presenter.event.CriticalErrorEvent.CriticalErrorType;
 import org.gemsjax.client.admin.presenter.event.LoginSuccessfulEvent;
 import org.gemsjax.client.admin.presenter.handler.LoginSuccessfulHandler;
 import org.gemsjax.client.admin.view.MetaModelView;
 import org.gemsjax.client.admin.view.implementation.CriticalErrorViewImpl;
 import org.gemsjax.client.admin.view.implementation.LoadingViewImpl;
 import org.gemsjax.client.admin.view.implementation.LogoutViewImpl;
 import org.gemsjax.client.admin.view.implementation.MetaModelViewImpl;
 import org.gemsjax.client.canvas.CanvasSupportException;
 import org.gemsjax.client.communication.WebSocketCommunicationConnection;
 import org.gemsjax.client.communication.channel.AuthenticationChannel;
 import org.gemsjax.client.communication.channel.CollaborationChannel;
 import org.gemsjax.client.experiment.presenter.ExperimentAuthenticationPresenter;
 import org.gemsjax.client.experiment.presenter.ExperimentMainPresenter;
 import org.gemsjax.client.experiment.view.ExperimentDescriptionTab;
 import org.gemsjax.client.experiment.view.ExperimentModelPlaceHolder;
 import org.gemsjax.client.experiment.view.impl.ExperimentLoginViewImpl;
 import org.gemsjax.client.experiment.view.impl.ExperimentMainViewImpl;
 import org.gemsjax.client.module.AuthenticationModule;
 import org.gemsjax.client.module.CollaborationModule;
 import org.gemsjax.client.tests.TestRunner;
 import org.gemsjax.client.tests.testcases.CollaborationFileMessageTest;
 import org.gemsjax.client.util.Console;
 import org.gemsjax.shared.communication.CommunicationConnection.EstablishedListener;
 import org.gemsjax.shared.metamodel.MetaModel;
 import org.gemsjax.shared.metamodel.impl.MetaModelImpl;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.event.shared.SimpleEventBus;
 import com.google.gwt.event.shared.UmbrellaException;
 import com.smartgwt.client.core.KeyIdentifier;
 import com.smartgwt.client.util.KeyCallback;
 import com.smartgwt.client.util.Page;
 import com.smartgwt.client.util.SC;
 
 public class ExperimentController implements EntryPoint, EstablishedListener, LoginSuccessfulHandler{
 
 	private LoadingPresenter loadingPresenter;
 	private CriticalErrorPresenter errorPresenter;
 	private EventBus eventBus;
 	private UserLanguage language = GWT.create(UserLanguage.class);
 	
 	private AuthenticationModule authenticationModule ;
 	
 	
 	@Override
 	public void onModuleLoad() {
 		
 		
 		try {
 			WebSocketCommunicationConnection webSocket = WebSocketCommunicationConnection.getInstance();
 			webSocket.addEstablishedListener(this);
 			webSocket.connect();
 				
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		
 	}
 
 
 	@Override
 	public void onEstablished() {
 		start();
 	}
 	
 	
 	private void start(){
 		KeyIdentifier debugKey = new KeyIdentifier();
 		debugKey.setAltKey(true);
 		debugKey.setKeyName("C");
 	
 		Page.registerKey(debugKey, new KeyCallback() {
 			public void execute(String keyName) {
 				SC.showConsole();
 				SC.debugger();
 			}
 		});
 		
 	
 		
 		
 		KeyIdentifier testRunnerKey = new KeyIdentifier();
 		testRunnerKey.setAltKey(true);
 		testRunnerKey.setKeyName("T");
 	
 		Page.registerKey(testRunnerKey, new KeyCallback() {
 			public void execute(String keyName) {
 				new TestRunner(new CollaborationFileMessageTest());
 			}
 		});
 		
 		
 		
 		
 		GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
 			
 			@Override
 			public void onUncaughtException(Throwable e) {
 				
 				if (e instanceof UmbrellaException)
 					e = e.getCause();
 				
 				String msg =e.toString() + " "+e.getLocalizedMessage() +" "+ e.getMessage()+" \n";
 				
 				for (StackTraceElement el : e.getStackTrace())
 					msg += el.toString() + "\n";
 				
 				
 				e.printStackTrace();
 				
 				Console.logException(e);
 				SC.say(msg);
 				SC.logWarn(msg);
 		
 			}
 		});
 	
 	
 	
 		eventBus = new SimpleEventBus();
 		eventBus.addHandler(LoginSuccessfulEvent.TYPE, this);
 		
 		// Loading Presenter is needed
 		loadingPresenter = new LoadingPresenter(eventBus, new LoadingViewImpl());
 		
 		// error Presenter
 		errorPresenter = new CriticalErrorPresenter(eventBus, new CriticalErrorViewImpl(), WebSocketCommunicationConnection.getInstance());
 		
 		
 		
 		String verificationCode = UrlHelper.getVerificationCode();
 		
 		if (verificationCode == null)
 			eventBus.fireEvent(new CriticalErrorEvent(CriticalErrorType.EXPERIMENT_VERIFICATION));
 		else{
 			authenticationModule = new AuthenticationModule(new AuthenticationChannel(WebSocketCommunicationConnection.getInstance()));
 		
 			ExperimentAuthenticationPresenter authenticationPresenter = new ExperimentAuthenticationPresenter(eventBus, new ExperimentLoginViewImpl(language), new LogoutViewImpl(language), authenticationModule, verificationCode);
 		
 		}
 	
 	}
 
 
 	@Override
 	public void onLoginSuccessful(LoginSuccessfulEvent event) {
 		
 		ExperimentUserImpl user = (ExperimentUserImpl) event.getAuthenticatedUser();
 		
		ExperimentMainViewImpl mainView = new ExperimentMainViewImpl(language, user);
	
		ExperimentMainPresenter p = new ExperimentMainPresenter(eventBus, mainView);
	
		
 		TabEnviroment.getInstance().addTab(new ExperimentDescriptionTab(language, "Description", user.getExperimentDescription() ));
 		
 		
 		MetaModel mm = new MetaModelImpl(user.getMetaModelId(), "Metamodel");
 		
		CollaborationModule module = new CollaborationModule(authenticationModule.getCurrentlyAuthenticatedUser(), mm, new CollaborationChannel(WebSocketCommunicationConnection.getInstance(), mm));
 		MetaModelViewImpl view;
 		try {
 			view = new MetaModelViewImpl("Metamodel", language);
 			MetaModelPresenter pr = new MetaModelPresenter(eventBus,view , mm,  module);
 			view.setCanClose(false);
 			
 			
 			//TODO replace with working implementation
 			TabEnviroment.getInstance().addTab(new ExperimentModelPlaceHolder());
 			TabEnviroment.getInstance().redraw();
 			
 		} catch (CanvasSupportException e) {
 			e.printStackTrace();
 			eventBus.fireEvent(new CriticalErrorEvent(CriticalErrorType.CANVAS_NOT_SUPPORTED));
 		} catch (IOException e) {
 			e.printStackTrace();
 			eventBus.fireEvent(new CriticalErrorEvent(CriticalErrorType.LIVE_CONNECTION_CLOSED));
 		}
 			
 		
 		
 	}
 	
 	
 	
 
 }
