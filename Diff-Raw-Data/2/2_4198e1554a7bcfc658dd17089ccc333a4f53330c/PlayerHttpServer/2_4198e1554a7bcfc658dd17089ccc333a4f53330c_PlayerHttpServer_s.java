 package Mike724.minecraft.WebMarket;
 import Mike724.minecraft.WebMarket.util.*;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.util.Arrays;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.concurrent.Executors;
 import java.util.logging.Logger;
 
 import com.sun.net.httpserver.Headers;
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 import com.sun.net.httpserver.HttpServer;
 
 public class PlayerHttpServer {
 	private Logger log;
 	public void start() throws IOException  {
		InetSocketAddress addr = new InetSocketAddress(Settings.HTTPPORT);
 		HttpServer server = HttpServer.create(addr, 0);
 		server.createContext("/", new Handler());
 		server.setExecutor(Executors.newCachedThreadPool());
 		server.start();
 		log.info("WebMarket player HTTP server is listening on port " + Settings.HTTPPORT);
 	}
 }
 
 class Handler implements HttpHandler {
 
 	private static HashMap<String, String> GetPlayerData(String player) {
 		HashMap<String, String> Data = new HashMap<String, String>();
 		Data.put("name", player);
 		Data.put("balance", "0");
 		return Data;
 	}
 
 	public void handle(HttpExchange exchange) throws IOException {
 			Headers responseHeaders = exchange.getResponseHeaders();
 			responseHeaders.set("Content-Type", "text/plain");
 			exchange.sendResponseHeaders(200, 0);
 			OutputStream responseBody = exchange.getResponseBody();
 
 			String[] args = exchange.getRequestURI().toASCIIString().split("/");
 			args = Arrays.copyOfRange(args, 1, args.length);
 
 			if(args[1].equalsIgnoreCase(Settings.SECRETKEY)) {
 				HashMap<String,String> Data = GetPlayerData(args[0]);
 				String Response = "";
 				for (Iterator<Map.Entry<String, String>> i = Data.entrySet().iterator(); i.hasNext();) {
 					Map.Entry<String, String> hm = i.next();
 					Response += hm.getKey() + ":" + hm.getValue() + "\n";
 				}
 				responseBody.write(Response.getBytes());
 			}
 			else responseBody.write("invalid".getBytes());
 			responseBody.close();
 	}
 
 }
