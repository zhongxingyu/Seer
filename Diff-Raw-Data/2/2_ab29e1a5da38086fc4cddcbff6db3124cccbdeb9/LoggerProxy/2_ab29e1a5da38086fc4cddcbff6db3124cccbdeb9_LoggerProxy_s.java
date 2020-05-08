 package blogger.activator;
 
 import java.lang.reflect.Method;
 
 public class LoggerProxy {
 
     private Object bloggerService;
     private Method methodInfo;
     private Method methodWarn;
     private Method methodDebug;
     private Method methodError;
     
     private static LoggerProxy instance = new LoggerProxy();
     
     private LoggerProxy() {	
     }
     
     public static LoggerProxy getInstance() {
 	return instance;
     }
 
     protected void setBloggerService(Object aBloggerService) {
 	bloggerService = aBloggerService;
 
 	if (bloggerService != null) {
 	    Method[] methods = bloggerService.getClass().getMethods();
 	    for (Method method : methods) {
 		String name = method.getName();
 		if (new String("info").equals(name)) {
 		    methodInfo = method;
 		} else if (new String("warn").equals(name)) {
 		    methodWarn = method;
 		} else if (new String("debug").equals(name)) {
 		    methodDebug = method;
 		} else if (new String("error").equals(name)) {
 		    methodError = method;
 		}
 	    }
 	}
     }
 
     public void info(Class<?> aClass, String aMessage) {
 	if (methodInfo == null) {
 	    System.out.println(aClass.getSimpleName() + new String("-> ") + aMessage);
 	    return;
 	}
 
 	try {
 	    methodInfo.invoke(bloggerService, aClass, aMessage);
 	} catch (Exception e) {
 	    e.printStackTrace();
 	}
     }
 
     public void warn(Class<?> aClass, String aMessage) {
 	if (methodInfo == null) {
 	    System.out.println(aClass.getSimpleName() + new String("-> ") + aMessage);
 	    return;
 	}
 
 	try {
 	    methodWarn.invoke(bloggerService, aClass, aMessage);
 	} catch (Exception e) {
 	    e.printStackTrace();
 	}
     }
 
     public void debug(Class<?> aClass, String aMessage) {
 	if (methodInfo == null) {
 	    System.out.println(aClass.getSimpleName() + new String("-> ") + aMessage);
 	    return;
 	}
 
 	try {
 	    methodDebug.invoke(bloggerService, aClass, aMessage);
 	} catch (Exception e) {
 	    e.printStackTrace();
 	}
     }
 
     public void error(Class<?> aClass, String aMessage, Throwable aThrowable) {
 	if (methodInfo == null) {
 	    System.out.println(aClass.getSimpleName() + new String("-> ..."));
 	    aThrowable.printStackTrace();
 	    return;
 	}
 
 	try {
 	    methodError.invoke(bloggerService, aClass, aMessage, aThrowable);
 	} catch (Exception e) {
 	    e.printStackTrace();
 	}
     }
 
     public void error(Class<?> aClass, String aMessage) {
 	if (methodInfo == null) {
 	    System.out.println(aClass.getSimpleName() + new String("-> ..."));
 	    return;
 	}
 
 	try {
	    methodError.invoke(bloggerService, aClass, aMessage);
 	} catch (Exception e) {
 	    e.printStackTrace();
 	}
     }
 }
