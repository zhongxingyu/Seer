 /* Copyright 2010 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.atlasapi.remotesite.bbc;
 
 import static org.atlasapi.media.entity.Publisher.BBC;
 import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;
 import static org.atlasapi.remotesite.HttpClients.webserviceClient;
 import static org.atlasapi.remotesite.bbc.BbcUriCanonicaliser.bbcProgrammeIdFrom;
 import static org.joda.time.Duration.standardSeconds;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Clip;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Film;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.ParentRef;
 import org.atlasapi.media.entity.Policy;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Restriction;
import org.atlasapi.media.entity.Series;
 import org.atlasapi.media.entity.Specialization;
 import org.atlasapi.media.entity.Topic;
 import org.atlasapi.media.entity.Topic.Type;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.atlasapi.persistence.topic.TopicStore;
 import org.atlasapi.query.content.PerPublisherCurieExpander;
 import org.atlasapi.remotesite.ContentExtractor;
 import org.atlasapi.remotesite.bbc.BbcProgrammeSource.ClipAndVersion;
 import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesContainerRef;
 import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesEpisode;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesSeriesContainer;
 import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesTopic;
 import org.atlasapi.remotesite.bbc.SlashProgrammesVersionRdf.BbcBroadcast;
 import org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers;
 import org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.BbcIonDeserializer;
 import org.atlasapi.remotesite.bbc.ion.model.IonEpisodeDetail;
 import org.atlasapi.remotesite.bbc.ion.model.IonEpisodeDetailFeed;
 import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChange;
 import org.atlasapi.remotesite.bbc.ion.model.IonVersion;
 import org.joda.time.Duration;
 import org.joda.time.Interval;
 import org.joda.time.LocalDate;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.base.Charsets;
 import com.google.common.base.Function;
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.google.common.primitives.Ints;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.http.HttpException;
 import com.metabroadcast.common.http.HttpResponsePrologue;
 import com.metabroadcast.common.http.HttpResponseTransformer;
 import com.metabroadcast.common.http.SimpleHttpRequest;
 import com.metabroadcast.common.time.Clock;
 import com.metabroadcast.common.time.SystemClock;
 
 public class BbcProgrammeGraphExtractor implements ContentExtractor<BbcProgrammeSource, Item> {
 
     private static final String SLASH_PROGRAMMES_ROOT = "http://www.bbc.co.uk/programmes/";
     private static final String EPISODE_DETAIL_PATTERN = "http://www.bbc.co.uk/iplayer/ion/episodedetail/episode/%s/include_broadcasts/1/clips/include/next_broadcasts/1/allow_unavailable/1/format/json";
     static final String FULL_IMAGE_EXTENSION = "_640_360.jpg";
     static final String THUMBNAIL_EXTENSION = "_150_84.jpg";
 
     private final BbcSeriesNumberResolver seriesResolver;
     private final BbcProgrammesPolicyClient policyClient;
     
 	private final Clock clock;
 	private final AdapterLog log;
     private final BbcIonDeserializer<IonEpisodeDetailFeed> ionDeserialiser;
     private final BbcProgrammeEncodingAndLocationCreator encodingCreator;
     private final TopicStore topicStore;
 
     public BbcProgrammeGraphExtractor(BbcSeriesNumberResolver seriesResolver, BbcProgrammesPolicyClient policyClient, TopicStore topicStore, Clock clock, AdapterLog log) {
         this.seriesResolver = seriesResolver;
         this.policyClient = policyClient;
         this.topicStore = topicStore;
 		this.clock = clock;
         this.log = log;
 		this.ionDeserialiser = BbcIonDeserializers.deserializerForClass(IonEpisodeDetailFeed.class);
 		this.encodingCreator = new BbcProgrammeEncodingAndLocationCreator(clock);
     }
 
     public BbcProgrammeGraphExtractor(TopicStore topicStore, AdapterLog log) {
         this(new SeriesFetchingBbcSeriesNumberResolver(), new BbcProgrammesPolicyClient(), topicStore, new SystemClock(), log);
     }
 
     public Item extract(BbcProgrammeSource source) {
 
         String episodeUri = source.getUri();
 
         SlashProgrammesRdf episode = source.episode();
 
         Item item = item(episodeUri, episode);
         
         IonEpisodeDetail episodeDetail = getEpisodeDetail(episodeUri);
         
         Map<String, IonVersion> ionVersions = ImmutableMap.of();
         if(episodeDetail != null) {
             ionVersions = Maps.uniqueIndex(episodeDetail.getVersions(), new Function<IonVersion, String>() {
                 @Override
                 public String apply(IonVersion input) {
                     return SLASH_PROGRAMMES_ROOT+input.getId();
                 }
             });
         }
 
         if (source.versions() != null && !source.versions().isEmpty()) {
         	
 			for (SlashProgrammesVersionRdf versionRdf : source.versions()) {
                 Version version = version(versionRdf);
                 version.setProvider(BBC);
                 
                 IonVersion ionVersion = ionVersions.get(version.getCanonicalUri());
                 if(ionVersion != null) {
                     setDurations(version, ionVersion);
                     version.setLastUpdated(ionVersion.getUpdated());
                     
                     if(!Strings.isNullOrEmpty(ionVersion.getGuidanceText())){ 
                         version.setRestriction(Restriction.from(ionVersion.getGuidanceText()));
                     }
                     
                     version.setManifestedAs(encodingsFrom(ionVersion, bbcProgrammeIdFrom(episodeUri)));
                 } else {
                     Long duration = Long.valueOf(versionRdf.version.duration());
                     version.setDuration(standardSeconds(duration));
                     version.setPublishedDuration(Ints.saturatedCast(duration));
                 }
                 item.addVersion(version);
             }
 
         }
         
         if (source.clips() != null) {
             for (ClipAndVersion clipAndVersion: source.clips()) {
                 if (clipAndVersion.clip().clip() != null && clipAndVersion.clip().clip().uri() != null) {
                     addClipToItem(clipAndVersion.clip(), clipAndVersion.version(), item);
                 }
             }
         }
         
         for (Entry<SlashProgrammesTopic, String> topicUri : source.topics().entrySet()) {
             Maybe<Topic> possibleTopic = topicStore.topicFor("dbpedia", topicUri.getValue());
             if(possibleTopic.isNothing()) {
                 log.record(warnEntry().withSource(getClass()).withDescription("Couldn't get Topic for %s, can't create new one", topicUri.getValue()));
             } else {
                 Topic topic = possibleTopic.requireValue();
                 topic.addPublisher(BBC);
                 topic.setTitle(titleFrom(topicUri.getValue()));
                 topic.setType(typeFrom(topicUri.getKey().resourceUri()));
                 item.addTopic(topic);
                 topicStore.write(topic);
             }
         }
 
         return item;
     }
 
     private Type typeFrom(String resourceUri) {
         String type = resourceUri.substring(resourceUri.indexOf("#")+1);
         for (Type topicType : Topic.Type.values()) {
             if(topicType.key().equals(type)) {
                 return topicType;
             }
         }
         return Topic.Type.UNKNOWN;
     }
 
     private String titleFrom(String dbpediaUri) {
         return dbpediaUri.substring(28).replaceAll("_", " ").replace("%28","(").replace("%29",")").replace("%27","'");
     }
 
     public static void setDurations(Version version, IonVersion ionVersion) {
         if(ionVersion.getDuration() != null) {
             Duration duration = standardSeconds(ionVersion.getDuration());
             version.setDuration(duration);
             version.setPublishedDuration(Ints.saturatedCast(duration.getStandardSeconds()));
         }
     }
 
     private Set<Encoding> encodingsFrom(IonVersion ionVersion, String pid) {
         List<IonOndemandChange> ondemands = ionVersion.getOndemands();
         Set<Encoding> encodings = Sets.newHashSetWithExpectedSize(ondemands.size());
         for (IonOndemandChange ondemand : ondemands) {
             
             Maybe<Encoding> encoding = encodingCreator.createEncoding(ondemand, pid);
             if(encoding.hasValue()) {
                 encodings.add(encoding.requireValue());
             }
         }
         return encodings;
     }
     
     private final HttpResponseTransformer<IonEpisodeDetailFeed> ION_TRANSLATOR = new HttpResponseTransformer<IonEpisodeDetailFeed>() {
 
         @Override
         public IonEpisodeDetailFeed transform(HttpResponsePrologue response, InputStream in) throws HttpException {
             return ionDeserialiser.deserialise(new InputStreamReader(in, response.getCharsetOrDefault(Charsets.UTF_8)));
         }
     };
 
     private IonEpisodeDetail getEpisodeDetail(String episodeUri) {
         try {
             SimpleHttpRequest<IonEpisodeDetailFeed> request = new SimpleHttpRequest<IonEpisodeDetailFeed>(String.format(EPISODE_DETAIL_PATTERN, bbcProgrammeIdFrom(episodeUri)), ION_TRANSLATOR);
             List<IonEpisodeDetail> episodeDetailList = webserviceClient().get(request).getBlocklist();
             if (episodeDetailList == null || episodeDetailList.isEmpty()) {
                 log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withDescription("Empty episode detail for " + episodeUri));
                 return null;
             } else {
                 return episodeDetailList.get(0);
             }
         } catch (HttpException e) {
             log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(getClass()).withDescription("Exception fetching episode detail for " + episodeUri));
             return null;
         } catch (Exception e) {
             log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(getClass()).withDescription("Exception parsing episode detail for " + episodeUri));
             return null;
         }
     }
 
     private Maybe<Integer> seriesNumber(SlashProgrammesRdf episode) {
         if (episode.series() != null && episode.series().uri() != null) {
             return seriesResolver.seriesNumberFor(episode.series().uri());
         }
         return Maybe.nothing();
     }
 
     private Location htmlLinkLocation(String episodeUri) {
         Location location = new Location();
         location.setUri(iplayerPageFrom(episodeUri));
         location.setTransportType(TransportType.LINK);
 
         Maybe<Policy> policy = policyClient.policyForUri(episodeUri);
 
         if (policy.hasValue()) {
             location.setPolicy(policy.requireValue());
         }
 
         location.setAvailable(policy.hasValue() && availableNow(policy.requireValue()));
         return location;
     }
 
     private boolean availableNow(Policy policy) {
 		return new Interval(policy.getAvailabilityStart(), policy.getAvailabilityEnd()).contains(clock.now());
 	}
 
 	private Version version(SlashProgrammesVersionRdf slashProgrammesVersion) {
         Version version = new Version();
         version.setCanonicalUri(SLASH_PROGRAMMES_ROOT+slashProgrammesVersion.pid());
         if (slashProgrammesVersion != null) {
             if (slashProgrammesVersion.broadcastSlots() != null) {
                 version.setBroadcasts(broadcastsFrom(slashProgrammesVersion));
             }
         }
         return version;
     }
 	
     @VisibleForTesting
     static Set<Broadcast> broadcastsFrom(SlashProgrammesVersionRdf slashProgrammesVersion) {
 
         Set<Broadcast> broadcasts = Sets.newHashSet();
 
         Set<BbcBroadcast> bbcBroadcasts = Sets.newHashSet();
 
         if (slashProgrammesVersion.broadcastSlots() != null) {
             bbcBroadcasts.addAll(slashProgrammesVersion.broadcastSlots());
         }
 
         for (BbcBroadcast bbcBroadcast : bbcBroadcasts) {
 
             Broadcast broadcast = new Broadcast(channelUrlFrom(bbcBroadcast.broadcastOn()), bbcBroadcast.broadcastDateTime(), bbcBroadcast.broadcastEndDateTime());
 
             if (bbcBroadcast.scheduleDate != null) {
                 broadcast.setScheduleDate(new LocalDate(bbcBroadcast.scheduleDate()));
             }
             broadcasts.add(broadcast);
         }
 
         return broadcasts;
     }
 
     private static String channelUrlFrom(String broadcastOn) {
         if (broadcastOn.contains("#")) {
             broadcastOn = broadcastOn.substring(0, broadcastOn.indexOf('#'));
         }
         return "http://www.bbc.co.uk" + broadcastOn;
     }
     
     private void addClipToItem(SlashProgrammesRdf clipWrapper, SlashProgrammesVersionRdf versionWrapper, Item item) {
 //        IonEpisodeDetail clipDetail = getEpisodeDetail(clipWrapper.clip().uri); TODO: use this instead of the policy client.
         
         String curie = BbcUriCanonicaliser.curieFor(clipWrapper.clip().uri());
         Clip clip = new Clip(clipWrapper.clip().uri(), curie, Publisher.BBC);
         item.addClip(clip);
         
         Maybe<Integer> seriesNumber = Maybe.nothing();
         if (item instanceof Episode) {
             seriesNumber = Maybe.fromPossibleNullValue(((Episode) item).getSeriesNumber());
         }
         
         SlashProgrammesEpisode slashProgrammesClip = clipWrapper.clip();
         populateItem(clip, slashProgrammesClip, seriesNumber);
         
         if (versionWrapper != null) {
             Location location = htmlLinkLocation(clipWrapper.clip().uri());
             Encoding encoding = new Encoding();
             encoding.addAvailableAt(location);
             Version version = version(versionWrapper);
             version.addManifestedAs(encoding);
             clip.addVersion(version);
         }
     }
 
     private Item item(String episodeUri, SlashProgrammesRdf episode) {
         String curie = BbcUriCanonicaliser.curieFor(episodeUri);
 
         
         Maybe<Integer> seriesNumber = seriesNumber(episode);
         
         Item item;
         if (episode.episode().isFilmFormat()) {
             item = new Film(episodeUri, curie, Publisher.BBC);
        } else if (episode.brand() == null && episode.series() == null) {
             item = new Item(episodeUri, curie, Publisher.BBC);
         } else {
             item = new Episode(episodeUri, curie, Publisher.BBC);

            SlashProgrammesSeriesContainer series = episode.series();
            String seriesUri = series.uri();
            if(series != null && seriesUri != null) {
                ((Episode) item).setSeriesRef(new ParentRef(seriesUri));
                //This will get over written below if there's a brand.
                ((Episode) item).setContainer(new Series(seriesUri, PerPublisherCurieExpander.CurieAlgorithm.BBC.compact(seriesUri), Publisher.BBC));
            } 
            
            SlashProgrammesContainerRef brand = episode.brand();
            if(brand != null && brand.uri() != null) {
                String brandUri = brand.uri();
                ((Episode) item).setContainer(new Brand(brandUri, PerPublisherCurieExpander.CurieAlgorithm.BBC.compact(brandUri), Publisher.BBC));
            }
         }
 
         SlashProgrammesEpisode slashProgrammesEpisode = episode.episode();
         
         populateItem(item, slashProgrammesEpisode, seriesNumber);
 
         return item;
     }
     
     private void populateItem(Item item, SlashProgrammesEpisode slashProgrammesEpisode, Maybe<Integer> seriesNumber) {
         item.setTitle(episodeTitle(slashProgrammesEpisode, seriesNumber));
 
         if (slashProgrammesEpisode != null) {
             item.setDescription(slashProgrammesEpisode.description());
             item.setGenres(new BbcProgrammesGenreMap().map(slashProgrammesEpisode.genreUris()));
             if (slashProgrammesEpisode.getMasterbrand() != null) {
                 MediaType mediaType = BbcMasterbrandMediaTypeMap.lookup(slashProgrammesEpisode.getMasterbrand().getResourceUri()).valueOrNull();
                 item.setMediaType(mediaType);
                 if (slashProgrammesEpisode.isFilmFormat()) {
                     item.setSpecialization(Specialization.FILM);
                 } else if (mediaType != null) {
                     item.setSpecialization(MediaType.VIDEO == mediaType ? Specialization.TV : Specialization.RADIO);
                 }
             }
         }
 
         if (item instanceof Episode) {
             if (seriesNumber.hasValue()) {
                 ((Episode) item).setSeriesNumber(seriesNumber.requireValue());
             }
             Integer episodeNumber = slashProgrammesEpisode.episodeNumber();
             if (episodeNumber != null) {
                 ((Episode) item).setEpisodeNumber(episodeNumber);
             }
         }
 
         Set<String> aliases = BbcAliasCompiler.bbcAliasUrisFor(item.getCanonicalUri());
         if (!aliases.isEmpty()) {
             item.setAliases(aliases);
         }
 
         item.setIsLongForm(true);
 
         item.setThumbnail(thumbnailUrlFrom(item.getCanonicalUri()));
         item.setImage(imageUrlFrom(item.getCanonicalUri()));
     }
 
     private static final Pattern titleIsEpisodeAndNumber = Pattern.compile("^Episode \\d+$");
 
     private String episodeTitle(SlashProgrammesEpisode slashProgrammesEpisode, Maybe<Integer> seriesNumber) {
         String title = slashProgrammesEpisode.title();
         if (seriesNumber.isNothing() || !titleIsEpisodeAndNumber.matcher(title).matches()) {
             return title;
         }
         return "Series " + seriesNumber.requireValue() + " " + title;
     }
 
     private String imageUrlFrom(String episodeUri) {
         return extractImageUrl(episodeUri, FULL_IMAGE_EXTENSION);
     }
 
     private String thumbnailUrlFrom(String episodeUri) {
         return extractImageUrl(episodeUri, THUMBNAIL_EXTENSION);
     }
 
 
 
     private String extractImageUrl(String episodeUri, String suffix) {
         return "http://www.bbc.co.uk/iplayer/images/episode/" + BbcFeeds.pidFrom(episodeUri) + suffix;
     }
 
     public static String iplayerPageFrom(String episodeUri) {
         return "http://www.bbc.co.uk/iplayer/episode/" + BbcFeeds.pidFrom(episodeUri);
     }
 
     @SuppressWarnings(value = "unused")
     private String embedCode(String locationUri, String epImageUrl) {
         return "<embed width=\"640\" height=\"395\""
                 + "flashvars=\"embedReferer=http://www.bbc.co.uk/iplayer/&amp;domId=bip-play-emp&amp;config=http://www.bbc.co.uk/emp/iplayer/config.xml&amp;playlist="
                 + locationUri
                 + "&amp;holdingImage="
                 + epImageUrl
                 + "&amp;config_settings_bitrateFloor=0&amp;config_settings_bitrateCeiling=2500&amp;config_settings_transportHeight=35&amp;config_settings_showPopoutCta=false&amp;config_messages_diagnosticsMessageBody=Insufficient bandwidth to stream this programme. Try downloading instead, or see our diagnostics page.&amp;config_settings_language=en&amp;guidance=unknown\""
                 + "allowscriptaccess=\"always\"" + "allowfullscreen=\"true\" wmode=\"default\" " + "quality=\"high\"" + "bgcolor=\"#000000\"" + "name=\"bbc_emp_embed_bip-play-emp\" "
                 + "id=\"bbc_emp_embed_bip-play-emp\" style=\"\" " + "src=\"http://www.bbc.co.uk/emp/9player.swf?revision=10344_10753\" " + "type=\"application/x-shockwave-flash\"/>";
     }
 }
