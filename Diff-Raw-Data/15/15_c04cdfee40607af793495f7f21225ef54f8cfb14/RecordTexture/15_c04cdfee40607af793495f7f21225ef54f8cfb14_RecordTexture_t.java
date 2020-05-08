 //#condition api.mm
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
 import javax.microedition.media.Manager;
 
 public class RecordTexture extends MediaNode  {
     boolean m_stopped = false;
     boolean m_singleShot = false;
     boolean m_enabled = false;
     boolean m_lastState = false;
     int m_lastStartTime, m_width, m_height;
 
     RecordTexture () {
         super (7, MediaObject.VIDEO, MediaObject.RECORDING);
         //System.out.println ("RecordTexture created");
         m_field[3] = new SFBool (false, this); // singleShot
         m_field[4] = new SFVec2f (320<<16, 240<<16, this); // size
         m_field[5] = new SFBool (true, this); // enable
         m_field[6] = new MFString (); // fileUrl
     }
 
     void start (Context c) {     
         ((MFString)super.m_field[0]).setValue(0, "capture://audio_video");
         super.start (c);
         fieldChanged (m_field[3]);
         fieldChanged (m_field[4]);
         fieldChanged (m_field[5]);
         m_lastStartTime = 0;
         //c.addLoadable (m_media);
     }
 
     boolean compose (Context c, Region clip, boolean forceUpdate) {
         if (m_singleShot) {
             if (m_media == null) {
                 m_media = new MediaObject (MediaObject.VIDEO, MediaObject.SNAPSHOT);
                 m_media.open ("capture://video", c.canvas, null);
             }
             if (m_restartTime != m_lastStartTime && m_media != null  && c.time >= m_restartTime) {
                 m_media.setVisible (false);
                 byte [] data = null;
                 // try snapShot with desired size
                 data = m_media.getSnapshot ("encoding=jpeg&width="+m_width+"&height="+m_height);
                 // check if data is ok
                 if( data == null ) {
                     // try default snapShot
                     data = m_media.getSnapshot (null);
                 }
                 if( data != null && data.length>0 ) {
                     String filename = ((MFString)m_field[6]).getValue(0);
                     Logger.println("Saving in: "+filename);
                     File file = new File (filename, File.MODE_OVERWRITE); 
                     if (file.writeAllBytes(data) == false) {
                         Logger.println ("RT.compose: cannot write data");
                     }
                     file.close (Loadable.CLOSED);
                 }
                 m_lastStartTime = m_restartTime;
                 c.clip.setInt (0, 0, c.width, c.height);
                 m_media.setVisible (true);
             }
             if (c.ac != null) {
                 c.ac.m_mediaObject = m_media;
             }
             m_isUpdated = false;
         } else {
             if (!m_stopped && super.m_media == null ) { //m_stopped just change from true to false
                 start (c);
             }
             if ( Manager.getSupportedProtocols("video/mpeg") != null && !m_stopped) {
                 return super.compose (c, clip, forceUpdate);
             }
            if (m_stopped && super.m_media!=null && super.m_media.m_state == Loadable.PLAYING) {
                 // System.out.println("recordTexture appel stop sur mn");
                 stop (c);
             }
         }
         return false;
     }
 
     public void fieldChanged (Field f) {
         if (f == m_field[3]) {
             m_singleShot = ((SFBool)f).getValue ();
         } else if (f == m_field [4]) {
             m_width = FixFloat.fix2int (((SFVec2f)f).m_x);
             m_height = FixFloat.fix2int (((SFVec2f)f).m_y);
         } else if (f == m_field [5]) {
             m_enabled = ((SFBool)f).getValue ();
         } else {
             int oldStartTime = m_startTime;
             super.fieldChanged(f);
             if (super.m_media != null && 
                super.m_media.m_state == Loadable.PLAYING &&
                 ((SFTime)super.m_field[1]).getValue() == -1000) { //-1000 in Java <=> -1 in VRML file 
                 m_stopped = true;
             } else {
                 m_stopped = false;
             }
             if( oldStartTime != m_startTime ) {
                 
             }
         }
     }
 }
 
