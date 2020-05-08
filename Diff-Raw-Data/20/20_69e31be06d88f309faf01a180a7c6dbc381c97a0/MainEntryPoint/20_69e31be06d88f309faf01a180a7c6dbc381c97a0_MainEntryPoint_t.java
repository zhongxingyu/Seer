 package nl.sense_os.commonsense.main.client;
 
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import nl.sense_os.commonsense.common.client.communication.CommonSenseApi;
 import nl.sense_os.commonsense.common.client.communication.SessionManager;
 import nl.sense_os.commonsense.common.client.communication.httpresponse.CurrentUserResponse;
 import nl.sense_os.commonsense.common.client.event.CurrentUserChangedEvent;
 import nl.sense_os.commonsense.common.client.model.User;
 import nl.sense_os.commonsense.common.client.util.Constants;
 import nl.sense_os.commonsense.main.client.alerts.create.AlertCreateController;
 import nl.sense_os.commonsense.main.client.application.MainApplicationView;
 import nl.sense_os.commonsense.main.client.env.create.EnvCreateController;
 import nl.sense_os.commonsense.main.client.env.list.EnvController;
 import nl.sense_os.commonsense.main.client.env.view.EnvViewController;
 import nl.sense_os.commonsense.main.client.ext.model.ExtUser;
 import nl.sense_os.commonsense.main.client.groups.create.GroupCreateController;
 import nl.sense_os.commonsense.main.client.groups.invite.GroupInviteController;
 import nl.sense_os.commonsense.main.client.groups.join.GroupJoinController;
 import nl.sense_os.commonsense.main.client.groups.leave.GroupLeaveController;
 import nl.sense_os.commonsense.main.client.groups.list.GroupController;
 import nl.sense_os.commonsense.main.client.sensors.SensorsPlace;
 import nl.sense_os.commonsense.main.client.sensors.delete.SensorDeleteController;
 import nl.sense_os.commonsense.main.client.sensors.library.LibraryController;
 import nl.sense_os.commonsense.main.client.sensors.share.SensorShareController;
 import nl.sense_os.commonsense.main.client.sensors.unshare.UnshareController;
 import nl.sense_os.commonsense.main.client.states.connect.StateConnectController;
 import nl.sense_os.commonsense.main.client.states.create.StateCreateController;
 import nl.sense_os.commonsense.main.client.states.defaults.StateDefaultsController;
 import nl.sense_os.commonsense.main.client.states.edit.StateEditController;
 import nl.sense_os.commonsense.main.client.states.feedback.FeedbackController;
 import nl.sense_os.commonsense.main.client.states.list.StateListController;
 import nl.sense_os.commonsense.main.client.viz.data.DataController;
 import nl.sense_os.commonsense.main.client.viz.panels.VizPanelsController;
 import nl.sense_os.commonsense.main.client.viz.tabs.VizMainController;
 
 import com.extjs.gxt.ui.client.GXT;
 import com.extjs.gxt.ui.client.Registry;
 import com.extjs.gxt.ui.client.mvc.Dispatcher;
 import com.extjs.gxt.ui.client.widget.Viewport;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.google.gwt.activity.shared.ActivityManager;
 import com.google.gwt.activity.shared.ActivityMapper;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JsonUtils;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.http.client.UrlBuilder;
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.place.shared.PlaceHistoryHandler;
 import com.google.gwt.place.shared.PlaceHistoryMapper;
 import com.google.gwt.user.client.Window.Location;
 import com.google.gwt.user.client.ui.AcceptsOneWidget;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.web.bindery.event.shared.EventBus;
 
 /**
  * Entry point for the CommonSense web application. Initializes services, prepares the MVC
  * framework, and dispatches the first events to show the application.
  */
 public class MainEntryPoint implements EntryPoint {
 
 	public static final boolean HACK_SKIP_LIB_DETAILS = Constants.ALLOW_HACKS && false;
 	private static final Logger LOG = Logger.getLogger(MainEntryPoint.class.getName());
 	private MainClientFactory clientFactory;
	private PlaceHistoryHandler historyHandler;
 
 	/**
 	 * Redirects the user to the main page
 	 */
 	public static void goToLoginPage() {
 		UrlBuilder builder = new UrlBuilder();
 		builder.setProtocol(Location.getProtocol());
 		builder.setHost(Location.getHost());
 		String path = Location.getPath().contains("index.html") ? Location.getPath().replace(
 				"index.html", "login.html") : Location.getPath() + "login.html";
 		builder.setPath(path);
 		for (Entry<String, List<String>> entry : Location.getParameterMap().entrySet()) {
 			if ("session_id".equals(entry.getKey()) || "error".equals(entry.getKey())) {
 				// do not copy the session id parameter
 			} else {
 				builder.setParameter(entry.getKey(), entry.getValue().toArray(new String[0]));
 			}
 		}
 		Location.replace(builder.buildString().replace("127.0.0.1%3A", "127.0.0.1:"));
 	}
 
 	/**
 	 * Requests the current user's details from CommonSense
 	 */
 	private void getCurrentUser() {
 		LOG.finest("Get current user");
 
 		// prepare request callback
 		RequestCallback callback = new RequestCallback() {
 
 			@Override
 			public void onError(Request request, Throwable exception) {
 				onGetCurrentUserFailure(-1, exception);
 			}
 
 			@Override
 			public void onResponseReceived(Request request, Response response) {
 				onGetCurrentUserResponse(response);
 			}
 		};
 
 		CommonSenseApi.getCurrentUser(callback);
 	}
 
 	/**
 	 * Initializes the event dispatcher by adding the application's controllers to it.
 	 */
 	private void initDispatcher() {
 
 		Dispatcher dispatcher = Dispatcher.get();
 
 		dispatcher.addController(new VizMainController());
 		dispatcher.addController(new VizPanelsController());
 		dispatcher.addController(new DataController());
 
 		// sensor library controllers
 		dispatcher.addController(new LibraryController());
 		dispatcher.addController(new SensorDeleteController());
 		dispatcher.addController(new SensorShareController());
 		dispatcher.addController(new UnshareController());
 
 		// group controllers
 		dispatcher.addController(new GroupController());
 		dispatcher.addController(new GroupCreateController());
 		dispatcher.addController(new GroupJoinController());
 		dispatcher.addController(new GroupLeaveController());
 		dispatcher.addController(new GroupInviteController());
 
 		// state controllers
 		dispatcher.addController(new StateListController());
 		dispatcher.addController(new StateConnectController());
 		dispatcher.addController(new StateCreateController());
 		dispatcher.addController(new StateDefaultsController());
 		dispatcher.addController(new StateEditController());
 		dispatcher.addController(new FeedbackController());
 
 		// environment controllers
 		dispatcher.addController(new EnvController());
 		dispatcher.addController(new EnvCreateController());
 		dispatcher.addController(new EnvViewController());
 
 		dispatcher.addController(new AlertCreateController());
 	}
 
 	/**
 	 * Handles failed request to get the current user details by redirecting to the login page.
 	 * 
 	 * @param code
 	 * @param error
 	 */
 	private void onGetCurrentUserFailure(int code, Throwable error) {
 		LOG.severe("Failed to get current user! Code: " + code + " " + error);
 		MainEntryPoint.goToLoginPage();
 	}
 
 	/**
 	 * Parses the response from CommonSense
 	 * 
 	 * @param response
 	 */
 	private void onGetCurrentUserResponse(Response response) {
 		int statusCode = response.getStatusCode();
 		if (Response.SC_OK == statusCode) {
 			CurrentUserResponse jso = JsonUtils.safeEval(response.getText());
 			onGetCurrentUserSuccess(jso.getUser());
 		} else {
 			onGetCurrentUserFailure(statusCode, new Throwable(response.getStatusText()));
 		}
 	}
 
 	/**
 	 * Handles the new user details
 	 * 
 	 * @param user
 	 */
 	private void onGetCurrentUserSuccess(User user) {
 
 		// store in registry
 		ExtUser extUser = new ExtUser(user);
 		Registry.register(nl.sense_os.commonsense.common.client.util.Constants.REG_USER, extUser);
 
 		// fire event
 		clientFactory.getEventBus().fireEvent(new CurrentUserChangedEvent(user));

		startApplication();
 	}
 
 	@Override
 	public void onModuleLoad() {
 
 		String sessionId = SessionManager.getSessionId();
 		if (null == sessionId) {
 			goToLoginPage();
 		} else {
			init();
 			getCurrentUser();
 		}
 	}
 
 	/**
	 * Initializes the views and starts the default activity.
 	 */
	private void init() {
 
 		// Create ClientFactory using deferred binding
 		clientFactory = GWT.create(MainClientFactory.class);
 		EventBus eventBus = clientFactory.getEventBus();
 		PlaceController placeController = clientFactory.getPlaceController();
 
 		// prepare UI
 		MainApplicationView main = clientFactory.getMainView();
 		eventBus.addHandler(CurrentUserChangedEvent.TYPE, main);
 		AcceptsOneWidget appWidget = main.getActivityPanel();
 
 		// Start ActivityManager for the main widget with our ActivityMapper
 		ActivityMapper activityMapper = new MainActivityMapper(clientFactory);
 		ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
 		activityManager.setDisplay(appWidget);
 
 		// Start PlaceHistoryHandler with our PlaceHistoryMapper
 		PlaceHistoryMapper historyMapper = GWT.create(MainPlaceHistoryMapper.class);
		historyHandler = new PlaceHistoryHandler(historyMapper);
 		historyHandler.register(placeController, eventBus, new SensorsPlace());
 
 		Viewport viewport = new Viewport();
 		viewport.setLayout(new FitLayout());
 		viewport.add(main.asWidget());
 		RootPanel.get().add(viewport);
 
 		/* initialize GXT MVC */
 		initDispatcher();
	}

	private void startApplication() {
 
 		GXT.hideLoadingPanel("loading");
 
 		// Goes to place represented on URL or default place
 		historyHandler.handleCurrentHistory();
 	}
 }
