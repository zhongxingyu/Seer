 package org.exoplatform.extension.exchange.listener;
 
 import java.net.URI;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import microsoft.exchange.webservices.data.EventType;
 import microsoft.exchange.webservices.data.ExchangeCredentials;
 import microsoft.exchange.webservices.data.ExchangeService;
 import microsoft.exchange.webservices.data.ExchangeVersion;
 import microsoft.exchange.webservices.data.Folder;
 import microsoft.exchange.webservices.data.FolderEvent;
 import microsoft.exchange.webservices.data.FolderId;
 import microsoft.exchange.webservices.data.GetEventsResults;
 import microsoft.exchange.webservices.data.ItemEvent;
 import microsoft.exchange.webservices.data.PullSubscription;
 import microsoft.exchange.webservices.data.WebCredentials;
 import microsoft.exchange.webservices.data.WellKnownFolderName;
 
 import org.exoplatform.calendar.service.Calendar;
 import org.exoplatform.calendar.service.CalendarEvent;
 import org.exoplatform.calendar.service.CalendarService;
 import org.exoplatform.container.xml.InitParams;
 import org.exoplatform.extension.exchange.service.CorrespondenceService;
 import org.exoplatform.extension.exchange.service.ExchangeStorageService;
 import org.exoplatform.extension.exchange.service.ExoStorageService;
 import org.exoplatform.extension.exchange.service.IntegrationService;
 import org.exoplatform.services.log.ExoLogger;
 import org.exoplatform.services.log.Log;
 import org.exoplatform.services.organization.OrganizationService;
 import org.exoplatform.services.security.ConversationState;
 import org.exoplatform.services.security.Identity;
 import org.exoplatform.services.security.IdentityConstants;
 import org.exoplatform.services.security.IdentityRegistry;
 import org.picocontainer.Startable;
 
 /**
  * 
  * Service to listen to exchange events. Used to synchronize eXo User calendar
  * state with exchange User calendar in real time. This service is used by two
  * modules: LoginModule to register User subscription to exchange events and a
  * LogoutListener that will be used to
  * 
  * @author Boubaker KHANFIR
  * 
  * 
  */
 public class IntegrationListener implements Startable {
 
   private static final Log LOG = ExoLogger.getLogger(IntegrationListener.class);
 
   private static final String EXCHANGE_SERVER_URL_PARAM_NAME = "exchange.ews.url";
   private static final String EXCHANGE_DOMAIN_PARAM_NAME = "exchange.domain";
   private static final String EXCHANGE_LISTENER_SCHEDULER_DELAY_NAME = "exchange.scheduler.delay";
   private static final String EXCHANGE_SYNCHRONIZE_ALL = "exchange.synchronize.all.folders";
   private static final String EXCHANGE_DELETE_CALENDAR_ON_UNSYNC = "exchange.delete.calendar.on.unsync";
 
   public static short diffTimeZone = 0;
 
   private static long threadIndex = 0;
   private static int schedulerDelayInSeconds = 0;
 
   private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(10);
   private final Map<String, ScheduledFuture<?>> futures = new HashMap<String, ScheduledFuture<?>>();
 
   private final ExoStorageService exoStorageService;
   private final ExchangeStorageService exchangeStorageService;
   private final CorrespondenceService correspondenceService;
   private final OrganizationService organizationService;
   private final CalendarService calendarService;
   private final IdentityRegistry identityRegistry;
 
   public String exchangeServerURL = null;
   public String exchangeDomain = null;
 
   private boolean synchronizeAllExchangeFolders = false;
   private boolean deleteExoCalendarOnUnsync = false;
 
   public IntegrationListener(OrganizationService organizationService, CalendarService calendarService, ExoStorageService exoStorageService, ExchangeStorageService exchangeStorageService,
       CorrespondenceService correspondenceService, IdentityRegistry identityRegistry, InitParams params) {
     this.exoStorageService = exoStorageService;
     this.exchangeStorageService = exchangeStorageService;
     this.correspondenceService = correspondenceService;
     this.identityRegistry = identityRegistry;
     this.organizationService = organizationService;
     this.calendarService = calendarService;
 
     if (params.containsKey(EXCHANGE_SERVER_URL_PARAM_NAME) && !params.getValueParam(EXCHANGE_SERVER_URL_PARAM_NAME).getValue().isEmpty()) {
       exchangeServerURL = params.getValueParam(EXCHANGE_SERVER_URL_PARAM_NAME).getValue();
     } else {
       LOG.warn("Echange Synchronization Service: init-param " + EXCHANGE_SERVER_URL_PARAM_NAME + "is not set.");
     }
     if (params.containsKey(EXCHANGE_DOMAIN_PARAM_NAME) && !params.getValueParam(EXCHANGE_DOMAIN_PARAM_NAME).getValue().isEmpty()) {
       exchangeDomain = params.getValueParam(EXCHANGE_DOMAIN_PARAM_NAME).getValue();
     } else {
       LOG.warn("Echange Synchronization Service: init-param " + EXCHANGE_DOMAIN_PARAM_NAME + "is not set.");
     }
     if (params.containsKey(EXCHANGE_LISTENER_SCHEDULER_DELAY_NAME)) {
       String schedulerDelayInSecondsString = params.getValueParam(EXCHANGE_LISTENER_SCHEDULER_DELAY_NAME).getValue();
       schedulerDelayInSeconds = Integer.valueOf(schedulerDelayInSecondsString);
     }
     if (schedulerDelayInSeconds < 10) {
       LOG.warn("Echange Synchronization Service: init-param " + EXCHANGE_LISTENER_SCHEDULER_DELAY_NAME + "is not correctly set. Use default: 30.");
       schedulerDelayInSeconds = 30;
     }
     if (params.containsKey(EXCHANGE_SYNCHRONIZE_ALL)) {
       String deleteExoCalendarOnUnsyncString = params.getValueParam(EXCHANGE_SYNCHRONIZE_ALL).getValue();
       if (deleteExoCalendarOnUnsyncString != null && deleteExoCalendarOnUnsyncString.equals("true")) {
         deleteExoCalendarOnUnsync = true;
       }
     }
     if (params.containsKey(EXCHANGE_DELETE_CALENDAR_ON_UNSYNC)) {
       String exchangeSynchronizeAllString = params.getValueParam(EXCHANGE_DELETE_CALENDAR_ON_UNSYNC).getValue();
       if (exchangeSynchronizeAllString != null && exchangeSynchronizeAllString.equals("true")) {
         synchronizeAllExchangeFolders = true;
       }
     }
 
     // Exchange system dates are saved using UTC timezone independing of User
     // Calendar timezone, so we have to get the diff with eXo Server TimeZone
     // and Exchange to make search queries
     diffTimeZone = getTimeZoneDiffWithUTC();
   }
 
   @Override
   public void start() {
     LOG.info("Echange Synchronization Service: Successfully started.");
   }
 
   @Override
   public void stop() {
     scheduledExecutor.shutdownNow();
   }
 
   /**
    * Register User with Exchange services.
    * 
    * @param username
    * @param password
    */
   public void userLoggedIn(final String username, final String password) throws Exception {
     String exchangeStoredUsername = IntegrationService.getUserArrtibute(organizationService, username, IntegrationService.USER_EXCHANGE_USERNAME_ATTRIBUTE);
     if (exchangeStoredUsername != null && !exchangeStoredUsername.isEmpty()) {
       String exchangeStoredServerName = IntegrationService.getUserArrtibute(organizationService, username, IntegrationService.USER_EXCHANGE_SERVER_URL_ATTRIBUTE);
       String exchangeStoredDomainName = IntegrationService.getUserArrtibute(organizationService, username, IntegrationService.USER_EXCHANGE_SERVER_DOMAIN_ATTRIBUTE);
       String exchangeStoredPassword = IntegrationService.getUserArrtibute(organizationService, username, IntegrationService.USER_EXCHANGE_PASSWORD_ATTRIBUTE);
      userLoggedIn(exchangeStoredUsername, exchangeStoredPassword, exchangeStoredDomainName, exchangeStoredServerName);
     } else if (exchangeDomain != null && exchangeServerURL != null) {
       if (LOG.isTraceEnabled()) {
         LOG.trace("Exchange Synchronization Service: User '" + username + "' have not yet set parameters, use default Exchange server settings.");
       }
      userLoggedIn(username, password, exchangeDomain, exchangeServerURL);
     } else {
       LOG.warn("Exchange Service is unvailable, please set parameters.");
     }
   }
 
   /**
    * 
    * Register User with Exchange services.
    * 
    * @param username
    * @param password
    * @param exchangeDomain
    * @param exchangeServerURL
    */
  public void userLoggedIn(final String username, final String password, String exchangeDomain, String exchangeServerURL) {
     try {
       Identity identity = identityRegistry.getIdentity(username);
       if (identity == null || identity.getUserId().equals(IdentityConstants.ANONIM)) {
         throw new IllegalStateException("Identity of user '" + username + "' not found.");
       }
 
       // Close other tasks if already exists, this can happens when user is
       // still logged in in other browser
       closeTaskIfExists(username);
 
       // Scheduled task: listen the changes made on MS Exchange Calendar
      Thread schedulerCommand = new ExchangeIntegrationTask(identity, password, exchangeDomain, exchangeServerURL);
       ScheduledFuture<?> future = scheduledExecutor.scheduleWithFixedDelay(schedulerCommand, 10, schedulerDelayInSeconds, TimeUnit.SECONDS);
 
       // Add future task to the map to destroy thread when the user logout
       futures.put(username, future);
 
       LOG.info("User '" + username + "' logged in, exchange synchronization task started.");
     } catch (Exception e) {
       LOG.warn("Exchange integration error for user '" + username + "' : " + e.getMessage());
       if (LOG.isTraceEnabled() || LOG.isDebugEnabled()) {
         LOG.trace("Error while initializing user integration with exchange: ", e);
       }
     }
   }
 
   /**
    * Unregister User from Exchange services.
    * 
    * @param username
    */
   public void userLoggedOut(String username) {
     closeTaskIfExists(username);
   }
 
   private void closeTaskIfExists(String username) {
     ScheduledFuture<?> future = futures.get(username);
     if (future != null) {
       future.cancel(true);
       IntegrationService integrationService = IntegrationService.getInstance(username);
       if (integrationService != null) {
         try {
           integrationService.removeInstance();
         } catch (Throwable e) {
           // Nothing to do, just log this.
           LOG.error(e);
         }
       }
       LOG.info("Exchange synchronization task stopped for User '" + username + "'.");
     }
   }
 
   private short getTimeZoneDiffWithUTC() {
     short diffTimeZone = 0;
     Date date = new Date();
     SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
     dateFormat.setTimeZone(TimeZone.getDefault());
     String dateTimeInOriginalTimeZone = dateFormat.format(date);
     dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
     String dateTimeInUTCTimeZone = dateFormat.format(date);
 
     try {
       long timeInOriginalTimeZone = dateFormat.parse(dateTimeInOriginalTimeZone).getTime();
       long timeInUTCTimeZone = dateFormat.parse(dateTimeInUTCTimeZone).getTime();
       diffTimeZone = (short) ((timeInUTCTimeZone - timeInOriginalTimeZone) / 60000);
     } catch (Exception e) {
       LOG.error("Error while calculating difference between UTC Timezone and current one.");
     }
 
     return diffTimeZone;
   }
 
   /**
    * 
    * Thread used to synchronize Exchange Calendar with eXo Calendar
    * 
    */
   protected class ExchangeIntegrationTask extends Thread {
     private IntegrationService integrationService;
     private List<FolderId> calendarFolderIds = new ArrayList<FolderId>();
     private PullSubscription subscription = null;
     private String username;
     private ConversationState state;
     private boolean firstSynchronization;
 
    public ExchangeIntegrationTask(Identity identity, String password, String exchangeDomain, String exchangeServerURL) throws Exception {
       super("ExchangeIntegrationTask-" + (threadIndex++));
       this.username = identity.getUserId();
       this.firstSynchronization = true;
 
       ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2, TimeZone.getDefault());
       service.setTimeout(20000);
      ExchangeCredentials credentials = new WebCredentials(username + "@" + exchangeDomain, password);
       service.setCredentials(credentials);
       service.setUrl(new URI(exchangeServerURL));
 
       integrationService = new IntegrationService(organizationService, calendarService, exoStorageService, exchangeStorageService, correspondenceService, service, username);
 
       // Set current identity visible in this Thread
       state = new ConversationState(identity);
       ConversationState.setCurrent(state);
 
       // First call to the service, this may fail because of wrong
       // credentials
       if (synchronizeAllExchangeFolders) {
         calendarFolderIds = exchangeStorageService.getAllExchangeCalendars(service);
       } else {
         // Test connection
         Folder folder = integrationService.getExchangeCalendar(FolderId.getFolderIdFromWellKnownFolderName(WellKnownFolderName.Calendar));
         if (folder != null) {
           integrationService.setSynchronizationStarted();
           calendarFolderIds = integrationService.getSynchronizedExchangeCalendars();
           integrationService.setSynchronizationStopped();
         } else {
           throw new RuntimeException("Error while authenticating user '" + username + "' to exchange, please make sure you are connected to the correct URL with correct credentials.");
         }
       }
     }
 
     @Override
     public void run() {
       waitOtherTasks();
       try {
         integrationService.setSynchronizationStarted();
 
         ConversationState.setCurrent(state);
 
         // Verify Exchange folders state with Exo Calendars state
         List<String> updatedExoEventIDs = integrationService.synchronizeExchangeFolderState(calendarFolderIds, synchronizeAllExchangeFolders, deleteExoCalendarOnUnsync);
         if (calendarFolderIds.isEmpty()) {
           return;
         }
         if (updatedExoEventIDs == null) {
           updatedExoEventIDs = new ArrayList<String>();
         }
         Date lastSyncDate = integrationService.getUserLastCheckDate();
         // This is used once, when user login
         if (firstSynchronization) {
           LOG.info("run first synchronization for user: " + username);
           // Verify modifications made on folders
           synchronizeByModificationDate(lastSyncDate, updatedExoEventIDs);
           this.firstSynchronization = false;
           // Begin catching events from Exchange after first synchronization
           newSubscription();
         } else {
           LOG.info("run scheduled synchronization for user: " + username);
           // This is used in a scheduled task when the user session still alive
           GetEventsResults events;
           try {
             events = subscription.getEvents();
           } catch (Exception e) {
             LOG.warn("Subscription seems timed out, retry. Original cause: " + e.getMessage() + "");
             newSubscription();
             events = subscription.getEvents();
           }
           if (synchronizeAllExchangeFolders) {
             synchronizeExchangeFolders(events, updatedExoEventIDs);
           }
           synchronizeExchangeApointments(events, updatedExoEventIDs);
           synchronizeByModificationDate(lastSyncDate, updatedExoEventIDs);
           // Renew subcription to manage new events
           newSubscription();
         }
 
         // Update date of last check in a user profile attribute
         long checkTime = java.util.Calendar.getInstance().getTimeInMillis();
         integrationService.setUserLastCheckDate(checkTime);
 
         LOG.info("Synchronization completed.");
       } catch (Exception e) {
         LOG.error("Error while synchronizing calndar entries.", e);
       } finally {
         integrationService.setSynchronizationStopped();
       }
     }
 
     private void waitOtherTasks() {
       int i = 0;
       while (integrationService.isSynchronizationStarted() && i < 30) {
         LOG.info("Exchange integration is in use, scheduled job will wait until synchronization is finished for user:'" + username + "'.");
         try {
           Thread.sleep(1000);
         } catch (Exception e) {
           LOG.warn(e.getMessage());
         }
         i++;
       }
     }
 
     private void synchronizeByModificationDate(Date lastSyncDate, List<String> updatedExoEventIDs) throws Exception {
       // synchronize eXo Calendar with Exchange
       for (FolderId folderId : calendarFolderIds) {
         Calendar calendar = integrationService.getUserCalendarByExchangeFolderId(folderId);
         if (calendar == null || lastSyncDate == null) {
           integrationService.synchronizeFullCalendar(folderId);
         } else {
           integrationService.synchronizeModificationsOfCalendar(folderId, lastSyncDate, updatedExoEventIDs, diffTimeZone);
         }
       }
     }
 
     private void synchronizeExchangeApointments(GetEventsResults events, List<String> updatedExoEventIDs) throws Exception {
       // loop through Appointment events
       Iterable<ItemEvent> itemEvents = events.getItemEvents();
       if (itemEvents.iterator().hasNext()) {
         List<String> itemIds = new ArrayList<String>();
         for (ItemEvent itemEvent : itemEvents) {
           if (itemIds.contains(itemEvent.getItemId().getUniqueId())) {
             continue;
           }
           itemIds.add(itemEvent.getItemId().getUniqueId());
           List<CalendarEvent> updatedEvents = integrationService.createOrUpdateOrDelete(itemEvent);
           if (updatedEvents != null && !updatedEvents.isEmpty() && updatedExoEventIDs != null) {
             for (CalendarEvent calendarEvent : updatedEvents) {
               updatedExoEventIDs.add(calendarEvent.getId());
             }
           }
         }
       }
     }
 
     private void synchronizeExchangeFolders(GetEventsResults events, List<String> updatedExoEventIDs) throws Exception {
       // If Calendar Folders was modified
       if (events.getFolderEvents() != null && events.getFolderEvents().iterator().hasNext()) {
         Iterator<FolderEvent> iterator = events.getFolderEvents().iterator();
         while (iterator.hasNext()) {
           FolderEvent folderEvent = (FolderEvent) iterator.next();
           if (folderEvent.getEventType().equals(EventType.Created) || folderEvent.getEventType().equals(EventType.Modified)) {
             if (!integrationService.isCalendarPresentInExo(folderEvent.getFolderId())) {
               List<String> updatedEventIDs = integrationService.synchronizeFullCalendar(folderEvent.getFolderId());
               updatedExoEventIDs.addAll(updatedEventIDs);
               if (!updatedEventIDs.isEmpty() && !calendarFolderIds.contains(folderEvent.getFolderId())) {
                 calendarFolderIds.add(folderEvent.getFolderId());
               }
             }
           } else if (folderEvent.getEventType().equals(EventType.Deleted)) {
             boolean deleted = integrationService.deleteExoCalendar(folderEvent.getFolderId());
             // If deleted, remove FolderId from listened folder Id and renew
             // subscription
             if (deleted && calendarFolderIds.contains(folderEvent.getFolderId())) {
               calendarFolderIds.remove(folderEvent.getFolderId());
             }
           } else {
             if (LOG.isTraceEnabled()) {
               LOG.trace("Folder Event wasn't catched: " + folderEvent.getEventType().name() + "on folder: " + folderEvent.getFolderId().getUniqueId());
             }
           }
         }
       }
     }
 
     private void newSubscription() throws Exception {
       if (LOG.isTraceEnabled()) {
         LOG.trace("New Subscription for user: " + username);
       }
       if (subscription != null) {
         try {
           subscription.unsubscribe();
         } catch (Exception e) {
           // Nothing to do, subscription may be timed out
           if (LOG.isDebugEnabled() || LOG.isTraceEnabled()) {
             LOG.error("Error while unsubscribe, will renew it anyway.", e);
           }
         }
       }
       subscription = integrationService.getService().subscribeToPullNotifications(calendarFolderIds, 5, null, EventType.Modified, EventType.Created, EventType.Deleted);
     }
 
     @Override
     public void interrupt() {
       if (subscription != null) {
         try {
           LOG.info("Thread interruption: unsubscribe user service:" + username);
           subscription.unsubscribe();
         } catch (Exception e) {
           LOG.error("Thread interruption: Error while unsubscribe to thread of user:" + username);
         }
       }
       try {
         integrationService.removeInstance();
       } catch (Throwable e) {
         LOG.error("Error while inerrupting thread", e);
       }
       super.interrupt();
     }
   }
 }
