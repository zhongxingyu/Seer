 package de.hdodenhof.holoreader.backend.servlets;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.api.client.http.GenericUrl;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.google.appengine.labs.repackaged.org.json.JSONArray;
 import com.google.appengine.labs.repackaged.org.json.JSONException;
 import com.google.appengine.labs.repackaged.org.json.JSONObject;
 
 import de.hdodenhof.holoreader.backend.exception.GCMException;
 import de.hdodenhof.holoreader.backend.exception.InvalidFeedException;
 import de.hdodenhof.holoreader.backend.gcm.GCMService;
 import de.hdodenhof.holoreader.backend.parser.FeedValidator;
 import de.hdodenhof.holoreader.backend.persistence.entities.DeviceEntity;
 import de.hdodenhof.holoreader.backend.persistence.entities.UserEntity;
 import de.hdodenhof.holoreader.backend.persistence.services.UserAndDeviceService;
 
 public class HoloreaderServlet extends HttpServlet {
 
     private static final long serialVersionUID = 1L;
 
     @SuppressWarnings("unused")
    private final Logger logger = Logger.getLogger(getClass().getName());
 
     @Override
     public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
         UserService userService = UserServiceFactory.getUserService();
 
         if (userService.isUserLoggedIn()) {
             GenericUrl url = new GenericUrl(request.getRequestURL().toString());
             url.setRawPath("/");
             String redirectAfterLogout = url.build();
 
             String eMail = userService.getCurrentUser().getEmail();
             UserAndDeviceService userAndDeviceService = new UserAndDeviceService();
             UserEntity user = userAndDeviceService.get(eMail);
 
             if (user == null) {
                 user = userAndDeviceService.storeDummyUser(eMail);
             }
 
             HashMap<String, String> devices = new HashMap<String, String>();
             for (DeviceEntity device : user.getDevices()) {
                 devices.put(device.getWebsaveKey(), device.getDevice());
             }
 
             request.setAttribute("loggedIn", true);
             request.setAttribute("name", request.getUserPrincipal().getName());
             request.setAttribute("logoutLink", userService.createLogoutURL(redirectAfterLogout));
             request.setAttribute("devices", devices);
         } else {
             String loginUrl = userService.createLoginURL("/");
 
             request.setAttribute("loggedIn", false);
             request.setAttribute("loginLink", loginUrl);
         }
         RequestDispatcher rd = request.getRequestDispatcher("WEB-INF/jsp/holoreader.jsp");
         rd.forward(request, response);
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         response.setContentType("application/json");
 
         UserService userService = UserServiceFactory.getUserService();
 
         if (userService.isUserLoggedIn()) {
             StringBuilder sb = new StringBuilder();
             BufferedReader br = request.getReader();
             String str;
             while ((str = br.readLine()) != null) {
                 sb.append(str);
             }
 
             String requestEntity = sb.toString();
 
             ArrayList<Device> devices = parseDevices(requestEntity);
             ArrayList<Feed> feeds = parseFeeds(requestEntity);
 
             if (devices != null && feeds != null) {
                 StringBuilder json = new StringBuilder();
                 HashMap<String, Boolean> resultMap = prepareResponse(feeds, json);
 
                 UserAndDeviceService us = new UserAndDeviceService();
 
                 GCMService gcmService = new GCMService();
                 ArrayList<String> regIds = new ArrayList<String>();
 
                 for (Device device : devices) {
                     try {
                         DeviceEntity deviceEntity = us.loadDevice(device.id);
                         regIds.add(deviceEntity.getRegId());
                     } catch (NullPointerException e) {
                         // device was deleted
                     }
                 }
 
                 try {
                     if (resultMap.containsValue(true)) {
                         gcmService.sendMessage(regIds, json.toString());
                     }
 
                     JSONObject responseEntity = new JSONObject();
                     for (Map.Entry<String, Boolean> entry : resultMap.entrySet()) {
                         responseEntity.put(entry.getKey(), entry.getValue());
                     }
                     response.setStatus(HttpServletResponse.SC_OK);
                     response.getWriter().print(responseEntity);
                 } catch (GCMException e) {
                     response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                 } catch (JSONException e) {
                     response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                 }
             } else {
                 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
             }
         } else {
             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
         }
     }
 
     private ArrayList<Feed> parseFeeds(String entity) {
         ArrayList<Feed> feeds = new ArrayList<Feed>();
 
         try {
             JSONObject jsonRequest = new JSONObject(entity);
             JSONArray jsonFeeds = jsonRequest.getJSONArray("feeds");
 
             for (int i = 0; i < jsonFeeds.length(); i++) {
                 JSONObject jsonFeed = jsonFeeds.getJSONObject(i);
 
                 Feed feed = new Feed();
                 feed.id = jsonFeed.getString("inputid");
                 feed.url = jsonFeed.getString("url");
 
                 feeds.add(feed);
             }
 
         } catch (JSONException e1) {
             return null;
         }
 
         return feeds;
     }
 
     private ArrayList<Device> parseDevices(String entity) {
         ArrayList<Device> devices = new ArrayList<Device>();
 
         try {
             JSONObject jsonRequest = new JSONObject(entity);
             JSONArray jsonDevices = jsonRequest.getJSONArray("devices");
 
             for (int i = 0; i < jsonDevices.length(); i++) {
                 JSONObject jsonDevice = jsonDevices.getJSONObject(i);
 
                 Device device = new Device();
                 device.id = jsonDevice.getString("id");
 
                 devices.add(device);
             }
         } catch (JSONException e1) {
             return null;
         }
 
         return devices;
     }
 
     private HashMap<String, Boolean> prepareResponse(ArrayList<Feed> feeds, StringBuilder json) {
         HashMap<String, Boolean> resultMap = new HashMap<String, Boolean>();
 
         json.append("[");
         for (Feed feed : feeds) {
             if (!resultMap.containsKey(feed.id)) {
                 try {
                     Map<String, String> result = FeedValidator.parseFeed(feed.url);
                     json.append("{");
                     json.append("\"title\":\"" + result.get(FeedValidator.RESULT_NAME) + "\"");
                     json.append(",");
                     json.append("\"url\":\"" + result.get(FeedValidator.RESULT_URL) + "\"");
                     json.append("}");
                     json.append(",");
                     resultMap.put(feed.id, true);
                 } catch (InvalidFeedException e) {
                     resultMap.put(feed.id, false);
                 }
             }
         }
         if (json.length() > 1) {
             json.setLength(json.length() - 1);
         }
         json.append("]");
 
         return resultMap;
     }
 
     private class Feed {
         String id;
         String url;
     }
 
     private class Device {
         String id;
     }
 
 }
