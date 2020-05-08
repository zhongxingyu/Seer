 package Lihad.BeyondVotes;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BeyondVotes extends JavaPlugin implements Listener {
 	public static FileConfiguration config;
 	protected static String PLUGIN_NAME = "BeyondVotes";
 	protected static String header = "[" + PLUGIN_NAME + "] ";
 	private static Logger log = Logger.getLogger("Minecraft");
 	public static List<Player> PLAYERS = new LinkedList<Player>();
 
 	public static Map<String,String> selection_enabled = new HashMap<String,String>();
 	public static List<String> selection_deletion = new LinkedList<String>();
 	public static List<String> override = new LinkedList<String>();
 	public static boolean active = false;
 	public static int lineindex = 0;
 	private int runrotation = 0;
 	public static File whitelist = new File("ftb-white-list.txt");
 
 	SimpleDateFormat parserSDF=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZZZ");
 	
 	public static File file = new File("plugins/Votifier/votes.log");
 
 	public static List<VotingSite> voting_site_list = new LinkedList<VotingSite>();
 	public static List<String> blocked_worlds = new LinkedList<String>();
 	public static long interval;
 	public static long gather_vote_interval;
 	public static int task;
 
 
 	public class VotingSite{
 		String name;
 		String web_address;
 		String votifier_name;
 		String[] spam_message;
 		boolean rewards_enabled;
 		boolean simple_date;
 		List<ItemStack> rewards;
 		List<Location> locations;
 
 		Map<String,Long> natural_voting_map;
 		Map<String,Date> simple_date_voting_map;
 		
 		void putVotingMap(String name, Object obj){
 			if(simple_date)simple_date_voting_map.put(name,(Date) obj);
 			else natural_voting_map.put(name,(Long) obj);
 		}
 		Object getVotingMap(String string){
 			if(simple_date)return simple_date_voting_map.get(string);
 			else return natural_voting_map.get(string);
 		}
 		boolean containsKeyVotingMap(String string){
 			if(simple_date && simple_date_voting_map.containsKey(string)) return true;
 			else if(natural_voting_map.containsKey(string)) return true;
 			else return false;
 		}
 		
 		VotingSite(String n, String w, String v, String[] s, boolean b, boolean bs, List<ItemStack> i, List<Location> l){
 			name = n; web_address = w; votifier_name = v;spam_message = s; rewards_enabled = b; simple_date =bs; rewards = i; locations = l;natural_voting_map = new HashMap<String,Long>();simple_date_voting_map = new HashMap<String,Date>();}
 	}
 	@Override
 	public void onDisable() {
 	}
 
 	@Override
 	public void onEnable(){
 		load();
 		
 		//Timer related to building the maps off the votifier log
 		task = this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable(){
 			public void run() {
 				if(active)info("Running vote gatherer off line "+lineindex);
 				Calendar cal = Calendar.getInstance();
 				String line;
 				cal.setTime(new Date(System.currentTimeMillis()));
 				try {
 					BufferedReader rd = new BufferedReader(new FileReader(file));
 					int linenumber = 0;
 					while ((line = rd.readLine()) != null) {
 						if(lineindex > linenumber){
 							linenumber++;
 							continue;
 						}else{
 							for(int i = 0;i<voting_site_list.size();i++){
 								if(line.contains(voting_site_list.get(i).votifier_name)){
 									if(voting_site_list.get(i).simple_date){
 										voting_site_list.get(i).putVotingMap((line.substring(line.indexOf("username:")+9, line.indexOf("address:")-1)).toLowerCase(),
 												parserSDF.parse(line.substring(line.indexOf("timeStamp:")+10,line.indexOf("timeStamp:")+35)));
 									}else{
 										voting_site_list.get(i).putVotingMap((line.substring(line.indexOf("username:")+9, line.indexOf("address:")-1)).toLowerCase(),
 												Long.parseLong(line.substring(line.indexOf("timeStamp:")+10,line.indexOf("timeStamp:")+20)));
 										info("Adding in: "+(line.substring(line.indexOf("username:")+9, line.indexOf("address:")-1)).toLowerCase()+" "+Long.parseLong(line.substring(line.indexOf("timeStamp:")+10,line.indexOf("timeStamp:")+20)));
 
 									}
 								}
 							}
 						}
 						linenumber++;
 					}
 					lineindex = linenumber;
 					rd.close();
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (ParseException e) {
 					e.printStackTrace();
 				}
 			
 				int runrotationmax = (int)((interval*voting_site_list.size())/gather_vote_interval);
 				int waittime = runrotationmax/voting_site_list.size();
 				double inverse = 1.0/waittime;
 				if(inverse*(double)runrotation == Math.floor(inverse*(double)runrotation))siteSpammer(voting_site_list.get((int)((inverse*runrotation))));
 				if(active)info("runrotationmax: "+runrotationmax);
 				if(active)info("runrotation: "+runrotation);
 				if(active)info("waittime: "+waittime);
 				if(active)info("inverse: "+inverse);
 				if(active)info("Math.floor(inverse*(double)runrotation): "+Math.floor(inverse*(double)runrotation));
 				if(active)info("inverse*(double)runrotation: "+inverse*(double)runrotation);
 
 				if(active)info("true/false: "+(inverse*runrotation == Math.floor(inverse*runrotation)));
 
 				
 				runrotation++;
 				if(runrotation >= runrotationmax) runrotation = 0;
 
 				if(active)info("Ending vote gatherer");
 			}
 		}, 0, gather_vote_interval);
 		
 		this.getServer().getPluginManager().registerEvents(this, this);
 	}
 
 	@EventHandler
 	public void onPlayerTeleport(PlayerTeleportEvent event){
 		Location location = eventHelperForEjection(event.getPlayer(), event.getTo(), event.getFrom());
 		if(location != null)event.setTo(location);
 	}
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event){
 		Location location = eventHelperForEjection(event.getPlayer(), event.getBlock().getLocation(), event.getBlock().getWorld().getSpawnLocation());
 		if(location != null)event.getPlayer().teleport(location);
 	}
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event){
 		if(event.getClickedBlock() != null && selection_enabled.containsKey(event.getPlayer().getName())){
 			for(int i = 0; i<voting_site_list.size();i++){
 				if(voting_site_list.get(i).locations.contains(event.getClickedBlock().getLocation())){
 					event.getPlayer().sendMessage("position already set");
 					return;
 				}
 			}
 			for(int i = 0; i<voting_site_list.size();i++){
 				if(voting_site_list.get(i).name.equalsIgnoreCase(selection_enabled.get(event.getPlayer().getName()))){
 					voting_site_list.get(i).locations.add(event.getClickedBlock().getLocation());
 					event.getPlayer().sendMessage("location set");
 					selection_enabled.remove(event.getPlayer().getName());
 					saveLocations();
 					return;
 				}
 			}
 
 		}else if(event.getClickedBlock() != null && selection_deletion.contains(event.getPlayer().getName())){
 			for(int i = 0; i<voting_site_list.size();i++){
 				if(voting_site_list.get(i).locations.contains(event.getClickedBlock().getLocation())){
 					voting_site_list.get(i).locations.add(event.getClickedBlock().getLocation());
 					event.getPlayer().sendMessage(voting_site_list.get(i).name+" location removed");
 					selection_deletion.remove(event.getPlayer().getName());	
 					saveLocations();
 					return;
 				}
 			}
 			event.getPlayer().sendMessage("no position set here");
 
 		}else if(event.getClickedBlock() != null){
 			for(int i = 0; i<voting_site_list.size();i++){
 				if(voting_site_list.get(i).locations.contains(event.getClickedBlock().getLocation())){
 					event.getPlayer().sendMessage(ChatColor.GRAY.toString()+"To receive your prize, just click the link below and vote!");
 					event.getPlayer().sendMessage(ChatColor.GRAY.toString()+"Click ->"+ChatColor.RED.toString()+ChatColor.UNDERLINE.toString()+" "+voting_site_list.get(i).web_address+ChatColor.RESET.toString()+ChatColor.GRAY.toString());
 				}
 			}
 		}
 	}
 	@SuppressWarnings("deprecation")
 	private void siteSpammer(VotingSite site){
 		//Method related spamming players
 		if(active)info("Running "+site.name+" Vote Spammer....");
 		Player[] players = getPlayers();		
 		Calendar calendar = Calendar.getInstance();
 		for(int i = 0;i<players.length;i++){
 			Player player = players[i];
 	        String sIp = (player).getName().toLowerCase();
 	        if(!site.containsKeyVotingMap(sIp)){
 	        	if(active)info("Player: "+player.getName()+" with Name ["+sIp+"] was not found.  Spammed");
 	        	messageSpamPlayer(player, site);
 	        }else if(site.simple_date && site.containsKeyVotingMap(sIp) && (calendar.get(Calendar.DAY_OF_MONTH) != ((Date)site.getVotingMap(sIp)).getDate())){
 	        	if(active)info("Player: "+player.getName()+" with Name ["+sIp+"] was found but hasn't voted today");
 	        	messageSpamPlayer(player, site);
 	        }else if(site.containsKeyVotingMap(sIp) && (System.currentTimeMillis()-(((Long)site.getVotingMap(sIp))*1000) > 86400000)){
 	        	if(active)info("Player: "+player.getName()+" with Name ["+sIp+"] was found but hasn't voted in the last 24 hours");
 	        	messageSpamPlayer(player, site);
 	        }else{
 	        	if(active)warning("Player: "+player.getName()+" has fallen outside all possibilities");
 	        }
 		}
 		if(active)info("Ending "+site.name+" Vote Spammer....");
 	}
 	
 	private void messageSpamPlayer(Player player, VotingSite site){
 		player.sendMessage(site.spam_message);
 	}
 	
 	private Player[] getPlayers(){
 		return getServer().getOnlinePlayers();
 	}
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if(cmd.getName().equalsIgnoreCase("bv") && args.length == 0 && sender instanceof ConsoleCommandSender) {
 			if(!active){
 				active = true;
 				info("Debug Active");
 			}else{
 				active = false;
 				info("Debug Off");
 			}
 			return true;	
 		}		
 		else if(cmd.getName().equalsIgnoreCase("bvreload") && sender.isOp()){
 			reload();
 			sender.sendMessage("bv Reloaded");
 			return true;	
 		}else if(cmd.getName().equalsIgnoreCase("bvset") && sender instanceof Player && ((Player)sender).isOp()){
 			if(selection_enabled.containsKey(((Player)sender).getName())){
 				selection_enabled.remove(((Player)sender).getName());
 				((Player)sender).sendMessage("Enable Tool Deselected");
 			}else{
 				selection_enabled.put(((Player)sender).getName(),args[0]);
 				selection_deletion.remove(((Player)sender).getName());
 				((Player)sender).sendMessage("Enable Tool Selected.  Please Click a Block");
 			}
 			return true;
 		}else if(cmd.getName().equalsIgnoreCase("bvdelete") && sender instanceof Player && ((Player)sender).isOp()){
 			if(selection_deletion.contains(((Player)sender).getName())){
 				selection_deletion.remove(((Player)sender).getName());
 				((Player)sender).sendMessage("Deletion Tool Deselected");
 			}else{
 				selection_deletion.add(((Player)sender).getName());
 				selection_enabled.remove(((Player)sender).getName());
 				((Player)sender).sendMessage("Deletion Tool Selected.  Please Click a Valid Block");
 			}
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("bvoverride") && (sender instanceof ConsoleCommandSender || ((Player)sender).isOp())){
 			if(args.length == 1){
 				if(this.getServer().getPlayer(args[0]) != null){
 					Player player = this.getServer().getPlayer(args[0]);
 					override.add(player.getName());
 				}else{
 					sender.sendMessage("Invalid Playername");
 				}
 			}else{
 				sender.sendMessage("Invalid Arguments");
 			}
 			return true;
 			//TODO:  Remove the spoofer once ftb is fixed/.
 		}else if(cmd.getName().equalsIgnoreCase("ivoted")){
 			for(int i = 0; i<voting_site_list.size();i++){
 				if(voting_site_list.get(i).rewards_enabled){
 					if(voting_site_list.get(i).containsKeyVotingMap(((Player)sender).getName().toLowerCase()) 
 							&& (System.currentTimeMillis()-(((Long)voting_site_list.get(i).getVotingMap(((Player)sender).getName().toLowerCase()))*1000) < 86400000)){
 						sender.sendMessage("You already voted for "+voting_site_list.get(i).votifier_name+" and received rewards today!");
						return true;
 					}
 					int rnd = new Random().nextInt(3);
 					if(rnd == 0){
 						try {
 							Long currentTime = (System.currentTimeMillis()/1000);
 							String outputline = ("Vote (from:"+voting_site_list.get(i).votifier_name+" username:"+((Player)sender).getName()+" address:"+((Player)sender).getAddress().getAddress().getHostAddress()+" timeStamp:"+currentTime);
 							BufferedWriter output = new BufferedWriter(new FileWriter(file, true));
 							output.newLine();
 							output.write(outputline);
 							output.close();
 							
 							voting_site_list.get(i).putVotingMap(sender.getName(), (System.currentTimeMillis()/1000));
 							sender.sendMessage("Congrats!!!  Here are your rewards!!");
 							for(int g = 0; g<voting_site_list.get(i).rewards.size();g++){
 								((Player)sender).getWorld().dropItemNaturally(((Player)sender).getLocation(), voting_site_list.get(i).rewards.get(g));
 							}
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 						
 					}
 					else{
 						sender.sendMessage("It doesnt look like you have voted yet on "+voting_site_list.get(i).votifier_name);
 					}
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	private void saveLocations(){
 		for(int i = 0; i<voting_site_list.size();i++){
 			List<String> strings = new LinkedList<String>();
 			for(int j = 0;j<voting_site_list.get(i).locations.size();j++){
 				strings.add(toString(voting_site_list.get(i).locations.get(j)));
 			}
 			config.set("sites."+voting_site_list.get(i).name+".locations", strings);
 		}
 		this.saveConfig();
 	}
 	private void reload(){
 		this.reloadConfig();
 		voting_site_list.clear();
 		lineindex = 0;
 		load();
 	}
 	private void load(){
 		config = getConfig();
 		List<String> enabled_site = config.getStringList("enabled");
 
 		for(int i=0;i<config.getConfigurationSection("sites").getKeys(false).size();i++){
 			ConfigurationSection section = config.getConfigurationSection("sites."+config.getConfigurationSection("sites").getKeys(false).toArray()[i]);
 
 			String name = config.getConfigurationSection("sites").getKeys(false).toArray()[i].toString();
 			if(enabled_site.contains(name)){
 				String web_address = section.getString("voting-webaddress");
 				String votifier_name = section.getString("votifier-log-name");
 				String[] spam_message = SpamDecorator.decor(section.getString("spam-message"));
 				boolean rewards_enabled = section.getBoolean("use-rewards");
 				boolean simple_date_enabled = section.getBoolean("use-simple-date");
 				List<ItemStack> rewards = new LinkedList<ItemStack>();
 				for(int j=0;j<section.getStringList("rewards").size();j++){
 					rewards.add(toItemStack(section.getStringList("rewards").get(j)));
 				}
 				List<Location> locations = new LinkedList<Location>();
 				for(int j=0;j<section.getStringList("locations").size();j++){
 					locations.add(toLocation(section.getStringList("locations").get(j)));
 				}
 				voting_site_list.add(new VotingSite(name, web_address, votifier_name, spam_message, rewards_enabled, simple_date_enabled, rewards, locations));
 				info("added "+name+" to list of avalible hits");
 			}else{
 				warning(name+" is not enabled.  skipping.");
 			}
 		}
 
 		blocked_worlds = config.getStringList("block-worlds");
 		interval = config.getLong("interval");
 		gather_vote_interval = config.getLong("gather-vote-interval");
 	}
 	@SuppressWarnings("deprecation")
 	private Location eventHelperForEjection(Player player, Location to, Location from){
 		if(blocked_worlds.contains(to.getWorld().getName())){
 			Calendar calendar = Calendar.getInstance();
 			if(player.isOp())return null;
 			else{
 				for(int i = 0; i<voting_site_list.size();i++ ){
 					if(override != null && !override.contains(player.getName())){
 						return ejectPlayer(player,voting_site_list.get(i),to,from);
 					}else if(voting_site_list.get(i).simple_date && (!(voting_site_list.get(i).containsKeyVotingMap((player).getName().toLowerCase()) 
 							&& (calendar.get(Calendar.DAY_OF_MONTH) == ((Date)voting_site_list.get(i).getVotingMap((player).getName().toLowerCase())).getDate())))){
 						return ejectPlayer(player,voting_site_list.get(i),to,from);
 					}
 					else if(!(voting_site_list.get(i).containsKeyVotingMap((player).getName().toLowerCase()) 
 							&& (System.currentTimeMillis()-(((Long)voting_site_list.get(i).getVotingMap(player.getName().toLowerCase()))*1000) < 86400000))){
 						return ejectPlayer(player,voting_site_list.get(i),to,from);
 					}
 				}
 			}		
 		}
 		return null;
 	}
 	private Location ejectPlayer(Player player, VotingSite site, Location to, Location from){
 		info("Ejecting player "+player.getName()+" from "+to.getWorld().getName());
 		messageSpamPlayer(player, site);
 		return from;
 	}
 	private static Location toLocation(String string){
 		String[] array;
 		if(string == null) return null;
 		array = string.split(",");
 		Location location = new Location(org.bukkit.Bukkit.getServer().getWorld(array[3]), Integer.parseInt(array[0]), Integer.parseInt(array[1]), Integer.parseInt(array[2]));
 		return location;
 	}
 	private static String toString(Location location){
 		if(location == null) return null;
 		return (location.getBlockX()+","+location.getBlockY()+","+location.getBlockZ()+","+location.getWorld().getName());
 	}
 	private ItemStack toItemStack(String string){
 		String[] array;
 		if(string == null) return null;
 		array = string.split(",");
 		return new ItemStack(Integer.parseInt(array[0]),Integer.parseInt(array[2]),Short.parseShort((array[1])));
 	}
 	private static void info(String message){ 
 		log.info(header + ChatColor.WHITE + message);
 	}
 	private static void severe(String message){
 		log.severe(header + ChatColor.RED + message);
 	}
 	private static void warning(String message){
 		log.warning(header + ChatColor.YELLOW + message);
 	}
 	private static void log(java.util.logging.Level level, String message){
 		log.log(level, header + message);
 	}
 }
