 /*
  * Copyright (C) 2010 Timothy Bourke
  * 
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  * 
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
  * for more details.
  * 
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc., 59
  * Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 
 package mnemojojo;
 
 import java.lang.*;
 import java.io.InputStream;
 import java.io.IOException;
 import java.util.Vector;
 import javax.microedition.media.*;
 
 public class SoundPlayer
     implements PlayerListener
 {
     private String base_path;
     private Vector queue = new Vector(6, 2);
     private Player mp = null;
 
     public SoundPlayer(String path)
     {
         base_path = path;
     }
 
     public void queue(String[] sounds)
     {
        for (int i = 0; i < sounds.length; ++i) {
            // add a brief gap between sounds
            queue.addElement(null);
            queue.addElement(sounds[i]);
        }
 
        startPlaying();
     }
 
     public void clear()
     {
         stopPlaying();
         queue.removeAllElements();
     }
 
     public void playerUpdate(Player player, String event, Object eventData)
     {
         if (event == PlayerListener.END_OF_MEDIA || event == PlayerListener.ERROR) {
             stopPlaying();
             startPlaying();
         }
     }
 
     private void startPlaying()
     {
         if ((mp != null) || (queue.size() == 0)) {
             return;
         }
 
         String sound = (String)queue.firstElement();
         queue.removeElementAt(0);
 
         try {
             if (sound == null) {
                 InputStream is = getClass().getResourceAsStream("/silence.wav");
                 mp = Manager.createPlayer(is, "audio/X-wav");
 
             } else {
                 mp = Manager.createPlayer(base_path + sound);
             }
 
             if (mp == null) {
                 startPlaying();
                 return;
             }
 
             mp.addPlayerListener(this);
             mp.start();
 
         } catch (MediaException e) {
             startPlaying();
             return;
         } catch (IOException e) {
             startPlaying();
             return;
         } catch (SecurityException e) {
             return;
         }
     }
 
     private void stopPlaying()
     {
         if (mp != null) {
             try {
                 mp.deallocate();
             } catch (IllegalStateException e) {}
             mp = null;
         }
     }
 }
 
