 package edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
 import org.apache.commons.beanutils.PropertyUtils;
 
 import java.lang.reflect.InvocationTargetException;
 
 import gov.nih.nci.cabig.ctms.domain.DomainObject;
 
 /**
  * @author Rhett Sutphin
  */
 public class BreadcrumbContext {
     private Study study;
     private PlannedCalendar plannedCalendar;
     private Epoch epoch;
     private Arm arm;
     private Period period;
     private PlannedEvent plannedEvent;
 
     private Participant participant;
     private StudyParticipantAssignment studyParticipantAssignment;
     private ScheduledCalendar scheduledCalendar;
     private ScheduledArm scheduledArm;
     private ScheduledEvent scheduledEvent;
 
     private Site site;
     private StudySite studySite;
    private Activity activity;
 
     public BreadcrumbContext() { }
 
     public static BreadcrumbContext create(DomainObject basis) {
         BreadcrumbContext context = new BreadcrumbContext();
         if (basis != null) {
             StringBuilder propertyName = new StringBuilder(basis.getClass().getSimpleName());
             propertyName.setCharAt(0, Character.toLowerCase(propertyName.charAt(0)));
             try {
                 PropertyUtils.setProperty(context, propertyName.toString(), basis);
             } catch (NoSuchMethodException e) {
                 throw new StudyCalendarSystemException("No setter for " + propertyName.toString() + " of type " + basis.getClass().getName(), e);
             } catch (IllegalAccessException e) {
                 throw new StudyCalendarSystemException("No setter for " + propertyName.toString() + " of type " + basis.getClass().getName(), e);
             } catch (InvocationTargetException e) {
                 throw new StudyCalendarSystemException("No setter for " + propertyName.toString() + " of type " + basis.getClass().getName(), e);
             }
         }
         return context;
     }
 
     public void setSite(Site site) {
         this.site = site;
     }
 
     public void setStudySite(StudySite studySite) {
         setSite(studySite.getSite());
         setStudy(studySite.getStudy());
         this.studySite = studySite;
     }
 
     public void setStudy(Study study) {
         this.study = study;
     }
 
     public void setPlannedCalendar(PlannedCalendar plannedCalendar) {
         setStudy(plannedCalendar.getStudy());
         this.plannedCalendar = plannedCalendar;
     }
 
     public void setEpoch(Epoch epoch) {
         setPlannedCalendar(epoch.getPlannedCalendar());
         this.epoch = epoch;
     }
 
     public void setArm(Arm arm) {
         setEpoch(arm.getEpoch());
         this.arm = arm;
     }
 
     public void setPeriod(Period period) {
         setArm(period.getArm());
         this.period = period;
     }
 
     public void setPlannedEvent(PlannedEvent plannedEvent) {
         if(plannedEvent != null) {
             setPeriod(plannedEvent.getPeriod());
         }
         this.plannedEvent = plannedEvent;
     }
 
     public void setParticipant(Participant participant) {
         this.participant = participant;
     }
 
     public void setStudyParticipantAssignment(StudyParticipantAssignment studyParticipantAssignment) {
         setParticipant(studyParticipantAssignment.getParticipant());
         setPlannedCalendar(studyParticipantAssignment.getStudySite().getStudy().getPlannedCalendar());
         this.studyParticipantAssignment = studyParticipantAssignment;
     }
 
     public void setScheduledCalendar(ScheduledCalendar scheduledCalendar) {
         setStudyParticipantAssignment(scheduledCalendar.getAssignment());
         this.scheduledCalendar = scheduledCalendar;
     }
 
     public void setScheduledArm(ScheduledArm scheduledArm) {
         setScheduledCalendar(scheduledArm.getScheduledCalendar());
         setArm(scheduledArm.getArm());
         this.scheduledArm = scheduledArm;
     }
 
     public void setScheduledEvent(ScheduledEvent scheduledEvent) {
         setScheduledArm(scheduledEvent.getScheduledArm());
         setPlannedEvent(scheduledEvent.getPlannedEvent());
         this.scheduledEvent = scheduledEvent;
     }
 
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

     // TODO: more setters, as needed
 
     ////// BEAN PROPERTIES
 
     public Site getSite() {
         return site;
     }
 
     public StudySite getStudySite() {
         return studySite;
     }
 
     public Study getStudy() {
         return study;
     }
 
     public PlannedCalendar getPlannedCalendar() {
         return plannedCalendar;
     }
 
     public Epoch getEpoch() {
         return epoch;
     }
 
     public Arm getArm() {
         return arm;
     }
 
     public Period getPeriod() {
         return period;
     }
 
     public PlannedEvent getPlannedEvent() {
         return plannedEvent;
     }
 
     public Participant getParticipant() {
         return participant;
     }
 
     public StudyParticipantAssignment getStudyParticipantAssignment() {
         return studyParticipantAssignment;
     }
 
     public ScheduledCalendar getScheduledCalendar() {
         return scheduledCalendar;
     }
 
     public ScheduledArm getScheduledArm() {
         return scheduledArm;
     }
 
     public ScheduledEvent getScheduledEvent() {
         return scheduledEvent;
     }

    public Activity getActivity() {
        return activity;
    }
 }
