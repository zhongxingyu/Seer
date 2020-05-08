 package org.util;
 
 import java.io.PrintWriter;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 
 import org.apache.commons.logging.LogFactory;
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.DailyRollingFileAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 
 import sun.reflect.Reflection;
 
 public class LogUtil {
 
 	private static final int maxStackTraceLength = 99;
 
 	static {
 		initLog4j(Level.INFO);
 	}
 
 	public static void initLog4j(Level level) {
 		PatternLayout layout = new PatternLayout("%d %-5p [%c{1}] %m%n");
 
 		ConsoleAppender console = new ConsoleAppender(layout);
		console.setWriter(new PrintWriter(System.err));
 		console.activateOptions();
 
 		DailyRollingFileAppender file = new DailyRollingFileAppender();
 		file.setFile("logs/suite.log");
 		file.setDatePattern("'.'yyyyMMdd");
 		file.setLayout(layout);
 		file.activateOptions();
 
 		Logger logger = Logger.getRootLogger();
 		logger.setLevel(level);
 		logger.removeAllAppenders();
 		logger.addAppender(console);
 		logger.addAppender(file);
 	}
 
 	public static void info(String message) {
 		info(Reflection.getCallerClass(2), message);
 	}
 
 	private static void info(Class<?> clazz, String message) {
 		LogFactory.getLog(clazz).info(message);
 	}
 
 	public static void error(Throwable th) {
 		error(Reflection.getCallerClass(2), th);
 	}
 
 	private static void error(Class<?> clazz, Throwable th) {
 		boolean isTrimmed = trimStackTrace(th);
 		LogFactory.getLog(clazz).error(isTrimmed ? "(Trimmed)" : "", th);
 	}
 
 	public static void fatal(Throwable th) {
 		fatal(Reflection.getCallerClass(2), th);
 	}
 
 	private static void fatal(Class<?> clazz, Throwable th) {
 		boolean isTrimmed = trimStackTrace(th);
 		LogFactory.getLog(clazz).fatal(isTrimmed ? "(Trimmed)" : "", th);
 	}
 
 	private static boolean trimStackTrace(Throwable th) {
 		boolean isTrimmed = false;
 
 		// Trims stack trace to appropriate length
 		while (th != null) {
 			StackTraceElement st0[] = th.getStackTrace();
 
 			if (st0.length > maxStackTraceLength) {
 				StackTraceElement st1[] = new StackTraceElement[maxStackTraceLength];
 				Util.copyArray(st0, 0, st1, 0, maxStackTraceLength);
 				th.setStackTrace(st1);
 
 				isTrimmed = true;
 			}
 
 			th = th.getCause();
 		}
 
 		return isTrimmed;
 	}
 
 	public static <I> I proxy(Class<I> interface_, final I object) {
 		@SuppressWarnings("unchecked")
 		final Class<I> clazz = (Class<I>) object.getClass();
 		ClassLoader classLoader = clazz.getClassLoader();
 		Class<?> classes[] = { interface_ };
 
 		InvocationHandler handler = new InvocationHandler() {
 			public Object invoke(Object proxy, Method method, Object ps[]) throws Exception {
 				String methodName = method.getName();
 				String prefix = methodName + "()\n";
 
 				String pd = "";
 				if (ps != null)
 					for (int i = 0; i < ps.length; i++)
 						pd += DumpUtil.dump("p" + i, ps[i]);
 
 				info(clazz, prefix + pd);
 
 				try {
 					Object value = method.invoke(object, ps);
 					String rd = DumpUtil.dump("return", value);
 					info(clazz, prefix + rd);
 					return value;
 				} catch (Exception ex) {
 					error(clazz, ex);
 					throw ex;
 				}
 			}
 		};
 
 		@SuppressWarnings("unchecked")
 		I proxied = (I) Proxy.newProxyInstance(classLoader, classes, handler);
 		return proxied;
 	}
 
 }
