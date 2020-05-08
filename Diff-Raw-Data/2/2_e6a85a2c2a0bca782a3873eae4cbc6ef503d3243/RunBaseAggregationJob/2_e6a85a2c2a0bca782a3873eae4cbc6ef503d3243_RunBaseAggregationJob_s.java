 package loaylitymonitor.quartz;
 
 import org.quartz.Job;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.batch.core.launch.support.CommandLineJobRunner;
 
 import java.io.File;
 
 public class RunBaseAggregationJob implements Job {
     private static Logger logger = LoggerFactory.getLogger(RunBaseAggregationJob.class);
 
     public void execute(JobExecutionContext context)
             throws JobExecutionException {
 
         System.out.println("Start cron job");
         logger.info("Cron start executing ");
         try {
             // TODO: remove these pronting, only for debug
             // Directory path here
             String path = ".";
 
             String files;
             File folder = new File(path);
             File[] listOfFiles = folder.listFiles();
 
             for (int i = 0; i < listOfFiles.length; i++)
             {
 
                 if (listOfFiles[i].isFile())
                 {
                     files = listOfFiles[i].getName();
                     System.out.println(files);
                 }
             }
 
             // TODO: run spring batch job
            CommandLineJobRunner.main(new String[]{"classpath*:spring-context.xml", "baseAggJob"});
         } catch (Exception e) {
             logger.error("Can't run spring batch 'baseAggJob' job" + e);
         }
 
     }
 }
