 package org.atlasapi.remotesite.worldservice;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.metabroadcast.common.time.DateTimeZones.LONDON;
 import static java.lang.Integer.parseInt;
 import static org.atlasapi.media.entity.MediaType.AUDIO;
 import static org.atlasapi.media.entity.Policy.RevenueContract.PRIVATE;
 import static org.atlasapi.media.entity.Publisher.WORLD_SERVICE;
 import static org.atlasapi.media.entity.Specialization.RADIO;
 import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;
 import static org.atlasapi.remotesite.worldservice.WorldServiceIds.curieFor;
 import static org.atlasapi.remotesite.worldservice.WorldServiceIds.uriFor;
 import static org.atlasapi.remotesite.worldservice.WorldServiceIds.uriForBrand;
 import static org.joda.time.Duration.standardSeconds;
 
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.ParentRef;
 import org.atlasapi.media.entity.Policy;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.remotesite.worldservice.model.WsAudioItem;
 import org.atlasapi.remotesite.worldservice.model.WsProgramme;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.intl.Countries;
 import com.metabroadcast.common.media.MimeType;
 
 public class DefaultWsProgrammeHandler implements WsProgrammeHandler {
 
     private static final DateTime BBC_FOUNDED = new DateTime(1927, 01, 01, 0, 0, 0, 0);
    private static final DateTimeFormatter dayFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(LONDON);
     private static final String LOCATION_PREFIX = "http://bbcdbmsw023.national.core.bbc.co.uk/MediaLibrary";
     private static final String BBC_WORLD_SERVICE_URI = "http://www.bbc.co.uk/services/worldservice";
     
     private final ContentResolver resolver;
     private final ContentWriter writer;
     private final AdapterLog log;
 
     public DefaultWsProgrammeHandler(ContentResolver resolver, ContentWriter writer, AdapterLog log) {
         this.resolver = resolver;
         this.writer = writer;
         this.log = log;
     }
 
     @Override
     public void handle(WsProgramme programme, Iterable<WsAudioItem> audioItems) {
         checkNotNull(programme.getProgId());
         checkNotNull(programme.getSeriesId());
 
         String episodeUri = uriFor(programme);
 
         Maybe<Identified> possibleEpisode = resolver.findByCanonicalUris(ImmutableSet.of(episodeUri)).get(episodeUri);
 
         Episode episode = null;
 
         if (possibleEpisode.hasValue()) {
             Identified resolved = possibleEpisode.requireValue();
             if (resolved instanceof Episode) {
                 episode = (Episode) resolved;
             } else {
                 log.record(errorEntry().withDescription("Resolved %s for episode %s", resolved.getClass().getSimpleName(), episodeUri));
                 return;
             }
         } else {
             episode = new Episode(episodeUri, curieFor(programme), WORLD_SERVICE);
         }
 
         episode.setParentRef(new ParentRef(uriForBrand(programme.getSeriesId())));
         episode.setTitle(titleFrom(programme, audioItems));
         episode.setDescription(programme.getSynopsis());
         if (!Strings.isNullOrEmpty(programme.getEpisodeNo()) && programme.getEpisodeNo().matches("\\d+")) {
             episode.setEpisodeNumber(Integer.parseInt(programme.getEpisodeNo()));
         }
         episode.setGenres(WsGenre.genresForCode(programme.getGenreCode()));
         episode.setMediaType(AUDIO);
         episode.setSpecialization(RADIO);
 
         if (!Iterables.isEmpty(audioItems)) {
             for (WsAudioItem audioItem : audioItems) {
                 Version version = new Version();
 
                 if (!Strings.isNullOrEmpty(audioItem.getDuration()) && audioItem.getDuration().matches("\\d+")) {
                     version.setDuration(new Duration(Long.parseLong(audioItem.getDuration())));
                 }
 
                 Policy policy = policyFor(audioItem);
 
                 String broadcastUri = audioItem.getLinkAudioBroadcastQuality();
                 if (!Strings.isNullOrEmpty(broadcastUri)) {
                     version.addManifestedAs(encodingFrom(policy, broadcastUri, MimeType.AUDIO_WAV));
                 }
 
                 String thumbnailUri = audioItem.getLinkAudioThumbnail();
                 if (!Strings.isNullOrEmpty(thumbnailUri)) {
                     version.addManifestedAs(encodingFrom(policy, thumbnailUri, MimeType.AUDIO_MP3));
                 }
                 if (!version.getManifestedAs().isEmpty()) {
                     episode.addVersion(version);
                 }
             }
         }
 
         Broadcast broadcast = broadcastFrom(programme);
         if (broadcast != null) {
             Version version = Iterables.getFirst(episode.getVersions(), new Version());
             if (version.getDuration() == null) {
                 version.setDuration(Duration.standardSeconds(broadcast.getBroadcastDuration()));
             }
             version.addBroadcast(broadcast);
         }
 
         writer.createOrUpdate(episode);
     }
 
     private String titleFrom(WsProgramme programme, Iterable<WsAudioItem> audioItems) {
         if(!Strings.isNullOrEmpty(programme.getEpisodeTitle())) {
             return programme.getEpisodeTitle();
         }
         for (WsAudioItem audioItem : audioItems) {
             if (!Strings.isNullOrEmpty(audioItem.getTitle())) {
                 return audioItem.getTitle();
             }
         }
         return null;
     }
 
     public Encoding encodingFrom(Policy policy, String uri, MimeType audioEncoding) {
         Encoding encoding = new Encoding();
         encoding.setAudioCoding(audioEncoding);
 
         Location location = new Location();
 
         location.setPolicy(policy);
         location.setTransportType(TransportType.LINK);
         location.setUri(LOCATION_PREFIX+uri.replace("\\", "/"));
 
         encoding.addAvailableAt(location);
         return encoding;
     }
 
     private Policy policyFor(WsAudioItem audioItem) {
         Policy policy = new Policy();
         for (String date : ImmutableList.of(audioItem.getAllowDownloadFrom(), audioItem.getInputDatetime(), audioItem.getLastAmendTimestamp())) {
             DateTime parsed = parse(date);
             if (parsed != null) {
                 policy.setAvailabilityStart(parsed);
                 break;
             }
         }
         policy.setAvailableCountries(ImmutableSet.of(Countries.ALL));
         policy.setRevenueContract(PRIVATE);
         return policy;
     }
 
     private DateTime parse(String date) {
         if (!Strings.isNullOrEmpty(date)) {
             try {
                 return dayFormatter.parseDateTime(date);
             } catch (Exception e) {
                 return null;
             }
         }
         return null;
     }
 
     private Broadcast broadcastFrom(WsProgramme programme) {
         if (!Strings.isNullOrEmpty(programme.getFirstTxDate()) && !Strings.isNullOrEmpty(programme.getProgDuration())) {
             DateTime start = dayFormatter.parseDateTime(programme.getFirstTxDate());
             if (start.isAfter(BBC_FOUNDED)) {
                 Broadcast broadcast = new Broadcast(BBC_WORLD_SERVICE_URI, start, standardSeconds(parseInt(programme.getProgDuration())));
                 broadcast.setScheduleDate(start.toLocalDate());
                 return broadcast;
             }
         }
         return null;
     }
 
 }
