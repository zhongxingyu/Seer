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
 
 package info.carlwithak.mpxg2.model;
 
 /**
  * Class to hold information about the Envelope Generator controller.
  *
  * @author Carl Green
  */
 public class EnvelopeGenerator implements DataObject {
     private GenericValue<Integer> src1 = new GenericValue<Integer>("Src1", "", 0, 20);
    private GenericValue<Integer> src2 = new GenericValue<Integer>("Src2", "", 0, 20);
     private GenericValue<Integer> aTrim = new GenericValue<Integer>("ATrim", "", 0, 100);
     private GenericValue<Integer> response = new GenericValue<Integer>("Resp", "", 0, 100);
 
     @Override
     public Parameter getParameter(final int parameterIndex) {
         Parameter parameter;
         switch (parameterIndex) {
             case 0:
                 parameter = src1;
                 break;
             case 1:
                 parameter = src2;
                 break;
             case 2:
                 parameter = aTrim;
                 break;
             case 3:
                 parameter = response;
                 break;
             default:
                 parameter = null;
         }
         return parameter;
     }
 
     public int getSrc1() {
         return src1.getValue();
     }
 
     public void setSrc1(final int src1) {
         this.src1.setValue(src1);
     }
 
     public int getSrc2() {
         return src2.getValue();
     }
 
     public void setSrc2(final int src2) {
         this.src2.setValue(src2);
     }
 
     public int getATrim() {
         return aTrim.getValue();
     }
 
     public void setATrim(final int aTrim) {
         this.aTrim.setValue(aTrim);
     }
 
     public int getResponse() {
         return response.getValue();
     }
 
     public void setResponse(final int response) {
         this.response.setValue(response);
     }
 
 }
