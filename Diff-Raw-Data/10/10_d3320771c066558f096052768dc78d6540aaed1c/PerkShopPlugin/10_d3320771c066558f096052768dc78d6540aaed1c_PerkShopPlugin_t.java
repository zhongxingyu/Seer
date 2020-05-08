 package com.mewin.perkShop;
 
 import com.mewin.perkShop.shop.Perk;
 import com.mewin.util.FileOutputHandler;
 import com.mewin.util.ShopUtils;
 import com.mewin.util.Utils;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author mewin<mewin001@hotmail.de>
  */
 public class PerkShopPlugin extends JavaPlugin {
     private Map<String, Object> config;
     private FileOutputHandler logHandler;
     private DatabaseConnector db;
     private ExpireChecker checker;
     int scheduledTaskId;
     
     @Override
     public void onEnable()
     {
         config = Utils.getConfig(this);
         logHandler = new FileOutputHandler(Utils.getLogFile(this), (String) getConfig("log-time-format", "dd.MM.yyyy - hh:mm:ss"));
         getLogger().addHandler(logHandler);
         logHandler.publish(new LogRecord(Level.INFO, "[PerkShop] Enabling PerkShop v" + this.getDescription().getVersion()));
         db = new DatabaseConnector();
         db.init(getConfig("sql.dsn", ""), getConfig("sql.username", ""), getConfig("sql.password", ""), getConfig("sql.prefix", ""), this);
         db.connect();
         checker = new ExpireChecker(this);
         scheduledTaskId = getServer().getScheduler().scheduleAsyncRepeatingTask(this, checker, 1200L, getConfig("perk-check-time", 300));
     }
     
     @Override
     public void onDisable()
     {
         config = null;
         getLogger().removeHandler(logHandler);
         logHandler.close();
         logHandler = null;
         db.destroy();
         db = null;
         getServer().getScheduler().cancelTask(scheduledTaskId);
         scheduledTaskId = -1;
         checker = null;
     }
     
     @Override
     public boolean onCommand(CommandSender cs, Command cmd, String cmdLabel, String[] params)
     {
         if (cmdLabel.equalsIgnoreCase("perk"))
         {
             if (params.length < 1)
             {
                 cs.sendMessage(ChatColor.AQUA + cmd.getUsage());
             }
             else if(params[0].equalsIgnoreCase("list"))
             {
                 if (!(cs instanceof Player))
                 {
                     cs.sendMessage("You don't have any perks.");
                 }
                 else if (cs.hasPermission("perks.list"))
                 {
                     printPlayerPerks((Player) cs);
                 }
                 else
                 {
                     cs.sendMessage(ChatColor.RED + cmd.getPermissionMessage());
                 }
             }
             else if (params[0].equalsIgnoreCase("info"))
             {
                 if (!(cs instanceof Player))
                 {
                     cs.sendMessage("You don't have any perks.");
                 }
                 else if (params.length < 2)
                 {
                     cs.sendMessage(ChatColor.AQUA + cmd.getUsage());
                 }
                 else if (cs.hasPermission("perks.info"))
                 {
                     printPlayerPerkInfo((Player) cs, params[1]);
                 }
                 else
                 {
                     cs.sendMessage(ChatColor.RED + cmd.getPermissionMessage());
                 }
             }
             else
             {
                 cs.sendMessage(cmd.getUsage());
             }
             return true;
         }
         
         return false;
     }
     
     public void printPlayerPerks(Player player)
     {
         Set<Perk> perks = ShopUtils.getActivePerks(player.getName(), db);
         
         if (perks.size() < 1)
         {
             player.sendMessage(ChatColor.GRAY + getConfig("messages.no-perk-active", "There is currently no perk active on you."));
         }
         else
         {
             player.sendMessage(ChatColor.GRAY + getConfig("messages.current-perks", "Your current perks:"));
             if (player.hasPermission("perks.info"))
             {
                 player.sendMessage(ChatColor.WHITE + getConfig("messages.type-info", "Type /perk info [perk] to get more information."));
             }
             String perkList = "";
             
             Iterator<Perk> itr = perks.iterator();
             
             while (itr.hasNext())
             {
                 Perk perk = itr.next();
                 perkList += perk.name;
                 if (itr.hasNext())
                 {
                     perkList += ", ";
                 }
             }
             player.sendMessage(ChatColor.AQUA + perkList);
         }
     }
     
     public void printPlayerPerkInfo(Player player, String name)
     {
         Set<Perk> perks = ShopUtils.getActivePerks(player.getName(), db);
         
         Perk thePerk = null;
         
         for(Perk perk : perks)
         {
             if (perk.name.equalsIgnoreCase(name))
             {
                 thePerk = perk;
                 break;
             }
         }
         
         if (thePerk == null)
         {
             player.sendMessage(ChatColor.RED + getConfig("messages.no-perk", "You don't have a perk \"{0}\"").replace("{0}", name));
         }
         else
         {
             player.sendMessage(getConfig("messages.no-perk", "§bname§f: §6{name}\n" +
                 "§bdescription§f: §6{description}\n" +
                "§bexpires§f: §6{expires}").replace("{name}", thePerk.name)
                                           .replace("{description}", thePerk.description)
                                           .replace("{expire}", ShopUtils.getPerkExpireString(thePerk)));
         }
     }
     
     public Object getConfig(String name, Object def)
     {
         if (!config.containsKey(name))
         {
             return def;
         }
         else
         {
             return config.get(name);
         }
     }
     
     public String getConfig(String name, String def)
     {
         return (String) getConfig(name, (Object) def);
     }
     
     public int getConfig(String name, int def)
     {
         return (int) getConfig(name, (Object) def);
     }
     
     public double getConfig(String name, double def)
     {
         return (double) getConfig(name, (Object) def);
     }
     
     private class ExpireChecker implements Runnable
     {
        private PerkShopPlugin plugin;
         
        public ExpireChecker(PerkShopPlugin plugin)
         {
             this.plugin = plugin;
         }
         
         @Override
         public void run() {
             for (Player player : getServer().getOnlinePlayers())
             {
                 ShopUtils.checkExpiredPerks(player, db, plugin);
             }
         }
         
     }
 }
