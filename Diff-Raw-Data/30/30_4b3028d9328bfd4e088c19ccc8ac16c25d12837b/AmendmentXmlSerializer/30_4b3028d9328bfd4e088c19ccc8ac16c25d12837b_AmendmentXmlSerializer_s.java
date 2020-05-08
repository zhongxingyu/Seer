 package edu.northwestern.bioinformatics.studycalendar.xml.writers;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
 import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
 import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
 import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
 import org.apache.commons.lang.StringUtils;
 import org.dom4j.Document;
 import org.dom4j.Element;
 
 import java.io.InputStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 public class AmendmentXmlSerializer extends AbstractStudyCalendarXmlSerializer<Amendment> {
     public static final String DATE = "date";
 
     public static final String MANDATORY = "mandatory";
     private static final String PREVIOUS_AMENDMENT_KEY = "previous-amendment-key";
 
     private Study study;
     private AmendmentDao amendmentDao;
 
     private boolean isDevelopmentAmendment = false;
 
     private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
 
 
     public Element createElement(Amendment amendment) {
         Element element = null;
         if (!isDevelopmentAmendment) {
             element = XsdElement.AMENDMENT.create();
         } else {
             element = XsdElement.DEVELOPMENT_AMENDMENT.create();
         }
         element.addAttribute(NAME, amendment.getName());
         element.addAttribute(DATE, formatter.format(amendment.getDate()));
         element.addAttribute(MANDATORY, Boolean.toString(amendment.isMandatory()));
         XsdAttribute.RELEASED_DATE.addToDateTime(element, amendment.getReleasedDate());
         XsdAttribute.UPDATED_DATE.addToDateTime(element, amendment.getUpdatedDate());
 
         if (amendment.getPreviousAmendment() != null) {
             element.addAttribute(PREVIOUS_AMENDMENT_KEY, amendment.getPreviousAmendment().getNaturalKey());
         }
 
         addDeltas(amendment, element);
 
         return element;
     }
 
     public Amendment readElement(Element element) {
         String name = element.attributeValue(NAME);
         Date date;
         try {
             date = formatter.parse(element.attributeValue(DATE));
         } catch (ParseException e) {
             throw new StudyCalendarValidationException("Could not parse date \"%s\", should be in format YYYY-MM-DD", element.attributeValue(DATE));
         }
 
         Amendment amendment = amendmentDao.getByNaturalKey(new Amendment.Key(date, name).toString(), study);
         if (amendment == null) {
             amendment = new Amendment();
             amendment.setName(name);
             amendment.setDate(date);
             amendment.setMandatory(Boolean.parseBoolean(element.attributeValue(MANDATORY)));
 
             String previousAmendmentKey = element.attributeValue(PREVIOUS_AMENDMENT_KEY);
             if (previousAmendmentKey != null) {
                 Amendment previousAmendment = findAmendment(study.getAmendmentsList(), previousAmendmentKey);
                 amendment.setPreviousAmendment(previousAmendment);
             }
 
             addDeltas(element, amendment);
         }
         return amendment;
     }
 
     protected Amendment findAmendment(final List<Amendment> amendments, final String amendmentKey) {
         for (Amendment amendment : amendments) {
             if (amendmentKey.equals(amendment.getNaturalKey())) {
                 return amendment;
             }
         }
         return null;
     }
 
     private void addDeltas(final Amendment amendment, Element element) {
         for (Delta delta : amendment.getDeltas()) {
             AbstractDeltaXmlSerializer serializer = getDeltaXmlSerializerFactory().createXmlSerializer(delta);
             Element eDelta = serializer.createElement(delta);
             element.add(eDelta);
         }
     }
 
 
     private void addDeltas(final Element element, Amendment amendment) {
         for (Object oDelta : element.elements()) {
             Element eDelta = (Element) oDelta;
             AbstractDeltaXmlSerializer serializer = getDeltaXmlSerializerFactory().createXmlSerializer(eDelta);
            Delta delta = serializer.readElement(eDelta);
            amendment.addDelta(delta);
         }
     }
 
 
     public void setAmendmentDao(AmendmentDao amendmentDao) {
         this.amendmentDao = amendmentDao;
     }
 
     public DeltaXmlSerializerFactory getDeltaXmlSerializerFactory() {
         DeltaXmlSerializerFactory factory = (DeltaXmlSerializerFactory) getBeanFactory().getBean("deltaXmlSerializerFactory");
         factory.setStudy(study);
         return factory;
     }
 
     public void setStudy(Study study) {
         this.study = study;
     }
 
     public boolean isDevelopmentAmendment() {
         return isDevelopmentAmendment;
     }
 
     public void setDevelopmentAmendment(final boolean developmentAmendment) {
         isDevelopmentAmendment = developmentAmendment;
     }
 
     public Date readLastModifiedDate(final InputStream in) {
         Document doc = deserializeDocument(in);
         Element elt = doc.getRootElement();
         return XsdAttribute.UPDATED_DATE.fromDateTime(elt);
 
     }
 
     public String validate(Amendment releasedAmendment, Element currAmendment) {
         String name = currAmendment.attributeValue(NAME);
         String gridId = currAmendment.attributeValue(ID);
 
 
         boolean mandatory = Boolean.parseBoolean(currAmendment.attributeValue(MANDATORY));
         StringBuffer errorMessageBuffer = new StringBuffer("");
 
 
         if (releasedAmendment.isMandatory() != mandatory
                 || !formatter.format(releasedAmendment.getDate()).equals(currAmendment.attributeValue(DATE))
                 || !StringUtils.equals(releasedAmendment.getName(), name)) {
             //fixme: saurabh add relased date also
             String errorMessage = String.format(
                     "\n\nA released amendment %s present in the system is not present in the imported document. " +
                             "\nThe relased amendment present in the system has following attributes:"
                     , releasedAmendment.getDisplayName());
 
             errorMessageBuffer.append(errorMessage);
             errorMessageBuffer.append(String.format("\n name: %s , mandatory: %s,date: %s\n", releasedAmendment.getName(),
                      releasedAmendment.isMandatory(), formatter.format(releasedAmendment.getDate())));
 
 
         } else {
             //check if amendments are identical or not
             if (currAmendment.elements().size() != releasedAmendment.getDeltas().size()) {
                 errorMessageBuffer.append(String.format("\nImported document and release amendment %s present in system must have identical number of deltas",
                         releasedAmendment.getDisplayName()));
 
             } else {
 
                 for (Object oDelta : currAmendment.elements()) {
                     Element eDelta = (Element) oDelta;
                     AbstractDeltaXmlSerializer serializer = getDeltaXmlSerializerFactory().createXmlSerializer(eDelta);
                     errorMessageBuffer.append(serializer.validate(releasedAmendment, eDelta));
                 }
             }
 
 
         }
 
         if (StringUtils.isEmpty(errorMessageBuffer.toString())) {
             return "";
         }
         //errorMessageBuffer.append(getErrorStringForDeltas(releasedAmendment));
 
        // errorMessageBuffer.append("\n Imported document has following amendment:\n" + currAmendment.asXML());
         errorMessageBuffer.append("\n\n Both amendment must be identical and they should appear in the same order.\n\n");
 
         return errorMessageBuffer.toString();
 
     }
 
     private String getErrorStringForDeltas(Amendment amendment) {
 
         StringBuffer errorMessageBuffer = new StringBuffer();
         if (amendment.getDeltas() != null && !amendment.getDeltas().isEmpty()) {
             errorMessageBuffer.append(String.format("\nDeltas of the  amendment %s present in the system: \n", amendment.getDisplayName()));
         }else{
             errorMessageBuffer.append(String.format("\nAmendment %s present in the system does not have any delta\n", amendment.getDisplayName()));
 
         }
 
         for (Delta delta : amendment.getDeltas()) {
             errorMessageBuffer.append(String.format("delta: %s - grid_id = %s, node_id=:%s \n", delta.getClass().getSimpleName(), delta.getGridId(), delta.getNode().getGridId()));
             List<Change> changes = delta.getChanges();
             for (int i = 0; i < changes.size(); i++) {
                 Change change = changes.get(0);
                 if (i == 0) {
                     errorMessageBuffer.append("Changes : \n");
 
                 }
                 errorMessageBuffer.append(String.format("%s, grid_id=%s", change.toString(), change.getGridId()));
 
             }
             errorMessageBuffer.append("\n");
         }
 
         return errorMessageBuffer.toString();
     }
 
     public String validateDevelopmentAmendment(Element developmentAmendment) {
         String name = developmentAmendment.attributeValue(NAME);
 
 
         boolean mandatory = Boolean.parseBoolean(developmentAmendment.attributeValue(MANDATORY));
         StringBuffer errorMessageBuffer = new StringBuffer("");
 
         for (Amendment releasedAmendment : study.getAmendmentsList()) {
             if (releasedAmendment.isMandatory() == mandatory
                     && StringUtils.equals(releasedAmendment.getName(), name)
                     && formatter.format(releasedAmendment.getDate()).equals(developmentAmendment.attributeValue(DATE))) {
 
                 String errorMessage = String.format(
                         "\n\nA released amendment %s present in the system matches with development amendment present in  imported document. " +
                                 "\nThe relased amendment present in the system has following attributes:"
                         , releasedAmendment.getDisplayName());
 
                 errorMessageBuffer.append(errorMessage);
                 errorMessageBuffer.append(String.format("\n name: %s ,mandatory: %s,date: %s \n", releasedAmendment.getName(),
                          releasedAmendment.isMandatory(), formatter.format(releasedAmendment.getDate())));
                
                 errorMessageBuffer.append("\n Imported document must not have any development amendment which matches with any relased amendment present in system.");
 
 
                 break;
 
             }
         }
 
         return errorMessageBuffer.toString();
 
 
     }
 }
