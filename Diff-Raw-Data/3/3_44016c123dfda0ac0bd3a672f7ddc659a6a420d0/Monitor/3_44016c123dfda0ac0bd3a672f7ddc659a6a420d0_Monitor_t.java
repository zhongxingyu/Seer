 package zad4313.ver1;
 
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 public class Monitor {
     private Lock lock = new ReentrantLock();
     private Queue<Condition> waitingDemands = new LinkedList<>();
 
     private void changePosition(Integer position) {
         System.out.println("position changed to: " + position);
     }
 
     public void demandNewPosition(Demand demand) throws InterruptedException {
         lock.lock();
         Condition currentThreadCondition = lock.newCondition();
         waitingDemands.add(currentThreadCondition);
         while(waitingDemands.peek()!=currentThreadCondition)
             currentThreadCondition.await();
         changePosition(demand.cylinder);
         lock.unlock();
     }
 
     public void release() {
         lock.lock();
        waitingDemands.poll();
         if(!waitingDemands.isEmpty())
             waitingDemands.peek().signal();
         lock.unlock();
     }
 }
