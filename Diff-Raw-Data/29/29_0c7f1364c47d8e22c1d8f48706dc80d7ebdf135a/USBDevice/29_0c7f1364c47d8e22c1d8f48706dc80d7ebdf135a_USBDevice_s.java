 
 public abstract class USBDevice<T> {
 	private Port<T> slot;
 	
 	public USBDevice() {
 		this.slot = new Port<T>();
 	}
 	
 	public Port<T> getSlot() {
 		return slot;
 	}
 	
 	public boolean eject() {
 		return slot.eject();
 	}
 	
 	public String getName() {
 		return slot.toString();
 	}
 	
 	public String toString() {
 		return getName();
 	}
 	
 	public abstract boolean insert(DataMedium dm);
 }
