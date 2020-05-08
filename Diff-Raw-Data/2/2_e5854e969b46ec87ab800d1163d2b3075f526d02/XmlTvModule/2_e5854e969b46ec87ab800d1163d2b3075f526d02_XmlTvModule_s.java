 package org.atlasapi.feeds.xmltv;
 
 import javax.annotation.PostConstruct;
 
 import org.atlasapi.feeds.upload.persistence.FileUploadResultStore;
 import org.atlasapi.feeds.upload.persistence.MongoFileUploadResultStore;
 import org.atlasapi.feeds.upload.s3.S3FileUploader;
 import org.atlasapi.feeds.xmltv.upload.XmlTvUploadHealthProbe;
 import org.atlasapi.feeds.xmltv.upload.XmlTvUploadService;
 import org.atlasapi.feeds.xmltv.upload.XmlTvUploadTask;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.content.KnownTypeContentResolver;
 import org.atlasapi.persistence.content.ScheduleResolver;
 import org.joda.time.LocalTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 
 import com.google.common.collect.ImmutableSet;
 import com.metabroadcast.common.health.HealthProbe;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.scheduling.RepetitionRules;
 import com.metabroadcast.common.scheduling.SimpleScheduler;
 import com.metabroadcast.common.security.UsernameAndPassword;
 import com.metabroadcast.common.webapp.health.HealthController;
 
 @Configuration
 public class XmlTvModule {
     
     private static final String SERVICE_NAME = "xmltv";
     
     @Autowired ScheduleResolver scheduleResolver;
     @Autowired KnownTypeContentResolver contentResolver;
     @Autowired SimpleScheduler scheduler;
     @Autowired DatabasedMongo mongo;
     @Autowired HealthController health;
     @Autowired FileUploadResultStore resultStore;
     
     private @Value("${xmltv.upload.enabled}") String uploadEnabled;
     private @Value("${xmltv.upload.bucket}") String s3bucket;
     private @Value("${xmltv.upload.folder}") String s3folder;
     private @Value("${s3.access}") String s3access;
     private @Value("${s3.secret}") String s3secret;
 
     public @Bean XmlTvController xmlTvController() {
         return new XmlTvController(xmltvFeedCompiler(), xmlTvChannels(), health);
     }
 
    private @Bean XmlTvChannelLookup xmlTvChannels() {
         return new XmlTvChannelLookup();
     }
 
     private @Bean XmlTvFeedCompiler xmltvFeedCompiler() {
         return new XmlTvFeedCompiler(scheduleResolver, contentResolver, Publisher.PA);
     }
     
     @PostConstruct
     public void scheduleUploadTask() {
         if(Boolean.valueOf(uploadEnabled)) {
             final XmlTvUploadService uploadService = new XmlTvUploadService(SERVICE_NAME, new S3FileUploader(new UsernameAndPassword(s3access, s3secret), s3bucket, s3folder));
             final XmlTvUploadTask uploadTask = new XmlTvUploadTask(uploadService, new MongoFileUploadResultStore(mongo), xmltvFeedCompiler(), xmlTvChannels());
             scheduler.schedule(uploadTask.withName("XmlTv Upload"), RepetitionRules.daily(new LocalTime(04,30,00)));
             health.addProbes(createProbes());
         }
     }
     
     private Iterable<HealthProbe> createProbes() {
         return ImmutableSet.<HealthProbe>of(new XmlTvUploadHealthProbe(xmlTvChannels(), resultStore));
     }
 
     static final String FEED_PREABMLE = "\t\nIn accessing this XML feed, you agree that you will only access its contents for your own personal " +
     		"and non-commercial use and not for any commercial or other purposes, including advertising or selling any goods or services, " +
     		"including any third-party software applications available to the general public.";
     
 }
