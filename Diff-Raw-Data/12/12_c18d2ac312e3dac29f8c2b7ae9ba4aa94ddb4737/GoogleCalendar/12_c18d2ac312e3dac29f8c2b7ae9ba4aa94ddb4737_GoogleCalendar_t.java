 /* --------------------------------------------------------------------------
  * @author Hauke Walden
  * @created 28.06.2011 
  * Copyright 2011 by Hauke Walden 
  * All rights reserved.
  * --------------------------------------------------------------------------
  */
 
 package de.mbaaba.google;
 
 import java.io.IOException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.google.gdata.client.calendar.CalendarQuery;
 import com.google.gdata.client.calendar.CalendarService;
 import com.google.gdata.data.DateTime;
 import com.google.gdata.data.ILink;
 import com.google.gdata.data.Link;
 import com.google.gdata.data.PlainTextConstruct;
 import com.google.gdata.data.batch.BatchOperationType;
 import com.google.gdata.data.batch.BatchStatus;
 import com.google.gdata.data.batch.BatchUtils;
 import com.google.gdata.data.calendar.CalendarEventEntry;
 import com.google.gdata.data.calendar.CalendarEventFeed;
 import com.google.gdata.data.calendar.EventWho;
 import com.google.gdata.data.extensions.Email;
 import com.google.gdata.data.extensions.ExtendedProperty;
 import com.google.gdata.data.extensions.FamilyName;
 import com.google.gdata.data.extensions.FullName;
 import com.google.gdata.data.extensions.GivenName;
 import com.google.gdata.data.extensions.PhoneNumber;
 import com.google.gdata.data.extensions.PhoneNumber.Rel;
 import com.google.gdata.data.extensions.Recurrence;
 import com.google.gdata.data.extensions.When;
 import com.google.gdata.data.extensions.Where;
 import com.google.gdata.data.extensions.Who;
 import com.google.gdata.util.ServiceException;
 
 import de.mbaaba.calendar.AbstractCalendar;
 import de.mbaaba.calendar.ICalendarEntry;
 import de.mbaaba.calendar.OutputManager;
 import de.mbaaba.calendar.Person;
 import de.mbaaba.util.Configurator;
 import de.mbaaba.util.Units;
 
 /**
  * The Class GoogleCalendar allows to access events within a calendar hosted at google.
  */
 public class GoogleCalendar extends AbstractCalendar {
 	private static final String NEWLINE = "\r\n";
 
 	private static final String NOTES_ID = "notes-id";
 
 	private CalendarService calendarService;
 
 	private URL feedUrl;
 
 	private CalendarEventEntry getByNotesID(String aNotesId) throws ServiceException, IOException {
 		final CalendarQuery myQuery = new CalendarQuery(feedUrl);
 		myQuery.setExtendedPropertyQuery(new CalendarQuery.ExtendedPropertyMatch[]{new CalendarQuery.ExtendedPropertyMatch(
 				NOTES_ID, aNotesId)});
 
 		final CalendarEventFeed resultFeed = this.calendarService.getFeed(myQuery, CalendarEventFeed.class);
 		if (resultFeed.getEntries().size() > 0) {
 			if (resultFeed.getEntries().size() > 1) {
 				OutputManager.printerr("Oops: More than one result for NOTES-ID=" + aNotesId + ", using first entry !");
 			}
 			final CalendarEventEntry entry = resultFeed.getEntries().get(0);
 			return entry;
 		}
 		return null;
 	}
 
 	private ArrayList<ICalendarEntry> dateRangeQuery(DateTime aStartTime, DateTime aEndTime) throws ServiceException, IOException {
 		final CalendarQuery myQuery = new CalendarQuery(feedUrl);
 		myQuery.setMinimumStartTime(aStartTime);
 		myQuery.setMaximumStartTime(aEndTime);
 		final CalendarEventFeed resultFeed = this.calendarService.query(myQuery, CalendarEventFeed.class);
 
 		final ArrayList<ICalendarEntry> res = new ArrayList<ICalendarEntry>();
 		for (int i = 0; i < resultFeed.getEntries().size(); i++) {
 			final CalendarEventEntry entry = resultFeed.getEntries().get(i);
 			final GoogleCalendarEntry googleCalendarEntry = new GoogleCalendarEntry(entry);
 			res.add(googleCalendarEntry);
 		}
 		return res;
 	}
 
 	private CalendarEventEntry createEvent(ICalendarEntry aCalendarEntry) throws ServiceException {
 		if (aCalendarEntry.getStartDates() == null) {
 			OutputManager.println("Ooops: no start date set: " + aCalendarEntry.getUniqueID());
 			return null;
 		}
 		if (aCalendarEntry.getEndDates() == null) {
 			OutputManager.println("Ooops: no end date set: " + aCalendarEntry.getUniqueID());
 			return null;
 		}
 
 		final CalendarEventEntry googleCalendarEventEntry = createGoogleEvent(aCalendarEntry);
 
 		addExtendedProperty(googleCalendarEventEntry, NOTES_ID, aCalendarEntry.getUniqueID());
 
 		return googleCalendarEventEntry;
 	}
 
 	private CalendarEventEntry createGoogleEvent(ICalendarEntry aCalendarEntry) {
 		final CalendarEventEntry googleCalendarEventEntry = new CalendarEventEntry();
 
 		updateGoogleCalendarEventEntry(aCalendarEntry, googleCalendarEventEntry);
 		return googleCalendarEventEntry;
 	}
 
 	private void updateGoogleCalendarEventEntry(ICalendarEntry aCalendarEntry, CalendarEventEntry aGoogleCalendarEventEntry) {
 		aGoogleCalendarEventEntry.setId(aCalendarEntry.getUniqueID());
 		aGoogleCalendarEventEntry.setTitle(new PlainTextConstruct(aCalendarEntry.getSubject()));
 		aGoogleCalendarEventEntry.setContent(new PlainTextConstruct(aCalendarEntry.getBody()));
 		aGoogleCalendarEventEntry.setQuickAdd(false);
 		aGoogleCalendarEventEntry.setWebContent(null);
 
 		if (aCalendarEntry.getLocation() != null) {
 			final Where where = new Where(Where.Rel.EVENT, "Address", aCalendarEntry.getLocation());
 			aGoogleCalendarEventEntry.addLocation(where);
 		}
 		if (aCalendarEntry.getRoom() != null) {
 			final Where where = new Where(Where.Rel.EVENT_ALTERNATE, "Room", aCalendarEntry.getRoom());
 			aGoogleCalendarEventEntry.addLocation(where);
 		}
 
 		aGoogleCalendarEventEntry.getTimes().clear();
 
 		final int numDates = aCalendarEntry.getStartDates().size();
 		
 		// if number of dates is more than 1 so handle recurrence
 		// if number of dates is 1 so handle single date event
 		if (numDates == 1) {
 			final String pattern = "yyyy-MM-dd'T'HH:mm:ss";
 			final SimpleDateFormat sdf = new SimpleDateFormat(pattern);
 			sdf.setTimeZone(aCalendarEntry.getTimezone());
 
 			final Date start = aCalendarEntry.getStartDates().get(0);
 			final Date end = aCalendarEntry.getEndDates().get(0);
 			final boolean allDayEvent = isAllDayEvent(start, end);
 			
 			DateTime startTime = DateTime.parseDateTime(sdf.format(start));  // Time value is irrelevant 
 			if (allDayEvent) {
 				startTime.setDateOnly(true);
 			} else {
 				startTime.setDateOnly(false);
 			}
 			DateTime endTime = DateTime.parseDateTime(sdf.format(end));  // Time value is irrelevant 
 			if (allDayEvent) {
 				endTime.setDateOnly(true);
 			} else {
 				endTime.setDateOnly(false);
 			}
 			When eventTimes = new When();
 			eventTimes.setStartTime(startTime);
 			eventTimes.setEndTime(endTime);
 			aGoogleCalendarEventEntry.addTime(eventTimes);
 		} else if (numDates > 1) {
 			final String pattern = "yyyyMMdd'T'HHmmss";
 			final SimpleDateFormat sdf = new SimpleDateFormat(pattern);
 			sdf.setTimeZone(aCalendarEntry.getTimezone());
 
 			final Recurrence rr = new Recurrence();
 			String rrS = "";
 			for (int ctr = 0; ctr < numDates; ctr++) {
 				final Date start = aCalendarEntry.getStartDates().get(ctr);
 				final Date end = aCalendarEntry.getEndDates().get(ctr);
 
 				if (ctr == 0) {
 					rrS = "DTSTART;TZID=\"W. Europe\":" + sdf.format(start) + NEWLINE + "DTEND;TZID=\"W. Europe\":" + sdf.format(end)
 							+ NEWLINE + "TRANSP:OPAQUE" + NEWLINE + "RDATE;VALUE=PERIOD:";
 				}
 
 				rrS = rrS + sdf.format(start) + "Z/" + sdf.format(end) + "Z,";
 			}
 			rrS = rrS.substring(0, rrS.length() - 1);
 			if (aCalendarEntry.getLastModified() != null) {
 				rrS = rrS + NEWLINE + "DTSTAMP:" + sdf.format(aCalendarEntry.getLastModified());
 			}
 
 			rr.setValue(rrS);
 			aGoogleCalendarEventEntry.setRecurrence(rr);
 		}
 
 		final List<Person> attendees = aCalendarEntry.getAttendees();
 		for (final Person person : attendees) {
 			final EventWho participant = createParticipant(person, Who.Rel.EVENT_ATTENDEE);
 			aGoogleCalendarEventEntry.addParticipant(participant);
 		}
 
 		final EventWho organizedBy = createParticipant(aCalendarEntry.getChair(), Who.Rel.EVENT_ORGANIZER);
 		aGoogleCalendarEventEntry.addParticipant(organizedBy);
 
 	}
 
 	/**
 	 * Gets <code>true</code> if more then 23h and 30m between start end else <code>false</code>.
 	 * @param pStart start date
 	 * @param pEnd end date
 	 * @return <code>true</code> if more then 23h and 30m between start end else <code>false</code>
 	 */
 	private boolean isAllDayEvent(Date pStart, Date pEnd) {
		return pEnd.getTime() - pStart.getTime() > Units.HOUR * 15 + Units.MINUTE * 30;
 	}
 
 	private EventWho createParticipant(Person aPerson, String aRelation) {
 		final EventWho participant = new EventWho();
 
 		if ((aPerson.getFirstName() != null) && (aPerson.getLastName() != null)) {
 			final FullName fullName = new FullName();
 			fullName.setValue(aPerson.getFirstName() + " " + aPerson.getLastName());
 			participant.setExtension(fullName);
 		}
 
 		if (aPerson.getPhoneJob() != null) {
 			final PhoneNumber phoneNumberExtension = new PhoneNumber();
 			phoneNumberExtension.setPhoneNumber(aPerson.getPhoneJob());
 			phoneNumberExtension.setRel(Rel.COMPANY_MAIN);
 			participant.addRepeatingExtension(phoneNumberExtension);
 		}
 
 		if (aPerson.getPhoneMobile() != null) {
 			final PhoneNumber mobileNumberExtension = new PhoneNumber();
 			mobileNumberExtension.setPhoneNumber(aPerson.getPhoneMobile());
 			mobileNumberExtension.setRel(Rel.MOBILE);
 			participant.addRepeatingExtension(mobileNumberExtension);
 		}
 
 		if (aPerson.getINetAdress() != null) {
 			final Email emailExtension = new Email();
 			emailExtension.setAddress(aPerson.getINetAdress());
 			participant.setExtension(emailExtension);
 		}
 
 		if (aPerson.getFirstName() != null) {
 			final GivenName givenName = new GivenName(aPerson.getFirstName(), "");
 			participant.setExtension(givenName);
 		}
 
 		if (aPerson.getLastName() != null) {
 			final FamilyName familyName = new FamilyName(aPerson.getLastName(), "");
 			participant.setExtension(familyName);
 		}
 
 		if (aRelation != null) {
 			participant.setRel(aRelation);
 		}
 
 		if (aPerson.getPrettyMailAdress() != null) {
 			participant.setEmail(aPerson.getPrettyMailAdress());
 		}
 
 		return participant;
 	}
 
 	private static void addExtendedProperty(CalendarEventEntry aEntry, String aName, String aValue) {
 		final ExtendedProperty property = new ExtendedProperty();
 		property.setName(aName);
 		property.setValue(aValue);
 
 		aEntry.addExtension(property);
 	}
 
 	@Override
 	public void deleteList(List<ICalendarEntry> aEntriesToDelete) {
 		final ArrayList<CalendarEventEntry> eventsToDelete = new ArrayList<CalendarEventEntry>();
 		for (final ICalendarEntry calendarEntry : aEntriesToDelete) {
 			try {
 				final CalendarEventEntry eventByNotesID = getByNotesID(calendarEntry.getUniqueID());
 				if (eventByNotesID != null) {
 					eventsToDelete.add(eventByNotesID);
 				}
 			} catch (final ServiceException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (final IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		if (eventsToDelete.size() > 0) {
 			deleteIntern(eventsToDelete);
 		}
 	}
 
 	private void deleteIntern(List<CalendarEventEntry> aEventsToDelete) {
 		final CalendarEventFeed batchRequest = new CalendarEventFeed();
 		for (final CalendarEventEntry toDelete : aEventsToDelete) {
 			if (toDelete == null) {
 				throw new RuntimeException("Null Entry????");
 			}
 			if (toDelete.getId() != null) {
 				BatchUtils.setBatchId(toDelete, String.valueOf("DEL_" + toDelete.getId()));
 				BatchUtils.setBatchOperationType(toDelete, BatchOperationType.DELETE);
 				batchRequest.getEntries().add(toDelete);
 			} else {
 				OutputManager.printerr("Ooops! Entry without ID cannot be deleted.");
 			}
 		}
 
 		boolean isSuccess = false;
 		try {
 			final CalendarEventFeed feed = this.calendarService.getFeed(feedUrl, CalendarEventFeed.class);
			final Link batchLink = feed.getLink(Link.Rel.FEED_BATCH, ILink.Type.ATOM);
 			final URL batchUrl = new URL(batchLink.getHref());
 
 			final CalendarEventFeed batchResponse = this.calendarService.batch(batchUrl, batchRequest);
 
 			isSuccess = true;
 			for (final CalendarEventEntry entry : batchResponse.getEntries()) {
 				final String batchId = BatchUtils.getBatchId(entry);
 				if (!BatchUtils.isSuccess(entry)) {
 					isSuccess = false;
 					final BatchStatus status = BatchUtils.getBatchStatus(entry);
 					OutputManager.println("Delete of " + batchId + " failed: " + status.getReason());
 				} else {
 					OutputManager.println("Deleted " + batchId + ".");
 				}
 			}
 		} catch (final IOException e) {
 			e.printStackTrace();
 		} catch (final ServiceException e) {
 			e.printStackTrace();
 		}
 		if (isSuccess) {
 			OutputManager.println("Successfully deleted all events via batch request.");
 		}
 	}
 
 	@Override
 	public void init(Configurator aConfigurator) throws Exception {
 		final String username = aConfigurator.getProperty("google.user", "");
 		final String password = aConfigurator.getProperty("google.pwd", "");
 		feedUrl = new URL(aConfigurator.getProperty("google.url", ""));
 
 		OutputManager.println("Using URL " + feedUrl);
 		this.calendarService = new CalendarService("exampleCo-exampleApp-1");
 		this.calendarService.setUserCredentials(username, password);
 		this.calendarService.useSsl();
 	}
 
 	@Override
 	public ArrayList<ICalendarEntry> readCalendarEntries(Date aStartDate, Date aEndDate) {
 		try {
 			return dateRangeQuery(new DateTime(aStartDate), new DateTime(aEndDate));
 		} catch (final ServiceException e) {
 			e.printStackTrace();
 		} catch (final IOException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 	
 	/**
 	 * Adds all given calendar entries.
 	 * 
 	 * @param aCalendarEntriesthe calendar entries to be added or updated.
 	 */
 	@Override
 	public void putList(List<ICalendarEntry> aCalendarEntries) {
 		// New feed all entries will be add to
 		CalendarEventFeed batchRequest = new CalendarEventFeed();
 		int batchIndex = 1;
 		for (final ICalendarEntry calendarEntry : aCalendarEntries) {
 			try {
 				if (calendarEntry.getUniqueID() == null) {
 					OutputManager.printerr("Entry has no unique ID!");
 					continue;
 				}
 				final CalendarEventEntry googleEvent = getByNotesID(calendarEntry.getUniqueID());
 				if (googleEvent != null) {
 					OutputManager.println("Updating entry " + calendarEntry.getShortString() + ".");
 					updateGoogleCalendarEventEntry(calendarEntry, googleEvent);
 					BatchUtils.setBatchId(googleEvent, String.valueOf(batchIndex));
 					BatchUtils.setBatchOperationType(googleEvent, BatchOperationType.UPDATE);
 					batchRequest.getEntries().add(googleEvent);
 					batchIndex++;
 				} else {
 					OutputManager.println("Adding entry " + calendarEntry.getShortString() + ".");
 					CalendarEventEntry entryToAdd = createEvent(calendarEntry);
 					if (entryToAdd != null) {
 						BatchUtils.setBatchId(entryToAdd, String.valueOf(batchIndex));
 						BatchUtils.setBatchOperationType(entryToAdd, BatchOperationType.INSERT);
 						batchRequest.getEntries().add(entryToAdd);
 						batchIndex++;
 					}
 				}
 			} catch (final Exception e) {
 				//skipp element if exception occured
 				OutputManager.println(e.getMessage());
 				continue;
 			}
 		}
 		
		try {
 			// Get some events to operate on.
			CalendarEventFeed feed = this.calendarService.getFeed(feedUrl, CalendarEventFeed.class);
 	
 			// Get the batch link URL and send the batch request there.
 			Link batchLink = feed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);
 			CalendarEventFeed batchResponse = this.calendarService.batch(new URL(batchLink.getHref()), batchRequest);
 	
 			// Ensure that all the operations were successful.
 			boolean isSuccess = true;
 			for (CalendarEventEntry entry : batchResponse.getEntries()) {
 			  String batchId = BatchUtils.getBatchId(entry);
 			  if (!BatchUtils.isSuccess(entry)) {
 			    isSuccess = false;
 			    BatchStatus status = BatchUtils.getBatchStatus(entry);
 			    OutputManager.println("Batchentry with Id: " + batchId + " failed (" + status.getReason() + ") " + status.getContent());
 			  }
 			}
 			if (isSuccess) {
 				OutputManager.println("Successfully created/updated all events via batch request.");
 			} else {
 				OutputManager.println("Not all events created/updated sucessfully via batch request.");
 			}
 		} catch (Exception e) {
 			throw new RuntimeException("Error during batch update: " + e.getMessage(), e);
 		}
 	}
 
 	@Override
 	public void put(ICalendarEntry aCalendarEntry) {
 		try {
 			if (aCalendarEntry.getUniqueID() == null) {
 				OutputManager.printerr("Entry has no unique ID!");
 				return;
 			}
 			final CalendarEventEntry googleEvent = getByNotesID(aCalendarEntry.getUniqueID());
 			if (googleEvent != null) {
 				OutputManager.println("Updating entry " + aCalendarEntry.getShortString() + ".");
 				updateGoogleCalendarEventEntry(aCalendarEntry, googleEvent);
 				googleEvent.delete();
 				createEvent(aCalendarEntry);
 			} else {
 				OutputManager.println("Adding entry " + aCalendarEntry.getShortString() + ".");
 				createEvent(aCalendarEntry);
 			}
 		} catch (final Exception e) {
 			throw new RuntimeException("Cannot access Google Calendar.", e);
 		}
 	}
 
 	@Override
 	public void close() {
 	}
 
 	// public void deleteAllEntries() {
 	// try {
 	// CalendarEventFeed resultFeed = (CalendarEventFeed)
 	// this.calendarService.getFeed(feedUrl, CalendarEventFeed.class);
 	// List<CalendarEventEntry> entries = resultFeed.getEntries();
 	// delete(entries);
 	// } catch (IOException e) {
 	// e.printStackTrace();
 	// } catch (ServiceException e) {
 	// e.printStackTrace();
 	// }
 	// }
 
 	@Override
 	public void delete(ICalendarEntry aParamCalendarEntry) {
 		final List<ICalendarEntry> list = new ArrayList<ICalendarEntry>();
 		list.add(aParamCalendarEntry);
 		deleteList(list);
 	}
 
 	public void deleteAllEntries() {
 		try {
 			final CalendarEventFeed resultFeed = this.calendarService.getFeed(feedUrl, CalendarEventFeed.class);
 			final List<CalendarEventEntry> entries = resultFeed.getEntries();
 			deleteIntern(entries);
 		} catch (final IOException e) {
 			e.printStackTrace();
 		} catch (final ServiceException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
 
 // DateTime startTime = new
 // DateTime(aCalendarEntry.getStartDates().get(ctr),
 // TimeZone.getTimeZone("GMT-1:00"));
 // DateTime endTime = new
 // DateTime(aCalendarEntry.getEndDates().get(ctr),
 // TimeZone.getTimeZone("GMT-1:00"));
 // When eventTime = new When();
 // eventTime.setStartTime(startTime);
 // eventTime.setEndTime(endTime);
 // aGoogleCalendarEventEntry.addTime(eventTime);
