 package org.atlasapi.remotesite.pa;
 
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
 
 import javax.annotation.PostConstruct;
 
 import org.atlasapi.feeds.upload.persistence.FileUploadResultStore;
 import org.atlasapi.media.channel.ChannelResolver;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.content.ContentGroupResolver;
 import org.atlasapi.persistence.content.ContentGroupWriter;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.content.PeopleResolver;
 import org.atlasapi.persistence.content.ScheduleResolver;
 import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
 import org.atlasapi.persistence.content.people.PersonWriter;
 import org.atlasapi.persistence.content.schedule.mongo.ScheduleWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.atlasapi.remotesite.channel4.epg.BroadcastTrimmer;
 import org.atlasapi.remotesite.channel4.epg.ScheduleResolverBroadcastTrimmer;
 import org.atlasapi.remotesite.pa.data.DefaultPaProgrammeDataStore;
 import org.atlasapi.remotesite.pa.data.PaProgrammeDataStore;
 import org.atlasapi.remotesite.pa.features.PaFeaturesUpdater;
 import org.atlasapi.remotesite.pa.people.PaCompletePeopleUpdater;
 import org.atlasapi.remotesite.pa.people.PaDailyPeopleUpdater;
 import org.atlasapi.remotesite.pa.persistence.MongoPaScheduleVersionStore;
 import org.atlasapi.remotesite.pa.persistence.PaScheduleVersionStore;
 import org.atlasapi.remotesite.rt.RtFilmModule;
 import org.atlasapi.s3.DefaultS3Client;
 import org.atlasapi.s3.S3Client;
 import org.joda.time.Duration;
 import org.joda.time.LocalTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Import;
 
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.scheduling.RepetitionRule;
 import com.metabroadcast.common.scheduling.RepetitionRules;
 import com.metabroadcast.common.scheduling.SimpleScheduler;
 import com.metabroadcast.common.security.UsernameAndPassword;
 
 @Configuration
 @Import(RtFilmModule.class)
 public class PaModule {
     private final static RepetitionRule PEOPLE_COMPLETE_INGEST = RepetitionRules.NEVER;
     private final static RepetitionRule PEOPLE_INGEST = RepetitionRules.daily(LocalTime.MIDNIGHT);
     private final static RepetitionRule FEATURES_INGEST = RepetitionRules.daily(LocalTime.MIDNIGHT);
     private final static RepetitionRule RECENT_FILE_INGEST = RepetitionRules.every(Duration.standardMinutes(10)).withOffset(Duration.standardMinutes(15));
     private final static RepetitionRule RECENT_FILE_DOWNLOAD = RepetitionRules.every(Duration.standardMinutes(10));
     private final static RepetitionRule COMPLETE_INGEST = RepetitionRules.NEVER;//weekly(DayOfWeek.FRIDAY, new LocalTime(22, 0, 0));
     
     private @Autowired SimpleScheduler scheduler;
     private @Autowired ContentWriter contentWriter;
     private @Autowired ContentResolver contentResolver;
     private @Autowired ContentGroupWriter contentGroupWriter;
     private @Autowired ContentGroupResolver contentGroupResolver;
     private @Autowired PeopleResolver personResolver;
     private @Autowired PersonWriter personWriter;
     private @Autowired AdapterLog log;
     private @Autowired ScheduleResolver scheduleResolver;
     private @Autowired ItemsPeopleWriter peopleWriter;
     private @Autowired ScheduleWriter scheduleWriter;
     private @Autowired ChannelResolver channelResolver;
     private @Autowired FileUploadResultStore fileUploadResultStore;
     private @Autowired DatabasedMongo mongo;
     private final Lock peopleLock = new ReentrantLock();
     
     private @Value("${pa.ftp.username}") String ftpUsername;
     private @Value("${pa.ftp.password}") String ftpPassword;
     private @Value("${pa.ftp.host}") String ftpHost;
     private @Value("${pa.ftp.path}") String ftpPath;
     private @Value("${pa.filesPath}") String localFilesPath;
     private @Value("${s3.access}") String s3access;
     private @Value("${s3.secret}") String s3secret;
     private @Value("${pa.s3.bucket}") String s3bucket;
     
     @PostConstruct
     public void startBackgroundTasks() {
        scheduler.schedule(paCompletePeopleUpdater().withName("PA Complete People Updater"), PEOPLE_COMPLETE_INGEST);
        scheduler.schedule(paDailyPeopleUpdater().withName("PA People Updater"), PEOPLE_INGEST);
         scheduler.schedule(paFeaturesUpdater().withName("PA Features Updater"), FEATURES_INGEST);
         scheduler.schedule(paFileUpdater().withName("PA File Updater"), RECENT_FILE_DOWNLOAD);
         scheduler.schedule(paCompleteUpdater().withName("PA Complete Updater"), COMPLETE_INGEST);
         scheduler.schedule(paRecentUpdater().withName("PA Recent Updater"), RECENT_FILE_INGEST);
         log.record(new AdapterLogEntry(Severity.INFO).withDescription("PA update scheduled task installed").withSource(PaCompleteUpdater.class));
     }
     
     private PaCompletePeopleUpdater paCompletePeopleUpdater() {
         return new PaCompletePeopleUpdater(paProgrammeDataStore(), personResolver, personWriter, peopleLock);
     }
 
     private PaDailyPeopleUpdater paDailyPeopleUpdater() {
         return new PaDailyPeopleUpdater(paProgrammeDataStore(), personResolver, personWriter, fileUploadResultStore, peopleLock);
     }
 
     @Bean PaFeaturesUpdater paFeaturesUpdater() {
         return new PaFeaturesUpdater(paProgrammeDataStore(), contentResolver, contentGroupResolver, contentGroupWriter);
     }
 
     @Bean PaFtpFileUpdater ftpFileUpdater() {
         return new PaFtpFileUpdater(ftpHost, new UsernameAndPassword(ftpUsername, ftpPassword), ftpPath, paProgrammeDataStore());
     }
     
     @Bean PaProgrammeDataStore paProgrammeDataStore() {
         S3Client s3client = new DefaultS3Client(s3access, s3secret, s3bucket);
         return new DefaultPaProgrammeDataStore(localFilesPath, s3client);
     }
     
     @Bean PaProgDataProcessor paProgrammeProcessor() {
         return new PaProgrammeProcessor(contentWriter, contentResolver, channelResolver, peopleWriter, log);
     }
     
     @Bean PaCompleteUpdater paCompleteUpdater() {
         PaEmptyScheduleProcessor processor = new PaEmptyScheduleProcessor(paProgrammeProcessor(), scheduleResolver);
         PaChannelProcessor channelProcessor = new PaChannelProcessor(processor, broadcastTrimmer(), scheduleWriter, paScheduleVersionStore());
         PaCompleteUpdater updater = new PaCompleteUpdater(channelProcessor, paProgrammeDataStore(), channelResolver);
         return updater;
     }
     
     @Bean PaRecentUpdater paRecentUpdater() {
         PaChannelProcessor channelProcessor = new PaChannelProcessor(paProgrammeProcessor(), broadcastTrimmer(), scheduleWriter, paScheduleVersionStore());
         PaRecentUpdater updater = new PaRecentUpdater(channelProcessor, paProgrammeDataStore(), channelResolver, fileUploadResultStore, paScheduleVersionStore());
         return updater;
     }
     
     @Bean BroadcastTrimmer broadcastTrimmer() {
         return new ScheduleResolverBroadcastTrimmer(Publisher.PA, scheduleResolver, contentResolver, contentWriter);
     }
     
     @Bean PaFileUpdater paFileUpdater() {
         return new PaFileUpdater(ftpFileUpdater());
     }
     
     public @Bean PaSingleDateUpdatingController paUpdateController() {
         PaChannelProcessor channelProcessor = new PaChannelProcessor(paProgrammeProcessor(), broadcastTrimmer(), scheduleWriter, paScheduleVersionStore());
         return new PaSingleDateUpdatingController(channelProcessor, scheduleResolver, channelResolver, log, paProgrammeDataStore());
     }
     
     public @Bean PaScheduleVersionStore paScheduleVersionStore() {
         return new MongoPaScheduleVersionStore(mongo);
     }
 }
