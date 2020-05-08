 package de.otto.jobstore.service;
 
 import de.otto.jobstore.TestSetup;
 import de.otto.jobstore.common.*;
 import de.otto.jobstore.common.properties.JobInfoProperty;
 import de.otto.jobstore.repository.JobDefinitionRepository;
 import de.otto.jobstore.repository.JobInfoRepository;
 import de.otto.jobstore.service.exception.JobException;
 import de.otto.jobstore.service.exception.JobExecutionException;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import javax.annotation.Resource;
 import java.net.URI;
 import java.util.*;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.*;
 import static org.testng.AssertJUnit.*;
 import static org.testng.AssertJUnit.assertEquals;
 
 @ContextConfiguration(locations = {"classpath:spring/jobs-context.xml"})
 public class JobServiceIntegrationTest extends AbstractTestNGSpringContextTests {
 
     @Resource(name = "jobServiceWithoutRemoteJobExecutorService")
     private JobService jobService;
 
     @Resource
     private JobInfoRepository jobInfoRepository;
 
     @Resource
     private JobInfoService jobInfoService;
 
     @Resource
     private JobDefinitionRepository jobDefinitionRepository;
 
     private RemoteJobExecutorService remoteJobExecutorService = mock(RemoteJobExecutorService.class);
     private JobRunnable jobRunnable;
 
     private static final String JOB_NAME_1 = "test_job_1";
     private static final String JOB_NAME_2 = "test_job_2";
     private static final String JOB_NAME_3 = "test_job_3";
     private static final Map<String, String> PARAMETERS = Collections.singletonMap("paramK", "paramV");
     private static final URI REMOTE_JOB_URI = URI.create("http://www.example.com");
 
     @BeforeMethod
     public void setUp() throws Exception {
         jobService.clean();
         jobInfoRepository.clear(true);
         jobDefinitionRepository.addOrUpdate(StoredJobDefinition.JOB_EXEC_SEMAPHORE);
         reset(remoteJobExecutorService);
     }
 
     @Test
     public void testExecutingRemoteJob() throws Exception {
         jobRunnable = TestSetup.remoteJobRunnable(remoteJobExecutorService, jobInfoService, PARAMETERS,
                 TestSetup.remoteJobDefinition(JOB_NAME_3, 0, 0));
         jobService.registerJob(jobRunnable);
         reset(remoteJobExecutorService);
         when(remoteJobExecutorService.startJob(any(RemoteJob.class))).thenReturn(REMOTE_JOB_URI);
         String id = jobService.executeJob(JOB_NAME_3);
         Thread.sleep(1000);
         // job should be started
         assertNotNull(id);
         JobInfo jobInfo = jobInfoRepository.findById(id);
         // job should still be running
         assertEquals(RunningState.RUNNING, RunningState.valueOf(jobInfo.getRunningState()));
         assertEquals(PARAMETERS, jobInfo.getParameters());
         assertEquals(REMOTE_JOB_URI.toString(), jobInfo.getAdditionalData().get(JobInfoProperty.REMOTE_JOB_URI.val()));
 
         // testPollingRunningRemoteJob
         reset(remoteJobExecutorService);
         List<String> logLines = new ArrayList<>();
         Collections.addAll(logLines, "log l.1", "log l.2");
         when(remoteJobExecutorService.getStatus(any(URI.class))).thenReturn(new RemoteJobStatus(RemoteJobStatus.Status.RUNNING, logLines, "bar"));
         // verify(remoteJobExecutorService, times(1)).
 
         jobService.pollRemoteJobs();
 
         jobInfo = jobInfoRepository.findByNameAndRunningState(JOB_NAME_3, RunningState.RUNNING);
         assertEquals(RunningState.RUNNING, RunningState.valueOf(jobInfo.getRunningState()));
         assertEquals(REMOTE_JOB_URI.toString(), jobInfo.getAdditionalData().get(JobInfoProperty.REMOTE_JOB_URI.val()));
         assertEquals(2, jobInfo.getLogLines().size());
         assertEquals("bar", jobInfo.getStatusMessage());
 
         //testPollingFinishedRemoteJob
         reset(remoteJobExecutorService);
         when(remoteJobExecutorService.getStatus(any(URI.class))).thenReturn(
                 new RemoteJobStatus(RemoteJobStatus.Status.FINISHED, logLines, new RemoteJobResult(true, 0, "done"), "date"));
 
         jobService.pollRemoteJobs();
 
         jobInfo = jobInfoRepository.findByName(JOB_NAME_3, 1).get(0);
         assertTrue(jobInfo.getRunningState().startsWith("FINISHED"));
         assertEquals(REMOTE_JOB_URI.toString(), jobInfo.getAdditionalData().get(JobInfoProperty.REMOTE_JOB_URI.val()));
         assertEquals(ResultCode.SUCCESSFUL, jobInfo.getResultState());
         assertEquals(2, jobInfo.getLogLines().size());
         assertEquals("done", jobInfo.getResultMessage());
     }
 
     @Test
     public void testExecuteJobWhichViolatesRunningConstraints() throws Exception {
         jobService.registerJob(new LocalJobRunnableMock(JOB_NAME_1));
         jobService.registerJob(new LocalJobRunnableMock(JOB_NAME_2));
         Set<String> constraint = new HashSet<>();
         constraint.add(JOB_NAME_1); constraint.add(JOB_NAME_2);
         jobService.addRunningConstraint(constraint);
 
         String id1 = jobService.executeJob(JOB_NAME_1);
         String id2 = jobService.executeJob(JOB_NAME_2);
 
         JobInfo jobInfo1 = jobInfoRepository.findById(id1);
         assertNotNull(jobInfo1);
         assertEquals(RunningState.RUNNING.name(), jobInfo1.getRunningState());
 
         JobInfo jobInfo2 = jobInfoRepository.findById(id2);
         assertNotNull(jobInfo2);
         assertEquals(RunningState.QUEUED.name(), jobInfo2.getRunningState());
     }
 
     @Test
     public void testExecuteJobFailsWithConnectionException() throws Exception {
         jobRunnable = TestSetup.remoteJobRunnable(remoteJobExecutorService, jobInfoService, PARAMETERS,
                 TestSetup.remoteJobDefinition(JOB_NAME_3, 0, 0));
         jobService.registerJob(jobRunnable);
         reset(remoteJobExecutorService);
         when(remoteJobExecutorService.startJob(any(RemoteJob.class))).thenThrow(new JobExecutionException("Error connecting to host"));
         String id = jobService.executeJob(JOB_NAME_3);
         Thread.sleep(1000);
         JobInfo jobInfo = jobInfoRepository.findById(id);
         assertTrue("Expected job to be finished but it is: " + jobInfo.getRunningState(), jobInfo.getRunningState().startsWith("FINISHED"));
         assertEquals(ResultCode.FAILED, jobInfo.getResultState());
     }
 
     @Test
     public void testIsJobExecutionDisabledReturnsCorrectStatus() throws Exception {
         jobRunnable = TestSetup.localJobRunnable(JOB_NAME_1, 1000);
         jobService.registerJob(jobRunnable);
 
         jobService.setJobExecutionEnabled(JOB_NAME_1, false);
         assertFalse(jobService.isJobExecutionEnabled(JOB_NAME_1));
 
         jobService.setJobExecutionEnabled(JOB_NAME_1, true);
         assertTrue(jobService.isJobExecutionEnabled(JOB_NAME_1));
     }
 
     @Test
     public void testIsExecutionDisabledReturnsCorrectStatus() throws Exception {
         jobService.setExecutionEnabled(false);
         assertFalse(jobService.isExecutionEnabled());
 
         jobService.setExecutionEnabled(true);
         assertTrue(jobService.isExecutionEnabled());
     }
 
     @Test
     public void testIfJobReactsOnTimeoutConditionAndIsMarkedAsTimeoutAfterwards() throws Throwable {
 
         final CountDownLatch c = new CountDownLatch(1);
 
         JobRunnable job = new AbstractLocalJobRunnable() {
             private AbstractLocalJobDefinition localJobDefinition = new AbstractLocalJobDefinition() {
                 @Override
                 public String getName() {
                     return JOB_NAME_1;
                 }
 
                 @Override
                 public long getMaxIdleTime() {
                     return 0;
                 }
 
                 @Override
                 public long getMaxExecutionTime() {
                     return 0;
                 }
 
                 @Override
                 public boolean isAbortable() {
                     return true;
                 }
             };
 
             @Override
             public JobDefinition getJobDefinition() {
                 return localJobDefinition;
             }
 
             @Override
             public void execute(JobExecutionContext context) throws JobException {
 
                 try {
                     try {
                         Thread.sleep(100);
                     } catch (InterruptedException e) {
                     }
 
                     context.checkForAbort();
                 } finally {
                     c.countDown();
                 }
             }
         };
 
         jobService.registerJob(job);
         String jobId = jobService.executeJob(job.getJobDefinition().getName());
         c.await();
 
 
         int count = 10;
         while (count-- > 0) {
             JobInfo jobInfo = jobInfoRepository.findById(jobId);
             if (RunningState.RUNNING.name().equals(jobInfo.getRunningState())) {
                 try {
                     Thread.sleep(100);
                 } catch (InterruptedException e) {
                 }
             } else {
                 break;
             }
         }
 
         JobInfo jobInfo = jobInfoRepository.findById(jobId);
         assertEquals(ResultCode.TIMED_OUT, jobInfo.getResultState());
 
     }
 
 
     ExecutorService executors = Executors.newFixedThreadPool(2);
 
     @Test(enabled = false)
     public void twoThreadsTryingToExecuteAJobShouldResultInOnlyOneExecution() throws Exception {
         for (int i = 0; i < 1000; i++) {
             jobService.registerJob(new LocalJobRunnableMock(JOB_NAME_1));
 
             executors.submit(new JobExecutionRunnable());
             executors.submit(new JobExecutionRunnable());
 
             Thread.sleep(100);
 
             final List<JobInfo> jobInfos = jobInfoRepository.findByName(JOB_NAME_1, 10);
             assertEquals("Only one job expected in database as the second thread should not create a job when one already exists.", 1, jobInfos.size());
             jobInfoRepository.clear(false);
         }
     }
 
     @Test(enabled = false)
     public void twoThreadsTryingToExecuteWhileAnotherJobWhichViolatesRunningConstraints() throws Exception {
         Set<String> runningConstraints = new HashSet<>(); runningConstraints.add(JOB_NAME_1); runningConstraints.add(JOB_NAME_2);
         for (int i = 0; i < 1000; i++) {
             jobService.registerJob(new LocalJobRunnableMock(JOB_NAME_2));
             jobService.executeJob(JOB_NAME_2);
 
             jobService.registerJob(new LocalJobRunnableMock(JOB_NAME_1));
 
             executors.submit(new JobExecutionRunnable());
             executors.submit(new JobExecutionRunnable());
 
             Thread.sleep(100);
 
             final List<JobInfo> jobInfos = jobInfoRepository.findByName(JOB_NAME_1, 10);
             assertEquals("Only one job expected in database.", 1, jobInfos.size());
             jobInfoRepository.clear(false);
         }
 
     }
 
     @Test
     public void testIfRetryDoesNotOccur() throws Exception {
         JobDefinition jobDefinition = TestSetup.localJobDefinition(JOB_NAME_1, 1000, 3);
         JobRunnable jobRunnable = TestSetup.localJobRunnable(jobDefinition, null);
         jobService.registerJob(jobRunnable);
 
         // no job yet started, should not start one
         jobService.doRetryFailedJobs();
         JobInfo jobInfo1 = jobInfoRepository.findMostRecent(JOB_NAME_1);
         assertNull(jobInfo1);
 
         String id = jobService.executeJob(JOB_NAME_1);
         JobInfo jobInfo2 = jobInfoRepository.findById(id);
 
         // no new job started, so last jobInfo should be same as current found jobInfo
         jobService.doRetryFailedJobs();
         JobInfo jobInfo3 = jobInfoRepository.findMostRecent(JOB_NAME_1);
        assertEquals(new Long(0), jobInfo3.getRetries());
         assertEquals(jobInfo2.getId(), jobInfo3.getId());
 
     }
 
     public void testIfRetryWorks() throws Exception {
         JobDefinition jobDefinition = TestSetup.localJobDefinition(JOB_NAME_1, 1000, 3);
         JobRunnable jobRunnable = TestSetup.localJobRunnable(jobDefinition, new JobExecutionException("We shall fail"));
         jobService.registerJob(jobRunnable);
 
         String id1 = jobService.executeJob(JOB_NAME_1);
 
         JobInfo jobInfo1 = jobInfoRepository.findById(id1);
         //assertNotNull(jobInfo1);
         //assertEquals(RunningState.RUNNING.name(), jobInfo1.getRunningState());
 
         jobService.doRetryFailedJobs();
         JobInfo jobInfo2 = jobInfoRepository.findMostRecent(JOB_NAME_1);
         assertEquals(new Long(1), jobInfo2.getRetries());
 
         jobService.doRetryFailedJobs();
         JobInfo jobInfo3 = jobInfoRepository.findMostRecent(JOB_NAME_1);
         assertEquals(new Long(2), jobInfo3.getRetries());
 
         jobService.doRetryFailedJobs();
         JobInfo jobInfo4 = jobInfoRepository.findMostRecent(JOB_NAME_1);
         assertEquals(new Long(3), jobInfo4.getRetries());
 
         // no new job started, so last jobInfo should be same as current found jobInfo
         jobService.doRetryFailedJobs();
         JobInfo jobInfo5 = jobInfoRepository.findMostRecent(JOB_NAME_1);
         assertEquals(jobInfo4.getId(), jobInfo5.getId());
     }
 
 
     class JobExecutionRunnable implements  Runnable {
 
         @Override
         public void run() {
             try {
                 jobService.executeJob(JOB_NAME_1);
             } catch (JobException e) {
                 throw new RuntimeException(e);
             }
         }
     }
 
     class LocalJobRunnableMock extends AbstractLocalJobRunnable {
 
         private JobDefinition localJobDefinition;
 
         LocalJobRunnableMock(JobDefinition jobDefinition) {
             localJobDefinition = jobDefinition;
         }
 
         LocalJobRunnableMock(String name) {
             localJobDefinition = TestSetup.localJobDefinition(name, 1000);
         }
 
         @Override
         public JobDefinition getJobDefinition() {
             return localJobDefinition;
         }
 
         @Override
         public void execute(JobExecutionContext context) throws JobException {
             try {
                 Thread.sleep(1000);
             } catch (InterruptedException e) {}
         }
     }
 
 }
