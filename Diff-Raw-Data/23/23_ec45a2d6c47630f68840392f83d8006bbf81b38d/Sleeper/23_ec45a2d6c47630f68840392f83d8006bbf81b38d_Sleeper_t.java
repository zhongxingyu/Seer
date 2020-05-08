// Copyright (C) 2001, 2002, 2003, 2004 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.util;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 
 import net.grinder.common.GrinderException;
 import net.grinder.common.Logger;
 
 
 /**
  * Manage sleeping
  *
 * <p>Several threads can safely use the same <code>Sleeper</code>.
  * </p>
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public class Sleeper {
 
   private static Random s_random = new Random();
   private static List s_allSleepers = new ArrayList();
 
   private boolean m_shutdown = false;
   private final double m_factor;
   private final double m_limit9975Factor;
   private final Logger m_logger;
 
   /**
    * The constructor.
    *
    * @param factor All sleep times are modified by this factor.
    * @param limit9975Factor See {@link #sleepNormal}.
    * @param logger  A logger to chat to. Pass <code>null</code> for no chat.
   */
   public Sleeper(double factor, double limit9975Factor, Logger logger) {
 
     if (factor < 0d || limit9975Factor < 0d) {
       throw new IllegalArgumentException("Factors must be positive");
     }
 
     synchronized (Sleeper.class) {
       s_allSleepers.add(new WeakReference(this));
     }
 
     m_factor = factor;
     m_limit9975Factor = limit9975Factor;
     m_logger = logger;
   }
 
   /**
    * Shutdown all Sleepers that are currently constructed.
   */
   public static final synchronized void shutdownAllCurrentSleepers() {
 
     final Iterator iterator = s_allSleepers.iterator();
 
     while (iterator.hasNext()) {
       final WeakReference reference = (WeakReference)iterator.next();
 
       final Sleeper sleeper = (Sleeper)reference.get();
 
       if (sleeper != null) {
         sleeper.shutdown();
       }
     }
 
     s_allSleepers.clear();
   }
 
   /**
    * Shutdown this <code>Sleeper</code>. Once called, all sleep
    * method invocations will throw {@link ShutdownException},
    * including those already sleeping.
   */
   public final synchronized void shutdown() {
 
     m_shutdown = true;
     notifyAll();
   }
 
   /**
    * Sleep for a time based on the meanTime parameter. The actual
    * time is taken from a pseudo normal distribution. Approximately
    * 99.75% of times will be within (100* limit9975Factor) percent
    * of the meanTime.
    *
    * @param meanTime Mean time.
    * @throws ShutdownException If this <code>Sleeper</code> has been shutdown.
   */
   public void sleepNormal(long meanTime) throws ShutdownException {
 
     sleepNormal(meanTime, (long)((meanTime * m_limit9975Factor) / 3.0));
   }
 
   /**
    * Sleep for a random time drawn from a pseudo normal distribution.
    *
    * @param meanTime Mean time.
    * @param sigma Standard deviation.
    * @throws ShutdownException If this <code>Sleeper</code> has been shutdown.
    */
   public void sleepNormal(long meanTime, long sigma) throws ShutdownException {
 
     checkShutdown();
 
     if (meanTime > 0) {
       if (sigma > 0) {
         doSleep(meanTime + (long)(s_random.nextGaussian() * sigma));
       }
       else {
         doSleep(meanTime);
       }
     }
   }
 
   /**
    * Sleep for a time based on the maximumTime parameter. The actual
    * time is taken from a pseudo random flat distribution between 0
    * and maximumTime.
    *
    * @param maximumTime Maximum time.
    * @throws ShutdownException If this <code>Sleeper</code> has been shutdown.
   */
   public void sleepFlat(long maximumTime) throws ShutdownException {
     checkShutdown();
 
     if (maximumTime > 0) {
       doSleep(Math.abs(s_random.nextLong()) % maximumTime);
     }
   }
 
   private void doSleep(long time) throws ShutdownException {
 
     if (time > 0) {
       final long factoredTime = (long)(time * m_factor);
 
       if (m_logger != null) {
         m_logger.output("sleeping for " + factoredTime + " ms");
       }
 
       long currentTime = System.currentTimeMillis();
       final long wakeUpTime = currentTime + factoredTime;
 
       while (currentTime < wakeUpTime) {
         try {
           synchronized (this) {
             checkShutdown();
             wait(wakeUpTime - currentTime);
           }
           break;
         }
         catch (InterruptedException e) {
           checkShutdown();
 
           currentTime = System.currentTimeMillis();
         }
       }
     }
   }
 
   private void checkShutdown() throws ShutdownException {
 
     if (m_shutdown) {
       throw new ShutdownException("Shut down");
     }
   }
 
   /**
    * Exception used to indicate that a Sleeper has been shutdown.
   */
   public static final class ShutdownException extends GrinderException {
 
     private ShutdownException(String message) {
       super (message);
     }
   }
 }
