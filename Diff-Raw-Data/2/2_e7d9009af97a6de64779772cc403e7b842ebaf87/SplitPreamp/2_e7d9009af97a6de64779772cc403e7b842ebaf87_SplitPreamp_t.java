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
 
 package info.carlwithak.mpxg2.model.effects.algorithms;
 
 import info.carlwithak.mpxg2.model.effects.Gain;
 import info.carlwithak.mpxg2.model.parameters.GenericValue;
 import info.carlwithak.mpxg2.model.parameters.Parameter;
 
 /**
  * Class for SplitPreamp parameters.
  *
  * @author Carl Green
  */
 public class SplitPreamp extends Gain {
     private static final String NAME = "SplitPreamp";
 
     public final GenericValue<Integer> lo = new GenericValue<Integer>("Lo", "dB", -25, 25);
     public final GenericValue<Integer> mid = new GenericValue<Integer>("Mid", "dB", -25, 25);
     public final GenericValue<Integer> hi = new GenericValue<Integer>("Hi", "dB", 0, 50);
    public final GenericValue<Integer> inLevel = new GenericValue<Integer>("InLvl", "dB", -64, 0);
     public final GenericValue<Integer> loCut = new GenericValue<Integer>("LoCut", "", 0, 20);
     public final GenericValue<Integer> feel = new GenericValue<Integer>("Feel", "", 0, 64);
     public final GenericValue<Integer> drive = new GenericValue<Integer>("Drive", "", 0, 60);
     public final GenericValue<Integer> tone = new GenericValue<Integer>("Tone", "", 0, 35);
     public final GenericValue<Integer> bass = new GenericValue<Integer>("Bass", "dB", -25, 25);
     public final GenericValue<Integer> treble = new GenericValue<Integer>("Trebl", "dB", -25, 25);
     public final GenericValue<Integer> level = new GenericValue<Integer>("Level", "dB", 0, 64);
 
     @Override
     public String getName() {
         return NAME;
     }
 
     @Override
     public Parameter getParameter(final int parameterIndex) {
         Parameter parameter;
         switch (parameterIndex) {
             case 0:
                 parameter = lo;
                 break;
             case 1:
                 parameter = mid;
                 break;
             case 2:
                 parameter = hi;
                 break;
             case 3:
                 parameter = inLevel;
                 break;
             case 4:
                 parameter = loCut;
                 break;
             case 5:
                 parameter = feel;
                 break;
             case 6:
                 parameter = drive;
                 break;
             case 7:
                 parameter = tone;
                 break;
             case 8:
                 parameter = bass;
                 break;
             case 9:
                 parameter = treble;
                 break;
             case 10:
                 parameter = level;
                 break;
             default:
                 parameter = null;
         }
         return parameter;
     }
 
 }
