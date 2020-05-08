 /*
  * STFQNWScheduler
  *
  * Implement the NWScheduler Interface to provide
  * a scheduler that ensures that system sends
  * at most max bytes per second.
  *
  * You must follow the coding standards distributed
  * on the class web page.
  *
  * (C) 2007 Mike Dahlin
  *
  * 
  * NOTE: Must run with -enableasesrtions flag on
  * java -enableassertions DemoSTFQScheduler
  */
 
 import java.util.concurrent.locks.Condition;
 import java.util.*;
 
 public class STFQNWScheduler implements NWScheduler{
 
 	STFQAlarmThread at;
 	SimpleLock mutex;
 	Condition c1;
 	Condition c2; 
 	long CurrentVirtualTime;
 	long maxBW;
 	long nextTurn;
 	HashMap<Integer,Long> flowFinishTags;
 
 	//-------------------------------------------------
 	// Constructor
 	//-------------------------------------------------
 	public STFQNWScheduler(long bytesPerSec)
 	{
 		this.nextTurn = System.currentTimeMillis();
 		this.CurrentVirtualTime = 0;
 		this.maxBW = bytesPerSec; 
 		this.mutex = new SimpleLock();
 		this.c1 = mutex.newCondition();
 		this.c2 = mutex.newCondition();
 		this.flowFinishTags = new HashMap<Integer,Long>();
 		this.at = new STFQAlarmThread(this);
 		this.at.start();
 	}
 
 	//-------------------------------------------------
 	// waitMyTurn -- return only after I may safely
 	// send. If prev send s0 at time t0 transmitted b0
 	// bytes, the next send may not send before
 	// t1 >= t0 + b0/bytesPerSec
 	//
 	// NOTE: See the assignent for important restriction.
 	// In particular, this call must use Condition.await()
 	// (or Condition.awaitUninterruptibly())
 	// with *no arguments* and it may not call Thread.sleep().
 	// Instead, you must rely on an alarmThread to 
 	// signal when it is OK to proceed.
 	//-------------------------------------------------
 	public void waitMyTurn(int flowId, float weight, int lenToSend)
 	{
 		mutex.lock();
 		if(!flowFinishTags.containsKey(flowId))
 			flowFinishTags.put(flowId, (long)0);
 

 		while(System.currentTimeMillis() < nextTurn) {
 			try {
 				c1.await();
 			}catch (InterruptedException E) {
 				//do something I guess
 			}
 		}
		long startTag = Math.max(flowFinishTags.get(flowId),CurrentVirtualTime);  //Guaranteed to be 
		long finishTag = startTag + (long)(lenToSend / weight);
 		flowFinishTags.put(flowId, finishTag);
 		
 		while(startTag > CurrentVirtualTime) {
 			try {
 				c2.await();
 			}catch (InterruptedException E) {
 				//do something I guess
 			}
 		}
 		nextTurn = System.currentTimeMillis() + 1000*lenToSend/maxBW;
		if(finishTag > CurrentVirtualTime)
			CurrentVirtualTime = finishTag;
 		
 
 
 		mutex.unlock();
 		//System.out.println("Sending: " + lenToSend);
 		return;
 	}
 
 
 
 	//
 	// Hint: you will need to add at least one
 	// new public method that AlarmThread will
 	// call.
 	//
 	public void procede() {
 		mutex.lock();
 		c1.signal();
 		c2.signal();
 		mutex.unlock();
 	}
 
 	public long getNextTurn(){
 		mutex.lock();
 		mutex.unlock();
 		return nextTurn;
 	}
 
 
 
 
 
 
 }
 
 
