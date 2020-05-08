 package com.github.kolorobot.icm.config;
 
 import com.github.kolorobot.icm.account.User;
 import com.github.kolorobot.icm.support.thymeleaf.ThymeleafLayoutInterceptor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.MessageSource;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.support.ReloadableResourceBundleMessageSource;
 import org.springframework.core.MethodParameter;
 import org.springframework.security.core.Authentication;
 import org.springframework.validation.Validator;
 import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
 import org.springframework.web.bind.support.WebDataBinderFactory;
 import org.springframework.web.context.request.NativeWebRequest;
 import org.springframework.web.method.support.HandlerMethodArgumentResolver;
 import org.springframework.web.method.support.ModelAndViewContainer;
 import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
 import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
 import org.thymeleaf.extras.springsecurity3.dialect.SpringSecurityDialect;
 import org.thymeleaf.spring4.SpringTemplateEngine;
 import org.thymeleaf.spring4.view.ThymeleafViewResolver;
 import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
 import org.thymeleaf.templateresolver.TemplateResolver;
 
 import java.util.List;
 
 @Configuration
 public class WebMvcConfig extends WebMvcConfigurationSupport {
 	
 	private static final String MESSAGE_SOURCE = "/WEB-INF/i18n/messages";
 	private static final String VIEWS = "/WEB-INF/views/";
 	
 	private static final String RESOURCES_HANDLER = "/resources/";
 	private static final String RESOURCES_LOCATION = RESOURCES_HANDLER + "**";
 
     private static final Logger LOGGER = LoggerFactory.getLogger("Config");
 
 	@Override
 	public RequestMappingHandlerMapping requestMappingHandlerMapping() {
 		RequestMappingHandlerMapping requestMappingHandlerMapping = super.requestMappingHandlerMapping();
 		requestMappingHandlerMapping.setUseSuffixPatternMatch(false);
 		requestMappingHandlerMapping.setUseTrailingSlashMatch(false);
 		return requestMappingHandlerMapping;
 	}
 	
 	@Bean(name = "messageSource")
 	public MessageSource configureMessageSource() {
 		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
 		messageSource.setBasename(MESSAGE_SOURCE);
 		messageSource.setCacheSeconds(5);
 		return messageSource;
 	}
 	
     @Bean
     public TemplateResolver templateResolver() {
         TemplateResolver templateResolver = new ServletContextTemplateResolver();
         templateResolver.setPrefix(VIEWS);
         templateResolver.setSuffix(".html");
         templateResolver.setTemplateMode("HTML5");
         templateResolver.setCacheable(false);
         return templateResolver;
     }
 
     @Bean
     public SpringTemplateEngine templateEngine() {
         SpringTemplateEngine templateEngine = new SpringTemplateEngine();
         templateEngine.addTemplateResolver(templateResolver());
         templateEngine.addDialect(new SpringSecurityDialect());
         return templateEngine;
     }
 
     @Bean
     public ViewResolver thymeleafViewResolver() {
         ThymeleafViewResolver vr = new ThymeleafViewResolver();
         vr.setTemplateEngine(templateEngine());
         vr.setCharacterEncoding("UTF-8");
         return vr;
     }
 
     @Override
     protected void addInterceptors(InterceptorRegistry registry) {
         registry.addInterceptor(new ThymeleafLayoutInterceptor());
     }
 
 	@Override
 	public Validator getValidator() {
 		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
 		validator.setValidationMessageSource(configureMessageSource());
 		return validator;
 	}
 	
 	@Override
 	public void addResourceHandlers(ResourceHandlerRegistry registry) {
 		registry.addResourceHandler(RESOURCES_HANDLER).addResourceLocations(RESOURCES_LOCATION);
 	}
 
 	@Override
 	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
 		configurer.enable();
 	}
 	
 	@Override
 	protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
 		argumentResolvers.add(new UserHandlerMethodArgumentResolver());
 	}
	
	// custom argument resolver inner classes
 
 	private static class UserHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
 
 		public boolean supportsParameter(MethodParameter parameter) {
 			return User.class.isAssignableFrom(parameter.getParameterType());
 		}
 
 		public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
 			Authentication auth = (Authentication) webRequest.getUserPrincipal();
 			return auth != null && auth.getPrincipal() instanceof User ? auth.getPrincipal() : null;
 		}
 	}
 }
