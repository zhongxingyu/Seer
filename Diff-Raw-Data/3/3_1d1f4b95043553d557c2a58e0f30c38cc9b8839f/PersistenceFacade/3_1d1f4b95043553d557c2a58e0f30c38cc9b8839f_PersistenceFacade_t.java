 package com.babycycle.babyfeeding.model;
 
 import android.app.Application;
 import android.content.Context;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.j256.ormlite.dao.RuntimeExceptionDao;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: babycycle
  * Date: 4/25/13
  * Time: 1:11 PM
  * To change this template use File | Settings | File Templates.
  */
 @Singleton
 public class PersistenceFacade {
 
     List<FeedEvent> feedEventList = new ArrayList<FeedEvent>();
     private List<Reminder> reminders;
     private Date lastFeedStartTime;
     private String runningEventId = "1";
 
     public static void setMaxGapOneFeedingMillis(long maxGapOneFeedingMillis) {
         PersistenceFacade.maxGapOneFeedingMillis = maxGapOneFeedingMillis;
     }
 
     private static long maxGapOneFeedingMillis = 600000;
 
     public void saveFeedEvent(FeedEvent feedEvent, Context context) {
         DatabaseHelper.getHelper(context).saveFeedEvent(feedEvent);
     }
 
 
     public void saveReminder(Reminder reminder, Context context) {
         DatabaseHelper.getHelper(context).saveReminder(reminder);
     }
 
     public List<FeedEvent> getFeedEventList(Context context) {
 //        feedEventList = DatabaseHelper.getHelper(context).getFeedEvents(context);
         feedEventList = DatabaseHelper.getHelper(context).getLastFeedEvents(context);
         groupOddEvenEvents();
         return feedEventList;
     }
 
     public Date getLastFeedStartTime() {
         if(lastFeedStartTime == null) {
             return new Date();
         }
         return lastFeedStartTime;
     }
 
     private void groupOddEvenEvents() {
         for (int i = 0; i < feedEventList.size(); i++) {
             if(i == 0){
                 feedEventList.get(i).odd = true;
             } else {
                 if(feedEventList.get(i - 1).getStartTime().getTime() - feedEventList.get(i).getFinishTime().getTime() > maxGapOneFeedingMillis) {
                     feedEventList.get(i).odd = !feedEventList.get(i - 1).odd;
                 } else {
                     feedEventList.get(i).odd = feedEventList.get(i - 1).odd;
                 }
             }
 
         }
         lastFeedStartTime = feedEventList.get(0).getStartTime();
     }
 
     public List<Reminder> getReminders(Context context) {
         reminders = DatabaseHelper.getHelper(context).getReminders(context);
         return reminders;
     }
 
     public List<Reminder> getUpdatedForTodayReminders(Context context) {
         reminders = DatabaseHelper.getHelper(context).getReminders(context);
         for(Reminder reminder:reminders) {
             boolean updated = false;
             Calendar calendar = Calendar.getInstance();
             int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
             int currentMonth = calendar.get(Calendar.MONTH);
             calendar.setTime(reminder.getTimeOfDay());
             if(calendar.get(Calendar.DAY_OF_MONTH) != currentDay) {
                 calendar.set(Calendar.DAY_OF_MONTH, currentDay);
                 updated = true;
             }
             if(calendar.get(Calendar.MONTH) != currentMonth) {
                 calendar.set(Calendar.MONTH, currentMonth);
                 updated = true;
             }
             if(updated) {
                 reminder.setTimeOfDay(calendar.getTime());
                 reminder.setWasConfirmed(false);
                 saveReminder(reminder, context);
             }
         }
         reminders = DatabaseHelper.getHelper(context).getReminders(context);
         return reminders;
     }
 
     public List<Reminder> getActiveReminders(Context context) {
         List<Reminder> remindersTmp = getUpdatedForTodayReminders(context);
         reminders = new ArrayList<Reminder>(remindersTmp.size());
         for(Reminder reminder:remindersTmp) {
             if(reminderShouldBeShown(reminder)) {
                 reminders.add(reminder);
             }
         }
 
         return reminders;
     }
 
     private boolean reminderShouldBeShown(Reminder reminder) {
         Calendar calendar = Calendar.getInstance();
         int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
         calendar.setTime(reminder.getTimeOfDay());
         return !reminder.isWasConfirmed() && calendar.get(Calendar.HOUR_OF_DAY) <= currentHour;
     }
 
     public FeedEvent getRunningFeedEvent(Context context) {
         FeedEvent feedEvent = DatabaseHelper.getHelper(context).getFeedEventForId(context, runningEventId);
         if(feedEvent.getFinishTime() == null) {
             return feedEvent;
         } else {
             return null;
         }
     }
     public void persistStartedFeedEvent(FeedEvent feedEvent, Context context) {
         FeedEvent runningFeedEvent = DatabaseHelper.getHelper(context).getFeedEventForId(context, runningEventId);
         runningFeedEvent.setStartTime(feedEvent.getStartTime());
         runningFeedEvent.setLeftBreast(feedEvent.isLeftBreast());
         runningFeedEvent.setRightBreast(feedEvent.isRightBreast());
         runningFeedEvent.setFinishTime(null);
         DatabaseHelper.getHelper(context).saveFeedEvent(runningFeedEvent);
     }
 
     public void deleteStartedFeedEvent(Context context) {
         FeedEvent feedEvent = DatabaseHelper.getHelper(context).getFeedEventForId(context, runningEventId);
         if(feedEvent != null) {
            feedEvent.setFinishTime(new Date(0));
            feedEvent.setStartTime(new Date(0));
             feedEvent.setLeftBreast(false);
             feedEvent.setRightBreast(false);
             DatabaseHelper.getHelper(context).saveFeedEvent(feedEvent);
         }
     }
 }
