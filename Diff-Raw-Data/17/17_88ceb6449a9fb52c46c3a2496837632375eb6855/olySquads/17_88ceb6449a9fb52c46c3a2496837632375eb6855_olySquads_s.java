 package com.olympuspvp.squads;
 
 import java.io.File;
 import java.util.*;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import com.olympuspvp.chat.olyChat;
 
 
 public class olySquads extends JavaPlugin{
 
 	FileConfiguration config = getConfig();
 	final File configFile = new File("plugins" + File.separator + "olySquads" + File.separator + "config.yml");
 	private HashMap<String, String> playerData = new HashMap<String, String>(); //Player Name, Squad Name
 	private HashMap<String, Squad> squads = new HashMap<String, Squad>();//Squad Name, Squad (Data)
 	//config.getConfigurationSection("Squads").getKeys(false).toArray();
 	LoginListener login = new LoginListener(this);
 	CommandManager command = new CommandManager(this);
 	
 	@Override
 	public void onEnable(){
 		config = this.getConfig();
 		try{
 			config = YamlConfiguration.loadConfiguration(configFile);
 		}catch(Exception  exe){
 			System.out.println("[olySquads] The config file could not be found. Creating one.");
 			config.set("Squads.Default.Owner", "CalDeFault");
 			List<String> members = new ArrayList<String>();
 			members.add("AeroEveryDefault");
 			members.add("CalDeFault");
 			config.set("Squads.Default.Home", "world;0;0;0;0;0"); //world;x;y;z;pitch;yaw;
 			config.set("Squads.Default.Rally", "world;0;0;0;0;0");
 			config.set("Players.AeroEveryDefault", "Default");
 			config.set("Players.CalDeFault", "Default");
 			saveConfig();
 			System.out.println("[olySquads] Default Configuration set at location " + configFile.getPath());
 		}Bukkit.getPluginManager().registerEvents(login, this);
 		reloadData();
 	}
 	
 	public void reloadData(){
 		for(Player p : Bukkit.getOnlinePlayers()){
 			loadData(p.getName());
 		}
 	}
 	
 	public void loadData(String name){
 		System.out.println("Loading data for " + name);
 		if(playerData.containsKey(name)) return;
 		String squad = config.getString("Players." + name);
 		playerData.put(name, squad);
 		if(squad == null) return;
 		if(squads.containsKey(squad)) return;
 		if(!loadSquad(squad)){
 			try{
 				Bukkit.getPlayerExact(name).sendMessage(ChatColor.DARK_RED + "Your squad could not be loaded.");
 			}catch(Exception e){}
 		}
 	}
 	
 	public void loadData(String name, String squad){
 		playerData.put(name, squad);
 	}
 
 	public boolean loadSquad(String name){
 		System.out.println("Loading squad " + name);
 		String owner = config.getString("Squads." + name + ".Owner");
 		if(owner == null) return false;
 		List<String> members = config.getStringList("Squads." + name + ".Members");
 		if(members == null) return false;
 		String homeString = config.getString("Squads." + name + ".Home");
 		Location home =  stringToLoc(homeString);
 		String rallyString = config.getString("Squads." + name + ".Rally");
 		Location rally = stringToLoc(rallyString);
 		Squad s = new Squad(name, owner, members, home, rally, this);
 		squads.put(name, s);
 		System.out.println("Squad loaded");
 		return true;
 	}
 	
 	public void loadSquad(String name, Squad s){
 		System.out.println("Loading squad " + name);
 		squads.put(name, s);
 		System.out.println("Squad loaded: " + squads.toString());
 	}
 	
 	/**
 	 * Returns a Location based on a parsed string. For reading from config.
 	 * @see locToString()
 	 * @param s The parsed string to convert
 	 * @return A location based on s
 	 */
 	public Location stringToLoc(String s){
 		if(s == null) return null;
 		String[] parse = s.split(";");
 		if(parse.length != 6) return null;
 		String worldName = parse[0];
 		int x, y, z;
 		float pitch, yaw;
 		World world;
 		try{
 			x = Integer.parseInt(parse[1]);
 			y = Integer.parseInt(parse[2]);
 			z = Integer.parseInt(parse[3]);
 			pitch = Float.parseFloat(parse[4]);
 			yaw = Float.parseFloat(parse[5]);
 			world = Bukkit.getWorld(worldName);
 			if(world == null) return null;
 		}catch(Exception e){
 			return null;
		}return new Location(world, x, y, z, pitch, yaw);
 	}
 	
 	/**
 	 * Returns a parsed string based on a location. For saving to config.
 	 * @see stringToLoc()
 	 * @param loc The location to convert
 	 * @return A string with the essential information of loc
 	 */
 	public String locToString(Location loc){
 		if(loc == null) return null;
 		int x = loc.getBlockX();
 		int y = loc.getBlockY();
 		int z = loc.getBlockZ();
 		float pitch = loc.getPitch();
 		float yaw = loc.getYaw();
 		String world = loc.getWorld().getName();
 		return world + ";" + x + ";" + y + ";" + z + ";" + pitch + ";" + yaw;
 	}
 	
 	public FileConfiguration getConfig(){
 		return config;
 	}
 
 	public Squad getPlayerSquad(String name){
 		if(!playerData.containsKey(name)) loadData(name);
 		String sName = playerData.get(name);
 		if(sName == null) return null;
 		Squad squad = squads.get(playerData.get(name)); //YOU, LINE, ARE A BASTARD
 		System.out.println(name + " returns " + squad.getName());
 		return squad; //YOU TOO
 	}
 	
 	public Squad getSquad(String name){
 		if(!squads.containsKey(name)) loadSquad(name);
 		return squads.get(name);
 	}
 	
 	public void setPlayerSquad(String name, String squad, boolean save){
 		System.out.println("Loading playerdata for " + name + " in " + squad + ". WillSave = " + save);
 		config.set("Players." + name, squad);
 		if(save) saveConfig();
 		playerData.put(name, squad);
 		System.out.println(playerData.toString());
 	}
 	
 	public void setPlayerSquad(String name, String squad){
 		setPlayerSquad(name, squad, true);
 	}
 	
 	public boolean onCommand(CommandSender s, Command cc, String c, String[] args){
 		if(c.equals("s") || c.equals("squad")){
 			command.squadCommand(s, args);
 		}return true;
 	}
 	
 	public olyChat getChat(){
 		return (olyChat) Bukkit.getPluginManager().getPlugin("olyChat");
 	}
 	
 }
