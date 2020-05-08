 /*
  * Copyright (C) 2013 MineStar.de 
  * 
  * This file is part of ConAir.
  * 
  * ConAir is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * ConAir is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with ConAir.  If not, see <http://www.gnu.org/licenses/>.
  */
 


 import de.minestar.conair.network.PacketType;
 import de.minestar.conair.network.client.ClientPacketHandler;
 import de.minestar.conair.network.client.packets.ChatPacket;
 import de.minestar.conair.network.packets.NetworkPacket;
 
 public class TestPacketHandler extends ClientPacketHandler {
 
     @Override
     public void handlePacket(NetworkPacket packet) {
         if (packet.getPacketID() == PacketType.getID(ChatPacket.class)) {
 
             System.out.println("Received:" + ((ChatPacket) packet).getMessage());
             return;
         }
     }
 }
