 package org.motechproject.ghana.mtn.domain;
 
 import ch.lambdaj.Lambda;
 import org.apache.commons.collections.CollectionUtils;
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.annotate.JsonProperty;
 import org.ektorp.support.TypeDiscriminator;
 import org.joda.time.*;
 import org.motechproject.ghana.mtn.domain.vo.Day;
 import org.motechproject.ghana.mtn.domain.vo.Week;
 import org.motechproject.ghana.mtn.domain.vo.WeekAndDay;
 import org.motechproject.ghana.mtn.utils.DateUtils;
 import org.motechproject.model.MotechAuditableDataObject;
 import org.motechproject.server.messagecampaign.contract.CampaignRequest;
 import org.motechproject.util.DateUtil;
 
 import java.util.List;
 
 import static java.util.Arrays.asList;
 import static org.joda.time.DateTimeConstants.SATURDAY;
 import static org.motechproject.util.DateUtil.setTimeZone;
 
 @TypeDiscriminator("doc.type === 'Subscription'")
 public class Subscription extends MotechAuditableDataObject {
     @JsonProperty("type")
     private String type = "Subscription";
 
     private Subscriber subscriber;
     private ProgramType programType;
     private SubscriptionStatus status;
 
     private WeekAndDay startWeekAndDay;
     private WeekAndDay lastMsgSentWeekAndDay;
 
     private DateTime registrationDate;
     private DateTime billingStartDate;
     private DateUtils dateUtils = new DateUtils();
 
     public Subscription() {
     }
 
     @JsonIgnore
     public boolean isNotValid() {
         return !programType.isInRange(startWeekAndDay.getWeek().getNumber());
     }
 
     public Subscriber getSubscriber() {
         return subscriber;
     }
 
     public void setSubscriber(Subscriber subscriber) {
         this.subscriber = subscriber;
     }
 
     public ProgramType getProgramType() {
         return programType;
     }
 
     public void setProgramType(ProgramType programType) {
         this.programType = programType;
     }
 
     public SubscriptionStatus getStatus() {
         return status;
     }
 
     public void setStatus(SubscriptionStatus status) {
         this.status = status;
     }
 
     public WeekAndDay getStartWeekAndDay() {
         return startWeekAndDay;
     }
 
     public void setStartWeekAndDay(WeekAndDay startWeekAndDay) {
         this.startWeekAndDay = startWeekAndDay;
     }
 
     public DateTime getRegistrationDate() {
         return setTimeZone(registrationDate);
     }
 
     public void setRegistrationDate(DateTime registrationDate) {
         this.registrationDate = registrationDate;
     }
 
     public CampaignRequest createCampaignRequest() {
         return new CampaignRequest(subscriber.getNumber(), programType.getProgramName(), null, null);
     }
 
     public Week currentWeek() {
         DateTime registeredDateStartDayTime = dateUtils.startOfDay(registrationDate);
         DateTime currentDateStartDayTime = dateUtils.startOfDay(dateUtils.now());
         int daysDiff = new Period(registeredDateStartDayTime, currentDateStartDayTime, PeriodType.days()).getDays();
 
         if (daysDiff > 0) {
             int dayOfWeek = registeredDateStartDayTime.get(DateTimeFieldType.dayOfWeek());
             int daysToSaturday = (dayOfWeek == DateTimeConstants.SUNDAY) ? 6 : SATURDAY - dayOfWeek;
             int daysAfterFirstSaturday = daysDiff > daysToSaturday ? daysDiff - daysToSaturday : 0;
             int weeksAfterFirstSaturday = daysAfterFirstSaturday / 7 + (daysAfterFirstSaturday % 7 > 0 ? 1 : 0);
             return startWeekAndDay.getWeek().add(weeksAfterFirstSaturday);
         }
         return startWeekAndDay.getWeek();
     }
 
     public Day currentDay() {
         String day = dateUtils.now().dayOfWeek().getAsText();
         return Day.valueOf(day.toUpperCase());
     }
 
     public String programName() {
         return programType.getProgramName();
     }
 
     public void updateLastMessageSent() {
         lastMsgSentWeekAndDay = new WeekAndDay(currentWeek(), currentDay());
     }
 
     public WeekAndDay getLastMsgSentWeekAndDay() {
         return lastMsgSentWeekAndDay;
     }
 
     public void setLastMsgSentWeekAndDay(WeekAndDay lastMsgSentWeekAndDay) {
         this.lastMsgSentWeekAndDay = lastMsgSentWeekAndDay;
     }
 
     public boolean alreadySent(ProgramMessage subscriptionMessage) {
         return lastMsgSentWeekAndDay != null && subscriptionMessage.getWeekAndDay().isBefore(lastMsgSentWeekAndDay);
     }
 
     private DateTime cycleStartDate() {
         return dateUtils.startOfDay(new ProgramMessageCycle().nearestCycleDate(registrationDate));
     }
 
     public DateTime billingStartDate() {
         List<Integer> forDaysToMoveToFirstOfMonth = asList(29, 30, 31);
         DateTime billingStartDate = cycleStartDate();
         if (forDaysToMoveToFirstOfMonth.contains(billingStartDate.getDayOfMonth()))
             return billingStartDate.dayOfMonth().addToCopy(1).withDayOfMonth(1);
         return billingStartDate;
     }
 
     public Subscription updateStartCycleInfo() {
         this.getStartWeekAndDay().setDay(dateUtils.day(cycleStartDate()));
         this.billingStartDate = billingStartDate();
         return this;
     }
 
     public String subscriberNumber() {
         return subscriber.getNumber();
     }
 
     public DateTime getBillingStartDate() {
         return setTimeZone(billingStartDate);
     }
 
     public void setBillingStartDate(DateTime billingStartDate) {
         this.billingStartDate = billingStartDate;
     }
 
     @JsonIgnore
     public Boolean isCompleted() {
        return currentWeek().getNumber() >= programType.getMaxWeek() && Day.FRIDAY.equals(currentDay());
     }
 }
