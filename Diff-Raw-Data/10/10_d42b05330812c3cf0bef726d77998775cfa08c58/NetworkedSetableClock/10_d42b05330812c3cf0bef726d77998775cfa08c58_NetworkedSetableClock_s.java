 /*
  * Created on May 10, 2007 Copyright (C) 2001-6, Anthony Harrison anh23@pitt.edu
  * (jactr.org) This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of the License,
  * or (at your option) any later version. This library is distributed in the
  * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
  * the GNU Lesser General Public License for more details. You should have
  * received a copy of the GNU Lesser General Public License along with this
  * library; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place, Suite 330, Boston, MA 02111-1307 USA
  */
 package org.commonreality.time.impl.net;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.commonreality.message.command.time.ITimeCommand;
 import org.commonreality.message.request.time.IRequestTime;
 import org.commonreality.message.request.time.RequestTime;
 import org.commonreality.participant.IParticipant;
 import org.commonreality.time.IClock;
 import org.commonreality.time.impl.BasicClock;
 
 /**
  * @author developer
  */
 public class NetworkedSetableClock extends BasicClock implements IClock,
     INetworkedClock
 {
   /**
    * logger definition
    */
   static private final Log LOGGER = LogFactory
                                       .getLog(NetworkedSetableClock.class);
 
   private ITimeCommand     _currentTimeCommand;
 
   private IParticipant     _participant;
 
   public NetworkedSetableClock(IParticipant participant)
   {
     _participant = participant;
     setDefaultWaitTime(100);
   }
 
   @Override
   protected WaitFor createWaitForTime()
   {
     return new WaitFor() {
       @Override
       public boolean shouldWait(double currentTime)
       {
         double targetTime = getWaitForTime();
         boolean shouldWait = Double.isInfinite(currentTime)
             || targetTime - currentTime > getEpsilon();
 
         /*
          * request time change, send unshifted
          */
         double requested = targetTime - getTimeShift();
        if (shouldWait) _participant.send(new RequestTime(_participant.getIdentifier(),
             requested));
 
         if (LOGGER.isDebugEnabled())
           LOGGER.debug("Waiting for " + requested + " @ " +currentTime+" "+ shouldWait);
 
         return shouldWait;
       }
     };
   }
 
   @Override
   protected WaitFor createWaitForAny()
   {
     return new WaitFor() {
       @Override
       public boolean shouldWait(double currentTime)
       {
         double targetTime = getWaitForTime();
         boolean shouldWait = Double.isInfinite(currentTime)
             || Math.abs(targetTime - currentTime) <= getEpsilon();
 
         /*
          * request time change
          */
        if (shouldWait) _participant.send(new RequestTime(_participant.getIdentifier(),
             IRequestTime.ANY_CHANGE));
 
         return shouldWait;
       }
     };
   }
 
   // /**
   // * @see org.commonreality.time.IClock#waitForChange()
   // */
   // public double waitForChange() throws InterruptedException
   // {
   // double now = _clock.getTime();
   // while (now == _clock.getTime())
   // {
   // _participant.send(new RequestTime(_participant.getIdentifier(),
   // IRequestTime.ANY_CHANGE));
   // _clock.await(750);
   // }
   // return _clock.getTime();
   // }
   //
   // /**
   // * @see org.commonreality.time.IClock#waitForTime(double)
   // */
   // public double waitForTime(double time) throws InterruptedException
   // {
   // while (_clock.getTime() < time)
   // {
   // /*
   // * request it unshifted
   // */
   // _participant.send(new RequestTime(_participant.getIdentifier(), time -
   // _clock.getTimeShift()));
   // _clock.await(750);
   // }
   //
   // double rtn = _clock.getTime();
   //
   // if (time < rtn)
   // if (LOGGER.isWarnEnabled())
   // LOGGER.warn("Time slippage detected, wanted " + time + " got " + rtn);
   //
   // return rtn;
   // }
 
   public void setCurrentTimeCommand(ITimeCommand timeCommand)
   {
     _currentTimeCommand = timeCommand;
     /*
      * networked time is unshifted, so we have to shift...
      */
     setTime(_currentTimeCommand.getTime() + getTimeShift());
   }
 
 }
