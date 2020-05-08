 package edu.northwestern.bioinformatics.studycalendar.web.template;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.Period;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
 import edu.northwestern.bioinformatics.studycalendar.domain.tools.DayRange;
 import edu.northwestern.bioinformatics.studycalendar.domain.tools.DefaultDayRange;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
 import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
 import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
 import gov.nih.nci.cabig.ctms.lang.ComparisonTools;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.BeanWrapperImpl;
 import org.springframework.validation.Errors;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * @author Rhett Sutphin
  */
 public class EditPeriodCommand implements PeriodCommand {
     private static final Collection<String> PROPERTIES_TO_UPDATE
         = Arrays.asList("name", "startDay", "duration.quantity", "duration.unit", "repetitions");
 
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     private Period period;
     private Period originalPeriod;
     private AmendmentService amendmentService;
     private TemplateService templateService;
 
     public EditPeriodCommand(Period period, AmendmentService amendmentService, TemplateService templateService) {
         this.originalPeriod = period;
         this.period = (Period) period.transientClone();
         this.amendmentService = amendmentService;
         this.templateService = templateService;
     }
 
     public Collection<ResourceAuthorization> authorizations(Errors bindErrors) {
         return ResourceAuthorization.createTemplateManagementAuthorizations(
             originalPeriod == null ? null : templateService.findStudy(originalPeriod),
             PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER);
     }
 
     public Period getPeriod() {
         return period;
     }
 
     public StudySegment getStudySegment() {
         return templateService.findParent(getPeriod());
     }
 
     public boolean apply() {
         List<Change> changes = new ArrayList<Change>();
        updateRevWithChangedProperties(changes);
         removeInvalidPlannedActivities(changes);
         if (!changes.isEmpty()) {
             amendmentService.updateDevelopmentAmendment(originalPeriod,
                 changes.toArray(new Change[changes.size()]));
         }
         return false;
     }
 
     private void updateRevWithChangedProperties(List<Change> target) {
         BeanWrapper originalWrapped = new BeanWrapperImpl(originalPeriod);
         BeanWrapper newWrapped = new BeanWrapperImpl(period);
         for (String prop : PROPERTIES_TO_UPDATE) {
             Object oldV = originalWrapped.getPropertyValue(prop);
             Object newV = newWrapped.getPropertyValue(prop);
             if (!ComparisonTools.nullSafeEquals(oldV, newV)) {
                 target.add(PropertyChange.create(prop, oldV, newV));
             }
         }
     }
 
     private void removeInvalidPlannedActivities(List<Change> target) {
         // look for PlannedActivity that are now invalid
         DayRange peDayRange = new DefaultDayRange(1, getPeriod().getDuration().getDays());
         for (PlannedActivity event : originalPeriod.getPlannedActivities()) {
             if (!peDayRange.containsDay(event.getDay())) {
                 target.add(Remove.create(event));
             }
         }
     }
 }
