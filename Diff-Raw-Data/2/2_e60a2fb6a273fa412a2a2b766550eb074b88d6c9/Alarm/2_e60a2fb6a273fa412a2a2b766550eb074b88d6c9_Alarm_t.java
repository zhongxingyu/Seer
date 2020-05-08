 /*
  * Copyright (C) 2012 Joakim Persson, Daniel Augurell, Adrian Bjugrd, Andreas Roln
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.chalmers.dat255.group09.Alarmed.model;
 
 import java.util.Calendar;
 
 public class Alarm implements Comparable<Alarm> {
 
 	private final int alarmHours;
 	private final int alarmMinutes;
 	private final int id;
 	private boolean enabled;
 	private String module;
 	private int volume;
 	private int daysOfWeek;
 
 	public Alarm(int hours, int minutes, int id)
 			throws IllegalArgumentException {
 		if (isIllegalHour(hours) || isIllegalMinutes(minutes)) {
 			throw new IllegalArgumentException("Illegal constructor argument!");
 		}
 		this.alarmHours = hours;
 		this.alarmMinutes = minutes;
 		this.id = id;
 	}
 
 	public Alarm(int hours, int minutes, int id, String module, int volume)
 			throws IllegalArgumentException {
 		this(hours, minutes, id);
 		this.module = module;
 		this.enabled = true;
 		this.volume = volume;
 		this.daysOfWeek = 0;
 	}
 
 	public long getTimeInMilliSeconds() {
 		Calendar cal = Calendar.getInstance();
 
 		if (isHourTomorrow() || isMinuteThisHourTomorrow()) {
 			cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1);
 		}
 		if (this.getDaysOfWeek() != 0) {
 			setNextOccuringDay(cal);
 		}
 
 		cal.set(Calendar.HOUR_OF_DAY, alarmHours);
 		cal.set(Calendar.MINUTE, alarmMinutes);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 
 		return cal.getTimeInMillis();
 	}
 
 	private void setNextOccuringDay(Calendar cal) {
 		int currentDay = cal.get(Calendar.DAY_OF_WEEK);
 		int nextDay = getDaysToNextAlarm(currentDay);
 		if (nextDay == -1) {
 			return;
 		}
 		cal.add(Calendar.DAY_OF_YEAR, nextDay);
 	}
 
 	private int getDaysToNextAlarm(int currentDay) {
 		boolean[] days = changeToCalendar(getBooleanArrayDayOfWeek());
 		for (int i = 0; i < 7; i++) {
 			if (days[(currentDay + i) % 7]) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	private boolean[] changeToCalendar(boolean[] booleanArrayDayOfWeek) {
 		boolean[] calendarDays = new boolean[7];
 		for (int i = 0; i < calendarDays.length; i++) {
 			if (i == 0) {
 				calendarDays[i] = booleanArrayDayOfWeek[6];
 			} else {
 				calendarDays[i] = booleanArrayDayOfWeek[i - 1];
 			}
 		}
 		return calendarDays;
 	}
 
 	private boolean isMinuteThisHourTomorrow() {
 		Calendar cal = Calendar.getInstance();
 		return (cal.get(Calendar.HOUR_OF_DAY) == alarmHours)
 				&& cal.get(Calendar.MINUTE) >= alarmMinutes;
 	}
 
 	private boolean isHourTomorrow() {
 		return Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > alarmHours;
 	}
 
 	private boolean isIllegalMinutes(int minutes) {
 		return minutes > 59 || minutes < 0;
 	}
 
 	private boolean isIllegalHour(int hours) {
 		return hours > 23 || hours < 0;
 	}
 
 	public int getAlarmHours() {
 		return alarmHours;
 	}
 
 	public int getAlarmMinutes() {
 		return alarmMinutes;
 	}
 
 	@Override
 	public String toString() {
 		// Format
 		int daysLeft = getDaysToNextAlarm(Calendar.getInstance().get(
 				Calendar.DAY_OF_WEEK));
 		boolean day = daysLeft > 0;
 		boolean days = daysLeft > 1;
 		boolean hour = getHoursToAlarm() > 0;
 		boolean hours = getHoursToAlarm() > 1;
 		boolean minute = getMinutesToAlarm() > 0;
 		boolean minutes = getMinutesToAlarm() > 1;
 
 		StringBuilder strBuilder = new StringBuilder();
 
 		strBuilder.append("Alarm is set for ");
 		if (day) {
 			strBuilder.append(daysLeft + " day" + (days ? "s" : ""));
 		}
		if (day && hour) {
 			strBuilder.append(" and ");
 		}
 		if (hour) {
 			strBuilder.append(getHoursToAlarm() + " hour" + (hours ? "s" : ""));
 		}
 		if ((day || hour) && minute) {
 			strBuilder.append(" and ");
 		}
 		if (minute) {
 			strBuilder.append(getMinutesToAlarm() + " minute"
 					+ (minutes ? "s" : ""));
 		}
 		strBuilder.append(" from now.");
 
 		return strBuilder.toString();
 	}
 
 	private int getMinutesToAlarm() {
 		int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);
 		int minutesToAlarm = (alarmMinutes - currentMinute + 60) % 60;
 
 		return minutesToAlarm;
 	}
 
 	private int getHoursToAlarm() {
 		int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);
 		int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
 		int hoursToAlarm = (alarmHours - currentHour + 24) % 24;
 
 		if (getMinutesToAlarm() == 0 && hoursToAlarm == 0) {
 			return 24;
 		}
 
 		if (currentMinute > alarmMinutes) {
 			if (hoursToAlarm == 0) {
 				hoursToAlarm = 24;
 			}
 			hoursToAlarm--;
 		}
 
 		return hoursToAlarm;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	@Override
 	public int hashCode() {
 		return (int) getTimeInMilliSeconds() * 7;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null || getClass() != obj.getClass()) {
 			return false;
 		}
 		Alarm other = (Alarm) obj;
 		return this.getTimeInMilliSeconds() == other.getTimeInMilliSeconds();
 	}
 
 	public int compareTo(Alarm another) {
 		return (int) (this.getTimeInMilliSeconds() - another
 				.getTimeInMilliSeconds());
 	}
 
 	public boolean isEnabled() {
 		return enabled;
 	}
 
 	public void setEnabled(boolean enabled) {
 		this.enabled = enabled;
 	}
 
 	public String getModule() {
 		return module;
 	}
 
 	public void setModule(String module) {
 		this.module = module;
 	}
 
 	public int getVolume() {
 		return volume;
 	}
 
 	public void setVolume(int volume) {
 		this.volume = volume;
 	}
 
 	public boolean[] getBooleanArrayDayOfWeek() {
 		boolean[] days = new boolean[7];
 		for (int i = 0; i < 7; i++) {
 			days[i] = (daysOfWeek & (1 << i)) > 0;
 		}
 		return days;
 	}
 
 	public int getDaysOfWeek() {
 		return daysOfWeek;
 	}
 
 	public void setDaysOfWeek(int daysOfWeek) {
 		this.daysOfWeek = daysOfWeek;
 	}
 
 }
