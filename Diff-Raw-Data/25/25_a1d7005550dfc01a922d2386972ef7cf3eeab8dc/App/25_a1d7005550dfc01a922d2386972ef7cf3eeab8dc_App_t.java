 package com.mrroman.springgames;
 
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletRegistration;
 
 import org.springframework.web.WebApplicationInitializer;
 import org.springframework.web.context.ContextLoaderListener;
 import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
 import org.springframework.web.servlet.DispatcherServlet;
 
 public class App implements WebApplicationInitializer {
 
 	public void onStartup(ServletContext container)
 			throws javax.servlet.ServletException {
 		AnnotationConfigWebApplicationContext applContext = new AnnotationConfigWebApplicationContext();
 		applContext.register(Config.class);
 
 		container.addListener(new ContextLoaderListener(applContext));
 
		FilterRegistration.Dynamic characterEncodingFilter = container.addFilter("characterEncodingFilter", new CharacterEncodingFilter());
		characterEncodingFilter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
		characterEncodingFilter.setInitParameter("encoding", "UTF-8");
		characterEncodingFilter.setInitParameter("forceEncoding", "true");
		
 		ServletRegistration.Dynamic dispatcher = container.addServlet(
 				"dispatcher", new DispatcherServlet(applContext));
 		dispatcher.setLoadOnStartup(1);
 		dispatcher.addMapping("/");
 	}
 
 }
