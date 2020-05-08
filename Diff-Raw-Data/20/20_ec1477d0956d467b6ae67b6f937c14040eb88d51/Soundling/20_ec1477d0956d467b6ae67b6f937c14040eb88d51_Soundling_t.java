 /*
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
  * Pairs a sound with additional information about the clip
  */
 package lib;
 
 import java.applet.Applet;
 import java.applet.AudioClip;
 
 /**
  *
  * @author nwiehoff
  */
 public class Soundling {
 
     private String name;
     private Sound sound;
     private String target;
     private boolean loop;
 
     public Soundling(String name, String target, boolean loop) {
         this.name = name;
         this.loop = loop;
         this.target = target;
         sound = new Sound(target);
     }
 
     public String getName() {
         return name;
     }
 
     public boolean isPlaying() {
         return sound.playing;
     }
 
     /*
      * controls
      */
     public void play() {
         if (!isLoop()) {
             sound.play();
         } else {
             sound.loop();
         }
     }
 
     public void stop() {
         sound.stop();
     }
 
     public boolean isLoop() {
         return loop;
     }
 
     /*
      * sound data container
      */
     private class Sound {
 
         private AudioClip clip;
         private boolean playing = false;
 
         public Sound(String target) {
             try {
                clip = Applet.newAudioClip(getClass().getClassLoader().getResource("resource/"+target));
                 //setup listener so we know when it is safe to replay
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
 
         private void stop() {
             Thread s = new Thread(new Runnable() {
                 @Override
                 public void run() {
                     clip.stop();
                     playing = false;
                 }
             });
             s.start();
         }
 
         private void play() {
             clip.play();
             playing = true;
         }
 
         private void loop() {
             Thread s = new Thread(new Runnable() {
                 @Override
                 public void run() {
                     clip.loop();
                     playing = true;
                 }
             });
             s.start();
         }
     }
 
     public String toString() {
         return name + ", " + target + ", " + isLoop();
     }
 }
