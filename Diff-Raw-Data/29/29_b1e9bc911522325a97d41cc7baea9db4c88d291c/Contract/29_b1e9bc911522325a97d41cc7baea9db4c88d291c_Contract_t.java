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
     private Calendar accepted;
 
     // can be mapped to ExecutionLiklyUntil
     private Calendar deadline;
 
     // can be mapped to ResultValidUntil
     // this must be at least equal to the deadline
     private Calendar resultValidity;
 
     // can be mapped to constantExecutionTime
     transient boolean deadlineIsOffset = false;
 
 
 
     public Contract() {}
 
     public Contract(Contract org) {
         if (org != null) {
            setAccepted( (Calendar) org.getAccepted().clone() );
            setDeadline( (Calendar) org.getDeadline().clone() );
            setResultValidity( (Calendar) org.getResultValidity().clone() );
             deadlineIsOffset = org.deadlineIsOffset;
         }
     }
 
 
     @Transient
     public Calendar getCurrentDeadline() {
         return  ((deadlineIsOffset) ?
            new DateTime(getDeadline()).plus(new Duration(new DateTime(0L), new DateTime(getDeadline()))).toGregorianCalendar() : getDeadline() );
     }
 
 
     @Transient
     public Calendar getCurrentTerminationTime() {
         Calendar deadline = getCurrentDeadline();
        return (deadline.compareTo(getResultValidity( )) <= 0) ? getResultValidity() : deadline;
 
     }
 
 
    @Temporal(value = TemporalType.TIMESTAMP)
     public Calendar getAccepted() {
         return accepted;
     }
 
 
     public void setAccepted( Calendar accepted ) {
         this.accepted = accepted;
     }
 
 
    @Temporal(value = TemporalType.TIMESTAMP)
     public Calendar getDeadline() {
         return deadline;
     }
 
 
     public void setDeadline( Calendar dl ) {
         deadline = dl;
        if( getResultValidity( ) == null )
            setResultValidity( dl );
     }
 
 
 
    @Temporal(value = TemporalType.TIMESTAMP)
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
