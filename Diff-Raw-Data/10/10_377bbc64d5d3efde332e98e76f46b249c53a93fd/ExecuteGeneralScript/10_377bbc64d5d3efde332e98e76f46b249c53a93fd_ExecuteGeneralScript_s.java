 package pig.experiments.pigprocessors;
 
 import org.apache.pig.backend.executionengine.ExecJob;
 import org.springframework.batch.core.StepContribution;
 import org.springframework.batch.core.scope.context.ChunkContext;
 import org.springframework.batch.repeat.RepeatStatus;
 import pig.experiments.JobConstants;
 
 import java.util.List;
 import java.util.Properties;
 
 public class ExecuteGeneralScript extends AbstractPigExecutor {
 
     @Override
     public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
         String dateTimeMarker = (String) chunkContext.getStepContext().getJobParameters().get(JobConstants.WORKING_HOUR);
 
         Properties scriptParameters = new Properties();
         scriptParameters.put("input", super.getInputPath() + dateTimeMarker);
         scriptParameters.put("output", super.getOutputPat() + dateTimeMarker);
         scriptParameters.put("parallel", 1);
 
         boolean isFailed = false;
         System.out.println("\nRun Pig job................\n");
         System.out.println( String.format("\nData will be processed from directory %s and pushed to %s",
                 super.getInputPath() + dateTimeMarker,
                 super.getOutputPat() + dateTimeMarker) );
         // we get control back, when Pig scipt will be finished
         List<ExecJob> jobs = super.getPigTemplate().executeScript(super.getSciptName(), scriptParameters);
 
         for(ExecJob job : jobs) {
             if( ExecJob.JOB_STATUS.FAILED == job.getStatus() ) {
                 isFailed = true;
             }

         }
 
         System.out.println("Fished " + (isFailed ? "unsuccessful" : "successful"));
 
 
         return RepeatStatus.FINISHED;
     }
 }
