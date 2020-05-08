 /*
  * Copyright (c) 2006-2015 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.audio;
 
 import java.applet.Applet;
 import java.applet.AudioClip;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 /**
  * The AudioPlayer handles the playing of the audio
  */
 public final class AudioPlayer implements Runnable {
 
     /** The AudioType enum */
     public enum AudioType {
 
         WAV, INVALID
 
     }
     /** The file object of the file to play */
     final File myFile;
 
     /**
      * Create the AudioPlayer
      *
      * @param file The file to play
      */
     public AudioPlayer(final File file) {
         myFile = file;
     }
 
     /**
      * Play this AudioPlayer.
      */
     public void play() {
         new Thread(this).start();
     }
 
     /**
      * Run this AudioPlayer (Should not be invoked directly).
      */
     @Override
     public void run() {
         final AudioType type = getAudioType(myFile);
         switch (type) {
             case WAV:
                 playWav();
                 break;
             default:
                 break;
         }
     }
 
     /**
      * Check if this File is a supported file type
      *
      * @param file the File to check
      *
      * @return true if playable, else false.
      */
     public static boolean isValid(final File file) {
         final AudioType type = getAudioType(file);
         return type != AudioType.INVALID;
     }
 
     /**
      * Get the AudioType of a given file
      *
      * @param file the File to check
      *
      * @return AudioType for this file.
      */
     public static AudioType getAudioType(final File file) {
         try (AudioInputStream unused = AudioSystem.getAudioInputStream(file)) {
            unused.reset();
             return AudioType.WAV;
         } catch (UnsupportedAudioFileException | IOException e) {
             return AudioType.INVALID;
         }
     }
 
     /**
      * Play the file as a wav file, using the Applet class. (This code seems to work better than the
      * non-applet version, but can't play streams)
      */
     private void playWav() {
         try {
             final AudioClip ac = Applet.newAudioClip(myFile.toURI().toURL());
             if (ac != null) {
                 ac.play();
             }
         } catch (MalformedURLException e) {
         }
     }
 
 }
