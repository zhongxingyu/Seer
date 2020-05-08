 /*
  * Copyright (C) 2012 mewin <mewin001@hotmail.de>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.mewin.perkShop;
 
 import com.mewin.perkShop.events.PlayerListener;
 import com.mewin.perkShop.shop.Order;
 import com.mewin.perkShop.shop.Perk;
 import com.mewin.util.FileOutputHandler;
 import com.mewin.util.ShopUtils;
 import com.mewin.util.Utils;
 import java.lang.reflect.Field;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_4_5.CraftServer;
 import org.bukkit.entity.Player;
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
     private int scheduledTaskId;
     private PlayerListener listener;
     private CraftServer craftServer;
     
     @Override
     public void onEnable()
     {
         craftServer = (CraftServer) getServer();
         config = Utils.getConfig(this);
         logHandler = new FileOutputHandler(Utils.getLogFile(this), (String) getConfig("log-time-format", "dd.MM.yyyy - hh:mm:ss"));
         getLogger().addHandler(logHandler);
         logHandler.publish(new LogRecord(Level.INFO, "[PerkShop] Enabling PerkShop v" + this.getDescription().getVersion()));
         db = new DatabaseConnector();
         db.init(getConfig("sql.dns", ""), getConfig("sql.username", ""), getConfig("sql.password", ""), getConfig("sql.prefix", ""), this);
         db.connect();
         checker = new ExpireChecker(this);
         scheduledTaskId = getServer().getScheduler().scheduleAsyncRepeatingTask(this, checker, 1200L, getConfig("perk-check-time", 300));
         listener = new PlayerListener(this, db);
         //replaceCommand();
     }
     
     private void replaceCommand()
     {
         Command originalPerkCmd = craftServer.getCommandMap().getCommand("perk");
         Map<String, Command> knownCommands = (Map<String, Command>) getPrivateValue(craftServer.getCommandMap(), "knownCommands");
         knownCommands.put("perk", new PerkCommand(this, originalPerkCmd, db));
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
         listener = null;
     }
 
     @Override
     public boolean onCommand(CommandSender cs, Command cmd, String cmdLabel, String[] params)
     {
         if (cmdLabel.equalsIgnoreCase("perk"))
         {
             String completeCmd = "/" + cmdLabel;
 
             for (String param : params)
             {
                 completeCmd += " " + param;
             }
             logToFile(Level.INFO, "[PerkShop] " + cs.getName() + " tried to use " + completeCmd);
 
             if (params.length < 1)
             {
                 cs.sendMessage(ChatColor.AQUA + cmd.getUsage());
             }
             else if("list".startsWith(params[0].toLowerCase()))
             {
                 if (!(cs instanceof Player))
                 {
                     cs.sendMessage("You don't have any perks.");
                 }
                 else if (cs.hasPermission("perks.list"))
                 {
                     boolean showExpired = params.length > 1 && params[1].equalsIgnoreCase("all");
                     printPlayerPerks((Player) cs, showExpired);
                 }
                 else
                 {
                     cs.sendMessage(ChatColor.RED + cmd.getPermissionMessage());
                 }
             }
             else if ("info".startsWith(params[0].toLowerCase()))
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
                     String restParams = "";
                     for (int i = 1; i < params.length; i++)
                     {
                         restParams += params[i] + " ";
                     }
                     printPlayerPerkInfo((Player) cs, restParams.trim());
                 }
                 else
                 {
                     cs.sendMessage(ChatColor.RED + cmd.getPermissionMessage());
                 }
             }
             else if ("reload".startsWith(params[0].toLowerCase()))
             {
                 if (!(cs instanceof Player))
                 {
                     reloadAllOrders();
                 }
                 else if (cs.hasPermission("perks.reload"))
                 {
                     if (params.length > 1 && "all".startsWith(params[1].toLowerCase()))
                     {
                         if (cs.hasPermission("perks.reload.all"))
                         {
                             cs.sendMessage(ChatColor.GREEN + getConfig("messages.perk-reload-all", "Updating all perks"));
                             reloadAllOrders();
                         }
                         else
                         {
                             cs.sendMessage(cmd.getPermissionMessage());
                         }
                     }
                     else
                     {
                         cs.sendMessage(ChatColor.GREEN + getConfig("messages.perk-reload", "Updating perks"));
                         reloadOrdersForPlayer((Player) cs);
                     }
                 }
                 else
                 {
                     cs.sendMessage(cmd.getPermissionMessage());
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
 
     public void reloadAllOrders()
     {
         for (Player player : getServer().getOnlinePlayers())
         {
             reloadOrdersForPlayer(player);
         }
     }
 
     public void reloadOrdersForPlayer(Player player)
     {
         Set<Order> playerOrders = db.getOrdersForMember(player.getName());
 
         getLogger().log(Level.INFO, "Loading orders for player {0}", player.getName());
 
         for (Order order : playerOrders)
         {
             if (!order.activated() && order.isPaid())
             {
                 getLogger().log(Level.INFO, "Activating order {0} (from {2}) for player {1}",
                         new String[] {
                             Integer.toString(order.id),
                             player.getName(),
                             ShopUtils.sdf.format(order.purchased)
                         });
                 order.activate(player, this, db);
                 if (!order.updatePerksActivated(db))
                 {
                     getLogger().log(Level.WARNING, "Could not save perks for order {0}.", order.id);
                 }
             }
         }
 
         ShopUtils.checkExpiredPerks(player, db, this);
     }
 
     public void logToFile(Level level, String msg)
     {
         LogRecord record = new LogRecord(level, msg);
 
         logHandler.publish(record);
     }
 
     public void printPlayerPerks(Player player, boolean showExpired)
     {
         Set<Perk> perks = ShopUtils.getActivePerks(player.getName(), db, showExpired);
 
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
                 if (!perk.canExpire() || !perk.expired)
                 {
                     perkList += ChatColor.AQUA;
                 }
                 else
                 {
                     perkList += ChatColor.GRAY;
                 }
                 perkList += perk.name;
                 if (itr.hasNext())
                 {
                     perkList += ChatColor.WHITE + ", ";
                 }
             }
             player.sendMessage(perkList);
         }
     }
 
     public void printPlayerPerkInfo(Player player, String name)
     {
         Set<Perk> perks = ShopUtils.getActivePerks(player.getName(), db, true);
 
         Perk thePerk = null;
 
         for(Perk perk : perks)
         {
             if (perk.name.equalsIgnoreCase(name))
             {
                 thePerk = perk;
                 if (!perk.expired)
                 {
                     break;
                 }
             }
 
             if (perk.name.toLowerCase().startsWith(name.toLowerCase()))
             {
                 if (thePerk == null || thePerk.expired)
                 {
                     thePerk = perk;
                 }
             }
         }
 
         if (thePerk == null)
         {
             player.sendMessage(ChatColor.RED + getConfig("messages.no-perk", "You don't have a perk \"{0}\"").replace("{0}", name));
         }
         else
         {
             player.sendMessage(getConfig("messages.perk-info", "§bname§f: §6{name}\n" +
                 "§bdescription§f: §6{description}\n" +
                 "§bexpires§f: §6{expires}").replace("{name}", thePerk.name)
                                            .replace("{description}", thePerk.description)
                                            .replace("{expires}", ShopUtils.getPerkExpireString(thePerk)));
         }
     }
 
     public Object getConfig(String name, Object def)
     {
         return getConfig(name, def, config);
     }
 
     private Object getConfig(String name, Object def, Map<String, Object> map)
     {
         int dot = name.indexOf(".");
 
         if (dot > 0)
         {
             Map subMap = (Map) map.get(name.substring(0, dot));
             
             return getConfig(name.substring(dot + 1), def, subMap);
         }
         if (!map.containsKey(name))
         {
             return def;
         }
         else
         {
             return map.get(name);
         }
     }
 
     public String getConfig(String name, String def)
     {
         return (String) getConfig(name, (Object) def);
     }
 
     public int getConfig(String name, int def)
     {
         return (Integer) getConfig(name, (Object) def);
     }
 
     public double getConfig(String name, double def)
     {
         return (Double) getConfig(name, (Object) def);
     }
     
     private Object getPrivateValue(Object obj, String name)
     {
         try
         {
             Field field = obj.getClass().getDeclaredField(name);
             field.setAccessible(true);
             return field.get(obj);
         }
         catch(NoSuchFieldException ex)
         {
             return null;
         }
         catch(SecurityException ex)
         {
             return null;
         }
         catch(IllegalArgumentException ex)
         {
             return null;
         }
         catch(IllegalAccessException ex)
         {
             return null;
         }
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
