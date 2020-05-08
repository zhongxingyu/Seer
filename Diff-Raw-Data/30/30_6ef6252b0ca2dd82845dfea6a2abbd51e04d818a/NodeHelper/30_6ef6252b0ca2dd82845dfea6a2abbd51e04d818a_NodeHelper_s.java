 package myhomeaudio.server.http.helper;
 
 import java.util.ArrayList;
 
 import org.apache.http.HttpStatus;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 import myhomeaudio.server.http.HTTPMimeType;
 import myhomeaudio.server.http.StatusCode;
 import myhomeaudio.server.manager.ClientManager;
 import myhomeaudio.server.manager.NodeManager;
 import myhomeaudio.server.node.Node;
 import myhomeaudio.server.node.NodeCommands;
 
 public class NodeHelper extends Helper implements HelperInterface,
 		NodeCommands, StatusCode {
 
 	@Override
 	public String getOutput(ArrayList<String> uriSegments, String data) {
 		// Set the content-type
 		this.contentType = HTTPMimeType.MIME_JSON;
 		this.httpStatus = HttpStatus.SC_BAD_REQUEST;
 
 		// Create the JSON object to represent the response
 		JSONObject body = new JSONObject();
 		body.put("status", STATUS_FAILED);
 
 		NodeManager nm = NodeManager.getInstance();
 		ClientManager cm = ClientManager.getInstance();
 
 		JSONObject jsonRequest = (JSONObject) JSONValue.parse(data);
		
 		String method = uriSegments.get(1);
		
 		if (method.equals("list")) {
 			// List the nodes
 
 			if (jsonRequest.containsKey("session")
 					&& cm.isValidClient((String) jsonRequest.get("session"))) {
 				body.put("nodes", nm.getJSONArray());
 				body.put("status", STATUS_OK);
 
 				this.httpStatus = HttpStatus.SC_OK;
 			}
		} else if(method.equals("activelist")){
			//List the active nodes
			
 			if (jsonRequest.containsKey("session")
 					&& cm.isValidClient((String) jsonRequest.get("session"))) {
 				body.put("nodes", nm.getActiveListJSONArray());
 				body.put("status", STATUS_OK);
 
 				this.httpStatus = HttpStatus.SC_OK;
 			}
		}else {
 
 			// Method not recognized
 			body.put("status", STATUS_BAD_METHOD);
 		}
 
 		return body.toString();
 	}
 }
