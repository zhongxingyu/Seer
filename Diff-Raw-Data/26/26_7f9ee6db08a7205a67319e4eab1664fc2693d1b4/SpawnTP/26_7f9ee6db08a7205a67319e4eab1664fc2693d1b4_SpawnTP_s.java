 package net.lazlecraft.spawntp;
 
 import java.io.IOException;
 import net.lazlecraft.spawntp.Metrics;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Color;
 import org.bukkit.FireworkEffect;
 import org.bukkit.FireworkEffect.Type;
 import org.bukkit.Location;
 import org.bukkit.Sound;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Firework;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.meta.FireworkMeta;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SpawnTP extends JavaPlugin implements Listener {
 	
 	public float sYaw;
 	public float sPitch;
 	public double sX;
 	public double sY;
 	public double sZ;
 	public String sWorld;
	public String sD;
 	public String prefix = ChatColor.GOLD +""+ ChatColor.BOLD + "[" + ChatColor.RED + ChatColor.BOLD + "SpawnTP" + ChatColor.GOLD + ChatColor.BOLD + "] ";
 	
 	public void onEnable(){
 		//Config
 		getConfig().options().copyDefaults(true);
		this.saveConfig();
 		//Get config values.
 		confreload();
 		//Register events
 		getServer().getPluginManager().registerEvents(this, this); 
 		//Metrics
 		try {
                     Metrics metrics = new Metrics(this);
                     metrics.start();
         }
                 catch (IOException e) {}
 	} 
 	
	public void confreload() {
 		this.saveConfig();
		this.reloadConfig();
		sYaw = getConfig().getInt("SpawnYaw");
		sPitch = getConfig().getInt("SpawnPitch");
 		sX = getConfig().getDouble("SpawnX");
 		sY = getConfig().getDouble("SpawnY");
 		sZ = getConfig().getDouble("SpawnZ");
 		sWorld = getConfig().getString("SpawnWorld");
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         if (!(sender instanceof Player)) {
             sender.sendMessage("You may not use SpawnTP commands, console!");
             return false;
         }
              
     	Player p = (Player)sender;
         if (((commandLabel.equalsIgnoreCase("setspawn")) || commandLabel.equalsIgnoreCase("sss")) && (sender.hasPermission("spawntp.setspawn"))) {
         	Location l = p.getLocation();
         	int x = l.getBlockX();
         	int y = l.getBlockY();
         	int z = l.getBlockZ();
         	getConfig().set("SpawnX", Double.valueOf(l.getBlockX() + 0.5));
         	getConfig().set("SpawnY", Double.valueOf(l.getBlockY() + 0.5));
         	getConfig().set("SpawnZ", Double.valueOf(l.getBlockZ() + 0.5));
         	getConfig().set("SpawnYaw", Float.valueOf(l.getYaw()));
         	getConfig().set("SpawnPitch", Float.valueOf(l.getPitch()));
         	getConfig().set("SpawnWorld", String.valueOf(l.getWorld().getName()));
         	p.getWorld().setSpawnLocation(x, y, z);
         	p.sendMessage(prefix + ChatColor.GREEN + "Spawn set!");
         	confreload();
         }
         else if (commandLabel.equalsIgnoreCase("spawn") && (sender.hasPermission("spawntp.spawn"))) {
         	Location SpawnLoc = new Location(Bukkit.getServer().getWorld(sWorld), sX, sY, sZ, sYaw, sPitch);
         	p.teleport(SpawnLoc);
         }
         else if (commandLabel.equalsIgnoreCase("spawntp")) {
         	p.sendMessage(prefix + ChatColor.GOLD + "This plugin is made by the almighty LaxWasHere");
        	p.sendMessage(prefix + ChatColor.GOLD + "Running version " + ChatColor.RED + this.getDescription().getVersion());
         	confreload();
         }
         else if (commandLabel.equalsIgnoreCase("spawnloc") && (sender.hasPermission("spawntp.location"))) {
         	p.sendMessage(prefix + ChatColor.GREEN + "World: " + ChatColor.GOLD + sWorld);
         	p.sendMessage(prefix + ChatColor.GREEN + "X: " + ChatColor.GOLD + sX);
         	p.sendMessage(prefix + ChatColor.GREEN + "Y: " + ChatColor.GOLD + sY);
         	p.sendMessage(prefix + ChatColor.GREEN + "Z: " + ChatColor.GOLD + sZ);
         }
         return true;
 	}
 	
 	//Null Join Message.
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent ev) {
 		if (!getConfig().getBoolean("EnableJoinQuitMessages")) {
 		ev.setJoinMessage("");
 		}
 	}
 	
 	//Null Quit Message
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent ev) {
 		if (!getConfig().getBoolean("EnableJoinQuitMessages")) {
 		ev.setQuitMessage("");
 		}
 	}
 	
 	@EventHandler
 	public void onPJ(PlayerJoinEvent ev) {
 		//Play sound
 		Player p = ev.getPlayer(); 
 		if (getConfig().getBoolean("SoundOnJoin")) {
 			Location loc = p.getLocation();
 			p.playSound(loc, Sound.valueOf(getConfig().getString("LoginSound")), getConfig().getInt("SoundVolume"), getConfig().getInt("SoundPitch"));
 		}
 	}
 	
 	//Fireworks
 	@EventHandler
 	//Thanks to http://lazle.us/11VGS5v
 	public void onJoinPlayer(PlayerJoinEvent ev) {
 		Player p = ev.getPlayer();
 		if (getConfig().getBoolean("FireworkOnJoin")) {
 			Location loc = p.getLocation();
 			Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
 			FireworkMeta ftw = (FireworkMeta) firework.getFireworkMeta();
			ftw.addEffects(FireworkEffect.builder().withFlicker().withTrail().withFade(Color.AQUA).withColor(Color.GREEN).with(Type.BALL_LARGE).build());
 			ftw.setPower(2);
 			firework.setFireworkMeta(ftw);
 	}
 }
 	
 	//SpawnTP
 	@EventHandler
 	public void onJoin(PlayerJoinEvent ev) {
 		Player p = ev.getPlayer();
 		if (sWorld == null)
 			p.teleport(p.getWorld().getSpawnLocation().add(0.5,0.5,0.5));
 		else {
 			Location SpawnLoc = new Location(Bukkit.getServer().getWorld(sWorld), sX, sY, sZ, sYaw, sPitch);
 			p.teleport(SpawnLoc);		
 		}
 	}
 }
 //LaxWasHere
