 package eai.msejdf.timer;
 
 import javax.annotation.Resource;
 import javax.ejb.Schedule;
 import javax.ejb.Stateless;
 import javax.ejb.Timeout;
 import javax.ejb.Timer;
 import javax.ejb.TimerService;
 
 import eai.msejdf.utils.EmailUtils;
 
 /**
  * Session Bean implementation class Timer
  */
 @Stateless
 public class SchedulerBean implements ITimer {
 
 	@Resource
 	TimerService timerService;
 	
     /**
      * Default constructor. 
      */
     public SchedulerBean() {        
     }
         
 	@Override
 	public void triggerEmail()
 	{
 		
 		System.out.println("\n\tinside createTimer() ....GOT the injected TimerService: "+ timerService);
 		System.out.println("\n\ttimerService [" + timerService + "]");
 		timerService.createTimer(20000, "Created new email timer Method");
 	}
 
 	@Timeout
 	public void timeout(Timer timer)
 	{
 		System.out.println("\n\tTimeout occurred - " + timer.getInfo());
 		EmailUtils.sendEmail("test@dei.uc.pt", "davidpc@dei.uc.pt", "testing", "test message");
 	}
 
     
	@Schedule(hour="0")			// everyday at 0 hours
 	protected void dailyMailCompanyDigest() {
 		System.out.println("\nSchedule timeout occurred !!!! ");
 	}
 
 }
