 package helper;
 
 public class ClassMeeting {
 
 	private String day;
 	private String periodBegin;
 	private String periodEnd;
 	private String bldg;
 	private String room;
 	
 	public ClassMeeting(String day, String periodBegin, String periodEnd, String bldg, String room) {
 		this.day = day;
 		this.periodBegin = periodBegin;
 		this.periodEnd = periodEnd;
 		this.bldg = bldg;
 		this.room = room;
 	}
 
 	public String getDay() {
 		return day;
 	}
 
 	public void setDay(String days) {
 		this.day = days;
 	}
 
 	public String getPeriodBegin() {
 		return periodBegin;
 	}
 
 	public void setPeriodBegin(String periodBegin) {
 		this.periodBegin = periodBegin;
 	}
 
 	public String getPeriodEnd() {
 		return periodEnd;
 	}
 
 	public void setPeriodEnd(String periodEnd) {
 		this.periodEnd = periodEnd;
 	}
 	
 	public String getBldg() {
 		return bldg;
 	}
 
 	public void setBldg(String bldg) {
 		this.bldg = bldg;
 	}
 
 	public String getRoom() {
 		return room;
 	}
 
 	public void setRoom(String room) {
 		this.room = room;
 	}
 
 	public int getDuration(){
 		int begin = convertTime(periodBegin);
 		int end = convertTime(periodEnd);
		System.out.print("Begin:"+begin+"\tEnd:"+end+" ");
 		int minutes = end%100 - begin%100;
 		int hours = end/100 - begin/100;	
 		return hours*60 + minutes;
 	}
 	
 	private int convertTime(String time){
 		if(time.charAt(4) == 'A'){
 			return Integer.parseInt(time.substring(0,4));
 		}
 		else if(time.charAt(4) == 'P'){
 			int militaryTime = Integer.parseInt(time.substring(0,4));
 			int hours = militaryTime/100;
 			if(hours == 12)
 				hours = 0;
 			return militaryTime%100 + hours*100 + 1200;
 		}
 		else
 			return 0;
 	}
 	
 	
 	@Override
 	public String toString() {
 		return "ClassMeeting [day=" + day + ", periodBegin=" + periodBegin
 				+ ", periodEnd=" + periodEnd + ", bldg=" + bldg + ", room="
 				+ room + "]";
 	}
 
 }
