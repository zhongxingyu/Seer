 package com.natepaulus.dailyemail.web.config;
 
 import java.util.List;
 import java.util.Properties;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.MessageSource;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.ImportResource;
 import org.springframework.context.support.ReloadableResourceBundleMessageSource;
 import org.springframework.scheduling.annotation.EnableAsync;
 import org.springframework.scheduling.annotation.EnableScheduling;
 import org.springframework.ui.velocity.VelocityEngineFactoryBean;
 import org.springframework.web.servlet.HandlerExceptionResolver;
 import org.springframework.web.servlet.HandlerMapping;
 import org.springframework.web.servlet.config.annotation.EnableWebMvc;
 import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
 import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
 import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
 import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
 import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
 import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
 import org.springframework.web.servlet.mvc.support.ControllerClassNameHandlerMapping;
 
 /**
  * The Class WebMvcContextConfiguration configures the settings for Spring Web
  * MVC.
  */
 @Configuration
 @EnableWebMvc
 @EnableAsync
 @EnableScheduling
 @ImportResource("WEB-INF/lib/applicationContext.xml")
 @ComponentScan(basePackages = "com.natepaulus.dailyemail")
 public class WebMvcContextConfiguration extends WebMvcConfigurerAdapter {
 
 	/** The logger. */
 	final Logger logger = LoggerFactory
 			.getLogger(WebMvcContextConfiguration.class);
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
 	 * #addResourceHandlers(org.springframework.web.servlet.config.annotation.
 	 * ResourceHandlerRegistry)
 	 */
 	@Override
 	public void addResourceHandlers(ResourceHandlerRegistry registry) {
 		registry.addResourceHandler("/resources/**")
 				.addResourceLocations("/public-resources/")
 				.setCachePeriod(31556926);
 		registry.addResourceHandler("/robots.txt")
 				.addResourceLocations("/robots.txt")
 				.setCachePeriod(31556926);
 		
 	}
 
 	/**
 	 * Message source.
 	 * 
 	 * @return the message source
 	 */
 	@Bean
 	public MessageSource messageSource() {
 		ReloadableResourceBundleMessageSource messageSource;
 		messageSource = new ReloadableResourceBundleMessageSource();
 		messageSource.setBasename("WEB-INF/lib/errors");
 		messageSource.setUseCodeAsDefaultMessage(true);
 		return messageSource;
 	}
 
 	/**
 	 * Controller class name handler mapping.
 	 * 
 	 * @return the handler mapping
 	 */
 	@Bean
 	public HandlerMapping controllerClassNameHandlerMapping() {
 		return new ControllerClassNameHandlerMapping();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
 	 * #configureHandlerExceptionResolvers(java.util.List)
 	 */
 	@Override
 	public void configureHandlerExceptionResolvers(
 			List<HandlerExceptionResolver> exceptionResolvers) {
 		exceptionResolvers.add(exceptionHandlerExceptionResolver());
		exceptionResolvers.add(simpleMappingExceptionResolver());		
 	}
 
 	/**
 	 * Simple mapping exception resolver.
 	 * 
 	 * @return the simple mapping exception resolver
 	 */
 	@Bean
 	public SimpleMappingExceptionResolver simpleMappingExceptionResolver() {
 		SimpleMappingExceptionResolver exceptionResolver;
 		exceptionResolver = new SimpleMappingExceptionResolver();
 		Properties mappings = new Properties();
 		mappings.setProperty("AuthenticationException", "login");
 		mappings.setProperty("AuthenticationException", "index");
 
 		Properties statusCodes = new Properties();
 		statusCodes.setProperty("login",
 				String.valueOf(HttpServletResponse.SC_UNAUTHORIZED));
 		statusCodes.setProperty("index",
 				String.valueOf(HttpServletResponse.SC_UNAUTHORIZED));
 
 		exceptionResolver.setExceptionMappings(mappings);
 		exceptionResolver.setStatusCodes(statusCodes);
 		exceptionResolver.setDefaultErrorView("errorView");
 		exceptionResolver.setOrder(2);
 
 		return exceptionResolver;
 	}
 
 	/**
 	 * Exception handler exception resolver. Set the order to 1 to handle custom
 	 * business exceptions
 	 * 
 	 * @return the exception handler exception resolver
 	 */
 	@Bean
 	public ExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver() {
 		ExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver = new ExceptionHandlerExceptionResolver();
 
 		exceptionHandlerExceptionResolver.setOrder(1);
 
 		return exceptionHandlerExceptionResolver;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
 	 * #addInterceptors(org.springframework.web.servlet.config.annotation.
 	 * InterceptorRegistry)
 	 */
 	@Override
 	public void addInterceptors(InterceptorRegistry registry) {
 		@SuppressWarnings("unused")
 		InterceptorRegistration registration = registry.addInterceptor(
 				new SecurityHandlerInterceptor()).addPathPatterns("/account",
 				"/account/*", "/reader", "/reader/*");
 
 	}
 
 	/**
 	 * Velocity engine.
 	 * 
 	 * @return the velocity engine factory bean
 	 */
 	@Bean
 	public VelocityEngineFactoryBean velocityEngine() {
 		VelocityEngineFactoryBean velocityFactoryBean = new VelocityEngineFactoryBean();
 		Properties velocityProperties = new Properties();
 		velocityProperties.put("resource.loader", "class");
 		velocityProperties
 				.put("class.resource.loader.class",
 						"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
 		velocityFactoryBean.setVelocityProperties(velocityProperties);
 		return velocityFactoryBean;
 	}
 
 }
