 package Networking;
 
 import Core.Logger;
 import Core.Message;
 import Core.MessageHandler;
 import Core.MessageListener;
 import Game.GameEngine;
 import java.io.*;
 import java.net.*;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 public class Server {
 
 	private static final int N_THREADS = 10;
 	private ServerSocket serverSocket;
 	private ExecutorService threadPool;
 	private Thread listenThread;
 	private List<CommunicationHandler> clientList;
 	private MessageHandler msgHandler;
 	private GameEngine gEngine;
 
 	public Server (short port) throws IOException {
 		this.serverSocket = new ServerSocket (port);
 		this.clientList = Collections.synchronizedList (new ArrayList<CommunicationHandler> ());
 		this.threadPool = Executors.newFixedThreadPool (N_THREADS);
 		this.gEngine = new GameEngine ();
 		this.msgHandler = new MessageHandler ();
 
 		this.msgHandler.registerMessageListener ("info", new MessageListener () {
 			@Override
 			public void messageReceived (Message msg) {
 				
 			}
 		});
 
 		this.msgHandler.registerMessageListener ("response", new MessageListener () {
 			@Override
 			public void messageReceived (Message msg) {
 				for (CommunicationHandler ch : clientList) {
					ch.sendData (msg.tag, msg);
 				}
 			}
 		});
 
 		//Connection listener
 		this.listenThread = createListenThread ();
 
 		//Listen for commands
 		createCmdThread ().start ();
 	}
 
 	public void startListening () {
 		this.listenThread.start ();
 	}
 
 	private Thread createListenThread () {
 		return new Thread (new Runnable () {
 			@Override
 			public void run () {
 				try {
 					Logger.log ("Listening for clients...");
 					while (true) {
 						Socket clientSocket = serverSocket.accept ();
 						Logger.log ("Client connecting...");
 
 						//Put client on new thread
 						try {
 							threadPool.execute (new ConnectionHandler (clientSocket, gEngine, msgHandler, clientList));
 						}
 						catch (Exception ex) {
 							Logger.logDebug (ex.getMessage ());
 						}
 					}
 				}
 				catch (SocketException sEx) {
 					//Cleanup
 					Logger.log ("Disconnecting clients...");
 					for (CommunicationHandler ch : clientList) {
 						ch.sendData ("exit", "");
 					}
 					threadPool.shutdown ();
 
 					//Disconnect all (will purge list)
 					for (int i = 0; i < clientList.size (); i++) {
 						clientList.get (0).disconnect ();
 					}
 
 				}
 				catch (Exception ex) {
 					Logger.logDebug ("Server listenThread: " + ex.getMessage ());
 				}
 			}
 		});
 	}
 
 	private Thread createCmdThread () {
 		return new Thread (new Runnable () {
 			private String cmd;
 			private boolean read = true;
 			BufferedReader reader = new BufferedReader (new InputStreamReader (System.in));
 
 			@Override
 			public void run () {
 				try {
 					while (read) {
 						cmd = reader.readLine ();
 
 						switch (cmd) {
 							case "exit":
 								serverSocket.close ();
 								System.exit (0);
 								break;
 						}
 					}
 				}
 				catch (Exception ex) {
 					Logger.logDebug (ex.getMessage ());
 				}
 				finally {
 					try {
 						reader.close ();
 					}
 					catch (Exception ex) {
 					}
 				}
 			}
 		});
 	}
 }
