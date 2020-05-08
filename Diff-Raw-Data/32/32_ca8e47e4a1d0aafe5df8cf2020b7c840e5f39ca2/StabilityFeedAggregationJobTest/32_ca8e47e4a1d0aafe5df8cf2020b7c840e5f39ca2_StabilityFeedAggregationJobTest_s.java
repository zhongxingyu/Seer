 package pl.psnc.dl.wf4ever.monitoring;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.Properties;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mockito;
 import org.quartz.JobDataMap;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 
 public class StabilityFeedAggregationJobTest {
 
     JobExecutionContext context;
     static final URI RO_URI = URI.create("http://www.example.com/ro/test-ro/");
     static final URI EMPTY_RO_URI = URI.create("http://www.example.com/ro/test-empty-ro/");
     URI checklistNotificationsUri;
     protected static final String HOST_STRING = "http://127.0.0.1:8089";
 

    /*@Rule
     public WireMockRule wireMockRule = new WireMockRule(8089);
    */
 
     @Before
     public void setUp()
             throws IOException {
         context = Mockito.mock(JobExecutionContext.class);
         Properties properties = new Properties();
         try {
             properties.load(getClass().getClassLoader().getResourceAsStream("connection.properties"));
         } catch (IOException e) {
             throw new IOException("Configuration couldn't be loaded", e);
         }
         checklistNotificationsUri = URI.create(properties.getProperty("checklist_notifications_uri"));
         //http mock
         InputStream checklistRefactorInput = StabilityFeedAggregationJobTest.class.getClassLoader()
                 .getResourceAsStream("monitoring/stability_service_notification.xml");
         InputStream checklistRefactorNoEntryInput = StabilityFeedAggregationJobTest.class.getClassLoader()
                 .getResourceAsStream("monitoring/stability_service_notification_case_empty.xml");
        //stabiltyservice
        /*
         stubFor(get(urlMatching((checklistNotificationsUri.toString() + ".*").replace(HOST_STRING, ""))).willReturn(
             aResponse().withStatus(200).withBody(IOUtils.toString(checklistRefactorInput))));
         stubFor(get(urlMatching((checklistNotificationsUri.toString() + ".*empty.*").replace(HOST_STRING, "")))
                 .willReturn(aResponse().withStatus(200).withBody(IOUtils.toString(checklistRefactorNoEntryInput))));
                */
     }
 
 
     @Test
     public void testExecute()
             throws IOException, JobExecutionException {
         StabilityFeedAggregationJob job = new StabilityFeedAggregationJob();
         JobDataMap jobDataMap = new JobDataMap();
         jobDataMap.put(StabilityFeedAggregationJob.RESEARCH_OBJECT_URI, RO_URI);
         Mockito.when(context.getMergedJobDataMap()).thenReturn(jobDataMap);
         job.execute(context);
     }
 }
