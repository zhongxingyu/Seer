 import java.util.HashMap;
 import java.util.Map;
 import akka.actor.ActorRef;
 import akka.actor.UntypedActor;
 
 /**
  * This actor handles the results from the BodyScan and the BagScan to determine
  * if a passenger is safe to let go or must be send to jail. The SecurityPoint
  * is the last station in the airport security line.
  * 
  * @author Anthony Barone
  * @author Ryan Clough
  * @author Rebecca Dudley
  * @author Ryan Mentley
  */
 public class SecurityStation extends UntypedActor {
 
     // Instance variables
     private final int lineNumber;
     private final ActorRef jail;
     private final int numScannersPerLine = 2;
     private Map<Passenger, Report> pendingReports = new HashMap<Passenger, Report>();
     private String INDENT = "      ";
     private int numCloseMsgsReceived;
 
     /**
      * Constructor for the SecurityStation
      * 
      * @param lineNumber
      */
     public SecurityStation( int lineNumber, ActorRef jail ) {
         this.lineNumber = lineNumber;
         this.jail = jail;
     }
 
     @Override
     public void onReceive( final Object msg ) throws Exception {
         // TODO printouts
 
         if ( msg instanceof Report ) {
             // If msg is a Report, check to see if we have received a report for
             // this passenger already. If so, make a decision whether to admit
             // through or not. Else, store the passenger & report in our reports
             // map for later.
             Report report = (Report) msg;
             if ( !pendingReports.containsKey( report.getPassenger() ) ) {
                 pendingReports.put(report.getPassenger(), report);
             } else {
                ScanResult sr = pendingReports.get( report.getPassenger() ).getResult();
                System.out.println(INDENT + "Security " + lineNumber + ": Passenger " + 
            			((Passenger) msg).getName() + " report recieved (" + 
            			(sr.value()? "pass" : "fail") + ")");
                if ( sr.value() && report.getResult().value() ) {
                     // Passenger passes, leaves system.
                    pendingReports.remove( report.getPassenger() );
                 } else {
                     // Passenger has failed one or more scans, send to Jail.
                 	System.out.println(INDENT + "Security " + lineNumber + ": Passenger " + 
                			((Passenger) msg).getName() + " (fail/pass) sent to jail");
                     jail.tell( report.getPassenger() );
                 }
             }
         } else if ( msg instanceof CloseMsg ) {
             // If msg is a CloseMsg, check to see if we have received CloseMsgs from
             // both scanners. If so, relay msg to Jail and terminate self.
             numCloseMsgsReceived++;
             System.out.println(INDENT + "Security " + lineNumber + "Close recieved (" +
             		numCloseMsgsReceived + " of " + numScannersPerLine + ")");
             if ( numCloseMsgsReceived >= numScannersPerLine ) {
                 jail.tell(msg);
                 System.out.println(INDENT + "Security " + lineNumber + 
                 		" Close sent to jail");
                 this.getContext().stop();
                 System.out.println(INDENT + "Security " + lineNumber + "Closed");
             }
         }
     }
 
 }
