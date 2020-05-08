 package io;
 
 import java.io.IOException;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.InetSocketAddress;
 import java.net.URI;
 import java.net.URLDecoder;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 import main.ModuleManager;
 
 import com.sun.net.httpserver.Filter;
 import com.sun.net.httpserver.HttpContext;
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 import com.sun.net.httpserver.HttpServer;
 
 import device.LEDcolor;
 import device.OutputLED;
 
 public class WebServer {
 	private ModuleManager manager;
 	private HttpServer server;
 	private String template;
 	private boolean stopServer = false;
 	
 	Map<String, Integer> prepareParams(String path){
 		String[] params=new String[]{};
 		if(path.split("\\?").length>1)
 			params=path.split("\\?")[1].split("&");
 		Map<String, Integer> result = new HashMap<String, Integer>();
 		result.put("id", 0);
 		result.put("r", 0);
 		result.put("g", 0);
 		result.put("b", 0);
 		result.put("d", 0);
 		for(String param : params){
 			String[] parts = param.split("=");
 			if(parts.length==2)
 				result.put(parts[0], Integer.parseInt(parts[1]));
 		}
 		return result;
 	}
 	
 	class Handler implements HttpHandler {
 		@Override
 		public void handle(HttpExchange exchange) throws IOException {
 			String path = exchange.getRequestURI().toASCIIString();
 			//System.out.print("new req: " + path + " -> ");
 			String response;
 			if (path.startsWith("/action")){
 				String module="constant";
 				if(path.split("\\/").length>2 && path.split("\\/")[2].split("\\?").length>0)
 					module=path.split("\\/")[2].split("\\?")[0];
 				//System.out.println("module: "+module);
 				Map<String, Integer> params = prepareParams(path);
 				manager.action(params.get("id"), module, params);
 				response = "";
 			}
 			else if (path.startsWith("/stop")) {
 				response = "stopping server";
 				stopServer = true;
			} else{
 				response = template;
 				if(manager.hasRGBmodule()){
					response.replaceAll("<%constantForm1%>",
 					"				<div id=\"control-r\"><input type=\"range\" name=\"slider\" id=\"slider-r\" value=\"<%currentR%>\" min=\"0\" max=\"255\" data-highlight=\"true\" /></div>"+
 					"				<div id=\"control-g\"><input type=\"range\" name=\"slider\" id=\"slider-g\" value=\"<%currentG%>\" min=\"0\" max=\"255\" data-highlight=\"true\" /></div>"+
 					"				<div id=\"control-b\"><input type=\"range\" name=\"slider\" id=\"slider-b\" value=\"<%currentB%>\" min=\"0\" max=\"255\" data-highlight=\"true\" /></div>");
					response.replaceAll("<%currentR%>", ((Integer)manager.getConnectionManager().getOutputLED(1).getR()).toString())
					response.replaceAll("<%currentG%>", ((Integer)manager.getConnectionManager().getOutputLED(1).getG()).toString())
					response.replaceAll("<%currentB%>", ((Integer)manager.getConnectionManager().getOutputLED(1).getB()).toString());
 				}else{
					response.replaceAll("<%constantForm1%>","");
 				}
 				if(manager.hasWhiteModule()){
					response.replaceAll("<%constantForm2%>",
 					"				<div id=\"control-d\"><input type=\"range\" name=\"slider\" id=\"slider-d\" value=\"255\" min=\"0\" max=\"255\" data-highlight=\"false\" /></div>");
 					//response.replaceAll("<%currentW%>", ((Integer)manager.getConnectionManager().getOutputLED(1).getR()).toString());
 				}else{
					response.replaceAll("<%constantForm2%>","");
 				}
 			}
 			exchange.sendResponseHeaders(200, response.length());
 			OutputStream os = exchange.getResponseBody();
 			os.write(response.getBytes());
 			os.close();
 		}
 	}
 
 	public void start(int port, ConnectionManager connection) {
 		manager=new ModuleManager(connection);
 		try {
 			StringBuilder text = new StringBuilder();
 		    String nl = System.getProperty("line.separator");
 		    Scanner scanner = new Scanner(new FileInputStream("resources/index.html"), "UTF-8");
 		    try {
 		    	while (scanner.hasNextLine())
 		    		text.append(scanner.nextLine() + nl);
 		    }
 		    finally{
 		    	scanner.close();
 		    }
 			template = text.toString();
 			server = HttpServer.create(new InetSocketAddress(port), 0);
 			server.createContext("/", new Handler());
 			server.setExecutor(null);
 			server.start();
 			System.out.println("Server running on port " + port);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void stop() {
 		server.stop(500);
 	}
 }
