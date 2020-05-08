 package generator;
 
 import java.util.HashMap;
 
 public class DayBuilder78 extends DayBuilder {
 
 	private HashMap<Character, Day> dayMap;
 
 	public DayBuilder78() {
 		dayMap = new HashMap<Character, Day>();
 	}
 
 	public HashMap<Character, Day> makeNormalDays(int start) {
 		dayMap = new HashMap<Character, Day>();
 		int temp;
 		int periodLength = 60;
 		Period currentPeriod;
 		// A Day
 		char dayType = 'A';
 		Day today = new Day();
 		currentPeriod = new Period(start, temp = makeIntTime(start, periodLength), 1);
 		today.add(currentPeriod);
 		today.setDayType(dayType);
 		temp = makeIntTime(temp, 5);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), 3);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, 15), -1);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), 4);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, 40), -7); // Lunch
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), -4); // Advisory
 		today.add(currentPeriod);
 		temp = makeIntTime(temp, 5);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), 6);
 		today.add(currentPeriod);
 		temp = makeIntTime(temp, 5);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength + 20), -8); // Sports
 		today.add(currentPeriod);
 		dayMap.put(dayType, today);
 
 		// B day
 		dayType = 'B';
 		today = new Day();
 		currentPeriod = new Period(start, temp = makeIntTime(start, periodLength), 2);
 		today.add(currentPeriod);
 		today.setDayType(dayType);
 		temp = makeIntTime(temp, 5);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), 4);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, 15), -1);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), 5);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, 40), -7);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), 1);
 		today.add(currentPeriod);
 		temp = makeIntTime(temp, 5);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), -6); // Study
 																						// Hall
 		today.add(currentPeriod);
 		temp = makeIntTime(temp, 5);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength + 20), -9); // Electives
 		today.add(currentPeriod);
 		dayMap.put(dayType, today);
 
 		// C Day
 		dayType = 'C';
 		today = new Day();
 		currentPeriod = new Period(start, temp = makeIntTime(start, periodLength), 3);
 		today.add(currentPeriod);
 		today.setDayType(dayType);
 		temp = makeIntTime(temp, 5);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), 5);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, 15), -1);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), 6);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, 40), -7);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), 2);
 		today.add(currentPeriod);
 		temp = makeIntTime(temp, 5);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), 1);
 		today.add(currentPeriod);
 		temp = makeIntTime(temp, 5);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength + 20), -8);
 		today.add(currentPeriod);
 		dayMap.put(dayType, today);
 
 		// D Day
 		dayType = 'D';
 		today = new Day();
 		currentPeriod = new Period(start, temp = makeIntTime(start, periodLength), 4);
 		today.add(currentPeriod);
 		today.setDayType(dayType);
 		temp = makeIntTime(temp, 5);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), 6);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, 15), -1);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), -5); // Clubs
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, 40), -7);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), 3);
 		today.add(currentPeriod);
 		temp = makeIntTime(temp, 5);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), 2);
 		today.add(currentPeriod);
 		temp = makeIntTime(temp, 5);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength + 20), -8);
 		today.add(currentPeriod);
 		dayMap.put(dayType, today);
 
 		// E Day
 		dayType = 'E';
 		today = new Day();
 		currentPeriod = new Period(start, temp = makeIntTime(start, periodLength), 5);
 		today.add(currentPeriod);
 		today.setDayType(dayType);
 		temp = makeIntTime(temp, 5);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength / 2), -6); // Study
 																							// Hall
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength / 2), -2); // Assembly
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, 15), -1);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), 1);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, 40), -7);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), 4);
 		today.add(currentPeriod);
 		temp = makeIntTime(temp, 5);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), 3);
 		today.add(currentPeriod);
 		temp = makeIntTime(temp, 5);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength + 20), -8);
 		today.add(currentPeriod);
 		dayMap.put(dayType, today);
 
 		// F Day
 		dayType = 'F';
 		today = new Day();
 		currentPeriod = new Period(start, temp = makeIntTime(start, periodLength), 6);
 		today.add(currentPeriod);
 		today.setDayType(dayType);
 		temp = makeIntTime(temp, 5);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), 1);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, 15), -1);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), 2);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, 40), -7);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), 5);
 		today.add(currentPeriod);
 		temp = makeIntTime(temp, 5);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), 4);
 		today.add(currentPeriod);
 		temp = makeIntTime(temp, 5);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength + 20), -9);
 		today.add(currentPeriod);
 		dayMap.put(dayType, today);
 
 		// G Day
 		dayType = 'G';
 		today = new Day();
 		currentPeriod = new Period(start, temp = makeIntTime(start, periodLength / 3), -3);
 		today.add(currentPeriod);
 		today.setDayType(dayType);
		currentPeriod = new Period(start, temp = makeIntTime(start, 2 * (periodLength / 3)), -6);
 		today.add(currentPeriod);
 		temp = makeIntTime(temp, 5);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), 2);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, 15), -1);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), 3);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, 40), -7);
 		today.add(currentPeriod);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), 6);
 		today.add(currentPeriod);
 		temp = makeIntTime(temp, 5);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), 5);
 		today.add(currentPeriod);
 		temp = makeIntTime(temp, 5);
 		currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength + 20), -8);
 		today.add(currentPeriod);
 		dayMap.put(dayType, today);
 
 		return dayMap;
 
 	}
 
 	public HashMap<Character, Day> makeWednesdays(int start) {
 		dayMap = new HashMap<Character, Day>();
 		int temp;
 		int periodLength = 50;
 		for (int i = 0; i < 7; i++) {
 			char dayType = 'A';
 			Day today = new Day();
 			dayType += i;
 			Period currentPeriod = new Period(start, temp = makeIntTime(start, periodLength), (dayType % 65) % 7 + 1);
 			today.add(currentPeriod);
 			today.setDayType(dayType);
 			currentPeriod = new Period(temp, temp = makeIntTime(temp, 15), -1);
 			today.add(currentPeriod);
 			currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), (dayType % 65 + 2) % 7 + 1);
 			today.add(currentPeriod);
 			temp = makeIntTime(temp, 5);
 			currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), (dayType % 65 + 3) % 7 + 1);
 			today.add(currentPeriod);
 			currentPeriod = new Period(temp, temp = makeIntTime(temp, 40), -7);
 			today.add(currentPeriod);
 			currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), (dayType % 65 + 6) % 7 + 1);
 			today.add(currentPeriod);
 			temp = makeIntTime(temp, 5);
 			currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength), (dayType % 65 + 5) % 7 + 1);
 			today.add(currentPeriod);
 			temp = makeIntTime(temp, 5);
 			currentPeriod = new Period(temp, temp = makeIntTime(temp, periodLength + 10),
 					(dayType == 'B' || dayType == 'F') ? -9 : -8);
 			today.add(currentPeriod);
 			dayMap.put(dayType, today);
 		}
 		return dayMap;
 
 	}
 
 	private int makeIntTime(int time, int add) {
 		int temp = (time % 1000) % 100;
 		time -= temp;
 		time += ((temp + add) / 60) * 100;
 		time += ((temp + add) % 60);
 		return time;
 	}
 }
