 package database.garbage;

 import java.util.TimerTask;
 import java.util.Vector;
 
 import logger.Log;
 import database.offer.Offer;
 import database.offer.OfferController;
 
 public class CollectorTask extends TimerTask{
 
 	@Override
 	public void run() {
 		Log.getInstance().write("CollectorTask", "Deleting old offers...");
 		Vector<Offer> toDelete = OfferController.getInstance().getOldOffers(2);
 		if(toDelete==null){
 			System.out.println("Fail");
 			return;
 		}
 		for(Offer o : toDelete){
 			OfferController.getInstance().deleteOffer(o);
 		}
 	}
 	
 }
