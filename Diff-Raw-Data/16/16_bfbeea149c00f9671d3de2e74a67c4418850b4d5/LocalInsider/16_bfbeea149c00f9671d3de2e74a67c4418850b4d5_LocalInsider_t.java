 /*
  * Copyright (C) 2008 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation, either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.novelang.outfit.shell.insider;
 
 import java.lang.management.ManagementFactory;
 import java.util.concurrent.atomic.AtomicLong;
 
 import static java.lang.System.currentTimeMillis;
 
 
 /**
  * Runs inside the JVM launched by {@link org.novelang.outfit.shell.JavaShell}.
  * 
  * @author Laurent Caillette
  */
 @SuppressWarnings( { "JavadocReference" } )
 public class LocalInsider implements Insider {
 
   private final AtomicLong keepaliveCounter ;
 
   public LocalInsider() {
     this( HEARTBEAT_FATAL_DELAY_MILLISECONDS ) ;
   }
 
 
   @SuppressWarnings( { "CallToThreadStartDuringObjectConstruction" } )
   public LocalInsider( final long delay ) {
    final String virtualMachineName = ManagementFactory.getRuntimeMXBean().getName();
 
     printOut( "Initializing " + getClass().getSimpleName() + " "
         + "with: "
         + "virtualMachineName=" + virtualMachineName + ", "
         + "fatalHeartbeatDelay=" + delay + " milliseconds..."
     ) ;
 
     keepaliveCounter = new AtomicLong( currentTimeMillis() ) ;
 
     final Thread heartbeatReceiver = new Thread(
         new Runnable() {
           @Override
           public void run() {
            printOut(
                "Started keepalive watcher from thread " + Thread.currentThread() + "." ) ;
             while( true ) {
               try {
                 Thread.sleep( delay ) ;
                 final long lag = currentTimeMillis() - keepaliveCounter.get() ;
                 if( lag > delay ) {
                   printErr( "No heartbeat for more than " + delay + " milliseconds, " +
                       "halting with status of " + STATUS_HEARTBEAT_PERIOD_EXPIRED + "." ) ;
                   Runtime.getRuntime().halt( STATUS_HEARTBEAT_PERIOD_EXPIRED ) ;
                   break ; // Avoid a compilation warning because of infinite loop.
                 }
               } catch( InterruptedException ignore ) { }
             }
           }
         }
         ,getClass().getSimpleName() + "-HeartbeatReceiver"
     ) ;
     heartbeatReceiver.setDaemon( true ) ;
     heartbeatReceiver.start() ;
 
     // Reset the counter for an approximative synchronization with heartbeat thread start time.
     // A semaphore may be overkill.
     keepAlive() ;
     
   }
 
   /**
    * Performs shutdown in a separate thread to let the {@link #shutdown()} method return.
    */
   @Override
   public void shutdown() {
     new Thread(
         new Runnable() {
           @Override
           public void run() {
             System.exit( 0 ) ;
           }
         },
         LocalInsider.class.getSimpleName()
     ).start() ;
   }
 
   @Override
   public void keepAlive() {
 //    printOut( "Received keepalive call at " + currentTimeMillis() + "." ) ;
     keepaliveCounter.set( currentTimeMillis() ) ;
   }
 
   @Override
   public boolean isAlive() {
     return true ;
   }
 
   private static void printOut( final String message ) {
     System.out.println( createTimestamp() + message ) ;
     System.out.flush() ;
   }
 
   private static void printErr( final String message ) {
     System.out.println( createTimestamp() + message ) ;
     System.err.flush() ;
   }
 
   private static String createTimestamp() {
 //    return new SimpleDateFormat( "HH:mm:ss,SSS " ).format( new Date() );
     return "" ;
   }
 }
