 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package bean.log;
 
 import java.util.Date;
 import javax.annotation.PostConstruct;
 import javax.annotation.Resource;
 import javax.ejb.ScheduleExpression;
 import javax.ejb.Singleton;
 import javax.ejb.Startup;
 import javax.ejb.Timeout;
 import javax.ejb.TimerService;
 import javax.inject.Named;
 
 /**
  *
  * @author Brian GOHIER
  */
 @Named(value="logSchedule")
 @Singleton
 @Startup
 public class LogSchedule
 {
     @Resource
     private TimerService service;
     private long start;
     
     @PostConstruct
     public void init()
     {
         this.start=(new Date()).getTime();
         ScheduleExpression exp=new ScheduleExpression();
        exp.dayOfMonth("*").hour("0").minute("0").second("0");
         service.createCalendarTimer(exp);
     }
     
     @Timeout
     public void timeOut()
     {
         ApplicationLogger.writeWarning("Archivage automatique du journal");
         ApplicationLogger.archive();
     }
     
 }
