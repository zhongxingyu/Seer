 package de.graind.client.widgets.monthly;
 
 import java.util.Date;
 
 import com.google.gwt.gdata.client.DateTime;
 import com.google.gwt.gdata.client.EventEntry;
 import com.google.gwt.gdata.client.GData;
 import com.google.gwt.gdata.client.GDataSystemPackage;
 import com.google.gwt.gdata.client.calendar.CalendarEventFeed;
 import com.google.gwt.gdata.client.calendar.CalendarEventFeedCallback;
 import com.google.gwt.gdata.client.calendar.CalendarEventQuery;
 import com.google.gwt.gdata.client.calendar.CalendarService;
 import com.google.gwt.gdata.client.impl.CallErrorException;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 import de.graind.client.util.CalendarUtil;
 import de.graind.shared.Config;
 
 public class MonthlyWidgetController implements MonthlyWidgetView.Controller {
   private static final DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-dd");
   private MonthlyWidgetView view;
   private CalendarService service;
 
   private AsyncCallback<EventEntry[]> eventCallback;
 
   private int year;
   private int month;
 
   public MonthlyWidgetController(MonthlyWidgetView view) {
     this.view = view;
 
     final Date now = new Date();
     this.year = CalendarUtil.getYear(now);
     this.month = CalendarUtil.getMonth(now);
 
     if (GData.isLoaded(GDataSystemPackage.CALENDAR)) {
       gdataLoaded();
     } else {
       GData.loadGDataApi(Config.API_KEY, new Runnable() {
         public void run() {
           gdataLoaded();
         }
       }, GDataSystemPackage.CALENDAR);
     }
   }
 
   private void gdataLoaded() {
     this.service = CalendarService.newInstance(Config.APPLICATION_NAME);
     MonthlyWidgetController.this.view.init(MonthlyWidgetController.this);
     fetchEventsForMonth(eventCallback);
   }
 
   private void fetchEventsForMonth(final AsyncCallback<EventEntry[]> callback) {
     CalendarEventQuery query = CalendarEventQuery
         .newInstance("http://www.google.com/calendar/feeds/default/private/full");
 
     final DateTime max = DateTime.newInstance(format.parse(year + "-" + month + "-"
         + CalendarUtil.getDaysOfMonth(year, month)));
     final DateTime min = DateTime.newInstance(format.parse(year + "-" + month + "-01"));
     query.setMaximumStartTime(max);
     query.setMinimumStartTime(min);
 
    // get all events (more then 1000 are unlikely)
    query.setMaxResults(1000);

     // TODO: add entries that start at an earlier date but are still valid
 
     service.getEventsFeed(query, new CalendarEventFeedCallback() {
       @Override
       public void onFailure(CallErrorException caught) {
         callback.onFailure(caught);
       }
 
       @Override
       public void onSuccess(CalendarEventFeed result) {
         callback.onSuccess(result.getEntries());
       }
     });
 
   }
 
   @Override
   public void setEventsCallback(AsyncCallback<EventEntry[]> callback) {
     this.eventCallback = callback;
   }
 
   @Override
   public void nextMonth() {
     if (month == 12) {
       month = 1;
       year++;
     } else {
       month++;
     }
     fetchEventsForMonth(eventCallback);
   }
 
   @Override
   public void previousMonth() {
     if (month == 1) {
       month = 12;
       year--;
     } else {
       month--;
     }
     fetchEventsForMonth(eventCallback);
   }
 
   @Override
   public int getMonth() {
     return month;
   }
 
   @Override
   public int getYear() {
     return year;
   }
 }
