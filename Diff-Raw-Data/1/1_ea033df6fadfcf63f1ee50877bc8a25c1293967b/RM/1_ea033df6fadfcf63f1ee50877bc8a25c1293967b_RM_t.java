 package com.mtaye.ResourceMadness;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
 
 import org.bukkit.ChatColor;
 
 import java.util.HashMap;
 import org.bukkit.entity.Player;
 import org.bukkit.DyeColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import java.io.*;
 
 import com.mtaye.ResourceMadness.RMGame.FilterState;
 import com.mtaye.ResourceMadness.RMGame.FilterType;
 import com.mtaye.ResourceMadness.RMGame.GameState;
 import com.mtaye.ResourceMadness.RMGame.ForceState;
 import com.mtaye.ResourceMadness.RMGame.InterfaceState;
 import com.mtaye.ResourceMadness.RMPlayer.ClaimType;
 import com.mtaye.ResourceMadness.RMPlayer.PlayerAction;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import com.nijiko.permissions.PermissionHandler;
 import com.ning.compress.lzf.LZFOutputStream;
 
 /**
  * ResourceMadness for Bukkit
  *
  * @author M-Taye
  */
 public class RM extends JavaPlugin {
 	private PluginDescriptionFile pdfFile;
 	public Logger log;
 	//public PermissionHandler Permissions = null;
 	@SuppressWarnings("unused")
 	
 	private String ver = "0.1";
 	
 	public HashMap<Player, Boolean> players = new HashMap<Player, Boolean>();
 
 	private RMBlockListener blockListener = new RMBlockListener(this);
 	private RMPlayerListener playerListener = new RMPlayerListener(this);
 	private RMLogListener logListener = new RMLogListener(this);
 	
 	private RMWatcher watcher;
 	private int watcherid;
 	//private RMInventoryListener inventoryListener = new RMPlayerListener(this);
 	
 	public enum DataType { CONFIG, STATS, PLAYER, GAME, LOG };
 	
 	public RM(){
 		RMPlayer.plugin = this;
 		RMGame.plugin = this;
 	}
 
 	public void onEnable(){
 		log = getServer().getLogger();
 		
 		watcher = new RMWatcher(this);
 		watcherid = getServer().getScheduler().scheduleSyncRepeatingTask(this, watcher, 25,25);
 	
 		//setupPermissions();
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
 		pm.registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
 		pm.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
 		pm.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
 		pm.registerEvent(Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
 		pm.registerEvent(Type.CUSTOM_EVENT, logListener, Priority.Normal, this);
 
 		pdfFile = this.getDescription();
 		log.log(Level.INFO, pdfFile.getName() + " v" + pdfFile.getVersion() + " enabled!" );
 		//RMConfig.load();
 		loadAll();
 	}
 	
 	public void onDisable(){
 		getServer().getScheduler().cancelTask(watcherid);
 		log.info(pdfFile.getName() + " disabled");
 		saveAll();
 		//RMConfig.save();
 	}
 	
 	public void saveAll(){
 		if(RMGame.getGames().size()==0) return;
 		File folder = getDataFolder();
 		if(!folder.exists()){
 			log.log(Level.INFO, "Creating config directory...");
 			folder.mkdir();
 		}
 		//save(DataType.CONFIG);
 		save(DataType.STATS);
 		save(DataType.PLAYER);
 		save(DataType.GAME);
 		save(DataType.LOG);
 		//saveLZF(DataType.LOG);
 	}
 	
 	public void saveLZF(DataType dataType){
 		File folder = getDataFolder();
 		File file = new File(folder.getAbsolutePath()+"/gamelog.txt");
 		if(!file.exists()){
 			try{
 				file.createNewFile();
 			}
 			catch(Exception e){
 				e.printStackTrace();
 				return;
 			}
 		}
 		try{
 			OutputStream output = new LZFOutputStream(new FileOutputStream(file.getAbsoluteFile()));
 			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(output));
 			String line = "";
 			
 			bw.write("[Resource Madness v"+pdfFile.getVersion()+" Game Data]");
 			for(RMGame rmGame : RMGame.getGames()){
 				RMGameConfig config = rmGame.getConfig();
 				line = "";
 				bw.write("\n");
 				//Game
 				//Log
 				//line += encodeLogToString(config.getLog());
 				for(int i=0; i<100; i++){
 					bw.write(line);
 				}
 			}
 			bw.flush();
 			output.close();
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 		
 	}
 	
 	//Save Data
 	public void save(DataType dataType){
 		File folder = getDataFolder();
 		File file = null;
 		switch(dataType){
 			case CONFIG: file = new File(folder.getAbsolutePath()+"/config.txt"); break;
 			case STATS: file = new File(folder.getAbsolutePath()+"/stats.txt"); break;
 			case PLAYER: file = new File(folder.getAbsolutePath()+"/playerdata.txt"); break;
 			case GAME: file = new File(folder.getAbsolutePath()+"/gamedata.txt"); break;
 			case LOG: file = new File(folder.getAbsolutePath()+"/gamelogdata.txt"); break;
 		}
 		if(file==null){
 			log.log(Level.WARNING, "Cannot load data. Data type unknown!");
 			return;
 		}
 		if(!file.exists()){
 			switch(dataType){
 				case CONFIG: log.log(Level.INFO, "Data file not found! Creating one..."); break;
 				case STATS: log.log(Level.INFO, "Stats file not found! Creating one..."); break;
 				case PLAYER: log.log(Level.INFO, "Player Data file not found! Creating one..."); break;
 				case GAME: log.log(Level.INFO, "Game Data file not found! Creating one..."); break;
 				case LOG: log.log(Level.INFO, "Game Data file not found! Creating one..."); break;
 			}
 			try{
 				file.createNewFile();
 			}
 			catch(Exception e){
 				e.printStackTrace();
 				return;
 			}
 		}
 		try{
 			FileOutputStream output = new FileOutputStream(file.getAbsoluteFile());
 			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(output));
 			String line = "";
 			switch(dataType){
 				case CONFIG:
 					bw.write("[Resource Madness v"+pdfFile.getVersion()+" Config]");
 					line = "";
 					bw.write("\n");
 					break;
 				case STATS:
 					bw.write("[Resource Madness v"+pdfFile.getVersion()+" Stats]");
 					//Stats
 					line = "";
 					bw.write("\n");
 					line += RMStats.getServerWins()+","+RMStats.getServerLosses()+","+RMStats.getServerTimesPlayed()+","+/*RMStats.getServerItemsFound()+","+*/RMStats.getServerItemsFoundTotal()+";";
 					bw.write(line);
 					break;
 				case PLAYER:
 					bw.write("[Resource Madness v"+pdfFile.getVersion()+" Player Data]");
 					for(RMPlayer rmp : RMPlayer.getPlayers().values()){
 						line = "";
 						bw.write("\n");
 						line = rmp.getName()+";";
 						//Stats
 						RMStats stats = rmp.getStats();
 						line += stats.getWins()+","+stats.getLosses()+","+stats.getTimesPlayed()+","+/*stats.getItemsFound()+","+*/stats.getItemsFoundTotal()+";";
 						//Inventory items
 						
 						line += encodeInventoryToString(rmp.getItems(), ClaimType.ITEMS) + ";";
 						line += encodeInventoryToString(rmp.getAward(), ClaimType.AWARD);
 						bw.write(line);
 					}
 					break;
 				case GAME:
 					bw.write("[Resource Madness v"+pdfFile.getVersion()+" Game Data]");
 					for(RMGame rmGame : RMGame.getGames()){
 						RMGameConfig config = rmGame.getConfig();
 						line = "";
 						bw.write("\n");
 						//Game
 						Block b = config.getPartList().getMainBlock();
 						line = b.getX()+","+b.getY()+","+b.getZ()+",";
 						line += config.getWorldName()+",";
 						line += config.getId()+",";
 						line += config.getOwnerName()+",";
 						line += config.getState().ordinal()+",";
 						line += config.getInterface().ordinal()+",";
 						line += config.getMaxPlayers()+",";
 						line += config.getMaxTeamPlayers()+",";
 						line += config.getAutoRandomizeAmount()+",";
 						line += config.getWarpToSafety()+",";
 						line += config.getAutoRestoreWorld()+",";
 						line += config.getWarnHackedItems()+",";
 						line += config.getAllowHackedItems()+",";
 						line += config.getKeepIngame()+",";
 						line += config.getAllowMidgameJoin()+",";
 						line += config.getClearPlayerInventory();
 						line += ";";
 						//Stats
 						RMStats stats = config.getGameStats();
 						line += stats.getWins()+","+stats.getLosses()+","+stats.getTimesPlayed()+","+/*stats.getItemsFound()+","+*/stats.getItemsFoundTotal()+";";
 						//Players
 						for(RMTeam rmt : config.getTeams()){
 							line+=rmt.getTeamColor().name()+":";
 							String players = "";
 							for(RMPlayer rmp : rmt.getPlayers()){
 								players += rmp.getName()+",";
 							}
 							players = stripLast(players,",");
 							line += players+" ";
 						}
 						line = stripLast(line, " ");
 						line += ";";
 						//Filter items
 						line += encodeFilterToString(config.getFilter().getItems(), FilterState.FILTER)+";";
 						//Game items
 						line += encodeFilterToString(config.getItems().getItems(), FilterState.ITEMS)+";";
 						//Chest items
 						for(RMTeam rmt : config.getTeams()){
 							line += encodeInventoryToString(rmt.getChest().getInventory(), ClaimType.ITEMS)+".";
 						}
 						line = stripLast(line, ".");
 						line += ";";
 						//Team items
 						for(RMTeam rmt : config.getTeams()){
 							line += encodeFilterToString(rmt.getChest().getItems(), FilterState.ITEMS)+".";
 						}
 						line = stripLast(line, ".");
 						bw.write(line);
 					}
 					break;
 				case LOG:
 					for(RMGame rmGame : RMGame.getGames()){
 						line = "";
 						//Log
 						RMGameConfig config = rmGame.getConfig();
 						line += encodeLogToString(config.getLog());
 						bw.write(line);
						bw.write("\n");
 					}
 					break;
 			}
 			bw.flush();
 			output.close();
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	public String encodeLogListToString(HashMap<Location, RMBlock> logList){
 		String line = "";
 		if(logList.size()==0) return "LOG";
 		
 		HashMap<String, HashMap<Integer, HashMap<Integer, List<String>>>> worldList = new HashMap<String, HashMap<Integer, HashMap<Integer, List<String>>>>();
 		
 		for(Location loc : logList.keySet()){
 			RMBlock rmBlock = logList.get(loc);
 			String world = loc.getWorld().getName();
 			int id = rmBlock.getType().getId();
 			int data = rmBlock.getData();
 			String pos = loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ();
 			if(!worldList.containsKey(world)) worldList.put(world, new HashMap<Integer, HashMap<Integer, List<String>>>());
 			
 			HashMap<Integer, HashMap<Integer, List<String>>> idList = worldList.get(world);
 			if(!idList.containsKey(id)) idList.put(id, new HashMap<Integer, List<String>>());
 				
 			HashMap<Integer, List<String>> dataList = idList.get(id);
 			if(!dataList.containsKey(data)) dataList.put(data, new ArrayList<String>());
 				
 			List<String> posList = dataList.get(data);
 			if(!posList.contains(pos)) posList.add(pos);
 		}
 		
 		for(String world : worldList.keySet()){
 			line+=world;
 			//ID
 			HashMap<Integer, HashMap<Integer, List<String>>> idList = worldList.get(world);
 			for(Integer id : idList.keySet()){
 				line+=":"+id;
 				//DATA
 				HashMap<Integer, List<String>> dataList = idList.get(id);
 				for(Integer data : dataList.keySet()){
 					line+="."+data;
 					//POS
 					List<String> posList = dataList.get(data);
 					for(String pos : posList){
 						line+=","+pos;
 					}
 				}
 			}
 			line+=" ";
 		}
 		line = stripLast(line, " ");
 		return line;
 	}
 	
 	public String encodeLogToString(RMLog log){
 		String line = "";
 		line += encodeLogListToString(log.getList())+";";
 		line += encodeLogListToString(log.getItemList());
 		return line;
 	}
 	
 	//Load All
 	public void loadAll(){
 		File folder = getDataFolder();
 		if(!folder.exists()){
 			log.log(Level.INFO, "Config folder not found! Will create one on save...");
 			return;
 		}
 		//load(DataType.CONFIG);
 		load(DataType.STATS);
 		load(DataType.PLAYER);
 		load(DataType.GAME);
 		load(DataType.LOG);
 	}
 	
 	public void load(DataType dataType){
 		File folder = getDataFolder();
 		File file = null;
 		switch(dataType){
 			case CONFIG: file = new File(folder.getAbsolutePath()+"/config.txt"); break;
 			case STATS: file = new File(folder.getAbsolutePath()+"/stats.txt"); break;
 			case PLAYER: file = new File(folder.getAbsolutePath()+"/playerdata.txt"); break;
 			case GAME: file = new File(folder.getAbsolutePath()+"/gamedata.txt"); break;
 			case LOG: file = new File(folder.getAbsolutePath()+"/gamelogdata.txt"); break;
 		}
 		if(file==null){
 			log.log(Level.WARNING, "Cannot load data. Data type unknown!");
 			return;
 		}
 		if(file.exists()){
 			FileInputStream input;
 			try {
 				input = new FileInputStream(file.getAbsoluteFile());
 				InputStreamReader isr = new InputStreamReader(input);
 				BufferedReader br = new BufferedReader(isr);
 				
 				String line;
 				while(true){
 					line = br.readLine();
 					if(line == null) break;
 					if(line.startsWith("[")) continue;
 					switch(dataType){
 						case CONFIG:
 							break;
 						case STATS:
 							String[] args = line.split(",");
 							//wins,losses,timesPlayed,itemsFound,itemsFoundTotal
 							RMStats.setServerWins(getIntByString(args[0]));
 							RMStats.setServerLosses(getIntByString(args[1]));
 							RMStats.setServerTimesPlayed(getIntByString(args[2]));
 							//RMStats.setServerItemsFound(getIntByString(args[3]));
 							RMStats.setServerItemsFoundTotal(getIntByString(args[3]));
 							break;
 						case PLAYER:
 							parseLoadedPlayerData(line.split(";"));
 							break;
 						case GAME:
 							parseLoadedData(line.split(";"));
 							break;
 						case LOG:
 							parseLoadedLogData(line.split(";"));
 							break;
 					}
 				}
 				input.close();
 				//saveConfig();
 			}
 			catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 		else{
 			switch(dataType){
 				case CONFIG: System.out.println("Could not find config file"); break;
 				case STATS: System.out.println("Could not find stats file"); break;
 				case PLAYER: System.out.println("Could not find player data file"); break;
 				case GAME: System.out.println("Could not find game data file"); break;
 			}
 		}
 	}
 	
 	public HashMap<Location, RMBlock> getLogDataByString(String strArg){
 		HashMap<Location, RMBlock> hashLog = new HashMap<Location, RMBlock>();
 		String[] worldArgs = strArg.split(" ");
 		for(String worldArg : worldArgs){
 			String[] idArgs = worldArg.split(":"); 
 			World world = getServer().getWorld(idArgs[0]);
 			if(world!=null){
 				idArgs = Arrays.copyOfRange(idArgs, 1, idArgs.length);
 				for(String idArg : idArgs){
 					String[] dataArgs = idArg.split("\\.");
 					int id = getIntByString(dataArgs[0]);
 					if(id!=-1){
 						Material mat = Material.getMaterial(id);
 						if(mat!=null){
 							dataArgs = Arrays.copyOfRange(dataArgs, 1, dataArgs.length);
 							for(String dataArg : dataArgs){
 								String[] posArgs = dataArg.split(",");
 								byte data = getByteByString(posArgs[0]);
 								if(data!=-1){
 									posArgs = Arrays.copyOfRange(posArgs, 1, posArgs.length);
 									for(int i=0; i<posArgs.length-2; i+=3){
 										int xPos = getIntByString(posArgs[i]);
 										int yPos = getIntByString(posArgs[i+1]);
 										int zPos = getIntByString(posArgs[i+2]);
 										Block b = world.getBlockAt(xPos, yPos, zPos);
 										hashLog.put(b.getLocation(), new RMBlock(b, mat, data));
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		return hashLog;
 	}
 	
 	public void parseLoadedLogData(String[] strArgs){
 		for(RMGame rmGame : RMGame.getGames()){
 			if(rmGame!=null){
 				RMGameConfig config = rmGame.getConfig();
 				if((strArgs[0].length()>0)&&(strArgs[0]!="LOG")) config.getLog().setList(getLogDataByString(strArgs[0]));
 				if((strArgs[1].length()>0)&&(strArgs[1]!="LOG")) config.getLog().setItemList(getLogDataByString(strArgs[1]));
 			}
 		}
 	}
 	
 	public void parseLoadedPlayerData(String[] strArgs){
 		//name
 		RMPlayer rmp = new RMPlayer(strArgs[0]);
 		
 		//wins,losses,timesPlayed,itemsFound,itemsFoundTotal
 		String[] args = strArgs[1].split(",");
 		RMStats stats = rmp.getStats();
 		
 		stats.setWins(getIntByString(args[0]));
 		stats.setLosses(getIntByString(args[1]));
 		stats.setTimesPlayed(getIntByString(args[2]));
 		//stats.setItemsFound(getIntByString(args[3]));
 		stats.setItemsFoundTotal(getIntByString(args[3]));
 		
 		//inventory items
 		if(!strArgs[2].equalsIgnoreCase("ITEMS")){
 			rmp.addItemsByItemStack(getItemStackByStringArray(strArgs[2]));
 		}
 		
 		//award items
 		if(!strArgs[3].equalsIgnoreCase("AWARD")){
 			rmp.addItemsByItemStack(getItemStackByStringArray(strArgs[3]));
 		}
 	}
 	
 	public ItemStack[] getItemStackByStringArray(String strArgs){
 		List<ItemStack> items = new ArrayList<ItemStack>();
 		String[] splitArgs = strArgs.split(",");
 		for(String splitArg : splitArgs){
 			String[] args = splitArg.split(":");
 			int id = getIntByString(args[0]);
 			int amount = getIntByString(args[1]);
 			short durability = getShortByString(args[2]);
 			if((id!=-1)&&(amount!=-1)&&(durability!=-1)){
 				if(args.length==4){
 					byte data = getByteByString(args[3]);
 					if(data!=-1){
 						Material mat = Material.getMaterial(id);
 						ItemStack item = new ItemStack(mat, amount, durability, data);
 						items.add(item);
 					}
 				}
 				else if(args.length==3){
 					Material mat = Material.getMaterial(id);
 					while(amount>mat.getMaxStackSize()){
 						ItemStack item = new ItemStack(mat, mat.getMaxStackSize(), durability);
 						items.add(item);
 						amount-=mat.getMaxStackSize();
 					}
 					ItemStack item = new ItemStack(mat,amount, durability);
 					items.add(item);
 				}
 			}
 		}
 		return items.toArray(new ItemStack[items.size()]);
 	}
 	
 	public void parseLoadedData(String[] strArgs){
 		
 		String[] args = strArgs[0].split(",");
 		//x,y,z,world,id,owner
 		int xLoc = getIntByString(args[0]);
 		int yLoc = getIntByString(args[1]);
 		int zLoc = getIntByString(args[2]);
 		World world = getServer().getWorld(args[3]);
 		Block b = world.getBlockAt(xLoc, yLoc, zLoc);
 
 		//maxPlayers,maxTeamPlayers,autoRandomizeAmount
 		//warpToSafety,autoRestoreWorld,warnHackedItems,allowHackedItems,allowPlayerLeave 
 		RMGameConfig config = new RMGameConfig();
 		config.setPartList(new RMPartList(b, this));
 		
 		config.setOwnerName(args[5]);
 		
 		config.setState(getStateByInt(getIntByString(args[6])));
 		config.setInterface(getInterfaceByInt(getIntByString(args[7])));
 		config.setMaxPlayers(getIntByString(args[8]));
 		config.setMaxTeamPlayers(getIntByString(args[9]));
 		config.setAutoRandomizeAmount(getIntByString(args[10]));
 		config.setWarpToSafety(Boolean.parseBoolean(args[11]));
 		config.setAutoRestoreWorld(Boolean.parseBoolean(args[12]));
 		config.setWarnHackedItems(Boolean.parseBoolean(args[13]));
 		config.setAllowHackedItems(Boolean.parseBoolean(args[14]));
 		config.setKeepIngame(Boolean.parseBoolean(args[15]));
 		config.setAllowMidgameJoin(Boolean.parseBoolean(args[16]));
 
 		//wins,losses,timesPlayed,itemsFound,itemsFoundTotal
 		args = strArgs[1].split(",");
 		RMStats gameStats = config.getGameStats();
 		
 		gameStats.setWins(getIntByString(args[0]));
 		gameStats.setLosses(getIntByString(args[1]));
 		gameStats.setTimesPlayed(getIntByString(args[2]));
 		//gameStats.setItemsFound(getIntByString(args[3]));
 		gameStats.setItemsFoundTotal(getIntByString(args[3]));
 		
 		//team players
 		args = strArgs[2].split(" ");
 		List<RMTeam> rmTeams = config.getPartList().fetchTeams();
 		for(RMTeam rmt : rmTeams){
 			config.getTeams().add(rmt);
 		}
 		for(int j=0; j<args.length; j++){
 			String[] splitArgs = args[j].split(":");
 			if(splitArgs.length==2){
 				if(splitArgs[1].length()>0){
 					String[] players = splitArgs[1].split(",");
 					for(String player : players){
 						RMTeam rmTeam = config.getTeams().get(j);
 						if(rmTeam!=null){
 							rmTeam.addPlayerSilent(RMPlayer.getPlayerByName(player));
 						}
 					}
 				}
 			}
 		}
 			
 		//filter items
 		if(!strArgs[3].equalsIgnoreCase("FILTER")){
 			HashMap<Integer, RMItem> rmItems = getRMItemsByStringArray(Arrays.asList(strArgs[3]), true);
 			config.setFilter(new RMFilter(rmItems));
 		}
 		
 		//game items
 		if(!strArgs[4].equalsIgnoreCase("ITEMS")){
 			HashMap<Integer, RMItem> rmItems = getRMItemsByStringArray(Arrays.asList(strArgs[4]), true);
 			config.setItems(new RMFilter(rmItems));
 		}
 		
 		//chest items
 		args = strArgs[5].split("\\.");
 		for(int j=0; j<args.length; j++){
 			if(!args[j].equalsIgnoreCase("ITEMS")){
 				ItemStack[] items = getItemStackByStringArray(args[j]);
 				List<ItemStack> inventory = new ArrayList<ItemStack>();
 				for(ItemStack item : items){
 					inventory.add(item);
 				}
 				RMTeam rmTeam = config.getTeams().get(j);
 				if(rmTeam!=null){
 					rmTeam.getChest().setInventory(inventory);
 				}
 			}
 		}
 		
 		//team items
 		args = strArgs[6].split("\\.");
 		for(int j=0; j<args.length; j++){
 			if(!args[j].equalsIgnoreCase("ITEMS")){
 				HashMap<Integer, RMItem> rmItems = getRMItemsByStringArray(Arrays.asList(args[j]), true);
 				RMTeam rmTeam = config.getTeams().get(j);
 				if(rmTeam!=null){
 					rmTeam.getChest().setItems(rmItems);
 				}
 			}
 		}
 		RMGame.tryAddGameFromConfig(config);
 	}
 	
 	public String encodeInventoryToString(ItemStack[] items, ClaimType claimType){
 		String line = "";
 		if((items!=null)&&(items.length>0)){
 			HashMap<String, ItemStack> hashItems = new HashMap<String, ItemStack>();
 			for(ItemStack item : items){
 				if(item!=null){
 					String idData = ""+item.getTypeId()+":"+item.getDurability();
 					if(item.getData()!=null) idData += ":"+Byte.toString(item.getData().getData());
 					if(hashItems.containsKey(idData)){
 						int amount = hashItems.get(idData).getAmount()+item.getAmount();
 						item.setAmount(amount);
 						hashItems.put(idData, item);
 					}
 					else hashItems.put(idData, item);
 				}
 			}
 			String[] array = hashItems.keySet().toArray(new String[hashItems.size()]);
 			Arrays.sort(array);
 			for(String idData : array){
 				//String[] splitItems = idData.split(":");
 				//int id = getIntByString(splitItems[0]);
 				ItemStack item = hashItems.get(idData);
 				line+=item.getTypeId()+":"+item.getAmount()+":"+item.getDurability();
 				if(item.getData()!=null) line+=":"+Byte.toString(item.getData().getData());
 				line+=",";
 			}
 		}
 		if(line.length()==0){
 			switch(claimType){
 				case ITEMS:
 					return "ITEMS";
 				case AWARD:
 					return "AWARD";
 			}
 		}
 		line = stripLast(line,",");
 		return line;
 	}
 
 	public String encodeFilterToString(HashMap<Integer, RMItem> filter, FilterState filterState){
 		if(filter.size()==0){
 			switch(filterState){
 				case FILTER: return "FILTER";
 				case ITEMS: return "ITEMS";
 			}
 		}
 		HashMap<Integer, String> rmItems = new HashMap<Integer, String>();
 		for(RMItem rmItem : filter.values()){
 			String amount = ""+rmItem.getAmount();
 			if(rmItem.getAmountHigh()>0) amount+="-"+rmItem.getAmountHigh();
 			rmItems.put(rmItem.getId(), amount);
 		}
 		
 		HashMap<String, List<Integer>> foundItems = new HashMap<String, List<Integer>>();
 		for(Integer i : rmItems.keySet()){
 			String amount = rmItems.get(i);
 			if(foundItems.containsKey(rmItems.get(i))){
 				List<Integer> list = foundItems.get(amount);
 				if(!list.contains(i)) list.add(i);
 				foundItems.put(amount, list);
 			}
 			else{
 				List<Integer> list = new ArrayList<Integer>();
 				list.add(i);
 				foundItems.put(amount, list);
 			}
 		}
 	
 		String line = "";
 		for(String amount : foundItems.keySet()){
 			if(line!=""){
 				line = stripLast(line, ",");
 				line+=" ";
 			}
 			line += amount+":";
 			List<Integer> listAmount = foundItems.get(amount);
 			Integer[] array = listAmount.toArray(new Integer[listAmount.size()]);
 			Arrays.sort(array);
 			
 			int firstItem = -1;
 			int lastItem = -1;
 			for(Integer item : array){
 				if(firstItem==-1){
 					firstItem = item;
 					lastItem = item;
 				}
 				else{
 					if(item-lastItem!=1){
 						if(lastItem-firstItem>1){
 							line += firstItem+"-"+lastItem+",";
 						}
 						else{
 							if(firstItem!=lastItem) line += firstItem+","+lastItem+",";
 							else line += firstItem+",";
 						}
 						firstItem = item;
 						lastItem = item;
 					}
 					else lastItem = item;
 				}
 			}
 			if(lastItem-firstItem>1) line += firstItem+"-"+lastItem+",";
 			else{
 				if(firstItem!=lastItem) line += firstItem+","+lastItem+",";
 				else line += firstItem+",";
 			}
 		}
 		line = stripLast(line,",");
 		return line;
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]){
 		Player p = null;
 		if(sender.getClass().getName().contains("Player")){
 			p = (Player)sender;
 			RMPlayer rmp = RMPlayer.getPlayerByName(p.getName());
 			if(rmp!=null){
 				if(cmd.getName().equals("resourcemadness")){
 					if(args.length==0){
 						rmInfo(rmp);
 					}
 					else{
 						RMGame rmGame = null;
 						String[] argsItems = args.clone();
 						if(args.length>1){
 							int gameid = getIntByString(args[0]);
 							if(gameid!=-1){
 								rmGame = getGameById(gameid);
 								if(rmGame!=null){
 									List<String> argsList = Arrays.asList(args);
 									argsList = argsList.subList(1, argsList.size());
 									args = argsList.toArray(new String[argsList.size()]);
 								}
 							}
 						}
 						//ADD
 						if(args[0].equalsIgnoreCase("add")){
 							rmp.setPlayerAction(PlayerAction.ADD);
 							rmp.sendMessage("Left click a game block to create your new game.");
 							return true;
 						}
 						//REMOVE
 						else if(args[0].equalsIgnoreCase("remove")){
 							if(rmGame!=null){
 								RMGame.tryRemoveGame(rmGame, rmp, true);
 								return true;
 							}
 							else{
 								rmp.setPlayerAction(PlayerAction.REMOVE);
 								rmp.sendMessage("Left click a game block to remove your game.");
 								return true;
 							}
 						}
 						//LIST
 						else if(args[0].equalsIgnoreCase("list")){
 							if(args.length==2){
 								sendListById(args[1], rmp);
 								return true;
 							}
 							else{
 								sendListByInt(0, rmp);
 								return true;
 							}
 						}
 						//INFO
 						else if(args[0].equalsIgnoreCase("info")){
 							if(rmGame!=null){
 								rmGame.sendInfo(rmp);
 								return true;
 							}
 							else{
 								rmp.setPlayerAction(PlayerAction.INFO);
 								rmp.sendMessage("Left click a game block to get info.");
 								return true;
 							}
 						}
 						/*
 						//SAVE
 						else if(args[0].equalsIgnoreCase("save")){
 							saveData();
 							return true;
 							if(rmGame!=null){
 								rmGame.saveConfig();
 								return true;
 							}
 							else{
 								rmp.setPlayerAction(PlayerAction.SAVE_CONFIG);
 								rmp.sendMessage("Left click a game block to save your game game.");
 								return true;
 							}
 						}
 						//LOAD
 						else if(args[0].equalsIgnoreCase("load")){
 							loadData();
 							return true;
 						}
 						*/
 						//JOIN
 						else if(args[0].equalsIgnoreCase("join")){
 							if(args.length==2){
 								if(rmGame!=null){								
 									RMTeam rmTeam = getTeamById(args[1], rmGame);
 									if(rmTeam!=null){
 										rmGame.joinTeam(rmTeam, rmp);
 										return true;
 									}
 									rmTeam = getTeamByDye(args[1], rmGame);
 									if(rmTeam!=null){
 										rmGame.joinTeam(rmTeam, rmp);
 										return true;
 									}
 									rmp.sendMessage("This team does not exist!");
 									return true;
 								}
 							}
 							else{
 								rmp.setPlayerAction(PlayerAction.JOIN);
 								rmp.sendMessage("Left click a team block to join the team.");
 								return true;
 							}
 						}
 						//QUIT
 						else if(args[0].equalsIgnoreCase("quit")){
 							for(RMTeam rmTeam : RMTeam.getTeams()){
 								RMPlayer rmPlayer = rmTeam.getPlayer(rmp.getName());
 								if(rmPlayer!=null){
 									rmTeam.removePlayer(rmPlayer);
 									return true;
 								}
 							}
 							rmp.sendMessage("You did not yet join any team.");
 							return true;
 							
 						}
 						//START
 						else if(args[0].equalsIgnoreCase("start")){
 							if(args.length==2){
 								int amount = getIntByString(args[1]);
 								if(amount!=-1){
 									if(rmGame!=null){
 										rmGame.setRandomizeAmount(rmp, amount);
 										rmGame.startGame(rmp);
 										return true;
 									}
 									else{
 										rmp.setRequestInt(amount);
 										rmp.setPlayerAction(PlayerAction.START_RANDOM);
 										rmp.sendMessage("Left click a game block to start the game.");
 										return true;
 									}
 								}
 							}
 							else{
 								if(rmGame!=null){
 									rmGame.startGame(rmp);
 									return true;
 								}
 								else{
 									rmp.setPlayerAction(PlayerAction.START);
 									rmp.sendMessage("Left click a game block to start the game.");
 									return true;
 								}
 							}
 						}
 						//RESTART
 						else if(args[0].equalsIgnoreCase("restart")){
 							if(rmGame!=null){
 								rmGame.restartGame(rmp);
 								return true;
 							}
 							else{
 								rmp.setPlayerAction(PlayerAction.RESTART);
 								rmp.sendMessage("Left click a game block to restart the game.");
 								return true;
 							}
 						}
 						//STOP
 						else if(args[0].equalsIgnoreCase("stop")){
 							if(rmGame!=null){
 								rmGame.stopGame(rmp);
 								return true;
 							}
 							else{
 								rmp.setPlayerAction(PlayerAction.STOP);
 								rmp.sendMessage("Left click a game block to stop the game.");
 								return true;
 							}
 						}
 						//RESTORE WORLD
 						else if(args[0].equalsIgnoreCase("restore")){
 							if(rmGame!=null){
 								rmGame.restoreWorld(rmp);
 								return true;
 							}
 							else{
 								rmp.setPlayerAction(PlayerAction.RESTORE);
 								rmp.sendMessage("Left click a game block to restore world changes.");
 								return true;
 							}
 						}
 						//ITEMS
 						else if(args[0].equalsIgnoreCase("items")){
 							RMTeam rmTeam = rmp.getTeam();
 							if(rmTeam!=null){
 								RMGame rmg = rmTeam.getGame(); 
 								if(rmg!=null){
 									if(rmg.getConfig().getState()==GameState.GAMEPLAY){
 										rmg.updateGameplayInfo(rmp, rmTeam);
 										return true;
 									}
 								}
 							}
 							else{
 								rmp.sendMessage("You must be in a game to use this command.");
 								return false;
 							}
 						}
 						//FILTER
 						else if(args[0].equalsIgnoreCase("filter")){
 							if(args.length>1){
 								List<String> listArgs = new ArrayList<String>();
 								for(int i=1; i<args.length; i++){
 									listArgs.add(args[i]);
 								}
 								if(listArgs.size()>0){
 									if(rmGame!=null){
 										parseFilter(rmp, listArgs);
 										rmGame.tryParseFilter(rmp);
 										return true;
 									}
 									else{
 										parseFilter(rmp, listArgs);
 										rmp.setPlayerAction(PlayerAction.FILTER);
 										rmp.sendMessage("Left click a game block to modify the filter.");
 										return true;
 									}
 								}
 							}
 							rmFilterInfo(rmp);
 							return true;
 						}
 						//CLAIM
 						else if(args[0].equalsIgnoreCase("claim")){
 							if(args.length==2){
 								if(args[1].equalsIgnoreCase("items")){
 									RMTeam rmTeam = rmp.getTeam();
 									if((rmTeam==null)||((rmTeam!=null)&&(rmTeam.getGame().getConfig().getState()==GameState.SETUP))){
 										rmp.claimItems();
 									}
 									else rmp.sendMessage("You can't claim your items while you're ingame.");
 									return true;
 								}
 								else if(args[1].equalsIgnoreCase("award")){
 									RMTeam rmTeam = rmp.getTeam();
 									if((rmTeam==null)||((rmTeam!=null)&&(rmTeam.getGame().getConfig().getState()==GameState.SETUP))){
 										rmp.claimAward();
 									}
 									else rmp.sendMessage("You can't claim your award while you're ingame.");
 									return true;
 								}
 							}
 						}
 						//SET
 						else if(args[0].equalsIgnoreCase("set")){
 							if(args.length>1){
 								PlayerAction action = null;
 								//MAX PLAYERS
 								if(args[1].equalsIgnoreCase("maxplayers")) action = PlayerAction.SET_MAX_PLAYERS;
 								//MAX TEAM PLAYERS
 								else if(args[1].equalsIgnoreCase("maxteamplayers")) action = PlayerAction.SET_MAX_TEAM_PLAYERS;
 								//MAX ITEMS
 								else if(args[1].equalsIgnoreCase("maxitems")) action = PlayerAction.SET_MAX_ITEMS;
 								//AUTO RANDOM ITEMS
 								else if(args[1].equalsIgnoreCase("random")) action = PlayerAction.SET_RANDOM;
 								
 								if(action!=null){
 									if(args.length==3){
 										int amount = getIntByString(args[2]);
 										if(amount>-1){
 											if(rmGame!=null){
 												switch(action){
 													case SET_MAX_PLAYERS: rmGame.setMaxPlayers(rmp, amount); break;
 													case SET_MAX_TEAM_PLAYERS: rmGame.setMaxTeamPlayers(rmp, amount); break;
 													case SET_MAX_ITEMS: rmGame.setMaxItems(rmp, amount); break;
 													case SET_RANDOM: rmGame.setRandomizeAmount(rmp, amount); break;
 												}
 												return true;
 											}
 											else{
 												rmp.setRequestInt(getIntByString(args[2]));
 												switch(action){
 													case SET_MAX_PLAYERS:
 														rmp.setPlayerAction(PlayerAction.SET_MAX_PLAYERS);
 														rmp.sendMessage("Left click a game block to set max players.");
 														break;
 													case SET_MAX_TEAM_PLAYERS:
 														rmp.setPlayerAction(PlayerAction.SET_MAX_TEAM_PLAYERS);
 														rmp.sendMessage("Left click a game block to set max team players.");
 														break;
 													case SET_MAX_ITEMS:
 														rmp.setPlayerAction(PlayerAction.SET_MAX_ITEMS);
 														rmp.sendMessage("Left click a game block to set max items.");
 														break;
 													case SET_RANDOM:
 														rmp.setPlayerAction(PlayerAction.SET_RANDOM);
 														rmp.sendMessage("Left click a game block to set auto randomize items.");
 														break;
 												}
 												return true;
 											}
 										}
 									}
 								}
 								
 								//SET & TOGGLE
 								action = null;
 								//GATHER PLAYERS
 								if(args[1].equalsIgnoreCase("teleport")) action = PlayerAction.SET_WARP;
 								//AUTO RESTORE WORLD
 								else if(args[1].equalsIgnoreCase("restore")) action = PlayerAction.SET_RESTORE;
 								//WARN HACK ITEMS
 								else if(args[1].equalsIgnoreCase("warnhacked")) action = PlayerAction.SET_WARN_HACKED;
 								//ALLOW HACK ITEMS
 								else if(args[1].equalsIgnoreCase("allowhacked")) action = PlayerAction.SET_ALLOW_HACKED;
 								//ALLOW PLAYER LEAVE
 								else if(args[1].equalsIgnoreCase("keepingame")) action = PlayerAction.SET_KEEP_INGAME;
 								//CLEAR PLAYER INVENTORY
 								else if(args[1].equalsIgnoreCase("clearinventory")) action = PlayerAction.SET_CLEAR_INVENTORY;
 								//ALLOW MIDGAME JOIN
 								else if(args[1].equalsIgnoreCase("joinmidgame")) action = PlayerAction.SET_MIDGAME_JOIN;
 								
 								if(action!=null){
 									int i=-1;
 									if(args.length==3){
 										i = getBoolIntByString(args[2]);
 										if(i!=-1){
 											if(i>1) i=1;
 										}
 									}
 									if(rmGame!=null){
 										switch(action){
 											case SET_WARP: rmGame.setWarpToSafety(rmp, i); break;
 											case SET_RESTORE: rmGame.setAutoRestoreWorld(rmp, i); break;
 											case SET_WARN_HACKED: rmGame.setWarnHackedItems(rmp, i); break;
 											case SET_ALLOW_HACKED: rmGame.setAllowHackedItems(rmp, i); break;
 											case SET_KEEP_INGAME: rmGame.setKeepIngame(rmp, i); break;
 											case SET_CLEAR_INVENTORY: rmGame.setClearPlayerInventory(rmp, i); break;
 											case SET_MIDGAME_JOIN: rmGame.setAllowMidgameJoin(rmp, i); break;
 										}
 									}
 									else{
 										rmp.setRequestInt(i);
 										switch(action){
 											case SET_WARP:
 												rmp.setPlayerAction(PlayerAction.SET_WARP);
 												rmp.sendMessage("Left click a game block to toggle teleport players.");
 												break;
 											case SET_RESTORE:
 												rmp.setPlayerAction(PlayerAction.SET_RESTORE);
 												rmp.sendMessage("Left click a game block to toggle auto restore world.");
 												break;
 											case SET_WARN_HACKED:
 												rmp.setPlayerAction(PlayerAction.SET_WARN_HACKED);
 												rmp.sendMessage("Left click a game block to toggle warn hacked items.");
 												break;
 											case SET_ALLOW_HACKED:
 												rmp.setPlayerAction(PlayerAction.SET_ALLOW_HACKED);
 												rmp.sendMessage("Left click a game block to toggle allow hacked items.");
 												break;
 											case SET_KEEP_INGAME:
 												rmp.setPlayerAction(PlayerAction.SET_KEEP_INGAME);
 												rmp.sendMessage("Left click a game block to toggle allow player leave.");
 												break;
 											case SET_CLEAR_INVENTORY:
 												rmp.setPlayerAction(PlayerAction.SET_CLEAR_INVENTORY);
 												rmp.sendMessage("Left click a game block to toggle clear player inventory.");
 												break;
 											case SET_MIDGAME_JOIN:
 												rmp.setPlayerAction(PlayerAction.SET_MIDGAME_JOIN);
 												rmp.sendMessage("Left click a game block to toggle allow midgame join.");
 												break;
 										}
 									}
 									return true;
 								}
 							}
 							rmSetInfo(rmp);
 							return true;
 						}
 						//Get Item NAME by ID or Item ID by NAME
 						else{
 							List<String> items = new ArrayList<String>();
 							List<String> itemsWarn = new ArrayList<String>();
 							for(String str : argsItems){
 								String[] strItems = str.split(",");
 								for(String strItem : strItems){
 									for(Material mat : Material.values()){
 										if(strItem.equalsIgnoreCase(mat.name())){
 											if(!items.contains(mat)) items.add(ChatColor.WHITE+mat.name()+":"+ChatColor.YELLOW+mat.getId());
 										}
 									}
 									if(strItem.contains("-")){
 										String[] strItems2 = strItem.split("-");
 										int id1=getIntByString(strItems2[0]);
 										int id2=getIntByString(strItems2[1]);
 										if((id1!=-1)&&(id2!=-1)){
 											if(id1>id2){
 												int id3=id1;
 												id1=id2;
 												id2=id3;
 											}
 											while(id1<=id2){
 												Material mat = Material.getMaterial(id1);
 												if(mat!=null){
 													if(!items.contains(mat)) items.add(""+ChatColor.WHITE+id1+":"+ChatColor.YELLOW+Material.getMaterial(id1).name());
 												}
 												else if(!itemsWarn.contains(strItem)) itemsWarn.add(""+id1);
 												id1++;
 											}
 										}
 									}
 									else{
 										int id = getIntByString(strItem);
 										if(id!=-1){
 											Material mat = Material.getMaterial(id);
 											if(mat!=null){
 												if(!items.contains(mat)) items.add(""+ChatColor.WHITE+id+":"+ChatColor.YELLOW+Material.getMaterial(id).name());
 											}
 											else if(!itemsWarn.contains(strItem)) itemsWarn.add(""+id);
 										}
 									}
 								}
 							}
 							if(items.size()>0){
 								rmp.sendMessage(getFormattedStringByList(items));
 								return true;
 							}
 							else if(itemsWarn.size()>0){
 								rmp.sendMessage("These items don't exist!");
 								//rmp.sendMessage("These items don't exist: "+getFormattedStringByList(itemsWarn));
 								return true;
 							}
 						}
 						rmInfo(rmp);
 					}
 				}
 			}
 		}
 		return true;
 	}
 	
 	public void parseFilter(RMPlayer rmp, List<String> args){
 		int size = 0;
 		List<Integer> items = new ArrayList<Integer>();
 		List<Integer[]> amount = new ArrayList<Integer[]>();
 		FilterType type = null;
 		ForceState force = null;
 		int randomize = 0;
 		for(String arg : args){
 			arg = arg.replace(" ", "");
 		}
 		
 		if(args.size()>1){
 			/*
 			if(args.get(0).equalsIgnoreCase("add")){
 				force = ForceState.ADD;
 				size+=1;
 			}
 			*/
 			if(args.get(0).equalsIgnoreCase("remove")){
 				force = ForceState.REMOVE;
 				size+=1;
 			}
 			if(args.get(0).equalsIgnoreCase("random")){
 				randomize = getIntByString(args.get(1));
 				if(randomize>0){
 					force = ForceState.RANDOMIZE;
 					size+=2;
 				}
 			}
 		}
 		if(args.size()>0){
 			args = args.subList(size, args.size());
 			String arg0 = args.get(0);
 			if(arg0.contains("all")) type = FilterType.ALL;
 			else if(arg0.contains("clear")) type = FilterType.CLEAR;
 			else if(arg0.contains("block")) type = FilterType.BLOCK;
 			else if(arg0.contains("item")) type = FilterType.ITEM;
 			else if(arg0.contains("raw")) type = FilterType.RAW;
 			else if(arg0.contains("crafted")) type = FilterType.CRAFTED;
 
 			if((type!=null)&&(type!=FilterType.CLEAR)){
 				boolean useDefaultAmount = false;
 				items = getItemsFromFilter(type);
 				amount.clear();
 				if(arg0.contains("stack")) useDefaultAmount = true;
 				else if(arg0.contains(":")){
 					List<String> strArgs = splitArgs(arg0);
 					String strAmount = ""; 
 					String[] strSplit = strArgs.get(0).split(":");
 					if(strSplit.length>1){
 						strAmount = strSplit[1];
 						Integer[] intAmount = checkInt(strAmount);
 						if(intAmount!=null){
 							for(int i=0; i<items.size(); i++){							
 								amount.add(intAmount);
 							}
 						}
 						else items.clear();
 					}
 				}
 				else{
 					Integer[] intAmount = new Integer[1];
 					intAmount[0] = 1;
 					for(int i=0; i<items.size(); i++){							
 						amount.add(intAmount);
 					}
 				}
 				if(useDefaultAmount) amount = getDefaultAmount(items);
 			}
 			else{
 				HashMap<Integer, Integer[]> hashItems = getItemsByStringArray(args, false);
 				items = Arrays.asList(hashItems.keySet().toArray(new Integer[hashItems.size()]));
 				amount = Arrays.asList(hashItems.values().toArray(new Integer[hashItems.size()][]));
 			}
 			if((type==null)&&(items.size()==0)){
 				rmInfo(rmp);
 				return;
 			}
 			//HashMap<Integer, Integer[]> hashItems = new HashMap<Integer, Integer[]>();
 			HashMap<Integer, RMItem> rmItems = new HashMap<Integer, RMItem>();
 			for(int i=0; i<items.size(); i++){
 				//hashItems.put(items.get(i), amount.get(i));
 				
 				Integer iItem = items.get(i);
 				Integer[] iAmount = amount.get(i);
 				int amount1 = -1;
 				int amount2 = -1;
 				if(iAmount.length>0) amount1 = iAmount[0];
 				if(iAmount.length>1) amount2 = iAmount[1];
 				
 				RMItem rmItem = new RMItem(iItem);
 				if(amount1 > -1) rmItem.setAmount(amount1);
 				if(amount2 > -1) rmItem.setAmountHigh(amount2);
 				
 				rmItems.put(items.get(i), rmItem);
 			}
 			//rmp.setRequestFilter(hashItems, type, force);
 			rmp.setRequestFilter(rmItems, type, force, randomize);
 		}
 	}
 	
 	public HashMap<Integer, RMItem> getRMItemsByStringArray(List<String> args, boolean invert){
 		HashMap<Integer, RMItem> rmItems = new HashMap<Integer, RMItem>();
 		HashMap<Integer, Integer[]> items = getItemsByStringArray(args, invert);
 
 		for(Integer item : items.keySet()){
 			Integer[] amount = items.get(item);
 			int amount1 = -1;
 			int amount2 = -1;
 			if(amount.length>0) amount1 = amount[0];
 			if(amount.length>1) amount2 = amount[1];
 			
 			RMItem rmItem = new RMItem(item);
 			if(amount1 > -1) rmItem.setAmount(amount1);
 			if(amount2 > -1) rmItem.setAmountHigh(amount2);
 			
 			rmItems.put(item, rmItem);
 		}
 		return rmItems;
 	}
 	
 	public HashMap<Integer, Integer[]> getItemsByStringArray(List<String> args, boolean invert){
 		HashMap<Integer, Integer[]> items = new HashMap<Integer, Integer[]>();
 		for(String arg : args){
 			List<String> strArgs = new ArrayList<String>();
 			if(invert) strArgs = Arrays.asList(arg.split(" "));
 			else strArgs = splitArgs(arg);
 			for(String strArg : strArgs){
 				String strAmount = "";
 				String[] strSplit = strArg.split(":");
 				String[] strItems = strSplit[invert?1:0].split(",");
 				Integer[] intAmount = null;
 				if(strSplit.length>1){
 					strAmount = strSplit[invert?0:1];
 					intAmount = checkInt(strAmount);
 				}
 				for(String str : strItems){
 					if(str.contains("-")){
 						String[] strItems2 = str.split("-");
 						int id1=getIntByString(strItems2[0]);
 						int id2=getIntByString(strItems2[1]);
 						if((id1!=-1)&&(id2!=-1)){
 							if(id1>id2){
 								int id3=id1;
 								id1=id2;
 								id2=id3;
 							}
 							while(id1<=id2){
 								Material mat = Material.getMaterial(id1);
 								if(mat!=null){
 									if(intAmount==null){
 										intAmount = new Integer[1];
 										if(strArg.contains("stack")){
 											intAmount[0] = mat.getMaxStackSize();
 										}
 										else intAmount[0] = 1;
 									}
 									items.put(mat.getId(),intAmount);
 								}
 								id1++;
 							}
 						}
 					}
 					else{
 						int id=getIntByString(str);
 						if(id!=-1){
 							Material mat = Material.getMaterial(id);
 							if(mat!=null){
 								if(intAmount==null){
 									intAmount = new Integer[1];
 									if(strArg.contains("stack")){
 										intAmount[0] = mat.getMaxStackSize();
 									}
 									else intAmount[0] = 1;
 								}
 								items.put(mat.getId(),intAmount);
 							}
 						}
 					}
 				}
 			}
 		}
 		return items;
 	}
 	
 	public Integer[] checkInt(String arg){
 		List<Integer> values = new ArrayList<Integer>();
 		
 		if(arg.contains("-")){
 			String[] split = arg.split("-");
 			int val1 = 0;
 			int val2 = 0;
 			if(split.length>0) val1 = getIntByString(split[0]);
 			if(split.length>1) val2 = getIntByString(split[1]);
 			if(val1>0) values.add(val1);
 			if(val2>0) values.add(val2);
 		}
 		else{
 			int val = getIntByString(arg);
 			if(val>=0) values.add(val);
 		}
 		if(values.size()==0) return null;
 		
 		return values.toArray(new Integer[values.size()]);
 	}
 	
 	public List<Integer[]> getDefaultAmount(List<Integer> items){
 		List<Integer[]> amount = new ArrayList<Integer[]>();
 		if(items==null) return null;
 		else{
 			for(Integer item : items){
 				Material mat = Material.getMaterial(item);
 				Integer[] intAmount = new Integer[1];
 				intAmount[0] = mat.getMaxStackSize();
 				amount.add(intAmount);
 			}
 		}
 		return amount;
 	}
 	
 	public List<Integer> getItemsFromFilter(FilterType type){
 		List<Material> materials = Arrays.asList(Material.values());
 		List<Integer> items = new ArrayList<Integer>();
 		switch(type){
 		case ALL:
 			for(Material mat : materials) items.add(mat.getId());
 			return items;
 		case BLOCK:
 			for(Material mat : materials) if(mat.isBlock()) items.add(mat.getId());
 			return items;
 		case ITEM:
 			for(Material mat : materials) if(mat!=Material.AIR) if(!mat.isBlock()) items.add(mat.getId());
 			return items;
 		case RAW:
 			return items;
 		case CRAFTED:
 			return items;
 		}
 		return items;
 	}
 	
 	public List<String> splitArgs(String listArg){
 		List<String> args = new ArrayList<String>();
 		//getServer().broadcastMessage("listArg:"+listArg);
 		if(listArg.contains(":")){
 			int pos = 0;
 			int posEnd = 0;
 			while(pos!=-1){
 				posEnd = listArg.indexOf(":",pos);
 				if(posEnd!=-1) posEnd = listArg.indexOf(",",posEnd);
 				if(posEnd!=-1){
 					//getServer().broadcastMessage("add:"+listArg.substring(pos,posEnd));
 					args.add(listArg.substring(pos,posEnd));
 					pos = posEnd+1;
 				}
 				else{
 					//getServer().broadcastMessage("add:"+listArg.substring(pos));
 					args.add(listArg.substring(pos));
 					pos = -1;
 				}
 			}
 			return args;
 		}
 		else return Arrays.asList(listArg);
 	}
 	
 	public void sendListById(String arg, RMPlayer rmp){
 		int id = getIntByString(arg, 0);
 		if(id<0) id=0;
 		sendListByInt(id, rmp);
 	}
 	public void sendListByInt(int id, RMPlayer rmp){
 		if(id<0) id=0;
 		List<RMGame> rmGames = RMGame.getGames();
 		if(rmGames.size()==0){
 			rmp.sendMessage("No games yet");
 			return;
 		}
 		if(id<0) id=0;
 		int i=id*10;
 		if(rmGames.size()>0) rmp.sendMessage("Page "+id+" of " +(int)(rmGames.size()/5));
 		HashMap<Integer, String> hashGames = new HashMap<Integer, String>();
 		while(i<rmGames.size()){
 			RMGame rmGame = rmGames.get(i);
 			hashGames.put(rmGame.getConfig().getId(), "Game: "+ChatColor.YELLOW+rmGame.getConfig().getId()+ChatColor.WHITE+" - "+"Owner: "+ChatColor.YELLOW+rmGame.getConfig().getOwnerName()+ChatColor.WHITE+" Teams: "+rmGame.getTextTeamPlayers());
 			if(i==id*10+10) break;
 			i++;
 		}
 		Integer[] gameIds = hashGames.keySet().toArray(new Integer[hashGames.size()]);
 		Arrays.sort(gameIds);
 		for(Integer gameId : gameIds){
 			rmp.sendMessage(hashGames.get(gameId));
 		}
 	}
 	
 	public RMGame getGameById(String arg){
 		int id = getIntByString(arg);
 		if(id!=-1) return RMGame.getGame(id);
 		return null;
 	}
 	public RMGame getGameById(int arg){
 		return RMGame.getGame(arg);
 	}
 	public RMTeam getTeamById(String arg, RMGame rmGame){
 		int id = getIntByString(arg);
 		if(id!=-1) return rmGame.getTeam(id);
 		return null;
 	}
 	
 	public DyeColor getDyeByString(String color){
 		for(DyeColor dyeColor : DyeColor.values()){
 			if(dyeColor.name().equalsIgnoreCase(color.toLowerCase())){
 				return dyeColor;
 			}
 		}
 		return null;
 	}
 	
 	public RMTeam getTeamByDye(String arg, RMGame rmGame){
 		DyeColor color = getDyeByString(arg);
 		if(color!=null) return rmGame.getTeam(color);
 		return null;
 	}
 	
 	public short getShortByString(String arg){
 		short data = 0;
 		try{
 			data = Byte.valueOf(arg);
 			return data;
 		} catch(Exception e){
 			return -1;
 		}
 	}
 	public byte getByteByString(String arg){
 		byte data = 0;
 		try{
 			data = Byte.valueOf(arg);
 			return data;
 		} catch(Exception e){
 			return -1;
 		}
 	}
 	public int getIntByString(String arg){
 		int i = 0;
 		try{
 			i = Integer.valueOf(arg);
 			return i;
 		} catch(Exception e){
 			return -1;
 		}
 	}
 	public int getBoolIntByString(String arg){
 		int i = 0;
 		try{
 			i = Integer.valueOf(arg);
 			return i;
 		} catch(Exception e){
 			try{
 				i = Boolean.valueOf(arg)?1:0;
 				return i;
 			}
 			catch(Exception ee){
 				return -1;
 			}
 		}
 	}
 	public int getIntByString(String arg, int def){
 		int i = 0;
 		try{
 			i = Integer.valueOf(arg);
 			return i;
 		} catch(Exception e){
 			return def;
 		}
 	}
 	
 	public GameState getStateByInt(int i){
 		switch(i){
 			case 0: return GameState.SETUP;
 			case 1: return GameState.COUNTDOWN;
 			case 2: return GameState.GAMEPLAY;
 			case 3: return GameState.GAMEOVER;
 			default: return GameState.SETUP;
 		}
 	}
 	
 	public InterfaceState getInterfaceByInt(int i){
 		switch(i){
 			case 0: return InterfaceState.MAIN;
 			case 1: return InterfaceState.FILTER_CLEAR;
 			default: return InterfaceState.MAIN;
 		}
 	}
 	
 	public ChatColor getChatColorByDye(DyeColor dye){
 		switch(dye){
 			case WHITE:
 				return ChatColor.WHITE;
 			case ORANGE:
 				return ChatColor.GOLD;
 			case MAGENTA:
 				return ChatColor.LIGHT_PURPLE;
 			case LIGHT_BLUE:
 				return ChatColor.BLUE;
 			case YELLOW:
 				return ChatColor.YELLOW;
 			case LIME:
 				return ChatColor.GREEN;
 			case PINK:
 				return ChatColor.RED;
 			case GRAY:
 				return ChatColor.DARK_GRAY;
 			case SILVER:
 				return ChatColor.GRAY;
 			case CYAN:
 				return ChatColor.DARK_AQUA;
 			case PURPLE:
 				return ChatColor.DARK_PURPLE;
 			case BLUE:
 				return ChatColor.DARK_BLUE;
 			case BROWN:
 				return ChatColor.GOLD;
 			case GREEN:
 				return ChatColor.DARK_GREEN;
 			case RED:
 				return ChatColor.DARK_RED;
 			case BLACK:
 				return ChatColor.BLACK;
 		}
 		return ChatColor.WHITE;
 	}
 	
 	public void rmInfo(RMPlayer rmp){
 		rmp.sendMessage("ResourceMadness Commands:");
 		rmp.sendMessage(ChatColor.GRAY+"Gray/green text is optional.");
 		rmp.sendMessage("/rm "+ChatColor.YELLOW+"add "+ChatColor.WHITE+"Create a new game.");
 		rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"remove "+ChatColor.WHITE+"Remove an existing game.");
 		rmp.sendMessage("/rm "+ChatColor.YELLOW+"list "+ChatColor.GRAY+"[page] "+ChatColor.WHITE+"List games.");
 		rmp.sendMessage("/rm "+ChatColor.AQUA+"[items(id/name)] "+ChatColor.WHITE+"Get the item's name or id");
 		rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"join "+ChatColor.AQUA+"[team(id/color)] "+ChatColor.WHITE+"Join a team.");
 		rmp.sendMessage("/rm "+ChatColor.YELLOW+"quit "+ChatColor.WHITE+"Quit a team.");
 		rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"start "+ChatColor.GREEN+"[amount] "+ChatColor.WHITE+"Start a game. Randomize with "+ChatColor.GREEN+"amount"+ChatColor.WHITE+".");
 		rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"restart/stop "+ChatColor.WHITE+"Restart/Stop a game.");
 		rmp.sendMessage("/rm "+ChatColor.YELLOW+"items "+ChatColor.WHITE+"Get which items you need to find.");
 		rmp.sendMessage("/rm "+ChatColor.YELLOW+"claim items/award "+ChatColor.WHITE+"Claim your items or award.");
 		rmp.sendMessage("/rm "+ChatColor.YELLOW+"set "+ChatColor.WHITE+"Set various game related settings.");
 		rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"filter "+ChatColor.WHITE+" Add items to filter");
 	}
 	
 	public void rmSetInfo(RMPlayer rmp){
 		rmp.sendMessage(ChatColor.GOLD+"/rm set");
 		rmp.sendMessage(ChatColor.AQUA+"maxplayers "+ChatColor.AQUA+"[amount] "+ChatColor.WHITE+"Set max players.");
 		rmp.sendMessage(ChatColor.AQUA+"maxteamplayers "+ChatColor.AQUA+"[amount] "+ChatColor.WHITE+"Set max team players.");
 		rmp.sendMessage(ChatColor.AQUA+"random "+ChatColor.AQUA+"[amount] "+ChatColor.WHITE+"Randomly pick "+ChatColor.GREEN+"amount "+ChatColor.WHITE+"of items every match.");
 		rmp.sendMessage(ChatColor.AQUA+"warp "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.warpToSafety+".");
 		rmp.sendMessage(ChatColor.AQUA+"restore "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.autoRestoreWorld+".");
 		rmp.sendMessage(ChatColor.AQUA+"warnhacked "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.warnHackedItems+".");
 		rmp.sendMessage(ChatColor.AQUA+"allowhacked "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.allowHackedItems+".");
 		rmp.sendMessage(ChatColor.AQUA+"keepingame "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.keepIngame+".");
 		rmp.sendMessage(ChatColor.AQUA+"clearinventory "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.clearPlayerInventory+".");
 		rmp.sendMessage(ChatColor.AQUA+"midgamejoin "+ChatColor.GREEN+"[true/false] "+ChatColor.WHITE+RMText.allowMidgameJoin+".");
 	}
 	
 	public void rmFilterInfo(RMPlayer rmp){
 		rmp.sendMessage(ChatColor.GOLD+"/rm filter");
 		/*
 		rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"filter "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item/clear"+ChatColor.BLUE+":[amount/stack]");
 		rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"filter "+ChatColor.YELLOW+"remove "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item/clear");
 		rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"filter "+ChatColor.YELLOW+"random "+ChatColor.GREEN+"[amount] "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item/clear"+ChatColor.BLUE+":[amount/stack]");
 		*/
 		rmp.sendMessage(ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item/clear"+ChatColor.BLUE+":[amount/stack]");
 		rmp.sendMessage(ChatColor.YELLOW+"remove "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item/clear");
 		rmp.sendMessage(ChatColor.YELLOW+"random "+ChatColor.GREEN+"[amount] "+ChatColor.AQUA+"[items(id)]"+ChatColor.YELLOW+"/all/block/item/clear"+ChatColor.BLUE+":[amount/stack]");
 		rmp.sendMessage(ChatColor.GOLD+"Examples:");
 		rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"filter "+ChatColor.YELLOW+"clear");
 		rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"filter "+ChatColor.AQUA+"1-5 6-9"+ChatColor.BLUE+":32 "+ChatColor.AQUA+"10-20,22,24"+ChatColor.BLUE+":stack "+ChatColor.AQUA+"27-35"+ChatColor.BLUE+":8-32");
 		rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"filter "+ChatColor.YELLOW+"remove "+ChatColor.AQUA+"1-10,20,288");
 		rmp.sendMessage("/rm "+ChatColor.GRAY+"[id] "+ChatColor.YELLOW+"filter "+ChatColor.YELLOW+"random "+ChatColor.GREEN+"20 "+ChatColor.AQUA+"all"+ChatColor.BLUE+":100-200");
 	}
 	
 	public String stripLast(String str, String s){
 		int pos = str.lastIndexOf(s);
 		if(pos!=-1){
 			String part1 = str.substring(0, pos);
 			String part2 = str.substring(pos+s.length());
 			return part1+part2;
 		}
 		return str;
 	}
 	
 	public String getFormattedStringByList(List<String> strList){
 		String line = "";
 		for(String str : strList){
 			line+=str+ChatColor.WHITE+", ";
 		}
 		line = stripLast(line, ",");
 		return line;
 	}
 	public String getFormattedStringByListMaterial(List<Material> materials){
 		String line = "";
 		for(Material mat : materials){
 			line+=mat.name()+", ";
 		}
 		line = stripLast(line, ",");
 		return line;
 	}
 	
 	public String getTextBlockList(List<List<Block>> blockList, boolean allowNull){
 		String line = "";
 		for(List<Block> bList : blockList){
 			for(Block b : bList){
 				if(b!=null){
 					line+=b.getType().name();
 				}
 				else if(allowNull) line+="null";
 				line+=",";
 			}
 		}
 		return stripLast(line, ",");
 	}
 	public String getTextList(List<Block> bList, boolean allowNull){
 		String line = "";
 		for(Block b : bList){
 			if(b!=null){
 				line+=b.getType().name();
 			}
 			else if(allowNull) line+="null";
 			line+=",";
 		}
 		return stripLast(line, ",");
 	}
 }
