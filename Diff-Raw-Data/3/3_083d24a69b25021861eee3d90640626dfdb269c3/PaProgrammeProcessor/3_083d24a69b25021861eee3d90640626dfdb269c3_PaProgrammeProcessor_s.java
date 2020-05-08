 package org.atlasapi.remotesite.pa;
 
 import org.atlasapi.genres.GenreMap;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.atlasapi.remotesite.ContentWriters;
 import org.atlasapi.remotesite.pa.bindings.Billing;
 import org.atlasapi.remotesite.pa.bindings.Category;
 import org.atlasapi.remotesite.pa.bindings.ChannelData;
 import org.atlasapi.remotesite.pa.bindings.ProgData;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.Duration;
 import org.joda.time.format.DateTimeFormat;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.metabroadcast.common.base.Maybe;
 
 public class PaProgrammeProcessor {
     
     private static final String PA_BASE_URL = "http://pressassociation.com";
     
     private final ContentWriters contentWriter;
     private final ContentResolver contentResolver;
     private final AdapterLog log;
     
     private final PaChannelMap channelMap = new PaChannelMap();
     private final GenreMap genreMap = new PaGenreMap();
 
     public PaProgrammeProcessor(ContentWriters contentWriter, ContentResolver contentResolver, AdapterLog log) {
         this.contentWriter = contentWriter;
         this.contentResolver = contentResolver;
         this.log = log;
     }
 
     public void process(ProgData progData, ChannelData channelData, DateTimeZone zone) {
         try {
             Maybe<Brand> brand = Maybe.nothing();
             try {
                 brand = getBrand(progData);
             } catch (ClassCastException e) {
                 log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(PaProgrammeProcessor.class).withDescription("This is definitely where the class cast will happen, with the brand " + e.getMessage()));
             }
             Maybe<Series> series = Maybe.nothing();
             try {
                 series = getSeries(progData);
             } catch (ClassCastException e) {
                 log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(PaProgrammeProcessor.class).withDescription("This is definitely where the class cast will happen, with the series " + e.getMessage()));
             }
             Maybe<Episode> episode = Maybe.nothing();
             try {
                 episode = getEpisode(progData, channelData, zone);
             } catch (ClassCastException e) {
                 log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(PaProgrammeProcessor.class).withDescription("This is definitely where the class cast will happen, with the episode " + e.getMessage()));
             }
 
             if (episode.hasValue()) {
                 if (series.hasValue()) {
                     series.requireValue().addItem(episode.requireValue());
                 }
                 try {
                 if (brand.hasValue()) {
                     contentWriter.createOrUpdateDefinitivePlaylist(brand.requireValue());
                 } else {
                     contentWriter.createOrUpdateDefinitiveItem(episode.requireValue());
                 }
                 } catch (ClassCastException e) {
                     log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(PaProgrammeProcessor.class).withDescription("This is definitely where the class cast will happen, when it's persisted " + e.getMessage()));
                 }
             }
         } catch (Exception e) {
             log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(PaProgrammeProcessor.class).withDescription(e.getMessage()));
         }
     }
 
     private Maybe<Brand> getBrand(ProgData progData) {
         String brandId = progData.getSeriesId();
         if (brandId == null || brandId.trim().isEmpty()) {
             return Maybe.nothing();
         }
 
         String brandUri = PA_BASE_URL + "/brands/" + brandId;
         Content resolvedContent = contentResolver.findByUri(brandUri);
         Brand brand;
         if (resolvedContent instanceof Brand) {
             brand = (Brand) resolvedContent;
         } else {
             brand = new Brand(brandUri, "pa:b-" + brandId, Publisher.PA);
         }
         
         brand.setTitle(progData.getTitle());
         brand.setGenres(genreMap.map(ImmutableSet.copyOf(Iterables.transform(progData.getCategory(), Category.TO_GENRE_URIS))));
 
         /*
          * Pictures currently have no path List<Picture> pictures =
          * progData.getPicture(); for (Picture picture : pictures) {
          * picture.getvalue(); }
          */
 
         return Maybe.just(brand);
     }
 
     private Maybe<Series> getSeries(ProgData progData) {
         if (progData.getSeriesNumber() != null) {
             return Maybe.nothing();
         }
         
         String seriesUri = PA_BASE_URL + "/series/" + progData.getSeriesId() + "-" + progData.getSeriesNumber();
         
         Content resolvedContent = contentResolver.findByUri(seriesUri);
         Series series;
         if (resolvedContent instanceof Series) {
             series = (Series) resolvedContent;
         } else {
             series = new Series(seriesUri, "pa:s-" + progData.getSeriesId() + "-" + progData.getSeriesNumber());
         }
         
         series.setPublisher(Publisher.PA);
         series.setGenres(genreMap.map(ImmutableSet.copyOf(Iterables.transform(progData.getCategory(), Category.TO_GENRE_URIS))));
 
         return Maybe.just(series);
     }
 
     private Maybe<Episode> getEpisode(ProgData progData, ChannelData channelData, DateTimeZone zone) {
         String channelUri = channelMap.getChannelUri(Integer.valueOf(channelData.getChannelId()));
         if (channelUri == null) {
             return Maybe.nothing();
         }
 
         String episodeUri = PA_BASE_URL + "/episodes/" + progData.getProgId();
         Content resolvedContent = contentResolver.findByUri(episodeUri);
         Episode episode;
         if (resolvedContent instanceof Episode) {
             episode = (Episode) resolvedContent;
         } else {
             episode = getBasicEpisode(progData);
         }
 
         Version version = findBestVersion(episode.getVersions());
 
         Duration duration = Duration.standardMinutes(Long.valueOf(progData.getDuration()));
 
         DateTime transmissionTime = getTransmissionTime(progData.getDate(), progData.getTime(), zone);
         Broadcast broadcast = new Broadcast(channelUri, transmissionTime, duration);
         version.addBroadcast(broadcast);
 
         return Maybe.just(episode);
     }
 
     private Version findBestVersion(Iterable<Version> versions) {
         for (Version version : versions) {
             if (version.getProvider() == Publisher.PA) {
                 return version;
             }
         }
 
         return versions.iterator().next();
     }
 
     private Episode getBasicEpisode(ProgData progData) {
         Episode episode = new Episode(PA_BASE_URL + "/episodes/" + progData.getProgId(), "pa:e-" + progData.getProgId(), Publisher.PA);
 
         if (progData.getEpisodeTitle() != null) {
             episode.setTitle(progData.getEpisodeTitle());
         } else {
             episode.setTitle(progData.getTitle());
         }
 
         try {
             if (progData.getEpisodeNumber() != null) {
                 episode.setEpisodeNumber(Integer.valueOf(progData.getEpisodeNumber()));
             }
             if (progData.getSeriesNumber() != null) {
                 episode.setSeriesNumber(Integer.valueOf(progData.getSeriesNumber()));
             }
         } catch (NumberFormatException e) {
             // sometimes we don't get valid numbers
         }
 
         if (progData.getBillings() != null) {
             for (Billing billing : progData.getBillings().getBilling()) {
                 if (billing.getType().equals("synopsis")) {
                     episode.setDescription(billing.getvalue());
                     break;
                 }
             }
         }
 
         episode.setGenres(genreMap.map(ImmutableSet.copyOf(Iterables.transform(progData.getCategory(), Category.TO_GENRE_URIS))));
 
         // episode.setImage(image);
         // episode.setThumbnail(thumbnail);
 
         Version version = new Version();
         version.setProvider(Publisher.PA);
         episode.addVersion(version);
 
         Duration duration = Duration.standardMinutes(Long.valueOf(progData.getDuration()));
         version.setDuration(duration);
 
         episode.addVersion(version);
 
         return episode;
     }
 
     protected static DateTime getTransmissionTime(String date, String time, DateTimeZone zone) {
         String dateString = date + "-" + time;
         return DateTimeFormat.forPattern("dd/MM/yyyy-HH:mm").withZone(zone).parseDateTime(dateString);
     }
 }
