 import java.util.LinkedList;
 import java.util.List;
 import akka.actor.ActorRef;
 import akka.actor.UntypedActor;
 
 /**
  * This class represents the waiting queue before a passenger receives the OK to proceed
  * through the body scanner. A passenger's luggage is sent to the BagScanner
  * immediately when he/she reaches this point.
  * 
  * @author Anthony Barone
  * @author Ryan Clough
  * @author Rebecca Dudley
  * @author Ryan Mentley
  */
 public class ScanQueue extends UntypedActor {
 
     // Constants
     private static final String INDENT = "  ";
 
     // Instance variables
     private final int position;
     private final ActorRef bagScanner;
     private final ActorRef bodyScanner;
     private List<Passenger> passengersWaiting = new LinkedList<Passenger>();
     private boolean bodyScannerReady = true;
     private boolean closeMsgReceived = false;
 
     /**
      * Constructor for a ScanQueue as part of a Line.
      * 
      * @param position The line number as to which this ScanQueue belongs to
      * @param bagScanner The baggage scanner belonging to this ScanQueue
      * @param bodyScanner The body scanner belonging to this ScanQueue
      */
     public ScanQueue( int position, ActorRef bagScanner, ActorRef bodyScanner ) {
         this.position = position;
         this.bagScanner = bagScanner;
         this.bodyScanner = bodyScanner;
     }
 
     @Override
     public void onReceive( final Object msg ) throws Exception {
         if ( msg instanceof Passenger ) {
             Passenger p = (Passenger)msg;
             printMsg("Passenger " + p.getName() + " arrives in line");
             // If msg is a Passenger, immediately send their luggage off to BaggageScan 
             // and send them to the BodyScan if it's in a 'ready' state. Otherwise, add 
             // them to the FIFO wait queue to be notified when the BodyScan requests the 
             // next passenger to be scanned.
             printMsg("Passenger " + p.getName() + " baggage placed on scanner");
             bagScanner.tell( msg );
             if ( bodyScannerReady ) {
                 printMsg("Passenger " + p.getName() + " enters the body scanner");
                bodyScanner.tell( msg, getContext() );
                 bodyScannerReady = false;
             } else {
                 passengersWaiting.add( 0, p );
             }
         } else if ( msg instanceof NextMsg ) {
             // If msg is a NextMsg, the body scanner is marked ready if no passengers 
             // are waiting. Otherwise, the first passenger is sent into the scanner.
             if ( passengersWaiting.isEmpty() ) {
                 bodyScannerReady = true;
                 // Check if we are trying to shutdown, this would be the right time
                 if ( closeMsgReceived ) {
                     bodyScanner.tell( new CloseMsg() );
                     printMsg("Close sent to body scanner");
                     getContext().stop();
                     printMsg("Closed");
                 }
             } else {
                 Passenger p = passengersWaiting.remove( 0 );
                 bodyScanner.tell( p, getContext() );
             }
         } else if ( msg instanceof CloseMsg ) {
             // If msg is a CloseMsg, message is relayed to the baggage scanner 
             // immediately, and to the body scanner if it is in the ready state. 
             // If the body scanner is still processing a passenger, and/or if 
             // passengers are waiting in this queue, the CloseMsg must be deferred 
             // until all passengers have been through the body scanner.
             closeMsgReceived = true;
             bagScanner.tell( msg );
             if ( bodyScannerReady && passengersWaiting.isEmpty() ) {
                 printMsg("Close sent to body scanner");
                 bodyScanner.tell( msg );
                 getContext().stop();
                 printMsg("Closed");
             }
         }
     }
 
     /**
      * Prints a properly indented and labeled message
      * 
      * @param msg the message to print
      */
     private void printMsg (String msg) {
         System.out.println(INDENT + "Queue " + position + ": " + msg);
     }
 }
