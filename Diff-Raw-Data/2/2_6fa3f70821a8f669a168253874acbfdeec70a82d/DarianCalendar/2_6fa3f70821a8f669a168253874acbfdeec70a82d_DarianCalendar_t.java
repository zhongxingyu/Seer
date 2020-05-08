 package fr.mvanbesien.calendars.calendars;
 
 import java.util.Calendar;
 
 import fr.mvanbesien.calendars.dates.DarianDate;
 import fr.mvanbesien.calendars.tools.Utils;
 
 public class DarianCalendar {
 
 	private final static double SOL_LENGTH_MS = 88775244.09;
 
 	private final static double DAY_LENGTH_MS = 86400000.00;
 
 	private static final double CALENDAR_START = 2308805.7782;
 
 	private static final long DECADE_LENGTH = 6 * 669 + 4 * 668;
 
 	private static final long[] DECADE_REPARTITION = { 669, 669, 668, 669, 668, 669, 668, 669, 668, 669 };
 
 	private static final long[] YEAR_REPARTITION = { 28, 28, 28, 27, 28, 28, 28, 27, 28, 28, 28, 27, 28, 28, 28, 28 };
 
 	public DarianDate getDate() {
 		return getDate(Calendar.getInstance());
 	}
 
 	public static DarianDate getDate(Calendar calendar) {
 
 		Calendar calendarFromJulianDay = Utils.getCalendarFromJulianDay(CALENDAR_START);
 		double elapsedDays = (calendar.getTimeInMillis() - calendarFromJulianDay.getTimeInMillis()) / DAY_LENGTH_MS;
 		double elapsedSols = (elapsedDays * DAY_LENGTH_MS / SOL_LENGTH_MS);
 
 		int year = 0;
 		while (elapsedSols > DECADE_LENGTH) {
 			year += 10;
 			elapsedSols -= DECADE_LENGTH;
 			if (year % 100 == 0 && year % 500 != 0) {
 				elapsedSols++;
 			}
 		}
 
 		for (int i = 0; i < DECADE_REPARTITION.length && elapsedSols > DECADE_REPARTITION[i]; i++) {
 			year++;
 			elapsedSols -= DECADE_REPARTITION[i];
 		}
 
 		int month = 0;
 		for (int i = 0; i < YEAR_REPARTITION.length && elapsedSols > YEAR_REPARTITION[i]; i++) {
 			month++;
 			elapsedSols -= YEAR_REPARTITION[i];
 		}
 
 		DarianDate date = new DarianDate();
		date.setOrdinaryDate(0, (int) elapsedSols + 1, month, year);
 		return date;
 	}
 
 	public static void main(String[] args) {
 		System.out.println(getDate(Calendar.getInstance()));
 	}
 }
