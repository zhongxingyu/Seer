 package gmaggess.sample.web.config.spring;
 
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.web.servlet.config.annotation.EnableWebMvc;
 import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
 
 @Configuration
 @ComponentScan({"gmaggess.sample.web"})
 @EnableWebMvc
 public class SampleWebConfig extends WebMvcConfigurerAdapter {
 
 }
