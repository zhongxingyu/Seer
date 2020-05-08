 package org.xmodel.future;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.TimeUnit;
 import org.xmodel.log.Log;
 
 /**
  * An interface for an asynchronous load operation performed by IWebClient.
  */
 public abstract class AsyncFuture<T>
 {
   public enum Status { running, success, failure};
   
   public AsyncFuture( T initiator)
   {
     this.initiator = initiator;
     this.listeners = Collections.synchronizedList( new ArrayList<IListener<T>>());
     this.latch = new Semaphore( 0);
   }
   
   /**
    * @return Returns the initiator of the operation.
    */
   public T getInitiator()
   {
     return initiator;
   }
   
   /**
    * Wait for the future to complete.
    */
   public void await() throws InterruptedException
   {
     await( Integer.MAX_VALUE);
   }
   
   /**
    * Wait for the future to complete.
    * @return Returns false if the timeout expires.
    */
   public boolean await( int timeout) throws InterruptedException
   {
     log.debugf( "await( %x, %d)", hashCode(), timeout);
     return latch.tryAcquire( timeout, TimeUnit.MILLISECONDS);
   }
   
   /**
    * Cancel this operation.
    */
   public abstract void cancel();
   
   /**
    * @return Returns true if the operation was successful.
    */
   public boolean isSuccess()
   {
     return status == Status.success;
   }
   
   /**
    * @return Returns true if the operation was not successful.
    */
   public boolean isFailure()
   {
     return status == Status.failure;
   }
   
   /**
    * @return Returns null or the failure message.
    */
   public String getFailureMessage()
   {
     return message;
   }
   
   /**
    * @return Returns null or the throwable that caused the failure.
    */
   public Throwable getFailureCause()
   {
     return throwable;
   }
   
   /**
    * @return Returns true if the operation is complete.
    */
   public boolean isDone()
   {
     return status != Status.running;
   }
   
   /**
    * Add a listener to this future.
    * @param listener The listener.
    */
   public void addListener( IListener<T> listener)
   {
     log.debugf( "addListener( %x, %x)", hashCode(), listener.hashCode());
     try
     {
       if ( isSuccess())
       {
         listener.notifyComplete( this);
       }
       else if ( isFailure())
       {
         listener.notifyComplete( this);
       }
       else
       {
         listeners.add( listener);
       }
     }
     catch( Exception e)
     {
       log.error( "Future notification failed because: ");
       log.exception( e);
     }
   }
   
   /**
    * Remove a listener from this future.
    * @param listener The listener.
    */
   public void removeListener( IListener<T> listener)
   {
     log.debugf( "removeListener( %x, %x)", hashCode(), listener.hashCode());
     listeners.remove( listener);
   }
   
   /**
    * Notify listeners that the future is complete.
    */
   public void notifySuccess() throws Exception
   {
     log.debugf( "notifySuccess( %x)", hashCode());
     
     status = Status.success;
    latch.release();
     
     for( int i=0; i<listeners.size(); i++)
       listeners.get( i).notifyComplete( this);
   }
   
   /**
    * Notify listeners that the future is complete.
    * @param message The message.
    */
   public void notifyFailure( String message) throws Exception
   {
     log.debugf( "notifyFailure( %x, %s)", hashCode(), message);
     
     this.status = Status.failure;
     this.message = message;
    latch.release();
     
     for( int i=0; i<listeners.size(); i++)
       listeners.get( i).notifyComplete( this);
   }
   
   /**
    * Notify listeners that the future is complete.
    * @param throwable The cause.
    */
   public void notifyFailure( Throwable throwable) throws Exception
   {
     log.debugf( "notifyFailure( %x, %s)", hashCode(), throwable.toString());
     
     this.status = Status.failure;
     this.throwable = throwable;
    latch.release();
     
     for( int i=0; i<listeners.size(); i++)
       listeners.get( i).notifyComplete( this);
   }
   
   public interface IListener<T>
   {
     /**
      * Called when the operation completes.
      * @param future The future.
      */
     public void notifyComplete( AsyncFuture<T> future) throws Exception;
   }
   
   private final static Log log = Log.getLog( AsyncFuture.class);
   
   private T initiator;
   private List<IListener<T>> listeners;
   private Status status;
   private String message;
   private Throwable throwable;
   private Semaphore latch;
 }
