 package time;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Timer;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.Singleton;
 import javax.ejb.Startup;
 import javax.enterprise.context.ApplicationScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
import util.Config;

 
 @ApplicationScoped
 @Singleton
 @Startup
 @Named("scheduler")
 public class Scheduler {
 	
 	@Inject
 	private ContractDeadLineTimer contractDeadLineTimer;
 	
 
 	public Scheduler() {
 	}
 
 	@PostConstruct
 	public void schedule(){
 		
 		Calendar deadLineDate = Calendar.getInstance();
		deadLineDate.add(Calendar.DAY_OF_MONTH, Config.daysBeforeDeadlineExpriration);
 		
 		Timer timer = new Timer();
 		contractDeadLineTimer.setDate(deadLineDate);
 
 		
 		Calendar cal = Calendar.getInstance();
		if(cal.get(Calendar.HOUR_OF_DAY) >= Config.dailyScheduledTaskExecutionHour){
			cal.add(Calendar.DAY_OF_MONTH, 1);			
		}
		cal.set(Calendar.HOUR_OF_DAY, Config.dailyScheduledTaskExecutionHour);
		long period = 1000*60*60*24;
 		timer.schedule(contractDeadLineTimer, new Date(cal.getTimeInMillis()), period);
 		
 	}
 
 
 }
