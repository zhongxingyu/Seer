 package catalog;
 import java.util.ArrayList;
 import java.util.Random;
 
 public class DeviceEntry {
 	String name;
 	int deviceID;
 	int buildID;
 	Long timestamp; 
 	ArrayList<ParameterEntry> parameters;
 
 	public DeviceEntry(String name, int deviceID){
 		this.name = name;
 		this.deviceID = deviceID;
 		parameters = new ArrayList<ParameterEntry>();
 	}
 	public void addParameter(ParameterEntry param){
 		parameters.add(param);
 	}
 	//gets random device
 	public static DeviceEntry randomEntry(){
 		Random gen = new Random();
 		String[] boards = {"BMS", "Driver Controls", "Light Board 1", "Light Board 2", "MPPT1", "MPPT2", "MPPT3", "MPPT4", "Suspension Travel", "Tire Pressure"};
 		DeviceEntry returnDevice = new DeviceEntry(boards[gen.nextInt(boards.length)], gen.nextInt(256));
		for(int i = 6; i < gen.nextInt(30); i++){
 			returnDevice.addParameter(ParameterEntry.randomEntry());
 		}
 		return returnDevice;
 	}
 }
