 package com.github.ribesg.javachat.server;
 
 import static com.github.ribesg.javachat.common.Constants.SEPARATOR_STRING;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import com.github.ribesg.javachat.common.Message;
 import com.github.ribesg.javachat.common.TcpPacket;
 import com.github.ribesg.javachat.common.requests.DeliverRequest;
 import com.github.ribesg.javachat.common.requests.Request.ReqType;
 import com.github.ribesg.javachat.common.responses.ConnectResponse;
 import com.github.ribesg.javachat.common.responses.DisconnectResponse;
 import com.github.ribesg.javachat.common.responses.PingResponse;
 import com.github.ribesg.javachat.common.responses.Response.RespStatus;
 import com.github.ribesg.javachat.common.responses.Response.RespType;
 import com.github.ribesg.javachat.common.responses.SendResponse;
 import com.github.ribesg.javachat.common.responses.ViewedResponse;
 
 public class ServerIO extends Thread {
 
 	private boolean terminated = false;
 
 	private final int serverPort;
 
 	private ServerSocket serverSocket;
 	private Socket clientSocket;
 	
 	private Server server;
 
 	public ServerIO(final int port, Server serv) {
 		serverPort = port;
 		server = serv;
 	}
 
 	@Override
 	public void start() {
 		try {
 			serverSocket = new ServerSocket(serverPort);
 		} catch (final IOException e) {
 			System.out.println("Unable to start the server! Error follows:");
 			e.printStackTrace();
 			exit();
 		}
 	}
 
 	@Override
 	public void run() {
 		try {
 			while (!terminated) {
 				clientSocket = serverSocket.accept();
 				try (BufferedReader reader = new BufferedReader(
 						new InputStreamReader(clientSocket.getInputStream()))) {
 					final String red = reader.readLine();
 					String[] parts = red.split(SEPARATOR_STRING);
 
 					if ('R' == red.charAt(0)) {// response
 
 					} else { // request
 						ReqType type = ReqType.valueOf(parts[0]);
 						long sessionId = Long.valueOf(parts[1]);
 						long sequenceNumber = Long.valueOf(parts[2]);
 
 						switch (type) {
 						case CONNECT:
 							String username = parts[3];
 							String password = parts[4];
 							switch (server.addClient(username, password, sessionId)) {
 								case ALREADY_CO:
 									sendResponse(clientSocket, RespType.CONNECT, RespStatus.ALREADY_CO, sessionId, sequenceNumber++);
 									break;
 								case BAD_PASSWORD:
 									sendResponse(clientSocket, RespType.CONNECT, RespStatus.INVALID_PASS, sessionId, sequenceNumber++);
 									break;
 								case FULL:
 									sendResponse(clientSocket, RespType.CONNECT, RespStatus.TOO_MANY_USERS, sessionId, sequenceNumber++);
 									break;
 								case OK:
 									sendResponse(clientSocket, RespType.CONNECT, RespStatus.OK, sessionId, sequenceNumber++);
 									break;
 							}
 						case PING:
 							if(server.ping(sessionId))
 							{
 								sendResponse(clientSocket, RespType.PONG, RespStatus.OK, sessionId, sequenceNumber++);
 							} else {
 								sendResponse(clientSocket, RespType.PONG, RespStatus.INVALID_SESS, sessionId, sequenceNumber++);
 							}
 							break;
 						case SEND:
 							String message = parts[3];
 							long time = Long.valueOf(parts[4]);
 							Message mess = new Message(message, time, time);
 							sendResponse(clientSocket, RespType.SEND, RespStatus.OK, sessionId, sequenceNumber++);
 						default:
 						}
 
 					}
 
 					terminated = red.equalsIgnoreCase("stop");
 					System.out.println(red);
 				}
 				clientSocket.close();
 			}
 		} catch (final Exception e) {
 			System.out.println("Fatal Error! Error follows:");
 			e.printStackTrace();
 			exit();
 		}
 	}
 
 	
 	
 	private void send(Socket client, TcpPacket packet) throws Exception {
 		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
 				client.getOutputStream()))) {
 			writer.write(packet.toString());
 		}
 	}
 	
 	public void sendDeliver(Socket client, final long sessionId, final long sequenceNumber, final Message message) throws Exception{
 		send(client, new DeliverRequest(sessionId, sequenceNumber, message.getContent(), message.getTime(), message.getId()));
 	}
 	
 	public void sendResponse(Socket client,RespType type, RespStatus state,  final long sessionId, final long sequenceNumber) throws Exception {
 		switch (type) {
 		case CONNECT:
 			send(client, new ConnectResponse(sessionId, sequenceNumber,state));
 			break;
 		case DISCONNECT:
 			send(client, new DisconnectResponse(sessionId,sequenceNumber, state));
 			break;
 		case PONG:
 			send(client, new PingResponse(sessionId, sequenceNumber, state));
 			break;
 		case SEND:
 			send(client, new SendResponse(sessionId, sequenceNumber, state));
 			break;
 		case VIEWED:
 			send(client, new ViewedResponse(sessionId, sequenceNumber, state));
 			break;
 		default:
 			throw new Exception("Server not supposed to send such type of response : " + type.toString());
 		}
 	}
 
 	public void exit() {
 		if (serverSocket != null) {
 			try {
 				serverSocket.close();
 			} catch (final IOException e) {
 				// We don't care : the socket may already be closed
 			}
 		}
 		if (clientSocket != null) {
 			try {
 				clientSocket.close();
 			} catch (final IOException e) {
 				// We don't care : the socket may already be closed
 			}
 		}
 		terminated = true;
 	}
 
 }
