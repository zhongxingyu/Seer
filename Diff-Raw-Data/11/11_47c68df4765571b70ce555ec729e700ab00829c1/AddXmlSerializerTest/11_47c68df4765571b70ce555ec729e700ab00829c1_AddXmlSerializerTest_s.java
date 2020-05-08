 package edu.northwestern.bioinformatics.studycalendar.xml.writers;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
 import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
 import org.dom4j.Element;
 import static org.easymock.EasyMock.expect;
 
 public class AddXmlSerializerTest extends StudyCalendarXmlTestCase {
     private AddXmlSerializer serializer;
     private Add add;
     private Element element;
     private ChangeDao changeDao;
 
     protected void setUp() throws Exception {
         super.setUp();
 
         element = registerMockFor(Element.class);
         changeDao = registerMockFor(ChangeDao.class);
 
        serializer = new AddXmlSerializer();
         serializer.setChangeDao(changeDao);
 
         add = setGridId("grid0", Add.create(new PlannedCalendar(), 0));
     }
 
     public void testCreateElement() {
         Element element = serializer.createElement(add);
         
         assertEquals("Wrong grid id", "grid0", element.attributeValue("id"));
         assertEquals("Wrong index", "0", element.attributeValue("index"));
     }
 
     public void testReadElementWhenAddExists() {
         expect(element.attributeValue("id")).andReturn("grid0");
         expect(changeDao.getByGridId("grid0")).andReturn(add);
         replayMocks();
 
         Add actual = (Add) serializer.readElement(element);
         verifyMocks();
         
         assertSame("Change objects should be the same", add, actual);
     }
 
     public void testReadElementWhenAddIsNew() {
         expect(element.attributeValue("id")).andReturn("grid0");
         expect(changeDao.getByGridId("grid0")).andReturn(null);
         expect(element.attributeValue("index")).andReturn("0");
         replayMocks();
 
         Add actual = (Add) serializer.readElement(element);
         verifyMocks();
 
         assertEquals("Wrong grid id", "grid0", actual.getGridId());
         assertEquals("Wrong grid index", 0, (int) actual.getIndex());
     }
 }
