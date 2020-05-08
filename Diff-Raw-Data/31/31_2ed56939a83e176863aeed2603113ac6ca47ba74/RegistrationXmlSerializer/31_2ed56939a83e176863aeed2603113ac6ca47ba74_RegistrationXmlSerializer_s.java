 package edu.northwestern.bioinformatics.studycalendar.xml.writers;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
 import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
 import edu.northwestern.bioinformatics.studycalendar.service.presenter.Registration;
 import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
 import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
 import org.dom4j.Element;
 import org.springframework.beans.factory.annotation.Required;
 
 import java.util.Date;
 
 import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
 import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.SUBJECT;
 
 public class RegistrationXmlSerializer extends AbstractStudyCalendarXmlCollectionSerializer<Registration> {
     private SubjectXmlSerializer subjectXmlSerializer;
 
     protected XsdElement rootElement() { return XsdElement.REGISTRATION; }
     protected XsdElement collectionRootElement() { return XsdElement.REGISTRATIONS; }
 
     @Override
     public Element createElement(Registration reg, boolean inCollection) {
         throw new UnsupportedOperationException("Creating Registration element not allowed");
     }
 
     @Override
     public Registration readElement(Element elt) {
         if (elt == null) return null;
         validateElement(elt);
 
         Date date = REGISTRATION_DATE.fromDate(elt);
         String segmentId = REGISTRATION_FIRST_STUDY_SEGMENT_ID.from(elt);
         String subjCoordName = REGISTRATION_SUBJECT_COORDINATOR_NAME.from(elt);
         String desiredAssignId = REGISTRATION_DESIRED_ASSIGNMENT_ID.from(elt);
 
         Subject subject = subjectXmlSerializer.readElement(elt.element(SUBJECT.xmlName()));
 
         StudySegment segment = new StudySegment();
         segment.setGridId(segmentId);
         Registration reg = new Registration();
         PscUser subjCoord = null;
         if (subjCoordName != null) {
             subjCoord = AuthorizationObjectFactory.createPscUser(subjCoordName);
         }
         reg.setSubject(subject);
         reg.setFirstStudySegment(segment);
         reg.setDate(date);
         reg.setDesiredStudySubjectAssignmentId(desiredAssignId);
         reg.setStudySubjectCalendarManager(subjCoord);
 
         return reg;
     }
 
     private void validateElement(Element elt) {
         if (REGISTRATION_FIRST_STUDY_SEGMENT_ID.from(elt) == null) {
             throw new StudyCalendarValidationException(
                     "Registration first study segment id is required");
         } else if (REGISTRATION_DATE.from(elt) == null) {
             throw new StudyCalendarValidationException(
                     "Registration date is required");
         }
     }
 
     ////// Bean setters
 
     @Required
     public void setSubjectXmlSerializer(SubjectXmlSerializer subjectXmlSerializer) {
         this.subjectXmlSerializer = subjectXmlSerializer;
     }
 }
