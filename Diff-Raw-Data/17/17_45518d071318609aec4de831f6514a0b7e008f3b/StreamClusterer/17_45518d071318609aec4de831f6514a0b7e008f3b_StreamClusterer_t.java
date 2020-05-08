 package org.tmu.old;
 
 import org.apache.commons.math3.stat.clustering.Cluster;
 import org.apache.commons.math3.stat.clustering.Clusterable;
 import org.apache.commons.math3.stat.clustering.KMeansPlusPlusClusterer;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.*;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Saeed
  * Date: 10/12/12
  * Time: 11:46 PM
  * To change this template use File | Settings | File Templates.
  */
 
 
 public class StreamClusterer<T extends Clusterable<T>>   {
     BlockingQueue<List<T>> queue=new LinkedBlockingQueue<List<T>>();
 
    ConcurrentLinkedQueue<T> intermediateCenters=new ConcurrentLinkedQueue<T>();
 
     AtomicBoolean stop=new AtomicBoolean(false);
     AtomicInteger liveThreadsCount=new AtomicInteger(0);
     List<Thread> threadsList=new ArrayList<Thread>();
 
     int seed=0;
     int clusterCount=0;
     int innerLoopCount=7;
 
     public StreamClusterer(int cluster_count,Random random)
     {
         seed=random.nextInt();
         clusterCount=cluster_count;
         int processors = Runtime.getRuntime().availableProcessors();
         for(int i=0;i<processors;i++)
         {
             Runnable r=new Runnable() {
                 public void run() {
                     doWork();
                 }
             };
             Thread t=new Thread(r);
             t.start();
             threadsList.add(t);
         }
     }
 
     public boolean IsDone()
     {
         while (liveThreadsCount.get()>0)
             try {
                 Thread.sleep(10);
             } catch (InterruptedException e) {
                 continue;
             }
         return true;
     }
 
     public void AddChunk(List<T> chunk)
     {
         queue.add(chunk);
     }
 
     public void RequestStop()
     {
         stop.set(true);
     }
 
     public List<T> getIntermediateCenters()
     {
         IsDone();
         List<T> result=new ArrayList<T>(intermediateCenters.size());
         result.addAll(intermediateCenters);
         return result;
     }
 
     private void doWork() {
         liveThreadsCount.incrementAndGet();
         int i=0;
         KMeansPlusPlusClusterer<T> kmeanspp=new KMeansPlusPlusClusterer<T>(new Random(seed));
         while(!stop.get())
         {
             try {
                 List<T> chunk=queue.poll(10, TimeUnit.MILLISECONDS);
                 if(chunk==null){
                     System.out.println("I am IDLE!!!!!");
                     continue;
                 }
                 if(chunk.size()==0){
                     stop.set(true);
                     break;
                 }
                 List<Cluster<T>> clusters=null;
                 if(innerLoopCount<=0)
                     clusters=kmeanspp.cluster(chunk,clusterCount,(int)Math.ceil(Math.log(chunk.size())));
                 else
                     clusters=kmeanspp.cluster(chunk,clusterCount,innerLoopCount);
 
                 for(Cluster<T> cluster:clusters)
                     intermediateCenters.add(cluster.getCenter());
             } catch (InterruptedException e) {
                 System.out.println("This shall never happen!");
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
         }
         liveThreadsCount.decrementAndGet();
     }
 
 }
