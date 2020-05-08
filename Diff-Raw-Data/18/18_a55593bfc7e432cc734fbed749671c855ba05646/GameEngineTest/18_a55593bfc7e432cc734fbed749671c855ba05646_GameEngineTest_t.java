 package unabomber.client.test;
 
 import junit.framework.TestCase;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.params.HttpParams;
 
 import android.location.Location;
 
 import unabomber.client.GameEngine;
 import unabomber.client.UnabomberHttpClient;
 
 public class GameEngineTest extends TestCase {	
 	private GameEngine gameEngine;
 	private MockHttpClient httpClient;
 	private static final String DEVICE_ID = "fake-device-id";
 
 	@Override
 	protected void setUp() throws Exception {
 		httpClient = new MockHttpClient(null);
 		gameEngine = new GameEngine(httpClient, DEVICE_ID);
 	}
 
	public void testSendLocationSendsPutRequest() {
 		gameEngine.sendLocation(null);
 		
 		HttpUriRequest request = httpClient.getRequest();
		assertEquals("PUT", request.getMethod());
 	}
 	
 	public void testSendLocationRequestContainsCurrentLocation() {
 		Location currentLocation = new Location("GPS");
 		gameEngine.sendLocation(currentLocation);
 		
 		HttpParams params = httpClient.getRequest().getParams();
 		assertEquals(currentLocation, (Location)(params.getParameter("current_location")));
 	}
 	
 	public void testSendLocationRequestIsToTheProperUri() {
 		gameEngine.sendLocation(null);
 		
 		HttpUriRequest request = httpClient.getRequest();
		assertEquals("http://10.0.2.2:3000/players/" + DEVICE_ID + "/update", request.getURI().toString());
 	}
 	
 	public final class MockHttpClient extends UnabomberHttpClient {
 		private HttpResponse response;
 		private HttpUriRequest request;
 		
 		private MockHttpClient(HttpResponse response) {
 			this.response = response;
 		}
 		
 		public HttpUriRequest getRequest() {
 			return request;
 		}
 
 		@Override
 		public HttpResponse executeRequest(HttpUriRequest request) {
 			this.request = request;
 			return response;
 		}
 	}
 }
