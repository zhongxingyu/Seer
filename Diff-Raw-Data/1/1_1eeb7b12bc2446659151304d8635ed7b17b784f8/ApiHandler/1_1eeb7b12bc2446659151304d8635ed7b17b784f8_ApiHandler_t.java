 package com.nexus.api;
 
 import java.lang.annotation.Annotation;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.google.common.collect.Lists;
 import com.nexus.NexusServer;
 import com.nexus.annotation.Authenticated;
 import com.nexus.annotation.PrivillegeRequired;
 import com.nexus.logging.NexusLog;
 import com.nexus.mysql.MySQLHelper;
 import com.nexus.mysql.TableList;
 import com.nexus.utils.Pair;
 import com.nexus.webserver.WebServerRequest;
 import com.nexus.webserver.WebServerResponse;
 import com.nexus.webserver.WebServerStatus;
 
 public class ApiHandler{
 	
 	private static List<Pair<String, Class<?>>> Handlers = Lists.newArrayList();
 	
 	public static void RegisterHandler(String path, Class<?> handler){
 		Handlers.add(new Pair<String, Class<?>>(path, handler));
 	}
 	
 	public static List<Pair<String, Class<?>>> GetHandlers(){
 		return Handlers;
 	}
 	
 	public static void HandleFunction(WebServerRequest Request, WebServerResponse Response, String... Data){
 		String path = String.format("/%s", Data[0]);
 		boolean isHandeled = false;
 		Iterator<Pair<String, Class<?>>> it = Handlers.iterator();
 		List<String> args = Lists.newArrayList();
		if(!path.endsWith("/")) path = String.format("%s/", path);
 		while(it.hasNext()){
 			Pair<String, Class<?>> e = it.next();
 			String key = e.GetValue1();
 			Pattern regex = Pattern.compile(key);
 			Matcher matcher = regex.matcher(path);
 			if(matcher.find()){
 				try{
 					if(((IApiHandler) e.GetValue2().newInstance()).getClass().isAnnotationPresent(Authenticated.class)){
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
 					if(((IApiHandler) e.GetValue2().newInstance()).getClass().isAnnotationPresent(PrivillegeRequired.class)){
 						Annotation a = ((IApiHandler) e.GetValue2().newInstance()).getClass().getAnnotation(PrivillegeRequired.class);
 						String Privilleges[] = ((PrivillegeRequired) a).value();
 						Connection conn = MySQLHelper.GetConnection();
 						Statement stmt = conn.createStatement();
 						ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM %s WHERE Role IN (SELECT Role FROM %s WHERE Username IN (SELECT User FROM %s WHERE Token='%s'))", TableList.TABLE_ROLES, TableList.TABLE_USERS, TableList.TABLE_SESSIONS, Request.GetParameter("token")));
 						
 						if(!rs.first()){
 							Response.SendHeaders(WebServerStatus.InternalServerError);
 							Response.SendError("Database Error");
 							Response.Close();
 							rs.close();
 							stmt.close();
 							conn.close();
 							return;
 						}
 						boolean HasPrivilleges = true;
 						for(String p : Privilleges){
 							try{
 								if(!rs.getBoolean(p)){
 									HasPrivilleges = false;
 								}
 							}catch(SQLException ex){
 								new RuntimeException("Unknown permission '" + p + "'").printStackTrace();
 								Response.SendHeaders(WebServerStatus.InternalServerError);
 								Response.SendError("Internal server error");
 								Response.Close();
 								return;
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
 					int i = 1;
 					while(true){
 						try{
 							String res = path.replaceAll(key, "$" + i);
 							if(res.equals("$" + i)) break;
 							args.add(res);
 							i++;
 						}catch(IndexOutOfBoundsException e1){
 							break;
 						}
 					}
 					((IApiHandler) e.GetValue2().newInstance()).Handle(Request, Response, args.toArray(new String[args.size()]));
 				}catch(SQLException ex){
 					Response.SendHeaders(WebServerStatus.InternalServerError);
 					Response.SendError("Database Error");
 					Response.Close();
 					return;
 				}catch(Exception e1){
 					Response.SendHeaders(WebServerStatus.InternalServerError);
 					Response.SendError("Internal server error");
 					Response.Close();
 					NexusLog.log("ApiHandler", Level.SEVERE, e1, "Exception in ApiHandler.HandleFunction:");
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
