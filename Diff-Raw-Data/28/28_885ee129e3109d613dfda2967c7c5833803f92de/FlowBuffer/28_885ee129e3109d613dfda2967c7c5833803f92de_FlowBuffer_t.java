 package guiFX;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import topo.ConnectionList;
 
 //private Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();
 public class FlowBuffer {
 	private static Map<Integer,List<String>> synchronizedMap = Collections.synchronizedMap(new HashMap<Integer, List<String>>());
 	static ConnectionList connections;
 	
 	public FlowBuffer(ConnectionList _connections){
 		this.connections = _connections;
 		}
 	
 	public static void addToBuffer(int ID, List<String> value){
 		synchronized(synchronizedMap){
 			if(synchronizedMap.containsKey(ID)){
 				//Do we care?
 			}else if((value.get(0).equals("Router1")) && (value.get(1).equals("GigabitEthernet0/0")) ){
				//System.out.println("packet: " + ID + " is leaving the network from " + value.get(0) + "[" + value.get(1) + "]");
 			}else{
 			//System.out.println("Added " + ID + " from " + value.get(0) + "[" + value.get(1) + "]");
 			synchronizedMap.put(ID, value);
 			connections.addPacketOut(value.get(0), value.get(1));
 			}
 		}
 		return;
 	}
 	public static void removeFrombuffer(int ID, List<String> value){
 		synchronized(synchronizedMap){
 			if(synchronizedMap.containsKey(ID)){
 				//System.out.println("Removed " + ID + " from " + name);
 				if((value.get(0).equals("Router1")) && (value.get(1).equals("GigabitEthernet0/0")) ){
 
 				}else{
 					synchronizedMap.remove(ID);
 					connections.addPacketIn(value.get(0), value.get(1));
 				}
 			}
 			return;
 		}
 	}
 	public static int getSize(){
 		return synchronizedMap.size();
 	}
 	public static void printBuffer(){
 		System.out.println(synchronizedMap.toString());
 		return;	
 	}
 	public static void getItem(){
 		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
 		System.out.println("~~~~~~~~~~~~Current Buffer Contents~~~~~~~~~~~~");
 		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
 		Iterator it = synchronizedMap.entrySet().iterator();
 		while(it.hasNext()){
 			Map.Entry pairs = (Map.Entry)it.next();
 			System.out.println(pairs.getKey() + " = " + pairs.getValue());
 			//it.remove();
 			}
 	}
 	
 	public static ArrayList<String> checkLoss(){
 		ArrayList<String> contents = new ArrayList<String>();
 		Iterator it = synchronizedMap.entrySet().iterator();
 		while(it.hasNext()){
 			Map.Entry pairs = (Map.Entry)it.next();
 			contents.add(pairs.getValue().toString());
 			//System.out.println(pairs.getKey() + " = " + pairs.getValue());
 			//it.remove();
 			}
 		
 		return contents;
 		
 	}
 }
