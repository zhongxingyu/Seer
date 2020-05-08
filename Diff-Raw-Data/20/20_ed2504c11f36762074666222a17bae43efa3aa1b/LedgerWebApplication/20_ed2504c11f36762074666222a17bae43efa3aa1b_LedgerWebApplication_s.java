 package com.bitresolution.ledger.web;
 
 import com.bitresolution.ledger.core.LedgerApplication;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Import;
 import org.springframework.web.servlet.LocaleResolver;
 import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
 import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
 import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
 import org.springframework.web.servlet.i18n.CookieLocaleResolver;
 import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
 import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
 import org.thymeleaf.dialect.IDialect;
 import org.thymeleaf.spring3.SpringTemplateEngine;
 import org.thymeleaf.spring3.dialect.SpringStandardDialect;
 import org.thymeleaf.spring3.view.ThymeleafViewResolver;
 import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
 import org.thymeleaf.templateresolver.TemplateResolver;
 
 import java.util.HashSet;
 import java.util.Set;
 
 @Configuration
 @ComponentScan(basePackageClasses = LedgerWebApplication.class)
 @Import({LedgerApplication.class})
 public class LedgerWebApplication extends WebMvcConfigurationSupport {
 
 
     @Override
     @Bean
     public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
        RequestMappingHandlerAdapter adapter = super.requestMappingHandlerAdapter();
        return adapter;
     }
 
     @Override
     public void addInterceptors(InterceptorRegistry registry) {
         // Changes the locale when a 'locale' request parameter is sent; e.g. /?locale=de
         registry.addInterceptor(new LocaleChangeInterceptor());
     }
 
     @Override
     public void addResourceHandlers(ResourceHandlerRegistry registry) {
         registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
     }
 
     /**
      * Saves a locale change using a cookie
      */
     @Bean
     public LocaleResolver getLocaleResolver() {
         return new CookieLocaleResolver();
     }
 
     @Bean
     public TemplateResolver templateResolver() {
         ServletContextTemplateResolver resolver = new ServletContextTemplateResolver();
         resolver.setPrefix("/WEB-INF/views/");
         resolver.setSuffix(".html");
         resolver.setTemplateMode("HTML5");
         return resolver;
     }
 
     @Bean
     @Autowired(required = true)
     public SpringTemplateEngine templateEngine(TemplateResolver templateResolver) {
         SpringTemplateEngine engine = new SpringTemplateEngine();
         engine.setTemplateResolver(templateResolver);
         engine.setDialects(dialects());
         return engine;
     }
 
     private Set<IDialect> dialects() {
         HashSet<IDialect> dialects = new HashSet<IDialect>();
         dialects.add(new SpringStandardDialect());
 //        dialects.add(new LayoutDialect());
         return dialects;
     }
 
     @Bean
     @Autowired(required = true)
     public ThymeleafViewResolver viewResolver(SpringTemplateEngine templateEngine) {
         ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
         viewResolver.setTemplateEngine(templateEngine);
         return viewResolver;
     }
 }
