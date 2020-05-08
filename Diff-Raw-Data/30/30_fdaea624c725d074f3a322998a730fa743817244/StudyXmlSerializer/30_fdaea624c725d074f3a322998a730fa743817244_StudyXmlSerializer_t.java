 package edu.northwestern.bioinformatics.studycalendar.xml.writers;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.Population;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
 import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
 import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
 import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.LAST_MODIFIED_DATE;
 import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.STUDY_ASSIGNED_IDENTIFIER;
 import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
 import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.*;
 import org.apache.commons.lang.StringUtils;
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.springframework.beans.factory.annotation.Required;
 
 import java.io.InputStream;
 import java.util.Date;
 import java.util.List;
 
 public class StudyXmlSerializer extends AbstractStudyCalendarXmlSerializer<Study> {
 
     private StudyDao studyDao;
     private StudyService studyService;
    private PlannedCalendarDao plannedCalendarDao;
 
     public Element createElement(Study study) {
         Element elt = XsdElement.STUDY.create();
         STUDY_ASSIGNED_IDENTIFIER.addTo(elt, study.getAssignedIdentifier());
         LAST_MODIFIED_DATE.addToDateTime(elt, study.getLastModifiedDate());
 
         Element eCalendar = getPlannedCalendarXmlSerializer(study).createElement(study.getPlannedCalendar());
 
         elt.add(eCalendar);
 
 //        for (Population population : study.getPopulations()) {
 //            Element ePopulation = getPopulationXmlSerializer(study).createElement(population);
 //            elt.add(ePopulation);
 //        }
 
         for (Amendment amendment : study.getAmendmentsList()) {
             Element eAmendment = getAmendmentSerializer(study).createElement(amendment);
             elt.add(eAmendment);
         }
 
         if (study.getDevelopmentAmendment() != null) {
             Amendment developmentAmendment = study.getDevelopmentAmendment();
             Element developmentAmendmentElement = getDevelopmentAmendmentSerializer(study).createElement(developmentAmendment);
             elt.add(developmentAmendmentElement);
         }
 
         return elt;
     }
 
     @SuppressWarnings({"unchecked"})
     public Study readElement(Element element) {
         validateElement(element);
 
         String key = XsdAttribute.STUDY_ASSIGNED_IDENTIFIER.from(element);
         Study study = studyDao.getByAssignedIdentifier(key);
 
         if (study == null) {
             study = new Study();
             study.setAssignedIdentifier(key);
             Element eCalendar = element.element(PlannedCalendarXmlSerializer.PLANNED_CALENDAR);
             PlannedCalendar calendar = getPlannedCalendarXmlSerializer(study).readElement(eCalendar);
            if (plannedCalendarDao.getByGridId(calendar.getGridId()) != null) {
                throw new StudyCalendarValidationException ("The planned calendar with the same grid-ID already exists. Please check the xml file and modify the IDs appropriately.");
            }
             study.setPlannedCalendar(calendar);
         }
 
         PopulationXmlSerializer populationXmlSerializer = getPopulationXmlSerializer(study);
         for (Object oPopulation : element.elements(PopulationXmlSerializer.POPULATION)) {
             Element ePopulation = (Element) oPopulation;
             Population population = populationXmlSerializer.readElement(ePopulation);
             if (!study.getPopulations().contains(population)) {
                 study.addPopulation(population);
             }
         }
 
         List<Element> eAmendments = element.elements(XsdElement.AMENDMENT.xmlName());
 
         Element currAmendment = findOriginalAmendment(eAmendments);
         while (currAmendment != null) {
             Amendment amendment = getAmendmentSerializer(study).readElement(currAmendment);
             if (!study.getAmendmentsList().contains(amendment)) {
                 study.pushAmendment(amendment);
             }
             currAmendment = findNextAmendment(currAmendment, eAmendments);
         }
 
         Element developmentAmendmentElement = element.element(XsdElement.DEVELOPMENT_AMENDMENT.xmlName());
         if (developmentAmendmentElement != null) {
             Amendment developmentAmendment = getDevelopmentAmendmentSerializer(study).readElement(developmentAmendmentElement);
             study.setDevelopmentAmendment(developmentAmendment);
         }
 
         return study;
     }
 
     public boolean validate(Element element) {
 
         String key = XsdAttribute.STUDY_ASSIGNED_IDENTIFIER.from(element);
 
         //initialize the study
         Study study = studyService.getStudyByAssignedIdentifier(key);
         if (study != null) {
             List<Element> eAmendments = element.elements(XsdElement.AMENDMENT.xmlName());
 
             Element currAmendment = findOriginalAmendment(eAmendments);
 
             StringBuffer errorMessageBuffer = new StringBuffer("");
 
             List<Amendment> list = study.getAmendmentsListInReverseOrder();
             if (list.size() > eAmendments.size()) {
                 String errorMessage = String.format("Imported document must have all released amendment presents in system. Study present in system has %s number of released amendments",
                         list.size());
                 errorMessageBuffer.append(errorMessage);
 
             } else {
                 for (Amendment amendment : list) {
 
                     errorMessageBuffer.append(getAmendmentSerializer(study).validate(amendment, currAmendment));
 
                     currAmendment = findNextAmendment(currAmendment, eAmendments);
                 }
             }
             Element developmentAmendmentElement = element.element(XsdElement.DEVELOPMENT_AMENDMENT.xmlName());
             if (developmentAmendmentElement != null) {
                 errorMessageBuffer.append(getDevelopmentAmendmentSerializer(study).validateDevelopmentAmendment(developmentAmendmentElement));
             }
 
             if (!StringUtils.isEmpty(errorMessageBuffer.toString())) {
                 log.error(errorMessageBuffer.toString());
 
                 StudyImportException studyImportException = new StudyImportException(errorMessageBuffer.toString());
 
                 throw studyImportException;
             }
 
         }
 
         return true;
     }
 
 
     private void validateElement(Element element) {
         if (element.getName() != null && (!element.getName().equals(STUDY.xmlName()))) {
             throw new StudyCalendarValidationException("Element type is other than <study>");
         } else if (element.elements(AMENDMENT.xmlName()).isEmpty() && element.element(DEVELOPMENT_AMENDMENT.xmlName()) == null) {
             throw new StudyCalendarValidationException("<study> must have at minimum an <amendment> or <development-amendment> child");
         }
     }
 
     private Element findNextAmendment(Element currAmendment, List<Element> eAmendments) {
         String name = XsdAttribute.AMENDMENT_NAME.from(currAmendment);
         Date date = XsdAttribute.AMENDMENT_DATE.fromDate(currAmendment);
         String key = new Amendment.Key(date, name).toString();
 
         for (Element amend : eAmendments) {
             if (key.equals(XsdAttribute.AMENDMENT_PREVIOUS_AMENDMENT_KEY.from(amend))) return amend;
         }
         return null;
     }
 
     private Element findOriginalAmendment(List<Element> eAmendments) {
         for (Element amend : eAmendments) {
             if (XsdAttribute.AMENDMENT_PREVIOUS_AMENDMENT_KEY.from(amend) == null) return amend;
         }
         return null;
     }
 
     public String readAssignedIdentifier(InputStream in) {
         Document doc = deserializeDocument(in);
         Element elt = doc.getRootElement();
         validateElement(elt);
         return XsdAttribute.STUDY_ASSIGNED_IDENTIFIER.from(elt);
     }
 
     protected PlannedCalendarXmlSerializer getPlannedCalendarXmlSerializer(Study study) {
         PlannedCalendarXmlSerializer plannedCalendarXmlSerializer = (PlannedCalendarXmlSerializer) getBeanFactory().getBean("plannedCalendarXmlSerializer");
         plannedCalendarXmlSerializer.setStudy(study);
         return plannedCalendarXmlSerializer;
     }
 
     protected PopulationXmlSerializer getPopulationXmlSerializer(Study study) {
         PopulationXmlSerializer populationXmlSerializer = (PopulationXmlSerializer) getBeanFactory().getBean("populationXmlSerializer");
         populationXmlSerializer.setStudy(study);
         return populationXmlSerializer;
     }
 
     protected StudyXmlSerializer getStudyXmlSerializer(Study study) {
         StudyXmlSerializer studyXmlSerializer = (StudyXmlSerializer) getBeanFactory().getBean("studyXmlSerializer");
         return studyXmlSerializer;
     }
 
     protected AmendmentXmlSerializer getAmendmentSerializer(Study study) {
         AmendmentXmlSerializer amendmentSerializer = (AmendmentXmlSerializer) getBeanFactory().getBean("amendmentXmlSerializer");
         amendmentSerializer.setStudy(study);
         amendmentSerializer.setDevelopmentAmendment(false);
         return amendmentSerializer;
     }
 
     protected AmendmentXmlSerializer getDevelopmentAmendmentSerializer(Study study) {
         AmendmentXmlSerializer amendmentSerializer = (AmendmentXmlSerializer) getBeanFactory().getBean("amendmentXmlSerializer");
         amendmentSerializer.setStudy(study);
         amendmentSerializer.setDevelopmentAmendment(true);
         return amendmentSerializer;
     }
 
     @Required
     public void setStudyDao(StudyDao studyDao) {
         this.studyDao = studyDao;
     }
 
     @Required
    public void setPlannedCalendarDao(PlannedCalendarDao plannedCalendarDao) {
        this.plannedCalendarDao = plannedCalendarDao;
    }

    @Required
     public void setStudyService(StudyService studyService) {
         this.studyService = studyService;
     }
 }
