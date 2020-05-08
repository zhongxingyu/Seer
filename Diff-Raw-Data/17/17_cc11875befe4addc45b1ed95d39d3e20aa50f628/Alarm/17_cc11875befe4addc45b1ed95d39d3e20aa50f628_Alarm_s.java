 package nachos.threads;
 
 import nachos.machine.*;
 import java.util.LinkedList;
 
 /**
  * Uses the hardware timer to provide preemption, and to allow threads to sleep
  * until a certain time.
  */
 public class Alarm {
   /**
    * Allocate a new Alarm. Set the machine's timer interrupt handler to this
    * alarm's callback.
    *
    * <p><b>Note</b>: Nachos will not function correctly with more than one
    * alarm.
    */
 
 
   // private LinkedList<Pair<KThread, Long>> waitingThreads;
 
   private LinkedList<KThread> waitingThreads;
   private LinkedList<Long> wakeUpTimes;
 
   private Lock startWaitingLock;
   
 
   public Alarm() {
     this.waitingThreads = new LinkedList<KThread>();
     this.wakeUpTimes = new LinkedList<Long>();
     this.startWaitingLock = new Lock();
 
     Machine.timer().setInterruptHandler(new Runnable() {
       public void run() { timerInterrupt(); }
     });
   }
 
   /**
    * The timer interrupt handler. This is called by the machine's timer
    * periodically (approximately every 500 clock ticks). Causes the current
    * thread to yield, forcing a context switch if there is another thread
    * that should be run.
    */
   public void timerInterrupt() {
     //loop over all the waiting threads and check to see if they can be
     //woken up
     Machine.interrupt().disable();
    while(!waitingThreads.isEmpty()){
 
      if(wakeUpTimes.peekFirst().longValue() < Machine.timer().getTime()){
        waitingThreads.removeFirst().ready();
        wakeUpTimes.removeFirst();
       }
     }
     Machine.interrupt().enable();
     KThread.currentThread().yield();
   }
 
   /**
    * Put the current thread to sleep for at least <i>x</i> ticks,
    * waking it up in the timer interrupt handler. The thread must be
    * woken up (placed in the scheduler ready set) during the first timer
    * interrupt where
    *
    * <p><blockquote>
    * (current time) >= (WaitUntil called time)+(x)
    * </blockquote>
    *
    * @param	x	the minimum number of clock ticks to wait.
    *
    * @see	nachos.machine.Timer#getTime()
    */
   public void waitUntil(long x) {
     // for now, cheat just to get something working (busy waiting is bad)
     Lib.assertNotReached();
     long wakeTime = Machine.timer().getTime() + x;
     Long wake = new Long(wakeTime);
 
     this.startWaitingLock.acquire();
     this.waitingThreads.add(KThread.currentThread());
     this.wakeUpTimes.add(wake);
     this.startWaitingLock.release();
 
     KThread.currentThread().sleep();
   }
 }
