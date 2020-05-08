 package org.utn.proyecto.helpful.integrart.integrar_t_android.activities.calendar;
 
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.Set;
 
 import android.graphics.drawable.Drawable;
 
 public class Task implements Comparable<Task>{
 	private final TaskType type;
 	private final int year;
 	private final int month;
 	private final int day;
 	private final int hour;
 	private final int minute;
 	private int size;
 	private Set<Integer> repeatDays = new HashSet<Integer>();
 	private TaskState state;
 	private long initTime;
 	
 	public Task(TaskType type, Calendar date){
 		this(type, date, 5);
 	}
 	
 	public Task(TaskType type, Calendar date, int size){
 		this.type = type;
 		this.year = date.get(Calendar.YEAR);
 		this.month = date.get(Calendar.MONTH);
 		this.day = date.get(Calendar.DAY_OF_MONTH);
 		this.hour = date.get(Calendar.HOUR_OF_DAY);
 		this.minute = date.get(Calendar.MINUTE);
 		this.size = size;
 		this.state = TaskState.DEFAULT;
 	}
 	
 	public String getName(){
 		return type.getName();
 	}
 	
 	public Drawable getPictogram(){
 		return type.getPictogram();
 	}
 	
 	public Drawable getLargeImage(){
		if(type.getLargeImage()!=null)
			return type.getLargeImage();
		if(type.getSmallImage()!=null)
			return type.getSmallImage();
		return type.getPictogram();
 	}
 	
 	public Drawable getSmallImage(){
		if(type.getSmallImage()!=null)
			return type.getSmallImage();
		if(type.getPictogram()!=null)
			return type.getPictogram();
		return type.getLargeImage();
 	}
 	
 	public boolean initAtHour(int hour){
 		int myHour = this.hour;
 		return myHour == hour;
 	}
 	
 	public boolean finalizeAtHour(int hour){
 		int myHour = this.hour;
 		int hours = (this.minute + size)/60;
 		return hour == (myHour + hours);
 	}
 	
 	public boolean inHour(int hour){
 		int myHour = this.hour;
 		int hours = (this.minute + size)/60;
 		return hour>=myHour && hour<=(myHour + hours);
 	}
 	
 	public boolean isInitialized(int hour, int minute){
 		return this.hour < hour || (this.hour==hour && this.minute <= minute); 
 	}
 	
 	public boolean isFinalized(int hour, int minute){
 		int myHour = this.hour + (this.minute + this.size)/60;
 		int myMinute = (this.minute + this.size)%60;
 		return isInitialized(hour, minute) && (myHour < hour || (myHour==hour && myMinute <= minute)); 
 	}
 	
 	public int getRemainingMinutes(int hour, int minute){
 		if(!isInitialized(hour, minute)) return size;
 		if(isFinalized(hour, minute)) return 0;
 		int myTime = this.hour*60 + this.minute + size;
 		int time = hour*60 + minute;
 		return myTime - time;
 	}
 	
 	public long getInitTime(){
 		return initTime;
 	}
 	
 	public void setInitTime(long time){
 		this.initTime = time;
 	}
 
 	public int getSize() {
 		return size;
 	}
 
 	public void setSize(int size) {
 		this.size = size;
 	}
 
 	public TaskType getType() {
 		return type;
 	}
 	
 	public int getHour(){
 		return hour;
 	}
 	
 	public int getMinute(){
 		return minute;
 	}
 	
 	public int getYear(){
 		return year;
 	}
 	
 	public int getMonth(){
 		return month;
 	}
 
 	public int getDay(){
 		return day;
 	}
 	
 	public boolean isRepeatable(){
 		return !repeatDays.isEmpty();
 	}
 	
 	public TaskData getData(){
 		return new TaskData(this);
 	}
 	
 	public void addRepeatDay(int dayOfWeek){
 		repeatDays.add(dayOfWeek);
 	}
 	
 	public void removeRepeatDay(int dayOfWeek){
 		repeatDays.remove(dayOfWeek);
 	}
 	
 	public void clearRepeat(){
 		repeatDays.clear();
 	}
 	
 	public Set<Integer> getRepeatDays(){
 		return new HashSet<Integer>(repeatDays);
 	}
 	
 	public boolean isRepeatAtDay(int dayOfWeek){
 		return repeatDays.contains(dayOfWeek);
 	}
 	
 	public String getTimeString(){
 		StringBuffer sb = new StringBuffer();
 		sb.append(hour<10? "0" + hour : hour);
 		sb.append(":");
 		sb.append(minute<10? "0" + minute : minute);
 		return sb.toString();
 	}
 	
 	public boolean isCompleted(){
 		return this.state == TaskState.BAD_COMPLETE || this.state == TaskState.COMPLETED;
 	}
 	
 	@Override
 	public boolean equals(Object o){
 		if(!(o instanceof Task)) return false;
 		Task t = (Task)o;
 		return this.type.equals(t.type) 
 				&& this.hour == t.hour
 				&& this.minute == t.minute;
 	}
 	
 	/**
 	 * Devuelve true si y solo si el estado cambi
 	 * @param hour
 	 * @param minute
 	 * @return
 	 */
 	public boolean changeState(int hour,int minute){
 		int time = hour*60 + minute;
 		int myTime = this.hour*60 + this.minute;
 		if(time == myTime){
 			return changeToInitState();
 		}
 		if(time == myTime+size){
 			return changeToEndState();
 		}
 		
 		if(time < myTime){
 			if(state != TaskState.DEFAULT){
 				state = TaskState.DEFAULT;
 				return true;
 			}
 			return false;
 		}
 		
 		if(time > myTime+this.size){
 			return changeToEndState();
 		}
 		
 		if(state == TaskState.READY || state == TaskState.ACTIVED || state== TaskState.BAD_COMPLETE || state==TaskState.COMPLETED) return false;
 		state = TaskState.READY;
 		
 		return true;
 	}
 	
 	private boolean changeToEndState(){
 		if(state==TaskState.EXCEEDED || state==TaskState.COMPLETED || state==TaskState.BAD_COMPLETE) return false;
 		state = TaskState.EXCEEDED;
 		return true;
 	}
 	
 	private boolean changeToInitState(){
 		if(state==TaskState.DEFAULT){
 			state = TaskState.READY;
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean active(){
 		if(this.state==TaskState.BAD_COMPLETE || this.state==TaskState.COMPLETED){
 			return false;
 		}
 		this.state = TaskState.ACTIVED;
 		return true;
 	}
 	
 	public boolean terminate(){
 		this.state = this.isFinalized(Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE))
 				? TaskState.COMPLETED
 				: TaskState.BAD_COMPLETE;
 		return true;
 	}
 	
 	public int hashCode(){
 		return this.type.hashCode() - this.hour + this.minute;
 	}
 	
 	@Override
 	public String toString(){
 		return getName() + " at " + hour + "h " + minute + "m";
 	}
 	
 	@Override
 	public int compareTo(Task another) {
 		if(equals(another)){
 			if(another.getState()!=this.getState()){
 				if(this.getState()==TaskState.DEFAULT){
 					this.setState(another.getState());
 				}
 				if(another.getState()==TaskState.DEFAULT){
 					another.setState(this.getState());
 				}
 			}
 		}
 		if(this.hour != another.hour)
 			return this.hour - another.hour;
 		return this.minute - another.minute;
 	}
 
 	public TaskState getState() {
 		return state;
 	}
 
 	public void setState(TaskState state) {
 		this.state = state;
 	}
 	
 	public class TaskData{
 		private final String type;
 		private final int year;
 		private final int month;
 		private final int day;
 		private final int hour;
 		private final int minute;
 		private final int size;
 		private final boolean repeatable;
 		private final TaskState state;
 		private long initTime;
 		
 		private TaskData(Task task){
 			this.type = task.getName();
 			this.year = task.getYear();
 			this.month = task.getMonth();
 			this.day = task.getDay();
 			this.hour = task.getHour();
 			this.minute = task.getMinute();
 			this.size = task.getSize();
 			this.repeatable = task.isRepeatable();
 			this.state = task.getState();
 			this.initTime = task.getInitTime();
 		}
 		
 		public long getInitTime(){
 			return initTime;
 		}
 
 		public int getSize() {
 			return size;
 		}
 
 		public String getType() {
 			return type;
 		}
 
 		public int getYear() {
 			return year;
 		}
 
 		public int getMonth() {
 			return month;
 		}
 
 		public int getDay() {
 			return day;
 		}
 
 		public int getHour() {
 			return hour;
 		}
 
 		public int getMinute() {
 			return minute;
 		}
 		
 		public boolean getRepeatable(){
 			return repeatable;
 		}
 		
 		public TaskState getState(){
 			return state;
 		}
 		
 		public Calendar buildCalendar(){
 			Calendar date = (Calendar) Calendar.getInstance().clone();
 			date.set(Calendar.YEAR, year);
 			date.set(Calendar.MONTH, month);
 			date.set(Calendar.DAY_OF_MONTH, day);
 			date.set(Calendar.HOUR_OF_DAY, hour);
 			date.set(Calendar.MINUTE, minute);
 			return date;
 		}
 	}
 }
