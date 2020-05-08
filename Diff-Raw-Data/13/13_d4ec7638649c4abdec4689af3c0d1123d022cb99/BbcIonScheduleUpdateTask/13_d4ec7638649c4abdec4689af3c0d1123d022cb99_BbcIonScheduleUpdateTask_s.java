 package org.atlasapi.remotesite.bbc.ion;
 
 import static org.atlasapi.persistence.logging.AdapterLogEntry.Severity.DEBUG;
 import static org.atlasapi.persistence.logging.AdapterLogEntry.Severity.WARN;
 
 import java.util.List;
 
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Container;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.MediaType;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.media.entity.Specialization;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.content.ResolvedContent;
 import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.atlasapi.remotesite.bbc.BbcAliasCompiler;
 import org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.BbcIonDeserializer;
 import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;
 import org.atlasapi.remotesite.bbc.ion.model.IonEpisode;
 import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;
 import org.joda.time.Duration;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.common.primitives.Ints;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.http.SimpleHttpClient;
 
 public class BbcIonScheduleUpdateTask implements Runnable {
 
     private static final String BBC_CURIE_BASE = "bbc:";
     private static final String SLASH_PROGRAMMES_ROOT = "http://www.bbc.co.uk/programmes/";
     private final String uri;
     private final SimpleHttpClient httpClient;
     private final ContentResolver localFetcher;
     private final ContentWriter writer;
     private final BbcIonDeserializer<IonSchedule> deserialiser;
     private final ItemsPeopleWriter itemsPeopleWriter;
     private final AdapterLog log;
     private final BbcItemFetcherClient fetcherClient;
 
     public BbcIonScheduleUpdateTask(String uri, SimpleHttpClient httpClient, ContentResolver localFetcher, ContentWriter writer, BbcIonDeserializer<IonSchedule> deserialiser, ItemsPeopleWriter itemsPeopleWriter, AdapterLog log, BbcItemFetcherClient fetcherClient){
         this.uri = uri;
         this.httpClient = httpClient;
         this.localFetcher = localFetcher;
         this.writer = writer;
         this.deserialiser = deserialiser;
         this.itemsPeopleWriter = itemsPeopleWriter;
         this.log = log;
         this.fetcherClient = fetcherClient;
     }
 
     @Override
     public void run() {
         log.record(new AdapterLogEntry(DEBUG).withSource(getClass()).withDescription("BBC Ion Schedule update for " + uri));
         try {
             IonSchedule schedule = deserialiser.deserialise(httpClient.getContentsOf(uri));
             for (IonBroadcast broadcast : schedule.getBlocklist()) {
                 String itemUri = SLASH_PROGRAMMES_ROOT + broadcast.getEpisodeId();
                 try { 
                 //find and (create and) update item
                 Maybe<Identified> possibleIdentified = localFetcher.findByCanonicalUris(ImmutableList.of(itemUri)).get(itemUri);
                 Identified identified = null;
                 if (!possibleIdentified.hasValue()) {
                     if(fetcherClient != null) {
                         identified = fetcherClient.createItem(broadcast.getEpisodeId());
                     } 
                     if(identified == null) {
                         identified = createItemFrom(broadcast);
                     }
                 } else {
                     identified = possibleIdentified.requireValue();
                 }
                 
                 if (identified instanceof Item) {
                     Item item = (Item) identified;
                     updateItemDetails(item, broadcast);
                     if (item instanceof Episode) {
                         updateEpisodeDetails((Episode) item, broadcast);
                     } else if (hasEpisodeDetails(broadcast.getEpisode())){
                         log.record(new AdapterLogEntry(Severity.INFO).withDescription("Trying to update episode: " + item.getCanonicalUri() + " that turned out to be a "+item.getClass().getSimpleName()).withSource(getClass()));
                     }
                     if(!Strings.isNullOrEmpty(broadcast.getSeriesId()) || !Strings.isNullOrEmpty(broadcast.getBrandId())) {
                         Series series = null;
                         if (!Strings.isNullOrEmpty(broadcast.getSeriesId())) {
                             String seriesUri = SLASH_PROGRAMMES_ROOT + broadcast.getSeriesId();
                             Maybe<Identified> maybeSeries = localFetcher.findByCanonicalUris(ImmutableList.of(seriesUri)).get(seriesUri);
                             
                             if (!maybeSeries.hasValue()) {
                                 series = createSeries(broadcast);
                             } else {
                                 if (maybeSeries.requireValue() instanceof Series) {
                                     series = (Series) maybeSeries.requireValue();
                                 } else {
                                     log.record(new AdapterLogEntry(Severity.WARN).withDescription("Trying to update item: " + item.getCanonicalUri() + " but got back series "+maybeSeries.requireValue().getCanonicalUri()+" that turned out to be a "+maybeSeries.getClass().getSimpleName()).withSource(getClass()));
                                 }
                             }
                                 
                             if (series != null) {
                                 updateSeries(series, broadcast);
                             }
                         }
                    
                        //TODO: need to write series anyway!
                         if (Strings.isNullOrEmpty(broadcast.getBrandId())) {
                        	writer.createOrUpdate(series);
                        	((Episode) item).setSeries(series);
                            
                        } else {
                             String brandUri = SLASH_PROGRAMMES_ROOT + broadcast.getBrandId();
                             Maybe<Identified> maybeIdentified = localFetcher.findByCanonicalUris(ImmutableList.of(brandUri)).get(brandUri);
                             Brand brand = null;
                             
                             if (!maybeIdentified.hasValue()) {
                                 brand = createBrandFrom(broadcast);
                             } else {
                                 Identified ided = maybeIdentified.requireValue();
                                 if (ided instanceof Brand) {
                                     brand = (Brand) ided;
                                 } else if (ided instanceof Series) {
                                     brand = brandFromSeries((Series) ided);
                                     log.record(new AdapterLogEntry(Severity.INFO).withDescription("Update item: " + item.getCanonicalUri() + " but upsold series "+ided.getCanonicalUri()+" turned out to be a brand").withSource(getClass()));
                                 }
                             }
                             
                             if (brand != null) {
                                 updateBrand(brand, broadcast);
                                 item.setContainer(brand);
                                 writer.createOrUpdate(brand);
                             }
                         }
                     }
                     writer.createOrUpdate(item);
                     createOrUpdatePeople((Item) item);
                 }
                 } catch (Exception e) {
                     log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withDescription("BBC Ion Updater failed for " + uri + " trying to process broadcast for " + itemUri).withSource(getClass()));
                 }
             }
         } catch (Exception e) {
             log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withDescription("BBC Ion Updater failed for " + uri).withSource(getClass()));
         }
     }
     
     private void createOrUpdatePeople(Item item) {
         itemsPeopleWriter.createOrUpdatePeople(item);
     }
     
     private Brand brandFromSeries(Series series) {
         Brand brand = new Brand(series.getCanonicalUri(), series.getCurie(), series.getPublisher());
         brand.setEquivalentTo(series.getEquivalentTo());
         brand.setTitle(series.getTitle());
         brand.setDescription(series.getDescription());
         brand.setImage(series.getImage());
         brand.setThumbnail(series.getThumbnail());
         brand.setFirstSeen(series.getFirstSeen());
         brand.setGenres(series.getGenres());
         brand.setMediaType(series.getMediaType());
         brand.setSpecialization(series.getSpecialization());
         
         return brand;
     }
 
     private void updateSeries(Series series, IonBroadcast broadcast) {
         series.setTitle(broadcast.getEpisode().getSeriesTitle());
     }
 
     private Series createSeries(IonBroadcast broadcast) {
         return new Series(SLASH_PROGRAMMES_ROOT + broadcast.getSeriesId(), BBC_CURIE_BASE + broadcast.getSeriesId(), Publisher.BBC);
     }
 
     private Brand createBrandFrom(IonBroadcast broadcast) {
         return new Brand(SLASH_PROGRAMMES_ROOT + broadcast.getBrandId(), BBC_CURIE_BASE + broadcast.getBrandId(), Publisher.BBC);
     }
 
     private void updateBrand(Brand brand, IonBroadcast broadcast) {
         brand.setTitle(broadcast.getEpisode().getBrandTitle());
         BbcIonEpisodeDetailItemFetcherClient.addImagesTo("http://www.bbc.co.uk/iplayer/images/progbrand/", broadcast.getBrandId(), brand);
     }
 
     private Item createItemFrom(IonBroadcast broadcast) {
         IonEpisode ionEpisode = broadcast.getEpisode();
         Item item;
         if (!Strings.isNullOrEmpty(broadcast.getBrandId()) || !Strings.isNullOrEmpty(broadcast.getSeriesId())) {
             item = new Episode(SLASH_PROGRAMMES_ROOT + broadcast.getEpisodeId(), BBC_CURIE_BASE + ionEpisode.getId(), Publisher.BBC);
         } else {
             item = new Item(SLASH_PROGRAMMES_ROOT + broadcast.getEpisodeId(), BBC_CURIE_BASE + ionEpisode.getId(), Publisher.BBC);
         }
         item.setAliases(BbcAliasCompiler.bbcAliasUrisFor(item.getCanonicalUri()));
         item.setIsLongForm(true);
         if (!Strings.isNullOrEmpty(broadcast.getMediaType())) {
             item.setMediaType(MediaType.valueOf(broadcast.getMediaType().toUpperCase()));
         }
         if (ionEpisode.getIsFilm() != null && ionEpisode.getIsFilm()) {
             item.setSpecialization(Specialization.FILM);
         }
         return item;
     }
 
     private void updateEpisodeDetails(Episode item, IonBroadcast broadcast) {
         // not sure how useful this is. there isn't mush else we can do.
         IonEpisode episode = broadcast.getEpisode();
         if (hasEpisodeDetails(episode)) {
             item.setEpisodeNumber(Ints.saturatedCast(episode.getPosition()));
         }
     }
     
     private boolean hasEpisodeDetails(IonEpisode episode) {
         return episode != null && Strings.isNullOrEmpty(episode.getSubseriesId()) && episode.getPosition() != null;
     }
 
     private void updateItemDetails(Item item, IonBroadcast ionBroadcast) {
 
 
         Version broadcastVersion = getBroadcastVersion(item, ionBroadcast);
         if (broadcastVersion == null) {
             broadcastVersion = versionFrom(ionBroadcast);
             item.addVersion(broadcastVersion);
         }
         
         Broadcast broadcast = atlasBroadcastFrom(ionBroadcast);
         if(broadcast!= null) {
             broadcastVersion.addBroadcast(broadcast);
         }
 
         IonEpisode episode = ionBroadcast.getEpisode();
         item.setTitle(episode.getTitle());
         item.setDescription(episode.getSynopsis());
         item.setThumbnail(episode.getMyImageBaseUrl() + episode.getId() + "_150_84.jpg");
         item.setImage(episode.getMyImageBaseUrl() + episode.getId() + "_640_360.jpg");
 
     }
 
     private Version versionFrom(IonBroadcast ionBroadcast) {
         Version version = new Version();
 
         version.setCanonicalUri(SLASH_PROGRAMMES_ROOT + ionBroadcast.getVersionId());
         version.setDuration(Duration.standardSeconds(ionBroadcast.getDuration()));
         version.setProvider(Publisher.BBC);
         
         return version;
     }
 
     private Version getBroadcastVersion(Item item, IonBroadcast ionBroadcast) {
         for (Version version : item.nativeVersions()) {
             if (version.getCanonicalUri().equals(SLASH_PROGRAMMES_ROOT + ionBroadcast.getVersionId())) {
                 return version;
             }
         }
         return null;
     }
 
     private Broadcast atlasBroadcastFrom(IonBroadcast ionBroadcast) {
         String serviceUri = BbcIonServices.get(ionBroadcast.getService());
         if(serviceUri == null) {
             log.record(new AdapterLogEntry(WARN).withDescription("Couldn't find service URI for Ion Service " + ionBroadcast.getService()).withSource(getClass()));
             return null;
         } else {
             Broadcast broadcast = new Broadcast(serviceUri, ionBroadcast.getStart(), ionBroadcast.getEnd());
             broadcast.withId(BBC_CURIE_BASE + ionBroadcast.getId()).setScheduleDate(ionBroadcast.getDate().toLocalDate());
             broadcast.setLastUpdated(ionBroadcast.getUpdated());
             return broadcast;
         }
     }
 }
