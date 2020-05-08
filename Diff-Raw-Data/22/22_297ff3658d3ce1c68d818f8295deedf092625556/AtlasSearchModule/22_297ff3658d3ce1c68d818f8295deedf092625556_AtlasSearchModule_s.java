 package org.atlasapi.search;
 
 import static org.atlasapi.persistence.content.listing.ContentListingCriteria.defaultCriteria;
 
 import java.io.File;
 import java.net.UnknownHostException;
 import java.util.List;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.atlasapi.media.channel.CachingChannelStore;
 import org.atlasapi.media.channel.ChannelResolver;
 import org.atlasapi.media.channel.MongoChannelGroupStore;
 import org.atlasapi.media.channel.MongoChannelStore;
 import org.atlasapi.media.entity.Described;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.audit.PersistenceAuditLog;
 import org.atlasapi.persistence.content.ContentCategory;
 import org.atlasapi.persistence.content.LookupResolvingContentResolver;
 import org.atlasapi.persistence.content.cassandra.CassandraContentStore;
 import org.atlasapi.persistence.content.listing.ContentListingCriteria;
 import org.atlasapi.persistence.content.mongo.MongoContentLister;
 import org.atlasapi.persistence.content.mongo.MongoContentResolver;
 import org.atlasapi.persistence.content.mongo.MongoPersonStore;
 import org.atlasapi.persistence.lookup.TransitiveLookupWriter;
 import org.atlasapi.persistence.lookup.entry.LookupEntryStore;
 import org.atlasapi.persistence.lookup.mongo.MongoLookupEntryStore;
 import org.atlasapi.search.loader.ContentBootstrapper;
 import org.atlasapi.search.searcher.BroadcastBooster;
 import org.atlasapi.search.searcher.ChannelGroupBroadcastChannelBooster;
 import org.atlasapi.search.searcher.LuceneContentIndex;
 import org.atlasapi.search.searcher.LuceneSearcherProbe;
 import org.atlasapi.search.searcher.ReloadingContentBootstrapper;
 import org.atlasapi.search.view.JsonSearchResultsView;
 import org.atlasapi.search.www.BackupController;
 import org.atlasapi.search.www.ContentIndexController;
 import org.atlasapi.search.www.DocumentController;
 import org.atlasapi.search.www.WebAwareModule;
 import org.joda.time.Duration;
 import org.springframework.context.annotation.Bean;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicates;
 import com.google.common.base.Splitter;
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableList.Builder;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.metabroadcast.common.health.HealthProbe;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.persistence.mongo.MongoSecondaryReadPreferenceBuilder;
 import com.metabroadcast.common.properties.Configurer;
 import com.metabroadcast.common.scheduling.RepetitionRules;
 import com.metabroadcast.common.scheduling.SimpleScheduler;
 import com.metabroadcast.common.webapp.health.HealthController;
 import com.mongodb.Mongo;
 import com.mongodb.ReadPreference;
 import com.mongodb.ServerAddress;
 import com.netflix.astyanax.AstyanaxContext;
 import com.netflix.astyanax.Keyspace;
 import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
 import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
 import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
 import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
 import com.netflix.astyanax.thrift.ThriftFamilyFactory;
 import com.mongodb.MongoOptions;
 
 public class AtlasSearchModule extends WebAwareModule {
 
 	private final String mongoHost = Configurer.get("mongo.host").get();
 	private final String mongoDbName = Configurer.get("mongo.dbName").get();
 	private final Integer mongoDbPort = Configurer.get("mongo.port").toInt();
     private final String cassandraCluster = Configurer.get("cassandra.cluster").get();
     private final String cassandraKeyspace = Configurer.get("cassandra.keyspace").get();
     private final String cassandraSeeds = Configurer.get("cassandra.seeds").get();
     private final int cassandraPort = Configurer.get("cassandra.port").toInt();
     private final int cassandraMaxConnectionsPerHost = Configurer.get("cassandra.maxConnectionsPerHost").toInt();
     private final int cassandraMaxBlockedThreadsPerHost = Configurer.get("cassandra.maxBlockedThreadsPerHost").toInt();
     private final int cassandraConnectionTimeout = Configurer.get("cassandra.connectionTimeout").toInt();
     private final int cassandraRequestTimeout = Configurer.get("cassandra.requestTimeout").toInt();
     private final String luceneDir = Configurer.get("lucene.contentDir").get();
     private final String luceneIndexAtStartup = Configurer.get("lucene.indexAtStartup", "").get();
 	private final String enablePeople = Configurer.get("people.enabled").get();
 	private final String enableMusic = Configurer.get("music.enabled").get();
 	private final String enableCassandra = Configurer.get("cassandra.enabled").get();
 	private final String priorityChannelGroup = Configurer.get("priorityChannelGroup").get();
 	private final String mongoTag = Strings.emptyToNull(Configurer.get("mongo.db.tag").get());
     private final String mongoFallbackTag = Strings.emptyToNull(Configurer.get("mongo.db.tag.fallback").get());
     private final String backupDirectory = Strings.emptyToNull(Configurer.get("backup.directory").get());
 
     private final MongoSecondaryReadPreferenceBuilder secondaryReadPreferenceBuilder = new MongoSecondaryReadPreferenceBuilder();
     
     @Override
 	public void configure() {
 	    MongoChannelGroupStore channelGroupStore = new MongoChannelGroupStore(mongo());
	    MongoLookupEntryStore lookupEntryStore = new MongoLookupEntryStore(mongo().collection("lookup"), readPreference());
 	    MongoContentResolver contentResolver = new MongoContentResolver(mongo(), lookupEntryStore);
 	    BroadcastBooster booster = new ChannelGroupBroadcastChannelBooster(mongoChannelGroupStore(), channelResolver(), priorityChannelGroup);
 	    CachingChannelStore channelStore = new CachingChannelStore(new MongoChannelStore(mongo(), channelGroupStore, channelGroupStore));
 	    SimpleScheduler simplescheduler = new SimpleScheduler();
 	    	    
 	    channelStore.start();
         LuceneContentIndex index = new LuceneContentIndex(
                 new File(luceneDir), 
                 contentResolver, 
                 booster,
                 channelStore, 
                 backupDirectory
         );
         
         IndexBackupScheduledTask indexBackupTask = new IndexBackupScheduledTask(index);
         simplescheduler.schedule(indexBackupTask, RepetitionRules.every(Duration.standardHours(24)));
         
         Builder<HealthProbe> probes = ImmutableList.builder();
         
         ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
         ReloadingContentBootstrapper mongoBootstrapper = new ReloadingContentBootstrapper(index, mongoBootstrapper(), scheduler, Boolean.valueOf(luceneIndexAtStartup), 180, TimeUnit.MINUTES);
         probes.add(new LuceneSearcherProbe("mongo-lucene", mongoBootstrapper));
         
         ReloadingContentBootstrapper cassandraBootstrapper = null;
         if(Boolean.valueOf(enableCassandra)) {
             cassandraBootstrapper = new ReloadingContentBootstrapper(index, cassandraBootstrapper(),scheduler,  Boolean.valueOf(luceneIndexAtStartup), 7, TimeUnit.DAYS);
             probes.add(new LuceneSearcherProbe("cassandra-lucene", cassandraBootstrapper));
         }
         
         ReloadingContentBootstrapper musicBootStrapper = null;
         if(Boolean.valueOf(enableMusic)) {
             musicBootStrapper = new ReloadingContentBootstrapper(index, musicBootstrapper(), scheduler, true, 120, TimeUnit.MINUTES);
             probes.add(new LuceneSearcherProbe("mongo-music", musicBootStrapper));
         }
         
 		bind("/system/health", new HealthController(probes.build()));
 		bind("/titles", new SearchServlet(new JsonSearchResultsView(), index));
 		bind("/debug/document", new DocumentController(index));
 		bind("/index", new ContentIndexController(new LookupResolvingContentResolver(contentResolver, lookupEntryStore), index));
 		bind("/system/backup", new BackupController(index));
 		
 		mongoBootstrapper.start();
 		
 		if(cassandraBootstrapper != null) {
 		    cassandraBootstrapper.start();
 		}
 		
 		if(musicBootStrapper != null) {
 		    musicBootStrapper.start();
 		}
 	}
 	
 	private ReadPreference readPreference() {
     	ImmutableList.Builder<String> tags = ImmutableList.builder();
         if (mongoTag != null) {
             tags.add(mongoTag);
         }
         
         if (mongoFallbackTag != null) {
             tags.add(mongoFallbackTag);
         }
         
         return secondaryReadPreferenceBuilder.fromProperties(tags.build());
 	}
 	
     @Bean
     ContentBootstrapper mongoBootstrapper() {
         
         ContentListingCriteria criteria = defaultCriteria()
                 .forPublishers(ImmutableSet.<Publisher>builder()
                         .add(Publisher.PA) 
                         .addAll(Publisher.all())
                         .build()
                         .asList()
                         )
                 .forContent(ImmutableSet.of(ContentCategory.CONTAINER, ContentCategory.TOP_LEVEL_ITEM))
                 .build();
         
         ContentBootstrapper bootstrapper = new ContentBootstrapper(criteria);
         bootstrapper.withContentListers(new MongoContentLister(mongo()));
         if (Boolean.valueOf(enablePeople)) {
            LookupEntryStore entryStore = new MongoLookupEntryStore(mongo().collection("peopleLookup"), readPreference());
             bootstrapper.withPeopleListers(new MongoPersonStore(mongo(), TransitiveLookupWriter.explicitTransitiveLookupWriter(entryStore), entryStore, new DummyPersistenceAuditLog()));
         }
         return bootstrapper;
     }
     
     @Bean
     ContentBootstrapper cassandraBootstrapper() {
         ContentBootstrapper bootstrapper = new ContentBootstrapper();
         bootstrapper.withContentListers(cassandra());
         return bootstrapper;
     }
 
     @Bean
     ContentBootstrapper musicBootstrapper() {
         List<Publisher> musicPublishers = ImmutableList.of(Publisher.BBC_MUSIC, Publisher.SPOTIFY, 
             Publisher.YOUTUBE, Publisher.RDIO, Publisher.SOUNDCLOUD, 
             Publisher.AMAZON_UK, Publisher.ITUNES);
         ContentListingCriteria criteria = defaultCriteria()
                 .forContent(ContentCategory.TOP_LEVEL_ITEM)
                 .forPublishers(musicPublishers )
                 .build();
         ContentBootstrapper bootstrapper = new ContentBootstrapper(criteria );
         bootstrapper.withContentListers(new MongoContentLister(mongo()));
         return bootstrapper;
     }
 
     public @Bean ChannelResolver channelResolver() {
         return new MongoChannelStore(mongo(), mongoChannelGroupStore(), mongoChannelGroupStore());
     }
     
     public @Bean MongoChannelGroupStore mongoChannelGroupStore() {
         return new MongoChannelGroupStore(mongo());
     }
     
 	public @Bean DatabasedMongo mongo() {
 		try {
             MongoOptions options = new MongoOptions();
             options.autoConnectRetry = true;
             Mongo mongo = new Mongo(mongoHosts(), options);
             mongo.setReadPreference(readPreference());
             return new DatabasedMongo(mongo, mongoDbName);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
     
     public @Bean CassandraContentStore cassandra() {
 		try {
 		    AstyanaxContext<Keyspace> context = new AstyanaxContext.Builder()
                 .forCluster(cassandraCluster)
                 .forKeyspace(cassandraKeyspace)
                 .withAstyanaxConfiguration(new AstyanaxConfigurationImpl().setDiscoveryType(NodeDiscoveryType.NONE))
                 .withConnectionPoolConfiguration(
                     new ConnectionPoolConfigurationImpl(cassandraCluster)
                         .setPort(cassandraPort)
                         .setMaxBlockedThreadsPerHost(cassandraMaxBlockedThreadsPerHost)
                         .setMaxConnsPerHost(cassandraMaxConnectionsPerHost)
                         .setConnectTimeout(cassandraConnectionTimeout)
                         .setSeeds(cassandraSeeds)
                 )
                 .withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
                 .buildKeyspace(ThriftFamilyFactory.getInstance());
 		    
 			CassandraContentStore cassandraContentStore = new CassandraContentStore(
 			    context, 
                 cassandraRequestTimeout
             );
             cassandraContentStore.init();
             return cassandraContentStore;
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
     
     private List<ServerAddress> mongoHosts() {
         Splitter splitter = Splitter.on(",").omitEmptyStrings().trimResults();
         return ImmutableList.copyOf(Iterables.filter(Iterables.transform(splitter.split(mongoHost), new Function<String, ServerAddress>() {
 
             @Override
             public ServerAddress apply(String input) {
                 try {
                     return new ServerAddress(input, mongoDbPort);
                 } catch (UnknownHostException e) {
                     return null;
                 }
             }
         }), Predicates.notNull()));
     }
     
     private static class DummyPersistenceAuditLog implements PersistenceAuditLog {
 
         @Override
         public void logWrite(Described described) {
             // DO NOTHING
             
         }
 
         @Override
         public void logNoWrite(Described described) {
             // DO NOTHING
         }
        
    };
 }
