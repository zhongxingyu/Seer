 package org.nebulostore.communication.address;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import net.tomp2p.p2p.PeerMaker;
 import net.tomp2p.peers.Number160;
 
 /**
  * TomP2P's kademlia based implementation of persistent addressing service.
  *
  * @author Grzegorz Milka
  */
 public final class TomP2PServer extends TomP2PPeer {
   /**
    * Time for timer task to wait between interrupts to end a long shutdown of
    * TP2P object.
    *
    * @see TP2PInterrupter
    */
   private static final int INTERRUPT_WAIT = 10000;
 
   public TomP2PServer() {
     super();
   }
 
   @Override
   public void setUpAndRun() throws IOException {
     if (bootstrapServerAddress_ == null) {
       throw new IllegalStateException("Bootstrap address has to be set.");
     }
     if (myCommAddress_ == null) {
       throw new IllegalStateException("CommAddress has to be set.");
     }
 
     try {
       myPeer_ = new PeerMaker(new Number160(myCommAddress_.hashCode())).
         setPorts(tomP2PPort_).makeAndListen();
     } catch (IOException e) {
       String errMsg = "Error when making peer";
       logger_.error(errMsg + " " + e);
       throw e;
     }
 
     try {
       uploadCurrentInetSocketAddress();
     } catch (IOException e) {
       logger_.warn(e.getMessage() + e.getCause().toString());
     }
 
     resolver_ = new CachedAddressResolver(
         new HashAddressResolver(myCommAddress_, myPeer_));
 
     logger_.info("TomP2PServer initialization finished. My address is: " +
         bootstrapServerAddress_ + ".");
   }
 
   @Override
   public void destroy() {
     checkSetUp();
     isTearingDown_.set(true);
     logger_.info("Starting tearDown procedure.");
 
     Timer interrupterTimer = new Timer(true);
     AtomicBoolean hasShutdown = new AtomicBoolean(false);
     Lock opLock = new ReentrantLock();
     TimerTask interrupter = new TP2PInterrupter(Thread.currentThread(),
         hasShutdown,
         opLock,
         false);
     TimerTask shutdowner = new TP2PInterrupter(Thread.currentThread(),
         hasShutdown,
         opLock,
         true);
     interrupterTimer.schedule(interrupter, INTERRUPT_WAIT);
     interrupterTimer.schedule(shutdowner, 2 * INTERRUPT_WAIT);
 
     myPeer_.shutdown();
 
     logger_.info("TomP2P peer has been shut down.");
 
     opLock.lock();
     try {
       hasShutdown.set(true);
       interrupterTimer.cancel();
     } finally {
       opLock.unlock();
     }
 
     resolver_ = null;
     myPeer_ = null;
   }
 
   @Override
   public InetSocketAddress getCurrentInetSocketAddress() throws IOException {
     checkSetUp();
     return new InetSocketAddress(bootstrapServerAddress_, commCliPort_);
   }
 
   /**
    * TimerTask which interrupts shutting down TP2P's object.
    * A bug where the server hangs on shutdown of TP2P has been reported. Since
    * we have been unable to determine the root cause the choice of action is to
    * periodically interrupt the shutdown hoping it will unblock.
    *
    * Note that if TP2P's shutdown ignores interrupts or blocks due to malignant
   * reasons this timer task may accomplish nothing.
    *
    * @author Grzegorz Milka
    */
   private class TP2PInterrupter extends TimerTask {
     private Thread serverThread_;
     private AtomicBoolean hasShutdown_;
     private Lock opLock_;
     private boolean shouldShutdown_;
 
     public TP2PInterrupter(Thread serverThread, AtomicBoolean hasShutdown,
         Lock opLock, boolean shouldShutdown) {
       serverThread_ = serverThread;
       hasShutdown_ = hasShutdown;
       opLock_ = opLock;
       shouldShutdown_ = true;
     }
 
     @Override
     public void run() {
       opLock_.lock();
       try {
         if (!hasShutdown_.get()) {
           logger_.error("TomP2PServer is forced to use TP2PInterrupter");
           if (shouldShutdown_) {
            serverThread_.stop();
           } else {
            serverThread_.interrupt();
           }
         }
       } finally {
         opLock_.unlock();
       }
     }
   }
 }
