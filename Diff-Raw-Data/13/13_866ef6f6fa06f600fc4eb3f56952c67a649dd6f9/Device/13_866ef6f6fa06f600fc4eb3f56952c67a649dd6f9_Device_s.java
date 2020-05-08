 import java.util.*;
 
 public class Device {
 	protected static ArrayList<Device> devices = new ArrayList<Device>();
 	protected static int count = 0;
 	protected String portID;
 	protected String deviceName;
 	protected Process currentProcess;
 	protected boolean locked;
 	
 	Device(String name){
 		this.deviceName = name;
 		locked = false;
 		devices.add(this);
 		count ++;
 		portID = "D-" + count;
 	}
 	
 	static ArrayList<Device> getAllDevices(){
 		return devices;
 	}
 	
 	static String getTableInfo(){
 		String isLocked = "", processName = "", deviceName = "", portID = "", 
 		returnString = "|-------Device Name-------|---Port ID---|-----Current Process-----|--Locked--|";
 		for(int x=0; x < count; x++){
 			Device d = devices.get(x);
 			// format locked string
 			if(d.isAvailable()){
 				isLocked = "Available.";
 			}
 			else{
 				isLocked = "Locked....";
 			}
 			// format Device name string
 			deviceName = d.deviceName;
 			for(int i = deviceName.length(); i < 25; i++){
 				deviceName += ".";
 			}
 			
 			// format port ID string
 			portID = d.portID;
 			for(int i = portID.length(); i < 13; i++){
 				portID += ".";
 			}
 		
 			// format process name string
 			if(d.currentProcess != null){
 				processName = d.currentProcess.getName();
 				for(int i = processName.length(); i < 25; i++){
 					processName += ".";
 				}
 			}
 			else{
 				processName = ".........................";
 			}
 			
 			
 			returnString += "\n|" + deviceName + "|" + portID + "|" + processName + "|" + isLocked + "|";
 		}
 		return returnString;
 	}
 	
 	static Device findDevice(String devicePort){
 		int index = Integer.parseInt((devicePort.split("-"))[1]);
 		if(index <= count){
 			return devices.get(index - 1);
 		}
 		else{
 			System.out.println("Error: There is no device with this port");
 			return null;
 		}
 	}
 	
 	String getName(){
 		return deviceName;
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
 		Device d = new Device("Hi there I'mma device");
 		d.setLock(p1);
 		d = new Device("Hi there I'mma device2");
 		d = new Device("Hi there I'mma device3");
 		d.setLock(p2);
 		d = new Device("Hi there I'mma device4");
 		System.out.println(Device.getTableInfo());
 		d = Device.findDevice("D-3");
 		if(d != null){
 			System.out.println("D-3 found");
 		}
 		d = Device.findDevice("D-23");
 		if(d != null){
 			System.out.println("D-23 found");
 		}
 	}
 
 }
