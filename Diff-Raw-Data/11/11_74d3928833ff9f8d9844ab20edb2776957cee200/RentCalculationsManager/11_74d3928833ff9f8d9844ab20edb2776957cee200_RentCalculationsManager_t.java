 package me.krotn.Rent;
 
 import java.util.ArrayList;
 
 /**
  * This class manages the various calculations required for the Rent plugin.
  *
  */
 public class RentCalculationsManager {
 	Rent plugin;
 	RentDatabaseManager dbMan;
 	RentLogManager logMan;
 	RentDateUtils dateUtils;
 	
 	/**
 	 * Constructs a RentCalculationsManager.
 	 * @param plugin The {@code Rent} plugin object used to get the various other objects 
 	 * needed to calculated the required values.
 	 */
 	public RentCalculationsManager(Rent plugin){
 		this.plugin = plugin;
 		this.dbMan = this.plugin.getDatabaseManager();
 		this.logMan = this.plugin.getLogManager();
 		this.dateUtils = this.plugin.getDateUtils();
 	}
 	
 	/**
 	 * Returns the amount an individual player would owe had they logged in during the given month.
 	 * @param monthID The {@code int} database ID of the requested month.
 	 * @return The {@code double} amount a player would owe for logging in during the given month.
 	 */
 	public double getIndividualRate(int monthID){
 		if(!dbMan.monthExists(dbMan.getMonthFromID(monthID))){
 			return 0;
 		}
 		double monthlyCost = dbMan.getMonthCost(monthID);
		boolean fixedRent = new Boolean(plugin.getPropertiesManager().getProperty("fixedRent")).booleanValue();
		if(!fixedRent){
			int numPlayersWhoLoggedIn = dbMan.getPlayersWhoLoggedIn(monthID).size();
			return monthlyCost/numPlayersWhoLoggedIn;
		}
		else{
			return monthlyCost;
		}
 	}
 	
 	/**
 	 * Returns the total amount a player owes. This <b>DOES NOT</b> include the current month in these calculations.
 	 * @param playerID The {@code int} database ID number of the requested player.
 	 * @return The total amount the player owes (could include {@code 0} or negative values) or {@code 0} if an the requested player does not exist.
 	 */
 	public double getAmountPlayerOwes(int playerID){
 		if(!dbMan.playerExists(dbMan.getPlayerFromID(playerID))){
 			return 0;
 		}
 		return getPlayerCost(playerID) - dbMan.getPlayerPayments(playerID);
 	}
 	
 	/**
 	 * Returns the total amount a player owes to the server, not including the amount they have already paid and <b>NOT INCLUDING</b> the current month.
 	 * @param playerID The {@code int} database ID of the requested player.
 	 * @return The {@code double} amount the player owes the server, before including the amount they have paid.
 	 */
 	public double getPlayerCost(int playerID){
 		if(!dbMan.playerExists(dbMan.getPlayerFromID(playerID))){
 			return 0;
 		}
 		String currentMonth = dateUtils.getCurrentMonth();
 		int currentMonthID = dbMan.getMonthID(currentMonth);
 		ArrayList<Integer> playerLogins = dbMan.getMonthsPlayerLoggedIn(playerID);
 		playerLogins.remove(new Integer(currentMonthID));
 		while(playerLogins.contains(new Integer(currentMonthID))){
 			playerLogins.remove(new Integer(currentMonthID));
 		}
 		double sum = 0.0;
 		for(Integer monthIDNum:playerLogins){
 			sum+=getIndividualRate(monthIDNum);
 		}
 		return sum;
 	}
 }
