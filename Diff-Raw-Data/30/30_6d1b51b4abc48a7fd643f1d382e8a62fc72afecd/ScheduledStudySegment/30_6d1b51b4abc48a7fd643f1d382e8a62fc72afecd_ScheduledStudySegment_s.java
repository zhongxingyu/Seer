 package edu.northwestern.bioinformatics.studycalendar.domain;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.tools.Range;
 import edu.northwestern.bioinformatics.studycalendar.tools.MutableRange;
 import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 import org.hibernate.annotations.GenericGenerator;
 import org.hibernate.annotations.OrderBy;
 import org.hibernate.annotations.Parameter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Transient;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 /**
  * @author Rhett Sutphin
  */
 @Entity
 @GenericGenerator(name="id-generator", strategy = "native",
     parameters = {
         @Parameter(name="sequence", value="seq_scheduled_study_segment_id")
     }
 )
 public class ScheduledStudySegment extends AbstractMutableDomainObject {
     private final Logger log = LoggerFactory.getLogger(getClass());
     private ScheduledCalendar scheduledCalendar;
     private List<ScheduledActivity> events = new LinkedList<ScheduledActivity>();
     private Integer startDay;
     private Date startDate;
 
     private StudySegment studySegment;
 
     ////// LOGIC
 
     public void addEvent(ScheduledActivity event) {
         getActivities().add(event);
         event.setScheduledStudySegment(this);
     }
 
     @Transient
     public String getName() {
         Epoch epoch = getStudySegment().getEpoch();
         StringBuilder name = new StringBuilder(epoch == null ? "null" : epoch.getName());
         if (epoch != null && epoch.getStudySegments().size() > 1) {
             name.append(": ").append(getStudySegment().getName());
         }
 
         int selfIndex = -1;
         List<Integer> studySegmentRepeats = new LinkedList<Integer>();
         for (int i = 0; i < getScheduledCalendar().getScheduledStudySegments().size(); i++) {
             ScheduledStudySegment sibling = getScheduledCalendar().getScheduledStudySegments().get(i);
             if (sibling.getStudySegment().equals(this.getStudySegment())) studySegmentRepeats.add(i);
             if (sibling == this) selfIndex = i;
         }
         if (selfIndex == -1) throw new StudyCalendarSystemException("This scheduled studySegment is not a child of its parent");
 
         if (studySegmentRepeats.size() > 1) {
             name.append(" (").append(studySegmentRepeats.indexOf(selfIndex) + 1).append(')');
         }
 
         return name.toString();
     }
 
     @Transient
     public Range<Date> getDateRange() {
         Calendar endDate = Calendar.getInstance();
         endDate.setTime(getStartDate());
         endDate.add(Calendar.DATE, getStudySegment().getLengthInDays() - 1);
         MutableRange<Date> range = new MutableRange<Date>(getStartDate(), endDate.getTime());
         for (ScheduledActivity scheduledActivity : getActivities()) {
             range.add(scheduledActivity.getActualDate());
         }
         return range.immutable();
     }
 
     @Transient
     public SortedMap<Date, List<ScheduledActivity>> getActivitiesByDate() {
         SortedMap<Date, List<ScheduledActivity>> byDate = new TreeMap<Date, List<ScheduledActivity>>();
         List<ScheduledActivity> tempEvents = getActivities();
         List<ScheduledActivity> events = new LinkedList<ScheduledActivity>();
         List<ScheduledActivity> scheduledEvents = new LinkedList<ScheduledActivity>();
         //TODO : flatten the comparison between the events in compare method of the ScheduledActivity.
         for(ScheduledActivity tempEvent : tempEvents) {
             if((tempEvent.getPlannedActivity() == null) || (tempEvent.getRepetitionNumber() == null)) {
                 events.add(tempEvent);
             } else {
                 scheduledEvents.add(tempEvent);
             }
         }
         Collections.sort(scheduledEvents);
         for(ScheduledActivity scheduledEvent : scheduledEvents) {
             events.add(scheduledEvent);
         }
         Collections.sort(events);
         for (ScheduledActivity event : events) {
             Date key = event.getActualDate();
             if (!byDate.containsKey(key)) {
                 byDate.put(key, new LinkedList<ScheduledActivity>());
             }
             byDate.get(key).add(event);
         }
         return byDate;
     }
 
     @Transient
     // TODO: ?
     public Date getTodayDate() {
         Calendar c = Calendar.getInstance();
         c.add(Calendar.DATE, -1);
         Date dayBeforeToday = c.getTime();
         return dayBeforeToday;
     }
 
     @Transient
     public Date getNextStudySegmentPerProtocolStartDate() {
         Date origin = getStartDate();
         if (origin != null) {
             Calendar defaultState = Calendar.getInstance();
             defaultState.setTime(origin);
             defaultState.add(Calendar.DATE, getStudySegment().getLengthInDays());
             return defaultState.getTime();
         } else {
             return null;
         }
     }
 
     @Transient
     public boolean isComplete() {
         for (ScheduledActivity event : getActivities()) {
             if (event.getCurrentState().getMode().isOutstanding()) {
                 return false;
             }
         }
         return true;
     }
 
    public void unscheduleOutstandingEvents(String reason) {
        for (ScheduledActivity event : getActivities()) event.unscheduleIfOutstanding(reason);
     }
 
     public void unschedulePopulationEvents(String reason, Population toRemove) {
         for (ScheduledActivity event : getActivities()) {
             if ( (event.getPlannedActivity().getPopulation() != null) && (event.getPlannedActivity().getPopulation().equals(toRemove))) {
                 event.unscheduleIfOutstanding(reason);
             }
         }
     }
     ////// BEAN PROPERTIES
 
     @OneToMany(mappedBy = "scheduledStudySegment")
     @OrderBy(clause="ideal_date")
     @Cascade(value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN})
     public List<ScheduledActivity> getActivities() {
         return events;
     }
 
     public void setActivities(List<ScheduledActivity> events) {
         this.events = events;
     }
 
     // This is annotated this way so that the IndexColumn in the parent
     // will work with the bidirectional mapping
     @ManyToOne
     @JoinColumn(insertable=false, updatable=false, nullable=false)
     public ScheduledCalendar getScheduledCalendar() {
         return scheduledCalendar;
     }
 
     public void setScheduledCalendar(ScheduledCalendar scheduledCalendar) {
         this.scheduledCalendar = scheduledCalendar;
     }
 
     @ManyToOne
     public StudySegment getStudySegment() {
         return studySegment;
     }
 
     public void setStudySegment(StudySegment studySegment) {
         this.studySegment = studySegment;
     }
 
     public Integer getStartDay() {
         return startDay;
     }
 
     public void setStartDay(Integer startDay) {
         this.startDay = startDay;
     }
 
     public Date getStartDate() {
         return startDate;
     }
 
     public void setStartDate(Date startDate) {
         this.startDate = startDate;
     }
 
     ///// OBJECT METHODS
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append(getClass().getSimpleName()).append('[')
             .append("name=");
         if (getStudySegment() != null) {
             sb.append(getName());
         }
         return sb.append("; events=").append(getActivities())
             .append(']').toString();
     }
 }
