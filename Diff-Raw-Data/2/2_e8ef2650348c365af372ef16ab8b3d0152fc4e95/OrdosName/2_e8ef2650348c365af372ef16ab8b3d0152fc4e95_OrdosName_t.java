 package org.landofordos.ordosname;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 //import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.palmergames.bukkit.towny.Towny;
 //import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
 import com.palmergames.bukkit.towny.object.TownyUniverse;
 
 public class OrdosName extends JavaPlugin implements Listener {
 
 	// Important plugin objects
 	private static Server server;
 	private static Logger logger;
 	// sql vars
 	private String URL;
 	private String dbUser;
 	private String dbPass;
 	private String dbTable;
 	private Connection connection;
 	//
 	private boolean verbose;
 	private long dbcleanuptime;
 	private boolean useTowny;
 	Towny towny;
 	TownyUniverse townyUniverse;
 
 	public void onDisable() {
 		logger.info("Disabled.");
 	}
 
 	public void onEnable() {
 		// static reference to this plugin and the server
 		// plugin = this;
 		server = getServer();
 		// start the logger
 		logger = getLogger();
 		// save config to default location if not already there
 		this.saveDefaultConfig();
 		// verbose logging? retrieve value from config file.
 		verbose = this.getConfig().getBoolean("verboselogging");
 		if (verbose) {
 			logger.info("Verbose logging enabled.");
 		} else {
 			logger.info("Verbose logging disabled.");
 		}
 		// retrieve SQL variables from config
 		URL = this.getConfig().getString("URL");
 		dbUser = this.getConfig().getString("Username");
 		dbPass = this.getConfig().getString("Password");
 		dbTable = this.getConfig().getString("tablename");
 		// create database connection
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			connection = DriverManager.getConnection("jdbc:" + URL + "?user=" + dbUser + "&password=" + dbPass);
 		} catch (Exception e1) {
 			e1.printStackTrace();
 			getServer().getPluginManager().disablePlugin(this);
 		}
 		// register events
 		server.getPluginManager().registerEvents(this, this);
 		// first-run initialisation
 		final boolean firstrun = this.getConfig().getBoolean("firstrun");
 		if (firstrun) {
 			try {
 				boolean SQLsuccess = this.createSQL();
 				if (verbose && SQLsuccess) {
 					logger.info("Tables created successfully.");
 				}
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			this.getConfig().set("firstrun", false);
 			this.saveConfig();
 			if (verbose) {
 				logger.info("First-run initialisation complete.");
 			}
 		}
 		// retrieve database cleanup threshold from config
 		dbcleanuptime = this.getConfig().getLong("dbcleanuptime");
 		dbcleanup();
 		// check for Towny integration, and if so open towny plugin object - if not found, disable the feature
 		useTowny = this.getConfig().getBoolean("usetowny");
 		if (useTowny) {
 			if (server.getPluginManager().getPlugin("Towny") == null) {
 				logger.severe("Towny integration was enabled, but Towny could not be found!");
 				// the feature is nonessential, so disable it and continue running the plugin.
 				useTowny = false;
 				return;
 			} else {
 				// however if it was found, take a reference to it so that we can use it later
 				Plugin p = server.getPluginManager().getPlugin("Towny");
 				// cast type
 				towny = (Towny) p;
 				townyUniverse = towny.getTownyUniverse();
 				if (verbose) {
 					logger.info("Towny integration enabled.");
 				}
 			}
 		}
 	}
 
 	private boolean createSQL() throws SQLException, ClassNotFoundException {
 		Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		try {
 			statement.executeUpdate("CREATE TABLE " + dbTable + " (user VARCHAR( 32 )  NOT NULL UNIQUE PRIMARY KEY, first VARCHAR( 32 ), "
 					+ "last VARCHAR( 32 ), title VARCHAR( 32 ), suffix VARCHAR( 32 ), titleoverridesfirst BIT DEFAULT FALSE, "
 					+ "townysuffix BIT NOT NULL, enabled BIT DEFAULT TRUE, displayname VARCHAR( 128 ), lastseen DATETIME NOT NULL);");
 		} catch (SQLException e) {
 			logger.info(" SQL Exception: " + e);
 			return false;
 		}
 		return true;
 	}
 
 	private int getResultSetNumRows(ResultSet res) {
 		try {
 			// get row at beginning so as to not affect it
 			int originalPlace = res.getRow();
 			res.last();
 			// Get the row number of the last row which is also the row count
 			int rowCount = res.getRow();
 			// move row back to original position
 			res.absolute(originalPlace);
 			return rowCount;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return -1;
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		// get timestamp for DB inserts
 		Object timestamp = new java.sql.Timestamp((new Date()).getTime());
 		// String timestamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
 		// command functionality
 		// ------------- ordosname functionality
 		if (cmd.getName().equalsIgnoreCase("ordosname")) {
 			if ((args.length < 1) || ((args.length == 1) && (args[0].equalsIgnoreCase("help")))) {
 				sender.sendMessage(ChatColor.YELLOW + "/setfirstname [name] " + ChatColor.WHITE + "- " + ChatColor.DARK_GREEN + "Set first name");
 				sender.sendMessage(ChatColor.YELLOW + "/setlastname [name] " + ChatColor.WHITE + "- " + ChatColor.DARK_GREEN + "Set last name");
 				sender.sendMessage(ChatColor.YELLOW + "/settitle [name] " + ChatColor.WHITE + "- " + ChatColor.DARK_GREEN + "Set title");
 				sender.sendMessage(ChatColor.YELLOW + "/setsuffix [name] " + ChatColor.WHITE + "- " + ChatColor.DARK_GREEN + "Set suffix");
 				sender.sendMessage(ChatColor.YELLOW + "/namereload ");
 				return false;
 			}
 			// code to reload configuration
 			if ((args.length == 1) && (args[0].equalsIgnoreCase("reload")) && (sender.hasPermission("ordosname.admin.reloadconfig"))) {
 				logger.info(sender.getName() + " initiated configuration reload.");
 				sender.sendMessage(ChatColor.YELLOW + "Reloading config");
 				// check for changes in the verbose logging var
 				if (verbose != this.getConfig().getBoolean("verbose")) {
 					verbose = this.getConfig().getBoolean("verboselogging");
 					if (verbose) {
 						logger.info("Verbose logging now enabled.");
 						sender.sendMessage(ChatColor.YELLOW + "Verbose logging now enabled.");
 					} else {
 						logger.info("Verbose logging now disabled.");
 						sender.sendMessage(ChatColor.YELLOW + "Verbose logging now disabled.");
 					}
 				}
 				// retrieve database cleanup threshold from config, if it has changed
 				if (dbcleanuptime != this.getConfig().getLong("dbcleanuptime")) {
 					dbcleanuptime = this.getConfig().getLong("dbcleanuptime");
 					// immediately run database cleanup using new threshold
 					logger.info("New database cleanup threshold (" + dbcleanuptime + ") loaded from config.");
 					sender.sendMessage(ChatColor.YELLOW + "New database cleanup threshold (" + dbcleanuptime + ") loaded from config.");
 					dbcleanup();
 				}
 				// check for Towny integration, and if so open towny plugin object - if not found, disable the feature
 				if (useTowny != this.getConfig().getBoolean("useTowny")) {
 					useTowny = this.getConfig().getBoolean("usetowny");
 					if (useTowny) {
 						if (server.getPluginManager().getPlugin("Towny") == null) {
 							logger.severe("Towny integration was enabled, but Towny could not be found!");
 							sender.sendMessage(ChatColor.RED + "Towny integration was enabled, but Towny could not be found!");
 							// the feature is nonessential, so disable it and continue running the plugin.
 							useTowny = false;
 							return true;
 						} else {
 							// however if it was found, take a reference to it so that we can use it later
 							Plugin p = server.getPluginManager().getPlugin("Towny");
 							// cast type
 							towny = (Towny) p;
 							townyUniverse = towny.getTownyUniverse();
 							logger.info("Towny integration now enabled.");
 						}
 					}
 				}
 				return true;
 			}
 			// code to check people's names
 			if ((args.length > 1) && (args[0].equalsIgnoreCase("namecheck")) && (sender.hasPermission("ordosname.admin.namecheck"))) {
 				// pick up spaced parameters held together by speech marks
 				String nameToCheck = "";
 				String target = null;
 				// boolean object - false represents nameToCheck not started, true nameToCheck in progress, null nameToCheck ended
 				Boolean nameToCheckstarted = false;
 				for (int i = 0; i < args.length; i++) {
 					if (target == null) {
 						if (args[i].startsWith("\"")) {
 							nameToCheckstarted = true;
 						}
 						if (nameToCheckstarted == true) {
 							nameToCheck += " " + args[i];
 							if (args[i].endsWith("\"")) {
 								nameToCheckstarted = null;
 							}
 						}
 					}
 				}
 				if (nameToCheckstarted == null) {
 					// trim off the start and end speech marks
 					nameToCheck = nameToCheck.substring(2, nameToCheck.length() - 1);
 				} else {
 					// if the nameToCheck never ENDED, that's bad news.
 					// assume all is well, though, and just chop off the start.
 					if (nameToCheckstarted) {
 						nameToCheck = nameToCheck.substring(2, nameToCheck.length());
 					}
 					// if the nameToCheck never started, assume single word nameToCheck
 					if (!nameToCheckstarted) {
 						nameToCheck = args[1];
 					}
 				}
 				if (server.getPlayer(nameToCheck) != null) {
 					// the server returns a player object when queried
 					Player player = server.getPlayer(nameToCheck);
 					Statement statement;
 					try {
 						statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 						ResultSet RS = statement.executeQuery("SELECT * FROM " + dbTable + " WHERE user = '" + player.getName() + "';");
 						if (!(RS == null) && (RS.first())) {
 							if (!(RS.getBoolean("enabled"))) {
 								if (verbose) {
 									sender.sendMessage("");
 								}
 							} else {
 								// if there's a result, set the player's name appropriately.
 								// fetch name objects and append appropriate spacing
 								String title = RS.getString("title");
 								String last = RS.getString("last");
 								String suffix = RS.getString("suffix");
 								String first;
 								// does the player's title override their first name, and do they have a title?
 								if ((title != null) && (RS.getBoolean("titleoverridesfirst"))) {
 									// if so, we won't be needing firstname.
 									first = null;
 								} else {
 									// if not, we'll need to fetch their first name
 									first = RS.getString("first");
 								}
 								// string of final name to be set
 								String name = "";
 								if (title != null) {
 									name += title + " ";
 								}
 								if (first != null) {
 									name += first + " ";
 								}
 								if (last != null) {
 									name += last + " ";
 								}
 								if (suffix != null) {
 									name += suffix;
 								}
 								if (name.endsWith(" ")) {
 									name = name.substring(0, name.length() - 1);
 								}
 								if (name.length() > 0) {
 									sender.sendMessage(ChatColor.DARK_GREEN + "The name of user " + ChatColor.WHITE + nameToCheck
 											+ ChatColor.DARK_GREEN + " is " + ChatColor.WHITE + name);
 									return true;
 								}
 							}
 						}
 					} catch (SQLException e) {
 						e.printStackTrace();
 					}
 				} else {
 					// if the server returned null for that player, try searching it as nickname instead.
 					try {
 						Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 						ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE displayname = '" + nameToCheck + "';");
 						if (!(tryRS == null) && (tryRS.first())) {
 							sender.sendMessage(ChatColor.DARK_GREEN + "The username of " + ChatColor.WHITE + nameToCheck + ChatColor.DARK_GREEN
 									+ " is " + ChatColor.WHITE + tryRS.getString("user"));
 							return true;
 						} else {
 							sender.sendMessage(ChatColor.DARK_GREEN + "No results found.");
 							return true;
 						}
 					} catch (SQLException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			// command to re-enable a user's towny suffix
 			if (useTowny) {
 				if ((args.length > 0) && (args[0].equalsIgnoreCase("townysuffix")) && (sender.hasPermission("ordosname.suffix.others"))) {
 					if (args.length < 2) {
 						sender.sendMessage(ChatColor.RED + "Incorrect number of arguments specified!");
 						return false;
 					} else {
 						if (!(sender instanceof Player)) {
 							sender.sendMessage("You cannot do this since you are not a player.");
 						} else {
 							if ((args.length == 1) || ((sender.getName().equals(args[1])) && (sender.hasPermission("ordosname.suffix.self")))) {
 								// set the user's own townysuffix to ON
 								try {
 									Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 									ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '" + sender.getName()
 											+ "';");
 									if (!(tryRS == null) && (tryRS.first())) {
 										// if there's a result, update the table instead of inserting.
 										statement.executeUpdate("UPDATE " + dbTable + " SET townysuffix = TRUE WHERE user= '" + sender.getName()
 												+ "';");
 									} else {
 										// If no result was returned then the user has not been added before.
 										// Use INSERT instead of update to create the record
 										statement.executeUpdate("INSERT INTO " + dbTable + " (user, last, townysuffix, lastseen) VALUES ('"
 												+ sender.getName() + "', '" + sender.getName() + "', TRUE, '" + timestamp + "');");
 										logger.info("Database entry was created for " + sender.getName());
 									}
 									if (statement != null) {
 										statement.close();
 									}
 									sender.sendMessage(ChatColor.RED + "Your suffix has been set to reflect your town.");
 								} catch (SQLException e) {
 									e.printStackTrace();
 								}
 								return true;
 							}
 						}
						if ((sender.hasPermission("ordosname.suffix.others")) && (args.length == 2)) {// set the user's own townysuffix to ON
 							try {
 								Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 								ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '" + args[1] + "';");
 								if (!(tryRS == null) && (tryRS.first())) {
 									// if there's a result, update the table instead of inserting.
 									statement.executeUpdate("UPDATE " + dbTable + " SET townysuffix = TRUE WHERE user= '" + args[1] + "';");
 								} else {
 									// If no result was returned then the user has not been added before.
 									// Use INSERT instead of update to create the record
 									statement.executeUpdate("INSERT INTO " + dbTable + " (user, last, townysuffix, lastseen) VALUES ('" + args[1]
 											+ "', '" + args[1] + "', TRUE, '" + timestamp + "');");
 									logger.info("Database entry was created for " + args[1]);
 								}
 								if (statement != null) {
 									statement.close();
 								}
 								sender.sendMessage(ChatColor.RED + args[1] + "'s suffix has been set to reflect their town.");
 							} catch (SQLException e) {
 								e.printStackTrace();
 							}
 							return true;
 						}
 						return false;
 					}
 				}
 			}
 		}
 		// -------------
 		if (cmd.getName().equalsIgnoreCase("namereload")) {
 			if (args.length < 1) {
 				sender.sendMessage(ChatColor.RED + "Incorrect number of arguments specified!");
 				return false;
 			} else {
 				if (!(sender instanceof Player)) {
 					sender.sendMessage("You cannot do this since you are not a player.");
 				} else {
 					if ((args.length == 0) || ((sender.getName().equals(args[0])) && (sender.hasPermission("ordosname.reload.self")))) {
 						reloadPlayerName(sender, sender.getName());
 						return true;
 					}
 				}
 				if ((sender.hasPermission("ordosname.reload.others")) && (args.length == 1)) {
 					reloadPlayerName(sender, args[0]);
 					return true;
 				}
 				return false;
 			}
 		}
 		// -------------
 		if (cmd.getName().equalsIgnoreCase("setfirstname")) {
 			if (args.length < 1) {
 				if (!(sender instanceof Player)) {
 					// if there are 0 arguments, clear the title of yourself.
 					// this only works if you are a player
 					sender.sendMessage("You cannot do this since you are not a player.");
 					return false;
 				} else {
 					// however if it was a player that issued the command, execute it.
 					// permissions check
 					if (sender.hasPermission("ordosname.name.first.self")) {
 						try {
 							Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 							ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '" + sender.getName() + "';");
 							if (!(tryRS == null) && (tryRS.first())) {
 								// if there's a result, update the table instead of inserting.
 								statement.executeUpdate("UPDATE " + dbTable + " SET first = NULL, lastseen = '" + timestamp + "' WHERE user= '"
 										+ sender.getName() + "';");
 							} else {
 								// If no result was returned then the user has not been added before.
 								// Use INSERT instead of update to create the record
 								statement.executeUpdate("INSERT INTO " + dbTable + " (user, first, last, townysuffix, lastseen) VALUES ('"
 										+ sender.getName() + "', NULL, '" + sender.getName() + "', '" + useTowny + ", '" + timestamp + "');");
 							}
 							if (statement != null) {
 								statement.close();
 							}
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 						reloadPlayerName((Player) sender);
 						return true;
 					} else {
 						// "You don't have permission!"
 					}
 				}
 				return false;
 			} else {
 				if (args.length == 1) {
 					if (!(sender instanceof Player)) {
 						// if only one arg specified take target to be self, but this only works if you are a player
 						sender.sendMessage("You cannot do this since you are not a player.");
 						return false;
 					} else {
 						// however if it was a player that issued the command, execute it.
 						// permissions check
 						if (sender.hasPermission("ordosname.name.first.self")) {
 							try {
 								Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 								ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '" + sender.getName() + "';");
 								if (!(tryRS == null) && (tryRS.first())) {
 									// if there's a result, update the table instead of inserting.
 									statement.executeUpdate("UPDATE " + dbTable + " SET first = '" + args[0] + "', lastseen = '" + timestamp
 											+ "' WHERE user= '" + sender.getName() + "';");
 								} else {
 									// If no result was returned then the user has not been added before.
 									// Use INSERT instead of update to create the record.
 									statement.executeUpdate("INSERT INTO " + dbTable + " (user, first, townysuffix, lastseen) VALUES ('"
 											+ sender.getName() + "', '" + args[0] + "', " + useTowny + ", '" + timestamp + "');");
 								}
 								if (statement != null) {
 									statement.close();
 								}
 							} catch (SQLException e) {
 								e.printStackTrace();
 							}
 							reloadPlayerName((Player) sender);
 							return true;
 						} else {
 							// "You don't have permission!"
 						}
 					}
 				}
 				if (args.length == 2) {
 					// permission check
 					if (sender.hasPermission("ordosname.name.first.others")) {
 						try {
 							Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 							ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '" + args[1] + "';");
 							if (!(tryRS == null) && (tryRS.first())) {
 								// if there's a result, update the table instead of inserting.
 								statement.executeUpdate("UPDATE " + dbTable + " SET first = '" + args[0] + "', lastseen = '" + timestamp
 										+ "' WHERE user= '" + args[1] + "';");
 							} else {
 								// If no result was returned then the user has not been added before.
 								// Use INSERT instead of update to create the record.
 								statement.executeUpdate("INSERT INTO " + dbTable + " (user, first, townysuffix, lastseen) VALUES ('" + args[1]
 										+ "', '" + args[0] + "', " + useTowny + ", '" + timestamp + "');");
 							}
 							if (statement != null) {
 								statement.close();
 							}
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 						reloadPlayerName(sender, args[1]);
 						return true;
 					} else {
 						// "You don't have permission!"
 					}
 				}
 			}
 		}
 		// -------------
 		if (cmd.getName().equalsIgnoreCase("setlastname")) {
 			if (args.length < 1) {
 				if (!(sender instanceof Player)) {
 					// if there are 0 arguments, clear the title of yourself.
 					// this only works if you are a player
 					sender.sendMessage("You cannot do this since you are not a player.");
 					return false;
 				} else {
 					// however if it was a player that issued the command, execute it.
 					// permissions check
 					if (sender.hasPermission("ordosname.name.last.self")) {
 						try {
 							Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 							ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '" + sender.getName() + "';");
 							if (!(tryRS == null) && (tryRS.first())) {
 								// if there's a result, update the table instead of inserting.
 								// since the last name of a player is never null, we reset it to their actual username instead
 								statement.executeUpdate("UPDATE " + dbTable + " SET last = '" + sender.getName() + "', lastseen = '" + timestamp
 										+ "' WHERE user= '" + sender.getName() + "';");
 							} else {
 								// If no result was returned then the user has not been added before.
 								// Use INSERT instead of update to create the record
 								// since the last name of a player is never null, we set it to their actual username instead
 								statement.executeUpdate("INSERT INTO " + dbTable + " (user, last, townysuffix, lastseen) VALUES ('"
 										+ sender.getName() + "', '" + sender.getName() + "', " + useTowny + ", '" + timestamp + "');");
 							}
 							if (statement != null) {
 								statement.close();
 							}
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 						reloadPlayerName((Player) sender);
 						return true;
 					} else {
 						// "You don't have permission!"
 					}
 				}
 				return false;
 			} else {
 				if (args.length == 1) {
 					if (!(sender instanceof Player)) {
 						// if only one arg specified take target to be self, but this only works if you are a player
 						sender.sendMessage("You cannot do this since you are not a player.");
 						return false;
 					} else {
 						// however if it was a player that issued the command, execute it.
 						// permissions check
 						if (sender.hasPermission("ordosname.name.last.self")) {
 							try {
 								Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 								ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '" + sender.getName() + "';");
 								if (!(tryRS == null) && (tryRS.first())) {
 									// if there's a result, update the table instead of inserting.
 									statement.executeUpdate("UPDATE " + dbTable + " SET last = '" + args[0] + "', lastseen = '" + timestamp
 											+ "' WHERE user= '" + sender.getName() + "';");
 								} else {
 									// If no result was returned then the user has not been added before.
 									// Use INSERT instead of update to create the record.
 									statement.executeUpdate("INSERT INTO " + dbTable + " (user, last, townysuffix, lastseen) VALUES ('"
 											+ sender.getName() + "', '" + args[0] + "', " + useTowny + ", '" + timestamp + "');");
 								}
 								if (statement != null) {
 									statement.close();
 								}
 							} catch (SQLException e) {
 								e.printStackTrace();
 							}
 							reloadPlayerName((Player) sender);
 							return true;
 						} else {
 							// "You don't have permission!"
 						}
 					}
 				}
 				if (args.length == 2) {
 					// permission check
 					if (sender.hasPermission("ordosname.name.last.others")) {
 						try {
 							Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 							ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '" + args[1] + "';");
 							if (!(tryRS == null) && (tryRS.first())) {
 								// if there's a result, update the table instead of inserting.
 								statement.executeUpdate("UPDATE " + dbTable + " SET last = '" + args[0] + "', lastseen = '" + timestamp
 										+ "' WHERE user= '" + args[1] + "';");
 							} else {
 								// If no result was returned then the user has not been added before.
 								// Use INSERT instead of update to create the record.
 								statement.executeUpdate("INSERT INTO " + dbTable + " (user, last, townysuffix, lastseen) VALUES ('" + args[1]
 										+ "', '" + args[0] + "', " + useTowny + ", '" + timestamp + "');");
 							}
 							if (statement != null) {
 								statement.close();
 							}
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 						reloadPlayerName(sender, args[1]);
 						return true;
 					} else {
 						// "You don't have permission!"
 					}
 				}
 			}
 		}
 		// -------------
 		if (cmd.getName().equalsIgnoreCase("settitle")) {
 			if (args.length < 1) {
 				if (!(sender instanceof Player)) {
 					// if there are 0 arguments, clear the title of yourself.
 					// this only works if you are a player
 					sender.sendMessage("You cannot do this since you are not a player.");
 					return false;
 				} else {
 					// however if it was a player that issued the command, execute it.
 					// permissions check
 					if (sender.hasPermission("ordosname.name.title.self")) {
 						try {
 							Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 							ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '" + sender.getName() + "';");
 							if (!(tryRS == null) && (tryRS.first())) {
 								// if there's a result, update the table instead of inserting.
 								statement.executeUpdate("UPDATE " + dbTable + " SET title = NULL, titleoverridesfirst = FALSE, lastseen = '"
 										+ timestamp + "' WHERE user= '" + sender.getName() + "';");
 							} else {
 								// If no result was returned then the user has not been added before.
 								// Use INSERT instead of update to create the record
 								statement.executeUpdate("INSERT INTO " + dbTable + " (user, title, titleoverridesfirst, last, lastseen) VALUES ('"
 										+ sender.getName() + "', NULL, FALSE, '" + sender.getName() + "', '" + timestamp + "');");
 							}
 							if (statement != null) {
 								statement.close();
 							}
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 						reloadPlayerName((Player) sender);
 						return true;
 					} else {
 						// "You don't have permission!"
 					}
 				}
 				return false;
 			} else {
 				String title = "";
 				String target = null;
 				// boolean object - null represents title not started, true title in progress, false title ended
 				Boolean titlestarted = null;
 				for (int i = 0; i < args.length; i++) {
 					if (target == null) {
 						if (args[i].startsWith("\"")) {
 							titlestarted = true;
 						}
 						if (titlestarted == true) {
 							title += " " + args[i];
 							if (args[i].endsWith("\"")) {
 								titlestarted = false;
 							}
 						}
 						if ((titlestarted == null) && (i < (args.length - 1))) {
 							target = args[i + 1];
 						}
 					}
 				}
 				if ((titlestarted == false) && (title.length() > 2)) {
 					// trim off the start and end speech marks
 					title = title.substring(2, title.length() - 1);
 				}
 				// if the title never started, assume single word title and pick a target if it was specified.
 				if (titlestarted == null) {
 					title = args[0];
 					if (args.length > 1) {
 						target = args[1];
 					}
 				}
 				if (target == null) {
 					if (!(sender instanceof Player)) {
 						// if only one arg specified take target to be self, but this only works if you are a player
 						sender.sendMessage("You cannot do this since you are not a player.");
 						return false;
 					} else {
 						// however if it was a player that issued the command, execute it.
 						// permissions check
 						if (sender.hasPermission("ordosname.name.title.self")) {
 							try {
 								Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 								ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '" + sender.getName() + "';");
 								if (!(tryRS == null) && (tryRS.first())) {
 									// if there's a result, update the table instead of inserting.
 									statement.executeUpdate("UPDATE " + dbTable + " SET title = '" + title + "', lastseen = '" + timestamp
 											+ "' WHERE user= '" + sender.getName() + "';");
 								} else {
 									// If no result was returned then the user has not been added before.
 									// Use INSERT instead of update to create the record.
 									statement.executeUpdate("INSERT INTO " + dbTable + " (user, title, last, townytitle, lastseen) VALUES ('"
 											+ sender.getName() + "', '" + title + "', '" + sender.getName() + "', FALSE, '" + timestamp + "');");
 								}
 								if (statement != null) {
 									statement.close();
 								}
 							} catch (SQLException e) {
 								e.printStackTrace();
 							}
 							reloadPlayerName((Player) sender);
 							return true;
 						} else {
 							// "You don't have permission!"
 						}
 					}
 				} else {
 					// permission check
 					if (sender.hasPermission("ordosname.name.title.others")) {
 						try {
 							Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 							ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '" + target + "';");
 							if (!(tryRS == null) && (tryRS.first())) {
 								// if there's a result, update the table instead of inserting.
 								statement.executeUpdate("UPDATE " + dbTable + " SET title = '" + title + "', lastseen = '" + timestamp
 										+ "' WHERE user= '" + target + "';");
 							} else {
 								// If no result was returned then the user has not been added before.
 								// Use INSERT instead of update to create the record.
 								statement.executeUpdate("INSERT INTO " + dbTable + " (user, title, last, townytitle, lastseen) VALUES ('" + target
 										+ "', '" + title + "', '" + target + "', FALSE, '" + timestamp + "');");
 							}
 							if (statement != null) {
 								statement.close();
 							}
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 						reloadPlayerName(server.getPlayer(target));
 						return true;
 					} else {
 						// "You don't have permission!"
 					}
 				}
 			}
 		}
 		// -------------
 		if (cmd.getName().equalsIgnoreCase("setsuffix")) {
 			if (args.length < 1) {
 				if (!(sender instanceof Player)) {
 					// if there are 0 arguments, clear the title of yourself.
 					// this only works if you are a player
 					sender.sendMessage("You cannot do this since you are not a player.");
 					return false;
 				} else {
 					// however if it was a player that issued the command, execute it.
 					// permissions check
 					if (sender.hasPermission("ordosname.name.suffix.self")) {
 						try {
 							Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 							ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '" + sender.getName() + "';");
 							if (!(tryRS == null) && (tryRS.first())) {
 								// if there's a result, update the table instead of inserting.
 								statement.executeUpdate("UPDATE " + dbTable + " SET suffix = NULL, townysuffix = FALSE, lastseen = '" + timestamp
 										+ "' WHERE user= '" + sender.getName() + "';");
 							} else {
 								// If no result was returned then the user has not been added before.
 								// Use INSERT instead of update to create the record
 								statement.executeUpdate("INSERT INTO " + dbTable + " (user, last, suffix, townysuffix, lastseen) VALUES ('"
 										+ sender.getName() + "', '" + sender.getName() + "', FALSE, '" + timestamp + "');");
 							}
 							if (statement != null) {
 								statement.close();
 							}
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 						reloadPlayerName((Player) sender);
 						return true;
 					} else {
 						// "You don't have permission!"
 					}
 				}
 				return false;
 			} else {
 				String suffix = "";
 				String target = null;
 				// boolean object - null represents suffix not started, true suffix in progress, false suffix ended
 				Boolean suffixstarted = null;
 				for (int i = 0; i < args.length; i++) {
 					if (target == null) {
 						if (args[i].startsWith("\"")) {
 							suffixstarted = true;
 						}
 						if (suffixstarted == true) {
 							suffix += " " + args[i];
 							if (args[i].endsWith("\"")) {
 								suffixstarted = false;
 							}
 						}
 						if ((suffixstarted == null) && (i < (args.length - 1))) {
 							target = args[i + 1];
 						}
 					}
 				}
 				if ((suffixstarted == false) && (suffix.length() > 2)) {
 					// trim off the start and end speech marks
 					suffix = suffix.substring(2, suffix.length() - 1);
 				}
 				// if the suffix never started, assume single word suffix and pick a target if it was specified.
 				if (suffixstarted == null) {
 					suffix = args[0];
 					if (args.length > 1) {
 						target = args[1];
 					}
 				}
 				if (target == null) {
 					if (!(sender instanceof Player)) {
 						// if only one arg specified take target to be self, but this only works if you are a player
 						sender.sendMessage("You cannot do this since you are not a player.");
 						return false;
 					} else {
 						// however if it was a player that issued the command, execute it.
 						// permissions check
 						if (sender.hasPermission("ordosname.name.suffix.self")) {
 							try {
 								Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 								ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '" + sender.getName() + "';");
 								if (!(tryRS == null) && (tryRS.first())) {
 									// if there's a result, update the table instead of inserting.
 									statement.executeUpdate("UPDATE " + dbTable + " SET suffix = '" + suffix + "', lastseen = '" + timestamp
 											+ "' WHERE user= '" + sender.getName() + "';");
 								} else {
 									// If no result was returned then the user has not been added before.
 									// Use INSERT instead of update to create the record.
 									statement.executeUpdate("INSERT INTO " + dbTable + " (user, suffix, last, townysuffix, lastseen) VALUES ('"
 											+ sender.getName() + "', '" + suffix + "', '" + sender.getName() + "', FALSE, '" + timestamp + "');");
 								}
 								if (statement != null) {
 									statement.close();
 								}
 							} catch (SQLException e) {
 								e.printStackTrace();
 							}
 							reloadPlayerName((Player) sender);
 							return true;
 						} else {
 							// "You don't have permission!"
 						}
 					}
 				} else {
 					// permission check
 					if (sender.hasPermission("ordosname.name.suffix.others")) {
 						try {
 							Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 							ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '" + target + "';");
 							if (!(tryRS == null) && (tryRS.first())) {
 								// if there's a result, update the table instead of inserting.
 								statement.executeUpdate("UPDATE " + dbTable + " SET suffix = '" + suffix + "', lastseen = '" + timestamp
 										+ "' WHERE user= '" + target + "';");
 							} else {
 								// If no result was returned then the user has not been added before.
 								// Use INSERT instead of update to create the record.
 								statement.executeUpdate("INSERT INTO " + dbTable + " (user, suffix, last, townysuffix, lastseen) VALUES ('" + target
 										+ "', '" + suffix + "', '" + target + "', FALSE, '" + timestamp + "');");
 							}
 							if (statement != null) {
 								statement.close();
 							}
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 						reloadPlayerName(server.getPlayer(target));
 						return true;
 					} else {
 						// "You don't have permission!"
 					}
 				}
 			}
 		}
 
 		return false;
 	}
 
 	private void dbcleanup() {
 		// sql query for datediff (much easier than doing so in-code)
 		try {
 			Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			ResultSet RS = statement.executeQuery("SELECT * FROM " + dbTable + " WHERE (DATEDIFF(lastseen, NOW()) > " + dbcleanuptime + ");");
 			// if there were results, delete them
 			if (!(RS == null) && (RS.first())) {
 				logger.info("Found " + getResultSetNumRows(RS) + " records to delete.");
 				statement.executeUpdate("DELETE FROM " + dbTable + " WHERE (DATEDIFF(minute, lastseen, GETDATE()) > " + dbcleanuptime + ");");
 			} else {
 				logger.info("Found 0 records to delete.");
 			}
 			logger.info("Database cleanup complete.");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void reloadPlayerName(CommandSender sender, String playername) {
 		Statement statement;
 		try {
 			statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			ResultSet RS = statement.executeQuery("SELECT * FROM " + dbTable + " WHERE user = '" + playername + "';");
 			if (!(RS == null) && (RS.first())) {
 				if (!(RS.getBoolean("enabled"))) {
 					sender.sendMessage(ChatColor.RED + "Data was found, but ENABLED was flagged FALSE");
 				} else {
 					// if there's a result, set the player's name appropriately.
 					Player player = server.getPlayer(playername);
 					if (player == null) {
 						sender.sendMessage(ChatColor.RED + "Player is offline.");
 					} else {
 						// fetch name objects and append appropriate spacing
 						String title = RS.getString("title");
 						String last = RS.getString("last");
 						String suffix = RS.getString("suffix");
 						String first;
 						// does the player's title override their first name, and do they have a title?
 						if ((title != null) && (RS.getBoolean("titleoverridesfirst"))) {
 							// if so, we won't be needing firstname.
 							first = null;
 						} else {
 							// if not, we'll need to fetch their first name
 							first = RS.getString("first");
 						}
 						// string of final name to be set
 						String name = "";
 						if (title != null) {
 							name += title + " ";
 						}
 						if (first != null) {
 							name += first + " ";
 						}
 						if (last != null) {
 							name += last + " ";
 						}
 						if (suffix != null) {
 							name += suffix;
 						}
 						if (name.endsWith(" ")) {
 							name = name.substring(0, name.length() - 1);
 						}
 						if (name.length() < 1) {
 							sender.sendMessage(ChatColor.RED + "Data was found, but all fields were NULL");
 						} else {
 							sender.sendMessage(ChatColor.RED + "Player " + player.getName() + "'s name set to " + name);
 							recordDisplayName(playername, name);
 							player.setDisplayName(name);
 						}
 					}
 				}
 			} else {
 				// If no result was returned then the user has no record. Return an error.
 				sender.sendMessage(ChatColor.RED + "No data found for player " + playername);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void reloadPlayerName(Player player) {
 		try {
 			Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			ResultSet RS = statement.executeQuery("SELECT * FROM " + dbTable + " WHERE user = '" + player.getName() + "';");
 			if (!(RS == null) && (RS.first())) {
 				if (!(RS.getBoolean("enabled"))) {
 					if (verbose) {
 						logger.info(ChatColor.RED + "Data was found, but ENABLED was flagged FALSE");
 					}
 				} else {
 					// if there's a result, set the player's name appropriately.
 					// fetch name objects and append appropriate spacing
 					String title = RS.getString("title");
 					String last = RS.getString("last");
 					String suffix = RS.getString("suffix");
 					String first;
 					// does the player's title override their first name, and do they have a title?
 					if ((title != null) && (RS.getBoolean("titleoverridesfirst"))) {
 						// if so, we won't be needing firstname.
 						first = null;
 					} else {
 						// if not, we'll need to fetch their first name
 						first = RS.getString("first");
 					}
 					// string of final name to be set
 					String name = "";
 					if (title != null) {
 						name += title + " ";
 					}
 					if (first != null) {
 						name += first + " ";
 					}
 					if (last != null) {
 						name += last + " ";
 					}
 					if (suffix != null) {
 						name += suffix;
 					}
 					if (name.endsWith(" ")) {
 						name = name.substring(0, name.length() - 1);
 					}
 					if (name.length() < 1) {
 						if (verbose) {
 							logger.info("Data was found, but all fields were NULL");
 						}
 					} else {
 						if (verbose) {
 							logger.info("Player " + player.getName() + "'s name set to " + name);
 						}
 						player.setDisplayName(name);
 						recordDisplayName(player.getName(), name);
 					}
 				}
 			} else {
 				if (verbose) {
 					// If no result was returned then the user has no record. Return an error.
 					logger.info("No data found for player " + player.getName());
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void reloadPlayerTownySuffix(Player player) {
 		// this method generates and applies a suffix to a player based on what town they are in.
 		// it assumes that useTowny is TRUE and thus that towny is a valid instance of the Towny plugin, and that the player has townysuffix TRUE.
 		// try and fetch the name of the town this player is in
 		// -----------
 		// get timestamp for DB inserts
 		Object timestamp = new java.sql.Timestamp((new Date()).getTime());
 		String townname = null;
 		try {
 			townname = TownyUniverse.getDataSource().getResident(player.getName()).getTown().getName();
 		} catch (Exception e) {
 			// if they aren't registered to a town, reset their suffix
 			if (verbose) {
 				logger.info("Player " + player.getName() + " does not belong to a town. Resetting suffix.");
 				try {
 					Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 					ResultSet RS = statement.executeQuery("SELECT suffix FROM " + dbTable + " WHERE user = '" + player.getName() + "';");
 					if (!(RS == null) && (RS.first())) {
 						// if the user is already in the database, update their record
 						statement.executeUpdate("UPDATE " + dbTable + " SET suffix = '' WHERE user= '" + player.getName() + "';");
 					} else {
 						// if the user is not already in the database, insert a new record
 						statement.executeQuery("INSERT INTO " + dbTable + " (user, last, suffix, townysuffix, lastseen) VALUES ('" + player.getName()
 								+ "', '" + player.getName() + "', '" + townname + "', " + useTowny + ", '" + timestamp + "');");
 					}
 				} catch (SQLException e1) {
 					e1.printStackTrace();
 				}
 				return;
 			}
 		}
 		// if the previous code returns a result, apply appropriate formatting.
 		if (townname != null) {
 			townname = "of " + townname;
 			// then query for their previous suffix - if it is different, then update the record
 			try {
 				Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 				ResultSet RS = statement.executeQuery("SELECT suffix FROM " + dbTable + " WHERE user = '" + player.getName() + "';");
 				// check to see if data has changed
 				if (!(RS == null) && (RS.first())) {
 					// if the user is already in the database, update their suffix if it differs.
 					String recordedSuffix = RS.getString("suffix");
 					if (RS.wasNull()) {
 						recordedSuffix = "";
 					}
 					if ((recordedSuffix.equals("")) || (!(recordedSuffix.equals(townname)))) {
 						if (verbose) {
 							logger.info("Assigning new suffix '" + townname + "' to player " + player.getName());
 						}
 						statement.executeUpdate("UPDATE " + dbTable + " SET suffix = '" + townname + "' WHERE user= '" + player.getName() + "';");
 						return;
 					} else {
 						// if they don't differ then nothing else is required.
 						if (verbose) {
 							logger.info("Attempted to add suffix to player " + player.getName() + " but their suffix is already correct.");
 							return;
 						}
 					}
 				} else {
 					// if the user is not already in the database, insert a new record with their username (so that the suffix doesn't look stupid)
 					statement.executeQuery("INSERT INTO " + dbTable + " (user, last, suffix, townysuffix, lastseen) VALUES ('" + player.getName()
 							+ "', '" + player.getName() + "', '" + townname + "', " + useTowny + ", '" + timestamp + "');");
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		} else {
 			if (verbose) {
 				logger.info("Player " + player.getName() + " does not belong to a town. Resetting suffix.");
 				try {
 					Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 					ResultSet RS = statement.executeQuery("SELECT suffix FROM " + dbTable + " WHERE user = '" + player.getName() + "';");
 					if (!(RS == null) && (RS.first())) {
 						// if the user is already in the database, update their record
 						statement.executeUpdate("UPDATE " + dbTable + " SET suffix = '' WHERE user= '" + player.getName() + "';");
 					} else {
 						// if the user is not already in the database, insert a new record
 						statement.executeQuery("INSERT INTO " + dbTable + " (user, last, suffix, townysuffix, lastseen) VALUES ('" + player.getName()
 								+ "', '" + player.getName() + "', '" + townname + "', " + useTowny + ", '" + timestamp + "');");
 					}
 				} catch (SQLException e1) {
 					e1.printStackTrace();
 				}
 				return;
 			}
 
 		}
 	}
 
 	public void recordDisplayName(String user, String name) {
 		try {
 			Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			statement.executeUpdate("UPDATE " + dbTable + " SET displayname = '" + name + "' WHERE user= '" + user + "';");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	// EventPriority.NORMAL by default
 	public void onPlayerLogin(PlayerLoginEvent event) {
 		// get timestamp for DB inserts
 		Object timestamp = new java.sql.Timestamp((new Date()).getTime());
 
 		Player player = event.getPlayer();
 
 		try {
 			Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			ResultSet tryRS = statement.executeQuery("SELECT townysuffix FROM " + dbTable + " WHERE user = '" + player.getName() + "';");
 			if (!(tryRS == null) && (tryRS.first())) {
 				// if the user is already in the database, just update their record with the new login time.
 				statement.executeUpdate("UPDATE " + dbTable + " SET lastseen = '" + timestamp + "' WHERE user= '" + player.getName() + "';");
 			} else {
 				// if the user is not already in the database, insert a new record with their username (so that suffixes and titles don't look stupid)
 				statement.executeUpdate("INSERT INTO " + dbTable + " (user, last, townysuffix, lastseen) VALUES ('" + player.getName() + "', '"
 						+ player.getName() + "', " + useTowny + ", '" + timestamp + "');");
 			}
 			// if towny integration enabled AND townysuffix enabled, check for the town they belong to and add the suffix
 			if (useTowny) {
 				// reload the resultset to look for changes
 				tryRS = statement.executeQuery("SELECT townysuffix FROM " + dbTable + " WHERE user = '" + player.getName() + "';");
 				if (!(tryRS == null) && (tryRS.first())) {
 					if (tryRS.getBoolean("townysuffix")) {
 						reloadPlayerTownySuffix(event.getPlayer());
 					}
 				} else {
 					logger.info("No townysuffix data found for player " + event.getPlayer().getName());
 				}
 			}
 			reloadPlayerName(player);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 }
