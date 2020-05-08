 /* Copyright (C) <2004> Wim Taymans <wim@fluendo.com>
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Library General Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Library General Public License for more details.
  *
  * You should have received a copy of the GNU Library General Public
  * License along with this library; if not, write to the
  * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
  * Boston, MA 02111-1307, USA.
  */
 
 package com.fluendo.plugin;
 
 import com.fluendo.jst.*;
 import com.fluendo.utils.*;
 import javax.sound.sampled.*;
 
 public class AudioSinkJ2 extends AudioSink
 {
   public static final int SEGSIZE = 8192;
 
   private SourceDataLine line = null;
   private int channels;
   private long samplesWritten;
 
   protected RingBuffer createRingBuffer() {
     return new RingBuffer();
   }
 
   protected boolean open (RingBuffer ring) {
     channels = ring.channels;
 
     AudioFormat format = new AudioFormat(ring.rate, 16, ring.channels, true, true);
     DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
 
     try {
       line = (SourceDataLine) AudioSystem.getLine(info);
       line.open(format);
     }
     catch (javax.sound.sampled.LineUnavailableException e) {
       e.printStackTrace();
       postMessage (Message.newError (this, "Could not open audio device."));
       return false;
     }
     catch (Exception e) {
       e.printStackTrace();
       postMessage (Message.newError (this, "Unknown problem opening audio device"));
       return false;
     }
 
     Debug.log(Debug.INFO, "line info: available: "+ line.available());
     Debug.log(Debug.INFO, "line info: buffer: "+ line.getBufferSize());
     Debug.log(Debug.INFO, "line info: framePosition: "+ line.getFramePosition());
 
     ring.segSize = SEGSIZE;
     ring.segTotal = line.getBufferSize() / ring.segSize;
     while (ring.segTotal < 4) {
       ring.segSize >>= 1;
       ring.segTotal = line.getBufferSize() / ring.segSize;
     }
 
     ring.emptySeg = new byte[ring.segSize];
     samplesWritten = 0;
 
     line.start();
 
     return true;
   }
 
   protected boolean close (RingBuffer ring)
   {
     line.stop();
     line.close();
 
     return true;
   }
 
   protected int write (byte[] data, int offset, int length) {
     int written;
 
     written = line.write (data, offset, length);
     samplesWritten += written / (2 * channels);
 
     return written;
   }
 
   protected long delay () {
     int frame; 
    long delay;
 
     //size = line.getBufferSize();
     //avail = line.available();
     frame = line.getFramePosition();
     //time = line.getMicrosecondPosition();
 
     delay = samplesWritten - frame;
 
     //System.out.println("size: "+size+" avail: "+avail+" frame: "+frame+" time: "+time+" delay: "+delay);
 
     //return (size - avail) / (2 * channels);
     return delay;
   }
 
   protected void reset () {
     line.flush();
     samplesWritten = line.getFramePosition();
   }
 
   public String getFactoryName ()
   {
     return "audiosinkj2";
   }
 
 }
