 package com.github.epelizzon.mte.configuration;
 
 import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Import;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.web.servlet.config.annotation.EnableWebMvc;
 import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
 
 @Configuration
 @EnableWebMvc
@ComponentScan(basePackages = {"com.github.epelizzon.mte.webapp"})
 @Import({HibernateConfiguration.class})
 public class SpringConfiguration extends WebMvcConfigurerAdapter {
     
     @Bean
     public PropertyPlaceholderConfigurer placeholderConfigurer() {
         final PropertyPlaceholderConfigurer propertyPlaceholderConfigurer = new PropertyPlaceholderConfigurer();
         propertyPlaceholderConfigurer.setLocation(new ClassPathResource("/"));
         propertyPlaceholderConfigurer.setIgnoreUnresolvablePlaceholders(true);
         return propertyPlaceholderConfigurer;
     }
 }
