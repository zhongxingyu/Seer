 package com.mitsugaru.Karmiconomy;
 
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import lib.Mitsugaru.SQLibrary.MySQL;
 import lib.Mitsugaru.SQLibrary.SQLite;
 import lib.Mitsugaru.SQLibrary.Database.Query;
 
 public class DatabaseHandler
 {
 	private Karmiconomy plugin;
 	private static Config config;
 	private SQLite sqlite;
 	private MySQL mysql;
 	private boolean useMySQL;
 	private final DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
 
 	public DatabaseHandler(Karmiconomy plugin, Config conf)
 	{
 		this.plugin = plugin;
 		config = conf;
 		useMySQL = config.useMySQL;
 		checkTables();
 		if (config.importSQL)
 		{
 			if (useMySQL)
 			{
 				importSQL();
 			}
 			config.set("mysql.import", false);
 		}
 	}
 
 	private void checkTables()
 	{
 		if (useMySQL)
 		{
 			// Connect to mysql database
 			mysql = new MySQL(plugin.getLogger(), Karmiconomy.TAG, config.host,
 					config.port, config.database, config.user, config.password);
 			// Check if table exists
 			if (!mysql.checkTable(Table.MASTER.getName()))
 			{
 				plugin.getLogger().info(
 						Karmiconomy.TAG + " Created master table");
 				// Create table
 				mysql.createTable("CREATE TABLE "
 						+ Table.MASTER.getName()
 						+ " (id INT UNSIGNED NOT NULL AUTO_INCREMENT, playername varchar(32) NOT NULL, laston TEXT NOT NULL, UNIQUE (playername), PRIMARY KEY (id));");
 			}
 			if (!mysql.checkTable(Table.ITEMS.getName()))
 			{
 				plugin.getLogger().info(
 						Karmiconomy.TAG + " Created items table");
 				mysql.createTable("CREATE TABLE "
 						+ Table.ITEMS.getName()
 						+ " (row INT UNSIGNED NOT NULL AUTO_INCREMENT, id INT UNSIGNED NOT NULL, itemid SMALLINT UNSIGNED NOT NULL, data TINYTEXT, durability TINYTEXT, place INT NOT NULL, destroy INT NOT NULL, craft INT NOT NULL, enchant INT NOT NULL, playerDrop INT NOT NULL, pickup INT NOT NULL, PRIMARY KEY(row))");
 			}
 			if (!mysql.checkTable(Table.COMMAND.getName()))
 			{
 				plugin.getLogger().info(
 						Karmiconomy.TAG + " Created command table");
 				mysql.createTable("CREATE TABLE "
 						+ Table.COMMAND.getName()
 						+ " (row INT UNSIGNED NOT NULL AUTO_INCREMENT, id INT UNSIGNED NOT NULL, command TEXT NOT NULL, count INT NOT NULL, PRIMARY KEY(row));");
 			}
 			if (!mysql.checkTable(Table.DATA.getName()))
 			{
 				plugin.getLogger()
 						.info(Karmiconomy.TAG + " Created data table");
 				mysql.createTable("CREATE TABLE "
 						+ Table.DATA.getName()
 						+ " (id INT UNSIGNED NOT NULL, bedenter INT NOT NULL, bedleave INT NOT NULL, bowshoot INT NOT NULL, chat INT NOT NULL, death INT NOT NULL, creative INT NOT NULL, survival INT NOT NULL, playerJoin INT NOT NULL, kick INT NOT NULL, quit INT NOT NULL, respawn INT NOT NULL, worldchange INT NOT NULL, tameocelot INT NOT NULL, tamewolf INT NOT NULL, paintingplace INT NOT NULL, eggThrow INT NOT NULL, sneak INT NOT NULL, sprint INT NOT NULL, PRIMARY KEY (id));");
 			}
 			if (!mysql.checkTable(Table.PORTAL.getName()))
 			{
 				plugin.getLogger().info(
 						Karmiconomy.TAG + " Created portal table");
 				mysql.createTable("CREATE TABLE "
 						+ Table.PORTAL.getName()
 						+ " (id INT UNSIGNED NOT NULL, pcreatenether INT NOT NULL, pcreateend INT NOT NULL, pcreatecustom INT NOT NULL, portalenter INT NOT NULL, PRIMARY KEY(id);");
 			}
 			if (!mysql.checkTable(Table.BUCKET.getName()))
 			{
 				plugin.getLogger().info(
 						Karmiconomy.TAG + " Created bucket table");
 				mysql.createTable("CREATE TABLE "
 						+ Table.BUCKET.getName()
 						+ " (id INT UNSIGNED NOT NULL, bemptylava INT NOT NULL, bemptywater INT NOT NULL, bfilllava INT NOT NULL, bfillwater INT NOT NULL, PRIMARY KEY(id);");
 			}
 		}
 		else
 		{
 			// Connect to sql database
 			sqlite = new SQLite(plugin.getLogger(), Karmiconomy.TAG, "data",
 					plugin.getDataFolder().getAbsolutePath());
 			// Check if table exists
 			if (!sqlite.checkTable(Table.MASTER.getName()))
 			{
 				plugin.getLogger().info(
 						Karmiconomy.TAG + " Created master table");
 				// Create table
 				sqlite.createTable("CREATE TABLE "
 						+ Table.MASTER.getName()
 						+ " (id INTEGER PRIMARY KEY, playername varchar(32) NOT NULL, laston TEXT NOT NULL, UNIQUE (playername));");
 			}
 			if (!sqlite.checkTable(Table.ITEMS.getName()))
 			{
 				plugin.getLogger().info(
 						Karmiconomy.TAG + " Created items table");
 				sqlite.createTable("CREATE TABLE "
 						+ Table.ITEMS.getName()
						+ " (row INTEGER PRIMARY KEY, id INTEGER NOT NULL, itemid INTEGER NOT NULL, data TEXT NOT NULL, durability TEXT NOT NULL, place INTEGER NOT NULL, destroy INTEGER NOT NULL, craft INTEGER NOT NULL, enchant INTEGER NOT NULL, playerDrop INTEGER NOT NULL, pickup INT NOT NULL);");
 			}
 			if (!sqlite.checkTable(Table.COMMAND.getName()))
 			{
 				plugin.getLogger().info(
 						Karmiconomy.TAG + " Created command table");
 				sqlite.createTable("CREATE TABLE "
 						+ Table.COMMAND.getName()
 						+ " (row INTEGER PRIMARY KEY, id INTEGER NOT NULL, command TEXT NOT NULL, count INTEGER NOT NULL);");
 			}
 			if (!sqlite.checkTable(Table.DATA.getName()))
 			{
 				plugin.getLogger()
 						.info(Karmiconomy.TAG + " Created data table");
 				sqlite.createTable("CREATE TABLE "
 						+ Table.DATA.getName()
 						+ " (id INTEGER PRIMARY KEY, bedenter INTEGER NOT NULL, bedleave INTEGER NOT NULL, bowshoot INTEGER NOT NULL, chat INTEGER NOT NULL, death INTEGER NOT NULL, creative INTEGER NOT NULL, survival INTEGER NOT NULL, playerJoin INTEGER NOT NULL, kick INTEGER NOT NULL, quit INTEGER NOT NULL, respawn INTEGER NOT NULL, worldchange INTEGER NOT NULL, tameocelot INTEGER NOT NULL, tamewolf INTEGER NOT NULL, paintingplace INTEGER NOT NULL, eggThrow INTEGER NOT NULL, sneak INTEGER NOT NULL, sprint INTEGER NOT NULL);");
 			}
 			if (!sqlite.checkTable(Table.PORTAL.getName()))
 			{
 				plugin.getLogger().info(
 						Karmiconomy.TAG + " Created portal table");
 				sqlite.createTable("CREATE TABLE "
 						+ Table.PORTAL.getName()
 						+ " (id INTEGER PRIMARY KEY, pcreatenether INTEGER NOT NULL, pcreateend INTEGER NOT NULL, pcreatecustom INTEGER NOT NULL, portalenter INTEGER NOT NULL);");
 			}
 			if (!sqlite.checkTable(Table.BUCKET.getName()))
 			{
 				plugin.getLogger().info(
 						Karmiconomy.TAG + " Created bucket table");
 				sqlite.createTable("CREATE TABLE "
 						+ Table.BUCKET.getName()
 						+ " (id INTEGER PRIMARY KEY, bemptylava INTEGER NOT NULL, bemptywater INTEGER NOT NULL, bfilllava INTEGER NOT NULL, bfillwater INTEGER NOT NULL);");
 			}
 		}
 	}
 
 	private void importSQL()
 	{
 		// Connect to sql database
 		try
 		{
 			// Grab local SQLite database
 			sqlite = new SQLite(plugin.getLogger(), Karmiconomy.TAG, "data",
 					plugin.getDataFolder().getAbsolutePath());
 			// Copy tables
 			Query rs = sqlite.select("SELECT * FROM " + Table.MASTER.getName()
 					+ ";");
 			if (rs.getResult().next())
 			{
 				plugin.getLogger().info(
 						Karmiconomy.TAG + " Importing master table...");
 				do
 				{
 					// import master
 					PreparedStatement statement = mysql.prepare("INSERT INTO "
 							+ Table.MASTER.getName()
 							+ " (id, playername, laston) VALUES(?,?,?);");
 					statement.setInt(1, rs.getResult().getInt("id"));
 					statement.setString(2,
 							rs.getResult().getString("playername"));
 					statement.setString(3, rs.getResult().getString("date"));
 					statement.executeUpdate();
 					statement.close();
 				} while (rs.getResult().next());
 			}
 			rs.closeQuery();
 			rs = sqlite.select("SELECT * FROM " + Table.DATA.getName() + ";");
 			if (rs.getResult().next())
 			{
 				plugin.getLogger().info(
 						Karmiconomy.TAG + " Importing data table...");
 				do
 				{
 					PreparedStatement statement = mysql
 							.prepare("INSERT INTO "
 									+ Table.DATA.getName()
 									+ " (id, bedenter, bedleave, bowshoot, chat, death, creative, survival, playerJoin, kick, quit, respawn, worldchange, tameocelot, tamewolf, paintingplace, eggThrow, sneak, sprint) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
 					// TODO set elements
 					statement.execute();
 					statement.close();
 				} while (rs.getResult().next());
 			}
 			// TODO import items
 			// TODO import command
 			rs.closeQuery();
 			rs = sqlite.select("SELECT * FROM " + Table.PORTAL.getName() + ";");
 			if (rs.getResult().next())
 			{
 				plugin.getLogger().info(
 						Karmiconomy.TAG + " Importing portal table...");
 				do
 				{
 					PreparedStatement statement = mysql
 							.prepare("INSERT INTO "
 									+ Table.PORTAL.getName()
 									+ " (id,pcreatenether, pcreateend, pcreatecustom, portalenter) VALUES (?,?,?,?,?);");
 					statement.setInt(1, rs.getResult().getInt("id"));
 					statement.setInt(2, rs.getResult().getInt("pcreatenether"));
 					statement.setInt(3, rs.getResult().getInt("pcreateend"));
 					statement.setInt(4, rs.getResult().getInt("pcreatecustom"));
 					statement.setInt(5, rs.getResult().getInt("portalenter"));
 					statement.executeUpdate();
 					statement.close();
 				} while (rs.getResult().next());
 			}
 			rs.closeQuery();
 			rs = sqlite.select("SELECT * FROM " + Table.BUCKET.getName() + ";");
 			if (rs.getResult().next())
 			{
 				plugin.getLogger().info(
 						Karmiconomy.TAG + " Importing bucket table...");
 				do
 				{
 					PreparedStatement statement = mysql
 							.prepare("INSERT INTO "
 									+ Table.BUCKET.getName()
 									+ " (id,bemptylava, bemptywater, bfilllava, bfillwater) VALUES (?,?,?,?,?);");
 					statement.setInt(1, rs.getResult().getInt("id"));
 					statement.setInt(2, rs.getResult().getInt("bemptylava"));
 					statement.setInt(3, rs.getResult().getInt("bemptywater"));
 					statement.setInt(4, rs.getResult().getInt("bfilllava"));
 					statement.setInt(5, rs.getResult().getInt("bfillwater"));
 					statement.executeUpdate();
 					statement.close();
 				} while (rs.getResult().next());
 			}
 			rs.closeQuery();
 			plugin.getLogger().info(
 					Karmiconomy.TAG + " Done importing SQLite into MySQL");
 		}
 		catch (SQLException e)
 		{
 			plugin.getLogger().warning(
 					Karmiconomy.TAG + " SQL Exception on Import");
 			e.printStackTrace();
 		}
 	}
 
 	public boolean checkConnection()
 	{
 		boolean connected = false;
 		if (useMySQL)
 		{
 			connected = mysql.checkConnection();
 		}
 		else
 		{
 			connected = sqlite.checkConnection();
 		}
 		return connected;
 	}
 
 	public void close()
 	{
 		if (useMySQL)
 		{
 			mysql.close();
 		}
 		else
 		{
 			sqlite.close();
 		}
 	}
 
 	public Query select(String query)
 	{
 		if (useMySQL)
 		{
 			return mysql.select(query);
 		}
 		else
 		{
 			return sqlite.select(query);
 		}
 	}
 
 	public void standardQuery(String query)
 	{
 		if (useMySQL)
 		{
 			mysql.standardQuery(query);
 		}
 		else
 		{
 			sqlite.standardQuery(query);
 		}
 	}
 
 	private int getPlayerId(String name)
 	{
 		int id = -1;
 		try
 		{
 			final Query query = select("SELECT * FROM "
 					+ Table.MASTER.getName() + " WHERE playername='" + name
 					+ "';");
 			if (query.getResult().next())
 			{
 				id = query.getResult().getInt("id");
 			}
 			query.closeQuery();
 		}
 		catch (SQLException e)
 		{
 			plugin.getLogger().warning("SQL Exception on grabbing player ID");
 			e.printStackTrace();
 		}
 		return id;
 	}
 
 	public boolean addPlayer(String name)
 	{
 		if (!name.contains("'"))
 		{
 			final int id = getPlayerId(name);
 			if (id == -1)
 			{
 				// Generate last on
 				final String laston = dateFormat.format(new Date());
 				// Insert player to master database
 				final String query = "INSERT INTO " + Table.MASTER.getName()
 						+ " (playername,laston) VALUES('" + name + "','"
 						+ laston + "');";
 				standardQuery(query);
 				// Grab generated id
 				final int generatedId = getPlayerId(name);
 				if (generatedId != -1)
 				{
 					// Add player data table
 					standardQuery("INSERT INTO "
 							+ Table.DATA.getName()
 							+ " (id, bedenter, bedleave, bowshoot, chat, death, creative, survival, playerJoin, kick, quit, respawn, worldchange, tameocelot, tamewolf, paintingplace, eggThrow, sneak, sprint) VALUES('"
 							+ generatedId
 							+ "','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0');");
 					// Add player portal table
 					standardQuery("INSERT INTO "
 							+ Table.PORTAL.getName()
 							+ " (id, pcreatenether, pcreateend, pcreatecustom, portalenter) VALUES('"
 							+ id + "','0','0','0','0');");
 					// Add player bucket table
 					standardQuery("INSERT INTO "
 							+ Table.BUCKET.getName()
 							+ " (id, bemptylava, bemptywater, bfilllava, bfillwater) VALUES('"
 							+ id + "','0','0','0','0');");
 					return true;
 				}
 				else
 				{
 					plugin.getLogger().warning(
 							"Player '" + name
 									+ "' NOT successfully added to database!");
 				}
 			}
 		}
 		else
 		{
 			plugin.getLogger().warning(
 					"Illegal character for player: " + name
 							+ " ... Not added to database.");
 		}
 		return false;
 	}
 
 	public boolean checkDateReset(String name)
 	{
 		boolean valid = false;
 		boolean same = false;
 		int id = getPlayerId(name);
 		if (id != -1)
 		{
 			valid = true;
 		}
 		else
 		{
 			// Reset was called, but somehow player did not exist. Add them to
 			// the database
 			addPlayer(name);
 		}
 		if (valid)
 		{
 			try
 			{
 				// Grab date
 				String date = "";
 				final Query query = select("SELECT * FROM "
 						+ Table.MASTER.getName() + " WHERE id='" + id + "'");
 				if (query.getResult().next())
 				{
 					date = query.getResult().getString("laston");
 				}
 				query.closeQuery();
 				final String current = dateFormat.format(new Date());
 				// Compare
 				final String[] dArray = date.split("-");
 				final String[] cArray = current.split("-");
 				if (cArray[0].equals(dArray[0]) && cArray[1].equals(dArray[1])
 						&& cArray[2].equals(dArray[2]))
 				{
 					same = true;
 				}
 			}
 			catch (SQLException e)
 			{
 				plugin.getLogger().warning(
 						"SQL Exception on grabbing player date");
 				e.printStackTrace();
 			}
 			catch (NullPointerException n)
 			{
 				plugin.getLogger().warning("NPE on grabbing player date");
 				n.printStackTrace();
 			}
 			catch (ArrayIndexOutOfBoundsException a)
 			{
 				plugin.getLogger()
 						.warning(
 								"ArrayIndexOutOfBoundsExceptoin on grabbing player date");
 				a.printStackTrace();
 			}
 		}
 		return same;
 	}
 
 	public void resetAllValues(String name)
 	{
 		boolean drop = false;
 		int id = getPlayerId(name);
 		if (id != -1)
 		{
 			drop = true;
 		}
 		else
 		{
 			// Reset was called, but somehow player did not exist. Add them to
 			// the
 			// database
 			addPlayer(name);
 		}
 		if (drop)
 		{
 			// Reset player values in data table
 			standardQuery("UPDATE "
 					+ Table.DATA.getName()
 					+ " SET bedenter='0', bedleave='0', bowshoot='0', chat='0', death='0', creative='0', survival='0', playerJoin='0', kick='0', quit='0', respawn='0', worldchange='0', tameocelot='0', tamewolf='0', paintingplace='0', eggThrow='0', sneak='0', sprint='0' WHERE id='"
 					+ id + "';");
 			// Reset player values in portal table
 			standardQuery("UPDATE "
 					+ Table.PORTAL.getName()
 					+ " SET pcreatenether='0', pcreateend='0', pcreatecustom='0', portalenter='0' WHERE id='"
 					+ id + "';");
 			// Reset player values in bucket table
 			standardQuery("UPDATE "
 					+ Table.BUCKET.getName()
 					+ " SET bemptylava='0', bemptywater='0', bfilllava='0', bfillwater='0' WHERE id='"
 					+ id + "';");
 			// Drop everything in items for player id
 			standardQuery("DELETE FROM " + Table.ITEMS.getName()
 					+ " WHERE id='" + id + "';");
 			// Drop everything in commands for player id
 			standardQuery("DELETE FROM " + Table.COMMAND.getName()
 					+ " WHERE id='" + id + "';");
 		}
 		else
 		{
 			plugin.getLogger().warning("Could not reset values for: " + name);
 		}
 	}
 
 	public void resetValue(Field field, String name, Item item, String command)
 	{
 		// TODO reset specfied field
 	}
 
 	public void incrementData(Field field, String name, Item item,
 			String command)
 	{
 		boolean inc = false;
 		int id = getPlayerId(name);
 		if (id != -1)
 		{
 			inc = true;
 		}
 		else
 		{
 			// Increment was called, but somehow player did not exist. Add them
 			// to
 			// database
 			addPlayer(name);
 			id = getPlayerId(name);
 			if (id != -1)
 			{
 				inc = true;
 			}
 		}
 		if (inc)
 		{
 			// Grab previous value
 			int value = getData(field, name, item, command);
 			// Increment count of specified field for given player name and
 			// optional item / command
 			value++;
 			switch (field.getTable())
 			{
 				case DATA:
 				{
 					// Update
 					standardQuery("UPDATE " + field.getTable().getName()
 							+ " SET " + field.getColumnName() + "='" + value
 							+ "' WHERE id='" + id + "';");
 					break;
 				}
 				case ITEMS:
 				{
 					// Handle items
 					if (item != null)
 					{
 						if (item.isTool())
 						{
 							standardQuery("UPDATE "
 									+ field.getTable().getName() + " SET "
 									+ field.getColumnName() + "='" + value
 									+ "' WHERE id='" + id + "' AND itemid='"
 									+ item.itemId() + "';");
 						}
 						else if (item.isPotion())
 						{
 							// See if entry exists
 							standardQuery("UPDATE "
 									+ field.getTable().getName() + " SET "
 									+ field.getColumnName() + "='" + value
 									+ "' WHERE id='" + id + "' AND itemid='"
 									+ item.itemId() + "' AND durability='"
 									+ item.itemDurability() + "';");
 						}
 						else
 						{
 							// See if entry exists
 							standardQuery("UPDATE "
 									+ field.getTable().getName() + " SET "
 									+ field.getColumnName() + "='" + value
 									+ "' WHERE id='" + id + "' AND itemid='"
 									+ item.itemId() + "' AND data='"
 									+ item.itemData() + "' AND durability='"
 									+ item.itemDurability() + "';");
 						}
 					}
 					else
 					{
 						plugin.getLogger().warning(
 								"Item cannot be null for field: " + field);
 					}
 					break;
 				}
 				case COMMAND:
 				{
 					// TODO check if insert or update
 					break;
 				}
 				case PORTAL:
 				{
 					// Update
 					standardQuery("UPDATE " + field.getTable().getName()
 							+ " SET " + field.getColumnName() + "='" + value
 							+ "' WHERE id='" + id + "';");
 					break;
 				}
 				case BUCKET:
 				{
 					// Update
 					standardQuery("UPDATE " + field.getTable().getName()
 							+ " SET " + field.getColumnName() + "='" + value
 							+ "' WHERE id='" + id + "';");
 					break;
 				}
 				default:
 				{
 					if (config.debugUnhandled)
 					{
 						plugin.getLogger().warning(
 								"Unhandled table " + field.getTable().getName()
 										+ " for field " + field);
 					}
 					break;
 				}
 			}
 
 		}
 		else
 		{
 			plugin.getLogger().warning(
 					"Could not increment value '" + field + "' for: " + name);
 		}
 
 	}
 
 	public int getData(Field field, String name, Item item, String command)
 	{
 		boolean validId = false;
 		int data = -1;
 		int id = getPlayerId(name);
 		if (id == -1)
 		{
 			plugin.getLogger().warning(
 					"Player '" + name + "' not found in master database!");
 			addPlayer(name);
 			id = getPlayerId(name);
 			if (id != -1)
 			{
 				validId = true;
 			}
 		}
 		else
 		{
 			validId = true;
 		}
 		if (validId)
 		{
 			try
 			{
 				Query query = null;
 				switch (field.getTable())
 				{
 					case DATA:
 					{
 						// Handle data specific stuff
 						query = select("SELECT * FROM "
 								+ field.getTable().getName() + " WHERE id='"
 								+ id + "';");
 						if (query.getResult().next())
 						{
 							data = query.getResult().getInt(
 									field.getColumnName());
 						}
 						break;
 					}
 					case ITEMS:
 					{
 						boolean found = false;
 						if (item != null)
 						{
 							// Handle items
 							if (item.isTool())
 							{
 								// See if entry exists
 								query = select("SELECT * FROM "
 										+ field.getTable().getName()
 										+ " WHERE id='" + id + "' AND itemid='"
 										+ item.itemId() + "';");
 								if (query.getResult().next())
 								{
 									found = true;
 									data = query.getResult().getInt(
 											field.getColumnName());
 								}
 								else
 								{
 									// No entry, create one
 									data = 0;
 								}
 								query.closeQuery();
 								if (!found)
 								{
 									// Add entry for tool item
 									standardQuery("INSERT INTO "
 											+ field.getTable().getName()
 											+ " (id,itemid,data,durability,place,destroy,craft,enchant,playerDrop) VALUES('"
 											+ id + "','" + item.itemId()
 											+ "','0','0','0','0','0','0','0');");
 								}
 							}
 							else if (item.isPotion())
 							{
 								// See if entry exists
 								query = select("SELECT * FROM "
 										+ field.getTable().getName()
 										+ " WHERE id='" + id + "' AND itemid='"
 										+ item.itemId() + "' AND durability='"
 										+ item.itemDurability() + "';");
 								if (query.getResult().next())
 								{
 									found = true;
 									data = query.getResult().getInt(
 											field.getColumnName());
 								}
 								else
 								{
 									// No entry, create one
 									data = 0;
 								}
 								query.closeQuery();
 								// Only record durability
 								if (!found)
 								{
 									// Add entry for tool item
 									standardQuery("INSERT INTO "
 											+ field.getTable().getName()
 											+ " (id,itemid,data,durability,place,destroy,craft,enchant,playerDrop) VALUES('"
 											+ id + "','" + item.itemId()
 											+ "','0','" + item.itemDurability()
 											+ "','0','0','0','0','0');");
 								}
 							}
 							else
 							{
 								// See if entry exists
 								query = select("SELECT * FROM "
 										+ field.getTable().getName()
 										+ " WHERE id='" + id + "' AND itemid='"
 										+ item.itemId() + "' AND data='"
 										+ item.itemData()
 										+ "' AND durability='"
 										+ item.itemDurability() + "';");
 								if (query.getResult().next())
 								{
 									found = true;
 									data = query.getResult().getInt(
 											field.getColumnName());
 								}
 								else
 								{
 									// No entry, create one
 									data = 0;
 								}
 								query.closeQuery();
 								// Normal, so set everything
 								if (!found)
 								{
 									// Add entry for tool item
 									standardQuery("INSERT INTO "
 											+ field.getTable().getName()
 											+ " (id,itemid,data,durability,place,destroy,craft,enchant,playerDrop) VALUES('"
 											+ id + "','" + item.itemId()
 											+ "','" + item.itemData() + "','"
 											+ item.itemDurability()
 											+ "','0','0','0','0','0');");
 								}
 							}
 						}
 						else
 						{
 							plugin.getLogger().warning(
 									"Item cannot be null for field: " + field);
 						}
 						break;
 					}
 					case COMMAND:
 					{
 						if (command != null)
 						{
 							// TODO handle command specific stuff
 						}
 						else
 						{
 							plugin.getLogger().warning(
 									"Command cannot be null for field: "
 											+ field);
 						}
 						break;
 					}
 					case PORTAL:
 					{
 						query = select("SELECT * FROM "
 								+ field.getTable().getName() + " WHERE id='"
 								+ id + "';");
 						if (query.getResult().next())
 						{
 							data = query.getResult().getInt(
 									field.getColumnName());
 						}
 						break;
 					}
 					case BUCKET:
 					{
 						query = select("SELECT * FROM "
 								+ field.getTable().getName() + " WHERE id='"
 								+ id + "';");
 						if (query.getResult().next())
 						{
 							data = query.getResult().getInt(
 									field.getColumnName());
 						}
 						break;
 					}
 					default:
 					{
 						if (config.debugUnhandled)
 						{
 							plugin.getLogger().warning(
 									"Unhandled table '"
 											+ field.getTable().getName()
 											+ "' for field '" + field + "'");
 						}
 						break;
 					}
 				}
 				if (query != null)
 				{
 					query.closeQuery();
 				}
 			}
 			catch (SQLException e)
 			{
 				plugin.getLogger().warning("SQL Exception on Import");
 				e.printStackTrace();
 			}
 		}
 		return data;
 	}
 
 	// TODO make method to get limit field for specified player
 
 	public enum Field
 	{
 		// TODO eggs, paintings, vehicle
 		BOW_SHOOT(Table.DATA, "bowshoot"), BED_ENTER(Table.DATA, "bedenter"), BED_LEAVE(
 				Table.DATA, "bedleave"), BLOCK_PLACE(Table.ITEMS, "place"), BLOCK_DESTROY(
 				Table.ITEMS, "destroy"), ITEM_CRAFT(Table.ITEMS, "craft"), ITEM_ENCHANT(
 				Table.ITEMS, "enchant"), ITEM_DROP(Table.ITEMS, "playerDrop"), ITEM_PICKUP(
 				Table.ITEMS, "pickup"), EGG_THROW(Table.DATA, "eggThrow"), CHAT(
 				Table.DATA, "chat"), COMMAND(Table.COMMAND, "command"), DEATH(
 				Table.DATA, "death"), CREATIVE(Table.DATA, "creative"), SURVIVAL(
 				Table.DATA, "survival"), JOIN(Table.DATA, "playerJoin"), KICK(
 				Table.DATA, "kick"), QUIT(Table.DATA, "quit"), RESPAWN(
 				Table.DATA, "respawn"), SNEAK(Table.DATA, "sneak"), SPRINT(
 				Table.DATA, "sprint"), PAINTING_PLACE(Table.DATA,
 				"paintingplace"), PORTAL_CREATE_NETHER(Table.PORTAL,
 				"pcreatenether"), PORTAL_CREATE_END(Table.PORTAL, "pcreateend"), PORTAL_CREATE_CUSTOM(
 				Table.PORTAL, "pcreatecustom"), PORTAL_ENTER(Table.PORTAL,
 				"portalenter"), TAME_OCELOT(Table.DATA, "tameocelot"), TAME_WOLF(
 				Table.DATA, "tamewolf"), WORLD_CHANGE(Table.DATA, "worldchange"), BUCKET_EMPTY_LAVA(
 				Table.BUCKET, "bemptylava"), BUCKET_EMPTY_WATER(Table.BUCKET,
 				"bemptywater"), BUCKET_FILL_LAVA(Table.BUCKET, "bfilllava"), BUCKET_FILL_WATER(
 				Table.BUCKET, "bfillwater");
 		private final Table table;
 		private final String columnname;
 
 		private Field(Table table, String columnname)
 		{
 			this.table = table;
 			this.columnname = columnname;
 		}
 
 		public Table getTable()
 		{
 			return table;
 		}
 
 		public String getColumnName()
 		{
 			return columnname;
 		}
 	}
 
 	public enum Table
 	{
 		MASTER(config.tablePrefix + "master"), ITEMS(config.tablePrefix
 				+ "items"), DATA(config.tablePrefix + "data"), COMMAND(
 				config.tablePrefix + "command"), PORTAL(config.tablePrefix
 				+ "portal"), BUCKET(config.tablePrefix + "bucket");
 		private final String table;
 
 		private Table(String table)
 		{
 			this.table = table;
 		}
 
 		public String getName()
 		{
 			return table;
 		}
 
 		@Override
 		public String toString()
 		{
 			return table;
 		}
 	}
 }
