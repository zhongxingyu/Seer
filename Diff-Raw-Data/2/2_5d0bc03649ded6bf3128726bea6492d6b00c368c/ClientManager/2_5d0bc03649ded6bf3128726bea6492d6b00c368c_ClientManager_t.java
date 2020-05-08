 package server;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 
 import client.GsonConverter;
 
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 import com.sun.net.httpserver.HttpServer;
 
 public class ClientManager implements HttpHandler {
 
 	protected ServerControl controller;
 	protected ArrayList<String> paths;
 
 	protected ClientManager(ServerControl controller, ArrayList<Object> paths) {
 		
 		this.controller = controller;
 		this.paths = new ArrayList<String>();
 		
 		for(Object path : paths) {
 			this.paths.add((String) path);
 		}
 		
 		System.out.println("Server attempting to open port: " + controller.PORT); // Debugging
 		
 		// Attempt to initiate a server
 		try {
 			HttpServer server = HttpServer.create(new InetSocketAddress(controller.PORT), 0);
 			
 			for(String path : this.paths) {
				server.createContext(path, this);
 			}
 
 			server.setExecutor(null);
 			server.start();
 			
 			// DEBUGGING //
 
 			System.out.println("Server succesfully opened port: " + controller.PORT);
 			System.out.println("---------------------------------------");
 			
 			// END //
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/** 
 	 * Runs the function that corresponds with the path
 	 * 
 	 * @param path The path used to find and run the function
 	 * 
 	 * @param gsonPack The arguments passed through to the function
 	 * 
 	 * @return String The packaged Object[] that has been converted to gson
 	 */
 	protected String update(String path, String gsonPack) {
 		
 		for(String comparePath : paths) {
 			if(path.equalsIgnoreCase(comparePath)) {
 				return controller.gamemanager.callFunction(gsonPack);
 			}
 		}
 		
 		return gsonPack;
 	}
 	
 	/**
 	 * Handles the request from the client
 	 * 
 	 * @param exchange Holds the stream in which the server
 	 * 		  can read and send data to the client
 	 */
 	public void handle(HttpExchange exchange) {
 		
 		InputStream inputStream = exchange.getRequestBody();
 
 		try {
 			int curData = inputStream.read();
 			String gsonPack = "";
 			while (curData != -1) {
 				gsonPack += (char) curData;
 				curData = inputStream.read();
 			}
 
 			String path = exchange.getHttpContext().getPath();
 			String response = update(path, gsonPack);
 			
 			// DEGUGGING //
 			
 			System.out.println("Path From Client: " + path);
 			System.out.println("Data From Client: " + gsonPack);
 			System.out.println("Data To Client: " + response);
 			System.out.println("---------------------------------------");
 			
 			// END //
 
 			exchange.sendResponseHeaders(200, response.length());
 
 			OutputStream outputStream = exchange.getResponseBody();
 			outputStream.write(response.getBytes());
 			exchange.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
