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
 
 class SFColor extends Field {
     int m_r, m_g, m_b;
 
     SFColor (int r, int g, int b, Observer o) {
         super (o);
         m_r = r;
         m_g = g;
         m_b = b;
     }
 
     SFColor (int r, int g, int b) {
         this (r, g, b, null);
     }
 
     void decode (DataInputStream dis, Node [] table, Decoder decoder) {
         m_r = Decoder.readUnsignedByte (dis);
         m_g = Decoder.readUnsignedByte (dis);
         m_b = Decoder.readUnsignedByte (dis);
         //System.out.println ("SFColor.decode: "+m_r+", "+m_g+", "+m_b);
     } 
 
     int getRgb () {
         return (m_r<<16) + (m_g<<8) + m_b;
     }
 
     void setRgb (int rgb) {
         setValue ((rgb & 0xFF0000) >> 16, (rgb & 0xFF00) >> 8, rgb & 0xFF);
     }
 
     void setValue (int r, int g, int b) {
         m_r = r;
         m_g = g;
         m_b = b;
         notifyChange ();
     }
 
     void copyValue (Field f) {
         try {
             SFColor c = (SFColor)f;
             setValue (c.m_r, c.m_g, c.m_b);
         } catch (Exception e) {
             System.err.println ("SFColor.copyValue: bad source field "+f);
         }
     }
 
     public void set (int index, Register r, int offset) {
         if (index == 0) {
            setRgb (r.getInt());
         } else if (index == Field.HEX_IDX) {
             try { setRgb (Integer.parseInt (r.getString (), 16)); } catch (Exception e) {}
         } else {
             int color = r.getColorComponent();
             if (index == 1) {
                 m_r = color;
             } else if (index == 2) {
                 m_g = color;
             } else {
                 m_b = color;
             }
             notifyChange ();
         }
     }
 
     public void get (int index, Register r, int offset) {
         if (index == 0) {
            r.setInt (getRgb ());
         } else if (index == Field.HEX_IDX) {
             r.setString (Integer.toHexString (getRgb ()));
         } else {
             int color;
             if (index == 1) {
                 color = m_r;
             } else if (index == 2) {
                 color = m_g;
             } else {
                 color = m_b;
             }
             r.setColorComponent(color);
         }
     }
  
 }
