 package de.adtelligence.friendfilter.config;
 
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.web.servlet.ViewResolver;
 import org.springframework.web.servlet.config.annotation.EnableWebMvc;
 import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
 import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
 import org.springframework.web.servlet.view.InternalResourceViewResolver;
 
 @Configuration
 @EnableWebMvc
 public class WebMvcConfig {
 
   public void addViewControllers(ViewControllerRegistry registry) {
   }
 
   @Bean
   public ViewResolver viewResolver() {
     InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
     viewResolver.setPrefix("/WEB-INF/views/");
     viewResolver.setSuffix(".jsp");
     return viewResolver;
   }
   
   public void addResourceHandlers(ResourceHandlerRegistry registry) {
     registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
     }
 }
