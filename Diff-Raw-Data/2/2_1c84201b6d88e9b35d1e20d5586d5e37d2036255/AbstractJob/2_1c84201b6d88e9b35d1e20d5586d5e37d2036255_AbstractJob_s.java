 package jobs;
 
 import java.lang.annotation.Annotation;
 import java.util.Date;
 import java.util.concurrent.TimeUnit;
 
 import play.Logger;
 import play.libs.Akka;
 import play.libs.Time.CronExpression;
 import akka.util.Duration;
 import akka.util.FiniteDuration;
 
 public abstract class AbstractJob implements Runnable {
 
   private final CronExpression cronExpression;
 
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
     Akka.system().scheduler().scheduleOnce(duration, this);
   }
 
   @Override
   public void run() {
     try {
       runInternal();
     } catch (final Exception e) {
      Logger.error("An error happend in the internal implementation of the job: " + this.getClass().getName());
     }
 
     scheduleJob();
   }
 
   public abstract void runInternal();
 
 }
