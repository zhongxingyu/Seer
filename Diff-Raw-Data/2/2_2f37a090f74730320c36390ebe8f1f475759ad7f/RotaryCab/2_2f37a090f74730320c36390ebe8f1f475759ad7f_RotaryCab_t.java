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
 
 import info.carlwithak.mpxg2.model.GenericValue;
 import info.carlwithak.mpxg2.model.Parameter;
 import info.carlwithak.mpxg2.model.Rate;
 import info.carlwithak.mpxg2.model.effects.Chorus;
 
 /**
  * Class for Rotary Cab parameters.
  *
  * @author Carl Green
  */
 public class RotaryCab extends Chorus {
     private static final String[] PARAMETER_NAMES = {
        "Mix", "Level", "Rate1", "Dpth1", "Rate2", "Dpth2", "Res", "Width", "Bal"
     };
 
     private Rate rate1;
     private GenericValue<Integer> depth1 = new GenericValue<Integer>("%", 0, 100);
     private Rate rate2;
     private GenericValue<Integer> depth2 = new GenericValue<Integer>("%", 0, 100);
     private GenericValue<Integer> resonance = new GenericValue<Integer>("", -100, 100);
     private GenericValue<Integer> width = new GenericValue<Integer>("%", 0, 100);
     private GenericValue<Integer> balance = new GenericValue<Integer>("", -50, 50);
 
     @Override
     public String getParameterName(final int destinationParameter) {
         return PARAMETER_NAMES[destinationParameter];
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
                 parameter = rate1;
                 break;
             case 3:
                 parameter = depth1;
                 break;
             case 4:
                 parameter = rate2;
                 break;
             case 5:
                 parameter = depth2;
                 break;
             case 6:
                 parameter = resonance;
                 break;
             case 7:
                 parameter = width;
                 break;
             case 8:
                 parameter = balance;
                 break;
             default:
                 parameter = null;
         }
         return parameter;
     }
 
     public Rate getRate1() {
         return rate1;
     }
 
     public void setRate1(Rate rate1) {
         this.rate1 = rate1;
     }
 
     public int getDepth1() {
         return depth1.getValue();
     }
 
     public void setDepth1(int depth1) {
         this.depth1.setValue(depth1);
     }
 
     public Rate getRate2() {
         return rate2;
     }
 
     public void setRate2(Rate rate2) {
         this.rate2 = rate2;
     }
 
     public int getDepth2() {
         return depth2.getValue();
     }
 
     public void setDepth2(int depth2) {
         this.depth2.setValue(depth2);
     }
 
     public int getResonance() {
         return resonance.getValue();
     }
 
     public void setResonance(int resonance) {
         this.resonance.setValue(resonance);
     }
 
     public int getWidth() {
         return width.getValue();
     }
 
     public void setWidth(int width) {
         this.width.setValue(width);
     }
 
     public int getBalance() {
         return balance.getValue();
     }
 
     public void setBalance(int balance) {
         this.balance.setValue(balance);
     }
 }
