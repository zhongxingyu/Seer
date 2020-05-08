 package dkwestbr.spring.autowired.example;
 
import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.Map;
 
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRegistration;
 
 import org.springframework.web.WebApplicationInitializer;
 import org.springframework.web.context.ContextLoaderListener;
 import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
 
import com.sun.jersey.spi.container.servlet.ServletContainer;
 import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
 
 import dkwestbr.spring.autowired.example.config.AppConfig;
 
 public class Initializer implements WebApplicationInitializer {
 
 	@Override
 	public void onStartup(ServletContext context) throws ServletException {
 		AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
 		appContext.register(AppConfig.class);
 		
 		context.addListener(new ContextLoaderListener(appContext));
 		
 		
         Map<String, String> filterParameters = new HashMap<>();
 
         // set filter parameters
         filterParameters.put("com.sun.jersey.config.property.packages", "dkwestbr.spring.autowired.example");
         filterParameters.put("com.sun.jersey.config.property.JSPTemplatesBasePath", "/WEB-INF/app");
         filterParameters.put("com.sun.jersey.config.property.WebPageContentRegex", "/(images|css|jsp)/.*");
 
         
         SpringServlet servlet = new SpringServlet();   
         ServletRegistration.Dynamic servletDispatcher = context.addServlet("jersey-servlet", servlet);
         servletDispatcher.setInitParameters(filterParameters);
         servletDispatcher.setLoadOnStartup(1);
         servletDispatcher.addMapping("/*");
         
         /*
         // register filter
         FilterRegistration.Dynamic filterDispatcher = context.addFilter("webFilter", new ServletContainer());
         filterDispatcher.setInitParameters(filterParameters);
         filterDispatcher.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");
 	    */
 	}
 
 }
