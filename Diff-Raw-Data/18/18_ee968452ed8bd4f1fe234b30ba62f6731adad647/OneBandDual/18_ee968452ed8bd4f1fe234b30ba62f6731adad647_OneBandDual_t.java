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
  * Class for 1-Band (D) parameters.
  *
  * @author Carl Green
  */
 public class OneBandDual extends Eq {
     private static final String NAME = "1-Band (D)";
 
    public final GenericValue<Integer> gainLeft = new GenericValue<Integer>("G-L", "dB", -72, 24);
    public final GenericValue<Integer> fcLeft = new GenericValue<Integer>("Fc-L", "Hz", 20, 20000);
    public final GenericValue<Double> qLeft = new GenericValue<Double>("Q-L", "", 0.1, 10.0);
    public final EqModeValue modeLeft = new EqModeValue("M-L");
    public final GenericValue<Integer> gainRight = new GenericValue<Integer>("G-R", "dB", -72, 24);
    public final GenericValue<Integer> fcRight = new GenericValue<Integer>("Fc-R", "Hz", 20, 20000);
    public final GenericValue<Double> qRight = new GenericValue<Double>("Q-R", "", 0.1, 10.0);
    public final EqModeValue modeRight = new EqModeValue("M-R");
 
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
                 parameter = gainLeft;
                 break;
             case 3:
                 parameter = fcLeft;
                 break;
             case 4:
                 parameter = qLeft;
                 break;
             case 5:
                 parameter = modeLeft;
                 break;
             case 6:
                 parameter = gainRight;
                 break;
             case 7:
                 parameter = fcRight;
                 break;
             case 8:
                 parameter = qRight;
                 break;
             case 9:
                 parameter = modeRight;
                 break;
             default:
                 parameter = null;
         }
         return parameter;
     }
 
 }
