 /* --------------------------------------------------------------------------
  * @author Hauke Walden
  * @created 28.06.2011 
  * Copyright 2011 by Hauke Walden 
  * All rights reserved.
  * --------------------------------------------------------------------------
  */
 
 package de.mbaaba.calendar;
 
 import java.io.FileNotFoundException;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import de.mbaaba.google.GoogleCalendar;
 import de.mbaaba.notes.NotesCalendar;
 import de.mbaaba.util.CommConfigUtil;
 import de.mbaaba.util.Logger;
 import de.mbaaba.util.PropertyFileConfigurator;
 import de.mbaaba.util.Units;
 
 /**
  * The Class Notes2GoogleExporter.
  */
 public class CalendarSyncTool {
 
 	/**
 	 * A logger for this class.
 	 */
 	private static final Logger LOG = new Logger(CalendarSyncTool.class);
 
 	/**
 	 * A formatter for dates. Used for println to the console.
 	 */
 	private static DateFormat logDateFormatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
 
 	/** Default target calendar type. */
 	public static final String defaultWriteToClassName = GoogleCalendar.class.getName();
 
 	/** Used to read configuration parameter. */
 	private PropertyFileConfigurator configurator;
 
 	/**
 	 * Stores the last known calendar entries.
 	 */
 	private FileCalendar lastKnownCalendarState;
 
 	/**
 	 * Instantiates a calendar converter.
 	 */
 	public CalendarSyncTool() {
 		configurator = new PropertyFileConfigurator("Notes2Google.properties");
 		lastKnownCalendarState = new FileCalendar();
 		lastKnownCalendarState.init(configurator);
 	}
 
 	/**
 	 * This will perform the read/filter/write loop.
 	 */
 	private void loop() {
 
 		ArrayList<CalendarEntry> oldSourceEntries = lastKnownCalendarState.readCalendarEntries(null, null);
 
 		while (true) {
 
 			try {
 
 				configurator = new PropertyFileConfigurator("Notes2Google.properties");
 				CommConfigUtil.init(configurator);
 
 				String readFromClassName = configurator.getProperty("calendar.from", NotesCalendar.class.getName());
 
 				println("Reading from calendar " + readFromClassName + " ... ");
 
 				AbstractCalendar sourceCalendar = (AbstractCalendar) Class.forName(readFromClassName).newInstance();
 				sourceCalendar.init(configurator);
 
 				long numDaysPast = configurator.getProperty("calendar.numDaysPast", 0);
 				long numDaysFuture = configurator.getProperty("calendar.numDaysFuture", 14);
 				Date startDate = new Date(System.currentTimeMillis() - Units.DAY * numDaysPast);
 				Date endDate = new Date(System.currentTimeMillis() + Units.DAY * numDaysFuture);
 
 				ArrayList<CalendarEntry> sourceEntriesUnfiltered = null;
 				try {
 					sourceEntriesUnfiltered = sourceCalendar.readCalendarEntries(startDate, endDate);
 				} catch (Exception e) {
 
 				}
 
 				// if we did not get a list of entries, something must have gone
 				// wrong. In that case, do not write anything to the target
 				// calendar
 				if (sourceEntriesUnfiltered != null) {
 					ArrayList<CalendarEntry> sourceEntries = runFilters(sourceEntriesUnfiltered);
 
 					ArrayList<CalendarEntry> newEntries = getNewEntries(sourceEntries, oldSourceEntries);
 					ArrayList<CalendarEntry> obsoleteEntries = getObsoleteEntries(sourceEntries, oldSourceEntries);
 
 					if ((newEntries.size() > 0) || (obsoleteEntries.size() > 0)) {
 						writeToTargetCalendar(newEntries, obsoleteEntries);
 						copyList(sourceEntries, oldSourceEntries);
 						saveLastKnownCalendarState(sourceEntries);
 					} else {
 						println("... no changes found.");
 					}
 				}
 
 				int repeatEach = configurator.getProperty("repeatEach", 20);
 				if (repeatEach > 0) {
 					Date now = new Date();
 					//TODO: 1: Handling of working-hours http://github.com/hwacookie/CalendarSyncTool/issues/issue/1
					while ((now.getDay() ==0) || (now.getDay() == 6) || ((now.getHours() < 8) || (now.getHours() > 18))) {
 						println("Not a working hour, sleeping for an hour!");
 						Thread.sleep(Units.HOUR * 1);
 						now = new Date();
 					}
 					int numMinutes = repeatEach;
 					println("Now sleeping for " + numMinutes + " minutes ...");
 					Thread.sleep(Units.MINUTE * numMinutes);
 				} else if (repeatEach == 0) {
 					println("Now sleeping for 10 seconds ...");
 					Thread.sleep(Units.SECOND * 10);
 				} else if (repeatEach < 0) {
 					println("Exiting ...");
 					break;
 				}
 
 			} catch (Throwable e) {
 				// catch all exceptions because even if something goes wrong
 				// while reading/writing entries, we never want to break the
 				// while-true-loop.
 				LOG.error(e.getMessage(), e);
 				printerr("Some error occured, resuming loop anyway. I'm still here! Error: " + e.getMessage());
 				println("Now sleeping for 10 seconds ...");
 				try {
 					Thread.sleep(Units.SECOND * 10);
 				} catch (InterruptedException e1) {
 					// ignore
 				}
 			}
 
 		}
 	}
 
 	/**
 	 * Saves the last known calendar state.
 	 * 
 	 * @param aEntries
 	 *            the source entries
 	 */
 	private void saveLastKnownCalendarState(ArrayList<CalendarEntry> aEntries) {
 		println("Persisting list of calendar entries.");
 		lastKnownCalendarState.deleteAll();
 		lastKnownCalendarState.putAll(aEntries);
 		lastKnownCalendarState.close();
 	}
 
 	/**
 	 * Write to target calendar.
 	 * 
 	 * @param newEntries
 	 *            the new entries
 	 * @param obsoleteEntries
 	 *            the obsolete entries
 	 * @throws InstantiationException
 	 *             the instantiation exception
 	 * @throws IllegalAccessException
 	 *             the illegal access exception
 	 * @throws ClassNotFoundException
 	 *             the class not found exception
 	 * @throws Exception
 	 *             the exception
 	 */
 	private void writeToTargetCalendar(ArrayList<CalendarEntry> newEntries, ArrayList<CalendarEntry> obsoleteEntries) throws InstantiationException, IllegalAccessException, ClassNotFoundException,
 			Exception {
 		String writeToClassName = configurator.getProperty("calendar.to", defaultWriteToClassName);
 		println("Some entries have changed, writing to calendar " + writeToClassName + ", ");
 
 		AbstractCalendar targetCalendar = (AbstractCalendar) Class.forName(writeToClassName).newInstance();
 		println("Initializing targetCalendar");
 		targetCalendar.init(configurator);
 
 		List<CalendarEntry> toBeDeletedList = new ArrayList<CalendarEntry>();
 		for (CalendarEntry calendarEntry : obsoleteEntries) {
 			if (!listHasEntryWithID(newEntries, calendarEntry)) {
 				toBeDeletedList.add(calendarEntry);
 			}
 		}
 		if (toBeDeletedList.size() > 0) {
 			println("Removing " + obsoleteEntries.size() + " entries ...");
 			targetCalendar.delete(toBeDeletedList);
 		}
 
 		println("Adding/Updating " + newEntries.size() + " entries ...");
 		targetCalendar.put(newEntries);
 
 		targetCalendar.close();
 	}
 
 	/**
 	 * The main method.
 	 * 
 	 * @param args
 	 *            the arguments
 	 * @throws Exception
 	 *             the exception
 	 */
 	public static void main(String[] args) throws Exception {
 		// if ((args.length > 0) && ("clear".equals(args[0]))) {
 		// ClearGoogle.clearGoogle();
 		// return;
 		// }
 
 		CalendarSyncTool notes2GoogleExporter = new CalendarSyncTool();
 		notes2GoogleExporter.loop();
 
 	}
 
 	/**
 	 * Runs all filters given in the configuration file.
 	 * 
 	 * These filters will either remove entries completely, or they may change
 	 * certain fields of the {@link CalendarEntry}.
 	 * 
 	 * @param aUnfilteredList
 	 *            the unfiltered list of entries
 	 * @return The list of filtered entries after the filters have been applied.
 	 */
 	private ArrayList<CalendarEntry> runFilters(ArrayList<CalendarEntry> aUnfilteredList) {
 		ArrayList<CalendarEntry> filteredEntries = new ArrayList<CalendarEntry>();
 		List<ICalendarFilter> allFilters = new ArrayList<ICalendarFilter>();
 
 		String filterScriptNames = configurator.getProperty("filters.scripts", "");
 
 		StringTokenizer tok = new StringTokenizer(filterScriptNames, ",");
 		while (tok.hasMoreTokens()) {
 			String scriptName = tok.nextToken();
 			try {
 				ScriptFilter filter = new ScriptFilter(scriptName, configurator);
 				allFilters.add(filter);
 			} catch (FileNotFoundException e) {
 				printerr("Found no script named \"" + scriptName + "\"", e);
 			}
 		}
 
 		// apply the fix location filters
 		allFilters.add(new FixLocationFilter(configurator));
 
 		for (CalendarEntry calendarEntry : aUnfilteredList) {
 			boolean skipThisEntry = false;
 
 			for (ICalendarFilter filter : allFilters) {
 				try {
 					if (!filter.passes(calendarEntry)) {
 						skipThisEntry = true;
 						break;
 					}
 				} catch (Exception e) {
 					LOG.error(e.getMessage(), e);
 					skipThisEntry = true;
 					break;
 				}
 
 			}
 
 			if (!skipThisEntry) {
 				filteredEntries.add(calendarEntry);
 			}
 		}
 		return filteredEntries;
 	}
 
 	/**
 	 * Prints a status message.
 	 * 
 	 * @param aMessage
 	 *            the string
 	 */
 	public static void println(String aMessage) {
 		System.out.println(logDateFormatter.format(new Date()) + " | " + aMessage);
 	}
 
 	/**
 	 * Prints a error message.
 	 * 
 	 * @param aErrorMessage
 	 *            the string
 	 */
 	public static void printerr(String aErrorMessage) {
 		LOG.error(aErrorMessage);
 		System.err.println(logDateFormatter.format(new Date()) + " | " + aErrorMessage);
 	}
 
 	/**
 	 * Prints a error message.
 	 * 
 	 * @param aErrorMessage
 	 *            the string
 	 */
 	public static void printerr(String aErrorMessage, Throwable e) {
 		LOG.error(aErrorMessage, e);
 		System.err.println(logDateFormatter.format(new Date()) + " | " + aErrorMessage);
 	}
 
 	/**
 	 * Copies all entries from the sourceList to the targetList.
 	 * 
 	 * @param aSourceList
 	 *            the calendar entries read
 	 * @param aTargetList
 	 *            the last list
 	 */
 	private void copyList(ArrayList<CalendarEntry> aSourceList, ArrayList<CalendarEntry> aTargetList) {
 		aTargetList.clear();
 		for (ICalendarEntry calendarEntry : aSourceList) {
 			aTargetList.add(new CalendarEntry(calendarEntry));
 		}
 	}
 
 	/**
 	 * Gets a list of new entries (that is, entries which are in the given
 	 * newList, but not in the oldList).
 	 * 
 	 * @param aNewList
 	 *            the new list
 	 * @param aOldList
 	 *            the old list
 	 * @return the new entries
 	 */
 	private ArrayList<CalendarEntry> getNewEntries(ArrayList<CalendarEntry> aNewList, ArrayList<CalendarEntry> aOldList) {
 		ArrayList<CalendarEntry> newEntries = new ArrayList<CalendarEntry>();
 		for (CalendarEntry newEntry : aNewList) {
 			if (!aOldList.contains(newEntry)) {
 				newEntries.add(newEntry);
 			}
 		}
 		return newEntries;
 	}
 
 	/**
 	 * Gets a list of obsolete entries (that is, entries which are in the given
 	 * oldList, but not in the newList).
 	 * 
 	 * @param aNewList
 	 *            the new list
 	 * @param aOldList
 	 *            the old list
 	 * @return the obsolete entries
 	 */
 	private ArrayList<CalendarEntry> getObsoleteEntries(ArrayList<CalendarEntry> aNewList, ArrayList<CalendarEntry> aOldList) {
 		ArrayList<CalendarEntry> obsoleteEntries = new ArrayList<CalendarEntry>();
 		for (CalendarEntry oldEntry : aOldList) {
 			if (!aNewList.contains(oldEntry)) {
 				obsoleteEntries.add(oldEntry);
 			}
 		}
 		return obsoleteEntries;
 	}
 
 	/**
 	 * Checks if the list has a calendar entry with the given id.
 	 * 
 	 * @param aList
 	 *            the list
 	 * @param aEntry
 	 *            the entry
 	 * @return true, if successful
 	 */
 	private boolean listHasEntryWithID(ArrayList<CalendarEntry> aList, ICalendarEntry aEntry) {
 		for (ICalendarEntry calendarEntry : aList) {
 			if (calendarEntry.getUniqueID().equals(aEntry.getUniqueID())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 }
