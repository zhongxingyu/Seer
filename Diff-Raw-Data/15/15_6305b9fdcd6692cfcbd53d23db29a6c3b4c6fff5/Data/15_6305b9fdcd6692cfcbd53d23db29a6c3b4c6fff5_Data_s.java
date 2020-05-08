 package dslab.auctionserver;
 
 import dslab.analyticsserver.AuctionEvent;
 import dslab.analyticsserver.BidEvent;
 import dslab.analyticsserver.EventNotFoundException;
 import dslab.billingserver.BillingServerInterface;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.security.KeyPair;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.util.ArrayList;
 import java.util.Date;
 import org.bouncycastle.openssl.PEMReader;
 import org.bouncycastle.openssl.PasswordFinder;
 
 
 
 
 /**
  * A Singleton holding important information and/or code that can be accessed from everywhere
  * @author rr
  */
 public class Data {
 
 	private ArrayList<User> user = new ArrayList<User>();
 	private ArrayList<Auction> auctions = new ArrayList<Auction>();
 	private PrivateKey privateKeyServer = null;
 	private PublicKey publicKeyServer = null;
 	private String keydirpath = null;
 	private String pathToServerPrivKey = null; 
 	
 
 	private static Data instance;
 
 	public static Data getInstance() {
 		if (instance == null) {
 			instance = new Data();
 		}
 		return instance;
 	}
 
 	private Data() {
 	}
 
 	public ArrayList<User> getUser() {
 		return user;
 	}
 
 	public void setUser(ArrayList<User> user) {
 		this.user = user;
 	}
 
 	public ArrayList<Auction> getAuctions() {
 		return auctions;
 	}
 
 	public void setAuctions(ArrayList<Auction> auctions) {
 		this.auctions = auctions;
 	}
 
 	public PrivateKey getPrivateKeyServer() {
 		return privateKeyServer;
 	}
 
 	public void setPrivateKeyServer(PrivateKey privateKeyServer) {
 		this.privateKeyServer = privateKeyServer;
 	}
 
 	public PublicKey getPublicKeyServer() {
 		return publicKeyServer;
 	}
 
 	public void setPublicKeyServer(PublicKey publicKeyServer) {
 		this.publicKeyServer = publicKeyServer;
 	}
 
 	public String getKeydirpath() {
 		return keydirpath;
 	}
 
 	public void setKeydirpath(String keydirpath) {
 		this.keydirpath = keydirpath;
 	}
 
 	public String getPathToServerPrivKey() {
 		return pathToServerPrivKey;
 	}
 
 	public void setPathToServerPrivKey(String pathToServerPrivKey) {
 		this.pathToServerPrivKey = pathToServerPrivKey;
 	}
 
 
 
 	synchronized public void addUser(User u) {
 		user.add(u);
 	}
 
 	synchronized public int getUserIndex(User u) {
 		return user.indexOf(u);
 	}
 
 	synchronized public User getUser(int i) {
 		return user.get(i);
 	}
 
 	synchronized public int getAuctionIndex(Auction a) {
 		return auctions.indexOf(a);
 	}
 
 	synchronized public Auction getAuction(int i) {
 		return auctions.get(i);
 	}
 
 	synchronized public void addAuction(Auction a) {
 		auctions.add(a);
 	}
 
 	synchronized public String listAuctions() {
 		String out = "";
 		int i = 0;
 		for (Auction a : auctions) {
 			if (a.isIncluded() == true) {
 				i++;
 				out += a.toString() + System.getProperty("line.separator");
 			}
 		}
 		if (i == 0) {
 			out = "Currently there are no auctions!";
 		}
 		return out;
 	}
 
 	synchronized public void logoutUsers() {
 		for (User u : user) {
 			u.logout();
 		}
 		System.out.println("Logout");
 	}
 	
 	synchronized public String getClientList(){
		String out = "";
 		for (User u : user){
 			out += u.getHostAddress() + ":" + u.getTcpPort() + " - " + u.getUsername() + System.getProperty("line.separator");
 		}
 		return out;
 	}
 
 	synchronized public void updateAuctions() {
 		for (Auction a : auctions) {
 			Date now = new Date();
 			if (a.isIncluded() && now.after(a.getEnd())) {
 				a.setIncluded(false);
 				if (a.isBid()) {
 					BillingServerProtocol.getInstance().sendBill(a.getCreator().getUsername(), a.getId(), (double)a.getHighestBid());
 					try {
 						AnalyticsServerProtocol.getInstance().processEvent(new BidEvent(BidEvent.placed, new Date().getTime(), a.getHighestBidder().getUsername(), a.getId(), a.getHighestBid()));
 					} catch (EventNotFoundException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				try {
 					AnalyticsServerProtocol.getInstance().processEvent(new AuctionEvent(AuctionEvent.ended, new Date().getTime(), a.getId()));
 				} catch (EventNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 
 
 	public void initKeys(){
 		//get the private key for the server:
 		PEMReader privIn = null;
 		try {
 			privIn = new PEMReader(new FileReader(pathToServerPrivKey), new PasswordFinder() {
 				@Override
 				public char[] getPassword() {
 					char[] password = new char[5];
 					for(int i=2; i<7; ++i){
 						password[i-2] = Integer.toString(i).charAt(0);
 					}
 					return password;
 				}
 			});
 		} catch (FileNotFoundException ex) {
 			System.out.println("server cannot find its private key file...");
 		}
 		KeyPair keyPair = null; 
 		try {
 			keyPair = (KeyPair) privIn.readObject();
 		} catch (IOException ex) {
 			System.out.println(pathToServerPrivKey + " Wrong password for the serverKey WTF?");
 			return;
 		}
 		this.privateKeyServer = keyPair.getPrivate();
 	}
 
 
 }
 
 
 
