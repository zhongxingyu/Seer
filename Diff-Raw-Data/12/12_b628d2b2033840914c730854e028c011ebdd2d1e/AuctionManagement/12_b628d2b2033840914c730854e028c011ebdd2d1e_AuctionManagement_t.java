 package server;
 
 import java.net.SocketException;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Timer;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.apache.log4j.Logger;
 
 /**
  * manages auctions
  * @author Babz
  *
  */
 public class AuctionManagement {
 
 	private static AuctionManagement instance;
 	private static UserManagement userMgmt = UserManagement.getInstance();
 	private static final Logger LOG = Logger.getLogger(AuctionManagement.class);
 	private final static ExecutorService threadpool = Executors.newCachedThreadPool();
 
 	private Map<Integer, Auction> allActiveAuctions = Collections.synchronizedMap(new ConcurrentHashMap<Integer, Auction>());
 	private static int auctionId = 0;
 	private Timer timer;
 
 	private enum StatusFlag {OVERBID, TERMINATED}
 
 	private AuctionManagement() {
 		timer = new Timer();
 		timer.scheduleAtFixedRate(new AuctionExpirationController(), 0, 5000);
 	}
 
 	public static synchronized AuctionManagement getInstance() {
 		if(instance == null) { 
 			instance = new AuctionManagement();
 			LOG.info("instance of AuctionManagement created");
 		}
 		return instance;
 	}
 
 	public String getAllActiveAuctions() {
 		String activeAuctions = "";
 		if(allActiveAuctions.isEmpty()) {
 			activeAuctions = "There are currently no active auctions";
 		} else {
 			synchronized (allActiveAuctions) {
 				for(Entry<Integer, Auction> entry: allActiveAuctions.entrySet()) {
 					activeAuctions += entry.getValue().toString() + "\n";
 				}
 			}
 		}
 		return activeAuctions;
 	}
 
 	/**
 	 * checks map for expired auctions and removes them from map;
 	 * when an auction terminates, highest bidder and owner get informed
 	 */
 	public synchronized void checkExpiredAuctions() {
 		synchronized (allActiveAuctions) {
 			for(Entry<Integer, Auction> entry: allActiveAuctions.entrySet()) {
 				Date now = new Date(System.currentTimeMillis());
 				Date expiration = entry.getValue().getExpirationDate(); 
 				if(now.compareTo(expiration) > 0) {
 					int auctionId = entry.getKey();
 					sendNotification(auctionId, StatusFlag.TERMINATED);
 					// informUsersAboutTermination(auctionId);
 					allActiveAuctions.remove(auctionId);
 				}
 			}
 		}
 	}
	
 	/**
 	 * sends udp notification to clients;
 	 * Terminated: notifications are sent to owner and winner
 	 * Overbid: previously highest bidder is notified
 	 * @param id auction id
 	 * @param flag termination notification or overbid notification is sent
 	 */
 	private void sendNotification(int id, StatusFlag flag) {
 		Auction currAuction = getAuction(id);
 		if(flag == StatusFlag.OVERBID) {
 			String prevHighestBidder = currAuction.getPreviousHighestBidder();
 			String message = "!new-bid " + currAuction.getDescription();
 			sendUdpMsg(prevHighestBidder, message);
 		} else if(flag == StatusFlag.TERMINATED) {
 			String owner = currAuction.getOwner();
 			String winner = currAuction.getHighestBidder();
 			String message = "!auction-ended " + winner + " " + currAuction.getHighestBidString() + " " + currAuction.getDescription();
 			sendUdpMsg(owner, message);
 			if(!currAuction.getHighestBidder().equals("none")) {
 				sendUdpMsg(winner, message);
 			}
 		} else {
 			System.out.println("flag is null!");
 		}
 
 	}
 
	public void sendUdpMsg(String user, String msg) {
 		boolean isOnline = userMgmt.getUserByName(user).isLoggedIn();
 		int udpPort = getUserUdpPort(user);
 		if(!isOnline) {
 			userMgmt.getUserByName(user).storeNotification(msg);
 		} else {
 			try {
 				threadpool.execute(new ServerUdpSocket(udpPort, msg));
 			} catch (SocketException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				System.out.println("problem opening udp socket");
 			}
 		}
 	}
 
 	private int getUserUdpPort(String username) {
 		return userMgmt.getUserByName(username).getUdpPort();
 	}
 
 	/**
 	 * creates an auction
 	 * @param owner
 	 * @param duration
 	 * @param description
 	 * @return id of auction
 	 */
 	public synchronized int createAuction(String owner, int duration, String description) {
 		Auction auctionNew = new Auction(++auctionId, owner, duration, description);
 		allActiveAuctions.put(auctionId, auctionNew);
 		return auctionId;
 	}
 
 	/**
 	 * returns expiration date of auction
 	 * @param id auction id
 	 * @return expiration date
 	 */
 	public Date getExpiration(int id) {
 		return allActiveAuctions.get(id).getExpirationDate();
 	}
 
 	/**
 	 * returns active auction
 	 * @param id look for auction by id
 	 * @return auction if active; null otherwise
 	 */
 	public Auction getAuction(int id) {
 		if(!allActiveAuctions.containsKey(id)) {
 			return null;
 		}
 		return allActiveAuctions.get(id);
 	}
 
 	public int bid(int id, double amount, String userName) {
 		Auction auction = getAuction(id);
 		if(auction == null) {
 			return -1;
 		} else {
 			double currHighestBid = auction.getHighestBid();
 			if(amount <= currHighestBid) {
 				return 0;
 			} else {
 				auction.setHighestBid(amount, userName);
 				if(!auction.getPreviousHighestBidder().equals("none")) {
 					sendNotification(id, StatusFlag.OVERBID);
 				}
 				return 1;
 			}
 		}
 
 	}
 
 }
