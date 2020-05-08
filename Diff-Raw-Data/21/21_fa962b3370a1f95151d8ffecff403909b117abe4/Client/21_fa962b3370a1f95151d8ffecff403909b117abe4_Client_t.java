 package com.github.codingricky.runkeeperclient;
 
 import com.github.codingricky.runkeeperclient.model.Record;
 import com.github.codingricky.runkeeperclient.model.TeamMember;
 import com.github.codingricky.runkeeperclient.model.TeamFeed;
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import com.github.codingricky.runkeeperclient.model.FitnessActivityFeed;
 import com.github.codingricky.runkeeperclient.model.FitnessActivity;
 import com.github.codingricky.runkeeperclient.model.User;
 import com.github.codingricky.runkeeperclient.model.WeightFeed;
 
 import java.io.IOException;
 import java.lang.reflect.Type;
 import java.util.Collection;
 import java.util.List;
 
 public class Client {
 
     public static final String PRODUCTION_URL = "http://api.runkeeper.com";
     public static final String USER_RESOURCE = "/user";
 
     private final HttpClient httpClient;
     private final String accessToken;
     private final String url;
     private final Gson gson;
 
     private User user;
 
     public Client(String accessToken) {
         this(PRODUCTION_URL, accessToken);
     }
 
     public Client(String url, String accessToken) {
         this.url = url;
         this.accessToken = accessToken;
         httpClient = new DefaultHttpClient();
         gson = new Gson();
         user = getUser();
     }
 
     public User getUser(Callback<User> callback) {
         HttpGet get = createHttpGetRequest(USER_RESOURCE, ContentTypes.USER);
         return execute(get, User.class, callback);
     }
 
     public User getUser() {
         return getUser(null);
     }
 
     public FitnessActivityFeed getFitnessActivities(Callback<FitnessActivityFeed> callback) {
         HttpGet get = createHttpGetRequest(user.getFitnessActivities(), ContentTypes.FITNESS_ACTIVITY_FEED);
         return execute(get, FitnessActivityFeed.class, callback);
     }
 
     public FitnessActivityFeed getFitnessActivities() {
         return getFitnessActivities(null);
     }
 
     public FitnessActivity getFitnessActivity(String resource, Callback<FitnessActivity> callback) {
         HttpGet get = createHttpGetRequest(resource, ContentTypes.FITNESS_ACTIVITY);
         return execute(get, FitnessActivity.class, callback);
     }
 
     public FitnessActivity getFitnessActivity(String resource) {
         return getFitnessActivity(resource, null);
     }
 
     public WeightFeed getWeightFeed() {
        HttpGet get = createHttpGetRequest(user.getWeight(), ContentTypes.WEIGHT_FEED);
         return execute(get, WeightFeed.class, null);
     }
 
     public TeamFeed getTeamFeed() {
         HttpGet get = createHttpGetRequest(user.getTeam(), ContentTypes.TEAM_FEED);
         return execute(get, TeamFeed.class);
     }
 
     public TeamMember getTeamMember(String resource) {
         HttpGet get = createHttpGetRequest(resource, ContentTypes.MEMBER);
         return execute(get, TeamMember.class);
     }
 
     public List<Record> getRecords() {
         HttpGet get = createHttpGetRequest(user.getRecords(), ContentTypes.RECORDS);
         Type collectionType = new TypeToken<Collection<Record>>(){}.getType();
         try {
             HttpResponse response = httpClient.execute(get);
             int statusCode = response.getStatusLine().getStatusCode();
             if (statusCode == HttpStatus.SC_OK) {
                 String entity = EntityUtils.toString(response.getEntity());
                 return gson.fromJson(entity, collectionType);
             } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                 throw new SecurityException();
             } else {
                 throw new ClientException("Unexpected statusCode " + statusCode);
             }
         } catch (IOException e) {
             throw new ClientException(e);
         }
     }
 
     private HttpGet createHttpGetRequest(String resource, String contentType) {
         String fullUrl = String.format("%s%s", url, resource);
         HttpGet get = new HttpGet(fullUrl);
         get.addHeader("Accept", contentType);
         get.addHeader("Authorization", "Bearer " + accessToken);
         return get;
     }
 
     private <T> T execute(HttpGet get, Class<T> clazz) {
         return execute(get, clazz, null);
     }
 
     private <T> T execute(HttpGet get, Class<T> clazz, Callback<T> callback) {
         try {
             HttpResponse response = httpClient.execute(get);
             int statusCode = response.getStatusLine().getStatusCode();
             if (statusCode == HttpStatus.SC_OK) {
                 String entity = EntityUtils.toString(response.getEntity());
                 T parsedObject = gson.fromJson(entity, clazz);
                 if (callback != null) {
                     callback.success(parsedObject, entity);
                 }
                 return parsedObject;
             } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                 throw new SecurityException();
             } else {
                 throw new ClientException("Unexpected statusCode " + statusCode);
             }
         } catch (IOException e) {
             throw new ClientException(e);
         }
     }
 }
