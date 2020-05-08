 package com.edify.config;
 
 import com.edify.web.servlet.EnvironmentInterceptor;
 import com.edify.web.support.i18n.CustomReloadableResourceBundleMessageSource;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.support.ReloadableResourceBundleMessageSource;
 import org.springframework.ui.context.support.ResourceBundleThemeSource;
 import org.springframework.web.multipart.commons.CommonsMultipartResolver;
 import org.springframework.web.servlet.config.annotation.*;
 import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
 import org.springframework.web.servlet.i18n.CookieLocaleResolver;
 import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
 import org.springframework.web.servlet.mvc.WebContentInterceptor;
 import org.springframework.web.servlet.theme.CookieThemeResolver;
 import org.springframework.web.servlet.theme.ThemeChangeInterceptor;
 import org.springframework.web.servlet.view.UrlBasedViewResolver;
 
 import java.util.Properties;
 
 /**
  * @author jarias
  * @since 9/2/12 12:04 PM
  */
 @EnableWebMvc
 @Configuration
 public class WebConfig extends WebMvcConfigurerAdapter {
     @Value("${assets.CacheSeconds}")
     private int assetsCacheSeconds;
 
     @Override
     public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
         configurer.enable();
     }
 
     @Override
     public void addViewControllers(ViewControllerRegistry registry) {
         //registry.addViewController("/login");
         registry.addViewController("/").setViewName("index");
         registry.addViewController("/uncaughtException");
         registry.addViewController("/resourceNotFound");
         registry.addViewController("/dataAccessFailure");
     }
 
     @Override
     public void addResourceHandlers(ResourceHandlerRegistry registry) {
         registry.addResourceHandler("/resources/**").addResourceLocations("classpath:/META-INF/web-resources/");
     }
 
     @Override
     public void addInterceptors(InterceptorRegistry registry) {
         registry.addInterceptor(new ThemeChangeInterceptor());
         LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
         localeChangeInterceptor.setParamName("lang");
         registry.addInterceptor(localeChangeInterceptor);
         registry.addInterceptor(environmentInterceptor());
         WebContentInterceptor webContentInterceptor = new WebContentInterceptor();
         webContentInterceptor.setUseExpiresHeader(true);
         webContentInterceptor.setUseCacheControlHeader(true);
         webContentInterceptor.setUseCacheControlNoStore(false);
         webContentInterceptor.setCacheSeconds(assetsCacheSeconds);
        registry.addInterceptor(webContentInterceptor).addPathPatterns("/css/*.css", "/js/*.js", "/img/*.png", "/img/*.jpg", "/*.ico");
     }
 
     @Bean
     public EnvironmentInterceptor environmentInterceptor() {
         return new EnvironmentInterceptor();
     }
 
     @Bean(name = "messageSource")
     public CustomReloadableResourceBundleMessageSource messageSource() {
         CustomReloadableResourceBundleMessageSource messageSource = new CustomReloadableResourceBundleMessageSource();
         messageSource.setBasenames("WEB-INF/i18n/messages");
         messageSource.setFallbackToSystemLocale(false);
         messageSource.setCacheSeconds(1);
         return messageSource;
     }
 
     @Bean(name = "localeResolver")
     public CookieLocaleResolver cookieLocaleResolver() {
         CookieLocaleResolver cookieLocaleResolver = new CookieLocaleResolver();
         cookieLocaleResolver.setCookieName("locale");
         return cookieLocaleResolver;
     }
 
     @Bean(name = "themeSource")
     public ResourceBundleThemeSource resourceBundleThemeSource() {
         return new ResourceBundleThemeSource();
     }
 
     @Bean(name = "themeResolver")
     public CookieThemeResolver themeResolver() {
         CookieThemeResolver themeResolver = new CookieThemeResolver();
         themeResolver.setCookieName("theme");
         themeResolver.setDefaultThemeName("standard");
         return themeResolver;
     }
 
     @Bean
     public SimpleMappingExceptionResolver exceptionResolver() {
         SimpleMappingExceptionResolver exceptionResolver = new SimpleMappingExceptionResolver();
         exceptionResolver.setDefaultErrorView("uncaughtException");
         Properties exceptionMappings = new Properties();
         exceptionMappings.setProperty(".DataAccessException", "dataAccessFailure");
         exceptionMappings.setProperty(".NoSuchRequestHandlingMethodException", "resourceNotFound");
         exceptionMappings.setProperty(".TypeMismatchException", "resourceNotFound");
         exceptionMappings.setProperty(".MissingServletRequestParameterException", "resourceNotFound");
         exceptionResolver.setExceptionMappings(exceptionMappings);
         return exceptionResolver;
     }
 
     @Bean(name = "multipartResolver")
     public CommonsMultipartResolver multipartResolver() {
         return new CommonsMultipartResolver();
     }
 
     @Bean(name = "jstlViewResolver")
     public UrlBasedViewResolver tilesViewResolver() {
         UrlBasedViewResolver viewResolver = new UrlBasedViewResolver();
         viewResolver.setViewClass(org.springframework.web.servlet.view.JstlView.class);
         viewResolver.setPrefix("/WEB-INF/views/");
         viewResolver.setSuffix(".jsp");
         return viewResolver;
     }
 }
