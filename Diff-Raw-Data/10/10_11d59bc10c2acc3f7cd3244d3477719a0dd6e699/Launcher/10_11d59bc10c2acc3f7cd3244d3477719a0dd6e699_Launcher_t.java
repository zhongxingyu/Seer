 package com.inertia.solutions.threading;
 
 import java.io.Console;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import org.apache.log4j.Logger;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;
 
 import com.inertia.solutions.threading.bean.ThreadResultBean;
 import com.inertia.solutions.threading.task.LoggingTask;
 
 public final class Launcher {
 	private static final Logger log = Logger.getLogger(Launcher.class);
 
 	private static final String THREAD_COUNT_INPUT = "\nPlease input the number of the task threads (Q to quit)";
 	private static final String QUIT = "Q";
 	
 	private Launcher() { }
 	
	/*
	 * to execute using the console:
	 * mvn exec:java -Dexec.mainClass="com.inertia.solutions.threading.Launcher"
	 */
 	public static void main(final String[] args) throws InterruptedException, ExecutionException {
 
 		ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfiguration.class);
 		LoggingTask loggingTask = ctx.getBean(LoggingTask.class);
 		
 		Console console = System.console();
 		String input = console.readLine(THREAD_COUNT_INPUT);
 		while (!QUIT.equalsIgnoreCase(input)) {
 			Integer threadCount = new Integer(input);
 			List<Future<ThreadResultBean>> futureList = new ArrayList<Future<ThreadResultBean>>();
 			for(int i=1;i<=threadCount;i++){
 				Future<ThreadResultBean> future = loggingTask.executeLogging(i);
 				futureList.add(future);
 			}
 			
 			for(Future<ThreadResultBean> future:futureList) {
 				ThreadResultBean bean = future.get();
 				log.info("After Execution: " + bean.toString());
 			}
 			
 			input = console.readLine("\nPlease enter calculation (Q to quit)");
 		}
 		
 		((AnnotationConfigApplicationContext) ctx).close();
 	}
 }
