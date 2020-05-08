 package bll.admin;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import bll.utils.ImageParser;
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 import com.sun.net.httpserver.HttpServer;
 import dal.admin.IConfigsStore;
 import dal.admin.IImageStore;
 import dal.admin.Image;
 import dal.admin.StoreFactory;
 
 /**
  * 
  * This class is responsible for serving the clients 
  * with a JSON representation of the latest images stored in the DB.
  * It is also responsible for providing configuration
  * values like the slideshow delay.
  * @author Stian Sandves
  *
  */
 
 public class ImageServer {
 
 	private static final int PORT = 8000;
 	private static final int NUMBER_OF_IMAGES_TO_SERVE = 100;
 
 	private HttpServer server;
 
 	public ImageServer() {
 		try {
 			initServer();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void initServer() throws IOException {
 		ExecutorService excutor;
 		InetSocketAddress addr = new InetSocketAddress("localhost", PORT);
 		server = HttpServer.create(addr, 0);
 		server.createContext("/images", new ImagesHandler());
 		server.createContext("/config", new ConfigHandler());
 		excutor = Executors.newCachedThreadPool();
 		server.setExecutor(excutor);
 	}
 
 	public void start() {
 		server.start();
 	}
 
 	public void stop(int seconds) {
 		server.stop(seconds);
 	}
 
 	private class ImagesHandler implements HttpHandler {
 		public void handle(HttpExchange exchange) throws IOException {
 			String response = getJsonRepresentationOfLastImages(NUMBER_OF_IMAGES_TO_SERVE);
 			ImageServer.respondToClient(exchange, response);
 		}
 
 		private String getJsonRepresentationOfLastImages(int numberOfImages) {
 			IImageStore store = StoreFactory.getImageStore();
 			ArrayList<Image> lastImages = store.getLast(numberOfImages);
 			return ImageParser.getJsonFromImage(lastImages);
 		}
 	}
 
 	static class ConfigHandler implements HttpHandler {
 		public void handle(HttpExchange exchange) throws IOException {
 			String response = getJsonRepresentationOfConfig("slideshow_delay");
 			ImageServer.respondToClient(exchange, response);
 		}
 		
 		private String getJsonRepresentationOfConfig(String name) {
 			IConfigsStore store = StoreFactory.getConfigsStore();
 			String value = store.getConfig(name);
 			String json = "{\"" + name + "\": \"" + value + "\"}";
			System.out.println(json);
 			return json;
 		}
 	}
 	
 	public static void respondToClient(HttpExchange exchange, String json) throws IOException {
 		exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, json.length());
 		OutputStream os = exchange.getResponseBody();
 		os.write(json.getBytes());
 		os.close();
 	}
 }
