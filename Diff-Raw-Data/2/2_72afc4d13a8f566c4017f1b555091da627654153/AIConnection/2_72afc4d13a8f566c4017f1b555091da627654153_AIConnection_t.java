 import java.io.*;
 import java.net.*;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.atomic.AtomicBoolean;
 import org.json.*;
 
 public class AIConnection {
     private Socket socket;
     private BufferedReader inputReader;
     private ConcurrentLinkedQueue<JSONObject> messages;
     private boolean gotHandshake = false;
     public AtomicBoolean gotLoadout = new AtomicBoolean(false);
     public String primaryWeapon = "";
     public String secondaryWeapon = "";
     public int primaryWeaponLevel = 1;
     public int secondaryWeaponLevel = 1;
     public String username;
     public Tile position = null;
     public Tile spawnTile = null;
     public boolean isAlive = true;
     public int health = 100;
     public int score = 0;
     public int rubidiumResources = 0;
     public int explosiumResources = 0;
     public int scrapResources = 0;
 
     
     public AIConnection(Socket clientSocket){
 	messages = new ConcurrentLinkedQueue<JSONObject>();
 	socket = clientSocket;
 	try {
 	    inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 	}
 	catch (IOException e){
 	    Debug.error("error creating connection handler: " + e);
 	}
     }
     public String readLine() throws IOException {
 	return inputReader.readLine();
     }
     public String getIp(){
 	return socket.getInetAddress() + ":" + Integer.toString(socket.getPort());
     }
 
     void sendError(String errorString){
 	try {
 	    JSONObject errorMessage = new JSONObject().put("error", errorString);
 	    sendMessage(errorMessage);
 	} catch (JSONException f){}
 	catch (IOException g){}
     }
     public synchronized void input(JSONObject o) throws ProtocolException, IOException {
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
 	else if(!gotLoadout.get()){
 	    parseLoadout(o);
 	}
 	else if(o.has("message")){
 	    try {
 		if(o.get("message").equals("action")){
 		    messages.add(o);
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
 
     private void parseLoadout(JSONObject o) throws ProtocolException {
 	try {
 	    if(!(o.get("message").equals("loadout"))){
 		throw new ProtocolException("Expected 'loadout', but got '" + o.get("message") + "' key");
 	    }
 	    if(!Util.validateWeapon(o.getString("primary-weapon"))){
 		throw new ProtocolException("Invalid primary weapon: '"
 					    + o.getString("primary-weapon") + "'");
 	    }
 	    if(!Util.validateWeapon(o.getString("secondary-weapon"))){
 		throw new ProtocolException("Invalid secondary weapon: '"
 					    + o.getString("secondary-weapon") + "'");
 	    }
 	    if(o.getString("primary-weapon").equals(o.getString("secondary-weapon"))){
 		throw new ProtocolException("Invalid loadout: Can't have the same weapon twice.");
 	    }
 	    primaryWeapon = o.getString("primary-weapon");
 	    secondaryWeapon = o.getString("secondary-weapon");
 	    Debug.info(username + " selected loadout: " + primaryWeapon + " and "
 			       + secondaryWeapon + ".");
 	    gotLoadout.set(true);
 	}
 	catch (JSONException e){
 	    throw new ProtocolException("Invalid or incomplete packet: " + e.getMessage());	    
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
 	    Util.validateUsername(o.getString("name"));
 	    username = o.getString("name");
 	    gotHandshake = true;
 	    return true;
 	}
 	catch (JSONException e){
 	    throw new ProtocolException("Invalid or incomplete packet: " + e.getMessage());
 	}
     }
     public void sendGamestate(int turnNumber, int dimension, String mapData[][],
 			      AIConnection playerlist[]){
 	JSONObject root = new JSONObject();
 	try {
 	    root.put("message", "gamestate");
 	    root.put("turn", turnNumber);
 	    JSONArray players = new JSONArray();
 	    
 	    for(AIConnection ai: playerlist){
 		JSONObject playerObject = new JSONObject();
 		playerObject.put("name", ai.username);
 		if(turnNumber != 0){
 		    playerObject.put("health", ai.health);
 		    playerObject.put("score", ai.score);
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
 	}
 	catch (IOException e) {
 	    Debug.error("Error writing to '" + username + "': " + e.getMessage());
 	}
     }
     public void sendDeadline(){
 	JSONObject o = new JSONObject();
 	try {
 	    o.put("message", "endturn");
 	    sendMessage(o);
 	} catch (JSONException e){}
 	catch (IOException e){
 	    Debug.error("Error writing to '" + username + "': " + e.getMessage());
 	}
     }
     public void sendMessage(JSONObject o) throws IOException{
 	if(!isAlive){
 	    Debug.debug("player '" + this.username + "' disconnected, not sending...");
 	    return;
 	}
 	socket.getOutputStream().write((o.toString() + "\n").getBytes());
     }
     public JSONObject getNextMessage(){
 	return messages.poll();
     }
     public synchronized void setSpawnpoint(Tile spawnpoint){
 	Debug.info("Player '" + username
 			   + "' spawns at " + spawnpoint.coords.getString());
 	position = spawnpoint;
 	spawnTile = spawnpoint;
 	position.playerOnTile = this;
     }
     public void clearAllMessages(){
 	if(messages.size() > 0){
 	    Debug.warn("Message inbox of " + username + " contained " + messages.size() + " extra messages, discarding...");
 	}
 	messages.clear();
     }
     public synchronized boolean doMove(JSONObject o){
 	// TODO verify that this is all exactly right
 	try {
 	    String direction = o.getString("direction");
 	    if(direction.equals("up")){
 		if(position.up != null && position.up.isAccessible()){
 		    if(position.up.playerOnTile != null)
 			throw new ProtocolException("Player " + position.up.playerOnTile.username
 						    + " is already on this tile.");
 		    position.playerOnTile = null;
 		    position = position.up;
 		    position.playerOnTile = this;
 		    return true;
 		}
 		else {
 		    throw Util.throwInaccessibleTileException("up", position.up);
 		}
 	    }
 	    else if(direction.equals("down")){
 		if(position.down != null && position.down.isAccessible()){
 		    if(position.down.playerOnTile != null)
 			throw new ProtocolException("Player " + position.down.playerOnTile.username
 						    + " is already on this tile.");
 		    position.playerOnTile = null;
 		    position = position.down;
 		    position.playerOnTile = this;
 		    return true;
 		}
 		else {
 		    throw Util.throwInaccessibleTileException("down", position.down);
 		}
 	    }
 	    else if(direction.equals("left-down")){
 		if(position.leftDown != null && position.leftDown.isAccessible()){
 		    if(position.leftDown.playerOnTile != null)
 			throw new ProtocolException("Player " + position.leftDown.playerOnTile.username
 						    + " is already on this tile.");
 		    position.playerOnTile = null;
 		    position = position.leftDown;
 		    position.playerOnTile = this;
 		    return true;
 		}
 		else {
 		    throw Util.throwInaccessibleTileException("left-down", position.leftDown);
 		}
 	    }
 	    else if(direction.equals("left-up")){
 		if(position.leftUp != null && position.leftUp.isAccessible()){
 		    if(position.leftUp.playerOnTile != null)
 			throw new ProtocolException("Player " + position.leftUp.playerOnTile.username
 						    + " is already on this tile.");
 		    position.playerOnTile = null;
 		    position = position.leftUp;
 		    position.playerOnTile = this;
 		    return true;
 		}
 		else {
 		    throw Util.throwInaccessibleTileException("left-up", position.leftUp);
 		}
 	    }
 	    else if(direction.equals("right-down")) {
 		if(position.rightDown != null && position.rightDown.isAccessible()){
 		    if(position.rightDown.playerOnTile != null)
 			throw new ProtocolException("Player " + position.rightDown.playerOnTile.username
 						    + " is already on this tile.");
 		    position.playerOnTile = null;
 		    position = position.rightDown;
 		    position.playerOnTile = this;
 		    return true;
 		}
 		else {
 		    throw Util.throwInaccessibleTileException("right-down", position.rightDown);
 		}
 	    }
 	    else if(direction.equals("right-up")){
 		if(position.rightUp != null && position.rightUp.isAccessible()){
 		    if(position.rightUp.playerOnTile != null)
 			throw new ProtocolException("Player " + position.rightUp.playerOnTile.username
 						    + " is already on this tile.");
 		    position.playerOnTile = null;
 		    position = position.rightUp;
 		    position.playerOnTile = this;
 		    return true;
 		}
 		else {
 		    throw Util.throwInaccessibleTileException("right-up", position.rightUp);
 		}
 	    }
 	    else {
 		throw new ProtocolException("Invalid direction: '" + direction + "'");
 	    }
 	}
 	catch (JSONException e){
 	    try {
 		JSONObject errorMessage
 		    = new JSONObject().put("error", "Invalid move packet: " + e.getMessage());
 		    sendMessage(errorMessage);
 	    } catch (JSONException f){}
 	    catch (IOException g){}
 	    return false;
 	}
 	catch (ProtocolException e){
 	    try {
 		JSONObject errorMessage
 		    = new JSONObject().put("error", "Invalid move packet: " + e.getMessage());
 		sendMessage(errorMessage);
 	    } catch (JSONException f){}
 	    catch (IOException g){}
 	    return false;
 	}
     }
     void invalidAction(JSONObject action){
 	try {
 	    JSONObject errorMessage
 		= new JSONObject().put("error", "Invalid action: " + action.get("type"));
 	    sendMessage(errorMessage);
 	} catch (JSONException f){}
 	catch (IOException g){}
     }
     boolean shootLaser(JSONObject action, GraphicsConnection graphicsConnection, int turnsLeft){
 	int laserLevel = 1;
 	if(primaryWeapon.equals("laser")){
 	    laserLevel = primaryWeaponLevel;
 	}
 	else if(secondaryWeapon.equals("laser")){
 	    laserLevel = secondaryWeaponLevel;
 	}
 	else {
 	    return false;
 	}
 	try {
 	    String direction = action.getString("direction");
 	    Laser laser = new Laser(this, turnsLeft);
 	    if(laser.setDirection(direction)){
 		laser.setPosition(position);
 		Coordinate endvector = laser.performShot(laserLevel);
 		graphicsConnection.setStartStopHack(position.coords, endvector);
 	    }
 	    else {
 		sendError("Invalid shot: unknown direction '"  + direction + "'");
 		return false;
 	    }
 	    return true;
 	}
 	catch (JSONException e){
 	    sendError("Invalid shot: lacks a direction key");
 	}
 	return false;
     }
 
     boolean shootDroid(JSONObject action, int turnsLeft){
 	int droidLevel = 1; // TODO
 	if(primaryWeapon.equals("droid")){
 	    droidLevel = primaryWeaponLevel;
 	}
 	else if(secondaryWeapon.equals("droid")){
 	    droidLevel = secondaryWeaponLevel;
 	}
 	else {
 	    Debug.warn("User '" + username
 			       + "' attempted to shoot the droid, but doesn't have it");
 	    return false;
 	}
 	int range = 3;
 	if(droidLevel == 2) {range = 4;}
 	if(droidLevel == 3) {range = 5;} // replicated here for more friendly error messages
 	try {
 	    JSONArray directionSequence = action.getJSONArray("sequence");
 	    Droid droid = new Droid(this, turnsLeft);
 	    if(directionSequence.length() > range){
 		Debug.warn("Got " + directionSequence.length() + " commands for the droid, but your droids level ("
 			   + droidLevel + ") only supports " + range + " steps.");
 		sendError("Got " + directionSequence.length() + " commands for the droid, but your droids level ("
 			  + droidLevel + ") only supports " + range + " steps.");
 	    }
 	    if(droid.setDirections(directionSequence, droidLevel)){
 		droid.setPosition(position);
 		int stepsTaken = droid.performShot();
 		JSONArray truncatedArray = new JSONArray();
 		for(int i = 0; i < stepsTaken; i++){
 		    truncatedArray.put(directionSequence.get(i));
 		}
 		action.put("sequence", truncatedArray);
 		Debug.debug("droid steps taken: " + stepsTaken);
 	    }
 	    else {
 		sendError("Invalid shot: unknown direction in droid sequence");
 		return false;
 	    }
 	    return true;
 	}
 	catch (JSONException e){
 	    sendError("Invalid shot: lacks a direction key");
 	}
 	return false;
     }
 
     boolean shootMortar(JSONObject action, int turnsLeft){
 	int mortarLevel = 1;
 	if(primaryWeapon.equals("mortar")){
 	    mortarLevel = primaryWeaponLevel;
 	}
 	else if(secondaryWeapon.equals("mortar")){
 	    mortarLevel = secondaryWeaponLevel;
 	}
 	else {
 	    Debug.warn("User '" + username
 			       + "' attempted to shoot the mortar, but doesn't have it");
 	    return false;
 	}
 	try {
 	    Coordinate relativeTargetCoordinates = new Coordinate(action.getString("coordinates"));
 	    Mortar mortar = new Mortar(this, turnsLeft);
 	    mortar.setPosition(this.position);
 	    mortar.setTarget(relativeTargetCoordinates, mortarLevel);
 	    return mortar.performShot();
 	}
 	catch (JSONException e){
 	    sendError("Invalid shot: lacks 'coordinates' key");
 	    return false;
 	}
     }
 
     boolean mineResource(){
 	TileType tileType = this.position.tileType;
 	Debug.game("Player " + username + " mining " + tileType);
 	boolean minedResource = this.position.mineTile();
 	if(minedResource){
 	    if(tileType == TileType.RUBIDIUM)
 		rubidiumResources++;
 	    if(tileType == TileType.EXPLOSIUM)
 		explosiumResources++;
 	    if(tileType == TileType.SCRAP)
 		scrapResources++;
 	    Debug.debug("Resources of player " + username + " are now: Rubidium: "
 			       + rubidiumResources + ", Explosium: " + explosiumResources + ", Scrap: " + scrapResources);
 	    return true;
 	}
 	return false;
     }
     
     void damagePlayer(int hitpoints, AIConnection dealingPlayer){
	if(health <= 0){
 	    Debug.warn("Player is already dead.");
 	    return;
 	}
 	Debug.stub("'" + this.username + "' received " + hitpoints
 			   + " damage from '" + dealingPlayer.username  + "'!");
 	health -= hitpoints;
 	if(!(dealingPlayer.username.equals(this.username))){
 	    dealingPlayer.givePoints(hitpoints); // damaged user other than self, award points
 	}
 	if(health <= 0){
 	    Debug.game(this.username + " got killed by " + dealingPlayer.username);
 	    Debug.guiMessage(this.username + " got killed by " + dealingPlayer.username);
 	    if(!(dealingPlayer.username.equals(this.username))){
 		dealingPlayer.givePoints(20); // 20 bonus points for killing someone
 	    }
 	    score -= 40;
 	    health = 0;
 	}
     }
     boolean upgradeWeapon(String weapon){
 	Debug.debug(username + " upgrading his " + weapon);
 	if(primaryWeapon.equals(weapon)){
 	    Debug.stub("upgrading primary weapon (" + weapon + ")");
 	    boolean success = subtractResourcesForWeaponUpgrade(weapon, primaryWeaponLevel);
 	    if(success){
 		primaryWeaponLevel++;
 		Debug.guiMessage(username + " upgrades his " + weapon);
 		return true;
 	    }
 	    else {
 		return false;
 	    }
 	}
 	else if(secondaryWeapon.equals(weapon)){
 	    boolean success = subtractResourcesForWeaponUpgrade(weapon, secondaryWeaponLevel);
 	    if(success){
 		secondaryWeaponLevel++;
 		Debug.guiMessage(username + " upgrades his " + weapon);
 		return true;
 	    }
 	    else {
 		return false;
 	    }
 	}
 	else {
 	    Debug.warn(username + " tried to upgrade weapon '" + weapon + "', but doesn't have it.");
 	    return false;
 	}
     }
 
     boolean subtractResourcesForWeaponUpgrade(String weapon, int currentLevel){
 	int resourcesToSubtract = 4;
 	if(currentLevel == 2)
 	    resourcesToSubtract = 5;
 	if(currentLevel == 3){
 	    Debug.warn(username + " tried to upgrade his " + weapon + ", but it is already level 3.");
 	    return false;	    
 	}
 	if(weapon.equals("laser")){
 	    if(rubidiumResources >= resourcesToSubtract){
 		rubidiumResources -= resourcesToSubtract;
 		return true;
 	    }
 	    else {
 		Debug.warn("Tried to upgrade the laser, but not enough rubidium");
 		return false;
 	    }
 	}
 	if(weapon.equals("mortar")){
 	    if(explosiumResources >= resourcesToSubtract){
 		explosiumResources -= resourcesToSubtract;
 		return true;
 	    }
 	    else {
 		Debug.warn("Tried to upgrade the mortar, but not enough explosium");
 		return false;
 	    }
 	}
 	if(weapon.equals("droid")){
 	    if(scrapResources >= resourcesToSubtract){
 		scrapResources -= resourcesToSubtract;
 		return true;
 	    }
 	    else {
 		Debug.warn("Tried to upgrade the droid, but not enough scrap");
 		return false;
 	    }
 	}
 	return false;
     }
     
     void respawn(){
 	position.playerOnTile = null;
 	position = spawnTile;
 	position.playerOnTile = this;
 	health = 100;
     }
     void givePoints(int points){
 	Debug.info("got awarded " + points + " points");
 	score += points;
     }
     void givePenality(int points){
 	Debug.warn(username + " got " + points + " penality");
 	score -= points;
     }
     void printStats(){
 	System.out.println(username + ": HP: " + health + ", score: " + score
 			   + ", RUB:" + rubidiumResources + ", EXP:" + explosiumResources
 			   + ", SCR:" + scrapResources + ", prim. lvl:" + primaryWeaponLevel +
 			   ", sec. lvl.:" + secondaryWeaponLevel);
     }
 }
 
