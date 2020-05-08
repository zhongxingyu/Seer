 package cz.cvut.fel.bupro.config;
 
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.context.MessageSource;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.support.ResourceBundleMessageSource;
 import org.springframework.web.servlet.ViewResolver;
 import org.thymeleaf.extras.springsecurity3.dialect.SpringSecurityDialect;
 import org.thymeleaf.spring3.SpringTemplateEngine;
 import org.thymeleaf.spring3.view.ThymeleafViewResolver;
 import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
 import org.thymeleaf.templateresolver.TemplateResolver;
 
 @Configuration
 public class MvcConfig {
 
 	@Bean
 	public TemplateResolver templateResolver() {
 		TemplateResolver resolver = new ServletContextTemplateResolver();
 		resolver.setPrefix("/WEB-INF/templates/"); //prefixes any controller RequestMapping return value
 		resolver.setSuffix(".html"); //suffixes any RequestMapping return value
 		resolver.setTemplateMode("HTML5");
 		return resolver;
 	}
 
 	@Bean
 	public SpringTemplateEngine templateEngine() {
 		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
 		templateEngine.setTemplateResolver(templateResolver());
 		templateEngine.setCacheManager(null); //FIXME devel prevent caching
 		templateEngine.addDialect(new SpringSecurityDialect()); //enable spring security extension
 		return templateEngine;
 	}
 
 	@Bean
 	public ViewResolver viewResolver() {
 		ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
 		viewResolver.setTemplateEngine(templateEngine());
		viewResolver.setCharacterEncoding("UTF-8");
 		return viewResolver;
 	}
 
 	@Bean
 	public MessageSource messageSource() {
 		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
 		messageSource.setBasename("locale/messages");
 		return messageSource;
 	}
 
 	@Bean @Qualifier(Qualifiers.EMAIL)
 	public MessageSource emailMessageSource() {
 		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
 		messageSource.setBasename("locale/emails");
 		return messageSource;
 	}
 
 }
