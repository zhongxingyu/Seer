 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.PriorityQueue;
 import java.util.Scanner;
 
 public class MultiDayWriter {
 
 	private PriorityQueue<CurrentDate> daysOff;
 	private PriorityQueue<CurrentDate> daysOn;
 
 	public MultiDayWriter(String daysOffFile, CurrentDate startDate, CurrentDate endDate) {
 		daysOff = new PriorityQueue<CurrentDate>();
 		Scanner s;
 		try {
 			s = new Scanner(new File(daysOffFile));
 			while (s.hasNextLine()) {
				parseDay(s.nextLine());
 
 			}
 			s.close();
 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		makeDaysOn(startDate, endDate);
 	}
 
 	private void makeDaysOn(CurrentDate startDate, CurrentDate endDate) {
 		Calendar cal = Calendar.getInstance();
 		cal.clear();
 		cal.set(startDate.getYear(), startDate.getMonth(), startDate.getDay(), 8, 8, 8);
 		Calendar endDay = Calendar.getInstance();
 		endDay.clear();
 		endDay.set(endDate.getYear(), endDate.getMonth(), endDate.getDay(), 8, 8, 8);
 		daysOn = new PriorityQueue<CurrentDate>();
 		CurrentDate cd;
 		boolean found = false;
 		while (cal.compareTo(endDay) < 1) {
 			cd = new CurrentDate(cal.get(Calendar.MONTH), cal.get(Calendar.DATE), cal.get(Calendar.YEAR));
 			for (CurrentDate c : daysOff) {
				if (c.getMonth() == cd.getMonth() && c.getYear() == cd.getYear() && c.getDay() == cd.getDay()) {
 					found = true;
 				}
 			}
 			if (!found) {
 				daysOn.offer(cd);
 			}
 			found = false;
 			cal.add(Calendar.DATE, 1);
 		}
 
 	}
 
 	public void generateICSFile(String filename) {
 		ICSWriter writer;
 		char dayType = 'A';
 		int dayAdjust = 0;
		for (CurrentDate c : daysOn) {
			char currentDayType = (char) (dayType + dayAdjust);
			try {
				writer = new ICSWriter(filename);
 				writer.writeDayToFile(currentDayType, c);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
 			}
			dayAdjust = (dayAdjust + 1) % 6;

 		}
 	}
 
	private void parseDay(String line) {
 		CurrentDate temp = new CurrentDate();
 		String[] brokenLine = line.split(" ");
 		temp.setMonth(Integer.parseInt(brokenLine[0]));
 		temp.setDay(Integer.parseInt(brokenLine[1]));
 		temp.setYear(Integer.parseInt(brokenLine[2]));
 		daysOff.offer(temp);
 	}
 }
