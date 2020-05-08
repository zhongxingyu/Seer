 package generator;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.PriorityQueue;
 
 public class ICSWriter {
 
 	private HashMap<Character, Day> normalDays;
 	private HashMap<Character, Day> wednesDays;
 	private HashMap<Character, Day> normalDaysDST;
 	private HashMap<Character, Day> wednesDaysDST;
 	private PriorityQueue<CurrentDate> excludedElectives;
 	private PriorityQueue<CurrentDate> excludedSevenEight;
 	private PriorityQueue<CurrentDate> excludedSixth;
 	private BufferedWriter out;
 	private String filename;
 	private int startYear = 2013;
 	private CurrentDate schoolStartDate = new CurrentDate(8, 21, 2013);
 	private CurrentDate dstStartDate = new CurrentDate(11, 3, 2013);
 	private CurrentDate dstEndDate = new CurrentDate(3, 10, 2014);
 	private CurrentDate dayAfterDSTStartDate = new CurrentDate(11, 4, 2013);
 	private SchoolType school;
 
 	public ICSWriter(String filename, SchoolType s) throws IOException {
 		this.school = s;
 		DayBuilder db = null;
 		switch (s) {
 			case SIXTH:
 				db = new DayBuilder6();
 				break;
 			case SEVENEIGHT:
 				db = new DayBuilder78();
 				break;
 			case HIGH:
 				db = new DayBuilder();
 				break;
 			default:
 				break;
 		}
 
 		normalDays = db.makeNormalDays(adjustTime(800, schoolStartDate, 1));
 		wednesDays = db.makeWednesdays(adjustTime(900, schoolStartDate, 1));
 		normalDaysDST = db.makeNormalDays(adjustTime(800, dayAfterDSTStartDate, 1));
 		wednesDaysDST = db.makeWednesdays(adjustTime(900, dayAfterDSTStartDate, 1));
 
 		Excluder e = new Excluder();
 		excludedElectives = e.makeExclusionQueue("ExcludedElectives.txt");
 		excludedSevenEight = e.makeExclusionQueue("ExcludedSevenEight.txt");
 		excludedSixth = e.makeExclusionQueue("ExcludedSixth.txt");
 
 		this.filename = filename;
 	}
 
 	public int adjustTime(int time, CurrentDate today, int direction) {
 		int currentTime = time;
 		if (today.isBefore(dstStartDate) || today.isAfterOrEqual(dstEndDate)) {
 			currentTime += 600 * direction;
 		} else {
 			currentTime += 700 * direction;
 		}
 		return currentTime;
 
 	}
 
 	public boolean isWednesday(CurrentDate today) {
 		Calendar wed = Calendar.getInstance();
 		wed.clear();
 		wed.set(startYear, 0, 1, 8, 8, 8);
 		while (wed.get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY) {
 			wed.add(Calendar.DATE, 1);
 		}
 		Calendar temp = Calendar.getInstance();
 		temp.clear();
 		temp.set(today.getYear(), today.getMonth() - 1, today.getDay(), 8, 8, 8);
 		while (wed.compareTo(temp) < 0) {
 			wed.add(Calendar.DAY_OF_WEEK, 7);
 			if (wed.compareTo(temp) == 0) {
 				return true;
 			}
 		}
 		return false;
 
 	}
 
 	public void writeHeader() {
 		File old = new File(filename);
 		old.delete();
 		try {
 			out = new BufferedWriter(new FileWriter(filename, true));
 
 			out.write("BEGIN:VCALENDAR");
 			out.newLine();
 			out.write("PRODID:-//Google Inc//Google Calendar 70.9054//EN");
 			out.newLine();
 			out.write("VERSION:2.0");
 			out.newLine();
 			out.write("CALSCALE:GREGORIAN");
 			out.newLine();
 			out.write("METHOD:PUBLISH");
 			out.newLine();
 			out.write("X-WR-CALNAME:All Periods");
 			out.newLine();
 			out.write("X-WR-TIMEZONE:America/Denver");
 			out.newLine();
 			out.write("X-WR-CALDESC:");
 			out.newLine();
 			out.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public void writeFooter() {
 		try {
 			out = new BufferedWriter(new FileWriter(filename, true));
 			out.write("END:VCALENDAR");
 			out.newLine();
 			out.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private String periodName(int periodNumber, String[] periodNames, CurrentDate today, boolean sixth, int[] sixthPrefs) {
 		if(sixth) {
 			return periodNameSixth(periodNumber, sixthPrefs);
 		} else {
 			return periodNameNonSixth(periodNumber, periodNames, today);
 		}
 	}
 
 	private String periodNameSixth(int periodNumber, int[] sixthPrefs) {
 		String s = "";
 		switch (periodNumber) {
 		case Period.HOMEROOM:
 			s = "Homeroom";
 			break;
 		case Period.GEOBASEBALL:
 			s = "Geobaseball";
 			break;
 		case Period.ELECTIVES:
 			s = "Electives";
 			break;
 		case Period.LUNCH:
 			s = "Lunch";
 			break;
 		case Period.ASSEMBLY:
 			s = "Assembly";
 			break;
 		case Period.BREAK:
 			s = "Break";
 			break;
 		case Period.CLUBS:
 			s = "Clubs";
 			break;
 		case Period.LANG12:
 			switch (sixthPrefs[0]) {
 			case 1:
 			case 2:
 				s = "Language Arts / Social Studies";
 				break;
 			case 3:
 			case 4:
 				switch (sixthPrefs[1]) {
 				case 3:
 					s = "Math";
 					break;
 				case 4:
 					s = "Science";
 					break;
 				default:
 					break;
 				}
 				break;
 
 			default:
 				break;
 			}
 			break;
 		case Period.LANG34:
 			switch (sixthPrefs[0]) {
 			case 3:
 			case 4:
 				s = "Language Arts / Social Studies";
 				break;
 			case 1:
 			case 2:
 				switch (sixthPrefs[1]) {
 				case 1:
 					s = "Math";
 					break;
 				case 2:
 					s = "Science";
 					break;
 				default:
 					break;
 				}
 				break;
 
 			default:
 				break;
 			}
 			break;
 		case Period.ROT1:
 			switch (sixthPrefs[3]) {
 			case 1:
 				s = "PE";
 				break;
 			case 2:
 				s = "Latin";
 				break;
 			case 3:
 				s = "Art";
 				break;
 			case 4:
 				s = "Writing";
 				break;
 			default:
 				break;
 			}
 			break;
 		case Period.ROT2:
 			switch (sixthPrefs[3]) {
 			case 1:
 				s = "Art";
 				break;
 			case 2:
 				s = "PE";
 				break;
 			case 3:
 				s = "Latin";
 				break;
 			case 4:
 				s = "Study Hall";
 				break;
 			default:
 				break;
 			}
 			break;
 		case Period.ROT3:
 			switch (sixthPrefs[3]) {
 			case 1:
 				s = "PE";
 				break;
 			case 2:
 				s = "PE";
 				break;
 			case 3:
 				s = "Writing";
 				break;
 			case 4:
 				s = "Art";
 				break;
 			default:
 				break;
 			}
 			break;
 		case Period.ROT4:
 			switch (sixthPrefs[3]) {
 			case 1:
 				s = "Writing";
 				break;
 			case 2:
 				s = "Art";
 				break;
 			case 3:
 				s = "PE";
 				break;
 			case 4:
 				s = "Latin";
 				break;
 			default:
 				break;
 			}
 			break;
 		case Period.ROT5:
 			switch (sixthPrefs[3]) {
 			case 1:
 				s = "Art";
 				break;
 			case 2:
 				s = "Writing";
 				break;
 			case 3:
 				s = "PE";
 				break;
 			case 4:
 				s = "PE";
 				break;
 			default:
 				break;
 			}
 			break;
 		case Period.ROT6:
 			switch (sixthPrefs[3]) {
 			case 1:
 				s = "Latin";
 				break;
 			case 2:
 				s = "PE";
 				break;
 			case 3:
 				s = "Art";
 				break;
 			case 4:
 				s = "Writing";
 				break;
 			default:
 				break;
 			}
 			break;
 		case Period.ROT7:
 			switch (sixthPrefs[3]) {
 			case 1:
 				s = "PE";
 				break;
 			case 2:
 				s = "Latin";
 				break;
 			case 3:
 				s = "Study Hall";
 				break;
 			case 4:
 				s = "Art";
 				break;
 			default:
 				break;
 			}
 			break;
 		case Period.ROT8:
 			switch (sixthPrefs[3]) {
 			case 1:
 				s = "Writing";
 				break;
 			case 2:
 				s = "Art";
 				break;
 			case 3:
 				s = "Latin";
 				break;
 			case 4:
 				s = "Study Hall";
 				break;
 			default:
 				break;
 			}
 			break;
 		case Period.ROT9:
 			switch (sixthPrefs[3]) {
 			case 1:
 				s = "Study Hall";
 				break;
 			case 2:
 				s = "Writing";
 				break;
 			case 3:
 				s = "PE";
 				break;
 			case 4:
 				s = "Art";
 				break;
 			default:
 				break;
 			}
 			break;
 		case Period.ROT10:
 			switch (sixthPrefs[3]) {
 			case 1:
 				s = "Latin";
 				break;
 			case 2:
 				s = "Study Hall";
 				break;
 			case 3:
 				s = "Art";
 				break;
 			case 4:
 				s = "PE";
 				break;
 			default:
 				break;
 			}
 			break;
 		case Period.ROT11:
 			switch (sixthPrefs[3]) {
 			case 1:
 				s = "PE";
 				break;
 			case 2:
 				s = "Latin";
 				break;
 			case 3:
 				s = "Art";
 				break;
 			case 4:
 				s = "Writing";
 				break;
 			default:
 				break;
 			}
 			break;
 		case Period.ROT12:
 			switch (sixthPrefs[3]) {
 			case 1:
 				s = "Art";
 				break;
 			case 2:
 				s = "PE";
 				break;
 			case 3:
 				s = "Writing";
 				break;
 			case 4:
 				s = "Latin";
 				break;
 			default:
 				break;
 			}
 			break;
 		case Period.ROT13:
 			switch (sixthPrefs[3]) {
 			case 1:
 				s = "Writing";
 				break;
 			case 2:
 				s = "Art";
 				break;
 			case 3:
 				s = "Latin";
 				break;
 			case 4:
 				s = "PE";
 				break;
 			default:
 				break;
 			}
 			break;
 		case Period.ROT14:
 			switch (sixthPrefs[3]) {
 			case 1:
 				s = "Latin";
 				break;
 			case 2:
 				s = "WTRG";
 				break;
 			case 3:
 				s = "Study Hall";
 				break;
 			case 4:
 				s = "Art";
 				break;
 			default:
 				break;
 			}
 			break;
 		case Period.ROT15:
 			switch (sixthPrefs[3]) {
 			case 1:
 				s = "Art";
 				break;
 			case 2:
 				s = "Study Hall";
 				break;
 			case 3:
 				s = "Writing";
 				break;
 			case 4:
 				s = "PE";
 				break;
 			default:
 				break;
 			}
 			break;
 		case Period.ROT16:
 			switch (sixthPrefs[3]) {
 			case 1:
 				s = "Study Hall";
 				break;
 			case 2:
 				s = "Art";
 				break;
 			case 3:
 				s = "PE";
 				break;
 			case 4:
 				s = "Latin";
 				break;
 			default:
 				break;
 			}
 			break;	
 		default:
 			break;
 		}
 		return s;
 	}
 
 	private String periodNameNonSixth(int periodNumber, String[] periodNames, CurrentDate today) {
 		String s = "";
 		switch (periodNumber) {
 			case Period.BREAK:
 				s = "Break";
 				break;
 			case Period.ASSEMBLY:
 				s = "Assembly";
 				break;
 			case Period.CLASSMEETING:
 				s = "Class Meeting";
 				break;
 			case Period.ADVISORY:
 				s = "Advisory";
 				break;
 			case Period.CLUBS:
 				s = "Clubs";
 				break;
 			case Period.LUNCH:
 				s = "Lunch";
 				break;
 			case Period.SPORTS:
 				s = "Sports";
 				break;
 			case Period.STUDYHALL:
 				s = "Study Hall";
 				break;
 			case Period.ELECTIVES:
 				s = "Electives";
 				break;
 			case Period.COMMUNITYTIME:
 				s = communityTime(today);
 				break;
 			default:
 				s = periodNames[periodNumber - 1];
 				break;
 		}
 		return s;
 	}
 
 	public void writeDayToFile(char dayType,
 			CurrentDate today,
 			int[] periodsToInclude,
 			String[] periodNames,
 			HashMap<Character, Integer> singleBox, int[] sixthPrefs,
 			boolean includeBreaksAndLunch,
 			boolean midSchoolElective, boolean sixth) throws IOException {
 		try {
 			out = new BufferedWriter(new FileWriter(filename, true));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		HashMap<Character, Day> currentDayMap;
 		if (today.isBefore(dstStartDate) || today.isAfterOrEqual(dstEndDate)) {
 			currentDayMap = isWednesday(today) ? wednesDays : normalDays;
 		} else {
 			currentDayMap = isWednesday(today) ? wednesDaysDST : normalDaysDST;
 		}
 		Day dayToPrint = currentDayMap.get(dayType);
 		for (Period p : dayToPrint.getD()) {
 			if (periodShouldBeIncluded(p.getNumber(), periodsToInclude, includeBreaksAndLunch, midSchoolElective, today)) {
 				if (singleBox.size() < 1 || (singleBox.containsKey(dayType) && p.getNumber() == singleBox.get(dayType))) {
 					Period periodToPrint = p;
 					out.write("BEGIN:VEVENT");
 					out.newLine();
 					out.write("DTSTART:"
 							+ today.getYear()
 							+ (today.getMonth() > 9 ? today.getMonth() : ("0" + today.getMonth()))
 							+ (today.getDay() > 9 ? today.getDay() : ("0" + today.getDay()))
 							+ "T"
 							+ (periodToPrint.getStartTime() > 1000 ? periodToPrint.getStartTime() : "0"
 									+ periodToPrint.getStartTime()) + "00Z");
 					out.newLine();
 					out.write("DTEND:"
 							+ today.getYear()
 							+ (today.getMonth() > 9 ? today.getMonth() : ("0" + today.getMonth()))
 							+ (today.getDay() > 9 ? today.getDay() : ("0" + today.getDay()))
 							+ "T"
 							+ (periodToPrint.getEndTime() > 1000 ? periodToPrint.getEndTime() : "0"
 									+ periodToPrint.getEndTime()) + "00Z");
 					out.newLine();
 					out.write("DTSTAMP:20120820T172628Z");
 					out.newLine();
 					out.write("UID:");
 					out.newLine();
 					out.write("CLASS:PUBLIC");
 					out.newLine();
 					out.write("CREATED:19000101T120000Z");
 					out.newLine();
 					out.write("DESCRIPTION:");
 					out.newLine();
 					out.write("LAST-MODIFIED:20120820T172422Z");
 					out.newLine();
 					out.write("LOCATION:");
 					out.newLine();
 					out.write("SEQUENCE:0");
 					out.newLine();
 					out.write("STATUS:CONFIRMED");
 					out.newLine();
 					out.write("SUMMARY:" + periodName(periodToPrint.getNumber(), periodNames, today, sixth, sixthPrefs));
 					out.newLine();
 					out.write("TRANSP:OPAQUE");
 					out.newLine();
 					out.write("END:VEVENT");
 					out.newLine();
 				}
 			}
 		}
 		out.close();
 
 	}
 
 	private boolean periodShouldBeIncluded(int periodNumber,
 			int[] periodsToInclude,
 			boolean includeBreaksAndLunch,
 			boolean midSchoolElective,
 			CurrentDate today) {
 		boolean found = false;
 		boolean electiveInclusion = true;
 		for (int i = 0; i < periodsToInclude.length; i++) {
			if (found = periodsToInclude[i] == periodNumber )
 				break;
 		}
 		for(CurrentDate c : excludedElectives) {
 		if (c.getMonth() == today.getMonth() && c.getYear() == today.getYear() && c.getDay() == today.getDay()) {
 			electiveInclusion = false;
 		}
 		}
 		switch (school) {
 		case SEVENEIGHT:
 			for(CurrentDate c : excludedSevenEight) {
 				if (c.getMonth() == today.getMonth() && c.getYear() == today.getYear() && c.getDay() == today.getDay()) {
 					return false;
 				}
 			}
 			break;
 		case SIXTH:
 			for(CurrentDate c : excludedSixth) {
 				if (c.getMonth() == today.getMonth() && c.getYear() == today.getYear() && c.getDay() == today.getDay()) {
 					return false;
 				}
 			}
 			break;
 		default:
 			break;
 		}
 		return (((periodNumber < 0 && periodNumber > -15) && includeBreaksAndLunch) || (periodNumber == Period.ELECTIVES && midSchoolElective && electiveInclusion))
 				|| found;
 	}
 
 	private String communityTime(CurrentDate today) {
 		System.out.println(today.toString() + today.getDayOfTheWeek());
 		switch (today.getDayOfTheWeek()) {
 			case 2:
 				return "Advisory";
 			case 3:
 				return "Assembly";
 			case 4:
 				return "Class Meeting";
 			case 5:
 				return "Clubs";
 			case 6:
 				return "Assembly";
 			default:
 				return "";
 		}
 	}
 }
