 package de.zib.gndms.model.gorfx;
 
 import javax.persistence.Embeddable;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 import java.util.Calendar;
 import java.util.Calendar;
 
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 import org.joda.time.format.ISODateTimeFormat;
 import org.apache.openjpa.persistence.Persistent;
 import org.apache.openjpa.persistence.Externalizer;
 import org.apache.openjpa.persistence.Factory;
 
 
 /**
  * @author: Maik Jorra <jorra@zib.de>
  * @version: $Id$
  * <p/>
  * User: mjorra, Date: 03.11.2008, Time: 16:56:10
  */
 
 @Embeddable
 public class Contract {
     // the comments denote the mapping to the
     // XSD OfferExecutionContract type
 
     // can be mapped to IfDesisionBefore
    @Temporal(value = TemporalType.TIMESTAMP)
     private Calendar accepted;
 
     // can be mapped to ExecutionLiklyUntil
    @Temporal(value = TemporalType.TIMESTAMP)
     private Calendar deadline;
 
     // can be mapped to ResultValidUntil
     // this must be at least equal to the deadline
    @Temporal(value = TemporalType.TIMESTAMP)
     private Calendar resultValidity;
 
     // can be mapped to constantExecutionTime
     transient boolean deadlineIsOffset = false;
 
 
 
     public Contract() {}
 
     public Contract(Contract org) {
         if (org != null) {
            accepted = (Calendar) org.accepted.clone();
            deadline = (Calendar) org.deadline.clone();
            resultValidity = (Calendar) org.resultValidity.clone();
             deadlineIsOffset = org.deadlineIsOffset;
         }
     }
 
 
     @Transient
     public Calendar getCurrentDeadline() {
         return  ((deadlineIsOffset) ?
            new DateTime(deadline).plus(new Duration(new DateTime(0L), new DateTime(deadline))).toGregorianCalendar() : deadline);
     }
 
 
     @Transient
     public Calendar getCurrentTerminationTime() {
         Calendar deadline = getCurrentDeadline();
        return (deadline.compareTo(resultValidity) <= 0) ? resultValidity : deadline;
 
     }
 
 
     public Calendar getAccepted() {
         return accepted;
     }
 
 
     public void setAccepted( Calendar accepted ) {
         this.accepted = accepted;
     }
 
 
     public Calendar getDeadline() {
         return deadline;
     }
 
 
     public void setDeadline( Calendar dl ) {
         deadline = dl;
        if( resultValidity == null )
            resultValidity = dl;
     }
 
 
 
     public Calendar getResultValidity() {
         return resultValidity;
     }
 
 
     public void setResultValidity( Calendar resultValidity ) {
         this.resultValidity = resultValidity;
     }
 
 
     public boolean isDeadlineIsOffset() {
         return deadlineIsOffset;
     }
 
 
     public void setDeadlineIsOffset( boolean deadlineIsOffset ) {
         this.deadlineIsOffset = deadlineIsOffset;
     }
 
 
     public static String dateToString( Calendar c ) {
         DateTime dt = new DateTime( c );
         return ISODateTimeFormat.dateTime( ).print( dt  );
     }
 
     
     public static Calendar dateToString( String s ) {
 
         return new DateTime( s ).toGregorianCalendar();
     }
 }
