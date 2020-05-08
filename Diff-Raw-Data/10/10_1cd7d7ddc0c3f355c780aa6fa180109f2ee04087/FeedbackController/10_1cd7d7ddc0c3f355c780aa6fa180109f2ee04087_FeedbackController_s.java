 package nl.sense_os.commonsense.main.client.states.feedback;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import nl.sense_os.commonsense.common.client.communication.SessionManager;
 import nl.sense_os.commonsense.common.client.communication.httpresponse.ServiceMethodResponse;
 import nl.sense_os.commonsense.common.client.constant.Urls;
 import nl.sense_os.commonsense.main.client.ext.model.ExtSensor;
 
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.event.EventType;
 import com.extjs.gxt.ui.client.mvc.AppEvent;
 import com.extjs.gxt.ui.client.mvc.Controller;
 import com.extjs.gxt.ui.client.mvc.View;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestBuilder.Method;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.http.client.UrlBuilder;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.Timer;
 
 public class FeedbackController extends Controller {
 
 	private static final Logger LOG = Logger.getLogger(FeedbackController.class.getName());
 	private View feedback;
 	private View chooser;
 
 	public FeedbackController() {
 
 		// LOG.setLevel(Level.ALL);
 
 		registerEventTypes(FeedbackEvents.FeedbackInit);
 		registerEventTypes(FeedbackEvents.ShowChooser, FeedbackEvents.FeedbackChosen);
 
 		registerEventTypes(FeedbackEvents.FeedbackSubmit);
 
 		registerEventTypes(FeedbackEvents.LabelsRequest);
 	}
 
 	private void checkFeedbackProcessed(final int checkCount, final ExtSensor state,
 			final List<FeedbackData> changes, final int index, final FeedbackPanel panel) {
 		ExtSensor sensor = (ExtSensor) state.getChild(0);
 
 		if (index < changes.size()) {
 
 			// prepare request properties
 			final Method method = RequestBuilder.GET;
 			final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
 			urlBuilder.setPath(Urls.PATH_SENSORS + "/" + sensor.getId() + "/services/"
 					+ state.getId() + "/GetManualInputMode.json");
 			final String url = urlBuilder.buildString();
 			final String sessionId = SessionManager.getSessionId();
 
 			// prepare request callback
 			RequestCallback reqCallback = new RequestCallback() {
 
 				@Override
 				public void onError(Request request, Throwable exception) {
 					LOG.warning("GET manual input mode onError callback: " + exception.getMessage());
 					onCheckProcessedFailed(panel);
 				}
 
 				@Override
 				public void onResponseReceived(Request request, Response response) {
 					LOG.finest("GET manual input mode response received: "
 							+ response.getStatusText());
 					int statusCode = response.getStatusCode();
 					if (Response.SC_OK == statusCode) {
 						onCheckProcessedSuccess(response.getText(), checkCount, state, changes,
 								index, panel);
 					} else {
 						LOG.warning("GET manual input mode returned incorrect status: "
 								+ statusCode);
 						onCheckProcessedFailed(panel);
 					}
 				}
 			};
 
 			// send request
 			try {
 				RequestBuilder builder = new RequestBuilder(method, url);
 				builder.setHeader("X-SESSION_ID", sessionId);
 				builder.sendRequest(null, reqCallback);
 			} catch (Exception e) {
 				LOG.warning("GET manual input mode request threw exception: " + e.getMessage());
 				reqCallback.onError(null, e);
 			}
 
 		} else {
 			LOG.severe("Cannot find sensor while checking if feedback is processed");
 		}
 	}
 
 	private void getLabels(final ExtSensor state, final List<ExtSensor> sensors) {
 
 		List<ModelData> methods = state.<List<ModelData>> get("methods");
 		boolean canHazClassLabels = false;
 		for (ModelData method : methods) {
 			if (method.get("name").equals("GetClassLabels")) {
 				canHazClassLabels = true;
 				break;
 			}
 		}
 		if (false == canHazClassLabels) {
 			onLabelsComplete(state, sensors, new ArrayList<String>());
 		}
 
 		if (sensors.size() > 0) {
 			ExtSensor sensor = sensors.get(0);
 
 			// prepare request properties
 			final Method method = RequestBuilder.GET;
 			final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
 			urlBuilder.setPath(Urls.PATH_SENSORS + "/" + sensor.getId() + "/services/"
 					+ state.getId() + "/GetClassLabels.json");
 			final String url = urlBuilder.buildString();
 			final String sessionId = SessionManager.getSessionId();
 
 			// prepare request callback
 			RequestCallback reqCallback = new RequestCallback() {
 
 				@Override
 				public void onError(Request request, Throwable exception) {
 					LOG.warning("GET class labels onError callback: " + exception.getMessage());
 					onLabelsFailure(0);
 				}
 
 				@Override
 				public void onResponseReceived(Request request, Response response) {
 					LOG.finest("GET class labels response received: " + response.getStatusText());
 					int statusCode = response.getStatusCode();
 					if (Response.SC_OK == statusCode) {
 						onLabelsSuccess(response.getText(), state, sensors);
 					} else {
 						LOG.warning("GET class labels returned incorrect status: " + statusCode);
 						onLabelsFailure(statusCode);
 					}
 				}
 			};
 
 			// send request
 			try {
 				RequestBuilder builder = new RequestBuilder(method, url);
 				builder.setHeader("X-SESSION_ID", sessionId);
 				builder.sendRequest(null, reqCallback);
 			} catch (Exception e) {
 				LOG.warning("GET class labels request threw exception: " + e.getMessage());
 				reqCallback.onError(null, e);
 			}
 
 		} else {
 			LOG.warning("No sensors!");
 			onLabelsFailure(0);
 		}
 	}
 
 	@Override
 	public void handleEvent(AppEvent event) {
 		EventType type = event.getType();
 
 		/*
 		 * SubmitRequest feedback data.
 		 */
 		if (type.equals(FeedbackEvents.FeedbackSubmit)) {
 			// LOG.fine( "FeedbackSubmit");
 			final ExtSensor state = event.<ExtSensor> getData("state");
 			final List<FeedbackData> changes = event.<List<FeedbackData>> getData("changes");
 			final FeedbackPanel panel = event.<FeedbackPanel> getData("panel");
 			markFeedback(state, changes, 0, panel);
 
 		} else
 
 		/*
 		 * Get state labels.
 		 */
 		if (type.equals(FeedbackEvents.LabelsRequest)) {
 			// LOG.fine( "LabelsRequest");
 			final ExtSensor state = event.getData("state");
 			final List<ExtSensor> sensors = event.getData("sensors");
 			getLabels(state, sensors);
 
 		} else
 
 		/*
 		 * Feedback settings chooser
 		 */
 		if (type.equals(FeedbackEvents.ShowChooser)) {
 			forwardToView(chooser, event);
 
 		} else
 
 		/*
 		 * Pass through to view.
 		 */
 		{
 			forwardToView(feedback, event);
 		}
 	}
 
 	@Override
 	protected void initialize() {
 		super.initialize();
 		feedback = new FeedbackView(this);
 		chooser = new FeedbackChooser(this);
 	}
 
 	/**
 	 * Submits feedback to CommonSense, one change at a time.
 	 * 
 	 * @param state
 	 *            State sensor to submit feedback for.
 	 * @param changes
 	 *            List of feedback changes.
 	 * @param index
 	 *            Index of the change that has to be processed.
 	 * @param panel
 	 *            Feedback panel that should receive callbacks about the feedback processing.
 	 */
 	private void markFeedback(final ExtSensor state, final List<FeedbackData> changes,
 			final int index, final FeedbackPanel panel) {
 
 		ExtSensor sensor = (ExtSensor) state.getChild(0);
 
 		if (index < changes.size()) {
 			FeedbackData change = changes.get(index);
 
 			// TODO also process delete changes
 			if (change.getType() == FeedbackData.TYPE_REMOVE) {
 				LOG.warning("Skipping feedback deletion!");
 				markFeedback(state, changes, index + 1, panel);
 				return;
 			}
 
 			// prepare request properties
 			final Method method = RequestBuilder.POST;
 			final UrlBuilder urlBuilder = new UrlBuilder().setHost(Urls.HOST);
 			urlBuilder.setPath(Urls.PATH_SENSORS + "/" + sensor.getId() + "/services/"
 					+ state.getId() + "/manualLearn.json");
 			final String url = urlBuilder.buildString();
 			final String sessionId = SessionManager.getSessionId();
 
 			// prepare request body
 			String body = "{\"start_date\":\""
 					+ NumberFormat.getFormat("#.000").format(change.getStart() / 1000d) + "\"";
 			body += ",\"end_date\":\""
 					+ NumberFormat.getFormat("#.000").format(change.getEnd() / 1000d) + "\"";
 			body += ",\"class_label\":\"" + change.getLabel() + "\"}";
 
 			// prepare request callback
 			RequestCallback reqCallback = new RequestCallback() {
 
 				@Override
 				public void onError(Request request, Throwable exception) {
 					LOG.warning("POST feedback onError callback: " + exception.getMessage());
 					onFeedbackFailed(0, panel);
 				}
 
 				@Override
 				public void onResponseReceived(Request request, Response response) {
 					LOG.finest("POST feedback response received: " + response.getStatusText());
 					int statusCode = response.getStatusCode();
 					if (Response.SC_OK == statusCode) {
 						onFeedbackMarked(response.getText(), state, changes, index, panel);
 					} else if (Response.SC_NO_CONTENT == statusCode) {
 						onNoContent(state, changes, index, panel);
 					} else {
 						LOG.warning("POST feedback returned incorrect status: " + statusCode);
 						onFeedbackFailed(statusCode, panel);
 					}
 				}
 			};
 
 			// send request
 			try {
 				RequestBuilder builder = new RequestBuilder(method, url);
 				builder.setHeader("X-SESSION_ID", sessionId);
 				builder.sendRequest(body, reqCallback);
 			} catch (Exception e) {
 				LOG.warning("POST feedback request threw exception: " + e.getMessage());
 				reqCallback.onError(null, e);
 			}
 
 		} else {
 			onFeedbackComplete(panel);
 		}
 	}
 
 	/**
 	 * Handles callback when feedback mark request returns 204 (No Content). This means that the
 	 * source sensors have no data in the selected time interval. Not a big problem, we just
 	 * continue with the next change.
 	 * 
 	 * @param state
 	 *            State sensor the feedback was given for.
 	 * @param changes
 	 *            List of feedback changes that have to be sent to CommonSense, one by one.
 	 * @param index
 	 *            Index of the change that returned 204.
 	 * @param panel
 	 *            The feedback panel that originated the changes.
 	 */
 	private void onNoContent(ExtSensor state, List<FeedbackData> changes, int index,
 			FeedbackPanel panel) {
 		markFeedback(state, changes, index + 1, panel);
 	}
 
 	/**
 	 * Notifies the feedback panel that processing is taking longer than usual.
 	 * 
 	 * @param panel
 	 *            The feedback panel that originated the changes, and should notify the user.
 	 */
 	private void notifySlowProcessing(FeedbackPanel panel) {
 		panel.onFeedbackSlow();
 	}
 
 	private void onCheckProcessedFailed(FeedbackPanel panel) {
 		onFeedbackFailed(0, panel);
 	}
 
 	private void onCheckProcessedSuccess(String response, final int checkCount,
 			final ExtSensor state, final List<FeedbackData> changes, final int index,
 			final FeedbackPanel panel) {
 
 		String result = null;
 		ServiceMethodResponse jso = ServiceMethodResponse.create(response).cast();
 		if (null != jso) {
 			result = jso.getResult();
 		}
 
 		if (result.equals("0")) {
 			// processing complete! mark the next feedback change
 			markFeedback(state, changes, index + 1, panel);
 		} else {
 			LOG.warning("Feedback still being processed... ManualInputMode=" + result);
 
 			if (checkCount == 10) {
 				// notify the user that processing is taking longer than usual
 				notifySlowProcessing(panel);
 			}
 
 			// check again in 500ms
 			new Timer() {
 
 				@Override
 				public void run() {
 					checkFeedbackProcessed(checkCount + 1, state, changes, index, panel);
 				}
 			}.schedule(500);
 		}
 	}
 
 	private void onFeedbackComplete(FeedbackPanel panel) {
 		AppEvent complete = new AppEvent(FeedbackEvents.FeedbackComplete);
 		complete.setData("panel", panel);
 		forwardToView(feedback, complete);
 	}
 
 	private void onFeedbackFailed(int code, FeedbackPanel panel) {
 		LOG.fine("Feedback failure");
 		AppEvent failure = new AppEvent(FeedbackEvents.FeedbackFailed);
 		failure.setData("panel", panel);
 		forwardToView(feedback, failure);
 	}
 
 	private void onFeedbackMarked(String response, ExtSensor state, List<FeedbackData> changes,
 			int index, FeedbackPanel panel) {
 		checkFeedbackProcessed(0, state, changes, index, panel);
 	}
 
 	private void onLabelsComplete(ExtSensor state, List<ExtSensor> sensors, List<String> labels) {
 		AppEvent event = new AppEvent(FeedbackEvents.LabelsSuccess);
 		event.setData("state", state);
 		event.setData("sensors", sensors);
 		event.setData("labels", labels);
 		forwardToView(feedback, event);
 	}
 
 	private void onLabelsFailure(int code) {
 		forwardToView(feedback, new AppEvent(FeedbackEvents.LabelsFailure));
 	}
 
 	private void onLabelsSuccess(String response, ExtSensor state, List<ExtSensor> sensors) {
 
 		// parse result from the GetClassLabels response
 		String resultString = null;
		ServiceMethodResponse jso = ServiceMethodResponse.create(response).cast();
		if (null != jso) {
			resultString = jso.getResult();
 		}
 
 		// parse labels from raw result String
 		if (resultString != null) {
 			resultString = resultString.replaceAll("&quot;", "\"");
 			JSONValue rawResult = JSONParser.parseStrict(resultString);
 			if (null != rawResult) {
 				JSONObject result = rawResult.isObject();
 				JSONValue rawLabels = result.get("classLabels");
 				if (null != rawLabels) {
 					JSONArray labels = rawLabels.isArray();
 					if (null != labels) {
 						List<String> list = new ArrayList<String>();
 						JSONString rawString;
 						for (int i = 0; i < labels.size(); i++) {
 							rawString = labels.get(i).isString();
 							if (null != rawString) {
 								list.add(rawString.stringValue());
 							} else {
 								LOG.warning("label is not a JSON string");
 								onLabelsFailure(0);
 							}
 						}
 
 						onLabelsComplete(state, sensors, list);
 
 					} else {
 						LOG.warning("\"classLabels\" is not a JSON array");
 						onLabelsFailure(0);
 					}
 				} else {
 					LOG.warning("\"classLabels\" is not valid JSON");
 					onLabelsFailure(0);
 				}
 			} else {
 				LOG.warning("result is not valid JSON");
 				onLabelsFailure(0);
 			}
 		} else {
 			LOG.warning("\"result\" is not a JSON string");
 			onLabelsFailure(0);
 		}
 	}
 }
