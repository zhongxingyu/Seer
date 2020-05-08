package fellesp;
 
 import java.sql.Time;
 
 public class Notification {
 	
 	private Time alarm;
 	private int i;
 
 	public Notification() {
 		this.i;
 	}
 	public Time getAlarm() {
 		return alarm;
 	}
 
 	public void setAlarm(Time alarm) {
 		this.alarm = alarm;
 	}
 	
 	public int response(int i){
 		if (i == 1){return 1;}
 		else if (i == 2){return 2;}
 		else {return 3;}
 	}
 	
 	public boolean acceptInvite(){
 		return false;
 	}
 	
 	public boolean rejectInvite(){
 		return false;
 	}
 	
 }
