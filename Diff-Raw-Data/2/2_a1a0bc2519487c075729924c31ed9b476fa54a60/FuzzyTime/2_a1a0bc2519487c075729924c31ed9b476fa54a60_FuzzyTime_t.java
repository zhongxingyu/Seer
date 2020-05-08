 package multimodal;
 
 import java.util.Date;
 
 public class FuzzyTime implements Cloneable{
 	private static final long DEFAULT_DEVIATION_SEC = 10*60;
 	long deviationSeconds;
 	Date startTime;
 	Date endTime;
 	long durationSeconds;
 
 	public FuzzyTime(Date startTime){
 		this.startTime = startTime;
 		this.deviationSeconds = FuzzyTime.DEFAULT_DEVIATION_SEC;
 	}
 	
 	public FuzzyTime(Date startTime, long deviationSeconds){
 		this(startTime);
 		this.deviationSeconds = deviationSeconds;
 	}
 	
 	public FuzzyTime(Date startTime, Date endTime){
 		this(startTime);
 		this.endTime = endTime;
 	}
 	
 	public FuzzyTime(Date startTime, Date endTime, long deviation){
 		this(startTime, endTime);
 		this.deviationSeconds = deviation;
 	}
 	
 	public FuzzyTime setDuration(long durationSeconds){
 		this.endTime = new Date(this.startTime.getTime()+durationSeconds*1000);
 		return this;
 	}
 
 	public static FuzzyTime now() {
 		return new FuzzyTime(new Date());
 	}
 
 	public static FuzzyTime nowPlusSeconds(long seconds) {
 		Date now = new Date();
 		return new FuzzyTime(new Date(now.getTime()+seconds*1000));
 	}
 	public static FuzzyTime todayPlusSeconds(long seconds) {
 		Date now = new Date();
 		Date today = new Date(now.getYear(),now.getMonth(),now.getDate());
 		return new FuzzyTime(new Date(today.getTime()+seconds*1000));
 	}
 	@Override
 	protected Object clone() throws CloneNotSupportedException {
 		return new FuzzyTime((Date)this.startTime.clone(), (Date)this.endTime.clone(), this.deviationSeconds);
 	}
 
 	public Date getCenter() {
 		return new Date((this.endTime.getTime()+this.startTime.getTime())/2);
 	}
 
 	public long getCenterDeviation() {
 		return this.durationSeconds+this.deviationSeconds;
 	}
 	
 }
