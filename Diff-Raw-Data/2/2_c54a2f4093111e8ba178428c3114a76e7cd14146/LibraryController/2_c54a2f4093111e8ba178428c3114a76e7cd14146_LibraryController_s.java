 package nl.sense_os.commonsense.client.sensors.library;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import nl.sense_os.commonsense.client.CommonSense;
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
 import nl.sense_os.commonsense.client.states.create.AvailServicesResponseJso;
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
 import com.google.gwt.core.client.JsonUtils;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
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
 
         LOG.setLevel(Level.WARNING);
 
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
 
     private void getAvailableServices(final List<SensorModel> library, final int index) {
 
         if (index < library.size()) {
 
             isLoadingServices = true;
             notifyState();
 
             SensorModel sensor = library.get(index);
             String params = "";
             if (sensor.getAlias() != -1) {
                 params = "?alias=" + sensor.getAlias();
             }
 
             // prepare request properties
             final String url = Urls.SENSORS + "/" + sensor.getId() + "/services/available.json"
                     + params;
             final String sessionId = Registry.get(Constants.REG_SESSION_ID);
 
             // prepare request callback
             RequestCallback reqCallback = new RequestCallback() {
 
                 @Override
                 public void onError(Request request, Throwable exception) {
                     LOG.warning("GET available services onError callback: "
                             + exception.getMessage());
                     onAvailServicesFailure(library, index);
                 }
 
                 @Override
                 public void onResponseReceived(Request request, Response response) {
                     LOG.finest("GET available services response received: "
                             + response.getStatusText());
                     int statusCode = response.getStatusCode();
                     if (Response.SC_OK == statusCode) {
                         onAvailServicesSuccess(response.getText(), library, index);
                     } else {
                         LOG.warning("GET available services returned incorrect status: "
                                 + statusCode);
                         onAvailServicesFailure(library, index);
                     }
                 }
             };
 
             // send request
             RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
             builder.setHeader("X-SESSION_ID", sessionId);
             try {
                 builder.sendRequest(null, reqCallback);
             } catch (RequestException e) {
                 LOG.warning("GET  available services request threw exception: " + e.getMessage());
                 onAvailServicesFailure(library, index);
             }
 
         } else {
             // hooray we're done!
             isLoadingServices = false;
             notifyState();
         }
     }
 
     private void getFullDetails(final List<SensorModel> library, final int page,
             final boolean shared, final AsyncCallback<ListLoadResult<SensorModel>> callback) {
 
         // prepare request properties
         final String url = Urls.SENSORS + ".json" + "?per_page=" + PER_PAGE + "&page=" + page
                + "&details=full" + "&shared=" + (shared ? "1" : "0");
         final String sessionId = Registry.get(Constants.REG_SESSION_ID);
 
         // prepare request callback
         RequestCallback reqCallback = new RequestCallback() {
 
             @Override
             public void onError(Request request, Throwable exception) {
                 LOG.warning("GET sensors onError callback: " + exception.getMessage());
                 onFullDetailsFailure(callback);
             }
 
             @Override
             public void onResponseReceived(Request request, Response response) {
                 LOG.finest("GET sensors response received: " + response.getStatusText());
                 int statusCode = response.getStatusCode();
                 if (Response.SC_OK == statusCode) {
                     // different callbacks for shared or unshared requests
                     if (shared) {
                         onSharedSensorsSuccess(response.getText(), library, page, callback);
                     } else {
                         onUnsharedSensorsSuccess(response.getText(), library, page, callback);
                     }
                 } else if (Response.SC_NO_CONTENT == statusCode) {
                     // different callbacks for shared or unshared requests
                     if (shared) {
                         onSharedSensorsSuccess(null, library, page, callback);
                     } else {
                         onUnsharedSensorsSuccess(null, library, page, callback);
                     }
                 } else {
                     LOG.warning("GET sensors returned incorrect status: " + statusCode);
                     onFullDetailsFailure(callback);
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
             onFullDetailsFailure(callback);
         }
     }
 
     private void getGroups(final List<SensorModel> library,
             final AsyncCallback<ListLoadResult<SensorModel>> callback) {
 
         // prepare request properties
         final String url = Urls.GROUPS + ".json" + "?per_page=" + PER_PAGE;
         final String sessionId = Registry.get(Constants.REG_SESSION_ID);
 
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
 
     private void getGroupSensors(final List<GroupModel> groups, final int index,
             final List<SensorModel> library,
             final AsyncCallback<ListLoadResult<SensorModel>> callback) {
 
         if (index < groups.size()) {
 
             int groupId = groups.get(index).getId();
 
             // prepare request properties
             final String url = Urls.SENSORS + ".json" + "?per_page=1000&details=full&alias="
                     + groupId;
             final String sessionId = Registry.get(Constants.REG_SESSION_ID);
 
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
                         onGroupSensorsSuccess(response.getText(), groups, index, library, callback);
                     } else if (Response.SC_NO_CONTENT == statusCode) {
                         // no content
                         onGroupSensorsSuccess(null, groups, index, library, callback);
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
                 onGroupSensorsFailure(callback);
             }
 
         } else {
 
             // continue loading more details in the background
             if (!CommonSense.HACK_SKIP_LIB_DETAILS) {
                 getAvailableServices(library, 0);
             }
 
             // notify the view that the list is complete
             onLoadComplete(library, callback);
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
 
     private void onAvailServicesFailure(List<SensorModel> library, int index) {
         index++;
         getAvailableServices(library, index);
     }
 
     private void onAvailServicesSuccess(String response, List<SensorModel> library, int index) {
 
         // parse list of services from response
         List<ServiceModel> services = new ArrayList<ServiceModel>();
         if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
             AvailServicesResponseJso jso = JsonUtils.unsafeEval(response);
             services = jso.getServices();
         }
 
         SensorModel sensor = library.get(index);
         sensor.set(SensorModel.AVAIL_SERVICES, services);
 
         index++;
         getAvailableServices(library, index);
     }
 
     private void onFullDetailsFailure(AsyncCallback<ListLoadResult<SensorModel>> callback) {
         onLoadFailure(callback);
     }
 
     private void onGroupSensorsFailure(AsyncCallback<ListLoadResult<SensorModel>> callback) {
         onLoadFailure(callback);
     }
 
     private void onGroupSensorsSuccess(String response, List<GroupModel> groups, int index,
             List<SensorModel> library, AsyncCallback<ListLoadResult<SensorModel>> callback) {
         LOG.fine("Received group sensors response...");
 
         // parse group sensors
         List<SensorModel> groupSensors = new ArrayList<SensorModel>();
         if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
             GetSensorsResponseJso responseJso = JsonUtils.unsafeEval(response);
             groupSensors.addAll(responseJso.getSensors());
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
 
         // next group
         index++;
         getGroupSensors(groups, index, library, callback);
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
 
         getGroupSensors(groups, 0, library, callback);
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
 
             getFullDetails(library, 0, false, callback);
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
             getFullDetails(library, page, true, callback);
 
         } else {
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
             getFullDetails(library, page, false, callback);
 
         } else {
             // continue by getting the shared sensors
             getFullDetails(library, page, true, callback);
         }
     }
 
     // private void xhrGetAvailableServices(List<SensorModel> library, int index) {
     //
     // if (index < library.size()) {
     //
     // isLoadingServices = true;
     // notifyState();
     //
     // SensorModel sensor = library.get(index);
     // String params = "";
     // if (sensor.getAlias() != -1) {
     // params = "?alias=" + sensor.getAlias();
     // }
     //
     // // prepare request properties
     // final String method = "GET";
     // final String url = Urls.SENSORS + "/" + sensor.getId() + "/services/available"
     // + ".json" + params;
     // final String sessionId = Registry.get(Constants.REG_SESSION_ID);
     // final AppEvent onSuccess = new AppEvent(LibraryEvents.AvailServicesAjaxSuccess);
     // onSuccess.setData("index", index);
     // onSuccess.setData("library", library);
     // final AppEvent onFailure = new AppEvent(LibraryEvents.AvailServicesAjaxFailure);
     // onFailure.setData("index", index);
     // onFailure.setData("library", library);
     //
     // // send request to AjaxController
     // final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
     // ajaxRequest.setData("method", method);
     // ajaxRequest.setData("url", url);
     // ajaxRequest.setData("session_id", sessionId);
     // ajaxRequest.setData("onSuccess", onSuccess);
     // ajaxRequest.setData("onFailure", onFailure);
     //
     // Dispatcher.forwardEvent(ajaxRequest);
     //
     // } else {
     // // hooray we're done!
     // isLoadingServices = false;
     // notifyState();
     // }
     // }
 
     // private void xhrGetFullDetails(List<SensorModel> library, int page, boolean shared,
     // AsyncCallback<ListLoadResult<SensorModel>> callback) {
     //
     // // prepare request properties
     // final String method = "GET";
     // final String url = Urls.SENSORS + ".json" + "?per_page=" + PER_PAGE + "&page=" + page
     // + "&details=full" + "&shared=" + (shared ? "1" : "0");
     // final String sessionId = Registry.get(Constants.REG_SESSION_ID);
     // final AppEvent onSuccess = new AppEvent(LibraryEvents.FullDetailsAjaxSuccess);
     // onSuccess.setData("library", library);
     // onSuccess.setData("page", page);
     // onSuccess.setData("callback", callback);
     // final AppEvent onFailure = new AppEvent(LibraryEvents.FullDetailsAjaxFailure);
     // onFailure.setData("callback", callback);
     //
     // // send request to AjaxController
     // final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
     // ajaxRequest.setData("method", method);
     // ajaxRequest.setData("url", url);
     // ajaxRequest.setData("session_id", sessionId);
     // ajaxRequest.setData("onSuccess", onSuccess);
     // ajaxRequest.setData("onFailure", onFailure);
     //
     // Dispatcher.forwardEvent(ajaxRequest);
     // }
 
     // private void xhrGetGroups(List<SensorModel> library,
     // AsyncCallback<ListLoadResult<SensorModel>> callback) {
     //
     // // prepare request properties
     // final String method = "GET";
     // final String url = Urls.GROUPS + ".json" + "?per_page=" + PER_PAGE;
     // final String sessionId = Registry.get(Constants.REG_SESSION_ID);
     // final AppEvent onSuccess = new AppEvent(LibraryEvents.GroupsAjaxSuccess);
     // onSuccess.setData("library", library);
     // onSuccess.setData("callback", callback);
     // final AppEvent onFailure = new AppEvent(LibraryEvents.GroupsAjaxFailure);
     // onFailure.setData("callback", callback);
     //
     // // send request to AjaxController
     // final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
     // ajaxRequest.setData("method", method);
     // ajaxRequest.setData("url", url);
     // ajaxRequest.setData("session_id", sessionId);
     // ajaxRequest.setData("onSuccess", onSuccess);
     // ajaxRequest.setData("onFailure", onFailure);
     //
     // Dispatcher.forwardEvent(ajaxRequest);
     // }
 
     // private void xhrGetGroupSensors(List<GroupModel> groups, int index, List<SensorModel>
     // library,
     // AsyncCallback<ListLoadResult<SensorModel>> callback) {
     //
     // if (index < groups.size()) {
     //
     // int groupId = groups.get(index).getId();
     //
     // // prepare request properties
     // final String method = "GET";
     // final String url = Urls.SENSORS + ".json" + "?per_page=1000&details=full&alias="
     // + groupId;
     // final String sessionId = Registry.get(Constants.REG_SESSION_ID);
     // final AppEvent onSuccess = new AppEvent(LibraryEvents.GroupSensorsAjaxSuccess);
     // onSuccess.setData("groups", groups);
     // onSuccess.setData("index", index);
     // onSuccess.setData("library", library);
     // onSuccess.setData("callback", callback);
     // final AppEvent onFailure = new AppEvent(LibraryEvents.GroupSensorsAjaxFailure);
     // onFailure.setData("callback", callback);
     //
     // // send request to AjaxController
     // final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
     // ajaxRequest.setData("method", method);
     // ajaxRequest.setData("url", url);
     // ajaxRequest.setData("session_id", sessionId);
     // ajaxRequest.setData("onSuccess", onSuccess);
     // ajaxRequest.setData("onFailure", onFailure);
     //
     // Dispatcher.forwardEvent(ajaxRequest);
     //
     // } else {
     //
     // // continue loading more details in the background
     // if (!CommonSense.HACK_SKIP_LIB_DETAILS) {
     // // getUsers(library, 0);
     // getAvailableServices(library, 0);
     // }
     //
     // // notify the view that the list is complete
     // onLoadComplete(library, callback);
     // }
     // }
 }
