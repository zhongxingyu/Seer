 package net.teerapap.whatnext.service;
 
 import net.teerapap.whatnext.model.Task;
 import net.teerapap.whatnext.model.When;
 import net.teerapap.whatnext.model.WhenCondition;
 
 import java.util.Comparator;
 import java.util.PriorityQueue;
 
 /**
  * It schedules the tasks based on a condition.
  * It orders the tasks by similarity between the condition and task's condition.
  * Created by teerapap on 10/31/13.
  */
 public class SimpleTaskScheduler implements TaskScheduler {
 
     PriorityQueue<Task> mNextQ;
     PriorityQueue<Task> mQ;
     When mCurrentCondition;
     TaskSchedulingListener mListener;
 
     public SimpleTaskScheduler() {
         mCurrentCondition = new WhenCondition();
         Comparator<Task> t = getComparator();
         mQ = new PriorityQueue<Task>(40, t);
         mNextQ = new PriorityQueue<Task>(40, t);
     }
 
     private Comparator<Task> getComparator() {
         // Get a comparator which order compare two tasks based on similarity with the interested condition.
         return new Comparator<Task>() {
 
             private int getSimilarity(When w1) {
                 int n = 0;
                 if (w1.isAtHome() == mCurrentCondition.isAtHome()) n += 10;
                 else if (w1.isAtHome()) n++;
                 if (w1.isAtWork() == mCurrentCondition.isAtWork()) n += 10;
                 else if (w1.isAtWork()) n++;
                 if (w1.isFreeTime() == mCurrentCondition.isFreeTime()) n += 10;
                 else if (w1.isFreeTime()) n++;
                 if (w1.isShopping() == mCurrentCondition.isShopping()) n += 10;
                 else if (w1.isShopping()) n++;
                 return n;
             }
 
             @Override
             public int compare(Task lhs, Task rhs) {
                 return -(getSimilarity(lhs) - getSimilarity(rhs));
             }
         };
     }
 
     private void notifyAfterScheduled() {
         if (mListener == null) return;
 
         Task t = getCurrentTask();
         if (t == null) {
             mListener.onNoTask();
         } else {
             mListener.onTaskScheduled(t);
         }
     }
 
     @Override
     public void reschedule(When condition) {
         if (mCurrentCondition.equals(condition)) return;
 
         if (condition == null) {
             // Reset the queue with old condition.
             mQ.addAll(mNextQ);
             mNextQ.clear();
         } else {
             // Change current condition.
             mCurrentCondition = condition;
 
             // Construct new priority queue based on new condition.
             Comparator<Task> comparator = getComparator();
             PriorityQueue<Task> t = mQ;
            mQ = new PriorityQueue<Task>(t.size() + mNextQ.size() + 1, comparator);
             mQ.addAll(t);
             mQ.addAll(mNextQ);
             mNextQ.clear();
         }
 
         // Notify
         notifyAfterScheduled();
     }
 
     @Override
     public void requestNextTask() {
         // Drop current scheduled task.
         Task t = mQ.poll();
 
         if (t != null) {
             // Add to next queue
             mNextQ.add(t);
         }
 
         if (mQ.size() == 0) { // current queue is empty
             // swap current queue with next queue.
             PriorityQueue<Task> tmp = mQ;
             mQ = mNextQ;
             mNextQ = tmp;
         }
 
         // Notify
         notifyAfterScheduled();
     }
 
     @Override
     public Task getCurrentTask() {
         return mQ.peek();
     }
 
     @Override
     public void addTask(Task task) {
         mQ.add(task);
         if (getCurrentTask().equals(task)) { // if recently added task is scheduled.
             notifyAfterScheduled();
         }
     }
 
     @Override
     public void removeTask(Task task) {
         Task currentTask = getCurrentTask();
 
         if (!mQ.remove(task)) { // if not in current queue
             mNextQ.remove(task);
         } else {
             if (currentTask != getCurrentTask()) { // if current task changes
                 notifyAfterScheduled();
             }
         }
     }
 
     @Override
     public void setTaskSchedulingListener(TaskSchedulingListener listener) {
         mListener = listener;
     }
 }
