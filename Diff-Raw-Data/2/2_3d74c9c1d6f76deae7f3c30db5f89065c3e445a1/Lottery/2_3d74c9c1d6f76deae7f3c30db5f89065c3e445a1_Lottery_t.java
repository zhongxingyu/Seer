 package net.erbros.Lottery;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Logger;
 
 import net.erbros.Lottery.register.payment.Method;
 import net.erbros.Lottery.register.payment.Method.MethodAccount;
 import net.erbros.Lottery.register.payment.Methods;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public class Lottery extends JavaPlugin {
 
 	protected static Integer cost;
 	protected Integer hours;
 	protected static Long nextexec;
 	public Method Method = null;
         public Methods Methods = null;
 	public Boolean timerStarted = false;
 	protected static Boolean useiConomy;
 	protected static Integer material;
 	protected Integer extraInPot;
 	protected Boolean broadcastBuying;
 	protected Boolean welcomeMessage;
 	protected Integer netPayout;
 	protected Boolean clearExtraInPot;
 	protected Integer maxTicketsEachUser;
 	protected Integer numberOfTicketsAvailable;
 	protected Integer jackpot;
         protected ArrayList<String> msgWelcome;
 	protected YamlConfiguration c;
         protected YamlConfiguration msgConfig;
 	// Starting timer we are going to use for scheduling.
 	Timer timer;
 
 	private static PlayerJoinListener PlayerListener = null;
 	public PluginDescriptionFile info = null;
 	protected static org.bukkit.Server server = null;
 
 	// Doing some logging. Thanks cyklo
 	protected static final Logger log = Logger.getLogger("Minecraft");;
 
 	@Override
 	public void onDisable() {
 		// Disable all running timers.
 		Bukkit.getServer().getScheduler().cancelTasks(this);
 		info = null;
 
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println(pdfFile.getName() + " version "
 				+ pdfFile.getVersion() + " has been unloaded.");
 		debugMsg(getDescription().getName()
 				+ ": has been disabled (including timers).");
 	}
 
 	@Override
 	public void onEnable() {
 
 		server = getServer();
 		// Do we need iConomy?
 		if (useiConomy == true) {
 			// Event Registration
 			getServer().getPluginManager().registerEvent(
 					Event.Type.PLUGIN_ENABLE, new PluginListener(this),
 					Priority.Monitor, this);
 			getServer().getPluginManager().registerEvent(
 					Event.Type.PLUGIN_DISABLE, new PluginListener(this),
 					Priority.Monitor, this);
 		}
 		if (welcomeMessage == true) {
 			PlayerListener = new PlayerJoinListener(this);
 			getServer().getPluginManager().registerEvent(
 					Event.Type.PLAYER_JOIN, PlayerListener, Priority.Monitor,
 					this);
 		}
 
 		// Listen for some player interaction perhaps? Thanks to cyklo :)
 
         getCommand("lottery").setExecutor(new CommandExecutor() {
             @Override
             public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
 
                 // Lets check if we have found a plugin for money.
                 if (!Methods.hasMethod() && useiConomy == true) {
                     debugMsg("No money plugin found yet.");
                     sender.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                         + ChatColor.WHITE + "Sorry, we haven't found a money plugin yet..");
 
                     return true;
                 }
 
 
                 // Can the player access the plugin?
                 if (!sender.hasPermission("lottery.buy")) {
 
                         return true;
                 }
 
 
 
                 // If its just /lottery, and no args.
                 if (args.length == 0) {
 
 
                     // Is this a console? If so, just tell that lottery is running and time until next draw.
                     if(!(sender instanceof Player)) {
                         sender.sendMessage("Hi Console - The Lottery plugin is running");
 
                         // Send some messages:
                         sender.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                         + ChatColor.WHITE + "Draw in: " + ChatColor.RED
                                         + timeUntil(Lottery.nextexec, false));
                         return true;
                     }
                     Player player = (Player) sender;
 
                     // Check if we got any money/items in the pot.
                     int amount = winningAmount();
                     // Send some messages:
                     player.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                     + ChatColor.WHITE + "Draw in: " + ChatColor.RED
                                     + timeUntil(Lottery.nextexec, false));
                     if (useiConomy == false) {
                             player.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                             + ChatColor.WHITE + "Buy a ticket for "
                                             + ChatColor.RED + Lottery.cost + " "
                                             + formatMaterialName(material)
                                             + ChatColor.WHITE + " with " + ChatColor.RED
                                             + "/lottery buy");
                             player.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                             + ChatColor.WHITE + "There is currently "
                                             + ChatColor.GREEN + amount + " "
                                             + formatMaterialName(material)
                                             + ChatColor.WHITE + " in the pot.");
                     } else {
                             player.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                             + ChatColor.WHITE + "Buy a ticket for "
                                             + ChatColor.RED + Method.format(Lottery.cost)
                                             + ChatColor.WHITE + " with " + ChatColor.RED
                                             + "/lottery buy");
                             player.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                             + ChatColor.WHITE + "There is currently "
                                             + ChatColor.GREEN + Method.format(amount)
                                             + ChatColor.WHITE + " in the pot.");
                     }
                     if (maxTicketsEachUser > 1) {
                             player.sendMessage(ChatColor.GOLD
                                             + "[LOTTERY] "
                                             + ChatColor.WHITE
                                             + "You got "
                                             + ChatColor.RED
                                             + playerInList((Player) sender)
                                             + " "
                                             + ChatColor.WHITE
                                             + pluralWording("ticket",
                                                             playerInList((Player) sender)));
                     }
                     // Number of tickets available?
                     if(numberOfTicketsAvailable > 0) {
                     	player.sendMessage(ChatColor.GOLD
                     			+ "[LOTTERY]"
                     			+ ChatColor.WHITE
                     			+ "There is "
                     			+ ChatColor.RED
                     			+ (numberOfTicketsAvailable - ticketsSold())
                     			+ ChatColor.WHITE
                     			+ pluralWording("ticket",numberOfTicketsAvailable - ticketsSold())
                     			+ "left.");
                     }
                     player.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                     + ChatColor.RED + "/lottery help" + ChatColor.WHITE
                                     + " for other commands");
                     // Does lastwinner exist and != null? Show.
                     // Show different things if we are using iConomy over
                     // material.
                     if (useiConomy == true) {
                             if (c.getString("lastwinner") != null) {
                                     player.sendMessage(ChatColor.GOLD
                                                     + "[LOTTERY] "
                                                     + ChatColor.WHITE
                                                     + "Last winner: "
                                                     + c.getString("lastwinner")
                                                     + " ("
                                                     + Method.format(c.getInt("lastwinneramount"))
                                                     + ")");
                             }
 
                     } else {
                             if (c.getString("lastwinner") != null) {
                                     player.sendMessage(ChatColor.GOLD
                                                     + "[LOTTERY] "
                                                     + ChatColor.WHITE
                                                     + "Last winner: "
                                                     + c.getString("lastwinner")
                                                     + " ("
                                                     + c.getInt("lastwinneramount")
                                                     + " "
                                                     + formatMaterialName(material) + ")");
                             }
                     }
 
                     // if not iConomy, make players check for claims.
                     if (useiConomy == false) {
                             player.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                             + ChatColor.WHITE
                                             + "Check if you have won with " + ChatColor.RED
                                             + "/lottery claim");
                     }
 
                 } else {
                         if (args[0].equalsIgnoreCase("buy")) {
 
 
                             // Is this a console? If so, just tell that lottery is running and time until next draw.
                             if(!(sender instanceof Player)) {
                                 sender.sendMessage(ChatColor.GOLD
                                                 + "[LOTTERY] " + ChatColor.WHITE
                                                 + "You're the console, I can't sell you tickets.");
                                 return true;
                             }
                             Player player = (Player) sender;
 
 
                             // How many tickets do the player want to buy?
                             int buyTickets = 1;
                             // Let's check if the user tries to be funny
                             if (args.length > 1) {
                                     try {
                                             @SuppressWarnings("unused")
                                             int x = Integer.parseInt(args[1]);
                                     } catch (NumberFormatException nFE) {
                                             player.sendMessage(ChatColor.GOLD
                                                             + "[LOTTERY] " + ChatColor.WHITE
                                                             + "Use a number! /lottery buy <number>");
                                             // Just setting args[1] to 1;
                                             args[1] = "1";
                                     }
                             }
 
                             if (args.length < 2) {
                                     buyTickets = 1;
                             } else if (Integer.parseInt(args[1].toString()) + playerInList(player) <= maxTicketsEachUser) {
                                     buyTickets = Integer.parseInt(args[1].toString());
                             } else if (Integer.parseInt(args[1].toString()) + playerInList(player) > maxTicketsEachUser) {
                                     buyTickets = maxTicketsEachUser - playerInList(player);
                             } else {
                                     buyTickets = 1;
                             }
 
                             if (buyTickets < 1) {
                                     buyTickets = 1;
                             }
                             
                             // Have the admin entered a max number of tickets in the lottery?
                             if(numberOfTicketsAvailable > 0) {
                             	// If so, can this user buy the selected amount?
                             	if(ticketsSold() + buyTickets > numberOfTicketsAvailable) {
                             		if(ticketsSold() >= numberOfTicketsAvailable) {
                             			player.sendMessage(ChatColor.GOLD
                                                 + "[LOTTERY] " + ChatColor.WHITE
                                                 + "There are no more tickets available");
                             			return true;
                             		} else {
                             			buyTickets = numberOfTicketsAvailable - ticketsSold();
                             		}
                             	}
                             }
 
                             if (addPlayer(player, maxTicketsEachUser, buyTickets) == true) {
                                 // You got your ticket.
                                 if (useiConomy == false) {
                                         player.sendMessage(ChatColor.GOLD
                                                         + "[LOTTERY] " + ChatColor.WHITE
                                                         + "You got " + buyTickets + " "
                                                         + pluralWording("ticket", buyTickets)
                                                         + " for " + ChatColor.RED
                                                         + Lottery.cost * buyTickets + " "
                                                         + formatMaterialName(material));
                                 } else {
                                         player.sendMessage(ChatColor.GOLD
                                                         + "[LOTTERY] "
                                                         + ChatColor.WHITE
                                                         + "You got "
                                                         + buyTickets
                                                         + " "
                                                         + pluralWording("ticket", buyTickets)
                                                         + " for "
                                                         + ChatColor.RED
                                                         + Method.format(Lottery.cost
                                                                         * buyTickets));
                                 }
                                 // Can a user buy more than one ticket? How many
                                 // tickets have he bought now?
                                 if (maxTicketsEachUser > 1) {
                                         player.sendMessage(ChatColor.GOLD
                                                         + "[LOTTERY] "
                                                         + ChatColor.WHITE
                                                         + "You now have "
                                                         + ChatColor.RED
                                                         + playerInList(player)
                                                         + " "
                                                         + ChatColor.WHITE
                                                         + pluralWording("ticket",
                                                                         playerInList(player)));
                                 }
                                 if (broadcastBuying == true) {
                                         Bukkit.broadcastMessage(ChatColor.GOLD
                                                         + "[LOTTERY] " + ChatColor.WHITE
                                                         + player.getDisplayName()
                                                         + " just bought " + buyTickets + " "
                                                         + pluralWording("ticket", buyTickets));
                                 }
 
                             } else {
                                 // Something went wrong.
                                 player.sendMessage(ChatColor.GOLD
                                                 + "[LOTTERY] "
                                                 + ChatColor.WHITE
                                                 + "Either you can't afford a ticket, or you got " + maxTicketsEachUser + " " + pluralWording("ticket", maxTicketsEachUser) + " already.");
                             }
                         } else if (args[0].equalsIgnoreCase("claim")) {
 
                             // Is this a console? If so, just tell that lottery is running and time until next draw.
                             if(!(sender instanceof Player)) {
                                 sender.sendMessage(ChatColor.GOLD
                                                 + "[LOTTERY] " + ChatColor.WHITE
                                                 + "You're the console, you don't have an inventory.");
                                 return true;
                             }
                             Player player = (Player) sender;
 
 
                             removeFromClaimList((Player) sender);
                         } else if (args[0].equalsIgnoreCase("draw")) {
 
                             if (sender.hasPermission("lottery.admin.draw")) {
                                     // Start a timer that ends in 3 secs.
                                     sender.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                                     + ChatColor.WHITE
                                                     + "Lottery will be drawn at once.");
                                     StartTimerSchedule(true);
                             } else {
                                     sender.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                                     + ChatColor.WHITE
                                                     + "You don't have access to that command.");
                             }
 
                         } else if (args[0].equalsIgnoreCase("help")) {
                             sender.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                             + ChatColor.WHITE + "Help commands");
                             sender.sendMessage(ChatColor.RED + "/lottery"
                                             + ChatColor.WHITE + " : Basic lottery info.");
                             sender.sendMessage(ChatColor.RED + "/lottery buy <n>"
                                             + ChatColor.WHITE + " : Buy ticket(s).");
                             sender.sendMessage(ChatColor.RED + "/lottery claim"
                                             + ChatColor.WHITE + " : Claim outstandig wins.");
                             sender.sendMessage(ChatColor.RED + "/lottery winners"
                                             + ChatColor.WHITE + " : Check last winners.");
                             // Are we dealing with admins?
                             if (sender.hasPermission("lottery.admin.draw"))
                                     sender.sendMessage(ChatColor.BLUE + "/lottery draw"
                                                     + ChatColor.WHITE + " : Draw lottery.");
                             if (sender.hasPermission("lottery.admin.addtopot"))
                                     sender.sendMessage(ChatColor.BLUE
                                                     + "/lottery addtopot" + ChatColor.WHITE
                                                     + " : Add number to pot.");
                             if (sender.hasPermission("lottery.admin.editconfig"))
                                     sender.sendMessage(ChatColor.BLUE + "/lottery config"
                                                     + ChatColor.WHITE + " : Edit the config.");
 
                         } else if (args[0].equalsIgnoreCase("winners")) {
                             // Get the winners.
                             ArrayList<String> winnerArray = new ArrayList<String>();
                             try {
                                     BufferedReader in = new BufferedReader(
                                                     new FileReader(getDataFolder()
                                                                     + File.separator
                                                                     + "lotteryWinners.txt"));
                                     String str;
                                     while ((str = in.readLine()) != null) {
                                             winnerArray.add(str);
                                     }
                                     in.close();
                             } catch (IOException e) {
                             }
                             String[] split;
                             String winListPrice;
                             for (int i = 0; i < winnerArray.size(); i++) {
                                     split = winnerArray.get(i).split(":");
                                     if (split[2].equalsIgnoreCase("0")) {
                                             winListPrice = Method.format(Double
                                                             .parseDouble(split[1]));
                                     } else {
                                             winListPrice = split[1]
                                                             + " "
                                                             + formatMaterialName(
                                                                             Integer.parseInt(split[2]))
                                                                             .toString();
                                     }
                                     sender.sendMessage((i + 1) + ". " + split[0] + " "
                                                     + winListPrice);
                             }
                         } else if (args[0].equalsIgnoreCase("addtopot")) {
                             // Do we trust this person?
                             if (sender.hasPermission("lottery.admin.addtopot")) {
                                     if (args[1] == null) {
                                             sender.sendMessage(ChatColor.GOLD
                                                             + "[LOTTERY] " + ChatColor.WHITE
                                                             + "/lottery addtopot <number>");
                                             return true;
                                     }
                                     int addToPot = 0;
                                     // Is it a number?
                                     try {
                                             addToPot = Integer.parseInt(args[1]);
                                     } catch (NumberFormatException nFE) {
                                             sender.sendMessage(ChatColor.GOLD
                                                             + "[LOTTERY] " + ChatColor.WHITE
                                                             + "Not a number.");
                                             return true;
                                     }
                                     extraInPot += addToPot;
                                     c.set("extraInPot", extraInPot);
                                     try {
 										c.save(new File(getDataFolder().getPath() + getDataFolder().separator + "config.yml"));
 									} catch (IOException e) {
 										// TODO Auto-generated catch block
 										e.printStackTrace();
 									}
 
                                     sender.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                                     + ChatColor.WHITE + "Added "
                                                     + ChatColor.GREEN + addToPot
                                                     + ChatColor.WHITE
                                                     + " to pot. Extra total is "
                                                     + ChatColor.GREEN + extraInPot);
                             } else {
                                     sender.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                                     + ChatColor.WHITE
                                                     + "You don't have access to that command.");
                             }
                         } else if (args[0].equalsIgnoreCase("config")) {
                             // Do we trust this person?
                             if (sender.hasPermission("lottery.admin.editconfig")) {
                                     // Did the admin provide any additional args or should we show options?
                                     if (args.length == 1) {
                                             sender.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                                             + ChatColor.WHITE + "Edit config commands");
                                             sender.sendMessage(ChatColor.RED + "/lottery config cost <i>");
                                             sender.sendMessage(ChatColor.RED + "/lottery config hours <i>");
                                             sender.sendMessage(ChatColor.RED + "/lottery config maxTicketsEachUser <i>");
                                             sender.sendMessage(ChatColor.RED + "/lottery config reload");
                                     } else if(args.length >= 2) {
                                             if(args[1].equalsIgnoreCase("cost")) {
                                                     if(args.length == 2) {
                                                             sender.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                                                             + ChatColor.WHITE + "Please provide a number");
                                                             return true;
                                                     } else {
                                                             int newCoin = 0;
                                                             try {
                                                                     newCoin = Integer.parseInt(args[2].toString());
                                                             } catch (NumberFormatException e) {
                                                                     //e.printStackTrace();
                                                             }
                                                             if(newCoin <= 0) {
                                                                     sender.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                                                                     + ChatColor.WHITE + "Provide a integer (number) greater than zero");
                                                                     return true;
                                                             } else {
                                                                     sender.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                                                                     + ChatColor.WHITE + "Cost changed to "
                                                                                     + ChatColor.RED + newCoin);
                                                                     c.set("cost", newCoin);
                                                                     // Save the configuration
                                                                     try {
 																		c.save(new File(getDataFolder().getPath() + getDataFolder().separator + "config.yml"));
 																	} catch (IOException e) {
 																		// TODO Auto-generated catch block
 																		e.printStackTrace();
 																	}
                                                                     // Reload the configuration
                                                                     loadConfig();
                                                             }
 
                                                     }
                                             } else if(args[1].equalsIgnoreCase("hours")) {
                                                     if(args.length == 2) {
                                                             sender.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                                                             + ChatColor.WHITE + "Please provide a number");
                                                             return true;
                                                     } else {
                                                             int newHours = 0;
                                                             try {
                                                                     newHours = Integer.parseInt(args[2].toString());
                                                             } catch (NumberFormatException e) {
                                                                     //e.printStackTrace();
                                                             }
                                                             if(newHours <= 0) {
                                                                     sender.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                                                                     + ChatColor.WHITE + "Provide a integer (number) greater than zero");
                                                                     return true;
                                                             } else {
                                                                     sender.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                                                                     + ChatColor.WHITE + "Hours changed to "
                                                                                     + ChatColor.RED + newHours);
                                                                     c.set("hours", newHours);
                                                                     // Save the configuration
                                                                     try {
 																		c.save(new File(getDataFolder().getPath() + getDataFolder().separator + "config.yml"));
 																	} catch (IOException e) {
 																		// TODO Auto-generated catch block
 																		e.printStackTrace();
 																	}
                                                                     // Reload the configuration
                                                                     loadConfig();
                                                             }
 
                                                     }
                                             } else if(args[1].equalsIgnoreCase("maxTicketsEachUser") || args[1].equalsIgnoreCase("max")) {
                                                     if(args.length == 2) {
                                                             sender.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                                                             + ChatColor.WHITE + "Please provide a number");
                                                             return true;
                                                     } else {
                                                             int newMaxTicketsEachUser = 0;
                                                             try {
                                                                     newMaxTicketsEachUser = Integer.parseInt(args[2].toString());
                                                             } catch (NumberFormatException e) {
                                                                     //e.printStackTrace();
                                                             }
                                                             if(newMaxTicketsEachUser <= 0) {
                                                                     sender.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                                                                     + ChatColor.WHITE + "Provide a integer (number) greater to or equal to zero");
                                                                     return true;
                                                             } else {
                                                                     sender.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                                                                     + ChatColor.WHITE + "Max amount of tickets changed to "
                                                                                     + ChatColor.RED + newMaxTicketsEachUser);
                                                                     c.set("maxTicketsEachUser", newMaxTicketsEachUser);
                                                                     // Save the configuration
                                                                     try {
 																		c.save(new File(getDataFolder().getPath() + getDataFolder().separator + "config.yml"));
 																	} catch (IOException e) {
 																		// TODO Auto-generated catch block
 																		e.printStackTrace();
 																	}
                                                                     // Reload the configuration
                                                                     loadConfig();
                                                             }
 
                                                     }
                                             } else if(args[1].equalsIgnoreCase("config")) {
                                                     // Lets just reload the config.
                                                     loadConfig();
                                                     sender.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                                                     + ChatColor.WHITE + "Config reloaded");
                                             }
                                     }
                                     // Let's save the configuration, just in case something was changed.
                             }
                     } else {
 
                             sender.sendMessage(ChatColor.GOLD + "[LOTTERY] "
                                             + ChatColor.WHITE
                                             + "Hey, I don't recognize that command!");
                     }
                 }
 
                 return true;
             }
 		});
 
 		// Is the date we are going to draw the lottery set? If not, we should
 		// do it.
		if (nextexec == 0) {
 
 			// Set first time to be config hours later? Millisecs, * 1000.
 			nextexec = System.currentTimeMillis() + extendTime();
 			c.set("nextexec", nextexec);
 
 			try {
 				c.save(new File(getDataFolder().getPath() + getDataFolder().separator + "config.yml"));
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else {
 			nextexec = c.getLong("nextexec");
 		}
 
 		// Start the timer for the first time.
 		StartTimerSchedule(false);
 
 		// This could, and should, probably be fixed nicer, but for now it'll
 		// have to do.
 		// Adding timer that waits the time between nextexec and time now.
 
 	}
 
     @Override
 	public void onLoad() {
 		getDataFolder().mkdirs();
 
 		// Load the config.
 		loadConfig();
         
 		// Woa, custom messages?
         msgConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder().getPath() + getDataFolder().separator + "customMessages.yml"));
         try {
 			msgConfig.load(new File(getDataFolder().getPath() + getDataFolder().separator + "customMessages.yml"));
 		} catch (FileNotFoundException e1) {
 			// TODO Auto-generated catch block
 			try {
 				msgConfig.save(new File(getDataFolder().getPath() + getDataFolder().separator + "customMessages.yml"));
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (InvalidConfigurationException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
         
         loadCustomMessages();
         try {
 			msgConfig.save(new File(getDataFolder().getPath() + getDataFolder().separator + "customMessages.yml"));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// Gets version number and writes out starting line to console.
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println(pdfFile.getName() + " version "
 				+ pdfFile.getVersion() + " is enabled");
 
 	}
 
 	public static org.bukkit.Server getBukkitServer() {
 		return server;
 	}
 
 	public boolean getWinner() {
 		ArrayList<String> players = playersInFile("lotteryPlayers.txt");
 
 		if (players.isEmpty() == true) {
 			Bukkit.broadcastMessage(ChatColor.GOLD + "[LOTTERY] "
 				+ ChatColor.WHITE
 				+ "No tickets sold this round. Thats a shame.");
 			return false;
 		} else {
 			// Find rand. Do minus 1 since its a zero based array.
 			int rand = 0;
 
 			// is max number of tickets 0? If not, include empty tickets not sold.
 			if(numberOfTicketsAvailable > 0 && ticketsSold() < numberOfTicketsAvailable) {
 				rand = new Random().nextInt(numberOfTicketsAvailable);
 				if(players.get(rand) == null) {
 					// No winner this time, pot goes on to jackpot!
 					Integer amount = winningAmount();
 					jackpot = jackpot + amount;
 					c.set("jackpot", jackpot);
 					addToWinnerList("Jackpot", amount, useiConomy ? 0 : material);
 					c.set("lastwinner", "Jackpot");
 					c.set("lastwinneramount", amount);
 					Bukkit.broadcastMessage(ChatColor.GOLD + "[LOTTERY] "
 						+ ChatColor.WHITE
 						+ "No winner! "
 						+ ChatColor.GREEN
 						+ amount
 						+ " "
 						+ ((useiConomy)? Method.format(amount) : material)
 						+ ChatColor.WHITE
 						+ " went to jackpot!");
 					Bukkit.broadcastMessage(ChatColor.GOLD + "[LOTTERY] "
 						+ ChatColor.WHITE
 						+ "It is now "
 						+ ChatColor.GREEN
 						+ jackpot
 						+ (useiConomy ? Method.format(jackpot) : material)
 						+ ChatColor.WHITE
 						+ " in the jackpot.");
 					clearAfterGettingWinner();
 				}
 			} else {
 				// Else just continue
 				rand = new Random().nextInt(players.size());
 			}
 			
 
 			debugMsg("Rand: " + Integer.toString(rand));
 			int amount = winningAmount();
 			if (useiConomy == true) {
 				Method.hasAccount(players.get(rand));
 				MethodAccount account = Method.getAccount(players.get(rand));
 
 				// Just make sure the account exists, or make it with default
 				// value.
 				// Add money to account.
 				account.add(amount);
 				// Announce the winner:
 				Bukkit.broadcastMessage(ChatColor.GOLD + "[LOTTERY] "
 						+ ChatColor.WHITE + "Congratulations to "
 						+ players.get(rand) + " for winning " + ChatColor.RED
 						+ Method.format(amount) + ".");
 				addToWinnerList(players.get(rand), amount, 0);
 			} else {
 				Bukkit.broadcastMessage(ChatColor.GOLD + "[LOTTERY] "
 						+ ChatColor.WHITE + "Congratulations to "
 						+ players.get(rand) + " for winning " + ChatColor.RED
 						+ amount + " " + formatMaterialName(material) + ".");
 				Bukkit.broadcastMessage(ChatColor.GOLD + "[LOTTERY] "
 						+ ChatColor.WHITE + "Use " + ChatColor.RED
 						+ "/lottery claim" + ChatColor.WHITE
 						+ " to claim the winnings.");
 				addToWinnerList(players.get(rand), amount, material);
 				addToClaimList(players.get(rand), amount, material.intValue());
 			}
 			Bukkit.broadcastMessage(ChatColor.GOLD
 					+ "[LOTTERY] "
 					+ ChatColor.WHITE
 					+ "There was in total "
 					+ realPlayersFromList(players).size()
 					+ " "
 					+ pluralWording("player", realPlayersFromList(players)
 							.size()) + " buying " + players.size() + " "
 					+ pluralWording("ticket", players.size()));
 
 			// Add last winner to config.
 			c.set("lastwinner", players.get(rand));
 			c.set("lastwinneramount", amount);
 
 			clearAfterGettingWinner();
 		}
 		return true;
 	}
 	
 	public void clearAfterGettingWinner() {
 
 		// extra money in pot added by admins and mods?
 		// Should this be removed?
 		if (clearExtraInPot == true) {
 			c.set("extraInPot", 0);
 			extraInPot = 0;
 		}
 		// Clear file.
 					try {
 						BufferedWriter out = new BufferedWriter(
 								new FileWriter(getDataFolder() + File.separator
 										+ "lotteryPlayers.txt", false));
 						out.write("");
 						out.close();
 
 					} catch (IOException e) {
 					}
 	}
 
 	void StartTimerSchedule(boolean drawAtOnce) {
 
 		long extendtime = 0;
 		// Cancel any existing timers.
 		if (timerStarted == true) {
 			// Let's try and stop any running threads.
 			try {
 				Bukkit.getServer().getScheduler().cancelTasks((Plugin) this);
 			} catch (ClassCastException exception) {
 			}
 			;
 
 			extendtime = extendTime();
 		} else {
 			// Get time until lottery drawing.
 			extendtime = nextexec - System.currentTimeMillis();
 		}
 		// What if the admin changed the config to a shorter time? lets check,
 		// and if
 		// that is the case, lets use the new time.
 		if (System.currentTimeMillis() + extendTime() < nextexec) {
 			nextexec = System.currentTimeMillis() + extendTime();
 
 			c.set("nextexec", Lottery.nextexec);
 			try {
 				c.save(new File(getDataFolder().getPath() + getDataFolder().separator + "config.yml"));
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		// If the time is passed (perhaps the server was offline?), draw lottery
 		// at once.
 		if (extendtime <= 0) {
 			extendtime = 100;
 		}
 
 		// Is the drawAtOnce boolean set to true? In that case, do drawing in a
 		// few secs.
 		if (drawAtOnce) {
 			extendtime = 100;
 			c.set("nextexec", System.currentTimeMillis() + 100);
 			nextexec = System.currentTimeMillis() + 100;
 			debugMsg("DRAW NOW");
 		}
 
 		// Delay in server ticks. 20 ticks = 1 second.
 		extendtime = extendtime / 1000 * 20;
 
 		checkWhatMethodToUse(extendtime);
 
 		// Timer is now started, let it know.
 		timerStarted = true;
 	}
 
 	class LotteryDraw extends TimerTask {
 		public void run() {
 			// Cancel timer.
 			// Get new config.
 			debugMsg("Doing a lottery draw");
 			
 			Lottery.nextexec = c.getLong("nextexec");
 
 			if (Lottery.nextexec > 0
 					&& System.currentTimeMillis() + 1000 >= Lottery.nextexec) {
 				// Get the winner, if any. And remove file so we are ready for
 				// new round.
 				debugMsg("Getting winner.");
 				if (getWinner() == false) {
 					debugMsg("Failed getting winner");
 				}
 				Lottery.nextexec = System.currentTimeMillis() + extendTime();
 
 				c.set("nextexec", Lottery.nextexec);
 				try {
 					c.save(new File(getDataFolder().getPath() + getDataFolder().separator + "config.yml"));
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			// Call a new timer.
 			StartTimerSchedule(false);
 		}
 	}
 
 	class extendLotteryDraw extends TimerTask {
 		public void run() {
 			// Cancel timer.
 			try {
 				Bukkit.getServer().getScheduler().cancelTasks((Plugin) this);
 			} catch (ClassCastException exception) {
 			}
 			;
 
 			nextexec = c.getLong("nextexec");
 
 			long extendtime = 0;
 
 			// How much time left? Below 0?
 			if (nextexec < System.currentTimeMillis()) {
 				extendtime = 3000;
 			} else {
 				extendtime = nextexec - System.currentTimeMillis();
 			}
 			// Delay in server ticks. 20 ticks = 1 second.
 			extendtime = extendtime / 1000 * 20;
 
 			checkWhatMethodToUse(extendtime);
 
 		}
 	}
 
 	void checkWhatMethodToUse(long extendtime) {
 		// Is this very long until? On servers with lag and long between
 		// restarts there might be a very long time between when server
 		// should have drawn winner and when it will draw. Perhaps help the
 		// server a bit by only scheduling for half the lengt at a time?
 		// But only if its more than 5 seconds left.
 		if (extendtime < 5 * 20) {
 			Bukkit.getServer()
 					.getScheduler()
 					.scheduleAsyncDelayedTask((Plugin) this, new LotteryDraw(),
 							extendtime);
 			debugMsg("LotteryDraw() " + extendtime + 100);
 		} else {
 			extendtime = extendtime / 15;
 			Bukkit.getServer()
 					.getScheduler()
 					.scheduleAsyncDelayedTask((Plugin) this,
 							new extendLotteryDraw(), extendtime);
 			debugMsg("extendLotteryDraw() " + extendtime);
 		}
 		// For bugtesting:
 	}
 
 	
 	public void loadConfig() {
 		
 		YamlConfiguration c = YamlConfiguration.loadConfiguration(new File(getDataFolder().getPath() + getDataFolder().separator + "config.yml"));
 		try {
 			c.load(new File(getDataFolder().getPath() + getDataFolder().separator + "config.yml"));
 		} catch (FileNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (InvalidConfigurationException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		cost = c.getInt("cost",5);
 		c.set("cost", cost);
 		hours = c.getInt("hours", 24);
 		c.set("hours", hours);
 		useiConomy = c.getBoolean("useiConomy", true);
 		c.set("useiConomy", useiConomy);
 		material = c.getInt("material", 266);
 		c.set("material", material);
 		broadcastBuying = c.getBoolean("broadcastBuying", true);
 		c.set("broadcastBuying", broadcastBuying);
 		welcomeMessage = c.getBoolean("welcomeMessage", true);
 		c.set("welcomeMessage", welcomeMessage);
 		extraInPot = c.getInt("extraInPot", 0);
 		c.set("extraInPot", extraInPot);
 		clearExtraInPot = c.getBoolean("clearExtraInPot", true);
 		c.set("clearExtraInPot", clearExtraInPot);
 		netPayout = c.getInt("netPayout", 100);
 		c.set("netPayout", netPayout);
 		maxTicketsEachUser = c.getInt("maxTicketsEachUser", 1);
 		c.set("maxTicketsEachUser", maxTicketsEachUser);
 		numberOfTicketsAvailable = c.getInt("numberOfTicketsAvailable", 0);
 		c.set("numberOfTicketsAvailable", numberOfTicketsAvailable);
 		jackpot = c.getInt("jackpot", 0);
 		c.set("jackpot", jackpot);
 		nextexec = c.getLong("nextexec", System.currentTimeMillis() + extendTime());
 		c.set("nextexec", nextexec);
 		
 		try {
 			c.save(new File(getDataFolder().getPath() + getDataFolder().separator + "config.yml"));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			debugMsg("Error with saving config");
 		}
 	}
     
     public void loadCustomMessages() {
            
         msgWelcome = formatCustomMessage("welcome", "&6[LOTTERY] &fDraw in: &c%drawLong%");
         
         
         // After loading all strings do a save.
         try {
 			msgConfig.save(new File(getDataFolder().getPath() + getDataFolder().separator + "customMessages.yml"));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         
     }
     
     public ArrayList<String> formatCustomMessage (String node, String def) {
         ArrayList<String> fList = new ArrayList<String>();
         // Lets find a msg.
         String msg = msgConfig.getString(node, def);
         msgConfig.set(node, msg);
         
         // Lets put this in a arrayList in case we want more than one line.
         Collections.addAll(fList, msg.split("%newline%"));
         
         return fList;
     }
     
     public String formatCustomMessageLive (String msg, Player player) {
         //Lets give timeLeft back if user provie %draw%
         msg = msg.replaceAll("%draw%", timeUntil(nextexec, true));
         //Lets give timeLeft with full words back if user provie %drawLong%
         msg = msg.replaceAll("%drawLong%", timeUntil(nextexec, false));
         // If %player% = Player name
         msg = msg.replaceAll("%player%", player.getDisplayName());
         // %cost% = cost
         msg = msg.replaceAll("%cost%", cost.toString());
         // %pot%
         msg = msg.replaceAll("%pot%", Integer.toString(winningAmount()));
         // Lets get some colors on this, shall we?
         msg = msg.replaceAll("(&([a-f0-9]))", "\u00A7$2");
         return msg;
     }
 
 	public long extendTime() {
 		hours = c.getInt("hours");
 		Long extendTime = Long.parseLong(hours.toString()) * 60 * 60 * 1000;
 		return extendTime;
 	}
 
 	public ArrayList<String> playersInFile(String file) {
 		ArrayList<String> players = new ArrayList<String>();
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(
 					getDataFolder() + File.separator + file));
 			String str;
 			while ((str = in.readLine()) != null) {
 				// add players to array.
 				players.add(str.toString());
 			}
 			in.close();
 		} catch (IOException e) {
 		}
 		return players;
 	}
 
 	public boolean addPlayer(Player player,
 			Integer maxAmountOfTickets, Integer numberOfTickets) {
 
 		if (playerInList(player) + numberOfTickets > maxAmountOfTickets) {
 			return false;
 		}
 
 		// Do the ticket cost money or item?
 		if (Lottery.useiConomy == false) {
 			// Do the user have the item?
 			if (player.getInventory().contains(Lottery.material,
 					Lottery.cost * numberOfTickets)) {
 				// Remove items.
 				player.getInventory().removeItem(
 						new ItemStack(Lottery.material, Lottery.cost
 								* numberOfTickets));
 			} else {
 				return false;
 			}
 		} else {
 			// Do the player have money?
 			// First checking if the player got an account, if not let's create
 			// it.
 			Method.hasAccount(player.getName());
                         Method.
 
 			MethodAccount account = Method.getAccount(player.getName());
 
 			// And lets withdraw some money
 			if (account.hasOver(Lottery.cost * numberOfTickets - 1)) {
 				// Removing coins from players account.
 				account.subtract(Lottery.cost * numberOfTickets);
 			} else {
 				return false;
 			}
 
 		}
 		// If the user paid, continue. Else we would already have sent return
 		// false
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(
 					getDataFolder() + File.separator + "lotteryPlayers.txt",
 					true));
 			for (Integer i = 0; i < numberOfTickets; i++) {
 				out.write(player.getName());
 				out.newLine();
 			}
 			out.close();
 
 		} catch (IOException e) {
 		}
 
 		return true;
 	}
 
 	public Integer playerInList(Player player) {
 		int numberOfTickets = 0;
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(
 					getDataFolder() + File.separator + "lotteryPlayers.txt"));
 			String str;
 			while ((str = in.readLine()) != null) {
 
 				if (str.equalsIgnoreCase(player.getName())) {
 					numberOfTickets = numberOfTickets + 1;
 				}
 			}
 			in.close();
 		} catch (IOException e) {
 		}
 
 		return numberOfTickets;
 	}
 
 	public int winningAmount() {
 		int amount = 0;
 		ArrayList<String> players = playersInFile("lotteryPlayers.txt");
 		amount = players.size() * Lottery.cost;
 		// Set the net payout as configured in the config.
 		if (netPayout > 0) {
 			amount = amount * netPayout / 100;
 		}
 		// Add extra money added by admins and mods?
 		amount += extraInPot;
 		// Any money in jackpot?
 		amount += jackpot;
 
 		return amount;
 	}
 	
 	public int ticketsSold() {
 		int sold = 0;
 		ArrayList<String> players = playersInFile("lotteryPlayers.txt");
 		sold = players.size();
 		return sold;
 	}
 
 	public String formatMaterialName(int materialId) {
 		String returnMaterialName = "";
 		String rawMaterialName = Material.getMaterial(materialId).toString();
 		rawMaterialName = rawMaterialName.toLowerCase();
 		// Large first letter.
 		String firstLetterCapital = rawMaterialName.substring(0, 1)
 				.toUpperCase();
 		rawMaterialName = firstLetterCapital
 				+ rawMaterialName.substring(1, rawMaterialName.length());
 		returnMaterialName = rawMaterialName.replace("_", " ");
 
 		return returnMaterialName;
 	}
 
 	public boolean removeFromClaimList(Player player) {
 		// Do the player have something to claim?
 		ArrayList<String> otherPlayersClaims = new ArrayList<String>();
 		ArrayList<String> claimArray = new ArrayList<String>();
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(
 					getDataFolder() + File.separator + "lotteryClaim.txt"));
 			String str;
 			while ((str = in.readLine()) != null) {
 				String[] split = str.split(":");
 				if (split[0].equals(player.getName())) {
 					// Adding this to player claim.
 					claimArray.add(str);
 				} else {
 					otherPlayersClaims.add(str);
 				}
 			}
 			in.close();
 		} catch (IOException e) {
 		}
 
 		// Did the user have any claims?
 		if (claimArray.isEmpty()) {
 			player.sendMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE
 					+ "You did not have anything unclaimed.");
 			return false;
 		}
 		// Do a bit payout.
 		for (int i = 0; i < claimArray.size(); i++) {
 			String[] split = claimArray.get(i).split(":");
 			int claimAmount = Integer.parseInt(split[1]);
 			int claimMaterial = Integer.parseInt(split[2]);
 			player.getInventory().addItem(
 					new ItemStack(claimMaterial, claimAmount));
 			player.sendMessage("You just claimed " + claimAmount + " "
 					+ formatMaterialName(claimMaterial) + ".");
 		}
 
 		// Add the other players claims to the file again.
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(
 					getDataFolder() + File.separator + "lotteryClaim.txt"));
 			for (int i = 0; i < otherPlayersClaims.size(); i++) {
 				out.write(otherPlayersClaims.get(i));
 				out.newLine();
 			}
 
 			out.close();
 
 		} catch (IOException e) {
 		}
 		return true;
 	}
 
 	public boolean addToClaimList(String playerName, int winningAmount,
 			int winningMaterial) {
 		// Then first add new winner, and after that the old winners.
 		try {
 			BufferedWriter out = new BufferedWriter(
 					new FileWriter(getDataFolder() + File.separator
 							+ "lotteryClaim.txt", true));
 			out.write(playerName + ":" + winningAmount + ":" + winningMaterial);
 			out.newLine();
 			out.close();
 		} catch (IOException e) {
 		}
 		return true;
 	}
 
 	public boolean addToWinnerList(String playerName, int winningAmount,
 			int winningMaterial) {
 		// This list should be 10 players long.
 		ArrayList<String> winnerArray = new ArrayList<String>();
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(
 					getDataFolder() + File.separator + "lotteryWinners.txt"));
 			String str;
 			while ((str = in.readLine()) != null) {
 				winnerArray.add(str);
 			}
 			in.close();
 		} catch (IOException e) {
 		}
 		// Then first add new winner, and after that the old winners.
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(
 					getDataFolder() + File.separator + "lotteryWinners.txt"));
 			out.write(playerName + ":" + winningAmount + ":" + winningMaterial);
 			out.newLine();
 			// How long is the array? We just want the top 9. Removing index 9
 			// since its starting at 0.
 			if (winnerArray.size() > 0) {
 				if (winnerArray.size() > 9) {
 					winnerArray.remove(9);
 				}
 				// Go trough list and output lines.
 				for (int i = 0; i < winnerArray.size(); i++) {
 					out.write(winnerArray.get(i));
 					out.newLine();
 				}
 			}
 			out.close();
 
 		} catch (IOException e) {
 		}
 		return true;
 	}
 
 	public String timeUntil(long time, boolean mini) {
 
 		double timeLeft = Double.parseDouble(Long.toString(((time - System
 				.currentTimeMillis()) / 1000)));
 		// If negative number, just tell them its DRAW TIME!
 		if (timeLeft < 0) {
                     // Lets make it draw at once.. ;)
                     StartTimerSchedule(true);
                     // And return some string to let the user know we are doing our best ;)
                     if(mini) {
                         return "Soon";
                     }
                     return "Draw will occur soon!";
 
 		}
 
 		// How many days left?
 		String stringTimeLeft = "";
 		
 		if (timeLeft >= 60 * 60 * 24) {
 			int days = (int) Math.floor(timeLeft / (60 * 60 * 24));
 			timeLeft -= 60 * 60 * 24 * days;
 			if (!mini) {
 				stringTimeLeft += Integer.toString(days) + " " + pluralWording("day", days) + ", ";
 			} else {
 				stringTimeLeft += Integer.toString(days) + "d ";
 			}
 		}
 		if (timeLeft >= 60 * 60) {
 			int hours = (int) Math.floor(timeLeft / (60 * 60));
 			timeLeft -= 60 * 60 * hours;
 			if (!mini) {
 				stringTimeLeft += Integer.toString(hours) + " " + pluralWording("hour", hours) + ", ";
 			} else {
 				stringTimeLeft += Integer.toString(hours) + "h ";
 			}
 		}
 		if (timeLeft >= 60) {
 			int minutes = (int) Math.floor(timeLeft / (60));
 			timeLeft -= 60 * minutes;
 			if (!mini) {
 				stringTimeLeft += Integer.toString(minutes) + " " + pluralWording("minute", minutes) + ", ";
 			} else {
 				stringTimeLeft += Integer.toString(minutes) + "m ";
 			}
 		} else {
 			// Lets remove the last comma, since it will look bad with 2 days, 3
 			// hours, and 14 seconds.
 			if (stringTimeLeft.equalsIgnoreCase("") == false && !mini) {
 				stringTimeLeft = stringTimeLeft.substring(0,
 						stringTimeLeft.length() - 1);
 			}
 		}
 		int secs = (int) timeLeft;
 		if (stringTimeLeft.equalsIgnoreCase("") == false && !mini) {
 			stringTimeLeft += "and ";
 		}
 		if (!mini) {
                     stringTimeLeft += Integer.toString(secs) + " " + pluralWording("second", secs);
                 } else {
                     stringTimeLeft += secs + "s";
 		}
 
 		return stringTimeLeft;
 	}
 
 	/*
 	// Stolen from ltguide! Thank you so much :)
 	public Boolean hasPermission(CommandSender sender, String node,
 			Boolean needOp) {
 		if (!(sender instanceof Player))
 			return true;
 
 		Player player = (Player) sender;
 		if (Permissions != null)
 			return Permissions.has(player, node);
 		else {
 			Plugin test = getServer().getPluginManager().getPlugin(
 					"Permissions");
 			if (test != null) {
 				Permissions = ((Permissions) test).getHandler();
 				return Permissions.has(player, node);
 			}
 		}
 		
 		
 		
 		if (needOp) {
 			return player.isOp();
 		}
 		return true;
 	}
 	*/
 
 	public static String pluralWording(String word, Integer number) {
 		// Start
 		if (word.equalsIgnoreCase("ticket")) {
 			if (number == 1) {
 				return "ticket";
 			} else {
 				return "tickets";
 			}
 		}
 		// Next
 		if (word.equalsIgnoreCase("player")) {
 			if (number == 1) {
 				return "player";
 			} else {
 				return "players";
 			}
 		}
 		// Next
 		if (word.equalsIgnoreCase("day")) {
 			if (number == 1) {
 				return "day";
 			} else {
 				return "days";
 			}
 		}
 		// Next
 		if (word.equalsIgnoreCase("hour")) {
 			if (number == 1) {
 				return "hour";
 			} else {
 				return "hours";
 			}
 		}
 		// Next
 		if (word.equalsIgnoreCase("minute")) {
 			if (number == 1) {
 				return "minute";
 			} else {
 				return "minutes";
 			}
 		}
 		// Next
 		if (word.equalsIgnoreCase("second")) {
 			if (number == 1) {
 				return "second";
 			} else {
 				return "seconds";
 			}
 		}
 		// Next
 		return "i don't know that word";
 	}
 	
 	// Enable some debugging?
 	public void debugMsg(String msg) {
 		if(c.getBoolean("debug") == true) {
 			if(msg != null) {
 				log.info(msg);
 				getServer().broadcastMessage(msg);
 			}
 		}
 	}
 
 	public Hashtable<String, Integer> realPlayersFromList(
 			ArrayList<String> ticketList) {
 		Hashtable<String, Integer> playerList = new Hashtable<String, Integer>();
 		int value = 0;
 		for (String check : ticketList) {
 			if (playerList.containsKey(check)) {
 				value = Integer.parseInt(playerList.get(check).toString()) + 1;
 			} else {
 				value = 1;
 			}
 			playerList.put(check, value);
 		}
 		return playerList;
 	}
 
 }
