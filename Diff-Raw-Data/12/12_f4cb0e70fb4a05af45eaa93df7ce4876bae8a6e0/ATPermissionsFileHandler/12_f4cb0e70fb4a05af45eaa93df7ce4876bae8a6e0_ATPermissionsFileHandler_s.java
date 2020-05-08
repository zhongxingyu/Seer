 package com.andoutay.admintime;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.GameMode;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerGameModeChangeEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.server.PluginDisableEvent;
 import org.bukkit.scheduler.BukkitTask;
 
 import ru.tehkode.permissions.PermissionManager;
 
 import com.earth2me.essentials.Essentials;
 
 public class ATPermissionsFileHandler implements Listener
 {
 	private AdminTime plugin;
 	private File permFile;
 	private YamlConfiguration perms;
 	private Logger log = Logger.getLogger("Minecraft");
 	private final String adMode = "adminMode";
 	private final String regMode = "regMode";
 	private HashMap <Player, BukkitTask> timers;
 	private boolean disabled;
 	
 	ATPermissionsFileHandler(AdminTime plugin)
 	{
 		permFile = new File(plugin.getDataFolder(), "permissions.yml");
 		perms = YamlConfiguration.loadConfiguration(permFile);
 		timers = new HashMap<Player, BukkitTask>();
 		this.plugin = plugin;
 	}
 	
 	public void onEnable()
 	{
 		disabled = false;
 		List<World> worlds = plugin.getServer().getWorlds();
 		
 		for (World w : worlds)
 		{
 			ArrayList<String> list = new ArrayList<String>(), list2 = new ArrayList<String>();
 			perms.addDefault(adMode + "." + w.getName(), list);
 			perms.addDefault(regMode + "." + w.getName(), list2);
 		}
 		
 		ArrayList<String> allList = new ArrayList<String>(), allList2 = new ArrayList<String>();
 		perms.addDefault(adMode + ".allWorlds", allList);
 		perms.addDefault(regMode + ".allWorlds", allList2);
 		
 		perms.options().copyDefaults(true);
 		try {
 			perms.save(permFile);
 		} catch (IOException e) {
 			log.severe(AdminTime.logPref + "Could not save permissions file");
 		}
 	}
 	
 	public void enterAdminMode(Player p)
 	{
 		swapPermSets(adMode, regMode, p, p.getWorld().getName());
 		setGodAndFly(p, true);
 		
 		if (AdminTime.inAdminMode.containsKey(p) && !AdminTime.inAdminMode.get(p)) {
 			AdminTime.inAdminMode.put(p, true);
 		}
 	}
 	
 	public void exitAdminMode(Player p)
 	{
 		if (AdminTime.inAdminMode.containsKey(p) && AdminTime.inAdminMode.get(p) && AdminTime.lastLocs.containsKey(p)) {
 			p.teleport(AdminTime.lastLocs.get(p));
 			AdminTime.lastLocs.remove(p);
 		}
 		swapPermSets(regMode, adMode, p, p.getWorld().getName());
 		setGodAndFly(p, false);
 
 		if (AdminTime.inAdminMode.containsKey(p) && AdminTime.inAdminMode.get(p)) {
 			AdminTime.inAdminMode.put(p, false);
 			plugin.tellAll(p, "left", "", "");
 		}
 	}
 	
 	private void swapPermSets(String set1, String set2, Player p, String worldName)
 	{
 		if (!(worldName == null || worldName.equalsIgnoreCase("")))
 		{
 			setPermSet(set2, p, worldName, false);
 			setPermSet(set1, p, worldName, true);
 		}
 		
 		setPermSet(set2, p, "allWorlds", false);
 		setPermSet(set1, p, "allWorlds", true);
 	}
 	
 	private void setPermSet(String set, Player p, String worldName, boolean tf)
 	{
 		List<String> ps = perms.getStringList(set + "." + worldName);
 		setPerms(ps, p, tf);
 	}
 	
 	private void setPerms(List<String> permsToMod, Player p, boolean tf)
 	{
 		PermissionManager pm = null;
 		try {
 			pm = ATClassManager.getPexManager(plugin);
 		} catch (ClassNotFoundException e) {
 			pm = null;
 		} catch (NoClassDefFoundError e) {
 			pm = null;
 		}
 		for (String perm : permsToMod)
 		{
 			if (pm != null && ATConfig.usePex)
 			{
 				if (tf)
 					pm.getUser(p).addPermission(perm);
 				else
 					pm.getUser(p).removePermission(perm);
 			}
 			else
 				p.addAttachment(plugin, perm, tf);
 		}
 		
 		p.recalculatePermissions();				
 	}
 	
 	public void reload()
 	{
 		perms = YamlConfiguration.loadConfiguration(permFile);
 		for (Player p : AdminTime.inAdminMode.keySet())
 		{
 			if (AdminTime.inAdminMode.get(p))
 				enterAdminMode(p);
 			else
 				exitAdminMode(p);
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent evt)
 	{
 		Player p = evt.getPlayer();
 		exitAdminMode(p);
 		if (p.hasPermission("admintime.loginlist"))
 			if (plugin.inAdminSize() > 0)
 				plugin.showListToSender((CommandSender)p, 1);
 	}
 	
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent evt)
 	{
 		Player p = evt.getPlayer();
 		if (AdminTime.inAdminMode.containsKey(p) && AdminTime.inAdminMode.get(p))
 		{
 			exitAdminMode(p);
 		} else {
 			AdminTime.lastLocs.remove(p);			//just in case other methods haven't caught this already
 		}
 		AdminTime.inAdminMode.remove(p);
 	}
 
 	@EventHandler(priority = EventPriority.LOW)
 	public void onPlayerChangeWorld(PlayerChangedWorldEvent evt)
 	{
		Player p = evt.getPlayer();
 		if (AdminTime.inAdminMode.containsKey(p) && AdminTime.inAdminMode.get(p))
 		{
 			setPermSet(adMode, p, evt.getFrom().getName(), false);
 			setPermSet(adMode, p, p.getWorld().getName(), true);
 		}
 		else
 		{
 			setPermSet(regMode, p, evt.getFrom().getName(), false);
 			setPermSet(regMode, p, p.getWorld().getName(), true);
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerChangeGameMode(PlayerGameModeChangeEvent evt)
 	{
 		final Player p = evt.getPlayer();
 		//freaking plugins using EventPriority.MONITOR...
		if (AdminTime.inAdminMode.containsKey(p) && AdminTime.inAdminMode.get(p)) timers.put(p, plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() { public void run() {reinforceGodAndFly(p); }}, ATConfig.flyDelay, 20));
 	}
 	
 	@EventHandler
 	private void onDisable(PluginDisableEvent evt)
 	{
 		if (!disabled)
 		{
 			for (Player p: AdminTime.inAdminMode.keySet())
 				exitAdminMode(p);
 			disabled = true;
 		}
 	}
 	
 	private void reinforceGodAndFly(Player p)
 	{
		if (p.getAllowFlight())
 		{
 			BukkitTask t = timers.get(p);
 			try
 			{
 				t.cancel();
 				
 				//1 Line for debug
 				if (ATConfig.dispDebug) log.info(AdminTime.logPref + "Canceled timer for " + p.getDisplayName());
 			}
 			catch (NullPointerException e)
 			{
 				
 			}
 			timers.remove(p);
 		}
 		else
 		{
 			//1 line for debug
 			if (ATConfig.dispDebug) log.info(AdminTime.logPref + "Set fly to true for " + p.getDisplayName());
 			setGodAndFly(p, true);
 		}
 	}
 
 	private void setGodAndFly(Player p, boolean tf)
 	{
 		if (ATConfig.useGod || ATConfig.useFly)
 		{
 			if (ATConfig.useFly)
 			{
 				if (p.getGameMode() == GameMode.CREATIVE)
 					p.setAllowFlight(true);
 				else
 					p.setAllowFlight(tf);
 				if (!p.getAllowFlight()) p.setFlying(false);
 			}
 			Essentials ess = null;
 			ess = ATClassManager.getEssentials(plugin);
 			if (ess == null) return;
 			if (ATConfig.useGod) ess.getUser(p).setGodModeEnabled(tf);
 		}
 	}
 }
