 package no.niths.common.config;
 
 import no.niths.application.rest.helper.CustomMultipartResolver;
 
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.web.multipart.MultipartResolver;
 import org.springframework.web.servlet.ViewResolver;
 import org.springframework.web.servlet.view.InternalResourceViewResolver;
 
 @Configuration
 @ComponentScan(AppConfig.WEB_PACKAGE)
 public class MVCConfig {
 
     public MVCConfig() {
         super();
     }
 
     @Bean
     public ViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
         viewResolver.setPrefix("/WEB-INF/views/");
         viewResolver.setSuffix(".jsp");
         return viewResolver;
     }
 
     @Bean
     public MultipartResolver multipartResolver() {
         return new CustomMultipartResolver();
     }
 }
