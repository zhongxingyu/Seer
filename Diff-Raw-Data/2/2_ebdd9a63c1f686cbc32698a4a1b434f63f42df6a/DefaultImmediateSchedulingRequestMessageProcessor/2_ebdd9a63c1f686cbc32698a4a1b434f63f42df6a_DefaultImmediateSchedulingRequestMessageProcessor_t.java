 /**
  * This file is part of Project Control Center (PCC).
  * 
  * PCC (Project Control Center) project is intellectual property of 
  * Dmitri Anatol'evich Pisarenko.
  * 
  * Copyright 2010, 2011 Dmitri Anatol'evich Pisarenko
  * All rights reserved
  *
  **/
 
 package co.altruix.pcc.impl.immediatereschedulingrequestprocessor;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.security.PrivateKey;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import at.silverstrike.pcc.api.export2tj3.InvalidDurationException;
 import at.silverstrike.pcc.api.gcaltasks2pcc.GoogleCalendarTasks2PccImporter;
 import at.silverstrike.pcc.api.gcaltasks2pcc.GoogleCalendarTasks2PccImporterFactory;
 import at.silverstrike.pcc.api.model.Booking;
 import at.silverstrike.pcc.api.model.Resource;
 import at.silverstrike.pcc.api.model.SchedulingObject;
 import at.silverstrike.pcc.api.model.UserData;
 import at.silverstrike.pcc.api.persistence.Persistence;
 import at.silverstrike.pcc.api.privatekeyreader.PrivateKeyReader;
 import at.silverstrike.pcc.api.privatekeyreader.PrivateKeyReaderFactory;
 import at.silverstrike.pcc.api.projectscheduler.ProjectScheduler;
 import at.silverstrike.pcc.impl.privatekeyreader.DefaultPrivateKeyReaderFactory;
 
 import com.google.api.client.auth.oauth2.draft10.AccessTokenResponse;
 import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
 import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessTokenRequest;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.http.javanet.NetHttpTransport;
 import com.google.api.client.json.jackson.JacksonFactory;
 import com.google.api.services.tasks.v1.Tasks;
 import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
 import com.google.gdata.client.authn.oauth.OAuthException;
 import com.google.gdata.client.authn.oauth.OAuthRsaSha1Signer;
 import com.google.gdata.client.calendar.CalendarService;
 import com.google.gdata.data.DateTime;
 import com.google.gdata.data.PlainTextConstruct;
 import com.google.gdata.data.calendar.CalendarEntry;
 import com.google.gdata.data.calendar.CalendarEventEntry;
 import com.google.gdata.data.calendar.CalendarEventFeed;
 import com.google.gdata.data.calendar.CalendarFeed;
 import com.google.gdata.data.extensions.When;
 import com.google.gdata.util.ServiceException;
 import com.google.inject.Injector;
 
 import ru.altruix.commons.api.di.PccException;
 import ru.altruix.commons.impl.appendutils.AppendUtils;
 import co.altruix.pcc.api.cdm.PccMessage;
 import co.altruix.pcc.api.immediatereschedulingrequestprocessor.ImmediateSchedulingRequestMessageProcessor;
 import co.altruix.pcc.impl.cdm.DefaultImmediateSchedulingRequest;
 
 /**
  * @author DP118M
  * 
  */
 class DefaultImmediateSchedulingRequestMessageProcessor implements
         ImmediateSchedulingRequestMessageProcessor {
    private static final String TIMESTAMP_FORMAT = "dd.MM.yyyy HH:mm:ss";
 
     private static final String LINE_SEPARATOR = System
             .getProperty("line.separator");
 
     private static final String END_CONFIRMATION_MESSAGE =
             "@{timestamp}: Finished calcualtion of plan for user '@{userId}'"
                     + LINE_SEPARATOR;
 
     private static final String START_CONFIRMATION_MESSAGE =
             "@{timestamp}: Started to calculate plan for user '@{userId}'"
                     + LINE_SEPARATOR;
 
     private static final Logger LOGGER = LoggerFactory
             .getLogger(DefaultImmediateSchedulingRequestMessageProcessor.class);
 
     private static final int ONE_MONTH = 1;
 
     private Persistence persistence;
 
     private PccMessage message;
 
     private Injector injector;
 
     private String taskJugglerPath;
 
     private String consumerKey;
 
     private String calendarScope;
 
     private String allCalendarsFeedUrl;
 
     private String clientId;
 
     private String clientSecret;
 
     private File testerLogFilePath;
 
     public void setMessage(final PccMessage aMessage) {
         this.message = aMessage;
     }
 
     public boolean isMessageProcessingSucceeded() {
         return true;
     }
 
     public void run() throws PccException {
         final DefaultImmediateSchedulingRequest request =
                 (DefaultImmediateSchedulingRequest) this.message;
 
         final UserData userData = persistence.getUser(request.getUserId());
         LOGGER.debug(
                 "Immediate rescheduling request for user {}, start processing",
                 userData.getUsername());
 
         sendConfirmationForTester(userData, START_CONFIRMATION_MESSAGE);
 
         importDataFromGoogleTasks(userData);
         calculatePlan(userData);
         exportDataToGoogleCalendar(userData);
 
         LOGGER.debug(
                 "Immediate rescheduling request for user {}, processing finished",
                 userData.getUsername());
 
         sendConfirmationForTester(userData,
                 END_CONFIRMATION_MESSAGE);
     }
 
     private void sendConfirmationForTester(final UserData aUser,
             String aTemplate) {
         try {
             final String[] searchList =
                     new String[] { "@{timestamp}", "@{userId}" };
             final String[] replacementList =
                     new String[] { getTimestamp(), Long.toString(aUser.getId()) };
 
             final String confirmationMessage =
                     StringUtils.replaceEach(aTemplate, searchList,
                             replacementList);
 
             AppendUtils.appendToFile(confirmationMessage,
                     this.testerLogFilePath);
         } catch (final IOException exception) {
             LOGGER.error("", exception);
         }
     }
 
     private String getTimestamp() {
         return new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
     }
 
     private void calculatePlan(final UserData aUser) {
         final ProjectScheduler scheduler =
                 injector.getInstance(ProjectScheduler.class);
 
         LOGGER.debug("calculatePlan, user: {}", aUser.getId());
 
         final List<SchedulingObject> schedulingObjectsToExport =
                 persistence.getTopLevelTasks(aUser);
 
         LOGGER.debug("SCHEDULING OBJECTS TO EXPORT (START)");
         for (final SchedulingObject curSchedulingObject : schedulingObjectsToExport) {
             LOGGER.debug("Name: {}, ID: {}",
                     new Object[] { curSchedulingObject.getName(),
                             curSchedulingObject.getId() });
         }
         LOGGER.debug("SCHEDULING OBJECTS TO EXPORT (END)");
 
         scheduler.getProjectExportInfo().setSchedulingObjectsToExport(
                 schedulingObjectsToExport);
 
         final List<Resource> resources = new LinkedList<Resource>();
         resources.add(persistence.getCurrentWorker(aUser));
 
         scheduler.getProjectExportInfo().setResourcesToExport(resources);
 
         scheduler.getProjectExportInfo().setProjectName("pcc");
 
         final Date now = new Date();
 
         scheduler.getProjectExportInfo().setNow(now);
         scheduler.getProjectExportInfo().setCopyright("Dmitri Pisarenko");
         scheduler.getProjectExportInfo().setCurrency("EUR");
         scheduler.getProjectExportInfo().setSchedulingHorizonMonths(ONE_MONTH);
         scheduler.getProjectExportInfo().setUserData(aUser);
 
         scheduler.setDirectory(System.getProperty("user.dir") + "/");
         scheduler.setInjector(injector);
         scheduler.setNow(now);
         scheduler.setTaskJugglerPath(taskJugglerPath);
         try {
             scheduler.run();
         } catch (final InvalidDurationException exception) {
             LOGGER.error("", exception);
         } catch (final PccException exception) {
             LOGGER.error("", exception);
         }
     }
 
     private void exportDataToGoogleCalendar(final UserData aUser) {
         try {
             final PrivateKey privKey = getPrivateKey();
             final OAuthRsaSha1Signer signer = new OAuthRsaSha1Signer(privKey);
 
             final GoogleOAuthParameters oauthParameters =
                     new GoogleOAuthParameters();
             oauthParameters.setOAuthConsumerKey(consumerKey);
             oauthParameters.setScope(calendarScope);
 
             oauthParameters.setOAuthVerifier(aUser
                     .getGoogleCalendarOAuthVerifier()); // Verifier from the
                                                         // interactive part
             oauthParameters.setOAuthToken(aUser.getGoogleCalendarOAuthToken()); // Access
                                                                                 // token
                                                                                 // from
                                                                                 // the
                                                                                 // interactive
                                                                                 // part
             oauthParameters.setOAuthTokenSecret(aUser
                     .getGoogleCalendarOAuthTokenSecret()); // Token secret from
                                                            // the interactive
                                                            // part
 
             final CalendarService calendarService =
                     new CalendarService(consumerKey);
 
             calendarService
                     .setOAuthCredentials(oauthParameters, signer);
 
             final URL feedUrl =
                     new URL(
                             allCalendarsFeedUrl);
             final CalendarFeed resultFeed =
                     calendarService.getFeed(feedUrl, CalendarFeed.class);
 
             LOGGER.debug("resultFeed: {}", resultFeed);
 
             LOGGER.debug("Your calendars:");
 
             CalendarEntry pccCalendar = null;
             for (int i = 0; (i < resultFeed.getEntries().size())
                     && (pccCalendar == null); i++) {
                 final CalendarEntry entry = resultFeed.getEntries().get(i);
 
                 if ("PCC".equals(entry.getTitle().getPlainText())) {
                     pccCalendar = entry;
                 }
             }
 
             // Delete all events in the PCC calendar
 
             LOGGER.debug(
                     "PCC calendar: edit link='{}', self link='{}', content='{}', id='{}'",
                     new Object[] { pccCalendar.getEditLink().getHref(),
                             pccCalendar.getSelfLink().getHref(),
                             pccCalendar.getContent(), pccCalendar.getId() });
 
             final String calendarId =
                     pccCalendar
                             .getId()
                             .substring(
                                     "http://www.google.com/calendar/feeds/default/calendars/"
                                             .length());
             final URL pccCalendarUrl =
                     new URL(
                             "https://www.google.com/calendar/feeds/${calendarId}/private/full"
                                     .replace("${calendarId}", calendarId));
 
             LOGGER.debug("pccCalendarUrl: {}", pccCalendarUrl);
             // calendarService.getFeed(feedUrl, feedClass)
 
             final CalendarEventFeed pccEventFeed =
                     calendarService.getFeed(pccCalendarUrl,
                             CalendarEventFeed.class);
             for (final CalendarEventEntry curEvent : pccEventFeed.getEntries()) {
                 LOGGER.debug("Deleting event ''", curEvent);
                 curEvent.delete();
             }
 
             final List<Booking> bookings =
                     this.persistence.getBookings(aUser);
 
             LOGGER.debug("Bookings to export: {}", bookings.size());
 
             for (final Booking curBooking : bookings) {
                 LOGGER.debug(
                         "Exporting: start date time: {}, end date time: {}",
                         new Object[] { curBooking.getStartDateTime(),
                                 curBooking.getEndDateTime() });
 
                 final CalendarEventEntry event = new CalendarEventEntry();
 
                 event.setTitle(new PlainTextConstruct(curBooking.getProcess()
                         .getName()));
 
                 final When eventTime = new When();
                 final DateTime startDateTime =
                         new DateTime(curBooking.getStartDateTime().getTime());
                 final DateTime endDateTime =
                         new DateTime(curBooking.getEndDateTime().getTime());
 
                 eventTime.setStartTime(startDateTime);
                 eventTime.setEndTime(endDateTime);
 
                 event.addTime(eventTime);
 
                 calendarService.insert(pccCalendarUrl, event);
             }
         } catch (final IOException exception) {
             LOGGER.error("", exception);
         } catch (final OAuthException exception) {
             LOGGER.error("", exception);
         } catch (final ServiceException exception) {
             LOGGER.error("", exception);
         }
 
     }
 
     private PrivateKey getPrivateKey() {
         final PrivateKeyReaderFactory factory =
                 new DefaultPrivateKeyReaderFactory();
         final PrivateKeyReader reader = factory.create();
 
         reader.setInputStream(getClass().getClassLoader()
                         .getResourceAsStream("privatekey"));
 
         try {
             reader.run();
 
             return reader.getPrivateKey();
         } catch (final PccException exception) {
             LOGGER.error("", exception);
             return null;
         }
     }
 
     private void importDataFromGoogleTasks(final UserData aUserData) {
         final HttpTransport httpTransport = new NetHttpTransport();
         final JacksonFactory jsonFactory = new JacksonFactory();
 
         try {
             final String googleTasksRefreshToken =
                     aUserData.getGoogleTasksRefreshToken();
 
             LOGGER.debug("googleTasksRefreshToken: {}", googleTasksRefreshToken);
 
             final AccessTokenResponse response =
                     new GoogleAccessTokenRequest.GoogleRefreshTokenGrant(
                             httpTransport,
                             jsonFactory,
                             clientId, clientSecret,
                             googleTasksRefreshToken)
                             .execute();
 
             final GoogleAccessProtectedResource accessProtectedResource =
                     new GoogleAccessProtectedResource(
                             response.accessToken, httpTransport, jsonFactory,
                             clientId, clientSecret,
                             googleTasksRefreshToken);
 
             final Tasks service =
                     new Tasks(httpTransport, accessProtectedResource,
                             jsonFactory);
             service.setApplicationName(this.consumerKey);
 
             final GoogleCalendarTasks2PccImporterFactory importerFactory =
                     this.injector
                             .getInstance(GoogleCalendarTasks2PccImporterFactory.class);
             final GoogleCalendarTasks2PccImporter importer =
                     importerFactory.create();
 
             importer.setInjector(this.injector);
             importer.setService(service);
             importer.setUser(aUserData);
 
             importer.run();
         } catch (final IOException exception) {
             LOGGER.error("", exception);
         } catch (final PccException exception) {
             LOGGER.error("", exception);
         }
     }
 
     public void setInjector(final Injector aInjector) {
         if (aInjector != null) {
             this.persistence = aInjector.getInstance(Persistence.class);
 
             this.injector = aInjector;
         }
     }
 
     public void setTaskJugglerPath(final String aTaskJugglerPath) {
         this.taskJugglerPath = aTaskJugglerPath;
     }
 
     @Override
     public void setConsumerKey(final String aConsumerKey) {
         this.consumerKey = aConsumerKey;
     }
 
     @Override
     public void setCalendarScope(final String aCalendarScope) {
         this.calendarScope = aCalendarScope;
     }
 
     @Override
     public void setAllCalendarsFeedUrl(final String aAllCalendarsFeedUrl) {
         this.allCalendarsFeedUrl = aAllCalendarsFeedUrl;
     }
 
     @Override
     public void setClientId(final String aClientId) {
         this.clientId = aClientId;
     }
 
     @Override
     public void setClientSecret(final String aClientSecret) {
         this.clientSecret = aClientSecret;
     }
 
     @Override
     public void setTesterLogFilePath(final File aTesterLogFilePath) {
         this.testerLogFilePath = aTesterLogFilePath;
     }
 }
