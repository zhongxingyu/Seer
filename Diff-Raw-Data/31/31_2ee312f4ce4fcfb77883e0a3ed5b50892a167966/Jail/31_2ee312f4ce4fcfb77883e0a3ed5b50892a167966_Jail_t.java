 import java.util.ArrayList;
 import java.util.List;
 import akka.actor.UntypedActor;
 
 /**
  * All passengers that fail the security check are sent here.
  * 
  * @author Anthony Barone
  * @author Ryan Clough
  * @author Rebecca Dudley
  * @author Ryan Mentley
  */
 public class Jail extends UntypedActor {
 
     // Constants
     private static final String INDENT = "        ";
 
     // Instance variables
     private int numCloseMsgsReceived = 0;
     private List<Passenger> prisoners = new ArrayList<Passenger>();
 
     @Override
     public void onReceive( final Object msg ) throws Exception {
         // If msg is a Passenger (someone being sent to Jail), add to prisoner list.
         if ( msg instanceof Passenger ) {
            printMsg("Passenger " + ((Passenger) msg).getName() + " jailed" );
             prisoners.add( (Passenger)msg );
         }
 
         // If msg is a CloseMessage from a security station, increment our
         // count of close messages received and check to see if all have closed.
         if ( msg instanceof CloseMsg ) {
             numCloseMsgsReceived++;
            printMsg("Close received (" + numCloseMsgsReceived +
                    " of " + TestBedConstants.NUM_LINES + " lines)" );
             if ( numCloseMsgsReceived == TestBedConstants.NUM_LINES ) {
                 // All security stations have shut down, terminate Jail.
                printMsg("Incarcerated Passengers");
                for (Passenger p : prisoners) {
                    System.out.println(INDENT + "      Passenger " + p.getName());
                }
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
    private void printMsg(String msg) {
        System.out.println(INDENT + "Jail: " + msg);
    }
 }
