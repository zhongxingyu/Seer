 package logbook.client.a_nonroo.app;
 
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import logbook.client.a_nonroo.app.activities.LogBookActivityMapper;
 import logbook.client.a_nonroo.app.client.LogBookAdminNav;
 import logbook.client.a_nonroo.app.client.LogBookNav;
 import logbook.client.a_nonroo.app.client.place.AdminPlace;
 import logbook.client.a_nonroo.app.client.place.LogBookPlaceHistoryFactory;
 import logbook.client.a_nonroo.app.client.place.LogBookPlaceHistoryMapper;
 import logbook.client.a_nonroo.app.client.place.LoginPlace;
 import logbook.client.a_nonroo.app.request.LogBookRequestFactory;
 import logbook.client.scaffold.request.RequestEvent;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.logging.client.LogConfiguration;
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.place.shared.PlaceHistoryHandler;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 import com.google.inject.Inject;
 import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryLogHandler;
 import com.google.web.bindery.requestfactory.shared.LoggingRequest;
 import com.google.web.bindery.requestfactory.shared.Receiver;
 /**
  * The applications core. Is instantiated by GIN.
  * @author milan
  *
  */
 public class LogBookApplication {
 
 	private static final Logger log = Logger.getLogger(LogBookApplication.class.getName());
 
 	private final LogBookShell studentShell;
 	private final LogBookAdminShell adminShell;
 	private final LogBookRequestFactory requestFactory;
 	private final EventBus eventBus;
 	private final PlaceController placeController;
 	private final LogBookPlaceHistoryFactory logBookPlaceHistoryFactory;
 	private final LogBookActivityMapper mcAppActivitiesMapper;
 
 
 	@Inject
 	public LogBookApplication(LogBookShell shell, LogBookAdminShell adminShell, LogBookRequestFactory requestFactory, EventBus eventBus,
 	                          PlaceController placeController, LogBookPlaceHistoryFactory oscePlaceHistoryFactory,
 	                          LogBookActivityMapper applicationMainActivitiesMapper) {
 		Log.debug("LogBookApp.LogBookApp");
 		this.studentShell = shell;
 		this.requestFactory = requestFactory;
 		this.eventBus = eventBus;
 		this.placeController = placeController;
 		this.logBookPlaceHistoryFactory = oscePlaceHistoryFactory;
 		this.mcAppActivitiesMapper = applicationMainActivitiesMapper;
 		this.adminShell=adminShell;
 	}
 	
 	
 
 	public void run() {
 		
 		Log.debug("McApp.run()");
 
 		/* Add handlers, setup activities */
 
		requestFactory.studentRequestNonRoo().isCurrentUserStudent().fire(new Receiver<Boolean>() {
 
 			@Override
 			public void onSuccess(Boolean isStudent) {
 				if(isStudent == null) {
					/*loginInit();
					RootLayoutPanel.get().add(loginShell);*/
 				}
 				else if(isStudent == true) {
 					initStudent();
 					RootLayoutPanel.get().clear();
 					RootLayoutPanel.get().add(studentShell);
 				}
 				else if(isStudent == false) {
 					initAdmin();
 					RootLayoutPanel.get().clear();
 					RootLayoutPanel.get().add(adminShell);	
 				}
 
 			}
		});
 		
		/*//initStudent();
		initAdmin();*/
 
 		/* Hide the loading message */
 
 //		Element loading = Document.get().getElementById("loading");
 //		loading.getParentElement().removeChild(loading);
 
 		/* And show the user the shell */
 
 		Log.debug("McApp.addPanlel");
		//RootLayoutPanel.get().add(shell);
		RootLayoutPanel.get().add(adminShell);
 		Log.debug("McApp.addPanlel.after");
 
 	}
 
 	private void initAdmin() 
 	{
 		Log.debug("McApp.initAdmin()");
 		GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
 			public void onUncaughtException(Throwable e) {
 				Log.warn("Fehler im Exception Handler: "+e.getMessage(), e);
 				log.log(Level.SEVERE, e.getMessage(), e);
 			}
 		});
 
 		if (LogConfiguration.loggingIsEnabled()) {
 			// Add remote logging handler
 			RequestFactoryLogHandler.LoggingRequestProvider provider = new RequestFactoryLogHandler.LoggingRequestProvider() {
 				public LoggingRequest getLoggingRequest() {
 					return requestFactory.loggingRequest();
 				}
 			};
 			Logger.getLogger("").addHandler(
 					new RequestFactoryLogHandler(provider, Level.WARNING,
 							new ArrayList<String>()));
 		}
 
 		RequestEvent.register(eventBus, new RequestEvent.Handler() {
 			// Only show loading status if a request isn't serviced in 250ms.
 			private static final int LOADING_TIMEOUT = 250;
 
 			public void onRequestEvent(RequestEvent requestEvent) {
 				if (requestEvent.getState() == RequestEvent.State.SENT) {
 				//	shell.getMole().showDelayed(LOADING_TIMEOUT);
 				} else {
 			//		shell.getMole().hide();
 				}
 			}
 		});
 
 		
 		Log.debug("McApp.Mapper");
 		
 
 		
 		/*CachingActivityMapper cached = new CachingActivityMapper(mcAppActivitiesMapper);
 		FilterForMainPlaces filterForMainPlaces = new FilterForMainPlaces();
 		ActivityMapper masterActivityMap = new FilteredActivityMapper(filterForMainPlaces, cached);
 		final ActivityManager masterActivityManager = new ActivityManager(masterActivityMap, eventBus);*/
 		
 		
 
 //		ProxyListPlacePicker proxyListPlacePicker = new ProxyListPlacePicker(placeController, proxyPlaceToListPlace);
 //		HasConstrainedValue<ProxyListPlace> listPlacePickerView = shell.getPlacesBox();
 //		listPlacePickerView.setAcceptableValues(getTopPlaces());
 //		proxyListPlacePicker.register(eventBus, listPlacePickerView);
 
 //		final ActivityManager detailsActivityManager = new ActivityManager(applicationDetailsActivities, eventBus);
 
 //		detailsActivityManager.setDisplay(shell.getDetailsPanel());
 		Log.debug("LogBook.History");
 		/* Browser history integration */
 		LogBookPlaceHistoryMapper mapper = GWT.create(LogBookPlaceHistoryMapper.class);
 		mapper.setFactory(logBookPlaceHistoryFactory);
 		PlaceHistoryHandler placeHistoryHandler = new PlaceHistoryHandler(mapper);
 		//ProxyListPlace defaultPlace = getTopPlaces().iterator().next();
 		
 		
 		//placeHistoryHandler.handleCurrentHistory();
 		//new LogBookHeaderLogic(requestFactory, placeController, eventBus);
 		//LogBookNav nav = new LogBookNav(requestFactory, placeController, placeHistoryHandler,mcAppActivitiesMapper,eventBus);
 		LogBookAdminNav adminNav = new LogBookAdminNav(requestFactory, placeController, placeHistoryHandler,mcAppActivitiesMapper,eventBus);
 		//masterActivityManager.setDisplay(shell.getLogBookNavSimplePanel());
 		//shell.setNavigation(nav);
 		adminShell.setNavigation(adminNav);
 		//if(placeController.getWhere() instanceof SkillPlace)
 		//{
 		//	nav.masterActivityManager.setDisplay((SimplePanel)nav.getMainLoogBookTabpanel().getWidget(1));
 			
 		//}
 		placeHistoryHandler.register(placeController, eventBus, new AdminPlace());
 		
 		placeHistoryHandler.handleCurrentHistory();
 		//LogBookShell shell = new LogBookShell(requestFactory, placeController, placeHistoryHandler);
 		
 		
 	//	shell.setNavigation(nav);
 		
 		//final TopPanel topPanel = TopPanel.instance(requestFactory, placeController);
 	//	shell.setTopPanel(topPanel);
 		
 		/*requestFactory.personRequest().findAllPeople().fire(new Receiver<List<PersonProxy>>(){
 
 			@Override
 			public void onSuccess(List<PersonProxy> response) {
 				topPanel.getLoggedUser().setAcceptableValues(response);
 				//topPanel.getLoggedUser().setValue(response.get(0), true);
 				
 				
 			}
 	          public void onFailure(ServerFailure error){
 	        	  ErrorPanel erorPanel = new ErrorPanel();
 	        	  erorPanel.setErrorMessage(error.getMessage());
 					Log.error(error.getMessage());
 				}
 	          @Override
 				public void onViolation(Set<Violation> errors) {
 					Iterator<Violation> iter = errors.iterator();
 					String message = "";
 					while(iter.hasNext()){
 						message += iter.next().getMessage() + "<br>";
 					}
 					Log.warn(McAppConstant.ERROR_WHILE_DELETE_VIOLATION + " in Antwort löschen -" + message);
 					
 		        	  ErrorPanel erorPanel = new ErrorPanel();
 		        	  erorPanel.setErrorMessage(message);
 					
 
 					
 				}
 		});
 		
 		requestFactory.institutionRequest().findAllInstitutions().fire(new Receiver<List<InstitutionProxy>>(){
 
 			@Override
 			public void onSuccess(List<InstitutionProxy> response) {
 				topPanel.getInstitutionListBox().setAcceptableValues(response);
 				//topPanel.getInstitutionListBox().setValue(response.get(0), true);
 			}
 	          public void onFailure(ServerFailure error){
 	        	  ErrorPanel erorPanel = new ErrorPanel();
 	        	  erorPanel.setErrorMessage(error.getMessage());
 					Log.error(error.getMessage());
 				}
 	          @Override
 				public void onViolation(Set<Violation> errors) {
 					Iterator<Violation> iter = errors.iterator();
 					String message = "";
 					while(iter.hasNext()){
 						message += iter.next().getMessage() + "<br>";
 					}
 					Log.warn(McAppConstant.ERROR_WHILE_DELETE_VIOLATION + " in Antwort löschen -" + message);
 					
 		        	  ErrorPanel erorPanel = new ErrorPanel();
 		        	  erorPanel.setErrorMessage(message);
 					
 
 					
 				}
 		});
 		*/
 		Log.debug("McApp.initAdminAfter");
 	}
 
 
 
 	private void initStudent() {
 		Log.debug("McApp.initStudent()");
 		GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
 			public void onUncaughtException(Throwable e) {
 				Log.warn("Fehler im Exception Handler: "+e.getMessage(), e);
 				log.log(Level.SEVERE, e.getMessage(), e);
 			}
 		});
 
 		if (LogConfiguration.loggingIsEnabled()) {
 			// Add remote logging handler
 			RequestFactoryLogHandler.LoggingRequestProvider provider = new RequestFactoryLogHandler.LoggingRequestProvider() {
 				public LoggingRequest getLoggingRequest() {
 					return requestFactory.loggingRequest();
 				}
 			};
 			Logger.getLogger("").addHandler(
 					new RequestFactoryLogHandler(provider, Level.WARNING,
 							new ArrayList<String>()));
 		}
 
 		RequestEvent.register(eventBus, new RequestEvent.Handler() {
 			// Only show loading status if a request isn't serviced in 250ms.
 			private static final int LOADING_TIMEOUT = 250;
 
 			public void onRequestEvent(RequestEvent requestEvent) {
 				if (requestEvent.getState() == RequestEvent.State.SENT) {
 				//	shell.getMole().showDelayed(LOADING_TIMEOUT);
 				} else {
 			//		shell.getMole().hide();
 				}
 			}
 		});
 
 		
 		Log.debug("McApp.Mapper");
 		
 
 		
 		/*CachingActivityMapper cached = new CachingActivityMapper(mcAppActivitiesMapper);
 		FilterForMainPlaces filterForMainPlaces = new FilterForMainPlaces();
 		ActivityMapper masterActivityMap = new FilteredActivityMapper(filterForMainPlaces, cached);
 		final ActivityManager masterActivityManager = new ActivityManager(masterActivityMap, eventBus);*/
 		
 		
 
 //		ProxyListPlacePicker proxyListPlacePicker = new ProxyListPlacePicker(placeController, proxyPlaceToListPlace);
 //		HasConstrainedValue<ProxyListPlace> listPlacePickerView = shell.getPlacesBox();
 //		listPlacePickerView.setAcceptableValues(getTopPlaces());
 //		proxyListPlacePicker.register(eventBus, listPlacePickerView);
 
 //		final ActivityManager detailsActivityManager = new ActivityManager(applicationDetailsActivities, eventBus);
 
 //		detailsActivityManager.setDisplay(shell.getDetailsPanel());
 		Log.debug("LogBook.History");
 		/* Browser history integration */
 		LogBookPlaceHistoryMapper mapper = GWT.create(LogBookPlaceHistoryMapper.class);
 		mapper.setFactory(logBookPlaceHistoryFactory);
 		PlaceHistoryHandler placeHistoryHandler = new PlaceHistoryHandler(mapper);
 		//ProxyListPlace defaultPlace = getTopPlaces().iterator().next();
 		
 		
 		//placeHistoryHandler.handleCurrentHistory();
 		//new LogBookHeaderLogic(requestFactory, placeController, eventBus);
 		LogBookNav nav = new LogBookNav(requestFactory, placeController, placeHistoryHandler,mcAppActivitiesMapper,eventBus);
 		//masterActivityManager.setDisplay(shell.getLogBookNavSimplePanel());
 		studentShell.setNavigation(nav);
 		//if(placeController.getWhere() instanceof SkillPlace)
 		//{
 		//	nav.masterActivityManager.setDisplay((SimplePanel)nav.getMainLoogBookTabpanel().getWidget(1));
 			
 		//}
 		placeHistoryHandler.register(placeController, eventBus, new LoginPlace());
 		
 		placeHistoryHandler.handleCurrentHistory();
 		//LogBookShell shell = new LogBookShell(requestFactory, placeController, placeHistoryHandler);
 		
 		
 	//	shell.setNavigation(nav);
 		
 		//final TopPanel topPanel = TopPanel.instance(requestFactory, placeController);
 	//	shell.setTopPanel(topPanel);
 		
 		/*requestFactory.personRequest().findAllPeople().fire(new Receiver<List<PersonProxy>>(){
 
 			@Override
 			public void onSuccess(List<PersonProxy> response) {
 				topPanel.getLoggedUser().setAcceptableValues(response);
 				//topPanel.getLoggedUser().setValue(response.get(0), true);
 				
 				
 			}
 	          public void onFailure(ServerFailure error){
 	        	  ErrorPanel erorPanel = new ErrorPanel();
 	        	  erorPanel.setErrorMessage(error.getMessage());
 					Log.error(error.getMessage());
 				}
 	          @Override
 				public void onViolation(Set<Violation> errors) {
 					Iterator<Violation> iter = errors.iterator();
 					String message = "";
 					while(iter.hasNext()){
 						message += iter.next().getMessage() + "<br>";
 					}
 					Log.warn(McAppConstant.ERROR_WHILE_DELETE_VIOLATION + " in Antwort löschen -" + message);
 					
 		        	  ErrorPanel erorPanel = new ErrorPanel();
 		        	  erorPanel.setErrorMessage(message);
 					
 
 					
 				}
 		});
 		
 		requestFactory.institutionRequest().findAllInstitutions().fire(new Receiver<List<InstitutionProxy>>(){
 
 			@Override
 			public void onSuccess(List<InstitutionProxy> response) {
 				topPanel.getInstitutionListBox().setAcceptableValues(response);
 				//topPanel.getInstitutionListBox().setValue(response.get(0), true);
 			}
 	          public void onFailure(ServerFailure error){
 	        	  ErrorPanel erorPanel = new ErrorPanel();
 	        	  erorPanel.setErrorMessage(error.getMessage());
 					Log.error(error.getMessage());
 				}
 	          @Override
 				public void onViolation(Set<Violation> errors) {
 					Iterator<Violation> iter = errors.iterator();
 					String message = "";
 					while(iter.hasNext()){
 						message += iter.next().getMessage() + "<br>";
 					}
 					Log.warn(McAppConstant.ERROR_WHILE_DELETE_VIOLATION + " in Antwort löschen -" + message);
 					
 		        	  ErrorPanel erorPanel = new ErrorPanel();
 		        	  erorPanel.setErrorMessage(message);
 					
 
 					
 				}
 		});
 		*/
 		Log.debug("McApp.initStudentAfter");
 	}
 
 }
