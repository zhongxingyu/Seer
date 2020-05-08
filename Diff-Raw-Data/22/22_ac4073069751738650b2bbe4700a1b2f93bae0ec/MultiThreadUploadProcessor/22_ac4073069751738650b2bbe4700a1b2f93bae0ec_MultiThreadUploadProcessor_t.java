 //
 // Treasure Data Bulk-Import Tool in Java
 //
 // Copyright (C) 2012 - 2013 Muga Nishizawa
 //
 //    Licensed under the Apache License, Version 2.0 (the "License");
 //    you may not use this file except in compliance with the License.
 //    You may obtain a copy of the License at
 //
 //        http://www.apache.org/licenses/LICENSE-2.0
 //
 //    Unless required by applicable law or agreed to in writing, software
 //    distributed under the License is distributed on an "AS IS" BASIS,
 //    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //    See the License for the specific language governing permissions and
 //    limitations under the License.
 //
 package com.treasure_data.bulk_import.upload_parts;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.logging.Logger;
 
 import com.treasure_data.client.TreasureDataClient;
 import com.treasure_data.client.bulkimport.BulkImportClient;
 import com.treasure_data.model.bulkimport.SessionSummary;
 
 public class MultiThreadUploadProcessor {
     static class Worker extends Thread {
         private MultiThreadUploadProcessor parent;
         private UploadProcessor proc;
         AtomicBoolean isFinished = new AtomicBoolean(false);
 
         public Worker(MultiThreadUploadProcessor parent, UploadProcessor proc) {
             this.parent = parent;
             this.proc = proc;
         }
 
         @Override
         public void run() {
             while (true) {
                 Task t = parent.taskQueue.poll();
                 if (t == null) {
                     continue;
                 } else if (t.endTask()) {
                     break;
                 }
 
                TaskResult result = proc.execute(t);
                parent.setTaskResult(result);
             }
             isFinished.set(true);
         }
     }
 
     private static final Logger LOG = Logger.getLogger(MultiThreadUploadProcessor.class.getName());
     private static BlockingQueue<Task> taskQueue;
 
     static {
         taskQueue = new LinkedBlockingQueue<Task>();
     }
 
     public static synchronized void addTask(Task task) {
         taskQueue.add(task);
     }
 
     public static synchronized void clearTasks() {
         // the method is for tests
         taskQueue.clear();
     }
 
     public static synchronized void addFinishTask(UploadConfiguration conf) {
         for (int i = 0; i < conf.getNumOfUploadThreads() * 2; i++) {
             taskQueue.add(Task.FINISH_TASK);
         }
     }
 
     private UploadConfiguration conf;
     private List<Worker> workers;
    private List<TaskResult> results;
 
     public MultiThreadUploadProcessor(UploadConfiguration conf) {
         this.conf = conf;
         workers = new ArrayList<Worker>();
        results = new ArrayList<TaskResult>();
     }
 
    protected synchronized void setTaskResult(TaskResult error) {
        results.add(error);
     }
 
    public List<TaskResult> getTaskResults() {
        return results;
     }
 
     public void registerWorkers() {
         for (int i = 0; i < conf.getNumOfUploadThreads(); i++) {
             addWorker(createWorker(conf));
         }
     }
 
     protected Worker createWorker(UploadConfiguration conf) {
         return new Worker(this, createUploadProcessor(conf));
     }
 
     protected void addWorker(Worker w) {
         workers.add(w);
     }
 
     protected UploadProcessor createUploadProcessor(UploadConfiguration conf) {
         return new UploadProcessor(createBulkImportClient(conf), conf);
     }
 
     protected BulkImportClient createBulkImportClient(UploadConfiguration conf) {
         return new BulkImportClient(new TreasureDataClient(conf.getProperties()));
     }
 
     public void startWorkers() {
         for (int i = 0; i < workers.size(); i++) {
             workers.get(i).start();
         }
     }
 
     public void joinWorkers() {
         long waitSec = 1 * 1000;
         while (!workers.isEmpty()) {
             Worker last = workers.get(workers.size() - 1);
             if (last.isFinished.get()) {
                 workers.remove(workers.size() - 1);
             }
 
             try {
                 Thread.sleep(waitSec);
             } catch (InterruptedException e) {
                 // ignore
             }
         }
     }
 }
