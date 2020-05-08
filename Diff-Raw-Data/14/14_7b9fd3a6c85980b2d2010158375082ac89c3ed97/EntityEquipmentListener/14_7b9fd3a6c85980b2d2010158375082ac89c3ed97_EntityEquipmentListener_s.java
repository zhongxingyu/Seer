 /* FakeHat - Bukkit plugin
  * Copyright (C) 2012 meiskam
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package org.shininet.bukkit.fakehat;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 //import net.minecraft.server.NBTTagCompound;
 
 import org.bukkit.Server;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.comphenix.protocol.ProtocolManager;
 import com.comphenix.protocol.events.ConnectionSide;
 import com.comphenix.protocol.events.PacketAdapter;
 import com.comphenix.protocol.events.PacketContainer;
 import com.comphenix.protocol.events.PacketEvent;
 import com.comphenix.protocol.events.PacketListener;
 import com.comphenix.protocol.reflect.FieldAccessException;
 
 @SuppressWarnings("unused")
 public class EntityEquipmentListener {
 
 	private final Server server;
 	private final Logger logger;
 
 	public EntityEquipmentListener(Server server, Logger logger) {
 		this.server = server;
 		this.logger = logger;
 	}
 	
 	public void addListener(ProtocolManager protocolManager, JavaPlugin plugin) {
 
 		protocolManager.addPacketListener(new PacketAdapter(plugin, ConnectionSide.SERVER_SIDE, 0x05) {
 			@Override
 			public void onPacketSending(PacketEvent event) {
 				PacketContainer packet = event.getPacket();
 				
 				try {
 					
 					if (event.getPacketID() == 0x05) {
 						if (packet.getSpecificModifier(int.class).size() == 2 && packet.getItemModifier().size() == 1) {
 							int id = packet.getSpecificModifier(int.class).read(0);
 							int slot = packet.getSpecificModifier(int.class).read(1);
 							if (slot == 4) {
 								ItemStack item = packet.getItemModifier().read(0);

								logger.info("id: "+id+" .. slot: "+slot+" .. item: "+item.toString());
								
								item.setTypeId(((FakeHat)plugin).hat);
								item.setDurability((short)0);
								// = new ItemStack(((FakeHat)plugin).hat);
								
 							}
 						}
 					}
 				
 				} catch (FieldAccessException e) {
 					logger.log(Level.SEVERE, "Couldn't access field.", e);
 				}
 			}
 		});
 	}
 }
