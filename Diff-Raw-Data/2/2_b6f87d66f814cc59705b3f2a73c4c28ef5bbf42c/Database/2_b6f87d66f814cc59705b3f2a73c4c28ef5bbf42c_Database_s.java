 package com.kokakiwi.fun.pulsar.db;
 
 import java.net.URI;
 
 import com.avaje.ebean.EbeanServer;
 import com.avaje.ebean.EbeanServerFactory;
 import com.avaje.ebean.config.DataSourceConfig;
 import com.avaje.ebean.config.ServerConfig;
 
 public class Database
 {
     private final EbeanServer server;
     
     public Database() throws Exception
     {
         ServerConfig config = new ServerConfig();
         DataSourceConfig sourceConfig = new DataSourceConfig();
         
         URI uri = new URI(System.getenv("DATABASE_URL"));
         
         String username = uri.getUserInfo().split(":")[0];
         String password = uri.getUserInfo().split(":")[1];
         
         String dbUrl = "jdbc:postgresql://" + uri.getHost() + uri.getPath();
         
         sourceConfig.setUsername(username);
         sourceConfig.setUrl(dbUrl);
         sourceConfig.setPassword(password);
         
         config.setDataSourceConfig(sourceConfig);
         server = EbeanServerFactory.create(config);
     }
 }
