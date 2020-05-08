 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.Arrays;
 
 /**
  * 
  */
 
 /**
  * @author sharath
  * 
  */
 public class SimpellaNetServer {
 	int port;
 	boolean status;
 
 	class TCPServer implements Runnable {
 		private int portNumber;
 
 		public TCPServer(int portNumber) {
 			this.portNumber = portNumber;
 		}
 
 		public void run() {
 			try {
 				TCPServerThread(portNumber);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	};
 
 	public int start() {
 		status = true;
 		Thread tcp_t = new Thread(new TCPServer(port));
 		tcp_t.start();
 		return 0;
 	}
 	
 	public void stop() {
 		status = false;
 	}
 
 	/**
 	 * TCP server thread.
 	 * 
 	 * @param tcpServerPort
 	 *            the tcp server port
 	 */
 	private void TCPServerThread(int tcpServerPort) throws Exception {
 		// create client socket
 		class TCPserverResponseThread implements Runnable {
 			private Socket clientSocket;
 			public TCPserverResponseThread(Socket clientSocket) {
 				this.clientSocket = clientSocket;
 			}
 		public void run() {
 				try {
 					TCPserverResponse(clientSocket);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		;
 		Socket clientSocket = new Socket();
 		System.out.println("Starting TCP Server at port " + tcpServerPort);
 		ServerSocket SimpellaTCP = null;
 		try {// create server socket
 			SimpellaTCP = new ServerSocket(tcpServerPort);
 		} catch (SocketException e) {
 			System.out.println("TCP Socket already in use.");
 			System.exit(1);
 		} catch (IOException e) {
 			System.out
 					.println("Failed to start TCP server at " + tcpServerPort);
 			System.exit(1);
 		}
 		while (status) {
 			try {
 				clientSocket = SimpellaTCP.accept();
 
 			} catch (IOException e) {
 				System.out.println("Accept failed at " + tcpServerPort);
 				continue;
 			}
 			System.out.println("Accepted!");
 			String inComingIP = clientSocket.getInetAddress().getHostAddress();
 			int inComingPort = clientSocket.getPort();
 			if (SimpellaConnectionStatus.isInConnectionPresent(inComingIP, inComingPort) || 
 					SimpellaConnectionStatus.isOutConnectionPresent(inComingIP, inComingPort)) {
 				System.out.println("Duplicate connection");
 				clientSocket.close();
 			} else {
 				if(SimpellaConnectionStatus.incomingConnectionCount < 3){
 					Thread tcp_serverResp_t = new Thread(new TCPserverResponseThread(
 						clientSocket));
 					tcp_serverResp_t.start();
 				} else {
 						String error = "SIMPELLA/0.6 503 Maximum number of connections reached";
 						System.out.println("Server replies with " + error);
 						DataOutputStream outToClient = new DataOutputStream(
 								clientSocket.getOutputStream());
 						outToClient.write(error.getBytes());
 						clientSocket.close();
 				}
 			}
 		}
 	}
 	
 	public static void sendPong(Socket clientSocket, byte[] header){
 		//Reply with a pong on ping 
 		System.out.println("Sending pong");
 		byte[] payload = new byte[37];
 		System.arraycopy(header, 0, payload, 0, 22);
 		Header h2 = new Header();
 		h2.initializeHeader();
 		h2.setHeader(payload);
 		h2.setMsgType("pong");
 		//TODO files and size
 		byte[] filesShared = null;
 		byte[] kbsShared = null;
 		//No need to set MsgId as it should be same as ping					
 		h2.setPongPayload(clientSocket,payload,filesShared,kbsShared);
 
 		DataOutputStream outToClient = null;
 		try {
 			outToClient = new DataOutputStream(
 					clientSocket.getOutputStream());
 			outToClient.write(payload);
 		} catch (IOException e) {
 			System.out.println("Socket Connection Error during pong write");
 		}
 		
 		System.out.println("Server replies with pong : " + Arrays.toString(payload));
 	}
 	
 	/**
 	 * TCP server response.
 	 *
 	 * @param clientSocket the client socket
 	 */
 	public void TCPserverResponse(Socket clientSocket) throws Exception {
 		//New connection, check for the connection headers
 		String replyToConnect = "SIMPELLA/0.6 200 OK";
 		DataOutputStream outToClient = new DataOutputStream(
 				clientSocket.getOutputStream());
 
 		DataInputStream inFromClient = new DataInputStream(
 				clientSocket.getInputStream());
 		byte[] cmd = new byte[512]; 
 		//TODO read larger messages in chunks
 		int len = inFromClient.read(cmd);
 		
 		String S = new String(cmd);
 		if (S.substring(0, len).equals("SIMPELLA CONNECT/0.6\r\n")) {
 			System.out.println("Server replies with " + replyToConnect);
 			outToClient.write(replyToConnect.getBytes());
 			SimpellaConnectionStatus.addIncomingConnection(clientSocket);
 		} else {
 			System.out.println("Unknown connection request, ignoring");
 			return;
 		}
 		while(true) {
 			byte[] header = new byte[23];
 			len = inFromClient.read(header, 0, 23);
 			if(len == -1) {
 				System.out.println("Client has close the socket, exit");
 				break;
 			}
 			handleMsg(header, clientSocket);
 		}
 	}
 	public static void broadcastPing(byte[] pingMsg, Socket sender) throws Exception {
 		Socket clientSocket = null;
 		String clientIP = "";
 		System.out.println("In broadcast ping");
 		for(int i = 0; i < 3; i++) {
 			
 			clientSocket = 
 					SimpellaConnectionStatus.incomingConnectionList[i].sessionSocket;
 			clientIP = 
 					SimpellaConnectionStatus.incomingConnectionList[i].remoteIP;
 			int clientPort = SimpellaConnectionStatus.incomingConnectionList[i].remotePort;
 			if(!(clientIP.equals("")) && 
 					!((sender.getInetAddress().getHostAddress().equals(clientIP)) &&
 							(sender.getPort() == clientPort))) {
 				/* send to everyone apart from this node */
 				System.out.println("sending ping to IP: " + clientIP + " Port = " + clientPort);
 				DataOutputStream outToServents = new DataOutputStream(
 						clientSocket.getOutputStream());
 				outToServents.write(pingMsg);
 			}
 		}
 		for(int j = 0; j < 3; j++) {
 			clientSocket = 
 					SimpellaConnectionStatus.outgoingConnectionList[j].sessionSocket;
 			
 			clientIP = 
 			SimpellaConnectionStatus.outgoingConnectionList[j].remoteIP;
 			int clientPort = SimpellaConnectionStatus.outgoingConnectionList[j].remotePort;
 			if(!(clientIP.equals("")) && 
 					!((sender.getInetAddress().getHostAddress().equals(clientIP)) &&
 							(sender.getPort() == clientPort))) {
 				System.out.println("sending ping to IP: " + clientIP + " Port = " + clientPort);
 				/* send to everyone apart from this node */
 				DataOutputStream outToServents = new DataOutputStream(
 						clientSocket.getOutputStream());
 				outToServents.write(pingMsg);
 			}
 		}
 	}
 	
 	public static void handleMsg(byte[] header, Socket sessionSocket) throws Exception{
 		
 		DataInputStream inFromClient = new DataInputStream(
 				sessionSocket.getInputStream());
 		if (header[16] == (byte) 0x00) {
 			System.out.println("Ping received");
 			String key = SimpellaRoutingTables.guidToString(header);
 			if(SimpellaRoutingTables.PingTable.containsKey(key)) {
 				//TODO combine if and else to one if ignore Ping if the node has seen the request!
 			} else {
 				SimpellaRoutingTables.insertPingTable(key, sessionSocket);
 				if(header[17] > 1) {
 					header[17]--; //decrement TTL
 					header[18]++; //Increment hops
 					broadcastPing(header, sessionSocket);
 					//Set TTL and hops to default values
 					header[17] = (byte)0x07;
 					header[18] = (byte)0x00; //Increment hops
 					sendPong(sessionSocket, header);
 				}
 			}
 			//TODO else if header[16] == 0x01, forward	
 		} else if(header[16] == (byte)0x01){
 			System.out.println("Pong received");
 			String guid = SimpellaRoutingTables.guidToString(header);
 			//Read the pong payload
 			byte[] pongPayLoad = new byte[14];
 			int len = inFromClient.read(pongPayLoad, 0, 14);
 			if(len != 14){
 				System.out.println("Something has gone wrong!");
 				return;
 			}
 			if(SimpellaRoutingTables.generatedPingList.contains(guid)) {
 				// pong is for me
 				System.out.println("Pong received for self: PayLoad = " + 
						pongPayLoad[2] + pongPayLoad[3] + pongPayLoad[4] + pongPayLoad[5]);
 				return;
 				//TODO read contents and store them in a store
 			} else {
 				// forward the pong using the routing table
 				if(SimpellaRoutingTables.PingTable.containsKey(guid)) {
 					Socket pongFwdSocket = SimpellaRoutingTables.PingTable.get(guid);
 					System.out.println("Pong received for ip " + 
 							pongFwdSocket.getInetAddress().getHostAddress());
 					header[17]--; //decrement TTL
 					header[18]++; //Increment hops
 					DataOutputStream pongToClient = null;
 					try {
 						pongToClient = new DataOutputStream(
 								pongFwdSocket.getOutputStream());
						pongToClient.write(header);
 						pongToClient.write(pongPayLoad);
 					} catch (IOException e) {
 						System.out.println("Socket Connection Error during pong write");
 					}	
 				}
 			}
 		} else if(header[16] == (byte)0x80){
 			//TODO handle message
 			System.out.println("Query-message");
 		} else if(header[16] == (byte)0x81){
 			//TODO handle message
 			System.out.println("Query-hit message");
 		}
 	}
 	public int getPort() {
 		return port;
 	}
 
 	public void setPort(int port) {
 		this.port = port;
 	}
 }
