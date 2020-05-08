 
 /*
 * ChannelPool.java            $Revision: 1.2 $ $Date: 2001/04/19 16:16:31 $
  *
  * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
  *
  * The contents of this file are subject to the Blocks Public License (the
  * "License"); You may not use this file except in compliance with the License.
  *
  * You may obtain a copy of the License at http://www.invisible.net/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  */
 package org.beepcore.beep.lib;
 
 
 import org.beepcore.beep.core.BEEPException;
 import org.beepcore.beep.core.DataListener;
 import org.beepcore.beep.core.Session;
 
 import java.util.LinkedList;
 import java.util.NoSuchElementException;
 import java.util.Date;
 import java.util.Iterator;
 
 import org.beepcore.beep.util.Log;
 
 
 /**
  * <code>ChannelPool</code> holds a collection of available
  * <code>SharedChannel</code>(s) and provides access to them.  Availabe
  * <code>SharedChannel</code>(s) are retrieved through the
  * <code>getSharedChannel</code>.  Each <code>SharedChannel</code> has a time
  * to live, after which, it is removed from the pool.
  *
  * @author Eric Dixon
  * @author Huston Franklin
  * @author Jay Kint
  * @author Scott Pead
 * @version $Revision: 1.2 $, $Date: 2001/04/19 16:16:31 $
  */
 public class ChannelPool {
 
     /**
      * @todo add tuning mechanisms, try to figure out what the average
      * channel reuse is and adjust ttl accordingly
      */
     private static final long DEFAULT_TIME_TO_LIVE = 120000;    // two minutes
     private long timeToLive = DEFAULT_TIME_TO_LIVE;
     Session session;
     LinkedList availableChannels;
 
     /**
      * Creates a <code>ChannelPool</code> with the given session.
      *
      * @param session The <code>session</code> on which all
      * <code>SharedChannel</code>(s) returned from <code>getSharedChannel</code>
      * are started.
      * @see #getSharedChannel
      */
     public ChannelPool(Session session)
     {
         this.session = session;
         this.availableChannels = new LinkedList();
     }
 
     /**
      * Creates a <code>ChannelPool</code> with the given session and time to
      * live.
      *
      * @param session The <code>session</code> on which all
      * <code>SharedChannel</code>(s) returned from <code>getSharedChannel</code>
      * are started.
      * @param ttl The time in milleseconds an available <code>SharedChannel</code>
      * will live in <code>ChannelPool</code>
      * @see #getSharedChannel
      */
     public ChannelPool(Session session, long ttl)
     {
         this.session = session;
         this.timeToLive = ttl;
         this.availableChannels = new LinkedList();
     }
 
     /**
      * Returns a <code>SharedChannel</code> which supports the specified
      * <code>profile</code>.
      *
      * @param profile Name of a profile for the requested
      * <code>SharedChannel</code>.
      *
      * @return A <code>SharedChannel</code> for the requested profile.
      *
      * @throws BEEPException
      */
     public SharedChannel getSharedChannel(String profile) throws BEEPException
     {
         SharedChannel sharedCh = null;
         boolean found = false;
 
         synchronized (availableChannels) {
             Iterator i = availableChannels.iterator();
 
             while (i.hasNext()) {
                 sharedCh = (SharedChannel) i.next();
 
                 if (sharedCh.getProfile().equals(profile)) {
                     Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                                  "Found an available channel for sharing");
                     i.remove();
 
                     found = true;
 
                     break;
                 }
             }
         }
 
         // nothing found, so create one and return it
         if (!found) {
             sharedCh =
                 new SharedChannel(this.session.startChannel(profile, null),
                                   this);
         }
 
         // clean up channels that have expired
         garbageCollect();
         Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                      "Sharing channel number:" + sharedCh.getNumber());
 
         return sharedCh;
     }
 
     /**
      * Returns a <code>SharedChannel</code> which supports the specified
      * <code>profile</code> and calls back on the specified
      * <code>DataListener</code>.  Once it is no longer needed, call
      * <code>release</code> on the <code>SharedChannel</code>
      * to return it to the pool of available channels.
      *
      * @param profile Name of profile for the requested
      * <code>SharedChannel</code>.
      * @param listener <code>DataListener</code> for the requested
      * <code>SharedChannel</code>.
      *
      * @return A <code>SharedChannel</code>.
      *
      * @see DataListener
      * @see SharedChannel
      *
      * @throws BEEPException
      */
     synchronized public SharedChannel getSharedChannel(String profile, DataListener listener)
             throws BEEPException
     {
         SharedChannel sharedCh = null;
         boolean found = false;
 
         synchronized (availableChannels) {
             Iterator i = availableChannels.iterator();
 
             while (i.hasNext()) {
                 sharedCh = (SharedChannel) i.next();
 
                 if (sharedCh.getProfile().equals(profile)) {
                     Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                                  "Found an available channel for sharing");
                     i.remove();
 
                     found = true;
 
                     break;
                 }
             }
         }
 
         // nothing found, so create one and return it
         if (!found) {
             sharedCh =
                 new SharedChannel(this.session.startChannel(profile, listener),
                                   this);
         }
 
         // clean up channels that have expired
         garbageCollect();
         Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                      "Sharing channel number:" + sharedCh.getNumber());
 
         return sharedCh;
     }
 
     /**
      * Called from <code>SharedChannel</code>.  Releases the sharedCh and adds
      * it to the list of available SharedChannels.
      */
     void releaseSharedChannel(SharedChannel sharedCh)
     {
 
         // this channel is available to share
         synchronized (availableChannels) {
             availableChannels.add(sharedCh);
         }
 
         garbageCollect();
     }
 
     /**
      * Sets the time to live or the number of milleseconds an unused channel will
      * remain in the pool before it is removed from the pool.
      * The default time to live is two minutes.
      *
      * @param ttl The time this channel has to live in the pool while in an
      * available state.
      */
     public void setSharedChannelTTL(long ttl)
     {
         this.timeToLive = ttl;
     }
 
     /**
      * Identifies SharedChannels that have exceeded their ttl, removes the from
      * the list of availableChannels, and closes them.
      */
     private void garbageCollect()
     {
         Log.logEntry(Log.SEV_DEBUG_VERBOSE, "garbage collecting");
 
         if (availableChannels.size() != 0) {
             Date now = new Date();
             long ttl = now.getTime() - timeToLive;
 
             synchronized (availableChannels) {
                while (availableChannels.size() > 0){
                    if (ttl > ((SharedChannel) availableChannels.get(0)).getTTL()) {
 
                         // channel has out lived its time to live
                         // die, die, die
                         SharedChannel shCh =
                            (SharedChannel) availableChannels.remove(0);
 
                         try {
                             Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                                          "garbage collected channel number:"
                                          + shCh.getNumber());
                             shCh.close();    // last gasp
                         } catch (BEEPException e) {
 
                             // ignore for now, we'll try again later
                             Log.logEntry(Log.SEV_ALERT,
                                          "unable to close channel number:"
                                          + shCh.getNumber());
                         }
 
                         shCh = null;    // death by gib @todo does this really set the object to null?
                     } else {
 
                         // since youngest channels are added to the end of the list,
                         // once we hit the above condition all channels below this one are
                         break;
                     }
                 }
             }
         }
     }
 }
