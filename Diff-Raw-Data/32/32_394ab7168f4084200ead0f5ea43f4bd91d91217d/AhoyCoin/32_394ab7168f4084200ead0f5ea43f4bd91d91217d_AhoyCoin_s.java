 package uk.co.ahoyworld;
  
 import java.util.Date;
 import java.util.HashMap;
 import java.util.logging.Logger;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
  
 public class AhoyCoin extends JavaPlugin {
 			    
 	static File configFile;
 	static File townsFile;
     static File basePriceFile;
     static File projectsFile;
     static File playerListFile;
     static FileConfiguration config;
     static FileConfiguration towns;
     static FileConfiguration basePrices;
     static FileConfiguration projects;
     static FileConfiguration playerList;
     Logger log;
     
     private ProjectsCommand projectsExecutor;
     
 	public static Date now = new Date();
 	public static String [] signText = new String [4];
 	
 	public static boolean replenishStartUp = true;
     
     String pre = ChatColor.GOLD + "[AhoyCoin]" + ChatColor.WHITE + " ";
     
     public HashMap<String, String> phrases = new HashMap<String, String>(); // ID = node name; String = phrase
     public static HashMap<String, String> replenishThreads = new HashMap<String, String>(); // ID = task ID; String [] = "TownName", "ItemName"
     
     public Long getTimeStamp()
     {
     	Date now = new Date();
     	Long timeStamp = now.getTime();
     	return timeStamp;
     }
     
     public void onEnable()
     {
     	new Event_onBlockClick(this);
     	projectsExecutor = new ProjectsCommand(this);
     	getCommand("project").setExecutor(projectsExecutor);
     	
     	//Phrases.getPhrases();
     	
         configFile = new File(getDataFolder(), "config.yml");
         townsFile = new File(getDataFolder(), "towns.yml");
         basePriceFile = new File(getDataFolder(), "basePrice.yml");
         projectsFile = new File(getDataFolder(), "projects.yml");
         playerListFile = new File(getDataFolder(), "playerList.yml");
      
         try
         {
             firstRun();
         } catch (Exception e) {
             e.printStackTrace();
         }
      
         config = new YamlConfiguration();
         towns = new YamlConfiguration();
         basePrices = new YamlConfiguration();
         projects = new YamlConfiguration();
         playerList = new YamlConfiguration();
 
         loadYamls();
         
         log = this.getLogger();
         
         replenishStartUp = true;
         Integer replenishTime = -1;
         
     	for (String town : towns.getKeys(false))
     	{
     		log.info("Key 1: " + town);
     		for (String item : towns.getConfigurationSection(town + ".items").getKeys(false))
     		{
     			log.info("Key 2: " + item);
     			if (towns.getKeys(true).contains(town + ".items." + item + ".replenishtimer"))
 				{
     				log.info("GOT ONE!");
     				if (towns.getKeys(true).contains(town + ".items." + item + ".replenishtime"))
     				{
     					replenishTime = towns.getInt(town + ".items." + item + ".replenishtime");
     				} else {
     					replenishTime = basePrices.getInt(item + ".replenishtime");
     				}
 					createReplenishTimer(town, item, ((replenishTime * 24000) - towns.getInt(town + ".items." + item + ".replenishtimer")), (replenishTime * 24000));
 				}
     		}
     	}
         
     	replenishStartUp = false;
     	
         log.info("Plugin enabled.");
     }
  
     public static void saveYamls()
     {
         try
         {
             config.save(configFile);
             towns.save(townsFile);
             basePrices.save(basePriceFile);
             projects.save(projectsFile);
             playerList.save(playerListFile);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
  
     public void loadYamls()
     {
         try
         {
             config.load(configFile);
             towns.load(townsFile);
             basePrices.load(basePriceFile);
             projects.load(projectsFile);
             playerList.save(playerListFile);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
  
     private void firstRun() throws Exception
     {
         if (!configFile.exists())
         {
             configFile.getParentFile().mkdirs();
             copy(getResource("config.yml"), configFile);
         }
         if (!townsFile.exists())
         {
             townsFile.getParentFile().mkdirs();
             copy(getResource("towns.yml"), townsFile);
         }
         if (!basePriceFile.exists())
         {
             basePriceFile.getParentFile().mkdirs();
             copy(getResource("basePrice.yml"), basePriceFile);
         }
         if (!projectsFile.exists())
         {
         	projectsFile.getParentFile().mkdirs();
         	copy(getResource("projects.yml"), projectsFile);
         }
         if (!playerListFile.exists())
         {
         	playerListFile.getParentFile().mkdirs();
         	copy(getResource("playerList.yml"), playerListFile);
         }
     }
  
     private void copy(InputStream in, File file)
     {
         try
         {
             OutputStream out = new FileOutputStream(file);
             byte[] buf = new byte[1024];
             int len;
             while ((len=in.read(buf))>0)
             {
                 out.write(buf,0,len);
             }
             out.close();
             in.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
  
     public void createReplenishTimer(String townName, String itemName, Integer initialDelay, Integer replenishTime)
     {
     	log.info("Replenish Timer Creator called! Town: " + townName + ", Item: " + itemName);
     	if (AhoyCoin.replenishStartUp)
     	{
     		signText[1] = townName;
     		signText[2] = itemName;
     	}
     	log.info("Sign 1: " + signText[1] + ", Sign 2: " + signText[2]);
     	log.info("Delay: " + initialDelay.toString() + ", Time: " + replenishTime.toString());
 		replenish.taskID = getServer().getScheduler().scheduleSyncRepeatingTask(this, new replenish(null), (long) initialDelay, (long) replenishTime);
 		
 		Integer startTime = (int) (System.currentTimeMillis() / 1000L);
 		System.out.println("[AhoyCoin] Task ran with an ID of " + Integer.toString(replenish.taskID) + ".");
 		String townNameTime = townName + "," + itemName + "," + startTime.toString();
 		AhoyCoin.replenishThreads.put(Integer.toString(replenish.taskID), townNameTime);
 
     }
     
     public void onDisable()
     {
     	// interrupt all running async threads and get their current time (split) in ticks
     	// save this value in towns.yml as "replenishtimer" under the appropriate item
     	// refer to onEnable for the rest...
     	
     	Integer i = 2;
     	Long ranForSecs = -1L;
     	Integer ranForTicks = -1;
     	String townName = "";
     	String itemName = "";
     	Integer replenishTime = -1;
     	double timesRun = -1;
     	Integer timesRunRounded = -1;
     	//Integer extraneousData = -1;
     	Integer finalTicks = -1;
     	Integer startTimeUnix = -1;
     	
     	for (String townNameTime : replenishThreads.values())
     	{
     		//Long timeStamp = getTimeStamp();
     		Integer timeStamp = (int) (System.currentTimeMillis() / 1000L);
     		String [] taskInfo = townNameTime.split(",");
     		townName = taskInfo[0];
     		itemName = taskInfo[1];
     		startTimeUnix = (int) (Long.parseLong(taskInfo[2]));
     		ranForSecs = (long) (timeStamp - startTimeUnix);
     		log.info("Current Time: " + timeStamp.toString() + ", Start Time: " + startTimeUnix.toString() + ", Ran for " + ranForSecs.toString() + " secs.");
     		//ranForSecs = (currentTime.longValue() - Long.parseLong(taskInfo[2]));
     		//log.info("Current Time: " + currentTime.toString() + ", Start Time: " + taskInfo[2]);
     		ranForTicks = (int) (ranForSecs * 20);
     		log.info("Task ID: " + i.toString() + ", Town: " + townName + ", Item: " + itemName + ".");
     		
     		if (towns.getKeys(true).contains(townName + ".items." + itemName + ".replenishtime"))
     		{
     			replenishTime = towns.getInt(townName + ".items." + itemName + ".replenishtime");
     		} else {
     			replenishTime = basePrices.getInt(itemName + ".replenishtime"); 
     		}
     		
     		if (ranForTicks > (replenishTime * 24000))
     		{
     			timesRun = ranForTicks / 24000;
     		} else {
     			timesRun = 0;
     		}
     		
     		timesRunRounded = (int)Math.floor(timesRun);
     		if (!(timesRun == 0))
     		{
     			//act normal. just take away the extraneous
         		finalTicks = (ranForTicks - (timesRunRounded * 24000));
     		} else {
     			//change up. add on to the timer!
         		finalTicks = (ranForTicks - (timesRunRounded * 24000));
     		}
     		//finalTicks = ranForTicks - extraneousData;
     		log.info("replenishTime: " + replenishTime.toString());
     		log.info("ranForTicks: " + ranForTicks.toString());
     		//log.info("extraneousData: " + extraneousData.toString());
     		log.info("timesRunRounded: " + timesRunRounded.toString());
     		log.info("Task ID: " + i.toString() + ", Final Ticks: " + finalTicks.toString() + ".");
     		
     		towns.set(townName + ".items." + itemName + ".replenishtimer", finalTicks);
     		
     		this.getServer().getScheduler().cancelTask(i);
     		i++;
     	}
     	
     	phrases.clear();
     	
         saveYamls();
         log.info("Plugin disabled.");
     }
     
     public void loadConfiguration()
     {
         this.getConfig().options().copyDefaults(true);
         this.saveConfig();
     }
     
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
     {
     	Player player = (Player) sender;
         if (cmd.getName().equalsIgnoreCase("ac"))
         {
             if (args.length == 1)
             {
                 if (args[0].equalsIgnoreCase("town"))
                 {
                     player.sendMessage(pre + "Usage: /AC town [list, add, remove]");
                     return true;
                 }
                 
                 if (args[0].equalsIgnoreCase("reload"))
                 {
                 	this.reloadConfig();
                 	return true;
                 }
                 
                 if (args[0].equalsIgnoreCase("testphrase"))
                 {
                 	player.sendMessage(pre + phrases.get("sign_created"));
                 }
                 
                 if (args[0].equalsIgnoreCase("disable"))
                 {
                 	onDisable();
                 }
                 
                 if (args[0].equalsIgnoreCase("enable"))
                 {
                 	onEnable();
                 }
             }
             if (args.length == 2)
             {
                 if (args[0].equalsIgnoreCase("town"))
                 {
                     if (args[1].equalsIgnoreCase("list"))
                     {
                     	if (!towns.getKeys(false).isEmpty())
                     	{
                     		String townsListStr = "";
                         	for (String str : towns.getKeys(false))
                         	{
                         		townsListStr = townsListStr + str + ", ";
                         	}
                         	player.sendMessage(pre + "List of created towns:");
                         	player.sendMessage(pre + "" + townsListStr);
                     	} else {
                     		player.sendMessage(pre + "No towns have been created.");
                     	}
                     	
                         return true;	
                     }
                     if (args[1].equalsIgnoreCase("add"))
                     {
                         player.sendMessage(pre + "Type the name of the town you would like to add");
                         player.sendMessage(pre + "Usage: /AC town add [name]");
                         return true;
                     }
                     if (args[1].equalsIgnoreCase("remove"))
                     {
                     	player.sendMessage(pre + "Name of town to remove not provided.");
                     	return true;
                     }
                 }
             }
             
             if (args.length == 3)
             {
             	if (args[0].equalsIgnoreCase("town"))
             	{
             		if (args[1].equalsIgnoreCase("add"))
             		{
             			int defaultTax = config.getInt("defaults.tax");
             			
             			towns.set(args[2].toString() + ".tax", defaultTax);
             			saveYamls();
             			player.sendMessage(pre + "Town \"" + args[2].toString() + "\" added.");
             			
             			return true;
             		}
             		
                 	if (args[1].equalsIgnoreCase("remove"))
                 	{
                 		if (!towns.getKeys(false).isEmpty())
                 		{
                 			for (String str : towns.getKeys(false))
                 			{
                 				if (str.equalsIgnoreCase(args[2].toString()))
                 				{
                 					towns.set(str, null);
                 					saveYamls();
                         			player.sendMessage(pre + "Town \"" + args[2].toString() + "\" removed.");
                         			return true;
                 				}
                 			}
 	        	    		player.sendMessage(pre + "Town \"" + args[2].toString() + "\" does not exist.");
 	        	    		return true;
                 		} else {
                 	    	player.sendMessage(pre + "There are no towns to remove.");
                 	    	return true;
                 		}
                 	}
             	}
             	
             	if (args[0].equalsIgnoreCase("pricereset"))
             	{
             		String town = args[1].toString();
             		String item = args[2].toString().toLowerCase();
             		
             		if (towns.getKeys(true).contains(town + ".items." + item))
             		{
             			// item exists
             			towns.set(town + ".items." + item, null);
             			saveYamls();
             			player.sendMessage(pre + "Item \"" + item + "\" for town \"" + town + "\" reset to defaults.");
             			return true;
             		} else {
             			// item doesn't exist
             			player.sendMessage(pre + "Item \"" + item + "\" for town \"" + town + "\" already set to defaults.");
             			return true;
             		}
             	}
             	
             	if (args[0].equalsIgnoreCase("taxset"))
             	{
             		boolean townExists = false;
             		String town = "";
             		for (String str : towns.getKeys(false))
             		{
             			if (str.equalsIgnoreCase(args[1]))
             			{
             				townExists = true;
             				town = str;
             				break;
             			}
             		}
             		if (townExists)
             		{
             			Integer tax = Integer.parseInt(args[2]);
             			towns.set(town + ".tax", tax);
             			saveYamls();
             			player.sendMessage(pre + "Tax rate for town \"" + town + "\" set to " + tax.toString() + "%.");
             			return true;
             		} else {
             			player.sendMessage(pre + "Town \"" + town + "\" does not exist.");
             			return true;
             		}
             	}
             }
             
             if (args.length >= 4)
             {
             	if (args[0].equalsIgnoreCase("priceset"))
             	{
             		String town = "";
             		String item = "";
             		
             		//Check town exists
             		boolean townExists = false;
             		for (String str : towns.getKeys(false))
             		{
             			if (args[1].toString().equalsIgnoreCase(str))
             			{
             				townExists = true;
             				town = str;
             				break;
             			}
             		}
             		if (townExists)
             		{
             			//Check item exists
             			boolean itemExists = false;
             			for (String str : basePrices.getKeys(false))
             			{
             				if (args[2].toString().equalsIgnoreCase(str))
             				{
             					itemExists = true;
             					item = str;
             					break;
             				}
             			}
             			if (itemExists)
             			{
             				//Submit!
             				player.sendMessage(pre + "Setting custom item for " + town + ":");
             				String reportStr = "Item \"" + item + "\"";
             				if (args.length > 3)
             				{
             					//Price
             					towns.set(town + ".items." + item + ".price", Integer.parseInt(args[3]));
             					reportStr = reportStr + " now sells for " + args[3].toString();
             				}
             				if (args.length > 4)
             				{
             					//Max Stock
             					towns.set(town + ".items." + item + ".maxstock", Integer.parseInt(args[4]));
             					reportStr = reportStr + " with a maximum stock of " + args[4].toString();
             				} else if (towns.getConfigurationSection(town + ".items." + item).getKeys(false).contains("maxstock")) {
             					reportStr = reportStr + " with a maximum stock of " + towns.getString(town + ".items." + item + ".maxstock");
             				} else {
             					reportStr = reportStr + " with a maximum stock of " + basePrices.getString(item + ".maxstock");
             				}
             				if (args.length > 5)
             				{
             					//Replenish every X days
             					towns.set(town + ".items." + item + ".replenishtime", Integer.parseInt(args[5]));
             					reportStr = reportStr + ", replenishing every " + args[5].toString() + " days";
             				} else if (towns.getConfigurationSection(town + ".items." + item).getKeys(false).contains("replenishtime")) {
             					reportStr = reportStr + ", replenishing every " + towns.getString(town + ".items." + item + ".replenishtime");
             				} else {
             					reportStr = reportStr + ", replenishing every " + basePrices.getString(item + ".replenishtime") + " days";
             				}
             				if (args.length > 6)
             				{
             					//Replenish Amount
             					towns.set(town + ".items." + item + ".replenishamount", Integer.parseInt(args[6]));
             					reportStr = reportStr + " for " + args[6].toString() + ".";
             				} else if (towns.getConfigurationSection(town + ".items." + item).getKeys(false).contains("replenishamount")) {
             					reportStr = reportStr + " for " + towns.getString(town + ".items." + item + ".replenishamount");
             				} else {
             					reportStr = reportStr + " for " + basePrices.getString(item + ".replenishamount") + ".";
             				}
             				saveYamls();
             				player.sendMessage(reportStr);
             				return true;
             			} else {
             				player.sendMessage(pre + "Item \"" + args[2].toString() + "\" does not exist.");
             				return true;
             			}
             		} else {
             			player.sendMessage(pre + "Town \"" + args[1].toString() + "\" does not exist.");
             			return true;
             		}
             	}
             }
         }
         return false;
     }
 }
