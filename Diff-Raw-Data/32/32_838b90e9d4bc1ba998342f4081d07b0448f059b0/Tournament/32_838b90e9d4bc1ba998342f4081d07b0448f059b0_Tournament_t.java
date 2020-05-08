 package edu.brown.cs32.MFTG.tournament;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import edu.brown.cs32.MFTG.monopoly.Player;
 import edu.brown.cs32.MFTG.networking.ClientCommunicationException;
 import edu.brown.cs32.MFTG.networking.ClientHandler;
 import edu.brown.cs32.MFTG.networking.ClientLostException;
 import edu.brown.cs32.MFTG.networking.InvalidResponseException;
 import edu.brown.cs32.MFTG.networking.PlayGamesCallable;
 import edu.brown.cs32.MFTG.networking.getPlayerCallable;
 import edu.brown.cs32.MFTG.tournament.Settings.WinningCondition;
 import edu.brown.cs32.MFTG.tournament.data.DataProcessor;
 import edu.brown.cs32.MFTG.tournament.data.GameDataAccumulator;
 import edu.brown.cs32.MFTG.tournament.data.GameDataReport;
 
 public class Tournament implements Runnable{
 	private final List<Integer> _players;
 	private final int _numPlayers;
 	private List<ClientHandler> _clientHandlers;
 	private ServerSocket _socket;
 	private Settings _settings;
 	private ExecutorService _executor;
 	private Map<Integer,Double> _roundWinners;
 	private Random _rand;
 
 	/**
 	 * Creates a tournament with the specified number of players, port and settings
 	 * @param players the heuristics for each player
 	 * @param settings the game settings
 	 * @param port the server port for the tournament
 	 * @throws IOException
 	 */
 	public Tournament(List<Integer> players, Settings settings, int port) throws IOException{
 		if (port <= 1024){
 			throw new IllegalArgumentException("Ports under 1024 are reserved!");
 		}
 		
 		/* initialize everything */
 		_socket = new ServerSocket(port);
 		_clientHandlers = new ArrayList<>();
 		_players = players;
 		_numPlayers = _players.size();
 		_settings = settings;
 		_rand = new Random();
 		_roundWinners = new HashMap<>();
 		resetRoundWinners();
 
 		/* figure out how many AI clients there are and connect them */
 		int numAIPlayers = 0;
 		for(Integer i : players){
 			if(i == 1){ //AIClient id
 				numAIPlayers++;
 			}
 		}
 		_executor = Executors.newFixedThreadPool(numAIPlayers+_players.size());
 
 		AIClient tempClient;
 		for(int i = 0; i < numAIPlayers; i++){
 			tempClient = new AIClient();
 			tempClient.connect(port, "localhost");
 			_executor.execute(tempClient);
 		}
 	}
 
 	/**
 	 * Returns a new balanced player with a specified id
 	 * @param id the id to assign to the new player
 	 */
 	private Player newBalancedPlayer(int id){
 		Player p = new Player(id,"balanced");
 		
 		p.setColorValue("purple", 1.7, 1.2, 1.5, 1.3);
 		p.setColorValue("light blue", 1.2, 1.2, 1.5, 1.3);
 		p.setColorValue("pink", 1.7, 1.2, 1.5, 1.3);
 		p.setColorValue("orange", 1.7, 1.2, 1.5, 1.3);
 		p.setColorValue("red", 1.7, 1.2, 1.5, 1.3);
 		p.setColorValue("yellow", 1.7, 1.2, 1.5, 1.3);
 		p.setColorValue("green", 1.7, 1.2, 1.5, 1.3);
 		p.setColorValue("dark blue", 1.7, 1.2, 1.5, 1.3);
 
 		p.setJailWait(2);
 		p.setJailRich(2);
 		p.setJailPoor(2);
 		p.setMinBuildCash(200);
 		p.setMinBuyCash(150);
 		p.setMinUnmortgageCash(300);
 		p.setTradingFear(1.3);
 		p.setLiquidity(6);
 		
 		return p;
 	}
 	
 	/**
 	 * The main loop, goes through the steps of the game sending instructions to the clients
 	 */
 	public void run() {
 
 		try {
 			getPlayerConnections();
 		} catch (IOException e) {
 			System.out.println("you are fucked");
 			return;
 		}
 		
 		/* initialze variables */
 		int gamesPerModule = (int)Math.ceil(((double)_settings.gamesPerRound)/_numPlayers);
 		List<Player> players = null;
 		List<GameDataReport> data;
 		List<Integer> confirmationIndices;
 
 		/* play numRounds sets of games */
 		for(int roundNum = 0; roundNum < _settings.numRounds; roundNum++){
 
 			/* request a Player from each client */
 			players = getNewPlayers(players);
 
 			/* generate which seeds will be used for integrity validation */
 			confirmationIndices = DataProcessor.generateConfirmationIndices(gamesPerModule, BackendConstants.CONFIRMATION_PERCENTAGE,_rand);
 
 			/* play a round of games */
 			data = playRoundOfGames(players, DataProcessor.generateSeeds(gamesPerModule, _players.size(), confirmationIndices,_rand));
 
 			/* check for cheating */
 			if(data.size() > 0){
 				if(DataProcessor.isCorrupted(data, confirmationIndices)){
 					System.out.println("someone is cheating");
 				}
 				sendEndOfRoundData(accumulateEndOfRoundData(data, roundNum));
 			}
 		}
 	}
 
 	/**
 	 * Waits for _players.size() connections from the clients.
 	 * @throws IOException
 	 */
 	public void getPlayerConnections() throws IOException{
 		int connectionsMade = 0;
 
 		while(connectionsMade < _players.size()){
 			Socket clientConnection = _socket.accept();
 			ClientHandler cHandler = new ClientHandler(clientConnection, connectionsMade,_settings);
 			connectionsMade++;
 			_clientHandlers.add(cHandler);
 			try {
				cHandler.sendIDAndTimeouts();
 			} catch (ClientCommunicationException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Get rid of the old Players and get new Players from each client
 	 * @throws InvalidResponseException 
 	 * @throws ClientLostException 
 	 * @throws ClientCommunicationException 
 	 */
 	private List<Player> getNewPlayers(List<Player> prevPlayers){
 		List<Future<Player>> playerFutures = new ArrayList<>();
 		List<Player> players = new ArrayList<>();
 
 		for(ClientHandler c : _clientHandlers){
 			Callable<Player> worker = new getPlayerCallable(c);
 			Future<Player> future = _executor.submit(worker);
 			playerFutures.add(future);
 		}
 
 		for (int i = 0; i < playerFutures.size(); i++){
 			try {
 				players.add(playerFutures.get(i).get());
 			} catch (InterruptedException | ExecutionException e) {
 				
 				if (e.getCause() instanceof SocketTimeoutException || e.getCause() instanceof SocketException){
 					System.out.println("caught timeout stuff");
 					_clientHandlers.get(i).setDoubleRead();
 				} else {
 					e.getCause().printStackTrace(); // will be removed, just for debugging
 				}
 				
 				if (prevPlayers != null){
 					players.add(prevPlayers.get(i));
 					
 					sendErrorMessage("Unable to retrieve heuristic information from client " + _clientHandlers.get(i)._id + 
 							". Reusing old heuristics");
 
 				} else {
 					players.add(newBalancedPlayer(i));
 					
 					sendErrorMessage("Unable to retrieve heuristic information from client " + _clientHandlers.get(i)._id + 
 							". Using default balanced heuristics");
 				}
 			}
 		}
 		return players;
 	}
 
 	/**
 	 * Tells the client handlers to send the instruction to play games to the clients
 	 * then waits for the clients to finish and hand back data
 	 * @param players the player heuristics
 	 * @param seeds the seed values for all of the games
 	 * @return the data recieved from the clients
 	 */
 	private List<GameDataReport> playRoundOfGames(List<Player> players, List<List<Long>> seeds) {
 		List<Future<GameDataReport>> gameDataFutures = new ArrayList<>();
 		List<GameDataReport> gameData = new ArrayList<>();
 
 		if (_clientHandlers.size() != seeds.size()){
 			System.out.println("seed size doesn't match num clients");
 		}
 
 		/* tell the clients to play games */
 		for (int i = 0; i < _clientHandlers.size(); i++){
 			Callable<GameDataReport> worker = new PlayGamesCallable(_clientHandlers.get(i), players, seeds.get(i), _settings);
 			Future<GameDataReport> future = _executor.submit(worker);
 			gameDataFutures.add(future);
 		}
 
 		/* wait for the clients to finish playing games */
 		int numFails = 0;
 		for (int i = 0; i < gameDataFutures.size(); i++){
 			try {
 				Future<GameDataReport> future = gameDataFutures.get(i);
 				future.get();
 				gameData.add(gameDataFutures.get(i).get());
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 				numFails++;
 			} catch (ExecutionException e) {
 				
 				if (e.getCause() instanceof SocketTimeoutException || e.getCause() instanceof SocketException){
 					
 					System.out.println("removing player from the game");
 					
 					sendErrorMessage("Connection with client " + _clientHandlers.get(i)._id + " timed out. " +
 							"Removing this client from the game");
 					_clientHandlers.remove(i);
 					players.remove(i);
 					gameDataFutures.remove(i);
 					i--;
 					
 					if (_clientHandlers.size() < 2){
 						sendErrorMessage("Not enough clients remaining to play a game. The game lobby will now close.");
 						shutDown();
 					}
 				} else {
 					e.getCause().printStackTrace(); // will be removed in final version
 					numFails++;
 				}
 			}
 		}
 
 		if (numFails > 0)
 			sendErrorMessage("Unable to use game data from " + numFails + " of the " + _clientHandlers.size() + " clients");
 
 		return gameData;
 	}
 	
 	
 	/**
 	 * Tell the client handlers to send the end of round data back to the clients
 	 * @param aggregatedData the data to send
 	 */
 	private void sendEndOfRoundData(GameDataReport aggregatedData) {
 		for(ClientHandler c : _clientHandlers){
 			try {
 				c.setGameData(aggregatedData);
 			} catch (IOException e) {
 				c.sendErrorMessage("Unable to display game data");
 			}
 		}
 	}
 	
 	/**
 	 * Send an error message to the clients
 	 * @param errorMessage
 	 */
 	private void sendErrorMessage(String errorMessage){
 		for (ClientHandler c : _clientHandlers){
 			c.sendErrorMessage(errorMessage);
 		}
 	}
 	
 	/**
 	 * To be called immediately before the program shuts down
 	 */
 	private void shutDown(){
 		// TODO implement
 	}
 	
 	/**
 	 * Combine all of the data after a set of games is completed
 	 * @param data the data to combine
 	 * @param roundNum the current set number
 	 * @return
 	 */
 	private GameDataReport accumulateEndOfRoundData(List<GameDataReport> data, int roundNum){
 		/* combine all of the data */
 		GameDataAccumulator[] accumulators = new GameDataAccumulator[data.size()];
 		for(int j = 0; j < data.size(); j++){
 			accumulators[j] = data.get(j).toGameDataAccumulator();
 		}
 		GameDataAccumulator combined = DataProcessor.combineAccumulators(accumulators);
 		
 		/* figure out and set the round winner */
 		int winnerID = combined.getPlayerWithMostGamesWon(_settings.winType).getLeft();
 		_roundWinners.put(winnerID, _roundWinners.get(winnerID) + 1);
 		
 		if(_settings.winType == WinningCondition.LAST_SET_WON && roundNum == _settings.numRounds-1){
 			resetRoundWinners();
 			winnerID = combined.getPlayerWithMostGamesWon(_settings.winType).getLeft();
 			_roundWinners.put(winnerID, _roundWinners.get(winnerID) + 1);
 		}
 		
 		combined.playerWins = _roundWinners;
 
 		/* determine whether match is over */
 		if(roundNum == _settings.numRounds-1){
 			combined.matchIsOver = true;
 		}
 		
 		return combined.toGameDataReport();
 	}
 
 	/**
 	 * Sets all the player win values to 0
 	 */
 	private void resetRoundWinners(){
 		for(int i = -1; i < BackendConstants.MAX_NUM_PLAYERS; i++){ //-1 is a tie
 			_roundWinners.put(i, 0.);
 		}
 	}
 }
