 package org.atlasapi.remotesite.channel4.epg;
 
 import static org.atlasapi.media.entity.Publisher.C4;
 
 import java.util.Set;
 
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Channel;
 import org.atlasapi.media.entity.ChildRef;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.ScheduleEntry;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.media.entity.ScheduleEntry.ItemRefAndBroadcast;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.atlasapi.remotesite.channel4.C4BrandUpdater;
 import org.atlasapi.remotesite.channel4.C4BroadcastBuilder;
 import org.joda.time.DateTime;
 
 import com.google.common.base.Objects;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.time.Clock;
 import com.metabroadcast.common.time.SystemClock;
 
 public class C4EpgBrandlessEntryProcessor {
     
     private static final String REAL_PROGRAMME_BASE = "http://www.channel4.com/programmes/";
     private static final String REAL_TAG_BASE = "tag:www.channel4.com,2009:/programmes/";
   
     private static final String SYNTH_PROGRAMME_BASE = "http://www.channel4.com/programmes/synthesized/";
     private static final String SYNTH_TAG_BASE = "tag:www.channel4.com,2009:/programmes/synthesized/";
 
     private final ContentWriter contentWriter;
     private final ContentResolver contentStore;
     private final C4BrandUpdater brandUpdater;
     private final AdapterLog log;
     private final Clock clock;
 
     public C4EpgBrandlessEntryProcessor(ContentWriter contentWriter, ContentResolver contentStore, C4BrandUpdater brandUpdater, AdapterLog log, Clock clock) {
         this.contentWriter = contentWriter;
         this.contentStore = contentStore;
         this.brandUpdater = brandUpdater;
         this.log = log;
         this.clock = clock;
     }
     
     public C4EpgBrandlessEntryProcessor(ContentWriter contentWriter, ContentResolver contentStore, C4BrandUpdater brandUpdater, AdapterLog log) {
         this(contentWriter, contentStore, brandUpdater, log, new SystemClock());
     }
 
     public ItemRefAndBroadcast process(C4EpgEntry entry, Channel channel) {
         try{
             
             String brandName = brandNameFrom(entry);
             
             //try to get container for the item.
             String brandUri = REAL_PROGRAMME_BASE + brandName;
             Maybe<Identified> maybeBrand = contentStore.findByCanonicalUris(ImmutableList.of(brandUri)).get(brandUri);
             
             DateTime now = clock.now();
             
             if(!maybeBrand.hasValue()) {
                 return writeBrandFromEntry(entry, brandName, channel,now);
             } else {
                 Brand brand = (Brand) maybeBrand.requireValue();
                 return updateEpisodeInBrand(entry, channel, brandName, brand,now);
             }
         } catch (Exception e) {
             log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withCause(e).withDescription("Exception processing brandless entry " + entry.id()));
             return null;
         }
     }
 
     public ItemRefAndBroadcast updateEpisodeInBrand(C4EpgEntry entry, Channel channel, String brandName, Brand brand, DateTime now) {
         return extractRelevantEpisode(entry, brand, brandName, channel, now);
     }
     
     public static String brandNameFrom(C4EpgEntry entry) {
         String realBrandName = C4EpgEntryProcessor.webSafeBrandName(entry);
         return realBrandName != null ? realBrandName : brandName(entry.title());
     }
 
     private static String brandName(String title) {
         return title.replaceAll("[^ a-zA-Z0-9]", "").replaceAll("\\s+", "-").toLowerCase();
     }
 
     private ItemRefAndBroadcast episodeFrom(C4EpgEntry entry, String synthBrandName, Channel channel, Brand brand, DateTime now) {
         
         String slotId = entry.slotId();
         Episode episode = new Episode(episodeUriFrom(entry), "c4:"+synthBrandName +"-"+slotId, C4);
         episode.addAlias(SYNTH_TAG_BASE+synthBrandName+"/"+slotId);
 
         boolean changed = false;
         
         if(!Objects.equal(episode.getTitle(), entry.title())) {
             episode.setTitle(entry.title());
             changed = true;
         }
         if(!Objects.equal(episode.getDescription(), entry.summary())) {
             episode.setDescription(entry.summary());
             changed = true;
         }
         
         if(changed || episode.getLastUpdated() == null) {
             episode.setLastUpdated(now);
         }
         
         Broadcast broadcast = C4EpgEntryProcessor.updateVersion(episode, entry, channel, now);
 
         episode.setContainer(brand);
         contentWriter.createOrUpdate(episode);
         
         return new ItemRefAndBroadcast(episode, broadcast);
     }
 
     public static String episodeUriFrom(C4EpgEntry entry) {
         return SYNTH_PROGRAMME_BASE + brandNameFrom(entry) + "/" + entry.slotId();
     }
 
     /**
      * Give synthesized brands 'real' uris so that when/if they appear in the /programmes feed they are
      * matched up 
      * @return 
      */
     private ItemRefAndBroadcast writeBrandFromEntry(C4EpgEntry entry, String synthBrandName, Channel channel, DateTime now) {
         Brand brand = null;
         try {
             brand = brandUpdater.createOrUpdateBrand(REAL_PROGRAMME_BASE + synthBrandName);
         } catch (Exception e) {
             brand = new Brand(REAL_PROGRAMME_BASE + synthBrandName, "c4:"+synthBrandName, C4);
             brand.addAlias(REAL_TAG_BASE + synthBrandName);
             brand.setTitle(entry.title());
             brand.setLastUpdated(entry.updated());
         }
         contentWriter.createOrUpdate(brand);
         
         return episodeFrom(entry, synthBrandName, channel, brand, now);
     }
 
     private ItemRefAndBroadcast extractRelevantEpisode(C4EpgEntry entry, Brand brand, String synthbrandName, Channel channel, DateTime now) {
         boolean found = false;
         //look for an episode with a broadcast with this entry's id, replace if found.
         Iterable<Episode> subItems = Iterables.filter(contentStore.findByCanonicalUris(Iterables.transform(brand.getChildRefs(), ChildRef.TO_URI)).getAllResolvedResults(), Episode.class);
 		for (Episode episode : subItems) {
             for (Version version : episode.getVersions()) {
                 Set<Broadcast> broadcasts = Sets.newHashSet();
                 Broadcast newBroadcast = null;
                 for (Broadcast broadcast : version.getBroadcasts()) {
                     if(broadcast.getId() != null && broadcast.getId().equals(C4BroadcastBuilder.idFrom(channel.uri(), entry.id()))) {
                         newBroadcast = createBroadcast(entry, channel);
                         broadcasts.add(newBroadcast);
                         found = true;
                     } else {
                         broadcasts.add(broadcast);
                     }
                 }
                 if(found) {
                     version.setBroadcasts(broadcasts);
                    C4EpgEntryProcessor.updateLocation(entry, version);
                     contentWriter.createOrUpdate(episode);
                     return new ScheduleEntry.ItemRefAndBroadcast(episode, newBroadcast);
                 }
             }
         }
         
         return episodeFrom(entry, synthbrandName, channel, brand, now);
     }
 
     private Broadcast createBroadcast(C4EpgEntry entry, Channel channel) {
         Broadcast entryBroadcast = new Broadcast(channel.uri(), entry.txDate(), entry.duration()).withId(C4BroadcastBuilder.idFrom(channel.uri(), entry.id()));
         entryBroadcast.addAlias(C4BroadcastBuilder.aliasFrom(channel.uri(), entry.id()));
         entryBroadcast.setIsActivelyPublished(true);
         entryBroadcast.setLastUpdated(entry.updated() != null ? entry.updated() : new DateTime());
         return entryBroadcast;
     }
 }
