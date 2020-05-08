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
 
 package com.fluendo.jst;
 
 import com.fluendo.utils.*;
 
 public abstract class Sink extends Element
 {
   private java.lang.Object prerollLock = new java.lang.Object();
   private boolean isEOS;
   private boolean flushing;
   private boolean havePreroll;
   private boolean needPreroll;
   private Clock.ClockID clockID;
   protected boolean discont; 
   protected long segStart;
   protected long segStop;
   protected long segPosition;
   
   protected Pad sinkpad = new Pad(Pad.SINK, "sink") {
     private int finishPreroll(Buffer buf)
     {
       synchronized (prerollLock) {
         int res = OK;
 	Sink sink = (Sink) parent;
 
 	if (isFlushing()) {
 	  return WRONG_STATE;
 	}
 
         if (needPreroll) {
 
 	  havePreroll = true;
           preroll (buf);
 
 	  boolean postPause = false;
 	  boolean postPlaying = false;
 	  int current, next, pending, postPending;
 
 	  synchronized (sink) {
 	    current = currentState;
 	    next = nextState;
 	    pending = pendingState;
 	    postPending = pending;
 
 	    switch (pending) {
 	      case PLAY:
 	        needPreroll = false;
 		postPlaying = true;
 		if (current == STOP)
 		  postPause = true;
 		break;
 	      case PAUSE:
 	        needPreroll = true;
 		postPause = true;
 		postPending = NONE;
 		break;
 	      case STOP:
 	        havePreroll = false;
 	        needPreroll = false;
 	        return WRONG_STATE;
 	    }
 	    if (pending != NONE) {
 	      currentState = pending;
 	      nextState = NONE;
 	      pendingState = NONE;
 	      lastReturn = SUCCESS;
 	    }
 	  }
 
 	  if (postPause)
 	    postMessage (Message.newStateChanged (this, current, next, postPending));
 	  if (postPlaying)
 	    postMessage (Message.newStateChanged (this, next, pending, NONE));
 
 	  if (postPause || postPlaying)
 	    postMessage (Message.newStateDirty (this));
 
 	  synchronized (sink) {
 	    sink.notifyAll();
 	  }
 
 	  if (needPreroll) {
 	    needPreroll = false;
 	    try {
 	      prerollLock.wait();
 	    }
 	    catch (InterruptedException ie) {}
 
 	    havePreroll = false;
 	  }
 	}
 	if (isFlushing())
 	  return WRONG_STATE;
 
 	return res;
       }
     }
 
     protected boolean eventFunc (Event event)
     {
       Sink sink = (Sink) parent;
       doEvent(event);
 
       switch (event.getType()) {
         case Event.FLUSH_START:
 	  synchronized (sink) {
 	    sink.flushing = true;
 	    if (clockID != null) {
 	      clockID.unschedule();
 	    }
 	  }
 	  synchronized (prerollLock) {
 	    sink.isEOS = false;
 	    needPreroll = true;
 	    prerollLock.notify();
 	    havePreroll = false;
 	  }
 	  synchronized (streamLock) {
 	    Debug.log(Debug.DEBUG, this+" flushed "+havePreroll+" "+needPreroll);
 	    lostState();
 	  }
 	  break;
         case Event.FLUSH_STOP:
 	  synchronized (streamLock) {
 	    sink.flushing = false;
 	  }
 	  break;
         case Event.NEWSEGMENT:
 	  synchronized (streamLock) {
 	    int segFmt = event.parseNewsegmentFormat();
 	    if (segFmt == Format.TIME) {
 	      segStart = event.parseNewsegmentStart();
 	      segStop = event.parseNewsegmentStop();
 	      segPosition = event.parseNewsegmentPosition();
 	    }
 	  }
 	  break;
         case Event.EOS:
 	  synchronized (streamLock) {
 	    isEOS = true;
 	    Debug.log(Debug.INFO, this+" got EOS");
 	    postMessage (Message.newEOS (this));
 	  }
 	  break;
 	default:
 	  break;
       }
 
       return true;
     }
   
     protected int chainFunc (Buffer buf)
     {
       int res;
       int status;
       long time;
 
       if (buf.isFlagSet (com.fluendo.jst.Buffer.FLAG_DISCONT))
         discont = true;
 
       time = buf.timestamp;
       /* clip to segment */
       if (time != -1 && time < segStart) {
 	buf.free();
         return OK;
       }
       buf.setFlag (com.fluendo.jst.Buffer.FLAG_DISCONT, discont);
       discont = false;
 
      if ((res = finishPreroll(buf)) != Pad.OK)
         return res;
 
       status = doSync(time);
       switch (status) {
         case Clock.EARLY:
         case Clock.OK:
           res = render (buf);
 	  break;
 	default:
 	  res = Pad.OK;
 	  break;
       }
       buf.free();
 
       return res;
     }
 
     protected boolean setCapsFunc (Caps caps)
     {
       boolean res;
       Sink sink = (Sink) parent;
       
       res = sink.setCapsFunc (caps);
 
       return res;
     }
     protected boolean activateFunc (int mode)
     {
       if (mode == MODE_NONE) {
         synchronized (prerollLock) {
 	  if (havePreroll) {
 	    prerollLock.notify();
 	  }
 	  needPreroll = false;
 	  havePreroll = false;
 	  flushing = true;
 	}
 	isEOS = false;
       }
       else {
 	flushing = false;
       }
       return true;
     }
   };
 
   protected int preroll (Buffer buf) {
     return Pad.OK;
   }
 
   protected boolean doEvent(Event event)
   {
     return true;
   }
 
   protected int doSync(long time) {
     int ret;
     Clock.ClockID id = null;
 
     synchronized (this) {
       if (flushing)
         return Clock.UNSCHEDULED;
 
       if (time == -1)
         return Clock.OK;
 
       time = time - segStart + baseTime;
 
       if (clock != null)
         id = clockID = clock.newSingleShotID (time);
     }
     
     if (id != null) {
       ret = id.waitID();
     }
     else
       ret = Clock.OK;
 
     synchronized (this) {
       clockID = null;
     }
     return ret;
   }
   protected boolean setCapsFunc (Caps caps) {
     return true;
   }
 
   protected int render (Buffer buf) {
     return Pad.OK;
   }
 
   public Sink () {
     super ();
     addPad (sinkpad);
     setFlag (Element.FLAG_IS_SINK);
   }
 
   public boolean sendEvent (Event event) {
     return sinkpad.pushEvent (event);
   }
 
   public boolean query (Query query) {
     switch (query.getType()) {
       case Query.DURATION:
         return sinkpad.getPeer().query (query);
       case Query.POSITION:
       {
 	long position = -1;
         if (query.parsePositionFormat() == Format.TIME) {
           synchronized (this) {
 	    if (currentState == PLAY) {
 	      if (clock != null) {
 	        position = clock.getTime() - baseTime + segPosition;
 	      }
 	    }
 	    else {
 	      position = segPosition;
 	    }
 	  }
 	  query.setPosition(Format.TIME, position);
 	}
 	else {
           return sinkpad.getPeer().query (query);
 	}
         break;
       }
       default:
         return sinkpad.getPeer().query (query);
     }
     return true;
   }
 
   protected int changeState (int transition) {
     int result = SUCCESS;
     int presult;
 
     switch (transition) {
       case STOP_PAUSE:
 	this.isEOS = false;
         synchronized (prerollLock) {
           needPreroll = true;
           havePreroll = false;
         }
         result = ASYNC;
         break;
       case PAUSE_PLAY:
         synchronized (prerollLock) {
           if (havePreroll) {
             needPreroll = false;
 	    prerollLock.notify();
 	  }
 	  else {
             needPreroll = false;
 	  }
 	}
         break;
       default:
         break;
     }
 
     presult = super.changeState(transition);
     if (presult == FAILURE)
       return presult;
 
     switch (transition) {
       case PLAY_PAUSE:
       {
         boolean checkEOS;
 
         /* unlock clock */
         synchronized (this) {
 	  if (clockID != null) {
 	    clockID.unschedule();
 	  }
 	  checkEOS = this.isEOS;
 	}
         synchronized (prerollLock) {
 	  if (!havePreroll && !checkEOS && pendingState == PAUSE) {
 	    needPreroll = true;
 	    result = ASYNC;
 	  }
 	}
         break;
       }
       case PAUSE_STOP:
         break;
       default:
         break;
     }
 
     return result;
   }
 }
