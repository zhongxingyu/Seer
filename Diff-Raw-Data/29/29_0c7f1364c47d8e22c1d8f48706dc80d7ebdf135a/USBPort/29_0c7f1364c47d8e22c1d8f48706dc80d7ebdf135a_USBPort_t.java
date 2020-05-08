 
 public class USBPort extends USBDevice<USBDevice<?>> {
 
 	public USBPort() {
 		super();
 	}
 	
 	@Override
 	public boolean insert(DataMedium dm) {
 		return false;
 	}
 	
	public <V> boolean insert(USBDevice<V> usbdev) {
 		return getSlot().insert(usbdev);
 	}
 }
