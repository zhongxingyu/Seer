 public class TimedTask extends AbstractTask {
     private String startDate, endDate;
 
     public TimedTask(String description, String startDate, String endDate) {
 	super(description, AbstractTask.Type.TIMED);
 	this.startDate = startDate;
 	this.endDate = endDate;
     }
 
     public TimedTask(String description, String startDate, String endDate,
 	    String venue) {
 	super(description, venue, AbstractTask.Type.TIMED);
 	this.startDate = startDate;
 	this.endDate = endDate;
     }
 
     public String toString() {
 	if (this.getVenue().equals(""))
 	    return this.getType() + ": " + this.getDescription() + " from "
 		    + startDate + " to " + endDate;
 	else
 	    return this.getType() + ": " + this.getDescription() + " at "
 		    + this.getVenue() + " from " + startDate + " to " + endDate;
     }
 
     public boolean equals(Object o) {
 	if(!o.getClass().equals(this.getClass())) return false;
 	
 	TimedTask otherTask = (TimedTask) o;
 
 	return (super.equals(o) 
 		&& (otherTask.startDate.equals(this.startDate)) 
 		&& (otherTask.endDate.equals(this.endDate)));
     }
 
     public int hashCode() {
 	int hashCode = super.hashCode() + startDate.hashCode()
 		+ endDate.hashCode();
 
 	return hashCode;
     }
 
     public String getStartDate() {
 	return startDate;
     }
 
    public void getStartDate(String sd) {
 	startDate = sd;
     }
 
     public String getEndDate() {
 	return endDate;
     }
 
     public void setEndDate(String ed) {
 	endDate = ed;
     }
 
 }
