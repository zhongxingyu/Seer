 package com.nexus.api;
 
 import java.lang.annotation.Annotation;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import com.nexus.MySQLHelper;
 import com.nexus.NexusServer;
 import com.nexus.annotation.Authenticated;
 import com.nexus.annotation.PrivillegeRequired;
 import com.nexus.webserver.WebServerRequest;
 import com.nexus.webserver.WebServerResponse;
 import com.nexus.webserver.WebServerStatus;
 
 
 public class ApiHandler{
 	
 	private static HashMap<String, Class<?>> Handlers = new HashMap<String, Class<?>>();
 	
 	public static void RegisterHandler(String path, Class<?> handler){
 		Handlers.put(path, handler);
 	}
 	
 	public static HashMap<String, Class<?>> GetHandlers(){
 		return Handlers;
 	}
 	
 	public static void HandleFunction(WebServerRequest Request, WebServerResponse Response){
 		String path = Request.Path.split("/api")[1];
 		if(path.startsWith("/")) path = path.substring(1);
 		if(path.endsWith("/"))   path = path.substring(0, path.length() - 1);
 		boolean isHandeled = false;
 		for(Entry<String, Class<?>> e : Handlers.entrySet()){
 			String key = e.getKey();
 			if(key.startsWith("/")) key = key.substring(1);
 			if(key.endsWith("/"))   key = key.substring(0, key.length() - 1);
 			if(key.startsWith(path) || key.endsWith(path) || key.equalsIgnoreCase(path)){
 				try{
 					if(((IApiHandler) e.getValue().newInstance()).getClass().isAnnotationPresent(Authenticated.class)){
 						if(NexusServer.Instance.AuthenticationManager.isTokenExpired(Request.GetParameter("token"))){
 							Response.SendHeaders(WebServerStatus.Unauthorized);
 							Response.SendError("Session token expired");
 							Response.Close();
 							return;
 						}
 						if(!NexusServer.Instance.AuthenticationManager.isTokenValid(Request.GetParameter("token"), Request.Address)){
 							Response.SendHeaders(WebServerStatus.Unauthorized);
 							Response.SendError("Invalid token");
 							Response.Close();
 							return;
 						}
 					}
 					if(((IApiHandler) e.getValue().newInstance()).getClass().isAnnotationPresent(PrivillegeRequired.class)){
 						Annotation a = ((IApiHandler) e.getValue().newInstance()).getClass().getAnnotation(PrivillegeRequired.class);
 						String Privilleges[] = ((PrivillegeRequired) a).value();
 						Connection conn = MySQLHelper.GetConnection();
 						Statement stmt = conn.createStatement();
 						ResultSet rs = stmt.executeQuery("SELECT * FROM roles WHERE Role IN (SELECT Role FROM users WHERE Username IN (SELECT User FROM sessions WHERE Token=\'" + Request.GetParameter("token") + "\'))");
 						rs.first();
 						boolean HasPrivilleges = true;
 						for(String p : Privilleges){
 							if(!rs.getBoolean(p)){
 								HasPrivilleges = false;
 							}
 						}
 						rs.close();
 						stmt.close();
 						conn.close();
 						if(!HasPrivilleges){
 							Response.SendHeaders(WebServerStatus.MethodNotAllowed);
 							Response.SendError("No permissions to execute this function");
 							Response.Close();
 							return;
 						}
 					}
 					((IApiHandler) e.getValue().newInstance()).Handle(Request, Response);
 				}catch(SQLException ex){
 					Response.SendHeaders(WebServerStatus.InternalServerError);
 					Response.SendError("Database Error");
 					Response.Close();
 					return;
 				}catch(Exception e1){
 					Response.SendHeaders(WebServerStatus.InternalServerError);
 					Response.SendError("Internal server error");
 					Response.Close();
					
					System.err.println("Exception in ApiHandler.HandleFunction:");
					e1.printStackTrace(System.err);
 					return;
 				}
 				isHandeled = true;
 			}
 		}
 		if(!isHandeled){
 			Response.SendHeaders(WebServerStatus.NotImplemented);
 			Response.SendError("Method not implemented");
 			Response.Close();
 			return;
 		}
 		Response.Close();
 	}
 }
