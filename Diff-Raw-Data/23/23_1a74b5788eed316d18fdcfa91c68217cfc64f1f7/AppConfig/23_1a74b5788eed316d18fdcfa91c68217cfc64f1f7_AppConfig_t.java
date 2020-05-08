 package com.nytimes.eps.war.container.config;
 
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.ViewResolver;
 import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
 
 import bigbank.BankDao;
 import bigbank.BankDaoStub;
 import bigbank.BankService;
 import bigbank.BankServiceImpl;
 import bigbank.SeedData;
 
 @Configuration
 //EnableWebMvc is the annotation needed
 //for automatic json serialization via jackson
 @EnableWebMvc
//@ImportResource("classpath:/applicationContext-security.xml")
@ComponentScan("bigbank.web")
 public class AppConfig {
   
   @Bean
   public BankService bankService() {
     return new BankServiceImpl(bankDao());
   }
   
   @Bean 
   public BankDao bankDao() {
     return new BankDaoStub();
   }
   
   @Bean
   public SeedData seedData() {
     SeedData seedData = new SeedData();
     seedData.setBankDao(bankDao());
     return seedData;
   }
  
  @Bean  
  public ViewResolver viewResolver() {  
    InternalResourceViewResolver resolver = new InternalResourceViewResolver();  
    resolver.setPrefix("WEB-INF/jsp/");  
    resolver.setSuffix(".jsp");  
    return resolver;  
  }  
 
 }
