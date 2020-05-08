 /**
  * CSE 403 AA
  * Project Nonogram: Backend
  * @author  HyeIn Kim 
  * @version v1.0, University of Washington 
  * @since   Spring 2013 
  */
 
 
 package uw.cse403.nonogramfun.network;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.SocketTimeoutException;
 
 import org.json.JSONObject;
 
 import uw.cse403.nonogramfun.enums.ClientRequest;
 import uw.cse403.nonogramfun.enums.ServerResponse;
 import uw.cse403.nonogramfun.nonogram.Nonogram;
 import uw.cse403.nonogramfun.utility.NonoUtil;
 import uw.cse403.nonogramfun.utility.ParameterPolice;
 
 
 /**
  * NonoServer is a server that accepts and processes client requests.
  * It has a main method that runs the server and a method that tells status of a server socket.
  */
 public class NonoServer {
 	private static final int NUM_PORTS = 4;
 	private static final String SERVER_IP = NonoConfig.getServerIP();
 	
 	
 	// Private constructor
 	private NonoServer() {}
 	
 
 	/**
 	 * The main method that runs this server
 	 * @throws Exception if any error occurs running server or processing client request.
 	 */
 	public static void main(String[] args) throws Exception {
 		System.out.println("Running NonoServer");
 		runServer();
 	}
 	
 	/**
 	 * Accepts a server socket and returns a string summarizing its status. 
 	 * @param serverSock A server socket whose status is going to be summarized.
 	 * @return a string summarizing the status of given server socket.
 	 */
 	public static String dumpServerState(ServerSocket serverSock) {
 		ParameterPolice.checkIfNull(serverSock, "Server Socket");
 		
 		StringBuilder sb = new StringBuilder();
 		if(serverSock != null){
 			sb.append("\nListening on: " + serverSock.toString());
 		}else{
 			sb.append("Not listening");
 		}
 		sb.append("\n");
 		return sb.toString();
 	}
 	
 	
 	// Runs the server, by opening the ports
 	private static void runServer() throws Exception {
 		if (SERVER_IP == null) {
 			throw new Exception("Error: Server cannot find its IP address!");
 		}
 		for(int i=0; i<NUM_PORTS; i++) {
 			openPort(i);
 		}
 	}
 	
 		
 	// Opens a port
 	private static synchronized void openPort(final int portID) {
 		Thread tcpThread = new Thread(){
 			@Override
 			public void run() {
 				ServerSocket serverSock = null;
 				try {
 					
 					// 1. Set up a server socket for this port
 					serverSock = new ServerSocket();
 					serverSock.bind(new InetSocketAddress(NonoConfig.SERVER_NAME, NonoConfig.BASE_PORT + portID));
 					serverSock.setSoTimeout(NonoConfig.SOCKET_TIMEOUT);
 					System.out.println(dumpServerState(serverSock));
 					
 					// 2. Wait for connection & process request. Retry if times out.
 					while(true) {
 						NonoNetwork network = null;
 						try {
 							network = new NonoNetwork(serverSock.accept());
 							JSONObject requestJSON = network.readMessageJSON();
 							JSONObject responseJSON = processRequest(requestJSON);
 							network.sendMessage(responseJSON);
 							//testSerer(network);
 						}catch(SocketTimeoutException ste) {
 							System.out.println("Socket timed out");
 						}catch(Exception e) {
 							sendErrorJSON(network, e);
 						}finally{
 							if(network != null) try { network.close(); network = null; }catch(Exception e) {}
 						}
 					}
 					
 				}catch(Exception e){
 					e.printStackTrace();
 				}finally{
 					if(serverSock != null) { try { serverSock.close(); }catch(Exception e) {} }
 				}
 			}
 		};
 		tcpThread.start();
 	}
 	
 	
 	// Accepts a JSON Object that represents a client request, processes the request and 
 	// returns a JSON Object that represents a server response to the client request.
 	private static JSONObject processRequest(JSONObject requestJSON) throws Exception {
 		ClientRequest request = NonoUtil.getClientRequest(requestJSON);
 		JSONObject responseJSON = new JSONObject();
 		NonoUtil.putServerResponse(responseJSON, ServerResponse.SUCCESS);
 		
 		switch (request) {
 			case CREATE_PUZZLE:
 				//testJSON(network, requestJSON);
 				Nonogram.createPuzzle(requestJSON);
 				break;
 			case GET_PUZZLE:
 				NonoUtil.putNonoPuzzle(responseJSON, Nonogram.getPuzzle(requestJSON));
 				break;
 			case SAVE_SCORE:
 				Nonogram.saveScore(requestJSON);
 				break;
 			case GET_SCORE_BOARD:
				NonoUtil.putScoreBoard(requestJSON, Nonogram.getScoreBoard(requestJSON));
 				break;
 			default:
 				throw new UnsupportedOperationException();
 		}
 		
 		return responseJSON;
 	}
 	
 	
 	// When error occurs while processing client request, sends the error to client
 	private static void sendErrorJSON(NonoNetwork network, Exception e) {
 		try {
 			e.printStackTrace();
 			JSONObject errorJSON = new JSONObject();
 			NonoUtil.putServerResponse(errorJSON, ServerResponse.ERROR);
 			NonoUtil.putObject(errorJSON, NonoUtil.JSON_ERROR_MSG_TAG, e.getStackTrace());
 			network.sendMessage(errorJSON);
 		}catch(Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	/*
 	// For testing  TODO: delete later
 	@SuppressWarnings("unused") 
 	private static void testJSON(NonoNetwork network, JSONObject requestJSON) throws JSONException, IOException {
 		System.out.println("Client sent me something!\n");	
 		System.out.println(requestJSON);
 		Integer[][] cArray = NonoUtil.getColorArray(requestJSON);
 		Integer bgColor = NonoUtil.getColor(requestJSON);
 		String name = NonoUtil.getString(requestJSON);
 		NonoPuzzle puzzle = NonoPuzzle.createNonoPuzzle(cArray, bgColor, name);
 		System.out.println("So I made a puzzle out of it!!");
 		System.out.println(puzzle);
 		System.out.println("\n");
 		
 		JSONObject responseJSON = new JSONObject();
 		NonoUtil.putServerResponse(responseJSON, ServerResponse.SUCCESS);
 		NonoUtil.putNonoPuzzle(responseJSON, puzzle);
 
 		network.sendMessage(responseJSON);
 		System.out.println("And I sent it to client!\n");
 		System.out.println("\n\n\n");
 	}
 	*/
 	
 	/*
 	// For testing  TODO: delete later
 	@SuppressWarnings("unused") 
 	private static void testSerer(NonoNetwork network) throws IOException, ClassNotFoundException {
 		//String request = network.readMessageString();
 		//System.out.println("Client said: " + request);
 		//network.sendMessage("Hi, Client. I'm server!");
 		
 		//Integer[][] arr = (Integer[][]) network.readMessageObject();
 		//network.sendMessage(NonoPuzzle.createNonoPuzzle(arr, Color.BLACK, "Test"));
 	}
 	*/
 	
 	
 }
