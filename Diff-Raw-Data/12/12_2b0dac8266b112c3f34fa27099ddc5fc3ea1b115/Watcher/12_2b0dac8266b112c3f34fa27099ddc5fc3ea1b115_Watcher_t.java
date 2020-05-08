 package lesson_9;
 
 import lesson_8.Watcher.WatcherLogger;
 import lesson_9.watchers.BaseWatcher;
 import lesson_9.watchers.Visitor;
 
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.*;
 
 import static java.nio.file.StandardWatchEventKinds.*;
 public class Watcher {
 
     private ExecutorService pool = Executors.newFixedThreadPool(2);
     List<Callable<List<Path>>> watchers = new ArrayList<>();
     List<Future<List<Path>>> futures = new ArrayList<>();
     Set<Path> resultedSet = new HashSet<>();
     WatcherLogger logger = new WatcherLogger();
 
     private void watchers(){
         watchers.add(new BaseWatcher(ENTRY_CREATE));
         watchers.add(new BaseWatcher(ENTRY_DELETE));
         watchers.add(new BaseWatcher(ENTRY_MODIFY));
         watchers.add(new Visitor());
 
         for (;;) {
             for (Callable<List<Path>> callable : watchers) {
                 futures.add(pool.submit(callable));
             }
             for (Future<List<Path>> future : futures) {
                 try {
                    if(future.isDone())
                        resultedSet.addAll(future.get(1000, TimeUnit.MILLISECONDS));
                            if (!resultedSet.isEmpty())
                                logger.log(resultedSet.toString(), "ResultOfThread");
                                resultedSet.clear();
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 } catch (ExecutionException e) {
                     e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
             }
         }
     }
 
     public static void main(String[] args) {
         new Watcher().watchers();
     }
 
 
 
 }
