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
 
 import java.io.*;
 import sun.audio.*;
 import com.fluendo.utils.*;
 
 public class AudioSinkSA extends AudioSink
 {
   private static final int BUFFER = 16 * 1024;
   private static final int SEGSIZE = 256;
   private static final int DELAY = 8 * 1000; /* in samples at 8000 Hz */
 
   private double rateDiff;
   private int delay;
 
   private static final boolean ZEROTRAP=true;
   private static final short BIAS=0x84;
   private static final int CLIP=32635;
   private static final byte[] exp_lut =
     { 0, 0, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3,
       4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
       5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
       5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
       6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
       6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
       6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
       6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
       7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
       7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
       7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
       7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
       7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
       7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
       7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
       7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7
     };
 
   /* muLaw header */
   private static final short[] header =
                          { 0x2e, 0x73, 0x6e, 0x64,              // header in be
                            0x00, 0x00, 0x00, 0x18,              // offset
                            0x7f, 0xff, 0xff, 0xff,              // length
                            0x00, 0x00, 0x00, 0x01,              // ulaw
                            0x00, 0x00, 0x1f, 0x40,              // frequency
                            0x00, 0x00, 0x00, 0x01               // channels
                          };
   private int headerPos;
 
   private final int toUlaw(int sample)
   {
     int sign, exponent, mantissa, ulawbyte;
 
     if (sample>32767) sample=32767;
     else if (sample<-32768) sample=-32768;
     /* Get the sample into sign-magnitude. */
     sign = (sample >> 8) & 0x80;    /* set aside the sign */
     if (sign != 0) sample = -sample;    /* get magnitude */
     if (sample > CLIP) sample = CLIP;    /* clip the magnitude */
 
     /* Convert from 16 bit linear to ulaw. */
     sample = sample + BIAS;
     exponent = exp_lut[(sample >> 7) & 0xFF];
     mantissa = (sample >> (exponent + 3)) & 0x0F;
     ulawbyte = ~(sign | (exponent << 4) | mantissa);
     if (ZEROTRAP)
       if (ulawbyte == 0) ulawbyte = 0x02;  /* optional CCITT trap */
 
     if (ulawbyte < 0)
       ulawbyte += 256;
 
     return ulawbyte;
   }
 
   private class RingReader extends InputStream {
     private AudioStream stream;
     private RingBufferSA ringBuffer;
 
     public RingReader(RingBufferSA rb) {
       ringBuffer = rb;
       try {
         headerPos = 0;
         stream = new AudioStream(this);
       }
       catch (Exception e) {
         e.printStackTrace();
       }
     }
     public synchronized boolean play () {
       AudioPlayer.player.start(stream);
       return true;
     }
     public synchronized boolean pause () {
       AudioPlayer.player.stop(stream);
       return true;
     }
     public synchronized boolean stop () {
       AudioPlayer.player.stop(stream);
       return true;
     }
     public int read() throws IOException {
       int result;
       if (headerPos < header.length)
 	result = header[headerPos++];
       else
         result = ringBuffer.read();
  
       //System.out.println("read "+result);
       return result;
     }
   }
 
   private class RingBufferSA extends RingBuffer
   {
     private RingReader reader;
     private int devicePos;
     public int nextSeg;
 
     public RingBufferSA () {
       reader = new RingReader (this);
       devicePos = 0;
     }
 
     protected void startWriteThread () {}
     public synchronized boolean play () {
       boolean res;
       res = super.play();
       reader.play();
       return res;
     }
     public synchronized boolean pause () {
       boolean res;
       res = super.pause();
       reader.pause();
       return res;
     }
     public synchronized boolean stop () {
       boolean res;
       res = super.stop();
       reader.stop();
       return res;
     }
     public int read () {
       int ringPos;
 
       ringPos = (int)(devicePos * rateDiff) * bps;
 
       /* if we don't know the segment total yet, we need
        * to return -1 now */
       if (segTotal == 0)
         return -1;
 
       while (ringPos >= nextSeg) {
         //System.out.println ("read: devicePos: "+devicePos+" ringPos: "+ringPos+" nextSeg: "+nextSeg);
         synchronized (this) {
 	  clear ((int) (playSeg % segTotal));
           playSeg++;
           notifyAll();
         }
         nextSeg += segSize;
       }
 
       int sample = 0;
       int ptr = ringPos % buffer.length;
       for (int j=0; j<channels; j++) {
         int b1, b2;
 
         b1 = buffer[ptr  ];
         b2 = buffer[ptr+1];
         if (b2<0) b2+=256;
 	/* multiply because we need to keep the sign */
         sample += (b1 * 256) | b2;
 	ptr += 2;
       }
       sample /= channels;
 
       devicePos++;
 
       return toUlaw (sample);
     }
   }
 
   protected RingBuffer createRingBuffer() {
     return new RingBufferSA();
   }
 
   protected boolean open (RingBuffer ring) {
     rateDiff = ring.rate / 8000.0;
     Debug.log(Debug.INFO, "rateDiff: "+rateDiff);
 
     ring.segSize = (int) (SEGSIZE * rateDiff);
     ring.segSize = ring.segSize * ring.bps;
     ring.segTotal = (int) (BUFFER * rateDiff);
     ring.segTotal = ring.segTotal * ring.bps / ring.segSize;
     ring.emptySeg = new byte[ring.segSize];
     
     ((RingBufferSA)ring).nextSeg = ring.segSize;
     delay = DELAY;
 
     return true;
   }
 
   protected boolean close (RingBuffer ring)
   {
     return true;
   }
 
   protected int write (byte[] data, int offset, int length) {
     System.out.println("write should not be called");
     return -1;
   }
 
   protected long delay () {
     long ret = ((int)(delay * rateDiff));
     return ret;
   }
 
   protected void reset () {
   }
 
   public String getFactoryName ()
   {
     return "audiosinksa";
   }
 
 }
