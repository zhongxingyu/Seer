 package com.tpg.tmjug.springdata.demo.mongodb.config;
 
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
 import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
 
 import com.mongodb.Mongo;
 import com.mongodb.WriteConcern;
 
 @Configuration
@EnableMongoRepositories(basePackages = "com.tpg.tmjug.springdata.demo.repository")
 public abstract class ApplicationConfig extends AbstractMongoConfiguration {
 
 	@Override
 	protected String getDatabaseName() {
 		return "tmjug_db";
 	}
 
 	@Override
 	@Bean
 	public Mongo mongo() throws Exception {
 		Mongo mongo = new Mongo();
 		mongo.setWriteConcern(WriteConcern.SAFE);
 
 		return mongo;
 	}
 
 	@Override
 	public @Bean SimpleMongoDbFactory mongoDbFactory() throws Exception {
 		return new SimpleMongoDbFactory(mongo(), getDatabaseName());
 	}
 
 	public @Bean MongoTemplate mongoTemplate() throws Exception {
 		MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory());
 		mongoTemplate.setWriteResultChecking(WriteResultChecking.LOG);
 
 		return mongoTemplate;
 	}
 
 	@Override
 	protected String getMappingBasePackage() {
 		return "com.tpg.tmjug.springdata.demo";
 	}	
 }
