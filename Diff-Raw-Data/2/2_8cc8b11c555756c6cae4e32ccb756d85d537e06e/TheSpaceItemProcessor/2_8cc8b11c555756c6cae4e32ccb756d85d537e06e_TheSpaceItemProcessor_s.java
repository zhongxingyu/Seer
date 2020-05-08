 package org.atlasapi.remotesite.space;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.metabroadcast.common.http.SimpleHttpClient;
 import com.metabroadcast.common.http.SimpleHttpRequest;
 import com.metabroadcast.common.intl.Countries;
 import java.util.Iterator;
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.entity.Clip;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.ParentRef;
 import org.atlasapi.media.entity.Policy;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.joda.time.Duration;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.ISODateTimeFormat;
 
 /**
  */
 public class TheSpaceItemProcessor {
 
     private final String BASE_CANONICAL_URI = "http://thespace.org/items/";
     private final String EPISODE_TYPE = "episode";
     //
     private final SimpleHttpClient client;
     private final AdapterLog log;
     private final ContentResolver contentResolver;
     private final ContentWriter contentWriter;
 
     public TheSpaceItemProcessor(SimpleHttpClient client, AdapterLog log, ContentResolver contentResolver, ContentWriter contentWriter) {
         this.client = client;
         this.log = log;
         this.contentResolver = contentResolver;
         this.contentWriter = contentWriter;
     }
 
     public void process(JsonNode item) throws Exception {
         ObjectMapper mapper = new ObjectMapper();
         //
         String type = item.get("type").asText();
         String pid = item.get("pid").asText();
         //
         if (type.equals(EPISODE_TYPE)) {
             Episode episode = (Episode) contentResolver.findByCanonicalUris(ImmutableSet.of(getCanonicalUri(pid))).getFirstValue().valueOrNull();
             if (episode == null) {
                 episode = new Episode();
             }
             makeEpisode(episode, item, mapper);
         }
     }
 
     private void makeEpisode(Episode episode, JsonNode node, ObjectMapper mapper) throws Exception {
         JsonNode pid = node.get("pid");
         episode.setCanonicalUri(getCanonicalUri(pid.asText()));
 
         JsonNode title = node.get("title");
         episode.setTitle(title.asText());
 
         JsonNode position = node.get("position");
         if (position != null) {
             episode.setEpisodeNumber(position.asInt());
         }
 
         JsonNode long_synopsis = node.get("long_synopsis");
         JsonNode medium_synopsis = node.get("medium_synopsis");
         JsonNode short_synopsis = node.get("short_synopsis");
         String synopsis = null;
         if (long_synopsis != null) {
             synopsis = long_synopsis.asText();
         } else if (medium_synopsis != null) {
             synopsis = medium_synopsis.asText();
         } else if (short_synopsis != null) {
             synopsis = short_synopsis.asText();
         }
         episode.setDescription(synopsis);
 
         JsonNode image = node.get("image");
         if (image != null) {
             JsonNode smallImage = image.get("depiction_320");
             if (smallImage != null) {
                 episode.setThumbnail(smallImage.asText());
             }
             JsonNode bigImage = image.get("depiction_640");
             if (bigImage != null) {
                 episode.setImage(bigImage.asText());
             }
         }
 
         Iterator<JsonNode> clips = node.get("available_clips").getElements();
         while (clips.hasNext()) {
             String cPid = clips.next().get("pid").asText();
             JsonNode clip = client.get(new SimpleHttpRequest<JsonNode>(TheSpaceUpdater.BASE_API_URL + "/items/" + cPid + ".json", new JSonNodeHttpResponseTransformer(mapper)));
            episode.addClip(getClip(mapper, clip.get("clip"), episode));
         }
 
         Iterator<JsonNode> versions = node.get("versions").getElements();
         while (versions.hasNext()) {
             String vPid = versions.next().get("pid").asText();
             JsonNode version = client.get(new SimpleHttpRequest<JsonNode>(TheSpaceUpdater.BASE_API_URL + "/items/" + vPid + ".json", new JSonNodeHttpResponseTransformer(mapper)));
             episode.addVersion(getVersion(mapper, version.get("version"), episode));
         }
 
         JsonNode parent = node.get("parent");
         if (parent != null) {
             String pPid = parent.get("pid").asText();
             Series series = (Series) contentResolver.findByCanonicalUris(ImmutableSet.of(getCanonicalUri(pPid))).getFirstValue().valueOrNull();
             if (series == null) {
                 series = new Series();
                 series.setChildRefs(ImmutableList.of(episode.childRef()));
                 fillSeries(series, mapper, client.get(new SimpleHttpRequest<JsonNode>(TheSpaceUpdater.BASE_API_URL + "/items/" + pPid + ".json", new JSonNodeHttpResponseTransformer(mapper))).get("programme"));
             } else {
                 series.setChildRefs(Iterables.concat(series.getChildRefs(), ImmutableList.of(episode.childRef())));
             }
             episode.setParentRef(ParentRef.parentRefFrom(series));
             contentWriter.createOrUpdate(series);
         }
         contentWriter.createOrUpdate(episode);
     }
 
     private Clip getClip(ObjectMapper mapper, JsonNode node, Content parent) throws Exception {
         Clip clip = new Clip();
 
         JsonNode pid = node.get("pid");
         clip.setCanonicalUri(getCanonicalUri(pid.asText()));
 
         JsonNode title = node.get("title");
         clip.setTitle(title.asText());
 
         JsonNode long_synopsis = node.get("long_synopsis");
         JsonNode medium_synopsis = node.get("medium_synopsis");
         JsonNode short_synopsis = node.get("short_synopsis");
         String synopsis = null;
         if (long_synopsis != null) {
             synopsis = long_synopsis.asText();
         } else if (medium_synopsis != null) {
             synopsis = medium_synopsis.asText();
         } else if (short_synopsis != null) {
             synopsis = short_synopsis.asText();
         }
         clip.setDescription(synopsis);
 
         JsonNode image = node.get("image");
         if (image != null) {
             JsonNode smallImage = image.get("depiction_320");
             if (smallImage != null) {
                 clip.setThumbnail(smallImage.asText());
             }
             JsonNode bigImage = image.get("depiction_640");
             if (bigImage != null) {
                 clip.setImage(bigImage.asText());
             }
         }
 
         Iterator<JsonNode> versions = node.get("versions").getElements();
         while (versions.hasNext()) {
             String vPid = versions.next().get("pid").asText();
             JsonNode version = client.get(new SimpleHttpRequest<JsonNode>(TheSpaceUpdater.BASE_API_URL + "/items/" + vPid + ".json", new JSonNodeHttpResponseTransformer(mapper)));
             clip.addVersion(getVersion(mapper, version.get("version"), clip));
         }
 
         return clip;
     }
 
     private Version getVersion(ObjectMapper mapper, JsonNode node, Item parent) {
         Version version = new Version();
 
         JsonNode pid = node.get("pid");
         version.setCanonicalUri(getCanonicalUri(pid.asText()));
 
         JsonNode duration = node.get("duration");
         if (duration != null) {
             version.setDuration(Duration.standardSeconds(Integer.parseInt(duration.asText())));
         }
 
         Iterator<JsonNode> availabilities = node.get("availabilities").getElements();
         while (availabilities.hasNext()) {
             Encoding encoding = new Encoding();
             Location location = new Location();
             Policy policy = new Policy();
             encoding.addAvailableAt(location);
             location.setAvailable(true);
             location.setTransportType(TransportType.LINK);
             location.setUri(parent.getCanonicalUri());
             location.setPolicy(policy);
             policy.setRevenueContract(Policy.RevenueContract.FREE_TO_VIEW);
             policy.setAvailableCountries(ImmutableSet.of(Countries.ALL));
 
             JsonNode availability = availabilities.next();
             JsonNode start = availability.get("start_of_media_availability");
             if (start != null) {
                 policy.setAvailabilityStart(ISODateTimeFormat.dateTimeParser().parseDateTime(start.asText()));
             }
             JsonNode end = availability.get("end_of_media_availability");
             if (end != null) {
                 policy.setAvailabilityEnd(ISODateTimeFormat.dateTimeParser().parseDateTime(end.asText()));
             }
 
             version.addManifestedAs(encoding);
         }
 
         return version;
     }
 
     private Series fillSeries(Series series, ObjectMapper mapper, JsonNode node) throws Exception {
         JsonNode pid = node.get("pid");
         series.setCanonicalUri(getCanonicalUri(pid.asText()));
 
         JsonNode title = node.get("title");
         series.setTitle(title.asText());
 
         JsonNode episodes = node.get("expected_child_count");
         if (episodes != null) {
             series.setTotalEpisodes(episodes.asInt());
         }
 
         JsonNode long_synopsis = node.get("long_synopsis");
         JsonNode medium_synopsis = node.get("medium_synopsis");
         JsonNode short_synopsis = node.get("short_synopsis");
         String synopsis = null;
         if (long_synopsis != null) {
             synopsis = long_synopsis.asText();
         } else if (medium_synopsis != null) {
             synopsis = medium_synopsis.asText();
         } else if (short_synopsis != null) {
             synopsis = short_synopsis.asText();
         }
         series.setDescription(synopsis);
 
         JsonNode image = node.get("image");
         if (image != null) {
             JsonNode smallImage = image.get("depiction_320");
             if (smallImage != null) {
                 series.setThumbnail(smallImage.asText());
             }
             JsonNode bigImage = image.get("depiction_640");
             if (bigImage != null) {
                 series.setImage(bigImage.asText());
             }
         }
 
         Iterator<JsonNode> clips = node.get("available_clips").getElements();
         while (clips.hasNext()) {
             String cPid = clips.next().get("pid").asText();
             JsonNode clip = client.get(new SimpleHttpRequest<JsonNode>(TheSpaceUpdater.BASE_API_URL + "/items/" + cPid + ".json", new JSonNodeHttpResponseTransformer(mapper)));
             series.addClip(getClip(mapper, clip.get("programme"), series));
         }
 
         return series;
     }
 
     private String getCanonicalUri(String pid) {
         return BASE_CANONICAL_URI + pid;
     }
 }
