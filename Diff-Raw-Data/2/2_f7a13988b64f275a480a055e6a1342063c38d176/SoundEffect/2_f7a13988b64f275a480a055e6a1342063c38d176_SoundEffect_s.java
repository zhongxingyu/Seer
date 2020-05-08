 package com.servegame.abendstern.tunnelblick.backend;
 
 import java.io.*;
 import java.util.*;
 
 /**
  * Manages a collection of sound effects -- commonly-played, short audio
  * samples stored in raw (unformatted) files.
  */
 public final class SoundEffect implements AudioSource {
   private final short data[];
   private int offset = 0;
 
   private SoundEffect(short data[]) {
     this.data = data;
   }
 
   public int read(short dst[], int len) {
     if (offset == data.length)
       return -1;
 
     if (len > data.length - offset)
       len = data.length - offset;
 
     for (int i = 0; i < len; ++i)
       dst[i] = data[offset+i];
 
     offset += len;
     return len;
   }
 
   private static final HashMap<String, short[]> effects =
     new HashMap<String, short[]>();
 
   /**
    * Plays the sound effect stored in the given raw file.
    *
    * On the first invocation with a certain name, the file is loaded off disk,
    * if possible. If the file cannot be loaded, nothing else happens.
    */
   public static synchronized void play(String filename, AudioPlayer player,
                                        short volume) {
     if (!effects.containsKey(filename)) {
       File file = new File(filename);
       FileInputStream fis = null;
       try {
         if (!file.exists())
           throw new FileNotFoundException(filename);
 
         short buffer[] = new short[(int)file.length() / 2];
         byte byteBuffer[] = new byte[(int)file.length()];
         fis = new FileInputStream(file);
         if (byteBuffer.length != fis.read(byteBuffer))
           throw new IOException("Could not read whole file");
 
         for (int i = 0; i < buffer.length; ++i)
           buffer[i] = (short)(
            ((short)byteBuffer[i*2+0]) |
             (((short)byteBuffer[i*2+1]) << 8));
 
         effects.put(filename, buffer);
       } catch (IOException ioe) {
         System.err.println("Loading sound from " + filename + ": " + ioe);
         effects.put(filename, null);
       } finally {
         if (fis != null) {
           try {
             fis.close();
           } catch (IOException whichWeCantDoAnythingAbout) {
           }
         }
       }
     }
 
     short[] buffer = effects.get(filename);
     if (buffer == null) return; // No sound to play
 
     player.addSource(new SoundEffect(buffer), volume);
   }
 }
