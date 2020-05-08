 package nl.sense_os.commonsense.main.client.states.defaults;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 import nl.sense_os.commonsense.common.client.communication.SessionManager;
 import nl.sense_os.commonsense.common.client.constant.Urls;
 import nl.sense_os.commonsense.main.client.ext.model.ExtDevice;
 import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
 
 import com.extjs.gxt.ui.client.Registry;
 import com.extjs.gxt.ui.client.event.EventType;
 import com.extjs.gxt.ui.client.mvc.AppEvent;
 import com.extjs.gxt.ui.client.mvc.Controller;
 import com.extjs.gxt.ui.client.mvc.Dispatcher;
 import com.extjs.gxt.ui.client.mvc.View;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestBuilder.Method;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.http.client.UrlBuilder;
 
 public class StateDefaultsController extends Controller {
 
 	private static final Logger LOG = Logger.getLogger(StateDefaultsController.class.getName());
 
 	public StateDefaultsController() {
 		registerEventTypes(StateDefaultsEvents.CheckDefaults,
 				StateDefaultsEvents.CheckDefaultsRequest, StateDefaultsEvents.CheckDefaultsSuccess);
 	}
 
 	private void checkDefaults(List<ExtDevice> devices, boolean overwrite, final View source) {
 
 		// prepare request properties
 		final Method method = RequestBuilder.POST;
 		final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
        urlBuilder.setPath(Urls.PATH_STATES + "/default.json");
 		final String url = urlBuilder.buildString();
 		final String sessionId = SessionManager.getSessionId();
 
 		// prepare body
 		String body = "{\"sensors\":[";
 		List<ExtSensor> sensors = Registry
 				.get(nl.sense_os.commonsense.common.client.util.Constants.REG_SENSOR_LIST);
 		for (ExtSensor sensor : sensors) {
 			ExtDevice sensorDevice = sensor.getDevice();
 			if (sensorDevice != null && devices.contains(sensorDevice)) {
 				body += "\"" + sensor.getId() + "\",";
 			}
 		}
 		if (body.length() > 1) {
 			body = body.substring(0, body.length() - 1);
 		}
 		body += "],";
 		body += "\"update\":\"" + (overwrite ? 1 : 0) + "\"";
 		body += "}";
 
 		// prepare request callback
 		RequestCallback reqCallback = new RequestCallback() {
 
 			@Override
 			public void onError(Request request, Throwable exception) {
 				LOG.warning("POST default services onError callback: " + exception.getMessage());
 				onCheckDefaultsFailure(source);
 			}
 
 			@Override
 			public void onResponseReceived(Request request, Response response) {
 				LOG.finest("POST default services response received: " + response.getStatusText());
 				int statusCode = response.getStatusCode();
 				if (Response.SC_OK == statusCode) {
 					onCheckDefaultsSuccess(response.getText(), source);
 				} else {
 					LOG.warning("POST default services returned incorrect status: " + statusCode);
 					onCheckDefaultsFailure(source);
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
 			LOG.warning("POST default services request threw exception: " + e.getMessage());
 			reqCallback.onError(null, e);
 		}
 	}
 
 	@Override
 	public void handleEvent(AppEvent event) {
 		final EventType type = event.getType();
 
 		if (type.equals(StateDefaultsEvents.CheckDefaultsRequest)) {
 			LOG.fine("CheckDefaultsRequest");
 			List<ExtDevice> devices = event.getData("devices");
 			boolean overwrite = event.getData("overwrite");
 			View source = (View) event.getSource();
 			checkDefaults(devices, overwrite, source);
 
 		} else if (type.equals(StateDefaultsEvents.CheckDefaults)) {
 			StateDefaultsView view = new StateDefaultsView(this);
 			forwardToView(view, event);
 		}
 	}
 
 	private void onCheckDefaultsFailure(View source) {
 		forwardToView(source, new AppEvent(StateDefaultsEvents.CheckDefaultsFailure));
 	}
 
 	private void onCheckDefaultsSuccess(String response, View source) {
 		AppEvent event = new AppEvent(StateDefaultsEvents.CheckDefaultsSuccess);
 		Dispatcher.forwardEvent(event);
 		forwardToView(source, event);
 	}
 }
