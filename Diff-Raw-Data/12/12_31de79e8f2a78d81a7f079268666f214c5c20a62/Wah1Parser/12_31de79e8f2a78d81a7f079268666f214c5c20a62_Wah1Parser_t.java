 /*
  *  Copyright (C) 2011 Carl Green
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
 
 import info.carlwithak.mpxg2.model.effects.algorithms.Wah1;
 
 /**
  * Class to parse parameter data for Wah 1 effect.
  *
  * @author Carl Green
  */
 public class Wah1Parser {
 
     public static Wah1 parse(byte[] effectParameters) {
         Wah1 wah1 = new Wah1();
 
        int mix = effectParameters[0] + effectParameters[1] * 16;
         wah1.setMix(mix);
 
        int level = effectParameters[2] + effectParameters[3] * 16;
         wah1.setLevel(level);
 
        int sweep = effectParameters[4] + effectParameters[5] * 16;
        wah1.setSweep(sweep);

         int bass = effectParameters[6] + effectParameters[7] * 16;
         wah1.setBass(bass);
 
         int type = effectParameters[8] + effectParameters[9] * 16;
         wah1.setType(type);
 
         int response = effectParameters[10] + effectParameters[11] * 16;
         wah1.setResponse(response);
 
         int gain = effectParameters[12] + effectParameters[13] * 16;
         wah1.setGain(gain);
 
         return wah1;
     }
 }
