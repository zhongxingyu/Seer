 package rstrip;
 
 /*
  * HTTP server
  * 
  * based on code from site
  * http://kenny.deeprosoft.com/%D0%BF%D0%B8%D1%88%D0%B5%D0%BC-http-%D1%81%D0%B5%D1%80%D0%B2%D0%B5%D1%80-java/
  */
 
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.InetSocketAddress;
 import java.sql.SQLException;
 import java.util.Map;
 
 import rstrip.actions.Action;
 import rstrip.actions.poi.AddPOIAction;
 import rstrip.actions.poi.DeletePOIAction;
 import rstrip.actions.poi.EditPOIAction;
 import rstrip.actions.poi.GetPOIAction;
 import rstrip.utility.Database;
 import rstrip.utility.ParameterFilter;
 import rstrip.utility.Settings;
 
 import com.google.gson.Gson;
 import com.sun.net.httpserver.HttpServer;
 import com.sun.net.httpserver.HttpHandler;
 import com.sun.net.httpserver.HttpContext;
 import com.sun.net.httpserver.HttpExchange;
 
 public class Server implements HttpHandler {
 	
 	public static Settings settings;
 	public static Database database;
 	
     public static void main(String[] args) {
     	try {
     		System.setProperty("file.encoding", "UTF8");
     		
     		settings = new Settings();
         	database = Database.init();
 
         	HttpServer server = HttpServer.create(new InetSocketAddress(settings.getServerPort()), 0);
         	HttpContext context = server.createContext("/", new Server());
             context.getFilters().add(new ParameterFilter());
             server.start();
             System.out.println("Server started\nPress any key to stop");
             System.in.read();
             server.stop(0);
             System.out.println("server stoped");
             database.disconnect();
     	} catch (ClassNotFoundException e) {
			System.out.println("ERROR 01: ClassNotFoundException - "+ e.getMessage());
 		} catch (SQLException  e) {
			System.out.println("ERROR 02: SQLException - "+ e.getMessage());
 		} catch (IOException e) {
			System.out.println("ERROR 03: IOException - "+ e.getMessage());
 		}
     }
 
     public void handle(HttpExchange exc) {
     	try {
             exc.sendResponseHeaders(200, 0);
             Map parameters = (Map)exc.getAttribute("parameters");
             Object callback = parameters.get("callback");
             
             PrintWriter out = new PrintWriter(new OutputStreamWriter(exc.getResponseBody(), "UTF-8"));
 
             if (callback != null){
 
                 Gson gson = new Gson();
                 String json;
 
                 Object nameAction = parameters.get("action");
                 Action action = null;
                 
                 if (nameAction != null){
 	                if (nameAction.equals("add-poi")){
 	                    action = new AddPOIAction(parameters);
 	                }
 	                else if (nameAction.equals("edit-poi")){
 	                    action = new EditPOIAction(parameters);
 	                }
 	                else if (nameAction.equals("delete-poi")){
 	                	action = new DeletePOIAction(parameters);
 	                }
 	                else if (nameAction.equals("get-poi")){
 	                	action = new GetPOIAction(parameters);
 	                }
                 }
 
                 if (action != null) {
 	                if (!action.haveError()){
 	                	action.execute();
 	                }
 	            	
 	                if (!action.haveError()){
 	                	json = gson.toJson(action.getResult());
 	                }
 	                else {
 	                	json = gson.toJson(action.getError());
 	                }
 	                out.println(callback + "(" +json + ")");
                 }
                 else {
                 	out.println("ERROR 20 :: WRONG OR EMPTY ACTION");
                 }
             }
             else{
                 out.println("ERROR 30 :: PARAMETER IS NOT PASSED (callback)");
             }
             
             out.close();
             exc.close();
 		} catch (IOException e) {
			System.out.println("ERROR 05: IOException - "+ e.getMessage());
 			try {
 				database.disconnect();
 			} catch (SQLException e1) {
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
     }
 }
