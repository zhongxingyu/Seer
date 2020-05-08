 package com.schema.bro.ks;
 
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.StringTokenizer;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.util.Log;
 
 public class Schedule {
 
 	private PriorityList lessons;
 	private SharedPreferences data;
 	public static final String PREFS_NAME = "MY_SCHEDULE";
 	//private Lesson nextLesson;
 	
 	public Schedule(Context context) {
 		data =  context.getSharedPreferences(PREFS_NAME, 0);
 		//nextLesson = null;
 		loadLessons();
 	}
 	
 	/*
 	public Schedule(Context context, int weekday) {
 		data = PreferenceManager.getDefaultSharedPreferences(context);
 		nextLesson = null;
 		loadLessons(weekday);
 	}
 	*/
 	
 	private void loadLessons(){
 		int count = data.getInt("count", 0);
 		lessons = new PriorityList();
 		for (int n = 0; n < count; n++) {
 			String lesson = data.getString("lesson_" + n, "empty");
 			if(!lesson.equals("empty")){
 				Lesson tempLesson;
 				try {
 					tempLesson = new Lesson(lesson);
 				} catch (Exception e) {
 					data.edit().putString("lesson_" + n, "empty").commit();
 					Log.e("Error", "Removed lesson_" + n + " due to it failed to load");
 					continue;
 				}
 				lessons.add(tempLesson);
 			}
 		}
 	}
 
 	/*
 	private void loadLessons(int weekday){
 		int count = data.getInt("count", 0);
 
 		lessons = new PriorityList();
 
 		for (int n = 0; n < count; n++) {
 			String lesson = data.getString("lesson_" + n, "empty");
 			if(lesson.equals("empty"))
 				continue;
 			Lesson tempLesson;
 			try {
 				tempLesson = new Lesson(lesson);
 			} catch (Exception e) {
 				data.edit().putString("lesson_" + n, "empty").commit();
 				Log.e("Error", "Removed lesson_" + n + " due to it failed to load");
 				continue;
 			}
 			int day = tempLesson.getWeekdayValue();
 			if (day == weekday) {
 				lessons.add(tempLesson);
 			}
 		}
 	}
 	*/
 	
 	public int addLesson(Lesson lesson) {
 		return lessons.add(lesson);
 	}
 	
 	public int addLesson(String lesson) {
 		Lesson les;
 		try {
 			les = new Lesson(lesson);
 		} catch (Exception e) {
 			Log.e("Error", "Lesson failed to load");
 			return -1;
 		}
 		return lessons.add(les);
 	}
 
 	public Lesson getLesson(int pos){
 		return (Lesson) lessons.get(pos);
 	}
 	
 	/** Not working */
 	protected void removeLesson(int pos) {
 		data.edit().putInt("count", data.getInt("count", 0) - 1).commit();
 		lessons.remove(pos);
 		data.edit().remove("lesson_" + pos).commit();
 	}
 	
 	public void update() {
 		loadLessons();
 	}
 
 	/*
 	public void update(int weekday) {
 		loadLessons(weekday);
 	}
 	*/
 
 	/*
 	public Lesson[] getWeekdayLessons() {
 		Lesson[] les = new Lesson[lessons.size()];
 		for (int n = 0; n < lessons.size(); n++) {
 			les[n] = (Lesson) lessons.get(n);
 		}
 		return les;
 	}
 	*/
 
 	/**Get lesson for a specified day.
 	 * 
 	 * @param weekday - Calendar API value for the day
 	 * @return an array of lessons for the specified day
 	 */
 	public Lesson[] getLessons(int weekday) {
 		int count = 0;
 		PriorityList tempLessons = new PriorityList();
 
 		for (int n = 0; n < lessons.size(); n++) {
 			Lesson temp = (Lesson) lessons.get(n);
 			int day = temp.getWeekdayValue();
 			if (day == weekday) {
 				tempLessons.add(temp);
 				count++;
 			}
 		}
 
 		Lesson[] les = new Lesson[count];
 		for (int n = 0; n < count; n++) {
 			les[n] = (Lesson) tempLessons.removeFirst();
 		}
 
 		return les;
 	}
 	
 	/** Get the current lesson. 
 	 * <p><b>Not fully tested yet</b>
 	 * 
 	 * @return lesson - An upcoming or ongoing lesson
 	*/
 	public Lesson getCurrentLesson(){
 		if(lessons.isEmpty()){
 			Log.e("Schedule:getNextLesson", "There is less than two lessons added");
 			return null;
 		}
 		
 		int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
 		final int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
 		final int minute = Calendar.getInstance().get(Calendar.MINUTE);
 		int curTime = hour * 60 + minute;
 		
 		boolean found = false;
 		Lesson tempLesson = null;
 		
 		while(!found){
 			for(int n=lessons.size()-1; n >= 0; n--){
 				Lesson lesson = (Lesson) lessons.get(n);
 				
 				if(day != lesson.getWeekdayValue())
 					continue;
 				
 				int lesEndTime = lesson.getEndHour() * 60 + lesson.getEndMinute();
 				
 				if(curTime <= lesEndTime ){
 					Log.i("Schedule:getCurrentLesson", "Got current lesson");
 					found = true;
 					tempLesson = lesson;
 					break;
 				}
 			}
 			day++;
 			if(day == Calendar.SATURDAY)
 				day = Calendar.MONDAY;
 			curTime = 0;
 		}
 		
 		return tempLesson;
 	}
 	
 	/** Get the lesson after the current one 
 	 * <p<b>Not fully tested yet</b>
 	 * 
 	 * @return lesson - The lesson after the upcoming or ongoing lesson 
 	*/
 	public Lesson getNextLesson(){
 		if(lessons.size() < 2){
 			Log.e("Schedule:getNextLesson", "There is less than two lessons added");
 			return null;
 		}
 		
 		int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
 		final int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
 		final int minute = Calendar.getInstance().get(Calendar.MINUTE);
 		int curTime = hour * 60 + minute;
 		
 		boolean found = false;
 		Lesson tempLesson = null;
 		
 		while(!found){
 			for(int n=lessons.size()-1; n >= 0; n--){
 				Lesson lesson = (Lesson) lessons.get(n);
 				
 				if(day != lesson.getWeekdayValue())
 					continue;
 				
 				int lesEndTime = lesson.getEndHour() * 60 + lesson.getEndMinute();
 				
 				if(curTime <= lesEndTime){
 					Log.i("Schedule:getNextLesson", "Got next lesson");
 					found = true;
 					if(n + 1< lessons.size()){
 						tempLesson = getLesson(n + 1);
 					}else{
 						tempLesson = getLesson(0);
 					}
 					break;
 				}
 			}
 			day++;
 			if(day == Calendar.SATURDAY)
 				day = Calendar.MONDAY;
 			curTime = 0;
 		}
 		
 		return tempLesson;
 	}
 	
 	/** Get an ongoing lesson if there is one at the moment 
 	 * <p><b>Not tested yet</b>
 	 * 
 	 * @return lesson - An ongoing lesson if there is one, else null  
 	*/
 	public Lesson getOngoingLesson(){
 		if(isEmpty()){
 			Log.e("Schedule:getOngoingLesson", "There is no lessons added");
 			return null;
 		}
 		
 		final int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
 		final int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
 		final int minute = Calendar.getInstance().get(Calendar.MINUTE);
 		final int curTime = hour * 60 + minute;
 		
 		Lesson[] todayLessons = getLessons(day);
 		for(int n = todayLessons.length - 1; n >= 0; n--){
 			Lesson lesson = todayLessons[n];
 			int lesStartTime = lesson.getStartHour() * 60 + lesson.getStartMinute();
 			int lesEndTime = lesson.getEndHour() * 60 + lesson.getEndMinute();
 			
 			if(curTime >= lesStartTime && curTime <= lesEndTime){
 				Log.i("Schedule:getOngoingLesson", "Got current lesson");
 				return lesson;
 			}
 		}
 		
 		Log.d("Schedule:getOngoingLesson", "There is no lessons right now");
 		return null;
 	}
 	
 	
 	/*
 	public int showNextLessonCard(){
 		int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
 		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
 		int minute = Calendar.getInstance().get(Calendar.MINUTE);
 		
 		for(int i=lessons.size() - 1; i >= 0; i--){
 			Lesson lesson = (Lesson) lessons.get(i);
 			
 			if(lesson.getWeekdayValue() != day)
 				continue;
 			
 			if(lesson.getStartHour() >= hour){
 				if(lesson.getStartMinute() >= minute){
 					// Lesson occurs after current time
 					nextLesson = lesson;
 					return 0;
 				}
 			}
 			
 			if(lesson.getEndHour() >= hour){
 				if(lesson.getEndMinute() >= minute){
 					// Lesson ends after current time
 					nextLesson = lesson;
 					return 1;
 				}
 			}
 		}
 		// No lesson match at all
 		nextLesson = null;
 		return -1;
 	}
 	*/
 	
 	/*
 	public Lesson getNextLesson(){
 		if(nextLesson == null)
 			showNextLessonCard();
 		return nextLesson;
 	}
 	*/
 	
 	/**
 	 * Clear the entire schedule.
 	 */
 	public void clear(){
 		data.edit().clear().commit();
 		lessons.clear();
 		if(lessons.isEmpty())
 			Log.d("Schedule:clear", "All lessons where removed");
 	}
 	
 
 	/** Add an entire schedule.
 	 * 
 	 * @param schedule - a string with information for the whole schedule
 	 */
 	public void addSchedule(String schedule){
 		clear();
 		StringTokenizer token = new StringTokenizer(schedule, "\n");
 		
 		int count = 0;
 		while(token.hasMoreTokens()){
 			String lesson = token.nextToken();
			StringTokenizer tok = new StringTokenizer(lesson, "|");
			Log.e("Schedule", lesson);
			for(int n=0; n<7; n++)
				tok.nextToken(); 
			String id = tok.nextToken();
			data.edit().putString("lesson_" + id, lesson).commit();
			int n = Integer.parseInt(id) + 1;
			if(n > count ){
				count = n;
			}
 		}
 		data.edit().putInt("count", count).commit();
 		Log.d("Schedule:addSchedule", count + " lessons where added");
 	}
 	
 	/** Get the entire schedule as an 
 	 * 	byte array for sending to another unit.
 	 * 
 	 * @return schedule - The entire schedule as an byte array.
 	 */
 	public byte[] getSchedule(){
 		String schedule = "";
 		for(int n=0; n < lessons.size(); n++){
 			Lesson lesson = getLesson(n);
 			schedule += lesson.toString();
 			if(n != lessons.size() - 1)
 				schedule += "\n";
 		}
 		return schedule.getBytes();
 	}
 	
 	public boolean isEmpty(){
 		return lessons.isEmpty();
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public static class PriorityList extends LinkedList {
 
 		private static final long serialVersionUID = 1L;
 
 		@SuppressWarnings("unchecked")
 		public int add(Comparable object) {
 			for (int i = 0; i < size(); i++) {
 				if (object.compareTo(get(i)) <= 0) {
 					add(i, object);
 					return i;
 				}
 			}
 			addLast(object);
 			return size();
 		}
 	}
 }
