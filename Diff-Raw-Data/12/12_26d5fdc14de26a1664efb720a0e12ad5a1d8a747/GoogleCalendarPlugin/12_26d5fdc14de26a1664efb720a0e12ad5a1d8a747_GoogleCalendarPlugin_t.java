 /*
  * Copyright (c) 2012, Soheil Hassas Yeganeh.
  *
  *     Licensed under the Apache License, Version 2.0 (the "License");
  *     you may not use this file except in compliance with the License.
  *     You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  *     Unless required by applicable law or agreed to in writing, software
  *     distributed under the License is distributed on an "AS IS" BASIS,
  *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *     See the License for the specific language governing permissions and
  *     limitations under the License.
  */
 
 package org.tomighty.plugin.gcal;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.TimeZone;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import javax.swing.JOptionPane;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.tomighty.Phase;
 import org.tomighty.bus.Bus;
 import org.tomighty.bus.Subscriber;
 import org.tomighty.bus.messages.timer.TimerStarted;
 import org.tomighty.bus.messages.timer.TimerStopped;
 import org.tomighty.config.Directories;
 import org.tomighty.plugin.Plugin;
 import org.tomighty.time.Time;
 
 import com.google.api.client.auth.oauth2.Credential;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.http.javanet.NetHttpTransport;
 import com.google.api.client.json.JsonFactory;
 import com.google.api.client.json.jackson.JacksonFactory;
 import com.google.api.client.util.DateTime;
 import com.google.api.services.calendar.CalendarScopes;
 import com.google.api.services.calendar.model.Calendar;
 import com.google.api.services.calendar.model.CalendarList;
 import com.google.api.services.calendar.model.CalendarListEntry;
 import com.google.api.services.calendar.model.Event;
 import com.google.api.services.calendar.model.EventDateTime;
 import com.google.api.services.samples.shared.cmdline.oauth2.LocalServerReceiver;
 import com.google.api.services.samples.shared.cmdline.oauth2.OAuth2Native;
 
 /**
  * The plugin main class.
  *
  * @author Soheil Hassas Yeganeh <soheil@cs.toronto.edu>
  * @version 1.0
  */
 public class GoogleCalendarPlugin implements Plugin {
     private final Bus bus;
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
     private String tomightyCalId;
 
     /** Global instance of the HTTP transport. */
     private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
 
     /** Global instance of the JSON factory. */
     private static final JsonFactory JSON_FACTORY = new JacksonFactory();
 
     private static com.google.api.services.calendar.Calendar client;
 
     private Date lastStartTimerDate;
 
     class StartTimeSubscriber implements Subscriber<TimerStarted> {
 
 
         @Override
         public void receive(TimerStarted message) {
             Phase phase = message.getPhase();
             if (phase == Phase.BREAK) {
                 return;
             }
             lastStartTimerDate = new Date();
             logger.info("GCAL Start: " + lastStartTimerDate + "," +
                     (phase == Phase.BREAK ? " BREAK " : " WORK "));
 
         }
 
     }
 
     class StopTimeSubscriber implements Subscriber<TimerStopped> {
         @Override
         public void receive(TimerStopped message) {
             Phase phase = message.getPhase();
             if (phase == Phase.BREAK) {
                 return;
             }
 
            Time time = message.getTime();
            if (time.isZero() && lastStartTimerDate == null) {
                return;
            }

             String projectName =
                     JOptionPane.showInputDialog(
                             "Which project were you working on?");
 
             Date endDate = new Date();
             Date startDate = lastStartTimerDate != null ? lastStartTimerDate :
                     (new Date(endDate.getTime() -
                     (time.minutes() * 60 + time.seconds()) * 1000));
             logger.info("GCAL: " + startDate + "," +
                     new Date() + "," + projectName + "," +
                     (phase == Phase.BREAK ? " BREAK " : " WORK "));
 
             Event event = new Event();
             event.setSummary("WORK " + projectName);
             DateTime start = new DateTime(startDate,
                     TimeZone.getTimeZone("UTC"));
             event.setStart(new EventDateTime().setDateTime(start));
 
             DateTime end = new DateTime(endDate,
                     TimeZone.getTimeZone("UTC"));
             event.setEnd(new EventDateTime().setDateTime(end));
 
             lastStartTimerDate = null;
 
             try {
                 client.events().insert(tomightyCalId, event).execute();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     @Inject
     public GoogleCalendarPlugin(Bus bus, Directories directories) {
         this.bus = bus;
         TomightyGCredentialStore store = new TomightyGCredentialStore(
                 directories);
         try{
             // authorization
             Credential credential = OAuth2Native.authorize(
                     HTTP_TRANSPORT, JSON_FACTORY, new LocalServerReceiver(),
                     Arrays.asList(CalendarScopes.CALENDAR), store);
             // set up global Calendar instance
             client = com.google.api.services.calendar.Calendar.builder(
                     HTTP_TRANSPORT, JSON_FACTORY)
                     .setApplicationName("Tomighty-GCal/1.0")
                     .setHttpRequestInitializer(credential).build();
             CalendarList list = client.calendarList().list().execute();
             for (CalendarListEntry cal : list.getItems()) {
                 if ("Tomighty".equals(cal.getSummary())) {
                     tomightyCalId = cal.getId();
                     break;
                 }
             }
             if (tomightyCalId == null) {
                 Calendar tomightyCal = new Calendar();
                 tomightyCal.setSummary("Tomighty");
                 tomightyCalId = client.calendars().insert(tomightyCal).execute()
                         .getId();
             }
         } catch (Exception e) {
             e.printStackTrace();
             System.exit(1);
         }
     }
 
     @PostConstruct
     public void initialize() {
         bus.subscribe(new StartTimeSubscriber(), TimerStarted.class);
         bus.subscribe(new StopTimeSubscriber(), TimerStopped.class);
     }
 }
