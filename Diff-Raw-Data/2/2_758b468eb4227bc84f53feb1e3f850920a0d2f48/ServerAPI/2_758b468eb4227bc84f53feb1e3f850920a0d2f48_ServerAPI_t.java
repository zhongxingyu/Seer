 package net.robbytu.banjoserver.framework.api;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 
 import net.robbytu.banjoserver.framework.Main;
 import net.robbytu.banjoserver.framework.interfaces.Server;
 
 public class ServerAPI {
 	
 	/**
 	 * Fetch all servers and filter them with a statement, including their status and online player count
 	 * @return An array of servers
 	 */
 	public static Server[] getServers(String statement) {
 		// Init some vars
 		Connection conn = Main.conn;
 		ArrayList<Server> servers = new ArrayList<Server>();
 		
 		try {
 			// Create a new select statement
 			Statement select = conn.createStatement();
 			ResultSet result = select.executeQuery("SELECT servername, online, players FROM bs_servers" + statement);
 			
 			// For each server ...
 			while(result.next()) {
 				// Create a new Server instance
 				Server server = new Server();
 				
 				// Fill in properties
 				server.serverName = result.getString(1);
 				server.serverStatus = result.getInt(2);
 				server.serverPlayers = result.getInt(3);
 				
 				// Add server to return array
 				servers.add(server);
 			}
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		// Return the array of servers
 		return servers.toArray(new Server[servers.size()]);
 	}
 
     /**
      * Fetch a specific server
      * @param serverId ID of the server
      * @return Server object
      */
     public static Server getServer(int serverId) {
        Server[] servers = getServers(" WHERE id = '" + serverId + "'");
         return servers[0];
     }
 	
 
 	/**
 	 * Fetch all servers, including their status and online player count
 	 * @return An array of servers
 	 */
 	public static Server[] getServers() {
 		return getServers("");
 	}
 	
 	/**
 	 * Fetch all online servers, including their status and online player count
 	 * @return An array of servers
 	 */
 	public static Server[] getOnlineServers() {
 		return getServers(" WHERE online = 1");
 	}
 
 	/**
 	 * Fetch all offline servers, including their status and online player count
 	 * @return An array of servers
 	 */
 	public static Server[] getOfflineServers() {
 		return getServers(" WHERE online = 0");
 	}
 }
