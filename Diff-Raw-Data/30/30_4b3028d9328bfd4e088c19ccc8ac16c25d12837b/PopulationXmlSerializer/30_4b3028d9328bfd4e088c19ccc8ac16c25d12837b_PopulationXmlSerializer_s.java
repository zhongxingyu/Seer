 package edu.northwestern.bioinformatics.studycalendar.xml.writers;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Population;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
 import org.dom4j.Element;
 import org.apache.commons.lang.StringUtils;
 
 public class PopulationXmlSerializer extends AbstractStudyCalendarXmlSerializer<Population> implements StatefulTemplateXmlSerializer<Population> {
     public static final String ABBREVIATION = "abbreviation";
     public static final String NAME = "name";
     public static final String POPULATION = "population";
     private static final String POPULATION_ABBREVIATION_IS_INVALID_MESSAGE = "Population's abbreviation '%s' is invalid. Please make sure abbreviation don't contain spaces and less then 5 characters long.";
 
     private PopulationDao populationDao;
     private Study study;
 
     public Element createElement(Population child) {
         return element(POPULATION)
                 .addAttribute(ABBREVIATION, child.getAbbreviation())
                 .addAttribute(NAME, child.getName());
     }
 
     public Population readElement(Element element) {
         String abbreviation = element.attributeValue(ABBREVIATION);
         validateElement(abbreviation);
         Population population = load(abbreviation);
         if (population == null) {
             population = new Population();
             population.setAbbreviation(abbreviation);
             population.setName(element.attributeValue(NAME));
             population.setStudy(study);
         }
         return population;
     }
 
     private Population load(String abbreviation) {
         if (study.getId() == null) {
             return null;
         } else {
             return populationDao.getByAbbreviation(study, abbreviation);
         }
     }
 
     public void setPopulationDao(PopulationDao populationDao) {
         this.populationDao = populationDao;
     }
 
     public void setStudy(Study study) {
         this.study = study;
     }
 
     private void validateElement(String abbreviation) {
         StringBuffer errorMessageBuffer = new StringBuffer("");
 
         if (abbreviation != null && (abbreviation.contains(" ") || abbreviation.length() > 5)) {
             String errorMessage = String.format(POPULATION_ABBREVIATION_IS_INVALID_MESSAGE, abbreviation);
             errorMessageBuffer.append(errorMessage);
         }
 
         if (!StringUtils.isEmpty(errorMessageBuffer.toString())) {
             log.error(errorMessageBuffer.toString());
 
             StudyImportException studyImportException = new StudyImportException(errorMessageBuffer.toString());
 
             throw studyImportException;
         }
     }
 
 }
