 package dk.sst.snomedcave.config;
 
 import dk.sst.snomedcave.controllers.ConceptController;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.web.servlet.config.annotation.EnableWebMvc;
 import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
 import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
 
 @Configuration
 @EnableWebMvc
 public class WebConfig extends WebMvcConfigurerAdapter {
     @Override
     public void addResourceHandlers(ResourceHandlerRegistry registry) {
         registry.addResourceHandler("/*").addResourceLocations("/");
     }
 
     @Bean
     public ConceptController snomedController() {
         return new ConceptController();
     }
 }
