 package com.schema.bro.ks;
 
 import java.util.Calendar;
 import java.util.StringTokenizer;
 
 import android.util.Log;
 
 public class Lesson implements Comparable<Lesson>{
 
 	private String weekday = "null";
 	private String startTime = "null";
 	private String endTime = "null";
 	private String name = "null";
 	private String room = "null";
 	private String master = "null";
 	private int ID = -1;
 	private int image = -1;
 	private int weekdayVal;
 	private int startHour;
 	private int startMinute;
 	private int endHour;
 	private int endMinute;
 	
 	public final static String SEPARATOR = "|";
 
 	public Lesson(String lessonString) throws Exception{
 		if(lessonString.equals("empty"))
 			return;
 		
 		// Decode string
 		try{
 			StringTokenizer token = new StringTokenizer(lessonString, SEPARATOR);
 			weekday = token.nextToken();
 			startTime = token.nextToken();
 			endTime = token.nextToken();
 			name = token.nextToken();
 			room = token.nextToken();
 			master = token.nextToken();
 			image = Integer.parseInt(token.nextToken());
 			ID = Integer.parseInt(token.nextToken());
 			init();
 		}catch(Exception ex){
 			Log.e("Lesson:Constuctor", "Couldn't load lesson. \n" + toString());
 			throw ex;
 		}
 		
 		
 	}
 	
 	public Lesson(String weekday, String startTime, String endTime, String name, String room,
 			String master, int image, int ID) {
 
 		this.weekday = weekday;
 		this.startTime = startTime;
 		this.endTime = endTime;
 		this.name = name;
 		this.room = room;
 		this.master = master;
 		this.image = image;
 		this.ID = ID;
 		
 		init();
 	}
 	
 	private void init(){
 	
 		weekdayVal = setWeekdayValue(weekday);
 		startHour = setHour(startTime);
 		startMinute = setMinute(startTime);
 		endHour = setHour(endTime);
 		endMinute = setMinute(endTime);
 		
 		
 	}
 	
 	private int setWeekdayValue(String weekday){
 		if(weekday.equals("Måndag"))
 			return Calendar.MONDAY;
 		else if(weekday.equals("Tisdag"))
 			return Calendar.TUESDAY;
 		else if(weekday.equals("Onsdag"))
 			return Calendar.WEDNESDAY;
 		else if(weekday.equals("Torsdag"))
 			return Calendar.THURSDAY;
 		else if(weekday.equals("Fredag"))
 			return Calendar.FRIDAY;
 		else if(weekday.equals("Lördag")
 				|| weekday.equals("Söndag"))
 			return 7;
 		
 		return -1;
 	}
 	
 	private int setHour(String time){
 		StringTokenizer token = new StringTokenizer(time, ":");
 		String hour = token.nextToken();
 		return Integer.parseInt(hour);
 	}
 	
 	private int setMinute(String time){
 		StringTokenizer token = new StringTokenizer(time, ":");
 		token.nextToken(); // Hour
 		String minute = token.nextToken();
 		return Integer.parseInt(minute);
 	}
 	
 	public String getWeekday() {
 		return weekday;
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	public String getStartTime() {
 		return startTime;
 	}
 
 	public String getEndTime() {
 		return endTime;
 	}
 
 	public String getRoom() {
 		return room;
 	}
 
 	public String getMaster() {
 		return master;
 	}
 	
 	public int getStartHour() {
 		return startHour;
 	}
 
 	public int getStartMinute() {
 		return startMinute;
 	}
 	
 	public int getEndHour() {
 		return endHour;
 	}
 
 	public int getEndMinute() {
 		return endMinute;
 	}
 	
 	public int getImage() {
 		return image;
 	}
 	
 	public int getID() {
 		return ID;
 	}
 	
 	public int getWeekdayValue() {
 		return weekdayVal;
 	}
 
 	/** Get the time until this lesson starts or until it ends.
 	 * 
 	 * @param during - true if to compare with end time, otherwise false. 
 	 * @return timeLeft - the time left in minutes until start or end time.
 	 */
 	public String getTimeLeft(boolean during){
 		final int timeLeft = getTimeLeftVal(during);
 		if (timeLeft/(24*60) >= 1)
 			if(timeLeft/(24*60) < 2)
 				return timeLeft/(24*60) + " dag";
 			else
 				return timeLeft/(24*60) + " dagar";
 		else if (timeLeft/60 >= 1){
 			final int hours = (int) Math.floor(timeLeft/60);
 			final int minutes = (int) (timeLeft - Math.floor(timeLeft/60)*60);
 			return hours + "h " + minutes + "m";
 		}else
 			return timeLeft + "min";
 		// Brolund has been here!
 		// This is no code of K-S!
 	}
 	
 	/** Get the time until this lesson starts or until it ends.
 	 * 
 	 * @param during - true if to compare with end time, otherwise false. 
 	 * @return timeLeft - the time left in minutes until start or end time.
 	 */
 	public int getTimeLeftVal(boolean during){
		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		int minute = Calendar.getInstance().get(Calendar.MINUTE);
 		if(during){
 			int hourDif = hour - endHour;
 			int minuteDif = minute - endMinute;
 			return Math.abs(hourDif * 60 + minuteDif);
 		}else{
 			int hourDif = hour - startHour;
 			int minuteDif = minute - startMinute;
			if (hourDif * 60 + minuteDif > 0){
				int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
				int lessonDay = getWeekdayValue();
 				if (day >= lessonDay)
 					if(lessonDay - day >= 0)
 						return (lessonDay-day)*24*60;
 					else
 						return (lessonDay-day+7)*24*60;
 					//return Math.abs(7-lessonDay+day)*24*60;
 				else
 					return Math.abs(day-lessonDay)*24*60;
 			}else
 			return Math.abs(hourDif * 60 + minuteDif);
 			// Brolund has been here!
 			// K-S is not responsible for any faults here!
 		}
 	}
 
 	public void setWeekday(String weekday){
 		this.weekday = weekday;
 		init();
 	}
 	
 	public void setStartTime(String startTime) {
 		this.startTime = startTime;
 		init();
 	}
 
 	public void setEndTime(String endTime) {
 		this.endTime = endTime;
 		init();
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public void setRoom(String room) {
 		this.room = room;
 	}
 
 	public void setMaster(String master) {
 		this.master = master;
 	}
 
 	public void setHour(int startHour) {
 		this.startHour = startHour;
 	}
 
 	public void setMinute(int startMinute) {
 		this.startMinute = startMinute;
 	}
 	
 	public void setImage(int image) {
 		this.image = image;
 	}
 	
 	public void setID(int ID) {
 		this.ID = ID;
 	}
 	
 	public static String convertToString(String weekday, String startTime, String endTime, String name, String room,
 			String master, int image, int ID){
 		return weekday + SEPARATOR + startTime + SEPARATOR + endTime
 				+ SEPARATOR + name + SEPARATOR + room + SEPARATOR + master + SEPARATOR + String.valueOf(image)
 				+ SEPARATOR + String.valueOf(ID);
 	}
 	
 	public String toString() {
 		return weekday + SEPARATOR + startTime + SEPARATOR + endTime
 				+ SEPARATOR + name + SEPARATOR + room + SEPARATOR + master + SEPARATOR + String.valueOf(image)
 				+ SEPARATOR + String.valueOf(ID);
 	}
 
 	@Override
 	public int compareTo(Lesson lesson) {
 		// Day
 		if(weekdayVal > lesson.getWeekdayValue())
 			return -1;
 		if(weekdayVal < lesson.getWeekdayValue())
 			return 1;
         // Hour
 		if(startHour > lesson.getStartHour())
 			return -2;
 		if(startHour < lesson.getStartHour())
 			return 2;
 		// Minute
 		if(startMinute > lesson.getStartMinute())
 			return -2;
 		if(startMinute < lesson.getStartMinute())
 			return 2;
 		// Equal
 		return 0;
 	}
 	
 	@Override
 	public boolean equals(Object lesson) {
 		Lesson les = (Lesson) lesson;
 		String otherLes = les.toString();
 		if(toString().equals(otherLes))
 			return true;
 		return false;
 	}
 
 }
