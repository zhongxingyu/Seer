 package server.logic;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import analyticsServer.event.EventFactory;
 
 import server.ServerStatus;
 
 public class AuctionDATABASE {
 	private static AuctionDATABASE instance=null;
 	public final static int NO_AUCTION_WITH_ID_FOUND=0;
 	public final static int SUCCESSFULLY_PLACED_BID=1;
 	public final static int NEEDS_MORE_MONEY=2;
 	public final static int AUCTION_EXPIRED=3;
 	public final static int OWNER_SAME_AS_BIDDER=4;
 	//Concerning Group bids
 	public final static int EXISTING_POLL=5;
 	public final static int SUCCESSFULLY_PLACED_POLL=6;
 	public final static int REJECTED_POLL=7;
 	public final static int PRICE_MISSMATCH=8;
 	public final static int INITIATOR_MISSMATCH=9;
 	public final static int SUCCESSFULLY_CONFIRMED_POLL=10;
 
 
 	private int idCounter=0;
 	private ConcurrentHashMap<Integer,Auction> auctionList;
 	private ConcurrentHashMap<Integer,TentativeBid> tentativeBids;
 	private GroupQueue queue;
 
 	private AuctionDATABASE(){
 		auctionList= new ConcurrentHashMap<Integer, Auction>();
 		tentativeBids= new ConcurrentHashMap<Integer, TentativeBid>();
 		queue= new GroupQueue();
 	}
 
 	public static AuctionDATABASE getInstance(){
 		if(instance==null)
 			instance= new AuctionDATABASE();
 		return instance;
 	}
 
 	public synchronized int createAuction(User owner, String description, long duration){
 		Auction tmp= new Auction(owner, description, duration*1000,++idCounter);
 		//tmp.setID(++idCounter);
 
 
 		auctionList.put(idCounter,tmp);
 
 		UserDATABASE.getInstance().getUser(owner.getName()).getConnection().print("!ack-create "+
 				tmp.getID()+
 				" "+tmp.getCreation().getTime()+
 				" "+duration+
 				" "+description);
 		return idCounter;
 	}
 
 
 	public synchronized int bidOnAuction(int id, User bidder, double money){
 		Auction tmp= auctionList.get(id);
 		
 		/*
 		if(bidder.getID()== tmp.getOwner().getID())
 			return OWNER_SAME_AS_BIDDER;
 		*/
 
 		if(tmp!=null){
 			if(tmp.isExpired())
 				return AUCTION_EXPIRED;
 
 			if(tmp.getPrice()>= money){
 				return NEEDS_MORE_MONEY;
 			}else{
 				// BID_OVERBID event
 
 				if(tmp.getHighestBidder()!=null)
 					ServerStatus.getInstance().notifyAnalyticsServer(EventFactory.createBidEvent(tmp.getHighestBidder().getName(), id, tmp.getPrice(), 1));
 
 
 				tmp.setHighestBidder(bidder);
 				tmp.setPrice(money);
 				auctionList.remove(id);
 				auctionList.put(id, tmp);
 
 				// BID_PLACED event
 
 				ServerStatus.getInstance().notifyAnalyticsServer(EventFactory.createBidEvent(bidder.getName(), id, money, 0));
 
 				return SUCCESSFULLY_PLACED_BID;
 			}
 		}else return NO_AUCTION_WITH_ID_FOUND;
 	}
 
 	public synchronized String getList(){
		String result = "";
 		Auction auction=null;
 		
 		Integer[] tmp= new Integer[auctionList.size()];
 		
 		auctionList.keySet().toArray(tmp);
 		Arrays.sort(tmp);
 		
 		
 		for(Integer key: tmp){
 			auction= auctionList.get(key);
 			if(!auction.isExpired()){
 				result= result+"\t"+auction.toString()+"\n";				
 			}			
 		}
 
 		if(result.length()==0)
 			result="There are currently no auctions.";
 		return result;
 	}
 
 	public synchronized ArrayList<Auction> getAuctionList(){
 		ArrayList<Auction> tmp = new ArrayList<Auction>();
 		Auction a=null;
 		for(Integer key: auctionList.keySet()){
 			a= auctionList.get(key);
 			if(!a.isExpired()){
 				tmp.add(a);
 			}
 
 		}
 		return tmp;
 	}
 
 
 	public synchronized String getFullList(){
 		String result = "";
 		Auction auction=null;
 		for(Integer key: auctionList.keySet()){
 			auction= auctionList.get(key);
 			result= result+auction.toDetailString()+"\n";				
 		}
 		if(result.length()==0)
 			result="There are currently no auctions.";
 		return result;
 	}
 
 	public synchronized String getDescription(int id){
 		if(auctionList.containsKey(id))
 			return auctionList.get(id).getDescription();
 		else return null;
 	}
 
 	public synchronized void killAuctions(){
 		//System.out.println("ending running auctions");
 		//kill queue
 		queue.cancel();
 		
 		//kill auctions
 		Auction tmp=null;
 		for(Integer key: auctionList.keySet()){
 			tmp= auctionList.get(key);
 			if(!tmp.isExpired()){
 				//System.out.println("kill!");
 				tmp.stop();
 			}
 		}
 
 		//kill tentativeBids
 		TentativeBid bid=null;
 		for(Integer key: tentativeBids.keySet()){
 			bid= tentativeBids.get(key);
 			bid.cancel();
 		}
 	}
 	private synchronized int numberOfTentativeBids(){
 		TentativeBid bid=null;
 		int n=0;
 
 		for(Integer key: tentativeBids.keySet()){
 			bid= tentativeBids.get(key);
 			if(bid.isTimedOut() && !bid.isConfirmed()){
 				tentativeBids.remove(bid.getAuctionId());
 			}else if(bid.isConfirmed()){
 				n++;
 			}
 		}
 		return n;
 	}
 
 	private synchronized int numberOfGroupAuctions(){
 		Auction bid=null;
 		int n=0;
 		
 		User bidder=null;
 		for(Integer key: auctionList.keySet()){
 			bid= auctionList.get(key);
 			
 			bidder= bid.getHighestBidder();
 			if(bidder!=null){
 				if(bidder.getName().equals("group"))
 					n++;
 			}
 				
 				
 
 		}
 		return n;
 	}
 
 	private synchronized boolean isGroupBidPossisble(String initiator){
 		//return (numberOfTentativeBids() <= UserDATABASE.getInstance().getActiveUsers());
 		boolean check= (numberOfGroupAuctions() <= UserDATABASE.getInstance().getActiveUsers());
 		
 		if(check== true){
 			check= check && queue.queue(initiator);
 		}
 		
 		return check;
 	}
 
 	public synchronized int createGroupBid(int auctionID, double price, String initiator){
 		if(!isGroupBidPossisble(initiator))
 			return REJECTED_POLL;
 
 		if(auctionList.get(auctionID) == null  )
 			return NO_AUCTION_WITH_ID_FOUND;
 
 		TentativeBid bid= tentativeBids.get(auctionID);
 		if(tentativeBids.get(auctionID)!=null && !bid.isTimedOut())
 			return EXISTING_POLL;
 		else{
 			tentativeBids.put(auctionID, new TentativeBid(auctionID, initiator, price));
 
 			return SUCCESSFULLY_PLACED_POLL;
 		}
 
 	}
 
 	public synchronized int confirmTentativeBid(int auctionId, double price, String initiator){
 		TentativeBid bid=tentativeBids.get(auctionId);
 		if(bid==null)
 			return NO_AUCTION_WITH_ID_FOUND;
 
 		else if(!bid.getInitiator().equals(initiator))
 			return INITIATOR_MISSMATCH;
 
 		else if(bid.getPrice() != price)
 			return PRICE_MISSMATCH;
 		else{
 			bid.confirm();
 
 			return SUCCESSFULLY_CONFIRMED_POLL;
 		}
 
 	}
 
 
 
 }
