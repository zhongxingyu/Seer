 package org.atlasapi.feeds.interlinking;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.atlasapi.feeds.interlinking.InterlinkBase.Operation;
 import org.atlasapi.feeds.interlinking.InterlinkFeed.InterlinkFeedAuthor;
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Description;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.media.entity.Version;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 
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
     
     public InterlinkFeed fromBrands(String id, Publisher publisher, DateTime from, DateTime to, List<Brand> brands) {
         InterlinkFeed feed = feed(id, publisher);
         
         for (Brand brand: brands) {
             InterlinkBrand interlinkBrand = fromBrand(brand, from, to);
             if (qualifies(from, to, brand)) {
                 feed.addEntry(interlinkBrand);
             }
             
             Map<String, InterlinkSeries> seriesLookup = Maps.newHashMap();
             for (Item item : brand.getItems()) {
                 InterlinkSeries linkSeries = null;
                 if (item instanceof Episode) {
                     Episode episode = (Episode) item;
                     Series series = episode.getSeriesSummary();
                     if (series != null && qualifies(from, to, series)) {
                         linkSeries = seriesLookup.get(series.getCanonicalUri());
                         if (linkSeries == null) {
                             linkSeries = fromSeries(series, interlinkBrand, brand, from, to);
                             feed.addEntry(linkSeries);
                             seriesLookup.put(series.getCanonicalUri(), linkSeries);
                         }
                     }
                 }
                 
                 populateFeedWithItem(feed, item, from, to, (linkSeries != null ? linkSeries : interlinkBrand));
             }
         }
         
         return feed;
     }
     
     static boolean qualifies(DateTime from, DateTime to, Description description) {
         return ((from == null && to == null) || (description != null && description.getLastUpdated() != null && description.getLastUpdated().isAfter(from) && description.getLastUpdated().isBefore(to)));
     }
     
     private InterlinkFeed feed(String id, Publisher publisher) {
         InterlinkFeed feed = new InterlinkFeed(id);
 
        feed.withAuthor(new InterlinkFeedAuthor(publisher.key(), publisher.key().split("\\.")[0]));
         feed.withUpdatedAt(new DateTime());
         
         return feed;
     }
 
     private InterlinkSeries fromSeries(Series series, InterlinkBrand linkBrand, Brand brand, DateTime from, DateTime to) {
         return new InterlinkSeries(idFrom(series), operationFor(series, brand, from, to), series.getSeriesNumber(), linkBrand)
         	.withTitle(series.getTitle())
         	.withDescription(toDescription(series))
         	.withLastUpdated(series.getLastUpdated())
         	.withSummary(toSummary(series))
         	.withThumbnail(series.getImage());
     }
 
 	private void populateFeedWithItem(InterlinkFeed feed, Item item, DateTime from, DateTime to, InterlinkContent parent) {
         InterlinkEpisode episode = new InterlinkEpisode(idFrom(item), operationFor(item, from, to), itemIndexFrom(item), item.getCanonicalUri(), parent)
             .withTitle(item.getTitle())
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
 
         InterlinkOnDemand onDemand = firstLinkLocation(item, from, to, episode);
         if (onDemand != null) {
             feed.addEntry(onDemand);
         }
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
     	for (Item item : brand.getItems()) {
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
 		for (Item item : brand.getItems()) {
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
 
 	protected String idFrom(Description description) {
 		return description.getCanonicalUri();
 	}
 
 	private String toSummary(Content content) {
     	String description = content.getDescription();
 		if (description == null) {
     		return null;
     	}
     	return summaryTruncator.truncate(description);
     }
 	
 	private String toDescription(Content content) {
     	String description = content.getDescription();
 		if (description == null) {
     		return null;
     	}
     	return descriptionTruncator.truncate(description);
     }
 
     protected InterlinkBroadcast fromBroadcast(Broadcast broadcast, InterlinkEpisode episode) {
         String id = broadcast.getBroadcastOn() + "-" + broadcast.getTransmissionTime().getMillis();
         String service = CHANNEL_LOOKUP.get(broadcast.getBroadcastOn());
         
         Operation operation = broadcastOperation(broadcast);
 
         return new InterlinkBroadcast(id, operation, episode)
     		.withLastUpdated(broadcast.getLastUpdated())
         	.withDuration(toDuration(broadcast.getBroadcastDuration()))
         	.withBroadcastStart(broadcast.getTransmissionTime())
         	.withService(service);
     }
     
     protected Operation broadcastOperation(Broadcast broadcast) {
         DateTime thirtyDaysAgo = new DateTime(DateTimeZones.UTC).minusDays(30);
         Operation operation = Operation.STORE;
         if (thirtyDaysAgo.isAfter(broadcast.getTransmissionTime()) || ! broadcast.isActivelyPublished()) {
             operation = Operation.DELETE;
         }
         return operation;
     }
 
     protected Set<Broadcast> broadcasts(Item item) {
         Set<Broadcast> broadcasts = Sets.newHashSet();
         for (Version version : item.nativeVersions()) {
             for (Broadcast broadcast : version.getBroadcasts()) {
                 broadcasts.add(broadcast);
             }
         }
         return broadcasts;
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
     
     protected InterlinkOnDemand firstLinkLocation(Item item, DateTime from, DateTime to, InterlinkEpisode episode) {
         for (Version version : item.nativeVersions()) {
             for (Encoding encoding : version.getManifestedAs()) {
                 for (Location location : encoding.getAvailableAt()) {
                     if (TransportType.LINK.equals(location.getTransportType()) && qualifies(from, to, location)) {
                         return fromLocation(location, episode, version.getDuration());
                     }
                 }
             }
         }
         return null;
     }
     
     protected InterlinkOnDemand fromLocation(Location linkLocation, InterlinkEpisode episode, int d) {
         Duration duration = new Duration(d*1000);
         Operation operation = linkLocation.getAvailable() ? Operation.STORE : Operation.DELETE;
         
         return new InterlinkOnDemand(idFrom(linkLocation), operation, linkLocation.getPolicy().getAvailabilityStart(), linkLocation.getPolicy().getAvailabilityEnd(), duration, episode)
             .withLastUpdated(linkLocation.getLastUpdated())
             .withService("4oD");
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
