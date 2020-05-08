 package com.jcertif.presentation.data;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.HttpURLConnection;
 import java.net.URLEncoder;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import com.jcertif.presentation.data.domain.JCertifPresentation;
 import com.jcertif.presentation.data.domain.JCertifPresentationKind;
 import com.jcertif.presentation.data.domain.JCertifSpeaker;
 import com.jcertif.presentation.data.domain.MyScheduleUser;
 import com.jcertif.presentation.data.domain.impl.JCertifPresentationComparator;
 import com.jcertif.presentation.data.domain.impl.JCertifPresentationImpl;
 import com.jcertif.presentation.data.domain.impl.JCertifSpeakerImpl;
 import com.jcertif.presentation.data.http.HttpClient;
 import com.jcertif.presentation.data.http.HttpResponse;
 import com.jcertif.presentation.data.http.impl.HttpClientImpl;
 
 /**
  * Facade for the JCertif REST API.
  * 
  * You can inject your own HttpClient implementation by using the constructor
  * taking it as a parameter or you can use the default implementation by using
  * the no-arg constructor.
  */
 public class RestApiFacadeImpl implements RestApiFacade, LazyLoadProvider {
 
     private final Logger logger = Logger.getLogger(getClass());
 
     private final HttpClient httpClient;
 
     private static final int JCERTIF_EVENT_ID = 1;
 
     private static final String JCERTIF_JSON_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
 
     public static final String REST_API_BASE_URL = "http://cfp.JCERTIF.com/rest/v1";
 
     public static final String SCHEDULE_URL = REST_API_BASE_URL + "/events/" + JCERTIF_EVENT_ID + "/schedule";
 
     public static final String MY_SCHEDULE_ACTIVATE_URL = REST_API_BASE_URL + "/events/users/activate";
 
     public static final String MY_SCHEDULE_VALIDATION_URL = REST_API_BASE_URL + "/events/users/validate";
 
     public static final String SEARCH_URL = REST_API_BASE_URL + "/events/" + JCERTIF_EVENT_ID + "/presentations/search";
 
     private static final String UTF_8 = "utf-8";
 
     public RestApiFacadeImpl() {
         // this(new OfflineHttpClientMock("20101112110640"));
         this(new HttpClientImpl());
     }
 
     public RestApiFacadeImpl(final HttpClient httpClient) {
         if (logger.isDebugEnabled()) {
             logger.debug("Initializing RestApiFacade with HttpClient " + httpClient.getClass().getName());
         }
         this.httpClient = httpClient;
     }
 
     /**
      * {@inheritDoc}
      */
    @Override
     public void activateMySchedule(final String firstName, final String lastName, final String email)
             throws RestApiException {
         try {
             final StringBuilder params = new StringBuilder(100);
             params.append("firstName=").append(URLEncoder.encode(firstName, UTF_8));
             params.append('&');
             params.append("lastName=").append(URLEncoder.encode(lastName, UTF_8));
             params.append('&');
             params.append("email=").append(URLEncoder.encode(email, UTF_8));
 
             final int response = httpClient.post(MY_SCHEDULE_ACTIVATE_URL, params.toString());
 
             if (response != HttpURLConnection.HTTP_CREATED) {
                 logger.error("Response code: " + response);
                 throw new RestApiException("MySchedule activation failed. Please try again later.");
             }
         } catch (final IOException e) {
             logger.error(e.getMessage());
             throw new RestApiException("MySchedule activation failed. Please try again later.");
         }
     }
 
     /**
      * {@inheritDoc}
      */
    @Override
     public void saveMySchedule(final MyScheduleUser user) throws RestApiException {
         if (user.getActivationCode() == null || user.getEmail() == null) {
             throw new IllegalArgumentException("Activation code and e-mail must be set for the user.");
         }
         if (user.getFavourites() == null) {
             throw new IllegalArgumentException("User must have favourites to save.");
         }
 
         try {
             final StringBuilder params = new StringBuilder(100);
             params.append("code=").append(URLEncoder.encode(user.getActivationCode(), UTF_8));
             for (final Integer favouriteId : user.getFavourites()) {
                 params.append('&');
                 params.append("favorites=");
                 params.append(favouriteId);
             }
 
             final int response = httpClient.post(SCHEDULE_URL + "/" + user.getEmail(), params.toString());
 
             if (response != HttpURLConnection.HTTP_CREATED) {
                 logger.error("Response code: " + response);
 
                 if (response == HttpURLConnection.HTTP_CONFLICT) {
                     user.setActivationCode(null);
                     throw new RestApiException("Activation code rejected. Please try signing in again.");
 
                 }
                 throw new RestApiException("Adding to MySchedule failed. Please try again later.");
             }
         } catch (final IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
    @Override
     public boolean isValidUser(final MyScheduleUser user) throws RestApiException {
         try {
             final StringBuilder params = new StringBuilder(100);
             params.append("email=").append(URLEncoder.encode(user.getEmail(), UTF_8));
             params.append('&');
             params.append("code=").append(URLEncoder.encode(user.getActivationCode(), UTF_8));
 
             final int response = httpClient.post(MY_SCHEDULE_VALIDATION_URL, params.toString());
             if (response == HttpURLConnection.HTTP_OK) {
                 return true;
             } else if (response == HttpURLConnection.HTTP_CONFLICT) {
                 return false;
             } else {
                 logger.error("Response code: " + response);
                 throw new RestApiException("MySchedule validation failed. Please try again later.");
             }
         } catch (final IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
    @Override
     public void getScheduleForUser(final MyScheduleUser user) {
         if (user != null && user.getEmail() != null) {
             try {
                 final HttpResponse response = httpClient.get(SCHEDULE_URL + "/" + user.getEmail());
 
                 if (response.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
                     // user has no favourites yet
                     user.setFavourites(new HashSet<Integer>());
                 } else if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
                     // parse the response
                     user.setFavourites(getScheduleIds(httpClient.get(SCHEDULE_URL + "/" + user.getEmail()).getResponse()));
                 }
 
                 if (logger.isDebugEnabled()) {
                     if (user.getFavourites() != null) {
                         logger.debug("Retrieved " + user.getFavourites().size() + " favourites for user " + user.getEmail());
                     }
                 }
             } catch (final IOException e) {
                 throw new RuntimeException(e);
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
    @Override
     public List<JCertifPresentation> getFullSchedule() {
         try {
             return getScheduleData(httpClient.get(SCHEDULE_URL).getResponse());
         } catch (final IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
    @Override
     public List<JCertifPresentation> search(final String tag) {
         String searchJson;
         try {
             searchJson = httpClient.get(SEARCH_URL + "?tags=" + tag).getResponse();
             final Set<Integer> ids = getScheduleIds(searchJson);
             final List<JCertifPresentation> result = new ArrayList<JCertifPresentation>(ids.size());
 
             // Use the full schedule to reuse JCERTIFPresentation instances
             // possibly already created.
             for (final JCertifPresentation presentation : getFullSchedule()) {
                 if (ids.contains(presentation.getId())) {
                     result.add(presentation);
                 }
             }
             return result;
         } catch (final IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     protected Set<Integer> getScheduleIds(final String scheduleJson) {
         final Set<Integer> result = new HashSet<Integer>();
         try {
             if (scheduleJson != null && scheduleJson.length() > 0) {
                 final JSONArray jsonArray = new JSONArray(scheduleJson);
                 for (int i = 0; i < jsonArray.length(); i++) {
                     final JSONObject json = (JSONObject) jsonArray.get(i);
                     if (json.has("id")) {
                         result.add(json.getInt("id"));
                     }
                 }
             }
         } catch (final JSONException e) {
             throw new RuntimeException(e);
         }
 
         return result;
     }
 
     protected List<JCertifPresentation> getScheduleData(final String scheduleJson) {
         final List<JCertifPresentation> result = new ArrayList<JCertifPresentation>();
         try {
             if (scheduleJson != null && scheduleJson.length() > 0) {
                 final JSONArray jsonArray = new JSONArray(scheduleJson);
                 for (int i = 0; i < jsonArray.length(); i++) {
                     final JSONObject json = (JSONObject) jsonArray.get(i);
                     result.add(parsePresentation(json));
                 }
             }
         } catch (final JSONException e) {
             throw new RuntimeException(e);
         }
 
         // sort the schedule
         Collections.sort(result, new JCertifPresentationComparator());
 
         return result;
     }
 
     /**
      * Parses a JCERTIFPresentation object from the given JSONObject.
      * 
      * @param json
      * @return
      * @throws JSONException
      */
     private JCertifPresentation parsePresentation(final JSONObject json) throws JSONException {
         final DateFormat df = new SimpleDateFormat(JCERTIF_JSON_DATE_PATTERN);
 
         try {
             final JCertifPresentationKind kind = JCertifPresentationKind.valueOf(json.getString("kind").toUpperCase()
                     .replaceAll(" ", "_"));
 
             int id = 0;
             final Date fromTime = df.parse(json.getString("fromTime"));
             final Date toTime = df.parse(json.getString("toTime"));
             final String room = json.getString("room");
             final boolean partnerSlot = json.getBoolean("partnerSlot");
             final String code = json.getString("code");
             final String type = json.getString("type");
 
             String presentationUri = null;
             if (json.has("presentationUri")) {
                 presentationUri = json.getString("presentationUri");
 
                 // parse the id from the presentationUri
                 id = Integer.valueOf(presentationUri.substring(presentationUri.lastIndexOf("/") + 1));
             }
 
             String title = "TBA";
             if (kind.isSpeak()) {
                 if (json.has("title")) {
                     title = json.getString("title");
                 }
             } else {
                 title = code;
             }
 
             final List<JCertifSpeaker> speakers = new ArrayList<JCertifSpeaker>();
             if (json.has("speakers")) {
                 final JSONArray speakersJson = json.getJSONArray("speakers");
                 for (int i = 0; i < speakersJson.length(); i++) {
                     final String speakerUri = ((JSONObject) speakersJson.get(i)).getString("speakerUri");
                     final String speakerName = ((JSONObject) speakersJson.get(i)).getString("speaker");
                     final int speakerId = Integer.valueOf(speakerUri.substring(speakerUri.lastIndexOf("/") + 1));
 
                     // wrap the speaker inside a lazy loading proxy
                     speakers.add(LazyLoadProxyFactory.getProxy(new JCertifSpeakerImpl(speakerId, speakerName, speakerUri),
                             this));
                 }
             }
 
             final JCertifPresentationImpl event = new JCertifPresentationImpl(id, fromTime, toTime, code, type, kind, title,
                     speakers, room, partnerSlot, presentationUri);
 
             // wrap the presentation inside a lazy loading proxy
             return LazyLoadProxyFactory.getProxy(event, this);
         } catch (final ParseException e) {
             throw new JSONException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
    @Override
     public void lazyLoadFields(final LazyLoadable lazy) {
         if (lazy.getLazyLoadingUri() == null) {
             return;
         }
 
         try {
             if (logger.isDebugEnabled()) {
                 logger.debug("Lazy loading object details " + lazy.getLazyLoadingUri());
             }
 
             final JSONObject jsonData = new JSONObject(httpClient.get(lazy.getLazyLoadingUri()).getResponse());
 
             for (final Method method : lazy.getClass().getMethods()) {
                 if (method.getName().startsWith("get") && method.isAnnotationPresent(LazyLoad.class)) {
 
                     final Class<?> returnType = method.getReturnType();
                     Method setterMethod = null;
 
                     try {
                         // find a setter for the value
                         setterMethod = lazy.getClass().getMethod("set" + method.getName().substring(3), returnType);
                         String jsonField = method.getAnnotation(LazyLoad.class).value();
                         String jsonSubField = null;
                         if (jsonField.contains("/")) {
                             final String temp = jsonField;
                             jsonField = temp.substring(0, temp.indexOf("/"));
                             jsonSubField = temp.substring(temp.indexOf("/") + 1);
                         }
 
                         Object value = null;
                         if (jsonData.has(jsonField)) {
                             if (returnType.equals(Set.class) && jsonSubField != null) {
                                 final JSONArray array = jsonData.getJSONArray(jsonField);
                                 final Set<String> result = new HashSet<String>(array.length());
                                 for (int i = 0; i < array.length(); i++) {
                                     final JSONObject jsonObj = (JSONObject) array.get(i);
                                     if (jsonObj.has(jsonSubField)) {
                                         result.add(jsonObj.getString(jsonSubField));
                                     }
                                 }
                                 value = result;
                             } else if (returnType.equals(int.class)) {
                                 value = jsonData.getInt(jsonField);
                             } else {
                                 // assume String
                                 value = jsonData.getString(jsonField);
                             }
                         } else {
                             logger.warn("No field found for name " + jsonField);
                         }
 
                         // call the setter
                         setterMethod.invoke(lazy, value);
                     } catch (final NoSuchMethodException e) {
                         logger.error("No matching setter found for getter: " + method.getName());
                     } catch (final IllegalArgumentException e) {
                         logger.error("Illegal argument for setter " + setterMethod.getName() + ": " + e.getMessage());
                     } catch (final IllegalAccessException e) {
                         logger.error("Illegal access to setter " + setterMethod.getName() + ": " + e.getMessage());
                     } catch (final InvocationTargetException e) {
                         logger.error("Couldn't invoke setter " + setterMethod.getName() + ": " + e.getMessage());
                     }
                 }
             }
         } catch (final JSONException e) {
             throw new RuntimeException(e);
         } catch (final IOException e) {
             throw new RuntimeException(e);
         }
     }
 }
