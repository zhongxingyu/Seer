 package main;
 
 import java.io.ObjectInputStream;
 import java.util.Calendar;
 
 import interfaces.Publisher;
 
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageListener;
 import javax.jms.ObjectMessage;
 
 public class BidMessageListenerImpl implements MessageListener {
 
 	Publisher publisher;
 
 	public BidMessageListenerImpl(Publisher publisher) {
 		super();
 		this.publisher = publisher;
 	}
 
 	@Override
 	public void onMessage(Message arg0) {
 			Auction auction = null;
 			ObjectMessage message = (ObjectMessage) arg0;
 			try {
 				auction = (Auction) message.getObject();
 			} catch (JMSException e) {
 				e.printStackTrace();
 			}
 
 			if (publisher.getAuctions().contains(auction)) {
				synchronized(publisher.getAuctions()){
 					for (Auction setAuction : publisher.getAuctions()) {
 						if (setAuction.equals(auction)) {
 							if (setAuction.getPrice() < auction.getPrice() && 
 									setAuction.getEndTime().compareTo(Calendar.getInstance().getTime()) > 0) {
 								setAuction.setPrice(auction.getPrice());
 								System.out.println("New highest bid in Your auction !:\n" 
 										+ setAuction.printDescription());
 								publisher.newHighestBid(setAuction);
 							}else{
 	//							System.out.println("Received bid was too low or the auction is finished.");
 							}
 							break;
 						}
 					}
 				}
 			}
 	}
 
 }
