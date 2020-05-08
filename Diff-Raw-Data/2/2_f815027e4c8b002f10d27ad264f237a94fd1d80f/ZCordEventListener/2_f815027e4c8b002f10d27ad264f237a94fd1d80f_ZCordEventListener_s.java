 package com.oneofthesevenbillion.ziah.ZCord;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.logging.Level;
 
 import com.oneofthesevenbillion.ziah.ZCord.network.Packet;
 import com.oneofthesevenbillion.ziah.ZCord.network.PacketReloadAccessories;
 import com.oneofthesevenbillion.ziah.ZCord.utils.ColorUtils;
 import com.oneofthesevenbillion.ziah.ZCord.utils.Utils;
 
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.ProxyServer;
 import net.md_5.bungee.api.config.ServerInfo;
 import net.md_5.bungee.api.connection.Connection;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.connection.Server;
 import net.md_5.bungee.api.event.ChatEvent;
 import net.md_5.bungee.api.event.PluginMessageEvent;
 import net.md_5.bungee.api.event.ServerKickEvent;
 import net.md_5.bungee.api.event.TargetedEvent;
 import net.md_5.bungee.api.plugin.Listener;
 import net.md_5.bungee.event.EventHandler;
 import net.md_5.bungee.protocol.packet.Packet3Chat;
 
 public class ZCordEventListener implements Listener {
 	//@EventHandler
 	//public void onPlayerJoin(ServerConnectedEvent event) {
 	//	((UserConnection) event.getPlayer()).sendPacket(p);
 	//}
 
 	@EventHandler
 	public void onChat(ChatEvent event) {
 		if (event.getMessage().contains("{MMSGSTART}") && event.getMessage().contains("{MMSGEND}")) {
 			String[] messageData = event.getMessage().substring(event.getMessage().indexOf("{MMSGSTART}") + 11, event.getMessage().indexOf("{MMSGEND}")).split(";");
         	if (messageData[0].equalsIgnoreCase("accreload")) {
 	        	for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
 		        	Packet packet = new PacketReloadAccessories(messageData[1]);
 		        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		        	DataOutputStream dos = new DataOutputStream(baos);
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
 				} catch (Exception e) {
 					ProxyServer.getInstance().getLogger().log(Level.WARNING, "Exception when getting chat message sender to add a group prefix.", e);
 				}
 				Connection receiver = null;
 				try {
 					receiver = (Connection) Utils.getPrivateValue(TargetedEvent.class, event, "receiver");
 				} catch (Exception e) {
 					ProxyServer.getInstance().getLogger().log(Level.WARNING, "Exception when getting chat message sender to add a group prefix.", e);
 				}
 				if (sender instanceof ProxiedPlayer && receiver instanceof Server) {
 					ProxiedPlayer player = (ProxiedPlayer) sender;
 					Class<?extends Object> bungeeperms = Class.forName("net.alpenblock.bungeeperms.BungeePerms");
 					Object bungeepermsInstance = bungeeperms.getDeclaredMethod("getInstance").invoke(null);
 					Object pm = bungeeperms.getDeclaredMethod("getPermissionsManager").invoke(bungeepermsInstance);
 					Object group = pm.getClass().getDeclaredMethod("getMainGroup", Class.forName("net.alpenblock.bungeeperms.Player")).invoke(pm, pm.getClass().getDeclaredMethod("getUser", String.class).invoke(pm, player.getName()));
 					String prefix = (String) group.getClass().getDeclaredMethod("getPrefix").invoke(group);
 					String suffix = (String) group.getClass().getDeclaredMethod("getSuffix").invoke(group);
 					String groupname = (String) group.getClass().getDeclaredMethod("getName").invoke(group);
 					groupname = groupname.substring(0, 1).toUpperCase() + groupname.substring(1);
 					event.setCancelled(true);
 					String message = "&7[&6Net " + prefix + groupname + suffix + "&7]&r " + event.getMessage();
					ZCord.instance.pingForZCore(((Server) receiver).getInfo());
 					if (ZCord.instance.zcoreServers.contains(((Server) receiver).getInfo())) {
 						ByteArrayOutputStream baos = new ByteArrayOutputStream();
 						DataOutputStream dos = new DataOutputStream(baos);
 						dos.writeInt(4);
 						dos.writeUTF(((ProxiedPlayer) sender).getName());
 						dos.writeUTF(message);
 						((Server) receiver).sendData("ziahsclient", baos.toByteArray());
 					}else{
 						((Server) receiver).unsafe().sendPacket(new Packet3Chat(ColorUtils.removeAllColors(message)));
 					}
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@EventHandler
 	public void onServerKick(ServerKickEvent event) {
 		ServerInfo server = ProxyServer.getInstance().getServerInfo(ZCord.instance.config.defaultserver);
 		if (server != null) {
 			event.getPlayer().sendMessage(ChatColor.RED + "You were kicked from the server, redirecting you to the default server, kick reason: " + event.getKickReason());
 			event.setCancelServer(server);
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void onPluginMessage(PluginMessageEvent event) {
 		try {
 			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(event.getData()));
 			int packetid = dis.readInt();
 			boolean isZCord = dis.readBoolean();
 			if (packetid == 3 && isZCord == true) {
 				Connection sender = event.getSender();
 				if (sender instanceof Server) {
 					ZCord.instance.zcoreServers.add(((Server) sender).getInfo());
 				}
 				event.setCancelled(true);
 			}
 		} catch(IOException e) {
 			// Impossible?
 		}
 		//PacketManager.onPacketData(event.getReceiver(), event.getSender(), event.getData());
 	}
 }
