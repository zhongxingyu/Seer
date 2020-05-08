 package org.atlasapi.persistence;
 
 import com.google.common.base.Splitter;
 import com.google.common.collect.Lists;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.content.cassandra.CassandraContentStore;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 
 /**
  */
 @Configuration
 public class CassandraPersistenceModule {
     
     private @Value("${cassandra.seeds}") String seeds;
     private @Value("${cassandra.port}") int port;
     private @Value("${cassandra.connectionTimeout}") int connectionTimeout;
     private @Value("${cassandra.requestTimeout}") int requestTimeout;
     
    public @Bean ContentResolver cassandraContentResolver() {
        CassandraContentStore store = cassandraContentStore();
        return store;
    }
    
    public @Bean ContentWriter cassandraContentWriter() {
        CassandraContentStore store = cassandraContentStore();
        return store;
    }

     private @Bean CassandraContentStore cassandraContentStore() {
         return new CassandraContentStore(Lists.newArrayList(Splitter.on(',').split(seeds)), port, Runtime.getRuntime().availableProcessors() * 10, connectionTimeout, requestTimeout);
     }
 }
