 
 public class USBPort extends USBDevice<USBDevice<?>> {
 
 	public USBPort() {
 		super();
 	}
 	
 	@Override
 	public boolean insert(DataMedium dm) {
 		return false;
 	}
 	
	public <T> boolean insert(USBDevice<T> usbdev) {
 		return getSlot().insert(usbdev);
 	}
 }
