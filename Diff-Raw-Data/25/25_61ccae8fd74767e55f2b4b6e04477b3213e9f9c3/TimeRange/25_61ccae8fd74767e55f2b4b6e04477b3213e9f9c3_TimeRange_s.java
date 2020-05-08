 package psy.util;
 
 import java.util.Date;
 
 /**
  * A range of time represented by two instances of {@link java.util.Date} 
  */
 public class TimeRange{
     private Date start, end;
     
     /**
      * Creates a TimeRange that has a start but no defined end
      * @param s The start Date
      */
     public TimeRange(Date s){
         this(s, null);
     }
     
     /**
      * Creates a fully defined TimeRange
      * Swaps the parameters if the start is after the end
      * @param s The start Date
      * @param e The end Date
      */
     public TimeRange(Date s, Date e){
     	if(s!=null && e!=null && e.after(s)){
     		start = s;
     		end = e;
     	}else{
     		end = s;
     		start = e;
     	}
     }
     
     /**
      * Creates a TimeRange with a given length and start Date
      * @param s The start date
      * @param length The length of the time range in milliseconds
      */
     public TimeRange(Date s, long length){
     	start = s;
     	long temp = start.getTime()+length;
     	end = new Date(temp);
     }
     
     /**
      * Creates a TimeRange with a given length and end Date
      * @param length The length of the time range in milliseconds
      * @param s The end date
      */
     public TimeRange(long length, Date e){
     	end = e;
     	long temp = end.getTime()-length;
     	start = new Date(temp);
     }
 
     /**
      * Creates a TimeRange from a {@link java.lang.String}. The inverse of toString()
      * @param str
      */
     public TimeRange(String str){
         String[] times = str.split("-");
         start = new Date(new Integer(times[0].trim()));
         end = new Date(new Integer(times[1].trim()));
     }
     
     /**
      * Accesses the start Date 
      * @return 
      */
     public Date getStart(){
         return start;
     }
     
     /**
      * Accesses the end Date
      * @return
      */
     public Date getEnd(){
         return end;
     }
     
     /**
      * Mutates the start Date
      * @param s
      */
     public void setStart(Date s){
         start = s;
     }
     
     /**
      * Mutates the end Date
      * @param e
      */
     public void setEnd(Date e){
         end = e;
     }
     
     /**
      * Tests if a specified Date is included in this TimeRange
      * @param date The date to compare
      * @return True iff the specified Date is after the start Date and before the end Date, if specified
      */
     public boolean includes(Date date){
         if(start==null)
             return date.before(end);
         else if(end==null)
             return date.after(start);
         else
             return (date.before(end) && date.after(start));
     }
     
     /**
      * Returns the number of milliseconds between the start and end Dates
      * @return
      */
     public long length(){
     	if(this.start==null || this.end==null)
     		return (long)Double.POSITIVE_INFINITY;
     	else
     		return (end.getTime() - start.getTime());
     }
     
     /**
      * Returns the number of seconds between the start and end Dates
      * @return
      */
     public double lengthInSeconds(){
         return (length()/1000);
     }
     
     /**
      * Returns the number of minutes between the start and end Dates
      * @return
      */
     public double lengthInMinutes(){
         return (lengthInSeconds()/60);
     }
     
     /**
      * Returns the number of hours between the start and end Dates
      * @return
      */
     public double lengthInHours(){
         return (lengthInMinutes()/60);
     }
     
     /**
      * Returns the number of days between the start and end Dates
      * @return
      */
     public double lengthInDays(){
         return (lengthInHours()/24);
     }
     
     /**
      * Returns the number of years between the start and end Dates
      * @return
      */
     public double lengthInYears(){
         return (lengthInDays()/365);
     }
     
     public String toString(){
         String out = "";
         if(start!=null)
             out+=start.getTime();
         out+=" - ";
         if(end!=null)
             out+=end.getTime();
         return out;
     }
     
     /**
     * Calculates the number of milliseconds in this TimeRange are also in a supplied second TimeRange
      * @param other The TimeRange to compare
      * @return
      */
    public long overlap(TimeRange other){
     	//If the other TimeRange is completely before this one, return 0
         if(other.getEnd()!=null && this.start!=null && other.getEnd().before(this.start))
         	return 0;
         //If the other TimeRange is completely after this one, return 0
         else if(other.getStart()!=null && this.end!=null && other.getStart().after(this.end))
         	return 0;
         //If the TimeRanges do overlap
         else{
         	Date overlapStart, overlapEnd;
         	//... find the start of the overlap
        	if(other.getStart()==null || other.getStart().before(this.start)){
         		overlapStart = this.start;
         	}else{
         		overlapStart = other.getStart();
         	}
         	//... and the end of the overlap
        	if(other.getEnd()==null || other.getEnd().before(this.end)){
        		overlapEnd = other.getEnd();
         	}else{
         		overlapEnd = this.end;
         	}
         	//If either end of the overlap is unbounded, return infinity
        	if(this.start==null || this.end==null)
        		return (long)Double.POSITIVE_INFINITY;
         	//Otherwise subtract the bounds
         	else
        		return (overlapEnd.getTime() - overlapStart.getTime());
         }
     }
     
     /**
      * Calculates how long ago the start Date is
      * @return Positive iff {@link start} is in the past
      */
     public long timeSinceStart(){
         Date now = new Date();
         return (now.getTime() - start.getTime());
     }
     
     /**
      * Calculates the amount of time until the end Date
      * @return Positive iff {@link end} is in the future
      */
     public long timeUntilEnd(){
         Date now = new Date();
         return (end.getTime() - now.getTime());
     }
     
     public double secondsUntilEnd(){
         return (timeUntilEnd()/1000);
     }
     
     public double minutesUntilEnd(){
         return (secondsUntilEnd()/60);
     }
     
     public double hoursUntilEnd(){
         return (minutesUntilEnd()/60);
     }
     
     public double daysUntilEnd(){
         return (hoursUntilEnd()/24);
     }
     
     public double yearsUntilEnd(){
         return (daysUntilEnd()/365);
     }
 }
