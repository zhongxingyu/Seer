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
 
 import info.carlwithak.mpxg2.model.effects.Eq;
import info.carlwithak.mpxg2.model.parameters.EqModeValue;
 import info.carlwithak.mpxg2.model.parameters.GenericValue;
 import info.carlwithak.mpxg2.model.parameters.Parameter;
 
 /**
  * Class for 3-Band (M) parameters.
  *
  * @author Carl Green
  */
 public class ThreeBandMono extends Eq {
     private static final String NAME = "3-Band (M)";
 
    public final GenericValue<Integer> gain1 = new GenericValue<Integer>("Gain1", "dB", -72, 24);
    public final GenericValue<Integer> gain2 = new GenericValue<Integer>("Gain2", "dB", -72, 24);
    public final GenericValue<Integer> gain3 = new GenericValue<Integer>("Gain3", "dB", -72, 24);
    public final GenericValue<Integer> fc1 = new GenericValue<Integer>("Fc 1", "Hz", 20, 20000);
    public final GenericValue<Integer> fc2 = new GenericValue<Integer>("Fc 2", "Hz", 20, 20000);
    public final GenericValue<Integer> fc3 = new GenericValue<Integer>("Fc 3", "Hz", 20, 20000);
    public final GenericValue<Double> q1 = new GenericValue<Double>("Q 1", "", 0.1, 10.0);
    public final GenericValue<Double> q2 = new GenericValue<Double>("Q 2", "", 0.1, 10.0);
    public final GenericValue<Double> q3 = new GenericValue<Double>("Q 3", "", 0.1, 10.0);
    public final EqModeValue mode1 = new EqModeValue("Mode1");
    public final EqModeValue mode2 = new EqModeValue("Mode2");
    public final EqModeValue mode3 = new EqModeValue("Mode3");
 
     @Override
     public String getName() {
         return NAME;
     }
 
     @Override
     public Parameter getParameter(final int parameterIndex) {
         Parameter parameter;
         switch (parameterIndex) {
             case 0:
             case 1:
                 parameter = super.getParameter(parameterIndex);
                 break;
             case 2:
                 parameter = gain1;
                 break;
             case 3:
                 parameter = gain2;
                 break;
             case 4:
                 parameter = gain3;
                 break;
             case 5:
                 parameter = fc1;
                 break;
             case 6:
                 parameter = fc2;
                 break;
             case 7:
                 parameter = fc3;
                 break;
             case 8:
                 parameter = q1;
                 break;
             case 9:
                 parameter = q2;
                 break;
             case 10:
                 parameter = q3;
                 break;
             case 11:
                 parameter = mode1;
                 break;
             case 12:
                 parameter = mode2;
                 break;
             case 13:
                 parameter = mode3;
                 break;
             default:
                 parameter = null;
         }
         return parameter;
     }
 
 }
