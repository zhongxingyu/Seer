 /*
  *  Wezzle
  *  Copyright (c) 2007-2008 Couchware Inc.  All rights reserved. 
  */
 
 package ca.couchware.wezzle2d.audio;
 
 import ca.couchware.wezzle2d.util.CouchLogger;
 import java.net.URL;
 import java.io.*;
 import javax.sound.sampled.*;
 
 
 /**
  * A class to hold an audio file.  This is used by both the sound and music
  * managers. 
  * 
  * @author Kevin, Cameron
  */
 
 public class SoundPlayer
 {             
     
     /** 
      * The audio track.
      */
     private Sound track;
           
     /** 
      * The url for the file.
      */
     private URL url;          
     
     /**
      * The sound clip.
      */
     private Clip clip;      
     
     /**
      * The gain control.
      */
     private FloatControl gainControl;
     
     /**
      * The normalized gain.
      */
     private double normalizedGain;
           
     /** 
      * The current volume.
      */
     private float volume;
 
     /**
      * The constructor.
      * 
      * @param key
      * @param path
      * @param cache
      */
     public SoundPlayer(Sound track, String path, boolean cache)
     {
         // The associated key.
         this.track = track;
         
         // Load the reserouce.
         url = this.getClass().getClassLoader().getResource(path);
         
         // Check the URL.
         if (url == null)
             throw new RuntimeException("Url Error: " + path + " does not exist.");                
                
         // Open the clip.
         open();
         
         // Set the current volumne.
         this.volume = 0.0f;                    
     }               
     
     public void play()
     {
         //LogManager.get().recordMessage("Playing in clip-mode.", "AudioPlayer#playClip");
         
         // Play clip from the start.
         clip.setFramePosition(0);
         clip.start();
         
         try
         {
            Thread.sleep(clip.getMicrosecondLength() / 1000);
         }
         catch (InterruptedException e)
         {
             CouchLogger.get().recordException(this.getClass(), e);
         }
     }
        
     /**
      * A method to load/rest an audio file.      
      */
     private void open()
     {                                   
         // Create the Audio Stream.
         AudioInputStream in = null;
         try
         {
             in = AudioSystem.getAudioInputStream(new BufferedInputStream(url.openStream()));
 
             // The base audio format.
             AudioFormat baseFormat = in.getFormat();
 
             // The decoded input stream.
             AudioInputStream decodedIn = AudioSystem.getAudioInputStream(baseFormat, in);
 
             clip = AudioSystem.getClip();
             clip.open(decodedIn);
             
             this.gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
         }
         catch (LineUnavailableException e)
         {
             CouchLogger.get().recordException(this.getClass(), e);
         }        
         catch (UnsupportedAudioFileException e)
         {
             CouchLogger.get().recordException(this.getClass(), e);
         }
         catch (IOException e)
         {
             CouchLogger.get().recordException(this.getClass(), e);
         }
         finally
         {
             try
             {
                 in.close();
             }
             catch (IOException e)
             {
                 CouchLogger.get().recordException(this.getClass(), e);
             }
         } // end try
     }        
        
     //--------------------------------------------------------------------------
     // Getters and Setters.
     //--------------------------------------------------------------------------           
    
     /**
      * Sets gain (i.e. volume) value.     
      * 
      * Linear scale 0.0 - 1.0.
      * Threshold Coefficent : 1/2 to avoid saturation.
      * 
      * @param fGain The gain to set.     
      */
     public void setNormalizedGain(double nGain)
     {        
         double minGainDB = gainControl.getMinimum();
         double ampGainDB = ((10.0f / 20.0f) * gainControl.getMaximum()) - gainControl.getMinimum();
         double cste = Math.log(10.0) / 20;
         double valueDB = minGainDB + (1 / cste) * Math.log(1 + (Math.exp(cste * ampGainDB) - 1) * nGain);
         gainControl.setValue((float) valueDB);        
         
         // Remember the normalized gain.
         this.normalizedGain = nGain;
     }
     
     /**
      * Gets the gain (i.e. volume) value.
      * 
      * @return
      */
     public double getNormalizedGain()
     {
         return normalizedGain;
     }
     
     /**
      * Get the track enumeration.
      *  
      * @return
      */
     public Sound getTrack()
     {
         return track;
     }
     
 }
