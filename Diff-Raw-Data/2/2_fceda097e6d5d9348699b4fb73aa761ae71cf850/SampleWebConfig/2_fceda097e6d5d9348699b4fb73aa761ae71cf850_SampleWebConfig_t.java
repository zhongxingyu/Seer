package gmaggess.sample.web.config.spring;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.http.MediaType;
 import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
 import org.springframework.web.servlet.View;
 import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
 import org.springframework.web.servlet.config.annotation.EnableWebMvc;
 import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
 import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
 import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
 
 @Configuration
 @ComponentScan({"gmaggess.sample.web"})
 @EnableWebMvc
 public class SampleWebConfig extends WebMvcConfigurerAdapter {
 
 }
