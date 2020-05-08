 package com.lyncode.oai.proxy;
 
 import static org.quartz.JobBuilder.newJob;
 import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
 import static org.quartz.TriggerBuilder.newTrigger;
 
 import java.io.File;
 import java.util.Calendar;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.eclipse.jetty.security.ConstraintMapping;
 import org.eclipse.jetty.security.ConstraintSecurityHandler;
 import org.eclipse.jetty.security.HashLoginService;
 import org.eclipse.jetty.security.SecurityHandler;
 import org.eclipse.jetty.security.authentication.BasicAuthenticator;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.util.security.Constraint;
 import org.eclipse.jetty.util.security.Credential;
 import org.eclipse.jetty.webapp.WebAppContext;
 import org.quartz.JobDetail;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 import org.quartz.SchedulerFactory;
 import org.quartz.Trigger;
 import org.quartz.impl.StdSchedulerFactory;
 
 import com.lyncode.oai.proxy.core.ConfigurationManager;
 import com.lyncode.oai.proxy.core.SolrServerManager;
 import com.lyncode.oai.proxy.job.HarvestJob;
 
 public class ProxyApplication {
 	private static Logger log = LogManager.getLogger(ProxyApplication.class);
 	private static final String ROLE = "admin";
 	private static final String REALM = "proxyRealm";
 	public static final String CONFIG_FILE = "config"+File.separator+"proxy.cfg";
 	
 	public static void main (String[] args) throws Exception {
 		ConfigurationManager.initialize(CONFIG_FILE);
 		Configuration config = ConfigurationManager.getConfiguration();
 		PropertyConfigurator.configure("config" + File.separator + "log4j.properties");
 
 		SolrServerManager.initialize();
 		
 		cronServer(config);
 		webServer(config);
 	}
 	
 	private static void cronServer (Configuration config) throws SchedulerException {
 		SchedulerFactory sf = new StdSchedulerFactory();
 		Scheduler sched = sf.getScheduler();
 		
 		JobDetail job = newJob(HarvestJob.class)
 				.withIdentity("harvestJob", "proxy")
 			    .build();
 		
 		Calendar c = Calendar.getInstance();
 		c.set(Calendar.HOUR_OF_DAY, config.getInt("schedule.hour", 0));
 		c.set(Calendar.MINUTE, config.getInt("schedule.minute", 0));
 		c.set(Calendar.SECOND, 0);
 		
 		if (c.before(Calendar.getInstance())) {
 			c.add(Calendar.DAY_OF_MONTH, 1);
 		}
 		
 		log.info("Harvest scheduled for "+c.getTime());
 		
 		Trigger trigger = newTrigger()
 				.withIdentity("harvestTrigger", "proxy")
 				.withSchedule(simpleSchedule()
 						.withIntervalInHours(24)
 						.repeatForever())
 				.startAt(c.getTime())
 				.build();
 		
 		sched.start();
 		sched.scheduleJob(job, trigger);
 		log.info("Scheduler started");
 	}
 	
 	private static void webServer (Configuration config) throws Exception {
 		Server server = new Server(config.getInt("proxy.port"));
 		
 		WebAppContext webapp = new WebAppContext();
 		webapp.setContextPath("/");
		webapp.setSecurityHandler(security(config.getString("admin.user"), config.getString("admin.pass")));
 		webapp.setWar("webapps" + File.separator + "proxy.war");
 		webapp.setParentLoaderPriority(true);
         
         server.setHandler(webapp);
         
         server.start();
 		log.info("Web server started");
         server.join();
 	}
	
	private static SecurityHandler security (String username, String password) {
		HashLoginService l = new HashLoginService();
        l.putUser(username, Credential.getCredential(password), new String[] { ROLE });
        l.setName(REALM);
        
        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
        constraint.setRoles(new String[]{ ROLE });
        constraint.setAuthenticate(true);
         
        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint(constraint);
        cm.setPathSpec("/admin*");
        
        ConstraintSecurityHandler csh = new ConstraintSecurityHandler();
        csh.setAuthenticator(new BasicAuthenticator());
        csh.setRealmName("Admin credentials could be found at config/proxy.cfg");
        csh.addConstraintMapping(cm);
        csh.setLoginService(l);
        
        return csh;
	}
 }
