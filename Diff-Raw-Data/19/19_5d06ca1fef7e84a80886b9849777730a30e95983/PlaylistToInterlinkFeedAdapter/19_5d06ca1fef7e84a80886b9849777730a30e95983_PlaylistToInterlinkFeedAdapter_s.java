 package org.atlasapi.feeds.interlinking;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.atlasapi.feeds.interlinking.InterlinkBase.Operation;
 import org.atlasapi.feeds.interlinking.InterlinkFeed.InterlinkFeedAuthor;
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Container;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Described;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.media.entity.Version;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 
 import com.google.common.base.Predicate;
 import com.google.common.base.Predicates;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.text.Truncator;
 import com.metabroadcast.common.time.DateTimeZones;
 
 public class PlaylistToInterlinkFeedAdapter implements PlaylistToInterlinkFeed {
     
     protected static final Operation DEFAULT_OPERATION = Operation.STORE;
 
 	private static Map<String, String> channelLookup() {
         Map<String, String> channelLookup = Maps.newHashMap();
         channelLookup.put("http://www.channel4.com", "C4");
         channelLookup.put("http://www.channel4.com/more4", "M4");
         channelLookup.put("http://www.e4.com", "E4");
         return channelLookup;
     }
 	
     public static Map<String, String> CHANNEL_LOOKUP = channelLookup(); 
 
 	private final Truncator summaryTruncator = new Truncator()
 		.withMaxLength(90)
 		.onlyTruncateAtAWordBoundary()
 		.omitTrailingPunctuationWhenTruncated()
 		.onlyStartANewSentenceIfTheSentenceIsAtLeastPercentComplete(50).withOmissionMarker("...");
 	
 	private final Truncator descriptionTruncator = new Truncator()
         .withMaxLength(180)
         .withOmissionMarker("...")
         .onlyTruncateAtAWordBoundary()
         .omitTrailingPunctuationWhenTruncated()
         .onlyStartANewSentenceIfTheSentenceIsAtLeastPercentComplete(50);
     
     public InterlinkFeed fromBrands(String id, Publisher publisher, DateTime from, DateTime to, List<Content> contents) {
         InterlinkFeed feed = feed(id, publisher);
         
         for (Content content : contents) {
         	if (!(content instanceof Brand)) {
         		// ignore non-brands for now, all C4 content is in brands
         		continue;
         	}
         	Brand brand = (Brand) content;
         	
         	boolean synthesized = brand.getCanonicalUri().startsWith("http://www.channel4.com/programmes/synthesized");
         	boolean includeBrand = containerQualifies(from, to, brand) && (!synthesized || (synthesized && brand.getContents().size() > 1));
         	
             InterlinkBrand interlinkBrand = fromBrand(brand, from, to);
             if (includeBrand) {
                 feed.addEntry(interlinkBrand);
             }
             
             Map<String, InterlinkSeries> seriesLookup = Maps.newHashMap();
             for (Item item : brand.getContents()) {
                 InterlinkSeries linkSeries = null;
                 if (item instanceof Episode) {
                     Episode episode = (Episode) item;
                     Series series = episode.getSeriesSummary();
                     if (series != null && containerQualifies(from, to, series)) {
                         linkSeries = seriesLookup.get(series.getCanonicalUri());
                         if (linkSeries == null) {
                             linkSeries = fromSeries(series, interlinkBrand, brand, from, to);
                             feed.addEntry(linkSeries);
                             seriesLookup.put(series.getCanonicalUri(), linkSeries);
                         }
                     }
                 }
                 
                 populateFeedWithItem(feed, item, from, to, (linkSeries != null ? linkSeries : includeBrand ? interlinkBrand : null));
             }
         }
         
         return feed;
     }
     
     static boolean containerQualifies(DateTime from, DateTime to, Container<?> description) {
         return ((from == null && to == null) || (description != null && description.getThisOrChildLastUpdated() != null && description.getThisOrChildLastUpdated().isAfter(from) && description.getThisOrChildLastUpdated().isBefore(to)));
     }
     
     static boolean qualifies(DateTime from, DateTime to, Identified description) {
         return ((from == null && to == null) || (description != null && description.getLastUpdated() != null && description.getLastUpdated().isAfter(from) && description.getLastUpdated().isBefore(to)));
     }
     
     private InterlinkFeed feed(String id, Publisher publisher) {
         InterlinkFeed feed = new InterlinkFeed(id);
 
         feed.withAuthor(new InterlinkFeedAuthor(toPublisherName(publisher), toPublisherName(publisher)));
         feed.withUpdatedAt(new DateTime());
         
         return feed;
     }
 
 	private String toPublisherName(Publisher publisher) {
 		return publisher.key().split("\\.")[0];
 	}
 
     private InterlinkSeries fromSeries(Series series, InterlinkBrand linkBrand, Brand brand, DateTime from, DateTime to) {
         return new InterlinkSeries(idFrom(series), operationFor(series, brand, from, to), series.getSeriesNumber(), linkBrand)
         	.withTitle(extractSeriesTitle(series))
         	.withDescription(toDescription(series))
         	.withLastUpdated(series.getLastUpdated())
         	.withSummary(toSummary(series))
         	.withThumbnail(series.getImage());
     }
 
 	private String extractSeriesTitle(Series series) {
 		String title = series.getTitle();
 		Pattern pattern = Pattern.compile(".*(Series\\s*\\d+).*");
 		Matcher match = pattern.matcher(title);
 		if(match.matches()) {
 			return match.group(1);
 		}
 		return title;
 	}
 
 	private void populateFeedWithItem(InterlinkFeed feed, Item item, DateTime from, DateTime to, InterlinkContent parent) {
 		String episodeId = idFrom(item);
 		
 		InterlinkOnDemand onDemand = firstLinkLocation(item, from, to, episodeId);
 		
 		InterlinkEpisode episode = new InterlinkEpisode(episodeId, operationFor(item, from, to), itemIndexFrom(item), onDemand == null ? linkFrom(item.getCanonicalUri()) : onDemand.uri(), parent)
             .withTitle(extractItemTitle(item))
             .withDescription(toDescription(item))
             .withLastUpdated(item.getLastUpdated())
             .withSummary(toSummary(item))
             .withThumbnail(item.getImage());
         
         if (qualifies(from, to, item)) {
             feed.addEntry(episode);
         }
 
         for (Broadcast broadcast : broadcasts(item)) {
             if (qualifies(from, to, broadcast)) {
                 InterlinkBroadcast interlinkBroadcast = fromBroadcast(broadcast, episode);
                 if (interlinkBroadcast != null) {
                     feed.addEntry(interlinkBroadcast);
                 }
             }
         }
 
         
         if (onDemand != null) {
             feed.addEntry(onDemand);
         }
     }
 
 	protected String linkFrom(String canonicalUri) {
 	    return canonicalUri;
     }
 
     private String extractItemTitle(Item item) {
 		String title = item.getTitle();
 		Pattern pattern = Pattern.compile(".*(Episode\\s*\\d+).*");
 		Matcher match = pattern.matcher(title);
 		if(match.matches()) {
 			return match.group(1);
 		}
 		return title;
 	}
 
     private InterlinkBrand fromBrand(Brand brand, DateTime from, DateTime to) {
         return new InterlinkBrand(idFrom(brand), operationFor(brand, from, to))
 			.withLastUpdated(brand.getLastUpdated())
         	.withTitle(brand.getTitle())
         	.withDescription(toDescription(brand))
         	.withSummary(toSummary(brand))
         	.withThumbnail(brand.getImage());
 
     }
     
     private Operation operationFor(Series series, Brand brand, DateTime from, DateTime to) {
     	for (Item item : brand.getContents()) {
     		if (!(item instanceof Episode)) {
     			continue;
     		}
     		Episode episode = (Episode) item;
     		Series seriesSummary = episode.getSeriesSummary();
     		if (seriesSummary == null) {
     			continue;
     		}
 			if (!seriesSummary.getCanonicalUri().equals(series.getCanonicalUri())) {
     			continue;
     		}
 			if (Operation.STORE.equals(operationFor(item, from, to))) {
 				return Operation.STORE;
 			}
 		}
 		return Operation.DELETE;
     }
 
 	private Operation operationFor(Brand brand, DateTime from, DateTime to) {
 		for (Item item : brand.getContents()) {
 			if (Operation.STORE.equals(operationFor(item, from, to))) {
 				return Operation.STORE;
 			}
 		}
 		return Operation.DELETE;
 	}
 
 	private Operation operationFor(Item item, DateTime from, DateTime to) {
 		Location location = firstQualifyingLocation(item, from, to);
 		
 		boolean activeBroadcast = false;
 		for (Version version: item.nativeVersions()) {
 		    for (Broadcast broadcast: version.getBroadcasts()) {
 		        Operation operation = broadcastOperation(broadcast);
 		        if (Operation.STORE.equals(operation)) {
 		            activeBroadcast = true;
 		        }
 		    }
 		}
 		
 		if ((location == null || !location.getAvailable()) && (! activeBroadcast)) {
 			return Operation.DELETE;
 		} else {
 			return Operation.STORE;
 		}
 	}
 
 	protected String idFrom(Identified description) {
 		return description.getCanonicalUri();
 	}
 
 	private String toSummary(Described content) {
     	String description = content.getDescription();
 		if (description == null) {
     		return null;
     	}
     	return summaryTruncator.truncate(description);
     }
 	
 	private String toDescription(Described content) {
     	String description = content.getDescription();
 		if (description == null) {
     		return "";
     	}
     	return descriptionTruncator.truncate(description);
     }
 
     private InterlinkBroadcast fromBroadcast(Broadcast broadcast, InterlinkEpisode episode) {
         String id = broadcastId(broadcast);
         String service = CHANNEL_LOOKUP.get(broadcast.getBroadcastOn());
         
         Operation operation = broadcastOperation(broadcast);
 
         return new InterlinkBroadcast(id, operation, episode)
     		.withLastUpdated(broadcast.getLastUpdated())
         	.withDuration(toDuration(broadcast.getBroadcastDuration()))
         	.withBroadcastStart(broadcast.getTransmissionTime())
         	.withService(service);
     }
 
 	protected String broadcastId(Broadcast broadcast) {
 		return broadcast.getBroadcastOn() + "-" + broadcast.getTransmissionTime().getMillis();
 	}
     
     private Operation broadcastOperation(Broadcast broadcast) {
         DateTime thirtyDaysAgo = new DateTime(DateTimeZones.UTC).minusDays(30);
         Operation operation = Operation.STORE;
         if (thirtyDaysAgo.isAfter(broadcast.getTransmissionTime()) || ! broadcast.isActivelyPublished()) {
             operation = Operation.DELETE;
         }
         return operation;
     }
 
     private Set<Broadcast> broadcasts(Item item) {
     	Predicate<Broadcast> predicate = broadcastFilter();
         Set<Broadcast> broadcasts = Sets.newHashSet();
         for (Version version : item.nativeVersions()) {
             for (Broadcast broadcast : version.getBroadcasts()) {
             	if (predicate.apply(broadcast)) {
             		broadcasts.add(broadcast);
             	}
             }
         }
         return broadcasts;
     }
     
     protected Predicate<Broadcast> broadcastFilter() {
     	return Predicates.alwaysTrue();
     }
     
     private Location firstQualifyingLocation(Item item, DateTime from, DateTime to) {
 	   for (Version version : item.nativeVersions()) {
            for (Encoding encoding : version.getManifestedAs()) {
                for (Location location : encoding.getAvailableAt()) {
                    if (TransportType.LINK.equals(location.getTransportType()) && qualifies(from, to, location)) {
                 	   return location;
                    }
                }
            }
 	   }
 	   return null;
     }
     
     protected InterlinkOnDemand firstLinkLocation(Item item, DateTime from, DateTime to, String parentId) {
         for (Version version : item.nativeVersions()) {
             for (Encoding encoding : version.getManifestedAs()) {
                 for (Location location : encoding.getAvailableAt()) {
                     if (TransportType.LINK.equals(location.getTransportType()) && qualifies(from, to, location)) {
                         return fromLocation(location, parentId, version.getDuration());
                     }
                 }
             }
         }
         return null;
     }
     
     protected InterlinkOnDemand fromLocation(Location linkLocation, String parentId, Integer durationInSeconds) {
         Duration duration = durationInSeconds == null ? null : Duration.standardSeconds(durationInSeconds);
         Operation operation = linkLocation.getAvailable() ? Operation.STORE : Operation.DELETE;
         
         return new InterlinkOnDemand(idFrom(linkLocation), linkLocation.getUri(), operation, linkLocation.getPolicy().getAvailabilityStart(), linkLocation.getPolicy().getAvailabilityEnd(), duration, parentId)
             .withLastUpdated(linkLocation.getLastUpdated())
             .withService("4od");
     }
 
     protected Integer itemIndexFrom(Item item) {
         if (item instanceof Episode) {
             return ((Episode) item).getEpisodeNumber();
         }
         return null;
     }
 
     static Duration toDuration(Integer seconds) {
         if (seconds != null) {
             return Duration.standardSeconds(seconds);
         }
         return null;
     }
 }
