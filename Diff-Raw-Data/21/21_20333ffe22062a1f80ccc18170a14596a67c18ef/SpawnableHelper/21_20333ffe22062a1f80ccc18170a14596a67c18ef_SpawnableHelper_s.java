 package expectj;
 
 import java.io.OutputStream;
 import java.nio.channels.Channels;
 import java.nio.channels.Pipe;
 
 /**
  * Helper class that wraps Spawnables to make them crunchier for ExpectJ to run.
  * @author Johan Walles
  */
 class SpawnableHelper
 implements TimerEventListener 
 {
     /**
      * The spawnable we're wrapping.
      */
     private Spawnable spawnable;
     
     /**
      * @param timeOutSeconds time interval in seconds to be allowed for spawn execution
      * @param runMe the spawnable to execute
      */
     SpawnableHelper(Spawnable runMe, long timeOutSeconds) {
         if (timeOutSeconds < -1) {
             throw new IllegalArgumentException("Time-out is invalid");
         }
         if (timeOutSeconds != -1) {
             tm = new Timer(timeOutSeconds, this);
         }
         this.spawnable = runMe;
     }
     
     /**
      * @param runMe the spawnable to execute
      */
     SpawnableHelper(Spawnable runMe) {
         this(runMe, -1);
     }
     
     /** Timer object to monitor our Spawnable */
     private Timer tm = null;
 
     /**
      * Handle spawn's stdout.
      */
     private Pipe systemOut;
 
     /**
      * Handle spawn's stderr.
      */
     private Pipe systemErr;
 
    // StreamPiper objects to pipe the output of one stream to other
     private StreamPiper spawnOutToSystemOut = null;
     private StreamPiper spawnErrToSystemErr = null;
     
     /**
      * Time callback method
      * This method is invoked when the time-out occurr
      */
     public void timerTimedOut() {
         stop();
     }
 
     /**
      * This method stops the spawn.
      */
     void stop() {
         spawnOutToSystemOut.stopProcessing();
         if (spawnErrToSystemErr != null) {
             spawnErrToSystemErr.stopProcessing();
         }
         spawnable.stop();
     }
 
     /**
      * Timer callback method
      * This method is invoked by the Timer, when the timer thread
      * receives an interrupted exception.
      * @param reason The reason we were interrupted.
      */
     public void timerInterrupted(InterruptedException reason) {
         // Print the stack trace and ignore the problem, this will make us never time
         // out.  Too bad.  /JW-2006apr10
         reason.printStackTrace();
     }
 
     /**
     * This method is used to Stop all the piper object from copying the
     * content to standard out. This is used after interact command.
      */
     synchronized void stopPipingToStandardOut() {
         spawnOutToSystemOut.stopPipingToStandardOut();
         if (spawnErrToSystemErr != null) {
             spawnErrToSystemErr.stopPipingToStandardOut();
         }
     }
 
     synchronized void startPipingToStandardOut() {
         spawnOutToSystemOut.startPipingToStandardOut();          
         if (spawnErrToSystemErr != null) {
             spawnErrToSystemErr.startPipingToStandardOut();
         }
     }
 
     /**
      * This method launches our Spawnable within the specified time
      * limit.  It tells the spawnable to start, and starts the timer when
      * enabled. It starts the piped streams to enable copying of spawn
      * stream contents to standard streams.
      * @throws Exception if launching the spawnable fails
      */ 
     void start() throws Exception {
         // Start the spawnable and timer if needed
         spawnable.start();
         if (tm != null)
             tm.startTimer();
         
         // Starting the piped streams and StreamPiper objects
         systemOut = Pipe.open();
         systemOut.source().configureBlocking(false);
         spawnOutToSystemOut = new StreamPiper(System.out, 
                                               spawnable.getInputStream(),
                                               Channels.newOutputStream(systemOut.sink()));
         spawnOutToSystemOut.start();
         
         if (spawnable.getErrorStream() != null) {
             systemErr = Pipe.open();
             systemErr.source().configureBlocking(false);
             
             spawnErrToSystemErr = new StreamPiper(System.err, 
                                                   spawnable.getErrorStream(),
                                                   Channels.newOutputStream(systemErr.sink()));
             spawnErrToSystemErr.start();
         }
     }
 
     /**
      * @return a channel from which data produced by the spawn can be read
      */
     Pipe.SourceChannel getSourceChannel() {
         return systemOut.source();
     }
 
     /**
      * @return the output stream of the spawn.
      */
     OutputStream getOutputStream() {
         return spawnable.getOutputStream();
     }
 
     /**
      * @return a channel from which stderr data produced by the spawn can be read, or
      * null if there is no channel to stderr.
      */
     Pipe.SourceChannel getErrorSourceChannel() {
         if (systemErr == null) {
             return null;
         }
         return systemErr.source();
     }
 
     /**
      * @return true if the spawn has exited.
      */
     boolean isClosed() {
         return spawnable.isClosed();
     }
 
     /**
      * If the spawn representes by this object has already exited, it
      * returns the exit code. isClosed() should be used in conjunction
      * with this method.
      * @return The exit code from the exited spawn.
      * @throws ExpectJException If the spawn is still running.
      */
     int getExitValue() 
     throws ExpectJException
     {
         if (!isClosed()) {
             throw new ExpectJException("Spawn is still running");
         }
         return spawnable.getExitValue();
     }
 
 
     /**
      * @return the available contents of Standard Out
      */
     String getCurrentStandardOutContents() {
         return spawnOutToSystemOut.getCurrentContents();
     }
 
     /**
      * @return the available contents of Standard Err, or null if stderr is not available
      */
     String getCurrentStandardErrContents() {
         if (spawnErrToSystemErr == null) {
             return null;
         }
         return spawnErrToSystemErr.getCurrentContents();
     }
 
 }
