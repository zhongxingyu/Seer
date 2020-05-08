 package edu.northwestern.bioinformatics.studycalendar.xml.writers;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import edu.northwestern.bioinformatics.studycalendar.domain.Source;
 import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
 import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
 import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
 import org.apache.commons.lang.StringUtils;
 import org.dom4j.Element;
 
 import static org.apache.commons.lang.StringUtils.isBlank;
 
 public class ActivityReferenceXmlSerializer extends AbstractStudyCalendarXmlSerializer<Activity> {
     @Override
     public Element createElement(Activity a) {
         if (a.getCode() == null) { throw new StudyCalendarValidationException("Activity code is required for serialization"); }
         if (a.getSource() == null) { throw new StudyCalendarValidationException("Activity source is required for serialization"); }
 
         Element aElt = XsdElement.ACTIVITY_REFERENCE.create();
         XsdAttribute.ACTIVITY_CODE.addTo(aElt, a.getCode());
         XsdAttribute.ACTIVITY_SOURCE.addTo(aElt, a.getSource().getNaturalKey());
         return aElt;
     }
 
     @Override
     public Activity readElement(Element element) {
         Activity activity = new Activity();
 
         String code = XsdAttribute.ACTIVITY_CODE.from(element);
         if (isBlank(code)) {
            throw new StudyCalendarValidationException("Activity code is required for %s", XsdElement.ACTIVITY_REFERENCE.xmlName());
         } else {
             activity.setCode(code);
         }
 
         String sourceName = XsdAttribute.ACTIVITY_SOURCE.from(element);
         if (isBlank(sourceName)) {
           throw new StudyCalendarValidationException("Source is required for %s", XsdElement.ACTIVITY_REFERENCE.xmlName());
         } else {
            Source source = new Source();
            source.setName(sourceName);
            activity.setSource(source);
         }
 
         return activity;
     }
 
     public boolean validateElement(Activity activity, Element element) {
         boolean valid = true;
         if (element == null && activity == null) {
             return true;
         } else if ((element != null && activity == null) || (activity != null && element == null)) {
             return false;
         } else if (!StringUtils.equals(activity.getCode(), XsdAttribute.ACTIVITY_CODE.from(element))) {
             valid = false;
         }
 
         if ((activity.getSource() == null && XsdAttribute.ACTIVITY_SOURCE.from(element) != null)
                 || (activity.getSource() != null && XsdAttribute.ACTIVITY_SOURCE.from(element) == null)
                 || (!StringUtils.equals(activity.getSource().getName(), XsdAttribute.ACTIVITY_SOURCE.from(element)))) {
             valid = false;
         }
 
 
         return valid;
     }
 }
