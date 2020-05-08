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
 
 import info.carlwithak.mpxg2.model.PanValue;
 import info.carlwithak.mpxg2.model.Parameter;
 import info.carlwithak.mpxg2.model.effects.Effect;
 
 /**
  * Class for Panner parameters.
  *
  * @author Carl Green
  */
 public class Panner extends Effect {
     private static final String NAME = "Panner";
 
    private PanValue pan1 = new PanValue("Pan1", -50, 50);
    private PanValue pan2 = new PanValue("Pan2", -50, 50);
 
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
                 parameter = pan1;
                 break;
             case 3:
                 parameter = pan2;
                 break;
             default:
                 parameter = null;
         }
         return parameter;
     }
 
     public PanValue getPan1() {
         return pan1;
     }
 
     public void setPan1(int pan1) {
         this.pan1.setValue(pan1);
     }
 
     public PanValue getPan2() {
         return pan2;
     }
 
     public void setPan2(int pan2) {
         this.pan2.setValue(pan2);
     }
 }
