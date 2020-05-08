 package edu.northwestern.bioinformatics.studycalendar.xml.writers;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
 import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
 import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.SCHEDULED_CALENDAR_ASSIGNMENT_ID;
 import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.SCHEDULED_CALENDAR_ID;
 import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
 import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.SCHEDULED_CALENDAR;
 import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.SCHEDULED_CALENDARS;
 import org.dom4j.Element;
 import org.springframework.beans.factory.annotation.Required;
 
 /**
  * @author John Dzak
  */
 public class ScheduledCalendarXmlSerializer extends AbstractStudyCalendarXmlSerializer<ScheduledCalendar> {
     private ScheduledStudySegmentXmlSerializer segmentSerialzier;
 
     protected XsdElement rootElement() { return SCHEDULED_CALENDAR; }
     protected XsdElement collectionRootElement() { return SCHEDULED_CALENDARS; }
 
     @Override
     public Element createElement(ScheduledCalendar scheduledCalendar) {
         Element elt = SCHEDULED_CALENDAR.create();
         if (scheduledCalendar.getGridId() != null)
             SCHEDULED_CALENDAR_ID.addTo(elt, scheduledCalendar.getGridId());
         if (scheduledCalendar.getAssignment() != null)
             SCHEDULED_CALENDAR_ASSIGNMENT_ID.addTo(elt, scheduledCalendar.getAssignment().getGridId());
 
         for (ScheduledStudySegment segment :  scheduledCalendar.getScheduledStudySegments()) {
             elt.add(segmentSerialzier.createElement(segment));
         }
         return elt;
     }
 
     @Override
     public ScheduledCalendar readElement(Element element) {
         throw new UnsupportedOperationException("Functionality to read a schedule element does not exist");
     }
 
     @Required
     public void setScheduledStudySegmentXmlSerializer(ScheduledStudySegmentXmlSerializer segmentSerialzier) {
         this.segmentSerialzier = segmentSerialzier;
     }
 }
