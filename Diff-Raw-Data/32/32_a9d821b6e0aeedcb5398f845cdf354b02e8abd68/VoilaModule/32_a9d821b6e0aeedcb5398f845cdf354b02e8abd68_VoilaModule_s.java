 package org.atlasapi.remotesite.voila;
 
 import static org.atlasapi.remotesite.HttpClients.webserviceClient;
 
 import java.text.ParseException;
 
 import javax.annotation.PostConstruct;
 
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.mongo.MongoTopicStore;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Throwables;
 import com.google.common.net.HostSpecifier;
 import com.metabroadcast.common.scheduling.RepetitionRules;
 import com.metabroadcast.common.scheduling.SimpleScheduler;
 
 @Configuration
 public class VoilaModule {
 
     private @Value("${cannon.host.name}") String cannonHostName;
     private @Value("${cannon.host.port}") Integer cannonHostPort;
     private @Autowired ContentResolver contentResolver;
     private @Autowired ContentWriter contentWriter;
    private @Autowired MongoTopicStore topicStore;
     private @Autowired SimpleScheduler scheduler;
     private @Autowired AdapterLog log;
     
     @PostConstruct
     public void scheduleTasks() {
         scheduler.schedule(cannonTwitterTopicsUpdater(), RepetitionRules.NEVER);
     }
 
     @Bean CannonTwitterTopicsUpdater cannonTwitterTopicsUpdater() {
         return new CannonTwitterTopicsUpdater(cannonTopicsClient(), contentTopicsUpdater());
     }
     
     @Bean ContentTwitterTopicsUpdater contentTopicsUpdater() {
        return new ContentTwitterTopicsUpdater(cannonTopicsClient(), contentResolver, topicStore, contentWriter, log);
     }
     
     @Bean CannonTwitterTopicsClient cannonTopicsClient() {
         try {
             return new CannonTwitterTopicsClient(webserviceClient(), HostSpecifier.from(cannonHostName), Optional.fromNullable(cannonHostPort), log);
         } catch (ParseException e) {
             throw Throwables.propagate(e);
         }
     }
 }
