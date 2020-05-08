 package myhomeaudio.server.http.helper.client;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import myhomeaudio.server.client.Client;
 import myhomeaudio.server.database.object.DatabaseClient;
 import myhomeaudio.server.database.object.DatabaseNode;
 import myhomeaudio.server.database.object.DatabaseUser;
 import myhomeaudio.server.http.HTTPMimeType;
 import myhomeaudio.server.http.StatusCode;
 import myhomeaudio.server.http.helper.Helper;
 import myhomeaudio.server.http.helper.HelperInterface;
 import myhomeaudio.server.locations.layout.DeviceObject;
 import myhomeaudio.server.locations.layout.NodeSignalBoundary;
 import myhomeaudio.server.locations.layout.NodeSignalRange;
 import myhomeaudio.server.manager.ClientManager;
 import myhomeaudio.server.manager.NodeManager;
 import myhomeaudio.server.manager.UserManager;
 import myhomeaudio.server.node.Node;
 import myhomeaudio.server.node.NodeCommands;
 import myhomeaudio.server.user.User;
 
 import org.apache.http.HttpStatus;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 public class ClientHelper extends Helper implements HelperInterface, StatusCode {
 
 	public String getOutput(ArrayList<String> uriSegments, String data) {
 
 		// Set the content-type
 		this.contentType = HTTPMimeType.MIME_JSON;
 		this.httpStatus = HttpStatus.SC_BAD_REQUEST;
 
 		// Create the JSON object to represent the response
 		JSONObject body = new JSONObject();
 		body.put("status", STATUS_FAILED);
 
 		ClientManager cm = ClientManager.getInstance();
 		UserManager um = UserManager.getInstance();
 
 		try {
 			// Convert the request into a JSON object
 			JSONObject jsonRequest = (JSONObject) JSONValue.parse(data);
 
 			if (uriSegments.get(1).equals("login")) {
 				System.out.println("Getting login from client");
 
 				// Make sure we have all the required fields
 				if (jsonRequest.containsKey("username")
 						&& jsonRequest.containsKey("password")
 						&& jsonRequest.containsKey("ipaddress")
 						&& jsonRequest.containsKey("macaddress")
 						&& jsonRequest.containsKey("bluetoothname")) {
 
 					User lUser = new User((String) jsonRequest.get("username"),
 							(String) jsonRequest.get("password"));
 
 					// Check that the login succeeded
 					if (um.loginUser(lUser) == STATUS_OK) {
 						Client lClient = new Client(
 								(String) jsonRequest.get("macaddress"),
 								(String) jsonRequest.get("ipaddress"),
 								(String) jsonRequest.get("bluetoothname"));
 
 						// Get the session by logging the client in
 						String sessionId = cm.loginClient(lClient,
 								um.getUser(lUser.getUsername()).getId());
 
 						if (sessionId != null) {
 
 							// Get whether user has previously configured
 							// network
 							boolean configured = cm.getClient(sessionId)
 									.isConfigured();
 
 							body.put("status", STATUS_OK);
 							body.put("session", sessionId);
 							body.put("configured", configured);
 							this.httpStatus = HttpStatus.SC_OK;
 						}
 					}
 				}
 
 			} else if (uriSegments.get(1).equals("logout")) {
 				System.out.println("Getting logout from client");
 
 				if (jsonRequest.containsKey("session")) {
 					int userId = cm.logoutClient((String) jsonRequest
 							.get("session"));
 
 					if (userId != -1) {
 						um.logoutUser(userId);
 						body.put("status", STATUS_OK);
 						this.httpStatus = HttpStatus.SC_OK;
 					}
 				}
 
 			} else if (uriSegments.get(1).equals("locations")) {
 				if (jsonRequest.containsKey("session")
 						&& jsonRequest.containsKey("locations")) {
 
 					JSONArray locations = (JSONArray) jsonRequest
 							.get("locations");
 					Iterator<JSONObject> i = locations.iterator();
 
 					ArrayList<DeviceObject> devices = new ArrayList<DeviceObject>(
 							locations.size());
 
 					// Convert the JSON object-based locations into
 					// DeviceObjects
 					while (i.hasNext()) {
 						JSONObject jsonDevice = i.next();
 
 						DeviceObject device = new DeviceObject(
								((Long)jsonDevice.get("id")).intValue(),
								((Long)jsonDevice.get("rssi")).intValue());
 
 						devices.add(device);
 					}
 
 					// Pass off the session and DeviceObject list to the
 					// ClientManager
 					if (cm.updateClientLocation(
 							(String) jsonRequest.get("session"), devices)) {
 						body.put("status", STATUS_OK);
 						this.httpStatus = HttpStatus.SC_OK;
 					}
 				}
 			} else if (uriSegments.get(1).equals("initialconfig")) {
 				if (jsonRequest.containsKey("session")
 						&& jsonRequest.containsKey("signatures")) {
 					if (cm.isValidClient((String) jsonRequest.get("session"))) {
 
 						NodeManager nm = NodeManager.getInstance();
 
 						ArrayList<NodeSignalBoundary> nodeSignatures = new ArrayList<NodeSignalBoundary>();
 
 						JSONArray signaturesArray = (JSONArray) jsonRequest
 								.get("signatures");
 
 						// For each node, get the signal ranges of all other
 						// nodes
 						for (Iterator<JSONObject> i = signaturesArray
 								.iterator(); i.hasNext();) {
 							JSONObject nextSignature = i.next();
 
 							// TODO node seems to be inactive
 							DatabaseNode node = nm
 									.getNodeById(((Long) nextSignature
 											.get("id")).intValue());
 
 							JSONArray foundNodes = (JSONArray) (nextSignature
 									.get("foundNodes"));
 
 							NodeSignalBoundary nodeSignalBoundary = new NodeSignalBoundary(
 									node.getId());
 
 							// For each found node, get their id and max/min
 							// signal values
 							for (Iterator<JSONObject> j = foundNodes.iterator(); j
 									.hasNext();) {
 								JSONObject nextFoundNode = j.next();
 								
 								nodeSignalBoundary
 										.addNodeRange(new NodeSignalRange(
 												((Long) nextFoundNode.get("id"))
 														.intValue(),
 												((Long) nextFoundNode
 														.get("min")).intValue(),
 												((Long) nextFoundNode
 														.get("max")).intValue()));
 							}
 
 							nodeSignatures.add(nodeSignalBoundary);
 						}
 
 						// Save the new configuration
 						if(cm.changeClientInitialization(
 								(String) jsonRequest.get("session"),
 								nodeSignatures)){
 							body.put("status", STATUS_OK);
 							this.httpStatus = HttpStatus.SC_OK;
 						}
 
 						
 					}
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			// Do nothing for now
 		}
 
 		return body.toString();
 	}
 
 }
