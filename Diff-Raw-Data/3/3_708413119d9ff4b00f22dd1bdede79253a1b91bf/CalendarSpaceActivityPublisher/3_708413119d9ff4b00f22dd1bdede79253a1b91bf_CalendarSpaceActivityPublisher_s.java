 /*
  * Copyright (C) 2003-2010 eXo Platform SAS.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see<http://www.gnu.org/licenses/>.
  */
 package org.exoplatform.cs.ext.impl;
 
 import org.exoplatform.calendar.service.CalendarEvent;
 import org.exoplatform.calendar.service.CalendarService;
 import org.exoplatform.calendar.service.CalendarSetting;
 import org.exoplatform.calendar.service.Utils;
 import org.exoplatform.calendar.service.impl.CalendarEventListener;
 import org.exoplatform.container.PortalContainer;
 import org.exoplatform.portal.application.PortalRequestContext;
 import org.exoplatform.portal.webui.util.Util;
 import org.exoplatform.services.log.ExoLogger;
 import org.exoplatform.services.log.Log;
 import org.exoplatform.services.security.ConversationState;
 import org.exoplatform.social.common.ExoSocialException;
 import org.exoplatform.social.core.activity.model.ExoSocialActivity;
 import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
 import org.exoplatform.social.core.identity.model.Identity;
 import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
 import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
 import org.exoplatform.social.core.manager.ActivityManager;
 import org.exoplatform.social.core.manager.IdentityManager;
 import org.exoplatform.social.core.space.model.Space;
 import org.exoplatform.social.core.space.spi.SpaceService;
 import org.exoplatform.webui.application.WebuiRequestContext;
 
 import java.text.DateFormat;
 import java.text.DateFormatSymbols;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TimeZone;
 
 /**
  * Created by The eXo Platform SAS
  * Author : eXoPlatform
  *          exo@exoplatform.com
  * Jul 30, 2010  
  */
 public class CalendarSpaceActivityPublisher extends CalendarEventListener {
   private final static Log   LOG                   = ExoLogger.getLogger(CalendarSpaceActivityPublisher.class);
 
   public static final String CALENDAR_APP_ID       = "cs-calendar:spaces";
 
   public static final String EVENT_ADDED           = "EventAdded".intern();
 
   public static final String EVENT_UPDATED         = "EventUpdated".intern();
 
   public static final String EVENT_ID_KEY          = "EventID".intern();
 
   public static final String CALENDAR_ID_KEY       = "CalendarID".intern();
 
   public static final String TASK_ADDED            = "TaskAdded".intern();
 
   public static final String TASK_UPDATED          = "TaskUpdated".intern();
 
   public static final String EVENT_TYPE_KEY        = "EventType".intern();
 
   public static final String EVENT_SUMMARY_KEY     = "EventSummary".intern();
 
   public static final String EVENT_TITLE_KEY       = "EventTitle".intern();
 
   public static final String EVENT_DESCRIPTION_KEY = "EventDescription".intern();
 
   public static final String EVENT_LOCALE_KEY      = "EventLocale".intern();
 
   public static final String EVENT_STARTTIME_KEY   = "EventStartTime".intern();
 
   public static final String EVENT_ENDTIME_KEY     = "EventEndTime".intern();
 
   public static final String EVENT_LINK_KEY        = "EventLink";
 
   public static final String INVITATION_DETAIL     = "/invitation/detail/";
 
   public static final String CALENDAR_FIELDS_CHANGED = "CALENDAR_FIELDS_CHANGED";
 
 
   public static final String SUMMARY_UPDATED = "summary_updated";
   public static final String DESCRIPTION_UPDATED = "description_updated";
   public static final String DESCRIPTION_REMOVED = "description_removed";
   public static final String FROM_UPDATED = "fromDateTime_updated";
   public static final String TO_UPDATED = "toDateTime_updated";
   public static final String LOCATION_UPDATED = "location_updated";
   public static final String LOCATION_REMOVED = "location_removed";
   public static final String ALLDAY_UPDATED = "allDay_updated";
   public static final String REPEAT_UPDATED = "repeatType_updated";
   public static final String ATTACH_UPDATED = "attachment_updated";
   public static final String CATEGORY_UPDATED = "eventCategoryName_updated";
   public static final String CALENDAR_UPDATED = "calendarId_updated";
   public static final String PRIORITY_UPDATED = "priority_updated";
 
 
   private static final String RP_END_BYDATE = "endByDate";
   private static final String RP_END_AFTER = "endAfter";
   private static final String RP_END_NEVER = "neverEnd";
 
   private static final String RP_MONTHLY_BYDAY = "monthlyByDay";
   private static final String RP_MONTHLY_BYMONTHDAY = "monthlyByMonthDay";
 
   private static final String REPEAT_EVENT_INSTANCE_REMOVED = "repeatEvent_instance_removed";
 
   public static final String NAME_UPDATED = "name_updated";
   public static final String NOTE_UPDATED = "note_updated";
   public static final String NOTE_REMOVED = "note_removed";
   public static final String TASK_CATEGORY_UPDATED = "taskCategoryName_updated";
   public static final String TASK_CALENDAR_UPDATED = "task_CalendarId_updated";
   public static final String TASK_ATTACH_UPDATED = "task_attachment_updated";
   public static final String TASK_NEED_ACTION = CalendarEvent.NEEDS_ACTION;
   public static final String TASK_IN_PROCESS_ACTION = CalendarEvent.IN_PROCESS;
   public static final String TASK_COMPLETED_ACTION = CalendarEvent.COMPLETED;
   public static final String TASK_CANCELLED_ACTION = CalendarEvent.CANCELLED;
   public static final String STOP_REPEATING = "stop_repeating";
   public static final String EVENT_CANCELLED="event_cancelled";
 
   private CalendarService calendarService;
   private IdentityManager identityManager;
   private ActivityManager activityManager;
   private SpaceService spaceService;
 
   public CalendarSpaceActivityPublisher() {
 
   }
 
   /**
    * Make url for the event of the calendar application. 
    * Format of the url is: 
    * <ul>
    *    <li>/[portal]/[space]/[calendar]/[username]/invitation/detail/[event id]/[calendar type]</li>
    * </ul>
    * The format is used to utilize the invitation email feature implemented before.
    * <br>
    * <strong>[NOTE]</strong>
    * Keep in mind that this function calls {@link PortalRequestContext} which is in webui layer while this function is usually invoked in the service layer. Need to be improved in the future for ensuring the system design convention.
    * 
    * @param event have to be not null
    * @return empty string if the process is failed.
    */
   private String makeEventLink(CalendarEvent event) {
     StringBuffer sb = new StringBuffer("");    
     PortalRequestContext requestContext = Util.getPortalRequestContext();
     sb.append(requestContext.getPortalURI())
     .append(requestContext.getNodePath())
     .append(INVITATION_DETAIL)
     .append(ConversationState.getCurrent().getIdentity().getUserId())
     .append("/").append(event.getId())
     .append("/").append(event.getCalType());    
     return sb.toString();
   }
 
   private Map<String, String> makeActivityParams(CalendarEvent event, String calendarId, String eventType) {
     Map<String, String> params = new HashMap<String, String>();
     params.put(EVENT_TYPE_KEY, eventType);
     params.put(EVENT_ID_KEY, event.getId());
     params.put(CALENDAR_ID_KEY, calendarId);
     params.put(EVENT_SUMMARY_KEY, event.getSummary());
     params.put(EVENT_LOCALE_KEY, event.getLocation() != null ? event.getLocation() : "");
     params.put(EVENT_DESCRIPTION_KEY, event.getDescription() != null ? event.getDescription() : "");
     params.put(EVENT_STARTTIME_KEY, String.valueOf(event.getFromDateTime().getTime()));
     params.put(EVENT_ENDTIME_KEY, String.valueOf(event.getToDateTime().getTime()));
     params.put(EVENT_LINK_KEY, makeEventLink(event));
     return params;
   }
 
   /**
    * publish a new event activity
    *
    * @param event
    */
   private void publishActivity(CalendarEvent event) {
     ExoSocialActivity activity = getActivityForEvent(event);
     if(activity == null) {
       if(LOG.isDebugEnabled()) {
         LOG.error("Can not record Activity for space when event added ");
       }
     }
   }
 
   /*
    * Gets activity for a calendar event
    * if the activity is not exist/removed,(re-)creates one
    * in case event is created before PLF 4, it has no activityId property,
    * we create new activity and set its activityId
    */
   private ExoSocialActivity getActivityForEvent(CalendarEvent calendarEvent) {
     String eventType = calendarEvent.getEventType().equalsIgnoreCase(CalendarEvent.TYPE_EVENT) ? EVENT_ADDED : TASK_ADDED;
     String calendarId = calendarEvent.getCalendarId();
     //if calendar is null, or not a space calendar, returns null
     if (calendarId == null || calendarId.indexOf(CalendarDataInitialize.SPACE_CALENDAR_ID_SUFFIX) < 0) {
       return null;
     }
 
     identityManager = (IdentityManager) PortalContainer.getInstance().getComponentInstanceOfType(IdentityManager.class);
     spaceService = (SpaceService) PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class);
     activityManager = (ActivityManager) PortalContainer.getInstance().getComponentInstanceOfType(ActivityManager.class);
 
     String spaceGroupId = Utils.getSpaceGroupIdFromCalendarId(calendarId);
     Space space = spaceService.getSpaceByGroupId(spaceGroupId);
     String userId = ConversationState.getCurrent().getIdentity().getUserId();
     Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
     Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, false);
 
     if(space == null) {
       return null;
     }
 
     ExoSocialActivity activity = null;
 
     if(calendarEvent.getActivityId() != null) {
       activity = activityManager.getActivity(calendarEvent.getActivityId());
     }
 
     if(activity == null) {
       // create activity
       activity = new ExoSocialActivityImpl();
       activity.setUserId(userIdentity.getId());
       activity.setTitle(calendarEvent.getSummary());
       activity.setBody(calendarEvent.getDescription());
       activity.setType(CALENDAR_APP_ID);
       activity.setTemplateParams(makeActivityParams(calendarEvent, calendarId, eventType));
       activityManager.saveActivityNoReturn(spaceIdentity, activity);
       calendarEvent.setActivityId(activity.getId());
     } else {
       //update the activity
       activity.setTitle(calendarEvent.getSummary());
       activity.setBody(calendarEvent.getDescription());
       activity.setTemplateParams(makeActivityParams(calendarEvent, calendarId, eventType));
       activityManager.updateActivity(activity);
     }
     return activity;
   }
   /**
    * adds comment to existing event activity
    *
    * @param event
    * @param messagesParams
    */
   private void updateToActivity(CalendarEvent event, Map<String, String> messagesParams){
     ExoSocialActivity activity = getActivityForEvent(event);
     if(activity != null) {
       //add comment to the activity
       ExoSocialActivity comment = createComment(messagesParams);
       activityManager.saveComment(activity, comment);
     }
   }
 
   /**
    * Adds a comment to the activity of a repetitive event, when user edits a following series
    * The comment has content: "The event will stop repeating on : $LAST_EVENT_DATE, cf RECURRING_ACTIVITY_04
    * from http://community.exoplatform.com/portal/intranet/wiki/group/spaces/platform_41/Recurring_Events_Specification
    * @param originEvent
    * @param stopDate
    */
   public void updateFollowingOccurrences(CalendarEvent originEvent, Date stopDate) {
     ExoSocialActivity activity = getActivityForEvent(originEvent);
     if(activity != null) {
       Map<String,String> params = new HashMap<String, String>();
       params.put(STOP_REPEATING, String.valueOf(stopDate.getTime()));
       ExoSocialActivity comment = createComment(params);
       activityManager.saveComment(activity,comment);
     }
   }
 
   /**
    * Adds a comment to the activity of a repetitive event, when user removes an exception event
    * The comment has content: "Event cancelled for $CANCEL_DATE, cf  RECURRING_ACTIVITY_05 from
    * http://community.exoplatform.com/portal/intranet/wiki/group/spaces/platform_41/Recurring_Events_Specification
    * @param originEvent  The origin repetitive event
    * @param removedEvent  The occurrence selected to be removed
    */
   public void removeOneOccurrence(CalendarEvent originEvent, CalendarEvent removedEvent) {
     ExoSocialActivity activity = getActivityForEvent(originEvent);
     Map<String,String> params = new HashMap<String, String>();
     params.put(EVENT_CANCELLED,String.valueOf(removedEvent.getFromDateTime().getTime()));
     ExoSocialActivity comment = createComment(params);
     activityManager.saveComment(activity, comment);
   }
   /**
    * creates a comment associated to updated fields
    *
    * @param messagesParams
    * @return a comment object
    * @since activity-type
    */
   private ExoSocialActivity createComment(Map<String,String> messagesParams) {
     String userId = ConversationState.getCurrent().getIdentity().getUserId();
     Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, false);
 
     ExoSocialActivity newComment = new ExoSocialActivityImpl();
     newComment.isComment(true);
     newComment.setUserId(userIdentity.getId());
     newComment.setType("CALENDAR_ACTIVITY");
     StringBuilder fields = new StringBuilder();
     Map<String, String> data = new LinkedHashMap<String, String>(); 
 
     for(String field : messagesParams.keySet()) {
       String value = messagesParams.get(field);
       data.put(field, value); // store field changed and its new value
       fields.append("," + field);
     }
     String fieldsChanged = fields.toString().substring(1); // remove the first ","
     data.put(CALENDAR_FIELDS_CHANGED, fieldsChanged);
     newComment.setTitleId(fieldsChanged);
     newComment.setTemplateParams(data);
 //    newComment.setTitle(title.toString());
     return newComment;
   }
 
   /**
    * delete the event activity
    *
    * @param event
    * @param calendarId
    */
   private void deleteActivity(CalendarEvent event, String calendarId){
     try {
       Class.forName("org.exoplatform.social.core.space.spi.SpaceService");
     } catch (ClassNotFoundException e) {
       if (LOG.isDebugEnabled()) {
         LOG.debug("eXo Social components not found!", e);
       }
       return;
     }
     if (calendarId == null || calendarId.indexOf(CalendarDataInitialize.SPACE_CALENDAR_ID_SUFFIX) < 0) {
       return;
     }
     try{
       activityManager = (ActivityManager) PortalContainer.getInstance().getComponentInstanceOfType(ActivityManager.class);
       spaceService = (SpaceService) PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class);
       String spaceGroupId = Utils.getSpaceGroupIdFromCalendarId(calendarId);
       Space space = spaceService.getSpaceByGroupId(spaceGroupId);
       if (space != null && event.getActivityId() != null) {
         activityManager.deleteActivity(event.getActivityId());
       }
     } catch (ExoSocialException e){
       if (LOG.isDebugEnabled())
         LOG.error("Can not delete Activity for space when event deleted ", e);
     }
   }
   private Map<String, String> buildParams(CalendarEvent oldEvent, CalendarEvent newEvent){
     Map<String, String> messagesParams = new LinkedHashMap<String, String>();
     try {
       if(CalendarEvent.TYPE_EVENT.equals(newEvent.getEventType())) {
         if(!oldEvent.getSummary().equals(newEvent.getSummary())) {
           messagesParams.put(SUMMARY_UPDATED,newEvent.getSummary()) ;
         }
 
         /* change description */
         if (newEvent.getDescription() != null && !newEvent.getDescription().equals(oldEvent.getDescription())) {
           messagesParams.put(DESCRIPTION_UPDATED,newEvent.getDescription()) ;
         }
         /* remove description */
         else if ((newEvent.getDescription() == null) && (oldEvent.getDescription() != null)) {
           messagesParams.put(DESCRIPTION_REMOVED, "") ;
         }
 
         /* change location */
         if (newEvent.getLocation()!= null && !newEvent.getLocation().equals(oldEvent.getLocation())) {
           messagesParams.put(LOCATION_UPDATED,newEvent.getLocation()) ;
         }
         /* remove location */
         else if ((newEvent.getLocation() == null) && (oldEvent.getLocation() !=null)) {
           messagesParams.put(LOCATION_REMOVED, "") ;
         }
 
         if(newEvent.getPriority()!= null && !newEvent.getPriority().equals(oldEvent.getPriority())) {
           messagesParams.put(PRIORITY_UPDATED,newEvent.getPriority()) ;
         }
         if(newEvent.getAttachment() != null) if(oldEvent.getAttachment() == null ){
           messagesParams.put(ATTACH_UPDATED,"") ;
         } else if(newEvent.getAttachment().size() != oldEvent.getAttachment().size()) {
           messagesParams.put(ATTACH_UPDATED,"") ;
         }
         if(isAllDayEvent(newEvent) && !isAllDayEvent(oldEvent) && CalendarEvent.TYPE_EVENT.equals(oldEvent.getEventType())) {
           messagesParams.put(ALLDAY_UPDATED,"") ;
         } else if (!isAllDayEvent(newEvent)) {
           if(newEvent.getFromDateTime().compareTo(oldEvent.getFromDateTime()) != 0) {
             messagesParams.put(FROM_UPDATED,String.valueOf(newEvent.getFromDateTime().getTime())) ;
           }
           if(newEvent.getToDateTime().compareTo(oldEvent.getToDateTime()) != 0) {
             messagesParams.put(TO_UPDATED,String.valueOf(newEvent.getToDateTime().getTime())) ; 
           }
         }
 
         /*=== compare the repeat type ===*/
         String repeatSummary = buildRepeatSummary(newEvent);
         if (!repeatSummary.equals(buildRepeatSummary(oldEvent)))
         {
           messagesParams.put(REPEAT_UPDATED, repeatSummary) ;
         }
 
         /*=== compare the activity id ===*/
         // oldEvent -- occurrence or instance of repetitive event -- no activity
         // newEvent -- repetitiveEvent - with activity
         if (!newEvent.getActivityId().equals(oldEvent.getActivityId()))
         {
           messagesParams.put(REPEAT_EVENT_INSTANCE_REMOVED, getDateFormattedAfterUserSetting(oldEvent.getRecurrenceId())) ;
         }
 
       } else {
         if(!oldEvent.getSummary().equals(newEvent.getSummary())) {
           messagesParams.put(NAME_UPDATED,newEvent.getSummary()) ;
         }
         /* change note */
         if(newEvent.getDescription() != null && !newEvent.getDescription().equals(oldEvent.getDescription())) {
           messagesParams.put(NOTE_UPDATED,newEvent.getDescription()) ;
         }
         /* removed note */
         else if ((newEvent.getDescription() == null) && (oldEvent.getDescription() != null)) {
           messagesParams.put(NOTE_REMOVED, "") ;
         }
         
         if (!isAllDayEvent(newEvent)) {
           if(newEvent.getFromDateTime().compareTo(oldEvent.getFromDateTime()) != 0) {
             messagesParams.put(FROM_UPDATED, String.valueOf(newEvent.getFromDateTime().getTime()));
           }
           if(newEvent.getToDateTime().compareTo(oldEvent.getToDateTime()) != 0) {
             messagesParams.put(TO_UPDATED, String.valueOf(newEvent.getToDateTime().getTime())); 
           }
         }
         if(newEvent.getPriority()!= null && !newEvent.getPriority().equals(oldEvent.getPriority())) {
           messagesParams.put(PRIORITY_UPDATED,newEvent.getPriority()) ;
         }
         if(newEvent.getAttachment() != null) if(oldEvent.getAttachment() == null ){
           messagesParams.put(TASK_ATTACH_UPDATED,"") ;
         } else if(newEvent.getAttachment().size() != oldEvent.getAttachment().size()) {
           messagesParams.put(TASK_ATTACH_UPDATED,"") ;
         }
         if(newEvent.getEventState() != null && !newEvent.getEventState().equals(oldEvent.getEventState())) {
           if(CalendarEvent.NEEDS_ACTION.equals(newEvent.getEventState())) {
             messagesParams.put(TASK_NEED_ACTION, newEvent.getEventState()) ;
           } else if(CalendarEvent.IN_PROCESS.equals(newEvent.getEventState())) {
             messagesParams.put(TASK_IN_PROCESS_ACTION, newEvent.getEventState()) ;
           } else if(CalendarEvent.COMPLETED.equals(newEvent.getEventState())) {
             messagesParams.put(TASK_COMPLETED_ACTION, newEvent.getEventState()) ;
           } else if(CalendarEvent.CANCELLED.equals(newEvent.getEventState())) {
             messagesParams.put(TASK_CANCELLED_ACTION, newEvent.getEventState()) ;
           }
         }
       }
     } catch (Exception e) {
       if (LOG.isDebugEnabled())
         LOG.error("Can not build message for space when event updated ", e);
     }
     return messagesParams;
   }
 
   /**
    * convert date to user date format using calendar setting
    *
    * @param date
    * @return
    */
   private String getDateFormattedAfterUserSetting(String date)
   {
     try
     {
       String userId = ConversationState.getCurrent().getIdentity().getUserId();
       calendarService = (CalendarService)PortalContainer.getInstance().getComponentInstance(CalendarService.class);   // not null
       CalendarSetting calSetting = calendarService.getCalendarSetting(userId);  // not null
       WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance() ;
       Locale locale = requestContext.getParentAppRequestContext().getLocale() ;
       DateFormat format = new SimpleDateFormat(calSetting.getDateFormat(), locale);
 
       /* recurrenceId to Date then re-convert it to user date format */
       DateFormat format1 = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
       Date eventDate = format1.parse(date);
 
       return format.format(eventDate);
     }
     catch (Exception e)
     {
       LOG.debug(e.getMessage());
     }
     return null;
   }
 
   /**
    * Build the repeating summary, i.e: daily every 2 days, until 02/03/2011. <br/>
    * The summary structure is defined in resource bundle, it contains some parameters and </br>
    * will be replaced by values from repeatEvent. <br/>
    * <p>There are 6 parameters: {count}, {until}, {interval}, {byDays}, {theDay}, {theNumber}.<br/>
    * Some labels in resource bundle to define numbers (the first, the second, ...) which were used in summary
    * @param repeatEvent the repeating event
    * @return summary string about repeating event
    * @throws Exception
    */
   public String buildRepeatSummary(CalendarEvent repeatEvent)
   {
     if (repeatEvent == null) return "";
     String repeatType = repeatEvent.getRepeatType();
     if (CalendarEvent.RP_NOREPEAT.equals(repeatType) || repeatType == null) return "";
 
     try {
     String userId = ConversationState.getCurrent().getIdentity().getUserId();
     calendarService = (CalendarService)PortalContainer.getInstance().getComponentInstance(CalendarService.class);
     CalendarSetting calSetting = calendarService.getCalendarSetting(userId);
     WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance() ;
 
     Locale locale = requestContext.getParentAppRequestContext().getLocale() ;
     DateFormat format = new SimpleDateFormat(calSetting.getDateFormat(), locale);
     DateFormatSymbols symbols = new DateFormatSymbols(locale);
     String[] dayOfWeeks = symbols.getWeekdays();
 
     int interval = (int)repeatEvent.getRepeatInterval();
     int count = (int)repeatEvent.getRepeatCount();
     Date until = repeatEvent.getRepeatUntilDate();
     String endType = RP_END_NEVER;
     if (count > 0) endType = RP_END_AFTER;
     if (until != null) endType = RP_END_BYDATE;
 
     StringBuilder pattern = new StringBuilder("");
     if (repeatType.equals(CalendarEvent.RP_DAILY)) {
       if (interval == 1) {
         //pattern = "Daily";
         pattern.append(CalendarUIActivity.getUICalendarLabel("daily"));
       } else {
         //pattern = "Every {interval} days";
         pattern.append(CalendarUIActivity.getUICalendarLabel("every-day"));
       }
       if (endType.equals(RP_END_AFTER)) {
         //pattern = "Daily, {count} times";
         //pattern = "Every {interval} days, {count} times";
         pattern.append(", " + CalendarUIActivity.getUICalendarLabel("count-times"));
       }
       if (endType.equals(RP_END_BYDATE)) {
         //pattern = "Daily, until {until}";
         //pattern = "Every {interval} days, until {until}";
         pattern.append(", " + CalendarUIActivity.getUICalendarLabel("until"));
       }
 
       return new String(pattern).replace("{interval}", String.valueOf(interval))
           .replace("{count}", String.valueOf(repeatEvent.getRepeatCount()))
           .replace("{until}", repeatEvent.getRepeatUntilDate()==null?"":format.format(repeatEvent.getRepeatUntilDate()));
     }
 
     if (repeatType.equals(CalendarEvent.RP_WEEKLY)) {
       if (interval == 1) {
         //pattern = "Weekly on {byDays}";
         pattern.append(CalendarUIActivity.getUICalendarLabel("weekly"));
       } else {
         //pattern = "Every {interval} weeks on {byDays}";
         pattern.append(CalendarUIActivity.getUICalendarLabel("every-week"));
       }
       if (endType.equals(RP_END_AFTER)) {
         //pattern = "Weekly on {byDays}, {count} times";
         //pattern = "Every {interval} weeks on {byDays}, {count} times";
         pattern.append(", " + CalendarUIActivity.getUICalendarLabel("count-times"));
       }
       if (endType.equals(RP_END_BYDATE)) {
         //pattern = "Weekly on {byDays}, until {until}";
         //pattern = "Every {interval} weeks on {byDays}, until {until}";
         pattern.append(", " + CalendarUIActivity.getUICalendarLabel("until"));
       }
 
       String[] weeklyByDays = repeatEvent.getRepeatByDay();
       StringBuffer byDays = new StringBuffer();
       for (int i = 0; i < weeklyByDays.length; i++) {
         if (i == 0) {
           byDays.append(dayOfWeeks[convertToDayOfWeek(weeklyByDays[0])]);
         } else {
           byDays.append(", ");
           byDays.append(dayOfWeeks[convertToDayOfWeek(weeklyByDays[i])]);
         }
       }
       return new String(pattern).replace("{interval}", String.valueOf(interval))
           .replace("{count}", String.valueOf(repeatEvent.getRepeatCount()))
           .replace("{until}", repeatEvent.getRepeatUntilDate()==null?"":format.format(repeatEvent.getRepeatUntilDate()))
           .replace("{byDays}", byDays.toString());
     }
 
     if (repeatType.equals(CalendarEvent.RP_MONTHLY)) {
       String monthlyType = RP_MONTHLY_BYMONTHDAY;
       if (repeatEvent.getRepeatByDay() != null && repeatEvent.getRepeatByDay().length > 0) monthlyType = RP_MONTHLY_BYDAY;
 
       if (interval == 1) {
         // pattern = "Monthly on"
         pattern.append(CalendarUIActivity.getUICalendarLabel("monthly"));
       } else {
         // pattern = "Every {interval} months on
         pattern.append(CalendarUIActivity.getUICalendarLabel("every-month"));
       }
 
       if (monthlyType.equals(RP_MONTHLY_BYDAY)) {
         // pattern = "Monthly on {theNumber} {theDay}
         // pattern = "Every {interval} months on {theNumber} {theDay}
         pattern.append(" " + CalendarUIActivity.getUICalendarLabel("monthly-by-day"));
       } else {
         // pattern = "Monthly on day {theDay}
         // pattern = "Every {interval} months on day {theDay}
         pattern.append(" " + CalendarUIActivity.getUICalendarLabel("monthly-by-month-day"));
       }
 
       if (endType.equals(RP_END_AFTER)) {
         pattern.append(", " + CalendarUIActivity.getUICalendarLabel("count-times"));
       }
       if (endType.equals(RP_END_BYDATE)) {
         pattern.append(", " + CalendarUIActivity.getUICalendarLabel("until"));
       }
 
       String theNumber = ""; // the first, the second, the third, ...
       String theDay = ""; // in monthly by day, it's Monday, Tuesday, ... (day of week), in monthly by monthday, it's 1-31 (day of month)
       if (monthlyType.equals(RP_MONTHLY_BYDAY)) {
         java.util.Calendar temp = getCalendarInstanceBySetting(calSetting);
         temp.setTime(repeatEvent.getFromDateTime());
         int weekOfMonth = temp.get(java.util.Calendar.WEEK_OF_MONTH);
         java.util.Calendar temp2 = getCalendarInstanceBySetting(calSetting);
         temp2.setTime(temp.getTime());
         temp2.add(java.util.Calendar.DATE, 7);
         if (temp2.get(java.util.Calendar.MONTH) != temp.get(java.util.Calendar.MONTH)) weekOfMonth = 5;
         int dayOfWeek = temp.get(java.util.Calendar.DAY_OF_WEEK);
         String[] weekOfMonths = new String[] {CalendarUIActivity.getUICalendarLabel("summary-the-first"), CalendarUIActivity.getUICalendarLabel("summary-the-second"), CalendarUIActivity.getUICalendarLabel("summary-the-third"),
             CalendarUIActivity.getUICalendarLabel("summary-the-fourth"), CalendarUIActivity.getUICalendarLabel("summary-the-last")};
         theNumber = weekOfMonths[weekOfMonth-1];
         theDay = dayOfWeeks[dayOfWeek];
       } else {
         java.util.Calendar temp = getCalendarInstanceBySetting(calSetting);
         temp.setTime(repeatEvent.getFromDateTime());
         int dayOfMonth = temp.get(java.util.Calendar.DAY_OF_MONTH);
         theDay = String.valueOf(dayOfMonth);
       }
       return new String(pattern).replace("{interval}", String.valueOf(interval))
           .replace("{count}", String.valueOf(repeatEvent.getRepeatCount()))
           .replace("{until}", repeatEvent.getRepeatUntilDate()==null?"":format.format(repeatEvent.getRepeatUntilDate()))
           .replace("{theDay}", theDay).replace("{theNumber}", theNumber);
     }
 
     if (repeatType.equals(CalendarEvent.RP_YEARLY)) {
       if (interval == 1) {
         // pattern = "Yearly on {theDay}"
         pattern.append(CalendarUIActivity.getUICalendarLabel("yearly"));
       } else {
         // pattern = "Every {interval} years on {theDay}"
         pattern.append(CalendarUIActivity.getUICalendarLabel("every-year"));
       }
 
       if (endType.equals(RP_END_AFTER)) {
         // pattern = "Yearly on {theDay}, {count} times"
         // pattern = "Every {interval} years on {theDay}, {count} times"
         pattern.append(", " + CalendarUIActivity.getUICalendarLabel("count-times"));
       }
       if (endType.equals(RP_END_BYDATE)) {
         // pattern = "Yearly on {theDay}, until {until}"
         // pattern = "Every {interval} years on {theDay}, until {until}"
         pattern.append(", " + CalendarUIActivity.getUICalendarLabel("until"));
       }
 
       String theDay = format.format(repeatEvent.getFromDateTime()); //
       return new String(pattern).replace("{interval}", String.valueOf(interval))
           .replace("{count}", String.valueOf(repeatEvent.getRepeatCount()))
           .replace("{until}", repeatEvent.getRepeatUntilDate()==null?"":format.format(repeatEvent.getRepeatUntilDate()))
           .replace("{theDay}", theDay);
     }
     }
     catch (Exception e)
     {
       LOG.info(e.getLocalizedMessage());
     }
     return null;
   }
 
   public static int convertToDayOfWeek(String day) {
     int dayOfWeek = (day.equals("MO")?2:
         (day.equals("TU")?3:
             (day.equals("WE")?4:
                 (day.equals("TH")?5:
                     (day.equals("FR")?6:
                         (day.equals("SA")?7:
                             (day.equals("SU")?1:0)
                         ))))));
     return dayOfWeek;
   }
 
   /**
    * get calendar by user setting (timezone, first day of week)
    * @param calendarSetting
    * @return calendar object
    */
   public static Calendar getCalendarInstanceBySetting(CalendarSetting calendarSetting) {
     Calendar calendar = GregorianCalendar.getInstance() ;
     calendar.setLenient(false);
     calendar.setTimeZone(TimeZone.getTimeZone(calendarSetting.getTimeZone()));
     calendar.setFirstDayOfWeek(Integer.parseInt(calendarSetting.getWeekStartOn()));
     calendar.setMinimalDaysInFirstWeek(4);
     return calendar;
   }
 
   private boolean isAllDayEvent(CalendarEvent eventCalendar) {
     try {
       TimeZone tz = getUserTimeZone() ;
       Calendar cal1 = new GregorianCalendar(tz) ;
       Calendar cal2 = new GregorianCalendar(tz) ;
       cal1.setLenient(false);
       cal1.setTime(eventCalendar.getFromDateTime()) ;
       //cal1.setTimeZone(tz);
       cal2.setLenient(false);
       cal2.setTime(eventCalendar.getToDateTime()) ;
       //cal2.setTimeZone(tz);
       return (cal1.get(Calendar.HOUR_OF_DAY) == 0  && 
           cal1.get(Calendar.MINUTE) == 0 &&
           cal2.get(Calendar.HOUR_OF_DAY) == cal2.getActualMaximum(Calendar.HOUR_OF_DAY)&& 
           cal2.get(Calendar.MINUTE) == cal2.getActualMaximum(Calendar.MINUTE) );
     } catch (Exception e) {
       if (LOG.isDebugEnabled())
         LOG.error("Can not check all day event when event updated ", e);
     }
     return false;
   }
 
   private TimeZone getUserTimeZone() {
     try {
       String username = ConversationState.getCurrent().getIdentity().getUserId();      
       CalendarService calService = (CalendarService) PortalContainer.getInstance().getComponentInstanceOfType(CalendarService.class);
       CalendarSetting setting = calService.getCalendarSetting(username);
       return TimeZone.getTimeZone(setting.getTimeZone());
     } catch (Exception e) {
       if (LOG.isDebugEnabled())
         LOG.error("Can not get time zone from user setting ", e);
       return null ;
     }
   }
 
   /**
    * publish new event activity
    *
    * @param event
    * @param calendarId
    */
   public void savePublicEvent(CalendarEvent event, String calendarId) {
     publishActivity(event);
   }
 
   /**
    * update existing event activity by creating a new comment in activity
    *
    * @param oldEvent
    * @param newEvent
    * @param calendarId
    */
   public void updatePublicEvent(CalendarEvent oldEvent, CalendarEvent newEvent, String calendarId) {
     Map<String, String> messagesParams = buildParams(oldEvent, newEvent);
     if(messagesParams.size() > 0) {
       updateToActivity(newEvent, messagesParams);
     }
   }
 
   /**
    * publish new event activity
    *
    * @param newEvent
    * @param calendarId
    */
   public void updatePublicEvent(CalendarEvent newEvent, String calendarId) {
     publishActivity(newEvent);
   }
 
   /**
    * remove the event activity
    *
    * @param event
    * @param calendarId
    */
   public void deletePublicEvent(CalendarEvent event, String calendarId) {
     deleteActivity(event, calendarId) ;
   }
 }
