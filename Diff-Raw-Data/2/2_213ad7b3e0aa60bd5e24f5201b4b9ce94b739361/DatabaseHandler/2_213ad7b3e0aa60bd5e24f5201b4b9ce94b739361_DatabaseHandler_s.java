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
 	public static SQLLibrary getDatabase()
 	{
 		return database;
 	}
 	public static boolean load(Craftconomy thePlugin)
 	{
 		HashMap<String, FieldType> map = new HashMap<String, FieldType>();
 		
 		if (Config.databaseType.equalsIgnoreCase("SQLite") || Config.databaseType.equalsIgnoreCase("minidb"))
 		{
 			database = new SQLLibrary("jdbc:sqlite:" + Craftconomy.plugin.getDataFolder().getAbsolutePath() + File.separator
 					+ "database.db","","", DatabaseType.SQLITE);
 		}
 		else if (Config.databaseType.equalsIgnoreCase("MySQL"))
 		{
 			database = new SQLLibrary("jdbc:mysql://" + Config.databaseAddress + ":" + Config.databasePort + "/"
 					+ Config.databaseDb, Config.databaseUsername, Config.databasePassword, DatabaseType.MYSQL);
 		}
 		if (database != null)
 		{
 			if (!database.checkTable(Config.databaseAccountTable))
 			{
 				map.put("id", FieldType.PRIMARY);
 				map.put("username", FieldType.VARCHAR);
 				if (database.createTable(Config.databaseAccountTable, map))
 				{
 					ILogger.info(Config.databaseAccountTable + " table created!");
 				}
 				else
 				{
 					ILogger.error("Unable to create the " + Config.databaseAccountTable + " table!");
 					return false;
 				}
 			}
 			if (!database.checkTable(Config.databaseCurrencyTable))
 			{
 				map.clear();
 				map.put("id", FieldType.PRIMARY);
 				map.put("name", FieldType.VARCHAR);
 				map.put("plural", FieldType.VARCHAR);
 				map.put("minor", FieldType.VARCHAR);
 				map.put("minorplural", FieldType.VARCHAR);
 				if (database.createTable(Config.databaseCurrencyTable, map))
 				{
 					HashMap<String,String> insertMap = new HashMap<String, String>();
 					insertMap.put("name", Config.currencyDefault);
 					insertMap.put("plural", Config.currencyDefaultPlural);
 					insertMap.put("minor", Config.currencyDefaultMinor);
 					insertMap.put("minorplural", Config.currencyDefaultMinorPlural);
 					database.createEntry(Config.databaseCurrencyTable, insertMap);
 					ILogger.info(Config.databaseCurrencyTable + " table created!");
 				}
 				else
 				{
 					ILogger.error("Unable to create the " + Config.databaseCurrencyTable + " table!");
 					return false;
 				}
 
 			}
 			else
 			{
 				HashMap<String,Boolean> updateCheck = new HashMap<String,Boolean>();
 				updateCheck.put("plural", false);
 				updateCheck.put("minor", false);
 				updateCheck.put("minorplural", false);
 				
 				//We check if it's the latest version
 				ResultSet result;
 				if (database.getType() == DatabaseType.SQLITE)
 				{
 				    try {
 	                    result = database.query("PRAGMA table_info(" + Config.databaseCurrencyTable + ")", true);
 	                    if (result != null)
 	                    {
 	                        while(result.next())
 	                        {
 	                            if (updateCheck.containsKey(result.getString("name")))
 	                            {
 	                                updateCheck.put(result.getString("name"), true);
 	                            }
 	                        }
 	                        if (updateCheck.containsValue(false))
 	                        {
 	                            ILogger.info("Updating " + Config.databaseCurrencyTable + " table");
 	                            if (!updateCheck.get("plural"))
 	                            {
 	                                database.query("ALTER TABLE " + Config.databaseCurrencyTable + " ADD COLUMN plural VARCHAR(30)", false);
 	                                ILogger.info("Column plural added in " + Config.databaseCurrencyTable + " table");
 	                            }
 	                            if (!updateCheck.get("minor"))
 	                            {
 	                                database.query("ALTER TABLE " + Config.databaseCurrencyTable + " ADD COLUMN minor VARCHAR(30)", false);
 	                                ILogger.info("Column minor added in " + Config.databaseCurrencyTable + " table");
 	                            }
 	                            if(!updateCheck.get("minorplural"))
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
 				else
 				{
 				    try {
 	                    result = database.query("SHOW COLUMNS FROM " + Config.databaseCurrencyTable, true);
 	                    while(result.next())
 	                    {
 	                        
 	                        if (map.containsKey(result.getString(1)))
 	                        {
 	                            updateCheck.put(result.getString(1), true);
 	                        }
 	                        
 	                    }
 	                    if (map.containsValue(false))
 	                    {
 	                        ILogger.info("Updating " + Config.databaseCurrencyTable + " table");
 	                        if (!updateCheck.get("plural"))
 	                        {
 	                            database.query("ALTER TABLE " + Config.databaseCurrencyTable + " ADD plural VARCHAR(30) NOT NULL", false);
 	                            ILogger.info("Column plural added in " + Config.databaseCurrencyTable + " table");
 	                        }
 	                        if (!updateCheck.get("minor"))
 	                        {
 	                            database.query("ALTER TABLE " + Config.databaseCurrencyTable + " ADD minor VARCHAR(30) NOT NULL", false);
 	                            ILogger.info("Column minor added in " + Config.databaseCurrencyTable + " table");
 	                        }
 	                        if(!updateCheck.get("minorplural"))
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
 				
 				
 			}
 			if (!database.checkTable(Config.databaseCurrencyExchangeTable))
 			{
 				map.clear();
 				map.put("src", FieldType.VARCHAR);
 				map.put("dest", FieldType.VARCHAR);
 				map.put("rate", FieldType.DOUBLE);
 				if (database.createTable(Config.databaseCurrencyExchangeTable, map))
 				{
 					ILogger.info(Config.databaseCurrencyExchangeTable + " table created!");
 				}
 				else
 				{
 					ILogger.error("Unable to create the " + Config.databaseCurrencyExchangeTable + " table!");
 					return false;
 				}
 			}
 			
 			if (!database.checkTable(Config.databaseBalanceTable))
 			{
 				map.clear();
 				map.put("id", FieldType.PRIMARY);
 				map.put("username_id", FieldType.INT);
 				map.put("currency_id", FieldType.INT);
 				map.put("worldName", FieldType.VARCHAR);
 				map.put("balance", FieldType.DOUBLE);
 				if (database.createTable(Config.databaseBalanceTable, map))
 				{
 					ILogger.info(Config.databaseBalanceTable + " table created!");
 				}
 				else
 				{
 					ILogger.error("Unable to create the " + Config.databaseBalanceTable + " table!");
 					return false;
 				}
 			}
 			if (!database.checkTable(Config.databaseBankTable))
 			{
 				map.clear();
 				map.put("id", FieldType.PRIMARY);
 				map.put("name", FieldType.VARCHAR);
 				map.put("owner", FieldType.VARCHAR);
 				if (database.createTable(Config.databaseBankTable, map))
 				{
 					ILogger.info(Config.databaseBankTable + " table created!");
 				}
 				else
 				{
 					ILogger.error("Unable to create the " + Config.databaseBankTable + " table!");
 					return false;
 				}
 			}
 			if (!database.checkTable(Config.databaseBankBalanceTable))
 			{
 				map.clear();
 				map.put("id", FieldType.PRIMARY);
 				map.put("bank_id", FieldType.INT);
 				map.put("currency_id", FieldType.INT);
 				map.put("worldName", FieldType.VARCHAR);
 				map.put("balance", FieldType.DOUBLE);
 				if (database.createTable(Config.databaseBankBalanceTable, map))
 				{
 					ILogger.info(Config.databaseBankBalanceTable + " table created!");
 				}
 				else
 				{
 					ILogger.error("Unable to create the " + Config.databaseBankBalanceTable + " table!");
 					return false;
 				}
 			}
 			if (!database.checkTable(Config.databaseBankMemberTable))
 			{
 				map.clear();
 				map.put("bank_id", FieldType.INT);
 				map.put("playerName", FieldType.VARCHAR);
 				if (database.createTable(Config.databaseBankMemberTable, map))
 				{
 					ILogger.info(Config.databaseBankMemberTable + " table created!");
 				}
 				else
 				{
 					ILogger.error("Unable to create the " + Config.databaseBankMemberTable + " table!");
 					return false;
 				}
 			}
 			ILogger.info("Database loaded!");
 			if (Config.fixName)
 			{
 				ILogger.info("DEBUG: Put all names in lowerspace (2.X -> 2.3 convert)");
 				try {
 					ResultSet result2 = database.query("SELECT username FROM " + Config.databaseAccountTable + "", true);
 					if (result2 != null)
 					{
 						while(result2.next())
 						{
 							if (result2.getString("username") != null)
 							{
 								database.query("UPDATE " + Config.databaseAccountTable + " SET username='" + result2.getString("username").toLowerCase() + "' WHERE username='" + result2.getString("username") + "'",false);
 							}
 							
 						}
 						Craftconomy.plugin.getConfig().set("System.Debug.fixName", false);
 						Craftconomy.plugin.saveConfig();
 					}
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 
 	public static boolean exists(String account)
 	{
 		HashMap<String, String> map = new HashMap<String,String>();
 		map.put("username", account);
 		return database.entryExist(Config.databaseAccountTable,map);
 	}
 
 	public static void create(String accountName)
 	{
 		HashMap<String, String> map = new HashMap<String, String>();
 		map.put("username", accountName);
 		database.createEntry(Config.databaseAccountTable, map);
 		map.clear();
 		Account account = AccountHandler.getAccount(accountName);
 		map.put("username_id", account.getPlayerId() + "");
 		map.put("worldName", Craftconomy.plugin.getServer().getWorlds().get(0).getName());
 		map.put("currency_id", getCurrencyId(Config.currencyDefault) + "");
 		map.put("balance", Config.defaultHoldings + "");
 		database.createEntry(Config.databaseBalanceTable, map);
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
 
 	
 	//TODO: Fix that
 	public static void deleteAllInitialAccounts()
 	{
 		HashMap<String, String> map = new HashMap<String, String>();
 		map.put("balance", Config.defaultHoldings + "");
 		database.deleteEntry(Config.databaseAccountTable, map);
 	}
 
 	//TODO: FIX that
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
 		int accountId = getAccountId(playerName);
 		
 		HashMap<String, String> map = new HashMap<String, String>();
 		map.put("username", playerName);
 		database.deleteEntry(Config.databaseAccountTable, map);
 		
 		map.clear();
 		map.put("username_id", accountId + "");
 		database.deleteEntry(Config.databaseBalanceTable, map);
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
 		boolean ok = false;
 		HashMap<String,String> map = new HashMap<String,String>();
 		map.put("name", currency);
 		if (database.entryExist(Config.databaseCurrencyTable, map))
 		{
 			ok = true;
 		}
 		return ok;
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
 			HashMap<String,String> map = new HashMap<String,String>();
 			map.put("name", currencyName);
 			map.put("plural", currencyNamePlural);
 			map.put("minor", currencyMinor);
 			map.put("minorplural", currencyMinorPlural);
 			if (database.createEntry(Config.databaseCurrencyTable, map))
 			{
 				success = true;
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
 			HashMap<String,String> map = new HashMap<String,String>();
 			map.put("currency_id", getCurrencyId(currencyName) + "");
 			if (database.deleteEntry(Config.databaseBalanceTable, map))
 			{
 				map.clear();
 				map.put("name", currencyName);
 				if(database.deleteEntry(Config.databaseCurrencyTable, map))
 				{
 					success = true;
 				}
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
 				HashMap<String,String> map = new HashMap<String,String>();
 				map.put("bank_id", bank.getId() + "");
 				map.put("worldName", world.getName());
 				map.put("currency_id", currency.getdatabaseId() + "");
 				map.put("balance", balance + "");
 				database.createEntry(Config.databaseBankBalanceTable, map);
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
 		HashMap<String,String> map = new HashMap<String, String>();
 		map.put("name", bankName);
 		map.put("owner", playerName);
 		if(database.createEntry(Config.databaseBankTable, map))
 		{
 			result = true;
 		}
 		return result;
 	}
 
 	public static boolean deleteBank(String bankName)
 	{
 		boolean result = false;
 		if (bankExists(bankName))
 		{
 			HashMap<String,String> map = new HashMap<String, String>();
 			map.put("name", bankName);
 			if (database.deleteEntry(Config.databaseBankTable, map))
 			{
 				result = true;
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
 		HashMap<String,String> map = new HashMap<String,String>();
 		map.put("bank_id", bank.getId() + "");
 		map.put("playerName", playerName);
 		database.createEntry(Config.databaseBankMemberTable, map);
 	}
 
 	public static void removeBankMember(Bank bank, String playerName)
 	{
 		HashMap<String,String> map = new HashMap<String,String>();
 		map.put("bank_id", bank.getId() + "");
 		map.put("playerName", playerName);
 		database.deleteEntry(Config.databaseBankMemberTable, map);
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
 				HashMap<String,String> map = new HashMap<String,String>();
 				map.put("src", src.getName());
 				map.put("dest", dest.getName());
 				map.put("rate", rate + "");
 				database.createEntry(Config.databaseCurrencyExchangeTable, map);
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
