 package team13.client;
 
 import java.util.HashMap;
 
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.http.client.URL;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.user.client.Window;
 
 public class FBFetcher {
 	public static FBFetcher currentFetcher;
 	private int NUM_OF_JSONS = 4;
 	private HashMap<String, JSONObject> jsonMap = new HashMap<String, JSONObject>();
 	
 	public FBFetcher(){
 		FBFetcher.currentFetcher = this;
 	}
 	
 	public void loginFB(){
 		String loginURL = "https://www.facebook.com/dialog/oauth?client_id=195431067216915&redirect_uri="
 				+ Window.Location.getHref() + "&scope=user_photos,user_status&response_type=token";
 		Window.Location.replace(loginURL);
 	}
 	
 	public void logoutFB(){
 		String accessToken = Window.Location.getHash().replaceFirst("#access_token=", "");
 		String backURL = Window.Location.getHref().split("#")[0];
 		String logoutURL = "https://www.facebook.com/logout.php?next=" + backURL
 				+ "&access_token=" + accessToken;
 		Window.Location.replace(logoutURL);
 	}
 	
 	public JSONObject getJSON(String key){
 		return jsonMap.get(key);
 	}
 	
 	public boolean isFetchFinished(){
 		if(jsonMap.size() == NUM_OF_JSONS){
 			return true;
 		}else{
 			return false;
 		}
 	}
 	
 	public void fetchNeededJSON(){
 		String accessToken = Window.Location.getHash().replaceFirst("#access_token=", "");
 		
 		String meURL = "https://graph.facebook.com/me?fields=id,name&access_token=" + accessToken;
 		String friendsURL = "https://graph.facebook.com/me/friends?access_token=" + accessToken;
		String photosURL = "https://graph.facebook.com/me/photos?limit=0&access_token=" + accessToken;
		String statusesURL = "https://graph.facebook.com/me/statuses?limit=0&access_token=" + accessToken;
 		
 		requestJSON("me", meURL);
 		requestJSON("friends", friendsURL);
 		requestJSON("photos", photosURL);
 		requestJSON("statuses", statusesURL);
 	}
 	
 	private void requestJSON(String jsonName, String url){
 		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));
 		
 		try {
 			builder.sendRequest(null, new JSONRequestCallback(jsonName));
 		} catch (RequestException e) {
 			MainPage.currentPage.displayError("Couldn't retrieve JSON");
 		}
 	}
 	
 	private class JSONRequestCallback implements RequestCallback{
 		private String jsonName;
 
 		public JSONRequestCallback(String jsonName){
 			this.jsonName = jsonName;
 		}
 		
 		@Override
 		public void onResponseReceived(Request request, Response response) {
 			if(200 == response.getStatusCode()){
 				jsonMap.put(jsonName, JSONParser.parseStrict(response.getText()).isObject());
 				MainPage.currentPage.checkJSON();
 			}else{
 				MainPage.currentPage.displayError("Couldn't retrieve JSON (" + response.getStatusText() + ")");
 			}
 		}
 
 		@Override
 		public void onError(Request request, Throwable exception) {
 			MainPage.currentPage.displayError("Couldn't retrieve JSON");
 		}
 	}
 }
