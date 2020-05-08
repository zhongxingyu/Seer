 package de.mbaaba.calendar;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import de.mbaaba.util.Configurator;
 import de.mbaaba.util.Units;
 
 public class SleepUtililty {
 
 	class ConfigParameter {
 		private static final String OFFICE_HOURS_END = "officeHours.end";
 
 		private static final String OFFICE_HOURS_START = "officeHours.start";
 
 		private static final String OFFICE_HOURS_WORKDAYS = "officeHours.workdays";
 
 		private static final String REPEAT_EACH_NONOFFICEHOURS = "repeatEach.nonofficehours";
 
 		private static final String REPEAT_EACH_OFFICEHOURS = "repeatEach.officehours";
 
 		private static final int DEFAULT_OFFICE_HOURS_END = 18;
 
 		private static final int DEFAULT_OFFICE_HOURS_START = 8;
 
 		private static final int DEFAULT_REPEAT_EACH_OFFICEHOURS = 20;
 
		private static final int DEFAULT_REPEAT_EACH_NONOFFICEHOURS = 20;
 
 		private static final String DEFAULT_OFFICE_HOURS_WORKDAYS = "1 2 3 4 5";
 	}
 
 	private static final long TEN_SECONDS = Units.SECOND * 10;
 
 	private Configurator configurator;
 
 	public SleepUtililty(Configurator aConfigurator) {
 		configurator = aConfigurator;
 	}
 
 	void sleepUntilNextRun() {
 
 		int nextRunInMinutes;
 		if (isInOfficehour(System.currentTimeMillis())) {
 			nextRunInMinutes = configurator.getProperty(ConfigParameter.REPEAT_EACH_OFFICEHOURS,
 					ConfigParameter.DEFAULT_REPEAT_EACH_OFFICEHOURS);
 		} else {
 			nextRunInMinutes = configurator.getProperty(ConfigParameter.REPEAT_EACH_NONOFFICEHOURS,
 					ConfigParameter.DEFAULT_REPEAT_EACH_NONOFFICEHOURS);
 		}
 
 		// Now, wait some time until we run again
 		long lastrun = System.currentTimeMillis();
 
 		CalendarSyncTool.print("Sleeping until next run in approx. " + nextRunInMinutes + " minutes ");
 
 		// sleep-loop
 		while (true) {
 			// No matter what, always sleep for at least 10 seconds.
 			try {
 				System.out.print(".");
 				Thread.sleep(TEN_SECONDS);
 			} catch (InterruptedException e) {
 				break;
 			}
 
 			// get current time
 			long now = System.currentTimeMillis();
 
 			// how much time has passed since the last run?
 			long timePassedSinceLastRun = now - lastrun;
 
 			// are we in an office hour now?
 			if (isInOfficehour(now)) {
 				// re-read configuration parameter
 				int repeatEachDuringOfficehours = configurator.getProperty(ConfigParameter.REPEAT_EACH_OFFICEHOURS,
 						ConfigParameter.DEFAULT_REPEAT_EACH_OFFICEHOURS);
 				if (timePassedSinceLastRun > (repeatEachDuringOfficehours * Units.MINUTE)) {
 					// break the sleep-loop if it's time to run again
 					break;
 				}
 			} else {
 				// we are not in an officehour
 				// re-read configuration parameter
 				int repeatEachDuringNonOfficehours = configurator.getProperty(ConfigParameter.REPEAT_EACH_NONOFFICEHOURS,
 						ConfigParameter.DEFAULT_REPEAT_EACH_NONOFFICEHOURS);
 				if (timePassedSinceLastRun > (repeatEachDuringNonOfficehours * Units.MINUTE)) {
 					// break the sleep-loop if it's time to run again
 					break;
 				}
 			}
 		}
 		System.out.println(" uaaargh ... yawn !!!");
 
 	}
 
 	/**
 	 * Checks if the given time is within office hours.
 	 *
 	 * @param aDate the date to be checked
 	 * @return true, if the given time is within office hours.
 	 */
 	boolean isInOfficehour(long aDate) {
 		String workdays = configurator.getProperty(ConfigParameter.OFFICE_HOURS_WORKDAYS,
 				ConfigParameter.DEFAULT_OFFICE_HOURS_WORKDAYS);
 		int officeHoursStart = configurator.getProperty(ConfigParameter.OFFICE_HOURS_START,
 				ConfigParameter.DEFAULT_OFFICE_HOURS_START);
 		int officeHoursEnd = configurator.getProperty(ConfigParameter.OFFICE_HOURS_END, ConfigParameter.DEFAULT_OFFICE_HOURS_END);
 
 		Calendar now = new GregorianCalendar();
 		now.setTimeInMillis(aDate);
 		int day = now.get(Calendar.DAY_OF_WEEK);
 		int hour = now.get(Calendar.HOUR_OF_DAY);
 		return ((workdays.indexOf(String.valueOf(day)) >= 0) && (hour >= officeHoursStart) && (hour <= officeHoursEnd));
 	}
 }
