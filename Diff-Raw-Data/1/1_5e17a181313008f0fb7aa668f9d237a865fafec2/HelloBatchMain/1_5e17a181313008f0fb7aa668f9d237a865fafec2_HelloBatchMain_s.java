 package batch.launch;
 
 import java.util.HashMap;
 
 import org.springframework.batch.core.Job;
 import org.springframework.batch.core.JobParameter;
 import org.springframework.batch.core.JobParameters;
 import org.springframework.batch.core.JobParametersInvalidException;
 import org.springframework.batch.core.launch.JobLauncher;
 import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
 import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
 import org.springframework.batch.core.repository.JobRestartException;
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;
 
 import batch.config.DatabaseRepositoryProducer;
 import batch.job.HelloWorldJobProducer;
 
 public class HelloBatchMain {
 
 	public static void main(String[] args)
 			throws JobExecutionAlreadyRunningException, JobRestartException,
 			JobInstanceAlreadyCompleteException, JobParametersInvalidException {
 		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
 				HelloWorldJobProducer.class, DatabaseRepositoryProducer.class);
 		Job job = context.getBean("helloWorldJob", Job.class);
 
 		JobLauncher launcher = context.getBean(JobLauncher.class);
 		HashMap<String, JobParameter> parameters = new HashMap<String, JobParameter>();
 		parameters.put("run", new JobParameter("c"));
 		launcher.run(job, new JobParameters(parameters));
 	}
 
 }
