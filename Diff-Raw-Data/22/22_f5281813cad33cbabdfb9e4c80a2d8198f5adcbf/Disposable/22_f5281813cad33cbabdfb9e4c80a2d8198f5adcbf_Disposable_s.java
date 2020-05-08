 package de.skuzzle.polly.sdk;
 
 import de.skuzzle.polly.sdk.exceptions.DisposingException;
 
 /**
  * <p>Disposable objects are used to release resources upon unloading of your plugin. If
  * you have classes in your plugin which need to free resources if you plugin unloads,
  * make them implement this class and add them to your 'MyPlugin' class using 
  * {@link PollyPlugin#addDisposable(Disposable)}.</p>
  * 
  * <p>This interface is implemented in a safe-way in {@link AbstractDisposable}. 
  * If implementing this yourself, take care of thread-safety and thrown exceptions. This
  * is a reusable sample implementation:</p>
  * <pre>
  *    private boolean disposed;
  *    
 *    @Override
  *    public synchronized boolean isDisposed() {
  *        return this.disposed;
  *    }
  *    
 *    @Override
  *    public synchronized void dispose() throws DisposingException {
  *        if (this.isDisposed() {
  *            throw new IllegalStateException();
  *        }
  *        try {
  *            // your disposing code
  *        } catch (Exception e) {
  *            throw new DisposingException(e);
  *        }
  * </pre>
  * 
  * @author Simon
  * @since zero day
  * @version 27.07.2011 5e9480b
  */
 public interface Disposable {
     
     
     /**
      * Determines whether this object already has been disposed.
      * @return If this object is disposed.
      */
     public abstract boolean isDisposed();
     
     
     
     /**
      * Call this method to free all resources of the implementing class.
      * @throws DisposingException If an exception occurs during disposing.
      * @throws IllegalStateException if this object already is disposed.
      */
     public abstract void dispose() throws DisposingException;
 }
