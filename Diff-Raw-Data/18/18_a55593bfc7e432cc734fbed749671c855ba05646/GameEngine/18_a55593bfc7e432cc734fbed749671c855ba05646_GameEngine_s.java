 package unabomber.client;
 
import org.apache.http.client.methods.HttpPost;
 import org.apache.http.params.BasicHttpParams;
 
 import android.location.Location;
 
 
 public class GameEngine {
 	private UnabomberHttpClient httpClient;
 	private String deviceId;
 
 	public GameEngine(UnabomberHttpClient httpClient, String deviceId) {
 		this.httpClient = httpClient;
 		this.deviceId = deviceId;
 	}
 	
 	public GameEngine(String deviceId) {
 		this(new UnabomberHttpClient(), deviceId);
 	}
 
 	public void sendLocation(Location currentLocation) {
		HttpPost request = new HttpPost("http://localhost:3000/players/" + deviceId + "/update");
 		BasicHttpParams params = new BasicHttpParams();
 		params.setParameter("current_location", currentLocation);
 		request.setParams(params);
 		httpClient.executeRequest(request);
 	}
 }
