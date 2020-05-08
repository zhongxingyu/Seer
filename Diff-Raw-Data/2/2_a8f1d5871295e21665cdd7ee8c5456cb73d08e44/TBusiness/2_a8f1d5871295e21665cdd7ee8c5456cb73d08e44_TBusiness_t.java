 package com.lebelw.Tickets;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.bukkit.command.CommandException;
 
 public class TBusiness {
 
 	static Tickets plugin;
 	public TBusiness(Tickets tickets) {
 		plugin = tickets;
 	}
 	public boolean addBusiness(String name,String owner,String ticketname) {
 		if (name == null && owner == null)
 			return false;
 		if (!plugin.checkIfPlayerExists(name))
 			plugin.createPlayerTicketAccount(name);
 		int playerid = plugin.getPlayerId(name);
 		if (playerid > -1){
 			String query = "INSERT INTO business(name,owner_id,ticketname) VALUES('"+ name +"','"+ playerid +"','"+ ticketname +"')";
 			if (Tickets.dbm.insert(query))
 				return true;
 			else
 				return false;
 		}else{
 			return false;
 		}
 		
 	}
 	public static int getBusinessId(String name){
		ResultSet result = Tickets.dbm.query("SELECT id FROM business WHERE name LIKE '%" + name + "%'");
     	try {
 			if (result != null  && result.next()){
 				return result.getInt("id");
 			}else
 				throw new CommandException("No business found!");
 		} catch (SQLException e) {
 			TLogger.warning(e.getMessage());
 		}
     	return -1;
 	}
 	public static ResultSet getBusinessList() {
 		ResultSet result = Tickets.dbm.query("SELECT * FROM business");
 		if (result != null)		
 			return result;
 		return null;
 	}
 }
