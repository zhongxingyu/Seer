 package edu.brown.cs32.MFTG.tournament;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.databind.JsonMappingException;
 
 import edu.brown.cs32.MFTG.gui.MonopolyGui;
 import edu.brown.cs32.MFTG.monopoly.GameData;
 import edu.brown.cs32.MFTG.monopoly.Player;
 import edu.brown.cs32.MFTG.networking.ClientRequestContainer;
 import edu.brown.cs32.MFTG.networking.InvalidRequestException;
 import edu.brown.cs32.MFTG.tournament.data.DataProcessor;
 import edu.brown.cs32.MFTG.tournament.data.GameDataReport;
 
 public class HumanClient extends Client{
 
 	private static final int DEFAULT_PORT = 8080;
 	
 	private MonopolyGui _gui;
 //	private DummyGUI _dummyGui;
 
 
 
 	public HumanClient(String host, int port){
 		super(host, port);
 
 		_gui = new MonopolyGui(this);
		
 	//	_dummyGui = new DummyGUI();
 	}
 	
 	/**
 	 * Connects the Client to a socket
 	 * @throws IOException
 	 */
 	public void connectAndRun(){
		System.out.println("IF YOU AIN'T GOT NO MONEY TAKE YO BROKE ASS HOME!!!!1");
 		try {
 			_server = new Socket(_host, _port);
 			_input = new BufferedReader(new InputStreamReader(_server.getInputStream()));
 			_output = new BufferedWriter(new OutputStreamWriter(_server.getOutputStream()));
 		} catch (UnknownHostException e) {
 			displayError("Unknown host. Unable to connect to server :( WHAT THE FUCK, MAN!!!");
 			return;
 		} catch (IOException e) {
 			displayError("Unable to connect to server :(");
 			return;
 		}
 		try {
 			_id = respondToSendID();
 			System.out.println("creading board");
 			_gui.createBoard(_id);
 			System.out.println("created board");
 		} catch (IOException | InvalidRequestException e1) {
 			displayError("Unable to retrieve a unique ID from the server :(");
 			return;
 		}
 
 		while(true){
 			try {
 				handleRequest();
 			} catch (Exception e){
 				// TODO handle this
 			}
 		}
 	}
 
 	/***************Networking Methods*************************/
 
 	/**
 	 * 
 	 * @param the request which is being responded to
 	 */
 	protected void respondToDisplayError(ClientRequestContainer request){
 		List<String> arguments = request._arguments;
 
 		if (arguments == null){
 			// throw an error
 		} else if (arguments.size() < 1){
 			// throw a different error
 		}
 
 		displayError(arguments.get(0));
 	}
 
 	/**
 	 * Responds to a request sent over the server to display game data by displaying the data received
 	 * @param the request which is being responded to
 	 * @throws JsonParseException
 	 * @throws JsonMappingException
 	 * @throws IOException
 	 */
 	protected void respondToDisplayData(ClientRequestContainer request) throws JsonParseException, JsonMappingException, IOException{
 		List<String> arguments = request._arguments;
 
 		if (arguments == null){
 			// error
 		} else if (arguments.size() < 1){
 			// error
 		}
 
 		GameDataReport gameDataReport = _oMapper.readValue(arguments.get(0), GameDataReport.class);
 
 		displayGameData(gameDataReport);
 	}
 
 	/*******************************************************/
 
 	/**
 	 * Set and display the combined GameData
 	 * @param combinedData
 	 */
 	public void displayGameData(GameDataReport combinedData) {
 		//TODO implement
 		_gui.getBoard().setPlayerSpecificPropertyData(getPlayerPropertyData(combinedData._overallPlayerPropertyData));
 		_gui.getBoard().setPropertyData(combinedData._overallPropertyData);
 		_gui.getBoard().setWealthData(getPlayerWealthData(combinedData._timeStamps));
 		//_dummyGui.setPlayerSpecificPropertyData(getPlayerPropertyData(combinedData._overallPlayerPropertyData));
 	//	_dummyGui.setPropertyData(combinedData._overallPropertyData);
 	//	_dummyGui.setWealthData(getPlayerWealthData(combinedData._timeStamps));
 		_gui.getBoard().roundCompleted();	
 	}
 	
 	/**
 	 * Called by the game runnables to update GameData info
 	 * when they finish playing
 	 * @param gameData
 	 */
 	public synchronized void addGameData(GameData gameData){
 		_data.add(gameData);
 		if(_data.size() >= _nextDisplaySize){
 			//TODO display some data
 			GameDataReport r = DataProcessor.aggregate(_data, NUM_DATA_POINTS);
 			//_dummyGui.setPlayerSpecificPropertyData(getPlayerPropertyData(r._overallPlayerPropertyData));
 		//	_dummyGui.setPropertyData(r._overallPropertyData);
 		//	_dummyGui.setWealthData(getPlayerWealthData(r._timeStamps));
 			_gui.getBoard().setPlayerSpecificPropertyData(getPlayerPropertyData(r._overallPlayerPropertyData));
 			_gui.getBoard().setPropertyData(r._overallPropertyData);
 			_gui.getBoard().setWealthData(getPlayerWealthData(r._timeStamps));
 			_nextDisplaySize += DATA_PACKET_SIZE; //set next point at which to display
 		}
 	}
 	
 	/**
 	 * Gets the player associated with this object
 	 * @param the time, in seconds, to wait before requesting the player
 	 * @return
 	 */
 	public Player getPlayer(int time){
 		return _gui.getBoard().getPlayer();
 //		return _dummyGui.getPlayer();
 	}
 
 	/**
 	 * Displays an error message in the gui
 	 * @param errorMessage the error message to be displayed
 	 */
 	private void displayError(String errorMessage){
 		JOptionPane.showMessageDialog(_gui, errorMessage);
 	}
 
 	/*******************************************************/
 
 	public static void main (String[] args){
 		int port;
 
 		if(args.length == 1){
 			port = DEFAULT_PORT;
 		} else if (args.length == 2){
 			try {
 				port = Integer.parseInt(args[1]);
 			} catch (NumberFormatException e){
 				System.out.println("ERROR: please enter a valid number");
 				System.out.println("Usage: <hostname> [serverport]");
 				return;
 			}
 		} else {
 			System.out.println("Usage: <hostname> [serverport]");
 			return;
 		}
 
 		Client pm = new HumanClient(args[0], port);
 
 		pm.connectAndRun();
 	}
 }
