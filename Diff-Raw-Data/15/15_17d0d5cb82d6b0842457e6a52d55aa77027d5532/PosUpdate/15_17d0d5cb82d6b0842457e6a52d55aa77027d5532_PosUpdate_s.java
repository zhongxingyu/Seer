 /*******************************************************************************
 * Copyright (c) 2012 MCForge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
 package net.mcforge.networking.packets.minecraft;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 
 import net.mcforge.API.ClassicExtension;
 import net.mcforge.API.player.PlayerMoveEvent;
 import net.mcforge.iomodel.Player;
 import net.mcforge.networking.IOClient;
 import net.mcforge.networking.packets.Packet;
 import net.mcforge.networking.packets.PacketManager;
 import net.mcforge.networking.packets.PacketType;
 import net.mcforge.server.Server;
 
 @ClassicExtension(extName = "HeldBlock")
 public class PosUpdate extends Packet {
 
 	public PosUpdate(String name, byte ID, PacketManager parent,
 			PacketType packetType) {
 		super(name, ID, parent, packetType);
 	}
 	
 	public PosUpdate(PacketManager pm) {
		super("PosUpdate", (byte)0x08, pm, PacketType.Client_to_Server);
		this.length = 9;
 	}
 
 	@Override
 	public void Write(IOClient player, Server server) {
 	}
 
 	@Override
 	public void Handle(byte[] message, Server server, IOClient p) {
 		Player player;
 		if (p instanceof Player) {
 			player = (Player)p;
 		}
 		else
 			return;
 		ByteBuffer bb = ByteBuffer.allocate(9);
 		bb.put(message);
 		byte ID = bb.get(0);
 		short X = bb.getShort(1);
 		short Y = bb.getShort(3);
 		short Z = bb.getShort(5);
 		player.yaw = bb.get(7);
 		player.pitch = bb.get(8);
 		if (player.hasExtension(this))
 			player.setHoldingBlock(ID);
 		player.setX(X);
 		player.setY(Y);
 		player.setZ(Z);
 		PlayerMoveEvent event = new PlayerMoveEvent(player, X, Y, Z);
 		server.getEventSystem().callEvent(event);
 		if (event.isCancelled()) {
 			player.setPos(player.oldX, player.oldY, player.oldZ, player.oldyaw, player.oldpitch);
 			return;
 		}
 		try {
 			player.UpdatePos();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 }
 
