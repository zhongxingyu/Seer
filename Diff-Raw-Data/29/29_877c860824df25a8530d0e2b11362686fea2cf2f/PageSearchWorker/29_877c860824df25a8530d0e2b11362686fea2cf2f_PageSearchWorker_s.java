 package com.zibea.parser.core.workers;
 
 import com.zibea.parser.core.exception.PageNotFoundException;
 import com.zibea.parser.core.task.Task;
 
 import java.io.IOException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.atomic.AtomicLong;
 
 /**
  * @author: Mikhail Bragin
  */
 public class PageSearchWorker implements Worker {
 
     private static final String pageParam = "?pag=";
 
     private ExecutorService pageSearchPool;
 
     private PageParseWorker pageParseWorker;
 
     private LinkedBlockingQueue<Task> tasks;
 
     private AtomicLong tasksProduced = new AtomicLong();
 
     public PageSearchWorker(PageParseWorker pageParseWorker) {
         this.pageSearchPool = Executors.newFixedThreadPool(1, new CustomThreadFactory("page-search-worker"));
         this.pageParseWorker = pageParseWorker;
         this.tasks = new LinkedBlockingQueue<>();
         start();
     }
 
     public void addTask(Task task) {
         tasks.add(task);
     }
 
     public void start() {
         pageSearchPool.submit(new NewParseWorker(tasks) {
 
             @Override
             public void processTask() throws InterruptedException {
 
                for (int i = 1; i <= Short.MAX_VALUE; i++) {
 
                     int attempt = 0;
 
                     while (attempt < 3) {
 
                         attempt++;
                         String newTaskUrl = task.getUrl() + pageParam + i;
 
                         try {
                             testUrl(newTaskUrl);
 
                             pageParseWorker.addTask(new Task(task.getState(),
                                     task.getCity(),
                                     task.getApartment(),
                                     task.getTransaction(),
                                     task.getDistrict(),
                                     newTaskUrl));
                             tasksProduced.incrementAndGet();
 
                             Thread.sleep(1000);
                             break;
                         } catch (PageNotFoundException e) {
                             return;  //if it was non existing page index, finish creating search page tasks
                         } catch (IOException e) {
                             Thread.sleep(2000);
                         }
                     }
                 }
             }
         });
     }
 
     public long getTasksProduced() {
         return tasksProduced.get();
     }
 
     public int getQueueSize() {
         return tasks.size();
     }
 }
