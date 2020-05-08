 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 
 public class Monitor {
 
     private List<Integer> free=new ArrayList<>();
     private List<Integer> full=new ArrayList<>();
     private Lock lockFree = new ReentrantLock();
     private Lock lockFull = new ReentrantLock();
     private Condition produceCond = lockFree.newCondition();
     private Condition consumeCond = lockFull.newCondition();
 
     public Monitor(int bufferSize){
         for(int i=0;i<bufferSize;i++)
             free.add(i);
     }
 
     public int[] lockProduction(int n) throws InterruptedException {
         int[] toReturn= new int[n];
         lockFree.lock();
         try {
            while(free.size()<n)
                 produceCond.await();
             for(int i=0;i<n;i++)
                 toReturn[i]=free.remove(free.size()-1);
         } finally {
             lockFree.unlock();
         }
         return toReturn;
     }
 
     public void unlockProduction(int[] indexes){
         lockFull.lock();
         try {
             for(int i:indexes)
                 full.add(i);
            consumeCond.signal();
         } finally {
             lockFull.unlock();
         }
     }
 
     public int[] lockConsumption(int n) throws InterruptedException {
         int[] toReturn=new int[n];
         lockFull.lock();
         try {
            while(full.size()<n)
                 consumeCond.await();
             for(int i=0;i<n;i++)
                 toReturn[i]=full.remove(full.size()-1);
         } finally {
             lockFull.unlock();
         }
         return toReturn;
     }
 
     public void unlockConsumption(int[] indexes){
         lockFree.lock();
         try {
             for(int i:indexes)
                 free.add(i);
            produceCond.signal();
         } finally {
             lockFree.unlock();
         }
     }
 }
