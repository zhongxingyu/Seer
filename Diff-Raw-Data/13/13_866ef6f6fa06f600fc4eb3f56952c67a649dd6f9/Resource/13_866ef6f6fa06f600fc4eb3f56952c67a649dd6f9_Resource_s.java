 import java.util.*;
 
 public class Resource {	
 	private static ArrayList<Resource> resources = new ArrayList<Resource>();
 	private static int count = 0;
 	private String portID;
 	private String resourceName;
 	private Process currentProcess;
 	private boolean locked;
 	
 	
 	Resource(String name){
 		this.resourceName = name;
 		locked = false;
 		resources.add(this);
 		count ++;
 		portID = "R-" + count;
 	}
 	
 	static ArrayList<Resource> getAllResources(){
 		return resources;
 	}
 	
 	static Resource findResource(String resourcePort){
 		int index = Integer.parseInt((resourcePort.split("-"))[1]);
 		if(index <= count){
 			return resources.get(index - 1);
 		}
 		else{
 			System.out.println("Error: There is no device with this port");
 			return null;
 		}
 	}
 	
 	static String getTableInfo(){
 		String isLocked = "", processName = "", resourceName = "", portID = "", 
 		returnString = "|------Resource Name------|---Port ID---|-----Current Process-----|--Locked--|";
 		for(int x=0; x < count; x++){
 			Resource r = resources.get(x);
 			// format locked string
 			if(r.isAvailable()){
 				isLocked = "Available.";
 			}
 			else{
 				isLocked = "Locked....";
 			}
 			// format Device name string
 			resourceName = r.resourceName;
 			for(int i = resourceName.length(); i < 25; i++){
 				resourceName += ".";
 			}
 			
 			// format port ID string
 			portID = r.portID;
 			for(int i = portID.length(); i < 13; i++){
 				portID += ".";
 			}
 		
 			// format process name string
 			if(r.currentProcess != null){
 				processName = r.currentProcess.getName();
 				for(int i = processName.length(); i < 25; i++){
 					processName += ".";
 				}
 			}
 			else{
 				processName = ".........................";
 			}
 			
 			
 			returnString += "\n|" + resourceName + "|" + portID + "|" + processName + "|" + isLocked + "|";
 		}
 		return returnString;
 	}
 	
 	void setLock(Process newProcess){
 		if(!locked){
 			locked = true;
 			this.currentProcess = newProcess;
 		}
 		else{
 			System.out.println("Error: Locked");
 		}
 	}
 	
 	void releaseLock(){
 		locked = false;
 		this.currentProcess = (Process)null;
 	}
 	
 	boolean isAvailable(){
 		return !locked;
 	}
 	
 	String getPortID(){
 		return this.portID;
 	}
 	
 	public static void main(String args[]){
 		Vector<Integer> x = new Vector<Integer>(1);
		Process p1 = new Process(1, "The process", 2, 3, false, x);
		Process p2 = new Process(2, "The Thing", 3, 3, false, x);
 		Resource r = new Resource("Hi there I'mma device");
 		r.setLock(p1);
 		r = new Resource("Hi there I'mma device2");
 		r = new Resource("Hi there I'mma device3");
 		r.setLock(p2);
 		r = new Resource("Hi there I'mma device4");
 		System.out.println(Resource.getTableInfo());
 	}
 
 }
