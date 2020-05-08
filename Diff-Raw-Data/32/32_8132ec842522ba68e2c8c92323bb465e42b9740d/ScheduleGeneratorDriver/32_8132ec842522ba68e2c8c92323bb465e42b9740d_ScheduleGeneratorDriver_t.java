 package generator;
 
 import java.util.HashMap;
 
 public class ScheduleGeneratorDriver {
 
 	public ScheduleGeneratorDriver() {
 		CurrentDate startDate = new CurrentDate(8, 21, 2012);
 		CurrentDate endDate = new CurrentDate(5, 24, 2013);
		MultiDayWriter writer = new MultiDayWriter("excluded.txt", startDate, endDate, startDate, endDate);
 		// true = upper school schedule false = middle school schedule
 		boolean school = false;
 		// sets whether to put breaks, student life, and lunch on the calendar
 		boolean includeBreaksAndLunch = false;
 		// If this has entries it will only generate specific period on specific
 		// days
 		HashMap<Character, Integer> singleBox = new HashMap<Character, Integer>();
 		// singleBox.put('G', 3);
 		// singleBox.put('E', 7);
 		// Specifies which periods will show up at all (if a period appears in
 		// singleBox but not here, it will not be on the schedule
 		int[] periodsToInclude = { 2, 5, 6 };
 		// Specifies the names of all the periods
 		String[] periodNames = { "Period 1", "Period 2", "Technology Department Meeting", "Period 4", "Period 5",
 				"Period 6", "Middle School Robotics" };
 		// specifies the name of the output file
 		writer.generateICSFile("test.txt", periodsToInclude, periodNames, school, singleBox, includeBreaksAndLunch);
 
 	}
 
 	public ScheduleGeneratorDriver(ScheduleDataStorage data) {
		CurrentDate startDate = new CurrentDate(8, 21, 2012);
		CurrentDate endDate = new CurrentDate(5, 24, 2013);
		MultiDayWriter writer = new MultiDayWriter("excluded.txt", startDate, endDate, data.getStartDate(),
				data.getEndDate());
 		writer.generateICSFile(data.getFilename(), data.getPeriodsToInclude(), data.getPeriodNames(), data.isSchool(),
 				data.getSingleBox(), data.isIncludeBreaksAndLunch());
 	}
 
 }
