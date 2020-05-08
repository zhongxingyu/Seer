 package talkingnet.core;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import talkingnet.core.io.Multipushable;
 import talkingnet.core.io.Multipushing;
 import talkingnet.core.io.Pullable;
 import talkingnet.core.io.Pushable;
 
 /**
  *
  * @author Alexander Oblovatniy <oblovatniy@gmail.com>
  */
 public class PushingMultipool extends Element implements Multipushing {
 
    private long fillingWaitTime = 3l;
     private Multipushable sink;
     private final List<InternalPool> pools = new LinkedList<InternalPool>();
     private PullingThread thread;
     private boolean doProcessing = false;
     private int bufferSize;
 
     public PushingMultipool(int bufferSize, Multipushable sink, String title) {
         super(title);
         this.bufferSize = bufferSize;
         this.sink = sink;
     }
 
     private class InternalPool implements Pushable, Pullable {
 
         private final ConcurrentLinkedQueue<byte[]> queue =
                 new ConcurrentLinkedQueue<byte[]>();
 
         public void push_in(byte[] data, int size) {
             queue.add(data);
         }
 
         public int pull_out(byte[] data, int size) {
             if (queue.isEmpty()) {
                 return 0;
             } else {
                 System.arraycopy(queue.poll(), 0, data, 0, size);
                 return size;
             }
         }
 
         public void flush() {
             queue.clear();
         }
     }
 
     public Pushable getNewSink() {
         InternalPool pool = new InternalPool();
         synchronized (pools) {
             pools.add(pool);
             pools.notifyAll();
         }
         return pool;
     }
 
     public void disposeSink(Pushable sink) {
         InternalPool pool = (InternalPool) sink;
         pool.flush();
         synchronized (pools) {
             pools.remove(pool);
         }
     }
 
     private class PullingThread extends Thread {
 
         @Override
         public void run() {
 
             List<byte[]> allData = new ArrayList<byte[]>();
            byte[] singleData = new byte[bufferSize];
 
             doProcessing = true;
 
            while (doProcessing) {                
                 synchronized (pools) {
                     for (InternalPool pool : pools) {
                         int pulled = pool.pull_out(singleData, bufferSize);
                         if (pulled > 0) {
                             allData.add(singleData);
                         }
                     }

                    try {
                        if (allData.isEmpty() && doProcessing) {
                            pools.wait(fillingWaitTime);
                        }
                    } catch (InterruptedException e) {
                        return;
                    }
                 }
                
                if (allData.isEmpty()==false){
                     multipush_out(allData);
                     allData.clear();
                 }
             }
             allData.clear();
         }
     }
 
     public void multipush_out(Collection<byte[]> data) {
         sink.multipush_in(data);
     }
 
     public void start() {
         if (thread != null) {
             return;
         }
 
         thread = new PullingThread();
         thread.start();
         synchronized (pools) {
             pools.notifyAll();
         }
     }
 
     public void stop() {
         doProcessing = false;
         thread = null;
         synchronized (pools) {
             pools.notifyAll();
         }
     }
 
     public long getFillingWaitTime() {
         return fillingWaitTime;
     }
 
     public synchronized void setFillingWaitTime(long fillingWaitTime) {
         this.fillingWaitTime = fillingWaitTime;
     }
 }
