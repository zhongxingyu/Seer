 package hoplugins.feedback.model.training;
 
 import hoplugins.Commons;
 
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 public class TrainingsWeek {
 	private Timestamp timestamp;
 	private int hrfId;
 	private int trainingType;
 	private int trainingIntensity;
 	private int staminaTrainingPart;
 	
 	public TrainingsWeek(Timestamp timestamp, int hrfId, int trainingType,
 			int trainingIntensity, int staminaTrainingPart) {
 		super();
 		this.timestamp = timestamp;
 		this.hrfId = hrfId;
 		this.trainingType = trainingType;
 		this.trainingIntensity = trainingIntensity;
 		this.staminaTrainingPart = staminaTrainingPart;
 	}
 	
 	public Timestamp getTimestamp() {
 		return timestamp;
 	}
 
 	public void setTimestamp(Timestamp timestamp) {
 		this.timestamp = timestamp;
 	}
 
 	public int getHrfId() {
 		return hrfId;
 	}
 
 	public void setHrfId(int hrfId) {
 		this.hrfId = hrfId;
 	}
 
 	public int getTrainingType() {
 		return trainingType;
 	}
 	public void setTrainingType(int trainingType) {
 		this.trainingType = trainingType;
 	}
 	public int getTrainingIntensity() {
 		return trainingIntensity;
 	}
 	public void setTrainingIntensity(int trainingIntensity) {
 		this.trainingIntensity = trainingIntensity;
 	}
 	public int getStaminaTrainingPart() {
 		return staminaTrainingPart;
 	}
 	public void setStaminaTrainingPart(int staminaTrainingPart) {
 		this.staminaTrainingPart = staminaTrainingPart;
 	}
 
 	public int getHtSeason () {
 		return Commons.getModel().getHelper().getHTSeason(timestamp);
 	}
 
 	public int getHtWeek() {
 		return Commons.getModel().getHelper().getHTWeek(timestamp);
 	}
 
 	public int getYear() {
 		Calendar cal = new GregorianCalendar();
 		cal.setTimeInMillis(timestamp.getTime());
 		return cal.get(Calendar.YEAR);
 	}
 
 	public int getWeek() {
 		Calendar cal = new GregorianCalendar();
 		cal.setTimeInMillis(timestamp.getTime());
 		return cal.get(Calendar.WEEK_OF_YEAR);
 	}
 
 	@Override
 	public String toString () {
		return ("TrainingsWeek: ("+timestamp.toString()+", "+ getHtSeason()+"."+getHtWeek()+", hrfId="+hrfId+"): TYP="+getTrainingType()+", TI="+getTrainingIntensity()+", SS="+getStaminaTrainingPart());
 	}
 }
