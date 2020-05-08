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
 
 public class Transform2D extends Group {
     int m_tx, m_ty, m_sx, m_sy, m_a/*RCA*/;
     boolean m_hasTrs, m_hasScale, m_hasRot;
     Transform2D () {
         super (4); // RC /13/10/07   av:super (3);
         //System.out.println ("Transform2D created");
         // m_field[0] is created by class Group
         m_field[1] = new SFVec2f (0, 0, this); // translation
         m_field[2] = new SFVec2f (1<<16, 1<<16, this); // scale
         m_field[3] = new SFFloat (0, this); // rotationAngle // RC /13/10/07
   
     }
 
     void start (Context c) {
         super.start (c);
         fieldChanged (m_field[1]);
         fieldChanged (m_field[2]);
         fieldChanged (m_field[3]); // RC /13/10/07
     }
 
     boolean compose (Context c, Region clip, boolean forceUpdate) {
         boolean updated = m_isUpdated | forceUpdate;
      
         m_isUpdated = false;
         c.matrix.push ();
         if (m_hasTrs) c.matrix.translate (m_tx, m_ty);
         if (m_hasRot) c.matrix.rotate (m_a); // RC /13/10/07
         if (m_hasScale) c.matrix.scale (m_sx, m_sy);
         updated |= super.compose (c, clip, updated); //RC 13/10/07
         //updated |= super.compose (c, clip, forceUpdate);
         c.matrix.pop ();
         return updated;
     }
 
     public void fieldChanged (Field f) {
         m_isUpdated = true;
         if (f == m_field[1]) {
             m_tx = ((SFVec2f)f).m_x;
             m_ty = ((SFVec2f)f).m_y;
             m_hasTrs = m_tx != 0 || m_ty != 0;
         } else if (f == m_field[2]) {
             m_sx = ((SFVec2f)f).m_x;
             m_sy = ((SFVec2f)f).m_y;
            m_hasScale = m_sx != 1 || m_sy != 1;
         } else { // RC 13/10/07
             m_a = ((SFFloat)f).m_f;
             m_hasRot = m_a != 0;
         }
     }
 
 }
