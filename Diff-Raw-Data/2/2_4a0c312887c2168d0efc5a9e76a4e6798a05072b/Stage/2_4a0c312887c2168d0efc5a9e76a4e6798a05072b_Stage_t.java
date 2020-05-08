 package com.github.rickyclarkson.swingflow;
 
 import com.github.rickyclarkson.monitorablefutures.Monitorable;
 import com.github.rickyclarkson.monitorablefutures.MonitorableExecutorService;
 import com.github.rickyclarkson.monitorablefutures.MonitorableFuture;
 import fj.data.Option;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 public final class Stage implements Iterable<Stage> {
     private final MonitorableExecutorService executorService;
     private final String name;
     private final Monitorable<Progress> command;
     private Option<MonitorableFuture<Progress>> future = Option.none();
     private final List<String> possibleValues;
     public Option<Stage> next;
 
     public static <T> Stage stage(MonitorableExecutorService executorService, String name, final Monitorable<Progress> command, List<T> possibleValues, T onException, final Option<Stage> next) {
         if (!possibleValues.contains(onException))
             throw new IllegalArgumentException("The onException parameter [" + onException + "] needs to be included in the list of possible values [" + possibleValues + ']');
 
         return new Stage(executorService, name, command, mapToString(possibleValues), onException.toString(), next);
     }
 
     private static <T> List<String> mapToString(List<T> list) {
         final List<String> results = new ArrayList<String>();
         for (T t: list)
             results.add(t.toString());
         return results;
     }
 
     private Stage(MonitorableExecutorService executorService, String name, final Monitorable<Progress> command, List<String> possibleValues, final String onException, final Option<Stage> next) {
         this.executorService = executorService;
         this.name = name;
         this.command = new Monitorable<Progress>(command.updates) {
             @Override
             public Progress call() throws Exception {
                 Progress result;
                 try {
                     result = command.call();
                 } catch (Exception e) {
                     e.printStackTrace();
                     result = Progress._Failed(0, 100, onException, e.getMessage());
                 }
                 if (!updates.offer(result, 10, TimeUnit.SECONDS)) {
                     final IllegalStateException exception = new IllegalStateException("Could not give " + result + " to the updates queue.");
                     exception.printStackTrace();
                     throw exception;
                 }
 
                 result._switch(new Progress.SwitchBlock() {
                     @Override
                     public void _case(Progress.InProgress x) {
                         throw new IllegalStateException("Should not be able to observe this state.");
                     }
 
                     @Override
                     public void _case(Progress.Complete x) {
                        for (Stage n: Stage.this.next)
                             n.start();
                     }
 
                     @Override
                     public void _case(Progress.Failed x) {
                     }
                 });
                 return result;
             }
         };
         this.possibleValues = possibleValues;
         this.next = next;
     }
 
     public List<String> possibleValues() {
         return possibleValues;
     }
 
     public void start() {
         future = Option.some(executorService.submit(command));
     }
 
     public String name() {
         return name;
     }
 
     public Option<MonitorableFuture<Progress>> future() {
         return future;
     }
 
     @Override
     public Iterator<Stage> iterator() {
         return new Iterator<Stage>() {
             Stage current = null;
             @Override
             public boolean hasNext() {
                 return current == null || current.next.isSome();
             }
 
             @Override
             public Stage next() {
                 if (current == null) {
                     current = Stage.this;
                     return current;
                 }
 
                 return current = current.next.some();
             }
 
             @Override
             public void remove() {
                 throw new UnsupportedOperationException();
             }
         };
     }
 
     public void rerun() {
         future = Option.none();
         start();
     }
 }
