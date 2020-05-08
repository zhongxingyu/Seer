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
 
 import com.dthielke.herochat.ChannelChatEvent;
 import com.winthier.chatlink.ChatLinkPlugin;
 import com.winthier.chatlink.Util;
 import com.winthier.chatlink.packet.ChatPacket;
 import com.winthier.winlink.BukkitRunnable;
 import com.winthier.winlink.WinLink;
 import com.winthier.winlink.WinLinkPlugin;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 
 public class HeroChatChannel implements Channel, Listener {
         private final ChatLinkPlugin plugin;
         private final String name;
         private String channelName;
         private String format;
 
         public HeroChatChannel(ChatLinkPlugin plugin, String name) {
                 this.plugin = plugin;
                 this.name = name;
         }
 
         @Override
         public void enable() {
         }
 
         @Override
         public void disable() {
                 HeroChatListener.getInstance(plugin).unregisterChannel(this);
         }
 
         @Override
         public void loadConfiguration(ConfigurationSection section) {
                 channelName = section.getString("Channel", "Global");
                 format = Util.replaceColorCodes(section.getString("Format", "[{server}]{sender}: {message}"));
                 HeroChatListener.getInstance(plugin).registerChannel(this);
         }
 
         @Override
         public void sendChat(String sender, String server, String message) {
                String msg = format.replaceAll("\\{server\\}", Matcher.quoteReplacement(server)).replaceAll("\\{sender\\}", Matcher.quoteReplacement(sender)).replaceAll("\\{message\\}", Matcher.quoteReplacement(message));
                 plugin.getLogger().info(String.format("[%s][%s]%s: %s", server, name, sender, message));
                 new HeroChatMessageTask(plugin, sender, msg, channelName).runTask(plugin);
         }
 
         @Override
         public String getName() {
                 return name;
         }
 
         public String getChannelName() {
                 return channelName;
         }
 
         /**
          * This method is called when HeroChatListener handles a
          * ChannelChatEvent with a matching channel name.
          */
         public void onChannelChat(ChannelChatEvent event) {
                 WinLinkPlugin.getWinLink().broadcastPacket(new ChatPacket(event.getSender().getName(), name, event.getMessage()));
         }
 }
 
 class HeroChatMessageTask extends BukkitRunnable {
         private final ChatLinkPlugin plugin;
         private final String sender;
         private final String message;
         private final String channelName;
 
         public HeroChatMessageTask(ChatLinkPlugin plugin, String sender, String message, String channelName) {
                 this.plugin = plugin;
                 this.sender = sender;
                 this.message = message;
                 this.channelName = channelName;
         }
 
         public void run() {
                 com.dthielke.herochat.Channel channel = com.dthielke.herochat.Herochat.getChannelManager().getChannel(channelName);
                 if (channel == null) {
                         plugin.getLogger().warning("HeroChat channel not found: `" + channelName + "'");
                         return;
                 }
                 for (com.dthielke.herochat.Chatter chatter : channel.getMembers()) {
                         Player player = chatter.getPlayer();
                         if (player == null) continue;
                         if (com.dthielke.herochat.Herochat.getChatterManager().getChatter(player).getIgnores().contains(sender.toLowerCase())) return;
                         player.sendMessage(message);
                 }
         }
 }
 
 class HeroChatListener implements Listener {
         private static HeroChatListener instance;
         private ChatLinkPlugin plugin;
         private Map<String, HeroChatChannel> channels = Collections.synchronizedMap(new HashMap<String, HeroChatChannel>());
 
         private HeroChatListener(ChatLinkPlugin plugin) {
                 this.plugin = plugin;
                 plugin.getServer().getPluginManager().registerEvents(this, plugin);
         }
 
         public static HeroChatListener getInstance(ChatLinkPlugin plugin) {
                 if (instance == null || instance.plugin != plugin) instance = new HeroChatListener(plugin);
                 return instance;
         }
 
         public void registerChannel(HeroChatChannel channel) {
                 channels.put(channel.getChannelName(), channel);
         }
 
         public void unregisterChannel(HeroChatChannel channel) {
                 channels.remove(channel.getChannelName());
         }
 
         @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
         public void onChannelChat(ChannelChatEvent event) {
                 HeroChatChannel channel = channels.get(event.getChannel().getName());
                 if (channel != null) channel.onChannelChat(event);
         }
 }
