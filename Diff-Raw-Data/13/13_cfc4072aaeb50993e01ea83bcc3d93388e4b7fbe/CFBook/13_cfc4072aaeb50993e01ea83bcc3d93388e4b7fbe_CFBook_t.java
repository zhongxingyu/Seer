 package me.cain.cfbook;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class CFBook extends JavaPlugin {
 	
 	Logger log = Logger.getLogger("Minecraft");
 	
 	public static boolean LightningAxe;
 	public static boolean LockDown;
 	public static PermissionHandler permissionHandler;
 	
 	public void onEnable() {
 		PluginManager pm = getServer().getPluginManager();
 		log.info("[CFBook] CFBook has been enabled!");
 		log.info("[CFBook] CFBook was created by CainFoool!");
 		pm.registerEvent(Type.PLAYER_INTERACT, new PListener(), Priority.Normal, this);
 		pm.registerEvent(Type.PLAYER_JOIN, new PListener(), Priority.Normal, this);
 		setupPerms();
 	}
 	
 	public void onDisable() {
 		log.info("[CFBook] CFBook has been disabled!");
 	}
 	
 	private void setupPerms() {
 		if (permissionHandler != null) {
 			return;
 		}
 		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
 		if (permissionsPlugin == null) {
 			log.info("[CFBook] Permissions not detected. Going to OP!");
 			return;
 		}
 		permissionHandler = ((Permissions) permissionsPlugin).getHandler();
 		log.info("[CFBook] Permissions found. Using " + ((Permissions)permissionsPlugin).getDescription().getFullName());
 	}
 	
	public static boolean pCheck(String node, Player player) {
		Plugin permissionsPlugin = Bukkit.getServer().getPluginManager().getPlugin("Permissions");
 		if(permissionsPlugin == null) {
 			return player.isOp();
 		} else {
			CFBook.permissionHandler.has(player, node);
 		}
 		return false;
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String [] args) {
 		Player p = (Player) sender;
 		Player [] derp = Bukkit.getServer().getOnlinePlayers();
 		if(label.equalsIgnoreCase("tp")) {
 			if(args.length < 1) {
 				p.sendMessage("/tp player");
 			} else {
 				if(pCheck("cfbook.teleport.to", ((Player) sender))) {
 					Player tpp = getServer().getPlayer(args[0]);
 					if(tpp != null) {
 					p.teleport(tpp);
 					p.sendMessage("Successfully teleported to " + args[0]);
 				} else {
 					p.sendMessage(ChatColor.RED + "Player " + args[0] + " is not online!");
 				}
 			} else {
 				p.sendMessage(ChatColor.RED + "You don't have permission!");
 			}
 		}
 		}
 		
 		if(label.equalsIgnoreCase("tphere")) {
 			if(args.length < 1) {
 				p.sendMessage("/tphere player");
 			} else {
 				if (pCheck("cfbook.teleport.here", ((Player) sender))) {
 					Player tpp = getServer().getPlayer(args[0]);
 					if(tpp != null) {
 					tpp.teleport(p.getLocation());
 					p.sendMessage(args[0] + " teleported to you successfully!");
 					tpp.sendMessage(p.getDisplayName() + " teleported you to them.");
 				} else {
 					p.sendMessage(ChatColor.RED + "Player " + args[0] + " is not online!");
 				}
 			} else {
 				p.sendMessage("You don't have permission!");
 			}
 		}
 		}
 		
 		if(label.equalsIgnoreCase("scare")) {
 			if(args.length < 1) {
 				p.sendMessage("/scare player");
 			} else {
 				if(pCheck("cfbook.scare", ((Player) sender))) {
 					Location loc = getServer().getPlayer(args[0]).getLocation();
 					World loc2 = getServer().getPlayer(args[0]).getWorld();
 					Player scared = getServer().getPlayer(args[0]);
 					loc2.strikeLightningEffect(loc);
 					p.sendMessage("Scared " + args[0] + " successfully!");
 					scared.sendMessage(((Player) sender).getDisplayName() + " scared you!");
 				
 			} else {
 				p.sendMessage(ChatColor.RED + "You don't have permission!");
 			}
 		}
 		}
 		
 		if(label.equalsIgnoreCase("cfbversion")) {
 			p.sendMessage(ChatColor.GREEN + "CFBook");
			p.sendMessage("Version: 1.15");
 		}
 		
 		if(label.equalsIgnoreCase("time")) {
 			if(args.length < 1) {
 				p.sendMessage("/time <day/night");
 			} else {
 				if(pCheck("cfbook.world.time", ((Player) sender))) {
 					if(args[0].equalsIgnoreCase("day")) {
 					p.getWorld().setTime(0);
 					getServer().broadcastMessage(ChatColor.GOLD + p.getDisplayName() + " made it day time!");
 					}
 					if(args[0].equalsIgnoreCase("night")) {
 					p.getWorld().setTime(1000000);
 					getServer().broadcastMessage(ChatColor.GOLD + p.getDisplayName() + " made it night time!");
 					}
 				} else {
 					p.sendMessage(ChatColor.RED + "You don't have permission!");
 				}
 			}
 		}
 		
 		
 		if(label.equalsIgnoreCase("lightningaxe")) {
 			if(args.length < 1) {
 				p.sendMessage("/lightningaxe on/off");
 			} else {
 				if(pCheck("cfbook.lightningaxe", ((Player) sender))) {
 					if(args[0].equalsIgnoreCase("on")) {
 					LightningAxe = true;
 					p.sendMessage("The almighty Lightning Axe has been enabled!");
 					p.sendMessage("TO USE: Left click with Gold Axe (only on blocks)");
 					}
 					if(args[0].equalsIgnoreCase("off")) {
 					LightningAxe = false;
 					p.sendMessage("The almighty Lightning Axe has been disabled!");
 					}
 				} else {
 					p.sendMessage(ChatColor.RED + "You don't have permission!");
 				}
 			}
 		}
 		
 		/* if(label.equalsIgnoreCase("i")) {
 			if(args.length < 1) {
 				p.sendMessage("/i <player> <itemid>");
 			} else if(args[1].length() < 1) {
 				p.sendMessage("Please specify an item (ID)");
 			} else if(args[0].length() < 1) {
 				p.sendMessage("Please specify a player.");
 			} else {
 				if(p.isOp()) {
 					Player pl = getServer().getPlayer(args[0]);
 					pl.getInventory().addItem(new ItemStack(Integer.parseInt(args[0]), 64));
 					p.sendMessage(ChatColor.RED + "Gave " + pl.getDisplayName() + " some " + args[1]);
 				} else {
 					p.sendMessage(ChatColor.RED + "You don't have permission!");
 				}
 			}
 		}
 		
 		Disabled for the time being. Sorry! 
 		*/
 		
 		if(label.equalsIgnoreCase("creative")) {
 			if(args.length < 1) {
 				p.sendMessage("/creative on/off");
 			} else {
 				if(pCheck("cfbook.world.creative", ((Player) sender))) {
 					if(args[0].equalsIgnoreCase("on")) {
 					for(Player p2: derp) {
 						p2.setGameMode(GameMode.CREATIVE);
 					}
 					getServer().broadcastMessage(ChatColor.GREEN + p.getDisplayName() + " has enabled Creative Building!");
 					}
 					if(args[0].equalsIgnoreCase("off")) {
 				    for(Player p2: derp) {
 				    	p2.setGameMode(GameMode.SURVIVAL);
 				    }
 				    getServer().broadcastMessage(ChatColor.GREEN + p.getDisplayName() + " has disabled Creative Building!");
 					}
 			} else {
 				p.sendMessage(ChatColor.RED + "You don't have permission!");
 			}
 			}
 		}
 		
 		if(label.equalsIgnoreCase("weather")) {
 			if(args.length < 1) {
 				p.sendMessage("/weather sun/rain");
 			} else {
 				if(pCheck("cfbook.world.weather", ((Player) sender))) {
 					if(args[0].equalsIgnoreCase("sun")) {
 						p.getWorld().setStorm(false);
 						p.getWorld().setThundering(false);
 						getServer().broadcastMessage(ChatColor.GOLD + p.getDisplayName() + " has made it sunny!");
 					}
 					if(args[0].equalsIgnoreCase("rain")) {
 						p.getWorld().setStorm(true);
 						p.getWorld().setThundering(true);
 						getServer().broadcastMessage(ChatColor.GOLD + p.getDisplayName() + " has made it rain!");
 					}
 				} else {
 					p.sendMessage(ChatColor.RED + "You don't have permission!");
 				}
 			}
 		}
 		
 		if(label.equalsIgnoreCase("rocket")) { 
 			if(args.length < 1) {
 				p.sendMessage("/rocket <player>");
 			} else {
 				Player p2 = getServer().getPlayer(args[0]);
 				if(pCheck("cfbook.player.rocket", ((Player) sender))) {
 					p2.setVelocity(new Vector(0, 50, 0));
 					p.sendMessage("You shot " + p2.getDisplayName() + " into the sky!");
 				} else {
 					return false;
 				}
 			}
 			}
 		
 		if(label.equalsIgnoreCase("clearinv")) {
 			if(pCheck("cfbook.player.clearinv", ((Player) sender))) {
 				p.getInventory().clear();
 				p.sendMessage("Inventory cleared!");
 			} else {
 				p.sendMessage(ChatColor.RED + "You don't have permission!");
 			}
 		}
 		
 		/* if(label.equalsIgnoreCase("drop")) {
 			if(args.length < 1) {
 				p.sendMessage("/drop id");
 			} else {
 				if(p.isOp()) {
 					p.getWorld().dropItemNaturally(p.getLocation(), new ItemStack(Material.STONE));
 				} else {
 					p.sendMessage(ChatColor.RED + "You don't have permission!");
 				}
 			}
 		}
 		
 		Disabled for the time being. Sorry!
 		
 		*/
 		
 		if(label.equalsIgnoreCase("lockdown")) {
 			if(args.length < 1) {
 				p.sendMessage("/lockdown on/off");
 			} else {
 
 				if(p.isOp()) {
 				if(args[0].equalsIgnoreCase("on"))
 				{
 					LockDown = true;
 					p.sendMessage("Server is now in lockdown.");
 					LockDownKick();
 				}
 				}
 				if(args[0].equalsIgnoreCase("off"))
 				{
 						LockDown = false;
 						p.sendMessage("Server disabled lockdown mode.");
 				}
 					else {
 					p.sendMessage(ChatColor.RED + "You don't have permission!");
 				}
 			}
 		}
 		
 		if(label.equalsIgnoreCase("heal")) {
 			if(args.length < 1)
 			{
 				p.sendMessage("/heal [player]");
 			} else {
 				if(pCheck("cfbook.player.heal", ((Player) sender))) {
 					Bukkit.getServer().getPlayer(args[0]).setHealth(20);
 					p.sendMessage(args[0] + " healed!");
 				}
 				else {
 					p.sendMessage(ChatColor.RED + "You don't have permission!");
 				}
 			}
 		}
 		
 		if(label.equalsIgnoreCase("world"))
 		{
 			if(args.length < 1)
 			{
 				p.sendMessage("/world <create,unload,tp> <world>");
 			}
 			else {
 				if(args[0].equalsIgnoreCase("create"))
 				{
 					if(args[1].equals(""))
 					{
 						p.sendMessage("/world create [world]");
 					}
 					else {
 				if(pCheck("cfbook.world.create", ((Player) sender)))
 				{
 					p.sendMessage("Creating world..");
 					Bukkit.getServer().createWorld(args[1].toString(), Environment.NORMAL);
 					p.sendMessage("Created!");
 				} else {
 					p.sendMessage(ChatColor.RED + "You have no permission!");
 				}
 			}
 				}
 				if(args[0].equalsIgnoreCase("unload"))
 				{
 					if(args[1].equals(""))
 					{
 						p.sendMessage("/world unload [world]");
 					}
 					else {
 				if(pCheck("cfbook.world.unload", ((Player) sender)))
 				{
 					p.sendMessage("Unloading world..");
 					Bukkit.getServer().unloadWorld(args[1].toString(), true);
 					p.sendMessage("Unloaded!");
 				} else {
 					p.sendMessage(ChatColor.RED + "You have no permission!");
 				}
 					}
 				}
 				if(args[0].equalsIgnoreCase("tp"))
 				{
 					if(args[1].equals(""))
 					{
 						p.sendMessage("/world tp [world]");
 					}
 					else {
 				if(pCheck("cfbook.world.tp", ((Player) sender)))
 				{
 					p.teleport(Bukkit.getWorld(args[1]).getSpawnLocation());
 				} else {
 					p.sendMessage(ChatColor.RED + "You have no permission!");
 				}
 					}
 				}
 				}
 		
 			
 		}
 		if(label.equalsIgnoreCase("kickall"))
 		{
 			if(pCheck("cfbook.player.kickall", ((Player) sender)))
 			{
 			for(Player derps: Bukkit.getServer().getOnlinePlayers())
 			{
 				if(pCheck("cfbook.player.kickall.bypass", ((Player) sender))) {
 					return false;
 				} else {
 			derps.kickPlayer("Kicked all players!");	
 			}
 			}
 			}
 			else {
 				p.sendMessage(ChatColor.RED + "You have no permission!");
 			}
 		}
 		if(label.equalsIgnoreCase("nick"))
 		{
 			if(args.length < 1)
 			{
 				p.sendMessage("/nick [nickname]");
 			} else {
 				if(pCheck("cfbook.player.nick", ((Player) sender)))
 				{
 					p.setDisplayName(args[0]);
 				}
 				else {
 					p.sendMessage(ChatColor.RED + "You have no permission!");
 				}
 			}
 		}
 		if(label.equalsIgnoreCase("shutdown"))
 		{
 			if(pCheck("cfbook.shutdown", ((Player) sender)))
 			{
 				getServer().broadcastMessage(ChatColor.GREEN + ((Player) sender).getDisplayName() + " has initiated a shutdown.");
 				Bukkit.getServer().shutdown();
 			}
 			else {
 				p.sendMessage(ChatColor.RED + "You have no permission!");
 			}
 		}
 		return false;
 	}
 	
 	public void LockDownKick()
 	{
 		Player[] p2 = Bukkit.getServer().getOnlinePlayers();
 		for(Player p: p2)
 		{
 			if(!p.isOp()){
 				p.kickPlayer("The server has switched to lockdown.");
 			}
 			else
 			{
 				return;
 			}
 		}
 	}
 	
 }
