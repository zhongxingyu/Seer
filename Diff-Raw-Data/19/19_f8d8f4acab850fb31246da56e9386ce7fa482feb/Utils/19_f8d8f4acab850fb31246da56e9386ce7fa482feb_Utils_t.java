 package pt.com.broker.monitorization;
 
 import java.util.Date;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import org.caudexorigo.concurrent.CustomExecutors;
 import org.caudexorigo.text.DateUtil;
 
 public class Utils
 {
 	private static ScheduledThreadPoolExecutor scheduledThreadPool;
 	static {
 		scheduledThreadPool = CustomExecutors.newScheduledThreadPool(4, "Monitor-Sched");
 	}
 	
 	public static String formatDate(long time)
 	{
 		Date date = new Date(time);
 
 		return DateUtil.formatISODate(date);
 	}
 	
	public static void schedule(Runnable task, long initialDelay, long delay, TimeUnit unit)
 	{
		scheduledThreadPool.scheduleWithFixedDelay(task, initialDelay, delay, unit);
 	}
 	public static void execute(Runnable task)
 	{
 		scheduledThreadPool.execute(task);
 	}
 }
