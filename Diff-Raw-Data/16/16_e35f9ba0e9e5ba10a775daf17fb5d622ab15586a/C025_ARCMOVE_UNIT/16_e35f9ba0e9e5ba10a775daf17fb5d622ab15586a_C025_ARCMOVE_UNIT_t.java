 /*
  *  Copyright 2008, 2009, 2010, 2011:
  *   Tobias Fleig (tfg[AT]online[DOT]de),
  *   Michael Haas (mekhar[AT]gmx[DOT]de),
  *   Johannes Kattinger (johanneskattinger[AT]gmx[DOT]de)
  *
  *  - All rights reserved -
  *
  *
  *  This file is part of Centuries of Rage.
  *
  *  Centuries of Rage is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Centuries of Rage is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Centuries of Rage.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package de._13ducks.cor.networks.cmd.client;
 
 import de._13ducks.cor.game.Unit;
 import de._13ducks.cor.game.client.ClientCore.InnerClient;
 import de._13ducks.cor.networks.client.ClientNetController.ClientHandler;
 import de._13ducks.cor.networks.cmd.ClientCommand;
 
 /**
  * Das ArcMove-Weiterleitung an das zuständige Behaviour
  * 
  * Es kommen immer zwei 25-Signale, hier werden sie gepuffert.
  * 
  * @author Tulius <tobifleig@gmail.com>
  */
 public class C025_ARCMOVE_UNIT extends ClientCommand {
 
     private Unit oldUnit;
     private byte[] oldPacket;
 
     @Override
     public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
         if (oldPacket == null) {
             oldUnit = rgi.mapModule.getUnitviaID(rgi.readInt(data, 1));
            oldPacket = data.clone();
         } else {
             // Zusammenbacken:
             byte[] meta = new byte[data.length * 2 - 1];
             meta[0] = oldPacket[0];
             meta[1] = oldPacket[1];
             meta[2] = oldPacket[2];
             meta[3] = oldPacket[3];
             meta[4] = oldPacket[4];
             meta[5] = oldPacket[5];
             meta[6] = oldPacket[6];
             meta[7] = oldPacket[7];
             meta[8] = oldPacket[8];
             meta[9] = oldPacket[9];
             meta[10] = oldPacket[10];
             meta[11] = oldPacket[11];
             meta[12] = oldPacket[12];
             meta[13] = oldPacket[13];
             meta[14] = oldPacket[14];
             meta[15] = oldPacket[15];
             meta[16] = oldPacket[16];
             meta[17] = data[1];
             meta[18] = data[2];
             meta[19] = data[3];
             meta[20] = data[4];
             meta[21] = data[5];
             meta[22] = data[6];
             meta[23] = data[7];
             meta[24] = data[8];
             meta[25] = data[9];
             meta[26] = data[10];
             meta[27] = data[11];
             meta[28] = data[12];
             meta[29] = data[13];
             meta[30] = data[14];
             meta[31] = data[15];
             meta[32] = data[16];
             
             if (oldUnit != null) {
                 oldUnit.getClientManager().gotSignal(meta);
             } else {
                 System.out.println("FixMe: Invalid ID (cmdC25)");
             }
             oldPacket = null;
         }
     }
 }
