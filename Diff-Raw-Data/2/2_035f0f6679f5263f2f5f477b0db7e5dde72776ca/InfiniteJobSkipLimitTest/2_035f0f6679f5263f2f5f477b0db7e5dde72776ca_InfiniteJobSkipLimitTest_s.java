    package no.magott.spring.batch.window;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.batch.core.ExitStatus;
 import org.springframework.batch.core.Job;
 import org.springframework.batch.core.JobExecution;
 import org.springframework.batch.core.JobParameters;
 import org.springframework.batch.core.JobParametersBuilder;
 import org.springframework.batch.core.launch.JobLauncher;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @ContextConfiguration(locations = { "classpath:infiniteJob-skip-limit-test.xml",
 		"classpath:springbatch-common.xml" })
 @RunWith(SpringJUnit4ClassRunner.class)
 public class InfiniteJobSkipLimitTest {
 
 	@Autowired
 	private JobLauncher jobLauncher;
 
 	@Autowired
 	private Job job;
 
 	@Test(timeout = 60000)
 	public void jobShouldAlsoFailWhenSkipLimitIsGreaterThanCommitInterval() throws Exception {
 
 		JobParameters params = new JobParametersBuilder().addLong("foo", System.currentTimeMillis())
 				.toJobParameters();
 		JobExecution jobExecution = jobLauncher.run(job, params);
 
 		assertEquals(ExitStatus.FAILED.getExitCode(), jobExecution.getExitStatus().getExitCode());
 
 	}
 
 }
