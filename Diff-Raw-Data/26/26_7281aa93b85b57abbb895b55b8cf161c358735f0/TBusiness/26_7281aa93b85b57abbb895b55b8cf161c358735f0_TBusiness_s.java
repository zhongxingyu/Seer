 package com.lebelw.Tickets;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.bukkit.command.CommandException;
 
 public class TBusiness {
 
 	static Tickets plugin;
 	public TBusiness(Tickets tickets) {
 		plugin = tickets;
 	}
 	public static boolean addBusiness(String name,String owner,int lottery,int lotterychance,int lotteryitem) {
 		if (name == null && owner == null)
 			return false;
		if (!plugin.checkIfPlayerExists(name))
			plugin.createPlayerTicketAccount(name);
		int playerid = plugin.getPlayerId(name);
		if (playerid > -1){
			String query = "INSERT INTO business(name,owner_id,lottery,lotterychance,lotteryitem) VALUES('"+ name +"','"+ playerid +"','"+ lottery +"',"+lotterychance+","+lotteryitem+")";
			if (Tickets.dbm.insert(query))
				return true;
			else
				throw new CommandException("A error occured");
		}else{
			throw new CommandException("This user doesn't exist in the database. Should not see this.");
 		}
 		
 	}
 	public static int getBusinessId(String name){
 		ResultSet result = Tickets.dbm.query("SELECT id FROM business WHERE name LIKE '%" + name + "%'");
     	try {
 			if (result != null  && result.next()){
 				if (result.isLast())
 					return result.getInt("id");
 				else
 					throw new CommandException("More than 1 business found! Please type more characters");
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
 	public static int isLotteryBusiness(String name){
 		ResultSet result = Tickets.dbm.query("SELECT lottery FROM business WHERE name LIKE '%" + name + "%'");
     	try {
 			if (result != null  && result.next()){
 				if (result.isLast())
 					if (result.getInt("lottery") == 1)
 						return 1;
 					else
 						throw new CommandException("You can't use this business for lottery!");
 				else
 					throw new CommandException("More than 1 business found! Please type more characters");
 			}else
 				throw new CommandException("No business found!");
 		} catch (SQLException e) {
 			TLogger.warning(e.getMessage());
 	    	return -1;
 		}
 
 	}
 	public static int getBusinessLotteryChance(String name) {
 		ResultSet result = Tickets.dbm.query("SELECT lotterychance FROM business WHERE name LIKE '%" + name + "%'");
     	try {
 			if (result != null  && result.next()){
 				if (result.isLast())
 					return result.getInt("lotterychance");
 				else
 					throw new CommandException("More than 1 business found! Please type more characters");
 			}else
 				throw new CommandException("No business found!");
 		} catch (SQLException e) {
 			TLogger.warning(e.getMessage());
 	    	return -1;
 		}
 	}
 	public static int getBusinessLotteryItem(String name){
 		ResultSet result = Tickets.dbm.query("SELECT lotteryitem FROM business WHERE name LIKE '%" + name + "%'");
     	try {
 			if (result != null  && result.next()){
 				if (result.isLast())
 					return result.getInt("lotteryitem");
 				else
 					throw new CommandException("More than 1 business found! Please type more characters");
 			}else
 				throw new CommandException("No business found!");
 		} catch (SQLException e) {
 			TLogger.warning(e.getMessage());
 	    	return -1;
 		}
 	}
 	public static int getBusinessLotteryCost(String name){
 		ResultSet result = Tickets.dbm.query("SELECT lotterycost FROM business WHERE name LIKE '%" + name + "%'");
     	try {
 			if (result != null  && result.next()){
 				if (result.isLast())
 					return result.getInt("lotterycost");
 				else
 					throw new CommandException("More than 1 business found! Please type more characters");
 			}else
 				throw new CommandException("No business found!");
 		} catch (SQLException e) {
 			TLogger.warning(e.getMessage());
 	    	return -1;
 		}
 	}
 	public static boolean isBusinessOwner(String name, String businessname){
 		
 		ResultSet result = Tickets.dbm.query("SELECT lotterycost FROM business WHERE name LIKE '%" + businessname + "%' AND owner='"+name+"'");
     	try {
 			if (result != null  && result.next()){
 				if (result.isLast())
 					return true;
 				else
 					throw new CommandException("More than 1 business found! Please type more characters");
 			}else
 				throw new CommandException("No business found!");
 		} catch (SQLException e) {
 			TLogger.warning(e.getMessage());
 	    	return false;
 		}
 	}
 	public static boolean deleteBusiness(String businessname){
 		ResultSet result = Tickets.dbm.query("SELECT id FROM business WHERE name LIKE '%" + businessname + "%'");
     	try {
 			if (result != null  && result.next()){
 				if (result.isLast()){
 					int businessid = result.getInt("id");
 					String query = "DELETE FROM tickets WHERE business_id=" + businessid;
 					if (Tickets.dbm.execute(query)){
 						return Tickets.dbm.execute("DELETE FROM business WHERE id=" + businessid);
 					}else
 						throw new CommandException("A error occured while deleting existing tickets.");
 				}
 					
 				else
 					throw new CommandException("More than 1 business found! Please type more characters");
 			}else
 				throw new CommandException("No business found!");
 		} catch (SQLException e) {
 			TLogger.warning(e.getMessage());
 	    	return false;
 		}
 	}
 }
