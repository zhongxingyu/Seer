 /*
 * Copyright 2010 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.codehaus.groovy.grails.plugins.batch;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextListener;
 import javax.servlet.ServletContextEvent;
 
 import org.codehaus.groovy.grails.web.context.GrailsConfigUtils;
 import org.codehaus.groovy.grails.commons.GrailsApplication;
 import org.codehaus.groovy.grails.commons.ApplicationHolder;
 
 import org.springframework.web.context.support.WebApplicationContextUtils;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.mock.web.MockServletContext;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.access.BootstrapException;
 import org.springframework.web.context.support.XmlWebApplicationContext;
 
 import org.codehaus.groovy.grails.commons.GrailsBootstrapClass;
 import org.codehaus.groovy.grails.commons.GrailsClass;
 import org.codehaus.groovy.grails.commons.spring.GrailsApplicationContext;
 import org.codehaus.groovy.grails.commons.BootstrapArtefactHandler;
 import org.codehaus.groovy.grails.compiler.injection.GrailsAwareClassLoader;
 import org.codehaus.groovy.grails.web.util.Log4jConfigListener;
 import org.codehaus.groovy.grails.web.context.GrailsContextLoaderListener;
 import org.codehaus.groovy.grails.plugins.PluginManagerHolder;
 import org.codehaus.groovy.grails.plugins.GrailsPluginManager;
 import org.codehaus.groovy.grails.plugins.DefaultGrailsPluginManager;
 
 
 import grails.util.GrailsUtil;
 import grails.util.Environment;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 //import org.apache.log4j.Logger;
 
 /**
  * @author Daniel Henrique Alves Lima
  */
 public class Bootstrap {
 
     private final String className = getClass().getName();
     private final Log log = LogFactory.getLog(getClass());
     //    public static final Logger LOGGER = Logger.getLogger(Bootstrap.class);
 
     private ServletContext servletContext;
     private ServletContextListener [] servletContextListeners;
 
     private WebApplicationContext webContext;
 
     private final boolean logEnabled;
     
     private Thread shutdownHook;
     
     public Bootstrap() {
 	logEnabled = "true".equals(getSystemProperty("debugBootstrap", "false"));
     }
 
     public void init(String [] args) {
 	logDebug(true, "init(): begin");
 
 	logDebug(true, "init(): this classLoader ", this.getClass().getClassLoader());
 	logDebug(true, "init(): thread classLoader ", Thread.currentThread().getContextClassLoader());
 
 	logDebug(true, "init(): env ", Environment.getCurrent());
 
 	String resourcePath = getSystemProperty("resourcePath", null);
 	if (resourcePath == null) {
 	    switch(Environment.getCurrent()) {
 	    case PRODUCTION:
 		resourcePath = "war";
 		break;
 	    default:
 		resourcePath = "web-app";
 	    }
 	}
 	 
 	logDebug(true, "init(): resourcePath ", resourcePath);
 
 	servletContext = resourcePath != null? new MockServletContext(resourcePath): new MockServletContext();
 	servletContext.setAttribute("args", args);
 
 	servletContextListeners = new ServletContextListener[] {
 	    new Log4jConfigListener(),
 	    new GrailsContextLoaderListener()
 	};
 	
 	this.shutdownHook = new Thread() {
 		
 		public void run() {
 		    logDebug(true, "shutdown hook run():");
 		    Bootstrap.this.destroy();
 		}
 	    };
 	
 	Runtime.getRuntime().addShutdownHook(this.shutdownHook);
 	logDebug(true, "init(): shutdown hook added");
 
 	try
 	    {
 		ServletContextEvent event = new ServletContextEvent(servletContext);
 		for (ServletContextListener l : servletContextListeners) {
 		    l.contextInitialized(event);
 		}
 	    } catch (RuntimeException e) {
 	    log.error("init()", e);
 	    throw e;
 	}
 	
 
 	// No fixed context defined for this servlet - create a local one.
 	/*XmlWebApplicationContext parent = new XmlWebApplicationContext();
 	  parent.setServletContext(servletContext);
 	  //parent.setNamespace(getClass().getName() + ".CONTEXT.");
 	  parent.refresh();*/
 	/*WebApplicationContext parent = WebApplicationContextUtils.getWebApplicationContext(servletContext);
 
 	WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(servletContext);
 	// construct the SpringConfig for the container managed application
 	//Assert.notNull(parent, "Grails requires a parent ApplicationContext, is the /WEB-INF/applicationContext.xml file missing?");
 	GrailsApplication application = parent.getBean(GrailsApplication.APPLICATION_ID, GrailsApplication.class);
     
 	//WebApplicationContext webContext;
 	if (wac instanceof GrailsApplicationContext) {
 	    webContext = wac;
 	}
 	else {
 	    webContext = GrailsConfigUtils.configureWebApplicationContext(servletContext, parent);
 
 	    try {
 		GrailsConfigUtils.executeGrailsBootstraps(application, webContext, servletContext);
 	    }
 	    catch (Exception e) {
 		log.debug("init()", e);
 		GrailsUtil.deepSanitize(e);
 		if (e instanceof BeansException) {
 		    throw (BeansException)e;
 		}
 	
 		throw new BootstrapException("Error executing bootstraps", e);
 	    }
 	    }*/
 
 
 	logDebug("init(): thread classLoader ", Thread.currentThread().getContextClassLoader());
 	logDebug("init(): end");
     }
 
     public void destroy() {
 	logDebug("destroy(): begin");
 
 	//GrailsApplication grailsApplication = webContext.getBean(GrailsApplication.APPLICATION_ID, GrailsApplication.class);
     
 	GrailsApplication grailsApplication = ApplicationHolder.getApplication();
 
 	GrailsClass[] bootstraps =  grailsApplication.getArtefacts(BootstrapArtefactHandler.TYPE);
 	for (int i = bootstraps.length - 1; i >= 0; i--) {
 	    GrailsClass bootstrap = bootstraps[i];
 	    ((GrailsBootstrapClass) bootstrap).callDestroy();
 	}
 
 
 	{
 	    ServletContextEvent event = new ServletContextEvent(servletContext);
 	    for (int i = servletContextListeners.length - 1; i >=0; i--) {
 		ServletContextListener l = servletContextListeners[i];
 		l.contextDestroyed(event);
 	    }
 	}
 
 	if (shutdownHook != null) {
 	    if (!shutdownHook.isAlive()) {
 		Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
 		logDebug(true, "destroy(): shutdown hook removed");
 	    }
 	    this.shutdownHook = null;
 	}
 
 	servletContext = null;
 
 	logDebug(true, "destroy(): end");
     }
 
     private static String getSystemProperty(String propertyName, String defaultValue) {
 	propertyName = "grails.plugins.batch." + propertyName;
 	String value = System.getProperty(propertyName);
 	if (value != null && value.length() <= 0) {
 	    value = null;
 	}
 
 	return value != null? value: defaultValue;
     }
 
 
     private void logDebug(String message, Object ... extra) {
 	logDebug(false, message, extra);
     }
 
     private void logDebug(boolean forceSysOut, String message, Object ... extra) {
 	if (logEnabled) {
 	    StringBuilder msg = new StringBuilder(message);
 	    for (Object x : extra) {
 		if (x != null) {
 		    msg.append(x.toString());
 		} else {
 		    msg.append("null");
 		}
 	    }
 
 	    if (log.isDebugEnabled() && !forceSysOut) {
 		log.debug(msg.toString());
 	    } else {
 		System.out.print("[");
 		System.out.print(className);
 		System.out.print("] ");
 		System.out.println(msg);
 	    }
 	}
     }
 
     public static void main(String [] args) {
 	Bootstrap r = new Bootstrap();
 	r.logDebug(true, "main(): begin ", new java.util.Date());
 
 	try {
 	    r.init(args);
 	} finally {
 	    try {
 		r.destroy();
 	    } finally {
 		r.logDebug(true, "main(): end ", new java.util.Date());
 	    }
 	}
     }
 }
