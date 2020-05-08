 package cz.pavel.uugooglesync;
 
 import java.awt.AWTException;
 import java.awt.Image;
 import java.awt.SystemTray;
 import java.awt.TrayIcon;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.log4j.Logger;
 
 import com.google.api.services.calendar.model.Event;
 
 import cz.pavel.uugooglesync.google.GoogleManager;
 import cz.pavel.uugooglesync.utils.CalendarUtils;
 import cz.pavel.uugooglesync.utils.Configuration;
 import cz.pavel.uugooglesync.utils.LogUtils;
 import cz.pavel.uugooglesync.uu.UUEvent;
 import cz.pavel.uugooglesync.uu.UUManager;
 
 public class UUGoogleSync {
 
 	private static final Logger log = LogUtils.getLogger();
 	
 	
 	
 	private UUManager uuManager;
 	private GoogleManager googleManager;
 	
 	private TrayIcon trayIcon; 
 	
 	private static final int MIN_SYNC_INTERVAL = 60;
 	private static final int MIN_SYNC_INTERVAL_AFTER_FAILURE = 10;
 	private static final String DEFAULT_SYSTRAY_MESSAGE = "UUGoogleSync";
 	private static final int ONE_MINUTE_SLEEP = 60 * 1000;
 	
 	private long lastSuccessfulSync = 0;
 	private long lastFailedSync = 0;
 	private int lastInserted, lastUpdated, lastDeleted;
 	
 	
 	private Map<String, UUEvent> loadUUEvents() throws ClientProtocolException, IOException {
 		uuManager = new UUManager();
 		return uuManager.getEvents(CalendarUtils.getStartDate(), CalendarUtils.getEndDate());
 	}
 	
 	private Map<String, Event> loadGoogleEvents() throws IOException {
 		googleManager = new GoogleManager();
 		return googleManager.getEvents(CalendarUtils.getStartDate(), CalendarUtils.getEndDateSunday());
 	}
 	
 	
 	private void fillGoogleEvent(Event googleEvent, UUEvent uuEvent) {
 		googleEvent.setStatus(uuEvent.isConfirmed() ? GoogleManager.STATUS_CONFIRMED : GoogleManager.STATUS_TENTATIVE);
 		googleEvent.setSummary(uuEvent.getSummary());
 		googleEvent.setLocation(uuEvent.getPlace());
 		googleEvent.setStart(CalendarUtils.calendarToGoogle(uuEvent.getStart()));
 		googleEvent.setEnd(CalendarUtils.calendarToGoogle(uuEvent.getEnd()));
 		googleManager.setId(googleEvent, uuEvent.getId());
 	}
 	
 	private static String coalesce(String data) {
 		return data == null ? "" : data;
 	}
 	
 	private boolean compareAndUpdateEvents(UUEvent uuEvent, Event googleEvent) throws IOException {
 		if (uuEvent.isConfirmed() && !GoogleManager.STATUS_CONFIRMED.equals(googleEvent.getStatus()) ||
 		    !uuEvent.getSummary().equals(coalesce(googleEvent.getSummary())) ||
 		    !uuEvent.getPlace().equals(coalesce(googleEvent.getLocation())) ||
 		    !uuEvent.getStart().equals(CalendarUtils.googleToCalendar(googleEvent.getStart())) ||
 		    !uuEvent.getEnd().equals(CalendarUtils.googleToCalendar(googleEvent.getEnd()))) {
 			// update
 			fillGoogleEvent(googleEvent, uuEvent);
 			googleManager.updateEvent(googleEvent);
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private void compareAndSynchronize(Map<String, UUEvent> uuEvents, Map<String, Event> googleEvents) throws IOException {
 		// first check events in both calendars
 		List<String> idsToRemove = new ArrayList<String>();
 		int updated = 0;
 		int inserted = 0;
 		int deleted = 0;
 		
 		for (Map.Entry<String, UUEvent> uuEventEntry : uuEvents.entrySet()) {
 			if (googleEvents.get(uuEventEntry.getKey()) != null) {
 				// compare and possibly update
 				if (compareAndUpdateEvents(uuEventEntry.getValue(), googleEvents.get(uuEventEntry.getKey()))) {
 					updated++;
 				}
 				idsToRemove.add(uuEventEntry.getKey());
 				googleEvents.remove(uuEventEntry.getKey());
 			}
 		}
 		
 		for (String id : idsToRemove) {
 			uuEvents.remove(id);
 		}
 		
 		// insert new items
 		for (Map.Entry<String, UUEvent> uuEventEntry : uuEvents.entrySet()) {
 			Event googleEvent = new Event();
 			fillGoogleEvent(googleEvent, uuEventEntry.getValue());
 			googleManager.insertEvent(googleEvent);
 			inserted++;
 		}
 		
 		// delete unused items
 		for (Map.Entry<String, Event> googleEventEntry : googleEvents.entrySet()) {
 			googleManager.deleteEvent(googleEventEntry.getValue().getId());
 			deleted++;
 		}
 		lastInserted = inserted;
 		lastUpdated = updated;
 		lastDeleted = deleted;
 		
 		log.info("Updated: " + updated + " Inserted: " + inserted + " Deleted: " + deleted);
 	}
 	
 	private void setSysTrayIcon() throws IOException, AWTException {
 		if (SystemTray.isSupported()) {
 			Image image = ImageIO.read(UUGoogleSync.class.getResourceAsStream("/icon.png"));
 			SystemTray tray = SystemTray.getSystemTray();
 			trayIcon = new TrayIcon(image, DEFAULT_SYSTRAY_MESSAGE);
 			trayIcon.setImageAutoSize(true);
 			tray.add(trayIcon);
 		}
 	}
 	
 	private void setSysTrayTooltip(String text) {
 		if (trayIcon != null) {
 			trayIcon.setToolTip(text);
 		}
 	}
 	
 	private void updateTooltip() {
 		String tooltipMessage = DEFAULT_SYSTRAY_MESSAGE;
 		if (lastFailedSync > 0) {
 			tooltipMessage += "\nPoslední synchronizace selhala. Čas: " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(lastFailedSync));
 		}
 		if (lastSuccessfulSync > 0) {
 			tooltipMessage += "\nPoslední úspěšná synchronizace: " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(lastSuccessfulSync));
 			// only add detailed statistics, if there was no failure
 			if (lastFailedSync == 0) {
				tooltipMessage += "\nVytvořeno: " + lastInserted + "\nUpraveno: " + lastUpdated + "\nSmazáno: " + lastDeleted;
 			}
 			
 		}
 		setSysTrayTooltip(tooltipMessage);
 	}
 	
 	public void synchronize() throws ClientProtocolException, IOException, AWTException {
 		Configuration.readProperties();
 		int syncInterval = Configuration.getInt(Configuration.Parameters.SYNC_INTERVAL, MIN_SYNC_INTERVAL);
 		if (syncInterval < MIN_SYNC_INTERVAL) {
 			syncInterval = MIN_SYNC_INTERVAL;
 			log.warn("Sync interval increased to " + syncInterval);
 		}
 		syncInterval *= 60 * 1000;
 
 		int syncIntervalAfterFailure = Configuration.getInt(Configuration.Parameters.SYNC_INTERVAL_AFTER_FAILURE, MIN_SYNC_INTERVAL_AFTER_FAILURE);
 		if (syncIntervalAfterFailure < MIN_SYNC_INTERVAL_AFTER_FAILURE) {
 			syncIntervalAfterFailure = MIN_SYNC_INTERVAL_AFTER_FAILURE;
 			log.warn("Sync interval after failure increased to " + syncIntervalAfterFailure);
 		}
 		syncIntervalAfterFailure *= 60 * 1000;
 
 		// set icon
 		setSysTrayIcon();
 		updateTooltip();
 		
 		while (true) {
 			log.info("Starting sync");
 			long start = System.currentTimeMillis();
 			long lastSyncCompleteTime = System.currentTimeMillis();
 			long timeToSleep = 0;
 			try {
 				Map<String, Event> googleEvents = loadGoogleEvents();
 				Map<String, UUEvent> uuEvents = loadUUEvents();
 				compareAndSynchronize(uuEvents, googleEvents);
 				lastSuccessfulSync = lastSyncCompleteTime = System.currentTimeMillis();
 				lastFailedSync = 0;
 				timeToSleep = syncInterval;
 				log.info("Sync completed in " + ((lastSyncCompleteTime - start + 500) / 1000) + " s");
 			} catch (Exception e) {
 				lastFailedSync = System.currentTimeMillis();
 				timeToSleep = syncIntervalAfterFailure;
 				log.error("Sync failed in " + ((lastFailedSync - start + 500) / 1000) + " s", e);
 			}
 			
 			while (System.currentTimeMillis() - lastSyncCompleteTime < timeToSleep) {
 				updateTooltip();
 				
 				try {
 					Thread.sleep(ONE_MINUTE_SLEEP);
 				} catch (InterruptedException e) {
 					log.error("Thread sleep interrupted", e);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		try {
 			new UUGoogleSync().synchronize();
 		} catch (Exception e) {
 			log.error("Error when running synchronization. Application will now exit.", e);
 		}
 	}
 
 }
