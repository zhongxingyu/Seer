 package de.kile.zapfmaster2000.client;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import javax.ws.rs.core.MediaType;
 
 import org.jboss.resteasy.client.ClientRequest;
 import org.jboss.resteasy.client.ClientResponse;
 
 import de.kile.zapfmaster2000.rest.api.box.DrawRequest;
 import de.kile.zapfmaster2000.rest.api.box.LoginRequest;
 
 public class MockClient {
 
	 private static final String URL = "http://localhost:8080/Zapfmaster2000RESTful/rest/";
 //	 private static final String URL = "http://localhost:8080/de.kile.zapfmaster2000.rest/rest/";
//	private static final String URL = "http://zapfmaster2000.dyndns.org:9130/zapfmaster2000-restful-1.0.0-SNAPSHOT/rest/";
 
 	private static final int UPDATE_RATE = 250;
 
 	private String boxPassphrase = "box-1";
 
 	public static void main(String[] pArgs) throws IOException {
 		System.out.println("Zapfmaster 2000 Mock Client");
 		System.out.println("Will send requests to " + URL);
 		new MockClient().run();
 	}
 
 	public void run() {
 		System.out.println("Commands: ");
 		System.out.println("\tboxpassphrase [passphrase]");
 		System.out.println("\tlogin [rfidid]");
 		System.out.println("\tdraw [amount] [duration]");
 
 		InputStreamReader is = new InputStreamReader(System.in);
 		BufferedReader in = new BufferedReader(is);
 
 		while (true) {
 			try {
 				String command = in.readLine();
 				String[] segments = command.split(" ");
 
 				switch (segments[0]) {
 				case "boxpassphrase":
 					assertParameterCount(segments, 1);
 					boxPassphrase = segments[1];
 					break;
 				case "login":
 					assertParameterCount(segments, 1);
 					performLogin(Long.parseLong(segments[1]));
 					break;
 				case "draw":
 					assertParameterCount(segments, 2);
 					performDraw(Double.parseDouble(segments[1]),
 							Double.parseDouble(segments[2]));
 					break;
 				default:
 					System.out.println("unknown command: " + segments[0]);
 				}
 
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 		}
 	}
 
 	private void performLogin(long pRfid) throws Exception {
 		LoginRequest loginRequest = new LoginRequest();
 		loginRequest.setBoxPassphrase(boxPassphrase);
 		loginRequest.setRfidTag(pRfid);
 
 		ClientRequest request = new ClientRequest(URL + "box/login");
 		ClientResponse<?> response = request.body(MediaType.APPLICATION_JSON,
 				loginRequest).post();
 		System.out.println("Response: " + response.getStatus());
 	}
 
 	private void performDraw(double pAmount, double pDuration) throws Exception {
 //		Thread.sleep(10000);
 //		performLogin(100);
 		int rawTicks = (int) (pAmount * 5200);
 
 		int numRequests = (int) (pDuration * 1000) / UPDATE_RATE;
 		int ticksPerUpdate = rawTicks / numRequests;
 
 		for (int i = 0; i < numRequests; ++i) {
 			DrawRequest drawRequest = new DrawRequest();
 			drawRequest.setBoxPassphrase(boxPassphrase);
 			drawRequest.setTicks(ticksPerUpdate);
 
 			ClientRequest request = new ClientRequest(URL + "box/draw");
 			ClientResponse<?> response = request.body(
 					MediaType.APPLICATION_JSON, drawRequest).post();
 			System.out.println("Response: " + response.getStatus());
 
 			Thread.sleep(UPDATE_RATE);
 		}
 	}
 
 	private void assertParameterCount(String[] pCommand, int pExpectedCount) {
 		if ((pCommand.length - 1) != pExpectedCount) {
 			throw new IllegalArgumentException("Wrong parmaeter count: "
 					+ pExpectedCount + " parameter expected.");
 		}
 	}
 }
