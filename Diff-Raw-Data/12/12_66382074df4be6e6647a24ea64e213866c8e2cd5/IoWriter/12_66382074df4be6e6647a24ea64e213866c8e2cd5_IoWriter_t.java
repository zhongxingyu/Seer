 package org.runetekk;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * IoWriter.java
  * @version 1.0.0
  * @author RuneTekk Development (SiniSoul)
  */
 public final class IoWriter implements Runnable {
     
     /**
      * The {@link Logger} for this class.
      */
     private static final Logger LOGGER = Logger.getLogger(IoWriter.class.getName());
     
     /**
      * The amount of time that one cycle will take.
      */
    private static final long CYCLE_TIME = 6000000L;
     
     /**
      * The main handler.
      */
     Main main;
     
     /**
      * The local thread.
      */
     private Thread thread;
     
     /**
      * The local thread is currently paused.
      */
     private boolean isPaused;
     
     /**
      * Initializes the local thread.
      */
     private void initialize() {
         thread = new Thread(this);
         thread.setDaemon(true);
         thread.start();
     }
     
     @Override
     public void run() {
         for(;;) {
             long startTime = System.nanoTime();
             synchronized(this) {
                 if(isPaused)
                     break;
                 ListNode node = main.activeList;
                 while((node = node.childNode) != null) { 
                     if(!(node instanceof IntegerNode))
                         break;
                     IntegerNode position = (IntegerNode) node;
                     Client client = main.clientArray[position.value];
                     if(client == null) {
                         LOGGER.log(Level.WARNING, "Null client id, removed from active list!");
                         main.removeClient(position);
                         continue;
                     }
                     try {
                         if(client.oWritePosition > 0) {
                             client.outputStream.write(client.outgoingBuffer, 0, client.oWritePosition);
                             client.oWritePosition = 0;
                             client.oWritten = true;
                         }
                     } catch(IOException ex) {
                         LOGGER.log(Level.WARNING, "Exception thrown while writing : ", ex);
                         main.removeClient(position);
                         client.destroy();
                         continue;
                     }
                 }
             }
             try {
                long sleepTime = (CYCLE_TIME - (System.nanoTime() - startTime))/10000L;
                 if(sleepTime > 0)
                     thread.sleep(sleepTime); 
             } catch(Exception ex) {}
         }
     }
       
     /**
      * Constructs a new {@link IoWriter};
      * @param main The main handler for this {@link IoWriter}.
      */
     IoWriter(Main main) {
         this.main = main;
         initialize();
     }
 }
