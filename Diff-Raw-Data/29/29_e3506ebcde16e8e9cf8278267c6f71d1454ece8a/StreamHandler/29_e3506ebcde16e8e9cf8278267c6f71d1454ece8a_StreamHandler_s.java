 package de.skuzzle.polly.process;
 
 import java.io.InputStream;
 
 
 /**
  * StreamHandlers allow the parallel consumption of incoming data from a process's
  * standard output or standard error output. As by the documentation of the 
  * {@link Process} class its essential to empty those streams because some native
  * implementations only provide limited buffersizes for these stream. Not clearing them
  * could then lead into a deadlock.
  * 
  * When creating a process using the {@link ProcessExecutor}, you can set an input and
  * an error stream handler using {@link ProcessExecutor#setInputHandler(StreamHandler)}
  * and {@link ProcessExecutor#setErrorHandler(StreamHandler)}.
  * 
  * The StreamHandler starts a new thread which handles the parallel consumption.
  * 
  * @author Simon
  */
 public abstract class StreamHandler extends Thread {
     
     /**
      * This state indicates that the stream was handled successfuly.
      */
     public final static int STATE_SUCCESS = 0;
     
     /**
      * This state indicates that an error occured during stream handling.
      */
     public final static int STATE_ERROR = -1;
     
     /**
      * This state indicates that the StreamHandler is currently running.
      */
     public final static int STATE_ACTIVE = 2;
     
     /**
      * Indicates that the handler has not been run yet.
      */
     public final static int STATE_OFF = 3;
     
     
     /**
      * This field is used for the creation of default names for the handlers.
      */
     private static int num;
     
     
 
     private InputStream stream;
     private int state;
     private Exception errorState;
     private boolean shutdownFlag;
     
     
     
     /**
      * Creates a new StreamHandler with a certain name. To attach an InputStream, use the
      * {@link #setStream(InputStream)} method.
      * 
      * @param name The name of the thread used for stream handling.
      */
     public StreamHandler(String name) {
         this(null, name);
     }
     
     
     
     /**
      * Creates a new StreamHandler for the given stream using a default name.
      * 
      * @param stream The stream to handle. Note: When using a StreamHandler with a 
      *      {@link ProcessExecutor}, this InputStream will be overridden with the 
      *      InputStream of the created process.
      */
     public StreamHandler(InputStream stream) {
         this(stream, "STREAM_HANDLER_" + (++num));
     }
     
     
     
     /**
      * Creates a new StreamHandler for the given stream and with given thread name.
      * 
      * @param stream The stream to handle. Note: When using a StreamHandler with a 
      *      {@link ProcessExecutor}, this InputStream will be overridden with the 
      *      InputStream of the created process.
      * @param name The name of the thread used for stream handling.
      */
     public StreamHandler(InputStream stream, String name) {
         super(name);
         this.stream = stream;
         this.state = STATE_OFF;
     }
     
     
     
     /**
      * Gets the current state of this handler. State may be one of these constants:
      * {@link #STATE_OFF}, {@link #STATE_ACTIVE},  {@link #STATE_OFF} or 
      * {@link #STATE_ERROR}.
      *  
     * When state is STATE_ERROR, the excpetion which caused this error can be retrieved
      * using {@link #getErrorState()}.
      * 
      * @return The current handler state.
      */
     public int getStreamState() {
         return this.state;
     }
     
     
     
     /**
      * If an exception occured during stream handling, this exception can be retrieved
      * with this method.
      * 
      * @return The exception that caused this StreamHandler to fail or <code>null</code>
      *      if <code>getStreamState() != STATE_ERROR</code>.
      */
     public Exception getErrorState() {
         return this.errorState;
     }
     
     
     
     /**
      * Sets the {@link InputStream} for this handler. You can only set the stream before
      * calling {@link #start()}.
      * 
      * @param stream The stream to handle.
      * @throws IllegalStateException If <code>getState() != STATE_OFF</code>.
      */
     public void setStream(InputStream stream) {
         if (this.state != STATE_OFF) {
             throw new IllegalStateException();
         }
         this.stream = stream;
     }
     
     
     
     /**
      * This method must be implemented by subclasses to handle incoming data.
      * 
      * @param stream The stream to handle.
      * @throws Exception Can be thrown by subclasses if they encounter any error. This
      *      StreamHandler will then stop executing and change its state to 
      *      {@link #STATE_ERROR}. The error can then be retrieved using 
      *      {@link #getErrorState()}
      */
     protected abstract void handle(InputStream stream) throws Exception;
 
     
     
     void processGone() {
         this.shutdownFlag = true;
         this.interrupt();
     }
     
     
     
     /**
      * Runs this StreamHandler. First it changes the current state to 
      * {@link #STATE_ACTIVE}, then calls the {@link #handle(InputStream)} method.
      * When returned, the state is changed to {@link #STATE_SUCCESS} or on exception to
      * {@link #STATE_ERROR}.
      */
     @Override
     public void run() {
         this.state = STATE_ACTIVE;
         try {
             this.handle(this.stream);
             this.state = STATE_SUCCESS;
         } catch (InterruptedException e) {
             if (this.shutdownFlag) {
                 this.state = STATE_SUCCESS;
             } else {
                 this.state = STATE_ERROR;
                 this.errorState = e;
             }
         } catch (Exception e) {
             this.state = STATE_ERROR;
             this.errorState = e;
         }
     }
 }
 
