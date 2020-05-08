 package edu.calpoly.ai.skynest;
 
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 
 
 /**ScheduleManager: Used to initialize the schedule (set schedule values to a 
  * 					certain time, regardless of the averaging algorithm) and 
  * 					read the current values from the schedule.
  * 
  * Usage:
  * 
  * ScheduleManager sm = new ScheduleManager(shared_preferences_obj);
  * int[] schedule = sm.getScheduleInts(); //returns current schedule as 14 ints
  * 
  * //one way of initializing times
  * sm.setTimeSlot(0, 550);  //sun depart
  * sm.setTimeSlot(1, 1000); //sun arrive
  * sm.setTimeSlot(2, 550);  //mon depart
  * sm.setTimeSlot(3, 1000); //mon arrive
  * sm.setTimeSlot(4, 550);  //tue depart
  * sm.setTimeSlot(5, 1000); //tue arrive
  * 
  * //another way of initializing times
  * sm.setTime(0, 0, 500); //sun depart
  * sm.setTime(0, 1, 800); //sun arrive
  * sm.setTime(1, 0, 500); //mon depart
  * sm.setTime(1, 1, 800); //mon arrive
  * sm.setTime(2, 0, 500); //tue depart
  * sm.setTime(2, 1, 800); //tue arrive
  * 
  * //one way of updating times
  * sm.updateTimeSlot(0, 582); //update sun depart
  * 
  * 
  * //reading
  * System.out.println("Sun Depart:" + sm.getTimeSlot(0));
  * System.out.println("Sun Arrive:" + sm.getTimeSlot(1));
  * System.out.println("Mon Depart:" + sm.getTimeSlot(2));
  * System.out.println("Mon Arrive:" + sm.getTimeSlot(3));
  * 
  **/
 
 public class ScheduleManager {
 	protected Schedule loaded_schedule;
 	SharedPreferences sp;
 	
 	/** constants **/
 	private final static int    NUM_DAYS = 7;
 	private final static int    NUM_TIMES = 2;
 	private final static double NEW_DATA_WEIGHT = 0.20;	//new data weighs 20% when averaged w/ old data
 
 	public ScheduleManager(SharedPreferences sp) {
 		this.sp = sp;
 	}
 
 	/** returns whole schedule from shared preferences as a Schedule object 
 	 * 	
 	 * 	the private Schedule class inside the ScheduleManager class might 
 	 * 	need to be changed to protected or something for to make the schedule 
 	 * 	object visible 
 	 **/
 	public Schedule getSchedule() {
 		loadSchedule();
 		return loaded_schedule;
 	}
 
 	/** returns whole schedule as an array of ints 
 	 * 	returns: int[14] [sun_depart, sun_arrive, mon_depart, ... 
 	 **/
 	public int[] getScheduleInts() {
 		int[] int_schedule = new int[NUM_DAYS*2];
 		int dayInt, timeInt;
 		loadSchedule();
 		
 		for(dayInt=0, timeInt=0; dayInt < NUM_DAYS; timeInt++){
 			if(timeInt == 2)
 			{
 				dayInt++;
 				timeInt=0;
 			}
 			
 			if(dayInt != NUM_DAYS)
 			{			
 				int_schedule[dayInt*2 + timeInt] = loaded_schedule.getTime(dayInt, timeInt);
 			}
 							
 		}
 		
 		return int_schedule;
 	}
 
 	/** returns specific integer time from the schedule
 	 *  parameters: int dayInt (0-6 : sun, mon, tue, ...)
 	 *  			int timeInt (0-1 : departure, arrival) 
 	 *  returns: 	int (minutes since start of day) 
 	 **/
 	public int getTime(int dayInt, int timeInt) {
 		loadSchedule();
 		return loaded_schedule.getTime(dayInt, timeInt);
 	}
 	
 	/** returns specific integer time from the schedule
 	 *  parameter: 	int slot (0-13 : sun_depart, sun_arrive, mon_depart, ...)
 	 *  returns: 	int (minutes since start of day) 
 	 **/
 	public int getTimeSlot(int slot) {
 		return this.getTime(slot/2, slot%2);
 	}
 	
 	/** sets specific integer time in the schedule 
 	 *  parameters: int dayInt (0-6 : sun, mon, tue, ...)
 	 *  			int timeInt (0-1 : departure, arrival)
 	 *  			int value (0-1440 : minutes since start of day) 
 	 **/
 	public void setTime(int dayInt, int timeInt, int value) {
 		loadSchedule();
 		loaded_schedule.setTime(dayInt, timeInt, value);
 		saveSchedule();
 	}
 
 	/** sets specific integer time in the schedule 
 	 *  parameters: int slot (0-13 : sun_depart, sun_arrive, mon_depart, ...)
 	 *  			int value (0-1440 : minutes since start of day) 
 	 **/
 	public void setTimeSlot(int slot, int value) {
 		this.setTime(slot/2, slot%2, value);
 	}
 	
 	/** updates shared preferences schedule using the time averaging algorithm.
 	 * 	uses constant NEW_DATA_WEIGHT
 	 *  parameters: int dayInt (0-6 : sun, mon, tue, ...)
 	 *  			int timeInt (0-1 : departure, arrival)
 	 *  			int value (0-1440 : minutes since start of day) 
 	 **/
 	public void updateTime(int dayInt, int timeInt, int value) {
 		int	old_value = loaded_schedule.getTime(dayInt, timeInt);		
 		int new_value = (int) ( (double) ( old_value * (1.0-NEW_DATA_WEIGHT) ) +
 							  ( (double) value*NEW_DATA_WEIGHT) );
 		
 		this.setTime(dayInt, timeInt, new_value);
 	}
 
 	/** updates shared preferences schedule using the time averaging algorithm.
 	 * 	uses constant NEW_DATA_WEIGHT
 	 *  parameters: int slot (0-13 : sun_depart, sun_arrive, mon_depart, ...)
 	 *  			int value (0-1440 : minutes since start of day) 
 	 **/
 	public void updateTimeSlot(int slot, int value) {
 		this.updateTime(slot/2, slot%2, value);
 	}
 	
 	
 	/* private functions/classes */
 	/** saves the loaded schedule into shared preferences **/
 	private void saveSchedule() {
 		Editor e = sp.edit();
     	for(int index=0; index<14; index++) {
     		e.putInt("sched" + index, loaded_schedule.getTime(index/2, index%2));
     	}
 		e.commit();
 	}
 	
 
 	/** reads shared preferences to refresh the the loaded schedule **/
 	private void loadSchedule() {
 		if(loaded_schedule == null){
 			loaded_schedule = new Schedule();
 		}
     	for(int index=0; index<14; index++) {
     		if(sp != null && sp.contains("sched" + index))
    			loaded_schedule.setTime(index/2, index%2, sp.getInt("sched" + index, -1));
     	}
 	}
 	
 	/**TODO: change this from private to something else (like protected/static)
 	 * if someone wants to use a Schedule object externally
 	 **/
 	private class Schedule {
 		private Day[] days = new Day[NUM_DAYS];
 		
 		public Schedule() {
 			int iter;
 			
 			for(iter=0; iter<NUM_DAYS; iter++){
 				days[iter] = new Day();
 			}
 		}
 		
 		public Day getDay(int day){
 			if(day < 0 || day > NUM_DAYS-1)
 				throw new IndexOutOfBoundsException("day argument has range 0 - " + (NUM_DAYS-1));
 
 			return this.days[day];
 		}
 
 		public void setDay(int which_day, Day day_obj){
 			if(day_obj == null)
 				throw new IllegalArgumentException("you cannot set day to null");
 			if(which_day < 0 || which_day > NUM_DAYS-1)
 				throw new IndexOutOfBoundsException("day argument has range 0 - " + (NUM_DAYS-1));
 			
 			this.days[which_day] = day_obj;
 		}
 		
 		public int getTime(int dayInt, int timeInt){
 			if(dayInt < 0 || dayInt > NUM_DAYS-1)
 				throw new IndexOutOfBoundsException("day argument has range 0 - " + (NUM_DAYS-1));
 			if(timeInt < 0 || timeInt > NUM_TIMES-1)
 				throw new IndexOutOfBoundsException("time argument has range 0 - " + (NUM_TIMES-1));
 			
 			if(this.getDay(dayInt) != null){
 				switch(timeInt){
 				case(0):
 						return this.getDay(dayInt).departure;
 				case(1):
 						return this.getDay(dayInt).arrival;
 				}
 			}
			return -2;
 		}
 		
 		public void setTime(int dayInt, int timeInt, int time_value){
 			if(dayInt < 0 || dayInt > NUM_DAYS-1)
 				throw new IndexOutOfBoundsException("day argument has range 0 - " + (NUM_DAYS-1));
 			if(timeInt < 0 || timeInt > NUM_TIMES-1)
 				throw new IndexOutOfBoundsException("time argument has range 0 - " + (NUM_TIMES-1));
 		
 			if(this.getDay(dayInt) == null){
 				this.setDay(dayInt, new Day());
 			}
 			
 			switch(timeInt){
 				case(0):
 						this.getDay(dayInt).departure = time_value;
 						break;
 				case(1):
 						this.getDay(dayInt).arrival = time_value;
 						break;
 			}
 		}
 		
 		private class Day {
 			public int departure, arrival;
 
 			public Day(){
				this.departure = -1;
				this.arrival = -1;
 			}
 			
 			@SuppressWarnings("unused")
 			public Day(int arrival, int departure){
 				this.departure = departure;
 				this.arrival = arrival;
 			}
 		}
 	}
 }
