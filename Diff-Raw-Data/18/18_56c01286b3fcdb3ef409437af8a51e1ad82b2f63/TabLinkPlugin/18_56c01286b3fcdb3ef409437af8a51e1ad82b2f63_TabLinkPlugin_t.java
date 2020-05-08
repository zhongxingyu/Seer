 /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  * Copyright 2012 StarTux
  *
  * This file is part of TabLink.
  *
  * TabLink is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * TabLink is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with TabLink.  If not, see <http://www.gnu.org/licenses/>.
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
 
 package com.winthier.tablink;
 
 import com.winthier.tablink.message.ClientConnectionMessage;
 import com.winthier.tablink.message.PlayerListRequestMessage;
 import com.winthier.tablink.message.PlayerStatusMessage;
 import com.winthier.tablink.packet.PlayerListRequest;
 import com.winthier.tablink.packet.PlayerStatus;
 import com.winthier.winlink.BukkitRunnable;
 import com.winthier.winlink.ClientConnection;
 import com.winthier.winlink.WinLink;
 import com.winthier.winlink.WinLinkPlugin;
 import com.winthier.winlink.event.ClientConnectEvent;
 import com.winthier.winlink.event.ClientDisconnectEvent;
 import com.winthier.winlink.event.ClientReceivePacketEvent;
 import com.winthier.winlink.event.ServerReceivePacketEvent;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class TabLinkPlugin extends JavaPlugin implements Listener {
         private Map<String, PlayerList> remoteLists = new HashMap<String, PlayerList>();
         private Queue<Object> queue = new ConcurrentLinkedQueue<Object>();
         private BukkitRunnable task;
         private LinkedList<Player> playerList = new LinkedList<Player>(); // for sorting
        private volatile boolean running = true;
 
         @Override
         public void onEnable() {
                 getServer().getPluginManager().registerEvents(this, this);
                 for (ClientConnection connection : WinLinkPlugin.getWinLink().getClientConnections()) {
                         if (connection.isConnected()) {
                                 handleClientConnect(connection);
                         }
                 }
                final TabLinkPlugin plugin = this;
                 task = new BukkitRunnable() {
                         public void run() {
                                 try {
                                        while (running) {
                                                try { Thread.sleep(1000); } catch (InterruptedException ie) {}
                                                new BukkitRunnable() {
                                                        public void run() {
                                                                iter();
                                                        }
                                                }.runTask(plugin);
                                        }
                                 } catch (Exception e) {
                                         e.printStackTrace();
                                 }
                         }
                 };
                task.runTaskAsynchronously(this);
         }
 
         @Override
         public void onDisable() {
                running = false;
                 task.cancel();
                 for (PlayerList list : remoteLists.values()) {
                         list.terminate();
                 }
                 remoteLists.clear();
         }
 
         public void sendMessage(Object msg) {
                 queue.offer(msg);
         }
 
         public void iter() {
                 Object msg;
                 while ((msg = queue.poll()) != null) {
                         if (msg instanceof PlayerStatusMessage) {
                                 // Received a player status update from a remote server
                                 PlayerStatusMessage message = (PlayerStatusMessage)msg;
                                 PlayerList list = remoteLists.get(message.connection.getName());
                                 list.update(message.playerStatus);
                                 list.broadcast(message.playerStatus);
                         } else if (msg instanceof ClientConnectionMessage) {
                                 // Connected to a remote server
                                 ClientConnectionMessage message = (ClientConnectionMessage)msg;
                                 if (message.connected) {
                                         handleClientConnect(message.connection);
                                 } else {
                                         handleClientDisconnect(message.connection);
                                 }
                         } else if (msg instanceof PlayerListRequestMessage) {
                                 // Send status of all online players
                                 PlayerListRequestMessage message = (PlayerListRequestMessage)msg;
                                 for (Player player : getServer().getOnlinePlayers()) {
                                         message.connection.sendPacket(new PlayerStatus(player.getName(), true));
                                 }
                         }
                 }
                 sortIter();
         }
 
         protected void handleClientConnect(ClientConnection connection) {
                 if (!remoteLists.containsKey(connection.getName())) {
                         PlayerList list = new PlayerList(this, connection.getName());
                         remoteLists.put(connection.getName(), list);
                 }
                 connection.sendPacket(new PlayerListRequest());
         }
 
         protected void handleClientDisconnect(ClientConnection connection) {
                 PlayerList list = remoteLists.remove(connection.getName());
                 if (list != null) list.terminate();
         }
 
         public void sortIter() {
                 if (remoteLists.isEmpty()) return;
                 Player currentPlayer;
                 if ((currentPlayer = playerList.poll()) == null) {
                         for (Player player : getServer().getOnlinePlayers()) playerList.add(player);
                         if (playerList.isEmpty()) return;
                 } else {
                         for (PlayerList list : remoteLists.values()) {
                                 for (PlayerStatus status : list.getList()) {
                                         list.send(status, false, currentPlayer);
                                         list.send(status, currentPlayer);
                                 }
                         }
                 }
         }
 
         @Override
         public boolean onCommand(CommandSender sender, Command command, String token, String args[]) {
                 if (args.length == 1 && args[0].equals("list")) {
                         sender.sendMessage("Connected servers:");
                         for (PlayerList list : remoteLists.values()) {
                                 StringBuilder sb = new StringBuilder("- ").append(list.getName()).append(":");
                                 for (PlayerStatus status : list.getList()) sb.append(" ").append(status.name);
                                 sender.sendMessage(sb.toString());
                         }
                 } else if (args.length == 1 && args[0].equals("reload")) {
                         try {
                                 reloadConfiguration();
                                 sender.sendMessage("TabLink Configuration reloaded");
                         } catch (Exception e) {
                                 e.printStackTrace();
                                 sender.sendMessage("An error occured. See console.");
                         }
                 } else if (args.length == 2 && args[0].equals("fakejoin")) {
                         WinLinkPlugin.getWinLink().broadcastPacket(new PlayerStatus(Util.replaceColorCodes(args[1]), true));
                 } else if (args.length == 2 && args[0].equals("fakeleave")) {
                         WinLinkPlugin.getWinLink().broadcastPacket(new PlayerStatus(Util.replaceColorCodes(args[1]), false));
                 } else if (args.length == 2 && args[0].equals("show")) {
                         Util.broadcastStatus(Util.replaceColorCodes(args[1]), true);
                 } else if (args.length == 2 && args[0].equals("hide")) {
                         Util.broadcastStatus(Util.replaceColorCodes(args[1]), false);
                 } else {
                         sender.sendMessage("Usage: /tablink [subcommand] ...");
                         sender.sendMessage("Subcommands: list, reload");
                 }
                 return true;
         }
 
         public void reloadConfiguration() {
                 reloadConfig();
                 for (PlayerList list : remoteLists.values()) list.reloadConfiguration();
         }
 
         @EventHandler(priority = EventPriority.MONITOR)
         public void onPlayerJoin(PlayerJoinEvent event) {
                 WinLinkPlugin.getWinLink().broadcastPacket(new PlayerStatus(event.getPlayer().getName(), true));
                 for (PlayerList list : remoteLists.values()) list.send(event.getPlayer());
         }
 
         @EventHandler(priority = EventPriority.MONITOR)
         public void onPlayerQuit(PlayerQuitEvent event) {
                 WinLinkPlugin.getWinLink().broadcastPacket(new PlayerStatus(event.getPlayer().getName(), false));
         }
 
         @EventHandler(priority = EventPriority.MONITOR)
         public void onPlayerKick(PlayerKickEvent event) {
                 WinLinkPlugin.getWinLink().broadcastPacket(new PlayerStatus(event.getPlayer().getName(), false));
         }
 
         @EventHandler(priority = EventPriority.MONITOR)
         public void onClientConnect(ClientConnectEvent event) {
                 sendMessage(new ClientConnectionMessage(event.getConnection(), true));
         }
 
         @EventHandler(priority = EventPriority.MONITOR)
         public void onClientDisconnect(ClientDisconnectEvent event) {
                 sendMessage(new ClientConnectionMessage(event.getConnection(), false));
         }
 
         @EventHandler(priority = EventPriority.MONITOR)
         public void onClientReceivePacket(ClientReceivePacketEvent event) {
                 if (event.getPacket() instanceof PlayerStatus) {
                         sendMessage(new PlayerStatusMessage(event.getConnection(), (PlayerStatus)event.getPacket()));
                 }
         }
 
         @EventHandler(priority = EventPriority.MONITOR)
         public void onServerReceivePacket(ServerReceivePacketEvent event) {
                 if (event.getPacket() instanceof PlayerListRequest) {
                         sendMessage(new PlayerListRequestMessage(event.getConnection()));
                 }
         }
 }
