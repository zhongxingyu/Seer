 /**
  *
  */
 package edu.hm.iny.netzwerke.blatt2.hmLogoProxy;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * @author Stff
  */
 public class ImageProxyServer {
 
 	/** Konstante fuer den Standard-Port beim TargetHost. */
 	private static final int STD_TARGET_PORT = 80;
 
 	/** Konstante fuer den Port des Proxys. */
 	private static final int PROXY_PORT = 8082;
 
 	private final String targetHost;
 
 	private final int targetPort;
 
 	// ///////////////////////////////// C T O R //////////////////////////////////////////
 
 	/**
 	 * @param targetHost
 	 * @param targetPort
 	 */
 	ImageProxyServer(final String targetHost, final int targetPort) {
 
 		this.targetHost = targetHost;
 		this.targetPort = targetPort;
 	}
 
 	/**
 	 * @param targetHost
 	 */
 	ImageProxyServer(final String targetHost) {
 		this(targetHost, STD_TARGET_PORT);
 	}
 
 	/**
 	 * @param ignored
 	 * @throws IOException
 	 */
 	void handleConnections() throws IOException {
 
 		try (final ServerSocket serverSocket = new ServerSocket(PROXY_PORT)) {
 
 			while (true) {
 
 				System.err.println("*** HM Logo Advertising-Server now running on port " + PROXY_PORT);
 
 				// Standard-IO/Socket-Voodoo fuer Rolle als Server.
 				try (final Socket socket = serverSocket.accept();
 						final BufferedReader fromClient = new BufferedReader(new InputStreamReader(
 								socket.getInputStream()));
 						final PrintWriter toClient = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()))) {
 
 					System.err.println("*** Oh! It seems we have a visitor... ");
 
 					// Standard-IO/Socket-Voodoo fuer Rolle als Client.
 					try (final Socket targetSocket = new Socket(InetAddress.getByName(targetHost), targetPort);
 							final BufferedReader fromServer = new BufferedReader(new InputStreamReader(
 									targetSocket.getInputStream()));
 							final PrintWriter toServer = new PrintWriter(new OutputStreamWriter(
 									targetSocket.getOutputStream()))) {
 
 						System.err.printf("*** Connection to %s on port %d established" + System.lineSeparator(),
 								targetHost, targetPort);
 
 						sendHTTPRequest(fromClient, toServer);
 						receiveResponseHeader(fromServer);
 
 						// Einlesen und Manipulation des response bodies.
 						final HTTPResponseManipulator responseManipulator = new HTTPResponseManipulator(
 								receiveResponseBody(fromServer));
 
 						sendTamperedResponse(responseManipulator.getTamperedResponse(), toClient);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param fromClient
 	 * @throws IOException
 	 */
 	private List<String> readRequestHeader(final BufferedReader fromClient) throws IOException {
 
 		final List<String> requestFromClient = new ArrayList<String>();
 
 		System.err.printf(System.lineSeparator() + "*** Sending request to %s on Port %d:" + System.lineSeparator(),
 				targetHost, targetPort);
 
		for (String line = fromClient.readLine(); line.length() > 0; line = fromClient.readLine()) {
 			requestFromClient.add(line);
 			System.err.println(line);
 		}
 
 		return requestFromClient;
 	}
 
 	/**
 	 * Methode zum Abfangen und Ausgeben des Response-Headers vom Server. Der Header wird nicht weiter beachtet.
 	 * @param fromServer
 	 * @throws IOException
 	 */
 	private void receiveResponseHeader(final BufferedReader fromServer) throws IOException {
 
 		System.err.println("*** Receiving response from Host... ");
 
 		for (String line = fromServer.readLine(); line.length() > 0; line = fromServer.readLine()) {
 			System.err.println(line);
 		}
 	}
 
 	/**
 	 * @param fromServer
 	 * @return
 	 * @throws IOException
 	 */
 	private List<String> receiveResponseBody(final BufferedReader fromServer) throws IOException {
 
 		final List<String> responseBody = new ArrayList<String>();
 
 		for (String line = fromServer.readLine(); line != null; line = fromServer.readLine()) {
 
 			responseBody.add(line);
 			// System.err.println(line);
 		}
 
 		return responseBody;
 	}
 
 	/**
 	 * Sends an HTTP Request to a globally defined server.
 	 * @param toServer A PrintWriter object for output to server.
 	 * @throws IOException
 	 */
 	private void sendHTTPRequest(final BufferedReader fromClient, final PrintWriter toServer) throws IOException {
 
 		final RequestParser requestParser = new RequestParser(readRequestHeader(fromClient));
 		final String request = "GET " + requestParser.getRelativeAddress() + " HTTP/1.1" + System.lineSeparator()
 				+ "Host: " + targetHost + System.lineSeparator() + System.lineSeparator();
 
 		toServer.println(request);
 		toServer.flush();
 	}
 
 	/**
 	 * Methode zum Rausschicken des manipulierten Response an den Client.
 	 * @param manipulatedResponse
 	 * @param toClient
 	 */
 	private void sendTamperedResponse(final List<String> manipulatedResponse, final PrintWriter toClient) {
 
 		// Rausschicken des manipulierten Response Headers in der Rolle des Servers an den
 		// urspruenglichen Client.
 		final Iterator<String> manResponseCursor = manipulatedResponse.iterator();
 
 		System.err.println("*** Sending tampered HTTP response to Client... ");
 
 		while (manResponseCursor.hasNext()) {
 
 			final String line = manResponseCursor.next();
 
 			toClient.println(line);
 			System.err.println(line);
 			toClient.flush();
 		}
 
 		toClient.println(System.lineSeparator());
 		toClient.flush();
 	}
 }
