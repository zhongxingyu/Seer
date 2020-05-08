 package nl.sense_os.commonsense.client.env.create;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import nl.sense_os.commonsense.client.common.ajax.AjaxEvents;
 import nl.sense_os.commonsense.client.common.json.parsers.EnvironmentParser;
 import nl.sense_os.commonsense.client.common.json.parsers.SensorParser;
 import nl.sense_os.commonsense.client.env.list.EnvEvents;
 import nl.sense_os.commonsense.client.utility.Log;
 import nl.sense_os.commonsense.shared.Constants;
 import nl.sense_os.commonsense.shared.DeviceModel;
 import nl.sense_os.commonsense.shared.EnvironmentModel;
 import nl.sense_os.commonsense.shared.SensorModel;
 
 import com.extjs.gxt.ui.client.Registry;
 import com.extjs.gxt.ui.client.event.EventType;
 import com.extjs.gxt.ui.client.mvc.AppEvent;
 import com.extjs.gxt.ui.client.mvc.Controller;
 import com.extjs.gxt.ui.client.mvc.Dispatcher;
 import com.extjs.gxt.ui.client.mvc.View;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.maps.client.geom.LatLng;
 import com.google.gwt.maps.client.overlay.Marker;
 import com.google.gwt.maps.client.overlay.Polygon;
 
 public class EnvCreateController extends Controller {
 
     private static final String TAG = "EnvCreateController";
     private View creator;
 
     public EnvCreateController() {
         registerEventTypes(EnvCreateEvents.ShowCreator);
         registerEventTypes(EnvCreateEvents.Forward, EnvCreateEvents.Back, EnvCreateEvents.Cancel);
         registerEventTypes(EnvCreateEvents.OutlineComplete);
         registerEventTypes(EnvCreateEvents.CreateRequest, EnvCreateEvents.CreateSuccess,
                 EnvCreateEvents.CreateAjaxSuccess, EnvCreateEvents.CreateAjaxFailure,
                 EnvCreateEvents.AddSensorsAjaxSuccess, EnvCreateEvents.AddSensorsAjaxFailure,
                 EnvCreateEvents.PositionSensorAjaxSuccess,
                 EnvCreateEvents.PositionSensorAjaxFailure, EnvCreateEvents.SetPositionAjaxSuccess,
                 EnvCreateEvents.SetPositionAjaxFailure, EnvCreateEvents.CreateSensorAjaxSuccess,
                 EnvCreateEvents.CreateSensorAjaxFailure);
     }
 
     private void addSensors(EnvironmentModel environment, Map<Marker, List<SensorModel>> sensors) {
 
         if (false == sensors.isEmpty()) {
 
             String sensorsArray = "[";
             for (Entry<Marker, List<SensorModel>> entry : sensors.entrySet()) {
                 List<SensorModel> sensorList = entry.getValue();
                 for (SensorModel sensor : sensorList) {
                     sensorsArray += "{\"id\":" + sensor.getId() + "},";
                 }
             }
             sensorsArray = sensorsArray.substring(0, sensorsArray.length() - 1) + "]";
 
             // prepare request properties
             final String method = "POST";
             final String url = Constants.URL_ENVIRONMENTS + "/" + environment.getId()
                     + "/sensors.json";
             final String sessionId = Registry.get(Constants.REG_SESSION_ID);
             final AppEvent onSuccess = new AppEvent(EnvCreateEvents.AddSensorsAjaxSuccess);
             onSuccess.setData("sensors", sensors);
             final AppEvent onFailure = new AppEvent(EnvCreateEvents.AddSensorsAjaxFailure);
             onFailure.setData("environment", environment);
 
             String body = "{\"sensors\":" + sensorsArray + "}";
 
             // send request to AjaxController
             final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
             ajaxRequest.setData("method", method);
             ajaxRequest.setData("url", url);
             ajaxRequest.setData("body", body);
             ajaxRequest.setData("session_id", sessionId);
             ajaxRequest.setData("onSuccess", onSuccess);
             ajaxRequest.setData("onFailure", onFailure);
 
             Dispatcher.forwardEvent(ajaxRequest);
 
         } else {
             onCreateComplete();
         }
     }
 
     private void create(String name, int floors, Polygon outline,
             Map<Marker, List<SensorModel>> sensors) {
 
         // create GPS outline String
         String gpsOutline = "";
         for (int i = 0; i < outline.getVertexCount(); i++) {
             LatLng vertex = outline.getVertex(i);
             gpsOutline += vertex.toUrlValue() + ";";
         }
         gpsOutline = gpsOutline.substring(0, gpsOutline.length() - 1);
 
         // create GPS position String
         String position = outline.getBounds().getCenter().toUrlValue();
 
         // prepare request properties
         final String method = "POST";
         final String url = Constants.URL_ENVIRONMENTS + ".json";
         final String sessionId = Registry.get(Constants.REG_SESSION_ID);
         final AppEvent onSuccess = new AppEvent(EnvCreateEvents.CreateAjaxSuccess);
         onSuccess.setData("sensors", sensors);
         final AppEvent onFailure = new AppEvent(EnvCreateEvents.CreateAjaxFailure);
 
         String body = "{\"environment\":{";
         body += "\"" + EnvironmentModel.NAME + "\":\"" + name + "\",";
         body += "\"" + EnvironmentModel.FLOORS + "\":" + floors + ",";
         body += "\"" + EnvironmentModel.OUTLINE + "\":\"" + gpsOutline + "\",";
         body += "\"" + EnvironmentModel.POSITION + "\":\"" + position + "\"}";
         body += "}";
 
         // send request to AjaxController
         final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
         ajaxRequest.setData("method", method);
         ajaxRequest.setData("url", url);
         ajaxRequest.setData("body", body);
         ajaxRequest.setData("session_id", sessionId);
         ajaxRequest.setData("onSuccess", onSuccess);
         ajaxRequest.setData("onFailure", onFailure);
 
         Dispatcher.forwardEvent(ajaxRequest);
     }
 
     private void createPositionSensor(List<DeviceModel> devices, int index) {
 
         // prepare body
         String dataStructure = "{\\\"latitude\\\":\\\"string\\\",\\\"longitude\\\":\\\"string\\\",\\\"altitude\\\":\\\"string\\\"}";
         String sensor = "{";
         sensor += "\"" + SensorModel.NAME + "\":\"position\",";
         sensor += "\"" + SensorModel.DISPLAY_NAME + "\":\"position\",";
         sensor += "\"" + SensorModel.PHYSICAL_SENSOR + "\":\"position\",";
         sensor += "\"" + SensorModel.DATA_TYPE + "\":\"json\",";
         sensor += "\"" + SensorModel.DATA_STRUCTURE + "\":\"" + dataStructure + "\"";
        sensor += "}";
         String body = "{\"sensor\":" + sensor + "}";
 
         // prepare request properties
         final String method = "POST";
         final String url = Constants.URL_SENSORS + ".json";
         final String sessionId = Registry.get(Constants.REG_SESSION_ID);
         final AppEvent onSuccess = new AppEvent(EnvCreateEvents.CreateSensorAjaxSuccess);
         onSuccess.setData("devices", devices);
         onSuccess.setData("index", index);
         final AppEvent onFailure = new AppEvent(EnvCreateEvents.CreateSensorAjaxFailure);
 
         // send request to AjaxController
         final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
         ajaxRequest.setData("method", method);
         ajaxRequest.setData("url", url);
         ajaxRequest.setData("body", body);
         ajaxRequest.setData("session_id", sessionId);
         ajaxRequest.setData("onSuccess", onSuccess);
         ajaxRequest.setData("onFailure", onFailure);
 
         Dispatcher.forwardEvent(ajaxRequest);
     }
 
     private void getPositionSensor(List<DeviceModel> devices, int index) {
 
         DeviceModel device = devices.get(index);
 
         // prepare request properties
         final String method = "GET";
         final String url = Constants.URL_DEVICES + "/" + device.getId() + "/sensors.json";
         final String sessionId = Registry.get(Constants.REG_SESSION_ID);
         final AppEvent onSuccess = new AppEvent(EnvCreateEvents.PositionSensorAjaxSuccess);
         onSuccess.setData("devices", devices);
         onSuccess.setData("index", index);
         final AppEvent onFailure = new AppEvent(EnvCreateEvents.PositionSensorAjaxFailure);
 
         // send request to AjaxController
         final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
         ajaxRequest.setData("method", method);
         ajaxRequest.setData("url", url);
         ajaxRequest.setData("session_id", sessionId);
         ajaxRequest.setData("onSuccess", onSuccess);
         ajaxRequest.setData("onFailure", onFailure);
 
         Dispatcher.forwardEvent(ajaxRequest);
     }
 
     @Override
     public void handleEvent(AppEvent event) {
         final EventType type = event.getType();
 
         if (type.equals(EnvCreateEvents.CreateRequest)) {
             Log.d(TAG, "CreateRequest");
             final String name = event.<String> getData("name");
             final int floors = event.getData("floors");
             final Polygon outline = event.<Polygon> getData("outline");
             final Map<Marker, List<SensorModel>> sensors = event
                     .<Map<Marker, List<SensorModel>>> getData("sensors");
             create(name, floors, outline, sensors);
 
         } else
 
         if (type.equals(EnvCreateEvents.CreateAjaxSuccess)) {
             Log.d(TAG, "CreateAjaxSuccess");
             final String response = event.<String> getData("response");
             final Map<Marker, List<SensorModel>> sensors = event.getData("sensors");
             onCreateSuccess(response, sensors);
 
         } else if (type.equals(EnvCreateEvents.CreateAjaxFailure)) {
             Log.w(TAG, "CreateAjaxFailure");
             // final int code = event.getData("code");
             onCreateFailure();
 
         } else
 
         if (type.equals(EnvCreateEvents.AddSensorsAjaxSuccess)) {
             Log.d(TAG, "AddSensorsAjaxSuccess");
             // final String response = event.<String> getData("response");
             final Map<Marker, List<SensorModel>> sensors = event.getData("sensors");
             onAddSensorSuccess(sensors);
 
         } else if (type.equals(EnvCreateEvents.AddSensorsAjaxFailure)) {
             Log.w(TAG, "AddSensorsAjaxFailure");
             // final int code = event.getData("code");
             final EnvironmentModel environment = event.getData("environment");
             onAddSensorsFailure(environment);
 
         } else
 
         if (type.equals(EnvCreateEvents.PositionSensorAjaxSuccess)) {
             Log.d(TAG, "PositionSensorAjaxSuccess");
             final String response = event.<String> getData("response");
             final List<DeviceModel> devices = event.getData("devices");
             final int index = event.getData("index");
             onPositionSensorSuccess(response, devices, index);
 
         } else if (type.equals(EnvCreateEvents.PositionSensorAjaxFailure)) {
             Log.w(TAG, "PositionSensorAjaxFailure");
             // final int code = event.getData("code");
             onPositionSensorFailure();
 
         } else
 
         if (type.equals(EnvCreateEvents.CreateSensorAjaxSuccess)) {
             Log.d(TAG, "CreateSensorAjaxSuccess");
             final String response = event.<String> getData("response");
             final List<DeviceModel> devices = event.getData("devices");
             final int index = event.getData("index");
             onCreateSensorSuccess(response, devices, index);
 
         } else if (type.equals(EnvCreateEvents.CreateSensorAjaxFailure)) {
             Log.w(TAG, "CreateSensorAjaxFailure");
             // final int code = event.getData("code");
             onCreateSensorFailure();
 
         } else
 
         if (type.equals(EnvCreateEvents.SetPositionAjaxSuccess)) {
             Log.d(TAG, "SetPositionAjaxSuccess");
             final String response = event.<String> getData("response");
             final List<DeviceModel> devices = event.getData("devices");
             final int index = event.getData("index");
             onSetPositionSuccess(response, devices, index);
 
         } else if (type.equals(EnvCreateEvents.SetPositionAjaxFailure)) {
             Log.w(TAG, "SetPositionAjaxFailure");
             // final int code = event.getData("code");
             onSetPositionFailure();
 
         } else
 
         {
             forwardToView(this.creator, event);
         }
     }
 
     private void onCreateSensorFailure() {
         onCreateFailure();
     }
 
     private void onCreateSensorSuccess(String response, List<DeviceModel> devices, int index) {
         JSONValue rawJson = JSONParser.parseLenient(response);
         if (null != rawJson && null != rawJson.isObject()) {
             JSONObject sensorJson = rawJson.isObject().get("sensor").isObject();
             SensorModel positionSensor = SensorParser.parseSensor(sensorJson);
             setPosition(positionSensor, devices, index);
         } else {
             onCreateFailure();
         }
     }
 
     @Override
     protected void initialize() {
         super.initialize();
         this.creator = new EnvCreator(this);
     }
 
     private void onAddSensorsFailure(EnvironmentModel environment) {
         // delete the environment that was created
         AppEvent delete = new AppEvent(EnvEvents.DeleteRequest);
         delete.setData("environment", environment);
         Dispatcher.forwardEvent(delete);
 
         onCreateFailure();
     }
 
     private void onAddSensorSuccess(Map<Marker, List<SensorModel>> sensors) {
 
         // create list of devices with their desired positions
         List<DeviceModel> toPosition = new ArrayList<DeviceModel>();
         for (Entry<Marker, List<SensorModel>> entry : sensors.entrySet()) {
             LatLng latlng = entry.getKey().getLatLng();
             for (SensorModel sensor : entry.getValue()) {
                 DeviceModel device = sensor.getDevice();
                 if (device != null && !toPosition.contains(device)) {
                     device.set("latlng", latlng);
                     toPosition.add(device);
                 }
             }
         }
         updatePosition(toPosition, 0);
     }
 
     private void onCreateComplete() {
         Dispatcher.forwardEvent(EnvCreateEvents.CreateSuccess);
     }
 
     private void onCreateFailure() {
         forwardToView(this.creator, new AppEvent(EnvCreateEvents.CreateFailure));
     }
 
     private void onCreateSuccess(String response, Map<Marker, List<SensorModel>> sensors) {
         JSONObject responseJson = JSONParser.parseLenient(response).isObject();
         if (null != responseJson) {
             JSONObject envJson = responseJson.get("environment").isObject();
             EnvironmentModel environment = EnvironmentParser.parse(envJson);
 
             // continue with adding sensors
             addSensors(environment, sensors);
 
         } else {
             onCreateFailure();
         }
     }
 
     private void onPositionSensorFailure() {
         onCreateFailure();
     }
 
     private void onPositionSensorSuccess(String response, List<DeviceModel> devices, int index) {
         List<SensorModel> sensors = new ArrayList<SensorModel>();
         SensorParser.parseSensors(response, sensors);
 
         SensorModel positionSensor = null;
         for (SensorModel sensor : sensors) {
             if (sensor.getName().equals("position")) {
                 positionSensor = sensor;
                 break;
             }
         }
         if (null != positionSensor) {
             setPosition(positionSensor, devices, index);
         } else {
             createPositionSensor(devices, index);
         }
     }
 
     private void onSetPositionFailure() {
         onCreateFailure();
     }
 
     private void onSetPositionSuccess(String response, List<DeviceModel> devices, int index) {
         index++;
         updatePosition(devices, index);
     }
 
     private void setPosition(SensorModel positionSensor, List<DeviceModel> devices, int index) {
 
         DeviceModel device = devices.get(index);
         LatLng latLng = device.<LatLng> get("latlng");
 
         // prepare request properties
         final String method = "POST";
         final String url = Constants.URL_SENSORS + "/" + positionSensor.getId() + "/data.json";
         final String sessionId = Registry.get(Constants.REG_SESSION_ID);
         final AppEvent onSuccess = new AppEvent(EnvCreateEvents.SetPositionAjaxSuccess);
         onSuccess.setData("devices", devices);
         onSuccess.setData("index", index);
         final AppEvent onFailure = new AppEvent(EnvCreateEvents.SetPositionAjaxFailure);
 
         String value = "{\\\"latitude\\\":" + latLng.getLatitude() + ",\\\"longitude\\\":"
                 + latLng.getLongitude() + ",\\\"provider\\\":\\\"environment\\\"}";
         String body = "{\"data\":[";
         body += "{\"value\":\"" + value + "\",\"date\":"
                 + NumberFormat.getFormat("#.#").format(System.currentTimeMillis() / 1000) + "}";
         body += "]}";
 
         // send request to AjaxController
         final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
         ajaxRequest.setData("method", method);
         ajaxRequest.setData("url", url);
         ajaxRequest.setData("body", body);
         ajaxRequest.setData("session_id", sessionId);
         ajaxRequest.setData("onSuccess", onSuccess);
         ajaxRequest.setData("onFailure", onFailure);
 
         Dispatcher.forwardEvent(ajaxRequest);
     }
 
     private void updatePosition(List<DeviceModel> devices, int index) {
 
         if (index < devices.size()) {
             getPositionSensor(devices, index);
 
         } else {
             // hooray we're done!
             onCreateComplete();
         }
     }
 
 }
