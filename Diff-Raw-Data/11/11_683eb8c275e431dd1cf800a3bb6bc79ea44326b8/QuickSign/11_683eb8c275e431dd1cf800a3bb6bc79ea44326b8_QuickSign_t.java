 package me.DDoS.Quicksign;
 
 import com.griefcraft.lwc.LWCPlugin;
 import com.palmergames.bukkit.towny.Towny;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import couk.Adamki11s.Regios.API.RegiosAPI;
 import de.diddiz.LogBlock.Consumer;
 import de.diddiz.LogBlock.LogBlock;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.logging.Logger;
 import me.DDoS.Quicksign.handler.*;
 import me.DDoS.Quicksign.listener.*;
 import me.DDoS.Quicksign.permission.Permissions;
 import me.DDoS.Quicksign.permission.PermissionsHandler;
 import me.DDoS.Quicksign.permission.Permission;
 import me.DDoS.Quicksign.session.*;
 import me.DDoS.Quicksign.sign.SignGenerator;
 import me.DDoS.Quicksign.util.*;
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author DDoS 
  */
 public class QuickSign extends JavaPlugin {
 
     public static final Logger log = Logger.getLogger("Minecraft");
     //
     private Permissions permissions;
     //
     private final SelectionHandler selectionHandler = new SelectionHandler(this);
     //
     private final SignGenerator signGenerator = new SignGenerator(this);
     //
     private SpoutHandler spoutHandler;
     private boolean spoutOn = false;
     //
     private final Map<Player, EditSession> sessions = new HashMap<Player, EditSession>();
     //
     private Consumer consumer;
     //
     private final BlackList blackList = new BlackList(this);
 
     @Override
     public void onEnable() {
 
         permissions = new PermissionsHandler(this).getPermissions();
 
         getServer().getPluginManager().registerEvents(new QSListener(this), this);
 
         checkForWorldGuard();
         checkForResidence();
         checkForRegios();
         checkForTowny();
         checkForLogBlock();
         checkForLWC();
         checkForLockette();
         checkForSpout();
 
         new QSConfig().setupConfig(this);
 
         log.info("[QuickSign] Plugin enabled. v" + getDescription().getVersion() + ", by DDoS");
 
     }
 
     @Override
     public void onDisable() {
 
         sessions.clear();
         log.info("[QuickSign] Plugin disabled. v" + getDescription().getVersion() + ", by DDoS");
 
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
         if (!(sender instanceof Player)) {
 
             sender.sendMessage("This command can only be used in-game.");
             return true;
 
         }
 
         Player player = (Player) sender;
 
         if (cmd.getName().equalsIgnoreCase("qs")
                 && hasPermissions(player, Permission.USE)) {
 
             if (args.length == 0 && !isUsing(player)) {
 
                 sessions.put(player, new StandardEditSession(player, this));
                 QSUtil.tell(player, "enabled [Normal Mode].");
                 return true;
 
             }
 
             if (args.length == 0 && isUsing(player)) {
 
                 removeSession(player);
                 QSUtil.tell(player, "disabled.");
                 return true;
 
             }
 
             if (args.length == 1 && args[0].equalsIgnoreCase("spout")) {
 
                 if (hasPermissions(player, Permission.USE_SPOUT)) {
 
                     if (!spoutOn) {
                         
                         QSUtil.tell(player, "Spout is not installed on the server.");
                         return true;
                         
                     }
                     
                     if (isUsing(player)) {
 
                         QSUtil.tell(player, "Please disable QuickSign, and renable with '/qs spout'.");
                         return true;
 
                     } else {
 
                         sessions.put(player, new SpoutEditSession(player, this));
                         QSUtil.tell(player, "enabled [Spout Mode].");
                         return true;
 
                     }
 
                 } else {
 
                     player.sendMessage(ChatColor.RED + "You don't have permission for this command.");
                     return true;
 
                 }
             }
 
             if (args.length >= 3 && args[0].equalsIgnoreCase("fs")) {
 
                 if (hasPermissions(player, Permission.FS)) {
 
                     signGenerator.createSign(player, args[1], QSUtil.mergeToString(args, 2));
                     return true;
 
                 } else {
 
                     player.sendMessage(ChatColor.RED + "You don't have permission for this command.");
                     return true;
 
                 }
             }
             
             if (args.length >= 3 && args[0].equalsIgnoreCase("rc")) {
 
                 if (hasPermissions(player, Permission.RC)) {
 
                     new QSConfig().setupConfig(this);
                     player.sendMessage(ChatColor.RED + "Configuration reloaded.");
                     return true;
 
                 } else {
 
                     player.sendMessage(ChatColor.RED + "You don't have permission for this command.");
                     return true;
 
                 }
             }
 
             if (!isUsing(player)) {
 
                 QSUtil.tell(player, "You need to activate QuickSign to use this command.");
                 return true;
 
             }
             
             if (args.length == 1 && args[0].equalsIgnoreCase("s")) {
 
                 if (hasPermissions(player, Permission.NO_REACH_LIMIT)) {
 
                     Block block = player.getTargetBlock(null, QSConfig.maxReach);
 
                     if (!QSUtil.checkForSign(block)) {
 
                         QSUtil.tell(player, "This block is not a sign.");
                         return true;
 
                     }
 
                     selectionHandler.handleSignSelection(null, (Sign) block.getState(), player);
                     return true;
 
                 } else {
 
                     player.sendMessage(ChatColor.RED + "You don't have permission for this command.");
                     return true;
 
                 }
 
             } else {
 
                 if (!getSession(player).handleCommand(args)) {
 
                     QSUtil.tell(player, "Your command was not recognized or is invalid.");
 
                 }
 
                 return true;
 
             }
         }
 
         player.sendMessage(ChatColor.RED + "You don't have permission for this command.");
         return true;
 
     }
 
     public SpoutHandler getSpoutHandler() {
 
         return spoutHandler;
 
     }
 
     public SelectionHandler getSelectionHandler() {
 
         return selectionHandler;
 
     }
 
     public boolean isInUse() {
 
         return !sessions.isEmpty();
 
     }
 
     public boolean isUsing(Player player) {
 
         return sessions.containsKey(player);
 
     }
 
     public EditSession getSession(Player player) {
 
         return sessions.get(player);
 
     }
 
     public void removeSession(Player player) {
 
         sessions.remove(player);
 
     }
 
     public Set<Entry<Player, EditSession>> getSessions() {
 
         return sessions.entrySet();
 
     }
     
     public BlackList getBlackList() {
         
         return blackList;
         
     }
     
     public boolean isSpoutOn() {
         
         return spoutOn;
         
     }
     
     public void setSpoutOn(boolean spoutOn) {
         
         this.spoutOn = spoutOn;
         
     }
 
     public Consumer getConsumer() {
 
         return consumer;
 
     }
 
     public void setConsumer(Consumer consumer) {
 
         this.consumer = consumer;
 
     }
 
     private void checkForWorldGuard() {
 
         Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
 
         if (plugin != null && plugin instanceof WorldGuardPlugin) {
 
             log.info("[QuickSign] WorldGuard detected. Features enabled.");
             selectionHandler.setWG((WorldGuardPlugin) plugin);
 
         } else {
 
             log.info("[QuickSign] No WorldGuard detected. Features disabled.");
 
         }
     }
 
     private void checkForResidence() {
 
         PluginManager pm = getServer().getPluginManager();
         Plugin plugin = pm.getPlugin("Residence");
 
         if (plugin != null) {
 
             log.info("[QuickSign] Residence detected. Features enabled.");
             selectionHandler.setResidence(true);
 
         } else {
 
             log.info("[QuickSign] No Residence detected. Features disabled.");
 
         }
     }
 
     private void checkForRegios() {
 
         PluginManager pm = getServer().getPluginManager();
         Plugin plugin = pm.getPlugin("Regios");
 
         if (plugin != null) {
 
             log.info("[QuickSign] Regios detected. Features enabled.");
             selectionHandler.setRegiosAPI(new RegiosAPI());
 
         } else {
 
             log.info("[QuickSign] No Regios detected. Features disabled.");
 
         }
     }
     
     private void checkForTowny() {
 
         PluginManager pm = getServer().getPluginManager();
         Plugin plugin = pm.getPlugin("Towny");
 
         if (plugin != null && plugin instanceof Towny) {
 
             log.info("[QuickSign] Towny detected. Features enabled.");
             selectionHandler.setTowny((Towny) plugin);
 
         } else {
 
             log.info("[QuickSign] No Towny detected. Features disabled.");
 
         }
     }
 
     private void checkForLWC() {
 
         PluginManager pm = getServer().getPluginManager();
         Plugin plugin = pm.getPlugin("LWC");
 
         if (plugin != null && plugin instanceof LWCPlugin) {
 
             log.info("[QuickSign] LWC detected. Features enabled.");
             selectionHandler.setLWC(((LWCPlugin) plugin).getLWC());
 
         } else {
 
             log.info("[QuickSign] No LWC detected. Features disabled.");
 
         }
     }
     
     private void checkForLockette() {
 
         PluginManager pm = getServer().getPluginManager();
         Plugin plugin = pm.getPlugin("Lockette");
 
         if (plugin != null) {
 
             log.info("[QuickSign] Lockette detected. Features enabled.");
             selectionHandler.setLockette(true);
             
         } else {
 
             log.info("[QuickSign] No Lockette detected. Features disabled.");
 
         }
     }
 
     private void checkForLogBlock() {
 
         PluginManager pm = getServer().getPluginManager();
         Plugin plugin = pm.getPlugin("LogBlock");
 
         if (plugin != null && plugin instanceof LogBlock) {
 
             log.info("[QuickSign] LogBlock detected. Features enabled.");
             consumer = ((LogBlock) plugin).getConsumer();
             return;
 
         } else {
 
             log.info("[QuickSign] No LogBlock detected. Features disabled.");
 
         }
     }
 
     private void checkForSpout() {
 
         PluginManager pm = getServer().getPluginManager();
         Plugin plugin = pm.getPlugin("Spout");
 
         if (plugin != null) {
 
             log.info("[QuickSign] Spout detected. Features enabled.");
             spoutOn = true;
 
         } else {
 
             log.info("[QuickSign] No Spout detected. Features disabled.");
             spoutOn = false;
 
         }
     }
 
     public boolean hasPermissions(Player player, Permission permission) {
 
         return permissions.hasPermission(player, permission.getNodeString());
 
     }
 }
