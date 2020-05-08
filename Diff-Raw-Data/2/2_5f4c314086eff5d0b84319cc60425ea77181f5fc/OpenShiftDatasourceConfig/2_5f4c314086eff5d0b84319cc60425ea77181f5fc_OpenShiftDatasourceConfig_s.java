 package com.localjobs.config;
 
 import javax.sql.DataSource;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Profile;
 import org.springframework.data.authentication.UserCredentials;
 import org.springframework.data.mongodb.MongoDbFactory;
 import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
 import org.springframework.orm.jpa.vendor.Database;
 
 import com.mongodb.Mongo;
 
 @Configuration
 @Profile("openshift")
 public class OpenShiftDatasourceConfig implements DatasourceConfig {
 
     @Bean(destroyMethod = "close")
     public DataSource dataSource() {
         String username = System.getenv("OPENSHIFT_POSTGRESQL_DB_USERNAME");
         String password = System.getenv("OPENSHIFT_POSTGRESQL_DB_PASSWORD");
         String host = System.getenv("OPENSHIFT_POSTGRESQL_DB_HOST");
         String port = System.getenv("OPENSHIFT_POSTGRESQL_DB_PORT");
         String databaseName = System.getenv("OPENSHIFT_APP_NAME");
         String url = "jdbc:postgresql://" + host + ":" + port + "/" + databaseName;
         BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.postgresql.Driver");
         dataSource.setUrl(url);
         dataSource.setUsername(username);
         dataSource.setPassword(password);
         dataSource.setTestOnBorrow(true);
         dataSource.setTestOnReturn(true);
         dataSource.setTestWhileIdle(true);
         dataSource.setTimeBetweenEvictionRunsMillis(1800000);
         dataSource.setNumTestsPerEvictionRun(3);
         dataSource.setMinEvictableIdleTimeMillis(1800000);
         dataSource.setValidationQuery("SELECT version()");
 
         return dataSource;
     }
 
     @Bean
     public MongoDbFactory mongoDbFactory() throws Exception {
         String openshiftMongoDbHost = System.getenv("OPENSHIFT_MONGODB_DB_HOST");
         int openshiftMongoDbPort = Integer.parseInt(System.getenv("OPENSHIFT_MONGODB_DB_PORT"));
         String username = System.getenv("OPENSHIFT_MONGODB_DB_USERNAME");
         String password = System.getenv("OPENSHIFT_MONGODB_DB_PASSWORD");
         Mongo mongo = new Mongo(openshiftMongoDbHost, openshiftMongoDbPort);
         UserCredentials userCredentials = new UserCredentials(username, password);
         String databaseName = System.getenv("OPENSHIFT_APP_NAME");
         MongoDbFactory mongoDbFactory = new SimpleMongoDbFactory(mongo, databaseName, userCredentials);
         return mongoDbFactory;
     }
 
     @Override
     public Database database() {
         return Database.POSTGRESQL;
     }
 }
