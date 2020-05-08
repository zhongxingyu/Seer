 package org.accesointeligente.server;
 
import com.ibm.icu.util.Calendar;

 import java.util.Timer;
 
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 public class BackgroundServiceManager implements ServletContextListener {
 	private Timer timer;
 
 	@Override
 	public void contextDestroyed(ServletContextEvent event) {
 		timer = null;
 	}
 
 	@Override
 	public void contextInitialized(ServletContextEvent event) {
 		timer = new Timer();
 		timer.schedule(new ResponseCheckerTask(), 60000, 3600000);
 
 		Calendar cal = Calendar.getInstance();
 		cal.add(Calendar.DAY_OF_MONTH, 1);
 		cal.set(Calendar.HOUR_OF_DAY, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 
 		timer.schedule(new RequestCreationTask(), cal.getTime(), 86400000);
 		timer.schedule(new RequestUpdateTask(), 60000, 3600000);
 	}
 }
