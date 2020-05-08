 package com.oneofthesevenbillion.ziah.ZCord;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.logging.Level;
 
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.ProxyServer;
 import net.md_5.bungee.api.config.ServerInfo;
 import net.md_5.bungee.api.connection.Connection;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.connection.Server;
 import net.md_5.bungee.api.event.ChatEvent;
 import net.md_5.bungee.api.event.PluginMessageEvent;
 import net.md_5.bungee.api.event.ServerKickEvent;
 import net.md_5.bungee.api.event.ServerSwitchEvent;
 import net.md_5.bungee.api.event.TargetedEvent;
 import net.md_5.bungee.api.plugin.Listener;
 import net.md_5.bungee.event.EventHandler;
 import net.md_5.bungee.protocol.packet.Packet3Chat;
 
 import com.oneofthesevenbillion.ziah.ZCord.network.Packet;
 import com.oneofthesevenbillion.ziah.ZCord.network.PacketReloadAccessories;
 import com.oneofthesevenbillion.ziah.ZCord.utils.ColorUtils;
 import com.oneofthesevenbillion.ziah.ZCord.utils.Utils;
 
 public class ZCordEventListener implements Listener {
     // @EventHandler
     // public void onPlayerJoin(ServerConnectedEvent event) {
     // ((UserConnection) event.getPlayer()).sendPacket(p);
     // }
 
     @EventHandler
     public void onChat(final ChatEvent event) {
         if (event.getMessage().contains("{MMSGSTART}") && event.getMessage().contains("{MMSGEND}")) {
             final String[] messageData = event.getMessage().substring(event.getMessage().indexOf("{MMSGSTART}") + 11, event.getMessage().indexOf("{MMSGEND}")).split(";");
             if (messageData[0].equalsIgnoreCase("accreload")) {
                 for (final ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                     final Packet packet = new PacketReloadAccessories(messageData[1]);
                     final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     final DataOutputStream dos = new DataOutputStream(baos);
                     packet.write(dos);
 
                     player.sendData("ziahsclient", baos.toByteArray());
                 }
             }
             event.setCancelled(true);
             return;
         }
 
         if (!event.getMessage().startsWith("/")) {
             try {
                 Connection sender = null;
                 try {
                     sender = (Connection) Utils.getPrivateValue(TargetedEvent.class, event, "sender");
                 } catch (final Exception e) {
                     ProxyServer.getInstance().getLogger().log(Level.WARNING, "Exception when getting chat message sender to add a group prefix.", e);
                 }
                 Connection receiver = null;
                 try {
                     receiver = (Connection) Utils.getPrivateValue(TargetedEvent.class, event, "receiver");
                 } catch (final Exception e) {
                     ProxyServer.getInstance().getLogger().log(Level.WARNING, "Exception when getting chat message sender to add a group prefix.", e);
                 }
                 if (sender instanceof ProxiedPlayer && receiver instanceof Server) {
                     final ProxiedPlayer player = (ProxiedPlayer) sender;
                     final Class<? extends Object> bungeeperms = Utils.getBungeePermsClass();
                     final Object bungeepermsInstance = Utils.getBungeePermsInstance(bungeeperms);
                     final Object pm = Utils.getPermissionsManager(bungeeperms, bungeepermsInstance);
                     final Object group = Utils.getGroupForPlayer(pm, player);
                     final boolean isGroupDefault = Utils.getGroupIsDefault(group);
                     if (!isGroupDefault) {
                         String groupname = Utils.getGroupName(group);
                         final String prefix = Utils.getGroupPrefix(group);
                         final String suffix = Utils.getGroupSuffix(group);
                         groupname = groupname.substring(0, 1).toUpperCase() + groupname.substring(1);
                         event.setCancelled(true);
                         final String message = "&7[&6Net " + prefix + groupname + suffix + "&7]&r " + event.getMessage();
                         if (!ZCord.instance.zcoreServers.contains(((Server) receiver).getInfo())) {
                             ZCord.instance.pingForZCore(((Server) receiver).getInfo());
                         }
                         if (ZCord.instance.zcoreServers.contains(((Server) receiver).getInfo())) {
                             final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                             final DataOutputStream dos = new DataOutputStream(baos);
                             dos.writeInt(4);
                             dos.writeUTF(((ProxiedPlayer) sender).getName());
                             dos.writeUTF(message);
                             ((Server) receiver).sendData("ziahsclient", baos.toByteArray());
                         } else {
                             ((Server) receiver).unsafe().sendPacket(new Packet3Chat(ColorUtils.removeAllColors(message)));
                         }
                     }
                 }
             } catch (final Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     @EventHandler
     public void onServerKick(final ServerKickEvent event) {
         final ServerInfo server = ProxyServer.getInstance().getServerInfo(ZCord.instance.config.defaultserver);
        if (server != null && !server.equals(event.getPlayer().getServer().getInfo())) {
             event.getPlayer().sendMessage(ChatColor.RED + "You were kicked from the server, redirecting you to the default server, kick reason: " + event.getKickReason());
             event.setCancelServer(server);
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void onPluginMessage(final PluginMessageEvent event) {
         try {
             final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(event.getData()));
             final int packetid = dis.readInt();
             final boolean isZCord = dis.readBoolean();
             if (packetid == 3 && isZCord == true) {
                 final Connection sender = event.getSender();
                 if (sender instanceof Server) {
                     ZCord.instance.zcoreServers.add(((Server) sender).getInfo());
                 }
                 event.setCancelled(true);
             }
         } catch (final IOException e) {
             // Impossible?
         }
         // PacketManager.onPacketData(event.getReceiver(), event.getSender(),
         // event.getData());
     }
 
     @EventHandler
     public void onServerSwitch(final ServerSwitchEvent event) {
         // event.getConnection().unsafe().sendPacket(new
         // PacketCEScoreboardObjective("votes", "Times Voted", (byte) 0));
         // event.getConnection().unsafe().sendPacket(new
         // PacketD0DisplayScoreboard("votes", (byte) 2));
         ZCord.instance.updateVotes();
     }
 }
