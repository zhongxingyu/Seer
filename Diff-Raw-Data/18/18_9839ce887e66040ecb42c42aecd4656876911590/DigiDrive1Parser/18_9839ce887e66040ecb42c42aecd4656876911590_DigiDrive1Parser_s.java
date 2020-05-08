 /*
  *  Copyright (C) 2012 Carl Green
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package info.carlwithak.mpxg2.sysex.effects.algorithms;
 
 import info.carlwithak.mpxg2.model.effects.algorithms.DigiDrive1;
 
 /**
  * Class to parse parameter data for DigiDrive1 effect.
  *
  * @author Carl Green
  */
 public class DigiDrive1Parser {
 
     public static DigiDrive1 parse(byte[] effectParameters) {
         DigiDrive1 digiDrive1 = new DigiDrive1();
 
         int mix = effectParameters[0] + effectParameters[1] * 16;
         digiDrive1.mix.setValue(mix);
 
         int level = effectParameters[2] + effectParameters[3] * 16;
         digiDrive1.level.setValue(level);
 
         int drive = effectParameters[4] + effectParameters[5] * 16;
         digiDrive1.drive.setValue(drive);
 
        int low = effectParameters[6] + effectParameters[7] * 16;
         digiDrive1.low.setValue(low);
 
        int mid = effectParameters[8] + effectParameters[9] * 16;
         digiDrive1.mid.setValue(mid);
 
        int high = effectParameters[10] + effectParameters[11] * 16;
         digiDrive1.high.setValue(high);
 
         return digiDrive1;
     }
 
 }
