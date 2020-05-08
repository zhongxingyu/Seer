 /*
  * Copyright (C) 2013 Dabo Ross <http://www.daboross.net/>
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
 package net.daboross.bungeedev.ncommon.listeners;
 
 import java.net.InetSocketAddress;
 import net.daboross.bungeedev.ncommon.config.SharedConfig;
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.CommandSender;
 import net.md_5.bungee.api.event.ProxyPingEvent;
 import net.md_5.bungee.api.plugin.Command;
 import net.md_5.bungee.api.plugin.Listener;
 import net.md_5.bungee.event.EventHandler;
 import net.md_5.bungee.event.EventPriority;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class PingStatistics extends Command implements Listener {
 
     private final SharedConfig config;
 
     public PingStatistics(SharedConfig config) {
         super("ping-stats");
         this.config = config;
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPing(ProxyPingEvent evt) {
        InetSocketAddress fromNet = evt.getConnection().getAddress();
        InetSocketAddress toNet = evt.getConnection().getVirtualHost();
         String from = fromNet == null ? null : fromNet.getHostString();
         String to = toNet == null ? null : toNet.getHostString();
         if (from != null && to != null) {
             hostUniquePings(from, to);
             senderUniquePings(from, to);
         }
     }
 
     private void hostUniquePings(String from, String to) {
         JSONObject uniqueHosts = config.getObject("ping-stats.unique-hosts");
         JSONObject uniqueHost = getJsonObject(uniqueHosts, to);
         uniqueHost.put(from, uniqueHost.optInt(from) + 1);
     }
 
     private void senderUniquePings(String from, String to) {
        JSONObject uniqueSenders = config.getObject("ping-stats.unique-users");
         JSONObject uniqueSender = getJsonObject(uniqueSenders, from);
         uniqueSender.put(to, uniqueSender.optInt(to) + 1);
 
     }
 
     @Override
     public void execute(CommandSender sender, String[] args) {
         if (!sender.hasPermission("ncommon.ping-stats")) {
             sender.sendMessage(ChatColor.RED + "You don't have permission.");
             return;
         }
        if (args.length < 1 || !(args[0].equalsIgnoreCase("hosts") || args[0].equalsIgnoreCase("users"))) {
             sender.sendMessage(ChatColor.GREEN + "Choose " + ChatColor.DARK_RED + "hosts" + ChatColor.GREEN + " or " + ChatColor.DARK_RED + "senders" + ChatColor.GREEN + ".");
             return;
         }
         if (args.length > 1) {
             sender.sendMessage(ChatColor.GREEN + "Too many arguments!");
             return;
         }
         if (args[0].equalsIgnoreCase("hosts")) {
             JSONObject uniqueHosts = config.getObject("ping-stats.unique-hosts");
             for (String key : uniqueHosts.keySet()) {
                 sender.sendMessage(ChatColor.DARK_RED + key + ChatColor.GREEN + ":\n" + uniqueHosts.getJSONObject(key).toString(2));
             }
        } else if (args[0].equals("users")) {
            JSONObject uniqueSenders = config.getObject("ping-stats.unique-users");
             for (String key : uniqueSenders.keySet()) {
                 sender.sendMessage(ChatColor.DARK_RED + key + ChatColor.GREEN + ":\n" + uniqueSenders.getJSONObject(key).toString(2));
             }
         }
     }
 
     public JSONObject getJsonObject(JSONObject object, String key) {
         try {
             return object.getJSONObject(key);
         } catch (JSONException ex) {
             JSONObject obj = new JSONObject();
             object.put(key, obj);
             return obj;
         }
     }
 }
