 package com.schema.bro.ks;
 
 import java.util.LinkedList;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 public class Schedule {
 
 	private PriorityList lessons;
 	private SharedPreferences data;
 
 	public Schedule(Context context) {
 		data = PreferenceManager.getDefaultSharedPreferences(context);
 		int count = data.getInt("count", 0);
 		lessons = new PriorityList();
 		for (int n = 0; n < count; n++) {
			lessons.add(new Lesson(data.getString("lesson_" + n, "empty")));
 		}
 	}
 
 	public Schedule(Context context, int weekday) {
 		data = PreferenceManager.getDefaultSharedPreferences(context);
 		int count = data.getInt("count", 0);
 
 		lessons = new PriorityList();
 
 		for (int n = 0; n < count; n++) {
			Lesson tempLesson = new Lesson(data.getString("lesson_" + n, "empty"));
 			int day = tempLesson.getWeekdayValue();
 			if (day == weekday) {
 				lessons.add(tempLesson);
 			}
 		}
 
 	}
 
 	public void addLesson(String lesson) {
 		int count = data.getInt("count", 0) + 1;
 		data.edit().putInt("count", count).commit();
 		lessons.add(new Lesson(lesson));
 		data.edit().putString("lesson_" + count, lesson).commit();
 	}
 
 	public void removeLesson(int pos) {
 		data.edit().putInt("count", data.getInt("count", 0) - 1).commit();
 		lessons.remove(pos);
 		data.edit().remove("lesson_" + pos).commit();
 	}
 
 	public void update() {
 		int count = data.getInt("count", 0);
 
 		if (count == lessons.size())
 			return;
 
 		lessons = new PriorityList();
 		for (int n = 0; n < count; n++) {
 			lessons.add(new Lesson(data.getString("lesson_" + n, "empty")));
 		}
 	}
 
 	public void update(int weekday) {
 		int count = data.getInt("count", 0);
 
 		lessons = new PriorityList();
 
 		for (int n = 0; n < count; n++) {
 			Lesson tempLesson = new Lesson(data.getString("lesson_" + n, "empty"));
 			int day = tempLesson.getWeekdayValue();
 			if (day == weekday) {
 				lessons.add(tempLesson);
 			}
 		}
 	}
 
 	public Lesson[] getWeekdayLessons() {
 		Lesson[] les = new Lesson[lessons.size()];
 		for (int n = 0; n < lessons.size(); n++) {
 			les[n] = (Lesson) lessons.get(n);
 		}
 		return les;
 	}
 
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
 
 	@SuppressWarnings("rawtypes")
 	public static class PriorityList extends LinkedList {
 
 		private static final long serialVersionUID = 1L;
 
 		@SuppressWarnings("unchecked")
 		public void add(Comparable object) {
 			for (int i = 0; i < size(); i++) {
 				if (object.compareTo(get(i)) <= 0) {
 					add(i, object);
 					return;
 				}
 			}
 			addLast(object);
 		}
 	}
 }
