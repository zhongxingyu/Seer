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
 
 public class Rectangle extends Node {
     int m_w, m_h, m_rx, m_ry;
     int m_rotation = 0,m_oldRotation=0;
     GraphicEffect m_ge;
     Region m_roundRegion;
 
     Rectangle () {
         super (2);
         //System.out.println ("Rectangle created");
         m_field[0] = new SFVec2f (0, 0, this); // size
         m_field[1] = new SFVec2f (0, 0, this); // cornerRadius
         m_ge = new GraphicEffect ();
     }
     
     void start (Context c) {
         fieldChanged (m_field[0]);
         fieldChanged (m_field[1]);
     }
     
     void render (Context c) {
         int w = m_region.x1-m_region.x0+1;
         int h = m_region.y1-m_region.y0+1;
         if (m_type == AppearanceContext.TYPE_BITMAP) {
             m_ac.m_image.drawImage (c.gc, m_region.x0, m_region.y0, w, h, m_ac.m_transparency,/*RCA*/m_rotation);
 //#ifdef api.mm
        } else if (m_type == AppearanceContext.TYPE_VIDEO && m_ac.m_mediaObject.getState() != Loadable.PLAYING) {
             //FT - m_ac.m_mediaObject.setRegion (m_region.x0, m_region.y0, w, h);
             c.gc.setColor (m_ac.m_color);
             c.gc.fillRect (m_region.x0, m_region.y0, w, h);
 //#endif
         } else if (m_type == AppearanceContext.TYPE_RECTANGLE && m_ac.m_transparency != 1<<16) {
             c.gc.setColor (m_ac.m_color);
             if (m_roundRegion == null) {
                 if (m_ac.m_filled) {
                     if (m_ac.m_transparency > 0) {
                         m_ge.fillBlock (c.gc, m_region.x0, m_region.y0, w, h, m_ac.m_color, m_ac.m_transparency);
                     } else {
                         c.gc.fillRect (m_region.x0, m_region.y0, w, h);
                     }
                 } else {
                     c.gc.drawRect (m_region.x0, m_region.y0, w-1, h-1);
                 }
             } else { // rounded
                 int rx = m_roundRegion.x1-m_roundRegion.x0;
                 int ry = m_roundRegion.y1-m_roundRegion.y0;
                 if (m_ac.m_filled) {
                     if (m_ac.m_transparency > 0) {
                         m_ge.fillEffect (c.gc, m_region.x0, m_region.y0, w, h, m_ac.m_color, m_ac.m_transparency, rx, ry, GraphicEffect.ROUND_RECT);
                     } else {
                         c.gc.fillRoundRect (m_region.x0, m_region.y0, w, h, rx, ry);
                     }
                 } else {
                     c.gc.drawRoundRect (m_region.x0, m_region.y0, w-1, h-1, rx, ry);
                 }
             }
         }
     }
 
     boolean compose (Context c, Region clip, boolean forceUpdate) {
         
         boolean updated = m_isUpdated | forceUpdate;
         m_isUpdated = false;
         m_ac = c.ac;
         if (updated) { // only compute region on updates !
 //#ifdef api.mm
             if (m_ac.m_mediaObject != null) {
                 m_type = AppearanceContext.TYPE_VIDEO;
                 m_region.setFloat (-m_w/2, m_h/2, m_w/2+1, -m_h/2-1);
             } else
 //#endif
             if (m_ac.m_image != null) {
                 m_type = AppearanceContext.TYPE_BITMAP;
                 int w = m_w; int h = m_h;
                 if (m_ac.m_image.m_srcW != 0) {
                     if (m_w < 0) { w = m_ac.m_image.m_srcW; }
                     if (m_h < 0) { h = m_ac.m_image.m_srcH; }
                 } else if (m_ac.m_hasMaterial) {
                     m_type = AppearanceContext.TYPE_RECTANGLE;
                 }
                 //MCP: (k+1)%2 Prevent rescale of odd sized images to even sized images (111x89=>110x88 !!)
                 m_region.setFloat (-w/2, h/2, w/2-(w+1)%2, -h/2+(h+1)%2);
             
             } else {
                 m_type = AppearanceContext.TYPE_RECTANGLE;
                 //MCP: (k+1)%2 Prevent odd sized rectangles to render as even rectangles (111x89=>110x88 !!)
                 m_region.setFloat (-m_w/2, m_h/2, m_w/2-(m_w+1)%2, -m_h/2+(m_h+1)%2);
             }
             if (m_rx != 0 || m_ry != 0) { // rounded corners
                 if (m_roundRegion == null) m_roundRegion = new Region();
                 m_roundRegion.setFloat (-m_rx/2, m_ry/2, m_rx/2, -m_ry/2);
                 c.matrix.transform (m_roundRegion);
                 m_roundRegion.toInt ();
                 m_roundRegion.getRotationAndNormalize ();
             } else {
                 m_roundRegion = null;
             }
             c.matrix.transform (m_region);
             m_region.toInt ();
             m_rotation = m_region.getRotationAndNormalize ();//RCA 13/10/07
         }
         if (!isVisible(clip, c.bounds)) {
             return updated;
         }
         
         c.addRenderNode (this);
 //#ifdef api.mm
         if (m_type == AppearanceContext.TYPE_VIDEO) {// && m_ac.m_mediaObject.getInternalState() == MediaObject.STATE_PLAYING) {
             if (m_rotation != m_oldRotation){
                 m_oldRotation = m_rotation;
                 m_ac.addClip (clip, new Region (0, 0, c.width, c.height));// m_region);
                 Logger.println ("Rectangle: rotating video");
                 updated = true;
             }
            if (m_ac.m_mediaObject.getInternalState() == Loadable.PLAYING){
                 c.m_hasVideo = true; //RCA: should be set only when video *is* active
                 if (m_isVideo == false) { //  nok sur w910=> && m_ac.m_mediaObject.m_player.getMediaTime()>0) {
                     m_isVideo = true;
                     //System.out.println("time:"+m_ac.m_mediaObject.m_player.getMediaTime());
                     m_ac.addClip (clip, m_region);
                     Logger.println ("Rectangle: swapping to video mode");
                     updated = true;
 //#ifdef jsr.nokia-ui
                     BackLight.start (); // try to force backlight on
 //#endif
                 }
             } else {
                 c.m_hasVideo = false;
                 if (m_isVideo) {
                     m_isVideo = false;
                     updated = true;
                     Logger.println ("Rectangle: swapping to NON video mode");
 //#ifdef jsr.nokia-ui
                     BackLight.stop (); // stop forcing backlight on
 //#endif
                 }
             }
             m_ac.m_mediaObject.setRegion (m_region.x0, m_region.y0, m_region.x1-m_region.x0-1, m_region.y1-m_region.y0-1, m_rotation); 
             if (updated) {
                 c.exclude.set (m_region, c.width, c.height); //FTE surface a ne pas dessiner sauf si overlay
                 //m_ac.m_mediaObject.setRegion (m_region.x0, m_region.y0, m_region.x1-m_region.x0-1, m_region.y1-m_region.y0-1, m_rotation); 
                 //return (updated);
             } else {
                 return (updated);
             }
         }
 //#endif
         m_region.x1 += 1;
         m_region.y1 += 1;
         updated |= m_ac.isUpdated (m_region);
         if (updated) {
             m_ac.addClip (clip, m_region);
         }
         m_region.x1 -= 1;
         m_region.y1 -= 1;
         return updated;
     }
 
     public void fieldChanged (Field f) {
         m_isUpdated = true;
         if (f == m_field[0]) {
             m_w = FixFloat.fix2int(((SFVec2f)f).m_x);
             m_h = FixFloat.fix2int(((SFVec2f)f).m_y);
         } else {
             m_rx = FixFloat.fix2int(((SFVec2f)f).m_x);
             m_ry = FixFloat.fix2int(((SFVec2f)f).m_y);
         }
     }
 }
