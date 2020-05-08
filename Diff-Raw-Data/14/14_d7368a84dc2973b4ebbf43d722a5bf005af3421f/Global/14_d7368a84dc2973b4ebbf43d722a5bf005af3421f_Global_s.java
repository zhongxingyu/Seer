 import akka.actor.Cancellable;
 import akka.actor.Scheduler;
 import akka.dispatch.MessageDispatcher;
 import jobs.FakeEventJob;
 import jobs.FakeHistoryRecordJob;
 import jobs.RemoveOldEventJob;
 import play.Application;
 import play.Configuration;
 import play.GlobalSettings;
 import play.Logger;
 import scala.concurrent.ExecutionContext;
 import scala.concurrent.duration.Duration;
 import scala.concurrent.duration.FiniteDuration;
 
 import java.io.File;
 import java.util.LinkedList;
 import java.util.List;
 
 import static jobs.Jobs.BET365_JOB;
 import static jobs.Jobs.MARATHON_JOB;
 import static jobs.Jobs.REMOVE_OLD_HISTORY_JOB;
 import static models.store.Organisation.BET365;
 import static models.store.Organisation.MARATHON;
 import static play.Logger.of;
 import static play.Play.isProd;
 import static play.libs.Akka.system;
 import static utils.BObjects.logAndStopExceptions;
 
 public class Global
   extends GlobalSettings {
 
   private static final Logger.ALogger LOG = of(GlobalSettings.class);
 
   private List<Cancellable> schedules = new LinkedList<Cancellable>();
 
   @Override
   public void onStart(Application app) {
     super.onStart(app);
 
     Scheduler scheduler = system().scheduler();
     ExecutionContext defaultDispatcher = (ExecutionContext) system().dispatcher();
 
     if (isProd()) {
       LOG.info("Starting real Jobs.");
 
       FiniteDuration oldEventOffset = Duration.create(0, "sec");
       FiniteDuration oldEventDelay = Duration.create(1, "min");
       long oldEventAge = Duration.create(1, "day").toMillis();
       schedules.add(scheduler.schedule(oldEventOffset, oldEventDelay, logAndStopExceptions(new RemoveOldEventJob(oldEventAge)), defaultDispatcher));
 
       FiniteDuration oldHistoryOffset = Duration.create(10, "sec");
       FiniteDuration oldHistoryDelay = Duration.create(1, "min");
       schedules.add(scheduler.schedule(oldHistoryOffset, oldHistoryDelay, logAndStopExceptions(REMOVE_OLD_HISTORY_JOB), defaultDispatcher));
 
       FiniteDuration marathonOffset = Duration.create(20, "sec");
       FiniteDuration marathonDelay = Duration.create(1, "min");
      MessageDispatcher marathonFetchingDispatcher = system().dispatchers().lookup("fetch-marathon");
       schedules.add(scheduler.schedule(marathonOffset, marathonDelay, logAndStopExceptions(MARATHON_JOB), marathonFetchingDispatcher));
 
       FiniteDuration bet365Offset = Duration.create(30, "sec");
       FiniteDuration bet365Delay = Duration.create(1, "min");
      MessageDispatcher bet365FetchingDispatcher = system().dispatchers().lookup("fetch-bet365");
       schedules.add(scheduler.schedule(bet365Offset, bet365Delay, logAndStopExceptions(BET365_JOB), bet365FetchingDispatcher));
 
     } else {
       LOG.info("Starting fake Jobs.");
 
       FiniteDuration offset = Duration.create(0, "ms");
       FiniteDuration delay = Duration.create(30, "sec");
       long oldEventAge = Duration.create(5, "min").toMillis();
 
       schedules.add(scheduler.schedule(offset, delay, logAndStopExceptions(new RemoveOldEventJob(oldEventAge)), defaultDispatcher));
       schedules.add(scheduler.schedule(offset, delay, logAndStopExceptions(new FakeEventJob()), defaultDispatcher));
       schedules.add(scheduler.schedule(offset, delay, logAndStopExceptions(new FakeHistoryRecordJob(BET365)), defaultDispatcher));
       schedules.add(scheduler.schedule(offset, delay, logAndStopExceptions(new FakeHistoryRecordJob(MARATHON)), defaultDispatcher));
     }
 
   }
 
   @Override
   public void onStop(Application app) {
     super.onStop(app);
 
     for (Cancellable schedule : schedules) {
       schedule.cancel();
     }
   }
 
   @Override
   public Configuration onLoadConfig(Configuration config, File path, ClassLoader classloader) {
     return super.onLoadConfig(config, path, classloader);
   }
 }
