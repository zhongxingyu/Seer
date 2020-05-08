     package no.magott.spring.batch.window;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Calendar;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.batch.core.ExitStatus;
 import org.springframework.batch.core.Job;
 import org.springframework.batch.core.JobExecution;
 import org.springframework.batch.core.JobParameters;
 import org.springframework.batch.core.JobParametersBuilder;
 import org.springframework.batch.core.launch.JobLauncher;
 import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
 import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
 import org.springframework.batch.core.repository.JobRestartException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @ContextConfiguration(locations = { "classpath:infiniteJob-batchwindow-listener-cutoff.xml",
 		"classpath:springbatch-common.xml" })
 @RunWith(SpringJUnit4ClassRunner.class)
 public class InfiniteJobBatchWindowListenerTests {
 
 	private static final int DELAY_FOR_BATCHWINDOW_IN_MILLIS = 1000;
 
 	private static final int TIMEOUT_LENIENCE_IN_MILLIS = 1000;
 
 	@Autowired
 	private JobLauncher jobLauncher;
 
 	@Autowired
 	private Job job;
 
 	@Test(timeout = TIMEOUT_LENIENCE_IN_MILLIS + DELAY_FOR_BATCHWINDOW_IN_MILLIS)
 	public void testLaunchJob() throws Exception {
 
 		JobParameters params = new JobParametersBuilder().addLong("batchwindow.closingTime", getClosingTime())
 		        .addString("batchwindow.cronDate", "")
 				.toJobParameters();
 		JobExecution jobExecution = jobLauncher.run(job, params);
 
		assertEquals(ExitStatus.STOPPED.getExitCode(), jobExecution.getExitStatus().getExitCode());
 
 		/*
 		 * Checks that the step execution contains exit description for batch
 		 * window timeout
 		 */
 		String description = jobExecution.getStepExecutions().iterator().next().getExitStatus().getExitDescription();
 		assertTrue(description.contains("Batch window has passed, batch will exit with data left to process"));
 
 		/*
 		 * The stepExecution.exitStatus is never propagated to jobExecution
 		 * because in general it is not possible to determine which step
 		 * execution has the "final" status
 		 */
 		assertEquals("", jobExecution.getExitStatus().getExitDescription());
 
 		/*
 		 * The interruption exception that stopped the job is saved in the job
 		 * execution, despite the fact that the status is not FAILED. In fact,
 		 * the exception actually signals the Job to take the STOPPED status.
 		 */
 		assertFalse("Stopped job saves its exception", jobExecution.getFailureExceptions().isEmpty());
 
 	}
 
 	private Long getClosingTime() {
 		Calendar calendar = Calendar.getInstance();
 		calendar.add(Calendar.MILLISECOND, DELAY_FOR_BATCHWINDOW_IN_MILLIS);
 		return (Long) calendar.getTime().getTime();
 	}
 	
 	@Test(timeout=2000+TIMEOUT_LENIENCE_IN_MILLIS)
 	public void timeOutCanBeSetUsingCronExpression() throws Exception {
 	    JobParameters params = new JobParametersBuilder().addLong("batchwindow.closingTime", getClosingTime())
                 .addString("batchwindow.cronDate", "*/2 * * * * *")
                 .toJobParameters();
         JobExecution jobExecution = jobLauncher.run(job, params);	
         assertEquals(ExitStatus.STOPPED, jobExecution.getExitStatus());
 	}
 
 }
