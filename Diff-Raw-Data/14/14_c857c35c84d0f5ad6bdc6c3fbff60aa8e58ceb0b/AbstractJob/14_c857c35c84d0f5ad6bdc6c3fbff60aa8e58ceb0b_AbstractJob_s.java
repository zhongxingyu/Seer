 package jobs;
 
import java.lang.annotation.Annotation;
import java.util.Date;
import java.util.concurrent.TimeUnit;

 import play.Logger;
 import play.libs.Akka;
 import play.libs.Time.CronExpression;
import scala.concurrent.ExecutionContext;
 import scala.concurrent.duration.Duration;
 import scala.concurrent.duration.FiniteDuration;
 
 public abstract class AbstractJob implements Runnable {
 
   private final CronExpression cronExpression;
 
   private boolean restartOnFail = true;
 
   public AbstractJob() throws Exception {
     final Annotation annotation = this.getClass().getAnnotation(AkkaJob.class);
     final AkkaJob akkaJob = (AkkaJob) annotation;
 
     final String annoCronExpression = akkaJob.cronExpression().trim();
 
     final boolean validExpression = CronExpression.isValidExpression(annoCronExpression);
     if (validExpression == false) {
       final String message = "The annotated cronExpression: " + annoCronExpression + " is not a valid CronExpression in class: " + this.getClass().getName();
       Logger.error(message);
       throw new Exception(message);
     }
     cronExpression = new CronExpression(annoCronExpression);
     scheduleJob();
   }
 
   /**
    * Schedules the job
    */
   private void scheduleJob() {
     final long nextInterval = cronExpression.getNextInterval(new Date());
     final FiniteDuration duration = Duration.create(nextInterval, TimeUnit.MILLISECONDS);
    Akka.system().scheduler().scheduleOnce(duration,this,null);  //scheduleOnce(duration, this);
   }
 
   @Override
   public void run() {
     try {
       // TODO: stopwatching how long the job is running
       runInternal();
     } catch (final Exception e) {
       Logger.error("An error happend in the internal implementation of the job: " + this.getClass().getCanonicalName(), e);
       if (restartOnFail == false) {
         if (Logger.isDebugEnabled() == true) {
           Logger.debug("Will not restart the job: " + this.getClass().getCanonicalName());
         }
         return;
       }
     }
 
     scheduleJob();
   }
 
   public void setRestartOnFail(final boolean restartOnFail) {
     this.restartOnFail = restartOnFail;
   }
 
   /**
    * Here you can implement the actual doing of the job
    */
   public abstract void runInternal();
 
 }
