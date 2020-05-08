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
 
 import info.carlwithak.mpxg2.model.effects.algorithms.OneBandDual;
 
 /**
  * Class to parse parameter data for 1-Band (D) effect.
  *
  * @author Carl Green
  */
 public class OneBandDualParser {
 
     public static OneBandDual parse(byte[] effectParameters) {
         OneBandDual oneBandDual = new OneBandDual();
 
         int mix = effectParameters[0] + effectParameters[1] * 16;
         oneBandDual.mix.setValue(mix);
 
         int level = (byte) (effectParameters[2] + effectParameters[3] * 16);
         oneBandDual.level.setValue(level);
 
         int gainLeft = (byte) (effectParameters[4] + effectParameters[5] * 16);
         oneBandDual.gainLeft.setValue(gainLeft);
 
         int fcLeft = 0;
         for (int i = 0; i < 4; i++) {
             fcLeft += (effectParameters[6 + i] * Math.pow(16, i));
         }
         oneBandDual.fcLeft.setValue(fcLeft);
 
         int qLeft = (byte) (effectParameters[10] + effectParameters[11] * 16);
         oneBandDual.qLeft.setValue(qLeft / 10.0);
 
         int modeLeft = (byte) (effectParameters[12] + effectParameters[13] * 16);
         oneBandDual.modeLeft.setValue(modeLeft);
 
         int gainRight = (byte) (effectParameters[14] + effectParameters[15] * 16);
         oneBandDual.gainRight.setValue(gainRight);
 
         int fcRight = 0;
         for (int i = 0; i < 4; i++) {
             fcRight += (effectParameters[16 + i] * Math.pow(16, i));
         }
         oneBandDual.fcRight.setValue(fcRight);
 
         int qRight = (byte) (effectParameters[20] + effectParameters[21] * 16);
         oneBandDual.qRight.setValue(qRight / 10.0);
 
         int modeRight = (byte) (effectParameters[22] + effectParameters[23] * 16);
         oneBandDual.modeRight.setValue(modeRight);
 
         return oneBandDual;
     }
 
 }
