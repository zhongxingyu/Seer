 package edu.brown.cs32.MFTG.tournament;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 
 import edu.brown.cs32.MFTG.monopoly.GameData;
 import edu.brown.cs32.MFTG.monopoly.Player;
 import edu.brown.cs32.MFTG.networking.ClientCommunicationException;
 import edu.brown.cs32.MFTG.networking.ClientHandler;
 import edu.brown.cs32.MFTG.networking.ClientLostException;
 import edu.brown.cs32.MFTG.networking.InvalidResponseException;
 import edu.brown.cs32.MFTG.networking.PlayGamesCallable;
 import edu.brown.cs32.MFTG.networking.getPlayerCallable;
 import edu.brown.cs32.MFTG.tournament.Settings.Turns;
 import edu.brown.cs32.MFTG.tournament.Settings.WinningCondition;
 import edu.brown.cs32.MFTG.tournament.data.DataProcessor;
 import edu.brown.cs32.MFTG.tournament.data.GameDataReport;
 
 public class Tournament implements Runnable{
 	private final int _numPlayers;
 	
 	public static final double CONFIRMATION_PERCENTAGE = 0.1; //confirm 10% of games
 	public static final int NUM_DATA_POINTS = 100;
 	private List<ClientHandler> _clientHandlers;
 	private ServerSocket _socket;
 	private Settings _settings;
 	private ExecutorService _executor; 
 	/**
 	 * Creates a tournament for the specified number of players and games
 	 * @param numPlayers
 	 * @param numGames
 	 * @throws IOException 
 	 */
 	public Tournament(int numPlayers, Settings settings, int port) throws IOException{
 		if (port <= 1024){
 			throw new IllegalArgumentException("Ports under 1024 are reserved!");
 		}
 		
 		_numPlayers = numPlayers;
 		_socket = new ServerSocket(port);
 		_clientHandlers = new ArrayList<>();
 		_settings = settings;
 		_executor = Executors.newFixedThreadPool(numPlayers);
 	}
 	
 	public void run() {
 		try {
 			getPlayerConnections();
 		} catch (IOException e) {
 			System.out.println("you are fucked");
 			return;
 		}
 		
 		int gamesPerModule = (int)Math.ceil(_settings.getNumGamesPerRound()/_numPlayers);
 		
 		List<Player> players = null;
 		List<List<GameData>> data;
 		List<Integer> confirmationIndices;
 		
 		for(int i = 0; i < _settings.getNumRounds(); i++){			
 			// request a Player from each client
 			List<Player> newPlayers = getNewPlayers();
 			if (newPlayers != null)
 				players = newPlayers;
 			else 
 				continue;
 			
 			// generate which seeds will be used for integrity validation
 			confirmationIndices = DataProcessor.generateConfirmationIndices(gamesPerModule, CONFIRMATION_PERCENTAGE);
 			
 			// play a round of games
			data = playRoundOfGames(players, DataProcessor.generateSeeds(gamesPerModule, players.size(), confirmationIndices));
 
 			// make sure nobody cheated
 			if(DataProcessor.isCorrupted(data, confirmationIndices)){
 				//System.out.println("Tournament : WOOHOO ... SOMEONE IS CHEATING!!!!!"); //TODO change this
 				//TODO FIGURE THIS OUT FRANCES!!!!!!!!!!!!!!!
 			}
 			
 			List<GameData> dataToSend = new ArrayList<>();
 			for(List<GameData> d : data){
 				dataToSend.addAll(d);
 			}
 			
 			sendEndOfRoundData(DataProcessor.aggregate(dataToSend, NUM_DATA_POINTS));
 		}
 		sendEndOfGameData();
 	}
 	
 	/**
 	 * Takes in a throwable that was received from a future and rethrows it as the appropriate method
 	 * @param cause the throwable to convert to a thrown Exception
 	 * @throws ClientLostException
 	 * @throws InvalidResponseException
 	 * @throws ClientCommunicationException
 	 */
 	private void handlePlayerExecutionException(Throwable cause, int culprit){
 		if (cause instanceof ClientLostException){
 			_clientHandlers.remove(culprit);
 			sendErrorMessage("Lost connection to client " + culprit +". Ejecting client from game and " +
 							 " advancing to the next round");
 		} else if (cause instanceof InvalidResponseException || cause instanceof ClientCommunicationException){
 			sendErrorMessage("Unable to retrieve heuristic information from client. Reusing old heuristics and " +
 							 "advancing to the next round");
 		} else {
 			sendErrorMessage("An error has occured retrieving heuristic information. Advancing to the next round");
 		}
 	}
 	
 	/**
 	 * Get rid of the old Players and get new Players from each client
 	 * @throws InvalidResponseException 
 	 * @throws ClientLostException 
 	 * @throws ClientCommunicationException 
 	 */
 	private List<Player> getNewPlayers(){
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
 			} catch (InterruptedException e) {
 				sendErrorMessage("Unable to retrieve heuristic information from client " + i + 
 						 ". Reusing old heuristics");
 				return null;
 			} catch (ExecutionException e) {
 				return null;
 			}
 		}
 		
 		
 		return players;
 	}
 	
 	private void handlePlayGamesException (Throwable cause, int culprit){
 		if (cause instanceof ClientLostException){
 			_clientHandlers.remove(culprit);
 			sendErrorMessage("Lost connection to client " + culprit +". Ejecting client from game");
 		} else if (cause instanceof InvalidResponseException || cause instanceof ClientCommunicationException){
 			sendErrorMessage("Unable to retrieve game data from client " + culprit + ". Reusing old heuristics");
 		}
 	}
 	
 	/**
 	 * Splits the games between each of the clients and has them play
 	 * rounds to next lowest multiple of # of clients
 	 * @param players the players who will be playing the game
 	 * @param seeds the seeds which will be used to play the games
 	 * @return the GameData from the games played
 	 * @throws ClientCommunicationException 
 	 * @throws InvalidResponseException 
 	 * @throws ClientLostException 
 	 */
 	private List<List<GameData>> playRoundOfGames(List<Player> players, List<List<Long>> seeds) {
 		List<Future<List<GameData>>> gameDataFutures = new ArrayList<>();
 		List<List<GameData>> gameData = new ArrayList<>();
 		
 		if (_clientHandlers.size() != seeds.get(0).size()){
 			// throw an error or something
 			System.out.println("seed size doesn't match num clients");
 			System.out.println("seeds size " + seeds.size() + " clients size " + _clientHandlers.size());
 		}
 		
 		for (int i = 0; i < _clientHandlers.size(); i++){
 			Callable<List<GameData>> worker = new PlayGamesCallable(_clientHandlers.get(i), players, seeds.get(i), _settings);
 			Future<List<GameData>> future = _executor.submit(worker);
 			gameDataFutures.add(future);
 		}
 		
 		int numFails = 0;
 		
 		for (int i = 0; i < gameDataFutures.size(); i++){
 			try {
 				gameData.add(gameDataFutures.get(i).get());
 			} catch (InterruptedException e) {
 				System.out.println("interrupted!");
 				numFails++;
 			} catch (ExecutionException e) {
 				System.out.println("execution exception");
 				numFails++;
 			}
 		}
 		
 		if (numFails > 0)
 			sendErrorMessage("Unable to use game data from " + numFails + " of the " + _clientHandlers.size() + " clients");
 		
 		return gameData;
 	}
 
 	private void sendEndOfRoundData(GameDataReport aggregatedData) {
 		for(ClientHandler c : _clientHandlers){
 			try {
 				c.setGameData(aggregatedData);
 			} catch (ClientCommunicationException e) {
 				c.sendErrorMessage("Unable to display game data");
 			}
 		}
 	}
 	
 	private void sendErrorMessage(String errorMessage){
 		for (ClientHandler c : _clientHandlers){
 			c.sendErrorMessage(errorMessage);
 		}
 	}
 	
 	private void sendEndOfGameData(){
 		//TODO implement
 	}
 
 	/**
 	 * Adds _numPlayers connections to the client handler list. This method does not return
 	 * until after numPlayers connections are made over the socket
 	 * @param numPlayers
 	 * @throws IOException
 	 */
 	public void getPlayerConnections() throws IOException{
 		int connectionsMade = 0;
 		
 		while(connectionsMade < _numPlayers){
 			Socket clientConnection = _socket.accept();
 			connectionsMade++;
 			ClientHandler cHandler = new ClientHandler(clientConnection, connectionsMade);
 			_clientHandlers.add(cHandler);
 			try {
 				cHandler.sendID();
 			} catch (ClientCommunicationException e) {
 				// TODO do something?
 			}
 		}
 	}
 }
