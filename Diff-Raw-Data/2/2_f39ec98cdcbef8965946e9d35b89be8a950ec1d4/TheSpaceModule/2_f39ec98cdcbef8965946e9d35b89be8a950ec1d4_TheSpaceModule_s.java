 package org.atlasapi.remotesite.space;
 
 import com.metabroadcast.common.properties.Configurer;
 import javax.annotation.PostConstruct;
 
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.joda.time.LocalTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 
 import com.metabroadcast.common.scheduling.RepetitionRules;
 import com.metabroadcast.common.scheduling.RepetitionRules.Daily;
 import com.metabroadcast.common.scheduling.SimpleScheduler;
 import org.atlasapi.persistence.content.ContentGroupResolver;
 import org.atlasapi.persistence.content.ContentGroupWriter;
 import org.atlasapi.persistence.content.ContentResolver;
 
 @Configuration
 public class TheSpaceModule {
 
     private final static Daily DAILY = RepetitionRules.daily(new LocalTime(4, 30, 0));
     private @Autowired
     SimpleScheduler scheduler;
     private @Autowired
     ContentResolver contentResolver;
     private @Autowired
     ContentWriter contentWriter;
     private @Autowired
     ContentGroupResolver groupResolver;
     private @Autowired
     ContentGroupWriter groupWriter;
     private @Autowired
     AdapterLog log;
 
     @PostConstruct
     public void startBackgroundTasks() throws Exception {
         scheduler.schedule(theSpaceUpdater().withName("TheSpace Updater"), DAILY);
         log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("Installed TheSpace updater"));
     }
 
     @Bean
     public TheSpaceUpdater theSpaceUpdater() throws Exception {
        return new TheSpaceUpdater(contentResolver, contentWriter, groupResolver, groupWriter, log, Configurer.get("space.keystore.path").get(), Configurer.get("space.keystore.password").get());
     }
 }
