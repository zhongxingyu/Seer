 package queue;
 
 public class QueueFactory {
 
 	 public static DistributedQueue getQueue(){
 		 DistributedQueue queue = null;
		 queue = new HazleCast();
		 //queue = new ActiveMQ();
 		 return queue;
 	 }
 }
