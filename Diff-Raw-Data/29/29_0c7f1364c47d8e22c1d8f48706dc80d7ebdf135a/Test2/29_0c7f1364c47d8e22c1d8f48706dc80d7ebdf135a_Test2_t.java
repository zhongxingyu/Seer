 
 public class Test2 {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
		Port<USBDevice<?>> usb1 = new Port<USBDevice<?>>();
		usb1.insert(new USBPort());

		USBPort usb = new USBPort();
		usb.insert(new USBPort());
		usb.getSlot().getDev().insert(new CardSDReader() );
		usb.getSlot().getDev().getSlot().getDev().
		//usb.insert(new CardSDReader());
		//usb.getSlot().getDev().insert(new CardSD("SD"));
 	}
 
 }
