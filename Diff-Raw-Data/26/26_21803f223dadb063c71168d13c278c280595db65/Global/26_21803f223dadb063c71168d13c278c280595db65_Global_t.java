 import java.util.concurrent.TimeUnit;
 
 
 import play.Application;
 import play.GlobalSettings;
 import play.Logger;
 import play.libs.Akka;
 import workers.StatisticsGenerator;
 import akka.util.Duration;
 
 public class Global extends GlobalSettings {
 
     @Override
     public void onStart(Application app) {
 	Logger.info("Application has started");
 	Akka.system().scheduler().schedule(
 		Duration.create(0, TimeUnit.MILLISECONDS), 
		Duration.create(5, TimeUnit.MINUTES), 
 	new Runnable() {
 
 	    @Override
 	    public void run() {
 		StatisticsGenerator.updateStatistics();
 	    }
 	});
     }
 
     @Override
     public void onStop(Application app) {
 	Logger.info("Application shutdown...");
     }
 
 }
