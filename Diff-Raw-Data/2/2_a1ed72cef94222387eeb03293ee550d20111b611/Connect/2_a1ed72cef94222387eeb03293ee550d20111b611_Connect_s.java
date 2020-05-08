 /*******************************************************************************
  * Copyright (c) 2012 GamezGalaxy.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 package com.gamezgalaxy.GGS.networking.packets.minecraft;
 
 import java.io.*;
 
 import com.gamezgalaxy.GGS.API.player.PlayerConnectEvent;
 import com.gamezgalaxy.GGS.API.player.PlayerLoginEvent;
 import com.gamezgalaxy.GGS.iomodel.Player;
 import com.gamezgalaxy.GGS.networking.IOClient;
 import com.gamezgalaxy.GGS.networking.packets.Packet;
 import com.gamezgalaxy.GGS.networking.packets.PacketManager;
 import com.gamezgalaxy.GGS.networking.packets.PacketType;
 import com.gamezgalaxy.GGS.server.Server;
 import com.gamezgalaxy.GGS.system.BanHandler;
 
 public class Connect extends Packet {
 
 	public Connect(String name, byte ID, PacketManager parent, PacketType packetType) {
 		super(name, ID, parent, packetType);
 	}
 	public Connect(PacketManager pm) {
 		super("Player Connect", (byte)0x00, pm, PacketType.Client_to_Server);
 		this.length = 130;
 	}
 
 	@Override
 	public void Write(IOClient player, Server server) {
 		// TODO Auto-generated method stub
 
 	}
 	@Override
 	public void Handle(byte[] message, Server server, IOClient p) {
 		Player player;
 		if (p instanceof Player) {
 			player = (Player)p;
 		}
 		else
 			return;
 		try {
 			byte version = message[0];
 			byte[] name = new byte[64];
 			for (int i = 1; i < 64; i++)
 				name[i - 1] = message[i];
 			player.username = new String(name, "US-ASCII").trim();
 			name = new byte[64];
 			for (int i = 65; i < 65 + 32; i++)
 				name[i - 65] = message[i];
 			player.mppass = new String(name, "US-ASCII").trim();
 			name = null;
 			PlayerConnectEvent connect = new PlayerConnectEvent(player);
 			server.getEventSystem().callEvent(connect);
 			if (player.VerifyLogin() && !connect.isCancelled()) {
 				if (BanHandler.isBanned(player.username))
 				{
 					player.Kick("You are banned!");
 				} else {
 					server.players.add(player);
 
 					player.Login();
 					PlayerLoginEvent login = new PlayerLoginEvent(player);
 					server.getEventSystem().callEvent(login);
					player.ClientType = message[130];
 				}
 			}
 			else {
 				if (connect.getKickMessage().equals(""))
 					player.Kick("Invalid Login!");
 				else
 					player.Kick(connect.getKickMessage());
 				return;
 			}
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
