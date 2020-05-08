 package ndsr;
 
 import com.google.gdata.client.calendar.CalendarQuery;
 import com.google.gdata.client.calendar.CalendarService;
 import com.google.gdata.data.DateTime;
 import com.google.gdata.data.PlainTextConstruct;
 import com.google.gdata.data.calendar.CalendarEntry;
 import com.google.gdata.data.calendar.CalendarEventEntry;
 import com.google.gdata.data.calendar.CalendarEventFeed;
 import com.google.gdata.data.calendar.CalendarFeed;
 import com.google.gdata.data.calendar.HiddenProperty;
 import com.google.gdata.data.extensions.When;
 import com.google.gdata.util.AuthenticationException;
 import com.google.gdata.util.ServiceException;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Calendar;
 import java.util.List;
 import java.util.TimeZone;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class CalendarHandler {
 
 	private static final Logger log = LoggerFactory.getLogger(Configuration.class);
 
     private CalendarService myService;
     private URL feedUrl;
 	private URL ownCalendarsFeedUrl;
     private Configuration configuration;
 
     public CalendarHandler(Configuration config) {
         myService = new CalendarService("qfel13Corp-qfel13App-1");
         configuration = config;
         try {
             String user = config.getUser();
             String passwd = config.getPasswd();
             
             myService.setUserCredentials(user, passwd);
             feedUrl = new URL(config.getUrl());
 			ownCalendarsFeedUrl = new URL("https://www.google.com/calendar/feeds/default/owncalendars/full");
         } catch (AuthenticationException ex) {
             log.error("authentication", ex);
         } catch (IOException ex) {
             log.error("io exception", ex);
         }
     }
 
 	public List<CalendarEntry> getCalendars() throws IOException, ServiceException {
 		CalendarFeed resultFeed = myService.getFeed(ownCalendarsFeedUrl, CalendarFeed.class);
 		
 		return resultFeed.getEntries();
 	}
 
 	public CalendarEntry createNewCalendar(String name, String summary) throws IOException, ServiceException {
 		CalendarEntry calendar = new CalendarEntry();
 		
 		calendar.setTitle(new PlainTextConstruct(name));
 		calendar.setSummary(new PlainTextConstruct(summary));
 		calendar.setHidden(HiddenProperty.FALSE);
 
 		return myService.insert(ownCalendarsFeedUrl, calendar);
 	}
 
     public String createOrUpdate() {
         try {
             CalendarEventFeed resultFeed = getFeedByStartDate(getTodayBegin(), getTodayEnd());
 
 			CalendarEventEntry entryForUpdate = getLatestEntry(resultFeed);
 
 			if (entryForUpdate != null) {
                 log.debug("There are following events with title \"{}\":", configuration.getEventName());
                 int size = resultFeed.getEntries().size();
 
                 // DEBUG
                 for (int i = 0; i < size; i++) {
                     CalendarEventEntry entry = resultFeed.getEntries().get(i);
                     log.debug("{}: ", entry.getTitle().getPlainText());
                     for (When when : entry.getTimes()) {
                         log.debug("{} - {}", when.getStartTime(), when.getEndTime());
                     }
                 }
                 // END OF DEBUG
                 
 				// UPDATE END
 				URL editUrl = new URL(entryForUpdate.getEditLink().getHref());
				log.debug("editUrl =  {}", editUrl);
 
 				Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Warsaw"));
 				cal.add(Calendar.MINUTE, 5);
 				DateTime endTime = new DateTime(cal.getTime(), TimeZone.getTimeZone("Europe/Warsaw"));
 
 				List<When> times = entryForUpdate.getTimes();
 				if (times.size() == 1) {
 					times.get(0).setEndTime(endTime);
 				} else {
 					// ERROR
					String errorMsg = "stop(): error: more then one When object in calendar event";
					log.error(errorMsg);
					return errorMsg;
 				}
 
 				// REMINDER
 //                    int reminderMinutes = 15;
 //                    Method methodType = Method.SMS;
 //
 //                    Reminder reminder = new Reminder();
 //                    reminder.setMinutes(reminderMinutes);
 //                    reminder.setMethod(methodType);
 
 //                    entryForUpdate.getReminder().add(reminder);
 
				CalendarEventEntry updatedEntry = entryForUpdate.update();
//				CalendarEventEntry updatedEntry = (CalendarEventEntry) myService.update(editUrl, entryForUpdate);
 				String result = "Updated " + updatedEntry.getTitle().getPlainText();
 				log.debug(result);
 				return result;
             } else {
                 return createEvent(10);
             }
         } catch (IOException ex) {
            log.error("IOException: ", ex);
             return "start(): IOException: " + ex.getMessage();
         } catch (ServiceException ex) {
            log.error("ServiceException: ", ex);
             return "start(): ServiceException: " + ex.getMessage();
         }
     }
 
 	public String createNewEvent() {
         try {
 			return createEvent(0);
 		} catch (IOException ex) {
             log.error("IOException: " + ex.getMessage());
             return "start(): IOException: " + ex.getMessage();
         } catch (ServiceException ex) {
             log.error("ServiceException: " + ex.getMessage());
             return "start(): ServiceException: " + ex.getMessage();
         }
     }
 
 	private String createEvent(int minutesBeforeNow) throws IOException, ServiceException {
 		log.debug("No event today. So create one");
 
 		CalendarEventEntry myEntry = new CalendarEventEntry();
 		myEntry.setTitle(new PlainTextConstruct(configuration.getEventName()));
 
 		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Warsaw"));
 		cal.add(Calendar.MINUTE, -minutesBeforeNow);
 		log.debug("cal.getTime() = {}", cal.getTime());
 		DateTime startTime = new DateTime(cal.getTime(), TimeZone.getTimeZone("Europe/Warsaw"));
 		cal.add(Calendar.MINUTE, 10);
 		log.debug("cal.getTime() = {}", cal.getTime());
 		DateTime endTime = new DateTime(cal.getTime(), TimeZone.getTimeZone("Europe/Warsaw"));
 		When eventTimes = new When();
 		eventTimes.setStartTime(startTime);
 		eventTimes.setEndTime(endTime);
 		myEntry.addTime(eventTimes);
 		
 		// Send the request and receive the response:
 		CalendarEventEntry insertedEntry = myService.insert(feedUrl, myEntry);
 		String result = "Created " + insertedEntry.getTitle().getPlainText();
 		log.debug(result);
 
 		return result;
 	}
 
     public String getStatsStr() {
         Stats stats;
         String s;
         try {
             stats = getStats();
             s = stats.toString();
         } catch (IOException ex) {
             log.error("io exception", ex);
             s = "io exception" + ex.getMessage();
         } catch (ServiceException ex) {
             log.error("service exception", ex);
             s = "service exception" + ex.getMessage();
         }
         return s;
     }
 
     public Stats getStats() throws IOException, ServiceException {
         Stats s = new Stats();
         getTodayStats(s);
         getWeekStats(s);
         return s;
     }
 
     public Stats getTodayStats(Stats stats) throws IOException, ServiceException {
         CalendarEventFeed resultFeed = getFeedByStartDate(getTodayBegin(), getTodayEnd());
 
         long milis = 0;
         for (CalendarEventEntry entry : resultFeed.getEntries()) {
             String title = entry.getTitle().getPlainText();
             if (title.startsWith(configuration.getEventName())) {
                 List<When> times = entry.getTimes();
                 if (times.size() == 1) {
                     DateTime startTime = times.get(0).getStartTime();
                     DateTime endTime = times.get(0).getEndTime();
                     milis += endTime.getValue() - startTime.getValue();
                 } else {
                     // ERROR
                 }
             }
         }
         long sec = milis / 1000;
         long min = sec / 60;
         long hours = min / 60;
         long minutes = min % 60;
 
         stats.setToday(hours, minutes);
         
 
         return stats;
     }
 
     public Stats getWeekStats(Stats stats) throws IOException, ServiceException {
         CalendarEventFeed resultFeed = getFeedByStartDate(getWeekBegin(), getWeekEnd());
 
         long milis = 0;
         for (CalendarEventEntry entry : resultFeed.getEntries()) {
             String title = entry.getTitle().getPlainText();
             if (title.startsWith(configuration.getEventName())) {
                 List<When> times = entry.getTimes();
                 if (times.size() == 1) {
                     DateTime startTime = times.get(0).getStartTime();
                     DateTime endTime = times.get(0).getEndTime();
                     milis += endTime.getValue() - startTime.getValue();
                 } else {
                     // ERROR
                 }
             }
         }
         long sec = milis / 1000;
         long min = sec / 60;
         long hours = min / 60;
         long minutes = min % 60;
 
         stats.setWeek(hours, minutes);
         
         return stats;
     }
 
     private Calendar getWeekBegin() {
         Calendar calendar = getTodayBegin();
         int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
         calendar.add(Calendar.DATE, -(dayOfWeek - 2));
         return calendar;
     }
 
     private Calendar getWeekEnd() {
         Calendar calendar = getWeekBegin();
         calendar.add(Calendar.DATE, 7);
         return calendar;
     }
 
     private Calendar getTodayBegin() {
         Calendar calendar = Calendar.getInstance();
         
         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
 
         return calendar;
     }
 
     private Calendar getTodayEnd() {
         Calendar calendar = getTodayBegin();
         calendar.add(Calendar.HOUR, 24);
         return calendar;
     }
 
     private CalendarEventFeed getFeedByStartDate(Calendar minimum, Calendar maximum) throws IOException, ServiceException {
    	log.debug("feedUrl = {}", feedUrl);
         CalendarQuery myQuery = new CalendarQuery(feedUrl);
         myQuery.setMinimumStartTime(new DateTime(minimum.getTime(), TimeZone.getTimeZone("Europe/Warsaw")));
         myQuery.setMaximumStartTime(new DateTime(maximum.getTime(), TimeZone.getTimeZone("Europe/Warsaw")));
         CalendarEventFeed resultFeed = myService.query(myQuery, CalendarEventFeed.class);
         return resultFeed;
     }
 
 	private CalendarEventEntry getLatestEntry(CalendarEventFeed resultFeed) {
 		CalendarEventEntry latestEntry = null;
 		for (CalendarEventEntry entry : resultFeed.getEntries()) {
 			if (entry.getTitle().getPlainText().equals(configuration.getEventName())) {
 				if (latestEntry != null) {
 					if (laterThenLatest(entry, latestEntry)) {
 						latestEntry = entry;
 					}
 				} else {
 					latestEntry = entry;
 				}
 			} else {
 				log.debug("title != conf");
 			}
 		}
 		return latestEntry;
 	}
 
 	private boolean laterThenLatest(CalendarEventEntry event, CalendarEventEntry latest) {
 		DateTime eventEndTime = event.getTimes().get(0).getEndTime();
 		DateTime latestEndTime = latest.getTimes().get(0).getEndTime();
 		return eventEndTime.getValue() > latestEndTime.getValue();
 	}
 }
