 package com.cn.server;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Map;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import com.cn.constants.Constants;
 import com.cn.constants.ProtocolConstants;
 import com.cn.constants.ServerConstants;
 import com.cn.protocol.Protocol;
 
 public class AuthenticationServer {
 	
 	private int port;
 	private ServerSocket serverSocket;
 	Logger logger = Logger.getLogger(AuthenticationServer.class);
 	
 	public AuthenticationServer() {
 		port = 8888;
 		try {
 			serverSocket = new ServerSocket(port);
 		} catch (IOException e) {
 			if(logger.isEnabledFor(Level.ERROR)) {
 				logger.error("AuthenticationServer threw an exception: ", e);
 			}
 		}
 		logger.trace("We started an Auth Server!");
 		logger.debug(ServerConstants.AUTH_SERVER_STARTUP);
 		logger.debug(ServerConstants.AUTH_SERVER_ACCEPTING);
 
 	}
 	
 	/**
 	 * Main method that is called when you launch the 
 	 * run_auth_server.bat or run_auth_server.sh
 	 * 
 	 * @param args not needed for now.
 	 */
 	public static void main(String[] args) {
 		PropertyConfigurator.configure(Constants.LOGGER_PROPERTIES);
 		AuthenticationServer server = new AuthenticationServer();
 		server.loop();
 	}
 
 	private void loop() {
 		logger.trace("Entering the auth server loop");
 		Socket clientSocket = null;
 		while(true) {
 			try {
 				clientSocket = null;
 				clientSocket = serverSocket.accept();
 				logger.trace("Client-(Auth)Server connection has been made successfully");
 			} catch (IOException e) {
 			    if(logger.isEnabledFor(Level.ERROR)) {
 			    	logger.error("Error establishing client-server connection: ", e);
 			    }
 				logger.debug(ServerConstants.Server_STARTUP_FAILED);
 				System.exit(1);
 			} 
 			
 			/*
 			 * Start a new thread (ServerThread).
 			 */
 			Thread t = new Thread(new AuthenticationServerThread(clientSocket));
 			if(logger.isTraceEnabled()) {
 				logger.trace("Starting a new AuthenticationServerThread: " + t);
 			}
 			t.start();
 		}
 	}
 	
 	public class AuthenticationServerThread implements Runnable{
 		
 		// Client-Server Interaction
 		protected Socket clientSocket;
 		protected BufferedReader sockBufReader = null;
 		protected PrintWriter sockPrintWriter = null;
 		protected BufferedInputStream bis;
 		protected BufferedOutputStream bos;
 		protected String accountName = null;
 		{logger.debug("ash is initialized to null");}
 		AuthenticationServerHelper ash = null;
 		
 		public AuthenticationServerThread(Socket socket) {
 			clientSocket = socket;
 
 			try {
 				bis = new BufferedInputStream(clientSocket.getInputStream());
 				bos = new BufferedOutputStream(clientSocket.getOutputStream());
 				sockPrintWriter = new PrintWriter(new OutputStreamWriter(bos), true);
 				sockBufReader = new BufferedReader(new InputStreamReader(bis));
 			} catch (IOException e) {
 				if(logger.isEnabledFor(Level.ERROR)) {
 					logger.error("Error initializing AuthenticationServerThread ", e);
 				}
 				System.exit(1);
 			} 
 		}
 
 		@Override
 		public void run() {
 			logger.trace("Beginning run method - AuthenticationServerThread.");
 			try {
 				String request;
 				while ((request = sockBufReader.readLine()) != null) {
 					logger.debug(request);
 
 					if (request.length() < 1) {
 						logger.debug("Message length is too short: "+request.length());
 						continue;
 					}
 
 					String cmd = Protocol.getRequestCmdSimple(request);
 					String[] args = Protocol.getRequestArgsSimple(request);
 
 					if(cmd.equalsIgnoreCase(Constants.LOGIN)) {
 						logger.debug("The cmd was 'login'");
 						onLOGIN(args);
 						continue;
 					}
 					else if(cmd.equalsIgnoreCase(Constants.SAVE)) {
 						logger.debug("The cmd was 'save'");
 						onSAVE(args);
 						continue;
 					}
 					else if(cmd.equalsIgnoreCase(Constants.CREATE_ACC)) {
 						logger.debug("The cmd was 'create account'");
 						onCREATEACC(args);
 						continue;
 					}
 					if(logger.isDebugEnabled()) {
 						logger.debug("An invalid message was received: " + request);
 						logger.debug(ServerConstants.INVALID_MSG_RECVD + request);
 					}
 					sockPrintWriter.println(Protocol.createSimpleResponse(ServerConstants.INVALID_MSG_RECVD_CODE));
 				}
 			} catch (Exception e) {
 				if(logger.isEnabledFor(Level.ERROR)) {
 					logger.error("User has disconnected from Auth Server... " +clientSocket.getInetAddress());
 				}
 
 			} finally {
 				try {
 					if (sockBufReader != null)
 						sockBufReader.close();
 					if (sockPrintWriter != null)
 						sockPrintWriter.close();
 					clientSocket.close();	
 				} catch (Exception e) {
 					if(logger.isEnabledFor(Level.ERROR)) {
 						logger.error("Exception thrown closing sockBufReader and sockPrintWriter", e);
 					}
 				}
 			}
 		}
 
 		@SuppressWarnings("unchecked")
 		private void onSAVE(String[] args) {
 			logger.trace("onSAVE method");
 			if(ash != null && ash.isAuthenticated()) {
 				logger.debug("ash is authenticated and not null");
 				Map map = ash.getCharacterMap();
 				if(map.containsKey(args[2])) {
 					logger.debug("character map contains char: "+args[2]);
 					map.remove(args[2]);
					String[] stats = new String[5];
 					for(int i=2; i<args.length; i++) {
 						//should include name...
 						stats[i-2] = args[i];
 					}
 					logger.debug("Stats: " +stats);
 					map.put(args[2], stats);
 					ash.overWriteChar();
 					sockPrintWriter.println(ProtocolConstants.SUCCESS);
 					logger.debug("onSAVE was successful");				
 				}
 			}
 			else {
 				logger.debug("Failed onSAVE");
 				sockPrintWriter.println(ProtocolConstants.FAILURE);
 			}
 		}
 
 		private void onLOGIN(String[] args) throws IOException {
 			logger.trace("Inside AuthServerThread.onLOGIN");
 			if(args.length != 3) {
 				invalidMsg();
 			}
 			else {
 				logger.debug("ash is being set. Username: " + args[1] + " Password: " + args[2]);
 				ash = new AuthenticationServerHelper(args[1], args[2]);
 				if(!ash.isUserFound()) {
 					sockPrintWriter.println(ProtocolConstants.USER_NOT_FOUND);
 					System.out.println("The login user was not found.");
 					return;
 				}
 				if(ash.isAuthenticated()) {
 					logger.debug("ash is authenticated in AuthServerThread.onLOGIN");
 					Map<String, String[]> characters = ash.getCharacterMap();
 					if(ash.getCharacterMap().size() > 0) {
 						sockPrintWriter.println(characters.keySet());
 					}
 					else {
 						logger.debug("No characters have been created on the account.");
 						sockPrintWriter.println(ProtocolConstants.NO_CHARS_CREATED);
 						return;
 					}
 					String request = null;
 					if ((request = sockBufReader.readLine()) != null) {
 						String loginAs = Protocol.getRequestCmdSimple(request);
 						if(characters.containsKey(loginAs)) {
 							sockPrintWriter.println(Protocol.convertListToProtocol(characters.get(loginAs)));
 						}
 						else {
 							invalidMsg();
 						}
 					}
 				}
 			}
 		}
 		
 		private void onCREATEACC(String[] args) {
 			AccountCreationHelper ach = new AccountCreationHelper(args[1], args[2]);
 			if(!ach.isAccountInUse()) {
 				System.out.println("The account is available.  It has been created.");
 				sockPrintWriter.println(ProtocolConstants.SUCCESS);
 			}
 			else {
 				sockPrintWriter.println(ProtocolConstants.ACCOUNT_ALREADY_IN_USE);
 			}
 		}
 
 		public void invalidMsg() {
 			System.out.println(ServerConstants.INVALID_MSG_RECVD);
 			sockPrintWriter.println(Protocol.createSimpleResponse(ServerConstants.INVALID_MSG_RECVD_CODE));
 		}
 
 	}
 
 }
