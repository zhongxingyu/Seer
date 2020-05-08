 package org.monome.pages.time;
 
 import groovy.lang.Closure;
 
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 public class MusicalScheduler {
     private int ppq = 96;
     private int bpm = 120;
     private long tickNanoseconds = 0;
     private Set<Task> tasks = new HashSet<Task>();
     private Queue<Task> tasksToAdd = new LinkedList<Task>();
     private Queue<Task> tasksToDelete = new LinkedList<Task>();
 
     private static MusicalScheduler instance;
 
     public static MusicalScheduler getInstance() {
         if (instance == null)
             instance = new MusicalScheduler();
 
         return instance;
     }
 
     public MusicalScheduler() {
         recalculateTickNanoseconds();
     }
 
     public int getBpm() {
         return bpm;
     }
 
     public void setBpm(int bpm) {
         this.bpm = bpm;
         recalculateTickNanoseconds();
     }
 
     public int getPpq() {
         return ppq;
     }
 
     public void setPpq(int ppq) {
         this.ppq = ppq;
         recalculateTickNanoseconds();
     }
 
     public long getTickNanoseconds() {
         return tickNanoseconds;
     }
 
     public void tick() {
         tasks.removeAll(tasksToDelete);
         tasksToDelete.clear();
         tasks.addAll(tasksToAdd);
         tasksToAdd.clear();

        for (Task t : tasks) {
            t.tick();
        }
     }
 
     public Task schedule(MusicalDuration every, Closure closure) {
         Task task = new Task(closure, every);
         task.start();
         return task;
     }
 
     private void recalculateTickNanoseconds() {
         tickNanoseconds = (TimeUnit.SECONDS.toNanos(60) / bpm) / ppq;
     }
 
     public class Task {
         private Closure closure;
         private MusicalDuration duration;
         int ticks = 0;
 
         private Task(Closure closure, MusicalDuration duration) {
             this.closure = closure;
             this.duration = duration;
         }
 
         public void start() {
             ticks = 0;
             tasksToAdd.add(this);
         }
 
         public void stop() {
             tasksToDelete.add(this);
         }
 
         public void tick() {
             if (ticks == 0) {
                 closure.call();
                 ticks = duration.getPulses(getPpq());
             }
             ticks--;
         }
     }
 }
