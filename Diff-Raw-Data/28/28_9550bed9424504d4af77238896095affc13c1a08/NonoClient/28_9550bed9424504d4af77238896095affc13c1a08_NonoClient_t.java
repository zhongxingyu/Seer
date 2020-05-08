 /**
  * CSE 403 AA
  * Project Nonogram: Backend
  * @author  HyeIn Kim
  * @version v1.0, University of Washington 
  * @since   Spring 2013 
  */
 
 
 package uw.cse403.nonogramfun.network;
 import java.net.InetAddress;
 import java.net.Socket;
 
 import org.json.JSONObject;
 
 import uw.cse403.nonogramfun.enums.ClientRequest;
 import uw.cse403.nonogramfun.enums.Difficulty;
 import uw.cse403.nonogramfun.enums.ServerResponse;
 import uw.cse403.nonogramfun.nonogram.NonoPuzzle;
 import uw.cse403.nonogramfun.utility.NonoUtil;
 import uw.cse403.nonogramfun.utility.ParameterPolice;
 import android.graphics.Color;
 
 
 /**
  * NonoClient is a client side of Networking code. It allows android client to communicate 
  * with server without knowing messy networking details, by providing simple interface.
  */
 public class NonoClient {
 	
 	
 	// Private constructor
 	private NonoClient() {}
 	
 	
 	/**
 	 * Accepts a 2D array represents a NonoPuzzle, background color and name of NonoPuzzle
 	 * as parameters. Creates NonoPuzzle according to the given parameters and saves it in the database.
 	 * @param cArray Color array represents a NonoPuzzle
 	 * @param bgColor Background color of NonoPuzzle
 	 * @param name Name of NonoPuzzle
 	 * @throws Exception if any error occurs (Network error, invalid puzzle, database error ect.)
 	 */
 	public static void createPuzzle(Integer[][] cArray, Integer bgColor, String name) throws Exception {
 		ParameterPolice.checkIfValid2DArray(cArray);
 		
 		// 1. Send request: Create & Save puzzle in database
 		NonoNetwork network = NonoConfig.getNonoNetwork();
 		JSONObject requestJSON = new JSONObject();
 		NonoUtil.putClientRequest(requestJSON, ClientRequest.CREATE_PUZZLE);
 		NonoUtil.putColorArray(requestJSON, cArray);
 		NonoUtil.putColor(requestJSON, bgColor);
 		NonoUtil.putString(requestJSON, name);
 		network.sendMessage(requestJSON);
 		
 		// 2. Get server response and check if it is success or error
 		JSONObject responseJSON = network.readMessageJSON();
 		checkResponseError(responseJSON);
 		network.close();
 	}
 	
 	
 	/**
 	 * Accepts a difficulty and returns a randomly chosen NonoPuzzle of given difficulty.
 	 * @param difficulty Difficulty of NonoPuzzle
 	 * @return A random NonoPuzzle of given difficulty
 	 * @throws Exception if any error occurs (Network error, invalid puzzle, database error ect.)
 	 */
 	public static NonoPuzzle getPuzzle(Difficulty difficulty) throws Exception {
 		ParameterPolice.checkIfNull(difficulty, "Difficulty");
 		
 		// 1. Send request: Create & Save puzzle in database
 		NonoNetwork network = NonoConfig.getNonoNetwork();
 		JSONObject requestJSON = new JSONObject();
 		NonoUtil.putClientRequest(requestJSON, ClientRequest.GET_PUZZLE);
 		NonoUtil.putDifficulty(requestJSON, difficulty);
 		network.sendMessage(requestJSON);
 		
 		// 2. Get server response and check if it is success or error
 		JSONObject responseJSON = network.readMessageJSON();
 		checkResponseError(responseJSON);
 		return NonoUtil.getNonoPuzzle(responseJSON);
 	}
 	
 	
 	// Throws IOException if server responds with error message.
 	private static void checkResponseError(JSONObject responseJSON) throws Exception {
 		if(NonoUtil.getServerResponse(responseJSON) != ServerResponse.SUCCESS) {
			//throw new IOException("Error: " + NonoUtil.getErrorMsg(responseJSON));
 		}
 	}
 	
 	
 	
 	
 	// For testing  TODO: delete later
 	@SuppressWarnings("unused")
 	private static void testClient() throws Exception {
 		while (true) {
 			NonoNetwork tcpHandler = new NonoNetwork(new Socket(NonoConfig.getServerIP(), 1027));
 			//tcpHandler.sendMessage("Hi, Server. I'm Client!ooooooooooooooooooo");
 			//String response = tcpHandler.readMessageString();
 			//System.out.println("Server said: " + response);
 			
 			Integer[][] arr = {{ Color.BLACK, Color.BLACK }, { Color.BLUE, Color.BLUE } };
 			//tcpHandler.sendMessage(arr);
 			//NonoPuzzle puzzle = (NonoPuzzle) tcpHandler.readMessageObject();
 			System.out.println("I got a response puzzle from server!!");
 			//System.out.println(puzzle);
 			tcpHandler.close();
 		}
 	}
 	// For testing  TODO: delete later
 	private static void testJSON() throws Exception {
		System.out.println("I'm sending something to server!!");
 		Integer[][] cArray = {{ Color.BLACK, Color.BLACK }, { Color.BLUE, Color.BLUE } };
 		Integer bgColor = Color.BLACK;
 		createPuzzle(cArray, bgColor, "NOmmmmmmmmmmmmmmmmmmmmm");
 		InetAddress inetAddress = InetAddress.getByName(NonoConfig.SERVER_NAME);
 		System.out.println(inetAddress.getHostAddress());
 		NonoNetwork network = new NonoNetwork(new Socket(inetAddress.getHostAddress(), NonoConfig.BASE_PORT));
 		JSONObject requestJSON = new JSONObject();
 		NonoUtil.putClientRequest(requestJSON, ClientRequest.CREATE_PUZZLE);
 		NonoUtil.putColorArray(requestJSON, cArray);
 		NonoUtil.putColor(requestJSON, bgColor);
 		NonoUtil.putString(requestJSON, "NONONONONONONONONONONO");
 		System.out.println(requestJSON.getString(NonoUtil.JSON_STRING_TAG));
 		network.sendMessage(requestJSON);
 		
 		JSONObject responseJSON = network.readMessageJSON();
 		checkResponseError(responseJSON);
 		System.out.println(responseJSON);
 		String success = responseJSON.getString("JSON_Header");
 		//ServerResponse puzzle = NonoUtil.getServerResponse(responseJSON);
 		if (!success.equals("\"SUCCESS\"")) {
 			System.out.println(success);
 			System.out.println("Transaction failed");
 		}
 		System.out.println("I got a response puzzle from server!!");
 		//System.out.println(puzzle);
 		System.out.println("\n\n\n");
 	}
 	
 	private static void testGetPuzzles() throws Exception {
 		System.out.println("I'm trying to get from the server!!");
 		NonoPuzzle puzzle = getPuzzle(Difficulty.EASY);
 		System.out.println("Got Puzzle!");
 		System.out.println(puzzle);
 	}
 	// For testing  TODO: delete later
 	public static void main(String[] args) throws Exception {
 		System.out.println("Running NonoClient");
 		testJSON();
		//testGetPuzzles();
 		//testClient();
 	}
 }
 	
