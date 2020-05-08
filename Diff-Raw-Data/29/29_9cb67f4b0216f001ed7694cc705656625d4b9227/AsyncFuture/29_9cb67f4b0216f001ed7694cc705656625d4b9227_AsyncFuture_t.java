 package org.xmodel.future;
 
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.TimeUnit;
 import org.xmodel.log.Log;
 
 /**
  * An interface for an asynchronous load operation performed by IWebClient.
  */
 public abstract class AsyncFuture<T>
 {
   public interface IListener<T>
   {
     /**
      * Called when the operation completes.
      * @param future The future.
      */
     public void notifyComplete( AsyncFuture<T> future) throws Exception;
   }
   
   @SuppressWarnings("unchecked")
   public AsyncFuture( T initiator)
   {
     this.result = this; // space-saving trick to indicate pending state
     this.initiator = initiator;
     this.listeners = new IListener[ 1];
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
     
     AwaitListener<T> listener = new AwaitListener<T>();
     addListener( listener);
     return listener.await( timeout);
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
     synchronized( this) { return result == null;}
   }
   
   /**
    * @return Returns true if the operation was not successful.
    */
   public boolean isFailure()
   {
     synchronized( this) { return result != null && result != this;}
   }
   
   /**
    * @return Returns null or the failure message.
    */
   public String getFailureMessage()
   {
     return (result instanceof String)? (String)result: null;
   }
   
   /**
    * @return Returns null or the throwable that caused the failure.
    */
   public Throwable getFailureCause()
   {
     return (result instanceof Throwable)? (Throwable)result: null;
   }
   
   /**
    * @return Returns true if the operation is complete.
    */
   public boolean isDone()
   {
     synchronized( this) { return result != this;}
   }
   
   /**
    * Add a listener to this future. The listener will be notified immediately
    * if the future has already completed.
    * @param listener The listener.
    */
   public void addListener( IListener<T> listener)
   {
     log.debugf( "addListener( %x, %x)", hashCode(), listener.hashCode());
     
     Object result;
     synchronized( this)
     {
       result = this.result;
       if ( result == this)
       {
         int slot = getListenerSlot();
         listeners[ slot] = listener;
       }
     }
     
     try
     {
       if ( result != this) notifyComplete( listener);
     }
     catch( Exception e)
     {
       log.error( "Future notification failed because: ");
       log.exception( e);
     }
   }
   
   /**
    * @return Returns the index of a null listener element.
    */
   @SuppressWarnings("unchecked")
   private int getListenerSlot()
   {
     if ( listeners == null)
     {
       listeners = new IListener[ 1];
       return 0;
     }
     
     for( int i=0; i<listeners.length; i++)
     {
       if ( listeners[ i] == null)
         return i;
     }
     
     IListener<T>[] array = new IListener[ listeners.length + 1];
     System.arraycopy( listeners, 0, array, 0, listeners.length);
     listeners = array;
     
     return listeners.length - 1;
   }
   
   /**
    * Remove a listener from this future.
    * @param listener The listener.
    */
   public void removeListener( IListener<T> listener)
   {
     log.debugf( "removeListener( %x, %x)", hashCode(), listener.hashCode());
     synchronized( this)
     {
       for( int i=0; i<listeners.length; i++)
       {
         if ( listeners[ i] == listener)
         {
           listeners[ i] = null;
           break;
         }
       }
     }
   }
   
   /**
    * Remove all listeners from the future.
    */
   @SuppressWarnings("unchecked")
   public void removeListeners()
   {
     log.debugf( "removeListeners( %x)", hashCode());
     synchronized( this)
     {
       listeners = new IListener[ 0];
     }
   }
 
   /**
    * Notify listeners that the future is complete.
    */
   public void notifySuccess()
   {
     log.debugf( "notifySuccess( %x)", hashCode());
     
     IListener<T>[] listeners = null;
     synchronized( this)
     {
       result = null;
       listeners = this.listeners;
       this.listeners = null;
     }
     
    if ( listeners != null)
    {
      for( int i=0; i<listeners.length; i++)
        notifyComplete( listeners[ i]);
    }
   }
   
   /**
    * Notify listeners that the future is complete.
    * @param result A String or Throwable.
    */
   public void notifyFailure( Object result)
   {
     if ( result == null) throw new IllegalArgumentException();
     
     log.debugf( "notifyFailure( %x, %s)", hashCode(), result.toString());
 
     IListener<T>[] listeners = null;
     synchronized( this)
     {
       this.result = result;
       listeners = this.listeners;
       this.listeners = null;
     }
     
    if ( listeners != null)
    {
      for( int i=0; i<listeners.length; i++)
        notifyComplete( listeners[ i]);
    }
   }
   
   /**
    * Notify a listener that the future is complete.
    * @param listener The listener.
    */
   public void notifyComplete( IListener<T> listener)
   {
     try
     {
       listener.notifyComplete( this);
     }
     catch( Exception e)
     {
       log.exception( e);
     }
   }
   
   private static class AwaitListener<T> implements IListener<T>
   {
     public AwaitListener()
     {
       this.latch = new Semaphore( 0);
     }
     
     /**
      * Wait for notification.
      * @param timeout The timeout in milliseconds
      * @return Returns false if the timeout expires.
      */
     public boolean await( int timeout)
     {
       try
       {
         return latch.tryAcquire( timeout, TimeUnit.MILLISECONDS);
       }
       catch( InterruptedException e)
       {
         log.debug( e.toString());
         return false;
       }
     }
     
     /* (non-Javadoc)
      * @see org.xmodel.future.AsyncFuture.IListener#notifyComplete(org.xmodel.future.AsyncFuture)
      */
     @Override
     public void notifyComplete( AsyncFuture<T> future) throws Exception
     {
       latch.release();
     }
     
     private Semaphore latch;
   }
 
   private final static Log log = Log.getLog( AsyncFuture.class);
   
   private T initiator;
   private Object result;
   private IListener<T>[] listeners;
 }
