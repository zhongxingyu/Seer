 package server;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.text.DecimalFormat;
 
 import org.apache.mina.common.IoAcceptor;
 import org.apache.mina.transport.socket.nio.SocketAcceptor;
 import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
 
 import server.util.ControlPanel;
 import server.event.EventManager;
 import server.model.npcs.NPCHandler;
 import server.model.npcs.NPCDrops;
 import server.model.players.PlayerHandler;
 import server.model.players.Player;
 import server.model.players.Client;
 import server.model.players.PlayerSave;
 import server.model.minigames.*;
 import server.net.ConnectionHandler;
 import server.net.ConnectionThrottleFilter;
 import server.util.ShutDownHook;
 import server.util.SimpleTimer;
 import server.util.log.Logger;
 import server.event.Event; 
 import server.event.EventContainer;
 import server.world.ItemHandler;
 import server.world.ObjectHandler;
 import server.world.ObjectManager;
 //import server.util.MadTurnipConnection;
 import server.world.ShopHandler;
 import server.world.map.VirtualWorld;
 import server.world.ClanChatHandler;
 import server.world.WorldMap;
 import server.world.WalkingHandler;
 
 /**
  * Server.java
  *
  * @author Sanity
  * @author Graham
  * @author Blake
  * @author Ryan Lmctruck30
  *
  */
 
 public class Server {
 	
 	
 	public static boolean sleeping;
 	public static ControlPanel panel = new ControlPanel(true); // false if you want it off
 	public static int cycleRate;
 	public static boolean UpdateServer = false;
 	public static long lastMassSave = System.currentTimeMillis();
 	private static IoAcceptor acceptor;
 	private static ConnectionHandler connectionHandler;
 	private static ConnectionThrottleFilter throttleFilter;
 	private static SimpleTimer engineTimer, debugTimer;
 	private static long cycleTime, cycles, totalCycleTime, sleepTime;
 	private static DecimalFormat debugPercentFormat;
 	public static boolean shutdownServer = false;		
 	public static boolean shutdownClientHandler;			
 	public static int serverlistenerPort; 
 	public static ItemHandler itemHandler = new ItemHandler();
 	public static PlayerHandler playerHandler = new PlayerHandler();
 	public static NPCHandler npcHandler = new NPCHandler();
 	public static ShopHandler shopHandler = new ShopHandler();
 	public static ObjectHandler objectHandler = new ObjectHandler();
 	public static ObjectManager objectManager = new ObjectManager();
 	public static FightPits fightPits = new FightPits();
 	public static PestControl pestControl = new PestControl();
 	public static NPCDrops npcDrops = new NPCDrops();
 	public static ClanChatHandler clanChat = new ClanChatHandler();
 	public static FightCaves fightCaves = new FightCaves();
 	public static RFD rfd = new RFD();
 	//public static WorldMap worldMap = new WorldMap();
 	public static long[] TIMES = new long[5];
 	//private static final WorkerThread engine = new WorkerThread();
 	
 	static {
 		if(!Config.SERVER_DEBUG) {
 			serverlistenerPort = 43594;
 		} else {
 			serverlistenerPort = 43594;
 		}
 		cycleRate = 575;
 		shutdownServer = false;
 		engineTimer = new SimpleTimer();
 		debugTimer = new SimpleTimer();
 		sleepTime = 0;
 		debugPercentFormat = new DecimalFormat("0.0#%");
 	}
 	//height,absX,absY,toAbsX,toAbsY,type
     /*public static final boolean checkPos(int height,int absX,int absY,int toAbsX,int toAbsY,int type)
     {
         return I.I(height,absX,absY,toAbsX,toAbsY,type);
     }*/
 	public static void main(java.lang.String args[]) throws NullPointerException, IOException {
 try {
 	WalkingHandler.getSingleton().initialize();
 } catch(Exception ex) {
 	ex.printStackTrace();
 }
 		
 		/**
 		 * Starting Up Server
 		 */
 		 
 		System.setOut(new Logger(System.out));
 		System.setErr(new Logger(System.err));
 		System.out.println("Launching ErasedPkz...");
 		//MadTurnipConnection md = new MadTurnipConnection();
 	      //md.start();
 		
 		/**
 		 * World Map Loader
 		 */
 		//if(!Config.SERVER_DEBUG)
 			//VirtualWorld.init();
 		//WorldMap.loadWorldMap();	
 
 		/**
 		 * Script Loader
 		 */
 		//ScriptManager.loadScripts();
 		
 		/**
 		 * Accepting Connections
 		 */
 		acceptor = new SocketAcceptor();
 		connectionHandler = new ConnectionHandler();
 		
 		SocketAcceptorConfig sac = new SocketAcceptorConfig();
 		sac.getSessionConfig().setTcpNoDelay(false);
 		sac.setReuseAddress(true);
 		sac.setBacklog(100);
 		
 		throttleFilter = new ConnectionThrottleFilter(Config.CONNECTION_DELAY);
 		sac.getFilterChain().addFirst("throttleFilter", throttleFilter);
 		acceptor.bind(new InetSocketAddress(serverlistenerPort), connectionHandler, sac);
 
 		/**
 		 * Initialise Handlers
 		 */
 		//VoteForCash.createConnection();
 		EventManager.initialize();
 		Connection.initialize();
 		//PlayerSaving.initialize();
 		//MysqlManager.createConnection();
 		
 		/**
 		 * Clipped Following (NPC)
 		 */
 		try {
 		 WalkingHandler.getSingleton().initialize();
 		} catch(Exception ex) {
 		ex.printStackTrace();
 		}
 		/**
 		 * Server Successfully Loaded 
 		 */
                 //Change To IP to your Hamachi or anything else
		System.out.println("Server listening on port 5.127.47.134:" + serverlistenerPort);
 		/**
 		 * Main Server Tick
 		 */
 		try {
 			while (!Server.shutdownServer) {
 				if (sleepTime > 0)
 					Thread.sleep(sleepTime);
 				engineTimer.reset();
 				itemHandler.process();
 				playerHandler.process();	
 	                        npcHandler.process();
 				shopHandler.process();
 				objectManager.process();
 				fightPits.process();
 				pestControl.process();
 				CastleWars.process();
 				cycleTime = engineTimer.elapsed();
 				if(cycleTime < 575)
 					sleepTime = cycleRate - cycleTime;
 				else
 					sleepTime = 0;
 				totalCycleTime += cycleTime;
 				cycles++;
 				debug();
 				if(Config.SERVER_DEBUG) //i see.... i used wrong symbol lol LOL !
 					//System.out.println(cycleTime+"--"+sleepTime);
 			if (System.currentTimeMillis() - lastMassSave > 10000) {
 					for(Player p : PlayerHandler.players) {
 						if(p == null)
 							continue;						
 						PlayerSave.saveGame((Client)p);
 						System.out.println("Saved game for " + p.playerName + ".");
 						lastMassSave = System.currentTimeMillis();
 					}
 				
 				}
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			System.out.println("A fatal exception has been thrown!");
 			for(Player p : PlayerHandler.players) {
 				if(p == null)
 					continue;						
 				PlayerSave.saveGame((Client)p);
 				System.out.println("Saved game for " + p.playerName + ".");
 			}
 		}
 		acceptor = null;
 		connectionHandler = null;
 		sac = null;
 		System.exit(0);
 	}
 	
 	public static void processAllPackets() {
 		for (int j = 0; j < playerHandler.players.length; j++) {
 			if (playerHandler.players[j] != null) {
 				while(playerHandler.players[j].processQueuedPackets());			
 			}	
 		}
 	}
 	
 	public static boolean playerExecuted = false;
 	private static void debug() {
 		if (debugTimer.elapsed() > 360*1000 || playerExecuted) {
 			long averageCycleTime = totalCycleTime / cycles;
 			System.out.println("Average Cycle Time: " + averageCycleTime + "ms");
 			double engineLoad = ((double) averageCycleTime / (double) cycleRate);
 			System.out.println("Players online: " + PlayerHandler.playerCount+ ", engine load: "+ debugPercentFormat.format(engineLoad));
 			totalCycleTime = 0;
 			cycles = 0;
 			System.gc();
 			System.runFinalization();
 			debugTimer.reset();
 			playerExecuted = false;
 		}
 	}
 	
 	public static long getSleepTimer() {
 		return sleepTime;
 	}
 	
 }
