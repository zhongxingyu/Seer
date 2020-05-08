 package com.zerodes.exchangesync.calendarsource.google;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.joda.time.DateTimeZone;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.api.client.auth.oauth2.Credential;
 import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
 import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore;
 import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
 import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.http.javanet.NetHttpTransport;
 import com.google.api.client.json.JsonFactory;
 import com.google.api.client.json.jackson2.JacksonFactory;
 import com.google.api.client.util.DateTime;
 import com.google.api.services.calendar.Calendar;
 import com.google.api.services.calendar.CalendarScopes;
 import com.google.api.services.calendar.model.CalendarList;
 import com.google.api.services.calendar.model.CalendarListEntry;
 import com.google.api.services.calendar.model.Event;
 import com.google.api.services.calendar.model.Event.ExtendedProperties;
 import com.google.api.services.calendar.model.Event.Organizer;
 import com.google.api.services.calendar.model.Event.Reminders;
 import com.google.api.services.calendar.model.EventAttendee;
 import com.google.api.services.calendar.model.EventDateTime;
 import com.google.api.services.calendar.model.EventReminder;
 import com.google.api.services.calendar.model.Events;
 import com.zerodes.exchangesync.calendarsource.CalendarSource;
 import com.zerodes.exchangesync.dto.AppointmentDto;
 import com.zerodes.exchangesync.dto.AppointmentDto.RecurrenceType;
 import com.zerodes.exchangesync.dto.PersonDto;
 import com.zerodes.exchangesync.settings.Settings;
 
 public class GoogleCalendarSourceImpl implements CalendarSource {
 	private static final Logger LOG = LoggerFactory.getLogger(GoogleCalendarSourceImpl.class);
 
 	// Google Id and Secret from https://code.google.com/apis/console/?pli=1#project:861974414961:access
 	private static final String GOOGLE_CLIENT_ID = "861974414961.apps.googleusercontent.com";
 	private static final String GOOGLE_CLIENT_SECRET = "RsmjfTuIDbNxLU_MdPOlvgVR";
 	private static final String USER_SETTING_CALENDAR_NAME = "googleCalendarName";
 	private static final String EXT_PROPERTY_EXCHANGE_ID = "exchangeId";
 
 	private final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
 	private final JsonFactory JSON_FACTORY = new JacksonFactory();
 	private Calendar client;
 	private String calendarId;
 
 	public GoogleCalendarSourceImpl(final Settings settings) throws Exception {
 		LOG.info("Connecting to Google Calendar...");
 		Credential credential = authorize();
 		client = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
 			.setApplicationName("Exchange Sync/1.0")
 			.build();
 		calendarId = getCalendarId(settings.getUserSetting(USER_SETTING_CALENDAR_NAME));
 		LOG.info("Connected to Google Calendar.");
 	}
 
 	/** Authorizes the installed application to access user's protected data. */
 	private Credential authorize() throws Exception {
 		// set up file credential store
 		FileCredentialStore credentialStore = new FileCredentialStore(
 				new File(System.getProperty("user.home"), ".credentials/calendar.json"), JSON_FACTORY);
 		// set up authorization code flow
 		final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
 				HTTP_TRANSPORT, JSON_FACTORY, GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, Collections.singleton(CalendarScopes.CALENDAR))
 			.setCredentialStore(credentialStore)
 			.build();
 		// authorize
 		return new AuthorizationCodeInstalledApp(flow,
 				new LocalServerReceiver()).authorize("user");
 	}
 
 	private String getCalendarId(final String name) throws IOException {
 		final CalendarList feed = client.calendarList().list().execute();
 		if (feed.getItems() != null) {
 			for (final CalendarListEntry entry : feed.getItems()) {
 				if (entry.getSummary().equals(name)) {
 					return entry.getId();
 				}
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public Collection<AppointmentDto> getAllAppointments(final org.joda.time.DateTime startDate,
 			final org.joda.time.DateTime endDate) throws IOException {
 		final Collection<AppointmentDto> results = new HashSet<AppointmentDto>();
 		int page = 1;
 		LOG.info("Retrieving Google Calendar events page " + page);
 		Events feed = client.events().list(calendarId).execute();
 		while (true) {
 			if (feed.getItems() != null) {
 				for (final Event event : feed.getItems()) {
					org.joda.time.DateTime eventStartDate = convertToJodaDateTime(event.getStart());
					org.joda.time.DateTime eventEndDate = coalesce(convertToJodaDateTime(event.getEnd()), convertToJodaDateTime(event.getStart()));
					if ((eventEndDate.isAfter(startDate) || eventEndDate.isEqual(startDate)) && eventStartDate.isBefore(endDate)) {
 						results.add(convertToAppointmentDto(event));
 					}
 				}
 			}
 			String pageToken = feed.getNextPageToken();
 			if (pageToken != null && !pageToken.isEmpty()) {
 				page++;
 				LOG.info("Retrieving Google Calendar events page " + page);
 				feed = client.events().list(calendarId).setPageToken(pageToken).execute();
 			} else {
 				break;
 			}
 		}
 		return results;
 	}
 
 	private AppointmentDto convertToAppointmentDto(final Event event) {
 		final GoogleAppointmentDto result = new GoogleAppointmentDto();
 		result.setGoogleId(event.getId());
 		result.setExchangeId(event.getExtendedProperties().getPrivate().get(EXT_PROPERTY_EXCHANGE_ID));
 		result.setLastModified(convertToJodaDateTime(event.getUpdated()));
 		result.setSummary(event.getSummary());
 		result.setDescription(event.getDescription());
 		result.setStart(convertToJodaDateTime(event.getStart()));
 		result.setEnd(convertToJodaDateTime(event.getEnd()));
 		if (event.getEnd().getDateTime() != null) {
 			result.setAllDay(event.getEnd().getDateTime().isDateOnly());
 		}
 		result.setLocation(event.getLocation());
 		if (event.getOrganizer() != null) {
 			final PersonDto person = new PersonDto();
 			person.setName(event.getOrganizer().getDisplayName());
 			person.setEmail(event.getOrganizer().getEmail());
 			result.setOrganizer(person);
 		}
 		if (event.getAttendees() != null) {
 			final Set<PersonDto> attendees = new HashSet<PersonDto>();
 			for (final EventAttendee eventAttendee : event.getAttendees()) {
 				final PersonDto person = new PersonDto();
 				person.setName(eventAttendee.getDisplayName());
 				person.setEmail(eventAttendee.getEmail());
 				if (eventAttendee.getOptional() != null) {
 					person.setOptional(eventAttendee.getOptional());
 				}
 				attendees.add(person);
 			}
 			result.setAttendees(attendees);
 		}
 		if (event.getReminders() != null && event.getReminders().getOverrides() != null) {
 			EventReminder reminder = event.getReminders().getOverrides().iterator().next();
 			result.setReminderMinutesBeforeStart(reminder.getMinutes());
 		}
 		// TODO: Recurrence
 		
 		return result;
 	}
 
 	private void populateEventFromAppointmentDto(final AppointmentDto appointmentDto, final Event event) {
 		event.setSummary(appointmentDto.getSummary());
 		event.setDescription(appointmentDto.getDescription());
 		event.setStart(convertToEventDateTime(appointmentDto.getStart(), appointmentDto.isAllDay()));
 		event.setEnd(convertToEventDateTime(appointmentDto.getEnd(), appointmentDto.isAllDay()));
 		event.setLocation(appointmentDto.getLocation());
 		if (appointmentDto.getOrganizer() != null && appointmentDto.getOrganizer().getEmail() != null) {
 			final Organizer organizer = new Organizer();
 			organizer.setDisplayName(appointmentDto.getOrganizer().getName());
 			organizer.setEmail(appointmentDto.getOrganizer().getEmail());
 			event.setOrganizer(organizer);
 		}
 		if (appointmentDto.getAttendees() != null) {
 			final List<EventAttendee> attendees = new ArrayList<EventAttendee>();
 			for (final PersonDto attendee : appointmentDto.getAttendees()) {
 				if (attendee.getEmail() != null) {
 					final EventAttendee eventAttendee = new EventAttendee();
 					eventAttendee.setDisplayName(attendee.getName());
 					eventAttendee.setEmail(attendee.getEmail());
 					eventAttendee.setOptional(attendee.isOptional());
 					attendees.add(eventAttendee);
 				}
 			}
 			event.setAttendees(attendees);
 		}
 		if (appointmentDto.getReminderMinutesBeforeStart() != null) {
 			final EventReminder reminder = new EventReminder();
 			reminder.setMinutes(appointmentDto.getReminderMinutesBeforeStart());
 			reminder.setMethod("popup");
 			final Reminders reminders = new Reminders();
 			reminders.setUseDefault(false);
 			reminders.setOverrides(Collections.singletonList(reminder));
 			event.setReminders(reminders);
 		}
 		if (appointmentDto.getRecurrenceType() != null) {
 			String recurrencePattern = "RRULE:";
 			if (appointmentDto.getRecurrenceType() == RecurrenceType.DAILY) {
 				recurrencePattern = recurrencePattern + "FREQ=DAILY";
 			} else if (appointmentDto.getRecurrenceType() == RecurrenceType.WEEKLY) {
 				recurrencePattern = recurrencePattern + "FREQ=WEEKLY";
 			} else if (appointmentDto.getRecurrenceType() == RecurrenceType.MONTHLY) {
 				recurrencePattern = recurrencePattern + "FREQ=MONTHLY";
 			} else if (appointmentDto.getRecurrenceType() == RecurrenceType.YEARLY) {
 				recurrencePattern = recurrencePattern + "FREQ=YEARLY";
 			}
 			recurrencePattern = recurrencePattern + ";COUNT=" + appointmentDto.getRecurrenceCount();
 			event.setRecurrence(Collections.singletonList(recurrencePattern));
 		}
 	}
 	
 	@Override
 	public void addAppointment(final AppointmentDto appointmentDto) throws IOException {
 		final Event event = new Event();
 		Map<String, String> privateProperties = new HashMap<String, String>();
 		privateProperties.put(EXT_PROPERTY_EXCHANGE_ID, appointmentDto.getExchangeId());
 		ExtendedProperties extProperties = new ExtendedProperties();
 		extProperties.setPrivate(privateProperties);
 		event.setExtendedProperties(extProperties);
 		populateEventFromAppointmentDto(appointmentDto, event);
 
 		client.events().insert(calendarId, event).execute();
 	}
 
 	@Override
 	public void updateAppointment(final AppointmentDto appointmentDto) throws IOException {
 		GoogleAppointmentDto googleAppointmentDto = (GoogleAppointmentDto) appointmentDto;
 		Event event = client.events().get(calendarId, googleAppointmentDto.getGoogleId()).execute();
 		populateEventFromAppointmentDto(appointmentDto, event);
 		client.events().update(calendarId, event.getId(), event).execute();
 	}
 
 	@Override
 	public void deleteAppointment(final AppointmentDto appointmentDto) throws IOException {
 		GoogleAppointmentDto googleAppointmentDto = (GoogleAppointmentDto) appointmentDto;
 		client.events().delete(calendarId, googleAppointmentDto.getGoogleId()).execute();
 	}
 
 	private DateTime convertToDateTime(final org.joda.time.DateTime date) {
 		return new DateTime(date.getMillis());
 	}
 
 	private DateTime convertToDate(final org.joda.time.DateTime date) {
 		return new DateTime(true, date.getMillis(), null);
 	}
 
 	private EventDateTime convertToEventDateTime(final org.joda.time.DateTime date, final boolean isAllDay) {
 		final EventDateTime result = new EventDateTime();
 		if (isAllDay) {
 			result.setDate(convertToDate(date));
 		} else {
 			result.setDateTime(convertToDateTime(date));
 		}
 		result.setTimeZone("UTC");
 		return result;
 	}
 
 	private org.joda.time.DateTime convertToJodaDateTime(final DateTime googleTime) {
 		if (googleTime == null) {
 			return null;
 		}
 		org.joda.time.DateTime jodaDate = new org.joda.time.DateTime(googleTime.getValue(), DateTimeZone.UTC);
 		return jodaDate;
 	}
 
 	private org.joda.time.DateTime convertToJodaDateTime(final EventDateTime googleTime) {
 		org.joda.time.DateTime result;
 		if (googleTime.getDateTime() == null) {
 			result = convertToJodaDateTime(googleTime.getDate());
 		} else {
 			result = convertToJodaDateTime(googleTime.getDateTime());
 		}
 		return result;
 	}

	public static <T> T coalesce(T ...items) {
		for(T item : items) {
			if (item != null) {
				return item;
			}
		}
		return null;
	}
 }
