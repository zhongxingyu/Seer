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
 
 public class Material2D extends Node {
     int m_color, m_transparency;
     boolean m_filled;
 
     Material2D () {
         super (3);
         //System.out.println ("Material2D created");
        m_field[0] = new SFColor (1, 1, 1, this); // emissiveColor
         m_field[1] = new SFFloat (0, this);       // transparency
         m_field[2] = new SFBool (true, this);     // filled
     }
     
     void start (Context c) {
         fieldChanged (m_field[0]);
         fieldChanged (m_field[1]);
         fieldChanged (m_field[2]);
     }
 
     void stop (Context c) {
     }
 
     boolean compose (Context c, Region clip, boolean forceUpdate) {
         boolean updated = isUpdated (forceUpdate);
         c.ac.m_color = m_color;
         c.ac.m_transparency = m_transparency;
         c.ac.m_filled = m_filled;
         c.ac.m_hasMaterial = true;
         return updated;
     }
 
     public void fieldChanged (Field f) {
         m_isUpdated = true;
         if (f == m_field[0]) {
             m_color = ((SFColor)f).getRgb ();
         } else if (f == m_field[1]) {
             m_transparency = ((SFFloat)f).getValue ();
         } else {
             m_filled = ((SFBool)f).getValue ();
         }
     }
 
 }
