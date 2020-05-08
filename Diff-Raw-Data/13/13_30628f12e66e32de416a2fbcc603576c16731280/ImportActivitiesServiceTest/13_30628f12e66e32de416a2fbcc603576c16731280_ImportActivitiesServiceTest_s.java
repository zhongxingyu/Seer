 package edu.northwestern.bioinformatics.studycalendar.service;
 
 import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
 import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
 import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import edu.northwestern.bioinformatics.studycalendar.domain.Source;
 import edu.northwestern.bioinformatics.studycalendar.xml.writers.ActivitySourceXmlSerializer;
 import static org.easymock.EasyMock.expect;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.Arrays;
 import static java.util.Arrays.asList;
 import java.util.List;
 
 /**
  * @author John Dzak
  */
 public class ImportActivitiesServiceTest extends StudyCalendarTestCase {
     private ImportActivitiesService service;
     private SourceDao sourceDao;
     private ActivitySourceXmlSerializer serializer;
     private Source source0, source1;
     private List<Source> sources;
     private SourceService sourceService;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
 
         serializer = registerMockFor(ActivitySourceXmlSerializer.class);
         sourceDao = registerDaoMockFor(SourceDao.class);
 
         service = new ImportActivitiesService();
         service.setSourceDao(sourceDao);
         service.setXmlSerializer(serializer);
         sourceService = registerMockFor(SourceService.class);
         service.setSourceService(sourceService);
 
         source0 = createNamedInstance("ICD-9", Source.class);
         source1 = createNamedInstance("Loink", Source.class);
         sources = asList(source0, source1);
     }
 
     public void testSave() {
         sourceDao.save(source0);
         sourceDao.save(source1);
         replayMocks();
 
         service.save(sources);
         verifyMocks();
     }
 
     public void testRead() throws Exception {
         InputStream target = registerMockFor(InputStream.class);
         expect(serializer.readCollectionOrSingleDocument(target)).andReturn(sources);
         replayMocks();
         service.readData(target);
         verifyMocks();
     }
 
     public void testReplaceCollidingSourcesWhereActivitiesAreDifferent() throws Exception {
         Source existingSource = setId(0, createNamedInstance("ICD-9", Source.class));
         Activity activity2 = assignSource(createActivity("Bone Scan One"), existingSource);
         List<Source> existingSources = asList(existingSource);
 
         expect(sourceDao.getAll()).andReturn(existingSources);
        sourceService.updateSource(existingSource, source0.getActivities());
         replayMocks();
 
        List<Source> actualSources = service.replaceCollidingSources(sources);
         verifyMocks();
 
         Source actualSource0 = actualSources.get(0);
         assertEquals("Wrong Id", 0, (int) actualSource0.getId());
         assertEquals("Wrong number of activities", 1, actualSource0.getActivities().size());
         assertEquals("Wrong activity", activity2.getName(), actualSource0.getActivities().get(0).getName());
     }
 
     public void testReplaceCollidingSourcesWhereActivitiesWithSameNameButDifferentCode() throws Exception {
         Activity activity = createActivity("a", "123", source0, createActivityType("Bone Scan"));
         source0.addActivity(activity);
         Source existingSource = setId(0, createNamedInstance("ICD-9", Source.class));
         Activity activity0 = createActivity("a", "1234", existingSource, createActivityType("Bone Scan"));
         List<Source> existingSources = asList(existingSource);
 
         expect(sourceDao.getAll()).andReturn(existingSources);
        sourceService.updateSource(existingSource, source0.getActivities());
         replayMocks();
 
        List<Source> actualSources = service.replaceCollidingSources(sources);
         verifyMocks();
 
         Source actualSource0 = actualSources.get(0);
         assertEquals("Wrong Id", 0, (int) actualSource0.getId());
 
         assertEquals("Wrong number of activities", 1, actualSource0.getActivities().size());
 
         assertEquals("Wrong activity name", activity0.getName(), actualSource0.getActivities().get(0).getName());
         assertEquals("Wrong activity code", activity0.getCode(), actualSource0.getActivities().get(0).getCode());
     }
     
     public void testLoadAndSaveReturnsNewSourceIfNoExistingSources() throws Exception {
         Source newSource = new Source();
         InputStream xml = new ByteArrayInputStream(new byte[0]);
         expect(sourceDao.getAll()).andReturn(Arrays.<Source>asList());
         expect(serializer.readCollectionOrSingleDocument(xml)).andReturn(Arrays.asList(newSource));
 
         replayMocks();
         assertSame(newSource, service.loadAndSave(xml));
         verifyMocks();
     }
     
     public void testLoadAndSaveReturnsNullIfNoSources() throws Exception {
         InputStream xml = new ByteArrayInputStream(new byte[0]);
         expect(sourceDao.getAll()).andReturn(Arrays.<Source>asList());
         expect(serializer.readCollectionOrSingleDocument(xml)).andReturn(Arrays.<Source>asList());
 
         replayMocks();
         assertNull(service.loadAndSave(xml));
         verifyMocks();
     }
 
     private Activity assignSource(Activity activity, Source source) {
         activity.setSource(source);
         source.getActivities().add(activity);
         return activity;
     }
 }
 
