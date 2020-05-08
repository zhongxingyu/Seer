 package nl.sense_os.commonsense.client.sensors.library;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import nl.sense_os.commonsense.client.CommonSense;
 import nl.sense_os.commonsense.client.auth.SessionManager;
 import nl.sense_os.commonsense.client.auth.login.LoginEvents;
 import nl.sense_os.commonsense.client.common.constants.Constants;
 import nl.sense_os.commonsense.client.common.constants.Urls;
 import nl.sense_os.commonsense.client.common.models.DeviceModel;
 import nl.sense_os.commonsense.client.common.models.EnvironmentModel;
 import nl.sense_os.commonsense.client.common.models.GroupModel;
 import nl.sense_os.commonsense.client.common.models.SensorModel;
 import nl.sense_os.commonsense.client.common.models.ServiceModel;
 import nl.sense_os.commonsense.client.common.models.UserModel;
 import nl.sense_os.commonsense.client.env.create.EnvCreateEvents;
 import nl.sense_os.commonsense.client.env.list.EnvEvents;
 import nl.sense_os.commonsense.client.groups.list.GetGroupsResponseJso;
 import nl.sense_os.commonsense.client.main.MainEvents;
 import nl.sense_os.commonsense.client.sensors.delete.SensorDeleteEvents;
 import nl.sense_os.commonsense.client.sensors.share.SensorShareEvents;
 import nl.sense_os.commonsense.client.sensors.unshare.UnshareEvents;
 import nl.sense_os.commonsense.client.states.create.StateCreateEvents;
 import nl.sense_os.commonsense.client.states.defaults.StateDefaultsEvents;
 import nl.sense_os.commonsense.client.states.list.StateListEvents;
 import nl.sense_os.commonsense.client.viz.tabs.VizEvents;
 
 import com.extjs.gxt.ui.client.Registry;
 import com.extjs.gxt.ui.client.data.BaseListLoadResult;
 import com.extjs.gxt.ui.client.data.ListLoadResult;
 import com.extjs.gxt.ui.client.event.EventType;
 import com.extjs.gxt.ui.client.mvc.AppEvent;
 import com.extjs.gxt.ui.client.mvc.Controller;
 import com.extjs.gxt.ui.client.mvc.Dispatcher;
 import com.extjs.gxt.ui.client.mvc.View;
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.core.client.JsonUtils;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestBuilder.Method;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.http.client.UrlBuilder;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 public class LibraryController extends Controller {
 
     private final static Logger LOG = Logger.getLogger(LibraryController.class.getName());
     private static final int PER_PAGE = 1000;
     private View grid;
     private boolean isLoadingList;
     private boolean isLoadingUsers;
     private boolean isLoadingServices;
 
     public LibraryController() {
 
 	// LOG.setLevel(Level.WARNING);
 
 	registerEventTypes(MainEvents.Init);
 	registerEventTypes(LoginEvents.LoggedOut);
 	registerEventTypes(VizEvents.Show);
 
 	registerEventTypes(LibraryEvents.ShowLibrary, LibraryEvents.LoadRequest,
 		LibraryEvents.ListUpdated);
 
 	// external events
 	registerEventTypes(SensorDeleteEvents.DeleteSuccess, SensorDeleteEvents.DeleteFailure);
 	registerEventTypes(SensorShareEvents.ShareComplete);
 	registerEventTypes(UnshareEvents.UnshareComplete);
 	registerEventTypes(StateCreateEvents.CreateServiceComplete, StateListEvents.RemoveComplete,
 		StateDefaultsEvents.CheckDefaultsSuccess);
 	registerEventTypes(EnvCreateEvents.CreateSuccess, EnvEvents.DeleteSuccess);
     }
 
     private List<DeviceModel> devicesFromLibrary(List<SensorModel> library) {
 	LOG.finest("Listing devices...");
 	List<DeviceModel> devices = new ArrayList<DeviceModel>();
 
 	// gather the devices of all sensors in the library
 	DeviceModel device;
 	for (SensorModel sensor : library) {
 	    device = sensor.getDevice();
 	    if (device != null && !devices.contains(device)) {
 		devices.add(device);
 		LOG.fine("Device: " + device);
 	    }
 	}
 
 	return devices;
     }
 
     /**
      * Requests a list of all available services for all sensors the user owns.
      * 
      * @param groupId
      *            Optional parameter to get the available services for sensors that are not shared
      *            directly with the user but with a group.
      */
     private void getAvailableServices(final int page, final String groupId) {
 
 	isLoadingServices = true;
 	notifyState();
 
 	// prepare request properties
 	final Method method = RequestBuilder.GET;
 	final UrlBuilder urlBuilder = new UrlBuilder();
 	urlBuilder.setHost(Urls.HOST);
 	urlBuilder.setPath(Urls.PATH_SENSORS + "/services/available.json");
 	if (groupId != null && groupId.length() > 0) {
 	    urlBuilder.setParameter("group_id", groupId);
 	}
 	urlBuilder.setParameter("per_page", "" + PER_PAGE);
 	urlBuilder.setParameter("page", "" + page);
 	final String url = urlBuilder.buildString();
 	final String sessionId = SessionManager.getSessionId();
 
 	// prepare request callback
 	RequestCallback reqCallback = new RequestCallback() {
 
 	    @Override
 	    public void onError(Request request, Throwable exception) {
 		LOG.warning("GET available services error callback: " + exception.getMessage());
 		onAvailServicesFailure();
 	    }
 
 	    @Override
 	    public void onResponseReceived(Request request, Response response) {
 		LOG.finest("GET available services response received: " + response.getStatusText());
 		int statusCode = response.getStatusCode();
 		if (Response.SC_OK == statusCode) {
 		    onAvailServicesSuccess(response.getText(), page, groupId);
 		} else if (Response.SC_NO_CONTENT == statusCode) {
 		    onAvailServicesSuccess(null, page, groupId);
 		} else {
 		    LOG.warning("GET available services returned incorrect status: " + statusCode);
 		    onAvailServicesFailure();
 		}
 	    }
 	};
 
 	// send request
 	RequestBuilder builder = new RequestBuilder(method, url);
 	builder.setHeader("X-SESSION_ID", sessionId);
 	try {
 	    builder.sendRequest(null, reqCallback);
 	} catch (RequestException e) {
 	    LOG.warning("GET  available services request threw exception: " + e.getMessage());
 	    onAvailServicesFailure();
 	}
     }
 
     private void getGroups(final List<SensorModel> library,
 	    final AsyncCallback<ListLoadResult<SensorModel>> callback) {
 
 	// prepare request properties
 	final UrlBuilder urlBuilder = new UrlBuilder();
 	urlBuilder.setHost(Urls.HOST).setPath(Urls.PATH_GROUPS + ".json");
 	urlBuilder.setParameter("per_page", "" + PER_PAGE);
 	final String url = urlBuilder.buildString();
 	final String sessionId = SessionManager.getSessionId();
 
 	// prepare request callback
 	RequestCallback reqCallback = new RequestCallback() {
 
 	    @Override
 	    public void onError(Request request, Throwable exception) {
 		LOG.warning("GET groups onError callback: " + exception.getMessage());
 		onGroupsFailure(callback);
 	    }
 
 	    @Override
 	    public void onResponseReceived(Request request, Response response) {
 		LOG.finest("GET groups response received: " + response.getStatusText());
 		int statusCode = response.getStatusCode();
 		if (Response.SC_OK == statusCode) {
 		    onGroupsSuccess(response.getText(), library, callback);
 		} else if (Response.SC_NO_CONTENT == statusCode) {
 		    // no content
 		    onGroupsSuccess(null, library, callback);
 		} else {
 		    LOG.warning("GET groups returned incorrect status: " + statusCode);
 		    onGroupsFailure(callback);
 		}
 	    }
 	};
 
 	// send request
 	RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
 	builder.setHeader("X-SESSION_ID", sessionId);
 	try {
 	    builder.sendRequest(null, reqCallback);
 	} catch (RequestException e) {
 	    LOG.warning("GET groups request threw exception: " + e.getMessage());
 	    onGroupsFailure(callback);
 	}
     }
 
     private void getGroupSensors(final List<GroupModel> groups, final int index, final int page,
 	    final List<SensorModel> library,
 	    final AsyncCallback<ListLoadResult<SensorModel>> callback) {
 
 	if (index < groups.size()) {
 
 	    int groupId = groups.get(index).getId();
 
 	    // prepare request properties
 	    final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
 	    urlBuilder.setPath(Urls.PATH_SENSORS + ".json");
	    urlBuilder.setParameter("per_page", "" + PER_PAGE);
	    urlBuilder.setParameter("page", "" + page);
 	    urlBuilder.setParameter("details", "full");
 	    urlBuilder.setParameter("group_id", Integer.toString(groupId));
 	    final String url = urlBuilder.buildString();
 	    final String sessionId = SessionManager.getSessionId();
 
 	    // prepare request callback
 	    RequestCallback reqCallback = new RequestCallback() {
 
 		@Override
 		public void onError(Request request, Throwable exception) {
 		    LOG.warning("GET group sensors onError callback: " + exception.getMessage());
 		    onGroupSensorsFailure(callback);
 		}
 
 		@Override
 		public void onResponseReceived(Request request, Response response) {
 		    LOG.finest("GET group sensors response received: " + response.getStatusText());
		    int statusCode = response.getStatusCode();
		    if (Response.SC_OK == statusCode) {
 			onGroupSensorsSuccess(response.getText(), groups, index, page, library,
 				callback);
		    } else if (Response.SC_NO_CONTENT == statusCode) {
 			// no content
 			onGroupSensorsSuccess(null, groups, index, page, library, callback);
		    } else {
			LOG.warning("GET group sensors returned incorrect status: " + statusCode);
 			onGroupSensorsFailure(callback);
 		    }
 		}
 	    };
 
 	    // send request
 	    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
 	    builder.setHeader("X-SESSION_ID", sessionId);
 	    try {
 		builder.sendRequest(null, reqCallback);
 	    } catch (RequestException e) {
 		LOG.warning("GET group sensors request threw exception: " + e.getMessage());
 		reqCallback.onError(null, e);
 	    }
 
 	} else {
 
 	    // notify the view that the list is complete
 	    onLoadComplete(library, callback);
 	}
     }
 
     private void getSensors(final List<SensorModel> library, final int page, final boolean shared,
 	    final AsyncCallback<ListLoadResult<SensorModel>> callback) {
 
 	// prepare request properties
 	final UrlBuilder urlBuilder = new UrlBuilder();
 	urlBuilder.setHost(Urls.HOST).setPath(Urls.PATH_SENSORS + ".json");
 	urlBuilder.setParameter("per_page", "" + PER_PAGE);
 	urlBuilder.setParameter("page", "" + page);
 	urlBuilder.setParameter("details", "full");
 	if (shared) {
 	    urlBuilder.setParameter("shared", "1");
 	}
 	final String url = urlBuilder.buildString();
 	final String sessionId = SessionManager.getSessionId();
 
 	// prepare request callback
 	RequestCallback reqCallback = new RequestCallback() {
 
 	    @Override
 	    public void onError(Request request, Throwable exception) {
 		LOG.warning("GET sensors error callback: " + exception.getMessage());
 		onSensorsFailure(callback);
 	    }
 
 	    @Override
 	    public void onResponseReceived(Request request, Response response) {
 		LOG.finest("GET sensors response received: " + response.getStatusText());
 		int statusCode = response.getStatusCode();
 		if (Response.SC_OK == statusCode) {
 		    onSensorsResponse(response.getText(), library, page, shared, callback);
 		} else if (Response.SC_NO_CONTENT == statusCode) {
 		    onSensorsResponse(null, library, page, shared, callback);
 		} else {
 		    LOG.warning("GET sensors returned incorrect status: " + statusCode);
 		    onSensorsFailure(callback);
 		}
 	    }
 	};
 
 	// send request
 	RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
 	builder.setHeader("X-SESSION_ID", sessionId);
 	try {
 	    builder.sendRequest(null, reqCallback);
 	} catch (RequestException e) {
 	    LOG.warning("GET sensors request threw exception: " + e.getMessage());
 	    reqCallback.onError(null, e);
 	}
     }
 
     @Override
     public void handleEvent(AppEvent event) {
 	final EventType type = event.getType();
 
 	if (type.equals(LibraryEvents.LoadRequest)) {
 	    LOG.finest("LoadRequest");
 	    final AsyncCallback<ListLoadResult<SensorModel>> callback = event.getData("callback");
 	    final boolean renewCache = event.getData("renewCache");
 	    onLoadRequest(renewCache, callback);
 
 	} else
 
 	/*
 	 * Clear data after logout
 	 */
 	if (type.equals(LoginEvents.LoggedOut)) {
 	    LOG.finest("LoggedOut");
 	    onLogout();
 
 	} else
 
 	/*
 	 * Pass through to view
 	 */
 	{
 	    LOG.finest("Pass through to grid");
 	    forwardToView(this.grid, event);
 	}
 
     }
 
     @Override
     protected void initialize() {
 	super.initialize();
 	this.grid = new LibraryGrid(this);
 
 	// initialize library and lists of devices and environments
 	Registry.register(Constants.REG_SENSOR_LIST, new ArrayList<SensorModel>());
 	Registry.register(Constants.REG_DEVICE_LIST, new ArrayList<DeviceModel>());
     }
 
     private void notifyState() {
 	if (isLoadingList || isLoadingUsers || isLoadingServices) {
 	    forwardToView(this.grid, new AppEvent(LibraryEvents.Working));
 	} else {
 	    forwardToView(this.grid, new AppEvent(LibraryEvents.Done));
 	}
 
     }
 
     private void onAvailServicesFailure() {
 	isLoadingServices = false;
 	notifyState();
     }
 
     private void onAvailServicesSuccess(String response, int page, String groupId) {
 
 	List<SensorModel> library = Registry.get(Constants.REG_SENSOR_LIST);
 
 	// parse list of services from response
 	if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
 	    AvailServicesResponseJso jso = JsonUtils.unsafeEval(response);
 	    JsArray<AvailServicesResponseEntryJso> entries = jso.getEntries();
 	    for (int i = 0; i < entries.length(); i++) {
 		int id = entries.get(i).getSensorId();
 		List<ServiceModel> availServices = entries.get(i).getServices();
 		for (SensorModel sensor : library) {
 		    if (sensor.getId() == id) {
 			sensor.setAvailServices(availServices);
 		    }
 		}
 	    }
 
 	    if (entries.length() < jso.getTotal()) {
 		page++;
 		getAvailableServices(page, groupId);
 		return;
 	    }
 	}
 
 	isLoadingServices = false;
 	notifyState();
     }
 
     private void onGroupSensorsFailure(AsyncCallback<ListLoadResult<SensorModel>> callback) {
 	onLoadFailure(callback);
     }
 
     private void onGroupSensorsSuccess(String response, List<GroupModel> groups, int index,
 	    int page, List<SensorModel> library, AsyncCallback<ListLoadResult<SensorModel>> callback) {
 	LOG.fine("Received group sensors response...");
 
 	// parse group sensors
 	List<SensorModel> groupSensors = new ArrayList<SensorModel>();
 	int total = 0;
 	if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
 	    GetSensorsResponseJso responseJso = JsonUtils.unsafeEval(response);
 	    groupSensors.addAll(responseJso.getSensors());
 	    total = responseJso.getTotal();
 	}
 
 	LOG.finest("Parsed group sensors...");
 
 	GroupModel group = groups.get(index);
 	for (SensorModel groupSensor : groupSensors) {
 	    if (!library.contains(groupSensor)) {
 		// set SensorModel.ALIAS property
 		groupSensor.setAlias(group.getId());
 		library.add(groupSensor);
 	    }
 	}
 
 	int retrieved = page * PER_PAGE + groupSensors.size();
 	if (total > retrieved) {
 	    // not all sensors from the group are retrieved yet
 	    page++;
 	    getGroupSensors(groups, index, page, library, callback);
 
 	} else {
 	    if (!CommonSense.HACK_SKIP_LIB_DETAILS && groupSensors.size() > 0) {
 		// get available services from the group sensors
 		getAvailableServices(0, "" + group.getId());
 	    }
 
 	    // next group
 	    index++;
 	    getGroupSensors(groups, index, 0, library, callback);
 	}
     }
 
     private void onGroupsFailure(AsyncCallback<ListLoadResult<SensorModel>> callback) {
 	onLoadFailure(callback);
     }
 
     private void onGroupsSuccess(String response, List<SensorModel> library,
 	    AsyncCallback<ListLoadResult<SensorModel>> callback) {
 
 	// parse list of groups from the response
 	List<GroupModel> groups = new ArrayList<GroupModel>();
 	if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
 	    GetGroupsResponseJso jso = JsonUtils.unsafeEval(response);
 	    groups = jso.getGroups();
 	}
 
 	getGroupSensors(groups, 0, 0, library, callback);
     }
 
     private void onLoadComplete(List<SensorModel> library,
 	    AsyncCallback<ListLoadResult<SensorModel>> callback) {
 	LOG.fine("Load complete...");
 
 	// update list of devices
 	Registry.<List<DeviceModel>> get(Constants.REG_DEVICE_LIST).clear();
 	Registry.<List<DeviceModel>> get(Constants.REG_DEVICE_LIST).addAll(
 		devicesFromLibrary(library));
 
 	isLoadingList = false;
 	notifyState();
 
 	if (null != callback) {
 	    LOG.finest("Create load result...");
 	    ListLoadResult<SensorModel> result = new BaseListLoadResult<SensorModel>(library);
 
 	    LOG.finest("Call back with load result...");
 	    callback.onSuccess(result);
 	}
 	LOG.finest("Dispatch ListUpdated...");
 	Dispatcher.forwardEvent(LibraryEvents.ListUpdated);
     }
 
     private void onLoadFailure(AsyncCallback<ListLoadResult<SensorModel>> callback) {
 	Registry.<List<SensorModel>> get(Constants.REG_SENSOR_LIST).clear();
 	Registry.<List<DeviceModel>> get(Constants.REG_DEVICE_LIST).clear();
 	Registry.<List<EnvironmentModel>> get(Constants.REG_ENVIRONMENT_LIST).clear();
 
 	Dispatcher.forwardEvent(LibraryEvents.ListUpdated);
 	forwardToView(this.grid, new AppEvent(LibraryEvents.Done));
 
 	if (null != callback) {
 	    callback.onFailure(null);
 	}
     }
 
     private void onLoadRequest(boolean renewCache,
 	    AsyncCallback<ListLoadResult<SensorModel>> callback) {
 
 	List<SensorModel> library = Registry.get(Constants.REG_SENSOR_LIST);
 	if (renewCache) {
 	    library.clear();
 	    Registry.<List<DeviceModel>> get(Constants.REG_DEVICE_LIST).clear();
 
 	    isLoadingList = true;
 	    notifyState();
 
 	    getSensors(library, 0, false, callback);
 	} else {
 	    onLoadComplete(library, callback);
 	}
     }
 
     /**
      * Clears the library and lists of devices.
      */
     private void onLogout() {
 	List<SensorModel> library = Registry.get(Constants.REG_SENSOR_LIST);
 	library.clear();
 
 	List<DeviceModel> devices = Registry.get(Constants.REG_DEVICE_LIST);
 	devices.clear();
     }
 
     private void onSensorsFailure(AsyncCallback<ListLoadResult<SensorModel>> callback) {
 	onLoadFailure(callback);
     }
 
     private void onSensorsResponse(String response, List<SensorModel> library, int page,
 	    boolean shared, AsyncCallback<ListLoadResult<SensorModel>> callback) {
 
 	// different callbacks for shared or unshared requests
 	if (shared) {
 	    onSharedSensorsSuccess(response, library, page, callback);
 	} else {
 	    onUnsharedSensorsSuccess(response, library, page, callback);
 	}
     }
 
     private void onSharedSensorsSuccess(String response, List<SensorModel> library, int page,
 	    AsyncCallback<ListLoadResult<SensorModel>> callback) {
 
 	// parse response
 	int total = library.size();
 	if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
 
 	    GetSensorsResponseJso responseJso = JsonUtils.unsafeEval(response);
 	    total = responseJso.getTotal();
 
 	    UserModel user = Registry.<UserModel> get(Constants.REG_USER);
 	    for (SensorModel sharedSensor : responseJso.getSensors()) {
 		sharedSensor.getUsers().add(user);
 		library.remove(sharedSensor);
 		library.add(sharedSensor);
 	    }
 	}
 
 	LOG.fine("total: " + total + ", library size: " + library.size());
 
 	if (total > library.size()) {
 	    // get the next page with sensors
 	    page++;
 	    getSensors(library, page, true, callback);
 
 	} else {
 	    // request full details for my own sensors
 	    if (!CommonSense.HACK_SKIP_LIB_DETAILS) {
 		getAvailableServices(0, null);
 	    }
 
 	    // continue by getting the group sensors
 	    getGroups(library, callback);
 	}
     }
 
     private void onUnsharedSensorsSuccess(String response, List<SensorModel> library, int page,
 	    AsyncCallback<ListLoadResult<SensorModel>> callback) {
 
 	// parse response
 	int total = library.size();
 	if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
 	    GetSensorsResponseJso responseJso = JsonUtils.unsafeEval(response);
 	    total = responseJso.getTotal();
 	    library.addAll(responseJso.getSensors());
 	}
 
 	LOG.fine("total: " + total + ", library size: " + library.size());
 
 	if (total > library.size()) {
 	    // get the next page with sensors
 	    page++;
 	    getSensors(library, page, false, callback);
 
 	} else {
 	    // continue by getting the shared sensors
 	    getSensors(library, page, true, callback);
 	}
     }
 }
