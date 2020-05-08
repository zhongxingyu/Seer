 package server;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 import com.sun.net.httpserver.HttpServer;
 
 public class ClientManager implements HttpHandler {
 
 	protected ServerControl controller;
 	protected String[] paths;
 
 	protected ClientManager(ServerControl controller, String[] paths) {
 		
 		this.controller = controller;
 		this.paths = paths;
 
 		// Try to initiate server here!
 		System.out.println("Server attempting to open port: " + controller.port); // Debugging
 		
 		try {
 			HttpServer server = HttpServer.create(new InetSocketAddress(controller.port), 0);
 
 			for (int i = 0; i < paths.length; i++) {
 				server.createContext(paths[i], this);
 			}
 
 			server.setExecutor(null);
 			server.start();
 
 			System.out.println("Server succesfully opened port: " + controller.port); // Debugging
 			System.out.println("---------------------------------------"); // Debugging
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public String update(String path, String args) {
 		for (int i = 0; i < paths.length; i++) {
 			if (path.equalsIgnoreCase(paths[i])) {
 				return controller.gamemanager.callFunction(path, args);
 			}
 		}
 
 		return path + " is not proper.";
 	}
 
 	@Override
 	public void handle(HttpExchange exchange) {
 		InputStream inputStream = exchange.getRequestBody();
 
 		try {
 			int curData = inputStream.read();
 			String args = "";
 			while (curData != -1) {
 				args += (char) curData;
 				curData = inputStream.read();
 			}
 
 			String path = exchange.getHttpContext().getPath();
 			String response = update(path, args);
 			
 			// DEGUGGING //
 			
 			System.out.println("Path From Client: " + path);
 			System.out.println("Data From Client: " + args);
 			System.out.println("Data To Client: " + response);
 			System.out.println("---------------------------------------");
 			
 			// END //
 
 			exchange.sendResponseHeaders(200, response.length());
 
 			OutputStream outputStream = exchange.getResponseBody();
 			outputStream.write(response.getBytes());
 			outputStream.close();
			exchange.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
