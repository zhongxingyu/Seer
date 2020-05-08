 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.q1cc.cfs.mmm.client.render;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 /**
  * Holds all the Worker Tasks to be done.
  * Is not fair.
  * @author cfstras
  */
 public class WorkerTaskPool {
     
     ArrayList<WorkerTask> tasks = new ArrayList<WorkerTask>();
     
     Worker[] workers;
     boolean workersIdle=true;
     boolean workerIdle=true;
     
     private final Object lock = new Object();
     
     public void initWorkers() {
         int num = Runtime.getRuntime().availableProcessors()+1;
         workers = new Worker[num];
         for(int i=0;i<num;i++){
             workers[i]=new Worker(this);
             workers[i].setPriority((Thread.MIN_PRIORITY+Thread.MAX_PRIORITY)/2);
             workers[i].setDaemon(true);
            workers[i].setName("Worker "+i);
             workers[i].start();
         }
     }
     
     public void add(WorkerTask task){
         synchronized(lock){
             tasks.add(task);
         }
         
         /// wake them up
         workersIdle = false;
         if(workerIdle){
             workerIdle=false;
             for (int i = 0; i < workers.length; i++) {
                 synchronized (workers[i]) {
                     workers[i].notifyAll();
                     workers[i].wasIdle=false;
                 }
             }
         }
     }
     
     public WorkerTask pop(){
         WorkerTask ret=null;
         synchronized(lock){
             if(tasks.size()<1) {
                 workersIdle=true;
                 workerIdle=true;
                 return null;
             }
             ret=tasks.get(0);
         
             for(WorkerTask t:tasks){
                 if(t.getPriority()>ret.getPriority()){
                     ret=t;
                 }
                 if(ret.getPriority()>=WorkerTask.PRIORITY_MAX){
                     break;
                 }
             }
             tasks.remove(ret);
         }
         return ret;
     }
 }
