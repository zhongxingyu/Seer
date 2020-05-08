 package org.atlasapi.feeds.radioplayer;
 
 import javax.annotation.PostConstruct;
 
 import org.atlasapi.feeds.radioplayer.upload.CachingFTPUploadResultStore;
 import org.atlasapi.feeds.radioplayer.upload.CommonsFTPFileUploader;
 import org.atlasapi.feeds.radioplayer.upload.FTPCredentials;
 import org.atlasapi.feeds.radioplayer.upload.FTPFileUploader;
 import org.atlasapi.feeds.radioplayer.upload.MongoFTPUploadResultStore;
 import org.atlasapi.feeds.radioplayer.upload.RadioPlayerFTPUploadResultStore;
 import org.atlasapi.feeds.radioplayer.upload.RadioPlayerRecordingExecutor;
 import org.atlasapi.feeds.radioplayer.upload.RadioPlayerServerHealthProbe;
 import org.atlasapi.feeds.radioplayer.upload.RadioPlayerUploadController;
 import org.atlasapi.feeds.radioplayer.upload.RadioPlayerUploadHealthProbe;
 import org.atlasapi.feeds.radioplayer.upload.RadioPlayerUploadTaskBuilder;
 import org.atlasapi.feeds.radioplayer.upload.RadioPlayerXMLValidator;
 import org.atlasapi.persistence.content.ScheduleResolver;
 import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.joda.time.Duration;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicates;
 import com.google.common.base.Splitter;
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.io.Resources;
 import com.metabroadcast.common.health.HealthProbe;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.properties.Configurer;
 import com.metabroadcast.common.scheduling.RepetitionRules;
 import com.metabroadcast.common.scheduling.RepetitionRules.Every;
 import com.metabroadcast.common.scheduling.SimpleScheduler;
 import com.metabroadcast.common.time.DayRangeGenerator;
 import com.metabroadcast.common.webapp.health.HealthController;
 
 @Configuration
 public class RadioPlayerModule {
 
 	private static final Every UPLOAD_EVERY_TEN_MINUTES = RepetitionRules.every(Duration.standardMinutes(10)).withOffset(Duration.standardMinutes(5));
 	private static final Every UPLOAD_EVERY_TWO_HOURS = RepetitionRules.every(Duration.standardHours(2));
 
 	private @Value("${rp.ftp.enabled}") String upload;
 	private @Value("${rp.ftp.services}") String uploadServices;
     private @Value("${rp.ftp.username}") String ftpUsername;
     private @Value("${rp.ftp.password}") String ftpPassword;
     private @Value("${rp.ftp.host}") String ftpHost;
     private @Value("${rp.ftp.port}") Integer ftpPort;
 	
    private @Autowired @Qualifier("mongoDbQueryExcutorThatFiltersUriQueries") KnownTypeQueryExecutor queryExecutor;
 	private @Autowired SimpleScheduler scheduler;
 	private @Autowired AdapterLog log;
 	private @Autowired DatabasedMongo mongo;
 	private @Autowired HealthController health;
 	private @Autowired ScheduleResolver scheduleResolver;
 	
 	private static DayRangeGenerator dayRangeGenerator = new DayRangeGenerator().withLookAhead(7).withLookBack(7);
 
 	public @Bean RadioPlayerController radioPlayerController() {
 		return new RadioPlayerController();
 	}
 	   
     public @Bean RadioPlayerHealthController radioPlayerHealthController() {
         return new RadioPlayerHealthController(health, Configurer.get("rp.health.password", "").get());
     }
     
     public @Bean RadioPlayerUploadController radioPlayerUploadController() {
         return new RadioPlayerUploadController(radioPlayerUploadTaskBuilder(), dayRangeGenerator, Configurer.get("rp.health.password", "").get());
     }
     
     @Bean FTPFileUploader radioPlayerFileUploader(){
         if(ftpHost != null && ftpPort != null && ftpUsername != null && ftpPassword != null) {
             return new CommonsFTPFileUploader(FTPCredentials.forServer(ftpHost).withPort(ftpPort).withUsername(ftpUsername).withPassword(ftpPassword).build());
         } else {
             return new CommonsFTPFileUploader(FTPCredentials.forServer("Credentials").withUsername("Set").build());
         }
     }
     
     @Bean RadioPlayerFTPUploadResultStore uploadResultRecorder() {
         return new CachingFTPUploadResultStore(new MongoFTPUploadResultStore(mongo));
     }
 
     @Bean RadioPlayerXMLValidator radioPlayerValidator() {
         try {
             return RadioPlayerXMLValidator.forSchemas(ImmutableSet.of(
                 Resources.getResource("epgSI_10.xsd").openStream(), 
                 Resources.getResource("epgSchedule_10.xsd").openStream()
             ));
         } catch (Exception e) {
             log.record(new AdapterLogEntry(Severity.WARN).withDescription("Couldn't load schemas for RadioPlayer XML validation").withCause(e));
             return null;
         }
     }
     
     @Bean RadioPlayerUploadTaskBuilder radioPlayerUploadTaskBuilder() {
         return new RadioPlayerUploadTaskBuilder(radioPlayerFileUploader(), radioPlayerUploadTaskRunner()).withLog(log).withValidator(radioPlayerValidator());
     }
     
     @Bean RadioPlayerRecordingExecutor radioPlayerUploadTaskRunner() {
         return new RadioPlayerRecordingExecutor(uploadResultRecorder());
     }
     
 	@PostConstruct 
 	public void scheduleTasks() {
 	    RadioPlayerFeedCompiler.init(queryExecutor, scheduleResolver);
 		if (Boolean.parseBoolean(upload)) {
 		    FTPCredentials credentials = FTPCredentials.forServer(ftpHost).withPort(ftpPort).withUsername(ftpUsername).withPassword(ftpPassword).build();
 		    
 		    createHealthProbes(credentials);
 			
             scheduler.schedule(radioPlayerUploadTaskBuilder().newTask(uploadServices(), dayRangeGenerator), UPLOAD_EVERY_TWO_HOURS);
             scheduler.schedule(radioPlayerUploadTaskBuilder().newTask(uploadServices(), new DayRangeGenerator()), UPLOAD_EVERY_TEN_MINUTES);
 
             log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass())
             .withDescription(String.format("Radioplayer uploader installed for: %s. Frequency: %s",credentials,UPLOAD_EVERY_TEN_MINUTES)));
 		} else {
 			log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass())
 			.withDescription("Not installing Radioplayer uploader"));
 		}
 	}
 
     @Bean Iterable<RadioPlayerService> uploadServices() {
         if (Strings.isNullOrEmpty(uploadServices) || uploadServices.toLowerCase().equals("all")) {
             return RadioPlayerServices.services;
         } else {
             return Iterables.filter(Iterables.transform(Splitter.on(',').split(uploadServices), new Function<String, RadioPlayerService>() {
                 @Override
                 public RadioPlayerService apply(String input) {
                     return RadioPlayerServices.all.get(input);
                 }
             }), Predicates.notNull());
         }
     }
 
     private void createHealthProbes(FTPCredentials credentials) {
 
         Function<RadioPlayerService, HealthProbe> createProbe = new Function<RadioPlayerService, HealthProbe>() {
             @Override
             public HealthProbe apply(RadioPlayerService service) {
                 return new RadioPlayerUploadHealthProbe(uploadResultRecorder(), service, dayRangeGenerator);
             }
         };
 
         health.addProbes(
                 Iterables.concat(Iterables.transform(uploadServices(), createProbe),
                 ImmutableList.of(new RadioPlayerServerHealthProbe(mongo, credentials)))
         );
     }
     
 }
