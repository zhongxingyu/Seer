 import java.io.*;
 import java.net.*;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.atomic.AtomicBoolean;
 import org.json.*;
 
 public class GraphicsConnection {
     private Socket socket;
     private BufferedReader inputReader;
     private ConcurrentLinkedQueue<JSONObject> messages;
     private boolean gotHandshake = false;
     String password = "supersecretpassword";
     public boolean isAlive = true;
     public GraphicsContainer container = null;
     public boolean isDoneProcessing = true;
     
     public GraphicsConnection(Socket clientSocket, GraphicsContainer containerArg){
 	messages = new ConcurrentLinkedQueue<JSONObject>();
 	container = containerArg;
 	socket = clientSocket;
 	try {
 	    inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 	}
 	catch (IOException e){
 	    System.out.println("[GRAPHCON] error creating connection handler: " + e);
 	}
     }
     public String readLine() throws IOException {
 	return inputReader.readLine();
     }
     public String getIp(){
 	return socket.getInetAddress() + ":" + Integer.toString(socket.getPort());
     }
     public synchronized void write(String string){ // only one thread may write at the same time
 	System.out.println("[GRAPHCON] writing to socket: " + string);
     }
     public synchronized void input(JSONObject o) throws ProtocolException, IOException {
 	System.out.println("[GRAPHCON] Got input");
 	if(!gotHandshake){
 	    if(parseHandshake(o)){
 		try {
 		    JSONObject successMessage = new JSONObject()
 			.put("message", "connect").put("status", true);
 		    sendMessage(successMessage);
 		}
 		catch (JSONException e) {}
 	    }
 	    return;
 	}
 	else if(o.has("message")){
 	    try {
 		if(o.get("message").equals("ready")){
 		    isDoneProcessing = true;
 		}
 		else {
 		    throw new ProtocolException("Unexpected message, got '" + o.get("message")
 						+ "' but expected 'action'");
 		}
 	    }
 	    catch (JSONException e){
 		throw new ProtocolException("Invalid or incomplete packet: " + e.getMessage());
 	    }
 	}
 	else {
 	    try {
 		throw new ProtocolException("Unexpected packet: '" + o.get("message") + "'");
 	    }
 	    catch (JSONException e) {
 		throw new ProtocolException("Invalid or incomplete packet");
 	    }
 	}
     }
 
     private boolean parseHandshake(JSONObject o) throws ProtocolException {
 	try {
 	    if(!(o.get("message").equals("connect"))){
 		throw new ProtocolException("Expected 'connect' handshake, but got '"
 					    + o.get("message") + "' key");
 	    }
 	    if(!(o.getInt("revision") == 1)){
 		throw new ProtocolException("Wrong protocol revision: supporting 1, but got " +
 					    o.getInt("revision"));
 	    }
 	    if(!o.getString("password").equals(password)){
 		System.out.println("Wrong password");
 		throw new ProtocolException("Wrong password!");
 	    }
 	    System.out.println("Correct password");
 	    gotHandshake = true;
 	    container.set(this);
 	    return true;
 	}
 	catch (JSONException e){
 	    throw new ProtocolException("Invalid or incomplete packet: " + e.getMessage());
 	}
     }
     public synchronized void sendGamestate(int turnNumber, int dimension, String mapData[][],
 			      AIConnection playerlist[]){
 	// TODO: correct turn number
 	System.out.println("Sending gamestate to graphics engine");
 	JSONObject root = new JSONObject();
 	try {
 	    root.put("message", "gamestate");
 	    root.put("turn", turnNumber);
 	    JSONArray players = new JSONArray();
 	    for(AIConnection ai: playerlist){
 		JSONObject playerObject = new JSONObject();
 		playerObject.put("name", ai.username);
		if(turnNumber != 0){
 		    playerObject.put("health", 100);
 		    playerObject.put("score", 0);
 		    playerObject.put("position", ai.position.coords.getCompactString());
 		    JSONObject primaryWeaponObject = new JSONObject();
 		    primaryWeaponObject.put("name", ai.primaryWeapon);
 		    primaryWeaponObject.put("level", ai.primaryWeaponLevel);
 		    playerObject.put("primary-weapon", primaryWeaponObject);
 		
 		    JSONObject secondaryWeaponObject = new JSONObject();
 		    secondaryWeaponObject.put("name", ai.secondaryWeapon);
 		    secondaryWeaponObject.put("level", ai.secondaryWeaponLevel);
 		    playerObject.put("secondary-weapon", secondaryWeaponObject);
 		}
 
 		players.put(playerObject);
 	    }
 	    root.put("players", players);	    
 
 	    JSONObject map = new JSONObject();
 	    map.put("j-length", dimension);
 	    map.put("k-length", dimension);
 	    map.put("data", new JSONArray(mapData));
 	    root.put("map", map);
 	}
 	catch (JSONException e){}
 	try {
 	    sendMessage(root);
 	    isDoneProcessing = false;
 	}
 	catch (IOException e) {
 	    System.out.println("Error writing to graphics: " + e.getMessage());
 	}
     }
     public void sendDeadline(){
 	JSONObject o = new JSONObject();
 	try {
 	    o.put("message", "endturn");
 	    sendMessage(o);
 	} catch (JSONException e){}
 	catch (IOException e){
 	    System.out.println("Error writing to graphics: " + e.getMessage());
 	}
     }
     public void sendEndActions(){
 	JSONObject o = new JSONObject();
 	try {
 	    o.put("message", "endactions");
 	    sendMessage(o);
 	} catch (JSONException e){}
 	catch (IOException e){
 	    System.out.println("Error writing to graphics: " + e.getMessage());
 	}
     }
     public void sendMessage(JSONObject o) throws IOException{
 	socket.getOutputStream().write((o.toString() + "\n").getBytes());
     }
     public JSONObject getNextMessage(){
 	return messages.poll();
     }
     public void waitForGraphics(){
 	System.out.println("Waiting for graphics...");
 	while(true){
 	    try {Thread.sleep(10);} catch (InterruptedException e){}
 	    if(isDoneProcessing){
 		break;
 	    }
 	}
     }
 }
