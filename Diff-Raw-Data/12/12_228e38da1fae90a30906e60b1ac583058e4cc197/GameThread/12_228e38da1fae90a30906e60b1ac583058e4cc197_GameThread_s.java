 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.atomic.AtomicInteger;
 import org.json.*;
 import java.io.IOException;
 
 public class GameThread {
     ConcurrentLinkedQueue<AIConnection> globalClients;
     int minUsers;
     int gameTimeoutSeconds;
     int roundTimeSeconds;
     World world;
     PlayerSelector playerSelector;
     AtomicInteger readyUsers = new AtomicInteger(0); // for loadouts
     GraphicsContainer graphicsContainer = null;
     public GameThread(ConcurrentLinkedQueue<AIConnection> globalClientsArg,
 		      int minUsersArg, int gameTimeoutSecondsArg, int roundTimeSecondsArg,
 		      World worldArg, GraphicsContainer graphicsContainerArg){
 	graphicsContainer = graphicsContainerArg;
 	world = worldArg;
 	globalClients = globalClientsArg;
 	minUsers = minUsersArg;
 	gameTimeoutSeconds = gameTimeoutSecondsArg;
 	roundTimeSeconds = roundTimeSecondsArg;
     }
     public void run(int gameSecondsTimeout){
 	try {
 	    Thread.sleep(500);
 	}
 	catch(InterruptedException e){}
 	System.out.println("[GAMETHRD] waiting for graphics engine to connect");
 	while(true){
 	    try {
 		Thread.sleep(100);
 	    } catch (InterruptedException e){}
 	    if(graphicsContainer.get() != null){
 		break;
 	    }
 	}
 	System.out.println("[GAMETHRD] got graphics engine connection");
 	System.out.println("[GAMETHRD] waiting for " + minUsers + " users to connect");
 	int waitIteration = 0;
 	while(true){
 	    waitIteration++;
 	    try {
 		Thread.sleep(500);
 	    }
 	    catch(InterruptedException e){}
 	    if(globalClients.size() == minUsers){
 		System.out.println("[GAMETHRD] Got " + minUsers + " users, starting the round");
 		break;
 	    }
 	}
 	System.out.println("[GAMETHRD] Initializing game");
 	gameMainloop();
     }
     public void gameMainloop(){
 	System.out.println("All clients connected.");
 	Util.pressEnterToContinue("Press enter to randomize spawns and send the initial gamestate");
 	initializeBoardWithPlayers();
 	playerSelector = new PlayerSelector(globalClients);
 	System.out.println("[GAMETHRD] Sending initial gamestate");
 	sendGamestate(0);
 	// we just loop until everyone has selected a loadout.
 	while(true){
 	    boolean allAreReady = true;
 	    for(AIConnection conn: globalClients){
 		if(!conn.gotLoadout.get()){
 		    System.out.println("Waiting for loadout from " + conn.username);
 		    allAreReady = false;
 		}
 	    }
 	    if(allAreReady){
 		break;
 	    }
 	    letClientsThink();
 	    letClientsThink();
 	    letClientsThink();
 	}
 	sendDeadline();
 	graphicsContainer.get().sendEndActions();
 	graphicsContainer.get().waitForGraphics();
 	System.out.println("All clients have sent a loadout");
 	Util.pressEnterToContinue("Press enter to start the game");
 	long startTime = System.nanoTime();
 	long gtsAsLong = gameTimeoutSeconds;
 	int roundNumber = 1;
 	while(true){
 	    long roundStartTime = System.nanoTime();
 	    if((roundStartTime - startTime) > gtsAsLong*1000000000){
 		System.out.println("[GAMETHRD] Time over!");
 		System.exit(0);
 	    }
 	    System.out.println("[GAMETHRD] Sending gamestate...");
 	    // TODO: clear out message queue of the player whos turn it is
 	    AIConnection currentPlayer = sendGamestate(roundNumber);
 	    
 	    System.out.println("sent gamestate. Current player: " + currentPlayer.username);
 	    letClientsThink();
 	    sendDeadline();
 	    System.out.println("[GAMETHRD] Deadline! Processing actions...");
 	    JSONObject first = currentPlayer.getNextMessage();
 	    JSONObject second = currentPlayer.getNextMessage();
 	    JSONObject third = currentPlayer.getNextMessage();
 	    int validAction = 0;
 	    if(letPlayerPerformAction(first, currentPlayer)){
 		broadcastAction(first, currentPlayer);
 		validAction++;
 	    }
 	    else {System.out.println("Action was invalid.");}
 	    if(letPlayerPerformAction(second, currentPlayer)){
 		broadcastAction(second, currentPlayer);
 		validAction++;
 	    }
 	    else {System.out.println("Action was invalid.");}
 	    if(letPlayerPerformAction(third, currentPlayer)){
 		broadcastAction(third, currentPlayer);
 		validAction++;
 	    }
 	    else {System.out.println("Action was invalid.");}
 	    System.out.println("[GAMETHRD] player performed " + validAction + " valid actions");
 	    graphicsContainer.get().sendEndActions();
 	    // simulate the GUI working -- we will have to wait for it later
 	    graphicsContainer.get().waitForGraphics();
 	    // end GUI working
 	    
 	    roundNumber++;
 	}
 	
     }
     private void broadcastAction(JSONObject action, AIConnection playerWhoPerformedTheAction){
 	// TODO: AIs can use this to inject extranous JSON fields into other peoples
 	// receiver stream. Write a function that sanitizes the attribute first before
 	// sending them off again.
 	System.out.println("Action was valid, re-broadcasting (FIXME)");
 	try {
 	    action.put("from", playerWhoPerformedTheAction.username);
 	}
 	catch (JSONException e){
 	}
 	try {
 	    graphicsContainer.get().sendMessage(action);
 	}
 	catch (IOException e) {
 	    System.out.println("Warning: failed to broadcast action to graphics");
 	}
 	for(AIConnection player: globalClients){
 	    try {
 		player.sendMessage(action);
 	    }
 	    catch (IOException e){
 		System.out.println("Warning: Failed to broadcast action to " + player.username);
 	    }
 	}
     }
     private boolean letPlayerPerformAction(JSONObject action, AIConnection currentPlayer){
 	// TODO: switch on the type of action here
 	if(action != null){
 	    return currentPlayer.doMove(action);
 	}
 	return false;
     }
     public void letClientsThink(){
 	try {
	    Thread.sleep(roundTimeSeconds*50);
 	}
 	catch (InterruptedException e){
	    System.out.println("INTTERUPTED!");
 	}
     }
     public AIConnection sendGamestate(int roundNumber){
 	// TODO: visualization needs to be integrated here
 	String matrix[][] = world.returnAsRowMajorMatrix();
 	AIConnection playerTurnOrder[] = playerSelector.getListInTurnOrderAndMoveToNextTurn();
 	if(roundNumber != 0){
 	    playerTurnOrder[0].clearAllMessages();
 	}
 	graphicsContainer.get().sendGamestate(roundNumber, world.dimension, matrix, playerTurnOrder);
 	for(AIConnection client: globalClients){
 	    client.sendGamestate(roundNumber, world.dimension, matrix, playerTurnOrder);
 	}
 	return playerTurnOrder[0];
     }
     public void sendDeadline(){
 	graphicsContainer.get().sendDeadline();
 	for(AIConnection client: globalClients){
 	    client.sendDeadline();
 	}
     }
     public void initializeBoardWithPlayers(){
 	// Associate AIs with players
 	for(AIConnection client: globalClients){
 	    Tile spawn = world.getRandomSpawnpoint();
 	    client.setSpawnpoint(spawn);
 	}
     }
 }
