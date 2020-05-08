 package edu.brown.cs32.MFTG.tournament;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JavaType;
 import com.fasterxml.jackson.databind.JsonMappingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 import edu.brown.cs32.MFTG.monopoly.GameData;
 import edu.brown.cs32.MFTG.monopoly.Player;
 import edu.brown.cs32.MFTG.networking.ClientRequestContainer;
 import edu.brown.cs32.MFTG.networking.ClientRequestContainer.Method;
 import edu.brown.cs32.MFTG.networking.InvalidRequestException;
 import edu.brown.cs32.MFTG.tournament.data.GameDataAccumulator;
 import edu.brown.cs32.MFTG.tournament.data.GameDataReport;
 import edu.brown.cs32.MFTG.tournament.data.PlayerWealthDataReport;
 import edu.brown.cs32.MFTG.tournament.data.PropertyDataReport;
 import edu.brown.cs32.MFTG.tournament.data.TimeStampReport;
 
 public abstract class Client implements Runnable{
 
 	/* Networking variables */
 	protected Socket _server;
 	protected BufferedReader _input;
 	protected BufferedWriter _output;
 	protected final ObjectMapper _oMapper;
 	protected Method _lastRequest;
 	protected int _port;
 	protected String _host;
 
 	/* Module variables */
 	protected int _nextDisplaySize;
 	protected int _numGamesPlayed;
 	protected GameDataAccumulator _data;
 	protected AtomicInteger _numThreadsDone;
 	protected ExecutorService _pool;
 	protected int _id;
 
 	public Client() {
 		_oMapper = new ObjectMapper();
 		_lastRequest = Method.DISPLAYGAMEDATA;
 
 		_pool = Executors.newFixedThreadPool(BackendConstants.NUM_THREADS);
 		_numThreadsDone = new AtomicInteger(0);
 	}
 	
 	public void connect(int port, String host){
 		_port = port;
 		_host = host;
 	}
 	
 	public abstract void run();
 
 	protected abstract void respondToDisplayError(ClientRequestContainer request);
 	
 	protected abstract void respondToDisplayData(ClientRequestContainer request) throws JsonParseException, JsonMappingException, IOException;
 	
 	public abstract void startGetPlayer(int time);
 	
 	public abstract Player finishGetPlayer();
 	
 	protected abstract void setPlayerNames(List<Player> players);
 	
 	public abstract void addGameData(GameData gameData);
 
 	/**
 	 * Writes a request to the output stream
 	 * @param request the request to write
 	 * @throws IOException
 	 */
 	protected void write(ClientRequestContainer request) throws IOException{
 		String json = _oMapper.writeValueAsString(request);
 		_output.write(json);
 		_output.write("\n");
 		_output.flush();
 	}
 
 	/**
 	 * Reads a request from the client
 	 * @return
 	 * @throws IOException
 	 */
 	protected ClientRequestContainer readRequest() throws IOException{
 		String json = _input.readLine();
 		return _oMapper.readValue(json, ClientRequestContainer.class);
 	}
 
 	/**
 	 * @throws IOException
 	 * @throws JsonMappingException 
 	 * @throws JsonParseException 
 	 */
 	protected void handleRequest() throws InvalidRequestException, IOException{
 		ClientRequestContainer request = readRequest();
 
 		Method method = request._method;
 		
 		if (method == null)
 			throw new InvalidRequestException();
 
 		if (method == Method.GETPLAYER){
 			if (_lastRequest == Method.DISPLAYGAMEDATA){ 
 				respondToGetPlayer(request);
 				_lastRequest = Method.GETPLAYER;
 			} else {
 				throw new InvalidRequestException();
 			}
 
 		} else if (method == Method.PLAYGAMES){
 			if (_lastRequest == Method.GETPLAYER){
 				respondToPlayGames(request);
 				_lastRequest = Method.PLAYGAMES;
 			} else {
 				throw new InvalidRequestException();
 			}
 
 		} else if (method == Method.DISPLAYGAMEDATA){
 			if (_lastRequest == Method.PLAYGAMES){
 				respondToDisplayData(request);
 				_lastRequest = Method.DISPLAYGAMEDATA;
 			} else {
 				throw new InvalidRequestException();
 			}
 		
 		} else if (method == Method.DISPLAYERROR){
 			respondToDisplayError(request);
 		
 		} else {
 			assert(false);
 		}
 	}
 
 
 	protected int respondToSendID() throws IOException, InvalidRequestException{
 
 		ClientRequestContainer request = readRequest();
 
 		if (request == null || request._method != Method.SENDID)
 			throw new InvalidRequestException();
 
 
 		List<String> arguments = request._arguments;
 
 		if (arguments == null){
 			// throw an error
 		} else if (arguments.size() < 1){
 			// throw a different error
 		}
 
 		try {
 			return Integer.parseInt(arguments.get(0));
 		} catch (NumberFormatException e){
 			throw new InvalidRequestException();
 		}
 	}
 	
 	/**
 	 * Responds to a request sent over the socket for player data, and sends the player information back in response
 	 * @param the request which is being responded to
 	 * @throws JsonParseException
 	 * @throws JsonMappingException
 	 * @throws IOException
 	 */
 	private void respondToGetPlayer(ClientRequestContainer request) {
 		List<String> arguments = request._arguments;
 		
 		if (arguments == null){
 			// throw an error
 		} else if (arguments.size() < 1){
 			// throw a different error
 		}
 		int time = Integer.parseInt(arguments.get(0));
 		
 		startGetPlayer(time);
 	}
 		
 	public void finishRespondToGetPlayer(){
 		Player p = finishGetPlayer();
 
 		String playerString;
 		
 		assert(!(p.getPropertyValues().keySet().contains(null)));
 		assert(!(p.getColorValues().keySet().contains(null)));
 		
 		try {
 			for(Entry<String, Double[]> d : p.getColorValues().entrySet()){
 			//	System.out.println(d);
 			}
 			for(Entry<String, Integer> d : p.getPropertyValues().entrySet()){
 			//	System.out.println(d);
 			}
 			
 			playerString = _oMapper.writeValueAsString(p);
 		} catch (JsonProcessingException e) {
 			e.printStackTrace();
 			assert(false);
 			return;
 		}
 		
 		ClientRequestContainer response = new ClientRequestContainer(Method.SENDPLAYER, Arrays.asList(playerString));
 		try {
 			write(response);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	/**
 	 * Responds to a request sent over the socket to play games, and sends the game data back in response
 	 * @param the request which is being responded to
 	 * @throws JsonParseException
 	 * @throws JsonMappingException
 	 * @throws IOException
 	 */
 	private void respondToPlayGames(ClientRequestContainer request) throws JsonParseException, JsonMappingException, IOException{
 		List<String> arguments = request._arguments;
 
 		if (arguments == null){
 			// throw a different different error
 		} else if (arguments.size() < 3){
 			// you get the idea
 		}
 		
 		JavaType listOfPlayers = _oMapper.getTypeFactory().constructCollectionType(List.class, Player.class);
 		JavaType listOfSeeds = _oMapper.getTypeFactory().constructCollectionType(List.class, Long.class);
 		List<Player> players = _oMapper.readValue(arguments.get(0), listOfPlayers);
 		List<Long> seeds = _oMapper.readValue(arguments.get(1), listOfSeeds);
 		Settings settings = _oMapper.readValue(arguments.get(2), Settings.class);
 		
 		GameDataReport gameData = playGames(players, seeds, settings);
 		String gameDataJson = _oMapper.writeValueAsString(gameData);
 		
 		ClientRequestContainer response = new ClientRequestContainer(Method.SENDGAMEDATA, Arrays.asList(gameDataJson));
 		write(response);
 	}
 	
 	/**
	 * Writes a list of of gameData to the server
	 * @param gameData
	 * @throws IOException
	 */
//	private void sendGameResponse(List<GameData> gameData) throws IOException{
//		for (GameData g : gameData){
//			System.out.println("writing g as a string");
//			String gameDataString = _oMapper.writeValueAsString(g);
//			System.out.println("written");
//			_output.write(gameDataString);
//			_output.write("\n");
//		}
//		_output.write("DONE\n");
//		_output.flush();
//		System.out.println("entire response written");
//	}
	
	/**
 	 * Creates a new tournament and runs it in a new thread
 	 * @param players the player heuristics from all of the clients
 	 * @param settings the game settings
 	 * @param port the server port
 	 */
 	public void launchTournament(List<Integer> players, Settings settings, int port){
 		try {
 			_pool.execute((new Tournament(players, settings, port)));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Play seeds.size games in separate threads
 	 * @param players the player heuristics
 	 * @param seeds the game seeds
 	 * @param settings the game settings
 	 * @return the data collected from the games
 	 */
 	public GameDataReport playGames(List<Player> players, List<Long> seeds, Settings settings){
 		/* reset variables */
 		_numGamesPlayed = 0;
 		_data = null;
 		_nextDisplaySize = BackendConstants.DATA_PACKET_SIZE;
 		_numThreadsDone.set(0);
 		setPlayerNames(players);
 		
 		/* launch the games in separate threads */
 		GameRunnerFactory gameRunnerFactory = new GameRunnerFactory(_numThreadsDone, this, BackendConstants.MAX_NUM_TURNS,
 				settings.freeParking,settings.doubleOnGo,settings.auctions,players.toArray(new Player[players.size()]));
 		
 		for(int i = 0; i < seeds.size(); i++){
 			_pool.execute(gameRunnerFactory.build(i,seeds.get(i)));
 		}
 		
 		/* wait for the games to finish */
 		synchronized (this){
 			while(_numThreadsDone.get() < seeds.size()){
 				try{
 					this.wait();
 				} catch (InterruptedException e){}
 			}
 		}
 		
 		return _data.toGameDataReport();
 	}
 
 	/**
 	 * Find the player property data specific to this player
 	 * @param allPlayerPropertyData the data for all of the players
 	 * @return the data specific to this player
 	 */
 	protected Map<String, PropertyDataReport> getPlayerPropertyData(Map<String,List<PropertyDataReport>> allPlayerPropertyData){
 		Map<String, PropertyDataReport> playerPropertyData = new HashMap<>();
 		for(List<PropertyDataReport> l : allPlayerPropertyData.values()){
 			for(PropertyDataReport d : l){
 				if(d.ownerID == _id){
 					playerPropertyData.put(d.propertyName, d);
 					break;
 				}
 			}
 		}
 		return playerPropertyData;
 	}
 
 	/**
 	 * Find the player wealth data specific to this player
 	 * @param timeStamps the wealth data over the game for all of the players
 	 * @return the data specific to this player
 	 */
 	protected List<PlayerWealthDataReport> getPlayerWealthData(List<TimeStampReport> timeStamps){
 		List<PlayerWealthDataReport> playerWealthData = new ArrayList<>();
 		for(TimeStampReport t : timeStamps){
 			playerWealthData.add(t.wealthData.get(_id));
 		}
 		return playerWealthData;
 	}
 }
