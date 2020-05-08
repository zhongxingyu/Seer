 package nl.sense_os.commonsense.main.client.states.create;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import nl.sense_os.commonsense.common.client.communication.SessionManager;
 import nl.sense_os.commonsense.common.client.communication.httpresponse.AvailServicesResponse;
 import nl.sense_os.commonsense.common.client.constant.Urls;
 import nl.sense_os.commonsense.common.client.model.Service;
 import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
 import nl.sense_os.commonsense.main.client.ext.model.ExtService;
 import nl.sense_os.commonsense.main.client.sensors.library.LibraryEvents;
 
 import com.extjs.gxt.ui.client.Registry;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.event.EventType;
 import com.extjs.gxt.ui.client.mvc.AppEvent;
 import com.extjs.gxt.ui.client.mvc.Controller;
 import com.extjs.gxt.ui.client.mvc.Dispatcher;
 import com.extjs.gxt.ui.client.mvc.View;
 import com.google.gwt.core.client.JsonUtils;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestBuilder.Method;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.http.client.UrlBuilder;
 
 public class StateCreateController extends Controller {
 
 	private static final Logger LOG = Logger.getLogger(StateCreateController.class.getName());
 	private View creator;
 	private boolean isLoadingSensors;
 
 	public StateCreateController() {
 		registerEventTypes(StateCreateEvents.ShowCreator);
 
 		// get available services for a sensor
 		registerEventTypes(StateCreateEvents.AvailableServicesRequested);
 
 		// load all sensors to create service from
 		registerEventTypes(StateCreateEvents.LoadSensors);
 		registerEventTypes(LibraryEvents.ListUpdated);
 
 		// create state from sensor
 		registerEventTypes(StateCreateEvents.CreateServiceRequested,
 				StateCreateEvents.CreateServiceComplete, StateCreateEvents.CreateServiceCancelled);
 	}
 
 	private void createService(String name, ExtService service, ExtSensor sensor,
 			List<ModelData> dataFields) {
 
 		// prepare request properties
 		final Method method = RequestBuilder.POST;
 		final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
 		urlBuilder.setPath(Urls.PATH_SENSORS + "/" + sensor.getId() + "/services.json");
 		final String url = urlBuilder.buildString();
 		final String sessionId = SessionManager.getSessionId();
 
 		// create request body
 		String serviceJson = "\"service\":{";
 		serviceJson += "\"name\":\"" + service.<String> get(ExtService.NAME) + "\"";
 		serviceJson += ",\"data_fields\":[";
 		for (ModelData dataField : dataFields) {
 			serviceJson += "\"" + dataField.get("text") + "\",";
 		}
 		serviceJson = serviceJson.substring(0, serviceJson.length() - 1) + "]";
 		serviceJson += "}";
 		String sensorJson = "\"sensor\":{";
 		sensorJson += "\"name\":\"" + name + "\"";
 		sensorJson += ",\"device_type\":\"" + name + "\"";
 		sensorJson += "}";
 		final String body = "{" + serviceJson + "," + sensorJson + "}";
 
 		// prepare request callback
 		RequestCallback reqCallback = new RequestCallback() {
 
 			@Override
 			public void onError(Request request, Throwable exception) {
 				LOG.warning("POST sensor service onError callback: " + exception.getMessage());
 				onCreateServiceFailure(0);
 			}
 
 			@Override
 			public void onResponseReceived(Request request, Response response) {
 				LOG.finest("POST sensor service response received: " + response.getStatusText());
 				int statusCode = response.getStatusCode();
 				if (Response.SC_CREATED == statusCode) {
 					onCreateServiceSuccess(response.getText());
 				} else {
 					LOG.warning("POST sensor service returned incorrect status: " + statusCode);
 					onCreateServiceFailure(statusCode);
 				}
 			}
 		};
 
 		// send request
 		try {
 			RequestBuilder builder = new RequestBuilder(method, url);
 			builder.setHeader("X-SESSION_ID", sessionId);
 			builder.setHeader("Content-Type", Urls.HEADER_JSON_TYPE);
 			builder.sendRequest(body, reqCallback);
 		} catch (Exception e) {
 			LOG.warning("POST sensor service request threw exception: " + e.getMessage());
 			reqCallback.onError(null, e);
 		}
 	}
 
 	private void getAvailableServices(ExtSensor sensor) {
 
 		// prepare request properties
 		final Method method = RequestBuilder.GET;
 		final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
 		urlBuilder.setPath(Urls.PATH_SENSORS + "/" + sensor.getId() + "/services/available.json");
 		if (sensor.getAlias() != -1) {
 			urlBuilder.setParameter("alias", "" + sensor.getAlias());
 		}
 		final String url = urlBuilder.buildString();
 		final String sessionId = SessionManager.getSessionId();
 
 		// prepare request callback
 		RequestCallback reqCallback = new RequestCallback() {
 
 			@Override
 			public void onError(Request request, Throwable exception) {
 				LOG.warning("GET sensor services onError callback: " + exception.getMessage());
 				onAvailableServicesFailure();
 			}
 
 			@Override
 			public void onResponseReceived(Request request, Response response) {
 				LOG.finest("GET sensor services response received: " + response.getStatusText());
 				int statusCode = response.getStatusCode();
 				if (Response.SC_OK == statusCode) {
 					onAvailableServicesSuccess(response.getText());
 				} else {
 					LOG.warning("GET sensor services returned incorrect status: " + statusCode);
 					onAvailableServicesFailure();
 				}
 			}
 		};
 
 		// send request
 		try {
 			RequestBuilder builder = new RequestBuilder(method, url);
 			builder.setHeader("X-SESSION_ID", sessionId);
 			builder.sendRequest(null, reqCallback);
 		} catch (Exception e) {
 			LOG.warning("GET sensor services request threw exception: " + e.getMessage());
 			reqCallback.onError(null, e);
 		}
 	}
 
 	@Override
 	public void handleEvent(AppEvent event) {
 		final EventType type = event.getType();
 
 		/*
 		 * Request available services for a sensor
 		 */
 		if (type.equals(StateCreateEvents.AvailableServicesRequested)) {
 			// LOG.fine( "AvailableServicesRequested");
 			final ExtSensor sensor = event.getData("sensor");
 			getAvailableServices(sensor);
 
 		} else
 
 		/*
 		 * Create service from a sensor
 		 */
 		if (type.equals(StateCreateEvents.CreateServiceRequested)) {
 			// LOG.fine( "CreateRequested");
 			final String name = event.<String> getData("name");
 			final ExtService service = event.<ExtService> getData("service");
 			final ExtSensor sensor = event.<ExtSensor> getData("sensor");
 			final List<ModelData> dataFields = event.<List<ModelData>> getData("dataFields");
 			createService(name, service, sensor, dataFields);
 
 		} else
 
 		/*
 		 * Load all sensors to create service from
 		 */
 		if (type.equals(StateCreateEvents.LoadSensors)) {
 			// LOG.fine( "LoadSensors");
 			loadSensors();
 
 		} else if (type.equals(LibraryEvents.ListUpdated)) {
 			if (isLoadingSensors) {
 				// LOG.fine( "Sensor lists updated: LoadSensors");
 				loadSensors();
 			}
 
 		} else
 
 		/*
 		 * Pass rest through to creator view
 		 */
 		{
 			forwardToView(creator, event);
 		}
 	}
 
 	@Override
 	protected void initialize() {
 		super.initialize();
 		creator = new StateCreator(this);
 	}
 
 	/**
 	 * Loads all sensors that can be used to create a new state with.
 	 */
 	// TODO does not signal LoadFailure
 	private void loadSensors() {
 		isLoadingSensors = true;
 
 		List<ExtSensor> sensors = Registry
 				.<List<ExtSensor>> get(nl.sense_os.commonsense.common.client.util.Constants.REG_SENSOR_LIST);
 		if (null == sensors) {
 			Dispatcher.forwardEvent(LibraryEvents.LoadRequest);
 			return;
 		}
 
 		AppEvent success = new AppEvent(StateCreateEvents.LoadSensorsSuccess);
 		success.setData("sensors", sensors);
 		forwardToView(creator, success);
 		isLoadingSensors = false;
 	}
 
 	private void onAvailableServicesFailure() {
 		forwardToView(creator, new AppEvent(StateCreateEvents.AvailableServicesNotUpdated));
 	}
 
 	private void onAvailableServicesSuccess(String response) {
 
 		// parse list of services from response
 		List<Service> services = new ArrayList<Service>();
 		if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
 			AvailServicesResponse jso = JsonUtils.unsafeEval(response);
 			services = jso.getServices();
 		}
 
 		// convert to Ext models
 		List<ExtService> extServices = new ArrayList<ExtService>(services.size());
 		for (Service service : services) {
 			extServices.add(new ExtService(service));
 		}
 
 		AppEvent success = new AppEvent(StateCreateEvents.AvailableServicesUpdated);
		success.setData("services", services);
 		forwardToView(creator, success);
 	}
 
 	private void onCreateServiceFailure(int code) {
 		forwardToView(creator, new AppEvent(StateCreateEvents.CreateServiceFailed));
 	}
 
 	private void onCreateServiceSuccess(String response) {
 
 		// TODO update global sensor list
 
 		// dispatch event
 		Dispatcher.forwardEvent(StateCreateEvents.CreateServiceComplete);
 	}
 }
