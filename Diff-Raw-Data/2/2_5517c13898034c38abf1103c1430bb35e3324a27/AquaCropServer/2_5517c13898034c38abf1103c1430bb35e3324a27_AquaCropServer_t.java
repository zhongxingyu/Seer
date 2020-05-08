 package org.uncertweb.aquacrop.remote;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * 
  * @author Richard Jones
  *
  */
 public class AquaCropServer {
 
 	private static final Logger logger = LoggerFactory.getLogger(AquaCropServer.class);
 	
 	private int port;
 	private String basePath;
 	private String prefixCommand;
 	private String basePathOverride;
 	private boolean keepFiles;
 	private ServerSocket serverSocket;
 
 	public AquaCropServer(int port, String basePath, boolean keepFiles) {
 		this(port, basePath, null, null, keepFiles);
 	}
 
 	public AquaCropServer(int port, String basePath, String prefixCommand, String basePathOverride, boolean keepFiles) {
 		this.port = port;
 		this.basePath = basePath;
 		this.prefixCommand = prefixCommand;
 		this.basePathOverride = basePathOverride;
 		this.keepFiles = keepFiles;
 	}
 
 	public void start() throws IOException {
 		// start socket		
 		serverSocket =  new ServerSocket(port);
 		logger.info("Listening on port " + port + "...");
 
 		// request loop
 		ExecutorService pool = Executors.newFixedThreadPool(4);
 		while (true && !serverSocket.isClosed()) {
 			try {
 				Socket clientSocket = serverSocket.accept();
 				logger.info("Client " + clientSocket.getRemoteSocketAddress() + " connected.");
 				pool.execute(new AquaCropServerThread(clientSocket, basePath, prefixCommand, basePathOverride, keepFiles));
 			}
 			catch (IOException e) {
 				if (!serverSocket.isClosed()) {
 					logger.error("Could not accept client connection: " + e.getMessage());
 				}
 			}
 		}
 	}
 
 	public void stop() throws IOException {
 		if (serverSocket != null) {
 			logger.info("Stopped listening on port " + port + ".");
 			serverSocket.close();
 		}
 	}
 
 	public static void main(String[] args) {
 		if (args.length > 1) {			
 			// parse arguments
 			int port = Integer.parseInt(args[0]);
 			String basePath = args[1];
 			String prefixCommand = null;
 			boolean keepFiles = false;
 			if (args.length > 2) {
 				String lcArgs = args[2].toLowerCase();
 				if (lcArgs.equals("true") || lcArgs.equals("false")) {
 					keepFiles = Boolean.parseBoolean(args[2]);
 				}
 				else {
 					prefixCommand = args[2];
 				}
 			}
 			String basePathOverride = null;
 			if (args.length > 3) {
 				basePathOverride = args[3];
 			}
 			if (args.length > 4) {
				keepFiles = Boolean.parseBoolean(args[4]);
 			}
 			logger.info("Starting server with base path '" + basePath + "' and " + (prefixCommand != null ? "prefix command '" + prefixCommand + "'." : "no prefix command."));
 			logger.info((keepFiles ? "Keeping" : "Removing") + " project files after AquaCrop runs.");
 			if (basePathOverride != null) {
 				logger.info("Using " + basePathOverride + " as path override.");
 			}
 
 			// create and start		
 			final AquaCropServer server = new AquaCropServer(port, basePath, prefixCommand, basePathOverride, keepFiles);
 			
 			// hook to shutdown gracefully
 			Runtime.getRuntime().addShutdownHook(new Thread() {
 				@Override
 				public void run() {
 					try {
 						server.stop();
 					}
 					catch (IOException e) {
 						logger.warn("Couldn't shutdown server gracefully, sockets may still be open.");
 					}
 				}
 			});
 			
 			try {
 				// start
 				server.start();
 			}
 			catch (IOException e) {
 				System.err.println("Could not listen on port " + port + ", is it already in use?");
 			}
 		}
 		else {
 			AquaCropServer.printUsage();
 		}
 	}
 
 	private static void printUsage() {
 		System.out.println("Usage: aquacrop-interface PORT BASE [PREFIX]");
 		System.out.println(" PORT the port you wish the server to listen on");
 		System.out.println(" BASE the path to where AquaCrop and ACsaV31plus directories can be found");
 		System.out.println(" PREFIX the prefix command for running the executables");
 	}
 
 }
