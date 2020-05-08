 package nl.sense_os.commonsense.lib.client.communication;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import nl.sense_os.commonsense.lib.client.model.apiclass.Group;
 import nl.sense_os.commonsense.lib.client.util.Md5Hasher;
 
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestBuilder.Method;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.http.client.UrlBuilder;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONBoolean;
 import com.google.gwt.json.client.JSONNumber;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.user.client.Window.Location;
 
 public class CommonSenseClient {
 
     public static class Urls {
         private static final String PATH_PREFIX = Constants.STABLE_MODE || Constants.RC_MODE
                 || Constants.DEV_MODE ? "api/" : "";
         public static final String HOST = Constants.STABLE_MODE ? "common.sense-os.nl"
                 : Constants.RC_MODE ? "rc.sense-os.nl"
                         : Constants.DEV_MODE ? "common.dev.sense-os.nl" : "api.sense-os.nl";
         public static final String PROTOCOL = "http";
 
         // main paths
         public static final String PATH_DOMAINS = PATH_PREFIX + "domains";
         public static final String PATH_SENSORS = PATH_PREFIX + "sensors";
         public static final String PATH_GROUPS = PATH_PREFIX + "groups";
         public static final String PATH_USERS = PATH_PREFIX + "users";
         public static final String PATH_ENVIRONMENTS = PATH_PREFIX + "environments";
         public static final String PATH_LOGIN = PATH_PREFIX + "login";
         public static final String PATH_LOGIN_GOOGLE = "login/openID/google"; // no prefix!
         public static final String PATH_LOGOUT = PATH_PREFIX + "logout";
         public static final String PATH_PW_RESET = PATH_PREFIX + "resetPassword";
         public static final String PATH_FORGOT_PASSWORD = PATH_PREFIX + "requestPasswordReset";
 
         // domains paths
         public static final String PATH_DOMAIN_USERS = PATH_DOMAINS + "/%1/users";
 
         // sensors paths
         public static final String PATH_AVAIL_SERVICES = PATH_SENSORS + "/services/available";
         public static final String PATH_SENSOR_DATA = PATH_SENSORS + "/%1/data";
         public static final String PATH_SENSOR_USERS = PATH_SENSORS + "/%1/users";
         public static final String PATH_SENSOR_DEVICE = PATH_SENSORS + "/%1/device";
         public static final String PATH_SERVICE = PATH_SENSORS + "/%1/services/%2";
         public static final String PATH_SERVICE_METHODS = PATH_SERVICE + "/methods";
         public static final String PATH_CONNECTED_SENSORS = PATH_SENSORS + "/%1/sensors";
 
         // users paths
         public static final String PATH_CURRENT_USER = PATH_USERS + "/current";
 
         // groups paths
         public static final String PATH_GROUP_USERS = PATH_GROUPS + "/%1/users";
 
         // environments paths
         public static final String PATH_ENVIRONMENT_SENSORS = PATH_ENVIRONMENTS + "/%1/sensors";
 
         private Urls() {
             // do not instantiate
         }
     }
 
     private static CommonSenseClient instance;
     private static final String JSON_TYPE = "application/json";
     private static final String WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
 
     /**
      * Creates a CommonSenseClient instance for the default API at http://api.sense-os.nl.
      * 
      * @return A CommonSenseClient instance, using singleton pattern
      * @see {@link #getClient(String)} if you want to avoid CORS issues in older browsers or IE
      */
     public static final CommonSenseClient getClient() {
         if (null == instance) {
             instance = new CommonSenseClient();
         }
         return instance;
     }
 
     /**
      * Creates a CommonSenseClient instance for a specific proxy of the CommonSense API.
      * 
      * @param proxy
      *            URL of the CommonSense proxy to use
      * @return A CommonSenseClient instance, using singleton pattern
      */
     public static final CommonSenseClient getClient(String proxy) {
         CommonSenseClient client = getClient();
         client.setProxy(proxy);
         return client;
     }
 
     /**
      * Sends request without data. Adds X-SESSION_ID header for authentication, Content-Type and
      * Accept headers data parsing.
      * 
      * @param callback
      * @param method
      * @param url
      * @param sessionId
      */
     private static void sendRequest(RequestCallback callback, Method method, String url,
             String sessionId) {
         try {
             RequestBuilder builder = new RequestBuilder(method, url);
             if (null != sessionId) {
                 builder.setHeader("X-SESSION_ID", sessionId);
             }
             builder.setHeader("Accept", JSON_TYPE);
             builder.sendRequest(null, callback);
         } catch (Exception e) {
             callback.onError(null, e);
         }
     }
 
     /**
      * Sends request with JSON data. Adds X-SESSION_ID header for authentication, Content-Type and
      * Accept headers data parsing.
      * 
      * @param callback
      * @param method
      * @param url
      * @param sessionId
      * @param jsonData
      */
     private static void sendRequest(RequestCallback callback, Method method, String url,
             String sessionId, JSONObject jsonData) {
         try {
             // serialize the json object
             String requestData = jsonData.toString();
 
             // prepare the request
             RequestBuilder builder = new RequestBuilder(method, url);
             if (null != sessionId) {
                 builder.setHeader("X-SESSION_ID", sessionId);
             }
             builder.setHeader("Content-Type", JSON_TYPE);
             builder.setHeader("Accept", JSON_TYPE);
 
             builder.sendRequest(requestData, callback);
         } catch (Exception e) {
             callback.onError(null, e);
         }
     }
 
     /**
      * Sends request with URL encoded data. Adds X-SESSION_ID header for authentication,
      * Content-Type and Accept headers data parsing.
      * 
      * @param callback
      * @param method
      * @param url
      * @param sessionId
      * @param formData
      */
     private static void sendRequest(RequestCallback callback, Method method, String url,
             String sessionId, Map<String, String> formData) {
         try {
             // put values in form urlencoded form
             String requestData = "";
             for (Entry<String, String> entry : formData.entrySet()) {
                 requestData += (requestData.length() > 0 ? "&" : "");
                 requestData += entry.getKey() + "=" + entry.getValue();
             }
 
             // prepare the request
             RequestBuilder builder = new RequestBuilder(method, url);
             if (null != sessionId) {
                 builder.setHeader("X-SESSION_ID", sessionId);
             }
             builder.setHeader("Content-Type", WWW_FORM_URLENCODED);
             builder.setHeader("Accept", JSON_TYPE);
 
             builder.sendRequest(requestData, callback);
         } catch (Exception e) {
             callback.onError(null, e);
         }
     }
 
     private String sessionId;
 
     private String proxy;
 
     private CommonSenseClient() {
         // private constructor to prevent direct instantiation
     }
 
     /**
      * 
      * @param callback
      * @param domainId
      * @param userId
      * @param username
      */
     public void addDomainUser(RequestCallback callback, String domainId, String userId,
             String username) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.POST;
         String url = getApiUrl(Urls.PATH_DOMAIN_USERS.replace("%1", domainId));
 
         // prepare request data
         JSONObject user = new JSONObject();
         if (null != userId) {
             user.put("id", new JSONString(userId));
         }
         if (null != username) {
             user.put("username", new JSONString(username));
         }
         JSONArray users = new JSONArray();
         users.set(0, user);
         JSONObject jsonData = new JSONObject();
         jsonData.put("users", users);
 
         // send request
         sendRequest(callback, method, url, sessionId, jsonData);
     }
 
     /**
      * Adds a list of sensors to an environment.
      * 
      * @param callback
      * @param environmentId
      * @param sensorIds
      */
     public void addEnvironmentSensors(RequestCallback callback, String environmentId,
             List<String> sensorIds) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.POST;
         String path = Urls.PATH_ENVIRONMENT_SENSORS.replace("%1", environmentId);
         String url = getApiUrl(path);
 
         // prepare body
         JSONArray sensors = new JSONArray();
         for (int i = 0; i < sensorIds.size(); i++) {
             JSONObject sensor = new JSONObject();
             sensor.put("id", new JSONString(sensorIds.get(i)));
             sensors.set(i, sensor);
         }
         JSONObject jsonData = new JSONObject();
         jsonData.put("sensors", sensors);
 
         // send request
         sendRequest(callback, method, url, sessionId, jsonData);
     }
 
     /**
      * Adds a user to the group.<br/>
      * <br/>
      * To add a user at least a username or user ID must be specified. Users can only add themselves
      * to a group. When joining a public group or a private group with access password a user is
      * automatically accepted in a group. To join a private group without access password members of
      * the group with the add user right should accept a user in a group via this function.
      * 
      * @param callback
      * @param groupId
      *            Group identifier
      * @param userId
      *            Identifier of the user that should be added. Optional if the username is provided
      * @param username
      *            Username of the user that should be added. Optional if the user ID is provided
      * @param listUsers
      *            Optional.
      * @param addUsers
      *            Optional.
      * @param removeUsers
      *            Optional.
      * @param listSensors
      *            Optional.
      * @param addSensors
      *            Optional.
      * @param removeSensors
      *            Optional.
      * @param editGroup
      *            Optional.
      * @param showId
      *            Optional.
      * @param showUsername
      *            Optional.
      * @param showName
      *            Optional.
      * @param showSurname
      *            Optional.
      * @param showEmail
      *            Optional.
      * @param showPhone
      *            Optional.
      * @param sensorIds
      *            Optional.
      * @param accessPassword
      *            Optional. Unhashed group access password
      */
     public void addGroupUser(RequestCallback callback, String groupId, String userId,
             String username, Boolean listUsers, Boolean addUsers, Boolean removeUsers,
             Boolean listSensors, Boolean addSensors, Boolean removeSensors, Boolean editGroup,
             Boolean showId, Boolean showUsername, Boolean showName, Boolean showSurname,
             Boolean showEmail, Boolean showPhone, List<String> sensorIds, String accessPassword) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.POST;
         String path = Urls.PATH_GROUP_USERS.replace("%1", groupId);
         String url = getApiUrl(path);
 
         // prepare request data
         JSONObject item = new JSONObject();
 
         // user
         JSONObject user = new JSONObject();
         if (null != userId) {
             user.put("id", new JSONString(userId));
         }
         if (null != username) {
             user.put("username", new JSONString(username));
         }
         item.put("user", user);
 
         // group permissions
         JSONObject permissions = new JSONObject();
         if (null != listUsers) {
             permissions.put("list_users", JSONBoolean.getInstance(listUsers));
         }
         if (null != addUsers) {
             permissions.put("add_users", JSONBoolean.getInstance(addUsers));
         }
         if (null != removeUsers) {
             permissions.put("remove_users", JSONBoolean.getInstance(removeUsers));
         }
         if (null != listSensors) {
             permissions.put("list_sensors", JSONBoolean.getInstance(listSensors));
         }
         if (null != addSensors) {
             permissions.put("add_sensors", JSONBoolean.getInstance(addSensors));
         }
         if (null != removeSensors) {
             permissions.put("remove_sensors", JSONBoolean.getInstance(removeSensors));
         }
         if (null != editGroup) {
             permissions.put("edit_group", JSONBoolean.getInstance(editGroup));
         }
         item.put("group_permissions", permissions);
 
         // display user info
         JSONObject userInfo = new JSONObject();
         if (null != showId) {
             userInfo.put("show_id", JSONBoolean.getInstance(showUsername));
         }
         if (null != showUsername) {
             userInfo.put("show_username", JSONBoolean.getInstance(showUsername));
         }
         if (null != showName) {
             userInfo.put("show_first_name", JSONBoolean.getInstance(showName));
         }
         if (null != showSurname) {
             userInfo.put("show_surname", JSONBoolean.getInstance(showSurname));
         }
         if (null != showEmail) {
             userInfo.put("show_email", JSONBoolean.getInstance(showEmail));
         }
         if (null != showPhone) {
             userInfo.put("show_phone_number", JSONBoolean.getInstance(showPhone));
         }
         item.put("display_user_info", userInfo);
 
         // share sensors
         JSONArray sensors = new JSONArray();
         if (null != sensorIds) {
             for (int i = 0; i < sensorIds.size(); i++) {
                 sensors.set(i, new JSONString(sensorIds.get(i)));
             }
         }
         item.put("sensors", sensors);
         String hashedPass = Md5Hasher.hash(accessPassword);
         item.put("access_password", new JSONString(hashedPass));
         JSONArray users = new JSONArray();
         users.set(0, item);
         JSONObject jsonData = new JSONObject();
         jsonData.put("users", users);
 
         // send request
         sendRequest(callback, method, url, sessionId, jsonData);
     }
 
     /**
      * Adds a sensor to a device. If the device does not exists then it will be created. Either a
      * device ID, or a type and UUID combination is needed. The type of the sensor will be
      * automatically be set to 1, i.e. physical.
      * 
      * @param callback
      * @param sensorId
      * @param deviceId
      * @param deviceType
      * @param deviceUuid
      */
     public void addSensorDevice(RequestCallback callback, String sensorId, String deviceId,
             String deviceType, String deviceUuid) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.POST;
         String path = Urls.PATH_SENSOR_DEVICE.replace("%1", sensorId);
         String url = getApiUrl(path);
 
         // prepare body
         JSONObject device = new JSONObject();
         if (null != deviceId) {
             device.put("id", new JSONString(deviceId));
         }
         if (null != deviceType) {
             device.put("type", new JSONString(deviceType));
         }
         if (null != deviceUuid) {
             device.put("uuid", new JSONString(deviceUuid));
         }
         JSONObject jsonData = new JSONObject();
         jsonData.put("device", device);
 
         // send request
         sendRequest(callback, method, url, sessionId, jsonData);
     }
 
     /**
      * 
      * @param callback
      * @param email
      */
     public void checkEmailAvailability(RequestCallback callback, String email) {
 
         // prepare request
         Method httpMethod = RequestBuilder.GET;
         String url = getApiUrl(Urls.PATH_USERS + "/check/email/" + email);
 
         sendRequest(callback, httpMethod, url, null);
     }
 
     /**
      * 
      * @param callback
      * @param username
      */
     public void checkUsernameAvailability(RequestCallback callback, String username) {
 
         // prepare request
         Method httpMethod = RequestBuilder.GET;
         String url = getApiUrl(Urls.PATH_USERS + "/check/username/" + username);
 
         sendRequest(callback, httpMethod, url, null);
     }
 
     /**
      * Creates a new environment at CommonSense.
      * 
      * @param callback
      * @param name
      * @param floors
      *            Indicates the amount of floors the environment has.
      * @param gpsOutline
      *            GPS outline of the environment. Should contain a list of latitude longitude points
      *            describing the outline of the environment. The list of points should create a
      *            polygon. The latitude longitude coordinates are separated by a space and each
      *            tuple by a comma. Optionally a third coordinate altitude can be specified after
      *            the longitude separated by a space. This field can have 8000 characters.
      * @param position
      *            Position of the environment. Should be the center of the environment which is also
      *            a GPS point in the order latitude longitude altitude separated by spaces.
      */
     public void createEnvironment(RequestCallback callback, String name, int floors,
             String gpsOutline, String position) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.POST;
         String path = Urls.PATH_ENVIRONMENTS;
         String url = getApiUrl(path);
 
         // prepare data
         JSONObject environment = new JSONObject();
         environment.put("name", new JSONString(name));
         environment.put("floors", new JSONNumber(floors));
         environment.put("gps_outline", new JSONString(gpsOutline));
         environment.put("position", new JSONString(position));
         JSONObject jsonData = new JSONObject();
         jsonData.put("environment", environment);
 
         // send request
         sendRequest(callback, method, url, sessionId, jsonData);
     }
 
     /**
      * Creates a new group at CommonSense.
      * 
      * @param callback
      * @param group
      */
     public void createGroup(RequestCallback callback, Group group) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.POST;
         String path = Urls.PATH_GROUPS;
         String url = getApiUrl(path);
 
         JSONObject jsonData = new JSONObject();
         jsonData.put("group", new JSONObject(group));
 
         // send request
         sendRequest(callback, method, url, sessionId, jsonData);
     }
 
     /**
      * Create a new group at CommonSense.
      * 
      * @param callback
      * @param name
      * @param email
      * @param username
      * @param password
      * @param description
      * @param publik
      * @param hidden
      * @param anonymous
      * @param accessPassword
      * @param requiredSensors
      * @param optionalSensors
      * @param defaultListUsers
      * @param defaultAddUsers
      * @param defaultRemoveUsers
      * @param defaultListSensors
      * @param defaultAddSensors
      * @param defaultRemoveSensors
      * @param reqShowUsername
      * @param reqShowName
      * @param reqShowSurname
      * @param reqShowEmail
      * @param reqShowMobile
      * @param reqShowId
      * @param reqShowZip
      * @param reqShowAddress
      * @param reqShowCountry
      */
     public void createGroup(RequestCallback callback, String name, String email, String username,
             String password, String description, Boolean publik, Boolean hidden, Boolean anonymous,
             String accessPassword, List<String> requiredSensors, List<String> optionalSensors,
             Boolean defaultListUsers, Boolean defaultAddUsers, Boolean defaultRemoveUsers,
             Boolean defaultListSensors, Boolean defaultAddSensors, Boolean defaultRemoveSensors,
             Boolean reqShowUsername, Boolean reqShowName, Boolean reqShowSurname,
             Boolean reqShowEmail, Boolean reqShowMobile, Boolean reqShowId, Boolean reqShowZip,
             Boolean reqShowAddress, Boolean reqShowCountry) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.POST;
         String path = Urls.PATH_GROUPS;
         String url = getApiUrl(path);
 
         // prepare request data
         JSONObject group = new JSONObject();
         group.put("name", new JSONString(name));
         if (null != email) {
             group.put("email", new JSONString(email));
         }
         if (null != username) {
             group.put("username", new JSONString(username));
         }
         if (null != password) {
             String hashedPass = Md5Hasher.hash(password);
             group.put("password", new JSONString(hashedPass));
         }
         if (null != description) {
             group.put("description", new JSONString(description));
         }
         if (null != publik) {
             group.put("public", JSONBoolean.getInstance(publik));
         }
         if (null != hidden) {
             group.put("hidden", JSONBoolean.getInstance(hidden));
         }
         if (null != anonymous) {
             group.put("anonymous", JSONBoolean.getInstance(anonymous));
         }
         if (null != accessPassword) {
             String hashedPass = Md5Hasher.hash(accessPassword);
             group.put("access_password", new JSONString(hashedPass));
         }
         if (null != requiredSensors) {
             JSONArray array = new JSONArray();
             for (int i = 0; i < requiredSensors.size(); i++) {
                 array.set(i, new JSONString(requiredSensors.get(i)));
             }
             group.put("required_sensors", array);
         }
         if (null != optionalSensors) {
             JSONArray array = new JSONArray();
             for (int i = 0; i < optionalSensors.size(); i++) {
                 array.set(i, new JSONString(optionalSensors.get(i)));
             }
             group.put("optional_sensors", array);
         }
         if (null != defaultListUsers) {
             group.put("default_list_users", JSONBoolean.getInstance(defaultListUsers));
         }
         if (null != defaultAddUsers) {
             group.put("default_add_users", JSONBoolean.getInstance(defaultAddUsers));
         }
         if (null != defaultRemoveUsers) {
             group.put("default_remove_users", JSONBoolean.getInstance(defaultRemoveUsers));
         }
         if (null != defaultListSensors) {
             group.put("default_list_sensors", JSONBoolean.getInstance(defaultListSensors));
         }
         if (null != defaultAddSensors) {
             group.put("default_add_sensors", JSONBoolean.getInstance(defaultAddSensors));
         }
         if (null != defaultRemoveSensors) {
             group.put("default_remove_sensors", JSONBoolean.getInstance(defaultRemoveSensors));
         }
         if (null != reqShowUsername) {
             group.put("required_show_username", JSONBoolean.getInstance(reqShowUsername));
         }
         if (null != reqShowName) {
             group.put("required_show_first_name", JSONBoolean.getInstance(reqShowName));
         }
         if (null != reqShowSurname) {
             group.put("required_show_surname", JSONBoolean.getInstance(reqShowSurname));
         }
         if (null != reqShowEmail) {
             group.put("required_show_email", JSONBoolean.getInstance(reqShowEmail));
         }
         if (null != reqShowId) {
             group.put("required_show_id", JSONBoolean.getInstance(reqShowId));
         }
         if (null != reqShowAddress) {
             group.put("required_show_address", JSONBoolean.getInstance(reqShowAddress));
         }
         if (null != reqShowZip) {
             group.put("required_show_zipcode", JSONBoolean.getInstance(reqShowZip));
         }
         if (null != reqShowCountry) {
             group.put("required_show_country", JSONBoolean.getInstance(reqShowCountry));
         }
         if (null != reqShowMobile) {
             group.put("required_show_phone_number", JSONBoolean.getInstance(reqShowMobile));
         }
         JSONObject jsonData = new JSONObject();
         jsonData.put("group", group);
 
         // send request
         sendRequest(callback, method, url, sessionId, jsonData);
     }
 
     /**
      * Creates a new sensor.
      * 
      * @param callback
      * @param name
      * @param description
      * @param displayName
      * @param dataType
      * @param dataStructure
      */
     public void createSensor(RequestCallback callback, String name, String description,
             String displayName, String dataType, String dataStructure) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.POST;
        String path = Urls.PATH_GROUPS;
         String url = getApiUrl(path);
 
         JSONObject sensor = new JSONObject();
         sensor.put("name", new JSONString(name));
         if (null != displayName) {
             sensor.put("display_name", new JSONString(displayName));
         }
         if (null != description) {
             sensor.put("device_type", new JSONString(description));
         }
         if (null != dataType) {
             sensor.put("data_type", new JSONString(dataType));
         }
         if (null != dataStructure) {
             sensor.put("display_name", new JSONString(dataStructure));
         }
         JSONObject jsonData = new JSONObject();
         jsonData.put("sensor", sensor);
 
         // send request
         sendRequest(callback, method, url, sessionId, jsonData);
     }
 
     /**
      * 
      * @param callback
      * @param sensorId
      * @param value
      * @param timestamp
      */
     public void createSensorData(RequestCallback callback, String sensorId, String value,
             long timestamp) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.POST;
         String path = Urls.PATH_SENSOR_DATA.replace("%1", sensorId);
         String url = getApiUrl(path);
 
         // prepare request data
         String date = NumberFormat.getFormat("#.000").format(timestamp / 1000d);
         JSONObject dataPoint = new JSONObject();
         dataPoint.put("value", new JSONString(value));
         dataPoint.put("date", new JSONString(date));
         JSONObject jsonData = new JSONObject();
         jsonData.put("data", dataPoint);
 
         // send request
         sendRequest(callback, method, url, sessionId, jsonData);
     }
 
     /**
      * Creates a user account. The response content will contain the created user information. The
      * UUID is a uniquely generated ID which can be used to retrieve data without logging in.
      * 
      * @param callback
      * @param username
      *            Username (must be unique)
      * @param password
      *            Unhashed password
      * @param name
      * @param surname
      * @param phone
      * @param email
      * @param address
      * @param zipCode
      * @param country
      * @param disableMail
      */
     public void createUser(RequestCallback callback, String username, String password, String name,
             String surname, String phone, String email, String address, String zipCode,
             String country, Boolean disableMail) {
 
         // prepare request properties
         Method method = RequestBuilder.POST;
         String path = Urls.PATH_USERS;
 
         if (null != disableMail) {
             path += "?disable_mail=" + (disableMail ? "1" : "0");
         }
         String url = getApiUrl(path);
 
         // prepare data
         JSONObject user = new JSONObject();
         user.put("username", new JSONString(username));
         user.put("password", new JSONString(Md5Hasher.hash(password)));
         if (null != email) {
             user.put("email", new JSONString(email));
         }
         if (null != name) {
             user.put("name", new JSONString(name));
         }
         if (null != surname) {
             user.put("surname", new JSONString(surname));
         }
         if (null != phone) {
             user.put("mobile", new JSONString(phone));
         }
         if (null != address) {
             user.put("address", new JSONString(address));
         }
         if (null != zipCode) {
             user.put("zipcode", new JSONString(zipCode));
         }
         if (null != country) {
             user.put("country", new JSONString(country));
         }
         JSONObject jsonData = new JSONObject();
         jsonData.put("user", user);
 
         // send request
         sendRequest(callback, method, url, sessionId, jsonData);
     }
 
     /**
      * 
      * @param callback
      * @param environmentId
      */
     public void deleteEnvironment(RequestCallback callback, String environmentId) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.DELETE;
         String path = Urls.PATH_ENVIRONMENTS + "/" + environmentId;
         String url = getApiUrl(path);
 
         // send request
         sendRequest(callback, method, url, sessionId);
     }
 
     /**
      * Deletes the group if the group has no other members. If the group does have other members,
      * the current user will be removed from the group. When a user leaves a group all his shared
      * sensors are automatically removed from this group.
      * 
      * @param callback
      * @param groupId
      */
     public void deleteGroup(RequestCallback callback, String groupId) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.DELETE;
         String path = Urls.PATH_GROUPS + "/" + groupId;
         String url = getApiUrl(path);
 
         // send request
         sendRequest(callback, method, url, sessionId);
     }
 
     /**
      * Deletes a sensor at CommonSense. If the current user is the owner of the sensor then the
      * sensor will be removed from the current user and all other users. If the current user is not
      * owner of the sensor then access to the sensor will be removed for this user.
      * 
      * @param callback
      * @param sensorId
      */
     public void deleteSensor(RequestCallback callback, String sensorId) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.DELETE;
         String path = Urls.PATH_SENSORS + "/" + sensorId;
         String url = getApiUrl(path);
 
         // send request
         sendRequest(callback, method, url, sessionId);
     }
 
     /**
      * 
      * @param callback
      * @param sensorId
      * @param dataId
      */
     public void deleteSensorData(RequestCallback callback, String sensorId, String dataId) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.DELETE;
         String path = Urls.PATH_SENSOR_DATA.replace("%1", sensorId) + "/" + dataId;
         String url = getApiUrl(path);
 
         // send request
         sendRequest(callback, method, url, sessionId);
     }
 
     /**
      * 
      * @param callback
      * @param sensorId
      * @param serviceId
      */
     public void disconnectService(RequestCallback callback, String sensorId, String serviceId) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.DELETE;
         String path = Urls.PATH_SERVICE.replace("%1", sensorId).replace("%2", serviceId);
         String url = getApiUrl(path);
 
         // send request
         sendRequest(callback, method, url, sessionId);
     }
 
     /**
      * @param callback
      *            RequestCallback to handle HTTP response
      * @param username
      * @param email
      */
     public void forgotPassword(RequestCallback callback, String username, String email) {
 
         // prepare request details
         Method method = RequestBuilder.POST;
         String url = getApiUrl(Urls.PATH_FORGOT_PASSWORD);
 
         // prepare request data
         Map<String, String> formData = new HashMap<String, String>();
         if (null != username) {
             formData.put("username", username);
         } else {
             formData.put("email", email);
         }
 
         // send request
         sendRequest(callback, method, url, null, formData);
     }
 
     /**
      * Gets a list of all visible groups.
      * 
      * @param callback
      * @param perPage
      *            Optional. Specifies which page of the results must be retrieved. The page offset
      *            starts at 0.
      * @param page
      *            Optional. Specifies the amount of items that must be received at once. The maximum
      *            amount is 1000 items and the default amount is 100 items.
      * @param publik
      *            Optional. If publik is true, only public groups are returned, if public is false
      *            only private groups are returned.
      * 
      * @see #getGroups(RequestCallback, Integer, Integer, Boolean, Boolean)
      */
     public void getAllGroups(RequestCallback callback, Integer perPage, Integer page, Boolean publik) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.GET;
         String path = Urls.PATH_GROUPS + "/all";
         List<String> params = new ArrayList<String>();
         if (null != page) {
             params.add("page=" + page.toString());
         }
         if (null != perPage) {
             params.add("per_page=" + perPage.toString());
         }
         if (null != publik) {
             params.add("public=" + (publik ? "1" : "0"));
         }
         String url = getApiUrl(path, params.toArray(new String[0]));
 
         // send request
         sendRequest(callback, method, url, sessionId);
     }
 
     /**
      * Creates a URL to the path in the CommonSense API. Uses a proxy if this was specified.
      * 
      * @param path
      *            The path in the CommonSense API, e.g. sensors/1/data
      * @param params
      *            Set of URL query parameters, e.g. "details=full"
      * @return A URL String
      */
     private String getApiUrl(String path, String... params) {
 
         // create URL
         String url = "";
         if (null == proxy) {
             UrlBuilder urlBuilder = new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                     .setPath(path);
             url = urlBuilder.buildString();
         } else {
             url = proxy + "/" + path;
         }
 
         // create parameters String
         String parameters = "";
         for (int i = 0; i < params.length; i++) {
             parameters += "&" + params[i];
         }
         parameters = parameters.replaceFirst("&", "?");
 
         return url + parameters;
     }
 
     /**
      * 
      * @param callback
      * @param perPage
      * @param page
      * @param groupId
      */
     public void getAvailableServices(RequestCallback callback, Integer perPage, Integer page,
             String groupId) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.GET;
         String path = Urls.PATH_AVAIL_SERVICES;
 
         List<String> params = new ArrayList<String>();
         if (null != page) {
             params.add("page=" + page.toString());
         }
         if (null != perPage) {
             params.add("per_page=" + perPage.toString());
         }
         if (null != groupId) {
             params.add("group_id=" + groupId);
         }
         String url = getApiUrl(path, params.toArray(new String[0]));
 
         sendRequest(callback, method, url, sessionId);
     }
 
     /**
      * 
      * @param callback
      * @param sensorId
      */
     public void getConnectedSensors(RequestCallback callback, String sensorId) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.GET;
         String path = Urls.PATH_CONNECTED_SENSORS.replace("%1", sensorId);
         String url = getApiUrl(path);
 
         // send request
         sendRequest(callback, method, url, sessionId);
     }
 
     /**
      * @param callback
      *            RequestCallback to handle HTTP response
      */
     public void getCurrentUser(RequestCallback callback) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         Method method = RequestBuilder.GET;
         String url = getApiUrl(Urls.PATH_CURRENT_USER);
 
         // send request
         sendRequest(callback, method, url, sessionId);
     }
 
     /**
      * 
      * @param callback
      * @param perPage
      * @param page
      */
     public void getEnvironments(RequestCallback callback, Integer perPage, Integer page) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.GET;
         String path = Urls.PATH_ENVIRONMENTS;
 
         List<String> params = new ArrayList<String>();
         if (null != page) {
             params.add("page=" + page.toString());
         }
         if (null != perPage) {
             params.add("per_page=" + perPage.toString());
         }
         String url = getApiUrl(path, params.toArray(new String[0]));
 
         // send request
         sendRequest(callback, method, url, sessionId);
     }
 
     /**
      * Gets the details of a group. Details of a private group can only be viewed by members and
      * details of a public group by all users.
      * 
      * @param callback
      * @param groupId
      */
     public void getGroupDetails(RequestCallback callback, String groupId) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.GET;
         String path = Urls.PATH_GROUPS + "/" + groupId;
         String url = getApiUrl(path);
 
         // send request
         sendRequest(callback, method, url, sessionId);
     }
 
     /**
      * Gets the list of groups that the user is a member of.
      * 
      * @param callback
      * @param perPage
      *            Optional. Specifies which page of the results must be retrieved. The page offset
      *            starts at 0.
      * @param page
      *            Optional. Specifies the amount of items that must be received at once. The maximum
      *            amount is 1000 items and the default amount is 100 items.
      * @param total
      *            Optional. By adding this parameter a total item count will be added to the result.
      * @param publik
      *            Optional. With this parameter set to 1, only public groups are returned and when
      *            it is set to 0 only private groups are returned.
      * @see #getAllGroups(RequestCallback, Integer, Integer, Boolean)
      */
     public void getGroups(RequestCallback callback, Integer perPage, Integer page, Boolean total,
             Boolean publik) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.GET;
         String path = Urls.PATH_GROUPS;
 
         List<String> params = new ArrayList<String>();
         if (null != page) {
             params.add("page=" + page.toString());
         }
         if (null != perPage) {
             params.add("per_page=" + perPage.toString());
         }
         if (null != total) {
             params.add("total=" + (total ? "1" : "0"));
         }
         if (null != publik) {
             params.add("public=" + (publik ? "1" : "0"));
         }
         String url = getApiUrl(path, params.toArray(new String[0]));
 
         // send request
         sendRequest(callback, method, url, sessionId);
     }
 
     /**
      * 
      * @param callback
      * @param groupId
      * @param perPage
      * @param page
      */
     public void getGroupUsers(RequestCallback callback, String groupId, Integer perPage,
             Integer page) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.GET;
         String path = Urls.PATH_GROUP_USERS.replace("%1", groupId);
 
         List<String> params = new ArrayList<String>();
         if (null != page) {
             params.add("page=" + page.toString());
         }
         if (null != perPage) {
             params.add("per_page=" + perPage.toString());
         }
         String url = getApiUrl(path, params.toArray(new String[0]));
 
         // send request
         sendRequest(callback, method, url, sessionId);
     }
 
     /**
      * @param callback
      *            RequestCallback to handle HTTP response
      * @param sensorId
      *            ID of the sensor to request data for
      * @param startDate
      * @param endDate
      * @param date
      * @param perPage
      * @param page
      * @param interval
      * @param next
      * @param last
      */
     public void getSensorData(RequestCallback callback, String sensorId, Long startDate,
             Long endDate, Long date, Integer perPage, Integer page, Integer interval, Boolean next,
             Boolean last, String sort) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.GET;
         String path = Urls.PATH_SENSOR_DATA.replace("%1", sensorId);
 
         List<String> params = new ArrayList<String>();
         if (null != startDate) {
             String dateString = NumberFormat.getFormat("#.000").format(
                     startDate.longValue() / 1000d);
             params.add("start_date=" + dateString);
         }
         if (null != endDate) {
             String dateString = NumberFormat.getFormat("#.000").format(endDate.longValue() / 1000d);
             params.add("end_date=" + dateString);
         }
         if (null != date) {
             String dateString = NumberFormat.getFormat("#.000").format(date.longValue() / 1000d);
             params.add("date=" + dateString);
         }
         if (null != perPage) {
             params.add("per_page=" + perPage.toString());
         }
         if (null != page) {
             params.add("page=" + page.toString());
         }
         if (null != interval) {
             params.add("interval=" + interval.toString());
         }
         if (null != last) {
             params.add("last=" + (last ? "1" : "0"));
         }
         if (null != next) {
             params.add("next=" + (next ? "1" : "0"));
         }
         if (null != sort) {
             params.add("sort=" + sort);
         }
         String url = getApiUrl(path, params.toArray(new String[0]));
 
         // send request
         sendRequest(callback, method, url, sessionId);
     }
 
     /**
      * @param callback
      *            RequestCallback to handle HTTP response
      * @param perPage
      * @param page
      * @param shared
      * @param owned
      * @param physical
      * @param details
      * @param groupId
      */
     public void getSensors(RequestCallback callback, Integer perPage, Integer page, Boolean shared,
             Boolean owned, Boolean physical, String details, String groupId) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.GET;
         String path = Urls.PATH_SENSORS;
 
         List<String> params = new ArrayList<String>();
         if (null != page) {
             params.add("page=" + page.toString());
         }
         if (null != perPage) {
             params.add("per_page=" + perPage.toString());
         }
         if (null != shared) {
             params.add("shared=" + (shared ? "1" : "0"));
         }
         if (null != owned) {
             params.add("owned=" + (owned ? "1" : "0"));
         }
         if (null != physical) {
             params.add("physical=" + (physical ? "1" : "0"));
         }
         if (null != details) {
             params.add("details=" + details);
         }
         if (null != groupId) {
             params.add("group_id=" + groupId);
         }
         String url = getApiUrl(path, params.toArray(new String[0]));
 
         // send request
         sendRequest(callback, method, url, sessionId);
     }
 
     /**
      * 
      * @param callback
      * @param sensorId
      * @param serviceId
      * @param methodName
      */
     public void getServiceMethodDetails(RequestCallback callback, String sensorId,
             String serviceId, String methodName) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.GET;
         String path = Urls.PATH_SERVICE.replace("%1", sensorId).replace("%2", serviceId) + "/"
                 + methodName;
         String url = getApiUrl(path);
 
         // send request
         sendRequest(callback, method, url, sessionId);
     }
 
     /**
      * 
      * @param callback
      * @param sensorId
      * @param serviceId
      */
     public void getServiceMethods(RequestCallback callback, String sensorId, String serviceId) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.GET;
         String path = Urls.PATH_SERVICE_METHODS.replace("%1", sensorId).replace("%2", serviceId);
         String url = getApiUrl(path);
 
         // send request
         sendRequest(callback, method, url, sessionId);
     }
 
     /**
      * @return The session ID, or null if it was not set
      */
     public String getSessionId() {
         return sessionId;
     }
 
     /**
      * Tries to connect the current user account with his or her Google account
      */
     public void googleConnect() {
 
         // set the current location as the callback
         UrlBuilder callbackBuilder = new UrlBuilder();
         callbackBuilder.setProtocol(Location.getProtocol());
         callbackBuilder.setHost(Location.getHost());
         callbackBuilder.setPath(Location.getPath());
         for (Entry<String, List<String>> entry : Location.getParameterMap().entrySet()) {
             if (entry.getKey().equals("session_id") || entry.getKey().equals("error")) {
                 // do not include these parameters
             } else {
                 callbackBuilder.setParameter(entry.getKey(), entry.getValue()
                         .toArray(new String[0]));
             }
         }
         String callbackUrl = callbackBuilder.buildString();
 
         // relocate to OpenID handler
         Location.replace(new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost(Urls.HOST)
                 .setPath(Urls.PATH_LOGIN_GOOGLE).setParameter("callback_url", callbackUrl)
                 .setParameter("session_id", sessionId).buildString());
     }
 
     /**
      * Relocates the browser to the Google Open ID login page.
      */
     public void googleLogin() {
 
         // set the current location as the callback
         UrlBuilder callbackBuilder = new UrlBuilder();
         callbackBuilder.setProtocol(Location.getProtocol());
         callbackBuilder.setHost(Location.getHost());
         callbackBuilder.setPath(Location.getPath());
         for (Entry<String, List<String>> entry : Location.getParameterMap().entrySet()) {
             if (entry.getKey().equals("session_id") || entry.getKey().equals("error")) {
                 // do not include these parameters
             } else {
                 callbackBuilder.setParameter(entry.getKey(), entry.getValue()
                         .toArray(new String[0]));
             }
         }
         String callbackUrl = callbackBuilder.buildString();
 
         // relocate to OpenID handler
         Location.replace(new UrlBuilder().setProtocol(Urls.PROTOCOL).setHost("api.sense-os.nl")
                 .setPath(Urls.PATH_LOGIN_GOOGLE).setParameter("callback_url", callbackUrl)
                 .buildString());
     }
 
     /**
      * Tries to log in. Stores the session ID if the call was successful.
      * 
      * @param callback
      *            RequestCallback to handle HTTP response
      * @param username
      *            Username
      * @param password
      *            Password (unhashed)
      */
     public void login(final RequestCallback callback, String username, String password) {
 
         // prepare request properties
         Method method = RequestBuilder.POST;
         String url = getApiUrl(Urls.PATH_LOGIN);
 
         // prepare request data
         String hashedPass = Md5Hasher.hash(password);
         Map<String, String> formData = new HashMap<String, String>();
         formData.put("username", username);
         formData.put("password", hashedPass);
 
         RequestCallback requestCallback = new RequestCallback() {
 
             @Override
             public void onError(Request request, Throwable exception) {
                 callback.onError(request, exception);
             }
 
             @Override
             public void onResponseReceived(Request request, Response response) {
                 try {
                     // try to get the session ID from the header
                     if (response.getStatusCode() == Response.SC_OK) {
                         String sessionId = response.getHeader("X-SESSION_ID");
                         setSessionId(sessionId);
                     }
                 } catch (Exception e) {
                     // ignore
                 }
                 callback.onResponseReceived(request, response);
             }
         };
 
         // send request
         sendRequest(requestCallback, method, url, null, formData);
     }
 
     /**
      * @param callback
      */
     public void logout(RequestCallback callback) {
 
         Method method = RequestBuilder.GET;
         String url = getApiUrl(Urls.PATH_LOGOUT);
 
         // send request
         sendRequest(callback, method, url, sessionId);
     }
 
     /**
      * Removes a user from the group. Only members with the right to delete users can perform this
      * action. If a user leaves a group then all his shared sensors will be removed from this group
      * automatically.
      * 
      * @param callback
      * @param groupId
      * @param userId
      */
     public void removeGroupUser(RequestCallback callback, String groupId, String userId) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.DELETE;
         String path = Urls.PATH_GROUP_USERS.replace("%1", groupId) + "/" + userId;
         String url = getApiUrl(path);
 
         // send request
         sendRequest(callback, method, url, sessionId);
     }
 
     /**
      * 
      * @param callback
      * @param password
      * @param token
      */
     public void resetPassword(RequestCallback callback, String password, String token) {
 
         // prepare request details
         Method method = RequestBuilder.POST;
         String url = getApiUrl(Urls.PATH_PW_RESET);
 
         // prepare request data
         String hashedPass = Md5Hasher.hash(password);
         Map<String, String> formData = new HashMap<String, String>();
         formData.put("token", token);
         formData.put("password", hashedPass);
 
         sendRequest(callback, method, url, null, formData);
     }
 
     public void setProxy(String proxy) {
         this.proxy = proxy;
     }
 
     /**
      * 
      * @param callback
      * @param sensorId
      * @param serviceId
      * @param value
      * @param timestamp
      */
     public void setServiceManualData(RequestCallback callback, String sensorId, String serviceId,
             String value, long timestamp) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.POST;
         String path = Urls.PATH_SERVICE.replace("%1", sensorId).replace("%2", serviceId)
                 + "/manualData";
         String url = getApiUrl(path);
 
         // prepare request data
         String date = NumberFormat.getFormat("#.000").format(timestamp / 1000d);
         JSONObject dataPoint = new JSONObject();
         dataPoint.put("value", new JSONString(value));
         dataPoint.put("date", new JSONString(date));
         JSONObject jsonData = new JSONObject();
         jsonData.put("data", dataPoint);
 
         // send request
         sendRequest(callback, method, url, sessionId, jsonData);
     }
 
     /**
      * 
      * @param callback
      * @param sensorId
      * @param serviceId
      * @param methodName
      * @param parameters
      */
     public void setServiceMethodDetails(RequestCallback callback, String sensorId,
             String serviceId, String methodName, String... parameters) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.POST;
         String path = Urls.PATH_SERVICE.replace("%1", sensorId).replace("%2", serviceId) + "/"
                 + methodName;
         String url = getApiUrl(path);
 
         // prepare request data
         JSONArray params = new JSONArray();
         for (int i = 0; i < parameters.length; i++) {
             String parameter = parameters[i];
             params.set(i, new JSONString(parameter));
         }
         JSONObject jsonData = new JSONObject();
         jsonData.put("parameters", params);
 
         // send request
         sendRequest(callback, method, url, sessionId, jsonData);
     }
 
     /**
      * @param sessionId
      *            The session ID to set
      */
     public void setSessionId(String sessionId) {
         this.sessionId = sessionId;
     }
 
     /**
      * Add a user to a sensor, giving the user access to the sensor and data. Only the owner of the
      * sensor is able to upload data, mutate sensors and add users to their sensor. To add a user at
      * least a username or user ID must be specified.
      * 
      * @param callback
      * @param sensorId
      * @param userId
      * @param username
      */
     public void shareSensor(RequestCallback callback, String sensorId, String userId,
             String username) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.POST;
         String path = Urls.PATH_SENSOR_USERS.replace("%1", sensorId);
         String url = getApiUrl(path);
 
         // prepare request data
         JSONObject user = new JSONObject();
         if (null != userId) {
             user.put("id", new JSONString(userId));
         }
         if (null != username) {
             user.put("username", new JSONString(username));
         }
         JSONObject jsonData = new JSONObject();
         jsonData.put("user", user);
 
         // send request
         sendRequest(callback, method, url, sessionId, jsonData);
     }
 
     /**
      * Removes a users from a sensor, which removes the access to the sensor for this user.
      * 
      * @param callback
      * @param sensorId
      * @param userId
      */
     public void unshareSensor(RequestCallback callback, String sensorId, String userId) {
 
         // check if there is a session ID
         if (null == sessionId) {
             callback.onError(null, new Exception("Not logged in"));
             return;
         }
 
         // prepare request properties
         Method method = RequestBuilder.DELETE;
         String path = Urls.PATH_SENSOR_USERS.replace("%1", sensorId) + "/" + userId;
         String url = getApiUrl(path);
 
         // send request
         sendRequest(callback, method, url, sessionId);
     }
 }
