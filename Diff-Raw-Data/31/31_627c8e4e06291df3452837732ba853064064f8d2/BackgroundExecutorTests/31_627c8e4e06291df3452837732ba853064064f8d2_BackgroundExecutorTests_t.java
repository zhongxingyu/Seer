 package org.jtrim.concurrent;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicReference;
 import java.util.logging.Level;
 import org.jtrim.cancel.Cancellation;
 import org.jtrim.cancel.CancellationToken;
 import org.jtrim.cancel.OperationCanceledException;
 import org.jtrim.cancel.TestCancellationSource;
 import org.jtrim.utils.ExceptionHelper;
 import org.jtrim.utils.LogCollector;
 import org.jtrim.utils.LogCollectorTest;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.mockito.stubbing.Stubber;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 /**
  *
  * @author Kelemen Attila
  */
 public final class BackgroundExecutorTests {
     // Waits until the specified executor terminates and tests
     // if the terminate listener has been called.
     private static void waitTerminateAndTest(final TaskExecutorService executor) throws InterruptedException {
         final CountDownLatch listener1Latch = new CountDownLatch(1);
         executor.addTerminateListener(new Runnable() {
             @Override
             public void run() {
                 listener1Latch.countDown();
             }
         });
         executor.awaitTermination(Cancellation.UNCANCELABLE_TOKEN);
         assertTrue(executor.isTerminated());
         listener1Latch.await();
 
         final AtomicReference<Thread> callingThread = new AtomicReference<>(null);
         executor.addTerminateListener(new Runnable() {
             @Override
             public void run() {
                 callingThread.set(Thread.currentThread());
             }
         });
         assertSame(Thread.currentThread(), callingThread.get());
     }
 
     private static void ensureBackgroundThreadStarted(TaskExecutorService executor) {
         executor.submit(Cancellation.UNCANCELABLE_TOKEN, Tasks.noOpCancelableTask(), null)
                 .waitAndGet(Cancellation.UNCANCELABLE_TOKEN, 5, TimeUnit.SECONDS);
     }
 
     private static void testShutdownAllowsPreviouslySubmittedTasksOnce(
             Factory<?> factory,
             boolean preStartThread,
             boolean zeroIdleTime,
             boolean testCleanup) throws InterruptedException {
 
         final AtomicReference<Throwable> error = new AtomicReference<>();
 
         String executorName = "testShutdownAllowsPreviouslySubmittedTasks";
         final TaskExecutorService executor = zeroIdleTime
                 ? factory.create(executorName, Integer.MAX_VALUE, 0, TimeUnit.NANOSECONDS)
                 : factory.create(executorName);
         try {
             final AtomicBoolean taskCompleted = new AtomicBoolean(false);
 
             if (preStartThread) {
                 ensureBackgroundThreadStarted(executor);
             }
             if (testCleanup) {
                 executor.execute(Cancellation.UNCANCELABLE_TOKEN, Tasks.noOpCancelableTask(), new CleanupTask() {
                     @Override
                     public void cleanup(boolean canceled, Throwable error) {
                         taskCompleted.set(true);
                     }
                 });
             }
             else {
                 executor.execute(Cancellation.UNCANCELABLE_TOKEN, new CancelableTask() {
                     @Override
                     public void execute(CancellationToken cancelToken) throws Exception {
                         taskCompleted.set(true);
                     }
                 }, null);
             }
             executor.shutdown();
 
             executor.awaitTermination(Cancellation.UNCANCELABLE_TOKEN);
             assertTrue("Task must have been completed.", taskCompleted.get());
         } finally {
             executor.shutdown();
             waitTerminateAndTest(executor);
         }
 
         ExceptionHelper.rethrowIfNotNull(error.get());
     }
 
     private static void testShutdownAllowsPreviouslySubmittedTasks(
             final Factory<?> factory,
             final boolean preStartThread,
             final boolean zeroIdleTime,
             final boolean testCleanup) throws InterruptedException {
 
         Runnable[] tasks = new Runnable[Runtime.getRuntime().availableProcessors() * 4];
         Arrays.fill(tasks, new Runnable() {
             @Override
             public void run() {
                 try {
                     testShutdownAllowsPreviouslySubmittedTasksOnce(
                             factory, preStartThread, zeroIdleTime, testCleanup);
                 } catch (InterruptedException ex) {
                     throw new RuntimeException("Unexpected interrupt.", ex);
                 }
             }
         });
 
         for (int i = 0; i < 25; i++) {
             Tasks.runConcurrently(tasks);
         }
     }
 
     @GenericTest
    public static void testShutdownAllowsPreviouslySubmittedTasks1(Factory<?> factory)
            throws InterruptedException {
         testShutdownAllowsPreviouslySubmittedTasks(factory, false, false, false);
     }
 
     @GenericTest
    public static void testShutdownAllowsPreviouslySubmittedTasks2(Factory<?> factory)
            throws InterruptedException {
         testShutdownAllowsPreviouslySubmittedTasks(factory, true, false, false);
     }
 
     @GenericTest
    public static void testShutdownAllowsPreviouslySubmittedTasks3(Factory<?> factory)
            throws InterruptedException {
         testShutdownAllowsPreviouslySubmittedTasks(factory, false, true, false);
     }
 
     @GenericTest
    public static void testShutdownAllowsPreviouslySubmittedTasks4(Factory<?> factory)
            throws InterruptedException {
         testShutdownAllowsPreviouslySubmittedTasks(factory, true, true, false);
     }
 
     @GenericTest
    public static void testShutdownAllowsPreviouslySubmittedCleanupTasks1(Factory<?> factory)
            throws InterruptedException {
         testShutdownAllowsPreviouslySubmittedTasks(factory, false, false, true);
     }
 
     @GenericTest
    public static void testShutdownAllowsPreviouslySubmittedCleanupTasks2(Factory<?> factory)
            throws InterruptedException {
         testShutdownAllowsPreviouslySubmittedTasks(factory, true, false, true);
     }
 
     @GenericTest
    public static void testShutdownAllowsPreviouslySubmittedCleanupTasks3(Factory<?> factory)
            throws InterruptedException {
         testShutdownAllowsPreviouslySubmittedTasks(factory, false, true, true);
     }
 
     @GenericTest
    public static void testShutdownAllowsPreviouslySubmittedCleanupTasks4(Factory<?> factory)
            throws InterruptedException {
         testShutdownAllowsPreviouslySubmittedTasks(factory, true, true, true);
     }
 
     @GenericTest
     public static void testInterruptDoesntBreakExecutor(Factory<?> factory) throws Exception {
         CancelableTask secondTask = mock(CancelableTask.class);
 
         final TaskExecutorService executor = factory.create("testInterruptDoesntBreakExecutor");
         try {
             executor.execute(Cancellation.UNCANCELABLE_TOKEN, new CancelableTask() {
                 @Override
                 public void execute(CancellationToken cancelToken) throws Exception {
                     Thread.currentThread().interrupt();
                 }
             }, new CleanupTask() {
                 @Override
                 public void cleanup(boolean canceled, Throwable error) throws Exception {
                     Thread.currentThread().interrupt();
                 }
             });
 
             Thread.sleep(50);
             executor.execute(Cancellation.UNCANCELABLE_TOKEN, secondTask, null);
         } finally {
             executor.shutdown();
             waitTerminateAndTest(executor);
         }
 
         verify(secondTask).execute(any(CancellationToken.class));
     }
 
     @GenericTest
     public static void testDoesntTerminateBeforeTaskCompletes1(Factory<?> factory) throws InterruptedException {
         final AtomicReference<Throwable> error = new AtomicReference<>();
         final TaskExecutorService executor = factory.create("");
         try {
             final WaitableSignal mayWaitSignal = new WaitableSignal();
             final AtomicBoolean taskCompleted = new AtomicBoolean(false);
 
             executor.execute(Cancellation.UNCANCELABLE_TOKEN, new CancelableTask() {
                 @Override
                 public void execute(CancellationToken cancelToken) throws Exception {
                     try {
                         executor.shutdown();
                         mayWaitSignal.signal();
                         assertTrue("Should be shut down.", executor.isShutdown());
                         Thread.sleep(50);
                         assertFalse("Should not be terminated", executor.isTerminated());
                         taskCompleted.set(true);
                     } catch (Throwable ex) {
                         error.set(ex);
                     }
                 }
             }, null);
 
             if (!mayWaitSignal.tryWaitSignal(Cancellation.UNCANCELABLE_TOKEN, 10, TimeUnit.SECONDS)) {
                 throw new OperationCanceledException("timeout");
             }
             executor.awaitTermination(Cancellation.UNCANCELABLE_TOKEN);
             assertTrue("Task must have been completed.", taskCompleted.get());
         } finally {
             executor.shutdown();
             waitTerminateAndTest(executor);
         }
 
         ExceptionHelper.rethrowIfNotNull(error.get());
     }
 
     @GenericTest
     public static void testDoesntTerminateBeforeTaskCompletes2(Factory<?> factory) throws InterruptedException {
         final AtomicReference<Throwable> error = new AtomicReference<>();
         final TaskExecutorService executor = factory.create("");
         try {
             final WaitableSignal mayRunTaskSignal = new WaitableSignal();
             final AtomicBoolean taskCompleted = new AtomicBoolean(false);
             executor.execute(Cancellation.UNCANCELABLE_TOKEN, new CancelableTask() {
                 @Override
                 public void execute(CancellationToken cancelToken) throws Exception {
                     try {
                         mayRunTaskSignal.waitSignal(Cancellation.UNCANCELABLE_TOKEN);
                         Thread.sleep(50);
                         taskCompleted.set(true);
                     } catch (Throwable ex) {
                         error.set(ex);
                     }
                 }
             }, null);
             executor.shutdown();
             mayRunTaskSignal.signal();
 
             executor.awaitTermination(Cancellation.UNCANCELABLE_TOKEN);
             assertTrue("Task must have been completed.", taskCompleted.get());
         } finally {
             executor.shutdown();
             waitTerminateAndTest(executor);
         }
 
         ExceptionHelper.rethrowIfNotNull(error.get());
     }
 
     @GenericTest
     public static void testSubmitTaskNoCleanup(Factory<?> factory) throws InterruptedException {
         TaskExecutorService executor = factory.create("");
         try {
             final Object taskResult = "TASK-RESULT";
 
             TaskFuture<?> future = executor.submit(
                     Cancellation.UNCANCELABLE_TOKEN,
                     new CancelableFunction<Object>() {
                 @Override
                 public Object execute(CancellationToken cancelToken) {
                     return taskResult;
                 }
             }, null);
 
             Object result = future.waitAndGet(Cancellation.UNCANCELABLE_TOKEN);
             assertSame(taskResult, result);
             assertEquals(TaskState.DONE_COMPLETED, future.getTaskState());
         } finally {
             executor.shutdown();
             waitTerminateAndTest(executor);
         }
     }
 
     @GenericTest
     public static void testSubmitTaskWithCleanup(Factory<?> factory) throws InterruptedException {
         TaskExecutorService executor = factory.create("");
         try {
             final Object taskResult = "TASK-RESULT";
             final CountDownLatch cleanupLatch = new CountDownLatch(1);
 
             TaskFuture<?> future = executor.submit(
                     Cancellation.UNCANCELABLE_TOKEN,
                     new CancelableFunction<Object>() {
                 @Override
                 public Object execute(CancellationToken cancelToken) {
                     return taskResult;
                 }
             },
                     new CleanupTask() {
                 @Override
                 public void cleanup(boolean canceled, Throwable error) throws Exception {
                     cleanupLatch.countDown();
                 }
             });
 
             Object result = future.waitAndGet(Cancellation.UNCANCELABLE_TOKEN);
             assertSame(taskResult, result);
             assertEquals(TaskState.DONE_COMPLETED, future.getTaskState());
             cleanupLatch.await();
         } finally {
             executor.shutdown();
             waitTerminateAndTest(executor);
         }
     }
 
     @GenericTest
     public static void testShutdownWithCleanups(Factory<?> factory) {
         int taskCount = 100;
 
         TaskExecutorService executor = factory.create("TEST-POOL");
         try {
             final AtomicInteger execCount = new AtomicInteger(0);
             final WaitableSignal cleanupSignal = new WaitableSignal();
             CleanupTask cleanupTask = new CleanupTask() {
                 @Override
                 public void cleanup(boolean canceled, Throwable error) {
                     execCount.incrementAndGet();
                     cleanupSignal.signal();
                 }
             };
 
             for (int i = 0; i < taskCount; i++) {
                 executor.execute(
                         Cancellation.UNCANCELABLE_TOKEN,
                         Tasks.noOpCancelableTask(),
                         cleanupTask);
             }
             executor.shutdown();
             executor.awaitTermination(Cancellation.UNCANCELABLE_TOKEN);
             cleanupSignal.waitSignal(Cancellation.UNCANCELABLE_TOKEN);
             assertEquals(taskCount, execCount.get());
         } finally {
             executor.shutdown();
         }
     }
 
     private static void doTestCanceledShutdownWithCleanups(Factory<?> factory) throws Exception {
         int taskCount = 100;
 
         TaskExecutorService executor = factory.create("TEST-POOL");
         try {
             final CountDownLatch cleanupLatch = new CountDownLatch(taskCount);
             CleanupTask cleanupTask = new CleanupTask() {
                 @Override
                 public void cleanup(boolean canceled, Throwable error) {
                     cleanupLatch.countDown();
                 }
             };
 
             TestCancellationSource cancelSource = newCancellationSource();
             for (int i = 0; i < taskCount; i++) {
                 executor.execute(
                         cancelSource.getToken(),
                         Tasks.noOpCancelableTask(),
                         cleanupTask);
             }
             cancelSource.getController().cancel();
             executor.shutdown();
             executor.awaitTermination(Cancellation.UNCANCELABLE_TOKEN);
             cleanupLatch.await();
             cancelSource.checkNoRegistration();
         } finally {
             executor.shutdown();
             executor.awaitTermination(Cancellation.UNCANCELABLE_TOKEN);
         }
     }
 
     @GenericTest
     public static void testCanceledShutdownWithCleanups(Factory<?> factory) throws Exception {
         for (int i = 0; i < 100; i++) {
             doTestCanceledShutdownWithCleanups(factory);
         }
     }
 
     private static void doTestCancellationWithCleanups(Factory<?> factory) {
         int taskCount = 100;
 
         TestCancellationSource cancelSource = newCancellationSource();
         TaskExecutorService executor = factory.create("TEST-POOL");
         try {
             final CountDownLatch latch = new CountDownLatch(taskCount);
             CleanupTask cleanupTask = new CleanupTask() {
                 @Override
                 public void cleanup(boolean canceled, Throwable error) {
                     latch.countDown();
                 }
             };
 
             for (int i = 0; i < taskCount; i++) {
                 executor.execute(
                         cancelSource.getToken(),
                         Tasks.noOpCancelableTask(),
                         cleanupTask);
             }
             cancelSource.getController().cancel();
 
             latch.await();
         } catch (InterruptedException ex) {
             Thread.currentThread().interrupt();
             throw new OperationCanceledException(ex);
         } finally {
             executor.shutdown();
             executor.awaitTermination(Cancellation.UNCANCELABLE_TOKEN);
         }
         cancelSource.checkNoRegistration();
     }
 
     @GenericTest
     public static void testCancellationWithCleanups(Factory<?> factory) {
         for (int i = 0; i < 100; i++) {
             doTestCancellationWithCleanups(factory);
         }
     }
 
     @GenericTest
     public static void testContextAwarenessInTask(Factory<?> factory) throws InterruptedException {
         final TaskExecutorService executor = factory.create("", 1);
         assertFalse("ExecutingInThis", ((MonitorableTaskExecutor)executor).isExecutingInThis());
 
         try {
             final WaitableSignal taskSignal = new WaitableSignal();
             final AtomicBoolean inContext = new AtomicBoolean();
 
             executor.execute(Cancellation.UNCANCELABLE_TOKEN, new CancelableTask() {
                 @Override
                 public void execute(CancellationToken cancelToken) {
                     inContext.set(((MonitorableTaskExecutor)executor).isExecutingInThis());
                     taskSignal.signal();
                 }
             }, null);
 
             taskSignal.waitSignal(Cancellation.UNCANCELABLE_TOKEN);
             assertTrue("ExecutingInThis", inContext.get());
         } finally {
             executor.shutdown();
             waitTerminateAndTest(executor);
         }
     }
 
     @GenericTest
     public static void testContextAwarenessInCleanup(Factory<?> factory) throws InterruptedException {
         final TaskExecutorService executor = factory.create("");
         try {
             final WaitableSignal taskSignal = new WaitableSignal();
             final AtomicBoolean inContext = new AtomicBoolean();
 
             executor.execute(Cancellation.UNCANCELABLE_TOKEN, Tasks.noOpCancelableTask(), new CleanupTask() {
                 @Override
                 public void cleanup(boolean canceled, Throwable error) {
                     inContext.set(((MonitorableTaskExecutor)executor).isExecutingInThis());
                     taskSignal.signal();
                 }
             });
 
             taskSignal.waitSignal(Cancellation.UNCANCELABLE_TOKEN);
             assertTrue("ExecutingInThis", inContext.get());
         } finally {
             executor.shutdown();
             waitTerminateAndTest(executor);
         }
     }
 
     @GenericTest
     public static void testToString(Factory<?> factory) {
         TaskExecutorService executor = factory.create("");
         try {
             assertNotNull(executor.toString());
         } finally {
             executor.shutdown();
         }
     }
 
     private static Stubber doAnswerSignal(final WaitableSignal calledSignal) {
         return doAnswer(new Answer<Void>() {
             @Override
             public Void answer(InvocationOnMock invocation) throws Throwable {
                 calledSignal.signal();
                 return null;
             }
         });
     }
 
     @GenericTest
     public static void testShutdownAndCancel(Factory<?> factory) throws Exception {
         final TaskExecutorService executor = factory.create("");
         try {
             CancelableTask task2 = mock(CancelableTask.class);
             CleanupTask cleanup1 = mock(CleanupTask.class);
             CleanupTask cleanup2 = mock(CleanupTask.class);
 
             final WaitableSignal cleanup1Signal = new WaitableSignal();
             final WaitableSignal cleanup2Signal = new WaitableSignal();
 
             doAnswerSignal(cleanup1Signal).when(cleanup1).cleanup(anyBoolean(), any(Throwable.class));
             doAnswerSignal(cleanup2Signal).when(cleanup2).cleanup(anyBoolean(), any(Throwable.class));
 
             final List<Boolean> cancellation = new LinkedList<>();
             TaskFuture<?> future1 = executor.submit(Cancellation.UNCANCELABLE_TOKEN, new CancelableTask() {
                 @Override
                 public void execute(CancellationToken cancelToken) throws Exception {
                     cancellation.add(cancelToken.isCanceled());
                     executor.shutdownAndCancel();
                     cancellation.add(cancelToken.isCanceled());
                 }
             }, cleanup1);
             TaskFuture<?> future2 = executor.submit(Cancellation.UNCANCELABLE_TOKEN, task2, cleanup2);
 
             executor.awaitTermination(Cancellation.UNCANCELABLE_TOKEN);
 
             cleanup1Signal.waitSignal(Cancellation.UNCANCELABLE_TOKEN);
             cleanup2Signal.waitSignal(Cancellation.UNCANCELABLE_TOKEN);
 
             verify(cleanup1).cleanup(false, null);
             verify(cleanup2).cleanup(true, null);
             verifyNoMoreInteractions(cleanup1, cleanup2);
             verifyZeroInteractions(task2);
 
             assertEquals(TaskState.DONE_COMPLETED, future1.getTaskState());
             assertEquals(TaskState.DONE_CANCELED, future2.getTaskState());
             assertEquals(Arrays.asList(false, true), cancellation);
         } finally {
             executor.shutdown();
             waitTerminateAndTest(executor);
         }
     }
 
     @GenericTest
     public static void testSubmitTasksAfterShutdown(Factory<?> factory) throws Exception {
         int taskCount = 100;
 
         CancelableTask task1 = mock(CancelableTask.class);
         CancelableTask task2 = mock(CancelableTask.class);
 
         CleanupTask cleanup1 = mock(CleanupTask.class);
         CleanupTask cleanup2 = mock(CleanupTask.class);
 
         TaskExecutorService executor = factory.create("TEST-POOL");
         try {
             for (int i = 0; i < taskCount; i++) {
                 executor.submit(Cancellation.UNCANCELABLE_TOKEN, task1, cleanup1);
             }
 
             executor.shutdown();
 
             for (int i = 0; i < taskCount; i++) {
                 executor.submit(Cancellation.UNCANCELABLE_TOKEN, task2, cleanup2);
             }
         } finally {
             executor.shutdown();
             waitTerminateAndTest(executor);
         }
 
         verify(task1, times(taskCount)).execute(any(CancellationToken.class));
         verifyZeroInteractions(task2);
 
         verify(cleanup1, times(taskCount)).cleanup(false, null);
         verify(cleanup2, times(taskCount)).cleanup(true, null);
     }
 
     @GenericTest
     public static void testAwaitTerminationTimeout(Factory<?> factory) {
         TaskExecutorService executor = factory.create("");
         try {
             assertFalse(executor.tryAwaitTermination(Cancellation.UNCANCELABLE_TOKEN, 100, TimeUnit.NANOSECONDS));
         } finally {
             executor.shutdown();
         }
     }
 
     @GenericTest
     public static void testPlainTaskWithError(Factory<?> factory) throws Exception {
         CancelableTask task1 = mock(CancelableTask.class);
         CancelableTask task2 = mock(CancelableTask.class);
 
         doThrow(new TestException())
                 .when(task1)
                 .execute(any(CancellationToken.class));
 
         TaskExecutorService executor = factory.create("");
         try (LogCollector logs = LogCollectorTest.startCollecting()) {
             try {
                 executor.execute(Cancellation.UNCANCELABLE_TOKEN, task1, null);
                 executor.execute(Cancellation.UNCANCELABLE_TOKEN, task2, null);
             } finally {
                 executor.shutdown();
                 waitTerminateAndTest(executor);
                 LogCollectorTest.verifyLogCount(TestException.class, Level.SEVERE, 1, logs);
             }
         }
 
         verify(task1).execute(any(CancellationToken.class));
         verify(task2).execute(any(CancellationToken.class));
         verifyNoMoreInteractions(task1, task2);
     }
 
     private static void runTestTerminatedAfterAwaitTermination(final Factory<?> factory) {
         Runnable testTask = new Runnable() {
             @Override
             public void run() {
                 TaskExecutorService executor = factory.create("Executor-testTerminatedAfterAwaitTermination");
                 try {
                     executor.execute(Cancellation.UNCANCELABLE_TOKEN, Tasks.noOpCancelableTask(), null);
                     executor.shutdown();
                     executor.awaitTermination(Cancellation.UNCANCELABLE_TOKEN);
                     assertTrue("Must be terminated after awaitTermination.", executor.isTerminated());
                 } finally {
                     executor.shutdown();
                 }
             }
         };
 
         Runnable[] testTasks = new Runnable[2 * Runtime.getRuntime().availableProcessors()];
         Arrays.fill(testTasks, testTask);
         Tasks.runConcurrently(testTasks);
     }
 
     @GenericTest
     public static void testTerminatedAfterAwaitTermination(final Factory<?> factory) {
         for (int i = 0; i < 100; i++) {
             runTestTerminatedAfterAwaitTermination(factory);
         }
     }
 
     private static TestCancellationSource newCancellationSource() {
         return new TestCancellationSource();
     }
 
     private enum TimeoutChangeType {
         NO_CHANGE,
         INCREASE,
         DECREASE,
         ZERO_TIMEOUT
     }
 
     private static class TestException extends RuntimeException {
         private static final long serialVersionUID = 1L;
     }
 
     public interface Factory<T extends TaskExecutorService & MonitorableTaskExecutor> {
         public T create(String poolName);
         public T create(String poolName, int maxQueueSize);
         public T create(
                 String poolName,
                 int maxQueueSize,
                 long idleTimeout,
                 TimeUnit timeUnit);
     }
 
     @Retention(RetentionPolicy.RUNTIME)
     @Target({ElementType.METHOD})
     private @interface GenericTest {
     }
 
     private BackgroundExecutorTests() {
         throw new AssertionError();
     }
 }
