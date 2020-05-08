 package com.hapiware.http;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.management.ManagementFactory;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.RejectedExecutionException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.management.InstanceAlreadyExistsException;
 import javax.management.InstanceNotFoundException;
 import javax.management.MBeanException;
 import javax.management.MBeanServer;
 import javax.management.MalformedObjectNameException;
 import javax.management.NotCompliantMBeanException;
 import javax.management.ObjectName;
 import javax.management.ReflectionException;
 
 
 
 /**
  * {@code FileWeb} is a small web server for publishing a directory tree over (local) net.
  * Web root directory will be the directory where {@code FileWeb} was started.
  *  
  * @author <a href="http://www.hapiware.com" target="_blank">hapi</a>
  *
  */
 public class FileWeb
 {
 	private final static Logger LOGGER = Logger.getLogger(FileWeb.class.getName());
 	
 	private final static int CONNECTION_TIMEOUT_MS = 2000;
 	private final static int DEFAULT_PORT = 80;
 	private final static int DEFAULT_NUMBER_OF_THREADS = 20;
 	private final static int MAX_NUMBER_OF_THREADS = 100;
 	private final static String CONTROLLING_NAME = "com.hapiware.http:type=Controlling";
 
 	
 	private final ExecutorService _executorService;
 	private final Controller _controller;
 	private final int _port;
 	
 	
 	public static void main(String[] args)
 	{
 		if(args.length > 2)
 			usageAndExit(-1);
 
		if(
			args[0].equalsIgnoreCase("-?") ||
			args[0].equalsIgnoreCase("-h") ||
			args[0].equalsIgnoreCase("-help") ||
			args[0].equalsIgnoreCase("--help")
		)
			usageAndExit(0);
		
		if(args[0].equalsIgnoreCase("--version"))
			showVersionAndExit();
 		
 		int port = DEFAULT_PORT;
 		int numberOfThreads = DEFAULT_NUMBER_OF_THREADS;
 		if(args.length == 1 || args.length == 2) {
 			try {
 				port = Integer.parseInt(args[0]);
 			}
 			catch(NumberFormatException e) {
 				LOGGER.severe("Start parameter '" + args[0] + "' was not recognised as a port number.");
 				usageAndExit(1);
 			}
 		}
 		if(args.length == 2) {
 			try {
 				numberOfThreads = Integer.parseInt(args[1]);
 			}
 			catch(NumberFormatException e) {
 				LOGGER.severe("Start parameter '" + args[1] + "' was not recognised as a number.");
 				usageAndExit(1);
 			}
 		}
 		if(numberOfThreads < 1 || numberOfThreads > MAX_NUMBER_OF_THREADS) {
 			numberOfThreads = DEFAULT_NUMBER_OF_THREADS;
 			LOGGER.warning(
 				"Number of threads must be something between 1 - " + MAX_NUMBER_OF_THREADS
 					+ ". Using " + DEFAULT_NUMBER_OF_THREADS + "."
 			);
 		}
 		
 		try {
 			registerAndStartFileWeb(port, numberOfThreads);
 		}
 		catch(Throwable t) {
 			LOGGER.log(Level.SEVERE, "Cannot start fileweb", t);
 			System.exit(1);
 		}
 		
 		LOGGER.info("Bye bye.");
 	}
 	
 	private static void showVersionAndExit()
 	{
 		System.out.println(
 			"  Version: " + FileWeb.class.getPackage().getImplementationVersion()
 		);
 		System.exit(0);
 	}
 
 	private static void usageAndExit(int status)
 	{
 		final String fileWeb = "java -jar fileweb.jar";
 		System.out.println("Description: A small and customisable web server for publishing a directory tree");
 		System.out.println("             over (local) net. Web root directory will be the directory where");
 		System.out.println("             'fileweb' was started.");
 		System.out.println();
 		System.out.println("Usage: " + fileWeb + " [-? | -h | -help | --help | --version]");
 		System.out.println("       " + fileWeb + " [PORT [NUM_OF_THREADS]]");
 		System.out.println();
 		System.out.println("       PORT:");
 		System.out.println("           A port number. Default port is " + DEFAULT_PORT + ".");
 		System.out.println();
 		System.out.println("       NUM_OF_THREADS:");
 		System.out.println("           Maximum number of threads in the thread pool (1 - " + MAX_NUMBER_OF_THREADS + ").");
 		System.out.println("           Default number of threads is " + DEFAULT_NUMBER_OF_THREADS + ".");
 		System.out.println();
 		System.out.println("Examples:");
 		System.out.println("    " + fileWeb + " -?");
 		System.out.println("    " + fileWeb);
 		System.out.println("    " + fileWeb + " 50001");
 		System.out.println("    " + fileWeb + " 50001 35");
 		System.out.println();
 		System.exit(status);
 	}
 	
 	private static void registerAndStartFileWeb(int port, int numberOfThreads)
 		throws
 			InstanceAlreadyExistsException,
 			NotCompliantMBeanException,
 			MalformedObjectNameException,
 			ReflectionException,
 			NullPointerException,
 			InstanceNotFoundException,
 			MBeanException
 	{
 		FileWeb fileWeb = new FileWeb(port, numberOfThreads);
 		MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
 		ObjectName ControllingName = new ObjectName(CONTROLLING_NAME);
 		beanServer.createMBean(
 			"com.hapiware.http.jmx.Controlling",
 			ControllingName,
 			new Object[] { fileWeb.getController() },
 			new String[] { fileWeb.getController().getClass().getName() }
 		);
 		fileWeb.start();
 	}
 
 	
 	public FileWeb(Integer port, Integer numberOfThreads)
 	{
 		_port = port;
 		_executorService = Executors.newFixedThreadPool(numberOfThreads);
 		_controller = new Controller(this);
 		addHooks();
 	}
 	
 	private void addHooks()
 	{
 		Runtime.getRuntime().addShutdownHook(
 			new Thread() {
 				public void run()
 				{
 					_executorService.shutdown();
 					System.out.println("fileweb shutdown.");
 				}
 			}
 		);
 	}
 	
 	
 	private static void handleRequest(Socket socket) throws IOException
 	{
 		int retries = 60;
 		InputStream is = socket.getInputStream();
 		while(
 			retries > 0
 			&& socket.isConnected()
 			&& !socket.isOutputShutdown() 
 			&& !socket.isInputShutdown()
 		) {
 			if(is.available() > 0) {
 				HttpRequest request = new HttpRequest(is);
 				HttpResponse response = new HttpResponse(request);
 				response.write(socket.getOutputStream());
 				break;
 			}
 			else {
 				retries--;
 				try {
 					Thread.sleep(500);
 				}
 				catch(InterruptedException e) {
					// Does nothing.
 				}
 			}
 		}
 	}
 	
 	private void start()
 	{
 		LOGGER.info("fileweb started.");
 		try {
 			ServerSocket serverSocket = new ServerSocket(_port);
 			serverSocket.setSoTimeout(CONNECTION_TIMEOUT_MS);
 			while(!_executorService.isShutdown()) {
 				try {
 					final Socket socket = serverSocket.accept();
 					_executorService.execute(
 						new Runnable() {
 							public void run()
 							{
 								try {
 									_controller.enter();
 									handleRequest(socket);
 								}
 								catch(SocketException e) {
 									LOGGER.log(Level.FINE, "Protocol problem.", e);
 								}
 								catch(RuntimeException e) {
 									_controller.registerError();
 									throw e;
 								}
 								catch(Throwable t) {
 									_controller.registerError();
 									LOGGER.log(Level.WARNING, "", t);
 								}
 								finally {
 									_controller.exit();
 									try {
 										socket.close();
 									}
 									catch(IOException e) {
 										// Does nothing.
 									}
 								}
 							}
 						}
 					);
 				}
 				catch(RejectedExecutionException e) {
 					if(!_executorService.isShutdown())
 						LOGGER.log(Level.SEVERE, "New task is not accepted.", e);
 				}
 				catch(SocketTimeoutException e) {
 					// Does nothing except allows ExecutorService to call shutdown().
 				}
 			}
 		}
 		catch(IOException e) {
 			LOGGER.log(Level.SEVERE, "Socket failed", e);
 		}
 	}
 	
 	public void stop()
 	{
 		_executorService.shutdown();
 		LOGGER.info("fileweb stopped.");
 	}
 
 	public Controller getController()
 	{
 		return _controller;
 	}
 }
