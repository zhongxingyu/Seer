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
 
     // Instance variables
     private int numCloseMsgsReceived = 0;
     private String INDENT = "        ";
     private List<Passenger> prisoners = new ArrayList<Passenger>();
 
     @Override
     public void onReceive( final Object msg ) throws Exception {
         // If msg is a Passenger (someone being sent to Jail), add to prisoner list.
         if ( msg instanceof Passenger ) {
         	System.out.println(INDENT + "Jail: Passenger " + ((Passenger) msg).getName() +
         			" jailed" );
             prisoners.add( (Passenger)msg );
         }
 
         // If msg is a CloseMessage from a security station, increment our
         // count of close messages received and check to see if all have closed.
         if ( msg instanceof CloseMsg ) {
             numCloseMsgsReceived++;
             System.out.println(INDENT + "Jail: Close received (" + numCloseMsgsReceived +
            		" of " + TestBedConstants.NUM_LINES + " lines)" );
            if ( numCloseMsgsReceived == TestBedConstants.NUM_LINES ) {
                 // All security stations have shut down, terminate Jail.
             	System.out.println("Incarcerated Passengers");
             	for (Passenger p : prisoners){
             		System.out.println(INDENT + "      Passenger " + p.getName());
             	}
                 this.getContext().stop();
                 System.out.println("Jail: Closed");
             }
         }
     }
 
 }
