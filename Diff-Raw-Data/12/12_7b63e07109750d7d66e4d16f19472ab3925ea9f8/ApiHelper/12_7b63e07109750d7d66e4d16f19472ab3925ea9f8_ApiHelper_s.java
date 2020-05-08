 package org.knetwork.webapp.util;
 
 import java.lang.reflect.Type;
 import java.util.List;
 
 import org.knetwork.webapp.entity.Badge;
 import org.knetwork.webapp.entity.Exercise;
 import org.knetwork.webapp.entity.User;
 import org.knetwork.webapp.oauth.KhanOAuthService;
 import org.scribe.model.Token;
 
 import com.google.gson.FieldNamingPolicy;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.reflect.TypeToken;
 
 public class ApiHelper {

     private final Token accessToken;
     private final KhanOAuthService oauthService;
     private final KhanAcademyApi api;
     private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
 
     public ApiHelper(final Token accessToken, final KhanOAuthService oauthService, final KhanAcademyApi api) {
         this.accessToken = accessToken;
         this.oauthService = oauthService;
         this.api = api;
     }
 
     public List<Badge> getBadges() {
         final String json = getResponseBody(api.getExercisesEndpoint());
         final Type badgesType = new TypeToken<List<Badge>>(){}.getType();
         return gson.fromJson(json, badgesType);
     }
 
     public List<Exercise> getExercises() {
         final String json = getResponseBody(api.getExercisesEndpoint());
         final Type exercisesType = new TypeToken<List<Exercise>>(){}.getType();
         return gson.fromJson(json, exercisesType);
     }
 
     private String getResponseBody(final String requestEndpoint) {
        return oauthService.getSignedRequestUrl(accessToken, requestEndpoint);
     }
 
     public User getUser() {
         final String json = getResponseBody(api.getUserEndpoint());
         return gson.fromJson(json, User.class);
     }
 }
