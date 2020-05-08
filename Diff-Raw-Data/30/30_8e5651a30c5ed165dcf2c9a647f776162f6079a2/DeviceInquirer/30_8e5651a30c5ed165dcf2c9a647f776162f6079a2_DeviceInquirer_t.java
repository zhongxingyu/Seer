 /**
  * 
  */
 package topo;
 
 import gm.Group;
 
 import java.util.List;
 
 import basic.Device;
 import basic.User;
 
 /**
  * This class is the one that communicates with the Discovery and Naming Services in order
  * to provide information regarding devices. 
  *(should be singleton)
  * @author adriana
  *
  */
 public class DeviceInquirer {
 
 	/**
 	 * Communicates with the remote services to check if the device is still online.
 	 * @param device
 	 * @return true of online, false if offline, null if the device was not registered
 	 */
 	public Boolean isDeviceOnline(Device device){return null;}
 	/**
 	 * Communicates with the remote services to check if the device is still registered.
 	 * @param device
 	 * @return true if device is registered, false otherwise
 	 */
 	public Boolean isDeviceRegistered(Device device) {return null;}
 	/**
 	 * Communicates with the remote services to retrieve the list of devices registered by a give user
 	 * @param user 
 	 * @return the list of Device objects containing their addresses and all the necessary information
 	 */
 	public List<Device> getUserDevices(User user) {return null;}
 	/**
 	 * Communicates with the remote services to retrieve the list of devices registered by every user in the given group
	 * @param group the group for which to retrieve devices
 	 * @return the list of Device objects, not necessarily ordered by user 
 	 */
 	public List<Device> getDevices(Group group) {return null;}
 		
 }
