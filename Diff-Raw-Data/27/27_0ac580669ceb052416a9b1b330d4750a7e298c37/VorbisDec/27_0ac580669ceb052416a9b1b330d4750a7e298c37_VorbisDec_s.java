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
 
 import java.util.*;
 import com.jcraft.jogg.*;
 import com.jcraft.jorbis.*;
 import com.fluendo.jst.*;
 import com.fluendo.utils.*;
 
 public class VorbisDec extends Element implements OggPayload
 {
   private long packet;
   private long offset;
   private Info vi;
   private Comment vc;
   private DspState vd;
   private Block vb;
   private boolean discont;
 
   private Packet op;
   private float[][][] _pcmf = new float[1][][];
   private int[] _index;
 
   private static final byte[] signature = { 0x01, 0x76, 0x6f, 0x72, 0x62, 0x69, 0x73 };
 
   public boolean isType (Packet op)
   {
     return typeFind (op.packet_base, op.packet, op.bytes) > 0;
   }
   public int takeHeader (Packet op)
   {
     return vi.synthesis_headerin(vc, op);
   }
   public boolean isHeader (Packet op)
   {
     return (op.packet_base[op.packet] & 0x01) == 0x01;
   }
   public boolean isKeyFrame (Packet op)
   {
     return true;
   }
   public long getFirstTs (Vector packets)
   {
     int len = packets.size();
    int i, j;
    long time;
     long total = 0;
     long prevSamples = 0;
     Packet p = new Packet();
 
     com.fluendo.jst.Buffer buf;
 
     /* add samples */
     for (i=0; i<len; i++) {
       boolean ignore;
       long temp;
 
       buf = (com.fluendo.jst.Buffer) packets.elementAt(i);
 
       p.packet_base = buf.data;
       p.packet = buf.offset;
       p.bytes = buf.length;
 
       long samples = vi.blocksize(p);
       if (samples <= 0)
         return -1;
 
       if (prevSamples == 0 ) {
         prevSamples = samples;
         /* ignore first packet */
         ignore = true;
       }
       else
         ignore = false;
 
       temp = (samples + prevSamples) / 4;
       prevSamples = samples;
 
       if (!ignore)
         total += temp;
  
       if (buf.time_offset != -1) {
         total = buf.time_offset - total;
 	long result = granuleToTime (total);
 
         buf = (com.fluendo.jst.Buffer) packets.elementAt(0);
 	buf.timestamp = result;
         return result;
       }
     }
     return -1;
   }
   public long granuleToTime (long gp)
   {
     if (gp < 0)
       return -1;
 
     return gp * Clock.SECOND / vi.rate;
   }
 
   private Pad srcPad = new Pad(Pad.SRC, "src") {
     protected boolean eventFunc (com.fluendo.jst.Event event) {
       return sinkPad.pushEvent(event);
     }
   };
 
   private Pad sinkPad = new Pad(Pad.SINK, "sink") {
     protected boolean eventFunc (com.fluendo.jst.Event event) {
       boolean result;
 
       switch (event.getType()) {
         case Event.FLUSH_START:
           result = srcPad.pushEvent(event);
 	  synchronized (streamLock) {
 	    Debug.log(Debug.INFO, "synced "+this);
 	  }
 	  break;
         case Event.FLUSH_STOP:
           result = srcPad.pushEvent(event);
 	  break;
         default:
           result = srcPad.pushEvent(event);
 	  break;
       }
       return result;
     }
     protected int chainFunc (com.fluendo.jst.Buffer buf) {
       int result = OK;
       long timestamp;
 
       //System.out.println ("creating packet");
       op.packet_base = buf.data;
       op.packet = buf.offset;
       op.bytes = buf.length;
       op.b_o_s = (packet == 0 ? 1 : 0);
       op.e_o_s = 0;
       op.packetno = packet;
 
       if (buf.isFlagSet (com.fluendo.jst.Buffer.FLAG_DISCONT)) {
 	offset = -1;
 	discont = true;
         Debug.log(Debug.INFO, "vorbis: got discont");
 	vd.synthesis_init(vi);
       }
 
       if (packet < 3) {
         //System.out.println ("decoding header");
         if(vi.synthesis_headerin(vc, op) < 0){
 	  // error case; not a vorbis header
 	  Debug.log(Debug.ERROR, "This Ogg bitstream does not contain Vorbis audio data.");
 	  return ERROR;
         }
         if (packet == 2) {
 	  vd.synthesis_init(vi);
 	  vb.init(vd);
 
 	  Debug.log(Debug.INFO, "vorbis rate: "+vi.rate);
 	  Debug.log(Debug.INFO, "vorbis channels: "+vi.channels);
 
           _index =new int[vi.channels];
 
 	  caps = new Caps ("audio/raw");
 	  caps.setFieldInt ("width", 16);
 	  caps.setFieldInt ("depth", 16);
 	  caps.setFieldInt ("rate", vi.rate);
 	  caps.setFieldInt ("channels", vi.channels);
         }
         buf.free();
         packet++;
 
 	return OK;
       }
       else {
         if (isHeader(op)) {
           Debug.log(Debug.INFO, "ignoring header");
 	  return OK;
 	}
 
         timestamp = buf.timestamp;
         if (timestamp != -1) {
 	  offset = timestamp * vi.rate / Clock.SECOND;
         }
 	else {
           timestamp = offset * Clock.SECOND / vi.rate;
 	}
 	
         int samples;
         if (vb.synthesis(op) == 0) { // test for success!
           vd.synthesis_blockin(vb);
         }
         else {
           Debug.log(Debug.ERROR, "decoding error");
 	  return ERROR;
         }
         //System.out.println ("decode vorbis done");
         while ((samples = vd.synthesis_pcmout (_pcmf, _index)) > 0) {
           float[][] pcmf=_pcmf[0];
 	  int numbytes = samples * 2 * vi.channels;
 	  int k = 0;
 
 	  buf.ensureSize(numbytes);
 	  buf.offset = 0;
 	  buf.timestamp = timestamp;
 	  buf.time_offset = offset;
 	  buf.length = numbytes;
 	  buf.caps = caps;
 	  buf.setFlag (com.fluendo.jst.Buffer.FLAG_DISCONT, discont);
 	  discont = false;
 
 	  //System.out.println(vi.rate + " " +target+ " " +samples);
 
           for (int j=0; j<samples; j++){
             for (int i=0; i<vi.channels; i++) {
 	       int val = (int) (pcmf[i][_index[i]+j] * 32767.0);
 	       if (val > 32767)
 	         val = 32767;
 	       else if (val < -32768)
 	         val = -32768;
 
                buf.data[k] = (byte) ((val >> 8) & 0xff);
                buf.data[k+1] = (byte) (val & 0xff);
 	       k+=2;
 	    }
           }
           //System.out.println ("decoded "+samples+" samples");
           vd.synthesis_read(samples);
 
 	  offset += samples;
 
           if ((result = srcPad.push(buf)) != OK)
             break;
         }
       }
       packet++;
 
       return result;
     }
   };
 
   public VorbisDec() {
     super();
 
     vi = new Info();
     vc = new Comment();
     vd = new DspState();
     vb = new Block(vd);
     op = new Packet();
 
     addPad (srcPad);
     addPad (sinkPad);
   }
 
   protected int changeState (int transition) {
     int res;
 
     switch (transition) {
       case STOP_PAUSE:
         packet = 0;
 	offset = -1;
         vi.init();
         vc.init();
         break;
       default:
         break;
     }
 
     res = super.changeState (transition);
 
     return res;
   }
 
   public String getFactoryName ()
   {
     return "vorbisdec";
   }
   public String getMime ()
   {
     return "audio/x-vorbis";
   }
   public int typeFind (byte[] data, int offset, int length)
   {
     if (length < signature.length)
       return -1;
 
     for (int i=0; i < signature.length; i++) {
       if (data[offset+i] != signature[i])
         return -1;
     }
     return 10;
   }
 }
