 /*
  * ConfigurationServlet.java
  *
  * Copyright (c) 2009 FooBrew, Inc.
  */
 package org.j2free.servlet;
 
 import java.net.InetAddress;
 import java.util.Enumeration;
 import java.util.Iterator;
 
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.j2free.email.EmailService;
 import org.j2free.jsp.tags.cache.FragmentCache;
 import org.j2free.servlet.filter.InvokerFilter;
 
 import org.j2free.util.Priority;
 import static org.j2free.util.Constants.*;
 
 
 /**
  * <tt>ConfigurationServlet</tt> should be loaded first by specifying
  * &lt;load-on-startup&gt;1&lt;/load-on-startup&gt; in web.xml.  This
  * servlet sets all context init params as context attributes.
  *
  * Additionally, <tt>ConfigurationServlet</tt> will look for a config
  * properties file in the default location, or a custom location if
  * the context init-param <tt>config-file</tt> is specified and, if
  * found, will set context-attributes for all properties.
  *
  * If a <tt>Configuration</tt> is found and successfully loaded, then
  * the following items will be configured accordingly:
  *  - <tt>RunMode</tt>
  *  - <tt>InvokerFilter</tt>
  *  - <tt>StaticJspServlet</tt>
  *  - <tt>LogoutServlet</tt>
  *  - <tt>ProxyServlet</tt>
  *  - <tt>SecureServlet</tt>
  *  - <tt>J2FreeAdminServlet</tt>
  *  - <tt>FragmentCache</tt>
  *  - <tt>FragmentCleaner</tt>
  *  - <tt>Task ScheduledExecutorService</tt>
  *
  * Lacking a <tt>Configuration</tt> default values will be used, and
  * the above items will not be initialized.
  *
  * Applications looking to use a custom configuration, or enable
  * additional J2Free services, such as <tt>EmailService</tt>
  * will need to extend <tt>ConfigurationServlet</tt>, having the
  * extended class set to &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
  * and override the init method. The overriding class can take advantage
  * of the <tt>ConfigurationServlet</tt> functionality by calling:
  * <code>
  *  public void init() throws ServletException {
  *      super.init();
  *      ...
  *  }
  * </code>
  *
  * Finally, this method of configuration can be ignore entirely by
  * removing the <tt>servlet-mapping</tt>.
  *
  * @see <tt>org.j2free.util.Constants</tt> for a list of configurable
  * properties.
  *
  * @author ryan
  */
 public class ConfigurationServlet extends HttpServlet {
 
     private static final Log log = LogFactory.getLog(ConfigurationServlet.class);
 
     @Override
     public void init() throws ServletException {
         super.init();
 
         ServletContext context = getServletContext();
 
         // (1) Set the context init-params as context-attribute, camelCase too
         Enumeration params = context.getInitParameterNames();
         String prop, camel, value;
         while (params.hasMoreElements()) {
 
             prop  = (String)params.nextElement();
             value = context.getInitParameter(prop);
             //camel = ServletUtils.toCamelCase(prop);
 
             log.debug("Setting application context attribute: [name=" + prop + ",value=" + value + "]");
             context.setAttribute(prop, value);
             /*
             if (!prop.equals(camel)) {
                 log.debug("Setting application context attribute: [name=" + camel + ",value=" + value + "]");
                 context.setAttribute(camel, value);
             }
              */
         }
 
         // Get the configuration file
         String configPath = (String)context.getAttribute(CONTEXT_ATTR_CONFIG_PATH);
 
         if (configPath == null || configPath.equals(EMPTY))
             configPath = DEFAULT_CONFIG_PATH;
 
         try {
             
             Configuration config = new PropertiesConfiguration(configPath);
             context.setAttribute(CONTEXT_ATTR_CONFIG,config);
 
             // Anything with the value "localhost" will be set to the IP if possible
             String localhost = "localhost";
             try {
                 localhost = InetAddress.getLocalHost().getHostAddress();
             } catch (Exception e) { }
 
             // (2) Set application context attributes for all config properties
             Iterator itr = config.getKeys();
             while (itr.hasNext()) {
 
                 prop  = (String)itr.next();
                 value = config.getString(prop);
                 value = (value.equals("localhost") ? localhost : value);
                 //camel = ServletUtils.toCamelCase(prop);
 
                 log.debug("Setting application context attribute: [name=" + prop + ",value=" + value + "]");
                 context.setAttribute(prop,value);
                 
                 /* Don't set camel-case versions...
                 if (!prop.equals(camel)) {
                     log.debug("Setting application context attribute: [name=" + camel + ",value=" + value + "]");
                     context.setAttribute(camel,value);
                 }
                  */
             }
 
             // (3) Run Mode configuration
             String runMode = config.getString(PROP_RUNMODE);
             try {
                 RUN_MODE = RunMode.valueOf(runMode);
             } catch (Exception e) {
                 log.warn("Error setting runmode, invalid value: " + runMode);
             }
 
             context.setAttribute("devMode",RUN_MODE != RunMode.PRODUCTION);
 
             // (4) InvokerFilter
             if (config.getBoolean(PROP_INVOKER_ON,false)) {
                 InvokerFilter.enable(
                         config.getString(PROP_INVOKER_BYPASSPATH,EMPTY),
                         config.getString(PROP_INVOKER_CONTROLLER,EMPTY),
                         config.getBoolean(PROP_INVOKER_BENCHMARK, false)
                     );
             }
 
             // (5) StaticJspServlet
             if (config.getBoolean(PROP_STATICJSP_ON, false)) {
 
                 String staticJspDir  = config == null ? DEFAULT_STATICJSP_DIR : config.getString(PROP_STATICJSP_DIR,DEFAULT_STATICJSP_DIR);
                 String staticJspPath = config == null ? DEFAULT_STATICJSP_PATH : config.getString(PROP_STATICJSP_PATH,DEFAULT_STATICJSP_PATH);
 
                 Set<String> staticJsps = context.getResourcePaths(staticJspDir);
                 if (staticJsps != null && !staticJsps.isEmpty()) {
                     for (String jsp : staticJsps) {
                         jsp = staticJspPath + jsp.replace(staticJspDir, EMPTY).replaceAll("\\.jsp$", EMPTY);
                         InvokerFilter.addServletMapping(jsp, StaticJspServlet.class);
                     }
                 }
             }
 
             // (6) LogoutServlet
             if (config.getBoolean(PROP_SERVLET_LOGOUT_ON,false))
                 addServletMapping(config, PROP_SERVLET_LOGOUT_PATH, DEFAULT_LOGOUT_PATH, LogoutServlet.class);
 
             // (7) ProxyServlet
             if (config.getBoolean(PROP_SERVLET_PROXY_ON,false))
                 addServletMapping(config, PROP_SERVLET_PROXY_PATH, DEFAULT_PROXY_PATH, ProxyServlet.class);
 
             // (8) SecureServlet
            if (config.getBoolean(PROP_SERVLET_SECURE_ON,false))
                 addServletMapping(config, PROP_SERVLET_SECURE_PATH, DEFAULT_SECURE_PATH, SecureServlet.class);
 
             // (9) Admin Servlet
             if (config.getBoolean(PROP_SERVLET_ADMIN_ON,false))
                 addServletMapping(config, PROP_SERVLET_ADMIN_PATH, DEFAULT_ADMIN_PATH, J2FreeAdminServlet.class);
 
             // (10) Fragment Cache Configuration
             boolean cacheEnabled = config.getBoolean(PROP_FRAGMENT_CACHE_ON);
             FragmentCache.ENABLED.set(cacheEnabled);
 
             // This is expected to be in seconds
             long temp = config.getLong(PROP_FRAGMENT_REQUEST_TIMEOUT,-1l);
             if (temp != -1)
                 FragmentCache.REQUEST_TIMEOUT.set(temp);
 
             // The property is in seconds, but WARNING_COMPUTE_DURATION does NOT use a TimeUnit, so it's in ms
             temp = config.getLong(PROP_FRAGMENT_WARNING_DURATION,-1l);
             if (temp != -1)
                 FragmentCache.WARNING_COMPUTE_DURATION.set(temp * 1000);
 
             // (11) Fragment Cleaner
             long interval = config.getLong(PROP_FRAGMENT_CLEANER_INTERVAL, DEFAULT_FRAGMENT_CLEANER_INTERVAL);
             FragmentCache.scheduleCleaner(interval, TimeUnit.SECONDS, false);
 
             // (12) For Task execution
             if (config.getBoolean(PROP_TASK_EXECUTOR_ON,false)) {
 
                 int threads = config.getInt(PROP_TASK_EXECUTOR_THREADS,DEFAULT_TASK_EXECUTOR_THREADS);
 
                 ScheduledExecutorService taskExecutor;
                 if (threads == 1)
                     taskExecutor = Executors.newSingleThreadScheduledExecutor();
                 else
                     taskExecutor = Executors.newScheduledThreadPool(threads);
                 
                 context.setAttribute(CONTEXT_ATTR_TASK_MANAGER,taskExecutor);
             }
 
             // (13) Email Service
             if (config.getBoolean(PROP_MAIL_SERVICE_ON, false)) {
 
                 EmailService.initialize(
                         config.getInt(PROP_MAIL_SERVICE_COREPOOL),
                         config.getInt(PROP_MAIL_SERVICE_MAXPOOL),
                         config.getLong(PROP_MAIL_SERVICE_KEEPALIVE)
                     );
 
                 if (config.getBoolean(PROP_MAIL_DUMMY_MODE,false))
                     EmailService.enableDummyMode();
                 else
                     EmailService.disableDummyMode();
 
                 String policy = config.getString(PROP_MAIL_ERROR_POLICY);
                 if (policy != null) {
                     if (policy.equals(VALUE_MAIL_POLICY_DISCARD)) {
 
                         EmailService.setErrorPolicy(new EmailService.DiscardPolicy());
                         
                     } else if (policy.equals(VALUE_MAIL_POLICY_REQUEUE)) {
 
                         Priority priority = null;
                         try {
                             priority = Priority.valueOf(config.getString(PROP_MAIL_REQUEUE_PRIORITY));
                         } catch (Exception e) {
                             log.warn("Error reading requeue policy priority: " + config.getString(PROP_MAIL_REQUEUE_PRIORITY,"") + ", using default");
                         }
 
                         if (priority == null)
                             EmailService.setErrorPolicy(new EmailService.RequeuePolicy());
                         else
                             EmailService.setErrorPolicy(new EmailService.RequeuePolicy(priority));
 
                     } else if (policy.equals(VALUE_MAIL_POLICY_RQAP)) {
                         
                         Priority priority = null;
                         try {
                             priority = Priority.valueOf(config.getString(PROP_MAIL_RQAP_PRIORITY));
                         } catch (Exception e) {
                             log.warn("Error reading requeue-and-pause policy priority: " + config.getString(PROP_MAIL_RQAP_PRIORITY,"") + ", using default");
                         }
 
                         String pattern = config.getString(PROP_MAIL_RQAP_INTERVAL,DEFAULT_MAIL_RQAP_INTERVAL);
                         EmailService.setErrorPolicy(new EmailService.RequeueAndPause(priority, pattern));
 
                     }
                 }
             }
 
         } catch (ConfigurationException ce) {
             log.error("Error loading configuration",ce);
         }
         
     }
 
     private void addServletMapping(Configuration config, String pathProp, String defaultPath, Class<? extends HttpServlet> servletClass) {
 
         String path;
         Class  oldKlass;
 
         Iterator itr;
 
         List paths = config.getList(pathProp);
 
         if (paths == null) {
             oldKlass = InvokerFilter.addServletMapping(defaultPath, servletClass);
             if (oldKlass != null)
                 log.error("Error mapping " + servletClass.getSimpleName() + ", " + oldKlass.getSimpleName() + " was alread mapped to " + defaultPath);
         } else {
             itr = paths.iterator();
             while (itr.hasNext()) {
                 path = (String)itr.next();
                 oldKlass = InvokerFilter.addServletMapping(path, servletClass);
                 if (oldKlass != null)
                     log.error("Error mapping " + servletClass.getSimpleName() + ", " + oldKlass.getSimpleName() + " was alread mapped to " + path);
             }
         }
     }
     
 }
