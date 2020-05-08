 package ch9k.network;
 
import ch9k.eventpool.DataEvent;
 import ch9k.eventpool.NetworkEvent;
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.TimeUnit;
 import org.apache.log4j.Logger;
 
 public class EventWriter implements Runnable, Closeable {
     /**
      * The ObjectOutputStream to write to;
      */
     private ObjectOutputStream out;
     
     /**
      * A logger, to log our efforts
      */
     private static final Logger logger = Logger.getLogger(EventWriter.class);
     
     /**
      * We all need a little help from time to time
      */
     private final ErrorHandler errorHandler;
     
     /**
      * Queue to take events from
      */
     private final BlockingQueue<NetworkEvent> queue;
     
     /**
      * Since i don't want to test for socket.isClosed() 
      * If true, the writer will stay alive
      */
     private boolean shouldContinue = true;
     
     public EventWriter(ObjectOutputStream stream, ErrorHandler handler,
             BlockingQueue<NetworkEvent> queue) {
         this.out = stream;
         this.errorHandler = handler;
         this.queue = queue;
     }
     
     /**
      * Close the ObjectOutputStream
      */
     public void close() throws IOException {
         shouldContinue = false;
         out.close();
     }
     
     /**
      * Send an event directly (bypassing the queue)
      * useful for testing if the outputstream is still alive
      */
     public synchronized void sendEvent(NetworkEvent event) throws IOException {
         logger.info(String.format("Writing event %s to %s",
                 event.getClass().getName(), event.getTarget()));
         out.writeObject(event);
         out.flush();

        /* This is supposed to take care of a memory leak in ObjectOutputStream
         * We don't do this for regural events since that would not be
         * efficient */
        if (event instanceof DataEvent) {
           out.reset();
        }

     }
     
     /**
      * Run until close is called, or we encounter an error
      * take events from the queue and send'em
      */
     public void run() {
         try {
             while(shouldContinue) {
                 try {
                     NetworkEvent event = queue.poll(200,TimeUnit.MILLISECONDS);
                     if(event == null) continue;
 
                     sendEvent(event);
                 } catch(InterruptedException e) {
                     logger.warn(e.toString());
                 }
             }
             
         } catch(IOException e) {
             logger.info(e.toString());
             errorHandler.writingFailed();
         }
     }
 }
