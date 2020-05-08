 package fi.helsinki.cs.okkopa;
 
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class OkkopaMain {
 
     public static void main(String[] args) {
 
         ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-context.xml");
 
 //        ScheduledExecutorService scheduler =
 //                Executors.newScheduledThreadPool(1);
         OkkopaRunner runner = (OkkopaRunner) ctx.getBean("okkopaRunner");
         runner.run();
 //        scheduler.scheduleWithFixedDelay(runner, 0, 1, TimeUnit.MINUTES);
     }
 }
