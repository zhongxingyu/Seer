 package me.greatman.Craftconomy.utils;
 
 import java.io.File;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.World;
 
 import com.sun.rowset.CachedRowSetImpl;
 
 import me.greatman.Craftconomy.Account;
 import me.greatman.Craftconomy.AccountHandler;
 import me.greatman.Craftconomy.Bank;
 import me.greatman.Craftconomy.Craftconomy;
 import me.greatman.Craftconomy.Currency;
 import me.greatman.Craftconomy.CurrencyHandler;
 import me.greatman.Craftconomy.ILogger;
 
 @SuppressWarnings("restriction")
 public class DatabaseHandler
 {
 	private static SQLLibrary database = null;
 	/**
 	 * Load the DatabaseHandler. Create the tables if needed
 	 * 
 	 * @param thePlugin The Craftconomy plugin
 	 * @return Success to everything or false.
 	 */
 	public static boolean load(Craftconomy thePlugin)
 	{
 		if (Config.databaseType.equalsIgnoreCase("SQLite") || Config.databaseType.equalsIgnoreCase("minidb"))
 		{
 			database = new SQLLibrary("jdbc:sqlite:" + Craftconomy.plugin.getDataFolder().getAbsolutePath() + File.separator
 					+ "database.db","","", DatabaseType.SQLITE);
 			if (!database.checkTable(Config.databaseAccountTable))
 			{
 				try
 				{
 					database.query("CREATE TABLE " + Config.databaseAccountTable + " ("
 							+ "id INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT,"
 							+ "username VARCHAR(30)  UNIQUE NOT NULL)", false);
 					ILogger.info(Config.databaseAccountTable + " table created!");
 				} catch (SQLException e)
 				{
 					ILogger.error("Unable to create the " + Config.databaseAccountTable + " table!");
 					return false;
 				}
 			}
 			if (!database.checkTable(Config.databaseCurrencyTable))
 			{
 				try
 				{
 					database.query("CREATE TABLE " + Config.databaseCurrencyTable + " ("
 							+ "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
 							+ "name VARCHAR(30) UNIQUE NOT NULL,"
 							+ "plural VARCHAR(30) NOT NULL,"
 							+ "minor VARCHAR(30) NOT NULL,"
 							+ "minorplural VARCHAR(30) NOT NULL)", false);
 					database.query("INSERT INTO " + Config.databaseCurrencyTable + "(name,plural,minor,minorplural) VALUES("
 							+ "'" + Config.currencyDefault + "',"
 							+ "'" + Config.currencyDefaultPlural + "',"
 							+ "'" + Config.currencyDefaultMinor + "'," 
 							+ "'" + Config.currencyDefaultMinorPlural + "')", false);
 					ILogger.info(Config.databaseCurrencyTable + " table created!");
 				} catch (SQLException e)
 				{
 					ILogger.error("Unable to create the " + Config.databaseCurrencyTable + " table!");
 					e.printStackTrace();
 					return false;
 				}
 
 			}
 			else
 			{
 				HashMap<String,Boolean> map = new HashMap<String,Boolean>();
 				map.put("plural", false);
 				map.put("minor", false);
 				map.put("minorplural", false);
 				
 				//We check if it's the latest version
 				ResultSet result;
 				try {
 					result = database.query("PRAGMA table_info(" + Config.databaseCurrencyTable + ")", true);
 					if (result != null)
 					{
 						while(result.next())
 						{
 							if (map.containsKey(result.getString("name")))
 							{
 								map.put(result.getString("name"), true);
 							}
 						}
 						if (map.containsValue(false))
 						{
 							ILogger.info("Updating " + Config.databaseCurrencyTable + " table");
 							if (!map.get("plural"))
 							{
 								database.query("ALTER TABLE " + Config.databaseCurrencyTable + " ADD COLUMN plural VARCHAR(30)", false);
 								ILogger.info("Column plural added in " + Config.databaseCurrencyTable + " table");
 							}
 							if (!map.get("minor"))
 							{
 								database.query("ALTER TABLE " + Config.databaseCurrencyTable + " ADD COLUMN minor VARCHAR(30)", false);
 								ILogger.info("Column minor added in " + Config.databaseCurrencyTable + " table");
 							}
 							if(!map.get("minorplural"))
 							{
 								database.query("ALTER TABLE " + Config.databaseCurrencyTable + " ADD COLUMN minorplural VARCHAR(30)", false);
								ILogger.info("Column minorplural added in " + Config.databaseCurrencyTable + " table");
 							}
 						}
 					}
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 				
 			}
 			if (!database.checkTable(Config.databaseCurrencyExchangeTable))
 			{
 				try
 				{
 					database.query("CREATE TABLE " + Config.databaseCurrencyExchangeTable + " ( "
 							+ "src VARCHAR ( 30 ) NOT NULL , " 
 							+ "dest VARCHAR ( 30 ) NOT NULL , "
 							+ "rate DOUBLE NOT NULL)", false);
 					ILogger.info(Config.databaseCurrencyExchangeTable + " table created!");
 				} catch (SQLException e)
 				{
 					ILogger.error("Unable to create the " + Config.databaseCurrencyExchangeTable + " table!");
 					return false;
 				}
 			}
 			if (!database.checkTable(Config.databaseBalanceTable))
 			{
 				try
 				{
 					database.query("CREATE TABLE " + Config.databaseBalanceTable + " ("
 							+ "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," 
 							+ "username_id INTEGER NOT NULL,"
 							+ "currency_id INTEGER NOT NULL," 
 							+ "worldName VARCHAR(30) NOT NULL,"
 							+ "balance DOUBLE NOT NULL)", false);
 					ILogger.info(Config.databaseBalanceTable + " table created!");
 				} catch (SQLException e)
 				{
 					ILogger.error("Unable to create the " + Config.databaseBalanceTable + " table!");
 					return false;
 				}
 			}
 			if (!database.checkTable(Config.databaseBankTable))
 			{
 				try
 				{
 					database.query("CREATE TABLE " + Config.databaseBankTable + " ("
 							+ "id INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT," 
 							+ "name VARCHAR(30)  UNIQUE NOT NULL,"
 							+ "owner VARCHAR(30) NOT NULL)", false);
 					ILogger.info(Config.databaseBankTable + " table created!");
 				} catch (SQLException e)
 				{
 					ILogger.error("Unable to create the " + Config.databaseBankTable + " table!");
 					return false;
 				}
 			}
 			if (!database.checkTable(Config.databaseBankBalanceTable))
 			{
 				try
 				{
 					database.query("CREATE TABLE " + Config.databaseBankBalanceTable + " ("
 							+ "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," 
 							+ "bank_id INTEGER NOT NULL,"
 							+ "currency_id INTEGER NOT NULL," 
 							+ "worldName VARCHAR(30) NOT NULL,"
 							+ "balance DOUBLE NOT NULL)", false);
 					ILogger.info(Config.databaseBankBalanceTable + " table created!");
 				} catch (SQLException e)
 				{
 					ILogger.error("Unable to create the " + Config.databaseBankBalanceTable + " table!");
 					return false;
 				}
 			}
 			if (!database.checkTable(Config.databaseBankMemberTable))
 			{
 				try
 				{
 					database.query("CREATE TABLE " + Config.databaseBankMemberTable + " ("
 							+ "bank_id INTEGER  NOT NULL," + "playerName VARCHAR(30) NOT NULL)", false);
 					ILogger.info(Config.databaseBankMemberTable + " table created!");
 				} catch (SQLException e)
 				{
 					ILogger.error("Unable to create the " + Config.databaseBankMemberTable + " table!");
 					return false;
 				}
 			}
 			ILogger.info("SQLite database loaded!");
 			return true;
 		}
 		else if (Config.databaseType.equalsIgnoreCase("mysql"))
 		{
 			database = new SQLLibrary("jdbc:mysql://" + Config.databaseAddress + ":" + Config.databasePort + "/"
 					+ Config.databaseDb, Config.databaseUsername, Config.databasePassword, DatabaseType.MYSQL);
 			if (!database.checkTable(Config.databaseAccountTable))
 			{
 				try
 				{
 					database.query("CREATE TABLE " + Config.databaseAccountTable + " ( "
 							+ "`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY ," 
 							+ "`username` VARCHAR( 30 ) NOT NULL "
 							+ ") ENGINE = InnoDB;", false);
 					ILogger.info(Config.databaseAccountTable + " table created!");
 				} catch (SQLException e)
 				{
 					ILogger.error("Unable to create the " + Config.databaseAccountTable + " table!");
 					return false;
 				}
 			}
 			if (!database.checkTable(Config.databaseBalanceTable))
 			{
 				try
 				{
 					database.query("CREATE TABLE " + Config.databaseBalanceTable + " ( "
 							+ "`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY ," 
 							+ "`username_id` INT NOT NULL ,"
 							+ "`currency_id` INT NOT NULL , " 
 							+ "`worldName` VARCHAR( 30 ) NOT NULL , "
 							+ "`balance` DOUBLE NOT NULL) ENGINE = InnoDB;", false);
 					ILogger.info(Config.databaseBalanceTable + " table created!");
 				} catch (SQLException e)
 				{
 					ILogger.error("Unable to create the " + Config.databaseBalanceTable + " table!");
 					return false;
 				}
 			}
 			if (!database.checkTable(Config.databaseCurrencyTable))
 			{
 				try
 				{
 					database.query("CREATE TABLE " + Config.databaseCurrencyTable + " ( "
 							+ "`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY , "
 							+ "`name` VARCHAR( 30 ) NOT NULL, "
 							+ "`plural` VARCHAR( 30 ) NOT NULL, "
 							+ "`minor` VARCHAR( 30 ) NOT NULL, "
 							+ "`minorplural` VARCHAR( 30 ) NOT NULL "
 							+ ") ENGINE = InnoDB;", false);
 					database.query("INSERT INTO " + Config.databaseCurrencyTable + "(name,plural,minor,minorplural) VALUES("
 							+ "'" + Config.currencyDefault + "',"
 							+ "'" + Config.currencyDefaultPlural + "',"
 							+ "'" + Config.currencyDefaultMinor + "'," 
 							+ "'" + Config.currencyDefaultMinorPlural + "')", false);
 					ILogger.info(Config.databaseCurrencyTable + " table created!");
 				} catch (SQLException e)
 				{
 					ILogger.error("Unable to create the " + Config.databaseCurrencyTable + " table!");
 					return false;
 				}
 			}
 			else
 			{
 				HashMap<String,Boolean> map = new HashMap<String,Boolean>();
 				map.put("plural", false);
 				map.put("minor", false);
 				map.put("minorplural", false);
 				ResultSet result;
 				
 				try {
 					result = database.query("SHOW COLUMNS FROM " + Config.databaseCurrencyTable, true);
 					while(result.next())
 					{
 						
 						if (map.containsKey(result.getString(1)))
 						{
 							map.put(result.getString(1), true);
 						}
 						
 					}
 					if (map.containsValue(false))
 					{
 						ILogger.info("Updating " + Config.databaseCurrencyTable + " table");
 						if (!map.get("plural"))
 						{
 							database.query("ALTER TABLE " + Config.databaseCurrencyTable + " ADD plural VARCHAR(30) NOT NULL", false);
 							ILogger.info("Column plural added in " + Config.databaseCurrencyTable + " table");
 						}
 						if (!map.get("minor"))
 						{
 							database.query("ALTER TABLE " + Config.databaseCurrencyTable + " ADD minor VARCHAR(30) NOT NULL", false);
 							ILogger.info("Column minor added in " + Config.databaseCurrencyTable + " table");
 						}
 						if(!map.get("minorplural"))
 						{
 							database.query("ALTER TABLE " + Config.databaseCurrencyTable + " ADD minorplural VARCHAR(30) NOT NULL", false);
 							ILogger.info("Column minorplural added in " + Config.databaseCurrencyTable + " table");
 						}
 					}
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					return false;
 				}
 			}
 			if (!database.checkTable(Config.databaseCurrencyExchangeTable))
 			{
 				try
 				{
 					database.query("CREATE TABLE " + Config.databaseCurrencyExchangeTable + " ( "
 							+ "`src` VARCHAR ( 30 ) NOT NULL , " 
 							+ "`dest` VARCHAR ( 30 ) NOT NULL , "
 							+ "`rate` DOUBLE NOT NULL " + ") ENGINE = InnoDB;", false);
 					ILogger.info(Config.databaseCurrencyExchangeTable + " table created!");
 				} catch (SQLException e)
 				{
 					ILogger.error("Unable to create the " + Config.databaseCurrencyExchangeTable + " table!");
 					return false;
 				}
 			}
 			if (!database.checkTable(Config.databaseBankTable))
 			{
 				try
 				{
 					database.query("CREATE TABLE " + Config.databaseBankTable + " ("
 							+ "`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY , " 
 							+ "`name` VARCHAR( 30 ) NOT NULL , "
 							+ "`owner` VARCHAR( 30 ) NOT NULL) ENGINE = InnoDB;", false);
 					ILogger.info(Config.databaseBankTable + " table created!");
 				} catch (SQLException e)
 				{
 					ILogger.error("Unable to create the " + Config.databaseBankTable + " table!");
 					return false;
 				}
 			}
 			if (!database.checkTable(Config.databaseBankBalanceTable))
 			{
 				try
 				{
 					database.query("CREATE TABLE " + Config.databaseBankBalanceTable + " ( "
 							+ "`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY ," 
 							+ "`bank_id` INT NOT NULL ,"
 							+ "`currency_id` INT NOT NULL , " 
 							+ "`worldName` VARCHAR( 30 ) NOT NULL , "
 							+ "`balance` DOUBLE NOT NULL) ENGINE = InnoDB;", false);
 					ILogger.info(Config.databaseBankBalanceTable + " table created!");
 				} catch (SQLException e)
 				{
 					ILogger.error("Unable to create the " + Config.databaseBankBalanceTable + " table!");
 					return false;
 				}
 			}
 			if (!database.checkTable(Config.databaseBankMemberTable))
 			{
 				try
 				{
 					database.query("CREATE TABLE " + Config.databaseBankMemberTable + " ("
 							+ "`bank_id` INT NOT NULL ," 
 							+ "`playerName` INT NOT NULL " + ") ENGINE = InnoDB;", false);
 					ILogger.info(Config.databaseBankMemberTable + " table created!");
 				} catch (SQLException e)
 				{
 					ILogger.error("Unable to create the " + Config.databaseBankMemberTable + " table!");
 					return false;
 				}
 			}
 			ILogger.info("MySQL table loaded!");
 			return true;
 		}
 		return false;
 	}
 
 	public static boolean exists(String account)
 	{
 		ResultSet result = null;
 		boolean exists = false;
 		String query = "SELECT * FROM " + Config.databaseAccountTable + " WHERE username='" + account + "'";
 		try
 		{
 			result = database.query(query, true);
 			if (result.next())
 				exists = true;
 		} catch (SQLException e)
 		{
 		}
 		return exists;
 	}
 
 	public static void create(String accountName)
 	{
 		String query = "INSERT INTO " + Config.databaseAccountTable + "(username) VALUES('" + accountName + "')";
 		try
 		{
 			database.query(query, false);
 			Account account = AccountHandler.getAccount(accountName);
 			query = "INSERT INTO " + Config.databaseBalanceTable
 					+ "(username_id,worldName,currency_id,balance) VALUES(" + account.getPlayerId() + "," + "'"
 					+ Craftconomy.plugin.getServer().getWorlds().get(0).getName() + "',"
 					+ getCurrencyId(Config.currencyDefault) + "," + Config.defaultHoldings + ")";
 			database.query(query, false);
 		} catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public static void deleteAll()
 	{
 		String query = "DELETE FROM " + Config.databaseAccountTable;
 		try
 		{
 			database.query(query, false);
 			query = "DELETE FROM " + Config.databaseBalanceTable;
 			database.query(query, false);
 			query = "DELETE FROM " + Config.databaseCurrencyTable;
 			database.query(query, false);
 			database.query("INSERT INTO " + Config.databaseCurrencyTable + "(name) VALUES('" + Config.currencyDefault
 					+ "')", false);
 			query = "DELETE FROM " + Config.databaseBankTable;
 			database.query(query, false);
 			query = "DELETE FROM " + Config.databaseBankBalanceTable;
 			database.query(query, false);
 		} catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 
 	}
 
 	public static void deleteAllInitialAccounts()
 	{
 		String query = "DELETE FROM " + Config.databaseAccountTable + " WHERE balance=" + Config.defaultHoldings;
 		try
 		{
 			database.query(query, false);
 		} catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public static ResultSet getAllInitialAccounts()
 	{
 		String query = "SELECT * FROM " + Config.databaseAccountTable + " WHERE balance=" + Config.defaultHoldings;
 		try
 		{
 			return database.query(query, true);
 		} catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	// TODO: Make that it verify if he is a bank owner
 	public static void delete(String playerName)
 	{
 		String query = "DELETE FROM " + Config.databaseAccountTable + " WHERE username='" + playerName + "'";
 		try
 		{
 			int accountId = getAccountId(playerName);
 			database.query(query, false);
 			if (accountId != 0)
 			{
 				query = "DELETE FROM " + Config.databaseBalanceTable + " WHERE username_id=" + accountId;
 				database.query(query, false);
 			}
 
 		} catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public static int getAccountId(String playerName)
 	{
 		int accountId = 0;
 		try
 		{
 			ResultSet result = database.query("SELECT id FROM " + Config.databaseAccountTable + " WHERE username='"
 					+ playerName + "'", true);
 			if (result != null)
 			{
 				result.next();
 				accountId = result.getInt("id");
 			}
 		} catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		return accountId;
 	}
 
 	public static String getAccountNameById(int id)
 	{
 		String accountName = null;
 		try
 		{
 			ResultSet result = database.query("SELECT username FROM " + Config.databaseAccountTable + " WHERE id="
 					+ id, true);
 			if (result != null)
 			{
 				result.next();
 				accountName = result.getString("username");
 			}
 		} catch (SQLException e)
 		{
 		}
 		return accountName;
 	}
 
 	/*
 	 * Update an account
 	 */
 
 	public static void updateAccount(Account account, double balance, Currency currency, World world)
 	{
 		String query = "SELECT id FROM " + Config.databaseBalanceTable + " WHERE " + "username_id="
 				+ account.getPlayerId() + " AND worldName='" + world.getName() + "'" + " AND currency_id="
 				+ currency.getdatabaseId();
 		CachedRowSetImpl result;
 		try
 		{
 			result = database.query(query, true);
 			if (result != null && result.size() != 0)
 			{
 				query = "UPDATE " + Config.databaseBalanceTable + " SET balance=" + balance + " WHERE username_id="
 						+ account.getPlayerId() + " AND worldName='" + world.getName() + "'" + " AND currency_id="
 						+ currency.getdatabaseId();
 				database.query(query, false);
 			}
 			else
 			{
 				query = "INSERT INTO " + Config.databaseBalanceTable
 						+ "(username_id,worldName,currency_id,balance) VALUES(" + account.getPlayerId() + "," + "'"
 						+ world.getName() + "'," + currency.getdatabaseId() + "," + balance + ")";
 				database.query(query, false);
 			}
 
 		} catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	/*
 	 * Balance functions
 	 */
 
 	/**
 	 * Get the default balance (When not MultiWorld)
 	 * 
 	 * @param account The account we want to get the default balance from.
 	 * @return The requested balance
 	 */
 	// public static double getDefaultBalance(Account account)
 	// {
 	// if (!Config.multiWorld)
 	// return getDefaultBalance(account,
 	// Craftconomy.plugin.getServer().getWorlds().get(0));
 	// return 0.00;
 	// }
 	/**
 	 * Get the default balance (When MultiWorld)
 	 * 
 	 * @param account The account we want to get the default balance from
 	 * @param worldName The world name
 	 * @return the requested balance
 	 */
 	/*
 	 * public static double getDefaultBalance(Account account, World world) {
 	 * return getBalanceCurrency(account, world.getName(),
 	 * CurrencyHandler.getCurrency(Config.currencyDefault, true)); }
 	 */
 	/**
 	 * Grab all balance from a account (When not multiWorld)
 	 * 
 	 * @param account The account we want to get the balance from
 	 * @return The result of the query or null if the system is MultiWorld
 	 *         enabled
 	 */
 	public static ResultSet getAllBalance(Account account)
 	{
 		String query = "SELECT balance,currency_id,worldName,Currency.name FROM " + Config.databaseBalanceTable
 				+ " LEFT JOIN " + Config.databaseCurrencyTable + " ON " + Config.databaseBalanceTable
 				+ ".currency_id = " + Config.databaseCurrencyTable + ".id WHERE username_id=" + account.getPlayerId()
 				+ " ORDER BY worldName";
 		try
 		{
 			ResultSet result = database.query(query, true);
 			if (result != null)
 			{
 				return result;
 			}
 		} catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * Get the balance of a account
 	 * 
 	 * @param account The account we want to get the balance
 	 * @param world The world that we want to check the balance
 	 * @param currency The currency we want to check
 	 * @return The balance
 	 */
 	public static double getBalanceCurrency(Account account, World world, Currency currency)
 	{
 		if (currency.getdatabaseId() != 0)
 		{
 			String query = "SELECT balance FROM " + Config.databaseBalanceTable + " WHERE username_id='"
 					+ account.getPlayerId() + "' AND worldName='" + world.getName() + "' AND currency_id="
 					+ currency.getdatabaseId();
 			ResultSet result;
 			try
 			{
 				result = database.query(query, true);
 				if (result != null)
 				{
 					if (!result.isLast())
 					{
 						result.next();
 						return result.getDouble("balance");
 					}
 				}
 			} catch (SQLException e)
 			{
 				e.printStackTrace();
 			}
 
 		}
 		return 0.00;
 	}
 
 	/*
 	 * Currency functions
 	 */
 
 	/**
 	 * Get the currency database ID
 	 * 
 	 * @param currency The currency we want to get the ID
 	 * @return The Currency ID
 	 */
 	public static int getCurrencyId(String currency)
 	{
 		if (currencyExist(currency, true))
 		{
 			String query = "SELECT id FROM " + Config.databaseCurrencyTable + " WHERE name='" + currency + "'";
 			ResultSet result;
 			try
 			{
 				result = database.query(query, true);
 				if (result != null)
 				{
 					result.next();
 					return result.getInt("id");
 				}
 			} catch (SQLException e)
 			{
 				e.printStackTrace();
 			}
 		}
 		return 0;
 	}
 
 	/**
 	 * Verify if a currency exists in the database
 	 * 
 	 * @param currency The currency name we want to check
 	 * @return True if the currency exists, else false
 	 */
 	public static boolean currencyExist(String currency)
 	{
 		return currencyExist(currency, false);
 	}
 
 	/**
 	 * Verify if a currency exist in the database
 	 * 
 	 * @param currency The currency we want to check
 	 * @param exact If we give the exact name or not
 	 * @return True if the currency exists, else false.
 	 */
 	public static boolean currencyExist(String currency, boolean exact)
 	{
 		String query;
 		if (exact)
 			query = "SELECT * FROM " + Config.databaseCurrencyTable + " WHERE name='" + currency + "'";
 		else query = "SELECT * FROM " + Config.databaseCurrencyTable + " WHERE name LIKE '%" + currency + "%'";
 		try
 		{
 			CachedRowSetImpl result = database.query(query, true);
 			if (result != null)
 			{
 				if (result.size() == 1)
 				{
 					return true;
 				}
 			}
 		} catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	// TODO: ???????????????????????
 	/**
 	 * Get the Currency full name
 	 * 
 	 * @param currencyName The currency name we want to get
 	 * @param exact If we give the exact name or not
 	 * @return The currency name
 	 */
 	public static String getCurrencyName(String currencyName, boolean exact)
 	{
 		String query;
 		if (exact)
 			query = "SELECT name FROM " + Config.databaseCurrencyTable + " WHERE name='" + currencyName + "'";
 		else query = "SELECT name FROM " + Config.databaseCurrencyTable + " WHERE name LIKE '%" + currencyName + "%'";
 		ResultSet result;
 		try
 		{
 			result = database.query(query, true);
 			if (result != null)
 			{
 				result.next();
 				return result.getString("name");
 			}
 		} catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static boolean createCurrency(String currencyName, String currencyNamePlural, String currencyMinor, String currencyMinorPlural)
 	{
 		boolean success = false;
 		if (!currencyExist(currencyName, true))
 		{
 			String query = "INSERT INTO " + Config.databaseCurrencyTable + "(name,plural,minor,minorplural) VALUES(" +
 									"'" + currencyName + "'," +
 									"'" + currencyNamePlural + "'," +
 									"'" + currencyMinor + "'," +
 									"'" + currencyMinorPlural + "',)";
 			try
 			{
 				database.query(query, false);
 				success = true;
 			} catch (SQLException e)
 			{
 				e.printStackTrace();
 			}
 		}
 		return success;
 
 	}
 
 	public static boolean modifyCurrency(CurrencyHandler.editType type, String oldCurrencyName, String newCurrencyName)
 	{
 		boolean success = false;
 		if (currencyExist(oldCurrencyName, true))
 		{
 			String query = "";
 			if (type == CurrencyHandler.editType.NAME)
 			{
 				CurrencyHandler.getCurrency(oldCurrencyName, true).setName(newCurrencyName);
 				query = "UPDATE " + Config.databaseCurrencyTable + " SET name='" + newCurrencyName
 						+ "' WHERE name='" + oldCurrencyName + "'";
 			}
 			else if (type == CurrencyHandler.editType.PLURAL)
 			{
 				CurrencyHandler.getCurrency(oldCurrencyName, true).setNamePlural(newCurrencyName);
 				query = "UPDATE " + Config.databaseCurrencyTable + " SET plural='" + newCurrencyName
 						+ "' WHERE name='" + oldCurrencyName + "'";
 			}
 			else if (type == CurrencyHandler.editType.MINOR)
 			{
 				CurrencyHandler.getCurrency(oldCurrencyName, true).setNameMinor(newCurrencyName);
 				query = "UPDATE " + Config.databaseCurrencyTable + " SET minor='" + newCurrencyName
 						+ "' WHERE name='" + oldCurrencyName + "'";
 			}
 			else if (type == CurrencyHandler.editType.MINORPLURAL)
 			{
 				CurrencyHandler.getCurrency(oldCurrencyName, true).setNameMinorPlural(newCurrencyName);
 				query = "UPDATE " + Config.databaseCurrencyTable + " SET minorplural='" + newCurrencyName
 						+ "' WHERE name='" + oldCurrencyName + "'";
 			}
 			 
 			try
 			{
 				database.query(query, false);
 				success = true;
 			} catch (SQLException e)
 			{
 				e.printStackTrace();
 			}
 		}
 		return success;
 	}
 
 	public static boolean removeCurrency(String currencyName)
 	{
 		boolean success = false;
 		if (currencyExist(currencyName, true))
 		{
 			String query2 = "DELETE FROM " + Config.databaseBalanceTable + " WHERE currency_id="
 					+ getCurrencyId(currencyName);
 			String query = "DELETE FROM " + Config.databaseCurrencyTable + " WHERE name='" + currencyName + "'";
 			try
 			{
 				database.query(query, false);
 
 				database.query(query2, false);
 				success = true;
 			} catch (SQLException e)
 			{
 				e.printStackTrace();
 			}
 
 		}
 		return success;
 	}
 
 	public static boolean bankExists(String bankName)
 	{
 		String query = "SELECT * FROM " + Config.databaseBankTable + " WHERE name='" + bankName + "'";
 		try
 		{
 			CachedRowSetImpl result = database.query(query, true);
 			if (result != null)
 			{
 				if (result.size() == 1)
 					return true;
 				else return false;
 			}
 		} catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 
 		return false;
 	}
 
 	public static void updateBankAccount(Bank bank, double balance, Currency currency, World world)
 	{
 		String query = "SELECT id FROM " + Config.databaseBankBalanceTable + " WHERE " + "bank_id=" + bank.getId()
 				+ " AND worldName='" + world.getName() + "'" + " AND currency_id=" + currency.getdatabaseId();
 		CachedRowSetImpl result;
 		try
 		{
 			result = database.query(query, true);
 			if (result != null && result.size() != 0)
 			{
 				query = "UPDATE " + Config.databaseBankBalanceTable + " SET balance=" + balance + " WHERE bank_id="
 						+ bank.getId() + " AND worldName='" + world.getName() + "'" + " AND currency_id="
 						+ currency.getdatabaseId();
 				database.query(query, false);
 			}
 			else
 			{
 				query = "INSERT INTO " + Config.databaseBankBalanceTable
 						+ "(bank_id,worldName,currency_id,balance) VALUES(" + bank.getId() + "," + "'"
 						+ world.getName() + "'," + currency.getdatabaseId() + "," + balance + ")";
 				database.query(query, false);
 			}
 
 		} catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public static String getBankOwner(String bankName)
 	{
 		if (bankExists(bankName))
 		{
 			String query = "SELECT owner FROM " + Config.databaseBankTable + " WHERE name='" + bankName + "'";
 			try
 			{
 				ResultSet result = database.query(query, true);
 				if (result != null)
 				{
 					result.next();
 					return result.getString("owner");
 				}
 			} catch (SQLException e)
 			{
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 
 	public static double getBankBalanceCurrency(Bank bank, World world, Currency currency)
 	{
 		if (bankExists(bank.getName()))
 		{
 			String query = "SELECT balance FROM " + Config.databaseBankBalanceTable + " WHERE bank_id='" + bank.getId()
 					+ "' AND worldName='" + world.getName() + "' AND currency_id=" + currency.getdatabaseId();
 			ResultSet result;
 			try
 			{
 				result = database.query(query, true);
 				if (result != null)
 				{
 					if (!result.isLast())
 					{
 						result.next();
 						return result.getDouble("balance");
 					}
 				}
 			} catch (SQLException e)
 			{
 				e.printStackTrace();
 			}
 		}
 		return 0.00;
 	}
 
 	public static int getBankId(String bankName)
 	{
 		String query = "SELECT id FROM " + Config.databaseBankTable + " WHERE name='" + bankName + "'";
 		try
 		{
 			ResultSet result = database.query(query, true);
 			if (result != null)
 			{
 				if (!result.isLast())
 				{
 					result.next();
 					return result.getInt("id");
 				}
 			}
 		} catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 
 		return 0;
 	}
 
 	public static boolean createBank(String bankName, String playerName)
 	{
 		boolean result = false;
 		String query = "INSERT INTO " + Config.databaseBankTable + "(name,owner) VALUES('" + bankName + "','"
 				+ playerName + "')";
 		try
 		{
 			database.query(query, false);
 			result = true;
 		} catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	public static boolean deleteBank(String bankName)
 	{
 		boolean result = false;
 		if (bankExists(bankName))
 		{
 			String query = "DELETE FROM " + Config.databaseBankTable + " WHERE name='" + bankName + "'";
 			try
 			{
 				database.query(query, false);
 				result = true;
 			} catch (SQLException e)
 			{
 				e.printStackTrace();
 			}
 		}
 		return result;
 	}
 
 	public static ResultSet getAllBankBalance(Bank bank)
 	{
 		String query = "SELECT balance,currency_id,worldName,Currency.name FROM " + Config.databaseBankBalanceTable
 				+ " LEFT JOIN " + Config.databaseCurrencyTable + " ON " + Config.databaseBankBalanceTable
 				+ ".currency_id = " + Config.databaseCurrencyTable + ".id WHERE bank_id=" + bank.getId()
 				+ " ORDER BY worldName";
 		try
 		{
 			ResultSet result = database.query(query, true);
 			if (result != null)
 			{
 				return result;
 			}
 		} catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static List<String> getBankMembers(Bank bank)
 	{
 		String query = "SELECT * FROM " + Config.databaseBankMemberTable + " WHERE bank_id = " + bank.getId();
 		List<String> list = new ArrayList<String>();
 		try
 		{
 			ResultSet result = database.query(query, true);
 			if (result != null)
 			{
 				while (result.next())
 				{
 					list.add(result.getString("playerName"));
 				}
 				return list;
 			}
 		} catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static List<String> listBanks()
 	{
 		String query = "SELECT name FROM " + Config.databaseBankTable;
 		List<String> list = new ArrayList<String>();
 		try
 		{
 			ResultSet result = database.query(query, true);
 			if (result != null)
 			{
 				while (result.next())
 				{
 					list.add(result.getString("name"));
 				}
 				return list;
 			}
 		} catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static void addBankMember(Bank bank, String playerName)
 	{
 		String query = "INSERT INTO " + Config.databaseBankMemberTable + " VALUES(" + bank.getId() + ",'" + playerName
 				+ "')";
 		try
 		{
 			database.query(query, false);
 		} catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public static void removeBankMember(Bank bank, String playerName)
 	{
 		String query = "DELETE FROM " + Config.databaseBankMemberTable + " WHERE bank_id=" + bank.getId()
 				+ " AND playerName='" + playerName + "'";
 		try
 		{
 			database.query(query, false);
 		} catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public static void setExchangeRate(Currency src, Currency dest, double rate)
 	{
 		// already in?
 		String query = "SELECT * FROM " + Config.databaseCurrencyExchangeTable + " WHERE src = '" + src.getName()
 				+ "' AND dest = '" + dest.getName() + "'";
 		try
 		{
 			ResultSet result = database.query(query, true);
 			if (result.next())
 			{
 				query = "UPDATE " + Config.databaseCurrencyExchangeTable + " SET rate=" + rate + " WHERE src='"
 						+ src.getName() + "' AND dest = '" + dest.getName() + "'";
 				database.query(query, false);
 			}
 			else
 			{
 				// create new
 				query = "INSERT INTO " + Config.databaseCurrencyExchangeTable + " (src, dest, rate) VALUES ('"
 						+ src.getName() + "','" + dest.getName() + "'," + String.valueOf(rate) + ")";
 				database.query(query, false);
 			}
 		} catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public static HashMap<String, Double> getExchangeRates(Currency a)
 	{
 		String query = "SELECT * FROM " + Config.databaseCurrencyExchangeTable + " WHERE src = '" + a.getName() + "'";
 		HashMap<String, Double> ret = new HashMap<String, Double>();
 		try
 		{
 			ResultSet result = database.query(query, true);
 			if (result != null)
 			{
 				while (result.next())
 				{
 					ret.put(result.getString("dest"), result.getDouble("rate"));
 				}
 			}
 		} catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	public static HashMap<String,String> getCurrencyNames(String currencyName, boolean exact) {
 		HashMap<String,String> map = new HashMap<String,String>();
 		String query;
 		if (exact)
 			query = "SELECT * FROM " + Config.databaseCurrencyTable + " WHERE name='" + currencyName + "'";
 		else query = "SELECT * FROM " + Config.databaseCurrencyTable + " WHERE name LIKE '%" + currencyName + "%'";
 		ResultSet result;
 		try
 		{
 			result = database.query(query, true);
 			if (result != null)
 			{
 				result.next();
 				map.put("name", result.getString("name"));
 				map.put("plural", result.getString("plural"));
 				map.put("minor", result.getString("minor"));
 				map.put("minorplural", result.getString("minorplural"));
 				return map;
 			}
 		} catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static List<Account> getTopList(Currency source, World world) {
 		String query = "SELECT * FROM " + Config.databaseBalanceTable +"" +
 				" LEFT JOIN " + Config.databaseAccountTable + " ON " + Config.databaseBalanceTable
 				+ ".username_id = " + Config.databaseAccountTable + ".id WHERE currency_id=" + source.getdatabaseId() + " AND worldName='" + world.getName() + "' ORDER BY balance DESC" ;
 		List<Account> accountList = new ArrayList<Account>();
 		try{
 			ResultSet result = database.query(query,true);
 			if (result != null)
 			{
 				int i = 0;
 				while(result.next() && i < 10)
 				{
 					accountList.add(AccountHandler.getAccount(result.getString("username")));
 					i++;
 				}
 			}
 		} catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		return accountList;
 	}
 }
