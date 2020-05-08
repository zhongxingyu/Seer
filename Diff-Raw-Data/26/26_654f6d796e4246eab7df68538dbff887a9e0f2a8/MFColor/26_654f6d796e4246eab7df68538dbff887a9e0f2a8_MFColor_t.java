 /*
  * Copyright (C) 2010 France Telecom
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package memoplayer;
 import java.io.*;
 
 class MFColor extends MFFloatBase {
 
     MFColor (Observer o) {
         super (3, o);
     }
     MFColor () {
         super (3, null);
     }
 
     void decode (DataInputStream dis, Node [] table, Decoder decoder) {
         //System.out.print ("decoding MFColor: #");
         int size = Decoder.readUnsignedByte (dis);
         if (size == 255) {
             size = Decoder.readUnsignedByte (dis) * 255 + Decoder.readUnsignedByte (dis) ;
         }
         //System.out.print (size+ " [ ");
         if (size > 0) {
             ensureCapacity (size);
             for (int i = 0; i < m_size; i++) {
                 m_value[i*3] = Decoder.readUnsignedByte (dis);
                 m_value[i*3+1] = Decoder.readUnsignedByte (dis);
                 m_value[i*3+2] = Decoder.readUnsignedByte (dis);
                 //System.out.print (m_value[i*3]+" "+m_value[i*3+1]+" "+m_value[i*3+2]+" ");
             }
             //System.out.println ("]");
         }
     }
 
     int getRgb (int i) {
         i *= 3;
         return (m_value[i]<<16) + (m_value[i+1]<<8) + m_value[i+2];
     }
 
     void setRgb (int i, int rgb) {
         i *= 3;
         m_value[i] = (rgb & 0xFF0000) >> 16;
         m_value[i+1] = (rgb & 0xFF00) >> 8;
         m_value[i+2] = rgb & 0xFF;
     }
 
     public void set (int index, Register r, int offset) {
         if (index == Field.LENGTH_IDX) {
             r.setInt (m_size);
         } else if (offset >= 0) {
             if (offset >= m_size) {
                 ensureCapacity(offset+1);
             }
             if (index == Field.HEX_IDX) {
                 try { setRgb(offset, Integer.parseInt(r.getString(), 16)); } catch (NumberFormatException e) {}
             } else if (index == 0) {
                Field f = r.getField();
                if (f instanceof SFColor) {
                    SFColor c = ((SFColor)f);
                    offset *= 3;
                    m_value[offset] = c.m_r;
                    m_value[offset+1] = c.m_g;
                    m_value[offset+2] = c.m_b;
                } else {
                    setRgb(offset, r.getInt());
                }
             } else {
                 m_value [offset*m_chunk+(index-1)] = r.getColorComponent();
             }
             notifyChange ();
         }
     }
     
     public void get (int index, Register r, int offset) {
         if (index == Field.LENGTH_IDX) {
             r.setInt (m_size);
         } else if (index == Field.OBJECT_IDX) {
             r.setField (this);
         } else if (offset >= 0 && offset < m_size) {
             if (index == Field.HEX_IDX) {
                 r.setString(Integer.toHexString(getRgb(offset)));
             } else if (index == 0) {
                 r.setInt(getRgb(offset));
             } else {
                 r.setColorComponent(m_value [offset*m_chunk+(index-1)]);
             }
         } else { // out of bounds, return default value
             r.setFloat(0);
         }
     } 
 }
