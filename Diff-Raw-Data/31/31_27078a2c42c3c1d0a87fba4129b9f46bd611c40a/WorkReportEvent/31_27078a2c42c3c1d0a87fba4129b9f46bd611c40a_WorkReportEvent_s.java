 package cz.admin24.myachievo.web2.calendar;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.lang3.time.DateUtils;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.vaadin.ui.components.calendar.event.CalendarEvent;
 import com.vaadin.ui.components.calendar.event.CalendarEvent.EventChangeNotifier;
 
 import cz.admin24.myachievo.connector.http.dto.WorkReport;
 import cz.admin24.myachievo.web2.utils.RemainingTime;
 import cz.admin24.myachievo.web2.utils.TimesheetUtils;
 
 public class WorkReportEvent implements CalendarEvent, EventChangeNotifier {
 
     private static final long               serialVersionUID     = 1L;
     private final List<EventChangeListener> eventChangeListeners = Lists.newLinkedList();
     // this object could be exchanged if report is changed!
     private WorkReport                      report;
     private final int                       timeBase;
 
 
     public WorkReportEvent(WorkReport report, int timeBase) {
         this.report = report;
         this.timeBase = timeBase;
     }
 
 
     /**
      * Updates report and fires event changed
      *
      * @param report
      */
     public void setReport(WorkReport report) {
         this.report = report;
         fireEventChanged();
     }
 
 
     public WorkReport getWorkReport() {
         return report;
     }
 
 
     @Override
     public Date getStart() {
         Date day = report.getDate();
         return DateUtils.addMinutes(day, timeBase);
     }
 
 
     @Override
     public Date getEnd() {
         Calendar c = Calendar.getInstance();
         c.setTime(getStart());
         c.add(Calendar.HOUR, report.getHours());
         c.add(Calendar.MINUTE, report.getMinutes());
         return c.getTime();
     }
 
 
     @Override
     public String getCaption() {
         StringBuffer ret = new StringBuffer();
         ret.append(report.getHours());
         ret.append(":");
         if (report.getMinutes() < 10) {
             ret.append("0");
         }
         ret.append(report.getMinutes());
        ret.append(" ");
         ret.append(report.getRemark());
 
         return ret.toString();
     }
 
 
     @Override
     public String getDescription() {
         return report.toString();
     }
 
 
     @Override
     public String getStyleName() {
         return null;
     }
 
 
     @Override
     public boolean isAllDay() {
         return false;
     }
 
 
     @Override
     public void addEventChangeListener(EventChangeListener listener) {
         eventChangeListeners.add(listener);
     }
 
 
     @Override
     public void removeEventChangeListener(EventChangeListener listener) {
         eventChangeListeners.remove(listener);
     }
 
 
     private void fireEventChanged() {
         for (EventChangeListener listener : eventChangeListeners) {
             listener.eventChange(new EventChangeEvent(this));
         }
     }
 }
