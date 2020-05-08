 package me.plornt.healthbar;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.player.AppearanceManager;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class HealthBar extends JavaPlugin {
     public static HashMap<Player, Integer> hn = new HashMap<Player, Integer>();
     public static HealthBar plugin;
     public static Configuration config;
     public static Server server;
     public static String goodHealthColor, hurtHealthColor, containerColor, container1, container2, barCharacter;
     public static Boolean usePermissions = false, useHeroes = false;
     private final HealthBarPlayerListener pl = new HealthBarPlayerListener(this);
     private final HealthBarPluginListener pe = new HealthBarPluginListener(this);
     public final Logger logger = Logger.getLogger("Minecraft");
 
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         // will do something with later..
         if (sender instanceof ConsoleCommandSender || sender.isOp() && !usePermissions || sender.hasPermission("healthbar.reload") && usePermissions) {
            if (commandLabel.equalsIgnoreCase("HealthBar") && args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                 this.loadConfig();
                 sender.sendMessage("c[HealthBar] 9Reloaded Configuration");
                 return true;
             }
         }
         return false;
     }
 
     public void setTitle(Player pl, int health, int tothealth, int death) {
         AppearanceManager sm = SpoutManager.getAppearanceManager();
         if (health >= 0 && health <= tothealth) {
             String bh;
             String gh;
             if (health > 0)
                 gh = new String(new char[health]).replace("\0", barCharacter);
             else
                 gh = "";
             if (health < tothealth)
                 bh = new String(new char[(tothealth - health)]).replace("\0", barCharacter);
             else
                 bh = "";
             String hb = "ece\n" + "" + containerColor + container1 + "" + goodHealthColor + gh + "" + hurtHealthColor + bh + "" + containerColor + container2;
             if (usePermissions) {
                 for (Player player : getServer().getOnlinePlayers()) {
                     if (player.hasPermission("healthbar.cansee")) {
                         if (player instanceof SpoutPlayer) {
                             String[] plName = sm.getTitle((SpoutPlayer) pl, (LivingEntity) player).split("ece");
                             if (plName[0] != null) {
                                 sm.setPlayerTitle((SpoutPlayer) player, (LivingEntity) pl, plName[0] + hb);
                             }
                         }
                     }
                 }
             } else if (pl instanceof SpoutPlayer) {
                 String[] plName = sm.getTitle((SpoutPlayer) pl, (LivingEntity) pl).split("ece");
                 sm.setGlobalTitle((LivingEntity) pl, plName[0] + hb);
             }
         }
     }
 
     @Override
     public void onDisable() {
         this.logger.info("[HealthBar] Shutting Down");
     }
 
     public void loadConfig() {
         if (!new File(getDataFolder(), "config.yml").exists()) {
             try {
                 getDataFolder().mkdir();
                 new File(getDataFolder(), "config.yml").createNewFile();
             } catch (Exception e) {
                 e.printStackTrace();
                 this.logger.info("Unable to create config file");
                 getServer().getPluginManager().disablePlugin(this);
                 return;
             }
         }
         config = this.getConfiguration();
 
         if (config.getKeys().isEmpty()) {
             config.setProperty("Colors.goodHealthColor", "a");
             config.setProperty("Colors.hurtHealthColor", "c");
             config.setProperty("Colors.containerColor", "9");
             config.setProperty("Characters.container1", "[");
             config.setProperty("Characters.container2", "]");
             config.setProperty("Characters.barCharacter", "|");
             config.setProperty("Permissions.usePermissions", "false");
             config.save();
         }
         goodHealthColor = (String) config.getProperty("Colors.goodHealthColor");
         if (goodHealthColor == null)
             goodHealthColor = "a";
         hurtHealthColor = (String) config.getProperty("Colors.hurtHealthColor");
         if (hurtHealthColor == null)
             hurtHealthColor = "9";
         containerColor = (String) config.getProperty("Colors.containerColor");
         if (containerColor == null)
             containerColor = "c";
         container1 = (String) config.getProperty("Colors.container1");
         if (container1 == null)
             container1 = "[";
         container2 = (String) config.getProperty("Colors.container2");
         if (container2 == null)
             container2 = "]";
         barCharacter = (String) config.getProperty("Colors.barCharacter");
         if (barCharacter == null)
             barCharacter = "|";
         if (((String) config.getProperty("Permissions.usePermissions")).equalsIgnoreCase("true"))
             usePermissions = true;
 
     }
 
     // Not sure if its possible but if heroes loads before my plugin is loaded
     // im fairly sure the on plugin enable wont go...
     public void checkHeroes() {
         // testing for Heroes without importing it! :D
         Plugin pla = HealthBar.plugin.getServer().getPluginManager().getPlugin("Heroes");
         if (pla != null) {
             useHeroes = true;
             new HealthBarHeroes(pla);
         }
     }
 
     @Override
     public void onEnable() {
 
         this.logger.info("[HealthBar] Loading..");
         server = getServer();
         loadConfig();
         PluginManager pm = server.getPluginManager();
         pm.registerEvent(Event.Type.PLAYER_JOIN, this.pl, Event.Priority.Monitor, this);
         pm.registerEvent(Event.Type.PLUGIN_ENABLE, this.pe, Event.Priority.Monitor, this);
         pm.registerEvent(Event.Type.PLAYER_RESPAWN, this.pl, Event.Priority.Monitor, this);
         pm.registerEvent(Event.Type.PLAYER_QUIT, this.pl, Event.Priority.Monitor, this);
         getServer().getScheduler().scheduleSyncRepeatingTask(this, new HealthBarEntityListener(this), 0, 1);
         this.logger.info("[HealthBar] Loaded up plugin... Version 0.8.");
     }
 }
