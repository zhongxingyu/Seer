 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.io.DataOutputStream;
 import java.util.Scanner;
 import java.security.SecureRandom;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.io.IOException;
 
 public class Server {
 	ChatUI userInterface;
 	ServerSocket serverConnection;
 	Socket clientConnection;
 	BufferedReader incoming;
 	DataOutputStream outgoing;
 	AppInfo serverInfo;
 
 	public Server(String username) {
 		serverInfo = new AppInfo(username);
 		userInterface = new ChatUI();
 		userInterface.setVisible(true);
 	}
 
 	class MessageListenerThread extends Thread {
 		public void run() {
 			try {
 				while(true) {
 					String incomingMessage = incoming.readLine();
 					if(incomingMessage == null) {
 						break;
 					}
 					String[] delimitedMessage = incomingMessage.split("message:");
 
 					if(incomingMessage.startsWith("cusername:")) {
 						serverInfo.remoteUsername = incomingMessage.substring(10);
 						userInterface.incomingMessageBox.textArea.append("Username changed to " + serverInfo.remoteUsername + "\n");
						//this line used to print the server's username
 					} else if(incomingMessage.contains("disconnect")) {
 						userInterface.incomingMessageBox.textArea.append("\rClient has disconnected.\n");
 						close();
 						System.exit(0);
 					} else {
 						userInterface.incomingMessageBox.textArea.append("\r" + delimitedMessage[1] + "\n");
						//this line used to print the server's username
 					}
 				}
 				userInterface.incomingMessageBox.textArea.append("break!\n");
 				close();
 				System.exit(0);
 			} catch(Exception e) {}
 		}
 	}
 
 	public static void main(String[] args) throws Exception {
 		Server s = new Server(args[0]);
 		s.start(50000);
 		s.listen();
 
 		String message;
 		Scanner sc = new Scanner(System.in);
 		s.handshake();
 		Thread messageListener = s.new MessageListenerThread();
 		messageListener.start();
 
 		s.userInterface.incomingMessageBox.textArea.append(s.serverInfo.username + ": ");
 		while(s.clientConnection.isConnected() && s.serverConnection.isBound()) {
 			
 			message = sc.nextLine();
 			while(message.contains("~") || message.length() == 0) {
				//this line used to print the server's username
 				message = sc.nextLine();
 				if(message == null)
 					break;
 			}
 
 			if(message.contains("KThanksBye")) {
 				s.sendHandshakeMessage("disconnect:");
 				s.close();
 				System.exit(0);
 			} else if(message.startsWith("@")) {
 				s.serverInfo.username = message.substring(1);
 				s.userInterface.incomingMessageBox.textArea.append("Username changed to " + s.serverInfo.username);
 				s.sendHandshakeMessage("cusername:" + s.serverInfo.username);
				//this line used to print the server's username
 			} else {
 				s.send(message);
				//this line used to print the server's username
 			}
 		}
 	}
 
 	public void start(int port) throws Exception {
 		userInterface.incomingMessageBox.textArea.append("Opening port " + port + "...");
 		serverConnection = new ServerSocket(port);
 		userInterface.incomingMessageBox.textArea.append(" Opened!\n");
 	}
 
 	public void listen() throws Exception {
 		userInterface.incomingMessageBox.textArea.append("Waiting for somebody to connect...");
 		clientConnection = serverConnection.accept();
 		userInterface.incomingMessageBox.textArea.append(" OK! Somebody connected!\n");
 		this.setupStreams();
 	}
 
 	public void setupStreams() throws Exception {
 		incoming = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
 		outgoing = new DataOutputStream(clientConnection.getOutputStream());
 	}
 
 	public void send(String message) throws Exception {
 		outgoing.writeBytes("message:" + serverInfo.username + ": " + message + "\n");
 	}
 
 	public void sendHandshakeMessage(String message) throws Exception {
 		outgoing.writeBytes(message + "\n");
 	}
 
 	public String readLine() throws Exception {
 		return incoming.readLine();
 	}
 
 	public void handshake() throws Exception {
 		String raw_message = "";
 		String[] token = new String[3];
 		generateHash();
 		while(true) {
 			while(incoming.ready()) {
 				raw_message = incoming.readLine();
 				token = raw_message.split(":");
 				if(token[0].equals("hash")) {
 					userInterface.incomingMessageBox.textArea.append("sending hash ID...");
 					sendHandshakeMessage("hash:" + serverInfo.hashID);
 					userInterface.incomingMessageBox.textArea.append(" sent!\n");
 					serverInfo.remoteHash = token[1];
 				} else if(token[0].equals("username")) {
 					userInterface.incomingMessageBox.textArea.append("sending username...");
 					sendHandshakeMessage("username:" + serverInfo.username);
 					userInterface.incomingMessageBox.textArea.append(" sent!\n");
 					serverInfo.remoteUsername = token[1];
 				} else if(token[0].equals("ipaddress")) {
 					userInterface.incomingMessageBox.textArea.append("sending local IP address...");
 					sendHandshakeMessage("ipaddress:" + serverConnection.getInetAddress().toString());
 					userInterface.incomingMessageBox.textArea.append(" sent!\n");
 					serverInfo.remoteIP = token[1];
 				} else if(token[0].equals("port")) {
 					userInterface.incomingMessageBox.textArea.append("sending local port number...");
 					sendHandshakeMessage("port:" + Integer.toString(serverConnection.getLocalPort()));
 					userInterface.incomingMessageBox.textArea.append(" sent!\n");
 					serverInfo.remotePort = token[1];
 				} else break;
 			}
 			
 			if(serverInfo.remoteUsername.length() != 0 && serverInfo.remoteIP.length() != 0 && serverInfo.remoteHash.length() != 0) {
 				break;
 			}
 		}
 	}
 
 	public void generateHash() throws Exception {
 		SecureRandom rnd = new SecureRandom();
 		String rndString = new BigInteger(130, rnd).toString(32);
 		MessageDigest md = MessageDigest.getInstance("MD5");
 		md.reset();
 		md.update(rndString.getBytes());
 		byte[] hash = md.digest();
 		BigInteger bigInt = new BigInteger(1, hash);
 		String hashText = bigInt.toString(16);
 		while(hashText.length() < 32) {
 			hashText = "0" + hashText;
 		}
 
 		serverInfo.hashID = hashText;
 	}
 
 	public void close() throws Exception {
 		userInterface.incomingMessageBox.textArea.append("Disconnecting... ");
 		serverConnection.close();
 		clientConnection.close();
 		userInterface.incomingMessageBox.textArea.append("Disconnected!\n");
 	}
 }
