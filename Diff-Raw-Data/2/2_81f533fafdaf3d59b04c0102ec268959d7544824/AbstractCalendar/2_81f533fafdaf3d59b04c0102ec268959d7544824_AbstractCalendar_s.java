 /* --------------------------------------------------------------------------
  * @author Hauke Walden
  * @created 28.06.2011 
  * Copyright 2011 by Hauke Walden 
  * All rights reserved.
  * --------------------------------------------------------------------------
  */
 
 package de.mbaaba.calendar;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import de.mbaaba.util.Configurator;
 
 /**
  * Abstract base class for all calendars.
  * 
  * @author walden_h1 blub
  * 
  */
 public abstract class AbstractCalendar {
 
 	/**
 	 * Initializes the calendar.
 	 * 
 	 * @param aConfigurator
 	 *            the {@link Configurator} that contains configuration
 	 *            information for this calendar (e.g. an account name, a URL, or
 	 *            a filename)
 	 * @throws Exception
 	 *             thrown if anything goes wrong during initialization.
 	 */
 	public abstract void init(Configurator aConfigurator) throws Exception;
 
 	/**
 	 * Closes the given calendar.
 	 */
 	public abstract void close();
 
 	/**
 	 * Read calendar entries from the calendar that are starting between the two
 	 * given dates.
 	 * 
 	 * @param aStartDate
 	 *            the starting date
 	 * @param aEndDate
 	 *            the end date
 	 * @return the list of calendar entries between the two dates.
 	 */
 	public abstract ArrayList<ICalendarEntry> readCalendarEntries(Date aStartDate, Date aEndDate);
 
 	/**
 	 * Adds all given calendar entries.
 	 * 
 	 * @see #put(ICalendarEntry)
 	 * @param aCalendarEntries
 	 *            the calendar entries to be added.
 	 */
 	public final void putList(List<ICalendarEntry> aCalendarEntries) {
 		for (ICalendarEntry calendarEntry : aCalendarEntries) {
 			put(calendarEntry);
 		}
 	}
 
 	/**
 	 * Adds the given entry to this calendar if it does not yet exist, or
 	 * replaces the content of the entry with the given one.
 	 * 
 	 * @param aCalendarEntry
 	 *            the calendar entry to be put into the calendar.
 	 */
 	public abstract void put(ICalendarEntry aCalendarEntry);

 	/**
 	 * Delete the given entry from the calendar.
 	 * 
 	 * @param aCalendarEntry
 	 *            the calendar entry to be deleted.
 	 */
 	public abstract void delete(ICalendarEntry aCalendarEntry);
 
 	/**
 	 * Delete all calendar entries in this calendar which are contained in the
 	 * given list.
 	 * 
 	 * @param aList
 	 *            the list of entries to be deleted.
 	 */
 	public void deleteList(List<ICalendarEntry> aCalendarEntries) {
 		for (ICalendarEntry calendarEntry : aCalendarEntries) {
 			delete(calendarEntry);
 		}
 	}
 
 }
