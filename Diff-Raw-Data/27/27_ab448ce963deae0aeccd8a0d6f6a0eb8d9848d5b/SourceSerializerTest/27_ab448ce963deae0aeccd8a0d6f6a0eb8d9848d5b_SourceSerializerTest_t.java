 package edu.northwestern.bioinformatics.studycalendar.xml.writers;
 
 import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
 import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
 import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
 import edu.northwestern.bioinformatics.studycalendar.domain.Source;
 import edu.northwestern.bioinformatics.studycalendar.service.SourceService;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import static org.easymock.EasyMock.*;
 
 import java.io.*;
 import java.util.List;
 import java.util.Collections;
 import java.util.ArrayList;
 
 public class SourceSerializerTest extends StudyCalendarTestCase {
     private static final String SOURCE_NAME = "TestSource";
     private SourceSerializer serializer;
     private Source source, anotherSource;
     private Activity act1, act2, act3;
     private static final char CSV_DELIM = ',';
     private static final char XLS_DELIM = '\t';
     private static final String CSV_HEADER = "Name,Type,Code,Description";
     private static final String XLS_HEADER = "Name\tType\tCode\tDescription";
     private static final String ACTIVITY_SOURCE = "Source";
 
     private ActivityType other, intervention;
 
     private SourceDao sourceDao;
     private SourceService sourceService;
     private ActivityTypeDao activityTypeDao;
     private File dataDir;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         serializer = new SourceSerializer();
         source = setId(11, createNamedInstance(SOURCE_NAME, Source.class));
         anotherSource = setId(12, createNamedInstance(SOURCE_NAME, Source.class));
         source = createNamedInstance(SOURCE_NAME, Source.class);
         activityTypeDao = registerDaoMockFor(ActivityTypeDao.class);
 
         other = Fixtures.createActivityType("OTHER");
         intervention = Fixtures.createActivityType("INTERVENTION");
 
         act1 = createActivity("Activity1", "Code1", source, other);
         act2 = createActivity("Activity2", "Code2", source, intervention);
         act2.setDescription("A2 also has frob, bar, and zap");
         act3 = createActivity("Activity3", "Code3", source, other);
         act3.setDescription("A3\nhas a long\ndescription");
 
         dataDir = getModuleRelativeDirectory("core", "src/test/java/edu/northwestern/bioinformatics/studycalendar/xml/writers/data");
 
         sourceDao = registerDaoMockFor(SourceDao.class);
         sourceService = new SourceService();
         sourceService.setSourceDao(sourceDao);
         serializer.setSourceDao(sourceDao);
         serializer.setSourceService(sourceService);
         serializer.setActivityTypeDao(activityTypeDao);
     }
 
     public void testCsvForActivityWithDescriptionWithCommas() throws Exception {
         Source oneAct = Fixtures.createSource("Src", act2);
         String doc = serializer.createDocumentString(oneAct, CSV_DELIM);
        String[] result = doc.split("\r?\n");
        String expectedSource = ACTIVITY_SOURCE + ",Src";
        assertEquals("Source header and content do not match", expectedSource, result[0]);
        assertEquals("Headers do not match", CSV_HEADER, result[1]);
        String expectedContents = "Activity2,INTERVENTION,Code2,\"A2 also has frob, bar, and zap\"";
        assertEquals("Contents do not match", expectedContents, result[2]);
     }
 
     public void testCsvForActivityWithDescriptionWithNewlines() throws Exception {
         Source oneAct = Fixtures.createSource("Src", act3);
         String doc = serializer.createDocumentString(oneAct, CSV_DELIM);
        String[] result = doc.split("\r?\n");
        String expectedContent1 = "Activity3,OTHER,Code3,\"A3";
        String expectedContent2 = "has a long";
        String expectedContent3 = "description\"";
        assertEquals("Contents with description first line do not match", expectedContent1, result[2]);
        assertEquals("Contents with description second line do not match", expectedContent2, result[3]);
        assertEquals("Contents with description third line do not match", expectedContent3, result[4]);
     }
 
     public void testCsvForActivityWithDescriptionWithQuotes() throws Exception {
         Source oneAct = Fixtures.createSource("Src", createActivity("A", "A", null, other, "aka \"Alpha\""));
         String doc = serializer.createDocumentString(oneAct, CSV_DELIM);
        String expectedContents = "A,OTHER,A,\"aka \"\"Alpha\"\"\"";
        String[] result = doc.split("\r?\n");
        assertEquals("Contents with description with quotes do not match", expectedContents, result[2]);
     }
 
     public void testWillUseOtherDelimiter() throws Exception {
         String document = serializer.createDocumentString(source, XLS_DELIM);
        String[] rows = document.split("\r?\n");
         assertEquals("Source header is incorrect", ACTIVITY_SOURCE +"\t"+SOURCE_NAME, rows[0]);
         assertEquals("Column header is incorrect", XLS_HEADER, rows[1]);
         assertEquals("Wrong first row", "Activity1\tOTHER\tCode1\t", rows[2]);
     }
 
     public void testCsvWithMultipleRows() throws Exception {
         String doc = serializer.createDocumentString(createSource("foo",
             createActivity("A"),
             createActivity("B"),
             createActivity("C"),
             createActivity("D")
         ), CSV_DELIM);
         assertEquals("Wrong number of rows:\n" + doc, 4 + 2, doc.split("\n").length);
     }
 
     public void testReadActivityWithEscapedQuotes() throws Exception {
         Activity actual = readCsvAndReturnActivity("Bs,OTHER,B,\"aka \"\"beta\"\"\",Single");
         assertEquals("Description not decoded properly", "aka \"beta\"", actual.getDescription());
     }
 
     @SuppressWarnings({ "unchecked" })
     private Activity readCsvAndReturnActivity(String csvRow) {
         String doc = CSV_HEADER + '\n' + csvRow + '\n';
         System.out.println(doc);
         ByteArrayInputStream in = new ByteArrayInputStream(doc.getBytes());
         // TODO: none of these things should be called from the serializer (#695)
         expect(activityTypeDao.getByName((String) notNull())).andStubReturn(other);
         expect(sourceDao.getByName((String) notNull())).andStubReturn(createSource("Single"));
         sourceDao.save((Source) notNull());
         replayMocks();
 
         Source deserialized = serializer.readDocument(in);
         assertNotNull("No source returned for " + csvRow, deserialized);
         return deserialized.getActivities().get(0);
     }
 
     public void testReadCSV() throws Exception {
         InputStream validDocStream = new FileInputStream(new File(dataDir, "Activity.csv"));
 
         expect(sourceDao.getByName(anotherSource.getName())).andReturn(anotherSource).anyTimes();
         expect(activityTypeDao.getByName("Other")).andReturn(other).anyTimes();
         expect(activityTypeDao.getByName("Intervention")).andReturn(intervention).anyTimes();
         replayMocks();
 
         Source importedSource = serializer.readDocument(validDocStream);
         verifyMocks();
         assertNotNull("sources must not be null", importedSource);
 
         List<Activity> activities = importedSource.getActivities();
         assertEquals("Document contains 3 activities", Integer.valueOf(3), Integer.valueOf(activities.size()));
 
         for (Activity activity : activities) {
             if (StringUtils.equals(activity.getName(), act1.getName())) {
                 assertActivitiesEqual(act1, activity);
             } else if (StringUtils.equals(activity.getName(), act2.getName())) {
                 assertActivitiesEqual(act2, activity);
             } else if (StringUtils.equals(activity.getName(), act3.getName())) {
                 assertActivitiesEqual(act3, activity);
             } else {
                 assertFalse("Activity must exists in source" + activity.toString(), true);
             }
         }
     }
 
 
     public void testActivitiesInNewFormatImportCSV() throws Exception {
         InputStream validDocStream = new FileInputStream(new File(dataDir, "New-format-for-activities.csv"));
         expect(activityTypeDao.getByName("Other")).andReturn(other).anyTimes();
         expect(activityTypeDao.getByName("Intervention")).andReturn(intervention).anyTimes();
         replayMocks();
 
         Source importedSource = serializer.readDocument(validDocStream);
         verifyMocks();
         assertNotNull("sources must not be null", importedSource);
 
         List<Activity> activities = importedSource.getActivities();
         assertEquals("Document contains 3 activities", Integer.valueOf(3), Integer.valueOf(activities.size()));
 
         for (Activity activity : activities) {
             if (StringUtils.equals(activity.getName(), act1.getName())) {
                 assertActivitiesEqual(act1, activity);
             } else if (StringUtils.equals(activity.getName(), act2.getName())) {
                 assertActivitiesEqual(act2, activity);
             } else if (StringUtils.equals(activity.getName(), act3.getName())) {
                 assertActivitiesEqual(act3, activity);
             } else {
                 assertFalse("Activity must exists in source" + activity.toString(), true);
             }
         }
     }
 
     public void testExportAndImportCSV() throws Exception {
         expect(sourceDao.getByName(anotherSource.getName())).andReturn(anotherSource).anyTimes();
 
         expect(activityTypeDao.getByName("OTHER")).andReturn(other).anyTimes();
         expect(activityTypeDao.getByName("INTERVENTION")).andReturn(intervention).anyTimes();
         String document = serializer.createDocumentString(source, CSV_DELIM);
 
 //        sourceDao.save(anotherSource);
 
         replayMocks();
         Source importedSource = serializer.readDocument(IOUtils.toInputStream(document));
         verifyMocks();
 
         assertNotNull("sources must not be null", importedSource);
         assertEquals("import should be able to import file that was created by calling export method ", source, importedSource);
         List<Activity> activities = importedSource.getActivities();
         assertEquals("Document contains 3 activities", Integer.valueOf(3), Integer.valueOf(activities.size()));
 
         for (Activity activity : activities) {
             if (StringUtils.equals(activity.getName(), act1.getName())) {
                 assertActivitiesEqual(act1, activity);
             } else if (StringUtils.equals(activity.getName(), act2.getName())) {
                 assertActivitiesEqual(act2, activity);
             } else if (StringUtils.equals(activity.getName(), act3.getName())) {
                 assertActivitiesEqual(act3, activity);
             } else {
                 assertFalse("Activity must exists in source" + activity.toString(), true);
             }
         }
     }
 
     public void testActivitiesMustBelongToSameSingleSourceInImportCSV() throws Exception {
         InputStream multipleSourceDoc = new FileInputStream(new File(dataDir, "Activity-multiple-sources.csv"));
 
         expect(sourceDao.getByName(anotherSource.getName())).andReturn(anotherSource).anyTimes();
         expect(activityTypeDao.getByName("Other")).andReturn(other).anyTimes();
         expect(activityTypeDao.getByName("Intervention")).andReturn(other).anyTimes();
         replayMocks();
 
         try {
             serializer.readDocument(multipleSourceDoc);
             fail("All activities must belong to same source. TestSource and another source are not same source.");
         } catch (Exception e) {
             assertEquals("All activities must belong to same source. TestSource and another source are not same source.", e.getMessage());
         }
         verifyMocks();
     }
 
 
 
     public void testValidateActivityByName() throws Exception {
         Activity activity = createActivity("", "123", source, createActivityType("Bone Scan"));
         Source source = new Source();
         source.addActivity(activity);
         String doc = serializer.createDocumentString(source, CSV_DELIM);
         ByteArrayInputStream in = new ByteArrayInputStream(doc.getBytes());
         try {
             serializer.readDocument(in);
         } catch (Exception e) {
             assertEquals("Activity name can not be empty or null for activities", e.getMessage());
         }
     }
 
     public void testValidateActivityByCode() throws Exception {
         Activity activity = createActivity("a", "", source, createActivityType("Bone Scan"));
         Source source = new Source();
         source.addActivity(activity);
         String doc = serializer.createDocumentString(source, CSV_DELIM);
         ByteArrayInputStream in = new ByteArrayInputStream(doc.getBytes());
         try {
             serializer.readDocument(in);
         } catch (Exception e) {
             assertEquals("Activity code can not be empty or null for activities", e.getMessage());
         }
     }
 
     public void testValidateActivityByType() throws Exception {
         Activity activity = createActivity("a", "123", source, createActivityType(""));
         Source source = new Source();
         source.addActivity(activity);
         String doc = serializer.createDocumentString(source, CSV_DELIM);
         ByteArrayInputStream in = new ByteArrayInputStream(doc.getBytes());
         expect(activityTypeDao.getByName(activity.getType().getName())).andReturn(activity.getType()).anyTimes();
         try {
             serializer.readDocument(in);
         } catch (Exception e) {
             assertEquals("Activity type " + activity.getType().getName() + " is invalid. Please choose from this list: null.", e.getMessage());
         }
     }
 
     public void testValidateActivitiesByUniqueName() throws Exception {
         Activity activity1 = createActivity("a", "123", source, createActivityType("Bone Scan"));
         Activity activity2 = createActivity("b", "1234", source, createActivityType("Bone Scan"));
         Activity activity3 = createActivity("a", "1235", source, createActivityType("Bone Scan"));
         List<Activity> activities = new ArrayList<Activity>();
         activities.add(activity1);
         activities.add(activity2);
         activities.add(activity3);
         Source source = new Source();
         source.setActivities(activities);
         String doc = serializer.createDocumentString(source, CSV_DELIM);
         ByteArrayInputStream in = new ByteArrayInputStream(doc.getBytes());
         expect(activityTypeDao.getByName(activity1.getType().getName())).andReturn(activity1.getType()).anyTimes();
         try {
             replayMocks();
             serializer.readDocument(in);
             verifyMocks();
         } catch (Exception e) {
             assertEquals("Name and Code must be unique for activities within same source", e.getMessage());
         }
     }
 
     public void testValidateActivitiesByUniqueCode() throws Exception {
         Activity activity1 = createActivity("a", "123", source, createActivityType("Bone Scan"));
         Activity activity2 = createActivity("b", "1234", source, createActivityType("Bone Scan"));
         Activity activity3 = createActivity("d", "123", source, createActivityType("Bone Scan"));
         List<Activity> activities = new ArrayList<Activity>();
         activities.add(activity1);
         activities.add(activity2);
         activities.add(activity3);
         Source source = new Source();
         source.setActivities(activities);
         String doc = serializer.createDocumentString(source, CSV_DELIM);
         ByteArrayInputStream in = new ByteArrayInputStream(doc.getBytes());
         expect(activityTypeDao.getByName(activity1.getType().getName())).andReturn(activity1.getType()).anyTimes();
         try {
             replayMocks();
             serializer.readDocument(in);
             verifyMocks();
         } catch (Exception e) {
             assertEquals("Name and Code must be unique for activities within same source", e.getMessage());
         }
     }
 
     public void testValidateActivitiesBySourceNameWhichDontExist() throws Exception {
         Activity activity = createActivity("a", "123", source, createActivityType("Bone Scan"));
         List<Activity> activities = Collections.singletonList(activity);
         Source source = new Source();
         source.setName("SOURCE");
         activity.setSource(source);
         source.setActivities(activities);
         String doc = serializer.createDocumentString(source, CSV_DELIM);
         ByteArrayInputStream in = new ByteArrayInputStream(doc.getBytes());
         expect(activityTypeDao.getByName(activity.getType().getName())).andReturn(activity.getType()).anyTimes();
         try {
             replayMocks();
             serializer.readDocument(in);
             verifyMocks();
         } catch (Exception e) {
             assertEquals("source " + source.getName() + " does not exist.", e.getMessage());
         }
     }
 
 
 
     private void assertActivitiesEqual(Activity expected, Activity actual) {
         assertEquals("name must be same", expected.getName(), actual.getName());
 //        assertEquals("source must be same", expected.getSource(), actual.getSource());
         assertEquals("code must be same", expected.getCode(), actual.getCode());
         assertEquals("desc must be same", expected.getDescription(), actual.getDescription());
         assertEquals("type must be same", expected.getType(), actual.getType());
     }
 }
