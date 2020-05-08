 package com.calcprogrammer1.calctunes;
 
 import java.io.File;
 
 import org.jaudiotagger.audio.AudioFile;
 import org.jaudiotagger.tag.FieldKey;
 import org.jaudiotagger.tag.Tag;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 
 interface MediaPlayerHandlerCallback
 {
     void onSongFinished();
     
     void onStop();
 }
 
 public class MediaPlayerHandler
 {
     MediaPlayer mp;
     LosslessMediaCodecHandler ls;
     
     boolean running = false;
     boolean prepared = false;
     boolean playonprepare = false;
     
     String current_path;
     String current_title;
     String current_album;
     String current_artist;
     
     MediaPlayerHandlerCallback cb;
     
     public MediaPlayerHandler()
     {
 
     }
     
     public void setCallback(MediaPlayerHandlerCallback callback)
     {
         cb = callback;
     }
     
     public void initialize(String filePath)
     {
         current_path = filePath;
         initialize();
     }
     
     public void initialize()
     {
         stopPlayback();
         mp = new MediaPlayer();
         File song = new File(current_path);
         AudioFile f;
         try
         {
             f = LibraryOperations.readAudioFileReadOnly(song);
             Tag tag = f.getTag();
             current_artist = tag.getFirst(FieldKey.ARTIST);
             current_album = tag.getFirst(FieldKey.ALBUM);
             current_title = tag.getFirst(FieldKey.TITLE);
             
             mp.reset();
             mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
             mp.setDataSource(current_path);
             mp.prepareAsync();
             mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
             {
                 public void onPrepared(MediaPlayer arg0)
                 {
                     prepared = true;
                     if(playonprepare)
                     {
                         mp.start();
                         playonprepare = false;
                     }
                 } 
             });
             mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
             {
                 public void onCompletion(MediaPlayer arg0)
                 {
                     mp.stop();
                     prepared = false;
                     mp.release();
                     if(cb != null) cb.onSongFinished();
                 }
             });
         }
         catch (Exception e)
         {
             mp.release();
             mp = null;
             ls = new LosslessMediaCodecHandler();
             ls.setCallback(new LosslessMediaCodecHandlerCallback()
             {
                 public void onCompletion()
                 {
                     prepared = false;
                     ls = null;
                     if(cb != null) cb.onSongFinished();
                 }
             });
             ls.setDataSource(current_path);
             prepared = true;
             if(playonprepare)
             {
                 ls.start();
                 playonprepare = false;
             }
         }
     }
     
     public void startPlayback()
     {
         if(prepared)
         {
             if(mp != null)
             {
                 mp.start();
             }
             else if(ls != null)
             {
                 ls.start();
             }
         }
         else
         {
             playonprepare = true;
         }
     }
     
     public void stopPlayback()
     {
         if(prepared)
         {
             if(mp != null)
             {
                 mp.stop();
                 prepared = false;
                 mp.release();
                 mp = null;
             }
             else if(ls != null)
             {
                 ls.stop();
                 prepared = false;
                 ls = null;
             }
             current_path = "";
             current_title = "";
             current_artist = "";
             current_album = "";
             if(cb != null) cb.onStop();
         }
     }
     
     public void pausePlayback()
     {
         if(prepared)
         {
            if(mp != null)
            {
                mp.pause();
            }
            else if(ls != null)
            {
                ls.pause();
            }
         }
     }
     
     public boolean isPlaying()
     {
         if(prepared)
         {
             if(mp != null)
             {
                 return mp.isPlaying();
             }
             else if(ls != null)
             {
                 return ls.isPlaying();
             }
             else
             {
                 return false;
             }
         }
         else
         {
             return false;
         }
     }
     
     public void seekPlayback(int seekto)
     {
         if(prepared)
         {
             if(mp != null)
             {
                 mp.pause();
                 mp.seekTo(seekto);
                 mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener()
                 {
                     public void onSeekComplete(MediaPlayer arg0)
                     {
                         mp.start();
                     }
                 });
             }
             else if(ls != null)
             {
                 ls.seekTo(seekto);
             }
         }
     }
     
     public int getCurrentPosition()
     {
         if(prepared)
         {
             if(mp != null)
             {
                 return mp.getCurrentPosition();
             }
             else if(ls != null)
             {
                 return ls.getCurrentPosition();
             }
             else
             {
                 return 0;
             }
         }
         else
         {
             return 0;
         }
     }
     
     public int getDuration()
     {
         if(prepared)
         {
             if(mp != null)
             {
                 return mp.getDuration();
             }
             else if(ls != null)
             {
                 return ls.getDuration();
             }
             else
             {
                 return 0;
             }
         }
         else
         {
             return 0;
         }
     }
 }
