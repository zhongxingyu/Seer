 import com.amazonaws.services.sqs.model.*;
 
 import java.io.IOException;
 
 public class VisibilityTimeoutRunnable {
     public static final int MILLISECONDS_IN_MINUTE = 1000 * 60;
     private ClientWorker worker;
     private boolean done;
     private Message message;
     private Thread timer;
 
     public void updateVisibilityTimeout() {
 	worker.parameters.sqs.changeMessageVisibility( new ChangeMessageVisibilityRequest( worker.parameters.param( Parameters.QUEUE_URL_ID ), 
 											   message.getReceiptHandle(),
 											   worker.parameters.visibility ) );
     }
 
     public VisibilityTimeoutRunnable( final ClientWorker worker, Message message ) {
 	this.worker = worker;
 	this.message = message;
 	timer = 
 	    new Thread( new Runnable() {
 		    public void run() {
 			boolean shouldRun = true;
			while( shouldRun && !Thread.currentThread().interrupted() ) {
 			    try {
 				Thread.sleep( worker.parameters.visibility * 1000 - MILLISECONDS_IN_MINUTE );
 				updateVisibilityTimeout();
 			    } catch ( InterruptedException e ) {
 				shouldRun = false;
 			    }
 			}
 		    }
 		} );
     }
     
     public void run() throws IOException {
 	timer.start();
 	worker.processFile( message.getBody() );
 	worker.doneWithFile( message );
 	timer.interrupt();
 	try {
 	    timer.join();
 	} catch ( InterruptedException e ) {}
     }
 }
