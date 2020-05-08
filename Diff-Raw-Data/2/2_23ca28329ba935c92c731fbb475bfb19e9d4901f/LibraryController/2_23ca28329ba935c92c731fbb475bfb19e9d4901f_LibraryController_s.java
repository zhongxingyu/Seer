 package nl.sense_os.commonsense.main.client.sensors.library;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import nl.sense_os.commonsense.common.client.communication.CommonSenseApi;
 import nl.sense_os.commonsense.common.client.communication.httpresponse.AvailServicesResponseEntry;
 import nl.sense_os.commonsense.common.client.communication.httpresponse.BatchAvailServicesResponse;
 import nl.sense_os.commonsense.common.client.communication.httpresponse.GetGroupsResponse;
 import nl.sense_os.commonsense.common.client.communication.httpresponse.GetSensorsResponse;
 import nl.sense_os.commonsense.common.client.model.Group;
 import nl.sense_os.commonsense.common.client.model.Sensor;
 import nl.sense_os.commonsense.common.client.model.Service;
 import nl.sense_os.commonsense.main.client.MainEntryPoint;
 import nl.sense_os.commonsense.main.client.env.create.EnvCreateEvents;
 import nl.sense_os.commonsense.main.client.env.list.EnvEvents;
 import nl.sense_os.commonsense.main.client.ext.model.ExtDevice;
 import nl.sense_os.commonsense.main.client.ext.model.ExtEnvironment;
 import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
 import nl.sense_os.commonsense.main.client.ext.model.ExtService;
 import nl.sense_os.commonsense.main.client.ext.model.ExtUser;
 import nl.sense_os.commonsense.main.client.sensors.delete.SensorDeleteEvents;
 import nl.sense_os.commonsense.main.client.sensors.share.SensorShareEvents;
 import nl.sense_os.commonsense.main.client.sensors.unshare.UnshareEvents;
 import nl.sense_os.commonsense.main.client.states.create.StateCreateEvents;
 import nl.sense_os.commonsense.main.client.states.defaults.StateDefaultsEvents;
 import nl.sense_os.commonsense.main.client.states.list.StateListEvents;
 import nl.sense_os.commonsense.main.client.viz.tabs.VizEvents;
 
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
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.Response;
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
 
 	private List<ExtDevice> devicesFromLibrary(List<ExtSensor> library) {
 		LOG.finest("Listing devices...");
 		List<ExtDevice> devices = new ArrayList<ExtDevice>();
 
 		// gather the devices of all sensors in the library
 		ExtDevice device;
 		for (ExtSensor sensor : library) {
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
 		CommonSenseApi.getAvailableServices(reqCallback, Integer.toString(PER_PAGE),
 				Integer.toString(page), groupId);
 	}
 
 	private void getGroups(final List<ExtSensor> library,
 			final AsyncCallback<ListLoadResult<ExtSensor>> callback) {
 
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
 
 		CommonSenseApi.getGroups(reqCallback, Integer.toString(PER_PAGE), null);
 	}
 
 	private void getGroupSensors(final List<Group> groups, final int index, final int page,
 			final List<ExtSensor> library, final AsyncCallback<ListLoadResult<ExtSensor>> callback) {
 
 		if (index < groups.size()) {
 
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
 					switch (response.getStatusCode()) {
 					case Response.SC_OK:
 						onGroupSensorsSuccess(response.getText(), groups, index, page, library,
 								callback);
 						break;
 					case Response.SC_NO_CONTENT:
 						// fall through
 					case Response.SC_FORBIDDEN:
 						// no content
 						onGroupSensorsSuccess(null, groups, index, page, library, callback);
 						break;
 					default:
 						LOG.warning("GET group sensors returned incorrect status: "
 								+ response.getStatusCode());
 						onGroupSensorsFailure(callback);
 					}
 				}
 			};
 
 			int groupId = groups.get(index).getId();
 
 			CommonSenseApi.getSensors(reqCallback, Integer.toString(PER_PAGE),
 					Integer.toString(page), null, null, null, "full", Integer.toString(groupId));
 
 		} else {
 
 			// notify the view that the list is complete
 			onLoadComplete(library, callback);
 		}
 	}
 
 	private void getSensors(final List<ExtSensor> library, final int page, final boolean shared,
 			final AsyncCallback<ListLoadResult<ExtSensor>> callback) {
 
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
 
 		CommonSenseApi.getSensors(reqCallback, Integer.toString(PER_PAGE), Integer.toString(page),
 				shared ? "1" : null, null, null, "full", null);
 	}
 
 	@Override
 	public void handleEvent(AppEvent event) {
 		final EventType type = event.getType();
 
 		if (type.equals(LibraryEvents.LoadRequest)) {
 			LOG.finest("LoadRequest");
 			final AsyncCallback<ListLoadResult<ExtSensor>> callback = event.getData("callback");
 			final boolean renewCache = event.getData("renewCache");
 			onLoadRequest(renewCache, callback);
 
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
 		Registry.register(nl.sense_os.commonsense.common.client.util.Constants.REG_SENSOR_LIST,
 				new ArrayList<ExtSensor>());
 		Registry.register(nl.sense_os.commonsense.common.client.util.Constants.REG_DEVICE_LIST,
 				new ArrayList<ExtDevice>());
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
 
 		List<ExtSensor> library = Registry
 				.get(nl.sense_os.commonsense.common.client.util.Constants.REG_SENSOR_LIST);
 
 		// parse list of services from response
 		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
 			BatchAvailServicesResponse jso = JsonUtils.unsafeEval(response);
 			JsArray<AvailServicesResponseEntry> entries = jso.getEntries();
 			for (int i = 0; i < entries.length(); i++) {
 				int id = entries.get(i).getSensorId();
 				List<Service> availServices = entries.get(i).getServices();
 				List<ExtService> extServices = new ArrayList<ExtService>();
 				for (Service service : availServices) {
 					extServices.add(new ExtService(service));
 				}
 				for (ExtSensor sensor : library) {
 					if (sensor.getId() == id) {
 						sensor.setAvailServices(extServices);
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
 
 	private void onGroupSensorsFailure(AsyncCallback<ListLoadResult<ExtSensor>> callback) {
 		onLoadFailure(callback);
 	}
 
 	private void onGroupSensorsSuccess(String response, List<Group> groups, int index, int page,
 			List<ExtSensor> library, AsyncCallback<ListLoadResult<ExtSensor>> callback) {
 		LOG.fine("Received group sensors response...");
 
 		// parse group sensors
		JsArray<Sensor> groupSensors = null;
 		int total = 0;
 		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
 			GetSensorsResponse responseJso = JsonUtils.unsafeEval(response);
 			groupSensors = responseJso.getRawSensors();
 			total = responseJso.getTotal();
 		}
 
 		LOG.finest("Parsed group sensors...");
 
 		Group group = groups.get(index);
 		for (int i = 0; i < groupSensors.length(); i++) {
 			ExtSensor groupSensor = new ExtSensor(groupSensors.get(i));
 			if (!library.contains(groupSensor)) {
 				// set SensorModel.ALIAS property
 				groupSensor.setAlias(group.getId());
 				library.add(groupSensor);
 			}
 		}
 
 		int retrieved = page * PER_PAGE + groupSensors.length();
 		if (total > retrieved) {
 			// not all sensors from the group are retrieved yet
 			page++;
 			getGroupSensors(groups, index, page, library, callback);
 
 		} else {
 			if (!MainEntryPoint.HACK_SKIP_LIB_DETAILS && groupSensors.length() > 0) {
 				// get available services from the group sensors
 				getAvailableServices(0, "" + group.getId());
 			}
 
 			// next group
 			index++;
 			getGroupSensors(groups, index, 0, library, callback);
 		}
 	}
 
 	private void onGroupsFailure(AsyncCallback<ListLoadResult<ExtSensor>> callback) {
 		onLoadFailure(callback);
 	}
 
 	private void onGroupsSuccess(String response, List<ExtSensor> library,
 			AsyncCallback<ListLoadResult<ExtSensor>> callback) {
 
 		// parse list of groups from the response
 		List<Group> groups = new ArrayList<Group>();
 		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
 			GetGroupsResponse jso = JsonUtils.unsafeEval(response);
 			groups = jso.getGroups();
 		}
 
 		getGroupSensors(groups, 0, 0, library, callback);
 	}
 
 	private void onLoadComplete(List<ExtSensor> library,
 			AsyncCallback<ListLoadResult<ExtSensor>> callback) {
 		LOG.fine("Load complete...");
 
 		// update list of devices
 		Registry.<List<ExtDevice>> get(
 				nl.sense_os.commonsense.common.client.util.Constants.REG_DEVICE_LIST).clear();
 		Registry.<List<ExtDevice>> get(
 				nl.sense_os.commonsense.common.client.util.Constants.REG_DEVICE_LIST).addAll(
 				devicesFromLibrary(library));
 
 		isLoadingList = false;
 		notifyState();
 
 		if (null != callback) {
 			LOG.finest("Create load result...");
 			ListLoadResult<ExtSensor> result = new BaseListLoadResult<ExtSensor>(library);
 
 			LOG.finest("Call back with load result...");
 			callback.onSuccess(result);
 		}
 		LOG.finest("Dispatch ListUpdated...");
 		Dispatcher.forwardEvent(LibraryEvents.ListUpdated);
 	}
 
 	private void onLoadFailure(AsyncCallback<ListLoadResult<ExtSensor>> callback) {
 		Registry.<List<ExtSensor>> get(
 				nl.sense_os.commonsense.common.client.util.Constants.REG_SENSOR_LIST).clear();
 		Registry.<List<ExtDevice>> get(
 				nl.sense_os.commonsense.common.client.util.Constants.REG_DEVICE_LIST).clear();
 		Registry.<List<ExtEnvironment>> get(
 				nl.sense_os.commonsense.common.client.util.Constants.REG_ENVIRONMENT_LIST).clear();
 
 		Dispatcher.forwardEvent(LibraryEvents.ListUpdated);
 		forwardToView(this.grid, new AppEvent(LibraryEvents.Done));
 
 		if (null != callback) {
 			callback.onFailure(null);
 		}
 	}
 
 	private void onLoadRequest(boolean renewCache, AsyncCallback<ListLoadResult<ExtSensor>> callback) {
 
 		List<ExtSensor> library = Registry
 				.get(nl.sense_os.commonsense.common.client.util.Constants.REG_SENSOR_LIST);
 		if (renewCache) {
 			library.clear();
 			Registry.<List<ExtDevice>> get(
 					nl.sense_os.commonsense.common.client.util.Constants.REG_DEVICE_LIST).clear();
 
 			isLoadingList = true;
 			notifyState();
 
 			getSensors(library, 0, false, callback);
 		} else {
 			onLoadComplete(library, callback);
 		}
 	}
 
 	private void onSensorsFailure(AsyncCallback<ListLoadResult<ExtSensor>> callback) {
 		onLoadFailure(callback);
 	}
 
 	private void onSensorsResponse(String response, List<ExtSensor> library, int page,
 			boolean shared, AsyncCallback<ListLoadResult<ExtSensor>> callback) {
 
 		// different callbacks for shared or unshared requests
 		if (shared) {
 			onSharedSensorsSuccess(response, library, page, callback);
 		} else {
 			onUnsharedSensorsSuccess(response, library, page, callback);
 		}
 	}
 
 	private void onSharedSensorsSuccess(String response, List<ExtSensor> library, int page,
 			AsyncCallback<ListLoadResult<ExtSensor>> callback) {
 
 		// parse response
 		int total = library.size();
 		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
 
 			GetSensorsResponse responseJso = JsonUtils.unsafeEval(response);
 			total = responseJso.getTotal();
 
 			ExtUser user = Registry
 					.<ExtUser> get(nl.sense_os.commonsense.common.client.util.Constants.REG_USER);
 			JsArray<Sensor> sharedSensors = responseJso.getRawSensors();
 			for (int i = 0; i < sharedSensors.length(); i++) {
 				ExtSensor sharedSensor = new ExtSensor(sharedSensors.get(i));
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
 			if (!MainEntryPoint.HACK_SKIP_LIB_DETAILS) {
 				getAvailableServices(0, null);
 			}
 
 			// continue by getting the group sensors
 			getGroups(library, callback);
 		}
 	}
 
 	private void onUnsharedSensorsSuccess(String response, List<ExtSensor> library, int page,
 			AsyncCallback<ListLoadResult<ExtSensor>> callback) {
 
 		// parse response
 		int total = library.size();
 		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
 			GetSensorsResponse responseJso = JsonUtils.unsafeEval(response);
 			total = responseJso.getTotal();
 			JsArray<Sensor> sensors = responseJso.getRawSensors();
 			for (int i = 0; i < sensors.length(); i++) {
 				ExtSensor sensor = new ExtSensor(sensors.get(i));
 				library.add(sensor);
 			}
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
