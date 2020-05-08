 package org.motechproject.ghana.telco.domain;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.annotate.JsonProperty;
 import org.ektorp.support.TypeDiscriminator;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeConstants;
 import org.joda.time.DateTimeFieldType;
 import org.motechproject.ghana.telco.domain.vo.WeekAndDay;
 import org.motechproject.ghana.telco.utils.DateUtils;
 import org.motechproject.model.DayOfWeek;
 import org.motechproject.model.MotechAuditableDataObject;
 import org.motechproject.model.Time;
 import org.motechproject.server.messagecampaign.contract.CampaignRequest;
 
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
     @JsonProperty("lastMsgSentWeekAndDay")
     private WeekAndDay lastMsgSentWeekAndDay;
 
     private DateTime registrationDate;
     private DateTime cycleStartDate;
     private DateUtils dateUtils = new DateUtils();
     @JsonProperty("subscriptionEndDate")
     private DateTime subscriptionEndDate;
 
     public Subscription() {
     }
 
     public Subscription(Subscriber subscriber, ProgramType programType, SubscriptionStatus status, WeekAndDay startWeekAndDay, DateTime registrationDate) {
         this.subscriber = subscriber;
         this.programType = programType;
         this.status = status;
         this.startWeekAndDay = startWeekAndDay;
         this.registrationDate = registrationDate;
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
 
     public Subscription setStatus(SubscriptionStatus status) {
         this.status = status;
         return this;
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
 
     public CampaignRequest createCampaignRegistrationRequest() {
         return new CampaignRequest(subscriber.getNumber(), programType.getProgramKey(), reminderTime(), getRegistrationDate().toLocalDate(), startWeekAndDay.getWeek().getNumber());
     }
 
     private Time reminderTime() {
        cycleStartDate=cycleStartDate.plusMinutes(1);
        return new Time(cycleStartDate.get(DateTimeFieldType.hourOfDay()), cycleStartDate.get(DateTimeFieldType.minuteOfHour()));
     }
 
     public CampaignRequest createCampaignRequest() {
         return new CampaignRequest(subscriber.getNumber(), programType.getProgramKey(), null, cycleStartDate.toLocalDate(), startWeekAndDay.getWeek().getNumber());
     }
 
     private int daysToSaturday(DateTime cycleStartDateWithStartDayTime) {
         int dayOfWeek = cycleStartDateWithStartDayTime.get(DateTimeFieldType.dayOfWeek());
         return (dayOfWeek == DateTimeConstants.SUNDAY) ? 6 : SATURDAY - dayOfWeek;
     }
 
     private DateTime cycleStartDate(ProgramMessageCycle programMessageCycle) {
         return programMessageCycle.nearestCycleDate(this);
     }
 
     public Subscription updateCycleInfo(ProgramMessageCycle programMessageCycle) {
         updateStartCycle(programMessageCycle);
         updateCycleEndDate();
         return this;
     }
 
     private void updateStartCycle(ProgramMessageCycle programMessageCycle) {
         DateTime startDateOfCycle = cycleStartDate(programMessageCycle);
         this.getStartWeekAndDay().setDay(dateUtils.day(startDateOfCycle));
         this.cycleStartDate = startDateOfCycle;
     }
 
     private void updateCycleEndDate() {
         int daysToFirstSaturday = daysToSaturday(this.cycleStartDate);
         Integer weeksRemaining = programType.getMaxWeek() - startWeekAndDay.getWeek().getNumber();
         this.subscriptionEndDate = this.cycleStartDate.dayOfMonth().addToCopy(daysToFirstSaturday + weeksRemaining * 7);
     }
 
     public DayOfWeek currentDay() {
         String day = dateUtils.now().dayOfWeek().getAsText();
         return DayOfWeek.valueOf(day);
     }
 
     public String programName() {
         return programType.getProgramName();
     }
 
     public String programKey() {
         return programType.getProgramKey();
     }
 
     public String subscriberNumber() {
         return subscriber.getNumber();
     }
 
     public DateTime getCycleStartDate() {
         return setTimeZone(cycleStartDate);
     }
 
     public void setCycleStartDate(DateTime cycleStartDate) {
         this.cycleStartDate = cycleStartDate;
     }
 
     public Boolean canRollOff() {
         return programType.canRollOff();
     }
 
     public ProgramType rollOverProgramType() {
         return programType.getRollOverProgramType();
     }
 
     public DateTime getSubscriptionEndDate() {
         return subscriptionEndDate;
     }
 }
