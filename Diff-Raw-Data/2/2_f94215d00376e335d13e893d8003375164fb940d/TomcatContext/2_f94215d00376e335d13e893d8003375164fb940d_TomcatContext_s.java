 package dolda.jsvc.j2ee;
 
 import dolda.jsvc.*;
 import dolda.jsvc.util.*;
 import javax.servlet.*;
 import java.lang.reflect.*;
 import java.util.*;
 import java.io.*;
 import java.util.logging.*;
 
 public class TomcatContext extends J2eeContext {
     private final String name;
     private static final Logger logger = Logger.getLogger("dolda.jsvc.context");
     
     TomcatContext(ServletConfig sc) {
 	super(sc);
 	ServletContext ctx = j2eeconfig().getServletContext();
 	Class<?> cclass = ctx.getClass();
 	String name;
 	try {
 	    Method cpm = cclass.getMethod("getContextPath");
 	    name = Misc.stripslashes((String)cpm.invoke(ctx), true, true);
 	} catch(NoSuchMethodException e) {
 	    throw(new RuntimeException("Could not fetch context path from Tomcat", e));
 	} catch(IllegalAccessException e) {
 	    throw(new RuntimeException("Could not fetch context path from Tomcat", e));
 	} catch(InvocationTargetException e) {
 	    throw(new RuntimeException("Could not fetch context path from Tomcat", e));
 	} catch(SecurityException e) {
 	    logger.log(Level.WARNING, "no permissions to fetch context name from Tomcat", e);
 	    name = null;
 	}
 	this.name = name;
 	readconfig();
     }
 
     private static void loadprops(Properties props, File pfile) {
 	if(!pfile.exists())
 	    return;
 	try {
 	    InputStream in = new FileInputStream(pfile);
 	    try {
 		props.load(in);
 	    } finally {
 		in.close();
 	    }
 	} catch(IOException e) {
 	    throw(new RuntimeException(e));
 	}
     }
 
     private void readconfig() {
 	File base;
 	try {
 	    String basename = System.getProperty("catalina.base");
 	    base = new File(basename);
 	} catch(SecurityException e) {
	    logger.log(Level.WARNING, "no permssions to fetch Tomcat base directory while reading configuration", e);
 	    return;
 	}
 	config.put("jsvc.storage", "file:" + new File(new File(base, "work"), "jsvc").getPath());
 	File cdir = new File(base, "conf");
 	try {
 	    loadprops(config, new File(cdir, "jsvc.properties"));
 	} catch(SecurityException e) {
 	    logger.log(Level.WARNING, "no permssions to read from Tomcat conf directory while reading configuration", e);
 	}
     }
     
     public static boolean tomcatp(ServletConfig sc) {
 	ServletContext ctx = sc.getServletContext();
 	if(ctx.getClass().getName().equals("org.apache.catalina.core.ApplicationContextFacade"))
 	    return(true);
 	return(false);
     }
     
     public String name() {
 	return(name);
     }
 }
