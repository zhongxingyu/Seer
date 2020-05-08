 package org.hackystat.dailyprojectdata.resource.snapshot;
 
 import java.io.Serializable;
 import java.util.Comparator;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataRef;
 import org.hackystat.utilities.tstamp.Tstamp;
 
 /**
  * Comparator that allows <code>SensorDataRef</code>s to be sorted by their timestamp.
  * 
  * @author jsakuda
  * 
  */
 public class SensorDataRefComparator implements Comparator<SensorDataRef>, Serializable {
   /** Generated serial id. */
   private static final long serialVersionUID = -1334273923611860686L;
 
   /** The order of the sorting. */
   private boolean ascending = true;
 
   /**
    * Creates a new comparator.
    * 
    * @param ascending True if the <code>SensorDataRef</code>s are being sorted in ascending
    *          order, otherwise false for descending.
    */
   public SensorDataRefComparator(boolean ascending) {
     this.ascending = ascending;
   }
 
   /**
    * Compares two SensorDataRefs.
    * 
    * @param o1 The first ref.
    * @param o2 The second ref.
    * @return Returns -1, 0, or 1 if the first argument is less than, equal to, or greater than
    *         the second.
    */
  @Override
   public int compare(SensorDataRef o1, SensorDataRef o2) {
     int result = this.compare(o1.getTimestamp(), o2.getTimestamp());
     
     if (!ascending) {
       // descending sort order, change result to be in reverse order
       result = result * -1;
     }
     
     return result;
   }
 
   /**
    * Compares two timestamps.
    * 
    * @param cal1 The first timestamp.
    * @param cal2 The second timestamp.
    * @return Returns -1 if cal1 < cal2, 1 if cal1 > cal2, otherwise returns 0.
    */
   private int compare(XMLGregorianCalendar cal1, XMLGregorianCalendar cal2) {
     if (Tstamp.lessThan(cal1, cal2)) {
       return -1;
     }
     else if (Tstamp.greaterThan(cal1, cal2)) {
       return 1;
     }
     return 0;
   }
 
 }
