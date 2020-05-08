 package controllers;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonPrimitive;
 import libs.lastfm.LastFMApi;
 import models.Artist;
 import models.City;
 import models.User;
 import models.enums.Gender;
 import play.exceptions.UnexpectedException;
 import play.libs.WS;
 import play.libs.ws.WSUrlFetch;
 import play.mvc.Controller;
 import play.mvc.Scope;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 
 public class FBConnect extends Controller {
 
     private static final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
 
     private static final String GENERAL_PROFILE_URL = "https://graph.facebook.com/me?access_token=";
     private static final String MUSIC_PROFILE_URL = "https://graph.facebook.com/me/music?access_token=";
     private static final String CLIENT_ID = "157297254334553";
     private static final String CLIENT_SECRET = "702df18b116a24006e240cbe8168b62f";
 
 
     public static void callback() {
         String code = params.get("code");
         String error = params.get("error");
         if (error != null) {
             String landUrl = "/";
             landUrl += landUrl.contains("?") ? "&" : "?";
             landUrl += "error_reason=" + WS.encode(params.get("error_reason")) +
                     "&error_description=" + WS.encode(params.get("error_description")) +
                     "&error=" + WS.encode(params.get("error"));
             redirect(landUrl);
         }
         if (code != null) {
             String authUrl = "https://graph.facebook.com/oauth/access_token?client_id="+CLIENT_ID+"&redirect_uri=http://localhost:9000/fbconnect/callback&client_secret="+CLIENT_SECRET+"&code=" + code;
             WSUrlFetch ws = new WSUrlFetch();
             String response = ws.newRequest(authUrl).get().getString();
             String accessToken = null;
             Integer expires = null;
             String[] pairs = response.split("&");
             for (String pair : pairs) {
                 String[] kv = pair.split("=");
                 if (kv.length != 2) {
                     throw new UnexpectedException("Module tags.fbconnect got an unexpected auth response from facebook");
                 } else {
                     if (kv[0].equals("access_token")) {
                         accessToken = kv[1];
                     }
                     if (kv[0].equals("expires")) {
                         expires = Integer.valueOf(kv[1]);
                     }
                 }
             }
             if (accessToken != null && expires != null) {
                 try {
                     String uri = GENERAL_PROFILE_URL + WS.encode(accessToken);
                     JsonObject jsonData = ws.newRequest(uri).get().getJson().getAsJsonObject();
                     jsonData.add("accessToken", new JsonPrimitive(accessToken));
                     jsonData.add("expires", new JsonPrimitive(expires));
 
                     uri = MUSIC_PROFILE_URL + WS.encode(accessToken);
                     JsonArray jsonMusic = ws.newRequest(uri).get().getJson().getAsJsonObject().get("data").getAsJsonArray();
                     jsonData.add("musics", jsonMusic);
 
                     String email = jsonData.get("email").getAsString();
                     User user = User.find("byEmail", email).first();
                     if(user == null) {
                         user = convertToUserAndSave(jsonData);
                     }
 
                    session.put("user", email);
 
                 } catch (Exception e) {
                     throw new RuntimeException("Unexpected: " + e);
                 }
             } else {
                 throw new UnexpectedException("Module tags.fbconnect could not find access token and expires in facebook callback");
             }
         }
         redirect("/welcome");
     }
 
     public static User convertToUserAndSave(JsonObject data) throws ParseException {
         String email = data.get("email").getAsString();
         String firstName = data.get("first_name").getAsString();
         String lastName = data.get("last_name").getAsString();
 
 
         JsonElement birthdayElement = data.get("birthday");
         String strBirthDate = birthdayElement != null ? birthdayElement.getAsString() : null;
         Date birthDate = dateFormat.parse(strBirthDate);
 
         JsonElement interestedInElement = data.get("interested_in");
         String strInterestedIn = interestedInElement != null ? interestedInElement.getAsString() : null;
 
         JsonElement locationElement = data.get("location");
         String cityName = locationElement != null ? locationElement.getAsJsonObject().get("name").getAsString() : null;
 
         JsonElement genderElement = data.get("gender");
         String strGender = genderElement != null ? genderElement.getAsString() : null;
         Gender gender = Gender.findByName(strGender);
 
         JsonElement musicsElement = data.get("musics");
         JsonArray jsonMusics = musicsElement != null ? musicsElement.getAsJsonArray() : null;
         List<Artist> artists = new ArrayList<Artist>();
         for (JsonElement jsonMusic : jsonMusics) {
             String artistName = jsonMusic.getAsJsonObject().get("name").getAsString();
             Artist artist = Artist.find("byName", artistName).first();
             if(artist == null) {
                 artist = new LastFMApi("1cae0d3a28fc36a955ea9241610d113a").retrieveArtist(artistName, LastFMApi.PictureSize.LARGE_SQUARE);
                 if(artist == null) continue;
                 Artist.em().persist(artist);
             }
             artists.add(artist);
         }
 
         Long facebookId = data.get("id").getAsLong();
 
         City city = City.find("byName", cityName).first();
         if(city == null) {
             city = new City();
             city.name = cityName;
         }
 
         User user = new User(firstName, lastName, email, gender, birthDate, city, strInterestedIn);
         user.facebookId = facebookId;
         user.artists = artists;
         user.save();
 
         return user;
     }
 
 }
