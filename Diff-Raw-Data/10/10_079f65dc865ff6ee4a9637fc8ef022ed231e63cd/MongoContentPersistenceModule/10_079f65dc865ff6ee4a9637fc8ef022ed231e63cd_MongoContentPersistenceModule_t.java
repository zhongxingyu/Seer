 package org.atlasapi.persistence;
 
 import org.atlasapi.persistence.content.ContentLister;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.content.KnownTypeContentResolver;
 import org.atlasapi.persistence.content.LookupResolvingContentResolver;
 import org.atlasapi.persistence.content.mongo.MongoContentLister;
 import org.atlasapi.persistence.content.mongo.MongoContentResolver;
 import org.atlasapi.persistence.content.mongo.MongoContentTables;
 import org.atlasapi.persistence.content.mongo.MongoContentWriter;
 import org.atlasapi.persistence.content.mongo.MongoPersonStore;
 import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
 import org.atlasapi.persistence.content.people.QueuingItemsPeopleWriter;
 import org.atlasapi.persistence.content.people.QueuingPersonWriter;
 import org.atlasapi.persistence.content.schedule.mongo.BatchingScheduleWriter;
 import org.atlasapi.persistence.content.schedule.mongo.MongoScheduleStore;
 import org.atlasapi.persistence.content.schedule.mongo.ScheduleWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.lookup.BasicLookupResolver;
 import org.atlasapi.persistence.lookup.mongo.MongoLookupEntryStore;
 import org.atlasapi.persistence.shorturls.MongoShortUrlSaver;
 import org.atlasapi.persistence.shorturls.ShortUrlSaver;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Primary;
 
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.time.SystemClock;
 
 @Configuration
 public class MongoContentPersistenceModule implements ContentPersistenceModule {
 
 	private @Autowired DatabasedMongo db;
 	private @Autowired AdapterLog log;
 	
 	
 	public @Bean ContentWriter contentWriter() {
		return new MongoContentWriter(new MongoContentTables(db), lookupStore(), new SystemClock());
 	}
 	
 	@Primary
 	public @Bean ContentResolver contentResolver() {
 	    return new LookupResolvingContentResolver(knownTypeContentResolver(), new BasicLookupResolver(lookupStore()));
 	}
 	
 	public @Bean KnownTypeContentResolver knownTypeContentResolver() {
	    return new MongoContentResolver(new MongoContentTables(db));
 	}
 	
 	public @Bean MongoLookupEntryStore lookupStore() {
 	    return new MongoLookupEntryStore(db);
 	}
 	
 	public @Bean MongoScheduleStore scheduleStore() {
 	    try {
             return new MongoScheduleStore(db);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
 	}
 	
 	public @Bean ItemsPeopleWriter itemsPeopleWriter() {
 	    return new QueuingItemsPeopleWriter(new QueuingPersonWriter(personStore(), log), log);
 	}
 	
 	public @Bean MongoPersonStore personStore() {
 	    return new MongoPersonStore(db);
 	}
 	
 	public @Bean ScheduleWriter queueingScheduleWriter() {
 		return new BatchingScheduleWriter(scheduleStore(), log);
 	}
 
 	@Override
 	public @Bean ShortUrlSaver shortUrlSaver() {
 		return new MongoShortUrlSaver(db);
 	}
 	
 	public @Bean ContentLister contentListener() {
		return new MongoContentLister(new MongoContentTables(db));
 	}
 }
