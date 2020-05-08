 package uk.thecodingbadgers.bConomy.config;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import org.bukkit.plugin.java.JavaPlugin;
 import uk.thecodingbadgers.bConomy.Global;
 import uk.thecodingbadgers.bConomy.account.Account;
 import uk.thecodingbadgers.bDatabaseManager.bDatabaseManager;
 import uk.thecodingbadgers.bDatabaseManager.bDatabaseManager.DatabaseType;
 
 public class DatabaseManager {
 	
 	private static boolean working = true;
 	
 	private static String transactions = "bConomy_Transactions";
 	
 	/** 
 	 * Sets up the database and loads in all the infomation in the table
 	 * 
 	 * @param plugin - the java plugin used to setup the database, used to get the offline player
 	 */
 	public static void setupDatabase(JavaPlugin plugin) {
 		
 		// creates the database instance
 		Global.m_database = bDatabaseManager.createDatabase(Config.m_dbInfo.dbname, Global.getPlugin(), Config.m_dbInfo.driver, Config.m_dbInfo.update);
 		
 		// login if sql 
 		if (Config.m_dbInfo.driver == DatabaseType.SQL) {
 			// logs in using values from the config
 			if (!Global.m_database.login(Config.m_dbInfo.host, Config.m_dbInfo.user, Config.m_dbInfo.password, Config.m_dbInfo.port)) {
 				working = false;
 				return;
 			}
 		}
 		
 		if (!Global.m_database.tableExists(Config.m_dbInfo.tablename)) {
 			
 			Global.outputToConsole("Could not find 'accounts' table, creating default now.");
 			
 			// creates the accounts table
 			String query = "CREATE TABLE " + Config.m_dbInfo.tablename + " (" +
 							"id INT," +
 							"username VARCHAR(64)," +
 							"balance DOUBLE" +
 							");";
 			
 			Global.m_database.query(query, true);
 		}
 		
 		// Create the tansactions table
 		if (!Global.m_database.tableExists(transactions)) {
 			
 			Global.outputToConsole("Could not find 'transactions' table, creating default now.");
 			
 			// creates the accounts table
 			String query = "CREATE TABLE `" + transactions + "` (" +
 							"`from` VARCHAR(64)," +
 							"`to` VARCHAR(64)," +
 							"`amount` DOUBLE," +
 							"`when` DOUBLE" +
 							");";
 			
 			Global.m_database.query(query, true);
 		}
 		
 		String query = "SELECT * FROM " + Config.m_dbInfo.tablename;
 		ResultSet result = Global.m_database.queryResult(query);
 		
 		if (result == null)
 			return;
 		
 		// load in the accounts
 		try {
 			while(result.next()) {
 				int id = result.getInt("id");
 				String name = result.getString("username");
 				double balance = result.getDouble("balance");
 				
 				// create the account and then add it to the array
 				Account account = new Account(id, name, balance);
 				Global.addAccout(account);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Add a account to the database
 	 * 
 	 * @param account the account to add
 	 */
 	public static void addAccount(Account account) {
 		
 		if (!working)
 			return;
 		
 		if (account == null)
 			return;
 		
 		String query = 	"INSERT INTO " + Config.m_dbInfo.tablename + " " +
 						"(`id`, `username`, `balance`) VALUES (" +
 						"'" + account.getId() + "', " +
 						"'" + account.getPlayerName() + "', " +
 						"'" + account.getBalance() + "');";
 				
 		Global.m_database.query(query, true);
 		
 		Global.outputToConsole("Account " + account.getPlayerName() + " has been added to the database.");
 	}
 	
 	/**
 	 * Update a account on the database
 	 * 
 	 * @param account the account to update
 	 */
 	public static void updateAccount(Account account){
 		
 		if (!working)
 			return;
 		
 		if (account == null)
 			return;
 		
 		String query = "UPDATE " + Config.m_dbInfo.tablename +
 				 " SET balance='" + account.getBalance() + "' " +
 				 "WHERE id='" + account.getId() + "';";
 		
 		Global.m_database.query(query);
  	}
 	
 	/**
 	 * Remove a account from the database
 	 * 
 	 * @param account to remove
 	 */
 	public static void removeAccount(Account account) {
 		
 		if (!working)
 			return;
 		
 		if (account == null)
 			return;
 		
 		String query = "DELETE FROM `" + Config.m_dbInfo.tablename + "` WHERE `id` = " + account.getId() + ";";
 		
 		Global.m_database.query(query, true);
 		Global.getAccounts().remove(account);		
 		Global.outputToConsole("Removed the account " + account.getPlayerName() +" [" + account.getId() + "]");
 	}
 	
 	/**
 	 * Execute a one off query 
 	 * 
 	 * @param query to execute
 	 * @return the results
 	 */
 	public static ResultSet executeQuery(String query) {
 		
 		if (!working)
 			return null;
 		
 		return Global.m_database.queryResult(query);
 	}
 
 	/**
 	 * Log a payment 
 	 * 
 	 * @param from Who is paying the money
 	 * @param to Who is receiving the money
 	 * @param amount The amount of money
 	 */
 	public static void logPayment(String from, String to, double amount) {
 		
 		if (!working)
 			return;
 		
 		Long time = System.currentTimeMillis();
 		
 		String query = "INSERT INTO " + transactions + " " +
 				"(`from`, `to`, `amount`, `when`) VALUES (" +
 				"'" + from + "', " +
 				"'" + to + "', " +
 				"'" + amount + "', " +
 				"'" + time + "');";
 		
 		Global.m_database.query(query);
 	}
 
 	public static ArrayList<String> getTransactions(String playerName) {
 		
 		ArrayList<String> playerTransactions = new ArrayList<String>();
 		
 		String query = "SELECT * FROM " + transactions +
 				" WHERE `from` = '" + playerName + "' OR `to` = '" + playerName + 
 				"' ORDER BY `when` DESC LIMIT 10";
 		
 		ResultSet result = Global.m_database.queryResult(query);
 		if (result != null) {
 		
 			try {
 				while(result.next()) {
 					String from = result.getString("from");
 					String to = result.getString("to");
 					double amount = result.getDouble("amount");
 					double time = result.getDouble("when");
 					
 					SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy HH:mm");
 			        String dateString = format.format(new Date((long)time));
 					
 					String action = "[" + dateString + "] " + from + " paid " + to + " " + Global.format(amount);
 					playerTransactions.add(action);
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			
 		}		
 		
 		return playerTransactions;
 	}
 
 	public static void getAccount(Account account) {
 		
		String query = "SELECT * FROM " + Config.m_dbInfo.tablename + " WHERE 'username'=" + account.getPlayerName();
 		ResultSet result = Global.m_database.queryResult(query);
 		
 		if (result == null)
 			return;
 		
 		// load in the accounts
 		try {
 			while(result.next()) {
 				double balance = result.getDouble("balance");
 				
 				// create the account and then add it to the array
 				account.setBalance(balance);	
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 	}
 }
