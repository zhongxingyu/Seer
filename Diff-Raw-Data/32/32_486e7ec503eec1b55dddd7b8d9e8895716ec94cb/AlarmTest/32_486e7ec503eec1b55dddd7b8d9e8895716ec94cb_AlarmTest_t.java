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
 package edu.chalmers.dat255.group09.Alarmed.test.model;
 
 import java.util.Calendar;
 
 import android.test.AndroidTestCase;
 import edu.chalmers.dat255.group09.Alarmed.model.Alarm;
 
 public class AlarmTest extends AndroidTestCase {
 
 	private final long ONE_HOUR_IN_MILLI_SECONDS = 3600000;
 	private final long ONE_MINUTE_IN_MILLI_SECONDS = 60000;
 
 	/*
	 * Since the alarm will triggered at hh:mm:00, and we are testing that only
 	 * the hh and mm will be equal. Since we are using milliseconds the delta
 	 * will be 60s = 60ms == 60000
 	 */
 
 	public void testAlarmTimeAfterFourHours() {
 		int hoursToAdd = 4;
 		int minutesToAdd = 0;
		long currentTime = System.currentTimeMillis();
		long expectedTime = getExpectedTime(hoursToAdd, minutesToAdd)
				- currentTime;
		long actualTime = getActualTime(hoursToAdd, minutesToAdd) - currentTime;
 		assertEquals(expectedTime, actualTime, 60000);
 	}
 
 	public void testAlarmTimeAfterTwelveHours() {
 		int hoursToAdd = 12;
 		int minutesToAdd = 0;
		long currentTime = System.currentTimeMillis();
		long expectedTime = getExpectedTime(hoursToAdd, minutesToAdd)
				- currentTime;
		long actualTime = getActualTime(hoursToAdd, minutesToAdd) - currentTime;
 		assertEquals(expectedTime, actualTime, 60000);
 	}
 
 	public void testAlarmTimeAfterTenHoursThrityMinutes() {
 		int hoursToAdd = 10;
 		int minutesToAdd = 30;
		long currentTime = System.currentTimeMillis();
		long expectedTime = getExpectedTime(hoursToAdd, minutesToAdd)
				- currentTime;
		long actualTime = getActualTime(hoursToAdd, minutesToAdd) - currentTime;

 		assertEquals(expectedTime, actualTime, 60000);
 	}
 
 	public void testAlarmTimeAfterTwentyHoursFiftyMinutes() {
 
 		int minutesToAdd = 52;
 		int hoursToAdd = 20;
		long currentTime = System.currentTimeMillis();
		long expectedTime = getExpectedTime(hoursToAdd, minutesToAdd)
				- currentTime;
		long actualTime = getActualTime(hoursToAdd, minutesToAdd) - currentTime;
 		assertEquals(expectedTime, actualTime, 60000);
 	}
 
 	private long getActualTime(int hoursToAdd, int minutesToAdd) {
 
 		Calendar cal = Calendar.getInstance();
 
 		int currentTime = cal.get(Calendar.HOUR_OF_DAY);
 		int currentMinute = cal.get(Calendar.MINUTE);
 
 		if (minutesToAdd + currentMinute >= 60) {
 			hoursToAdd++;
 		}
 
 		int hours = (currentTime + hoursToAdd) % 24;
 		int minutes = (currentMinute + minutesToAdd) % 60;
 
 		return new Alarm(hours, minutes, 0).getTimeInMilliSeconds();
 	}
 
 	private long getExpectedTime(int hoursToAdd, int minutesToAdd) {
 		long hoursToAlarm = hoursToAdd * ONE_HOUR_IN_MILLI_SECONDS;
 		long minutesToAlarm = minutesToAdd * ONE_MINUTE_IN_MILLI_SECONDS;
 
 		long expectedTime = System.currentTimeMillis();
 		expectedTime += hoursToAlarm;
 		expectedTime += minutesToAlarm;
 
 		return expectedTime;
 	}
 
 	/*
 	 * The toString() will return a different format depending on how long it's
 	 * left. The format has 8 different formats.
 	 */
 
 	private Alarm alarmTimeFromNow(int minute, int hour) {
 		Calendar cal = Calendar.getInstance();
 		int minuteToTrigger = cal.get(Calendar.MINUTE) + minute;
 		int hourToTrigger = cal.get(Calendar.HOUR_OF_DAY) + hour;
 		if (minuteToTrigger >= 60) {
 			hourToTrigger++;
 		}
 		return new Alarm((hourToTrigger % 24), (minuteToTrigger % 60), 0);
 	}
 
 	public void testOneMinuteToString() {
 		Alarm at = alarmTimeFromNow(1, 0);
 		String toMatch = "Alarm is set for 1 minute from now.";
 		assertEquals(at.toString(), toMatch);
 	}
 
 	public void testThirtyMinuteToString() {
 		Alarm at = alarmTimeFromNow(30, 0);
 		String toMatch = "Alarm is set for 30 minutes from now.";
 		assertEquals(at.toString(), toMatch);
 	}
 
 	public void testOneHourToString() {
 		Alarm at = alarmTimeFromNow(0, 1);
 		String toMatch = "Alarm is set for 1 hour from now.";
 		assertEquals(at.toString(), toMatch);
 	}
 
 	public void testFiveHoursToString() {
 		Alarm at = alarmTimeFromNow(0, 5);
 		String toMatch = "Alarm is set for 5 hours from now.";
 		assertEquals(at.toString(), toMatch);
 	}
 
 	public void testOneHourOneMinuteToString() {
 		Alarm at = alarmTimeFromNow(1, 1);
 		String toMatch = "Alarm is set for 1 hour and 1 minute from now.";
 		assertEquals(at.toString(), toMatch);
 	}
 
 	public void testOneHourThirtyMinuteToString() {
 		Alarm at = alarmTimeFromNow(30, 1);
 		String toMatch = "Alarm is set for 1 hour and 30 minutes from now.";
 		assertEquals(at.toString(), toMatch);
 	}
 
 	public void testFiveHourOneMinuteToString() {
 		Alarm at = alarmTimeFromNow(1, 5);
 		String toMatch = "Alarm is set for 5 hours and 1 minute from now.";
 		assertEquals(at.toString(), toMatch);
 	}
 
 	public void testFiveHourThirtyMinuteToString() {
 		Alarm at = alarmTimeFromNow(30, 5);
 		String toMatch = "Alarm is set for 5 hours and 30 minutes from now.";
 		assertEquals(at.toString(), toMatch);
 	}
 
 	public void testTwentyThreeHourThirtyMinuteToString() {
 		Alarm at = alarmTimeFromNow(30, 23);
 		String toMatch = "Alarm is set for 23 hours and 30 minutes from now.";
 		assertEquals(at.toString(), toMatch);
 	}
 
 	public void testTwentyFourHourToString() {
 		Alarm at = alarmTimeFromNow(0, 24);
 		String toMatch = "Alarm is set for 24 hours from now.";
 		assertEquals(at.toString(), toMatch);
 	}
 
 	public void testIllegalHourInput() {
 		try {
 			new Alarm(24, 23, 0);
 			fail("Should generate an illegalargumentexpection");
 		} catch (IllegalArgumentException e) {
 			assertTrue(true);
 		}
 	}
 
 	public void testNegativeHourInput() {
 		try {
 			new Alarm(-3, 23, 0);
 			fail("Should generate an illegalargumentexpection");
 		} catch (IllegalArgumentException e) {
 			assertTrue(true);
 		}
 
 	}
 
 	public void testIllegalMinuteInput() {
 		try {
 			new Alarm(0, 60, 0);
 			fail("Should generate an illegalargumentexpection");
 		} catch (IllegalArgumentException e) {
 			assertTrue(true);
 		}
 
 	}
 
 	public void testNegativeMinuteInput() {
 		try {
 			new Alarm(22, -60, 0);
 			fail("Should generate an illegalargumentexpection");
 		} catch (IllegalArgumentException e) {
 			assertTrue(true);
 		}
 
 	}
 
 	public void testNegativeHourMinuteInput() {
 		try {
 			new Alarm(-1, -2, 0);
 			fail("Should generate an illegalargumentexpection");
 		} catch (IllegalArgumentException e) {
 			assertTrue(true);
 		}
 
 	}
 
 	public void testIllegalHourMinuteInput() {
 		try {
 			new Alarm(24, 60, 0);
 			fail("Should generate an illegalargumentexpection");
 		} catch (IllegalArgumentException e) {
 			assertTrue(true);
 		}
 	}
 
 	public void testGetID() {
 		Alarm alarm = new Alarm(20, 20, 1);
 		int actual = 1;
 		assertEquals(alarm.getId(), actual);
 	}
 
 	public void testGetAlarmHours() {
 		Alarm alarm = new Alarm(20, 20, 1);
 		int actual = 20;
 		assertEquals(alarm.getAlarmHours(), actual);
 	}
 
 	public void testGetAlarmMinutes() {
 		Alarm alarm = new Alarm(20, 20, 1);
 		int actual = 20;
 		assertEquals(alarm.getAlarmMinutes(), actual);
 	}
 
 	public void testHashCode() {
 		Alarm alarm = new Alarm(20, 20, 1);
 		Alarm otherAlarm = new Alarm(21, 20, 2);
 
 		assertFalse(alarm.hashCode() == otherAlarm.hashCode());
 
 		otherAlarm = new Alarm(20, 20, 2);
 		assertTrue(alarm.hashCode() == otherAlarm.hashCode());
 	}
 
 	public void testEquals() {
 
 		Alarm alarm = new Alarm(20, 20, 1);
 		Alarm otherAlarm = new Alarm(21, 20, 2);
 
 		// // Testing for self refrence and null
 		assertTrue(alarm.equals(alarm));
 		assertFalse(alarm.equals(null));
 
 		assertFalse(alarm.equals(otherAlarm));
 
 		otherAlarm = new Alarm(20, 20, 2);
 		assertTrue(alarm.equals(otherAlarm));
 	}
 
 	public void testCompareTo() {
 		int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
 
 		// Sets the test to be set on different times depending on the current
 		// time
 		int hour = currentHour > 12 ? 6 : 18;
 		int minute = 20;
 		Alarm alarm = new Alarm(hour, minute, 1);
 		Alarm otherAlarm = new Alarm(hour + 1, minute, 1);
 
 		assertTrue(alarm.compareTo(otherAlarm) < 0);
 
 		otherAlarm = new Alarm(hour, minute, 2);
 
 		assertTrue(alarm.compareTo(otherAlarm) == 0);
 
 		otherAlarm = new Alarm(hour - 1, minute - 1, 2);
 
 		assertTrue(alarm.compareTo(otherAlarm) > 0);
 	}
 
 	public void testEnabled() {
 
 		Alarm alarm = new Alarm(20, 20, 1);
 
 		assertTrue(alarm.isEnabled());
 
 		alarm.setEnabled(false);
 
 		assertFalse(alarm.isEnabled());
 
 		alarm.setEnabled(true);
 
 		assertTrue(alarm.isEnabled());
 	}
 }
