 package org.kelemenattila.rectlife.concurrent;
 
 import java.util.concurrent.ForkJoinPool;
 import java.util.concurrent.RecursiveAction;
 import org.jtrim.utils.ExceptionHelper;
 
 /**
  *
  * @author Kelemen Attila
  */
 public final class ForkJoinUtils {
     public static void forAll(
             ForkJoinPool pool,
             int startInclusive,
             int endExclusive,
             int threshold,
             IntRangeTask task) {
         ExceptionHelper.checkNotNullArgument(pool, "pool");
         ExceptionHelper.checkArgumentInRange(threshold, 1, Integer.MAX_VALUE, "threshold");
         ExceptionHelper.checkNotNullArgument(task, "task");
 
         pool.invoke(new ForAllAction(startInclusive, endExclusive, threshold, task));
     }
 
     @SuppressWarnings("serial")
     private static final class ForAllAction extends RecursiveAction {
         private final int startInclusive;
         private final int endExclusive;
         private final int threshold;
         private final IntRangeTask task;
 
         public ForAllAction(int startInclusive, int endExclusive, int threshold, IntRangeTask task) {
             this.startInclusive = startInclusive;
             this.endExclusive = endExclusive;
             this.threshold = threshold;
             this.task = task;
         }
 
         @Override
         protected void compute() {
             if (endExclusive - startInclusive <= threshold) {
                 task.doWork(startInclusive, endExclusive);
             }
             else {
                 int midPoint = (endExclusive - startInclusive) / 2 + startInclusive;
                 invokeAll(
                         new ForAllAction(startInclusive, midPoint, threshold, task),
                        new ForAllAction(midPoint + 1, endExclusive, threshold, task));
             }
         }
     }
 
     private ForkJoinUtils() {
         throw new AssertionError();
     }
 }
