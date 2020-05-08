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
 import com.fluendo.jst.*;
 import com.fluendo.utils.*;
 
 public class OggDemux extends Element
 {
   private SyncState oy;
   private OggChain chain;
   private Page og;
   private Packet op;
   private static final byte[] signature = { 0x4f, 0x67, 0x67, 0x53 };
 
   private OggPayload payloads[] = {
     new TheoraDec(),
     new VorbisDec()
   };
 
   class OggStream extends Pad {
     public int serialno;
     public StreamState os;
     private Vector headers;
     public boolean haveHeaders;
     public Vector queue;
     public boolean started;
     public boolean complete;
     public boolean discont;
     public boolean active;
     public boolean haveKeyframe;
     public boolean sentHeaders;
     
     private OggPayload payload;
 
     public OggStream (int serial) {
       super (Pad.SRC, "serial_"+serial);
 
       serialno = serial;
       os = new StreamState();
       os.init(serial);
       os.reset();
       queue = new Vector();
       headers = new Vector();
       haveHeaders = false;
       haveKeyframe = false;
       payload = null;
       discont = true;
       complete = false;
       started = false;
     }
 
     public void markDiscont () {
       discont = true;
       complete = false;
       haveKeyframe = false;
       started = false;
     }
     public void reset () {
       markDiscont();
       os.reset();
     }
 
     public boolean isComplete () {
       return complete;
     }
     public void activate() {
       if (active)
         return;
 
       sentHeaders = false;
       addPad(this);
       active = true;
     }
     public void deActivate() {
       if (!active)
         return;
       removePad(this);
       pushEvent (Event.newEOS());	
       active = false;
     }
     public void reStart(long firstTs) {
       if (!active)
         return;
 
       pushEvent (Event.newNewsegment (false, Format.TIME, firstTs, -1, firstTs));
 
       if (!sentHeaders) {
         for (int i=0; i<headers.size(); i++) {
           com.fluendo.jst.Buffer buf = (com.fluendo.jst.Buffer) headers.elementAt(i);
           buf.setFlag (com.fluendo.jst.Buffer.FLAG_DISCONT, discont);
 	  discont = false;
 	  push (buf);
         }
 	sentHeaders = true;
       }
       for (int i=0; i<queue.size(); i++) {
         com.fluendo.jst.Buffer buf = (com.fluendo.jst.Buffer) queue.elementAt(i);
         buf.setFlag (com.fluendo.jst.Buffer.FLAG_DISCONT, discont);
 	discont = false;
 	push (buf);
       }
       queue.setSize(0);
       started = true;
     }
 
     public long getFirstTs () {
       return payload.getFirstTs (queue);
     }
 
     private com.fluendo.jst.Buffer bufferFromPacket (Packet op)
     {
       com.fluendo.jst.Buffer data = com.fluendo.jst.Buffer.create();
 
       data.copyData(op.packet_base, op.packet, op.bytes);
       data.time_offset = op.granulepos;
       if (payload != null)
         data.timestamp = payload.granuleToTime (op.granulepos);
       else
         data.timestamp = -1;
       data.setFlag (com.fluendo.jst.Buffer.FLAG_DISCONT, discont);
       data.setFlag (com.fluendo.jst.Buffer.FLAG_DELTA_UNIT, !payload.isKeyFrame(op));
 
       return data;
     }
 
     public int pushPacket (Packet op) {
       if (payload == null) {
         for (int i=0; i<payloads.length; i++) {
 	  OggPayload pl = payloads[i];
 
 	  if (pl.isType (op)) {
             try {
 	      payload = (OggPayload) pl.getClass().newInstance();
 	      break;
 	    }
 	    catch (Exception e) {}
 	  }
 	}
 	if (payload == null) {
           Debug.log(Debug.INFO, "unknown stream "+serialno);
           postMessage (Message.newError (this, "unknown stream"));
 	  return Pad.ERROR;
 	}
 
         String mime = payload.getMime();
         Debug.log(Debug.INFO, "new stream "+serialno+", mime "+mime);
       
         setCaps (new Caps (mime));
       }
       if (!haveHeaders && payload.isHeader(op)) {
         if (payload.takeHeader (op) < 0) {
           postMessage (Message.newError (this, "cannot read header"));
 	  return Pad.ERROR;
 	}
 
         com.fluendo.jst.Buffer data = bufferFromPacket (op);
         headers.addElement(data);
       }
       else {
         haveHeaders = true;
       }
 
       if (haveHeaders) {
         if (complete && started) {
           com.fluendo.jst.Buffer data = bufferFromPacket (op);
 	  return push (data);
 	}
         if (haveKeyframe || payload.isKeyFrame(op)) {
           com.fluendo.jst.Buffer data = bufferFromPacket (op);
 	  queue.addElement (data);
 	  haveKeyframe = true;
 	  if (op.granulepos != -1) {
 	    complete = true;
 	  }
 	}
       }
       return Pad.OK;
     }
 
     public int pushPage (Page og) {
       int res;
       int flowRet = Pad.OK;
 
       res = os.pagein(og);
       if (res < 0) {
         // error; stream version mismatch perhaps
         System.err.println("Error reading first page of Ogg bitstream data.");
         postMessage (Message.newError (this, "Error reading first page of Ogg bitstream data."));
         return ERROR;
       }
       while (flowRet == OK) {
 	res = os.packetout(op);
         if(res == 0)
 	  break; // need more data
         if(res == -1) { 
 	  // missing or corrupt data at this page position
           // no reason to complain; already complained above
 	  Debug.log(Debug.WARNING, "ogg error: packetout gave "+res);
 	  discont = true;
         }
         else {
 	  flowRet = pushPacket(op);
         }
       }
       return flowRet;
     }
     protected boolean eventFunc (com.fluendo.jst.Event event) {
       return sinkPad.pushEvent (event);
     }
   }
 
   class OggChain {
     private Vector streams;
     private boolean active;
     private boolean synced;
     private long firstTs;
 
     public OggChain () {
       streams = new Vector();
       synced = false;
       active = false;
       firstTs = -1;
     }
 
     public boolean isActive() {
       return active;
     }
 
     public void activate() {
       if (active)
         return;
 
       Debug.log(Debug.INFO, "activating chain");
       for (int i=0; i<streams.size(); i++) {
         OggStream stream = (OggStream) streams.elementAt(i);
 	stream.activate();
       }
       active = true;
       noMorePads();
     }
     public void deActivate() {
       if (!active)
         return;
       Debug.log(Debug.INFO, "deActivating chain");
       for (int i=0; i<streams.size(); i++) {
         OggStream stream = (OggStream) streams.elementAt(i);
 	stream.deActivate();
       }
       active = false;
     }
     public void reStart() {
       if (!active)
         return;
 
       if (firstTs == -1) {
         long maxTs = 0;
         long minTs = Long.MAX_VALUE;
         /* collect first timestamp */
         for (int i=0; i<streams.size(); i++) {
           OggStream stream = (OggStream) streams.elementAt(i);
 	  long ts = stream.getFirstTs();
 	  maxTs = Math.max (maxTs, ts);
 	  minTs = Math.min (minTs, ts);
         }
 	firstTs = maxTs;
       }
       for (int i=0; i<streams.size(); i++) {
         OggStream stream = (OggStream) streams.elementAt(i);
 	stream.reStart(firstTs);
       }
     }
 
 
     public void addStream (OggStream stream) {
       streams.addElement (stream);
     }
 
     public void markDiscont () {
       synced = false;
       firstTs = -1;
       for (int i=0; i<streams.size(); i++) {
 	OggStream stream = (OggStream) streams.elementAt(i);
 	stream.markDiscont();
       }
     }
 
     public OggStream findStream (int serial) {
       OggStream stream = null;
       for (int i=0; i<streams.size(); i++) {
         stream = (OggStream) streams.elementAt(i);
         if (stream.serialno == serial)
           break;
         stream = null;
       }
       return stream;
     }
     public void resetStreams ()
     {
       for (int i=0; i<streams.size(); i++) {
 	OggStream stream = (OggStream) streams.elementAt(i);
 	stream.reset();
       }
     }
     public boolean forwardEvent (com.fluendo.jst.Event event)
     {
       for (int i=0; i<streams.size(); i++) {
 	OggStream stream = (OggStream) streams.elementAt(i);
 	stream.pushEvent (event);
       }
       return true;
     }
 
     public int pushPage (Page og, OggStream stream) {
       int flowRet = Pad.OK;
 
       flowRet = stream.pushPage (og);
 
       /* now check if all streams are Synced */
       if (!synced) {
         boolean check = true;
         for (int i=0; i<streams.size(); i++) {
 	  OggStream cstream = (OggStream) streams.elementAt(i);
 
 	  if (!(check = cstream.isComplete()))
 	    break;
         }
 	if (check) {
           Debug.log(Debug.INFO, "steams synced");
 	  activate();
 	  reStart();
 	  synced = true;
 	}
       }
       return flowRet;
     }
   }
 
   private Pad sinkPad = new Pad(Pad.SINK, "sink") {
     protected boolean eventFunc (com.fluendo.jst.Event event)
     {
       switch (event.getType()) {
         case Event.FLUSH_START:
          if (chain)
	    chain.forwardEvent (event);
 	  synchronized (streamLock) {
             Debug.log(Debug.INFO, "synced "+this);
 	  }
 	  break;
         case Event.FLUSH_STOP:
 	  oy.reset();
          if (chain) {
	    chain.resetStreams();
	    chain.forwardEvent (event);
	  }
 	  break;
         case Event.NEWSEGMENT:
 	  break;
         case Event.EOS:
 	  Debug.log(Debug.INFO, "ogg: got EOS");
          if (chain) {
	    chain.forwardEvent (event);
	  else
            postMessage (Message.newError (this, "no streams found"));
 	  break;
         default:
          if (chain) {
	    chain.forwardEvent (event);
 	  break;
       }
       return true;
     }
     protected int chainFunc (com.fluendo.jst.Buffer buf)
     {
       int res;
       int flowRet = OK;
 
       int index = oy.buffer(buf.length);
 
       if (buf.isFlagSet (com.fluendo.jst.Buffer.FLAG_DISCONT)) {
 	Debug.log(Debug.INFO, "ogg: got discont");
 	if (chain != null) {
 	  chain.markDiscont ();
 	}
       }
 
       System.arraycopy(buf.data, buf.offset, oy.data, index, buf.length);
       oy.wrote(buf.length);
   
       while (flowRet == OK) {
         res = oy.pageout(og);
         if (res == 0)
 	  break; // need more data
         if(res == -1) { 
 	  // missing or corrupt data at this page position
           // no reason to complain; already complained above
 	  Debug.log(Debug.WARNING, "ogg: pageout gave "+res);
 	  if (chain != null) {
 	    chain.markDiscont ();
 	  }
         }
         else {
 	  int serial = og.serialno();
 	  OggStream stream = null;
 	  if (chain != null) {
 	    stream = chain.findStream (serial);
 	  }
 	  if (stream == null) {
 	    if (chain != null) {
 	      if (chain.isActive()) {
 	        chain.deActivate();
 	        chain = null;
 	      }
 	    }
             if (chain == null)
 	      chain = new OggChain();
 
 	    stream = new OggStream(serial);
 	    chain.addStream (stream);
 	  }
 	  flowRet = chain.pushPage (og, stream);
         }
       }
       return flowRet;
     }
     protected boolean activateFunc (int mode) 
     {
       if (mode == MODE_NONE) {
 	oy.reset();
 	chain.resetStreams();
       }
       return true;
     }
   };
 
   public String getFactoryName ()
   {
     return "oggdemux";
   }
   public String getMime ()
   {
     return "application/ogg";
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
 
   public OggDemux () {
     super ();
 
     oy = new SyncState();
     og = new Page();
     op = new Packet();
 
     chain = null;
 
     addPad (sinkPad);
   }
 }
