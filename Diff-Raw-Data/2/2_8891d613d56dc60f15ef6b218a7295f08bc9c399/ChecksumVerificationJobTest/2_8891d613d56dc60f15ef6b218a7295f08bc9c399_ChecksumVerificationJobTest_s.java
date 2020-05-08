 package pl.psnc.dl.wf4ever.monitoring;
 
 import javax.ws.rs.core.UriBuilder;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mockito;
 import org.quartz.JobDataMap;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 
 import pl.psnc.dl.wf4ever.db.ResourceInfo;
 import pl.psnc.dl.wf4ever.dl.DigitalLibrary;
 import pl.psnc.dl.wf4ever.model.BaseTest;
 import pl.psnc.dl.wf4ever.model.Builder;
 import pl.psnc.dl.wf4ever.monitoring.ChecksumVerificationJob.Mismatch;
 
 /**
  * Test that the checksum verification plugin detects checksum inconsistencies.
  * 
  * @author piotrekhol
  * 
  */
 public class ChecksumVerificationJobTest extends BaseTest {
 
     /** The only file that the mock DL will handle. */
     private static final String FILE_PATH = "a workflow.t2flow";
 
     /** Result of the last job call. */
     private ResultAnswer resultAnswer;
 
     /** A builder using the mock digital library. */
     private Builder fsBuilder;
 
     /** Context with the input data. */
     private JobExecutionContext context;
 
 
     /**
      * Prepare the mock digital library.
      * 
      * @throws Exception
      *             it shouldn't happen
      */
     @Override
     @Before
     public void setUp()
             throws Exception {
         super.setUp();
         DigitalLibrary dlMock = Mockito.mock(DigitalLibrary.class);
         Mockito.when(dlMock.getFileContents(researchObject.getUri(), FILE_PATH)).thenReturn(
             IOUtils.toInputStream("lorem ipsum"));
         Mockito.when(dlMock.getFileInfo(researchObject.getUri(), FILE_PATH)).thenReturn(
             new ResourceInfo(null, null, "80a751fde577028640c419000e33eba6", 0, "MD5", null, null));
         Mockito.when(dlMock.fileExists(researchObject.getUri(), FILE_PATH)).thenReturn(true);
 
         JobDataMap jobDataMap = new JobDataMap();
         jobDataMap.put(ChecksumVerificationJob.RESEARCH_OBJECT_URI, researchObject.getUri());
         context = Mockito.mock(JobExecutionContext.class);
         Mockito.when(context.getMergedJobDataMap()).thenReturn(jobDataMap);
         resultAnswer = new ResultAnswer();
         Mockito.doAnswer(resultAnswer).when(context).setResult(Mockito.any());
         fsBuilder = new Builder(builder.getUser(), builder.getDataset(), builder.isUseTransactions(), dlMock);
     }
 
 
     @Override
     @After
     public void tearDown()
             throws Exception {
         super.tearDown();
     }
 
 
     /**
      * Test that the job reports no mismatches correctly.
      * 
      * @throws JobExecutionException
      *             any problem when running the job
      */
     @Test
     public final void testExecuteWithNoMismatches()
             throws JobExecutionException {
         ChecksumVerificationJob job = new ChecksumVerificationJob();
         job.setBuilder(fsBuilder);
         job.execute(context);
         Assert.assertNotNull(resultAnswer.getResult());
         Assert.assertTrue(resultAnswer.getResult().matches());
         Assert.assertTrue(resultAnswer.getResult().getMismatches().isEmpty());
     }
 
 
     /**
      * Test that the job reports mismatches correctly.
      * 
      * @throws JobExecutionException
      *             any problem when running the job
      */
     @Test
     public final void testExecuteWithMismatches()
             throws JobExecutionException {
         Mockito.when(fsBuilder.getDigitalLibrary().getFileContents(researchObject.getUri(), FILE_PATH)).thenReturn(
             IOUtils.toInputStream("lorem ipsum this is something new"));
 
         ChecksumVerificationJob job = new ChecksumVerificationJob();
         job.setBuilder(fsBuilder);
         job.execute(context);
         Assert.assertNotNull(resultAnswer.getResult());
         Assert.assertFalse(resultAnswer.getResult().matches());
         Assert.assertEquals(1, resultAnswer.getResult().getMismatches().size());
         Mismatch mismatch = resultAnswer.getResult().getMismatches().iterator().next();
         Assert.assertEquals(UriBuilder.fromUri(researchObject.getUri()).path(FILE_PATH).build(),
             mismatch.getResourceUri());
         Assert.assertEquals("663e9f8d61af863dfb207870ee028041", mismatch.getCalculatedChecksum().toLowerCase());
         Assert.assertEquals("80a751fde577028640c419000e33eba6", mismatch.getExpectedChecksum().toLowerCase());
     }
 }
