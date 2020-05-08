 package de.otto.jobstore.service;
 
 import de.otto.jobstore.common.*;
 import de.otto.jobstore.common.properties.JobInfoProperty;
 import de.otto.jobstore.repository.JobInfoRepository;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
 import org.springframework.test.util.ReflectionTestUtils;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import javax.annotation.Resource;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.*;
 import static org.testng.AssertJUnit.*;
 
 @ContextConfiguration(locations = {"classpath:spring/lhotse-jobs-context.xml"})
 public class JobServiceIntegrationTest extends AbstractTestNGSpringContextTests {
 
     @Resource(name = "jobServiceWithoutRemoteJobExecutorService")
     private JobService jobService;
 
     @Resource
     private JobInfoRepository jobInfoRepository;
 
     private RemoteJobExecutorService remoteJobExecutorService = mock(RemoteJobExecutorService.class);
     private RemoteJobRunnableMock jobRunnable = new RemoteJobRunnableMock(remoteJobExecutorService, 100, 10);
 
     private static final String JOB_NAME_1 = "test_job_1";
     private static final Map<String, String> PARAMETERS = Collections.singletonMap("paramK", "paramV");
     private static final URI REMOTE_JOB_URI = URI.create("http://www.example.com");
 
     @BeforeClass
     public void setUp() throws Exception {
         jobInfoRepository.clear(true);
         ReflectionTestUtils.setField(jobService, "remoteJobExecutorService", remoteJobExecutorService);
     }
 
     @Test
     public void testExecutingRemoteJob() throws Exception {
         jobRunnable = new RemoteJobRunnableMock(remoteJobExecutorService, 0, 0);
         jobService.clean();
         jobService.registerJob(jobRunnable);
         reset(remoteJobExecutorService);
         when(remoteJobExecutorService.startJob(any(RemoteJob.class))).thenReturn(REMOTE_JOB_URI);
         String id = jobService.executeJob(JOB_NAME_1);
         Thread.sleep(1000);
         //Job should be started
         assertNotNull(id);
         JobInfo jobInfo = jobInfoRepository.findById(id);
         //Job should still be running
         assertEquals(RunningState.RUNNING, RunningState.valueOf(jobInfo.getRunningState()));
         assertEquals(PARAMETERS, jobInfo.getParameters());
         assertEquals(REMOTE_JOB_URI.toString(), jobInfo.getAdditionalData().get(JobInfoProperty.REMOTE_JOB_URI.val()));
 
        //testPollingRunningRemoteJob
         reset(remoteJobExecutorService);
         List<String> logLines = new ArrayList<>();
         Collections.addAll(logLines, "log1", "log2");
         when(remoteJobExecutorService.getStatus(any(URI.class))).thenReturn(new RemoteJobStatus(RemoteJobStatus.Status.RUNNING, logLines));
 
         jobService.pollRemoteJobs();
 
        jobInfo = jobInfoRepository.findByNameAndRunningState(JOB_NAME_1, RunningState.RUNNING);
         assertEquals(RunningState.RUNNING, RunningState.valueOf(jobInfo.getRunningState()));
         assertEquals(REMOTE_JOB_URI.toString(), jobInfo.getAdditionalData().get(JobInfoProperty.REMOTE_JOB_URI.val()));
         assertEquals(2, jobInfo.getLogLines().size());
 
        //testPollingFinishedRemoteJob
         reset(remoteJobExecutorService);
         when(remoteJobExecutorService.getStatus(any(URI.class))).thenReturn(
                 new RemoteJobStatus(RemoteJobStatus.Status.FINISHED, logLines, new RemoteJobResult(true, 0, "done"), "date"));
 
         jobService.pollRemoteJobs();
 
        jobInfo = jobInfoRepository.findByName(JOB_NAME_1, 1).get(0);
         assertTrue(jobInfo.getRunningState().startsWith("FINISHED"));
         assertEquals(REMOTE_JOB_URI.toString(), jobInfo.getAdditionalData().get(JobInfoProperty.REMOTE_JOB_URI.val()));
         assertEquals(ResultCode.SUCCESSFUL, jobInfo.getResultState());
         assertEquals(2, jobInfo.getLogLines().size());
         assertEquals("done", jobInfo.getResultMessage());
     }
 
     class RemoteJobRunnableMock extends AbstractRemoteJobRunnable {
 
         final long maxExecutionTime;
         final long pollingInterval;
 
         RemoteJobRunnableMock(RemoteJobExecutorService remoteJobExecutorService, long maxExecutionTime, long pollingInterval) {
             super(remoteJobExecutorService);
             this.maxExecutionTime = maxExecutionTime;
             this.pollingInterval = pollingInterval;
         }
 
         @Override
         public String getName() {
             return JOB_NAME_1;
         }
 
         @Override
         public Map<String, String> getParameters() {
             return PARAMETERS;
         }
 
         @Override
         public long getMaxExecutionTime() {
             return maxExecutionTime;
         }
 
         @Override
         public long getPollingInterval() {
             return pollingInterval;
         }
     }
 }
