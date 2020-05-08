 package ass2;
 
 import contollers.GetAuctionController;
 
 //TODO add a flag and store in controller when someone wins bid
 
 public class popAuction implements Runnable {
 	int id;
 	
 	public popAuction(int id) {
 		this.id = id;
 	}
 	
 	public void run() {
 		System.out.println("popping auction: " + id);
		new GetAuctionController(null).popAuction(id);
 	}
 
 }
