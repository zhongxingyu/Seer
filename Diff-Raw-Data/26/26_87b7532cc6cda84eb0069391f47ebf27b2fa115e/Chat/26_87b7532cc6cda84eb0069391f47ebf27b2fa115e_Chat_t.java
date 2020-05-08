 package com.thepastimers.Chat;
 
 import com.thepastimers.Database.Database;
 import com.thepastimers.Logger.Logger;
 import com.thepastimers.Worlds.Worlds;
 import org.bukkit.*;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.conversations.Conversation;
 import org.bukkit.conversations.ConversationAbandonedEvent;
 import org.bukkit.entity.*;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.inventory.*;
 import org.bukkit.map.MapView;
 import org.bukkit.metadata.MetadataValue;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionAttachment;
 import org.bukkit.permissions.PermissionAttachmentInfo;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.scheduler.BukkitTask;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.util.*;
 import org.bukkit.util.Vector;
 
 import java.lang.reflect.Method;
 import java.net.InetSocketAddress;
 import java.sql.Timestamp;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: solum
  * Date: 6/22/13
  * Time: 9:48 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Chat extends JavaPlugin implements Listener {
     Database database;
     Worlds worlds;
     Logger logger;
     Map<Integer,Map<Class,JavaPlugin>> listeners;
     List<ChatCode> codes;
     Map<String,Map<Class,JavaPlugin>> commandListeners;
 
     @Override
     public void onEnable() {
         getLogger().info("Chat init");
 
         getServer().getPluginManager().registerEvents(this,this);
 
         database = (Database)getServer().getPluginManager().getPlugin("Database");
 
         if (database == null) {
             getLogger().warning("Unable to load Database plugin. Some functionality may not be available");
         }
 
         worlds = (Worlds)getServer().getPluginManager().getPlugin("Worlds");
         if (worlds == null) {
             getLogger().warning("Unable to load Worlds plugin. Some functionality may not be available.");
         }
 
         logger = (Logger)getServer().getPluginManager().getPlugin("Logger");
         if (logger == null) {
             getLogger().warning("Unable to load Logger plugin. Some functionality may not be available.");
         }
 
         getLogger().info(ChatData.getTableInfo());
         getLogger().info(CommandData.getTableInfo());
 
         BukkitTask task = new GetChats(this,database).runTaskTimer(this,0,60);
         BukkitTask task2 = new GetCommands(this,database).runTaskTimer(this,0,60);
         listeners = new HashMap<Integer,Map<Class,JavaPlugin>>();
         commandListeners = new HashMap<String, Map<Class, JavaPlugin>>();
 
         codes = new ArrayList<ChatCode>();
 
         codes.add(new ChatCode(":red:;&c",ChatColor.RED.toString(),"Red text"));
         codes.add(new ChatCode(":blue:;&1",ChatColor.BLUE.toString(),"Blue text"));
         codes.add(new ChatCode(":green:;&a",ChatColor.GREEN.toString(),"Green text"));
         codes.add(new ChatCode(":dark green:;&2",ChatColor.DARK_GREEN.toString(),"Dark Green text"));
         codes.add(new ChatCode(":yellow:;&e",ChatColor.YELLOW.toString(),"Yellow text"));
         codes.add(new ChatCode(":purple:;&d",ChatColor.LIGHT_PURPLE.toString(),"Purple text"));
         codes.add(new ChatCode(":white:;&f",ChatColor.WHITE.toString(),"White text"));
         codes.add(new ChatCode(":gold:;&6",ChatColor.GOLD.toString(),"Gold text"));
         codes.add(new ChatCode(":gray:;&7",ChatColor.GRAY.toString(),"Gray text"));
         codes.add(new ChatCode(":black:;&0",ChatColor.BLACK.toString(),"Black text"));
         codes.add(new ChatCode(":dark red:;&4",ChatColor.DARK_RED.toString(),"Dark Red text"));
         codes.add(new ChatCode(":aqua:;&b",ChatColor.AQUA.toString(),"Aqua text"));
         codes.add(new ChatCode(":dark aqua:;&3",ChatColor.DARK_AQUA.toString(),"Dark Aqua text"));
         codes.add(new ChatCode(":dark blue:;&9",ChatColor.DARK_BLUE.toString(),"Dark Blue text"));
         codes.add(new ChatCode(":dark purple:;&5",ChatColor.DARK_PURPLE.toString(),"Dark Purple text"));
         codes.add(new ChatCode(":dark gray:;&8",ChatColor.DARK_GRAY.toString(),"Dark Gray text"));
 
         codes.add(new ChatCode(":obf:;&k",ChatColor.getByChar("k").toString(),"Obfuscated text"));
         codes.add(new ChatCode(":strike:;&m",ChatColor.getByChar("m").toString(),"Strikethrough"));
         codes.add(new ChatCode(":italic:;&o",ChatColor.getByChar("o").toString(),"Italic"));
         codes.add(new ChatCode(":underline:;&n",ChatColor.getByChar("n").toString(),"Underline"));
         codes.add(new ChatCode(":bold:;&l",ChatColor.getByChar("l").toString(),"Bold"));
 
         codes.add(new ChatCode(":reset:;&r",ChatColor.getByChar("r").toString(),"Reset formatting"));
 
         getLogger().info("Chat init complete");
     }
 
     @Override
     public void onDisable() {
         getLogger().info("Chat disable");
     }
 
     public void register(Class c, JavaPlugin plugin) {
         register(c,plugin,1);
     }
 
     public void register(Class c, JavaPlugin plugin, int priority) {
         if (listeners == null) {
             listeners = new HashMap<Integer,Map<Class,JavaPlugin>>();
         }
         Map<Class,JavaPlugin> listenerMap = listeners.get(priority);
         if (listenerMap == null) {
             listenerMap = new HashMap<Class, JavaPlugin>();
             listeners.put(priority,listenerMap);
         }
         listenerMap.put(c,plugin);
     }
 
     public void registerCommand(String command, Class c, JavaPlugin plugin) {
         Map<Class,JavaPlugin> classMap = new HashMap<Class, JavaPlugin>();
         classMap.put(c,plugin);
         commandListeners.put(command, classMap);
     }
 
     @EventHandler
     public void onChat(AsyncPlayerChatEvent event) {
         sendChat(event.getMessage(),event.getPlayer().getName());
         event.setCancelled(true);
     }
 
     public void sendChat(String message, String player) {
         sendChat(message,player,true);
     }
 
     public void sendChat(String message, String player, boolean save) {
         sendChat(message,player,save,false);
     }
 
     public void sendChat(String message, String player, boolean save, boolean web) {
         ChatData data = new ChatData();
         data.setPlayer(player);
         data.setPlayerString(player);
         data.setMessage(message);
         Date date = new Date();
         data.setTime(new Timestamp(date.getTime()));
         data.setSeen(true);
 
         SortedSet<Integer> keys = new TreeSet<Integer>(listeners.keySet());
         for (Integer prio : keys) {
             Map<Class,JavaPlugin> classMap = listeners.get(prio);
             for (Class c : classMap.keySet()) {
                 try {
                     JavaPlugin p = classMap.get(c);
                     Class[] argTypes = new Class[] {ChatData.class};
                     Method m = c.getDeclaredMethod("doChat",argTypes);
                     m.invoke(p,data);
                 } catch (Exception e) {
                     getLogger().warning("Unable to call doChat for " + c.getName());
                     e.printStackTrace();
                 }
             }
         }
 
         // handle colors
         String bak = data.getPlayerString();
         data.setPlayerString(replaceColor(data.getPlayerString()));
         data.setPlayerString(data.getPlayerString() + ChatColor.getByChar("r").toString());
 
         String messageBak = data.getMessage();
         data.setMessage(replaceColor(data.getMessage()));
         data.setMessage(data.getMessage() + ChatColor.getByChar("r").toString());
         String originalMessage = stripColors(data.getMessage());
 
        Date now = new Date();

        String mainMessage = "(" + now.getHours() + ":" + now.getMinutes() + ") <" + data.getPlayerString() + "> " + data.getMessage();
         String vanillaMessage = "";
         if (web) {
            mainMessage = ChatColor.LIGHT_PURPLE + "[WEB]" + ChatColor.WHITE + " " + mainMessage;
         }
 
         vanillaMessage = "<" + data.getPlayer() + "> " + originalMessage;
 
         Player[] list = getServer().getOnlinePlayers();
 
         for (Player p : list) {
             if (worlds != null && worlds.getPlayerWorldType(p.getName()) == Worlds.VANILLA) {
                 p.sendMessage(vanillaMessage);
             } else {
                 p.sendMessage(mainMessage);
             }
         }
         getLogger().info(mainMessage);
 
         if (logger != null) {
             Player p = getServer().getPlayer(player);
             if (p == null) {
                 //getLogger().info("Got player " + player);
                 OfflinePlayer op = getServer().getOfflinePlayer(player);
                 logger.writeEvent(op,"chat",messageBak);
                 //getLogger().info("Player is " + p);
             } else {
                 logger.writeEvent(p,"chat",messageBak);
             }
         }
 
         // we want it unspoiled (without the color codes) for when it goes into db
         data.setPlayerString(bak);
         data.setMessage(messageBak);
         if (database != null && save) {
             data.save(database);
         }
     }
     
     public String replaceColor(String text) {
        for (ChatCode code : codes) {
             //getLogger().info(text + " " + code.getKey() + " " + code.getCode());
             String[] codeList = code.getKey().split(";");
             for (int i=0;i<codeList.length;i++) {
                 text = text.replace(codeList[i],code.getCode());
             }
         }
         return text;
     }
 
     public String stripColors(String text) {
         for (ChatCode code : codes) {
             //getLogger().info(text + " " + code.getKey() + " " + code.getCode());
             String[] codeList = code.getKey().split(";");
             for (int i=0;i<codeList.length;i++) {
                 text = text.replace(codeList[i],"");
             }
         }
         return text;
     }
 
     public void saveMessage(String message, String player) {
         ChatData data = new ChatData();
         data.setPlayer(player);
         data.setPlayerString(player);
         data.setMessage(message);
         Date date = new Date();
         data.setTime(new Timestamp(date.getTime()));
         data.setSeen(true);
 
         SortedSet<Integer> keys = new TreeSet<Integer>(listeners.keySet());
         for (Integer prio : keys) {
             Map<Class,JavaPlugin> classMap = listeners.get(prio);
             for (Class c : classMap.keySet()) {
                 try {
                     JavaPlugin p = classMap.get(c);
                     Class[] argTypes = new Class[] {ChatData.class};
                     Method m = c.getDeclaredMethod("doChat",argTypes);
                     m.invoke(p,data);
                 } catch (Exception e) {
                     getLogger().warning("Unable to call doChat for " + c.getName());
                     getLogger().warning(e.getMessage());
                 }
             }
         }
 
         if (database != null) {
             data.save(database);
         }
     }
 
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         String playerName = "";
 
         if (sender instanceof Player) {
             playerName = ((Player)sender).getName();
         } else {
             playerName = "CONSOLE";
         }
 
         String command = cmd.getName();
 
         if (command.equalsIgnoreCase("codes")) {
             sender.sendMessage("Chat codes:");
             for (ChatCode code : codes) {
                 String key = code.getKey().replace(";"," or ");
                 sender.sendMessage(key + " = " + code.getDescription());
             }
         } else {
             return false;
         }
         return true;
     }
 }
