 package maze;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 
 import server.ServerQuery;
 
 /**
  * QueryResponder.java
  * 
  * Takes querys from Robots in BlokIDE and sends responses
  * to these requests. This protocol is defined in our
  * protocol document
  * 
  * @author:			Shafqat Bhuiyan
  * @author:			Devin Barry
  * @date:			27.10.2012
  * @lastModified:	29.10.2012
  * 
  */
 public class QueryResponder extends Thread {
 	private final int QUERY_PORT = 9001;
 	private final int RESPONSE_PORT_BASE = 10002;
 	
 	private Maze maze;
 	private ServerQuery sq;
 	private String response;
 
 	public QueryResponder(Maze maze) {
 		this.maze = maze;
 	}
 
 	public void run() {
 		byte[] receiveData;
 		while (true) {
 			try {
 				// Listen for query
 				DatagramSocket serverSocket = new DatagramSocket(null);
 				serverSocket.setReuseAddress(true); // Allow multiple receivers
 													// on this port
 				serverSocket.bind(new InetSocketAddress("0.0.0.0", QUERY_PORT));
 
 				//be completely certain that we clear the data array everytime
 				receiveData = new byte[1024];
 				for (int i = 0; i < receiveData.length; i++) {
 					receiveData[i] = (byte) 0;
 				}
 
 				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
 				serverSocket.receive(receivePacket);
 				String query = new String(receivePacket.getData());
 				
 				sq = new ServerQuery(receiveData); //create a new server query
 				System.out.println("RECEIVED: " + query);
 				
 				fetchResponse(query);
 				
 				//If the query was valid, send a response
 				if (response != "") {
 					respondToQuery(query);
 				}
 
 				serverSocket.close();
 			}
 			catch (Exception e) {
 				e.printStackTrace();
 			}
 
 		}
 
 	}
 	
 	/**
 	 * Calculates the correct response for the query received
 	 * @param query
 	 */
 	private void fetchResponse(String query) {
 		response = "";
 		if (sq.getQuery().equals("obstacle")) {
			response = sq.getQuery() + ","; //send back the original query string
 			if (maze.getWall(sq.getDirection(), sq.getX(), sq.getY())) {
				response += "wall,"; 
 			}
 			else {
				response += "empty,";
 			}
 			response += sq.getDirection() + ",";
 			response += sq.getX() + ",";
 			response += sq.getY() + "\0"; //C needs a null at the end of the string
 		}
 		else if (sq.getQuery().equals("sensor")) {
			response = sq.getQuery() + ",";//send back the original query string
 			if (maze.getSensor(sq.getX(), sq.getY())) {
 				response += "true";
 			}
 			else {
 				response += "false";
 			}
 			response += "\0"; //C needs a null at the end of the string
 		}
 		else if (sq.getQuery().equals("switch")) {
			response = sq.getQuery() + ","; //send back the original query string
 			if (maze.getSwitch(sq.getX(), sq.getY())) {
 				response += "true";
 			}
 			else {
 				response += "false";
 			}
 			response += "\0"; //C needs a null at the end of the string
 		}
 		else {
 			System.out.println("Invalid query: " + query);
 		}
 	}
 		
 
 	/**
 	 * Sends the response back to the requester
 	 * @param query
 	 * @throws IOException
 	 */
 	private void respondToQuery(String query) throws IOException {
 		InetAddress sendingAddress = InetAddress.getByName("localhost");
 		
 		//byte[] sendData = new byte[10];
 		DatagramSocket clientSocket = new DatagramSocket();
 		//sendData[0] = (byte) response.charAt(0);
 		byte[] sendData = response.getBytes();
 		
 		//System.out.println("Sending: " + (int) (sendData[0]));
 		System.out.println("Sending: " + response);
 		
 		int responsePort = RESPONSE_PORT_BASE + 1000 * (sq.getRobotID() - 1);
 		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, sendingAddress, responsePort);
 		clientSocket.send(sendPacket);
 		clientSocket.close();
 	}
 }
