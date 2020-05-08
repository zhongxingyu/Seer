 package topo;
 
 import java.util.List;
 
 import basic.Device;
 
 import gm.Group;
 
 /**
  * Class that implements the logic for peer selection. It periodically interogates DS 
  * for updates regarding devices(address, online status). It uses strategies for providing the
  * peers for PUSH and PULL operations depending on the devices properties. 
  * There is one OverlayManager per group Overlay. 
  * @author adriana
  *
  */
 public class OverlayManager {
 
 	private Group group;
 	private Overlay overlay;
 	
 	/**
 	 * Provides a list of devices from which the user should pull data 
	 * @return the list of devices
 	 */
 	public List<Device> getDevicesForPull(){return null;}
 	/**
 	 * Provides a list of devices from which the user should push data 
	 * @return the list of devices
 	 */
 	public List<Device> getDevicesForPush(){return null;}
 	
 	/**
 	 * The OverlayManager is informed that a change in the group's structure has occurred, so it
 	 * needs to update the overlay. 
 	 */
 	public void changeOccured(){}
 	
 }
