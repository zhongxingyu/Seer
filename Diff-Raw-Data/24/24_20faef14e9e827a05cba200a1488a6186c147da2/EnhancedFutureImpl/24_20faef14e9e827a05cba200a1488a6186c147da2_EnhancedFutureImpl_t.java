 package net.chrisrichardson.asyncpojos.futures;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 import java.util.concurrent.atomic.AtomicReference;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.springframework.core.task.TaskExecutor;
 
 import com.google.common.base.Function;
 
 /**
  * A fairly lame Future implementation
  */
 public class EnhancedFutureImpl<T> implements EnhancedFuture<T>, Promise {
 
   private Lock lock = new ReentrantLock();
   private Condition outcomeAvailable = lock.newCondition();
 
   private AtomicReference<Outcome<?>> outcomeHolder = new AtomicReference<Outcome<?>>();
   private List<CompletionCallback<?>> completionCallbacks = new ArrayList<CompletionCallback<?>>();
 
   @Override
   public void supplyResult(Object result, final TaskExecutor executionService) {
     final Object self = this;
     if (result instanceof Future) {
       if (!(result instanceof EnhancedFutureImpl))
         throw new RuntimeException("not EnhancedFutureImpl: " + result);
       EnhancedFutureImpl<Object> otherFuture = (EnhancedFutureImpl<Object>) result;
       otherFuture.addCompletionCallback(new CompletionCallback<Object>() {
 
         @Override
         public void onCompletion(Outcome<Object> otherOutcome) {
           if (otherOutcome.isSuccessful())
             supplyResult(otherOutcome.success, executionService);
           else
             supplyThrowable(otherOutcome.failure, executionService);
         }
       });
     } else {
       @SuppressWarnings("unchecked")
      Outcome<T> o = (Outcome<T>) Outcome.makeSuccessfulOutcome(transformResult(result));
       setOutcomeAndNotify(o, executionService);
     }
   }
 
   protected Object transformResult(Object result) {
     return result;
   }
 
   @Override
   public void addCompletionCallback(CompletionCallback<T> completionCallback) {
     Outcome<T> existingOutcome = null;
     lock.lock();
     try {
       existingOutcome = (Outcome<T>) outcomeHolder.get();
       if (existingOutcome == null) {
         completionCallbacks.add(completionCallback);
         return;
       }
     } finally {
       lock.unlock();
     }
     completionCallback.onCompletion(existingOutcome);
   }
 
   private void setOutcomeAndNotify(final Outcome<T> o, TaskExecutor executionService) {
     @SuppressWarnings("rawtypes")
     List<CompletionCallback> callbacksToInvoke = new ArrayList<CompletionCallback>();
     lock.lock();
     try {
       if (!outcomeHolder.compareAndSet(null, o)) {
         throw new RuntimeException("Already supplied: " + this + ", existingOutcome=" + outcomeHolder.get() + ", otherOutcome=" + o);
       }
       outcomeAvailable.signalAll();
       callbacksToInvoke.addAll(completionCallbacks);
     } finally {
       lock.unlock();
     }
     for (@SuppressWarnings("rawtypes") final CompletionCallback<T> callback : callbacksToInvoke) {
       if (executionService == null)
         callback.onCompletion(o);
       else
         executionService.execute(new Runnable() {
   
           @Override
           public void run() {
             callback.onCompletion(o);
           }
         });
     }
   }
 
   @Override
   public void supplyThrowable(Throwable e, TaskExecutor executionService) {
     setOutcomeAndNotify((Outcome<T>) Outcome.makeFailedOutcome(e), executionService);
   }
 
   @Override
   public boolean cancel(boolean b) {
     return false;
   }
 
   @Override
   public boolean isCancelled() {
     return false;
   }
 
   @Override
   public boolean isDone() {
     return outcomeHolder.get() != null;
   }
 
   @Override
   public T get() throws InterruptedException, ExecutionException {
     lock.lock();
     try {
       if (outcomeHolder.get() == null) {
         outcomeAvailable.await();
       }
       return processOutcome(outcomeHolder.get());
     } finally {
       lock.unlock();
     }
   }
 
   private T processOutcome(Outcome o) throws ExecutionException {
     if (o.isSuccessful())
       return (T)o.success;
     else
       throw new ExecutionException(o.failure);
   }
 
   @Override
   public T get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
     lock.lock();
     try {
       if (outcomeHolder.get() == null) {
         if (!outcomeAvailable.await(l, timeUnit))
           throw new TimeoutException();
       }
       return processOutcome(outcomeHolder.get());
     } finally {
       lock.unlock();
     }
   }
 
   @Override
   public void addSuccessCallback(final SuccessCallback<T> successCallback) {
     addCompletionCallback(new CompletionCallback<T>() {
 
       @Override
       public void onCompletion(Outcome<T> outcome) {
         if (outcome.isSuccessful())
           successCallback.onSuccess(outcome.success);
       }
     });
   }
 
   @Override
   public void addFailureCallback(final FailureCallback failureCallback) {
     addCompletionCallback(new CompletionCallback<T>() {
 
       @Override
       public void onCompletion(Outcome<T> outcome) {
         if (!outcome.isSuccessful())
           failureCallback.onFailure(outcome.failure);
       }
     });
   }
 
   @Override
   public EnhancedFuture<T> withTimeout(TimeUnit timeUnit, int amount) {
     // FIXME - make me do something
     return this;
   }
 
   @Override
   public <ResultType> EnhancedFuture<ResultType> map(Function<T, ResultType> function) {
     MappedEnhancedFuture<ResultType, T> result = new MappedEnhancedFuture<ResultType, T>(function);
     pipeTo(result);
     return result;
   }
 
   private void pipeTo(final Promise result) {
     final TaskExecutor executionService = null;
     this.addSuccessCallback(new SuccessCallback<T>() {
 
       @Override
       public void onSuccess(T value) {
         result.supplyResult(value, executionService);
       }
     });
     this.addFailureCallback(new FailureCallback() {
       
       @Override
       public void onFailure(Throwable t) {
         result.supplyThrowable(t, executionService);
       }
     });
   }
 }
