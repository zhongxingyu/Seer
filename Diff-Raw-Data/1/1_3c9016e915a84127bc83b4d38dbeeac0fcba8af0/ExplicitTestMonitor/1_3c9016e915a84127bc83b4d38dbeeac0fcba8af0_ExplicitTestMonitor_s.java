 package examples.TestMonitor;
 
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 public class ExplicitTestMonitor extends TestMonitor {
     final Lock mutex = new ReentrantLock();
     Condition[] conds;
     
     private int numProc;
     private int numAccess;
     public ExplicitTestMonitor(int numProc_) {
         numProc = numProc_;
         numAccess = 0;
 
         conds = new Condition[numProc];
 
         for(int i = 0; i < numProc; ++i) {
             conds[i] = mutex.newCondition();
         }
     }
     public void access(int myId) {
         setCurrentCpuTime();
         mutex.lock();
        addSyncTime();
         setCurrentCpuTime();
         while((numAccess % numProc) != myId) {
             try {
                 conds[numAccess % numProc].signal();
                 conds[myId].await();
             } catch(InterruptedException e) {
             }
         }
         addSyncTime();
         //System.out.println("myId: " + myId_dummy + " numAccess: " + numAccess);
         ++numAccess;
         conds[numAccess % numProc].signal();
         mutex.unlock();
     }
 }
