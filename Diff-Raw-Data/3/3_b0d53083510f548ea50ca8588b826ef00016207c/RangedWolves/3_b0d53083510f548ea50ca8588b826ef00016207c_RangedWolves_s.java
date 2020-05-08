 package com.ACStache.RangedWolves;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import com.garbagemule.MobArena.ArenaMaster;
 import com.garbagemule.MobArena.MobArena;
 import com.garbagemule.MobArena.MobArenaHandler;
 
 public class RangedWolves extends JavaPlugin
 {
     public static MobArenaHandler maHandler;
     public static ArenaMaster am;
     private Logger log = Logger.getLogger("Minecraft");
     private PluginDescriptionFile info;
     private static File dir, file;
     private final RWEntityListener entityListener = new RWEntityListener(this);
     private final RWPlayerListener playerListener = new RWPlayerListener(this);
     @SuppressWarnings("unused")
     private RWArenaListener arenaListener;
 
     public void onEnable()
     {
        setupMobArena();
         
         info = getDescription();
         
         RWDebug.setDebug(false);
         
         dir = getDataFolder();
         file = new File(dir, "config.yml");
         if(!dir.exists())
         {
             dir.mkdir();
             log.info("[" + info.getName() + "] No config found. Generating a default config"); 
             RWConfig.initConfig(file);
         }
         else
         {
             Configuration config = new Configuration(file);
             config.load();
             if(config.getKeys("RW-on-Server") != null)
             {
                 RWConfig.loadConfig(file);
                 log.info("[" + info.getName() + "] Config loaded");
             }
             else
             {
                 log.info("[" + info.getName() + "] Config is missing pieces, generating a default config");
                 config.getAll().clear();
                 RWConfig.initConfig(file);
             }
         }
         
         log.info("[" + info.getName() + "] " + info.getVersion() + " Enabled successfully! By: " + info.getAuthors());
         
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Event.Type.CREATURE_SPAWN, entityListener, Priority.Monitor, this);
         pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Monitor, this);
         pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Priority.Monitor, this);
         pm.registerEvent(Event.Type.ENTITY_TAME, entityListener, Priority.Monitor, this);
         pm.registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Priority.Normal, this);
     }
 
     public void onDisable()
     {
         RWConfig.clearWorlds();
         RWConfig.clearArenas();
         RWConfig.clearProjectiles();
         log.info("[" + info.getName() + "] " + info.getVersion() + " Disabled!");
     }
     
     private void setupMobArena()
     {
         Plugin maPlugin = (MobArena)Bukkit.getServer().getPluginManager().getPlugin("MobArena");
         
         if(maPlugin == null) {return;}
         
         maHandler = new MobArenaHandler();
         arenaListener = new RWArenaListener();
         am = ((MobArena)maPlugin).getAM();
     }
     
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
     {
         if(command.getName().equalsIgnoreCase("rw"))
         {
             if(args.length >= 1)
             {
                 if(args[0].equalsIgnoreCase("debug"))
                 {
                     if((sender instanceof Player && ((Player)sender).isOp()) || !(sender instanceof Player))
                     {
                         RWDebug.setDebug(!(RWDebug.getDebug()));
                         if(sender instanceof Player)
                         {
                             if(RWDebug.getDebug())
                                 ((Player)sender).sendMessage(ChatColor.AQUA + "RW: Debug Mode Activated");
                             else
                                 ((Player)sender).sendMessage(ChatColor.AQUA + "RW: Debug Mode Deactivated");
                         }
                         else
                         {
                             if(RWDebug.getDebug())
                                 log.info("[" + info.getName() + "] Debug Mode Activated");
                             else
                                 log.info("[" + info.getName() + "] Debug Mode Deactivated");
                         }
                     }
                     else
                     {
                         ((Player)sender).sendMessage(ChatColor.AQUA + "RW: You don't have permission to do that");
                     }
                 }
                 else if(args[0].equalsIgnoreCase("reload"))
                 {
                     if((sender instanceof Player && ((Player)sender).isOp()) || !(sender instanceof Player))
                     {
                         RWConfig.loadConfig(file);
                         if(sender instanceof Player)
                             ((Player)sender).sendMessage(ChatColor.AQUA + "RW: Config reloaded");
                         else
                             log.info("[" + info.getName() + "] Config reloaded");
                     }
                     else
                     {
                         ((Player)sender).sendMessage(ChatColor.AQUA + "RW: You don't have permission to do that");
                     }
                 }
                 else if(args[0].equalsIgnoreCase("retro"))
                 {
                     if(sender instanceof Player)
                     {
                         Player player = (Player)sender;
                         int wolvesAdded = 0;
                         
                         for(Entity e : player.getNearbyEntities(20, 20, 20)) //check a box (radius of 20) around the player
                         {
                             if(e instanceof Wolf)
                             {
                                 Wolf wolf = (Wolf)e;
                                 if(!RWOwner.checkWorldWolf(wolf)) //if wolf is not part of the known pets
                                 {
                                     Player owner = (Player)wolf.getOwner();
                                     if(owner != null) //wolf has an owner
                                     {
                                         RWOwner.addWolf(owner.getName(), wolf);
                                         wolvesAdded++;
                                     }
                                 }
                             }
                         }
                         
                         if(wolvesAdded != 0)
                             player.sendMessage(ChatColor.AQUA + "RW: " + wolvesAdded + " wolves added to their owners");
                         else
                             player.sendMessage(ChatColor.AQUA + "RW: No new wolves added");
                     }
                     else
                     {
                         log.info("[" + info.getName() + "] You don't have permission to do that from the console");
                     }
                 }
                 else
                 {
                     if(sender instanceof Player)
                     {
                         ((Player)sender).sendMessage(ChatColor.AQUA + "Ranged Wolves version " + info.getVersion());
                         ((Player)sender).sendMessage(ChatColor.AQUA + "Please type '/rw debug', '/rw reload', or '/rw retro'");
                     }
                     else
                     {
                         log.info("[" + info.getName() + "] Please type 'rw debug' or 'rw reload'");
                     }
                 }
             }
         }
         return true;
     }
 }
