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
 import java.net.*;
 import com.fluendo.jst.*;
 import com.fluendo.utils.*;
 
 public class HTTPSrc extends Element
 {
   private String userId;
   private String password;
   private String urlString;
   private InputStream input;
   private long contentLength;
   private String mime;
   private Caps outCaps;
   private boolean discont;
   private URL documentBase;
 
   private static final int DEFAULT_READSIZE = 4096;
 
   private int readSize = DEFAULT_READSIZE;
 
   private Pad srcpad = new Pad(Pad.SRC, "src") {
     private boolean doSeek (Event event) {
       boolean result;
       int format;
       long position;
 
       format = event.parseSeekFormat();
       position = event.parseSeekPosition();
 
       if (format == Format.PERCENT) {
         position = position * contentLength / Format.PERCENT_MAX;
       }
       else if (format != Format.BYTES) {
         Debug.log (Debug.WARNING, "can only seek in bytes");
         return false;
       }
 
       pushEvent (Event.newFlushStart());
 
       synchronized (streamLock) {
         Debug.log(Debug.INFO, "synced "+this);
 
         try {
           input = getInputStream (position);
           result = true;
         }
         catch (Exception e) {
 	  e.printStackTrace ();
           result = false;
         }
         pushEvent (Event.newFlushStop());
 
         pushEvent (Event.newNewsegment(false, Format.BYTES, position, contentLength, position));
 
         if (result) {
 	  postMessage (Message.newStreamStatus (this, true, Pad.OK, "restart after seek"));
 	  result = startTask();
 	}
       }
       return result;
     }
 
     protected boolean eventFunc (Event event)
     {
       boolean res;
 
       switch (event.getType()) {
         case Event.SEEK:
 	  res = doSeek(event);
 	  break;
         default:
           res = super.eventFunc (event);
           break;
       }
       return res;
     }
 
     protected void taskFunc()
     {
       int ret;
 
       Buffer data = Buffer.create();
       data.ensureSize (readSize);
       data.offset = 0;
       try {
         data.length = input.read (data.data, 0, readSize);
       }
       catch (Exception e) {
 	e.printStackTrace();
         data.length = 0;
       }
       if (data.length <= 0) {
 	/* EOS */
 	data.free();
         Debug.log(Debug.INFO, this+" reached EOS");
 	pushEvent (Event.newEOS());
 	postMessage (Message.newStreamStatus (this, false, Pad.UNEXPECTED, "reached EOS"));
 	pauseTask();
       }
       else {
         if (srcpad.getCaps() == null) {
 	  String typeMime;
 
 	  typeMime = ElementFactory.typeFindMime (data.data, data.offset, data.length);
 	  if (typeMime != null) {
 	    if (!typeMime.equals (mime)) {
               Debug.log(Debug.WARNING, "server contentType: "+mime+" disagrees with our typeFind: "
 	                 +typeMime);
 	    }
             Debug.log(Debug.INFO, "using typefind contentType: "+typeMime);
 	    mime = typeMime;
 	  }
 	  else {
             Debug.log(Debug.INFO, "typefind failed, using server contentType: "+mime);
 	  }
 
           outCaps = new Caps (mime);
           srcpad.setCaps (outCaps);
         }
         data.caps = outCaps;
 	data.setFlag (com.fluendo.jst.Buffer.FLAG_DISCONT, discont);
 	discont = false;
         if ((ret = push(data)) != OK) {
 	  if (isFlowFatal(ret) || ret == Pad.NOT_LINKED) {
 	    postMessage (Message.newError (this, "error: "+getFlowName (ret)));
	    pushEvent (Event.newEOS());
 	  }
 	  postMessage (Message.newStreamStatus (this, false, ret, "reason: "+getFlowName (ret)));
 	  pauseTask();
         }
       }
     }
     
     protected boolean activateFunc (int mode)
     {
       boolean res = true;
 
       switch (mode) {
         case MODE_NONE:
 	  postMessage (Message.newStreamStatus (this, false, Pad.WRONG_STATE, "stopping"));
 	  res = stopTask();
 	  input = null;
 	  outCaps = null;
 	  mime = null;
 	  break;
         case MODE_PUSH:
 	  try {
 	    input = getInputStream(0); 
 	    if (input == null)
 	      res = false;
 	  }
 	  catch (Exception e) {
 	    res = false;
 	  }
 	  if (res) {
 	    postMessage (Message.newStreamStatus (this, true, Pad.OK, "activating"));
 	    res = startTask();
 	  }
 	  break;
 	default:
 	  res = false;
 	  break;
       }
       return res;
     }
   };
 
   private InputStream getInputStream (long offset) throws Exception
   {
     InputStream dis = null;
 
     try {
       URL url;
       postMessage(Message.newResource (this, "Opening "+urlString));
       Debug.log(Debug.INFO, "reading from url "+urlString);
       if (documentBase != null)
         url = new URL(documentBase, urlString);
       else
         url = new URL(urlString);
       Debug.log(Debug.INFO, "trying to open "+url);
       URLConnection uc = url.openConnection();
       if (userId != null && password != null) {
         String userPassword = userId + ":" + password;
         String encoding = Base64Converter.encode (userPassword.getBytes());
         uc.setRequestProperty ("Authorization", "Basic " + encoding);
       }
       uc.setRequestProperty ("Range", "bytes=" + offset+"-");
       /* FIXME, do typefind ? */
       dis = uc.getInputStream();
       contentLength = uc.getHeaderFieldInt ("Content-Length", 0) + offset;
 
       mime = uc.getContentType();
 
       discont = true;
 
       Debug.log(Debug.INFO, "opened "+url);
       Debug.log(Debug.INFO, "contentLength: "+contentLength);
       Debug.log(Debug.INFO, "server contentType: "+mime);
     }
     catch (SecurityException e) {
       e.printStackTrace();
       postMessage(Message.newError (this, "Not allowed "+urlString+"..."));
     }
     catch (Exception e) {
       e.printStackTrace();
       postMessage(Message.newError (this, "Failed opening "+urlString+"..."));
     }
 
     return dis;
   }
 
   public String getFactoryName () {
     return "httpsrc";
   }
 
   public HTTPSrc () {
     super ();
     addPad (srcpad);
   }
 
   public synchronized boolean setProperty(String name, java.lang.Object value) {
     boolean res = true;
 
     if (name.equals("url")) {
       urlString = String.valueOf(value);
     }
     else if (name.equals("documentBase")) {
       documentBase = (URL)value;
     }
     else if (name.equals("userId")) {
       userId = String.valueOf(value);
     }
     else if (name.equals("password")) {
       password = String.valueOf(value);
     }
     else if (name.equals("readSize")) {
       readSize = Integer.parseInt((String)value);
     }
     else {
       res = false;
     }
     return res;
   }
 }
