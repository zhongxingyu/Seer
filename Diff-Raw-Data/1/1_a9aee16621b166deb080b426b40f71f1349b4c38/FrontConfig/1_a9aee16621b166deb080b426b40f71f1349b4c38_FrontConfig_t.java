 package org.cloudfoundry.samples.handson.config;
 
 import org.cloudfoundry.samples.handson.ex4.Ex4Config;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.ComponentScan.Filter;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.servlet.DispatcherServlet;
 import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
 import org.springframework.web.servlet.config.annotation.EnableWebMvc;
 import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
 import org.springframework.web.servlet.view.InternalResourceViewResolver;
 import org.springframework.web.servlet.view.JstlView;
 
 /**
  * Configuration class for beans that are needed from the start.
  * 
  * As a convenience, you can add more beans in dedicated configuration classes
  * (eg. {@link Ex4Config}) , they will all be merged in a single
  * {@link ApplicationContext}
  * 
  * @author Eric Bottard
  * 
  */
 @Configuration
 @EnableWebMvc
 public class FrontConfig extends WebMvcConfigurerAdapter {
 
 	/**
 	 * Allows controllers to just return a String as a view name, that will be
 	 * resolved as a jsp page inside /WEB-INF/views.
 	 */
 	@Bean
 	public InternalResourceViewResolver internalResourceViewResolver() {
 		InternalResourceViewResolver internalResourceViewResolver = new InternalResourceViewResolver();
 		internalResourceViewResolver.setViewClass(JstlView.class);
 		internalResourceViewResolver.setPrefix("/WEB-INF/views/");
 		internalResourceViewResolver.setSuffix(".jsp");
 		return internalResourceViewResolver;
 	}
 
 	/**
 	 * Allows mapping the {@link DispatcherServlet} to "/" while still being
 	 * able to serve static resources.
 	 */
 	@Override
 	public void configureDefaultServletHandling(
 			DefaultServletHandlerConfigurer configurer) {
 		configurer.enable();
 	}
 }
