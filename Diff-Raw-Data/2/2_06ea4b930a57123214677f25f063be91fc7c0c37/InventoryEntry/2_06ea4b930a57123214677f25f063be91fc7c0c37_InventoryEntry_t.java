 package adapters.db.sqlite.inventory;
 
 import adapters.db.sqlite.upcMap.UPCEntry;
 
 public class InventoryEntry {
 	private UPCEntry upc = null;
 	private String created = null;
 	
 	/*
 	 * timestamp should be in seconds
 	 */
 	public InventoryEntry(UPCEntry upc, long timestamp) {
 		this.upc = upc;
		this.created = Long.toString(timestamp / 1000);
 	}
 	
 	public InventoryEntry(UPCEntry upc, String created) {
 		this.upc = upc;
 		this.created = created;
 	}
 	
 	public UPCEntry getUPC(){
 		return upc;
 	}
 	
 	public String getCreated(){
 		return created;
 	}
 	
 	public String toString() {
 		return getUPC().toString() + " (Created: " + getCreated() + ")";
 	}
 	
 	public String toCSV(){
 		return getUPC() + "," + upc.getItemName() + "," + upc.getAmount() + "," + getCreated();
 	}
 }
