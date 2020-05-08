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
		deadLineDate.add(Calendar.DAY_OF_MONTH, 1);
 		
 		Timer timer = new Timer();
 		contractDeadLineTimer.setDate(deadLineDate);
 
 		
 		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, 15);
		long period = 90000;
 		timer.schedule(contractDeadLineTimer, new Date(cal.getTimeInMillis()), period);
 		
 	}
 
 
 }
