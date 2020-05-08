 package org.mcsg.double0negative.tabapi;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.MemoryConfiguration;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.comphenix.protocol.Packets;
 import com.comphenix.protocol.ProtocolLibrary;
 import com.comphenix.protocol.ProtocolManager;
 import com.comphenix.protocol.events.ConnectionSide;
 import com.comphenix.protocol.events.ListenerPriority;
 import com.comphenix.protocol.events.PacketAdapter;
 import com.comphenix.protocol.events.PacketContainer;
 import com.comphenix.protocol.events.PacketEvent;
 
 
 /**
  * TabAPI
  * 
  * Provides a simple interface for adding custom text to 
  * display on the minecraft tab menu on a player/plugin 
  * basis
  * 
  * @author Double0negative
  * 
  *
  */
 
 
 public class TabAPI extends JavaPlugin implements Listener, CommandExecutor{
 
 
 	private static HashMap<String, TabObject>playerTab = new HashMap<String, TabObject>();
 	private static HashMap<String, TabHolder>playerTabLast = new HashMap<String, TabHolder>();
 	
 	private static HashMap<Player, ArrayList<PacketContainer>>cachedPackets = new HashMap<Player, ArrayList<PacketContainer>>();
 	private static HashMap<Player, Integer>updateSchedules = new HashMap<Player, Integer>();
 
 	private static int horzTabSize = 3;
 	private static int vertTabSize = 20;
 	private static String[] colors = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "a","b", "c", "d", "f"};
 
 	private static int e = 0;
 	private static int r = 0;
 	
 	private static long flickerPrevention = 5L;
 
 	private static ProtocolManager protocolManager;
 
 	private static boolean shuttingdown = false;
 	
 	private static TabAPI plugin;
 	
 	public void onEnable(){
 		TabAPI.plugin = this;
 		
 		// read initial config if there's any
 		FileConfiguration config = getConfig();
 		config.options().copyDefaults(true);
 		
 		// add defaults if there are new ones
 		MemoryConfiguration defaultConfig = new MemoryConfiguration();
 		defaultConfig.set("flickerPrevention", flickerPrevention);
 		config.setDefaults(defaultConfig);
 		saveConfig();
 		
 		// load config settings
 		reloadConfiguration();
 		
 		this.getCommand("tabapi").setExecutor(this);
 		
 		try {
 			new Metrics(this).start();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		protocolManager = ProtocolLibrary.getProtocolManager();
 
 		
 		Bukkit.getServer().getPluginManager().registerEvents(this, this);
 		for(Player p: Bukkit.getOnlinePlayers()){
 			Plugin plugin = Bukkit.getPluginManager().getPlugin("TabAPI");
 			setPriority(plugin, p, 2);
 			resetTabList(p);
 			setPriority(plugin, p, -2);
 			
 		}
 
 
 		protocolManager.addPacketListener(new PacketAdapter(this,
 				ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL, Packets.Server.PLAYER_INFO) {
 			@Override
 			public void onPacketSending(PacketEvent event) {
 				switch (event.getPacketID()) {
 				case Packets.Server.PLAYER_INFO:
 					PacketContainer p = event.getPacket();
 					String s = p.getStrings().read(0);
 					if(s.startsWith("$")){  // this is a packet sent by TabAPI **Work around until I figure out how to make my own
 						p.getStrings().write(0,s.substring(1));  // packets bypass this block**
 						event.setPacket(p);
 					}
 					else{
 						event.setCancelled(true);
 					}
 					break;
 				}
 			}
 		});
 
 	}
 	
 	public void reloadConfiguration() {
 		reloadConfig();
 		flickerPrevention = getConfig().getLong("flickerPrevention");
 	}
 
 	public void onDisable(){
 		shuttingdown = true;
 		for(Player p: Bukkit.getOnlinePlayers()){
 			clearTab(p);
 		}
 		flushPackets();
 		playerTab = null;
 		playerTabLast = null;
 	}
 	
     @Override
     public boolean onCommand(CommandSender sender, Command cmd1, String commandLabel, String[] args){
         PluginDescriptionFile pdfFile = this.getDescription();
 
         Player player = null;
         if (sender instanceof Player) {
             player = (Player) sender;
             if(args.length == 1 && player.hasPermission("tabapi.reload")){
             	reloadConfiguration();
             	updateAll();
             }
             else{
                 player.sendMessage(ChatColor.DARK_RED +""+ ChatColor.BOLD +"TabAPI - Double0negative"+ChatColor.RESET+  ChatColor.RED +" Version: "+ pdfFile.getVersion() );
 
             }
         }
         else{
             sender.sendMessage(ChatColor.DARK_RED +""+ ChatColor.BOLD +"TabAPI - Double0negative"+ChatColor.RESET+  ChatColor.RED +" Version: "+ pdfFile.getVersion() );
             return true;
         }
         
         return true;
         
     }
 
 	private static void addPacket(Player p, String msg, boolean b, int ping){
 		PacketContainer message = protocolManager.createPacket(Packets.Server.PLAYER_INFO);
 		message.getStrings().write(0, ((!shuttingdown)?"$":"")+msg);
 		message.getBooleans().write(0, b);
 		message.getIntegers().write(0, ping);
 		ArrayList<PacketContainer> packetList = cachedPackets.get(p);
 		if (packetList == null) {
 			packetList = new ArrayList<PacketContainer>();
 			cachedPackets.put(p, packetList);
 		}
 		packetList.add(message);
 	}
 
 	private static void flushPackets() {
		final Player[] packetPlayers = cachedPackets.keySet().toArray(new Player[0]);
		for (Player p : packetPlayers) {
 			flushPackets(p, null);
 		}
 	}
 
 	private static void flushPackets(final Player p, final TabHolder tabCopy) {
 		final PacketContainer[] packets = (PacketContainer[]) cachedPackets.get(p).toArray(new PacketContainer[0]);
 		
 		// cancel old task (prevents flickering)
 		Integer taskID = updateSchedules.get(p);
 		if (taskID != null) {
 			Bukkit.getScheduler().cancelTask(taskID);
 		}
 		
 		taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(TabAPI.plugin, new Runnable() {
 			@Override
 			public void run() {
 				if (p.isOnline()) {
 					
 					for (PacketContainer packet : packets) {
 						try {
 							protocolManager.sendServerPacket(p, packet);
 						} catch (InvocationTargetException e) {
 							e.printStackTrace();
 							System.out.println("[TabAPI] Error sending packet to client");
 						}
 					}
 				}
 				if (tabCopy != null) playerTabLast.put(p.getName(), tabCopy); // we set this only if we really finally flush it (which is just now)
 				updateSchedules.remove(p); // we're done, no need to cancel this one on next run
 			}
 		}, flickerPrevention);
 		
 		// let's keep a reference to be able to cancel this (see above)
 		updateSchedules.put(p, taskID);
 		
 		cachedPackets.remove(p);
 	}
 	
 	
 	private static TabObject getTab(Player p){
 		TabObject tabo = playerTab.get(p.getName());
 		if(tabo == null){
 			tabo = new TabObject();
 			playerTab.put(p.getName(), tabo);
 		}
 		return tabo;
 	}
 	
 
 	/**
 	 * Priorities
 	 * 
 	 * -2 = no longer active, remove
 	 * -1 = background, only show if nothing else is there
 	 *  0 = normal
 	 *  1 = high priority
 	 *  2 = always show, only use if MUST show
 	 * 
 	 */
 	public static void setPriority(Plugin plugin, Player player, int pri){
 		getTab(player).setPriority(plugin, pri);
 	}
 
 	/**
 	 * Returns the tab list to the vanilla tab list for a player. 
 	 * If another plugin holds higher priority, this does notning
 	 * @param p
 	 */
 	public static void disableTabForPlayer(Player p){
 		playerTab.put(p.getName(), null);
 		resetTabList(p);
 	}
 
 	/**
 	 * Resets tab to normal tab. 
 	 * @param p
 	 */
 	public static void resetTabList(Player p){
 		int a = 0; int b = 0;
 
 		for(Player pl : Bukkit.getOnlinePlayers()){
 			setTabString(Bukkit.getPluginManager().getPlugin("TabAPI"), p, a, b, pl.getPlayerListName());
 			b++;
 			if(b > horzTabSize){
 				b = 0;
 				a++;
 			}
 		}
 	}
 
 	public static void setTabString(Plugin plugin, Player p, int x, int y, String msg){
 	setTabString(plugin, p, x, y, msg, 0);
 		
 	}
 	
 	/**
 	 * Set the tab for a player. 
 	 * 
 	 * If the plugin the tab is being set from does not have a priority, It will automatically be give a base
 	 * priority of 0
 	 * 
 	 * @param plugin
 	 * @param p
 	 * @param x
 	 * @param y
 	 * @param msg
 	 * @param ping
 	 */
 	public static void setTabString(Plugin plugin, Player p, int x, int y, String msg, int ping){
 		try{
 			TabObject tabo = getTab(p);
 			tabo.setTab(plugin, x, y,msg,ping);
 			playerTab.put(p.getName(), tabo);
 		}catch(Exception e){e.printStackTrace();}
 
 	}
 
 
 	/**
 	 * Updates a players tab
 	 * 
 	 * A tab will be updated with the tab from the highest priority plugin
 	 * 
 	 * @param p
 	 */
 	public static void updatePlayer(Player p){
 		if(!p.isOnline()) return;
 		r = 0; e = 0;
 		TabObject tabo = playerTab.get(p.getName());
 		TabHolder tab = tabo.getTab();
 		if(tab == null) return;
 		
 		/* need to clear the tab first */
 		clearTab(p);
 		
 		for(int b = 0; b < tab.maxv; b++){
 			for(int a = 0; a < tab.maxh ; a++){	
 				// fix empty tabs
 				if(tab.tabs[a][b] == null) tab.tabs[a][b] = nextNull();
 
 				String msg = tab.tabs[a][b];
 				int ping = tab.tabPings[a][b];
 				
 				addPacket(p, (msg == null)? " ": msg.substring(0, Math.min(msg.length(), 16)), true, ping);
 			}
 		}
 		flushPackets(p, tab.getCopy());
 
 	}
 
 	/**
 	 * Clear a players tab menu
 	 * @param p
 	 */
 	public static void clearTab(Player p){
 		if(!p.isOnline())return;
 
 		TabHolder tabold = playerTabLast.get(p.getName());
 
 		if(tabold != null){
 			for(String [] s: tabold.tabs){
 				for(String msg:s){
 					if(msg != null){
 						addPacket(p, msg.substring(0, Math.min(msg.length(), 16)), false, 0);
 					}
 				}
 			}
 		}
 	}
 
 	public static void updateAll(){
 		for(Player p:Bukkit.getOnlinePlayers()){
 			updatePlayer(p);
 		
 		}
 	}
 
 
 	/* return the next null filler */
 	public static String nextNull(){
 		String s = "";
 		for(int a = 0; a < r; a++){
 			s = " "+s;
 		}
 		s = s + "\u00A7" + colors[e];
 		e++; 
 		if(e > 14){
 			e = 0;
 			r++;
 		}
 		return s;
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void PlayerLeave(PlayerQuitEvent e){
 		//cleanup
 		playerTab.remove(e.getPlayer().getName());
 		playerTabLast.remove(e.getPlayer().getName());
 	}
 
 	public static int getVertSize(){
 		return vertTabSize;
 	}
 	
 	public static int getHorizSize(){
 		return horzTabSize;
 	}
 }
