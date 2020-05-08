 package com.bookshop.config;
 
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;
 
 import com.bookshop.controllers.BookController;
 import com.mongodb.Mongo;
 
 @Configuration
 @ComponentScan(basePackageClasses = BookController.class)
 @EnableWebMvc
 public class ApplicationConfig {
 
 	@Bean
 	public MongoTemplate mongoTemplate() throws Exception{
 		Mongo mongo = new Mongo("localhost", 27017);
 		MongoTemplate mongoTemplate = new MongoTemplate(mongo, "bookshop");
 		return mongoTemplate;
 	}
	
	@Bean
	public MappingJacksonJsonView jsonView(){
		MappingJacksonJsonView jsonView = new MappingJacksonJsonView();
		jsonView.setPrefixJson(true);
		return jsonView;
	}
 }
