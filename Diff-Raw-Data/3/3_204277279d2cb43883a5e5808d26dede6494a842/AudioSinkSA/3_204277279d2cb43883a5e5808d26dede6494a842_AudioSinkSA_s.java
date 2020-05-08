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
   private static final int SEGSIZE = 1024;
   private static final int DELAY = 16 * 1024;
 
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
   private static final byte[] header =
                          { 0x2e, 0x73, 0x6e, 0x64,              // header in be
                            0x00, 0x00, 0x00, 0x18,              // offset
                            0x7f,   -1,   -1,   -1,              // length
                            0x00, 0x00, 0x00, 0x01,              // ulaw
                            0x00, 0x00, 0x1f, 0x40,              // frequency
                            0x00, 0x00, 0x00, 0x01               // channels
                          };
   private boolean needHeader;
 
   private final byte toUlaw(int sample)
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
 
     return (byte) ulawbyte;
   }
 
   private class RingReader extends InputStream {
     private AudioStream stream;
     private RingBufferSA ringBuffer;
 
     public RingReader(RingBufferSA rb) {
       ringBuffer = rb;
       try {
         needHeader = true;
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
       return -1;
     }
 
     public int read(byte[] b) throws IOException {
       return -1;
     }
     public int read(byte[] b, int off, int len) throws IOException 
     {
       int ret;
 
       if (needHeader) {
         System.arraycopy (header, 0, b, off, header.length);
 	needHeader = false;
 	return header.length;
       }
       ret = ringBuffer.read (b, off, len);
       return ret;
     }
   }
 
   private class RingBufferSA extends RingBuffer
   {
     private RingReader reader;
     private int pos;
 
     public RingBufferSA () {
       reader = new RingReader (this);
       pos = 0;
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
     public int read (byte[] b, int off, int len) {
       int ptr = pos;
       int nextSeg = ((ptr / segSize) + 1) * segSize;
 
       //System.out.println ("read: ptr: "+ptr+" pos: "+pos+" len: "+len);
       for (int i=0; i < len; i++) {
 	ptr = pos + (int)(i * rateDiff) * bps; 
 
 	while (ptr >= nextSeg) {
           synchronized (this) {
             //System.out.println ("inc/clear: ptr: "+ptr+" playSeg: "+playSeg);
 	    clear ((int) (playSeg % segTotal));
             playSeg++;
             notifyAll();
           }
           nextSeg = ((ptr / segSize) + 1) * segSize;
 	}
 
         int sample = 0;
 	int ptr2 = ptr % buffer.length;
         for (int j=0; j<channels; j++) {
           int b1, b2;
 
           b1 = buffer[ptr2  ];
           b2 = buffer[ptr2+1];
           if (b2<0) b2+=256;
           sample += (b1 * 256) | b2;
 	  ptr2 += 2;
         }
         sample /= channels;
 
         b[off + i] = toUlaw (sample);
       }
       pos = ptr;
       //MemUtils.dump (b, off, len);
       return len;
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
