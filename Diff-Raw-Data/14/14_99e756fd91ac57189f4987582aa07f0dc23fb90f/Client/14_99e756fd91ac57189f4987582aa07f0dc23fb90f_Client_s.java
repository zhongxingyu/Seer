 package de.quui.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.Date;
 
 public class Client extends Thread implements IClient {
 	final public static String PONG = "PONG";
 	final public static String UTF8 = "UTF8";
 
 	protected volatile boolean running = true;
 
 	private BufferedReader _input;
 	private PrintWriter _output;
 	private String _id;
 	private long _connectionTime;
 	protected IClientDataHandler _handler;
 	protected Server _server;
 
 	public Client(Server server, Socket socket, IClientDataHandler handler, String id,
 			long connectionTime) {
 		_server = server;
 		_id = id;
 		_handler = handler;
 		_handler.setClient(this);
 		_connectionTime = connectionTime;
 
 		try {
 			initialize(socket);
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private void initialize(Socket socket) throws IOException {
 		_input = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF8));
 		_output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), UTF8));
 	}
 
 	final public void run() {
 		if (!running) {
 			return;
 		}
 
 		char buffer[] = new char[1];
 		int readResult = -1;
 
 		try {
 			do {
 				String data = "";
 				do {
 					readResult = _input.read(buffer, 0, 1);
 					data += buffer[0];
 				} while ((buffer[0] != 0) && (readResult != -1));
 
 				if (readResult != -1) {
 					data = data.replace("\u0000", "");
 
 					if(handleFlashSecurity(data)) {
 						continue;
 					}
 					_handler.onData(data);
 				}
 			} while (readResult != -1);
 		} catch (IOException e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 		destroy();
 	}
 
 	private boolean handleFlashSecurity(String data) {
 		if (data.indexOf("policy-file-request") != -1) {
 			send("<?xml version='1.0'?><cross-domain-policy><allow-access-from domain='*' to-ports='*'/></cross-domain-policy>");
 			return true;
 		} 
 		return false;
 	}
 
 	/**
 	 * send a message to the connected client
 	 * 
 	 * @param message
 	 */
 	final public void send(String message) {
 		try {
 			_output.print(message + '\u0000');
 			_output.flush();
 		} catch (Exception e) {
 			System.out.println("Client.send exeption " + e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	final public String getClientId() {
 		return _id;
 	}
 
 	/**
 	 * @return the time this client is connected to the server measured in seconds
 	 */
 	final public long getConnectionTime() {
 		long diff = new Date().getTime() - _connectionTime;
 		return ((diff / 60) / 60);
 	}
 	
 	/**
 	 * Shuts down the thread and cleans up used resources
 	 */
 	public void destroy() {
		System.out.println("shutting down client: "+getClientId());
		running = false;
		_server.removeClient(this);
		_server = null;
		_input = null;
		_output = null;
 	}
 }
