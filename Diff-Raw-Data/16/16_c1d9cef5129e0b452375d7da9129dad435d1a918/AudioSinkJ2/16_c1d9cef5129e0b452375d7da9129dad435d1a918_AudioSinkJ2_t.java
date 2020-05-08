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
  public static final int SEGSIZE = 2048;
 
   private SourceDataLine line = null;
   private int channels;
   private long samplesWritten;
 
   protected RingBuffer createRingBuffer() {
     return new RingBuffer();
   }
 
   protected boolean open (RingBuffer ring) {
     channels = ring.channels;
     line = openLine(ring.channels, ring.rate);
     if (line == null) {
       postMessage (Message.newError (this, "Could not open audio device."));
       return false;
     }
 
     Debug.log(Debug.INFO, "line info: available: "+ line.available());
     Debug.log(Debug.INFO, "line info: buffer: "+ line.getBufferSize());
     Debug.log(Debug.INFO, "line info: framePosition: "+ line.getFramePosition());
 
    ring.segSize = SEGSIZE*channels*2;
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
 
   protected SourceDataLine openLine(int channels, int rate) {
     AudioFormat format = new AudioFormat(rate, 16, channels, true, true);
     DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
     SourceDataLine line = null;
 
     try {
       Mixer.Info[] mixers = AudioSystem.getMixerInfo();
 
       /* On linux, the default implementation gives terribly inaccurate results
        * from line.available(), so we can't keep sync. Modern JVMs have an ALSA
        * implementation that doesn't suck, so use that if available. */
       for(int i=0; i < mixers.length; i++) {
         Debug.log(Debug.INFO, "mixer description: " + 
                 mixers[i].getDescription() + ", vendor: " + 
                 mixers[i].getVendor());
         /* Apparently either description or vendor might contain 'ALSA' - on
          * my system, it's vendor */
         String desc = mixers[i].getDescription();
         String vendor = mixers[i].getVendor();
         if(desc.indexOf("ALSA") >= 0 || 
            vendor.indexOf("ALSA") >= 0) 
         {
           /* Unfortunately, the alsa devices include useless ones that we have
            * no sane way of filtering out! Hence this insanity. */
           if (desc.indexOf("IEC958") >= 0)
             continue;
 
           try {
             Line.Info[] lines = AudioSystem.getMixer(mixers[i]).
                 getSourceLineInfo(info);
 
             for (int j=0; j < lines.length; j++) {
               Debug.log(Debug.INFO, "Mixer supports line: " + 
                   lines[j].toString());
               AudioFormat[] formats = ((DataLine.Info)lines[j]).getFormats();
               for(int k=0; k < formats.length; k++)
                 Debug.log(Debug.INFO, "Format: " + formats[k].toString());
             }
             Debug.log(Debug.INFO, "Attempting to get a line from ALSA mixer");
             line = (SourceDataLine) AudioSystem.getMixer(
                 mixers[i]).getLine(info);
             /* Got one. Excellent. Try it. */
             line.open(format);
             break;
           } catch (Exception e) {
             if (line != null) {
               line.close();
               line = null;
             }
             /* Don't care too much; we'll fall through to the default case
              * later, and do proper error handling there */
             Debug.log(Debug.INFO, "mixer: " + mixers[i].getDescription() + 
                 " failed: " + e);
           }
         }
       }
 
       /* If that failed, use the default line. */
       if (line == null) {
         line = (SourceDataLine) AudioSystem.getLine(info);
         line.open(format);
       }
     }
     catch (javax.sound.sampled.LineUnavailableException e) {
       Debug.error(e.toString());
       return null;
     }
     catch (Exception e) {
       Debug.error(e.toString());
       return null;
     }
 
     return line;
   }
 
   public boolean test() {
     SourceDataLine line;
     line = openLine(2, 44000);
     if (line == null) {
       return false;
     }
     line.close();
     return true;
   }
 
   protected boolean close (RingBuffer ring)
   {
     line.stop();
     line.close();
 
     return true;
   }
 
   protected int write (byte[] data, int offset, int length) {
     int written = 0;
     
     if ( offset < 0 || offset >= data.length || offset + length > data.length || length <= 0 ) {
       // This happens on stop for some reason
       Debug.debug( "Invalid audio write offset=" + offset + ", length=" + length + ", data.length=" + data.length );
       return length;
     }
 
 
     // Need to avoid blocking due to lock contention in line.getFramePosition() in Java 6.
     while ( true ) {
       int available = line.available();
       if ( length > available ) {
 	if ( available > 0 ) {
 	  Debug.debug( "Doing partial audio write of " + available + " bytes" );
 	  written += line.write( data, offset, available );
 	  offset += available;
 	  length -= available;
 	}
 	if ( length > 0 ) {
 	  try {
 	    // Sleep for a quarter of the buffer time before we fill it up again
 	    AudioFormat format = line.getFormat();
 	    long sleepTime = (long)(line.getBufferSize() * 1000 
	      / format.getSampleRate() / format.getSampleSizeInBits() * 8 / (2*channels));//ee
 	    Debug.debug( "Sleeping for " + sleepTime + "ms" );
 	    Thread.sleep(sleepTime);
 	  } catch ( InterruptedException e ) {}
 	  continue;
 	}
       } else {
 	Debug.debug( "Doing complete audio write of " + length + " bytes" );
 	written += line.write( data, offset, length );
       }
       break;
     }
     samplesWritten += written / (2 * channels);
     return written;
   }
 
   protected long delay () {
     int frame; 
     long delay;
 
     frame = line.getFramePosition();
     delay = samplesWritten - frame;
     return delay;
   }
 
   protected void reset () {
     Debug.log(Debug.DEBUG, "reset audio: "+ line);
     line.flush();
     samplesWritten = line.getFramePosition();
     Debug.log(Debug.DEBUG, "samples written: "+ samplesWritten);
   }
 
   public String getFactoryName ()
   {
     return "audiosinkj2";
   }
 
 }
