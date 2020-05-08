 package net.sayon.dovor;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
import java.util.ArrayList;
 import java.util.Random;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.sayon.dovor.events.EventDispatcher;
 import net.sayon.dovor.listeners.BuddyListener;
 
 public class Dovor {
 	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
 	private static final Logger log;
 
 	private Server server;
 	private BuddyList buddyList;
 	private Random random = new Random();
 	private Configuration config;
 	private int status = Buddy.ONLINE;
 	private BuddyListener dispatcher;
	private ArrayList<BuddyListener> buddyListeners = new ArrayList<BuddyListener>();
 
 	static { // TODO switch to log4j
 		log = Logger.getLogger(Dovor.class.getName());
 		log.setLevel(Dovor.getLogLevel());
 	}
 
 	public static void main(String[] args) {
 		new Dovor().init();
 	}
 
 	public Dovor() {
 		this.config = new Configuration();
 		this.dispatcher = new EventDispatcher(this);
 	}
 
 	public void init() {
 		try {
 			config.load("config.ini");
 		} catch (IOException e) {
 			log.severe("Failed to load config " + e.getLocalizedMessage());
 			throw new RuntimeException(e);
 		}
 		config.loadTorId();
 		// yqnkszic6ax54sjx
 		this.server = new Server(config.getLocalPort(), this);
 		server.start();
 
 		this.buddyList = new BuddyList(this);
 		try {
 			buddyList.loadBuddylist();
 		} catch (FileNotFoundException e) {
 			log.warning("Failed to load Buddylist: " + e.getLocalizedMessage());
 		}
 
 		// test code
 		Buddy b = getBuddyList().getBuddy("yqnkszic6ax54sjx", true);
 		b.setFullBuddy(true);
 		b.setNextReconnection();
 		try {
 			b.requestAdd();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		System.out.println((System.currentTimeMillis() - b.getNextReconnection()));
 	}
 
 	public static Level getLogLevel() {
 		return Level.FINEST;
 	}
 
 	public BuddyList getBuddyList() {
 		return buddyList;
 	}
 
 	public Random getRandom() {
 		return random;
 	}
 
 	public Configuration getConfig() {
 		return config;
 	}
 
 	public void setStatus(int status) {
 		this.status = status;
 		log.info("Status: " + status);
 	}
 
 	public int getStatus() {
 		return status;
 	}
 
 	public String getStatusStringInternal() {
 		return getStatusStringInternal(status);
 	}
 
 	public static String getStatusStringInternal(int status) {
 		if (status == Buddy.ONLINE)
 			return "available";
 		else if (status == Buddy.AWAY)
 			return "away";
 		else if (status == Buddy.XA)
 			return "xa";
 		else
 			return null; // should never happen
 	}
 
 	public String getStatusStringDisplay() {
 		return getStatusStringDisplay(status);
 	}
 
 	public static String getStatusStringDisplay(int status) {
 		if (status == Buddy.ONLINE)
 			return "Available";
 		else if (status == Buddy.AWAY)
 			return "Away";
 		else if (status == Buddy.XA)
 			return "Extended Away";
 		else if (status == Buddy.OFFLINE)
 			return "Offline";
 		else if (status == Buddy.CONNECTING_ME)
 			return "Handshake";
 		else if (status == Buddy.CONNECTING_THEM)
 			return "Handshake";
 		else if (status == Buddy.CONNECTING)
 			return "Handshake";
 		else
 			return null; // should never happen
 	}
 
 	public ScheduledExecutorService getExecutor() {
 		return executor;
 	}
 
	public void addBuddyListener(BuddyListener bl) {
 		if (bl == null)
 			throw new NullPointerException();
		buddyListeners.add(bl);
 	}
 
 	public BuddyListener getDispatcher() {
 		return dispatcher;
 	}
 
	public ArrayList<BuddyListener> getBuddyListeners() {
 		return buddyListeners;
 	}
 }
