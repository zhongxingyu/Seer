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
 
 package org.tbrk.mnemododo;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import android.content.Context;
 import android.content.res.AssetFileDescriptor;
 import android.media.MediaPlayer;
import android.widget.Toast; /* XXX */
 
 public class SoundPlayer
 {
     private String base_path = "";
     private Queue<String> to_play = new LinkedList<String>();
     private MediaPlayer mp = null;
     private Context context = null;
     
     public SoundPlayer(Context context)
     {
         this.context = context;
     }
     
     public void setBasePath(String path)
     {
         base_path = path;
     }
     
     public void queue(String[] sounds)
     {
         for (String sound : sounds) {
            Toast.makeText(context, "media: queuing: " + sound, Toast.LENGTH_SHORT).show(); /* XXX */
             if (!to_play.isEmpty()) {
                 // add a brief gap between sounds
                 to_play.add(null);
             }
 
             File f = new File(base_path, sound);
             to_play.add(f.getAbsolutePath());
         }
         
         startPlaying();
     }
     
     public void clear()
     {
         stopPlaying();
         to_play.clear();
     }
     
     public void release()
     {
         clear();
         if (mp != null) {
             mp.release();
             mp = null;
         }
     }
     
     private void startPlaying()
     {
         if (to_play.isEmpty()) {
             return;
         }
         
         if (mp == null) {
             mp = new MediaPlayer();
             if (mp == null) {
                 return;
             }
             
             mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                 public void onCompletion(MediaPlayer mp)
                 {
                     SoundPlayer.this.startPlaying();
                 }
             });
             
             mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                 public boolean onError(MediaPlayer mp, int what, int extra)
                 {
                     return true;
                 }
             });
             
         } else {
             if (mp.isPlaying()) {
                 return;
             }
 
             mp.reset();
         }
         
         try {
             String file = to_play.remove();
            String filename; /* XXX */
             if (file == null) {
                 AssetFileDescriptor afd =
                     context.getResources().openRawResourceFd(R.raw.silence);
                 if (afd == null) {
                     startPlaying();
                     return;
                 }
                 mp.setDataSource(afd.getFileDescriptor(),
                         afd.getStartOffset(), afd.getLength());
                 afd.close();
                filename = "null"; /* XXX */
 
             } else {
                 mp.setDataSource(file);
                filename = file; /* XXX */
             }
 
             mp.prepare();
            Toast.makeText(context, "media: MediaPlayer.start: " + filename, Toast.LENGTH_SHORT).show(); /* XXX */
             mp.start();
 
         } catch (IllegalArgumentException e) {
            Toast.makeText(context, "media: IllegalArgumentException", Toast.LENGTH_SHORT).show(); /* XXX */
             startPlaying();
             return;
         } catch (IllegalStateException e) {
            Toast.makeText(context, "media: IllegalStateException", Toast.LENGTH_SHORT).show(); /* XXX */
             return;
         } catch (IOException e) {
            Toast.makeText(context, "media: IOException", Toast.LENGTH_SHORT).show(); /* XXX */
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show(); /* XXX */
             startPlaying();
             return;
         }
     }
 
     private void stopPlaying()
     {
         if (mp == null) {
             return;
         }
         
         try {
             mp.stop();
         } catch (IllegalStateException e) {
             return;
         }
     }
 }
