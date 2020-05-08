 package mi.task;
 
 import org.junit.Test;
 
 import java.util.concurrent.Executors;
 
 public class TaskTest {
     @Test
     public void testTask() throws Exception {
         // Executed in current thread.
         // We use a Waiter to force main thread to wait the async task.
         // The Waiter, which can be considered as a traditional java future, is a friendly utility for testing.
         // Don't use it in a pure async program, for it may block the execution.
         Waiter<Integer> waiter1 = testAsync(1).continueWithWaiter();
         waiter1.execute(new SynchronizedScheduler());
         System.out.println(waiter1.getResult());
 
         // Executed in a thread pool.
         Waiter<Integer> waiter2 = testAsync(2).continueWithWaiter();
         waiter2.execute(new ExecutorScheduler(Executors.newSingleThreadExecutor()));
         System.out.println(waiter2.getResult());
     }
 
     /**
      * Build an async method running on a thread pool.
      * @param a
      * @param b
      * @param func
      */
     public static void addCallback(final int a, final int b, final Action1<Integer> func) {
         Executors.newSingleThreadExecutor().execute(new Runnable() {
             @Override
             public void run() {
                 func.apply(a + b);
             }
         });
     }
 
     /**
      * Wrap a callback based async method.
      * @param a
      * @param b
      * @return
      */
     public static ITask<Integer> addAsync(int a, int b) {
         return addAsyncTask.initWithState(new int[]{a, b});
     }
 
     /**
      * This task is stateless, so we can build the task once and always reuse it.
      * Most primitive tasks are stateless, but {@link Waiter} is stateful.
      */
     private static final Task<Integer> addAsyncTask = Task.from(new Action1<Context<int[], Integer>>() {
         @Override
         public void apply(final Context<int[], Integer> context) {
             int a = context.getState()[0];
             int b = context.getState()[1];
             addCallback(a, b, new Action1<Integer>() {
                 @Override
                 public void apply(Integer value) {
                     context.resume(value);
                 }
             });
         }
     });
 
     /**
      * We combine several async actions to build a new one.
      * Don't Repeat Yourself when such action sequence must be reused.
      * @return
      */
     public Task<Integer> testAsync(int value) {
         return testAsyncTask.initWithState(value);
     }
 
     // This task is also stateless, so we can build the task once and always reuse it.
     private static final Task<Integer> testAsyncTask = Task.from(new Func1<Integer, Integer>() {
         // Create a task from a normal function.
         @Override
         public Integer apply(Integer value) {
             return value;
         }
     }).continueWith(new Action1<Context<Integer, Integer>>() {
         @Override
         public void apply(final Context<Integer, Integer> context) {
             addCallback(context.getState(), 1, new Action1<Integer>() {
                 @Override
                 public void apply(Integer value) {
                     context.resume(value);
                 }
             });
         }
     }).continueWith(new Func1<Integer, ITask<Integer>>() {
         // Serialize tasks.
         @Override
         public ITask<Integer> apply(Integer value) {
             // A multi-branch dispatch.
             if (value == 1) {
                 // Run an async function to produce a nested task: ITask<ITask<Integer>>.
                 return addAsync(value, 1);
             }
             return addAsync(value, 2);
         }
     }).flattenAndContinueWith(new Func1<Integer, Task<Integer>>() {
         // We must flatten the nested task(schedule the nested task) before we use the Integer.
         @Override
         public Task<Integer> apply(final Integer value) {
             // Fan out 2 tasks.
             final Task<Integer> task2 = Task.from(new Func0<Integer>() {
                 @Override
                 public Integer apply() {
                     return value * 2;
                 }
             });
 
             final Task<Integer> task3 = Task.from(new Func0<Integer>() {
                 @Override
                 public Integer apply() {
                     return value * 3;
                 }
             });
 
             // Collect results of the 2 task2
             return Task.continueWhenAll(task2, task3).continueWith(new Func1<Object[], Integer>() {
                 @Override
                 public Integer apply(Object[] value) {
                     return (Integer) value[0] + (Integer) value[1];
                 }
             });
         }
     }).flattenAndContinueWith(new Func1<Integer, Integer>() {
         @Override
         public Integer apply(Integer value) {
             return value * 2;
         }
     });
 }
