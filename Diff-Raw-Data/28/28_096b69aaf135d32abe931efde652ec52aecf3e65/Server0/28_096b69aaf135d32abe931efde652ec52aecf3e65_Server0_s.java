 package org.aimas.craftingquest.core;
 
 import gnu.cajo.invoke.Remote;
 import gnu.cajo.utils.ItemServer;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.SwingUtilities;
 
 import org.aimas.craftingquest.gui.GraphicInterface;
 import org.aimas.craftingquest.state.Blueprint;
 import org.aimas.craftingquest.state.GameState;
 import org.aimas.craftingquest.state.PlayerState;
 import org.aimas.craftingquest.state.Point2i;
 import org.aimas.craftingquest.state.Transition;
 import org.aimas.craftingquest.state.Transition.ActionType;
 import org.aimas.craftingquest.state.objects.ICrafted;
 import org.aimas.craftingquest.state.resources.ResourceType;
 import org.aimas.craftingquest.state.TransitionResult;
 import org.aimas.craftingquest.state.UnitState;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 /**
  * 
  * @author Razvan
  */
 public class Server0 implements IServer {
 
 	/* communication */
 	private String servername = "CraftingQuest";
 	private int portNumber = 1198;
 	
 	//private List<Object> clients;
 	private Object[] clients;
 	private boolean guiOn = false;
 	private GraphicInterface displayer;
 	private int connectedClients = 0;
 	private boolean[] unresponsive;
 	
 	/* game */
 	private GameState state;
 	private ActionEngine actionEngine;
 	private Timer timer;
 	private long[] secrets;
 	private long gameStartTime = 0;
 	
 	/* sync */
 	private Object updateSync = new Object();
 
 	/* logging */
 	private static Logger logger = Logger.getLogger(Server0.class);
 	private static Logger gui_logger = Logger.getLogger("org.aimas.craftingquest.core.guilogger");
 	
 	public Server0(String servername, int portNumber, String secretsFile) throws Exception {
 		this.servername = servername;
 		this.portNumber = portNumber;
 		
 		state = GameGenerator.setupGame();
 		
 		System.out.println("Game has " + state.round.noRounds + " rounds.");
 		
 		actionEngine = new ActionEngine(state);
 		timer = new Timer();
 		secrets = readSecrets(secretsFile);
 		
 		if (secrets == null) {
 			throw new Exception("could not retrieve secrets");
 		}
 		
 		if(state.playerStates.size() != secrets.length){
 		    throw new Exception("each client must have a secret");
 		}
 		
 		clients = new Object[secrets.length];
 		unresponsive = new boolean[secrets.length];
 		for (int i = 0; i < unresponsive.length; i++) {
 			unresponsive[i] = false;
 		}
 		
 		if (System.getProperty("savemap") != null) {
 			/*boolean savemap =*/ Boolean.parseBoolean(System.getProperty("savemap"));
 			GamePolicy.saveMapResources(state);
 		}
 		
 		if (System.getProperty("gui") != null) {
 			guiOn = Boolean.parseBoolean(System.getProperty("gui"));
 		}
 		
 		if (guiOn) {
 			displayer = new GraphicInterface(state);
 			SwingUtilities.invokeLater(new Runnable() {
 				
 				@Override
 				public void run() {
 					displayer.setVisible(true);
 				}
 			});
 		}
 		
 	}
 
 	public static void main(String[] args) throws Exception {
 		// args are servername, port, secrets file
 		Server0 server = null;
 		
 		// să ruleze cu GUI
		//System.setProperty("gui", "true");
 		
 		if(args == null || args.length != 3) {
 			server = new Server0("CraftingQuest", 1198, "secrets.txt");
 		}
 		else {
 			if (args != null && args.length == 3) {
 				String serverName = args[0];
 				int port = Integer.parseInt(args[1]);
 				String secretsFileName = args[2];
 				
 				server = new Server0(serverName, port, secretsFileName);
 			}
 		}
 		
 		// configure logger
 		PropertyConfigurator.configure("logging.properties");
 		
 		Remote.config(null, server.getPortNumber(), null, 0);
 		ItemServer.bind(server, server.getServername());
 
 		server.gameLoop();
 	}
 
 	
 	/* communication */
 	public synchronized int addRemoteClient(Object client) {
 		Long secret = null;
 		try {
 			secret = (Long) Remote.invoke(client, "getSecret", 0);
 		} catch (Exception ex) {
 			logger.error("Failed adding remote client. Could not invoke getSecret", ex);
 		}
 		
 		int clientNo = allowedSecret(secret);
 		
 		if(clientNo == -1) {
 		    logger.warn("[srv][register] - newclient not authorized: secret = " + secret);
 		    return -1;
 		}
 		
 		logger.info("[srv][register] - newclient_" + secret);
 		if (clients[clientNo] == null) {
 		    clients[clientNo] = client;
 		    connectedClients++;				// increase number of connected players 
 		}
 		
 		return clientNo;	
 	}
 	
 	
 	private void sendEvent(int clientID, Object client, Event event) {
 		try {
 			if (!unresponsive[clientID]) {
 				Remote.invoke(client, "onEvent", event);
 			}
 		} catch (Exception ex) {
 			// mark client as unresponsive
 			unresponsive[clientID] = true;
 			logger.fatal("Could not send event " + event.type + " to client " + clientID, ex);
 		}
 	}
 	
 	
 	private void updateGame() {
 		
 		synchronized(updateSync) {
 			// check if we still have a connected client
 			boolean responsiveClients = false;
 			for (int i = 0; i < unresponsive.length; i++) {
 				if (!unresponsive[i]) {
 					responsiveClients = true;
 					break;
 				}
 			}
 			
 			if (!responsiveClients) {
 				declareWinner();
 				System.exit(10);
 			}
 			
 			// increment turn number
 			state.round.currentRound++;
 			
 			System.out.println("Current round: " + state.round.currentRound);
 			
 			long roundStartTime = gameStartTime + state.round.currentRound * GamePolicy.roundTime;
 			state.round.startTime = roundStartTime;
 		}
 		
 		if (guiOn) {
 			// update graphics
 			SwingUtilities.invokeLater(new Runnable() {
 				
 				@Override
 				public void run() {
 					displayer.updateState();
 				}
 			});
 		}
 	}
 
 	private void endGame() {
 		for (int cID = 0; cID < clients.length; cID++) {
 			sendEvent(cID, clients[cID], new Event(Event.EventType.GameEnd));
 		}
 		// Remote.shutdown();
 		
 		declareWinner();
 		System.exit(0);
 	}
 
 	public void gameLoop() {
 		long delay = 200;
 		long delayPlayer = GamePolicy.playerTotalTime;
 		long period = GamePolicy.roundTime;
 		int connectWaitTime = GamePolicy.connectWaitTime;
 		int initializationWaitTime = GamePolicy.initializationWaitTime;
 		
 		try {
 			Thread.sleep(connectWaitTime);
 		} catch (InterruptedException ex) {
 			logger.fatal("ConnectWait interrupted", ex);
 		}
 
 		if (connectedClients != clients.length) {
 			logger.info("Insufficient number of clients connected. Expected=" + clients.length + 
 					", connected=" + connectedClients + ". Game will end!");
 			System.exit(1);
 		}
 		
 		// initialization period
 		for (int cID = 0; cID < clients.length; cID++) {
 			Object client = clients[cID];
 			sendEvent(cID, client, new Event(Event.EventType.Initialization));
 		}
 		
 		try {
 			Thread.sleep(initializationWaitTime);		// wait for the specified initialization period
 		} catch (InterruptedException ex) {
 			logger.fatal("InitializationWait interrupted", ex);
 		}
 		
 		// mark beginning of game
 		gameStartTime = System.currentTimeMillis() + delay;		// first action will commence after delay milliseconds
 		state.round.startTime = gameStartTime;
 		
 		// player time
 		for (int i = 0; i < clients.length; i++) {
 			final int clientID = i;
 			timer.scheduleAtFixedRate(new TimerTask() {
 
 				//Object client = clients.get(clientID);
 				Object client = clients[clientID];
 
 				@Override
 				public void run() {
 					// replenish player's units with their energy supplies
 					actionEngine.replenishEnergy();
 					
 					// set round status: currentRound and roundStartTime
 					Integer playerID = state.getPlayerIds().get(clientID);
 					PlayerState player = state.playerStates.get(playerID);
 					int currentRound = state.round.currentRound;
 
 					player.round.currentRound = currentRound;
 					player.round.startTime = gameStartTime + 
 						currentRound * GamePolicy.roundTime + clientID * GamePolicy.playerActionTime;
 
 					// then subtract specific energy amount if the player is near some towers
 					actionEngine.doTowerDrain(state, state.getPlayerIds().get(clientID));
 
 					// update the view of the player's own units before the start of the new round
 					actionEngine.updatePlayerSight(state, state.getPlayerIds().get(clientID));
 					
 					// update the view of the player's own towers before the start of the new round
 					actionEngine.updateTowerSight(state, state.getPlayerIds().get(clientID));
 
 					// decrease frozen rounds left for frozen units :)
 					actionEngine.unfreeze(state, state.getPlayerIds().get(clientID));
 
 					// send the NEW ROUND event to current client
 					sendEvent(clientID, client, new Event(Event.EventType.NewRound));
 
 					// send the END ROUND event to all the others
 					for (int cID = 0; cID < clients.length; cID++) {
 						if (cID != clientID) {
 							sendEvent(cID, clients[cID], new Event(Event.EventType.EndRound));
 						}
 					}
 				}
 			}, delay + delayPlayer * i, period);
 		}
 
 		// update time
 		timer.scheduleAtFixedRate(new TimerTask() {
 
 			@Override
 			public void run() {
 				updateGame();
 			}
 		}, delay + period - GamePolicy.updateTime, period);
 
 		// close
 		timer.schedule(new TimerTask() {
 
 			@Override
 			public void run() {
 				timer.cancel();
 				endGame();
 			}
 		}, delay + GamePolicy.roundTime * state.round.noRounds);
 	}
 
 	
 	@Override
 	public synchronized PlayerState process(Transition action) {
 		int clientID = allowedSecret(action.secret);
 		if (clientID == -1) {
 			return null;
 		}
 		
 		Integer playerID = state.getPlayerIds().get(clientID);
 		PlayerState player = state.playerStates.get(playerID);
 		
 		if (action.operator == ActionType.PlayerReady) {
 			TransitionResult res = new TransitionResult(action.id);
 			res.errorType = TransitionResult.TransitionError.NoError;
 			player.response = res;
 			
 			printToGuiLog(player, action);
 			return player;
 		}
 		
 		if (!allowedPlayer(clientID)) {
 			return null;
 		}
 		
 		
 		if (player.round.currentRound < state.round.currentRound) {
 			player.round.currentRound = state.round.currentRound;
 			player.round.startTime = GamePolicy.connectWaitTime + 
 				state.round.currentRound * GamePolicy.roundTime + clientID * GamePolicy.playerActionTime;
 		}
 		
 		player.response = actionEngine.process(player, action);
 		
 		if (player.response.valid()) {
 			logger.info("[srv]Executed action [" + action.operator.name() + "] for client: " + clientID);
 		}
 		else {
 			logger.info("[srv]Failed action [" + action.operator.name() + "] for client: " + clientID);
 		}
 		
 		
 		// update the view of the player's own units after the execution of an action
 		actionEngine.updatePlayerSight(state, playerID);
 		
 		// update the view of the player's own towers after the execution of an action
 		actionEngine.updateTowerSight(state, playerID);
 		
 		return player;
 	}
 
 		
 	private boolean allowedPlayer(int clientID) {
 		synchronized (updateSync) {
 			int factor = clientID % GamePolicy.nrPlayers;
 			long currentTime = System.currentTimeMillis();
 			int currentRound = state.round.currentRound;
 			
 			long diff = currentTime - gameStartTime - currentRound * GamePolicy.roundTime;
 			//System.out.println("diff for allowedPlayer is: " + diff);
 			
 			if (diff >= factor * GamePolicy.playerTotalTime && diff < (factor + 1) * GamePolicy.playerTotalTime) {
 				return true;
 			}
 			
 			return false;
 		}
 	}
 	
 	private int allowedSecret(long secret) {
 		for (int i = 0; i < secrets.length; i++) {
 			if (secret == secrets[i]) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	private long[] readSecrets(String fileName) {
 		long[] secrets = null;
 		
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(fileName));
 			int len = Integer.parseInt(br.readLine());
 			secrets = new long[len];
 			
 			for (int i = 0; i < len; i++) {
 				secrets[i] = Long.parseLong(br.readLine());
 			}
 			
 			br.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 			return null;
 		}
 		
 		return secrets;
 	}
 	
 	private void declareWinner() {
 		//System.out.println("PRINTING RESULTS");
 		
 		class PlayerScore {
 			public PlayerState player;
 			public float score;
 			public PlayerScore(PlayerState player, float score) {
 				this.player = player;
 				this.score = score;
 			}
 			public String toString() {
 				return player.id.toString() + "," +
 						score + "," +
 						player.getKills() + "," +
 						player.getRetaliationKills() + "," +
 						player.getDeadUnits() + "," +
 						player.getPlacedTowers() + "," +
 						player.getSuccessfulTraps() + "," +
 						player.getPlacedTraps() + "," +
 						player.getKillingSprees() + "," +
 						player.getFirstBlood();
 			}
 		}
 		
 		List<Integer> playerIDs = state.getPlayerIds();
 		ArrayList<PlayerScore> scores = new ArrayList<PlayerScore>();
 		
 		for (Integer playerID : playerIDs) {
 			PlayerState ps = state.playerStates.get(playerID);
 			float score = ps.getScore();
 			scores.add(new PlayerScore(ps, score));
 		}
 		
 		Collections.sort(scores, new Comparator<PlayerScore>() {
 			public int compare (PlayerScore a, PlayerScore b) {
 				return Float.compare(b.score, a.score);
 			}
 		});
 		
 		try
 		{
 			BufferedWriter bw = new BufferedWriter(new FileWriter("winner.txt"));
 			Iterator<PlayerScore> it = scores.iterator();
 			logger.info("Scores:");
 			while (it.hasNext()) {
 				PlayerScore ps = it.next();
 				bw.write(ps.toString());
 				bw.newLine();
 				logger.info(ps.toString());
 			}
 			bw.flush();
 			bw.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void printToGuiLog(PlayerState player, Transition action) {
 		if (player.validLastTransition()) {
 			if (action.operator == ActionType.PlayerReady) {
 				for (UnitState u : player.units) {
 					gui_logger.info(state.round.currentRound + " " + action.operator.name() + " " + player.id + " " 
 							+ u.pos.x + " " + u.pos.y + " " + player.gold + " " + u.energy);
 				}
 			}
 		}
 	}
 	
 	public String getServername() {
 		return servername;
 	}
 
 	public int getPortNumber() {
 		return portNumber;
 	}
 
 }
