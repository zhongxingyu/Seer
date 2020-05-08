 package de.adtelligence.friendfilter.config;
 
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.PropertySource;
 import org.springframework.context.annotation.ComponentScan.Filter;
 
 @Configuration
 @ComponentScan(basePackages = "de.adtelligence.friendfilter", excludeFilters = { @Filter(Configuration.class) })
 @PropertySource("classpath:de/adtelligence/friendfilter/config/application.properties")
public class MainConfig {

}
