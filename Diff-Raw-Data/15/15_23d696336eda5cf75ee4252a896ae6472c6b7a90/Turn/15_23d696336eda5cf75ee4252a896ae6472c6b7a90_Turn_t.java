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
 
 public class Turn {
     //This should probably be somewhere else eventually, I'm just not sure where yet
     public static final String versionString = "0.01a";
     public static final byte versionCode = 2;
     
     //Both of these are gotten from the server at auth time
     private int gameId;
     private long sessionId;
     
     private Map<Byte, Short> unitsCreated;
     private Map<Byte, List<Vec2D.Short>> towersCreated;
     private Map<Byte, List<Vec2D.Short>> nodes;
     
     public Turn(int gameId, long sessionId){
     	this.sessionId = sessionId;
     	this.gameId = gameId;
     	
         unitsCreated = new HashMap<Byte, Short>();
         towersCreated = new TreeMap<Byte, List<Vec2D.Short>>();
         nodes = new TreeMap<Byte, List<Vec2D.Short>>();
     }
     
     //TODO: public Turn(byte[] turnData), parses the byte array and fills the data members with the correct data
     
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
             nodes.get(pathId).add(location);
         }else{
             List<Vec2D.Short> temp = new LinkedList<Vec2D.Short>();
             temp.add(location);
             
             nodes.put(pathId, temp);
         }
     }
     
     public String toString() {
         String result = "";
         byte[] dataBytes = this.toBytes().array();
         
         for(int i=0; i<dataBytes.length; i++){
             result += Byte.toString(dataBytes[i]) + " ";
         }
         
         return result;
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
      * {
      *   byte pathNum
      *   byte nodeNum
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
         buffer.putLong(sessionId);
         
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
             	byte nodeNum = 0;
             	while(posIt.hasNext()){
             		Vec2D.Short pos = posIt.next();
 	            	buffer.put(entry.getKey().byteValue());
 	            	buffer.put(nodeNum);
 	            	nodeNum++;
 	            	
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
 }
