 package multimodal;
 
 import java.util.Date;
 import java.util.LinkedList;
 
 import multimodal.schedule.Room;
 
 public class Constraint {
 	private LinkedList<Property> properties;
 	private Date startTime;
 	private Date endTime;
 	private int minimalDurationSec;
 
 	public Constraint(){
 		this.properties = new LinkedList<Property>();
 		//huge time window; No time restriction
 		this.startTime = new Date(0); //far in the past
 		this.endTime = new Date(Long.MAX_VALUE); //far in the future
 		this.minimalDurationSec = 1; 
 	}
 	
 	public void constrain(Property mustHave){
 		this.properties.add(mustHave);
 	}
 	
 	public void constrainStart(Date startTime){
 		this.startTime = startTime;
 	}
 	
 	public void constrainStartNowPlusSec(long inSeconds){
 		constrainStart(new Date(System.currentTimeMillis()+inSeconds*1000));
 	}
 	
 	public void constrainEndNowPlusSec(long inSeconds){
 		this.constrainEnd(new Date(System.currentTimeMillis()+inSeconds*1000));
 	}
 	
 	public void constrainEnd(Date endTime){
 		if(startTime.getTime()+this.minimalDurationSec*1000l>endTime.getTime()){
 			throw new IllegalArgumentException("end time cannot be shorter than starttime + duration!");
 		}
 		this.endTime = endTime;
 	}
 	public void constrainDuration(int minimalDurationSec){
 		if(minimalDurationSec<0){
 			throw new IllegalArgumentException("Can't create a negative time span!");
 		}
 		if((endTime.getTime()-startTime.getTime())/1000<minimalDurationSec){
 			throw new IllegalArgumentException("duration is longer than the specified end and start time!");
 		}
 		this.minimalDurationSec = minimalDurationSec;
 	}
 	
 	public boolean fulfillsTimeConstraint(Date d){
 		return this.startTime.before(d) && this.endTime.after(d);
 	}
 	public boolean fulfillsPropertyConstraints(Room r){
 		return this.properties.containsAll(r.getProperties());
 	}
 
 	public Date getStartTime() {
 		return (Date) this.startTime.clone();
 	}
 	
 	public Date getEndTime() {
 		return (Date) endTime.clone();
 	}
 
 	public long getDuration() {
 		return this.minimalDurationSec;
 	}
 
 	public Constraint fuzzyTimeConstrain(FuzzyTime ft) {
 		this.startTime = ft.startTime;
 		this.endTime = ft.endTime;
 		return this;
 	}
 }
