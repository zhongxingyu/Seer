 package com.voracious.dragons.common;
 
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.apache.commons.codec.binary.Base64;
 
 import com.voracious.dragons.client.screens.PlayScreen;
 
 public class Turn {
     //This should probably be somewhere else eventually, I'm just not sure where yet
     public static final String versionString = "0.01a";
     public static final int sessionLength = 32;
     public static final byte versionCode = 3;
     
     //Both of these are gotten from the server at auth time
     private int gameId;
     private String sessionId;
     private boolean isPlayer1;
     
     private Map<Byte, Short> unitsCreated;
     private Map<Byte, List<Vec2D.Short>> towersCreated;
     private Map<Byte, List<Vec2D.Short>> nodes;
     
     public Turn(int gameId, String sessionId, boolean isP1) {
     	this.sessionId = sessionId;
     	this.gameId = gameId;
     	this.isPlayer1=isP1;
     	
         unitsCreated = new HashMap<>();
         towersCreated = new TreeMap<>();
         nodes = new TreeMap<>();
     }
     
     public Turn(String turnData,boolean isP1){
         parseBytes(Base64.decodeBase64(turnData));
     	this.isPlayer1=isP1;
     }
     
     public Turn(byte[] turnData,boolean isP1) {
         parseBytes(turnData);
     	this.isPlayer1=isP1;
     }
     
 	private void parseBytes(byte[] turnData){
         if(Turn.versionCode == turnData[0]){
             ByteBuffer turn = ByteBuffer.wrap(turnData);
             turn.position(1);
             
             gameId = turn.getInt();
 
             byte[] session = new byte[sessionLength];
             turn.get(session);
             sessionId = Base64.encodeBase64String(session);
             
             byte numberOfUnits = turn.get();
             byte numberOfTowers = turn.get();
             byte numberOfNodes = turn.get();
 
             unitsCreated = new HashMap<>();
             for(int i = 0; i < numberOfUnits; i++){
                 byte unitId = turn.get();
                 short numSpawned = turn.getShort();
                 createUnit(unitId, numSpawned);
             }
             
             towersCreated = new HashMap<>();
             for(int i = 0; i < numberOfTowers; i++) {
                 byte towerId = turn.get();
                 short x = turn.getShort();
                 short y = turn.getShort();
                 createTower(towerId, new Vec2D.Short(x, y));
             }
             
             nodes = new TreeMap<>();
             for(int i = 0; i < numberOfNodes; i++) {
                 byte pathNum = turn.get();
                 short x = turn.getShort();
                 short y = turn.getShort();
                 addNode(pathNum, new Vec2D.Short(x, y));
             }
         }else{
             throw new IllegalArgumentException("Client is using a different version: " + Turn.versionCode);
         }
     }
     
     public void createUnit(byte unitId, short numUnits) {
         if(unitsCreated.containsKey(unitId)){
             unitsCreated.put(unitId, (short) (unitsCreated.get(unitId) + numUnits));
         }else{
             unitsCreated.put(unitId, numUnits);
         }
     }
     
     public void createTower(byte towerId, Vec2D.Short location) {
         if(towersCreated.containsKey(towerId)){
             towersCreated.get(towerId).add(location);
         }else{
             List<Vec2D.Short> temp = new LinkedList<Vec2D.Short>();
             temp.add(location);
             
             towersCreated.put(towerId, temp);
         }
     }
     
     synchronized
     public void addNode(byte pathId, Vec2D.Short location) {
         if(nodes.containsKey(pathId)){
            nodes.get(pathId).add(nodes.get(pathId).size()-1,location);
         }else{
             List<Vec2D.Short> temp = new LinkedList<Vec2D.Short>();
             //add to the player's castle as a starting point
            temp.add(!this.isPlayer1? PlayScreen.start_node
             			 :PlayScreen.end_node);
             //add the click's
             temp.add(location);
             
             //add the other players castle
            temp.add(this.isPlayer1? PlayScreen.start_node
        			 :PlayScreen.end_node);
             
             nodes.put(pathId, temp);
         }
     }
     
     public String toString() {
         return Base64.encodeBase64String(this.toBytes().array());
     }
     
     /*
      * byte versionCode
      * int  gameId
      * long sessId
      * 
      * byte numberOfUnits
      * byte numberOfTowers
      * byte numberOfPathNodes
      * 
      * {
      *   byte unitId
      *   short numSpawned
      * }*numberOfUnits
      * 
      * {
      *   byte towerId
      *   short x
      *   short y
      * }*numberOfTowers
      * 
      * Nodes must be in order, you could actually switch pathNums randomly but each sequential entry for the
      * same pathNum is assumed to be in the order of the nodes
      * {
      *   byte pathNum
      *   short x
      *   short y
      * }*numberOfPathNodes
      * 
      */
     
     public ByteBuffer toBytes() {
         int bufferSize = Byte.SIZE + Integer.SIZE + Long.SIZE; //size of header info (everything through numberOfPathNodes)
         bufferSize += unitsCreated.size() * (Byte.SIZE/8 + Short.SIZE/8); //Bytes from the unit data
 
         byte numberOfTowers = 0;
         { //Count the number of towers
             Iterator<List<Vec2D.Short>> it = towersCreated.values().iterator();
             
             while(it.hasNext()){
                 numberOfTowers += it.next().size();
             }
             
             bufferSize += numberOfTowers * (Byte.SIZE/8 + 2*(Short.SIZE/8)); //Bytes from the tower data
         }
         
         byte numberOfNodes = 0;
         { //Count the number of nodes
             Iterator<List<Vec2D.Short>> it = nodes.values().iterator();
             
             while(it.hasNext()){
                 numberOfNodes += it.next().size();
             }
             
             bufferSize += numberOfNodes * (2*(Byte.SIZE/8) + 2*(Short.SIZE/8)); //Bytes form the node data
         }
         
         ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
         buffer.limit(buffer.capacity());
         
         //put the version code
         buffer.put(versionCode);
         
         //put the game id
         buffer.putInt(gameId);
         
         //put the session id
         buffer.put(Base64.decodeBase64(sessionId));
         
         //Put the array sizes
         buffer.put((byte) unitsCreated.size());
         buffer.put(numberOfTowers);
         buffer.put(numberOfNodes);
         
         { //put the units data
             Set<Map.Entry<Byte, Short>> entrySet = unitsCreated.entrySet();
             Iterator<Map.Entry<Byte, Short>> it = entrySet.iterator();
             while(it.hasNext()){
                 Map.Entry<Byte, Short> entry = (Map.Entry<Byte, Short>) it.next();
                 
                 buffer.put(entry.getKey().byteValue());
                 
                 buffer.putShort(entry.getValue().shortValue());
             }
         }
         
         { //put the towers data
             Set<Map.Entry<Byte, List<Vec2D.Short>>> entrySet= towersCreated.entrySet();
             Iterator<Map.Entry<Byte, List<Vec2D.Short>>> it= entrySet.iterator();
             while(it.hasNext()){
             	Map.Entry<Byte, List<Vec2D.Short>> entry = it.next();
             	
             	Iterator<Vec2D.Short> posIt = entry.getValue().iterator();
             	while(posIt.hasNext()){
             		Vec2D.Short pos = posIt.next();
             		
 	            	buffer.put(entry.getKey().byteValue());
 	            	
 	            	buffer.putShort(pos.getx());
 	            	buffer.putShort(pos.gety());
             	}
             }
         }
         
         { //put the nodes data
             Set<Map.Entry<Byte, List<Vec2D.Short>>> entrySet= nodes.entrySet();
             Iterator<Map.Entry<Byte, List<Vec2D.Short>>> it= entrySet.iterator();
             while(it.hasNext()){
             	Map.Entry<Byte, List<Vec2D.Short>> entry = it.next();
             	
             	Iterator<Vec2D.Short> posIt = entry.getValue().iterator();
             	while(posIt.hasNext()){
             		Vec2D.Short pos = posIt.next();
 	            	buffer.put(entry.getKey().byteValue());
 	            	
 	            	buffer.putShort(pos.getx());
 	            	buffer.putShort(pos.gety());
             	}
             }
         }
         
         return buffer;
     }
     
     synchronized
     public List<List<Vec2D.Short>> getPaths(){
     	List<List<Vec2D.Short>> ret = new ArrayList<List<Vec2D.Short>>(nodes.size());
     	
     	Iterator<Map.Entry<Byte, List<Vec2D.Short>>> it = nodes.entrySet().iterator();
     	while(it.hasNext()){
     		Map.Entry<Byte, List<Vec2D.Short>> tmp=it.next();
     		Byte loc = tmp.getKey();
     		List<Vec2D.Short> val = new LinkedList<Vec2D.Short>(tmp.getValue());
     		ret.add(loc, val);
     	}
     	return ret;
     }
     
     synchronized
     public List<List<Vec2D.Short>> getTowers(){
     	List<List<Vec2D.Short>> ret= new ArrayList<List<Vec2D.Short>>(towersCreated.size());
     	
     	Iterator<Map.Entry<Byte, List<Vec2D.Short>>> it = towersCreated.entrySet().iterator();
     	while(it.hasNext()){
     		Map.Entry<Byte, List<Vec2D.Short>> tmp=it.next();
     		Byte loc =tmp.getKey();
     		List<Vec2D.Short> val=new LinkedList<Vec2D.Short>(tmp.getValue());
     		ret.add(loc, val);
     	}
     	return ret;
     }
     
     synchronized
     public List<Short> getUnits(){
     	List<Short> ret= new LinkedList<Short>(unitsCreated.values());
     	
     	return ret;    	
     }
 
 	public int getGameId() {
 		return gameId;
 	}
 
 	public void setGameId(int gameId) {
 		this.gameId = gameId;
 	}
 
 	public String getSessionId() {
 		return sessionId;
 	}
 
 	public void setSessionId(String sessionId) {
 		this.sessionId = sessionId;
 	}
 }
