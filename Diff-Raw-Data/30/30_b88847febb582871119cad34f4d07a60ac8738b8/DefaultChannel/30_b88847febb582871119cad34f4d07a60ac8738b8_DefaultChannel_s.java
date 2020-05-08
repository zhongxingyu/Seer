 /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  * Copyright 2012 StarTux
  *
  * This file is part of ChatLink.
  *
  * ChatLink is free software: you can redistribute it and/or
  * modify it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of
  * the License, or (at your option) any later version.
  *
  * ChatLink is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public
  * License along with ChatLink.  If not, see
  * <http://www.gnu.org/licenses/>.
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
 
 package com.winthier.chatlink.channel;
 
 import com.winthier.chatlink.ChatLinkPlugin;
 import com.winthier.chatlink.Util;
 import com.winthier.chatlink.packet.ChatPacket;
 import com.winthier.winlink.BukkitRunnable;
 import com.winthier.winlink.WinLink;
 import com.winthier.winlink.WinLinkPlugin;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Cancellable;
 import org.bukkit.event.Listener;
 
 public abstract class DefaultChannel implements Channel {
         protected ChatLinkPlugin plugin;
         protected String name;
         protected String chatCommand;
         protected String ignoreCommand;
         protected String format;
         protected String permission;
         protected Set<String> ignoreList = Collections.synchronizedSet(new LinkedHashSet<String>());
 
         public DefaultChannel(ChatLinkPlugin plugin, String name) {
                 this.plugin = plugin;
                 this.name = name;
         }
 
         @Override
         public void enable() {
         }
 
         @Override
         public void disable() {
                 getListener(plugin).unregisterChannel(this);
         }
 
         protected abstract DefaultListener getListener(ChatLinkPlugin plugin);
 
         @Override
         public void loadConfiguration(ConfigurationSection section) {
                 chatCommand = section.getString("Prefix", "#");
                 ignoreCommand = section.getString("Ignore");
                 permission = section.getString("Permission");
                 format = Util.replaceColorCodes(section.getString("Format", "&f[{server}]{sender}: {message}"));
                 getListener(plugin).registerChannel(this);
         }
 
         @Override
         public void sendChat(final String sender, final String server, final String message) {
                 String worldName = plugin.getServer().getWorlds().get(0).getName();
                final String output = format.replaceAll("\\{server\\}", Matcher.quoteReplacement(server)).replaceAll("\\{sender\\}", Matcher.quoteReplacement(sender)).replaceAll("\\{message\\}", Matcher.quoteReplacement(message)).replaceAll("\\{prefix\\}", Matcher.quoteReplacement(Util.replaceColorCodes(plugin.getChat().getPlayerPrefix(worldName, sender)))).replaceAll("\\{suffix\\}", Matcher.quoteReplacement(Util.replaceColorCodes(plugin.getChat().getPlayerSuffix(worldName, sender))));
                 plugin.getLogger().info(String.format("[%s][%s]%s: %s", server, name, sender, message));
                 new BukkitRunnable() {
                         public void run() {
                                 for (Player player : plugin.getServer().getOnlinePlayers()) {
                                         if (ignoreList.contains(player.getName())) continue;
                                         if (plugin.ignore.doesIgnore(player.getName(), sender)) continue;
                                         if (permission != null && !player.hasPermission(permission)) continue;
                                         player.sendMessage(output);
                                 }
                         }
                 }.runTask(plugin);
         }
 
         @Override
         public String getName() {
                 return name;
         }
 
         public String getChatCommand() {
                 return chatCommand;
         }
 
         public String getIgnoreCommand() {
                 return ignoreCommand;
         }
 
         protected void ignore(final Player player) {
                 if (ignoreList.remove(player.getName())) {
                         plugin.getLogger().info(player.getName() + " will no longer ignore channel " + name);
                         new BukkitRunnable() {
                                 public void run() {
                                         player.sendMessage("No longer ignoring channel");
                                 }
                         }.runTask(plugin);
                 } else {
                         ignoreList.add(player.getName());
                         plugin.getLogger().info(player.getName() + " will ignore channel " + name);
                         new BukkitRunnable() {
                                 public void run() {
                                         player.sendMessage("Now ignoring channel");
                                 }
                         }.runTask(plugin);
                 }
         }
 
         protected void chat(final Player player, final String message) {
                 if (message.length() == 0) return;
                 // We need a permission check. So in case this is async, defer it to the main thread.
                 new BukkitRunnable() {
                         public void run() {
                                 if (permission != null && !player.hasPermission(permission)) {
                                         player.sendMessage(ChatColor.RED + "You do not have permission");
                                         return;
                                 }
                                 WinLinkPlugin.getWinLink().broadcastPacket(new ChatPacket(player.getName(), name, message));
                                 sendChat(player.getName(), WinLinkPlugin.getWinLink().getServerName(), message);
                         }
                 }.runTask(plugin);
         }
 
         protected abstract static class DefaultListener implements Listener {
                 protected ChatLinkPlugin plugin;
                 protected Map<String, DefaultChannel> speakMap = Collections.synchronizedMap(new HashMap<String, DefaultChannel>());
                 protected Map<String, DefaultChannel> ignoreMap = Collections.synchronizedMap(new HashMap<String, DefaultChannel>());
 
                 protected DefaultListener(ChatLinkPlugin plugin) {
                         this.plugin = plugin;
                         plugin.getServer().getPluginManager().registerEvents(this, plugin);
                 }
 
                 public void registerChannel(DefaultChannel channel) {
                         speakMap.put(channel.getChatCommand(), channel);
                         if (channel.getIgnoreCommand() != null) ignoreMap.put(channel.getIgnoreCommand(), channel);
                 }
 
                 public void unregisterChannel(DefaultChannel channel) {
                         speakMap.remove(channel.getChatCommand());
                         if (channel.getIgnoreCommand() != null) ignoreMap.remove(channel.getIgnoreCommand());
                 }
         }
 }
