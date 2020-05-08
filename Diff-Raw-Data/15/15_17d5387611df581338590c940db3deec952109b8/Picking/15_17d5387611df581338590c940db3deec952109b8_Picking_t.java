 package madesy.model;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import madesy.model.types.PickingStatus;
 
 public class Picking {
 	private String id;
 	private List<Integer> barcodes = new ArrayList<Integer>();
 	private PickingStatus pickingStatus;
 	private String senderId;
 	
 	public Picking(String senderId) {
 		this.id = UUID.randomUUID().toString();
 		this.pickingStatus = PickingStatus.NEW;
 		this.senderId = senderId;
 	}
 	
 	@Override
 	public String toString() {
 		return "Picking [id=" + id + ", barcodes=" + barcodes
 				+ ", pickingStatus=" + pickingStatus + ", clientId=" + senderId
 				+ "]";
 	}
 
 	@Override
 	public int hashCode() {;
 		return id.hashCode();
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (!(obj instanceof Picking))
 			return false;
 		Picking other = (Picking) obj;
		if (id.equals(other.id))
 			return false;
 		return true;
 	}
 	
 	public String getId() {
 		return id;
 	}
 	
 	public List<Integer> getBarcodes() {
 		return barcodes;
 	}
 	
 	public void setBarcodes(List<Integer> barcodes) {
 		this.barcodes = barcodes;
 	}
 	
 	public PickingStatus getPickingStates() {
 		return pickingStatus;
 	}
 	
 	public void setPickingStates(PickingStatus pickingStatus) {
 		this.pickingStatus = pickingStatus;
 	}
 	
 	public String getSenderId() {
 		return senderId;
 	}
 	
 	public void setSenderId(String senderId) {
 		this.senderId = senderId;
 	}
 	
 }
