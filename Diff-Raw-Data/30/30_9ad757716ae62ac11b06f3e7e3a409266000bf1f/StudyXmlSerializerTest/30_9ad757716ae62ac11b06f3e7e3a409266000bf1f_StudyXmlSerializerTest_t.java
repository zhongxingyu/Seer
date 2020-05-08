 package edu.northwestern.bioinformatics.studycalendar.xml.writers;
 
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
 import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.Population;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
 import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
 import static edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer.*;
 import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.STUDY;
 import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
 import static org.dom4j.DocumentHelper.*;
 import org.dom4j.Element;
 import org.dom4j.QName;
 import static org.easymock.EasyMock.expect;
 
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 public class StudyXmlSerializerTest extends StudyCalendarXmlTestCase {
     private StudyXmlSerializer serializer;
     private Study study;
     private Element element;
     private PlannedCalendar calendar;
     private StudyDao studyDao;
    private PlannedCalendarDao plannedCalendarDao;
     private PopulationXmlSerializer populationSerializer;
     private Population population;
     private Element ePopulation;
     private PlannedCalendarXmlSerializer plannedCalendarSerializer;
     private AmendmentXmlSerializer amendmentSerializer;
     private AmendmentXmlSerializer developmentAmendmentSerializer;
     private Amendment firstAmendment;
     private Amendment developmentAmendment;
     private Element eFirstAmendment;
     private Element eDevelopmentAmendment;
     private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
     private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
 
     private Element eCalendar;
     private Amendment secondAmendment;
     private Element eSecondAmendment;
 
 
     protected void setUp() throws Exception {
         super.setUp();
 
         element = registerMockFor(Element.class);
         studyDao = registerDaoMockFor(StudyDao.class);
        plannedCalendarDao = registerDaoMockFor(PlannedCalendarDao.class);
         amendmentSerializer = registerMockFor(AmendmentXmlSerializer.class);
         populationSerializer = registerMockFor(PopulationXmlSerializer.class);
         plannedCalendarSerializer = registerMockFor(PlannedCalendarXmlSerializer.class);
         developmentAmendmentSerializer = registerMockFor(AmendmentXmlSerializer.class);
 
         serializer = new StudyXmlSerializer() {
             protected PlannedCalendarXmlSerializer getPlannedCalendarXmlSerializer(Study study) {
                 return plannedCalendarSerializer;
             }
 
             protected PopulationXmlSerializer getPopulationXmlSerializer(Study study) {
                 return populationSerializer;
             }
 
             public AmendmentXmlSerializer getAmendmentSerializer(Study study) {
                 return amendmentSerializer;
             }
 
             protected AmendmentXmlSerializer getDevelopmentAmendmentSerializer(Study study) {
                 return developmentAmendmentSerializer;
             }
         };
         serializer.setStudyDao(studyDao);
        serializer.setPlannedCalendarDao(plannedCalendarDao);
 
         calendar = setGridId("grid1", new PlannedCalendar());
         population = createPopulation("MP", "My Population");
 
         firstAmendment = createAmendment("[First]", createDate(2008, Calendar.JANUARY, 2), true);
 
         secondAmendment = createAmendment("[Second]", createDate(2008, Calendar.JANUARY, 11), true);
         secondAmendment.setPreviousAmendment(firstAmendment);
 
         developmentAmendment = createAmendment("[Development]", createDate(2008, Calendar.FEBRUARY, 13), true);
         developmentAmendment.setReleasedDate(null);
 
         study = createStudy();
 
         eCalendar = createCalendarElement(calendar);
         ePopulation = createPopulationElement(population);
         eFirstAmendment = createAmendmentElement(firstAmendment);
         eSecondAmendment = createAmendmentElement(secondAmendment);
         eDevelopmentAmendment = createDevelopmentAmendmentElement(developmentAmendment);
     }
 
     public void testReadElementWhereElementIsNew() {
         expectResolveStudy("Study A", null);
         expectDeserializePopulation();
         expectDeserializePlannedCalendar();
         expectDeserializeAmendments();
         expectDesearializeDevelopmentAmendment();
        expect(plannedCalendarDao.getByGridId(calendar.getGridId())).andReturn(null);
         replayMocks();
 
         Study actual = serializer.readElement(createStudyElement());
         verifyMocks();
 
         assertEquals("Wrong assigned identifier", "Study A", actual.getAssignedIdentifier());
         assertSame("PlannedCalendar should be the same", calendar, actual.getPlannedCalendar());
         assertSame("Populations should be the same", population, actual.getPopulations().iterator().next());
         assertSame("Wrong first amendment", firstAmendment, actual.getAmendment().getPreviousAmendment());
         assertSame("Wrong second amendment", secondAmendment, actual.getAmendment());
         assertSame("Wrong development amendment", developmentAmendment, actual.getDevelopmentAmendment());
     }
 
     public void testReadElementWithInvalidElementName() {
         expect(element.getName()).andReturn("study-snapshot").times(2);
         try {
             replayMocks();
             serializer.readElement(element);
             fail("Exception should be thrown");
             verifyMocks();
         } catch (StudyCalendarValidationException e) {
             assertEquals("Element type is other than <study>", e.getMessage());
         }
     }
 
     public void testReadElementWithNoDevelopmentOrReleasedAmendments() {
         Element eStudy = createStudyElement();
         eStudy.remove(eFirstAmendment);
         eStudy.remove(eSecondAmendment);
         eStudy.remove(eDevelopmentAmendment);
 
         try {
             replayMocks();
             serializer.readElement(eStudy);
             fail("Exception should be thrown");
             verifyMocks();
         } catch (StudyCalendarValidationException e) {
             assertEquals("<study> must have at minimum an <amendment> or <development-amendment> child", e.getMessage());
         }
     }
 
     public void testReadElementWhereElementExists() {
         expectResolveStudy("Study A", study);
         expectDeserializeAmendments();
         expectDeserializePopulation();
         expectDesearializeDevelopmentAmendment();
         replayMocks();
 
         Study actual = serializer.readElement(createStudyElement());
         verifyMocks();
 
         assertSame("Wrong Study", study, actual);
     }
 
     public void testCreateElement() {
         expectChildrenSerializers();
         replayMocks();
 
         Element actual = serializer.createElement(study);
         verifyMocks();
 
         assertEquals("Wrong attribute size", 2, actual.attributeCount());
         assertEquals("Wrong assigned identifier", "Study A", actual.attribute("assigned-identifier").getValue());
         assertEquals("Wrong last modified date", dateTimeFormat.format(study.getLastModifiedDate()), actual.attribute("last-modified-date").getValue());
 
         assertEquals("Should have planned calendar child and development-amendment", 3, actual.nodeCount());
         assertNotNull("Planned calendar should exist", actual.element("planned-calendar"));
         assertNotNull("Amendment should exist", actual.element("amendment"));
         assertNotNull("Development Amendment should exist", actual.element("development-amendment"));
 
     }
 
     public void testCreateDocumentString() throws Exception {
 
         StringBuffer expected = new StringBuffer();
         expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
         expected.append("<study ");
         expected.append(MessageFormat.format("       {0}=\"{1}\"", SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS));
         expected.append(MessageFormat.format("       {0}:{1}=\"{2}\"", SCHEMA_NAMESPACE_ATTRIBUTE, XML_SCHEMA_ATTRIBUTE, XSI_NS));
         expected.append(MessageFormat.format("       {0}=\"{1}\"", "assigned-identifier", "Study A"));
         expected.append(MessageFormat.format("       {0}=\"{1}\"", "last-modified-date", dateTimeFormat.format(study.getLastModifiedDate())));
         expected.append(MessageFormat.format("       {0}:{1}=\"{2} {3}\">", SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, AbstractStudyCalendarXmlSerializer.SCHEMA_LOCATION));
 
         expected.append(MessageFormat.format("<planned-calendar id=\"{0}\"/>", calendar.getGridId()));
         expected.append(MessageFormat.format("<amendment name=\"{0}\" date=\"{1}\" mandatory=\"{2}\" released-date=\"{3}\" />", firstAmendment.getName()
                 , formatter.format(firstAmendment.getDate()), String.valueOf(firstAmendment.isMandatory()), dateTimeFormat.format(firstAmendment.getReleasedDate())));
         expected.append(MessageFormat.format("<development-amendment name=\"{0}\" date=\"{1}\" mandatory=\"{2}\"/>",
             developmentAmendment.getName(), formatter.format(developmentAmendment.getDate()), String.valueOf(developmentAmendment.isMandatory())));
 
         expected.append("</study>");
 
         expectChildrenSerializers();
 
         replayMocks();
 
         String actual = serializer.createDocumentString(study);
         log.debug("actual:" + actual);
         log.debug("expected:" + expected);
         verifyMocks();
 
         assertXMLEqual(expected.toString(), actual);
     }
 
     ////// Expect helper methods
     
     private void expectChildrenSerializers() {
         expect(plannedCalendarSerializer.createElement(calendar)).andReturn(eCalendar);
         expect(amendmentSerializer.createElement(firstAmendment)).andReturn(eFirstAmendment);
         expect(developmentAmendmentSerializer.createElement(developmentAmendment)).andReturn(eDevelopmentAmendment);
 
     }
 
     private void expectResolveStudy(String name, Study resolved) {
         expect(studyDao.getByAssignedIdentifier(name)).andReturn(resolved);
     }
 
     private void expectDeserializePlannedCalendar() {
         expect(plannedCalendarSerializer.readElement(eCalendar)).andReturn(calendar);
     }
 
     private void expectDeserializePopulation() {
         expect(populationSerializer.readElement(ePopulation)).andReturn(population);
     }
 
     private void expectDesearializeDevelopmentAmendment() {
         expect(developmentAmendmentSerializer.readElement(eDevelopmentAmendment)).andReturn(developmentAmendment);
     }
 
     private void expectDeserializeAmendments() {
         expect(amendmentSerializer.readElement(eFirstAmendment)).andReturn(firstAmendment);
         expect(amendmentSerializer.readElement(eSecondAmendment)).andReturn(secondAmendment);
     }
 
     ////// Element Creation Helpers
 
     private Element createStudyElement() {
         QName qStudy = createQName(STUDY.xmlName(), AbstractStudyCalendarXmlSerializer.DEFAULT_NAMESPACE);
         Element eStudy = createElement(qStudy);
         eStudy.addAttribute("assigned-identifier", study.getAssignedIdentifier());
         eStudy.addAttribute("last-modified-date", formatter.format(study.getLastModifiedDate()));
 
         eStudy.add(ePopulation);
         eStudy.add(eCalendar);
 
         // Amendments are added in this order to test unordered deserialization
         eStudy.add(eSecondAmendment);
         eStudy.add(eFirstAmendment);
         eStudy.add(eDevelopmentAmendment);
         return eStudy;
     }
 
     private Element createDevelopmentAmendmentElement(Amendment developmentAmendment) {
         AmendmentXmlSerializer s = new AmendmentXmlSerializer();
         s.setDevelopmentAmendment(true);
         return s.createElement(developmentAmendment);
     }
 
     private Element createAmendmentElement(Amendment amendment) {
         AmendmentXmlSerializer s = new AmendmentXmlSerializer();
         return s.createElement(amendment);
     }
 
     private Element createPopulationElement(Population population) {
         PopulationXmlSerializer s = new PopulationXmlSerializer();
         return s.createElement(population);
     }
 
     private Element createCalendarElement(PlannedCalendar calendar) {
         PlannedCalendarXmlSerializer s = new PlannedCalendarXmlSerializer();
         return s.createElement(calendar);
     }
 
     ////// Create PSC object helpers
     private Study createStudy() {
         Study created = createNamedInstance("Study A", Study.class);
         created.setPlannedCalendar(calendar);
         created.addPopulation(population);
         created.setAmendment(firstAmendment);
         created.setDevelopmentAmendment(developmentAmendment);
         return created;
     }
 }
