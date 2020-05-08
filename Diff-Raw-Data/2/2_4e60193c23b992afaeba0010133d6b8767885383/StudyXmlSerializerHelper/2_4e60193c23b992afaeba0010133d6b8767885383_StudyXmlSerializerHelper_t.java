 package edu.northwestern.bioinformatics.studycalendar.xml.writers;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import edu.northwestern.bioinformatics.studycalendar.domain.*;
 import edu.northwestern.bioinformatics.studycalendar.domain.tools.TemplateTraversalHelper;
 import org.dom4j.Element;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 
 public class StudyXmlSerializerHelper {
 
     private ActivitySourceXmlSerializer activitySourceXmlSerializer;
 
     public Element generateSourcesElementWithActivities(Study study) {
         Collection<Activity> activities = findAllActivities(study);
         Collection<Source> sources = groupActivitiesBySource(activities);
         return activitySourceXmlSerializer.createElement(sources);
     }
 
     protected Collection<Activity> findAllActivities(Study study) {
         Collection<Activity> result = new HashSet<Activity>();
         for (PlannedActivity a : TemplateTraversalHelper.findChildren(study.getPlannedCalendar(), PlannedActivity.class)) {
             result.add(a.getActivity());
         }
 
         for (Parent p : TemplateTraversalHelper.findRootParentNodes(study)) {
             for (PlannedActivity a : TemplateTraversalHelper.findChildren(p, PlannedActivity.class)) {
                 result.add(a.getActivity());
             }
         }
 
         return result;
     }
 
     protected Collection<Source> groupActivitiesBySource(Collection<Activity> all) {
         List<Source> result = new ArrayList<Source>();
         for (Activity a : all) {
             if (!result.contains(a.getSource())) {
                 result.add(a.getSource().transientClone());
             }
             Source s = result.get(result.indexOf(a.getSource()));
             s.addActivity(a.transientClone());
         }
         return result;
     }
 
     public void replaceActivityReferencesWithCorrespondingDefinitions(Study study, Element eStudy) {
         Element eSource = eStudy.element("sources");
         if (eSource != null) {
             Collection<Source> sources = activitySourceXmlSerializer.readCollectionElement(eSource);
 
             Collection<Activity> activityRefs = findAllActivities(study);
             for (Activity ref : activityRefs) {
                 if (ref.getSource() == null) {
                     throw new StudyCalendarValidationException(MessageFormat.format("Source is missing for activity reference [code={0}; source=(MISSING)]", ref.getCode()));
                 }
 
                 Source foundSource = ref.getSource().findSourceWhichHasSameName(sources);
                 Activity foundActivityDef = ref.findActivityInCollectionWhichHasSameCode(foundSource.getActivities());
 
                 if (foundActivityDef == null) {
                     throw new StudyCalendarValidationException(MessageFormat.format("Problem resolving activity reference [code={0}; source={1}]", ref.getCode(), ref.getSource().getName()));
                 }
                 ref.updateActivity(foundActivityDef);
                ref.setProperties(new ArrayList<ActivityProperty>());
                 for (ActivityProperty p : (new ArrayList<ActivityProperty>(foundActivityDef.getProperties()))) {
                     ref.addProperty(p.clone());
                 }
             }
         }
     }
 
     public void setActivitySourceXmlSerializer(ActivitySourceXmlSerializer activitySourceXmlSerializer) {
         this.activitySourceXmlSerializer = activitySourceXmlSerializer;
     }
 }
