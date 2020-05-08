 package at.ac.tuwien.dslab2.service.server;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.TreeMap;
 
 import at.ac.tuwien.dslab2.domain.Auction;
 import at.ac.tuwien.dslab2.domain.Bid;
 import at.ac.tuwien.dslab2.domain.Client;
 import at.ac.tuwien.dslab2.domain.User;
 
 public class AuctionServiceImpl implements AuctionService {
 	private final Map<String, User> users;
	private final SortedMap<Long, Auction> auctions;
 	private final Timer timer;
	private volatile long idCounter;
 
 	// Private constructor prevents instantiation from other classes
 	private AuctionServiceImpl() {
 		users = Collections.synchronizedMap(new LinkedHashMap<String, User>());
 		auctions = Collections
				.synchronizedSortedMap(new TreeMap<Long, Auction>());
 		timer = new Timer("Timer thread");
 		idCounter = 1;
 	}
 
 	private static class AuctionServiceHolder {
 		public static final AuctionService INSTANCE = new AuctionServiceImpl();
 	}
 
 	public static AuctionService getInstance() {
 		return AuctionServiceHolder.INSTANCE;
 	}
 
 	@Override
 	public Auction create(User owner, String description, int duration) {
 
 		Auction a = new Auction(idCounter++, description, owner, duration);
 		auctions.put(a.getId(), a);
 
 		// Start timer for auction
 		synchronized (timer) {
 			timer.schedule(new AuctionEndTask(a), a.getEndDate());
 		}
 		return a;
 	}
 
 	@Override
 	public String list() {
 		if (auctions.isEmpty())
 			return "There are currently no auctions running!";
 
 		StringBuilder sb = new StringBuilder();
 		synchronized (auctions) {
 			for (Auction a : auctions.values()) {
 				sb.append(a.toString());
 				sb.append("\n");
 			}
 		}
 
 		return sb.toString();
 	}
 
 	@Override
 	public Auction bid(User user, int auctionId, double amount) {
 
 		Auction a = auctions.get(auctionId);
 
 		if (a == null)
 			return null;
 
 		User prevHighestBidder = a.getHighestBidder();
 
 		a.addBid(new Bid(amount, user));
 
 		// Notify overbid
 		if (prevHighestBidder != null && !prevHighestBidder.equals(user)
 				&& !prevHighestBidder.equals(a.getHighestBidder())) {
 			prevHighestBidder.addNotification("!new-bid " + a.getDescription());
 		}
 
 		return a;
 	}
 
 	@Override
 	public User login(String userName, Client client) {
 		User u;
 		if (users.containsKey(userName)) {
 			u = users.get(userName);
 		} else {
 			u = new User(userName, client);
 			users.put(userName, u);
 		}
 
 		return u;
 	}
 
 	@Override
 	public void logout(User user) {
 		user.setClient(null);
 		user.setLoggedIn(false);
 	}
 
 	@Override
 	public void close() throws IOException {
 		this.timer.cancel();
 	}
 
 	private class AuctionEndTask extends TimerTask {
 		Auction a;
 
 		public AuctionEndTask(Auction a) {
 			super();
 			this.a = a;
 		}
 
 		@Override
 		public void run() {
 			final User owner = a.getOwner();
 			final User winner = a.getHighestBidder();
 			final Bid bid = a.getHighestBid();
 
 			final String notification = "!auction-ended "
 					+ (winner != null ? winner.toString() : "none")
 					+ " "
 					+ (bid != null ? String.format("%.2f", bid.getAmount())
 							: "0.00") + " " + a.getDescription();
 
 			// Notify owner
 			if (owner != null)
 				owner.addNotification(notification);
 
 			// Notify winner
 			if (winner != null && !winner.equals(owner))
 				winner.addNotification(notification);
 
 			auctions.remove(a.getId());
 		}
 	}
 
 }
