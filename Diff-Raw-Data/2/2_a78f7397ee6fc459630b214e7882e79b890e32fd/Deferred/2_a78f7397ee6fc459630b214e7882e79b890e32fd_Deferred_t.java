 package net.avh4.framework.async;
 
 import java.util.concurrent.Semaphore;
 
 public class Deferred<T> implements Promise<T> {
     private T value;
     private boolean isResolved;
    private final Semaphore semaphore = new Semaphore(0);
     private Function<T, ?> pendingCallback;
 
     synchronized public void resolve(T value) {
         this.value = value;
         isResolved = true;
         semaphore.release();
         if (pendingCallback != null) {
             pendingCallback.apply(this.value);
         }
     }
 
     @Override
     public T waitForValue() throws InterruptedException {
         if (!isResolved) {
             semaphore.acquire();
         }
         return value;
     }
 
     @Override
     synchronized public void whenDone(Function<T, ?> callable) {
         if (isResolved) {
             callable.apply(value);
         } else {
             pendingCallback = callable;
         }
     }
 }
