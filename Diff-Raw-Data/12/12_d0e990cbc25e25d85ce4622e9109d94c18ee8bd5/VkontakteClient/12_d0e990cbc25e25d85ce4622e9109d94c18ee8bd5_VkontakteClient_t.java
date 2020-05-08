 package com.musicbox.vkontakte;
 
 import com.google.gson.Gson;
 import com.musicbox.WebWorker;
 import com.musicbox.vkontakte.structure.audio.AudioSearch;
 import com.musicbox.vkontakte.structure.profiles.Profile;
 import com.musicbox.vkontakte.structure.profiles.ProfileSearch;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 public class VkontakteClient {
     private OAuthToken oauth;
     private Gson json = new Gson();
 
     public VkontakteClient(OAuthToken token) {
         this.oauth = token;
     }
 
     public String getURLByTrack(String track) {
        return this.json.fromJson(retrieveReader("execute?code=" + URLEncoder.encode("return API.audio.search({\"q\":\"" + track + "\",\"count\":1, \"sort\":2})@.url[1];")), AudioSearch.class).getResponse();
     }
 
     public Profile getProfile() {
         return getProfileById(oauth.getUser_id());
     }
 
     public Profile getProfileById(int id) {
         Profile profile = this.json
                 .fromJson(
                         retrieveReader(
                                 "getProfiles?uids="
                                         .concat(String.valueOf(id))
                                         .concat("&fields=photo_big,sex,bdate,city,country"),
                                 this.oauth.getAccess_token()), ProfileSearch.class).getResponse()
                 .get(0);
         profile.setToken(this.oauth);
 
         return profile;
     }
 
     private Reader retrieveReader(String query) {
         return retrieveReader(query, this.oauth.getAccess_token());
     }
 
     private Reader retrieveReader(String query, String token) {
         String url = "https://api.vkontakte.ru/method/".concat(query)
                 .concat("&access_token=").concat(token);
        //System.out.println(url);

         InputStream source = WebWorker.retrieveStream(url);
         try {
             return new InputStreamReader(source, "utf-8");
         } catch (UnsupportedEncodingException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return null;
     }
 
     public static OAuthToken getOauthTokenByCode(String code) {
         String query = "https://oauth.vkontakte.ru/access_token?client_id=2810768&client_secret=OP1L2XAhJHfgEHg8Y1Vu&code="
                 .concat(code);
         Gson json = new Gson();
         return json.fromJson(
                 new InputStreamReader(WebWorker.retrieveStream(query)),
                 OAuthToken.class);
     }
 }
