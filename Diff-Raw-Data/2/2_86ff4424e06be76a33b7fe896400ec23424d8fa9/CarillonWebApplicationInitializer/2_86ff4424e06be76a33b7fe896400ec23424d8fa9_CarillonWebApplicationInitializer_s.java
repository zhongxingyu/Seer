 package org.carillonlib.webcore.application.config;
 
 import java.util.Map;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRegistration;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.WebApplicationInitializer;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
 import org.springframework.web.servlet.DispatcherServlet;
 
 public class CarillonWebApplicationInitializer implements WebApplicationInitializer {
 
 	public static final String CONFIG_PACKAGE_NAME = "carillonconf";
 
 	private Logger log = LoggerFactory.getLogger(getClass());
 
 	@Override
 	public void onStartup(ServletContext servletContext) throws ServletException {
 		log.info(getClass().getSimpleName() + " beginning initialization for context: " + servletContext.getContextPath());
 
 		AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
 		log.debug("Setting active spring profile: " + getActiveProfile());
 		applicationContext.getEnvironment().setActiveProfiles(getActiveProfile());
 		applicationContext.scan(CONFIG_PACKAGE_NAME);
 
 		ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher", new DispatcherServlet(applicationContext));
 		dispatcher.setLoadOnStartup(1);
 		dispatcher.addMapping("/app/*");
 
 		applicationContext.refresh();
 		executeInitializationDelegates(servletContext, applicationContext);
 
 	}
 
 	protected void executeInitializationDelegates(ServletContext servletContext, WebApplicationContext applicationContext) {
 		Map<String, ? extends CarillonApplicationInitDelegate> delegates = applicationContext.getBeansOfType(CarillonApplicationInitDelegate.class);
 		for (CarillonApplicationInitDelegate delegate : delegates.values()) {
 			delegate.onCarillonStartup(applicationContext);
 		}
 	}
 
 	protected String getActiveProfile() {
 		String activeProfile = System.getProperty("spring.activeProfile");
 		if (activeProfile != null) {
 			return activeProfile;
 		}
 
 		// default to development profile
 		return CarillonSpringProfiles.DEVELOPMENT;
 	}
 
 }
