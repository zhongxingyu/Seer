 /*
  * This file is part of VanillaClassic.
  *
  * Copyright (c) 2012, Greatman <http://www.github.com/greatman/>
  * VanillaClassic is licensed under the SpoutDev License Version 1.
  *
  * VanillaClassic is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * In addition, 180 days after any changes are published, you can use the
  * software, incorporating those changes, under the terms of the MIT license,
  * as described in the SpoutDev License Version 1.
  *
  * VanillaClassic is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License,
  * the MIT license and the SpoutDev License Version 1 along with this program.
  * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
  * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
  * including the MIT license.
  */
 package com.greatmancode.vanillaclassic.protocol.handler;
 
 import org.spout.api.geo.cuboid.Block;
import org.spout.api.material.BlockMaterial;
 import org.spout.api.material.Material;
 import org.spout.api.protocol.MessageHandler;
 import org.spout.api.protocol.Session;
 
 import com.greatmancode.vanillaclassic.material.VanillaClassicMaterials;
 import com.greatmancode.vanillaclassic.protocol.msg.SetBlockClientMessage;
 
 public final class SetBlockHandler extends MessageHandler<SetBlockClientMessage> {
 	@Override
 	public void handleServer(Session session, SetBlockClientMessage message) {
 		if (session.getPlayer() == null) {
 			session.disconnect("Invalid player!");
 		}
 		
 		short x = message.getX();
 		short y = message.getY();
 		short z = message.getZ();
 		//Adding a block
 		if (message.getMode() == 0x00) {
 			Block block = session.getPlayer().getWorld().getBlock(x, y, z);
 			if (block.getMaterial().equals(VanillaClassicMaterials.AIR)) {
 				//We check if it's a valid block ID.
 				Material mat = VanillaClassicMaterials.getMaterialFromID(message.getBlockType());
 				if (mat != null) {
 					//Everything is good! Let's place the block.
					block.setMaterial((BlockMaterial) mat);
 				}
 			}
 		}
 		//Removing a block
 		else if (message.getMode() == 0x00) {
 			Block block = session.getPlayer().getWorld().getBlock(x, y, z);
 			//We don't want to remove air from the map..
 			if (!block.getMaterial().equals(VanillaClassicMaterials.AIR)) {
 				//TODO: Probably need to use onPlacement for this..
 				block.setMaterial(VanillaClassicMaterials.AIR);
 			}
 		}
 	}
 
 }
