 package org.motechproject.ananya.referencedata.flw.repository;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.motechproject.ananya.referencedata.flw.domain.jobs.FrontLineWorkerSyncJob;
 import org.motechproject.model.CronSchedulableJob;
 import org.motechproject.scheduler.MotechSchedulerService;
 
 import java.util.Properties;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 @RunWith(MockitoJUnitRunner.class)
 public class AllSyncJobsTest {
 
     @Mock
     private MotechSchedulerService schedulerService;
     @Mock
     private Properties referenceDataProperteies;
 
     @Test
     public void shouldScheduleJobToAddFLWToQueue() {
         AllSyncJobs allSyncJobs = new AllSyncJobs(schedulerService, referenceDataProperteies);
        when(referenceDataProperteies.get("cronExpression")).thenReturn("* * * * *");
 
         allSyncJobs.addFrontLineWorkerSyncJob();
 
         ArgumentCaptor<CronSchedulableJob> captor = ArgumentCaptor.forClass(CronSchedulableJob.class);
         verify(schedulerService).safeScheduleJob(captor.capture());
         assertEquals(FrontLineWorkerSyncJob.class, captor.getValue().getClass());
     }
 }
