 /**
  * TAC AgentWare
  * http://www.sics.se/tac        tac-dev@sics.se
  *
  * Copyright (c) 2001-2006 SICS AB. All rights reserved.
  *
  * SICS grants you the right to use, modify, and redistribute this
  * software for noncommercial purposes, on the conditions that you:
  * (1) retain the original headers, including the copyright notice and
  * this text, (2) clearly document the difference between any derived
  * software and the original, and (3) acknowledge your use of this
  * software in pertaining publications and reports.  SICS provides
  * this software "as is", without any warranty of any kind.  IN NO
  * EVENT SHALL SICS BE LIABLE FOR ANY DIRECT, SPECIAL OR INDIRECT,
  * PUNITIVE, INCIDENTAL OR CONSEQUENTIAL LOSSES OR DAMAGES ARISING OUT
  * OF THE USE OF THE SOFTWARE.
  *
  * -----------------------------------------------------------------
  *
  * TACAgent
  *
  * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
  * Created : 23 April, 2002
  * Updated : $Date: 2006/05/02 12:15:04 $
  *	     $Revision: 1.6 $
  * Purpose : TAC Classic AgentWare for Java
  */
 
 package se.sics.tac.aw;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.table.AbstractTableModel;
 
 import se.sics.tac.util.ArgEnumerator;
 import se.sics.tac.util.LogFormatter;
 
 public class TACAgent implements Task, TACMessageReceiver {
 
   private final static String EOL = System.getProperty("line.separator",
 						       "\r\n");
 
   public final static String VERSION = "Beta 9";
 
   private static final Logger log =
     Logger.getLogger(TACAgent.class.getName());
 
   /** Command status */
   public final static int NO_ERROR = 0;
   public final static int INTERNAL_ERROR = 1;
   public final static int AGENT_NOT_AUTH = 2;
   public final static int GAME_NOT_FOUND = 4;
   public final static int NOT_MEMBER_OF_GAME = 5;
   public final static int GAME_FUTURE = 7;
   public final static int GAME_COMPLETE = 10;
   public final static int AUCTION_NOT_FOUND = 11;
   public final static int AUCTION_CLOSED = 12;
   public final static int BID_NOT_FOUND = 13;
   public final static int TRANS_NOT_FOUND = 14;
   public final static int CANNOT_WITHDRAW_BID = 15;
   public final static int BAD_BIDSTRING_FORMAT = 16;
   public final static int NOT_SUPPORTED = 17;
   /** Extension in 1.1 */
   public final static int GAME_TYPE_NOT_SUPPORTED = 18;
 
   private final static String[] statusName = {
     "no error",
     "internal error",
     "agent not auth",
     "game not found",
     "not member of game",
     "game future",
     "game complete",
     "auction not found",
     "auction closed",
     "bid not found",
     "trans not found",
     "cannot withdraw bid",
     "bad bidstring format",
     "not supported",
     "game type not supported"
   };
 
   private final static int[] statusCodes = {
     NO_ERROR,
     INTERNAL_ERROR,
     AGENT_NOT_AUTH,
     GAME_NOT_FOUND,
     NOT_MEMBER_OF_GAME,
     GAME_FUTURE,
     GAME_COMPLETE,
     AUCTION_NOT_FOUND,
     AUCTION_CLOSED,
     BID_NOT_FOUND,
     TRANS_NOT_FOUND,
     CANNOT_WITHDRAW_BID,
     BAD_BIDSTRING_FORMAT,
     NOT_SUPPORTED,
     GAME_TYPE_NOT_SUPPORTED
   };
 
   /** Client preferences types */
   public final static int ARRIVAL = 0;
   public final static int DEPARTURE = 1;
   public final static int HOTEL_VALUE = 2;
   public final static int E1 = 3;
   public final static int E2 = 4;
   public final static int E3 = 5;
 
   public final static int CAT_FLIGHT = 0;
   public final static int CAT_HOTEL = 1;
   public final static int CAT_ENTERTAINMENT = 2;
 
   private final static String[] categoryName = {
     "flight", "hotel", "entertainment"
   };
 
   /** TAC Types */
   public final static int TYPE_INFLIGHT = 1;
   public final static int TYPE_OUTFLIGHT = 0;
 
   public final static int TYPE_GOOD_HOTEL = 1;
   public final static int TYPE_CHEAP_HOTEL = 0;
 
   public final static int TYPE_ALLIGATOR_WRESTLING = 1;
   public final static int TYPE_AMUSEMENT = 2;
   public final static int TYPE_MUSEUM = 3;
 
   public final static int MIN_FLIGHT = 0;
   public final static int MIN_HOTEL = 8;
   public final static int MIN_ENTERTAINMENT = 16;
 
   public final static int MAX_FLIGHT = 7;
   public final static int MAX_HOTEL = 15;
   public final static int MAX_ENTERTAINMENT = 27;
 
   /** Internal operations to perform after transactions have been received */
   private final static int OP_NOOP = 0x000;
   private final static int OP_GAME_STARTS = 0x001;
   private final static int OP_GAME_ENDS = 0x002;
   private final static int OP_CLOSE_AUCTION = 0x100;
   private final static int OP_CLEAR_BID = 0x100000;
 
   /** The number of auctions in a TAC game */
   private final static int NO_AUCTIONS = 28;
 
   /** Timeout for quotes (when waiting for reply) */
   private final static int QUOTE_TIMEOUT = 120 * 1000;
 
   /** Constants for automatic updates and game */
   private final static int INFO_UPDATE_PERIOD = 30000;
 
   private final static int DEFAULT_GAME_LENGTH = 12 * 60 * 1000;
 
   private final static String[] auctionType = new String[] {
     "Inflight 1", "Inflight 2", "Inflight 3", "Inflight 4",
     "Outflight 2", "Outflight 3", "Outflight 4", "Outflight 5",
     "Cheap Hotel 1", "Cheap Hotel 2", "Cheap Hotel 3", "Cheap Hotel 4",
     "Good Hotel 1", "Good Hotel 2", "Good Hotel 3", "Good Hotel 4",
     "Alligator 1", "Alligator 2", "Alligator 3", "Alligator 4",
     "Amusement 1", "Amusement 2", "Amusement 3", "Amusement 4",
     "Museum 1", "Museum 2", "Museum 3", "Museum 4"
   };
 
   private AgentImpl agent;
   private String host = "localhost";
   private int port = 6500;
   private String userName;
   private String password;
 
   private String logPrefix = "aw";
   private String childLogPrefix = logPrefix;
   private LogFormatter logFormatter;
   private FileHandler rootFileHandler;
   private FileHandler childFileHandler;
   private String childFileName;
 
   private Properties config;
 
   private TACConnection connection = null;
 
   private int nextGameID = -1;
   private long nextGameTime = -1;
 
   private String gameType;
 
   private int userID = -1;
   private long timeDiff = 0;
 
   private boolean isNextGameTaskRunning = false;
 
   private int lastHotelAuction = -1;
   private int clearID = 0;
   // Client Preferences
   private int[][] clientPrefs = new int[8][6];
 
   // Auction and ownership information
   private int[] auctionIDs = new int[NO_AUCTIONS];
   private int[] owns = new int[NO_AUCTIONS];
   private Bid[] bids = new Bid[NO_AUCTIONS];
   private Quote[] quotes = new Quote[NO_AUCTIONS];
   private float[] costs = new float[NO_AUCTIONS];
 
   private long[] pendingQuotes = new long[NO_AUCTIONS];
 
   private int[] allocate = new int[NO_AUCTIONS];
 
   private int playingGame = -1;
   private long startTime = 0;
   private int gameLength = DEFAULT_GAME_LENGTH;
   private String playingGameType;
   private int earliestTransID = -1;
   private boolean isGameStarted = false;
 
   private int[] transActions = new int[10];
   private int transActionsNum = 0;
   private int[] waitActions = new int[10];
   private int waitActionsNum = 0;
   private long lastSentTransactionRequest = 0L;
 
   private int printOwnDelay = 0;
 
   private AgentTableModel tableModel;
   private AgentDisplay display;
 
   private String connectionClassName;
 
   private int exitAfterGames = -1;
   private int gamesPlayed = 0;
   private int lastGamePlayed = -1;
 
   private TACAgent(AgentImpl agent) {
     this.agent = agent;
     for (int i = 0; i < NO_AUCTIONS; i++) {
       quotes[i] = new Quote(i);
     }
   }
 
   /**
    * This constructor is only for backward compability
    *
    * @deprecated
    */
   public TACAgent(AgentImpl agent, String host, int port,
 		  String user, String pwd, String className) {
     this(agent);
     if (host != null) {
       this.host = host;
       this.port = port;
     }
     userName = user;
     password = pwd;
     connectionClassName = className;
 
     // Default initialization of logging. Should this be done??? FIX THIS!!!
     initLogging(3, 0, false);
 
     log.fine("Starting TAC AgentWare version " + VERSION);
     log.fine("Using agent implementation " + agent.getClass().getName());
     log.fine("Using TAC server " + host + " at port " + port);
 
     agent.init(this, new ArgEnumerator(new String[0], "", false));
 
     connect();
   }
 
   public TACAgent(AgentImpl agent, ArgEnumerator a, Properties config) {
     this(agent);
     this.config = config;
 
     userName =
       trim(a.getArgument("-agent", config.getProperty("agent", null)));
     password = trim(a.getArgument("-password",
 				  config.getProperty("password", null)));
     if (userName == null) {
       System.err.println("Missing value for agent name");
       a.usage(1);
     }
     if (password == null) {
       System.err.println("Missing value for agent password");
       a.usage(1);
     }
 
     host =
       trim(a.getArgument("-host", config.getProperty("host", "localhost")));
     port = a.getArgument("-port", getInt(config, "port", 6500));
     connectionClassName =
       trim(a.getArgument("-connection",
 			 config.getProperty("connection",
 					    "se.sics.tac.aw.TACReader")));
 
     gameType =
       trim(a.getArgument("-gameType", config.getProperty("gameType", null)));
     exitAfterGames = a.getArgument("-exitAfterGames",
 				   getInt(config, "exitAfterGames", -1));
     if (exitAfterGames == 0) {
       // Exit immediately???
       System.err.println("Exit as requested after " + exitAfterGames
 			 + " played games");
       System.exit(0);
     }
 
     int consoleLevel =
       a.getArgument("-consoleLogLevel", getInt(config, "consoleLogLevel", 3));
     int fileLevel =
       a.getArgument("-fileLogLevel", getInt(config, "fileLogLevel", 0));
     this.logPrefix =
       trim(a.getArgument("-logPrefix", config.getProperty("logPrefix", "aw")));
 
     initLogging(consoleLevel, fileLevel, true);
 
     // Create directories for logs
     File fp = new File("games");
     if ((!fp.exists() && !fp.mkdir()) || !fp.isDirectory()) {
       this.childLogPrefix = this.logPrefix;
       log.severe("could not create directory 'games'");
     } else {
       this.childLogPrefix = "games" + File.separatorChar + this.logPrefix;
     }
 
     printOwnDelay = a.getArgument("-printOwnDelay",
 				  getInt(config, "printOwnDelay", 0)) * 1000;
 
     log.fine("Starting TAC AgentWare version " + VERSION);
     log.fine("Using agent implementation " + agent.getClass().getName());
     log.fine("Using TAC server " + host + " at port " + port);
 
     agent.init(this, a);
 
     // Make sure all arguments have been extracted
     a.checkArguments();
     // Allow garbage collection
     this.config = null;
 
     connect();
   }
 
 
 
   // -------------------------------------------------------------------
   // Configuration handling - only available during agent initialization
   // -------------------------------------------------------------------
 
   public String getConfig(String name, String defaultValue) {
     if (config == null) {
       return defaultValue;
     }
     return trim(config.getProperty(name, defaultValue));
   }
 
   public int getConfig(String name, int defaultValue) {
     if (config == null) {
       return defaultValue;
     }
     return getInt(config, name, defaultValue);
   }
 
 
   // -------------------------------------------------------------------
   // GUI handling
   // -------------------------------------------------------------------
 
   public void showGUI() {
     if (display == null) {
       tableModel = new AgentTableModel();
       display = new AgentDisplay(tableModel, this);
     }
     display.setVisible(true);
   }
 
 
 
   // -------------------------------------------------------------------
   // Connection handling
   // -------------------------------------------------------------------
 
   private void connect() {
     do {
       try {
 	connection = (TACConnection) Class.forName(connectionClassName).
 	  newInstance();
       } catch (Exception e) {
 	log.log(Level.SEVERE, "could not create TACConnection object of class "
 		+ connectionClassName, e);
 	fatalError("no TACConnection  available");
       }
       try {
 	connection.init(this);
       } catch (Exception e) {
 	log.log(Level.SEVERE, "Connection failure:", e);
       }
 
       if (!connection.isConnected()) {
 	log.warning("could not connect to server " + host + " at port "
 		    + port + " (will retry in 5 seconds)");
 	try {
  	  Thread.sleep(5000);
 	} catch (Exception e) {
 	}
       }
     } while (!connection.isConnected());
   }
 
 
   // -------------------------------------------------------------------
   // Timer tasks - handles game start/end, quote and bid requests, etc
   // -------------------------------------------------------------------
 
   private void cancelTimers() {
     TimeDispatcher d = TimeDispatcher.getDefault();
     d.cancelTask("gameStarts", this);
     d.cancelTask("gameEnds", this);
     d.cancelTask("hotelQuotes", this);
     d.cancelTask("flightQuotes", this);
     d.cancelTask("quotes", this);
     d.cancelTask("bids", this);
     d.cancelTask("printOwn", this);
   }
 
   public void performWork(long time, Object key, Object value) {
     TimeDispatcher td = TimeDispatcher.getDefault();
     if (key == "hotelQuotes") {
       // Request all hotel quotes
       if (value == connection) {
 	td.addTask(time + 60000, key, value, this);
 	TACConnection conn = (TACConnection) value;
 	for (int i = MIN_HOTEL; i <= MAX_HOTEL; i++) {
 	  if (!quotes[i].isAuctionClosed()) {
 	    lastHotelAuction = i;
 	    requestQuote(quotes[i], conn, false);
 	  }
 	}
       }
 
     } else if (key == "flightQuotes") {
       // Request all flight quotes
       if (value == connection) {
 	td.addTask(time + 10000, key, value, this);
 	TACConnection conn = (TACConnection) value;
 
 	for (int i = MIN_FLIGHT; i <= MAX_FLIGHT; i++) {
 	  if (!quotes[i].isAuctionClosed()) {
 	    requestQuote(quotes[i], conn, false);
 	  }
 	}
       }
 
     } else if (key == "quotes") {
       if (value == connection) {
 	// Request the entertainment quotes only
 	td.addTask(time + INFO_UPDATE_PERIOD, key, value, this);
 	requestQuotes((TACConnection) value, false, false);
       }
 
     } else if (key == "bids") {
       if (value == connection) {
 	td.addTask(time + INFO_UPDATE_PERIOD, key, value, this);
 	requestBidInfos((TACConnection) value);
       }
 
     } else if (key == "printOwn") {
       if (value == connection && (printOwnDelay > 0)) {
 	td.addTask(time + printOwnDelay, key, value, this);
 	printOwn();
       }
 
     } else if (key == "gameStarts") {
       nextGameStarts((TACConnection) value);
 
     } else if (key == "gameEnds") {
       gameEnds();
 
     } else if (key instanceof Quote) {
       requestQuote((Quote) key, (TACConnection) value, true);
     }
   }
 
 
 
   // -------------------------------------------------------------------
   // API's for the connection handlers
   // -------------------------------------------------------------------
 
   public String getHost() {
     return host;
   }
 
   public int getPort() {
     return port;
   }
 
   public String getUser() {
     return userName;
   }
 
   public String getPassword() {
     return password;
   }
 
 
 
   // -------------------------------------------------------------------
   // API's for the agent
   // -------------------------------------------------------------------
 
   public int getGameID() {
     return playingGame;
   }
 
   public long getServerTime() {
     return System.currentTimeMillis() - timeDiff;
   }
 
   public long getGameTime() {
     return getServerTime() - startTime;
   }
 
   public long getGameTimeLeft() {
     long time = startTime + gameLength - getServerTime();
     return time > 0L ? time : 0L;
   }
 
   public String getGameTimeAsString() {
     if (playingGame < 0) {
       return "--:--";
     }
     return getTimeAsString(getGameTime());
   }
 
   public String getGameTimeLeftAsString() {
     if (playingGame < 0) {
       return "--:--";
     }
     return getTimeAsString(getGameTimeLeft());
   }
 
   // Returns the time in minutes and seconds
   private String getTimeAsString(long time) {
     long timeInSeconds = time / 1000;
     int sek = (int) (timeInSeconds % 60);
     StringBuffer sb = new StringBuffer();
     sb.append(timeInSeconds / 60).append(':');
     if (sek < 10) {
       sb.append('0');
     }
     return sb.append(sek).toString();
   }
 
   public int getGameLength() {
     return gameLength;
   }
 
   public String commandStatusToString(int status) {
     for (int i = 0, n = statusCodes.length; i < n; i++) {
       if (statusCodes[i] == status) {
 	return statusName[i];
       }
     }
     return Integer.toString(status);
   }
 
   public int getServerAuctionID(int auction) {
     return auctionIDs[auction];
   }
 
   /**
    * @deprecated Use getServerAuctionID() instead!
    **/
   public int getAuctionID(int auction) {
     return auctionIDs[auction];
   }
 
   public static String getAuctionTypeAsString(int auction) {
     return auctionType[auction];
   }
 
   public static int getAuctionNo() {
     return NO_AUCTIONS;
   }
 
   public static int getAuctionCategory(int auction) {
     if (auction < 8) {
       return CAT_FLIGHT;
     } else if (auction < 16) {
       return CAT_HOTEL;
     }
     return CAT_ENTERTAINMENT;
   }
 
   // Returns the day of the auction in the range 1 - 5
   public static int getAuctionDay(int auction) {
     int day = (auction % 4) + 1;
     if ((auction / 4) == 1) {
       // Outflights are specified as day 2 to day 5
       day++;
     }
     return day;
   }
 
   // Returns the type of the auction.  Please note that
   // auctions in different categories might have the
   // same value as type.
   public static int getAuctionType(int auction) {
     int type = auction / 4;
     switch (type) {
     case 0: return TYPE_INFLIGHT;
     case 1: return TYPE_OUTFLIGHT;
     case 2: return TYPE_CHEAP_HOTEL;
     case 3: return TYPE_GOOD_HOTEL;
     case 4: return TYPE_ALLIGATOR_WRESTLING;
     case 5: return TYPE_AMUSEMENT;
     default: return TYPE_MUSEUM;
     }
   }
 
   public static int getAuctionFor(int category, int type, int day) {
     if (category == 0) {
       if (type == 1) {
 	return day - 1;
       } else {
 	return day + 2;
       }
     } else if (category == 2) {
       type--;
     }
     return category * 8 + type * 4 + day - 1;
   }
 
   private int getAuctionCategory(String cat) {
     if ("flight".equals(cat)) return CAT_FLIGHT;
     if ("hotel".equals(cat)) return CAT_HOTEL;
     if ("entertainment".equals(cat)) return CAT_ENTERTAINMENT;
     log.warning("illegal category '" + cat + '\'');
     return -1;
   }
 
   public static String auctionCategoryToString(int category) {
     return (category >= CAT_FLIGHT) && (category < categoryName.length)
       ? categoryName[category]
       : Integer.toString(category);
   }
 
   public int getClientPreference(int client, int type) {
     return clientPrefs[client][type];
   }
 
   public int getOwn(int auctionID) {
     // The id that the agent gets for auctions is always 0 - 27
     return owns[auctionID];
   }
 
     // What might be owned in addition  to "getOwn"
   public int getProbablyOwn(int auctionID) {
     Bid bid = getBid(auctionID);
     if (bid == null)
       return 0;
     Quote quote = getQuote(auctionID);
     if (quote.hasHQW(bid)) {
       return quote.getHQW();
     }
     return bid.getQuantity();
   }
 
   public synchronized Bid getBid(int auctionID) {
     return bids[auctionID];
   }
 
   public Quote getQuote(int auctionID) {
     return quotes[auctionID];
   }
 
   public int getAllocation(int auction) {
     return allocate[auction];
   }
 
   public void setAllocation(int auction, int alloc) {
     allocate[auction] = alloc;
     if (tableModel != null) {
       tableModel.fireTableCellUpdated(auction, 8);
     }
   }
 
   public void clearAllocation() {
     for (int i = 0; i < NO_AUCTIONS; i++) {
       allocate[i] = 0;
     }
   }
 
   private void clearAll() {
     isGameStarted = false;
     lastHotelAuction = -1;
     clearID = 0;
     for (int i = 0, n = clientPrefs.length; i < n; i++) {
       int[] tmp = clientPrefs[i];
       for (int j = 0, m = tmp.length; j < m; j++) {
 	tmp[j] = 0;
       }
     }
 
     for (int i = 0; i < NO_AUCTIONS; i++) {
       auctionIDs[i] = 0;
       owns[i] = 0;
       bids[i] = null;
       costs[i] = 0f;
       allocate[i] = 0;
       quotes[i].clearAll();
       pendingQuotes[i] = 0L;
     }
     if (tableModel != null) {
       tableModel.fireTableDataChanged();
     }
   }
 
   public void submitBid(Bid bid) {
     if (getGameID() < 0) {
       throw new IllegalStateException("No game playing");
     }
     int auction = bid.getAuction();
     bid.submitted();
     TACMessage msg = new TACMessage("submitBid");
     prepareBidMsg(msg, bid);
     updateBid(bid);
     sendMessage(msg, this);
   }
 
   public void replaceBid(Bid oldBid, Bid bid) {
     if (getGameID() < 0) {
       throw new IllegalStateException("No game playing");
     }
     int auction = bid.getAuction();
     int oldAuction = oldBid.getAuction();
     if (oldBid.isPreliminary()) {
       throw new IllegalStateException("Old bid is still preliminary");
     }
     if (auction != oldAuction) {
       throw new IllegalArgumentException("Bids do not have same AuctionID");
     }
     bid.submitted();
     if (oldBid != bids[auction]) {
       bid.setRejectReason(Bid.ACTIVE_BID_CHANGED);
       bid.setProcessingState(Bid.REJECTED);
       try {
 	agent.bidRejected(bid);
       } catch (Exception e) {
 	log.log(Level.SEVERE, "agent could not handle bidRejected", e);
       }
     } else {
       TACMessage msg = new TACMessage("replaceBid");
       msg.setParameter("bidID", oldBid.getID());
       msg.setParameter("bidHash", oldBid.getBidHash());
 
       prepareBidMsg(msg, bid);
       updateBid(bid);
       sendMessage(msg, this);
     }
   }
 
   // inflight((AllocDay1-Own|ProbablyOwn-BidQ[R][C])...)
   public void printOwn() {
     StringBuffer sb = new StringBuffer();
     sb.append(EOL);
     printOwn("inflights", MIN_FLIGHT, MIN_FLIGHT + 4, sb);
     sb.append(',');
     printOwn("outflights", MAX_FLIGHT - 3, MAX_FLIGHT + 1, sb);
     sb.append(EOL);
     printOwn("good hotel", MIN_HOTEL, MIN_HOTEL + 4, sb);
     sb.append(',');
     printOwn("cheap hotel", MAX_HOTEL - 3, MAX_HOTEL + 1, sb);
     sb.append(EOL);
     printOwn("wrestling", MIN_ENTERTAINMENT, MIN_ENTERTAINMENT + 4, sb);
     sb.append(',');
     printOwn("amusement", MIN_ENTERTAINMENT + 4, MIN_ENTERTAINMENT + 8, sb);
     sb.append(',');
     printOwn("museum", MIN_ENTERTAINMENT + 8, MIN_ENTERTAINMENT + 12, sb);
     sb.append(EOL);
     log.fine(sb.toString());
   }
 
   private void printOwn(String type, int startAuction, int endAuction,
 			StringBuffer sb) {
     sb.append(type).append('(');
     for (int i = startAuction; i < endAuction; i++) {
       Bid bid = getBid(i);
       Quote q = getQuote(i);
       sb.append("(").append(getAllocation(i)).append('-')
 	.append(getOwn(i)).append('|')
 	.append(getProbablyOwn(i)).append('-')
 	.append(bid != null ? bid.getQuantity() : 0);
       if (q.isAuctionClosed()) {
 	sb.append('C');
       }
       sb.append(')');
     }
     sb.append(')');
   }
 
 
 
   // -------------------------------------------------------------------
   // API's to the connection handlers
   // -------------------------------------------------------------------
 
   void reset(long timeout, TACConnection conn) {
     if (connection == conn) {
       log.fine("performing connection reset");
       cancelTimers();
       // Clear transaction q
       transActionsNum = 0;
       waitActionsNum = 0;
       disconnect(500);
       playingGame = -1;
       nextGameID = -1;
       exitGameLog();
 
       TACMessage.resetResponseTime();
 
       if (timeout > 0) {
 	try {
 	  Thread.sleep(timeout);
 	} catch (Exception e) {
 	}
       }
 
       connect();
     }
   }
 
   private void disconnect(int timeout) {
     if (connection != null && connection.isConnected()) {
       TACMessage m = new TACMessage("quit");
       try {
 	m.setMessageReceiver(this);
 	connection.sendMessage(m);
 	Thread.sleep(timeout);
       } catch (Exception e) {
       }
       connection.disconnect();
     }
   }
 
   public void sendMessage(TACMessage msg, TACMessageReceiver recv) {
     TACConnection connection = this.connection;
     if (connection != null) {
       try {
 	msg.setMessageReceiver(recv);
 	connection.sendMessage(msg);
       } catch (IOException e) {
 	log.log(Level.WARNING, "could not send message " + msg.getType(), e);
 	reset(0, connection);
       }
     } else {
       log.log(Level.WARNING, "could not send message: no connection");
     }
   }
 
   public void messageReceived(TACMessage msg) {
     if (msg.isTACError()) {
       // A TAC Error was received as reply for the message
       String type = msg.getType();
       if ("getGameConsts".equals(type)) {
 	// This is only for backward compability.  Older versions of the
 	// server might return tacerror for unknown message types.
 	log.warning("ignoring error for getGameConst: " + msg.getValue());
       } else {
 	agent.tacerrorReceived(msg);
       }
 
     } else if (msg.nextTag() && (!msg.isDeclaration() || msg.nextTag())) {
       if (msg.isTag("auth")) {
 	handleLogin(msg);
       } else if (msg.isTag("serverTime")) {
 	handleServerTime(msg);
       } else if (msg.isTag("nextGame")) {
 	handleNextGame(msg);
       } else if (msg.isTag("getGameParams")) {
 	handleGetGame(msg);
 	// Get the transactions before the games starts
 	requestTransactions(OP_GAME_STARTS);
 	if (display != null) {
 	  String status = playingGameType != null
 	    ? userName + ": Showing game " + nextGameID
 	    + " of type " + playingGameType
 	    : userName + ": Showing game " + nextGameID;
 	  display.setGameStatus(status);
 	}
       } else if (msg.isTag("getGameAuctionIDs")) {
 	handleGetAuctions(msg);
       } else if (msg.isTag("getGameConsts")) {
 	handleGetConstants(msg);
       } else if (msg.isTag("submitBid")) {
 	handleBidSubmission(msg);
       } else if (msg.isTag("replaceBid")) {
 	handleBidSubmission(msg);
       } else if (msg.isTag("getQuote")) {
 	handleQuote(msg);
       } else if (msg.isTag("bidInfo")) {
 	handleBidInfo(msg);
       } else if (msg.isTag("transIDs")) {
 	handleTransIDs(msg);
       } else if (msg.isTag("transInfo")) {
 	handleTransInfo(msg);
       } else if (msg.isTag("recoverBidIDs")) {
 	handleRecoverBidIDs(msg);
       }
     }
   }
 
   private void requestNextGame() {
     TACMessage msg = new TACMessage("nextGame");
     if (gameType != null) {
       msg.setParameter("type", gameType);
     }
 
     // Set stat info if more than 5 messages sent!
     if (TACMessage.getMessageCount() > 5) {
       msg.setParameter("stat.avgResponseTime",
 		       "" + TACMessage.getAverageResponseTime());
       msg.setParameter("stat.messageCount",
 		       "" + TACMessage.getMessageCount());
     }
 
     sendMessage(msg, this);
   }
 
   private void requestQuotes(TACConnection conn, boolean flightQuotes,
 			     boolean hotelQuotes) {
     // This should be changed so that it will only request those quotes
     // that are old enough...
     if (flightQuotes) {
       for (int i = MIN_FLIGHT; i <= MAX_FLIGHT ; i++) {
 	if (!quotes[i].isAuctionClosed()) {
 	  requestQuote(quotes[i], conn, false);
 	}
       }
     }
     for (int i = MIN_ENTERTAINMENT; i <= MAX_ENTERTAINMENT ; i++) {
       if (!quotes[i].isAuctionClosed()) {
 	requestQuote(quotes[i], conn, false);
       }
     }
 
     if (hotelQuotes) {
       for (int i = MIN_HOTEL; i <= MAX_HOTEL ; i++) {
 	if (!quotes[i].isAuctionClosed()) {
 	  lastHotelAuction = i;
 	  requestQuote(quotes[i], conn, false);
 	}
       }
     }
   }
 
   private void requestQuote(Quote quote, TACConnection conn, boolean force) {
     int auction = quote.getAuction();
     int auctionID = auctionIDs[auction];
 
     if (auctionID > 0) {
       long currentTime = System.currentTimeMillis();
 
 //       log.info("requesting quote for " + auctionID);
 
       if (!force && (pendingQuotes[auction] > 0)
 	  && ((pendingQuotes[auction] + QUOTE_TIMEOUT) > currentTime)) {
 	// Quote is already pending and it has not passed sufficient time
 	// to regards a retransmission (no use to request quotes faster
 	// than they arrive)
 	long delay = currentTime - pendingQuotes[auction];
 	if (delay > 4000) {
 	  // Warn if the quote has been delayed too long
 	  log.warning("still awaiting quote for auction " + auction
 		      + " (" + getAuctionTypeAsString(auction)
 		      + ") after " + (delay / 1000) + " sec");
 	}
 
       } else {
 	pendingQuotes[auction] = currentTime;
 	try {
 	  TACMessage msg = new TACMessage("getQuote");
 	  msg.setParameter("auctionID", auctionID);
 	  Bid bid = bids[auction];
 	  msg.setUserData(quote);
 	  if (bid != null) {
 	    int id;
 	    if ((id = bid.getID()) != Bid.NO_ID) {
 	      msg.setParameter("bidID", id);
 	      msg.setUserData(bid);
 	    } else if (((bid = bid.getReplacing()) != null)
 		       && ((id = bid.getID()) != Bid.NO_ID)) {
 	      // Request HQW for previous bid if it currently is being
 	      // replaced (in case the new bid is rejected)
 	      msg.setParameter("bidID", id);
 	      msg.setUserData(bid);
 	    }
 	  }
 	  conn.sendMessage(msg, this);
 	} catch (Exception e) {
 	  log.log(Level.SEVERE, "could not request quote for auction "
 		  + auction + " (" + getAuctionTypeAsString(auction) + ')', e);
 	  pendingQuotes[auction] = 0L;
 	  reset(0, conn);
 	}
       }
     }
   }
 
   private void requestBidInfos(TACConnection conn) {
     Bid bid;
     int bidID;
     try {
       for (int i = 0; i < NO_AUCTIONS; i++) {
 	bid = bids[i];
 	if (bid != null && ((bidID = bid.getID()) != Bid.NO_ID)
 	    && !quotes[i].isAuctionClosed()) {
 	  TACMessage msg = new TACMessage("bidInfo");
 	  msg.setParameter("bidID", bidID);
 	  msg.setUserData(bid);
 	  conn.sendMessage(msg, this);
 	}
       }
     } catch (IOException e) {
       log.log(Level.SEVERE, "could not request bid infos", e);
       reset(0, conn);
     }
     requestTransactions(OP_NOOP);
   }
 
   private synchronized void requestTransactions(int call) {
     if (transActionsNum == 0) {
       transActions[0] = call;
       transActionsNum++;
       TACMessage msg = new TACMessage("transIDs");
       msg.setParameter("earliestTransID", earliestTransID);
       lastSentTransactionRequest = System.currentTimeMillis();
       sendMessage(msg, this);
     } else {
       if (waitActionsNum == waitActions.length) {
 	int[] tmp = new int[waitActions.length * 2];
 	System.arraycopy(waitActions, 0, tmp, 0, waitActionsNum);
 	waitActions = tmp;
       }
       waitActions[waitActionsNum++] = call;
 
       long currentTime = System.currentTimeMillis();
       if ((currentTime - lastSentTransactionRequest) > 30000) {
 	// Too long time after last sent transaction
 	TACMessage msg = new TACMessage("transIDs");
 	msg.setParameter("earliestTransID", earliestTransID);
 	lastSentTransactionRequest = currentTime;
 	log.warning("WARNING: transaction timeout after "
 		    + ((currentTime - lastSentTransactionRequest) / 1000)
 		    + " sec!!! (resending)");
 	sendMessage(msg, this);
       }
     }
   }
 
   private void prepareBidMsg(TACMessage msg, Bid bid) {
     int auction = bid.getAuction();
     msg.setParameter("auctionID", auctionIDs[auction]);
     msg.setParameter("bidString", bid.getBidString());
     msg.setParameter("expireTime", 0);
     msg.setParameter("expireMode", 0);
     msg.setParameter("divisible", 1);
     msg.setUserData(bid);
   }
 
   private void nextGameStarts(TACConnection conn) {
     clearAll();
     playingGame = nextGameID;
     startTime = nextGameTime;
     playingGameType = null;
     gameLength = DEFAULT_GAME_LENGTH;
     earliestTransID = -1;
     enterGameLog(nextGameID);
     log.fine("Starting up game: " + playingGame);
 
     // If illegal state, end game and restart... should not happen?
     if (playingGame == -1) {
       log.severe("GameID error, GameID = -1 - aborting game start...");
       gameEnds();
       return;
     }
 
     try {
       TACMessage msg = new TACMessage("getGameAuctionIDs");
       msg.setParameter("gameID", nextGameID);
       conn.sendMessage(msg, this);
     } catch (IOException e) {
       log.log(Level.SEVERE, "could not request game auctions", e);
       reset(0, conn);
     }
   }
 
   private void gameEnds() {
     log.fine("Game " + nextGameID + " has ended");
     playingGame = -1;
     nextGameID = -1;
     isGameStarted = false;
 
     cancelTimers();
 
     if (earliestTransID != -1) {
       requestTransactions(OP_GAME_ENDS);
     } else {
       handleGameEnd();
     }
 
     if (exitAfterGames <= 0 || gamesPlayed < exitAfterGames) {
       requestNextGame();
     }
   }
 
   private synchronized void handleGameEnd() {
     exitGameLog();
     if (exitAfterGames > 0 && gamesPlayed >= exitAfterGames) {
       // We have played the specified number of games
       log.info("Exit as requested after " + exitAfterGames
 	       + " played games");
       disconnect(500);
       if (rootFileHandler != null) {
 	rootFileHandler.close();
       }
       System.exit(0);
     }
   }
 
   private boolean handleLogin(TACMessage msg) {
     int status = NO_ERROR;
     while (msg.nextTag()) {
       if (msg.isTag("userID")) {
 	userID = msg.getValueAsInt(-1);
 	log.fine("Logged in as " + userID);
 	TACMessage msg2 = new TACMessage("serverTime");
 	sendMessage(msg2, this);
 
 	// Do not request "nextGame" if already playing a game!!
 	if (playingGame == -1) {
 	  requestNextGame();
 	}
 	return true;
       } else if (msg.isTag("commandStatus")) {
 	status = msg.getValueAsInt(NO_ERROR);
       }
     }
     fatalError("Failed to login as " + userName + ": status="
 	       + commandStatusToString(status));
     return false;
   }
 
   private void handleBidSubmission(TACMessage msg) {
     Bid bid = (Bid) msg.getUserData();
     int status = NO_ERROR;
 
     while (msg.nextTag()) {
       if (msg.isTag("bidID")) {
 	int id = msg.getValueAsInt(Bid.NO_ID);
 	bid.setID(id);
       } else if (msg.isTag("bidHash")) {
 	String hash = msg.getValue();
 	bid.setBidHash(hash);
       } else if (msg.isTag("rejectReason")) {
 	int reject = msg.getValueAsInt(Bid.NOT_REJECTED);
 	bid.setRejectReason(reject);
 	if (reject != Bid.NOT_REJECTED) {
 	  bid.setProcessingState(Bid.REJECTED);
 	}
       } else if (msg.isTag("commandStatus")) {
 	status = mapCommandStatus(msg.getValueAsInt(NO_ERROR));
       }
     }
 
     if (bid.isRejected()) {
       // reset the active bid!
       revertBid(bid, NO_ERROR);
     } else if (status == AUCTION_CLOSED) {
       // reset the active bid!
       revertBid(bid, status);
       // Let the quote close the auction later!
     } else if (status != NO_ERROR) {
       fatalError("Can not handle bid submission: "
 		 + commandStatusToString(status), 5000);
     } else {
       // Request Bid info
       TACMessage msg2 = new TACMessage("bidInfo");
       msg2.setParameter("bidID", bid.getID());
       msg2.setUserData(bid);
       sendMessage(msg2, this);
 
       // Should not do this until bidinfo arrives where the bid is
       // hopefully no longer preliminary. (For backward compability!)
 //       agent.bidUpdated(bid);
     }
   }
 
   // the bid "bid" has been rejected/ or in error
   // ensure that the information about active bid, etc is correct
   // call agent
   private synchronized void revertBid(Bid bid, int status) {
     int auction = bid.getAuction();
 
     Bid activeBid = getBid(auction);
 
     if (bid.same(activeBid)) {
       activeBid = bid.getReplacing();
       if (activeBid != null) {
 	bids[auction] = activeBid;
       } else {
 	bids[auction] = null;
       }
     } else if (activeBid != null) {
       Bid child;
       while ((child = activeBid.getReplacing()) != null && !child.same(bid)) {
 	activeBid = child;
       }
       if (child != null && child.same(bid)) {
 	activeBid.setReplacing(child.getReplacing());
 	bid = null;
       }
       activeBid = null;
     }
 
     // if this was the active bid
     if (bid != null) {
       if (status == NO_ERROR) {
 	try {
 	  agent.bidRejected(bid);
 	} catch (Exception e) {
 	  log.log(Level.SEVERE, "agent could not handle bidRejected", e);
 	}
       } else {
 	try {
 	  agent.bidError(bid, status);
 	} catch (Exception e) {
 	  log.log(Level.SEVERE, "agent could not handle bidError", e);
 	}
       }
     }
 
 //     if (activeBid != null && !activeBid.isPreliminary()) {
 //       agent.bidUpdated(activeBid);
 //     }
   }
 
   private void handleTransIDs(TACMessage msg) {
     TACMessage msg2 = null;
     int oldEarliest = earliestTransID;
     while (msg.nextTag()) {
       if (msg.isTag("transID")) {
 	int id = msg.getValueAsInt(-1);
 	if (id > earliestTransID) {
 	  earliestTransID = id;
 	}
 	if (id > oldEarliest) {
 	  msg2 = new TACMessage("transInfo");
 	  msg2.setParameter("transID", id);
 	  sendMessage(msg2, this);
 	}
       }
     }
     if (msg2 != null) {
       // Indicate that this was the last for this transaction sessions
       msg2.setUserData(this);
     } else {
       // Nothing to retrieve
       callAgent();
     }
   }
 
   private void handleTransInfo(TACMessage msg) {
     int quantity = 0;
     int auction = 0;
     float price = 0f;
     int status = NO_ERROR;
    //have moved this to try and prevent the multiple purchase issues
    Object obj = msg.getUserData();
    if (obj != null) {
      callAgent();
    }
    
     while (msg.nextTag()) {
       if (msg.isTag("/transInfo")) {
 	if (status == NO_ERROR) {
 	  Transaction trans = new Transaction(auction, quantity, price);
 	  owns[auction] += quantity;
 	  costs[auction] += quantity * price;
 	  try {
 	    if (tableModel != null) {
 	      tableModel.fireTableRowsUpdated(auction, auction);
 	    }
 	    agent.transaction(trans);
 	  } catch (Exception e) {
 	    log.log(Level.SEVERE, "agent could not handle transaction "
 		    + trans, e);
 	  }
 	} else {
 	  // What should we do here??? FIX THIS!!!
 	}
       } else if (msg.isTag("quantity")) {
 	quantity = (int) msg.getValueAsFloat(0f);
       } else if (msg.isTag("price")) {
 	price =  msg.getValueAsFloat(0f);
       } else if (msg.isTag("auctionID")) {
 	auction = getAuctionPos(msg.getValueAsInt(0));
       } else if (msg.isTag("commandStatus")) {
 	status = msg.getValueAsInt(NO_ERROR);
       }
     }
    
   }
 
   private void callAgent() {
     for (int i = 0; i < transActionsNum; i++) {
       int ival = transActions[i];
       if ((OP_CLEAR_BID & ival) != 0) {
 	clearBid(ival - OP_CLEAR_BID);
       } else {
 	try {
 	  if ((OP_CLOSE_AUCTION & ival) != 0) {
 	    agent.auctionClosed(ival - OP_CLOSE_AUCTION);
 	  } else if (ival == OP_GAME_STARTS) {
 	    // Another game is being played
 	    if (playingGame != lastGamePlayed) {
 	      lastGamePlayed = playingGame;
 	      gamesPlayed++;
 	    }
 	    agent.gameStarted();
 	  } else if (ival == OP_GAME_ENDS) {
 	    agent.gameStopped();
 	  }
 	} catch (Throwable e) {
 	  log.log(Level.SEVERE, "agent could not handle operation " +
 		  ival, e);
 	  // Check if thread was killed
 	  if (e instanceof ThreadDeath) {
 	    throw (ThreadDeath) e;
 	  }
 	}
 
 	if (ival == OP_GAME_ENDS) {
 	  handleGameEnd();
 	} else if (ival == OP_GAME_STARTS) {
 	  TimeDispatcher d = TimeDispatcher.getDefault();
 	  long currentTime = getServerTime();
 	  isGameStarted = true;
 	  d.addTask(currentTime + INFO_UPDATE_PERIOD,
 		    "quotes", connection, this);
 	  d.addTask(currentTime + (int) (1.5 * INFO_UPDATE_PERIOD),
 		    "bids", connection, this);
 	  if (printOwnDelay > 0) {
 	    d.addTask(currentTime + printOwnDelay,
 		      "printOwn", connection, this);
 	  }
 
 	  // Start the hotel quote fetch (one second after update)
 	  long nextHotelTime = startTime + 61000;
 	  if (nextHotelTime < currentTime) {
 	    nextHotelTime += 60000 * ((currentTime - nextHotelTime) / 60000);
 	  }
 	  long nextFlightTime = startTime + 11000;
 	  if (nextFlightTime < currentTime) {
 	    nextFlightTime += 10000 * ((currentTime - nextFlightTime) / 10000);
 	  }
 
 	  TimeDispatcher.getDefault().addTask(nextHotelTime,
 					      "hotelQuotes",
 					      connection, this);
 	  TimeDispatcher.getDefault().addTask(nextFlightTime,
 					      "flightQuotes",
 					      connection, this);
 	  requestQuotes(connection, true, true);
 	}
       }
     }
     swapTransactions();
   }
 
   private synchronized void swapTransactions() {
     if (waitActionsNum == 0) {
       transActionsNum = 0;
       // Do nothing more...
     } else {
       // Set up actions to call after next transaction
       transActionsNum = waitActionsNum;
       int[] tmp = transActions;
       transActions = waitActions;
       waitActions = tmp;
       waitActionsNum = 0;
 
       // Request new transaction!!!
       TACMessage msg = new TACMessage("transIDs");
       msg.setParameter("earliestTransID", earliestTransID);
       sendMessage(msg, this);
     }
   }
 
   private void handleQuote(TACMessage msg) {
     Object obj = msg.getUserData();
     Quote quote;
     int auction;
     if (obj instanceof Quote) {
       quote = (Quote) obj;
       auction = quote.getAuction();
     } else {
       Bid bid = (Bid) obj;
       auction = bid.getAuction();
       quote = quotes[auction];
       quote.setHQW(-1);
       quote.setBid(bid);
     }
 
     // Quote is no longer pending
     pendingQuotes[auction] = 0L;
 
     int oldAuctionStatus = quote.getAuctionStatus();
     while (msg.nextTag()) {
       if (msg.isTag("lastAskPrice")) {
 	quote.setAskPrice(msg.getValueAsFloat(0f));
       } else if (msg.isTag("lastBidPrice")) {
 	quote.setBidPrice(msg.getValueAsFloat(0f));
       } else if (msg.isTag("hypotheticalQuantityWon")) {
 	quote.setHQW(msg.getValueAsInt(-1));
       } else if (msg.isTag("auctionStatus")) {
 	quote.setAuctionStatus(msg.getValueAsInt(Quote.AUCTION_INITIALIZING));
       } else if (msg.isTag("nextQuoteTime")) {
 	quote.setNextQuoteTime(1000 * msg.getValueAsLong(0));
       } else if (msg.isTag("auctionStatus")) {
 	quote.setLastQuoteTime(1000 * msg.getValueAsLong(0));
       }
     }
 
     try {
       agent.quoteUpdated(quote);
     } catch (Exception e) {
       log.log(Level.SEVERE,
 	      "agent could not handle quoteUpdated for " + quote, e);
     }
 
     try {
       if (isLastAuction(quote)) {
 	agent.quoteUpdated(getAuctionCategory(auction));
       }
     } catch (Exception e) {
       log.log(Level.SEVERE,
 	      "agent could not handle quoteUpdated for " + quote, e);
     }
     if (quote.isAuctionClosed()
 	&& (oldAuctionStatus != Quote.AUCTION_CLOSED)) {
       requestTransactions(OP_CLOSE_AUCTION + auction);
     }
     if (tableModel != null) {
       tableModel.fireTableRowsUpdated(auction, auction);
     }
   }
 
   private boolean isLastAuction(Quote quote) {
     int auction = quote.getAuction();
     int category = getAuctionCategory(auction);
     long serverTime, quoteTime;
     if (category == CAT_ENTERTAINMENT) {
       return auction == MAX_ENTERTAINMENT;
     } else if (category == CAT_FLIGHT) {
       return auction == MAX_FLIGHT;
     } else if (!quote.isAuctionClosed() &&
 	       (quoteTime = quote.getNextQuoteTime()) > 0 &&
 	       (serverTime = getServerTime()) > quoteTime) {
       lastHotelAuction = auction;
       log.fine("rerequesting hotel quote for auction " + auction);
       TimeDispatcher.getDefault()
 	.addTask(serverTime + 1000, quote, connection, this);
       return false;
     } else {
       return auction == lastHotelAuction;
     }
   }
 
   private void handleBidInfo(TACMessage msg) {
     Bid bid = (Bid) msg.getUserData();
     String bidHash = null;
     String bidString = null;
     int rejectReason = Bid.NOT_REJECTED;
     int processingState = Bid.UNPROCESSED;
     long timeClosed = 0L;
     long timeProcessed = 0L;
     int commandStatus = NO_ERROR;
 
     while (msg.nextTag()) {
       if (msg.isTag("bidString")) {
 	bidString = msg.getValue();
       } else if (msg.isTag("bidHash")) {
 	bidHash = msg.getValue();
       } else if (msg.isTag("rejectReason")) {
 	rejectReason = Bid.mapRejectReason(msg.getValueAsInt(rejectReason));
       } else if (msg.isTag("processingState")) {
 	processingState =
 	  Bid.mapProcessingState(msg.getValueAsInt(processingState));
       } else if (msg.isTag("timeClosed")) {
 	timeClosed = msg.getValueAsLong(0);
       } else if (msg.isTag("timeProcessed")) {
 	timeProcessed = msg.getValueAsLong(0);
       } else if (msg.isTag("commandStatus")) {
 	commandStatus = msg.getValueAsInt(NO_ERROR);
       }
     }
 
     // Potential problem 1:
     // 1. BidInfo is sent by return of submitBid
     // 2. BidInfo is sent pga 45 second period bid info requesting
     // 3. BidInfo is received with changed bidHash
     // 4. BidInfo is received with same bidHash as 3 but different from msg.Bid
 
     // Potential problem 2:
     // 1. Bid is submitted with submitBid
     // 2. BidInfo is requested
     // 3. Bid is submitted with submitBid
     // 4. BidInfo for 2 is received
 
     if (commandStatus != NO_ERROR) {
       log.warning("could not retrieve bidInfo for bid " + bid.getID()
 		  + " in auction " + bid.getAuction() + ": "
 		  + commandStatusToString(commandStatus));
     } else {
       // Bid is ok (not preliminary or rejected)!
       bid.setReplacing(null);
       bid.setProcessingState(processingState);
       bid.setRejectReason(rejectReason);
       bid.setTimeProcessed(timeProcessed);
       bid.setTimeClosed(timeClosed);
 
       String oldHash = bid.getBidHash();
       if (oldHash == null && !isGameStarted) {
 	// The bid is being recovered at startup
 	bid.setBidHash(bidHash);
 	bid.setBidString(bidString);
 	recoverBid(bid);
       } else if (!bidHash.equals(oldHash)) {
 	int auction = bid.getAuction();
 	int clearID = this.clearID++;
 	log.finest("Requesting transactions for bid " + bid.getID()
 		   + " ClearID=" + clearID);
 	requestTransactions(OP_CLEAR_BID + (clearID << 5) + auction);
 	bid.setBidTransacted(clearID, bidHash, bidString);
       } else {
 	try {
 	  agent.bidUpdated(bid);
 	} catch (Exception e) {
 	  log.log(Level.SEVERE, "agent could not handle bidUpdated", e);
 	}
       }
       int row = bid.getAuction();
       if (tableModel != null) {
 	tableModel.fireTableRowsUpdated(row, row);
       }
     }
   }
 
   private synchronized void clearBid(int transID) {
     int auction = transID & 31;
     int clearID = transID >> 5;
 
     Bid activeBid = getBid(auction);
     while (activeBid != null) {
       if (activeBid.getClearID() == clearID) {
 	String bidString = activeBid.getClearString();
 	Bid newBid = new Bid(activeBid, bidString, activeBid.getClearHash());
 	boolean isActiveBid = activeBid == getBid(auction);
 	if (bidString.equals(Bid.EMPTY_BID_STRING)) {
 	  removeBid(auction, activeBid);
 	} else {
 	  changeBid(auction, activeBid, newBid);
 	}
 	if (isActiveBid) {
 	  try {
 	    agent.bidUpdated(newBid);
 	  } catch (Exception e) {
 	    log.log(Level.SEVERE, "agent could not handle bidUpdated", e);
 	  }
 	  if (tableModel != null) {
 	    tableModel.fireTableRowsUpdated(auction, auction);
 	  }
 	}
 	activeBid = null;
 
       } else {
 	activeBid = activeBid.getReplacing();
       }
     }
   }
 
   private synchronized void recoverBid(Bid bid) {
     int auction = bid.getAuction();
     if (bids[auction] != null) {
       log.warning("bid already exist for auction "
 		  + getAuctionTypeAsString(auction)
 		  + " when recovering bid");
     } else {
       bids[auction] = bid;
       log.finer("bid " + bid.getID() + " for "
 		+ getAuctionTypeAsString(auction) + " has been recovered");
     }
   }
 
   private synchronized void updateBid(Bid bid) {
     int auction = bid.getAuction();
     bid.setReplacing(bids[auction]);
     bids[auction] = bid;
   }
 
   private synchronized void changeBid(int auction, Bid bid, Bid newBid) {
     Bid activeBid = getBid(auction);
     if (activeBid != null) {
       if (activeBid.same(bid)) {
 	bids[auction] = newBid;
       } else {
 	Bid child;
 	while ((child = activeBid.getReplacing()) != null && !child.same(bid))
 	  {
 	    activeBid = child;
 	  }
 	if (child != null && child.same(bid)) {
 	  activeBid.setReplacing(newBid);
 	}
       }
     }
   }
 
   private void removeBid(int auction, Bid bid) {
     changeBid(auction, bid, null);
   }
 
   private void handleGetAuctions(TACMessage msg) {
     while (msg.nextTag()) {
       if (msg.isTag("auctionIDs")) {
 	int cat = -1;
 	int type = -1;
 	int day = -1;
 	int id = -1;
 	while (msg.nextTag() && !msg.isTag("/auctionIDs")) {
 	  if (msg.isTag("/TACAuctionTuple")) {
 	    if (cat < 0 || id < 0) {
 	      // Missing information about this auction.
 	      // What should we do here??? FIX THIS!!!
 	      log.severe("missing information for auction"
 			 + " category: " + cat
 			 + " type: " + type
 			 + " day: " + day
 			 + " id: " + id);
 	    } else {
 	      addAuction(cat, type, day, id);
 	    }
 	  } else if (msg.isTag("type")) {
 	    type = msg.getValueAsInt(-1);
 	  } else if (msg.isTag("day")) {
 	    day = msg.getValueAsInt(-1);
 	  } else if (msg.isTag("category")) {
 	    cat = getAuctionCategory(msg.getValue());
 	  } else if (msg.isTag("ID")) {
 	    id = msg.getValueAsInt(-1);
 	  }
 	}
       } else if (msg.isTag("commandStatus")) {
 	int status = mapCommandStatus(msg.getValueAsInt(NO_ERROR));
 	if (status == GAME_FUTURE) {
 	  // Wait a second and retry!
 	  try {
 	    Thread.sleep(1000);
 	  } catch (Exception e) {
 	  }
 	  log.fine("handleGetAuctions: Game future, retrying");
 	  nextGameStarts(connection);
 	  return;
 	} else if (status == GAME_COMPLETE) {
 	  log.fine("handleGetAuctions: Game completed, ending");
 	  gameEnds();
 	} else if (status != NO_ERROR) {
 	  fatalError("could not get auctions for game "
 		     + nextGameID + ": status="
 		     + commandStatusToString(status), 5000);
 	}
       }
     }
 
     // Check if the agent already have any bids in the game i.e.
     // if the agent has been restarted during a game
     if ((getServerTime() - startTime) > 2500) {
       msg = new TACMessage("recoverBidIDs");
       sendMessage(msg, this);
     }
 
     // Extension!!!
     msg = new TACMessage("getGameConsts");
     msg.setParameter("gameID", nextGameID);
     sendMessage(msg, this);
 
     msg = new TACMessage("getGameParams");
     msg.setParameter("gameID", nextGameID);
     sendMessage(msg, this);
   }
 
   private void handleGetConstants(TACMessage msg) {
     // The status code will be NOT_SUPPORTED and the message will contain
     // no other fields if the server did not support this command
     // => does not need to check it
     while (msg.nextTag()) {
       if (msg.isTag("gameLength")) {
 	int len = msg.getValueAsInt(-1);
 	if (len > 0) {
 	  this.gameLength = len * 1000;
 	}
       } else if (msg.isTag("gameType")) {
 	this.playingGameType = msg.getValue();
       }
     }
   }
 
   private void handleGetGame(TACMessage msg) {
     boolean gameRunning = true;
 
     while (msg.nextTag()) {
       if (msg.isTag("clientPreferences")) {
 	msg.nextTag(); // Ignore list...
 	int client = -1;
 	int arr = 0;
 	int dep = 0;
 	int hotel = 0;
 	int type = -1;
 	int[] events = new int[3];
 	int price = 0;
 	while (msg.nextTag() && !msg.isTag("/clientPreferences")) {
 	  if (msg.isTag("/clientPrefTuple")) {
 	    if (client != -1) {
 	      setClient(client - 1, arr, dep, hotel, events);
 	    }
 	  } else if (msg.isTag("client")) {
 	    client = msg.getValueAsInt(-1);
 	  } else if (msg.isTag("arrival")) {
 	    arr = msg.getValueAsInt(-1);
 	  } else if (msg.isTag("departure")) {
 	    dep = msg.getValueAsInt(-1);
 	  } else if (msg.isTag("hotel")) {
 	    hotel = (int) msg.getValueAsFloat(-1f);
 	  } else if (msg.isTag("ticketPreferences")) {
 	    while (msg.nextTag() && !msg.isTag("/ticketPreferences")) {
 	      if (msg.isTag("type")) {
 		type = msg.getValueAsInt(-1);
 	      } else if (msg.isTag("price")) {
 		price = (int) msg.getValueAsFloat(-1f);
 	      } else if (msg.isTag("/typePriceTuple")) {
 		events[type - 1] = price;
 	      }
 	    }
 	  }
 	}
       } else if (msg.isTag("ticketEndowments")) {
 	int day = -1;
 	int type = -1;
 	int quantity = -1;
 	while (msg.nextTag() && !msg.isTag("/ticketEndowments")) {
 	  if (msg.isTag("/ticketEndowmentTuple")) {
 	    addOwn(CAT_ENTERTAINMENT, type, day, quantity);
 	  } else if (msg.isTag("type")) {
 	    type = msg.getValueAsInt(-1);
 	  } else if (msg.isTag("day")) {
 	    day = msg.getValueAsInt(-1);
 	  } else if (msg.isTag("quantity")) {
 	    quantity = msg.getValueAsInt(-1);
 	  }
 	}
       } else if (msg.isTag("commandStatus")) {
 	int status = msg.getValueAsInt(NO_ERROR);
 	if (status != NO_ERROR) {
 	  if (status == GAME_COMPLETE) {
 	    log.fine("handleGetGame: Game completed, ending");
 	    gameRunning = false;
 	    gameEnds();
 	  } else {
 	    fatalError("could not get game parameters for game "
 		       + nextGameID + ": status="
 		       + commandStatusToString(status), 5000);
 	    gameRunning = false;
 	  }
 	}
       }
     }
 
     if (gameRunning) {
       TimeDispatcher.getDefault()
 	.addTask(startTime + 1000 + gameLength, "gameEnds",
 		 connection, this);
     }
   }
 
   private void handleRecoverBidIDs(TACMessage msg) {
     while (msg.nextTag()) {
       if (msg.isTag("auctionBidIDsTuple")) {
 	int auctionID = -1;
 	int bidID = -1;
 	while (msg.nextTag()) {
 	  if (msg.isTag("auctionID")) {
 	    auctionID = msg.getValueAsInt(-1);
 	  } else if (msg.isTag("bidID")) {
 	    bidID = msg.getValueAsInt(-1);
 	  } else if (msg.isTag("/auctionBidIDsTuple")) {
 	    if (auctionID != -1 && bidID != -1) {
 	      int auction = getAuctionPos(auctionID);
 	      Bid bid = new Bid(auction);
 	      bid.setID(bidID);
 
 	      // Request information about this bid
 	      log.finer("recovering bid " + bidID + " for "
 			+ getAuctionTypeAsString(auction));
 	      TACMessage msg2 = new TACMessage("bidInfo");
 	      msg2.setParameter("bidID", bidID);
 	      msg2.setUserData(bid);
 	      sendMessage(msg2, this);
 	    }
 	    break;
 	  }
 	}
       } else if (msg.isTag("commandStatus")) {
 	int status = msg.getValueAsInt(NO_ERROR);
 	if (status != NO_ERROR) {
 	  log.severe("could not recover bids for game "
 		     + nextGameID + ": status="
 		     + commandStatusToString(status));
 	}
       }
     }
   }
 
   private void addOwn(int category, int type, int day, int quantity) {
     int pos = getAuctionFor(category, type, day);
     owns[pos] += quantity;
   }
 
   private void addAuction(int category, int type, int day, int id) {
     int pos = getAuctionFor(category, type, day);
     auctionIDs[pos] = id;
     log.finest("Auction " + pos + " (" + getAuctionTypeAsString(pos)
 	       + "): " + id);
   }
 
   private int getAuctionPos(int id) {
     for (int i = 0; i < NO_AUCTIONS; i++) {
       if (auctionIDs[i] == id) {
 	return i;
       }
     }
     throw new IllegalArgumentException("auction " + id + " not found");
   }
 
   private void setClient(int client, int arr, int dep, int hotel,
 			 int[] events) {
     int[] prefs = clientPrefs[client];
     prefs[ARRIVAL] = arr;
     prefs[DEPARTURE] = dep;
     prefs[HOTEL_VALUE] = hotel;
     prefs[E1] = events[0];
     prefs[E2] = events[1];
     prefs[E3] = events[2];
   }
 
 
   private void handleNextGame(TACMessage msg) {
     TACConnection connection = this.connection;
     if (connection == null) return;
 
     int status = NO_ERROR;
     int gameID = -1;
     long startTime = 0L;
 
     while (msg.nextTag()) {
       if (msg.isTag("gameID")) {
 	gameID = msg.getValueAsInt(-1);
       } else if (msg.isTag("startTime")) {
 	startTime = msg.getValueAsLong(-1);
       } else if (msg.isTag("commandStatus")) {
 	status = mapCommandStatus(msg.getValueAsInt(NO_ERROR));
       }
     }
 
     if ((gameID >= 0) && (startTime > 0)) {
       this.nextGameID = gameID;
       this.nextGameTime = startTime * 1000;
       long delay = this.nextGameTime - getServerTime();
       log.fine("Next Game -> " + nextGameID + " (should wait "
 	       + (delay / 1000) + ')');
 
       if (delay < 0) {
 	// Game might have ended!!! FIX THIS!!!!
 	if ((delay + 1000 + gameLength) < 0) {
 	  if (playingGame == gameID) {
 	    gameEnds();
 	  }
 	  // Should not play this game again... request again later...
 	  this.nextGameID = -1;
 	} else {
 	  nextGameStarts(connection);
 	}
       } else {
 	if (display != null) {
 	  display.setGameStatus(userName + ": Waiting for game: " +
 				nextGameID);
 	}
 	if (delay > 50000) {
 	  long sleepTime = (long) (60 * (10000 + Math.random() * 2000));
 	  reset(sleepTime < delay ? sleepTime : delay, connection);
 	} else {
 	  TimeDispatcher.getDefault()
 	    .addTask(nextGameTime + 1000, "gameStarts", connection, this);
 	}
       }
     } else if (status != NO_ERROR) {
       fatalError("Failed to get next game"
 		 + (gameType != null ? " with game type '" + gameType + '\''
 		    : "") + ": status="
 		 + commandStatusToString(status), 5000);
     }
   }
 
   private void handleServerTime(TACMessage msg) {
     while (msg.nextTag()) {
       if (msg.isTag("time")) {
 	long serverTime = msg.getValueAsLong(-1) * 1000;
 	long time = System.currentTimeMillis();
 	timeDiff = time - serverTime;
 	log.fine("Setting server time diff to " + (timeDiff / 1000)
 		 + " seconds");
 	// Make sure the log formatter uses the server time instead of
 	// local time
 	logFormatter.setTimeDiff(timeDiff);
 	TimeDispatcher.getDefault().setTimeDiff(timeDiff);
       }
     }
   }
 
   private static int mapCommandStatus(int status) {
     if (status == 9) {
       return GAME_FUTURE;
     } else {
       return status;
     }
   }
 
 
 
   // -------------------------------------------------------------------
   // Logging handling
   // -------------------------------------------------------------------
 
   private void initLogging(int consoleLevel, int fileLevel,
 			   boolean exitIfFileFails) {
     Level consoleLogLevel = LogFormatter.getLogLevel(consoleLevel);
     Level fileLogLevel = LogFormatter.getLogLevel(fileLevel);
     Level logLevel = consoleLogLevel.intValue() < fileLogLevel.intValue()
       ? consoleLogLevel : fileLogLevel;
 
     // Initialize the logging
     Logger root = Logger.getLogger("se");
     root.setLevel(logLevel);
 
     LogFormatter.setConsoleLevel(consoleLogLevel);
 //     LogFormatter.setLevelForAllHandlers(logLevel);
 
     if (fileLevel < 6) {
       try {
 	this.rootFileHandler = new FileHandler(logPrefix + "%g.log",
 					       1000000, 10);
 	this.rootFileHandler.setLevel(fileLogLevel);
 	root.addHandler(this.rootFileHandler);
       } catch (IOException ioe) {
 	log.log(Level.SEVERE, "could not log to file", ioe);
 	if (exitIfFileFails) {
 	  fatalError("could not log to file '" + logPrefix + "0.log'");
 	}
       }
     }
 
     this.logFormatter = new LogFormatter();
     // Set shorter names for the log
     this.logFormatter.setAliasLevel(2);
     LogFormatter.setFormatterForAllHandlers(this.logFormatter);
   }
 
   private synchronized void enterGameLog(int gameID) {
     if (rootFileHandler != null) {
       exitGameLog();
       LogFormatter.separator(log, Level.FINE, "Entering log for game "
 			     + gameID);
       try {
 	Logger root = Logger.getLogger("");
 	String name = childLogPrefix + "_GAME_" + gameID + ".log";
 	childFileHandler = new FileHandler(name, true);
 	childFileHandler.setFormatter(logFormatter);
 	childFileHandler.setLevel(rootFileHandler.getLevel());
 	childFileName = name;
 	root.addHandler(childFileHandler);
 	root.removeHandler(rootFileHandler);
 	LogFormatter.separator(log, Level.FINE, "Log for game "
 			       + gameID + " started");
 
       } catch (Exception e) {
 	log.log(Level.SEVERE, "could not open child log file for game "
 		+ gameID, e);
       }
     }
   }
 
   private synchronized void exitGameLog() {
     if (childFileHandler != null && rootFileHandler != null) {
       Logger root = Logger.getLogger("");
       LogFormatter.separator(log, Level.FINE, "Game log complete");
 
       root.addHandler(rootFileHandler);
       root.removeHandler(childFileHandler);
       childFileHandler.close();
       childFileHandler = null;
       if (childFileName != null) {
 	new File(childFileName + ".lck").delete();
 	childFileName = null;
       }
     }
   }
 
 
 
   // -------------------------------------------------------------------
   //
   // -------------------------------------------------------------------
 
   private class AgentTableModel extends AbstractTableModel {
 
     private final String[] columnName = new String[] {
       "ID", "Type", "Ask Price", "Bid Price", "Status", "PS",
       "BidString", "HQW", "Allocation", "Own", "Cost"
     };
 
     public String getColumnName(int col) {
       return columnName[col];
     }
 
     public int getRowCount() {
       return 28;
     }
 
     public int getColumnCount() {
       return columnName.length;
     }
 
     public Object getValueAt(int row, int col) {
       switch (col) {
       case 0:
 	return Integer.toString(auctionIDs[row]);
       case 1:
 	return getAuctionTypeAsString(row);
       case 2:
 	return Float.toString(quotes[row].getAskPrice());
       case 3:
 	return Float.toString(quotes[row].getBidPrice());
       case 4:
 	return quotes[row].getAuctionStatusAsString();
       case 5:
 	Bid bd = bids[row];
 	return (bd != null)
 	  ? bd.getProcessingStateAsString()
 	  : "no bid";
       case 6:
 	Bid bid = bids[row];
 	if (bid != null) {
 	  return bid.getBidString();
 	}
 	return "no bid";
       case 7:
 	{
 	  int hqw = quotes[row].getHQW();
 	  return hqw >= 0 ? Integer.toString(hqw) : "";
 	}
       case 8:
 	return Integer.toString(allocate[row]);
       case 9:
 	return Integer.toString(owns[row]);
       case 10:
 	return Float.toString(costs[row]);
       default:
 	return "-";
       }
     }
   }
 
 
 
   // -------------------------------------------------------------------
   // Temporary fatal error handling
   // -------------------------------------------------------------------
 
   void fatalError(String message) {
     log.severe("************************************************************");
     log.severe("* FATAL ERROR: " + message);
     log.severe("************************************************************");
     disconnect(500);
     System.exit(1);
   }
 
   void fatalError(String message, long delay) {
     log.severe("************************************************************");
     log.severe("* FATAL ERROR: " + message);
     log.severe("************************************************************");
     reset(delay, connection);
   }
 
 
 
   // -------------------------------------------------------------------
   // Startup and argument handling
   // -------------------------------------------------------------------
 
   public static void main(String[] args) {
     String usage =
       "Usage: tacagent.jar [-options]\n"
       + "where options include:\n"
       + "    -config <configfile>      set the config file to use\n"
       + "    -agentimpl <className>    set the agent implementation\n"
       + "    -agent <agentname>        set the agent name\n"
       + "    -password <password>      set the agent password\n"
       + "    -host <host>              set the TAC Info Server host\n"
       + "    -port <port>              set the TAC Info Server port\n"
 //       + "    -gameType <type>          set the game type to play\n"
       + "    -exitAfterGames <games>   set the number of games to play\n"
       + "    -connection <className>   set the TAC connection handler\n"
       + "    -consoleLogLevel <level>  set the console log level\n"
       + "    -fileLogLevel <level>     set the file log level\n"
       + "    -logPrefix <prefix>       set the prefix to log files\n"
       + "    -nogui                    do not show agent gui\n"
       + "    -h                        show this help message\n";
     ArgEnumerator a = new ArgEnumerator(args, usage, false);
     String configFile = a.getArgument("-config");
     if (configFile == null) {
       configFile = "agent.conf";
     }
 
     File configFP = new File(configFile);
     Properties config = getConfig(configFile, configFP);
     if (config == null) {
       config = new Properties();
     }
 
     String agentClass =
       trim(a.getArgument("-agentimpl",
 			 config.getProperty("agentimpl",
 					    "se.sics.tac.aw.DummyAgent")));
 
     // Try to create the agent
     AgentImpl agent;
     try {
       agent = (AgentImpl) Class.forName(agentClass).newInstance();
     } catch (Exception e) {
       log.log(Level.SEVERE, "could not create AgentImpl object of class "
 	      + agentClass, e);
       new TACAgent(null).fatalError("no agent implementation available");
       return;
     }
 
     // Add the agents own arguments
     usage = agent.getUsage();
     if (usage != null) {
       a.setUsage(a.getUsage() + "\nOptions for agent implementation "
 		 + agentClass + '\n' + usage + '\n');
     }
     a.checkHelp();
 
     boolean gui =
       !(a.hasArgument("-nogui")
 	|| "true".equals(config.getProperty("nogui", null)));
 
     TACAgent agentWare = new TACAgent(agent, a, config);
     if (gui) {
       agentWare.showGUI();
     }
     // Allow garbage usage
     usage = null;
     a = null;
     configFP = null;
     config = null;
   }
 
   // Returns the configuration from the specified configuration file
   // if that file exists. Otherwise NULL is returned.
   // If the config file could not be properly parsed the Java will
   // be terminated with an error message.
   public static Properties getConfig(String configFile) {
     return getConfig(configFile, new File(configFile));
   }
 
   private static Properties getConfig(String configFile, File configFP) {
     try {
       if (configFP.exists()) {
 	InputStream input =
 	  new BufferedInputStream(new FileInputStream(configFP));
 	try {
 	  Properties p = new Properties();
 	  p.load(input);
 	  p.setProperty("CONFIG_FILE", configFile);
 	  return p;
 	} finally {
 	  input.close();
 	}
       }
     } catch (Exception e) {
       System.err.println("could not read config file '" + configFile + "':");
       e.printStackTrace();
       System.exit(1);
     }
     return null;
   }
 
   private static int getInt(Properties p, String name, int defaultValue) {
     String v = trim(p.getProperty(name));
     if (v != null) {
       try {
 	return Integer.parseInt(v);
       } catch (Exception e) {
 	System.err.println("Non-integer value for parameter '" + name
 			   + "'='" + v
 			   + "' in config file "
 			   + p.getProperty("CONFIG_FILE", ""));
 	System.exit(1);
       }
     }
     return defaultValue;
   }
 
   private static String trim(String text) {
     return (text == null || (text = text.trim()).length() == 0)
       ? null
       : text;
   }
 
 }
