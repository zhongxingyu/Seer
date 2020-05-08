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
 
 package com.winthier.chatlink;
 
 import com.winthier.chatlink.channel.AsyncChatChannel;
 import com.winthier.chatlink.channel.Channel;
 import com.winthier.chatlink.channel.HeroChatChannel;
 import com.winthier.chatlink.channel.SyncChatChannel;
 import com.winthier.chatlink.ignore.HeroChatIgnore;
 import com.winthier.chatlink.ignore.IgnoreBackend;
 import com.winthier.chatlink.ignore.NullIgnore;
 import com.winthier.chatlink.ignore.WinthierIgnore;
 import com.winthier.chatlink.packet.ChatPacket;
 import com.winthier.chatlink.packet.WhisperAckPacket;
 import com.winthier.chatlink.packet.WhisperPacket;
 import com.winthier.winlink.BukkitRunnable;
 import com.winthier.winlink.ClientConnection;
 import com.winthier.winlink.ServerConnection;
 import com.winthier.winlink.WinLinkPlugin;
 import com.winthier.winlink.event.ClientReceivePacketEvent;
 import com.winthier.winlink.event.ServerReceivePacketEvent;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ChatLinkPlugin extends JavaPlugin implements Listener {
         private Map<String, Channel> channels = Collections.synchronizedMap(new HashMap<String, Channel>());
         private BukkitRunnable task;
         private WhisperCommand whisperCommand;
         public IgnoreBackend ignore;
         public net.milkbowl.vault.chat.Chat vaultChat;
 
         @Override
         public void onEnable() {
                 setupChat();
                 if (getServer().getPluginManager().getPlugin("Herochat") != null) ignore = new HeroChatIgnore();
                 else if (getServer().getPluginManager().getPlugin("Winthier") != null) ignore = new WinthierIgnore();
                 else ignore = new NullIgnore();
                 whisperCommand = new WhisperCommand(this);
                 getConfig().options().copyDefaults(true);
                 loadConfiguration();
                 saveConfig();
                 getServer().getPluginManager().registerEvents(this, this);
         }
 
         private boolean setupChat() {
                 RegisteredServiceProvider<net.milkbowl.vault.chat.Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
                 if (chatProvider != null) {
                         vaultChat = chatProvider.getProvider();
                 }
                 return (vaultChat != null);
         }
 
        public net.milkbowl.vault.chat.Chat getChat() {
                 return vaultChat;
         }
 
         @Override
         public void onDisable() {
                 clearChannels();
                 ignore = null;
         }
 
         private void clearChannels() {
                 synchronized(channels) {
                         for (Channel channel : channels.values()) {
                                 channel.disable();
                         }
                 }
                 channels.clear();
         }
 
         @Override
         public boolean onCommand(CommandSender sender, Command command, String token, String args[]) {
                 if (args.length == 1 && args[0].equals("reload")) {
                         try {
                                 clearChannels();
                                 reloadConfig();
                                 loadConfiguration();
                                 sender.sendMessage("Configuration reloaded.");
                         } catch (Exception e) {
                                 e.printStackTrace();
                                 sender.sendMessage("An error occured. See console.");
                         }
                 } else {
                         sender.sendMessage("Usage: /chatlink [subcommand] ...");
                         sender.sendMessage("Subcommands: reload");
                 }
                 return true;
         }
 
         @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
         public void onClientReceivePacket(ClientReceivePacketEvent event) {
                 if (event.getPacket() instanceof ChatPacket) {
                         ChatPacket packet = (ChatPacket)event.getPacket();
                         Channel channel = channels.get(packet.channel);
                         if (channel == null) {
                                 getLogger().warning("Channel not found: `" + packet.channel + "'");
                                 return;
                         }
                         channel.sendChat(packet.sender, event.getConnection().getName(), packet.message);
                 } else if (event.getPacket() instanceof WhisperPacket) {
                         final WhisperPacket packet = (WhisperPacket)event.getPacket();
                         final ClientConnection connection = event.getConnection();
                         new BukkitRunnable() {
                                 public void run() {
                                         Player player = getServer().getPlayer(packet.recipient);
                                         if (player == null) return;
                                         if (whisperCommand.msgRecipient(player, packet.sender, connection.getName(), packet.message)) {
                                                 connection.sendPacket(new WhisperAckPacket(player.getName(), packet.sender, packet.message));
                                         }
                                 }
                         }.runTask(this);
                 }
         }
         
         @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
         public void onServerReceivePacket(ServerReceivePacketEvent event) {
                 if (event.getPacket() instanceof WhisperAckPacket) {
                         final WhisperAckPacket packet = (WhisperAckPacket)event.getPacket();
                         final ServerConnection connection = event.getConnection();
                         new BukkitRunnable() {
                                 public void run() {
                                         try {
                                                 Player player = getServer().getPlayer(packet.sender);
                                                 if (player == null) return;
                                                 whisperCommand.msgSender(player, packet.recipient, connection.getName(), packet.message);
                                         } catch (Exception e) {
                                                 e.printStackTrace();
                                         }
                                 }
                         }.runTask(this);
                 }
         }
 
         public void loadConfiguration() {
                 synchronized (channels) { for (Channel channel : channels.values()) channel.disable(); }
                 whisperCommand.loadConfiguration();
                 channels.clear();
                 ConfigurationSection channelsSection = getConfig().getConfigurationSection("channels");
                 if (channelsSection == null) {
                         channelsSection = getConfig().createSection("channels");
                 }
                 for (String name : channelsSection.getKeys(false)) {
                         ConfigurationSection channelSection = channelsSection.getConfigurationSection(name);
                         String channelType = channelSection.getString("Type", "default");
                         Channel channel;
                         if (channelType.equalsIgnoreCase("default")) {
                                 channel = new SyncChatChannel(this, name);
                         } else if (channelType.equalsIgnoreCase("sync")) {
                                 channel = new SyncChatChannel(this, name);
                         } else if (channelType.equalsIgnoreCase("async")) {
                                 channel = new AsyncChatChannel(this, name);
                         } else if (channelType.equalsIgnoreCase("HeroChat")) {
                                 channel = new HeroChatChannel(this, name);
                         } else {
                                 getLogger().warning("config.yml: channel `" + name + "': invalid type: `" + channelType + "'");
                                 continue;
                         }
                         channel.enable();
                         channel.loadConfiguration(channelSection);
                         channels.put(name, channel);
                 }
         }
 }
