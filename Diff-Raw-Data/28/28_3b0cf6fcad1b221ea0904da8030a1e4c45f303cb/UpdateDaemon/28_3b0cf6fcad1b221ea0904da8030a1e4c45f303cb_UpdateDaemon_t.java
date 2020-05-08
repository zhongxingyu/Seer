 /*
 
 Copyright: 2010 Bindley Bioscience Center, Purdue University
 
 License: X11 license.
 
   Permission is hereby granted, free of charge, to any person
   obtaining a copy of this software and associated documentation
   files (the "Software"), to deal in the Software without
   restriction, including without limitation the rights to use,
   copy, modify, merge, publish, distribute, sublicense, and/or sell
   copies of the Software, and to permit persons to whom the
   Software is furnished to do so, subject to the following
   conditions:
 
   The above copyright notice and this permission notice shall be
   included in all copies or substantial portions of the Software.
 
   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
   OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
   HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
   WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
   FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
   OTHER DEALINGS IN THE SOFTWARE.
 
 */
 
 package edu.purdue.bbc.util;
 
 import java.util.Iterator;
import java.util.List;
 import java.util.LinkedList;
 
 /**
  * This class will create a background thread which will process update events
  * in the background at minimum priority by default.
  */
 public class UpdateDaemon extends Thread {
  protected List<DaemonListener> listeners;
   protected DaemonRunnable runnable;
 
   /**
    * Creates a new UpdateDaemon this method is protected, so create a new
    * daemon with the create() method.
    * 
    * @param runnable The Runnable which this thread will run. This is created
    *   automatically by the create() method.
    */
   protected UpdateDaemon( DaemonRunnable runnable ){ 
     super( runnable );
     this.runnable = runnable;
     this.runnable.setLock( this );
     this.setPriority( MIN_PRIORITY );
     this.setDaemon( true );
     this.listeners = new LinkedList( );
     this.runnable.setListeners( this.listeners );
   }
 
   /**
    * Creates a new UpdateDaemon and returns it. This instance should then be 
    * started by calling it's start() method.
    * 
    * @param interval The interval to check for new updates, in milliseconds.
    * @return a new UpdateDaemon.
    */
   public static UpdateDaemon create( long interval ) {
     DaemonRunnable runnable = new DaemonRunnable( interval );
     return new UpdateDaemon( runnable );
   }
 
   /**
    * Calling this method with an object implementing DaemonListener will cause
    * it's daemonUpdate method to be called at the next interval.
    * 
    * @param listener The object to be processed.
    */
  public void update( DaemonListener listener ) {
     if ( !listeners.contains( listener )) 
       listeners.add( listener );
   }
 
   /**
    * Attemps to cancel the next update. If the event has not yet fired, the
    * update will be cancelled. There is no guarantee that cancellation will
    * succeed.
    * 
    * @param The listener to cancel the update for.
    * @return true if the cancellation was successful. False if the update has
    *   already started and thus unable to be cancelled.
    */
  public boolean cancelUpdate( DaemonListener listener ) {
     return listeners.remove( listener );
   }
 
   /**
    * Kills the thread. At the next update, the thread will not execute and
    * instead exit.
    */
   public void kill( ) {
     this.runnable.setKillBit( true );
   }
 
   /**
    * Runnable class for the Thread. This is created automatically by
    * UpdateDaemon's create() method.
    */
   protected static class DaemonRunnable implements Runnable {
     protected Object lock;
     protected boolean kill = false;
     protected long interval;
    protected List<DaemonListener> listeners;
 
     public DaemonRunnable( long interval ) {
       this.interval = interval;
     }
 
    public void setListeners( List<DaemonListener> listeners ) {
       this.listeners = listeners;
     }
 
     public void setLock( Object lock ) {
       this.lock = lock;
     }
 
     public void setKillBit( boolean kill ) {
       this.kill = kill;
     }
 
     public void run( ) {
       while( true ) {
         try {
           Thread.sleep( this.interval );
         } catch ( InterruptedException e ) { 
           continue;
         }
         if ( this.kill )
           return;
        while( listeners.size( ) > 0) {
          DaemonListener listener = listeners.remove( 0 );
          listener.daemonUpdate( );
         }
       }
     }
   }
 }
