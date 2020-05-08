 public abstract class AbstractTask implements Cloneable {
     private String description;
     private String venue;
     private Type type;
     private Status status;
 
     public static enum Type {
 	TIMED, FLOATING, DEADLINE;
     }
 
     public static enum Status {
 	UNDONE, DONE, IMPOSSIBLE;
     }
 
     public AbstractTask(String desc, Type t) {
 	description = desc;
 	type = t;
 	venue = "";
 	status = Status.UNDONE;
     }
 
     public AbstractTask(String desc, String v, Type t) {
 	description = desc;
 	type = t;
 	venue = v;
 	status = Status.UNDONE;
     }
 
     public Object clone() {
 	Object o = null;
 	try {
 	    o = super.clone();
 	} catch (CloneNotSupportedException e) {
 	    System.out.println("Cloning Task Failed");
 	    assert false;
 	}
 	return o;
     }
 
     public boolean equals(Object o) {
 	AbstractTask otherTask = (AbstractTask) o;
 
	return otherTask.description.equals(this.description)
		&& otherTask.type.equals(this.type)
		&& otherTask.venue.equals(this.venue)
		&& otherTask.status == this.status;
     }
 
     public int hashCode() {
 	int hashCode = description.hashCode() + type.hashCode()
 		+ venue.hashCode() + status.hashCode();
 	return hashCode;
     }
 
     public String getDescription() {
 	return description;
     }
 
     public void setDescription(String desc) {
 	description = desc;
     }
 
     public String getVenue() {
 	return venue;
     }
 
     public void setVenue(String v) {
 	venue = v;
     }
 
     public Type getType() {
 	return type;
     }
 
     public void setStatus(Status s) {
 	status = s;
     }
 
     public Status getStatus() {
 	return status;
     }
 }
