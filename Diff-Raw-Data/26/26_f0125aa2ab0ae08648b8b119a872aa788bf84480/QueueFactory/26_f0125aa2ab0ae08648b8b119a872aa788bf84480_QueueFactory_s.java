 package queue;
 
 public class QueueFactory {
 
 	 public static DistributedQueue getQueue(){
 		 DistributedQueue queue = null;
		//queue = new HazleCast();
		 //queue = new SQS();
		 queue = new ActiveMQ();
 		 return queue;
 	 }

	public static DistributedQueue getDistributedQueue() {
		 DistributedQueue queue = null;
		 queue = new HazleCast();
		return queue;
	}
 }
