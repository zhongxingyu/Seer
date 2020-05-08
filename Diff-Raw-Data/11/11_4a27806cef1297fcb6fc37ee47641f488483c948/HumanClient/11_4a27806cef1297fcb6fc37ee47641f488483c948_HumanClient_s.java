 package edu.brown.cs32.MFTG.tournament;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.swing.JOptionPane;
 import javax.swing.Timer;
 
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.databind.JsonMappingException;
 
 import edu.brown.cs32.MFTG.gui.MonopolyGui;
 import edu.brown.cs32.MFTG.monopoly.GameData;
 import edu.brown.cs32.MFTG.monopoly.Player;
 import edu.brown.cs32.MFTG.networking.ClientRequestContainer;
 import edu.brown.cs32.MFTG.networking.InvalidRequestException;
 import edu.brown.cs32.MFTG.tournament.data.DataProcessor;
 import edu.brown.cs32.MFTG.tournament.data.GameDataAccumulator;
 import edu.brown.cs32.MFTG.tournament.data.GameDataReport;
 
 public class HumanClient extends Client{
 
 	private MonopolyGui _gui;
 	private ExecutorService _executor = Executors.newCachedThreadPool();
 	private Timer _timer;
 	private Map<Integer,String> _playerNames;
 
 	public HumanClient(boolean music){
 		super();
 		_gui = new MonopolyGui(this, music);
 		_playerNames = new HashMap<>();
 	}
 	
 	/**
 	 * Connects the Client to a socket
 	 * @throws IOException
 	 */
 	public void run(){
 		try {
 			_server = new Socket(_host, _port);
 			_input = new BufferedReader(new InputStreamReader(_server.getInputStream()));
 			_output = new BufferedWriter(new OutputStreamWriter(_server.getOutputStream()));
 		} catch (UnknownHostException e) {
 			displayMessage("Unknown host. Unable to connect to server :( WHAT THE FUCK, MAN!!!");
 			sayGoodbye(false);
 			return;
 		} catch (IOException e) {
 			displayMessage("Unable to connect to server :(");
 			sayGoodbye(false);
 			return;
 		}
 		try {
 			respondToSendConstants();
 			_gui.createBoard(_id, _gui.getCurrentProfile(), this);
 			if(_gui.getUserMusic())_gui.playNextInGameSong();
 		} catch (IOException | InvalidRequestException e1) {
 			displayMessage("Unable to retrieve a unique ID from the server :(");
 			sayGoodbye(false);
 			return;
 		}
 		Callable<Void> worker = new RequestCallable(this);
 		_executor.submit(worker);
 	}
 
 	/***************Networking Methods*************************/
 
 	/**
 	 * 
 	 * @param the request which is being responded to
 	 * @throws InvalidRequestException 
 	 */
 	protected void respondToDisplayError(ClientRequestContainer request) throws InvalidRequestException{
 		if (request == null)
 			throw new InvalidRequestException("Null request");
 		
 		List<String> arguments = request._arguments;
 
 		if (arguments == null || arguments.size() < 1){
 			throw new InvalidRequestException("Wrong number of arguments");
 		}
 
 		displayMessage(arguments.get(0));
 	}
 
 	/**
 	 * Responds to a request sent over the server to display game data by displaying the data received
 	 * @param the request which is being responded to
 	 * @throws JsonParseException
 	 * @throws JsonMappingException
 	 * @throws IOException
 	 */
 	protected void respondToDisplayData(ClientRequestContainer request) throws IOException, InvalidRequestException{
 		if (request == null)
 			throw new InvalidRequestException("Null request");
 		
 		List<String> arguments = request._arguments;
 
 		if (arguments == null || arguments.size() < 1){
 			throw new InvalidRequestException("Wrong number of arguments");
 		}
 
 		GameDataReport gameDataReport = _oMapper.readValue(arguments.get(0), GameDataReport.class);
 
 		displayGameData(gameDataReport);
 	}
 
 	/*******************************************************/
 
 	/**
 	 * Returns the user to the welcome screen  
 	 */
 	synchronized void returnToWelcomeScreen(){
 		sayGoodbye(true);
 		_gui.switchPanels("greet");
 	}
 	
 	/**
 	 * Set and display the combined GameData at the end of a set
 	 * @param combinedData
 	 */
 	public void displayGameData(GameDataReport combinedData) {
 		/* update records */
 		_gui.getCurrentProfile().getRecord().addSet(combinedData.getPlayerWithMostWins() == _id);
 		for(Integer i : combinedData._winList.values()){
 			_gui.getCurrentProfile().getRecord().addGame(i == _id);
 		}
 		
 		/* take care of end of game business */
 		if(combinedData._matchIsOver){
 			finishMatch(combinedData);
 		}
 		
 		/* update the board */
 		sendDataToGui(combinedData);
 		_gui.getBoard().setWinnerData(combinedData._playerWins, _playerNames);
 	}
 	
 	/**
 	 * Called by the game runnables to update GameData info
 	 * when they finish playing
 	 * @param gameData
 	 */
 	public synchronized void addGameData(GameData gameData){		
 		List<GameData> dataList = new ArrayList<>();
 		dataList.add(gameData);
 		GameDataAccumulator newData = DataProcessor.aggregate(dataList,BackendConstants.NUM_DATA_POINTS);
 		
 		_data = _data == null ? newData :
 			DataProcessor.combineAccumulators(_data, DataProcessor.aggregate(dataList,BackendConstants.NUM_DATA_POINTS));
 		
 		/* display data and determine the next display point */
 		_numGamesPlayed++;
 		if(_numGamesPlayed >= _nextDisplaySize){
 			sendDataToGui(_data.toGameDataReport());
 			_nextDisplaySize += BackendConstants.DATA_PACKET_SIZE;
 		}
 	}
 	
 	/**
 	 * Starts the timer for choosing heuristics, after which the player will be retrieved
 	 * @param the time, in seconds, to wait before requesting the player
 	 */
 	public void startGetPlayer(int time){
 		_timer = new Timer(1000*(time+1), new GetPlayerActionListener(this));
 		_gui.getBoard().newRound(time);
 		_timer.setRepeats(false);
 		_timer.start();
 	}
 	
 	/**
 	 * Gets the player associated with this object
 	 * @return the player
 	 */
 	public Player finishGetPlayer(){
 		if(_timer!= null) _timer.stop();
 		Player p = _gui.getBoard().getPlayer();
 		return p;
 	}
 	
 	/**
 	 * Updates records, data and displays finishing screen for the end of the game
 	 * @param combinedData
 	 */
 	private void finishMatch(GameDataReport combinedData){
 		int numPlayers = combinedData._timeStamps.get(0).wealthData.size();
 		
 		
 		
 		
 		List<String> names = new ArrayList<>();
 		
 //		for(int i = 0; )
 		
 		for(int i = 0; i < BackendConstants.MAX_NUM_PLAYERS; i++){
 			names.add(i < numPlayers ? _playerNames.get(i) : "");
 		}
 		int winnerID = combinedData.getPlayerWithMostWins();
 		names.remove(winnerID);
 		names.add(0, _playerNames.get(winnerID));
 		
 		/* displays the end game screen */
 		_gui.createEndGame(_gui.getBoard(), winnerID == _id, names.toArray(new String[names.size()]));
 		if(_gui.getUserMusic())_gui.playNextOutOfGameSong();
 		
 		/* update records */
 		_gui.getCurrentProfile().getRecord().addMatch(winnerID == _id);
 		_gui.saveProfiles();
 	}
 	
 	/**
 	 * Updates data in the gui
 	 * @param data 
 	 */
 	private void sendDataToGui(GameDataReport data){
 		_gui.getBoard().setPlayerSpecificPropertyData(getPlayerPropertyData(data._overallPlayerPropertyData));
 		_gui.getBoard().setPropertyData(data._overallPropertyData);
 		_gui.getBoard().setWealthData(getPlayerWealthData(data._timeStamps));
 	}
 	
 	/**
 	 * Populates the map of player ids to names
 	 */
 	protected void setPlayerNames(List<Player> players){
 		for(Player p : players){
 			_playerNames.put(p.ID, p.Name);
 		}
 		_playerNames.put(-1, "banker");
 	}
 
 	/**
 	 * Displays an error message in the gui
 	 * @param errorMessage the error message to be displayed
 	 */
 	synchronized void displayMessage(String message){
 		JOptionPane.showMessageDialog(_gui, message);
 	}	
 	
 	private class GetPlayerActionListener implements ActionListener{
 		HumanClient _client;
 		public GetPlayerActionListener(HumanClient client){
 			_client = client;
 		}
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			_client.finishRespondToGetPlayer();
 		}
 	}
 }
