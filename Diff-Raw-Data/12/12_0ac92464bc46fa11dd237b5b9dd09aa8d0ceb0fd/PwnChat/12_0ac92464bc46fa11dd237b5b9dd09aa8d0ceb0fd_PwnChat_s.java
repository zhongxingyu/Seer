 /*
  * PwnChat -- A Bukkit/Spigot plugin for multi-channel cross-server (via bungeecord) chat.
  * Copyright (c) 2013 Pwn9.com. Sage905 <ptoal@takeflight.ca>
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  */
 
 package com.pwn9.pwnchat;
 
 import com.pwn9.pwnchat.commands.pchat;
 import com.pwn9.pwnchat.config.PwnChatConfig;
 import com.pwn9.pwnchat.listeners.ChatListener;
 import com.pwn9.pwnchat.listeners.PlayerJoinListener;
 import com.pwn9.pwnchat.listeners.PlayerQuitListener;
 import com.pwn9.pwnchat.tasks.PluginMessageTask;
 import com.pwn9.pwnchat.utils.LogManager;
 import net.milkbowl.vault.chat.Chat;
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.entity.Player;
 import org.bukkit.event.HandlerList;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.messaging.PluginMessageListener;
 
 import java.io.*;
 
 public class PwnChat extends JavaPlugin implements PluginMessageListener {
 
 	private Chat chat = null;
     private Permission perms = null;
 	private PwnChatConfig config;
     private LogManager logManager;
 
     public static final String PREFIX = ChatColor.YELLOW + "[PwnChat]";
 
     @Override
     public void onLoad() {
         LogManager.getInstance(getLogger(), getDataFolder());
     }
 
     @Override
     public void reloadConfig() {
         unregisterListeners();
         super.reloadConfig();
         ChannelManager.getInstance().setupChannels(this, config);
         setupLog();
         registerListeners();
 
     }
 
     public void onEnable() {
 
         try {
             config = new PwnChatConfig(this);
             config.init();
         } catch (InvalidConfigurationException ex) {
             getLogger().severe("Failed to load configuration. " + ex.getMessage());
             getServer().getPluginManager().disablePlugin(this);
             return;
         }
 
         setupLog();
 
         setupChat();
 
         setupPerms();
 
         setupBungeeChannels();
 
         ChannelManager.getInstance().setupChannels(this, config);
 
         getCommand("pchat").setExecutor(new pchat(this));
 
         registerListeners();
 
 //		if (config.Settings_FactionServer) {
 //			ListenerManager.getInstance().registerListener(new FactionChatListener(this), this);
 //		} else {
 //            ListenerManager.getInstance().registerListener(new ChatListener(this), this);
 //		}
 
 	}
     //TODO: See what happens when Pwnfilter gets disabled while we're running!
 
     public void setupPwnFilter() {
     if (getServer().getPluginManager().getPlugin("PwnFilter") != null) {
 
     }
     getLogger().info("PwnFilter Dependency not found.  Disabling chat filtering.");
     }
 
     private void registerListeners() {
         new PlayerJoinListener(this);
         new PlayerQuitListener(this);
 
         ChatListener cl = new ChatListener(this);
     }
 
     private void unregisterListeners() {
         HandlerList.unregisterAll(this);
     }
 
     private void setupLog() {
         logManager = LogManager.getInstance();
         logManager.stop();
         LogManager.DebugModes dm;
         try {
             dm = LogManager.DebugModes.valueOf(config.Settings_debug);
         } catch (IllegalArgumentException ex ) {
             dm = LogManager.DebugModes.off;
         }
         logManager.setDebugMode(dm);
         logManager.start("pwnchat.log");
     }
 
     private boolean setupChat() {
 		RegisteredServiceProvider<Chat> chatProvider = getServer()
 				.getServicesManager().getRegistration(
 						net.milkbowl.vault.chat.Chat.class);
 		if (chatProvider != null) {
 			chat = chatProvider.getProvider();
 		}
 
 		return (chat != null);
 	}
 
     private boolean setupPerms() {
         RegisteredServiceProvider<Permission> permsProvider = getServer()
                 .getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
         if (permsProvider != null) {
             perms = permsProvider.getProvider();
         }
 
         return (perms != null);
     }
 
     private void setupBungeeChannels() {
         if (config.Settings_BungeeCord == true) {
             getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
             getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
         } else {
             getServer().getMessenger().unregisterIncomingPluginChannel(this,"BungeeCord", this);
             getServer().getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
         }
     }
 
 	public Chat getChat() {
 		return this.chat;
 	}
 
     public Permission getPerms() {
         return this.perms;
     }
 
 	private void disable() {
 		Bukkit.getPluginManager().disablePlugin(this);
 
 	}
 
     @Override
     public void onPluginMessageReceived(String dataChannel, Player player, byte[] message) {
         String serverName;
 
         if (!dataChannel.equals("BungeeCord")) {
             return;
         }
         LogManager.getInstance().debugMedium("Received message from bungeecord.");
 
         DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
         try {
             String subChannel = in.readUTF();
             if (subChannel.equals("PwnChat")) {
                 short len = in.readShort();
                 byte[] msgbytes = new byte[len];
                 in.readFully(msgbytes);
 
                 DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
 
                 String command = msgin.readUTF();
 
                 if (command.equals("ChannelMessage")) {
 
                     final String channelName = msgin.readUTF(); // Get the channel name.
                     final String playerName = msgin.readUTF();
                     final String format = msgin.readUTF();
                     final String chatMessage = msgin.readUTF();
 
                     final Channel chatChannel = ChannelManager.getInstance().getChannel(channelName);
                     if (chatChannel == null) return; // Not for us.
                     LogManager.getInstance().debugMedium(String.format("[%s] <%s> (<%s>) %s", channelName, playerName, format, chatMessage));
 
                    chatChannel.sendMessage(this, playerName, format, chatMessage);
 
                 }
             }
         } catch (IOException ex) {
             getLogger().warning("IO Exception: " + ex);
         }
     }
 
      public void sendToChannel(Player p, Channel c, String format, String message) {
 
          if (!getServer().getMessenger().isOutgoingChannelRegistered(this,"BungeeCord")) return;
 
          ByteArrayOutputStream b = new ByteArrayOutputStream();
          DataOutputStream out = new DataOutputStream(b);
 
          try {
              out.writeUTF("Forward");
              out.writeUTF("ALL");
              out.writeUTF("PwnChat");
              ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
              DataOutputStream msgout = new DataOutputStream(msgbytes);
              msgout.writeUTF("ChannelMessage");
              msgout.writeUTF(c.getName());
              msgout.writeUTF(p.getDisplayName());
              msgout.writeUTF(format);
              msgout.writeUTF(message);
              out.writeShort(msgbytes.toByteArray().length);
              out.write(msgbytes.toByteArray());
          } catch ( IOException ex) {
              getLogger().warning("Caught exception when trying to send: " + ex.getMessage());
          }
          Bukkit.getScheduler().runTask(this,new PluginMessageTask(this, p, b));
      }
 
 }
