 package org.jtrim.concurrent.executor;
 
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicReference;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jtrim.event.ListenerRef;
 import org.jtrim.utils.ExceptionHelper;
 
 /**
  * Defines a convenient abstract base class for {@link TaskExecutorService}
  * implementations.
  * <P>
  * {@code AbstractTaskExecutorService} defines default implementations for all
  * the {@code submit} and {@code execute} methods which all rely on the protected
  * {@link #submitTask(CancellationToken, CancellationController, CancelableTask, Runnable)}
  * method. Only this {@code submitTask} method is needed to be implemented by
  * subclasses to actually schedule a task. Note that all the {@code submit} and
  * {@code execute} methods rely directly the {@code submitTask} method and
  * overriding any of them has no effect on the others (i.e.: they don't call
  * each other). For further details on how to implement the {@code submitTask}
  * method: see its documentation.
  * <P>
  * {@code AbstractTaskExecutorService} also defines a default implementation for
  * the {@link #awaitTermination(CancellationToken)} method. The implementation
  * of this method simply calls repeatedly the other variant of
  * {@code awaitTermination} until it returns {@code true}.
  *
  * @author Kelemen Attila
  */
 public abstract class AbstractTaskExecutorService
 implements
         TaskExecutorService {
 
     private static final Logger LOGGER = Logger.getLogger(AbstractTaskExecutorService.class.getName());
 
     /**
      * Implementations must override this method to actually execute submitted
      * tasks.
      * <P>
      * Assuming no cancellation requests, implementations must first execute
      * {@code task} then after the task terminates, they must execute
      * {@code cleanupTask} (notice that {@code cleanupTask} is a simple
      * {@code Runnable}). Implementations must ensure that the
      * {@code cleanupTask} is executed always, regardless of the circumstances
      * and they must also ensure, that it is not executed concurrently with
      * {@code task}. Note that {@code AbstractTaskExecutorService} will catch
      * every exception {@code task} may throw (i.e.: anything extending
      * {@code Throwable}, even {@link OperationCanceledException}). Therefore if
      * {@code task} throws an exception, it can be considered an error in
      * {@code AbstractTaskExecutorService}.
      * <P>
      * Cancellation requests can be detected using the provided
      * {@code CancellationToken} and if an implementation chooses not to even
      * try to execute {@code task}, it must only call {@code cleanupTask}. The
      * {@code submit} and {@code execute} implementations assume, that if
      * {@code cleanupTask} has been called, {@code task} will not be called and
      * the task was canceled.
      * <P>
      * It might be possible, that an implementation wishes to cancel
      * {@code task} after it has been started (possibly due to a
      * {@code shutdownAndCancel} request). This can be done by the provided
      * {@link CancellationController} which in will cause the task to be
      * canceled and the passed {@code CancellationToken} signaling cancellation
      * (it will not cause, the {@code CancellationToken} passed to the
      * {@code submit} or the {@code execute} methods to signal cancellation
      * request).
      * <P>
      * The specified {@code cleanupTask} must always be executed (this is the
      * way {@code AbstractTaskExecutorService} is able to detect that the task
      * has terminated) but the {@code hasUserDefinedCleanup} may allow some
      * optimization possibilities for implementations. This is because if there
      * was no user defined cleanup task specified, the {@code cleanupTask} is
      * <I>synchronization transparent</I> and as such, can be called from any
      * context (although it is still recommended not to execute the
      * {@code cleanupTask} while holding a lock).
      * <P>
      * In case there is a user defined cleanup task and it throws an exception,
      * the exception is logged by {@code AbstractTaskExecutorService} with the
      * log level {@code Level.WARNING}. The exception thrown will be hidden from
      * the implementations executing the {@code cleanupTask} and so they may
      * expect the {@code cleanupTask} not to throw an exception.
      * <P>
      * Note that none of the passed argument is {@code null}, this is enforced
      * by the {@code AbstractTaskExecutorService}, so implementations may safely
      * assume the arguments to be non-null and does not need to verify them.
      *
      * @param cancelToken the {@code CancellationToken} which can be checked by
      *   implementations if the currently submitted task has been canceled.
      *   Also this is the {@code CancellationToken} implementations should pass
      *   to {@code task}. This argument cannot be {@code null}.
      * @param cancelController the {@code CancellationController} which can be
      *   used by implementations to make the specified {@code CancellationToken}
      *   signal a cancellation request. This argument cannot be {@code null}.
      * @param task the {@code CancelableTask} whose {@code execute} method is
      *   to be executed. Implementations must execute this task at most once and
      *   only before calling {@code cleanupTask.run()}. This argument cannot be
      *   {@code null}. Note that this task will not throw any exception,
      *   {@code AbstractTaskExecutorService} will catch every exception thrown
      *   by the submitted task.
      * @param cleanupTask the {@code Runnable} whose {@code run} method must be
      *   invoked after the specified task has completed, or the implementation
      *   chooses never to execute the task. This cleanup task must be executed
      *   always regardless of the circumstances, and it must be executed exactly
      *   once. Note that it can be expected that this {@code cleanupTask} does
      *   not throw any exception. This argument cannot be {@code null}.
      * @param hasUserDefinedCleanup {@code true} if the specified
      *   {@code cleanupTask} needs to execute a user defined cleanup task,
      *   {@code false} otherwise. In case this argument is {@code false}, the
      *   {@code cleanupTask} can be considered
      *   <I>synchronization transparent</I>.
      */
     protected abstract void submitTask(
             CancellationToken cancelToken,
             CancellationController cancelController,
             CancelableTask task,
             Runnable cleanupTask,
             boolean hasUserDefinedCleanup);
 
     /**
      * {@inheritDoc }
      */
     @Override
     public void awaitTermination(CancellationToken cancelToken) {
         while (!awaitTermination(cancelToken, Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
             // Repeat until it has been terminated, or throws an exception.
         }
     }
 
     /**
      * {@inheritDoc }
      */
     @Override
     public void execute(
             CancellationToken cancelToken,
             CancelableTask task,
             CleanupTask cleanupTask) {
         callSubmitTask(cancelToken, task, cleanupTask);
     }
 
     /**
      * {@inheritDoc }
      */
     @Override
     public TaskFuture<?> submit(
             CancellationToken cancelToken,
             CancelableTask task,
             CleanupTask cleanupTask) {
         return callSubmitTask(cancelToken, task, cleanupTask);
     }
 
     /**
      * {@inheritDoc }
      */
     @Override
     public <V> TaskFuture<V> submit(
             CancellationToken cancelToken,
             CancelableFunction<V> task,
             CleanupTask cleanupTask) {
         return callSubmitTask(cancelToken, task, cleanupTask);
     }
 
     private <V> TaskFuture<V> callSubmitTask(
             CancellationToken userCancelToken,
             CancelableTask userTask,
             CleanupTask userCleanupTask) {
 
         return callSubmitTask(
                 userCancelToken,
                 new FunctionWrapper<V>(userTask),
                 userCleanupTask);
     }
 
     // This implementation prevents errors (that is, it will always be in a
     // consistent state) from misuses and usually responds with an
     // IllegalStateException (for example if the cleanup task is called
     // multiple times).
     private <V> TaskFuture<V> callSubmitTask(
             CancellationToken userCancelToken,
             CancelableFunction<V> userFunction,
             CleanupTask userCleanupTask) {
         ExceptionHelper.checkNotNullArgument(userCancelToken, "userCancelToken");
         ExceptionHelper.checkNotNullArgument(userFunction, "userFunction");
 
         if (isShutdown() || userCancelToken.isCanceled()) {
             if (userCleanupTask == null) {
                 return CanceledTaskFuture.getCanceledFuture();
             }
 
             Runnable cleanupTask = new UserCleanupWrapper(userCleanupTask, true, null);
             cleanupTask = new RunOnceTask(cleanupTask);
 
             submitTask(
                     CancellationSource.CANCELED_TOKEN,
                     DummyCancellationController.INSTANCE,
                     DummyCancelableTask.INSTANCE,
                     cleanupTask,
                     true);
             return CanceledTaskFuture.getCanceledFuture();
         }
 
         AtomicReference<TaskState> currentState = new AtomicReference<>(TaskState.NOT_STARTED);
         AtomicReference<TaskResult<V>> resultRef = new AtomicReference<>(TaskResult.<V>getCanceledResult());
         SimpleWaitSignal waitDoneSignal = new SimpleWaitSignal();
 
         final CancellationSource newCancelSource = new CancellationSource();
         ListenerRef cancelRef = userCancelToken.addCancellationListener(new Runnable() {
             @Override
             public void run() {
                 newCancelSource.getController().cancel();
             }
         });
 
         TaskFinalizer<V> taskFinalizer = new TaskFinalizer<>(cancelRef,
                 currentState, resultRef, waitDoneSignal, userCleanupTask);
 
         CancelableTask task = new RunOnceCancelableTask(new TaskOfAbstractExecutor<>(
                 userFunction, currentState, resultRef, taskFinalizer));
 
         Runnable cleanupTask = new RunOnceTask(
                 new CleanupTaskOfAbstractExecutor(taskFinalizer));
 
         submitTask(newCancelSource.getToken(), newCancelSource.getController(),
                 task, cleanupTask, userCleanupTask != null);
 
         return new TaskFutureOfAbstractExecutor<>(
                 currentState,
                 resultRef,
                 waitDoneSignal);
     }
 
    private enum DummyCancelableTask implements CancelableTask {
         INSTANCE;
 
         @Override
         public void execute(CancellationToken cancelToken) {
         }
     }
 
    private enum DummyCancellationController implements CancellationController {
         INSTANCE;
 
         @Override
         public void cancel() {
         }
     }
 
     // This class should be factored out at some time in the future.
     private static class SimpleWaitSignal {
         private final Lock lock;
         private final Condition waitSignal;
         private volatile boolean signaled;
 
         public SimpleWaitSignal() {
             this.lock = new ReentrantLock();
             this.waitSignal = lock.newCondition();
             this.signaled = false;
         }
 
         public void signal() {
             signaled = true;
             lock.lock();
             try {
                 waitSignal.signalAll();
             } finally {
                 lock.unlock();
             }
         }
 
         public boolean isSignaled() {
             return signaled;
         }
 
         public void waitSignal(CancellationToken cancelToken) {
             if (signaled) {
                 return;
             }
 
             lock.lock();
             try {
                 while (!signaled) {
                     CancelableWaits.await(cancelToken, waitSignal);
                 }
             } finally {
                 lock.unlock();
             }
         }
 
         public boolean waitSignal(CancellationToken cancelToken,
                 long timeout, TimeUnit timeUnit) {
 
             if (signaled) {
                 return true;
             }
 
             long timeoutNanos = timeUnit.toNanos(timeout);
             long startTime = System.nanoTime();
             lock.lock();
             try {
                 while (!signaled) {
                     long elapsed = System.nanoTime() - startTime;
                     long timeToWait = timeoutNanos - elapsed;
                     if (timeToWait <= 0) {
                         return false;
                     }
                     CancelableWaits.await(cancelToken, timeToWait, TimeUnit.NANOSECONDS, waitSignal);
                 }
                 return true;
             } finally {
                 lock.unlock();
             }
         }
     }
 
     private static class FunctionWrapper<V> implements CancelableFunction<V> {
         private final CancelableTask task;
 
         public FunctionWrapper(CancelableTask task) {
             ExceptionHelper.checkNotNullArgument(task, "task");
             this.task = task;
         }
 
         @Override
         public V execute(CancellationToken cancelToken) {
             task.execute(cancelToken);
             return null;
         }
     }
 
     private static class TaskResult<V> {
         private static final TaskResult<?> CANCELED = new TaskResult<>(null, true);
 
         @SuppressWarnings("unchecked")
         public static <V> TaskResult<V> getCanceledResult() {
             // This cast is safe, since it returns null for the result which
             // is valid for every kind of objects.
             return (TaskResult<V>)CANCELED;
         }
 
         public final V result;
         public final Throwable error;
         public final boolean canceled;
 
         public TaskResult(V result) {
             this.result = result;
             this.error = null;
             this.canceled = false;
         }
 
         public TaskResult(Throwable error, boolean canceled) {
             this.result = null;
             this.error = error;
             this.canceled = canceled;
         }
     }
 
     private static class RunOnceTask implements Runnable {
         private AtomicReference<Runnable> subTaskRef;
 
         public RunOnceTask(Runnable subTask) {
             this.subTaskRef = new AtomicReference<>(subTask);
         }
 
         @Override
         public void run() {
             Runnable subTask = subTaskRef.getAndSet(null);
             if (subTask == null) {
                 throw new IllegalStateException("This task is not allowed to "
                         + "be called multiple times.");
             }
             subTask.run();
         }
     }
 
     private static class RunOnceCancelableTask implements CancelableTask {
         private AtomicReference<CancelableTask> subTaskRef;
 
         public RunOnceCancelableTask(CancelableTask subTask) {
             this.subTaskRef = new AtomicReference<>(subTask);
         }
 
         @Override
         public void execute(CancellationToken cancelToken) {
             CancelableTask subTask = subTaskRef.getAndSet(null);
             if (subTask == null) {
                 throw new IllegalStateException("This task is not allowed to "
                         + "be called multiple times.");
             }
             subTask.execute(cancelToken);
         }
     }
 
     private static class TaskOfAbstractExecutor<V>
     implements
             CancelableTask {
 
         private final CancelableFunction<V> function;
         private final AtomicReference<TaskState> currentState;
         private final AtomicReference<TaskResult<V>> resultRef;
         private final TaskFinalizer<V> taskFinalizer;
 
         public TaskOfAbstractExecutor(
                 CancelableFunction<V> function,
                 AtomicReference<TaskState> currentState,
                 AtomicReference<TaskResult<V>> resultRef, TaskFinalizer<V> taskFinalizer) {
 
             this.function = function;
             this.currentState = currentState;
             this.resultRef = resultRef;
             this.taskFinalizer = taskFinalizer;
         }
 
         @Override
         public void execute(CancellationToken cancelToken) {
             if (!currentState.compareAndSet(TaskState.NOT_STARTED, TaskState.RUNNING)) {
                 throw new IllegalStateException("Multiple execute call "
                         + "of the task of AbstractTaskExecutorService.");
             }
 
             // This default null value should be overwritten in every case
             TaskResult<V> taskResult = null;
             try {
                 V result = function.execute(cancelToken);
                 taskResult = new TaskResult<>(result);
             } catch (OperationCanceledException ex) {
                 taskResult = new TaskResult<>(ex, true);
             } catch (Throwable ex) {
                 taskResult = new TaskResult<>(ex, false);
             } finally {
                 // This check should always succeed (not counting Thread.stop
                 // and similar methods) because of the "catch Throwable".
                 if (taskResult != null) {
                     // resultRef is only set here and due to the first check, it
                     // can only overwrite the initial value
                     resultRef.set(taskResult);
                 }
                 taskFinalizer.finish();
             }
         }
     }
 
     // The job of TaskFinalizer is to set the state of the task to finished.
     // finish() is called from the submitted task and the cleanup method.
     //
     // TaskFinalizer also unregisters the cancellation listener which was only
     // registered to forward cancellation request to the cancellation token
     // passed to submitTask. This cancellation forwarding is not useful after
     // the task completes, since there is nothing we can do at that point.
     //
     // Whenever finish() is called for the first time, the only task remaining
     // is to call the cleanup method which does not need the result of the task
     // and the "cancelRef" (which might have a user provided implementation and
     // if it is poorly written, it may retain considerable memory).
     private static class TaskFinalizer<V> {
         private final AtomicReference<TaskState> currentState;
         private final SimpleWaitSignal waitDoneSignal;
         private final AtomicBoolean finished;
         private final CleanupTask userCleanupTask;
 
         // unset after finish() returns.
         // i.e.: only available for the first call of finish()
         private ListenerRef cancelRef;
         private AtomicReference<TaskResult<V>> resultRef;
 
         // This field is only set after finish() returns, in whic case
         // finished.get() == true
         private volatile Runnable cleanupTask;
 
         public TaskFinalizer(
                 ListenerRef cancelRef,
                 AtomicReference<TaskState> currentState,
                 AtomicReference<TaskResult<V>> resultRef,
                 SimpleWaitSignal waitDoneSignal,
                 CleanupTask userCleanupTask) {
 
             this.cancelRef = cancelRef;
             this.currentState = currentState;
             this.resultRef = resultRef;
             this.waitDoneSignal = waitDoneSignal;
             this.userCleanupTask = userCleanupTask;
             this.finished = new AtomicBoolean(false);
             this.cleanupTask = null;
         }
 
         public Runnable finish() {
             if (!finished.getAndSet(true)) {
                 TaskResult<V> result = resultRef.get();
                 try {
                     // Do not reference it anymore, so that the result of
                     // the task will not be retained until the cleanup
                     // task is done.
                     resultRef = null;
 
                     TaskState terminateState;
                     if (result.canceled) {
                         terminateState = TaskState.DONE_CANCELED;
                     }
                     else if (result.error != null) {
                         terminateState = TaskState.DONE_ERROR;
                     }
                     else {
                         terminateState = TaskState.DONE_COMPLETED;
                     }
 
                     currentState.set(terminateState);
                     waitDoneSignal.signal();
 
                     cancelRef.unregister();
                     cancelRef = null;
                 } finally {
                     if (userCleanupTask != null) {
                         cleanupTask = new UserCleanupWrapper(
                                 userCleanupTask,
                                 result.canceled,
                                 result.error);
                     }
                     else {
                         cleanupTask = NoOp.INSTANCE;
                     }
                 }
             }
 
             Runnable result = cleanupTask;
             if (result == null) {
                 // First of all, finish is called from two places:
                 // from the submitted task and the cleanup method.
                 // If finishResult was not yet set but finished is
                 // already set, it means that the first finish call
                 // is executing concurrently which is not allowed.
                 throw new IllegalStateException(
                         "task and cleanup were called concurrently.");
             }
             return result;
         }
     }
 
     private static class UserCleanupWrapper implements Runnable {
         private final CleanupTask userCleanupTask;
         private final boolean canceled;
         private final Throwable error;
 
         public UserCleanupWrapper(
                 CleanupTask userCleanupTask,
                 boolean canceled,
                 Throwable error) {
 
             this.userCleanupTask = userCleanupTask;
             this.canceled = canceled;
             this.error = error;
         }
 
         @Override
         public void run() {
             try {
                 userCleanupTask.cleanup(canceled, error);
             } catch (Throwable ex) {
                 LOGGER.log(Level.WARNING,
                         "The cleanup task has thrown an exception.", ex);
             }
         }
     }
 
     private static class CanceledTaskFuture<V> implements TaskFuture<V> {
         private static final CanceledTaskFuture<?> CANCELED_FUTURE
                 = new CanceledTaskFuture<>();
 
         @SuppressWarnings("unchecked")
         public static <V> TaskFuture<V> getCanceledFuture() {
             // This is safe because we never actually return any result
             // and throw an exception instead.
             return (TaskFuture<V>)CANCELED_FUTURE;
         }
 
         @Override
         public TaskState getTaskState() {
             return TaskState.DONE_CANCELED;
         }
 
         @Override
         public V tryGetResult() {
             throw new OperationCanceledException();
         }
 
         @Override
         public V waitAndGet(CancellationToken cancelToken) {
             return tryGetResult();
         }
 
         @Override
         public V waitAndGet(CancellationToken cancelToken, long timeout, TimeUnit timeUnit) {
             return tryGetResult();
         }
     }
 
     private enum NoOp implements Runnable {
         INSTANCE;
 
         @Override
         public void run() { }
     }
 
     private static class CleanupTaskOfAbstractExecutor implements Runnable {
         private final TaskFinalizer<?> taskFinalizer;
 
         public CleanupTaskOfAbstractExecutor(TaskFinalizer<?> taskFinalizer) {
             this.taskFinalizer = taskFinalizer;
         }
 
         @Override
         public void run() {
             taskFinalizer.finish().run();
         }
     }
 
     private static class TaskFutureOfAbstractExecutor<V>
     implements
             TaskFuture<V> {
 
         private final AtomicReference<TaskState> currentState;
         private final AtomicReference<TaskResult<V>> resultRef;
         private final SimpleWaitSignal waitDoneSignal;
 
         public TaskFutureOfAbstractExecutor(
                 AtomicReference<TaskState> currentState,
                 AtomicReference<TaskResult<V>> resultRef,
                 SimpleWaitSignal waitDoneSignal) {
 
             this.currentState = currentState;
             this.resultRef = resultRef;
             this.waitDoneSignal = waitDoneSignal;
         }
 
         @Override
         public TaskState getTaskState() {
             return currentState.get();
         }
 
         private V fetchResult() {
             assert getTaskState().isDone();
 
             TaskResult<V> result = resultRef.get();
             Throwable resultException = result.error;
             if (resultException != null) {
                 throw new TaskExecutionException(resultException);
             }
             else if (result.canceled) {
                 // We pass the causing exception, to preserve the stack
                 // trace of the point of cancellation.
                 throw new OperationCanceledException(resultException);
             }
             return result.result;
         }
 
         @Override
         public V tryGetResult() {
             if (getTaskState().isDone()) {
                 return fetchResult();
             }
             return getTaskState().isDone() ? fetchResult() : null;
         }
 
         @Override
         public V waitAndGet(CancellationToken cancelToken) {
             waitDoneSignal.waitSignal(cancelToken);
             return fetchResult();
         }
 
         @Override
         public V waitAndGet(CancellationToken cancelToken, long timeout, TimeUnit timeUnit) {
             if (!waitDoneSignal.waitSignal(cancelToken, timeout, timeUnit)) {
                 throw new OperationCanceledException();
             }
             return fetchResult();
         }
     }
 }
